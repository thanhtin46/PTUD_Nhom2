package UI;

import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.PhieuTiepNhan;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * TraCuuKhachHang_UI đáp ứng chuẩn nghiệp vụ:
 * - UC002: Tìm kiếm thông tin khách hàng (theo từ khóa đa năng: Mã KH, Tên, SĐT, CCCD)
 * - UC003: Xem chi tiết thông tin khách hàng và phương tiện sở hữu
 * - UC004: Xem lịch sử sửa chữa/bảo dưỡng của khách hàng tại gara
 */
public class TraCuuKhachHang_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final DefaultTableModel model = UiHelper.model(
            "Mã KH", "Họ tên", "SĐT", "CCCD", "Địa chỉ", "Email", "Ghi chú", "Ngày ĐK");
    private final KhoDuLieu kho = KhoDuLieu.get();
    private JTable table;

    public TraCuuKhachHang_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("🔍 Tra cứu khách hàng");
        JLabel phu = new JLabel("Tìm kiếm khách hàng, xem thông tin chi tiết xe và lịch sử sửa chữa (UC002, UC003, UC004)");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(Box.createVerticalStrut(4));
        north.add(phu);
        root.add(north, BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Kết quả tra cứu khách hàng");

        // Thanh tìm kiếm
        JPanel tim = new JPanel(new BorderLayout(12, 0));
        tim.setOpaque(false);
        tim.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel lbl = new JLabel("Từ khóa tìm kiếm:");
        lbl.setFont(UiHelper.FONT_NHAN);
        lbl.setForeground(new Color(51, 65, 85));
        tim.add(lbl, BorderLayout.WEST);

        UiHelper.trangTriInput(txtTuKhoa);
        tim.add(txtTuKhoa, BorderLayout.CENTER);

        JPanel boxNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boxNut.setOpaque(false);
        JButton btnTim = UiHelper.nut("Tìm kiếm", UiHelper.XANH_TRUNG, e -> napBang());
        JButton btnLamMoi = UiHelper.nut("Làm mới", UiHelper.XAM, e -> {
            txtTuKhoa.setText("");
            napBang();
        });
        boxNut.add(btnTim);
        boxNut.add(btnLamMoi);
        tim.add(boxNut, BorderLayout.EAST);
        txtTuKhoa.addActionListener(e -> napBang());
        bangPanel.add(tim, BorderLayout.NORTH);

        // Bảng danh sách
        table = UiHelper.bang(model);
        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        // Thanh thao tác chi tiết (UC003, UC004)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnXemChiTiet = UiHelper.nut("Xem chi tiết khách hàng", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> xemChiTiet());
        JButton btnXemLichSu = UiHelper.nut("Xem lịch sử sửa chữa", UiIcon.of(UiIcon.CLOCK, 15), UiHelper.CAM, e -> xemLichSuSuaChua());

        footer.add(btnXemChiTiet);
        footer.add(btnXemLichSu);
        bangPanel.add(footer, BorderLayout.SOUTH);

        root.add(bangPanel, BorderLayout.CENTER);
        napBang();
    }

    private void napBang() {
        String q = txtTuKhoa.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (KhachHang kh : kho.getKhachHangs()) {
            String gop = (kh.getMaKH() + " " + kh.getHoTen() + " " + kh.getSdt() + " " + kh.getCccd()).toLowerCase();
            if (q.isEmpty() || gop.contains(q)) {
                model.addRow(new Object[]{kh.getMaKH(), kh.getHoTen(), kh.getSdt(), kh.getCccd(),
                        kh.getDiaChi(), kh.getEmail(), kh.getGhiChu(), kh.getNgayDangKy()});
            }
        }
    }

    /** UC003: Xem thông tin chi tiết khách hàng và danh sách xe sở hữu */
    private void xemChiTiet() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trong bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maKH = model.getValueAt(dong, 0).toString();
        KhachHang kh = kho.timKhachHangTheoMa(maKH);
        if (kh == null) return;

        // Tìm xe sở hữu
        StringBuilder sbXe = new StringBuilder();
        int demXe = 0;
        for (PhuongTien xe : kho.getPhuongTiens()) {
            if (xe.getMaChuXe().equalsIgnoreCase(maKH)) {
                demXe++;
                sbXe.append("  • Biển số: <b>").append(xe.getBienSo()).append("</b> - ")
                    .append(xe.getHangXe()).append(" ").append(xe.getDongXe())
                    .append(" (Màu: ").append(xe.getMauSon()).append(", Năm: ").append(xe.getNamSX())
                    .append(", ODO: ").append(xe.getOdo()).append(" km)<br>");
            }
        }
        if (demXe == 0) sbXe.append("  <i>Chưa có thông tin phương tiện đăng ký</i><br>");

        String msg = "<html><div style='width:380px;font-family:Segoe UI;'>"
                + "<h3 style='color:#0F172A;margin-bottom:8px;'>THÔNG TIN CHI TIẾT KHÁCH HÀNG</h3>"
                + "<table cellpadding='3'>"
                + "<tr><td><b>Mã khách hàng:</b></td><td>" + kh.getMaKH() + "</td></tr>"
                + "<tr><td><b>Họ và tên:</b></td><td>" + kh.getHoTen() + "</td></tr>"
                + "<tr><td><b>Số điện thoại:</b></td><td>" + kh.getSdt() + "</td></tr>"
                + "<tr><td><b>CCCD/CMND:</b></td><td>" + kh.getCccd() + "</td></tr>"
                + "<tr><td><b>Email:</b></td><td>" + kh.getEmail() + "</td></tr>"
                + "<tr><td><b>Địa chỉ:</b></td><td>" + kh.getDiaChi() + "</td></tr>"
                + "<tr><td><b>Ngày đăng ký:</b></td><td>" + kh.getNgayDangKy() + "</td></tr>"
                + "<tr><td><b>Ghi chú:</b></td><td>" + kh.getGhiChu() + "</td></tr>"
                + "</table>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;margin:10px 0;'>"
                + "<h4 style='color:#2563EB;margin-bottom:6px;'>DANH SÁCH XE SỞ HỮU (" + demXe + " xe):</h4>"
                + sbXe.toString()
                + "</div></html>";

        JOptionPane.showMessageDialog(this, msg, "UC003: Chi tiết khách hàng", JOptionPane.INFORMATION_MESSAGE);
    }

    /** UC004: Xem lịch sử sửa chữa của khách hàng */
    private void xemLichSuSuaChua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trong bảng để xem lịch sử sửa chữa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maKH = model.getValueAt(dong, 0).toString();
        KhachHang kh = kho.timKhachHangTheoMa(maKH);
        if (kh == null) return;

        // Tạo dialog hiển thị bảng lịch sử sửa chữa
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lịch sử sửa chữa - " + kh.getHoTen(), true);
        dlg.setSize(800, 450);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new BorderLayout(12, 12));
        pnl.setBackground(UiHelper.NEN);
        pnl.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTieuDe = new JLabel("Lịch sử sửa chữa của khách hàng: " + kh.getHoTen() + " (" + kh.getSdt() + ")");
        lblTieuDe.setFont(UiHelper.FONT_TIEU_DE_PHU);
        lblTieuDe.setForeground(UiHelper.XANH_DAM);
        pnl.add(lblTieuDe, BorderLayout.NORTH);

        DefaultTableModel mdLs = UiHelper.model("Số phiếu", "Ngày tiếp nhận", "Biển số", "Hãng / Loại", "Yêu cầu sửa chữa", "KTV xử lý", "Tiến độ", "Chi phí");
        int count = 0;
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (p.getMaKH().equalsIgnoreCase(maKH) || p.getSdt().equals(kh.getSdt())) {
                count++;
                mdLs.addRow(new Object[]{
                        p.getSoPhieu(), p.getNgayTiepNhan(), p.getBienSo(),
                        p.getHangXe() + " " + p.getLoaiXe(),
                        p.getYeuCau(),
                        p.getKyThuatVien() == null || p.getKyThuatVien().isEmpty() ? "Chưa giao" : p.getKyThuatVien(),
                        p.getTienDo(),
                        UiHelper.tien(p.getThanhTien()) + " đ"
                });
            }
        }

        if (count == 0) {
            JOptionPane.showMessageDialog(this, "Khách hàng " + kh.getHoTen() + " chưa có lịch sử sửa chữa nào trong hệ thống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTable tblLs = UiHelper.bang(mdLs);
        JScrollPane spLs = new JScrollPane(tblLs);
        UiHelper.danhSachBong(spLs);
        pnl.add(spLs, BorderLayout.CENTER);

        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, e -> dlg.dispose());
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setOpaque(false);
        pnlBot.add(btnDong);
        pnl.add(pnlBot, BorderLayout.SOUTH);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }
}
