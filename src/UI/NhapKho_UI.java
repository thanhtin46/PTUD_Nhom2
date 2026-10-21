package UI;

import Entity.ChiTietKho;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuKho;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
<<<<<<< HEAD

import DAO.Kho_DAO;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
=======
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
import java.util.ArrayList;

/**
 * Phiếu nhập / xuất kho.
<<<<<<< HEAD
 * - Phiếu NHẬP: ghi xuống DB qua Kho_DAO
 *     + "Nhập kho ngay"  -> themPhieuNhap (DA_NHAP, cộng tồn kho)
 *     + "Lưu chờ nhập"   -> luuPhieuChoNhap (CHO_NHAP, chưa cộng tồn kho)
 * - Phiếu XUẤT: vẫn dùng KhoDuLieu (bộ nhớ) vì DAO chưa có hàm xuất kho.
 */
public class NhapKho_UI extends JPanel {
    private static final DateTimeFormatter FMT_CHUAN = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_NHAP  = DateTimeFormatter.ofPattern("d/M/yyyy");

=======
 * UC010: với phiếu NHẬP, có thêm:
 *   - Nhà cung cấp, ngày dự kiến giao.
 *   - Nút "Lưu chờ nhập" để ghi nhận đơn đặt hàng, chưa cộng tồn kho.
 *   - Nút "Nhập ngay" để cộng tồn kho luôn.
 */
public class NhapKho_UI extends JPanel {
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
    private final boolean xuat;
    private final NhanVien nv;
    private final JTextField txtSoPhieu = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtGhiChu = new JTextField();
<<<<<<< HEAD
    private final JComboBox<String> cboNcc = new JComboBox<>();       // UC010
    private final JTextField txtNgayDuKien = new JTextField();        // UC010
=======
    private final JTextField txtNcc = new JTextField();           // UC010
    private final JTextField txtNgayDuKien = new JTextField();    // UC010
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
    private final JComboBox<String> cboPhuTung = new JComboBox<>();
    private final JTextField txtSoLuong = new JTextField("1");
    private final JTextField txtDonGia = new JTextField();
    private final JLabel lblTong = new JLabel("0");
    private DefaultTableModel modelDong;
    private final ArrayList<ChiTietKho> dongTam = new ArrayList<>();
    private final KhoDuLieu kho = KhoDuLieu.get();
<<<<<<< HEAD
    private final Kho_DAO nhapKhoDAO = new Kho_DAO();
    private ArrayList<PhuTung> dsPhuTung = new ArrayList<>();
    private ArrayList<String[]> dsNcc = new ArrayList<>();            // {maNCC, tenNCC}
=======
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22

    public NhapKho_UI(NhanVien nv) {
        this(nv, false);
    }

    public NhapKho_UI(NhanVien nv, boolean xuat) {
        this.nv = nv;
        this.xuat = xuat;
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang(xuat ? "📤 Phiếu xuất kho" : "📥 Phiếu nhập kho"), BorderLayout.NORTH);

        txtSoPhieu.setEditable(false);
<<<<<<< HEAD
        txtSoPhieu.setText(maPhieuMoi());
        txtNgay.setText(LocalDate.now().format(FMT_CHUAN));
        // Mặc định ngày dự kiến giao = +7 ngày cho phiếu nhập
        txtNgayDuKien.setText(LocalDate.now().plusDays(7).format(FMT_CHUAN));

        napComboNcc();
=======
        txtSoPhieu.setText(xuat ? kho.maPhieuXuatMoi() : kho.maPhieuNhapMoi());
        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        // Mặc định ngày dự kiến giao = +7 ngày cho phiếu nhập
        txtNgayDuKien.setText(LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
        napComboPt();
        cboPhuTung.addActionListener(e -> capNhatGia());
        capNhatGia();

        JPanel formPanel = UiHelper.panelForm("Thông tin phiếu", 480);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Số phiếu", txtSoPhieu);
        UiHelper.themDong(form, 1, "Ngày lập", txtNgay);
        if (!xuat) {
<<<<<<< HEAD
            UiHelper.themDong(form, 2, "Nhà cung cấp", cboNcc);
=======
            UiHelper.themDong(form, 2, "Nhà cung cấp", txtNcc);
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
            UiHelper.themDong(form, 3, "Ngày dự kiến giao", txtNgayDuKien);
            UiHelper.themDong(form, 4, "Ghi chú", txtGhiChu);
            UiHelper.themDong(form, 5, "Phụ tùng", cboPhuTung);
            UiHelper.themDong(form, 6, "Số lượng", txtSoLuong);
            UiHelper.themDong(form, 7, "Đơn giá", txtDonGia);
        } else {
            UiHelper.themDong(form, 2, "Ghi chú", txtGhiChu);
            UiHelper.themDong(form, 3, "Phụ tùng", cboPhuTung);
            UiHelper.themDong(form, 4, "Số lượng", txtSoLuong);
            UiHelper.themDong(form, 5, "Đơn giá", txtDonGia);
        }

        JPanel nut = new JPanel();
        nut.setOpaque(false);
        nut.setLayout(new BoxLayout(nut, BoxLayout.Y_AXIS));
        nut.setBorder(new EmptyBorder(10, 0, 0, 0));
        JButton bt1 = UiHelper.nut("Thêm dòng", UiIcon.of(UiIcon.PLUS, 16), new Color(46, 160, 92), true, e -> themDong());
        JButton bt2 = UiHelper.nut("Xóa dòng cuối", UiIcon.of(UiIcon.TRASH, 16), new Color(220, 80, 80), true, e -> xoaDong());
        nut.add(bt1);
        nut.add(Box.createVerticalStrut(8));
        nut.add(bt2);
        if (!xuat) {
            JButton btChoNhap = UiHelper.nut("Lưu chờ nhập (đặt NCC)", UiIcon.of(UiIcon.CLOCK, 16), new Color(225, 150, 45), true, e -> luuChoNhap());
            JButton btNhapNgay = UiHelper.nut("Nhập kho ngay", UiIcon.of(UiIcon.IMPORT, 16), new Color(37, 99, 168), true, e -> taoPhieu());
            nut.add(Box.createVerticalStrut(8));
            nut.add(btChoNhap);
            nut.add(Box.createVerticalStrut(8));
            nut.add(btNhapNgay);
        } else {
            JButton bt3 = UiHelper.nut("Tạo phiếu xuất kho", UiIcon.of(UiIcon.EXPORT, 16),
                    new Color(37, 99, 168), true, e -> taoPhieu());
            nut.add(Box.createVerticalStrut(8));
            nut.add(bt3);
        }
        for (Component c : nut.getComponents()) {
            if (c instanceof JButton b) {
                b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                b.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }

        UiHelper.ganNoiDungForm(formPanel, form, nut);

        JPanel bangPanel = UiHelper.panelBang("Chi tiết phiếu");
        modelDong = UiHelper.model("Mã PT", "Tên", "SL", "Đơn giá", "Thành tiền");
        JTable table = UiHelper.bang(modelDong);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Mã PT
        table.getColumnModel().getColumn(1).setPreferredWidth(250); // Tên
        table.getColumnModel().getColumn(2).setPreferredWidth(70);  // SL
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Đơn giá
<<<<<<< HEAD
        table.getColumnModel().getColumn(4).setPreferredWidth(130); // Thành tiền

=======
        table.getColumnModel().getColumn(4).setPreferredWidth(140); // Thành tiền
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
        UiHelper.canGiua(table, 0, 2);
        UiHelper.canTrai(table, 1);
        UiHelper.canPhai(table, 3, 4);
        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        JPanel tong = new JPanel(new BorderLayout());
        tong.setOpaque(false);
        tong.setBorder(new EmptyBorder(10, 4, 4, 4));
        lblTong.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTong.setForeground(new Color(220, 80, 80));
        lblTong.setHorizontalAlignment(SwingConstants.RIGHT);
        tong.add(lblTong, BorderLayout.CENTER);
        bangPanel.add(tong, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, bangPanel);
        split.setResizeWeight(0.32);
        split.setDividerLocation(480);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
    }

<<<<<<< HEAD
    // ====================================================================
    //  NẠP DỮ LIỆU TỪ DAO
    // ====================================================================

    private String maPhieuMoi() {
        return xuat ? kho.maPhieuXuatMoi() : nhapKhoDAO.sinhMaPhieuNhapMoi();
    }

    private void napComboNcc() {
        cboNcc.removeAllItems();
        dsNcc = nhapKhoDAO.layTatCaNhaCungCap();
        for (String[] n : dsNcc) {
            cboNcc.addItem(n[0] + " - " + n[1]);
        }
    }

    private void napComboPt() {
        cboPhuTung.removeAllItems();
        dsPhuTung = nhapKhoDAO.layTatCaPhuTung();
        for (PhuTung pt : dsPhuTung) {
            cboPhuTung.addItem(pt.getMaPT() + " - " + pt.getTen() + " - " + pt.getDonVi());
        }
    }

    /** Lấy phụ tùng theo vị trí trong combo (tránh split chuỗi). */
    private PhuTung ptDangChon() {
        int i = cboPhuTung.getSelectedIndex();
        if (i < 0 || i >= dsPhuTung.size()) return null;
        return dsPhuTung.get(i);
    }

    /** Mã NCC đang chọn, null nếu chưa chọn. */
    private String maNccDangChon() {
        int i = cboNcc.getSelectedIndex();
        if (i < 0 || i >= dsNcc.size()) return null;
        return dsNcc.get(i)[0];
=======
    private void napComboPt() {
        cboPhuTung.removeAllItems();
        for (PhuTung pt : kho.getPhuTungs()) {
            cboPhuTung.addItem(pt.getMaPT() + " - " + pt.getTen() + " (tồn " + pt.getSoLuongTon() + ")");
        }
    }

    private PhuTung ptDangChon() {
        Object sel = cboPhuTung.getSelectedItem();
        if (sel == null) return null;
        String ma = sel.toString().split(" - ")[0];
        return kho.timPhuTung(ma);
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
    }

    private void capNhatGia() {
        PhuTung pt = ptDangChon();
        if (pt != null) txtDonGia.setText(String.valueOf(pt.getDonGia()));
    }

<<<<<<< HEAD
    // ====================================================================
    //  DÒNG CHI TIẾT
    // ====================================================================

=======
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
    private void themDong() {
        PhuTung pt = ptDangChon();
        if (pt == null) {
            UiHelper.loi(this, "Chưa có phụ tùng!", cboPhuTung);
            return;
        }
<<<<<<< HEAD
        if (!txtSoLuong.getText().trim().matches("\\d{1,9}") || Integer.parseInt(txtSoLuong.getText().trim()) <= 0) {
=======
        if (!txtSoLuong.getText().trim().matches("\\d+") || Integer.parseInt(txtSoLuong.getText().trim()) <= 0) {
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
            UiHelper.loi(this, "Số lượng phải là số nguyên dương!", txtSoLuong);
            return;
        }
        int sl = Integer.parseInt(txtSoLuong.getText().trim());
        long gia;
        try {
            gia = Long.parseLong(txtDonGia.getText().trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            UiHelper.loi(this, "Đơn giá không hợp lệ!", txtDonGia);
            return;
        }
<<<<<<< HEAD
        if (gia < 0) {
            UiHelper.loi(this, "Đơn giá không được âm!", txtDonGia);
            return;
        }
        // Khóa chính (maPhieu, maPT): không cho trùng phụ tùng trong cùng phiếu
        for (ChiTietKho c : dongTam) {
            if (c.getMaPT().equals(pt.getMaPT())) {
                UiHelper.loi(this, "Phụ tùng đã có trong phiếu, hãy xóa dòng cũ trước!", cboPhuTung);
                return;
            }
        }
=======
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
        if (xuat && sl > pt.getSoLuongTon()) {
            UiHelper.loi(this, "Số lượng xuất không được lớn hơn tồn kho (" + pt.getSoLuongTon() + ")!", txtSoLuong);
            return;
        }
        ChiTietKho ct = new ChiTietKho(pt.getMaPT(), pt.getTen(), sl, gia);
        dongTam.add(ct);
        modelDong.addRow(new Object[]{ct.getMaPT(), ct.getTenPT(), ct.getSoLuong(),
                UiHelper.tien(ct.getDonGia()), UiHelper.tien(ct.getThanhTien())});
        capNhatTong();
    }

    private void xoaDong() {
        if (dongTam.isEmpty()) return;
        int i = dongTam.size() - 1;
        dongTam.remove(i);
        modelDong.removeRow(i);
        capNhatTong();
    }

    private void capNhatTong() {
        long tong = 0;
        for (ChiTietKho ct : dongTam) tong += ct.getThanhTien();
        lblTong.setText("Tổng tiền: " + UiHelper.tien(tong) + " VNĐ");
    }

<<<<<<< HEAD
    // ====================================================================
    //  LƯU PHIẾU
    // ====================================================================

    /** Parse ngày d/M/yyyy và chuẩn hóa về dd/MM/yyyy; null nếu sai. */
    private String chuanHoaNgay(String s) {
        try {
            return LocalDate.parse(s.trim(), FMT_NHAP).format(FMT_CHUAN);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Phiếu nhập: nhập kho ngay (DAO). Phiếu xuất: dùng KhoDuLieu. */
=======
    /** Tạo phiếu nhập kho: cộng tồn kho ngay. */
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
    private void taoPhieu() {
        if (dongTam.isEmpty()) {
            UiHelper.loi(this, "Phiếu chưa có dòng chi tiết!", txtGhiChu);
            return;
        }
<<<<<<< HEAD
        String ngay = chuanHoaNgay(txtNgay.getText());
        if (ngay == null) {
            UiHelper.loi(this, "Ngày lập phải có dạng dd/MM/yyyy!", txtNgay);
            return;
        }

        if (xuat) {
            PhieuKho phieu = new PhieuKho(txtSoPhieu.getText(), PhieuKho.XUAT,
                    ngay, nv.getHoTen(), txtGhiChu.getText().trim());
            phieu.getChiTiet().addAll(dongTam);
            if (!kho.apDungTonKho(phieu)) {
                UiHelper.loi(this, "Không đủ tồn kho để xuất!", txtSoLuong);
                return;
            }
            JOptionPane.showMessageDialog(this, "Đã tạo phiếu " + phieu.getSoPhieu()
                    + " — Tổng: " + UiHelper.tien(phieu.getTongTien()) + " VNĐ");
            resetSauKhiLuu();
            return;
        }

        String maNCC = maNccDangChon();
        if (maNCC == null) {
            UiHelper.loi(this, "Vui lòng chọn nhà cung cấp!", cboNcc);
            return;
        }
        long tong = tinhTong();
        boolean ok = nhapKhoDAO.themPhieuNhap(txtSoPhieu.getText(), maNCC, nv.getMaNV(),
                ngay, txtGhiChu.getText().trim(), dongTam);
        if (!ok) {
            UiHelper.loi(this, "Lưu phiếu nhập thất bại! Kiểm tra lại kết nối CSDL hoặc mã phiếu.", txtGhiChu);
            return;
        }
        JOptionPane.showMessageDialog(this, "Đã nhập kho phiếu " + txtSoPhieu.getText()
                + " — Tổng: " + UiHelper.tien(tong) + " VNĐ");
=======
        if (!xuat && txtNcc.getText().trim().isEmpty()) {
            UiHelper.loi(this, "Vui lòng nhập nhà cung cấp!", txtNcc);
            return;
        }
        PhieuKho phieu = new PhieuKho(txtSoPhieu.getText(),
                xuat ? PhieuKho.XUAT : PhieuKho.NHAP,
                txtNgay.getText().trim(),
                nv.getHoTen(),
                txtGhiChu.getText().trim());
        if (!xuat) {
            phieu.setNhaCungCap(txtNcc.getText().trim());
            phieu.setNgayDuKienGiao(txtNgayDuKien.getText().trim());
        }
        phieu.getChiTiet().addAll(dongTam);
        if (!kho.apDungTonKho(phieu)) {
            UiHelper.loi(this, "Không đủ tồn kho để xuất!", txtSoLuong);
            return;
        }
        JOptionPane.showMessageDialog(this, "Đã tạo phiếu " + phieu.getSoPhieu()
                + " — Tổng: " + UiHelper.tien(phieu.getTongTien()) + " VNĐ");
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
        resetSauKhiLuu();
    }

    /** UC010: lưu phiếu chờ nhập — ghi nhận đặt hàng NCC, chưa cộng tồn kho. */
    private void luuChoNhap() {
        if (xuat) return;     // chỉ áp dụng cho nhập
        if (dongTam.isEmpty()) {
            UiHelper.loi(this, "Phiếu chưa có dòng chi tiết!", txtGhiChu);
            return;
        }
<<<<<<< HEAD
        String maNCC = maNccDangChon();
        if (maNCC == null) {
            UiHelper.loi(this, "Vui lòng chọn nhà cung cấp!", cboNcc);
            return;
        }
        String ngay = chuanHoaNgay(txtNgay.getText());
        if (ngay == null) {
            UiHelper.loi(this, "Ngày lập phải có dạng dd/MM/yyyy!", txtNgay);
            return;
        }
        String ngayDuKien = chuanHoaNgay(txtNgayDuKien.getText());
        if (ngayDuKien == null) {
            UiHelper.loi(this, "Ngày dự kiến giao phải có dạng dd/MM/yyyy!", txtNgayDuKien);
            return;
        }
        if (LocalDate.parse(ngayDuKien, FMT_CHUAN).isBefore(LocalDate.parse(ngay, FMT_CHUAN))) {
            UiHelper.loi(this, "Ngày dự kiến giao không được trước ngày lập!", txtNgayDuKien);
            return;
        }

        boolean ok = nhapKhoDAO.luuPhieuChoNhap(txtSoPhieu.getText(), maNCC, nv.getMaNV(),
                ngay, ngayDuKien, txtGhiChu.getText().trim(), dongTam);
        if (!ok) {
            UiHelper.loi(this, "Lưu phiếu chờ nhập thất bại! Kiểm tra lại kết nối CSDL hoặc mã phiếu.", txtGhiChu);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Đã lưu phiếu chờ nhập " + txtSoPhieu.getText() + ".\n" +
=======
        if (txtNcc.getText().trim().isEmpty()) {
            UiHelper.loi(this, "Vui lòng nhập nhà cung cấp!", txtNcc);
            return;
        }
        if (!txtNgayDuKien.getText().trim().matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            UiHelper.loi(this, "Ngày dự kiến giao phải có dạng dd/MM/yyyy!", txtNgayDuKien);
            return;
        }
        PhieuKho phieu = new PhieuKho(txtSoPhieu.getText(),
                PhieuKho.NHAP,
                txtNgay.getText().trim(),
                nv.getHoTen(),
                txtGhiChu.getText().trim(),
                txtNcc.getText().trim(),
                txtNgayDuKien.getText().trim(),
                PhieuKho.CHO_NHAP);
        phieu.getChiTiet().addAll(dongTam);
        kho.luuPhieuChoNhap(phieu);
        JOptionPane.showMessageDialog(this,
                "Đã lưu phiếu chờ nhập " + phieu.getSoPhieu() + ".\n" +
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
                "Theo dõi trong menu 'Theo dõi chờ nhập' cho đến khi NCC giao hàng.",
                "Đã lưu đơn đặt hàng",
                JOptionPane.INFORMATION_MESSAGE);
        resetSauKhiLuu();
    }

<<<<<<< HEAD
    private long tinhTong() {
        long tong = 0;
        for (ChiTietKho ct : dongTam) tong += ct.getThanhTien();
        return tong;
    }

=======
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
    private void resetSauKhiLuu() {
        dongTam.clear();
        modelDong.setRowCount(0);
        txtGhiChu.setText("");
<<<<<<< HEAD
        txtSoPhieu.setText(maPhieuMoi());
        napComboPt();          // nạp lại để cập nhật tồn kho mới
        capNhatGia();
        capNhatTong();
    }
}
=======
        txtNcc.setText("");
        txtSoPhieu.setText(xuat ? kho.maPhieuXuatMoi() : kho.maPhieuNhapMoi());
        napComboPt();
        capNhatTong();
    }
}
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
