package UI;

import Entity.KhoDuLieu;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TrangChu_UI extends JPanel {
    public TrangChu_UI() {
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        add(root, BorderLayout.CENTER);

        JLabel tde = UiHelper.tieuDeTrang("Trang chủ — Gara Ô tô The T4");
        JLabel phu = new JLabel("Tổng quan hệ thống");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(110, 120, 135));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(phu);
        north.add(Box.createVerticalStrut(8));
        root.add(north, BorderLayout.NORTH);

        KhoDuLieu kho = KhoDuLieu.get();
        int tongTon = 0;
        for (PhuTung pt : kho.getPhuTungs()) tongTon += pt.getSoLuongTon();

        JPanel grid = new JPanel(new GridLayout(2, 3, 18, 18));
        grid.setOpaque(false);
        grid.add(theThongKe("👥", "Khách hàng", String.valueOf(kho.getKhachHangs().size()),
                "Tổng số khách đã đăng ký", new Color(46, 132, 230)));
        grid.add(theThongKe("🚗", "Phương tiện", String.valueOf(kho.getPhuongTiens().size()),
                "Đang được quản lý", new Color(225, 130, 50)));
        grid.add(theThongKe("🔧", "Phiếu sửa chữa", String.valueOf(kho.getPhieuTiepNhans().size()),
                "Đang tiếp nhận & xử lý", new Color(60, 170, 100)));
        grid.add(theThongKe("🛡", "Phiếu bảo hành", String.valueOf(kho.getPhieuBaoHanhs().size()),
                "Đang còn hạn & hết hạn", new Color(140, 95, 200)));
        grid.add(theThongKe("📦", "Tồn phụ tùng", String.valueOf(tongTon),
                "Qua " + kho.getPhuTungs().size() + " mặt hàng", new Color(60, 160, 200)));
        grid.add(theThongKe("👨‍💼", "Nhân viên", String.valueOf(kho.getNhanViens().size()),
                "Đang hoạt động", new Color(220, 80, 110)));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(18, 0, 0, 0));
        wrap.add(grid, BorderLayout.CENTER);
        root.add(wrap, BorderLayout.CENTER);

        // Lời chào
        JPanel chao = new JPanel(new BorderLayout());
        chao.setBackground(UiHelper.NEN_CARD);
        chao.setBorder(BorderFactory.createCompoundBorder(
                new UiHelper.ShadowBorder(8, new Color(0, 0, 0, 20)),
                new EmptyBorder(18, 22, 18, 22)));
        JLabel c1 = new JLabel("👋 Chào mừng bạn đến với Gara Ô tô The T4");
        c1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        c1.setForeground(UiHelper.XANH_DAM);
        JLabel c2 = new JLabel("Chọn chức năng từ menu bên trái để bắt đầu.");
        c2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c2.setForeground(new Color(100, 110, 125));
        JPanel ct = new JPanel();
        ct.setOpaque(false);
        ct.setLayout(new BoxLayout(ct, BoxLayout.Y_AXIS));
        c1.setAlignmentX(Component.LEFT_ALIGNMENT);
        c2.setAlignmentX(Component.LEFT_ALIGNMENT);
        ct.add(c1);
        ct.add(Box.createVerticalStrut(4));
        ct.add(c2);
        chao.add(ct, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(18, 0, 0, 0));
        south.add(chao, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
    }

    private JPanel theThongKe(String icon, String ten, String so, String phuDe, Color mau) {
        JPanel p = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(248, 250, 253));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new UiHelper.ShadowBorder(6, new Color(0, 0, 0, 18)),
                new EmptyBorder(18, 22, 18, 22)));

        JLabel lblIcon = new JLabel(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UiHelper.lighten(mau, 0.15f), 0, getHeight(), mau));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(56, 56));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTen.setForeground(new Color(100, 110, 125));
        JLabel lblSo = new JLabel(so);
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblSo.setForeground(mau);
        JLabel lblPhu = new JLabel(phuDe);
        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
