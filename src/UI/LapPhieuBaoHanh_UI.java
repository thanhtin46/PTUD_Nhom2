package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuBaoHanh;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LapPhieuBaoHanh_UI extends JPanel {
    private static final DateTimeFormatter DMY=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NhanVien nv; private final KhoDuLieu kho=KhoDuLieu.get();
    private final JTextField txtSoPhieu=new JTextField(),txtNgayBd=new JTextField(),txtNgayKt=new JTextField();
    private final JComboBox<String> cboBienSo=new JComboBox<>(); private final JTextArea txtNoiDung=new JTextArea(4,20);
    private final JLabel lblDem=new JLabel(); private final DefaultTableModel model=UiHelper.model("Số phiếu","Biển số xe","Ngày bắt đầu","Ngày kết thúc","Nội dung bảo hành");
    private JTable table; private boolean dangSua=false;

    public LapPhieuBaoHanh_UI(NhanVien nv){
        this.nv=nv;JPanel root=UiHelper.nen();setLayout(new BorderLayout());setBackground(UiHelper.NEN);add(root,BorderLayout.CENTER);
        JPanel header=new JPanel();header.setOpaque(false);header.setLayout(new BoxLayout(header,BoxLayout.Y_AXIS));JLabel title=UiHelper.tieuDeTrang("Lập phiếu bảo hành"),sub=new JLabel("Tạo và quản lý hồ sơ bảo hành phương tiện");sub.setFont(new Font("Segoe UI",Font.PLAIN,14));sub.setForeground(new Color(100,116,139));title.setAlignmentX(LEFT_ALIGNMENT);sub.setAlignmentX(LEFT_ALIGNMENT);header.add(title);header.add(Box.createVerticalStrut(4));header.add(sub);header.add(Box.createVerticalStrut(10));root.add(header,BorderLayout.NORTH);
        txtSoPhieu.setEditable(false);for(PhuongTien xe:kho.getPhuongTiens())cboBienSo.addItem(xe.getBienSo());
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,taoForm(),taoBang());split.setResizeWeight(.34);split.setDividerLocation(440);split.setBorder(null);split.setOpaque(false);root.add(split,BorderLayout.CENTER);lamMoi();napBang();
    }
    private JPanel taoForm(){
        JPanel panel=UiHelper.panelForm("Thông tin phiếu bảo hành",440);JPanel content=new JPanel(new BorderLayout(0,12));content.setOpaque(false);
        JPanel form=new JPanel(new GridBagLayout());form.setOpaque(false);UiHelper.themDong(form,0,"Số phiếu",txtSoPhieu);UiHelper.themDong(form,1,"Biển số xe",cboBienSo);UiHelper.themDong(form,2,"Ngày bắt đầu",txtNgayBd);UiHelper.themDong(form,3,"Ngày kết thúc",txtNgayKt);
        txtNoiDung.setFont(UiHelper.FONT_BT);txtNoiDung.setLineWrap(true);txtNoiDung.setWrapStyleWord(true);txtNoiDung.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiHelper.VIEN),new EmptyBorder(8,10,8,10)));JScrollPane contentScroll=new JScrollPane(txtNoiDung);contentScroll.setPreferredSize(new Dimension(190,100));UiHelper.themDong(form,4,"Nội dung bảo hành",contentScroll);content.add(form,BorderLayout.CENTER);
        JPanel actions=new JPanel(new GridLayout(2,2,8,8));actions.setOpaque(false);actions.setBorder(new EmptyBorder(8,0,0,0));
        actions.add(UiHelper.nut("Tạo phiếu",UiIcon.of(UiIcon.CHECK,16),UiHelper.XANH_LA,true,e->tao()));
        actions.add(UiHelper.nut("Lưu chỉnh sửa",UiIcon.of(UiIcon.CHECK,16),UiHelper.XANH_TRUNG,true,e->capNhat()));
        actions.add(UiHelper.nut("Xóa phiếu",UiIcon.of(UiIcon.TRASH,15),new Color(220,38,38),e->xoa()));
        actions.add(UiHelper.nut("Làm mới",UiIcon.of(UiIcon.REFRESH,15),UiHelper.XAM,e->lamMoi()));content.add(actions,BorderLayout.SOUTH);
        UiHelper.ganNoiDungForm(panel,content,null);return panel;
    }
    private JPanel taoBang(){JPanel panel=UiHelper.panelBang("Danh sách phiếu bảo hành");table=UiHelper.bang(model);table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);int[] widths={110,130,140,140,300};for(int i=0;i<widths.length;i++)dinhDangCot(i,i==4?SwingConstants.LEFT:SwingConstants.CENTER,widths[i]);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())napLenForm();});
        JScrollPane sp=new JScrollPane(table);sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);UiHelper.danhSachBong(sp);panel.add(sp,BorderLayout.CENTER);JPanel footer=new JPanel(new BorderLayout());footer.setOpaque(false);footer.setBorder(new EmptyBorder(10,4,4,4));lblDem.setFont(new Font("Segoe UI",Font.BOLD,13));lblDem.setForeground(new Color(71,85,105));footer.add(lblDem,BorderLayout.WEST);panel.add(footer,BorderLayout.SOUTH);return panel;}
    private void dinhDangCot(int col,int align,int width){var tc=table.getColumnModel().getColumn(col);tc.setPreferredWidth(width);tc.setHeaderRenderer(new DefaultTableCellRenderer(){public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,s,f,r,c);l.setHorizontalAlignment(align);l.setBackground(new Color(241,245,249));l.setForeground(new Color(30,41,59));l.setFont(new Font("Segoe UI",Font.BOLD,13));l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,UiHelper.VIEN),new EmptyBorder(8,align==SwingConstants.LEFT?12:4,8,4)));return l;}});tc.setCellRenderer(new DefaultTableCellRenderer(){public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){Component x=super.getTableCellRendererComponent(t,v,s,f,r,c);x.setBackground(s?new Color(219,234,254):(r%2==0?Color.WHITE:new Color(248,250,252)));x.setForeground(s?UiHelper.XANH_TRUNG:new Color(30,41,59));if(x instanceof JLabel l){l.setHorizontalAlignment(align);l.setBorder(new EmptyBorder(0,align==SwingConstants.LEFT?12:4,0,4));}return x;}});}
    private void napBang(){model.setRowCount(0);for(PhieuBaoHanh p:kho.getPhieuBaoHanhs())model.addRow(new Object[]{p.getSoPhieu(),p.getBienSo(),p.getNgayBatDau(),p.getNgayKetThuc(),p.getNoiDung()});lblDem.setText("Hiển thị: "+kho.getPhieuBaoHanhs().size()+" phiếu bảo hành");}
    private void napLenForm(){int row=table.getSelectedRow();if(row<0)return;String ma=String.valueOf(model.getValueAt(row,0));PhieuBaoHanh p=kho.getPhieuBaoHanhs().stream().filter(x->x.getSoPhieu().equals(ma)).findFirst().orElse(null);if(p==null)return;dangSua=true;txtSoPhieu.setText(p.getSoPhieu());cboBienSo.setSelectedItem(p.getBienSo());txtNgayBd.setText(p.getNgayBatDau());txtNgayKt.setText(p.getNgayKetThuc());txtNoiDung.setText(p.getNoiDung());}
    private boolean hopLe(){if(cboBienSo.getSelectedItem()==null){UiHelper.loi(this,"Chưa có xe để lập bảo hành!",cboBienSo);return false;}try{LocalDate bd=LocalDate.parse(txtNgayBd.getText().trim(),DMY),kt=LocalDate.parse(txtNgayKt.getText().trim(),DMY);if(kt.isBefore(bd)){UiHelper.loi(this,"Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.",txtNgayKt);return false;}}catch(DateTimeParseException ex){UiHelper.loi(this,"Ngày phải đúng định dạng dd/MM/yyyy.",txtNgayBd);return false;}if(txtNoiDung.getText().trim().isEmpty()){UiHelper.loi(this,"Vui lòng nhập nội dung bảo hành.",txtNoiDung);return false;}return true;}
    private PhieuBaoHanh taoDoiTuong(String ma){return new PhieuBaoHanh(ma,String.valueOf(cboBienSo.getSelectedItem()),txtNgayBd.getText().trim(),txtNgayKt.getText().trim(),nv!=null?nv.getHoTen():"Tiếp tân","Còn hạn",txtNoiDung.getText().trim());}
    private void tao(){if(dangSua){JOptionPane.showMessageDialog(this,"Đang chọn phiếu để chỉnh sửa. Hãy chọn Làm mới trước khi tạo phiếu mới.");return;}if(!hopLe())return;boolean ok=kho.themPhieuBaoHanh(taoDoiTuong(kho.maPhieuBaoHanhMoi()));napBang();lamMoi();JOptionPane.showMessageDialog(this,ok?"Đã tạo phiếu bảo hành và lưu CSDL SQL Server!":"Đã tạo phiếu bảo hành!");}
    private void capNhat(){if(!dangSua){JOptionPane.showMessageDialog(this,"Chọn một phiếu trong danh sách để chỉnh sửa.");return;}if(!hopLe())return;PhieuBaoHanh replacement=taoDoiTuong(txtSoPhieu.getText());boolean ok=kho.capNhatPhieuBaoHanh(replacement);napBang();lamMoi();JOptionPane.showMessageDialog(this,ok?"Đã cập nhật phiếu bảo hành.":"Đã cập nhật phiếu trong bộ nhớ; chưa đồng bộ được SQL Server.");}
    private void xoa(){if(!dangSua){JOptionPane.showMessageDialog(this,"Chọn một phiếu trong danh sách để xóa.");return;}String ma=txtSoPhieu.getText();if(JOptionPane.showConfirmDialog(this,"Bạn có chắc muốn xóa phiếu bảo hành "+ma+"?","Xác nhận xóa",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;boolean ok=kho.xoaPhieuBaoHanh(ma);napBang();lamMoi();JOptionPane.showMessageDialog(this,ok?"Đã xóa phiếu bảo hành.":"Đã xóa phiếu khỏi bộ nhớ; chưa đồng bộ được SQL Server.");}
    private void lamMoi(){dangSua=false;table.clearSelection();txtSoPhieu.setText(String.format("BH%03d",kho.getPhieuBaoHanhs().size()+1));txtNgayBd.setText(LocalDate.now().format(DMY));txtNgayKt.setText(LocalDate.now().plusMonths(6).format(DMY));txtNoiDung.setText("");if(cboBienSo.getItemCount()>0)cboBienSo.setSelectedIndex(0);}
}
