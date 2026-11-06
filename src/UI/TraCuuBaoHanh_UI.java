package UI;

import DAO.PhieuBaoHanh_DAO;
import Entity.ChiTietPhieuBaoHanh;
import Entity.KhoDuLieu;
import Entity.PhieuBaoHanh;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình tra cứu phiếu bảo hành Gara Ô tô The T4.
 * Nêu rõ và phân định tách bạch 2 nhóm hạng mục:
 * 1. Dịch vụ sửa chữa (tay nghề kỹ thuật, căn chỉnh, tháo lắp).
 * 2. Phụ tùng / Linh kiện (vật tư chính hãng thay thế).
 */
public class TraCuuBaoHanh_UI extends JPanel {

    private final PhieuBaoHanh_DAO dao = new PhieuBaoHanh_DAO();

    // ==== BỘ LỌC TÌM KIẾM ====
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí (Tự động nhận diện)",
            "Mã phiếu bảo hành (PBH...)",
            "Mã hóa đơn (HD...)",
            "Biển số xe",
            "Khách hàng (Tên / SĐT)",
            "Hạng mục Dịch vụ sửa chữa",
            "Hạng mục Phụ tùng & Linh kiện"
    });

    private final JLabel lblGoiY = new JLabel();
    private final JLabel lblDemKetQua = new JLabel("Đang tải dữ liệu...");

    // ==== BỘ LỌC NHANH TRẠNG THÁI & PHÂN LOẠI ====
    private String locTinhTrang = "TAT_CA";
    private String locLoaiHangMuc = "TAT_CA";

    private JButton chipTatCaTT, chipConHan, chipHetHan, chipDangXuLy;
    private JButton chipTatCaLoai, chipCoSuaChua, chipCoPhuTung;

    // ==== THẺ THỐNG KÊ KPI ====
    private final JLabel lblKpiTongPhieu = new JLabel("0");
    private final JLabel lblKpiConHan = new JLabel("0");
    private final JLabel lblKpiSuaChua = new JLabel("0");
    private final JLabel lblKpiPhuTung = new JLabel("0");

    // ==== BẢNG KẾT QUẢ TRA CỨU (9 CỘT RÕ RÀNG) ====
    private final DefaultTableModel model = UiHelper.model(
            "Mã phiếu", "Mã HĐ", "Biển số xe", "Khách hàng", "Số điện thoại",
            "Thời hạn hiệu lực", "Dịch vụ sửa chữa", "Phụ tùng & Linh kiện", "Trạng thái"
    );
    private JTable table;
    private ArrayList<PhieuBaoHanh> dsHienTai = new ArrayList<>();

    public TraCuuBaoHanh_UI() {
        initUI();
        capNhatGoiY();
        napBang();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        root.setLayout(new BorderLayout(0, 10));
        add(root, BorderLayout.CENTER);

        // 1. HEADER & KPI CARDS
        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = UiHelper.tieuDeTrang("Tra cứu phiếu bảo hành");
        JLabel subtitle = new JLabel("Tra cứu chi tiết thời hạn và phạm vi bảo hành Dịch vụ sửa chữa & Phụ tùng linh kiện cho khách hàng");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));
        title.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(3));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(8));
        topBox.add(header);

        // KPI CARDS
        topBox.add(taoKhungKPI());
        topBox.add(Box.createVerticalStrut(10));
        root.add(topBox, BorderLayout.NORTH);

        // 2. KHUNG DANH SÁCH & BỘ LỌC
        JPanel panelBang = UiHelper.panelBang("Danh sách phiếu bảo hành dịch vụ & phụ tùng");
        panelBang.add(taoKhungTimKiem(), BorderLayout.NORTH);

        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Định dạng căn lề và độ rộng 9 cột
        dinhDangCot(0, SwingConstants.CENTER, 105); // Mã phiếu
        dinhDangCot(1, SwingConstants.CENTER, 90);  // Mã HĐ
        dinhDangCot(2, SwingConstants.CENTER, 110); // Biển số xe
        dinhDangCot(3, SwingConstants.LEFT, 150);   // Khách hàng
        dinhDangCot(4, SwingConstants.CENTER, 105); // Số ĐT
        dinhDangCot(5, SwingConstants.CENTER, 175); // Thời hạn hiệu lực
        dinhDangCot(6, SwingConstants.LEFT, 230);   // Dịch vụ sửa chữa
        dinhDangCot(7, SwingConstants.LEFT, 230);   // Phụ tùng & Linh kiện
        dinhDangCot(8, SwingConstants.CENTER, 110); // Trạng thái

        // Render phân biệt chuyên biệt cho Dịch vụ sửa chữa (Xanh dương) và Phụ tùng (Xanh lá)
        caiDatRendererHangMuc();

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    xemChiTiet();
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        panelBang.add(sp, BorderLayout.CENTER);

        // 3. FOOTER
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 4, 4));

        lblDemKetQua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDemKetQua.setForeground(new Color(71, 85, 105));
        footer.add(lblDemKetQua, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(UiHelper.nut("Xem chi tiết phiếu", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> xemChiTiet()));
        actions.add(UiHelper.nut("In phiếu bảo hành", UiIcon.of(UiIcon.PRINT, 15), new Color(194, 65, 12), e -> inPhieuDangChon()));
        actions.add(UiHelper.nut("Lập phiếu bảo hành mới", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> chuyenTrangLapPhieu()));

        footer.add(actions, BorderLayout.EAST);
        panelBang.add(footer, BorderLayout.SOUTH);

        root.add(panelBang, BorderLayout.CENTER);
    }

    // ===============================================================
    // 1. THẺ THỐNG KÊ KPI
    // ===============================================================
    private JPanel taoKhungKPI() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        p.add(taoTheKpi("TỔNG PHIẾU BẢO HÀNH", lblKpiTongPhieu, new Color(30, 58, 138), new Color(239, 246, 255)));
        p.add(taoTheKpi("ĐANG CÒN HIỆU LỰC", lblKpiConHan, new Color(21, 128, 61), new Color(240, 253, 244)));
        p.add(taoTheKpi("CÓ BẢO HÀNH SỬA CHỮA", lblKpiSuaChua, new Color(37, 99, 235), new Color(239, 246, 255)));
        p.add(taoTheKpi("CÓ BẢO HÀNH PHỤ TÙNG", lblKpiPhuTung, new Color(109, 40, 217), new Color(245, 243, 255)));

        return p;
    }

    private JPanel taoTheKpi(String tieuDe, JLabel lblSo, Color mauChu, Color mauNen) {
        JPanel card = new JPanel(new BorderLayout(5, 2));
        card.setBackground(mauNen);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel lblTitle = new JLabel(tieuDe);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(100, 116, 139));

        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSo.setForeground(mauChu);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblSo, BorderLayout.CENTER);
        return card;
    }

    // ===============================================================
    // 2. KHUNG TÌM KIẾM VÀ BỘ LỌC
    // ===============================================================
    private JPanel taoKhungTimKiem() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Hàng 1: Tiêu chí & Ô nhập từ khóa
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setOpaque(false);

        JPanel criteria = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        criteria.setOpaque(false);
        JLabel label = new JLabel("Tìm theo:");
        label.setFont(UiHelper.FONT_NHAN);
        label.setForeground(new Color(51, 65, 85));

        cboTieuChi.setPreferredSize(new Dimension(260, 38));
        cboTieuChi.setFont(UiHelper.FONT_BT);
        cboTieuChi.setBackground(Color.WHITE);
        cboTieuChi.addActionListener(e -> {
            capNhatGoiY();
            napBang();
        });
        criteria.add(label);
        criteria.add(cboTieuChi);
        row1.add(criteria, BorderLayout.WEST);

        txtTuKhoa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTuKhoa.setPreferredSize(new Dimension(340, 38));
        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { napBang(); }
            public void removeUpdate(DocumentEvent e) { napBang(); }
            public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        row1.add(txtTuKhoa, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(UiHelper.nut("Tìm kiếm", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> napBang()));
        buttons.add(UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, e -> lamMoiBoLoc()));
        row1.add(buttons, BorderLayout.EAST);
        p.add(row1);
        p.add(Box.createVerticalStrut(8));

        // Hàng 2: Bộ lọc nhanh Tình trạng & Loại hạng mục bảo hành
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);

        JLabel status = new JLabel("Tình trạng:");
        status.setFont(new Font("Segoe UI", Font.BOLD, 12));
        status.setForeground(new Color(100, 116, 139));
        row2.add(status);

        chipTatCaTT = taoChip("Tất cả", () -> { locTinhTrang = "TAT_CA"; capNhatChip(); napBang(); });
        chipConHan = taoChip("Còn hạn", () -> { locTinhTrang = "Còn hạn"; capNhatChip(); napBang(); });
        chipHetHan = taoChip("Hết hạn", () -> { locTinhTrang = "Hết hạn"; capNhatChip(); napBang(); });
        chipDangXuLy = taoChip("Đang xử lý", () -> { locTinhTrang = "Đang xử lý"; capNhatChip(); napBang(); });

        row2.add(chipTatCaTT);
        row2.add(chipConHan);
        row2.add(chipHetHan);
        row2.add(chipDangXuLy);

        row2.add(Box.createHorizontalStrut(14));

        JLabel lblLoai = new JLabel("Nhóm bảo hành:");
        lblLoai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLoai.setForeground(new Color(100, 116, 139));
        row2.add(lblLoai);

        chipTatCaLoai = taoChip("Tất cả nhóm", () -> { locLoaiHangMuc = "TAT_CA"; capNhatChip(); napBang(); });
        chipCoSuaChua = taoChip("Có Dịch vụ sửa chữa", () -> { locLoaiHangMuc = "sửa chữa"; capNhatChip(); napBang(); });
        chipCoPhuTung = taoChip("Có Phụ tùng linh kiện", () -> { locLoaiHangMuc = "phụ tùng"; capNhatChip(); napBang(); });

        row2.add(chipTatCaLoai);
        row2.add(chipCoSuaChua);
        row2.add(chipCoPhuTung);

        p.add(row2);
        p.add(Box.createVerticalStrut(8));

        // Hàng 3: Khung nhắc nhở & gợi ý
        JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        hint.setOpaque(true);
        hint.setBackground(new Color(239, 246, 255));
        hint.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        lblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGoiY.setForeground(new Color(30, 64, 175));
        hint.add(lblGoiY);
        p.add(hint);

        capNhatChip();
        return p;
    }

    private JButton taoChip(String text, Runnable onClick) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.BLACK);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        button.addActionListener(e -> onClick.run());
        return button;
    }

    private void capNhatChip() {
        dinhDangNutChip(chipTatCaTT, "TAT_CA".equalsIgnoreCase(locTinhTrang));
        dinhDangNutChip(chipConHan, "Còn hạn".equalsIgnoreCase(locTinhTrang));
        dinhDangNutChip(chipHetHan, "Hết hạn".equalsIgnoreCase(locTinhTrang));
        dinhDangNutChip(chipDangXuLy, "Đang xử lý".equalsIgnoreCase(locTinhTrang));

        dinhDangNutChip(chipTatCaLoai, "TAT_CA".equalsIgnoreCase(locLoaiHangMuc));
        dinhDangNutChip(chipCoSuaChua, "sửa chữa".equalsIgnoreCase(locLoaiHangMuc));
        dinhDangNutChip(chipCoPhuTung, "phụ tùng".equalsIgnoreCase(locLoaiHangMuc));
    }

    private void dinhDangNutChip(JButton button, boolean selected) {
        if (button == null) return;
        button.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 12));
        button.setBackground(selected ? new Color(239, 246, 255) : Color.WHITE);
        button.setForeground(selected ? new Color(29, 78, 216) : new Color(51, 65, 85));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? new Color(59, 130, 246) : new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
    }

    private void lamMoiBoLoc() {
        txtTuKhoa.setText("");
        cboTieuChi.setSelectedIndex(0);
        locTinhTrang = "TAT_CA";
        locLoaiHangMuc = "TAT_CA";
        capNhatChip();
        capNhatGoiY();
        napBang();
    }

    private void capNhatGoiY() {
        String text = switch (cboTieuChi.getSelectedIndex()) {
            case 1 -> "Nhập mã phiếu bảo hành (VD: PBH00107, BH001) để tìm chính xác.";
            case 2 -> "Nhập mã hóa đơn liên kết (VD: HD003, HD101) để xem phiếu bảo hành tương ứng.";
            case 3 -> "Nhập biển số xe (VD: 51A-20120 hoặc 51A20120) của phương tiện bảo hành.";
            case 4 -> "Nhập tên hoặc số điện thoại khách hàng chủ xe (VD: Trịnh Thanh Trúc, 0918...).";
            case 5 -> "Nhập tên công việc dịch vụ sửa chữa (VD: Cân chỉnh thước lái, đại tu máy, thay thế...).";
            case 6 -> "Nhập tên phụ tùng linh kiện (VD: Lọc dầu, bugi, dung dịch kim phun...).";
            default -> "Tìm nhanh tự động theo mã phiếu, mã HĐ, biển số xe, tên/SĐT khách hàng, công việc sửa chữa hoặc phụ tùng.";
        };
        lblGoiY.setText(text);
        txtTuKhoa.setToolTipText(text);
    }

    // ===============================================================
    // 3. ĐỊNH DẠNG BẢNG & RENDERER MỤC BẢO HÀNH
    // ===============================================================
    private void dinhDangCot(int col, int align, int width) {
        TableColumn tc = table.getColumnModel().getColumn(col);
        tc.setPreferredWidth(width);
        tc.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(align);
                l.setBackground(new Color(241, 245, 249));
                l.setForeground(new Color(30, 41, 59));
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(8, align == SwingConstants.LEFT ? 10 : 4, 8, 4)
                ));
                return l;
            }
        });
        tc.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(new Color(30, 41, 59));
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

    private void caiDatRendererHangMuc() {
        // Cột 6: Hạng mục Sửa chữa (Badge xanh dương)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(new EmptyBorder(0, 8, 0, 8));
                String val = v != null ? v.toString().trim() : "";
                if (!s) {
                    l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    if (val.isEmpty() || "—".equals(val)) {
                        l.setForeground(new Color(148, 163, 184));
                        l.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    } else {
                        l.setForeground(new Color(29, 78, 216));
                        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
                return l;
            }
        });

        // Cột 7: Phụ tùng & Linh kiện (Badge xanh lá)
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(new EmptyBorder(0, 8, 0, 8));
                String val = v != null ? v.toString().trim() : "";
                if (!s) {
                    l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    if (val.isEmpty() || "—".equals(val)) {
                        l.setForeground(new Color(148, 163, 184));
                        l.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    } else {
                        l.setForeground(new Color(21, 128, 61));
                        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
                return l;
            }
        });

        // Cột 8: Trạng thái bảo hành
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = v != null ? v.toString().trim() : "";
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!s) {
                    if ("Còn hạn".equalsIgnoreCase(val)) {
                        l.setBackground(new Color(240, 253, 244));
                        l.setForeground(new Color(21, 128, 61));
                    } else if ("Hết hạn".equalsIgnoreCase(val)) {
                        l.setBackground(new Color(254, 242, 242));
                        l.setForeground(new Color(185, 28, 28));
                    } else {
                        l.setBackground(new Color(254, 243, 199));
                        l.setForeground(new Color(180, 83, 9));
                    }
                }
                return l;
            }
        });
    }

    // ===============================================================
    // 4. NẠP DỮ LIỆU & THỐNG KÊ
    // ===============================================================
    private void napBang() {
        String q = txtTuKhoa.getText().trim();
        int criterion = cboTieuChi.getSelectedIndex();
        model.setRowCount(0);

        // Lấy danh sách phiếu bảo hành theo truy vấn động đầy đủ
        dsHienTai = dao.traCuu(q, criterion, locTinhTrang, locLoaiHangMuc);

        // Cập nhật thẻ KPI
        List<PhieuBaoHanh> tatCa = dao.layTatCa();
        int tongPhieu = tatCa.size();
        int conHan = 0;
        int coSC = 0;
        int coPT = 0;
        for (PhieuBaoHanh p : tatCa) {
            if ("Còn hạn".equalsIgnoreCase(p.getTinhTrang())) conHan++;
            if (p.coSuaChua()) coSC++;
            if (p.coPhuTung()) coPT++;
        }
        lblKpiTongPhieu.setText(String.valueOf(tongPhieu));
        lblKpiConHan.setText(String.valueOf(conHan));
        lblKpiSuaChua.setText(String.valueOf(coSC));
        lblKpiPhuTung.setText(String.valueOf(coPT));

        // Nạp các dòng vào bảng hiển thị
        for (PhieuBaoHanh p : dsHienTai) {
            String sc = p.getNoiDungSuaChua().isEmpty() ? "—" : p.getNoiDungSuaChua();
            String pt = p.getNoiDungPhuTung().isEmpty() ? "—" : p.getNoiDungPhuTung();
            String thoiHan = safe(p.getNgayBDHienThi()) + "  -  " + safe(p.getNgayKTHienThi());

            model.addRow(new Object[]{
                    p.getSoPhieu(),
                    safe(p.getMaHoaDon()).isEmpty() ? "—" : p.getMaHoaDon(),
                    p.getBienSo(),
                    safe(p.getTenKhachHang()).isEmpty() ? "Chưa rõ" : p.getTenKhachHang(),
                    safe(p.getSoDienThoai()).isEmpty() ? "—" : p.getSoDienThoai(),
                    thoiHan,
                    sc,
                    pt,
                    p.getTinhTrang()
            });
        }

        lblDemKetQua.setText(String.format("Hiển thị: %d / %d phiếu bảo hành", dsHienTai.size(), tongPhieu));
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // ===============================================================
    // 5. XEM CHI TIẾT PHIẾU BẢO HÀNH (PHÂN CHIA SỬA CHỮA & PHỤ TÙNG)
    // ===============================================================
    private void xemChiTiet() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu bảo hành trong bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PhieuBaoHanh p = dsHienTai.get(table.convertRowIndexToModel(row));
        String maPBH = p.getSoPhieu();

        List<ChiTietPhieuBaoHanh> dsCT = dao.layChiTietPhieuBaoHanh(maPBH);
        List<ChiTietPhieuBaoHanh> dsSC = new ArrayList<>();
        List<ChiTietPhieuBaoHanh> dsPT = new ArrayList<>();

        for (ChiTietPhieuBaoHanh ct : dsCT) {
            if (ct.laSuaChua()) dsSC.add(ct);
            else dsPT.add(ct);
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWindow, "Chi tiết phiếu bảo hành — " + maPBH, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(920, 640);
        dlg.setLocationRelativeTo(parentWindow);
        dlg.setLayout(new BorderLayout(0, 10));

        // Khối Header Thông tin phiếu & Phương tiện
        JPanel pnlHead = new JPanel(new GridLayout(4, 2, 12, 6));
        pnlHead.setBackground(new Color(241, 245, 249));
        pnlHead.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)),
                new EmptyBorder(12, 18, 12, 18)
        ));

        pnlHead.add(new JLabel("Mã phiếu: " + maPBH));
        pnlHead.add(new JLabel("Hóa đơn liên kết: " + (safe(p.getMaHoaDon()).isEmpty() ? "—" : p.getMaHoaDon())));
        pnlHead.add(new JLabel("Chủ xe / Khách hàng: " + safe(p.getTenKhachHang()) + " (" + safe(p.getSoDienThoai()) + ")"));
        pnlHead.add(new JLabel("Biển số xe: " + safe(p.getBienSo())));
        pnlHead.add(new JLabel("Thời hạn hiệu lực: " + safe(p.getNgayBDHienThi()) + "  -  " + safe(p.getNgayKTHienThi())));
        pnlHead.add(new JLabel("Tình trạng: " + safe(p.getTinhTrang())));
        pnlHead.add(new JLabel("Cố vấn / Người lập: " + (safe(p.getNguoiLap()).isEmpty() ? "Xưởng Dịch Vụ The T4" : p.getNguoiLap())));
        pnlHead.add(new JLabel(String.format("Tổng hạng mục: %d (Sửa chữa: %d  |  Phụ tùng: %d)", dsCT.size(), dsSC.size(), dsPT.size())));

        dlg.add(pnlHead, BorderLayout.NORTH);

        // Khối nội dung: Tabs phân tách rõ rệt 2 danh mục Sửa chữa & Phụ tùng
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabs.addTab("1. Dịch vụ sửa chữa (" + dsSC.size() + ")", taoBangHangMuc(dsSC, true));
        tabs.addTab("2. Phụ tùng & Linh kiện (" + dsPT.size() + ")", taoBangHangMuc(dsPT, false));
        tabs.addTab("3. Tất cả hạng mục (" + dsCT.size() + ")", taoBangTatCaHangMuc(dsCT));

        dlg.add(tabs, BorderLayout.CENTER);

        // Footer buttons: Đóng + In phiếu bảo hành
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(6, 14, 6, 14)
        ));

        JButton btnIn = UiHelper.nut("In phiếu bảo hành", UiIcon.of(UiIcon.PRINT, 14), new Color(194, 65, 12), ev -> {
            LapPhieuBaoHanh_UI.inPhieuBaoHanh(dlg, maPBH);
        });
        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, ev -> dlg.dispose());

        pnlBot.add(btnIn);
        pnlBot.add(btnDong);
        dlg.add(pnlBot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JComponent taoBangHangMuc(List<ChiTietPhieuBaoHanh> list, boolean laSuaChua) {
        if (list.isEmpty()) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(Color.WHITE);
            JLabel lbl = new JLabel(laSuaChua
                    ? "Phiếu bảo hành này không bao gồm hạng mục dịch vụ sửa chữa độc lập."
                    : "Phiếu bảo hành này không bao gồm linh kiện phụ tùng thay thế.");
            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lbl.setForeground(new Color(100, 116, 139));
            p.add(lbl);
            return p;
        }

        DefaultTableModel md = UiHelper.model("STT", laSuaChua ? "Nội dung công việc sửa chữa" : "Tên linh kiện / Phụ tùng thay thế", "Thời hạn", "Ngày hết hạn", "Điều kiện bảo hành", "Ghi chú");
        JTable tbl = UiHelper.bang(md);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(45);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(260);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(85);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(95);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(260);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(120);

        UiHelper.canGiua(tbl, 0, 2, 3);

        tbl.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!isSelected) {
                    l.setForeground(laSuaChua ? new Color(29, 78, 216) : new Color(21, 128, 61));
                    l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
                return l;
            }
        });

        int stt = 1;
        for (ChiTietPhieuBaoHanh ct : list) {
            md.addRow(new Object[]{
                    stt++,
                    ct.getTenHangMuc(),
                    ct.getThoiHanBaoHanh() + " tháng",
                    ct.getNgayHetHanHienThi(),
                    ct.getDieuKienBaoHanh(),
                    safe(ct.getGhiChu())
            });
        }

        return new JScrollPane(tbl);
    }

    private JComponent taoBangTatCaHangMuc(List<ChiTietPhieuBaoHanh> list) {
        DefaultTableModel md = UiHelper.model("STT", "Loại bảo hành", "Tên hạng mục", "Thời hạn", "Ngày hết hạn", "Điều kiện bảo hành", "Ghi chú");
        JTable tbl = UiHelper.bang(md);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(45);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(135);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(240);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(85);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(95);
        tbl.getColumnModel().getColumn(5).setPreferredWidth(240);
        tbl.getColumnModel().getColumn(6).setPreferredWidth(110);

        UiHelper.canGiua(tbl, 0, 3, 4);

        tbl.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = value != null ? value.toString() : "";
                boolean laSC = val.contains("sửa chữa") || val.contains("dịch vụ");
                if (!isSelected) {
                    l.setBackground(laSC ? new Color(239, 246, 255) : new Color(240, 253, 244));
                    l.setForeground(laSC ? new Color(29, 78, 216) : new Color(21, 128, 61));
                    l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                }
                return l;
            }
        });

        int stt = 1;
        for (ChiTietPhieuBaoHanh ct : list) {
            md.addRow(new Object[]{
                    stt++,
                    ct.getLoaiHangMuc(),
                    ct.getTenHangMuc(),
                    ct.getThoiHanBaoHanh() + " tháng",
                    ct.getNgayHetHanHienThi(),
                    ct.getDieuKienBaoHanh(),
                    safe(ct.getGhiChu())
            });
        }

        return new JScrollPane(tbl);
    }

    private void inPhieuDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu bảo hành trong bảng để in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PhieuBaoHanh p = dsHienTai.get(table.convertRowIndexToModel(row));
        LapPhieuBaoHanh_UI.inPhieuBaoHanh(this, p.getSoPhieu());
    }

    private void chuyenTrangLapPhieu() {
        Main_UI main = Main_UI.getInstance();
        if (main == null) {
            main = (Main_UI) SwingUtilities.getAncestorOfClass(Main_UI.class, this);
        }
        if (main != null) {
            main.chuyenTrang("Lập phiếu bảo hành");
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy giao diện chính.");
        }
    }
}
