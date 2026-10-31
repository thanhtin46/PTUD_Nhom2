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
    /** Quyền gắn với nút, không suy đoán theo nội dung nhãn. */
    public static <T extends AbstractButton> T ganQuyen(T nut, QuyenTruyCap.Quyen quyen) {
        nut.putClientProperty("quyenTruyCap", quyen);
        nut.setVisible(QuyenTruyCap.co(hienTai(), quyen));
        return nut;
    }

    public static void locNut(Component component) {
        if (component instanceof AbstractButton nut
                && nut.getClientProperty("quyenTruyCap") instanceof QuyenTruyCap.Quyen quyen) {
            nut.setVisible(QuyenTruyCap.co(hienTai(), quyen));
        }
        if (component instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) locNut(child);
        }
    }
    public static boolean phieu(String ma) {
        NhanVien n=hienTai();if(n==null)return false;
        if(QuyenTruyCap.co(n,QuyenTruyCap.Quyen.PHAN_CONG))return true;
        if(!QuyenTruyCap.co(n,QuyenTruyCap.Quyen.SC) && !QuyenTruyCap.co(n,QuyenTruyCap.Quyen.TIEN_DO))return false;
        try {
            return new DAO.PhieuSuaChua_DAO().duocPhanCong(n.getMaNV(), ma);
        } catch (SQLException ex) {
            return false;
        }
    }
}
