package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;
import Entity.ChiTietKho;
import Entity.PhieuKho;
import Entity.PhuTung;

/**
 * DAO kho: phụ tùng, nhà cung cấp, phiếu nhập kho (nhập ngay + chờ nhập UC010).
 *
 * YÊU CẦU DB: bảng PhieuNhapKho phải có thêm 3 cột
 *   ngayDuKienGiao DATE NULL, ghiChu NVARCHAR(255) NULL,
 *   trangThai NVARCHAR(20) NOT NULL DEFAULT 'DA_NHAP'  (CHO_NHAP | DA_NHAP)
 *
 * Giả định: ConnectDB.getConnection() trả về một Connection MỚI mỗi lần gọi
 * (nếu là singleton dùng chung thì bỏ try-with-resources ở Connection).
 */
public class Kho_DAO {

    public static final String TT_CHO_NHAP = "CHO_NHAP";
    public static final String TT_DA_NHAP  = "DA_NHAP";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ====================================================================
    //  TIỆN ÍCH
    // ====================================================================

    private static String fmt(Date d) {
        return d == null ? "" : d.toLocalDate().format(FMT);
    }

    private static Date parseNgay(String s) {
        return Date.valueOf(LocalDate.parse(s.trim(), FMT));
    }

    // ====================================================================
    //  PHỤ TÙNG / NHÀ CUNG CẤP / SINH MÃ
    // ====================================================================

    /** Tất cả phụ tùng kèm tồn kho (LEFT JOIN Kho). */
    public ArrayList<PhuTung> layTatCaPhuTung() {
        ArrayList<PhuTung> ds = new ArrayList<>();
        String sql = """
            SELECT p.maPT, p.tenPT, p.donViTinh, p.donGiaBan,
                   COALESCE(k.soLuongTon, 0) AS soLuongTon
            FROM dbo.PhuTung p
            LEFT JOIN dbo.Kho k ON k.maPT = p.maPT
            ORDER BY p.maPT
            """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhuTung pt = new PhuTung();
                pt.setMaPT(rs.getString("maPT"));
                pt.setTen(rs.getString("tenPT"));
                pt.setDonVi(rs.getString("donViTinh"));
                pt.setDonGia(rs.getLong("donGiaBan"));
                pt.setSoLuongTon(rs.getInt("soLuongTon"));
                ds.add(pt);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi layTatCaPhuTung: " + e.getMessage());
        }
        return ds;
    }

    /** Danh sách NCC dạng {maNCC, tenNCC} để nạp combo. */
    public ArrayList<String[]> layTatCaNhaCungCap() {
        ArrayList<String[]> ds = new ArrayList<>();
        String sql = "SELECT maNCC, tenNCC FROM dbo.NhaCungCap ORDER BY maNCC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new String[]{rs.getString("maNCC"), rs.getString("tenNCC")});
            }
        } catch (SQLException e) {
            System.out.println("Lỗi layTatCaNhaCungCap: " + e.getMessage());
        }
        return ds;
    }

    /** Sinh mã phiếu nhập mới dạng PN001, PN002, ... */
    public String sinhMaPhieuNhapMoi() {
        String sql = """
            SELECT MAX(CAST(SUBSTRING(maPhieuNhap, 3, 18) AS INT))
            FROM dbo.PhieuNhapKho
            WHERE maPhieuNhap LIKE 'PN[0-9]%'
            """;
        int max = 0;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) max = rs.getInt(1);   // NULL -> 0
        } catch (SQLException e) {
            System.out.println("Lỗi sinhMaPhieuNhapMoi: " + e.getMessage());
        }
        return String.format("PN%03d", max + 1);
    }

    // ====================================================================
    //  GHI: NHẬP NGAY / LƯU CHỜ NHẬP / XÁC NHẬN NHẬN HÀNG
    // ====================================================================

    /** Nhập kho ngay: lưu phiếu DA_NHAP và cộng tồn kho. */
    public boolean themPhieuNhap(String maPhieuNhap, String maNCC, String maNV,
                                 String ngayNhap, String ghiChu,
                                 List<ChiTietKho> chiTiet) {
        return luuPhieu(maPhieuNhap, maNCC, maNV, ngayNhap, null, ghiChu, chiTiet, true);
    }

    /** UC010: lưu phiếu CHO_NHAP, KHÔNG cộng tồn kho. */
    public boolean luuPhieuChoNhap(String maPhieuNhap, String maNCC, String maNV,
                                   String ngayLap, String ngayDuKienGiao, String ghiChu,
                                   List<ChiTietKho> chiTiet) {
        return luuPhieu(maPhieuNhap, maNCC, maNV, ngayLap, ngayDuKienGiao, ghiChu, chiTiet, false);
    }

    /** Hàm lõi: ghi phiếu + chi tiết, tuỳ chọn cộng kho, trong một transaction. */
    private boolean luuPhieu(String maPhieuNhap, String maNCC, String maNV,
                             String ngay, String ngayDuKien, String ghiChu,
                             List<ChiTietKho> chiTiet, boolean nhapNgay) {
        if (chiTiet == null || chiTiet.isEmpty()) return false;

        String sqlPhieu = """
            INSERT INTO PhieuNhapKho
            (maPhieuNhap, maNCC, maNV, ngayNhap, tongTien, ngayDuKienGiao, ghiChu, trangThai)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        String sqlCT = """
            INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap, maPT, tenPT, soLuong, donGia)
            VALUES (?, ?, ?, ?, ?)
            """;
        String sqlKho = "UPDATE Kho SET soLuongTon = soLuongTon + ? WHERE maPT = ?";

        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                long tongTien = 0;
                for (ChiTietKho ct : chiTiet) {
                    if (ct.getSoLuong() <= 0 || ct.getDonGia() < 0)
                        throw new SQLException("Số lượng hoặc đơn giá không hợp lệ.");
                    tongTien = Math.addExact(tongTien,
                            Math.multiplyExact((long) ct.getSoLuong(), ct.getDonGia()));
                }

                try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                    ps.setString(1, maPhieuNhap);
                    ps.setString(2, maNCC);
                    ps.setString(3, maNV);
                    ps.setDate(4, parseNgay(ngay));
                    ps.setLong(5, tongTien);
                    if (ngayDuKien == null || ngayDuKien.isBlank())
                        ps.setNull(6, Types.DATE);
                    else
                        ps.setDate(6, parseNgay(ngayDuKien));
                    ps.setString(7, ghiChu == null || ghiChu.isBlank() ? null : ghiChu.trim());
                    ps.setString(8, nhapNgay ? TT_DA_NHAP : TT_CHO_NHAP);
                    ps.executeUpdate();
                }

                try (PreparedStatement psCT = con.prepareStatement(sqlCT);
                     PreparedStatement psKho = con.prepareStatement(sqlKho)) {
                    for (ChiTietKho ct : chiTiet) {
                        psCT.setString(1, maPhieuNhap);
                        psCT.setString(2, ct.getMaPT());
                        psCT.setString(3, ct.getTenPT());
                        psCT.setInt(4, ct.getSoLuong());
                        psCT.setLong(5, ct.getDonGia());
                        psCT.executeUpdate();

                        if (nhapNgay) {
                            psKho.setInt(1, ct.getSoLuong());
                            psKho.setString(2, ct.getMaPT());
                            if (psKho.executeUpdate() != 1)
                                throw new SQLException("Không tìm thấy phụ tùng trong kho: " + ct.getMaPT());
                        }
                    }
                }

                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * UC010: NCC đã giao hàng. Đổi CHO_NHAP -> DA_NHAP, cập nhật ngày nhập = hôm nay
     * và cộng tồn kho theo chi tiết phiếu. Trả về false nếu phiếu không ở trạng thái chờ.
     */
    public boolean xacNhanNhanHang(String maPhieuNhap) {
        String sqlTT = """
            UPDATE PhieuNhapKho
            SET trangThai = ?, ngayNhap = CAST(GETDATE() AS DATE)
            WHERE maPhieuNhap = ? AND trangThai = ?
            """;
        String sqlCong = """
            UPDATE k
            SET k.soLuongTon = k.soLuongTon + ct.soLuong
            FROM Kho k
            JOIN ChiTietPhieuNhapKho ct ON ct.maPT = k.maPT
            WHERE ct.maPhieuNhap = ?
            """;
        String sqlDem = "SELECT COUNT(*) FROM ChiTietPhieuNhapKho WHERE maPhieuNhap = ?";

        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlTT)) {
                    ps.setString(1, TT_DA_NHAP);
                    ps.setString(2, maPhieuNhap);
                    ps.setString(3, TT_CHO_NHAP);
                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;           // không tồn tại hoặc đã nhập rồi
                    }
                }

                int soDong;
                try (PreparedStatement ps = con.prepareStatement(sqlDem)) {
                    ps.setString(1, maPhieuNhap);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        soDong = rs.getInt(1);
                    }
                }

                int daCong;
                try (PreparedStatement ps = con.prepareStatement(sqlCong)) {
                    ps.setString(1, maPhieuNhap);
                    daCong = ps.executeUpdate();
                }
                if (daCong != soDong)
                    throw new SQLException("Có phụ tùng chưa có dòng trong bảng Kho.");

                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Huỷ phiếu chờ nhập (chỉ xoá được phiếu CHO_NHAP). */
    public boolean huyPhieuChoNhap(String maPhieuNhap) {
        String sqlCT = """
            DELETE FROM ChiTietPhieuNhapKho
            WHERE maPhieuNhap = ?
              AND EXISTS (SELECT 1 FROM PhieuNhapKho
                          WHERE maPhieuNhap = ? AND trangThai = ?)
            """;
        String sqlPN = "DELETE FROM PhieuNhapKho WHERE maPhieuNhap = ? AND trangThai = ?";
        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlCT)) {
                    ps.setString(1, maPhieuNhap);
                    ps.setString(2, maPhieuNhap);
                    ps.setString(3, TT_CHO_NHAP);
                    ps.executeUpdate();
                }
                int n;
                try (PreparedStatement ps = con.prepareStatement(sqlPN)) {
                    ps.setString(1, maPhieuNhap);
                    ps.setString(2, TT_CHO_NHAP);
                    n = ps.executeUpdate();
                }
                if (n != 1) { con.rollback(); return false; }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ====================================================================
    //  ĐỌC: PHIẾU NHẬP
    // ====================================================================

    /** Tất cả phiếu nhập (mọi trạng thái), kèm chi tiết. */
    public ArrayList<PhieuKho> layTatCaPhieuNhapKho() {
        return layPhieuNhap(null);
    }

    /** Phiếu đang chờ NCC giao (UC010). */
    public ArrayList<PhieuKho> layPhieuChoNhap() {
        return layPhieuNhap(TT_CHO_NHAP);
    }

    /** @param trangThai null = lấy tất cả */
    public ArrayList<PhieuKho> layPhieuNhap(String trangThai) {
        ArrayList<PhieuKho> ds = new ArrayList<>();
        String sql = """
            SELECT pn.maPhieuNhap, pn.ngayNhap, nv.hoTen AS nguoiLap,
                   ncc.tenNCC, pn.ngayDuKienGiao, pn.ghiChu, pn.trangThai
            FROM dbo.PhieuNhapKho pn
            JOIN dbo.NhanVien nv ON nv.maNV = pn.maNV
            JOIN dbo.NhaCungCap ncc ON ncc.maNCC = pn.maNCC
            """
            + (trangThai != null ? "WHERE pn.trangThai = ? " : "")
            + "ORDER BY pn.ngayNhap DESC, pn.maPhieuNhap DESC";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (trangThai != null) ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuKho p = new PhieuKho();
                    p.setSoPhieu(rs.getString("maPhieuNhap"));
                    p.setLoai(PhieuKho.NHAP);
                    p.setNgay(fmt(rs.getDate("ngayNhap")));
                    p.setNguoiLap(rs.getString("nguoiLap"));
                    p.setNhaCungCap(rs.getString("tenNCC"));
                    p.setNgayDuKienGiao(fmt(rs.getDate("ngayDuKienGiao")));
                    String gc = rs.getString("ghiChu");
                    p.setGhiChu(gc == null ? "" : gc);
                    p.setTrangThai(rs.getString("trangThai"));
                    ds.add(p);
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi layPhieuNhap: " + e.getMessage());
            e.printStackTrace();
        }

        for (PhieuKho p : ds) p.getChiTiet().addAll(layChiTietPhieuNhap(p.getSoPhieu()));
        return ds;
    }

    /** Một phiếu nhập theo mã (kèm chi tiết); null nếu không có. */
    public PhieuKho layPhieuNhapTheoMa(String maPhieuNhap) {
        for (PhieuKho p : layPhieuNhap(null)) {
            if (p.getSoPhieu().equals(maPhieuNhap)) return p;
        }
        return null;
    }

    /** Các dòng chi tiết của một phiếu nhập. */
    public ArrayList<ChiTietKho> layChiTietPhieuNhap(String maPhieuNhap) {
        ArrayList<ChiTietKho> ds = new ArrayList<>();
        String sql = """
            SELECT maPT, tenPT, soLuong, donGia
            FROM dbo.ChiTietPhieuNhapKho
            WHERE maPhieuNhap = ?
            """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieuNhap);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new ChiTietKho(
                            rs.getString("maPT"),
                            rs.getString("tenPT"),
                            rs.getInt("soLuong"),
                            rs.getBigDecimal("donGia").longValue()));
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi layChiTietPhieuNhap: " + e.getMessage());
        }
        return ds;
    }
}