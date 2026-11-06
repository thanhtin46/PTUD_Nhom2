package DAO;

import ConnectDB.ConnectDB;
import Entity.ChucVu;
import Entity.QuyenTruyCap;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object quản lý danh mục Chức vụ / Vai trò nhân sự trong hệ thống.
 */
public class ChucVu_DAO {

    /**
     * Tự động kiểm tra và khởi tạo bảng ChucVu trong SQL Server nếu chưa tồn tại,
     * đồng thời khởi tạo dữ liệu chức vụ mẫu và đồng bộ các chức vụ hiện có từ bảng NhanVien.
     */
    public static synchronized void kiemTraVaKhoiTaoBangChucVu(Connection con) {
        if (con == null) return;
        try {
            boolean bangTonTai = false;
            DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getTables(null, "dbo", "ChucVu", new String[]{"TABLE"})) {
                if (rs.next()) {
                    bangTonTai = true;
                }
            }

            if (!bangTonTai) {
                try (Statement st = con.createStatement()) {
                    st.execute("CREATE TABLE ChucVu (" +
                            "maCV VARCHAR(20) NOT NULL PRIMARY KEY, " +
                            "tenChucVu NVARCHAR(100) NOT NULL UNIQUE, " +
                            "moTa NVARCHAR(255) NULL, " +
                            "ngayTao VARCHAR(20) NULL DEFAULT CONVERT(VARCHAR(20), GETDATE(), 23)" +
                            ")");
                }
            }

            // Kiểm tra số lượng bản ghi hiện có
            int count = 0;
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ChucVu")) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }

            // Nếu bảng chưa có bản ghi, nạp 10 chức vụ mặc định
            if (count == 0) {
                String[][] macDinh = {
                        {"CV01", "Quản lý", "Ban Giám đốc, điều hành tổng thể gara, quản lý tài chính và nhân sự"},
                        {"CV02", "Cố vấn dịch vụ", "Tiếp nhận xe, tư vấn khách hàng, chẩn đoán ban đầu và lập báo giá"},
                        {"CV03", "Nhân viên tiếp tân", "Đón tiếp khách hàng, tạo phiếu tiếp nhận và hỗ trợ thủ tục"},
                        {"CV04", "Nhân viên sửa chữa", "Kỹ thuật viên thực hiện sửa chữa, bảo dưỡng theo lệnh xưởng"},
                        {"CV05", "KTV Máy - Gầm", "Kỹ thuật viên chuyên trách đại tu động cơ, hộp số và hệ thống gầm"},
                        {"CV06", "KTV Điện - Điện Lạnh Ô tô", "Kỹ thuật viên chuyên điện tử, điều khiển ECU và điện lạnh"},
                        {"CV07", "Trưởng tổ Kỹ thuật", "Quản lý tổ thợ, kiểm tra KCS chất lượng trước khi bàn giao xe"},
                        {"CV08", "Nhân viên kho", "Thủ kho phụ tùng, kiểm tra nhập xuất vật tư và tồn kho"},
                        {"CV09", "Nhân viên thu ngân", "Thu ngân quầy, lập và xuất hóa đơn dịch vụ, thanh toán VietQR"},
                        {"CV10", "KTV Học việc", "Học việc xưởng, hỗ trợ kỹ thuật viên chính và vệ sinh khu vực"}
                };

                String insertSql = "INSERT INTO ChucVu (maCV, tenChucVu, moTa, ngayTao) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    for (String[] cv : macDinh) {
                        ps.setString(1, cv[0]);
                        ps.setString(2, cv[1]);
                        ps.setString(3, cv[2]);
                        ps.setString(4, "2026-01-01");
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // Quét các chức vụ hiện có trong bảng NhanVien mà chưa có trong ChucVu để bổ sung tự động
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DISTINCT chucVu FROM NhanVien WHERE chucVu IS NOT NULL AND RTRIM(LTRIM(chucVu)) <> ''")) {
                List<String> dsChucVuNV = new ArrayList<>();
                while (rs.next()) {
                    String cvRaw = rs.getString(1);
                    if (cvRaw != null && !cvRaw.isBlank()) {
                        dsChucVuNV.add(QuyenTruyCap.giaiMaMojibake(cvRaw.trim()));
                    }
                }

                for (String cvName : dsChucVuNV) {
                    if (!kiemTraTonTaiTen(con, cvName)) {
                        String maMoi = sinhMaMoi(con);
                        try (PreparedStatement ps = con.prepareStatement(
                                "INSERT INTO ChucVu (maCV, tenChucVu, moTa, ngayTao) VALUES (?, ?, ?, ?)")) {
                            ps.setString(1, maMoi);
                            ps.setString(2, cvName);
                            ps.setString(3, "Chức vụ đồng bộ từ hồ sơ nhân sự hiện hữu");
                            ps.setString(4, LocalDate.now().toString());
                            ps.executeUpdate();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ChucVu> layDanhSachChucVu() {
        List<ChucVu> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return ds;

        kiemTraVaKhoiTaoBangChucVu(con);

        String sql = "SELECT maCV, tenChucVu, moTa, ngayTao FROM ChucVu ORDER BY " +
                "TRY_CAST(SUBSTRING(maCV, 3, 10) AS INT) ASC, maCV ASC";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ma = rs.getString("maCV");
                String ten = QuyenTruyCap.giaiMaMojibake(rs.getString("tenChucVu"));
                String moTa = QuyenTruyCap.giaiMaMojibake(rs.getString("moTa"));
                String ngay = rs.getString("ngayTao");
                ds.add(new ChucVu(ma, ten, moTa, ngay));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<String> layDanhSachTenChucVu() {
        List<String> list = new ArrayList<>();
        for (ChucVu cv : layDanhSachChucVu()) {
            if (cv.getTenChucVu() != null && !cv.getTenChucVu().isBlank()) {
                list.add(cv.getTenChucVu());
            }
        }
        return list;
    }

    public boolean themChucVu(ChucVu cv) throws SQLException {
        if (cv == null || cv.getTenChucVu() == null || cv.getTenChucVu().isBlank()) {
            throw new IllegalArgumentException("Tên chức vụ không được để trống!");
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new SQLException("Không có kết nối CSDL!");

        kiemTraVaKhoiTaoBangChucVu(con);

        String tenChuan = cv.getTenChucVu().trim();
        if (kiemTraTonTaiTen(con, tenChuan)) {
            throw new IllegalArgumentException("Tên chức vụ \"" + tenChuan + "\" đã tồn tại trong danh mục!");
        }

        String ma = cv.getMaCV();
        if (ma == null || ma.isBlank()) {
            ma = sinhMaChucVuTuDong();
            cv.setMaCV(ma);
        } else {
            ma = ma.trim().toUpperCase();
            if (kiemTraTonTaiMa(con, ma)) {
                throw new IllegalArgumentException("Mã chức vụ \"" + ma + "\" đã tồn tại!");
            }
        }

        String sql = "INSERT INTO ChucVu (maCV, tenChucVu, moTa, ngayTao) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            ps.setString(2, tenChuan);
            ps.setString(3, cv.getMoTa() != null ? cv.getMoTa().trim() : "");
            ps.setString(4, cv.getNgayTao() != null && !cv.getNgayTao().isBlank() ? cv.getNgayTao() : LocalDate.now().toString());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhatChucVu(ChucVu cv, String tenCu) throws SQLException {
        if (cv == null || cv.getMaCV() == null || cv.getMaCV().isBlank()) {
            throw new IllegalArgumentException("Mã chức vụ không hợp lệ!");
        }
        if (cv.getTenChucVu() == null || cv.getTenChucVu().isBlank()) {
            throw new IllegalArgumentException("Tên chức vụ không được để trống!");
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new SQLException("Không có kết nối CSDL!");

        kiemTraVaKhoiTaoBangChucVu(con);

        String tenMoi = cv.getTenChucVu().trim();
        // Kiểm tra nếu đổi tên sang tên khác đã tồn tại
        if (tenCu != null && !tenCu.trim().equalsIgnoreCase(tenMoi)) {
            if (kiemTraTonTaiTen(con, tenMoi)) {
                throw new IllegalArgumentException("Tên chức vụ mới \"" + tenMoi + "\" đã trùng với chức vụ khác!");
            }
        }

        String sql = "UPDATE ChucVu SET tenChucVu = ?, moTa = ? WHERE maCV = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenMoi);
            ps.setString(2, cv.getMoTa() != null ? cv.getMoTa().trim() : "");
            ps.setString(3, cv.getMaCV().trim());
            int n = ps.executeUpdate();

            // Nếu đổi tên thành công, đồng bộ cập nhật vào bảng NhanVien cho những nhân viên đang giữ chức vụ cũ
            if (n > 0 && tenCu != null && !tenCu.trim().equalsIgnoreCase(tenMoi)) {
                try (PreparedStatement psNV = con.prepareStatement("UPDATE NhanVien SET chucVu = ? WHERE chucVu = ?")) {
                    psNV.setString(1, tenMoi);
                    psNV.setString(2, tenCu.trim());
                    psNV.executeUpdate();
                }
            }
            return n > 0;
        }
    }

    public boolean xoaChucVu(String maCV) throws SQLException {
        if (maCV == null || maCV.isBlank()) {
            throw new IllegalArgumentException("Mã chức vụ cần xóa không hợp lệ!");
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) throw new SQLException("Không có kết nối CSDL!");

        kiemTraVaKhoiTaoBangChucVu(con);

        String ma = maCV.trim();
        String ten = "";
        try (PreparedStatement ps = con.prepareStatement("SELECT tenChucVu FROM ChucVu WHERE maCV = ?")) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ten = rs.getString(1);
                } else {
                    throw new IllegalArgumentException("Không tìm thấy chức vụ có mã: " + ma);
                }
            }
        }

        // Kiểm tra xem có nhân viên nào đang giữ chức vụ này không
        int soNV = demSoNhanVienTheoTenChucVu(ten);
        if (soNV > 0) {
            throw new IllegalStateException("Không thể xóa chức vụ [" + ten + "] vì hiện có " + soNV + " nhân viên đang giữ chức vụ này!\n" +
                    "Vui lòng điều chuyển chức vụ của các nhân viên trước khi xóa.");
        }

        try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChucVu WHERE maCV = ?")) {
            ps.setString(1, ma);
            return ps.executeUpdate() > 0;
        }
    }

    public int demSoNhanVienTheoTenChucVu(String tenChucVu) {
        if (tenChucVu == null || tenChucVu.isBlank()) return 0;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;

        String sql = "SELECT COUNT(*) FROM NhanVien WHERE chucVu = ? OR chucVu = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenChucVu.trim());
            ps.setString(2, QuyenTruyCap.giaiMaMojibake(tenChucVu.trim()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String sinhMaChucVuTuDong() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return "CV01";
        return sinhMaMoi(con);
    }

    private static String sinhMaMoi(Connection con) {
        String sql = "SELECT MAX(TRY_CAST(SUBSTRING(maCV, 3, 10) AS INT)) FROM ChucVu WHERE maCV LIKE 'CV%'";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return String.format("CV%02d", max + 1);
            }
        } catch (SQLException ignored) {}
        return "CV01";
    }

    private static boolean kiemTraTonTaiTen(Connection con, String ten) {
        if (con == null || ten == null) return false;
        String sql = "SELECT 1 FROM ChucVu WHERE LOWER(tenChucVu) = LOWER(?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ten.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ignored) {}
        return false;
    }

    private static boolean kiemTraTonTaiMa(Connection con, String ma) {
        if (con == null || ma == null) return false;
        String sql = "SELECT 1 FROM ChucVu WHERE UPPER(maCV) = UPPER(?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ignored) {}
        return false;
    }
}
