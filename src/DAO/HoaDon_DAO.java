package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietDichVu;
import Entity.HoaDon;
import Entity.KhoDuLieu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * HoaDon_DAO — Lớp DAO chuyên trách phục vụ Quản lý & Tra cứu Hóa đơn:
 * - CRUD: Lấy danh sách, Thêm mới hóa đơn & chi tiết, Cập nhật, Hủy/Xóa hóa đơn
 * - Sinh mã hóa đơn tự động thông minh (tự lấp khoảng trống khi xóa/hủy, không tăng lên mãi)
 * - Tra cứu đa tiêu chí: Mã HĐ, Phiếu SC, Biển số xe, Khách hàng, Thu ngân
 * - Lọc theo trạng thái và phương thức thanh toán
 * - Thống kê doanh thu & số lượng hóa đơn
 */
public class HoaDon_DAO {

    /**
     * Lấy toàn bộ danh sách hóa đơn từ SQL Server
     */
    public ArrayList<HoaDon> layTatCaHoaDon() {
        ArrayList<HoaDon> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return fallbackCache();

        String sql = "SELECT hd.maHD, hd.soPhieuSC, hd.maKH, kh.hoTen, kh.soDT, " +
                     "ISNULL(pt.bienSoXe, N'Chưa rõ') AS bienSoXe, " +
                     "CONVERT(VARCHAR(10), hd.ngayLap, 103) AS ngayLapStr, " +
                     "hd.hinhThucThanhToan, nv.hoTen AS tenNV, " +
                     "hd.tienDichVu, hd.tienPhuTung, hd.giamGia, hd.tongTien, " +
                     "ISNULL(hd.ghiChu, '') AS ghiChu, " +
                     "ISNULL(hd.trangThai, N'Đã thanh toán') AS trangThai " +
                     "FROM HoaDon hd " +
                     "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                     "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                     "LEFT JOIN PhieuTiepNhan ptn ON (ptn.maPTN = hd.soPhieuSC OR ('SC' + SUBSTRING(ptn.maPTN, 3, 10)) = hd.soPhieuSC) " +
                     "LEFT JOIN PhuongTien pt ON (pt.soVIN = ptn.soVIN OR pt.maKH = hd.maKH) " +
                     "ORDER BY hd.maHD DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            HashSet<String> daXem = new HashSet<>();
            while (rs.next()) {
                String maHD = rs.getString("maHD");
                if (daXem.contains(maHD)) continue;
                daXem.add(maHD);

                String soPhieuSC = rs.getString("soPhieuSC");
                String maKH = rs.getString("maKH");
                String tenKH = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String bienSo = rs.getString("bienSoXe");
                String ngay = rs.getString("ngayLapStr");
                String httt = rs.getString("hinhThucThanhToan");
                String tenNV = rs.getString("tenNV");
                long tienDV = (long) rs.getDouble("tienDichVu");
                long tienPT = (long) rs.getDouble("tienPhuTung");
                double giamGia = rs.getDouble("giamGia");
                long tong = (long) rs.getDouble("tongTien");
                String ghiChu = rs.getString("ghiChu");
                String tt = rs.getString("trangThai");

                String ptTT = "TIEN_MAT".equalsIgnoreCase(httt) ? "Tiền mặt" :
                              ("CHUYEN_KHOAN".equalsIgnoreCase(httt) ? "Chuyển khoản QR" : "Quẹt thẻ POS");

                list.add(new HoaDon(maHD, soPhieuSC != null ? soPhieuSC : "TN001", maKH, tenKH, sdt, bienSo, ngay,
                        tienDV, tienPT, giamGia, tong, ptTT, tenNV != null ? tenNV : "Thu ngân", ghiChu, tt));
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
        if (hd == null || hd.getMaHD() == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // 1. Xác định mã khách hàng
            String maKH = hd.getMaKH();
            if (maKH == null || maKH.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM KhachHang WHERE soDT = ?")) {
                    ps.setString(1, hd.getSdtKH());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) maKH = rs.getString("maKH");
                    }
                }
            }
            if (maKH == null || maKH.isEmpty()) maKH = "KH001";

            // 2. Xác định mã nhân viên thu ngân
            String nvLap = (maNV != null && !maNV.isEmpty()) ? maNV : "NV05";

            // 3. Phương thức thanh toán chuẩn hóa cho DB
            String httt = "TIEN_MAT";
            if (hd.getPhuongThuc() != null) {
                String pt = hd.getPhuongThuc().toLowerCase();
                if (pt.contains("chuyển") || pt.contains("qr")) httt = "CHUYEN_KHOAN";
                else if (pt.contains("pos") || pt.contains("thẻ")) httt = "THE_POS";
            }

            // 4. Thêm hóa đơn
            String sqlHD = "INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, soPhieuSC, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) " +
                           "VALUES (?, ?, ?, CAST(GETDATE() AS DATE), ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlHD)) {
                stmt.setString(1, hd.getMaHD());
                stmt.setString(2, maKH);
                stmt.setString(3, nvLap);
                stmt.setString(4, httt);
                stmt.setString(5, hd.getSoPhieuSC() != null ? hd.getSoPhieuSC() : "TN001");
                stmt.setDouble(6, (double) hd.getTienDichVu());
                stmt.setDouble(7, (double) hd.getTienPhuTung());
                stmt.setDouble(8, hd.getGiamGiaPhanTram());
                stmt.setDouble(9, (double) hd.getTongTien());
                stmt.setString(10, hd.getGhiChu() != null ? hd.getGhiChu() : "");
                stmt.setString(11, hd.getTrangThai() != null ? hd.getTrangThai() : "Đã thanh toán");
                stmt.executeUpdate();
            }

            // 5. Thêm chi tiết hóa đơn
            String sqlCT = "INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, soLuong, donGia) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlCT)) {
                if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
                    HashSet<String> daCoPT = new HashSet<>();
                    int ptCount = 1;
                    for (ChiTietDichVu ct : dsChiTiet) {
                        String rawPT = ct.getMaPT() != null ? ct.getMaPT().trim() : "";
                        String maPT = rawPT;
                        if (!maPT.toUpperCase().startsWith("PT") || daCoPT.contains(maPT)) {
                            while (ptCount <= 20 && daCoPT.contains(String.format("PT%03d", ptCount))) {
                                ptCount++;
                            }
                            maPT = String.format("PT%03d", Math.min(20, ptCount++));
                        }
                        daCoPT.add(maPT);

                        stmt.setString(1, hd.getMaHD());
                        stmt.setString(2, maPT);
                        stmt.setString(3, ct.getTenHangMuc());
                        stmt.setInt(4, ct.getSoLuong());
                        stmt.setDouble(5, (double) ct.getDonGia());
                        stmt.executeUpdate();
                    }
                } else {
                    stmt.setString(1, hd.getMaHD());
                    stmt.setString(2, "PT001");
                    stmt.setString(3, "Chi phí phụ tùng & vật tư sửa chữa");
                    stmt.setInt(4, 1);
                    stmt.setDouble(5, (double) hd.getTienPhuTung());
                    stmt.executeUpdate();

                    if (hd.getTienDichVu() > 0) {
                        stmt.setString(1, hd.getMaHD());
                        stmt.setString(2, "PT002");
                        stmt.setString(3, "Chi phí tiền công bảo dưỡng / sửa chữa");
                        stmt.setInt(4, 1);
                        stmt.setDouble(5, (double) hd.getTienDichVu());
                        stmt.executeUpdate();
                    }
                }
            }

            // 6. Cập nhật trạng thái phiếu tiếp nhận sang "Hoàn thành"
            if (hd.getSoPhieuSC() != null && !hd.getSoPhieuSC().isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE PhieuTiepNhan SET trangThaiXe = N'Hoàn thành' WHERE maPTN = ?")) {
                    ps.setString(1, hd.getSoPhieuSC());
                    ps.executeUpdate();
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.themHoaDon: " + e.getMessage());
            return false;
        }
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
        if (maHD == null || maHD.trim().isEmpty()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // 1. Hoàn lại tồn kho cho các phụ tùng trong hóa đơn
            try (PreparedStatement psRead = con.prepareStatement("SELECT maPT, soLuong FROM ChiTietHoaDon WHERE maHD = ?")) {
                psRead.setString(1, maHD);
                try (ResultSet rs = psRead.executeQuery()) {
                    while (rs.next()) {
                        String maPT = rs.getString("maPT");
                        int sl = rs.getInt("soLuong");
                        if (maPT != null && maPT.toUpperCase().startsWith("PT")) {
                            try (PreparedStatement psAdd = con.prepareStatement("UPDATE PhuTung SET soLuongTon = soLuongTon + ? WHERE maPT = ?")) {
                                psAdd.setInt(1, sl);
                                psAdd.setString(2, maPT);
                                psAdd.executeUpdate();
                            }
                        }
                    }
                }
            }

            // 2. Xóa chi tiết hóa đơn
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHD = ?")) {
                ps.setString(1, maHD);
                ps.executeUpdate();
            }

            // 3. Xóa hóa đơn
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM HoaDon WHERE maHD = ?")) {
                ps.setString(1, maHD);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.xoaHoaDon: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách chi tiết dịch vụ & phụ tùng của hóa đơn
     */
    public ArrayList<ChiTietDichVu> layChiTietTheoHoaDon(String maHD) {
        ArrayList<ChiTietDichVu> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || maHD == null) return list;

        String sql = "SELECT ct.maPT, ct.tenPhuTung, ct.soLuong, ct.donGia, " +
                     "ISNULL(pt.donViTinh, N'Cái') AS donViTinh " +
                     "FROM ChiTietHoaDon ct " +
                     "LEFT JOIN PhuTung pt ON ct.maPT = pt.maPT " +
                     "WHERE ct.maHD = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPT = rs.getString("maPT");
                    String ten = rs.getString("tenPhuTung");
                    int sl = rs.getInt("soLuong");
                    long donGia = (long) rs.getDouble("donGia");
                    String dvt = rs.getString("donViTinh");
                    String loai = (ten != null && ten.toLowerCase().contains("công")) ? "Tiền công" : "Phụ tùng";

                    list.add(new ChiTietDichVu(loai, maPT, ten, dvt, sl, donGia));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.layChiTietTheoHoaDon: " + e.getMessage());
        }
        return list;
    }

    /**
     * Sinh mã số hóa đơn tự động thông minh:
     * - Tự động lấp khoảng trống khi xóa/hủy hóa đơn (trở lại số hóa đơn vừa xóa)
     * - Không tăng lên vô tận khi bấm làm mới form
     * - Bắt đầu từ HD001 nếu chưa có hóa đơn nào
     */
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
    public ArrayList<HoaDon> traCuuHoaDon(String tuKhoa, int tieuChi, String locTrangThai, String locPhuongThuc) {
        ArrayList<HoaDon> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return fallbackTraCuuCache(tuKhoa, tieuChi, locTrangThai, locPhuongThuc);

        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT hd.maHD, hd.soPhieuSC, hd.maKH, kh.hoTen, kh.soDT, ")
           .append("ISNULL(pt.bienSoXe, N'Chưa rõ') AS bienSoXe, ")
           .append("CONVERT(VARCHAR(10), hd.ngayLap, 103) AS ngayLapStr, ")
           .append("hd.hinhThucThanhToan, nv.hoTen AS tenNV, ")
           .append("hd.tienDichVu, hd.tienPhuTung, hd.giamGia, hd.tongTien, ")
           .append("ISNULL(hd.ghiChu, '') AS ghiChu, ")
           .append("ISNULL(hd.trangThai, N'Đã thanh toán') AS trangThai ")
           .append("FROM HoaDon hd " +
                   "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                   "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                   "LEFT JOIN PhieuTiepNhan ptn ON (ptn.maPTN = hd.soPhieuSC OR ('SC' + SUBSTRING(ptn.maPTN, 3, 10)) = hd.soPhieuSC) " +
                   "LEFT JOIN PhuongTien pt ON (pt.soVIN = ptn.soVIN OR pt.maKH = hd.maKH) " +
                   "WHERE 1=1 ");

        // Lọc trạng thái
        if (locTrangThai != null && !locTrangThai.isEmpty() && !"TAT_CA".equalsIgnoreCase(locTrangThai) && !"Tất cả trạng thái".equalsIgnoreCase(locTrangThai)) {
            sql.append("AND LOWER(hd.trangThai) LIKE ? ");
        }

        // Lọc phương thức
        if (locPhuongThuc != null && !locPhuongThuc.isEmpty() && !"TAT_CA".equalsIgnoreCase(locPhuongThuc) && !"Tất cả phương thức".equalsIgnoreCase(locPhuongThuc)) {
            if (locPhuongThuc.contains("QR") || locPhuongThuc.contains("khoản")) {
                sql.append("AND (hd.hinhThucThanhToan = 'CHUYEN_KHOAN' OR LOWER(hd.hinhThucThanhToan) LIKE '%qr%') ");
            } else if (locPhuongThuc.contains("Tiền mặt")) {
                sql.append("AND hd.hinhThucThanhToan = 'TIEN_MAT' ");
            } else if (locPhuongThuc.contains("POS") || locPhuongThuc.contains("thẻ")) {
                sql.append("AND (hd.hinhThucThanhToan = 'THE_POS' OR LOWER(hd.hinhThucThanhToan) LIKE '%pos%') ");
            }
        }

        // Lọc từ khóa theo tiêu chí
        if (!q.isEmpty()) {
            switch (tieuChi) {
                case 1 -> sql.append("AND LOWER(hd.maHD) LIKE ? ");
                case 2 -> sql.append("AND LOWER(hd.soPhieuSC) LIKE ? ");
                case 3 -> sql.append("AND LOWER(pt.bienSoXe) LIKE ? ");
                case 4 -> sql.append("AND (LOWER(kh.hoTen) LIKE ? OR LOWER(kh.soDT) LIKE ? OR LOWER(hd.maKH) LIKE ?) ");
                case 5 -> sql.append("AND LOWER(nv.hoTen) LIKE ? ");
                default -> {
                    sql.append("AND (LOWER(hd.maHD) LIKE ? ")
                       .append("OR LOWER(hd.soPhieuSC) LIKE ? ")
                       .append("OR LOWER(pt.bienSoXe) LIKE ? ")
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
            if (locTrangThai != null && !locTrangThai.isEmpty() && !"TAT_CA".equalsIgnoreCase(locTrangThai) && !"Tất cả trạng thái".equalsIgnoreCase(locTrangThai)) {
                stmt.setString(pIdx++, "%" + locTrangThai.trim().toLowerCase() + "%");
            }

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

                    String soPhieuSC = rs.getString("soPhieuSC");
                    String maKH = rs.getString("maKH");
                    String tenKH = rs.getString("hoTen");
                    String sdt = rs.getString("soDT");
                    String bienSo = rs.getString("bienSoXe");
                    String ngay = rs.getString("ngayLapStr");
                    String httt = rs.getString("hinhThucThanhToan");
                    String tenNV = rs.getString("tenNV");
                    long tienDV = (long) rs.getDouble("tienDichVu");
                    long tienPT = (long) rs.getDouble("tienPhuTung");
                    double giamGia = rs.getDouble("giamGia");
                    long tong = (long) rs.getDouble("tongTien");
                    String ghiChu = rs.getString("ghiChu");
                    String tt = rs.getString("trangThai");

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
        ArrayList<HoaDon> list = new ArrayList<>();
        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        for (HoaDon hd : KhoDuLieu.get().getHoaDons()) {
            if (locTrangThai != null && !locTrangThai.isEmpty() && !"TAT_CA".equalsIgnoreCase(locTrangThai) && !"Tất cả trạng thái".equalsIgnoreCase(locTrangThai)) {
                if (!locTrangThai.equalsIgnoreCase(hd.getTrangThai())) continue;
            }

            if (locPhuongThuc != null && !locPhuongThuc.isEmpty() && !"TAT_CA".equalsIgnoreCase(locPhuongThuc) && !"Tất cả phương thức".equalsIgnoreCase(locPhuongThuc)) {
                if (!hd.getPhuongThuc().toLowerCase().contains(locPhuongThuc.toLowerCase())) continue;
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

        String sql = "SELECT ISNULL(SUM(tongTien), 0) FROM HoaDon WHERE trangThai = N'Đã thanh toán'";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return (long) rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Lỗi HoaDon_DAO.layTongDoanhThu: " + e.getMessage());
        }
        return 0;
    }
}
