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
import java.time.LocalDate;
import java.time.YearMonth;
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
    // Đánh dấu đang nạp lại phiếu sửa chữa có sẵn
    private boolean dangNapPhieuSuaChua = false;

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
    private JTextField txtGioHenGiao;
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
        kho.dongBoTuSQLServer();
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

        }

        tinhToanTaiChinh();
        xuLyThayDoiTrangThai();
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

        JButton btnHoanThanh = UiHelper.nut("Hoàn thành", UiIcon.of(UiIcon.CHECK, 15), UiHelper.XANH_LA, e -> hoanThanhPhieuSuaChua());
        JButton btnIn = UiHelper.nut("In phiếu", UiIcon.of(UiIcon.PRINT, 15), new Color(13, 148, 136), e -> {});
        JButton btnLamMoi = UiHelper.nut("Làm mới (F5)", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, e -> lamMoi());
        JButton btnTraCuu = UiHelper.nut("Tra cứu", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_DAM, e -> {
            Main_UI mainUI = Main_UI.getInstance();
            if (mainUI != null) mainUI.chuyenTrang("Tra cứu phiếu sửa chữa");
        });

        right.add(btnHoanThanh);
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

        // Chọn ngày bằng lịch popup, chọn giờ bằng hộp thoại riêng.
        LocalDateTime henMacDinh = LocalDateTime.now().plusHours(3);
        txtNgayHenGiao = new JTextField(henMacDinh.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtNgayHenGiao.setEditable(false);
        txtNgayHenGiao.setFocusable(false);
        UiHelper.trangTriInput(txtNgayHenGiao);

        txtGioHenGiao = new JTextField(henMacDinh.format(DateTimeFormatter.ofPattern("HH:mm")));
        txtGioHenGiao.setEditable(false);
        txtGioHenGiao.setFocusable(false);
        UiHelper.trangTriInput(txtGioHenGiao);

        JButton btnChonNgayHen = UiHelper.nut("Chọn lịch", null, UiHelper.XANH_TRUNG,
                e -> chonNgayBangLich());
        JButton btnChonGioHen = UiHelper.nut("Chọn giờ", null, UiHelper.XANH_TRUNG,
                e -> chonGioHen());

        JPanel pnlNgayHen = new JPanel(new BorderLayout(5, 0));
        pnlNgayHen.setOpaque(false);
        pnlNgayHen.add(txtNgayHenGiao, BorderLayout.CENTER);
        pnlNgayHen.add(btnChonNgayHen, BorderLayout.EAST);

        JPanel pnlGioHen = new JPanel(new BorderLayout(5, 0));
        pnlGioHen.setOpaque(false);
        pnlGioHen.add(txtGioHenGiao, BorderLayout.CENTER);
        pnlGioHen.add(btnChonGioHen, BorderLayout.EAST);

        JPanel pnlHenHoanThanh = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlHenHoanThanh.setOpaque(false);
        pnlHenHoanThanh.add(pnlNgayHen);
        pnlHenHoanThanh.add(pnlGioHen);

        UiHelper.themDong(col1, 0, "Mã phiếu sửa chữa", txtMaPSC);
        UiHelper.themDong(col1, 1, "Phiếu tiếp nhận *", cboPhieuTN);
        UiHelper.themDong(col1, 2, "Biển số xe", lblBienSoXe);
        UiHelper.themDong(col1, 3, "Hãng & Dòng xe", lblHangXeDongXe);
        UiHelper.themDong(col1, 4, "Khách hàng & SĐT", lblKhachHang);
        UiHelper.themDong(col1, 5, "Yêu cầu ban đầu", lblYeuCauDauVao);
        UiHelper.themDong(col1, 6, "Ngày giờ lập", txtNgayLap);
        UiHelper.themDong(col1, 7, "Hẹn hoàn thành", pnlHenHoanThanh);

        // CỘT 2: Phân công KTV & Tiến độ
        JPanel col2 = new JPanel(new GridBagLayout());
        col2.setOpaque(false);

        cboKtvPhuTrach = new JComboBox<>();
        cboKtvPhuTrach.setFont(UiHelper.FONT_BT);
        cboKtvPhuTrach.setBackground(Color.WHITE);
        cboKtvPhuTrach.setPreferredSize(new Dimension(100, 36));
        cboKtvPhuTrach.addActionListener(e -> xuLyChonKtv());

        cboTienDo = new JComboBox<>(new String[]{
                "Chờ phụ tùng", "Chờ KTV"
        });
        cboTienDo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cboTienDo.setBackground(Color.WHITE);
        cboTienDo.setPreferredSize(new Dimension(100, 36));
        cboTienDo.addActionListener(e -> xuLyThayDoiTrangThai());

        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(UiHelper.FONT_BT);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        JScrollPane spGhiChu = new JScrollPane(txtGhiChu);
        spGhiChu.setPreferredSize(new Dimension(190, 80));

        UiHelper.themDong(col2, 0, "KTV phụ trách chính *", cboKtvPhuTrach);
        UiHelper.themDong(col2, 1, "Trạng thái *", cboTienDo);
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

    private LocalDate layNgayHenDangChon() {
        try {
            return LocalDate.parse(
                    txtNgayHenGiao.getText().trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    /** Mở lịch tháng để người dùng bấm chọn ngày. */
    private void chonNgayBangLich() {
        LocalDate ngayDangChon = layNgayHenDangChon();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Chọn ngày hẹn hoàn thành", Dialog.ModalityType.APPLICATION_MODAL);
        final YearMonth[] thangHienTai = {YearMonth.from(ngayDangChon)};
        JPanel pnlLich = new JPanel(new BorderLayout(8, 8));
        pnlLich.setBorder(new EmptyBorder(12, 12, 12, 12));
        JPanel pnlTieuDe = new JPanel(new BorderLayout(8, 0));
        JButton btnThangTruoc = new JButton("<");
        JButton btnThangSau = new JButton(">");
        JLabel lblThang = new JLabel("", SwingConstants.CENTER);
        lblThang.setFont(UiHelper.FONT_TIEU_DE_PHU);
        pnlTieuDe.add(btnThangTruoc, BorderLayout.WEST);
        pnlTieuDe.add(lblThang, BorderLayout.CENTER);
        pnlTieuDe.add(btnThangSau, BorderLayout.EAST);
        JPanel pnlNgay = new JPanel(new GridLayout(0, 7, 4, 4));

        Runnable veLich = () -> {
            pnlNgay.removeAll();
            for (String thu : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) {
                JLabel lbl = new JLabel(thu, SwingConstants.CENTER);
                lbl.setFont(UiHelper.FONT_BT);
                pnlNgay.add(lbl);
            }
            YearMonth thang = thangHienTai[0];
            lblThang.setText("Tháng " + thang.getMonthValue() + " / " + thang.getYear());
            int lech = thang.atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < lech; i++) pnlNgay.add(new JLabel(""));
            for (int d = 1; d <= thang.lengthOfMonth(); d++) {
                final LocalDate ngay = thang.atDay(d);
                JButton btnNgay = new JButton(String.valueOf(d));
                btnNgay.setFocusPainted(false);
                if (ngay.equals(ngayDangChon)) {
                    btnNgay.setBackground(new Color(219, 234, 254));
                    btnNgay.setForeground(new Color(29, 78, 216));
                    btnNgay.setFont(btnNgay.getFont().deriveFont(Font.BOLD));
                }
                btnNgay.addActionListener(e -> {
                    txtNgayHenGiao.setText(ngay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    dialog.dispose();
                });
                pnlNgay.add(btnNgay);
            }
            pnlNgay.revalidate();
            pnlNgay.repaint();
        };
        btnThangTruoc.addActionListener(e -> {
            thangHienTai[0] = thangHienTai[0].minusMonths(1);
            veLich.run();
        });
        btnThangSau.addActionListener(e -> {
            thangHienTai[0] = thangHienTai[0].plusMonths(1);
            veLich.run();
        });
        veLich.run();
        pnlLich.add(pnlTieuDe, BorderLayout.NORTH);
        pnlLich.add(pnlNgay, BorderLayout.CENTER);
        dialog.setContentPane(pnlLich);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Chọn giờ/phút từ danh sách thả xuống. */
    private void chonGioHen() {
        JComboBox<String> cboGio = new JComboBox<>();
        JComboBox<String> cboPhut = new JComboBox<>();
        for (int i = 0; i < 24; i++) cboGio.addItem(String.format("%02d", i));
        for (int i = 0; i < 60; i++) cboPhut.addItem(String.format("%02d", i));
        String[] hienTai = txtGioHenGiao.getText().split(":");
        if (hienTai.length == 2) {
            cboGio.setSelectedItem(hienTai[0]);
            cboPhut.setSelectedItem(hienTai[1]);
        }
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        pnl.add(new JLabel("Giờ:"));
        pnl.add(cboGio);
        pnl.add(new JLabel("Phút:"));
        pnl.add(cboPhut);
        int luaChon = JOptionPane.showConfirmDialog(this, pnl, "Chọn giờ hẹn",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (luaChon == JOptionPane.OK_OPTION) {
            txtGioHenGiao.setText(cboGio.getSelectedItem() + ":" + cboPhut.getSelectedItem());
        }
    }

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
        xuLyThayDoiTrangThai();
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
        xuLyThayDoiTrangThai();
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


    private boolean dangXuLyTrangThai = false;

    private boolean kiemTraDaXuatKho(String maPSC) {
        if (maPSC == null || maPSC.isBlank()) return false;
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT TOP 1 trangThai FROM PhieuXuatKho WHERE maPSC = ?")) {
            ps.setString(1, maPSC.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tt = rs.getString("trangThai");
                    return tt != null && tt.trim().equalsIgnoreCase("Đã xuất");
                }
            }
        } catch (SQLException ignored) {}
        return false;
    }

    private void xuLyThayDoiTrangThai() {
        if (dangXuLyTrangThai || cboTienDo == null || cboKtvPhuTrach == null) return;
        dangXuLyTrangThai = true;
        try {
            String tt = String.valueOf(cboTienDo.getSelectedItem());
            String selPTN = (cboPhieuTN != null && cboPhieuTN.getSelectedIndex() > 0)
                    ? String.valueOf(cboPhieuTN.getSelectedItem()).split(" - ")[0].trim() : null;

            NhanVien ktvRanh = null;
            for (NhanVien nv : kho.getNhanViens()) {
                if (!nv.dangHoatDong()) continue;
                if (Entity.QuyenTruyCap.vaiTro(nv.getVaiTro()) != Entity.QuyenTruyCap.VaiTro.SUA_CHUA) continue;
                String lich = kho.layThongTinLichSuaChuaKtv(nv.getMaNV(), selPTN);
                if (lich == null && ktvRanh == null) {
                    ktvRanh = nv;
                }
            }

            if ("Chờ KTV".equalsIgnoreCase(tt)) {
                if (ktvRanh == null) {
                    // Tất cả KTV đều đã có lịch: làm mờ phần chọn KTV
                    cboKtvPhuTrach.setEnabled(false);
                    cboKtvPhuTrach.setSelectedIndex(0);
                    cboKtvPhuTrach.setToolTipText("Tất cả kỹ thuật viên đều đang có lịch. Đơn sẽ tự động được gán khi có KTV trống lịch.");
                } else {
                    // Có ít nhất 1 KTV trống lịch: tự động gán KTV này
                    cboKtvPhuTrach.setEnabled(true);
                    cboKtvPhuTrach.setToolTipText(null);
                    for (int i = 0; i < cboKtvPhuTrach.getItemCount(); i++) {
                        String it = cboKtvPhuTrach.getItemAt(i);
                        if (it != null && it.startsWith(ktvRanh.getMaNV() + " - ")) {
                            cboKtvPhuTrach.setSelectedIndex(i);
                            break;
                        }
                    }
                    // Khi KTV được gán vào đơn nhưng đơn đó chưa được xuất phụ tùng thì chuyển trạng thái 'Chờ phụ tùng'
                    boolean coPhuTung = modelPhuTung != null && modelPhuTung.getRowCount() > 0;
                    boolean daXuat = kiemTraDaXuatKho(maPSC);
                    if (coPhuTung && !daXuat) {
                        cboTienDo.setSelectedItem("Chờ phụ tùng");
                    }
                }
            } else if ("Chờ phụ tùng".equalsIgnoreCase(tt)) {
                cboKtvPhuTrach.setEnabled(true);
                cboKtvPhuTrach.setToolTipText(null);
                if (cboKtvPhuTrach.getSelectedIndex() <= 0 && ktvRanh != null) {
                    for (int i = 0; i < cboKtvPhuTrach.getItemCount(); i++) {
                        String it = cboKtvPhuTrach.getItemAt(i);
                        if (it != null && it.startsWith(ktvRanh.getMaNV() + " - ")) {
                            cboKtvPhuTrach.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        } finally {
            dangXuLyTrangThai = false;
        }
    }

    private void xuLyChonKtv() {
        if (dangXuLyTrangThai || cboTienDo == null || cboKtvPhuTrach == null) return;
        if (cboKtvPhuTrach.getSelectedIndex() > 0) {
            boolean coPhuTung = modelPhuTung != null && modelPhuTung.getRowCount() > 0;
            boolean daXuat = kiemTraDaXuatKho(maPSC);
            if (coPhuTung && !daXuat) {
                dangXuLyTrangThai = true;
                try {
                    cboTienDo.setSelectedItem("Chờ phụ tùng");
                } finally {
                    dangXuLyTrangThai = false;
                }
            }
        }
    }

    private void hoanTatSauKhiXuat(String maPSC) {
        String sqlCapNhat = "UPDATE PhieuSuaChua SET trangThai = N'HOAN_THANH' WHERE maPSC = ?";
        String sqlCapNhatPTN = "UPDATE PhieuTiepNhan SET trangThaiXe = N'Hoàn thành' " +
                "WHERE maPTN = (SELECT maPTN FROM PhieuSuaChua WHERE maPSC = ?) " +
                "   OR maPTN = ? " +
                "   OR ('TN' + SUBSTRING(?, 3, 18)) = maPTN";

        try (Connection con = ConnectDB.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sqlCapNhat)) {
                ps.setString(1, maPSC);
                ps.executeUpdate();
            }
            try (PreparedStatement psPTN = con.prepareStatement(sqlCapNhatPTN)) {
                psPTN.setString(1, maPSC);
                psPTN.setString(2, maPSC);
                psPTN.setString(3, maPSC);
                psPTN.executeUpdate();
            }
            kho.dongBoTuSQLServer();
            Main_UI.lamMoiThongBao();
            JOptionPane.showMessageDialog(this,
                    "Đã hoàn tất phiếu sửa chữa " + maPSC + ".",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hoàn tất phiếu: " + ex.getMessage());
        }
    }
    private void napPhieuSuaChuaTuCSDL(String maPSCCanMo) {

        if (!BaoVeQuyen.phieu(maPSCCanMo)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Phiếu không thuộc phạm vi được giao."
            );
            return;
        }

        String sqlPSC = """
    SELECT maPSC, maPTN, maNV, soVIN,
           ngayBD, ngayHoanThanhDuKien,
           trangThai, ghiChu
    FROM PhieuSuaChua
    WHERE maPSC = ?
""";

        try (Connection con = ConnectDB.getInstance().getConnection()) {

            String maNV;
            String maPTN;

            // BƯỚC 1: Lấy chính xác thông tin phiếu sửa chữa
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

                    maPTN = rs.getString("maPTN");

                    maNV = rs.getString("maNV");

                    String ghiChu = rs.getString("ghiChu");

                    txtGhiChu.setText(ghiChu != null ? ghiChu : "");

                    txtMaPSC.setText(maPSC);

                    if (lblMa != null) {
                        lblMa.setText("[" + maPSC + "]");
                    }

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
                        txtNgayHenGiao.setText(ngayHen.toLocalDate().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        // CSDL hiện lưu ngày kiểu DATE nên giờ cũ không được lưu lại.
                    }
                }
            }

            // BƯỚC 2: Chọn đúng phiếu tiếp nhận nhưng không sinh lại mã PSC
            dangNapPhieuSuaChua = true;

            try {
                if (maPTN != null && !maPTN.isBlank()) {
                    chonPhieuTiepNhan(maPTN);
                }
            } finally {
                dangNapPhieuSuaChua = false;
            }

            // Đảm bảo mã PSC luôn giữ đúng mã đã lấy từ CSDL
            txtMaPSC.setText(maPSC);

            if (lblMa != null) {
                lblMa.setText("[" + maPSC + "]");
            }

            // BƯỚC 3: Chọn lại kỹ thuật viên
            for (int i = 0; i < cboKtvPhuTrach.getItemCount(); i++) {

                String item = cboKtvPhuTrach.getItemAt(i);

                if (item != null && item.startsWith(maNV + " - ")) {
                    cboKtvPhuTrach.setSelectedIndex(i);
                    break;
                }
            }

            // BƯỚC 4: Xóa dữ liệu cũ trên bảng
            modelCongViec.setRowCount(0);
            modelPhuTung.setRowCount(0);

            // BƯỚC 5: Nạp lại công việc sửa chữa
            String sqlCongViec =
                    "SELECT stt, noiDungCongViec, tenKTV, tienCong " +
                            "FROM ChiTietCongViecSuaChua " +
                            "WHERE maPSC = ? ORDER BY stt";

            try (PreparedStatement ps = con.prepareStatement(sqlCongViec)) {

                ps.setString(1, maPSC);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        int stt = rs.getInt("stt");
                        String noiDung = rs.getString("noiDungCongViec");
                        String tenKTV = rs.getString("tenKTV");

                        java.math.BigDecimal tienBD = rs.getBigDecimal("tienCong");
                        long tienCong = tienBD == null ? 0 : tienBD.longValue();

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

            // BƯỚC 6: Nạp lại phụ tùng sửa chữa
            String sqlChiTiet =
                    "SELECT ct.maPT, ct.tenPhuTung, ct.soLuong, " +
                            "ct.donGia, pt.donViTinh " +
                            "FROM ChiTietSuaChua ct " +
                            "LEFT JOIN PhuTung pt ON ct.maPT = pt.maPT " +
                            "WHERE ct.maPSC = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {

                ps.setString(1, maPSC);

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

            // BƯỚC 7: Tính lại tổng chi phí
            tinhToanTaiChinh();
        xuLyThayDoiTrangThai();

            System.out.println(
                    "Đã mở phiếu sửa chữa: " + maPSC +
                            " | Phiếu tiếp nhận: " + maPTN
            );

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
        String selPTN = (cboPhieuTN != null && cboPhieuTN.getSelectedIndex() > 0)
                ? String.valueOf(cboPhieuTN.getSelectedItem()).split(" - ")[0].trim() : null;
        for (NhanVien nv : kho.getNhanViens()) {
            if (!nv.dangHoatDong()) continue;
            // Chỉ lấy Nhân viên sửa chữa (KTV xưởng), không lấy Quản lý, Tiếp tân, Thu ngân, Thủ kho
            if (Entity.QuyenTruyCap.vaiTro(nv.getVaiTro()) != Entity.QuyenTruyCap.VaiTro.SUA_CHUA) {
                continue;
            }
            String lich = kho.layThongTinLichSuaChuaKtv(nv.getMaNV(), selPTN);
            if (lich != null) {
                cboKtvPhuTrach.addItem(nv.getMaNV() + " - " + nv.getHoTen() + " [ĐÃ CÓ LỊCH: " + lich + "]");
            } else {
                cboKtvPhuTrach.addItem(nv.getMaNV() + " - " + nv.getHoTen() + " [Sẵn sàng]");
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
            napDuLieuKtv();
            return;
        }

        String sel = String.valueOf(cboPhieuTN.getSelectedItem());
        String soPhieu = sel.split(" - ")[0].trim();
        if (!dangNapPhieuSuaChua) {
            this.maPSC = sinhMaPSCTuDong(soPhieu);

            if (txtMaPSC != null) {
                txtMaPSC.setText(this.maPSC);
            }

            if (lblMa != null) {
                lblMa.setText("[" + this.maPSC + "]");
            }
        }
        PhieuTiepNhan p = kho.timPhieuTiepNhan(soPhieu);
        if (p == null) return;

        lblBienSoXe.setText(p.getBienSo());
        lblHangXeDongXe.setText(p.getHangXe() + " " + p.getLoaiXe());
        lblKhachHang.setText(p.getHoTen() + " • SĐT: " + p.getSdt());
        lblYeuCauDauVao.setText(p.getYeuCau());

        // Làm mới danh sách KTV theo phiếu này
        napDuLieuKtv();

        // Tự động chọn KTV nếu đã phân công
        if (p.getKyThuatVien() != null && !p.getKyThuatVien().isEmpty()) {
            for (int i = 0; i < cboKtvPhuTrach.getItemCount(); i++) {
                String it = cboKtvPhuTrach.getItemAt(i);
                if (it != null && (it.startsWith(p.getKyThuatVien() + " - ") || it.contains(p.getKyThuatVien()))) {
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
        xuLyThayDoiTrangThai();
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
        xuLyThayDoiTrangThai();
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
        xuLyThayDoiTrangThai();
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

        String tt = String.valueOf(cboTienDo.getSelectedItem());
        boolean choKtvTatCaBan = "Chờ KTV".equalsIgnoreCase(tt) && !cboKtvPhuTrach.isEnabled();

        if (!choKtvTatCaBan && cboKtvPhuTrach.getSelectedIndex() <= 0) {
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

    private long docSoTien(Object giaTri) {
        String chuoi = String.valueOf(giaTri).trim();
        if (chuoi.contains("-")) throw new IllegalArgumentException("Số tiền không được âm.");
        return Long.parseLong(chuoi.replaceAll("[^0-9]", ""));
    }

    private void hoanThanhPhieuSuaChua() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.SC)) return;
        if (!kiemTraDuLieuTruocKhiLuu()) {
            return;
        }

        String selPTN = String.valueOf(cboPhieuTN.getSelectedItem());
        String maPTN = selPTN.split(" - ")[0].trim();
        if (!BaoVeQuyen.phieu(maPTN)) {
            JOptionPane.showMessageDialog(this, "Phiếu không thuộc phạm vi được giao.");
            return;
        }

        String selKTV = String.valueOf(cboKtvPhuTrach.getSelectedItem());
        String maNV = null;
        if (cboKtvPhuTrach.isEnabled() && cboKtvPhuTrach.getSelectedIndex() > 0 && selKTV.contains(" - ")) {
            maNV = selKTV.split(" - ")[0].trim();
        }

        if (maNV != null) {
            NhanVien nvChon = kho.timNhanVienTheoMa(maNV);
            if (nvChon == null || Entity.QuyenTruyCap.vaiTro(nvChon.getVaiTro()) != Entity.QuyenTruyCap.VaiTro.SUA_CHUA) {
                JOptionPane.showMessageDialog(this,
                        "Chỉ được phân công công việc cho Nhân viên sửa chữa (Kỹ thuật viên)!",
                        "Kỹ thuật viên không hợp lệ",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!Entity.QuyenTruyCap.co(BaoVeQuyen.hienTai(), Entity.QuyenTruyCap.Quyen.PHAN_CONG)
                    && !maNV.equals(BaoVeQuyen.hienTai().getMaNV())) {
                JOptionPane.showMessageDialog(this, "Không được đổi người phụ trách.");
                return;
            }

            // Kiểm tra xem KTV đã có lịch sửa chữa khác chưa
            String lichKtv = kho.layThongTinLichSuaChuaKtv(maNV, maPTN);
            if (lichKtv != null) {
                JOptionPane.showMessageDialog(this,
                        "Kỹ thuật viên " + selKTV + " hiện ĐÃ CÓ LỊCH SỬA CHỮA!\n\n" +
                                "• Đang thực hiện: " + lichKtv + "\n" +
                                "• Quy định điều phối: Mỗi kỹ thuật viên chỉ nhận 1 xe sửa chữa tại một thời điểm.\n\n" +
                                "Vui lòng chọn kỹ thuật viên khác đang [Sẵn sàng] hoặc chọn trạng thái [Chờ KTV].",
                        "Kỹ thuật viên đang có lịch",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Lấy ngày đã chọn từ bộ chọn ngày.
        Date ngayHen;
        try {
            LocalDate ngayHenDaChon = LocalDate.parse(
                    txtNgayHenGiao.getText().trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            ngayHen = Date.valueOf(ngayHenDaChon);

            java.time.LocalTime.parse(txtGioHenGiao.getText().trim(),
                    DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày và giờ hẹn hợp lệ.");
            return;
        }

        boolean coPhuTung = modelPhuTung.getRowCount() > 0;
        boolean daXuatKho = kiemTraDaXuatKho(maPSC);

        String trangThaiLuu;
        if (maNV == null) {
            trangThaiLuu = "CHO_KTV";
        } else if (coPhuTung && !daXuatKho) {
            trangThaiLuu = "CHO_SUA";
        } else {
            trangThaiLuu = "DANG_SUA";
        }

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
                    maPTN, maNV, ngayHen.toLocalDate(), txtGhiChu.getText(), trangThaiLuu, phuTungs, congViecs);
            maPSC = new Service.SuaChuaService().luu(BaoVeQuyen.hienTai(), duLieu);
            kho.dongBoTuSQLServer();
            Main_UI.lamMoiThongBao();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu phiếu sửa chữa:\n" + ex.getMessage(),
                    "Lỗi lưu dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Tự động chuyển đơn xuất phụ tùng nếu phiếu có phụ tùng chưa xuất
        if (coPhuTung && !daXuatKho) {
            XuatKho_UI.datPSCCanChon(maPSC);
            Main_UI mainUI = Main_UI.getInstance();
            if (mainUI != null && mainUI.coQuyenTrang("Xuất kho")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã lưu phiếu sửa chữa " + maPSC + " thành công.\n" +
                                "Hệ thống tự động chuyển sang đơn xuất phụ tùng để thực hiện xuất kho.",
                        "Chuyển đơn xuất phụ tùng",
                        JOptionPane.INFORMATION_MESSAGE
                );
                mainUI.chuyenTrang("Xuất kho");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã lưu phiếu sửa chữa " + maPSC + " và chuyển yêu cầu xuất phụ tùng tới bộ phận kho."
                );
            }
            return;
        }

        // Nếu phiếu không có phụ tùng hoặc phụ tùng đã xuất kho hoàn tất -> Hoàn tất phiếu sửa chữa
        hoanTatSauKhiXuat(maPSC);
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
        LocalDateTime henMacDinh = LocalDateTime.now().plusHours(3);
        txtNgayHenGiao.setText(henMacDinh.toLocalDate().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtGioHenGiao.setText(henMacDinh.toLocalTime().format(
                DateTimeFormatter.ofPattern("HH:mm")));
        txtTenCongViec.setText("");
        txtTienCong.setText("150000");
        cboCongViecMau.setSelectedIndex(0);
        cboPhuTung.setSelectedIndex(0);
        txtSoLuongPT.setText("1");
        modelCongViec.setRowCount(0);
        modelPhuTung.setRowCount(0);
        tinhToanTaiChinh();
        xuLyThayDoiTrangThai();
        JOptionPane.showMessageDialog(this, "Đã làm mới form sửa chữa phương tiện!");
    }

}

