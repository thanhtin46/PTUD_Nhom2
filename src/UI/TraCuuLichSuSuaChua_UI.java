package UI;

import DAO.TraCuuLichSuSuaChua_DAO;
import DAO.TraCuuLichSuSuaChua_DAO.ChiTietBaoHanhDTO;
import DAO.TraCuuLichSuSuaChua_DAO.ChiTietCongViecDTO;
import DAO.TraCuuLichSuSuaChua_DAO.ChiTietPhuTungDTO;
import DAO.TraCuuLichSuSuaChua_DAO.LichSuRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Màn hình tra cứu lịch sử sửa chữa của khách hàng và phương tiện.
 */
public class TraCuuLichSuSuaChua_UI extends JPanel {

    private final TraCuuLichSuSuaChua_DAO dao = new TraCuuLichSuSuaChua_DAO();

    // ==== BỘ LỌC TÌM KIẾM ====
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí (Tự động nhận diện)",
            "Tên khách hàng",
            "Số điện thoại",
            "Biển số xe",
            "Mã phiếu tiếp nhận (TN...)",
            "Mã phiếu sửa chữa (SC...)",
            "Mã khách hàng / CCCD"
    });
    private final JComboBox<String> cboThoiGian = new JComboBox<>(new String[]{
            "Tất cả thời gian", "Hôm nay", "Tháng này", "Năm nay"
    });

    private String locTrangThai = "TAT_CA";
    private String locThanhToan = "TAT_CA";
    private String locThoiGian = "TAT_CA";

    private JButton chipTatCaTT, chipHoanThanh, chipDangSua, chipChoSua, chipChoPT, chipChoTN;
    private JButton chipTatCaPay, chipDaThanhToan, chipChuaThanhToan;

    private final JLabel lblGoiY = new JLabel();
    private final JLabel lblDemKetQua = new JLabel("Đang tải dữ liệu...");

    // ==== THẺ THỐNG KÊ KPI ====
    private final JLabel lblKpiTongLuot = new JLabel("0");
    private final JLabel lblKpiHoanThanh = new JLabel("0");
    private final JLabel lblKpiDangXuLy = new JLabel("0");
    private final JLabel lblKpiTongTien = new JLabel("0 đ");

    // ==== BẢNG MASTER (LỊCH SỬ SỬA CHỮA) ====
    private DefaultTableModel modelMaster;
    private JTable tblMaster;
    private ArrayList<LichSuRecord> dsHienTai = new ArrayList<>();
    private LichSuRecord banGhiDangChon = null;

    // ==== PANEL DETAIL (TABBED PANE CHI TIẾT) ====
    private JLabel lblTieuDeDetail;
    private DefaultTableModel modelCongViec;
    private JTable tblCongViec;
    private JLabel lblTongTienCong;

    private DefaultTableModel modelPhuTung;
    private JTable tblPhuTung;
    private JLabel lblTongTienPhuTung;

    private JPanel pnlHoaDonVaBaoHanh;

    public TraCuuLichSuSuaChua_UI() {
        this("", 0);
    }

    public TraCuuLichSuSuaChua_UI(String tuKhoaKhoiTao, int tieuChiKhoiTao) {
        initUI();
        if (tuKhoaKhoiTao != null && !tuKhoaKhoiTao.trim().isEmpty()) {
            txtTuKhoa.setText(tuKhoaKhoiTao.trim());
            if (tieuChiKhoiTao >= 0 && tieuChiKhoiTao < cboTieuChi.getItemCount()) {
                cboTieuChi.setSelectedIndex(tieuChiKhoiTao);
            }
        }
        napBang();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        root.setLayout(new BorderLayout(0, 14));
        add(root, BorderLayout.CENTER);

        // 1. HEADER & KPI CARDS
        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setOpaque(false);

        topBox.add(taoHeader());
        topBox.add(Box.createVerticalStrut(10));
        topBox.add(taoTheKpi());
        topBox.add(Box.createVerticalStrut(12));

        root.add(topBox, BorderLayout.NORTH);

        // 2. KHU VỰC CHÍNH (TÌM KIẾM + BẢNG MASTER + CHI TIẾT)
        JPanel centerPanel = UiHelper.panelBang("Lịch sử sửa chữa & bảo dưỡng của khách hàng");
        centerPanel.setLayout(new BorderLayout(0, 10));

        // Khung tìm kiếm trên đầu bảng
        centerPanel.add(taoKhungTimKiem(), BorderLayout.NORTH);

        // SplitPane giữa Bảng Master và Bảng Detail
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(8);
splitPane.setDividerLocation(230);

        // Bảng Master
        JPanel pnlMasterWrap = new JPanel(new BorderLayout());
        pnlMasterWrap.setOpaque(false);

        modelMaster = UiHelper.model(
                "Mã TN", "Mã SC", "Ngày tiếp nhận", "Khách hàng", "Số ĐT",
                "Biển số xe", "Hãng / Dòng xe", "Yêu cầu sửa chữa", "KTV phụ trách",
                "Tiến độ sửa chữa", "Tiền công", "Tiền phụ tùng", "Tổng chi phí", "Thanh toán", "Mã HĐ"
        );
        tblMaster = UiHelper.bang(modelMaster);
        tblMaster.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        dinhDangCotMaster(0, SwingConstants.CENTER, 85);   // Mã TN
        dinhDangCotMaster(1, SwingConstants.CENTER, 85);   // Mã SC
        dinhDangCotMaster(2, SwingConstants.CENTER, 125);  // Ngày tiếp nhận
        dinhDangCotMaster(3, SwingConstants.LEFT, 150);    // Khách hàng
        dinhDangCotMaster(4, SwingConstants.CENTER, 105);  // Số ĐT
        dinhDangCotMaster(5, SwingConstants.CENTER, 105);  // Biển số xe
        dinhDangCotMaster(6, SwingConstants.LEFT, 130);    // Hãng / Dòng xe
        dinhDangCotMaster(7, SwingConstants.LEFT, 180);    // Yêu cầu sửa chữa
        dinhDangCotMaster(8, SwingConstants.LEFT, 135);    // KTV phụ trách
        dinhDangCotMaster(9, SwingConstants.CENTER, 120);  // Tiến độ sửa chữa (Badge)
        dinhDangCotMaster(10, SwingConstants.RIGHT, 100);  // Tiền công
        dinhDangCotMaster(11, SwingConstants.RIGHT, 105);  // Tiền phụ tùng
        dinhDangCotMaster(12, SwingConstants.RIGHT, 115);  // Tổng chi phí
        dinhDangCotMaster(13, SwingConstants.CENTER, 120); // Thanh toán (Badge)
        dinhDangCotMaster(14, SwingConstants.CENTER, 85);   // Mã HĐ

        tblMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                chonDongMaster();
            }
        });

        tblMaster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    xemHoSoDayDu();
                }
            }
        });

        JScrollPane spMaster = new JScrollPane(tblMaster);
        spMaster.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        spMaster.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(spMaster);
        spMaster.setPreferredSize(new Dimension(900, 230));
spMaster.setMinimumSize(new Dimension(900, 160));
pnlMasterWrap.add(spMaster, BorderLayout.CENTER);

        splitPane.setTopComponent(pnlMasterWrap);

        // Panel Detail bên dưới
        JPanel pnlDetailWrap = taoPanelDetail();
pnlDetailWrap.setPreferredSize(new Dimension(900, 320));
pnlDetailWrap.setMinimumSize(new Dimension(900, 220));
        splitPane.setBottomComponent(pnlDetailWrap);

        centerPanel.add(splitPane, BorderLayout.CENTER);

        // 3. FOOTER THAO TÁC
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 4, 4));

        lblDemKetQua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDemKetQua.setForeground(new Color(71, 85, 105));
        footer.add(lblDemKetQua, BorderLayout.WEST);

        JPanel boxNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boxNut.setOpaque(false);

        JButton btnXemHoSo = UiHelper.nut("Xem hồ sơ đầy đủ", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> xemHoSoDayDu());
        JButton btnInPhieu = UiHelper.nut("In phiếu tóm tắt", UiIcon.of(UiIcon.PRINT, 15), new Color(14, 116, 144), e -> inPhieuTomTat());
        JButton btnTiepNhanMoi = UiHelper.nut("Lập phiếu tiếp nhận mới", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> lapPhieuTiepNhanMoi());
        JButton btnLamMoi = UiHelper.nut("Làm mới dữ liệu", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, e -> lamMoi());

        boxNut.add(btnXemHoSo);
        boxNut.add(btnInPhieu);
        boxNut.add(btnTiepNhanMoi);
        boxNut.add(btnLamMoi);
        footer.add(boxNut, BorderLayout.EAST);

        centerPanel.add(footer, BorderLayout.SOUTH);
        root.add(centerPanel, BorderLayout.CENTER);

        capNhatGoiY();
    }

    // =========================================================================
    //                            HEADER & KPI CARDS
    // =========================================================================
    private JPanel taoHeader() {
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        JLabel tde = UiHelper.tieuDeTrang("Tra cứu lịch sử sửa chữa của khách hàng");
        JLabel phu = new JLabel("Tra cứu lịch sử tiếp nhận, sửa chữa, phụ tùng và bảo hành theo khách hàng hoặc phương tiện");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));

        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);

        north.add(tde);
        north.add(Box.createVerticalStrut(4));
        north.add(phu);
        return north;
    }

    private JPanel taoTheKpi() {
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        kpiGrid.setPreferredSize(new Dimension(1000, 74));

        kpiGrid.add(taoTheKpiItem("TỔNG LƯỢT SỬA CHỮA", lblKpiTongLuot, UiIcon.CLOCK, new Color(2, 132, 199), new Color(224, 242, 254), new Color(186, 230, 253)));
        kpiGrid.add(taoTheKpiItem("ĐÃ HOÀN THÀNH", lblKpiHoanThanh, UiIcon.CHECK, new Color(22, 163, 74), new Color(220, 252, 231), new Color(187, 247, 208)));
        kpiGrid.add(taoTheKpiItem("ĐANG TRONG XƯỞNG", lblKpiDangXuLy, UiIcon.WRENCH, new Color(217, 119, 6), new Color(254, 243, 199), new Color(253, 230, 138)));
        kpiGrid.add(taoTheKpiItem("TỔNG CHI PHÍ PHÁT SINH", lblKpiTongTien, UiIcon.BILL, new Color(124, 58, 237), new Color(237, 233, 254), new Color(221, 214, 254)));

        return kpiGrid;
    }

    private JPanel taoTheKpiItem(String tieuDe, JLabel lblGiaTri, String iconType, Color mauChu, Color mauNen, Color mauVien) {
        JPanel the = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauNen);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(mauVien);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        the.setOpaque(false);
        the.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Icon tròn bên trái
        JPanel pnlIcon = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(mauVien);
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        pnlIcon.setOpaque(false);
        pnlIcon.setPreferredSize(new Dimension(38, 38));
        pnlIcon.add(new JLabel(UiIcon.of(iconType, 18, mauChu)));
        the.add(pnlIcon, BorderLayout.WEST);

        // Thông tin text bên phải
        JPanel pnlText = new JPanel();
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
        pnlText.setOpaque(false);

        JLabel lblTitle = new JLabel(tieuDe);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(100, 116, 139));

        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblGiaTri.setForeground(mauChu);

        pnlText.add(lblTitle);
        pnlText.add(Box.createVerticalStrut(2));
        pnlText.add(lblGiaTri);
        the.add(pnlText, BorderLayout.CENTER);

        return the;
    }

    // =========================================================================
    //                            KHUNG TÌM KIẾM & BỘ LỌC
    // =========================================================================
    private JPanel taoKhungTimKiem() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Hàng 1: Tiêu chí lọc + Ô nhập + Thời gian + Nút tìm kiếm & Làm mới
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setOpaque(false);

        JPanel pnlTieuChi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlTieuChi.setOpaque(false);
        JLabel lblTieuChi = new JLabel("Tìm theo:");
        lblTieuChi.setFont(UiHelper.FONT_NHAN);
        lblTieuChi.setForeground(new Color(51, 65, 85));
        cboTieuChi.setPreferredSize(new Dimension(245, 38));
        cboTieuChi.setFont(UiHelper.FONT_BT);
        cboTieuChi.setBackground(Color.WHITE);
        cboTieuChi.addActionListener(e -> {
            capNhatGoiY();
            napBang();
        });
        pnlTieuChi.add(lblTieuChi);
        pnlTieuChi.add(cboTieuChi);
        row1.add(pnlTieuChi, BorderLayout.WEST);

        txtTuKhoa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTuKhoa.setPreferredSize(new Dimension(340, 38));
        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { napBang(); }
            @Override public void removeUpdate(DocumentEvent e) { napBang(); }
            @Override public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        row1.add(txtTuKhoa, BorderLayout.CENTER);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRight.setOpaque(false);

        JLabel lblTg = new JLabel("Thời gian:");
        lblTg.setFont(UiHelper.FONT_NHAN);
        lblTg.setForeground(new Color(51, 65, 85));
        cboThoiGian.setPreferredSize(new Dimension(135, 38));
        cboThoiGian.setFont(UiHelper.FONT_BT);
        cboThoiGian.setBackground(Color.WHITE);
        cboThoiGian.addActionListener(e -> {
            int idx = cboThoiGian.getSelectedIndex();
            locThoiGian = switch (idx) {
                case 1 -> "HOM_NAY";
                case 2 -> "THANG_NAY";
                case 3 -> "NAM_NAY";
                default -> "TAT_CA";
            };
            napBang();
        });

        JButton btnTimKiem = UiHelper.nut("Tìm kiếm", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> napBang());
        JButton btnLamMoiLoc = UiHelper.nut("Xóa lọc", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, e -> lamMoi());

        pnlRight.add(lblTg);
        pnlRight.add(cboThoiGian);
        pnlRight.add(btnTimKiem);
        pnlRight.add(btnLamMoiLoc);
        row1.add(pnlRight, BorderLayout.EAST);

        p.add(row1);
        p.add(Box.createVerticalStrut(8));

        // Hàng 2: Bộ lọc Chips (Tiến độ & Thanh toán)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);

        // Chips Tiến độ
        JLabel lblTd = new JLabel("Tiến độ:");
        lblTd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTd.setForeground(new Color(100, 116, 139));
        row2.add(lblTd);

        chipTatCaTT = taoChip("Tất cả", "TAT_CA", true);
        chipHoanThanh = taoChip("Hoàn thành", "HOAN_THANH", true);
        chipDangSua = taoChip("Đang sửa", "DANG_SUA", true);
        chipChoSua = taoChip("Chờ sửa", "CHO_SUA", true);
        chipChoPT = taoChip("Chờ phụ tùng", "CHO_PHU_TUNG", true);
        chipChoTN = taoChip("Chờ tiếp nhận", "CHO_TIEP_NHAN", true);

        row2.add(chipTatCaTT);
        row2.add(chipHoanThanh);
        row2.add(chipDangSua);
        row2.add(chipChoSua);
        row2.add(chipChoPT);
        row2.add(chipChoTN);

        row2.add(Box.createHorizontalStrut(12));

        // Chips Thanh toán
        JLabel lblTt = new JLabel("Thanh toán:");
        lblTt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTt.setForeground(new Color(100, 116, 139));
        row2.add(lblTt);

        chipTatCaPay = taoChip("Tất cả", "TAT_CA", false);
        chipDaThanhToan = taoChip("Đã thanh toán", "DA_THANH_TOAN", false);
        chipChuaThanhToan = taoChip("Chưa thanh toán", "CHUA_THANH_TOAN", false);

        row2.add(chipTatCaPay);
        row2.add(chipDaThanhToan);
        row2.add(chipChuaThanhToan);

        p.add(row2);
        p.add(Box.createVerticalStrut(8));

        // Hàng 3: Gợi ý tìm kiếm
        JPanel hintBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        hintBox.setOpaque(true);
        hintBox.setBackground(new Color(239, 246, 255));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254)),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel iconHint = new JLabel(UiIcon.of(UiIcon.SEARCH, 14, new Color(30, 64, 175)));
        lblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGoiY.setForeground(new Color(30, 64, 175));

        hintBox.add(iconHint);
        hintBox.add(lblGoiY);
        p.add(hintBox);

        capNhatGiaoDienChip();
        return p;
    }

    private JButton taoChip(String text, String value, boolean laLocTienDo) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.BLACK);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        btn.addActionListener(e -> {
            if (laLocTienDo) {
                locTrangThai = value;
            } else {
                locThanhToan = value;
            }
            capNhatGiaoDienChip();
            napBang();
        });
        return btn;
    }

    private void capNhatGiaoDienChip() {
        setChipActive(chipTatCaTT, "TAT_CA".equalsIgnoreCase(locTrangThai));
        setChipActive(chipHoanThanh, "HOAN_THANH".equalsIgnoreCase(locTrangThai));
        setChipActive(chipDangSua, "DANG_SUA".equalsIgnoreCase(locTrangThai));
        setChipActive(chipChoSua, "CHO_SUA".equalsIgnoreCase(locTrangThai));
        setChipActive(chipChoPT, "CHO_PHU_TUNG".equalsIgnoreCase(locTrangThai));
        setChipActive(chipChoTN, "CHO_TIEP_NHAN".equalsIgnoreCase(locTrangThai));

        setChipActive(chipTatCaPay, "TAT_CA".equalsIgnoreCase(locThanhToan));
        setChipActive(chipDaThanhToan, "DA_THANH_TOAN".equalsIgnoreCase(locThanhToan));
        setChipActive(chipChuaThanhToan, "CHUA_THANH_TOAN".equalsIgnoreCase(locThanhToan));
    }

    private void setChipActive(JButton btn, boolean active) {
        if (btn == null) return;
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        btn.setBackground(active ? new Color(239, 246, 255) : Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? new Color(59, 130, 246) : new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
    }

    private void capNhatGoiY() {
        String msg = switch (cboTieuChi.getSelectedIndex()) {
            case 1 -> "Nhập tên khách hàng (VD: Nguyễn Minh Quân, Trần Ngọc Mai...) để xem toàn bộ các lần vào xưởng của khách.";
            case 2 -> "Nhập số điện thoại khách hàng (VD: 0912000001) để tra cứu lịch sử sửa chữa chính xác.";
            case 3 -> "Nhập biển số xe (VD: 51A-12345, 59A-67890) để xem toàn bộ lịch sử kỹ thuật của xe.";
            case 4 -> "Nhập mã phiếu tiếp nhận (VD: TN001, TN101...) để định vị hồ sơ tiếp nhận.";
            case 5 -> "Nhập mã phiếu sửa chữa (VD: SC001, SC101...) để kiểm tra nhật ký thi công.";
            case 6 -> "Nhập mã khách hàng (KH001) hoặc số CCCD/CMND để tra cứu hồ sơ khách hàng.";
            default -> "Tìm nhanh tự động: Gõ tên khách, SĐT, biển số xe hoặc mã phiếu để xem toàn bộ lịch sử sửa chữa.";
        };
        lblGoiY.setText(msg);
        txtTuKhoa.setToolTipText(msg);
    }

    // =========================================================================
    //                            PANEL DETAIL (TABS DƯỚI)
    // =========================================================================
    private JPanel taoPanelDetail() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(6, 0, 0, 0));

        // Tiêu đề dòng chi tiết
        JPanel pnlHeaderDetail = new JPanel(new BorderLayout());
        pnlHeaderDetail.setOpaque(false);

        lblTieuDeDetail = new JLabel("Chi tiết lần sửa chữa: (Vui lòng chọn 1 dòng ở bảng trên để xem chi tiết)");
        lblTieuDeDetail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTieuDeDetail.setForeground(UiHelper.XANH_TRUNG);
        pnlHeaderDetail.add(lblTieuDeDetail, BorderLayout.WEST);

        pnl.add(pnlHeaderDetail, BorderLayout.NORTH);

        // TabbedPane 3 Tab
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(900, 270));
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: Công việc sửa chữa
        JPanel tab1 = new JPanel(new BorderLayout(0, 6));
        tab1.setBackground(Color.WHITE);
        tab1.setBorder(new EmptyBorder(8, 8, 8, 8));

        modelCongViec = UiHelper.model("STT", "Nội dung công việc sửa chữa", "KTV thực hiện", "Tiền công");
        tblCongViec = UiHelper.bang(modelCongViec);
        dinhDangCotCon(tblCongViec, 0, SwingConstants.CENTER, 50);
        dinhDangCotCon(tblCongViec, 1, SwingConstants.LEFT, 450);
        dinhDangCotCon(tblCongViec, 2, SwingConstants.LEFT, 180);
        dinhDangCotCon(tblCongViec, 3, SwingConstants.RIGHT, 140);

        tblCongViec.setPreferredScrollableViewportSize(new Dimension(800, 140));
        JScrollPane spCongViec = new JScrollPane(tblCongViec);
        spCongViec.setPreferredSize(new Dimension(800, 140));
        spCongViec.getViewport().setBackground(Color.WHITE);
        tab1.add(spCongViec, BorderLayout.CENTER);

        JPanel pnlFoot1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFoot1.setOpaque(false);
        lblTongTienCong = new JLabel("Tổng tiền công: 0 đ");
        lblTongTienCong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongTienCong.setForeground(new Color(37, 99, 235));
        pnlFoot1.add(lblTongTienCong);
        tab1.add(pnlFoot1, BorderLayout.SOUTH);

        tabs.addTab("  1. Công việc sửa chữa & Tiền công  ", UiIcon.of(UiIcon.WRENCH, 14), tab1);

        // Tab 2: Phụ tùng thay thế
        JPanel tab2 = new JPanel(new BorderLayout(0, 6));
        tab2.setBackground(Color.WHITE);
        tab2.setBorder(new EmptyBorder(8, 8, 8, 8));

        modelPhuTung = UiHelper.model("STT", "Mã PT", "Tên phụ tùng thay thế", "ĐVT", "Số lượng", "Đơn giá", "Thành tiền", "Tình trạng");
        tblPhuTung = UiHelper.bang(modelPhuTung);
        dinhDangCotCon(tblPhuTung, 0, SwingConstants.CENTER, 50);
        dinhDangCotCon(tblPhuTung, 1, SwingConstants.CENTER, 90);
        dinhDangCotCon(tblPhuTung, 2, SwingConstants.LEFT, 320);
        dinhDangCotCon(tblPhuTung, 3, SwingConstants.CENTER, 70);
        dinhDangCotCon(tblPhuTung, 4, SwingConstants.CENTER, 70);
        dinhDangCotCon(tblPhuTung, 5, SwingConstants.RIGHT, 120);
        dinhDangCotCon(tblPhuTung, 6, SwingConstants.RIGHT, 130);
        dinhDangCotCon(tblPhuTung, 7, SwingConstants.CENTER, 110);

        tblPhuTung.setPreferredScrollableViewportSize(new Dimension(800, 140));
        JScrollPane spPhuTung = new JScrollPane(tblPhuTung);
        spPhuTung.setPreferredSize(new Dimension(800, 140));
        spPhuTung.getViewport().setBackground(Color.WHITE);
        tab2.add(spPhuTung, BorderLayout.CENTER);

        JPanel pnlFoot2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFoot2.setOpaque(false);
        lblTongTienPhuTung = new JLabel("Tổng tiền phụ tùng: 0 đ");
        lblTongTienPhuTung.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongTienPhuTung.setForeground(new Color(245, 158, 11));
        pnlFoot2.add(lblTongTienPhuTung);
        tab2.add(pnlFoot2, BorderLayout.SOUTH);

        tabs.addTab("  2. Phụ tùng & Linh kiện thay thế  ", UiIcon.of(UiIcon.BOX, 14), tab2);

        // Tab 3: Hóa đơn & Bảo hành
        pnlHoaDonVaBaoHanh = new JPanel(new BorderLayout(12, 12));
        pnlHoaDonVaBaoHanh.setBackground(Color.WHITE);
        pnlHoaDonVaBaoHanh.setBorder(new EmptyBorder(12, 16, 12, 16));
        napThongTinHoaDonVaBaoHanh(null, null);

        tabs.addTab("  3. Hóa đơn & Sổ bảo hành  ", UiIcon.of(UiIcon.SHIELD, 14), pnlHoaDonVaBaoHanh);

        pnl.add(tabs, BorderLayout.CENTER);
        return pnl;
    }

    // =========================================================================
    //                        XỬ LÝ NẠP DỮ LIỆU & SỰ KIỆN
    // =========================================================================
    private void napBang() {
        String tuKhoa = txtTuKhoa.getText().trim();
        int tieuChi = cboTieuChi.getSelectedIndex();
        modelMaster.setRowCount(0);

        dsHienTai = dao.traCuuLichSu(tuKhoa, tieuChi, locTrangThai, locThanhToan, locThoiGian);

        long tongTienThucThu = 0;
        int countHoanThanh = 0;
        int countDangXuLy = 0;

        for (LichSuRecord r : dsHienTai) {
            tongTienThucThu += r.getTongTien();
            String tt = r.getTrangThaiSC();
            if (tt.contains("Hoàn thành")) {
                countHoanThanh++;
            } else {
                countDangXuLy++;
            }

            modelMaster.addRow(new Object[]{
                    r.getMaPTN(),
                    r.getMaPSC(),
                    r.getNgayTN(),
                    r.getTenKH(),
                    r.getSoDT(),
                    r.getBienSoXe(),
                    r.getTenXe(),
                    r.getYeuCauSC(),
                    r.getTenKTV(),
                    r.getTrangThaiSC(),
                    UiHelper.tien(r.getTienDichVu()) + " đ",
                    UiHelper.tien(r.getTienPhuTung()) + " đ",
                    UiHelper.tien(r.getTongTien()) + " đ",
                    r.getTrangThaiHD(),
                    r.getMaHD()
            });
        }

        // Cập nhật thẻ KPI
        lblKpiTongLuot.setText(String.valueOf(dsHienTai.size()));
        lblKpiHoanThanh.setText(String.valueOf(countHoanThanh));
        lblKpiDangXuLy.setText(String.valueOf(countDangXuLy));
        lblKpiTongTien.setText(UiHelper.tien(tongTienThucThu) + " đ");

        lblDemKetQua.setText("Hiển thị: " + dsHienTai.size() + " lần sửa chữa / tiếp nhận trong hệ thống");

        // Tự động chọn dòng đầu tiên nếu có dữ liệu
        if (!dsHienTai.isEmpty()) {
            tblMaster.setRowSelectionInterval(0, 0);
        } else {
            banGhiDangChon = null;
            lblTieuDeDetail.setText("Chi tiết lần sửa chữa: (Không có dữ liệu phù hợp)");
            modelCongViec.setRowCount(0);
            modelPhuTung.setRowCount(0);
            lblTongTienCong.setText("Tổng tiền công: 0 đ");
            lblTongTienPhuTung.setText("Tổng tiền phụ tùng: 0 đ");
            napThongTinHoaDonVaBaoHanh(null, null);
        }
    }

    private void chonDongMaster() {
        int r = tblMaster.getSelectedRow();
        if (r < 0 || r >= dsHienTai.size()) return;

        banGhiDangChon = dsHienTai.get(r);
        lblTieuDeDetail.setText("Chi tiết lần sửa chữa: [" + banGhiDangChon.getMaPTN() + " / " + banGhiDangChon.getMaPSC()
                + "] — Khách hàng: " + banGhiDangChon.getTenKH() + " (" + banGhiDangChon.getSoDT() + ") — Xe: "
                + banGhiDangChon.getBienSoXe() + " (" + banGhiDangChon.getTenXe() + ")");

        // 1. Nạp công việc
        modelCongViec.setRowCount(0);
        ArrayList<ChiTietCongViecDTO> dsCv = dao.layChiTietCongViec(banGhiDangChon.getMaPSC(), banGhiDangChon.getMaHD());
        long sumCong = 0;
        int sttCv = 1;
        for (ChiTietCongViecDTO cv : dsCv) {
            sumCong += cv.getTienCong();
            modelCongViec.addRow(new Object[]{
                    sttCv++,
                    cv.getNoiDung(),
                    cv.getTenKTV(),
                    UiHelper.tien(cv.getTienCong()) + " đ"
            });
        }
        lblTongTienCong.setText("Tổng tiền công: " + UiHelper.tien(sumCong > 0 ? sumCong : banGhiDangChon.getTienDichVu()) + " đ (" + dsCv.size() + " hạng mục)");

        // 2. Nạp phụ tùng
        modelPhuTung.setRowCount(0);
        ArrayList<ChiTietPhuTungDTO> dsPt = dao.layChiTietPhuTung(banGhiDangChon.getMaPSC(), banGhiDangChon.getMaHD());
        long sumPt = 0;
        int sttPt = 1;
        for (ChiTietPhuTungDTO pt : dsPt) {
            sumPt += pt.getThanhTien();
            modelPhuTung.addRow(new Object[]{
                    sttPt++,
                    pt.getMaPT(),
                    pt.getTenPT(),
                    pt.getDonViTinh(),
                    pt.getSoLuong(),
                    UiHelper.tien(pt.getDonGia()) + " đ",
                    UiHelper.tien(pt.getThanhTien()) + " đ",
                    pt.getTrangThai()
            });
        }
        lblTongTienPhuTung.setText("Tổng tiền phụ tùng: " + UiHelper.tien(sumPt > 0 ? sumPt : banGhiDangChon.getTienPhuTung()) + " đ (" + dsPt.size() + " mặt hàng)");

        // 3. Nạp hóa đơn & bảo hành
        ArrayList<ChiTietBaoHanhDTO> dsBh = dao.layChiTietBaoHanh(banGhiDangChon.getMaHD(), banGhiDangChon.getMaKH(), banGhiDangChon.getSoVIN());
        napThongTinHoaDonVaBaoHanh(banGhiDangChon, dsBh);
    }

    private void napThongTinHoaDonVaBaoHanh(LichSuRecord r, ArrayList<ChiTietBaoHanhDTO> dsBh) {
        pnlHoaDonVaBaoHanh.removeAll();

        if (r == null) {
            JLabel lblNull = new JLabel("Chưa chọn lần sửa chữa nào để xem thông tin hóa đơn và bảo hành", SwingConstants.CENTER);
            lblNull.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblNull.setForeground(new Color(148, 163, 184));
            pnlHoaDonVaBaoHanh.add(lblNull, BorderLayout.CENTER);
            pnlHoaDonVaBaoHanh.revalidate();
            pnlHoaDonVaBaoHanh.repaint();
            return;
        }

        JPanel grid = new JPanel(new GridLayout(1, 2, 16, 0));
        grid.setOpaque(false);

        // Panel Trái: Thông tin Hóa đơn
        JPanel pnlHd = new JPanel(new BorderLayout(0, 8));
        pnlHd.setOpaque(true);
        pnlHd.setBackground(new Color(248, 250, 252));
        pnlHd.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblHdTitle = new JLabel("THÔNG TIN HÓA ĐƠN DỊCH VỤ", UiIcon.of(UiIcon.BILL, 16, new Color(37, 99, 235)), SwingConstants.LEFT);
        lblHdTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHdTitle.setForeground(new Color(15, 23, 42));
        pnlHd.add(lblHdTitle, BorderLayout.NORTH);

        StringBuilder sbHd = new StringBuilder();
        sbHd.append("<html><div style='font-family:Segoe UI; font-size:12px; line-height:1.6;'>")
            .append("<b>Mã hóa đơn:</b> <span style='color:#2563EB; font-weight:bold;'>").append(r.getMaHD()).append("</span><br>")
            .append("<b>Khách hàng:</b> ").append(r.getTenKH()).append(" — SĐT: ").append(r.getSoDT()).append("<br>")
            .append("<b>Phương tiện:</b> ").append(r.getBienSoXe()).append(" (").append(r.getTenXe()).append(")<br>")
            .append("<b>Hình thức thanh toán:</b> ").append(r.getHinhThucTT()).append("<br>")
            .append("<b>Trạng thái:</b> ").append(r.getTrangThaiHD().contains("Đã thanh toán")
                    ? "<span style='color:#16A34A; font-weight:bold;'>Đã thanh toán</span>"
                    : "<span style='color:#E11D48; font-weight:bold;'>Chưa thanh toán</span>").append("<br>")
            .append("<hr style='border:0; border-top:1px solid #E2E8F0; margin:6px 0;'>")
            .append("<b>Tiền công dịch vụ:</b> ").append(UiHelper.tien(r.getTienDichVu())).append(" đ<br>")
            .append("<b>Tiền phụ tùng:</b> ").append(UiHelper.tien(r.getTienPhuTung())).append(" đ<br>")
            .append("<b>TỔNG TIỀN PHẢI TRẢ:</b> <span style='color:#DC2626; font-size:14px; font-weight:bold;'>")
            .append(UiHelper.tien(r.getTongTien())).append(" đ</span>")
            .append("</div></html>");

        JLabel lblHdBody = new JLabel(sbHd.toString());
        JScrollPane spHd = new JScrollPane(lblHdBody); spHd.setBorder(null); spHd.setOpaque(false); spHd.getViewport().setOpaque(false); pnlHd.add(spHd, BorderLayout.CENTER);
        grid.add(pnlHd);

        // Panel Phải: Thông tin Bảo hành
        JPanel pnlBh = new JPanel(new BorderLayout(0, 8));
        pnlBh.setOpaque(true);
        pnlBh.setBackground(new Color(248, 250, 252));
        pnlBh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblBhTitle = new JLabel("SỔ BẢO HÀNH ĐI KÈM", UiIcon.of(UiIcon.SHIELD, 16, new Color(22, 163, 74)), SwingConstants.LEFT);
        lblBhTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBhTitle.setForeground(new Color(15, 23, 42));
        pnlBh.add(lblBhTitle, BorderLayout.NORTH);

        StringBuilder sbBh = new StringBuilder();
        sbBh.append("<html><div style='font-family:Segoe UI; font-size:12px; line-height:1.6;'>");

        if (dsBh == null || dsBh.isEmpty()) {
            sbBh.append("<div style='color:#64748B; padding:10px 0;'>")
                .append("<i>Lần sửa chữa này chưa có phiếu bảo hành hoặc phụ tùng thay thế không thuộc diện cấp sổ bảo hành định kỳ.</i>")
                .append("</div>");
        } else {
            ChiTietBaoHanhDTO bh = dsBh.get(0);
            sbBh.append("<b>Số phiếu bảo hành:</b> <span style='color:#16A34A; font-weight:bold;'>").append(bh.getMaPBH()).append("</span><br>")
                .append("<b>Thời hạn hiệu lực:</b> từ ").append(bh.getNgayBD()).append(" đến <b>").append(bh.getNgayKT()).append("</b><br>")
                .append("<b>Tình trạng:</b> ").append(bh.getTrangThai().contains("Còn hạn")
                        ? "<span style='color:#16A34A; font-weight:bold;'>Còn hiệu lực bảo hành</span>"
                        : "<span style='color:#DC2626; font-weight:bold;'>Hết hạn bảo hành</span>").append("<br>")
                .append("<hr style='border:0; border-top:1px solid #E2E8F0; margin:6px 0;'>")
                .append("<b>Hạng mục bảo hành:</b> ").append(bh.getTenHangMuc()).append("<br>")
                .append("<b>Thời gian cam kết:</b> ").append(bh.getThoiHan()).append(" tháng<br>")
                .append("<b>Điều kiện:</b> ").append(bh.getDieuKien());
        }
        sbBh.append("</div></html>");

        JLabel lblBhBody = new JLabel(sbBh.toString());
        JScrollPane spBh = new JScrollPane(lblBhBody); spBh.setBorder(null); spBh.setOpaque(false); spBh.getViewport().setOpaque(false); pnlBh.add(spBh, BorderLayout.CENTER);
        grid.add(pnlBh);

        pnlHoaDonVaBaoHanh.add(grid, BorderLayout.CENTER);
        pnlHoaDonVaBaoHanh.revalidate();
        pnlHoaDonVaBaoHanh.repaint();
    }

    // =========================================================================
    //                        CÁC THAO TÁC NÚT (ACTIONS)
    // =========================================================================
    private void xemHoSoDayDu() {
        if (banGhiDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lần sửa chữa trong bảng để xem hồ sơ đầy đủ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<ChiTietCongViecDTO> dsCv = dao.layChiTietCongViec(banGhiDangChon.getMaPSC(), banGhiDangChon.getMaHD());
        ArrayList<ChiTietPhuTungDTO> dsPt = dao.layChiTietPhuTung(banGhiDangChon.getMaPSC(), banGhiDangChon.getMaHD());
        ArrayList<ChiTietBaoHanhDTO> dsBh = dao.layChiTietBaoHanh(banGhiDangChon.getMaHD(), banGhiDangChon.getMaKH(), banGhiDangChon.getSoVIN());

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Hồ sơ sửa chữa chi tiết — " + banGhiDangChon.getBienSoXe(), true);
        dlg.setSize(850, 620);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new BorderLayout(12, 12));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Header Dialog
        JPanel dlgHeader = new JPanel(new BorderLayout());
        dlgHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("HỒ SƠ TIẾP NHẬN & SỬA CHỮA PHƯƠNG TIỆN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Hệ thống Quản lý Gara Ô tô The T4 — Tra cứu lịch sử kỹ thuật & thanh toán");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        dlgHeader.add(lblTitle, BorderLayout.NORTH);
        dlgHeader.add(lblSub, BorderLayout.SOUTH);
        pnl.add(dlgHeader, BorderLayout.NORTH);

        // Body Dialog HTML
        StringBuilder html = new StringBuilder();
        html.append("<html><div style='font-family:Segoe UI; font-size:12px; color:#1E293B; line-height:1.5;'>")
            .append("<table width='100%' cellpadding='5' style='background:#F8FAFC; border:1px solid #E2E8F0; margin-bottom:12px;'>")
            .append("<tr>")
            .append("<td width='50%'><b>Mã phiếu tiếp nhận:</b> <b style='color:#2563EB;'>").append(banGhiDangChon.getMaPTN()).append("</b></td>")
            .append("<td width='50%'><b>Mã phiếu sửa chữa:</b> <b style='color:#059669;'>").append(banGhiDangChon.getMaPSC()).append("</b></td>")
            .append("</tr><tr>")
            .append("<td><b>Ngày giờ tiếp nhận:</b> ").append(banGhiDangChon.getNgayTN()).append("</td>")
            .append("<td><b>Kỹ thuật viên phụ trách:</b> ").append(banGhiDangChon.getTenKTV()).append("</td>")
            .append("</tr><tr>")
            .append("<td><b>Khách hàng:</b> ").append(banGhiDangChon.getTenKH()).append(" — <b>SĐT:</b> ").append(banGhiDangChon.getSoDT()).append("</td>")
            .append("<td><b>Phương tiện:</b> <b style='color:#D97706;'>").append(banGhiDangChon.getBienSoXe()).append("</b> (").append(banGhiDangChon.getTenXe()).append(")</td>")
            .append("</tr><tr>")
            .append("<td colspan='2'><b>Yêu cầu / Hiện trạng xe:</b> ").append(banGhiDangChon.getYeuCauSC()).append("</td>")
            .append("</tr><tr>")
            .append("<td><b>Tiến độ sửa chữa:</b> <b style='color:#2563EB;'>").append(banGhiDangChon.getTrangThaiSC()).append("</b></td>")
            .append("<td><b>Tình trạng thanh toán:</b> ").append(banGhiDangChon.getTrangThaiHD().contains("Đã thanh toán") ? "<b style='color:#16A34A;'>Đã thanh toán</b>" : "<b style='color:#DC2626;'>Chưa thanh toán</b>").append(" (HĐ: ").append(banGhiDangChon.getMaHD()).append(")</td>")
            .append("</tr></table>");

        // Bảng công việc
        html.append("<h4 style='color:#0F172A; margin:8px 0 4px 0;'>1. CÔNG VIỆC SỬA CHỮA & TIỀN CÔNG DỊCH VỤ</h4>")
            .append("<table width='100%' border='1' cellspacing='0' cellpadding='4' style='border-color:#E2E8F0; border-collapse:collapse;'>")
            .append("<tr style='background:#F1F5F9; font-weight:bold;'>")
            .append("<th width='35'>STT</th><th>Nội dung công việc sửa chữa</th><th width='150'>KTV thực hiện</th><th width='110' align='right'>Tiền công</th>")
            .append("</tr>");

        if (dsCv.isEmpty()) {
            html.append("<tr><td colspan='4' align='center' style='color:#94A3B8;'><i>Chưa có ghi nhận công việc sửa chữa chi tiết</i></td></tr>");
        } else {
            int stt = 1;
            for (ChiTietCongViecDTO cv : dsCv) {
                html.append("<tr>")
                    .append("<td align='center'>").append(stt++).append("</td>")
                    .append("<td>").append(cv.getNoiDung()).append("</td>")
                    .append("<td>").append(cv.getTenKTV()).append("</td>")
                    .append("<td align='right'>").append(UiHelper.tien(cv.getTienCong())).append(" đ</td>")
                    .append("</tr>");
            }
        }
        html.append("<tr style='background:#F8FAFC; font-weight:bold;'><td colspan='3' align='right'>Tổng tiền công dịch vụ:</td><td align='right' style='color:#2563EB;'>")
            .append(UiHelper.tien(banGhiDangChon.getTienDichVu())).append(" đ</td></tr></table>");

        // Bảng phụ tùng
        html.append("<h4 style='color:#0F172A; margin:12px 0 4px 0;'>2. PHỤ TÙNG & VẬT TƯ THAY THẾ</h4>")
            .append("<table width='100%' border='1' cellspacing='0' cellpadding='4' style='border-color:#E2E8F0; border-collapse:collapse;'>")
            .append("<tr style='background:#F1F5F9; font-weight:bold;'>")
            .append("<th width='35'>STT</th><th width='75'>Mã PT</th><th>Tên phụ tùng</th><th width='45'>ĐVT</th><th width='45'>SL</th><th width='95' align='right'>Đơn giá</th><th width='105' align='right'>Thành tiền</th>")
            .append("</tr>");

        if (dsPt.isEmpty()) {
            html.append("<tr><td colspan='7' align='center' style='color:#94A3B8;'><i>Không sử dụng phụ tùng thay thế</i></td></tr>");
        } else {
            int stt = 1;
            for (ChiTietPhuTungDTO pt : dsPt) {
                html.append("<tr>")
                    .append("<td align='center'>").append(stt++).append("</td>")
                    .append("<td align='center'>").append(pt.getMaPT()).append("</td>")
                    .append("<td>").append(pt.getTenPT()).append("</td>")
                    .append("<td align='center'>").append(pt.getDonViTinh()).append("</td>")
                    .append("<td align='center'>").append(pt.getSoLuong()).append("</td>")
                    .append("<td align='right'>").append(UiHelper.tien(pt.getDonGia())).append(" đ</td>")
                    .append("<td align='right'>").append(UiHelper.tien(pt.getThanhTien())).append(" đ</td>")
                    .append("</tr>");
            }
        }
        html.append("<tr style='background:#F8FAFC; font-weight:bold;'><td colspan='6' align='right'>Tổng tiền phụ tùng:</td><td align='right' style='color:#D97706;'>")
            .append(UiHelper.tien(banGhiDangChon.getTienPhuTung())).append(" đ</td></tr></table>");

        // Tổng kết chi phí
        html.append("<div style='margin-top:12px; padding:10px; background:#EFF6FF; border:1px solid #BFDBFE; text-align:right;'>")
            .append("<span style='font-size:13px;'>TỔNG CỘNG CHI PHÍ SỬA CHỮA: </span>")
            .append("<b style='font-size:16px; color:#DC2626;'>").append(UiHelper.tien(banGhiDangChon.getTongTien())).append(" đ</b>")
            .append("</div>");

        // Bảo hành nếu có
        if (dsBh != null && !dsBh.isEmpty()) {
            ChiTietBaoHanhDTO bh = dsBh.get(0);
            html.append("<div style='margin-top:8px; padding:8px 12px; background:#F0FDF4; border:1px solid #BBF7D0;'>")
                .append("<b>SỔ BẢO HÀNH: </b> Mã <b>").append(bh.getMaPBH()).append("</b> | Hiệu lực: <b>").append(bh.getNgayBD()).append("</b> đến <b>").append(bh.getNgayKT()).append("</b><br>")
                .append("<b>Hạng mục: </b>").append(bh.getTenHangMuc()).append(" (Thời hạn: ").append(bh.getThoiHan()).append(" tháng) | <b>Điều kiện: </b>").append(bh.getDieuKien())
                .append("</div>");
        }

        html.append("</div></html>");

        JLabel lblBody = new JLabel(html.toString());
        JScrollPane sp = new JScrollPane(lblBody);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        pnl.add(sp, BorderLayout.CENTER);

        // Nút đóng
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setOpaque(false);
        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, e -> dlg.dispose());
        pnlBot.add(btnDong);
        pnl.add(pnlBot, BorderLayout.SOUTH);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }

    private void inPhieuTomTat() {
        if (banGhiDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lần sửa chữa trong bảng để in phiếu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        xemHoSoDayDu();
    }

    private void lapPhieuTiepNhanMoi() {
        Main_UI mainUI = Main_UI.getInstance();
        if (mainUI == null) {
            mainUI = (Main_UI) SwingUtilities.getAncestorOfClass(Main_UI.class, this);
        }
        if (mainUI != null) {
            mainUI.chuyenTrang("Lập phiếu tiếp nhận");
            if (banGhiDangChon != null && !banGhiDangChon.getSoDT().isEmpty()) {
                String sdt = banGhiDangChon.getSoDT();
                SwingUtilities.invokeLater(() -> {
                    Main_UI mu = Main_UI.getInstance();
                    if (mu != null) {
                        timVaDienSdt(mu.getContentPane(), sdt);
                    }
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể chuyển trang lập phiếu tiếp nhận.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean timVaDienSdt(Container c, String sdt) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JTextField tf && tf.getToolTipText() != null && tf.getToolTipText().contains("SĐT")) {
                tf.setText(sdt);
                tf.postActionEvent();
                return true;
            }
            if (comp instanceof Container child) {
                if (timVaDienSdt(child, sdt)) return true;
            }
        }
        return false;
    }

    private void lamMoi() {
        txtTuKhoa.setText("");
        cboTieuChi.setSelectedIndex(0);
        cboThoiGian.setSelectedIndex(0);
        locTrangThai = "TAT_CA";
        locThanhToan = "TAT_CA";
        locThoiGian = "TAT_CA";
        capNhatGiaoDienChip();
        capNhatGoiY();
        napBang();
    }

    // =========================================================================
    //                        ĐỊNH DẠNG BẢNG & CỘT
    // =========================================================================
    private void dinhDangCotMaster(int col, int align, int width) {
        TableColumn tc = tblMaster.getColumnModel().getColumn(col);
        tc.setPreferredWidth(width);

        tc.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                lbl.setHorizontalAlignment(align);
                lbl.setBackground(new Color(241, 245, 249));
                lbl.setForeground(new Color(30, 41, 59));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(8, align == SwingConstants.LEFT ? 10 : 4, 8, 4)
                ));
                return lbl;
            }
        });

        tc.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                String strVal = val != null ? val.toString() : "";

                if (!isSel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(new Color(30, 41, 59));

                    // Cột Tiến độ sửa chữa (cột 9)
                    if (c == 9) {
                        if (strVal.contains("Hoàn thành")) {
                            comp.setForeground(new Color(22, 163, 74));
                            comp.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        } else if (strVal.contains("Đang sửa")) {
                            comp.setForeground(new Color(217, 119, 6));
                            comp.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        } else if (strVal.contains("Chờ phụ tùng")) {
                            comp.setForeground(new Color(225, 29, 72));   // Rose 600
                            comp.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        } else {
                            comp.setForeground(new Color(71, 85, 105));
                        }
                    }
                    // Cột Thanh toán (cột 13)
                    else if (c == 13) {
                        if (strVal.contains("Đã thanh toán")) {
                            comp.setForeground(new Color(22, 163, 74));
                            comp.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        } else {
                            comp.setForeground(new Color(225, 29, 72));   // Rose 600
                            comp.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        }
                    }
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(UiHelper.XANH_TRUNG);
                }

                if (comp instanceof JLabel l) {
                    l.setHorizontalAlignment(align);
                    l.setBorder(new EmptyBorder(0, align == SwingConstants.LEFT ? 10 : 4, 0, 4));
                }
                return comp;
            }
        });
    }

    private void dinhDangCotCon(JTable tbl, int col, int align, int width) {
        TableColumn tc = tbl.getColumnModel().getColumn(col);
        tc.setPreferredWidth(width);

        tc.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                lbl.setHorizontalAlignment(align);
                lbl.setBackground(new Color(248, 250, 252));
                lbl.setForeground(new Color(51, 65, 85));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(6, 6, 6, 6)
                ));
                return lbl;
            }
        });

        tc.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                if (!isSel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                    comp.setForeground(new Color(30, 41, 59));
                } else {
                    comp.setBackground(new Color(239, 246, 255));
                    comp.setForeground(UiHelper.XANH_TRUNG);
                }
                if (comp instanceof JLabel l) {
                    l.setHorizontalAlignment(align);
                    l.setBorder(new EmptyBorder(0, align == SwingConstants.LEFT ? 8 : 4, 0, 4));
                }
                return comp;
            }
        });
    }
}
