package DAO;

import ConnectDB.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lớp DAO truy vấn dữ liệu thống kê cho trang chủ các bộ phận (sửa chữa, kho, thu ngân).
 */
public class TrangChuBoPhan_DAO {

    private Connection getValidConnection() throws SQLException {
        ConnectDB cdb = ConnectDB.getInstance();
        Connection con = cdb.getConnection();
        if (con == null || con.isClosed()) {
            cdb.connect();
            con = cdb.getConnection();
        }
        if (con == null || con.isClosed()) {
            throw new SQLException("Không thể kết nối đến cơ sở dữ liệu SQL Server!");
        }
        return con;
    }

    // =========================================================================
    // 1. DỮ LIỆU BỘ PHẬN SỬA CHỮA / KỸ THUẬT VIÊN
    // =========================================================================

    public Map<String, Object> layThongKeSuaChua(String maKTV) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tongDangLam", 0);
        map.put("dangSua", 0);
        map.put("choVatTu", 0);
        map.put("canGiaoGap", 0);

        String sql = "SELECT "
                + "    COUNT(DISTINCT CASE WHEN s.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY') THEN s.maPSC END) AS tongDangLam, "
                + "    COUNT(DISTINCT CASE WHEN s.trangThai LIKE '%DANG_SUA%' THEN s.maPSC END) AS dangSua, "
                + "    COUNT(DISTINCT CASE WHEN x.trangThai IN (N'Chờ xuất', 'CHO_XUAT') THEN s.maPSC END) AS choVatTu, "
                + "    COUNT(DISTINCT CASE WHEN s.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY') "
                + "                         AND s.ngayHoanThanhDuKien IS NOT NULL "
                + "                         AND s.ngayHoanThanhDuKien <= CAST(GETDATE() AS DATE) THEN s.maPSC END) AS canGiaoGap "
                + "FROM PhieuSuaChua s "
                + "LEFT JOIN PhieuXuatKho x ON s.maPSC = x.maPSC "
                + "WHERE (? IS NULL OR s.maNV = ?)";

        try {
            Connection con = getValidConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maKTV);
                ps.setString(2, maKTV);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        map.put("tongDangLam", rs.getInt("tongDangLam"));
                        map.put("dangSua", rs.getInt("dangSua"));
                        map.put("choVatTu", rs.getInt("choVatTu"));
                        map.put("canGiaoGap", rs.getInt("canGiaoGap"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi layThongKeSuaChua: " + e.getMessage());
        }
        return map;
    }

    public ArrayList<Map<String, Object>> layDanhSachPhieuSuaChuaKTV(String maKTV) {
        ArrayList<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT s.maPSC, s.maPTN, v.bienSoXe, "
                + "       ISNULL(v.hangXe, '') + ' ' + ISNULL(v.dongXe, '') AS tenXe, "
                + "       ISNULL(k.hoTen, N'Khách vãng lai') AS tenKH, "
                + "       ISNULL(k.soDT, '—') AS soDT, "
                + "       ISNULL(tn.yeuCauSC, N'Sửa chữa theo yêu cầu') AS yeuCauSC, "
                + "       s.ngayBD, s.ngayHoanThanhDuKien, s.trangThai, "
                + "       ISNULL(x.trangThai, N'Chưa yêu cầu') AS trangThaiXuatKho "
                + "FROM PhieuSuaChua s "
                + "LEFT JOIN PhieuTiepNhan tn ON s.maPTN = tn.maPTN "
                + "LEFT JOIN PhuongTien v ON s.soVIN = v.soVIN "
                + "LEFT JOIN KhachHang k ON s.maKH = k.maKH "
                + "LEFT JOIN PhieuXuatKho x ON s.maPSC = x.maPSC "
                + "WHERE (? IS NULL OR s.maNV = ?) "
                + "  AND s.trangThai NOT IN ('DA_HUY', 'HUY') "
                + "ORDER BY "
                + "    CASE WHEN s.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH') "
                + "              AND s.ngayHoanThanhDuKien IS NOT NULL "
                + "              AND s.ngayHoanThanhDuKien < CAST(GETDATE() AS DATE) THEN 1 "
                + "         WHEN s.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH') "
                + "              AND s.ngayHoanThanhDuKien IS NOT NULL "
                + "              AND s.ngayHoanThanhDuKien = CAST(GETDATE() AS DATE) THEN 2 "
                + "         WHEN s.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH') THEN 3 "
                + "         ELSE 4 END ASC, "
                + "    s.ngayHoanThanhDuKien ASC, s.maPSC DESC";

        try {
            Connection con = getValidConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maKTV);
                ps.setString(2, maKTV);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> r = new LinkedHashMap<>();
                        r.put("maPSC", rs.getString("maPSC"));
                        r.put("maPTN", rs.getString("maPTN"));
                        r.put("bienSoXe", rs.getString("bienSoXe"));
                        r.put("tenXe", rs.getString("tenXe"));
                        r.put("tenKH", rs.getString("tenKH"));
                        r.put("soDT", rs.getString("soDT"));
                        r.put("yeuCauSC", rs.getString("yeuCauSC"));
                        Date dHen = rs.getDate("ngayHoanThanhDuKien");
                        r.put("ngayHen", dHen != null ? dHen.toLocalDate() : null);
                        r.put("trangThai", rs.getString("trangThai"));
                        r.put("trangThaiXuatKho", rs.getString("trangThaiXuatKho"));
                        list.add(r);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi layDanhSachPhieuSuaChuaKTV: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // 2. DỮ LIỆU BỘ PHẬN KHO PHỤ TÙNG & VẬT TƯ
    // =========================================================================

    public Map<String, Object> layThongKeKho() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tongMatHang", 0);
        map.put("choXuat", 0);
        map.put("choNhap", 0);
        map.put("canhBaoTon", 0);

        try {
            Connection con = getValidConnection();

            // Tổng mặt hàng
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM PhuTung")) {
                if (rs.next()) map.put("tongMatHang", rs.getInt(1));
            }

            // Chờ xuất kho
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM PhieuXuatKho WHERE trangThai IN (N'Chờ xuất', 'CHO_XUAT')")) {
                if (rs.next()) map.put("choXuat", rs.getInt(1));
            }

            // Chờ nhập kho
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM PhieuNhapKho WHERE trangThai = 'CHO_NHAP'")) {
                if (rs.next()) map.put("choNhap", rs.getInt(1));
            }

            // Cảnh báo tồn kho dưới mức an toàn
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Kho WHERE soLuongTon <= soLTT")) {
                if (rs.next()) map.put("canhBaoTon", rs.getInt(1));
            }

        } catch (Exception e) {
            System.err.println("Lỗi layThongKeKho: " + e.getMessage());
        }
        return map;
    }

    public ArrayList<Map<String, Object>> layDanhSachChoXuatKho() {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT x.maPhieuXuat, x.maPSC, v.bienSoXe, x.ngayXuat, x.trangThai, ISNULL(x.lyDoXuat, '') AS lyDoXuat "
                + "FROM PhieuXuatKho x "
                + "LEFT JOIN PhieuSuaChua s ON x.maPSC = s.maPSC "
                + "LEFT JOIN PhuongTien v ON s.soVIN = v.soVIN "
                + "WHERE x.trangThai IN (N'Chờ xuất', 'CHO_XUAT') "
                + "ORDER BY x.ngayXuat ASC, x.maPhieuXuat ASC";

        try {
            Connection con = getValidConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("maPhieuXuat", rs.getString("maPhieuXuat"));
                    r.put("maPSC", rs.getString("maPSC"));
                    r.put("bienSoXe", rs.getString("bienSoXe") != null ? rs.getString("bienSoXe") : "—");
                    Timestamp ts = rs.getTimestamp("ngayXuat");
                    r.put("ngayXuat", ts != null ? ts.toLocalDateTime().toLocalDate() : null);
                    r.put("trangThai", rs.getString("trangThai"));
                    r.put("lyDoXuat", rs.getString("lyDoXuat"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi layDanhSachChoXuatKho: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Map<String, Object>> layDanhSachChoNhapKho() {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT n.maPhieuNhap, ISNULL(ncc.tenNCC, n.maNCC) AS tenNCC, "
                + "       n.ngayNhap, n.ngayDuKienGiao, n.tongTien, n.trangThai "
                + "FROM PhieuNhapKho n "
                + "LEFT JOIN NhaCungCap ncc ON n.maNCC = ncc.maNCC "
                + "WHERE n.trangThai = 'CHO_NHAP' "
                + "ORDER BY n.ngayDuKienGiao ASC, n.maPhieuNhap ASC";

        try {
            Connection con = getValidConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("maPhieuNhap", rs.getString("maPhieuNhap"));
                    r.put("tenNCC", rs.getString("tenNCC"));
                    Date dGiao = rs.getDate("ngayDuKienGiao");
                    r.put("ngayGiao", dGiao != null ? dGiao.toLocalDate() : null);
                    r.put("tongTien", rs.getLong("tongTien"));
                    r.put("trangThai", rs.getString("trangThai"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi layDanhSachChoNhapKho: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Map<String, Object>> layDanhSachPhuTungCanhBao() {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT p.maPT, p.tenPT, k.soLuongTon, k.soLTT, ISNULL(k.viTriKho, N'Chưa xếp') AS viTriKho, "
                + "       ISNULL(p.donViTinh, N'Cái') AS donViTinh "
                + "FROM PhuTung p "
                + "JOIN Kho k ON p.maPT = k.maPT "
                + "WHERE k.soLuongTon <= k.soLTT "
                + "ORDER BY k.soLuongTon ASC, p.maPT ASC";

        try {
            Connection con = getValidConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("maPT", rs.getString("maPT"));
                    r.put("tenPT", rs.getString("tenPT"));
                    r.put("soLuongTon", rs.getInt("soLuongTon"));
                    r.put("soLTT", rs.getInt("soLTT"));
                    r.put("viTriKho", rs.getString("viTriKho"));
                    r.put("donViTinh", rs.getString("donViTinh"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi layDanhSachPhuTungCanhBao: " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // 3. DỮ LIỆU BỘ PHẬN THU NGÂN & KẾ TOÁN
    // =========================================================================

    public Map<String, Object> layThongKeThuNgan() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("choThanhToan", 0);
        map.put("hoaDonHomNay", 0);
        map.put("doanhThuHomNay", 0L);
        map.put("banLeHomNay", 0);

        try {
            Connection con = getValidConnection();

            // Xe sửa xong chờ thanh toán
            String sqlCho = "SELECT COUNT(*) FROM PhieuSuaChua s "
                    + "WHERE s.trangThai IN ('HOAN_THANH', 'DA_HOAN_THANH', N'Hoàn thành') "
                    + "  AND NOT EXISTS ( "
                    + "      SELECT 1 FROM HoaDon h "
                    + "      WHERE (h.ghiChu LIKE '%' + s.maPSC + '%' OR h.ghiChu LIKE '%' + s.maPTN + '%') "
                    + "        AND h.trangThai NOT IN ('DA_HUY', N'Đã hủy') "
                    + "  )";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sqlCho)) {
                if (rs.next()) map.put("choThanhToan", rs.getInt(1));
            }

            // Hóa đơn hôm nay & Doanh thu hôm nay
            String sqlHD = "SELECT COUNT(*) AS soHD, ISNULL(SUM(tongTien), 0) AS tongThu "
                    + "FROM HoaDon "
                    + "WHERE ngayLap = CAST(GETDATE() AS DATE) "
                    + "  AND trangThai NOT IN ('DA_HUY', N'Đã hủy')";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sqlHD)) {
                if (rs.next()) {
                    map.put("hoaDonHomNay", rs.getInt("soHD"));
                    map.put("doanhThuHomNay", (long) rs.getDouble("tongThu"));
                }
            }

            // Hóa đơn bán lẻ hôm nay
            String sqlBanLe = "SELECT COUNT(*) FROM HoaDon "
                    + "WHERE ngayLap = CAST(GETDATE() AS DATE) "
                    + "  AND (tienDichVu = 0 OR tienDichVu IS NULL) "
                    + "  AND trangThai NOT IN ('DA_HUY', N'Đã hủy')";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sqlBanLe)) {
                if (rs.next()) map.put("banLeHomNay", rs.getInt(1));
            }

        } catch (Exception e) {
            System.err.println("Lỗi layThongKeThuNgan: " + e.getMessage());
        }
        return map;
    }

    public ArrayList<Map<String, Object>> layDanhSachChoThanhToan() {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.maPSC, s.maPTN, v.bienSoXe, "
                + "       ISNULL(k.hoTen, N'Khách vãng lai') AS tenKH, "
                + "       ISNULL(k.soDT, '—') AS soDT, "
                + "       s.ngayHoanThanhDuKien, s.trangThai "
                + "FROM PhieuSuaChua s "
                + "JOIN PhuongTien v ON v.soVIN = s.soVIN "
                + "JOIN KhachHang k ON k.maKH = s.maKH "
                + "WHERE s.trangThai IN ('HOAN_THANH', 'DA_HOAN_THANH', N'Hoàn thành') "
                + "  AND NOT EXISTS ( "
                + "      SELECT 1 FROM HoaDon h "
                + "      WHERE (h.ghiChu LIKE '%' + s.maPSC + '%' OR h.ghiChu LIKE '%' + s.maPTN + '%') "
                + "        AND h.trangThai NOT IN ('DA_HUY', N'Đã hủy') "
                + "  ) "
                + "ORDER BY s.ngayBD DESC";

        try {
            Connection con = getValidConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("maPSC", rs.getString("maPSC"));
                    r.put("maPTN", rs.getString("maPTN"));
                    r.put("bienSoXe", rs.getString("bienSoXe"));
                    r.put("tenKH", rs.getString("tenKH"));
                    r.put("soDT", rs.getString("soDT"));
                    Date d = rs.getDate("ngayHoanThanhDuKien");
                    r.put("ngayHoanThanh", d != null ? d.toLocalDate() : null);
                    r.put("trangThai", rs.getString("trangThai"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi layDanhSachChoThanhToan: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<Map<String, Object>> layDanhSachHoaDonHomNay() {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT h.maHD, ISNULL(k.hoTen, N'Khách lẻ') AS tenKH, h.tongTien, "
                + "       h.hinhThucThanhToan, h.trangThai, ISNULL(h.ghiChu, '') AS ghiChu "
                + "FROM HoaDon h "
                + "LEFT JOIN KhachHang k ON h.maKH = k.maKH "
                + "WHERE h.ngayLap = CAST(GETDATE() AS DATE) "
                + "ORDER BY h.maHD DESC";

        try {
            Connection con = getValidConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("maHD", rs.getString("maHD"));
                    r.put("tenKH", rs.getString("tenKH"));
                    r.put("tongTien", (long) rs.getDouble("tongTien"));
                    r.put("hinhThucThanhToan", rs.getString("hinhThucThanhToan"));
                    r.put("trangThai", rs.getString("trangThai"));
                    r.put("ghiChu", rs.getString("ghiChu"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi layDanhSachHoaDonHomNay: " + e.getMessage());
        }
        return list;
    }
}
