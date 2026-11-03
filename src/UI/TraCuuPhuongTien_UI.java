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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Màn hình tra cứu thông tin phương tiện và chủ sở hữu.
 * - Tìm theo: Lựa chọn tiêu chí tìm kiếm kết hợp nhập từ khóa vào ô tìm kiếm.
 * - Lọc nhanh: Tra cứu trực tiếp ra ngay kết quả khi bấm vào (Tất cả, Đang sửa, Đã tiếp nhận, Chưa tiếp nhận, Toyota, Honda, Hyundai...), không liên kết hay phụ thuộc vào Tìm theo.
 */
public class TraCuuPhuongTien_UI extends JPanel {
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboTieuChi = new JComboBox<>(new String[]{
            "Tất cả tiêu chí",
            "Biển số xe",
            "Hãng xe",
            "Dòng xe / Model",
            "Chủ sở hữu (Tên / SĐT / Mã KH)"
    });
    private final JLabel lblGoiY = new JLabel();
    private final JLabel lblDemKetQua = new JLabel("Đang tải dữ liệu...");
    private String boLocNhanh = "TAT_CA";

    private final Map<JButton, String> mapChips = new LinkedHashMap<>();

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

        // KHUNG TÌM KIẾM CHI TIẾT & BỘ LỌC NHANH
        JPanel searchBox = taoKhungTimKiem();
        bangPanel.add(searchBox, BorderLayout.NORTH);

        // BẢNG DANH SÁCH PHƯƠNG TIỆN
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Định dạng căn lề các cột giữa tiêu đề và dữ liệu
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
        BaoVeQuyen.ganQuyen(btnTiepNhan, Entity.QuyenTruyCap.Quyen.TIEP_NHAN);

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

        // Hàng 1: Tiêu chí tìm kiếm (Tìm theo) + Ô nhập từ khóa + Nút tìm kiếm & làm mới
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
            boLocNhanh = "TAT_CA";
            capNhatGiaoDienChip();
            napBang();
        });
        pnlNut.add(btnTim);
        pnlNut.add(btnLamMoi);
        row1.add(pnlNut, BorderLayout.EAST);
        p.add(row1);

        p.add(Box.createVerticalStrut(8));

        // Hàng 2: Bộ lọc nhanh - TRA CỨU TRỰC TIẾP RA NGAY, không liên kết với "Tìm theo"
        JPanel row2 = new JPanel(new BorderLayout(10, 0));
        row2.setOpaque(false);

        JPanel pnlChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlChips.setOpaque(false);
        JLabel lblLocNhanh = new JLabel("Lọc nhanh:");
        lblLocNhanh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLocNhanh.setForeground(new Color(100, 116, 139));
        pnlChips.add(lblLocNhanh);

        mapChips.clear();
        themChip(pnlChips, "Tất cả", "TAT_CA");
        themChip(pnlChips, "Đang sửa tại gara", "DANG_SUA");
        themChip(pnlChips, "Đã tiếp nhận", "DA_TIEP_NHAN");
        themChip(pnlChips, "Chưa tiếp nhận", "CHUA_TIEP_NHAN");

        row2.add(pnlChips, BorderLayout.WEST);
        p.add(row2);
        p.add(Box.createVerticalStrut(8));

        // Hàng 3: Hộp gợi ý & giải thích trực quan giúp người dùng dễ sử dụng
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

    private void themChip(JPanel panel, String text, String kieu) {
        JButton btn = taoChipButton(text, kieu);
        mapChips.put(btn, kieu);
        panel.add(btn);
    }

    private JButton taoChipButton(String text, String kieu) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean active = kieu.equalsIgnoreCase(boLocNhanh);
                boolean hover = getModel().isRollover();

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
                setForeground(Color.BLACK);
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.BLACK);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setRolloverEnabled(true);

        // Bấm nút lọc nhanh sẽ tra cứu trực tiếp ra kết quả ngay, KHÔNG liên kết hay thay đổi "Tìm theo"
        btn.addActionListener(e -> {
            this.boLocNhanh = kieu;
            capNhatGiaoDienChip();
            napBang();
        });
        return btn;
    }

    private void capNhatGiaoDienChip() {
        for (Map.Entry<JButton, String> entry : mapChips.entrySet()) {
            JButton chip = entry.getKey();
            String kieu = entry.getValue();
            if (chip == null) continue;
            boolean active = kieu.equalsIgnoreCase(boLocNhanh);
            chip.setForeground(Color.BLACK);
            chip.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
            chip.repaint();
        }
    }

    private void capNhatPlaceholderGoiY() {
        int idx = cboTieuChi.getSelectedIndex();
        switch (idx) {
            case 1 -> {
                txtTuKhoa.setToolTipText("Nhập biển số xe (VD: 51A-12345, 59A...)");
                lblGoiY.setText("Tìm theo Biển số xe: Nhập biển số (VD: 51A-12345, 59A-67890...) vào ô tìm kiếm.");
            }
            case 2 -> {
                txtTuKhoa.setToolTipText("Nhập tên hãng xe (VD: Toyota, Honda, Hyundai, Mazda, Kia, Ford...)");
                lblGoiY.setText("Tìm theo Hãng xe: Nhập tên hãng xe cần tìm (hoặc bấm nút ở phần Lọc nhanh để xem ngay).");
            }
            case 3 -> {
                txtTuKhoa.setToolTipText("Nhập tên dòng xe hoặc model (VD: Vios, Camry, Civic, Accent...)");
                lblGoiY.setText("Tìm theo Dòng xe / Model: Nhập mẫu xe cụ thể (VD: Vios, Camry, Civic, Accent, Morning...).");
            }
            case 4 -> {
                txtTuKhoa.setToolTipText("Nhập tên chủ xe, số điện thoại hoặc mã khách hàng (VD: Minh Quân, 0912..., KH001)");
                lblGoiY.setText("Tìm theo Chủ sở hữu: Nhập họ tên, số điện thoại hoặc mã khách hàng để tìm các xe tương ứng.");
            }
            default -> {
                txtTuKhoa.setToolTipText("Nhập Biển số xe, Hãng xe, Dòng xe, Tên chủ, SĐT hoặc Số khung...");
                lblGoiY.setText("Mẹo: Bấm các nút 'Lọc nhanh' để xem kết quả ngay, hoặc chọn 'Tìm theo' và nhập thông tin để tìm kiếm.");
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
                    xe.getNgayTiepNhanGanNhatHienThi()
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
                + "<tr><td><b>Tiếp nhận gần nhất:</b></td><td>" + xe.getNgayTiepNhanGanNhatHienThi() + "</td></tr>"
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

    /** Xem lịch sử bảo dưỡng và sửa chữa của xe */
    private void xemLichSuSuaChua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phương tiện trong bảng để xem lịch sử sửa chữa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bienSo = model.getValueAt(dong, 0).toString();
        String hang = model.getValueAt(dong, 1).toString();
        String dongXe = model.getValueAt(dong, 2).toString();
        String chuXe = model.getValueAt(dong, 6).toString();

        ArrayList<TraCuuKhachHang_DAO.LichSuSuaChuaDTO> dsLichSu = traCuuDAO.layLichSuSuaChuaCuaXe(bienSo);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lịch sử sửa chữa — Xe " + bienSo, true);
        dlg.setSize(880, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel pnlHead = new JPanel(new GridLayout(2, 2, 10, 4));
        pnlHead.setBackground(new Color(241, 245, 249));
        pnlHead.setBorder(new EmptyBorder(12, 16, 12, 16));
        pnlHead.add(new JLabel("Biển số: " + bienSo));
        pnlHead.add(new JLabel("Hãng & Dòng xe: " + hang + " " + dongXe));
        pnlHead.add(new JLabel("Chủ sở hữu: " + chuXe));
        pnlHead.add(new JLabel("Tổng số lần vào gara: " + dsLichSu.size() + " lần"));
        dlg.add(pnlHead, BorderLayout.NORTH);

        DefaultTableModel mLS = UiHelper.model("Mã phiếu TN", "Ngày tiếp nhận", "Yêu cầu sửa chữa", "KTV phụ trách", "Trạng thái xe", "Tổng tiền (VNĐ)");
        JTable tblLS = UiHelper.bang(mLS);

        for (TraCuuKhachHang_DAO.LichSuSuaChuaDTO ls : dsLichSu) {
            mLS.addRow(new Object[]{
                    ls.getSoPhieu(),
                    ls.getNgayTiepNhan(),
                    ls.getYeuCau(),
                    ls.getKtvXuLy(),
                    ls.getTienDo(),
                    ls.getChiPhi() > 0 ? String.format("%,d đ", ls.getChiPhi()) : "Chưa lập HĐ"
            });
        }

        dlg.add(new JScrollPane(tblLS), BorderLayout.CENTER);

        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBot.setBorder(new EmptyBorder(8, 12, 8, 12));
        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, e -> dlg.dispose());
        pnlBot.add(btnDong);
        dlg.add(pnlBot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    /** Lập phiếu tiếp nhận trực tiếp cho phương tiện đang chọn */
    private void lapPhieuTiepNhan() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một xe trong danh sách để lập phiếu tiếp nhận!", "Chưa chọn xe", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bienSo = model.getValueAt(dong, 0).toString();
        PhuongTien xe = kho.timXeTheoBienSo(bienSo);

        if (xe == null) {
            // Tạo đối tượng từ DTO nếu chưa có trong bộ nhớ
            TraCuuPhuongTien_DAO.PhuongTienDTO dto = (dong < dsHienTai.size()) ? dsHienTai.get(dong) : null;
            if (dto != null) {
                xe = new PhuongTien(dto.getBienSo(), dto.getHangXe(), dto.getDongXe(), dto.getSoKhung(),
                        dto.getSoMay(), dto.getMauSon(), dto.getNamSX(), dto.getOdo(), dto.getMaChuXe(), dto.getNgayTiepNhanGanNhat());
            }
        }

        if (xe == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu phương tiện để chuyển trang tiếp nhận!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
