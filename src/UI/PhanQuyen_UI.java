package UI;
import Entity.*;
import DAO.PhanQuyen_DAO;
import javax.swing.*;
import java.awt.*;
import java.util.*;
public class PhanQuyen_UI extends JPanel {
    private static final String[] NHAN = {"Xem khách hàng","Thêm sửa khách hàng","Xóa khách hàng","Xem phương tiện","Thêm sửa phương tiện","Xóa phương tiện","Tiếp nhận xe","Xem lịch sử sửa chữa","Lập sửa chữa","Cập nhật tiến độ","Phân công và điều hành","Xem kho","Quản lý phụ tùng","Nhập kho","Xuất kho","Thanh toán","Xem và in hóa đơn","Hủy hóa đơn","Bảo hành","Báo cáo quản trị","Quản lý tài khoản","Phân quyền","Cấu hình hệ thống"};
    private final JComboBox<String> staff=new JComboBox<>();
    private final JComboBox<String> role=new JComboBox<>(new String[]{"Quản lý","Nhân viên tiếp tân","Nhân viên sửa chữa","Nhân viên kho","Nhân viên thu ngân"});
    private final Map<QuyenTruyCap.Quyen,JCheckBox> checks=new LinkedHashMap<>();
    private final java.util.List<NhanVien> people=new ArrayList<>();
    public PhanQuyen_UI() {
        setLayout(new BorderLayout(10,10));setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JPanel top=new JPanel(new GridLayout(0,1,5,5));top.add(UiHelper.tieuDeTrang("Vai trò và quyền bổ sung"));top.add(staff);top.add(role);
        top.add(new JLabel("Quyền mặc định được giữ theo vai trò. Thay đổi có hiệu lực từ lần đăng nhập tiếp theo."));add(top,BorderLayout.NORTH);
        JPanel grid=new JPanel(new GridLayout(0,3,8,8));
        for(QuyenTruyCap.Quyen q:QuyenTruyCap.Quyen.values()){JCheckBox c=new JCheckBox(NHAN[q.ordinal()]);checks.put(q,c);grid.add(c);}add(new JScrollPane(grid));
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));bottom.setOpaque(false);JButton reset=UiHelper.nut("Khôi phục theo vai trò", UiIcon.of(UiIcon.REFRESH, 14), UiHelper.XAM, e->{checks.values().forEach(c->c.setSelected(false));defaults();});JButton save=UiHelper.nut("Lưu thay đổi", UiIcon.of(UiIcon.CHECK, 14), UiHelper.XANH_TRUNG, e->save());bottom.add(reset);bottom.add(save);add(bottom,BorderLayout.SOUTH);
        people.addAll(new DAO.NhanVien_DAO().layTatCaNhanVien());for(NhanVien n:people)staff.addItem(n.getMaNV()+" — "+n.getHoTen());
        staff.addActionListener(e->select());role.addActionListener(e->defaults());reset.addActionListener(e->{checks.values().forEach(c->c.setSelected(false));defaults();});
        save.addActionListener(e->save());select();
    }
    private void defaults(){Set<QuyenTruyCap.Quyen> base=QuyenTruyCap.macDinh((String)role.getSelectedItem());checks.forEach((q,c)->{
        boolean fixed=base.contains(q);if(!c.isEnabled())c.setSelected(false);c.setEnabled(!fixed && q!=QuyenTruyCap.Quyen.PHAN_QUYEN && q!=QuyenTruyCap.Quyen.NHAN_VIEN && q!=QuyenTruyCap.Quyen.CAU_HINH);if(fixed)c.setSelected(true);
    });}
    private void select(){int i=staff.getSelectedIndex();if(i<0)return;NhanVien n=people.get(i);role.setSelectedItem(n.getVaiTro());checks.values().forEach(c->c.setSelected(false));
        try{new PhanQuyen_DAO().napQuyen(n);if(n.getCacQuyen()!=null)for(String q:n.getCacQuyen())try{checks.get(QuyenTruyCap.Quyen.valueOf(q)).setSelected(true);}catch(IllegalArgumentException ignored){}defaults();}
        catch(Exception e){JOptionPane.showMessageDialog(this,"Không tải được quyền: "+e.getMessage());}
    }
    private void save(){if(!BaoVeQuyen.yeuCau(this,QuyenTruyCap.Quyen.PHAN_QUYEN))return;int i=staff.getSelectedIndex();if(i<0)return;
        if(JOptionPane.showConfirmDialog(this,"Lưu vai trò và quyền mới?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        Set<String> extra=new HashSet<>();checks.forEach((q,c)->{if(c.isEnabled()&&c.isSelected())extra.add(q.name());});
        try{new PhanQuyen_DAO().luu(BaoVeQuyen.hienTai(),people.get(i).getMaNV(),(String)role.getSelectedItem(),extra);JOptionPane.showMessageDialog(this,"Đã lưu SQL. Có hiệu lực từ lần đăng nhập tiếp theo.");}
        catch(Exception e){JOptionPane.showMessageDialog(this,"Không lưu được quyền. Kiểm tra migration và dữ liệu.\n"+e.getMessage());}
    }
}
