package UI;

import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class QuanLyPhuongTien_UI extends JPanel {
    private JTextField txtBienSo, txtSoKhung, txtSoMay, txtMauSon, txtNamSX, txtOdo;
    private JComboBox<String> cboHang, cboDong, cboChuXe;
    private JTable table;
    private DefaultTableModel model;
    private final KhoDuLieu kho = KhoDuLieu.get();

    private static final String[] HANG = {"Toyota", "Honda", "Hyundai", "Mazda", "Kia"};
    private static final String[][] DONG = {
        {"Vios 2021", "Camry", "Innova", "Fortuner"},
        {"City", "Civic", "CR-V"},
        {"Accent", "Elantra", "Tucson"},
        {"Mazda3", "CX-5", "CX-8"},
        {"Morning", "Cerato", "Seltos"}
    };

    public QuanLyPhuongTien_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý phương tiện"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0);
        split.setDividerLocation(440);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin xe", 440);

        txtBienSo = new JTextField();
        txtSoKhung = new JTextField();
        txtSoMay = new JTextField();
        txtMauSon = new JTextField();
        txtNamSX = new JTextField();
        txtOdo = new JTextField();

        cboHang = new JComboBox<>(HANG);
        cboDong = new JComboBox<>(DONG[0]);
        cboHang.addActionListener(e ->
                cboDong.setModel(new DefaultComboBoxModel<>(DONG[cboHang.getSelectedIndex()])));

        cboChuXe = new JComboBox<>();
        napComboChuXe();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Biển số xe *", txtBienSo);
        UiHelper.themDong(form, 1, "Số khung (VIN) *", txtSoKhung);
        UiHelper.themDong(form, 2, "Số máy", txtSoMay);
        UiHelper.themDong(form, 3, "Hãng xe", cboHang);
        UiHelper.themDong(form, 4, "Dòng xe / Model", cboDong);
        UiHelper.themDong(form, 5, "Màu sơn", txtMauSon);
        UiHelper.themDong(form, 6, "Năm sản xuất", txtNamSX);
        UiHelper.themDong(form, 7, "Chủ sở hữu", cboChuXe);
        UiHelper.themDong(form, 8, "Số km hiện tại", txtOdo);

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

    private void napComboChuXe() {
        cboChuXe.removeAllItems();
        for (KhachHang kh : kho.getKhachHangs()) {
            cboChuXe.addItem(kh.getMaKH() + " - " + kh.getHoTen());
        }
    }

    private String maChuDangChon() {
        Object sel = cboChuXe.getSelectedItem();
        if (sel == null) return "";
        String s = sel.toString();
        int i = s.indexOf(" - ");
        return i > 0 ? s.substring(0, i) : s;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phương tiện");
        model = UiHelper.model("Biển số", "Hãng", "Dòng xe", "VIN", "Màu", "Năm", "ODO (km)",
                "Mã chủ", "Tên chủ", "SĐT", "Tiếp nhận gần nhất");
        table = UiHelper.bang(model);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(110);  // Biển số
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Hãng
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Dòng xe
        table.getColumnModel().getColumn(3).setPreferredWidth(170);  // VIN
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Màu
        table.getColumnModel().getColumn(5).setPreferredWidth(70);   // Năm
        table.getColumnModel().getColumn(6).setPreferredWidth(90);   // ODO
        table.getColumnModel().getColumn(7).setPreferredWidth(90);   // Mã chủ
        table.getColumnModel().getColumn(8).setPreferredWidth(160);  // Tên chủ
        table.getColumnModel().getColumn(9).setPreferredWidth(110);  // SĐT
        table.getColumnModel().getColumn(10).setPreferredWidth(140); // Tiếp nhận gần nhất

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
        for (PhuongTien xe : kho.getPhuongTiens()) {
            KhachHang chu = kho.timKhachHangTheoMa(xe.getMaChuXe());
            String ten = chu != null ? chu.getHoTen() : "";
            String sdt = chu != null ? chu.getSdt() : "";
            model.addRow(new Object[]{xe.getBienSo(), xe.getHangXe(), xe.getDongXe(), xe.getSoKhung(),
                    xe.getMauSon(), xe.getNamSX(), xe.getOdo(),
                    xe.getMaChuXe(), ten, sdt, xe.getNgayTiepNhanGanNhat()});
        }
    }

    private void them() {
        if (!kiemTraForm(true)) return;
        String maChu = maChuDangChon();
        if (maChu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chủ sở hữu!");
            return;
        }
        String ngayHomNay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        kho.getPhuongTiens().add(new PhuongTien(
                txtBienSo.getText().trim(),
                String.valueOf(cboHang.getSelectedItem()),
                String.valueOf(cboDong.getSelectedItem()),
                txtSoKhung.getText().trim(), txtSoMay.getText().trim(), txtMauSon.getText().trim(),
                txtNamSX.getText().trim(), txtOdo.getText().trim(), maChu, ngayHomNay));
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, "Thêm xe thành công!");
    }

    private void xoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn xe cần xóa!");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa xe này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        String bienSo = model.getValueAt(dong, 0).toString();
        kho.getPhuongTiens().removeIf(xe -> xe.getBienSo().equals(bienSo));
        napBang();
        lamMoi();
    }

    private void sua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn xe cần sửa!");
            return;
        }
        if (!kiemTraForm(false)) return;
        PhuongTien xe = kho.timXeTheoBienSo(model.getValueAt(dong, 0).toString());
        if (xe == null) return;
        xe.setHangXe(String.valueOf(cboHang.getSelectedItem()));
        xe.setDongXe(String.valueOf(cboDong.getSelectedItem()));
        xe.setSoKhung(txtSoKhung.getText().trim());
        xe.setSoMay(txtSoMay.getText().trim());
        xe.setMauSon(txtMauSon.getText().trim());
        xe.setNamSX(txtNamSX.getText().trim());
        xe.setOdo(txtOdo.getText().trim());
        xe.setMaChuXe(maChuDangChon());
        napBang();
        JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
    }

    private void lamMoi() {
        txtBienSo.setText("");
        txtSoKhung.setText("");
        txtSoMay.setText("");
        txtMauSon.setText("");
        txtNamSX.setText("");
        txtOdo.setText("");
        cboHang.setSelectedIndex(0);
        if (cboDong.getItemCount() > 0) cboDong.setSelectedIndex(0);
        if (cboChuXe.getItemCount() > 0) cboChuXe.setSelectedIndex(0);
        txtBienSo.setEditable(true);
        table.clearSelection();
        txtBienSo.requestFocus();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        txtBienSo.setText(model.getValueAt(dong, 0).toString());
        cboHang.setSelectedItem(model.getValueAt(dong, 1));
        cboDong.setSelectedItem(model.getValueAt(dong, 2));
        txtSoKhung.setText(model.getValueAt(dong, 3).toString());
        txtMauSon.setText(model.getValueAt(dong, 4).toString());
        txtNamSX.setText(model.getValueAt(dong, 5).toString());
        txtOdo.setText(model.getValueAt(dong, 6).toString());

        String maChu = model.getValueAt(dong, 7).toString();
        for (int i = 0; i < cboChuXe.getItemCount(); i++) {
            if (cboChuXe.getItemAt(i).startsWith(maChu + " - ")) {
                cboChuXe.setSelectedIndex(i);
                break;
            }
        }
        txtBienSo.setEditable(false);
    }

    private boolean kiemTraForm(boolean laThem) {
        String bienSo = txtBienSo.getText().trim();
        String soKhung = txtSoKhung.getText().trim();
        String namSX = txtNamSX.getText().trim();
        String odo = txtOdo.getText().trim();

        if (bienSo.isEmpty() || soKhung.isEmpty()) {
            UiHelper.loi(this, "Biển số xe và Số khung không được để trống!", txtBienSo);
            return false;
        }
        if (!namSX.isEmpty()) {
            int namMax = LocalDate.now().getYear() + 1;
            if (!namSX.matches("\\d{4}") || Integer.parseInt(namSX) < 1950 || Integer.parseInt(namSX) > namMax) {
                UiHelper.loi(this, "Năm sản xuất phải gồm 4 chữ số, từ 1950 đến " + namMax + "!", txtNamSX);
                return false;
            }
        }
        if (!odo.isEmpty() && !odo.matches("\\d+")) {
            UiHelper.loi(this, "Số km hiện tại chỉ được nhập số!", txtOdo);
            return false;
        }
        if (laThem && kho.timXeTheoBienSo(bienSo) != null) {
            UiHelper.loi(this, "Biển số \"" + bienSo + "\" đã tồn tại!", txtBienSo);
            return false;
        }
        return true;
    }
}
