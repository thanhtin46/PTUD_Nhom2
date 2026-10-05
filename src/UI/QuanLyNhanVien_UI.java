package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;

import javax.swing.*;
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

        JLabel tde = UiHelper.tieuDeTrang("👨‍💼 Quản lý nhân viên");
        JLabel phu = new JLabel("Thêm, sửa, vô hiệu hóa & phân quyền nhân viên");
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

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0);
        split.setDividerLocation(400);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin nhân viên", 400);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Mã NV *", txtMa);
        UiHelper.themDong(form, 1, "Họ tên *", txtTen);
        UiHelper.themDong(form, 2, "Tài khoản *", txtTk);
        UiHelper.themDong(form, 3, "Mật khẩu", txtMk);
        UiHelper.themDong(form, 4, "Vai trò", cboVt);
        UiHelper.themDong(form, 5, "Trạng thái", cboTrangThai);

        JPanel nut = new JPanel(new GridLayout(1, 4, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        nut.add(UiHelper.nut("Thêm", new Color(46, 160, 92), e -> them()));
        nut.add(UiHelper.nut("Sửa", new Color(37, 99, 168), e -> sua()));
        nut.add(UiHelper.nut("Vô hiệu hóa", new Color(220, 80, 80), e -> voHieuHoa()));
        nut.add(UiHelper.nut("Làm mới", new Color(120, 130, 150), e -> lamMoi()));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(form, BorderLayout.CENTER);
        body.add(nut, BorderLayout.SOUTH);
        formPanel.add(body, BorderLayout.CENTER);
        return formPanel;
    }

    private JScrollPane taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách nhân viên");
        model = UiHelper.model("Mã NV", "Họ tên", "Tài khoản", "Vai trò", "Trạng thái");
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

    /**
     * UC013: "Xóa" nhân viên = vô hiệu hóa (giữ lại lịch sử).
     * Nếu đang NGUNG thì chuyển lại DANG_LAM.
     */
    private void voHieuHoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Chọn nhân viên cần vô hiệu hóa/kích hoạt!");
            return;
        }
        String ma = model.getValueAt(dong, 0).toString();
        NhanVien nv = kho.timNhanVienTheoMa(ma);
        if (nv == null) return;
        // Không cho vô hiệu hóa nếu chỉ còn 1 người đang hoạt động
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
