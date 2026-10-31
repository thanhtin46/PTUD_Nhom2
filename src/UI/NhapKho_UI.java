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

/**
 * Màn hình lập phiếu nhập kho phụ tùng từ nhà cung cấp.
 */
public class NhapKho_UI extends JPanel {
    private final boolean xuat;
    private final NhanVien nv;
    private final JTextField txtSoPhieu = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtGhiChu = new JTextField();
    private final JComboBox<String> cboNcc = new JComboBox<>();   // Nhà cung cấp
    private final JTextField txtNgayDuKien = new JTextField();    // Ngày dự kiến giao
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
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang(xuat ? "📤 Phiếu xuất kho" : "📥 Phiếu nhập kho"), BorderLayout.NORTH);

        txtSoPhieu.setEditable(false);
        txtSoPhieu.setText(xuat ? kho.maPhieuXuatMoi() : kho.maPhieuNhapMoi());
        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        // Mặc định ngày dự kiến giao = +7 ngày cho phiếu nhập
        txtNgayDuKien.setText(LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        napComboPt();
        if (!xuat) {
            napComboNcc();
        }
        cboPhuTung.addActionListener(e -> capNhatGia());
        capNhatGia();

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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Mã PT
        table.getColumnModel().getColumn(1).setPreferredWidth(250); // Tên
        table.getColumnModel().getColumn(2).setPreferredWidth(70);  // SL
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Đơn giá
        table.getColumnModel().getColumn(4).setPreferredWidth(140); // Thành tiền
        UiHelper.canGiua(table, 0, 2);
        UiHelper.canTrai(table, 1);
        UiHelper.canPhai(table, 3, 4);
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

    private void napComboPt() {
        cboPhuTung.removeAllItems();
        for (PhuTung pt : kho.getPhuTungs()) {
            cboPhuTung.addItem(pt.getMaPT() + " - " + pt.getTen() + " (tồn " + pt.getSoLuongTon() + ")");
        }
    }

    private void napComboNcc() {
        cboNcc.removeAllItems();
        cboNcc.setEditable(true);
        for (String ncc : kho.layDanhSachNhaCungCap()) {
            cboNcc.addItem(ncc);
        }
    }

    private String layNhaCungCap() {
        Object sel = cboNcc.getSelectedItem();
        return sel != null ? sel.toString().trim() : "";
    }

    private PhuTung ptDangChon() {
        Object sel = cboPhuTung.getSelectedItem();
        if (sel == null) return null;
        String s = sel.toString().trim();
        if (s.isEmpty()) return null;
        String ma = s.split(" - ")[0].trim();
        return kho.timPhuTung(ma);
    }

    private void capNhatGia() {
        PhuTung pt = ptDangChon();
        if (pt != null) txtDonGia.setText(String.valueOf(pt.getDonGia()));
    }

    private void themDong() {
        // Ngoại lệ: Mã phụ tùng không tồn tại
        PhuTung pt = ptDangChon();
        if (pt == null || kho.timPhuTung(pt.getMaPT()) == null) {
            UiHelper.loi(this, "Ngoại lệ: Mã phụ tùng không tồn tại trong hệ thống!", cboPhuTung);
            return;
        }

        // Ngoại lệ: Số lượng không hợp lệ
        String slStr = txtSoLuong.getText().trim();
        if (!slStr.matches("\\d+") || Integer.parseInt(slStr) <= 0) {
            UiHelper.loi(this, "Ngoại lệ: Số lượng không hợp lệ! Vui lòng nhập số nguyên dương lớn hơn 0.", txtSoLuong);
            return;
        }
        int sl = Integer.parseInt(slStr);

        long gia;
        try {
            gia = Long.parseLong(txtDonGia.getText().trim().replace(",", ""));
            if (gia < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            UiHelper.loi(this, "Đơn giá không hợp lệ! Vui lòng nhập số tiền hợp lệ.", txtDonGia);
            return;
        }

        if (xuat && sl > pt.getSoLuongTon()) {
            UiHelper.loi(this, "Số lượng xuất không được lớn hơn tồn kho (" + pt.getSoLuongTon() + ")!", txtSoLuong);
            return;
        }

        // Kiểm tra nếu phụ tùng đã có trong danh sách chi tiết thì cộng dồn số lượng (tránh trùng khóa chính CSDL)
        ChiTietKho daCo = null;
        int rowIdx = -1;
        for (int i = 0; i < dongTam.size(); i++) {
            if (dongTam.get(i).getMaPT().equalsIgnoreCase(pt.getMaPT())) {
                daCo = dongTam.get(i);
                rowIdx = i;
                break;
            }
        }

        if (daCo != null) {
            int slMoi = daCo.getSoLuong() + sl;
            daCo.setSoLuong(slMoi);
            daCo.setDonGia(gia);
            modelDong.setValueAt(slMoi, rowIdx, 2);
            modelDong.setValueAt(UiHelper.tien(gia), rowIdx, 3);
            modelDong.setValueAt(UiHelper.tien(daCo.getThanhTien()), rowIdx, 4);
        } else {
            ChiTietKho ct = new ChiTietKho(pt.getMaPT(), pt.getTen(), sl, gia);
            dongTam.add(ct);
            modelDong.addRow(new Object[]{ct.getMaPT(), ct.getTenPT(), ct.getSoLuong(),
                    UiHelper.tien(ct.getDonGia()), UiHelper.tien(ct.getThanhTien())});
        }
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

    /** Tạo phiếu: Phân nhánh xuất kho hoặc nhập kho ngay. */
    private void taoPhieu() {
        if (xuat) {
            taoPhieuXuatKho();
        } else {
            nhapKhoNgay();
        }
    }

    /**
     * Thực hiện nhập kho phụ tùng.
     */
    private void nhapKhoNgay() {
        // 1. Ngoại lệ: Phiếu rỗng
        if (dongTam.isEmpty()) {
            UiHelper.loi(this, "Ngoại lệ: Phiếu rỗng! Vui lòng chọn và thêm ít nhất một phụ tùng trước khi nhập kho.", cboPhuTung);
            return;
        }

        // 2. Kiểm tra Nhà cung cấp hợp lệ
        String ncc = layNhaCungCap();
        if (ncc.isEmpty()) {
            UiHelper.loi(this, "Nhà cung cấp không hợp lệ! Vui lòng chọn hoặc nhập nhà cung cấp.", cboNcc);
            return;
        }

        // 3. Ngoại lệ: Mã phụ tùng không tồn tại & Số lượng không hợp lệ
        for (ChiTietKho ct : dongTam) {
            PhuTung pt = kho.timPhuTung(ct.getMaPT());
            if (pt == null) {
                UiHelper.loi(this, "Ngoại lệ: Mã phụ tùng " + ct.getMaPT() + " không tồn tại trong danh mục hệ thống!", cboPhuTung);
                return;
            }
            if (ct.getSoLuong() <= 0) {
                UiHelper.loi(this, "Ngoại lệ: Số lượng phụ tùng " + ct.getTenPT() + " không hợp lệ (" + ct.getSoLuong() + ")!", txtSoLuong);
                return;
            }
        }

        String maNV = (nv != null && nv.getMaNV() != null && !nv.getMaNV().isEmpty()) ? nv.getMaNV() : "NV04";
        String tenNV = nv != null ? nv.getHoTen() : "Nhân viên kho";

        PhieuKho phieu = new PhieuKho(txtSoPhieu.getText().trim(),
                PhieuKho.NHAP,
                txtNgay.getText().trim(),
                tenNV,
                txtGhiChu.getText().trim(),
                ncc,
                txtNgayDuKien.getText().trim(),
                PhieuKho.DA_NHAP);
        phieu.getChiTiet().addAll(dongTam);

        try {
            // [Bắt đầu transaction SQL] -> [Lưu phiếu và chi tiết] -> [Cộng tồn kho] -> [Cập nhật trạng thái DA_NHAP] -> [COMMIT]
            kho.nhapKhoNgay(phieu, maNV);

            // [COMMIT] thành công -> Hoàn tất nhập kho
            JOptionPane.showMessageDialog(this,
                    "Hoàn tất nhập kho thành công!\n" +
                    "Số phiếu: " + phieu.getSoPhieu() + "\n" +
                    "Nhà cung cấp: " + ncc + "\n" +
                    "Tổng tiền: " + UiHelper.tien(phieu.getTongTien()) + " VNĐ\n" +
                    "Trạng thái: " + phieu.getTrangThaiHienThi() + "\n" +
                    "Tồn kho của các phụ tùng đã được cộng tăng đúng số lượng.",
                    "Hoàn tất nhập kho",
                    JOptionPane.INFORMATION_MESSAGE);

            resetSauKhiLuu();
        } catch (Exception ex) {
            // Ngoại lệ: Lỗi CSDL -> [ROLLBACK] -> Thông báo lỗi, giữ tồn cũ
            JOptionPane.showMessageDialog(this,
                    "Ngoại lệ: Lỗi CSDL trong quá trình nhập kho!\n" +
                    "Hệ thống đã thực hiện ROLLBACK toàn bộ giao dịch.\n" +
                    "Số lượng tồn kho cũ được giữ nguyên.\n\n" +
                    "Chi tiết lỗi: " + ex.getMessage(),
                    "Lỗi CSDL - Đã ROLLBACK (Giữ tồn cũ)",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void taoPhieuXuatKho() {
        if (dongTam.isEmpty()) {
            UiHelper.loi(this, "Phiếu chưa có dòng chi tiết!", txtGhiChu);
            return;
        }
        PhieuKho phieu = new PhieuKho(txtSoPhieu.getText().trim(),
                PhieuKho.XUAT,
                txtNgay.getText().trim(),
                nv != null ? nv.getHoTen() : "Nhân viên kho",
                txtGhiChu.getText().trim());
        phieu.getChiTiet().addAll(dongTam);
        if (!kho.apDungTonKho(phieu)) {
            UiHelper.loi(this, "Không đủ tồn kho để xuất!", txtSoLuong);
            return;
        }
        JOptionPane.showMessageDialog(this, "Đã tạo phiếu xuất kho " + phieu.getSoPhieu()
                + " — Tổng: " + UiHelper.tien(phieu.getTongTien()) + " VNĐ");
        resetSauKhiLuu();
    }

    /**
     * Nhánh Lưu chờ (Lưu phiếu CHO_NHAP -> Kết thúc, chưa tăng tồn).
     */
    private void luuChoNhap() {
        if (xuat) return; // Chỉ áp dụng cho nhập kho

        // 1. Ngoại lệ: Phiếu rỗng
        if (dongTam.isEmpty()) {
            UiHelper.loi(this, "Ngoại lệ: Phiếu rỗng! Vui lòng chọn và thêm phụ tùng vào danh sách.", cboPhuTung);
            return;
        }

        // 2. Kiểm tra Nhà cung cấp hợp lệ
        String ncc = layNhaCungCap();
        if (ncc.isEmpty()) {
            UiHelper.loi(this, "Nhà cung cấp không hợp lệ! Vui lòng chọn hoặc nhập nhà cung cấp.", cboNcc);
            return;
        }

        // 3. Kiểm tra ngày dự kiến giao
        String ngayDk = txtNgayDuKien.getText().trim();
        if (!ngayDk.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            UiHelper.loi(this, "Ngày dự kiến giao phải có dạng dd/MM/yyyy!", txtNgayDuKien);
            return;
        }

        // 4. Ngoại lệ: Mã phụ tùng không tồn tại & Số lượng không hợp lệ
        for (ChiTietKho ct : dongTam) {
            if (kho.timPhuTung(ct.getMaPT()) == null) {
                UiHelper.loi(this, "Ngoại lệ: Mã phụ tùng " + ct.getMaPT() + " không tồn tại!", cboPhuTung);
                return;
            }
            if (ct.getSoLuong() <= 0) {
                UiHelper.loi(this, "Ngoại lệ: Số lượng phụ tùng " + ct.getTenPT() + " không hợp lệ!", txtSoLuong);
                return;
            }
        }

        // Lưu phiếu CHO_NHAP
        PhieuKho phieu = new PhieuKho(txtSoPhieu.getText().trim(),
                PhieuKho.NHAP,
                txtNgay.getText().trim(),
                nv != null ? nv.getHoTen() : "Nhân viên kho",
                txtGhiChu.getText().trim(),
                ncc,
                ngayDk,
                PhieuKho.CHO_NHAP);
        phieu.getChiTiet().addAll(dongTam);

        // Kết thúc, chưa tăng tồn
        try { kho.luuPhieuChoNhap(phieu); }
        catch (IllegalStateException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Không lưu được phiếu", JOptionPane.ERROR_MESSAGE); return; }

        JOptionPane.showMessageDialog(this,
                "Đã lưu phiếu chờ nhập: " + phieu.getSoPhieu() + "\n" +
                "Nhà cung cấp: " + ncc + "\n" +
                "Tổng tiền: " + UiHelper.tien(phieu.getTongTien()) + " VNĐ\n\n" +
                "Trạng thái: CHO_NHAP (Chưa tăng tồn kho).\n" +
                "Theo dõi trong menu 'Theo dõi chờ nhập' cho đến khi NCC giao hàng.",
                "Đã lưu đơn chờ nhập",
                JOptionPane.INFORMATION_MESSAGE);

        resetSauKhiLuu();
    }

    private void resetSauKhiLuu() {
        dongTam.clear();
        modelDong.setRowCount(0);
        txtGhiChu.setText("");
        txtSoPhieu.setText(xuat ? kho.maPhieuXuatMoi() : kho.maPhieuNhapMoi());
        napComboPt();
        if (!xuat) {
            napComboNcc();
        }
        capNhatTong();
    }
}
