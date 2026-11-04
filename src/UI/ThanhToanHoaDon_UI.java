package UI;

import DAO.HoaDonBanHang_DAO;
import DAO.HoaDon_DAO;
import Entity.CauHinhHeThong;
import Entity.ChiTietDichVu;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Quầy thu ngân & Lập hóa đơn - The T4 Auto
 * Thiết kế chuẩn UI/UX theo mẫu:
 *  - Bộ lọc chuyển chế độ: Sửa chữa / Dịch vụ xe <-> Bán lẻ Phụ tùng Shop
 *  - Thanh Carousel Xe chờ thanh toán (Chips chọn nhanh có sao ★)
 *  - Khung thông tin hóa đơn & 2 thẻ mini thống kê (Tiền công / Tiền phụ tùng)
 *  - Bảng kê chi phí 2 dòng (Tên hạng mục + Phụ đề KTV / Mã PT) với huy hiệu Loai Chi Phí
 *  - Khung Quầy thu ngân & Phương thức thanh toán (VietQR động 24/7 & Tiền mặt đếm nhanh)
 *  - Thanh thao tác chân trang với phím tắt F9, Ctrl+P
 */
public class ThanhToanHoaDon_UI extends JPanel {

    public static final int CHE_DO_SUA_CHUA = 0;
    public static final int CHE_DO_BAN_LE = 1;
    private int cheDoHienTai = CHE_DO_SUA_CHUA;

    private static String phieuCanThanhToan;
    public static void datPhieuCanThanhToan(String ma) {
        phieuCanThanhToan = ma;
    }

    private final NhanVien nvHienTai;
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final CauHinhHeThong cauHinh = kho.getCauHinh();
    private final HoaDon_DAO hoaDonDAO = new HoaDon_DAO();
    private final HoaDonBanHang_DAO hoaDonBanHangDAO = new HoaDonBanHang_DAO();

    // ==== Header & Chuyển chế độ ====
    private JLabel lblTdeTrang;
    private JLabel lblPhuTrang;
    private JButton btnCheDoSuaChua, btnCheDoBanLe;
    private final CardLayout cardLayoutNghiepVu = new CardLayout();
    private JPanel panelNghiepVu;

    // ==== Thanh Xe chờ thanh toán (Chips) ====
    private JPanel pnlBannerChoTT;
    private JLabel lblSoXeChoTTBadge;
    private JPanel pnlChipsContainer;
    private final List<JButton> dsNutChips = new ArrayList<>();
    private int viTriChipDangChon = 0;

    // ===============================================================
    // CÁC THÀNH PHẦN CHẾ ĐỘ 1: THANH TOÁN SỬA CHỮA
    // ===============================================================
    private final JTextField txtMaHD = new JTextField();
    private final JTextField txtNgay = new JTextField();
    private final JComboBox<String> cboKhachHang = new JComboBox<>();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtBienSo = new JTextField();
    private final JTextField txtPhieuSC = new JTextField();
    private final JTextField txtTienDichVu = new JTextField("0");
    private final JTextField txtTienPhuTung = new JTextField("0");
    private final JTextField txtGiamGia = new JTextField("0");
    private final JTextField txtGhiChu = new JTextField();
    private final List<PhieuTiepNhan> dsPhieuDangChon = new ArrayList<>();

    // Mini thống kê & Huy hiệu thẻ
    private JLabel lblPhieuBadge;
    private JLabel lblTienCongMini;
    private JLabel lblTienPTMini;
    private JLabel lblHangMucBadge;
    private JLabel lblBangKeSubtitle;
    private JLabel lblFooterTienPT;
    private JLabel lblFooterTienCong;
    private JLabel lblTongChiPhiBang;

    // Bảng chi tiết dịch vụ
    private DefaultTableModel modelChiTiet;
    private JTable tblChiTiet;
    private final List<ChiTietDichVu> dsChiTietHienTai = new ArrayList<>();

    // ===============================================================
    // CÁC THÀNH PHẦN CHẾ ĐỘ 2: BÁN LẺ PHỤ TÙNG (SHOP)
    // ===============================================================
    private final JTextField txtMaHDBanLe = new JTextField();
    private final JTextField txtNgayBanLe = new JTextField();
    private final JComboBox<KhachHang> cboKhachHangBanLe = new JComboBox<>();
    private final JTextField txtSdtBanLe = new JTextField();
    private final JTextField txtDiaChiBanLe = new JTextField();
    private final JTextField txtNhanVienBanLe = new JTextField();
    private final JTextField txtTienPhuTungBanLe = new JTextField("0");
    private final JTextField txtGiamGiaBanLe = new JTextField("0");
    private final JTextField txtGhiChuBanLe = new JTextField();
    private final JLabel lblTongTienBanLe = new JLabel("0 VNĐ");
    private final JComboBox<PhuTung> cboPhuTungShop = new JComboBox<>();
    private final JTextField txtTimPTShop = new JTextField();
    private final JSpinner spSoLuongShop = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private DefaultTableModel modelChiTietBanLe;
    private JTable tblChiTietBanLe;
    private final JLabel lblTongPhuTungBanLe = new JLabel("Tổng số phụ tùng: 0 món | Tiền hàng: 0 đ");

    // ===============================================================
    // CÁC THÀNH PHẦN DÙNG CHUNG: QUẦY THU NGÂN & THANH TOÁN
    // ===============================================================
    private final JLabel lblTongTien = new JLabel("0 VNĐ");
    private String phuongThucChon = "Chuyển khoản QR";
    private JButton btnQrTab, btnTienMatTab;
    private final CardLayout cardLayoutTienTra = new CardLayout();
    private JPanel panelTienTra;
    private final CardLayout cardLayoutCotPhai = new CardLayout();
    private JPanel panelCotPhai;

    // Tiền mặt
    private final JLabel lblTienMatCanThu = new JLabel("0 đ");
    private final JLabel lblTienMatKhachDua = new JLabel("0 đ");
    private final JLabel lblTienMatThua = new JLabel("0 đ");
    private final JTextField txtTienKhachDua = new JTextField("0");
    private final JLabel lblTienThua = new JLabel("0 đ");

    // VietQR
    private JPanel qrCanvas;
    private final JLabel lblQrSoTien = new JLabel("0 VNĐ");
    private final JLabel lblQrNoiDung = new JLabel("TT HD015");
    private final JLabel lblQrThuHuong = new JLabel("THE T4 AUTO");

    // Hướng dẫn QR - bước 1 cập nhật số tiền
    private JLabel lblHuongDanQr1;

    public ThanhToanHoaDon_UI(NhanVien nv) {
        this(nv, CHE_DO_SUA_CHUA);
    }

    public ThanhToanHoaDon_UI() {
        this(null, CHE_DO_SUA_CHUA);
    }

    public ThanhToanHoaDon_UI(NhanVien nv, int cheDoKhoiTao) {
        this.nvHienTai = nv;
        this.cheDoHienTai = cheDoKhoiTao;

        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header trang & Chuyển chế độ
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        lblTdeTrang = UiHelper.tieuDeTrang("Quầy thu ngân");
        lblPhuTrang = new JLabel("Thanh toán dịch vụ sửa chữa, quét mã VietQR tự động, quản lý quầy thu ngân và xuất hóa đơn theo mẫu");
        lblPhuTrang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPhuTrang.setForeground(new Color(100, 116, 139));
        lblTdeTrang.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPhuTrang.setAlignmentX(Component.LEFT_ALIGNMENT);

        north.add(lblTdeTrang);
        north.add(Box.createVerticalStrut(4));
        north.add(lblPhuTrang);
        north.add(Box.createVerticalStrut(10));

        // Thanh nút chuyển chế độ
        JPanel thanhChon = taoThanhChonCheDo();
        thanhChon.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(thanhChon);
        north.add(Box.createVerticalStrut(10));

        root.add(north, BorderLayout.NORTH);

        // Nội dung quầy thu ngân
        root.add(taoNoiDungThuNgan(), BorderLayout.CENTER);

        // Đăng ký phím tắt toàn màn hình (F9 thanh toán, Ctrl+P in, F5 làm mới)
        dangKyPhimTat();

        // Nạp dữ liệu ban đầu
        khoiTaoDuLieu();
        chuyenCheDo(cheDoKhoiTao);
    }

    /**
     * Thanh nút chuyển chế độ Sửa chữa / Bán lẻ Shop
     */
    private JPanel taoThanhChonCheDo() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        btnCheDoSuaChua = taoNutChonCheDo(" Thanh toán Sửa chữa / Dịch vụ xe", UiIcon.WRENCH);
        btnCheDoBanLe = taoNutChonCheDo(" Bán lẻ Phụ tùng / Vật tư (Shop)", UiIcon.BOX);

        btnCheDoSuaChua.addActionListener(e -> chuyenCheDo(CHE_DO_SUA_CHUA));
        btnCheDoBanLe.addActionListener(e -> chuyenCheDo(CHE_DO_BAN_LE));

        bar.add(btnCheDoSuaChua);
        bar.add(btnCheDoBanLe);
        return bar;
    }

    private JButton taoNutChonCheDo(String text, String iconName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean act = (this == btnCheDoSuaChua && cheDoHienTai == CHE_DO_SUA_CHUA)
                        || (this == btnCheDoBanLe && cheDoHienTai == CHE_DO_BAN_LE);
                boolean hover = getModel().isRollover();

                Color bg = act ? new Color(37, 99, 235) : (hover ? new Color(241, 245, 249) : Color.WHITE);
                Color border = act ? new Color(37, 99, 235) : (hover ? new Color(148, 163, 184) : new Color(203, 213, 225));

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(act ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();

                setIcon(UiIcon.of(iconName, 15, act ? Color.WHITE : new Color(51, 65, 85)));
                setForeground(act ? Color.WHITE : new Color(51, 65, 85));
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setIcon(UiIcon.of(iconName, 15, new Color(51, 65, 85)));
        btn.setIconTextGap(8);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setRolloverEnabled(true);
        return btn;
    }

    /**
     * Chuyển đổi giữa Chế độ Sửa chữa và Chế độ Bán lẻ Shop
     */
    public void chuyenCheDo(int mode) {
        this.cheDoHienTai = mode;
        if (mode == CHE_DO_SUA_CHUA) {
            cardLayoutNghiepVu.show(panelNghiepVu, "SUA_CHUA");
            lblTdeTrang.setText("Quầy thu ngân");
            lblPhuTrang.setText("Thanh toán dịch vụ sửa chữa, quét mã VietQR tự động, quản lý quầy thu ngân và xuất hóa đơn theo mẫu");
            tinhTongTien();
        } else {
            cardLayoutNghiepVu.show(panelNghiepVu, "BAN_LE");
            lblTdeTrang.setText("Quầy thu ngân — Lập hóa đơn bán lẻ Phụ tùng (Shop)");
            lblPhuTrang.setText("Bán trực tiếp phụ tùng & vật tư tại shop, quét mã VietQR tự động, tự động trừ tồn kho và xuất hóa đơn bán lẻ");
            if (txtMaHDBanLe.getText().isBlank()) {
                txtMaHDBanLe.setText(hoaDonDAO.sinhMaHoaDonTuDong());
            }
            tinhTongTienBanLe();
        }

        if (btnCheDoSuaChua != null && btnCheDoBanLe != null) {
            btnCheDoSuaChua.repaint();
            btnCheDoBanLe.repaint();
        }
    }

    // ===============================================================
    // BỐ CỤC NỘI DUNG CHÍNH
    // ===============================================================
    private JPanel taoNoiDungThuNgan() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);

        // Panel nghiệp vụ có CardLayout: Sửa chữa vs Bán lẻ Shop
        panelNghiepVu = new JPanel(cardLayoutNghiepVu);
        panelNghiepVu.setOpaque(false);

        // Card 1: Nghiệp vụ sửa chữa (Bao gồm thanh Carousel xe chờ TT + 2 thẻ bên trên)
        JPanel cardSuaChua = new JPanel(new BorderLayout(0, 10));
        cardSuaChua.setOpaque(false);

        // 1. Thanh XE CHỜ THANH TOÁN (Chips)
        cardSuaChua.add(taoThanhXeChoThanhToan(), BorderLayout.NORTH);

        // 2. Hai thẻ trên: Bên trái thông tin hóa đơn, bên phải bảng kê chi phí
        JPanel topRow = new JPanel(new BorderLayout(14, 0));
        topRow.setOpaque(false);
        topRow.add(taoFormThongTinSuaChua(), BorderLayout.WEST);
        topRow.add(taoBangChiTietSuaChua(), BorderLayout.CENTER);
        cardSuaChua.add(topRow, BorderLayout.CENTER);

        panelNghiepVu.add(cardSuaChua, "SUA_CHUA");

        // Card 2: Nghiệp vụ bán lẻ phụ tùng (Shop)
        JPanel cardBanLe = new JPanel(new BorderLayout(14, 0));
        cardBanLe.setOpaque(false);
        cardBanLe.add(taoFormThongTinBanLe(), BorderLayout.WEST);
        cardBanLe.add(taoBangChiTietBanLe(), BorderLayout.CENTER);
        panelNghiepVu.add(cardBanLe, "BAN_LE");

        // Panel dưới: QUẦY THU NGÂN & PHƯƠNG THỨC THANH TOÁN (Dùng chung)
        JPanel botPanel = taoBoxQuayThuNgan();

        wrap.add(panelNghiepVu);
        wrap.add(Box.createVerticalStrut(14));
        wrap.add(botPanel);

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

    // ===============================================================
    // 1. THANH XE CHỜ THANH TOÁN (CAROUSEL CHIPS)
    // ===============================================================
    private JPanel taoThanhXeChoThanhToan() {
        pnlBannerChoTT = new JPanel(new BorderLayout(12, 0)) {
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
        pnlBannerChoTT.setOpaque(false);
        pnlBannerChoTT.setBorder(new EmptyBorder(8, 14, 8, 14));

        // Nhãn bên trái có icon tròn màu cam + Tiêu đề + Huy hiệu số lượng xe
        JPanel leftPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPnl.setOpaque(false);

        JLabel lblIcon = new JLabel(UiIcon.of(UiIcon.CLOCK, 16, new Color(217, 119, 6)));

        JLabel lblTitle = new JLabel("XE CHỜ THANH TOÁN:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(15, 23, 42));

        lblSoXeChoTTBadge = new JLabel("0 xe đã hoàn thành");
        lblSoXeChoTTBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSoXeChoTTBadge.setForeground(new Color(217, 119, 6));
        lblSoXeChoTTBadge.setBackground(new Color(254, 243, 199));
        lblSoXeChoTTBadge.setOpaque(true);
        lblSoXeChoTTBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(253, 230, 138), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        leftPnl.add(lblIcon);
        leftPnl.add(lblTitle);
        leftPnl.add(lblSoXeChoTTBadge);

        // Container chứa danh sách nút chips cuộn ngang mượt mà
        pnlChipsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlChipsContainer.setOpaque(false);

        JScrollPane scrollChips = new JScrollPane(pnlChipsContainer);
        scrollChips.setBorder(null);
        scrollChips.setOpaque(false);
        scrollChips.getViewport().setOpaque(false);
        scrollChips.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollChips.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollChips.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 3));
        scrollChips.setPreferredSize(new Dimension(100, 36));

        pnlBannerChoTT.setPreferredSize(new Dimension(0, 48));
        pnlBannerChoTT.add(leftPnl, BorderLayout.WEST);
        pnlBannerChoTT.add(scrollChips, BorderLayout.CENTER);
        return pnlBannerChoTT;
    }

    private void capNhatThanhXeChoTT() {
        if (pnlChipsContainer == null) return;
        pnlChipsContainer.removeAll();
        dsNutChips.clear();

        int count = dsPhieuDangChon.size();
        lblSoXeChoTTBadge.setText(count + " xe đã hoàn thành");

        for (int i = 0; i < count; i++) {
            final int index = i;
            PhieuTiepNhan p = dsPhieuDangChon.get(i);
            String bienSo = (p.getBienSo() != null && !p.getBienSo().isBlank()) ? p.getBienSo() : p.getSoPhieu();
            String tenKh = (p.getHoTen() != null && !p.getHoTen().isBlank()) ? p.getHoTen() : "Khách";

            JButton chip = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean isSelected = (index == viTriChipDangChon);
                    boolean hover = getModel().isRollover();

                    Color bg = isSelected ? new Color(245, 158, 11) : (hover ? new Color(253, 230, 138) : new Color(254, 243, 199));
                    Color border = isSelected ? new Color(217, 119, 6) : new Color(253, 230, 138);

                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(border);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                    if (isSelected) {
                        int sx = getWidth() - 14;
                        int sy = getHeight() / 2;
                        g2.setColor(Color.WHITE);
                        int[] xp = new int[10];
                        int[] yp = new int[10];
                        for (int k = 0; k < 10; k++) {
                            double a = k * Math.PI / 5 - Math.PI / 2;
                            int d = (k % 2 == 0) ? 5 : 2;
                            xp[k] = (int) (sx + d * Math.cos(a));
                            yp[k] = (int) (sy + d * Math.sin(a));
                        }
                        g2.fillPolygon(xp, yp, 10);
                    }

                    g2.dispose();

                    setForeground(isSelected ? Color.WHITE : new Color(146, 64, 14));
                    super.paintComponent(g);
                }
            };
            chip.setText(bienSo + " | " + tenKh);
            chip.setFont(new Font("Segoe UI", i == viTriChipDangChon ? Font.BOLD : Font.PLAIN, 11));
            chip.setFocusPainted(false);
            chip.setContentAreaFilled(false);
            chip.setOpaque(false);
            chip.setBorder(new EmptyBorder(6, 12, 6, i == viTriChipDangChon ? 22 : 12));
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chip.addActionListener(e -> chonXeTheoIndex(index));

            dsNutChips.add(chip);
            pnlChipsContainer.add(chip);
        }

        pnlChipsContainer.revalidate();
        pnlChipsContainer.repaint();
    }

    private void chonXeTheoIndex(int index) {
        if (index < 0 || index >= dsPhieuDangChon.size()) return;
        this.viTriChipDangChon = index;
        if (cboKhachHang.getItemCount() > index + 1) {
            cboKhachHang.setSelectedIndex(index + 1);
        }
        lamMoiStyleChips();
    }

    private void lamMoiStyleChips() {
        for (int i = 0; i < dsNutChips.size(); i++) {
            JButton chip = dsNutChips.get(i);
            PhieuTiepNhan p = dsPhieuDangChon.get(i);
            String bienSo = (p.getBienSo() != null && !p.getBienSo().isBlank()) ? p.getBienSo() : p.getSoPhieu();
            String tenKh = (p.getHoTen() != null && !p.getHoTen().isBlank()) ? p.getHoTen() : "Khách";
            boolean sel = (i == viTriChipDangChon);
            chip.setText(bienSo + " | " + tenKh);
            chip.setFont(new Font("Segoe UI", sel ? Font.BOLD : Font.PLAIN, 11));
            chip.setBorder(new EmptyBorder(6, 12, 6, sel ? 22 : 12));
            chip.repaint();
        }
    }

    // ===============================================================
    // 2. THẺ TRÁI: THÔNG TIN HÓA ĐƠN & SỬA CHỮA
    // ===============================================================
    private JPanel taoFormThongTinSuaChua() {
        JPanel card = taoCardTrang(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(480, 430));

        // Header thẻ: Icon tài liệu + Tiêu đề + Huy hiệu Phiếu: TNxxx
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JPanel leftHead = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHead.setOpaque(false);
        leftHead.add(new JLabel(UiIcon.of(UiIcon.FILE, 16, new Color(37, 99, 235))));

        JLabel lblTitle = new JLabel("Thông tin hóa đơn & sửa chữa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(15, 23, 42));
        leftHead.add(lblTitle);

        lblPhieuBadge = new JLabel("Phiếu: ---");
        lblPhieuBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPhieuBadge.setForeground(new Color(37, 99, 235));
        lblPhieuBadge.setBackground(new Color(239, 246, 255));
        lblPhieuBadge.setOpaque(true);
        lblPhieuBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(3, 10, 3, 10)
        ));

        head.add(leftHead, BorderLayout.WEST);
        head.add(lblPhieuBadge, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Body form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        txtMaHD.setEditable(false);
        txtNgay.setEditable(false);
        txtSdt.setEditable(false);
        txtBienSo.setEditable(false);
        txtPhieuSC.setEditable(false);

        UiHelper.trangTriInput(txtMaHD);
        UiHelper.trangTriInput(txtNgay);
        UiHelper.trangTriInput(txtSdt);
        UiHelper.trangTriInput(txtBienSo);
        UiHelper.trangTriInput(txtGiamGia);
        UiHelper.trangTriInput(txtGhiChu);

        txtGhiChu.putClientProperty("placeholder", "Ghi chú hóa đơn hoặc yêu cầu khách...");
        txtGiamGia.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { tinhTongTien(); }
        });

        cboKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboKhachHang.setBackground(Color.WHITE);
        cboKhachHang.addActionListener(e -> {
            int sel = cboKhachHang.getSelectedIndex();
            if (sel > 0 && sel <= dsPhieuDangChon.size()) {
                viTriChipDangChon = sel - 1;
                lamMoiStyleChips();
            }
            chonKhachHang();
        });

        // Hàng 1: Mã HĐ + Ngày lập hóa đơn
        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);
        row1.add(taoInputCoNhan("Mã HĐ", txtMaHD));
        row1.add(taoInputCoNhan("Ngày lập hóa đơn", txtNgay));
        form.add(row1);
        form.add(Box.createVerticalStrut(8));

        // Hàng 2: Khách hàng & Phương tiện * (Combobox đầy đủ chiều rộng)
        form.add(taoInputCoNhan("Khách hàng & Phương tiện *", cboKhachHang));
        form.add(Box.createVerticalStrut(8));

        // Hàng 3: Số điện thoại + Biển số xe
        JPanel row3 = new JPanel(new GridLayout(1, 2, 10, 0));
        row3.setOpaque(false);
        row3.add(taoInputCoNhan("Số điện thoại", txtSdt));
        row3.add(taoInputCoNhan("Biển số xe", txtBienSo));
        form.add(row3);
        form.add(Box.createVerticalStrut(8));

        // Hàng 4: Hai thẻ mini thống kê (Tiền công dịch vụ / Tiền phụ tùng vật tư)
        JPanel rowMini = new JPanel(new GridLayout(1, 2, 10, 0));
        rowMini.setOpaque(false);

        lblTienCongMini = new JLabel("0 đ");
        lblTienPTMini = new JLabel("0 đ");

        rowMini.add(taoTheMiniThongKe("Tiền công dịch vụ", lblTienCongMini));
        rowMini.add(taoTheMiniThongKe("Tiền phụ tùng / vật tư", lblTienPTMini));
        form.add(rowMini);
        form.add(Box.createVerticalStrut(8));

        // Hàng 5: Giảm giá (%) + Ghi chú thanh toán
        JPanel row5 = new JPanel(new GridLayout(1, 2, 10, 0));
        row5.setOpaque(false);

        JPanel pnlGiamGia = new JPanel(new BorderLayout(4, 0));
        pnlGiamGia.setOpaque(false);
        pnlGiamGia.add(txtGiamGia, BorderLayout.CENTER);
        JLabel lblPt = new JLabel("%");
        lblPt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPt.setForeground(new Color(100, 116, 139));
        pnlGiamGia.add(lblPt, BorderLayout.EAST);

        row5.add(taoInputCoNhan("Giảm giá (%)", pnlGiamGia));
        row5.add(taoInputCoNhan("Ghi chú thanh toán", txtGhiChu));
        form.add(row5);

        card.add(form, BorderLayout.CENTER);

        // Banner chân thẻ: Trạng thái thu ngân + Tổng phải thu
        JPanel bannerDuoi = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 253, 244)); // Emerald 50
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(187, 247, 208)); // Emerald 200
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        bannerDuoi.setOpaque(false);
        bannerDuoi.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel leftTt = new JPanel();
        leftTt.setLayout(new BoxLayout(leftTt, BoxLayout.Y_AXIS));
        leftTt.setOpaque(false);

        JLabel lblTtNhan = new JLabel("TRẠNG THÁI THU NGÂN");
        lblTtNhan.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTtNhan.setForeground(new Color(100, 116, 139));

        JLabel lblTtVal = new JLabel("● Đã thanh toán (Mặc định khi thu tiền)");
        lblTtVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTtVal.setForeground(new Color(22, 163, 74));

        leftTt.add(lblTtNhan);
        leftTt.add(Box.createVerticalStrut(2));
        leftTt.add(lblTtVal);

        JPanel rightTong = new JPanel();
        rightTong.setLayout(new BoxLayout(rightTong, BoxLayout.Y_AXIS));
        rightTong.setOpaque(false);

        JLabel lblTongNhan = new JLabel("Tổng phải thu");
        lblTongNhan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTongNhan.setForeground(new Color(100, 116, 139));
        lblTongNhan.setAlignmentX(Component.RIGHT_ALIGNMENT);

        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongTien.setForeground(new Color(30, 58, 138));
        lblTongTien.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightTong.add(lblTongNhan);
        rightTong.add(lblTongTien);

        bannerDuoi.add(leftTt, BorderLayout.WEST);
        bannerDuoi.add(rightTong, BorderLayout.EAST);

        card.add(bannerDuoi, BorderLayout.SOUTH);
        return card;
    }

    private JPanel taoInputCoNhan(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(71, 85, 105));
        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel taoTheMiniThongKe(String title, JLabel lblValue) {
        JPanel box = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblT.setForeground(new Color(100, 116, 139));

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblValue.setForeground(new Color(15, 23, 42));

        box.add(lblT, BorderLayout.NORTH);
        box.add(lblValue, BorderLayout.CENTER);
        return box;
    }

    // ===============================================================
    // 3. THẺ PHẢI: BẢNG KÊ CHI PHÍ TIỀN CÔNG & PHỤ TÙNG THAY THẾ
    // ===============================================================
    private JPanel taoBangChiTietSuaChua() {
        JPanel card = taoCardTrang(new BorderLayout(0, 10));

        // Header bảng kê: Icon danh sách + Tiêu đề + Phụ đề chi tiết + Huy hiệu X hạng mục
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JPanel leftHead = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHead.setOpaque(false);
        leftHead.add(new JLabel(UiIcon.of(UiIcon.WRENCH, 16, new Color(99, 102, 241))));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel lblTitle = new JLabel("Bảng kê chi phí tiền công & phụ tùng thay thế");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(15, 23, 42));

        lblBangKeSubtitle = new JLabel("Chi tiết dịch vụ từ lệnh sửa chữa ---");
        lblBangKeSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBangKeSubtitle.setForeground(new Color(100, 116, 139));

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblBangKeSubtitle);

        leftHead.add(titleBlock);

        lblHangMucBadge = new JLabel("0 hạng mục");
        lblHangMucBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHangMucBadge.setForeground(new Color(71, 85, 105));
        lblHangMucBadge.setBackground(new Color(241, 245, 249));
        lblHangMucBadge.setOpaque(true);
        lblHangMucBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(3, 10, 3, 10)
        ));

        head.add(leftHead, BorderLayout.WEST);
        head.add(lblHangMucBadge, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Bảng dữ liệu JTable với Renderer tùy biến hiển thị 2 dòng
        modelChiTiet = new DefaultTableModel(
                new Object[]{"LOẠI CHI PHÍ", "TÊN HẠNG MỤC / PHỤ TÙNG", "ĐVT", "SL", "ĐƠN GIÁ (Đ)", "THÀNH TIỀN (Đ)"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblChiTiet.setFillsViewportHeight(true);
        tblChiTiet.setRowHeight(48);
        tblChiTiet.setShowGrid(false);
        tblChiTiet.setIntercellSpacing(new Dimension(0, 0));
        tblChiTiet.setSelectionBackground(new Color(239, 246, 255));
        tblChiTiet.setSelectionForeground(new Color(15, 23, 42));

        // Định dạng header bảng
        tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblChiTiet.getTableHeader().setBackground(new Color(248, 250, 252));
        tblChiTiet.getTableHeader().setForeground(new Color(100, 116, 139));
        tblChiTiet.getTableHeader().setPreferredSize(new Dimension(0, 34));
        tblChiTiet.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        // Cài đặt độ rộng cột
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(95);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(270);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(50);
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(45);
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(105);
        tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(115);

        // Renderer Cột 0: Huy hiệu Loại Chi Phí
        tblChiTiet.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
                p.setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                String txt = String.valueOf(val != null ? val : "");
                boolean isPT = txt.toLowerCase().contains("phụ tùng");

                JLabel tag = new JLabel(isPT ? "Phụ tùng" : "Tiền công");
                tag.setFont(new Font("Segoe UI", Font.BOLD, 11));
                tag.setForeground(isPT ? new Color(37, 99, 235) : new Color(5, 150, 105));
                tag.setBackground(isPT ? new Color(239, 246, 255) : new Color(236, 253, 245));
                tag.setOpaque(true);
                tag.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isPT ? new Color(219, 234, 254) : new Color(167, 243, 208), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
                p.add(tag);
                p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
                return p;
            }
        });

        // Renderer Cột 1: Tên hạng mục (2 dòng: Dòng chính in đậm + Dòng phụ in mờ)
        tblChiTiet.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                p.setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(6, 10, 6, 10)
                ));

                String ten = String.valueOf(val != null ? val : "");
                String sub = "";
                if (r < dsChiTietHienTai.size()) {
                    sub = dsChiTietHienTai.get(r).getGhiChuPhu();
                }

                JLabel lblTen = new JLabel(ten);
                lblTen.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblTen.setForeground(new Color(15, 23, 42));

                JLabel lblSub = new JLabel(sub);
                lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                lblSub.setForeground(new Color(100, 116, 139));

                p.add(lblTen);
                p.add(Box.createVerticalStrut(2));
                p.add(lblSub);
                return p;
            }
        });

        // Renderer Cột 2 & 3 (ĐVT, SL): Canh giữa
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", (c == 3) ? Font.BOLD : Font.PLAIN, 12));
                l.setForeground(new Color(51, 65, 85));
                l.setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));
                return l;
            }
        };
        tblChiTiet.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(centerRender);

        // Renderer Cột 4 & 5 (Đơn giá, Thành tiền): Canh phải
        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(new Font("Segoe UI", (c == 5) ? Font.BOLD : Font.PLAIN, 12));
                l.setForeground(new Color(15, 23, 42));
                l.setBackground(sel ? new Color(239, 246, 255) : Color.WHITE);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        new EmptyBorder(0, 0, 0, 12)
                ));
                return l;
            }
        };
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(rightRender);
        tblChiTiet.getColumnModel().getColumn(5).setCellRenderer(rightRender);

        JScrollPane sp = new JScrollPane(tblChiTiet);
        sp.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249), 1));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(0, 240));
        card.add(sp, BorderLayout.CENTER);

        // Footer thanh tổng bảng kê
        JPanel footer = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 14, 8, 14));

        JPanel leftFoot = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftFoot.setOpaque(false);

        lblFooterTienPT = new JLabel("Tiền phụ tùng: 0 đ");
        lblFooterTienPT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooterTienPT.setForeground(new Color(71, 85, 105));

        lblFooterTienCong = new JLabel("Tiền công dịch vụ: 0 đ");
        lblFooterTienCong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooterTienCong.setForeground(new Color(71, 85, 105));

        leftFoot.add(lblFooterTienPT);
        leftFoot.add(lblFooterTienCong);

        lblTongChiPhiBang = new JLabel("Tổng chi phí dịch vụ & vật tư: 0 đ");
        lblTongChiPhiBang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongChiPhiBang.setForeground(new Color(30, 58, 138));

        footer.add(leftFoot, BorderLayout.WEST);
        footer.add(lblTongChiPhiBang, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    // ===============================================================
    // 4. THẺ DƯỚI: QUẦY THU NGÂN & PHƯƠNG THỨC THANH TOÁN
    // ===============================================================
    private JPanel taoBoxQuayThuNgan() {
        JPanel box = taoCardTrang(new BorderLayout(0, 14));
        box.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header Quầy thu ngân: Tiêu đề + Tabs Chuyển khoản QR / Tiền mặt
        JPanel headRow = new JPanel(new BorderLayout());
        headRow.setOpaque(false);

        JPanel leftTitle = new JPanel();
        leftTitle.setLayout(new BoxLayout(leftTitle, BoxLayout.Y_AXIS));
        leftTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẦY THU NGÂN & PHƯƠNG THỨC THANH TOÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Chọn kênh thanh toán thích hợp và xuất hóa đơn giá trị gia tăng");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        leftTitle.add(lblTitle);
        leftTitle.add(Box.createVerticalStrut(2));
        leftTitle.add(lblSub);

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tabs.setOpaque(false);

        btnQrTab = taoNutTabPhuongThuc(" Chuyển khoản QR (VietQR)", UiIcon.REFRESH, true);
        btnTienMatTab = taoNutTabPhuongThuc(" Tiền mặt", UiIcon.BILL, false);

        btnQrTab.addActionListener(e -> doiPhuongThuc("Chuyển khoản QR"));
        btnTienMatTab.addActionListener(e -> doiPhuongThuc("Tiền mặt"));

        tabs.add(btnQrTab);
        tabs.add(btnTienMatTab);

        headRow.add(leftTitle, BorderLayout.WEST);
        headRow.add(tabs, BorderLayout.EAST);
        box.add(headRow, BorderLayout.NORTH);

        // Body: 2 Cột (Trái: Hướng dẫn QR hoặc Đếm tiền mặt | Phải: Mã QR hoặc Hóa đơn tiền mặt)
        JPanel bodyRow = new JPanel(new GridLayout(1, 2, 20, 0));
        bodyRow.setOpaque(false);

        bodyRow.add(taoCotTraTrai());

        panelCotPhai = new JPanel(cardLayoutCotPhai);
        panelCotPhai.setOpaque(false);
        panelCotPhai.add(taoCotVietQR(), "QR");
        panelCotPhai.add(taoCotTienMat(), "CASH");
        cardLayoutCotPhai.show(panelCotPhai, "QR");
        bodyRow.add(panelCotPhai);

        box.add(bodyRow, BorderLayout.CENTER);

        // Footer quầy: Phím tắt + Các nút thao tác (Làm mới, In hóa đơn, Xác nhận thanh toán)
        JPanel footerRow = new JPanel(new BorderLayout());
        footerRow.setOpaque(false);
        footerRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Phím tắt bên trái
        JLabel lblShortcuts = new JLabel("<html><font color='#64748B'>Nhấn phím tắt </font><b style='background:#F1F5F9;padding:2px 6px;border-radius:4px;'>F9</b><font color='#64748B'> để hoàn tất thanh toán • </font><b style='background:#F1F5F9;padding:2px 6px;border-radius:4px;'>Ctrl + P</b><font color='#64748B'> để in</font></html>");
        lblShortcuts.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerRow.add(lblShortcuts, BorderLayout.WEST);

        // Các nút hành động bên phải
        JPanel actionRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRight.setOpaque(false);

        JButton btnLamMoi = taoNutHanhDong(" Làm mới", UiIcon.of(UiIcon.REFRESH, 15, new Color(71, 85, 105)), new Color(241, 245, 249), new Color(203, 213, 225), new Color(71, 85, 105), e -> lamMoiForm());
        btnLamMoi.setPreferredSize(new Dimension(115, 38));

        JButton btnInHoaDon = taoNutHanhDong(" In hóa đơn", UiIcon.of(UiIcon.PRINT, 15, Color.WHITE), new Color(37, 99, 235), new Color(37, 99, 235), Color.WHITE, e -> xemVaInHoaDon());
        btnInHoaDon.setPreferredSize(new Dimension(135, 38));

        JButton btnXacNhan = taoNutHanhDong(" XÁC NHẬN THANH TOÁN (F9)", UiIcon.of(UiIcon.CHECK, 15, Color.WHITE), new Color(22, 163, 74), new Color(22, 163, 74), Color.WHITE, e -> thucHienThanhToan());
        btnXacNhan.setPreferredSize(new Dimension(275, 38));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));

        actionRight.add(btnLamMoi);
        actionRight.add(btnInHoaDon);
        actionRight.add(btnXacNhan);

        footerRow.add(actionRight, BorderLayout.EAST);
        box.add(footerRow, BorderLayout.SOUTH);

        return box;
    }

    private JPanel taoCotTraTrai() {
        panelTienTra = new JPanel(cardLayoutTienTra);
        panelTienTra.setOpaque(false);

        // Subcard 1: Hướng dẫn QR theo mẫu
        JPanel qrCard = new JPanel();
        qrCard.setLayout(new BoxLayout(qrCard, BoxLayout.Y_AXIS));
        qrCard.setOpaque(false);

        JPanel rowTdeHd = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowTdeHd.setOpaque(false);
        rowTdeHd.add(new JLabel(UiIcon.of(UiIcon.REFRESH, 16, new Color(37, 99, 235))));
        JLabel lblTdeHd = new JLabel("HƯỚNG DẪN THANH TOÁN VIETQR");
        lblTdeHd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTdeHd.setForeground(new Color(30, 58, 138));
        rowTdeHd.add(lblTdeHd);
        qrCard.add(rowTdeHd);
        qrCard.add(Box.createVerticalStrut(12));

        // 3 Bước tròn số (1), (2), (3)
        lblHuongDanQr1 = new JLabel("<html><b>Mã QR động:</b> Tự động cập nhật đúng chính xác số tiền cần thanh toán <b>0 VNĐ</b> và cú pháp hóa đơn.</html>");
        qrCard.add(taoDongBuocHuongDan("1", lblHuongDanQr1));
        qrCard.add(Box.createVerticalStrut(8));

        JLabel lblHd2 = new JLabel("<html><b>Mọi Ngân hàng & Ví điện tử:</b> Hỗ trợ VCB, MBBank, BIDV, VietinBank, Techcombank, MoMo, ZaloPay...</html>");
        qrCard.add(taoDongBuocHuongDan("2", lblHd2));
        qrCard.add(Box.createVerticalStrut(8));

        JLabel lblHd3 = new JLabel("<html><b>Không cần thối tiền thừa:</b> Hệ thống tự động báo \"Thành công\" ngay khi tiền vào tài khoản.</html>");
        qrCard.add(taoDongBuocHuongDan("3", lblHd3));
        qrCard.add(Box.createVerticalStrut(14));

        // Huy hiệu bảo mật Napas & VietinBank an toàn
        JPanel rowSafe = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rowSafe.setOpaque(false);
        rowSafe.add(new JLabel(UiIcon.of(UiIcon.SHIELD, 15, new Color(22, 163, 74))));
        JLabel lblSafe = new JLabel("Kết nối Napas 24/7 & VietinBank an toàn");
        lblSafe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSafe.setForeground(new Color(22, 163, 74));
        rowSafe.add(lblSafe);
        qrCard.add(rowSafe);

        panelTienTra.add(qrCard, "QR");

        // Subcard 2: Tiền mặt đếm nhanh
        JPanel cashCard = new JPanel();
        cashCard.setLayout(new BoxLayout(cashCard, BoxLayout.Y_AXIS));
        cashCard.setOpaque(false);

        JPanel rowNhap = new JPanel(new BorderLayout(8, 0));
        rowNhap.setOpaque(false);
        JLabel lblNhapKd = new JLabel("Tiền khách đưa (VNĐ):");
        lblNhapKd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNhapKd.setForeground(new Color(15, 23, 42));

        txtTienKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtTienKhachDua.setPreferredSize(new Dimension(180, 36));
        UiHelper.trangTriInput(txtTienKhachDua);
        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { tinhTienThua(); }
            @Override public void removeUpdate(DocumentEvent e) { tinhTienThua(); }
            @Override public void changedUpdate(DocumentEvent e) { tinhTienThua(); }
        });
        rowNhap.add(lblNhapKd, BorderLayout.WEST);
        rowNhap.add(txtTienKhachDua, BorderLayout.CENTER);
        cashCard.add(rowNhap);
        cashCard.add(Box.createVerticalStrut(8));

        // Phím đếm tiền nhanh
        JPanel quickButtons = new JPanel(new GridLayout(2, 3, 6, 6));
        quickButtons.setOpaque(false);
        quickButtons.add(taoNutDemNhanh("10,000 đ", 10000));
        quickButtons.add(taoNutDemNhanh("20,000 đ", 20000));
        quickButtons.add(taoNutDemNhanh("50,000 đ", 50000));
        quickButtons.add(taoNutDemNhanh("100,000 đ", 100000));
        quickButtons.add(taoNutDemNhanh("200,000 đ", 200000));
        quickButtons.add(taoNutDemNhanh("500,000 đ", 500000));
        cashCard.add(quickButtons);
        cashCard.add(Box.createVerticalStrut(8));

        JPanel rowThua = new JPanel(new BorderLayout(8, 0));
        rowThua.setOpaque(false);
        JLabel lblTdeThua = new JLabel("Tiền thối lại khách:");
        lblTdeThua.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTdeThua.setForeground(new Color(15, 23, 42));

        lblTienThua.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTienThua.setForeground(new Color(22, 163, 74));
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        rowThua.add(lblTdeThua, BorderLayout.WEST);
        rowThua.add(lblTienThua, BorderLayout.CENTER);
        cashCard.add(rowThua);

        panelTienTra.add(cashCard, "CASH");
        return panelTienTra;
    }

    private JPanel taoDongBuocHuongDan(String so, JLabel lblText) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblSo = new JLabel(so, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(37, 99, 235));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSo.setPreferredSize(new Dimension(22, 22));
        lblSo.setMinimumSize(new Dimension(22, 22));
        lblSo.setMaximumSize(new Dimension(22, 22));
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSo.setForeground(Color.WHITE);

        JPanel wrapSo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        wrapSo.setOpaque(false);
        wrapSo.add(lblSo);

        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblText.setForeground(new Color(71, 85, 105));

        row.add(wrapSo, BorderLayout.WEST);
        row.add(lblText, BorderLayout.CENTER);
        return row;
    }

    private JPanel taoCotVietQR() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
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
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Header nhỏ trong hộp QR
        JPanel headBox = new JPanel(new BorderLayout());
        headBox.setOpaque(false);

        JLabel lblTitle = new JLabel("MÃ VIETQR TỰ ĐỘNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblNapas = new JLabel("24/7 NAPAS");
        lblNapas.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblNapas.setForeground(Color.WHITE);
        lblNapas.setBackground(new Color(15, 23, 42));
        lblNapas.setOpaque(true);
        lblNapas.setBorder(new EmptyBorder(2, 8, 2, 8));

        headBox.add(lblTitle, BorderLayout.WEST);
        headBox.add(lblNapas, BorderLayout.EAST);
        card.add(headBox, BorderLayout.NORTH);

        // Khung vẽ mã QR sắc nét chuẩn đồ họa
        qrCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h) - 6;
                int ox = (w - size) / 2;
                int oy = (h - size) / 2;

                long tong = layTongTienHienTai();
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(ox, oy, size, size, 10, 10);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(ox, oy, size, size, 10, 10);

                if (tong <= 0) {
                    g2.setColor(new Color(148, 163, 184));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    String s1 = "[ CHƯA CÓ SỐ TIỀN ]";
                    g2.drawString(s1, ox + (size - fm.stringWidth(s1)) / 2, oy + size / 2 - 4);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    fm = g2.getFontMetrics();
                    String s2 = "Chọn xe để hiện mã QR";
                    g2.drawString(s2, ox + (size - fm.stringWidth(s2)) / 2, oy + size / 2 + 14);
                    g2.dispose();
                    return;
                }

                // Vẽ 3 ô định vị Finder Patterns
                veOVuongDinhVi(g2, ox + 10, oy + 10, 32);
                veOVuongDinhVi(g2, ox + size - 42, oy + 10, 32);
                veOVuongDinhVi(g2, ox + 10, oy + size - 42, 32);

                // Vẽ các điểm ma trận QR
                g2.setColor(new Color(15, 23, 42));
                int step = 6;
                String ma = (cheDoHienTai == CHE_DO_SUA_CHUA ? txtMaHD.getText() : txtMaHDBanLe.getText());
                int seed = (ma + "_" + tong).hashCode();
                for (int x = ox + 10; x < ox + size - 10; x += step) {
                    for (int y = oy + 10; y < oy + size - 10; y += step) {
                        if (x < ox + 46 && y < oy + 46) continue;
                        if (x > ox + size - 46 && y < oy + 46) continue;
                        if (x < ox + 46 && y > oy + size - 46) continue;
                        if (x > ox + size / 2 - 20 && x < ox + size / 2 + 20 && y > oy + size / 2 - 20 && y < oy + size / 2 + 20) continue;

                        int hash = Math.abs((x * 37 + y * 19 + seed) % 100);
                        if (hash > 44) {
                            g2.fillRect(x, y, step - 1, step - 1);
                        }
                    }
                }

                // Logo VietQR ở chính giữa
                int cx = ox + size / 2;
                int cy = oy + size / 2;
                int badgeW = 48, badgeH = 22;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(cx - badgeW / 2 - 2, cy - badgeH / 2 - 2, badgeW + 4, badgeH + 4, 6, 6);
                g2.setColor(new Color(30, 58, 138));
                g2.fillRoundRect(cx - badgeW / 2, cy - badgeH / 2, badgeW, badgeH, 4, 4);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fmB = g2.getFontMetrics();
                String vqr = "VIETQR";
                g2.drawString(vqr, cx - fmB.stringWidth(vqr) / 2, cy + 4);

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
        card.add(qrCanvas, BorderLayout.CENTER);

        // Thông tin bên dưới mã QR
        JPanel infoQr = new JPanel(new GridLayout(3, 1, 0, 3));
        infoQr.setOpaque(false);

        infoQr.add(taoRowThongTinQR("Số tiền:", lblQrSoTien, true, false));
        infoQr.add(taoRowThongTinQR("Nội dung:", lblQrNoiDung, false, true));
        infoQr.add(taoRowThongTinQR("Thụ hưởng:", lblQrThuHuong, false, false));

        card.add(infoQr, BorderLayout.SOUTH);
        return card;
    }

    private JPanel taoRowThongTinQR(String label, JLabel lblVal, boolean boldVal, boolean blueVal) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(100, 116, 139));

        lblVal.setFont(new Font("Segoe UI", boldVal ? Font.BOLD : Font.PLAIN, 12));
        lblVal.setForeground(blueVal ? new Color(37, 99, 235) : new Color(15, 23, 42));

        row.add(l, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.EAST);
        return row;
    }

    private JPanel taoCotTienMat() {
        JPanel card = new JPanel(new BorderLayout(8, 8)) {
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
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblTitle = new JLabel("QUẦY THU TIỀN MẶT TẠI CHỖ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(15, 23, 42));
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 2, 6, 10));
        grid.setOpaque(false);

        JLabel l1 = new JLabel("Tổng tiền cần thu:");
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTienMatCanThu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTienMatCanThu.setForeground(new Color(15, 23, 42));

        JLabel l2 = new JLabel("Khách đưa:");
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTienMatKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTienMatKhachDua.setForeground(new Color(37, 99, 235));

        JLabel l3 = new JLabel("Tiền thối lại:");
        l3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTienMatThua.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTienMatThua.setForeground(new Color(22, 163, 74));

        grid.add(l1); grid.add(lblTienMatCanThu);
        grid.add(l2); grid.add(lblTienMatKhachDua);
        grid.add(l3); grid.add(lblTienMatThua);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JButton taoNutTabPhuongThuc(String text, String iconName, boolean active) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean act = Boolean.TRUE.equals(getClientProperty("activeTab"));
                boolean hover = getModel().isRollover();

                Color bg = act ? new Color(37, 99, 235) : (hover ? new Color(226, 232, 240) : new Color(241, 245, 249));
                Color border = act ? new Color(37, 99, 235) : new Color(203, 213, 225);

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();

                setIcon(UiIcon.of(iconName, 14, act ? Color.WHITE : new Color(71, 85, 105)));
                setForeground(act ? Color.WHITE : new Color(71, 85, 105));
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setIcon(UiIcon.of(iconName, 14, active ? Color.WHITE : new Color(71, 85, 105)));
        btn.setIconTextGap(6);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        setTabStyle(btn, active);
        return btn;
    }

    private void setTabStyle(JButton btn, boolean active) {
        if (btn == null) return;
        btn.putClientProperty("activeTab", active);
        btn.repaint();
    }

    private void doiPhuongThuc(String pt) {
        phuongThucChon = pt;
        boolean isQr = "Chuyển khoản QR".equals(pt);
        setTabStyle(btnQrTab, isQr);
        setTabStyle(btnTienMatTab, !isQr);

        cardLayoutTienTra.show(panelTienTra, isQr ? "QR" : "CASH");
        cardLayoutCotPhai.show(panelCotPhai, isQr ? "QR" : "CASH");

        long tong = layTongTienHienTai();
        if (isQr) {
            dongBoQR(tong);
        } else {
            dongBoTienMat(tong);
            tinhTienThua();
        }
    }

    private JButton taoNutHanhDong(String text, Icon icon, Color bg, Color border, Color fg, java.awt.event.ActionListener al) {
        JButton b = new JButton(text, icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                boolean pressed = getModel().isPressed();

                Color c = pressed ? bg.darker() : (hover ? bg.brighter() : bg);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (al != null) b.addActionListener(al);
        return b;
    }

    private JButton taoNutDemNhanh(String nhan, long soTien) {
        JButton b = new JButton(nhan) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean pressed = getModel().isPressed();
                boolean hover = getModel().isRollover();
                Color bg = pressed ? new Color(226, 232, 240) : (hover ? new Color(235, 240, 248) : new Color(241, 245, 249));
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(new Color(30, 41, 59));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(4, 6, 4, 6));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            try {
                String raw = txtTienKhachDua.getText().replace(",", "").replace(".", "").trim();
                long hienTai = raw.isEmpty() ? 0 : Long.parseLong(raw);
                long moi = hienTai + soTien;
                txtTienKhachDua.setText(String.format("%,d", moi));
                tinhTienThua();
            } catch (Exception ex) {
                txtTienKhachDua.setText(String.format("%,d", soTien));
                tinhTienThua();
            }
        });
        return b;
    }

    private JPanel taoCardTrang(LayoutManager lm) {
        JPanel p = new JPanel(lm) {
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
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        return p;
    }

    // ===============================================================
    // CÁC HÀM XỬ LÝ DỮ LIỆU SỬA CHỮA
    // ===============================================================
    private void khoiTaoDuLieu() {
        txtMaHD.setText(hoaDonDAO.sinhMaHoaDonTuDong());
        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Nạp danh sách xe cần thanh toán
        cboKhachHang.removeAllItems();
        dsPhieuDangChon.clear();
        cboKhachHang.addItem("-- Chọn khách hàng có xe cần thanh toán --");

        for (PhieuTiepNhan ptn : kho.getPhieuTiepNhans()) {
            String td = ptn.getTienDo() != null ? ptn.getTienDo() : "";
            // Cho phép thanh toán các xe đã hoàn thành hoặc đang xử lý dịch vụ
            boolean hopLe = td.contains("Hoàn thành") || td.contains("hoàn thành") || td.contains("sửa") || td.contains("Sửa")
                    || td.contains("Chờ") || td.equalsIgnoreCase("HOAN_THANH") || td.equalsIgnoreCase("DANG_SUA") || td.equalsIgnoreCase("CHO_THANH_TOAN");
            if (hopLe) {
                boolean daLap = false;
                for (HoaDon hd : kho.getHoaDons()) {
                    if (ptn.getSoPhieu().equalsIgnoreCase(hd.getSoPhieuSC()) && !HoaDon.DA_HUY.equals(hd.getTrangThai())) {
                        daLap = true;
                        break;
                    }
                }
                if (!daLap) {
                    dsPhieuDangChon.add(ptn);
                    String hangDongXe = (ptn.getHangXe() != null && !ptn.getHangXe().isBlank()) ? (" (" + ptn.getHangXe() + " " + ptn.getLoaiXe() + ")") : "";
                    cboKhachHang.addItem(ptn.getHoTen() + " — Xe " + ptn.getBienSo() + hangDongXe);
                }
            }
        }

        capNhatThanhXeChoTT();

        if (phieuCanThanhToan != null && !phieuCanThanhToan.trim().isEmpty()) {
            chonPhieuCuThe(phieuCanThanhToan);
            phieuCanThanhToan = null;
        } else if (cboKhachHang.getSelectedIndex() <= 0 && cboKhachHang.getItemCount() > 1) {
            chonXeTheoIndex(0);
        }

        // Khởi tạo Bán lẻ Shop
        txtMaHDBanLe.setText(hoaDonDAO.sinhMaHoaDonTuDong());
        txtNgayBanLe.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtNhanVienBanLe.setText(nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân");
        napKhachHangBanLe();
        napPhuTungShop();
    }

    private void chonPhieuCuThe(String soPhieu) {
        if (soPhieu == null || soPhieu.trim().isEmpty()) return;
        String pTarget = soPhieu.trim().toUpperCase();
        for (int i = 0; i < dsPhieuDangChon.size(); i++) {
            PhieuTiepNhan p = dsPhieuDangChon.get(i);
            if (p.getSoPhieu().equalsIgnoreCase(pTarget) || (p.getBienSo() != null && p.getBienSo().equalsIgnoreCase(pTarget))) {
                chonXeTheoIndex(i);
                return;
            }
        }
    }

    private void chonKhachHang() {
        int idx = cboKhachHang.getSelectedIndex();
        if (idx <= 0 || idx > dsPhieuDangChon.size()) {
            txtSdt.setText("");
            txtBienSo.setText("");
            txtPhieuSC.setText("");
            lblPhieuBadge.setText("Phiếu: ---");
            lblBangKeSubtitle.setText("Chi tiết dịch vụ từ lệnh sửa chữa ---");
            lblHangMucBadge.setText("0 hạng mục");
            lblTienCongMini.setText("0 đ");
            lblTienPTMini.setText("0 đ");
            lblFooterTienPT.setText("Tiền phụ tùng: 0 đ");
            lblFooterTienCong.setText("Tiền công dịch vụ: 0 đ");
            lblTongChiPhiBang.setText("Tổng chi phí dịch vụ & vật tư: 0 đ");
            lblTongTien.setText("0 VNĐ");
            modelChiTiet.setRowCount(0);
            dsChiTietHienTai.clear();
            tinhTongTien();
            return;
        }

        PhieuTiepNhan ptn = dsPhieuDangChon.get(idx - 1);
        txtSdt.setText(ptn.getSdt() != null ? ptn.getSdt() : "");
        txtBienSo.setText(ptn.getBienSo() != null ? ptn.getBienSo() : "");
        txtPhieuSC.setText(ptn.getSoPhieu() != null ? ptn.getSoPhieu() : "");

        lblPhieuBadge.setText("Phiếu: " + ptn.getSoPhieu());
        lblBangKeSubtitle.setText("Chi tiết dịch vụ từ lệnh sửa chữa " + ptn.getSoPhieu());

        modelChiTiet.setRowCount(0);
        dsChiTietHienTai.clear();
        List<ChiTietDichVu> ds = kho.layChiTietTheoPhieu(ptn.getSoPhieu());
        long tongCong = 0, tongPT = 0;

        for (ChiTietDichVu ct : ds) {
            dsChiTietHienTai.add(ct);
            modelChiTiet.addRow(new Object[]{
                    ct.getLoai(),
                    ct.getTenHangMuc(),
                    ct.getDonVi(),
                    ct.getSoLuong(),
                    String.format("%,d", ct.getDonGia()),
                    String.format("%,d", ct.getThanhTien())
            });
            if ("Tiền công".equalsIgnoreCase(ct.getLoai())) tongCong += ct.getThanhTien();
            else tongPT += ct.getThanhTien();
        }

        lblHangMucBadge.setText(dsChiTietHienTai.size() + " hạng mục");
        lblTienCongMini.setText(String.format("%,d đ", tongCong));
        lblTienPTMini.setText(String.format("%,d đ", tongPT));
        txtTienDichVu.setText(String.valueOf(tongCong));
        txtTienPhuTung.setText(String.valueOf(tongPT));

        tinhTongTien();
    }

    private long tinhTongTien() {
        try {
            long cong = 0, pt = 0;
            try { cong = Long.parseLong(txtTienDichVu.getText().trim().replace(",", "").replace(".", "")); } catch (Exception ignored) {}
            try { pt = Long.parseLong(txtTienPhuTung.getText().trim().replace(",", "").replace(".", "")); } catch (Exception ignored) {}

            double ck = 0;
            try { ck = Double.parseDouble(txtGiamGia.getText().trim()); } catch (Exception ignored) {}

            long tongChiPhi = cong + pt;
            lblFooterTienPT.setText("Tiền phụ tùng: " + UiHelper.tien(pt) + " đ");
            lblFooterTienCong.setText("Tiền công dịch vụ: " + UiHelper.tien(cong) + " đ");
            lblTongChiPhiBang.setText("Tổng chi phí dịch vụ & vật tư: " + UiHelper.tien(tongChiPhi) + " đ");

            long giam = Math.round(tongChiPhi * (ck / 100.0));
            long sauGiam = Math.max(0, tongChiPhi - giam);

            lblTongTien.setText(String.format("%,d VNĐ", sauGiam));

            if ("Chuyển khoản QR".equals(phuongThucChon)) {
                dongBoQR(sauGiam);
            } else {
                dongBoTienMat(sauGiam);
            }

            tinhTienThua();
            return sauGiam;
        } catch (Exception ex) {
            lblTongTien.setText("0 VNĐ");
            dongBoQR(0);
            return 0;
        }
    }

    private long layTongTienHienTai() {
        try {
            String txt = (cheDoHienTai == CHE_DO_SUA_CHUA ? lblTongTien.getText() : lblTongTienBanLe.getText());
            return Long.parseLong(txt.replaceAll("[^0-9]", ""));
        } catch (Exception ex) {
            return 0;
        }
    }

    private void dongBoQR(long tong) {
        lblQrSoTien.setText(tong > 0 ? String.format("%,d VNĐ", tong) : "0 VNĐ");
        String ma = (cheDoHienTai == CHE_DO_SUA_CHUA ? txtMaHD.getText().trim() : txtMaHDBanLe.getText().trim());
        if (tong > 0 && !ma.isEmpty()) {
            lblQrNoiDung.setText("TT " + ma);
            lblHuongDanQr1.setText("<html><b>Mã QR động:</b> Tự động cập nhật đúng chính xác số tiền cần thanh toán <b>" + String.format("%,d VNĐ", tong) + "</b> và cú pháp hóa đơn.</html>");
        } else {
            lblQrNoiDung.setText("TT HD015");
            lblHuongDanQr1.setText("<html><b>Mã QR động:</b> Tự động cập nhật đúng chính xác số tiền cần thanh toán <b>0 VNĐ</b> và cú pháp hóa đơn.</html>");
        }
        lblQrThuHuong.setText("TT HD003");
        if (qrCanvas != null) qrCanvas.repaint();
    }

    private void dongBoTienMat(long tong) {
        lblTienMatCanThu.setText(UiHelper.tien(tong) + " đ");
        String raw = txtTienKhachDua.getText().replace(",", "").replace(".", "").trim();
        long khach = raw.isEmpty() ? 0 : Long.parseLong(raw);
        lblTienMatKhachDua.setText(UiHelper.tien(khach) + " đ");
    }

    private void tinhTienThua() {
        try {
            long canThu = layTongTienHienTai();
            String rawKhachDua = txtTienKhachDua.getText().replace(",", "").replace(".", "").trim();
            long khachDua = rawKhachDua.isEmpty() ? 0 : Long.parseLong(rawKhachDua);

            lblTienMatCanThu.setText(UiHelper.tien(canThu) + " đ");
            lblTienMatKhachDua.setText(UiHelper.tien(khachDua) + " đ");

            long thua = khachDua - canThu;
            if (thua >= 0) {
                lblTienThua.setText(UiHelper.tien(thua) + " đ");
                lblTienThua.setForeground(new Color(22, 163, 74));
                lblTienMatThua.setText(UiHelper.tien(thua) + " đ");
                lblTienMatThua.setForeground(new Color(22, 163, 74));
            } else {
                lblTienThua.setText("-" + UiHelper.tien(Math.abs(thua)) + " đ (Thiếu)");
                lblTienThua.setForeground(new Color(220, 38, 38));
                lblTienMatThua.setText("-" + UiHelper.tien(Math.abs(thua)) + " đ");
                lblTienMatThua.setForeground(new Color(220, 38, 38));
            }
        } catch (Exception ex) {
            lblTienThua.setText("0 đ");
            lblTienMatThua.setText("0 đ");
        }
    }

    private void dangKyPhimTat() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "xacNhanTT");
        am.put("xacNhanTT", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { thucHienThanhToan(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "inHD");
        am.put("inHD", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { xemVaInHoaDon(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "lamMoi");
        am.put("lamMoi", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { lamMoiForm(); }
        });
    }

    private void thucHienThanhToanSuaChua() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.THANH_TOAN)) return;
        int idx = cboKhachHang.getSelectedIndex();
        if (idx <= 0 || idx > dsPhieuDangChon.size()) {
            UiHelper.loi(this, "Vui lòng chọn xe / khách hàng cần thanh toán!", cboKhachHang);
            return;
        }

        long tong = tinhTongTien();
        if (tong <= 0) {
            UiHelper.loi(this, "Tổng thanh toán phải lớn hơn 0 đồng!", txtTienDichVu);
            return;
        }

        PhieuTiepNhan ptn = dsPhieuDangChon.get(idx - 1);
        String nguoiThu = nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân";

        HoaDon hd = new HoaDon(
                txtMaHD.getText().trim(),
                ptn.getSoPhieu(),
                ptn.getMaKH(),
                ptn.getHoTen(),
                ptn.getSdt(),
                ptn.getBienSo(),
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

        java.util.List<ChiTietDichVu> dsItems = new java.util.ArrayList<>();
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
            if (loai.toLowerCase().contains("phụ tùng")) {
                PhuTung ptFound = kho.timPhuTungPhuHop("", ten);
                if (ptFound != null) maPT = ptFound.getMaPT();
            }
            dsItems.add(new ChiTietDichVu(loai, maPT, ten, dvt, sl, gia));
        }

        boolean ok = hoaDonDAO.themHoaDon(hd, nvHienTai != null ? nvHienTai.getMaNV() : "NV05", dsItems);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Không lưu được hóa đơn. Kiểm tra kết nối SQL Server.", "Lỗi lưu CSDL", JOptionPane.ERROR_MESSAGE);
            return;
        }
        kho.dongBoTuSQLServer();

        int luaChonBH = JOptionPane.showConfirmDialog(this,
                "Thanh toán hóa đơn [" + hd.getMaHD() + "] thành công!\n"
                        + "Khách hàng: " + hd.getTenKH() + " (" + hd.getBienSo() + ")\n"
                        + "Số tiền: " + UiHelper.tien(tong) + " đ (" + phuongThucChon + ")\n"
                        + "Phiếu sửa chữa " + ptn.getSoPhieu() + " đã hoàn tất.\n\n"
                        + "Bạn có muốn lập phiếu bảo hành cho xe này ngay không?",
                "Thanh toán thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        lamMoiFormSuaChua();

        if (luaChonBH == JOptionPane.YES_OPTION && Main_UI.getInstance() != null) {
            Main_UI.getInstance().chuyenTrang("Lập phiếu bảo hành");
        }
    }

    private void lamMoiFormSuaChua() {
        khoiTaoDuLieu();
        txtSdt.setText("");
        txtBienSo.setText("");
        txtPhieuSC.setText("");
        txtTienDichVu.setText("0");
        txtTienPhuTung.setText("0");
        txtGiamGia.setText("0");
        txtGhiChu.setText("");
        lblTongTien.setText("0 VNĐ");
        lblQrSoTien.setText("0 VNĐ");
        lblQrNoiDung.setText("TT HD015");
        modelChiTiet.setRowCount(0);
        dsChiTietHienTai.clear();
        lblPhieuBadge.setText("Phiếu: ---");
        lblHangMucBadge.setText("0 hạng mục");
        lblBangKeSubtitle.setText("Chi tiết dịch vụ từ lệnh sửa chữa ---");
        lblTienCongMini.setText("0 đ");
        lblTienPTMini.setText("0 đ");
        lblFooterTienPT.setText("Tiền phụ tùng: 0 đ");
        lblFooterTienCong.setText("Tiền công dịch vụ: 0 đ");
        lblTongChiPhiBang.setText("Tổng chi phí dịch vụ & vật tư: 0 đ");
    }

    private void xemVaInHoaDonSuaChua() {
        int idx = cboKhachHang.getSelectedIndex();
        long tong = tinhTongTien();
        String maHD = txtMaHD.getText().trim();
        String ngayLap = txtNgay.getText().trim();
        String tenKH = idx > 0 && idx <= dsPhieuDangChon.size() ? dsPhieuDangChon.get(idx - 1).getHoTen() : "Khách vãng lai";
        String sdt = txtSdt.getText().trim();
        String bienSo = txtBienSo.getText().trim();
        String soPhieu = txtPhieuSC.getText().trim();
        String thuNgan = nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân";

        StringBuilder sbItems = new StringBuilder();
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            sbItems.append("<tr>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(i + 1).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;'>").append(modelChiTiet.getValueAt(i, 1)).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(modelChiTiet.getValueAt(i, 2)).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(modelChiTiet.getValueAt(i, 3)).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:right;'>").append(modelChiTiet.getValueAt(i, 4)).append(" đ</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:right;font-weight:bold;'>").append(modelChiTiet.getValueAt(i, 5)).append(" đ</td>")
                    .append("</tr>");
        }
        if (sbItems.length() == 0) {
            sbItems.append("<tr><td colspan='6' style='text-align:center;padding:12px;color:#94A3B8;'>Chưa có hạng mục chi tiết</td></tr>");
        }

        String html = "<html><body style='font-family:Segoe UI, sans-serif;font-size:12px;color:#0F172A;padding:15px;'>"
                + "<div style='text-align:center;border-bottom:2px solid #2563EB;padding-bottom:10px;'>"
                + "<h2 style='margin:0;color:#1E3A8A;'>" + cauHinh.getTenGara() + "</h2>"
                + "<p style='margin:3px 0;color:#475569;'>" + cauHinh.getDiaChi() + " | Hotline: " + cauHinh.getHotline() + "</p>"
                + "<h3 style='margin:10px 0 2px 0;color:#0F172A;letter-spacing:1px;'>HÓA ĐƠN THANH TOÁN DỊCH VỤ</h3>"
                + "<p style='margin:0;font-size:11px;color:#64748B;'>Số: <b style='color:#DC2626;'>" + maHD + "</b> | Ngày: " + ngayLap + "</p>"
                + "</div>"
                + "<table style='width:100%;margin-top:12px;font-size:12px;' cellpadding='3'>"
                + "<tr><td width='50%'><b>Khách hàng:</b> " + tenKH + "</td><td><b>Biển số xe:</b> " + bienSo + "</td></tr>"
                + "<tr><td><b>Điện thoại:</b> " + sdt + "</td><td><b>Phiếu sửa chữa:</b> " + soPhieu + "</td></tr>"
                + "<tr><td><b>Phương thức:</b> " + phuongThucChon + "</td><td><b>Thu ngân:</b> " + thuNgan + "</td></tr>"
                + "</table>"
                + "<h4 style='margin:14px 0 6px 0;'>CHI TIẾT HẠNG MỤC DỊCH VỤ & PHỤ TÙNG</h4>"
                + "<table style='width:100%;border-collapse:collapse;font-size:11px;'>"
                + "<tr style='background:#F1F5F9;font-weight:bold;'>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:35px;'>STT</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;text-align:left;'>Tên hạng mục / Phụ tùng</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:55px;'>ĐVT</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:45px;'>SL</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:95px;'>Đơn giá</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:105px;'>Thành tiền</th>"
                + "</tr>"
                + sbItems
                + "</table>"
                + "<table style='width:100%;margin-top:10px;font-size:12px;' cellpadding='4'>"
                + "<tr><td align='right' width='70%'>Tiền công dịch vụ:</td><td align='right'><b>" + txtTienDichVu.getText() + " đ</b></td></tr>"
                + "<tr><td align='right'>Tiền phụ tùng / vật tư:</td><td align='right'><b>" + txtTienPhuTung.getText() + " đ</b></td></tr>"
                + "<tr><td align='right'>Giảm giá chiết khấu:</td><td align='right'><b>" + txtGiamGia.getText() + "%</b></td></tr>"
                + "<tr style='background:#EFF6FF;'><td align='right'><b style='color:#1E3A8A;font-size:14px;'>TỔNG PHẢI THU:</b></td>"
                + "<td align='right'><b style='color:#DC2626;font-size:16px;'>" + UiHelper.tien(tong) + " đ</b></td></tr>"
                + "</table>"
                + "</body></html>";

        hienThiHopThoaiIn(html, "Hóa đơn dịch vụ " + maHD);
    }

    // ===============================================================
    // CÁC HÀM XỬ LÝ DỮ LIỆU BÁN LẺ SHOP
    // ===============================================================
    private JPanel taoFormThongTinBanLe() {
        JPanel card = taoCardTrang(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(480, 430));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel lblTitle = new JLabel("Thông tin khách mua & hóa đơn bán lẻ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(15, 23, 42));
        head.add(lblTitle, BorderLayout.WEST);
        card.add(head, BorderLayout.NORTH);

        txtMaHDBanLe.setEditable(false);
        txtNgayBanLe.setEditable(false);
        txtSdtBanLe.setEditable(false);
        txtDiaChiBanLe.setEditable(false);
        txtNhanVienBanLe.setEditable(false);
        txtTienPhuTungBanLe.setEditable(false);

        UiHelper.trangTriInput(txtMaHDBanLe);
        UiHelper.trangTriInput(txtNgayBanLe);
        UiHelper.trangTriInput(txtSdtBanLe);
        UiHelper.trangTriInput(txtDiaChiBanLe);
        UiHelper.trangTriInput(txtNhanVienBanLe);
        UiHelper.trangTriInput(txtTienPhuTungBanLe);
        UiHelper.trangTriInput(txtGiamGiaBanLe);
        UiHelper.trangTriInput(txtGhiChuBanLe);

        cboKhachHangBanLe.setFont(UiHelper.FONT_BT);
        cboKhachHangBanLe.setBackground(Color.WHITE);
        cboKhachHangBanLe.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang kh) {
                    setText(kh.getMaKH() + " — " + kh.getHoTen() + " (" + kh.getSdt() + ")");
                }
                return this;
            }
        });

        cboKhachHangBanLe.addActionListener(e -> {
            KhachHang kh = (KhachHang) cboKhachHangBanLe.getSelectedItem();
            if (kh != null) {
                txtSdtBanLe.setText(kh.getSdt() != null ? kh.getSdt() : "");
                txtDiaChiBanLe.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
            }
        });

        txtGiamGiaBanLe.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { tinhTongTienBanLe(); }
        });

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);
        row1.add(taoInputCoNhan("Mã HĐ", txtMaHDBanLe));
        row1.add(taoInputCoNhan("Ngày lập", txtNgayBanLe));
        form.add(row1);
        form.add(Box.createVerticalStrut(8));

        form.add(taoInputCoNhan("Khách hàng mua lẻ *", cboKhachHangBanLe));
        form.add(Box.createVerticalStrut(8));

        JPanel row3 = new JPanel(new GridLayout(1, 2, 10, 0));
        row3.setOpaque(false);
        row3.add(taoInputCoNhan("Số điện thoại", txtSdtBanLe));
        row3.add(taoInputCoNhan("Địa chỉ", txtDiaChiBanLe));
        form.add(row3);
        form.add(Box.createVerticalStrut(8));

        JPanel row4 = new JPanel(new GridLayout(1, 2, 10, 0));
        row4.setOpaque(false);
        row4.add(taoInputCoNhan("Tiền phụ tùng", txtTienPhuTungBanLe));
        row4.add(taoInputCoNhan("Giảm giá (%)", txtGiamGiaBanLe));
        form.add(row4);
        form.add(Box.createVerticalStrut(8));

        form.add(taoInputCoNhan("Ghi chú đơn hàng", txtGhiChuBanLe));

        card.add(form, BorderLayout.CENTER);

        // Banner chân thẻ bán lẻ
        JPanel bannerDuoi = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 253, 244));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(187, 247, 208));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        bannerDuoi.setOpaque(false);
        bannerDuoi.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblTtVal = new JLabel("● Đã thanh toán (Mặc định khi thu tiền)");
        lblTtVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTtVal.setForeground(new Color(22, 163, 74));

        lblTongTienBanLe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongTienBanLe.setForeground(new Color(30, 58, 138));

        bannerDuoi.add(lblTtVal, BorderLayout.WEST);
        bannerDuoi.add(lblTongTienBanLe, BorderLayout.EAST);
        card.add(bannerDuoi, BorderLayout.SOUTH);

        return card;
    }

    private JPanel taoBangChiTietBanLe() {
        JPanel card = taoCardTrang(new BorderLayout(0, 10));

        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        toolbar.add(new JLabel("Tìm nhanh:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35;
        txtTimPTShop.setPreferredSize(new Dimension(160, 34));
        UiHelper.trangTriInput(txtTimPTShop);
        txtTimPTShop.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locPhuTungShop(txtTimPTShop.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { locPhuTungShop(txtTimPTShop.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { locPhuTungShop(txtTimPTShop.getText()); }
        });
        toolbar.add(txtTimPTShop, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        toolbar.add(new JLabel("Phụ tùng:"), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.65;
        cboPhuTungShop.setPreferredSize(new Dimension(280, 34));
        cboPhuTungShop.setFont(UiHelper.FONT_BT);
        cboPhuTungShop.setBackground(Color.WHITE);
        cboPhuTungShop.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhuTung pt) {
                    String dv = (pt.getDonVi() != null && !pt.getDonVi().isBlank()) ? pt.getDonVi() : "Cái";
                    setText("[" + pt.getMaPT() + "] " + pt.getTen() + " (" + dv + ") | " + UiHelper.tien(pt.getDonGia()) + " đ | Tồn: " + pt.getSoLuongTon());
                }
                return this;
            }
        });
        toolbar.add(cboPhuTungShop, gbc);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);
        row2.add(new JLabel("Số lượng bán:"));
        spSoLuongShop.setPreferredSize(new Dimension(80, 34));
        row2.add(spSoLuongShop);

        JButton btnThem = UiHelper.nut("+ Thêm vào đơn", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> themPhuTungShop());
        btnThem.setPreferredSize(new Dimension(140, 34));
        row2.add(btnThem);

        JButton btnXoa = UiHelper.nut("Xóa dòng", UiIcon.of(UiIcon.TRASH, 15), UiHelper.CHU_CHAY, e -> xoaPhuTungShop());
        btnXoa.setPreferredSize(new Dimension(110, 34));
        row2.add(btnXoa);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4; gbc.weightx = 1.0;
        toolbar.add(row2, gbc);

        card.add(toolbar, BorderLayout.NORTH);

        modelChiTietBanLe = UiHelper.model("MÃ PT", "TÊN PHỤ TÙNG / VẬT TƯ", "ĐVT", "SL", "ĐƠN GIÁ (Đ)", "THÀNH TIỀN (Đ)");
        tblChiTietBanLe = UiHelper.bang(modelChiTietBanLe);
        tblChiTietBanLe.setRowHeight(36);

        JScrollPane sp = new JScrollPane(tblChiTietBanLe);
        sp.setPreferredSize(new Dimension(0, 180));
        card.add(sp, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 4, 4));
        lblTongPhuTungBanLe.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongPhuTungBanLe.setForeground(new Color(51, 65, 85));
        footer.add(lblTongPhuTungBanLe, BorderLayout.WEST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private void napKhachHangBanLe() {
        cboKhachHangBanLe.removeAllItems();
        for (KhachHang kh : kho.getKhachHangs()) {
            cboKhachHangBanLe.addItem(kh);
        }
        if (cboKhachHangBanLe.getItemCount() > 0) {
            cboKhachHangBanLe.setSelectedIndex(0);
            KhachHang kh = (KhachHang) cboKhachHangBanLe.getSelectedItem();
            if (kh != null) {
                txtSdtBanLe.setText(kh.getSdt() != null ? kh.getSdt() : "");
                txtDiaChiBanLe.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
            }
        }
    }

    private void napPhuTungShop() {
        cboPhuTungShop.removeAllItems();
        for (PhuTung pt : kho.getPhuTungs()) {
            cboPhuTungShop.addItem(pt);
        }
    }

    private void locPhuTungShop(String tuKhoa) {
        String q = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        cboPhuTungShop.removeAllItems();
        for (PhuTung pt : kho.getPhuTungs()) {
            if (q.isEmpty() || pt.getTen().toLowerCase().contains(q) || pt.getMaPT().toLowerCase().contains(q)) {
                cboPhuTungShop.addItem(pt);
            }
        }
    }

    private void themPhuTungShop() {
        PhuTung pt = (PhuTung) cboPhuTungShop.getSelectedItem();
        if (pt == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng cần bán!", "Chưa chọn phụ tùng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sl = (Integer) spSoLuongShop.getValue();
        if (sl <= 0) return;
        int tonKho = pt.getSoLuongTon();
        if (sl > tonKho) {
            JOptionPane.showMessageDialog(this, "Số lượng bán (" + sl + ") vượt quá tồn kho (" + tonKho + ")!", "Không đủ tồn kho", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < modelChiTietBanLe.getRowCount(); i++) {
            if (String.valueOf(modelChiTietBanLe.getValueAt(i, 0)).equalsIgnoreCase(pt.getMaPT())) {
                int slHienTai = Integer.parseInt(String.valueOf(modelChiTietBanLe.getValueAt(i, 3)));
                int slMoi = slHienTai + sl;
                if (slMoi > tonKho) {
                    JOptionPane.showMessageDialog(this, "Tổng số lượng (" + slMoi + ") vượt quá tồn kho (" + tonKho + ")!", "Không đủ tồn kho", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                modelChiTietBanLe.setValueAt(slMoi, i, 3);
                modelChiTietBanLe.setValueAt(String.format("%,d", slMoi * pt.getDonGia()), i, 5);
                tinhTongTienBanLe();
                return;
            }
        }

        String dv = (pt.getDonVi() != null && !pt.getDonVi().isBlank()) ? pt.getDonVi() : "Cái";
        modelChiTietBanLe.addRow(new Object[]{
                pt.getMaPT(),
                pt.getTen(),
                dv,
                sl,
                String.format("%,d", pt.getDonGia()),
                String.format("%,d", sl * pt.getDonGia())
        });
        tinhTongTienBanLe();
    }

    private void xoaPhuTungShop() {
        int row = tblChiTietBanLe.getSelectedRow();
        if (row >= 0) {
            modelChiTietBanLe.removeRow(row);
            tinhTongTienBanLe();
        }
    }

    private long tinhTongTienBanLe() {
        long tongTienHang = 0;
        int tongSoLuong = 0;

        for (int i = 0; i < modelChiTietBanLe.getRowCount(); i++) {
            try {
                int sl = Integer.parseInt(String.valueOf(modelChiTietBanLe.getValueAt(i, 3)).trim());
                long tt = Long.parseLong(String.valueOf(modelChiTietBanLe.getValueAt(i, 5)).replaceAll("[^0-9]", ""));
                tongTienHang += tt;
                tongSoLuong += sl;
            } catch (Exception ignored) {}
        }

        txtTienPhuTungBanLe.setText(String.format("%,d", tongTienHang));
        lblTongPhuTungBanLe.setText("Tổng số: " + tongSoLuong + " món | Tiền hàng: " + UiHelper.tien(tongTienHang) + " đ");

        double ck = 0;
        try { ck = Double.parseDouble(txtGiamGiaBanLe.getText().trim()); } catch (Exception ignored) {}

        long giam = Math.round(tongTienHang * (ck / 100.0));
        long sauGiam = Math.max(0, tongTienHang - giam);

        lblTongTienBanLe.setText(String.format("%,d VNĐ", sauGiam));
        lblTongTien.setText(String.format("%,d VNĐ", sauGiam));

        if ("Chuyển khoản QR".equals(phuongThucChon)) {
            dongBoQR(sauGiam);
        } else {
            dongBoTienMat(sauGiam);
        }

        tinhTienThua();
        return sauGiam;
    }

    private void thucHienThanhToanBanLe() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.THANH_TOAN)) return;
        if (modelChiTietBanLe.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Đơn bán lẻ chưa có phụ tùng nào! Vui lòng bấm '+ Thêm vào đơn'.", "Chưa có mặt hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long tong = tinhTongTienBanLe();
        KhachHang kh = (KhachHang) cboKhachHangBanLe.getSelectedItem();
        if (kh == null) return;

        String maHD = txtMaHDBanLe.getText().trim();
        String maNV = nvHienTai != null ? nvHienTai.getMaNV() : "NV05";
        double giamGia = 0;
        try { giamGia = Double.parseDouble(txtGiamGiaBanLe.getText().trim()); } catch (Exception ignored) {}
        String ghiChu = txtGhiChuBanLe.getText().trim();
        if (ghiChu.isBlank()) ghiChu = "Bán lẻ phụ tùng tại shop";

        Map<String, Integer> chiTietMap = new LinkedHashMap<>();
        for (int i = 0; i < modelChiTietBanLe.getRowCount(); i++) {
            String maPT = String.valueOf(modelChiTietBanLe.getValueAt(i, 0)).trim();
            int sl = Integer.parseInt(String.valueOf(modelChiTietBanLe.getValueAt(i, 3)).trim());
            chiTietMap.merge(maPT, sl, Integer::sum);
        }

        boolean ok = false;
        try {
            ok = hoaDonBanHangDAO.lapHoaDonBanHang(maHD, kh.getMaKH(), maNV, phuongThucChon, giamGia, ghiChu, chiTietMap);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi SQL khi lưu hóa đơn bán lẻ: " + ex.getMessage(), "Lỗi lưu CSDL", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Không lưu được hóa đơn bán lẻ vào CSDL SQL Server!", "Lỗi lưu CSDL", JOptionPane.ERROR_MESSAGE);
            return;
        }

        kho.dongBoTuSQLServer();
        napPhuTungShop();

        int inOpt = JOptionPane.showConfirmDialog(this,
                "Thanh toán hóa đơn bán lẻ [" + maHD + "] thành công!\n"
                        + "Khách hàng: " + kh.getHoTen() + "\n"
                        + "Số tiền: " + UiHelper.tien(tong) + " đ (" + phuongThucChon + ")\n"
                        + "Đã tự động trừ tồn kho phụ tùng.\n\n"
                        + "Bạn có muốn in hóa đơn bán lẻ cho khách hàng ngay không?",
                "Thanh toán thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (inOpt == JOptionPane.YES_OPTION) {
            xemVaInHoaDonBanLe();
        }
        lamMoiFormBanLe();
    }

    private void lamMoiFormBanLe() {
        txtMaHDBanLe.setText(hoaDonDAO.sinhMaHoaDonTuDong());
        txtNgayBanLe.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtTienPhuTungBanLe.setText("0");
        txtGiamGiaBanLe.setText("0");
        txtGhiChuBanLe.setText("");
        lblTongTienBanLe.setText("0 VNĐ");
        lblTongTien.setText("0 VNĐ");
        modelChiTietBanLe.setRowCount(0);
        lblTongPhuTungBanLe.setText("Tổng số phụ tùng: 0 món | Tiền hàng: 0 đ");
        txtTimPTShop.setText("");
        napPhuTungShop();
        dongBoQR(0);
    }

    private void xemVaInHoaDonBanLe() {
        long tong = tinhTongTienBanLe();
        String maHD = txtMaHDBanLe.getText().trim();
        String ngayLap = txtNgayBanLe.getText().trim();
        KhachHang kh = (KhachHang) cboKhachHangBanLe.getSelectedItem();
        String tenKH = kh != null ? kh.getHoTen() : "Khách mua lẻ";
        String sdt = txtSdtBanLe.getText().trim();
        String diaChi = txtDiaChiBanLe.getText().trim();
        String thuNgan = nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân";

        StringBuilder sbItems = new StringBuilder();
        for (int i = 0; i < modelChiTietBanLe.getRowCount(); i++) {
            sbItems.append("<tr>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(i + 1).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;'>").append(modelChiTietBanLe.getValueAt(i, 1)).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(modelChiTietBanLe.getValueAt(i, 2)).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:center;'>").append(modelChiTietBanLe.getValueAt(i, 3)).append("</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:right;'>").append(modelChiTietBanLe.getValueAt(i, 4)).append(" đ</td>")
                    .append("<td style='border:1px solid #CBD5E1;padding:6px;text-align:right;font-weight:bold;'>").append(modelChiTietBanLe.getValueAt(i, 5)).append(" đ</td>")
                    .append("</tr>");
        }

        String html = "<html><body style='font-family:Segoe UI, sans-serif;font-size:12px;color:#0F172A;padding:15px;'>"
                + "<div style='text-align:center;border-bottom:2px solid #2563EB;padding-bottom:10px;'>"
                + "<h2 style='margin:0;color:#1E3A8A;'>" + cauHinh.getTenGara() + "</h2>"
                + "<p style='margin:3px 0;color:#475569;'>" + cauHinh.getDiaChi() + " | Hotline: " + cauHinh.getHotline() + "</p>"
                + "<h3 style='margin:10px 0 2px 0;color:#0F172A;letter-spacing:1px;'>HÓA ĐƠN BÁN LẺ PHỤ TÙNG & VẬT TƯ</h3>"
                + "<p style='margin:0;font-size:11px;color:#64748B;'>Số: <b style='color:#DC2626;'>" + maHD + "</b> | Ngày: " + ngayLap + "</p>"
                + "</div>"
                + "<table style='width:100%;margin-top:12px;font-size:12px;' cellpadding='3'>"
                + "<tr><td width='55%'><b>Khách hàng:</b> " + tenKH + "</td><td><b>Điện thoại:</b> " + sdt + "</td></tr>"
                + "<tr><td><b>Địa chỉ:</b> " + (diaChi.isEmpty() ? "Tại quầy shop" : diaChi) + "</td><td><b>Thu ngân:</b> " + thuNgan + "</td></tr>"
                + "</table>"
                + "<h4 style='margin:14px 0 6px 0;'>DANH SÁCH PHỤ TÙNG & VẬT TƯ</h4>"
                + "<table style='width:100%;border-collapse:collapse;font-size:11px;'>"
                + "<tr style='background:#F1F5F9;font-weight:bold;'>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:35px;'>STT</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;text-align:left;'>Tên phụ tùng / Vật tư</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:55px;'>ĐVT</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:45px;'>SL</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:95px;'>Đơn giá</th>"
                + "<th style='border:1px solid #CBD5E1;padding:6px;width:105px;'>Thành tiền</th>"
                + "</tr>"
                + sbItems
                + "</table>"
                + "<table style='width:100%;margin-top:10px;font-size:12px;' cellpadding='4'>"
                + "<tr><td align='right' width='70%'>Tổng tiền hàng:</td><td align='right'><b>" + txtTienPhuTungBanLe.getText() + " đ</b></td></tr>"
                + "<tr><td align='right'>Chiết khấu / Giảm giá:</td><td align='right'><b>" + txtGiamGiaBanLe.getText() + "%</b></td></tr>"
                + "<tr style='background:#EFF6FF;'><td align='right'><b style='color:#1E3A8A;font-size:14px;'>TỔNG THANH TOÁN:</b></td>"
                + "<td align='right'><b style='color:#DC2626;font-size:16px;'>" + UiHelper.tien(tong) + " đ</b></td></tr>"
                + "</table>"
                + "</body></html>";

        hienThiHopThoaiIn(html, "Hóa đơn bán lẻ " + maHD);
    }

    private void hienThiHopThoaiIn(String html, String title) {
        JEditorPane editor = new JEditorPane("text/html", html);
        editor.setEditable(false);
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setPreferredSize(new Dimension(640, 580));

        Object[] options = {"In ngay", "Đóng"};
        int choice = JOptionPane.showOptionDialog(this, scroll, "Xem trước & In — " + title,
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            try {
                editor.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Không thể in hóa đơn: " + ex.getMessage(), "Lỗi máy in", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===============================================================
    // CÁC HÀM ĐIỀU PHỐI CHUNG
    // ===============================================================
    private void thucHienThanhToan() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.THANH_TOAN)) return;
        if (cheDoHienTai == CHE_DO_SUA_CHUA) {
            thucHienThanhToanSuaChua();
        } else {
            thucHienThanhToanBanLe();
        }
    }

    private void xemVaInHoaDon() {
        if (cheDoHienTai == CHE_DO_SUA_CHUA) {
            xemVaInHoaDonSuaChua();
        } else {
            xemVaInHoaDonBanLe();
        }
    }

    private void lamMoiForm() {
        if (cheDoHienTai == CHE_DO_SUA_CHUA) {
            lamMoiFormSuaChua();
        } else {
            lamMoiFormBanLe();
        }
    }

    // ===============================================================
    // HÀM ĐỌC SỐ TIỀN THÀNH CHỮ TIẾNG VIỆT CHUẨN
    // ===============================================================
    public static String docSoThanhChu(long so) {
        if (so == 0) return "Không đồng";
        if (so < 0) return "Âm " + docSoThanhChu(-so);

        String[] chuSo = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
        String[] donVi = {"", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ"};

        StringBuilder kq = new StringBuilder();
        int cap = 0;

        while (so > 0) {
            int baSo = (int) (so % 1000);
            if (baSo > 0) {
                String chuBaSo = docBaSo(baSo, so >= 1000, chuSo);
                if (!chuBaSo.isEmpty()) {
                    String dv = donVi[cap];
                    if (!dv.isEmpty()) chuBaSo += " " + dv;
                    if (kq.length() > 0) {
                        kq.insert(0, chuBaSo + " ");
                    } else {
                        kq.append(chuBaSo);
                    }
                }
            }
            so /= 1000;
            cap++;
        }

        String result = kq.toString().trim() + " đồng";
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    private static String docBaSo(int n, boolean canDocKhongTram, String[] chuSo) {
        int tram = n / 100;
        int chuc = (n % 100) / 10;
        int donVi = n % 10;

        StringBuilder sb = new StringBuilder();

        if (tram > 0 || canDocKhongTram) {
            sb.append(chuSo[tram]).append(" trăm");
        }

        if (chuc > 1) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(chuSo[chuc]).append(" mươi");
            if (donVi == 1) sb.append(" mốt");
            else if (donVi == 5) sb.append(" lăm");
            else if (donVi > 0) sb.append(" ").append(chuSo[donVi]);
        } else if (chuc == 1) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("mười");
            if (donVi == 5) sb.append(" lăm");
            else if (donVi > 0) sb.append(" ").append(chuSo[donVi]);
        } else if (donVi > 0) {
            if (sb.length() > 0) sb.append(" linh ");
            sb.append(chuSo[donVi]);
        }

        return sb.toString().trim();
    }
}
