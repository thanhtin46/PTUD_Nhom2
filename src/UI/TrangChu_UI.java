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

        JLabel tde = UiHelper.tieuDeTrang("Bảng điều khiển");
        JLabel phu = new JLabel("Tổng quan hoạt động của Gara Ô tô The T4");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(112, 128, 141));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(tde);
        header.add(phu);
        root.add(header, BorderLayout.NORTH);

        KhoDuLieu kho = KhoDuLieu.get();
        int tongTon = 0;
        for (PhuTung pt : kho.getPhuTungs()) tongTon += pt.getSoLuongTon();

        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);
        grid.add(theThongKe("KH", "Khách hàng", String.valueOf(kho.getKhachHangs().size()),
                "Tổng số khách đã đăng ký", new Color(48, 126, 167)));
        grid.add(theThongKe("XE", "Phương tiện", String.valueOf(kho.getPhuongTiens().size()),
                "Đang được quản lý", new Color(203, 127, 51)));
        grid.add(theThongKe("SC", "Phiếu sửa chữa", String.valueOf(kho.getPhieuTiepNhans().size()),
                "Tiếp nhận và xử lý", new Color(49, 145, 105)));
        grid.add(theThongKe("BH", "Phiếu bảo hành", String.valueOf(kho.getPhieuBaoHanhs().size()),
                "Còn hạn và hết hạn", new Color(123, 100, 174)));
        grid.add(theThongKe("PT", "Tồn phụ tùng", String.valueOf(tongTon),
                kho.getPhuTungs().size() + " mặt hàng trong kho", new Color(47, 139, 151)));
        grid.add(theThongKe("NV", "Nhân viên", String.valueOf(kho.getNhanViens().size()),
                "Tài khoản nhân viên", new Color(180, 91, 105)));

        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);
        gridWrap.setBorder(new EmptyBorder(8, 0, 0, 0));
        gridWrap.add(grid, BorderLayout.CENTER);
        root.add(gridWrap, BorderLayout.CENTER);

        JPanel chao = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                veNenThe(g, this);
            }
        };
        chao.setOpaque(false);
        chao.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel noiDungChao = new JPanel();
        noiDungChao.setOpaque(false);
        noiDungChao.setLayout(new BoxLayout(noiDungChao, BoxLayout.Y_AXIS));
        JLabel loiChao = new JLabel("Chào mừng bạn đến với Gara Ô tô The T4");
        loiChao.setFont(new Font("Segoe UI", Font.BOLD, 17));
        loiChao.setForeground(UiHelper.XANH_DAM);
        JLabel huongDan = new JLabel("Chọn chức năng từ menu bên trái để bắt đầu công việc.");
        huongDan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        huongDan.setForeground(new Color(112, 128, 141));
        loiChao.setAlignmentX(Component.LEFT_ALIGNMENT);
        huongDan.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDungChao.add(loiChao);
        noiDungChao.add(Box.createVerticalStrut(5));
        noiDungChao.add(huongDan);
        chao.add(noiDungChao, BorderLayout.CENTER);

        JLabel trangThai = new JLabel("●  Sẵn sàng làm việc");
        trangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        trangThai.setForeground(new Color(43, 128, 91));
        trangThai.setBorder(new EmptyBorder(8, 12, 8, 12));
        chao.add(trangThai, BorderLayout.EAST);

        JPanel welcomeWrap = new JPanel(new BorderLayout());
        welcomeWrap.setOpaque(false);
        welcomeWrap.setBorder(new EmptyBorder(16, 0, 0, 0));
        welcomeWrap.add(chao, BorderLayout.CENTER);
        root.add(welcomeWrap, BorderLayout.SOUTH);
    }

    private JPanel theThongKe(String kyHieu, String ten, String so, String moTa, Color mau) {
        JPanel p = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                veNenThe(g, this);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel bieuTuong = new JLabel(kyHieu, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bieuTuong.setOpaque(false);
        bieuTuong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bieuTuong.setForeground(mau);
        bieuTuong.setPreferredSize(new Dimension(48, 48));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTen.setForeground(new Color(101, 118, 132));
        JLabel lblSo = new JLabel(so);
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 29));
        lblSo.setForeground(UiHelper.XANH_DAM);
        JLabel lblMoTa = new JLabel(moTa);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMoTa.setForeground(new Color(137, 151, 162));
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(lblTen);
        text.add(Box.createVerticalStrut(2));
        text.add(lblSo);
        text.add(Box.createVerticalStrut(2));
        text.add(lblMoTa);

        p.add(bieuTuong, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private static void veNenThe(Graphics g, JComponent panel) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UiHelper.VIEN);
        g2.fillRoundRect(0, 0, panel.getWidth(), panel.getHeight(), 16, 16);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(1, 1, panel.getWidth() - 2, panel.getHeight() - 2, 15, 15);
        g2.dispose();
    }
}
