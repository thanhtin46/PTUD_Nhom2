package DAO;

import ConnectDB.ConnectDB;
import Entity.KhoDuLieu;
import Entity.PhieuBaoHanh;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Tắt cảnh báo SQL của IntelliJ (chưa cấu hình Data Source nên báo "dbo" đỏ).
// Đây không phải lỗi biên dịch, code vẫn chạy bình thường.
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class PhieuBaoHanh_DAO {
    private final Connection suppliedConnection;

    public PhieuBaoHanh_DAO() { this.suppliedConnection = null; }

    // Allows transaction tests without opening the application's shared connection.
    public PhieuBaoHanh_DAO(Connection connection) { this.suppliedConnection = connection; }

    // Lấy kết nối dùng chung của project.
    // Lưu ý: KHÔNG đóng Connection ở đây vì ConnectDB dùng chung 1 kết nối.
    // Chỉ đóng PreparedStatement và ResultSet (đã dùng try-with-resources).
    private Connection getConnection() throws SQLException {
        Connection c = suppliedConnection != null ? suppliedConnection : ConnectDB.getInstance().getConnection();
        if (c == null || c.isClosed()) throw new SQLException("Chưa kết nối SQL Server.");
        return c;
    }

    // Kiểm tra chuỗi null hoặc rỗng (thay cho String.isBlank() của Java 11)
    private boolean rong(String s) {
        return s == null || s.trim().isEmpty();
    }

    // =========================================================
    // 1. LỚP LỰA CHỌN HIỂN THỊ TRÊN COMBOBOX
    // =========================================================
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

    // =========================================================
    // 2. LỚP CHI TIẾT BẢO HÀNH
    // =========================================================
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

        String sql = "SELECT COUNT(*) FROM dbo.PhieuBaoHanh WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maHoaDon = ? AND trangThaiBH <> N'DA_HUY'";

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

            Date ngayKetThuc = ngayBatDau;
            for (Object[] ct : dsChiTiet) {
                if (ct == null || ct.length < 5 || !(ct[1] instanceof Number)
                        || !(ct[2] instanceof Date)) throw new SQLException("Chi tiết bảo hành không hợp lệ.");
                int thang = ((Number) ct[1]).intValue();
                Date hetHan = (Date) ct[2];
                if (thang <= 0 || !hetHan.equals(Date.valueOf(ngayBatDau.toLocalDate().plusMonths(thang))))
                    throw new SQLException("Ngày hết hạn phải khớp ngày bắt đầu và số tháng bảo hành. Hãy nhập lại hạng mục.");
                if (hetHan.after(ngayKetThuc)) ngayKetThuc = hetHan;
            }
            luuPhieu(c, maPBH, maHD, maNV, bienSoXe, ngayBatDau, ngayKetThuc, trangThai, ghiChu);
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
                          String bienSoXe, Date ngayBatDau, Date ngayKetThuc,
                          String trangThai, String ghiChu) throws SQLException {
        String sql = "INSERT INTO dbo.PhieuBaoHanh "
                + "(maPBH, maHoaDon, maNV, ngayBD, ngayKT, trangThaiBH, ghiChu, maKH, soVIN) "
                + "SELECT ?, h.maHD, ?, ?, ?, ?, ?, h.maKH, pt.soVIN FROM dbo.HoaDon h "
                + "JOIN dbo.PhuongTien pt ON pt.maKH = h.maKH AND pt.bienSoXe = ? "
                + "LEFT JOIN dbo.PhieuSuaChua sc ON sc.maPSC = h.soPhieuSC "
                + "WHERE h.maHD = ? AND (h.soPhieuSC IS NULL OR sc.soVIN = pt.soVIN)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maPBH); ps.setString(2, maNV);
            ps.setDate(3, ngayBatDau); ps.setDate(4, ngayKetThuc);
            ps.setString(5, toDatabaseStatus(trangThai)); ps.setString(6, ghiChu);
            ps.setString(7, bienSoXe); ps.setString(8, maHD);
            if (ps.executeUpdate() != 1) throw new SQLException("Xe không khớp với hóa đơn đã chọn.");
        }
    }
    // Lưu các hạng mục vào bảng ChiTietPhieuBaoHanh
    private void luuChiTiet(Connection c, String maPBH, List<Object[]> dsChiTiet)
            throws SQLException {

        String sql = "INSERT INTO dbo.HangMucBaoHanh "
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
            System.err.println("Rollback thất bại: " + ex.getMessage());
        }
    }

    // =========================================================
    // 8. LẤY THÔNG TIN PHIẾU ĐỂ IN
    // =========================================================
    public ThongTinIn layThongTinIn(String maPBH) throws SQLException {

        String sql = "SELECT b.maPBH, b.maHoaDon AS maHD, b.maNV, nv.hoTen AS tenNhanVien, "
                + "b.ngayLap, b.ngayBD AS ngayBatDau, b.trangThaiBH AS trangThai, b.ghiChu, pt.bienSoXe, "
                + "h.maKH, k.hoTen AS tenKhachHang, h.tongTien "
                + "FROM dbo.PhieuBaoHanh b JOIN dbo.PhuongTien pt ON pt.soVIN = b.soVIN "
                + "JOIN dbo.HoaDon h ON h.maHD = b.maHoaDon "
                + "LEFT JOIN dbo.NhanVien nv ON nv.maNV = b.maNV "
                + "LEFT JOIN dbo.KhachHang k ON k.maKH = h.maKH "
                + "WHERE b.maPBH = ?";

        String sqlCT = "SELECT tenHangMuc, thoiHanBaoHanh, ngayHetHan, "
                + "dieuKienBaoHanh, ghiChu "
                + "FROM dbo.HangMucBaoHanh "
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

                t.trangThai = hienThiTrangThai(rs.getString("trangThai"));
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

    // =========================================================
    // 9. LẤY DANH SÁCH PHIẾU BẢO HÀNH ĐÃ LẬP
    // =========================================================
    public List<Object[]> layDanhSachPhieu() throws SQLException {

        String sql = "SELECT b.maPBH, b.maHoaDon AS maHD, b.maNV, b.ngayLap, b.ngayBD AS ngayBatDau, "
                + "b.trangThaiBH AS trangThai, pt.bienSoXe, k.hoTen AS tenKhachHang "
                + "FROM dbo.PhieuBaoHanh b JOIN dbo.PhuongTien pt ON pt.soVIN = b.soVIN "
                + "JOIN dbo.HoaDon h ON h.maHD = b.maHoaDon "
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
                        hienThiTrangThai(rs.getString("trangThai")),
                        rs.getString("bienSoXe"),
                        rs.getString("tenKhachHang")
                });
            }
        }

        return ds;
    }
    public ArrayList<PhieuBaoHanh> layTatCa() {
        ArrayList<PhieuBaoHanh> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return ds;
        String sql = "SELECT bh.maPBH, pt.bienSoXe, CONVERT(VARCHAR(10), bh.ngayBD, 103) AS ngayBD, " +
                "CONVERT(VARCHAR(10), bh.ngayKT, 103) AS ngayKT, bh.trangThaiBH, COALESCE(ct.lyDo, hm.noiDung, bh.ghiChu) AS lyDo " +
                "FROM PhieuBaoHanh bh JOIN PhuongTien pt ON pt.soVIN = bh.soVIN " +
                "OUTER APPLY (SELECT STRING_AGG(CAST(lyDo AS nvarchar(max)), N'; ') AS lyDo FROM ChiTietPhieuBaoHanh WHERE maPBH = bh.maPBH) ct " + "OUTER APPLY (SELECT STRING_AGG(CAST(tenHangMuc AS nvarchar(max)), N'; ') AS noiDung FROM HangMucBaoHanh WHERE maPBH = bh.maPBH) hm ORDER BY bh.maPBH DESC";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String status = rs.getString("trangThaiBH");
                String displayStatus = switch (status == null ? "" : status.toUpperCase()) {
                    case "CON_HAN" -> "Còn hạn";
                    case "DANG_XU_LY" -> "Đang xử lý";
                    case "HET_HAN" -> "Hết hạn";
                    case "TAM_NGUNG" -> "Tạm ngưng";
                    case "DA_HUY" -> "Đã hủy";
                    default -> status == null ? "" : status;
                };
                ds.add(new PhieuBaoHanh(rs.getString("maPBH"), rs.getString("bienSoXe"),
                        rs.getString("ngayBD"), rs.getString("ngayKT"), "", displayStatus,
                        rs.getString("lyDo") == null ? "" : rs.getString("lyDo")));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhieuBaoHanh_DAO.layTatCa: " + e.getMessage());
        }
        return ds;
    }

    public ArrayList<PhieuBaoHanh> traCuu(String tuKhoa, int tieuChi, String tinhTrang) {
        String q = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        ArrayList<PhieuBaoHanh> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        ArrayList<PhieuBaoHanh> source = con == null
                ? new ArrayList<>(KhoDuLieu.get().getPhieuBaoHanhs()) : layTatCa();
        for (PhieuBaoHanh p : source) {
            String text = switch (tieuChi) {
                case 1 -> p.getSoPhieu();
                case 2 -> p.getBienSo();
                case 3 -> p.getTinhTrang();
                case 4 -> p.getNoiDung();
                default -> String.join(" ", safe(p.getSoPhieu()), safe(p.getBienSo()), safe(p.getNgayBatDau()),
                        safe(p.getNgayKetThuc()), safe(p.getTinhTrang()), safe(p.getNoiDung()));
            };
            boolean matchText = q.isEmpty() || safe(text).toLowerCase().contains(q);
            boolean matchStatus = tinhTrang == null || tinhTrang.isBlank() || "TAT_CA".equalsIgnoreCase(tinhTrang)
                    || tinhTrang.equalsIgnoreCase(p.getTinhTrang());
            if (matchText && matchStatus) result.add(p);
        }
        return result;
    }

    public boolean them(PhieuBaoHanh pbh) {
        if (pbh == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        String lookup = "SELECT soVIN, maKH FROM PhuongTien WHERE bienSoXe = ?";
        try {
            String vin, maKH;
            try (PreparedStatement ps = con.prepareStatement(lookup)) {
                ps.setString(1, pbh.getBienSo());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    vin = rs.getString("soVIN"); maKH = rs.getString("maKH");
                }
            }
            con.setAutoCommit(false);
            String status = toDatabaseStatus(pbh.getTinhTrang());
            String insert = "INSERT INTO PhieuBaoHanh (maPBH, ngayBD, ngayKT, trangThaiBH, maKH, soVIN, maHoaDon) " +
                    "VALUES (?, TRY_CONVERT(date, ?, 103), TRY_CONVERT(date, ?, 103), ?, ?, ?, NULL)";
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setString(1, pbh.getSoPhieu()); ps.setString(2, pbh.getNgayBatDau()); ps.setString(3, pbh.getNgayKetThuc());
                ps.setString(4, status); ps.setString(5, maKH); ps.setString(6, vin); ps.executeUpdate();
            }
            themChiTietMacDinh(con, pbh);
            con.commit();
            return true;
        } catch (SQLException e) {
            rollback(con); System.err.println("Lỗi PhieuBaoHanh_DAO.them: " + e.getMessage()); return false;
        } finally { autoCommit(con); }
    }

    public boolean capNhat(PhieuBaoHanh pbh) {
        if (pbh == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        String sql = "UPDATE bh SET ngayBD = TRY_CONVERT(date, ?, 103), ngayKT = TRY_CONVERT(date, ?, 103), " +
                "trangThaiBH = ?, soVIN = pt.soVIN, maKH = pt.maKH FROM PhieuBaoHanh bh " +
                "JOIN PhuongTien pt ON pt.bienSoXe = ? WHERE bh.maPBH = ?";
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, pbh.getNgayBatDau()); ps.setString(2, pbh.getNgayKetThuc());
                ps.setString(3, toDatabaseStatus(pbh.getTinhTrang())); ps.setString(4, pbh.getBienSo()); ps.setString(5, pbh.getSoPhieu());
                if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE ChiTietPhieuBaoHanh SET lyDo = ? WHERE maPBH = ?")) {
                ps.setString(1, pbh.getNoiDung()); ps.setString(2, pbh.getSoPhieu());
                if (ps.executeUpdate() == 0) themChiTietMacDinh(con, pbh);
            }
            con.commit(); return true;
        } catch (SQLException e) {
            rollback(con); System.err.println("Lỗi PhieuBaoHanh_DAO.capNhat: " + e.getMessage()); return false;
        } finally { autoCommit(con); }
    }

    public boolean xoa(String soPhieu) {
        if (soPhieu == null || soPhieu.isBlank()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuBaoHanh WHERE maPBH = ?")) {
                ps.setString(1, soPhieu); ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuBaoHanh WHERE maPBH = ?")) {
                ps.setString(1, soPhieu); if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }
            con.commit(); return true;
        } catch (SQLException e) {
            rollback(con); System.err.println("Lỗi PhieuBaoHanh_DAO.xoa: " + e.getMessage()); return false;
        } finally { autoCommit(con); }
    }

    private void themChiTietMacDinh(Connection con, PhieuBaoHanh pbh) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuBaoHanh (maPBH, maPT, lyDo, huongXuLy) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pbh.getSoPhieu()); ps.setString(2, "PT001");
            ps.setString(3, pbh.getNoiDung() == null ? "Bảo hành linh kiện xe" : pbh.getNoiDung());
            ps.setString(4, "Kiểm tra và thay thế miễn phí"); ps.executeUpdate();
        }
    }

    private String toDatabaseStatus(String status) {
        if ("Tạm ngưng".equalsIgnoreCase(status)) return "TAM_NGUNG";
        if ("Đã hủy".equalsIgnoreCase(status)) return "DA_HUY";
        if ("Hết hạn".equalsIgnoreCase(status)) return "HET_HAN";
        if ("Đang xử lý".equalsIgnoreCase(status)) return "DANG_XU_LY";
        return "CON_HAN";
    }
    private String hienThiTrangThai(String status) {
        if (status == null) return "";
        return switch (status) {
            case "CON_HAN" -> "Còn hạn";
            case "HET_HAN" -> "Hết hạn";
            case "DANG_XU_LY" -> "Đang xử lý";
            case "TAM_NGUNG" -> "Tạm ngưng";
            case "DA_HUY" -> "Đã hủy";
            default -> status;
        };
    }

    private String safe(String value) { return value == null ? "" : value; }
    private void rollback(Connection con) { if (con != null) try { con.rollback(); } catch (SQLException ignored) { } }
    private void autoCommit(Connection con) { if (con != null) try { con.setAutoCommit(true); } catch (SQLException ignored) { } }
}