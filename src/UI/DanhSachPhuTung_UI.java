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
import java.util.Arrays;
import java.util.List;

/**
 * Màn hình quản lý danh sách phụ tùng trong kho.
 * Hỗ trợ tìm kiếm, lọc theo phân loại, kiểm tra ràng buộc dữ liệu và cập nhật thông tin.
 * Các combobox hỗ trợ gõ chữ tìm kiếm, danh sách sắp xếp theo bảng chữ cái hoặc số mã.
 */
public class DanhSachPhuTung_UI extends JPanel {

    // ── Các phân loại sắp xếp theo bảng chữ cái tiếng Việt ─────────────────
    static final String[] PHAN_LOAI_OPTIONS = {
            "Dầu nhớt & Dung dịch",
            "Điện & Ắc quy",
            "Động cơ & Nhiên liệu",
            "Hệ thống Phanh",
            "Khác",
            "Khung gầm & Giảm xóc",
            "Lọc & Bảo dưỡng",
            "Lốp & Phụ kiện"
    };

    private static final List<String> DS_TON_KHO = List.of(
            "Tất cả mức tồn",
            "Còn nhiều (> 20)",
            "Trung bình (10 - 20)",
            "Sắp hết (< 10)"
    );

    // ── Form nhập liệu bên trái ───────────────────────────────────────────
    private final JTextField        txtMa       = new JTextField();
    private final JTextField        txtTen      = new JTextField();
    private final JComboBox<String> cboLoaiForm = new JComboBox<>(PHAN_LOAI_OPTIONS);
    private final JTextField        txtDonVi    = new JTextField();
    private final JTextField        txtGia      = new JTextField();
    private final JTextField        txtTon      = new JTextField();

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
    private final JComboBox<String> cboTonKho  = new JComboBox<>(DS_TON_KHO.toArray(new String[0]));
    private final JLabel lblThongKe = new JLabel("Hiển thị: 0 phụ tùng");

    private final KhoDuLieu kho = KhoDuLieu.get();

    public DanhSachPhuTung_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý phụ tùng"), BorderLayout.NORTH);

        napLocLoai();

        // Ràng buộc phím: Đơn giá và Tồn kho chỉ cho phép nhập số, khi gõ chữ sẽ không hiển thị
        UiHelper.chiChoNhapSo(txtGia, 12);
        UiHelper.chiChoNhapSo(txtTon, 8);

        // Gắn tìm kiếm khi gõ chữ cho các combobox
        UiHelper.ganLocTimKiemChoComboBox(cboLoaiForm, Arrays.asList(PHAN_LOAI_OPTIONS), null);
        UiHelper.ganLocTimKiemChoComboBox(cboTonKho, DS_TON_KHO, this::napBang);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0.32);
        split.setDividerLocation(390);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);

        lamMoi();
        napBang();
    }

    /** Khởi tạo combobox lọc phân loại (gồm "Tất cả" + các loại cố định sắp xếp A-Z). */
    private void napLocLoai() {
        List<String> dsLoc = new ArrayList<>();
        dsLoc.add("Tất cả phân loại");
        dsLoc.addAll(Arrays.asList(PHAN_LOAI_OPTIONS));
        cboLocLoai.setModel(new DefaultComboBoxModel<>(dsLoc.toArray(new String[0])));
        UiHelper.ganLocTimKiemChoComboBox(cboLocLoai, dsLoc, this::napBang);
    }

    // ─────────────────────────────────────────────────────────────────────
    // XÂY DỰNG FORM TRÁI
    // ─────────────────────────────────────────────────────────────────────
    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin phụ tùng", 390);
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
        UiHelper.themDong(form, 3, "Đơn vị tính *", txtDonVi);
        // Đơn giá
        UiHelper.themDong(form, 4, "Đơn giá (VNĐ) *", txtGia);

        // Số lượng tồn kho (chỉ đọc, quản lý qua phiếu nhập / xuất kho)
        txtTon.setText("0");
        txtTon.setEditable(false);
        txtTon.setBackground(new Color(241, 245, 249));
        UiHelper.themDong(form, 5, "Số lượng tồn kho", txtTon);

        // Nút hành động
        JPanel nut = new JPanel(new GridLayout(1, 4, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));
        nut.add(UiHelper.nut("Thêm",    UiHelper.XANH_LA,    e -> them()));
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
        JPanel bangPanel = UiHelper.panelBang("Danh sách phụ tùng & linh kiện");

        bangPanel.add(taoThanhLoc(), BorderLayout.NORTH);

        // Cột: Mã PT | Tên phụ tùng | Phân loại | ĐVT | Tồn kho | Đơn giá (đ) (Đã bỏ cột Nhà cung cấp)
        model = UiHelper.model("Mã PT", "Tên phụ tùng", "Phân loại", "ĐVT", "Tồn kho", "Đơn giá (đ)");
        table = UiHelper.bang(model);
        table.setAutoCreateRowSorter(true); // Hỗ trợ click tiêu đề cột để sắp xếp trực tiếp theo A-Z hoặc số
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Mã PT
        table.getColumnModel().getColumn(1).setPreferredWidth(210);  // Tên
        table.getColumnModel().getColumn(2).setPreferredWidth(145);  // Phân loại
        table.getColumnModel().getColumn(3).setPreferredWidth(65);   // ĐVT
        table.getColumnModel().getColumn(4).setPreferredWidth(75);   // Tồn kho
        table.getColumnModel().getColumn(5).setPreferredWidth(115);  // Đơn giá

        UiHelper.canGiua(table, 0, 3);
        UiHelper.canTrai(table, 1, 2);
        UiHelper.canPhai(table, 5);

        // Renderer màu cho cột Tồn kho (cột 4)
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

        JPanel row1 = new JPanel(new GridBagLayout());
        row1.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);

        // Ô tìm kiếm
        gbc.gridx = 0; gbc.weightx = 0.40;
        txtTimKiem.setPreferredSize(new Dimension(0, 34));
        UiHelper.trangTriInput(txtTimKiem);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { napBang(); }
            @Override public void removeUpdate(DocumentEvent e) { napBang(); }
            @Override public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        row1.add(txtTimKiem, gbc);

        // ComboBox Phân loại
        gbc.gridx = 1; gbc.weightx = 0.28;
        cboLocLoai.setPreferredSize(new Dimension(0, 34));
        cboLocLoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row1.add(cboLocLoai, gbc);

        // ComboBox Mức tồn kho
        gbc.gridx = 2; gbc.weightx = 0.20;
        cboTonKho.setPreferredSize(new Dimension(0, 34));
        cboTonKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        row1.add(cboTonKho, gbc);

        // Nút Bỏ lọc
        gbc.gridx = 3; gbc.weightx = 0.12; gbc.insets = new Insets(0, 0, 0, 0);
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
        String tuKhoaKhongDau = UiHelper.boDauTiengViet(tuKhoa);
        String loaiChon = cboLocLoai != null && cboLocLoai.getSelectedItem() != null ? String.valueOf(cboLocLoai.getSelectedItem()) : "Tất cả phân loại";
        String tonChon  = cboTonKho  != null && cboTonKho.getSelectedItem() != null  ? String.valueOf(cboTonKho.getSelectedItem())  : "Tất cả mức tồn";

        model.setRowCount(0);
        int dem = 0;
        int tong = 0;

        List<PhuTung> listLoc = new ArrayList<>();

        for (PhuTung pt : kho.getPhuTungs()) {
            // Bỏ qua các mã dịch vụ bảo hành ảo DVxxx
            if (pt.getMaPT() != null && pt.getMaPT().startsWith("DV")) {
                continue;
            }
            tong++;

            // 1. Lọc từ khóa
            if (!tuKhoa.isEmpty()) {
                String ma  = pt.getMaPT() != null ? pt.getMaPT().toLowerCase() : "";
                String ten = pt.getTen()  != null ? pt.getTen().toLowerCase()  : "";
                String tenKhongDau = UiHelper.boDauTiengViet(ten);
                if (!ma.contains(tuKhoa) && !ten.contains(tuKhoa) && !tenKhongDau.contains(tuKhoaKhongDau)) {
                    continue;
                }
            }
            // 2. Lọc phân loại
            if (!"Tất cả phân loại".equals(loaiChon) && !loaiChon.startsWith("--")) {
                if (!phanLoaiPhuTung(pt).equalsIgnoreCase(loaiChon)) continue;
            }
            // 3. Lọc mức tồn
            if ("Còn nhiều (> 20)".equals(tonChon)       && pt.getSoLuongTon() <= 20) continue;
            if ("Trung bình (10 - 20)".equals(tonChon)
                    && (pt.getSoLuongTon() < 10 || pt.getSoLuongTon() > 20))          continue;
            if ("Sắp hết (< 10)".equals(tonChon)         && pt.getSoLuongTon() >= 10) continue;

            listLoc.add(pt);
        }

        // Sắp xếp danh sách phụ tùng theo số mã tăng dần (PT001, PT002, ..., PT101...)
        listLoc.sort((a, b) -> BaoCaoTonKho_UI.soSanhMa(a.getMaPT(), b.getMaPT()));

        for (PhuTung pt : listLoc) {
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

    private String sinhMaMoi() {
        int max = 0;
        for (PhuTung pt : kho.getPhuTungs()) {
            if (pt.getMaPT() != null && pt.getMaPT().startsWith("PT")) {
                try {
                    int num = Integer.parseInt(pt.getMaPT().replaceAll("[^0-9]", ""));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return String.format("PT%03d", max + 1);
    }

    private void them() {
        if (!kiemTra(true)) return;

        try {
            long donGia = Long.parseLong(txtGia.getText().trim().replaceAll("[^0-9]", ""));
            PhuTung pt = new PhuTung(
                    txtMa.getText().trim().toUpperCase(),
                    txtTen.getText().trim(),
                    txtDonVi.getText().trim(),
                    0,
                    donGia
            );

            // Phân loại
            pt.setLoaiPT(String.valueOf(cboLoaiForm.getSelectedItem()));
            pt.setMaNCC(null);
            pt.setTenNCC(null);

            // Lưu dữ liệu
            boolean ok = kho.themPhuTung(pt);

            if (ok) {
                napBang();
                lamMoi();
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm phụ tùng [" + pt.getMaPT() + "] thành công!",
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

        int modelRow = table.convertRowIndexToModel(dong);
        String ma = model.getValueAt(modelRow, 0).toString();
        PhuTung pt = kho.timPhuTung(ma);
        if (pt == null) return;

        pt.setTen(txtTen.getText().trim());
        pt.setDonVi(txtDonVi.getText().trim());
        pt.setDonGia(Long.parseLong(txtGia.getText().trim().replaceAll("[^0-9]", "")));
        pt.setLoaiPT(String.valueOf(cboLoaiForm.getSelectedItem()));
        pt.setMaNCC(null);
        pt.setTenNCC(null);

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
        int modelRow = table.convertRowIndexToModel(dong);
        String ma  = model.getValueAt(modelRow, 0).toString();
        String ten = model.getValueAt(modelRow, 1).toString();
        if (JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa phụ tùng [" + ma + "] " + ten + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;

        boolean ok = kho.xoaPhuTung(ma);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this,
                ok ? "Đã xóa phụ tùng [" + ma + "] khỏi CSDL SQL Server!"
                   : "Đã xóa phụ tùng!");
    }

    private void lamMoi() {
        txtMa.setText(sinhMaMoi());
        txtTen.setText("");
        txtDonVi.setText("");
        txtTon.setText("0");
        txtGia.setText("");
        cboLoaiForm.setSelectedIndex(0);
        txtMa.setEditable(true);
        table.clearSelection();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;

        int modelRow = table.convertRowIndexToModel(dong);
        String ma = model.getValueAt(modelRow, 0).toString();

        txtMa.setText(ma);
        txtTen.setText(model.getValueAt(modelRow, 1).toString());
        txtDonVi.setText(model.getValueAt(modelRow, 3).toString());
        txtTon.setText(model.getValueAt(modelRow, 4).toString());
        txtGia.setText(model.getValueAt(modelRow, 5).toString().replaceAll("[^0-9]", ""));
        txtMa.setEditable(false);

        // Chọn đúng phân loại trong combo
        String loai = model.getValueAt(modelRow, 2).toString();
        for (int i = 0; i < cboLoaiForm.getItemCount(); i++) {
            if (loai.equalsIgnoreCase(cboLoaiForm.getItemAt(i))) {
                cboLoaiForm.setSelectedIndex(i);
                break;
            }
        }
    }

    private boolean kiemTra(boolean them) {
        String ma = txtMa.getText().trim().toUpperCase();
        txtMa.setText(ma);
        String ten = txtTen.getText().trim();
        txtTen.setText(ten);
        String donVi = txtDonVi.getText().trim();
        txtDonVi.setText(donVi);
        String giaStr = txtGia.getText().trim().replaceAll("[^0-9]", "");

        // 1. Ràng buộc Mã phụ tùng
        if (ma.isEmpty()) {
            UiHelper.loi(this, "Mã phụ tùng không được để trống!", txtMa);
            return false;
        }
        if (!ma.matches("^PT\\d{3,}$")) {
            UiHelper.loi(this, "Mã phụ tùng không đúng định dạng!\nMã phụ tùng phải bắt đầu bằng \"PT\" theo sau là ít nhất 3 chữ số (Ví dụ: PT001, PT101).", txtMa);
            return false;
        }
        if (them && kho.timPhuTung(ma) != null) {
            UiHelper.loi(this, "Mã phụ tùng \"" + ma + "\" đã tồn tại trong hệ thống!", txtMa);
            return false;
        }

        // 2. Ràng buộc Tên phụ tùng
        if (ten.isEmpty()) {
            UiHelper.loi(this, "Tên phụ tùng không được để trống!", txtTen);
            return false;
        }
        if (ten.length() < 2) {
            UiHelper.loi(this, "Tên phụ tùng phải có ít nhất 2 ký tự!", txtTen);
            return false;
        }
        if (!ten.matches("^[\\p{Lu}\\d][\\p{L}\\p{N}\\s/.,%()_+-]*$")) {
            UiHelper.loi(this, "Tên phụ tùng không hợp lệ!\nTên phụ tùng phải bắt đầu bằng chữ cái in hoa hoặc số (Ví dụ: Lọc dầu động cơ, Bugi iridium, Ắc quy 12V 60Ah, Lốp 205/55R16)!", txtTen);
            return false;
        }

        // 3. Ràng buộc Phân loại
        if (cboLoaiForm.getSelectedItem() == null || String.valueOf(cboLoaiForm.getSelectedItem()).trim().isEmpty()) {
            UiHelper.loi(this, "Vui lòng chọn phân loại phụ tùng!", cboLoaiForm);
            return false;
        }

        // 4. Ràng buộc Đơn vị tính
        if (donVi.isEmpty()) {
            UiHelper.loi(this, "Đơn vị tính không được để trống!", txtDonVi);
            return false;
        }
        if (!donVi.matches("^[\\p{Lu}][\\p{Ll}\\s]*$")) {
            UiHelper.loi(this, "Đơn vị tính không hợp lệ!\nĐơn vị tính phải bắt đầu bằng chữ hoa và chỉ chứa các chữ cái (Ví dụ: Cái, Chiếc, Bộ, Bình, Lít, Chai, Hộp)!", txtDonVi);
            return false;
        }

        // 5. Ràng buộc Đơn giá
        if (giaStr.isEmpty()) {
            UiHelper.loi(this, "Đơn giá không được để trống!", txtGia);
            return false;
        }
        if (!giaStr.matches("^[1-9]\\d*$")) {
            UiHelper.loi(this, "Đơn giá phải là số nguyên dương lớn hơn 0!", txtGia);
            return false;
        }
        try {
            long gia = Long.parseLong(giaStr);
            if (gia < 1000) {
                UiHelper.loi(this, "Đơn giá phải từ 1.000 VNĐ trở lên!", txtGia);
                return false;
            }
        } catch (NumberFormatException e) {
            UiHelper.loi(this, "Đơn giá vượt quá giới hạn cho phép!", txtGia);
            return false;
        }

        return true;
    }
}
