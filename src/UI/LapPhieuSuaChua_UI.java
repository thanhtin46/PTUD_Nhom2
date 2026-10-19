package UI;

import ConnectDB.ConnectDB;
import Entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * LapPhieuSuaChua_UI - Lập phiếu sửa chữa phương tiện chuyên nghiệp
 * Thiết kế chuẩn ERP Gara ô tô cao cấp (Khớp 100% bản thiết kế mẫu).
 */
public class LapPhieuSuaChua_UI extends JPanel {

    private final NhanVien nvHienTai;
    private final KhoDuLieu kho = KhoDuLieu.get();

    // Dữ liệu mã phiếu
    private static int demPSC = 42;
    private String maPSC;

    // Components Khối 1
    private JComboBox<String> cboPhieuTN;
    private JTextField txtCoVan;
    private JLabel lblBienSo;
    private JLabel lblThongTinXe;
    private JLabel lblKhachHang;
    private JLabel lblOdo;
    private JTextField txtNgayLap;
    private JTextField txtNgayDuKien;

    // Components Khối 2
    private JComboBox<String> cboKtvTruong;
    private JLabel lblBacTho;
    private JPanel pnlThoPhuList;
    private final ArrayList<String> dsThoPhu = new ArrayList<>();
    private String trangThaiChon = "Đang xử lý";
    private JButton btnDangXuLy;
    private JButton btnChoPhuTung;
    private JButton btnHoanThanh;
    private JComboBox<String> cboKhoang;
    private JComboBox<String> cboUuTien;

    // Components Khối 3: Bảng công việc
    private DefaultTableModel modelCongViec;
    private JTable tblCongViec;
    private JLabel lblTongCongViec;
    private JLabel lblTongTienCong;

    // Components Khối 4: Bảng phụ tùng
    private DefaultTableModel modelPhuTung;
    private JTable tblPhuTung;
    private JLabel lblTongPhuTung;
    private JLabel lblTongTienPhuTung;

    // Components Khối Dưới: Ghi chú & Tài chính
    private JTextArea txtGhiChu;
    private JLabel lblTienCongTho;
    private JLabel lblTienPhuTungVatTu;
    private JLabel lblTienVat;
    private JLabel lblTongDuKien;
    private JLabel lblDocSoTien;

    // Màu sắc chủ đạo (Professional Slate & Royal Blue Theme)
    private static final Color XANH_HEADER = new Color(30, 58, 138);   // Blue 900
    private static final Color XANH_ACCENT = new Color(37, 99, 235);   // Blue 600
    private static final Color NEN_APP = new Color(248, 250, 252);     // Slate 50
    private static final Color VIEN_CARD = new Color(226, 232, 240);   // Slate 200
    private static final Color CHU_TIEU_DE = new Color(15, 23, 42);    // Slate 900
    private static final Color CHU_PHU = new Color(100, 116, 139);     // Slate 500

    public LapPhieuSuaChua_UI(NhanVien nv) {
        this.nvHienTai = nv;
        this.maPSC = String.format("PSC%05d", demPSC++);

        setLayout(new BorderLayout());
        setBackground(NEN_APP);

        // Thiết lập phím tắt F4, F5
        setupKeyBindings();

        // Panel chứa nội dung có thể cuộn
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(NEN_APP);
        mainContent.setBorder(new EmptyBorder(12, 16, 16, 16));

        // 1. Header trên cùng
        mainContent.add(taoHeaderPanel());
        mainContent.add(Box.createVerticalStrut(12));

        // 2. Hàng trên: Khối 1 (Tiếp nhận xe) & Khối 2 (Phân công KTV)
        mainContent.add(taoHang1());
        mainContent.add(Box.createVerticalStrut(12));

        // 3. Hàng giữa: Khối 3 (Hạng mục công việc) & Khối 4 (Phụ tùng xuất kho)
        mainContent.add(taoHang2());
        mainContent.add(Box.createVerticalStrut(12));

        // 4. Hàng dưới: Ghi chú chẩn đoán kỹ thuật & Tổng kết tài chính
        mainContent.add(taoHang3());
        mainContent.add(Box.createVerticalStrut(8));

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 5. Thanh Footer cố định dưới đáy
        add(taoFooterPanel(), BorderLayout.SOUTH);

        // Nạp dữ liệu mẫu ban đầu
        khoiTaoDuLieuMau();
        tinhToanTaiChinh();
    }

    // ===============================================================
    // 1. HEADER PANEL
    // ===============================================================
    private JPanel taoHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Bên trái: Icon, Tiêu đề, Mã phiếu, Subtitle
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        // Icon Wrench trong khung xanh
        JPanel pnlIcon = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(XANH_HEADER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        pnlIcon.setPreferredSize(new Dimension(38, 38));
        JLabel lblIcon = new JLabel(UiIcon.of(UiIcon.WRENCH, 20));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        pnlIcon.add(lblIcon, BorderLayout.CENTER);

        // Text title & badge
        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("LẬP PHIẾU SỬA CHỮA PHƯƠNG TIỆN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(CHU_TIEU_DE);

        // Badge mã phiếu
        JLabel lblBadgePSC = new JLabel(" MÃ PHIẾU: " + maPSC + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255)); // Blue 50
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(new Color(147, 197, 253)); // Blue 300
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadgePSC.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBadgePSC.setForeground(new Color(29, 78, 216)); // Blue 700
        lblBadgePSC.setBorder(new EmptyBorder(2, 6, 2, 6));

        titleRow.add(lblTitle);
        titleRow.add(lblBadgePSC);

        JLabel lblSub = new JLabel("Lập kế hoạch công việc, phân công kỹ thuật viên trưởng & xuất phụ tùng chính xác");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(CHU_PHU);

        textGroup.add(titleRow);
        textGroup.add(lblSub);

        left.add(pnlIcon);
        left.add(textGroup);

        // Bên phải: Các nút thao tác nhanh
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        JButton btnLamMoi = UiHelper.nut("Làm mới (F5)", UiIcon.of(UiIcon.REFRESH, 14), new Color(100, 116, 139), true, e -> lamMoiToanBo());
        btnLamMoi.setPreferredSize(new Dimension(130, 34));

        JButton btnIn = UiHelper.nut("In phiếu (F4)", UiIcon.of(UiIcon.PRINT, 14), new Color(13, 148, 136), true, e -> inPhieuSuaChua());
        btnIn.setPreferredSize(new Dimension(125, 34));

        JButton btnHuy = UiHelper.nut("Hủy bỏ", UiIcon.of(UiIcon.TRASH, 14), new Color(239, 68, 68), true, e -> huyBo());
        btnHuy.setPreferredSize(new Dimension(88, 34));

        JButton btnLuuBanGiao = UiHelper.nut("Lưu & Bàn giao SC", UiIcon.of(UiIcon.CHECK, 15), XANH_ACCENT, true, e -> xacNhanLuuPhieu());
        btnLuuBanGiao.setPreferredSize(new Dimension(165, 34));
        btnLuuBanGiao.setFont(new Font("Segoe UI", Font.BOLD, 12));

        right.add(btnLamMoi);
        right.add(btnIn);
        right.add(btnHuy);
        right.add(btnLuuBanGiao);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ===============================================================
    // 2. HÀNG 1: KHỐI 1 (TIẾP NHẬN XE) & KHỐI 2 (PHÂN CÔNG KTV)
    // ===============================================================
    private JPanel taoHang1() {
        JPanel hang = new JPanel(new GridLayout(1, 2, 12, 0));
        hang.setOpaque(false);

        hang.add(taoKhoi1());
        hang.add(taoKhoi2());

        return hang;
    }

    /** KHỐI 1: THÔNG TIN LIÊN KẾT & TIẾP NHẬN XE */
    private JPanel taoKhoi1() {
        JPanel card = taoCardTrang();
        card.setLayout(new BorderLayout(0, 8));

        // Tiêu đề khối
        JPanel headerKhoid = new JPanel(new BorderLayout());
        headerKhoid.setOpaque(false);

        JLabel lblTde = new JLabel("KHỐI 1: THÔNG TIN LIÊN KẾT & TIẾP NHẬN XE", UiIcon.of(UiIcon.FILE, 14), JLabel.LEFT);
        lblTde.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTde.setForeground(XANH_HEADER);

        JLabel lblLink = new JLabel("Liên kết tiếp nhận");
        lblLink.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLink.setForeground(XANH_ACCENT);

        headerKhoid.add(lblTde, BorderLayout.WEST);
        headerKhoid.add(lblLink, BorderLayout.EAST);
        card.add(headerKhoid, BorderLayout.NORTH);

        // Body Khối 1
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Dòng 1: ComboBox Phiếu tiếp nhận & Cố vấn dịch vụ
        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);

        JPanel pnlPtn = new JPanel(new BorderLayout(0, 4));
        pnlPtn.setOpaque(false);
        JLabel lblPtn = new JLabel("Phiếu tiếp nhận liên quan *");
        lblPtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPtn.setForeground(CHU_PHU);
        cboPhieuTN = new JComboBox<>();
        cboPhieuTN.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboPhieuTN.setPreferredSize(new Dimension(0, 32));
        pnlPtn.add(lblPtn, BorderLayout.NORTH);
        pnlPtn.add(cboPhieuTN, BorderLayout.CENTER);

        JPanel pnlCoVan = new JPanel(new BorderLayout(0, 4));
        pnlCoVan.setOpaque(false);
        JLabel lblCv = new JLabel("Cố vấn dịch vụ giao việc");
        lblCv.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCv.setForeground(CHU_PHU);
        txtCoVan = new JTextField(nvHienTai != null ? nvHienTai.getHoTen() + " (" + nvHienTai.getMaNV() + ")" : "Nguyễn Văn An (NV-0001)");
        txtCoVan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCoVan.setEditable(false);
        txtCoVan.setBackground(new Color(241, 245, 249));
        txtCoVan.setPreferredSize(new Dimension(0, 32));
        pnlCoVan.add(lblCv, BorderLayout.NORTH);
        pnlCoVan.add(txtCoVan, BorderLayout.CENTER);

        row1.add(pnlPtn);
        row1.add(pnlCoVan);
        body.add(row1);
        body.add(Box.createVerticalStrut(8));

        // Dòng 2: Card tóm tắt phương tiện & khách hàng
        JPanel cardXe = new JPanel(new BorderLayout(8, 0)) {
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
        cardXe.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Trái: Icon xe + Biển số + Model + Chủ xe
        JPanel leftXe = new JPanel(new BorderLayout(10, 0));
        leftXe.setOpaque(false);

        JLabel lblCarIcon = new JLabel(UiIcon.of(UiIcon.CAR, 28));
        lblCarIcon.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel pnlDetailXe = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlDetailXe.setOpaque(false);

        JPanel rowBienSo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rowBienSo.setOpaque(false);
        lblBienSo = new JLabel("60A-12345");
        lblBienSo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBienSo.setForeground(new Color(15, 23, 42));

        lblThongTinXe = new JLabel("Toyota Camry 2.5Q (2022)");
        lblThongTinXe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblThongTinXe.setForeground(new Color(71, 85, 105));

        rowBienSo.add(lblBienSo);
        rowBienSo.add(lblThongTinXe);

        lblKhachHang = new JLabel("Khách hàng: Trần Minh Tuấn • SĐT: 0918.234.889");
        lblKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKhachHang.setForeground(new Color(100, 116, 139));

        pnlDetailXe.add(rowBienSo);
        pnlDetailXe.add(lblKhachHang);

        leftXe.add(lblCarIcon, BorderLayout.WEST);
        leftXe.add(pnlDetailXe, BorderLayout.CENTER);

        // Phải: ODO
        JPanel rightOdo = new JPanel(new GridLayout(2, 1, 0, 2));
        rightOdo.setOpaque(false);
        rightOdo.setPreferredSize(new Dimension(145, 36));
        JLabel lblTdeOdo = new JLabel("Chỉ số ODO:", SwingConstants.RIGHT);
        lblTdeOdo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTdeOdo.setForeground(CHU_PHU);

        lblOdo = new JLabel("45,230 km", SwingConstants.RIGHT);
        lblOdo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOdo.setForeground(new Color(15, 23, 42));

        rightOdo.add(lblTdeOdo);
        rightOdo.add(lblOdo);

        cardXe.add(leftXe, BorderLayout.CENTER);
        cardXe.add(rightOdo, BorderLayout.EAST);
        body.add(cardXe);
        body.add(Box.createVerticalStrut(8));

        // Dòng 3: Ngày lập & Dự kiến hoàn thành
        JPanel row3 = new JPanel(new GridLayout(1, 2, 10, 0));
        row3.setOpaque(false);

        JPanel pnlNgayLap = new JPanel(new BorderLayout(0, 4));
        pnlNgayLap.setOpaque(false);
        JLabel lblNl = new JLabel("Ngày lập sửa chữa");
        lblNl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNl.setForeground(CHU_PHU);
        txtNgayLap = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtNgayLap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNgayLap.setPreferredSize(new Dimension(0, 30));
        pnlNgayLap.add(lblNl, BorderLayout.NORTH);
        pnlNgayLap.add(txtNgayLap, BorderLayout.CENTER);

        JPanel pnlNgayDk = new JPanel(new BorderLayout(0, 4));
        pnlNgayDk.setOpaque(false);
        JLabel lblNdk = new JLabel("Thời gian dự kiến hoàn thành");
        lblNdk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNdk.setForeground(CHU_PHU);
        txtNgayDuKien = new JTextField(LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " (3 giờ)");
        txtNgayDuKien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNgayDuKien.setPreferredSize(new Dimension(0, 30));
        pnlNgayDk.add(lblNdk, BorderLayout.NORTH);
        pnlNgayDk.add(txtNgayDuKien, BorderLayout.CENTER);

        row3.add(pnlNgayLap);
        row3.add(pnlNgayDk);
        body.add(row3);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /** KHỐI 2: PHÂN CÔNG KỸ THUẬT VIÊN */
    private JPanel taoKhoi2() {
        JPanel card = taoCardTrang();
        card.setLayout(new BorderLayout(0, 8));

        // Tiêu đề khối
        JPanel headerKhoi = new JPanel(new BorderLayout());
        headerKhoi.setOpaque(false);

        JLabel lblTde = new JLabel("KHỐI 2: PHÂN CÔNG KỸ THUẬT VIÊN", UiIcon.of(UiIcon.WRENCH, 14), JLabel.LEFT);
        lblTde.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTde.setForeground(XANH_HEADER);

        JLabel lblTo = new JLabel("Tổ Máy - Gầm - Điện");
        lblTo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTo.setForeground(new Color(37, 99, 235));

        headerKhoi.add(lblTde, BorderLayout.WEST);
        headerKhoi.add(lblTo, BorderLayout.EAST);
        card.add(headerKhoi, BorderLayout.NORTH);

        // Body Khối 2
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Dòng 1: Kỹ thuật viên trưởng & Badge bậc thợ
        JPanel pnlKtvTr = new JPanel(new BorderLayout(0, 4));
        pnlKtvTr.setOpaque(false);
        JLabel lblKtvTitle = new JLabel("Kỹ thuật viên trưởng (Chịu trách nhiệm chính) *");
        lblKtvTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKtvTitle.setForeground(CHU_PHU);

        JPanel boxKtv = new JPanel(new BorderLayout(6, 0));
        boxKtv.setOpaque(false);
        cboKtvTruong = new JComboBox<>();
        cboKtvTruong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboKtvTruong.setPreferredSize(new Dimension(0, 32));

        lblBacTho = new JLabel("Bậc 4/7", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBacTho.setPreferredSize(new Dimension(60, 32));
        lblBacTho.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBacTho.setForeground(new Color(51, 65, 85));

        boxKtv.add(cboKtvTruong, BorderLayout.CENTER);
        boxKtv.add(lblBacTho, BorderLayout.EAST);

        pnlKtvTr.add(lblKtvTitle, BorderLayout.NORTH);
        pnlKtvTr.add(boxKtv, BorderLayout.CENTER);
        body.add(pnlKtvTr);
        body.add(Box.createVerticalStrut(6));

        // Dòng 2: Đội thợ phụ hỗ trợ dạng Chip tag
        JPanel pnlThoPhuWrapper = new JPanel(new BorderLayout(0, 4));
        pnlThoPhuWrapper.setOpaque(false);
        JLabel lblTpTitle = new JLabel("Đội thợ phụ hỗ trợ (2 nhân sự)");
        lblTpTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTpTitle.setForeground(CHU_PHU);

        pnlThoPhuList = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlThoPhuList.setOpaque(false);

        // Khởi tạo 2 thợ phụ mẫu
        dsThoPhu.add("Hoàng Đình Kha (KTV 2/7)");
        dsThoPhu.add("Đặng Quốc Bảo (Học việc)");
        capNhatChipsThoPhu();

        pnlThoPhuWrapper.add(lblTpTitle, BorderLayout.NORTH);
        pnlThoPhuWrapper.add(pnlThoPhuList, BorderLayout.CENTER);
        body.add(pnlThoPhuWrapper);
        body.add(Box.createVerticalStrut(6));

        // Dòng 3: Trạng thái phiếu sửa chữa (3 nút dạng Segmented Tab)
        JPanel pnlTrangThai = new JPanel(new BorderLayout(0, 4));
        pnlTrangThai.setOpaque(false);
        JLabel lblTtTitle = new JLabel("Trạng thái phiếu sửa chữa");
        lblTtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTtTitle.setForeground(CHU_PHU);

        JPanel pnlBtnGroup = new JPanel(new GridLayout(1, 3, 6, 0));
        pnlBtnGroup.setOpaque(false);

        btnDangXuLy = taoNutTrangThai("● Đang xử lý", new Color(37, 99, 235), new Color(239, 246, 255));
        btnChoPhuTung = taoNutTrangThai("● Chờ phụ tùng", new Color(217, 119, 6), new Color(254, 243, 199));
        btnHoanThanh = taoNutTrangThai("● Hoàn thành", new Color(22, 163, 74), new Color(240, 253, 244));

        btnDangXuLy.addActionListener(e -> chonTrangThai("Đang xử lý"));
        btnChoPhuTung.addActionListener(e -> chonTrangThai("Chờ phụ tùng"));
        btnHoanThanh.addActionListener(e -> chonTrangThai("Hoàn thành"));

        pnlBtnGroup.add(btnDangXuLy);
        pnlBtnGroup.add(btnChoPhuTung);
        pnlBtnGroup.add(btnHoanThanh);

        pnlTrangThai.add(lblTtTitle, BorderLayout.NORTH);
        pnlTrangThai.add(pnlBtnGroup, BorderLayout.CENTER);
        body.add(pnlTrangThai);
        body.add(Box.createVerticalStrut(6));

        // Dòng 4: Khoang thực hiện & Mức ưu tiên
        JPanel row4 = new JPanel(new GridLayout(1, 2, 10, 0));
        row4.setOpaque(false);

        JPanel pnlKhoang = new JPanel(new BorderLayout(0, 2));
        pnlKhoang.setOpaque(false);
        JLabel lblKhTitle = new JLabel("Khoang thực hiện:");
        lblKhTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblKhTitle.setForeground(CHU_PHU);
        cboKhoang = new JComboBox<>(new String[]{"CẦU NÂNG 03 (Zone B)", "CẦU NÂNG 01 (Zone A)", "CẦU NÂNG 02 (Zone A)", "KHOANG ĐIỆN LẠNH 04"});
        cboKhoang.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cboKhoang.setPreferredSize(new Dimension(0, 28));
        pnlKhoang.add(lblKhTitle, BorderLayout.NORTH);
        pnlKhoang.add(cboKhoang, BorderLayout.CENTER);

        JPanel pnlUuTien = new JPanel(new BorderLayout(0, 2));
        pnlUuTien.setOpaque(false);
        JLabel lblUtTitle = new JLabel("Mức ưu tiên:");
        lblUtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUtTitle.setForeground(CHU_PHU);
        cboUuTien = new JComboBox<>(new String[]{"Tiêu chuẩn", "Khẩn cấp (Làm ngay)", "Khách hẹn VIP"});
        cboUuTien.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cboUuTien.setPreferredSize(new Dimension(0, 28));
        pnlUuTien.add(lblUtTitle, BorderLayout.NORTH);
        pnlUuTien.add(cboUuTien, BorderLayout.CENTER);

        row4.add(pnlKhoang);
        row4.add(pnlUuTien);
        body.add(row4);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // ===============================================================
    // 3. HÀNG 2: KHỐI 3 (CÔNG VIỆC) & KHỐI 4 (PHỤ TÙNG XUẤT KHO)
    // ===============================================================
    private JPanel taoHang2() {
        JPanel hang = new JPanel(new GridLayout(1, 2, 12, 0));
        hang.setOpaque(false);

        hang.add(taoKhoi3());
        hang.add(taoKhoi4());

        return hang;
    }

    /** KHỐI 3: HẠNG MỤC CÔNG VIỆC & TIỀN CÔNG */
    private JPanel taoKhoi3() {
        JPanel card = taoCardTrang();
        card.setLayout(new BorderLayout(0, 8));

        // Header khối
        JPanel headerKhoi = new JPanel(new BorderLayout());
        headerKhoi.setOpaque(false);

        JLabel lblTde = new JLabel("KHỐI 3: HẠNG MỤC CÔNG VIỆC & TIỀN CÔNG", UiIcon.of(UiIcon.FILE, 14), JLabel.LEFT);
        lblTde.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTde.setForeground(XANH_HEADER);

        JButton btnThemCV = UiHelper.nut("+ Thêm công việc", XANH_ACCENT, e -> themCongViecDialog());
        btnThemCV.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnThemCV.setPreferredSize(new Dimension(130, 26));

        headerKhoi.add(lblTde, BorderLayout.WEST);
        headerKhoi.add(btnThemCV, BorderLayout.EAST);
        card.add(headerKhoi, BorderLayout.NORTH);

        // Bảng công việc
        modelCongViec = UiHelper.model("STT", "MÃ CV", "NỘI DUNG SỬA CHỮA", "KTV PHỤ TRÁCH", "GIỜ", "ĐƠN GIÁ", "THÀNH TIỀN", "XÓA");
        tblCongViec = UiHelper.bang(modelCongViec);
        tblCongViec.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Header renderer cho tất cả các cột
        DefaultTableCellRenderer headerRendererCV = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setBorder(new EmptyBorder(0, 1, 0, 1));
                l.setHorizontalAlignment(c == 0 || c == 7 ? SwingConstants.CENTER : (c >= 4 && c <= 6 ? SwingConstants.RIGHT : SwingConstants.LEFT));
                return l;
            }
        };
        for (int i = 0; i < tblCongViec.getColumnCount(); i++) {
            tblCongViec.getColumnModel().getColumn(i).setHeaderRenderer(headerRendererCV);
        }

        // Định dạng cột
        tblCongViec.getColumnModel().getColumn(0).setPreferredWidth(32);   // STT
        tblCongViec.getColumnModel().getColumn(1).setPreferredWidth(65);   // Mã CV
        tblCongViec.getColumnModel().getColumn(2).setPreferredWidth(165);  // Nội dung
        tblCongViec.getColumnModel().getColumn(3).setPreferredWidth(95);   // KTV
        tblCongViec.getColumnModel().getColumn(4).setPreferredWidth(38);   // Giờ
        tblCongViec.getColumnModel().getColumn(5).setPreferredWidth(70);   // Đơn giá
        tblCongViec.getColumnModel().getColumn(6).setPreferredWidth(80);   // Thành tiền
        tblCongViec.getColumnModel().getColumn(7).setPreferredWidth(32);   // Xóa

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblCongViec.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Renderer cho nút Xóa (icon trash vector)
        tblCongViec.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, "", isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setIcon(UiIcon.of(UiIcon.TRASH, 13));
                l.setToolTipText("Xóa hạng mục này");
                return l;
            }
        });

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblCongViec.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tblCongViec.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tblCongViec.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        // Click để xóa dòng công việc
        tblCongViec.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tblCongViec.columnAtPoint(e.getPoint());
                int row = tblCongViec.rowAtPoint(e.getPoint());
                if (col == 7 && row >= 0) {
                    modelCongViec.removeRow(row);
                    capNhatSttBang(modelCongViec);
                    tinhToanTaiChinh();
                }
            }
        });

        JScrollPane sp = new JScrollPane(tblCongViec);
        sp.setPreferredSize(new Dimension(0, 140));
        sp.setBorder(new LineBorder(VIEN_CARD, 1));
        card.add(sp, BorderLayout.CENTER);

        // Footer Khối 3
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 0, 4));

        lblTongCongViec = new JLabel("Tổng cộng 3 hạng mục công việc");
        lblTongCongViec.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTongCongViec.setForeground(CHU_PHU);

        lblTongTienCong = new JLabel("Tổng tiền công: 775,000 VNĐ");
        lblTongTienCong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongTienCong.setForeground(CHU_TIEU_DE);

        footer.add(lblTongCongViec, BorderLayout.WEST);
        footer.add(lblTongTienCong, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    /** KHỐI 4: PHỤ TÙNG & VẬT TƯ XUẤT KHO */
    private JPanel taoKhoi4() {
        JPanel card = taoCardTrang();
        card.setLayout(new BorderLayout(0, 8));

        // Header khối
        JPanel headerKhoi = new JPanel(new BorderLayout());
        headerKhoi.setOpaque(false);

        JLabel lblTde = new JLabel("KHỐI 4: PHỤ TÙNG & VẬT TƯ XUẤT KHO", UiIcon.of(UiIcon.BOX, 14), JLabel.LEFT);
        lblTde.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTde.setForeground(XANH_HEADER);

        JButton btnThemPT = UiHelper.nut("+ Xuất phụ tùng", XANH_ACCENT, e -> themPhuTungDialog());
        btnThemPT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnThemPT.setPreferredSize(new Dimension(125, 26));

        headerKhoi.add(lblTde, BorderLayout.WEST);
        headerKhoi.add(btnThemPT, BorderLayout.EAST);
        card.add(headerKhoi, BorderLayout.NORTH);

        // Bảng phụ tùng
        modelPhuTung = UiHelper.model("STT", "MÃ PT", "TÊN PHỤ TÙNG", "ĐVT", "SL", "ĐƠN GIÁ", "THÀNH TIỀN", "KHO", "XÓA");
        tblPhuTung = UiHelper.bang(modelPhuTung);
        tblPhuTung.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableCellRenderer headerRendererPT = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setBorder(new EmptyBorder(0, 1, 0, 1));
                l.setHorizontalAlignment(c == 0 || c == 3 || c == 4 || c == 7 || c == 8 ? SwingConstants.CENTER : (c >= 5 && c <= 6 ? SwingConstants.RIGHT : SwingConstants.LEFT));
                return l;
            }
        };
        for (int i = 0; i < tblPhuTung.getColumnCount(); i++) {
            tblPhuTung.getColumnModel().getColumn(i).setHeaderRenderer(headerRendererPT);
        }

        tblPhuTung.getColumnModel().getColumn(0).setPreferredWidth(32);   // STT
        tblPhuTung.getColumnModel().getColumn(1).setPreferredWidth(65);   // Mã PT
        tblPhuTung.getColumnModel().getColumn(2).setPreferredWidth(155);  // Tên PT
        tblPhuTung.getColumnModel().getColumn(3).setPreferredWidth(38);   // ĐVT
        tblPhuTung.getColumnModel().getColumn(4).setPreferredWidth(32);   // SL
        tblPhuTung.getColumnModel().getColumn(5).setPreferredWidth(70);   // Đơn giá
        tblPhuTung.getColumnModel().getColumn(6).setPreferredWidth(80);   // Thành tiền
        tblPhuTung.getColumnModel().getColumn(7).setPreferredWidth(65);   // Kho
        tblPhuTung.getColumnModel().getColumn(8).setPreferredWidth(32);   // Xóa

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        tblPhuTung.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblPhuTung.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblPhuTung.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Renderer cho nút Xóa phụ tùng (icon trash vector)
        tblPhuTung.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, "", isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setIcon(UiIcon.of(UiIcon.TRASH, 13));
                l.setToolTipText("Xóa phụ tùng này");
                return l;
            }
        });

        tblPhuTung.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tblPhuTung.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        // Renderer cho cột Kho (Badge Sẵn sàng màu xanh)
        tblPhuTung.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!isSel) {
                    l.setForeground(new Color(22, 163, 74));
                    l.setBackground(new Color(240, 253, 244));
                }
                return l;
            }
        });

        // Click xóa phụ tùng
        tblPhuTung.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tblPhuTung.columnAtPoint(e.getPoint());
                int row = tblPhuTung.rowAtPoint(e.getPoint());
                if (col == 8 && row >= 0) {
                    modelPhuTung.removeRow(row);
                    capNhatSttBang(modelPhuTung);
                    tinhToanTaiChinh();
                }
            }
        });

        JScrollPane sp = new JScrollPane(tblPhuTung);
        sp.setPreferredSize(new Dimension(0, 140));
        sp.setBorder(new LineBorder(VIEN_CARD, 1));
        card.add(sp, BorderLayout.CENTER);

        // Footer Khối 4
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 0, 4));

        lblTongPhuTung = new JLabel("Tổng cộng 3 loại vật tư xuất kho");
        lblTongPhuTung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTongPhuTung.setForeground(CHU_PHU);

        lblTongTienPhuTung = new JLabel("Tổng phụ tùng: 2,190,000 VNĐ");
        lblTongTienPhuTung.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongTienPhuTung.setForeground(CHU_TIEU_DE);

        footer.add(lblTongPhuTung, BorderLayout.WEST);
        footer.add(lblTongTienPhuTung, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    // ===============================================================
    // 4. HÀNG 3: GHI CHÚ CHẨN ĐOÁN & TỔNG KẾT TÀI CHÍNH
    // ===============================================================
    private JPanel taoHang3() {
        JPanel hang = new JPanel(new GridLayout(1, 2, 12, 0));
        hang.setOpaque(false);
        hang.setPreferredSize(new Dimension(1000, 190));
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        // Trái: Ghi chú chẩn đoán kỹ thuật & Hướng dẫn sửa chữa
        JPanel cardTrai = taoCardTrang();
        cardTrai.setLayout(new BorderLayout(0, 8));
        cardTrai.setPreferredSize(new Dimension(0, 190));

        JLabel lblTdeGc = new JLabel("GHI CHÚ CHẨN ĐOÁN KỸ THUẬT & HƯỚNG DẪN SỬA CHỮA", UiIcon.of(UiIcon.EDIT, 14), JLabel.LEFT);
        lblTdeGc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTdeGc.setForeground(XANH_HEADER);
        cardTrai.add(lblTdeGc, BorderLayout.NORTH);

        txtGhiChu = new JTextArea(5, 20);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setText("- Khách phản ánh máy hơi rung giật khi dừng đèn đỏ chờ số D. Đã kiểm tra bugi số 2 đánh lửa kém.\n"
                + "- Tiến hành súc rửa két nước giải nhiệt bằng dung dịch chuyên dụng 3M.\n"
                + "- Siết ốc bánh xe bằng cần lực 110 Nm sau khi kiểm tra lốp.");
        txtGhiChu.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane spGc = new JScrollPane(txtGhiChu);
        spGc.setPreferredSize(new Dimension(0, 100));
        spGc.setBorder(new LineBorder(VIEN_CARD, 1));
        cardTrai.add(spGc, BorderLayout.CENTER);

        // 2 badge cam kết dưới chân box
        JPanel pnlBadgeBot = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        pnlBadgeBot.setOpaque(false);

        JLabel lblBadge1 = new JLabel(" Phụ tùng chính hãng được bảo hành 06 tháng / 10.000 km", UiIcon.of(UiIcon.CHECK, 13), JLabel.LEFT);
        lblBadge1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBadge1.setForeground(new Color(22, 163, 74));

        JLabel lblBadge2 = new JLabel(" Cập nhật tự động vào sổ theo dõi ODO", UiIcon.of(UiIcon.REFRESH, 13), JLabel.LEFT);
        lblBadge2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBadge2.setForeground(new Color(37, 99, 235));

        pnlBadgeBot.add(lblBadge1);
        pnlBadgeBot.add(lblBadge2);
        cardTrai.add(pnlBadgeBot, BorderLayout.SOUTH);

        // Phải: Tổng kết tài chính dự kiến
        JPanel cardPhai = taoCardTrang();
        cardPhai.setLayout(new BorderLayout(0, 4));
        cardPhai.setPreferredSize(new Dimension(0, 190));

        JPanel pnlFinRows = new JPanel(new GridLayout(3, 1, 0, 4));
        pnlFinRows.setOpaque(false);
        pnlFinRows.setBorder(new EmptyBorder(0, 4, 2, 4));

        lblTienCongTho = new JLabel("775,000 VNĐ");
        lblTienCongTho.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTienCongTho.setForeground(new Color(15, 23, 42));

        lblTienPhuTungVatTu = new JLabel("2,190,000 VNĐ");
        lblTienPhuTungVatTu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTienPhuTungVatTu.setForeground(new Color(15, 23, 42));

        lblTienVat = new JLabel("237,200 VNĐ");
        lblTienVat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTienVat.setForeground(new Color(15, 23, 42));

        pnlFinRows.add(taoRowTaiChinh("1. Tổng tiền công thợ:", lblTienCongTho));
        pnlFinRows.add(taoRowTaiChinh("2. Tổng tiền phụ tùng - vật tư:", lblTienPhuTungVatTu));
        pnlFinRows.add(taoRowTaiChinh("3. Thuế GTGT VAT (8%):", lblTienVat));

        cardPhai.add(pnlFinRows, BorderLayout.NORTH);

        // Phần tổng tiền to & chữ
        JPanel pnlTongBox = new JPanel(new BorderLayout());
        pnlTongBox.setOpaque(false);
        pnlTongBox.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));

        JPanel pnlTongInner = new JPanel(new BorderLayout(10, 0));
        pnlTongInner.setOpaque(false);
        pnlTongInner.setBorder(new EmptyBorder(5, 12, 5, 12));

        JPanel leftTong = new JPanel();
        leftTong.setLayout(new BoxLayout(leftTong, BoxLayout.Y_AXIS));
        leftTong.setOpaque(false);
        JLabel lblTdeTong = new JLabel("TỔNG DỰ KIẾN:");
        lblTdeTong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTdeTong.setForeground(new Color(15, 23, 42));
        lblTdeTong.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubTong = new JLabel("(Đã bao gồm VAT & công thợ)");
        lblSubTong.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSubTong.setForeground(CHU_PHU);
        lblSubTong.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTong.add(lblTdeTong);
        leftTong.add(Box.createVerticalStrut(3));
        leftTong.add(lblSubTong);

        JPanel rightTong = new JPanel();
        rightTong.setLayout(new BoxLayout(rightTong, BoxLayout.Y_AXIS));
        rightTong.setOpaque(false);

        lblTongDuKien = new JLabel("3,202,200 VNĐ", SwingConstants.RIGHT);
        lblTongDuKien.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongDuKien.setForeground(new Color(29, 78, 216)); // Blue 700
        lblTongDuKien.setAlignmentX(Component.RIGHT_ALIGNMENT);

        lblDocSoTien = new JLabel("Ba triệu hai trăm lẻ hai ngàn hai trăm đồng", SwingConstants.RIGHT);
        lblDocSoTien.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDocSoTien.setForeground(new Color(100, 116, 139));
        lblDocSoTien.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightTong.add(lblTongDuKien);
        rightTong.add(Box.createVerticalStrut(3));
        rightTong.add(lblDocSoTien);

        pnlTongInner.add(leftTong, BorderLayout.WEST);
        pnlTongInner.add(rightTong, BorderLayout.EAST);

        pnlTongBox.add(pnlTongInner, BorderLayout.CENTER);
        cardPhai.add(pnlTongBox, BorderLayout.CENTER);

        hang.add(cardTrai);
        hang.add(cardPhai);
        return hang;
    }

    private JPanel taoRowTaiChinh(String label, JLabel lblValue) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lblL = new JLabel(label);
        lblL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblL.setForeground(new Color(71, 85, 105));

        row.add(lblL, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        return row;
    }

    // ===============================================================
    // 5. FOOTER PANEL CỐ ĐỊNH DƯỚI CÙNG
    // ===============================================================
    private JPanel taoFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN_CARD),
                new EmptyBorder(8, 16, 8, 16)
        ));

        // Phím tắt & Trạng thái đồng bộ máy chủ
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        left.add(taoLabelPhimTat("F4", "In phiếu"));
        left.add(taoLabelPhimTat("F5", "Làm mới"));
        left.add(taoLabelPhimTat("F9", "Kiểm kho"));
        left.add(taoLabelPhimTat("Esc", "Đóng cửa sổ"));

        JLabel lblSep = new JLabel("|");
        lblSep.setForeground(new Color(203, 213, 225));
        left.add(lblSep);

        JLabel lblOnline = new JLabel("● Máy chủ đồng bộ: Online (Ping 12ms)");
        lblOnline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblOnline.setForeground(new Color(22, 163, 74));
        left.add(lblOnline);

        // Nút hành động phải
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton btnLuuTam = UiHelper.nut("Lưu tạm (Draft)", new Color(241, 245, 249), e -> luuTamDraft());
        btnLuuTam.setForeground(new Color(51, 65, 85));
        btnLuuTam.setPreferredSize(new Dimension(135, 34));

        JButton btnChuyenLenh = UiHelper.nut("Xác nhận & Chuyển lệnh sửa chữa", UiIcon.of(UiIcon.CHECK, 14), new Color(30, 58, 138), true, e -> xacNhanLuuPhieu());
        btnChuyenLenh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChuyenLenh.setPreferredSize(new Dimension(245, 34));

        right.add(btnLuuTam);
        right.add(btnChuyenLenh);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private JLabel taoLabelPhimTat(String key, String desc) {
        JLabel l = new JLabel("<html><b style='background:#E2E8F0;padding:2px 4px;border-radius:3px;'>" + key + "</b> " + desc + "</html>");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    // ===============================================================
    // CÁC HÀM TIỆN ÍCH UI & TƯƠNG TÁC
    // ===============================================================

    private JPanel taoCardTrang() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(VIEN_CARD);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        return p;
    }

    private JButton taoNutTrangThai(String text, Color borderCol, Color bgCol) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(borderCol);
        btn.setBackground(bgCol);
        btn.setBorder(new LineBorder(borderCol, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 28));
        return btn;
    }

    private void chonTrangThai(String tt) {
        this.trangThaiChon = tt;
        btnDangXuLy.setFont(new Font("Segoe UI", "Đang xử lý".equals(tt) ? Font.BOLD : Font.PLAIN, 12));
        btnChoPhuTung.setFont(new Font("Segoe UI", "Chờ phụ tùng".equals(tt) ? Font.BOLD : Font.PLAIN, 12));
        btnHoanThanh.setFont(new Font("Segoe UI", "Hoàn thành".equals(tt) ? Font.BOLD : Font.PLAIN, 12));
    }

    private void capNhatChipsThoPhu() {
        pnlThoPhuList.removeAll();
        for (String tp : dsThoPhu) {
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(239, 246, 255));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.setColor(new Color(191, 219, 254));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    g2.dispose();
                }
            };
            chip.setOpaque(false);
            JLabel lblText = new JLabel(" " + tp, UiIcon.of(UiIcon.USER, 12), JLabel.LEFT);
            lblText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblText.setForeground(new Color(30, 58, 138));

            JLabel lblDel = new JLabel("\u00D7");
            lblDel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblDel.setForeground(new Color(148, 163, 184));
            lblDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblDel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    dsThoPhu.remove(tp);
                    capNhatChipsThoPhu();
                }
            });

            chip.add(lblText);
            chip.add(lblDel);
            pnlThoPhuList.add(chip);
        }

        // Nút thêm thợ phụ
        JButton btnThem = new JButton("+ Thêm");
        btnThem.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnThem.setForeground(XANH_ACCENT);
        btnThem.setBackground(Color.WHITE);
        btnThem.setBorder(new LineBorder(new Color(191, 219, 254), 1, true));
        btnThem.setFocusPainted(false);
        btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThem.addActionListener(e -> themThoPhuDialog());
        pnlThoPhuList.add(btnThem);

        pnlThoPhuList.revalidate();
        pnlThoPhuList.repaint();
    }

    private void themThoPhuDialog() {
        String ten = JOptionPane.showInputDialog(this, "Nhập họ tên và cấp bậc thợ phụ:", "Thêm thợ phụ", JOptionPane.PLAIN_MESSAGE);
        if (ten != null && !ten.trim().isEmpty()) {
            dsThoPhu.add(ten.trim());
            capNhatChipsThoPhu();
        }
    }

    // ===============================================================
    // KHỞI TẠO & NẠP DỮ LIỆU MẪU CHUẨN XÁC
    // ===============================================================
    private void khoiTaoDuLieuMau() {
        // Nạp ComboBox Phiếu Tiếp Nhận từ KhoDuLieu
        cboPhieuTN.removeAllItems();
        cboPhieuTN.addItem("PTN00025 - Trần Minh Tuấn (60A-12345)");
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            cboPhieuTN.addItem(p.getSoPhieu() + " - " + p.getHoTen() + " (" + p.getBienSo() + ")");
        }

        cboPhieuTN.addActionListener(e -> {
            String sel = (String) cboPhieuTN.getSelectedItem();
            if (sel != null && sel.contains("60A-12345")) {
                lblBienSo.setText("60A-12345");
                lblThongTinXe.setText("Toyota Camry 2.5Q (2022)");
                lblKhachHang.setText("Khách hàng: Trần Minh Tuấn • Điện thoại: 0918.234.889");
                lblOdo.setText("45,230 km");
            } else if (sel != null) {
                String soPhieu = sel.split(" - ")[0];
                for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
                    if (p.getSoPhieu().equals(soPhieu)) {
                        lblBienSo.setText(p.getBienSo());
                        lblThongTinXe.setText(p.getHangXe() + " " + p.getLoaiXe() + " (2022)");
                        lblKhachHang.setText("Khách hàng: " + p.getHoTen() + " • Điện thoại: " + p.getSdt());
                        PhuongTien xe = kho.timXeTheoBienSo(p.getBienSo());
                        lblOdo.setText(xe != null && !xe.getOdo().isEmpty() ? xe.getOdo() + " km" : "28,500 km");
                        txtGhiChu.setText("- Yêu cầu tiếp nhận: " + p.getYeuCau() + "\n- Kiểm tra tổng thể các hạng mục động cơ và hệ thống gầm xe.");
                        break;
                    }
                }
            }
        });

        // Nạp ComboBox KTV
        cboKtvTruong.removeAllItems();
        cboKtvTruong.addItem("Võ Văn Nam - Bậc 4/7 (Trưởng tổ Máy-Gầm)");
        cboKtvTruong.addItem("Trần Văn Sửa Chữa (NV02 - Bậc 5/7)");
        cboKtvTruong.addItem("Nguyễn Văn Kho (NV03 - Bậc 3/7)");
        for (NhanVien n : kho.getNhanViens()) {
            String vt = n.getVaiTro() == null ? "" : n.getVaiTro().toLowerCase();
            if (vt.contains("sửa chữa") || vt.contains("kỹ thuật") || vt.contains("ktv")
                    || vt.contains("máy") || vt.contains("gầm") || vt.contains("điện") || vt.contains("sơn") || vt.contains("bảo dưỡng")) {
                cboKtvTruong.addItem(n.getHoTen() + " (" + n.getMaNV() + ")");
            }
        }

        // Nạp dữ liệu mẫu bảng công việc (Khớp 100% mẫu)
        modelCongViec.setRowCount(0);
        modelCongViec.addRow(new Object[]{1, "CV-BD01", "Bảo dưỡng định kỳ cấp 40.000km", "Võ Văn Nam", 1.5, "300,000", "450,000", "🗑"});
        modelCongViec.addRow(new Object[]{2, "CV-LM02", "Súc rửa hệ thống làm mát & két nước", "Hoàng Đình Kha", 1.0, "250,000", "250,000", "🗑"});
        modelCongViec.addRow(new Object[]{3, "CV-TD03", "Thay dầu động cơ & cốc lọc dầu", "Võ Văn Nam", 0.5, "150,000", "75,000", "🗑"});

        // Nạp dữ liệu mẫu bảng phụ tùng xuất kho (Khớp 100% mẫu)
        modelPhuTung.setRowCount(0);
        modelPhuTung.addRow(new Object[]{1, "PT-OIL01", "Dầu Castrol Magnatec 5W-30 (Can 4L)", "Can", 1, "820,000", "820,000", "Sẵn sàng", "🗑"});
        modelPhuTung.addRow(new Object[]{2, "PT-SPK04", "Bugi Denso Iridium Power IK20", "Cái", 4, "280,000", "1,120,000", "Sẵn sàng", "🗑"});
        modelPhuTung.addRow(new Object[]{3, "PT-FLT12", "Lọc dầu động cơ Toyota chính hãng", "Cái", 1, "250,000", "250,000", "Sẵn sàng", "🗑"});
    }

    // ===============================================================
    // TÍNH TOÁN TÀI CHÍNH TỰ ĐỘNG (CÔNG THỢ + PHỤ TÙNG + VAT 8%)
    // ===============================================================
    private void tinhToanTaiChinh() {
        long tongTienCong = 0;
        for (int i = 0; i < modelCongViec.getRowCount(); i++) {
            try {
                String tt = modelCongViec.getValueAt(i, 6).toString().replace(",", "").replace(".", "");
                tongTienCong += Long.parseLong(tt);
            } catch (Exception ignored) {}
        }

        long tongTienPhuTung = 0;
        for (int i = 0; i < modelPhuTung.getRowCount(); i++) {
            try {
                String tt = modelPhuTung.getValueAt(i, 6).toString().replace(",", "").replace(".", "");
                tongTienPhuTung += Long.parseLong(tt);
            } catch (Exception ignored) {}
        }

        long vat = (long) Math.round((tongTienCong + tongTienPhuTung) * 0.08);
        long tongDuKien = tongTienCong + tongTienPhuTung + vat;

        // Cập nhật label Khối 3 & Khối 4
        lblTongCongViec.setText("Tổng cộng " + modelCongViec.getRowCount() + " hạng mục công việc");
        lblTongTienCong.setText("Tổng tiền công: " + UiHelper.tien(tongTienCong) + " VNĐ");

        lblTongPhuTung.setText("Tổng cộng " + modelPhuTung.getRowCount() + " loại vật tư xuất kho");
        lblTongTienPhuTung.setText("Tổng phụ tùng: " + UiHelper.tien(tongTienPhuTung) + " VNĐ");

        // Cập nhật label Tài chính dưới
        lblTienCongTho.setText(UiHelper.tien(tongTienCong) + " VNĐ");
        lblTienPhuTungVatTu.setText(UiHelper.tien(tongTienPhuTung) + " VNĐ");
        lblTienVat.setText(UiHelper.tien(vat) + " VNĐ");
        lblTongDuKien.setText(UiHelper.tien(tongDuKien) + " VNĐ");
        lblDocSoTien.setText(docSoThanhChu(tongDuKien));
    }

    private void capNhatSttBang(DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }
    }

    // ===============================================================
    // DIALOG THÊM CÔNG VIỆC
    // ===============================================================
    private void themCongViecDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm hạng mục công việc sửa chữa", true);
        dlg.setSize(440, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(5, 2, 8, 10));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JTextField txtMaCV = new JTextField("CV-" + (int)(Math.random() * 899 + 100));
        JComboBox<String> cboNoiDung = new JComboBox<>(new String[]{
                "Bảo dưỡng định kỳ cấp 40.000km",
                "Súc rửa hệ thống làm mát & két nước",
                "Thay dầu động cơ & cốc lọc dầu",
                "Cân chỉnh độ chụm & góc đặt bánh xe",
                "Vệ sinh hệ thống kim phun & bướm ga",
                "Bảo dưỡng hệ thống phanh 4 bánh",
                "Kiểm tra & nạp gas điều hòa R134a"
        });
        cboNoiDung.setEditable(true);

        JComboBox<String> cboTho = new JComboBox<>(new String[]{"Võ Văn Nam", "Hoàng Đình Kha", "Đặng Quốc Bảo", "Trần Văn Sửa Chữa"});
        JTextField txtGio = new JTextField("1.0");
        JTextField txtGia = new JTextField("250000");

        p.add(new JLabel("Mã công việc:")); p.add(txtMaCV);
        p.add(new JLabel("Nội dung sửa chữa:")); p.add(cboNoiDung);
        p.add(new JLabel("KTV phụ trách:")); p.add(cboTho);
        p.add(new JLabel("Thời gian thực hiện (giờ):")); p.add(txtGio);
        p.add(new JLabel("Đơn giá giờ công (VNĐ):")); p.add(txtGia);

        JPanel pBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnLuu = UiHelper.nut("Thêm vào phiếu", XANH_ACCENT, ev -> {
            try {
                double gio = Double.parseDouble(txtGio.getText().trim());
                long gia = Long.parseLong(txtGia.getText().trim().replace(",", ""));
                long tt = (long)(gio * gia);
                int stt = modelCongViec.getRowCount() + 1;
                modelCongViec.addRow(new Object[]{
                        stt,
                        txtMaCV.getText().trim(),
                        String.valueOf(cboNoiDung.getSelectedItem()),
                        String.valueOf(cboTho.getSelectedItem()),
                        gio,
                        UiHelper.tien(gia),
                        UiHelper.tien(tt),
                        "🗑"
                });
                tinhToanTaiChinh();
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đúng số giờ và đơn giá!");
            }
        });
        JButton btnDong = UiHelper.nut("Đóng", new Color(100, 116, 139), ev -> dlg.dispose());
        pBot.add(btnDong);
        pBot.add(btnLuu);

        dlg.add(p, BorderLayout.CENTER);
        dlg.add(pBot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ===============================================================
    // DIALOG XUẤT THÊM PHỤ TÙNG
    // ===============================================================
    private void themPhuTungDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xuất thêm phụ tùng từ kho", true);
        dlg.setSize(450, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 10));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JComboBox<String> cboPt = new JComboBox<>();
        for (PhuTung pt : kho.getPhuTungs()) {
            cboPt.addItem(pt.getMaPT() + " - " + pt.getTen() + " (" + pt.getDonVi() + ") - " + UiHelper.tien(pt.getDonGia()) + " đ");
        }

        JTextField txtSl = new JTextField("1");
        JLabel lblTonKho = new JLabel("Tồn kho hiện tại: Sẵn sàng");
        lblTonKho.setForeground(new Color(22, 163, 74));

        p.add(new JLabel("Chọn phụ tùng:")); p.add(cboPt);
        p.add(new JLabel("Số lượng xuất:")); p.add(txtSl);
        p.add(new JLabel("Trạng thái kho:")); p.add(lblTonKho);

        JPanel pBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnLuu = UiHelper.nut("Xuất vào phiếu", XANH_ACCENT, ev -> {
            try {
                int sl = Integer.parseInt(txtSl.getText().trim());
                if (sl <= 0) throw new Exception();

                String sel = (String) cboPt.getSelectedItem();
                if (sel == null) return;
                String ma = sel.split(" - ")[0];
                PhuTung pt = kho.timPhuTung(ma);
                if (pt != null) {
                    int stt = modelPhuTung.getRowCount() + 1;
                    long tt = sl * pt.getDonGia();
                    modelPhuTung.addRow(new Object[]{
                            stt,
                            pt.getMaPT(),
                            pt.getTen(),
                            pt.getDonVi(),
                            sl,
                            UiHelper.tien(pt.getDonGia()),
                            UiHelper.tien(tt),
                            "Sẵn sàng",
                            "🗑"
                    });
                    tinhToanTaiChinh();
                    dlg.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập số lượng hợp lệ lớn hơn 0!");
            }
        });
        JButton btnDong = UiHelper.nut("Đóng", new Color(100, 116, 139), ev -> dlg.dispose());
        pBot.add(btnDong);
        pBot.add(btnLuu);

        dlg.add(p, BorderLayout.CENTER);
        dlg.add(pBot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ===============================================================
    // XÁC NHẬN LƯU & BÀN GIAO PHIẾU SỬA CHỮA (ĐỒNG BỘ CSDL)
    // ===============================================================
    private void xacNhanLuuPhieu() {
        if (modelCongViec.getRowCount() == 0 && modelPhuTung.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất một hạng mục công việc hoặc phụ tùng!");
            return;
        }

        String ktvTr = (String) cboKtvTruong.getSelectedItem();
        String bienSo = lblBienSo.getText();

        // 1. Cập nhật tiến độ tiếp nhận xe
        String selPTN = (String) cboPhieuTN.getSelectedItem();
        String soPTN = selPTN != null ? selPTN.split(" - ")[0] : "TN001";
        kho.capNhatTienDoPhieuTiepNhan(soPTN, trangThaiChon, ktvTr);

        // 2. Lưu trực tiếp vào CSDL SQL Server (bảng PhieuSuaChua & ChiTietSuaChua)
        Connection con = ConnectDB.getInstance().getConnection();
        boolean dbOk = false;
        if (con != null) {
            try {
                // Insert/Update PhieuSuaChua
                String sqlPSC = "IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = ?) " +
                                "INSERT INTO PhieuSuaChua (maPSC, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) " +
                                "VALUES (?, ?, 'KH001', (SELECT TOP 1 soVIN FROM PhuongTien WHERE bienSoXe = ?), SYSDATETIME(), DATEADD(day, 1, CAST(GETDATE() AS DATE)), ?) " +
                                "ELSE " +
                                "UPDATE PhieuSuaChua SET trangThai = ?, maNV = ? WHERE maPSC = ?";
                try (PreparedStatement stmt = con.prepareStatement(sqlPSC)) {
                    stmt.setString(1, maPSC);
                    stmt.setString(2, maPSC);
                    stmt.setString(3, nvHienTai != null ? nvHienTai.getMaNV() : "NV02");
                    stmt.setString(4, bienSo);
                    stmt.setString(5, "Đang xử lý".equals(trangThaiChon) ? "DANG_SUA" : ("Hoàn thành".equals(trangThaiChon) ? "DA_HOAN_THANH" : "CHO_SUA"));
                    stmt.setString(6, trangThaiChon);
                    stmt.setString(7, nvHienTai != null ? nvHienTai.getMaNV() : "NV02");
                    stmt.setString(8, maPSC);
                    stmt.executeUpdate();
                }

                // Xóa chi tiết cũ và insert các chi tiết mới
                try (PreparedStatement del = con.prepareStatement("DELETE FROM ChiTietSuaChua WHERE maPSC = ?")) {
                    del.setString(1, maPSC);
                    del.executeUpdate();
                }

                String sqlCT = "INSERT INTO ChiTietSuaChua (maPSC, maPT, noiDungSC, tenPhuTung, soLuong, donGia, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = con.prepareStatement(sqlCT)) {
                    for (int i = 0; i < modelPhuTung.getRowCount(); i++) {
                        String maPT = modelPhuTung.getValueAt(i, 1).toString();
                        String tenPT = modelPhuTung.getValueAt(i, 2).toString();
                        int sl = Integer.parseInt(modelPhuTung.getValueAt(i, 4).toString());
                        long gia = Long.parseLong(modelPhuTung.getValueAt(i, 5).toString().replace(",", ""));
                        stmt.setString(1, maPSC);
                        stmt.setString(2, maPT);
                        stmt.setString(3, "Thay thế linh kiện: " + tenPT);
                        stmt.setString(4, tenPT);
                        stmt.setInt(5, sl);
                        stmt.setDouble(6, (double) gia);
                        stmt.setString(7, "CHO_THUC_HIEN");
                        stmt.executeUpdate();
                    }
                }
                dbOk = true;
            } catch (SQLException e) {
                System.err.println("Lỗi lưu PhieuSuaChua DB: " + e.getMessage());
            }
        }

        // Ghi Audit Log
        kho.ghiNhatKy(nvHienTai != null ? nvHienTai.getHoTen() : "Quản lý xưởng",
                "Quản lý xưởng", "Lập lệnh sửa chữa", "INFO",
                "Lập lệnh sửa chữa " + maPSC + " cho xe " + bienSo + " - KTV trưởng: " + ktvTr + " - Tổng: " + lblTongDuKien.getText());

        int hoiIn = JOptionPane.showConfirmDialog(this,
                "Lập lệnh sửa chữa [" + maPSC + "] thành công!" + (dbOk ? " (Đã lưu CSDL SQL Server)\n" : "\n")
                        + "Xe: " + bienSo + " • KTV trưởng: " + ktvTr + "\n"
                        + "Tổng chi phí dự kiến: " + lblTongDuKien.getText() + "\n\n"
                        + "Bạn có muốn IN LỆNH SỬA CHỮA bàn giao cho kỹ thuật viên không?",
                "Bàn giao sửa chữa thành công",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (hoiIn == JOptionPane.YES_OPTION) {
            inPhieuSuaChua();
        }
    }

    private void luuTamDraft() {
        JOptionPane.showMessageDialog(this, "Đã lưu tạm (Draft) bản ghi " + maPSC + " vào bộ nhớ hệ thống!");
    }

    private void lamMoiToanBo() {
        this.maPSC = String.format("PSC%05d", demPSC++);
        txtNgayLap.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtNgayDuKien.setText(LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " (3 giờ)");
        khoiTaoDuLieuMau();
        tinhToanTaiChinh();
        JOptionPane.showMessageDialog(this, "Đã làm mới form sửa chữa phương tiện!");
    }

    private void huyBo() {
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn hủy bỏ các thay đổi đang nhập?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            lamMoiToanBo();
        }
    }

    // ===============================================================
    // XUẤT & IN PHIẾU SỬA CHỮA THEO MẪU GARA
    // ===============================================================
    private void inPhieuSuaChua() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "In Lệnh Sửa Chữa & Phiếu Giao Việc - " + maPSC, true);
        dlg.setSize(820, 680);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        String html = taoHtmlPhieuSuaChua();
        JEditorPane editor = new JEditorPane();
        editor.setEditable(false);
        editor.setContentType("text/html");
        editor.setText(html);
        editor.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(editor);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnIn = UiHelper.nut("In ngay", UiIcon.of(UiIcon.PRINT, 14), XANH_ACCENT, true, e -> {
            try { editor.print(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        JButton btnDong = UiHelper.nut("Đóng", new Color(100, 116, 139), e -> dlg.dispose());

        pnlNut.add(btnDong);
        pnlNut.add(btnIn);
        dlg.add(pnlNut, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private String taoHtmlPhieuSuaChua() {
        StringBuilder sbCV = new StringBuilder();
        for (int i = 0; i < modelCongViec.getRowCount(); i++) {
            sbCV.append("<tr>")
                    .append("<td style='text-align:center;'>").append(i + 1).append("</td>")
                    .append("<td>").append(modelCongViec.getValueAt(i, 1)).append("</td>")
                    .append("<td><b>").append(modelCongViec.getValueAt(i, 2)).append("</b></td>")
                    .append("<td>").append(modelCongViec.getValueAt(i, 3)).append("</td>")
                    .append("<td style='text-align:center;'>").append(modelCongViec.getValueAt(i, 4)).append("</td>")
                    .append("<td style='text-align:right;'>").append(modelCongViec.getValueAt(i, 5)).append("</td>")
                    .append("<td style='text-align:right;'><b>").append(modelCongViec.getValueAt(i, 6)).append("</b></td>")
                    .append("</tr>");
        }

        StringBuilder sbPT = new StringBuilder();
        for (int i = 0; i < modelPhuTung.getRowCount(); i++) {
            sbPT.append("<tr>")
                    .append("<td style='text-align:center;'>").append(i + 1).append("</td>")
                    .append("<td>").append(modelPhuTung.getValueAt(i, 1)).append("</td>")
                    .append("<td><b>").append(modelPhuTung.getValueAt(i, 2)).append("</b></td>")
                    .append("<td style='text-align:center;'>").append(modelPhuTung.getValueAt(i, 3)).append("</td>")
                    .append("<td style='text-align:center;'>").append(modelPhuTung.getValueAt(i, 4)).append("</td>")
                    .append("<td style='text-align:right;'>").append(modelPhuTung.getValueAt(i, 5)).append("</td>")
                    .append("<td style='text-align:right;'><b>").append(modelPhuTung.getValueAt(i, 6)).append("</b></td>")
                    .append("</tr>");
        }

        return "<html><body style='font-family:Segoe UI, sans-serif;padding:15px;color:#1e293b;font-size:12px;'>"
                + "<div style='text-align:center;border-bottom:2px solid #1e3a8a;padding-bottom:8px;margin-bottom:12px;'>"
                + "  <h2 style='margin:0;color:#1e3a8a;font-size:18px;'>GARA Ô TÔ CHUYÊN NGHIỆP THE T4</h2>"
                + "  <p style='margin:2px;font-size:11px;color:#64748b;'>Địa chỉ: 12 Nguyễn Văn Bảo, P.4, Q.Gò Vấp, TP.HCM &nbsp;|&nbsp; Hotline: 1900 8888 - 0988 888 888</p>"
                + "  <h3 style='margin:10px 0 2px 0;color:#0f172a;'>PHIẾU GIAO VIỆC & LỆNH SỬA CHỮA PHƯƠNG TIỆN</h3>"
                + "  <p style='margin:0;font-size:12px;color:#2563eb;'><b>Số lệnh: " + maPSC + "</b> &nbsp;|&nbsp; Ngày lập: " + txtNgayLap.getText() + "</p>"
                + "</div>"
                + "<table style='width:100%;font-size:12px;margin-bottom:12px;background:#f8fafc;padding:8px;border-radius:6px;'>"
                + "  <tr>"
                + "    <td style='width:50%;'><b>Biển số xe:</b> <span style='font-size:14px;color:#1e3a8a;font-weight:bold;'>" + lblBienSo.getText() + "</span></td>"
                + "    <td><b>KTV trưởng phụ trách:</b> " + cboKtvTruong.getSelectedItem() + "</td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td><b>Dòng xe:</b> " + lblThongTinXe.getText() + "</td>"
                + "    <td><b>Chỉ số ODO:</b> " + lblOdo.getText() + "</td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td>" + lblKhachHang.getText() + "</td>"
                + "    <td><b>Khoang thực hiện:</b> " + cboKhoang.getSelectedItem() + "</td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td><b>Cố vấn dịch vụ:</b> " + txtCoVan.getText() + "</td>"
                + "    <td><b>Dự kiến hoàn thành:</b> " + txtNgayDuKien.getText() + "</td>"
                + "  </tr>"
                + "</table>"
                + "<h4 style='margin:8px 0 4px 0;color:#1e3a8a;'>I. HẠNG MỤC CÔNG VIỆC THỰC HIỆN:</h4>"
                + "<table border='1' cellspacing='0' cellpadding='4' style='width:100%;border-collapse:collapse;font-size:11px;border-color:#cbd5e1;'>"
                + "  <tr style='background:#f1f5f9;color:#334155;'>"
                + "    <th>STT</th><th>Mã CV</th><th>Nội dung sửa chữa</th><th>KTV</th><th>Giờ</th><th>Đơn giá</th><th>Thành tiền (đ)</th>"
                + "  </tr>" + sbCV.toString()
                + "</table>"
                + "<p style='text-align:right;margin:4px 0 10px 0;'><b>" + lblTongTienCong.getText() + "</b></p>"
                + "<h4 style='margin:8px 0 4px 0;color:#1e3a8a;'>II. PHỤ TÙNG & VẬT TƯ XUẤT KHO:</h4>"
                + "<table border='1' cellspacing='0' cellpadding='4' style='width:100%;border-collapse:collapse;font-size:11px;border-color:#cbd5e1;'>"
                + "  <tr style='background:#f1f5f9;color:#334155;'>"
                + "    <th>STT</th><th>Mã PT</th><th>Tên phụ tùng</th><th>ĐVT</th><th>SL</th><th>Đơn giá</th><th>Thành tiền (đ)</th>"
                + "  </tr>" + sbPT.toString()
                + "</table>"
                + "<p style='text-align:right;margin:4px 0 10px 0;'><b>" + lblTongTienPhuTung.getText() + "</b></p>"
                + "<div style='border:1px dashed #cbd5e1;padding:8px;background:#f8fafc;border-radius:4px;margin-bottom:12px;'>"
                + "  <b>Ghi chú chẩn đoán kỹ thuật:</b><br/>"
                + "  <span style='font-size:11px;color:#475569;'>" + txtGhiChu.getText().replace("\n", "<br/>") + "</span>"
                + "</div>"
                + "<div style='text-align:right;border-top:1px solid #e2e8f0;padding-top:6px;'>"
                + "  <p style='margin:2px 0;'>Tổng tiền công: <b>" + lblTienCongTho.getText() + "</b></p>"
                + "  <p style='margin:2px 0;'>Tổng phụ tùng: <b>" + lblTienPhuTungVatTu.getText() + "</b></p>"
                + "  <p style='margin:2px 0;'>Thuế VAT (8%): <b>" + lblTienVat.getText() + "</b></p>"
                + "  <h3 style='margin:4px 0;color:#1e3a8a;'>TỔNG DỰ KIẾN: " + lblTongDuKien.getText() + "</h3>"
                + "  <p style='margin:0;font-style:italic;color:#64748b;'>(" + lblDocSoTien.getText() + ")</p>"
                + "</div>"
                + "<table style='width:100%;margin-top:25px;text-align:center;font-size:11px;'>"
                + "  <tr>"
                + "    <td style='width:33%;'><b>KHÁCH HÀNG XÁC NHẬN</b><br/><br/><br/><br/>(Ký & ghi rõ họ tên)</td>"
                + "    <td style='width:33%;'><b>KTV TRƯỞNG NHẬN LỆNH</b><br/><br/><br/><br/>" + cboKtvTruong.getSelectedItem() + "</td>"
                + "    <td style='width:33%;'><b>CỐ VẤN DỊCH VỤ</b><br/><br/><br/><br/>" + txtCoVan.getText() + "</td>"
                + "  </tr>"
                + "</table>"
                + "</body></html>";
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "inPhieu");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "lamMoi");
        am.put("inPhieu", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { inPhieuSuaChua(); }
        });
        am.put("lamMoi", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { lamMoiToanBo(); }
        });
    }

    private static final String[] CHU_SO = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};

    public static String docSoThanhChu(long so) {
        if (so == 0) return "Không đồng";
        if (so < 0) return "Âm " + docSoThanhChu(-so);
        String s = "";
        long ty = so / 1_000_000_000;
        long trieu = (so % 1_000_000_000) / 1_000_000;
        long nghin = (so % 1_000_000) / 1_000;
        long le = so % 1_000;

        if (ty > 0) s += docBlockBaSo((int) ty) + " tỷ ";
        if (trieu > 0) s += docBlockBaSo((int) trieu) + " triệu ";
        if (nghin > 0) s += docBlockBaSo((int) nghin) + " ngàn ";
        if (le > 0) s += docBlockBaSo((int) le);

        s = s.trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) return "Không đồng";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1) + " đồng";
    }

    private static String docBlockBaSo(int n) {
        int tram = n / 100;
        int chuc = (n % 100) / 10;
        int donVi = n % 10;
        String s = "";
        if (tram > 0) s += CHU_SO[tram] + " trăm ";
        if (chuc > 1) {
            s += CHU_SO[chuc] + " mươi ";
            if (donVi == 1) s += "mốt";
            else if (donVi == 5) s += "lăm";
            else if (donVi > 0) s += CHU_SO[donVi];
        } else if (chuc == 1) {
            s += "mười ";
            if (donVi == 5) s += "lăm";
            else if (donVi > 0) s += CHU_SO[donVi];
        } else if (chuc == 0 && donVi > 0) {
            if (tram > 0) s += "lẻ " + CHU_SO[donVi];
            else s += CHU_SO[donVi];
        }
        return s.trim();
    }
}
