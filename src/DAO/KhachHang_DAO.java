package DAO;

import ConnectDB.ConnectDB;
import Entity.KhachHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.sql.Types;
import java.util.ArrayList;

/**
 * Lớp DAO xử lý dữ liệu khách hàng với CSDL SQL Server:
 * - Phân nhóm khách hàng chuẩn hóa: chỉ gồm "Khách mới" và "Khách hàng thân thiết".
 * - Tự động nâng hạng lên "Khách hàng thân thiết" khi khách có từ 2 lần sửa chữa ở quán.
 * - CRUD: Lấy danh sách, Tìm kiếm, Thêm mới, Cập nhật, Xóa an toàn dữ liệu liên quan.
 * - Tự động dọn dẹp khóa ngoại cascade khi xóa khách hàng.
 */
public class KhachHang_DAO {

    /**
     * Đồng bộ và chuẩn hóa nhóm toàn bộ khách hàng trong CSDL:
     * - Khách hàng có từ 2 lần sửa chữa / tiếp nhận / hóa đơn tự động chuyển thành "Khách hàng thân thiết".
     * - Khách hàng còn lại nếu chứa giá trị cũ (Bảo dưỡng định kỳ, Khách VIP, v.v.) chuẩn hóa về "Khách mới".
     */
    public void dongBoNhomTatCaKhachHang() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;

        try (java.sql.Statement stmt = con.createStatement()) {
            // 1. Tự động chuyển về "Khách hàng thân thiết" khi có >= 2 lần sửa chữa
            stmt.executeUpdate(
                "UPDATE KhachHang SET ghiChu = N'" + KhachHang.NHOM_THAN_THIET + "' " +
                "WHERE maKH IN (" +
                "  SELECT maKH FROM (" +
                "    SELECT maKH, COUNT(DISTINCT maPTN) as cnt FROM PhieuTiepNhan GROUP BY maKH HAVING COUNT(DISTINCT maPTN) >= 2 " +
                "    UNION " +
                "    SELECT maKH, COUNT(DISTINCT maPSC) as cnt FROM PhieuSuaChua GROUP BY maKH HAVING COUNT(DISTINCT maPSC) >= 2 " +
                "    UNION " +
                "    SELECT maKH, COUNT(DISTINCT maHD) as cnt FROM HoaDon GROUP BY maKH HAVING COUNT(DISTINCT maHD) >= 2 " +
                "  ) t " +
                ")"
            );

            // 2. Chuẩn hóa tất cả các giá trị ghi chú rỗng hoặc giá trị cũ về "Khách mới"
            stmt.executeUpdate(
                "UPDATE KhachHang SET ghiChu = N'" + KhachHang.NHOM_MOI + "' " +
                "WHERE ghiChu IS NULL OR LTRIM(RTRIM(ghiChu)) = '' " +
                "   OR (ghiChu <> N'" + KhachHang.NHOM_THAN_THIET + "' AND ghiChu <> N'" + KhachHang.NHOM_MOI + "')"
            );
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.dongBoNhomTatCaKhachHang: " + e.getMessage());
        }
    }

    /**
     * Lấy số lần sửa chữa / tiếp nhận / hóa đơn của khách hàng tại gara
     */
    public int laySoLanSuaChua(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return 0;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;

        String sql = "SELECT " +
                     "  (SELECT COUNT(DISTINCT maPTN) FROM PhieuTiepNhan WHERE maKH = ?) AS soPTN, " +
                     "  (SELECT COUNT(DISTINCT maPSC) FROM PhieuSuaChua WHERE maKH = ?) AS soPSC, " +
                     "  (SELECT COUNT(DISTINCT maHD) FROM HoaDon WHERE maKH = ?) AS soHD";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maKH.trim());
            stmt.setString(2, maKH.trim());
            stmt.setString(3, maKH.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int ptn = rs.getInt("soPTN");
                    int psc = rs.getInt("soPSC");
                    int hd = rs.getInt("soHD");
                    return Math.max(ptn, Math.max(psc, hd));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.laySoLanSuaChua: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Tự động kiểm tra và chuyển nhóm khách hàng:
     * Nhóm "Khách mới" sẽ tự động chuyển về "Khách hàng thân thiết" khi có từ 2 lần sửa chữa ở quán.
     * @return true nếu nhóm khách hàng được nâng hạng thành công
     */
    public boolean tuDongCapNhatNhomKhachHang(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return false;
        int soLan = laySoLanSuaChua(maKH);
        if (soLan < 2) return false;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "UPDATE KhachHang SET ghiChu = ? WHERE maKH = ? AND (ghiChu IS NULL OR ghiChu <> ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, KhachHang.NHOM_THAN_THIET);
            stmt.setString(2, maKH.trim());
            stmt.setString(3, KhachHang.NHOM_THAN_THIET);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.tuDongCapNhatNhomKhachHang: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy toàn bộ danh sách khách hàng từ SQL Server sắp xếp theo mã tăng dần
     */
    public ArrayList<KhachHang> layTatCaKhachHang() {
        ArrayList<KhachHang> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        // Tự động đồng bộ chuẩn hóa nhóm khách hàng
        dongBoNhomTatCaKhachHang();

        String sql = "SELECT maKH, hoTen, soDT, chungMinh, " +
                     "ISNULL(diaChi, '') AS diaChi, " +
                     "ISNULL(email, '') AS email, " +
                     "ISNULL(ghiChu, '') AS ghiChu, " +
                     "ISNULL(ngayDangKy, '') AS ngayDangKy " +
                     "FROM KhachHang ORDER BY maKH ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String ma = rs.getString("maKH");
                String ten = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String cccd = rs.getString("chungMinh");
                String diaChi = rs.getString("diaChi");
                String email = rs.getString("email");
                String ghiChu = KhachHang.chuanHoaNhom(rs.getString("ghiChu"));
                Date dDangKy = rs.getDate("ngayDangKy");
                LocalDate ngayDangKy = dDangKy != null ? dDangKy.toLocalDate() : null;

                list.add(new KhachHang(ma, ten, sdt, cccd != null ? cccd : "", diaChi, email, ghiChu, ngayDangKy));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.layTatCaKhachHang: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm khách hàng theo mã khách hàng
     */
    public KhachHang timKhachHangTheoMa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return null;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;

        String sql = "SELECT maKH, hoTen, soDT, chungMinh, " +
                     "ISNULL(diaChi, '') AS diaChi, " +
                     "ISNULL(email, '') AS email, " +
                     "ISNULL(ghiChu, '') AS ghiChu, " +
                     "ISNULL(ngayDangKy, '') AS ngayDangKy " +
                     "FROM KhachHang WHERE maKH = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maKH.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String ma = rs.getString("maKH");
                    String ten = rs.getString("hoTen");
                    String sdt = rs.getString("soDT");
                    String cccd = rs.getString("chungMinh");
                    String diaChi = rs.getString("diaChi");
                    String email = rs.getString("email");
                    String ghiChu = KhachHang.chuanHoaNhom(rs.getString("ghiChu"));
                    Date dDangKy = rs.getDate("ngayDangKy");
                    LocalDate ngayDangKy = dDangKy != null ? dDangKy.toLocalDate() : null;

                    return new KhachHang(ma, ten, sdt, cccd != null ? cccd : "", diaChi, email, ghiChu, ngayDangKy);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.timKhachHangTheoMa: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm khách hàng theo số điện thoại (hỗ trợ cả định dạng gốc và chuỗi chỉ chứa số)
     */
    public KhachHang timKhachHangTheoSdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        String sdtChuan = sdt.trim();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;

        String sql = "SELECT maKH, hoTen, soDT, chungMinh, " +
                     "ISNULL(diaChi, '') AS diaChi, " +
                     "ISNULL(email, '') AS email, " +
                     "ISNULL(ghiChu, '') AS ghiChu, " +
                     "ISNULL(ngayDangKy, '') AS ngayDangKy " +
                     "FROM KhachHang WHERE soDT = ? OR REPLACE(REPLACE(soDT, ' ', ''), '.', '') = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, sdtChuan);
            stmt.setString(2, sdtChuan.replaceAll("[^0-9]", ""));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String ma = rs.getString("maKH");
                    String ten = rs.getString("hoTen");
                    String phone = rs.getString("soDT");
                    String cccd = rs.getString("chungMinh");
                    String diaChi = rs.getString("diaChi");
                    String email = rs.getString("email");
                    String ghiChu = KhachHang.chuanHoaNhom(rs.getString("ghiChu"));
                    Date dDangKy = rs.getDate("ngayDangKy");
                    LocalDate ngayDangKy = dDangKy != null ? dDangKy.toLocalDate() : null;

                    return new KhachHang(ma, ten, phone, cccd != null ? cccd : "", diaChi, email, ghiChu, ngayDangKy);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.timKhachHangTheoSdt: " + e.getMessage());
        }
        return null;
    }

    /**
     * Thêm mới một khách hàng vào CSDL SQL Server
     */
    public boolean themKhachHang(KhachHang kh) {
        if (kh == null || kh.getMaKH() == null || kh.getMaKH().trim().isEmpty()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO KhachHang (maKH, hoTen, soDT, chungMinh, diaChi, email, ghiChu, ngayDangKy) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getMaKH().trim());
            stmt.setString(2, kh.getHoTen() != null ? kh.getHoTen().trim() : "");
            stmt.setString(3, kh.getSdt() != null && kh.getSdt().trim().length() >= 9 ? kh.getSdt().trim() : "0900000000");

            String cccd = kh.getCccd();
            if (cccd != null && !cccd.trim().isEmpty()) {
                stmt.setString(4, cccd.trim());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.setString(5, kh.getDiaChi() != null ? kh.getDiaChi().trim() : "");
            stmt.setString(6, kh.getEmail() != null ? kh.getEmail().trim() : "");
            stmt.setString(7, KhachHang.chuanHoaNhom(kh.getGhiChu()));
            stmt.setDate(8, kh.getNgayDangKy() != null ? Date.valueOf(kh.getNgayDangKy()) : null);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.themKhachHang: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin khách hàng trong CSDL SQL Server
     */
    public boolean capNhatKhachHang(KhachHang kh) {
        if (kh == null || kh.getMaKH() == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        // Quy tắc: Nếu khách hàng đã có từ 2 lần sửa chữa, tự động chuyển/giữ ở "Khách hàng thân thiết"
        String nhomKH = KhachHang.chuanHoaNhom(kh.getGhiChu());
        if (laySoLanSuaChua(kh.getMaKH()) >= 2) {
            nhomKH = KhachHang.NHOM_THAN_THIET;
        }

        String sql = "UPDATE KhachHang SET hoTen = ?, soDT = ?, chungMinh = ?, diaChi = ?, email = ?, ghiChu = ?, ngayDangKy = ? " +
                     "WHERE maKH = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getHoTen() != null ? kh.getHoTen().trim() : "");
            stmt.setString(2, kh.getSdt() != null && kh.getSdt().trim().length() >= 9 ? kh.getSdt().trim() : "0900000000");

            String cccd = kh.getCccd();
            if (cccd != null && !cccd.trim().isEmpty()) {
                stmt.setString(3, cccd.trim());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            stmt.setString(4, kh.getDiaChi() != null ? kh.getDiaChi().trim() : "");
            stmt.setString(5, kh.getEmail() != null ? kh.getEmail().trim() : "");
            stmt.setString(6, nhomKH);
            stmt.setDate(7, kh.getNgayDangKy() != null ? Date.valueOf(kh.getNgayDangKy()) : null);
            stmt.setString(8, kh.getMaKH().trim());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.capNhatKhachHang: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa khách hàng và tự động dọn dẹp an toàn các dữ liệu liên quan (Cascade an toàn)
     */
    public boolean xoaKhachHang(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        boolean autoCommitGoc = true;
        try {
            autoCommitGoc = con.getAutoCommit();
            con.setAutoCommit(false);

            // 1. Xóa ChiTietHoaDon liên quan đến các hóa đơn của khách hàng
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ChiTietHoaDon WHERE maHD IN (SELECT maHD FROM HoaDon WHERE maKH = ?)")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }

            // 2. Xóa HoaDon của khách hàng
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM HoaDon WHERE maKH = ?")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }

            // 3. Xóa ChiTietPhieuBaoHanh (theo maKH hoặc theo soVIN của các xe thuộc maKH)
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ChiTietPhieuBaoHanh WHERE maPBH IN (" +
                    "  SELECT maPBH FROM PhieuBaoHanh WHERE maKH = ? OR soVIN IN (SELECT soVIN FROM PhuongTien WHERE maKH = ?)" +
                    ")")) {
                ps.setString(1, maKH);
                ps.setString(2, maKH);
                ps.executeUpdate();
            }

            // 4. Xóa PhieuBaoHanh
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM PhieuBaoHanh WHERE maKH = ? OR soVIN IN (SELECT soVIN FROM PhuongTien WHERE maKH = ?)")) {
                ps.setString(1, maKH);
                ps.setString(2, maKH);
                ps.executeUpdate();
            }

            // 5. Xóa ChiTietSuaChua (theo maKH hoặc theo soVIN của các xe thuộc maKH)
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ChiTietSuaChua WHERE maPSC IN (" +
                    "  SELECT maPSC FROM PhieuSuaChua WHERE maKH = ? OR soVIN IN (SELECT soVIN FROM PhuongTien WHERE maKH = ?)" +
                    ")")) {
                ps.setString(1, maKH);
                ps.setString(2, maKH);
                ps.executeUpdate();
            }

            // 6. Xóa PhieuSuaChua
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM PhieuSuaChua WHERE maKH = ? OR soVIN IN (SELECT soVIN FROM PhuongTien WHERE maKH = ?)")) {
                ps.setString(1, maKH);
                ps.setString(2, maKH);
                ps.executeUpdate();
            }

            // 7. Xóa PhieuTiepNhan
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM PhieuTiepNhan WHERE maKH = ? OR soVIN IN (SELECT soVIN FROM PhuongTien WHERE maKH = ?)")) {
                ps.setString(1, maKH);
                ps.setString(2, maKH);
                ps.executeUpdate();
            }

            // 8. Xóa PhuongTien của khách hàng
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM PhuongTien WHERE maKH = ?")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }

            // 9. Xóa KhachHang
            int rows;
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM KhachHang WHERE maKH = ?")) {
                ps.setString(1, maKH);
                rows = ps.executeUpdate();
            }

            con.commit();
            return rows > 0;
        } catch (SQLException ex) {
            try {
                con.rollback();
            } catch (SQLException ignored) {}
            System.err.println("Lỗi KhachHang_DAO.xoaKhachHang: " + ex.getMessage());
            return false;
        } finally {
            try {
                con.setAutoCommit(autoCommitGoc);
            } catch (SQLException ignored) {}
        }
    }

    /**
     * Kiểm tra mã khách hàng đã tồn tại trong CSDL hay chưa
     */
    public boolean kiemTraTrungMa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "SELECT 1 FROM KhachHang WHERE maKH = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maKH.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.kiemTraTrungMa: " + e.getMessage());
        }
        return false;
    }

    /**
     * Kiểm tra số điện thoại đã tồn tại cho khách hàng khác hay chưa
     * @param sdt Số điện thoại cần kiểm tra
     * @param maKHBoQua Mã khách hàng hiện tại (khi sửa) để bỏ qua chính nó, hoặc null khi thêm mới
     */
    public boolean kiemTraTrungSdt(String sdt, String maKHBoQua) {
        if (sdt == null || sdt.trim().isEmpty()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "SELECT 1 FROM KhachHang WHERE soDT = ?" + (maKHBoQua != null ? " AND maKH <> ?" : "");
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, sdt.trim());
            if (maKHBoQua != null) {
                stmt.setString(2, maKHBoQua.trim());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.kiemTraTrungSdt: " + e.getMessage());
        }
        return false;
    }

    /**
     * Tự động sinh mã khách hàng mới tiếp theo (VD: KH001, KH002... KH010)
     */
    public String sinhMaKhachHangTuDong() {
        Connection con = ConnectDB.getInstance().getConnection();
        int maxSo = 0;

        if (con != null) {
            String sql = "SELECT maKH FROM KhachHang WHERE maKH LIKE 'KH%'";
            try (PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maKH");
                    if (ma != null) {
                        try {
                            int so = Integer.parseInt(ma.replaceAll("[^0-9]", ""));
                            if (so > maxSo) maxSo = so;
                        } catch (Exception ignored) {}
                    }
                }
            } catch (SQLException e) {
                System.err.println("Lỗi KhachHang_DAO.sinhMaKhachHangTuDong: " + e.getMessage());
            }
        }

        // Định dạng chuẩn đồng bộ KH001, KH002... KH100...
        return String.format("KH%03d", maxSo + 1);
    }

    /**
     * Đếm tổng số khách hàng hiện có trong hệ thống
     */
    public int layTongSoKhachHang() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return 0;

        String sql = "SELECT COUNT(*) FROM KhachHang";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Lỗi KhachHang_DAO.layTongSoKhachHang: " + e.getMessage());
        }
        return 0;
    }
}
