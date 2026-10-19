package UI;

import Entity.KhoDuLieu;
import Entity.PhieuTiepNhan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * TraCuuPhieuTiepNhan_UI — Tra cứu hồ sơ phiếu tiếp nhận xe vào gara:
 * - Đáp ứng UC005 (Quản lý & Tra cứu phiếu tiếp nhận dịch vụ)
 * - Tìm kiếm đa năng theo Số phiếu, Biển số, Tên khách hàng, SĐT
 * - Lọc theo trạng thái tiến độ (Chờ tiếp nhận, Đang sửa, Chờ phụ tùng, Hoàn thành)
 */
public class TraCuuPhieuTiepNhan_UI extends JPanel {
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTienDo = new JComboBox<>(new String[]{
            "Tất cả tiến độ", "Chờ tiếp nhận", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"
    });
    private final JLabel lblTongPhieu = new JLabel("Tổng: 0 phiếu");
    private DefaultTableModel model;
    private JTable table;

    public TraCuuPhieuTiepNhan_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("Tra cứu phiếu tiếp nhận");
        JLabel phu = new JLabel("Tra cứu, theo dõi tiến độ và thông tin tiếp nhận phương tiện vào xưởng dịch vụ");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(Box.createVerticalStrut(4));
        north.add(phu);
        north.add(Box.createVerticalStrut(12));
        root.add(north, BorderLayout.NORTH);

        // Bảng kết quả tra cứu
        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu tiếp nhận");

        // Thanh công cụ tìm kiếm
        JPanel timPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        timPanel.setOpaque(false);
        timPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblTk = new JLabel("Từ khóa:");
        lblTk.setFont(UiHelper.FONT_NHAN);
        lblTk.setForeground(new Color(51, 65, 85));
        timPanel.add(lblTk);

        txtTuKhoa.setPreferredSize(new Dimension(240, 36));
        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.setToolTipText("Nhập số phiếu, biển số, tên khách hàng hoặc SĐT...");
        timPanel.add(txtTuKhoa);

        JLabel lblLd = new JLabel("Tiến độ:");
        lblLd.setFont(UiHelper.FONT_NHAN);
        lblLd.setForeground(new Color(51, 65, 85));
        timPanel.add(lblLd);

        cboTienDo.setPreferredSize(new Dimension(160, 36));
        cboTienDo.setFont(UiHelper.FONT_BT);
        cboTienDo.setBackground(Color.WHITE);
        timPanel.add(cboTienDo);

        JButton btnTim = UiHelper.nut("Tìm kiếm", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_DAM, true, e -> napBang());
        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, true, e -> {
            txtTuKhoa.setText("");
            cboTienDo.setSelectedIndex(0);
            napBang();
        });
        timPanel.add(btnTim);
        timPanel.add(btnLamMoi);

        txtTuKhoa.addActionListener(e -> napBang());
        cboTienDo.addActionListener(e -> napBang());

        bangPanel.add(timPanel, BorderLayout.NORTH);

        // Cấu trúc bảng
        model = UiHelper.model(
                "Số phiếu", "Mã KH", "Khách hàng", "Số ĐT", "Biển số xe",
                "Hãng / Dòng xe", "Ngày tiếp nhận", "Yêu cầu dịch vụ", "Tiến độ", "Kỹ thuật viên", "Tạm tính"
        );
        table = UiHelper.bang(model);

        // Tùy biến renderer cho cột Tiến độ (Cột 8)
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSel) {
                    String td = val != null ? val.toString() : "";
                    if (td.contains("Hoàn thành")) {
                        l.setForeground(new Color(22, 163, 74));   // Green 600
                    } else if (td.contains("Đang sửa")) {
                        l.setForeground(new Color(217, 119, 6));   // Amber 600
                    } else if (td.contains("Chờ phụ tùng")) {
                        l.setForeground(new Color(225, 29, 72));   // Rose 600
                    } else {
                        l.setForeground(new Color(71, 85, 105));   // Slate 600
                    }
                }
                return l;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        // Footer thống kê
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 6));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 0, 0, 0));
        lblTongPhieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongPhieu.setForeground(new Color(71, 85, 105));
        footer.add(lblTongPhieu);
        bangPanel.add(footer, BorderLayout.SOUTH);

        root.add(bangPanel, BorderLayout.CENTER);
        napBang();
    }

    private void napBang() {
        String tuKhoa = txtTuKhoa.getText().trim().toLowerCase();
        String locTienDo = (String) cboTienDo.getSelectedItem();
        if (locTienDo == null) locTienDo = "Tất cả tiến độ";

        model.setRowCount(0);
        int count = 0;
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (!locTienDo.equals("Tất cả tiến độ") && !locTienDo.equalsIgnoreCase(p.getTienDo())) {
                continue;
            }

            String gop = (p.getSoPhieu() + " " + p.getMaKH() + " " + p.getHoTen() + " "
                    + p.getSdt() + " " + p.getBienSo() + " " + p.getHangXe() + " "
                    + p.getLoaiXe() + " " + p.getYeuCau()).toLowerCase();

            if (tuKhoa.isEmpty() || gop.contains(tuKhoa)) {
                String xe = (p.getHangXe() != null ? p.getHangXe() : "") + " "
                        + (p.getLoaiXe() != null ? p.getLoaiXe() : "");
                model.addRow(new Object[]{
                        p.getSoPhieu(),
                        p.getMaKH() != null ? p.getMaKH() : "—",
                        p.getHoTen(),
                        p.getSdt(),
                        p.getBienSo(),
                        xe.trim(),
                        p.getNgayTiepNhan(),
                        p.getYeuCau(),
                        p.getTienDo(),
                        p.getNguoiLap() != null ? p.getNguoiLap() : "—",
                        UiHelper.tien(p.getThanhTien())
                });
                count++;
            }
        }
        lblTongPhieu.setText("Hiển thị: " + count + " / " + kho.getPhieuTiepNhans().size() + " phiếu tiếp nhận");
    }
}
