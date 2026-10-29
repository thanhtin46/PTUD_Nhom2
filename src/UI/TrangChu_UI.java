package UI;

import DAO.TrangChuTiepTan_DAO;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;
import Entity.PhuTung;
import Entity.ThongKeTiepTan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TrangChu_UI — Màn hình Dashboard điều khiển & vận hành tổng quan Gara Ô tô The T4:
 * - Header thoáng đãng, hiển thị thời gian thực và nút làm mới dữ liệu
 * - Hệ thống 6 thẻ thống kê chỉ số KPI chính (Khách hàng, Phương tiện, Sửa chữa, Bảo hành, Tồn kho, Nhân viên)
 *   với kích thước gọn gàng, hiệu ứng hover và hỗ trợ bấm để chuyển trực tiếp đến màn hình tương ứng
 * - Cột trái (64%): Bảng tiến độ các xe đang tiếp nhận & sửa chữa gần đây trong xưởng với badge màu trực quan
 * - Cột phải (36%): Khối phím tắt thao tác nhanh (6 tác vụ chính) + Khối cảnh báo vận hành & phụ tùng sắp hết
 * - Chân trang: Banner chào mừng sang trọng kèm trạng thái kết nối SQL Server thời gian thực
 */
public class TrangChu_UI extends JPanel {

    private final Main_UI mainUI;
    private final NhanVien nvHienTai;
    private final TrangChuTiepTan_DAO trangChuDAO = new TrangChuTiepTan_DAO();

    // Các nhãn số liệu thống kê KPI
    private JLabel lblSoKH, lblMoTaKH;
    private JLabel lblSoXe, lblMoTaXe;
    private JLabel lblSoSC, lblMoTaSC;
    private JLabel lblSoBH, lblMoTaBH;
    private JLabel lblSoKho, lblMoTaKho;
    private JLabel lblSoNV, lblMoTaNV;

    // Bảng tiến độ xe trong xưởng
    private DefaultTableModel modelXe;
    private JTable tblXe;
    private final ArrayList<PhieuTiepNhan> dsXeHienThi = new ArrayList<>();
    private JLabel lblBadgeXeDangXuLy;

    // Các thành phần cảnh báo vận hành
    private JPanel boxCanhBaoKho;
    private JLabel lblCanhBaoKhoIcon;
    private JLabel lblCanhBaoKhoTieuDe;
    private JLabel lblCanhBaoKhoChiTiet;
    private JLabel lblCanhBaoXeText;
    private JLabel lblCanhBaoBHText;
    private JLabel lblCapNhatLuc;

    public TrangChu_UI() {
        this(null, null);
    }

    public TrangChu_UI(Main_UI mainUI) {
        this(mainUI, null);
    }

    public TrangChu_UI(Main_UI mainUI, NhanVien nvHienTai) {
        this.mainUI = mainUI;
        this.nvHienTai = nvHienTai;

        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        root.setLayout(new BorderLayout(0, 10));
        add(root, BorderLayout.CENTER);

        // 1. Header Trang chủ
        root.add(taoHeader(), BorderLayout.NORTH);

        // 2. Nội dung chính trong CuonPanel (tự động khóa theo chiều ngang viewport)
        UiHelper.CuonPanel contentBody = new UiHelper.CuonPanel(new BoxLayout(null, BoxLayout.Y_AXIS));
        contentBody.setLayout(new BoxLayout(contentBody, BoxLayout.Y_AXIS));
        contentBody.setOpaque(false);

        // 2.1. Lưới 6 thẻ thống kê KPI
        contentBody.add(taoLuoiKPI());
        contentBody.add(Box.createVerticalStrut(12));

        // 2.2. Khu vực trung tâm: Bảng tiến độ xe (Trái) + Thao tác nhanh & Cảnh báo (Phải)
        contentBody.add(taoKhuVucTrungTam());
        contentBody.add(Box.createVerticalStrut(12));

        // 2.3. Banner chào mừng ở chân trang
        contentBody.add(taoBannerChaoMung());

        JScrollPane sp = new JScrollPane(contentBody);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        root.add(sp, BorderLayout.CENTER);

        // Nạp dữ liệu ban đầu
        napDuLieu();
    }

    // ===============================================================
    // 1. HEADER BẢNG ĐIỀU KHIỂN
    // ===============================================================
    private JPanel taoHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);

        // Cột trái: Tiêu đề & mô tả
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel tde = UiHelper.tieuDeTrang("Bảng điều khiển & Vận hành Gara");
        JLabel phu = new JLabel("Tổng quan hoạt động, tiến độ xưởng & cảnh báo vận hành Gara Ô tô The T4");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phu.setForeground(new Color(100, 116, 139));

        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(tde);
        text.add(Box.createVerticalStrut(3));
        text.add(phu);
        header.add(text, BorderLayout.CENTER);

        // Cột phải: Ngày giờ thời gian thực & Nút làm mới
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        right.setOpaque(false);

        String ngayHienTai = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy", Locale.forLanguageTag("vi"))
        );
        ngayHienTai = Character.toUpperCase(ngayHienTai.charAt(0)) + ngayHienTai.substring(1);

        JPanel pnlNgay = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlNgay.setOpaque(false);
        JLabel iconLich = new JLabel(UiIcon.of(UiIcon.CLOCK, 14, new Color(71, 85, 105)));
        JLabel lblNgay = new JLabel(ngayHienTai);
        lblNgay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNgay.setForeground(new Color(71, 85, 105));
        pnlNgay.add(iconLich);
        pnlNgay.add(lblNgay);
        pnlNgay.setBorder(new EmptyBorder(0, 0, 0, 8));
        right.add(pnlNgay);

        lblCapNhatLuc = new JLabel("Cập nhật: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        lblCapNhatLuc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCapNhatLuc.setForeground(new Color(148, 163, 184));
        right.add(lblCapNhatLuc);

        JButton btnLamMoi = UiHelper.nut(" Làm mới", UiIcon.of(UiIcon.REFRESH, 13), UiHelper.XANH_TRUNG, e -> napDuLieu());
        btnLamMoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLamMoi.setPreferredSize(new Dimension(110, 34));
        right.add(btnLamMoi);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ===============================================================
    // 2. LƯỚI 6 THẺ CHỈ SỐ KPI CHÍNH (GỌN GÀNG, BẤM ĐIỀU HƯỚNG)
    // ===============================================================
    private JPanel taoLuoiKPI() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 10));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSoKH = new JLabel("0");
        lblMoTaKH = new JLabel("Tổng số khách đã đăng ký");
        grid.add(theThongKe(
                UiIcon.of(UiIcon.USERS, 22, new Color(37, 99, 235)),
                "KHÁCH HÀNG", lblSoKH, lblMoTaKH,
                new Color(37, 99, 235), "Quản lý khách hàng"
        ));

        lblSoXe = new JLabel("0");
        lblMoTaXe = new JLabel("Đang được quản lý");
        grid.add(theThongKe(
                UiIcon.of(UiIcon.CAR, 22, new Color(245, 158, 11)),
                "PHƯƠNG TIỆN", lblSoXe, lblMoTaXe,
                new Color(245, 158, 11), "Quản lý phương tiện"
        ));

        lblSoSC = new JLabel("0");
        lblMoTaSC = new JLabel("Tiếp nhận và xử lý");
        grid.add(theThongKe(
                UiIcon.of(UiIcon.WRENCH, 22, new Color(22, 163, 74)),
                "PHIẾU SỬA CHỮA", lblSoSC, lblMoTaSC,
                new Color(22, 163, 74), "Phiếu sửa chữa"
        ));

        lblSoBH = new JLabel("0");
        lblMoTaBH = new JLabel("Còn hạn & bảo dưỡng định kỳ");
        grid.add(theThongKe(
                UiIcon.of(UiIcon.SHIELD, 22, new Color(124, 58, 237)),
                "PHIẾU BẢO HÀNH", lblSoBH, lblMoTaBH,
                new Color(124, 58, 237), "Bảo hành"
        ));

        lblSoKho = new JLabel("0");
        lblMoTaKho = new JLabel("Mặt hàng trong kho");
        grid.add(theThongKe(
                UiIcon.of(UiIcon.BOX, 22, new Color(14, 165, 233)),
                "TỒN KHO PHỤ TÙNG", lblSoKho, lblMoTaKho,
                new Color(14, 165, 233), "Báo cáo tồn kho"
        ));

        lblSoNV = new JLabel("0");
        lblMoTaNV = new JLabel("Tài khoản đang hoạt động");
        grid.add(theThongKe(
                UiIcon.of(UiIcon.USER, 22, new Color(239, 68, 68)),
                "NHÂN VIÊN", lblSoNV, lblMoTaNV,
                new Color(239, 68, 68), "Nhân viên"
        ));

        return grid;
    }

    /** Tạo một thẻ thống kê nhỏ gọn, có icon pastel, hiệu ứng hover và bấm chuyển trang */
    private JPanel theThongKe(Icon icon, String ten, JLabel lblGiaTri, JLabel lblMoTa, Color mau, String tenMenu) {
        JPanel p = new JPanel(new BorderLayout(12, 0)) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        dieuHuong(tenMenu);
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Đổ bóng
                g2.setColor(new Color(15, 23, 42, hover ? 14 : 7));
                g2.fillRoundRect(0, 2, w, h - 2, 14, 14);

                // Nền card trắng
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h - 1, 14, 14);

                // Viền ngoài (đổi màu khi hover)
                g2.setColor(hover ? mau : UiHelper.VIEN);
                g2.setStroke(new BasicStroke(hover ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, w - 1, h - 2, 14, 14);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.setBorder(new EmptyBorder(10, 14, 10, 14));
        p.setPreferredSize(new Dimension(0, 78));

        // Khối icon pastel
        JLabel bieuTuong = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 26));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bieuTuong.setOpaque(false);
        bieuTuong.setPreferredSize(new Dimension(42, 42));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTen.setForeground(new Color(100, 116, 139));

        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblGiaTri.setForeground(UiHelper.XANH_DAM);

        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMoTa.setForeground(new Color(148, 163, 184));

        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGiaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(lblTen);
        text.add(lblGiaTri);
        text.add(lblMoTa);

        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 13));
        arrow.setForeground(new Color(203, 213, 225));

        p.add(bieuTuong, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        p.add(arrow, BorderLayout.EAST);
        return p;
    }

    // ===============================================================
    // 3. KHU VỰC TRUNG TÂM: TIẾN ĐỘ XE (TRÁI) + THAO TÁC NHANH & CẢNH BÁO (PHẢI)
    // ===============================================================
    private JPanel taoKhuVucTrungTam() {
        JPanel pnl = new JPanel(new BorderLayout(14, 0));
        pnl.setOpaque(false);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Cột trái: Bảng danh sách xe đang tiếp nhận & sửa chữa gần đây
        pnl.add(taoBangTienDoXe(), BorderLayout.CENTER);

        // Cột phải: Thao tác nhanh & Cảnh báo vận hành
        pnl.add(taoPanelTienIchPhai(), BorderLayout.EAST);

        return pnl;
    }

    /** Bảng danh sách xe trong xưởng dịch vụ */
    private JPanel taoBangTienDoXe() {
        JPanel card = UiHelper.taoThe(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Header bảng
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerLeft.setOpaque(false);

        JLabel iconXe = new JLabel(UiIcon.of(UiIcon.CAR, 18, UiHelper.XANH_DAM));
        JLabel lblTieuDe = new JLabel("Tiến độ xe trong xưởng dịch vụ");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTieuDe.setForeground(UiHelper.XANH_DAM);

        lblBadgeXeDangXuLy = new JLabel("0 xe đang xử lý") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 252, 231));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadgeXeDangXuLy.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadgeXeDangXuLy.setForeground(new Color(22, 101, 52));
        lblBadgeXeDangXuLy.setBorder(new EmptyBorder(3, 8, 3, 8));

        headerLeft.add(iconXe);
        headerLeft.add(lblTieuDe);
        headerLeft.add(lblBadgeXeDangXuLy);
        header.add(headerLeft, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        headerRight.setOpaque(false);

        JButton btnThemTN = UiHelper.nut("+ Tiếp nhận xe", UiIcon.of(UiIcon.PLUS, 12), UiHelper.XANH_TRUNG, e -> dieuHuong("Lập phiếu tiếp nhận"));
        btnThemTN.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnThemTN.setPreferredSize(new Dimension(115, 30));

        JButton btnXemTatCa = UiHelper.nut("Tiến độ xe →", null, UiHelper.XAM, e -> dieuHuong("Cập nhật tiến độ"));
        btnXemTatCa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnXemTatCa.setPreferredSize(new Dimension(105, 30));

        headerRight.add(btnThemTN);
        headerRight.add(btnXemTatCa);
        header.add(headerRight, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // Bảng dữ liệu xe
        modelXe = UiHelper.model(
                "STT", "Số phiếu", "Biển số xe", "Khách hàng",
                "Yêu cầu dịch vụ", "Ngày tiếp nhận", "Tiến độ"
        );
        tblXe = UiHelper.bang(modelXe);
        tblXe.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblXe.setRowHeight(34);

        dinhDangCot(0, SwingConstants.CENTER, 40);   // STT
        dinhDangCot(1, SwingConstants.CENTER, 75);   // Số phiếu
        dinhDangCotBienSo(2, 95);                    // Biển số xe (in đậm xanh)
        dinhDangCot(3, SwingConstants.LEFT, 115);    // Khách hàng
        dinhDangCot(4, SwingConstants.LEFT, 170);    // Yêu cầu
        dinhDangCot(5, SwingConstants.CENTER, 90);   // Ngày tiếp nhận
        dinhDangCotTienDo(6, 115);                   // Tiến độ (Pill badge)

        tblXe.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dieuHuong("Cập nhật tiến độ");
                }
            }
        });

        JScrollPane sp = new JScrollPane(tblXe);
        sp.setPreferredSize(new Dimension(540, 290));
        sp.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        card.add(sp, BorderLayout.CENTER);

        // Hướng dẫn ở chân bảng
        JLabel lblGhiChu = new JLabel("Nhấp đúp chuột vào một dòng để chuyển đến màn hình cập nhật tiến độ xe.", UiIcon.of(UiIcon.SEARCH, 12, new Color(148, 163, 184)), SwingConstants.LEFT);
        lblGhiChu.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblGhiChu.setForeground(new Color(148, 163, 184));
        lblGhiChu.setIconTextGap(5);
        lblGhiChu.setBorder(new EmptyBorder(4, 4, 0, 4));
        card.add(lblGhiChu, BorderLayout.SOUTH);

        return card;
    }

    /** Cột tiện ích bên phải: Thao tác nhanh + Cảnh báo vận hành */
    private JPanel taoPanelTienIchPhai() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(340, 0));

        // 1. Khối phím tắt thao tác nhanh
        right.add(taoCardThaoTacNhanh());
        right.add(Box.createVerticalStrut(10));

        // 2. Khối cảnh báo vận hành & tồn kho
        right.add(taoCardCanhBaoVanHanh());

        return right;
    }

    /** Khối phím tắt tác vụ thường dùng */
    private JPanel taoCardThaoTacNhanh() {
        JPanel card = UiHelper.taoThe(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblTde = new JLabel("Thao tác nhanh", UiIcon.of(UiIcon.WRENCH, 14, UiHelper.XANH_DAM), SwingConstants.LEFT);
        lblTde.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTde.setForeground(UiHelper.XANH_DAM);
        lblTde.setIconTextGap(6);
        card.add(lblTde, BorderLayout.NORTH);

        JPanel gridBtns = new JPanel(new GridLayout(3, 2, 8, 7));
        gridBtns.setOpaque(false);

        gridBtns.add(taoNutThaoTacNhanh("Tiếp nhận xe", UiIcon.of(UiIcon.PLUS, 14, new Color(37, 99, 235)), "Lập phiếu tiếp nhận"));
        gridBtns.add(taoNutThaoTacNhanh("Lập phiếu SC", UiIcon.of(UiIcon.WRENCH, 14, new Color(22, 163, 74)), "Phiếu sửa chữa"));
        gridBtns.add(taoNutThaoTacNhanh("Thanh toán HĐ", UiIcon.of(UiIcon.BILL, 14, new Color(14, 165, 233)), "Thanh toán hóa đơn"));
        gridBtns.add(taoNutThaoTacNhanh("Lịch sử xe", UiIcon.of(UiIcon.SEARCH, 14, new Color(124, 58, 237)), "Tra cứu lịch sử sửa chữa"));
        gridBtns.add(taoNutThaoTacNhanh("Lập bảo hành", UiIcon.of(UiIcon.SHIELD, 14, new Color(245, 158, 11)), "Lập phiếu bảo hành"));
        gridBtns.add(taoNutThaoTacNhanh("Kho phụ tùng", UiIcon.of(UiIcon.BOX, 14, new Color(239, 68, 68)), "Báo cáo tồn kho"));

        card.add(gridBtns, BorderLayout.CENTER);
        return card;
    }

    private JButton taoNutThaoTacNhanh(String tieuDe, Icon icon, String tenMenu) {
        JButton btn = new JButton() {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                if (hover) {
                    g2.setColor(new Color(239, 246, 255));
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
                    g2.setColor(new Color(59, 130, 246));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                } else {
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BorderLayout(8, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 8, 6, 8));
        btn.setPreferredSize(new Dimension(0, 36));

        JLabel lblIcon = new JLabel(icon);
        JLabel lblText = new JLabel(tieuDe);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblText.setForeground(new Color(30, 41, 59));

        btn.add(lblIcon, BorderLayout.WEST);
        btn.add(lblText, BorderLayout.CENTER);
        btn.addActionListener(e -> dieuHuong(tenMenu));
        return btn;
    }

    /** Khối cảnh báo vận hành & nhắc nhở */
    private JPanel taoCardCanhBaoVanHanh() {
        JPanel card = UiHelper.taoThe(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblTde = new JLabel("Cảnh báo & Vận hành", UiIcon.of(UiIcon.SHIELD, 14, UiHelper.XANH_DAM), SwingConstants.LEFT);
        lblTde.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTde.setForeground(UiHelper.XANH_DAM);
        lblTde.setIconTextGap(6);
        card.add(lblTde, BorderLayout.NORTH);

        JPanel boxList = new JPanel();
        boxList.setOpaque(false);
        boxList.setLayout(new BoxLayout(boxList, BoxLayout.Y_AXIS));

        // 1. Box Cảnh báo tồn kho phụ tùng
        boxCanhBaoKho = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(253, 230, 138));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        boxCanhBaoKho.setOpaque(false);
        boxCanhBaoKho.setBackground(new Color(254, 252, 232));
        boxCanhBaoKho.setBorder(new EmptyBorder(8, 10, 8, 10));
        boxCanhBaoKho.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boxCanhBaoKho.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dieuHuong("Báo cáo tồn kho");
            }
        });

        lblCanhBaoKhoIcon = new JLabel(UiIcon.of(UiIcon.BOX, 18, new Color(180, 83, 9)));

        JPanel pnlKhoInfo = new JPanel();
        pnlKhoInfo.setOpaque(false);
        pnlKhoInfo.setLayout(new BoxLayout(pnlKhoInfo, BoxLayout.Y_AXIS));

        lblCanhBaoKhoTieuDe = new JLabel("Cảnh báo tồn kho vật tư");
        lblCanhBaoKhoTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCanhBaoKhoTieuDe.setForeground(new Color(146, 64, 14));

        lblCanhBaoKhoChiTiet = new JLabel("Đang kiểm tra tồn kho...");
        lblCanhBaoKhoChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCanhBaoKhoChiTiet.setForeground(new Color(180, 83, 9));

        pnlKhoInfo.add(lblCanhBaoKhoTieuDe);
        pnlKhoInfo.add(Box.createVerticalStrut(2));
        pnlKhoInfo.add(lblCanhBaoKhoChiTiet);

        boxCanhBaoKho.add(lblCanhBaoKhoIcon, BorderLayout.WEST);
        boxCanhBaoKho.add(pnlKhoInfo, BorderLayout.CENTER);
        boxList.add(boxCanhBaoKho);
        boxList.add(Box.createVerticalStrut(7));

        // 2. Box Tiến độ xe
        JPanel boxXe = taoThongBaoItem(
                UiIcon.of(UiIcon.CAR, 18, new Color(29, 78, 216)), "Tiến độ xe tại xưởng",
                lblCanhBaoXeText = new JLabel("Đang tải dữ liệu..."),
                new Color(239, 246, 255), new Color(191, 219, 254),
                new Color(30, 58, 138), new Color(29, 78, 216),
                "Cập nhật tiến độ"
        );
        boxList.add(boxXe);
        boxList.add(Box.createVerticalStrut(7));

        // 3. Box Bảo hành
        JPanel boxBH = taoThongBaoItem(
                UiIcon.of(UiIcon.SHIELD, 18, new Color(107, 33, 168)), "Bảo dưỡng định kỳ",
                lblCanhBaoBHText = new JLabel("Đang tải dữ liệu..."),
                new Color(250, 245, 255), new Color(233, 213, 255),
                new Color(88, 28, 135), new Color(107, 33, 168),
                "Bảo hành"
        );
        boxList.add(boxBH);

        card.add(boxList, BorderLayout.CENTER);
        return card;
    }

    private JPanel taoThongBaoItem(Icon icon, String title, JLabel lblSub,
                                  Color bg, Color border, Color textTitle, Color textSub, String tenMenu) {
        JPanel box = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(8, 10, 8, 10));
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        box.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dieuHuong(tenMenu);
            }
        });

        JLabel lblIcon = new JLabel(icon);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(textTitle);

        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(textSub);

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(2));
        info.add(lblSub);

        box.add(lblIcon, BorderLayout.WEST);
        box.add(info, BorderLayout.CENTER);
        return box;
    }

    // ===============================================================
    // 4. BANNER CHÀO MỪNG CHÂN TRANG
    // ===============================================================
    private JPanel taoBannerChaoMung() {
        JPanel chao = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Đổ bóng nhẹ
                g2.setColor(new Color(15, 23, 42, 8));
                g2.fillRoundRect(0, 2, w, h - 2, 14, 14);

                // Nền card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w, h - 1, 14, 14);

                // Viền ngoài
                g2.setColor(UiHelper.VIEN);
                g2.drawRoundRect(0, 0, w - 1, h - 2, 14, 14);

                // Viền gradient bên trái
                GradientPaint gp = new GradientPaint(0, 0, new Color(37, 99, 235), 0, h, new Color(14, 165, 233));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 6, h - 1, 6, 6);

                g2.dispose();
            }
        };
        chao.setOpaque(false);
        chao.setAlignmentX(Component.LEFT_ALIGNMENT);
        chao.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel noiDungChao = new JPanel();
        noiDungChao.setOpaque(false);
        noiDungChao.setLayout(new BoxLayout(noiDungChao, BoxLayout.Y_AXIS));

        String tenNguoiDung = (nvHienTai != null && nvHienTai.getHoTen() != null)
                ? nvHienTai.getHoTen() + " (" + (nvHienTai.getVaiTro() != null ? nvHienTai.getVaiTro() : "Nhân viên") + ")"
                : "bạn";

        JLabel loiChao = new JLabel("Chào mừng " + tenNguoiDung + " đến với Hệ thống Quản lý Gara Ô tô The T4");
        loiChao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loiChao.setForeground(UiHelper.XANH_DAM);

        JLabel huongDan = new JLabel("Hệ thống đã sẵn sàng phục vụ khách hàng. Vui lòng chọn chức năng từ menu bên trái hoặc dùng thao tác nhanh.");
        huongDan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        huongDan.setForeground(new Color(100, 116, 139));

        loiChao.setAlignmentX(Component.LEFT_ALIGNMENT);
        huongDan.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDungChao.add(loiChao);
        noiDungChao.add(Box.createVerticalStrut(2));
        noiDungChao.add(huongDan);
        chao.add(noiDungChao, BorderLayout.CENTER);

        JLabel trangThai = new JLabel("●  Hệ thống trực tuyến  |  SQL Server: QuanLySuaChuaXe") {
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
        trangThai.setFont(new Font("Segoe UI", Font.BOLD, 11));
        trangThai.setForeground(new Color(22, 163, 74));
        trangThai.setBorder(new EmptyBorder(6, 10, 6, 10));
        chao.add(trangThai, BorderLayout.EAST);

        return chao;
    }

    // ===============================================================
    // 5. XỬ LÝ NẠP DỮ LIỆU & ĐIỀU HƯỚNG
    // ===============================================================
    public void napDuLieu() {
        KhoDuLieu kho = KhoDuLieu.get();
        ThongKeTiepTan tk = trangChuDAO.layThongKeTongQuan();

        // 1. Cập nhật các thẻ KPI
        int tongTon = 0;
        int soPhuTungCanhBao = 0;
        List<PhuTung> dsPhuTung = kho.getPhuTungs();
        for (PhuTung pt : dsPhuTung) {
            tongTon += pt.getSoLuongTon();
            if (pt.getSoLuongTon() <= pt.getSoLuongToiThieu() || pt.getSoLuongTon() <= 5) {
                soPhuTungCanhBao++;
            }
        }

        lblSoKH.setText(String.valueOf(tk.getTongKhachHang()));
        lblMoTaKH.setText("Đăng ký hôm nay: " + tk.getKhachHangMoiHomNay());

        lblSoXe.setText(String.valueOf(tk.getTongPhuongTien()));
        lblMoTaXe.setText("Đang được quản lý");

        lblSoSC.setText(String.valueOf(tk.getTongPhieuTiepNhan()));
        lblMoTaSC.setText(tk.getXeDangSuaChua() + " xe đang sửa chữa");

        lblSoBH.setText(String.valueOf(tk.getTongPhieuBaoHanh()));
        lblMoTaBH.setText(tk.getBaoHanhConHan() + " phiếu còn hạn");

        lblSoKho.setText(String.valueOf(tongTon));
        lblMoTaKho.setText(dsPhuTung.size() + " mặt hàng trong kho");

        lblSoNV.setText(String.valueOf(kho.getNhanViens().size()));
        lblMoTaNV.setText("Tài khoản đang hoạt động");

        // 2. Nạp bảng danh sách xe trong xưởng
        dsXeHienThi.clear();
        modelXe.setRowCount(0);
        ArrayList<PhieuTiepNhan> ds = trangChuDAO.layTiepNhanGanDay(15);
        if (ds == null || ds.isEmpty()) {
            ds = new ArrayList<>(kho.getPhieuTiepNhans());
        }

        int demXeDangXuLy = 0;
        int stt = 1;
        for (PhieuTiepNhan p : ds) {
            dsXeHienThi.add(p);
            String td = p.getTienDo() != null ? p.getTienDo() : "Chờ tiếp nhận";
            if (!td.contains("Đã thanh toán") && !td.contains("Đã giao")) {
                demXeDangXuLy++;
            }
            modelXe.addRow(new Object[]{
                    stt++,
                    p.getSoPhieu(),
                    p.getBienSo(),
                    p.getHoTen(),
                    p.getYeuCau() != null && !p.getYeuCau().isEmpty() ? p.getYeuCau() : "Bảo dưỡng & sửa chữa",
                    p.getNgayTiepNhan(),
                    td
            });
        }
        lblBadgeXeDangXuLy.setText(demXeDangXuLy + " xe đang xử lý");

        // 3. Cập nhật cảnh báo kho
        if (soPhuTungCanhBao > 0) {
            boxCanhBaoKho.setBackground(new Color(254, 252, 232));
            lblCanhBaoKhoIcon.setIcon(UiIcon.of(UiIcon.BOX, 18, new Color(180, 83, 9)));
            lblCanhBaoKhoTieuDe.setText("Cảnh báo tồn kho (" + soPhuTungCanhBao + " mục thấp)");
            lblCanhBaoKhoChiTiet.setText("Có " + soPhuTungCanhBao + " phụ tùng tồn ≤ mức an toàn. Bấm kiểm tra kho →");
        } else {
            boxCanhBaoKho.setBackground(new Color(240, 253, 244));
            lblCanhBaoKhoIcon.setIcon(UiIcon.of(UiIcon.CHECK, 18, new Color(22, 163, 74)));
            lblCanhBaoKhoTieuDe.setText("Kho phụ tùng an toàn");
            lblCanhBaoKhoChiTiet.setText("Toàn bộ " + dsPhuTung.size() + " mặt hàng đều đủ tồn kho cung ứng.");
        }

        // 4. Cập nhật cảnh báo xe & bảo hành
        lblCanhBaoXeText.setText(tk.getXeDangSuaChua() + " xe đang sửa, " + tk.getXeHoanThanh() + " xe hoàn thành.");
        lblCanhBaoBHText.setText(tk.getBaoHanhConHan() + " phiếu bảo hành đang có hiệu lực.");

        // 5. Cập nhật thời gian
        lblCapNhatLuc.setText("Cập nhật: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void dieuHuong(String tenMenu) {
        if (mainUI != null) {
            mainUI.chuyenTrang(tenMenu);
        } else {
            Main_UI main = Main_UI.getInstance();
            if (main != null) {
                main.chuyenTrang(tenMenu);
            } else {
                JOptionPane.showMessageDialog(this, "Chuyển đến màn hình: " + tenMenu, "Điều hướng", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void dinhDangCot(int cot, int canLe, int doRong) {
        TableColumn col = tblXe.getColumnModel().getColumn(cot);
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

    private void dinhDangCotBienSo(int cot, int doRong) {
        TableColumn col = tblXe.getColumnModel().getColumn(cot);
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
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBackground(s ? new Color(219, 234, 254) : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                l.setForeground(new Color(29, 78, 216));
                l.setBorder(new EmptyBorder(4, 4, 4, 4));
                return l;
            }
        });
    }

    private void dinhDangCotTienDo(int cot, int doRong) {
        TableColumn col = tblXe.getColumnModel().getColumn(cot);
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
                String status = v == null ? "Chờ xử lý" : v.toString();

                Color bgPill, fgPill;
                if (status.contains("Hoàn thành") || status.contains("Đã xong")) {
                    bgPill = new Color(220, 252, 231);
                    fgPill = new Color(22, 101, 52);
                } else if (status.contains("Sửa chữa") || status.contains("Đang sửa") || status.contains("Xử lý")) {
                    bgPill = new Color(254, 243, 199);
                    fgPill = new Color(180, 83, 9);
                } else if (status.contains("phụ tùng") || status.contains("linh kiện")) {
                    bgPill = new Color(243, 232, 255);
                    fgPill = new Color(107, 33, 168);
                } else if (status.contains("thanh toán") || status.contains("giao xe")) {
                    bgPill = new Color(241, 245, 249);
                    fgPill = new Color(71, 85, 105);
                } else {
                    bgPill = new Color(219, 234, 254);
                    fgPill = new Color(30, 64, 175);
                }

                JPanel pillWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                pillWrap.setOpaque(true);
                pillWrap.setBackground(s ? new Color(219, 234, 254) : (r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

                JLabel pill = new JLabel(status, SwingConstants.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bgPill);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pill.setForeground(fgPill);
                pill.setBorder(new EmptyBorder(3, 10, 3, 10));

                pillWrap.add(pill);
                return pillWrap;
            }
        });
    }
}
