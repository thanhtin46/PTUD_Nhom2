package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QuanLyNhanVien_UI extends JPanel {
    private static final String[] VAI_TRO = {
            "Nhân viên tiếp tân", "Nhân viên sửa chữa", "Nhân viên kho", "Quản lý"};

    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtTk = new JTextField();
    private final JPasswordField txtMk = new JPasswordField();
    private final JComboBox<String> cboVt = new JComboBox<>(VAI_TRO);
    private final JComboBox<String> cboTrangThai = new JComboBox<>(
            new String[]{NhanVien.DANG_LAM, NhanVien.NGUNG});
    private JTable table;
    private DefaultTableModel model;
    private final KhoDuLieu kho = KhoDuLieu.get();

    public QuanLyNhanVien_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý nhân viên"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0);
        split.setDividerLocation(430);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin nhân viên", 430);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Mã NV *", txtMa);
        UiHelper.themDong(form, 1, "Họ tên *", txtTen);
        UiHelper.themDong(form, 2, "Tài khoản *", txtTk);
        UiHelper.themDong(form, 3, "Mật khẩu", txtMk);
        UiHelper.themDong(form, 4, "Vai trò", cboVt);
        UiHelper.themDong(form, 5, "Trạng thái", cboTrangThai);

        JPanel nut = new JPanel(new GridLayout(2, 2, 8, 8));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));
        nut.add(UiHelper.nut("Thêm", UiHelper.XANH_LA, e -> them()));
        nut.add(UiHelper.nut("Sửa", UiHelper.XANH_TRUNG, e -> sua()));
        nut.add(UiHelper.nut("Khóa / Kích hoạt", UiHelper.CHU_CHAY, e -> voHieuHoa()));
        nut.add(UiHelper.nut("Làm mới", UiHelper.XAM, e -> lamMoi()));

        UiHelper.ganNoiDungForm(formPanel, form, nut);
        return formPanel;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách nhân viên");
        model = UiHelper.model("Mã NV", "Họ tên", "Tài khoản", "Vai trò", "Trạng thái");
        table = UiHelper.bang(model);
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
        for (NhanVien nv : kho.getNhanViens()) {
            String trangThaiHien = nv.dangHoatDong() ? "Đang làm" : "Ngưng";
            model.addRow(new Object[]{nv.getMaNV(), nv.getHoTen(), nv.getTaiKhoan(),
                    nv.getVaiTro(), trangThaiHien});
        }
    }

    private void them() {
        if (txtMa.getText().trim().isEmpty() || txtTk.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            UiHelper.loi(this, "Mã, họ tên và tài khoản không được để trống!", txtMa);
            return;
        }
        if (kho.timNhanVienTheoMa(txtMa.getText().trim()) != null) {
            UiHelper.loi(this, "Mã nhân viên đã tồn tại!", txtMa);
            return;
        }
        kho.getNhanViens().add(new NhanVien(txtMa.getText().trim(), txtTen.getText().trim(),
                txtTk.getText().trim(), new String(txtMk.getPassword()),
                String.valueOf(cboVt.getSelectedItem()),
                String.valueOf(cboTrangThai.getSelectedItem())));
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
    }

    private void voHieuHoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Chọn nhân viên cần vô hiệu hóa/kích hoạt!");
            return;
        }
        String ma = model.getValueAt(dong, 0).toString();
        NhanVien nv = kho.timNhanVienTheoMa(ma);
        if (nv == null) return;
        long soDangLam = kho.getNhanViens().stream().filter(NhanVien::dangHoatDong).count();
        if (nv.dangHoatDong() && soDangLam <= 1) {
            UiHelper.loi(this, "Không thể vô hiệu hóa người dùng cuối cùng đang hoạt động!", txtMa);
            return;
        }
        if (nv.dangHoatDong()) {
            if (JOptionPane.showConfirmDialog(this,
                    "Vô hiệu hóa tài khoản \"" + nv.getTaiKhoan() + "\"?\nNhân viên sẽ không đăng nhập được, lịch sử được giữ lại.",
                    "Xác nhận vô hiệu hóa",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            nv.setTrangThai(NhanVien.NGUNG);
        } else {
            if (JOptionPane.showConfirmDialog(this,
                    "Kích hoạt lại tài khoản \"" + nv.getTaiKhoan() + "\"?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;
            nv.setTrangThai(NhanVien.DANG_LAM);
        }
        napBang();
        lamMoi();
    }

    private void sua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Chọn nhân viên cần sửa!");
            return;
        }
        NhanVien nv = kho.timNhanVienTheoMa(model.getValueAt(dong, 0).toString());
        if (nv == null) return;
        nv.setHoTen(txtTen.getText().trim());
        nv.setTaiKhoan(txtTk.getText().trim());
        String mk = new String(txtMk.getPassword());
        if (!mk.isEmpty()) nv.setMatKhau(mk);
        nv.setVaiTro(String.valueOf(cboVt.getSelectedItem()));
        nv.setTrangThai(String.valueOf(cboTrangThai.getSelectedItem()));
        napBang();
        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
    }

    private void lamMoi() {
        txtMa.setText("");
        txtTen.setText("");
        txtTk.setText("");
        txtMk.setText("");
        cboTrangThai.setSelectedItem(NhanVien.DANG_LAM);
        txtMa.setEditable(true);
        table.clearSelection();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        NhanVien nv = kho.timNhanVienTheoMa(model.getValueAt(dong, 0).toString());
        if (nv == null) return;
        txtMa.setText(nv.getMaNV());
        txtTen.setText(nv.getHoTen());
        txtTk.setText(nv.getTaiKhoan());
        txtMk.setText("");
        cboVt.setSelectedItem(nv.getVaiTro());
        cboTrangThai.setSelectedItem(nv.getTrangThai());
        txtMa.setEditable(false);
    }
}
