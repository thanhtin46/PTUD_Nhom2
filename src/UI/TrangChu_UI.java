package UI;

import Entity.KhoDuLieu;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * TrangChu_UI — Màn hình Dashboard tổng quan thiết kế theo phong cách Figma:
 * - Header tiêu đề và mô tả thoáng đãng
 * - Hệ thống 6 thẻ thống kê chỉ số chính (KPI cards) bo góc 18px với biểu tượng có nền mờ pastel hiện đại
 * - Welcome Card sang trọng có thanh viền Gradient bên trái và trạng thái xanh real-time
 */
public class TrangChu_UI extends JPanel {

    public TrangChu_UI() {
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        add(root, BorderLayout.CENTER);

        // Header
        JLabel tde = UiHelper.tieuDeTrang("Bảng điều khiển");
        JLabel phu = new JLabel("Tổng quan hoạt động & tình trạng vận hành của Gara Ô tô The T4");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(tde);
        header.add(Box.createVerticalStrut(4));
        header.add(phu);
        root.add(header, BorderLayout.NORTH);

        // Lấy số liệu
        KhoDuLieu kho = KhoDuLieu.get();
        int tongTon = 0;
        for (PhuTung pt : kho.getPhuTungs()) tongTon += pt.getSoLuongTon();

        // Lưới thống kê
        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setOpaque(false);

        grid.add(theThongKe(UiIcon.of(UiIcon.USERS, 24, new Color(37, 99, 235)), "Khách hàng", String.valueOf(kho.getKhachHangs().size()),
                "Tổng số khách đã đăng ký", new Color(37, 99, 235))); // Blue

        grid.add(theThongKe(UiIcon.of(UiIcon.CAR, 24, new Color(245, 158, 11)), "Phương tiện", String.valueOf(kho.getPhuongTiens().size()),
                "Đang được quản lý", new Color(245, 158, 11))); // Amber

        grid.add(theThongKe(UiIcon.of(UiIcon.WRENCH, 24, new Color(22, 163, 74)), "Phiếu sửa chữa", String.valueOf(kho.getPhieuTiepNhans().size()),
                "Tiếp nhận và xử lý", new Color(22, 163, 74))); // Green

        grid.add(theThongKe(UiIcon.of(UiIcon.SHIELD, 24, new Color(124, 58, 237)), "Phiếu bảo hành", String.valueOf(kho.getPhieuBaoHanhs().size()),
                "Còn hạn & bảo dưỡng định kỳ", new Color(124, 58, 237))); // Purple

        grid.add(theThongKe(UiIcon.of(UiIcon.BOX, 24, new Color(14, 165, 233)), "Tồn kho phụ tùng", String.valueOf(tongTon),
                kho.getPhuTungs().size() + " mặt hàng trong kho", new Color(14, 165, 233))); // Sky Blue

        grid.add(theThongKe(UiIcon.of(UiIcon.USER, 24, new Color(239, 68, 68)), "Nhân viên", String.valueOf(kho.getNhanViens().size()),
                "Tài khoản đang hoạt động", new Color(239, 68, 68))); // Red

        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);
        gridWrap.setBorder(new EmptyBorder(12, 0, 16, 0));
        gridWrap.add(grid, BorderLayout.CENTER);
        root.add(gridWrap, BorderLayout.CENTER);

        // Banner chào mừng ở chân trang
        JPanel chao = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Đổ bóng nhẹ
                g2.setColor(new Color(15, 23, 42, 10));
                g2.fillRoundRect(0, 2, w, h - 2, 18, 18);

                // Nền card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h - 1, 18, 18);

                // Viền ngoài
                g2.setColor(UiHelper.VIEN);
                g2.drawRoundRect(0, 0, w - 1, h - 2, 18, 18);

                // Điểm nhấn viền gradient bên trái
                GradientPaint gp = new GradientPaint(0, 0, new Color(37, 99, 235), 0, h, new Color(14, 165, 233));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 6, h - 1, 6, 6);

                g2.dispose();
            }
        };
        chao.setOpaque(false);
        chao.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel noiDungChao = new JPanel();
        noiDungChao.setOpaque(false);
        noiDungChao.setLayout(new BoxLayout(noiDungChao, BoxLayout.Y_AXIS));
        JLabel loiChao = new JLabel("Chào mừng bạn đến với Hệ thống Quản lý Gara Ô tô The T4");
        loiChao.setFont(new Font("Segoe UI", Font.BOLD, 17));
        loiChao.setForeground(UiHelper.XANH_DAM);
        JLabel huongDan = new JLabel("Hệ thống đã sẵn sàng phục vụ khách hàng. Vui lòng chọn chức năng từ menu bên trái để tiếp tục.");
        huongDan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        huongDan.setForeground(new Color(100, 116, 139));
        loiChao.setAlignmentX(Component.LEFT_ALIGNMENT);
        huongDan.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDungChao.add(loiChao);
        noiDungChao.add(Box.createVerticalStrut(4));
        noiDungChao.add(huongDan);
        chao.add(noiDungChao, BorderLayout.CENTER);

        JLabel trangThai = new JLabel("●  Hệ thống trực tuyến") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(22, 163, 74, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        trangThai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        trangThai.setForeground(new Color(22, 163, 74));
        trangThai.setBorder(new EmptyBorder(8, 14, 8, 14));
        chao.add(trangThai, BorderLayout.EAST);

        root.add(chao, BorderLayout.SOUTH);
    }

    /** Tạo thẻ thống kê hiện đại bo cong 18px với icon nổi bật */
    private JPanel theThongKe(Icon icon, String ten, String so, String moTa, Color mau) {
        JPanel p = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Bóng đổ
                g2.setColor(new Color(15, 23, 42, 8));
                g2.fillRoundRect(0, 2, w, h - 2, 18, 18);

                // Nền card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h - 1, 18, 18);

                // Viền xám nhạt
                g2.setColor(UiHelper.VIEN);
                g2.drawRoundRect(0, 0, w - 1, h - 2, 18, 18);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Khối icon có nền tròn/bo mềm mại mờ màu accent
        JLabel bieuTuong = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 26));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bieuTuong.setOpaque(false);
        bieuTuong.setHorizontalAlignment(SwingConstants.CENTER);
        bieuTuong.setPreferredSize(new Dimension(54, 54));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTen.setForeground(new Color(100, 116, 139));

        JLabel lblSo = new JLabel(so);
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblSo.setForeground(UiHelper.XANH_DAM);

        JLabel lblMoTa = new JLabel(moTa);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMoTa.setForeground(new Color(148, 163, 184));

        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(lblTen);
        text.add(Box.createVerticalStrut(2));
        text.add(lblSo);
        text.add(Box.createVerticalStrut(4));
        text.add(lblMoTa);

        p.add(bieuTuong, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }
}
