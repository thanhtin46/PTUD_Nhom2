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

/**
 * DanhSachPhuTung_UI — Quản lý & Lọc danh mục phụ tùng Gara:
 * - Hỗ trợ lọc theo từng phân loại phụ tùng (Dầu nhớt, Phanh, Lọc, Điện - Ắc quy, Gầm, Động cơ, Lốp xe...)
 * - Hỗ trợ lọc theo tình trạng tồn kho (Còn nhiều, Trung bình, Sắp hết hàng)
 * - Thanh tìm kiếm thông minh theo Mã PT và Tên phụ tùng (tìm tức thì khi gõ)
 * - Click chọn dòng trong bảng đã lọc sẽ nạp ngay lên form để Sửa hoặc Xóa
 * - Đồng bộ 2 chiều tức thì với CSDL SQL Server và bộ nhớ KhoDuLieu
 */
public class DanhSachPhuTung_UI extends JPanel {
    // Form nhập liệu bên trái
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtDonVi = new JTextField();
    private final JTextField txtTon = new JTextField();
    private final JTextField txtGia = new JTextField();

    // Bảng dữ liệu và thanh lọc bên phải
    private JTable table;
    private DefaultTableModel model;
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
                g2.drawString("Tìm theo mã hoặc tên phụ tùng...", insets.left + 8, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                g2.dispose();
            }
        }
    };
    private final JComboBox<String> cboLoai = new JComboBox<>(new String[]{
            "Tất cả phân loại",
            "Lọc & Bảo dưỡng",
            "Dầu nhớt & Dung dịch",
            "Hệ thống Phanh",
            "Điện & Ắc quy",
            "Khung gầm & Giảm xóc",
            "Động cơ & Nhiên liệu",
            "Lốp & Phụ kiện"
    });
    private final JComboBox<String> cboTonKho = new JComboBox<>(new String[]{
            "Tất cả mức tồn",
            "Còn nhiều (> 20)",
            "Trung bình (10 - 20)",
            "Sắp hết (< 10)"
    });
    private final JLabel lblThongKe = new JLabel("Hiển thị: 0 phụ tùng");

    private final KhoDuLieu kho = KhoDuLieu.get();

    public DanhSachPhuTung_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý phụ tùng"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0.32);
        split.setDividerLocation(460);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);

        napBang();
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin phụ tùng", 460);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Mã phụ tùng *", txtMa);
        UiHelper.themDong(form, 1, "Tên phụ tùng *", txtTen);
        UiHelper.themDong(form, 2, "Đơn vị tính", txtDonVi);
        UiHelper.themDong(form, 4, "Đơn giá (VNĐ) *", txtGia);

        JPanel nut = new JPanel(new GridLayout(1, 4, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));
        nut.add(UiHelper.nut("Thêm", UiHelper.XANH_LA, e -> them()));
        nut.add(UiHelper.nut("Sửa", UiHelper.XANH_TRUNG, e -> sua()));
        nut.add(UiHelper.nut("Xóa", UiHelper.CHU_CHAY, e -> xoa()));
        nut.add(UiHelper.nut("Làm mới", UiHelper.XAM, e -> lamMoi()));

        UiHelper.ganNoiDungForm(formPanel, form, nut);
        return formPanel;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phụ tùng");

        // 1. THANH CÔNG CỤ LỌC & TÌM KIẾM
        JPanel pnlLoc = taoThanhLoc();
        bangPanel.add(pnlLoc, BorderLayout.NORTH);

        // 2. BẢNG DỮ LIỆU
        model = UiHelper.model("Mã PT", "Tên phụ tùng", "Phân loại", "ĐVT", "Tồn kho", "Đơn giá (đ)");
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(85);  // Mã PT
        table.getColumnModel().getColumn(1).setPreferredWidth(230); // Tên phụ tùng
        table.getColumnModel().getColumn(2).setPreferredWidth(140); // Phân loại
        table.getColumnModel().getColumn(3).setPreferredWidth(65);  // ĐVT
        table.getColumnModel().getColumn(4).setPreferredWidth(85);  // Tồn kho
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Đơn giá

        UiHelper.canGiua(table, 0, 3);
        UiHelper.canTrai(table, 1, 2);
        UiHelper.canPhai(table, 5);

        // Renderer cảnh báo cho cột Tồn kho (Cột 4)
        DefaultTableCellRenderer tonRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    int ton = Integer.parseInt(value.toString());
                    if (ton < 10) {
                        setForeground(new Color(220, 38, 38)); // Đỏ cảnh báo
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (ton <= 20) {
                        setForeground(new Color(217, 119, 6)); // Cam cảnh báo nhẹ
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(new Color(22, 163, 74)); // Xanh an toàn
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                } catch (Exception ignored) {
                    setForeground(new Color(51, 65, 85));
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(4).setCellRenderer(tonRenderer);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienLenForm();
        });

        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        return bangPanel;
    }

    private JPanel taoThanhLoc() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Hàng 1: Ô tìm kiếm + ComboBox Phân loại + ComboBox Mức tồn + Nút Bỏ lọc
        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);

        // Ô tìm kiếm
        gbc.gridx = 0;
        gbc.weightx = 0.40;
        txtTimKiem.setPreferredSize(new Dimension(0, 34));
        UiHelper.trangTriInput(txtTimKiem);
        txtTimKiem.setToolTipText("Tìm kiếm nhanh theo Mã PT hoặc Tên phụ tùng...");
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { napBang(); }
            @Override public void removeUpdate(DocumentEvent e) { napBang(); }
            @Override public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        row1.add(txtTimKiem, gbc);

        // Combobox Phân loại
        gbc.gridx = 1;
        gbc.weightx = 0.28;
        cboLoai.setPreferredSize(new Dimension(0, 34));
        cboLoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboLoai.addActionListener(e -> napBang());
        row1.add(cboLoai, gbc);

        // Combobox Mức tồn kho
        gbc.gridx = 2;
        gbc.weightx = 0.22;
        cboTonKho.setPreferredSize(new Dimension(0, 34));
        cboTonKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboTonKho.addActionListener(e -> napBang());
        row1.add(cboTonKho, gbc);

        // Nút Bỏ lọc
        gbc.gridx = 3;
        gbc.weightx = 0.10;
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton btnBoLoc = UiHelper.nut("Bỏ lọc", UiIcon.of(UiIcon.REFRESH, 13), UiHelper.XAM, true, e -> boLoc());
        btnBoLoc.setPreferredSize(new Dimension(85, 34));
        row1.add(btnBoLoc, gbc);

        // Hàng 2: Nhãn hướng dẫn & thống kê số lượng
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);

        lblThongKe.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblThongKe.setForeground(new Color(30, 58, 138));
        row2.add(lblThongKe, BorderLayout.WEST);

        JLabel lblHuongDan = new JLabel("💡 Mẹo: Nhấp vào dòng phụ tùng bất kỳ trong bảng để chỉnh sửa thông tin bên trái");
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHuongDan.setForeground(new Color(100, 116, 139));
        row2.add(lblHuongDan, BorderLayout.EAST);

        wrap.add(row1, BorderLayout.NORTH);
        wrap.add(row2, BorderLayout.SOUTH);
        return wrap;
    }

    public static String phanLoaiPhuTung(PhuTung pt) {
        if (pt == null || pt.getTen() == null) return "Khác";
        String ten = pt.getTen().toLowerCase();
        if (ten.contains("lọc")) return "Lọc & Bảo dưỡng";
        if (ten.contains("dầu") || ten.contains("nhớt") || ten.contains("nước làm mát") || ten.contains("dung dịch")) return "Dầu nhớt & Dung dịch";
        if (ten.contains("phanh")) return "Hệ thống Phanh";
        if (ten.contains("quy") || ten.contains("bugi") || ten.contains("đèn") || ten.contains("mobin") || ten.contains("điện")) return "Điện & Ắc quy";
        if (ten.contains("giảm xóc") || ten.contains("rô-tuyn") || ten.contains("gầm") || ten.contains("thước lái")) return "Khung gầm & Giảm xóc";
        if (ten.contains("bơm") || ten.contains("curoa") || ten.contains("động cơ") || ten.contains("nhiên liệu")) return "Động cơ & Nhiên liệu";
        if (ten.contains("lốp") || ten.contains("gạt mưa") || ten.contains("vỏ")) return "Lốp & Phụ kiện";
        return "Khác";
    }

    private void boLoc() {
        txtTimKiem.setText("");
        cboLoai.setSelectedIndex(0);
        cboTonKho.setSelectedIndex(0);
        napBang();
    }

    private void napBang() {
        String tuKhoa = txtTimKiem != null ? txtTimKiem.getText().trim().toLowerCase() : "";
        String loaiChon = cboLoai != null ? String.valueOf(cboLoai.getSelectedItem()) : "Tất cả phân loại";
        String tonChon = cboTonKho != null ? String.valueOf(cboTonKho.getSelectedItem()) : "Tất cả mức tồn";

        model.setRowCount(0);
        int dem = 0;
        int tong = kho.getPhuTungs().size();

        for (PhuTung pt : kho.getPhuTungs()) {
            // 1. Lọc theo từ khóa tìm kiếm (Mã PT hoặc Tên PT)
            if (!tuKhoa.isEmpty()) {
                String ma = pt.getMaPT() != null ? pt.getMaPT().toLowerCase() : "";
                String ten = pt.getTen() != null ? pt.getTen().toLowerCase() : "";
                if (!ma.contains(tuKhoa) && !ten.contains(tuKhoa)) {
                    continue;
                }
            }

            // 2. Lọc theo nhóm phân loại
            if (!"Tất cả phân loại".equals(loaiChon)) {
                String loaiPT = phanLoaiPhuTung(pt);
                if (!loaiPT.equalsIgnoreCase(loaiChon)) {
                    continue;
                }
            }

            // 3. Lọc theo mức tồn kho
            if ("Còn nhiều (> 20)".equals(tonChon) && pt.getSoLuongTon() <= 20) {
                continue;
            } else if ("Trung bình (10 - 20)".equals(tonChon) && (pt.getSoLuongTon() < 10 || pt.getSoLuongTon() > 20)) {
                continue;
            } else if ("Sắp hết (< 10)".equals(tonChon) && pt.getSoLuongTon() >= 10) {
                continue;
            }

            dem++;
            model.addRow(new Object[]{
                    pt.getMaPT(),
                    pt.getTen(),
                    phanLoaiPhuTung(pt),
                    pt.getDonVi(),
                    pt.getSoLuongTon(),
                    UiHelper.tien(pt.getDonGia())
            });
        }

        if (lblThongKe != null) {
            lblThongKe.setText("Hiển thị: " + dem + " / " + tong + " phụ tùng");
        }
    }

    private void them() {
        if (!kiemTra(true)) return;
        PhuTung pt = new PhuTung(txtMa.getText().trim(), txtTen.getText().trim(),
                txtDonVi.getText().trim(), Integer.parseInt(txtTon.getText().trim()),
                Long.parseLong(txtGia.getText().trim().replace(",", "")));
        boolean ok = kho.themPhuTung(pt);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Thêm phụ tùng và lưu CSDL SQL Server thành công!" : "Thêm phụ tùng thành công!");
    }

    private void xoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng cần xóa trong danh sách!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ma = table.getValueAt(dong, 0).toString();
        String ten = table.getValueAt(dong, 1).toString();
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa phụ tùng [" + ma + "] " + ten + "?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

        boolean ok = kho.xoaPhuTung(ma);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Đã xóa phụ tùng khỏi CSDL SQL Server!" : "Đã xóa phụ tùng!");
    }

    private void sua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng cần sửa trong danh sách!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!kiemTra(false)) return;
        String ma = table.getValueAt(dong, 0).toString();
        PhuTung pt = kho.timPhuTung(ma);
        if (pt == null) return;

        pt.setTen(txtTen.getText().trim());
        pt.setDonVi(txtDonVi.getText().trim());
        pt.setDonGia(Long.parseLong(txtGia.getText().trim().replace(",", "")));
        boolean ok = kho.capNhatPhuTung(pt);
        napBang();
        JOptionPane.showMessageDialog(this, ok ? "Cập nhật phụ tùng [" + ma + "] và đồng bộ CSDL SQL Server thành công!" : "Cập nhật thành công!");
    }

    private void lamMoi() {
        txtMa.setText("");
        txtTen.setText("");
        txtDonVi.setText("");
        txtGia.setText("");
        txtMa.setEditable(true);
        table.clearSelection();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        txtMa.setText(table.getValueAt(dong, 0).toString());
        txtTen.setText(table.getValueAt(dong, 1).toString());
        txtDonVi.setText(table.getValueAt(dong, 3).toString());
        txtGia.setText(table.getValueAt(dong, 5).toString().replace(",", ""));
        txtMa.setEditable(false);
    }

    private boolean kiemTra(boolean them) {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            UiHelper.loi(this, "Mã và tên phụ tùng không được để trống!", txtMa);
            return false;
        }
        if (!txtGia.getText().trim().replace(",", "").matches("\\d+")) {
            UiHelper.loi(this, " đơn giá phải là số nguyên dương hợp lệ!", txtTon);
            return false;
        }
        if (them && kho.timPhuTung(txtMa.getText().trim()) != null) {
            UiHelper.loi(this, "Mã phụ tùng đã tồn tại trong hệ thống!", txtMa);
            return false;
        }
        return true;
    }
}
