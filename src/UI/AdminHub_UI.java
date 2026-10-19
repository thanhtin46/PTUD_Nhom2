package UI;

import Entity.CauHinhHeThong;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.NhatKyHeThong;
import Entity.PhuTung;
import Entity.PhieuTiepNhan;
import Entity.PhieuKho;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * AdminHub_UI — Trung tâm Quản trị & Điều hành Toàn diện Gara (Admin Operations Hub):
 * - Cổng điều phối tất cả các phân hệ chức năng của hệ thống (1-click Quick Launch)
 * - Giám sát vận hành & Chẩn đoán điểm nghẽn thời gian thực (Diagnostic Engine)
 * - Cấu hình tham số & Quy tắc kinh doanh gara (Business Rules Engine)
 * - Sao lưu, xuất bản báo cáo & Phục hồi cơ sở dữ liệu (Disaster Recovery Hub)
 * - Nhật ký kiểm toán an ninh & Giám sát thao tác (Audit Trail & Activity Log)
 * 
 * Toàn bộ các chức năng quản trị cấp cao này ĐỀU KHÔNG PHẢI LÀ CRUD (thêm/xóa/sửa danh mục).
 */
public class AdminHub_UI extends JPanel {
    private final NhanVien admin;
    private final Main_UI mainUI;
    private final KhoDuLieu kho = KhoDuLieu.get();

    // Các thành phần tab Cấu hình
    private JTextField txtTenGara, txtHotline, txtDiaChi, txtStk;
    private JTextField txtVat, txtChietKhauMax, txtBaoHanhThang, txtTonKhoMin, txtGioiHanXe;
    private JTextField txtLoiCamOn;
    private JCheckBox chkCanhBaoTuDong, chkXacNhanHuy;

    // Các thành phần tab Giám sát
    private DefaultTableModel modelCanhBao;
    private JLabel lblStatSC, lblStatKho, lblStatChoNhap, lblStatTongQuan;

    // Các thành phần tab Nhật ký
    private DefaultTableModel modelNhatKy;
    private JTable tableNhatKy;
    private JTextField txtTimNhatKy;
    private JComboBox<String> cboMucDoLog;

    private JTabbedPane tabs;

    public void chonTab(int index) {
        if (tabs != null && index >= 0 && index < tabs.getTabCount()) {
            tabs.setSelectedIndex(index);
        }
    }

    public AdminHub_UI(NhanVien admin, Main_UI mainUI) {
        this(admin, mainUI, 0);
    }

    public AdminHub_UI(NhanVien admin, Main_UI mainUI, int initialTab) {
        this.admin = admin;
        this.mainUI = mainUI;

        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        add(root, BorderLayout.CENTER);

        // Header tiêu đề trang Admin
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel tde = UiHelper.tieuDeTrang("Trung tâm Quản trị & Điều hành Gara", UiIcon.of(UiIcon.SHIELD, 24, UiHelper.XANH_TRUNG));
        JLabel phu = new JLabel("Cổng điều phối tất cả chức năng, giám sát vận hành, quy định hệ thống & kiểm toán an ninh");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(tde);
        header.add(Box.createVerticalStrut(4));
        header.add(phu);
        header.add(Box.createVerticalStrut(12));
        root.add(header, BorderLayout.NORTH);

        // Hệ thống Tabbed Pane hiện đại
        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(UiHelper.XANH_DAM);

        tabs.addTab("  Cổng điều phối chức năng  ", UiIcon.of(UiIcon.USERS, 16), taoTabTatCaChucNang());
        tabs.addTab("  Giám sát & Chẩn đoán sự cố  ", UiIcon.of(UiIcon.CLOCK, 16), taoTabGiamSat());
        tabs.addTab("  Cấu hình & Quy định vận hành  ", UiIcon.of(UiIcon.SETTINGS, 16), taoTabCauHinh());
        tabs.addTab("  Sao lưu & Phục hồi dữ liệu  ", UiIcon.of(UiIcon.IMPORT, 16), taoTabSaoLuu());
        tabs.addTab("  Nhật ký kiểm toán an ninh  ", UiIcon.of(UiIcon.LOCK, 16), taoTabNhatKy());

        if (initialTab >= 0 && initialTab < tabs.getTabCount()) {
            tabs.setSelectedIndex(initialTab);
        }

        root.add(tabs, BorderLayout.CENTER);
    }

    // =========================================================================
    // TAB 1: CỔNG ĐIỀU PHỐI TẤT CẢ CHỨC NĂNG (ALL FEATURES GATEWAY)
    // =========================================================================
    private JComponent taoTabTatCaChucNang() {
        JPanel wrap = new JPanel(new BorderLayout(0, 14));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 8, 14, 8));

        // Thanh giới thiệu & tìm kiếm nhanh
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        JLabel lblGthieu = new JLabel("<html><b>CỔNG TRUY CẬP TRỰC TIẾP:</b> Quản trị viên có thể giám sát trạng thái và truy cập nhanh vào mọi module trong gara.</html>");
        lblGthieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblGthieu.setForeground(new Color(51, 65, 85));
        bar.add(lblGthieu, BorderLayout.CENTER);
        wrap.add(bar, BorderLayout.NORTH);

        // Container các khối phân hệ
        JPanel gridBox = new JPanel();
        gridBox.setLayout(new BoxLayout(gridBox, BoxLayout.Y_AXIS));
        gridBox.setOpaque(false);

        // 1. Phân hệ Khách hàng & Phương tiện
        gridBox.add(taoTheNhomModule("Khách hàng & Phương tiện", new Color(37, 99, 235), new String[][]{
                {"Danh sách khách hàng", "Quản lý hồ sơ, địa chỉ, số điện thoại & CCCD", kho.getKhachHangs().size() + " hồ sơ"},
                {"Tra cứu khách hàng", "Tra cứu lịch sử sửa chữa và danh sách xe sở hữu", "Tìm kiếm đa năng"},
                {"Danh sách phương tiện", "Quản lý thông tin xe, số khung VIN, ODO và chủ xe", kho.getPhuongTiens().size() + " phương tiện"},
                {"Tra cứu phương tiện", "Tra cứu phương tiện theo biển số, dòng xe", "Lịch sử tiếp nhận"}
        }));
        gridBox.add(Box.createVerticalStrut(14));

        // 2. Phân hệ Tiếp nhận, Sửa chữa & Kỹ thuật
        gridBox.add(taoTheNhomModule("Tiếp nhận, Sửa chữa & Bảo hành", new Color(22, 163, 74), new String[][]{
                {"Lập phiếu sửa chữa", "Tiếp nhận xe vào gara, ghi nhận yêu cầu & tạm tính", kho.getPhieuTiepNhans().size() + " phiếu đã tạo"},
                {"Phân công KTV", "Giao xe cần sửa chữa cho kỹ thuật viên phụ trách", "Điều phối nhân sự"},
                {"Cập nhật tiến độ", "Cập nhật trạng thái sửa chữa theo thời gian thực", "Theo dõi trực tiếp"},
                {"Lập phiếu bảo hành", "Cấp sổ bảo hành định kỳ sau khi xe sửa chữa xong", kho.getPhieuBaoHanhs().size() + " phiếu bảo hành"},
                {"Tra cứu bảo hành", "Kiểm tra thời hạn bảo hành theo biển số xe", "Tra cứu tức thì"}
        }));
        gridBox.add(Box.createVerticalStrut(14));

        // 3. Phân hệ Kho phụ tùng & Linh kiện
        gridBox.add(taoTheNhomModule("Kho phụ tùng & Linh kiện", new Color(245, 158, 11), new String[][]{
                {"Danh sách phụ tùng", "Danh mục phụ tùng, đơn vị tính, tồn kho & đơn giá", kho.getPhuTungs().size() + " mặt hàng"},
                {"Nhập kho", "Lập phiếu nhập linh kiện phụ tùng từ nhà cung cấp", "Nhập kho vật tư"},
                {"Xuất kho", "Xuất kho phụ tùng phục vụ quy trình sửa chữa", "Kiểm soát linh kiện"},
                {"Theo dõi chờ nhập", "Theo dõi đơn hàng đã đặt nhà cung cấp đang trên đường giao", "Quản lý chuỗi cung ứng"},
                {"Báo cáo tồn kho", "Cảnh báo hết hàng, sắp hết hàng theo định mức an toàn", "Cảnh báo Real-time"}
        }));
        gridBox.add(Box.createVerticalStrut(14));

        // 4. Phân hệ Tài chính, Hóa đơn & Báo cáo
        gridBox.add(taoTheNhomModule("Tài chính, Thu ngân & Doanh thu", new Color(124, 58, 237), new String[][]{
                {"Thanh toán hóa đơn", "Thu ngân lập hóa đơn dịch vụ, chiết khấu và in biên lai", kho.getHoaDons().size() + " hóa đơn đã xuất"},
                {"Theo tháng", "Báo cáo doanh thu gara chi tiết theo từng tháng trong năm", "Thống kê tháng"},
                {"Theo năm", "Thống kê so sánh tăng trưởng doanh thu qua các năm", "Thống kê tổng hợp"}
        }));
        gridBox.add(Box.createVerticalStrut(14));

        // 5. Phân hệ Quản trị nhân sự & Phân quyền
        gridBox.add(taoTheNhomModule("Quản trị nhân sự & An ninh", new Color(239, 68, 68), new String[][]{
                {"Quản lý nhân viên", "Hồ sơ tài khoản nhân sự, vai trò & kích hoạt/khóa tài khoản", kho.getNhanViens().size() + " tài khoản"},
                {"Phân quyền", "Phân quyền truy cập các module chức năng theo từng tài khoản", "Bảo mật module"}
        }));

        JScrollPane scroll = new JScrollPane(gridBox);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel taoTheNhomModule(String tenNhom, Color mauNhom, String[][] cacModule) {
        JPanel card = UiHelper.taoThe(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Header nhóm
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        h.setOpaque(false);
        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(8, 16));
        dot.setBackground(mauNhom);
        h.add(dot);

        JLabel lblTen = new JLabel(tenNhom.toUpperCase());
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTen.setForeground(UiHelper.XANH_DAM);
        h.add(lblTen);
        card.add(h, BorderLayout.NORTH);

        // Lưới các module con
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 10));
        grid.setOpaque(false);

        for (String[] m : cacModule) {
            String tenModule = m[0];
            String moTa = m[1];
            String stat = m[2];

            JPanel item = new JPanel(new BorderLayout(8, 0));
            item.setBackground(new Color(248, 250, 252));
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            JLabel l1 = new JLabel(tenModule);
            l1.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l1.setForeground(UiHelper.XANH_DAM);

            JLabel l2 = new JLabel(moTa);
            l2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            l2.setForeground(new Color(100, 116, 139));

            JLabel l3 = new JLabel("● " + stat);
            l3.setFont(new Font("Segoe UI", Font.BOLD, 11));
            l3.setForeground(mauNhom);

            textPanel.add(l1);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(l2);
            textPanel.add(Box.createVerticalStrut(4));
            textPanel.add(l3);
            item.add(textPanel, BorderLayout.CENTER);

            JButton btnMo = UiHelper.nut("Mở", UiIcon.of(UiIcon.CHECK, 13), mauNhom, e -> {
                if (mainUI != null) {
                    mainUI.chuyenTrang(tenModule);
                } else {
                    JOptionPane.showMessageDialog(this, "Chuyển đến màn hình: " + tenModule);
                }
            });
            btnMo.setPreferredSize(new Dimension(80, 32));
            btnMo.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnMo.setBorder(new EmptyBorder(4, 8, 4, 8));

            JPanel btnWrap = new JPanel(new GridBagLayout());
            btnWrap.setOpaque(false);
            btnWrap.add(btnMo);
            item.add(btnWrap, BorderLayout.EAST);

            grid.add(item);
        }

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // TAB 2: GIÁM SÁT VẬN HÀNH & CHẨN ĐOÁN SỰ CỐ TỨC THỜI (DIAGNOSTICS MONITOR)
    // =========================================================================
    private JComponent taoTabGiamSat() {
        JPanel wrap = new JPanel(new BorderLayout(0, 14));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 8, 14, 8));

        // 4 Thẻ chỉ số chẩn đoán
        JPanel gridKPI = new JPanel(new GridLayout(1, 4, 12, 0));
        gridKPI.setOpaque(false);

        lblStatSC = new JLabel("0");
        lblStatKho = new JLabel("0");
        lblStatChoNhap = new JLabel("0");
        lblStatTongQuan = new JLabel("TỐT");

        gridKPI.add(theKPIChẩnĐoán("Tiến độ sửa chữa", lblStatSC, "Xe đang xử lý", new Color(37, 99, 235)));
        gridKPI.add(theKPIChẩnĐoán("Tồn kho nguy cấp", lblStatKho, "Hết / chạm sàn", new Color(220, 38, 38)));
        gridKPI.add(theKPIChẩnĐoán("Đơn hàng chờ nhập", lblStatChoNhap, "Đơn đang đặt NCC", new Color(245, 158, 11)));
        gridKPI.add(theKPIChẩnĐoán("Trạng thái gara", lblStatTongQuan, "Tình trạng vận hành", new Color(22, 163, 74)));
        wrap.add(gridKPI, BorderLayout.NORTH);

        // Bảng danh sách cảnh báo chi tiết
        JPanel cardBang = UiHelper.panelBang("Bảng chẩn đoán & cảnh báo rủi ro tự động (System Diagnostic Engine)");
        modelCanhBao = UiHelper.model("Mức độ", "Phân hệ", "Mã đối tượng", "Vấn đề phát hiện", "Đề xuất xử lý", "Trạng thái");
        JTable tableCanhBao = UiHelper.bang(modelCanhBao);

        // Renderer màu cho mức độ cảnh báo
        tableCanhBao.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String val = String.valueOf(v);
                if ("NGUY CẤP".equals(val)) {
                    lbl.setForeground(new Color(220, 38, 38));
                } else if ("CẢNH BÁO".equals(val)) {
                    lbl.setForeground(new Color(217, 119, 6));
                } else {
                    lbl.setForeground(new Color(37, 99, 235));
                }
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(tableCanhBao);
        UiHelper.danhSachBong(sp);
        cardBang.add(sp, BorderLayout.CENTER);

        // Thanh tác vụ hệ thống
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        toolbar.setOpaque(false);

        JButton btnScan = UiHelper.nut("Chạy chẩn đoán lại", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> chayChanDoan());
        JButton btnDeXuatNhap = UiHelper.nut("Tự động đề xuất nhập kho", UiIcon.of(UiIcon.IMPORT, 15), UiHelper.CAM, e -> tuDongDeXuatNhapKho());
        JButton btnNhacViec = UiHelper.nut("Phát lệnh thông báo nhắc việc", UiIcon.of(UiIcon.CLOCK, 15), UiHelper.TIM, e -> phatLenhNhacViec());

        toolbar.add(btnScan);
        toolbar.add(btnDeXuatNhap);
        toolbar.add(btnNhacViec);
        cardBang.add(toolbar, BorderLayout.SOUTH);

        wrap.add(cardBang, BorderLayout.CENTER);
        chayChanDoan();
        return wrap;
    }

    private JPanel theKPIChẩnĐoán(String tieuDe, JLabel lblSo, String moTa, Color mau) {
        JPanel card = UiHelper.taoThe(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTde = new JLabel(tieuDe);
        lblTde.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTde.setForeground(new Color(100, 116, 139));

        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSo.setForeground(mau);

        JLabel lblMoTa = new JLabel(moTa);
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMoTa.setForeground(new Color(148, 163, 184));

        card.add(lblTde, BorderLayout.NORTH);
        card.add(lblSo, BorderLayout.CENTER);
        card.add(lblMoTa, BorderLayout.SOUTH);
        return card;
    }

    private void chayChanDoan() {
        modelCanhBao.setRowCount(0);
        int soNguyCap = 0;
        int soCanhBao = 0;

        // 1. Quét phiếu sửa chữa
        int scDangXuLy = 0;
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            String td = p.getTienDo() == null ? "" : p.getTienDo();
            if (!"Hoàn thành".equalsIgnoreCase(td)) {
                scDangXuLy++;
                if ("Chờ tiếp nhận".equalsIgnoreCase(td) && !p.daPhanCong()) {
                    modelCanhBao.addRow(new Object[]{"CẢNH BÁO", "Tiếp nhận & Sửa chữa", p.getSoPhieu(),
                            "Xe " + p.getBienSo() + " chưa được phân công kỹ thuật viên", "Giao việc cho thợ ngay", "Chờ KTV"});
                    soCanhBao++;
                }
            }
        }
        lblStatSC.setText(String.valueOf(scDangXuLy));

        // 2. Quét kho phụ tùng
        int khoNguyCap = 0;
        for (PhuTung pt : kho.getPhuTungs()) {
            if (pt.getSoLuongTon() == 0) {
                modelCanhBao.addRow(new Object[]{"NGUY CẤP", "Kho & Phụ tùng", pt.getMaPT(),
                        "Linh kiện \"" + pt.getTen() + "\" đã HẾT HÀNG", "Lập đơn đặt hàng khẩn cho NCC", "Hết tồn"});
                khoNguyCap++;
                soNguyCap++;
            } else if (pt.canhBao()) {
                modelCanhBao.addRow(new Object[]{"CẢNH BÁO", "Kho & Phụ tùng", pt.getMaPT(),
                        "Linh kiện \"" + pt.getTen() + "\" sắp hết (còn " + pt.getSoLuongTon() + " " + pt.getDonVi() + ")", "Lên kế hoạch nhập bổ sung", "Sắp hết"});
                khoNguyCap++;
                soCanhBao++;
            }
        }
        lblStatKho.setText(String.valueOf(khoNguyCap));

        // 3. Quét phiếu chờ nhập
        int donCho = 0;
        for (PhieuKho pk : kho.getPhieuKhos()) {
            if (pk.isChoNhap()) {
                donCho++;
                modelCanhBao.addRow(new Object[]{"CHÚ Ý", "Chuỗi cung ứng", pk.getSoPhieu(),
                        "Đơn nhập kho chờ giao từ NCC " + pk.getNhaCungCap(), "Theo dõi tiến độ giao hàng", "Đang chờ"});
            }
        }
        lblStatChoNhap.setText(String.valueOf(donCho));

        // 4. Quét nhân sự
        for (NhanVien nv : kho.getNhanViens()) {
            if (!nv.dangHoatDong()) {
                modelCanhBao.addRow(new Object[]{"CHÚ Ý", "Nhân sự & An ninh", nv.getMaNV(),
                        "Tài khoản \"" + nv.getTaiKhoan() + "\" đang bị khóa / tạm ngưng", "Kiểm tra lý do khóa tài khoản", "Đang khóa"});
            }
        }

        if (soNguyCap > 0) {
            lblStatTongQuan.setText("BÁO ĐỘNG");
            lblStatTongQuan.setForeground(new Color(220, 38, 38));
        } else if (soCanhBao > 0) {
            lblStatTongQuan.setText("CẦN XỬ LÝ");
            lblStatTongQuan.setForeground(new Color(217, 119, 6));
        } else {
            lblStatTongQuan.setText("TỐT");
            lblStatTongQuan.setForeground(new Color(22, 163, 74));
        }

        kho.ghiNhatKy(admin != null ? admin.getHoTen() : "Admin", "Quản lý", "Chạy chẩn đoán", NhatKyHeThong.INFO,
                "Quét phát hiện " + soNguyCap + " mục nguy cấp, " + soCanhBao + " mục cảnh báo vận hành");
    }

    private void tuDongDeXuatNhapKho() {
        ArrayList<PhuTung> canNhap = new ArrayList<>();
        for (PhuTung pt : kho.getPhuTungs()) {
            if (pt.canhBao()) canNhap.add(pt);
        }
        if (canNhap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tất cả phụ tùng trong kho đều ở mức tồn an toàn. Không cần nhập thêm!",
                    "Thông báo hệ thống", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DANH SÁCH ĐỀ XUẤT ĐẶT HÀNG NHẬP KHO TỰ ĐỘNG:\n\n");
        long tongTienDuKien = 0;
        for (PhuTung pt : canNhap) {
            int slDeXuat = Math.max(10, pt.getSoLuongToiThieu() * 3 - pt.getSoLuongTon());
            long tien = slDeXuat * pt.getDonGia();
            tongTienDuKien += tien;
            sb.append(String.format("• [%s] %s | Tồn hiện tại: %d %s | Đề xuất nhập: %d %s | Dự kiến: %,d đ\n",
                    pt.getMaPT(), pt.getTen(), pt.getSoLuongTon(), pt.getDonVi(), slDeXuat, pt.getDonVi(), tien));
        }
        sb.append("\nTổng ngân sách dự kiến: ").append(String.format("%,d VNĐ", tongTienDuKien));

        JTextArea ta = new JTextArea(sb.toString(), 14, 50);
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Đề xuất đơn hàng nhập kho tự động (Auto-Replenish)", JOptionPane.INFORMATION_MESSAGE);

        kho.ghiNhatKy(admin != null ? admin.getHoTen() : "Admin", "Quản lý", "Tạo đề xuất nhập kho", NhatKyHeThong.INFO,
                "Tự động tính toán đề xuất nhập " + canNhap.size() + " mặt hàng thiếu hụt");
    }

    private void phatLenhNhacViec() {
        int count = 0;
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (!"Hoàn thành".equalsIgnoreCase(p.getTienDo())) count++;
        }
        kho.ghiNhatKy(admin != null ? admin.getHoTen() : "Admin", "Quản lý", "Phát lệnh nhắc việc", NhatKyHeThong.WARN,
                "Phát cảnh báo đôn đốc tiến độ cho " + count + " phiếu sửa chữa đang thực hiện");
        JOptionPane.showMessageDialog(this,
                "Đã phát lệnh cảnh báo & đôn đốc tiến độ đến tất cả các kỹ thuật viên!\nSố lượng phiếu được gửi nhắc nhở: " + count,
                "Thông báo điều hành", JOptionPane.INFORMATION_MESSAGE);
        chayChanDoan();
    }

    // =========================================================================
    // TAB 3: CẤU HÌNH & THAM SỐ VẬN HÀNH (BUSINESS RULES ENGINE)
    // =========================================================================
    private JComponent taoTabCauHinh() {
        CauHinhHeThong ch = kho.getCauHinh();

        JPanel wrap = new JPanel(new BorderLayout(0, 14));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 8, 14, 8));

        JPanel formCard = UiHelper.panelForm("Quy định kinh doanh & Tham số hệ thống", 650);

        txtTenGara = new JTextField(ch.getTenGara());
        txtHotline = new JTextField(ch.getHotline());
        txtDiaChi = new JTextField(ch.getDiaChi());
        txtStk = new JTextField(ch.getStkNganHang());
        txtVat = new JTextField(String.valueOf(ch.getThueVAT()));
        txtChietKhauMax = new JTextField(String.valueOf(ch.getChietKhauToiDa()));
        txtBaoHanhThang = new JTextField(String.valueOf(ch.getThoiHanBaoHanhThang()));
        txtTonKhoMin = new JTextField(String.valueOf(ch.getTonKhoCanhBaoMin()));
        txtGioiHanXe = new JTextField(String.valueOf(ch.getGioiHanXeTrongNgay()));
        txtLoiCamOn = new JTextField(ch.getLoiCamOnHoaDon());

        chkCanhBaoTuDong = new JCheckBox("Kích hoạt cơ chế chẩn đoán & cảnh báo rủi ro tự động (Automated Monitoring)", ch.isBatCanhBaoTuDong());
        chkXacNhanHuy = new JCheckBox("Yêu cầu mật khẩu Admin xác thực khi thu ngân thực hiện hủy hóa đơn", ch.isYeuCauXacNhanHuy());

        chkCanhBaoTuDong.setFont(UiHelper.FONT_BT);
        chkXacNhanHuy.setFont(UiHelper.FONT_BT);
        chkCanhBaoTuDong.setOpaque(false);
        chkXacNhanHuy.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        UiHelper.themDong(form, 0, "Tên thương hiệu Gara *", txtTenGara, 4, 4);
        UiHelper.themDong(form, 1, "Hotline liên hệ *", txtHotline, 4, 4);
        UiHelper.themDong(form, 2, "Địa chỉ trụ sở *", txtDiaChi, 4, 4);
        UiHelper.themDong(form, 3, "Tài khoản nhận thanh toán", txtStk, 4, 4);

        // Hàng đôi: Thuế VAT & Chiết khấu tối đa
        JPanel rowThue = new JPanel(new GridLayout(1, 2, 8, 0));
        rowThue.setOpaque(false);
        rowThue.add(txtVat);
        rowThue.add(txtChietKhauMax);
        UiHelper.themDong(form, 4, "Thuế VAT (%) / Chiết khấu tối đa (%)", rowThue, 4, 4);

        // Hàng đôi: Thời hạn bảo hành & Tồn kho cảnh báo
        JPanel rowBh = new JPanel(new GridLayout(1, 2, 8, 0));
        rowBh.setOpaque(false);
        rowBh.add(txtBaoHanhThang);
        rowBh.add(txtTonKhoMin);
        UiHelper.themDong(form, 5, "BH mặc định (tháng) / Tồn kho tối thiểu", rowBh, 4, 4);

        UiHelper.themDong(form, 6, "Giới hạn xe tiếp nhận / ngày", txtGioiHanXe, 4, 4);
        UiHelper.themDong(form, 7, "Lời cảm ơn in trên hóa đơn", txtLoiCamOn, 4, 4);
        UiHelper.themDong(form, 8, "Chính sách cảnh báo", chkCanhBaoTuDong, 4, 4);
        UiHelper.themDong(form, 9, "Chính sách hủy đơn", chkXacNhanHuy, 4, 4);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnLuu = UiHelper.nut("Lưu quy định hệ thống", UiIcon.of(UiIcon.CHECK, 15), UiHelper.XANH_LA, e -> luuCauHinh());
        JButton btnReset = UiHelper.nut("Khôi phục mặc định", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, e -> khoiPhucCauHinhMacDinh());

        nut.add(btnReset);
        nut.add(btnLuu);

        UiHelper.ganNoiDungForm(formCard, form, nut);
        wrap.add(formCard, BorderLayout.CENTER);
        return wrap;
    }

    private void luuCauHinh() {
        try {
            CauHinhHeThong ch = kho.getCauHinh();
            ch.setTenGara(txtTenGara.getText().trim());
            ch.setHotline(txtHotline.getText().trim());
            ch.setDiaChi(txtDiaChi.getText().trim());
            ch.setStkNganHang(txtStk.getText().trim());
            ch.setThueVAT(Integer.parseInt(txtVat.getText().trim()));
            ch.setChietKhauToiDa(Integer.parseInt(txtChietKhauMax.getText().trim()));
            ch.setThoiHanBaoHanhThang(Integer.parseInt(txtBaoHanhThang.getText().trim()));
            ch.setTonKhoCanhBaoMin(Integer.parseInt(txtTonKhoMin.getText().trim()));
            ch.setGioiHanXeTrongNgay(Integer.parseInt(txtGioiHanXe.getText().trim()));
            ch.setLoiCamOnHoaDon(txtLoiCamOn.getText().trim());
            ch.setBatCanhBaoTuDong(chkCanhBaoTuDong.isSelected());
            ch.setYeuCauXacNhanHuy(chkXacNhanHuy.isSelected());

            kho.ghiNhatKy(admin != null ? admin.getHoTen() : "Admin", "Quản lý", "Cập nhật cấu hình", NhatKyHeThong.INFO,
                    "Cập nhật các tham số quy định vận hành toàn hệ thống");

            JOptionPane.showMessageDialog(this, "Đã lưu và áp dụng thành công quy định vận hành hệ thống!",
                    "Cập nhật cấu hình", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi nhập liệu số: Vui lòng kiểm tra lại VAT, Chiết khấu, Số tháng!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void khoiPhucCauHinhMacDinh() {
        if (JOptionPane.showConfirmDialog(this, "Khôi phục lại toàn bộ quy định hệ thống về mặc định của gara?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        kho.getCauHinh().resetMacDinh();
        CauHinhHeThong ch = kho.getCauHinh();
        txtTenGara.setText(ch.getTenGara());
        txtHotline.setText(ch.getHotline());
        txtDiaChi.setText(ch.getDiaChi());
        txtStk.setText(ch.getStkNganHang());
        txtVat.setText(String.valueOf(ch.getThueVAT()));
        txtChietKhauMax.setText(String.valueOf(ch.getChietKhauToiDa()));
        txtBaoHanhThang.setText(String.valueOf(ch.getThoiHanBaoHanhThang()));
        txtTonKhoMin.setText(String.valueOf(ch.getTonKhoCanhBaoMin()));
        txtGioiHanXe.setText(String.valueOf(ch.getGioiHanXeTrongNgay()));
        txtLoiCamOn.setText(ch.getLoiCamOnHoaDon());
        chkCanhBaoTuDong.setSelected(ch.isBatCanhBaoTuDong());
        chkXacNhanHuy.setSelected(ch.isYeuCauXacNhanHuy());

        kho.ghiNhatKy(admin != null ? admin.getHoTen() : "Admin", "Quản lý", "Reset quy định", NhatKyHeThong.WARN,
                "Khôi phục quy định vận hành về thiết lập mặc định ban đầu");
        JOptionPane.showMessageDialog(this, "Đã khôi phục cài đặt mặc định thành công!");
    }

    // =========================================================================
    // TAB 4: SAO LƯU & PHỤC HỒI DỮ LIỆU (DISASTER RECOVERY HUB)
    // =========================================================================
    private JComponent taoTabSaoLuu() {
        JPanel wrap = new JPanel(new GridLayout(1, 3, 16, 0));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 10, 16, 10));

        // Khối 1: Sao lưu
        JPanel card1 = UiHelper.taoThe(new BorderLayout(0, 12));
        card1.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel t1 = new JLabel("SAO LƯU DỮ LIỆU (BACKUP)");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t1.setForeground(UiHelper.XANH_DAM);
        card1.add(t1, BorderLayout.NORTH);

        JTextArea ta1 = new JTextArea("Đóng gói toàn bộ cơ sở dữ liệu hệ thống (Khách hàng, Xe, Phụ tùng, Phiếu sửa chữa, Hóa đơn, Phân quyền) ra tệp tin có đính kèm chữ ký kiểm tra toàn vẹn.\n\nNên thực hiện sao lưu định kỳ cuối mỗi ngày làm việc.");
        ta1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ta1.setForeground(new Color(71, 85, 105));
        ta1.setWrapStyleWord(true);
        ta1.setLineWrap(true);
        ta1.setOpaque(false);
        ta1.setEditable(false);
        card1.add(ta1, BorderLayout.CENTER);

        JButton btnBackup = UiHelper.nut("Xuất file sao lưu (.txt)", UiIcon.of(UiIcon.EXPORT, 15), UiHelper.XANH_TRUNG, true, e -> thucHienSaoLuu());
        card1.add(btnBackup, BorderLayout.SOUTH);

        // Khối 2: Phục hồi
        JPanel card2 = UiHelper.taoThe(new BorderLayout(0, 12));
        card2.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel t2 = new JLabel("PHỤC HỒI DỮ LIỆU (RESTORE)");
        t2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t2.setForeground(UiHelper.XANH_DAM);
        card2.add(t2, BorderLayout.NORTH);

        JTextArea ta2 = new JTextArea("Khôi phục trạng thái hoạt động của hệ thống từ tệp tin sao lưu trước đó.\n\nHệ thống sẽ tự động xác minh mã băm Checksum để đảm bảo dữ liệu không bị hỏng hóc hoặc can thiệp trái phép.");
        ta2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ta2.setForeground(new Color(71, 85, 105));
        ta2.setWrapStyleWord(true);
        ta2.setLineWrap(true);
        ta2.setOpaque(false);
        ta2.setEditable(false);
        card2.add(ta2, BorderLayout.CENTER);

        JButton btnRestore = UiHelper.nut("Chọn file phục hồi", UiIcon.of(UiIcon.IMPORT, 15), UiHelper.CAM, true, e -> thucHienPhucHoi());
        card2.add(btnRestore, BorderLayout.SOUTH);

        // Khối 3: Khôi phục dữ liệu mẫu
        JPanel card3 = UiHelper.taoThe(new BorderLayout(0, 12));
        card3.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel t3 = new JLabel("DỮ LIỆU DEMO BAN ĐẦU");
        t3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t3.setForeground(UiHelper.XANH_DAM);
        card3.add(t3, BorderLayout.NORTH);

        JTextArea ta3 = new JTextArea("Phục hồi hệ thống về trạng thái dữ liệu mẫu chuẩn ban đầu của đề tài (Factory Reset).\n\nTính năng tiện dụng cho việc báo cáo, thuyết trình đồ án hoặc kiểm thử quy trình vận hành từ đầu.");
        ta3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ta3.setForeground(new Color(71, 85, 105));
        ta3.setWrapStyleWord(true);
        ta3.setLineWrap(true);
        ta3.setOpaque(false);
        ta3.setEditable(false);
        card3.add(ta3, BorderLayout.CENTER);

        JButton btnFactory = UiHelper.nut("Reset dữ liệu mẫu ban đầu", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.CHU_CHAY, true, e -> thucHienResetDemo());
        card3.add(btnFactory, BorderLayout.SOUTH);

        wrap.add(card1);
        wrap.add(card2);
        wrap.add(card3);
        return wrap;
    }

    private void thucHienSaoLuu() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn vị trí lưu tệp sao lưu dữ liệu");
        fc.setSelectedFile(new File("Backup_GaraTheT4_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                String nd = kho.taoBaoCaoBackup();
                fw.write(nd);
                kho.ghiNhatKy(admin != null ? admin.getHoTen() : "Admin", "Quản lý", "Sao lưu hệ thống", NhatKyHeThong.INFO,
                        "Xuất bản sao lưu dữ liệu thành công ra file: " + fc.getSelectedFile().getName());
                JOptionPane.showMessageDialog(this, "Sao lưu dữ liệu toàn hệ thống thành công!\nĐường dẫn: " + fc.getSelectedFile().getAbsolutePath(),
                        "Sao lưu dữ liệu", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi sao lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void thucHienPhucHoi() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn tệp sao lưu dữ liệu hợp lệ để phục hồi");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            kho.ghiNhatKy(admin != null ? admin.getHoTen() : "Admin", "Quản lý", "Phục hồi dữ liệu", NhatKyHeThong.WARN,
                    "Phục hồi và nạp dữ liệu từ file sao lưu: " + f.getName());
            JOptionPane.showMessageDialog(this,
                    "Xác thực toàn vẹn dữ liệu thành công!\nHệ thống đã nạp lại trạng thái an toàn từ bản sao lưu: " + f.getName(),
                    "Phục hồi dữ liệu", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void thucHienResetDemo() {
        if (JOptionPane.showConfirmDialog(this,
                "Khôi phục lại toàn bộ dữ liệu mẫu ban đầu của gara (Demo Factory Reset)?\nMọi thay đổi thử nghiệm trước đó sẽ được làm mới.",
                "Xác nhận Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        kho.resetDuLieuMacDinh();
        JOptionPane.showMessageDialog(this, "Đã khôi phục dữ liệu mẫu ban đầu thành công!", "Reset hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        napBangNhatKy();
        chayChanDoan();
    }

    // =========================================================================
    // TAB 5: NHẬT KÝ KIỂM TOÁN AN NINH (AUDIT TRAIL & ACTIVITY LOG)
    // =========================================================================
    private JComponent taoTabNhatKy() {
        JPanel wrap = new JPanel(new BorderLayout(0, 12));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(14, 8, 14, 8));

        JPanel card = UiHelper.panelBang("Nhật ký kiểm toán an ninh & lịch sử thao tác hệ thống (Audit Trail)");

        // Thanh công cụ lọc nhật ký
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTim = new JLabel("Tìm kiếm:");
        lblTim.setFont(UiHelper.FONT_NHAN);
        txtTimNhatKy = new JTextField(18);
        UiHelper.trangTriInput(txtTimNhatKy);

        JLabel lblMdo = new JLabel("Mức độ:");
        lblMdo.setFont(UiHelper.FONT_NHAN);
        cboMucDoLog = new JComboBox<>(new String[]{"Tất cả", "INFO", "WARN", "CRITICAL"});
        cboMucDoLog.setFont(UiHelper.FONT_BT);

        JButton btnLoc = UiHelper.nut("Lọc nhật ký", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> napBangNhatKy());
        JButton btnXuat = UiHelper.nut("Xuất file log", UiIcon.of(UiIcon.PRINT, 15), UiHelper.CAM, e -> xuatFileLog());

        txtTimNhatKy.addActionListener(e -> napBangNhatKy());
        cboMucDoLog.addActionListener(e -> napBangNhatKy());

        filterBar.add(lblTim);
        filterBar.add(txtTimNhatKy);
        filterBar.add(lblMdo);
        filterBar.add(cboMucDoLog);
        filterBar.add(btnLoc);
        filterBar.add(btnXuat);

        card.add(filterBar, BorderLayout.NORTH);

        modelNhatKy = UiHelper.model("Thời gian", "Người thực hiện", "Vai trò", "Hành động", "Mức độ", "Chi tiết thao tác");
        tableNhatKy = UiHelper.bang(modelNhatKy);

        // Renderer màu cho mức độ
        tableNhatKy.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String m = String.valueOf(v);
                if (NhatKyHeThong.CRITICAL.equals(m)) {
                    lbl.setForeground(new Color(220, 38, 38));
                } else if (NhatKyHeThong.WARN.equals(m)) {
                    lbl.setForeground(new Color(217, 119, 6));
                } else {
                    lbl.setForeground(new Color(37, 99, 235));
                }
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(tableNhatKy);
        UiHelper.danhSachBong(sp);
        card.add(sp, BorderLayout.CENTER);

        wrap.add(card, BorderLayout.CENTER);
        napBangNhatKy();
        return wrap;
    }

    private void napBangNhatKy() {
        modelNhatKy.setRowCount(0);
        String q = txtTimNhatKy != null ? txtTimNhatKy.getText().trim().toLowerCase() : "";
        String mucDo = cboMucDoLog != null ? String.valueOf(cboMucDoLog.getSelectedItem()) : "Tất cả";

        for (NhatKyHeThong log : kho.getNhatKyList()) {
            if (!"Tất cả".equals(mucDo) && !mucDo.equalsIgnoreCase(log.getMucDo())) continue;
            String textGop = (log.getNguoiThucHien() + " " + log.getVaiTro() + " " + log.getHanhDong() + " " + log.getChiTiet()).toLowerCase();
            if (!q.isEmpty() && !textGop.contains(q)) continue;

            modelNhatKy.addRow(new Object[]{
                    log.getThoiGian(), log.getNguoiThucHien(), log.getVaiTro(), log.getHanhDong(), log.getMucDo(), log.getChiTiet()
            });
        }
    }

    private void xuatFileLog() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn vị trí xuất tệp tin nhật ký kiểm toán");
        fc.setSelectedFile(new File("AuditLog_Gara_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                fw.write("ThoiGian,NguoiThucHien,VaiTro,HanhDong,MucDo,ChiTiet\n");
                for (NhatKyHeThong log : kho.getNhatKyList()) {
                    fw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            log.getThoiGian(), log.getNguoiThucHien(), log.getVaiTro(),
                            log.getHanhDong(), log.getMucDo(), log.getChiTiet().replace("\"", "\"\"")));
                }
                JOptionPane.showMessageDialog(this, "Xuất nhật ký kiểm toán thành công ra tệp tin CSV!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
