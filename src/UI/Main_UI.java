package UI;

import Entity.NhanVien;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Main_UI — Cửa sổ chính của ứng dụng.
 * Bố cục: WEST = sidebar (logo, user, menu, đăng xuất), CENTER = nội dung.
 *
 * Menu dùng BoxLayout Y_AXIS trong JScrollPane — cách chuẩn của Swing.
 * Mỗi mục = 1 dòng cố định 224×32 nên KHÔNG thể overlap.
 */
public class Main_UI extends JFrame {

    // ==== Trạng thái ====
    private final NhanVien nhanVienHienTai;
    private final ArrayList<NhanVien> danhSachNhanVien;

    // ==== UI ====
    private JPanel contentPanel;   // CENTER: chứa các panel nội dung
    private MenuRow nutDangChon;   // nút menu đang được chọn (tô xanh)
    private MenuRow nutDauTien;    // nút đầu tiên để auto-click khi mở app

    // ==== Hằng số phân quyền ====
    private static final String TIEP_TAN = "Nhân viên tiếp tân";
    private static final String SUA_CHUA = "Nhân viên sửa chữa";
    private static final String KHO = "Nhân viên kho";
    private static final String QUAN_LY = "Quản lý";

    // ==== Hằng số giao diện ====
    private static final Color NEN_SIDE     = new Color(24, 28, 36);
    private static final Color NEN_HOVER    = new Color(255, 255, 255, 16);
    private static final Color MAU_CHON     = new Color(40, 96, 220);
    private static final Color MAU_LOGO     = new Color(40, 96, 220);
    private static final Color CHU_CHINH    = new Color(232, 236, 244);
    private static final Color CHU_PHU      = new Color(140, 150, 168);
    private static final Color CHU_NUT_DANG_XUAT = new Color(220, 130, 130);
    private static final Color NEN_HOVER_DANG_XUAT = new Color(60, 30, 30);

    private static final int W = 240;          // chiều rộng sidebar
    private static final int NUT_CAO = 38;     // chiều cao mỗi nút menu
    private static final int NUT_RONG = 224;   // chiều rộng mỗi nút menu (trừ border 8+8)

    public Main_UI(NhanVien nv, ArrayList<NhanVien> danhSachNhanVien) {
        this.nhanVienHienTai = nv;
        this.danhSachNhanVien = danhSachNhanVien;

        // Ép LAF Windows cho title bar + nút ✕
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        initComponents();
    }

    private void initComponents() {
        setTitle("Hệ thống quản lý Gara ô tô");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.min(1440, screen.width  - 40);
        int h = Math.min(800,  screen.height - 120);
        setSize(w, h);
        setMinimumSize(new Dimension(1100, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(243, 246, 250));

        add(taoSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Mở mục đầu tiên
        if (nutDauTien != null) {
            chonMenu(nutDauTien, nutDauTien.text);
        }
    }

    // ===============================================================
    //                       SIDEBAR
    // ===============================================================
    private JPanel taoSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(NEN_SIDE);
        sidebar.setPreferredSize(new Dimension(W, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // === 1. Logo + brand ===
        sidebar.add(taoLogo());
        sidebar.add(filler(12, W));

        // === 2. User card ===
        sidebar.add(taoUserCard());
        sidebar.add(filler(12, W));

        // === 3. Nhãn MENU ===
        sidebar.add(nhan("MENU", 4));
        sidebar.add(filler(4, W));

        // === 4. Danh sách menu (cuộn được) ===
        sidebar.add(taoMenuCuon());

        // === 5. Đẩy mọi thứ phía dưới xuống đáy ===
        sidebar.add(Box.createVerticalGlue());

        // === 6. Nút đăng xuất ===
        sidebar.add(taoNutDangXuat());
        sidebar.add(filler(10, W));

        return sidebar;
    }

    /** Tạo header logo + brand "Auto Service" */
    private JPanel taoLogo() {
        JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        box.setOpaque(false);
        box.setMaximumSize(new Dimension(W, 60));
        box.setPreferredSize(new Dimension(W, 60));
        box.setMinimumSize(new Dimension(W, 60));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(new EmptyBorder(18, 16, 12, 16));

        // Badge "A" bo tròn
        JLabel badge = new JLabel("A") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAU_LOGO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String s = "A";
                int tx = (getWidth()  - fm.stringWidth(s)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(s, tx, ty);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(28, 28));
        badge.setOpaque(false);
        box.add(badge);

        JLabel brand = new JLabel("<html><b style='color:#E8ECF4;font-size:14px'>Auto Service</b>"
                + "<br><span style='color:#8C96A8;font-size:10px'>Garage Management</span></html>");
        brand.setOpaque(false);
        box.add(brand);

        return box;
    }

    /** Thẻ hiển thị nhân viên đang đăng nhập */
    private JPanel taoUserCard() {
        JPanel userWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        userWrap.setOpaque(false);
        userWrap.setMaximumSize(new Dimension(W, 56));
        userWrap.setPreferredSize(new Dimension(W, 56));
        userWrap.setMinimumSize(new Dimension(W, 56));
        userWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        userWrap.setBorder(new EmptyBorder(0, 12, 0, 12));

        JPanel user = new JPanel(new BorderLayout(10, 0));
        user.setBackground(new Color(255, 255, 255, 12));
        user.setOpaque(true);
        user.setPreferredSize(new Dimension(216, 44));
        user.setMaximumSize(new Dimension(216, 44));
        user.setMinimumSize(new Dimension(216, 44));
        user.setBorder(new EmptyBorder(6, 10, 6, 10));

        // Avatar
        JPanel avatar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 140, 230));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(32, 32));
        String ten = nhanVienHienTai.getHoTen();
        String chuCaiDau = ten == null || ten.isEmpty() ? "?" : ten.substring(0, 1).toUpperCase();
        JLabel lblChu = new JLabel(chuCaiDau, SwingConstants.CENTER);
        lblChu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChu.setForeground(Color.WHITE);
        avatar.add(lblChu, BorderLayout.CENTER);
        user.add(avatar, BorderLayout.WEST);

        // Tên + vai trò
        String tenHien = escapeHtml(ten == null ? "" : ten);
        String vaiTroHien = escapeHtml(nhanVienHienTai.getVaiTro() == null ? "" : nhanVienHienTai.getVaiTro());
        JLabel info = new JLabel("<html><b style='color:#E8ECF4;font-size:12px'>" + tenHien + "</b>"
                + "<br><span style='color:#8C96A8;font-size:10px'>" + vaiTroHien + "</span></html>");
        info.setOpaque(false);
        user.add(info, BorderLayout.CENTER);

        userWrap.add(user);
        return userWrap;
    }

    /** Nhãn đơn giản (MENU, ...) */
    private JLabel nhan(String text, int bottomPad) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(110, 120, 140));
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(0, 20, bottomPad, 0));
        l.setMaximumSize(new Dimension(W, 22));
        l.setPreferredSize(new Dimension(W, 22));
        l.setMinimumSize(new Dimension(W, 22));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    /** Khoảng trống cố định (strut) trong BoxLayout Y_AXIS */
    private Component filler(int h, int w) {
        Dimension d = new Dimension(w, h);
        Box.Filler f = new Box.Filler(d, d, d);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setAlignmentY(Component.TOP_ALIGNMENT);
        return f;
    }

    /**
     * Tạo JScrollPane chứa menuPanel (BoxLayout Y_AXIS).
     * Mỗi nút có kích thước cứng 224×32 → không bao giờ overlap.
     */
    private JScrollPane taoMenuCuon() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(NEN_SIDE);
        menuPanel.setOpaque(true);
        menuPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        menuPanel.setBorder(new EmptyBorder(0, 8, 0, 8));

        // Thêm các mục menu
        for (MucMenu muc : taoDanhSachMenu()) {
            if (muc.coQuyen(nhanVienHienTai.getVaiTro())) {
                themMucMenu(menuPanel, muc);
            }
        }

        JScrollPane sp = new JScrollPane(menuPanel);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(120, 130, 150);
                this.trackColor = new Color(0, 0, 0, 0);
            }
            private JButton zeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setOpaque(false); return b; }
        });
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setAlignmentY(Component.TOP_ALIGNMENT);
        sp.setMaximumSize(new Dimension(W, Integer.MAX_VALUE));
        sp.setMinimumSize(new Dimension(W, 0));
        return sp;
    }

    /**
     * Thêm 1 mục menu (có thể là item đơn hoặc nhóm có mục con).
     * Mỗi nút = 1 JButton 224×32 cứng → BoxLayout xếp tuyệt đối.
     */
    private void themMucMenu(JPanel menuPanel, MucMenu muc) {
        // Khoảng cách giữa các nhóm
        if (menuPanel.getComponentCount() > 0) {
            menuPanel.add(filler(8, NUT_RONG));
        }
        if (!muc.laNhom()) {
            MenuRow nut = taoNutMenu(muc.ten, muc.icon, 0, false);
            nut.addActionListener(e -> chonMenu(nut, muc.ten));
            menuPanel.add(nut);
            if (nutDauTien == null) nutDauTien = nut;
            return;
        }
        // Mục cha
        MenuRow nutCha = taoNutMenu(muc.ten, muc.icon, 0, true);
        nutCha.addActionListener(e -> chonMenu(nutCha, muc.ten));
        menuPanel.add(nutCha);
        if (nutDauTien == null) nutDauTien = nutCha;
        // Mục con — dải phân cách 2px giữa tiêu đề nhóm và item con đầu tiên
        boolean dau = true;
        for (MucMenu con : muc.danhSachCon) {
            if (dau) {
                menuPanel.add(filler(2, NUT_RONG));
                dau = false;
            }
            MenuRow nutCon = taoNutMenu("•  " + con.ten, con.icon, 1, false);
            nutCon.addActionListener(e -> chonMenu(nutCon, con.ten));
            menuPanel.add(nutCon);
        }
    }

    /**
     * Tạo 1 dòng menu = JPanel tùy chỉnh. Vẽ nền + text thủ công, không dùng JButton
     * (JButton có margin/inset nội bộ gây ra text bị lệch/clip khó đoán).
     */
    private MenuRow taoNutMenu(String text, String icon, int capDo, boolean laNhom) {
        int trai = (laNhom || capDo == 0) ? 14 : 30;
        MenuRow row = new MenuRow(text, trai, laNhom);
        row.setPreferredSize(new Dimension(NUT_RONG, NUT_CAO));
        row.setMinimumSize(new Dimension(NUT_RONG, NUT_CAO));
        row.setMaximumSize(new Dimension(NUT_RONG, NUT_CAO));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setAlignmentY(Component.TOP_ALIGNMENT);
        return row;
    }

    // ===============================================================
    // MenuRow — JPanel vẽ 1 dòng menu (nền hover/select + text)
    // ===============================================================
    private class MenuRow extends JPanel {
        private final String text;
        private final int paddingLeft;
        private final boolean laNhom;
        private Color bgHien = null;          // null = không có nền
        private Color fgHien = new Color(200, 208, 220);
        private boolean dangHover = false;
        private final java.util.List<java.awt.event.ActionListener> listeners = new java.util.ArrayList<>();

        MenuRow(String text, int paddingLeft, boolean laNhom) {
            this.text = text;
            this.paddingLeft = paddingLeft;
            this.laNhom = laNhom;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isSelected()) { dangHover = true; repaint(); }
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isSelected()) { dangHover = false; repaint(); }
                }
                @Override public void mousePressed(java.awt.event.MouseEvent e) {
                    fire();
                }
            });
        }
        void setSelected(boolean sel) {
            if (sel) {
                bgHien = MAU_CHON;
                fgHien = Color.WHITE;
                dangHover = false;
            } else {
                bgHien = null;
                fgHien = new Color(200, 208, 220);
            }
            repaint();
        }
        boolean isSelected() {
            return bgHien == MAU_CHON;
        }
        void addActionListener(java.awt.event.ActionListener l) { listeners.add(l); }
        private void fire() {
            for (java.awt.event.ActionListener l : listeners) l.actionPerformed(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            // 1. Nền
            Color nen;
            if (bgHien != null) nen = bgHien;
            else if (dangHover) nen = NEN_HOVER;
            else nen = null;

            if (nen != null) {
                g2.setColor(nen);
                g2.fillRect(0, 0, w, h);
            }

            // 2. Thanh accent trái khi selected
            if (isSelected()) {
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRect(0, 0, 3, h);
            }

            // 3. Text — baseline đặt chính giữa dọc theo font metrics
            g2.setColor(fgHien);
            g2.setFont(new Font("Segoe UI", laNhom ? Font.BOLD : Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, paddingLeft, textY);

            g2.dispose();
        }
    }

    /** Nút đăng xuất ở đáy sidebar */
    private JButton taoNutDangXuat() {
        JButton nut = new JButton("Đăng xuất");
        nut.setFocusPainted(false);
        nut.setForeground(CHU_NUT_DANG_XUAT);
        nut.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nut.setHorizontalAlignment(SwingConstants.LEFT);
        nut.setBorder(new EmptyBorder(8, 20, 8, 8));
        nut.setMaximumSize(new Dimension(W, 36));
        nut.setPreferredSize(new Dimension(W, 36));
        nut.setMinimumSize(new Dimension(W, 36));
        nut.setAlignmentX(Component.LEFT_ALIGNMENT);
        nut.setAlignmentY(Component.TOP_ALIGNMENT);
        nut.setContentAreaFilled(false);
        nut.setOpaque(false);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.addActionListener(e -> {
            dispose();
            new Login_UI(danhSachNhanVien).setVisible(true);
        });
        nut.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                nut.setOpaque(true);
                nut.setBackground(NEN_HOVER_DANG_XUAT);
                nut.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                nut.setOpaque(false);
                nut.setBackground(null);
                nut.repaint();
            }
        });
        return nut;
    }

    // ===============================================================
    //                       CHỌN MENU
    // ===============================================================
    private void chonMenu(MenuRow nut, String tenMenu) {
        if (nutDangChon != null) nutDangChon.setSelected(false);
        nutDangChon = nut;
        nut.setSelected(true);

        JComponent noiDung = taoNoiDung(tenMenu);
        contentPanel.removeAll();
        if (noiDung instanceof JPanel jp) {
            contentPanel.add(jp, BorderLayout.CENTER);
        } else {
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setBackground(new Color(243, 246, 250));
            wrap.add(noiDung, BorderLayout.CENTER);
            contentPanel.add(wrap, BorderLayout.CENTER);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /** Map tên menu → panel nội dung tương ứng */
    private JComponent taoNoiDung(String tenMenu) {
        return switch (tenMenu) {
            case "Trang chủ"               -> new TrangChu_UI();
            case "Danh sách khách hàng"    -> new QuanLyKhachHang_UI();
            case "Tra cứu khách hàng"      -> new TraCuuKhachHang_UI();
            case "Danh sách phương tiện"   -> new QuanLyPhuongTien_UI();
            case "Tra cứu phương tiện"     -> new TraCuuPhuongTien_UI();
            case "Tra cứu bảo hành"        -> new TraCuuBaoHanh_UI();
            case "Lập phiếu bảo hành"      -> new LapPhieuBaoHanh_UI(nhanVienHienTai);
            case "Lập phiếu sửa chữa"      -> new LapPhieuSuaChua_UI(nhanVienHienTai);
            case "Cập nhật tiến độ"        -> new CapNhatTienDo_UI();
            case "Danh sách phụ tùng"      -> new DanhSachPhuTung_UI();
            case "Nhập kho"                -> new NhapKho_UI(nhanVienHienTai);
            case "Xuất kho"                -> new XuatKho_UI(nhanVienHienTai);
            case "Theo dõi chờ nhập"       -> new TheoDoiChoNhap_UI();
            case "Báo cáo tồn kho"         -> new BaoCaoTonKho_UI();
            case "Phân công KTV"           -> new PhanCongKyThuatVien_UI();
            case "Quản lý nhân viên"       -> new QuanLyNhanVien_UI();
            case "Phân quyền"              -> new PhanQuyen_UI();
            case "Theo tháng"              -> new BaoCaoDoanhThu_UI(false);
            case "Theo năm"                -> new BaoCaoDoanhThu_UI(true);
            // Mục cha → mở mục con đầu tiên
            case "Quản lý khách hàng"     -> new QuanLyKhachHang_UI();
            case "Quản lý phương tiện"     -> new QuanLyPhuongTien_UI();
            case "Quản lý bảo hành"        -> new TraCuuBaoHanh_UI();
            case "Quản lý phụ tùng"        -> new DanhSachPhuTung_UI();
            case "Báo cáo doanh thu"       -> new BaoCaoDoanhThu_UI(false);
            default -> {
                JLabel lbl = new JLabel("Chức năng đang phát triển: " + tenMenu, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lbl.setForeground(new Color(21, 58, 110));
                yield lbl;
            }
        };
    }

    // ===============================================================
    //                       DỮ LIỆU MENU
    // ===============================================================

    /** Một mục trong cây menu. Có thể là item đơn hoặc nhóm có mục con. */
    private static class MucMenu {
        final String ten;
        final String icon;
        final String vaiTro;          // null = ai cũng thấy
        final ArrayList<MucMenu> danhSachCon = new ArrayList<>();

        MucMenu(String ten, String icon, String vaiTro) {
            this.ten = ten; this.icon = icon; this.vaiTro = vaiTro;
        }
        MucMenu them(MucMenu con) { danhSachCon.add(con); return this; }
        boolean laNhom() { return !danhSachCon.isEmpty(); }
        boolean coQuyen(String vaiTroNV) {
            // null = mục chung cho mọi vai trò
            if (vaiTro == null) return true;
            // Quản lý thấy tất cả
            if (QUAN_LY.equals(vaiTroNV)) return true;
            // Vai trò đúng mới thấy
            return vaiTro.equals(vaiTroNV);
        }
        static MucMenu item(String ten, String icon) {
            return new MucMenu(ten, icon, null);
        }
        static MucMenu item(String ten, String icon, String vaiTro) {
            return new MucMenu(ten, icon, vaiTro);
        }
        static MucMenu nhom(String ten, String icon, String vaiTro) {
            return new MucMenu(ten, icon, vaiTro);
        }
    }

    /** Cây menu đầy đủ của ứng dụng */
    private ArrayList<MucMenu> taoDanhSachMenu() {
        ArrayList<MucMenu> ds = new ArrayList<>();
        ds.add(MucMenu.item("Trang chủ", "🏠"));

        ds.add(MucMenu.nhom("Quản lý khách hàng", "👥", TIEP_TAN)
                .them(MucMenu.item("Danh sách khách hàng", "•"))
                .them(MucMenu.item("Tra cứu khách hàng", "•")));

        ds.add(MucMenu.nhom("Quản lý phương tiện", "🚗", TIEP_TAN)
                .them(MucMenu.item("Danh sách phương tiện", "•"))
                .them(MucMenu.item("Tra cứu phương tiện", "•")));

        ds.add(MucMenu.nhom("Quản lý bảo hành", "🛡", TIEP_TAN)
                .them(MucMenu.item("Tra cứu bảo hành", "•"))
                .them(MucMenu.item("Lập phiếu bảo hành", "•")));

        ds.add(MucMenu.item("Lập phiếu sửa chữa", "🔧", SUA_CHUA));
        ds.add(MucMenu.item("Cập nhật tiến độ",     "📈", SUA_CHUA));

        ds.add(MucMenu.nhom("Quản lý phụ tùng", "📦", KHO)
                .them(MucMenu.item("Danh sách phụ tùng", "•"))
                .them(MucMenu.item("Nhập kho", "•"))
                .them(MucMenu.item("Xuất kho", "•"))
                .them(MucMenu.item("Theo dõi chờ nhập", "•")));    // UC010
        ds.add(MucMenu.item("Báo cáo tồn kho", "📊", KHO));

        ds.add(MucMenu.item("Phân công KTV", "👷", SUA_CHUA));      // UC015
        ds.add(MucMenu.item("Cập nhật tiến độ", "📈", SUA_CHUA));

        ds.add(MucMenu.item("Quản lý nhân viên", "👨‍💼", QUAN_LY));
        ds.add(MucMenu.item("Phân quyền", "🔐", QUAN_LY));            // UC014
        ds.add(MucMenu.nhom("Báo cáo doanh thu", "📑", QUAN_LY)
                .them(MucMenu.item("Theo tháng", "•"))
                .them(MucMenu.item("Theo năm", "•")));
        return ds;
    }

    // ===============================================================
    //                       TIỆN ÍCH
    // ===============================================================
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
