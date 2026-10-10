package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UC015: Phân công việc cho kỹ thuật viên.
 * - Liệt kê phiếu sửa chữa (chưa hoàn thành).
 * - Chọn KTV từ danh sách nhân viên sửa chữa.
 * - Lưu vào PhieuTiepNhan.kyThuatVien.
 * - Tự động đổi tiến độ "Chờ tiếp nhận" → "Đã phân công".
 */
public class PhanCongKyThuatVien_UI extends JPanel {
    private final KhoDuLieu kho = KhoDuLieu.get();
    private JTable bang;
    private DefaultTableModel model;
    private JComboBox<String> cboKtv;
    private PhieuTiepNhan phieuDangChon;

    public PhanCongKyThuatVien_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("👷 Phân công kỹ thuật viên");
        JLabel phu = new JLabel("Giao việc sửa chữa cho từng kỹ thuật viên");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(110, 120, 135));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(phu);
        root.add(north, BorderLayout.NORTH);

        // ==== Thẻ thống kê ====
        int soChoPC = 0, soDaPC = 0, soDangSua = 0;
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            String td = p.getTienDo() == null ? "" : p.getTienDo();
            if (td.contains("Chờ") || td.isEmpty() || td.contains("tiếp nhận")) {
                if (!p.daPhanCong()) soChoPC++;
                else soDaPC++;
            } else if (td.contains("Đang sửa") || td.contains("sửa")) {
                soDangSua++;
            }
        }
        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(0, 0, 14, 0));
        grid.add(the("📋", "Chờ phân công", String.valueOf(soChoPC), "Cần giao việc gấp", new Color(220, 80, 80)));
        grid.add(the("✅", "Đã phân công", String.valueOf(soDaPC), "Đang chờ KTV nhận", new Color(225, 150, 45)));
        grid.add(the("🔧", "Đang sửa", String.valueOf(soDangSua), "KTV đang xử lý", new Color(46, 160, 92)));
        JPanel wrapGrid = new JPanel(new BorderLayout());
        wrapGrid.setPreferredSize(new Dimension(0, 100));
        wrapGrid.setOpaque(false);
        wrapGrid.add(grid, BorderLayout.CENTER);
        root.add(wrapGrid, BorderLayout.CENTER);

        // ==== Bảng ====
        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu sửa chữa");
        model = UiHelper.model("Số phiếu", "Họ tên", "Biển số", "Yêu cầu", "KTV phụ trách", "Tiến độ");
        napBang();
        bang = UiHelper.bang(model);
        // Tô màu cột tiến độ
        bang.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String td = v == null ? "" : v.toString();
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                if (td.contains("Chờ") || td.contains("tiếp nhận")) {
                    comp.setBackground(new Color(255, 232, 232));
                    comp.setForeground(new Color(160, 30, 30));
                } else if (td.contains("phân công")) {
                    comp.setBackground(new Color(255, 245, 224));
                    comp.setForeground(new Color(180, 100, 0));
                } else if (td.contains("Đang sửa") || td.contains("sửa")) {
                    comp.setBackground(new Color(224, 245, 232));
                    comp.setForeground(new Color(30, 120, 60));
                } else if (td.contains("Hoàn")) {
                    comp.setBackground(new Color(220, 235, 250));
                    comp.setForeground(new Color(15, 35, 70));
                }
                return comp;
            }
        });
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = bang.getSelectedRow();
                if (r >= 0) {
                    String soPhieu = model.getValueAt(r, 0).toString();
                    for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
                        if (p.getSoPhieu().equals(soPhieu)) {
                            phieuDangChon = p;
                            if (p.daPhanCong()) {
                                cboKtv.setSelectedItem(tenKtv(p.getKyThuatVien()));
                            }
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

        // ==== Panel phân công ====
        JPanel pcPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pcPanel.setOpaque(false);
        pcPanel.setBorder(new EmptyBorder(12, 4, 0, 4));
        JLabel lblKtv = new JLabel("Chọn kỹ thuật viên:");
        lblKtv.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKtv.setForeground(new Color(60, 70, 85));
        cboKtv = new JComboBox<>();
        napComboKtv();
        cboKtv.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboKtv.setPreferredSize(new Dimension(280, 34));
        JButton btnPhanCong = UiHelper.nut("👷 Phân công", new Color(37, 99, 168), e -> phanCong());
        JButton btnBoPhanCong = UiHelper.nut("🔄 Bỏ phân công", new Color(220, 80, 80), e -> boPhanCong());
        pcPanel.add(lblKtv);
        pcPanel.add(cboKtv);
        pcPanel.add(btnPhanCong);
        pcPanel.add(btnBoPhanCong);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(wrapBang, BorderLayout.CENTER);
        south.add(pcPanel, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
    }

    private void napComboKtv() {
        cboKtv.removeAllItems();
        cboKtv.addItem("-- Chọn KTV --");
        for (NhanVien nv : kho.getNhanViens()) {
            if (!nv.dangHoatDong()) continue;
            // Chỉ lấy nhân viên sửa chữa + Quản lý
            if (nv.getVaiTro().contains("sửa chữa") || nv.getVaiTro().contains("Quản lý")) {
                cboKtv.addItem(nv.getMaNV() + " - " + nv.getHoTen());
            }
        }
    }

    private String tenKtv(String maNV) {
        NhanVien nv = kho.timNhanVienTheoMa(maNV);
        return nv == null ? "" : nv.getMaNV() + " - " + nv.getHoTen();
    }

    private void napBang() {
        model.setRowCount(0);
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            String td = p.getTienDo() == null ? "" : p.getTienDo();
            String ktv = "";
            if (p.daPhanCong()) {
                NhanVien nv = kho.timNhanVienTheoMa(p.getKyThuatVien());
                ktv = nv == null ? p.getKyThuatVien() : nv.getHoTen();
            } else {
                ktv = "(chưa phân công)";
            }
            model.addRow(new Object[]{p.getSoPhieu(), p.getHoTen(), p.getBienSo(),
                    p.getYeuCau(), ktv, td});
        }
    }

    private void phanCong() {
        if (phieuDangChon == null) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần phân công!");
            return;
        }
        Object sel = cboKtv.getSelectedItem();
        if (sel == null || sel.toString().startsWith("--")) {
            UiHelper.loi(this, "Vui lòng chọn kỹ thuật viên!", null);
            return;
        }
        String maKtv = sel.toString().split(" - ")[0];
        phieuDangChon.setKyThuatVien(maKtv);
        // Tự động đổi tiến độ: "Chờ tiếp nhận" → "Đã phân công"
        String td = phieuDangChon.getTienDo();
        if (td == null || td.isEmpty() || td.contains("Chờ") || td.contains("tiếp nhận")) {
            phieuDangChon.setTienDo("Đã phân công");
        }
        JOptionPane.showMessageDialog(this,
                "Đã phân công " + sel + " cho phiếu " + phieuDangChon.getSoPhieu() + "!",
                "Phân công thành công",
                JOptionPane.INFORMATION_MESSAGE);
        napBang();
    }

    private void boPhanCong() {
        if (phieuDangChon == null) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần bỏ phân công!");
            return;
        }
        if (!phieuDangChon.daPhanCong()) {
            JOptionPane.showMessageDialog(this, "Phiếu này chưa được phân công!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Bỏ phân công phiếu " + phieuDangChon.getSoPhieu() + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        phieuDangChon.setKyThuatVien("");
        phieuDangChon.setTienDo("Chờ tiếp nhận");
        napBang();
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

