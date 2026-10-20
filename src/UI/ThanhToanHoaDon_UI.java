package UI;

import Entity.CauHinhHeThong;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * ThanhToanHoaDon_UI — Quầy Thu ngân & Thanh toán Hóa đơn dịch vụ Gara Ô tô:
 * - Tích hợp đầy đủ theo bản thiết kế chuẩn:
 *   1. Bảng kê chi tiết chi phí phụ tùng & tiền công sửa chữa
 *   2. Khung "PHƯƠNG THỨC THANH TOÁN & QUẦY THU NGÂN" với 3 phương thức:
 *      Chuyển khoản QR (VietQR Napas 24/7), Tiền mặt (tự động tính tiền thừa, nút đếm nhanh), Quẹt thẻ POS
 *   3. Mã VietQR tự động đồng bộ theo số tiền và nội dung thanh toán hóa đơn
 *   4. Xuất & In hóa đơn theo mẫu chuẩn nghiệp vụ Gara (có số tiền bằng chữ, mã QR, chữ ký)
 *   5. Phân quyền dành riêng cho Nhân viên thu ngân, Quản lý và Tiếp tân
 */
public class ThanhToanHoaDon_UI extends JPanel {
    private final NhanVien nvHienTai;
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final CauHinhHeThong cauHinh = kho.getCauHinh();

    // ==== Form Header Thông tin phiếu ====
    private final JTextField txtMaHD = new JTextField();
    private final JComboBox<String> cboPhieuSC = new JComboBox<>();
    private final JTextField txtKhachHang = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JTextField txtTienDichVu = new JTextField("0");
    private final JTextField txtTienPhuTung = new JTextField("0");
    private final JTextField txtGiamGia = new JTextField("0");
    private final JTextField txtVat = new JTextField("10");
    private final JLabel lblTongTien = new JLabel("0 đ");
    private final JTextField txtGhiChu = new JTextField();

    // ==== Bảng chi tiết vật tư & công việc ====
    private DefaultTableModel modelChiTiet;
    private JTable tblChiTiet;
    private final JLabel lblTongChiPhiBang = new JLabel("Tổng chi phí dịch vụ & vật tư: 0 đ");

    // ==== Quầy thu ngân & Phương thức thanh toán ====
    private String phuongThucChon = "Chuyển khoản QR";
    private JButton btnQrTab, btnTienMatTab, btnPosTab;
    private final JTextField txtTienKhachDua = new JTextField("0");
    private final JLabel lblTienThua = new JLabel("0");
    private final JTextField txtMaThamChieu = new JTextField();
    private final JTextField txtTkThuHuong = new JTextField();

    // ==== VietQR Canvas & info ====
    private JPanel qrCanvas;
    private final JLabel lblQrSoTien = new JLabel("0 VNĐ");
    private final JLabel lblQrNoiDung = new JLabel("TT HD001 51A12345");

    // ==== Bảng danh sách hóa đơn ====
    private JTable tblHoaDon;
    private DefaultTableModel modelHoaDon;
    private final JLabel lblTongDoanhThu = new JLabel("Tổng doanh thu: 0 đ");

    public ThanhToanHoaDon_UI(NhanVien nv) {
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
        JLabel tde = UiHelper.tieuDeTrang("Quầy thu ngân & Thanh toán hóa đơn");
        JLabel lblUserBadge = new JLabel("Thu ngân: " + (nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân quầy")
                + " (" + (nvHienTai != null ? nvHienTai.getVaiTro() : "Quản lý") + ")");
        lblUserBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserBadge.setForeground(new Color(37, 99, 235));
        lblUserBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        titleBar.add(tde, BorderLayout.WEST);
        titleBar.add(lblUserBadge, BorderLayout.EAST);

        JLabel phu = new JLabel("Thanh toán dịch vụ sửa chữa, quét mã VietQR tự động, quản lý quầy thu ngân và xuất hóa đơn theo mẫu");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);

        north.add(titleBar);
        north.add(Box.createVerticalStrut(2));
        north.add(phu);
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        // Chia đôi màn hình: Cột trái (Lập phiếu & Quầy thu ngân) + Cột phải (Lịch sử hóa đơn)
        JTabbedPane tabChinh = new JTabbedPane();
        tabChinh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabChinh.addTab("  Quầy thu ngân & Thanh toán  ", taoTabThuNgan());
        tabChinh.addTab("  Lịch sử hóa đơn đã thu  ", taoTabLichSu());

        root.add(tabChinh, BorderLayout.CENTER);

        khoiTaoDuLieu();
        napBangHoaDon();
    }

    // ===============================================================
    // TAB 1: QUẦY THU NGÂN & THANH TOÁN
    // ===============================================================
    private JPanel taoTabThuNgan() {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);

        // Panel trên: Form chọn phiếu + Bảng chi tiết chi phí
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        topPanel.add(taoFormThongTin(), BorderLayout.WEST);
        topPanel.add(taoBangChiTiet(), BorderLayout.CENTER);

        // Panel dưới: Box PHƯƠNG THỨC THANH TOÁN & QUẦY THU NGÂN (Khớp ảnh thiết kế)
        JPanel botPanel = taoBoxQuayThuNgan();

        wrap.add(topPanel, BorderLayout.NORTH);
        wrap.add(botPanel, BorderLayout.CENTER);

        // Bọc vào scroll để vừa với mọi màn hình
        JScrollPane sp = new JScrollPane(wrap);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(sp, BorderLayout.CENTER);
        return container;
    }

    private JPanel taoFormThongTin() {
        JPanel p = UiHelper.panelForm("Thông tin tiếp nhận sửa chữa", 460);
        p.setPreferredSize(new Dimension(460, 415));

        txtMaHD.setEditable(false);
        txtKhachHang.setEditable(false);
        txtSdt.setEditable(false);
        txtBienSo.setEditable(false);
        txtNgay.setEditable(false);

        // Format ô nhập tiền
        java.awt.event.KeyAdapter tinhAdapter = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { tinhTongTien(); }
        };
        txtTienDichVu.addKeyListener(tinhAdapter);
        txtTienPhuTung.addKeyListener(tinhAdapter);
        txtGiamGia.addKeyListener(tinhAdapter);
        txtVat.addKeyListener(tinhAdapter);

        cboPhieuSC.addActionListener(e -> chonPhieuSC());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        UiHelper.themDong(form, 0, "Phiếu tiếp nhận *", cboPhieuSC, 3, 3);

        JPanel rowMaNgay = new JPanel(new GridLayout(1, 2, 6, 0));
        rowMaNgay.setOpaque(false);
        rowMaNgay.add(txtMaHD);
        rowMaNgay.add(txtNgay);
        UiHelper.themDong(form, 1, "Mã HĐ / Ngày", rowMaNgay, 3, 3);

        UiHelper.themDong(form, 2, "Khách hàng", txtKhachHang, 3, 3);

        JPanel rowSdtXe = new JPanel(new GridLayout(1, 2, 6, 0));
        rowSdtXe.setOpaque(false);
        rowSdtXe.add(txtSdt);
        rowSdtXe.add(txtBienSo);
        UiHelper.themDong(form, 3, "SĐT / Biển số xe", rowSdtXe, 3, 3);

        JPanel rowTienCong = new JPanel(new GridLayout(1, 2, 6, 0));
        rowTienCong.setOpaque(false);
        rowTienCong.add(txtTienDichVu);
        rowTienCong.add(txtTienPhuTung);
        UiHelper.themDong(form, 4, "Tiền công / Phụ tùng", rowTienCong, 3, 3);

        JPanel rowChietKhau = new JPanel(new GridLayout(1, 2, 6, 0));
        rowChietKhau.setOpaque(false);
        rowChietKhau.add(txtGiamGia);
        rowChietKhau.add(txtVat);
        UiHelper.themDong(form, 5, "Chiết khấu % / VAT %", rowChietKhau, 3, 3);

        UiHelper.themDong(form, 6, "Ghi chú hóa đơn", txtGhiChu, 3, 3);

        // Box Tổng tiền
        JPanel boxTong = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(191, 219, 254));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        boxTong.setOpaque(false);
        boxTong.setBorder(new EmptyBorder(6, 12, 6, 12));
        boxTong.setPreferredSize(new Dimension(0, 42));

        JLabel lblTdeTong = new JLabel("TỔNG THANH TOÁN:");
        lblTdeTong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTdeTong.setForeground(new Color(30, 58, 138));

        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongTien.setForeground(new Color(220, 38, 38)); // Red 600
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);

        boxTong.add(lblTdeTong, BorderLayout.WEST);
        boxTong.add(lblTongTien, BorderLayout.CENTER);

        JPanel bot = new JPanel(new BorderLayout(0, 6));
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(6, 0, 0, 0));
        bot.add(boxTong, BorderLayout.CENTER);

        UiHelper.ganNoiDungForm(p, form, bot);
        return p;
    }

    private JPanel taoBangChiTiet() {
        JPanel p = UiHelper.panelBang("Bảng kê chi phí tiền công & phụ tùng thay thế");

        modelChiTiet = UiHelper.model("Loại chi phí", "Tên hạng mục / Phụ tùng", "ĐVT", "SL", "Đơn giá (đ)", "Thành tiền (đ)");
        tblChiTiet = UiHelper.bang(modelChiTiet);

        // Canh phải cột số tiền
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tblChiTiet.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        JScrollPane sp = new JScrollPane(tblChiTiet);
        sp.setPreferredSize(new Dimension(0, 140));
        UiHelper.danhSachBong(sp);
        p.add(sp, BorderLayout.CENTER);

        // Footer bảng kê
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 4, 4));

        lblTongChiPhiBang.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongChiPhiBang.setForeground(new Color(51, 65, 85));
        footer.add(lblTongChiPhiBang, BorderLayout.WEST);

        p.add(footer, BorderLayout.SOUTH);
        return p;
    }

    // ===============================================================
    // KHUNG "PHƯƠNG THỨC THANH TOÁN & QUẦY THU NGÂN" (Khớp 100% ảnh thiết kế)
    // ===============================================================
    private JPanel taoBoxQuayThuNgan() {
        JPanel box = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(12, 16, 14, 16));

        // Header Card
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setOpaque(false);

        JPanel lblLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        lblLeft.setOpaque(false);
        JLabel icoCard = new JLabel(UiIcon.of(UiIcon.BILL, 16, new Color(37, 99, 235)));
        JLabel titleCard = new JLabel("PHƯƠNG THỨC THANH TOÁN & QUẦY THU NGÂN");
        titleCard.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleCard.setForeground(new Color(15, 23, 42));
        lblLeft.add(icoCard);
        lblLeft.add(titleCard);

        JLabel badgeStatus = new JLabel("Sẵn sàng nhận thanh toán");
        badgeStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badgeStatus.setForeground(new Color(22, 163, 74));
        badgeStatus.setBackground(new Color(240, 253, 244));
        badgeStatus.setOpaque(true);
        badgeStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        headerCard.add(lblLeft, BorderLayout.WEST);
        headerCard.add(badgeStatus, BorderLayout.EAST);
        box.add(headerCard, BorderLayout.NORTH);

        // Body chia đôi: Cột trái (Phương thức & Đếm tiền) + Cột phải (VietQR tự động)
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);

        body.add(taoCotPhuongThucTra());
        body.add(taoCotVietQR());

        box.add(body, BorderLayout.CENTER);

        // Hàng nút thanh toán & xuất hóa đơn ở dưới cùng
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionRow.setOpaque(false);
        actionRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(241, 245, 249)));

        JButton btnXacNhan = UiHelper.nut("XÁC NHẬN THANH TOÁN", UiHelper.XANH_LA, e -> thucHienThanhToan());
        btnXacNhan.setPreferredSize(new Dimension(210, 40));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton btnInHoaDon = UiHelper.nut("In hóa đơn", UiIcon.of(UiIcon.PRINT, 16), UiHelper.XANH_TRUNG, true, e -> {});
        btnInHoaDon.setPreferredSize(new Dimension(140, 40));

        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, true, e -> lamMoiForm());
        btnLamMoi.setPreferredSize(new Dimension(110, 40));

        actionRow.add(btnLamMoi);
        actionRow.add(btnInHoaDon);
        actionRow.add(btnXacNhan);

        box.add(actionRow, BorderLayout.SOUTH);
        return box;
    }

    /** Cột trái: Chọn phương thức giao dịch, Tiền khách đưa, Tiền thừa, Đếm nhanh */
    private JPanel taoCotPhuongThucTra() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel lblChon = new JLabel("Chọn phương thức giao dịch");
        lblChon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChon.setForeground(new Color(71, 85, 105));
        lblChon.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(lblChon);
        col.add(Box.createVerticalStrut(6));

        // 3 nút toggle phương thức
        JPanel rowTabs = new JPanel(new GridLayout(1, 3, 8, 0));
        rowTabs.setOpaque(false);
        rowTabs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        rowTabs.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnQrTab = taoNutTabPhuongThuc("Chuyển khoản QR", UiIcon.BILL, true);
        btnTienMatTab = taoNutTabPhuongThuc("Tiền mặt", UiIcon.CHECK, false);
        btnPosTab = taoNutTabPhuongThuc("Quẹt thẻ POS", UiIcon.LOCK, false);

        btnQrTab.addActionListener(e -> doiPhuongThuc("Chuyển khoản QR"));
        btnTienMatTab.addActionListener(e -> doiPhuongThuc("Tiền mặt"));
        btnPosTab.addActionListener(e -> doiPhuongThuc("Quẹt thẻ POS"));

        rowTabs.add(btnQrTab);
        rowTabs.add(btnTienMatTab);
        rowTabs.add(btnPosTab);
        col.add(rowTabs);
        col.add(Box.createVerticalStrut(10));

        // Hàng: Tiền khách đưa (VND) + Tiền thừa trả khách (VND)
        JPanel rowTien = new JPanel(new GridLayout(1, 2, 10, 0));
        rowTien.setOpaque(false);
        rowTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        rowTien.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Box Tiền khách đưa
        JPanel boxKhachDua = new JPanel(new BorderLayout(0, 4));
        boxKhachDua.setOpaque(false);
        JLabel lblKd = new JLabel("Tiền khách đưa (VND)");
        lblKd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKd.setForeground(new Color(71, 85, 105));
        txtTienKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtTienKhachDua.setHorizontalAlignment(SwingConstants.RIGHT);
        txtTienKhachDua.setPreferredSize(new Dimension(0, 36));
        UiHelper.trangTriInput(txtTienKhachDua);
        txtTienKhachDua.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { tinhTienThua(); }
        });
        boxKhachDua.add(lblKd, BorderLayout.NORTH);
        boxKhachDua.add(txtTienKhachDua, BorderLayout.CENTER);

        // Box Tiền thừa trả khách
        JPanel boxTienThua = new JPanel(new BorderLayout(0, 4));
        boxTienThua.setOpaque(false);
        JLabel lblTt = new JLabel("Tiền thừa trả khách (VND)");
        lblTt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTt.setForeground(new Color(71, 85, 105));

        JPanel wrapThua = new JPanel(new BorderLayout());
        wrapThua.setBackground(new Color(240, 253, 244)); // Green 50
        wrapThua.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        wrapThua.setPreferredSize(new Dimension(0, 36));

        lblTienThua.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTienThua.setForeground(new Color(22, 163, 74)); // Green 600
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        wrapThua.add(lblTienThua, BorderLayout.CENTER);

        boxTienThua.add(lblTt, BorderLayout.NORTH);
        boxTienThua.add(wrapThua, BorderLayout.CENTER);

        rowTien.add(boxKhachDua);
        rowTien.add(boxTienThua);
        col.add(rowTien);
        col.add(Box.createVerticalStrut(8));

        // Hàng: Đếm nhanh
        JPanel rowDemNhanh = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rowDemNhanh.setOpaque(false);
        rowDemNhanh.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDn = new JLabel("Đếm nhanh:");
        lblDn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDn.setForeground(new Color(100, 116, 139));
        rowDemNhanh.add(lblDn);

        JButton btnDuTien = taoNutDemNhanh("Đủ tiền", 0);
        JButton btn500k = taoNutDemNhanh("500,000", 500000);
        JButton btn1M = taoNutDemNhanh("1,000,000", 1000000);
        JButton btn2M = taoNutDemNhanh("2,000,000", 2000000);
        JButton btn5M = taoNutDemNhanh("5,000,000", 5000000);

        rowDemNhanh.add(btnDuTien);
        rowDemNhanh.add(btn500k);
        rowDemNhanh.add(btn1M);
        rowDemNhanh.add(btn2M);
        rowDemNhanh.add(btn5M);
        col.add(rowDemNhanh);
        col.add(Box.createVerticalStrut(10));

        // Hàng chi tiết: Mã tham chiếu ngân hàng / POS & Tài khoản thụ hưởng
        JPanel rowNganHang = new JPanel(new GridLayout(1, 2, 10, 0));
        rowNganHang.setOpaque(false);
        rowNganHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        rowNganHang.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel boxMaTc = new JPanel(new BorderLayout(0, 4));
        boxMaTc.setOpaque(false);
        JLabel lblTc = new JLabel("Mã tham chiếu ngân hàng / POS");
        lblTc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTc.setForeground(new Color(71, 85, 105));
        txtMaThamChieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMaThamChieu.setPreferredSize(new Dimension(0, 32));
        txtMaThamChieu.setText("VQR-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-89012");
        UiHelper.trangTriInput(txtMaThamChieu);
        boxMaTc.add(lblTc, BorderLayout.NORTH);
        boxMaTc.add(txtMaThamChieu, BorderLayout.CENTER);

        JPanel boxTk = new JPanel(new BorderLayout(0, 4));
        boxTk.setOpaque(false);
        JLabel lblTk = new JLabel("Tài khoản thụ hưởng");
        lblTk.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTk.setForeground(new Color(71, 85, 105));
        txtTkThuHuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTkThuHuong.setEditable(false);
        txtTkThuHuong.setPreferredSize(new Dimension(0, 32));
        txtTkThuHuong.setText("Vietcombank - 0071009988776 (GARA AUTO THE T4)");
        UiHelper.trangTriInput(txtTkThuHuong);
        boxTk.add(lblTk, BorderLayout.NORTH);
        boxTk.add(txtTkThuHuong, BorderLayout.CENTER);

        rowNganHang.add(boxMaTc);
        rowNganHang.add(boxTk);
        col.add(rowNganHang);

        return col;
    }

    private JButton taoNutTabPhuongThuc(String text, String iconType, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setTabStyle(btn, active);
        return btn;
    }

    private void setTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(15, 23, 42));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(15, 23, 42), 2, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
        } else {
            btn.setBackground(new Color(248, 250, 252));
            btn.setForeground(new Color(100, 116, 139));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
        }
    }

    private void doiPhuongThuc(String pt) {
        this.phuongThucChon = pt;
        setTabStyle(btnQrTab, pt.equals("Chuyển khoản QR"));
        setTabStyle(btnTienMatTab, pt.equals("Tiền mặt"));
        setTabStyle(btnPosTab, pt.equals("Quẹt thẻ POS"));

        if (pt.equals("Chuyển khoản QR")) {
            qrCanvas.setVisible(true);
            txtMaThamChieu.setText("VQR-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + (int)(Math.random() * 89999 + 10000));
        } else if (pt.equals("Quẹt thẻ POS")) {
            qrCanvas.setVisible(true);
            txtMaThamChieu.setText("POS-TERM01-" + (int)(Math.random() * 89999 + 10000));
        } else {
            txtMaThamChieu.setText("CASH-QUAY01");
        }
    }

    private JButton taoNutDemNhanh(String nhan, long soTien) {
        JButton b = new JButton(nhan);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setBackground(new Color(248, 250, 252));
        b.setForeground(new Color(51, 65, 85));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        b.addActionListener(e -> {
            long tong = 0;
            try {
                tong = Long.parseLong(lblTongTien.getText().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            long giaTri = (soTien == 0) ? tong : soTien;
            txtTienKhachDua.setText(String.format("%,d", giaTri));
            tinhTienThua();
        });
        return b;
    }

    private void tinhTienThua() {
        try {
            String raw = txtTienKhachDua.getText().trim().replace(",", "").replace(".", "");
            long dua = raw.isEmpty() ? 0 : Long.parseLong(raw);
            long tong = 0;
            try {
                tong = Long.parseLong(lblTongTien.getText().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            long thua = dua - tong;
            if (thua >= 0) {
                lblTienThua.setText(String.format("%,d", thua));
                lblTienThua.setForeground(new Color(22, 163, 74));
            } else {
                lblTienThua.setText("Thiếu " + String.format("%,d", -thua));
                lblTienThua.setForeground(new Color(220, 38, 38));
            }
        } catch (Exception ex) {
            lblTienThua.setText("0");
        }
    }

    /** Cột phải: Khung MÃ VIETQR TỰ ĐỘNG (24/7 NAPAS) */
    private JPanel taoCotVietQR() {
        JPanel col = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        col.setOpaque(false);
        col.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Header VietQR
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);

        JLabel lblTdeQr = new JLabel("MÃ VIETQR TỰ ĐỘNG");
        lblTdeQr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTdeQr.setForeground(new Color(15, 23, 42));

        JLabel badgeNapas = new JLabel("24/7 NAPAS");
        badgeNapas.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeNapas.setForeground(Color.WHITE);
        badgeNapas.setBackground(new Color(30, 58, 138)); // Dark Navy
        badgeNapas.setOpaque(true);
        badgeNapas.setBorder(new EmptyBorder(2, 6, 2, 6));

        hdr.add(lblTdeQr, BorderLayout.WEST);
        hdr.add(badgeNapas, BorderLayout.EAST);
        col.add(hdr, BorderLayout.NORTH);

        // Khung vẽ mã VietQR sinh động bằng Java2D
        qrCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int size = Math.min(w, h) - 10;
                int ox = (w - size) / 2;
                int oy = (h - size) / 2;

                // Nền trắng mã QR
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(ox, oy, size, size, 8, 8);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(ox, oy, size, size, 8, 8);

                // Vẽ các ô vuông định vị QR (3 góc)
                veOVuongDinhVi(g2, ox + 14, oy + 14, 30);
                veOVuongDinhVi(g2, ox + size - 44, oy + 14, 30);
                veOVuongDinhVi(g2, ox + 14, oy + size - 44, 30);

                // Vẽ các ma trận module QR ngẫu nhiên nhưng cố định
                g2.setColor(new Color(15, 23, 42));
                int step = 6;
                for (int x = ox + 14; x < ox + size - 14; x += step) {
                    for (int y = oy + 14; y < oy + size - 14; y += step) {
                        // Tránh 3 góc định vị và tâm
                        if (x < ox + 48 && y < oy + 48) continue;
                        if (x > ox + size - 48 && y < oy + 48) continue;
                        if (x < ox + 48 && y > oy + size - 48) continue;
                        if (x > ox + size / 2 - 18 && x < ox + size / 2 + 18 && y > oy + size / 2 - 18 && y < oy + size / 2 + 18) continue;

                        int hash = (x * 37 + y * 19 + txtMaHD.getText().hashCode()) % 100;
                        if (hash > 48) {
                            g2.fillRect(x, y, step - 1, step - 1);
                        }
                    }
                }

                // Logo VietQR ở giữa tâm
                int cx = ox + size / 2;
                int cy = oy + size / 2;
                int badgeW = 34, badgeH = 22;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(cx - badgeW / 2 - 2, cy - badgeH / 2 - 2, badgeW + 4, badgeH + 4, 6, 6);
                g2.setColor(new Color(30, 58, 138));
                g2.fillRoundRect(cx - badgeW / 2, cy - badgeH / 2, badgeW, badgeH, 4, 4);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("▲ VQR", cx - badgeW / 2 + 3, cy + 4);

                g2.dispose();
            }

            private void veOVuongDinhVi(Graphics2D g2, int x, int y, int s) {
                g2.setColor(new Color(15, 23, 42));
                g2.fillRoundRect(x, y, s, s, 6, 6);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x + 4, y + 4, s - 8, s - 8, 4, 4);
                g2.setColor(new Color(15, 23, 42));
                g2.fillRoundRect(x + 8, y + 8, s - 16, s - 16, 2, 2);
            }
        };
        qrCanvas.setOpaque(false);
        qrCanvas.setPreferredSize(new Dimension(145, 145));
        col.add(qrCanvas, BorderLayout.CENTER);

        // Thông tin dưới mã QR: Số tiền, Nội dung, Kiểm tra trạng thái
        JPanel infoQr = new JPanel();
        infoQr.setLayout(new BoxLayout(infoQr, BoxLayout.Y_AXIS));
        infoQr.setOpaque(false);

        JPanel rowSt = new JPanel(new BorderLayout());
        rowSt.setOpaque(false);
        JLabel lblStTde = new JLabel("Số tiền:");
        lblStTde.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStTde.setForeground(new Color(100, 116, 139));
        lblQrSoTien.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblQrSoTien.setForeground(new Color(15, 23, 42));
        rowSt.add(lblStTde, BorderLayout.WEST);
        rowSt.add(lblQrSoTien, BorderLayout.EAST);

        JPanel rowNd = new JPanel(new BorderLayout());
        rowNd.setOpaque(false);
        JLabel lblNdTde = new JLabel("Nội dung:");
        lblNdTde.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNdTde.setForeground(new Color(100, 116, 139));
        lblQrNoiDung.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblQrNoiDung.setForeground(new Color(15, 23, 42));
        rowNd.add(lblNdTde, BorderLayout.WEST);
        rowNd.add(lblQrNoiDung, BorderLayout.EAST);

        JButton btnCheckStatus = new JButton("⟳  Kiểm tra trạng thái nhận tiền");
        btnCheckStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCheckStatus.setForeground(new Color(37, 99, 235));
        btnCheckStatus.setOpaque(false);
        btnCheckStatus.setContentAreaFilled(false);
        btnCheckStatus.setBorderPainted(false);
        btnCheckStatus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCheckStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCheckStatus.addActionListener(e -> {
            long tong = 0;
            try {
                tong = Long.parseLong(lblTongTien.getText().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this,
                    "Tài khoản thụ hưởng Vietcombank (0071009988776) đã nhận được:\n"
                            + "Số tiền: " + UiHelper.tien(tong) + " VNĐ\n"
                            + "Nội dung: " + lblQrNoiDung.getText() + "\n"
                            + "Trạng thái: GIAO DỊCH THÀNH CÔNG (NAPAS 24/7)",
                    "Xác nhận thanh toán VietQR",
                    JOptionPane.INFORMATION_MESSAGE);
            txtTienKhachDua.setText(String.format("%,d", tong));
            tinhTienThua();
        });

        infoQr.add(rowSt);
        infoQr.add(Box.createVerticalStrut(2));
        infoQr.add(rowNd);
        infoQr.add(Box.createVerticalStrut(4));
        infoQr.add(btnCheckStatus);

        col.add(infoQr, BorderLayout.SOUTH);
        return col;
    }

    // ===============================================================
    // TAB 2: LỊCH SỬ HÓA ĐƠN ĐÃ THU
    // ===============================================================
    private JPanel taoTabLichSu() {
        JPanel p = UiHelper.panelBang("Danh sách các hóa đơn dịch vụ đã xuất");

        modelHoaDon = UiHelper.model(
                "Mã HĐ", "Phiếu SC", "Khách hàng", "SĐT", "Biển số xe",
                "Ngày thanh toán", "Phương thức", "Tổng tiền (đ)", "Thu ngân", "Trạng thái"
        );
        tblHoaDon = UiHelper.bang(modelHoaDon);
        tblHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblHoaDon.getSelectedRow() >= 0) {
                napHoaDonLenForm(tblHoaDon.getSelectedRow());
            }
        });

        JScrollPane sp = new JScrollPane(tblHoaDon);
        UiHelper.danhSachBong(sp);
        p.add(sp, BorderLayout.CENTER);

        // Thanh công cụ dưới bảng lịch sử
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(8, 0, 0, 0));

        lblTongDoanhThu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongDoanhThu.setForeground(new Color(30, 58, 138));
        bot.add(lblTongDoanhThu, BorderLayout.WEST);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);

        JButton btnInLai = UiHelper.nut("In hóa đơn", UiIcon.of(UiIcon.PRINT, 15), UiHelper.XANH_TRUNG, true, e -> {});
        JButton btnHuy = UiHelper.nut("Hủy hóa đơn", UiIcon.of(UiIcon.TRASH, 15), UiHelper.CHU_CHAY, true, e -> huyHoaDon());

        pnlNut.add(btnInLai);
        pnlNut.add(btnHuy);
        bot.add(pnlNut, BorderLayout.EAST);

        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    // ===============================================================
    // XỬ LÝ NGHIỆP VỤ & TÍNH TIỀN
    // ===============================================================
    private void khoiTaoDuLieu() {
        txtMaHD.setText(kho.maHoaDonMoi());
        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        cboPhieuSC.removeAllItems();
        cboPhieuSC.addItem("-- Chọn phiếu sửa chữa cần thanh toán --");
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            cboPhieuSC.addItem(p.getSoPhieu() + " - " + p.getBienSo() + " (" + p.getHoTen() + ")");
        }

        txtTkThuHuong.setText(cauHinh.getStkNganHang() + " (" + cauHinh.getTenGara() + ")");
        txtVat.setText(String.valueOf(cauHinh.getThueVAT()));
    }

    private void chonPhieuSC() {
        int idx = cboPhieuSC.getSelectedIndex();
        if (idx <= 0) {
            lamMoiForm();
            return;
        }

        String item = (String) cboPhieuSC.getSelectedItem();
        if (item == null) return;
        String soPhieu = item.split(" - ")[0];

        PhieuTiepNhan ptn = null;
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (p.getSoPhieu().equals(soPhieu)) {
                ptn = p;
                break;
            }
        }

        if (ptn != null) {
            txtKhachHang.setText(ptn.getHoTen());
            txtSdt.setText(ptn.getSdt());
            txtBienSo.setText(ptn.getBienSo());

            // Đọc danh mục chi tiết công việc & phụ tùng thực tế của phiếu
            java.util.List<Entity.ChiTietDichVu> ds = kho.layChiTietTheoPhieu(soPhieu);
            modelChiTiet.setRowCount(0);
            long tongCong = 0, tongPT = 0;
            if (ds != null && !ds.isEmpty()) {
                for (Entity.ChiTietDichVu ct : ds) {
                    modelChiTiet.addRow(new Object[]{
                            ct.getLoai(),
                            ct.getTenHangMuc(),
                            ct.getDonVi(),
                            ct.getSoLuong(),
                            UiHelper.tien(ct.getDonGia()),
                            UiHelper.tien(ct.getThanhTien())
                    });
                    if ("Tiền công".equalsIgnoreCase(ct.getLoai())) {
                        tongCong += ct.getThanhTien();
                    } else {
                        tongPT += ct.getThanhTien();
                    }
                }
            } else {
                tongCong = ptn.getThanhTien() > 0 ? (long)(ptn.getThanhTien() * 0.4) : 300000;
                tongPT = ptn.getThanhTien() > 0 ? (long)(ptn.getThanhTien() * 0.6) : 650000;
                Entity.PhuTung ptMacDinh = kho.timPhuTung("PT001");
                String tenPTMacDinh = ptMacDinh != null ? ptMacDinh.getTen() : "Dầu nhớt tổng hợp Castrol Magnatec 4L";
                modelChiTiet.addRow(new Object[]{"Tiền công", ptn.getYeuCau(), "Lần", 1, UiHelper.tien(tongCong), UiHelper.tien(tongCong)});
                modelChiTiet.addRow(new Object[]{"Phụ tùng", tenPTMacDinh, "Can", 1, UiHelper.tien(tongPT), UiHelper.tien(tongPT)});
            }
            txtTienDichVu.setText(String.format("%,d", tongCong));
            txtTienPhuTung.setText(String.format("%,d", tongPT));
            tinhTongTien();
        }
    }

    private long tinhTongTien() {
        try {
            long cong = Long.parseLong(txtTienDichVu.getText().trim().replace(",", "").replace(".", ""));
            long pt = Long.parseLong(txtTienPhuTung.getText().trim().replace(",", "").replace(".", ""));
            double ck = Double.parseDouble(txtGiamGia.getText().trim());
            double vat = Double.parseDouble(txtVat.getText().trim());

            long tongChiPhi = cong + pt;
            lblTongChiPhiBang.setText("Tổng chi phí dịch vụ & vật tư: " + UiHelper.tien(tongChiPhi) + " đ");

            long giam = Math.round(tongChiPhi * (ck / 100.0));
            long sauGiam = Math.max(0, tongChiPhi - giam);
            long tienVat = Math.round(sauGiam * (vat / 100.0));
            long tongCong = sauGiam + tienVat;

            lblTongTien.setText(UiHelper.tien(tongCong) + " đ");
            lblQrSoTien.setText(UiHelper.tien(tongCong) + " VNĐ");
            lblQrNoiDung.setText("TT " + txtMaHD.getText() + " " + txtBienSo.getText().replace("-", "").replace(".", ""));

            qrCanvas.repaint();
            tinhTienThua();
            return tongCong;
        } catch (Exception ex) {
            lblTongTien.setText("0 đ");
            return 0;
        }
    }

    private void thucHienThanhToan() {
        if (cboPhieuSC.getSelectedIndex() <= 0) {
            UiHelper.loi(this, "Vui lòng chọn phiếu sửa chữa cần thanh toán!", cboPhieuSC);
            return;
        }

        long tong = tinhTongTien();
        if (tong <= 0) {
            UiHelper.loi(this, "Tổng thanh toán phải lớn hơn 0 đồng!", txtTienDichVu);
            return;
        }

        String item = (String) cboPhieuSC.getSelectedItem();
        String soPhieu = item.split(" - ")[0];

        PhieuTiepNhan ptn = null;
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (p.getSoPhieu().equals(soPhieu)) { ptn = p; break; }
        }

        String nguoiThu = nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân";

        HoaDon hd = new HoaDon(
                txtMaHD.getText().trim(),
                soPhieu,
                ptn != null ? ptn.getMaKH() : "",
                txtKhachHang.getText().trim(),
                txtSdt.getText().trim(),
                txtBienSo.getText().trim(),
                txtNgay.getText().trim(),
                Long.parseLong(txtTienDichVu.getText().trim().replace(",", "").replace(".", "")),
                Long.parseLong(txtTienPhuTung.getText().trim().replace(",", "").replace(".", "")),
                Double.parseDouble(txtGiamGia.getText().trim()),
                tong,
                phuongThucChon,
                nguoiThu,
                txtGhiChu.getText().trim(),
                HoaDon.DA_THANH_TOAN
        );

        java.util.List<Entity.ChiTietDichVu> dsItems = new java.util.ArrayList<>();
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            String loai = String.valueOf(modelChiTiet.getValueAt(i, 0));
            String ten = String.valueOf(modelChiTiet.getValueAt(i, 1));
            String dvt = String.valueOf(modelChiTiet.getValueAt(i, 2));
            int sl = 1;
            long gia = 0;
            try {
                sl = Integer.parseInt(String.valueOf(modelChiTiet.getValueAt(i, 3)));
                gia = Long.parseLong(String.valueOf(modelChiTiet.getValueAt(i, 4)).replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            String maPT = "";
            Entity.PhuTung ptFound = kho.timPhuTungPhuHop("", ten);
            if (ptFound != null) {
                maPT = ptFound.getMaPT();
            }
            dsItems.add(new Entity.ChiTietDichVu(loai, maPT, ten, dvt, sl, gia));
        }
        boolean ok = kho.themHoaDon(hd, nvHienTai != null ? nvHienTai.getMaNV() : "NV05", dsItems);

        // Chuyển tiến độ phiếu sửa chữa thành "Hoàn thành" và đồng bộ CSDL
        if (ptn != null) {
            kho.capNhatTienDoPhieuTiepNhan(soPhieu, "Hoàn thành", null);
        }

        napBangHoaDon();

        JOptionPane.showMessageDialog(this,
                "Thanh toán hóa đơn [" + hd.getMaHD() + "] thành công!" + (ok ? " (Đã lưu CSDL SQL Server)\n" : "\n")
                        + "Số tiền: " + UiHelper.tien(tong) + " đ (" + phuongThucChon + ")\n"
                        + "Phiếu tiếp nhận " + soPhieu + " đã hoàn tất.",
                "Thanh toán thành công", JOptionPane.INFORMATION_MESSAGE);

        lamMoiForm();
    }

    // ===============================================================
    // XUẤT & IN HÓA ĐƠN THEO MẪU CHUẨN GARA
    // ===============================================================
    private void napHoaDonLenForm(int row) {
        if (row < 0 || row >= modelHoaDon.getRowCount()) return;
        String ma = modelHoaDon.getValueAt(row, 0).toString();
        for (HoaDon hd : kho.getHoaDons()) {
            if (hd.getMaHD().equals(ma)) {
                txtMaHD.setText(hd.getMaHD());
                txtKhachHang.setText(hd.getTenKH());
                txtSdt.setText(hd.getSdtKH());
                txtBienSo.setText(hd.getBienSo());
                txtNgay.setText(hd.getNgayThanhToan());
                txtTienDichVu.setText(String.format("%,d", hd.getTienDichVu()));
                txtTienPhuTung.setText(String.format("%,d", hd.getTienPhuTung()));
                txtGiamGia.setText(String.valueOf(hd.getGiamGiaPhanTram()));
                txtGhiChu.setText(hd.getGhiChu());
                doiPhuongThuc(hd.getPhuongThuc());
                tinhTongTien();
                break;
            }
        }
    }

    private void napBangHoaDon() {
        modelHoaDon.setRowCount(0);
        long tongTien = 0;
        for (HoaDon hd : kho.getHoaDons()) {
            modelHoaDon.addRow(new Object[]{
                    hd.getMaHD(),
                    hd.getSoPhieuSC(),
                    hd.getTenKH(),
                    hd.getSdtKH(),
                    hd.getBienSo(),
                    hd.getNgayThanhToan(),
                    hd.getPhuongThuc(),
                    UiHelper.tien(hd.getTongTien()),
                    hd.getNguoiThu(),
                    hd.getTrangThai()
            });
            if (HoaDon.DA_THANH_TOAN.equals(hd.getTrangThai())) {
                tongTien += hd.getTongTien();
            }
        }
        lblTongDoanhThu.setText("Tổng doanh thu quầy: " + UiHelper.tien(tongTien) + " đ (" + kho.getHoaDons().size() + " hóa đơn)");
    }

    private void huyHoaDon() {
        int r = tblHoaDon.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần hủy trong bảng!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ma = modelHoaDon.getValueAt(r, 0).toString();
        int cf = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn hủy hóa đơn [" + ma + "] không?", "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (cf != JOptionPane.YES_OPTION) return;

        for (HoaDon h : kho.getHoaDons()) {
            if (h.getMaHD().equals(ma)) {
                h.setTrangThai(HoaDon.DA_HUY);
                break;
            }
        }
        napBangHoaDon();
        JOptionPane.showMessageDialog(this, "Đã hủy hóa đơn [" + ma + "].", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void lamMoiForm() {
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
        lblQrSoTien.setText("0 VNĐ");
        lblQrNoiDung.setText("TT " + txtMaHD.getText() + " GARA");
        modelChiTiet.setRowCount(0);
        lblTongChiPhiBang.setText("Tổng chi phí dịch vụ & vật tư: 0 đ");
        txtTienKhachDua.setText("0");
        lblTienThua.setText("0");
        tblHoaDon.clearSelection();
        doiPhuongThuc("Chuyển khoản QR");
    }

    // ===============================================================
    // HÀM ĐỌC SỐ TIỀN THÀNH CHỮ TIẾNG VIỆT CHUẨN
    // ===============================================================
    private static final String[] CHU_SO = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
    private static final String[] DON_VI = {"", "nghìn", "triệu", "tỷ", "nghìn tỷ"};

    public static String docSoThanhChu(long so) {
        if (so == 0) return "Không đồng";
        if (so < 0) return "Âm " + docSoThanhChu(-so);

        String s = String.valueOf(so);
        ArrayList<String> blocks = new ArrayList<>();
        while (s.length() > 0) {
            int len = Math.min(3, s.length());
            blocks.add(0, s.substring(s.length() - len));
            s = s.substring(0, s.length() - len);
        }

        StringBuilder res = new StringBuilder();
        int nBlocks = blocks.size();
        for (int i = 0; i < nBlocks; i++) {
            int val = Integer.parseInt(blocks.get(i));
            if (val > 0) {
                String bStr = docBlock(val, i > 0);
                res.append(bStr).append(" ").append(DON_VI[nBlocks - 1 - i]).append(" ");
            }
        }

        String kq = res.toString().trim().replaceAll("\\s+", " ") + " đồng chẵn.";
        return Character.toUpperCase(kq.charAt(0)) + kq.substring(1);
    }

    private static String docBlock(int n, boolean dayDu) {
        int tram = n / 100;
        int chuc = (n % 100) / 10;
        int donVi = n % 10;
        StringBuilder sb = new StringBuilder();

        if (tram > 0 || dayDu) {
            sb.append(CHU_SO[tram]).append(" trăm ");
        }

        if (chuc > 1) {
            sb.append(CHU_SO[chuc]).append(" mươi ");
            if (donVi == 1) sb.append("mốt");
            else if (donVi == 5) sb.append("lăm");
            else if (donVi > 0) sb.append(CHU_SO[donVi]);
        } else if (chuc == 1) {
            sb.append("mười ");
            if (donVi == 5) sb.append("lăm");
            else if (donVi > 0) sb.append(CHU_SO[donVi]);
        } else if (chuc == 0 && donVi > 0) {
            if (tram > 0 || dayDu) sb.append("lẻ ");
            sb.append(CHU_SO[donVi]);
        }

        return sb.toString().trim();
    }
}
