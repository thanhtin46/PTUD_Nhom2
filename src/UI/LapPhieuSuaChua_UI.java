package UI;

import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

        root.add(UiHelper.tieuDeTrang("Lập phiếu sửa chữa / tiếp nhận"), BorderLayout.NORTH);

        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtSdt.addActionListener(e -> doSdt());
        txtBienSo.addActionListener(e -> doBienSo());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0);
        split.setDividerLocation(450);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin khách hàng & xe", 450);
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
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));
        JButton btn = UiHelper.nut("Tạo phiếu tiếp nhận", UiIcon.of(UiIcon.CHECK, 16), UiHelper.XANH_LA, true, e -> tao());
        nut.add(btn, BorderLayout.CENTER);

        UiHelper.ganNoiDungForm(formPanel, form, nut);
        return formPanel;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu sửa chữa");
        model = UiHelper.model("Số phiếu", "SĐT", "Họ tên", "Biển số", "Hãng", "Loại", "Ngày", "Yêu cầu", "Tiến độ", "Thành tiền");
        JTable table = UiHelper.bang(model);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);   // Số phiếu
        table.getColumnModel().getColumn(1).setPreferredWidth(110);  // SĐT
        table.getColumnModel().getColumn(2).setPreferredWidth(160);  // Họ tên
        table.getColumnModel().getColumn(3).setPreferredWidth(110);  // Biển số
        table.getColumnModel().getColumn(4).setPreferredWidth(90);   // Hãng
        table.getColumnModel().getColumn(5).setPreferredWidth(110);  // Loại
        table.getColumnModel().getColumn(6).setPreferredWidth(100);  // Ngày
        table.getColumnModel().getColumn(7).setPreferredWidth(200);  // Yêu cầu
        table.getColumnModel().getColumn(8).setPreferredWidth(120);  // Tiến độ
        table.getColumnModel().getColumn(9).setPreferredWidth(120);  // Thành tiền

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        return bangPanel;
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
