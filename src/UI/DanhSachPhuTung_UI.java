package UI;

import Entity.KhoDuLieu;
import Entity.PhuTung;

import javax.swing.*;
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

        root.add(UiHelper.tieuDeTrang("📦 Quản lý phụ tùng"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0);
        split.setDividerLocation(380);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin phụ tùng", 380);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Mã phụ tùng *", txtMa);
        UiHelper.themDong(form, 1, "Tên phụ tùng *", txtTen);
        UiHelper.themDong(form, 2, "Đơn vị tính", txtDonVi);
        UiHelper.themDong(form, 3, "Số lượng tồn *", txtTon);
        UiHelper.themDong(form, 4, "Đơn giá (VNĐ) *", txtGia);

        JPanel nut = new JPanel(new GridLayout(1, 4, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        nut.add(UiHelper.nut("Thêm", new Color(46, 160, 92), e -> them()));
        nut.add(UiHelper.nut("Sửa", new Color(37, 99, 168), e -> sua()));
        nut.add(UiHelper.nut("Xóa", new Color(220, 80, 80), e -> xoa()));
        nut.add(UiHelper.nut("Làm mới", new Color(120, 130, 150), e -> lamMoi()));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(form, BorderLayout.CENTER);
        body.add(nut, BorderLayout.SOUTH);
        formPanel.add(body, BorderLayout.CENTER);
        return formPanel;
    }

    private JScrollPane taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phụ tùng");
        model = UiHelper.model("Mã PT", "Tên phụ tùng", "ĐVT", "Tồn kho", "Đơn giá");
        table = UiHelper.bang(model);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienLenForm();
        });
        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        JScrollPane wrap = new JScrollPane(bangPanel);
        wrap.setBorder(null);
        wrap.setOpaque(false);
        wrap.getViewport().setOpaque(false);
        return wrap;
    }

    private void napBang() {
        model.setRowCount(0);
        for (PhuTung pt : kho.getPhuTungs()) {
            model.addRow(new Object[]{pt.getMaPT(), pt.getTen(), pt.getDonVi(), pt.getSoLuongTon(), UiHelper.tien(pt.getDonGia())});
        }
    }

    private void them() {
        if (!kiemTra(true)) return;
        kho.getPhuTungs().add(new PhuTung(txtMa.getText().trim(), txtTen.getText().trim(),
                txtDonVi.getText().trim(), Integer.parseInt(txtTon.getText().trim()),
                Long.parseLong(txtGia.getText().trim().replace(",", ""))));
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, "Thêm phụ tùng thành công!");
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
        kho.getPhuTungs().removeIf(pt -> pt.getMaPT().equals(ma));
        napBang();
        lamMoi();
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
        napBang();
        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
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
