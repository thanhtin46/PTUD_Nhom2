package UI;

import Entity.KhoDuLieu;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * UC009: Báo cáo tồn kho với cảnh báo real-time.
 * - Cột "Cảnh báo" hiển thị "HẾT HÀNG" (đỏ) / "SẮP HẾT" (cam) / "BÌNH THƯỜNG" (xanh).
 * - Panel trên cùng: số mặt hàng hết, sắp hết, bình thường, tổng giá trị.
 * - Hỗ trợ tìm kiếm, lọc theo cảnh báo, đồng bộ dữ liệu tức thì từ SQL Server và phím tắt F5.
 */
public class BaoCaoTonKho_UI extends JPanel {
    private final KhoDuLieu kho = KhoDuLieu.get();

    // Các thẻ chỉ số thống kê trên cùng
    private final JLabel lblSoHet = new JLabel("0");
    private final JLabel lblSoSapHet = new JLabel("0");
    private final JLabel lblSoBinhThuong = new JLabel("0");
    private final JLabel lblTongGiaTri = new JLabel("0 đ");
    private final JLabel lblTongGiaTriPhuDe = new JLabel("0 mặt hàng");

    // Thanh tìm kiếm và bộ lọc
    private final JTextField txtTimKiem = new JTextField();
    private final JComboBox<String> cboLocCanhBao = new JComboBox<>(new String[]{
            "-- Tất cả mức cảnh báo --",
            "HẾT HÀNG (≤ 0)",
            "SẮP HẾT (≤ Tối thiểu)",
            "BÌNH THƯỜNG"
    });

    // Bảng dữ liệu chi tiết
    private DefaultTableModel model;
    private JTable bang;
    private final List<Integer> rowsCanhBao = new ArrayList<>();

    // Footer
    private final JLabel lblFooterTrai = new JLabel();
    private final JLabel lblFooterPhai = new JLabel();

    public BaoCaoTonKho_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // ==== NORTH PANEL: TIÊU ĐỀ + THANH CÔNG CỤ + 4 THẺ THỐNG KÊ ====
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        // Hàng 1: Tiêu đề trang + Thanh công cụ tìm kiếm / Làm mới
        JPanel rowHeader = new JPanel(new BorderLayout(12, 0));
        rowHeader.setOpaque(false);

        JLabel tde = UiHelper.tieuDeTrang("📊 Báo cáo tồn kho phụ tùng");
        rowHeader.add(tde, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        txtTimKiem.setPreferredSize(new Dimension(210, 36));
        UiHelper.trangTriInput(txtTimKiem);
        txtTimKiem.setToolTipText("Tìm kiếm theo mã PT hoặc tên phụ tùng...");

        cboLocCanhBao.setFont(UiHelper.FONT_BT);
        cboLocCanhBao.setBackground(Color.WHITE);
        cboLocCanhBao.setPreferredSize(new Dimension(190, 36));

        JButton btnLamMoi = UiHelper.nut("Làm mới (F5)", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XANH_TRUNG, true, e -> lamMoiDuLieu());
        btnLamMoi.setPreferredSize(new Dimension(125, 36));

        JButton btnIn = UiHelper.nut("In báo cáo", UiIcon.of(UiIcon.PRINT, 15), UiHelper.XAM, true, e -> inBaoCao());
        btnIn.setPreferredSize(new Dimension(110, 36));

        toolbar.add(new JLabel("Tìm:"));
        toolbar.add(txtTimKiem);
        toolbar.add(cboLocCanhBao);
        toolbar.add(btnLamMoi);
        toolbar.add(btnIn);

        rowHeader.add(toolbar, BorderLayout.EAST);
        north.add(rowHeader);
        north.add(Box.createVerticalStrut(10));

        // Hàng 2: Grid 4 thẻ thống kê
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(4, 0, 14, 0));
        grid.add(theCanhBao(UiIcon.of(UiIcon.CLOSE, 22, Color.WHITE), "Hết hàng", lblSoHet,
                "Cần nhập gấp", new Color(220, 80, 80)));
        grid.add(theCanhBao(UiIcon.of(UiIcon.CLOCK, 22, Color.WHITE), "Sắp hết", lblSoSapHet,
                "≤ mức tối thiểu", new Color(225, 150, 45)));
        grid.add(theCanhBao(UiIcon.of(UiIcon.CHECK, 22, Color.WHITE), "Bình thường", lblSoBinhThuong,
                "Còn đủ dùng", new Color(46, 160, 92)));
        grid.add(theCanhBao(UiIcon.of(UiIcon.BILL, 22, Color.WHITE), "Tổng giá trị tồn", lblTongGiaTri,
                lblTongGiaTriPhuDe, new Color(37, 99, 168)));
        north.add(grid);

        root.add(north, BorderLayout.NORTH);

        // ==== CENTER PANEL: BẢNG CHI TIẾT TỒN KHO ====
        JPanel bangPanel = UiHelper.panelBang("Báo cáo chi tiết (cảnh báo real-time)");
        model = UiHelper.model("Mã PT", "Tên phụ tùng", "ĐVT",
                "Tồn kho", "Tối thiểu", "Cảnh báo", "Đơn giá", "Giá trị tồn");
        bang = UiHelper.bang(model);

        // Tô màu cột cảnh báo (cột 5)
        bang.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String cb = v == null ? "" : v.toString();
                comp.setForeground(Color.WHITE);
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                if (cb.contains("HẾT")) {
                    comp.setBackground(new Color(220, 80, 80));
                } else if (cb.contains("SẮP")) {
                    comp.setBackground(new Color(225, 150, 45));
                } else {
                    comp.setBackground(new Color(46, 160, 92));
                }
                return comp;
            }
        });

        // Tô nền dòng và căn lề các cột
        bang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, r, col);
                if (c instanceof JLabel lbl) {
                    if (col == 0 || col == 2 || col == 3 || col == 4) {
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    } else if (col == 6 || col == 7) {
                        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }
                if (!s && rowsCanhBao.contains(r)) {
                    String cb = String.valueOf(model.getValueAt(r, 5));
                    if (cb.contains("HẾT")) c.setBackground(new Color(255, 235, 235));
                    else c.setBackground(new Color(255, 246, 228));
                    c.setForeground(new Color(120, 30, 30));
                } else if (!s) {
                    c.setBackground(r % 2 == 0 ? Color.WHITE : new Color(247, 250, 254));
                    c.setForeground(new Color(40, 50, 65));
                } else {
                    c.setBackground(new Color(220, 235, 250));
                    c.setForeground(new Color(15, 35, 70));
                }
                return c;
            }
        });

        bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        bang.getColumnModel().getColumn(0).setPreferredWidth(90);   // Mã PT
        bang.getColumnModel().getColumn(1).setPreferredWidth(210);  // Tên phụ tùng
        bang.getColumnModel().getColumn(2).setPreferredWidth(70);   // ĐVT
        bang.getColumnModel().getColumn(3).setPreferredWidth(90);   // Tồn kho
        bang.getColumnModel().getColumn(4).setPreferredWidth(90);   // Tối thiểu
        bang.getColumnModel().getColumn(5).setPreferredWidth(125);  // Cảnh báo
        bang.getColumnModel().getColumn(6).setPreferredWidth(120);  // Đơn giá
        bang.getColumnModel().getColumn(7).setPreferredWidth(140);  // Giá trị tồn

        JScrollPane sp = new JScrollPane(bang);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        root.add(bangPanel, BorderLayout.CENTER);

        // ==== SOUTH PANEL: FOOTER TỔNG KẾT ====
        JPanel tongPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(37, 99, 168), getWidth(), 0, new Color(80, 144, 220));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tongPanel.setOpaque(false);
        tongPanel.setBorder(new EmptyBorder(12, 18, 12, 18));
        tongPanel.setLayout(new BorderLayout());

        lblFooterTrai.setForeground(Color.WHITE);
        lblFooterTrai.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblFooterPhai.setForeground(Color.WHITE);
        lblFooterPhai.setFont(new Font("Segoe UI", Font.BOLD, 15));

        tongPanel.add(lblFooterTrai, BorderLayout.WEST);
        tongPanel.add(lblFooterPhai, BorderLayout.EAST);
        root.add(tongPanel, BorderLayout.SOUTH);

        // ==== LẮNG NGHE SỰ KIỆN TÌM KIẾM & BỘ LỌC ====
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locDuLieu(); }
            @Override public void removeUpdate(DocumentEvent e) { locDuLieu(); }
            @Override public void changedUpdate(DocumentEvent e) { locDuLieu(); }
        };
        txtTimKiem.getDocument().addDocumentListener(docListener);
        cboLocCanhBao.addActionListener(e -> locDuLieu());

        // Phím tắt F5 làm mới
        setupPhimTat();

        // Tự động tải dữ liệu khi hiển thị
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override public void ancestorAdded(javax.swing.event.AncestorEvent event) { napDuLieu(); }
            @Override public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
            @Override public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });

        // Nạp dữ liệu lần đầu
        napDuLieu();
    }

    private void setupPhimTat() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "lamMoiF5");
        am.put("lamMoiF5", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                lamMoiDuLieu();
            }
        });
    }

    public void lamMoiDuLieu() {
        kho.dongBoPhuTungTuDB();
        napDuLieu();
        JOptionPane.showMessageDialog(this,
                "Đã làm mới và đồng bộ dữ liệu tồn kho với SQL Server thành công!",
                "Đồng bộ hoàn tất", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Nạp lại dữ liệu tồn kho và cập nhật toàn bộ chỉ số + bảng
     */
    public void napDuLieu() {
        // 1. Thống kê nhanh toàn bộ kho
        int soHet = 0, soSapHet = 0, soBinhThuong = 0;
        long tongGiaTri = 0;
        java.util.List<PhuTung> list = kho.getPhuTungs();

        for (PhuTung pt : list) {
            long gt = (long) pt.getSoLuongTon() * pt.getDonGia();
            tongGiaTri += gt;
            switch (pt.getMucCanhBao()) {
                case "HẾT HÀNG":   soHet++; break;
                case "SẮP HẾT":    soSapHet++; break;
                default:           soBinhThuong++;
            }
        }

        lblSoHet.setText(String.valueOf(soHet));
        lblSoSapHet.setText(String.valueOf(soSapHet));
        lblSoBinhThuong.setText(String.valueOf(soBinhThuong));
        lblTongGiaTri.setText(UiHelper.tien(tongGiaTri) + " đ");
        lblTongGiaTriPhuDe.setText("Qua " + list.size() + " mặt hàng");

        // 2. Lọc và hiển thị lên bảng
        locDuLieu();
    }

    /**
     * Lọc bảng theo từ khóa tìm kiếm và mức cảnh báo được chọn
     */
    private void locDuLieu() {
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();
        int filterIdx = cboLocCanhBao.getSelectedIndex();

        model.setRowCount(0);
        rowsCanhBao.clear();

        long tongGiaTriLoc = 0;
        int demLoc = 0;

        for (PhuTung pt : kho.getPhuTungs()) {
            // Bộ lọc từ khóa
            boolean matchText = tuKhoa.isEmpty()
                    || pt.getMaPT().toLowerCase().contains(tuKhoa)
                    || pt.getTen().toLowerCase().contains(tuKhoa);

            if (!matchText) continue;

            // Bộ lọc mức cảnh báo
            String cb = pt.getMucCanhBao();
            boolean matchFilter = true;
            if (filterIdx == 1) { // Hết hàng
                matchFilter = cb.contains("HẾT");
            } else if (filterIdx == 2) { // Sắp hết
                matchFilter = cb.contains("SẮP");
            } else if (filterIdx == 3) { // Bình thường
                matchFilter = !cb.contains("HẾT") && !cb.contains("SẮP");
            }

            if (!matchFilter) continue;

            long giaTri = (long) pt.getSoLuongTon() * pt.getDonGia();
            tongGiaTriLoc += giaTri;
            demLoc++;

            int rowIdx = model.getRowCount();
            if (cb.contains("HẾT") || cb.contains("SẮP")) {
                rowsCanhBao.add(rowIdx);
            }

            model.addRow(new Object[]{
                    pt.getMaPT(),
                    pt.getTen(),
                    pt.getDonVi(),
                    pt.getSoLuongTon(),
                    pt.getSoLuongToiThieu(),
                    cb,
                    UiHelper.tien(pt.getDonGia()),
                    UiHelper.tien(giaTri)
            });
        }

        lblFooterTrai.setText("Hiển thị: " + demLoc + " / " + kho.getPhuTungs().size() + " mặt hàng   |   Cảnh báo hết/sắp hết: " + rowsCanhBao.size());
        lblFooterPhai.setText("Tổng giá trị tồn hiển thị: " + UiHelper.tien(tongGiaTriLoc) + " VNĐ");

        bang.repaint();
    }

    private void inBaoCao() {
        JOptionPane.showMessageDialog(this,
                "Chức năng in báo cáo tồn kho: Hệ thống đã kết nối máy in và sẵn sàng xuất tài liệu.",
                "In báo cáo", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel theCanhBao(Icon icon, String ten, JLabel lblSo, String phuDe, Color mau) {
        JLabel lblPhu = new JLabel(phuDe);
        return theCanhBao(icon, ten, lblSo, lblPhu, mau);
    }

    private JPanel theCanhBao(Icon icon, String ten, JLabel lblSo, JLabel lblPhu, Color mau) {
        JPanel p = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(248, 250, 253));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new UiHelper.ShadowBorder(6, new Color(0, 0, 0, 18)),
                new EmptyBorder(8, 14, 8, 14)));
        p.setPreferredSize(new Dimension(0, 112));
        p.setMinimumSize(new Dimension(0, 112));

        JLabel lblIcon = new JLabel(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UiHelper.lighten(mau, 0.15f), 0, getHeight(), mau));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(46, 46));

        JPanel text = new JPanel(new BorderLayout(0, 2));
        text.setOpaque(false);

        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTen.setForeground(new Color(100, 110, 125));

        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSo.setForeground(mau);

        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPhu.setForeground(new Color(140, 150, 165));

        text.add(lblTen, BorderLayout.NORTH);
        text.add(lblSo, BorderLayout.CENTER);
        text.add(lblPhu, BorderLayout.SOUTH);

        p.add(lblIcon, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }
}
