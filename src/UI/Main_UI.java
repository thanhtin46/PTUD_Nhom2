package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Main_UI — Cửa sổ làm việc chính của Hệ thống Quản lý Gara Ô tô The T4:
 * - Thanh menu gom nhóm 9 phân hệ chuẩn xác theo tài liệu đặc tả và ảnh thiết kế:
 *   1. Khách hàng (Quản lý khách hàng, Tra cứu khách hàng)
 *   2. Phương tiện (Quản lý phương tiện, Tra cứu phương tiện)
 *   3. Phiếu tiếp nhận (Lập phiếu tiếp nhận, Tra cứu phiếu tiếp nhận)
 *   4. Sửa chữa (Lập phiếu sửa chữa, Tra cứu phiếu sửa chữa)
 *   5. Hóa đơn (Lập hóa đơn, Tra cứu hóa đơn)
 *   6. Kho phụ tùng (Quản lý phụ tùng, Nhập kho, Xuất kho, Báo cáo tồn kho)
 *   7. Bảo hành (Lập phiếu bảo hành, Tra cứu phiếu bảo hành)
 *   8. Nhân viên (Quản lý nhân viên, Tra cứu nhân viên)
 *   9. Báo cáo – Thống kê (Thống kê doanh thu)
 * - Tích hợp Trang chủ & Trung tâm Quản trị Hệ thống (Admin Hub) cho Quản lý
 * - Giao diện thanh menu sáng (Light Sidebar) đồng bộ với ảnh thiết kế
 */
public class Main_UI extends JFrame {

    // ==== Trạng thái ====
    private static Main_UI instance;
    private final NhanVien nhanVienHienTai;
    private final ArrayList<NhanVien> danhSachNhanVien;
    private final KhoDuLieu kho = KhoDuLieu.get();

    // ==== UI ====
    private JPanel contentPanel;
    private MenuRow nutDangChon;
    private MenuRow nutDauTien;
    private final ArrayList<MenuRow> tatCaNutMenu = new ArrayList<>();

    // ==== Phân quyền ====
    private static final String TIEP_TAN = "Nhân viên tiếp tân";
    private static final String SUA_CHUA = "Nhân viên sửa chữa";
    private static final String KHO = "Nhân viên kho";
    private static final String THU_NGAN = "Nhân viên thu ngân";
    private static final String QUAN_LY = "Quản lý";

    // ==== Kích thước & Màu sắc ====
    private static final int W = 270;
    private static final Color NEN_SIDE = Color.WHITE;
    private static final Color VIEN_SIDE = new Color(226, 232, 240);       // Slate 200
    private static final Color NEN_HOVER = new Color(241, 245, 249);       // Slate 100
    private static final Color NEN_CHON = new Color(239, 246, 255);        // Blue 50
    private static final Color MAU_CHON = new Color(37, 99, 235);          // Blue 600
    private static final Color MAU_ICON_NHOM = new Color(2, 132, 199);     // Sky 600
    private static final Color CHU_TIEU_DE = new Color(15, 23, 42);        // Slate 900
    private static final Color CHU_MUC_CON = new Color(71, 85, 105);       // Slate 600
    private static final Color DUONG_KE = new Color(241, 245, 249);        // Slate 100

    public Main_UI(NhanVien nv, ArrayList<NhanVien> danhSachNhanVien) {
        this.nhanVienHienTai = nv;
        this.danhSachNhanVien = danhSachNhanVien;
        instance = this;

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
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UiHelper.NEN);

        add(taoSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Mở màn hình đầu tiên
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
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, VIEN_SIDE));

        // 1. Logo & Brand
        sidebar.add(taoLogo());

        // 2. Thẻ nhân viên (User Card)
        sidebar.add(taoUserCard());

        Component vSpace = Box.createVerticalStrut(8);
        ((JComponent) vSpace).setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(vSpace);

        // 3. Danh sách menu cuộn được
        sidebar.add(taoMenuCuon());

        // 4. Nút đăng xuất ở chân trang
        sidebar.add(taoNutDangXuat());

        return sidebar;
    }

    /** Header thương hiệu Gara */
    private JPanel taoLogo() {
        JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        box.setOpaque(false);
        box.setMaximumSize(new Dimension(W, 64));
        box.setPreferredSize(new Dimension(W, 64));
        box.setMinimumSize(new Dimension(W, 64));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(new EmptyBorder(14, 16, 8, 16));

        // Badge logo gradient
        JLabel badge = new JLabel("T4") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(37, 99, 235), getWidth(), getHeight(), new Color(14, 165, 233));
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

        JLabel brand = new JLabel("<html><b style='color:#0F172A;font-size:14px'>Gara Ô tô The T4</b>"
                + "<br><span style='color:#64748B;font-size:10px;font-weight:bold;letter-spacing:0.5px'>HỆ THỐNG QUẢN LÝ GARA</span></html>");
        brand.setOpaque(false);
        box.add(brand);

        return box;
    }

    /** Thẻ thông tin người dùng đang đăng nhập */
    private JPanel taoUserCard() {
        JPanel userWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        userWrap.setOpaque(false);
        userWrap.setMaximumSize(new Dimension(W, 58));
        userWrap.setPreferredSize(new Dimension(W, 58));
        userWrap.setMinimumSize(new Dimension(W, 58));
        userWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        userWrap.setBorder(new EmptyBorder(0, 14, 0, 14));

        JPanel user = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        user.setOpaque(false);
        user.setPreferredSize(new Dimension(W - 28, 48));
        user.setMaximumSize(new Dimension(W - 28, 48));
        user.setBorder(new EmptyBorder(6, 10, 6, 10));

        // Avatar tròn
        JPanel avatar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235));
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

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel lblTen = new JLabel(ten == null ? "" : ten);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTen.setForeground(CHU_TIEU_DE);
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVaiTro = new JLabel(nhanVienHienTai.getVaiTro() == null ? "" : nhanVienHienTai.getVaiTro());
        lblVaiTro.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVaiTro.setForeground(new Color(100, 116, 139));
        lblVaiTro.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(lblTen);
        info.add(Box.createVerticalStrut(2));
        info.add(lblVaiTro);
        user.add(info, BorderLayout.CENTER);

        userWrap.add(user);
        return userWrap;
    }

    /** Danh sách menu có cuộn mượt */
    private JScrollPane taoMenuCuon() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setOpaque(true);
        menuPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        tatCaNutMenu.clear();
        for (MucMenu muc : taoDanhSachMenu()) {
            if (muc.coQuyen(nhanVienHienTai.getVaiTro())) {
                themMucMenu(menuPanel, muc);
            }
        }

        JScrollPane sp = new JScrollPane(menuPanel);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(203, 213, 225);
                this.trackColor = new Color(0, 0, 0, 0);
            }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setOpaque(false);
                return b;
            }
        });
        return sp;
    }

    private void themMucMenu(JPanel menuPanel, MucMenu muc) {
        if (!muc.laNhom()) {
            // Mục độc lập (Trang chủ, Quản trị hệ thống)
            MenuRow nut = new MenuRow(muc.ten, muc.icon, 0, false);
            nut.addActionListener(e -> chonMenu(nut, muc.ten));
            menuPanel.add(nut);

            Component s = Box.createVerticalStrut(2);
            ((JComponent) s).setAlignmentX(Component.LEFT_ALIGNMENT);
            menuPanel.add(s);

            tatCaNutMenu.add(nut);
            if (nutDauTien == null) nutDauTien = nut;

            if ("Quản trị hệ thống (Admin)".equals(muc.ten)) {
                menuPanel.add(taoDuongKePhanNhom());
            }
            return;
        }

        // Nhóm cha (Header có icon và tên nhóm)
        MenuRow nutCha = new MenuRow(muc.ten, muc.icon, 0, true);
        nutCha.addActionListener(e -> {
            if (!muc.danhSachCon.isEmpty()) {
                chuyenTrang(muc.danhSachCon.get(0).ten);
            }
        });
        menuPanel.add(nutCha);

        // Các mục con thụt lề
        for (MucMenu con : muc.danhSachCon) {
            MenuRow nutCon = new MenuRow(con.ten, null, 1, false);
            nutCon.addActionListener(e -> chonMenu(nutCon, con.ten));
            menuPanel.add(nutCon);

            Component s = Box.createVerticalStrut(1);
            ((JComponent) s).setAlignmentX(Component.LEFT_ALIGNMENT);
            menuPanel.add(s);

            tatCaNutMenu.add(nutCon);
        }

        // Đường kẻ ngăn cách giữa các nhóm
        menuPanel.add(taoDuongKePhanNhom());
    }

    private Component taoDuongKePhanNhom() {
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(DUONG_KE);
                g.fillRect(8, 4, getWidth() - 16, 1);
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(W - 20, 9));
        line.setMaximumSize(new Dimension(W - 20, 9));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    /** Nút đăng xuất tinh tế ở cuối sidebar */
    private JPanel taoNutDangXuat() {
        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        box.setMaximumSize(new Dimension(W, 52));
        box.setPreferredSize(new Dimension(W, 52));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN_SIDE),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JButton btn = new JButton("ĐĂNG XUẤT", UiIcon.of(UiIcon.LOGOUT, 16, new Color(225, 29, 72)));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(225, 29, 72));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(254, 205, 211), 1, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(255, 241, 242));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        btn.addActionListener(e -> {
            int ch = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ch == JOptionPane.YES_OPTION) {
                dispose();
                new Login_UI(danhSachNhanVien).setVisible(true);
            }
        });

        box.add(btn, BorderLayout.CENTER);
        return box;
    }

    // ===============================================================
    //                       ĐIỀU HƯỚNG MÀN HÌNH
    // ===============================================================
    private void chonMenu(MenuRow nut, String tenMenu) {
        chonMenu(nut, tenMenu, null);
    }

    private void chonMenu(MenuRow nut, String tenMenu, Component trang) {
        try {
            if (nutDangChon != null) {
                nutDangChon.setChon(false);
            }

            nutDangChon = nut;

            if (nutDangChon != null) {
                nutDangChon.setChon(true);
            }

            contentPanel.removeAll();

            Component manHinh = trang != null
                    ? trang
                    : layTrangTheoTen(tenMenu);

            if (manHinh == null) {
                throw new IllegalStateException(
                        "Không tạo được giao diện: " + tenMenu);
            }

            contentPanel.add(manHinh, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();

        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể mở màn hình: " + tenMenu
                            + "\nLỗi: " + ex.getMessage(),
                    "Lỗi giao diện",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static Main_UI getInstance() {
        return instance;
    }

    public NhanVien getNhanVienHienTai() {
        return nhanVienHienTai;
    }

    public void chuyenTrangTiepNhan(Entity.PhuongTien xe) {
        chuyenTrang("Lập phiếu tiếp nhận", new LapPhieuTiepNhan_UI(nhanVienHienTai, xe));
    }

    public void chuyenTrang(String tenMenu) {
        chuyenTrang(tenMenu, null);
    }

    public void chuyenTrang(String tenMenu, Component trang) {
        if (tenMenu == null || tenMenu.isEmpty()) return;

        MenuRow found = null;
        for (MenuRow row : tatCaNutMenu) {
            if (row.text.equalsIgnoreCase(tenMenu) || row.text.contains(tenMenu) || tenMenu.contains(row.text)) {
                found = row;
                break;
            }
        }

        if (found != null) {
            chonMenu(found, found.text, trang);
        } else {
            if (nutDangChon != null) nutDangChon.setChon(false);
            nutDangChon = null;
            contentPanel.removeAll();
            contentPanel.add(trang != null ? trang : layTrangTheoTen(tenMenu), BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    private Component layTrangTheoTen(String tenMenu) {
        return switch (tenMenu) {
            case "Trang chủ"
                    -> new TrangChu_UI();
            case "Quản trị hệ thống (Admin)", "Quản trị hệ thống", "Admin Hub"
                    -> new AdminHub_UI(nhanVienHienTai, this);

            // 1. Khách hàng
            case "Khách hàng", "Quản lý khách hàng", "Danh sách khách hàng"
                    -> new QuanLyKhachHang_UI();
            case "Tra cứu khách hàng"
                    -> new TraCuuKhachHang_UI();

            // 2. Phương tiện
            case "Phương tiện", "Quản lý phương tiện", "Danh sách phương tiện"
                    -> new QuanLyPhuongTien_UI();
            case "Tra cứu phương tiện"
                    -> new TraCuuPhuongTien_UI();

            // 3. Phiếu tiếp nhận
            case "Phiếu tiếp nhận", "Lập phiếu tiếp nhận"
                    -> new LapPhieuTiepNhan_UI(nhanVienHienTai);
            case "Tra cứu phiếu tiếp nhận"
                    -> new TraCuuPhieuTiepNhan_UI();

            // 4. Sửa chữa
            case "Phiếu sửa chữa", "Sửa chữa", "Lập phiếu sửa chữa"
                    -> new LapPhieuSuaChua_UI(nhanVienHienTai);
            case "Phân công KTV"
                    -> new PhanCongKyThuatVien_UI();
            case "Tra cứu phiếu sửa chữa", "Cập nhật tiến độ"
                    -> new CapNhatTienDo_UI();

            // 5. Hóa đơn
            case "Hóa đơn", "Lập hóa đơn", "Thanh toán hóa đơn"
                    -> new ThanhToanHoaDon_UI(nhanVienHienTai);
            case "Tra cứu hóa đơn"
                    -> new TraCuuHoaDon_UI(nhanVienHienTai);

            // 6. Kho phụ tùng
            case "Kho phụ tùng", "Quản lý phụ tùng", "Danh sách phụ tùng"
                    -> new DanhSachPhuTung_UI();
            case "Nhập kho"
                    -> new NhapKho_UI(nhanVienHienTai);
            case "Xuất kho"
                    -> new XuatKho_UI(nhanVienHienTai);
            case "Báo cáo tồn kho"
                    -> new BaoCaoTonKho_UI();
            case "Lịch sử nhập xuất kho", "Theo dõi lịch sử nhập xuất", "Lịch sử nhập xuất"
                    -> new LichSuNhapXuat_UI(nhanVienHienTai);
            case "Theo dõi chờ nhập"
                    -> new TheoDoiChoNhap_UI();

            // 7. Bảo hành
            case "Bảo hành", "Lập phiếu bảo hành"
                    -> new LapPhieuBaoHanh_UI(nhanVienHienTai);
            case "Tra cứu phiếu bảo hành", "Tra cứu bảo hành", "Quản lý bảo hành"
                    -> new TraCuuBaoHanh_UI();

            // 8. Nhân viên
            case "Nhân viên", "Quản lý nhân viên"
                    -> new QuanLyNhanVien_UI();
            case "Tra cứu nhân viên", "Phân quyền", "Phân quyền người dùng"
                    -> new PhanQuyen_UI();

            // 9. Báo cáo – Thống kê
            case "Báo cáo – Thống kê", "Báo cáo thống kê", "Thống kê doanh thu", "Báo cáo doanh thu", "Theo tháng", "Theo năm"
                    -> new BaoCaoDoanhThu_UI();
            case "Xu hướng doanh thu (Đường)", "Biểu đồ đường xu hướng"
                    -> {
                        BaoCaoDoanhThu_UI ui = new BaoCaoDoanhThu_UI();
                        ui.datCheDoBieuDo(1);
                        yield ui;
                    }

            default -> {
                JLabel lbl = new JLabel("Chức năng đang mở: " + tenMenu, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lbl.setForeground(UiHelper.XANH_DAM);
                yield lbl;
            }
        };
    }

    // ===============================================================
    //                       DỮ LIỆU DANH SÁCH MENU
    // ===============================================================
    private static class MucMenu {
        final String ten;
        final String icon;
        final String vaiTro;
        final ArrayList<MucMenu> danhSachCon = new ArrayList<>();

        MucMenu(String ten, String icon, String vaiTro) {
            this.ten = ten;
            this.icon = icon;
            this.vaiTro = vaiTro;
        }

        MucMenu them(MucMenu con) {
            danhSachCon.add(con);
            return this;
        }

        boolean laNhom() {
            return !danhSachCon.isEmpty();
        }

        boolean coQuyen(String vaiTroNV) {
            if (vaiTro == null) return true;
            if (vaiTroNV == null || vaiTroNV.trim().isEmpty()) return false;

            String vt = vaiTroNV.trim().toLowerCase();

            // 1. Quản lý / Admin / Điều hành có TOÀN QUYỀN tất cả các chức năng
            if (vt.contains("quản lý")
                    || vt.contains("quản trị")
                    || vt.contains("admin")
                    || vt.contains("điều hành")
                    || vt.contains("giám đốc")
                    || vt.contains("chủ xưởng")) {
                return true;
            }

            // 2. Kiểm tra khớp theo danh sách quyền của từng mục menu
            String[] vts = vaiTro.split(",");
            for (String v : vts) {
                String req = v.trim().toLowerCase();

                // Khớp chính xác hoặc chứa nhau
                if (vt.equalsIgnoreCase(req) || vt.contains(req) || req.contains(vt)) {
                    return true;
                }

                // Nhóm Tiếp tân / Cố vấn dịch vụ / Khách hàng
                if (req.contains("tiếp tân") && (vt.contains("tiếp tân") || vt.contains("cố vấn") || vt.contains("lễ tân") || vt.contains("tiếp nhận") || vt.contains("dịch vụ"))) {
                    return true;
                }

                // Nhóm Kỹ thuật viên / Sửa chữa
                if (req.contains("sửa chữa") && (vt.contains("sửa chữa") || vt.contains("ktv") || vt.contains("kỹ thuật") || vt.contains("máy") || vt.contains("gầm") || vt.contains("điện") || vt.contains("sơn") || vt.contains("bảo dưỡng"))) {
                    return true;
                }

                // Nhóm Kho / Vật tư / Phụ tùng
                if (req.contains("kho") && (vt.contains("kho") || vt.contains("vật tư") || vt.contains("phụ tùng") || vt.contains("thủ kho"))) {
                    return true;
                }

                // Nhóm Thu ngân / Kế toán
                if (req.contains("thu ngân") && (vt.contains("thu ngân") || vt.contains("kế toán") || vt.contains("thanh toán") || vt.contains("tài chính"))) {
                    return true;
                }
            }
            return false;
        }

        static MucMenu don(String ten, String icon, String vaiTro) {
            return new MucMenu(ten, icon, vaiTro);
        }

        static MucMenu nhom(String ten, String icon, String vaiTro) {
            return new MucMenu(ten, icon, vaiTro);
        }

        static MucMenu con(String ten) {
            return new MucMenu(ten, null, null);
        }
    }

    private ArrayList<MucMenu> taoDanhSachMenu() {
        ArrayList<MucMenu> ds = new ArrayList<>();

        // Mục độc lập
        ds.add(MucMenu.don("Trang chủ", UiIcon.HOME, null));
        ds.add(MucMenu.don("Quản trị hệ thống (Admin)", UiIcon.SETTINGS, QUAN_LY));

        // 1. Khách hàng
        ds.add(MucMenu.nhom("Khách hàng", UiIcon.USERS, TIEP_TAN + "," + THU_NGAN)
                .them(MucMenu.con("Quản lý khách hàng"))
                .them(MucMenu.con("Tra cứu khách hàng")));

        // 2. Phương tiện
        ds.add(MucMenu.nhom("Phương tiện", UiIcon.CAR, TIEP_TAN + "," + THU_NGAN)
                .them(MucMenu.con("Quản lý phương tiện"))
                .them(MucMenu.con("Tra cứu phương tiện")));

        // 3. Phiếu tiếp nhận
        ds.add(MucMenu.nhom("Phiếu tiếp nhận", UiIcon.FILE, TIEP_TAN + "," + THU_NGAN + "," + SUA_CHUA)
                .them(MucMenu.con("Lập phiếu tiếp nhận"))
                .them(MucMenu.con("Tra cứu phiếu tiếp nhận")));

        // 4. Sửa chữa
        ds.add(MucMenu.nhom("Phiếu sửa chữa", UiIcon.WRENCH, SUA_CHUA)
                .them(MucMenu.con("Lập phiếu sửa chữa"))
                .them(MucMenu.con("Tra cứu phiếu sửa chữa")));

        // 5. Hóa đơn
        ds.add(MucMenu.nhom("Hóa đơn", UiIcon.BILL, TIEP_TAN + "," + THU_NGAN)
                .them(MucMenu.con("Lập hóa đơn"))
                .them(MucMenu.con("Tra cứu hóa đơn")));

        // 6. Kho phụ tùng
        ds.add(MucMenu.nhom("Kho phụ tùng", UiIcon.BOX, KHO)
                .them(MucMenu.con("Quản lý phụ tùng"))
                .them(MucMenu.con("Nhập kho"))
                .them(MucMenu.con("Xuất kho"))
                .them(MucMenu.con("Lịch sử nhập xuất kho"))
                .them(MucMenu.con("Báo cáo tồn kho")));

        // 7. Bảo hành
        ds.add(MucMenu.nhom("Bảo hành", UiIcon.SHIELD, TIEP_TAN)
                .them(MucMenu.con("Lập phiếu bảo hành"))
                .them(MucMenu.con("Tra cứu phiếu bảo hành")));

        // 8. Nhân viên
        ds.add(MucMenu.nhom("Nhân viên", UiIcon.USERS, QUAN_LY)
                .them(MucMenu.con("Quản lý nhân viên"))
                .them(MucMenu.con("Tra cứu nhân viên")));

        // 9. Báo cáo – Thống kê
        ds.add(MucMenu.nhom("Báo cáo – Thống kê", UiIcon.CHART, QUAN_LY + "," + THU_NGAN)
                .them(MucMenu.con("Thống kê doanh thu"))
                .them(MucMenu.con("Xu hướng doanh thu (Đường)")));

        return ds;
    }

    // ===============================================================
    //                       DÒNG NÚT MENU (MenuRow)
    // ===============================================================
    private class MenuRow extends JPanel {
        private final String text;
        private final String iconType;
        private final int capDo;
        private final boolean laNhomHeader;
        private boolean dangChon = false;
        private boolean dangHover = false;
        private final java.util.List<java.awt.event.ActionListener> listeners = new java.util.ArrayList<>();

        MenuRow(String text, String iconType, int capDo, boolean laNhomHeader) {
            this.text = text;
            this.iconType = iconType;
            this.capDo = capDo;
            this.laNhomHeader = laNhomHeader;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            int h = laNhomHeader ? 34 : (capDo == 0 ? 38 : 30);
            setPreferredSize(new Dimension(W - 16, h));
            setMaximumSize(new Dimension(W - 16, h));
            setMinimumSize(new Dimension(W - 16, h));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    dangHover = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    dangHover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    for (java.awt.event.ActionListener al : listeners) {
                        al.actionPerformed(new java.awt.event.ActionEvent(MenuRow.this, 0, text));
                    }
                }
            });
        }

        void addActionListener(java.awt.event.ActionListener al) {
            listeners.add(al);
        }

        void setChon(boolean b) {
            this.dangChon = b;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (dangChon) {
                g2.setColor(NEN_CHON);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);

                // Dải vạch chỉ thị Accent Blue
                g2.setColor(MAU_CHON);
                g2.fillRoundRect(2, 6, 3, h - 12, 2, 2);
            } else if (dangHover) {
                g2.setColor(NEN_HOVER);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 8, 8);
            }

            int x = 12;

            // Icon nếu có
            if (iconType != null) {
                Color icoColor = dangChon ? MAU_CHON : MAU_ICON_NHOM;
                UiIcon ico = UiIcon.of(iconType, 16, icoColor);
                ico.paintIcon(this, g2, x, (h - 16) / 2);
                x += 24;
            } else if (capDo == 1) {
                x += 24; // Thụt lề mục con
            }

            // Tiêu đề
            if (laNhomHeader) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(CHU_TIEU_DE);
            } else if (dangChon) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(MAU_CHON);
            } else {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(CHU_MUC_CON);
            }

            FontMetrics fm = g2.getFontMetrics();
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }
}
