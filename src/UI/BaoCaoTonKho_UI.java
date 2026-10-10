package UI;

import Entity.KhoDuLieu;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UC009: Báo cáo tồn kho với cảnh báo real-time.
 * - Cột "Cảnh báo" hiển thị "HẾT HÀNG" (đỏ) / "SẮP HẾT" (cam) / "BÌNH THƯỜNG" (xanh).
 * - Panel trên cùng: số mặt hàng hết, sắp hết, tổng giá trị.
 */
public class BaoCaoTonKho_UI extends JPanel {
    public BaoCaoTonKho_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("📊 Báo cáo tồn kho");
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        root.add(north, BorderLayout.NORTH);

        // ==== Thống kê nhanh ====
        KhoDuLieu kho = KhoDuLieu.get();
        int soHet = 0, soSapHet = 0, soBinhThuong = 0;
        long tongGiaTri = 0;
        for (PhuTung pt : kho.getPhuTungs()) {
            long gt = pt.getSoLuongTon() * pt.getDonGia();
            tongGiaTri += gt;
            switch (pt.getMucCanhBao()) {
                case "HẾT HÀNG":   soHet++; break;
                case "SẮP HẾT":    soSapHet++; break;
                default:           soBinhThuong++;
            }
        }

        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 0, 14, 0));
        grid.add(theCanhBao("🚨", "Hết hàng", String.valueOf(soHet),
                "Cần nhập gấp", new Color(220, 80, 80)));
        grid.add(theCanhBao("⚠", "Sắp hết", String.valueOf(soSapHet),
                "≤ mức tối thiểu", new Color(225, 150, 45)));
        grid.add(theCanhBao("✅", "Bình thường", String.valueOf(soBinhThuong),
                "Còn đủ dùng", new Color(46, 160, 92)));
        grid.add(theCanhBao("💰", "Tổng giá trị tồn",
                UiHelper.tien(tongGiaTri) + " đ", "Qua " + kho.getPhuTungs().size() + " mặt hàng",
                new Color(37, 99, 168)));
        JPanel wrapGrid = new JPanel(new BorderLayout());
        wrapGrid.setPreferredSize(new Dimension(0, 100));
        wrapGrid.setOpaque(false);
        wrapGrid.add(grid, BorderLayout.CENTER);
        root.add(wrapGrid, BorderLayout.CENTER);

        // ==== Bảng chi tiết ====
        JPanel bangPanel = UiHelper.panelBang("Báo cáo chi tiết (cảnh báo real-time)");
        DefaultTableModel model = UiHelper.model("Mã PT", "Tên phụ tùng", "ĐVT",
                "Tồn kho", "Tối thiểu", "Cảnh báo", "Đơn giá", "Giá trị tồn");
        for (PhuTung pt : kho.getPhuTungs()) {
            model.addRow(new Object[]{pt.getMaPT(), pt.getTen(), pt.getDonVi(),
                    pt.getSoLuongTon(), pt.getSoLuongToiThieu(), pt.getMucCanhBao(),
                    UiHelper.tien(pt.getDonGia()),
                    UiHelper.tien(pt.getSoLuongTon() * pt.getDonGia())});
        }
        JTable bang = UiHelper.bang(model);
        // Tô màu cột cảnh báo
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
        // Tô nền dòng có cảnh báo
        final List<Integer> rowsCanhBao = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String cb = model.getValueAt(i, 5).toString();
            if (cb.contains("HẾT") || cb.contains("SẮP")) rowsCanhBao.add(i);
        }
        bang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, r, col);
                if (!s && rowsCanhBao.contains(r)) {
                    String cb = model.getValueAt(r, 5).toString();
                    if (cb.contains("HẾT")) c.setBackground(new Color(255, 232, 232));
                    else c.setBackground(new Color(255, 245, 224));
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
        JScrollPane sp = new JScrollPane(bang);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        JScrollPane wrapBang = new JScrollPane(bangPanel);
        wrapBang.setBorder(null);
        wrapBang.setOpaque(false);
        wrapBang.getViewport().setOpaque(false);
        wrapBang.setPreferredSize(new Dimension(0, 0));

        // ==== Footer ====
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
        JLabel tTrai = new JLabel("Số mặt hàng: " + kho.getPhuTungs().size()
                + "   |   Đang hết/sắp hết: " + (soHet + soSapHet));
        tTrai.setForeground(Color.WHITE);
        tTrai.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel tPhai = new JLabel("Tổng giá trị tồn: " + UiHelper.tien(tongGiaTri) + " VNĐ");
        tPhai.setForeground(Color.WHITE);
        tPhai.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tongPanel.add(tTrai, BorderLayout.WEST);
        tongPanel.add(tPhai, BorderLayout.EAST);

        // Gộp bảng + footer
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(14, 0, 0, 0));
        south.add(wrapBang, BorderLayout.CENTER);
        south.add(tongPanel, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
    }

    private JPanel theCanhBao(String icon, String ten, String so, String phuDe, Color mau) {
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
                new EmptyBorder(14, 18, 14, 18)));
        p.setPreferredSize(new Dimension(0, 100));
        p.setMinimumSize(new Dimension(0, 100));

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
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(48, 48));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTen.setForeground(new Color(100, 110, 125));
        JLabel lblSo = new JLabel(so);
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSo.setForeground(mau);
        JLabel lblPhu = new JLabel(phuDe);
        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPhu.setForeground(new Color(140, 150, 165));
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPhu.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(lblTen);
        text.add(lblSo);
        text.add(lblPhu);

        p.add(lblIcon, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }
}

