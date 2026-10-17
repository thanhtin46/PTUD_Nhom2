package UI;

import Entity.NhanVien;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Main_UI — Cửa sổ chính hiện đại của ứng dụng theo bản thiết kế Figma:
 * - Sidebar Tone Slate 900 đậm chất công nghệ, logo badge bo tròn có độ chuyển màu
 * - Thẻ tài khoản (User Card) hiệu ứng kính thủy tinh (Glassmorphism) mờ ảo
 * - Thanh menu phân nhóm trực quan, nút được chọn có dải sáng Accent Blue
 * - Nút đăng xuất cảnh báo đỏ tinh tế
 * - Content Panel mượt mà, phân cấp khoảng cách thoáng đãng
 */
public class Main_UI extends JFrame {

    // ==== Trạng thái ====
    private final NhanVien nhanVienHienTai;
    private final ArrayList<NhanVien> danhSachNhanVien;

    // ==== UI ====
    private JPanel contentPanel;   // CENTER: chứa các panel nội dung
    private MenuRow nutDangChon;   // nút menu đang được chọn
    private MenuRow nutDauTien;    // nút đầu tiên để auto-click khi mở app

    // ==== Hằng số phân quyền ====
    private static final String TIEP_TAN = "Nhân viên tiếp tân";
    private static final String SUA_CHUA = "Nhân viên sửa chữa";
    private static final String KHO = "Nhân viên kho";
    private static final String QUAN_LY = "Quản lý";

    // ==== Hằng số giao diện đồng bộ Figma ====
    private static final Color NEN_SIDE             = new Color(15, 23, 42);       // Slate 900
    private static final Color NEN_HOVER            = new Color(30, 41, 59);       // Slate 800
    private static final Color MAU_CHON             = new Color(37, 99, 235);      // Blue 600
    private static final Color MAU_LOGO_START       = new Color(37, 99, 235);      // Gradient Blue
    private static final Color MAU_LOGO_END         = new Color(14, 165, 233);     // Sky Blue
    private static final Color CHU_CHINH            = new Color(248, 250, 252);    // Slate 50
    private static final Color CHU_PHU              = new Color(148, 163, 184);    // Slate 400
    private static final Color CHU_NUT_DANG_XUAT    = new Color(248, 113, 113);    // Red 400
    private static final Color NEN_HOVER_DANG_XUAT  = new Color(69, 10, 10, 150);  // Dark Red

    private static final int W = 250;          // Chiều rộng sidebar chuẩn
    private static final int NUT_CAO = 42;     // Chiều cao nút menu thoải mái, chuẩn UX
    private static final int NUT_RONG = 230;   // Chiều rộng mỗi nút menu

    public Main_UI(NhanVien nv, ArrayList<NhanVien> danhSachNhanVien) {
        this.nhanVienHienTai = nv;
        this.danhSachNhanVien = danhSachNhanVien;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        initComponents();
    }

    private void initComponents() {
        setTitle("Hệ thống quản lý Gara ô tô — The T4 Auto");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.min(1480, screen.width - 40);
        int h = Math.min(840, screen.height - 80);
        setSize(w, h);
        setMinimumSize(new Dimension(1150, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UiHelper.NEN);

        add(taoSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Mở mục đầu tiên
        if (nutDauTien != null) {
            chonMenu(nutDauTien, nutDauTien.text);
        }
    }

    // ===============================================================
    //                       SIDEBAR HIỆN ĐẠI
    // ===============================================================
    private JPanel taoSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(NEN_SIDE);
        sidebar.setPreferredSize(new Dimension(W, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // === 1. Logo + brand ===
        sidebar.add(taoLogo());
        sidebar.add(filler(10, W));

        // === 2. User card ===
        sidebar.add(taoUserCard());
        sidebar.add(filler(14, W));

        // === 3. Nhãn MENU ===
        sidebar.add(nhan("DANH MỤC QUẢN LÝ", 6));
        sidebar.add(filler(4, W));

        // === 4. Danh sách menu (cuộn được) ===
        sidebar.add(taoMenuCuon());

        // === 5. Đẩy mọi thứ phía dưới xuống đáy ===
        sidebar.add(Box.createVerticalGlue());

        // === 6. Nút đăng xuất ===
        sidebar.add(taoNutDangXuat());
        sidebar.add(filler(12, W));

        return sidebar;
    }

    /** Tạo header logo + brand với gradient */
    private JPanel taoLogo() {
        JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        box.setOpaque(false);
        box.setMaximumSize(new Dimension(W, 68));
        box.setPreferredSize(new Dimension(W, 68));
        box.setMinimumSize(new Dimension(W, 68));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(new EmptyBorder(18, 16, 12, 16));

        // Badge logo gradient nổi bật
        JLabel badge = new JLabel("T4") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, MAU_LOGO_START, getWidth(), getHeight(), MAU_LOGO_END);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String s = "T4";
                int tx = (getWidth() - fm.stringWidth(s)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(s, tx, ty);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(36, 36));
        badge.setOpaque(false);
        box.add(badge);

        JLabel brand = new JLabel("<html><b style='color:#F8FAFC;font-size:14px'>Gara Ô tô The T4</b>"
                + "<br><span style='color:#94A3B8;font-size:10px;letter-spacing:1px'>HỆ THỐNG QUẢN LÝ GARA</span></html>");
        brand.setOpaque(false);
        box.add(brand);

        return box;
    }

    /** Thẻ hiển thị nhân viên đang đăng nhập với avatar tròn gradient */
    private JPanel taoUserCard() {
        JPanel userWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        userWrap.setOpaque(false);
        userWrap.setMaximumSize(new Dimension(W, 68));
        userWrap.setPreferredSize(new Dimension(W, 68));
        userWrap.setMinimumSize(new Dimension(W, 68));
        userWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        userWrap.setBorder(new EmptyBorder(0, 12, 0, 12));

        JPanel user = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59, 180)); // Glass effect
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        user.setOpaque(false);
        user.setPreferredSize(new Dimension(226, 56));
        user.setMaximumSize(new Dimension(226, 56));
        user.setMinimumSize(new Dimension(226, 56));
        user.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Avatar gradient
        JPanel avatar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(59, 130, 246), getWidth(), getHeight(), new Color(147, 51, 234));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));
        String ten = nhanVienHienTai.getHoTen();
        String chuCaiDau = ten == null || ten.isEmpty() ? "?" : ten.substring(0, 1).toUpperCase();
        JLabel lblChu = new JLabel(chuCaiDau, SwingConstants.CENTER);
        lblChu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChu.setForeground(Color.WHITE);
        avatar.add(lblChu, BorderLayout.CENTER);
        user.add(avatar, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setAlignmentY(Component.CENTER_ALIGNMENT);

        String tenHien = ten == null ? "" : ten;
        String vaiTroHien = nhanVienHienTai.getVaiTro() == null ? "" : nhanVienHienTai.getVaiTro();

        JLabel lblTen = new JLabel(tenHien);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTen.setForeground(CHU_CHINH);
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVaiTro = new JLabel(vaiTroHien);
        lblVaiTro.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVaiTro.setForeground(CHU_PHU);
        lblVaiTro.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(lblTen);
        info.add(Box.createVerticalStrut(2));
        info.add(lblVaiTro);
        user.add(info, BorderLayout.CENTER);

        userWrap.add(user);
        return userWrap;
    }

    private JLabel nhan(String text, int bottomPad) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(100, 116, 139));
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(0, 20, bottomPad, 0));
        l.setMaximumSize(new Dimension(W, 22));
        l.setPreferredSize(new Dimension(W, 22));
        l.setMinimumSize(new Dimension(W, 22));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private Component filler(int h, int w) {
        Dimension d = new Dimension(w, h);
        Box.Filler f = new Box.Filler(d, d, d);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setAlignmentY(Component.TOP_ALIGNMENT);
        return f;
    }

    private JScrollPane taoMenuCuon() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(NEN_SIDE);
        menuPanel.setOpaque(true);
        menuPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        menuPanel.setBorder(new EmptyBorder(0, 8, 0, 8));

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
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(51, 65, 85);
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

    private void themMucMenu(JPanel menuPanel, MucMenu muc) {
        if (menuPanel.getComponentCount() > 0) {
            menuPanel.add(filler(6, NUT_RONG));
        }
        if (!muc.laNhom()) {
            MenuRow nut = taoNutMenu(muc.ten, 0, false);
            nut.addActionListener(e -> chonMenu(nut, muc.ten));
            menuPanel.add(nut);
            if (nutDauTien == null) nutDauTien = nut;
            return;
        }
        // Nhóm cha
        MenuRow nutCha = taoNutMenu(muc.ten, 0, true);
        nutCha.addActionListener(e -> chonMenu(nutCha, muc.ten));
        menuPanel.add(nutCha);
        if (nutDauTien == null) nutDauTien = nutCha;

        // Các mục con
        for (MucMenu con : muc.danhSachCon) {
            menuPanel.add(filler(2, NUT_RONG));
            MenuRow nutCon = taoNutMenu("   •  " + con.ten, 1, false);
            nutCon.addActionListener(e -> chonMenu(nutCon, con.ten));
            menuPanel.add(nutCon);
        }
    }

    private MenuRow taoNutMenu(String text, int capDo, boolean laNhom) {
        int trai = (laNhom || capDo == 0) ? 14 : 26;
        MenuRow row = new MenuRow(text, trai, laNhom);
        row.setPreferredSize(new Dimension(NUT_RONG, NUT_CAO));
        row.setMinimumSize(new Dimension(NUT_RONG, NUT_CAO));
        row.setMaximumSize(new Dimension(NUT_RONG, NUT_CAO));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setAlignmentY(Component.TOP_ALIGNMENT);
        return row;
    }

    // ===============================================================
    // MenuRow — Dòng menu tương tác bóng bẩy, bo góc 10px
    // ===============================================================
    private class MenuRow extends JPanel {
        private final String text;
        private final int paddingLeft;
        private final boolean laNhom;
        private Color bgHien = null;
        private Color fgHien = new Color(203, 213, 225); // Slate 300
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
                fgHien = new Color(203, 213, 225);
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            Color nen = bgHien != null ? bgHien : (dangHover ? NEN_HOVER : null);

            if (nen != null) {
                g2.setColor(nen);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 10, 10);
            }

            // Thanh accent trắng ở mép trái khi được chọn
            if (isSelected()) {
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillRoundRect(4, 8, 4, h - 16, 4, 4);
            }

            g2.setColor(fgHien);
            g2.setFont(new Font("Segoe UI", laNhom ? Font.BOLD : Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, paddingLeft, textY);

            g2.dispose();
        }
    }

    /** Nút đăng xuất hiện đại có icon biểu tượng */
    private JButton taoNutDangXuat() {
        JButton nut = new JButton("Đăng xuất");
        nut.setIcon(UiIcon.of(UiIcon.LOGOUT, 16, CHU_NUT_DANG_XUAT));
        nut.setIconTextGap(10);
        nut.setFocusPainted(false);
        nut.setForeground(CHU_NUT_DANG_XUAT);
        nut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nut.setHorizontalAlignment(SwingConstants.LEFT);
        nut.setBorder(new EmptyBorder(8, 20, 8, 8));
        nut.setMaximumSize(new Dimension(W, 40));
        nut.setPreferredSize(new Dimension(W, 40));
        nut.setMinimumSize(new Dimension(W, 40));
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
            wrap.setBackground(UiHelper.NEN);
            wrap.add(noiDung, BorderLayout.CENTER);
            contentPanel.add(wrap, BorderLayout.CENTER);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void chuyenTrang(String tenMenu) {
        JComponent noiDung = taoNoiDung(tenMenu);
        contentPanel.removeAll();
        contentPanel.add(noiDung, BorderLayout.CENTER);
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
            case "Thanh toán hóa đơn"      -> new ThanhToanHoaDon_UI(nhanVienHienTai);
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
            case "Quản lý khách hàng"     -> new QuanLyKhachHang_UI();
            case "Quản lý phương tiện"     -> new QuanLyPhuongTien_UI();
            case "Quản lý bảo hành"        -> new TraCuuBaoHanh_UI();
            case "Quản lý phụ tùng"        -> new DanhSachPhuTung_UI();
            case "Báo cáo doanh thu"       -> new BaoCaoDoanhThu_UI(false);
            default -> {
                JLabel lbl = new JLabel("Chức năng đang phát triển: " + tenMenu, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lbl.setForeground(UiHelper.XANH_DAM);
                yield lbl;
            }
        };
    }

    // ===============================================================
    //                       DỮ LIỆU MENU
    // ===============================================================
    private static class MucMenu {
        final String ten;
        final String vaiTro;
        final ArrayList<MucMenu> danhSachCon = new ArrayList<>();

        MucMenu(String ten, String vaiTro) {
            this.ten = ten; this.vaiTro = vaiTro;
        }
        MucMenu them(MucMenu con) { danhSachCon.add(con); return this; }
        boolean laNhom() { return !danhSachCon.isEmpty(); }
        boolean coQuyen(String vaiTroNV) {
            if (vaiTro == null) return true;
            if (QUAN_LY.equals(vaiTroNV)) return true;
            return vaiTro.equals(vaiTroNV);
        }
        static MucMenu item(String ten) {
            return new MucMenu(ten, null);
        }
        static MucMenu item(String ten, String vaiTro) {
            return new MucMenu(ten, vaiTro);
        }
        static MucMenu nhom(String ten, String vaiTro) {
            return new MucMenu(ten, vaiTro);
        }
    }

    private ArrayList<MucMenu> taoDanhSachMenu() {
        ArrayList<MucMenu> ds = new ArrayList<>();
        ds.add(MucMenu.item("Trang chủ"));

        ds.add(MucMenu.nhom("Quản lý khách hàng", TIEP_TAN)
                .them(MucMenu.item("Danh sách khách hàng"))
                .them(MucMenu.item("Tra cứu khách hàng")));

        ds.add(MucMenu.nhom("Quản lý phương tiện", TIEP_TAN)
                .them(MucMenu.item("Danh sách phương tiện"))
                .them(MucMenu.item("Tra cứu phương tiện")));

        ds.add(MucMenu.nhom("Quản lý bảo hành", TIEP_TAN)
                .them(MucMenu.item("Tra cứu bảo hành"))
                .them(MucMenu.item("Lập phiếu bảo hành")));

        ds.add(MucMenu.item("Lập phiếu sửa chữa", SUA_CHUA));
        ds.add(MucMenu.item("Thanh toán hóa đơn"));
        ds.add(MucMenu.item("Phân công KTV", SUA_CHUA));
        ds.add(MucMenu.item("Cập nhật tiến độ", SUA_CHUA));

        ds.add(MucMenu.nhom("Quản lý phụ tùng", KHO)
                .them(MucMenu.item("Danh sách phụ tùng"))
                .them(MucMenu.item("Nhập kho"))
                .them(MucMenu.item("Xuất kho"))
                .them(MucMenu.item("Theo dõi chờ nhập")));
        ds.add(MucMenu.item("Báo cáo tồn kho", KHO));

        ds.add(MucMenu.item("Quản lý nhân viên", QUAN_LY));
        ds.add(MucMenu.item("Phân quyền", QUAN_LY));
        ds.add(MucMenu.nhom("Báo cáo doanh thu", QUAN_LY)
                .them(MucMenu.item("Theo tháng"))
                .them(MucMenu.item("Theo năm")));
        return ds;
    }
}
