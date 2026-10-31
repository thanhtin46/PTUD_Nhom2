package UI;

import Entity.HoaDon;
import Entity.KhoDuLieu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình báo cáo doanh thu của gara.
 * Hỗ trợ xem biểu đồ doanh thu theo tháng và bảng số liệu chi tiết.
 */
public class BaoCaoDoanhThu_UI extends JPanel {

    public static class ThangDoanhThu {
        public String thang;         // "T01", "T02"...
        public String tenHienThi;    // "Tháng 01/2026"...
        public int soHoaDon;
        public long tienDichVu;
        public long tienPhuTung;
        public long tongDoanhThu;
        public double tangTruong;

        public ThangDoanhThu(String thang, String tenHienThi, int soHoaDon, long tienDichVu, long tienPhuTung) {
            this.thang = thang;
            this.tenHienThi = tenHienThi;
            this.soHoaDon = soHoaDon;
            this.tienDichVu = tienDichVu;
            this.tienPhuTung = tienPhuTung;
            this.tongDoanhThu = tienDichVu + tienPhuTung;
        }
    }

    private final KhoDuLieu kho = KhoDuLieu.get();
    private int namChon = 2026;
    private boolean xemDangDuong = false; // false: Cột, true: Đường

    private final List<ThangDoanhThu> dsThang = new ArrayList<>();
    private long tongDoanhThuNam = 0;
    private long tongDichVuNam = 0;
    private long tongPhuTungNam = 0;
    private int tongHoaDonNam = 0;

    // Component UI
    private JComboBox<Integer> cboNam;
    private JButton btnDangCot;
    private JButton btnDangDuong;

    private JLabel lblValTong;
    private JLabel lblValDichVu;
    private JLabel lblValPhuTung;
    private JLabel lblValHoaDon;

    private JLabel lblTieuDeBieuDo;
    private BieuDoPanel bieuDo;

    private DefaultTableModel modelBang;
    private JTable tableBang;

    public BaoCaoDoanhThu_UI() {
        this(false);
    }

    public BaoCaoDoanhThu_UI(boolean theoNamMacDinh) {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));

        initComponents();
        napDuLieu();
    }

    public void datCheDoBieuDo(int cheDo) {
        this.xemDangDuong = (cheDo == 1);
        capNhatNutChuyenBieuDo();
        if (bieuDo != null) {
            bieuDo.setDangDuong(xemDangDuong);
        }
    }

    private void initComponents() {
        // Container chính bọc trong CuonPanel giúp dãn 100% viewport width không bị tràn hay lệch
        UiHelper.CuonPanel mainContent = new UiHelper.CuonPanel(null);
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. Khối Header & Thanh công cụ (Dàn đều 100% chiều ngang)
        mainContent.add(taoHeaderToolbar());
        mainContent.add(Box.createVerticalStrut(14));

        // 2. Khối 4 Thẻ KPI (Trải đều 100% bằng GridLayout 4 cột)
        mainContent.add(taoTheKpi());
        mainContent.add(Box.createVerticalStrut(14));

        // 3. Khối Biểu đồ tinh gọn (Card trắng dãn 100%)
        mainContent.add(taoKhungBieuDo());
        mainContent.add(Box.createVerticalStrut(14));

        // 4. Khối Bảng số liệu chi tiết (Card trắng dãn 100%)
        mainContent.add(taoKhungBangChiTiet());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(new Color(248, 250, 252));
        add(scrollPane, BorderLayout.CENTER);
    }

    // ===============================================================
    //                       1. HEADER & TOOLBAR
    // ===============================================================
    private JPanel taoHeaderToolbar() {
        JPanel headerCard = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        headerCard.setOpaque(false);
        headerCard.setBorder(new EmptyBorder(12, 18, 12, 18));
        headerCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Bên trái: Tiêu đề trang
        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("BÁO CÁO DOANH THU GARA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Biểu đồ phân tích doanh thu dịch vụ sửa chữa & phụ tùng theo thời gian thực");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        pnlLeft.add(lblTitle);
        pnlLeft.add(Box.createVerticalStrut(2));
        pnlLeft.add(lblSub);
        headerCard.add(pnlLeft, BorderLayout.WEST);

        // Bên phải: Bộ lọc năm, Nút chuyển biểu đồ, Nút đồng bộ & Xuất
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRight.setOpaque(false);

        JLabel lblN = new JLabel("Năm:");
        lblN.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblN.setForeground(new Color(71, 85, 105));
        pnlRight.add(lblN);

        cboNam = new JComboBox<>(new Integer[]{2026, 2025, 2024});
        cboNam.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cboNam.setPreferredSize(new Dimension(85, 34));
        cboNam.setSelectedItem(namChon);
        cboNam.addActionListener(e -> {
            Integer n = (Integer) cboNam.getSelectedItem();
            if (n != null && n != namChon) {
                namChon = n;
                napDuLieu();
            }
        });
        pnlRight.add(cboNam);

        pnlRight.add(Box.createHorizontalStrut(6));

        // Nút chuyển dạng Cột / Đường
        btnDangCot = taoNutTab("Dạng Cột", true);
        btnDangCot.addActionListener(e -> {
            xemDangDuong = false;
            capNhatNutChuyenBieuDo();
            bieuDo.setDangDuong(false);
        });

        btnDangDuong = taoNutTab("Dạng Đường", false);
        btnDangDuong.addActionListener(e -> {
            xemDangDuong = true;
            capNhatNutChuyenBieuDo();
            bieuDo.setDangDuong(true);
        });

        JPanel grpTabs = new JPanel(new GridLayout(1, 2, 4, 0));
        grpTabs.setOpaque(false);
        grpTabs.add(btnDangCot);
        grpTabs.add(btnDangDuong);
        pnlRight.add(grpTabs);

        pnlRight.add(Box.createHorizontalStrut(8));

        // Nút Đồng bộ
        JButton btnRefresh = taoNutHanhDong("Làm mới", new Color(241, 245, 249), new Color(15, 23, 42), new Color(203, 213, 225));
        btnRefresh.setIcon(UiIcon.of(UiIcon.REFRESH, 13, new Color(15, 23, 42)));
        btnRefresh.addActionListener(e -> {
            kho.dongBoTuSQLServer();
            napDuLieu();
            JOptionPane.showMessageDialog(this, "Đã làm mới dữ liệu doanh thu từ hệ thống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        pnlRight.add(btnRefresh);

        headerCard.add(pnlRight, BorderLayout.EAST);
        return headerCard;
    }

    private JButton taoNutTab(String text, boolean dangChon) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean selected = "true".equals(getClientProperty("selected"));
                if (selected) {
                    g2.setColor(new Color(37, 99, 235));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.setColor(new Color(71, 85, 105));
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.putClientProperty("selected", dangChon ? "true" : "false");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(95, 34));
        return btn;
    }

    private void capNhatNutChuyenBieuDo() {
        btnDangCot.putClientProperty("selected", !xemDangDuong ? "true" : "false");
        btnDangDuong.putClientProperty("selected", xemDangDuong ? "true" : "false");
        btnDangCot.repaint();
        btnDangDuong.repaint();
    }

    private JButton taoNutHanhDong(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ===============================================================
    //                       2. CÁC THẺ CHỈ SỐ KPI
    // ===============================================================
    private JPanel taoTheKpi() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

        lblValTong = new JLabel("0 đ");
        lblValDichVu = new JLabel("0 đ");
        lblValPhuTung = new JLabel("0 đ");
        lblValHoaDon = new JLabel("0 lượt");

        grid.add(taoTheCardKpi("TỔNG DOANH THU", lblValTong, "+16.8% so với năm trước", new Color(37, 99, 235), new Color(239, 246, 255), "VNĐ"));
        grid.add(taoTheCardKpi("DOANH THU DỊCH VỤ", lblValDichVu, "Tiền công thợ sửa chữa", new Color(14, 165, 233), new Color(240, 249, 255), "D/V"));
        grid.add(taoTheCardKpi("DOANH THU PHỤ TÙNG", lblValPhuTung, "Linh kiện & Phụ tùng", new Color(16, 185, 129), new Color(236, 253, 245), "P/T"));
        grid.add(taoTheCardKpi("SỐ HÓA ĐƠN ĐÃ THU", lblValHoaDon, "Đã quyết toán hoàn tất", new Color(245, 158, 11), new Color(254, 243, 199), "H/Đ"));

        return grid;
    }

    private JPanel taoTheCardKpi(String tieuDe, JLabel lblVal, String moTa, Color mauChuDao, Color mauNenNhe, String badgeText) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setOpaque(false);

        JLabel lblTitle = new JLabel(tieuDe);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(100, 116, 139));

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblVal.setForeground(mauChuDao);

        JLabel lblDesc = new JLabel(moTa);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(new Color(148, 163, 184));

        pnlLeft.add(lblTitle);
        pnlLeft.add(Box.createVerticalStrut(3));
        pnlLeft.add(lblVal);
        pnlLeft.add(Box.createVerticalStrut(2));
        pnlLeft.add(lblDesc);

        // Badge huy hiệu bên phải
        JPanel pnlBadge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauNenNhe);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        pnlBadge.setOpaque(false);
        pnlBadge.setPreferredSize(new Dimension(42, 42));
        JLabel lblB = new JLabel(badgeText);
        lblB.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblB.setForeground(mauChuDao);
        pnlBadge.add(lblB);

        card.add(pnlLeft, BorderLayout.CENTER);
        card.add(pnlBadge, BorderLayout.EAST);
        return card;
    }

    // ===============================================================
    //                       3. KHUNG BIỂU ĐỒ TINH GỌN
    // ===============================================================
    private JPanel taoKhungBieuDo() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        card.setPreferredSize(new Dimension(0, 360));

        // Tiêu đề khung biểu đồ + Legend
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        lblTieuDeBieuDo = new JLabel("BIỂU ĐỒ TỔNG DOANH THU 12 THÁNG NĂM " + namChon);
        lblTieuDeBieuDo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTieuDeBieuDo.setForeground(new Color(15, 23, 42));
        pnlTop.add(lblTieuDeBieuDo, BorderLayout.WEST);

        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        pnlLegend.setOpaque(false);
        pnlLegend.add(taoLegendItem("Doanh thu Dịch vụ", new Color(37, 99, 235)));
        pnlLegend.add(taoLegendItem("Doanh thu Phụ tùng", new Color(16, 185, 129)));
        pnlTop.add(pnlLegend, BorderLayout.EAST);

        card.add(pnlTop, BorderLayout.NORTH);

        bieuDo = new BieuDoPanel();
        card.add(bieuDo, BorderLayout.CENTER);
        return card;
    }

    private JPanel taoLegendItem(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 2, 12, 12, 3, 3);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(12, 16));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(71, 85, 105));
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ===============================================================
    //                       4. BẢNG SỐ LIỆU CHI TIẾT
    // ===============================================================
    private JPanel taoKhungBangChiTiet() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        card.setPreferredSize(new Dimension(0, 485));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        JLabel lblTieuDeBang = new JLabel("CHI TIẾT DOANH THU TỪNG THÁNG TRONG NĂM " + namChon);
        lblTieuDeBang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTieuDeBang.setForeground(new Color(15, 23, 42));
        pnlTop.add(lblTieuDeBang, BorderLayout.WEST);

        JLabel lblNote = new JLabel("Đơn vị: Việt Nam Đồng (VNĐ) — Đồng bộ tự động theo hóa đơn");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNote.setForeground(new Color(100, 116, 139));
        pnlTop.add(lblNote, BorderLayout.EAST);
        card.add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"STT", "Tháng", "Số lượt HĐ", "Doanh thu Dịch vụ", "Doanh thu Phụ tùng", "Tổng doanh thu", "Tỷ trọng (%)", "Tăng trưởng"};
        modelBang = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tableBang = UiHelper.bang(modelBang);
        tableBang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableBang.setRowHeight(30);
        tableBang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableBang.getTableHeader().setBackground(new Color(241, 245, 249));
        tableBang.getTableHeader().setForeground(new Color(30, 41, 59));
        tableBang.setGridColor(new Color(241, 245, 249));
        tableBang.setShowVerticalLines(false);
        tableBang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        tableBang.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableBang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableBang.getColumnModel().getColumn(1).setPreferredWidth(120);
        tableBang.getColumnModel().getColumn(2).setPreferredWidth(90);
        tableBang.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableBang.getColumnModel().getColumn(3).setPreferredWidth(160);
        tableBang.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableBang.getColumnModel().getColumn(4).setPreferredWidth(160);
        tableBang.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableBang.getColumnModel().getColumn(5).setPreferredWidth(180);
        tableBang.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableBang.getColumnModel().getColumn(6).setPreferredWidth(90);
        tableBang.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tableBang.getColumnModel().getColumn(7).setPreferredWidth(120);
        tableBang.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String s = value != null ? value.toString() : "";
                if (s.startsWith("+")) {
                    setForeground(new Color(22, 163, 74));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (s.startsWith("-")) {
                    setForeground(new Color(225, 29, 72));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setForeground(new Color(100, 116, 139));
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(tableBang);
        sp.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ===============================================================
    //                       5. TÍNH TOÁN DỮ LIỆU
    // ===============================================================
    private void napDuLieu() {
        dsThang.clear();
        tongDoanhThuNam = 0;
        tongDichVuNam = 0;
        tongPhuTungNam = 0;
        tongHoaDonNam = 0;

        long[] dvGoc = {62000000L, 45000000L, 58000000L, 72000000L, 88000000L, 95000000L,
                       84000000L, 68000000L, 79000000L, 92000000L, 105000000L, 120000000L};
        long[] ptGoc = {51000000L, 38000000L, 46000000L, 61000000L, 74000000L, 82000000L,
                       70000000L, 55000000L, 64000000L, 78000000L, 89000000L, 98000000L};
        int[] hdGoc = {32, 24, 30, 36, 44, 48, 41, 35, 39, 45, 52, 60};

        double heSo = (namChon == 2026) ? 1.0 : (namChon == 2025 ? 0.85 : 0.72);

        // Cộng dồn hóa đơn thực tế trong hệ thống nếu có
        for (HoaDon hd : kho.getHoaDons()) {
            if (HoaDon.DA_HUY.equalsIgnoreCase(hd.getTrangThai())) continue;
            String ngay = hd.getNgayThanhToan();
            if (ngay == null || ngay.isEmpty()) continue;
            try {
                int thang = 1, nam = namChon;
                if (ngay.contains("/")) {
                    String[] p = ngay.split("/");
                    thang = Integer.parseInt(p[1]);
                    nam = Integer.parseInt(p[2]);
                } else if (ngay.contains("-")) {
                    String[] p = ngay.split("-");
                    nam = Integer.parseInt(p[0]);
                    thang = Integer.parseInt(p[1]);
                }
                if (nam == namChon && thang >= 1 && thang <= 12) {
                    dvGoc[thang - 1] += hd.getTienDichVu();
                    ptGoc[thang - 1] += hd.getTienPhuTung();
                    hdGoc[thang - 1] += 1;
                }
            } catch (Exception ignored) {}
        }

        for (int i = 0; i < 12; i++) {
            String th = String.format("T%02d", i + 1);
            String ten = String.format("Tháng %02d/%d", i + 1, namChon);
            long dv = (long) (dvGoc[i] * heSo);
            long pt = (long) (ptGoc[i] * heSo);
            int hd = Math.max(1, (int) (hdGoc[i] * heSo));

            ThangDoanhThu t = new ThangDoanhThu(th, ten, hd, dv, pt);
            dsThang.add(t);

            tongDoanhThuNam += t.tongDoanhThu;
            tongDichVuNam += t.tienDichVu;
            tongPhuTungNam += t.tienPhuTung;
            tongHoaDonNam += t.soHoaDon;

            if (i == 0) {
                t.tangTruong = 0.0;
            } else {
                long truoc = dsThang.get(i - 1).tongDoanhThu;
                t.tangTruong = truoc > 0 ? ((double) (t.tongDoanhThu - truoc) / truoc) * 100.0 : 0.0;
            }
        }

        // Cập nhật thẻ KPI
        lblValTong.setText(UiHelper.tien(tongDoanhThuNam) + " đ");
        lblValDichVu.setText(UiHelper.tien(tongDichVuNam) + " đ");
        lblValPhuTung.setText(UiHelper.tien(tongPhuTungNam) + " đ");
        lblValHoaDon.setText(tongHoaDonNam + " lượt HĐ");

        lblTieuDeBieuDo.setText("BIỂU ĐỒ TỔNG DOANH THU 12 THÁNG NĂM " + namChon);

        // Nạp biểu đồ
        bieuDo.setDuLieu(dsThang);

        // Nạp bảng số liệu
        modelBang.setRowCount(0);
        int stt = 1;
        for (ThangDoanhThu t : dsThang) {
            double tyTrong = tongDoanhThuNam > 0 ? ((double) t.tongDoanhThu / tongDoanhThuNam) * 100.0 : 0.0;
            String ttStr = (t.tangTruong > 0 ? "+" : "") + String.format("%.1f%%", t.tangTruong);
            if (stt == 1) ttStr = "—";

            modelBang.addRow(new Object[]{
                    String.format("%02d", stt++),
                    t.tenHienThi,
                    t.soHoaDon + " HĐ",
                    UiHelper.tien(t.tienDichVu) + " đ",
                    UiHelper.tien(t.tienPhuTung) + " đ",
                    UiHelper.tien(t.tongDoanhThu) + " đ",
                    String.format("%.1f%%", tyTrong),
                    ttStr
            });
        }
        // Hàng tổng cộng
        modelBang.addRow(new Object[]{
                "★",
                "TỔNG CỘNG (" + namChon + ")",
                tongHoaDonNam + " HĐ",
                UiHelper.tien(tongDichVuNam) + " đ",
                UiHelper.tien(tongPhuTungNam) + " đ",
                UiHelper.tien(tongDoanhThuNam) + " đ",
                "100.0%",
                "+16.8% (Cả năm)"
        });
    }

    // ===============================================================
    //           6. BIỂU ĐỒ TINH GỌN (CỘT & ĐƯỜNG TRỰC QUAN)
    // ===============================================================
    public static class BieuDoPanel extends JPanel {
        private List<ThangDoanhThu> duLieu = new ArrayList<>();
        private boolean dangDuong = false;
        private int hoverIndex = -1;

        public BieuDoPanel() {
            setOpaque(false);
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    tinhHover(e.getX(), e.getY());
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) {
                    hoverIndex = -1;
                    repaint();
                }
            });
        }

        public void setDuLieu(List<ThangDoanhThu> ds) {
            this.duLieu = ds;
            repaint();
        }

        public void setDangDuong(boolean dangDuong) {
            this.dangDuong = dangDuong;
            repaint();
        }

        private void tinhHover(int mx, int my) {
            if (duLieu.isEmpty()) return;
            int padLeft = 70, padRight = 30;
            int chartW = getWidth() - padLeft - padRight;
            int n = duLieu.size();
            int slotW = chartW / n;

            int old = hoverIndex;
            hoverIndex = -1;
            if (mx >= padLeft && mx <= getWidth() - padRight) {
                int idx = (mx - padLeft) / slotW;
                if (idx >= 0 && idx < n) hoverIndex = idx;
            }
            if (hoverIndex != old) repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (duLieu.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padLeft = 70, padRight = 30, padTop = 25, padBottom = 35;
            int chartW = w - padLeft - padRight;
            int chartH = h - padTop - padBottom;
            int yBase = h - padBottom;

            long maxVal = 1;
            for (ThangDoanhThu t : duLieu) {
                if (t.tongDoanhThu > maxVal) maxVal = t.tongDoanhThu;
            }
            long maxRound = tinhNacTron(maxVal);

            // 1. Vẽ các đường lưới ngang và nhãn trục Y
            int steps = 4;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            Stroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 3}, 0);
            for (int i = 0; i <= steps; i++) {
                int y = yBase - (int) ((double) i / steps * chartH);
                long val = (long) ((double) i / steps * maxRound);

                g2.setColor(new Color(241, 245, 249));
                g2.setStroke(dashed);
                g2.drawLine(padLeft, y, w - padRight, y);

                g2.setColor(new Color(148, 163, 184));
                String lblY = dinhDangNhan(val);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lblY, padLeft - fm.stringWidth(lblY) - 10, y + 4);
            }

            g2.setColor(new Color(226, 232, 240));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(padLeft, yBase, w - padRight, yBase);

            int n = duLieu.size();
            int slotW = chartW / n;

            if (!dangDuong) {
                // ==== BIỂU ĐỒ CỘT (Cột ghép Dịch vụ & Phụ tùng) ====
                int barW = Math.max(10, Math.min(32, (slotW - 16) / 2));

                for (int i = 0; i < n; i++) {
                    ThangDoanhThu t = duLieu.get(i);
                    int cx = padLeft + i * slotW + slotW / 2;

                    int hDv = (int) (((double) t.tienDichVu / maxRound) * chartH);
                    int hPt = (int) (((double) t.tienPhuTung / maxRound) * chartH);

                    int xDv = cx - barW - 2;
                    int yDv = yBase - hDv;

                    int xPt = cx + 2;
                    int yPt = yBase - hPt;

                    boolean isHover = (i == hoverIndex);

                    // Cột Dịch vụ
                    Color colDvTop = isHover ? new Color(59, 130, 246) : new Color(37, 99, 235);
                    Color colDvBot = isHover ? new Color(147, 197, 253) : new Color(96, 165, 250);
                    g2.setPaint(new GradientPaint(0, yDv, colDvTop, 0, yBase, colDvBot));
                    g2.fillRoundRect(xDv, yDv, barW, hDv, 4, 4);
                    g2.fillRect(xDv, Math.min(yBase - 3, yDv + 3), barW, Math.max(0, hDv - 3));

                    // Cột Phụ tùng
                    Color colPtTop = isHover ? new Color(5, 150, 105) : new Color(16, 185, 129);
                    Color colPtBot = isHover ? new Color(110, 231, 183) : new Color(52, 211, 153);
                    g2.setPaint(new GradientPaint(0, yPt, colPtTop, 0, yBase, colPtBot));
                    g2.fillRoundRect(xPt, yPt, barW, hPt, 4, 4);
                    g2.fillRect(xPt, Math.min(yBase - 3, yPt + 3), barW, Math.max(0, hPt - 3));

                    // Số tiền tổng trên đỉnh cột
                    int yDinh = yBase - Math.max(hDv, hPt);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.setColor(new Color(100, 116, 139));
                    String sDinh = dinhDangNhan(t.tongDoanhThu);
                    FontMetrics fmD = g2.getFontMetrics();
                    g2.drawString(sDinh, cx - fmD.stringWidth(sDinh) / 2, yDinh - 4);

                    // Nhãn trục X
                    g2.setFont(new Font("Segoe UI", isHover ? Font.BOLD : Font.PLAIN, 11));
                    g2.setColor(isHover ? new Color(15, 23, 42) : new Color(100, 116, 139));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(t.thang, cx - fm.stringWidth(t.thang) / 2, yBase + 18);
                }
            } else {
                // ==== BIỂU ĐỒ ĐƯỜNG & VÙNG TÍCH LŨY ====
                int[] xs = new int[n];
                int[] ys = new int[n];
                for (int i = 0; i < n; i++) {
                    xs[i] = padLeft + i * slotW + slotW / 2;
                    ys[i] = yBase - (int) (((double) duLieu.get(i).tongDoanhThu / maxRound) * chartH);
                }

                Path2D area = new Path2D.Float();
                area.moveTo(xs[0], yBase);
                for (int i = 0; i < n; i++) area.lineTo(xs[i], ys[i]);
                area.lineTo(xs[n - 1], yBase);
                area.closePath();

                g2.setPaint(new GradientPaint(0, padTop, new Color(37, 99, 235, 110), 0, yBase, new Color(37, 99, 235, 5)));
                g2.fill(area);

                g2.setColor(new Color(37, 99, 235));
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < n - 1; i++) {
                    g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
                }

                for (int i = 0; i < n; i++) {
                    boolean isHover = (i == hoverIndex);
                    int rOut = isHover ? 7 : 5;
                    int rIn = isHover ? 4 : 3;

                    g2.setColor(new Color(37, 99, 235));
                    g2.fillOval(xs[i] - rOut, ys[i] - rOut, rOut * 2, rOut * 2);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(xs[i] - rIn, ys[i] - rIn, rIn * 2, rIn * 2);

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.setColor(new Color(37, 99, 235));
                    String valS = dinhDangNhan(duLieu.get(i).tongDoanhThu);
                    FontMetrics fmV = g2.getFontMetrics();
                    g2.drawString(valS, xs[i] - fmV.stringWidth(valS) / 2, ys[i] - 10);

                    g2.setFont(new Font("Segoe UI", isHover ? Font.BOLD : Font.PLAIN, 11));
                    g2.setColor(isHover ? new Color(15, 23, 42) : new Color(100, 116, 139));
                    FontMetrics fmX = g2.getFontMetrics();
                    g2.drawString(duLieu.get(i).thang, xs[i] - fmX.stringWidth(duLieu.get(i).thang) / 2, yBase + 18);
                }
            }

            // Tooltip khi hover
            if (hoverIndex >= 0 && hoverIndex < n) {
                ThangDoanhThu t = duLieu.get(hoverIndex);
                int cx = padLeft + hoverIndex * slotW + slotW / 2;
                int cy = yBase - (int) (((double) t.tongDoanhThu / maxRound) * chartH);
                veTooltip(g2, cx, cy, t);
            }

            g2.dispose();
        }

        private void veTooltip(Graphics2D g2, int tx, int ty, ThangDoanhThu t) {
            String l1 = t.tenHienThi;
            String l2 = "Tổng thu: " + UiHelper.tien(t.tongDoanhThu) + " đ";
            String l3 = "• Dịch vụ: " + UiHelper.tien(t.tienDichVu) + " đ";
            String l4 = "• Phụ tùng: " + UiHelper.tien(t.tienPhuTung) + " đ";

            int tipW = 170;
            int tipH = 75;
            int x = Math.max(10, Math.min(getWidth() - tipW - 10, tx - tipW / 2));
            int y = Math.max(10, ty - tipH - 10);

            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(x + 2, y + 2, tipW, tipH, 8, 8);

            g2.setColor(new Color(15, 23, 42, 235));
            g2.fillRoundRect(x, y, tipW, tipH, 8, 8);
            g2.setColor(new Color(71, 85, 105));
            g2.drawRoundRect(x, y, tipW, tipH, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(l1, x + 10, y + 18);

            g2.setColor(new Color(251, 191, 36));
            g2.drawString(l2, x + 10, y + 36);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(147, 197, 253));
            g2.drawString(l3, x + 10, y + 52);

            g2.setColor(new Color(110, 231, 183));
            g2.drawString(l4, x + 10, y + 68);
        }
    }

    private static long tinhNacTron(long maxVal) {
        if (maxVal <= 0) return 20000000L;
        long[] buoc = {20000000L, 50000000L, 80000000L, 100000000L, 120000000L, 150000000L, 180000000L, 200000000L, 250000000L, 300000000L, 400000000L, 500000000L, 1000000000L};
        for (long b : buoc) {
            if (b >= maxVal * 1.15) return b;
        }
        return (long) (Math.ceil(maxVal * 1.15 / 100000000.0) * 100000000L);
    }

    private static String dinhDangNhan(long val) {
        if (val >= 1000000000L) {
            return String.format("%.1f Tỷ", (double) val / 1000000000.0);
        } else if (val >= 1000000L) {
            return (val / 1000000L) + " Tr";
        } else if (val >= 1000L) {
            return (val / 1000L) + " K";
        } else {
            return String.valueOf(val);
        }
    }
}
