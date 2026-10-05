package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuBaoHanh;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LapPhieuBaoHanh_UI extends JPanel {
    private final NhanVien nv;
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final JTextField txtSoPhieu = new JTextField();
    private final JComboBox<String> cboBienSo = new JComboBox<>();
    private final JTextField txtNgayBd = new JTextField();
    private final JTextField txtNgayKt = new JTextField();
    private final JTextField txtNoiDung = new JTextField();
    private final JComboBox<String> cboTinhTrang = new JComboBox<>(new String[]{"Còn hạn", "Hết hạn", "Đang xử lý"});
    private DefaultTableModel model;

    public LapPhieuBaoHanh_UI(NhanVien nv) {
        this.nv = nv;
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("🛡 Lập phiếu bảo hành"), BorderLayout.NORTH);

        txtSoPhieu.setEditable(false);
        txtSoPhieu.setText(String.format("BH%03d", kho.getPhieuBaoHanhs().size() + 1));
        txtNgayBd.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtNgayKt.setText(LocalDate.now().plusMonths(6).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        for (PhuongTien xe : kho.getPhuongTiens()) cboBienSo.addItem(xe.getBienSo());

        JPanel formPanel = UiHelper.panelForm("Thông tin phiếu bảo hành", 400);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Số phiếu", txtSoPhieu);
        UiHelper.themDong(form, 1, "Biển số xe", cboBienSo);
        UiHelper.themDong(form, 2, "Ngày bắt đầu", txtNgayBd);
        UiHelper.themDong(form, 3, "Ngày kết thúc", txtNgayKt);
        UiHelper.themDong(form, 4, "Tình trạng", cboTinhTrang);
        UiHelper.themDong(form, 5, "Nội dung", txtNoiDung);

        JPanel nut = new JPanel(new BorderLayout());
        nut.setOpaque(false);
        nut.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JButton btn = UiHelper.nut("✓  Tạo phiếu bảo hành", new Color(46, 160, 92), true, e -> tao());
        nut.add(btn, BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(nut, BorderLayout.SOUTH);
        formPanel.add(body, BorderLayout.CENTER);

        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu bảo hành");
        model = UiHelper.model("Số phiếu", "Biển số", "Ngày BĐ", "Ngày KT", "Người lập", "Tình trạng", "Nội dung");
        JScrollPane sp = new JScrollPane(UiHelper.bang(model));
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, bangPanel);
        split.setResizeWeight(0);
        split.setDividerLocation(420);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private void napBang() {
        model.setRowCount(0);
        for (PhieuBaoHanh p : kho.getPhieuBaoHanhs()) {
            model.addRow(new Object[]{p.getSoPhieu(), p.getBienSo(), p.getNgayBatDau(), p.getNgayKetThuc(),
                    p.getNguoiLap(), p.getTinhTrang(), p.getNoiDung()});
        }
    }

    private void tao() {
        if (cboBienSo.getSelectedItem() == null) {
            UiHelper.loi(this, "Chưa có xe để lập bảo hành!", cboBienSo);
            return;
        }
        kho.getPhieuBaoHanhs().add(new PhieuBaoHanh(
                kho.maPhieuBaoHanhMoi(),
                cboBienSo.getSelectedItem().toString(),
                txtNgayBd.getText().trim(),
                txtNgayKt.getText().trim(),
                nv.getHoTen(),
                String.valueOf(cboTinhTrang.getSelectedItem()),
                txtNoiDung.getText().trim()));
        napBang();
        txtSoPhieu.setText(String.format("BH%03d", kho.getPhieuBaoHanhs().size() + 1));
        txtNoiDung.setText("");
        JOptionPane.showMessageDialog(this, "Đã tạo phiếu bảo hành!");
    }
}
