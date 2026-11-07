package UI;

import DAO.DuLieu_DAO;
import Entity.KhoDuLieu;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Màn hình quản lý danh sách phụ tùng trong kho.
 * Hỗ trợ tìm kiếm, lọc theo phân loại và cập nhật thông tin.
 * Cho phép chọn Phân loại và Nhà cung cấp khi thêm/sửa phụ tùng.
 */
public class DanhSachPhuTung_UI extends JPanel {

    // ── Các phân loại cố định ──────────────────────────────────────────────
    static final String[] PHAN_LOAI_OPTIONS = {
            "Lọc & Bảo dưỡng",
            "Dầu nhớt & Dung dịch",
            "Hệ thống Phanh",
            "Điện & Ắc quy",
            "Khung gầm & Giảm xóc",
            "Động cơ & Nhiên liệu",
            "Lốp & Phụ kiện",
            "Khác"
    };

    // ── Form nhập liệu bên trái ───────────────────────────────────────────
    private final JTextField     txtMa    = new JTextField();
    private final JTextField     txtTen   = new JTextField();
    private final JTextField     txtDonVi = new JTextField();
    private final JTextField     txtTon   = new JTextField();
    private final JTextField     txtGia   = new JTextField();
    private final JComboBox<String> cboLoaiForm = new JComboBox<>(PHAN_LOAI_OPTIONS);
    private final JComboBox<String> cboNCC      = new JComboBox<>();

    // ── Bảng & thanh lọc bên phải ────────────────────────────────────────
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
                g2.drawString("Tìm theo mã hoặc tên phụ tùng...", insets.left + 8,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                g2.dispose();
            }
        }
    };
    private final JComboBox<String> cboLocLoai = new JComboBox<>();
    private final JComboBox<String> cboTonKho  = new JComboBox<>(new String[]{
            "Tất cả mức tồn", "Còn nhiều (> 20)", "Trung bình (10 - 20)", "Sắp hết (< 10)"
    });
    private final JLabel lblThongKe = new JLabel("Hiển thị: 0 phụ tùng");

    private final KhoDuLieu kho = KhoDuLieu.get();

    public DanhSachPhuTung_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý phụ tùng"), BorderLayout.NORTH);

        napDanhSachNCC();
        napLocLoai();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0.32);
        split.setDividerLocation(480);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);

        napBang();
    }

    // ─────────────────────────────────────────────────────────────────────
    // NẠP DỮ LIỆU PHỤ
    // ─────────────────────────────────────────────────────────────────────

    /** Nạp danh sách nhà cung cấp từ CSDL vào combo. */
    private void napDanhSachNCC() {
        cboNCC.removeAllItems();
        cboNCC.addItem("-- Chọn nhà cung cấp --");
        ArrayList<String> dsNCC = kho.layDanhSachNhaCungCap();
        for (String s : dsNCC) cboNCC.addItem(s);
    }

    /** Khởi tạo combobox lọc phân loại (gồm "Tất cả" + các loại cố định). */
    private void napLocLoai() {
        cboLocLoai.removeAllItems();
        cboLocLoai.addItem("Tất cả phân loại");
        for (String s : PHAN_LOAI_OPTIONS) cboLocLoai.addItem(s);
        cboLocLoai.addActionListener(e -> napBang());
    }

    // ─────────────────────────────────────────────────────────────────────
    // XÂY DỰNG FORM TRÁI
    // ─────────────────────────────────────────────────────────────────────
    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin phụ tùng", 480);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        // Mã phụ tùng
        UiHelper.themDong(form, 0, "Mã phụ tùng *", txtMa);
        // Tên phụ tùng
        UiHelper.themDong(form, 1, "Tên phụ tùng *", txtTen);
        // Phân loại (combobox)
        cboLoaiForm.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        UiHelper.themDong(form, 2, "Phân loại *", cboLoaiForm);
        // Đơn vị tính
        UiHelper.themDong(form, 3, "Đơn vị tính", txtDonVi);
        // Đơn giá
        UiHelper.themDong(form, 4, "Đơn giá (VNĐ) *", txtGia);
        // Nhà cung cấp (combobox)
        cboNCC.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        UiHelper.themDong(form, 5, "Nhà cung cấp", cboNCC);

        // Tồn kho — chỉ đọc (hệ thống quản lý qua phiếu nhập/xuất)
        txtTon.setText("0");
        txtTon.setEditable(false);

        // Nút hành động
        JPanel nut = new JPanel(new GridLayout(1, 4, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));
        nut.add(UiHelper.nut("Thêm",    UiHelper.XANH_LA,   e -> them()));
        nut.add(UiHelper.nut("Sửa",     UiHelper.XANH_TRUNG, e -> sua()));
        nut.add(UiHelper.nut("Xóa",     UiHelper.CHU_CHAY,   e -> xoa()));
        nut.add(UiHelper.nut("Làm mới", UiHelper.XAM,        e -> lamMoi()));

        UiHelper.ganNoiDungForm(formPanel, form, nut);
        return formPanel;
    }

    // ─────────────────────────────────────────────────────────────────────
    // XÂY DỰNG BẢNG PHẢI
    // ─────────────────────────────────────────────────────────────────────
    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách phụ tùng");

        bangPanel.add(taoThanhLoc(), BorderLayout.NORTH);

        // Cột: Mã PT | Tên | Phân loại | NCC | ĐVT | Tồn kho | Đơn giá
        model = UiHelper.model("Mã PT", "Tên phụ tùng", "Phân loại", "Nhà cung cấp", "ĐVT", "Tồn kho", "Đơn giá (đ)");
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Mã PT
        table.getColumnModel().getColumn(1).setPreferredWidth(200);  // Tên
        table.getColumnModel().getColumn(2).setPreferredWidth(135);  // Phân loại
        table.getColumnModel().getColumn(3).setPreferredWidth(150);  // NCC
        table.getColumnModel().getColumn(4).setPreferredWidth(60);   // ĐVT
        table.getColumnModel().getColumn(5).setPreferredWidth(80);   // Tồn kho
        table.getColumnModel().getColumn(6).setPreferredWidth(115);  // Đơn giá

        UiHelper.canGiua(table, 0, 4);
        UiHelper.canTrai(table, 1, 2, 3);
        UiHelper.canPhai(table, 6);

        // Renderer màu cho cột Tồn kho (cột 5)
        DefaultTableCellRenderer tonRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    int ton = Integer.parseInt(value.toString());
                    if (ton < 10) {
                        setForeground(new Color(220, 38, 38));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (ton <= 20) {
                        setForeground(new Color(217, 119, 6));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(new Color(22, 163, 74));
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                } catch (Exception ignored) {
                    setForeground(new Color(51, 65, 85));
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(tonRenderer);

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

        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);

        // Ô tìm kiếm
        gbc.gridx = 0; gbc.weightx = 0.38;
        txtTimKiem.setPreferredSize(new Dimension(0, 34));
        UiHelper.trangTriInput(txtTimKiem);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { napBang(); }
            @Override public void removeUpdate(DocumentEvent e) { napBang(); }
            @Override public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        row1.add(txtTimKiem, gbc);

        // ComboBox Phân loại
        gbc.gridx = 1; gbc.weightx = 0.26;
        cboLocLoai.setPreferredSize(new Dimension(0, 34));
        cboLocLoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row1.add(cboLocLoai, gbc);

        // ComboBox Mức tồn kho
        gbc.gridx = 2; gbc.weightx = 0.22;
        cboTonKho.setPreferredSize(new Dimension(0, 34));
        cboTonKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboTonKho.addActionListener(e -> napBang());
        row1.add(cboTonKho, gbc);

        // Nút Bỏ lọc
        gbc.gridx = 3; gbc.weightx = 0.14; gbc.insets = new Insets(0, 0, 0, 0);
        JButton btnBoLoc = UiHelper.nut("Bỏ lọc", UiIcon.of(UiIcon.REFRESH, 13), UiHelper.XAM, true, e -> boLoc());
        btnBoLoc.setPreferredSize(new Dimension(85, 34));
        row1.add(btnBoLoc, gbc);

        // Hàng 2: thống kê + hướng dẫn
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        lblThongKe.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblThongKe.setForeground(new Color(30, 58, 138));
        row2.add(lblThongKe, BorderLayout.WEST);

        JLabel lblHuongDan = new JLabel("💡 Nhấp vào dòng bất kỳ để chỉnh sửa thông tin bên trái");
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHuongDan.setForeground(new Color(100, 116, 139));
        row2.add(lblHuongDan, BorderLayout.EAST);

        wrap.add(row1, BorderLayout.NORTH);
        wrap.add(row2, BorderLayout.SOUTH);
        return wrap;
    }

    // ─────────────────────────────────────────────────────────────────────
    // PHÂN LOẠI TỰ ĐỘNG THEO TÊN (dùng khi cột loaiPT trong DB còn trống)
    // ─────────────────────────────────────────────────────────────────────
    public static String phanLoaiPhuTung(PhuTung pt) {
        // Ưu tiên giá trị lưu trong DB
        if (pt != null && pt.getLoaiPT() != null && !pt.getLoaiPT().trim().isEmpty()) {
            return pt.getLoaiPT().trim();
        }
        if (pt == null || pt.getTen() == null) return "Khác";
        String ten = pt.getTen().toLowerCase();
        if (ten.contains("lọc"))                                           return "Lọc & Bảo dưỡng";
        if (ten.contains("dầu") || ten.contains("nhớt")
                || ten.contains("nước làm mát") || ten.contains("dung dịch")) return "Dầu nhớt & Dung dịch";
        if (ten.contains("phanh"))                                         return "Hệ thống Phanh";
        if (ten.contains("quy") || ten.contains("bugi")
                || ten.contains("đèn") || ten.contains("mobin")
                || ten.contains("điện"))                                   return "Điện & Ắc quy";
        if (ten.contains("giảm xóc") || ten.contains("rô-tuyn")
                || ten.contains("gầm") || ten.contains("thước lái"))      return "Khung gầm & Giảm xóc";
        if (ten.contains("bơm") || ten.contains("curoa")
                || ten.contains("động cơ") || ten.contains("nhiên liệu")) return "Động cơ & Nhiên liệu";
        if (ten.contains("lốp") || ten.contains("gạt mưa")
                || ten.contains("vỏ"))                                     return "Lốp & Phụ kiện";
        return "Khác";
    }

    // ─────────────────────────────────────────────────────────────────────
    // LOGIC NGHIỆP VỤ
    // ─────────────────────────────────────────────────────────────────────
    private void boLoc() {
        txtTimKiem.setText("");
        cboLocLoai.setSelectedIndex(0);
        cboTonKho.setSelectedIndex(0);
        napBang();
    }

    private void napBang() {
        String tuKhoa   = txtTimKiem != null ? txtTimKiem.getText().trim().toLowerCase() : "";
        String loaiChon = cboLocLoai != null ? String.valueOf(cboLocLoai.getSelectedItem()) : "Tất cả phân loại";
        String tonChon  = cboTonKho  != null ? String.valueOf(cboTonKho.getSelectedItem())  : "Tất cả mức tồn";

        model.setRowCount(0);
        int dem = 0;
        int tong = kho.getPhuTungs().size();

        for (PhuTung pt : kho.getPhuTungs()) {
            // 1. Lọc từ khóa
            if (!tuKhoa.isEmpty()) {
                String ma  = pt.getMaPT() != null ? pt.getMaPT().toLowerCase() : "";
                String ten = pt.getTen()  != null ? pt.getTen().toLowerCase()  : "";
                if (!ma.contains(tuKhoa) && !ten.contains(tuKhoa)) continue;
            }
            // 2. Lọc phân loại
            if (!"Tất cả phân loại".equals(loaiChon)) {
                if (!phanLoaiPhuTung(pt).equalsIgnoreCase(loaiChon)) continue;
            }
            // 3. Lọc mức tồn
            if ("Còn nhiều (> 20)".equals(tonChon)       && pt.getSoLuongTon() <= 20) continue;
            if ("Trung bình (10 - 20)".equals(tonChon)
                    && (pt.getSoLuongTon() < 10 || pt.getSoLuongTon() > 20))          continue;
            if ("Sắp hết (< 10)".equals(tonChon)         && pt.getSoLuongTon() >= 10) continue;

            dem++;
            String tenNCC = pt.getTenNCC() != null && !pt.getTenNCC().trim().isEmpty()
                    ? pt.getTenNCC() : (pt.getMaNCC() != null ? pt.getMaNCC() : "—");
            model.addRow(new Object[]{
                    pt.getMaPT(),
                    pt.getTen(),
                    phanLoaiPhuTung(pt),
                    tenNCC,
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

        try {
            PhuTung pt = new PhuTung(
                    txtMa.getText().trim(),
                    txtTen.getText().trim(),
                    txtDonVi.getText().trim(),
                    0,
                    Long.parseLong(
                            txtGia.getText().trim().replace(",", "")
                    )
            );

            // Phân loại
            pt.setLoaiPT(
                    String.valueOf(cboLoaiForm.getSelectedItem())
            );

            // Nhà cung cấp
            String nccItem = (String) cboNCC.getSelectedItem();

            if (nccItem != null
                    && !nccItem.startsWith("--")
                    && nccItem.contains(" - ")) {

                pt.setMaNCC(nccItem.split(" - ", 2)[0].trim());

                pt.setTenNCC(
                        nccItem.split(" - ", 2)[1].trim()
                );
            } else {
                pt.setMaNCC(null);
                pt.setTenNCC(null);
            }

            // Lưu dữ liệu
            boolean ok = kho.themPhuTung(pt);

            if (ok) {
                napBang();
                lamMoi();

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm phụ tùng thành công!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể thêm phụ tùng vào CSDL!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đơn giá không hợp lệ!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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
        pt.setLoaiPT(String.valueOf(cboLoaiForm.getSelectedItem()));

        String nccItem = String.valueOf(cboNCC.getSelectedItem());
        if (nccItem != null && nccItem.contains(" - ") && !nccItem.startsWith("--")) {
            pt.setMaNCC(nccItem.split(" - ")[0].trim());
            pt.setTenNCC(nccItem.substring(nccItem.indexOf(" - ") + 3).trim());
        } else {
            pt.setMaNCC(null);
            pt.setTenNCC(null);
        }

        boolean ok = kho.capNhatPhuTung(pt);
        napBang();
        JOptionPane.showMessageDialog(this,
                ok ? "Cập nhật phụ tùng [" + ma + "] và đồng bộ CSDL SQL Server thành công!"
                   : "Cập nhật thành công!");
    }

    private void xoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng cần xóa trong danh sách!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ma  = table.getValueAt(dong, 0).toString();
        String ten = table.getValueAt(dong, 1).toString();
        if (JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phụ tùng [" + ma + "] " + ten + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;

        boolean ok = kho.xoaPhuTung(ma);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this,
                ok ? "Đã xóa phụ tùng khỏi CSDL SQL Server!"
                   : "Đã xóa phụ tùng!");
    }

    private void lamMoi() {
        txtMa.setText("");
        txtTen.setText("");
        txtDonVi.setText("");
        txtTon.setText("0");
        txtGia.setText("");
        cboLoaiForm.setSelectedIndex(0);
        cboNCC.setSelectedIndex(0);
        txtMa.setEditable(true);
        table.clearSelection();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;

        String ma = table.getValueAt(dong, 0).toString();
        PhuTung pt = kho.timPhuTung(ma);

        txtMa.setText(ma);
        txtTen.setText(table.getValueAt(dong, 1).toString());
        txtDonVi.setText(table.getValueAt(dong, 4).toString());
        txtTon.setText(table.getValueAt(dong, 5).toString());
        txtGia.setText(table.getValueAt(dong, 6).toString().replace(",", ""));
        txtMa.setEditable(false);

        // Chọn đúng phân loại trong combo
        String loai = table.getValueAt(dong, 2).toString();
        for (int i = 0; i < cboLoaiForm.getItemCount(); i++) {
            if (loai.equalsIgnoreCase(cboLoaiForm.getItemAt(i))) {
                cboLoaiForm.setSelectedIndex(i);
                break;
            }
        }

        // Chọn đúng NCC trong combo
        if (pt != null && pt.getMaNCC() != null && !pt.getMaNCC().isEmpty()) {
            for (int i = 0; i < cboNCC.getItemCount(); i++) {
                String item = cboNCC.getItemAt(i);
                if (item != null && item.startsWith(pt.getMaNCC())) {
                    cboNCC.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cboNCC.setSelectedIndex(0);
        }
    }

    private boolean kiemTra(boolean them) {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            UiHelper.loi(this, "Mã và tên phụ tùng không được để trống!", txtMa);
            return false;
        }
        if (!txtGia.getText().trim().replace(",", "").matches("[1-9]\\d*")) {
            UiHelper.loi(this, "Đơn giá phải là số nguyên dương!", txtGia);
            return false;
        }
        if (them && kho.timPhuTung(txtMa.getText().trim()) != null) {
            UiHelper.loi(this, "Mã phụ tùng đã tồn tại trong hệ thống!", txtMa);
            return false;
        }
        return true;
    }
}
