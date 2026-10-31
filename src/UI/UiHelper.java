package UI;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Lớp tiện ích hỗ trợ định dạng giao diện, màu sắc, font chữ và các component Swing.
 */
public final class UiHelper {

    // Bảng màu giao diện
    public static final Color XANH_DAM       = new Color(15, 23, 42);      // Chữ chính, tiêu đề
    public static final Color XANH_TRUNG     = new Color(37, 99, 235);     // Nút chính
    public static final Color XANH_NHAT      = new Color(96, 165, 250);    // Màu phụ
    public static final Color XANH_SIDE      = new Color(15, 23, 42);      // Màu menu bên trái
    public static final Color NEN            = new Color(241, 245, 249);   // Màu nền
    public static final Color NEN_CARD       = Color.WHITE;                // Nền khung trắng
    public static final Color VIEN           = new Color(226, 232, 240);   // Viền khung và ô nhập
    public static final Color VIEN_FOCUS     = new Color(37, 99, 235);     // Viền khi chọn ô nhập
    public static final Color CHU_CHAY       = new Color(239, 68, 68);     // Xóa, cảnh báo
    public static final Color XANH_LA        = new Color(22, 163, 74);     // Thêm mới, hoàn thành
    public static final Color CAM            = new Color(245, 158, 11);    // Cảnh báo, chờ
    public static final Color TIM            = new Color(124, 58, 237);    // Khách hàng, bảo hành
    public static final Color XAM            = new Color(100, 116, 139);   // Nút phụ, nhãn phụ
    public static final Color TEXT_MUTED     = new Color(148, 163, 184);   // Chữ mờ

    // Font chữ
    public static final Font FONT_BT         = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_NHAN       = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TIEU_DE     = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_TIEU_DE_PHU = new Font("Segoe UI", Font.BOLD, 16);

    private UiHelper() {}

    /** Nền chính của từng trang */
    public static JPanel nen() {
        JPanel p = new JPanel(new BorderLayout(24, 24));
        p.setBackground(NEN);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
        return p;
    }

    /** Tạo container dạng card cho form nhập liệu */
    public static JPanel panelForm(String tieuDe, int rong) {
        JPanel panel = taoThe(new BorderLayout(0, 14));
        panel.setPreferredSize(new Dimension(rong, 0));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel tde = new JLabel(tieuDe);
        tde.setFont(FONT_TIEU_DE_PHU);
        tde.setForeground(XANH_DAM);
        tde.setBorder(new EmptyBorder(0, 2, 4, 0));
        panel.add(tde, BorderLayout.NORTH);
        return panel;
    }

    /** Tạo container dạng card cho bảng dữ liệu */
    public static JPanel panelBang(String tieuDe) {
        JPanel panel = taoThe(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel tde = new JLabel(tieuDe);
        tde.setFont(FONT_TIEU_DE_PHU);
        tde.setForeground(XANH_DAM);
        tde.setBorder(new EmptyBorder(0, 2, 4, 0));
        panel.add(tde, BorderLayout.NORTH);
        return panel;
    }

    /**
     * Panel hỗ trợ cuộn
     */
    public static class CuonPanel extends JPanel implements Scrollable {
        public CuonPanel(LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 32; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    /**
     * Gắn nội dung nhập liệu và các nút chức năng vào form.
     */
    public static void ganNoiDungForm(JPanel formPanel, JComponent form, JComponent nut) {
        JPanel noiDung = new JPanel(new BorderLayout(0, 14));
        noiDung.setOpaque(false);
        noiDung.add(form, BorderLayout.NORTH);
        if (nut != null) {
            noiDung.add(nut, BorderLayout.SOUTH);
        }

        CuonPanel cuon = new CuonPanel(new BorderLayout());
        cuon.add(noiDung, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(cuon);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        formPanel.add(scroll, BorderLayout.CENTER);
    }

    /** Tạo panel nền trắng có bo góc */
    public static JPanel taoThe(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // Đổ bóng
                g2.setColor(new Color(15, 23, 42, 8));
                g2.fillRoundRect(0, 2, w, h - 2, 18, 18);
                g2.setColor(new Color(15, 23, 42, 14));
                g2.fillRoundRect(0, 1, w, h - 1, 18, 18);

                // Nền panel
                g2.setColor(NEN_CARD);
                g2.fillRoundRect(0, 0, w, h - 1, 18, 18);

                // Vẽ đường viền
                g2.setColor(VIEN);
                g2.drawRoundRect(0, 0, w - 1, h - 2, 18, 18);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /** Thêm dòng trong form GridBagLayout với khoảng đệm mặc định */
    public static void themDong(JPanel form, int dong, String nhan, JComponent o) {
        themDong(form, dong, nhan, o, 5, 5);
    }

    /** Thêm dòng trong form GridBagLayout tùy chỉnh khoảng đệm trên / dưới */
    public static void themDong(JPanel form, int dong, String nhan, JComponent o, int topPad, int botPad) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = dong;
        gbc.insets = new Insets(topPad, 4, botPad, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lbl = new JLabel(nhan);
        lbl.setFont(FONT_NHAN);
        lbl.setForeground(new Color(51, 65, 85));
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(lbl, gbc);

        trangTriInput(o);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(topPad, 12, botPad, 4);
        form.add(o, gbc);
    }

    /** Định dạng ô nhập liệu và hiệu ứng khi chọn */
    public static void trangTriInput(JComponent o) {
        o.setFont(FONT_BT);
        if (o instanceof JTextField tf) {
            tf.setPreferredSize(new Dimension(190, 38));
            tf.setBackground(new Color(248, 250, 252));
            tf.setForeground(XANH_DAM);
            tf.setCaretColor(XANH_TRUNG);
            tf.setBorder(createInputBorder(false));

            tf.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    tf.setBackground(Color.WHITE);
                    tf.setBorder(createInputBorder(true));
                    tf.repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    tf.setBackground(new Color(248, 250, 252));
                    tf.setBorder(createInputBorder(false));
                    tf.repaint();
                }
            });
        } else if (o instanceof JComboBox<?> cb) {
            cb.setPreferredSize(new Dimension(190, 38));
            cb.setBackground(Color.WHITE);
            cb.setForeground(XANH_DAM);
            cb.setBorder(BorderFactory.createLineBorder(VIEN, 1, true));
        } else if (o instanceof JScrollPane sp) {
            sp.setBorder(BorderFactory.createLineBorder(VIEN, 1, true));
            if (sp.getViewport().getView() instanceof JTextArea ta) {
                ta.setFont(FONT_BT);
                ta.setBackground(new Color(248, 250, 252));
                ta.setForeground(XANH_DAM);
                ta.setBorder(new EmptyBorder(8, 12, 8, 12));
                ta.setCaretColor(XANH_TRUNG);
            }
        }
    }

    private static javax.swing.border.Border createInputBorder(boolean focus) {
        Color viBorder = focus ? VIEN_FOCUS : VIEN;
        int thickness = focus ? 2 : 1;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(viBorder, thickness, true),
                new EmptyBorder(focus ? 6 : 7, 12, focus ? 6 : 7, 12)
        );
    }

    /**
     * Ràng buộc JTextField chỉ cho phép nhập số [0-9].
     * @param tf JTextField cần giới hạn
     * @param doDaiToiDa Độ dài tối đa (nếu <= 0 thì không giới hạn)
     */
    public static void chiChoNhapSo(JTextField tf, int doDaiToiDa) {
        if (tf == null) return;
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument) tf.getDocument();
        doc.setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr)
                    throws javax.swing.text.BadLocationException {
                if (string == null) return;
                String filtered = string.replaceAll("[^0-9]", "");
                if (filtered.isEmpty()) return;
                int currentLength = fb.getDocument().getLength();
                if (doDaiToiDa > 0 && currentLength + filtered.length() > doDaiToiDa) {
                    int remaining = doDaiToiDa - currentLength;
                    if (remaining <= 0) return;
                    filtered = filtered.substring(0, remaining);
                }
                super.insertString(fb, offset, filtered, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs)
                    throws javax.swing.text.BadLocationException {
                if (text == null) {
                    super.replace(fb, offset, length, null, attrs);
                    return;
                }
                String filtered = text.replaceAll("[^0-9]", "");
                int currentLength = fb.getDocument().getLength() - length;
                if (doDaiToiDa > 0 && currentLength + filtered.length() > doDaiToiDa) {
                    int remaining = doDaiToiDa - currentLength;
                    if (remaining <= 0) return;
                    filtered = filtered.substring(0, remaining);
                }
                super.replace(fb, offset, length, filtered, attrs);
            }
        });
    }

    public static void chiChoNhapSo(JTextField tf) {
        chiChoNhapSo(tf, 0);
    }

    public static JButton nut(String text, Color mau, java.awt.event.ActionListener hanhDong) {
        return nut(text, (Icon) null, mau, false, hanhDong);
    }

    public static JButton nut(String text, Color mau, boolean full, java.awt.event.ActionListener hanhDong) {
        return nut(text, (Icon) null, mau, full, hanhDong);
    }

    public static JButton nut(String text, Icon icon, Color mau, java.awt.event.ActionListener hanhDong) {
        return nut(text, icon, mau, false, hanhDong);
    }

    /** Tạo nút bấm với màu sắc và icon tương ứng */
    public static JButton nut(String text, Icon icon, Color mau, boolean full, java.awt.event.ActionListener hanhDong) {
        Icon finalIcon = icon;
        String finalText = text;
        if (finalText != null) {
            if (finalIcon == null) {
                if (finalText.startsWith("✓") || finalText.startsWith("✔")) {
                    finalIcon = UiIcon.of(UiIcon.CHECK, 15);
                } else if (finalText.startsWith("➕") || finalText.startsWith("+")) {
                    finalIcon = UiIcon.of(UiIcon.PLUS, 15);
                } else if (finalText.startsWith("🗑") || finalText.contains("Xóa") || finalText.contains("Hủy")) {
                    finalIcon = UiIcon.of(UiIcon.TRASH, 15);
                } else if (finalText.startsWith("⏳") || finalText.contains("Lưu chờ")) {
                    finalIcon = UiIcon.of(UiIcon.CLOCK, 15);
                } else if (finalText.startsWith("📥") || finalText.contains("Nhập kho") || finalText.contains("nhập")) {
                    finalIcon = UiIcon.of(UiIcon.IMPORT, 15);
                } else if (finalText.startsWith("📤") || finalText.contains("Xuất kho") || finalText.contains("xuất")) {
                    finalIcon = UiIcon.of(UiIcon.EXPORT, 15);
                } else if (finalText.startsWith("🖨") || finalText.contains("In hóa đơn")) {
                    finalIcon = UiIcon.of(UiIcon.PRINT, 15);
                } else if (finalText.startsWith("✎") || finalText.startsWith("Sửa")) {
                    finalIcon = UiIcon.of(UiIcon.EDIT, 15);
                } else if (finalText.contains("Làm mới")) {
                    finalIcon = UiIcon.of(UiIcon.REFRESH, 15);
                } else if (finalText.startsWith("🔍") || finalText.contains("Xem") || finalText.contains("Tra cứu")) {
                    finalIcon = UiIcon.of(UiIcon.SEARCH, 15);
                } else if (finalText.contains("Khóa") || finalText.contains("Vô hiệu")) {
                    finalIcon = UiIcon.of(UiIcon.LOCK, 15);
                } else if (finalText.contains("Thanh toán")) {
                    finalIcon = UiIcon.of(UiIcon.CHECK, 15);
                }
            }
            // Lọc bỏ ký tự đặc biệt ở đầu chuỗi
            finalText = finalText.replaceAll("^[✓✔➕+🗑⏳📥📤🖨✎🔍⏻]+", "").trim();
            finalText = finalText.replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]", "").trim();
        }

        final Icon bIcon = finalIcon;
        final String bText = finalText;

        JButton b = new JButton(bText) {
            private boolean hover = false, pressed = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hover = false; pressed = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                Color nenNut = !isEnabled() ? new Color(148, 163, 184)
                        : (pressed ? darken(mau, 0.15f) : (hover ? lighten(mau, 0.10f) : mau));

                // Bóng nhẹ dưới nút
                if (!pressed && isEnabled()) {
                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fillRoundRect(0, 2, w, h - 2, 12, 12);
                }

                g2.setColor(nenNut);
                g2.fillRoundRect(0, 0, w, h - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 8, 8, 8));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        if (bIcon != null) {
            b.setIcon(bIcon);
            b.setIconTextGap(6);
        }
        if (full) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        } else {
            int calcW = 92;
            if (bText != null && !bText.isEmpty()) {
                FontMetrics fm = b.getFontMetrics(b.getFont());
                int textW = fm.stringWidth(bText);
                int iconW = bIcon != null ? (bIcon.getIconWidth() + 10) : 0;
                calcW = Math.max(92, textW + iconW + 24);
            }
            b.setPreferredSize(new Dimension(calcW, 38));
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

    /** Định dạng bảng JTable hiển thị dữ liệu */
    public static JTable bang(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(new Color(30, 41, 59));
                } else {
                    c.setBackground(new Color(219, 234, 254));
                    c.setForeground(XANH_TRUNG);
                }
                return c;
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                int prefW = 0;
                for (int i = 0; i < getColumnCount(); i++) {
                    prefW += getColumnModel().getColumn(i).getPreferredWidth();
                }
                if (prefW > 0) {
                    d.width = Math.max(d.width, prefW);
                }
                return d;
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                if (getParent() instanceof JViewport vp) {
                    int prefW = 0;
                    for (int i = 0; i < getColumnCount(); i++) {
                        prefW += getColumnModel().getColumn(i).getPreferredWidth();
                    }
                    return vp.getWidth() >= prefW;
                }
                return false;
            }
        };
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(40);
        table.setFont(FONT_BT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(XANH_TRUNG);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setBorder(new EmptyBorder(0, 0, 0, 0));

        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(new Color(30, 41, 59));
        h.setBackground(new Color(241, 245, 249));
        h.setPreferredSize(new Dimension(0, 44));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(241, 245, 249));
                lbl.setForeground(new Color(30, 41, 59));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, VIEN),
                        new EmptyBorder(10, 10, 10, 10)
                ));
                return lbl;
            }
        });
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class)).setHorizontalAlignment(SwingConstants.CENTER);
        if (table.getDefaultRenderer(String.class) instanceof DefaultTableCellRenderer sr) {
            sr.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return table;
    }

    public static void canGiua(JTable table, int... cot) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c : cot) {
            if (c >= 0 && c < table.getColumnCount()) {
                table.getColumnModel().getColumn(c).setCellRenderer(r);
            }
        }
    }

    public static void canPhai(JTable table, int... cot) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : cot) {
            if (c >= 0 && c < table.getColumnCount()) {
                table.getColumnModel().getColumn(c).setCellRenderer(r);
            }
        }
    }

    public static void canTrai(JTable table, int... cot) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.LEFT);
        for (int c : cot) {
            if (c >= 0 && c < table.getColumnCount()) {
                table.getColumnModel().getColumn(c).setCellRenderer(r);
            }
        }
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

    /** Border đổ bóng cho panel */
    public static class ShadowBorder extends AbstractBorder {
        private final int size;
        private final Color color;

        public ShadowBorder(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < size; i++) {
                float a = color.getAlpha() / 255f * (1f - i / (float) size) * 0.4f;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 255)));
                g2.drawRoundRect(x + i, y + i, width - 1 - 2 * i, height - 1 - 2 * i, 18, 18);
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
        return tieuDeTrang(text, null);
    }

    public static JLabel tieuDeTrang(String text, Icon icon) {
        String cleanTitle = text;
        Icon finalIcon = icon;
        if (cleanTitle != null) {
            if (finalIcon == null) {
                if (cleanTitle.contains("Thanh toán") || cleanTitle.contains("hóa đơn")) finalIcon = UiIcon.of(UiIcon.BILL, 22, XANH_TRUNG);
                else if (cleanTitle.contains("phụ tùng") || cleanTitle.contains("Tồn kho")) finalIcon = UiIcon.of(UiIcon.BOX, 22, XANH_TRUNG);
                else if (cleanTitle.contains("bảo hành")) finalIcon = UiIcon.of(UiIcon.SHIELD, 22, XANH_TRUNG);
                else if (cleanTitle.contains("sửa chữa") || cleanTitle.contains("tiếp nhận") || cleanTitle.contains("tiến độ")) finalIcon = UiIcon.of(UiIcon.WRENCH, 22, XANH_TRUNG);
                else if (cleanTitle.contains("khách hàng")) finalIcon = UiIcon.of(UiIcon.USERS, 22, XANH_TRUNG);
                else if (cleanTitle.contains("phương tiện") || cleanTitle.contains("xe")) finalIcon = UiIcon.of(UiIcon.CAR, 22, XANH_TRUNG);
                else if (cleanTitle.contains("nhân viên") || cleanTitle.contains("Kỹ thuật")) finalIcon = UiIcon.of(UiIcon.USER, 22, XANH_TRUNG);
                else if (cleanTitle.contains("kho") || cleanTitle.contains("nhập") || cleanTitle.contains("xuất")) finalIcon = UiIcon.of(UiIcon.IMPORT, 22, XANH_TRUNG);
                else if (cleanTitle.contains("quyền")) finalIcon = UiIcon.of(UiIcon.LOCK, 22, XANH_TRUNG);
            }
            cleanTitle = cleanTitle.replaceAll("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\\u23F3\\u23F1\\u23F0\\u231A\\u231B\\u2600-\\u27BF💳📦📋🛡️👨‍🔧👥🚗⚙️🔍⏳⏱📊]+", "").trim();
            cleanTitle = cleanTitle.replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]", "").trim();
        }
        JLabel lbl = new JLabel(cleanTitle);
        lbl.setFont(FONT_TIEU_DE);
        lbl.setForeground(XANH_DAM);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        if (finalIcon != null) {
            lbl.setIcon(finalIcon);
            lbl.setIconTextGap(10);
        }
        return lbl;
    }

    public static void danhSachBong(JComponent o) {
        if (o instanceof JScrollPane sp) {
            sp.setBorder(BorderFactory.createLineBorder(VIEN, 1, true));
            sp.getViewport().setBackground(Color.WHITE);
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.getHorizontalScrollBar().setUnitIncrement(16);
        }
    }

    // ==== TIỆN ÍCH ĐỊNH DẠNG & CHUYỂN ĐỔI NGÀY THÁNG (JAVA.TIME) ====
    public static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DINH_DANG_NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter BOHOA_NGAY_NGHIEM_NGAT = new DateTimeFormatterBuilder()
            .parseStrict()
            .appendPattern("dd/MM/uuuu")
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    /** Định dạng LocalDate sang chuỗi dd/MM/yyyy thân thiện */
    public static String dinhDangNgay(LocalDate date) {
        return date != null ? date.format(DINH_DANG_NGAY) : "";
    }

    /** Định dạng LocalDateTime sang chuỗi dd/MM/yyyy HH:mm thân thiện */
    public static String dinhDangNgayGio(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DINH_DANG_NGAY_GIO) : "";
    }

    /**
     * Chuyển đổi chuỗi ngày nhập vào (dd/MM/yyyy) sang LocalDate.
     * Bắt lỗi nếu sai định dạng hoặc ngày không tồn tại (ví dụ 31/02) và hiển thị thông báo.
     */
    public static LocalDate chuyenChuoiSangNgay(String text, Component parent, String tenTruong) {
        if (text == null || text.trim().isEmpty()) return null;
        String s = text.trim();
        try {
            return LocalDate.parse(s, BOHOA_NGAY_NGHIEM_NGAT);
        } catch (DateTimeParseException ex) {
            String nhan = (tenTruong != null && !tenTruong.isBlank()) ? tenTruong + ": " : "";
            JOptionPane.showMessageDialog(
                    parent,
                    nhan + "Ngày '" + s + "' không hợp lệ hoặc không tồn tại!\nVui lòng nhập đúng định dạng dd/MM/yyyy (ví dụ: 15/05/2026).",
                    "Lỗi định dạng ngày",
                    JOptionPane.WARNING_MESSAGE
            );
            return null;
        }
    }
}
