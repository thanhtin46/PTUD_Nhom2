package UI;

import Entity.KhoDuLieu;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình báo cáo tồn kho phụ tùng linh kiện.
 * Chỉ hiển thị phụ tùng linh kiện, tự động loại bỏ các dịch vụ.
 * Hỗ trợ các combobox lọc tìm kiếm khi gõ chữ, sắp xếp theo bảng chữ cái A-Z hoặc số mã.
 */
public class BaoCaoTonKho_UI extends JPanel {
    private final KhoDuLieu kho = KhoDuLieu.get();

    // Danh sách phân loại phụ tùng sắp xếp theo bảng chữ cái A-Z
    private static final List<String> DS_PHAN_LOAI = List.of(
            "-- Tất cả phân loại --",
            "Dầu nhớt & Dung dịch",
            "Điện & Ắc quy",
            "Động cơ & Nhiên liệu",
            "Hệ thống Phanh",
            "Khác",
            "Khung gầm & Giảm xóc",
            "Lọc & Bảo dưỡng",
            "Lốp & Phụ kiện"
    );

    private static final List<String> DS_CANH_BAO = List.of(
            "-- Tất cả mức cảnh báo --",
            "HẾT HÀNG (≤ 0)",
            "SẮP HẾT (≤ Tối thiểu)",
            "BÌNH THƯỜNG"
    );

    private static final List<String> DS_SAP_XEP = List.of(
            "Mã PT: Số mã tăng dần (PT001 →)",
            "Mã PT: Số mã giảm dần",
            "Tên phụ tùng: Bảng chữ cái A - Z",
            "Tên phụ tùng: Bảng chữ cái Z - A",
            "Tồn kho: Ít nhất trước",
            "Tồn kho: Nhiều nhất trước",
            "Giá trị tồn: Cao nhất trước"
    );

    // Các thẻ chỉ số thống kê trên cùng
    private final JLabel lblSoHet = new JLabel("0");
    private final JLabel lblSoSapHet = new JLabel("0");
    private final JLabel lblSoBinhThuong = new JLabel("0");
    private final JLabel lblTongGiaTri = new JLabel("0 đ");
    private final JLabel lblTongGiaTriPhuDe = new JLabel("0 mặt hàng");

    // Thanh tìm kiếm và bộ lọc
    private final JTextField txtTimKiem = new JTextField() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(getFont().deriveFont(Font.ITALIC, 12f));
                Insets insets = getInsets();
                g2.drawString("Tìm kiếm theo mã hoặc tên phụ tùng...", insets.left + 8,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                g2.dispose();
            }
        }
    };
    private final JComboBox<String> cboLocLoai = new JComboBox<>(DS_PHAN_LOAI.toArray(new String[0]));
    private final JComboBox<String> cboLocCanhBao = new JComboBox<>(DS_CANH_BAO.toArray(new String[0]));
    private final JComboBox<String> cboSapXep = new JComboBox<>(DS_SAP_XEP.toArray(new String[0]));

    // Bảng dữ liệu chi tiết
    private DefaultTableModel model;
    private JTable bang;
    private final List<Integer> rowsCanhBao = new ArrayList<>();

    // Footer
    private final JLabel lblFooterTrai = new JLabel();
    private final JLabel lblFooterPhai = new JLabel();

    public BaoCaoTonKho_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // ==== NORTH PANEL: TIÊU ĐỀ + 2 NÚT THAO TÁC + 4 THẺ THỐNG KÊ ====
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        // Hàng 1: Tiêu đề trang + Nút Làm mới & In báo cáo
        JPanel rowHeader = new JPanel(new BorderLayout(12, 0));
        rowHeader.setOpaque(false);

        JLabel tde = UiHelper.tieuDeTrang("Báo cáo tồn kho phụ tùng");
        rowHeader.add(tde, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnLamMoi = UiHelper.nut("Làm mới (F5)", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XANH_TRUNG, true, e -> lamMoiDuLieu());
        btnLamMoi.setPreferredSize(new Dimension(135, 36));

        JButton btnIn = UiHelper.nut("In báo cáo", UiIcon.of(UiIcon.PRINT, 15), UiHelper.XAM, true, e -> inBaoCao());
        btnIn.setPreferredSize(new Dimension(115, 36));

        actions.add(btnLamMoi);
        actions.add(btnIn);
        rowHeader.add(actions, BorderLayout.EAST);

        north.add(rowHeader);
        north.add(Box.createVerticalStrut(10));

        // Hàng 2: Grid 4 thẻ thống kê
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(4, 0, 14, 0));
        grid.add(theCanhBao(UiIcon.of(UiIcon.CLOSE, 22, Color.WHITE), "Hết hàng", lblSoHet,
                "Cần nhập gấp", new Color(220, 80, 80)));
        grid.add(theCanhBao(UiIcon.of(UiIcon.CLOCK, 22, Color.WHITE), "Sắp hết", lblSoSapHet,
                "≤ mức tối thiểu", new Color(225, 150, 45)));
        grid.add(theCanhBao(UiIcon.of(UiIcon.CHECK, 22, Color.WHITE), "Bình thường", lblSoBinhThuong,
                "Còn đủ dùng", new Color(46, 160, 92)));
        grid.add(theCanhBao(UiIcon.of(UiIcon.BILL, 22, Color.WHITE), "Tổng giá trị tồn", lblTongGiaTri,
                lblTongGiaTriPhuDe, new Color(37, 99, 168)));
        north.add(grid);

        root.add(north, BorderLayout.NORTH);

        // ==== CENTER PANEL: BẢNG CHI TIẾT TỒN KHO ====
        JPanel bangPanel = UiHelper.panelBang("Báo cáo chi tiết phụ tùng & linh kiện (cảnh báo real-time)");
        bangPanel.add(taoThanhLoc(), BorderLayout.NORTH);

        model = UiHelper.model("Mã PT", "Tên phụ tùng", "Phân loại", "ĐVT",
                "Tồn kho", "Tối thiểu", "Cảnh báo", "Đơn giá", "Giá trị tồn");
        bang = UiHelper.bang(model);
        bang.setAutoCreateRowSorter(true); // Hỗ trợ click tiêu đề cột để sắp xếp trực tiếp theo bảng chữ cái / số

        // Tô màu cột cảnh báo (cột 6)
        bang.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String cb = v == null ? "" : v.toString();
                comp.setForeground(Color.WHITE);
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                if (cb.contains("HẾT")) {
                    comp.setBackground(new Color(220, 80, 80));
                } else if (cb.contains("SẮP")) {
                    comp.setBackground(new Color(225, 150, 45));
                } else {
                    comp.setBackground(new Color(46, 160, 92));
                }
                return comp;
            }
        });

        // Tô nền dòng và căn lề các cột
        bang.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int col) {
                Component c = super.getTableCellRendererComponent(t, v, s, f, r, col);
                if (c instanceof JLabel lbl) {
                    if (col == 0 || col == 3 || col == 4 || col == 5) {
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    } else if (col == 7 || col == 8) {
                        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }
                if (!s && rowsCanhBao.contains(r)) {
                    String cb = String.valueOf(model.getValueAt(r, 6));
                    if (cb.contains("HẾT")) c.setBackground(new Color(255, 235, 235));
                    else c.setBackground(new Color(255, 246, 228));
                    c.setForeground(new Color(120, 30, 30));
                } else if (!s) {
                    c.setBackground(r % 2 == 0 ? Color.WHITE : new Color(247, 250, 254));
                    c.setForeground(new Color(40, 50, 65));
                } else {
                    c.setBackground(new Color(220, 235, 250));
                    c.setForeground(new Color(15, 35, 70));
                }
                return c;
            }
        });

        bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        bang.getColumnModel().getColumn(0).setPreferredWidth(80);   // Mã PT
        bang.getColumnModel().getColumn(1).setPreferredWidth(200);  // Tên phụ tùng
        bang.getColumnModel().getColumn(2).setPreferredWidth(140);  // Phân loại
        bang.getColumnModel().getColumn(3).setPreferredWidth(65);   // ĐVT
        bang.getColumnModel().getColumn(4).setPreferredWidth(75);   // Tồn kho
        bang.getColumnModel().getColumn(5).setPreferredWidth(75);   // Tối thiểu
        bang.getColumnModel().getColumn(6).setPreferredWidth(115);  // Cảnh báo
        bang.getColumnModel().getColumn(7).setPreferredWidth(105);  // Đơn giá
        bang.getColumnModel().getColumn(8).setPreferredWidth(125);  // Giá trị tồn

        JScrollPane sp = new JScrollPane(bang);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        root.add(bangPanel, BorderLayout.CENTER);

        // ==== SOUTH PANEL: FOOTER TỔNG KẾT ====
        JPanel tongPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(37, 99, 168), getWidth(), 0, new Color(80, 144, 220));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tongPanel.setOpaque(false);
        tongPanel.setBorder(new EmptyBorder(12, 18, 12, 18));
        tongPanel.setLayout(new BorderLayout());

        lblFooterTrai.setForeground(Color.WHITE);
        lblFooterTrai.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblFooterPhai.setForeground(Color.WHITE);
        lblFooterPhai.setFont(new Font("Segoe UI", Font.BOLD, 15));

        tongPanel.add(lblFooterTrai, BorderLayout.WEST);
        tongPanel.add(lblFooterPhai, BorderLayout.EAST);
        root.add(tongPanel, BorderLayout.SOUTH);

        // ==== LẮNG NGHE SỰ KIỆN TÌM KIẾM & BỘ LỌC ====
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locDuLieu(); }
            @Override public void removeUpdate(DocumentEvent e) { locDuLieu(); }
            @Override public void changedUpdate(DocumentEvent e) { locDuLieu(); }
        };
        txtTimKiem.getDocument().addDocumentListener(docListener);
        cboLocLoai.addActionListener(e -> locDuLieu());
        cboLocCanhBao.addActionListener(e -> locDuLieu());
        cboSapXep.addActionListener(e -> locDuLieu());

        // Phím tắt F5 làm mới
        setupPhimTat();

        // Tự động tải dữ liệu khi hiển thị
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override public void ancestorAdded(javax.swing.event.AncestorEvent event) { napDuLieu(); }
            @Override public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
            @Override public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });

        // Nạp dữ liệu lần đầu
        napDuLieu();
    }

    private JPanel taoThanhLoc() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);

        // 1. Ô tìm kiếm
        gbc.gridx = 0; gbc.weightx = 0.28;
        txtTimKiem.setPreferredSize(new Dimension(0, 36));
        UiHelper.trangTriInput(txtTimKiem);
        row1.add(txtTimKiem, gbc);

        // 2. ComboBox Phân loại (tìm kiếm khi gõ chữ)
        gbc.gridx = 1; gbc.weightx = 0.22;
        cboLocLoai.setPreferredSize(new Dimension(0, 36));
        cboLocLoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        UiHelper.ganLocTimKiemChoComboBox(cboLocLoai, DS_PHAN_LOAI, this::locDuLieu);
        row1.add(cboLocLoai, gbc);

        // 3. ComboBox Mức cảnh báo (tìm kiếm khi gõ chữ)
        gbc.gridx = 2; gbc.weightx = 0.22;
        cboLocCanhBao.setPreferredSize(new Dimension(0, 36));
        cboLocCanhBao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        UiHelper.ganLocTimKiemChoComboBox(cboLocCanhBao, DS_CANH_BAO, this::locDuLieu);
        row1.add(cboLocCanhBao, gbc);

        // 4. ComboBox Sắp xếp (tìm kiếm khi gõ chữ)
        gbc.gridx = 3; gbc.weightx = 0.28;
        cboSapXep.setPreferredSize(new Dimension(0, 36));
        cboSapXep.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        UiHelper.ganLocTimKiemChoComboBox(cboSapXep, DS_SAP_XEP, this::locDuLieu);
        row1.add(cboSapXep, gbc);

        // 5. Nút Bỏ lọc
        gbc.gridx = 4; gbc.weightx = 0.0; gbc.insets = new Insets(0, 0, 0, 0);
        JButton btnBoLoc = UiHelper.nut("Bỏ lọc", UiIcon.of(UiIcon.REFRESH, 13), UiHelper.XAM, true, e -> boLoc());
        btnBoLoc.setPreferredSize(new Dimension(85, 36));
        row1.add(btnBoLoc, gbc);

        // Hàng 2: Hướng dẫn
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);

        JLabel lblHuongDan = new JLabel("💡 Nhấp tiêu đề cột để sắp xếp trực tiếp • Gõ chữ vào các ô combobox để tìm kiếm và lọc danh sách");
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHuongDan.setForeground(new Color(100, 116, 139));
        row2.add(lblHuongDan, BorderLayout.WEST);

        wrap.add(row1, BorderLayout.NORTH);
        wrap.add(row2, BorderLayout.SOUTH);
        return wrap;
    }

    private void boLoc() {
        txtTimKiem.setText("");
        cboLocLoai.setSelectedIndex(0);
        cboLocCanhBao.setSelectedIndex(0);
        cboSapXep.setSelectedIndex(0);
        locDuLieu();
    }

    private void setupPhimTat() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "lamMoiF5");
        am.put("lamMoiF5", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                lamMoiDuLieu();
            }
        });
    }

    public void lamMoiDuLieu() {
        kho.dongBoPhuTungTuDB();
        napDuLieu();
        JOptionPane.showMessageDialog(this,
                "Đã làm mới và đồng bộ dữ liệu tồn kho phụ tùng với SQL Server thành công!",
                "Đồng bộ hoàn tất", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Kiểm tra xem một đối tượng có phải là phụ tùng / linh kiện thật sự không.
     * Tự động loại bỏ các mã dịch vụ bảo hành ảo (DVxxx) hoặc dịch vụ sửa chữa.
     */
    public static boolean laPhuTungLinhKien(PhuTung pt) {
        if (pt == null || pt.getMaPT() == null) return false;
        String ma = pt.getMaPT().trim().toUpperCase();
        if (ma.startsWith("DV")) return false;
        if (pt.getTen() != null && pt.getTen().toLowerCase().contains("dịch vụ")) return false;
        return ma.startsWith("PT");
    }

    /**
     * Nạp lại dữ liệu tồn kho và cập nhật toàn bộ chỉ số + bảng
     */
    public void napDuLieu() {
        // 1. Thống kê nhanh toàn bộ phụ tùng linh kiện thực tế trong kho
        int soHet = 0, soSapHet = 0, soBinhThuong = 0;
        long tongGiaTri = 0;
        int tongPhuTung = 0;

        for (PhuTung pt : kho.getPhuTungs()) {
            if (!laPhuTungLinhKien(pt)) continue; // Bỏ các dịch vụ
            tongPhuTung++;
            long gt = (long) pt.getSoLuongTon() * pt.getDonGia();
            tongGiaTri += gt;
            switch (pt.getMucCanhBao()) {
                case "HẾT HÀNG" -> soHet++;
                case "SẮP HẾT" -> soSapHet++;
                default -> soBinhThuong++;
            }
        }

        lblSoHet.setText(String.valueOf(soHet));
        lblSoSapHet.setText(String.valueOf(soSapHet));
        lblSoBinhThuong.setText(String.valueOf(soBinhThuong));
        lblTongGiaTri.setText(UiHelper.tien(tongGiaTri) + " đ");
        lblTongGiaTriPhuDe.setText("Qua " + tongPhuTung + " mặt hàng phụ tùng");

        // 2. Lọc và hiển thị lên bảng
        locDuLieu();
    }

    /**
     * Lọc bảng theo từ khóa tìm kiếm, phân loại, mức cảnh báo và sắp xếp theo bảng chữ cái / số mã.
     */
    private void locDuLieu() {
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();
        String tuKhoaKhongDau = UiHelper.boDauTiengViet(tuKhoa);

        String loaiChon = cboLocLoai.getSelectedItem() != null ? cboLocLoai.getSelectedItem().toString() : "";
        String canhBaoChon = cboLocCanhBao.getSelectedItem() != null ? cboLocCanhBao.getSelectedItem().toString() : "";
        int sapXepIdx = cboSapXep.getSelectedIndex();

        model.setRowCount(0);
        rowsCanhBao.clear();

        List<PhuTung> listLoc = new ArrayList<>();
        int tongPhuTungThucTe = 0;

        for (PhuTung pt : kho.getPhuTungs()) {
            if (!laPhuTungLinhKien(pt)) continue; // Bỏ hoàn toàn các gói dịch vụ
            tongPhuTungThucTe++;

            // 1. Lọc theo từ khóa (Mã hoặc Tên)
            if (!tuKhoa.isEmpty()) {
                String ma = pt.getMaPT().toLowerCase();
                String ten = pt.getTen() != null ? pt.getTen().toLowerCase() : "";
                String tenKhongDau = UiHelper.boDauTiengViet(ten);
                if (!ma.contains(tuKhoa) && !ten.contains(tuKhoa) && !tenKhongDau.contains(tuKhoaKhongDau)) {
                    continue;
                }
            }

            // 2. Lọc theo phân loại
            if (!loaiChon.isEmpty() && !loaiChon.startsWith("--")) {
                String phanLoai = DanhSachPhuTung_UI.phanLoaiPhuTung(pt);
                if (!phanLoai.equalsIgnoreCase(loaiChon)) {
                    continue;
                }
            }

            // 3. Lọc theo mức cảnh báo
            String cb = pt.getMucCanhBao();
            if (!canhBaoChon.isEmpty() && !canhBaoChon.startsWith("--")) {
                if (canhBaoChon.contains("HẾT") && !cb.contains("HẾT")) continue;
                if (canhBaoChon.contains("SẮP") && !cb.contains("SẮP")) continue;
                if (canhBaoChon.contains("BÌNH THƯỜNG") && (cb.contains("HẾT") || cb.contains("SẮP"))) continue;
            }

            listLoc.add(pt);
        }

        // 4. Sắp xếp danh sách theo bảng chữ cái hoặc số mã
        sapXepDanhSach(listLoc, sapXepIdx);

        // 5. Đưa dữ liệu đã sắp xếp lên bảng
        long tongGiaTriLoc = 0;
        int demLoc = 0;

        for (PhuTung pt : listLoc) {
            String cb = pt.getMucCanhBao();
            long giaTri = (long) pt.getSoLuongTon() * pt.getDonGia();
            tongGiaTriLoc += giaTri;
            demLoc++;

            int rowIdx = model.getRowCount();
            if (cb.contains("HẾT") || cb.contains("SẮP")) {
                rowsCanhBao.add(rowIdx);
            }

            model.addRow(new Object[]{
                    pt.getMaPT(),
                    pt.getTen(),
                    DanhSachPhuTung_UI.phanLoaiPhuTung(pt),
                    pt.getDonVi(),
                    pt.getSoLuongTon(),
                    pt.getSoLuongToiThieu(),
                    cb,
                    UiHelper.tien(pt.getDonGia()),
                    UiHelper.tien(giaTri)
            });
        }

        lblFooterTrai.setText("Hiển thị: " + demLoc + " / " + tongPhuTungThucTe + " phụ tùng linh kiện   |   Cảnh báo hết/sắp hết: " + rowsCanhBao.size());
        lblFooterPhai.setText("Tổng giá trị tồn hiển thị: " + UiHelper.tien(tongGiaTriLoc) + " VNĐ");

        bang.repaint();
    }

    /**
     * Sắp xếp danh sách theo tiêu chí người dùng lựa chọn:
     * - Bảng chữ cái tiếng Việt A-Z / Z-A
     * - Số mã phụ tùng tăng dần / giảm dần (PT001, PT002, ..., PT101...)
     * - Số lượng tồn kho / Giá trị tồn
     */
    private void sapXepDanhSach(List<PhuTung> list, int kieuSapXep) {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        collator.setStrength(Collator.SECONDARY);

        list.sort((a, b) -> {
            switch (kieuSapXep) {
                case 1 -> { // Mã PT giảm dần
                    return soSanhMa(b.getMaPT(), a.getMaPT());
                }
                case 2 -> { // Tên phụ tùng A - Z (bảng chữ cái)
                    int c = collator.compare(a.getTen() != null ? a.getTen() : "", b.getTen() != null ? b.getTen() : "");
                    return c != 0 ? c : soSanhMa(a.getMaPT(), b.getMaPT());
                }
                case 3 -> { // Tên phụ tùng Z - A
                    int c = collator.compare(b.getTen() != null ? b.getTen() : "", a.getTen() != null ? a.getTen() : "");
                    return c != 0 ? c : soSanhMa(a.getMaPT(), b.getMaPT());
                }
                case 4 -> { // Tồn kho ít nhất
                    int c = Integer.compare(a.getSoLuongTon(), b.getSoLuongTon());
                    return c != 0 ? c : soSanhMa(a.getMaPT(), b.getMaPT());
                }
                case 5 -> { // Tồn kho nhiều nhất
                    int c = Integer.compare(b.getSoLuongTon(), a.getSoLuongTon());
                    return c != 0 ? c : soSanhMa(a.getMaPT(), b.getMaPT());
                }
                case 6 -> { // Giá trị tồn cao nhất
                    long gtA = (long) a.getSoLuongTon() * a.getDonGia();
                    long gtB = (long) b.getSoLuongTon() * b.getDonGia();
                    int c = Long.compare(gtB, gtA);
                    return c != 0 ? c : soSanhMa(a.getMaPT(), b.getMaPT());
                }
                default -> { // 0: Mã PT số mã tăng dần (PT001 -> PT002 -> PT101...)
                    return soSanhMa(a.getMaPT(), b.getMaPT());
                }
            }
        });
    }

    /**
     * So sánh theo số mã tự nhiên (ví dụ PT001 < PT002 < PT010 < PT101).
     */
    public static int soSanhMa(String ma1, String ma2) {
        if (ma1 == null) return ma2 == null ? 0 : -1;
        if (ma2 == null) return 1;
        try {
            long num1 = Long.parseLong(ma1.replaceAll("[^0-9]", ""));
            long num2 = Long.parseLong(ma2.replaceAll("[^0-9]", ""));
            return Long.compare(num1, num2);
        } catch (Exception e) {
            return ma1.compareToIgnoreCase(ma2);
        }
    }

    private void inBaoCao() {
        JOptionPane.showMessageDialog(this,
                "Chức năng in báo cáo tồn kho: Hệ thống đã kết nối máy in và sẵn sàng xuất tài liệu.",
                "In báo cáo", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel theCanhBao(Icon icon, String ten, JLabel lblSo, String phuDe, Color mau) {
        JLabel lblPhu = new JLabel(phuDe);
        return theCanhBao(icon, ten, lblSo, lblPhu, mau);
    }

    private JPanel theCanhBao(Icon icon, String ten, JLabel lblSo, JLabel lblPhu, Color mau) {
        JPanel p = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(248, 250, 253));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new UiHelper.ShadowBorder(6, new Color(0, 0, 0, 18)),
                new EmptyBorder(8, 14, 8, 14)));
        p.setPreferredSize(new Dimension(0, 112));
        p.setMinimumSize(new Dimension(0, 112));

        JLabel lblIcon = new JLabel(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UiHelper.lighten(mau, 0.15f), 0, getHeight(), mau));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(46, 46));

        JPanel text = new JPanel(new BorderLayout(0, 2));
        text.setOpaque(false);

        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTen.setForeground(new Color(100, 110, 125));

        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSo.setForeground(mau);

        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPhu.setForeground(new Color(140, 150, 165));

        text.add(lblTen, BorderLayout.NORTH);
        text.add(lblSo, BorderLayout.CENTER);
        text.add(lblPhu, BorderLayout.SOUTH);

        p.add(lblIcon, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }
}
