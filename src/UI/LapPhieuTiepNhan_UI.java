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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LapPhieuTiepNhan_UI extends JPanel {
    private final NhanVien nv;
    private final KhoDuLieu kho = KhoDuLieu.get();

    private final JComboBox<String> cboXeCoSan = new JComboBox<>();
    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtHang = new JTextField();
    private final JTextField txtLoai = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtYeuCau = new JTextField();
    private final JTextField txtThanhTien = new JTextField("0");

    private DefaultTableModel model;
    private boolean isUpdating = false;

    public LapPhieuTiepNhan_UI(NhanVien nv) {
        this(nv, null);
    }

    public LapPhieuTiepNhan_UI(NhanVien nv, PhuongTien xeChon) {
        this.nv = nv;
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Lập phiếu tiếp nhận xe vào xưởng"), BorderLayout.NORTH);

        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Lắng nghe sự kiện để tự động đồng bộ & tra cứu thông tin
        txtSdt.addActionListener(e -> doSdt());
        txtSdt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                doSdt();
            }
        });

        txtBienSo.addActionListener(e -> doBienSo());
        txtBienSo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                doBienSo();
            }
        });

        cboXeCoSan.addActionListener(e -> {
            if (isUpdating) return;
            int idx = cboXeCoSan.getSelectedIndex();
            if (idx <= 0) return;
            String item = (String) cboXeCoSan.getSelectedItem();
            if (item != null) {
                int dashIdx = item.indexOf(" - ");
                String bs = dashIdx > 0 ? item.substring(0, dashIdx).trim() : item.trim();
                PhuongTien xe = kho.timXeTheoBienSo(bs);
                if (xe != null) {
                    napThongTinXe(xe);
                }
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0.32);
        split.setDividerLocation(490);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);

        napComboXeCoSan();
        napBang();

        if (xeChon != null) {
            napThongTinXe(xeChon);
        }
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin tiếp nhận xe & khách hàng", 470);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        UiHelper.themDong(form, 0, "Chọn xe trong hệ thống", cboXeCoSan, 3, 3);
        UiHelper.themDong(form, 1, "Biển số xe *", txtBienSo, 3, 3);
        UiHelper.themDong(form, 2, "Hãng xe", txtHang, 3, 3);
        UiHelper.themDong(form, 3, "Dòng xe / Model", txtLoai, 3, 3);
        UiHelper.themDong(form, 4, "Số điện thoại chủ xe *", txtSdt, 3, 3);
        UiHelper.themDong(form, 5, "Họ tên chủ xe", txtHoTen, 3, 3);
        UiHelper.themDong(form, 6, "Ngày tiếp nhận", txtNgay, 3, 3);
        UiHelper.themDong(form, 7, "Yêu cầu của khách", txtYeuCau, 3, 3);
        UiHelper.themDong(form, 8, "Tạm tính dịch vụ (VNĐ)", txtThanhTien, 3, 3);

        JPanel nut = new JPanel(new GridLayout(1, 2, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btnTao = UiHelper.nut("Tạo phiếu tiếp nhận", UiIcon.of(UiIcon.CHECK, 16), UiHelper.XANH_LA, true, e -> tao());
        JButton btnLamMoi = UiHelper.nut("Làm mới form", UiIcon.of(UiIcon.REFRESH, 16), UiHelper.XAM, false, e -> lamMoi());

        nut.add(btnTao);
        nut.add(btnLamMoi);

        UiHelper.ganNoiDungForm(formPanel, form, nut);
        return formPanel;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu tiếp nhận chờ xử lý");
        model = UiHelper.model("Số phiếu", "SĐT", "Họ tên", "Biển số", "Hãng", "Loại", "Ngày", "Yêu cầu", "Tiến độ", "Tạm tính");
        JTable table = UiHelper.bang(model);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(85);   // Số phiếu
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // SĐT
        table.getColumnModel().getColumn(2).setPreferredWidth(140);  // Họ tên
        table.getColumnModel().getColumn(3).setPreferredWidth(95);   // Biển số
        table.getColumnModel().getColumn(4).setPreferredWidth(85);   // Hãng
        table.getColumnModel().getColumn(5).setPreferredWidth(95);   // Loại
        table.getColumnModel().getColumn(6).setPreferredWidth(90);   // Ngày
        table.getColumnModel().getColumn(7).setPreferredWidth(160);  // Yêu cầu
        table.getColumnModel().getColumn(8).setPreferredWidth(110);  // Tiến độ
        table.getColumnModel().getColumn(9).setPreferredWidth(100);  // Tạm tính

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        return bangPanel;
    }

    private void napComboXeCoSan() {
        isUpdating = true;
        try {
            cboXeCoSan.removeAllItems();
            cboXeCoSan.addItem("-- Chọn xe đã đăng ký trong gara (hoặc nhập bên dưới) --");
            for (PhuongTien xe : kho.getPhuongTiens()) {
                KhachHang chu = kho.timKhachHangTheoMa(xe.getMaChuXe());
                String tenChu = chu != null ? chu.getHoTen() : "Chưa có chủ";
                String sdt = chu != null ? chu.getSdt() : "";
                cboXeCoSan.addItem(xe.getBienSo() + " - " + xe.getHangXe() + " " + xe.getDongXe() + " (" + tenChu + " - " + sdt + ")");
            }
        } finally {
            isUpdating = false;
        }
    }

    public void napThongTinXe(PhuongTien xe) {
        if (xe == null) return;
        isUpdating = true;
        try {
            txtBienSo.setText(xe.getBienSo());
            txtHang.setText(xe.getHangXe() != null ? xe.getHangXe() : "");
            txtLoai.setText(xe.getDongXe() != null ? xe.getDongXe() : "");

            KhachHang kh = kho.timKhachHangTheoMa(xe.getMaChuXe());
            if (kh != null) {
                txtSdt.setText(kh.getSdt() != null ? kh.getSdt() : "");
                txtHoTen.setText(kh.getHoTen() != null ? kh.getHoTen() : "");
            }

            dongBoComboXe(xe.getBienSo());
        } finally {
            isUpdating = false;
        }
    }

    private void dongBoComboXe(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) return;
        String bsChuan = bienSo.trim();
        for (int i = 1; i < cboXeCoSan.getItemCount(); i++) {
            String item = cboXeCoSan.getItemAt(i);
            if (item != null && item.startsWith(bsChuan + " ")) {
                cboXeCoSan.setSelectedIndex(i);
                return;
            }
        }
    }

    private void doSdt() {
        if (isUpdating) return;
        String sdt = txtSdt.getText().trim();
        if (sdt.isEmpty()) return;

        KhachHang kh = kho.timKhachHangTheoSdt(sdt);
        if (kh == null) return;

        isUpdating = true;
        try {
            txtHoTen.setText(kh.getHoTen());

            // Nếu ô biển số đang trống, tìm xe sở hữu đầu tiên của khách này để điền tự động
            if (txtBienSo.getText().trim().isEmpty()) {
                for (PhuongTien xe : kho.getPhuongTiens()) {
                    if (xe.getMaChuXe() != null && xe.getMaChuXe().equalsIgnoreCase(kh.getMaKH())) {
                        txtBienSo.setText(xe.getBienSo());
                        txtHang.setText(xe.getHangXe());
                        txtLoai.setText(xe.getDongXe());
                        dongBoComboXe(xe.getBienSo());
                        break;
                    }
                }
            }
        } finally {
            isUpdating = false;
        }
    }

    private void doBienSo() {
        if (isUpdating) return;
        String bs = txtBienSo.getText().trim();
        if (bs.isEmpty()) return;

        PhuongTien xe = kho.timXeTheoBienSo(bs);
        if (xe == null) return;

        isUpdating = true;
        try {
            txtBienSo.setText(xe.getBienSo());
            txtHang.setText(xe.getHangXe() != null ? xe.getHangXe() : "");
            txtLoai.setText(xe.getDongXe() != null ? xe.getDongXe() : "");

            KhachHang kh = kho.timKhachHangTheoMa(xe.getMaChuXe());
            if (kh != null) {
                txtSdt.setText(kh.getSdt() != null ? kh.getSdt() : "");
                txtHoTen.setText(kh.getHoTen() != null ? kh.getHoTen() : "");
            }

            dongBoComboXe(xe.getBienSo());
        } finally {
            isUpdating = false;
        }
    }

    private void napBang() {
        model.setRowCount(0);
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            model.addRow(new Object[]{p.getSoPhieu(), p.getSdt(), p.getHoTen(), p.getBienSo(), p.getHangXe(),
                    p.getLoaiXe(), p.getNgayTiepNhan(), p.getYeuCau(), p.getTienDo(), UiHelper.tien(p.getThanhTien())});
        }
    }

    private void lamMoi() {
        isUpdating = true;
        try {
            cboXeCoSan.setSelectedIndex(0);
            txtBienSo.setText("");
            txtHang.setText("");
            txtLoai.setText("");
            txtSdt.setText("");
            txtHoTen.setText("");
            txtYeuCau.setText("");
            txtThanhTien.setText("0");
            txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            txtBienSo.requestFocus();
        } finally {
            isUpdating = false;
        }
    }

    private void tao() {
        String bienSo = txtBienSo.getText().trim();
        String sdt = txtSdt.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String hang = txtHang.getText().trim();
        String loai = txtLoai.getText().trim();
        String ngay = txtNgay.getText().trim();
        String yeuCau = txtYeuCau.getText().trim();

        if (bienSo.isEmpty()) {
            UiHelper.loi(this, "Biển số xe không được để trống!", txtBienSo);
            return;
        }
        if (sdt.isEmpty()) {
            UiHelper.loi(this, "Số điện thoại chủ xe không được để trống!", txtSdt);
            return;
        }

        long tien = 0;
        String tienStr = txtThanhTien.getText().trim().replace(",", "");
        if (!tienStr.isEmpty()) {
            if (!tienStr.matches("\\d+")) {
                UiHelper.loi(this, "Tạm tính dịch vụ phải là số!", txtThanhTien);
                return;
            }
            tien = Long.parseLong(tienStr);
        }

        // 1. Đồng bộ khách hàng: Nếu chưa có trong hệ thống thì tự động lưu khách hàng mới
        KhachHang kh = kho.timKhachHangTheoSdt(sdt);
        String maKH;
        if (kh == null) {
            maKH = kho.maKhachHangMoi();
            String tenKH = hoTen.isEmpty() ? ("Khách hàng " + sdt) : hoTen;
            kh = new KhachHang(maKH, tenKH, sdt, "", "", "", "Khách tiếp nhận xe vào gara", ngay);
            kho.themKhachHang(kh);
        } else {
            maKH = kh.getMaKH();
            if (!hoTen.isEmpty() && !hoTen.equalsIgnoreCase(kh.getHoTen())) {
                kh.setHoTen(hoTen);
                kho.capNhatKhachHang(kh);
            }
        }

        // 2. Đồng bộ phương tiện: Nếu chưa có xe trong Quản lý phương tiện thì tự động thêm xe mới
        PhuongTien xe = kho.timXeTheoBienSo(bienSo);
        if (xe == null) {
            String hangXe = hang.isEmpty() ? "Khác" : hang;
            String dongXe = loai.isEmpty() ? "Xe con" : loai;
            String vin = "VIN-" + bienSo.replaceAll("[^a-zA-Z0-9]", "");
            xe = new PhuongTien(bienSo, hangXe, dongXe, vin, "", "Mặc định",
                    String.valueOf(LocalDate.now().getYear()), "0", maKH, ngay);
            kho.themPhuongTien(xe);
        } else {
            xe.setNgayTiepNhanGanNhat(ngay);
            if (!hang.isEmpty()) xe.setHangXe(hang);
            if (!loai.isEmpty()) xe.setDongXe(loai);
            if ((xe.getMaChuXe() == null || xe.getMaChuXe().isEmpty()) && !maKH.isEmpty()) {
                xe.setMaChuXe(maKH);
            }
            kho.capNhatPhuongTien(xe);
        }

        // 3. Tạo phiếu tiếp nhận
        PhieuTiepNhan ptn = new PhieuTiepNhan(
                kho.maPhieuTiepNhanMoi(), maKH, kh.getHoTen(), sdt,
                xe.getBienSo(), xe.getHangXe(), xe.getDongXe(),
                ngay, yeuCau, "Chờ tiếp nhận",
                nv != null ? nv.getHoTen() : "Tiếp tân", tien);

        boolean ok = kho.themPhieuTiepNhan(ptn, nv != null ? nv.getMaNV() : "NV01");

        napBang();
        napComboXeCoSan();
        dongBoComboXe(xe.getBienSo());
        txtYeuCau.setText("");
        txtThanhTien.setText("0");

        JOptionPane.showMessageDialog(this,
                (ok ? "Đã tạo phiếu tiếp nhận và lưu CSDL thành công!" : "Đã tạo phiếu tiếp nhận!") +
                        "\nPhương tiện [" + xe.getBienSo() + "] và Khách hàng [" + kh.getHoTen() + "] đã được đồng bộ vào hệ thống.",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }
}
