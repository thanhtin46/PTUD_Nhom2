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

        String sql = "SELECT nv.maNV, nv.hoTen, nv.chucVu, " +
                     "ISNULL(tk.taiKhoan, nv.maNV) AS taiKhoan, " +
                     "tk.matKhau, tk.trangThai " +
                     "FROM NhanVien nv " +
                     "LEFT JOIN TaiKhoan tk ON nv.maNV = tk.maNV";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String maNV = rs.getString("maNV");
                String hoTen = rs.getString("hoTen");
                String vaiTro = rs.getString("chucVu");
                String taiKhoan = rs.getString("taiKhoan");
                String matKhau = rs.getString("matKhau");
                String trangThaiSql = rs.getString("trangThai");

                // Ánh xạ trạng thái SQL -> NhanVien ("HOAT_DONG" -> DANG_LAM, "KHOA" -> NGUNG)
                String trangThai = "HOAT_DONG".equalsIgnoreCase(trangThaiSql) ? NhanVien.DANG_LAM : NhanVien.NGUNG;

                NhanVien nv = new NhanVien(maNV, hoTen, taiKhoan, matKhau, vaiTro, trangThai);
                ds.add(nv);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đọc dữ liệu NhanVien từ SQL Server: " + e.getMessage());
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Kiểm tra thông tin đăng nhập trực tiếp từ CSDL
     */
    public NhanVien kiemTraDangNhap(String taiKhoan, String matKhau) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;

        String sql = "SELECT nv.maNV, nv.hoTen, nv.chucVu, tk.taiKhoan, tk.matKhau, tk.trangThai " +
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
                    String tk = rs.getString("taiKhoan");
                    String mk = rs.getString("matKhau");
                    String trangThaiSql = rs.getString("trangThai");
                    String trangThai = "HOAT_DONG".equalsIgnoreCase(trangThaiSql) ? NhanVien.DANG_LAM : NhanVien.NGUNG;

                    return new NhanVien(maNV, hoTen, tk, mk, vaiTro, trangThai);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        ConnectDB.getInstance().connect();
        NhanVien_DAO dao = new NhanVien_DAO();
        ArrayList<NhanVien> list = dao.layTatCaNhanVien();
        System.out.println("Tong so nhan vien trong SQL Server: " + list.size());
        for (NhanVien nv : list) {
            System.out.println("-> Ma: " + nv.getMaNV() + " | HoTen: " + nv.getHoTen() + " | TK: " + nv.getTaiKhoan() + " | MK: " + nv.getMatKhau() + " | ChucVu: " + nv.getVaiTro());
        }
    }
}
