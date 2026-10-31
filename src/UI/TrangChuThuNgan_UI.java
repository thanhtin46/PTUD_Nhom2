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
 * Màn hình trang chủ dành cho nhân viên thu ngân.
 */
public class TrangChuThuNgan_UI extends JPanel {

    private final NhanVien nvHienTai;
    private final Main_UI mainUI;
    private final TrangChuBoPhan_DAO dao = new TrangChuBoPhan_DAO();

    // KPI labels
    private JLabel lblChoThanhToan, lblHoaDonHomNay, lblDoanhThuHomNay, lblBanLeHomNay;
    private JLabel lblCapNhatLuc;

    // Bảng dữ liệu
    private DefaultTableModel modelChoTT, modelHoaDon;
    private JTable tblChoTT, tblHoaDon;
    private final ArrayList<Map<String, Object>> dsChoTT = new ArrayList<>();
    private final ArrayList<Map<String, Object>> dsHD = new ArrayList<>();

    // Thông báo
    private JLabel lblThongBaoChoTT;
    private JLabel lblThongBaoDoanhThu;

    public TrangChuThuNgan_UI(NhanVien nv, Main_UI mainUI) {
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

        String tenNv = (nvHienTai != null && nvHienTai.getHoTen() != null) ? nvHienTai.getHoTen() : "Thu ngân";
        String maNv = (nvHienTai != null && nvHienTai.getMaNV() != null) ? nvHienTai.getMaNV() : "TN";

        JLabel tde = UiHelper.tieuDeTrang("Bàn làm việc Thu ngân & Thanh toán");
        JLabel phu = new JLabel("Thu ngân phụ trách: " + maNv + " - " + tenNv + " • Lập hóa đơn dịch vụ, thanh toán bán lẻ và đối soát doanh thu");
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

        lblChoThanhToan = new JLabel("0");
        lblHoaDonHomNay = new JLabel("0");
        lblDoanhThuHomNay = new JLabel("0 đ");
        lblBanLeHomNay = new JLabel("0");

        JPanel cardChoTT = theKpiNho(
                UiIcon.of(UiIcon.CAR, 22, new Color(225, 29, 72)),
                "XE CHỜ THANH TOÁN", lblChoThanhToan, "Đã sửa xong chờ thanh toán",
                new Color(225, 29, 72)
        );
        cardChoTT.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cardChoTT.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { thanhToanXeDangChon(); }
        });
        grid.add(cardChoTT);

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.BILL, 22, new Color(37, 99, 235)),
                "HÓA ĐƠN HÔM NAY", lblHoaDonHomNay, "Hóa đơn đã xuất trong ngày",
                new Color(37, 99, 235)
        ));

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.CHECK, 22, new Color(22, 163, 74)),
                "DOANH THU THỰC THU", lblDoanhThuHomNay, "Tiền thực thu hôm nay",
                new Color(22, 163, 74)
        ));

        grid.add(theKpiNho(
                UiIcon.of(UiIcon.BOX, 22, new Color(245, 158, 11)),
                "BÁN LẺ PHỤ TÙNG", lblBanLeHomNay, "Giao dịch quầy shop",
                new Color(245, 158, 11)
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
        JPanel panel = UiHelper.panelBang("Danh sách xe sửa chữa hoàn thành chờ thanh toán");

        modelChoTT = UiHelper.model(
                "Mã phiếu SC", "Phiếu tiếp nhận", "Biển số xe", "Tên khách hàng", "Số điện thoại", "Ngày hoàn thành", "Trạng thái"
        );
        tblChoTT = UiHelper.bang(modelChoTT);

        tblChoTT.getColumnModel().getColumn(0).setPreferredWidth(90);
        tblChoTT.getColumnModel().getColumn(1).setPreferredWidth(95);
        tblChoTT.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblChoTT.getColumnModel().getColumn(3).setPreferredWidth(150);
        tblChoTT.getColumnModel().getColumn(4).setPreferredWidth(105);
        tblChoTT.getColumnModel().getColumn(5).setPreferredWidth(110);
        tblChoTT.getColumnModel().getColumn(6).setPreferredWidth(110);

        UiHelper.canGiua(tblChoTT, 0, 1, 2, 4, 5, 6);
        UiHelper.canTrai(tblChoTT, 3);

        tblChoTT.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!s) {
                    l.setForeground(new Color(22, 163, 74));
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                }
                return l;
            }
        });

        tblChoTT.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblChoTT.getSelectedRow() >= 0) {
                    thanhToanXeDangChon();
                }
            }
        });

        JScrollPane sp = new JScrollPane(tblChoTT);
        UiHelper.danhSachBong(sp);
        panel.add(sp, BorderLayout.CENTER);

        // Thanh thao tác dưới bảng
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(8, 4, 4, 4));

        JLabel lblHuongDan = new JLabel("Nhấp đúp chuột vào một xe để mở ngay màn hình thanh toán hóa đơn");
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHuongDan.setForeground(new Color(100, 116, 139));
        bot.add(lblHuongDan, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setOpaque(false);

        JButton btnThanhToan = UiHelper.nut("Thanh toán xe đã chọn", UiIcon.of(UiIcon.BILL, 14), UiHelper.XANH_DAM, e -> thanhToanXeDangChon());
        JButton btnBanLe = UiHelper.nut("Bán lẻ phụ tùng", UiIcon.of(UiIcon.BOX, 14), UiHelper.CAM, true, e -> chuyenTrangBanLe());

        actionBtns.add(btnBanLe);
        actionBtns.add(btnThanhToan);
        bot.add(actionBtns, BorderLayout.EAST);

        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel taoCotPhai() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setPreferredSize(new Dimension(340, 0));

        // 1. Khối Cảnh báo & Thông báo Thu ngân
        JPanel pnlCanhBao = UiHelper.panelForm("Thông báo & Cảnh báo Thu ngân", 340);
        JPanel boxCanhBao = new JPanel();
        boxCanhBao.setOpaque(false);
        boxCanhBao.setLayout(new BoxLayout(boxCanhBao, BoxLayout.Y_AXIS));

        lblThongBaoChoTT = new JLabel("Đang kiểm tra xe chờ thanh toán...");
        lblThongBaoChoTT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBaoChoTT.setForeground(new Color(51, 65, 85));

        lblThongBaoDoanhThu = new JLabel("Đang đối soát doanh thu...");
        lblThongBaoDoanhThu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBaoDoanhThu.setForeground(new Color(51, 65, 85));

        JPanel theKhachDoi = taoTheThongBao(UiIcon.of(UiIcon.CAR, 18, new Color(225, 29, 72)), "Khách đợi giao xe", lblThongBaoChoTT, new Color(254, 242, 242));
        theKhachDoi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        theKhachDoi.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { thanhToanXeDangChon(); }
        });
        boxCanhBao.add(theKhachDoi);
        boxCanhBao.add(Box.createVerticalStrut(8));
        boxCanhBao.add(taoTheThongBao(UiIcon.of(UiIcon.BILL, 18, new Color(22, 163, 74)), "Doanh thu hôm nay", lblThongBaoDoanhThu, new Color(240, 253, 244)));
        boxCanhBao.add(Box.createVerticalStrut(8));
        boxCanhBao.add(taoTheThongBao(UiIcon.of(UiIcon.SHIELD, 18, new Color(37, 99, 235)), "Quy chuẩn xuất hóa đơn",
                new JLabel("<html><i>Kiểm tra kỹ phiếu bảo hành kèm theo, hướng dẫn khách thanh toán VietQR và ký biên bản giao xe.</i></html>"), new Color(239, 246, 255)));

        pnlCanhBao.add(boxCanhBao, BorderLayout.CENTER);
        wrap.add(pnlCanhBao);
        wrap.add(Box.createVerticalStrut(10));

        // 2. Khối Thao tác nhanh
        JPanel pnlTacVu = UiHelper.panelForm("Lối tắt thao tác thu ngân", 340);
        JPanel gridBtns = new JPanel(new GridLayout(4, 1, 6, 6));
        gridBtns.setOpaque(false);

        JButton btnTTSC = UiHelper.nut("  Thanh toán sửa chữa xe", UiIcon.of(UiIcon.BILL, 15), UiHelper.XANH_DAM, e -> chuyenTrang("Thanh toán hóa đơn"));
        JButton btnTTBL = UiHelper.nut("  Lập hóa đơn bán lẻ Shop", UiIcon.of(UiIcon.BOX, 15), UiHelper.CAM, true, e -> chuyenTrangBanLe());
        BaoVeQuyen.ganQuyen(btnTTBL, Entity.QuyenTruyCap.Quyen.THANH_TOAN);
        JButton btnTraCuuHD = UiHelper.nut("  Tra cứu & In lại hóa đơn", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> chuyenTrang("Tra cứu hóa đơn"));
        JButton btnBaoCao = UiHelper.nut("  Báo cáo doanh thu xưởng", UiIcon.of(UiIcon.CHART, 15), UiHelper.XAM, true, e -> chuyenTrang("Báo cáo — Thống kê"));

        gridBtns.add(btnTTSC);
        gridBtns.add(btnTTBL);
        gridBtns.add(btnTraCuuHD);
        gridBtns.add(btnBaoCao);

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
            private ArrayList<Map<String, Object>> choList;

            @Override
            protected Void doInBackground() {
                stat = dao.layThongKeThuNgan();
                choList = dao.layDanhSachChoThanhToan();
                return null;
            }

            @Override
            protected void done() {
                if (stat != null) {
                    int choTT = (int) stat.getOrDefault("choThanhToan", 0);
                    int soHD = (int) stat.getOrDefault("hoaDonHomNay", 0);
                    long doanhThu = (long) stat.getOrDefault("doanhThuHomNay", 0L);
                    int banLe = (int) stat.getOrDefault("banLeHomNay", 0);

                    lblChoThanhToan.setText(String.valueOf(choTT));
                    lblHoaDonHomNay.setText(String.valueOf(soHD));
                    lblDoanhThuHomNay.setText(UiHelper.tien(doanhThu) + " đ");
                    lblBanLeHomNay.setText(String.valueOf(banLe));

                    if (choTT > 0) {
                        lblThongBaoChoTT.setText("<html><font color='#DC2626'><b>Có " + choTT + " xe</b></font> sửa xong đang đợi thanh toán & giao xe.</html>");
                    } else {
                        lblThongBaoChoTT.setText("Không có xe tồn đọng chờ thanh toán.");
                    }

                    lblThongBaoDoanhThu.setText("<html>Đã thu: <b>" + UiHelper.tien(doanhThu) + " đ</b> (" + soHD + " hóa đơn hôm nay)</html>");
                }

                dsChoTT.clear();
                modelChoTT.setRowCount(0);
                if (choList != null) {
                    dsChoTT.addAll(choList);
                    for (Map<String, Object> r : choList) {
                        LocalDate d = (LocalDate) r.get("ngayHoanThanh");
                        String ngayStr = d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";

                        modelChoTT.addRow(new Object[]{
                                r.get("maPSC"),
                                r.get("maPTN"),
                                r.get("bienSoXe"),
                                r.get("tenKH"),
                                r.get("soDT"),
                                ngayStr,
                                "Chờ thanh toán"
                        });
                    }
                }

                String gio = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                lblCapNhatLuc.setText("Cập nhật: " + gio);
            }
        };
        worker.execute();
    }

    private void thanhToanXeDangChon() {
        int r = tblChoTT.getSelectedRow();
        if (r < 0 || r >= dsChoTT.size()) {
            if (dsChoTT.size() > 0) {
                r = 0;
            } else {
                JOptionPane.showMessageDialog(this, "Hiện không có xe nào đang chờ thanh toán!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Map<String, Object> map = dsChoTT.get(r);
        String maPTN = map.get("maPTN") != null ? map.get("maPTN").toString() : "";
        String maPSC = map.get("maPSC") != null ? map.get("maPSC").toString() : "";
        String bienSo = map.get("bienSoXe") != null ? map.get("bienSoXe").toString() : "";
        String target = !maPTN.isEmpty() ? maPTN : (!maPSC.isEmpty() ? maPSC : bienSo);
        ThanhToanHoaDon_UI.datPhieuCanThanhToan(target);

        chuyenTrang("Thanh toán hóa đơn");
    }

    private void chuyenTrangBanLe() {
        chuyenTrang("Lập hóa đơn bán hàng");
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
