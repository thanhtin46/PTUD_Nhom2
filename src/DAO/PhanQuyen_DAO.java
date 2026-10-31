package DAO;
import Entity.*;
import java.sql.*;
import java.util.*;
public class PhanQuyen_DAO {
    public void napQuyen(NhanVien n) throws SQLException {
        Connection c=ConnectDB.ConnectDB.getInstance().getConnection();
        if(c==null)throw new SQLException("Không kết nối SQL");
        // Kiểm tra nếu bảng quyền bổ sung chưa tồn tại
        try(PreparedStatement p=c.prepareStatement("SELECT OBJECT_ID('dbo.QuyenBoSung','U')");ResultSet r=p.executeQuery()) {
            r.next();if(r.getObject(1)==null){n.setCacQuyen(new String[0]);return;}
        }
        List<String> list=new ArrayList<>();
        try(PreparedStatement p=c.prepareStatement("SELECT maQuyen FROM QuyenBoSung WHERE maNV=?")) {
            p.setString(1,n.getMaNV());try(ResultSet r=p.executeQuery()){while(r.next())list.add(r.getString(1));}
        }
        n.setCacQuyen(list.toArray(new String[0]));
    }
    public void luu(NhanVien actor,String ma,String role,Set<String> extra) throws SQLException {
        if(!QuyenTruyCap.co(actor,QuyenTruyCap.Quyen.PHAN_QUYEN))throw new SQLException("Chỉ quản lý được phân quyền");
        if(QuyenTruyCap.vaiTro(role)==QuyenTruyCap.VaiTro.KHONG_RO)throw new SQLException("Vai trò không hợp lệ");
        for(String q:extra)try{QuyenTruyCap.Quyen.valueOf(q);}catch(Exception e){throw new SQLException("Quyền không hợp lệ");}
        Connection c=ConnectDB.ConnectDB.getInstance().getConnection();if(c==null)throw new SQLException("Không kết nối SQL");
        synchronized(ConnectDB.ConnectDB.getInstance()) {
            boolean auto=c.getAutoCommit();try {
                c.setAutoCommit(false);
                baoVeQuanLyCuoi(c,ma,role,true);
                try(PreparedStatement p=c.prepareStatement("UPDATE NhanVien SET chucVu=? WHERE maNV=?")){p.setString(1,role);p.setString(2,ma);if(p.executeUpdate()!=1)throw new SQLException("Không tìm thấy nhân viên");}
                try(PreparedStatement p=c.prepareStatement("DELETE FROM QuyenBoSung WHERE maNV=?")){p.setString(1,ma);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO QuyenBoSung VALUES(?,?)")) {
                    for(String q:extra){p.setString(1,ma);p.setString(2,q);p.addBatch();}p.executeBatch();
                }
                ghi(c,actor.getMaNV(),ma,"Vai trò: "+role+"; quyền bổ sung: "+String.join(",",new TreeSet<>(extra)));
                c.commit();
            }catch(SQLException e){c.rollback();throw e;}finally{c.setAutoCommit(auto);}
        }
    }
    public static void baoVeQuanLyCuoi(Connection c,String target,String newRole,boolean active) throws SQLException {
        int others=0;boolean current=false;
        try(PreparedStatement p=c.prepareStatement("SELECT n.maNV,n.chucVu,n.trangThai,t.trangThai AS tt FROM NhanVien n WITH(UPDLOCK,HOLDLOCK) JOIN TaiKhoan t WITH(UPDLOCK,HOLDLOCK) ON t.maNV=n.maNV");ResultSet r=p.executeQuery()) {
            while(r.next()) {
                NhanVien n=new NhanVien(r.getString(1),"","","",r.getString(2),r.getString(3));
                boolean manager=QuyenTruyCap.vaiTro(n.getVaiTro())==QuyenTruyCap.VaiTro.QUAN_LY && n.dangHoatDong() && !"KHOA".equalsIgnoreCase(r.getString(4));
                if(manager){if(target.equals(n.getMaNV()))current=true;else others++;}
            }
        }
        if(current && others==0 && (!active || QuyenTruyCap.vaiTro(newRole)!=QuyenTruyCap.VaiTro.QUAN_LY))throw new SQLException("Không thể khóa hoặc hạ quyền quản lý hoạt động cuối cùng");
    }
    public static void ghi(Connection c,String actor,String target,String text)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO NhatKyQuyen(nguoiThucHien,doiTuong,noiDung) VALUES(?,?,?)")){p.setString(1,actor);p.setString(2,target);p.setString(3,text);p.executeUpdate();}
    }
}
