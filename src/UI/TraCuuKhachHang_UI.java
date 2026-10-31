package UI;

import DAO.KhachHang_DAO;
import DAO.TraCuuKhachHang_DAO;
import Entity.KhachHang;
import Entity.KhoDuLieu;
import Entity.PhuongTien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Màn hình tra cứu thông tin khách hàng và lịch sử sửa chữa.
 */
public class TraCuuKhachHang_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí (Tự động nhận diện)",
            "Họ và tên khách hàng",
            "Số điện thoại",
            "Biển số xe sở hữu",
            "Số CCCD / CMND",
            "Mã khách hàng"
    });
    private final JLabel lblGoiY = new JLabel();
    private final JLabel lblDemKetQua = new JLabel("Đang tải dữ liệu...");
    private String boLocNhanh = "TAT_CA";

    private final DefaultTableModel model = UiHelper.model(
            "Mã KH", "Họ tên", "SĐT", "CCCD", "Xe sở hữu", "Địa chỉ", "Email", "Ghi chú", "Ngày ĐK");
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final TraCuuKhachHang_DAO traCuuDAO = new TraCuuKhachHang_DAO();
    private final KhachHang_DAO khachHangDAO = new KhachHang_DAO();
    private JTable table;

    public TraCuuKhachHang_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("Tra cứu khách hàng");
        JLabel phu = new JLabel("Tìm kiếm thông tin khách hàng, xem danh sách xe sở hữu và lịch sử sửa chữa tại gara");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(Box.createVerticalStrut(4));
        north.add(phu);
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Kết quả tra cứu khách hàng");

        // KHUNG TÌM KIẾM CHI TIẾT & HƯỚNG DẪN RÕ RÀNG
        JPanel searchBox = taoKhungTimKiem();
        bangPanel.add(searchBox, BorderLayout.NORTH);

        // BẢNG DANH SÁCH KHÁCH HÀNG
        table = UiHelper.bang(model);
        // AUTO_RESIZE_OFF giữ nguyên độ rộng cột, cuộn ngang khi cần
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(85);   // Mã KH
        table.getColumnModel().getColumn(1).setPreferredWidth(160);  // Họ tên
        table.getColumnModel().getColumn(2).setPreferredWidth(105);  // SĐT
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // CCCD
        table.getColumnModel().getColumn(4).setPreferredWidth(160);  // Xe sở hữu
        table.getColumnModel().getColumn(5).setPreferredWidth(300);  // Địa chỉ — tăng để hiện đủ
        table.getColumnModel().getColumn(6).setPreferredWidth(145);  // Email
        table.getColumnModel().getColumn(7).setPreferredWidth(120);  // Ghi chú
        table.getColumnModel().getColumn(8).setPreferredWidth(100);  // Ngày ĐK

        UiHelper.canGiua(table, 0, 2, 3, 4, 8);
        UiHelper.canTrai(table, 1, 5, 6, 7);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    xemChiTiet();
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        // THANH THAO TÁC DƯỚI BẢNG
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 4, 4));

        lblDemKetQua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDemKetQua.setForeground(new Color(71, 85, 105));
        footer.add(lblDemKetQua, BorderLayout.WEST);

        JPanel boxNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boxNut.setOpaque(false);

        JButton btnXemChiTiet = UiHelper.nut("Xem chi tiết & Xe", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> xemChiTiet());
        JButton btnXemLichSu = UiHelper.nut("Xem lịch sử sửa chữa", UiIcon.of(UiIcon.CLOCK, 15), UiHelper.CAM, e -> xemLichSuSuaChua());
        JButton btnTiepNhan = UiHelper.nut("Lập phiếu tiếp nhận cho khách", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> lapPhieuTiepNhanChoKhach());

        boxNut.add(btnXemChiTiet);
        boxNut.add(btnXemLichSu);
        boxNut.add(btnTiepNhan);
        footer.add(boxNut, BorderLayout.EAST);

        bangPanel.add(footer, BorderLayout.SOUTH);

        root.add(bangPanel, BorderLayout.CENTER);
        capNhatPlaceholderGoiY();
        napBang();
    }

    private JPanel taoKhungTimKiem() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(0, 0, 12, 0));

        // Hàng 1: Tiêu chí lọc + Ô nhập + Nút tìm
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setOpaque(false);

        JPanel pnlTieuChi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlTieuChi.setOpaque(false);
        JLabel lblTieuChi = new JLabel("Tìm theo:");
        lblTieuChi.setFont(UiHelper.FONT_NHAN);
        lblTieuChi.setForeground(new Color(51, 65, 85));
        cboTieuChi.setPreferredSize(new Dimension(220, 38));
        cboTieuChi.setFont(UiHelper.FONT_BT);
        cboTieuChi.setBackground(Color.WHITE);
        cboTieuChi.addActionListener(e -> {
            capNhatPlaceholderGoiY();
            napBang();
        });
        pnlTieuChi.add(lblTieuChi);
        pnlTieuChi.add(cboTieuChi);
        row1.add(pnlTieuChi, BorderLayout.WEST);

        txtTuKhoa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTuKhoa.setPreferredSize(new Dimension(380, 38));
        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { napBang(); }
            @Override public void removeUpdate(DocumentEvent e) { napBang(); }
            @Override public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        row1.add(txtTuKhoa, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlNut.setOpaque(false);
        JButton btnTim = UiHelper.nut("Tìm kiếm", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> napBang());
        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, e -> {
            txtTuKhoa.setText("");
            cboTieuChi.setSelectedIndex(0);
            boLocNhanh = "TAT_CA";
            napBang();
        });
        pnlNut.add(btnTim);
        pnlNut.add(btnLamMoi);
        row1.add(pnlNut, BorderLayout.EAST);
        p.add(row1);

        p.add(Box.createVerticalStrut(8));

        // Hàng 2: Bộ lọc nhanh & Hướng dẫn nhập chi tiết
        JPanel row2 = new JPanel(new BorderLayout(10, 0));
        row2.setOpaque(false);

        JPanel pnlChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlChips.setOpaque(false);
        JLabel lblLocNhanh = new JLabel("Lọc nhanh:");
        lblLocNhanh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLocNhanh.setForeground(new Color(100, 116, 139));
        pnlChips.add(lblLocNhanh);

        JButton chipTatCa = taoChipButton("Tất cả", "TAT_CA");
        JButton chipVip = taoChipButton("Khách VIP", "VIP");
        JButton chipDangSua = taoChipButton("Có xe đang sửa", "DANG_SUA");
        JButton chipMoi = taoChipButton("Khách mới đăng ký", "MOI");

        pnlChips.add(chipTatCa);
        pnlChips.add(chipVip);
        pnlChips.add(chipDangSua);
        pnlChips.add(chipMoi);
        row2.add(pnlChips, BorderLayout.WEST);

        p.add(row2);
        p.add(Box.createVerticalStrut(8));

        // Hàng 3: Hộp gợi ý & giải thích rõ ràng giúp người dùng không mơ hồ
        JPanel boxHint = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(191, 219, 254));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boxHint.setOpaque(false);
        boxHint.setBorder(new EmptyBorder(4, 10, 4, 10));

        lblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGoiY.setForeground(new Color(30, 64, 175));
        boxHint.add(lblGoiY);
        p.add(boxHint);

        return p;
    }

    private JButton taoChipButton(String text, String kieu) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(51, 65, 85));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        btn.addActionListener(e -> {
            this.boLocNhanh = kieu;
            napBang();
        });
        return btn;
    }

    private void capNhatPlaceholderGoiY() {
        int idx = cboTieuChi.getSelectedIndex();
        switch (idx) {
            case 1 -> {
                txtTuKhoa.setToolTipText("Nhập họ tên đầy đủ hoặc từ ngữ trong tên (VD: Minh Quân, Mai...)");
                lblGoiY.setText("Nhập Họ tên khách hàng (VD: Nguyễn Minh Quân, Trần Ngọc Mai...) để tìm kiếm nhanh.");
            }
            case 2 -> {
                txtTuKhoa.setToolTipText("Nhập số điện thoại 10 số (VD: 0912000001...)");
                lblGoiY.setText("Nhập Số điện thoại (VD: 0912000001, 0969...) để tìm kiếm chính xác khách hàng.");
            }
            case 3 -> {
                txtTuKhoa.setToolTipText("Nhập biển số xe khách hàng sở hữu (VD: 51A-12345, 59A...)");
                lblGoiY.setText("Nhập Biển số xe (VD: 51A-12345, 59A-67890). Hệ thống sẽ tìm khách hàng đang sở hữu xe này.");
            }
            case 4 -> {
                txtTuKhoa.setToolTipText("Nhập số thẻ Căn cước công dân / CMND (VD: 079000000001...)");
                lblGoiY.setText("Nhập Số CCCD / CMND (VD: 079000000001, 074206...) của chủ sở hữu.");
            }
            case 5 -> {
                txtTuKhoa.setToolTipText("Nhập mã định danh khách hàng (VD: KH001, KH002...)");
                lblGoiY.setText("Nhập Mã khách hàng (VD: KH001, KH002...) để định danh chính xác.");
            }
            default -> {
                txtTuKhoa.setToolTipText("Nhập Số điện thoại, Họ tên, Biển số xe (51A-12345), số CCCD hoặc Mã KH...");
                lblGoiY.setText("Hệ thống tự động quét cả Họ tên, Số điện thoại (10 số), Biển số xe (VD: 51A-12345), CCCD và Mã KH.");
            }
        }
    }

    private void napBang() {
        String q = txtTuKhoa.getText().trim();
        int tieuChi = cboTieuChi.getSelectedIndex();
        model.setRowCount(0);

        // 1. Tra cứu trực tiếp từ CSDL SQL Server thông qua TraCuuKhachHang_DAO
        ArrayList<KhachHang> danhSach = traCuuDAO.traCuuKhachHang(q, tieuChi, boLocNhanh);

        // 2. Dự phòng nếu offline thì tìm trên bộ nhớ cache
        if (danhSach.isEmpty() && q.isEmpty() && "TAT_CA".equals(boLocNhanh)) {
            danhSach = kho.getKhachHangs();
        }

        for (KhachHang kh : danhSach) {
            String bienSoXe = traCuuDAO.layDanhSachBienSoXeCuaKhach(kh.getMaKH());
            model.addRow(new Object[]{
                    kh.getMaKH(), kh.getHoTen(), kh.getSdt(), kh.getCccd(),
                    bienSoXe,
                    kh.getDiaChi(), kh.getEmail(), kh.getGhiChu(), kh.getNgayDangKyHienThi()
            });
        }

        int tongSo = khachHangDAO.layTongSoKhachHang();
        if (tongSo == 0) tongSo = kho.getKhachHangs().size();
        lblDemKetQua.setText("Tìm thấy: " + danhSach.size() + " / " + tongSo + " khách hàng");
    }

    /** Xem thông tin chi tiết khách hàng và danh sách xe sở hữu */
    private void xemChiTiet() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trong bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maKH = model.getValueAt(dong, 0).toString();
        KhachHang kh = khachHangDAO.timKhachHangTheoMa(maKH);
        if (kh == null) {
            kh = kho.timKhachHangTheoMa(maKH);
        }
        if (kh == null) return;
        final KhachHang finalKh = kh;

        // Truy vấn danh sách xe sở hữu trực tiếp qua TraCuuKhachHang_DAO
        ArrayList<PhuongTien> dsXe = traCuuDAO.layDanhSachXeCuaKhach(maKH);
        StringBuilder sbXe = new StringBuilder();
        int demXe = dsXe.size();

        for (PhuongTien xe : dsXe) {
            sbXe.append("  • Biển số: <b>").append(xe.getBienSo()).append("</b> — ")
                .append(xe.getHangXe()).append(" ").append(xe.getDongXe())
                .append(" (Màu: ").append(xe.getMauSon()).append(", Năm SX: ").append(xe.getNamSX())
                .append(", ODO: ").append(xe.getOdo()).append(" km)<br>");
        }
        if (demXe == 0) sbXe.append("  <i>Khách hàng chưa đăng ký phương tiện nào trong hệ thống gara</i><br>");

        String msg = "<html><div style='width:450px;font-family:Segoe UI;'>"
                + "<h3 style='color:#0F172A;margin-bottom:8px;'>HỒ SƠ CHI TIẾT KHÁCH HÀNG</h3>"
                + "<table cellpadding='3' style='font-size:12px;'>"
                + "<tr><td width='120'><b>Mã khách hàng:</b></td><td><span style='color:#2563EB;font-weight:bold;'>" + kh.getMaKH() + "</span></td></tr>"
                + "<tr><td><b>Họ và tên:</b></td><td><b>" + kh.getHoTen() + "</b></td></tr>"
                + "<tr><td><b>Số điện thoại:</b></td><td>" + kh.getSdt() + "</td></tr>"
                + "<tr><td><b>CCCD/CMND:</b></td><td>" + kh.getCccd() + "</td></tr>"
                + "<tr><td><b>Email:</b></td><td>" + kh.getEmail() + "</td></tr>"
                + "<tr><td><b>Địa chỉ:</b></td><td>" + kh.getDiaChi() + "</td></tr>"
                + "<tr><td><b>Ngày đăng ký:</b></td><td>" + kh.getNgayDangKyHienThi() + "</td></tr>"
                + "<tr><td><b>Ghi chú / Nhóm:</b></td><td>" + kh.getGhiChu() + "</td></tr>"
                + "</table>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;margin:10px 0;'>"
                + "<h4 style='color:#2563EB;margin-bottom:6px;'>DANH SÁCH XE SỞ HỮU (" + demXe + " xe):</h4>"
                + sbXe.toString()
                + "</div></html>";

        JOptionPane.showMessageDialog(this, msg, "Hồ sơ khách hàng", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Xem lịch sử sửa chữa của khách hàng */
    private void xemLichSuSuaChua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trong bảng để xem lịch sử sửa chữa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maKH = model.getValueAt(dong, 0).toString();
        KhachHang kh = khachHangDAO.timKhachHangTheoMa(maKH);
        if (kh == null) {
            kh = kho.timKhachHangTheoMa(maKH);
        }
        if (kh == null) return;
        final KhachHang finalKh = kh;

        // Truy vấn lịch sử trực tiếp qua TraCuuKhachHang_DAO
        ArrayList<TraCuuKhachHang_DAO.LichSuSuaChuaDTO> dsLs = traCuuDAO.layLichSuSuaChuaCuaKhach(maKH);

        if (dsLs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Khách hàng [" + kh.getMaKH() + "] " + kh.getHoTen() + " chưa có lịch sử tiếp nhận / sửa chữa nào trong hệ thống!",
                    "Lịch sử sửa chữa", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lịch sử sửa chữa — " + kh.getHoTen(), true);
        dlg.setSize(920, 500);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new BorderLayout(12, 12));
        pnl.setBackground(UiHelper.NEN);
        pnl.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTieuDe = new JLabel("Lịch sử sửa chữa của khách hàng: " + kh.getHoTen() + " — SĐT: " + kh.getSdt() + " (" + dsLs.size() + " lần tiếp nhận)");
        lblTieuDe.setFont(UiHelper.FONT_TIEU_DE_PHU);
        lblTieuDe.setForeground(UiHelper.XANH_DAM);
        pnl.add(lblTieuDe, BorderLayout.NORTH);

        DefaultTableModel mdLs = UiHelper.model("Số phiếu", "Ngày tiếp nhận", "Biển số", "Hãng / Loại", "Yêu cầu sửa chữa", "KTV xử lý", "Tiến độ", "Chi phí");
        for (TraCuuKhachHang_DAO.LichSuSuaChuaDTO dto : dsLs) {
            mdLs.addRow(new Object[]{
                    dto.getSoPhieu(), dto.getNgayTiepNhan(), dto.getBienSo(),
                    dto.getXe(), dto.getYeuCau(), dto.getKtvXuLy(), dto.getTienDo(),
                    UiHelper.tien(dto.getChiPhi()) + " đ"
            });
        }

        JTable tblLs = UiHelper.bang(mdLs);
        tblLs.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UiHelper.canGiua(tblLs, 0, 1, 2, 6);
        UiHelper.canTrai(tblLs, 3, 4, 5);
        UiHelper.canPhai(tblLs, 7);

        JScrollPane spLs = new JScrollPane(tblLs);
        UiHelper.danhSachBong(spLs);
        pnl.add(spLs, BorderLayout.CENTER);

        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, e -> dlg.dispose());
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setOpaque(false);
        JButton btnMoToanManHinh = UiHelper.nut("Mở tra cứu chi tiết", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, ev -> {
            dlg.dispose();
            Main_UI mainUI = Main_UI.getInstance();
            if (mainUI != null) {
                mainUI.chuyenTrang("Tra cứu lịch sử sửa chữa", new TraCuuLichSuSuaChua_UI(finalKh.getSdt(), 2));
            }
        });
        pnlBot.add(btnMoToanManHinh);
        pnlBot.add(btnDong);
        pnl.add(pnlBot, BorderLayout.SOUTH);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }

    private void lapPhieuTiepNhanChoKhach() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng muốn lập phiếu tiếp nhận!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sdt = model.getValueAt(dong, 2).toString();
        Main_UI mainUI = Main_UI.getInstance();
        if (mainUI != null) {
            mainUI.chuyenTrang("Lập phiếu tiếp nhận");
            // Auto-fill sau khi chuyển trang
            SwingUtilities.invokeLater(() -> {
                Container cp = mainUI.getContentPane();
                timVaDienSdt(cp, sdt);
            });
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
}
