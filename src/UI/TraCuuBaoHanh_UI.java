package UI;

import Entity.KhoDuLieu;
import Entity.PhieuBaoHanh;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class TraCuuBaoHanh_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí (Tự động nhận diện)", "Số phiếu bảo hành", "Biển số xe", "Tình trạng", "Nội dung bảo hành"
    });
    private final JLabel lblGoiY = new JLabel();
    private final JLabel lblDemKetQua = new JLabel();
    private String locTinhTrang = "TAT_CA";
    private JButton chipTatCa, chipConHan, chipHetHan, chipDangXuLy;
    private final DefaultTableModel model = UiHelper.model("Số phiếu", "Biển số xe", "Ngày bắt đầu", "Ngày kết thúc", "Tình trạng", "Nội dung bảo hành");
    private JTable table;
    private ArrayList<PhieuBaoHanh> dsHienTai = new ArrayList<>();

    public TraCuuBaoHanh_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout()); setBackground(UiHelper.NEN); add(root, BorderLayout.CENTER);
        JPanel header = new JPanel(); header.setOpaque(false); header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UiHelper.tieuDeTrang("Tra cứu phiếu bảo hành");
        JLabel subtitle = new JLabel("Tra cứu thời hạn, tình trạng và lịch sử bảo hành của phương tiện");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14)); subtitle.setForeground(new Color(100,116,139));
        title.setAlignmentX(LEFT_ALIGNMENT); subtitle.setAlignmentX(LEFT_ALIGNMENT); header.add(title); header.add(Box.createVerticalStrut(4)); header.add(subtitle); header.add(Box.createVerticalStrut(10));
        root.add(header, BorderLayout.NORTH);

        JPanel panel = UiHelper.panelBang("Danh sách phiếu bảo hành");
        panel.add(taoKhungTimKiem(), BorderLayout.NORTH);
        table = UiHelper.bang(model); table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {105,120,125,125,120,280};
        for (int i=0;i<widths.length;i++) dinhDangCot(i, i==5 ? SwingConstants.LEFT : SwingConstants.CENTER, widths[i]);
        table.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if(e.getClickCount()==2) xemChiTiet(); }});
        JScrollPane sp = new JScrollPane(table); sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); UiHelper.danhSachBong(sp); panel.add(sp, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false); footer.setBorder(new EmptyBorder(10,4,4,4));
        lblDemKetQua.setFont(new Font("Segoe UI",Font.BOLD,13)); lblDemKetQua.setForeground(new Color(71,85,105)); footer.add(lblDemKetQua,BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); actions.setOpaque(false);
        actions.add(UiHelper.nut("Xem chi tiết phiếu", UiIcon.of(UiIcon.SEARCH,15), UiHelper.XANH_TRUNG,e->xemChiTiet()));
        actions.add(UiHelper.nut("Lập phiếu bảo hành mới", UiIcon.of(UiIcon.PLUS,15), UiHelper.XANH_LA,e->chuyenTrangLapPhieu())); footer.add(actions,BorderLayout.EAST); panel.add(footer,BorderLayout.SOUTH);
        root.add(panel,BorderLayout.CENTER); capNhatGoiY(); napBang();
    }

    private JPanel taoKhungTimKiem() {
        JPanel p=new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(0,0,12,0));
        JPanel row=new JPanel(new BorderLayout(10,0)); row.setOpaque(false);
        JPanel criteria=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); criteria.setOpaque(false); JLabel label=new JLabel("Tìm theo:"); label.setFont(UiHelper.FONT_NHAN); label.setForeground(new Color(51,65,85));
        cboTieuChi.setPreferredSize(new Dimension(245,38)); cboTieuChi.setFont(UiHelper.FONT_BT); cboTieuChi.setBackground(Color.WHITE); cboTieuChi.addActionListener(e->{capNhatGoiY();napBang();}); criteria.add(label);criteria.add(cboTieuChi);row.add(criteria,BorderLayout.WEST);
        txtTuKhoa.setFont(new Font("Segoe UI",Font.PLAIN,14));txtTuKhoa.setPreferredSize(new Dimension(350,38));UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){napBang();}public void removeUpdate(DocumentEvent e){napBang();}public void changedUpdate(DocumentEvent e){napBang();}});row.add(txtTuKhoa,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));buttons.setOpaque(false);
        buttons.add(UiHelper.nut("Tìm kiếm",UiIcon.of(UiIcon.SEARCH,15),UiHelper.XANH_TRUNG,e->napBang()));
        buttons.add(UiHelper.nut("Làm mới",UiIcon.of(UiIcon.REFRESH,15),UiHelper.XAM,e->{txtTuKhoa.setText("");cboTieuChi.setSelectedIndex(0);locTinhTrang="TAT_CA";capNhatChip();napBang();}));row.add(buttons,BorderLayout.EAST);p.add(row);p.add(Box.createVerticalStrut(8));
        JPanel chips=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));chips.setOpaque(false);JLabel status=new JLabel("Tình trạng:");status.setFont(new Font("Segoe UI",Font.BOLD,12));status.setForeground(new Color(100,116,139));chips.add(status);
        chipTatCa=taoChip("Tất cả","TAT_CA");chipConHan=taoChip("Còn hạn","Còn hạn");chipHetHan=taoChip("Hết hạn","Hết hạn");chipDangXuLy=taoChip("Đang xử lý","Đang xử lý");chips.add(chipTatCa);chips.add(chipConHan);chips.add(chipHetHan);chips.add(chipDangXuLy);p.add(chips);p.add(Box.createVerticalStrut(8));
        JPanel hint=new JPanel(new FlowLayout(FlowLayout.LEFT,10,6));hint.setOpaque(true);hint.setBackground(new Color(239,246,255));hint.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(191,219,254)),new EmptyBorder(4,10,4,10)));
        lblGoiY.setFont(new Font("Segoe UI",Font.PLAIN,12));lblGoiY.setForeground(new Color(30,64,175));hint.add(lblGoiY);p.add(hint);capNhatChip();return p;
    }
    private JButton taoChip(String text,String value){JButton button=new JButton(text);button.setFont(new Font("Segoe UI",Font.PLAIN,12));button.setForeground(Color.BLACK);button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));button.setFocusPainted(false);button.setBackground(Color.WHITE);button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(203,213,225),1,true),new EmptyBorder(5,12,5,12)));button.addActionListener(e->{locTinhTrang=value;capNhatChip();napBang();});return button;}
    private void capNhatChip(){capNhatChip(chipTatCa,"TAT_CA".equalsIgnoreCase(locTinhTrang));capNhatChip(chipConHan,"Còn hạn".equalsIgnoreCase(locTinhTrang));capNhatChip(chipHetHan,"Hết hạn".equalsIgnoreCase(locTinhTrang));capNhatChip(chipDangXuLy,"Đang xử lý".equalsIgnoreCase(locTinhTrang));}
    private void capNhatChip(JButton button,boolean selected){if(button==null)return;button.setFont(new Font("Segoe UI",selected?Font.BOLD:Font.PLAIN,12));button.setBackground(selected?new Color(239,246,255):Color.WHITE);button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(selected?new Color(59,130,246):new Color(203,213,225),1,true),new EmptyBorder(5,12,5,12)));}
    private void capNhatGoiY(){String text=switch(cboTieuChi.getSelectedIndex()){case 1->"Nhập số phiếu bảo hành (VD: BH001) để tìm chính xác.";case 2->"Nhập biển số xe để xem các phiếu bảo hành của phương tiện.";case 3->"Nhập tình trạng: Còn hạn, Hết hạn hoặc Đang xử lý.";case 4->"Nhập từ khóa trong nội dung bảo hành.";default->"Tìm nhanh theo số phiếu, biển số, tình trạng hoặc nội dung bảo hành.";};lblGoiY.setText(text);txtTuKhoa.setToolTipText(text);}
    private void dinhDangCot(int col,int align,int width){TableColumn tc=table.getColumnModel().getColumn(col);tc.setPreferredWidth(width);tc.setHeaderRenderer(new DefaultTableCellRenderer(){public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,s,f,r,c);l.setHorizontalAlignment(align);l.setBackground(new Color(241,245,249));l.setForeground(new Color(30,41,59));l.setFont(new Font("Segoe UI",Font.BOLD,13));l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,UiHelper.VIEN),new EmptyBorder(8,align==SwingConstants.LEFT?12:4,8,4)));return l;}});tc.setCellRenderer(new DefaultTableCellRenderer(){public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){Component x=super.getTableCellRendererComponent(t,v,s,f,r,c);if(!s){x.setBackground(r%2==0?Color.WHITE:new Color(248,250,252));x.setForeground(new Color(30,41,59));}else{x.setBackground(new Color(219,234,254));x.setForeground(UiHelper.XANH_TRUNG);}if(x instanceof JLabel l){l.setHorizontalAlignment(align);l.setBorder(new EmptyBorder(0,align==SwingConstants.LEFT?12:4,0,4));}return x;}});}
    private void napBang(){String q=txtTuKhoa.getText().trim();int criterion=cboTieuChi.getSelectedIndex();model.setRowCount(0);dsHienTai=KhoDuLieu.get().getPhieuBaoHanhDAO().traCuu(q,criterion,locTinhTrang);for(PhieuBaoHanh p:dsHienTai){String bs=(p.getPhuongTien()!=null)?p.getPhuongTien().getBienSo():p.getBienSo();model.addRow(new Object[]{p.getSoPhieu(),bs,p.getNgayBDHienThi(),p.getNgayKTHienThi(),p.getTinhTrang(),p.getNoiDung()});}lblDemKetQua.setText("Hiển thị: "+dsHienTai.size()+" / "+KhoDuLieu.get().getPhieuBaoHanhs().size()+" phiếu bảo hành");}
    private String safe(String s){return s==null?"":s;}
    private void xemChiTiet(){
        int row=table.getSelectedRow();
        if(row<0){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn một phiếu bảo hành trong bảng để xem chi tiết!","Thông báo",JOptionPane.WARNING_MESSAGE);
            return;
        }
        PhieuBaoHanh p=dsHienTai.get(table.convertRowIndexToModel(row));
        String maPBH = p.getSoPhieu();

        java.util.List<Entity.ChiTietPhieuBaoHanh> dsCT = KhoDuLieu.get().getPhieuBaoHanhDAO().layChiTietPhieuBaoHanh(maPBH);

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWindow, "Chi tiết phiếu bảo hành — " + maPBH, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(820, 560);
        dlg.setLocationRelativeTo(parentWindow);
        dlg.setLayout(new BorderLayout());

        // Header info
        JPanel pnlHead = new JPanel(new GridLayout(3, 2, 10, 4));
        pnlHead.setBackground(new Color(241, 245, 249));
        pnlHead.setBorder(new EmptyBorder(12, 16, 12, 16));
        pnlHead.add(new JLabel("Mã phiếu: " + maPBH));
        pnlHead.add(new JLabel("Biển số xe: " + safe(p.getBienSo())));
        pnlHead.add(new JLabel("Thời hạn: " + safe(p.getNgayBDHienThi()) + "  ➔  " + safe(p.getNgayKTHienThi())));
        pnlHead.add(new JLabel("Tình trạng: " + safe(p.getTinhTrang())));
        pnlHead.add(new JLabel("Người lập: " + safe(p.getNguoiLap())));
        pnlHead.add(new JLabel("Tổng số hạng mục: " + (dsCT.isEmpty() ? "1" : dsCT.size()) + " mục"));
        dlg.add(pnlHead, BorderLayout.NORTH);

        // Center Table with Sửa chữa & Phụ tùng
        DefaultTableModel md = UiHelper.model("STT", "Loại bảo hành", "Tên hạng mục", "Thời hạn", "Ngày hết hạn", "Điều kiện bảo hành");
        JTable tbl = UiHelper.bang(md);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(45);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(140);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(230);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(85);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(95);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(260);

        UiHelper.canGiua(tbl, 0, 3, 4);
        tbl.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = value != null ? value.toString() : "";
                boolean laSC = val.contains("sửa chữa") || val.contains("dịch vụ");
                if (!isSelected) {
                    l.setBackground(laSC ? new Color(239, 246, 255) : new Color(240, 253, 244));
                    l.setForeground(laSC ? new Color(29, 78, 216) : new Color(21, 128, 61));
                }
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                return l;
            }
        });

        int idx = 1;
        for (Entity.ChiTietPhieuBaoHanh ct : dsCT) {
            md.addRow(new Object[]{
                    idx++,
                    ct.getLoaiHangMuc(),
                    ct.getTenHangMuc(),
                    ct.getThoiHanBaoHanh() + " tháng",
                    ct.getNgayHetHanHienThi(),
                    ct.getDieuKienBaoHanh()
            });
        }
        if (dsCT.isEmpty() && !safe(p.getNoiDung()).isEmpty()) {
            md.addRow(new Object[]{1, "Phụ tùng / Linh kiện", p.getNoiDung(), "6 tháng", safe(p.getNgayKTHienThi()), "Theo quy chuẩn dịch vụ gara"});
        }

        dlg.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // Footer buttons: Đóng + In phiếu
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBot.setBorder(new EmptyBorder(6, 12, 6, 12));
        JButton btnIn = UiHelper.nut("In phiếu bảo hành", UiIcon.of(UiIcon.PRINT, 14), new Color(194, 65, 12), ev -> {
            new LapPhieuBaoHanh_UI(null).inPhieuTheoMa(maPBH);
        });
        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, ev -> dlg.dispose());
        pnlBot.add(btnIn);
        pnlBot.add(btnDong);
        dlg.add(pnlBot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }
    private void chuyenTrangLapPhieu(){Main_UI main=Main_UI.getInstance();if(main==null)main=(Main_UI)SwingUtilities.getAncestorOfClass(Main_UI.class,this);if(main!=null)main.chuyenTrang("Lập phiếu bảo hành");else JOptionPane.showMessageDialog(this,"Không tìm thấy giao diện chính.");}
}
