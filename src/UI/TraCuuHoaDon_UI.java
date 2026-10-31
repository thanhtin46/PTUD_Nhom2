package UI;

import DAO.HoaDon_DAO;
import Entity.CauHinhHeThong;
import Entity.ChiTietDichVu;
import Entity.HoaDon;
import Entity.KhoDuLieu;
import Entity.NhanVien;

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
import java.awt.print.PrinterException;
import java.util.ArrayList;

/**
 * Màn hình tra cứu và quản lý danh sách hóa đơn thanh toán.
 */
public class TraCuuHoaDon_UI extends JPanel {
    private final HoaDon_DAO hoaDonDAO = new HoaDon_DAO();
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final CauHinhHeThong cauHinh = kho.getCauHinh();

    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí (Tự động)", "Mã hóa đơn", "Mã khách hàng",
            "Biển số xe", "Khách hàng (Tên / SĐT)", "Thu ngân"
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

    public TraCuuHoaDon_UI(NhanVien nv) {
        this();
    }

    public TraCuuHoaDon_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header trang
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        JLabel title = UiHelper.tieuDeTrang("Tra cứu hóa đơn");
        JLabel subtitle = new JLabel("Tra cứu, xem chi tiết và theo dõi lịch sử thanh toán hóa đơn dịch vụ");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        north.add(title);
        north.add(Box.createVerticalStrut(4));
        north.add(subtitle);
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        // Khung bảng kết quả
        JPanel bangPanel = UiHelper.panelBang("Danh sách hóa đơn");
        bangPanel.add(taoKhungTimKiem(), BorderLayout.NORTH);

        model = UiHelper.model(
                "Mã hóa đơn", "Mã KH", "Khách hàng", "Số ĐT", "Biển số xe",
                "Ngày lập", "Phương thức", "Tiền công", "Phụ tùng", "Giảm giá",
                "Tổng tiền", "Thu ngân", "Trạng thái"
        );
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Căn chỉnh các cột thẳng hàng giữa Tiêu đề và Dữ liệu
        dinhDangCot(0, SwingConstants.CENTER, 100);  // Mã hóa đơn
        dinhDangCot(1, SwingConstants.CENTER, 95);   // Mã KH
        dinhDangCot(2, SwingConstants.LEFT, 150);    // Khách hàng
        dinhDangCot(3, SwingConstants.CENTER, 105);  // Số ĐT
        dinhDangCot(4, SwingConstants.CENTER, 110);  // Biển số xe
        dinhDangCot(5, SwingConstants.CENTER, 100);  // Ngày lập
        dinhDangCot(6, SwingConstants.LEFT, 140);    // Phương thức
        dinhDangCot(7, SwingConstants.RIGHT, 115);   // Tiền công
        dinhDangCot(8, SwingConstants.RIGHT, 115);   // Phụ tùng
        dinhDangCot(9, SwingConstants.CENTER, 80);   // Giảm giá
        dinhDangCot(10, SwingConstants.RIGHT, 130);  // Tổng tiền
        dinhDangCot(11, SwingConstants.LEFT, 135);   // Thu ngân
        dinhDangCotTrangThai(12, 125);               // Trạng thái

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

        // Footer thao tác
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 4, 4));

        lblDemKetQua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDemKetQua.setForeground(new Color(71, 85, 105));
        footer.add(lblDemKetQua, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnXemChiTiet = UiHelper.nut("Xem chi tiết", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> xemChiTiet());
        JButton btnInLai = UiHelper.nut("In hóa đơn", UiIcon.of(UiIcon.PRINT, 15), UiHelper.XANH_TRUNG, true, e -> inLaiHoaDon());
        JButton btnLapMoi = UiHelper.nut("Lập hóa đơn mới", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> chuyenTrangLapHoaDon());

        actions.add(btnXemChiTiet);
        actions.add(btnInLai);
        actions.add(btnLapMoi);
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

        // Hàng 1: Tiêu chí tìm kiếm + Ô nhập từ khóa + Nút tìm kiếm & Làm mới
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setOpaque(false);

        JPanel criteria = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        criteria.setOpaque(false);
        JLabel label = new JLabel("Tìm theo:");
        label.setFont(UiHelper.FONT_NHAN);
        label.setForeground(new Color(51, 65, 85));
        cboTieuChi.setPreferredSize(new Dimension(240, 38));
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

        // Hàng 2: Bộ lọc nhanh Trạng thái (Hover không mờ chữ, chữ đen giữ nguyên) + Lọc Phương thức
        JPanel row2 = new JPanel(new BorderLayout(8, 0));
        row2.setOpaque(false);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        chips.add(statusLabel);

        chipTatCa = taoChipButton("Tất cả", "TAT_CA");
        chipDaThanhToan = taoChipButton("Đã thanh toán", "Đã thanh toán");
        chipDaHuy = taoChipButton("Đã hủy", "Đã hủy");

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

        // Hộp gợi ý & hướng dẫn người dùng
        JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(191, 219, 254));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hint.setOpaque(false);
        hint.setBorder(new EmptyBorder(4, 10, 4, 10));

        lblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGoiY.setForeground(new Color(30, 64, 175));
        hint.add(lblGoiY);
        p.add(hint);

        return p;
    }

    /**
     * Tạo nút lọc nhanh theo trạng thái.
     */
    private JButton taoChipButton(String text, String value) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = value.equalsIgnoreCase(locTrangThai);
                boolean hover = getModel().isRollover();

                Color bg;
                Color border;
                if (active) {
                    bg = hover ? new Color(219, 234, 254) : new Color(239, 246, 255);
                    border = new Color(59, 130, 246);
                } else {
                    bg = hover ? new Color(241, 245, 249) : Color.WHITE;
                    border = hover ? new Color(148, 163, 184) : new Color(203, 213, 225);
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(border);
                g2.setStroke(new BasicStroke(active ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.dispose();

                setForeground(Color.BLACK);
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.BLACK);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setRolloverEnabled(true);

        btn.addActionListener(e -> {
            this.locTrangThai = value;
            repaint();
            napBang();
        });
        return btn;
    }

    private void capNhatGoiY() {
        switch (cboTieuChi.getSelectedIndex()) {
            case 1 -> lblGoiY.setText("Nhập mã hóa đơn (VD: HD001, HD002) để định danh chính xác.");
            case 2 -> lblGoiY.setText("Nhập mã phiếu tiếp nhận hoặc phiếu sửa chữa liên quan.");
            case 3 -> lblGoiY.setText("Nhập biển số xe (VD: 51A-123.45) để tìm các hóa đơn của xe.");
            case 4 -> lblGoiY.setText("Nhập họ tên khách hàng hoặc số điện thoại liên hệ.");
            case 5 -> lblGoiY.setText("Nhập tên nhân viên thu ngân đã lập hóa đơn.");
            default -> lblGoiY.setText("Tìm nhanh theo mã hóa đơn, phiếu sửa chữa, biển số xe, khách hàng hoặc thu ngân.");
        }
        txtTuKhoa.setToolTipText(lblGoiY.getText());
    }

    private void lamMoiTimKiem() {
        txtTuKhoa.setText("");
        cboTieuChi.setSelectedIndex(0);
        cboPhuongThuc.setSelectedIndex(0);
        locTrangThai = "TAT_CA";
        repaint();
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
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
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
                    if (cot == 10) {
                        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        l.setForeground(selected ? UiHelper.XANH_TRUNG : new Color(220, 38, 38));
                    }
                }
                return comp;
            }
        });
    }

    private void dinhDangCotTrangThai(int cot, int doRong) {
        TableColumn column = table.getColumnModel().getColumn(cot);
        column.setPreferredWidth(doRong);

        column.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBackground(new Color(241, 245, 249));
                l.setForeground(new Color(30, 41, 59));
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(8, 4, 8, 4)));
                return l;
            }
        });

        column.setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean selected, boolean focus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, selected, focus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBackground(selected ? new Color(219, 234, 254) : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                String status = v == null ? "" : v.toString();
                if (status.contains("Đã hủy")) {
                    l.setForeground(new Color(220, 38, 38));
                } else {
                    l.setForeground(new Color(22, 163, 74));
                }
                return l;
            }
        });
    }

    private void napBang() {
        String method = (String) cboPhuongThuc.getSelectedItem();
        dsHienTai = hoaDonDAO.traCuuHoaDon(txtTuKhoa.getText().trim(), cboTieuChi.getSelectedIndex(),
                locTrangThai, method != null ? method : "TAT_CA");
        model.setRowCount(0);
        long tongTienDoanhThu = 0;

        for (HoaDon hd : dsHienTai) {
            String maKH = (hd.getKhachHang() != null) ? hd.getKhachHang().getMaKH() : "";
            String tenKH = (hd.getKhachHang() != null) ? hd.getKhachHang().getHoTen() : "";
            String sdtKH = (hd.getKhachHang() != null) ? hd.getKhachHang().getSdt() : "";
            String bienSo = (hd.getPhieuSuaChua() != null && hd.getPhieuSuaChua().getPhuongTien() != null)
                    ? hd.getPhieuSuaChua().getPhuongTien().getBienSo()
                    : hd.getBienSo();
            String nguoiThu = (hd.getNhanVien() != null) ? hd.getNhanVien().getHoTen() : hd.getNguoiThu();

            model.addRow(new Object[]{
                    hd.getMaHD(),
                    maKH,
                    tenKH,
                    sdtKH,
                    bienSo,
                    hd.getNgayLapHienThi(),
                    hd.getPhuongThuc(),
                    UiHelper.tien(hd.getTienDichVu()),
                    UiHelper.tien(hd.getTienPhuTung()),
                    hd.getGiamGiaPhanTram() + "%",
                    UiHelper.tien(hd.getTongTien()),
                    nguoiThu,
                    hd.getTrangThai()
            });
            if (HoaDon.DA_THANH_TOAN.equals(hd.getTrangThai())) {
                tongTienDoanhThu += hd.getTongTien();
            }
        }
        int total = hoaDonDAO.layTongSoHoaDon();
        lblDemKetQua.setText("Hiển thị: " + dsHienTai.size() + " / " + total + " hóa đơn | Tổng doanh thu: " + UiHelper.tien(tongTienDoanhThu) + " đ");
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
                    .append("</td><td align='center'>").append(ct.getSoLuong()).append("</td><td align='right'>")
                    .append(UiHelper.tien(ct.getDonGia())).append(" đ</td><td align='right'>")
                    .append(UiHelper.tien(ct.getThanhTien())).append(" đ</td></tr>");
        }
        if (rows.length() == 0) rows.append("<tr><td colspan='5' align='center'>Không có dữ liệu chi tiết hạng mục.</td></tr>");

        String html = "<html><div style='width:580px;font-family:Segoe UI;font-size:12px'>"
                + "<h3 style='color:#0F172A;margin-bottom:8px;'>CHI TIẾT HÓA ĐƠN: " + hd.getMaHD() + "</h3>"
                + "<table cellpadding='4' width='100%'>"
                + dong("Mã hóa đơn", hd.getMaHD()) + dong("Mã khách hàng", hd.getMaKH())
                + dong("Ngày thanh toán", hd.getNgayLapHienThi()) + dong("Trạng thái", hd.getTrangThai())
                + dong("Khách hàng", hd.getTenKH()) + dong("Số điện thoại", hd.getSdtKH())
                + dong("Biển số xe", hd.getBienSo()) + dong("Thu ngân", hd.getNguoiThu())
                + dong("Phương thức", hd.getPhuongThuc()) + "</table><hr>"
                + "<b style='color:#1E3A8A;'>Bảng kê chi tiết hạng mục dịch vụ & phụ tùng</b>"
                + "<table border='1' cellspacing='0' cellpadding='5' width='100%' style='border-collapse:collapse;margin-top:6px;'>"
                + "<tr bgcolor='#F1F5F9'><th>Loại</th><th>Hạng mục</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>"
                + rows + "</table><br><table cellpadding='4' width='100%'>"
                + dong("Tiền công", UiHelper.tien(hd.getTienDichVu()) + " đ")
                + dong("Tiền phụ tùng", UiHelper.tien(hd.getTienPhuTung()) + " đ")
                + dong("Giảm giá", hd.getGiamGiaPhanTram() + "%")
                + "<tr><td width='155'><b style='color:#1E3A8A;'>TỔNG THANH TOÁN:</b></td><td><b style='color:#DC2626;font-size:14px;'>" + UiHelper.tien(hd.getTongTien()) + " đ</b></td></tr>"
                + dong("Ghi chú", hd.getGhiChu()) + "</table></div></html>";

        JOptionPane.showMessageDialog(this, html, "Chi tiết hóa đơn " + hd.getMaHD(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void inLaiHoaDon() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= dsHienTai.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong bảng để in!", "Chưa chọn hóa đơn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        HoaDon hd = dsHienTai.get(row);
        ArrayList<ChiTietDichVu> chiTiet = hoaDonDAO.layChiTietTheoHoaDon(hd.getMaHD());
        if (chiTiet.isEmpty()) chiTiet = new ArrayList<>(kho.layChiTietTheoHoaDon(hd.getMaHD()));

        StringBuilder sbItems = new StringBuilder();
        int stt = 1;
        for (ChiTietDichVu ct : chiTiet) {
            sbItems.append("<tr>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(stt++).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;'>").append(ct.getTenHangMuc()).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(ct.getDonVi() != null ? ct.getDonVi() : "Lần").append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(ct.getSoLuong()).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:right;'>").append(UiHelper.tien(ct.getDonGia())).append(" đ</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:right;font-weight:bold;'>").append(UiHelper.tien(ct.getThanhTien())).append(" đ</td>")
                    .append("</tr>");
        }
        if (sbItems.length() == 0) {
            sbItems.append("<tr><td colspan='6' style='text-align:center;padding:12px;color:#94A3B8;'>Không có dữ liệu chi tiết hạng mục</td></tr>");
        }

        String html = "<html><body style='font-family:Segoe UI, sans-serif;font-size:12px;color:#0F172A;padding:15px;'>"
                + "<div style='text-align:center;border-bottom:2px solid #2563EB;padding-bottom:10px;'>"
                + "<h2 style='margin:0;color:#1E3A8A;'>" + cauHinh.getTenGara() + "</h2>"
                + "<p style='margin:3px 0;color:#475569;'>" + cauHinh.getDiaChi() + " | Hotline: " + cauHinh.getHotline() + "</p>"
                + "<h3 style='margin:10px 0 2px 0;color:#0F172A;letter-spacing:1px;'>HÓA ĐƠN THANH TOÁN DỊCH VỤ</h3>"
                + "<p style='margin:0;font-size:11px;color:#64748B;'>Số: <b style='color:#DC2626;'>" + hd.getMaHD() + "</b> | Ngày: " + hd.getNgayLapHienThi() + "</p>"
                + "</div>"
                + "<table style='width:100%;margin-top:12px;font-size:12px;' cellpadding='3'>"
                + "<tr><td width='50%'><b>Khách hàng:</b> " + hd.getTenKH() + "</td><td><b>Biển số xe:</b> " + hd.getBienSo() + "</td></tr>"
                + "<tr><td><b>Điện thoại:</b> " + hd.getSdtKH() + "</td><td><b>Mã khách hàng:</b> " + hd.getMaKH() + "</td></tr>"
                + "<tr><td><b>Phương thức:</b> " + hd.getPhuongThuc() + "</td><td><b>Thu ngân:</b> " + hd.getNguoiThu() + "</td></tr>"
                + "</table>"
                + "<h4 style='margin:14px 0 6px 0;'>CHI TIẾT HẠNG MỤC DỊCH VỤ & PHỤ TÙNG</h4>"
                + "<table style='width:100%;border-collapse:collapse;font-size:11px;'>"
                + "<tr style='background:#F1F5F9;font-weight:bold;'>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:35px;'>STT</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;text-align:left;'>Tên hạng mục / Phụ tùng</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:55px;'>ĐVT</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:45px;'>SL</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:95px;'>Đơn giá</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:105px;'>Thành tiền</th>"
                + "</tr>"
                + sbItems
                + "</table>"
                + "<table style='width:100%;margin-top:10px;font-size:12px;' cellpadding='4'>"
                + "<tr><td align='right' width='70%'>Tiền dịch vụ / Tiền công:</td><td align='right'><b>" + UiHelper.tien(hd.getTienDichVu()) + " đ</b></td></tr>"
                + "<tr><td align='right'>Tiền vật tư / Phụ tùng:</td><td align='right'><b>" + UiHelper.tien(hd.getTienPhuTung()) + " đ</b></td></tr>"
                + "<tr><td align='right'>Giảm giá chiết khấu:</td><td align='right'><b>" + hd.getGiamGiaPhanTram() + "%</b></td></tr>"
                + "<tr style='background:#EFF6FF;'><td align='right'><b style='color:#1E3A8A;font-size:14px;'>TỔNG THANH TOÁN:</b></td>"
                + "<td align='right'><b style='color:#DC2626;font-size:16px;'>" + UiHelper.tien(hd.getTongTien()) + " đ</b></td></tr>"
                + "<tr><td colspan='2' style='font-style:italic;color:#475569;'>Bằng chữ: <b>" + ThanhToanHoaDon_UI.docSoThanhChu(hd.getTongTien()) + "</b></td></tr>"
                + "</table>"
                + "<table style='width:100%;margin-top:20px;text-align:center;font-size:12px;'>"
                + "<tr><td width='50%'><b>Khách hàng</b><br><span style='font-size:10px;color:#94A3B8;'>(Ký, ghi rõ họ tên)</span><br><br><br><br><b>" + hd.getTenKH() + "</b></td>"
                + "<td width='50%'><b>Người lập phiếu</b><br><span style='font-size:10px;color:#94A3B8;'>(Ký, đóng dấu)</span><br><br><br><br><b>" + hd.getNguoiThu() + "</b></td></tr>"
                + "</table>"
                + "</body></html>";

        JEditorPane editor = new JEditorPane("text/html", html);
        editor.setEditable(false);
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setPreferredSize(new Dimension(620, 560));

        Object[] options = {"Đóng"};
        JOptionPane.showOptionDialog(this, scroll, "Xem chi tiết hóa đơn - " + hd.getMaHD(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    }

    private String dong(String label, String value) {
        return "<tr><td width='155'><b>" + escape(label) + ":</b></td><td>" + escape(value) + "</td></tr>";
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
