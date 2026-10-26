package DAO;

import ConnectDB.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuBaoHanh_DAO {

    private Connection getConnection() {
        return ConnectDB.getInstance().getConnection();
    }

    private boolean rong(String s) {
        return s == null || s.trim().isEmpty();
    }

    // 1. LỚP LỰA CHỌN HIỂN THỊ TRÊN COMBOBOX
    public static class LuaChon {
        public final String ma;
        public final String ten;

        public LuaChon(String ma, String ten) {
            this.ma = ma;
            this.ten = ten;
        }

        @Override
        public String toString() {
            return ten;
        }
    }


    // 2. LỚP CHI TIẾT BẢO HÀNH
    public static class ChiTiet {
        public String tenHangMuc;
        public int thoiHan;
        public Date ngayHetHan;
        public String dieuKien;
        public String ghiChu;
    }

    // =========================================================
    // 3. LỚP DỮ LIỆU DÙNG ĐỂ IN PHIẾU
    // =========================================================
    public static class ThongTinIn {
        public String maPBH;
        public String maHD;
        public String maNV;
        public String tenNhanVien;

        public Date ngayLap;
        public Date ngayBatDau;

        public String trangThai;
        public String ghiChu;

        public String maKH;
        public String tenKhachHang;
        public String bienSoXe;
        public String tongTien;

        public List<ChiTiet> chiTiet = new ArrayList<>();
    }

    // =========================================================
    // 4. LẤY DANH SÁCH HÓA ĐƠN ĐỂ LẬP PHIẾU BẢO HÀNH
    // =========================================================
    public List<LuaChon> layDanhSachHoaDon() throws SQLException {

        String sql = "SELECT h.maHD, h.ngayLap, h.tongTien, h.maKH, k.hoTen "
                + "FROM dbo.HoaDon h "
                + "LEFT JOIN dbo.KhachHang k ON k.maKH = h.maKH "
                + "ORDER BY h.ngayLap DESC, h.maHD DESC";

        List<LuaChon> ds = new ArrayList<>();
        Connection c = getConnection();

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maHD = rs.getString("maHD");
                Date ngay = rs.getDate("ngayLap");
                String tenKH = rs.getString("hoTen");
                String tongTien = rs.getString("tongTien");

                if (tenKH == null) {
                    tenKH = "Chưa có tên KH";
                }

                String ten = maHD + " | " + ngay + " | " + tenKH
                        + " | Tổng tiền: " + tongTien;

                ds.add(new LuaChon(maHD, ten));
            }
        }

        return ds;
    }

    // =========================================================
    // 5. SINH MÃ PHIẾU: PBH00001, PBH00002, ...
    // =========================================================
    public String taoMaPhieuMoi() throws SQLException {

        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maPBH, 4, 17))), 0) + 1 AS soMoi "
                + "FROM dbo.PhieuBaoHanh "
                + "WHERE maPBH LIKE 'PBH%'";

        Connection c = getConnection();

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int soMoi = rs.getInt("soMoi");
                return String.format("PBH%05d", soMoi);
            }
        }

        throw new SQLException("Không tạo được mã phiếu bảo hành.");
    }

    // =========================================================
    // 6. KIỂM TRA HÓA ĐƠN ĐÃ CÓ PHIẾU BẢO HÀNH CHƯA
    // =========================================================
    public boolean hoaDonDaCoPhieu(String maHD) throws SQLException {
        return hoaDonDaCoPhieu(getConnection(), maHD);
    }

    // Bản dùng chung kết nối (để gọi bên trong transaction)
    private boolean hoaDonDaCoPhieu(Connection c, String maHD) throws SQLException {

        String sql = "SELECT COUNT(*) FROM dbo.PhieuBaoHanh "
                + "WHERE maHD = ? AND trangThai <> N'Đã hủy'";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // Kiểm tra hóa đơn có tồn tại không
    private boolean hoaDonTonTai(Connection c, String maHD) throws SQLException {

        String sql = "SELECT maHD FROM dbo.HoaDon WHERE maHD = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Ngày bắt đầu bảo hành không được trước ngày lập phiếu (ngày hôm nay)
    private void kiemTraNgayBatDau(Connection c, Date ngayBatDau) throws SQLException {

        String sql = "SELECT CAST(GETDATE() AS DATE) AS ngayLap";

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Date ngayLap = rs.getDate("ngayLap");

                if (ngayBatDau.before(ngayLap)) {
                    throw new SQLException(
                            "Ngày bắt đầu bảo hành không được trước ngày lập phiếu.");
                }
            }
        }
    }

    // =========================================================
    // 7. THÊM PHIẾU BẢO HÀNH VÀ CÁC HẠNG MỤC
    // dsChiTiet: mỗi phần tử là Object[] gồm 5 giá trị:
    //   [0] tenHangMuc (String)
    //   [1] thoiHan    (Number)
    //   [2] ngayHetHan (java.sql.Date)
    //   [3] dieuKien   (String)
    //   [4] ghiChu     (String)
    // =========================================================
    public void themPhieuBaoHanh(
            String maPBH,
            String maHD,
            String maNV,
            String bienSoXe,
            Date ngayBatDau,
            String trangThai,
            String ghiChu,
            List<Object[]> dsChiTiet) throws SQLException {

        // --- Kiểm tra dữ liệu đầu vào ---
        if (rong(maPBH)) {
            throw new SQLException("Mã phiếu bảo hành không được để trống.");
        }
        if (rong(maHD)) {
            throw new SQLException("Vui lòng chọn hóa đơn.");
        }
        if (rong(maNV)) {
            throw new SQLException("Vui lòng chọn nhân viên.");
        }
        if (rong(bienSoXe)) {
            throw new SQLException("Vui lòng nhập hoặc chọn biển số xe.");
        }
        if (ngayBatDau == null) {
            throw new SQLException("Ngày bắt đầu bảo hành không được để trống.");
        }
        if (dsChiTiet == null || dsChiTiet.isEmpty()) {
            throw new SQLException("Phiếu bảo hành phải có ít nhất một hạng mục.");
        }

        Connection c = getConnection();
        boolean autoCu = c.getAutoCommit();

        try {
            // Bắt đầu transaction: lưu phiếu + chi tiết cùng lúc, lỗi thì hủy hết
            c.setAutoCommit(false);

            if (!hoaDonTonTai(c, maHD)) {
                throw new SQLException("Hóa đơn không tồn tại.");
            }

            if (hoaDonDaCoPhieu(c, maHD)) {
                throw new SQLException("Hóa đơn này đã có phiếu bảo hành.");
            }

            kiemTraNgayBatDau(c, ngayBatDau);

            luuPhieu(c, maPBH, maHD, maNV, bienSoXe, ngayBatDau, trangThai, ghiChu);
            luuChiTiet(c, maPBH, dsChiTiet);

            c.commit();

        } catch (SQLException e) {
            huyGiaoDich(c);
            throw e;

        } catch (RuntimeException e) {
            // Ví dụ: ép kiểu sai, dữ liệu null trong chi tiết
            huyGiaoDich(c);
            throw new SQLException("Dữ liệu chi tiết phiếu bảo hành không hợp lệ.", e);

        } finally {
            c.setAutoCommit(autoCu);
        }
    }

    // Lưu 1 dòng vào bảng PhieuBaoHanh
    private void luuPhieu(Connection c, String maPBH, String maHD, String maNV,
                          String bienSoXe, Date ngayBatDau,
                          String trangThai, String ghiChu) throws SQLException {

        String sql = "INSERT INTO dbo.PhieuBaoHanh "
                + "(maPBH, maHD, maNV, ngayBatDau, trangThai, ghiChu, bienSoXe) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPBH);
            ps.setString(2, maHD);
            ps.setString(3, maNV);
            ps.setDate(4, ngayBatDau);
            ps.setString(5, trangThai);
            ps.setString(6, ghiChu);
            ps.setString(7, bienSoXe);

            ps.executeUpdate();
        }
    }

    // Lưu các hạng mục vào bảng ChiTietPhieuBaoHanh
    private void luuChiTiet(Connection c, String maPBH, List<Object[]> dsChiTiet)
            throws SQLException {

        String sql = "INSERT INTO dbo.ChiTietPhieuBaoHanh "
                + "(maPBH, tenHangMuc, thoiHanBaoHanh, ngayHetHan, dieuKienBaoHanh, ghiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {

            for (Object[] ct : dsChiTiet) {

                if (ct == null || ct.length < 5) {
                    throw new SQLException("Dữ liệu chi tiết phiếu bảo hành không hợp lệ.");
                }

                String tenHangMuc = (String) ct[0];
                int thoiHan = ((Number) ct[1]).intValue();
                Date ngayHetHan = (Date) ct[2];
                String dieuKien = (String) ct[3];
                String ghiChuCT = (String) ct[4];

                if (rong(tenHangMuc)) {
                    throw new SQLException("Tên hạng mục bảo hành không được để trống.");
                }
                if (thoiHan <= 0) {
                    throw new SQLException("Thời hạn bảo hành phải lớn hơn 0.");
                }
                if (ngayHetHan == null) {
                    throw new SQLException("Ngày hết hạn bảo hành không được để trống.");
                }

                ps.setString(1, maPBH);
                ps.setString(2, tenHangMuc);
                ps.setInt(3, thoiHan);
                ps.setDate(4, ngayHetHan);
                ps.setString(5, dieuKien);
                ps.setString(6, ghiChuCT);

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    // Hủy transaction, không để lỗi rollback che mất lỗi gốc
    private void huyGiaoDich(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // =========================================================
    // 8. LẤY THÔNG TIN PHIẾU ĐỂ IN
    // =========================================================
    public ThongTinIn layThongTinIn(String maPBH) throws SQLException {

        String sql = "SELECT b.maPBH, b.maHD, b.maNV, nv.hoTen AS tenNhanVien, "
                + "b.ngayLap, b.ngayBatDau, b.trangThai, b.ghiChu, b.bienSoXe, "
                + "h.maKH, k.hoTen AS tenKhachHang, h.tongTien "
                + "FROM dbo.PhieuBaoHanh b "
                + "JOIN dbo.HoaDon h ON h.maHD = b.maHD "
                + "JOIN dbo.NhanVien nv ON nv.maNV = b.maNV "
                + "LEFT JOIN dbo.KhachHang k ON k.maKH = h.maKH "
                + "WHERE b.maPBH = ?";

        String sqlCT = "SELECT tenHangMuc, thoiHanBaoHanh, ngayHetHan, "
                + "dieuKienBaoHanh, ghiChu "
                + "FROM dbo.ChiTietPhieuBaoHanh "
                + "WHERE maPBH = ? "
                + "ORDER BY maCTPBH";

        Connection c = getConnection();
        ThongTinIn t = new ThongTinIn();

        // Thông tin chung của phiếu
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPBH);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy phiếu " + maPBH);
                }

                t.maPBH = rs.getString("maPBH");
                t.maHD = rs.getString("maHD");
                t.maNV = rs.getString("maNV");
                t.tenNhanVien = rs.getString("tenNhanVien");

                t.ngayLap = rs.getDate("ngayLap");
                t.ngayBatDau = rs.getDate("ngayBatDau");

                t.trangThai = rs.getString("trangThai");
                t.ghiChu = rs.getString("ghiChu");

                t.maKH = rs.getString("maKH");
                t.tenKhachHang = rs.getString("tenKhachHang");
                t.bienSoXe = rs.getString("bienSoXe");
                t.tongTien = rs.getString("tongTien");
            }
        }

        // Các hạng mục bảo hành
        try (PreparedStatement ps = c.prepareStatement(sqlCT)) {
            ps.setString(1, maPBH);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    ChiTiet ct = new ChiTiet();

                    ct.tenHangMuc = rs.getString("tenHangMuc");
                    ct.thoiHan = rs.getInt("thoiHanBaoHanh");
                    ct.ngayHetHan = rs.getDate("ngayHetHan");
                    ct.dieuKien = rs.getString("dieuKienBaoHanh");
                    ct.ghiChu = rs.getString("ghiChu");

                    t.chiTiet.add(ct);
                }
            }
        }

        return t;
    }


    // 9. LẤY DANH SÁCH PHIẾU BẢO HÀNH ĐÃ LẬP
    public List<Object[]> layDanhSachPhieu() throws SQLException {

        String sql = "SELECT b.maPBH, b.maHD, b.maNV, b.ngayLap, b.ngayBatDau, "
                + "b.trangThai, b.bienSoXe, k.hoTen AS tenKhachHang "
                + "FROM dbo.PhieuBaoHanh b "
                + "JOIN dbo.HoaDon h ON h.maHD = b.maHD "
                + "LEFT JOIN dbo.KhachHang k ON k.maKH = h.maKH "
                + "ORDER BY b.ngayLap DESC, b.maPBH DESC";

        List<Object[]> ds = new ArrayList<>();
        Connection c = getConnection();

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ds.add(new Object[]{
                        rs.getString("maPBH"),
                        rs.getString("maHD"),
                        rs.getString("maNV"),
                        rs.getDate("ngayLap"),
                        rs.getDate("ngayBatDau"),
                        rs.getString("trangThai"),
                        rs.getString("bienSoXe"),
                        rs.getString("tenKhachHang")
                });
            }
        }

        return ds;
    }
}