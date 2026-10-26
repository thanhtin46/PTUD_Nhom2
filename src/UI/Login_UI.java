package UI;

import Entity.NhanVien;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

/**
 * Login_UI — Màn hình Đăng nhập hiện đại chuẩn Figma:
 * - Khung hình không viền bo cong góc mềm mại 24px, hỗ trợ kéo rê
 * - Cột trái: Nền gradient Slate/Blue chiều sâu công nghệ, hình minh họa xe và giới thiệu năng động
 * - Cột phải: Form đăng nhập nền trắng tinh tế, input focus viền xanh, nút bấm nổi bật
 */
public class Login_UI extends JFrame {
    private JTextField txtTaiKhoan;
    private JPasswordField txtMatKhau;
    private ArrayList<NhanVien> danhSachNhanVien;

    public Login_UI(ArrayList<NhanVien> danhSachNhanVien) {
        this.danhSachNhanVien = danhSachNhanVien;
        initComponents();
    }

    private void initComponents() {
        setTitle("Hệ thống quản lý Gara ô tô — Đăng nhập");
        setUndecorated(true);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 1100, 650, 24, 24));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel trai = taoPanelTrai();
        JPanel phai = taoPanelPhai();

        root.add(trai, BorderLayout.WEST);
        root.add(phai, BorderLayout.CENTER);
        setContentPane(root);

        // Kéo thả cửa sổ
        addMouseDrag();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void addMouseDrag() {
        final Point[] drag = {null};
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { drag[0] = e.getPoint(); }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (drag[0] != null) {
                    Point p = getLocation();
                    setLocation(p.x + e.getX() - drag[0].x, p.y + e.getY() - drag[0].y);
                }
            }
        });
    }

    private JPanel taoPanelTrai() {
        JPanel trai = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Gradient Slate 900 -> Blue 900
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), 0, h, new Color(30, 58, 138));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Các khối vòng tròn trang trí khuếch tán ánh sáng
                g2.setColor(new Color(59, 130, 246, 25));
                g2.fillOval(-80, -80, 360, 360);
                g2.setColor(new Color(147, 51, 234, 20));
                g2.fillOval(w - 240, h - 260, 340, 340);
                g2.dispose();
            }
        };
        trai.setPreferredSize(new Dimension(560, 0));
        trai.setBorder(new EmptyBorder(40, 48, 36, 48));

        JLabel badge = new JLabel("GARAGE MANAGEMENT SYSTEM");
        badge.setForeground(new Color(96, 165, 250)); // Blue 400
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel tde = new JLabel("GARA Ô TÔ THE T4");
        tde.setForeground(Color.WHITE);
        tde.setFont(new Font("Segoe UI", Font.BOLD, 36));

        JLabel phuDe = new JLabel("<html>Giải pháp toàn diện quản lý khách hàng, tiếp nhận xe,<br>tiến độ sửa chữa, phụ tùng và bảo hành chuyên nghiệp.</html>");
        phuDe.setForeground(new Color(203, 213, 225));
        phuDe.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        // Hình ảnh Gara chuyên nghiệp độ nét cao (Thợ sửa xe + Cầu nâng ô tô đỏ)
        JPanel panelAnh = new JPanel() {
            private Image img = null;
            {
                try {
                    img = HinhAnh.doc("login_gara.jpg");
                    if (img == null) img = HinhAnh.doc("login_gara.png");
                } catch (Exception ignored) {}
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();

                // Tạo khung bo góc 18px với clip
                RoundRectangle2D.Float clipShape = new RoundRectangle2D.Float(0, 0, w, h, 20, 20);
                g2.setClip(clipShape);

                if (img != null) {
                    int iw = img.getWidth(null);
                    int ih = img.getHeight(null);
                    if (iw > 0 && ih > 0) {
                        double scale = Math.max((double) w / iw, (double) h / ih);
                        int dw = (int) Math.round(iw * scale);
                        int dh = (int) Math.round(ih * scale);
                        int dx = (w - dw) / 2;
                        int dy = (h - dh) / 2;
                        g2.drawImage(img, dx, dy, dw, dh, null);
                    }
                } else {
                    g2.setColor(new Color(30, 41, 59));
                    g2.fillRect(0, 0, w, h);
                }

                g2.setClip(null);

                // Đường viền thủy tinh tinh tế quanh ảnh
                g2.setColor(new Color(255, 255, 255, 50));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
                g2.dispose();
            }
        };
        panelAnh.setOpaque(false);
        panelAnh.setPreferredSize(new Dimension(464, 320));
        panelAnh.setMaximumSize(new Dimension(464, 320));
        panelAnh.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tdeBox = new JPanel();
        tdeBox.setOpaque(false);
        tdeBox.setLayout(new BoxLayout(tdeBox, BoxLayout.Y_AXIS));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        tdeBox.add(badge);
        tdeBox.add(Box.createVerticalStrut(6));
        tdeBox.add(tde);
        tdeBox.add(Box.createVerticalStrut(12));
        tdeBox.add(phuDe);
        tdeBox.add(Box.createVerticalStrut(20));
        tdeBox.add(panelAnh);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        JLabel ft = new JLabel("© 2026 Gara Ô tô The T4 — Chuẩn hóa trải nghiệm dịch vụ");
        ft.setForeground(new Color(148, 163, 184));
        ft.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(ft);

        trai.add(tdeBox, BorderLayout.CENTER);
        trai.add(footer, BorderLayout.SOUTH);
        return trai;
    }

    private JPanel taoPanelPhai() {
        JPanel phai = new JPanel(new GridBagLayout());
        phai.setBackground(Color.WHITE);
        phai.setBorder(new EmptyBorder(30, 48, 30, 48));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setPreferredSize(new Dimension(380, 560));
        center.setMaximumSize(new Dimension(380, 560));

        JLabel dong = new JLabel("Chào mừng trở lại");
        dong.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        dong.setForeground(new Color(100, 116, 139));
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tde = new JLabel("Đăng nhập hệ thống");
        tde.setFont(new Font("Segoe UI", Font.BOLD, 28));
        tde.setForeground(UiHelper.XANH_DAM);
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Vui lòng nhập tài khoản và mật khẩu để tiếp tục");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(dong);
        center.add(Box.createVerticalStrut(4));
        center.add(tde);
        center.add(Box.createVerticalStrut(6));
        center.add(sub);
        center.add(Box.createVerticalStrut(28));

        JLabel lblTk = new JLabel("Tài khoản");
        lblTk.setFont(UiHelper.FONT_NHAN);
        lblTk.setForeground(new Color(51, 65, 85));
        lblTk.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtTaiKhoan = new JTextField();
        UiHelper.trangTriInput(txtTaiKhoan);
        txtTaiKhoan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtTaiKhoan.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(lblTk);
        center.add(Box.createVerticalStrut(6));
        center.add(txtTaiKhoan);
        center.add(Box.createVerticalStrut(16));

        JLabel lblMk = new JLabel("Mật khẩu");
        lblMk.setFont(UiHelper.FONT_NHAN);
        lblMk.setForeground(new Color(51, 65, 85));
        lblMk.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtMatKhau = new JPasswordField();
        UiHelper.trangTriInput(txtMatKhau);
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(lblMk);
        center.add(Box.createVerticalStrut(6));
        center.add(txtMatKhau);
        center.add(Box.createVerticalStrut(26));

        JButton btnDangNhap = UiHelper.nut("ĐĂNG NHẬP", UiIcon.of(UiIcon.CHECK, 16), UiHelper.XANH_TRUNG, true, e -> xuLyDangNhap());
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(btnDangNhap);
        center.add(Box.createVerticalStrut(12));

        JButton btnThoat = UiHelper.nut("THOÁT", UiIcon.of(UiIcon.CLOSE, 16), new Color(148, 163, 184), true, e -> System.exit(0));
        btnThoat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnThoat.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(btnThoat);
        center.add(Box.createVerticalStrut(20));

        // Box tài khoản gợi ý (có thể bấm chọn nhanh)
        JPanel boxGoiY = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        boxGoiY.setOpaque(false);
        boxGoiY.setBorder(new EmptyBorder(10, 14, 10, 14));
        boxGoiY.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        boxGoiY.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTdeGoiY = new JLabel("Tài khoản hệ thống (bấm để chọn nhanh):");
        lblTdeGoiY.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTdeGoiY.setForeground(new Color(51, 65, 85));

        JPanel chipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chipPanel.setOpaque(false);
        String[] accs = {"tiepTan", "suaChua", "kho", "thuNgan", "quanLy"};
        for (String acc : accs) {
            JButton chip = new JButton(acc);
            chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
            chip.setForeground(new Color(37, 99, 235));
            chip.setBackground(new Color(239, 246, 255));
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                    new EmptyBorder(3, 8, 3, 8)
            ));
            chip.setFocusPainted(false);
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chip.addActionListener(e -> {
                txtTaiKhoan.setText(acc);
                txtMatKhau.setText("123");
            });
            chipPanel.add(chip);
        }

        JLabel lblPassDefault = new JLabel("Mật khẩu mặc định: 123");
        lblPassDefault.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPassDefault.setForeground(new Color(100, 116, 139));

        JPanel boxGoiYInner = new JPanel();
        boxGoiYInner.setOpaque(false);
        boxGoiYInner.setLayout(new BoxLayout(boxGoiYInner, BoxLayout.Y_AXIS));
        lblTdeGoiY.setAlignmentX(Component.LEFT_ALIGNMENT);
        chipPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPassDefault.setAlignmentX(Component.LEFT_ALIGNMENT);
        boxGoiYInner.add(lblTdeGoiY);
        boxGoiYInner.add(Box.createVerticalStrut(4));
        boxGoiYInner.add(chipPanel);
        boxGoiYInner.add(Box.createVerticalStrut(4));
        boxGoiYInner.add(lblPassDefault);

        boxGoiY.add(boxGoiYInner, BorderLayout.CENTER);
        center.add(boxGoiY);

        center.add(Box.createVerticalGlue());

        JLabel ft = new JLabel("Hỗ trợ kỹ thuật: 0901 234 567");
        ft.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ft.setForeground(new Color(148, 163, 184));
        ft.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(ft);

        phai.add(center, new GridBagConstraints());
        getRootPane().setDefaultButton(btnDangNhap);
        return phai;
    }

    private void xuLyDangNhap() {
        String tk = txtTaiKhoan.getText().trim();
        String mk = new String(txtMatKhau.getPassword()).trim();

        if (tk.isEmpty() || mk.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ tên tài khoản và mật khẩu!",
                    "Chưa nhập thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Kiểm tra xác thực trực tiếp qua CSDL SQL Server bằng ConnectDB
        DAO.NhanVien_DAO nvDao = new DAO.NhanVien_DAO();
        NhanVien nv = nvDao.kiemTraDangNhap(tk, mk);

        // 2. Dự phòng nếu danh sách trong bộ nhớ có tài khoản
        if (nv == null && danhSachNhanVien != null) {
            for (NhanVien n : danhSachNhanVien) {
                if (n.getTaiKhoan() != null && n.getTaiKhoan().equals(tk) && n.getMatKhau() != null && n.getMatKhau().equals(mk)) {
                    nv = n;
                    break;
                }
            }
        }

        if (nv != null) {
            boolean laTaiKhoanHeThong = "quanLy".equalsIgnoreCase(tk) || "tiepTan".equalsIgnoreCase(tk)
                    || "suaChua".equalsIgnoreCase(tk) || "kho".equalsIgnoreCase(tk) || "thuNgan".equalsIgnoreCase(tk)
                    || (nv.getVaiTro() != null && nv.getVaiTro().toLowerCase().contains("quản lý"));
            if (!laTaiKhoanHeThong && !nv.dangHoatDong()) {
                JOptionPane.showMessageDialog(this,
                        "Tài khoản này đã bị vô hiệu hóa. Vui lòng liên hệ quản lý!",
                        "Không thể đăng nhập",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dispose();
            new Main_UI(nv, danhSachNhanVien).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Tài khoản hoặc mật khẩu không chính xác!",
                    "Lỗi đăng nhập",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
