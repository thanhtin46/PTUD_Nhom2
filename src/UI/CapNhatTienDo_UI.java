package UI;

import Entity.KhoDuLieu;
import Entity.PhieuTiepNhan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CapNhatTienDo_UI extends JPanel {
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final JComboBox<String> cboTienDo = new JComboBox<>(new String[]{
            "Chờ tiếp nhận", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"});
    private JTable table;
    private DefaultTableModel model;

    public CapNhatTienDo_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("⏱ Cập nhật tiến độ sửa chữa"), BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu");
        model = UiHelper.model("Số phiếu", "Biển số", "Khách", "Yêu cầu", "Tiến độ", "Thành tiền");
        table = UiHelper.bang(model);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                cboTienDo.setSelectedItem(model.getValueAt(table.getSelectedRow(), 4).toString());
            }
        });
        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        duoi.setOpaque(false);
        duoi.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel lbl = new JLabel("Tiến độ mới:");
        lbl.setFont(UiHelper.FONT_NHAN);
        lbl.setForeground(new Color(60, 70, 85));
        duoi.add(lbl);
        duoi.add(cboTienDo);
        duoi.add(UiHelper.nut("✓  Cập nhật", new Color(37, 99, 168), e -> capNhat()));
        bangPanel.add(duoi, BorderLayout.SOUTH);

        root.add(bangPanel, BorderLayout.CENTER);
        napBang();
    }

    private void napBang() {
        model.setRowCount(0);
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            model.addRow(new Object[]{p.getSoPhieu(), p.getBienSo(), p.getHoTen(), p.getYeuCau(),
                    p.getTienDo(), UiHelper.tien(p.getThanhTien())});
        }
    }

    private void capNhat() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần cập nhật tiến độ!");
            return;
        }
        String so = model.getValueAt(dong, 0).toString();
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (p.getSoPhieu().equals(so)) {
                p.setTienDo(String.valueOf(cboTienDo.getSelectedItem()));
                break;
            }
        }
        napBang();
        JOptionPane.showMessageDialog(this, "Đã cập nhật tiến độ!");
    }
}
