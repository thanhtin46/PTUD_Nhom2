package UI;

import Entity.ChiTietKho;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuKho;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class NhapKho_UI extends JPanel {

    private final boolean xuat;
    private final NhanVien nv;

    private final JTextField txtSoPhieu = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtGhiChu = new JTextField();
    private final JComboBox<String> cboNcc = new JComboBox<>();
    private final JTextField txtNgayDuKien = new JTextField();
    private final JComboBox<String> cboPhuTung = new JComboBox<>();
    private final JTextField txtSoLuong = new JTextField("1");
    private final JTextField txtDonGia = new JTextField();

    private final JLabel lblTong = new JLabel("0");

    private DefaultTableModel modelDong;

    private final ArrayList<ChiTietKho> dongTam = new ArrayList<>();

    private final KhoDuLieu kho = KhoDuLieu.get();

    public NhapKho_UI(NhanVien nv) {
        this(nv, false);
    }

    public NhapKho_UI(NhanVien nv, boolean xuat) {

        this.nv = nv;
        this.xuat = xuat;

        // =========================
        // KHỞI TẠO GIAO DIỆN
        // =========================

        JPanel root = UiHelper.nen();

        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        add(root, BorderLayout.CENTER);

        root.add(
                UiHelper.tieuDeTrang(
                        xuat ? "📤 Phiếu xuất kho" : "📥 Phiếu nhập kho"
                ),
                BorderLayout.NORTH
        );

        txtDonGia.setEditable(false);
        txtSoPhieu.setEditable(false);
        cboNcc.setEditable(false);
        txtSoPhieu.setText(
                xuat ? kho.maPhieuXuatMoi() : kho.maPhieuNhapMoi()
        );

        txtNgay.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        txtNgayDuKien.setText(
                LocalDate.now().plusDays(7)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        // =========================
        // NẠP COMBOBOX
        // =========================

        napComboPt();

        if (!xuat) {
            napComboNcc();
        }

        // =========================
        // SỰ KIỆN CHỌN PHỤ TÙNG
        // =========================

        cboPhuTung.addActionListener(e -> {
            capNhatGia();

            if (!xuat) {
                capNhatNhaCungCap();
            }
        });

        capNhatGia();

        if (!xuat) {
            capNhatNhaCungCap();
        }

        // =========================
        // FORM THÔNG TIN PHIẾU
        // =========================

        JPanel formPanel = UiHelper.panelForm("Thông tin phiếu", 480);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        UiHelper.themDong(form, 0, "Số phiếu", txtSoPhieu);
        UiHelper.themDong(form, 1, "Ngày lập", txtNgay);

        if (!xuat) {

            UiHelper.themDong(form, 2, "Nhà cung cấp", cboNcc);
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

        // =========================
        // CÁC NÚT CHỨC NĂNG
        // =========================

        JPanel nut = new JPanel();

        nut.setOpaque(false);
        nut.setLayout(new BoxLayout(nut, BoxLayout.Y_AXIS));
        nut.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btThem = UiHelper.nut(
                "Thêm dòng",
                UiIcon.of(UiIcon.PLUS, 16),
                new Color(46, 160, 92),
                true,
                e -> themDong()
        );

        JButton btXoa = UiHelper.nut(
                "Xóa dòng cuối",
                UiIcon.of(UiIcon.TRASH, 16),
                new Color(220, 80, 80),
                true,
                e -> xoaDong()
        );

        nut.add(btThem);
        nut.add(Box.createVerticalStrut(8));
        nut.add(btXoa);

        if (!xuat) {

            JButton btChoNhap = UiHelper.nut(
                    "Lưu chờ nhập (đặt NCC)",
                    UiIcon.of(UiIcon.CLOCK, 16),
                    new Color(225, 150, 45),
                    true,
                    e -> luuChoNhap()
            );

            JButton btNhapNgay = UiHelper.nut(
                    "Nhập kho ngay",
                    UiIcon.of(UiIcon.IMPORT, 16),
                    new Color(37, 99, 168),
                    true,
                    e -> taoPhieu()
            );

            nut.add(Box.createVerticalStrut(8));
            nut.add(btChoNhap);

            nut.add(Box.createVerticalStrut(8));
            nut.add(btNhapNgay);

        } else {

            JButton btXuat = UiHelper.nut(
                    "Tạo phiếu xuất kho",
                    UiIcon.of(UiIcon.EXPORT, 16),
                    new Color(37, 99, 168),
                    true,
                    e -> taoPhieu()
            );

            nut.add(Box.createVerticalStrut(8));
            nut.add(btXuat);
        }

        for (Component c : nut.getComponents()) {
            if (c instanceof JButton b) {
                b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                b.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }

        UiHelper.ganNoiDungForm(formPanel, form, nut);

        // =========================
        // BẢNG CHI TIẾT PHIẾU
        // =========================

        JPanel bangPanel = UiHelper.panelBang("Chi tiết phiếu");

        modelDong = UiHelper.model(
                "Mã PT",
                "Tên",
                "SL",
                "Đơn giá",
                "Thành tiền"
        );

        JTable table = UiHelper.bang(modelDong);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);

        UiHelper.canGiua(table, 0, 2);
        UiHelper.canTrai(table, 1);
        UiHelper.canPhai(table, 3, 4);

        JScrollPane sp = new JScrollPane(table);

        sp.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

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

        // =========================
        // CHIA GIAO DIỆN
        // =========================

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                formPanel,
                bangPanel
        );

        split.setResizeWeight(0.32);
        split.setDividerLocation(480);
        split.setBorder(null);
        split.setOpaque(false);

        root.add(split, BorderLayout.CENTER);
    }

    // =====================================================
    // 1. NẠP DANH SÁCH PHỤ TÙNG
    // =====================================================

    private void napComboPt() {
        cboPhuTung.removeAllItems();
        java.util.List<PhuTung> list = new java.util.ArrayList<>();
        for (PhuTung pt : kho.getPhuTungs()) {
            if (pt.getMaPT() != null && pt.getMaPT().startsWith("DV")) continue;
            list.add(pt);
        }
        list.sort((a, b) -> BaoCaoTonKho_UI.soSanhMa(a.getMaPT(), b.getMaPT()));

        java.util.List<String> ds = new java.util.ArrayList<>();
        for (PhuTung pt : list) {
            String item = pt.getMaPT() + " - " + pt.getTen() + " (tồn " + pt.getSoLuongTon() + ")";
            cboPhuTung.addItem(item);
            ds.add(item);
        }
        UiHelper.ganLocTimKiemChoComboBox(cboPhuTung, ds, () -> {
            capNhatGia();
            if (!xuat) {
                capNhatNhaCungCap();
            }
        });
    }

    // =====================================================
    // 2. NẠP DANH SÁCH NHÀ CUNG CẤP
    // =====================================================

    private void napComboNcc() {
        cboNcc.removeAllItems();
        java.util.List<String> ds = new java.util.ArrayList<>(kho.layDanhSachNhaCungCap());
        for (String ncc : ds) {
            cboNcc.addItem(ncc);
        }
        UiHelper.ganLocTimKiemChoComboBox(cboNcc, ds, null);
    }

    // =====================================================
    // 3. LẤY NHÀ CUNG CẤP ĐANG CHỌN
    // =====================================================

    private String layNhaCungCap() {

        Object sel = cboNcc.getSelectedItem();

        return sel != null ? sel.toString().trim() : "";
    }

    // =====================================================
    // 4. LẤY PHỤ TÙNG ĐANG CHỌN
    // =====================================================

    private PhuTung ptDangChon() {

        Object sel = cboPhuTung.getSelectedItem();

        if (sel == null) {
            return null;
        }

        String s = sel.toString().trim();

        if (s.isEmpty()) {
            return null;
        }

        String ma = s.split(" - ")[0].trim();

        return kho.timPhuTung(ma);
    }

    // =====================================================
    // 5. CẬP NHẬT ĐƠN GIÁ
    // =====================================================

    private void capNhatGia() {

        PhuTung pt = ptDangChon();

        if (pt != null) {
            txtDonGia.setText(String.valueOf(pt.getDonGia()));
        } else {
            txtDonGia.setText("");
        }
    }

    // =====================================================
    // 6. TỰ ĐỘNG CẬP NHẬT NHÀ CUNG CẤP THEO PHỤ TÙNG
    // =====================================================

    private void capNhatNhaCungCap() {

        PhuTung pt = ptDangChon();

        if (pt == null) {
            cboNcc.setSelectedItem(null);
            return;
        }

        String maNCC = pt.getMaNCC();

        if (maNCC == null || maNCC.trim().isEmpty()) {
            cboNcc.setSelectedItem(null);
            return;
        }

        maNCC = maNCC.trim();

        // Tìm nhà cung cấp theo mã NCC trong ComboBox
        for (int i = 0; i < cboNcc.getItemCount(); i++) {

            String item = cboNcc.getItemAt(i);

            if (item == null) {
                continue;
            }

            item = item.trim();

            if (item.equalsIgnoreCase(maNCC)
                    || item.startsWith(maNCC + " - ")) {

                cboNcc.setSelectedIndex(i);
                return;
            }
        }

        // Nếu không tìm thấy trong danh sách,
        // hiển thị mã nhà cung cấp để người dùng nhận biết.
        cboNcc.setSelectedItem(maNCC);
    }

    // =====================================================
    // 7. THÊM DÒNG CHI TIẾT
    // =====================================================

    private void themDong() {

        PhuTung pt = ptDangChon();

        if (pt == null || kho.timPhuTung(pt.getMaPT()) == null) {

            UiHelper.loi(
                    this,
                    "Ngoại lệ: Mã phụ tùng không tồn tại trong hệ thống!",
                    cboPhuTung
            );

            return;
        }

        // Kiểm tra số lượng
        String slStr = txtSoLuong.getText().trim();

        if (!slStr.matches("\\d+")) {

            UiHelper.loi(
                    this,
                    "Số lượng không hợp lệ! Vui lòng nhập số nguyên dương.",
                    txtSoLuong
            );

            return;
        }

        int sl;

        try {
            sl = Integer.parseInt(slStr);
        } catch (NumberFormatException ex) {

            UiHelper.loi(
                    this,
                    "Số lượng vượt quá giới hạn cho phép!",
                    txtSoLuong
            );

            return;
        }

        if (sl <= 0) {

            UiHelper.loi(
                    this,
                    "Số lượng phải lớn hơn 0!",
                    txtSoLuong
            );

            return;
        }

        // Kiểm tra đơn giá
        long gia;

        try {

            gia = Long.parseLong(
                    txtDonGia.getText().trim().replace(",", "")
            );

            if (gia < 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {

            UiHelper.loi(
                    this,
                    "Đơn giá không hợp lệ!",
                    txtDonGia
            );

            return;
        }

        // Kiểm tra tồn kho khi xuất
        if (xuat && sl > pt.getSoLuongTon()) {

            UiHelper.loi(
                    this,
                    "Số lượng xuất không được lớn hơn tồn kho ("
                            + pt.getSoLuongTon() + ")!",
                    txtSoLuong
            );

            return;
        }

        // Kiểm tra phụ tùng đã có trong danh sách chưa
        ChiTietKho daCo = null;
        int rowIdx = -1;

        for (int i = 0; i < dongTam.size(); i++) {

            if (dongTam.get(i).getMaPT()
                    .equalsIgnoreCase(pt.getMaPT())) {

                daCo = dongTam.get(i);
                rowIdx = i;
                break;
            }
        }

        if (daCo != null) {

            int slMoi = daCo.getSoLuong() + sl;

            if (xuat && slMoi > pt.getSoLuongTon()) {

                UiHelper.loi(
                        this,
                        "Tổng số lượng xuất vượt quá tồn kho ("
                                + pt.getSoLuongTon() + ")!",
                        txtSoLuong
                );

                return;
            }

            daCo.setSoLuong(slMoi);
            daCo.setDonGia(gia);

            modelDong.setValueAt(slMoi, rowIdx, 2);

            modelDong.setValueAt(
                    UiHelper.tien(gia),
                    rowIdx,
                    3
            );

            modelDong.setValueAt(
                    UiHelper.tien(daCo.getThanhTien()),
                    rowIdx,
                    4
            );

        } else {

            ChiTietKho ct = new ChiTietKho(
                    pt.getMaPT(),
                    pt.getTen(),
                    sl,
                    gia
            );

            dongTam.add(ct);

            modelDong.addRow(new Object[]{
                    ct.getMaPT(),
                    ct.getTenPT(),
                    ct.getSoLuong(),
                    UiHelper.tien(ct.getDonGia()),
                    UiHelper.tien(ct.getThanhTien())
            });
        }

        capNhatTong();
    }

    // =====================================================
    // 8. XÓA DÒNG CUỐI
    // =====================================================

    private void xoaDong() {

        if (dongTam.isEmpty()) {
            return;
        }

        int i = dongTam.size() - 1;

        dongTam.remove(i);
        modelDong.removeRow(i);

        capNhatTong();
    }

    // =====================================================
    // 9. CẬP NHẬT TỔNG TIỀN
    // =====================================================

    private void capNhatTong() {

        long tong = 0;

        for (ChiTietKho ct : dongTam) {
            tong += ct.getThanhTien();
        }

        lblTong.setText(
                "Tổng tiền: " + UiHelper.tien(tong) + " VNĐ"
        );
    }

    // =====================================================
    // 10. PHÂN NHÁNH TẠO PHIẾU
    // =====================================================

    private void taoPhieu() {

        if (xuat) {
            taoPhieuXuatKho();
        } else {
            nhapKhoNgay();
        }
    }

    // =====================================================
    // 11. NHẬP KHO NGAY
    // =====================================================

    private void nhapKhoNgay() {

        // Kiểm tra phiếu rỗng
        if (dongTam.isEmpty()) {

            UiHelper.loi(
                    this,
                    "Phiếu rỗng! Vui lòng thêm ít nhất một phụ tùng.",
                    cboPhuTung
            );

            return;
        }

        // Kiểm tra nhà cung cấp
        String ncc = layNhaCungCap();

        if (ncc.isEmpty()) {

            UiHelper.loi(
                    this,
                    "Nhà cung cấp không hợp lệ!",
                    cboNcc
            );

            return;
        }

        // Kiểm tra ngày lập
        LocalDate ldNgay = UiHelper.chuyenChuoiSangNgay(
                txtNgay.getText().trim(),
                this,
                "Ngày lập phiếu"
        );

        if (ldNgay == null) {
            txtNgay.requestFocus();
            return;
        }

        // Kiểm tra ngày dự kiến giao
        LocalDate ldGiao = UiHelper.chuyenChuoiSangNgay(
                txtNgayDuKien.getText().trim(),
                this,
                "Ngày dự kiến giao"
        );

        if (ldGiao == null) {
            txtNgayDuKien.requestFocus();
            return;
        }

        // Kiểm tra phụ tùng và số lượng
        for (ChiTietKho ct : dongTam) {

            PhuTung pt = kho.timPhuTung(ct.getMaPT());

            if (pt == null) {

                UiHelper.loi(
                        this,
                        "Mã phụ tùng " + ct.getMaPT()
                                + " không tồn tại!",
                        cboPhuTung
                );

                return;
            }

            if (ct.getSoLuong() <= 0) {

                UiHelper.loi(
                        this,
                        "Số lượng phụ tùng không hợp lệ!",
                        txtSoLuong
                );

                return;
            }
        }

        String maNV = (
                nv != null
                        && nv.getMaNV() != null
                        && !nv.getMaNV().isEmpty()
        ) ? nv.getMaNV() : "NV04";

        String tenNV = nv != null
                ? nv.getHoTen()
                : "Nhân viên kho";

        // Tạo phiếu nhập
        PhieuKho phieu = new PhieuKho(
                txtSoPhieu.getText().trim(),
                PhieuKho.NHAP,
                ldNgay,
                tenNV,
                txtGhiChu.getText().trim(),
                ncc,
                ldGiao,
                PhieuKho.DA_NHAP
        );

        phieu.getChiTiet().addAll(dongTam);

        try {

            // Lưu phiếu và cập nhật tồn kho bằng transaction
            kho.nhapKhoNgay(phieu, maNV);

            JOptionPane.showMessageDialog(
                    this,
                    "Hoàn tất nhập kho thành công!\n"
                            + "Số phiếu: " + phieu.getSoPhieu() + "\n"
                            + "Nhà cung cấp: " + ncc + "\n"
                            + "Tổng tiền: "
                            + UiHelper.tien(phieu.getTongTien()) + " VNĐ\n"
                            + "Trạng thái: "
                            + phieu.getTrangThaiHienThi() + "\n"
                            + "Tồn kho đã được cập nhật.",
                    "Hoàn tất nhập kho",
                    JOptionPane.INFORMATION_MESSAGE
            );

            resetSauKhiLuu();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi CSDL trong quá trình nhập kho!\n"
                            + "Hệ thống đã thực hiện ROLLBACK.\n"
                            + "Chi tiết lỗi: " + ex.getMessage(),
                    "Lỗi nhập kho",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // 12. TẠO PHIẾU XUẤT KHO
    // =====================================================

    private void taoPhieuXuatKho() {

        if (dongTam.isEmpty()) {

            UiHelper.loi(
                    this,
                    "Phiếu chưa có dòng chi tiết!",
                    txtGhiChu
            );

            return;
        }

        LocalDate ngay = UiHelper.chuyenChuoiSangNgay(
                txtNgay.getText().trim(),
                this,
                "Ngày lập phiếu"
        );

        if (ngay == null) {
            txtNgay.requestFocus();
            return;
        }

        PhieuKho phieu = new PhieuKho(
                txtSoPhieu.getText().trim(),
                PhieuKho.XUAT,
                txtNgay.getText().trim(),
                nv != null ? nv.getHoTen() : "Nhân viên kho",
                txtGhiChu.getText().trim()
        );

        phieu.getChiTiet().addAll(dongTam);

        if (!kho.apDungTonKho(phieu)) {

            UiHelper.loi(
                    this,
                    "Không đủ tồn kho để xuất!",
                    txtSoLuong
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Đã tạo phiếu xuất kho "
                        + phieu.getSoPhieu()
                        + " — Tổng: "
                        + UiHelper.tien(phieu.getTongTien())
                        + " VNĐ"
        );

        resetSauKhiLuu();
    }

    // =====================================================
    // 13. LƯU PHIẾU CHỜ NHẬP
    // =====================================================

    private void luuChoNhap() {

        if (xuat) {
            return;
        }

        // Kiểm tra phiếu rỗng
        if (dongTam.isEmpty()) {

            UiHelper.loi(
                    this,
                    "Phiếu rỗng! Vui lòng thêm phụ tùng.",
                    cboPhuTung
            );

            return;
        }

        // Kiểm tra nhà cung cấp
        String ncc = layNhaCungCap();

        if (ncc.isEmpty()) {

            UiHelper.loi(
                    this,
                    "Nhà cung cấp không hợp lệ!",
                    cboNcc
            );

            return;
        }

        // Kiểm tra ngày dự kiến giao
        LocalDate ldGiao = UiHelper.chuyenChuoiSangNgay(
                txtNgayDuKien.getText().trim(),
                this,
                "Ngày dự kiến giao"
        );

        if (ldGiao == null) {
            txtNgayDuKien.requestFocus();
            return;
        }

        // Kiểm tra ngày lập
        LocalDate ldNgay = UiHelper.chuyenChuoiSangNgay(
                txtNgay.getText().trim(),
                this,
                "Ngày lập phiếu"
        );

        if (ldNgay == null) {
            txtNgay.requestFocus();
            return;
        }

        // Kiểm tra chi tiết phiếu
        for (ChiTietKho ct : dongTam) {

            if (kho.timPhuTung(ct.getMaPT()) == null) {

                UiHelper.loi(
                        this,
                        "Mã phụ tùng " + ct.getMaPT()
                                + " không tồn tại!",
                        cboPhuTung
                );

                return;
            }

            if (ct.getSoLuong() <= 0) {

                UiHelper.loi(
                        this,
                        "Số lượng phụ tùng không hợp lệ!",
                        txtSoLuong
                );

                return;
            }
        }

        // Tạo phiếu chờ nhập
        PhieuKho phieu = new PhieuKho(
                txtSoPhieu.getText().trim(),
                PhieuKho.NHAP,
                ldNgay,
                nv != null ? nv.getHoTen() : "Nhân viên kho",
                txtGhiChu.getText().trim(),
                ncc,
                ldGiao,
                PhieuKho.CHO_NHAP
        );

        phieu.getChiTiet().addAll(dongTam);

        try {

            kho.luuPhieuChoNhap(phieu);

        } catch (IllegalStateException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Không lưu được phiếu",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Đã lưu phiếu chờ nhập: "
                        + phieu.getSoPhieu() + "\n"
                        + "Nhà cung cấp: " + ncc + "\n"
                        + "Tổng tiền: "
                        + UiHelper.tien(phieu.getTongTien()) + " VNĐ\n\n"
                        + "Trạng thái: CHO_NHAP\n"
                        + "Chưa tăng tồn kho.",
                "Đã lưu đơn chờ nhập",
                JOptionPane.INFORMATION_MESSAGE
        );

        resetSauKhiLuu();
    }

    // =====================================================
    // 14. RESET FORM SAU KHI LƯU
    // =====================================================

    private void resetSauKhiLuu() {

        dongTam.clear();

        modelDong.setRowCount(0);

        txtGhiChu.setText("");
        txtSoLuong.setText("1");

        txtSoPhieu.setText(
                xuat ? kho.maPhieuXuatMoi() : kho.maPhieuNhapMoi()
        );

        txtNgay.setText(
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
        );

        txtNgayDuKien.setText(
                LocalDate.now().plusDays(7).format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                )
        );

        napComboPt();

        if (!xuat) {
            napComboNcc();
        }

        capNhatGia();

        if (!xuat) {
            capNhatNhaCungCap();
        }

        capNhatTong();
    }
}