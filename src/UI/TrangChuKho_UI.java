package UI;

import DAO.TrangChuBoPhan_DAO;
import Entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

/**
 * Màn hình trang chủ dành cho nhân viên thủ kho.
 */
public class TrangChuKho_UI extends JPanel {

    private final NhanVien nvHienTai;
    private final Main_UI mainUI;
    private final TrangChuBoPhan_DAO dao = new TrangChuBoPhan_DAO();

    // KPI labels
    private JLabel lblTongMatHang, lblChoXuat, lblChoNhap, lblCanhBaoTon;
    private JLabel lblCapNhatLuc;

    // Các bảng tab
    private DefaultTableModel modelXuat, modelNhap, modelCanhBao;
    private JTable tblXuat, tblNhap, tblCanhBao;

    // Dữ liệu bộ nhớ tạm
    private final ArrayList<Map<String, Object>> dsXuat = new ArrayList<>();
    private final ArrayList<Map<String, Object>> dsNhap = new ArrayList<>();
    private final ArrayList<Map<String, Object>> dsCanhBao = new ArrayList<>();

    // Thông báo
    private JLabel lblCanhBaoTonKho;
    private JLabel lblThongBaoXuatKho;

    public TrangChuKho_UI(NhanVien nv, Main_UI mainUI) {
        this.nvHienTai = nv;
        this.mainUI = mainUI;

        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        root.setLayout(new BorderLayout(0, 14));
        add(root, BorderLayout.CENTER);

        // 1. Header
        root.add(taoHeader(), BorderLayout.NORTH);

        // 2. Nội dung cuộn
        JPanel contentBody = new JPanel();
        contentBody.setOpaque(false);
        contentBody.setLayout(new BoxLayout(contentBody, BoxLayout.Y_AXIS));

        // 2.1. Thẻ KPI (4 thẻ)
        contentBody.add(taoLuoiKPI());
        contentBody.add(Box.createVerticalStrut(14));

        // 2.2. Khu vực giữa: Tabs nghiệp vụ bên trái + Cảnh báo & Thao tác nhanh bên phải
        JPanel midPanel = new JPanel(new BorderLayout(14, 0));
        midPanel.setOpaque(false);
        midPanel.add(taoTabsNghiepVu(), BorderLayout.CENTER);
        midPanel.add(taoCotPhai(), BorderLayout.EAST);
        contentBody.add(midPanel);

        JScrollPane sp = new JScrollPane(contentBody);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        root.add(sp, BorderLayout.CENTER);

        napDuLieuAsync();
    }

    private JPanel taoHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        String tenNv = (nvHienTai != null && nvHienTai.getHoTen() != null) ? nvHienTai.getHoTen() : "Thủ kho";
        String maNv = (nvHienTai != null && nvHienTai.getMaNV() != null) ? nvHienTai.getMaNV() : "KHO";

        JLabel tde = UiHelper.tieuDeTrang("Bàn làm việc Quản trị Kho & Vật tư");
        JLabel phu = new JLabel("Thủ kho phụ trách: " + maNv + " - " + tenNv + " • Giám sát xuất nhập và kiểm soát định mức an toàn");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(tde);
        left.add(Box.createVerticalStrut(3));
        left.add(phu);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.setOpaque(false);

        lblCapNhatLuc = new JLabel("Cập nhật: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        lblCapNhatLuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCapNhatLuc.setForeground(new Color(100, 116, 139));

        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 14), UiHelper.XANH_TRUNG, e -> napDuLieuAsync());
        btnLamMoi.setPreferredSize(new Dimension(110, 36));

        right.add(lblCapNhatLuc);
        right.add(btnLamMoi);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel taoLuoiKPI() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);

        lblTongMatHang = new JLabel("0");
        lblChoXuat = new JLabel("0");
        lblChoNhap = new JLabel("0");
        lblCanhBaoTon = new JLabel("0");

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.BOX, 22, new Color(37, 99, 235)),
                "MẶT HÀNG TRONG KHO", lblTongMatHang, "Danh mục phụ tùng quản lý",
                new Color(37, 99, 235)
        ));

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.EXPORT, 22, new Color(225, 29, 72)),
                "CHỜ XUẤT KHO", lblChoXuat, "Yêu cầu từ xưởng sửa chữa",
                new Color(225, 29, 72)
        ));

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.IMPORT, 22, new Color(245, 158, 11)),
                "CHỜ NHẬP KHO", lblChoNhap, "Đơn đặt hàng đang giao",
                new Color(245, 158, 11)
        ));

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.SHIELD, 22, new Color(124, 58, 237)),
                "CẢNH BÁO TỒN KHO", lblCanhBaoTon, "Dưới định mức an toàn",
                new Color(124, 58, 237)
        ));

        return grid;
    }

    private JPanel theKpiNho(Icon icon, String tieuDe, JLabel lblSo, String moTa, Color mauVien) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(mauVien);
                g2.fillRoundRect(0, 0, getWidth(), 4, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new JLabel(icon));
        card.add(iconWrap, BorderLayout.WEST);

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

        return card;
    }

    private JPanel taoTabsNghiepVu() {
        JPanel panel = UiHelper.panelBang("Nhiệm vụ quản lý kho & cấp phát vật tư");

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // TAB 1: Yêu cầu chờ xuất kho
        JPanel tab1 = new JPanel(new BorderLayout(0, 8));
        tab1.setOpaque(false);
        tab1.setBorder(new EmptyBorder(8, 0, 0, 0));

        modelXuat = UiHelper.model("Mã phiếu xuất", "Phiếu sửa chữa", "Biển số xe", "Ngày xuất", "Trạng thái", "Lý do xuất");
        tblXuat = UiHelper.bang(modelXuat);
        tblXuat.getColumnModel().getColumn(0).setPreferredWidth(95);
        tblXuat.getColumnModel().getColumn(1).setPreferredWidth(95);
        tblXuat.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblXuat.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblXuat.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblXuat.getColumnModel().getColumn(5).setPreferredWidth(220);
        UiHelper.canGiua(tblXuat, 0, 1, 2, 3, 4);
        UiHelper.canTrai(tblXuat, 5);

        tblXuat.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!s) {
                    l.setForeground(new Color(225, 29, 72));
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                }
                return l;
            }
        });

        JScrollPane sp1 = new JScrollPane(tblXuat);
        UiHelper.danhSachBong(sp1);
        tab1.add(sp1, BorderLayout.CENTER);

        JPanel bot1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bot1.setOpaque(false);
        JButton btnMoXuat = UiHelper.nut("Mở phân hệ Xuất kho", UiIcon.of(UiIcon.EXPORT, 14), UiHelper.XANH_DAM, e -> chuyenTrang("Xuất kho"));
        bot1.add(btnMoXuat);
        tab1.add(bot1, BorderLayout.SOUTH);
        tabs.addTab("Yêu cầu chờ xuất kho", tab1);

        // TAB 2: Đơn hàng chờ nhập kho
        JPanel tab2 = new JPanel(new BorderLayout(0, 8));
        tab2.setOpaque(false);
        tab2.setBorder(new EmptyBorder(8, 0, 0, 0));

        modelNhap = UiHelper.model("Mã phiếu nhập", "Nhà cung cấp", "Ngày dự kiến giao", "Tổng tiền", "Trạng thái");
        tblNhap = UiHelper.bang(modelNhap);
        tblNhap.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblNhap.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblNhap.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblNhap.getColumnModel().getColumn(3).setPreferredWidth(130);
        tblNhap.getColumnModel().getColumn(4).setPreferredWidth(110);
        UiHelper.canGiua(tblNhap, 0, 2, 4);
        UiHelper.canTrai(tblNhap, 1);
        UiHelper.canPhai(tblNhap, 3);

        tblNhap.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!s) {
                    l.setForeground(new Color(217, 119, 6));
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                }
                return l;
            }
        });

        JScrollPane sp2 = new JScrollPane(tblNhap);
        UiHelper.danhSachBong(sp2);
        tab2.add(sp2, BorderLayout.CENTER);

        JPanel bot2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bot2.setOpaque(false);
        JButton btnMoNhap = UiHelper.nut("Mở phân hệ Nhập kho", UiIcon.of(UiIcon.IMPORT, 14), UiHelper.XANH_TRUNG, e -> chuyenTrang("Nhập kho"));
        bot2.add(btnMoNhap);
        tab2.add(bot2, BorderLayout.SOUTH);
        tabs.addTab("Đơn hàng chờ nhập kho", tab2);

        // TAB 3: Phụ tùng cảnh báo sắp hết
        JPanel tab3 = new JPanel(new BorderLayout(0, 8));
        tab3.setOpaque(false);
        tab3.setBorder(new EmptyBorder(8, 0, 0, 0));

        modelCanhBao = UiHelper.model("Mã phụ tùng", "Tên phụ tùng", "Tồn hiện tại", "Định mức tối thiểu", "Vị trí kệ", "Đơn vị tính");
        tblCanhBao = UiHelper.bang(modelCanhBao);
        tblCanhBao.getColumnModel().getColumn(0).setPreferredWidth(85);
        tblCanhBao.getColumnModel().getColumn(1).setPreferredWidth(220);
        tblCanhBao.getColumnModel().getColumn(2).setPreferredWidth(95);
        tblCanhBao.getColumnModel().getColumn(3).setPreferredWidth(125);
        tblCanhBao.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblCanhBao.getColumnModel().getColumn(5).setPreferredWidth(90);
        UiHelper.canGiua(tblCanhBao, 0, 2, 3, 4, 5);
        UiHelper.canTrai(tblCanhBao, 1);

        tblCanhBao.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!s) {
                    l.setForeground(new Color(220, 38, 38));
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                }
                return l;
            }
        });

        JScrollPane sp3 = new JScrollPane(tblCanhBao);
        UiHelper.danhSachBong(sp3);
        tab3.add(sp3, BorderLayout.CENTER);

        JPanel bot3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bot3.setOpaque(false);
        JButton btnBaoCaoTon = UiHelper.nut("Báo cáo tồn kho chi tiết", UiIcon.of(UiIcon.CHART, 14), UiHelper.XAM, true, e -> chuyenTrang("Báo cáo tồn kho"));
        bot3.add(btnBaoCaoTon);
        tab3.add(bot3, BorderLayout.SOUTH);
        tabs.addTab("Cảnh báo tồn an toàn", tab3);

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel taoCotPhai() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setPreferredSize(new Dimension(340, 0));

        // 1. Khối Cảnh báo & Thông báo Kho
        JPanel pnlCanhBao = UiHelper.panelForm("Thông báo & Cảnh báo Kho", 340);
        JPanel boxCanhBao = new JPanel();
        boxCanhBao.setOpaque(false);
        boxCanhBao.setLayout(new BoxLayout(boxCanhBao, BoxLayout.Y_AXIS));

        lblCanhBaoTonKho = new JLabel("Đang kiểm tra tồn kho...");
        lblCanhBaoTonKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCanhBaoTonKho.setForeground(new Color(51, 65, 85));

        lblThongBaoXuatKho = new JLabel("Đang kiểm tra yêu cầu xuất...");
        lblThongBaoXuatKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBaoXuatKho.setForeground(new Color(51, 65, 85));

        boxCanhBao.add(taoTheThongBao(UiIcon.of(UiIcon.SHIELD, 18, new Color(225, 29, 72)), "Hàng chạm đáy an toàn", lblCanhBaoTonKho, new Color(254, 242, 242)));
        boxCanhBao.add(Box.createVerticalStrut(8));
        boxCanhBao.add(taoTheThongBao(UiIcon.of(UiIcon.EXPORT, 18, new Color(245, 158, 11)), "Yêu cầu cấp phát xưởng", lblThongBaoXuatKho, new Color(254, 243, 199)));
        boxCanhBao.add(Box.createVerticalStrut(8));
        boxCanhBao.add(taoTheThongBao(UiIcon.of(UiIcon.BOX, 18, new Color(37, 99, 235)), "Quy chuẩn xếp dỡ kho",
                new JLabel("<html><i>Sắp xếp đúng ô kệ (Kệ 1-4), dán nhãn mã vạch và phân loại linh kiện điện tử tránh ẩm mốc.</i></html>"), new Color(239, 246, 255)));

        pnlCanhBao.add(boxCanhBao, BorderLayout.CENTER);
        wrap.add(pnlCanhBao);
        wrap.add(Box.createVerticalStrut(10));

        // 2. Khối Thao tác nhanh
        JPanel pnlTacVu = UiHelper.panelForm("Lối tắt thao tác kho", 340);
        JPanel gridBtns = new JPanel(new GridLayout(5, 1, 6, 6));
        gridBtns.setOpaque(false);

        JButton btnXuat = UiHelper.nut("  Lập phiếu xuất kho", UiIcon.of(UiIcon.EXPORT, 15), UiHelper.XANH_DAM, e -> chuyenTrang("Xuất kho"));
        JButton btnNhap = UiHelper.nut("  Lập phiếu nhập kho", UiIcon.of(UiIcon.IMPORT, 15), UiHelper.XANH_TRUNG, e -> chuyenTrang("Nhập kho"));
        JButton btnChoNhap = UiHelper.nut("  Theo dõi hàng chờ nhập", UiIcon.of(UiIcon.CLOCK, 15), UiHelper.CAM, true, e -> chuyenTrang("Theo dõi chờ nhập"));
        JButton btnBCTon = UiHelper.nut("  Báo cáo tồn kho an toàn", UiIcon.of(UiIcon.CHART, 15), UiHelper.XAM, true, e -> chuyenTrang("Báo cáo tồn kho"));
        JButton btnLichSu = UiHelper.nut("  Lịch sử nhập xuất kho", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XAM, true, e -> chuyenTrang("Lịch sử nhập xuất kho"));

        gridBtns.add(btnXuat);
        gridBtns.add(btnNhap);
        gridBtns.add(btnChoNhap);
        gridBtns.add(btnBCTon);
        gridBtns.add(btnLichSu);

        pnlTacVu.add(gridBtns, BorderLayout.CENTER);
        wrap.add(pnlTacVu);

        return wrap;
    }

    private JPanel taoTheThongBao(Icon icon, String tieuDe, JComponent noiDung, Color mauNen) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauNen);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel iconP = new JPanel(new GridBagLayout());
        iconP.setOpaque(false);
        iconP.add(new JLabel(icon));
        card.add(iconP, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel lTieuDe = new JLabel(tieuDe);
        lTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lTieuDe.setForeground(UiHelper.XANH_DAM);

        text.add(lTieuDe);
        text.add(Box.createVerticalStrut(2));
        text.add(noiDung);
        card.add(text, BorderLayout.CENTER);

        return card;
    }

    public void napDuLieuAsync() {
        lblCapNhatLuc.setText("Đang tải dữ liệu...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Map<String, Object> stat;
            private ArrayList<Map<String, Object>> xuatList;
            private ArrayList<Map<String, Object>> nhapList;
            private ArrayList<Map<String, Object>> cbList;

            @Override
            protected Void doInBackground() {
                stat = dao.layThongKeKho();
                xuatList = dao.layDanhSachChoXuatKho();
                nhapList = dao.layDanhSachChoNhapKho();
                cbList = dao.layDanhSachPhuTungCanhBao();
                return null;
            }

            @Override
            protected void done() {
                if (stat != null) {
                    lblTongMatHang.setText(String.valueOf(stat.getOrDefault("tongMatHang", 0)));
                    lblChoXuat.setText(String.valueOf(stat.getOrDefault("choXuat", 0)));
                    lblChoNhap.setText(String.valueOf(stat.getOrDefault("choNhap", 0)));

                    int cbTon = (int) stat.getOrDefault("canhBaoTon", 0);
                    lblCanhBaoTon.setText(String.valueOf(cbTon));

                    if (cbTon > 0) {
                        lblCanhBaoTonKho.setText("<html><font color='#DC2626'><b>Cảnh báo: " + cbTon + " mặt hàng</b></font> chạm đáy an toàn, cần nhập thêm!</html>");
                    } else {
                        lblCanhBaoTonKho.setText("Tồn kho các mặt hàng đảm bảo an toàn.");
                    }

                    int choXuat = (int) stat.getOrDefault("choXuat", 0);
                    if (choXuat > 0) {
                        lblThongBaoXuatKho.setText("<html>Có <b>" + choXuat + " phiếu xuất kho</b> đang chờ cấp phát cho thợ.</html>");
                    } else {
                        lblThongBaoXuatKho.setText("Đã xuất kho đầy đủ, không tồn đọng.");
                    }
                }

                // Cập nhật tab 1: Chờ xuất
                dsXuat.clear();
                modelXuat.setRowCount(0);
                if (xuatList != null) {
                    dsXuat.addAll(xuatList);
                    for (Map<String, Object> r : xuatList) {
                        LocalDate d = (LocalDate) r.get("ngayXuat");
                        String ngayStr = d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
                        modelXuat.addRow(new Object[]{
                                r.get("maPhieuXuat"),
                                r.get("maPSC"),
                                r.get("bienSoXe"),
                                ngayStr,
                                r.get("trangThai"),
                                r.get("lyDoXuat")
                        });
                    }
                }

                // Cập nhật tab 2: Chờ nhập
                dsNhap.clear();
                modelNhap.setRowCount(0);
                if (nhapList != null) {
                    dsNhap.addAll(nhapList);
                    for (Map<String, Object> r : nhapList) {
                        LocalDate d = (LocalDate) r.get("ngayGiao");
                        String ngayStr = d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
                        long tien = (long) r.getOrDefault("tongTien", 0L);
                        modelNhap.addRow(new Object[]{
                                r.get("maPhieuNhap"),
                                r.get("tenNCC"),
                                ngayStr,
                                UiHelper.tien(tien) + " đ",
                                r.get("trangThai")
                        });
                    }
                }

                // Cập nhật tab 3: Cảnh báo tồn
                dsCanhBao.clear();
                modelCanhBao.setRowCount(0);
                if (cbList != null) {
                    dsCanhBao.addAll(cbList);
                    for (Map<String, Object> r : cbList) {
                        modelCanhBao.addRow(new Object[]{
                                r.get("maPT"),
                                r.get("tenPT"),
                                r.get("soLuongTon"),
                                r.get("soLTT"),
                                r.get("viTriKho"),
                                r.get("donViTinh")
                        });
                    }
                }

                String gio = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                lblCapNhatLuc.setText("Cập nhật: " + gio);
            }
        };
        worker.execute();
    }

    private void chuyenTrang(String tenTrang) {
        Main_UI m = mainUI != null ? mainUI : Main_UI.getInstance();
        if (m != null) {
            m.chuyenTrang(tenTrang);
        } else {
            JOptionPane.showMessageDialog(this, "Chuyển tới: " + tenTrang);
        }
    }
}
