package UI;

import ConnectDB.ConnectDB;
import Entity.*;
import DAO.PhieuXuat_DAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Màn hình lập phiếu sửa chữa cho xe.
 * Phân công kỹ thuật viên, chọn công việc sửa chữa và phụ tùng thay thế.
 */
public class LapPhieuSuaChua_UI extends JPanel {

    private final NhanVien nvHienTai;
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final PhieuXuat_DAO pxkDAO = new PhieuXuat_DAO();

    // Biến tĩnh hỗ trợ đồng bộ chuyển từ màn hình Tra cứu sang
    private static String phieuCanChonSan = null;
    public static void datPhieuCanChon(String soPhieu) {
        phieuCanChonSan = soPhieu;
    }

    // Dùng khi mở phiếu sửa chữa từ màn hình khác
    private static String pscCanMoLai = null;

    public static void datPSCCanMoLai(String maPSC) {
        pscCanMoLai = maPSC;
    }

    private String maPSC;
    private JTextField txtMaPSC;
    private JLabel lblMa;

    // Khối 1: Tiếp nhận xe & Phân công
    private JComboBox<String> cboPhieuTN;
    private JLabel lblBienSoXe;
    private JLabel lblKhachHang;
    private JLabel lblHangXeDongXe;
    private JLabel lblYeuCauDauVao;
    private JTextField txtNgayLap;
    private JTextField txtNgayHenGiao;
    private JComboBox<String> cboKtvPhuTrach;
    private JComboBox<String> cboTienDo;
    private JTextArea txtGhiChu;

    // Khối 2: Công việc sửa chữa
    private DefaultTableModel modelCongViec;
    private JTable tblCongViec;
    private JComboBox<String> cboCongViecMau;
    private JTextField txtTenCongViec;
    private JTextField txtTienCong;

    // Khối 3: Phụ tùng thay thế
    private DefaultTableModel modelPhuTung;
    private JTable tblPhuTung;
    private JComboBox<String> cboPhuTung;
    private JTextField txtSoLuongPT;
    private JLabel lblGiaPT;

    // Khối 4: Tổng kết tài chính
    private JLabel lblTienCong;
    private JLabel lblTienPhuTung;
    private JLabel lblTienVat;
    private JLabel lblTongChiPhi;

    public LapPhieuSuaChua_UI(NhanVien nv) {
        this.nvHienTai = nv;
        this.maPSC = sinhMaPSCMoi();

        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        setupPhimTat();

        JPanel root = UiHelper.nen();
        root.setLayout(new BorderLayout(0, 14));

        // Header trên cùng
        root.add(taoHeaderPanel(), BorderLayout.NORTH);

        // Nội dung cuộn ở giữa
        JPanel content = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                Container p = getParent();
                if (p instanceof JViewport) {
                    d.width = p.getWidth();
                }
                return d;
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // 1. Khối thông tin tiếp nhận & Phân công
        content.add(taoKhoiThongTinVaPhanCong());
        content.add(Box.createVerticalStrut(14));

        // 2. Khối 2 bảng: Công việc sửa chữa & Phụ tùng thay thế
        content.add(taoKhoiChiTietSuaChua());
        content.add(Box.createVerticalStrut(14));

        // 3. Khối tổng kết tài chính
        content.add(taoKhoiTongKetTaiChinh());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        root.add(scrollPane, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);

        // Nạp dữ liệu ban đầu
        napDuLieuTiepNhan();
        napDuLieuPhuTung();
        napDuLieuKtv();

        // Ưu tiên mở lại phiếu sửa chữa đã lưu từ màn hình xuất kho
        if (pscCanMoLai != null && !pscCanMoLai.isBlank()) {
            String maCanMo = pscCanMoLai;
            pscCanMoLai = null;

            napPhieuSuaChuaTuCSDL(maCanMo);

        } else if (phieuCanChonSan != null) {
            chonPhieuTiepNhan(phieuCanChonSan);
            phieuCanChonSan = null;

        } else {
            napPhieuMauDauTien();
        }

        tinhToanTaiChinh();
    }

    private void setupPhimTat() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "lamMoi");
        am.put("lamMoi", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { lamMoi(); }
        });
    }

    private JPanel taoHeaderPanel() {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel lblTde = UiHelper.tieuDeTrang("Phiếu sửa chữa phương tiện");
        lblMa = new JLabel("[" + maPSC + "]");
        lblMa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMa.setForeground(UiHelper.XANH_TRUNG);
        left.add(lblTde);
        left.add(lblMa);
        p.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        JButton btnLuu = UiHelper.nut("Yêu cầu xuất kho", UiIcon.of(UiIcon.CHECK, 15), UiHelper.XANH_LA, e -> luuPhieuSuaChua());
        JButton btnHoanTat = UiHelper.nut("Hoàn tất phiếu", new Color(22, 163, 74), e -> hoanTatPhieuSuaChua());
        JButton btnIn = UiHelper.nut("In phiếu", UiIcon.of(UiIcon.PRINT, 15), new Color(13, 148, 136), e -> {});
        JButton btnLamMoi = UiHelper.nut("Làm mới (F5)", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, e -> lamMoi());
        JButton btnTraCuu = UiHelper.nut("Tra cứu", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_DAM, e -> {
            Main_UI mainUI = Main_UI.getInstance();
            if (mainUI != null) mainUI.chuyenTrang("Tra cứu phiếu sửa chữa");
        });

        right.add(btnLuu);
        right.add(btnHoanTat);
        right.add(btnIn);
        right.add(btnLamMoi);
        right.add(btnTraCuu);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    // ===============================================================
    // KHỐI 1: THÔNG TIN TIẾP NHẬN & PHÂN CÔNG
    // ===============================================================
    private JPanel taoKhoiThongTinVaPhanCong() {
        JPanel card = UiHelper.taoThe(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblHeader = new JLabel("1. THÔNG TIN TIẾP NHẬN XE & PHÂN CÔNG KỸ THUẬT");
        lblHeader.setFont(UiHelper.FONT_TIEU_DE_PHU);
        lblHeader.setForeground(UiHelper.XANH_DAM);
        card.add(lblHeader, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);

        // CỘT 1: Thông tin xe & Khách hàng
        JPanel col1 = new JPanel(new GridBagLayout());
        col1.setOpaque(false);

// Khởi tạo phiếu tiếp nhận trước khi thêm vào giao diện
        cboPhieuTN = new JComboBox<>();
        cboPhieuTN.setFont(UiHelper.FONT_BT);
        cboPhieuTN.setBackground(Color.WHITE);
        cboPhieuTN.setPreferredSize(new Dimension(100, 36));

// Cập nhật thông tin khi chọn phiếu tiếp nhận
        cboPhieuTN.addActionListener(e -> khiChonPhieuTiepNhan());

        // Mã phiếu sửa chữa tự động
        txtMaPSC = new JTextField(maPSC);
        txtMaPSC.setEditable(false);
        txtMaPSC.setFocusable(false);
        txtMaPSC.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtMaPSC.setForeground(UiHelper.XANH_TRUNG);
        txtMaPSC.setBackground(new Color(241, 245, 249));
        UiHelper.trangTriInput(txtMaPSC);

        lblBienSoXe = new JLabel("-");
        lblBienSoXe.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBienSoXe.setForeground(UiHelper.XANH_TRUNG);

        lblHangXeDongXe = new JLabel("-");
        lblHangXeDongXe.setFont(UiHelper.FONT_BT);

        lblKhachHang = new JLabel("-");
        lblKhachHang.setFont(UiHelper.FONT_BT);

        lblYeuCauDauVao = new JLabel("-");
        lblYeuCauDauVao.setFont(UiHelper.FONT_BT);
        lblYeuCauDauVao.setForeground(new Color(220, 38, 38));

        txtNgayLap = new JTextField(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtNgayLap.setEditable(false);
        UiHelper.trangTriInput(txtNgayLap);

        txtNgayHenGiao = new JTextField(LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        UiHelper.trangTriInput(txtNgayHenGiao);

        UiHelper.themDong(col1, 0, "Mã phiếu sửa chữa", txtMaPSC);
        UiHelper.themDong(col1, 1, "Phiếu tiếp nhận *", cboPhieuTN);
        UiHelper.themDong(col1, 2, "Biển số xe", lblBienSoXe);
        UiHelper.themDong(col1, 3, "Hãng & Dòng xe", lblHangXeDongXe);
        UiHelper.themDong(col1, 4, "Khách hàng & SĐT", lblKhachHang);
        UiHelper.themDong(col1, 5, "Yêu cầu ban đầu", lblYeuCauDauVao);
        UiHelper.themDong(col1, 6, "Ngày giờ lập", txtNgayLap);
        UiHelper.themDong(col1, 7, "Hẹn hoàn thành", txtNgayHenGiao);

        // CỘT 2: Phân công KTV & Tiến độ
        JPanel col2 = new JPanel(new GridBagLayout());
        col2.setOpaque(false);

        cboKtvPhuTrach = new JComboBox<>();
        cboKtvPhuTrach.setFont(UiHelper.FONT_BT);
        cboKtvPhuTrach.setBackground(Color.WHITE);
        cboKtvPhuTrach.setPreferredSize(new Dimension(100, 36));

        cboTienDo = new JComboBox<>(new String[]{
                "Đang sửa", "Chờ phụ tùng", "Hoàn thành", "Chờ tiếp nhận"
        });
        cboTienDo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cboTienDo.setBackground(Color.WHITE);
        cboTienDo.setPreferredSize(new Dimension(100, 36));

        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(UiHelper.FONT_BT);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setText("Khách phản ánh máy hơi rung giật khi dừng đèn đỏ. Đã kiểm tra bugi và lọc gió.");
        JScrollPane spGhiChu = new JScrollPane(txtGhiChu);
        spGhiChu.setPreferredSize(new Dimension(190, 80));

        UiHelper.themDong(col2, 0, "KTV phụ trách chính *", cboKtvPhuTrach);
        UiHelper.themDong(col2, 1, "Tiến độ sửa chữa *", cboTienDo);
        UiHelper.themDong(col2, 2, "Chẩn đoán / Ghi chú", spGhiChu);

        grid.add(col1);
        grid.add(col2);
        card.add(grid, BorderLayout.CENTER);

        return card;
    }

    public static String sinhMaPSCTuDong(String maPTN) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null && maPTN != null && !maPTN.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT TOP 1 maPSC FROM PhieuSuaChua WHERE maPTN = ? ORDER BY ngayBD DESC")) {
                ps.setString(1, maPTN.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String existing = rs.getString(1);
                        if (existing != null && !existing.isBlank()) return existing;
                    }
                }
            } catch (SQLException ignored) {}
        }
        return sinhMaPSCMoi();
    }

    public static String sinhMaPSCMoi() {
        Connection con = ConnectDB.getInstance().getConnection();
        java.util.HashSet<Integer> dsSo = new java.util.HashSet<>();
        if (con != null) {
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT maPSC FROM PhieuSuaChua")) {
                while (rs.next()) {
                    String ma = rs.getString(1);
                    if (ma != null && ma.toUpperCase().startsWith("SC")) {
                        try {
                            int so = Integer.parseInt(ma.replaceAll("[^0-9]", ""));
                            dsSo.add(so);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (SQLException ignored) {}
        }
        for (Entity.PhieuTiepNhan p : KhoDuLieu.get().getPhieuTiepNhans()) {
            if (p.getSoPhieu() != null) {
                try {
                    int so = Integer.parseInt(p.getSoPhieu().replaceAll("[^0-9]", ""));
                    dsSo.add(so);
                } catch (Exception ignored) {}
            }
        }
        int soMoi = 1;
        while (dsSo.contains(soMoi)) {
            soMoi++;
        }
        return String.format("SC%03d", soMoi);
    }

    // ===============================================================
    // KHỐI 2: CHI TIẾT CÔNG VIỆC & PHỤ TÙNG XUẤT KHO
    // ===============================================================
    private JPanel taoKhoiChiTietSuaChua() {
        JPanel card = new JPanel(new GridLayout(1, 2, 14, 0));
        card.setOpaque(false);

        // BẢNG 1: CÔNG VIỆC SỬA CHỮA
        JPanel pnlCongViec = UiHelper.taoThe(new BorderLayout(0, 8));
        pnlCongViec.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblHeaderCV = new JLabel("2. HẠNG MỤC CÔNG VIỆC & TIỀN CÔNG");
        lblHeaderCV.setFont(UiHelper.FONT_TIEU_DE_PHU);
        lblHeaderCV.setForeground(UiHelper.XANH_DAM);
        pnlCongViec.add(lblHeaderCV, BorderLayout.NORTH);

        modelCongViec = UiHelper.model("STT", "Nội dung công việc", "KTV", "Tiền công (đ)", "Xóa");
        tblCongViec = UiHelper.bang(modelCongViec);
        tblCongViec.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblCongViec.getColumnModel().getColumn(0).setPreferredWidth(35);  // STT
        tblCongViec.getColumnModel().getColumn(1).setPreferredWidth(170); // Nội dung
        tblCongViec.getColumnModel().getColumn(2).setPreferredWidth(110); // KTV
        tblCongViec.getColumnModel().getColumn(3).setPreferredWidth(110); // Tiền công
        tblCongViec.getColumnModel().getColumn(4).setPreferredWidth(45);  // Xóa

        UiHelper.canGiua(tblCongViec, 0, 2, 4);
        UiHelper.canTrai(tblCongViec, 1);
        UiHelper.canPhai(tblCongViec, 3);

        tblCongViec.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tblCongViec.columnAtPoint(e.getPoint());
                int row = tblCongViec.rowAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    modelCongViec.removeRow(row);
                    capNhatStt(modelCongViec);
                    tinhToanTaiChinh();
                }
            }
        });

        JScrollPane spCV = new JScrollPane(tblCongViec);
        spCV.setPreferredSize(new Dimension(0, 160));
        UiHelper.danhSachBong(spCV);
        pnlCongViec.add(spCV, BorderLayout.CENTER);

        // Thanh thêm nhanh công việc
        JPanel pnlThemCV = new JPanel(new BorderLayout(6, 6));
        pnlThemCV.setOpaque(false);
        pnlThemCV.setBorder(new EmptyBorder(8, 0, 0, 0));

        cboCongViecMau = new JComboBox<>(new String[]{
                "-- Chọn công việc mẫu hoặc tự nhập --",
                "Thay dầu động cơ & lọc nhớt (150,000 đ)",
                "Bảo dưỡng hệ thống phanh 4 bánh (250,000 đ)",
                "Vệ sinh kim phun & buồng đốt bu-gi (200,000 đ)",
                "Cân chỉnh góc đặt bánh xe (300,000 đ)",
                "Kiểm tra và nạp gas điều hòa (350,000 đ)",
                "Bảo dưỡng định kỳ 40.000 km (800,000 đ)",
                "Sửa chữa hệ thống điện & quét lỗi ECU (450,000 đ)"
        });
        cboCongViecMau.setFont(UiHelper.FONT_BT);
        cboCongViecMau.setBackground(Color.WHITE);
        cboCongViecMau.setPreferredSize(new Dimension(100, 36));
        cboCongViecMau.addActionListener(e -> khiChonCongViecMau());

        JPanel pnlNhapCV = new JPanel(new BorderLayout(6, 0));
        pnlNhapCV.setOpaque(false);
        txtTenCongViec = new JTextField();
        txtTenCongViec.setToolTipText("Nhập nội dung công việc sửa chữa...");
        UiHelper.trangTriInput(txtTenCongViec);

        txtTienCong = new JTextField("150000");
        txtTienCong.setPreferredSize(new Dimension(100, 36));
        UiHelper.trangTriInput(txtTienCong);

        JButton btnThemCV = UiHelper.nut("Thêm", UiIcon.of(UiIcon.PLUS, 14), UiHelper.XANH_TRUNG, e -> themCongViec());

        pnlNhapCV.add(txtTenCongViec, BorderLayout.CENTER);
        pnlNhapCV.add(txtTienCong, BorderLayout.EAST);

        pnlThemCV.add(cboCongViecMau, BorderLayout.NORTH);
        pnlThemCV.add(pnlNhapCV, BorderLayout.CENTER);
        pnlThemCV.add(btnThemCV, BorderLayout.EAST);

        pnlCongViec.add(pnlThemCV, BorderLayout.SOUTH);

        // BẢNG 2: PHỤ TÙNG XUẤT KHO
        JPanel pnlPhuTung = UiHelper.taoThe(new BorderLayout(0, 8));
        pnlPhuTung.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblHeaderPT = new JLabel("3. PHỤ TÙNG & VẬT TƯ THAY THẾ XUẤT KHO");
        lblHeaderPT.setFont(UiHelper.FONT_TIEU_DE_PHU);
        lblHeaderPT.setForeground(UiHelper.XANH_DAM);
        pnlPhuTung.add(lblHeaderPT, BorderLayout.NORTH);

        modelPhuTung = UiHelper.model("STT", "Mã PT", "Tên phụ tùng", "ĐVT", "SL", "Đơn giá (đ)", "Thành tiền (đ)", "Xóa");
        tblPhuTung = UiHelper.bang(modelPhuTung);
        tblPhuTung.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblPhuTung.getColumnModel().getColumn(0).setPreferredWidth(35);  // STT
        tblPhuTung.getColumnModel().getColumn(1).setPreferredWidth(65);  // Mã
        tblPhuTung.getColumnModel().getColumn(2).setPreferredWidth(150); // Tên
        tblPhuTung.getColumnModel().getColumn(3).setPreferredWidth(45);  // ĐVT
        tblPhuTung.getColumnModel().getColumn(4).setPreferredWidth(40);  // SL
        tblPhuTung.getColumnModel().getColumn(5).setPreferredWidth(95);  // Đơn giá
        tblPhuTung.getColumnModel().getColumn(6).setPreferredWidth(105); // Thành tiền
        tblPhuTung.getColumnModel().getColumn(7).setPreferredWidth(45);  // Xóa

        UiHelper.canGiua(tblPhuTung, 0, 1, 3, 4, 7);
        UiHelper.canTrai(tblPhuTung, 2);
        UiHelper.canPhai(tblPhuTung, 5, 6);

        tblPhuTung.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tblPhuTung.columnAtPoint(e.getPoint());
                int row = tblPhuTung.rowAtPoint(e.getPoint());
                if (col == 7 && row >= 0) {
                    modelPhuTung.removeRow(row);
                    capNhatStt(modelPhuTung);
                    tinhToanTaiChinh();
                }
            }
        });

        JScrollPane spPT = new JScrollPane(tblPhuTung);
        spPT.setPreferredSize(new Dimension(0, 160));
        UiHelper.danhSachBong(spPT);
        pnlPhuTung.add(spPT, BorderLayout.CENTER);

        // Thanh thêm phụ tùng từ kho
        JPanel pnlThemPT = new JPanel(new BorderLayout(6, 6));
        pnlThemPT.setOpaque(false);
        pnlThemPT.setBorder(new EmptyBorder(8, 0, 0, 0));

        cboPhuTung = new JComboBox<>();
        cboPhuTung.setFont(UiHelper.FONT_BT);
        cboPhuTung.setBackground(Color.WHITE);
        cboPhuTung.setPreferredSize(new Dimension(100, 36));
        cboPhuTung.addActionListener(e -> capNhatGiaPhuTungDangChon());

        JPanel pnlNhapPT = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlNhapPT.setOpaque(false);

        JLabel lblSl = new JLabel("SL:");
        lblSl.setFont(UiHelper.FONT_NHAN);
        txtSoLuongPT = new JTextField("1");
        txtSoLuongPT.setPreferredSize(new Dimension(45, 36));
        UiHelper.trangTriInput(txtSoLuongPT);

        lblGiaPT = new JLabel("0 đ");
        lblGiaPT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGiaPT.setForeground(new Color(30, 64, 175));

        JButton btnThemPT = UiHelper.nut("Xuất kho", UiIcon.of(UiIcon.PLUS, 14), UiHelper.XANH_LA, e -> themPhuTung());

        pnlNhapPT.add(lblSl);
        pnlNhapPT.add(txtSoLuongPT);
        pnlNhapPT.add(lblGiaPT);
        pnlNhapPT.add(btnThemPT);

        pnlThemPT.add(cboPhuTung, BorderLayout.NORTH);
        pnlThemPT.add(pnlNhapPT, BorderLayout.CENTER);

        pnlPhuTung.add(pnlThemPT, BorderLayout.SOUTH);

        card.add(pnlCongViec);
        card.add(pnlPhuTung);
        return card;
    }


    private void hoanTatPhieuSuaChua() {
        if (!BaoVeQuyen.phieu(maPSC)) { JOptionPane.showMessageDialog(this,"Phiếu không thuộc phạm vi được giao."); return; }
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.SC)) return;
        if (maPSC == null || maPSC.isBlank()) {
            JOptionPane.showMessageDialog(this, "Mã phiếu sửa chữa không hợp lệ.");
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn hoàn tất phiếu sửa chữa " + maPSC + "?",
                "Xác nhận hoàn tất",
                JOptionPane.YES_NO_OPTION
        );

        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        String sqlKiemTra =
                "SELECT trangThai FROM PhieuXuatKho WHERE maPSC = ?";

        String sqlCapNhat =
                "UPDATE PhieuSuaChua SET trangThai = N'HOAN_THANH' " +
                        "WHERE maPSC = ?";

        try (Connection con = ConnectDB.getInstance().getConnection()) {

            // Kiểm tra phiếu xuất kho đã được xác nhận xuất hoàn toàn chưa
            try (PreparedStatement ps = con.prepareStatement(sqlKiemTra)) {
                ps.setString(1, maPSC);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Phiếu sửa chữa chưa có phiếu xuất kho.\n" +
                                        "Vui lòng chuyển sang màn hình Xuất kho."
                        );
                        return;
                    }

                    String trangThaiXuat = rs.getString("trangThai");

                    if (trangThaiXuat == null ||
                            !trangThaiXuat.trim().equalsIgnoreCase("Đã xuất")) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Phiếu xuất kho chưa hoàn tất.\n" +
                                        "Chỉ được hoàn tất sửa chữa khi trạng thái xuất kho là Đã xuất."
                        );
                        return;
                    }
                }
            }

            // Cập nhật trạng thái phiếu sửa chữa
            try (PreparedStatement ps = con.prepareStatement(sqlCapNhat)) {
                ps.setString(1, maPSC);

                int soDong = ps.executeUpdate();

                if (soDong == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Không tìm thấy phiếu sửa chữa " + maPSC
                    );
                    return;
                }
            }

            // Đồng bộ trạng thái trên giao diện
            cboTienDo.setSelectedItem("Hoàn thành");

            JOptionPane.showMessageDialog(
                    this,
                    "Đã hoàn tất phiếu sửa chữa " + maPSC + ".",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi hoàn tất phiếu sửa chữa:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }


    private void napPhieuSuaChuaTuCSDL(String maPSCCanMo) {
        if (!BaoVeQuyen.phieu(maPSCCanMo)) { JOptionPane.showMessageDialog(this,"Phiếu không thuộc phạm vi được giao."); return; }
        String sqlPSC =
                "SELECT maPSC, maNV, soVIN, ngayBD, ngayHoanThanhDuKien " +
                        "FROM PhieuSuaChua WHERE maPSC = ?";

        try (Connection con = ConnectDB.getInstance().getConnection()) {

            // BƯỚC 1: Lấy thông tin phiếu sửa chữa
            String maNV;
            String soVIN;

            try (PreparedStatement ps = con.prepareStatement(sqlPSC)) {
                ps.setString(1, maPSCCanMo);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Không tìm thấy phiếu sửa chữa: " + maPSCCanMo
                        );
                        return;
                    }

                    maPSC = rs.getString("maPSC");
                    maNV = rs.getString("maNV");
                    soVIN = rs.getString("soVIN");

                    txtMaPSC.setText(maPSC);
                    if (lblMa != null) lblMa.setText("[" + maPSC + "]");

                    Timestamp ngayBD = rs.getTimestamp("ngayBD");
                    if (ngayBD != null) {
                        txtNgayLap.setText(
                                ngayBD.toLocalDateTime().format(
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                )
                        );
                    }

                    Date ngayHen = rs.getDate("ngayHoanThanhDuKien");
                    if (ngayHen != null) {
                        txtNgayHenGiao.setText(
                                ngayHen.toLocalDate()
                                        .atStartOfDay()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        );
                    }
                }
            }

            // BƯỚC 2: Chọn lại kỹ thuật viên
            for (int i = 0; i < cboKtvPhuTrach.getItemCount(); i++) {
                String item = cboKtvPhuTrach.getItemAt(i);

                if (item != null && item.startsWith(maNV + " - ")) {
                    cboKtvPhuTrach.setSelectedIndex(i);
                    break;
                }
            }

            // BƯỚC 3: Khôi phục thông tin phiếu tiếp nhận theo VIN
            String maPTN = null;
            String sqlPTN =
                    "SELECT TOP 1 maPTN " +
                            "FROM PhieuTiepNhan " +
                            "WHERE soVIN = ? " +
                            "ORDER BY maPTN DESC";

            try (PreparedStatement ps = con.prepareStatement(sqlPTN)) {
                ps.setString(1, soVIN);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maPTN = rs.getString("maPTN");
                    }
                }
            }

            if (maPTN != null) {
                chonPhieuTiepNhan(maPTN);
            }

            // BƯỚC 4: Xóa dữ liệu cũ trên bảng trước khi nạp lại
            modelCongViec.setRowCount(0);
            modelPhuTung.setRowCount(0);


            // Nạp lại hạng mục công việc đã lưu
            String sqlCongViec =
                    "SELECT stt, noiDungCongViec, tenKTV, tienCong " +
                            "FROM ChiTietCongViecSuaChua " +
                            "WHERE maPSC = ? ORDER BY stt";

            try (PreparedStatement ps = con.prepareStatement(sqlCongViec)) {
                ps.setString(1, maPSCCanMo);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int stt = rs.getInt("stt");
                        String noiDung = rs.getString("noiDungCongViec");
                        String tenKTV = rs.getString("tenKTV");
                        long tienCong = rs.getBigDecimal("tienCong").longValue();

                        modelCongViec.addRow(new Object[]{
                                stt,
                                noiDung,
                                tenKTV,
                                UiHelper.tien(tienCong),
                                "✕"
                        });
                    }
                }
            }


            // BƯỚC 5: Nạp phụ tùng đã lưu trong phiếu sửa chữa
            String sqlChiTiet =
                    "SELECT ct.maPT, ct.tenPhuTung, ct.soLuong, " +
                            "ct.donGia, pt.donViTinh " +
                            "FROM ChiTietSuaChua ct " +
                            "LEFT JOIN PhuTung pt ON ct.maPT = pt.maPT " +
                            "WHERE ct.maPSC = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                ps.setString(1, maPSCCanMo);

                try (ResultSet rs = ps.executeQuery()) {
                    int stt = 1;

                    while (rs.next()) {
                        String maPT = rs.getString("maPT");
                        String tenPT = rs.getString("tenPhuTung");
                        int soLuong = rs.getInt("soLuong");

                        java.math.BigDecimal giaBD = rs.getBigDecimal("donGia");
                        long donGia = giaBD == null ? 0 : giaBD.longValue();

                        String donVi = rs.getString("donViTinh");
                        if (donVi == null) {
                            donVi = "";
                        }

                        modelPhuTung.addRow(new Object[]{
                                stt++,
                                maPT,
                                tenPT,
                                donVi,
                                soLuong,
                                UiHelper.tien(donGia),
                                UiHelper.tien((long) soLuong * donGia),
                                "✕"
                        });
                    }
                }
            }

            tinhToanTaiChinh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải phiếu sửa chữa:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }


    // ===============================================================
    // KHỐI 3: TỔNG KẾT TÀI CHÍNH
    // ===============================================================
    private JPanel taoKhoiTongKetTaiChinh() {
        JPanel card = UiHelper.taoThe(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        left.setOpaque(false);

        lblTienCong = new JLabel("Tiền công: 0 đ");
        lblTienCong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTienCong.setForeground(new Color(71, 85, 105));

        lblTienPhuTung = new JLabel("Phụ tùng: 0 đ");
        lblTienPhuTung.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTienPhuTung.setForeground(new Color(71, 85, 105));

        lblTienVat = new JLabel("VAT (8%): 0 đ");
        lblTienVat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTienVat.setForeground(new Color(100, 116, 139));

        left.add(lblTienCong);
        left.add(new JLabel("•"));
        left.add(lblTienPhuTung);
        left.add(new JLabel("•"));
        left.add(lblTienVat);

        card.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel lblLblTong = new JLabel("TỔNG CHI PHÍ DỰ KIẾN:");
        lblLblTong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLblTong.setForeground(new Color(30, 58, 138));

        lblTongChiPhi = new JLabel("0 đ");
        lblTongChiPhi.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTongChiPhi.setForeground(new Color(220, 38, 38));

        right.add(lblLblTong);
        right.add(lblTongChiPhi);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    // ===============================================================
    // CÁC HÀM XỬ LÝ DỮ LIỆU
    // ===============================================================
    private void napDuLieuTiepNhan() {
        cboPhieuTN.removeAllItems();
        cboPhieuTN.addItem("-- Chọn phiếu tiếp nhận sửa chữa --");
        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (!BaoVeQuyen.phieu(p.getSoPhieu())) continue;
            cboPhieuTN.addItem(p.getSoPhieu() + " - " + p.getHoTen() + " (" + p.getBienSo() + ")");
        }
    }

    private void napDuLieuPhuTung() {
        cboPhuTung.removeAllItems();
        cboPhuTung.addItem("-- Chọn phụ tùng từ kho --");
        for (PhuTung pt : kho.getPhuTungs()) {
            cboPhuTung.addItem(pt.getMaPT() + " - " + pt.getTen() + " (Tồn " + pt.getSoLuongTon() + " " + pt.getDonVi() + ")");
        }
        capNhatGiaPhuTungDangChon();
    }

    private void napDuLieuKtv() {
        cboKtvPhuTrach.removeAllItems();
        cboKtvPhuTrach.addItem("-- Chọn kỹ thuật viên phụ trách --");
        for (NhanVien nv : kho.getNhanViens()) {
            if (!nv.dangHoatDong()) continue;
            String vt = nv.getVaiTro() == null ? "" : nv.getVaiTro().toLowerCase();
            if (vt.contains("sửa chữa") || vt.contains("quản lý") || vt.contains("ktv")
                    || vt.contains("kỹ thuật") || vt.contains("máy") || vt.contains("gầm") || vt.contains("điện")) {
                cboKtvPhuTrach.addItem(nv.getMaNV() + " - " + nv.getHoTen());
            }
        }
    }

    public void chonPhieuTiepNhan(String soPhieu) {
        if (soPhieu == null) return;
        for (int i = 0; i < cboPhieuTN.getItemCount(); i++) {
            String item = cboPhieuTN.getItemAt(i);
            if (item != null && item.startsWith(soPhieu)) {
                cboPhieuTN.setSelectedIndex(i);
                break;
            }
        }
    }

    private void khiChonPhieuTiepNhan() {
        int idx = cboPhieuTN.getSelectedIndex();
        if (idx <= 0) {
            lblBienSoXe.setText("-");
            lblHangXeDongXe.setText("-");
            lblKhachHang.setText("-");
            lblYeuCauDauVao.setText("-");
            return;
        }

        String sel = String.valueOf(cboPhieuTN.getSelectedItem());
        String soPhieu = sel.split(" - ")[0].trim();
        this.maPSC = sinhMaPSCTuDong(soPhieu);
        if (txtMaPSC != null) txtMaPSC.setText(this.maPSC);
        if (lblMa != null) lblMa.setText("[" + this.maPSC + "]");
        PhieuTiepNhan p = kho.timPhieuTiepNhan(soPhieu);
        if (p == null) return;

        lblBienSoXe.setText(p.getBienSo());
        lblHangXeDongXe.setText(p.getHangXe() + " " + p.getLoaiXe());
        lblKhachHang.setText(p.getHoTen() + " • SĐT: " + p.getSdt());
        lblYeuCauDauVao.setText(p.getYeuCau());

        // Tự động chọn KTV nếu đã phân công
        if (p.getKyThuatVien() != null && !p.getKyThuatVien().isEmpty()) {
            for (int i = 0; i < cboKtvPhuTrach.getItemCount(); i++) {
                String it = cboKtvPhuTrach.getItemAt(i);
                if (it != null && (it.contains(p.getKyThuatVien()) || it.startsWith(p.getKyThuatVien()))) {
                    cboKtvPhuTrach.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Tự động chọn tiến độ
        if (p.getTienDo() != null) {
            for (int i = 0; i < cboTienDo.getItemCount(); i++) {
                if (cboTienDo.getItemAt(i).equalsIgnoreCase(p.getTienDo())) {
                    cboTienDo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Tự động nạp chi tiết công việc và phụ tùng nếu đã có
        modelCongViec.setRowCount(0);
        modelPhuTung.setRowCount(0);
        java.util.List<ChiTietDichVu> ds = kho.layChiTietTheoPhieu(soPhieu);
        for (ChiTietDichVu ct : ds) {
            if ("Tiền công".equalsIgnoreCase(ct.getLoai())) {
                int stt = modelCongViec.getRowCount() + 1;
                String ktv = (p.getKyThuatVien() != null && !p.getKyThuatVien().isEmpty()) ? p.getKyThuatVien() : "KTV Xưởng";
                modelCongViec.addRow(new Object[]{stt, ct.getTenHangMuc(), ktv, UiHelper.tien(ct.getDonGia()), "✕"});
            } else {
                int stt = modelPhuTung.getRowCount() + 1;
                modelPhuTung.addRow(new Object[]{stt, ct.getMaPT(), ct.getTenHangMuc(), ct.getDonVi(), ct.getSoLuong(), UiHelper.tien(ct.getDonGia()), UiHelper.tien(ct.getThanhTien()), "✕"});
            }
        }
        tinhToanTaiChinh();
    }

    private void napPhieuMauDauTien() {
        if (cboPhieuTN.getItemCount() > 1) {
            cboPhieuTN.setSelectedIndex(1);
        }
        // Thêm dữ liệu mẫu ban đầu trực quan
        modelCongViec.setRowCount(0);
        modelCongViec.addRow(new Object[]{1, "Bảo dưỡng định kỳ & Thay dầu máy", "Lê Văn Cường", UiHelper.tien(150000), "✕"});
        modelCongViec.addRow(new Object[]{2, "Kiểm tra hệ thống phanh 4 bánh", "Hoàng Đình Kha", UiHelper.tien(250000), "✕"});

        modelPhuTung.setRowCount(0);
        PhuTung pt1 = kho.timPhuTung("PT001");
        if (pt1 != null) {
            modelPhuTung.addRow(new Object[]{1, pt1.getMaPT(), pt1.getTen(), pt1.getDonVi(), 1, UiHelper.tien(pt1.getDonGia()), UiHelper.tien(pt1.getDonGia()), "✕"});
        }
        PhuTung pt2 = kho.timPhuTung("PT002");
        if (pt2 != null) {
            modelPhuTung.addRow(new Object[]{2, pt2.getMaPT(), pt2.getTen(), pt2.getDonVi(), 1, UiHelper.tien(pt2.getDonGia()), UiHelper.tien(pt2.getDonGia()), "✕"});
        }
        tinhToanTaiChinh();
    }

    private void khiChonCongViecMau() {
        int idx = cboCongViecMau.getSelectedIndex();
        if (idx <= 0) return;
        String sel = String.valueOf(cboCongViecMau.getSelectedItem());
        if (sel.contains("Thay dầu")) {
            txtTenCongViec.setText("Thay dầu động cơ & lọc nhớt");
            txtTienCong.setText("150000");
        } else if (sel.contains("phanh")) {
            txtTenCongViec.setText("Bảo dưỡng hệ thống phanh 4 bánh");
            txtTienCong.setText("250000");
        } else if (sel.contains("kim phun")) {
            txtTenCongViec.setText("Vệ sinh kim phun & buồng đốt bu-gi");
            txtTienCong.setText("200000");
        } else if (sel.contains("góc đặt bánh")) {
            txtTenCongViec.setText("Cân chỉnh góc đặt bánh xe");
            txtTienCong.setText("300000");
        } else if (sel.contains("điều hòa")) {
            txtTenCongViec.setText("Kiểm tra và nạp gas điều hòa");
            txtTienCong.setText("350000");
        } else if (sel.contains("40.000")) {
            txtTenCongViec.setText("Bảo dưỡng định kỳ 40.000 km");
            txtTienCong.setText("800000");
        } else if (sel.contains("ECU")) {
            txtTenCongViec.setText("Sửa chữa hệ thống điện & quét lỗi ECU");
            txtTienCong.setText("450000");
        }
    }

    private void capNhatGiaPhuTungDangChon() {
        int idx = cboPhuTung.getSelectedIndex();
        if (idx <= 0) {
            lblGiaPT.setText("0 đ");
            return;
        }
        String sel = String.valueOf(cboPhuTung.getSelectedItem());
        String maPT = sel.split(" - ")[0].trim();
        PhuTung pt = kho.timPhuTung(maPT);
        if (pt != null) {
            lblGiaPT.setText(UiHelper.tien(pt.getDonGia()) + " đ");
        }
    }

    private void themCongViec() {
        String ten = txtTenCongViec.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung công việc!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long gia;

        try {
            gia = Long.parseLong(
                    txtTienCong.getText().trim().replaceAll("[^0-9]", "")
            );

            if (gia <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Tiền công phải lớn hơn 0!",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập tiền công hợp lệ!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String ktv = (String) cboKtvPhuTrach.getSelectedItem();
        if (ktv == null || ktv.startsWith("--")) ktv = "Chưa phân công";
        else if (ktv.contains(" - ")) ktv = ktv.split(" - ")[1];

        int stt = modelCongViec.getRowCount() + 1;
        modelCongViec.addRow(new Object[]{stt, ten, ktv, UiHelper.tien(gia), "✕"});
        txtTenCongViec.setText("");
        tinhToanTaiChinh();
    }


    private void themPhuTung() {
        int idx = cboPhuTung.getSelectedIndex();

        if (idx <= 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn phụ tùng từ kho!",
                    "Nhắc nhở", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String sel = String.valueOf(cboPhuTung.getSelectedItem());
        String maPT = sel.split(" - ")[0].trim();
        PhuTung pt = kho.timPhuTung(maPT);

        if (pt == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phụ tùng.");
            return;
        }

        int sl;
        try {
            sl = Integer.parseInt(txtSoLuongPT.getText().trim());

            if (sl <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this, "Số lượng phải là số nguyên lớn hơn 0!",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Tính cả số lượng đã thêm trước đó của cùng phụ tùng.
        int dongTrung = -1;
        int tongSL = sl;

        for (int i = 0; i < modelPhuTung.getRowCount(); i++) {
            String maDangCo = String.valueOf(modelPhuTung.getValueAt(i, 1));

            if (maDangCo.equals(maPT)) {
                dongTrung = i;

                try {
                    tongSL += Integer.parseInt(
                            String.valueOf(modelPhuTung.getValueAt(i, 4))
                    );
                } catch (NumberFormatException ignored) {
                }

                break;
            }
        }

        if (tongSL > pt.getSoLuongTon()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tổng số lượng yêu cầu vượt tồn kho hiện có.\n"
                            + "Phụ tùng: " + pt.getTen()
                            + "\nTồn kho hiện tại: " + pt.getSoLuongTon()
                            + " " + pt.getDonVi(),
                    "Cảnh báo tồn kho", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (dongTrung >= 0) {
            // Phụ tùng đã có: cộng thêm số lượng.
            modelPhuTung.setValueAt(tongSL, dongTrung, 4);
            modelPhuTung.setValueAt(
                    UiHelper.tien((long) tongSL * pt.getDonGia()),
                    dongTrung, 6
            );
        } else {
            // Phụ tùng mới: thêm vào bảng.
            int stt = modelPhuTung.getRowCount() + 1;

            modelPhuTung.addRow(new Object[]{
                    stt,
                    pt.getMaPT(),
                    pt.getTen(),
                    pt.getDonVi(),
                    sl,
                    UiHelper.tien(pt.getDonGia()),
                    UiHelper.tien((long) sl * pt.getDonGia()),
                    "✕"
            });
        }

        txtSoLuongPT.setText("1");
        tinhToanTaiChinh();
    }


    private void capNhatStt(DefaultTableModel m) {
        for (int i = 0; i < m.getRowCount(); i++) {
            m.setValueAt(i + 1, i, 0);
        }
    }

    private void tinhToanTaiChinh() {
        long tongCong = 0;
        for (int i = 0; i < modelCongViec.getRowCount(); i++) {
            try {
                String val = modelCongViec.getValueAt(i, 3).toString().replaceAll("[^0-9]", "");
                tongCong += Long.parseLong(val);
            } catch (Exception ignored) {}
        }

        long tongPT = 0;
        for (int i = 0; i < modelPhuTung.getRowCount(); i++) {
            try {
                String val = modelPhuTung.getValueAt(i, 6).toString().replaceAll("[^0-9]", "");
                tongPT += Long.parseLong(val);
            } catch (Exception ignored) {}
        }

        long vat = (long) ((tongCong + tongPT) * 0.08);
        long tongTatCa = tongCong + tongPT + vat;

        lblTienCong.setText("Tiền công: " + UiHelper.tien(tongCong) + " đ");
        lblTienPhuTung.setText("Phụ tùng: " + UiHelper.tien(tongPT) + " đ");
        lblTienVat.setText("VAT (8%): " + UiHelper.tien(vat) + " đ");
        lblTongChiPhi.setText(UiHelper.tien(tongTatCa) + " VNĐ");
    }


    private boolean kiemTraDuLieuTruocKhiLuu() {
        if (cboPhieuTN.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn phiếu tiếp nhận!"
            );
            return false;
        }

        if (cboKtvPhuTrach.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng phân công kỹ thuật viên!"
            );
            return false;
        }

        if (modelCongViec.getRowCount() == 0
                && modelPhuTung.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng thêm ít nhất một công việc hoặc phụ tùng!"
            );
            return false;
        }

        return true;
    }
    // ===============================================================
    // LƯU & ĐỒNG BỘ DỮ LIỆU
    // ===============================================================

    private long docSoTien(Object giaTri) {
        String chuoi = String.valueOf(giaTri).trim();
        if (chuoi.contains("-")) throw new IllegalArgumentException("Số tiền không được âm.");
        return Long.parseLong(chuoi.replaceAll("[^0-9]", ""));
    }

    private void luuPhieuSuaChua() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.SC)) return;
        if (!kiemTraDuLieuTruocKhiLuu()) {
            return;
        }

        String selPTN = String.valueOf(cboPhieuTN.getSelectedItem());
        String maPTN = selPTN.split(" - ")[0].trim();
        if (!BaoVeQuyen.phieu(maPTN)) { JOptionPane.showMessageDialog(this,"Phiếu không thuộc phạm vi được giao."); return; }

        String selKTV = String.valueOf(cboKtvPhuTrach.getSelectedItem());
        if (selKTV.startsWith("--") || !selKTV.contains(" - ")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn kỹ thuật viên!");
            return;
        }
        String maNV = selKTV.split(" - ")[0].trim();
        if (!Entity.QuyenTruyCap.co(BaoVeQuyen.hienTai(),Entity.QuyenTruyCap.Quyen.PHAN_CONG) && !maNV.equals(BaoVeQuyen.hienTai().getMaNV())) { JOptionPane.showMessageDialog(this,"Không được đổi người phụ trách."); return; }

        // Chuyển ngày hẹn sang kiểu SQL DATE.
        Date ngayHen;
        try {
            LocalDateTime ngayHenDateTime = LocalDateTime.parse(
                    txtNgayHenGiao.getText().trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            ngayHen = Date.valueOf(ngayHenDateTime.toLocalDate());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Ngày hẹn không hợp lệ. Định dạng: dd/MM/yyyy HH:mm"
            );
            return;
        }

        // Chỉ yêu cầu xuất kho khi phiếu có phụ tùng.
        boolean coPhuTung = modelPhuTung.getRowCount() > 0;

        try {
            java.util.List<ChiTietDichVu> phuTungs = new java.util.ArrayList<>();
            for (int i = 0; i < modelPhuTung.getRowCount(); i++) {
                phuTungs.add(new ChiTietDichVu("Phụ tùng",
                        String.valueOf(modelPhuTung.getValueAt(i, 1)),
                        String.valueOf(modelPhuTung.getValueAt(i, 2)),
                        String.valueOf(modelPhuTung.getValueAt(i, 3)),
                        Integer.parseInt(String.valueOf(modelPhuTung.getValueAt(i, 4))),
                        docSoTien(modelPhuTung.getValueAt(i, 5))));
            }
            java.util.List<Entity.DuLieuPhieuSuaChua.CongViec> congViecs = new java.util.ArrayList<>();
            for (int i = 0; i < modelCongViec.getRowCount(); i++) {
                congViecs.add(new Entity.DuLieuPhieuSuaChua.CongViec(
                        String.valueOf(modelCongViec.getValueAt(i, 1)),
                        String.valueOf(modelCongViec.getValueAt(i, 2)),
                        docSoTien(modelCongViec.getValueAt(i, 3))));
            }
            Entity.DuLieuPhieuSuaChua duLieu = new Entity.DuLieuPhieuSuaChua(
                    maPTN, maNV, ngayHen.toLocalDate(), phuTungs, congViecs);
            maPSC = new Service.SuaChuaService().luu(BaoVeQuyen.hienTai(), duLieu);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu phiếu sửa chữa:\n" + ex.getMessage(),
                    "Lỗi lưu dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (coPhuTung) {
            // Truyền mã phiếu sửa chữa sang màn hình xuất kho.

            JOptionPane.showMessageDialog(
                    this,
                    "Đã lưu tạm phiếu sửa chữa " + maPSC
                            + ".\nTiếp theo, thủ kho cần xác nhận xuất phụ tùng.",
                    "Đã lưu tạm",
                    JOptionPane.INFORMATION_MESSAGE
            );

            Main_UI mainUI = Main_UI.getInstance();
            if (mainUI != null && mainUI.coQuyenTrang("Xuất kho")) {
                XuatKho_UI.datPSCCanChon(maPSC);
                mainUI.chuyenTrang("Xuất kho");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Yêu cầu đã lưu. Bộ phận kho sẽ xử lý xuất phụ tùng."
                );
            }
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Đã lưu phiếu sửa chữa " + maPSC
                        + ". Phiếu không có phụ tùng nên không cần xác nhận xuất kho."
        );
    }



    private void lamMoi() {
        this.maPSC = sinhMaPSCMoi();
        if (txtMaPSC != null) {
            txtMaPSC.setText(this.maPSC);
        }
        if (lblMa != null) {
            lblMa.setText("[" + this.maPSC + "]");
        }
        txtNgayLap.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtNgayHenGiao.setText(LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtTenCongViec.setText("");
        txtTienCong.setText("150000");
        cboCongViecMau.setSelectedIndex(0);
        cboPhuTung.setSelectedIndex(0);
        txtSoLuongPT.setText("1");
        modelCongViec.setRowCount(0);
        modelPhuTung.setRowCount(0);
        tinhToanTaiChinh();
        JOptionPane.showMessageDialog(this, "Đã làm mới form sửa chữa phương tiện!");
    }

}
