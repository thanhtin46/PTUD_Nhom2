package UI;

import DAO.PhieuTiepNhan_DAO;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LapPhieuTiepNhan_UI extends JPanel {
    private final NhanVien nv;
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final PhieuTiepNhan_DAO phieuTiepNhanDAO = new PhieuTiepNhan_DAO();

    private final JComboBox<String> cboXeCoSan = new JComboBox<>();
    private final JTextField txtSoPhieu = new JTextField();
    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtHang = new JTextField();
    private final JTextField txtLoai = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtYeuCau = new JTextField();

    private JTable table;
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
        txtSoPhieu.setEditable(false);
        txtSoPhieu.setText(kho.maPhieuTiepNhanMoi());

        // Lắng nghe sự kiện để tự động đồng bộ & tra cứu thông tin
        UiHelper.chiChoNhapSo(txtSdt, 10);
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

        UiHelper.themDong(form, 0, "Số phiếu tiếp nhận", txtSoPhieu, 3, 3);
        UiHelper.themDong(form, 1, "Chọn xe trong hệ thống", cboXeCoSan, 3, 3);
        UiHelper.themDong(form, 2, "Biển số xe *", txtBienSo, 3, 3);
        UiHelper.themDong(form, 3, "Hãng xe", txtHang, 3, 3);
        UiHelper.themDong(form, 4, "Dòng xe / Model", txtLoai, 3, 3);
        UiHelper.themDong(form, 5, "Số điện thoại chủ xe *", txtSdt, 3, 3);
        UiHelper.themDong(form, 6, "Họ tên chủ xe", txtHoTen, 3, 3);
        UiHelper.themDong(form, 7, "Ngày tiếp nhận", txtNgay, 3, 3);
        UiHelper.themDong(form, 8, "Yêu cầu của khách *", txtYeuCau, 3, 3);

        // Cụm 4 nút: Tạo mới, Sửa, Xóa, Làm mới
        JPanel nut = new JPanel(new GridLayout(1, 4, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton btnTao = UiHelper.nut("Tạo phiếu", UiIcon.of(UiIcon.PLUS, 16), UiHelper.XANH_LA, true, e -> tao());
        JButton btnSua = UiHelper.nut("Sửa phiếu", UiIcon.of(UiIcon.CHECK, 16), UiHelper.XANH_TRUNG, true, e -> sua());
        JButton btnXoa = UiHelper.nut("Xóa phiếu", UiIcon.of(UiIcon.CLOSE, 16), UiHelper.CHU_CHAY, true, e -> xoa());
        BaoVeQuyen.ganQuyen(btnXoa, Entity.QuyenTruyCap.Quyen.KH_XOA);
        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 16), UiHelper.XAM, false, e -> lamMoi());

        nut.add(btnTao);
        nut.add(btnSua);
        nut.add(btnXoa);
        nut.add(btnLamMoi);

        UiHelper.ganNoiDungForm(formPanel, form, nut);
        return formPanel;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu tiếp nhận chờ xử lý");
        model = UiHelper.model("Số phiếu", "SĐT", "Họ tên", "Biển số", "Hãng", "Dòng xe", "Ngày tiếp nhận", "Yêu cầu", "Tiến độ");
        table = UiHelper.bang(model);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(85);   // Số phiếu
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // SĐT
        table.getColumnModel().getColumn(2).setPreferredWidth(140);  // Họ tên
        table.getColumnModel().getColumn(3).setPreferredWidth(95);   // Biển số
        table.getColumnModel().getColumn(4).setPreferredWidth(85);   // Hãng
        table.getColumnModel().getColumn(5).setPreferredWidth(95);   // Dòng xe
        table.getColumnModel().getColumn(6).setPreferredWidth(115);   // Ngày tiếp nhận
        table.getColumnModel().getColumn(7).setPreferredWidth(180);  // Yêu cầu
        table.getColumnModel().getColumn(8).setPreferredWidth(110);  // Tiến độ

        UiHelper.canGiua(table, 0, 1, 3, 6, 8);
        UiHelper.canTrai(table, 2, 4, 5, 7);

        // Click vào dòng trên bảng để đổ dữ liệu lên form sửa/xóa
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienLenForm();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        return bangPanel;
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;

        isUpdating = true;
        try {
            txtSoPhieu.setText(model.getValueAt(dong, 0).toString());
            txtSdt.setText(model.getValueAt(dong, 1) != null ? model.getValueAt(dong, 1).toString() : "");
            txtHoTen.setText(model.getValueAt(dong, 2) != null ? model.getValueAt(dong, 2).toString() : "");
            String bienSo = model.getValueAt(dong, 3) != null ? model.getValueAt(dong, 3).toString() : "";
            txtBienSo.setText(bienSo);
            txtHang.setText(model.getValueAt(dong, 4) != null ? model.getValueAt(dong, 4).toString() : "");
            txtLoai.setText(model.getValueAt(dong, 5) != null ? model.getValueAt(dong, 5).toString() : "");
            txtNgay.setText(model.getValueAt(dong, 6) != null ? model.getValueAt(dong, 6).toString() : "");
            txtYeuCau.setText(model.getValueAt(dong, 7) != null ? model.getValueAt(dong, 7).toString() : "");

            dongBoComboXe(bienSo);
        } finally {
            isUpdating = false;
        }
    }

    private void napComboXeCoSan() {
        isUpdating = true;
        try {
            cboXeCoSan.removeAllItems();
            cboXeCoSan.addItem("-- Chọn xe đã đăng ký trong gara --");
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
        // Tải danh sách trực tiếp qua PhieuTiepNhan_DAO
        java.util.List<PhieuTiepNhan> ds = phieuTiepNhanDAO.layTatCaPhieuTiepNhan();
        if (ds.isEmpty()) {
            ds = kho.getPhieuTiepNhans();
        }
        for (PhieuTiepNhan p : ds) {
            model.addRow(new Object[]{
                    p.getSoPhieu(), p.getSdt(), p.getHoTen(), p.getBienSo(), p.getHangXe(),
                    p.getLoaiXe(), p.getNgayTiepNhanHienThi(), p.getYeuCau(), p.getTienDo()
            });
        }
    }

    private void lamMoi() {
        isUpdating = true;
        try {
            cboXeCoSan.setSelectedIndex(0);
            txtSoPhieu.setText(kho.maPhieuTiepNhanMoi());
            txtBienSo.setText("");
            txtHang.setText("");
            txtLoai.setText("");
            txtSdt.setText("");
            txtHoTen.setText("");
            txtYeuCau.setText("");
            txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            table.clearSelection();
            txtBienSo.requestFocus();
        } finally {
            isUpdating = false;
        }
    }

    private void tao() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.TIEP_NHAN)) return;
        String bienSo = txtBienSo.getText().trim();
        String sdt = txtSdt.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String hang = txtHang.getText().trim();
        String loai = txtLoai.getText().trim();
        String ngay = txtNgay.getText().trim();
        String yeuCau = txtYeuCau.getText().trim();

        LocalDate ngayTN = null;
        if (!ngay.isEmpty()) {
            ngayTN = UiHelper.chuyenChuoiSangNgay(ngay, this, "Ngày tiếp nhận");
            if (ngayTN == null) {
                txtNgay.requestFocus();
                return;
            }
        } else {
            ngayTN = LocalDate.now();
        }

        if (bienSo.isEmpty()) {
            UiHelper.loi(this, "Biển số xe không được để trống!", txtBienSo);
            return;
        }
        if (sdt.isEmpty()) {
            UiHelper.loi(this, "Số điện thoại chủ xe không được để trống!", txtSdt);
            return;
        }
        if (yeuCau.isEmpty()) {
            UiHelper.loi(this, "Vui lòng nhập yêu cầu / tình trạng sửa chữa của khách!", txtYeuCau);
            return;
        }

        // 1. Đồng bộ khách hàng
        KhachHang kh = kho.timKhachHangTheoSdt(sdt);
        String maKH;
        if (kh == null) {
            maKH = kho.maKhachHangMoi();
            String tenKH = hoTen.isEmpty() ? ("Khách hàng " + sdt) : hoTen;
            kh = new KhachHang(maKH, tenKH, sdt, "", "", "", "Khách tiếp nhận xe vào gara", ngayTN);
            kho.themKhachHang(kh);
        } else {
            maKH = kh.getMaKH();
            if (!hoTen.isEmpty() && !hoTen.equalsIgnoreCase(kh.getHoTen())) {
                kh.setHoTen(hoTen);
                kho.capNhatKhachHang(kh);
            }
        }

        // 2. Đồng bộ phương tiện
        PhuongTien xe = kho.timXeTheoBienSo(bienSo);
        if (xe == null) {
            String hangXe = hang.isEmpty() ? "Khác" : hang;
            String dongXe = loai.isEmpty() ? "Xe con" : loai;
            String vin = "VIN-" + bienSo.replaceAll("[^a-zA-Z0-9]", "");
            xe = new PhuongTien(bienSo, hangXe, dongXe, vin, "", "Mặc định",
                    String.valueOf(LocalDate.now().getYear()), "0", maKH, ngayTN);
            kho.themPhuongTien(xe);
        } else {
            xe.setNgayTiepNhanGanNhat(ngayTN);
            if (!hang.isEmpty()) xe.setHangXe(hang);
            if (!loai.isEmpty()) xe.setDongXe(loai);
            if ((xe.getMaChuXe() == null || xe.getMaChuXe().isEmpty()) && !maKH.isEmpty()) {
                xe.setMaChuXe(maKH);
            }
            kho.capNhatPhuongTien(xe);
        }

        // 3. Tạo phiếu tiếp nhận qua PhieuTiepNhan_DAO
        String maPhieu = txtSoPhieu.getText().trim();
        if (maPhieu.isEmpty()) maPhieu = kho.maPhieuTiepNhanMoi();

        LocalDateTime thoiGianTN = ngayTN.atTime(java.time.LocalTime.now());
        PhieuTiepNhan ptn = new PhieuTiepNhan(
                maPhieu, maKH, kh.getHoTen(), sdt,
                xe.getBienSo(), xe.getHangXe(), xe.getDongXe(),
                thoiGianTN, yeuCau, "Chờ tiếp nhận",
                nv != null ? nv.getHoTen() : "Tiếp tân", 0);

        boolean ok = phieuTiepNhanDAO.themPhieuTiepNhan(ptn, nv != null ? nv.getMaNV() : "NV01");
        kho.themPhieuTiepNhan(ptn, nv != null ? nv.getMaNV() : "NV01");

        napBang();
        napComboXeCoSan();
        dongBoComboXe(xe.getBienSo());
        lamMoi();

        JOptionPane.showMessageDialog(this,
                (ok ? "Đã tạo phiếu tiếp nhận và lưu CSDL SQL Server thành công!" : "Đã tạo phiếu tiếp nhận!") +
                        "\nPhương tiện [" + xe.getBienSo() + "] và Khách hàng [" + kh.getHoTen() + "] đã được đồng bộ vào hệ thống.",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void sua() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.TIEP_NHAN)) return;
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu tiếp nhận trong bảng để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String soPhieu = txtSoPhieu.getText().trim();
        if (soPhieu.isEmpty()) {
            soPhieu = model.getValueAt(dong, 0).toString();
        }

        String bienSo = txtBienSo.getText().trim();
        String sdt = txtSdt.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String hang = txtHang.getText().trim();
        String loai = txtLoai.getText().trim();
        String ngay = txtNgay.getText().trim();
        String yeuCau = txtYeuCau.getText().trim();

        LocalDate ngayTN = null;
        if (!ngay.isEmpty()) {
            ngayTN = UiHelper.chuyenChuoiSangNgay(ngay, this, "Ngày tiếp nhận");
            if (ngayTN == null) {
                txtNgay.requestFocus();
                return;
            }
        } else {
            ngayTN = LocalDate.now();
        }

        if (bienSo.isEmpty()) {
            UiHelper.loi(this, "Biển số xe không được để trống!", txtBienSo);
            return;
        }
        if (sdt.isEmpty()) {
            UiHelper.loi(this, "Số điện thoại chủ xe không được để trống!", txtSdt);
            return;
        }
        if (yeuCau.isEmpty()) {
            UiHelper.loi(this, "Vui lòng nhập yêu cầu của khách!", txtYeuCau);
            return;
        }

        // Cập nhật thông tin khách hàng nếu có sửa tên
        KhachHang kh = kho.timKhachHangTheoSdt(sdt);
        String maKH = (kh != null) ? kh.getMaKH() : "KH001";
        if (kh != null && !hoTen.isEmpty() && !hoTen.equalsIgnoreCase(kh.getHoTen())) {
            kh.setHoTen(hoTen);
            kho.capNhatKhachHang(kh);
        }

        // Cập nhật thông tin xe
        PhuongTien xe = kho.timXeTheoBienSo(bienSo);
        if (xe != null) {
            if (!hang.isEmpty()) xe.setHangXe(hang);
            if (!loai.isEmpty()) xe.setDongXe(loai);
            kho.capNhatPhuongTien(xe);
        }

        // Cập nhật phiếu tiếp nhận qua PhieuTiepNhan_DAO
        String tienDoHienTai = model.getValueAt(dong, 8) != null ? model.getValueAt(dong, 8).toString() : "Chờ tiếp nhận";
        PhieuTiepNhan ptn = new PhieuTiepNhan(
                soPhieu, maKH, hoTen, sdt, bienSo, hang, loai, ngay, yeuCau, tienDoHienTai,
                nv != null ? nv.getHoTen() : "Tiếp tân", 0
        );

        boolean ok = phieuTiepNhanDAO.capNhatPhieuTiepNhan(ptn);

        // Cập nhật trong cache
        PhieuTiepNhan pCache = kho.timPhieuTiepNhan(soPhieu);
        if (pCache != null) {
            // Cập nhật cache nếu cần
            napBang();
        } else {
            napBang();
        }

        JOptionPane.showMessageDialog(this, ok ? "Cập nhật phiếu tiếp nhận và lưu CSDL SQL Server thành công!" : "Cập nhật phiếu tiếp nhận thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xoa() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.KH_XOA)) return;
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu tiếp nhận trong bảng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String soPhieu = model.getValueAt(dong, 0).toString().trim();
        String bienSo = model.getValueAt(dong, 3).toString().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phiếu tiếp nhận [" + soPhieu + "] của xe [" + bienSo + "] không?\nThao tác này sẽ đồng thời dọn dẹp các dữ liệu liên quan.",
                "Xác nhận xóa phiếu tiếp nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Xóa an toàn qua PhieuTiepNhan_DAO
        boolean ok = kho.xoaPhieuTiepNhan(soPhieu);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Không thể xóa phiếu. Phiếu có thể đang có giao dịch liên quan.");
            return;
        }

        napBang();
        lamMoi();

        JOptionPane.showMessageDialog(this, ok ? "Đã xóa phiếu tiếp nhận [" + soPhieu + "] khỏi CSDL SQL Server!" : "Đã xóa phiếu tiếp nhận!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}
