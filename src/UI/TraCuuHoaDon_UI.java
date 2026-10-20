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

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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

        UiHelper.canGiua(table, 0, 1, 3, 4, 5, 6, 9);
        UiHelper.canTrai(table, 2, 11);

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        // Footer thao tác hóa đơn
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel lblHuongDan = new JLabel("Chọn một hóa đơn từ bảng để xem thông tin");
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHuongDan.setForeground(new Color(100, 116, 139));
        footer.add(lblHuongDan, BorderLayout.WEST);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);

        JButton btnIn = UiHelper.nut("In hóa đơn", UiIcon.of(UiIcon.PRINT, 15), new Color(13, 148, 136), true, e -> {});
        pnlNut.add(btnIn);
        footer.add(pnlNut, BorderLayout.EAST);

        bangPanel.add(footer, BorderLayout.SOUTH);
        root.add(bangPanel, BorderLayout.CENTER);

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

    }
