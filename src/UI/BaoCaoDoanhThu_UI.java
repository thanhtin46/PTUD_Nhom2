package UI;

import Entity.KhoDuLieu;
import Entity.PhieuKho;
import Entity.PhieuTiepNhan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.TreeMap;

public class BaoCaoDoanhThu_UI extends JPanel {
    private final boolean theoNam;
    private final DefaultTableModel model = UiHelper.model("Kỳ", "Nguồn", "Số chứng từ", "Doanh thu (VNĐ)");
    private final JLabel lblTong = new JLabel();

    public BaoCaoDoanhThu_UI(boolean theoNam) {
        this.theoNam = theoNam;
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        JLabel tde = UiHelper.tieuDeTrang(theoNam ? "💰 Báo cáo doanh thu theo năm" : "💰 Báo cáo doanh thu theo tháng");
        JLabel phu = new JLabel(theoNam ? "Tổng hợp doanh thu theo từng năm" : "Tổng hợp doanh thu theo từng tháng");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(110, 120, 135));
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(phu);
        root.add(north, BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Chi tiết doanh thu");
        JScrollPane sp = new JScrollPane(UiHelper.bang(model));
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        root.add(bangPanel, BorderLayout.CENTER);

        JPanel tongPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(46, 132, 230), getWidth(), 0, new Color(140, 95, 200));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tongPanel.setOpaque(false);
        tongPanel.setBorder(new EmptyBorder(14, 20, 14, 20));
        tongPanel.setLayout(new BorderLayout());
        lblTong.setForeground(Color.WHITE);
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTong.setHorizontalAlignment(SwingConstants.RIGHT);
        tongPanel.add(lblTong, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(12, 0, 0, 0));
        wrap.add(tongPanel, BorderLayout.CENTER);
        root.add(wrap, BorderLayout.SOUTH);
        nap();
    }

    private void nap() {
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Map<String, long[]> map = new TreeMap<>(); // value = {tổng, số chứng từ}

        for (PhieuTiepNhan p : KhoDuLieu.get().getPhieuTiepNhans()) {
            String ky = ky(p.getNgayTiepNhan(), fmt);
            if (ky == null) continue;
            String key = ky + "|Sửa chữa";
            map.computeIfAbsent(key, k -> new long[2]);
            map.get(key)[0] += p.getThanhTien();
            map.get(key)[1] += 1;
        }
        for (PhieuKho p : KhoDuLieu.get().getPhieuKhos()) {
            if (!PhieuKho.XUAT.equals(p.getLoai())) continue;
            String ky = ky(p.getNgay(), fmt);
            if (ky == null) continue;
            String key = ky + "|Xuất kho";
            map.computeIfAbsent(key, k -> new long[2]);
            map.get(key)[0] += p.getTongTien();
            map.get(key)[1] += 1;
        }

        long tong = 0;
        for (Map.Entry<String, long[]> e : map.entrySet()) {
            String[] p = e.getKey().split("\\|");
            model.addRow(new Object[]{p[0], p[1], e.getValue()[1], UiHelper.tien(e.getValue()[0])});
            tong += e.getValue()[0];
        }
        lblTong.setText("Tổng doanh thu: " + UiHelper.tien(tong) + " VNĐ");
    }

    private String ky(String ngay, DateTimeFormatter fmt) {
        try {
            LocalDate d = LocalDate.parse(ngay, fmt);
            return theoNam ? String.valueOf(d.getYear()) : String.format("%02d/%d", d.getMonthValue(), d.getYear());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
