package UI;

import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LapPhieuSuaChua_UI extends JPanel {
    private final NhanVien nv;
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtHang = new JTextField();
    private final JTextField txtLoai = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtYeuCau = new JTextField();
    private final JTextField txtThanhTien = new JTextField("0");
    private DefaultTableModel model;

    public LapPhieuSuaChua_UI(NhanVien nv) {
        this.nv = nv;
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("🔧 Lập phiếu sửa chữa / tiếp nhận"), BorderLayout.NORTH);

        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtSdt.addActionListener(e -> doSdt());
        txtBienSo.addActionListener(e -> doBienSo());

        JPanel formPanel = UiHelper.panelForm("Thông tin khách hàng & xe", 420);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Số điện thoại *", txtSdt);
        UiHelper.themDong(form, 1, "Họ tên", txtHoTen);
        UiHelper.themDong(form, 2, "Biển số xe *", txtBienSo);
        UiHelper.themDong(form, 3, "Hãng xe", txtHang);
        UiHelper.themDong(form, 4, "Loại xe", txtLoai);
        UiHelper.themDong(form, 5, "Ngày tiếp nhận", txtNgay);
        UiHelper.themDong(form, 6, "Yêu cầu của khách", txtYeuCau);
        UiHelper.themDong(form, 7, "Thành tiền (VNĐ)", txtThanhTien);

        JPanel nut = new JPanel(new BorderLayout());
        nut.setOpaque(false);
        nut.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JButton btn = UiHelper.nut("✓  Tạo phiếu tiếp nhận", new Color(46, 160, 92), true, e -> tao());
        nut.add(btn, BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(nut, BorderLayout.SOUTH);
        formPanel.add(body, BorderLayout.CENTER);

        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu sửa chữa");
        model = UiHelper.model("Số phiếu", "SĐT", "Họ tên", "Biển số", "Hãng", "Loại", "Ngày", "Yêu cầu", "Tiến độ", "Thành tiền");
        JScrollPane sp = new JScrollPane(UiHelper.bang(model));
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, bangPanel);
        split.setResizeWeight(0);
        split.setDividerLocation(440);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private void doSdt() {
        KhachHang kh = kho.timKhachHangTheoSdt(txtSdt.getText().trim());
        if (kh == null) return;
        txtHoTen.setText(kh.getHoTen());
        for (PhuongTien xe : kho.getPhuongTiens()) {
            if (xe.getMaChuXe().equals(kh.getMaKH())) {
                txtBienSo.setText(xe.getBienSo());
                txtHang.setText(xe.getHangXe());
                txtLoai.setText(xe.getDongXe());
                break;
            }
        }
    }

    private void doBienSo() {
        PhuongTien xe = kho.timXeTheoBienSo(txtBienSo.getText().trim());
        if (xe == null) return;
        txtHang.setText(xe.getHangXe());
        txtLoai.setText(xe.getDongXe());
        KhachHang kh = kho.timKhachHangTheoMa(xe.getMaChuXe());
        if (kh != null) {
            txtSdt.setText(kh.getSdt());
            txtHoTen.setText(kh.getHoTen());
        }
    }

    private void napBang() {
        model.setRowCount(0);
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            model.addRow(new Object[]{p.getSoPhieu(), p.getSdt(), p.getHoTen(), p.getBienSo(), p.getHangXe(),
                    p.getLoaiXe(), p.getNgayTiepNhan(), p.getYeuCau(), p.getTienDo(), UiHelper.tien(p.getThanhTien())});
        }
    }

    private void tao() {
        if (txtSdt.getText().trim().isEmpty() || txtBienSo.getText().trim().isEmpty()) {
            UiHelper.loi(this, "Số điện thoại và biển số không được để trống!", txtSdt);
            return;
        }
        long tien = 0;
        String tienStr = txtThanhTien.getText().trim().replace(",", "");
        if (!tienStr.isEmpty()) {
            if (!tienStr.matches("\\d+")) {
                UiHelper.loi(this, "Thành tiền phải là số!", txtThanhTien);
                return;
            }
            tien = Long.parseLong(tienStr);
        }
        KhachHang kh = kho.timKhachHangTheoSdt(txtSdt.getText().trim());
        String maKH = kh != null ? kh.getMaKH() : "";
        kho.getPhieuTiepNhans().add(new PhieuTiepNhan(
                kho.maPhieuTiepNhanMoi(), maKH, txtHoTen.getText().trim(), txtSdt.getText().trim(),
                txtBienSo.getText().trim(), txtHang.getText().trim(), txtLoai.getText().trim(),
                txtNgay.getText().trim(), txtYeuCau.getText().trim(), "Chờ tiếp nhận",
                nv.getHoTen(), tien));
        PhuongTien xe = kho.timXeTheoBienSo(txtBienSo.getText().trim());
        if (xe != null) xe.setNgayTiepNhanGanNhat(txtNgay.getText().trim());
        napBang();
        txtYeuCau.setText("");
        txtThanhTien.setText("0");
        JOptionPane.showMessageDialog(this, "Đã tạo phiếu tiếp nhận / sửa chữa!");
    }
}
