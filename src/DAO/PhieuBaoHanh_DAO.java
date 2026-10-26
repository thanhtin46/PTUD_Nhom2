
package DAO;

import ConnectDB.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuBaoHanh_DAO {

    private Connection getConnection() {
        return ConnectDB.getInstance().getConnection();
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

        String sql = """
            SELECT h.maHD, h.ngayLap, h.tongTien,
                   h.maKH, k.hoTen
            FROM dbo.HoaDon h
            LEFT JOIN dbo.KhachHang k ON k.maKH = h.maKH
            ORDER BY h.ngayLap DESC, h.maHD DESC
            """;

        List<LuaChon> ds = new ArrayList<>();

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maHD = rs.getString("maHD");
                Date ngay = rs.getDate("ngayLap");
                String tenKH = rs.getString("hoTen");
                String tongTien = rs.getString("tongTien");

                String ten = maHD
                             + " | " + ngay
                             + " | " + (tenKH == null
                        ? "Chưa có tên KH" : tenKH)
                             + " | Tổng tiền: " + tongTien;

                ds.add(new LuaChon(maHD, ten));
            }
        }

        return ds;
    }

    // =========================================================
    // 5. SINH MÃ PHIẾU PBH00001, PBH00002...
    // =========================================================
    public String taoMaPhieuMoi() throws SQLException {

        String sql = """
            SELECT ISNULL(MAX(
                TRY_CONVERT(INT, SUBSTRING(maPBH, 4, 17))
            ), 0) + 1 AS soMoi
            FROM dbo.PhieuBaoHanh
            WHERE maPBH LIKE 'PBH%'
            """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return String.format("PBH%05d", rs.getInt("soMoi"));
            }
        }

        throw new SQLException("Không tạo được mã phiếu bảo hành.");
    }

    // =========================================================
    // 6. KIỂM TRA HÓA ĐƠN ĐÃ CÓ PHIẾU BẢO HÀNH CHƯA
    // =========================================================
    public boolean hoaDonDaCoPhieu(String maHD) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM dbo.PhieuBaoHanh
            WHERE maHD = ?
              AND trangThai <> N'Đã hủy'
            """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // =========================================================
    // 7. THÊM PHIẾU BẢO HÀNH VÀ CÁC HẠNG MỤC
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

        if (maPBH == null || maPBH.isBlank()) {
            throw new SQLException("Mã phiếu bảo hành không được để trống.");
        }

        if (maHD == null || maHD.isBlank()) {
            throw new SQLException("Vui lòng chọn hóa đơn.");
        }

        if (maNV == null || maNV.isBlank()) {
            throw new SQLException("Vui lòng chọn nhân viên.");
        }

        if (bienSoXe == null || bienSoXe.isBlank()) {
            throw new SQLException("Vui lòng nhập hoặc chọn biển số xe.");
        }

        if (ngayBatDau == null) {
            throw new SQLException("Ngày bắt đầu bảo hành không được để trống.");
        }

        if (dsChiTiet == null || dsChiTiet.isEmpty()) {
            throw new SQLException(
                    "Phiếu bảo hành phải có ít nhất một hạng mục."
            );
        }

        String sqlPhieu = """
            INSERT INTO dbo.PhieuBaoHanh
                (maPBH, maHD, maNV, ngayBatDau,
                 trangThai, ghiChu, bienSoXe)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        String sqlChiTiet = """
            INSERT INTO dbo.ChiTietPhieuBaoHanh
                (maPBH, tenHangMuc, thoiHanBaoHanh,
                 ngayHetHan, dieuKienBaoHanh, ghiChu)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection c = getConnection()) {

            boolean autoCu = c.getAutoCommit();

            try {
                c.setAutoCommit(false);

                // Kiểm tra hóa đơn tồn tại và khóa bản ghi trong transaction.
                String sqlHoaDon = """
                    SELECT maHD
                    FROM dbo.HoaDon WITH (UPDLOCK, HOLDLOCK)
                    WHERE maHD = ?
                    """;

                try (PreparedStatement ps =
                             c.prepareStatement(sqlHoaDon)) {

                    ps.setString(1, maHD);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Hóa đơn không tồn tại.");
                        }
                    }
                }

                // Kiểm tra hóa đơn đã có phiếu bảo hành hay chưa.
                String sqlTrung = """
                    SELECT COUNT(*)
                    FROM dbo.PhieuBaoHanh WITH (UPDLOCK, HOLDLOCK)
                    WHERE maHD = ?
                      AND trangThai <> N'Đã hủy'
                    """;

                try (PreparedStatement ps =
                             c.prepareStatement(sqlTrung)) {

                    ps.setString(1, maHD);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException(
                                    "Hóa đơn này đã có phiếu bảo hành."
                            );
                        }
                    }
                }

                // Ngày bắt đầu không được trước ngày lập phiếu.
                // ngayLap do SQL Server tự lấy theo DEFAULT.
                String sqlNgayLap = """
                    SELECT CAST(GETDATE() AS DATE) AS ngayLap
                    """;

                try (PreparedStatement ps =
                             c.prepareStatement(sqlNgayLap);
                     ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        Date ngayLap = rs.getDate("ngayLap");

                        if (ngayBatDau.before(ngayLap)) {
                            throw new SQLException(
                                    "Ngày bắt đầu bảo hành không được trước ngày lập phiếu."
                            );
                        }
                    }
                }

                // Lưu phiếu bảo hành.
                try (PreparedStatement ps =
                             c.prepareStatement(sqlPhieu)) {

                    ps.setString(1, maPBH);
                    ps.setString(2, maHD);
                    ps.setString(3, maNV);
                    ps.setDate(4, ngayBatDau);
                    ps.setString(5, trangThai);
                    ps.setString(6, ghiChu);
                    ps.setString(7, bienSoXe);

                    ps.executeUpdate();
                }

                // Lưu danh sách hạng mục bảo hành.
                try (PreparedStatement ps =
                             c.prepareStatement(sqlChiTiet)) {

                    for (Object[] ct : dsChiTiet) {

                        if (ct == null || ct.length < 5) {
                            throw new SQLException(
                                    "Dữ liệu chi tiết phiếu bảo hành không hợp lệ."
                            );
                        }

                        String tenHangMuc = (String) ct[0];
                        int thoiHan = ((Number) ct[1]).intValue();
                        Date ngayHetHan = (Date) ct[2];
                        String dieuKien = (String) ct[3];
                        String ghiChuCT = (String) ct[4];

                        if (tenHangMuc == null || tenHangMuc.isBlank()) {
                            throw new SQLException(
                                    "Tên hạng mục bảo hành không được để trống."
                            );
                        }

                        if (thoiHan <= 0) {
                            throw new SQLException(
                                    "Thời hạn bảo hành phải lớn hơn 0."
                            );
                        }

                        if (ngayHetHan == null) {
                            throw new SQLException(
                                    "Ngày hết hạn bảo hành không được để trống."
                            );
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

                c.commit();

            } catch (Exception ex) {

                try {
                    c.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }

                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }

                throw new SQLException("Lỗi lưu phiếu bảo hành.", ex);

            } finally {
                try {
                    c.setAutoCommit(autoCu);
                } catch (SQLException ignored) {
                    // Không ghi đè lỗi ban đầu.
                }
            }
        }
    }

    // =========================================================
    // 8. LẤY THÔNG TIN PHIẾU ĐỂ IN
    // =========================================================
    public ThongTinIn layThongTinIn(String maPBH)
            throws SQLException {

        String sql = """
            SELECT b.maPBH, b.maHD, b.maNV,
                   nv.hoTen AS tenNhanVien,
                   b.ngayLap, b.ngayBatDau,
                   b.trangThai, b.ghiChu, b.bienSoXe,
                   h.maKH, k.hoTen AS tenKhachHang,
                   h.tongTien
            FROM dbo.PhieuBaoHanh b
            JOIN dbo.HoaDon h ON h.maHD = b.maHD
            JOIN dbo.NhanVien nv ON nv.maNV = b.maNV
            LEFT JOIN dbo.KhachHang k ON k.maKH = h.maKH
            WHERE b.maPBH = ?
            """;

        String sqlCT = """
            SELECT tenHangMuc, thoiHanBaoHanh, ngayHetHan,
                   dieuKienBaoHanh, ghiChu
            FROM dbo.ChiTietPhieuBaoHanh
            WHERE maPBH = ?
            ORDER BY maCTPBH
            """;

        try (Connection c = getConnection()) {

            ThongTinIn t = new ThongTinIn();

            try (PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, maPBH);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException(
                                "Không tìm thấy phiếu " + maPBH
                        );
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
    }

    // =========================================================
    // 9. LẤY DANH SÁCH PHIẾU BẢO HÀNH ĐÃ LẬP
    // =========================================================
    public List<Object[]> layDanhSachPhieu() throws SQLException {

        String sql = """
            SELECT b.maPBH, b.maHD, b.maNV,
                   b.ngayLap, b.ngayBatDau,
                   b.trangThai, b.bienSoXe,
                   k.hoTen AS tenKhachHang
            FROM dbo.PhieuBaoHanh b
            JOIN dbo.HoaDon h ON h.maHD = b.maHD
            LEFT JOIN dbo.KhachHang k ON k.maKH = h.maKH
            ORDER BY b.ngayLap DESC, b.maPBH DESC
            """;

        List<Object[]> ds = new ArrayList<>();

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
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
