package UI;

import DAO.TraCuuKhachHang_DAO;
import DAO.TraCuuPhuongTien_DAO;
import Entity.KhoDuLieu;
import Entity.PhuongTien;

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
 * TraCuuPhuongTien_UI — Giao diện tra cứu phương tiện đồng bộ với phong cách Tra cứu khách hàng:
 * - Tìm kiếm đa tiêu chí: Tất cả, Biển số xe, Hãng xe, Dòng xe, Chủ sở hữu
 * - Lọc nhanh dạng Chip Button: Tất cả, Biển số, Hãng xe, Chủ xe
 * - Khung hướng dẫn rõ ràng, chi tiết, không gây mơ hồ
 * - Bảng danh sách các cột chia đều, căn thẳng hàng tuyệt đối giữa Tiêu đề và Nội dung
 * - Xem chi tiết hồ sơ xe và chủ xe
 * - Xem lịch sử sửa chữa/bảo dưỡng của xe trực tiếp từ CSDL SQL Server
 * - Lập phiếu tiếp nhận nhanh cho xe đã chọn
 */
public class TraCuuPhuongTien_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí (Tự động nhận diện)",
            "Biển số xe",
            "Hãng xe",
            "Dòng xe / Model",
            "Chủ sở hữu (Tên / SĐT / Mã KH)"
    });
    private final JLabel lblGoiY = new JLabel();
    private final JLabel lblDemKetQua = new JLabel("Đang tải dữ liệu...");
    private String boLocNhanh = "TAT_CA";

    private JButton chipTatCa;
    private JButton chipBienSo;
    private JButton chipHang;
    private JButton chipChuXe;

    private final DefaultTableModel model = UiHelper.model(
            "Biển số", "Hãng xe", "Dòng xe", "Số khung (VIN)", "Màu sơn", "Năm SX", "Chủ sở hữu", "Số điện thoại", "Tiếp nhận gần nhất"
    );

    private final TraCuuPhuongTien_DAO traCuuDAO = new TraCuuPhuongTien_DAO();
    private final KhoDuLieu kho = KhoDuLieu.get();
    private JTable table;
    private ArrayList<TraCuuPhuongTien_DAO.PhuongTienDTO> dsHienTai = new ArrayList<>();

    public TraCuuPhuongTien_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("Tra cứu phương tiện");
        JLabel phu = new JLabel("Tìm kiếm thông tin xe, xem hồ sơ kỹ thuật, chủ sở hữu và lịch sử sửa chữa tại gara");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(Box.createVerticalStrut(4));
        north.add(phu);
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        JPanel bangPanel = UiHelper.panelBang("Kết quả tra cứu phương tiện");

        // KHUNG TÌM KIẾM CHI TIẾT & HƯỚNG DẪN RÕ RÀNG
        JPanel searchBox = taoKhungTimKiem();
        bangPanel.add(searchBox, BorderLayout.NORTH);

        // BẢNG DANH SÁCH PHƯƠNG TIỆN
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Định dạng các cột đều nhau và thẳng hàng tuyệt đối giữa Tiêu đề và Dữ liệu
        dinhDangCot(0, SwingConstants.CENTER, 125); // Biển số
        dinhDangCot(1, SwingConstants.LEFT, 120);   // Hãng xe
        dinhDangCot(2, SwingConstants.LEFT, 130);   // Dòng xe
        dinhDangCot(3, SwingConstants.CENTER, 145); // Số khung (VIN)
        dinhDangCot(4, SwingConstants.CENTER, 100); // Màu sơn
        dinhDangCot(5, SwingConstants.CENTER, 90);  // Năm SX
        dinhDangCot(6, SwingConstants.LEFT, 165);   // Chủ sở hữu
        dinhDangCot(7, SwingConstants.CENTER, 120); // Số điện thoại
        dinhDangCot(8, SwingConstants.CENTER, 145); // Tiếp nhận gần nhất

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

        JButton btnXemChiTiet = UiHelper.nut("Xem chi tiết xe", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> xemChiTiet());
        JButton btnXemLichSu = UiHelper.nut("Xem lịch sử sửa chữa", UiIcon.of(UiIcon.CLOCK, 15), UiHelper.CAM, e -> xemLichSuSuaChua());
        JButton btnTiepNhan = UiHelper.nut("Lập phiếu tiếp nhận cho xe này", UiIcon.of(UiIcon.FILE, 15), UiHelper.XANH_LA, e -> lapPhieuTiepNhan());

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

        // Hàng 1: Tiêu chí tìm kiếm + Ô nhập + Nút tìm kiếm & làm mới
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setOpaque(false);

        JPanel pnlTieuChi = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlTieuChi.setOpaque(false);
        JLabel lblTieuChi = new JLabel("Tìm theo:");
        lblTieuChi.setFont(UiHelper.FONT_NHAN);
        lblTieuChi.setForeground(new Color(51, 65, 85));
        cboTieuChi.setPreferredSize(new Dimension(240, 38));
        cboTieuChi.setFont(UiHelper.FONT_BT);
        cboTieuChi.setBackground(Color.WHITE);
        cboTieuChi.addActionListener(e -> {
            capNhatTrangThaiChipTheoTieuChi();
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
            capNhatGiaoDienChip();
            napBang();
        });
        pnlNut.add(btnTim);
        pnlNut.add(btnLamMoi);
        row1.add(pnlNut, BorderLayout.EAST);
        p.add(row1);

        p.add(Box.createVerticalStrut(8));

        // Hàng 2: Bộ lọc nhanh (Tất cả, Biển số, Hãng xe, Chủ xe)
        JPanel row2 = new JPanel(new BorderLayout(10, 0));
        row2.setOpaque(false);

        JPanel pnlChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlChips.setOpaque(false);
        JLabel lblLocNhanh = new JLabel("Lọc nhanh:");
        lblLocNhanh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLocNhanh.setForeground(new Color(100, 116, 139));
        pnlChips.add(lblLocNhanh);

        chipTatCa = taoChipButton("Tất cả", "TAT_CA", 0);
        chipBienSo = taoChipButton("Biển số", "BIEN_SO", 1);
        chipHang = taoChipButton("Hãng xe", "HANG_XE", 2);
        chipChuXe = taoChipButton("Chủ xe", "CHU_XE", 4);

        pnlChips.add(chipTatCa);
        pnlChips.add(chipBienSo);
        pnlChips.add(chipHang);
        pnlChips.add(chipChuXe);
        row2.add(pnlChips, BorderLayout.WEST);

        p.add(row2);
        p.add(Box.createVerticalStrut(8));

        // Hàng 3: Hộp gợi ý & giải thích trực quan giúp người dùng dễ sử dụng
        JPanel boxHint = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255)); // Blue 50
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(191, 219, 254)); // Blue 200
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boxHint.setOpaque(false);
        boxHint.setBorder(new EmptyBorder(4, 10, 4, 10));

        lblGoiY.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGoiY.setForeground(new Color(30, 64, 175)); // Blue 800
        boxHint.add(lblGoiY);
        p.add(boxHint);

        capNhatGiaoDienChip();
        return p;
    }

    private JButton taoChipButton(String text, String kieu, int mucTieuChi) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = kieu.equalsIgnoreCase(boLocNhanh);
                boolean hover = getModel().isRollover();

                // Xác định màu nền: khi hover vào chỉ thay đổi màu nền, chữ màu đen giữ nguyên
                Color bg;
                Color border;
                if (active) {
                    bg = hover ? new Color(219, 234, 254) : new Color(239, 246, 255);
                    border = new Color(59, 130, 246);
                } else {
                    bg = hover ? new Color(241, 245, 249) : Color.WHITE;
                    border = hover ? new Color(148, 163, 184) : new Color(203, 213, 225);
                }

                // Vẽ nền bo góc
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Vẽ viền
                g2.setColor(border);
                g2.setStroke(new BasicStroke(active ? 1.5f : 1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.dispose();

                // Luôn giữ chữ màu đen, không làm mờ chữ khi hover
                setForeground(Color.BLACK);
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.BLACK); // Mặc định chữ màu đen
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setRolloverEnabled(true);

        btn.addActionListener(e -> {
            this.boLocNhanh = kieu;
            cboTieuChi.setSelectedIndex(mucTieuChi);
            capNhatGiaoDienChip();
            txtTuKhoa.requestFocus();
            napBang();
        });
        return btn;
    }

    private void capNhatGiaoDienChip() {
        JButton[] chips = {chipTatCa, chipBienSo, chipHang, chipChuXe};
        String[] kieus = {"TAT_CA", "BIEN_SO", "HANG_XE", "CHU_XE"};

        for (int i = 0; i < chips.length; i++) {
            if (chips[i] == null) continue;
            boolean active = kieus[i].equalsIgnoreCase(boLocNhanh);
            chips[i].setForeground(Color.BLACK); // Chữ màu đen giữ nguyên
            chips[i].setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
            chips[i].repaint();
        }
    }

    private void capNhatTrangThaiChipTheoTieuChi() {
        int idx = cboTieuChi.getSelectedIndex();
        switch (idx) {
            case 1 -> boLocNhanh = "BIEN_SO";
            case 2 -> boLocNhanh = "HANG_XE";
            case 4 -> boLocNhanh = "CHU_XE";
            default -> boLocNhanh = "TAT_CA";
        }
        capNhatGiaoDienChip();
    }

    private void capNhatPlaceholderGoiY() {
        int idx = cboTieuChi.getSelectedIndex();
        switch (idx) {
            case 1 -> {
                txtTuKhoa.setToolTipText("Nhập biển số xe (VD: 51A-12345, 59A...)");
                lblGoiY.setText("Nhập Biển số xe (VD: 51A-12345, 59A-67890...) để tìm kiếm xe chính xác.");
            }
            case 2 -> {
                txtTuKhoa.setToolTipText("Nhập tên hãng xe (VD: Toyota, Honda, Hyundai, Mazda, Kia, Ford...)");
                lblGoiY.setText("Nhập Hãng xe (VD: Toyota, Honda, Hyundai, Mazda, Kia, Ford...) để lọc toàn bộ dòng xe thuộc hãng.");
            }
            case 3 -> {
                txtTuKhoa.setToolTipText("Nhập tên dòng xe hoặc model (VD: Vios, Camry, Civic, CR-V, CX-5, Accent...)");
                lblGoiY.setText("Nhập Dòng xe / Model (VD: Vios, Camry, Civic, CX-5, Accent, Morning...) để tìm theo mẫu xe cụ thể.");
            }
            case 4 -> {
                txtTuKhoa.setToolTipText("Nhập tên chủ xe, số điện thoại hoặc mã khách hàng (VD: Minh Quân, 0912..., KH001)");
                lblGoiY.setText("Nhập Họ tên chủ sở hữu, Số điện thoại hoặc Mã khách hàng (VD: Nguyễn Minh Quân, 0912..., KH001).");
            }
            default -> {
                txtTuKhoa.setToolTipText("Nhập Biển số xe, Hãng xe, Dòng xe, Tên chủ, SĐT hoặc Số khung...");
                lblGoiY.setText("Hệ thống tự động quét Biển số xe, Hãng xe, Dòng xe, Tên chủ xe, Số điện thoại và Số khung VIN.");
            }
        }
    }

    /** Căn lề đồng bộ cho cả tiêu đề cột và nội dung dữ liệu để luôn thẳng hàng tuyệt đối */
    private void dinhDangCot(int cot, int canLe, int doRong) {
        if (cot < 0 || cot >= table.getColumnCount()) return;
        TableColumn tc = table.getColumnModel().getColumn(cot);
        tc.setPreferredWidth(doRong);

        final int padL = (canLe == SwingConstants.LEFT) ? 12 : 4;
        final int padR = (canLe == SwingConstants.RIGHT) ? 12 : 4;

        // Tiêu đề cột (Header)
        tc.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(canLe);
                lbl.setBackground(new Color(241, 245, 249));
                lbl.setForeground(new Color(30, 41, 59));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(8, padL, 8, padR)
                ));
                return lbl;
            }
        });

        // Nội dung dữ liệu (Cell)
        tc.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!t.isRowSelected(r)) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(new Color(30, 41, 59));
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(UiHelper.XANH_TRUNG);
                }
                if (comp instanceof JLabel lbl) {
                    lbl.setHorizontalAlignment(canLe);
                    lbl.setBorder(new EmptyBorder(0, padL, 0, padR));
                }
                return comp;
            }
        });
    }

    private void napBang() {
        String q = txtTuKhoa.getText().trim();
        int tieuChi = cboTieuChi.getSelectedIndex();
        model.setRowCount(0);

        // 1. Tra cứu trực tiếp từ CSDL SQL Server qua TraCuuPhuongTien_DAO
        dsHienTai = traCuuDAO.traCuuPhuongTien(q, tieuChi, boLocNhanh);

        for (TraCuuPhuongTien_DAO.PhuongTienDTO xe : dsHienTai) {
            model.addRow(new Object[]{
                    xe.getBienSo(),
                    xe.getHangXe(),
                    xe.getDongXe(),
                    xe.getSoKhung(),
                    xe.getMauSon(),
                    xe.getNamSX(),
                    xe.getTenChuXe(),
                    xe.getSdtChuXe(),
                    xe.getNgayTiepNhanGanNhat()
            });
        }

        int tongSo = traCuuDAO.layTongSoPhuongTien();
        if (tongSo == 0) tongSo = kho.getPhuongTiens().size();
        lblDemKetQua.setText("Tìm thấy: " + dsHienTai.size() + " / " + tongSo + " phương tiện");
    }

    /** Xem chi tiết hồ sơ xe và chủ sở hữu */
    private void xemChiTiet() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phương tiện trong bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TraCuuPhuongTien_DAO.PhuongTienDTO xe = (dong < dsHienTai.size()) ? dsHienTai.get(dong) : null;
        if (xe == null) {
            String bienSo = model.getValueAt(dong, 0).toString();
            PhuongTien pt = kho.timXeTheoBienSo(bienSo);
            if (pt == null) return;
            var chu = kho.timKhachHangTheoMa(pt.getMaChuXe());
            String tenChu = chu != null ? chu.getHoTen() : "";
            String sdtChu = chu != null ? chu.getSdt() : "";
            String diaChiChu = chu != null ? chu.getDiaChi() : "";
            String emailChu = chu != null ? chu.getEmail() : "";
            xe = new TraCuuPhuongTien_DAO.PhuongTienDTO(pt.getBienSo(), pt.getHangXe(), pt.getDongXe(),
                    pt.getSoKhung(), pt.getSoMay(), pt.getMauSon(), pt.getNamSX(), pt.getOdo(),
                    pt.getMaChuXe(), pt.getNgayTiepNhanGanNhat(), tenChu, sdtChu, diaChiChu, emailChu);
        }

        String msg = "<html><div style='width:460px;font-family:Segoe UI;'>"
                + "<h3 style='color:#0F172A;margin-bottom:8px;'>HỒ SƠ PHƯƠNG TIỆN CHI TIẾT</h3>"
                + "<table cellpadding='3' style='font-size:12px;'>"
                + "<tr><td width='130'><b>Biển số xe:</b></td><td><span style='color:#2563EB;font-weight:bold;font-size:13px;'>" + xe.getBienSo() + "</span></td></tr>"
                + "<tr><td><b>Hãng xe:</b></td><td><b>" + xe.getHangXe() + "</b></td></tr>"
                + "<tr><td><b>Dòng xe / Model:</b></td><td><b>" + xe.getDongXe() + "</b></td></tr>"
                + "<tr><td><b>Số khung (VIN):</b></td><td>" + xe.getSoKhung() + "</td></tr>"
                + "<tr><td><b>Màu sơn:</b></td><td>" + xe.getMauSon() + "</td></tr>"
                + "<tr><td><b>Năm sản xuất:</b></td><td>" + xe.getNamSX() + "</td></tr>"
                + "<tr><td><b>Số ODO hiện tại:</b></td><td>" + xe.getOdo() + " km</td></tr>"
                + "<tr><td><b>Tiếp nhận gần nhất:</b></td><td>" + xe.getNgayTiepNhanGanNhat() + "</td></tr>"
                + "</table>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;margin:10px 0;'>"
                + "<h4 style='color:#2563EB;margin-bottom:6px;'>THÔNG TIN CHỦ SỞ HỮU:</h4>"
                + "<table cellpadding='3' style='font-size:12px;'>"
                + "<tr><td width='130'><b>Mã chủ xe:</b></td><td>" + xe.getMaChuXe() + "</td></tr>"
                + "<tr><td><b>Họ và tên chủ:</b></td><td><b>" + xe.getTenChuXe() + "</b></td></tr>"
                + "<tr><td><b>Số điện thoại:</b></td><td>" + xe.getSdtChuXe() + "</td></tr>"
                + "<tr><td><b>Địa chỉ:</b></td><td>" + xe.getDiaChiChuXe() + "</td></tr>"
                + "<tr><td><b>Email:</b></td><td>" + xe.getEmailChuXe() + "</td></tr>"
                + "</table>"
                + "</div></html>";

        JOptionPane.showMessageDialog(this, msg, "Hồ sơ phương tiện — " + xe.getBienSo(), JOptionPane.INFORMATION_MESSAGE);
    }

    /** Xem lịch sử sửa chữa của chiếc xe */
    private void xemLichSuSuaChua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phương tiện trong bảng để xem lịch sử sửa chữa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String bienSo = model.getValueAt(dong, 0).toString().trim();

        ArrayList<TraCuuKhachHang_DAO.LichSuSuaChuaDTO> dsLs = traCuuDAO.layLichSuSuaChuaCuaXe(bienSo);

        if (dsLs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Xe mang biển số [" + bienSo + "] chưa có lịch sử tiếp nhận / sửa chữa nào trong hệ thống!",
                    "Lịch sử sửa chữa", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lịch sử sửa chữa xe — " + bienSo, true);
        dlg.setSize(920, 500);
        dlg.setLocationRelativeTo(this);

        JPanel pnl = new JPanel(new BorderLayout(12, 12));
        pnl.setBackground(UiHelper.NEN);
        pnl.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTieuDe = new JLabel("Lịch sử sửa chữa của xe: " + bienSo + " (" + dsLs.size() + " lần tiếp nhận)");
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
        pnlBot.add(btnDong);
        pnl.add(pnlBot, BorderLayout.SOUTH);

        dlg.setContentPane(pnl);
        dlg.setVisible(true);
    }

    /** Lập phiếu tiếp nhận cho phương tiện đã chọn */
    private void lapPhieuTiepNhan() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một xe trong bảng kết quả tra cứu để lập phiếu tiếp nhận!", "Chưa chọn xe", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String bienSo = model.getValueAt(dong, 0).toString().trim();
        PhuongTien xe = kho.timXeTheoBienSo(bienSo);
        if (xe == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin xe với biển số: " + bienSo, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Main_UI mainUI = Main_UI.getInstance();
        if (mainUI == null) {
            mainUI = (Main_UI) SwingUtilities.getAncestorOfClass(Main_UI.class, this);
        }
        if (mainUI != null) {
            mainUI.chuyenTrangTiepNhan(xe);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy giao diện chính để chuyển trang.");
        }
    }
}
