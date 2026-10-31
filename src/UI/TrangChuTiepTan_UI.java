package UI;

import DAO.TrangChuTiepTan_DAO;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;
import Entity.ThongKeTiepTan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

/**
 * Màn hình trang chủ dành cho nhân viên tiếp tân.
 */
public class TrangChuTiepTan_UI extends JPanel {
    private final NhanVien nvHienTai;
    private final TrangChuTiepTan_DAO trangChuDAO = new TrangChuTiepTan_DAO();

    // Các nhãn hiển thị số liệu KPI để cập nhật khi làm mới
    private JLabel lblPhieuHomNay, lblMoTaPhieuHomNay;
    private JLabel lblXeDangSC, lblMoTaXeDangSC;
    private JLabel lblXeHoanThanh, lblMoTaXeHoanThanh;
    private JLabel lblXeChoTN, lblMoTaXeChoTN;
    private JLabel lblTongKH, lblMoTaTongKH;
    private JLabel lblTongXe, lblMoTaTongXe;
    private JLabel lblBaoHanh, lblMoTaBaoHanh;
    private JLabel lblHoaDon, lblMoTaHoaDon;
    private JLabel lblCapNhatLuc;

    // Bảng danh sách tiếp nhận gần đây
    private DefaultTableModel modelTiepNhan;
    private JTable tblTiepNhan;
    private ArrayList<PhieuTiepNhan> dsTiepNhanGanDay = new ArrayList<>();

    // Panel thanh tiến độ
    private JPanel pnlTienDoWrap;

    public TrangChuTiepTan_UI() {
        this(null);
    }

    public TrangChuTiepTan_UI(NhanVien nv) {
        this.nvHienTai = nv;
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        root.setLayout(new BorderLayout(0, 14));
        add(root, BorderLayout.CENTER);

        // 1. Header Trang chủ Tiếp tân
        root.add(taoHeader(), BorderLayout.NORTH);

        // 2. Nội dung chính cuộn dọc (Lưới KPI + Bảng tiếp nhận + Phân bổ tiến độ & Tác vụ nhanh)
        JPanel contentBody = new JPanel();
        contentBody.setOpaque(false);
        contentBody.setLayout(new BoxLayout(contentBody, BoxLayout.Y_AXIS));

        // Lấy dữ liệu thống kê từ DAO Tiếp tân
        ThongKeTiepTan tk = trangChuDAO.layThongKeTongQuan();

        // 2.1. Lưới thống kê 8 thẻ KPI tiếp tân
        contentBody.add(taoLuoiThongKe(tk));
        contentBody.add(Box.createVerticalStrut(14));

        // 2.2. Khung Bảng danh sách xe tiếp nhận gần đây + Cột phân bổ tiến độ & Tác vụ nhanh
        JPanel midPanel = new JPanel(new BorderLayout(14, 0));
        midPanel.setOpaque(false);
        midPanel.add(taoBangTiepNhanGanDay(), BorderLayout.CENTER);
        midPanel.add(taoPanelTienDoVaThaoTac(tk), BorderLayout.EAST);
        contentBody.add(midPanel);
        contentBody.add(Box.createVerticalStrut(14));

        // 2.3. Banner Chào mừng & Trạng thái hệ thống
        contentBody.add(taoBannerChaoMung());

        JScrollPane sp = new JScrollPane(contentBody);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        root.add(sp, BorderLayout.CENTER);

        // Nạp dữ liệu ban đầu
        napBangTiepNhan();
    }

    // ===============================================================
    // 1. HEADER TRANG CHỦ TIẾP TÂN
    // ===============================================================
    private JPanel taoHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel tde = UiHelper.tieuDeTrang("Bảng điều khiển Tiếp tân");
        JLabel phu = new JLabel("Tổng quan hoạt động tiếp nhận xe, theo dõi tiến độ sửa chữa và dịch vụ khách hàng tại xưởng");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(tde);
        left.add(Box.createVerticalStrut(3));
        left.add(phu);
        header.add(left, BorderLayout.WEST);

        // Nút làm mới dữ liệu & thời gian cập nhật
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.setOpaque(false);

        lblCapNhatLuc = new JLabel("Cập nhật: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        lblCapNhatLuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCapNhatLuc.setForeground(new Color(100, 116, 139));

        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, true, e -> capNhatDuLieu());
        btnLamMoi.setPreferredSize(new Dimension(110, 36));

        right.add(lblCapNhatLuc);
        right.add(btnLamMoi);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ===============================================================
    // 2. LƯỚI THẺ CHỈ SỐ KPI TIẾP TÂN (8 THẺ)
    // ===============================================================
    private JPanel taoLuoiThongKe(ThongKeTiepTan tk) {
        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 14));
        grid.setOpaque(false);

        // Hàng 1: Các chỉ số liên quan trực tiếp đến xe và xưởng dịch vụ
        lblPhieuHomNay = new JLabel(String.valueOf(tk.getPhieuHomNay()));
        lblMoTaPhieuHomNay = new JLabel("Tổng " + tk.getTongPhieuTiepNhan() + " phiếu tiếp nhận");
        grid.add(theThongKe(UiIcon.of(UiIcon.FILE, 24, new Color(37, 99, 235)), "Tiếp nhận hôm nay",
                lblPhieuHomNay, lblMoTaPhieuHomNay, new Color(37, 99, 235)));

        lblXeDangSC = new JLabel(String.valueOf(tk.getXeDangSuaChua()));
        lblMoTaXeDangSC = new JLabel("Đang xử lý trong xưởng");
        grid.add(theThongKe(UiIcon.of(UiIcon.WRENCH, 24, new Color(245, 158, 11)), "Xe đang sửa chữa",
                lblXeDangSC, lblMoTaXeDangSC, new Color(245, 158, 11)));

        lblXeHoanThanh = new JLabel(String.valueOf(tk.getXeHoanThanh()));
        lblMoTaXeHoanThanh = new JLabel("Sẵn sàng giao xe / thanh toán");
        grid.add(theThongKe(UiIcon.of(UiIcon.CHECK, 24, new Color(22, 163, 74)), "Xe đã hoàn thành",
                lblXeHoanThanh, lblMoTaXeHoanThanh, new Color(22, 163, 74)));

        lblXeChoTN = new JLabel(String.valueOf(tk.getXeChoTiepNhan()));
        lblMoTaXeChoTN = new JLabel("Chờ chẩn đoán & phân công");
        grid.add(theThongKe(UiIcon.of(UiIcon.CLOCK, 24, new Color(225, 29, 72)), "Xe chờ tiếp nhận",
                lblXeChoTN, lblMoTaXeChoTN, new Color(225, 29, 72)));

        // Hàng 2: Các chỉ số quản lý khách hàng, phương tiện, bảo hành và doanh thu
        lblTongKH = new JLabel(String.valueOf(tk.getTongKhachHang()));
        lblMoTaTongKH = new JLabel("+" + tk.getKhachHangMoiHomNay() + " khách mới hôm nay");
        grid.add(theThongKe(UiIcon.of(UiIcon.USERS, 24, new Color(124, 58, 237)), "Khách hàng",
                lblTongKH, lblMoTaTongKH, new Color(124, 58, 237)));

        lblTongXe = new JLabel(String.valueOf(tk.getTongPhuongTien()));
        lblMoTaTongXe = new JLabel("Phương tiện trong hệ thống");
        grid.add(theThongKe(UiIcon.of(UiIcon.CAR, 24, new Color(14, 165, 233)), "Phương tiện quản lý",
                lblTongXe, lblMoTaTongXe, new Color(14, 165, 233)));

        lblBaoHanh = new JLabel(String.valueOf(tk.getBaoHanhConHan()));
        lblMoTaBaoHanh = new JLabel("Còn hạn / " + tk.getTongPhieuBaoHanh() + " phiếu");
        grid.add(theThongKe(UiIcon.of(UiIcon.SHIELD, 24, new Color(79, 70, 229)), "Phiếu bảo hành",
                lblBaoHanh, lblMoTaBaoHanh, new Color(79, 70, 229)));

        lblHoaDon = new JLabel(UiHelper.tien(tk.getDoanhThuHomNay()) + " đ");
        lblHoaDon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMoTaHoaDon = new JLabel(tk.getHoaDonHomNay() + " hóa đơn hôm nay");
        grid.add(theThongKe(UiIcon.of(UiIcon.BILL, 24, new Color(5, 150, 105)), "Doanh thu hôm nay",
                lblHoaDon, lblMoTaHoaDon, new Color(5, 150, 105)));

        return grid;
    }

    private JPanel theThongKe(Icon icon, String ten, JLabel lblGiaTri, JLabel lblMoTa, Color mau) {
        JPanel p = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(new Color(15, 23, 42, 8));
                g2.fillRoundRect(0, 2, w, h - 2, 14, 14);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h - 1, 14, 14);

                g2.setColor(UiHelper.VIEN);
                g2.drawRoundRect(0, 0, w - 1, h - 2, 14, 14);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel bieuTuong = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 26));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bieuTuong.setOpaque(false);
        bieuTuong.setHorizontalAlignment(SwingConstants.CENTER);
        bieuTuong.setPreferredSize(new Dimension(50, 50));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTen.setForeground(new Color(100, 116, 139));

        if (lblGiaTri.getFont().getSize() < 20) {
            lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 26));
        }
        lblGiaTri.setForeground(UiHelper.XANH_DAM);

        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMoTa.setForeground(new Color(148, 163, 184));

        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGiaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(lblTen);
        text.add(Box.createVerticalStrut(2));
        text.add(lblGiaTri);
        text.add(Box.createVerticalStrut(2));
        text.add(lblMoTa);

        p.add(bieuTuong, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    // ===============================================================
    // 3. BẢNG DANH SÁCH XE TIẾP NHẬN GẦN ĐÂY
    // ===============================================================
    private JPanel taoBangTiepNhanGanDay() {
        JPanel p = UiHelper.panelBang("Danh sách phương tiện tiếp nhận gần đây");

        modelTiepNhan = UiHelper.model(
                "Số phiếu", "Biển số xe", "Khách hàng", "Số ĐT",
                "Hãng xe", "Ngày tiếp nhận", "Yêu cầu dịch vụ", "Tiến độ"
        );
        tblTiepNhan = UiHelper.bang(modelTiepNhan);
        tblTiepNhan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        dinhDangCot(0, SwingConstants.CENTER, 95);   // Số phiếu
        dinhDangCot(1, SwingConstants.CENTER, 105);  // Biển số xe
        dinhDangCot(2, SwingConstants.LEFT, 140);    // Khách hàng
        dinhDangCot(3, SwingConstants.CENTER, 100);  // Số ĐT
        dinhDangCot(4, SwingConstants.LEFT, 100);    // Hãng xe
        dinhDangCot(5, SwingConstants.CENTER, 105);  // Ngày tiếp nhận
        dinhDangCot(6, SwingConstants.LEFT, 210);    // Yêu cầu dịch vụ
        dinhDangCotTienDo(7, 125);                   // Tiến độ

        tblTiepNhan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = tblTiepNhan.getSelectedRow();
                    if (r >= 0 && r < dsTiepNhanGanDay.size()) {
                        xemChiTietPhieu(dsTiepNhanGanDay.get(r));
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(tblTiepNhan);
        sp.setPreferredSize(new Dimension(680, 260));
        UiHelper.danhSachBong(sp);
        p.add(sp, BorderLayout.CENTER);

        // Thanh công cụ dưới bảng
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 4, 4, 4));

        JLabel lblHuongDan = new JLabel("Nhấp đúp chuột vào một dòng để xem chi tiết phiếu tiếp nhận");
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHuongDan.setForeground(new Color(100, 116, 139));
        footer.add(lblHuongDan, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setOpaque(false);

        JButton btnXem = UiHelper.nut("Xem chi tiết", UiIcon.of(UiIcon.SEARCH, 14), UiHelper.XANH_TRUNG, e -> {
            int r = tblTiepNhan.getSelectedRow();
            if (r >= 0 && r < dsTiepNhanGanDay.size()) {
                xemChiTietPhieu(dsTiepNhanGanDay.get(r));
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu tiếp nhận trong bảng!", "Chưa chọn dòng", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnTatCa = UiHelper.nut("Xem tất cả", UiIcon.of(UiIcon.EYE, 14), UiHelper.XAM, true, e -> {
            Main_UI main = Main_UI.getInstance();
            if (main != null) main.chuyenTrang("Tra cứu phiếu tiếp nhận");
        });

        actionBtns.add(btnXem);
        actionBtns.add(btnTatCa);
        footer.add(actionBtns, BorderLayout.EAST);

        p.add(footer, BorderLayout.SOUTH);
        return p;
    }

    private void dinhDangCot(int cot, int canLe, int doRong) {
        TableColumn col = tblTiepNhan.getColumnModel().getColumn(cot);
        col.setPreferredWidth(doRong);
        int left = canLe == SwingConstants.LEFT ? 10 : 4;
        int right = canLe == SwingConstants.RIGHT ? 10 : 4;

        col.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(canLe);
                l.setBackground(new Color(241, 245, 249));
                l.setForeground(new Color(30, 41, 59));
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(6, left, 6, right)));
                return l;
            }
        });

        col.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(canLe);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                l.setBackground(s ? new Color(219, 234, 254) : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                l.setForeground(s ? UiHelper.XANH_TRUNG : new Color(30, 41, 59));
                l.setBorder(new EmptyBorder(4, left, 4, right));
                return l;
            }
        });
    }

    private void dinhDangCotTienDo(int cot, int doRong) {
        TableColumn col = tblTiepNhan.getColumnModel().getColumn(cot);
        col.setPreferredWidth(doRong);

        col.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBackground(new Color(241, 245, 249));
                l.setForeground(new Color(30, 41, 59));
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(6, 4, 6, 4)));
                return l;
            }
        });

        col.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setBackground(s ? new Color(219, 234, 254) : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                String status = v == null ? "" : v.toString();
                if (status.contains("Hoàn thành")) l.setForeground(new Color(22, 163, 74));
                else if (status.contains("Sửa chữa") || status.contains("Xử lý")) l.setForeground(new Color(217, 119, 6));
                else if (status.contains("Chờ")) l.setForeground(new Color(225, 29, 72));
                else l.setForeground(new Color(37, 99, 235));

                return l;
            }
        });
    }

    // ===============================================================
    // 4. KHỐI TIẾN ĐỘ & TÁC VỤ NHANH DÀNH CHO TIẾP TÂN
    // ===============================================================
    private JPanel taoPanelTienDoVaThaoTac(ThongKeTiepTan tk) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setPreferredSize(new Dimension(340, 0));

        // 4.1. Khối phân bổ tiến độ xe trong xưởng
        JPanel pnlTienDo = UiHelper.panelForm("Tiến độ xe trong xưởng", 340);
        pnlTienDoWrap = new JPanel();
        pnlTienDoWrap.setOpaque(false);
        pnlTienDoWrap.setLayout(new BoxLayout(pnlTienDoWrap, BoxLayout.Y_AXIS));

        napDuLieuTienDo(pnlTienDoWrap);

        pnlTienDo.add(pnlTienDoWrap, BorderLayout.CENTER);
        wrap.add(pnlTienDo);
        wrap.add(Box.createVerticalStrut(10));

        // 4.2. Khối lối tắt tác vụ nhanh của tiếp tân
        JPanel pnlTacVu = UiHelper.panelForm("Lối tắt tiếp tân", 340);
        JPanel gridBtns = new JPanel(new GridLayout(4, 1, 6, 6));
        gridBtns.setOpaque(false);

        JButton btnLapPhieu = UiHelper.nut("  Lập phiếu tiếp nhận mới", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> {
            Main_UI main = Main_UI.getInstance();
            if (main != null) main.chuyenTrang("Lập phiếu tiếp nhận");
        });
        BaoVeQuyen.ganQuyen(btnLapPhieu, Entity.QuyenTruyCap.Quyen.TIEP_NHAN);

        JButton btnKhachHang = UiHelper.nut("  Quản lý khách hàng", UiIcon.of(UiIcon.USERS, 15), UiHelper.XANH_TRUNG, e -> {
            Main_UI main = Main_UI.getInstance();
            if (main != null) main.chuyenTrang("Quản lý khách hàng");
        });

        JButton btnPhuongTien = UiHelper.nut("  Quản lý phương tiện", UiIcon.of(UiIcon.CAR, 15), UiHelper.XANH_TRUNG, e -> {
            Main_UI main = Main_UI.getInstance();
            if (main != null) main.chuyenTrang("Quản lý phương tiện");
        });

        JButton btnHoaDon = UiHelper.nut("  Quầy thu ngân / Hóa đơn", UiIcon.of(UiIcon.BILL, 15), UiHelper.XANH_DAM, e -> {
            Main_UI main = Main_UI.getInstance();
            if (main != null) main.chuyenTrang("Lập hóa đơn");
        });

        gridBtns.add(btnLapPhieu);
        gridBtns.add(btnKhachHang);
        gridBtns.add(btnPhuongTien);
        gridBtns.add(btnHoaDon);

        pnlTacVu.add(gridBtns, BorderLayout.CENTER);
        wrap.add(pnlTacVu);

        return wrap;
    }

    private void napDuLieuTienDo(JPanel container) {
        container.removeAll();
        Map<String, Integer> map = trangChuDAO.layThongKeTheoTienDo();
        int tong = 0;
        for (int v : map.values()) tong += v;
        if (tong == 0) tong = 1;

        container.add(taoDongTienDo("Chờ tiếp nhận", map.getOrDefault("Chờ tiếp nhận", 0), tong, new Color(225, 29, 72)));
        container.add(Box.createVerticalStrut(6));
        container.add(taoDongTienDo("Đang sửa chữa", map.getOrDefault("Đang sửa chữa", 0), tong, new Color(245, 158, 11)));
        container.add(Box.createVerticalStrut(6));
        container.add(taoDongTienDo("Hoàn thành", map.getOrDefault("Hoàn thành", 0), tong, new Color(22, 163, 74)));
        container.add(Box.createVerticalStrut(6));
        container.add(taoDongTienDo("Đã giao xe", map.getOrDefault("Đã giao xe", 0), tong, new Color(37, 99, 235)));
        container.revalidate();
        container.repaint();
    }

    private JPanel taoDongTienDo(String ten, int so, int tong, Color mau) {
        JPanel p = new JPanel(new BorderLayout(8, 2));
        p.setOpaque(false);

        int percent = Math.min(100, Math.round((so * 100.0f) / tong));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTen.setForeground(new Color(51, 65, 85));

        JLabel lblChiSo = new JLabel(so + " xe (" + percent + "%)");
        lblChiSo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChiSo.setForeground(mau);

        top.add(lblTen, BorderLayout.WEST);
        top.add(lblChiSo, BorderLayout.EAST);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(percent);
        bar.setPreferredSize(new Dimension(0, 8));
        bar.setForeground(mau);
        bar.setBackground(new Color(241, 245, 249));
        bar.setBorderPainted(false);

        p.add(top, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    // ===============================================================
    // 5. BANNER CHÀO MỪNG DƯỚI CÙNG
    // ===============================================================
    private JPanel taoBannerChaoMung() {
        JPanel chao = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(new Color(15, 23, 42, 10));
                g2.fillRoundRect(0, 2, w, h - 2, 14, 14);

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h - 1, 14, 14);

                g2.setColor(UiHelper.VIEN);
                g2.drawRoundRect(0, 0, w - 1, h - 2, 14, 14);

                GradientPaint gp = new GradientPaint(0, 0, new Color(37, 99, 235), 0, h, new Color(14, 165, 233));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 5, h - 1, 5, 5);
                g2.dispose();
            }
        };
        chao.setOpaque(false);
        chao.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel noiDungChao = new JPanel();
        noiDungChao.setOpaque(false);
        noiDungChao.setLayout(new BoxLayout(noiDungChao, BoxLayout.Y_AXIS));

        String tenNhanVien = nvHienTai != null ? nvHienTai.getHoTen() : "Nhân viên Tiếp tân";
        String vaiTro = nvHienTai != null ? nvHienTai.getVaiTro() : "Bộ phận Tiếp nhận & Dịch vụ";

        JLabel loiChao = new JLabel("Xin chào, " + tenNhanVien + " — " + vaiTro);
        loiChao.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loiChao.setForeground(UiHelper.XANH_DAM);

        JLabel huongDan = new JLabel("Hệ thống Gara Ô tô The T4 đã đồng bộ cơ sở dữ liệu và sẵn sàng phục vụ khách hàng tiếp nhận xe.");
        huongDan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        huongDan.setForeground(new Color(100, 116, 139));

        loiChao.setAlignmentX(Component.LEFT_ALIGNMENT);
        huongDan.setAlignmentX(Component.LEFT_ALIGNMENT);

        noiDungChao.add(loiChao);
        noiDungChao.add(Box.createVerticalStrut(3));
        noiDungChao.add(huongDan);
        chao.add(noiDungChao, BorderLayout.CENTER);

        JLabel trangThai = new JLabel("●  Hệ thống CSDL trực tuyến") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(22, 163, 74, 22));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        trangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        trangThai.setForeground(new Color(22, 163, 74));
        trangThai.setBorder(new EmptyBorder(6, 12, 6, 12));
        chao.add(trangThai, BorderLayout.EAST);

        return chao;
    }

    // ===============================================================
    // CẬP NHẬT DỮ LIỆU & NẠP BẢNG
    // ===============================================================
    private void napBangTiepNhan() {
        dsTiepNhanGanDay = trangChuDAO.layTiepNhanGanDay(10);
        modelTiepNhan.setRowCount(0);
        for (PhieuTiepNhan p : dsTiepNhanGanDay) {
            modelTiepNhan.addRow(new Object[]{
                    p.getSoPhieu(),
                    p.getBienSo(),
                    p.getHoTen(),
                    p.getSdt(),
                    p.getHangXe(),
                    p.getNgayTiepNhanHienThi(),
                    p.getYeuCau(),
                    p.getTienDo() != null ? p.getTienDo() : "Chờ tiếp nhận"
            });
        }
    }

    private void capNhatDuLieu() {
        ThongKeTiepTan tk = trangChuDAO.layThongKeTongQuan();

        lblPhieuHomNay.setText(String.valueOf(tk.getPhieuHomNay()));
        lblMoTaPhieuHomNay.setText("Tổng " + tk.getTongPhieuTiepNhan() + " phiếu tiếp nhận");

        lblXeDangSC.setText(String.valueOf(tk.getXeDangSuaChua()));
        lblMoTaXeDangSC.setText("Đang xử lý trong xưởng");

        lblXeHoanThanh.setText(String.valueOf(tk.getXeHoanThanh()));
        lblMoTaXeHoanThanh.setText("Sẵn sàng giao xe / thanh toán");

        lblXeChoTN.setText(String.valueOf(tk.getXeChoTiepNhan()));
        lblMoTaXeChoTN.setText("Chờ chẩn đoán & phân công");

        lblTongKH.setText(String.valueOf(tk.getTongKhachHang()));
        lblMoTaTongKH.setText("+" + tk.getKhachHangMoiHomNay() + " khách mới hôm nay");

        lblTongXe.setText(String.valueOf(tk.getTongPhuongTien()));
        lblMoTaTongXe.setText("Phương tiện trong hệ thống");

        lblBaoHanh.setText(String.valueOf(tk.getBaoHanhConHan()));
        lblMoTaBaoHanh.setText("Còn hạn / " + tk.getTongPhieuBaoHanh() + " phiếu");

        lblHoaDon.setText(UiHelper.tien(tk.getDoanhThuHomNay()) + " đ");
        lblMoTaHoaDon.setText(tk.getHoaDonHomNay() + " hóa đơn hôm nay");

        lblCapNhatLuc.setText("Cập nhật: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        napBangTiepNhan();
        if (pnlTienDoWrap != null) {
            napDuLieuTienDo(pnlTienDoWrap);
        }
    }

    private void xemChiTietPhieu(PhieuTiepNhan p) {
        if (p == null) return;
        String html = "<html><div style='width:450px;font-family:Segoe UI;font-size:12px;padding:4px;'>"
                + "<h3 style='color:#1E3A8A;margin-bottom:8px;'>CHI TIẾT PHIẾU TIẾP NHẬN: " + p.getSoPhieu() + "</h3>"
                + "<table cellpadding='4' width='100%'>"
                + "<tr><td width='130'><b>Số phiếu:</b></td><td><b style='color:#2563EB;'>" + p.getSoPhieu() + "</b></td></tr>"
                + "<tr><td><b>Khách hàng:</b></td><td>" + p.getHoTen() + "</td></tr>"
                + "<tr><td><b>Số điện thoại:</b></td><td>" + p.getSdt() + "</td></tr>"
                + "<tr><td><b>Biển số xe:</b></td><td><b style='color:#DC2626;'>" + p.getBienSo() + "</b></td></tr>"
                + "<tr><td><b>Hãng xe / Dòng xe:</b></td><td>" + (p.getHangXe() != null ? p.getHangXe() : "—") + " / " + (p.getLoaiXe() != null ? p.getLoaiXe() : "—") + "</td></tr>"
                + "<tr><td><b>Ngày tiếp nhận:</b></td><td>" + p.getNgayTiepNhanHienThi() + "</td></tr>"
                + "<tr><td><b>Người tiếp nhận:</b></td><td>" + (p.getNguoiLap() != null ? p.getNguoiLap() : "Tiếp tân") + "</td></tr>"
                + "<tr><td><b>Tiến độ hiện tại:</b></td><td><b style='color:#16A34A;'>" + (p.getTienDo() != null ? p.getTienDo() : "Chờ tiếp nhận") + "</b></td></tr>"
                + "<tr><td valign='top'><b>Yêu cầu dịch vụ:</b></td><td>" + (p.getYeuCau() != null ? p.getYeuCau() : "—") + "</td></tr>"
                + "</table>"
                + "</div></html>";

        JOptionPane.showMessageDialog(this, html, "Thông tin phiếu tiếp nhận " + p.getSoPhieu(), JOptionPane.INFORMATION_MESSAGE);
    }
}
