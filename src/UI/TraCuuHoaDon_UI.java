package UI;

import Entity.CauHinhHeThong;
import Entity.HoaDon;
import Entity.KhoDuLieu;
import Entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * TraCuuHoaDon_UI — Màn hình Chuyên biệt Tra cứu Hóa đơn dịch vụ:
 * - Tách biệt hoàn toàn với màn hình Lập hóa đơn (chấm dứt tình trạng trùng lặp)
 * - Tra cứu đa năng theo: Mã HĐ, Phiếu sửa chữa, Tên khách, SĐT, Biển số xe, Thu ngân
 * - Lọc theo Phương thức thanh toán (QR, Tiền mặt, POS) và Trạng thái (Đã TT, Đã hủy)
 * - Thẻ KPI thống kê tổng doanh thu thực thu, số lượng hóa đơn theo từng phương thức
 * - Xem chi tiết và In lại hóa đơn theo mẫu chuẩn Gara Ô tô The T4
 * - Xuất file hóa đơn (HTML) để lưu trữ hoặc gửi khách hàng
 */
public class TraCuuHoaDon_UI extends JPanel {
    private final NhanVien nvHienTai;
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final CauHinhHeThong cauHinh = kho.getCauHinh();

    // ==== Bộ lọc tìm kiếm ====
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboPhuongThuc = new JComboBox<>(new String[]{
            "Tất cả phương thức", "Chuyển khoản QR", "Tiền mặt", "Quẹt thẻ POS"
    });
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả trạng thái", "Đã thanh toán", "Đã hủy"
    });

    // ==== Thẻ thống kê KPI ====
    private final JLabel lblTongDoanhThu = new JLabel("0 đ");
    private final JLabel lblTongSoHD = new JLabel("0 HĐ");
    private final JLabel lblDoanhThuQR = new JLabel("0 đ");
    private final JLabel lblDoanhThuTienMat = new JLabel("0 đ");

    // ==== Bảng danh sách hóa đơn ====
    private DefaultTableModel model;
    private JTable table;

    public TraCuuHoaDon_UI(NhanVien nv) {
        this.nvHienTai = nv;
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header trang
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel tde = UiHelper.tieuDeTrang("Tra cứu hóa đơn thanh toán");
        JLabel lblRole = new JLabel("Người tra cứu: " + (nvHienTai != null ? nvHienTai.getHoTen() : "Nhân viên")
                + " (" + (nvHienTai != null ? nvHienTai.getVaiTro() : "Quản lý") + ")");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRole.setForeground(new Color(37, 99, 235));
        lblRole.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        titleBar.add(tde, BorderLayout.WEST);
        titleBar.add(lblRole, BorderLayout.EAST);

        JLabel phu = new JLabel("Tìm kiếm, tra cứu lịch sử thu tiền, lọc theo phương thức thanh toán và in lại hóa đơn dịch vụ");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);

        north.add(titleBar);
        north.add(Box.createVerticalStrut(2));
        north.add(phu);
        north.add(Box.createVerticalStrut(10));

        // Hàng thẻ KPI tóm tắt doanh thu
        north.add(taoHangKpi());
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        // Bảng kết quả tra cứu
        JPanel bangPanel = UiHelper.panelBang("Danh sách kết quả tra cứu hóa đơn");

        // Thanh công cụ tìm kiếm
        bangPanel.add(taoThanhCongCu(), BorderLayout.NORTH);

        // Cấu trúc bảng
        model = UiHelper.model(
                "Mã HĐ", "Số phiếu SC", "Khách hàng", "Số ĐT", "Biển số xe",
                "Ngày thanh toán", "Phương thức TT", "Tiền công (đ)", "Tiền phụ tùng (đ)",
                "Giảm giá (%)", "Tổng tiền (đ)", "Thu ngân lập", "Trạng thái"
        );
        table = UiHelper.bang(model);

        // Định dạng cột tiền & trạng thái
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSel) l.setForeground(new Color(37, 99, 235));
                return l;
            }
        });

        table.getColumnModel().getColumn(12).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSel) {
                    String st = val != null ? val.toString() : "";
                    if (st.contains("Đã thanh toán")) {
                        l.setForeground(new Color(22, 163, 74));   // Green 600
                    } else if (st.contains("Đã hủy")) {
                        l.setForeground(new Color(220, 38, 38));   // Red 600
                    } else {
                        l.setForeground(new Color(71, 85, 105));   // Slate 600
                    }
                }
                return l;
            }
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);   // Mã HĐ
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Số phiếu SC
        table.getColumnModel().getColumn(2).setPreferredWidth(160);  // Khách hàng
        table.getColumnModel().getColumn(3).setPreferredWidth(110);  // SĐT
        table.getColumnModel().getColumn(4).setPreferredWidth(110);  // Biển số xe
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Ngày thanh toán
        table.getColumnModel().getColumn(6).setPreferredWidth(130);  // Phương thức TT
        table.getColumnModel().getColumn(7).setPreferredWidth(110);  // Tiền công
        table.getColumnModel().getColumn(8).setPreferredWidth(120);  // Tiền phụ tùng
        table.getColumnModel().getColumn(9).setPreferredWidth(90);   // Giảm giá
        table.getColumnModel().getColumn(10).setPreferredWidth(120); // Tổng tiền
        table.getColumnModel().getColumn(11).setPreferredWidth(150); // Thu ngân lập
        table.getColumnModel().getColumn(12).setPreferredWidth(120); // Trạng thái

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        // Footer thao tác hóa đơn
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel lblHuongDan = new JLabel("Nhấp đúp chuột hoặc chọn 1 hóa đơn và nhấn nút để xem / in lại hóa đơn");
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHuongDan.setForeground(new Color(100, 116, 139));
        footer.add(lblHuongDan, BorderLayout.WEST);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);

        JButton btnXuat = UiHelper.nut("Xuất hóa đơn", UiIcon.of(UiIcon.EXPORT, 15), UiHelper.XANH_TRUNG, true, e -> xuatFileHoaDon());
        JButton btnIn = UiHelper.nut("In hóa đơn theo mẫu", UiIcon.of(UiIcon.PRINT, 15), new Color(13, 148, 136), true, e -> inHoaDonDangChon());

        pnlNut.add(btnXuat);
        pnlNut.add(btnIn);
        footer.add(pnlNut, BorderLayout.EAST);

        bangPanel.add(footer, BorderLayout.SOUTH);
        root.add(bangPanel, BorderLayout.CENTER);

        // Sự kiện nhấp đúp để in
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    inHoaDonDangChon();
                }
            }
        });

        napBang();
    }

    /** 4 thẻ KPI tóm tắt tài chính quầy thu ngân */
    private JPanel taoHangKpi() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);

        grid.add(taoTheKpi("Tổng doanh thu thực thu", lblTongDoanhThu, new Color(37, 99, 235), new Color(239, 246, 255)));
        grid.add(taoTheKpi("Tổng số hóa đơn đã xuất", lblTongSoHD, new Color(15, 23, 42), new Color(241, 245, 249)));
        grid.add(taoTheKpi("Thu qua Chuyển khoản QR", lblDoanhThuQR, new Color(13, 148, 136), new Color(240, 253, 250)));
        grid.add(taoTheKpi("Thu qua Tiền mặt / POS", lblDoanhThuTienMat, new Color(217, 119, 6), new Color(254, 243, 199)));

        return grid;
    }

    private JPanel taoTheKpi(String tieuDe, JLabel lblVal, Color fg, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel t = new JLabel(tieuDe);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(new Color(100, 116, 139));

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblVal.setForeground(fg);

        card.add(t, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    /** Thanh tìm kiếm với 3 bộ lọc liên hoàn */
    private JPanel taoThanhCongCu() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblTk = new JLabel("Từ khóa:");
        lblTk.setFont(UiHelper.FONT_NHAN);
        lblTk.setForeground(new Color(51, 65, 85));
        p.add(lblTk);

        txtTuKhoa.setPreferredSize(new Dimension(240, 36));
        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.setToolTipText("Nhập mã HĐ, số phiếu, tên khách hàng, SĐT hoặc biển số xe...");
        p.add(txtTuKhoa);

        JLabel lblPt = new JLabel("Phương thức:");
        lblPt.setFont(UiHelper.FONT_NHAN);
        lblPt.setForeground(new Color(51, 65, 85));
        p.add(lblPt);

        cboPhuongThuc.setPreferredSize(new Dimension(170, 36));
        cboPhuongThuc.setFont(UiHelper.FONT_BT);
        cboPhuongThuc.setBackground(Color.WHITE);
        p.add(cboPhuongThuc);

        JLabel lblSt = new JLabel("Trạng thái:");
        lblSt.setFont(UiHelper.FONT_NHAN);
        lblSt.setForeground(new Color(51, 65, 85));
        p.add(lblSt);

        cboTrangThai.setPreferredSize(new Dimension(160, 36));
        cboTrangThai.setFont(UiHelper.FONT_BT);
        cboTrangThai.setBackground(Color.WHITE);
        p.add(cboTrangThai);

        JButton btnTim = UiHelper.nut("Tìm kiếm", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_DAM, true, e -> napBang());
        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, true, e -> {
            txtTuKhoa.setText("");
            cboPhuongThuc.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            napBang();
        });

        p.add(btnTim);
        p.add(btnLamMoi);

        txtTuKhoa.addActionListener(e -> napBang());
        cboPhuongThuc.addActionListener(e -> napBang());
        cboTrangThai.addActionListener(e -> napBang());

        return p;
    }

    private void napBang() {
        String q = txtTuKhoa.getText().trim().toLowerCase();
        String ptLoc = (String) cboPhuongThuc.getSelectedItem();
        String stLoc = (String) cboTrangThai.getSelectedItem();
        if (ptLoc == null) ptLoc = "Tất cả phương thức";
        if (stLoc == null) stLoc = "Tất cả trạng thái";

        model.setRowCount(0);

        long tongDoanhThu = 0;
        long thuQR = 0;
        long thuKhac = 0;
        int count = 0;

        for (HoaDon hd : kho.getHoaDons()) {
            // Lọc phương thức
            if (!ptLoc.equals("Tất cả phương thức") && !ptLoc.equalsIgnoreCase(hd.getPhuongThuc())) {
                continue;
            }

            // Lọc trạng thái
            if (!stLoc.equals("Tất cả trạng thái") && !stLoc.equalsIgnoreCase(hd.getTrangThai())) {
                continue;
            }

            String gop = (hd.getMaHD() + " " + hd.getSoPhieuSC() + " " + hd.getTenKH() + " "
                    + hd.getSdtKH() + " " + hd.getBienSo() + " " + hd.getNguoiThu() + " "
                    + hd.getNgayThanhToan() + " " + hd.getGhiChu()).toLowerCase();

            if (q.isEmpty() || gop.contains(q)) {
                model.addRow(new Object[]{
                        hd.getMaHD(),
                        hd.getSoPhieuSC(),
                        hd.getTenKH(),
                        hd.getSdtKH(),
                        hd.getBienSo(),
                        hd.getNgayThanhToan(),
                        hd.getPhuongThuc(),
                        UiHelper.tien(hd.getTienDichVu()),
                        UiHelper.tien(hd.getTienPhuTung()),
                        hd.getGiamGiaPhanTram() + "%",
                        UiHelper.tien(hd.getTongTien()),
                        hd.getNguoiThu(),
                        hd.getTrangThai()
                });

                if (HoaDon.DA_THANH_TOAN.equals(hd.getTrangThai())) {
                    tongDoanhThu += hd.getTongTien();
                    if (hd.getPhuongThuc().contains("QR") || hd.getPhuongThuc().contains("khoản")) {
                        thuQR += hd.getTongTien();
                    } else {
                        thuKhac += hd.getTongTien();
                    }
                }
                count++;
            }
        }

        lblTongDoanhThu.setText(UiHelper.tien(tongDoanhThu) + " đ");
        lblTongSoHD.setText(count + " / " + kho.getHoaDons().size() + " HĐ");
        lblDoanhThuQR.setText(UiHelper.tien(thuQR) + " đ");
        lblDoanhThuTienMat.setText(UiHelper.tien(thuKhac) + " đ");
    }

    private HoaDon layHoaDonDangChon() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn từ bảng để thao tác!", "Chưa chọn hóa đơn", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String ma = model.getValueAt(r, 0).toString();
        for (HoaDon h : kho.getHoaDons()) {
            if (h.getMaHD().equals(ma)) return h;
        }
        return null;
    }

    private void inHoaDonDangChon() {
        HoaDon hd = layHoaDonDangChon();
        if (hd == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hóa đơn thanh toán dịch vụ — " + hd.getMaHD(), true);
        dlg.setSize(620, 720);
        dlg.setLocationRelativeTo(this);

        JPanel rootPnl = new JPanel(new BorderLayout(12, 12));
        rootPnl.setBackground(Color.WHITE);
        rootPnl.setBorder(new EmptyBorder(16, 20, 16, 20));

        String html = taoNoiDungHoaDonHtml(hd);
        JEditorPane editor = new JEditorPane("text/html", html);
        editor.setEditable(false);
        editor.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(editor);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        rootPnl.add(sp, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlNut.setOpaque(false);

        JButton btnIn = UiHelper.nut("🖨  In ngay", UiHelper.XANH_TRUNG, e -> {
            try {
                boolean ok = editor.print();
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, "Đã gửi lệnh in hóa đơn [" + hd.getMaHD() + "] tới máy in nhiệt!", "In thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnLuu = UiHelper.nut("Xuất file HTML", new Color(13, 148, 136), e -> luuFileHtml(hd, html));
        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, e -> dlg.dispose());

        pnlNut.add(btnLuu);
        pnlNut.add(btnIn);
        pnlNut.add(btnDong);
        rootPnl.add(pnlNut, BorderLayout.SOUTH);

        dlg.setContentPane(rootPnl);
        dlg.setVisible(true);
    }

    private void xuatFileHoaDon() {
        HoaDon hd = layHoaDonDangChon();
        if (hd == null) return;
        String html = taoNoiDungHoaDonHtml(hd);
        luuFileHtml(hd, html);
    }

    private void luuFileHtml(HoaDon hd, String html) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("HoaDon_" + hd.getMaHD() + ".html"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile(), StandardCharsets.UTF_8)) {
                fw.write(html);
                JOptionPane.showMessageDialog(this, "Đã xuất file hóa đơn thành công tại:\n" + fc.getSelectedFile().getAbsolutePath(), "Xuất file hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String taoNoiDungHoaDonHtml(HoaDon hd) {
        long tong = hd.getTongTien();
        String chuTien = ThanhToanHoaDon_UI.docSoThanhChu(tong);

        return "<html><body style='font-family:Segoe UI, sans-serif;margin:16px;color:#0F172A;'>"
                + "<div style='text-align:center;'>"
                + "<h2 style='margin:0;color:#1E3A8A;letter-spacing:1px;'>" + cauHinh.getTenGara().toUpperCase() + "</h2>"
                + "<p style='margin:3px 0;font-size:11px;color:#64748B;'>Đ/c: " + cauHinh.getDiaChi() + " | Hotline: " + cauHinh.getHotline() + "</p>"
                + "<p style='margin:2px 0;font-size:11px;color:#64748B;'>STK Vietcombank: <b>" + cauHinh.getStkNganHang() + "</b> (Napas 24/7)</p>"
                + "<h3 style='margin:16px 0 4px 0;color:#0F172A;border-top:2px dashed #94A3B8;padding-top:12px;'>HÓA ĐƠN DỊCH VỤ SỬA CHỮA & BẢO DƯỠNG</h3>"
                + "<p style='font-size:11px;color:#475569;'>Số hóa đơn: <b>" + hd.getMaHD() + "</b> &nbsp;|&nbsp; Ngày lập: " + hd.getNgayThanhToan() + "</p>"
                + "</div>"
                + "<table style='width:100%;font-size:12px;margin-top:10px;border-collapse:collapse;' cellpadding='4'>"
                + "<tr><td style='width:35%;color:#475569;'>Khách hàng:</td><td><b>" + hd.getTenKH() + "</b></td></tr>"
                + "<tr><td style='color:#475569;'>Số điện thoại:</td><td>" + hd.getSdtKH() + "</td></tr>"
                + "<tr><td style='color:#475569;'>Biển số xe:</td><td><b>" + hd.getBienSo() + "</b></td></tr>"
                + "<tr><td style='color:#475569;'>Phiếu tiếp nhận:</td><td>" + hd.getSoPhieuSC() + "</td></tr>"
                + "<tr><td style='color:#475569;'>Phương thức thanh toán:</td><td><b>" + hd.getPhuongThuc() + "</b></td></tr>"
                + "<tr><td style='color:#475569;'>Trạng thái:</td><td><b>" + hd.getTrangThai() + "</b></td></tr>"
                + "</table>"
                + "<br>"
                + "<table style='width:100%;font-size:11px;border-collapse:collapse;' border='1' bordercolor='#CBD5E1' cellpadding='6'>"
                + "<tr style='background:#F1F5F9;font-weight:bold;'>"
                + "<th>STT</th><th>Hạng mục dịch vụ / Phụ tùng</th><th>ĐVT</th><th>SL</th><th>Đơn giá (đ)</th><th>Thành tiền (đ)</th>"
                + "</tr>"
                + "<tr><td align='center'>1</td><td>Tiền công sửa chữa & bảo dưỡng kỹ thuật</td><td align='center'>Gói</td><td align='center'>1</td><td align='right'>" + UiHelper.tien(hd.getTienDichVu()) + "</td><td align='right'>" + UiHelper.tien(hd.getTienDichVu()) + "</td></tr>"
                + "<tr><td align='center'>2</td><td>Phụ tùng thay thế và vật tư kỹ thuật</td><td align='center'>Bộ</td><td align='center'>1</td><td align='right'>" + UiHelper.tien(hd.getTienPhuTung()) + "</td><td align='right'>" + UiHelper.tien(hd.getTienPhuTung()) + "</td></tr>"
                + "</table>"
                + "<table style='width:100%;font-size:12px;margin-top:8px;' cellpadding='4'>"
                + "<tr><td align='right' style='color:#475569;'>Tổng chi phí trước giảm:</td><td align='right' style='width:30%;'><b>" + UiHelper.tien(hd.getTienDichVu() + hd.getTienPhuTung()) + " đ</b></td></tr>"
                + "<tr><td align='right' style='color:#E11D48;'>Chiết khấu giảm giá (" + hd.getGiamGiaPhanTram() + "%):</td><td align='right' style='color:#E11D48;'>-" + UiHelper.tien((long)((hd.getTienDichVu() + hd.getTienPhuTung()) * (hd.getGiamGiaPhanTram() / 100.0))) + " đ</td></tr>"
                + "<tr><td align='right' style='color:#475569;'>Thuế GTGT (VAT 10%):</td><td align='right'>+" + UiHelper.tien(Math.round(tong * 0.1 / 1.1)) + " đ</td></tr>"
                + "<tr style='background:#EFF6FF;font-size:14px;'><td align='right' style='color:#1E3A8A;'><b>TỔNG TIỀN THANH TOÁN:</b></td><td align='right' style='color:#2563EB;'><b>" + UiHelper.tien(tong) + " đ</b></td></tr>"
                + "</table>"
                + "<p style='font-size:11px;font-style:italic;color:#1E293B;margin:6px 0;'>Số tiền viết bằng chữ: <b>" + chuTien + "</b></p>"
                + "<hr style='border:none;border-top:1px dashed #CBD5E1;margin:12px 0;'>"
                + "<table style='width:100%;font-size:11px;text-align:center;margin-top:10px;' cellpadding='4'>"
                + "<tr><td style='width:50%;'><b>KHÁCH HÀNG</b><br><span style='color:#94A3B8;'>(Ký và ghi rõ họ tên)</span><br><br><br><br><b>" + hd.getTenKH() + "</b></td>"
                + "<td style='width:50%;'><b>THU NGÂN LẬP PHIẾU</b><br><span style='color:#94A3B8;'>(Ký và xác nhận đã thu tiền)</span><br><br><br><br><b>" + hd.getNguoiThu() + "</b></td></tr>"
                + "</table>"
                + "<p style='text-align:center;font-size:10px;color:#94A3B8;margin-top:20px;'><i>Cảm ơn quý khách đã tin tưởng dịch vụ tại " + cauHinh.getTenGara() + "! Hân hạnh phục vụ quý khách lần sau.</i></p>"
                + "</body></html>";
    }
}
