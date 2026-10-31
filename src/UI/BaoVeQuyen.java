package UI;
import Entity.*;
import javax.swing.*;
import java.awt.Component;
import java.sql.*;
public final class BaoVeQuyen {
    private BaoVeQuyen() {}
    public static NhanVien hienTai() { return Main_UI.getInstance()==null?null:Main_UI.getInstance().getNhanVienHienTai(); }
    public static boolean yeuCau(Component owner, QuyenTruyCap.Quyen q) {
        if(QuyenTruyCap.co(hienTai(),q))return true;
        JOptionPane.showMessageDialog(owner,"Bạn không có quyền thực hiện thao tác này.");return false;
    }
    /** Ẩn các nút thao tác nếu nhân viên không có quyền */
    public static void locNut(Component component,String route) {
        if(component instanceof javax.swing.AbstractButton b) {
            String text=b.getText()==null?"":b.getText().toLowerCase(java.util.Locale.ROOT);
            QuyenTruyCap.Quyen q=null;
            if(text.contains("hủy hóa đơn"))q=QuyenTruyCap.Quyen.HD_HUY;
            else if(text.contains("phân công"))q=QuyenTruyCap.Quyen.PHAN_CONG;
            else if(text.contains("tiếp nhận") && !text.contains("tra cứu"))q=QuyenTruyCap.Quyen.TIEP_NHAN;
            else if(text.contains("lập hóa đơn"))q=QuyenTruyCap.Quyen.THANH_TOAN;
            else if(text.contains("xóa") && route.equals("Quản lý khách hàng"))q=QuyenTruyCap.Quyen.KH_XOA;
            else if(text.contains("xóa") && route.equals("Quản lý phương tiện"))q=QuyenTruyCap.Quyen.XE_XOA;
            else if(text.contains("xóa") && route.equals("Lập phiếu tiếp nhận"))q=QuyenTruyCap.Quyen.KH_XOA;
            if(q!=null && !QuyenTruyCap.co(hienTai(),q))b.setVisible(false);
        }
        if(component instanceof java.awt.Container c)for(Component child:c.getComponents())locNut(child,route);
    }
    public static boolean phieu(String ma) {
        NhanVien n=hienTai();if(n==null)return false;
        if(QuyenTruyCap.co(n,QuyenTruyCap.Quyen.PHAN_CONG))return true;
        if(!QuyenTruyCap.co(n,QuyenTruyCap.Quyen.SC) && !QuyenTruyCap.co(n,QuyenTruyCap.Quyen.TIEN_DO))return false;
        try {
            Connection c=ConnectDB.ConnectDB.getInstance().getConnection();if(c==null)return false;
            try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM PhieuSuaChua WHERE maNV=? AND (maPSC=? OR maPTN=?)")) {
                p.setString(1,n.getMaNV());p.setString(2,ma);p.setString(3,ma);
                try(ResultSet r=p.executeQuery()){return r.next();}
            }
        }catch(SQLException ex){return false;}
    }
}
