package UI;

import Entity.LenhSuaChuaTam;
import Entity.NhanVien;
import DAO.PhieuXuat_DAO;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Giao diện lập phiếu xuất kho phụ tùng.
 */
public class XuatKho_UI extends JPanel {

    private final Color NAVY = new Color(17, 42, 70);
    private final Color BLUE = new Color(65, 91, 170);
    private final Color LIGHT_BLUE = new Color(230, 238, 255);
    private final Color BORDER = new Color(200, 207, 219);
    private final Color BG = new Color(247, 249, 253);
    private final Color GREEN = new Color(0, 145, 105);

    private final NhanVien nhanVienHienTai;

    private JTextField txtMaPhieu;
    private JTextField txtNgayLap;
    private JTextField txtThuKho;
    private JTextField txtLenhSuaChua;
    private JComboBox<String> cboLyDo;

    private DefaultTableModel model;
    private JTable table;

    private JLabel lblSoMatHang;
    private JLabel lblTongTien;
    private JLabel lblTongGiaTri;
    private JLabel lblChietKhau;
    private static String maPSCCanChon;

    public static void datPSCCanChon(String maPSC) {
        maPSCCanChon = maPSC;
    }

    private final DecimalFormat df = new DecimalFormat("#,##0");

    public XuatKho_UI(NhanVien nhanVienHienTai) {
        this.nhanVienHienTai = nhanVienHienTai;

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        content.add(createHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(BG);
        center.add(createThongTinPanel(), BorderLayout.NORTH);
        center.add(createDanhSachPanel(), BorderLayout.CENTER);

        content.add(center, BorderLayout.CENTER);
        content.add(createTongKetPanel(), BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        capNhatTongTien();
    }

    private void setFontChu(Component c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                setFontChu(child);
            }
        }
    }

    // ---------------- HEADER ----------------

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(8, 5, 10, 5));

        JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
        titlePanel.setBackground(Color.WHITE);

        JLabel icon = new JLabel(UiIcon.of(UiIcon.BOX, 24), SwingConstants.CENTER);
        icon.setForeground(BLUE);
        icon.setPreferredSize(new Dimension(42, 42));
        icon.setOpaque(true);
        icon.setBackground(LIGHT_BLUE);
        icon.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel titles = new JPanel();
        titles.setBackground(Color.WHITE);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("LẬP PHIẾU XUẤT KHO PHỤ TÙNG & VẬT TƯ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Xuất cấp phụ tùng thay thế và vật tư tiêu hao trực tiếp cho Kỹ thuật viên theo lệnh sửa chữa.");
        subtitle.setForeground(new Color(90, 98, 110));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        titlePanel.add(icon, BorderLayout.WEST);
        titlePanel.add(titles, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttons.setBackground(Color.WHITE);

        JButton btnHuy = taoNut("Hủy bỏ", UiIcon.CLOSE, Color.WHITE, NAVY);
        JButton btnLuu = taoNut("Lưu tạm bản nháp", UiIcon.FILE, Color.WHITE, NAVY);
        JButton btnIn = taoNut("In phiếu xuất", UiIcon.PRINT, Color.WHITE, NAVY);

        btnHuy.addActionListener(e -> huyPhieu());
        btnLuu.addActionListener(e -> luuTam());
        btnIn.addActionListener(e -> inPhieu());

        buttons.add(btnHuy);
        buttons.add(btnLuu);
        buttons.add(btnIn);

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(buttons, BorderLayout.EAST);
        return header;
    }

    // ---------------- THÔNG TIN PHIẾU ----------------

    private JPanel createThongTinPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));

        panel.add(tieuDeMuc("1. THÔNG TIN CHỨNG TỪ & LỆNH SỬA CHỮA LIÊN KẾT"),
                BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 3, 10, 8));
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(10, 10, 10, 10));

        txtMaPhieu = new JTextField(taoMaPhieu());
        txtMaPhieu.setEditable(false);
        txtMaPhieu.setBackground(new Color(241, 245, 253));

        txtNgayLap = new JTextField(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtNgayLap.setEditable(false);

        String tenThuKho = "Chưa xác định";
        if (nhanVienHienTai != null) {
            // Không phụ thuộc tên getter cụ thể của Entity.NhanVien.
            tenThuKho = nhanVienHienTai.toString();
        }
        txtThuKho = new JTextField(tenThuKho);
        txtThuKho.setEditable(false);
        txtThuKho.setBackground(new Color(241, 245, 253));

        txtLenhSuaChua = new JTextField();
        txtLenhSuaChua.setToolTipText("Chọn lệnh sửa chữa cần xuất phụ tùng");

        JButton btnChonPSC = taoNut("Chọn phiếu SC / Tra cứu", UiIcon.SEARCH, LIGHT_BLUE, BLUE);
        btnChonPSC.addActionListener(e -> chonPhieuSuaChua());

        JPanel lienKetPanel = new JPanel(new BorderLayout(5, 0));
        lienKetPanel.setBackground(Color.WHITE);
        lienKetPanel.add(txtLenhSuaChua, BorderLayout.CENTER);
        lienKetPanel.add(btnChonPSC, BorderLayout.EAST);

        cboLyDo = new JComboBox<>(new String[]{
                "Xuất vật tư thay thế theo phiếu sửa chữa",
                "Xuất vật tư bổ sung",
                "Xuất vật tư bảo hành"
        });

        fields.add(createField("Mã phiếu xuất kho *", txtMaPhieu));
        fields.add(createField("Ngày giờ lập phiếu *", txtNgayLap));
        fields.add(createField("Thủ kho lập phiếu *", txtThuKho));

        fields.add(createField("Lệnh sửa chữa liên kết *", lienKetPanel));
        fields.add(createField("Lý do xuất kho *", cboLyDo));
        fields.add(createField(" ", new JLabel(" ")));

        panel.add(fields, BorderLayout.CENTER);
        return panel;
    }

    private String taoMaPhieu() {
        return "PXK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    private void chonPhieuSuaChua() {
        PhieuXuat_DAO dao = new PhieuXuat_DAO();

        try {
            DefaultTableModel modelPSC =
                    dao.layDanhSachPSCChoXuat();

            if (modelPSC.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không có phiếu sửa chữa nào đang chờ phụ tùng.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            JTable tbl = new JTable(modelPSC);
            tbl.setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION
            );
            tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            int[] widths = {
                    110, 100, 160, 110, 100, 160, 180, 110
            };

            for (int i = 0; i < widths.length; i++) {
                tbl.getColumnModel()
                        .getColumn(i)
                        .setPreferredWidth(widths[i]);
            }

            JScrollPane scroll = new JScrollPane(tbl);
            scroll.setPreferredSize(new Dimension(1100, 300));

            int result = JOptionPane.showConfirmDialog(
                    this,
                    scroll,
                    "Chọn phiếu sửa chữa đang chờ phụ tùng",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            int row = tbl.getSelectedRow();

            if (row < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Vui lòng chọn một phiếu sửa chữa."
                );
                return;
            }

            String maPSC = tbl.getValueAt(row, 0).toString();

            txtLenhSuaChua.setText(maPSC);

            try {
                PhieuXuat_DAO daoo = new PhieuXuat_DAO();

                dao.layPhuTungTheoPSC(maPSC, model);

                capNhatTongTien();

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Phiếu sửa chữa chưa có phụ tùng trong ChiTietSuaChua.",
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể tải phụ tùng: " + ex.getMessage(),
                        "Lỗi database",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            JOptionPane.showMessageDialog(
                    this,
                    "Đã chọn phiếu sửa chữa: " + maPSC,
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách phiếu sửa chữa:\n"
                            + ex.getMessage(),
                    "Lỗi database",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createField(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(NAVY);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));

        input.setPreferredSize(new Dimension(150, 30));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    // ---------------- DANH SÁCH PHỤ TÙNG ----------------

    private JPanel createDanhSachPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(LIGHT_BLUE);

        JLabel title = new JLabel("2. DANH SÁCH PHỤ TÙNG & VẬT TƯ XUẤT KHO",
                UiIcon.of(UiIcon.BOX, 16, NAVY), SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(NAVY);
        title.setIconTextGap(8);
        title.setBorder(new EmptyBorder(9, 3, 9, 3));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 3));
        actions.setBackground(LIGHT_BLUE);

        JButton btnThem = taoNut("Thêm phụ tùng ngoài lệnh", UiIcon.PLUS, Color.WHITE, NAVY);
        JButton btnXoa = taoNut("Xóa dòng đã chọn", UiIcon.TRASH, Color.WHITE, Color.RED);
        JButton btnRefresh = taoNut("Làm mới", UiIcon.REFRESH, Color.WHITE, NAVY);

        btnThem.addActionListener(e -> themPhuTung());
        btnXoa.addActionListener(e -> xoaDong());
        btnRefresh.addActionListener(e -> capNhatTongTien());

        actions.add(btnThem);
        actions.add(btnXoa);
        actions.add(btnRefresh);
        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        String[] columns = {
                "Chọn", "STT", "Mã phụ tùng", "Tên phụ tùng",
                "ĐVT", "Tồn khả dụng",
                "SL yêu cầu", "SL thực xuất", "Đơn giá (VNĐ)",
                "Thành tiền (VNĐ)", "Tình trạng xuất"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0 || col == 7; // Chỉ cho chọn phụ tùng và sửa số lượng thực xuất
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0) return Boolean.class;
                if (col == 1 || col == 6 || col == 7) {
                    return Integer.class;
                }
                return Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(65);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setForeground(NAVY);
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionBackground(new Color(214, 227, 250));
        table.setSelectionForeground(NAVY);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(245, 247, 251));
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setPreferredSize(new Dimension(100, 45));
        table.getTableHeader().setReorderingAllowed(false);

        int[] widths = {45, 45, 105, 175, 55, 95, 75, 85, 110, 120, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0
                            ? new Color(239, 244, 255) : Color.WHITE);
                }
                setBorder(new EmptyBorder(5, 7, 5, 7));
                setVerticalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);

        // Checkbox có renderer mặc định riêng; đặt màu xen kẽ cho checkbox.
        table.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
            private final JCheckBox checkBox = new JCheckBox();

            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                checkBox.setSelected(Boolean.TRUE.equals(value));
                checkBox.setOpaque(true);
                checkBox.setBackground(isSelected
                        ? tbl.getSelectionBackground()
                        : (row % 2 == 0 ? new Color(239, 244, 255) : Color.WHITE));
                return checkBox;
            }
        });

        // Chỉ tính tổng khi người dùng đổi checkbox hoặc số lượng thực xuất.
        model.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE
                    && (e.getColumn() == 0 || e.getColumn() == 7
                    || e.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS)) {
                capNhatTongTien();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setMinimumSize(new Dimension(0, 180));
        // Không khóa chiều rộng/cao của JScrollPane: bảng tự giãn theo vùng màn hình.
        scroll.setBorder(BorderFactory.createEmptyBorder());

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ---------------- TỔNG KẾT ----------------

    private JPanel createTongKetPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(Color.WHITE);

        JLabel title = new JLabel("Tổng hợp giá trị phiếu xuất",
                UiIcon.of(UiIcon.BILL, 16, NAVY), SwingConstants.LEFT);
        title.setIconTextGap(7);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(NAVY);

        JLabel badge = new JLabel("Hạch toán tự động");
        badge.setForeground(BLUE);
        badge.setOpaque(true);
        badge.setBackground(LIGHT_BLUE);
        badge.setBorder(new EmptyBorder(4, 7, 4, 7));

        heading.add(title, BorderLayout.WEST);
        heading.add(badge, BorderLayout.EAST);

        JPanel values = new JPanel(new GridLayout(3, 1, 4, 4));
        values.setBackground(Color.WHITE);
        values.setBorder(new EmptyBorder(8, 0, 8, 0));

        lblSoMatHang = new JLabel("0 mặt hàng (0 đơn vị)");
        lblTongTien = new JLabel("0 VNĐ");
        lblChietKhau = new JLabel("0 VNĐ (0%)");

        values.add(createSummaryRow("Tổng số lượng mặt hàng:", lblSoMatHang));
        values.add(createSummaryRow("Tổng tiền phụ tùng (Trước thuế):", lblTongTien));
        values.add(createSummaryRow("Chiết khấu điều chuyển nội bộ:", lblChietKhau));

        JPanel total = new JPanel(new BorderLayout());
        total.setBackground(Color.WHITE);
        total.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JLabel totalLabel = new JLabel("TỔNG GIÁ TRỊ XUẤT KHO:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalLabel.setForeground(NAVY);

        lblTongGiaTri = new JLabel("0 VNĐ");
        lblTongGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTongGiaTri.setForeground(NAVY);
        lblTongGiaTri.setHorizontalAlignment(SwingConstants.RIGHT);

        total.add(totalLabel, BorderLayout.WEST);
        total.add(lblTongGiaTri, BorderLayout.EAST);

        JButton btnXacNhan = UiHelper.nut("  Xác nhận & Xuất kho",
                UiIcon.of(UiIcon.CHECK, 18, Color.WHITE),
                NAVY, true, e -> xacNhanXuat());
        btnXacNhan.setPreferredSize(new Dimension(0, 46));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel body = new JPanel(new BorderLayout(5, 5));
        body.setBackground(Color.WHITE);
        body.add(values, BorderLayout.NORTH);
        body.add(total, BorderLayout.CENTER);
        body.add(btnXacNhan, BorderLayout.SOUTH);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryRow(String text, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);

        JLabel label = new JLabel(text);
        label.setForeground(new Color(90, 98, 110));
        value.setForeground(NAVY);
        value.setFont(new Font("Segoe UI", Font.BOLD, 11));

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private JPanel tieuDeMuc(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BLUE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel label = new JLabel(text, UiIcon.of(UiIcon.FILE, 15, NAVY), SwingConstants.LEFT);
        label.setIconTextGap(7);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(NAVY);
        label.setBorder(new EmptyBorder(7, 3, 7, 3));

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JButton taoNut(String text, String iconType, Color bg, Color fg) {
        JButton button = new JButton(text, UiIcon.of(iconType, 15, fg)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean pressed = getModel().isPressed();
                boolean hover = getModel().isRollover();
                Color c = pressed ? (bg.equals(Color.WHITE) ? new Color(226, 232, 240) : bg.darker())
                                  : (hover ? (bg.equals(Color.WHITE) ? new Color(241, 245, 249) : bg.brighter()) : bg);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setIconTextGap(7);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(7, 9, 7, 9));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        return button;
    }

    private JButton taoNut(String text, Color bg, Color fg) {
        return taoNut(text, UiIcon.PLUS, bg, fg);
    }

    // ---------------- TÍNH TỔNG ----------------

    private void capNhatTongTien() {
        if (model == null || lblSoMatHang == null) {
            return;
        }

        int soMatHang = 0;
        int tongSoLuong = 0;
        long tongTien = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            if (!Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                continue;
            }

            // Cột 7: Số lượng thực xuất
            int soLuong = docSo(model.getValueAt(i, 7));

            // Cột 8: Đơn giá
            long donGia = docSo(model.getValueAt(i, 8));

            soMatHang++;
            tongSoLuong += soLuong;
            tongTien += (long) soLuong * donGia;

            // Cột 9: Thành tiền
            model.setValueAt((long) soLuong * donGia, i, 9);
        }

        lblSoMatHang.setText(
                soMatHang + " mặt hàng (" + tongSoLuong + " đơn vị)"
        );

        lblTongTien.setText(df.format(tongTien) + " VNĐ");
        lblTongGiaTri.setText(df.format(tongTien) + " VNĐ");
        lblChietKhau.setText("0 VNĐ (0%)");
    }

    private int docSo(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        String text = value.toString().replace(",", "").trim();
        if (text.isEmpty()) return 0;
        return Integer.parseInt(text);
    }

    // ---------------- THAO TÁC ----------------

    private void themPhuTung() {
        JTextField ma = new JTextField();
        JTextField ten = new JTextField();
        JTextField dvt = new JTextField("Cái");
        JTextField ton = new JTextField();
        JTextField sl = new JTextField("1");
        JTextField gia = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Mã phụ tùng:"));
        form.add(ma);
        form.add(new JLabel("Tên phụ tùng:"));
        form.add(ten);
        form.add(new JLabel("Đơn vị tính:"));
        form.add(dvt);
        form.add(new JLabel("Tồn khả dụng:"));
        form.add(ton);
        form.add(new JLabel("Số lượng xuất:"));
        form.add(sl);
        form.add(new JLabel("Đơn giá:"));
        form.add(gia);

        int result = JOptionPane.showConfirmDialog(this, form, "Thêm phụ tùng ngoài lệnh",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String maPT = ma.getText().trim();
            String tenPT = ten.getText().trim();
            String donVi = dvt.getText().trim();
            int tonKho = Integer.parseInt(ton.getText().trim());
            int soLuong = Integer.parseInt(sl.getText().trim());
            long donGia = Long.parseLong(gia.getText().trim().replace(",", ""));

            if (maPT.isEmpty() || tenPT.isEmpty() || donVi.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ mã, tên và đơn vị tính.");
            }
            if (tonKho < 0 || soLuong <= 0 || donGia < 0) {
                throw new IllegalArgumentException("Tồn kho, số lượng hoặc đơn giá không hợp lệ.");
            }
            if (soLuong > tonKho) {
                throw new IllegalArgumentException("Số lượng xuất vượt tồn khả dụng.");
            }

            model.addRow(new Object[]{
                    true, model.getRowCount() + 1, maPT, tenPT, donVi,
                     tonKho + " " + donVi,
                    soLuong, soLuong, donGia, (long) soLuong * donGia,
                    "Chờ xuất"
            });
            capNhatTongTien();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Tồn kho, số lượng và đơn giá phải là số hợp lệ.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xoaDong() {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                model.removeRow(i);
            }
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 1);
        }
        capNhatTongTien();
    }

    private void luuTam() {
        JOptionPane.showMessageDialog(this,
                "Giao diện hiện chưa kết nối database nên chưa lưu được bản nháp.",
                "Chưa kết nối dữ liệu", JOptionPane.INFORMATION_MESSAGE);
    }

    private void inPhieu() {
        // Xem trước phiếu xuất kho
    }

    private void huyPhieu() {
        int result = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn xóa các dòng phụ tùng trên phiếu hiện tại?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            model.setRowCount(0);
            capNhatTongTien();
        }
    }


    private void xacNhanXuat() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.XUAT)) return;
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        String maPSC = txtLenhSuaChua.getText().trim();

        if (maPSC.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu sửa chữa.");
            return;
        }

        int soDongChon = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            if (Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                soDongChon++;
            }
        }

        if (soDongChon == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng cần xuất.");
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận xuất " + soDongChon + " mặt hàng?\n"
                        + "Tồn kho sẽ được cập nhật trong SQL Server.",
                "Xác nhận xuất kho",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String maNV = nhanVienHienTai == null
                    ? ""
                    : nhanVienHienTai.getMaNV();

            String lyDo = cboLyDo.getSelectedItem() == null
                    ? "Xuất vật tư theo phiếu sửa chữa"
                    : cboLyDo.getSelectedItem().toString();

            PhieuXuat_DAO dao = new PhieuXuat_DAO();

            String maPX = dao.taoVaXacNhanXuat(
                    maPSC, maNV, model, lyDo
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Xuất kho thành công!\n"
                            + "Mã phiếu xuất: " + maPX
                            + "\nMã phiếu sửa chữa: " + maPSC,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Tải lại tồn kho sau khi xuất thành công.
            dao.layPhuTungTheoPSC(maPSC, model);
            capNhatTongTien();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xác nhận xuất kho:\n" + ex.getMessage(),
                    "Lỗi database",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        // Chuyển mã phiếu vừa xuất kho sang màn hình sửa chữa
        LapPhieuSuaChua_UI.datPSCCanMoLai(maPSC);

// Quay về màn hình phiếu sửa chữa
        Main_UI mainUI = Main_UI.getInstance();

        if (mainUI != null && mainUI.coQuyenTrang("Phiếu sửa chữa")) {
            mainUI.chuyenTrang("Phiếu sửa chữa");
        }
    }


    // Chạy thử giao diện độc lập.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gara Auto - Xuất kho");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new XuatKho_UI(null));
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }


}
