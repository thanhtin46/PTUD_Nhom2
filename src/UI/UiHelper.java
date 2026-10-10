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

/** Helper giao diện chung: màu, font, nút, bảng và các thành phần Swing dùng lại. */
public final class UiHelper {

    // Bảng màu giao diện
    public static final Color XANH_DAM = new Color(25, 48, 70);
    public static final Color XANH_TRUNG = new Color(42, 112, 153);
    public static final Color XANH_NHAT = new Color(67, 151, 190);
    public static final Color XANH_SIDE = new Color(20, 35, 52);
    public static final Color NEN = new Color(243, 247, 250);
    public static final Color NEN_CARD = Color.WHITE;
    public static final Color VIEN = new Color(222, 231, 238);
    public static final Color CHU_CHAY = new Color(232, 60, 60);
    public static final Color XANH_LA = new Color(46, 160, 92);
    public static final Color CAM = new Color(225, 150, 45);
    public static final Color TIM = new Color(140, 95, 200);
    public static final Color XAM = new Color(120, 130, 145);

    public static final Font FONT_BT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_NHAN = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TIEU_DE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TIEU_DE_PHU = new Font("Segoe UI", Font.BOLD, 16);

    private UiHelper() {}

    public static JPanel nen() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(NEN);
        p.setBorder(new EmptyBorder(24, 26, 24, 26));
        return p;
    }

    public static JPanel panelForm(String tieuDe, int rong) {
        JPanel panel = taoThe(new BorderLayout(0, 14));
        panel.setPreferredSize(new Dimension(rong, 0));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel tde = new JLabel(tieuDe);
        tde.setFont(FONT_TIEU_DE_PHU);
        tde.setForeground(XANH_DAM);
        tde.setBorder(new EmptyBorder(0, 4, 10, 0));
        panel.add(tde, BorderLayout.NORTH);
        return panel;
    }

    public static JPanel panelBang(String tieuDe) {
        JPanel panel = taoThe(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel tde = new JLabel(tieuDe);
        tde.setFont(FONT_TIEU_DE_PHU);
        tde.setForeground(XANH_DAM);
        tde.setBorder(new EmptyBorder(0, 4, 4, 0));
        panel.add(tde, BorderLayout.NORTH);
        return panel;
    }

    private static JPanel taoThe(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VIEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(NEN_CARD);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
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
            tf.setPreferredSize(new Dimension(190, 40));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(VIEN, 1, true),
                    new EmptyBorder(7, 11, 7, 11)));
            tf.setBackground(new Color(250, 252, 253));
        } else if (o instanceof JComboBox cb) {
            cb.setPreferredSize(new Dimension(190, 40));
            cb.setBackground(Color.WHITE);
            cb.setBorder(BorderFactory.createLineBorder(VIEN, 1, true));
        } else if (o instanceof JScrollPane sp) {
            sp.setBorder(BorderFactory.createLineBorder(VIEN, 1, true));
            if (sp.getViewport().getView() instanceof JTextArea ta) {
                ta.setFont(FONT_BT);
                ta.setBackground(new Color(250, 252, 253));
                ta.setBorder(new EmptyBorder(8, 10, 8, 10));
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
                Color nenNut = pressed ? darken(mau, 0.16f) : (hover ? lighten(mau, 0.08f) : mau);
                g2.setColor(nenNut);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 16, 9, 16));
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
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 251, 253));
                    c.setForeground(new Color(40, 50, 65));
                } else {
                    c.setBackground(new Color(226, 241, 247));
                    c.setForeground(XANH_DAM);
                }
                return c;
            }
        };
        table.setRowHeight(36);
        table.setFont(FONT_BT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(new Color(233, 239, 243));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(226, 241, 247));
        table.setSelectionForeground(XANH_DAM);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setBorder(new EmptyBorder(0, 0, 0, 0));
        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(XANH_DAM);
        h.setBackground(new Color(239, 245, 248));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(239, 245, 248));
                lbl.setForeground(XANH_DAM);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(new EmptyBorder(10, 8, 10, 8));
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
            sp.setBorder(BorderFactory.createLineBorder(VIEN, 1, true));
            sp.getViewport().setBackground(Color.WHITE);
        }
    }
}
