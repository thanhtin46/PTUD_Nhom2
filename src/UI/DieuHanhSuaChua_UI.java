package UI;

import DAO.DieuHanhSuaChua_DAO;
import Entity.NhanVien;
import Entity.PhieuDieuHanhDTO;
import Entity.ThongKeKtvDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Điều hành sửa chữa: theo dõi tiến độ phiếu sửa chữa và khối lượng công việc của kỹ thuật viên.
 */
public class DieuHanhSuaChua_UI extends JPanel {

    private final NhanVien nvHienTai;
    private final Main_UI mainUI;
    private final DieuHanhSuaChua_DAO dao = new DieuHanhSuaChua_DAO();

    // Dữ liệu bộ nhớ tạm sau khi nạp từ SQL
    private final ArrayList<PhieuDieuHanhDTO> dsPhieuGoc = new ArrayList<>();
    private final ArrayList<ThongKeKtvDTO> dsKtvGoc = new ArrayList<>();

    // KTV hiện tại đang lọc (null = tất cả)
    private String maKtvDangLoc = null;

    // Các nhãn chỉ số KPI tổng quan
    private JLabel lblStatChuaHT;
    private JLabel lblStatHenHomNay;
    private JLabel lblStatQuaHen;
    private JLabel lblStatTongKtv;
    private JLabel lblCapNhatLuc;

    // Bảng Kỹ thuật viên
    private DefaultTableModel modelKtv;
    private JTable tableKtv;
    private TableRowSorter<DefaultTableModel> sorterKtv;
    private JLabel lblThongBaoLocKtv;
    private JButton btnBoLocKtv;

    // Bảng Phiếu sửa chữa
    private DefaultTableModel modelPhieu;
    private JTable tablePhieu;
    private JTextField txtTuKhoa;
    private JComboBox<String> cboLocHan;
    private JLabel lblDemPhieu;

    public DieuHanhSuaChua_UI() {
        this(null, null);
    }

    public DieuHanhSuaChua_UI(NhanVien nvHienTai, Main_UI mainUI) {
        this.nvHienTai = nvHienTai;
        this.mainUI = mainUI;

        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        root.setLayout(new BorderLayout(0, 14));
        add(root, BorderLayout.CENTER);

        // 1. Header & KPI Cards
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(taoHeaderPanel());
        topPanel.add(Box.createVerticalStrut(12));
        topPanel.add(taoLuoiKPICards());
        root.add(topPanel, BorderLayout.NORTH);

        // 2. Nội dung chính: SplitPane (Trái: Thống kê KTV - Phải: Theo dõi hạn sửa chữa)
        JSplitPane splitPane = taoSplitPaneNoiDung();
        root.add(splitPane, BorderLayout.CENTER);

        // Nạp dữ liệu ban đầu từ SQL Server
        napDuLieuAsync();
    }

    // =========================================================================
    // 1. HEADER & KPI CARDS
    // =========================================================================

    private JPanel taoHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        // Tiêu đề
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel tde = UiHelper.tieuDeTrang("Điều hành sửa chữa");
        JLabel phu = new JLabel("Theo dõi hạn hoàn thành sửa chữa & Thống kê khối lượng công việc kỹ thuật viên");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phu.setForeground(new Color(100, 116, 139));

        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(tde);
        text.add(Box.createVerticalStrut(3));
        text.add(phu);
        header.add(text, BorderLayout.CENTER);

        // Thao tác bên phải
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        lblCapNhatLuc = new JLabel("Đang tải dữ liệu...");
        lblCapNhatLuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCapNhatLuc.setForeground(new Color(100, 116, 139));
        right.add(lblCapNhatLuc);

        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 14), UiHelper.XANH_TRUNG, e -> napDuLieuAsync());
        btnLamMoi.setPreferredSize(new Dimension(110, 36));
        btnLamMoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        right.add(btnLamMoi);

        JButton btnMoPhieu = UiHelper.nut("Mở phiếu đang chọn", UiIcon.of(UiIcon.WRENCH, 14), UiHelper.XANH_DAM, e -> moPhieuDangChon());
        btnMoPhieu.setPreferredSize(new Dimension(175, 36));
        btnMoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        right.add(btnMoPhieu);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel taoLuoiKPICards() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);

        lblStatChuaHT = new JLabel("0");
        lblStatHenHomNay = new JLabel("0");
        lblStatQuaHen = new JLabel("0");
        lblStatTongKtv = new JLabel("0");

        // Card 1: Chưa hoàn thành
        grid.add(theKpiNho(
                UiIcon.of(UiIcon.WRENCH, 22, new Color(37, 99, 235)),
                "CHƯA HOÀN THÀNH",
                lblStatChuaHT,
                "Đang sửa & Chờ sửa",
                new Color(37, 99, 235),
                e -> {
                    cboLocHan.setSelectedItem("Tất cả chưa hoàn thành");
                    locDanhSachPhieu();
                }
        ));

        // Card 2: Hạn hôm nay
        grid.add(theKpiNho(
                UiIcon.of(UiIcon.CLOCK, 22, new Color(217, 119, 6)),
                "HẸN HÔM NAY",
                lblStatHenHomNay,
                "Cần giao trong ngày",
                new Color(217, 119, 6),
                e -> {
                    cboLocHan.setSelectedItem("Hẹn hôm nay");
                    locDanhSachPhieu();
                }
        ));

        // Card 3: Quá hẹn
        grid.add(theKpiNho(
                UiIcon.of(UiIcon.CLOCK, 22, new Color(225, 29, 72)),
                "QUÁ HẸN",
                lblStatQuaHen,
                "Cần đôn đốc xử lý",
                new Color(225, 29, 72),
                e -> {
                    cboLocHan.setSelectedItem("Quá hẹn");
                    locDanhSachPhieu();
                }
        ));

        // Card 4: Kỹ thuật viên
        grid.add(theKpiNho(
                UiIcon.of(UiIcon.USERS, 22, new Color(16, 185, 129)),
                "KỸ THUẬT VIÊN",
                lblStatTongKtv,
                "Đang hoạt động trong xưởng",
                new Color(16, 185, 129),
                e -> boLocKtv()
        ));

        return grid;
    }

    private JPanel theKpiNho(Icon icon, String tieuDe, JLabel lblSo, String moTa, Color mauVien, java.awt.event.ActionListener onClick) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                // Vạch chỉ màu trên đầu card
                g2.setColor(mauVien);
                g2.fillRoundRect(0, 0, getWidth(), 4, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon bên trái
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        JLabel lIcon = new JLabel(icon);
        iconWrap.add(lIcon);
        card.add(iconWrap, BorderLayout.WEST);

        // Text & Số liệu ở giữa
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel lTieuDe = new JLabel(tieuDe);
        lTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lTieuDe.setForeground(new Color(100, 116, 139));

        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSo.setForeground(UiHelper.XANH_DAM);

        JLabel lMoTa = new JLabel(moTa);
        lMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lMoTa.setForeground(new Color(148, 163, 184));

        center.add(lTieuDe);
        center.add(Box.createVerticalStrut(2));
        center.add(lblSo);
        center.add(Box.createVerticalStrut(2));
        center.add(lMoTa);
        card.add(center, BorderLayout.CENTER);

        if (onClick != null) {
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.actionPerformed(null);
                }
            });
        }

        return card;
    }

    // =========================================================================
    // 2. NỘI DUNG CHÍNH (SPLIT PANE: KTV & THEO DÕI HẠN)
    // =========================================================================

    private JSplitPane taoSplitPaneNoiDung() {
        JPanel pnlTrai = taoPanelKhoiLuongKtv();
        JPanel pnlPhai = taoPanelTheoDoiPhieu();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlTrai, pnlPhai);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setDividerLocation(380);

        return split;
    }

    // -------------------------------------------------------------------------
    // BẢNG TRÁI: THỐNG KÊ KHỐI LƯỢNG CÔNG VIỆC KỸ THUẬT VIÊN
    // -------------------------------------------------------------------------

    private JPanel taoPanelKhoiLuongKtv() {
        JPanel panel = UiHelper.panelBang("Khối lượng công việc Kỹ thuật viên");
        panel.setPreferredSize(new Dimension(380, 500));

        // Thanh thao tác trên bảng KTV
        JPanel topKtv = new JPanel(new BorderLayout(6, 4));
        topKtv.setOpaque(false);
        topKtv.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblSub = new JLabel("Chọn KTV để lọc danh sách phiếu bên phải:");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(100, 116, 139));
        topKtv.add(lblSub, BorderLayout.WEST);

        btnBoLocKtv = UiHelper.nut("Bỏ lọc KTV", UiIcon.of(UiIcon.REFRESH, 12), UiHelper.XAM, e -> boLocKtv());
        btnBoLocKtv.setPreferredSize(new Dimension(95, 26));
        btnBoLocKtv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnBoLocKtv.setVisible(false);
        topKtv.add(btnBoLocKtv, BorderLayout.EAST);

        panel.add(topKtv, BorderLayout.NORTH);

        // Bảng KTV
        modelKtv = UiHelper.model("Mã KTV", "Họ và tên KTV", "Đang làm", "Quá hẹn");
        tableKtv = UiHelper.bang(modelKtv);
        sorterKtv = new TableRowSorter<>(modelKtv);
        tableKtv.setRowSorter(sorterKtv);

        tableKtv.getColumnModel().getColumn(0).setPreferredWidth(65);
        tableKtv.getColumnModel().getColumn(1).setPreferredWidth(160);
        tableKtv.getColumnModel().getColumn(2).setPreferredWidth(75);
        tableKtv.getColumnModel().getColumn(3).setPreferredWidth(70);

        UiHelper.canGiua(tableKtv, 0, 2, 3);
        UiHelper.canTrai(tableKtv, 1);

        // Renderer cảnh báo màu cho cột Quá hẹn của KTV
        tableKtv.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSel) {
                    try {
                        int count = Integer.parseInt(val != null ? val.toString() : "0");
                        if (count > 0) {
                            l.setForeground(new Color(225, 29, 72));
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                        } else {
                            l.setForeground(new Color(100, 116, 139));
                            l.setFont(l.getFont().deriveFont(Font.PLAIN));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return l;
            }
        });

        // Sự kiện chọn dòng KTV để lọc bảng phiếu
        tableKtv.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableKtv.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = tableKtv.convertRowIndexToModel(selectedRow);
                    maKtvDangLoc = String.valueOf(modelKtv.getValueAt(modelRow, 0));
                    String tenKtv = String.valueOf(modelKtv.getValueAt(modelRow, 1));
                    capNhatTrangThaiLocKtv(maKtvDangLoc, tenKtv);
                } else {
                    maKtvDangLoc = null;
                    capNhatTrangThaiLocKtv(null, null);
                }
                locDanhSachPhieu();
            }
        });

        JScrollPane sp = new JScrollPane(tableKtv);
        UiHelper.danhSachBong(sp);
        panel.add(sp, BorderLayout.CENTER);

        // Chú thích chân trang (Tuân thủ yêu cầu: không gọi là "đánh giá năng suất")
        JPanel botNote = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        botNote.setOpaque(false);
        botNote.setBorder(new EmptyBorder(6, 2, 0, 2));

        JLabel lNote = new JLabel("<html><i style='color:#64748B;'>ℹ Khối lượng công việc phân công điều phối hiện tại; không dùng đánh giá hay xếp hạng nhân viên.</i></html>");
        lNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        botNote.add(lNote);

        panel.add(botNote, BorderLayout.SOUTH);
        return panel;
    }

    // -------------------------------------------------------------------------
    // BẢNG PHẢI: THEO DÕI HẠN SỬA CHỮA
    // -------------------------------------------------------------------------

    private JPanel taoPanelTheoDoiPhieu() {
        JPanel panel = UiHelper.panelBang("Danh sách phiếu sửa chữa theo dõi tiến độ");

        // Thanh công cụ tìm kiếm và bộ lọc hạn
        JPanel filterPanel = new JPanel(new BorderLayout(8, 8));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilters.setOpaque(false);

        // Tìm kiếm từ khóa
        JLabel lblTk = new JLabel("Tìm kiếm:");
        lblTk.setFont(UiHelper.FONT_NHAN);
        lblTk.setForeground(new Color(51, 65, 85));
        leftFilters.add(lblTk);

        txtTuKhoa = new JTextField();
        txtTuKhoa.setPreferredSize(new Dimension(150, 32));
        txtTuKhoa.setToolTipText("Tìm theo mã phiếu, biển số xe, khách hàng, SĐT...");
        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locDanhSachPhieu(); }
            @Override public void removeUpdate(DocumentEvent e) { locDanhSachPhieu(); }
            @Override public void changedUpdate(DocumentEvent e) { locDanhSachPhieu(); }
        });
        leftFilters.add(txtTuKhoa);

        // Bộ lọc hạn
        JLabel lblLoc = new JLabel("Lọc hạn:");
        lblLoc.setFont(UiHelper.FONT_NHAN);
        lblLoc.setForeground(new Color(51, 65, 85));
        leftFilters.add(lblLoc);

        cboLocHan = new JComboBox<>(new String[]{
                "Tất cả chưa hoàn thành",
                "Hẹn hôm nay",
                "Quá hẹn",
                "Tất cả phiếu (kể cả đã xong)"
        });
        cboLocHan.setPreferredSize(new Dimension(175, 32));
        cboLocHan.setFont(UiHelper.FONT_BT);
        cboLocHan.setBackground(Color.WHITE);
        cboLocHan.addActionListener(e -> locDanhSachPhieu());
        leftFilters.add(cboLocHan);

        // Nhãn trạng thái đang lọc KTV
        lblThongBaoLocKtv = new JLabel("");
        lblThongBaoLocKtv.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblThongBaoLocKtv.setForeground(new Color(37, 99, 235));
        leftFilters.add(lblThongBaoLocKtv);

        filterPanel.add(leftFilters, BorderLayout.CENTER);

        // Nút xem tiến độ / cập nhật
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightActions.setOpaque(false);

        JButton btnTienDo = UiHelper.nut("Xem tiến độ xưởng", UiIcon.of(UiIcon.CHECK, 13), UiHelper.XAM, e -> xemTienDoXuong());
        btnTienDo.setPreferredSize(new Dimension(135, 32));
        btnTienDo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rightActions.add(btnTienDo);

        filterPanel.add(rightActions, BorderLayout.EAST);
        panel.add(filterPanel, BorderLayout.NORTH);

        // Bảng Phiếu sửa chữa
        modelPhieu = UiHelper.model(
                "Mã phiếu", "Biển số xe", "Tên khách hàng", "KTV phụ trách",
                "Ngày hẹn hoàn thành", "Trạng thái", "Tình trạng hạn"
        );
        tablePhieu = UiHelper.bang(modelPhieu);

        tablePhieu.getColumnModel().getColumn(0).setPreferredWidth(85);
        tablePhieu.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablePhieu.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablePhieu.getColumnModel().getColumn(3).setPreferredWidth(130);
        tablePhieu.getColumnModel().getColumn(4).setPreferredWidth(125);
        tablePhieu.getColumnModel().getColumn(5).setPreferredWidth(110);
        tablePhieu.getColumnModel().getColumn(6).setPreferredWidth(115);

        UiHelper.canGiua(tablePhieu, 0, 1, 4, 5, 6);
        UiHelper.canTrai(tablePhieu, 2, 3);

        // Cài đặt CellRenderer đặc biệt: Tô màu nhẹ phân biệt phiếu quá hẹn & định dạng hạn
        tablePhieu.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                String maPSC = String.valueOf(table.getValueAt(row, 0));
                PhieuDieuHanhDTO dto = timPhieuTrongDsGoc(maPSC);

                if (!isSelected) {
                    if (dto != null && dto.isQuaHen()) {
                        // Tô màu nền hồng/đỏ nhạt cho phiếu quá hẹn
                        l.setBackground(new Color(254, 242, 242));
                    } else if (dto != null && dto.isHenHomNay() && dto.isChuaHoanThanh()) {
                        // Tô màu nền vàng/cam nhạt cho phiếu hẹn hôm nay
                        l.setBackground(new Color(254, 243, 199));
                    } else {
                        l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }

                    // Tùy chỉnh màu chữ theo cột
                    if (col == 6) { // Cột tình trạng hạn
                        if (dto != null && dto.isQuaHen()) {
                            l.setForeground(new Color(220, 38, 38)); // Đỏ đậm
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                        } else if (dto != null && dto.isHenHomNay() && dto.isChuaHoanThanh()) {
                            l.setForeground(new Color(180, 83, 9));  // Cam đậm
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                        } else if (dto != null && dto.isHoanThanh()) {
                            l.setForeground(new Color(22, 163, 74));  // Xanh lá
                            l.setFont(l.getFont().deriveFont(Font.PLAIN));
                        } else {
                            l.setForeground(new Color(100, 116, 139));
                            l.setFont(l.getFont().deriveFont(Font.PLAIN));
                        }
                    } else if (col == 4) { // Cột ngày hẹn
                        if (dto != null && dto.getNgayHoanThanhDuKien() == null) {
                            l.setForeground(new Color(148, 163, 184));
                            l.setFont(l.getFont().deriveFont(Font.ITALIC));
                        } else {
                            l.setForeground(new Color(51, 65, 85));
                            l.setFont(l.getFont().deriveFont(Font.PLAIN));
                        }
                    } else if (col == 5) { // Cột trạng thái
                        if (dto != null && dto.isHoanThanh()) {
                            l.setForeground(new Color(22, 163, 74));
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                        } else if (dto != null && dto.getTrangThai().contains("DANG_SUA")) {
                            l.setForeground(new Color(37, 99, 235));
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                        } else {
                            l.setForeground(new Color(71, 85, 105));
                            l.setFont(l.getFont().deriveFont(Font.PLAIN));
                        }
                    } else {
                        l.setForeground(new Color(30, 41, 59));
                        l.setFont(l.getFont().deriveFont(Font.PLAIN));
                    }
                }

                // Căn lề theo cột
                if (col == 0 || col == 1 || col == 4 || col == 5 || col == 6) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                }

                return l;
            }
        });

        // Double click mở phiếu sửa chữa
        tablePhieu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tablePhieu.getSelectedRow() >= 0) {
                    moPhieuDangChon();
                }
            }
        });

        JScrollPane spPhieu = new JScrollPane(tablePhieu);
        UiHelper.danhSachBong(spPhieu);
        panel.add(spPhieu, BorderLayout.CENTER);

        // Thanh trạng thái & đếm kết quả ở chân bảng
        JPanel botPhieu = new JPanel(new BorderLayout());
        botPhieu.setOpaque(false);
        botPhieu.setBorder(new EmptyBorder(8, 4, 4, 4));

        lblDemPhieu = new JLabel("Đang tải dữ liệu...");
        lblDemPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDemPhieu.setForeground(new Color(71, 85, 105));
        botPhieu.add(lblDemPhieu, BorderLayout.WEST);

        panel.add(botPhieu, BorderLayout.SOUTH);
        return panel;
    }

    // =========================================================================
    // 3. LOGIC NẠP DỮ LIỆU & BỘ LỌC
    // =========================================================================

    /**
     * Nạp dữ liệu bất đồng bộ qua SwingWorker để không làm đơ giao diện
     */
    public void napDuLieuAsync() {
        lblCapNhatLuc.setText("Đang tải dữ liệu...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private ArrayList<PhieuDieuHanhDTO> listPhieu;
            private ArrayList<ThongKeKtvDTO> listKtv;
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    listPhieu = dao.layDanhSachTheoDoi();
                    listKtv = dao.layThongKeKyThuatVien();
                } catch (Exception ex) {
                    this.error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                if (error != null) {
                    lblCapNhatLuc.setText("Lỗi kết nối CSDL!");
                    lblDemPhieu.setText("Không thể tải dữ liệu.");
                    JOptionPane.showMessageDialog(
                            DieuHanhSuaChua_UI.this,
                            "Lỗi tải dữ liệu điều hành sửa chữa:\n" + error.getMessage(),
                            "Lỗi kết nối CSDL",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                dsPhieuGoc.clear();
                if (listPhieu != null) dsPhieuGoc.addAll(listPhieu);

                dsKtvGoc.clear();
                if (listKtv != null) dsKtvGoc.addAll(listKtv);

                // Cập nhật các bảng & chỉ số
                capNhatKpiCards();
                napBangKtv();
                locDanhSachPhieu();

                String gio = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                lblCapNhatLuc.setText("Cập nhật lúc: " + gio);
            }
        };
        worker.execute();
    }

    private void capNhatKpiCards() {
        int demChuaHT = 0;
        int demHenHomNay = 0;
        int demQuaHen = 0;

        for (PhieuDieuHanhDTO p : dsPhieuGoc) {
            if (p.isChuaHoanThanh()) {
                demChuaHT++;
                if (p.isQuaHen()) {
                    demQuaHen++;
                } else if (p.isHenHomNay()) {
                    demHenHomNay++;
                }
            }
        }

        lblStatChuaHT.setText(String.valueOf(demChuaHT));
        lblStatHenHomNay.setText(String.valueOf(demHenHomNay));
        lblStatQuaHen.setText(String.valueOf(demQuaHen));
        lblStatTongKtv.setText(String.valueOf(dsKtvGoc.size()));
    }

    private void napBangKtv() {
        modelKtv.setRowCount(0);
        for (ThongKeKtvDTO k : dsKtvGoc) {
            modelKtv.addRow(new Object[]{
                    k.getMaKTV(),
                    k.getTenKTV(),
                    k.getSoPhieuDangPhuTrach(),
                    k.getSoPhieuQuaHen()
            });
        }
    }

    /**
     * Lọc danh sách phiếu theo từ khóa, bộ lọc hạn và KTV đang chọn
     */
    private void locDanhSachPhieu() {
        String q = txtTuKhoa != null ? txtTuKhoa.getText().trim().toLowerCase() : "";
        String locHan = cboLocHan != null ? String.valueOf(cboLocHan.getSelectedItem()) : "Tất cả chưa hoàn thành";

        modelPhieu.setRowCount(0);
        int demHienThi = 0;
        int demQuaHenHienThi = 0;

        for (PhieuDieuHanhDTO p : dsPhieuGoc) {
            // 1. Lọc theo KTV (nếu đang chọn một KTV cụ thể)
            if (maKtvDangLoc != null && !maKtvDangLoc.isBlank()) {
                if (p.getMaKTV() == null || !p.getMaKTV().equalsIgnoreCase(maKtvDangLoc)) {
                    continue;
                }
            }

            // 2. Lọc theo hạn / trạng thái
            if ("Tất cả chưa hoàn thành".equals(locHan)) {
                if (!p.isChuaHoanThanh()) continue;
            } else if ("Hẹn hôm nay".equals(locHan)) {
                if (!p.isChuaHoanThanh() || !p.isHenHomNay()) continue;
            } else if ("Quá hẹn".equals(locHan)) {
                if (!p.isQuaHen()) continue;
            }
            // "Tất cả phiếu (kể cả đã xong)" -> không bỏ qua

            // 3. Tìm kiếm theo từ khóa (Mã phiếu, Biển số, Khách hàng, KTV, SĐT)
            if (!q.isEmpty()) {
                String ma = p.getMaPSC() != null ? p.getMaPSC().toLowerCase() : "";
                String bs = p.getBienSoXe() != null ? p.getBienSoXe().toLowerCase() : "";
                String kh = p.getTenKhachHang() != null ? p.getTenKhachHang().toLowerCase() : "";
                String ktv = p.getTenKTV() != null ? p.getTenKTV().toLowerCase() : "";
                String sdt = p.getSoDT() != null ? p.getSoDT().toLowerCase() : "";

                boolean match = ma.contains(q) || bs.contains(q) || kh.contains(q) || ktv.contains(q) || sdt.contains(q);
                if (!match) continue;
            }

            // Đưa vào bảng
            demHienThi++;
            if (p.isQuaHen()) demQuaHenHienThi++;

            modelPhieu.addRow(new Object[]{
                    p.getMaPSC(),
                    p.getBienSoXe(),
                    p.getTenKhachHang(),
                    p.getTenKTV(),
                    p.getNgayHenHienThi(),
                    p.getTrangThaiHienThi(),
                    p.getSoNgayQuaHenHienThi()
            });
        }

        String thongKeText = "Hiển thị: " + demHienThi + " / " + dsPhieuGoc.size() + " phiếu sửa chữa";
        if (demQuaHenHienThi > 0) {
            thongKeText += "  •  <font color='#DC2626'><b>" + demQuaHenHienThi + " phiếu quá hẹn</b></font>";
        }
        if (maKtvDangLoc != null) {
            thongKeText += "  •  Lọc theo: <b>" + maKtvDangLoc + "</b>";
        }
        lblDemPhieu.setText("<html>" + thongKeText + "</html>");
    }

    private void capNhatTrangThaiLocKtv(String maKtv, String tenKtv) {
        if (maKtv != null && !maKtv.isBlank()) {
            lblThongBaoLocKtv.setText("  [Lọc KTV: " + maKtv + " - " + tenKtv + "]");
            btnBoLocKtv.setVisible(true);
        } else {
            lblThongBaoLocKtv.setText("");
            btnBoLocKtv.setVisible(false);
        }
    }

    private void boLocKtv() {
        tableKtv.clearSelection();
        maKtvDangLoc = null;
        capNhatTrangThaiLocKtv(null, null);
        locDanhSachPhieu();
    }

    private PhieuDieuHanhDTO timPhieuTrongDsGoc(String maPSC) {
        if (maPSC == null) return null;
        for (PhieuDieuHanhDTO p : dsPhieuGoc) {
            if (maPSC.equalsIgnoreCase(p.getMaPSC())) {
                return p;
            }
        }
        return null;
    }

    // =========================================================================
    // 4. ĐIỀU HƯỚNG TỚI MÀN HÌNH TIẾN ĐỘ / LẬP PHIẾU
    // =========================================================================

    private void moPhieuDangChon() {
        int row = tablePhieu.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một phiếu sửa chữa trong bảng để mở chi tiết!",
                    "Chưa chọn phiếu",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String maPSC = String.valueOf(modelPhieu.getValueAt(row, 0));
        PhieuDieuHanhDTO p = timPhieuTrongDsGoc(maPSC);

        // Nạp mã phiếu cần mở vào LapPhieuSuaChua_UI
        LapPhieuSuaChua_UI.datPSCCanMoLai(maPSC);

        Main_UI ui = mainUI != null ? mainUI : Main_UI.getInstance();
        if (ui != null) {
            ui.chuyenTrang("Lập phiếu sửa chữa");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Đã chọn mở phiếu: " + maPSC + (p != null ? (" (" + p.getBienSoXe() + ")") : ""),
                    "Mở phiếu sửa chữa",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void xemTienDoXuong() {
        Main_UI ui = mainUI != null ? mainUI : Main_UI.getInstance();
        if (ui != null) {
            ui.chuyenTrang("Cập nhật tiến độ");
        } else {
            JOptionPane.showMessageDialog(this, "Chuyển tới màn hình: Cập nhật tiến độ");
        }
    }
}
