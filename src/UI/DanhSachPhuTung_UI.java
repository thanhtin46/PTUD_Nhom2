package UI;

import Entity.KhoDuLieu;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DanhSachPhuTung_UI extends JPanel {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtDonVi = new JTextField();
    private final JTextField txtTon = new JTextField();
    private final JTextField txtGia = new JTextField();
    private JTable table;
    private DefaultTableModel model;
    private final KhoDuLieu kho = KhoDuLieu.get();

    public DanhSachPhuTung_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý phụ tùng"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0.32);
        split.setDividerLocation(470);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin phụ tùng", 470);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Mã phụ tùng *", txtMa);
        UiHelper.themDong(form, 1, "Tên phụ tùng *", txtTen);
        UiHelper.themDong(form, 2, "Đơn vị tính", txtDonVi);
        UiHelper.themDong(form, 3, "Số lượng tồn *", txtTon);
        UiHelper.themDong(form, 4, "Đơn giá (VNĐ) *", txtGia);

        JPanel nut = new JPanel(new GridLayout(1, 4, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));
        nut.add(UiHelper.nut("Thêm", UiHelper.XANH_LA, e -> them()));
        nut.add(UiHelper.nut("Sửa", UiHelper.XANH_TRUNG, e -> sua()));
        nut.add(UiHelper.nut("Xóa", UiHelper.CHU_CHAY, e -> xoa()));
        nut.add(UiHelper.nut("Làm mới", UiHelper.XAM, e -> lamMoi()));

        UiHelper.ganNoiDungForm(formPanel, form, nut);
        return formPanel;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phụ tùng");
        model = UiHelper.model("Mã PT", "Tên phụ tùng", "ĐVT", "Tồn kho", "Đơn giá");
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Mã PT
        table.getColumnModel().getColumn(1).setPreferredWidth(260); // Tên phụ tùng
        table.getColumnModel().getColumn(2).setPreferredWidth(90);  // ĐVT
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Tồn kho
        table.getColumnModel().getColumn(4).setPreferredWidth(140); // Đơn giá
        UiHelper.canGiua(table, 0, 2, 3);
        UiHelper.canTrai(table, 1);
        UiHelper.canPhai(table, 4);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienLenForm();
        });
        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        return bangPanel;
    }

    private void napBang() {
        model.setRowCount(0);
        for (PhuTung pt : kho.getPhuTungs()) {
            model.addRow(new Object[]{pt.getMaPT(), pt.getTen(), pt.getDonVi(), pt.getSoLuongTon(), UiHelper.tien(pt.getDonGia())});
        }
    }

    private void them() {
        if (!kiemTra(true)) return;
        PhuTung pt = new PhuTung(txtMa.getText().trim(), txtTen.getText().trim(),
                txtDonVi.getText().trim(), Integer.parseInt(txtTon.getText().trim()),
                Long.parseLong(txtGia.getText().trim().replace(",", "")));
        boolean ok = kho.themPhuTung(pt);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Thêm phụ tùng và lưu CSDL SQL Server thành công!" : "Thêm phụ tùng thành công!");
    }

    private void xoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng cần xóa!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa phụ tùng này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String ma = model.getValueAt(dong, 0).toString();
        boolean ok = kho.xoaPhuTung(ma);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Đã xóa phụ tùng khỏi CSDL SQL Server!" : "Đã xóa phụ tùng!");
    }

    private void sua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng cần sửa!");
            return;
        }
        if (!kiemTra(false)) return;
        PhuTung pt = kho.timPhuTung(model.getValueAt(dong, 0).toString());
        if (pt == null) return;
        pt.setTen(txtTen.getText().trim());
        pt.setDonVi(txtDonVi.getText().trim());
        pt.setSoLuongTon(Integer.parseInt(txtTon.getText().trim()));
        pt.setDonGia(Long.parseLong(txtGia.getText().trim().replace(",", "")));
        boolean ok = kho.capNhatPhuTung(pt);
        napBang();
        JOptionPane.showMessageDialog(this, ok ? "Cập nhật và đồng bộ CSDL SQL Server thành công!" : "Cập nhật thành công!");
    }

    private void lamMoi() {
        txtMa.setText("");
        txtTen.setText("");
        txtDonVi.setText("");
        txtTon.setText("");
        txtGia.setText("");
        txtMa.setEditable(true);
        table.clearSelection();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        txtMa.setText(model.getValueAt(dong, 0).toString());
        txtTen.setText(model.getValueAt(dong, 1).toString());
        txtDonVi.setText(model.getValueAt(dong, 2).toString());
        txtTon.setText(model.getValueAt(dong, 3).toString());
        txtGia.setText(model.getValueAt(dong, 4).toString().replace(",", ""));
        txtMa.setEditable(false);
    }

    private boolean kiemTra(boolean them) {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            UiHelper.loi(this, "Mã và tên phụ tùng không được để trống!", txtMa);
            return false;
        }
        if (!txtTon.getText().trim().matches("\\d+") || !txtGia.getText().trim().replace(",", "").matches("\\d+")) {
            UiHelper.loi(this, "Tồn kho và đơn giá phải là số nguyên!", txtTon);
            return false;
        }
        if (them && kho.timPhuTung(txtMa.getText().trim()) != null) {
            UiHelper.loi(this, "Mã phụ tùng đã tồn tại!", txtMa);
            return false;
        }
        return true;
    }
}
