package UI;

import ConnectDB.ConnectDB;
import Entity.KhoDuLieu;
import Entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * QuanLyNhanVien_UI — Phân hệ Quản Lý Danh Mục Nhân Viên & Hồ Sơ Nhân Sự Chuẩn ERP Gara Ô Tô:
 * - Tinh chỉnh sát 100% theo bản thiết kế mẫu:
 *   1. Header ERP: Breadcrumbs, Tiêu đề, Badge số lượng, Subtitle, 3 nút hành động (+ Thêm F2, Phân quyền, Tải lại F5)
 *   2. Thanh lọc: Ô tìm kiếm đa năng có icon kính lúp & placeholder, Lọc theo Bộ phận, Lọc theo Trạng thái, Nút Đặt lại
 *   3. Bảng hồ sơ chuẩn 11 cột: STT (01, 02..), Mã NV, Họ và tên (Initials Avatar), Chức vụ / Bộ phận (2 dòng),
 *      Bậc nghề / Trình độ (Chip tag màu), Số điện thoại, Email công vụ, Ngày vào làm, Tài khoản & Quyền hạn (2 dòng),
 *      Trạng thái (Pill badge), Thao tác (3 nút vector: Xem chi tiết, Chỉnh sửa, Khóa/Mở)
 *   4. Chân trang chuyên nghiệp: Đèn trạng thái CSDL kết nối trực tuyến, phím tắt F2, F3, F5, Esc
 *   5. Hộp thoại nghiệp vụ: Thêm mới (tự sinh mã), Sửa hồ sơ, Xem thẻ nhân sự, Ma trận phân quyền vai trò, Xuất file CSV UTF-8
 *   6. Tự động đồng bộ CSDL SQL Server (QuanLySuaChuaXe)
 */
public class QuanLyNhanVien_UI extends JPanel {

    // ==== MÀU SẮC CHỦ ĐẠO FIGMA ERP ====
    private static final Color XANH_CHINH       = new Color(37, 99, 235);     // Blue 600
    private static final Color XANH_DAM         = new Color(15, 23, 42);      // Slate 900
    private static final Color NEN_APP          = new Color(248, 250, 252);   // Slate 50
    private static final Color NEN_CARD         = Color.WHITE;
    private static final Color VIEN_CARD        = new Color(226, 232, 240);   // Slate 200
    private static final Color CHU_TIEU_DE      = new Color(15, 23, 42);      // Slate 900
    private static final Color CHU_PHU          = new Color(100, 116, 139);   // Slate 500
    private static final Color XANH_LA          = new Color(22, 163, 74);     // Green 600
    private static final Color CAM              = new Color(217, 119, 6);     // Amber 600
    private static final Color XANH_DUONG_NHAT  = new Color(2, 132, 199);     // Sky 600

    private static final Color[] AVATAR_COLORS = {
            new Color(30, 41, 59),    // Slate dark (như NV-0091 trong mẫu)
            new Color(37, 99, 235),   // Blue
            new Color(16, 185, 129),  // Emerald
            new Color(139, 92, 246),  // Violet
            new Color(245, 158, 11),  // Amber
            new Color(6, 182, 212),   // Cyan
            new Color(236, 72, 153),  // Pink
            new Color(71, 85, 105)    // Slate muted
    };

    private final KhoDuLieu kho = KhoDuLieu.get();
    private NhanVien nvHienTai;

    // Components Header
    private JLabel lblBadgeCount;

    // Components Bộ lọc
    private JTextField txtTimKiem;
    private JComboBox<String> cboBoPhan;
    private JComboBox<String> cboTrangThai;
    private JCheckBox chkAnThoiViec;
    private JLabel lblSoLuongHienThi;

    // Components Bảng
    private JTable table;
    private DefaultTableModel model;
    private final List<NhanVien> danhSachHienThi = new ArrayList<>();

    // Components Footer
    private JLabel lblThongKeTong;
    private JLabel lblTrangThaiDB;

    public QuanLyNhanVien_UI() {
        this(null);
    }

    public QuanLyNhanVien_UI(NhanVien nv) {
        this.nvHienTai = nv;
        setLayout(new BorderLayout());
        setBackground(NEN_APP);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(NEN_APP);
        root.setBorder(new EmptyBorder(14, 18, 14, 18));

        // 1. Header trên cùng
        root.add(taoHeaderPanel(), BorderLayout.NORTH);

        // 2. Nội dung chính: Bộ lọc + Bảng danh sách
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(taoBoLocCard(), BorderLayout.NORTH);
        centerPanel.add(taoBangCard(), BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        // 3. Footer dưới đáy
        root.add(taoFooterPanel(), BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);

        setupKeyBindings();
        napBang();
    }

    // ===============================================================
    // 1. HEADER PANEL CHUẨN ERP
    // ===============================================================
    private JPanel taoHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Bên trái: Breadcrumb + Tiêu đề + Count Badge + Subtitle
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // Breadcrumb
        JLabel lblBreadcrumb = new JLabel("Hệ thống  ›  Nhân sự & Phân quyền  ›  Danh mục nhân viên");
        lblBreadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBreadcrumb.setForeground(new Color(100, 116, 139));
        left.add(lblBreadcrumb);
        left.add(Box.createVerticalStrut(3));

        // Tiêu đề + Badge
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(CHU_TIEU_DE);
        titleRow.add(lblTitle);

        lblBadgeCount = new JLabel("9 Hồ sơ");
        lblBadgeCount.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadgeCount.setForeground(new Color(37, 99, 235));
        lblBadgeCount.setBackground(new Color(239, 246, 255));
        lblBadgeCount.setOpaque(true);
        lblBadgeCount.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        titleRow.add(lblBadgeCount);
        left.add(titleRow);

        left.add(Box.createVerticalStrut(2));
        JLabel lblSub = new JLabel("Quản lý hồ sơ nhân sự, phân quyền tài khoản kỹ thuật, vai trò xưởng và trạng thái công tác.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(CHU_PHU);
        left.add(lblSub);

        header.add(left, BorderLayout.WEST);

        // Bên phải: 4 Nút chức năng
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.setOpaque(false);

        // Nút 1: + Thêm nhân viên mới (F2) — Custom paint chống bị Windows Look & Feel đè màu
        JButton btnThem = new JButton("+ Thêm nhân viên mới (F2)", UiIcon.of(UiIcon.PLUS, 13, Color.WHITE)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? new Color(23, 37, 84) :
                          (getModel().isRollover() ? new Color(29, 78, 216) : new Color(30, 58, 138));
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnThem.setContentAreaFilled(false);
        btnThem.setOpaque(false);
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnThem.setForeground(Color.WHITE);
        btnThem.setFocusPainted(false);
        btnThem.setBorder(new EmptyBorder(8, 14, 8, 14));
        btnThem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThem.addActionListener(e -> hienDialogThem());
        right.add(btnThem);



        // Nút 3: Phân quyền vai trò
        JButton btnPhanQuyen = new JButton("Phân quyền vai trò", UiIcon.of(UiIcon.SHIELD, 13, new Color(37, 99, 235)));
        btnPhanQuyen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPhanQuyen.setForeground(new Color(51, 65, 85));
        btnPhanQuyen.setBackground(Color.WHITE);
        btnPhanQuyen.setFocusPainted(false);
        btnPhanQuyen.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN_CARD, 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));
        btnPhanQuyen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPhanQuyen.addActionListener(e -> hienDialogPhanQuyen());
        right.add(btnPhanQuyen);

        // Nút 4: Tải lại (F5)
        JButton btnLamMoi = new JButton(UiIcon.of(UiIcon.REFRESH, 14, new Color(71, 85, 105)));
        btnLamMoi.setBackground(Color.WHITE);
        btnLamMoi.setToolTipText("Tải lại danh sách (F5)");
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setPreferredSize(new Dimension(36, 36));
        btnLamMoi.setBorder(new LineBorder(VIEN_CARD, 1, true));
        btnLamMoi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLamMoi.addActionListener(e -> {
            ConnectDB.getInstance().connect();
            napBang();
            JOptionPane.showMessageDialog(this, "Đã làm mới dữ liệu từ CSDL SQL Server!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        right.add(btnLamMoi);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ===============================================================
    // 2. BỘ LỌC CARD (SEARCH & FILTER)
    // ===============================================================
    private JPanel taoBoLocCard() {
        JPanel card = taoCardContainer(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Ô tìm kiếm với Icon Search và Placeholder tự vẽ
        gbc.gridx = 0;
        gbc.weightx = 0.45;
        txtTimKiem = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Vẽ icon Search bên trái
                UiIcon searchIcon = UiIcon.of(UiIcon.SEARCH, 13, new Color(148, 163, 184));
                searchIcon.paintIcon(this, g2, 10, (getHeight() - 13) / 2);

                // Vẽ placeholder nếu rỗng
                if (getText().isEmpty() && !hasFocus()) {
                    g2.setColor(new Color(148, 163, 184));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2.drawString("Tìm theo Mã NV, Tên, SĐT, CCCD...", 30, (getHeight() + g2.getFontMetrics().getAscent() - 4) / 2);
                }
                g2.dispose();
            }
        };
        txtTimKiem.setPreferredSize(new Dimension(240, 36));
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN_CARD, 1, true),
                new EmptyBorder(0, 30, 0, 10)
        ));
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { napBang(); }
            @Override public void removeUpdate(DocumentEvent e) { napBang(); }
            @Override public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        form.add(txtTimKiem, gbc);

        // 2. Bộ phận
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        cboBoPhan = new JComboBox<>(new String[]{
                "Tất cả bộ phận",
                "Ban Điều Hành Gara",
                "Ban Cố Vấn Dịch Vụ",
                "Xưởng Sửa Chữa Lớn",
                "Tổ Điện - Điện Tử",
                "Kho Phụ Tùng & Vật Tư",
                "Phòng Tài Chính - Kế Toán",
                "Tổ Học Việc Kỹ Thuật"
        });
        cboBoPhan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboBoPhan.setPreferredSize(new Dimension(170, 36));
        cboBoPhan.setBackground(Color.WHITE);
        cboBoPhan.addActionListener(e -> napBang());
        form.add(cboBoPhan, gbc);

        // 3. Trạng thái
        gbc.gridx = 2;
        gbc.weightx = 0.20;
        cboTrangThai = new JComboBox<>(new String[]{
                "Tất cả trạng thái",
                "Đang làm việc",
                "Nghỉ phép",
                "Thử việc",
                "Đã thôi việc"
        });
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboTrangThai.setPreferredSize(new Dimension(140, 36));
        cboTrangThai.setBackground(Color.WHITE);
        cboTrangThai.addActionListener(e -> napBang());
        form.add(cboTrangThai, gbc);

        // 4. Nút Đặt lại
        gbc.gridx = 3;
        gbc.weightx = 0.10;
        JButton btnReset = new JButton("Đặt lại", UiIcon.of(UiIcon.CLOSE, 11, new Color(100, 116, 139)));
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.setForeground(CHU_PHU);
        btnReset.setBackground(new Color(241, 245, 249));
        btnReset.setFocusPainted(false);
        btnReset.setBorder(new LineBorder(VIEN_CARD, 1, true));
        btnReset.setPreferredSize(new Dimension(85, 36));
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            txtTimKiem.setText("");
            cboBoPhan.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            chkAnThoiViec.setSelected(true);
            napBang();
        });
        form.add(btnReset, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    // ===============================================================
    // 3. BẢNG DANH SÁCH NHÂN SỰ GARA (11 CỘT CHUẨN MOCKUP)
    // ===============================================================
    private JPanel taoBangCard() {
        JPanel card = taoCardContainer(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Sub-header trên bảng
        JPanel subHeader = new JPanel(new BorderLayout());
        subHeader.setOpaque(false);

        JPanel subLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        subLeft.setOpaque(false);

        JLabel lblListTitle = new JLabel("DANH SÁCH HỒ SƠ NHÂN SỰ GARA");
        lblListTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblListTitle.setForeground(new Color(30, 41, 59));
        subLeft.add(lblListTitle);

        lblSoLuongHienThi = new JLabel("|  Hiển thị 9 / 9 nhân viên");
        lblSoLuongHienThi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoLuongHienThi.setForeground(CHU_PHU);
        subLeft.add(lblSoLuongHienThi);
        subHeader.add(subLeft, BorderLayout.WEST);

        chkAnThoiViec = new JCheckBox("Ẩn nhân viên đã thôi việc", true);
        chkAnThoiViec.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkAnThoiViec.setForeground(CHU_PHU);
        chkAnThoiViec.setOpaque(false);
        chkAnThoiViec.setFocusPainted(false);
        chkAnThoiViec.addActionListener(e -> napBang());
        subHeader.add(chkAnThoiViec, BorderLayout.EAST);

        card.add(subHeader, BorderLayout.NORTH);

        // 11 Cột khớp 100% Mockup ERP
        String[] columns = {
                "STT", "Mã NV", "Họ và tên", "Chức vụ / Bộ phận", "Bậc nghề / Trình độ",
                "Số điện thoại", "Email công vụ", "Ngày vào làm", "Tài khoản & Quyền hạn", "Trạng thái", "Thao tác"
        };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                int prefW = 0;
                for (int i = 0; i < getColumnCount(); i++) {
                    prefW += getColumnModel().getColumn(i).getPreferredWidth();
                }
                if (prefW > 0) {
                    d.width = Math.max(d.width, prefW);
                }
                return d;
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                if (getParent() instanceof JViewport vp) {
                    int prefW = 0;
                    for (int i = 0; i < getColumnCount(); i++) {
                        prefW += getColumnModel().getColumn(i).getPreferredWidth();
                    }
                    return vp.getWidth() >= prefW;
                }
                return false;
            }
        };
        table.setRowHeight(52);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(CHU_TIEU_DE);

        // Bật cuộn ngang tự nhiên và giữ đúng kích thước chuẩn của từng cột
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Header Table
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, VIEN_CARD));
        header.setReorderingAllowed(false);

        // Thiết lập kích thước cột cân đối, không bị khuất cột thao tác khi phóng to
        table.getColumnModel().getColumn(0).setPreferredWidth(45);   // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(85);   // Mã NV
        table.getColumnModel().getColumn(2).setPreferredWidth(210);  // Họ và tên (Initials Avatar + Tên đầy đủ)
        table.getColumnModel().getColumn(3).setPreferredWidth(190);  // Chức vụ / Bộ phận (2 dòng)
        table.getColumnModel().getColumn(4).setPreferredWidth(135);  // Bậc nghề / Trình độ (Chip tag)
        table.getColumnModel().getColumn(5).setPreferredWidth(115);  // Số điện thoại
        table.getColumnModel().getColumn(6).setPreferredWidth(190);  // Email công vụ
        table.getColumnModel().getColumn(7).setPreferredWidth(105);  // Ngày vào làm
        table.getColumnModel().getColumn(8).setPreferredWidth(195);  // Tài khoản & Quyền hạn (2 dòng)
        table.getColumnModel().getColumn(9).setPreferredWidth(130);  // Trạng thái (Pill badge)
        table.getColumnModel().getColumn(10).setPreferredWidth(110); // Thao tác (3 icon buttons)

        // Gán Renderers chuyên dụng
        table.getColumnModel().getColumn(0).setCellRenderer(new SttCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new MaNVCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new HoTenCellRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new ChucVuBoPhanCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new BacNgheCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new SdtCellRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new EmailCellRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new NgayVaoLamCellRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new TaiKhoanQuyenCellRenderer());
        table.getColumnModel().getColumn(9).setCellRenderer(new TrangThaiCellRenderer());
        table.getColumnModel().getColumn(10).setCellRenderer(new ThaoTacCellRenderer());

        // Bắt sự kiện click chuột
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || row >= danhSachHienThi.size()) return;

                NhanVien nv = danhSachHienThi.get(row);

                // Click vào cột Thao tác (col 10)
                if (col == 10) {
                    Rectangle rect = table.getCellRect(row, col, false);
                    int clickX = e.getX() - rect.x;
                    // Phân vùng 3 icon: Xem (~33px), Sửa (~33px), Khóa (~34px)
                    if (clickX < 34) {
                        hienDialogChiTiet(nv);
                    } else if (clickX < 67) {
                        hienDialogSua(nv);
                    } else {
                        voHieuHoa(nv);
                    }
                } else if (e.getClickCount() == 2) {
                    hienDialogChiTiet(nv);
                }
            }
        });

        table.setComponentPopupMenu(taoContextMenu());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(new LineBorder(VIEN_CARD, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    // ===============================================================
    // 4. FOOTER STATUS BAR
    // ===============================================================
    private JPanel taoFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 4, 4));

        lblThongKeTong = new JLabel("Tổng cộng: 9 nhân viên | 8 đang làm | 1 thử việc | 0 nghỉ phép");
        lblThongKeTong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongKeTong.setForeground(CHU_PHU);
        footer.add(lblThongKeTong, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel lblShortcuts = new JLabel("Phím tắt:  [F2] Thêm NV   [F3] Tìm kiếm   [F5] Tải lại   [Ctrl+E] Xuất file   [Esc] Đóng");
        lblShortcuts.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblShortcuts.setForeground(new Color(148, 163, 184));
        right.add(lblShortcuts);

        lblTrangThaiDB = new JLabel("● SQL Server: Trực tuyến");
        lblTrangThaiDB.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTrangThaiDB.setForeground(new Color(22, 163, 74));
        right.add(lblTrangThaiDB);

        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    // ===============================================================
    // NẠP VÀ LỌC DỮ LIỆU BẢNG
    // ===============================================================
    public void napBang() {
        String keyword = txtTimKiem != null ? txtTimKiem.getText().trim().toLowerCase() : "";
        String boPhanChon = cboBoPhan != null ? (String) cboBoPhan.getSelectedItem() : "Tất cả bộ phận";
        String trangThaiChon = cboTrangThai != null ? (String) cboTrangThai.getSelectedItem() : "Tất cả trạng thái";
        boolean anThoiViec = chkAnThoiViec == null || chkAnThoiViec.isSelected();

        danhSachHienThi.clear();
        for (NhanVien nv : kho.getNhanViens()) {
            if (anThoiViec && NhanVien.NGUNG.equalsIgnoreCase(nv.getTrangThai())) {
                continue;
            }

            if (boPhanChon != null && !boPhanChon.equals("Tất cả bộ phận")) {
                if (nv.getBoPhan() == null || !nv.getBoPhan().equalsIgnoreCase(boPhanChon)) {
                    continue;
                }
            }

            if (trangThaiChon != null && !trangThaiChon.equals("Tất cả trạng thái")) {
                String tt = nv.getTrangThai();
                if ("Đang làm việc".equals(trangThaiChon) && !NhanVien.DANG_LAM.equalsIgnoreCase(tt)) continue;
                if ("Nghỉ phép".equals(trangThaiChon) && !NhanVien.NGHI_PHEP.equalsIgnoreCase(tt)) continue;
                if ("Thử việc".equals(trangThaiChon) && !NhanVien.THU_VIEC.equalsIgnoreCase(tt)) continue;
                if ("Đã thôi việc".equals(trangThaiChon) && !NhanVien.NGUNG.equalsIgnoreCase(tt)) continue;
            }

            if (!keyword.isEmpty()) {
                boolean match = (nv.getMaNV() != null && nv.getMaNV().toLowerCase().contains(keyword))
                        || (nv.getHoTen() != null && nv.getHoTen().toLowerCase().contains(keyword))
                        || (nv.getSoDT() != null && nv.getSoDT().toLowerCase().contains(keyword))
                        || (nv.getEmail() != null && nv.getEmail().toLowerCase().contains(keyword))
                        || (nv.getVaiTro() != null && nv.getVaiTro().toLowerCase().contains(keyword))
                        || (nv.getTaiKhoan() != null && nv.getTaiKhoan().toLowerCase().contains(keyword))
                        || (nv.getBacNghe() != null && nv.getBacNghe().toLowerCase().contains(keyword));
                if (!match) continue;
            }

            danhSachHienThi.add(nv);
        }

        model.setRowCount(0);
        int stt = 1;
        for (NhanVien nv : danhSachHienThi) {
            model.addRow(new Object[]{
                    String.format("%02d", stt++),
                    nv.getMaNV(),
                    nv,
                    nv,
                    nv.getBacNghe(),
                    nv.getSoDT(),
                    nv.getEmail(),
                    nv.getNgayVaoLam(),
                    nv,
                    nv.getTrangThai(),
                    nv
            });
        }

        capNhatThongKe();
    }

    private void capNhatThongKe() {
        int tong = kho.getNhanViens().size();
        int dangHien = danhSachHienThi.size();

        long dangLam = kho.getNhanViens().stream().filter(NhanVien::dangHoatDong).count();
        long nghiPhep = kho.getNhanViens().stream().filter(n -> NhanVien.NGHI_PHEP.equalsIgnoreCase(n.getTrangThai())).count();
        long thuViec = kho.getNhanViens().stream().filter(n -> NhanVien.THU_VIEC.equalsIgnoreCase(n.getTrangThai())).count();
        long thoiViec = kho.getNhanViens().stream().filter(n -> NhanVien.NGUNG.equalsIgnoreCase(n.getTrangThai())).count();

        if (lblBadgeCount != null) {
            lblBadgeCount.setText(tong + " Hồ sơ");
        }
        if (lblSoLuongHienThi != null) {
            lblSoLuongHienThi.setText("|  Hiển thị " + dangHien + " / " + tong + " nhân viên");
        }
        if (lblThongKeTong != null) {
            lblThongKeTong.setText(String.format("Tổng cộng: %d nhân viên | %d đang làm | %d thử việc | %d nghỉ phép",
                    tong, dangLam, thuViec, nghiPhep));
        }
    }

    // ===============================================================
    // DIALOGS & CÁC CHỨC NĂNG NGHIỆP VỤ
    // ===============================================================

    /**
     * Dialog Thêm nhân viên mới
     */
    private void hienDialogThem() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Nhân Viên Mới", true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setSize(560, 620);
        dlg.setLocationRelativeTo(this);

        JPanel dlgHeader = new JPanel(new BorderLayout());
        dlgHeader.setBackground(new Color(248, 250, 252));
        dlgHeader.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Thêm Hồ Sơ Nhân Viên Mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(CHU_TIEU_DE);
        dlgHeader.add(lblTitle, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Nhập thông tin nhân sự và cấp tài khoản hệ thống");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(CHU_PHU);
        dlgHeader.add(lblSub, BorderLayout.SOUTH);
        dlg.add(dlgHeader, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        String maTiepTheo = sinhMaNVTiepTheo();
        JTextField txtMa = taoInput(maTiepTheo);
        JTextField txtTen = taoInput("");
        JTextField txtSdt = taoInput("");
        JTextField txtEmail = taoInput("");

        JComboBox<String> cboChucVu = new JComboBox<>(new String[]{
                "Cố vấn dịch vụ",
                "KTV Máy - Gầm",
                "KTV Điện - Điện Lạnh Ô tô",
                "Trưởng tổ Máy - Gầm",
                "Nhân viên thủ kho",
                "Kế toán kiêm Thu ngân",
                "Quản lý điều hành",
                "KTV Học việc"
        });
        cboChucVu.setEditable(true);
        cboChucVu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboChucVu.setPreferredSize(new Dimension(200, 34));

        JComboBox<String> cboBoPhanForm = new JComboBox<>(new String[]{
                "Ban Cố Vấn Dịch Vụ",
                "Xưởng Sửa Chữa Lớn",
                "Tổ Điện - Điện Tử",
                "Kho Phụ Tùng & Vật Tư",
                "Phòng Tài Chính - Kế Toán",
                "Ban Điều Hành Gara",
                "Tổ Học Việc Kỹ Thuật"
        });
        cboBoPhanForm.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboBoPhanForm.setPreferredSize(new Dimension(200, 34));

        JComboBox<String> cboBacNgheForm = new JComboBox<>(new String[]{
                "Thợ bậc 4/7",
                "Thợ bậc 3/7",
                "Thợ bậc 5/7",
                "Kỹ sư Ô tô ĐHBK",
                "Cử nhân Quản trị",
                "Cử nhân Kế toán",
                "Trung cấp Vật tư",
                "Trung cấp Nghề"
        });
        cboBacNgheForm.setEditable(true);
        cboBacNgheForm.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboBacNgheForm.setPreferredSize(new Dimension(200, 34));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        JTextField txtNgayVao = taoInput(sdf.format(new Date()));

        JComboBox<String> cboTrangThaiForm = new JComboBox<>(new String[]{
                "Đang làm việc", "Thử việc", "Nghỉ phép"
        });
        cboTrangThaiForm.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JCheckBox chkCapTK = new JCheckBox("Cấp tài khoản đăng nhập hệ thống", true);
        chkCapTK.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkCapTK.setForeground(XANH_CHINH);
        chkCapTK.setOpaque(false);

        JTextField txtTaiKhoan = taoInput("");
        JPasswordField txtMatKhau = new JPasswordField("123");
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMatKhau.setPreferredSize(new Dimension(200, 34));
        txtMatKhau.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN_CARD, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));

        JComboBox<String> cboVaiTroTK = new JComboBox<>(new String[]{
                "Nhân viên tiếp tân", "Nhân viên sửa chữa", "Nhân viên kho", "Nhân viên thu ngân", "Quản lý"
        });
        cboVaiTroTK.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtTen.getDocument().addDocumentListener(new DocumentListener() {
            private void updateSug() {
                String name = txtTen.getText().trim();
                if (!name.isEmpty()) {
                    String clean = boDauTiengViet(name).toLowerCase().replaceAll("[^a-z0-9]", "");
                    if (txtTaiKhoan.getText().isEmpty() || txtTaiKhoan.getText().startsWith("nv")) {
                        txtTaiKhoan.setText(clean.length() > 8 ? clean.substring(0, 8) : clean);
                    }
                    if (txtEmail.getText().isEmpty() || txtEmail.getText().contains("@garaauto.vn")) {
                        txtEmail.setText(clean + "@garaauto.vn");
                    }
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { updateSug(); }
            @Override public void removeUpdate(DocumentEvent e) { updateSug(); }
            @Override public void changedUpdate(DocumentEvent e) { updateSug(); }
        });

        int r = 0;
        themDongForm(body, gbc, r++, "Mã nhân viên *", txtMa);
        themDongForm(body, gbc, r++, "Họ và tên *", txtTen);
        themDongForm(body, gbc, r++, "Số điện thoại *", txtSdt);
        themDongForm(body, gbc, r++, "Email công vụ", txtEmail);
        themDongForm(body, gbc, r++, "Chức vụ *", cboChucVu);
        themDongForm(body, gbc, r++, "Bộ phận", cboBoPhanForm);
        themDongForm(body, gbc, r++, "Bậc thợ / Bằng cấp", cboBacNgheForm);
        themDongForm(body, gbc, r++, "Ngày vào làm", txtNgayVao);
        themDongForm(body, gbc, r++, "Trạng thái", cboTrangThaiForm);

        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 2;
        body.add(Box.createVerticalStrut(6), gbc);
        body.add(chkCapTK, gbc);

        gbc.gridwidth = 1;
        themDongForm(body, gbc, r++, "Tên đăng nhập", txtTaiKhoan);
        themDongForm(body, gbc, r++, "Mật khẩu ban đầu", txtMatKhau);
        themDongForm(body, gbc, r++, "Phân quyền vai trò", cboVaiTroTK);

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel dlgFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        dlgFooter.setBackground(new Color(248, 250, 252));
        dlgFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN_CARD));

        JButton btnHuy = taoNutOutline("✕ Hủy bỏ", e -> dlg.dispose());
        JButton btnLuu = taoNutPrimary("💾 Lưu hồ sơ", e -> {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            String email = txtEmail.getText().trim();
            String chucVu = (String) cboChucVu.getSelectedItem();
            String boPhan = (String) cboBoPhanForm.getSelectedItem();
            String bacNghe = (String) cboBacNgheForm.getSelectedItem();
            String ngayVao = txtNgayVao.getText().trim();
            String ttStr = (String) cboTrangThaiForm.getSelectedItem();

            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập Mã và Họ tên nhân viên!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                txtTen.requestFocus();
                return;
            }

            if (kho.timNhanVienTheoMa(ma) != null) {
                JOptionPane.showMessageDialog(dlg, "Mã nhân viên [" + ma + "] đã tồn tại!", "Trùng lặp", JOptionPane.WARNING_MESSAGE);
                txtMa.requestFocus();
                return;
            }

            if (sdt.isEmpty()) {
                sdt = "09" + (int)(Math.random() * 89999999 + 10000000);
            }

            String trangThai = NhanVien.DANG_LAM;
            if ("Thử việc".equals(ttStr)) trangThai = NhanVien.THU_VIEC;
            else if ("Nghỉ phép".equals(ttStr)) trangThai = NhanVien.NGHI_PHEP;

            String tk = chkCapTK.isSelected() && !txtTaiKhoan.getText().trim().isEmpty() ? txtTaiKhoan.getText().trim() : ma;
            String mk = new String(txtMatKhau.getPassword());
            if (mk.isEmpty()) mk = "123";
            String vaiTro = (String) cboVaiTroTK.getSelectedItem();

            NhanVien nvMoi = new NhanVien(ma, ten, tk, mk, vaiTro, trangThai, null, sdt, email, bacNghe, boPhan, ngayVao);
            boolean dbOk = kho.themNhanVien(nvMoi, sdt);

            napBang();
            dlg.dispose();
            JOptionPane.showMessageDialog(this,
                    "Thêm nhân viên [" + ten + "] thành công!" + (dbOk ? "\n(Đã đồng bộ trực tiếp vào SQL Server)" : ""),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        dlgFooter.add(btnHuy);
        dlgFooter.add(btnLuu);
        dlg.add(dlgFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    /**
     * Dialog Sửa thông tin nhân viên
     */
    private void hienDialogSua(NhanVien nv) {
        if (nv == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cập Nhật Hồ Sơ Nhân Viên", true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setSize(560, 620);
        dlg.setLocationRelativeTo(this);

        JPanel dlgHeader = new JPanel(new BorderLayout());
        dlgHeader.setBackground(new Color(248, 250, 252));
        dlgHeader.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Cập Nhật Hồ Sơ: " + nv.getHoTen());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(CHU_TIEU_DE);
        dlgHeader.add(lblTitle, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Mã NV: " + nv.getMaNV() + "  |  Tài khoản: " + nv.getTaiKhoan());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(CHU_PHU);
        dlgHeader.add(lblSub, BorderLayout.SOUTH);
        dlg.add(dlgHeader, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        JTextField txtMa = taoInput(nv.getMaNV());
        txtMa.setEditable(false);
        txtMa.setBackground(new Color(241, 245, 249));

        JTextField txtTen = taoInput(nv.getHoTen());
        JTextField txtSdt = taoInput(nv.getSoDT());
        JTextField txtEmail = taoInput(nv.getEmail());

        JComboBox<String> cboChucVu = new JComboBox<>(new String[]{
                "Cố vấn trưởng / Điều hành", "Cố vấn dịch vụ 2", "KTV Máy - Gầm",
                "KTV Điện - Lạnh Ô tô", "Trưởng tổ Máy - Gầm", "Nhân viên kho",
                "Kế toán kiêm Thu ngân", "Quản lý", "KTV Học việc Máy - Gầm"
        });
        cboChucVu.setEditable(true);
        cboChucVu.setSelectedItem(nv.getVaiTro());
        cboChucVu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboChucVu.setPreferredSize(new Dimension(200, 34));

        JComboBox<String> cboBoPhanForm = new JComboBox<>(new String[]{
                "Ban Cố Vấn Dịch Vụ", "Tổ Tiếp Nhận Xe", "Xưởng Sửa Chữa Lớn",
                "Tổ Điện Điện Tử", "Kho Phụ Tùng & Vật Tư", "Phòng Tài Chính - Kế Toán",
                "Ban Giám Đốc Điều Hành", "Tổ Học Việc Kỹ Thuật"
        });
        cboBoPhanForm.setSelectedItem(nv.getBoPhan());
        cboBoPhanForm.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboBoPhanForm.setPreferredSize(new Dimension(200, 34));

        JComboBox<String> cboBacNgheForm = new JComboBox<>(new String[]{
                "Kỹ sư Ô tô ĐHBK", "Thợ bậc 4/7", "Thợ bậc 5/7", "Thợ bậc 3/7",
                "Cử nhân Quản trị", "Cử nhân Kế toán", "Trung cấp Vật tư", "Trung cấp Nghề", "Thạc sĩ QTKD"
        });
        cboBacNgheForm.setEditable(true);
        cboBacNgheForm.setSelectedItem(nv.getBacNghe());
        cboBacNgheForm.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboBacNgheForm.setPreferredSize(new Dimension(200, 34));

        JTextField txtNgayVao = taoInput(nv.getNgayVaoLam());

        JComboBox<String> cboTrangThaiForm = new JComboBox<>(new String[]{
                "Đang làm việc", "Nghỉ phép", "Thử việc", "Đã thôi việc"
        });
        if (NhanVien.DANG_LAM.equalsIgnoreCase(nv.getTrangThai())) cboTrangThaiForm.setSelectedItem("Đang làm việc");
        else if (NhanVien.NGHI_PHEP.equalsIgnoreCase(nv.getTrangThai())) cboTrangThaiForm.setSelectedItem("Nghỉ phép");
        else if (NhanVien.THU_VIEC.equalsIgnoreCase(nv.getTrangThai())) cboTrangThaiForm.setSelectedItem("Thử việc");
        else cboTrangThaiForm.setSelectedItem("Đã thôi việc");
        cboTrangThaiForm.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField txtTaiKhoan = taoInput(nv.getTaiKhoan());
        JPasswordField txtMatKhau = new JPasswordField(nv.getMatKhau() != null ? nv.getMatKhau() : "");
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMatKhau.setPreferredSize(new Dimension(200, 34));
        txtMatKhau.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN_CARD, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));

        JComboBox<String> cboVaiTroTK = new JComboBox<>(new String[]{
                "Nhân viên tiếp tân", "Nhân viên sửa chữa", "Nhân viên kho", "Nhân viên thu ngân", "Quản lý"
        });
        cboVaiTroTK.setSelectedItem(nv.getVaiTro());
        cboVaiTroTK.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        int r = 0;
        themDongForm(body, gbc, r++, "Mã nhân viên", txtMa);
        themDongForm(body, gbc, r++, "Họ và tên *", txtTen);
        themDongForm(body, gbc, r++, "Số điện thoại *", txtSdt);
        themDongForm(body, gbc, r++, "Email công vụ", txtEmail);
        themDongForm(body, gbc, r++, "Chức danh", cboChucVu);
        themDongForm(body, gbc, r++, "Bộ phận", cboBoPhanForm);
        themDongForm(body, gbc, r++, "Bậc thợ / Bằng cấp", cboBacNgheForm);
        themDongForm(body, gbc, r++, "Ngày vào làm", txtNgayVao);
        themDongForm(body, gbc, r++, "Trạng thái", cboTrangThaiForm);

        gbc.gridx = 0; gbc.gridy = r++; gbc.gridwidth = 2;
        body.add(Box.createVerticalStrut(6), gbc);
        JLabel lblTkTitle = new JLabel("Thông tin tài khoản đăng nhập");
        lblTkTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTkTitle.setForeground(XANH_CHINH);
        body.add(lblTkTitle, gbc);

        gbc.gridwidth = 1;
        themDongForm(body, gbc, r++, "Tên đăng nhập", txtTaiKhoan);
        themDongForm(body, gbc, r++, "Mật khẩu", txtMatKhau);
        themDongForm(body, gbc, r++, "Vai trò phân quyền", cboVaiTroTK);

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel dlgFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        dlgFooter.setBackground(new Color(248, 250, 252));
        dlgFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN_CARD));

        JButton btnHuy = taoNutOutline("✕ Hủy bỏ", e -> dlg.dispose());
        JButton btnLuu = taoNutPrimary("💾 Cập nhật", e -> {
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Họ tên không được để trống!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            nv.setHoTen(ten);
            nv.setSoDT(txtSdt.getText().trim());
            nv.setEmail(txtEmail.getText().trim());
            nv.setVaiTro((String) cboChucVu.getSelectedItem());
            nv.setBoPhan((String) cboBoPhanForm.getSelectedItem());
            nv.setBacNghe((String) cboBacNgheForm.getSelectedItem());
            nv.setNgayVaoLam(txtNgayVao.getText().trim());

            String ttStr = (String) cboTrangThaiForm.getSelectedItem();
            if ("Đang làm việc".equals(ttStr)) nv.setTrangThai(NhanVien.DANG_LAM);
            else if ("Nghỉ phép".equals(ttStr)) nv.setTrangThai(NhanVien.NGHI_PHEP);
            else if ("Thử việc".equals(ttStr)) nv.setTrangThai(NhanVien.THU_VIEC);
            else nv.setTrangThai(NhanVien.NGUNG);

            nv.setTaiKhoan(txtTaiKhoan.getText().trim());
            String mkMoi = new String(txtMatKhau.getPassword()).trim();
            if (!mkMoi.isEmpty()) nv.setMatKhau(mkMoi);

            boolean dbOk = kho.capNhatNhanVien(nv);
            napBang();
            dlg.dispose();
            JOptionPane.showMessageDialog(this,
                    "Cập nhật hồ sơ nhân viên [" + ten + "] thành công!" + (dbOk ? "\n(Đã đồng bộ trực tiếp vào SQL Server)" : ""),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        dlgFooter.add(btnHuy);
        dlgFooter.add(btnLuu);
        dlg.add(dlgFooter, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    /**
     * Dialog Xem chi tiết hồ sơ nhân viên (Card Profile)
     */
    private void hienDialogChiTiet(NhanVien nv) {
        if (nv == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hồ Sơ Chi Tiết Nhân Viên", true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setSize(520, 500);
        dlg.setLocationRelativeTo(this);

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(new Color(248, 250, 252));
        pnlTop.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pnlUser.setOpaque(false);

        JPanel avatarBig = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int hash = Math.abs(nv.getHoTen().hashCode());
                g2.setColor(AVATAR_COLORS[hash % AVATAR_COLORS.length]);
                g2.fillRoundRect(0, 0, 56, 56, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                String in = tinhInitials(nv.getHoTen());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (56 - fm.stringWidth(in)) / 2;
                int ty = (56 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(in, tx, ty);
                g2.dispose();
            }
        };
        avatarBig.setPreferredSize(new Dimension(56, 56));
        avatarBig.setOpaque(false);
        pnlUser.add(avatarBig);

        JPanel pnlName = new JPanel();
        pnlName.setLayout(new BoxLayout(pnlName, BoxLayout.Y_AXIS));
        pnlName.setOpaque(false);

        JLabel lblName = new JLabel(nv.getHoTen());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(CHU_TIEU_DE);
        pnlName.add(lblName);
        pnlName.add(Box.createVerticalStrut(3));

        JLabel lblRole = new JLabel(nv.getVaiTro() + "  •  " + nv.getBoPhan());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRole.setForeground(CHU_PHU);
        pnlName.add(lblRole);

        pnlUser.add(pnlName);
        pnlTop.add(pnlUser, BorderLayout.WEST);

        JLabel lblStatusBadge = new JLabel(nv.dangHoatDong() ? "● Đang làm việc" : ("● " + nv.getTrangThai()));
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusBadge.setForeground(nv.dangHoatDong() ? XANH_LA : CAM);
        pnlTop.add(lblStatusBadge, BorderLayout.EAST);

        dlg.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlInfo = new JPanel(new GridLayout(4, 2, 16, 14));
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(new EmptyBorder(20, 24, 20, 24));

        themChiTiet(pnlInfo, "Mã nhân viên:", nv.getMaNV());
        themChiTiet(pnlInfo, "Số điện thoại:", nv.getSoDT());
        themChiTiet(pnlInfo, "Email công vụ:", nv.getEmail());
        themChiTiet(pnlInfo, "Bậc nghề / Trình độ:", nv.getBacNghe());
        themChiTiet(pnlInfo, "Bộ phận công tác:", nv.getBoPhan());
        themChiTiet(pnlInfo, "Ngày vào làm:", nv.getNgayVaoLam());
        themChiTiet(pnlInfo, "Tên tài khoản:", nv.getTaiKhoan());
        themChiTiet(pnlInfo, "Vai trò hệ thống:", nv.getVaiTro());

        dlg.add(pnlInfo, BorderLayout.CENTER);

        JPanel pnlFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        pnlFoot.setBackground(new Color(248, 250, 252));
        pnlFoot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN_CARD));

        JButton btnSuaNhanh = taoNutPrimary("✏ Sửa hồ sơ", e -> {
            dlg.dispose();
            hienDialogSua(nv);
        });
        JButton btnDong = taoNutOutline("Đóng", e -> dlg.dispose());

        pnlFoot.add(btnSuaNhanh);
        pnlFoot.add(btnDong);
        dlg.add(pnlFoot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void themChiTiet(JPanel p, String title, String val) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        JLabel l1 = new JLabel(title);
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l1.setForeground(CHU_PHU);
        JLabel l2 = new JLabel(val != null ? val : "—");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l2.setForeground(CHU_TIEU_DE);
        row.add(l1, BorderLayout.NORTH);
        row.add(l2, BorderLayout.CENTER);
        p.add(row);
    }

    /**
     * Vô hiệu hóa / Khóa tài khoản
     */
    private void voHieuHoa(NhanVien nv) {
        if (nv == null) return;

        long soDangLam = kho.getNhanViens().stream().filter(NhanVien::dangHoatDong).count();
        if (nv.dangHoatDong() && soDangLam <= 1) {
            JOptionPane.showMessageDialog(this,
                    "Không thể vô hiệu hóa nhân sự đang làm việc duy nhất trong hệ thống!",
                    "Cảnh báo an toàn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ttMoi;
        if (nv.dangHoatDong()) {
            int ch = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn vô hiệu hóa / chuyển sang THÔI VIỆC cho nhân sự:\n" +
                    "[" + nv.getMaNV() + "] " + nv.getHoTen() + " (" + nv.getVaiTro() + ")?\n\n" +
                    "Tài khoản đăng nhập sẽ bị khóa. Toàn bộ lịch sử tiếp nhận/sửa chữa/hóa đơn vẫn được bảo toàn nguyên vẹn.",
                    "Xác nhận khóa hồ sơ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ch != JOptionPane.YES_OPTION) return;
            ttMoi = NhanVien.NGUNG;
        } else {
            int ch = JOptionPane.showConfirmDialog(this,
                    "Kích hoạt mở lại tài khoản làm việc cho nhân viên:\n" +
                    "[" + nv.getMaNV() + "] " + nv.getHoTen() + "?",
                    "Xác nhận kích hoạt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ch != JOptionPane.YES_OPTION) return;
            ttMoi = NhanVien.DANG_LAM;
        }

        boolean ok = kho.doiTrangThaiNhanVien(nv.getMaNV(), ttMoi);
        napBang();
        JOptionPane.showMessageDialog(this,
                (NhanVien.DANG_LAM.equals(ttMoi) ? "Đã kích hoạt hoạt động lại cho " : "Đã chuyển sang thôi việc / khóa tài khoản của ")
                + nv.getHoTen() + (ok ? "\n(Đã cập nhật trực tiếp vào SQL Server)" : ""));
    }

    /**
     * Dialog Phân quyền vai trò
     */
    private void hienDialogPhanQuyen() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ma Trận Phân Quyền Vai Trò Xưởng Gara The T4", true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setSize(750, 520);
        dlg.setLocationRelativeTo(this);

        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setBackground(new Color(248, 250, 252));
        pnlHead.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Phân Quyền Vai Trò & Phân Hệ Chức Năng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(CHU_TIEU_DE);
        pnlHead.add(lblTitle, BorderLayout.WEST);

        JLabel lblSub = new JLabel("Hệ thống phân quyền chuẩn theo quy trình vận hành dịch vụ gara ô tô The T4");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(CHU_PHU);
        pnlHead.add(lblSub, BorderLayout.SOUTH);
        dlg.add(pnlHead, BorderLayout.NORTH);

        String[] cols = {"VAI TRÒ / CHỨC DANH", "MÔ TẢ TRÁCH NHIỆM CHÍNH", "QUYỀN TRUY CẬP PHÂN HỆ", "NHÂN SỰ ĐẢM TRÁCH"};
        DefaultTableModel m = new DefaultTableModel(cols, 0);
        JTable tbl = new JTable(m);
        tbl.setRowHeight(42);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBackground(new Color(248, 250, 252));

        m.addRow(new Object[]{"Admin / Ban Giám Đốc", "Toàn quyền điều hành, cấu hình, quản lý nhân sự & tài chính", "Tất cả chức năng + Báo cáo + Cấu hình", demNhanSuTheoVaiTro("Quản lý") + " người"});
        m.addRow(new Object[]{"Cố vấn dịch vụ / Tiếp tân", "Tiếp nhận xe, kiểm tra tình trạng ban đầu, báo giá sơ bộ", "Tiếp nhận xe, Khách hàng, Tra cứu xe", (demNhanSuTheoVaiTro("Cố vấn") + demNhanSuTheoVaiTro("tiếp tân")) + " người"});
        m.addRow(new Object[]{"Kỹ thuật viên Sửa chữa", "Lập phiếu SC, chẩn đoán hư hỏng, xuất phụ tùng, sửa xe", "Phiếu sửa chữa, Phân công KTV, Tiến độ", (demNhanSuTheoVaiTro("KTV") + demNhanSuTheoVaiTro("sửa chữa")) + " người"});
        m.addRow(new Object[]{"Nhân viên Thủ kho", "Quản lý tồn kho phụ tùng, kiểm tra nhập kho, xuất kho", "Kho phụ tùng, Nhập kho, Xuất kho, Tồn kho", demNhanSuTheoVaiTro("kho") + " người"});
        m.addRow(new Object[]{"Kế toán / Thu ngân", "Thu ngân quầy, xuất hóa đơn, in phiếu thu, báo cáo ca", "Quầy thu ngân, Thanh toán hóa đơn, Báo cáo DT", (demNhanSuTheoVaiTro("thu ngân") + demNhanSuTheoVaiTro("Kế toán")) + " người"});

        tbl.getColumnModel().getColumn(0).setPreferredWidth(170);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(230);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(200);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(110);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(new LineBorder(VIEN_CARD, 1, true));
        dlg.add(sp, BorderLayout.CENTER);

        JPanel pnlFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        pnlFoot.setBackground(new Color(248, 250, 252));
        JButton btnDong = taoNutPrimary("Đã hiểu & Đóng", e -> dlg.dispose());
        pnlFoot.add(btnDong);
        dlg.add(pnlFoot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private int demNhanSuTheoVaiTro(String key) {
        int cnt = 0;
        String k = key.toLowerCase();
        for (NhanVien nv : kho.getNhanViens()) {
            if ((nv.getVaiTro() != null && nv.getVaiTro().toLowerCase().contains(k)) ||
                (nv.getBoPhan() != null && nv.getBoPhan().toLowerCase().contains(k))) {
                cnt++;
            }
        }
        return cnt;
    }

    private JPopupMenu taoContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem mXem = new JMenuItem("Xem chi tiết hồ sơ", UiIcon.of(UiIcon.EYE, 13, new Color(37, 99, 235)));
        mXem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < danhSachHienThi.size()) hienDialogChiTiet(danhSachHienThi.get(row));
        });
        menu.add(mXem);

        JMenuItem mSua = new JMenuItem("Chỉnh sửa thông tin", UiIcon.of(UiIcon.EDIT, 13, new Color(217, 119, 6)));
        mSua.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < danhSachHienThi.size()) hienDialogSua(danhSachHienThi.get(row));
        });
        menu.add(mSua);

        menu.addSeparator();

        JMenuItem mKhoa = new JMenuItem("Khóa / Mở khóa tài khoản", UiIcon.of(UiIcon.LOCK, 13, new Color(225, 29, 72)));
        mKhoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < danhSachHienThi.size()) voHieuHoa(danhSachHienThi.get(row));
        });
        menu.add(mKhoa);

        return menu;
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "themNV");
        am.put("themNV", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { hienDialogThem(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "timKiem");
        am.put("timKiem", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { txtTimKiem.requestFocus(); txtTimKiem.selectAll(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "lamMoi");
        am.put("lamMoi", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { napBang(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "boChon");
        am.put("boChon", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { table.clearSelection(); }
        });
    }

    private String sinhMaNVTiepTheo() {
        int max = 0;
        for (NhanVien nv : kho.getNhanViens()) {
            String m = nv.getMaNV();
            if (m != null && m.toUpperCase().startsWith("NV")) {
                try {
                    int num = Integer.parseInt(m.substring(2).replaceAll("[^0-9]", ""));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return String.format("NV-%04d", max + 1);
    }

    // ===============================================================
    // CÁC COMPONENT CUSTOM TABLE CELL RENDERER THEO MẪU ERP
    // ===============================================================

    /** Cột 0: STT (01, 02.. Centered) */
    private static class SttCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(new Color(100, 116, 139));
            lbl.setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            return lbl;
        }
    }

    /** Cột 1: Mã NV (Bold Navy Blue NV-0091) */
    private static class MaNVCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(new Color(30, 58, 138)); // Blue 900
            lbl.setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            return lbl;
        }
    }

    /** Cột 2: Họ và tên (Tên đầy đủ, sạch đẹp, không kèm ký hiệu) */
    private static class HoTenCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(CHU_TIEU_DE);
            lbl.setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            lbl.setBorder(new EmptyBorder(0, 14, 0, 14));
            if (val instanceof NhanVien nv) {
                lbl.setText(nv.getHoTen());
            } else if (val != null) {
                lbl.setText(val.toString());
            }
            return lbl;
        }
    }

    /** Cột 3: Chức vụ / Bộ phận (2 dòng) */
    private static class ChucVuBoPhanCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel lblChucVu = new JLabel();
        private final JLabel lblBoPhan = new JLabel();

        public ChucVuBoPhanCellRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(true);
            setBorder(new EmptyBorder(8, 8, 8, 8));

            lblChucVu.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblChucVu.setForeground(new Color(30, 41, 59));

            lblBoPhan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblBoPhan.setForeground(CHU_PHU);

            add(lblChucVu);
            add(Box.createVerticalStrut(2));
            add(lblBoPhan);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            if (val instanceof NhanVien nv) {
                lblChucVu.setText(nv.getVaiTro());
                lblBoPhan.setText(nv.getBoPhan());
            }
            return this;
        }
    }

    /** Cột 4: Bậc nghề / Trình độ (Chip tag màu chuẩn ERP) */
    private static class BacNgheCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel lblChip = new JLabel("", SwingConstants.CENTER);

        public BacNgheCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 12));
            setOpaque(true);

            lblChip.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblChip.setOpaque(true);
            add(lblChip);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            String txt = val != null ? val.toString() : "—";
            lblChip.setText(txt);

            String low = txt.toLowerCase();
            if (low.contains("kỹ sư") || low.contains("cử nhân") || low.contains("thạc sĩ")) {
                lblChip.setBackground(new Color(239, 246, 255)); // Blue 50
                lblChip.setForeground(new Color(29, 78, 216));   // Blue 700
                lblChip.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(191, 219, 254), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
            } else if (low.contains("bậc")) {
                lblChip.setBackground(new Color(254, 243, 199)); // Amber 100
                lblChip.setForeground(new Color(180, 83, 9));    // Amber 700
                lblChip.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(253, 230, 138), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
            } else {
                lblChip.setBackground(new Color(241, 245, 249)); // Slate 100
                lblChip.setForeground(new Color(71, 85, 105));   // Slate 600
                lblChip.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(203, 213, 225), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
            }
            return this;
        }
    }

    /** Cột 5: Số điện thoại (Format chuẩn chấm cách e.g. 0903.118.892) */
    private static class SdtCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(new Color(30, 41, 59));
            lbl.setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));

            if (val != null) {
                String s = val.toString().replaceAll("[^0-9]", "");
                if (s.length() == 10) {
                    lbl.setText(s.substring(0, 4) + "." + s.substring(4, 7) + "." + s.substring(7));
                }
            }
            return lbl;
        }
    }

    /** Cột 6: Email công vụ */
    private static class EmailCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(new Color(71, 85, 105));
            lbl.setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
            return lbl;
        }
    }

    /** Cột 7: Ngày vào làm (dd/MM/yyyy Centered) */
    private static class NgayVaoLamCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(new Color(71, 85, 105));
            lbl.setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            return lbl;
        }
    }

    /** Cột 8: Tài khoản & Quyền hạn (2 dòng: Icon + Role bold, Subline UID + Quyền) */
    private static class TaiKhoanQuyenCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel lblRole = new JLabel();
        private final JLabel lblUid = new JLabel();

        public TaiKhoanQuyenCellRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(true);
            setBorder(new EmptyBorder(8, 8, 8, 8));

            lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblRole.setForeground(new Color(30, 41, 59));

            lblUid.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblUid.setForeground(CHU_PHU);

            add(lblRole);
            add(Box.createVerticalStrut(2));
            add(lblUid);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            if (val instanceof NhanVien nv) {
                String vt = nv.getVaiTro() != null ? nv.getVaiTro().toUpperCase() : "NHÂN VIÊN";
                String roleTitle;
                String desc;

                if (vt.contains("QUẢN LÝ") || vt.contains("ADMIN")) {
                    roleTitle = "Admin Hệ Thống";
                    desc = "UID: " + nv.getTaiKhoan() + " / Quyền tối cao";
                    lblRole.setIcon(UiIcon.of(UiIcon.SHIELD, 12, new Color(126, 34, 206)));
                } else if (vt.contains("TRƯỞNG TỔ") || vt.contains("KTV TRƯỞNG")) {
                    roleTitle = "KTV Trưởng";
                    desc = "UID: " + nv.getTaiKhoan() + " / Giao lệnh xưởng";
                    lblRole.setIcon(UiIcon.of(UiIcon.WRENCH, 12, new Color(217, 119, 6)));
                } else if (vt.contains("THU NGÂN") || vt.contains("KẾ TOÁN")) {
                    roleTitle = "Thu ngân & Kế toán";
                    desc = "UID: " + nv.getTaiKhoan() + " / Thu tiền, Hóa đơn";
                    lblRole.setIcon(UiIcon.of(UiIcon.BILL, 12, new Color(22, 163, 74)));
                } else if (vt.contains("TIẾP TÂN") || vt.contains("CỐ VẤN")) {
                    roleTitle = "Cố vấn dịch vụ";
                    desc = "UID: " + nv.getTaiKhoan() + " / Tiếp nhận xe";
                    lblRole.setIcon(UiIcon.of(UiIcon.USER, 12, new Color(3, 105, 161)));
                } else if (vt.contains("KHO")) {
                    roleTitle = "Thủ kho vật tư";
                    desc = "UID: " + nv.getTaiKhoan() + " / Xuất nhập kho";
                    lblRole.setIcon(UiIcon.of(UiIcon.BOX, 12, new Color(180, 83, 9)));
                } else if (vt.contains("HỌC VIỆC") || vt.contains("THỬ VIỆC")) {
                    roleTitle = "Chưa cấp tài khoản";
                    desc = "Thực tập sinh kỹ thuật";
                    lblRole.setIcon(UiIcon.of(UiIcon.LOCK, 12, new Color(148, 163, 184)));
                } else {
                    roleTitle = "Kỹ thuật viên";
                    desc = "UID: " + nv.getTaiKhoan() + " / Cập nhật tiến độ";
                    lblRole.setIcon(UiIcon.of(UiIcon.WRENCH, 12, new Color(37, 99, 235)));
                }

                lblRole.setText(" " + roleTitle);
                lblUid.setText(desc);
            }
            return this;
        }
    }

    /** Cột 9: Trạng thái (Pill badge chuẩn ERP) */
    private static class TrangThaiCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel lblPill = new JLabel("", SwingConstants.CENTER);

        public TrangThaiCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 12));
            setOpaque(true);

            lblPill.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblPill.setOpaque(true);
            add(lblPill);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            String tt = val != null ? val.toString() : NhanVien.DANG_LAM;

            if (NhanVien.DANG_LAM.equalsIgnoreCase(tt)) {
                lblPill.setText("● Đang làm việc");
                lblPill.setBackground(new Color(220, 252, 231)); // Green 100
                lblPill.setForeground(new Color(21, 128, 61));   // Green 700
                lblPill.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(134, 239, 172), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
            } else if (NhanVien.NGHI_PHEP.equalsIgnoreCase(tt)) {
                lblPill.setText("● Nghỉ phép (Phép năm)");
                lblPill.setBackground(new Color(254, 243, 199)); // Amber 100
                lblPill.setForeground(new Color(180, 83, 9));    // Amber 700
                lblPill.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(253, 230, 138), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
            } else if (NhanVien.THU_VIEC.equalsIgnoreCase(tt)) {
                lblPill.setText("● Học việc / Thử việc");
                lblPill.setBackground(new Color(224, 242, 254)); // Sky 100
                lblPill.setForeground(new Color(3, 105, 161));   // Sky 700
                lblPill.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(125, 211, 252), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
            } else {
                lblPill.setText("● Đã thôi việc");
                lblPill.setBackground(new Color(241, 245, 249)); // Slate 100
                lblPill.setForeground(new Color(100, 116, 139)); // Slate 500
                lblPill.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(203, 213, 225), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
            }
            return this;
        }
    }

    /** Cột 10: Thao tác (3 icon button vector: Xem Eye, Sửa Edit, Khóa Lock/Key) */
    private static class ThaoTacCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel btnXem = new JLabel("", SwingConstants.CENTER);
        private final JLabel btnSua = new JLabel("", SwingConstants.CENTER);
        private final JLabel btnKhoa = new JLabel("", SwingConstants.CENTER);

        public ThaoTacCellRenderer() {
            setLayout(new GridLayout(1, 3, 4, 0));
            setOpaque(true);
            setBorder(new EmptyBorder(12, 5, 12, 5));

            dinhDangButton(btnXem, UiIcon.of(UiIcon.EYE, 13, new Color(71, 85, 105)), "Xem chi tiết hồ sơ");
            dinhDangButton(btnSua, UiIcon.of(UiIcon.EDIT, 13, new Color(71, 85, 105)), "Chỉnh sửa thông tin");
            dinhDangButton(btnKhoa, UiIcon.of(UiIcon.KEY, 13, new Color(71, 85, 105)), "Khóa / Mở khóa");

            add(btnXem);
            add(btnSua);
            add(btnKhoa);
        }

        private void dinhDangButton(JLabel lbl, Icon icon, String tip) {
            lbl.setIcon(icon);
            lbl.setBackground(Color.WHITE);
            lbl.setOpaque(true);
            lbl.setToolTipText(tip);
            lbl.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
            setBackground(isSel ? t.getSelectionBackground() : (r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250)));
            if (val instanceof NhanVien nv) {
                if (nv.dangHoatDong()) {
                    btnKhoa.setIcon(UiIcon.of(UiIcon.KEY, 13, new Color(71, 85, 105)));
                    btnKhoa.setToolTipText("Khóa tài khoản / Cho thôi việc");
                } else {
                    btnKhoa.setIcon(UiIcon.of(UiIcon.LOCK, 13, new Color(225, 29, 72)));
                    btnKhoa.setToolTipText("Kích hoạt lại tài khoản");
                }
            }
            return this;
        }
    }

    // ===============================================================
    // CÁC HÀM TIỆN ÍCH
    // ===============================================================

    private static String tinhInitials(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) return "NV";
        String[] parts = hoTen.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }

    private static String boDauTiengViet(String s) {
        if (s == null) return "";
        String nfd = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace('đ', 'd').replace('Đ', 'D');
    }

    private JPanel taoCardContainer(LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(NEN_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN_CARD, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return card;
    }

    private JButton taoNutPrimary(String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(XANH_CHINH);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    private JButton taoNutOutline(String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(51, 65, 85));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN_CARD, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(al);
        return btn;
    }

    private JTextField taoInput(String val) {
        JTextField tf = new JTextField(val);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(200, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(VIEN_CARD, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));
        return tf;
    }

    private void themDongForm(JPanel p, GridBagConstraints gbc, int row, String lbl, JComponent cmp) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(51, 65, 85));
        p.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        p.add(cmp, gbc);
    }
}
