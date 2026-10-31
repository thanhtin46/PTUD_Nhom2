package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectDB {

    private static final ConnectDB instance = new ConnectDB();

    private Connection connection;
    private static boolean daDongBoCauTruc = false;

    // Danh sách mật khẩu phổ biến của các thành viên trong nhóm để máy nào cũng chạy được
    private static final String[] DANH_SACH_MAT_KHAU = {
            "sapassword123@",
            "sapassword",
            "123456",
            "123",
            "sa"
    };

    private static final String BASE_URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=QuanLySuaChuaXe;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;"
                    + "loginTimeout=3;";

    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword123@";

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    public synchronized void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            String configuredUrl = System.getProperty("gara.db.url", System.getenv("GARA_DB_URL"));
            if (configuredUrl != null && !configuredUrl.isBlank()) {
                String configuredUser = System.getProperty("gara.db.user", System.getenv("GARA_DB_USER"));
                String configuredPassword = System.getProperty("gara.db.password", System.getenv("GARA_DB_PASSWORD"));
                connection = configuredUser == null || configuredUser.isBlank()
                        ? DriverManager.getConnection(configuredUrl)
                        : DriverManager.getConnection(configuredUrl, configuredUser, configuredPassword);
                damBaoCauTrucDatabase(connection);
                return;
            }
            // 1. Thử mật khẩu chính của máy hiện tại
            try {
                connection = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
                if (connection != null && !connection.isClosed()) {
                    System.out.println("Kết nối CSDL SQL Server (QuanLySuaChuaXe) thành công!");
                    damBaoCauTrucDatabase(connection);
                    return;
                }
            } catch (SQLException ignored) {}

            // 2. Thử các mật khẩu dự phòng khác của thành viên trong nhóm
            for (String mk : DANH_SACH_MAT_KHAU) {
                if (mk.equals(PASSWORD)) continue;
                try {
                    connection = DriverManager.getConnection(BASE_URL, USER, mk);
                    if (connection != null && !connection.isClosed()) {
                        System.out.println("Kết nối CSDL SQL Server (QuanLySuaChuaXe) thành công với mật khẩu dự phòng!");
                        damBaoCauTrucDatabase(connection);
                        return;
                    }
                } catch (SQLException ignored) {}
            }

            // 3. Thử kết nối qua Windows Authentication (nếu cấu hình)
            try {
                String winUrl = "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=3;";
                connection = DriverManager.getConnection(winUrl);
                if (connection != null && !connection.isClosed()) {
                    System.out.println("Kết nối CSDL SQL Server qua Windows Authentication thành công!");
                    damBaoCauTrucDatabase(connection);
                    return;
                }
            } catch (SQLException ignored) {}

            System.err.println("Không thể kết nối SQL Server (localhost:1433, DB: QuanLySuaChuaXe). Kiểm tra cấu hình kết nối; dữ liệu chưa lưu sẽ không được xem là thành công.");

        } catch (Exception e) {
            connection = null;
            System.err.println("Lỗi khởi tạo kết nối CSDL: " + e.getMessage());
        }
    }

    // Kiểm tra và cập nhật cấu trúc bảng khi khởi động
    private static synchronized void damBaoCauTrucDatabase(Connection c) {
        if (c == null || daDongBoCauTruc) return;
        try (Statement stmt = c.createStatement()) {
            // NhanVien
            stmt.execute("IF COL_LENGTH('dbo.NhanVien', 'email') IS NULL ALTER TABLE dbo.NhanVien ADD email VARCHAR(100) NULL;");
            stmt.execute("IF COL_LENGTH('dbo.NhanVien', 'bacNghe') IS NULL ALTER TABLE dbo.NhanVien ADD bacNghe NVARCHAR(50) NULL;");
            stmt.execute("IF COL_LENGTH('dbo.NhanVien', 'boPhan') IS NULL ALTER TABLE dbo.NhanVien ADD boPhan NVARCHAR(50) NULL;");
            stmt.execute("IF COL_LENGTH('dbo.NhanVien', 'ngayVaoLam') IS NULL ALTER TABLE dbo.NhanVien ADD ngayVaoLam VARCHAR(20) NULL;");
            stmt.execute("IF COL_LENGTH('dbo.NhanVien', 'trangThai') IS NULL ALTER TABLE dbo.NhanVien ADD trangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_NV_TrangThai DEFAULT N'Đang làm việc' WITH VALUES;");
            // Chuẩn hóa cột DATE cho KhachHang và PhuongTien
            stmt.execute("IF COL_LENGTH('dbo.KhachHang', 'ngayDangKy') IS NOT NULL " +
                         "AND (SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='KhachHang' AND COLUMN_NAME='ngayDangKy') <> 'date' BEGIN " +
                         "  UPDATE dbo.KhachHang SET ngayDangKy = CONVERT(VARCHAR(10), CONVERT(DATE, ngayDangKy, 103), 120) WHERE ngayDangKy LIKE '__/__/____'; " +
                         "  ALTER TABLE dbo.KhachHang ALTER COLUMN ngayDangKy DATE NULL; " +
                         "END;");
            stmt.execute("IF COL_LENGTH('dbo.PhuongTien', 'ngayTiepNhanGanNhat') IS NOT NULL " +
                         "AND (SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='PhuongTien' AND COLUMN_NAME='ngayTiepNhanGanNhat') <> 'date' BEGIN " +
                         "  UPDATE dbo.PhuongTien SET ngayTiepNhanGanNhat = CONVERT(VARCHAR(10), CONVERT(DATE, ngayTiepNhanGanNhat, 103), 120) WHERE ngayTiepNhanGanNhat LIKE '__/__/____'; " +
                         "  ALTER TABLE dbo.PhuongTien ALTER COLUMN ngayTiepNhanGanNhat DATE NULL; " +
                         "END;");

            // PhieuTiepNhan / PhieuSuaChua: liên kết tường minh maPTN và maPhieuXuat
            stmt.execute("IF COL_LENGTH('dbo.PhieuSuaChua', 'maPTN') IS NULL BEGIN " +
                         "  ALTER TABLE dbo.PhieuSuaChua ADD maPTN VARCHAR(20) NULL CONSTRAINT FK_PSC_PTN REFERENCES dbo.PhieuTiepNhan(maPTN); " +
                         "  EXEC('UPDATE sc SET sc.maPTN = tn.maPTN FROM dbo.PhieuSuaChua sc JOIN dbo.PhieuTiepNhan tn ON sc.maPSC = ''SC'' + SUBSTRING(tn.maPTN, 3, 18) WHERE sc.maPTN IS NULL'); " +
                         "END;");
            stmt.execute("IF COL_LENGTH('dbo.PhieuSuaChua', 'maPhieuXuat') IS NULL ALTER TABLE dbo.PhieuSuaChua ADD maPhieuXuat VARCHAR(20) NULL;");
            stmt.execute("IF OBJECT_ID('dbo.FK_PSC_PhieuXuatKho', 'F') IS NULL AND OBJECT_ID('dbo.PhieuXuatKho', 'U') IS NOT NULL " +
                         "ALTER TABLE dbo.PhieuSuaChua ADD CONSTRAINT FK_PSC_PhieuXuatKho FOREIGN KEY (maPhieuXuat) REFERENCES dbo.PhieuXuatKho(maPhieuXuat);");

            // PhieuNhapKho: trạng thái, ngày giao, ghi chú
            stmt.execute("IF COL_LENGTH('dbo.PhieuNhapKho', 'trangThai') IS NULL ALTER TABLE dbo.PhieuNhapKho ADD trangThai VARCHAR(20) NOT NULL CONSTRAINT DF_PNK_TrangThai DEFAULT 'DA_NHAP' WITH VALUES;");
            stmt.execute("IF COL_LENGTH('dbo.PhieuNhapKho', 'ngayDuKienGiao') IS NULL ALTER TABLE dbo.PhieuNhapKho ADD ngayDuKienGiao DATE NULL;");
            stmt.execute("IF COL_LENGTH('dbo.PhieuNhapKho', 'ghiChu') IS NULL ALTER TABLE dbo.PhieuNhapKho ADD ghiChu NVARCHAR(255) NULL;");

            // PhieuBaoHanh
            stmt.execute("IF COL_LENGTH('dbo.PhieuBaoHanh', 'ngayLap') IS NULL ALTER TABLE dbo.PhieuBaoHanh ADD ngayLap DATE NOT NULL CONSTRAINT DF_PBH_NgayLap DEFAULT (CONVERT(date, GETDATE())) WITH VALUES;");
            stmt.execute("IF COL_LENGTH('dbo.PhieuBaoHanh', 'maNV') IS NULL ALTER TABLE dbo.PhieuBaoHanh ADD maNV VARCHAR(20) NULL;");
            stmt.execute("IF COL_LENGTH('dbo.PhieuBaoHanh', 'ghiChu') IS NULL ALTER TABLE dbo.PhieuBaoHanh ADD ghiChu NVARCHAR(MAX) NULL;");

            // ChiTietPhieuBaoHanh: Hợp nhất Hạng mục bảo hành vào ChiTietPhieuBaoHanh (khóa chính maPBH, maPT)
            stmt.execute("IF OBJECT_ID('dbo.ChiTietPhieuBaoHanh', 'U') IS NULL " +
                         "CREATE TABLE dbo.ChiTietPhieuBaoHanh (" +
                         "  maPBH VARCHAR(20) NOT NULL, maPT VARCHAR(20) NOT NULL, " +
                         "  tenHangMuc NVARCHAR(255) NOT NULL, thoiHanBaoHanh INT NOT NULL DEFAULT 6 CHECK (thoiHanBaoHanh > 0), " +
                         "  ngayHetHan DATE NOT NULL, dieuKienBaoHanh NVARCHAR(MAX) NULL, ghiChu NVARCHAR(MAX) NULL, " +
                         "  lyDo NVARCHAR(255) NULL, huongXuLy NVARCHAR(255) NULL, " +
                         "  CONSTRAINT PK_ChiTietPhieuBaoHanh PRIMARY KEY (maPBH, maPT), " +
                         "  CONSTRAINT FK_CTPBH_PhieuBaoHanh FOREIGN KEY (maPBH) REFERENCES dbo.PhieuBaoHanh(maPBH) ON DELETE CASCADE, " +
                         "  CONSTRAINT FK_CTPBH_PhuTung FOREIGN KEY (maPT) REFERENCES dbo.PhuTung(maPT));");
            stmt.execute("IF COL_LENGTH('dbo.ChiTietPhieuBaoHanh', 'tenHangMuc') IS NULL ALTER TABLE dbo.ChiTietPhieuBaoHanh ADD tenHangMuc NVARCHAR(255) NULL;");
            stmt.execute("IF COL_LENGTH('dbo.ChiTietPhieuBaoHanh', 'thoiHanBaoHanh') IS NULL ALTER TABLE dbo.ChiTietPhieuBaoHanh ADD thoiHanBaoHanh INT NOT NULL CONSTRAINT DF_CTPBH_ThoiHan DEFAULT 6 WITH VALUES;");
            stmt.execute("IF COL_LENGTH('dbo.ChiTietPhieuBaoHanh', 'ngayHetHan') IS NULL ALTER TABLE dbo.ChiTietPhieuBaoHanh ADD ngayHetHan DATE NULL;");
            stmt.execute("IF COL_LENGTH('dbo.ChiTietPhieuBaoHanh', 'dieuKienBaoHanh') IS NULL ALTER TABLE dbo.ChiTietPhieuBaoHanh ADD dieuKienBaoHanh NVARCHAR(MAX) NULL;");
            stmt.execute("IF COL_LENGTH('dbo.ChiTietPhieuBaoHanh', 'ghiChu') IS NULL ALTER TABLE dbo.ChiTietPhieuBaoHanh ADD ghiChu NVARCHAR(MAX) NULL;");

            // Chuyển dữ liệu từ bảng cũ HangMucBaoHanh sang ChiTietPhieuBaoHanh nếu bảng cũ còn tồn tại dưới dạng Table
            stmt.execute("IF OBJECT_ID('dbo.HangMucBaoHanh', 'U') IS NOT NULL BEGIN " +
                         "  UPDATE ct SET " +
                         "    ct.tenHangMuc = COALESCE(ct.tenHangMuc, hm.tenHangMuc), " +
                         "    ct.thoiHanBaoHanh = COALESCE(ct.thoiHanBaoHanh, hm.thoiHanBaoHanh, 6), " +
                         "    ct.ngayHetHan = COALESCE(ct.ngayHetHan, hm.ngayHetHan), " +
                         "    ct.dieuKienBaoHanh = COALESCE(ct.dieuKienBaoHanh, hm.dieuKienBaoHanh), " +
                         "    ct.ghiChu = COALESCE(ct.ghiChu, hm.ghiChu) " +
                         "  FROM dbo.ChiTietPhieuBaoHanh ct JOIN dbo.HangMucBaoHanh hm ON ct.maPBH = hm.maPBH; " +
                         "  INSERT INTO dbo.ChiTietPhieuBaoHanh (maPBH, maPT, tenHangMuc, thoiHanBaoHanh, ngayHetHan, dieuKienBaoHanh, ghiChu) " +
                         "  SELECT hm.maPBH, COALESCE((SELECT TOP 1 maPT FROM dbo.PhuTung WHERE tenPT = hm.tenHangMuc), 'PT001'), " +
                         "         hm.tenHangMuc, hm.thoiHanBaoHanh, hm.ngayHetHan, hm.dieuKienBaoHanh, hm.ghiChu " +
                         "  FROM dbo.HangMucBaoHanh hm " +
                         "  WHERE NOT EXISTS (SELECT 1 FROM dbo.ChiTietPhieuBaoHanh ct WHERE ct.maPBH = hm.maPBH); " +
                         "  DROP TABLE dbo.HangMucBaoHanh; " +
                         "END;");

            // Tạo VIEW HangMucBaoHanh tương thích ngược
            stmt.execute("IF OBJECT_ID('dbo.HangMucBaoHanh', 'V') IS NULL AND OBJECT_ID('dbo.HangMucBaoHanh', 'U') IS NULL " +
                         "EXEC('CREATE VIEW dbo.HangMucBaoHanh AS SELECT maPBH, maPT, tenHangMuc, thoiHanBaoHanh, ngayHetHan, dieuKienBaoHanh, ghiChu FROM dbo.ChiTietPhieuBaoHanh');");

            // ChiTietCongViecSuaChua
            stmt.execute("IF OBJECT_ID('dbo.ChiTietCongViecSuaChua', 'U') IS NULL " +
                         "CREATE TABLE dbo.ChiTietCongViecSuaChua (" +
                         "  maPSC VARCHAR(20) NOT NULL, stt INT NOT NULL, noiDungCongViec NVARCHAR(255) NOT NULL, " +
                         "  tenKTV NVARCHAR(100) NULL, tienCong DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (tienCong >= 0), " +
                         "  PRIMARY KEY (maPSC, stt), CONSTRAINT FK_CTCV_PSC FOREIGN KEY (maPSC) REFERENCES dbo.PhieuSuaChua(maPSC));");

            // ChiTietHoaDon & ChiTietDichVuHoaDon: Bổ sung donViTinh
            stmt.execute("IF COL_LENGTH('dbo.ChiTietHoaDon', 'donViTinh') IS NULL ALTER TABLE dbo.ChiTietHoaDon ADD donViTinh NVARCHAR(50) NULL CONSTRAINT DF_CTHD_DonViTinh DEFAULT N'Cái' WITH VALUES;");
            stmt.execute("IF OBJECT_ID('dbo.ChiTietDichVuHoaDon', 'U') IS NULL " +
                         "CREATE TABLE dbo.ChiTietDichVuHoaDon (" +
                         "  maHD VARCHAR(20) NOT NULL, stt INT NOT NULL, tenDichVu NVARCHAR(255) NOT NULL, " +
                         "  donViTinh NVARCHAR(50) NULL CONSTRAINT DF_CTDVHD_DVT DEFAULT N'Lần', " +
                         "  soLuong INT NOT NULL CHECK (soLuong > 0), donGia DECIMAL(15,2) NOT NULL CHECK (donGia >= 0), " +
                         "  PRIMARY KEY (maHD, stt), CONSTRAINT FK_CTDVHD_HoaDon FOREIGN KEY (maHD) REFERENCES dbo.HoaDon(maHD));");
            stmt.execute("IF COL_LENGTH('dbo.ChiTietDichVuHoaDon', 'donViTinh') IS NULL ALTER TABLE dbo.ChiTietDichVuHoaDon ADD donViTinh NVARCHAR(50) NULL CONSTRAINT DF_CTDVHD_DonViTinh DEFAULT N'Lần' WITH VALUES;");

            // HoaDon: Đảm bảo không có FK_HoaDon_PhieuSuaChua và không có cột soPhieuSC
            stmt.execute("IF OBJECT_ID('dbo.FK_HoaDon_PhieuSuaChua', 'F') IS NOT NULL ALTER TABLE dbo.HoaDon DROP CONSTRAINT FK_HoaDon_PhieuSuaChua;");
            stmt.execute("IF COL_LENGTH('dbo.HoaDon', 'soPhieuSC') IS NOT NULL ALTER TABLE dbo.HoaDon DROP COLUMN soPhieuSC;");

            // Dữ liệu mẫu hóa đơn bán lẻ phụ tùng shop (HD201, HD202, HD203)
            stmt.execute("IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon WHERE maHD = 'HD201') " +
                         "INSERT INTO dbo.HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) " +
                         "VALUES ('HD201', 'KH101', 'NV05', '2026-10-25', 'CHUYEN_KHOAN', 0, 420000, 0, 420000, N'Bán lẻ phụ tùng tại shop', 'DA_THANH_TOAN');");
            stmt.execute("IF NOT EXISTS (SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHD = 'HD201' AND maPT = 'PT101') " +
                         "INSERT INTO dbo.ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES " +
                         "('HD201', 'PT101', N'Lọc dầu động cơ', N'Chiếc', 1, 150000), " +
                         "('HD201', 'PT103', N'Bugi Denso Iridium', N'Cái', 2, 85000), " +
                         "('HD201', 'PT110', N'Nước làm mát cao cấp', N'Can', 1, 100000);");

            stmt.execute("IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon WHERE maHD = 'HD202') " +
                         "INSERT INTO dbo.HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) " +
                         "VALUES ('HD202', 'KH102', 'NV05', '2026-10-26', 'TIEN_MAT', 0, 480000, 5, 456000, N'Bán lẻ dầu nhớt động cơ (Chiết khấu 5%)', 'DA_THANH_TOAN');");
            stmt.execute("IF NOT EXISTS (SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHD = 'HD202' AND maPT = 'PT104') " +
                         "INSERT INTO dbo.ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES " +
                         "('HD202', 'PT104', N'Dầu động cơ Castrol Magnatec', N'Lít', 4, 120000);");

            stmt.execute("IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon WHERE maHD = 'HD203') " +
                         "INSERT INTO dbo.HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) " +
                         "VALUES ('HD203', 'KH103', 'NV05', '2026-10-27', 'TIEN_MAT', 0, 350000, 0, 350000, N'Khách đổi ý trả hàng hủy hóa đơn', 'DA_HUY');");
            stmt.execute("IF NOT EXISTS (SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHD = 'HD203' AND maPT = 'PT109') " +
                         "INSERT INTO dbo.ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES " +
                         "('HD203', 'PT109', N'Cần gạt mưa Bosch AeroTwin', N'Bộ', 1, 350000);");

            // QuyenBoSung & NhatKyQuyen
            stmt.execute("IF OBJECT_ID('dbo.QuyenBoSung', 'U') IS NULL " +
                         "CREATE TABLE dbo.QuyenBoSung (" +
                         "  maNV VARCHAR(20) NOT NULL, maQuyen VARCHAR(40) NOT NULL, " +
                         "  CONSTRAINT PK_QuyenBoSung PRIMARY KEY(maNV, maQuyen), " +
                         "  CONSTRAINT FK_QBS_NhanVien FOREIGN KEY (maNV) REFERENCES dbo.NhanVien(maNV) ON DELETE CASCADE);");
            stmt.execute("IF OBJECT_ID('dbo.NhatKyQuyen', 'U') IS NULL " +
                         "CREATE TABLE dbo.NhatKyQuyen (" +
                         "  id BIGINT IDENTITY(1,1) PRIMARY KEY, thoiGian DATETIME2 NOT NULL DEFAULT SYSDATETIME(), " +
                         "  nguoiThucHien VARCHAR(20) NOT NULL, doiTuong VARCHAR(20) NOT NULL, " +
                         "  noiDung NVARCHAR(2000) NOT NULL);");

            daDongBoCauTruc = true;
        } catch (SQLException ex) {
            System.err.println("Cảnh báo đồng bộ cấu trúc CSDL: " + ex.getMessage());
        }
    }

    public synchronized void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = null;

        } catch (SQLException e) {
            System.err.println("Lỗi đóng kết nối: " + e.getMessage());
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra kết nối: " + e.getMessage());
        }

        return connection;
    }

    public static void main(String[] args) {
        Connection con = getInstance().getConnection();

        if (con != null) {
            System.out.println("Cơ sở dữ liệu: " + getCatalog(con));
        } else {
            System.out.println("Kết nối thất bại. Xem thông báo lỗi phía trên.");
        }
    }

    private static String getCatalog(Connection con) {
        try {
            return con.getCatalog();
        } catch (SQLException e) {
            return "Không xác định";
        }
    }
}
