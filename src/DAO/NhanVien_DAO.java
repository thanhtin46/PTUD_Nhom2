package DAO;

import ConnectDB.ConnectDB;
import Entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class NhanVien_DAO {

    private static boolean daKiemTraCauTruc = false;

    /**
     * Tự động kiểm tra và đảm bảo bảng NhanVien trong CSDL SQL Server có đủ các cột mở rộng.
     * Giúp hệ thống hoạt động trơn tru dù người dùng chạy trên bản CSDL cũ hay mới.
     */
    public synchronized void damBaoCauTrucBang() {
        if (daKiemTraCauTruc) return;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;

        try {
            String sql = "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('NhanVien') AND name = 'email') " +
                         "    ALTER TABLE NhanVien ADD email VARCHAR(100); " +
                         "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('NhanVien') AND name = 'bacNghe') " +
                         "    ALTER TABLE NhanVien ADD bacNghe NVARCHAR(50); " +
                         "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('NhanVien') AND name = 'boPhan') " +
                         "    ALTER TABLE NhanVien ADD boPhan NVARCHAR(50); " +
                         "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('NhanVien') AND name = 'ngayVaoLam') " +
                         "    ALTER TABLE NhanVien ADD ngayVaoLam VARCHAR(20); " +
                         "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('NhanVien') AND name = 'trangThai') " +
                         "    ALTER TABLE NhanVien ADD trangThai NVARCHAR(30) DEFAULT N'Đang làm việc'; ";
            try (Statement stmt = con.createStatement()) {
                stmt.execute(sql);
            }
            daKiemTraCauTruc = true;
        } catch (SQLException ignored) {
            // Nếu không có quyền cập nhật bảng, giữ nguyên truy vấn cơ bản
        }
    }

    /**
     * Lấy toàn bộ danh sách nhân viên và tài khoản từ SQL Server
     */
    public ArrayList<NhanVien> layTatCaNhanVien() {
        damBaoCauTrucBang();
        ArrayList<NhanVien> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return ds;

        // 1. Thử truy vấn đầy đủ với các cột mở rộng
        String sqlDayDu = "SELECT nv.maNV, nv.hoTen, nv.chucVu, nv.soDT, nv.email, nv.bacNghe, nv.boPhan, nv.ngayVaoLam, nv.trangThai AS trangThaiNV, " +
                          "ISNULL(tk.taiKhoan, nv.maNV) AS taiKhoan, " +
                          "tk.matKhau, tk.trangThai AS trangThaiTK " +
                          "FROM NhanVien nv " +
                          "LEFT JOIN TaiKhoan tk ON nv.maNV = tk.maNV " +
                          "ORDER BY nv.maNV ASC";

        try (PreparedStatement stmt = con.prepareStatement(sqlDayDu);
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

                // Xác định trạng thái làm việc chuẩn hóa
                String trangThai = NhanVien.DANG_LAM;
                if ("KHOA".equalsIgnoreCase(trangThaiTK) || NhanVien.NGUNG.equalsIgnoreCase(trangThaiNV) ||
                    (trangThaiNV != null && (trangThaiNV.toLowerCase().contains("thôi") || trangThaiNV.toLowerCase().contains("ngừng")))) {
                    trangThai = NhanVien.NGUNG;
                } else {
                    trangThai = NhanVien.DANG_LAM;
                }

                NhanVien nv = new NhanVien(maNV, hoTen, taiKhoan, matKhau != null ? matKhau : "123",
                        vaiTro, trangThai, null, soDT, email, bacNghe, boPhan, ngayVaoLam);
                ds.add(nv);
            }
            return ds;
        } catch (SQLException e) {
            // Dự phòng nếu bảng NhanVien chỉ có các cột cơ bản
            String sqlCoBan = "SELECT nv.maNV, nv.hoTen, nv.chucVu, nv.soDT, " +
                              "ISNULL(tk.taiKhoan, nv.maNV) AS taiKhoan, " +
                              "tk.matKhau, tk.trangThai AS trangThaiTK " +
                              "FROM NhanVien nv " +
                              "LEFT JOIN TaiKhoan tk ON nv.maNV = tk.maNV " +
                              "ORDER BY nv.maNV ASC";
            try (PreparedStatement stmt2 = con.prepareStatement(sqlCoBan);
                 ResultSet rs2 = stmt2.executeQuery()) {
                while (rs2.next()) {
                    String maNV = rs2.getString("maNV");
                    String hoTen = rs2.getString("hoTen");
                    String vaiTro = rs2.getString("chucVu");
                    String soDT = rs2.getString("soDT");
                    String taiKhoan = rs2.getString("taiKhoan");
                    String matKhau = rs2.getString("matKhau");
                    String trangThaiTK = rs2.getString("trangThaiTK");

                    String trangThai = "KHOA".equalsIgnoreCase(trangThaiTK) ? NhanVien.NGUNG : NhanVien.DANG_LAM;
                    NhanVien nv = new NhanVien(maNV, hoTen, taiKhoan, matKhau != null ? matKhau : "123",
                            vaiTro, trangThai, null, soDT, null, null, null, null);
                    ds.add(nv);
                }
            } catch (SQLException ex) {
                System.err.println("Lỗi khi đọc dữ liệu NhanVien từ SQL Server: " + ex.getMessage());
            }
        }
        return ds;
    }

    /**
     * Kiểm tra thông tin đăng nhập trực tiếp từ CSDL
     */
    public NhanVien kiemTraDangNhap(String taiKhoan, String matKhau) {
        damBaoCauTrucBang();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;

        // 1. Thử truy vấn đầy đủ với các cột mở rộng
        String sqlDayDu = "SELECT nv.maNV, nv.hoTen, nv.chucVu, nv.soDT, nv.email, nv.bacNghe, nv.boPhan, nv.ngayVaoLam, nv.trangThai AS trangThaiNV, " +
                          "tk.taiKhoan, tk.matKhau, tk.trangThai AS trangThaiTK " +
                          "FROM NhanVien nv " +
                          "JOIN TaiKhoan tk ON nv.maNV = tk.maNV " +
                          "WHERE (tk.taiKhoan = ? OR nv.maNV = ?) AND tk.matKhau = ?";

        try (PreparedStatement stmt = con.prepareStatement(sqlDayDu)) {
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
                    if ("KHOA".equalsIgnoreCase(trangThaiTK) || NhanVien.NGUNG.equalsIgnoreCase(trangThaiNV) ||
                        (trangThaiNV != null && (trangThaiNV.toLowerCase().contains("thôi") || trangThaiNV.toLowerCase().contains("ngừng")))) {
                        trangThai = NhanVien.NGUNG;
                    } else {
                        trangThai = NhanVien.DANG_LAM;
                    }

                    return new NhanVien(maNV, hoTen, tk, mk, vaiTro, trangThai, null, soDT, email, bacNghe, boPhan, ngayVaoLam);
                }
            }
        } catch (SQLException e) {
            // 2. Dự phòng an toàn nếu CSDL chỉ có các cột cơ bản
            String sqlCoBan = "SELECT nv.maNV, nv.hoTen, nv.chucVu, nv.soDT, " +
                              "tk.taiKhoan, tk.matKhau, tk.trangThai AS trangThaiTK " +
                              "FROM NhanVien nv " +
                              "JOIN TaiKhoan tk ON nv.maNV = tk.maNV " +
                              "WHERE (tk.taiKhoan = ? OR nv.maNV = ?) AND tk.matKhau = ?";
            try (PreparedStatement stmt2 = con.prepareStatement(sqlCoBan)) {
                stmt2.setString(1, taiKhoan);
                stmt2.setString(2, taiKhoan);
                stmt2.setString(3, matKhau);

                try (ResultSet rs2 = stmt2.executeQuery()) {
                    if (rs2.next()) {
                        String maNV = rs2.getString("maNV");
                        String hoTen = rs2.getString("hoTen");
                        String vaiTro = rs2.getString("chucVu");
                        String soDT = rs2.getString("soDT");
                        String tk = rs2.getString("taiKhoan");
                        String mk = rs2.getString("matKhau");
                        String trangThaiTK = rs2.getString("trangThaiTK");

                        String trangThai = "KHOA".equalsIgnoreCase(trangThaiTK) ? NhanVien.NGUNG : NhanVien.DANG_LAM;
                        return new NhanVien(maNV, hoTen, tk, mk, vaiTro, trangThai, null, soDT, null, null, null, null);
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Lỗi kiemTraDangNhap: " + ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Thêm nhân viên và tài khoản mới vào CSDL
     */
    private boolean themNhanVienNoiBo(NhanVien nv, String sdt) {
        damBaoCauTrucBang();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String phone = (nv.getSoDT() != null && !nv.getSoDT().trim().isEmpty()) ? nv.getSoDT().trim() :
                       ((sdt != null && sdt.trim().length() >= 9) ? sdt.trim() : ("09" + (int)(Math.random() * 89999999 + 10000000)));
        String trangThaiNV = nv.getTrangThai() != null ? nv.getTrangThai() : NhanVien.DANG_LAM;
        String trangThaiSql = NhanVien.NGUNG.equalsIgnoreCase(trangThaiNV) ? "KHOA" : "HOAT_DONG";

        try {
            // 1. Thử insert NhanVien với đầy đủ cột
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
            } catch (SQLException exCol) {
                // Dự phòng nếu bảng chỉ có 4 cột cơ bản
                String sqlNVCoBan = "INSERT INTO NhanVien (maNV, hoTen, chucVu, soDT) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmtCoBan = con.prepareStatement(sqlNVCoBan)) {
                    stmtCoBan.setString(1, nv.getMaNV());
                    stmtCoBan.setString(2, nv.getHoTen());
                    stmtCoBan.setString(3, nv.getVaiTro());
                    stmtCoBan.setString(4, phone);
                    stmtCoBan.executeUpdate();
                }
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
    private boolean capNhatNhanVienNoiBo(NhanVien nv) {
        damBaoCauTrucBang();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String trangThaiNV = nv.getTrangThai() != null ? nv.getTrangThai() : NhanVien.DANG_LAM;
        String trangThaiSql = NhanVien.NGUNG.equalsIgnoreCase(trangThaiNV) ? "KHOA" : "HOAT_DONG";

        try {
            // 1. Thử update NhanVien đầy đủ
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
            } catch (SQLException exCol) {
                // Dự phòng update cơ bản nếu thiếu cột
                String sqlNVCoBan = "UPDATE NhanVien SET hoTen = ?, chucVu = ?, soDT = ? WHERE maNV = ?";
                try (PreparedStatement stmtCoBan = con.prepareStatement(sqlNVCoBan)) {
                    stmtCoBan.setString(1, nv.getHoTen());
                    stmtCoBan.setString(2, nv.getVaiTro());
                    stmtCoBan.setString(3, nv.getSoDT());
                    stmtCoBan.setString(4, nv.getMaNV());
                    stmtCoBan.executeUpdate();
                }
            }

            // 2. Update TaiKhoan
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
    private boolean capNhatTrangThaiTaiKhoanNoiBo(String maNV, String trangThaiMoi) {
        damBaoCauTrucBang();
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
            try {
                String sqlNV = "UPDATE NhanVien SET trangThai = ? WHERE maNV = ?";
                try (PreparedStatement stmt = con.prepareStatement(sqlNV)) {
                    stmt.setString(1, trangThaiMoi);
                    stmt.setString(2, maNV);
                    stmt.executeUpdate();
                }
            } catch (SQLException ignored) {
                // Bảng NhanVien cũ không có cột trangThai thì bỏ qua
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
        return capNhatTrangThaiTaiKhoan(maNV, NhanVien.NGUNG);
    }

    /**
     * Đổi mật khẩu tài khoản nhân viên
     */
    private boolean doiMatKhauNoiBo(String maNV, String matKhauMoi) {
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

    public static void main(String[] args) {
        ConnectDB.getInstance().connect();
        NhanVien_DAO dao = new NhanVien_DAO();
        ArrayList<NhanVien> list = dao.layTatCaNhanVien();
        System.out.println("Tong so nhan vien trong SQL Server: " + list.size());
        for (NhanVien nv : list) {
            System.out.println("-> Ma: " + nv.getMaNV() + " | HoTen: " + nv.getHoTen() + " | TK: " + nv.getTaiKhoan() + " | MK: " + nv.getMatKhau() + " | ChucVu: " + nv.getVaiTro());
        }
    }

    public boolean capNhatNhanVien(NhanVien nv) {
        Entity.NhanVien actor=UI.BaoVeQuyen.hienTai();
        if(!Entity.QuyenTruyCap.co(actor,Entity.QuyenTruyCap.Quyen.NHAN_VIEN))return false;
        synchronized(ConnectDB.getInstance()) {
            Connection c=ConnectDB.getInstance().getConnection();if(c==null)return false;
            boolean auto=true;try {auto=c.getAutoCommit();c.setAutoCommit(false);
                PhanQuyen_DAO.baoVeQuanLyCuoi(c,nv.getMaNV(),nv.getVaiTro(),nv.dangHoatDong());
                if(!capNhatNhanVienNoiBo(nv))throw new SQLException("Không cập nhật được tài khoản");
                PhanQuyen_DAO.ghi(c,actor.getMaNV(),nv.getMaNV(),"capNhatNhanVien");c.commit();return true;
            }catch(SQLException ex){try{c.rollback();}catch(SQLException ignored){}System.err.println(ex.getMessage());return false;}
            finally{try{c.setAutoCommit(auto);}catch(SQLException ignored){}}
        }
    }

    public boolean capNhatTrangThaiTaiKhoan(String maNV, String trangThaiMoi) {
        Entity.NhanVien actor=UI.BaoVeQuyen.hienTai();
        if(!Entity.QuyenTruyCap.co(actor,Entity.QuyenTruyCap.Quyen.NHAN_VIEN))return false;
        synchronized(ConnectDB.getInstance()) {
            Connection c=ConnectDB.getInstance().getConnection();if(c==null)return false;
            boolean auto=true;try {auto=c.getAutoCommit();c.setAutoCommit(false);
                String oldRole="";try(PreparedStatement p=c.prepareStatement("SELECT chucVu FROM NhanVien WHERE maNV=?")){p.setString(1,maNV);try(ResultSet r=p.executeQuery()){if(r.next())oldRole=r.getString(1);}} PhanQuyen_DAO.baoVeQuanLyCuoi(c,maNV,oldRole,new NhanVien(maNV,"","","",oldRole,trangThaiMoi).dangHoatDong());
                if(!capNhatTrangThaiTaiKhoanNoiBo(maNV, trangThaiMoi))throw new SQLException("Không cập nhật được tài khoản");
                PhanQuyen_DAO.ghi(c,actor.getMaNV(),maNV,"capNhatTrangThaiTaiKhoan");c.commit();return true;
            }catch(SQLException ex){try{c.rollback();}catch(SQLException ignored){}System.err.println(ex.getMessage());return false;}
            finally{try{c.setAutoCommit(auto);}catch(SQLException ignored){}}
        }
    }

    public boolean themNhanVien(NhanVien nv, String sdt) {
        Entity.NhanVien actor=UI.BaoVeQuyen.hienTai();
        if(!Entity.QuyenTruyCap.co(actor,Entity.QuyenTruyCap.Quyen.NHAN_VIEN))return false;
        synchronized(ConnectDB.getInstance()) {
            Connection c=ConnectDB.getInstance().getConnection();if(c==null)return false;
            boolean auto=true;try {auto=c.getAutoCommit();c.setAutoCommit(false);
                
                if(!themNhanVienNoiBo(nv, sdt))throw new SQLException("Không cập nhật được tài khoản");
                PhanQuyen_DAO.ghi(c,actor.getMaNV(),nv.getMaNV(),"themNhanVien");c.commit();return true;
            }catch(SQLException ex){try{c.rollback();}catch(SQLException ignored){}System.err.println(ex.getMessage());return false;}
            finally{try{c.setAutoCommit(auto);}catch(SQLException ignored){}}
        }
    }

    public boolean doiMatKhau(String maNV, String matKhauMoi) {
        Entity.NhanVien actor=UI.BaoVeQuyen.hienTai();
        if(!Entity.QuyenTruyCap.co(actor,Entity.QuyenTruyCap.Quyen.NHAN_VIEN))return false;
        synchronized(ConnectDB.getInstance()) {
            Connection c=ConnectDB.getInstance().getConnection();if(c==null)return false;
            boolean auto=true;try {auto=c.getAutoCommit();c.setAutoCommit(false);
                
                if(!doiMatKhauNoiBo(maNV, matKhauMoi))throw new SQLException("Không cập nhật được tài khoản");
                PhanQuyen_DAO.ghi(c,actor.getMaNV(),maNV,"doiMatKhau");c.commit();return true;
            }catch(SQLException ex){try{c.rollback();}catch(SQLException ignored){}System.err.println(ex.getMessage());return false;}
            finally{try{c.setAutoCommit(auto);}catch(SQLException ignored){}}
        }
    }
}
