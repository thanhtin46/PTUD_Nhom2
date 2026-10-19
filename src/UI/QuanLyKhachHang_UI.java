package UI;

import Entity.KhachHang;
import Entity.KhoDuLieu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class QuanLyKhachHang_UI extends JPanel {
    private JTextField txtMa, txtTen, txtSdt, txtCccd, txtDiaChi, txtEmail;
    private JTextArea txtGhiChu;
    private JTable table;
    private DefaultTableModel model;
    private final KhoDuLieu kho = KhoDuLieu.get();

    public QuanLyKhachHang_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý khách hàng"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0);
        split.setDividerLocation(440);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin khách hàng", 440);

        txtMa = new JTextField();
        txtTen = new JTextField();
        txtSdt = new JTextField();
        txtCccd = new JTextField();
        txtDiaChi = new JTextField();
        txtEmail = new JTextField();
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        JScrollPane spGhiChu = new JScrollPane(txtGhiChu);
        spGhiChu.setPreferredSize(new Dimension(190, 75));
        spGhiChu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        spGhiChu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Mã khách hàng *", txtMa);
        UiHelper.themDong(form, 1, "Họ và tên *", txtTen);
        UiHelper.themDong(form, 2, "Số điện thoại *", txtSdt);
        UiHelper.themDong(form, 3, "CCCD/CMND", txtCccd);
        UiHelper.themDong(form, 4, "Địa chỉ", txtDiaChi);
        UiHelper.themDong(form, 5, "Email", txtEmail);
        UiHelper.themDong(form, 6, "Ghi chú / Nhóm KH", spGhiChu);

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
        JPanel bangPanel = UiHelper.panelBang("Danh sách khách hàng");
        model = UiHelper.model("Mã KH", "Họ tên", "SĐT", "CCCD", "Địa chỉ", "Email", "Ghi chú", "Ngày ĐK");
        table = UiHelper.bang(model);

        // Chế độ tự động dãn đều 100% khi phóng to và cuộn ngang khi thu nhỏ
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // Đặt độ rộng tối thiểu và phù hợp cho từng cột để hiện đầy đủ thông tin
        table.getColumnModel().getColumn(0).setPreferredWidth(90);   // Mã KH
        table.getColumnModel().getColumn(1).setPreferredWidth(170);  // Họ tên
        table.getColumnModel().getColumn(2).setPreferredWidth(110);  // SĐT
        table.getColumnModel().getColumn(3).setPreferredWidth(130);  // CCCD
        table.getColumnModel().getColumn(4).setPreferredWidth(220);  // Địa chỉ
        table.getColumnModel().getColumn(5).setPreferredWidth(180);  // Email
        table.getColumnModel().getColumn(6).setPreferredWidth(160);  // Ghi chú
        table.getColumnModel().getColumn(7).setPreferredWidth(110);  // Ngày ĐK

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienLenForm();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        return bangPanel;
    }

    private void napBang() {
        model.setRowCount(0);
        for (KhachHang kh : kho.getKhachHangs()) {
            model.addRow(new Object[]{kh.getMaKH(), kh.getHoTen(), kh.getSdt(), kh.getCccd(),
                    kh.getDiaChi(), kh.getEmail(), kh.getGhiChu(), kh.getNgayDangKy()});
        }
    }

    private void them() {
        if (!kiemTraForm(true)) return;
        String ngayHomNay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        KhachHang kh = new KhachHang(
                txtMa.getText().trim(), txtTen.getText().trim(), txtSdt.getText().trim(),
                txtCccd.getText().trim(), txtDiaChi.getText().trim(), txtEmail.getText().trim(),
                txtGhiChu.getText().trim(), ngayHomNay);
        boolean ok = kho.themKhachHang(kh);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Thêm khách hàng và lưu CSDL SQL Server thành công!" : "Thêm khách hàng thành công!");
    }

    private void xoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khách hàng này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        String ma = model.getValueAt(dong, 0).toString();
        boolean ok = kho.xoaKhachHang(ma);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Đã xóa khách hàng khỏi CSDL SQL Server!" : "Đã xóa khách hàng!");
    }

    private void sua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!");
            return;
        }
        if (!kiemTraForm(false)) return;
        KhachHang kh = kho.timKhachHangTheoMa(model.getValueAt(dong, 0).toString());
        if (kh == null) return;
        kh.setHoTen(txtTen.getText().trim());
        kh.setSdt(txtSdt.getText().trim());
        kh.setCccd(txtCccd.getText().trim());
        kh.setDiaChi(txtDiaChi.getText().trim());
        kh.setEmail(txtEmail.getText().trim());
        kh.setGhiChu(txtGhiChu.getText().trim());
        boolean ok = kho.capNhatKhachHang(kh);
        napBang();
        JOptionPane.showMessageDialog(this, ok ? "Cập nhật và đồng bộ CSDL SQL Server thành công!" : "Cập nhật thành công!");
    }

    private void lamMoi() {
        txtMa.setText("");
        txtTen.setText("");
        txtSdt.setText("");
        txtCccd.setText("");
        txtDiaChi.setText("");
        txtEmail.setText("");
        txtGhiChu.setText("");
        txtMa.setEditable(true);
        table.clearSelection();
        txtMa.requestFocus();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        txtMa.setText(model.getValueAt(dong, 0).toString());
        txtTen.setText(model.getValueAt(dong, 1).toString());
        txtSdt.setText(model.getValueAt(dong, 2).toString());
        txtCccd.setText(model.getValueAt(dong, 3).toString());
        txtDiaChi.setText(model.getValueAt(dong, 4).toString());
        txtEmail.setText(model.getValueAt(dong, 5).toString());
        txtGhiChu.setText(model.getValueAt(dong, 6).toString());
        txtMa.setEditable(false);
    }

    private boolean kiemTraForm(boolean laThem) {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String cccd = txtCccd.getText().trim();
        String email = txtEmail.getText().trim();

        if (ma.isEmpty() || ten.isEmpty() || sdt.isEmpty()) {
            UiHelper.loi(this, "Mã khách hàng, Họ tên và Số điện thoại không được để trống!", txtMa);
            return false;
        }
        if (!sdt.matches("0\\d{9}")) {
            UiHelper.loi(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0!", txtSdt);
            return false;
        }
        if (!cccd.isEmpty() && !cccd.matches("\\d{9}|\\d{12}")) {
            UiHelper.loi(this, "CCCD/CMND phải gồm 9 hoặc 12 chữ số!", txtCccd);
            return false;
        }
        if (!email.isEmpty() && !email.contains("@")) {
            UiHelper.loi(this, "Email không hợp lệ!", txtEmail);
            return false;
        }
        if (laThem && kho.timKhachHangTheoMa(ma) != null) {
            UiHelper.loi(this, "Mã khách hàng \"" + ma + "\" đã tồn tại!", txtMa);
            return false;
        }
        return true;
    }
}
