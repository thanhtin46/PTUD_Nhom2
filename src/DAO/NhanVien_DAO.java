package DAO;

import ConnectDB.ConnectDB;
import Entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class NhanVien_DAO {

    /**
     * Lấy toàn bộ danh sách nhân viên và tài khoản từ SQL Server
     */
    public ArrayList<NhanVien> layTatCaNhanVien() {
        ArrayList<NhanVien> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return ds;

        String sql = "SELECT nv.maNV, nv.hoTen, nv.chucVu, nv.soDT, nv.email, nv.bacNghe, nv.boPhan, nv.ngayVaoLam, nv.trangThai AS trangThaiNV, " +
                     "ISNULL(tk.taiKhoan, nv.maNV) AS taiKhoan, " +
                     "tk.matKhau, tk.trangThai AS trangThaiTK " +
                     "FROM NhanVien nv " +
                     "LEFT JOIN TaiKhoan tk ON nv.maNV = tk.maNV " +
                     "ORDER BY nv.maNV ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String maNV = rs.getString("maNV");
                String hoTen = rs.getString("hoTen");
                String vaiTro = rs.getString("chucVu");
                String soDT = rs.getString("soDT");
                String email = rs.getString("email");
                String bacNghe = rs.getString("bacNghe");
                String boPhan = rs.getString("boPhan");
                String ngayVaoLam = rs.getString("ngayVaoLam");
                String trangThaiNV = rs.getString("trangThaiNV");
                String taiKhoan = rs.getString("taiKhoan");
                String matKhau = rs.getString("matKhau");
                String trangThaiTK = rs.getString("trangThaiTK");

                // Xác định trạng thái làm việc
                String trangThai = NhanVien.DANG_LAM;
                if (trangThaiNV != null && !trangThaiNV.trim().isEmpty()) {
                    trangThai = trangThaiNV.trim();
                } else if ("KHOA".equalsIgnoreCase(trangThaiTK)) {
                    trangThai = NhanVien.NGUNG;
                }

                NhanVien nv = new NhanVien(maNV, hoTen, taiKhoan, matKhau != null ? matKhau : "123",
                        vaiTro, trangThai, null, soDT, email, bacNghe, boPhan, ngayVaoLam);
                ds.add(nv);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đọc dữ liệu NhanVien từ SQL Server: " + e.getMessage());
        }
        return ds;
    }

    /**
     * Kiểm tra thông tin đăng nhập trực tiếp từ CSDL
     */
    public NhanVien kiemTraDangNhap(String taiKhoan, String matKhau) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;

        String sql = "SELECT nv.maNV, nv.hoTen, nv.chucVu, nv.soDT, nv.email, nv.bacNghe, nv.boPhan, nv.ngayVaoLam, nv.trangThai AS trangThaiNV, " +
                     "tk.taiKhoan, tk.matKhau, tk.trangThai AS trangThaiTK " +
                     "FROM NhanVien nv " +
                     "JOIN TaiKhoan tk ON nv.maNV = tk.maNV " +
                     "WHERE (tk.taiKhoan = ? OR nv.maNV = ?) AND tk.matKhau = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, taiKhoan);
            stmt.setString(2, taiKhoan);
            stmt.setString(3, matKhau);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String maNV = rs.getString("maNV");
                    String hoTen = rs.getString("hoTen");
                    String vaiTro = rs.getString("chucVu");
                    String soDT = rs.getString("soDT");
                    String email = rs.getString("email");
                    String bacNghe = rs.getString("bacNghe");
                    String boPhan = rs.getString("boPhan");
                    String ngayVaoLam = rs.getString("ngayVaoLam");
                    String trangThaiNV = rs.getString("trangThaiNV");
                    String tk = rs.getString("taiKhoan");
                    String mk = rs.getString("matKhau");
                    String trangThaiTK = rs.getString("trangThaiTK");

                    String trangThai = NhanVien.DANG_LAM;
                    if (trangThaiNV != null && !trangThaiNV.trim().isEmpty()) {
                        trangThai = trangThaiNV.trim();
                    } else if ("KHOA".equalsIgnoreCase(trangThaiTK)) {
                        trangThai = NhanVien.NGUNG;
                    }

                    return new NhanVien(maNV, hoTen, tk, mk, vaiTro, trangThai, null, soDT, email, bacNghe, boPhan, ngayVaoLam);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiemTraDangNhap: " + e.getMessage());
        }
        return null;
    }

    /**
     * Thêm nhân viên và tài khoản mới vào CSDL
     */
    public boolean themNhanVien(NhanVien nv, String sdt) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String phone = (nv.getSoDT() != null && !nv.getSoDT().trim().isEmpty()) ? nv.getSoDT().trim() :
                       ((sdt != null && sdt.trim().length() >= 9) ? sdt.trim() : ("09" + (int)(Math.random() * 89999999 + 10000000)));
        String trangThaiNV = nv.getTrangThai() != null ? nv.getTrangThai() : NhanVien.DANG_LAM;
        String trangThaiSql = NhanVien.NGUNG.equalsIgnoreCase(trangThaiNV) ? "KHOA" : "HOAT_DONG";

        try {
            // 1. Insert NhanVien
            String sqlNV = "INSERT INTO NhanVien (maNV, hoTen, chucVu, soDT, email, bacNghe, boPhan, ngayVaoLam, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlNV)) {
                stmt.setString(1, nv.getMaNV());
                stmt.setString(2, nv.getHoTen());
                stmt.setString(3, nv.getVaiTro());
                stmt.setString(4, phone);
                stmt.setString(5, nv.getEmail());
                stmt.setString(6, nv.getBacNghe());
                stmt.setString(7, nv.getBoPhan());
                stmt.setString(8, nv.getNgayVaoLam());
                stmt.setString(9, trangThaiNV);
                stmt.executeUpdate();
            }

            // 2. Insert TaiKhoan
            String sqlTK = "INSERT INTO TaiKhoan (maNV, taiKhoan, matKhau, trangThai) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlTK)) {
                stmt.setString(1, nv.getMaNV());
                stmt.setString(2, nv.getTaiKhoan() != null && !nv.getTaiKhoan().trim().isEmpty() ? nv.getTaiKhoan().trim() : nv.getMaNV());
                stmt.setString(3, nv.getMatKhau() != null && !nv.getMatKhau().isEmpty() ? nv.getMatKhau() : "123");
                stmt.setString(4, trangThaiSql);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi themNhanVien: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin nhân viên và tài khoản
     */
    public boolean capNhatNhanVien(NhanVien nv) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String trangThaiNV = nv.getTrangThai() != null ? nv.getTrangThai() : NhanVien.DANG_LAM;
        String trangThaiSql = NhanVien.NGUNG.equalsIgnoreCase(trangThaiNV) ? "KHOA" : "HOAT_DONG";

        try {
            // Update NhanVien
            String sqlNV = "UPDATE NhanVien SET hoTen = ?, chucVu = ?, soDT = ?, email = ?, bacNghe = ?, boPhan = ?, ngayVaoLam = ?, trangThai = ? WHERE maNV = ?";
            try (PreparedStatement stmt = con.prepareStatement(sqlNV)) {
                stmt.setString(1, nv.getHoTen());
                stmt.setString(2, nv.getVaiTro());
                stmt.setString(3, nv.getSoDT());
                stmt.setString(4, nv.getEmail());
                stmt.setString(5, nv.getBacNghe());
                stmt.setString(6, nv.getBoPhan());
                stmt.setString(7, nv.getNgayVaoLam());
                stmt.setString(8, trangThaiNV);
                stmt.setString(9, nv.getMaNV());
                stmt.executeUpdate();
            }

            // Update TaiKhoan
            String sqlTK = "UPDATE TaiKhoan SET taiKhoan = ?, matKhau = ?, trangThai = ? WHERE maNV = ?";
            try (PreparedStatement stmt = con.prepareStatement(sqlTK)) {
                stmt.setString(1, nv.getTaiKhoan());
                stmt.setString(2, nv.getMatKhau());
                stmt.setString(3, trangThaiSql);
                stmt.setString(4, nv.getMaNV());
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    // Nếu chưa có tài khoản thì tạo mới
                    try (PreparedStatement ins = con.prepareStatement(
                            "INSERT INTO TaiKhoan (maNV, taiKhoan, matKhau, trangThai) VALUES (?, ?, ?, ?)")) {
                        ins.setString(1, nv.getMaNV());
                        ins.setString(2, nv.getTaiKhoan());
                        ins.setString(3, nv.getMatKhau() != null ? nv.getMatKhau() : "123");
                        ins.setString(4, trangThaiSql);
                        ins.executeUpdate();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatNhanVien: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật trạng thái nhân viên và tài khoản
     */
    public boolean capNhatTrangThaiTaiKhoan(String maNV, String trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String trangThaiSql = NhanVien.NGUNG.equalsIgnoreCase(trangThaiMoi) ? "KHOA" : "HOAT_DONG";
        try {
            String sqlTK = "UPDATE TaiKhoan SET trangThai = ? WHERE maNV = ?";
            try (PreparedStatement stmt = con.prepareStatement(sqlTK)) {
                stmt.setString(1, trangThaiSql);
                stmt.setString(2, maNV);
                stmt.executeUpdate();
            }
            String sqlNV = "UPDATE NhanVien SET trangThai = ? WHERE maNV = ?";
            try (PreparedStatement stmt = con.prepareStatement(sqlNV)) {
                stmt.setString(1, trangThaiMoi);
                stmt.setString(2, maNV);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTrangThaiTaiKhoan: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa an toàn nhân viên: Thử xóa cứng nếu chưa có giao dịch, nếu có FK thì chuyển sang Thôi việc (Soft-delete)
     */
    public boolean xoaNhanVien(String maNV) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            try (PreparedStatement stmtTK = con.prepareStatement("DELETE FROM TaiKhoan WHERE maNV = ?")) {
                stmtTK.setString(1, maNV);
                stmtTK.executeUpdate();
            }
            try (PreparedStatement stmtNV = con.prepareStatement("DELETE FROM NhanVien WHERE maNV = ?")) {
                stmtNV.setString(1, maNV);
                return stmtNV.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            // Có ràng buộc khóa ngoại (lịch sử tiếp nhận, hóa đơn...) -> Chuyển sang thôi việc
            return capNhatTrangThaiTaiKhoan(maNV, NhanVien.NGUNG);
        }
    }

    /**
     * Đổi mật khẩu tài khoản nhân viên
     */
    public boolean doiMatKhau(String maNV, String matKhauMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maNV = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, matKhauMoi);
            stmt.setString(2, maNV);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi doiMatKhau: " + e.getMessage());
            return false;
        }
    }

<<<<<<< HEAD
    public static void main(String[] args) throws SQLException {
=======
    public static void main(String[] args) {
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
        ConnectDB.getInstance().connect();
        NhanVien_DAO dao = new NhanVien_DAO();
        ArrayList<NhanVien> list = dao.layTatCaNhanVien();
        System.out.println("Tong so nhan vien trong SQL Server: " + list.size());
        for (NhanVien nv : list) {
            System.out.println("-> Ma: " + nv.getMaNV() + " | HoTen: " + nv.getHoTen() + " | TK: " + nv.getTaiKhoan() + " | MK: " + nv.getMatKhau() + " | ChucVu: " + nv.getVaiTro());
        }
    }
}
