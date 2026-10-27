package DAO;

import ConnectDB.ConnectDB;
import Entity.*;
import java.sql.*;
import java.time.LocalDate;

/** Phiếu chờ nhập được lưu SQL; xác nhận chỉ cộng tồn kho đúng một lần. */
public class NhapKho_DAO {
    public void luuChoNhap(PhieuKho phieu) throws SQLException {
        Connection c = ConnectDB.getInstance().getConnection();
        if (c == null) throw new SQLException("Chưa kết nối SQL Server");
        boolean auto = c.getAutoCommit();
        try {
            c.setAutoCommit(false);
            if (phieu.getChiTiet().isEmpty()) throw new SQLException("Phiếu không có chi tiết");
            String supplier = phieu.getNhaCungCap().split(" - ")[0].trim();
            String staff = null;
            try (PreparedStatement ps = c.prepareStatement("SELECT TOP 1 maNV FROM NhanVien WHERE maNV=? OR hoTen=? ORDER BY maNV")) {
                ps.setString(1, phieu.getNguoiLap()); ps.setString(2, phieu.getNguoiLap());
                try (ResultSet rs=ps.executeQuery()) { if(rs.next()) staff=rs.getString(1); }
            }
            if (staff == null) throw new SQLException("Không xác định được nhân viên lập phiếu");
            try (PreparedStatement ps=c.prepareStatement("INSERT INTO PhieuNhapKho(maPhieuNhap,maNCC,maNV,ngayNhap,tongTien,trangThai,ngayDuKienGiao,ghiChu) VALUES(?,?,?,CAST(GETDATE() AS date),?,'CHO_NHAP',?,?)")) {
                ps.setString(1,phieu.getSoPhieu()); ps.setString(2,supplier); ps.setString(3,staff); ps.setLong(4,phieu.getTongTien());
                LocalDate date=KhoDuLieu.phanTichNgay(phieu.getNgayDuKienGiao());
                ps.setDate(5,date==null?null:Date.valueOf(date)); ps.setString(6,phieu.getGhiChu()); ps.executeUpdate();
            }
            for(ChiTietKho ct:phieu.getChiTiet()) {
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO ChiTietPhieuNhapKho(maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES(?,?,?,?,?)")) {
                    ps.setString(1,phieu.getSoPhieu());ps.setString(2,ct.getMaPT());ps.setString(3,ct.getTenPT());ps.setInt(4,ct.getSoLuong());ps.setLong(5,ct.getDonGia());ps.executeUpdate();
                }
            }
            c.commit();
        } catch(SQLException|RuntimeException ex) { c.rollback();throw new SQLException("Không lưu được phiếu chờ nhập",ex); }
        finally { c.setAutoCommit(auto); }
    }

    public boolean doiTrangThai(String ma, boolean nhap) {
        Connection c=ConnectDB.getInstance().getConnection();
        if(c==null)return false;
        boolean auto=true;
        try {
            auto=c.getAutoCommit();c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("UPDATE PhieuNhapKho WITH (UPDLOCK,HOLDLOCK) SET trangThai=? WHERE maPhieuNhap=? AND trangThai='CHO_NHAP'")) {
                ps.setString(1,nhap?"DA_NHAP":"DA_HUY");ps.setString(2,ma);
                if(ps.executeUpdate()!=1)throw new SQLException("Phiếu không ở trạng thái chờ nhập");
            }
            if(nhap) {
                try(PreparedStatement ps=c.prepareStatement("SELECT ct.maPT FROM ChiTietPhieuNhapKho ct LEFT JOIN Kho k ON k.maPT=ct.maPT WHERE ct.maPhieuNhap=? AND k.maPT IS NULL")) {
                    ps.setString(1,ma);try(ResultSet rs=ps.executeQuery()){if(rs.next())throw new SQLException("Phụ tùng chưa có vị trí kho");}
                }
                try(PreparedStatement ps=c.prepareStatement("UPDATE k SET soLuongTon=k.soLuongTon+ct.soLuong FROM Kho k JOIN ChiTietPhieuNhapKho ct ON ct.maPT=k.maPT WHERE ct.maPhieuNhap=?")) {
                    ps.setString(1,ma);if(ps.executeUpdate()==0)throw new SQLException("Phiếu không có chi tiết");
                }
            }
            c.commit();return true;
        }catch(SQLException ex){try{c.rollback();}catch(SQLException ignored){}System.err.println(ex.getMessage());return false;}
        finally{try{c.setAutoCommit(auto);}catch(SQLException ignored){}}
    }
}
