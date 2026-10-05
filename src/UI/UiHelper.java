package UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/** Helper giao diện chung: màu, font, nút gradient, bảng, border đổ bóng. */
public final class UiHelper {

    // Bảng màu giao diện
    public static final Color XANH_DAM = new Color(21, 58, 110);
    public static final Color XANH_TRUNG = new Color(37, 99, 168);
    public static final Color XANH_NHAT = new Color(80, 144, 220);
    public static final Color XANH_SIDE = new Color(18, 50, 95);
    public static final Color NEN = new Color(243, 246, 250);
    public static final Color NEN_CARD = Color.WHITE;
    public static final Color VIEN = new Color(220, 228, 240);
    public static final Color CHU_CHAY = new Color(232, 60, 60);
    public static final Color XANH_LA = new Color(46, 160, 92);
    public static final Color CAM = new Color(225, 150, 45);
    public static final Color TIM = new Color(140, 95, 200);
    public static final Color XAM = new Color(120, 130, 145);

    public static final Font FONT_BT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_NHAN = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TIEU_DE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_TIEU_DE_PHU = new Font("Segoe UI", Font.BOLD, 16);

    private UiHelper() {}

    public static JPanel nen() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(NEN);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    public static JPanel panelForm(String tieuDe, int rong) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setPreferredSize(new Dimension(rong, 0));
        panel.setBackground(NEN_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(8, new Color(0, 0, 0, 20)),
                new EmptyBorder(18, 20, 20, 20)));

        JLabel tde = new JLabel(tieuDe);
        tde.setFont(FONT_TIEU_DE_PHU);
        tde.setForeground(XANH_DAM);
        tde.setBorder(new EmptyBorder(0, 4, 10, 0));
        panel.add(tde, BorderLayout.NORTH);
        return panel;
    }

    public static JPanel panelBang(String tieuDe) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(NEN_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(8, new Color(0, 0, 0, 20)),
                new EmptyBorder(14, 16, 16, 16)));

        JLabel tde = new JLabel(tieuDe);
        tde.setFont(FONT_TIEU_DE_PHU);
        tde.setForeground(XANH_DAM);
        tde.setBorder(new EmptyBorder(0, 4, 4, 0));
        panel.add(tde, BorderLayout.NORTH);
        return panel;
    }

    public static void themDong(JPanel form, int dong, String nhan, JComponent o) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = dong;
        // Tăng khoảng cách: top 12, left 4, bottom 12, right 4 — dễ đọc hơn
        gbc.insets = new Insets(10, 4, 10, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_NHAN);
        lbl.setForeground(new Color(60, 70, 85));
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(lbl, gbc);

        trangTriInput(o);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Thêm 1 khoảng trống ngang giữa label và input
        gbc.insets = new Insets(10, 8, 10, 4);
        form.add(o, gbc);
    }

    public static void trangTriInput(JComponent o) {
        o.setFont(FONT_BT);
        if (o instanceof JTextField tf) {
            tf.setPreferredSize(new Dimension(190, 38));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(VIEN, 1, true),
                    new EmptyBorder(6, 10, 6, 10)));
            tf.setBackground(new Color(252, 253, 255));
        } else if (o instanceof JComboBox cb) {
            cb.setPreferredSize(new Dimension(190, 38));
            cb.setBackground(Color.WHITE);
            cb.setBorder(BorderFactory.createLineBorder(VIEN, 1, true));
        } else if (o instanceof JScrollPane sp) {
            sp.setBorder(BorderFactory.createLineBorder(VIEN, 1, true));
            if (sp.getViewport().getView() instanceof JTextArea ta) {
                ta.setFont(FONT_BT);
                ta.setBackground(new Color(252, 253, 255));
                ta.setBorder(new EmptyBorder(6, 8, 6, 8));
            }
        }
    }

    public static JButton nut(String text, Color mau, java.awt.event.ActionListener hanhDong) {
        return nut(text, mau, false, hanhDong);
    }

    public static JButton nut(String text, Color mau, boolean full, java.awt.event.ActionListener hanhDong) {
        JButton b = new JButton(text) {
            private boolean hover = false, pressed = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hover = false; pressed = false; repaint(); }
                    public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color top = mau;
                Color bot = darken(mau, 0.18f);
                if (pressed) { top = darken(mau, 0.22f); bot = darken(mau, 0.30f); }
                else if (hover) { top = lighten(mau, 0.10f); bot = mau; }
                g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 14, 9, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        if (full) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        } else {
            b.setPreferredSize(new Dimension(110, 36));
        }
        if (hanhDong != null) b.addActionListener(hanhDong);
        return b;
    }

    public static Color darken(Color c, float f) {
        return new Color(Math.max(0, (int)(c.getRed()   * (1 - f))),
                         Math.max(0, (int)(c.getGreen() * (1 - f))),
                         Math.max(0, (int)(c.getBlue()  * (1 - f))));
    }

    public static Color lighten(Color c, float f) {
        return new Color(Math.min(255, (int)(c.getRed()   + (255 - c.getRed())   * f)),
                         Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * f)),
                         Math.min(255, (int)(c.getBlue()  + (255 - c.getBlue())  * f)));
    }

    public static JTable bang(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(247, 250, 254));
                    c.setForeground(new Color(40, 50, 65));
                } else {
                    c.setBackground(new Color(220, 235, 250));
                    c.setForeground(new Color(15, 35, 70));
                }
                return c;
            }
        };
        table.setRowHeight(32);
        table.setFont(FONT_BT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(new Color(232, 238, 248));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(220, 235, 250));
        table.setSelectionForeground(new Color(15, 35, 70));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setBorder(new EmptyBorder(0, 0, 0, 0));
        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(Color.WHITE);
        h.setBackground(XANH_TRUNG);
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(XANH_TRUNG);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(new EmptyBorder(8, 6, 8, 6));
                return lbl;
            }
        });
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class)).setHorizontalAlignment(SwingConstants.CENTER);
        return table;
    }

    public static DefaultTableModel model(String... cot) {
        return new DefaultTableModel(cot, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? String.class : Object.class;
            }
        };
    }

    public static String tien(long v) { return String.format("%,d", v); }

    public static void loi(Component parent, String nd, JComponent focus) {
        JOptionPane.showMessageDialog(parent, nd, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        if (focus != null) focus.requestFocus();
    }

    /** Border đổ bóng mềm cho card. */
    public static class ShadowBorder extends AbstractBorder {
        private final int size;
        private final Color color;

        public ShadowBorder(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < size; i++) {
                float a = color.getAlpha() / 255f * (1f - i / (float) size) * 0.5f;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 255)));
                g2.drawRoundRect(x + i, y + i, width - 1 - 2 * i, height - 1 - 2 * i, 16, 16);
            }
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(size, size, size, size); }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(size, size, size, size);
            return insets;
        }
    }

    public static JLabel tieuDeTrang(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TIEU_DE);
        lbl.setForeground(XANH_DAM);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        return lbl;
    }

    public static void danhSachBong(JComponent o) {
        if (o instanceof JScrollPane sp) {
            sp.setBorder(BorderFactory.createCompoundBorder(
                    new ShadowBorder(6, new Color(0, 0, 0, 15)),
                    new EmptyBorder(0, 0, 0, 0)));
            sp.getViewport().setBackground(Color.WHITE);
        }
    }
}
