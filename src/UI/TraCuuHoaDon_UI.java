package UI;

import DAO.HoaDon_DAO;
import Entity.ChiTietDichVu;
import Entity.HoaDon;
import Entity.KhoDuLieu;

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

/** Tra cứu hóa đơn theo cùng bố cục với màn hình tra cứu phiếu tiếp nhận. */
public class TraCuuHoaDon_UI extends JPanel {
    private final HoaDon_DAO hoaDonDAO = new HoaDon_DAO();
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí (Tự động nhận diện)", "Mã hóa đơn", "Số phiếu tiếp nhận / sửa chữa",
            "Biển số xe", "Khách hàng (Tên / SĐT / Mã KH)", "Thu ngân"
    });
    private final JComboBox<String> cboPhuongThuc = new JComboBox<>(new String[]{
            "Tất cả phương thức", "Chuyển khoản QR", "Tiền mặt", "Quẹt thẻ POS"
    });
    private final JLabel lblGoiY = new JLabel();
    private final JLabel lblDemKetQua = new JLabel("Đang tải dữ liệu...");
    private String locTrangThai = "TAT_CA";
    private JButton chipTatCa, chipDaThanhToan, chipDaHuy;
    private DefaultTableModel model;
    private JTable table;
    private ArrayList<HoaDon> dsHienTai = new ArrayList<>();

    public TraCuuHoaDon_UI(Entity.NhanVien nv) {
        this();
    }

    public TraCuuHoaDon_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel title = UiHelper.tieuDeTrang("Tra cứu hóa đơn");
        JLabel subtitle = new JLabel("Tìm hóa đơn, xem chi tiết thanh toán và theo dõi trạng thái hóa đơn dịch vụ");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(title);
        north.add(Box.createVerticalStrut(4));
        north.add(subtitle);
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Danh sách hóa đơn");
        bangPanel.add(taoKhungTimKiem(), BorderLayout.NORTH);
        model = UiHelper.model("Mã hóa đơn", "Số phiếu", "Khách hàng", "Số ĐT", "Biển số xe",
                "Ngày lập", "Phương thức", "Tiền công", "Phụ tùng", "Giảm giá", "Tổng tiền", "Thu ngân", "Trạng thái");
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        dinhDangCot(0, SwingConstants.CENTER, 100);
        dinhDangCot(1, SwingConstants.CENTER, 110);
        dinhDangCot(2, SwingConstants.LEFT, 150);
        dinhDangCot(3, SwingConstants.CENTER, 110);
        dinhDangCot(4, SwingConstants.CENTER, 110);
        dinhDangCot(5, SwingConstants.CENTER, 105);
        dinhDangCot(6, SwingConstants.LEFT, 145);
        dinhDangCot(7, SwingConstants.RIGHT, 115);
        dinhDangCot(8, SwingConstants.RIGHT, 115);
        dinhDangCot(9, SwingConstants.CENTER, 90);
        dinhDangCot(10, SwingConstants.RIGHT, 125);
        dinhDangCot(11, SwingConstants.LEFT, 140);
        dinhDangCotTrangThai(12, 125);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) xemChiTiet();
            }
        });
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 4, 4));
        lblDemKetQua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDemKetQua.setForeground(new Color(71, 85, 105));
        footer.add(lblDemKetQua, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(UiHelper.nut("Xem chi tiết hóa đơn", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> xemChiTiet()));
        actions.add(UiHelper.nut("Lập hóa đơn mới", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> chuyenTrangLapHoaDon()));
        footer.add(actions, BorderLayout.EAST);
        bangPanel.add(footer, BorderLayout.SOUTH);
        root.add(bangPanel, BorderLayout.CENTER);
        capNhatGoiY();
        napBang();
    }

    private JPanel taoKhungTimKiem() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setOpaque(false);
        JPanel criteria = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        criteria.setOpaque(false);
        JLabel label = new JLabel("Tìm theo:");
        label.setFont(UiHelper.FONT_NHAN);
        label.setForeground(new Color(51, 65, 85));
        cboTieuChi.setPreferredSize(new Dimension(250, 38));
        cboTieuChi.setFont(UiHelper.FONT_BT);
        cboTieuChi.setBackground(Color.WHITE);
        cboTieuChi.addActionListener(e -> { capNhatGoiY(); napBang(); });
        criteria.add(label);
        criteria.add(cboTieuChi);
        row1.add(criteria, BorderLayout.WEST);

        txtTuKhoa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTuKhoa.setPreferredSize(new Dimension(320, 38));
        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { napBang(); }
            @Override public void removeUpdate(DocumentEvent e) { napBang(); }
            @Override public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        row1.add(txtTuKhoa, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(UiHelper.nut("Tìm kiếm", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> napBang()));
        buttons.add(UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, false, e -> lamMoiTimKiem()));
        row1.add(buttons, BorderLayout.EAST);
        p.add(row1);
        p.add(Box.createVerticalStrut(8));

        JPanel row2 = new JPanel(new BorderLayout(8, 0));
        row2.setOpaque(false);
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        chips.add(statusLabel);
        chipTatCa = taoChip("Tất cả", "TAT_CA");
        chipDaThanhToan = taoChip("Đã thanh toán", "Đã thanh toán");
        chipDaHuy = taoChip("Đã hủy", "Đã hủy");
        chips.add(chipTatCa);
        chips.add(chipDaThanhToan);
        chips.add(chipDaHuy);
        row2.add(chips, BorderLayout.WEST);

        JPanel method = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        method.setOpaque(false);
        JLabel methodLabel = new JLabel("Thanh toán:");
        methodLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        methodLabel.setForeground(new Color(100, 116, 139));
        cboPhuongThuc.setPreferredSize(new Dimension(175, 34));
        cboPhuongThuc.setFont(UiHelper.FONT_BT);
        cboPhuongThuc.setBackground(Color.WHITE);
        cboPhuongThuc.addActionListener(e -> napBang());
        method.add(methodLabel);
        method.add(cboPhuongThuc);
        row2.add(method, BorderLayout.EAST);
        p.add(row2);
        p.add(Box.createVerticalStrut(8));

        JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        hint.setOpaque(false);
        hint.setBackground(new Color(239, 246, 255));
        hint.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        lblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGoiY.setForeground(new Color(30, 64, 175));
        hint.add(lblGoiY);
        p.add(hint);
        return p;
    }

    private JButton taoChip(String text, String value) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.BLACK);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(5, 12, 5, 12)));
        button.addActionListener(e -> {
            locTrangThai = value;
            capNhatChip();
            napBang();
        });
        return button;
    }

    private void capNhatChip() {
        capNhatChip(chipTatCa, "TAT_CA".equalsIgnoreCase(locTrangThai));
        capNhatChip(chipDaThanhToan, "Đã thanh toán".equalsIgnoreCase(locTrangThai));
        capNhatChip(chipDaHuy, "Đã hủy".equalsIgnoreCase(locTrangThai));
    }

    private void capNhatChip(JButton button, boolean selected) {
        if (button == null) return;
        button.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 12));
        button.setBackground(selected ? new Color(239, 246, 255) : Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? new Color(59, 130, 246) : new Color(203, 213, 225), 1, true),
                new EmptyBorder(5, 12, 5, 12)));
    }

    private void capNhatGoiY() {
        switch (cboTieuChi.getSelectedIndex()) {
            case 1 -> lblGoiY.setText("Nhập mã hóa đơn (VD: HD001) để định danh hóa đơn.");
            case 2 -> lblGoiY.setText("Nhập mã phiếu tiếp nhận hoặc phiếu sửa chữa liên quan.");
            case 3 -> lblGoiY.setText("Nhập biển số xe để xem các hóa đơn của xe.");
            case 4 -> lblGoiY.setText("Nhập tên khách hàng, số điện thoại hoặc mã khách hàng.");
            case 5 -> lblGoiY.setText("Nhập tên nhân viên thu ngân đã lập hóa đơn.");
            default -> lblGoiY.setText("Tìm nhanh theo mã hóa đơn, phiếu, biển số, khách hàng hoặc thu ngân.");
        }
        txtTuKhoa.setToolTipText(lblGoiY.getText());
    }

    private void lamMoiTimKiem() {
        txtTuKhoa.setText("");
        cboTieuChi.setSelectedIndex(0);
        cboPhuongThuc.setSelectedIndex(0);
        locTrangThai = "TAT_CA";
        capNhatChip();
        napBang();
    }

    private void dinhDangCot(int cot, int canLe, int doRong) {
        TableColumn column = table.getColumnModel().getColumn(cot);
        column.setPreferredWidth(doRong);
        int left = canLe == SwingConstants.LEFT ? 12 : 4;
        int right = canLe == SwingConstants.RIGHT ? 12 : 4;
        column.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(canLe);
                l.setBackground(new Color(241, 245, 249));
                l.setForeground(new Color(30, 41, 59));
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(8, left, 8, right)));
                return l;
            }
        });
        column.setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean selected, boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, selected, focus, r, c);
                comp.setBackground(selected ? new Color(219, 234, 254) : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                comp.setForeground(selected ? UiHelper.XANH_TRUNG : new Color(30, 41, 59));
                if (comp instanceof JLabel l) {
                    l.setHorizontalAlignment(canLe);
                    l.setBorder(new EmptyBorder(0, left, 0, right));
                    if (cot == 10) l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
                return comp;
            }
        });
    }

    private void dinhDangCotTrangThai(int cot, int doRong) {
        TableColumn column = table.getColumnModel().getColumn(cot);
        column.setPreferredWidth(doRong);
        column.setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean selected, boolean focus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, selected, focus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBackground(selected ? new Color(219, 234, 254) : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                String status = v == null ? "" : v.toString();
                l.setForeground(selected ? UiHelper.XANH_TRUNG : (status.contains("Đã hủy") ? new Color(220, 38, 38) : new Color(22, 163, 74)));
                return l;
            }
        });
    }

    private void napBang() {
        String method = (String) cboPhuongThuc.getSelectedItem();
        dsHienTai = hoaDonDAO.traCuuHoaDon(txtTuKhoa.getText().trim(), cboTieuChi.getSelectedIndex(),
                locTrangThai, method != null ? method : "TAT_CA");
        model.setRowCount(0);
        for (HoaDon hd : dsHienTai) {
            model.addRow(new Object[]{hd.getMaHD(), hd.getSoPhieuSC(), hd.getTenKH(), hd.getSdtKH(), hd.getBienSo(),
                    hd.getNgayThanhToan(), hd.getPhuongThuc(), UiHelper.tien(hd.getTienDichVu()),
                    UiHelper.tien(hd.getTienPhuTung()), hd.getGiamGiaPhanTram() + "%", UiHelper.tien(hd.getTongTien()),
                    hd.getNguoiThu(), hd.getTrangThai()});
        }
        int total = hoaDonDAO.layTongSoHoaDon();
        lblDemKetQua.setText("Hiển thị: " + dsHienTai.size() + " / " + total + " hóa đơn");
    }

    private void xemChiTiet() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= dsHienTai.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong bảng để xem chi tiết.", "Chưa chọn hóa đơn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        HoaDon hd = dsHienTai.get(row);
        ArrayList<ChiTietDichVu> chiTiet = hoaDonDAO.layChiTietTheoHoaDon(hd.getMaHD());
        if (chiTiet.isEmpty()) chiTiet = new ArrayList<>(kho.layChiTietTheoHoaDon(hd.getMaHD()));
        StringBuilder rows = new StringBuilder();
        for (ChiTietDichVu ct : chiTiet) {
            rows.append("<tr><td>").append(escape(ct.getLoai())).append("</td><td>").append(escape(ct.getTenHangMuc()))
                    .append("</td><td align='right'>").append(ct.getSoLuong()).append("</td><td align='right'>")
                    .append(UiHelper.tien(ct.getDonGia())).append(" đ</td><td align='right'>")
                    .append(UiHelper.tien(ct.getThanhTien())).append(" đ</td></tr>");
        }
        if (rows.length() == 0) rows.append("<tr><td colspan='5'>Không có dữ liệu chi tiết hạng mục.</td></tr>");
        String html = "<html><div style='width:560px;font-family:Segoe UI;font-size:12px'>"
                + "<h3 style='color:#0F172A'>CHI TIẾT HÓA ĐƠN</h3>"
                + "<table cellpadding='4'>"
                + dong("Mã hóa đơn", hd.getMaHD()) + dong("Số phiếu", hd.getSoPhieuSC())
                + dong("Ngày lập", hd.getNgayThanhToan()) + dong("Trạng thái", hd.getTrangThai())
                + dong("Khách hàng", hd.getTenKH()) + dong("Số điện thoại", hd.getSdtKH())
                + dong("Biển số xe", hd.getBienSo()) + dong("Thu ngân", hd.getNguoiThu())
                + dong("Phương thức", hd.getPhuongThuc()) + "</table><hr>"
                + "<b>Chi tiết hạng mục</b><table border='1' cellspacing='0' cellpadding='5' width='100%'>"
                + "<tr bgcolor='#F1F5F9'><th>Loại</th><th>Hạng mục</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>"
                + rows + "</table><br><table cellpadding='4'>"
                + dong("Tiền công", UiHelper.tien(hd.getTienDichVu()) + " đ")
                + dong("Tiền phụ tùng", UiHelper.tien(hd.getTienPhuTung()) + " đ")
                + dong("Giảm giá", hd.getGiamGiaPhanTram() + "%")
                + dong("Tổng thanh toán", UiHelper.tien(hd.getTongTien()) + " đ")
                + dong("Ghi chú", hd.getGhiChu()) + "</table></div></html>";
        JOptionPane.showMessageDialog(this, html, "Hóa đơn " + hd.getMaHD(), JOptionPane.INFORMATION_MESSAGE);
    }

    private String dong(String label, String value) {
        return "<tr><td width='145'><b>" + escape(label) + ":</b></td><td>" + escape(value) + "</td></tr>";
    }

    private String escape(String value) {
        if (value == null || value.isBlank()) return "—";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void chuyenTrangLapHoaDon() {
        Main_UI main = Main_UI.getInstance();
        if (main != null) main.chuyenTrang("Lập hóa đơn");
        else JOptionPane.showMessageDialog(this, "Không tìm thấy giao diện chính.");
    }
}
