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
 * Phiếu nhập / xuất kho.
 * UC010: với phiếu NHẬP, có thêm:
 *   - Nhà cung cấp, ngày dự kiến giao.
 *   - Nút "Lưu chờ nhập" để ghi nhận đơn đặt hàng, chưa cộng tồn kho.
 *   - Nút "Nhập ngay" để cộng tồn kho luôn.
 */
public class NhapKho_UI extends JPanel {
    private final boolean xuat;
    private final NhanVien nv;
    private final JTextField txtSoPhieu = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtGhiChu = new JTextField();
    private final JTextField txtNcc = new JTextField();           // UC010
    private final JTextField txtNgayDuKien = new JTextField();    // UC010
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
        cboPhuTung.addActionListener(e -> capNhatGia());
        capNhatGia();

        JPanel formPanel = UiHelper.panelForm("Thông tin phiếu", 420);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Số phiếu", txtSoPhieu);
        UiHelper.themDong(form, 1, "Ngày lập", txtNgay);
        if (!xuat) {
            UiHelper.themDong(form, 2, "Nhà cung cấp", txtNcc);
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
        JButton bt1 = UiHelper.nut("➕  Thêm dòng", new Color(46, 160, 92), true, e -> themDong());
        JButton bt2 = UiHelper.nut("🗑  Xóa dòng cuối", new Color(220, 80, 80), true, e -> xoaDong());
        nut.add(bt1);
        nut.add(Box.createVerticalStrut(8));
        nut.add(bt2);
        if (!xuat) {
            JButton btChoNhap = UiHelper.nut("⏳  Lưu chờ nhập (đặt NCC)", new Color(225, 150, 45), true, e -> luuChoNhap());
            JButton btNhapNgay = UiHelper.nut("📥  Nhập kho ngay", new Color(37, 99, 168), true, e -> taoPhieu());
            nut.add(Box.createVerticalStrut(8));
            nut.add(btChoNhap);
            nut.add(Box.createVerticalStrut(8));
            nut.add(btNhapNgay);
        } else {
            JButton bt3 = UiHelper.nut("📤  Tạo phiếu xuất kho",
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

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(form, BorderLayout.CENTER);
        body.add(nut, BorderLayout.SOUTH);
        formPanel.add(body, BorderLayout.CENTER);

        JPanel bangPanel = UiHelper.panelBang("Chi tiết phiếu");
        modelDong = UiHelper.model("Mã PT", "Tên", "SL", "Đơn giá", "Thành tiền");
        JTable table = UiHelper.bang(modelDong);
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
        split.setResizeWeight(0);
        split.setDividerLocation(440);
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

    private PhuTung ptDangChon() {
        Object sel = cboPhuTung.getSelectedItem();
        if (sel == null) return null;
        String ma = sel.toString().split(" - ")[0];
        return kho.timPhuTung(ma);
    }

    private void capNhatGia() {
        PhuTung pt = ptDangChon();
        if (pt != null) txtDonGia.setText(String.valueOf(pt.getDonGia()));
    }

    private void themDong() {
        PhuTung pt = ptDangChon();
        if (pt == null) {
            UiHelper.loi(this, "Chưa có phụ tùng!", cboPhuTung);
            return;
        }
        if (!txtSoLuong.getText().trim().matches("\\d+") || Integer.parseInt(txtSoLuong.getText().trim()) <= 0) {
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

    /** Tạo phiếu nhập kho: cộng tồn kho ngay. */
    private void taoPhieu() {
        if (dongTam.isEmpty()) {
            UiHelper.loi(this, "Phiếu chưa có dòng chi tiết!", txtGhiChu);
            return;
        }
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
        resetSauKhiLuu();
    }

    /** UC010: lưu phiếu chờ nhập — ghi nhận đặt hàng NCC, chưa cộng tồn kho. */
    private void luuChoNhap() {
        if (xuat) return;     // chỉ áp dụng cho nhập
        if (dongTam.isEmpty()) {
            UiHelper.loi(this, "Phiếu chưa có dòng chi tiết!", txtGhiChu);
            return;
        }
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
                "Theo dõi trong menu 'Theo dõi chờ nhập' cho đến khi NCC giao hàng.",
                "Đã lưu đơn đặt hàng",
                JOptionPane.INFORMATION_MESSAGE);
        resetSauKhiLuu();
    }

    private void resetSauKhiLuu() {
        dongTam.clear();
        modelDong.setRowCount(0);
        txtGhiChu.setText("");
        txtNcc.setText("");
        txtSoPhieu.setText(xuat ? kho.maPhieuXuatMoi() : kho.maPhieuNhapMoi());
        napComboPt();
        capNhatTong();
    }
}
