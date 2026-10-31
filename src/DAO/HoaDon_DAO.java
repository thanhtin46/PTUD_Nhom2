package DAO;

import ConnectDB.ConnectDB;
import Entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Lớp DAO xử lý dữ liệu hóa đơn thanh toán và bán lẻ.
 */
public class HoaDon_DAO {

    /**
     * Lấy toàn bộ danh sách hóa đơn từ SQL Server
     */
    public ArrayList<HoaDon> layTatCaHoaDon() {
        ArrayList<HoaDon> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return fallbackCache();

        String sql = "SELECT hd.maHD, hd.maKH, kh.hoTen, kh.soDT, " +
                     "CASE WHEN hd.tienDichVu = 0 THEN N'Bán lẻ (Shop)' ELSE ISNULL(pt.bienSoXe, N'—') END AS bienSoXe, " +
                     "hd.ngayLap, " +
                     "hd.hinhThucThanhToan, nv.hoTen AS tenNV, " +
                     "hd.tienDichVu, hd.tienPhuTung, hd.giamGia, hd.tongTien, " +
                     "ISNULL(hd.ghiChu, '') AS ghiChu, " +
                     "ISNULL(hd.trangThai, N'Đã thanh toán') AS trangThai " +
                     "FROM HoaDon hd " +
                     "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                     "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                     "LEFT JOIN (SELECT maKH, MIN(bienSoXe) AS bienSoXe FROM PhuongTien GROUP BY maKH) pt ON pt.maKH = hd.maKH " +
                     "ORDER BY hd.maHD DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            HashSet<String> daXem = new HashSet<>();
            while (rs.next()) {
                String maHD = rs.getString("maHD");
                if (daXem.contains(maHD)) continue;
                daXem.add(maHD);

                String maKH = rs.getString("maKH");
                String tenKH = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String bienSo = rs.getString("bienSoXe");
                Date dLap = rs.getDate("ngayLap");
                LocalDate ngay = dLap != null ? dLap.toLocalDate() : null;
                String httt = rs.getString("hinhThucThanhToan");
                String tenNV = rs.getString("tenNV");
                long tienDV = (long) rs.getDouble("tienDichVu");
                long tienPT = (long) rs.getDouble("tienPhuTung");
                double giamGia = rs.getDouble("giamGia");
                long tong = (long) rs.getDouble("tongTien");
                String ghiChu = rs.getString("ghiChu");
                String tt = rs.getString("trangThai");
                String soPhieuSC = tienDV == 0 ? "Bán lẻ" : (maKH != null ? maKH : "TN001");

                String ptTT = "TIEN_MAT".equalsIgnoreCase(httt) ? "Tiền mặt" :
                              ("CHUYEN_KHOAN".equalsIgnoreCase(httt) ? "Chuyển khoản QR" : "Quẹt thẻ POS");

                KhachHang kh = new KhachHang(maKH, tenKH, sdt, "", "");
                PhuongTien pt = (bienSo != null && !bienSo.isEmpty() && !bienSo.equals("—"))
                        ? new PhuongTien(bienSo, "", "", "VIN_" + bienSo.replaceAll("[^A-Za-z0-9]", ""), "", "", "2022", "0", kh, ngay)
                        : null;
                PhieuSuaChua psc = new PhieuSuaChua(soPhieuSC != null ? soPhieuSC : "TN001");
                psc.setKhachHang(kh);
                psc.setPhuongTien(pt);
                NhanVien nv = new NhanVien();
                nv.setHoTen(tenNV != null ? tenNV : "Thu ngân");

                HoaDon hd = new HoaDon(maHD, psc, kh, nv, ngay, tienDV, tienPT, giamGia, tong, ptTT, ghiChu, tt);
                list.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.layTatCaHoaDon: " + e.getMessage());
            return fallbackCache();
        }
        return list;
    }

    /**
     * Thêm mới hóa đơn kèm chi tiết phụ tùng & dịch vụ vào SQL Server
     */
    public boolean themHoaDon(HoaDon hd, String maNV, List<ChiTietDichVu> dsChiTiet) {
        return new ThanhToan_DAO().luu(hd, maNV, dsChiTiet);
    }

    /**
     * Cập nhật thông tin hóa đơn (phương thức thanh toán, ghi chú, trạng thái)
     */
    public boolean capNhatHoaDon(HoaDon hd) {
        if (hd == null || hd.getMaHD() == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String httt = "TIEN_MAT";
        if (hd.getPhuongThuc() != null) {
            String pt = hd.getPhuongThuc().toLowerCase();
            if (pt.contains("chuyển") || pt.contains("qr")) httt = "CHUYEN_KHOAN";
            else if (pt.contains("pos") || pt.contains("thẻ")) httt = "THE_POS";
        }

        String sql = "UPDATE HoaDon SET hinhThucThanhToan = ?, ghiChu = ?, trangThai = ? WHERE maHD = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, httt);
            stmt.setString(2, hd.getGhiChu() != null ? hd.getGhiChu() : "");
            stmt.setString(3, hd.getTrangThai() != null ? hd.getTrangThai() : "Đã thanh toán");
            stmt.setString(4, hd.getMaHD());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.capNhatHoaDon: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hủy hoặc xóa an toàn hóa đơn và chi tiết hóa đơn
     */
    public boolean xoaHoaDon(String maHD) {
        return new ThanhToan_DAO().huy(maHD);
    }

    /**
     * Lấy danh sách chi tiết dịch vụ & phụ tùng của hóa đơn
     */
    public ArrayList<ChiTietDichVu> layChiTietTheoHoaDon(String maHD) {
        return new DuLieu_DAO().layChiTietTheoHoaDon(maHD);
    }

    // Sinh mã hóa đơn tự động tăng dần (VD: HD001, HD002)
    public String sinhMaHoaDonTuDong() {
        Connection con = ConnectDB.getInstance().getConnection();
        TreeSet<Integer> dsSo = new TreeSet<>();

        // 1. Quét danh sách mã hóa đơn từ SQL Server
        if (con != null) {
            String sql = "SELECT maHD FROM HoaDon WHERE maHD LIKE 'HD%'";
            try (PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maHD");
                    if (ma != null) {
                        try {
                            int so = Integer.parseInt(ma.replaceAll("[^0-9]", ""));
                            dsSo.add(so);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (SQLException e) {
                System.err.println("Lỗi HoaDon_DAO.sinhMaHoaDonTuDong: " + e.getMessage());
            }
        }

        // 2. Quét thêm từ bộ nhớ cache phòng trường hợp offline
        for (HoaDon h : KhoDuLieu.get().getHoaDons()) {
            if (h.getMaHD() != null && h.getMaHD().toUpperCase().startsWith("HD")) {
                try {
                    int so = Integer.parseInt(h.getMaHD().replaceAll("[^0-9]", ""));
                    dsSo.add(so);
                } catch (Exception ignored) {}
            }
        }

        // 3. Tìm số nguyên dương nhỏ nhất chưa được sử dụng
        int soMoi = 1;
        while (dsSo.contains(soMoi)) {
            soMoi++;
        }

        return String.format("HD%03d", soMoi);
    }

    /**
     * Tra cứu hóa đơn theo từ khóa, tiêu chí và bộ lọc nhanh
     * @param tuKhoa Từ khóa tìm kiếm
     * @param tieuChi 0: Tất cả, 1: Mã HĐ, 2: Số phiếu SC, 3: Biển số xe, 4: Khách hàng, 5: Thu ngân
     * @param locTrangThai "TAT_CA", "Đã thanh toán", "Đã hủy"
     * @param locPhuongThuc "TAT_CA", "Chuyển khoản QR", "Tiền mặt", "Quẹt thẻ POS"
     */
        private boolean isTatCa(String s) {
        if (s == null || s.trim().isEmpty()) return true;
        String lower = s.trim().toLowerCase();
        return lower.equals("tat_ca") || lower.contains("tất cả") || lower.contains("tat ca") || lower.contains("t?t c?");
    }

    public ArrayList<HoaDon> traCuuHoaDon(String tuKhoa, int tieuChi, String locTrangThai, String locPhuongThuc) {
        return traCuuHoaDon(tuKhoa, tieuChi, locTrangThai, locPhuongThuc, "TAT_CA");
    }

    public ArrayList<HoaDon> traCuuHoaDon(String tuKhoa, int tieuChi, String locTrangThai, String locPhuongThuc, String locLoaiHD) {
        ArrayList<HoaDon> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return fallbackTraCuuCache(tuKhoa, tieuChi, locTrangThai, locPhuongThuc, locLoaiHD);

        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT hd.maHD, hd.maKH, kh.hoTen, kh.soDT, ")
           .append("CASE WHEN hd.tienDichVu = 0 THEN N'Bán lẻ (Shop)' ELSE ISNULL(pt.bienSoXe, N'—') END AS bienSoXe, ")
           .append("hd.ngayLap, ")
           .append("hd.hinhThucThanhToan, nv.hoTen AS tenNV, ")
           .append("hd.tienDichVu, hd.tienPhuTung, hd.giamGia, hd.tongTien, ")
           .append("ISNULL(hd.ghiChu, '') AS ghiChu, ")
           .append("ISNULL(hd.trangThai, N'Đã thanh toán') AS trangThai ")
           .append("FROM HoaDon hd " +
                   "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                   "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                   "LEFT JOIN (SELECT maKH, MIN(bienSoXe) AS bienSoXe FROM PhuongTien GROUP BY maKH) pt ON pt.maKH = hd.maKH " +
                   "WHERE 1=1 ");

        // Lọc trạng thái (hỗ trợ cả DA_HUY/DA_THANH_TOAN và tiếng Việt có dấu)
        // Lọc trạng thái (hỗ trợ cả DA_HUY/DA_THANH_TOAN/CHO_THANH_TOAN và tiếng Việt có dấu)
        if (!isTatCa(locTrangThai)) {
            String lowerTrangThai = locTrangThai.toLowerCase();
            if (lowerTrangThai.contains("hủy") || lowerTrangThai.contains("huy")) {
                sql.append("AND (hd.trangThai IN ('DA_HUY', N'Đã hủy') OR LOWER(hd.trangThai) LIKE '%huy%') ");
            } else if (lowerTrangThai.contains("chờ") || lowerTrangThai.contains("chua") || lowerTrangThai.contains("cho") || lowerTrangThai.contains("chưa")) {
                sql.append("AND (hd.trangThai IN ('CHO_THANH_TOAN', 'CHUA_THANH_TOAN', N'Chưa thanh toán', N'Chờ thanh toán') OR LOWER(hd.trangThai) LIKE '%chưa%' OR LOWER(hd.trangThai) LIKE '%chờ%') ");
            } else {
                sql.append("AND (hd.trangThai IN ('DA_THANH_TOAN', N'Đã thanh toán') OR LOWER(hd.trangThai) LIKE '%thanh toan%') ");
            }
        }

        // Lọc phương thức
        if (!isTatCa(locPhuongThuc)) {
            if (locPhuongThuc.contains("QR") || locPhuongThuc.contains("khoản")) {
                sql.append("AND (hd.hinhThucThanhToan = 'CHUYEN_KHOAN' OR LOWER(hd.hinhThucThanhToan) LIKE '%qr%') ");
            } else if (locPhuongThuc.contains("Tiền mặt")) {
                sql.append("AND hd.hinhThucThanhToan = 'TIEN_MAT' ");
            } else if (locPhuongThuc.contains("POS") || locPhuongThuc.contains("thẻ")) {
                sql.append("AND (hd.hinhThucThanhToan = 'THE_POS' OR LOWER(hd.hinhThucThanhToan) LIKE '%pos%') ");
            }
        }

        // Lọc loại hóa đơn (Sửa chữa vs Bán lẻ Shop)
        if (!isTatCa(locLoaiHD)) {
            if (locLoaiHD.toLowerCase().contains("bán lẻ") || locLoaiHD.toLowerCase().contains("ban le") || locLoaiHD.toLowerCase().contains("shop")) {
                sql.append("AND hd.tienDichVu = 0 ");
            } else if (locLoaiHD.toLowerCase().contains("sửa chữa") || locLoaiHD.toLowerCase().contains("sua chua")) {
                sql.append("AND hd.tienDichVu > 0 ");
            }
        }

        // Lọc từ khóa theo tiêu chí
        if (!q.isEmpty()) {
            switch (tieuChi) {
                case 1 -> sql.append("AND LOWER(hd.maHD) LIKE ? ");
                case 2 -> sql.append("AND LOWER(hd.maKH) LIKE ? ");
                case 3 -> sql.append("AND LOWER(CASE WHEN hd.tienDichVu = 0 THEN N'Bán lẻ (Shop)' ELSE ISNULL(pt.bienSoXe, N'—') END) LIKE ? ");
                case 4 -> sql.append("AND (LOWER(kh.hoTen) LIKE ? OR LOWER(kh.soDT) LIKE ? OR LOWER(hd.maKH) LIKE ?) ");
                case 5 -> sql.append("AND LOWER(nv.hoTen) LIKE ? ");
                default -> {
                    sql.append("AND (LOWER(hd.maHD) LIKE ? ")
                       .append("OR LOWER(hd.maKH) LIKE ? ")
                       .append("OR LOWER(CASE WHEN hd.tienDichVu = 0 THEN N'Bán lẻ (Shop)' ELSE ISNULL(pt.bienSoXe, N'—') END) LIKE ? ")
                       .append("OR LOWER(kh.hoTen) LIKE ? ")
                       .append("OR LOWER(kh.soDT) LIKE ? ")
                       .append("OR LOWER(hd.maKH) LIKE ? ")
                       .append("OR LOWER(nv.hoTen) LIKE ?) ");
                }
            }
        }

        sql.append("ORDER BY hd.maHD DESC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int pIdx = 1;

            if (!q.isEmpty()) {
                String pat = "%" + q + "%";
                switch (tieuChi) {
                    case 1, 2, 3, 5 -> stmt.setString(pIdx++, pat);
                    case 4 -> {
                        stmt.setString(pIdx++, pat);
                        stmt.setString(pIdx++, pat);
                        stmt.setString(pIdx++, pat);
                    }
                    default -> {
                        for (int i = 0; i < 7; i++) {
                            stmt.setString(pIdx++, pat);
                        }
                    }
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                HashSet<String> daXem = new HashSet<>();
                while (rs.next()) {
                    String maHD = rs.getString("maHD");
                    if (daXem.contains(maHD)) continue;
                    daXem.add(maHD);

                    String maKH = rs.getString("maKH");
                    String tenKH = rs.getString("hoTen");
                    String sdt = rs.getString("soDT");
                    String bienSo = rs.getString("bienSoXe");
                    Date dLap = rs.getDate("ngayLap");
                LocalDate ngay = dLap != null ? dLap.toLocalDate() : null;
                    String httt = rs.getString("hinhThucThanhToan");
                    String tenNV = rs.getString("tenNV");
                    long tienDV = (long) rs.getDouble("tienDichVu");
                    long tienPT = (long) rs.getDouble("tienPhuTung");
                    double giamGia = rs.getDouble("giamGia");
                    long tong = (long) rs.getDouble("tongTien");
                    String ghiChu = rs.getString("ghiChu");
                    String tt = rs.getString("trangThai");
                    String soPhieuSC = tienDV == 0 ? "Bán lẻ" : (maKH != null ? maKH : "TN001");

                    String ptTT = "TIEN_MAT".equalsIgnoreCase(httt) ? "Tiền mặt" :
                                  ("CHUYEN_KHOAN".equalsIgnoreCase(httt) ? "Chuyển khoản QR" : "Quẹt thẻ POS");

                    list.add(new HoaDon(maHD, soPhieuSC != null ? soPhieuSC : "TN001", maKH, tenKH, sdt, bienSo, ngay,
                            tienDV, tienPT, giamGia, tong, ptTT, tenNV != null ? tenNV : "Thu ngân", ghiChu, tt));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.traCuuHoaDon: " + e.getMessage());
            return fallbackTraCuuCache(tuKhoa, tieuChi, locTrangThai, locPhuongThuc);
        }

        return list;
    }

    private ArrayList<HoaDon> fallbackCache() {
        return new ArrayList<>(KhoDuLieu.get().getHoaDons());
    }

    private ArrayList<HoaDon> fallbackTraCuuCache(String tuKhoa, int tieuChi, String locTrangThai, String locPhuongThuc) {
        return fallbackTraCuuCache(tuKhoa, tieuChi, locTrangThai, locPhuongThuc, "TAT_CA");
    }

    private ArrayList<HoaDon> fallbackTraCuuCache(String tuKhoa, int tieuChi, String locTrangThai, String locPhuongThuc, String locLoaiHD) {
        ArrayList<HoaDon> list = new ArrayList<>();
        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        for (HoaDon hd : KhoDuLieu.get().getHoaDons()) {
            if (!isTatCa(locTrangThai)) {
                String ltt = locTrangThai.toLowerCase();
                boolean isHuy = ltt.contains("hủy") || ltt.contains("huy");
                boolean isCho = ltt.contains("chờ") || ltt.contains("cho") || ltt.contains("chua");
                String hdTT = hd.getTrangThai() != null ? hd.getTrangThai().toLowerCase() : "";
                if (isHuy && !hdTT.contains("hủy") && !hdTT.equals("da_huy")) continue;
                if (isCho && !hdTT.contains("chờ") && !hdTT.contains("chưa") && !hdTT.contains("cho_")) continue;
                if (!isHuy && !isCho && (hdTT.contains("hủy") || hdTT.contains("chờ") || hdTT.contains("chưa"))) continue;
            }

            if (!isTatCa(locPhuongThuc)) {
                if (!hd.getPhuongThuc().toLowerCase().contains(locPhuongThuc.toLowerCase())) continue;
            }

            if (!isTatCa(locLoaiHD)) {
                boolean isBanLe = hd.getTienDichVu() == 0 || "Bán lẻ (Shop)".equalsIgnoreCase(hd.getBienSo());
                if (locLoaiHD.toLowerCase().contains("bán lẻ") || locLoaiHD.toLowerCase().contains("shop")) {
                    if (!isBanLe) continue;
                } else if (locLoaiHD.toLowerCase().contains("sửa chữa")) {
                    if (isBanLe) continue;
                }
            }

            boolean match = q.isEmpty();
            if (!match) {
                switch (tieuChi) {
                    case 1 -> match = hd.getMaHD() != null && hd.getMaHD().toLowerCase().contains(q);
                    case 2 -> match = hd.getSoPhieuSC() != null && hd.getSoPhieuSC().toLowerCase().contains(q);
                    case 3 -> match = hd.getBienSo() != null && hd.getBienSo().toLowerCase().contains(q);
                    case 4 -> match = (hd.getTenKH() != null && hd.getTenKH().toLowerCase().contains(q))
                            || (hd.getSdtKH() != null && hd.getSdtKH().contains(q))
                            || (hd.getMaKH() != null && hd.getMaKH().toLowerCase().contains(q));
                    case 5 -> match = hd.getNguoiThu() != null && hd.getNguoiThu().toLowerCase().contains(q);
                    default -> {
                        String gop = (hd.getMaHD() + " " + hd.getSoPhieuSC() + " " + hd.getTenKH() + " "
                                + hd.getSdtKH() + " " + hd.getBienSo() + " " + hd.getNguoiThu()).toLowerCase();
                        match = gop.contains(q);
                    }
                }
            }

            if (match) {
                list.add(hd);
            }
        }
        return list;
    }

    public int layTongSoHoaDon() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return KhoDuLieu.get().getHoaDons().size();

        String sql = "SELECT COUNT(*) FROM HoaDon";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.layTongSoHoaDon: " + e.getMessage());
        }
        return KhoDuLieu.get().getHoaDons().size();
    }

    public long layTongDoanhThu() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            long tong = 0;
            for (HoaDon h : KhoDuLieu.get().getHoaDons()) {
                if (HoaDon.DA_THANH_TOAN.equalsIgnoreCase(h.getTrangThai())) {
                    tong += h.getTongTien();
                }
            }
            return tong;
        }

        String sql = "SELECT ISNULL(SUM(tongTien), 0) FROM HoaDon WHERE trangThai IN ('DA_THANH_TOAN', N'Đã thanh toán')";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return (long) rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.layTongDoanhThu: " + e.getMessage());
        }
        return 0;
    }
}
