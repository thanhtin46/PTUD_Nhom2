package UI;

import DAO.TrangChuBoPhan_DAO;
import Entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

/**
 * Màn hình trang chủ dành cho kỹ thuật viên sửa chữa.
 */
public class TrangChuSuaChua_UI extends JPanel {

    private final NhanVien nvHienTai;
    private final Main_UI mainUI;
    private final TrangChuBoPhan_DAO dao = new TrangChuBoPhan_DAO();

    // KPI labels
    private JLabel lblTongDangLam, lblDangSua, lblChoVatTu, lblCanGiaoGap;
    private JLabel lblCapNhatLuc;

    // Bảng phiếu sửa chữa
    private DefaultTableModel modelPhieu;
    private JTable tblPhieu;
    private final ArrayList<Map<String, Object>> dsPhieuHienThi = new ArrayList<>();

    // Khối thông báo
    private JLabel lblCanhBaoTienDo;
    private JLabel lblThongBaoVatTu;

    public TrangChuSuaChua_UI(NhanVien nv, Main_UI mainUI) {
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

        // 2.2. Khu vực giữa: Bảng việc bên trái + Cảnh báo & Thao tác nhanh bên phải
        JPanel midPanel = new JPanel(new BorderLayout(14, 0));
        midPanel.setOpaque(false);
        midPanel.add(taoBangCongViec(), BorderLayout.CENTER);
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

        String tenNv = (nvHienTai != null && nvHienTai.getHoTen() != null) ? nvHienTai.getHoTen() : "Kỹ thuật viên";
        String maNv = (nvHienTai != null && nvHienTai.getMaNV() != null) ? nvHienTai.getMaNV() : "KTV";

        JLabel tde = UiHelper.tieuDeTrang("Bàn làm việc Kỹ thuật & Sửa chữa");
        JLabel phu = new JLabel("KTV phụ trách: " + maNv + " - " + tenNv + " • Theo dõi tiến độ xe phân công và xuất kho vật tư");
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

        lblTongDangLam = new JLabel("0");
        lblDangSua = new JLabel("0");
        lblChoVatTu = new JLabel("0");
        lblCanGiaoGap = new JLabel("0");

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.WRENCH, 22, new Color(37, 99, 235)),
                "PHIẾU ĐANG PHỤ TRÁCH", lblTongDangLam, "Xe được giao trong xưởng",
                new Color(37, 99, 235)
        ));

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.CAR, 22, new Color(14, 165, 233)),
                "ĐANG SỬA CHỮA", lblDangSua, "Đang tháo lắp & căn chỉnh",
                new Color(14, 165, 233)
        ));

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.BOX, 22, new Color(245, 158, 11)),
                "CHỜ XUẤT VẬT TƯ", lblChoVatTu, "Đang đợi thủ kho cấp phát",
                new Color(245, 158, 11)
        ));

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.CLOCK, 22, new Color(225, 29, 72)),
                "CẦN GIAO GẤP / QUÁ HẸN", lblCanGiaoGap, "Hạn hôm nay hoặc trễ hẹn",
                new Color(225, 29, 72)
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

    private JPanel taoBangCongViec() {
        JPanel panel = UiHelper.panelBang("Danh sách xe được phân công sửa chữa");

        modelPhieu = UiHelper.model(
                "Mã phiếu", "Biển số xe", "Hãng / Dòng xe", "Khách hàng",
                "Yêu cầu sửa chữa", "Hạn hoàn thành", "Tiến độ", "Vật tư kho"
        );
        tblPhieu = UiHelper.bang(modelPhieu);

        tblPhieu.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblPhieu.getColumnModel().getColumn(1).setPreferredWidth(95);
        tblPhieu.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblPhieu.getColumnModel().getColumn(3).setPreferredWidth(130);
        tblPhieu.getColumnModel().getColumn(4).setPreferredWidth(180);
        tblPhieu.getColumnModel().getColumn(5).setPreferredWidth(110);
        tblPhieu.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblPhieu.getColumnModel().getColumn(7).setPreferredWidth(105);

        UiHelper.canGiua(tblPhieu, 0, 1, 5, 6, 7);
        UiHelper.canTrai(tblPhieu, 2, 3, 4);

        tblPhieu.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                if (!isSelected && row < dsPhieuHienThi.size()) {
                    Map<String, Object> r = dsPhieuHienThi.get(row);
                    LocalDate ngayHen = (LocalDate) r.get("ngayHen");
                    String trangThai = String.valueOf(r.get("trangThai"));
                    boolean chuaHT = !trangThai.contains("HOAN_THANH");

                    if (chuaHT && ngayHen != null && ngayHen.isBefore(LocalDate.now())) {
                        l.setBackground(new Color(254, 242, 242)); // Hồng nhạt quá hẹn
                    } else if (chuaHT && ngayHen != null && ngayHen.isEqual(LocalDate.now())) {
                        l.setBackground(new Color(254, 243, 199)); // Vàng nhạt hôm nay
                    } else {
                        l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }

                    if (col == 5 && chuaHT && ngayHen != null && ngayHen.isBefore(LocalDate.now())) {
                        l.setForeground(new Color(220, 38, 38));
                        l.setFont(l.getFont().deriveFont(Font.BOLD));
                    } else if (col == 6) {
                        if (trangThai.contains("HOAN_THANH")) {
                            l.setForeground(new Color(22, 163, 74));
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                        } else if (trangThai.contains("DANG_SUA")) {
                            l.setForeground(new Color(37, 99, 235));
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                        } else {
                            l.setForeground(new Color(100, 116, 139));
                        }
                    } else if (col == 7) {
                        String vt = String.valueOf(r.get("trangThaiXuatKho"));
                        if (vt.contains("Chờ xuất") || vt.contains("CHO_XUAT")) {
                            l.setForeground(new Color(217, 119, 6));
                            l.setFont(l.getFont().deriveFont(Font.BOLD));
                        } else if (vt.contains("Đã xuất") || vt.contains("Da xu")) {
                            l.setForeground(new Color(22, 163, 74));
                        } else {
                            l.setForeground(new Color(148, 163, 184));
                        }
                    } else {
                        l.setForeground(new Color(30, 41, 59));
                        l.setFont(l.getFont().deriveFont(Font.PLAIN));
                    }
                }

                if (col == 0 || col == 1 || col == 5 || col == 6 || col == 7) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                }

                return l;
            }
        });

        tblPhieu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblPhieu.getSelectedRow() >= 0) {
                    moChiTietPhieuDangChon();
                }
            }
        });

        JScrollPane sp = new JScrollPane(tblPhieu);
        UiHelper.danhSachBong(sp);
        panel.add(sp, BorderLayout.CENTER);

        // Thanh thao tác dưới bảng
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(8, 4, 4, 4));

        JLabel lblHuongDan = new JLabel("Nhấp đúp vào dòng để mở chi tiết phiếu sửa chữa & phiếu xuất phụ tùng");
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHuongDan.setForeground(new Color(100, 116, 139));
        bot.add(lblHuongDan, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setOpaque(false);

        JButton btnMoPSC = UiHelper.nut("Mở phiếu sửa chữa", UiIcon.of(UiIcon.WRENCH, 14), UiHelper.XANH_DAM, e -> moChiTietPhieuDangChon());
        JButton btnCapNhatTD = UiHelper.nut("Cập nhật tiến độ", UiIcon.of(UiIcon.CHECK, 14), UiHelper.XANH_TRUNG, e -> chuyenTrang("Cập nhật tiến độ"));

        actionBtns.add(btnCapNhatTD);
        actionBtns.add(btnMoPSC);
        bot.add(actionBtns, BorderLayout.EAST);

        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel taoCotPhai() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setPreferredSize(new Dimension(340, 0));

        // 1. Khối Cảnh báo & Thông báo Kỹ thuật
        JPanel pnlCanhBao = UiHelper.panelForm("Thông báo & Cảnh báo Kỹ thuật", 340);
        JPanel boxCanhBao = new JPanel();
        boxCanhBao.setOpaque(false);
        boxCanhBao.setLayout(new BoxLayout(boxCanhBao, BoxLayout.Y_AXIS));

        lblCanhBaoTienDo = new JLabel("Đang kiểm tra tiến độ xưởng...");
        lblCanhBaoTienDo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCanhBaoTienDo.setForeground(new Color(51, 65, 85));

        lblThongBaoVatTu = new JLabel("Đang kiểm tra vật tư phụ tùng...");
        lblThongBaoVatTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBaoVatTu.setForeground(new Color(51, 65, 85));

        boxCanhBao.add(taoTheThongBao(UiIcon.of(UiIcon.CLOCK, 18, new Color(225, 29, 72)), "Hạn giao xe", lblCanhBaoTienDo, new Color(254, 242, 242)));
        boxCanhBao.add(Box.createVerticalStrut(8));
        boxCanhBao.add(taoTheThongBao(UiIcon.of(UiIcon.BOX, 18, new Color(245, 158, 11)), "Cấp phát phụ tùng", lblThongBaoVatTu, new Color(254, 243, 199)));
        boxCanhBao.add(Box.createVerticalStrut(8));
        boxCanhBao.add(taoTheThongBao(UiIcon.of(UiIcon.SHIELD, 18, new Color(22, 163, 74)), "Quy chuẩn an toàn 5S",
                new JLabel("<html><i>Kiểm tra áp suất lốp, siết chuẩn lực bu-lông và vệ sinh khoang máy trước khi bàn giao xe.</i></html>"), new Color(240, 253, 244)));

        pnlCanhBao.add(boxCanhBao, BorderLayout.CENTER);
        wrap.add(pnlCanhBao);
        wrap.add(Box.createVerticalStrut(10));

        // 2. Khối Thao tác nhanh
        JPanel pnlTacVu = UiHelper.panelForm("Lối tắt thao tác nhanh", 340);
        JPanel gridBtns = new JPanel(new GridLayout(4, 1, 6, 6));
        gridBtns.setOpaque(false);

        JButton btnTD = UiHelper.nut("  Cập nhật tiến độ sửa chữa", UiIcon.of(UiIcon.CHECK, 15), UiHelper.XANH_TRUNG, e -> chuyenTrang("Cập nhật tiến độ"));
        JButton btnLapPSC = UiHelper.nut("  Lập / Chỉnh sửa phiếu sửa chữa", UiIcon.of(UiIcon.WRENCH, 15), UiHelper.XANH_DAM, e -> chuyenTrang("Lập phiếu sửa chữa"));
        JButton btnLichSu = UiHelper.nut("  Tra cứu lịch sử sửa chữa xe", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XAM, true, e -> chuyenTrang("Tra cứu lịch sử sửa chữa"));
        JButton btnPhuTung = UiHelper.nut("  Danh mục phụ tùng xưởng", UiIcon.of(UiIcon.BOX, 15), UiHelper.XAM, true, e -> chuyenTrang("Danh sách phụ tùng"));

        gridBtns.add(btnTD);
        gridBtns.add(btnLapPSC);
        gridBtns.add(btnLichSu);
        gridBtns.add(btnPhuTung);

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
            private ArrayList<Map<String, Object>> phieuList;

            @Override
            protected Void doInBackground() {
                String maKtv = (nvHienTai != null) ? nvHienTai.getMaNV() : null;
                stat = dao.layThongKeSuaChua(maKtv);
                phieuList = dao.layDanhSachPhieuSuaChuaKTV(maKtv);
                return null;
            }

            @Override
            protected void done() {
                if (stat != null) {
                    lblTongDangLam.setText(String.valueOf(stat.getOrDefault("tongDangLam", 0)));
                    lblDangSua.setText(String.valueOf(stat.getOrDefault("dangSua", 0)));
                    lblChoVatTu.setText(String.valueOf(stat.getOrDefault("choVatTu", 0)));

                    int canGiao = (int) stat.getOrDefault("canGiaoGap", 0);
                    lblCanGiaoGap.setText(String.valueOf(canGiao));

                    if (canGiao > 0) {
                        lblCanhBaoTienDo.setText("<html><font color='#DC2626'><b>Cần giao gấp: " + canGiao + " xe</b></font> trễ hạn hoặc đến hạn hôm nay!</html>");
                    } else {
                        lblCanhBaoTienDo.setText("Tiến độ xưởng ổn định, không có xe trễ hạn.");
                    }

                    int choVatTu = (int) stat.getOrDefault("choVatTu", 0);
                    if (choVatTu > 0) {
                        lblThongBaoVatTu.setText("<html>Có <b>" + choVatTu + " xe</b> đang chờ thủ kho xuất linh kiện.</html>");
                    } else {
                        lblThongBaoVatTu.setText("Tất cả linh kiện đã được cấp phát đầy đủ.");
                    }
                }

                dsPhieuHienThi.clear();
                modelPhieu.setRowCount(0);
                if (phieuList != null) {
                    dsPhieuHienThi.addAll(phieuList);
                    for (Map<String, Object> r : phieuList) {
                        LocalDate d = (LocalDate) r.get("ngayHen");
                        String ngayHenStr = d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Chưa có hẹn";
                        String tt = String.valueOf(r.get("trangThai"));
                        String ttHienThi = tt.contains("HOAN_THANH") ? "Hoàn thành" : (tt.contains("DANG_SUA") ? "Đang sửa" : "Chờ sửa");

                        modelPhieu.addRow(new Object[]{
                                r.get("maPSC"),
                                r.get("bienSoXe"),
                                r.get("tenXe"),
                                r.get("tenKH"),
                                r.get("yeuCauSC"),
                                ngayHenStr,
                                ttHienThi,
                                r.get("trangThaiXuatKho")
                        });
                    }
                }

                String gio = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                lblCapNhatLuc.setText("Cập nhật: " + gio);
            }
        };
        worker.execute();
    }

    private void moChiTietPhieuDangChon() {
        int r = tblPhieu.getSelectedRow();
        if (r < 0 || r >= dsPhieuHienThi.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu sửa chữa trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPSC = String.valueOf(dsPhieuHienThi.get(r).get("maPSC"));
        LapPhieuSuaChua_UI.datPSCCanMoLai(maPSC);
        chuyenTrang("Lập phiếu sửa chữa");
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
