package DAO;

import ConnectDB.ConnectDB;
import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.PhieuTiepNhan;
import Entity.PhuongTien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * PhieuTiepNhan_DAO — Lớp DAO chuyên trách phục vụ Phiếu tiếp nhận xe vào xưởng:
 * - CRUD: Lấy danh sách, Thêm mới, Sửa thông tin, Xóa an toàn từ CSDL SQL Server
 * - Tra cứu đa tiêu chí: Số phiếu, Biển số xe, Khách hàng, SĐT, Yêu cầu
 * - Lọc theo tiến độ xe trong xưởng
 * - Kiểm tra ràng buộc trước khi xóa
 */
public class PhieuTiepNhan_DAO {

    /**
     * Lấy toàn bộ danh sách phiếu tiếp nhận từ SQL Server
     */
    public ArrayList<PhieuTiepNhan> layTatCaPhieuTiepNhan() {
        ArrayList<PhieuTiepNhan> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return fallbackCache();

        String sql = "SELECT tn.maPTN, tn.maKH, kh.hoTen, kh.soDT, pt.bienSoXe, pt.hangXe, pt.dongXe, " +
                     "CONVERT(VARCHAR(10), tn.ngayTN, 103) AS ngayTNStr, tn.yeuCauSC, tn.trangThaiXe, nv.hoTen AS tenNV, " +
                     "sc.maNV AS maNVPhanCong, ktv.hoTen AS tenKtvPhanCong " +
                     "FROM PhieuTiepNhan tn " +
                     "JOIN KhachHang kh ON tn.maKH = kh.maKH " +
                     "JOIN PhuongTien pt ON tn.soVIN = pt.soVIN " +
                     "LEFT JOIN NhanVien nv ON tn.maNV = nv.maNV " +
                     "LEFT JOIN PhieuSuaChua sc ON (sc.maPTN = tn.maPTN OR (sc.maPTN IS NULL AND ('SC' + SUBSTRING(tn.maPTN, 3, 10)) = sc.maPSC)) " +
                     "LEFT JOIN NhanVien ktv ON sc.maNV = ktv.maNV " +
                     "ORDER BY tn.maPTN DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String maTN = rs.getString("maPTN");
                String maKH = rs.getString("maKH");
                String hoTen = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String bienSo = rs.getString("bienSoXe");
                String hangXe = rs.getString("hangXe");
                String loaiXe = rs.getString("dongXe");
                String ngayTN = rs.getString("ngayTNStr");
                String yeuCau = rs.getString("yeuCauSC");
                String trangThai = rs.getString("trangThaiXe");
                String nguoiLap = rs.getString("tenNV");
                String maKtv = rs.getString("maNVPhanCong");

                PhieuTiepNhan p = new PhieuTiepNhan(
                        maTN, maKH, hoTen, sdt, bienSo, hangXe, loaiXe, ngayTN, yeuCau,
                        trangThai != null ? trangThai : "Chờ tiếp nhận",
                        nguoiLap != null ? nguoiLap : "Tiếp tân", 0,
                        maKtv != null ? maKtv : ""
                );
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhieuTiepNhan_DAO.layTatCaPhieuTiepNhan: " + e.getMessage());
            return fallbackCache();
        }
        return list;
    }

    /**
     * Thêm mới phiếu tiếp nhận vào SQL Server
     */
    public boolean themPhieuTiepNhan(PhieuTiepNhan ptn, String maNV) {
        if (ptn == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // 1. Xác định số VIN của xe
            String soVIN = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT soVIN FROM PhuongTien WHERE bienSoXe = ?")) {
                ps.setString(1, ptn.getBienSo());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) soVIN = rs.getString("soVIN");
                }
            }

            if (soVIN.isEmpty()) {
                soVIN = "VIN_" + ptn.getBienSo().replaceAll("[^A-Za-z0-9]", "");
                // Thêm xe nhanh vào CSDL nếu chưa có
                new PhuongTien_DAO().themPhuongTien(new PhuongTien(
                        ptn.getBienSo(), ptn.getHangXe(), ptn.getLoaiXe(), soVIN,
                        "", "Mặc định", "2022", "0", ptn.getMaKH(), ptn.getNgayTiepNhan()
                ));
            }

            // 2. Xác định mã khách hàng
            String maKH = ptn.getMaKH();
            if (maKH == null || maKH.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM PhuongTien WHERE soVIN = ?")) {
                    ps.setString(1, soVIN);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) maKH = rs.getString("maKH");
                    }
                }
            }
            if (maKH == null || maKH.isEmpty()) maKH = "KH001";

            // 3. Xác định nhân viên lập
            String nvLap = (maNV != null && !maNV.isEmpty()) ? maNV : "NV01";

            // 4. Thêm phiếu tiếp nhận
            String sqlTN = "INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) " +
                           "VALUES (?, ?, ?, ?, SYSDATETIME(), ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlTN)) {
                stmt.setString(1, ptn.getSoPhieu());
                stmt.setString(2, nvLap);
                stmt.setString(3, maKH);
                stmt.setString(4, soVIN);
                stmt.setString(5, ptn.getYeuCau() != null ? ptn.getYeuCau() : "Yêu cầu kiểm tra & sửa chữa");
                stmt.setString(6, ptn.getTienDo() != null ? ptn.getTienDo() : "Chờ tiếp nhận");
                stmt.executeUpdate();
            }

            // 5. Thêm phiếu sửa chữa tương ứng để theo dõi tiến độ kỹ thuật
            String maPSC = "SC" + ptn.getSoPhieu().replaceAll("[^0-9]", "");
            if (maPSC.length() <= 2) maPSC = "SC00" + ptn.getSoPhieu();
            String sqlSC = "INSERT INTO PhieuSuaChua (maPSC, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) " +
                           "VALUES (?, ?, ?, ?, SYSDATETIME(), DATEADD(day, 2, SYSDATETIME()), ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlSC)) {
                stmt.setString(1, maPSC);
                stmt.setString(2, nvLap);
                stmt.setString(3, maKH);
                stmt.setString(4, soVIN);
                stmt.setString(5, ptn.getTienDo() != null ? ptn.getTienDo() : "Chờ tiếp nhận");
                stmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi PhieuTiepNhan_DAO.themPhieuTiepNhan: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin phiếu tiếp nhận trong SQL Server
     */
    public boolean capNhatPhieuTiepNhan(PhieuTiepNhan ptn) {
        if (ptn == null || ptn.getSoPhieu() == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // Xác định số VIN từ biển số xe
            String soVIN = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT soVIN FROM PhuongTien WHERE bienSoXe = ?")) {
                ps.setString(1, ptn.getBienSo());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) soVIN = rs.getString("soVIN");
                }
            }

            StringBuilder sql = new StringBuilder("UPDATE PhieuTiepNhan SET yeuCauSC = ?, trangThaiXe = ? ");
            if (!soVIN.isEmpty()) {
                sql.append(", soVIN = ? ");
            }
            if (ptn.getMaKH() != null && !ptn.getMaKH().isEmpty()) {
                sql.append(", maKH = ? ");
            }
            sql.append("WHERE maPTN = ?");

            try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
                int idx = 1;
                stmt.setString(idx++, ptn.getYeuCau() != null ? ptn.getYeuCau() : "");
                stmt.setString(idx++, ptn.getTienDo() != null ? ptn.getTienDo() : "Chờ tiếp nhận");
                if (!soVIN.isEmpty()) {
                    stmt.setString(idx++, soVIN);
                }
                if (ptn.getMaKH() != null && !ptn.getMaKH().isEmpty()) {
                    stmt.setString(idx++, ptn.getMaKH());
                }
                stmt.setString(idx, ptn.getSoPhieu());
                stmt.executeUpdate();
            }

            // Đồng bộ trạng thái sang PhieuSuaChua
            String maPSC = "SC" + ptn.getSoPhieu().replaceAll("[^0-9]", "");
            try (PreparedStatement stmt = con.prepareStatement("UPDATE PhieuSuaChua SET trangThai = ? WHERE maPSC = ?")) {
                stmt.setString(1, ptn.getTienDo() != null ? ptn.getTienDo() : "Chờ tiếp nhận");
                stmt.setString(2, maPSC);
                stmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi PhieuTiepNhan_DAO.capNhatPhieuTiepNhan: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa phiếu tiếp nhận và các dữ liệu liên quan an toàn khỏi CSDL
     */
    public boolean xoaPhieuTiepNhan(String soPhieu) {
        if (soPhieu == null || soPhieu.trim().isEmpty()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String maPTN = soPhieu.trim();
        String maPSC = "SC" + maPTN.replaceAll("[^0-9]", "");

        try {
            // 1. Xóa chi tiết hóa đơn & hóa đơn liên quan (nếu có)
            // HoaDon khong co soPhieuSC

            // 2. Xóa chi tiết sửa chữa & phiếu sửa chữa
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietSuaChua WHERE maPSC = ?")) {
                ps.setString(1, maPSC);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuSuaChua WHERE maPSC = ?")) {
                ps.setString(1, maPSC);
                ps.executeUpdate();
            }

            // 3. Xóa phiếu tiếp nhận
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuTiepNhan WHERE maPTN = ?")) {
                ps.setString(1, maPTN);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhieuTiepNhan_DAO.xoaPhieuTiepNhan: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tra cứu phiếu tiếp nhận theo từ khóa, tiêu chí và tiến độ
     * @param tuKhoa Từ khóa tìm kiếm
     * @param tieuChi 0: Tất cả, 1: Số phiếu, 2: Biển số xe, 3: Khách hàng, 4: Yêu cầu
     * @param locTienDo "TAT_CA", "Chờ tiếp nhận", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"
     */
    public ArrayList<PhieuTiepNhan> traCuuPhieuTiepNhan(String tuKhoa, int tieuChi, String locTienDo) {
        ArrayList<PhieuTiepNhan> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return fallbackTraCuuCache(tuKhoa, tieuChi, locTienDo);

        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT tn.maPTN, tn.maKH, kh.hoTen, kh.soDT, pt.bienSoXe, pt.hangXe, pt.dongXe, ")
           .append("CONVERT(VARCHAR(10), tn.ngayTN, 103) AS ngayTNStr, tn.yeuCauSC, tn.trangThaiXe, nv.hoTen AS tenNV, ")
           .append("sc.maNV AS maNVPhanCong, ktv.hoTen AS tenKtvPhanCong ")
           .append("FROM PhieuTiepNhan tn ")
           .append("JOIN KhachHang kh ON tn.maKH = kh.maKH ")
           .append("JOIN PhuongTien pt ON tn.soVIN = pt.soVIN ")
           .append("LEFT JOIN NhanVien nv ON tn.maNV = nv.maNV ")
           .append("LEFT JOIN PhieuSuaChua sc ON (sc.maPTN = tn.maPTN OR (sc.maPTN IS NULL AND ('SC' + SUBSTRING(tn.maPTN, 3, 10)) = sc.maPSC)) ")
           .append("LEFT JOIN NhanVien ktv ON sc.maNV = ktv.maNV ")
           .append("WHERE 1=1 ");

        // Lọc theo tiến độ
        if (locTienDo != null && !locTienDo.isEmpty() && !"TAT_CA".equalsIgnoreCase(locTienDo) && !"Tất cả tiến độ".equalsIgnoreCase(locTienDo)) {
            sql.append("AND LOWER(tn.trangThaiXe) LIKE ? ");
        }

        // Lọc theo từ khóa và tiêu chí
        if (!q.isEmpty()) {
            switch (tieuChi) {
                case 1 -> sql.append("AND LOWER(tn.maPTN) LIKE ? ");
                case 2 -> sql.append("AND LOWER(pt.bienSoXe) LIKE ? ");
                case 3 -> sql.append("AND (LOWER(kh.hoTen) LIKE ? OR LOWER(kh.soDT) LIKE ? OR LOWER(tn.maKH) LIKE ?) ");
                case 4 -> sql.append("AND LOWER(tn.yeuCauSC) LIKE ? ");
                default -> {
                    sql.append("AND (LOWER(tn.maPTN) LIKE ? ")
                       .append("OR LOWER(pt.bienSoXe) LIKE ? ")
                       .append("OR LOWER(kh.hoTen) LIKE ? ")
                       .append("OR LOWER(kh.soDT) LIKE ? ")
                       .append("OR LOWER(tn.maKH) LIKE ? ")
                       .append("OR LOWER(pt.hangXe) LIKE ? ")
                       .append("OR LOWER(pt.dongXe) LIKE ? ")
                       .append("OR LOWER(tn.yeuCauSC) LIKE ?) ");
                }
            }
        }

        sql.append("ORDER BY tn.maPTN DESC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int pIdx = 1;
            if (locTienDo != null && !locTienDo.isEmpty() && !"TAT_CA".equalsIgnoreCase(locTienDo) && !"Tất cả tiến độ".equalsIgnoreCase(locTienDo)) {
                stmt.setString(pIdx++, "%" + locTienDo.trim().toLowerCase() + "%");
            }

            if (!q.isEmpty()) {
                String pat = "%" + q + "%";
                switch (tieuChi) {
                    case 1, 2, 4 -> stmt.setString(pIdx++, pat);
                    case 3 -> {
                        stmt.setString(pIdx++, pat);
                        stmt.setString(pIdx++, pat);
                        stmt.setString(pIdx++, pat);
                    }
                    default -> {
                        for (int i = 0; i < 8; i++) {
                            stmt.setString(pIdx++, pat);
                        }
                    }
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maTN = rs.getString("maPTN");
                    String maKH = rs.getString("maKH");
                    String hoTen = rs.getString("hoTen");
                    String sdt = rs.getString("soDT");
                    String bienSo = rs.getString("bienSoXe");
                    String hangXe = rs.getString("hangXe");
                    String loaiXe = rs.getString("dongXe");
                    String ngayTN = rs.getString("ngayTNStr");
                    String yeuCau = rs.getString("yeuCauSC");
                    String trangThai = rs.getString("trangThaiXe");
                    String nguoiLap = rs.getString("tenNV");
                    String maKtv = rs.getString("maNVPhanCong");

                    list.add(new PhieuTiepNhan(
                            maTN, maKH, hoTen, sdt, bienSo, hangXe, loaiXe, ngayTN, yeuCau,
                            trangThai != null ? trangThai : "Chờ tiếp nhận",
                            nguoiLap != null ? nguoiLap : "Tiếp tân", 0,
                            maKtv != null ? maKtv : ""
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhieuTiepNhan_DAO.traCuuPhieuTiepNhan: " + e.getMessage());
            return fallbackTraCuuCache(tuKhoa, tieuChi, locTienDo);
        }

        return list;
    }

    private ArrayList<PhieuTiepNhan> fallbackCache() {
        return new ArrayList<>(KhoDuLieu.get().getPhieuTiepNhans());
    }

    private ArrayList<PhieuTiepNhan> fallbackTraCuuCache(String tuKhoa, int tieuChi, String locTienDo) {
        ArrayList<PhieuTiepNhan> list = new ArrayList<>();
        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        for (PhieuTiepNhan p : KhoDuLieu.get().getPhieuTiepNhans()) {
            if (locTienDo != null && !locTienDo.isEmpty() && !"TAT_CA".equalsIgnoreCase(locTienDo) && !"Tất cả tiến độ".equalsIgnoreCase(locTienDo)) {
                if (p.getTienDo() == null || !p.getTienDo().toLowerCase().contains(locTienDo.toLowerCase())) {
                    continue;
                }
            }

            boolean match = q.isEmpty();
            if (!match) {
                switch (tieuChi) {
                    case 1 -> match = p.getSoPhieu() != null && p.getSoPhieu().toLowerCase().contains(q);
                    case 2 -> match = p.getBienSo() != null && p.getBienSo().toLowerCase().contains(q);
                    case 3 -> match = (p.getHoTen() != null && p.getHoTen().toLowerCase().contains(q))
                            || (p.getSdt() != null && p.getSdt().contains(q))
                            || (p.getMaKH() != null && p.getMaKH().toLowerCase().contains(q));
                    case 4 -> match = p.getYeuCau() != null && p.getYeuCau().toLowerCase().contains(q);
                    default -> {
                        String gop = (p.getSoPhieu() + " " + p.getBienSo() + " " + p.getHoTen() + " "
                                + p.getSdt() + " " + p.getHangXe() + " " + p.getLoaiXe() + " " + p.getYeuCau()).toLowerCase();
                        match = gop.contains(q);
                    }
                }
            }

            if (match) {
                list.add(p);
            }
        }
        return list;
    }

    public int layTongSoPhieu() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return KhoDuLieu.get().getPhieuTiepNhans().size();

        String sql = "SELECT COUNT(*) FROM PhieuTiepNhan";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Lỗi PhieuTiepNhan_DAO.layTongSoPhieu: " + e.getMessage());
        }
        return KhoDuLieu.get().getPhieuTiepNhans().size();
    }

    /**
     * Sinh mã số phiếu tiếp nhận tự động thông minh:
     * - Tự động lấp khoảng trống khi xóa phiếu (trở lại số phiếu vừa xóa)
     * - Không tăng lên vô tận khi bấm làm mới hoặc hủy
     * - Luôn bắt đầu từ TN001 nếu chưa có phiếu nào
     */
    public String sinhMaTiepNhanTuDong() {
        Connection con = ConnectDB.getInstance().getConnection();
        java.util.TreeSet<Integer> dsSo = new java.util.TreeSet<>();

        // 1. Quét danh sách mã phiếu từ CSDL SQL Server
        if (con != null) {
            String sql = "SELECT maPTN FROM PhieuTiepNhan WHERE maPTN LIKE 'TN%'";
            try (PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maPTN");
                    if (ma != null) {
                        try {
                            int so = Integer.parseInt(ma.replaceAll("[^0-9]", ""));
                            dsSo.add(so);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (SQLException e) {
                System.err.println("Lỗi PhieuTiepNhan_DAO.sinhMaTiepNhanTuDong: " + e.getMessage());
            }
        }

        // 2. Quét thêm từ bộ nhớ cache phòng trường hợp offline
        for (PhieuTiepNhan p : KhoDuLieu.get().getPhieuTiepNhans()) {
            if (p.getSoPhieu() != null && p.getSoPhieu().toUpperCase().startsWith("TN")) {
                try {
                    int so = Integer.parseInt(p.getSoPhieu().replaceAll("[^0-9]", ""));
                    dsSo.add(so);
                } catch (Exception ignored) {}
            }
        }

        // 3. Tìm số nguyên dương nhỏ nhất chưa được sử dụng
        int soMoi = 1;
        while (dsSo.contains(soMoi)) {
            soMoi++;
        }

        return String.format("TN%03d", soMoi);
    }
}
