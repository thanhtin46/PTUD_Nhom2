package DAO;

import ConnectDB.ConnectDB;
import Entity.KhachHang;
import Entity.PhuongTien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Lớp DAO phục vụ tra cứu thông tin khách hàng và danh sách xe sở hữu.
 */
public class TraCuuKhachHang_DAO {

    /**
     * DTO đại diện cho một bản ghi lịch sử sửa chữa của khách hàng
     */
    public static class LichSuSuaChuaDTO {
        private String soPhieu;
        private String ngayTiepNhan;
        private String bienSo;
        private String xe;
        private String yeuCau;
        private String ktvXuLy;
        private String tienDo;
        private long chiPhi;

        public LichSuSuaChuaDTO(String soPhieu, String ngayTiepNhan, String bienSo, String xe,
                               String yeuCau, String ktvXuLy, String tienDo, long chiPhi) {
            this.soPhieu = soPhieu;
            this.ngayTiepNhan = ngayTiepNhan;
            this.bienSo = bienSo;
            this.xe = xe;
            this.yeuCau = yeuCau;
            this.ktvXuLy = ktvXuLy;
            this.tienDo = tienDo;
            this.chiPhi = chiPhi;
        }

        public String getSoPhieu() { return soPhieu; }
        public String getNgayTiepNhan() { return ngayTiepNhan; }
        public String getBienSo() { return bienSo; }
        public String getXe() { return xe; }
        public String getYeuCau() { return yeuCau; }
        public String getKtvXuLy() { return ktvXuLy; }
        public String getTienDo() { return tienDo; }
        public long getChiPhi() { return chiPhi; }
    }

    /**
     * Tra cứu danh sách khách hàng trực tiếp từ SQL Server theo từ khóa, tiêu chí và bộ lọc nhanh
     * @param tuKhoa Từ khóa tìm kiếm (có thể rỗng)
     * @param tieuChi 0: Tất cả, 1: Họ tên, 2: SĐT, 3: Biển số xe, 4: CCCD, 5: Mã KH
     * @param boLocNhanh "TAT_CA", "VIP", "DANG_SUA", "MOI"
     */
    public ArrayList<KhachHang> traCuuKhachHang(String tuKhoa, int tieuChi, String boLocNhanh) {
        ArrayList<KhachHang> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT kh.maKH, kh.hoTen, kh.soDT, kh.chungMinh, ")
           .append("ISNULL(kh.diaChi, '') AS diaChi, ")
           .append("ISNULL(kh.email, '') AS email, ")
           .append("ISNULL(kh.ghiChu, '') AS ghiChu, ")
           .append("ISNULL(kh.ngayDangKy, '') AS ngayDangKy ")
           .append("FROM KhachHang kh ")
           .append("LEFT JOIN PhuongTien pt ON kh.maKH = pt.maKH ")
           .append("WHERE 1=1 ");

        // 1. Áp dụng điều kiện từ khóa theo tiêu chí
        if (!q.isEmpty()) {
            switch (tieuChi) {
                case 1 -> sql.append("AND LOWER(kh.hoTen) LIKE ? ");
                case 2 -> sql.append("AND (LOWER(kh.soDT) LIKE ? OR REPLACE(REPLACE(kh.soDT, ' ', ''), '.', '') LIKE ?) ");
                case 3 -> sql.append("AND LOWER(pt.bienSoXe) LIKE ? ");
                case 4 -> sql.append("AND LOWER(kh.chungMinh) LIKE ? ");
                case 5 -> sql.append("AND LOWER(kh.maKH) LIKE ? ");
                default -> {
                    // Tất cả tiêu chí
                    sql.append("AND (LOWER(kh.maKH) LIKE ? ")
                       .append("OR LOWER(kh.hoTen) LIKE ? ")
                       .append("OR LOWER(kh.soDT) LIKE ? ")
                       .append("OR LOWER(kh.chungMinh) LIKE ? ")
                       .append("OR LOWER(pt.bienSoXe) LIKE ? ")
                       .append("OR LOWER(ISNULL(kh.diaChi, '')) LIKE ?) ");
                }
            }
        }

        // 2. Áp dụng bộ lọc nhanh
        if ("VIP".equalsIgnoreCase(boLocNhanh)) {
            sql.append("AND LOWER(kh.ghiChu) LIKE '%vip%' ");
        } else if ("DANG_SUA".equalsIgnoreCase(boLocNhanh)) {
            sql.append("AND kh.maKH IN (")
               .append("  SELECT tn.maKH FROM PhieuTiepNhan tn ")
               .append("  WHERE LOWER(ISNULL(tn.trangThaiXe, '')) NOT LIKE N'%hoàn thành%' ")
               .append("    AND LOWER(ISNULL(tn.trangThaiXe, '')) NOT LIKE N'%đã giao%' ")
               .append("    AND LOWER(ISNULL(tn.trangThaiXe, '')) NOT LIKE N'%hủy%' ")
               .append(") ");
        } else if ("MOI".equalsIgnoreCase(boLocNhanh)) {
            sql.append("AND (LOWER(kh.ghiChu) LIKE N'%mới%' OR kh.ngayDangKy LIKE '%2026%' OR kh.ngayDangKy LIKE '%2025%') ");
        }

        sql.append("ORDER BY kh.maKH ASC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (!q.isEmpty()) {
                String pattern = "%" + q + "%";
                switch (tieuChi) {
                    case 1, 3, 4, 5 -> stmt.setString(paramIdx++, pattern);
                    case 2 -> {
                        stmt.setString(paramIdx++, pattern);
                        stmt.setString(paramIdx++, "%" + q.replaceAll("[^0-9]", "") + "%");
                    }
                    default -> {
                        stmt.setString(paramIdx++, pattern); // maKH
                        stmt.setString(paramIdx++, pattern); // hoTen
                        stmt.setString(paramIdx++, pattern); // soDT
                        stmt.setString(paramIdx++, pattern); // chungMinh
                        stmt.setString(paramIdx++, pattern); // bienSoXe
                        stmt.setString(paramIdx++, pattern); // diaChi
                    }
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maKH");
                    String ten = rs.getString("hoTen");
                    String sdt = rs.getString("soDT");
                    String cccd = rs.getString("chungMinh");
                    String diaChi = rs.getString("diaChi");
                    String email = rs.getString("email");
                    String ghiChu = rs.getString("ghiChu");
                    String ngayDangKy = rs.getString("ngayDangKy");

                    list.add(new KhachHang(ma, ten, sdt, cccd != null ? cccd : "", diaChi, email, ghiChu, ngayDangKy));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuKhachHang_DAO.traCuuKhachHang: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách toàn bộ phương tiện thuộc quyền sở hữu của khách hàng
     */
    public ArrayList<PhuongTien> layDanhSachXeCuaKhach(String maKH) {
        ArrayList<PhuongTien> list = new ArrayList<>();
        if (maKH == null || maKH.trim().isEmpty()) return list;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT soVIN, bienSoXe, hangXe, dongXe, maSX, maKH, " +
                     "ISNULL(soMay, '') AS soMay, " +
                     "ISNULL(mauSon, '') AS mauSon, " +
                     "ISNULL(odo, '0') AS odo, " +
                     "ISNULL(ngayTiepNhanGanNhat, '') AS ngayTiepNhanGanNhat " +
                     "FROM PhuongTien WHERE maKH = ? ORDER BY bienSoXe ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maKH.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String vin = rs.getString("soVIN");
                    String bienSo = rs.getString("bienSoXe");
                    String hangXe = rs.getString("hangXe");
                    String dongXe = rs.getString("dongXe");
                    int namSX = rs.getInt("maSX");
                    String maChu = rs.getString("maKH");
                    String soMay = rs.getString("soMay");
                    String mauSon = rs.getString("mauSon");
                    String odo = rs.getString("odo");
                    String ngayTN = rs.getString("ngayTiepNhanGanNhat");

                    list.add(new PhuongTien(bienSo, hangXe, dongXe, vin, soMay, mauSon, String.valueOf(namSX), odo, maChu, ngayTN));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuKhachHang_DAO.layDanhSachXeCuaKhach: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy chuỗi danh sách biển số xe khách hàng sở hữu (VD: "51A-12345, 60B-67890")
     */
    public String layDanhSachBienSoXeCuaKhach(String maKH) {
        ArrayList<PhuongTien> listXe = layDanhSachXeCuaKhach(maKH);
        if (listXe.isEmpty()) return "(Chưa có xe)";

        StringBuilder sb = new StringBuilder();
        for (PhuongTien xe : listXe) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(xe.getBienSo());
        }
        return sb.toString();
    }

    /**
     * Lấy lịch sử sửa chữa, bảo dưỡng của khách hàng từ SQL Server
     */
    public ArrayList<LichSuSuaChuaDTO> layLichSuSuaChuaCuaKhach(String maKH) {
        ArrayList<LichSuSuaChuaDTO> list = new ArrayList<>();
        if (maKH == null || maKH.trim().isEmpty()) return list;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT tn.maPTN, " +
                     "CONVERT(VARCHAR(10), tn.ngayTN, 103) AS ngayTNStr, " +
                     "pt.bienSoXe, " +
                     "ISNULL(pt.hangXe, '') + ' ' + ISNULL(pt.dongXe, '') AS xe, " +
                     "ISNULL(tn.yeuCauSC, '') AS yeuCauSC, " +
                     "ISNULL(ktv.hoTen, N'Chưa giao') AS tenKTV, " +
                     "ISNULL(tn.trangThaiXe, N'Chờ tiếp nhận') AS trangThaiXe, " +
                     "ISNULL(hd.tongTien, 0) AS chiPhi " +
                     "FROM PhieuTiepNhan tn " +
                     "JOIN PhuongTien pt ON tn.soVIN = pt.soVIN " +
                     "LEFT JOIN PhieuSuaChua sc ON (sc.maPTN = tn.maPTN OR (sc.maPTN IS NULL AND ('SC' + SUBSTRING(tn.maPTN, 3, 10)) = sc.maPSC)) " +
                     "LEFT JOIN NhanVien ktv ON sc.maNV = ktv.maNV " +
                     "OUTER APPLY (SELECT TOP 1 tongTien FROM HoaDon hd WHERE hd.maKH = tn.maKH AND hd.ngayLap = CAST(tn.ngayTN AS DATE)) hd " +
                     "WHERE tn.maKH = ? " +
                     "ORDER BY tn.ngayTN DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maKH.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String soPhieu = rs.getString("maPTN");
                    String ngayTN = rs.getString("ngayTNStr");
                    String bienSo = rs.getString("bienSoXe");
                    String xe = rs.getString("xe");
                    String yeuCau = rs.getString("yeuCauSC");
                    String tenKTV = rs.getString("tenKTV");
                    String trangThaiXe = rs.getString("trangThaiXe");
                    long chiPhi = (long) rs.getDouble("chiPhi");

                    list.add(new LichSuSuaChuaDTO(soPhieu, ngayTN, bienSo, xe.trim(), yeuCau, tenKTV, trangThaiXe, chiPhi));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuKhachHang_DAO.layLichSuSuaChuaCuaKhach: " + e.getMessage());
        }
        return list;
    }

    /**
     * Kiểm tra khách hàng có xe đang trong tiến trình sửa chữa hay không
     */
    public boolean khachHangCoXeDangSua(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "SELECT 1 FROM PhieuTiepNhan " +
                     "WHERE maKH = ? " +
                     "  AND LOWER(ISNULL(trangThaiXe, '')) NOT LIKE N'%hoàn thành%' " +
                     "  AND LOWER(ISNULL(trangThaiXe, '')) NOT LIKE N'%đã giao%' " +
                     "  AND LOWER(ISNULL(trangThaiXe, '')) NOT LIKE N'%hủy%'";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maKH.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuKhachHang_DAO.khachHangCoXeDangSua: " + e.getMessage());
        }
        return false;
    }

    /**
     * Thống kê số lượng phục vụ hiển thị: [Tổng số khách, Khách VIP, Khách có xe đang sửa]
     */
    public int[] layThongKeKhachHang() {
        int[] result = new int[]{0, 0, 0};
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return result;

        try {
            // 1. Tổng số khách
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM KhachHang");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result[0] = rs.getInt(1);
            }

            // 2. Số khách VIP
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM KhachHang WHERE LOWER(ghiChu) LIKE '%vip%'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result[1] = rs.getInt(1);
            }

            // 3. Số khách có xe đang sửa
            String sqlDangSua = "SELECT COUNT(DISTINCT maKH) FROM PhieuTiepNhan " +
                                "WHERE LOWER(ISNULL(trangThaiXe, '')) NOT LIKE N'%hoàn thành%' " +
                                "  AND LOWER(ISNULL(trangThaiXe, '')) NOT LIKE N'%đã giao%' " +
                                "  AND LOWER(ISNULL(trangThaiXe, '')) NOT LIKE N'%hủy%'";
            try (PreparedStatement ps = con.prepareStatement(sqlDangSua);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) result[2] = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuKhachHang_DAO.layThongKeKhachHang: " + e.getMessage());
        }
        return result;
    }
}
