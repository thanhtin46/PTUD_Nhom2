package UI;

import DAO.PhieuTiepNhan_DAO;
import Entity.KhoDuLieu;
import Entity.PhieuTiepNhan;

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
 * Màn hình tra cứu danh sách phiếu tiếp nhận xe vào xưởng.
 */
public class TraCuuPhieuTiepNhan_UI extends JPanel {
    private final PhieuTiepNhan_DAO phieuTiepNhanDAO = new PhieuTiepNhan_DAO();
    private final KhoDuLieu kho = KhoDuLieu.get();

    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí (Tự động nhận diện)",
            "Số phiếu tiếp nhận",
            "Biển số xe",
            "Khách hàng (Tên / SĐT / Mã KH)",
            "Yêu cầu dịch vụ"
    });
    private final JLabel lblGoiY = new JLabel();
    private final JLabel lblDemKetQua = new JLabel("Đang tải dữ liệu...");
    private String locTienDo = "TAT_CA";

    private JButton chipTatCa;
    private JButton chipChoTN;
    private JButton chipDangSua;
    private JButton chipChoPT;
    private JButton chipHoanThanh;

    private DefaultTableModel model;
    private JTable table;
    private ArrayList<PhieuTiepNhan> dsHienTai = new ArrayList<>();

    public TraCuuPhieuTiepNhan_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("Tra cứu phiếu tiếp nhận");
        JLabel phu = new JLabel("Tra cứu, theo dõi tiến độ và lịch sử tiếp nhận phương tiện vào xưởng dịch vụ");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(Box.createVerticalStrut(4));
        north.add(phu);
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        // Bảng kết quả tra cứu
        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu tiếp nhận");

        // Khung tìm kiếm chi tiết & lọc tiến độ
        JPanel searchBox = taoKhungTimKiem();
        bangPanel.add(searchBox, BorderLayout.NORTH);

        // Bảng danh sách phiếu tiếp nhận (đã bỏ cột Kỹ thuật viên và Tạm tính)
        model = UiHelper.model(
                "Số phiếu", "Mã KH", "Khách hàng", "Số ĐT", "Biển số xe",
                "Hãng xe", "Dòng xe", "Ngày tiếp nhận", "Yêu cầu dịch vụ", "Tiến độ"
        );
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Định dạng căn lề các cột giữa tiêu đề và dữ liệu
        dinhDangCot(0, SwingConstants.CENTER, 100); // Số phiếu
        dinhDangCot(1, SwingConstants.CENTER, 85);  // Mã KH
        dinhDangCot(2, SwingConstants.LEFT, 150);   // Khách hàng
        dinhDangCot(3, SwingConstants.CENTER, 110); // Số ĐT
        dinhDangCot(4, SwingConstants.CENTER, 110); // Biển số xe
        dinhDangCot(5, SwingConstants.LEFT, 110);   // Hãng xe
        dinhDangCot(6, SwingConstants.LEFT, 120);   // Dòng xe
        dinhDangCot(7, SwingConstants.CENTER, 115); // Ngày tiếp nhận
        dinhDangCot(8, SwingConstants.LEFT, 200);   // Yêu cầu dịch vụ
        dinhDangCotTienDo(9, 130);                  // Tiến độ (có màu trạng thái)

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    xemChiTietPhieu();
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        // Thanh thao tác dưới bảng
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 4, 4, 4));

        lblDemKetQua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDemKetQua.setForeground(new Color(71, 85, 105));
        footer.add(lblDemKetQua, BorderLayout.WEST);

        JPanel boxNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boxNut.setOpaque(false);

        JButton btnXemChiTiet = UiHelper.nut("Xem chi tiết phiếu", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, e -> xemChiTietPhieu());
        JButton btnTaoMoi = UiHelper.nut("Lập phiếu tiếp nhận mới", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, e -> chuyenTrangLapPhieu());

        boxNut.add(btnXemChiTiet);
        boxNut.add(btnTaoMoi);
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
            locTienDo = "TAT_CA";
            capNhatGiaoDienChip();
            napBang();
        });
        pnlNut.add(btnTim);
        pnlNut.add(btnLamMoi);
        row1.add(pnlNut, BorderLayout.EAST);
        p.add(row1);

        p.add(Box.createVerticalStrut(8));

        // Hàng 2: Bộ lọc nhanh theo Tiến độ (giữ lại tiến độ thay cho lọc)
        JPanel row2 = new JPanel(new BorderLayout(10, 0));
        row2.setOpaque(false);

        JPanel pnlChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlChips.setOpaque(false);
        JLabel lblLocNhanh = new JLabel("Tiến độ:");
        lblLocNhanh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLocNhanh.setForeground(new Color(100, 116, 139));
        pnlChips.add(lblLocNhanh);

        chipTatCa = taoChipButton("Tất cả", "TAT_CA");
        chipChoTN = taoChipButton("Chờ tiếp nhận", "Chờ tiếp nhận");
        chipDangSua = taoChipButton("Đang sửa", "Đang sửa");
        chipChoPT = taoChipButton("Chờ phụ tùng", "Chờ phụ tùng");
        chipHoanThanh = taoChipButton("Hoàn thành", "Hoàn thành");

        pnlChips.add(chipTatCa);
        pnlChips.add(chipChoTN);
        pnlChips.add(chipDangSua);
        pnlChips.add(chipChoPT);
        pnlChips.add(chipHoanThanh);
        row2.add(pnlChips, BorderLayout.WEST);

        p.add(row2);
        p.add(Box.createVerticalStrut(8));

        // Hàng 3: Hộp gợi ý & giải thích trực quan
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

        capNhatGiaoDienChip();
        return p;
    }

    private JButton taoChipButton(String text, String kieuTienDo) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = kieuTienDo.equalsIgnoreCase(locTienDo);
                boolean hover = getModel().isRollover();

                // Xác định màu nền: hover vào chỉ thay đổi màu nền, chữ màu đen giữ nguyên
                Color bg;
                Color border;
                if (active) {
                    bg = hover ? new Color(219, 234, 254) : new Color(239, 246, 255);
                    border = new Color(59, 130, 246);
                } else {
                    bg = hover ? new Color(241, 245, 249) : Color.WHITE;
                    border = hover ? new Color(148, 163, 184) : new Color(203, 213, 225);
                }

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

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
            this.locTienDo = kieuTienDo;
            capNhatGiaoDienChip();
            txtTuKhoa.requestFocus();
            napBang();
        });
        return btn;
    }

    private void capNhatGiaoDienChip() {
        JButton[] chips = {chipTatCa, chipChoTN, chipDangSua, chipChoPT, chipHoanThanh};
        String[] kieus = {"TAT_CA", "Chờ tiếp nhận", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"};

        for (int i = 0; i < chips.length; i++) {
            if (chips[i] == null) continue;
            boolean active = kieus[i].equalsIgnoreCase(locTienDo);
            chips[i].setForeground(Color.BLACK); // Chữ màu đen giữ nguyên
            chips[i].setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
            chips[i].repaint();
        }
    }

    private void capNhatPlaceholderGoiY() {
        int idx = cboTieuChi.getSelectedIndex();
        switch (idx) {
            case 1 -> {
                txtTuKhoa.setToolTipText("Nhập mã số phiếu tiếp nhận (VD: TN001, TN002...)");
                lblGoiY.setText("Nhập Số phiếu tiếp nhận (VD: TN001, TN002...) để định danh chính xác phiếu.");
            }
            case 2 -> {
                txtTuKhoa.setToolTipText("Nhập biển số xe (VD: 51A-12345, 59A...)");
                lblGoiY.setText("Nhập Biển số xe (VD: 51A-12345, 59A-67890...) để lọc toàn bộ phiếu của xe này.");
            }
            case 3 -> {
                txtTuKhoa.setToolTipText("Nhập tên khách hàng, số điện thoại hoặc mã KH (VD: Minh Quân, 0912..., KH001)");
                lblGoiY.setText("Nhập Họ tên khách hàng, Số điện thoại hoặc Mã KH để xem lịch sử tiếp nhận.");
            }
            case 4 -> {
                txtTuKhoa.setToolTipText("Nhập yêu cầu sửa chữa hoặc hiện tượng xe (VD: Thay nhớt, Sơn xe, Phanh...)");
                lblGoiY.setText("Nhập Yêu cầu dịch vụ / Mô tả hư hỏng của khách hàng.");
            }
            default -> {
                txtTuKhoa.setToolTipText("Nhập Số phiếu, Biển số xe, Khách hàng, SĐT hoặc Yêu cầu dịch vụ...");
                lblGoiY.setText("Hệ thống tự động quét Số phiếu, Biển số xe, Tên khách hàng, Số điện thoại và Yêu cầu dịch vụ.");
            }
        }
    }

    /** Căn lề cho tiêu đề và nội dung dữ liệu trong bảng */
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

    /** Căn lề và tô màu trạng thái riêng cho cột Tiến độ */
    private void dinhDangCotTienDo(int cot, int doRong) {
        if (cot < 0 || cot >= table.getColumnCount()) return;
        TableColumn tc = table.getColumnModel().getColumn(cot);
        tc.setPreferredWidth(doRong);

        tc.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(241, 245, 249));
                lbl.setForeground(new Color(30, 41, 59));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, UiHelper.VIEN),
                        new EmptyBorder(8, 4, 8, 4)
                ));
                return lbl;
            }
        });

        tc.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));

                if (!isSel) {
                    l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    String td = val != null ? val.toString() : "";
                    if (td.contains("Hoàn thành")) {
                        l.setForeground(new Color(22, 163, 74));
                    } else if (td.contains("Đang sửa")) {
                        l.setForeground(new Color(217, 119, 6));
                    } else if (td.contains("Chờ phụ tùng")) {
                        l.setForeground(new Color(225, 29, 72));   // Rose 600
                    } else {
                        l.setForeground(new Color(71, 85, 105));
                    }
                } else {
                    l.setBackground(new Color(219, 234, 254));
                    l.setForeground(UiHelper.XANH_TRUNG);
                }
                return l;
            }
        });
    }

    private void napBang() {
        String q = txtTuKhoa.getText().trim();
        int tieuChi = cboTieuChi.getSelectedIndex();
        model.setRowCount(0);

        // 1. Tra cứu trực tiếp từ SQL Server qua PhieuTiepNhan_DAO
        dsHienTai = phieuTiepNhanDAO.traCuuPhieuTiepNhan(q, tieuChi, locTienDo);

        for (PhieuTiepNhan p : dsHienTai) {
            model.addRow(new Object[]{
                    p.getSoPhieu(),
                    p.getMaKH() != null ? p.getMaKH() : "—",
                    p.getHoTen(),
                    p.getSdt(),
                    p.getBienSo(),
                    p.getHangXe() != null ? p.getHangXe() : "",
                    p.getLoaiXe() != null ? p.getLoaiXe() : "",
                    p.getNgayTiepNhan(),
                    p.getYeuCau(),
                    p.getTienDo()
            });
        }

        int tongSo = phieuTiepNhanDAO.layTongSoPhieu();
        if (tongSo == 0) tongSo = kho.getPhieuTiepNhans().size();
        lblDemKetQua.setText("Hiển thị: " + dsHienTai.size() + " / " + tongSo + " phiếu tiếp nhận");
    }

    /** Xem chi tiết hồ sơ phiếu tiếp nhận */
    private void xemChiTietPhieu() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu tiếp nhận trong bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PhieuTiepNhan p = (dong < dsHienTai.size()) ? dsHienTai.get(dong) : null;
        if (p == null) {
            String soPhieu = model.getValueAt(dong, 0).toString();
            p = kho.timPhieuTiepNhan(soPhieu);
        }
        if (p == null) return;

        String msg = "<html><div style='width:460px;font-family:Segoe UI;'>"
                + "<h3 style='color:#0F172A;margin-bottom:8px;'>CHI TIẾT PHIẾU TIẾP NHẬN XE</h3>"
                + "<table cellpadding='3' style='font-size:12px;'>"
                + "<tr><td width='140'><b>Số phiếu:</b></td><td><span style='color:#2563EB;font-weight:bold;font-size:13px;'>" + p.getSoPhieu() + "</span></td></tr>"
                + "<tr><td><b>Ngày tiếp nhận:</b></td><td>" + p.getNgayTiepNhan() + "</td></tr>"
                + "<tr><td><b>Trạng thái tiến độ:</b></td><td><b>" + p.getTienDo() + "</b></td></tr>"
                + "<tr><td><b>Nhân viên tiếp nhận:</b></td><td>" + p.getNguoiLap() + "</td></tr>"
                + "</table>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;margin:8px 0;'>"
                + "<h4 style='color:#2563EB;margin-bottom:6px;'>THÔNG TIN PHƯƠNG TIỆN:</h4>"
                + "<table cellpadding='3' style='font-size:12px;'>"
                + "<tr><td width='140'><b>Biển số xe:</b></td><td><span style='color:#0F172A;font-weight:bold;'>" + p.getBienSo() + "</span></td></tr>"
                + "<tr><td><b>Hãng & Dòng xe:</b></td><td>" + p.getHangXe() + " " + p.getLoaiXe() + "</td></tr>"
                + "</table>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;margin:8px 0;'>"
                + "<h4 style='color:#2563EB;margin-bottom:6px;'>THÔNG TIN KHÁCH HÀNG:</h4>"
                + "<table cellpadding='3' style='font-size:12px;'>"
                + "<tr><td width='140'><b>Mã khách hàng:</b></td><td>" + p.getMaKH() + "</td></tr>"
                + "<tr><td><b>Họ tên khách:</b></td><td><b>" + p.getHoTen() + "</b></td></tr>"
                + "<tr><td><b>Số điện thoại:</b></td><td>" + p.getSdt() + "</td></tr>"
                + "</table>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;margin:8px 0;'>"
                + "<h4 style='color:#2563EB;margin-bottom:6px;'>YÊU CẦU & HIỆN TRẠNG SỬA CHỮA:</h4>"
                + "<div style='background:#F8FAFC;padding:8px;border-radius:6px;border:1px solid #E2E8F0;font-size:12px;'>"
                + (p.getYeuCau() != null && !p.getYeuCau().isEmpty() ? p.getYeuCau() : "Không có mô tả chi tiết")
                + "</div>"
                + "</div></html>";

        JOptionPane.showMessageDialog(this, msg, "Hồ sơ phiếu tiếp nhận — " + p.getSoPhieu(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void chuyenTrangLapPhieu() {
        Main_UI mainUI = Main_UI.getInstance();
        if (mainUI == null) {
            mainUI = (Main_UI) SwingUtilities.getAncestorOfClass(Main_UI.class, this);
        }
        if (mainUI != null) {
            mainUI.chuyenTrang("Lập phiếu tiếp nhận");
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy giao diện chính.");
        }
    }
}
