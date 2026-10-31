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
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn hình thanh toán hóa đơn sửa chữa xe và bán lẻ phụ tùng.
 */
public class ThanhToanHoaDon_UI extends JPanel {

    public static final int CHE_DO_SUA_CHUA = 0;
    public static final int CHE_DO_BAN_LE = 1;
    private int cheDoHienTai = CHE_DO_SUA_CHUA;

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
    private DefaultTableModel modelChiTiet;
    private JTable tblChiTiet;
    private final JLabel lblTongChiPhiBang = new JLabel("Tổng chi phí dịch vụ & vật tư: 0 đ");

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
    private final JLabel lblTongTienBanLe = new JLabel("0 đ");
    private final JComboBox<PhuTung> cboPhuTungShop = new JComboBox<>();
    private final JTextField txtTimPTShop = new JTextField();
    private final JSpinner spSoLuongShop = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private DefaultTableModel modelChiTietBanLe;
    private JTable tblChiTietBanLe;
    private final JLabel lblTongPhuTungBanLe = new JLabel("Tổng số phụ tùng: 0 món | Tiền hàng: 0 đ");

    // ===============================================================
    // CÁC THÀNH PHẦN DÙNG CHUNG: QUẦY THU NGÂN & THANH TOÁN
    // ===============================================================
    private final JLabel lblTongTien = new JLabel("0 đ");
    private String phuongThucChon = "Chuyển khoản QR";
    private JButton btnQrTab, btnTienMatTab;
    private final CardLayout cardLayoutTienTra = new CardLayout();
    private JPanel panelTienTra;
    private final CardLayout cardLayoutCotPhai = new CardLayout();
    private JPanel panelCotPhai;
    private final JLabel lblTienMatCanThu = new JLabel("0 đ");
    private final JLabel lblTienMatKhachDua = new JLabel("0 đ");
    private final JLabel lblTienMatThua = new JLabel("0 đ");
    private final JTextField txtTienKhachDua = new JTextField("0");
    private final JLabel lblTienThua = new JLabel("0");
    private final JTextField txtMaThamChieu = new JTextField();
    private final JTextField txtTkThuHuong = new JTextField();
    private JPanel qrCanvas;
    private final JLabel lblQrSoTien = new JLabel("0 VNĐ");
    private final JLabel lblQrNoiDung = new JLabel("TT HD001 51A12345");

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

        // Header trang
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        lblTdeTrang = UiHelper.tieuDeTrang("Quầy thu ngân");
        lblPhuTrang = new JLabel("Thanh toán dịch vụ sửa chữa, quét mã VietQR tự động, quản lý quầy thu ngân và xuất hóa đơn");
        lblPhuTrang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPhuTrang.setForeground(new Color(100, 116, 139));
        lblTdeTrang.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPhuTrang.setAlignmentX(Component.LEFT_ALIGNMENT);

        north.add(lblTdeTrang);
        north.add(Box.createVerticalStrut(4));
        north.add(lblPhuTrang);
        north.add(Box.createVerticalStrut(8));

        // Thanh chọn chế độ Sửa chữa / Bán lẻ Shop
        JPanel thanhChon = taoThanhChonCheDo();
        thanhChon.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(thanhChon);
        north.add(Box.createVerticalStrut(6));

        root.add(north, BorderLayout.NORTH);

        // Nội dung quầy thu ngân
        root.add(taoNoiDungThuNgan(), BorderLayout.CENTER);

        // Nạp dữ liệu ban đầu
        khoiTaoDuLieu();
        chuyenCheDo(cheDoKhoiTao);
    }

    /**
     * Thanh nút chuyển chế độ nghiệp vụ Sửa chữa / Bán lẻ phụ tùng Shop
     */
    private JPanel taoThanhChonCheDo() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        JLabel lbl = new JLabel("Nghiệp vụ thanh toán:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));
        bar.add(lbl);

        btnCheDoSuaChua = taoNutChonCheDo(" Thanh toán Sửa chữa / Dịch vụ xe", UiIcon.WRENCH, cheDoHienTai == CHE_DO_SUA_CHUA);
        btnCheDoBanLe = taoNutChonCheDo(" Bán lẻ Phụ tùng / Vật tư (Shop)", UiIcon.BOX, cheDoHienTai == CHE_DO_BAN_LE);

        btnCheDoSuaChua.addActionListener(e -> chuyenCheDo(CHE_DO_SUA_CHUA));
        btnCheDoBanLe.addActionListener(e -> chuyenCheDo(CHE_DO_BAN_LE));

        bar.add(btnCheDoSuaChua);
        bar.add(btnCheDoBanLe);
        return bar;
    }

    private JButton taoNutChonCheDo(String text, String iconType, boolean active) {
        JButton btn = new JButton(text, UiIcon.of(iconType, 16, active ? Color.WHITE : UiHelper.XANH_TRUNG)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean act = (this == btnCheDoSuaChua && cheDoHienTai == CHE_DO_SUA_CHUA)
                        || (this == btnCheDoBanLe && cheDoHienTai == CHE_DO_BAN_LE);
                boolean hover = getModel().isRollover();

                Color bg = act ? UiHelper.XANH_TRUNG : (hover ? new Color(241, 245, 249) : Color.WHITE);
                Color border = act ? UiHelper.XANH_TRUNG : (hover ? new Color(148, 163, 184) : new Color(203, 213, 225));

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(act ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();

                setForeground(act ? Color.WHITE : new Color(30, 41, 59));
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
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
            lblTdeTrang.setText("Quầy thu ngân — Thanh toán Sửa chữa xe");
            lblPhuTrang.setText("Thanh toán theo phiếu sửa chữa xe, quét mã VietQR tự động, quản lý quầy thu ngân và xuất hóa đơn dịch vụ");
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
            btnCheDoSuaChua.setIcon(UiIcon.of(UiIcon.WRENCH, 16, mode == CHE_DO_SUA_CHUA ? Color.WHITE : UiHelper.XANH_TRUNG));
            btnCheDoBanLe.setIcon(UiIcon.of(UiIcon.BOX, 16, mode == CHE_DO_BAN_LE ? Color.WHITE : UiHelper.XANH_TRUNG));
            btnCheDoSuaChua.repaint();
            btnCheDoBanLe.repaint();
        }
    }

    // ===============================================================
    // BỐ CỤC NỘI DUNG CHÍNH
    // ===============================================================
    private JPanel taoNoiDungThuNgan() {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);

        // Panel nghiệp vụ có CardLayout: Sửa chữa vs Bán lẻ Shop
        panelNghiepVu = new JPanel(cardLayoutNghiepVu);
        panelNghiepVu.setOpaque(false);

        // Card 1: Nghiệp vụ sửa chữa
        JPanel cardSuaChua = new JPanel(new BorderLayout(10, 0));
        cardSuaChua.setOpaque(false);
        cardSuaChua.add(taoFormThongTin(), BorderLayout.WEST);
        cardSuaChua.add(taoBangChiTiet(), BorderLayout.CENTER);
        panelNghiepVu.add(cardSuaChua, "SUA_CHUA");

        // Card 2: Nghiệp vụ bán lẻ phụ tùng (Shop)
        JPanel cardBanLe = new JPanel(new BorderLayout(10, 0));
        cardBanLe.setOpaque(false);
        cardBanLe.add(taoFormThongTinBanLe(), BorderLayout.WEST);
        cardBanLe.add(taoBangChiTietBanLe(), BorderLayout.CENTER);
        panelNghiepVu.add(cardBanLe, "BAN_LE");

        // Panel dưới: Box PHƯƠNG THỨC THANH TOÁN & QUẦY THU NGÂN (Dùng chung)
        JPanel botPanel = taoBoxQuayThuNgan();

        wrap.add(panelNghiepVu, BorderLayout.NORTH);
        wrap.add(botPanel, BorderLayout.CENTER);

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
    // GIAO DIỆN CHẾ ĐỘ 1: SỬA CHỮA & DỊCH VỤ XE
    // ===============================================================
    private JPanel taoFormThongTin() {
        JPanel p = UiHelper.panelForm("Thông tin hóa đơn & sửa chữa", 470);
        p.setPreferredSize(new Dimension(470, 420));

        txtMaHD.setEditable(false);
        txtNgay.setEditable(false);
        UiHelper.chiChoNhapSo(txtSdt, 10);
        txtSdt.setEditable(false);
        txtBienSo.setEditable(false);
        txtPhieuSC.setEditable(false);

        java.awt.event.KeyAdapter tinhAdapter = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { tinhTongTien(); }
        };
        txtTienDichVu.addKeyListener(tinhAdapter);
        txtTienPhuTung.addKeyListener(tinhAdapter);
        txtGiamGia.addKeyListener(tinhAdapter);

        cboKhachHang.addActionListener(e -> chonKhachHang());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JPanel rowMaNgay = new JPanel(new GridLayout(1, 2, 6, 0));
        rowMaNgay.setOpaque(false);
        rowMaNgay.add(txtMaHD);
        rowMaNgay.add(txtNgay);
        UiHelper.themDong(form, 0, "Mã HĐ / Ngày", rowMaNgay, 3, 3);

        UiHelper.themDong(form, 1, "Khách hàng *", cboKhachHang, 3, 3);

        JPanel rowSdtXe = new JPanel(new GridLayout(1, 2, 6, 0));
        rowSdtXe.setOpaque(false);
        rowSdtXe.add(txtSdt);
        rowSdtXe.add(txtBienSo);
        UiHelper.themDong(form, 2, "SĐT / Biển số xe", rowSdtXe, 3, 3);

        UiHelper.themDong(form, 3, "Phiếu sửa chữa", txtPhieuSC, 3, 3);

        JPanel rowTienCong = new JPanel(new GridLayout(1, 2, 6, 0));
        rowTienCong.setOpaque(false);
        rowTienCong.add(txtTienDichVu);
        rowTienCong.add(txtTienPhuTung);
        UiHelper.themDong(form, 4, "Tiền công / Phụ tùng", rowTienCong, 3, 3);

        UiHelper.themDong(form, 5, "Giảm giá (%)", txtGiamGia, 3, 3);
        UiHelper.themDong(form, 6, "Ghi chú hóa đơn", txtGhiChu, 3, 3);

        JTextField txtTrangThai = new JTextField("Đã thanh toán (Mặc định khi thu tiền)");
        txtTrangThai.setEditable(false);
        txtTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtTrangThai.setForeground(new Color(22, 163, 74));
        UiHelper.trangTriInput(txtTrangThai);
        UiHelper.themDong(form, 7, "Trạng thái", txtTrangThai, 3, 3);

        JPanel boxTong = taoBoxTongTienUI(lblTongTien);
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
        tblChiTiet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        dinhDangCotBang(tblChiTiet, 0, SwingConstants.CENTER, 110);
        dinhDangCotBang(tblChiTiet, 1, SwingConstants.LEFT, 350);
        dinhDangCotBang(tblChiTiet, 2, SwingConstants.CENTER, 65);
        dinhDangCotBang(tblChiTiet, 3, SwingConstants.CENTER, 55);
        dinhDangCotBang(tblChiTiet, 4, SwingConstants.RIGHT, 120);
        dinhDangCotBang(tblChiTiet, 5, SwingConstants.RIGHT, 135);

        JScrollPane sp = new JScrollPane(tblChiTiet);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setPreferredSize(new Dimension(0, 140));
        UiHelper.danhSachBong(sp);
        p.add(sp, BorderLayout.CENTER);

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
    // GIAO DIỆN CHẾ ĐỘ 2: BÁN LẺ PHỤ TÙNG (SHOP)
    // ===============================================================
    private JPanel taoFormThongTinBanLe() {
        JPanel p = UiHelper.panelForm("Thông tin khách mua & hóa đơn bán lẻ", 470);
        p.setPreferredSize(new Dimension(470, 420));

        txtMaHDBanLe.setEditable(false);
        txtNgayBanLe.setEditable(false);
        UiHelper.chiChoNhapSo(txtSdtBanLe, 10);
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

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JPanel rowMaNgay = new JPanel(new GridLayout(1, 2, 6, 0));
        rowMaNgay.setOpaque(false);
        rowMaNgay.add(txtMaHDBanLe);
        rowMaNgay.add(txtNgayBanLe);
        UiHelper.themDong(form, 0, "Mã HĐ / Ngày", rowMaNgay, 3, 3);

        UiHelper.themDong(form, 1, "Khách hàng *", cboKhachHangBanLe, 3, 3);

        JPanel rowSdtDc = new JPanel(new GridLayout(1, 2, 6, 0));
        rowSdtDc.setOpaque(false);
        rowSdtDc.add(txtSdtBanLe);
        rowSdtDc.add(txtDiaChiBanLe);
        UiHelper.themDong(form, 2, "SĐT / Địa chỉ", rowSdtDc, 3, 3);

        UiHelper.themDong(form, 3, "Thu ngân", txtNhanVienBanLe, 3, 3);
        UiHelper.themDong(form, 4, "Tiền hàng phụ tùng", txtTienPhuTungBanLe, 3, 3);
        UiHelper.themDong(form, 5, "Giảm giá (%)", txtGiamGiaBanLe, 3, 3);
        UiHelper.themDong(form, 6, "Ghi chú đơn hàng", txtGhiChuBanLe, 3, 3);

        JTextField txtTrangThai = new JTextField("Đã thanh toán (Mặc định khi thu tiền)");
        txtTrangThai.setEditable(false);
        txtTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtTrangThai.setForeground(new Color(22, 163, 74));
        UiHelper.trangTriInput(txtTrangThai);
        UiHelper.themDong(form, 7, "Trạng thái", txtTrangThai, 3, 3);

        JPanel boxTong = taoBoxTongTienUI(lblTongTienBanLe);
        JPanel bot = new JPanel(new BorderLayout(0, 6));
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(6, 0, 0, 0));
        bot.add(boxTong, BorderLayout.CENTER);

        UiHelper.ganNoiDungForm(p, form, bot);
        return p;
    }

    private JPanel taoBangChiTietBanLe() {
        JPanel p = UiHelper.panelBang("Danh mục phụ tùng bán lẻ tại Shop (Xuất trực tiếp từ Kho)");

        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        // Hàng 1: Tìm kiếm nhanh + Chọn phụ tùng
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblTim = new JLabel("Tìm nhanh:");
        lblTim.setFont(UiHelper.FONT_NHAN);
        toolbar.add(lblTim, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35;
        txtTimPTShop.setPreferredSize(new Dimension(160, 34));
        UiHelper.trangTriInput(txtTimPTShop);
        txtTimPTShop.setToolTipText("Gõ tên hoặc mã phụ tùng để lọc");
        txtTimPTShop.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locPhuTungShop(txtTimPTShop.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { locPhuTungShop(txtTimPTShop.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { locPhuTungShop(txtTimPTShop.getText()); }
        });
        toolbar.add(txtTimPTShop, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblPt = new JLabel("Phụ tùng:");
        lblPt.setFont(UiHelper.FONT_NHAN);
        toolbar.add(lblPt, gbc);

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
                    if (pt.getSoLuongTon() <= 0) {
                        setForeground(new Color(220, 38, 38));
                    } else if (pt.getSoLuongTon() <= 5) {
                        setForeground(new Color(217, 119, 6));
                    } else {
                        setForeground(new Color(15, 23, 42));
                    }
                }
                return this;
            }
        });
        toolbar.add(cboPhuTungShop, gbc);

        // Hàng 2: Số lượng + Nút Thêm vào đơn + Nút Xóa dòng
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);

        JLabel lblSl = new JLabel("Số lượng bán:");
        lblSl.setFont(UiHelper.FONT_NHAN);
        row2.add(lblSl);

        spSoLuongShop.setPreferredSize(new Dimension(80, 34));
        spSoLuongShop.setFont(UiHelper.FONT_BT);
        row2.add(spSoLuongShop);

        JButton btnThem = UiHelper.nut("+ Thêm vào đơn", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> themPhuTungShop());
        btnThem.setPreferredSize(new Dimension(140, 34));
        row2.add(btnThem);

        JButton btnXoa = UiHelper.nut("Xóa dòng", UiIcon.of(UiIcon.TRASH, 15), UiHelper.CHU_CHAY, e -> xoaPhuTungShop());
        btnXoa.setPreferredSize(new Dimension(110, 34));
        row2.add(btnXoa);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4; gbc.weightx = 1.0;
        toolbar.add(row2, gbc);

        p.add(toolbar, BorderLayout.NORTH);

        modelChiTietBanLe = UiHelper.model("Mã PT", "Tên phụ tùng / Vật tư", "ĐVT", "SL", "Đơn giá (đ)", "Thành tiền (đ)");
        tblChiTietBanLe = UiHelper.bang(modelChiTietBanLe);
        tblChiTietBanLe.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        dinhDangCotBang(tblChiTietBanLe, 0, SwingConstants.CENTER, 90);
        dinhDangCotBang(tblChiTietBanLe, 1, SwingConstants.LEFT, 260);
        dinhDangCotBang(tblChiTietBanLe, 2, SwingConstants.CENTER, 65);
        dinhDangCotBang(tblChiTietBanLe, 3, SwingConstants.CENTER, 55);
        dinhDangCotBang(tblChiTietBanLe, 4, SwingConstants.RIGHT, 115);
        dinhDangCotBang(tblChiTietBanLe, 5, SwingConstants.RIGHT, 130);

        JScrollPane sp = new JScrollPane(tblChiTietBanLe);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setPreferredSize(new Dimension(0, 140));
        UiHelper.danhSachBong(sp);
        p.add(sp, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 4, 4));
        lblTongPhuTungBanLe.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongPhuTungBanLe.setForeground(new Color(51, 65, 85));
        footer.add(lblTongPhuTungBanLe, BorderLayout.WEST);

        p.add(footer, BorderLayout.SOUTH);
        return p;
    }

    private JPanel taoBoxTongTienUI(JLabel lblMoney) {
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

        lblMoney.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMoney.setForeground(new Color(220, 38, 38));
        lblMoney.setHorizontalAlignment(SwingConstants.RIGHT);

        boxTong.add(lblTdeTong, BorderLayout.WEST);
        boxTong.add(lblMoney, BorderLayout.CENTER);
        return boxTong;
    }

    private void dinhDangCotBang(JTable table, int cot, int canLe, int doRong) {
        TableColumn col = table.getColumnModel().getColumn(cot);
        col.setPreferredWidth(doRong);
        int left = (canLe == SwingConstants.LEFT) ? 12 : 4;
        int right = (canLe == SwingConstants.RIGHT) ? 12 : 4;

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
                        new EmptyBorder(6, left, 6, right)
                ));
                return l;
            }
        });

        col.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(canLe);
                l.setFont(new Font("Segoe UI", (cot == 5) ? Font.BOLD : Font.PLAIN, 12));
                l.setBackground(s ? new Color(219, 234, 254) : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                l.setForeground(s ? UiHelper.XANH_TRUNG : new Color(30, 41, 59));
                l.setBorder(new EmptyBorder(4, left, 4, right));
                return l;
            }
        });
    }

    // ===============================================================
    // KHUNG PHƯƠNG THỨC THANH TOÁN & QUẦY THU NGÂN (DÙNG CHUNG)
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
        box.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Header quầy thu ngân
        JPanel headRow = new JPanel(new BorderLayout());
        headRow.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẦY THU NGÂN & PHƯƠNG THỨC THANH TOÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(15, 23, 42));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tabs.setOpaque(false);
        btnQrTab = taoNutTabPhuongThuc("Chuyển khoản QR (VietQR)", UiIcon.BILL, true);
        btnTienMatTab = taoNutTabPhuongThuc("Tiền mặt", UiIcon.BILL, false);

        btnQrTab.addActionListener(e -> doiPhuongThuc("Chuyển khoản QR"));
        btnTienMatTab.addActionListener(e -> doiPhuongThuc("Tiền mặt"));

        tabs.add(btnQrTab);
        tabs.add(btnTienMatTab);

        headRow.add(lblTitle, BorderLayout.WEST);
        headRow.add(tabs, BorderLayout.EAST);
        box.add(headRow, BorderLayout.NORTH);

        // Body: 2 cột
        JPanel bodyRow = new JPanel(new GridLayout(1, 2, 14, 0));
        bodyRow.setOpaque(false);

        bodyRow.add(taoCotPhuongThucTra());

        panelCotPhai = new JPanel(cardLayoutCotPhai);
        panelCotPhai.setOpaque(false);
        panelCotPhai.add(taoCotVietQR(), "QR");
        panelCotPhai.add(taoCotTienMat(), "CASH");
        cardLayoutCotPhai.show(panelCotPhai, "QR");
        bodyRow.add(panelCotPhai);

        box.add(bodyRow, BorderLayout.CENTER);

        // Footer quầy: Nút thao tác
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, false, e -> lamMoiForm());
        btnLamMoi.setPreferredSize(new Dimension(110, 40));

        JButton btnInHoaDon = UiHelper.nut("In hóa đơn", UiIcon.of(UiIcon.PRINT, 16), UiHelper.XANH_TRUNG, true, e -> xemVaInHoaDon());
        btnInHoaDon.setPreferredSize(new Dimension(140, 40));

        JButton btnXacNhan = UiHelper.nut("XÁC NHẬN THANH TOÁN", UiHelper.XANH_LA, e -> thucHienThanhToan());
        btnXacNhan.setPreferredSize(new Dimension(210, 40));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));

        actionRow.add(btnLamMoi);
        actionRow.add(btnInHoaDon);
        actionRow.add(btnXacNhan);

        box.add(actionRow, BorderLayout.SOUTH);
        return box;
    }

    private JPanel taoCotPhuongThucTra() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        panelTienTra = new JPanel(cardLayoutTienTra);
        panelTienTra.setOpaque(false);

        // Subcard 1: Hướng dẫn QR
        JPanel qrCard = new JPanel();
        qrCard.setLayout(new BoxLayout(qrCard, BoxLayout.Y_AXIS));
        qrCard.setOpaque(false);

        JLabel lblHd = new JLabel("<html><b>HƯỚNG DẪN THANH TOÁN VIETQR:</b><br>"
                + "• Mã QR động bên phải đã tích hợp sẵn số tiền thanh toán chính xác.<br>"
                + "• Hỗ trợ mọi ứng dụng Ngân hàng (VCB, MB, BIDV, Techcombank,...) và Ví điện tử.<br>"
                + "• Khi chuyển khoản, hệ thống không yêu cầu tiền thừa hay thối lại tiền lẻ.</html>");
        lblHd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHd.setForeground(new Color(71, 85, 105));
        qrCard.add(lblHd);
        panelTienTra.add(qrCard, "QR");

        // Subcard 2: Tiền mặt
        JPanel cashCard = new JPanel();
        cashCard.setLayout(new BoxLayout(cashCard, BoxLayout.Y_AXIS));
        cashCard.setOpaque(false);

        JPanel rowNhap = new JPanel(new BorderLayout(8, 0));
        rowNhap.setOpaque(false);
        JLabel lblNhapKd = new JLabel("Tiền khách đưa (VNĐ):");
        lblNhapKd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNhapKd.setForeground(new Color(15, 23, 42));

        txtTienKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTienKhachDua.setPreferredSize(new Dimension(160, 36));
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

        // Phím đếm nhanh
        JPanel quickButtons = new JPanel(new GridLayout(2, 3, 6, 6));
        quickButtons.setOpaque(false);
        quickButtons.add(taoNutDemNhanh("10,000 đ", 10000));
        quickButtons.add(taoNutDemNhanh("20,000 đ", 20000));
        quickButtons.add(taoNutDemNhanh("50,000 đ", 50000));
        quickButtons.add(taoNutDemNhanh("100,000 đ", 100000));
        quickButtons.add(taoNutDemNhanh("200,000 đ", 20000));
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
        cardLayoutTienTra.show(panelTienTra, "QR");

        col.add(panelTienTra);
        return col;
    }

    private JButton taoNutTabPhuongThuc(String text, String iconType, boolean active) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean act = Boolean.TRUE.equals(getClientProperty("activeTab"));
                boolean hover = getModel().isRollover();

                Color bg = act ? UiHelper.XANH_TRUNG : (hover ? new Color(226, 232, 240) : new Color(241, 245, 249));
                Color border = act ? UiHelper.XANH_TRUNG : (hover ? new Color(148, 163, 184) : new Color(203, 213, 225));

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();

                setForeground(act ? Color.WHITE : new Color(71, 85, 105));
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
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

    private long layTongTienHienTai() {
        try {
            return Long.parseLong(lblTongTien.getText().replace(" đ", "").replace(",", "").replace(".", "").trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private void dongBoQR(long tong) {
        lblQrSoTien.setText(tong > 0 ? String.format("%,d VNĐ", tong) : "0 VNĐ");
        String ma = (cheDoHienTai == CHE_DO_SUA_CHUA ? txtMaHD.getText().trim() : txtMaHDBanLe.getText().trim());
        String xe = (cheDoHienTai == CHE_DO_SUA_CHUA ? txtBienSo.getText().trim().replace("-", "").replace(".", "").replace(" ", "") : "SHOP");
        if (tong > 0 && !ma.isEmpty()) {
            lblQrNoiDung.setText("TT " + ma + (xe.isEmpty() ? "" : " " + xe));
        } else {
            lblQrNoiDung.setText("Chờ nhập tiền...");
        }
        if (qrCanvas != null) qrCanvas.repaint();
    }

    private void dongBoTienMat(long tong) {
        lblTienMatCanThu.setText(UiHelper.tien(tong) + " đ");
        String raw = txtTienKhachDua.getText().replace(",", "").replace(".", "").trim();
        long khach = raw.isEmpty() ? 0 : Long.parseLong(raw);
        lblTienMatKhachDua.setText(UiHelper.tien(khach) + " đ");
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

    private JPanel taoCotVietQR() {
        JPanel col = new JPanel(new BorderLayout(10, 0));
        col.setOpaque(false);

        qrCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h) - 10;
                int ox = (w - size) / 2;
                int oy = (h - size) / 2;

                long tong = layTongTienHienTai();
                if (tong <= 0) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(ox, oy, size, size, 8, 8);
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRoundRect(ox, oy, size, size, 8, 8);

                    g2.setColor(new Color(148, 163, 184));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    String s1 = "[ CHƯA CÓ SỐ TIỀN ]";
                    g2.drawString(s1, ox + (size - fm.stringWidth(s1)) / 2, oy + size / 2 - 4);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    fm = g2.getFontMetrics();
                    String s2 = "Nhập tiền để hiện mã QR";
                    g2.drawString(s2, ox + (size - fm.stringWidth(s2)) / 2, oy + size / 2 + 14);
                    g2.dispose();
                    return;
                }

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(ox, oy, size, size, 8, 8);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(ox, oy, size, size, 8, 8);

                veOVuongDinhVi(g2, ox + 14, oy + 14, 30);
                veOVuongDinhVi(g2, ox + size - 44, oy + 14, 30);
                veOVuongDinhVi(g2, ox + 14, oy + size - 44, 30);

                g2.setColor(new Color(15, 23, 42));
                int step = 6;
                String ma = (cheDoHienTai == CHE_DO_SUA_CHUA ? txtMaHD.getText() : txtMaHDBanLe.getText());
                int seed = (ma + "_" + tong).hashCode();
                for (int x = ox + 14; x < ox + size - 14; x += step) {
                    for (int y = oy + 14; y < oy + size - 14; y += step) {
                        if (x < ox + 48 && y < oy + 48) continue;
                        if (x > ox + size - 48 && y < oy + 48) continue;
                        if (x < ox + 48 && y > oy + size - 48) continue;
                        if (x > ox + size / 2 - 18 && x < ox + size / 2 + 18 && y > oy + size / 2 - 18 && y < oy + size / 2 + 18) continue;

                        int hash = Math.abs((x * 37 + y * 19 + seed) % 100);
                        if (hash > 46) {
                            g2.fillRect(x, y, step - 1, step - 1);
                        }
                    }
                }

                int cx = ox + size / 2;
                int cy = oy + size / 2;
                int badgeW = 34, badgeH = 22;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(cx - badgeW / 2 - 2, cy - badgeH / 2 - 2, badgeW + 4, badgeH + 4, 6, 6);
                g2.setColor(new Color(30, 58, 138));
                g2.fillRoundRect(cx - badgeW / 2, cy - badgeH / 2, badgeW, badgeH, 4, 4);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("•• VQR", cx - badgeW / 2 + 3, cy + 4);

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
        lblQrNoiDung.setForeground(new Color(37, 99, 235));
        rowNd.add(lblNdTde, BorderLayout.WEST);
        rowNd.add(lblQrNoiDung, BorderLayout.EAST);

        JPanel rowTk = new JPanel(new BorderLayout());
        rowTk.setOpaque(false);
        JLabel lblTkTde = new JLabel("Thụ hưởng:");
        lblTkTde.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTkTde.setForeground(new Color(100, 116, 139));
        JLabel lblTkVal = new JLabel(cauHinh.getStkNganHang() != null ? cauHinh.getStkNganHang() : "1028888888 (Vietcombank)");
        lblTkVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTkVal.setForeground(new Color(15, 23, 42));
        rowTk.add(lblTkTde, BorderLayout.WEST);
        rowTk.add(lblTkVal, BorderLayout.EAST);

        infoQr.add(rowSt);
        infoQr.add(Box.createVerticalStrut(4));
        infoQr.add(rowNd);
        infoQr.add(Box.createVerticalStrut(4));
        infoQr.add(rowTk);

        col.add(infoQr, BorderLayout.SOUTH);
        return col;
    }

    private JPanel taoCotTienMat() {
        JPanel col = new JPanel(new BorderLayout(8, 8));
        col.setOpaque(false);
        col.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel grid = new JPanel(new GridLayout(3, 2, 6, 8));
        grid.setOpaque(false);

        JLabel l1 = new JLabel("Tổng tiền cần thu:");
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTienMatCanThu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTienMatCanThu.setForeground(new Color(15, 23, 42));

        JLabel l2 = new JLabel("Khách đưa:");
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTienMatKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTienMatKhachDua.setForeground(new Color(37, 99, 235));

        JLabel l3 = new JLabel("Tiền thối lại:");
        l3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTienMatThua.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTienMatThua.setForeground(new Color(22, 163, 74));

        grid.add(l1); grid.add(lblTienMatCanThu);
        grid.add(l2); grid.add(lblTienMatKhachDua);
        grid.add(l3); grid.add(lblTienMatThua);

        col.add(grid, BorderLayout.CENTER);
        return col;
    }

    // ===============================================================
    // Xử lý hóa đơn sửa chữa
    // ===============================================================
    private void khoiTaoDuLieu() {
        txtMaHD.setText(hoaDonDAO.sinhMaHoaDonTuDong());
        txtNgay.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtTkThuHuong.setText(cauHinh.getStkNganHang() != null ? cauHinh.getStkNganHang() : "1028888888 (Vietcombank - The T4 Auto)");
        txtMaThamChieu.setText("BILL" + System.currentTimeMillis() % 1000000);

        // 1. Nạp danh sách khách hàng sửa chữa
        cboKhachHang.removeAllItems();
        dsPhieuDangChon.clear();
        cboKhachHang.addItem("-- Chọn khách hàng có xe cần thanh toán --");

        for (PhieuTiepNhan ptn : kho.getPhieuTiepNhans()) {
            if ("Hoàn thành".equalsIgnoreCase(ptn.getTienDo()) || "Đang sửa chữa".equalsIgnoreCase(ptn.getTienDo())) {
                boolean daLap = false;
                for (HoaDon hd : kho.getHoaDons()) {
                    if (ptn.getSoPhieu().equalsIgnoreCase(hd.getSoPhieuSC()) && !HoaDon.DA_HUY.equals(hd.getTrangThai())) {
                        daLap = true;
                        break;
                    }
                }
                if (!daLap) {
                    dsPhieuDangChon.add(ptn);
                    cboKhachHang.addItem(ptn.getHoTen() + " — Xe " + ptn.getBienSo() + " (" + ptn.getSoPhieu() + ")");
                }
            }
        }

        // 2. Nạp dữ liệu bán lẻ Shop
        txtMaHDBanLe.setText(hoaDonDAO.sinhMaHoaDonTuDong());
        txtNgayBanLe.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtNhanVienBanLe.setText(nvHienTai != null ? nvHienTai.getHoTen() : "Thu ngân");

        napKhachHangBanLe();
        napPhuTungShop();
    }

    private void chonKhachHang() {
        int idx = cboKhachHang.getSelectedIndex();
        if (idx <= 0 || idx > dsPhieuDangChon.size()) {
            txtSdt.setText("");
            txtBienSo.setText("");
            txtPhieuSC.setText("");
            txtTienDichVu.setText("0");
            txtTienPhuTung.setText("0");
            modelChiTiet.setRowCount(0);
            tinhTongTien();
            return;
        }

        PhieuTiepNhan ptn = dsPhieuDangChon.get(idx - 1);
        txtSdt.setText(ptn.getSdt() != null ? ptn.getSdt() : "");
        txtBienSo.setText(ptn.getBienSo() != null ? ptn.getBienSo() : "");
        txtPhieuSC.setText(ptn.getSoPhieu() != null ? ptn.getSoPhieu() : "");

        modelChiTiet.setRowCount(0);
        List<ChiTietDichVu> ds = kho.layChiTietTheoPhieu(ptn.getSoPhieu());
        long tongCong = 0, tongPT = 0;

        for (ChiTietDichVu ct : ds) {
            modelChiTiet.addRow(new Object[]{
                    ct.getLoai(), ct.getTenHangMuc(), ct.getDonVi(),
                    ct.getSoLuong(), String.format("%,d", ct.getDonGia()), String.format("%,d", ct.getThanhTien())
            });
            if ("Tiền công".equalsIgnoreCase(ct.getLoai())) tongCong += ct.getThanhTien();
            else tongPT += ct.getThanhTien();
        }

        txtTienDichVu.setText(String.format("%,d", tongCong));
        txtTienPhuTung.setText(String.format("%,d", tongPT));
        tinhTongTien();
    }

    private long tinhTongTien() {
        try {
            long cong = Long.parseLong(txtTienDichVu.getText().trim().replace(",", "").replace(".", ""));
            long pt = Long.parseLong(txtTienPhuTung.getText().trim().replace(",", "").replace(".", ""));
            double ck = Double.parseDouble(txtGiamGia.getText().trim());

            long tongChiPhi = cong + pt;
            lblTongChiPhiBang.setText("Tổng chi phí dịch vụ & vật tư: " + UiHelper.tien(tongChiPhi) + " đ");

            long giam = Math.round(tongChiPhi * (ck / 100.0));
            long sauGiam = Math.max(0, tongChiPhi - giam);

            lblTongTien.setText(UiHelper.tien(sauGiam) + " đ");

            if ("Chuyển khoản QR".equals(phuongThucChon)) {
                dongBoQR(sauGiam);
            } else {
                dongBoTienMat(sauGiam);
            }

            tinhTienThua();
            return sauGiam;
        } catch (Exception ex) {
            lblTongTien.setText("0 đ");
            if ("Chuyển khoản QR".equals(phuongThucChon)) {
                dongBoQR(0);
            } else {
                dongBoTienMat(0);
            }
            return 0;
        }
    }

    private void thucHienThanhToanSuaChua() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.THANH_TOAN)) return;
        int idx = cboKhachHang.getSelectedIndex();
        if (idx <= 0 || idx > dsPhieuDangChon.size()) {
            UiHelper.loi(this, "Vui lòng chọn khách hàng đã sửa chữa cần thanh toán!", cboKhachHang);
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
            JOptionPane.showMessageDialog(this, "Không lưu được hóa đơn. Kiểm tra phiếu sửa chữa, chi tiết và kết nối SQL Server.", "Lưu thất bại", JOptionPane.ERROR_MESSAGE);
            return;
        }
        kho.dongBoTuSQLServer();

        int luaChonBH = JOptionPane.showConfirmDialog(this,
                "Thanh toán hóa đơn [" + hd.getMaHD() + "] thành công!" + (ok ? " (Đã lưu CSDL SQL Server)\n" : " (Đang lưu trong bộ nhớ)\n")
                        + "Khách hàng: " + hd.getTenKH() + " (" + hd.getBienSo() + ")\n"
                        + "Số tiền: " + UiHelper.tien(tong) + " đ (" + phuongThucChon + ")\n"
                        + "Phiếu sửa chữa " + ptn.getSoPhieu() + " đã hoàn tất.\n\n"
                        + "Bạn có muốn chuyển sang phân hệ \"Lập phiếu bảo hành\" cho hóa đơn này ngay không?",
                "Thanh toán thành công & Lập bảo hành", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

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
        lblTongTien.setText("0 đ");
        lblQrSoTien.setText("0 VNĐ");
        lblQrNoiDung.setText("—");
        modelChiTiet.setRowCount(0);
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
                + "<tr><td align='right' width='70%'>Tiền dịch vụ / Tiền công:</td><td align='right'><b>" + txtTienDichVu.getText() + " đ</b></td></tr>"
                + "<tr><td align='right'>Tiền vật tư / Phụ tùng:</td><td align='right'><b>" + txtTienPhuTung.getText() + " đ</b></td></tr>"
                + "<tr><td align='right'>Giảm giá chiết khấu:</td><td align='right'><b>" + txtGiamGia.getText() + "%</b></td></tr>"
                + "<tr style='background:#EFF6FF;'><td align='right'><b style='color:#1E3A8A;font-size:14px;'>TỔNG THANH TOÁN:</b></td>"
                + "<td align='right'><b style='color:#DC2626;font-size:16px;'>" + UiHelper.tien(tong) + " đ</b></td></tr>"
                + "<tr><td colspan='2' style='font-style:italic;color:#475569;'>Bằng chữ: <b>" + docSoThanhChu(tong) + "</b></td></tr>"
                + "</table>"
                + "<table style='width:100%;margin-top:20px;text-align:center;font-size:12px;'>"
                + "<tr><td width='50%'><b>Khách hàng</b><br><span style='font-size:10px;color:#94A3B8;'>(Ký, ghi rõ họ tên)</span><br><br><br><br><b>" + tenKH + "</b></td>"
                + "<td width='50%'><b>Người lập phiếu</b><br><span style='font-size:10px;color:#94A3B8;'>(Ký, đóng dấu)</span><br><br><br><br><b>" + thuNgan + "</b></td></tr>"
                + "</table>"
                + "</body></html>";

        hienThiHopThoaiIn(html, "Hóa đơn dịch vụ " + maHD);
    }

    // ===============================================================
    // Xử lý hóa đơn bán lẻ
    // ===============================================================
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phụ tùng từ danh mục!", "Chưa chọn phụ tùng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sl = (Integer) spSoLuongShop.getValue();
        if (sl <= 0) {
            JOptionPane.showMessageDialog(this, "Số lượng bán phải lớn hơn 0!", "Số lượng không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int tonKho = pt.getSoLuongTon();
        if (sl > tonKho) {
            JOptionPane.showMessageDialog(this, "Số lượng bán (" + sl + ") vượt quá tồn kho hiện có (" + tonKho + ")!", "Không đủ tồn kho", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra xem đã có trong bảng chưa để cộng dồn
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
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng phụ tùng trong bảng để xóa!", "Chưa chọn dòng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modelChiTietBanLe.removeRow(row);
        tinhTongTienBanLe();
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
        lblTongPhuTungBanLe.setText("Tổng số: " + tongSoLuong + " món (" + modelChiTietBanLe.getRowCount() + " loại) | Tiền hàng: " + UiHelper.tien(tongTienHang) + " đ");

        double ck = 0;
        try {
            ck = Double.parseDouble(txtGiamGiaBanLe.getText().trim());
        } catch (Exception ignored) {}

        long giam = Math.round(tongTienHang * (ck / 100.0));
        long sauGiam = Math.max(0, tongTienHang - giam);

        lblTongTienBanLe.setText(UiHelper.tien(sauGiam) + " đ");
        lblTongTien.setText(UiHelper.tien(sauGiam) + " đ");

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
            JOptionPane.showMessageDialog(this, "Đơn bán lẻ chưa có phụ tùng nào! Vui lòng chọn phụ tùng và bấm '+ Thêm vào đơn'.", "Chưa có mặt hàng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long tong = tinhTongTienBanLe();
        if (tong <= 0) {
            JOptionPane.showMessageDialog(this, "Tổng thanh toán phải lớn hơn 0 đồng!", "Lỗi thanh toán", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KhachHang kh = (KhachHang) cboKhachHangBanLe.getSelectedItem();
        if (kh == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng mua lẻ!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Tiền mặt".equals(phuongThucChon)) {
            String rawKhachDua = txtTienKhachDua.getText().replace(",", "").replace(".", "").trim();
            long khachDua = rawKhachDua.isEmpty() ? 0 : Long.parseLong(rawKhachDua);
            if (khachDua < tong) {
                JOptionPane.showMessageDialog(this, "Khách đưa chưa đủ tiền thanh toán! Còn thiếu " + UiHelper.tien(tong - khachDua) + " đ.", "Khách đưa thiếu", JOptionPane.WARNING_MESSAGE);
                txtTienKhachDua.requestFocus();
                return;
            }
        }

        String maHD = txtMaHDBanLe.getText().trim();
        String maNV = nvHienTai != null ? nvHienTai.getMaNV() : "NV05";
        double giamGia = 0;
        try {
            giamGia = Double.parseDouble(txtGiamGiaBanLe.getText().trim());
        } catch (Exception ignored) {}
        String ghiChu = txtGhiChuBanLe.getText().trim();
        if (ghiChu.isBlank()) {
            ghiChu = "Bán lẻ phụ tùng tại shop";
        }

        int cf = JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán hóa đơn bán lẻ [" + maHD + "]?\n"
                        + "Khách hàng: " + kh.getHoTen() + " (" + kh.getSdt() + ")\n"
                        + "Số món phụ tùng: " + modelChiTietBanLe.getRowCount() + " loại\n"
                        + "Tổng thanh toán: " + UiHelper.tien(tong) + " đ (" + phuongThucChon + ")",
                "Xác nhận thanh toán bán lẻ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (cf != JOptionPane.YES_OPTION) return;

        Map<String, Integer> chiTietMap = new LinkedHashMap<>();
        for (int i = 0; i < modelChiTietBanLe.getRowCount(); i++) {
            String maPT = String.valueOf(modelChiTietBanLe.getValueAt(i, 0)).trim();
            int sl = Integer.parseInt(String.valueOf(modelChiTietBanLe.getValueAt(i, 3)).trim());
            chiTietMap.merge(maPT, sl, Integer::sum);
        }

        try {
            boolean ok = hoaDonBanHangDAO.lapHoaDonBanHang(maHD, kh.getMaKH(), maNV, phuongThucChon, giamGia, ghiChu, chiTietMap);
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
                            + "Đã tự động cập nhật trừ tồn kho phụ tùng.\n\n"
                            + "Bạn có muốn in hóa đơn bán lẻ cho khách hàng ngay bây giờ không?",
                    "Thanh toán thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (inOpt == JOptionPane.YES_OPTION) {
                xemVaInHoaDonBanLe();
            }

            lamMoiFormBanLe();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu hóa đơn bán lẻ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lamMoiFormBanLe() {
        txtMaHDBanLe.setText(hoaDonDAO.sinhMaHoaDonTuDong());
        txtNgayBanLe.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtTienPhuTungBanLe.setText("0");
        txtGiamGiaBanLe.setText("0");
        txtGhiChuBanLe.setText("");
        lblTongTienBanLe.setText("0 đ");
        lblTongTien.setText("0 đ");
        modelChiTietBanLe.setRowCount(0);
        lblTongPhuTungBanLe.setText("Tổng số phụ tùng: 0 món | Tiền hàng: 0 đ");
        txtTimPTShop.setText("");
        napPhuTungShop();
        if (cboKhachHangBanLe.getItemCount() > 0) {
            cboKhachHangBanLe.setSelectedIndex(0);
        }
        if ("Chuyển khoản QR".equals(phuongThucChon)) {
            dongBoQR(0);
        } else {
            dongBoTienMat(0);
            tinhTienThua();
        }
    }

    private void xemVaInHoaDonBanLe() {
        long tong = tinhTongTienBanLe();
        String maHD = txtMaHDBanLe.getText().trim();
        String ngayLap = txtNgayBanLe.getText().trim();
        KhachHang kh = (KhachHang) cboKhachHangBanLe.getSelectedItem();
        String tenKH = kh != null ? kh.getHoTen() : "Khách mua lẻ tại shop";
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
        if (sbItems.length() == 0) {
            sbItems.append("<tr><td colspan='6' style='text-align:center;padding:12px;color:#94A3B8;'>Chưa có phụ tùng trong đơn</td></tr>");
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
                + "<tr><td><b>Phương thức:</b> " + phuongThucChon + "</td><td><b>Trạng thái:</b> Đã thanh toán</td></tr>"
                + "</table>"
                + "<h4 style='margin:14px 0 6px 0;'>DANH SÁCH PHỤ TÙNG & VẬT TƯ XUẤT BÁN</h4>"
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
                + "<tr><td align='right' width='70%'>Tổng tiền hàng phụ tùng:</td><td align='right'><b>" + txtTienPhuTungBanLe.getText() + " đ</b></td></tr>"
                + "<tr><td align='right'>Chiết khấu / Giảm giá:</td><td align='right'><b>" + txtGiamGiaBanLe.getText() + "%</b></td></tr>"
                + "<tr style='background:#EFF6FF;'><td align='right'><b style='color:#1E3A8A;font-size:14px;'>TỔNG THANH TOÁN:</b></td>"
                + "<td align='right'><b style='color:#DC2626;font-size:16px;'>" + UiHelper.tien(tong) + " đ</b></td></tr>"
                + "<tr><td colspan='2' style='font-style:italic;color:#475569;'>Bằng chữ: <b>" + docSoThanhChu(tong) + "</b></td></tr>"
                + "</table>"
                + "<table style='width:100%;margin-top:20px;text-align:center;font-size:12px;'>"
                + "<tr><td width='50%'><b>Khách hàng</b><br><span style='font-size:10px;color:#94A3B8;'>(Ký, ghi rõ họ tên)</span><br><br><br><br><b>" + tenKH + "</b></td>"
                + "<td width='50%'><b>Người bán hàng</b><br><span style='font-size:10px;color:#94A3B8;'>(Ký, đóng dấu)</span><br><br><br><br><b>" + thuNgan + "</b></td></tr>"
                + "</table>"
                + "</body></html>";

        hienThiHopThoaiIn(html, "Hóa đơn bán lẻ " + maHD);
    }

    private void hienThiHopThoaiIn(String html, String title) {
        JEditorPane editor = new JEditorPane("text/html", html);
        editor.setEditable(false);
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setPreferredSize(new Dimension(620, 560));

        Object[] options = {"In ngay", "Đóng"};
        int choice = JOptionPane.showOptionDialog(this, scroll, "Xem trước & In — " + title,
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            try {
                boolean done = editor.print();
                if (done) {
                    JOptionPane.showMessageDialog(this, "Lệnh in đã được gửi tới máy in thành công!", "Đã in", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Không thể in hóa đơn: " + ex.getMessage(), "Lỗi máy in", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===============================================================
    // CÁC HÀM ĐIỀU PHỐI CHUNG THEO CHẾ ĐỘ
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
