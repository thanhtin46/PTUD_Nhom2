package UI;

import Entity.ChiTietKho;
import Entity.KhoDuLieu;
import Entity.PhieuKho;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * UC010: Theo dõi phụ tùng chờ nhập.
 * - Liệt kê các phiếu nhập có trangThai = CHO_NHAP.
 * - Cảnh báo trễ hạn giao hàng.
 * - Cho phép xác nhận đã nhập / hủy.
 */
public class TheoDoiChoNhap_UI extends JPanel {
    private final KhoDuLieu kho = KhoDuLieu.get();
    private JTable bang;
    private DefaultTableModel model;
    private PhieuKho phieuDangChon;

    public TheoDoiChoNhap_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("⏳ Theo dõi phụ tùng chờ nhập");
        JLabel phu = new JLabel("Đơn hàng đã đặt từ nhà cung cấp, đang chờ giao hàng");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(110, 120, 135));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(phu);
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        // ==== Thẻ thống kê ====
        int soChoNhap = 0, soTreHan = 0;
        long giaTriChoNhap = 0;
        LocalDate homNay = LocalDate.now();
        for (PhieuKho pk : kho.getPhieuKhos()) {
            if (!pk.isChoNhap()) continue;
            soChoNhap++;
            giaTriChoNhap += pk.getTongTien();
            if (!pk.getNgayDuKienGiao().isEmpty()) {
                try {
                    LocalDate d = LocalDate.parse(pk.getNgayDuKienGiao(),
                            DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    if (ChronoUnit.DAYS.between(d, homNay) > 0) soTreHan++;
                } catch (Exception ignored) {}
            }
        }
        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(0, 0, 14, 0));
        grid.add(the("📦", "Đang chờ nhập", String.valueOf(soChoNhap), "Phiếu từ NCC", new Color(37, 99, 168)));
        grid.add(the("🚨", "Trễ hạn giao", String.valueOf(soTreHan), "Cần liên hệ NCC", new Color(220, 80, 80)));
        grid.add(the("💰", "Giá trị chờ", UiHelper.tien(giaTriChoNhap) + " đ", "Tổng đơn chưa nhập", new Color(225, 150, 45)));
        JPanel wrapGrid = new JPanel(new BorderLayout());
        wrapGrid.setPreferredSize(new Dimension(0, 100));
        wrapGrid.setOpaque(false);
        wrapGrid.add(grid, BorderLayout.CENTER);
        root.add(wrapGrid, BorderLayout.CENTER);

        // ==== Bảng ====
        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu chờ nhập");
        model = UiHelper.model("Số phiếu", "Ngày đặt", "Nhà cung cấp", "Dự kiến giao",
                "Số mặt hàng", "Tổng tiền", "Trạng thái", "Cảnh báo");
        napBang();
        bang = UiHelper.bang(model);
        bang.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String cb = v == null ? "" : v.toString();
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                if (cb.contains("TRỄ")) {
                    comp.setBackground(new Color(220, 80, 80));
                    comp.setForeground(Color.WHITE);
                } else if (cb.contains("SẮP")) {
                    comp.setBackground(new Color(225, 150, 45));
                    comp.setForeground(Color.WHITE);
                } else if (cb.contains("ĐÚNG")) {
                    comp.setBackground(new Color(46, 160, 92));
                    comp.setForeground(Color.WHITE);
                } else {
                    comp.setBackground(Color.LIGHT_GRAY);
                    comp.setForeground(Color.DARK_GRAY);
                }
                return comp;
            }
        });
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = bang.getSelectedRow();
                if (row >= 0) {
                    String soPhieu = model.getValueAt(row, 0).toString();
                    for (PhieuKho pk : kho.getPhieuKhos()) {
                        if (pk.getSoPhieu().equals(soPhieu)) {
                            phieuDangChon = pk;
                            break;
                        }
                    }
                }
            }
        });
        JScrollPane sp = new JScrollPane(bang);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        JScrollPane wrapBang = new JScrollPane(bangPanel);
        wrapBang.setBorder(null);
        wrapBang.setOpaque(false);
        wrapBang.getViewport().setOpaque(false);
        wrapBang.setPreferredSize(new Dimension(0, 0));

        // ==== Nút hành động ====
        JPanel nutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nutPanel.setOpaque(false);
        nutPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnXem = UiHelper.nut("👁 Xem chi tiết", new Color(37, 99, 168), e -> xemChiTiet());
        JButton btnNhap = UiHelper.nut("✅ Xác nhận đã nhập", new Color(46, 160, 92), e -> xacNhanNhap());
        JButton btnHuy = UiHelper.nut("❌ Hủy phiếu", new Color(220, 80, 80), e -> huyPhieu());
        nutPanel.add(btnXem);
        nutPanel.add(btnNhap);
        nutPanel.add(btnHuy);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(wrapBang, BorderLayout.CENTER);
        south.add(nutPanel, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
    }

    private void napBang() {
        model.setRowCount(0);
        LocalDate homNay = LocalDate.now();
        for (PhieuKho pk : kho.getPhieuKhos()) {
            if (!pk.isChoNhap()) continue;
            String canhBao = "";
            if (!pk.getNgayDuKienGiao().isEmpty()) {
                try {
                    LocalDate d = LocalDate.parse(pk.getNgayDuKienGiao(),
                            DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    long ngay = ChronoUnit.DAYS.between(d, homNay);
                    if (ngay > 0)      canhBao = "TRỄ " + ngay + " ngày";
                    else if (ngay == 0) canhBao = "ĐÚNG hẹn";
                    else if (ngay >= -3) canhBao = "SẮP đến (" + (-ngay) + " ngày)";
                    else canhBao = "Còn " + (-ngay) + " ngày";
                } catch (Exception ex) {
                    canhBao = "?";
                }
            }
            model.addRow(new Object[]{pk.getSoPhieu(), pk.getNgay(), pk.getNhaCungCap(),
                    pk.getNgayDuKienGiao(), pk.getChiTiet().size(),
                    UiHelper.tien(pk.getTongTien()), pk.getTrangThaiHienThi(), canhBao});
        }
    }

    private void xemChiTiet() {
        if (phieuDangChon == null) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần xem!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Số phiếu: ").append(phieuDangChon.getSoPhieu()).append("\n");
        sb.append("Ngày đặt: ").append(phieuDangChon.getNgay()).append("\n");
        sb.append("Nhà cung cấp: ").append(phieuDangChon.getNhaCungCap()).append("\n");
        sb.append("Dự kiến giao: ").append(phieuDangChon.getNgayDuKienGiao()).append("\n");
        sb.append("Người lập: ").append(phieuDangChon.getNguoiLap()).append("\n");
        sb.append("Ghi chú: ").append(phieuDangChon.getGhiChu()).append("\n\n");
        sb.append("Chi tiết:\n");
        for (ChiTietKho ct : phieuDangChon.getChiTiet()) {
            sb.append("  - ").append(ct.getTenPT())
                    .append("  x").append(ct.getSoLuong())
                    .append("  = ").append(UiHelper.tien(ct.getThanhTien())).append("đ\n");
        }
        sb.append("\nTổng: ").append(UiHelper.tien(phieuDangChon.getTongTien())).append("đ");
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(500, 360));
        JOptionPane.showMessageDialog(this, sp, "Chi tiết phiếu " + phieuDangChon.getSoPhieu(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void xacNhanNhap() {
        if (phieuDangChon == null) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần xác nhận!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xác nhận đã nhập hàng cho phiếu " + phieuDangChon.getSoPhieu() + "?\n" +
                "Số lượng sẽ được cộng vào tồn kho.",
                "Xác nhận nhập hàng",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;
        boolean ok = kho.xacNhanNhap(phieuDangChon);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật tồn kho thành công!");
            napBang();
            phieuDangChon = null;
        } else {
            UiHelper.loi(this, "Không thể xác nhận phiếu này!", null);
        }
    }

    private void huyPhieu() {
        if (phieuDangChon == null) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần hủy!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Hủy phiếu chờ nhập " + phieuDangChon.getSoPhieu() + "?",
                "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        boolean ok = kho.huyPhieuChoNhap(phieuDangChon);
        if (ok) {
            napBang();
            phieuDangChon = null;
        }
    }

    private JPanel the(String icon, String ten, String so, String phuDe, Color mau) {
        JPanel p = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(248, 250, 253));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new UiHelper.ShadowBorder(6, new Color(0, 0, 0, 18)),
                new EmptyBorder(14, 18, 14, 18)));
        p.setPreferredSize(new Dimension(0, 100));
        p.setMinimumSize(new Dimension(0, 100));

        JLabel lblIcon = new JLabel(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UiHelper.lighten(mau, 0.15f), 0, getHeight(), mau));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(48, 48));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTen.setForeground(new Color(100, 110, 125));
        JLabel lblSo = new JLabel(so);
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSo.setForeground(mau);
        JLabel lblPhu = new JLabel(phuDe);
        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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

