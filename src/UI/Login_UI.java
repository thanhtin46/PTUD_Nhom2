package UI;

import Entity.NhanVien;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

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
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, 1100, 640, 24, 24));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(20, 50, 100));

        JPanel trai = taoPanelTrai();
        JPanel phai = taoPanelPhai();

        root.add(trai, BorderLayout.WEST);
        root.add(phai, BorderLayout.CENTER);
        setContentPane(root);

        // kéo cửa sổ vì undecorated
        addMouseDrag();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void addMouseDrag() {
        final Point[] drag = {null};
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) { drag[0] = e.getPoint(); }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
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
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(18, 40, 58), 0, h, new Color(30, 91, 112));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                // ánh sáng mềm phía trên
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(-100, -100, w + 200, h / 2);
                g2.dispose();
            }
        };
        trai.setPreferredSize(new Dimension(580, 0));
        trai.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel tde = new JLabel("HỆ THỐNG QUẢN LÝ");
        tde.setForeground(new Color(255, 255, 255, 220));
        tde.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel tde2 = new JLabel("GARA Ô TÔ THE T4");
        tde2.setForeground(Color.WHITE);
        tde2.setFont(new Font("Segoe UI", Font.BOLD, 36));

        JLabel phuDe = new JLabel("<html>Quản lý khách hàng, phương tiện, phụ tùng,<br>phiếu sửa chữa & bảo hành trong cùng<br>một không gian làm việc.</html>");
        phuDe.setForeground(new Color(220, 235, 255));
        phuDe.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        // Ảnh gara đẹp
        ImageIcon anh = HinhAnh.icon("login_gara.png", 500, 360);
        if (anh == null) anh = HinhAnh.icon("login_gara.jpg", 500, 360);
        if (anh == null) {
            anh = new ImageIcon(HinhAnh.anhGaraMacDinh(900, 640));
            anh = new ImageIcon(anh.getImage().getScaledInstance(500, 360, Image.SCALE_SMOOTH));
        }
        JLabel lblAnh = new JLabel(anh);
        lblAnh.setHorizontalAlignment(SwingConstants.CENTER);
        lblAnh.setBorder(new javax.swing.border.LineBorder(new Color(255, 255, 255, 60), 2, true));

        JPanel tdeBox = new JPanel();
        tdeBox.setOpaque(false);
        tdeBox.setLayout(new BoxLayout(tdeBox, BoxLayout.Y_AXIS));
        tde2.setAlignmentX(Component.LEFT_ALIGNMENT);
        phuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        tdeBox.add(tde);
        tdeBox.add(Box.createVerticalStrut(2));
        tdeBox.add(tde2);
        tdeBox.add(Box.createVerticalStrut(14));
        tdeBox.add(phuDe);
        tdeBox.add(Box.createVerticalStrut(20));
        tdeBox.add(lblAnh);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        JLabel ft = new JLabel("© 2026 Gara Ô tô The T4 — Phiên bản 2.0");
        ft.setForeground(new Color(200, 220, 245));
        ft.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(ft);

        trai.add(tdeBox, BorderLayout.CENTER);
        trai.add(footer, BorderLayout.SOUTH);
        return trai;
    }

    private JPanel taoPanelPhai() {
        JPanel phai = new JPanel(new GridBagLayout());
        phai.setBackground(Color.WHITE);
        phai.setBorder(new EmptyBorder(20, 40, 20, 20));

        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setMaximumSize(new Dimension(420, 600));

        JLabel dong = new JLabel("Xin chào!");
        dong.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        dong.setForeground(new Color(110, 120, 135));
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tde = new JLabel("Đăng nhập hệ thống");
        tde.setFont(new Font("Segoe UI", Font.BOLD, 30));
        tde.setForeground(UiHelper.XANH_DAM);
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Vui lòng nhập tài khoản để tiếp tục");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(110, 120, 135));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(dong);
        center.add(tde);
        center.add(Box.createVerticalStrut(6));
        center.add(sub);
        center.add(Box.createVerticalStrut(28));

        JLabel lblTk = new JLabel("Tài khoản");
        lblTk.setFont(UiHelper.FONT_NHAN);
        lblTk.setForeground(new Color(60, 70, 85));
        lblTk.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtTaiKhoan = new JTextField();
        UiHelper.trangTriInput(txtTaiKhoan);
        txtTaiKhoan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtTaiKhoan.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(lblTk);
        center.add(Box.createVerticalStrut(6));
        center.add(txtTaiKhoan);
        center.add(Box.createVerticalStrut(14));

        JLabel lblMk = new JLabel("Mật khẩu");
        lblMk.setFont(UiHelper.FONT_NHAN);
        lblMk.setForeground(new Color(60, 70, 85));
        lblMk.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtMatKhau = new JPasswordField();
        UiHelper.trangTriInput(txtMatKhau);
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(lblMk);
        center.add(Box.createVerticalStrut(6));
        center.add(txtMatKhau);
        center.add(Box.createVerticalStrut(26));

        JButton btnDangNhap = UiHelper.nut("ĐĂNG NHẬP", UiHelper.XANH_TRUNG, true, e -> xuLyDangNhap());
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(btnDangNhap);
        center.add(Box.createVerticalStrut(10));

        JButton btnThoat = UiHelper.nut("THOÁT", new Color(140, 150, 165), true, e -> System.exit(0));
        btnThoat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnThoat.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(btnThoat);
        center.add(Box.createVerticalStrut(20));

        JLabel goiy = new JLabel("<html><b style='color:#1B3A6E'>Tài khoản mẫu:</b> "
                + "<span style='color:#1B3A6E'>tiepTan</span> / "
                + "<span style='color:#1B3A6E'>suaChua</span> / "
                + "<span style='color:#1B3A6E'>kho</span> / "
                + "<span style='color:#1B3A6E'>quanLy</span> "
                + "(mật khẩu <b>123</b>)</html>");
        goiy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        goiy.setForeground(new Color(110, 120, 135));
        goiy.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(goiy);
        center.add(Box.createVerticalGlue());

        JPanel help = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        help.setOpaque(false);
        JLabel ft = new JLabel("Hỗ trợ kỹ thuật: 0901 234 567");
        ft.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ft.setForeground(new Color(160, 170, 185));
        help.add(ft);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(help);

        phai.add(center, new GridBagConstraints());

        getRootPane().setDefaultButton(btnDangNhap);
        return phai;
    }

    private void xuLyDangNhap() {
        String tk = txtTaiKhoan.getText().trim();
        String mk = new String(txtMatKhau.getPassword()).trim();

        NhanVien nv = null;
        for (NhanVien n : danhSachNhanVien) {
            if (!n.getTaiKhoan().equals(tk)) continue;
            if (!n.getMatKhau().equals(mk)) continue;
            if (!n.dangHoatDong()) {
                JOptionPane.showMessageDialog(this,
                        "Tài khoản đã bị vô hiệu hóa. Liên hệ quản lý!",
                        "Không thể đăng nhập",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            nv = n;
            break;
        }

        if (nv != null) {
            dispose();
            new Main_UI(nv, danhSachNhanVien).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Sai tài khoản hoặc mật khẩu!",
                    "Lỗi đăng nhập",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
