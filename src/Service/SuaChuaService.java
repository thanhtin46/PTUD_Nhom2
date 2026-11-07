package Service;

import ConnectDB.ConnectDB;
import DAO.PhieuSuaChua_DAO;
import Entity.DuLieuPhieuSuaChua;
import Entity.NhanVien;
import Entity.QuyenTruyCap;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuaChuaService {

    private final PhieuSuaChua_DAO phieuDAO;

    public SuaChuaService() {
        this(new PhieuSuaChua_DAO());
    }

    public SuaChuaService(PhieuSuaChua_DAO phieuDAO) {
        this.phieuDAO = java.util.Objects.requireNonNull(phieuDAO);
    }

    public String luu(
            NhanVien nguoiThucHien,
            DuLieuPhieuSuaChua duLieu) throws SQLException {

        if (!QuyenTruyCap.co(nguoiThucHien, QuyenTruyCap.Quyen.SC)) {
            throw new IllegalArgumentException(
                    "Bạn không có quyền lưu phiếu sửa chữa.");
        }

        if (duLieu == null) {
            throw new IllegalArgumentException(
                    "Thiếu dữ liệu phiếu sửa chữa.");
        }

        boolean duocPhanCong = QuyenTruyCap.co(
                nguoiThucHien,
                QuyenTruyCap.Quyen.PHAN_CONG
        );

        if (!duocPhanCong
                && duLieu.getMaKyThuatVien() != null
                && !nguoiThucHien.getMaNV().equals(
                duLieu.getMaKyThuatVien())) {

            throw new IllegalArgumentException(
                    "Không được đổi người phụ trách.");
        }

        return phieuDAO.luu(
                duLieu,
                duocPhanCong ? null : nguoiThucHien.getMaNV()
        );
    }

    /**
     * Tự động điều phối KTV cho các đơn đang chờ KTV:
     * - Tìm các KTV xưởng đang rảnh (không có lịch sửa chữa active).
     * - Tìm các đơn sửa chữa đang có trạng thái 'CHO_KTV' (hoặc chưa gán KTV).
     * - Lần lượt gán KTV rảnh vào đơn:
     *   + Nếu đơn có phụ tùng nhưng chưa được xuất kho -> Chuyển trạng thái sang 'CHO_SUA' (Chờ phụ tùng).
     *   + Nếu đơn không có phụ tùng hoặc phụ tùng đã xuất kho -> Chuyển trạng thái sang 'DANG_SUA' (Đang sửa).
     *
     * @return Số lượng đơn đã được tự động gán KTV
     */
    public static int tuDongDieuPhoiKtvChoDonCho() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;
        int soLuongGan = 0;
        try {
            // 1. Tìm các KTV xưởng đang rảnh
            String sqlKtvRanh = """
                SELECT nv.maNV, nv.hoTen
                FROM NhanVien nv
                WHERE (nv.chucVu LIKE N'%sửa chữa%' OR nv.boPhan LIKE N'%sửa chữa%')
                  AND nv.trangThai = N'Đang làm việc'
                  AND NOT EXISTS (
                      SELECT 1 FROM PhieuSuaChua sc
                      WHERE sc.maNV = nv.maNV
                        AND sc.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY')
                  )
                ORDER BY nv.maNV
            """;
            List<String> dsKtvRanh = new ArrayList<>();
            try (Statement s = con.createStatement();
                 ResultSet rs = s.executeQuery(sqlKtvRanh)) {
                while (rs.next()) {
                    dsKtvRanh.add(rs.getString("maNV"));
                }
            }
            if (dsKtvRanh.isEmpty()) return 0;

            // 2. Tìm các phiếu sửa chữa đang chờ KTV
            String sqlPhieuCho = """
                SELECT sc.maPSC, sc.maPTN
                FROM PhieuSuaChua sc
                WHERE sc.trangThai = 'CHO_KTV'
                   OR (sc.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY') AND (sc.maNV IS NULL OR sc.maNV = ''))
                ORDER BY sc.ngayBD ASC
            """;
            List<String[]> dsPhieuCho = new ArrayList<>();
            try (Statement s = con.createStatement();
                 ResultSet rs = s.executeQuery(sqlPhieuCho)) {
                while (rs.next()) {
                    dsPhieuCho.add(new String[]{rs.getString("maPSC"), rs.getString("maPTN")});
                }
            }

            // 3. Gán KTV rảnh vào đơn
            int ktvIndex = 0;
            for (String[] p : dsPhieuCho) {
                if (ktvIndex >= dsKtvRanh.size()) break;
                String maKtv = dsKtvRanh.get(ktvIndex++);
                String maPSC = p[0];
                String maPTN = p[1];

                boolean coPhuTung = false;
                boolean daXuatKho = false;

                try (PreparedStatement psPT = con.prepareStatement("SELECT COUNT(*) FROM ChiTietSuaChua WHERE maPSC = ?")) {
                    psPT.setString(1, maPSC);
                    try (ResultSet rsPT = psPT.executeQuery()) {
                        if (rsPT.next() && rsPT.getInt(1) > 0) coPhuTung = true;
                    }
                }

                if (coPhuTung) {
                    try (PreparedStatement psXK = con.prepareStatement("SELECT TOP 1 trangThai FROM PhieuXuatKho WHERE maPSC = ?")) {
                        psXK.setString(1, maPSC);
                        try (ResultSet rsXK = psXK.executeQuery()) {
                            if (rsXK.next()) {
                                String ttXK = rsXK.getString("trangThai");
                                if (ttXK != null && ttXK.trim().equalsIgnoreCase("Đã xuất")) {
                                    daXuatKho = true;
                                }
                            }
                        }
                    }
                }

                // Khi KTV được gán vào đơn nhưng đơn đó chưa được xuất phụ tùng thì chuyển trạng thái 'Chờ phụ tùng'
                String ttMoi = (coPhuTung && !daXuatKho) ? "CHO_SUA" : "DANG_SUA";
                String ttPTN = (coPhuTung && !daXuatKho) ? "Chờ phụ tùng" : "Đang sửa";

                try (PreparedStatement psUp = con.prepareStatement(
                        "UPDATE PhieuSuaChua SET maNV = ?, trangThai = ? WHERE maPSC = ?")) {
                    psUp.setString(1, maKtv);
                    psUp.setString(2, ttMoi);
                    psUp.setString(3, maPSC);
                    psUp.executeUpdate();
                }

                if (maPTN != null && !maPTN.isBlank()) {
                    try (PreparedStatement psUpPTN = con.prepareStatement(
                            "UPDATE PhieuTiepNhan SET kyThuatVien = ?, trangThaiXe = ? WHERE maPTN = ?")) {
                        psUpPTN.setString(1, maKtv);
                        psUpPTN.setString(2, ttPTN);
                        psUpPTN.setString(3, maPTN);
                        psUpPTN.executeUpdate();
                    }
                }
                soLuongGan++;
            }
        } catch (Exception ex) {
            System.err.println("Lỗi tự động điều phối KTV: " + ex.getMessage());
        }
        return soLuongGan;
    }
}
