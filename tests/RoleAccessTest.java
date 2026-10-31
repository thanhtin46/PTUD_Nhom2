package tests;

import Entity.*;
import DAO.PhanQuyen_DAO;
import java.util.*;
import java.sql.*;
public class RoleAccessTest {
    static int checks=0;
    static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    static NhanVien n(String role){return new NhanVien("TEST","Test","quanLy","",role);}
    public static void main(String[] args)throws Exception {
        String[] roles={"Quản lý","Nhân viên tiếp tân","Nhân viên sửa chữa","Nhân viên kho","Nhân viên thu ngân","Unknown"};
        for(String role:roles) {
            NhanVien user=n(role);
            for(var entry:QuyenTruyCap.TRANG.entrySet())check(QuyenTruyCap.moTrang(user,entry.getKey())==QuyenTruyCap.macDinh(role).contains(entry.getValue()),role+entry.getKey());
            check(!QuyenTruyCap.moTrang(user,"không tồn tại"),"unknown route");
        }
        check(!QuyenTruyCap.moTrang(n("Unknown"),"Admin Hub"),"username must not grant admin");
        check(!QuyenTruyCap.moTrang(n("Nhân viên kho"),"Phiếu sửa chữa"),"warehouse cannot repair");
        check(!QuyenTruyCap.moTrang(n("Nhân viên sửa chữa"),"Xuất kho"),"technician cannot export");
        NhanVien cashier=n("Nhân viên thu ngân");check(QuyenTruyCap.moTrang(cashier,"Bán lẻ phụ tùng"),"retail alias");
        check(!QuyenTruyCap.co(cashier,QuyenTruyCap.Quyen.KH_XOA),"read is not delete");
        cashier.setCacQuyen(new String[]{"KH_SUA"});check(QuyenTruyCap.moTrang(cashier,"Quản lý khách hàng"),"extra");
        cashier.setTrangThai("NGUNG");check(!QuyenTruyCap.moTrang(cashier,"Lập hóa đơn"),"locked");
        if(args.length>0) {
            Connection c=ConnectDB.ConnectDB.getInstance().getConnection();
            if(c==null||!c.getCatalog().equals("GaraSyncTest_Report_20261028"))throw new IllegalStateException("Test DB only");
            NhanVien actor=n("Quản lý");actor.setMaNV("NV04");
            String target="NV_RPT_LOCK",oldRole;Set<String> old=new HashSet<>();
            try(PreparedStatement p=c.prepareStatement("SELECT chucVu FROM NhanVien WHERE maNV=?")){p.setString(1,target);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalStateException("Missing fixture");oldRole=r.getString(1);}}
            NhanVien read=n(oldRole);read.setMaNV(target);PhanQuyen_DAO dao=new PhanQuyen_DAO();dao.napQuyen(read);Collections.addAll(old,read.getCacQuyen());
            try {
                dao.luu(actor,target,oldRole,Set.of("KH_SUA"));dao.napQuyen(read);check(Arrays.asList(read.getCacQuyen()).contains("KH_SUA"),"persist SQL extra");
                dao.luu(actor,target,oldRole,Set.of());dao.napQuyen(read);check(read.getCacQuyen().length==0,"revoke SQL extra");
                try{dao.luu(n("Nhân viên kho"),target,oldRole,Set.of());throw new AssertionError("non manager writes");}catch(SQLException expected){checks++;}
            }finally{dao.luu(actor,target,oldRole,old);}
        }
        System.out.println("PASS: "+checks+" permission checks");
    }
}
