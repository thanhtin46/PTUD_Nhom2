package UI;

import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ThanhToanHoaDon_UI — Màn hình Quản lý & Thanh toán Hóa đơn dịch vụ:
 * - Chọn phiếu sửa chữa để thanh toán (tự động load thông tin khách hàng, xe, chi phí)
 * - Tùy chọn tiền công dịch vụ, tiền phụ tùng linh kiện, chiết khấu giảm giá (%)
 * - Lựa chọn hình thức thanh toán: Tiền mặt, Chuyển khoản, Thẻ
 * - Lập hóa đơn và in/xuất hóa đơn thanh toán chuyên nghiệp
 * - Bảng danh sách các hóa đơn đã lập, thống kê tổng doanh thu thu ngân
 */
public class ThanhToanHoaDon_UI extends JPanel {
    private final NhanVien nvHienTai;
    private final KhoDuLieu kho = KhoDuLieu.get();

    private final JTextField txtMaHD = new JTextField();
    private final JComboBox<String> cboPhieuSC = new JComboBox<>();
    private final JTextField txtKhachHang = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtTienDichVu = new JTextField("0");
    private final JTextField txtTienPhuTung = new JTextField("0");
    private final JTextField txtGiamGia = new JTextField("0");
    private final JLabel lblTongTien = new JLabel("0 đ");
    private final JComboBox<String> cboPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng (POS)"});
    private final JTextField txtGhiChu = new JTextField();

    private JTable table;
    private DefaultTableModel model;

    public ThanhToanHoaDon_UI(NhanVien nv) {
        this.nvHienTai = nv;
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("💳 Thanh toán hóa đơn");
        JLabel phu = new JLabel("Lập hóa đơn thanh toán dịch vụ sửa chữa & bảo dưỡng xe cho khách hàng");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(Box.createVerticalStrut(4));
        north.add(phu);
        root.add(north, BorderLayout.NORTH);

        // Split pane Form + Bảng
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0);
        split.setDividerLocation(450);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);

        khoiTaoDuLieu();
        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin thanh toán hóa đơn", 470);

        txtMaHD.setEditable(false);
        txtKhachHang.setEditable(false);
        txtSdt.setEditable(false);
        txtBienSo.setEditable(false);
        txtNgay.setEditable(false);

        // Sự kiện tính lại tiền khi thay đổi dịch vụ, phụ tùng, giảm giá
        java.awt.event.KeyAdapter tinhTienAdapter = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { tinhTongTien(); }
        };
        txtTienDichVu.addKeyListener(tinhTienAdapter);
        txtTienPhuTung.addKeyListener(tinhTienAdapter);
        txtGiamGia.addKeyListener(tinhTienAdapter);

        cboPhieuSC.addActionListener(e -> chonPhieuSC());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Phiếu tiếp nhận *", cboPhieuSC, 4, 4);

        // Dòng kết hợp Mã HĐ & Ngày thanh toán
        JPanel rowMaNgay = new JPanel(new GridLayout(1, 2, 8, 0));
        rowMaNgay.setOpaque(false);
        rowMaNgay.add(txtMaHD);
        rowMaNgay.add(txtNgay);
        UiHelper.themDong(form, 1, "Mã HĐ / Ngày lập", rowMaNgay, 4, 4);

        UiHelper.themDong(form, 2, "Khách hàng", txtKhachHang, 4, 4);
        UiHelper.themDong(form, 3, "Số điện thoại", txtSdt, 4, 4);
        UiHelper.themDong(form, 4, "Biển số xe", txtBienSo, 4, 4);
        UiHelper.themDong(form, 5, "Tiền công / DV (đ)", txtTienDichVu, 4, 4);
        UiHelper.themDong(form, 6, "Tiền phụ tùng (đ)", txtTienPhuTung, 4, 4);
        UiHelper.themDong(form, 7, "Chiết khấu (%)", txtGiamGia, 4, 4);
        UiHelper.themDong(form, 8, "Phương thức TT", cboPhuongThuc, 4, 4);
        UiHelper.themDong(form, 9, "Ghi chú giao dịch", txtGhiChu, 4, 4);

        // Box hiển thị tổng tiền nổi bật
        JPanel boxTong = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255)); // Blue 50
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(191, 219, 254)); // Blue 200
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        boxTong.setOpaque(false);
        boxTong.setBorder(new EmptyBorder(8, 14, 8, 14));
        boxTong.setPreferredSize(new Dimension(0, 44));
        JLabel lblTdeTong = new JLabel("TỔNG THANH TOÁN:");
        lblTdeTong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTdeTong.setForeground(new Color(30, 58, 138));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongTien.setForeground(UiHelper.CHU_CHAY);
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        boxTong.add(lblTdeTong, BorderLayout.WEST);
        boxTong.add(lblTongTien, BorderLayout.CENTER);

        // Các nút hành động
        JPanel nut = new JPanel(new GridLayout(1, 3, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(4, 0, 0, 0));

        JButton btnThanhToan = UiHelper.nut("Thanh toán", UiIcon.of(UiIcon.CHECK, 15), UiHelper.XANH_LA, e -> thanhToan());
        JButton btnIn = UiHelper.nut("In hóa đơn", UiIcon.of(UiIcon.PRINT, 15), UiHelper.XANH_TRUNG, e -> inHoaDon());
        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, e -> lamMoi());

        btnThanhToan.setBorder(new EmptyBorder(8, 6, 8, 6));
        btnIn.setBorder(new EmptyBorder(8, 6, 8, 6));
        btnLamMoi.setBorder(new EmptyBorder(8, 6, 8, 6));

        nut.add(btnThanhToan);
        nut.add(btnIn);
        nut.add(btnLamMoi);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        bottom.add(boxTong, BorderLayout.NORTH);
        bottom.add(nut, BorderLayout.SOUTH);

        UiHelper.ganNoiDungForm(formPanel, form, bottom);
        return formPanel;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách hóa đơn đã xuất");
        model = UiHelper.model("Mã HĐ", "Phiếu SC", "Khách hàng", "Biển số", "Ngày TT", "Phương thức", "Tổng tiền", "Thu ngân", "Trạng thái");
        table = UiHelper.bang(model);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienLenForm();
        });

        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        // Thanh công cụ dưới bảng
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bot.setOpaque(false);
        JButton btnHuy = UiHelper.nut("Hủy hóa đơn", UiHelper.CHU_CHAY, e -> huyHoaDon());
        bot.add(btnHuy);
        bangPanel.add(bot, BorderLayout.SOUTH);

        return bangPanel;
    }

    private void khoiTaoDuLieu() {
        txtMaHD.setText(kho.maHoaDonMoi());
        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Load danh sách phiếu sửa chữa vào combobox
        cboPhieuSC.removeAllItems();
        cboPhieuSC.addItem("-- Chọn phiếu sửa chữa --");
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            cboPhieuSC.addItem(p.getSoPhieu() + " - " + p.getBienSo() + " (" + p.getHoTen() + ")");
        }
    }

    private void chonPhieuSC() {
        int idx = cboPhieuSC.getSelectedIndex();
        if (idx <= 0) {
            txtKhachHang.setText("");
            txtSdt.setText("");
            txtBienSo.setText("");
            txtTienDichVu.setText("0");
            tinhTongTien();
            return;
        }
        String item = (String) cboPhieuSC.getSelectedItem();
        if (item == null) return;
        String soPhieu = item.split(" - ")[0];

        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (p.getSoPhieu().equals(soPhieu)) {
                txtKhachHang.setText(p.getHoTen());
                txtSdt.setText(p.getSdt());
                txtBienSo.setText(p.getBienSo());
                txtTienDichVu.setText(String.valueOf(p.getThanhTien()));
                tinhTongTien();
                break;
            }
        }
    }

    private long tinhTongTien() {
        try {
            long dv = Long.parseLong(txtTienDichVu.getText().trim().replace(",", ""));
            long pt = Long.parseLong(txtTienPhuTung.getText().trim().replace(",", ""));
            double gg = Double.parseDouble(txtGiamGia.getText().trim());

            long tongGoc = dv + pt;
            long sauGiam = Math.max(0, Math.round(tongGoc * (1.0 - gg / 100.0)));
            lblTongTien.setText(UiHelper.tien(sauGiam) + " đ");
            return sauGiam;
        } catch (Exception ex) {
            lblTongTien.setText("0 đ");
            return 0;
        }
    }

    private void napBang() {
        model.setRowCount(0);
        for (HoaDon hd : kho.getHoaDons()) {
            model.addRow(new Object[]{
                    hd.getMaHD(), hd.getSoPhieuSC(), hd.getTenKH(), hd.getBienSo(),
                    hd.getNgayThanhToan(), hd.getPhuongThuc(),
                    UiHelper.tien(hd.getTongTien()) + " đ",
                    hd.getNguoiThu(), hd.getTrangThai()
            });
        }
    }

    private void thanhToan() {
        if (cboPhieuSC.getSelectedIndex() <= 0) {
            UiHelper.loi(this, "Vui lòng chọn phiếu sửa chữa cần thanh toán!", cboPhieuSC);
            return;
        }

        long tong = tinhTongTien();
        if (tong <= 0) {
            int conf = JOptionPane.showConfirmDialog(this,
                    "Tổng thanh toán đang là 0 đồng. Bạn có chắc muốn tạo hóa đơn miễn phí?",
                    "Xác nhận số tiền", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
        }

        String item = (String) cboPhieuSC.getSelectedItem();
        String soPhieu = item.split(" - ")[0];

        // Lấy thông tin khách
        PhieuTiepNhan ptn = null;
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (p.getSoPhieu().equals(soPhieu)) { ptn = p; break; }
        }
        String maKH = ptn != null ? ptn.getMaKH() : "";

        long dv = Long.parseLong(txtTienDichVu.getText().trim().replace(",", ""));
        long pt = Long.parseLong(txtTienPhuTung.getText().trim().replace(",", ""));
        double gg = Double.parseDouble(txtGiamGia.getText().trim());
        String nguoiThu = nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân";

        HoaDon hd = new HoaDon(
                txtMaHD.getText().trim(),
                soPhieu,
                maKH,
                txtKhachHang.getText().trim(),
                txtSdt.getText().trim(),
                txtBienSo.getText().trim(),
                txtNgay.getText().trim(),
                dv, pt, gg, tong,
                (String) cboPhuongThuc.getSelectedItem(),
                nguoiThu,
                txtGhiChu.getText().trim(),
                HoaDon.DA_THANH_TOAN
        );

        kho.getHoaDons().add(hd);

        // Cập nhật tiến độ phiếu tiếp nhận sang "Hoàn thành"
        if (ptn != null) {
            ptn.setTienDo("Hoàn thành");
        }

        napBang();
        JOptionPane.showMessageDialog(this, "Thanh toán hóa đơn [" + hd.getMaHD() + "] thành công!\nPhiếu sửa chữa " + soPhieu + " đã chuyển sang trạng thái Hoàn thành.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        lamMoi();
    }

    private void inHoaDon() {
        int dong = table.getSelectedRow();
        HoaDon hd;
        if (dong >= 0) {
            String ma = model.getValueAt(dong, 0).toString();
            hd = null;
            for (HoaDon h : kho.getHoaDons()) if (h.getMaHD().equals(ma)) { hd = h; break; }
        } else {
            // Lấy theo form hiện tại nếu có
            if (cboPhieuSC.getSelectedIndex() <= 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn từ bảng để in!", "Chưa chọn hóa đơn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long dv = Long.parseLong(txtTienDichVu.getText().trim().replace(",", ""));
            long pt = Long.parseLong(txtTienPhuTung.getText().trim().replace(",", ""));
            double gg = Double.parseDouble(txtGiamGia.getText().trim());
            hd = new HoaDon(txtMaHD.getText(), "TN...", "", txtKhachHang.getText(), txtSdt.getText(), txtBienSo.getText(),
                    txtNgay.getText(), dv, pt, gg, tinhTongTien(), (String) cboPhuongThuc.getSelectedItem(),
                    nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân", txtGhiChu.getText(), "Tạm tính");
        }
        if (hd == null) return;

        // Mở popup giao diện hóa đơn in
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hóa đơn thanh toán - " + hd.getMaHD(), true);
        dlg.setSize(520, 640);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new BorderLayout(14, 14));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(24, 28, 24, 28));

        String html = "<html><div style='font-family:Segoe UI;color:#0F172A;'>"
                + "<div style='text-align:center;'>"
                + "<h2 style='margin:0;color:#2563EB;'>GARA Ô TÔ THE T4</h2>"
                + "<p style='margin:2px;font-size:11px;color:#64748B;'>Đ/c: 123 Nguyễn Văn Cừ, Quận 5, TP.HCM | Hotline: 0901 234 567</p>"
                + "<h3 style='margin-top:14px;border-top:1px dashed #CBD5E1;padding-top:10px;'>HÓA ĐƠN THANH TOÁN DỊCH VỤ</h3>"
                + "<p style='font-size:12px;color:#475569;'>Số hóa đơn: <b>" + hd.getMaHD() + "</b> | Ngày: " + hd.getNgayThanhToan() + "</p>"
                + "</div>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;'>"
                + "<table style='font-size:12px;width:100%;' cellpadding='4'>"
                + "<tr><td>Khách hàng:</td><td><b>" + hd.getTenKH() + "</b></td></tr>"
                + "<tr><td>Số điện thoại:</td><td>" + hd.getSdtKH() + "</td></tr>"
                + "<tr><td>Biển số xe:</td><td><b>" + hd.getBienSo() + "</b></td></tr>"
                + "<tr><td>Phiếu tiếp nhận:</td><td>" + hd.getSoPhieuSC() + "</td></tr>"
                + "<tr><td>Phương thức TT:</td><td><b>" + hd.getPhuongThuc() + "</b></td></tr>"
                + "</table>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;'>"
                + "<table style='font-size:12px;width:100%;' cellpadding='4'>"
                + "<tr><td>Tiền công dịch vụ:</td><td style='text-align:right;'><b>" + UiHelper.tien(hd.getTienDichVu()) + " đ</b></td></tr>"
                + "<tr><td>Tiền phụ tùng linh kiện:</td><td style='text-align:right;'><b>" + UiHelper.tien(hd.getTienPhuTung()) + " đ</b></td></tr>"
                + "<tr><td>Chiết khấu giảm giá:</td><td style='text-align:right;color:#E11D48;'>-" + hd.getGiamGiaPhanTram() + "%</td></tr>"
                + "<tr style='font-size:14px;background:#F8FAFC;'><td style='color:#1E293B;'><b>TỔNG TIỀN PHẢI THU:</b></td><td style='text-align:right;color:#2563EB;'><b>" + UiHelper.tien(hd.getTongTien()) + " đ</b></td></tr>"
                + "</table>"
                + "<p style='font-size:11px;color:#64748B;margin-top:12px;'>Ghi chú: " + (hd.getGhiChu().isEmpty() ? "Không có" : hd.getGhiChu()) + "</p>"
                + "<div style='text-align:center;margin-top:28px;font-size:11px;color:#94A3B8;'>"
                + "<p>Thu ngân lập phiếu: <b>" + hd.getNguoiThu() + "</b></p>"
                + "<p><i>Cảm ơn quý khách đã tin tưởng dịch vụ Gara The T4!</i></p>"
                + "</div>"
                + "</div></html>";

        JLabel lblHd = new JLabel(html);
        JScrollPane sp = new JScrollPane(lblHd);
        sp.setBorder(BorderFactory.createLineBorder(UiHelper.VIEN, 1, true));
        pnl.add(sp, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        JButton btnXacNhanIn = UiHelper.nut("In ngay", UiHelper.XANH_TRUNG, e -> {
            JOptionPane.showMessageDialog(dlg, "Đã gửi lệnh in thành công tới máy in nhiệt hóa đơn!", "In hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });
        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, e -> dlg.dispose());
        pnlNut.add(btnXacNhanIn);
        pnlNut.add(btnDong);
        pnl.add(pnlNut, BorderLayout.SOUTH);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }

    private void huyHoaDon() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần hủy!", "Chưa chọn hóa đơn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ma = model.getValueAt(dong, 0).toString();
        int conf = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn hủy hóa đơn [" + ma + "] không?", "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        for (HoaDon hd : kho.getHoaDons()) {
            if (hd.getMaHD().equals(ma)) {
                hd.setTrangThai(HoaDon.DA_HUY);
                break;
            }
        }
        napBang();
        JOptionPane.showMessageDialog(this, "Đã hủy hóa đơn [" + ma + "].", "Đã hủy", JOptionPane.INFORMATION_MESSAGE);
    }

    private void lamMoi() {
        txtMaHD.setText(kho.maHoaDonMoi());
        cboPhieuSC.setSelectedIndex(0);
        txtKhachHang.setText("");
        txtSdt.setText("");
        txtBienSo.setText("");
        txtTienDichVu.setText("0");
        txtTienPhuTung.setText("0");
        txtGiamGia.setText("0");
        txtGhiChu.setText("");
        lblTongTien.setText("0 đ");
        table.clearSelection();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        String ma = model.getValueAt(dong, 0).toString();
        for (HoaDon hd : kho.getHoaDons()) {
            if (hd.getMaHD().equals(ma)) {
                txtMaHD.setText(hd.getMaHD());
                txtKhachHang.setText(hd.getTenKH());
                txtSdt.setText(hd.getSdtKH());
                txtBienSo.setText(hd.getBienSo());
                txtNgay.setText(hd.getNgayThanhToan());
                txtTienDichVu.setText(String.valueOf(hd.getTienDichVu()));
                txtTienPhuTung.setText(String.valueOf(hd.getTienPhuTung()));
                txtGiamGia.setText(String.valueOf(hd.getGiamGiaPhanTram()));
                cboPhuongThuc.setSelectedItem(hd.getPhuongThuc());
                txtGhiChu.setText(hd.getGhiChu());
                lblTongTien.setText(UiHelper.tien(hd.getTongTien()) + " đ");
                break;
            }
        }
    }
}
