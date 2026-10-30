package UI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

import DAO.HoaDonBanHang_DAO;
import DAO.PhieuBaoHanh_DAO.ChiTiet;
import Entity.KhachHang;
import Entity.NhanVien;
import Entity.PhuTung;

import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HoaDonBanHang_UI extends JPanel {

    // ================= MÀU SẮC =================
    private final Color NAVY = new Color(24, 43, 75);
    private final Color BLUE = new Color(42, 111, 219);
    private final Color BG = new Color(245, 247, 251);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(45, 55, 72);

    private final DecimalFormat df = new DecimalFormat("#,##0");

    // ================= COMPONENT =================
    private JTextField txtMaHD, txtNgayLap, txtNhanVien;
    private JTextField txtSDT, txtEmail, txtDiaChi;
    private JTextField txtGiamGia, txtKhachDua;
    private JTextField txtTongTien, txtThanhToan, txtTienThua;

    private JComboBox<KhachHang> cbKhachHang;
    private JComboBox<PhuTung> cbPhuTung;
    private JComboBox<String> cbHinhThuc;

    private JSpinner spSoLuong;

    private JTable table;
    private DefaultTableModel model;

    // ================= DỮ LIỆU =================
    private final NhanVien nhanVienHienTai;
    private final HoaDonBanHang_DAO dao = new HoaDonBanHang_DAO();

    public HoaDonBanHang_UI(NhanVien nv) {
        this.nhanVienHienTai = nv;
        initUI();
        loadKhachHang();
        loadPhuTung();
    }

    public HoaDonBanHang_UI() {
        this(null);
    }

    // ================= TẢI DỮ LIỆU TỪ DAO =================
    private void loadKhachHang() {
        try {
            cbKhachHang.removeAllItems();
            for (KhachHang kh : dao.layDanhSachKhachHang()) {
                cbKhachHang.addItem(kh);
            }
        } catch (SQLException e) {
            baoLoi("Không tải được khách hàng: " + e.getMessage());
        }
    }

    private void loadPhuTung() {
        try {
            cbPhuTung.removeAllItems();
            for (PhuTung pt : dao.layDanhSachPhuTung()) {
                cbPhuTung.addItem(pt);
            }
        } catch (SQLException e) {
            baoLoi("Không tải được phụ tùng: " + e.getMessage());
        }
    }

    private void baoLoi(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ================= GIAO DIỆN CHÍNH =================
    private void initUI() {
        setLayout(new BorderLayout());

    

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(20, 25, 20, 25));

        main.add(createHeader(), BorderLayout.NORTH);
        main.add(createContent(), BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    // ================= SIDEBAR =================
    

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("LẬP HÓA ĐƠN BÁN HÀNG");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Quản lý và tạo hóa đơn bán phụ tùng cho khách hàng");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        JLabel user = new JLabel("Nhân viên: " + tenNhanVien());
        user.setFont(new Font("Arial", Font.BOLD, 13));
        user.setForeground(NAVY);
        header.add(user, BorderLayout.EAST);

        return header;
    }

    private String tenNhanVien() {
        return nhanVienHienTai != null ? nhanVienHienTai.getHoTen() : "";
    }

    // ================= CONTENT =================
    private JPanel createContent() {
        JPanel top = new JPanel(new GridLayout(1, 2, 12, 0));
        top.setOpaque(false);
        top.add(createInvoiceInfo());
        top.add(createCustomerInfo());

        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(createProductPanel(), BorderLayout.CENTER);
        body.add(createPaymentPanel(), BorderLayout.SOUTH);

        return body;
    }

    // ================= THÔNG TIN HÓA ĐƠN =================
    private JPanel createInvoiceInfo() {
        JPanel panel = createCard("THÔNG TIN HÓA ĐƠN");

        txtMaHD = createTextField();
        txtMaHD.setText(taoMaHD());

        txtNgayLap = createTextField();
        txtNgayLap.setText(java.time.LocalDate.now().toString());
        txtNgayLap.setEditable(false);

        txtNhanVien = createTextField();
        txtNhanVien.setText(tenNhanVien());
        txtNhanVien.setEditable(false);

        cbHinhThuc = new JComboBox<>(new String[] {
            "Tiền mặt", "Chuyển khoản", "Thẻ"
        });
        cbHinhThuc.setFont(new Font("Arial", Font.PLAIN, 13));

        addFormRow(panel, "Mã hóa đơn:", txtMaHD);
        addFormRow(panel, "Ngày lập:", txtNgayLap);
        addFormRow(panel, "Nhân viên:", txtNhanVien);
        addFormRow(panel, "Thanh toán:", cbHinhThuc);

        return panel;
    }

    private String taoMaHD() {
        return "HD" + System.currentTimeMillis() % 100000;
    }

    // ================= THÔNG TIN KHÁCH HÀNG =================
    private JPanel createCustomerInfo() {
        JPanel panel = createCard("THÔNG TIN KHÁCH HÀNG");

        cbKhachHang = new JComboBox<>();
        cbKhachHang.setFont(new Font("Arial", Font.PLAIN, 13));
        cbKhachHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof KhachHang) {
                    KhachHang kh = (KhachHang) value;
                    setText(kh.getMaKH() + " - " + kh.getHoTen());
                }
                return this;
            }
        });

        txtSDT = createTextField();
        txtEmail = createTextField();
        txtDiaChi = createTextField();
        txtSDT.setEditable(false);
        txtEmail.setEditable(false);
        txtDiaChi.setEditable(false);

        addFormRow(panel, "Khách hàng:", cbKhachHang);
        addFormRow(panel, "Số điện thoại:", txtSDT);
        addFormRow(panel, "Email:", txtEmail);
        addFormRow(panel, "Địa chỉ:", txtDiaChi);

        cbKhachHang.addActionListener(e -> {
            KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
            txtSDT.setText(kh == null ? "" : kh.getSdt());
            txtEmail.setText(kh == null ? "" : kh.getEmail());
            txtDiaChi.setText(kh == null ? "" : kh.getDiaChi());
        });

        return panel;
    }

    // ================= DANH SÁCH PHỤ TÙNG =================
    private JPanel createProductPanel() {
        JPanel panel = createCard("DANH SÁCH PHỤ TÙNG BÁN");

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        addPanel.setBackground(WHITE);

        cbPhuTung = new JComboBox<>();
        cbPhuTung.setPreferredSize(new Dimension(300, 35));
        cbPhuTung.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof PhuTung) {
                    PhuTung pt = (PhuTung) value;
                    setText(pt.getMaPT() + " - " + pt.getTen()
                            + " (" + df.format(pt.getDonGia()) + " VNĐ)");
                }
                return this;
            }
        });

        spSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spSoLuong.setPreferredSize(new Dimension(80, 35));

        JButton btnThem = createButton("+ Thêm phụ tùng", BLUE);
        btnThem.addActionListener(e -> themPhuTung());

        JButton btnXoa = createButton("Xóa dòng", new Color(220, 70, 70));
        btnXoa.addActionListener(e -> xoaPhuTung());

        addPanel.add(new JLabel("Phụ tùng:"));
        addPanel.add(cbPhuTung);
        addPanel.add(new JLabel("Số lượng:"));
        addPanel.add(spSoLuong);
        addPanel.add(btnThem);
        addPanel.add(btnXoa);

        String[] columns = {
            "Mã phụ tùng", "Tên phụ tùng", "Đơn vị",
            "Số lượng", "Đơn giá", "Thành tiền"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(230, 237, 247));
        table.getTableHeader().setForeground(NAVY);
        table.setSelectionBackground(new Color(215, 230, 250));
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 238)));

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(WHITE);
        body.add(addPanel, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);

        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    // ================= THANH TOÁN =================
    private JPanel createPaymentPanel() {
        JPanel panel = createCard("THÔNG TIN THANH TOÁN");

        JPanel left = new JPanel(new GridLayout(2, 2, 10, 8));
        left.setBackground(WHITE);

        txtGiamGia = createTextField();
        txtGiamGia.setText("0");
        txtKhachDua = createTextField();
        txtKhachDua.setText("0");

        left.add(new JLabel("Giảm giá (VNĐ):"));
        left.add(txtGiamGia);
        left.add(new JLabel("Khách đưa (VNĐ):"));
        left.add(txtKhachDua);

        txtTongTien = createTextField();
        txtTongTien.setText("0");
        txtTongTien.setEditable(false);

        txtThanhToan = createTextField();
        txtThanhToan.setText("0");
        txtThanhToan.setEditable(false);

        txtTienThua = createTextField();
        txtTienThua.setText("0");
        txtTienThua.setEditable(false);

        JPanel right = new JPanel(new GridLayout(3, 2, 10, 7));
        right.setBackground(WHITE);
        right.add(new JLabel("Tổng tiền hàng:"));
        right.add(txtTongTien);
        right.add(new JLabel("Tiền thanh toán:"));
        right.add(txtThanhToan);
        right.add(new JLabel("Tiền thừa:"));
        right.add(txtTienThua);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(WHITE);
        footer.add(left, BorderLayout.CENTER);
        footer.add(right, BorderLayout.EAST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttons.setBackground(WHITE);

        JButton btnLamMoi = createButton("Làm mới", new Color(120, 130, 145));
        JButton btnLuu = createButton("Lưu hóa đơn", BLUE);
        JButton btnIn = createButton("In hóa đơn", new Color(20, 150, 110));

        btnLamMoi.addActionListener(e -> lamMoi());
        btnLuu.addActionListener(e -> luuHoaDon());
        btnIn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chức năng in hóa đơn sẽ được tích hợp sau."));

        buttons.add(btnLamMoi);
        buttons.add(btnLuu);
        buttons.add(btnIn);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setBackground(WHITE);
        body.add(footer, BorderLayout.CENTER);
        body.add(buttons, BorderLayout.SOUTH);

        panel.add(body, BorderLayout.CENTER);

        txtGiamGia.addActionListener(e -> tinhTien());
        txtKhachDua.addActionListener(e -> tinhTien());

        return panel;
    }

    // ================= THÊM PHỤ TÙNG =================
    private void themPhuTung() {
        PhuTung pt = (PhuTung) cbPhuTung.getSelectedItem();
        if (pt == null) return;

        int soLuong = (Integer) spSoLuong.getValue();

        if (soLuong > pt.getSoLuongTon()) {
            JOptionPane.showMessageDialog(this,
                "Số lượng tồn kho không đủ!\nSố lượng còn: " + pt.getSoLuongTon(),
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Nếu phụ tùng đã có trong bảng thì cộng dồn
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(pt.getMaPT())) {
                int slMoi = Integer.parseInt(model.getValueAt(i, 3).toString())
                        + soLuong;

                if (slMoi > pt.getSoLuongTon()) {
                    JOptionPane.showMessageDialog(this,
                            "Số lượng vượt quá tồn kho!");
                    return;
                }

                model.setValueAt(slMoi, i, 3);
                model.setValueAt(df.format((double) slMoi * pt.getDonGia()), i, 5);
                tinhTien();
                return;
            }
        }

        model.addRow(new Object[] {
            pt.getMaPT(),
            pt.getTen(),
            pt.getDonVi(),
            soLuong,
            df.format(pt.getDonGia()),
            df.format((double) soLuong * pt.getDonGia())
        });

        tinhTien();
    }

    // ================= XÓA PHỤ TÙNG =================
    private void xoaPhuTung() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phụ tùng cần xóa!");
            return;
        }

        model.removeRow(row);
        tinhTien();
    }

    // ================= TÍNH TIỀN =================
    private void tinhTien() {
        double tongTien = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            tongTien += parseMoney(model.getValueAt(i, 5).toString());
        }

        double giamGia = parseMoney(txtGiamGia.getText());
        double khachDua = parseMoney(txtKhachDua.getText());

        double thanhToan = Math.max(0, tongTien - giamGia);
        double tienThua = Math.max(0, khachDua - thanhToan);

        txtTongTien.setText(df.format(tongTien));
        txtThanhToan.setText(df.format(thanhToan));
        txtTienThua.setText(df.format(tienThua));
    }

    private double parseMoney(String value) {
        try {
            return Double.parseDouble(value.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // ================= LƯU HÓA ĐƠN =================
    private void luuHoaDon() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hóa đơn chưa có phụ tùng!");
            return;
        }

        tinhTien(); // cập nhật số liệu mới nhất trước khi kiểm tra

        double giamGia = parseMoney(txtGiamGia.getText());
        double khachDua = parseMoney(txtKhachDua.getText());
        double thanhToan = parseMoney(txtThanhToan.getText());

        if (giamGia < 0 || giamGia > parseMoney(txtTongTien.getText())) {
            JOptionPane.showMessageDialog(this, "Giảm giá không hợp lệ!");
            return;
        }

        if (khachDua < thanhToan) {
            JOptionPane.showMessageDialog(this,
                    "Khách đưa chưa đủ tiền thanh toán!");
            return;
        }

        KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
        if (kh == null) {
            baoLoi("Chưa chọn khách hàng!");
            return;
        }
        if (nhanVienHienTai == null) {
            baoLoi("Chưa xác định nhân viên lập hóa đơn!");
            return;
        }

        String maHD = txtMaHD.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn lưu hóa đơn " + maHD + " không?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;
        Map<String, Integer> chiTiet = new LinkedHashMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String maPT = model.getValueAt(i, 0).toString();
            int soLuong = Integer.parseInt(model.getValueAt(i, 3).toString());
            chiTiet.merge(maPT, soLuong, Integer::sum);
        }

        try {
            if (dao.kiemTraMaHD(maHD)) {
                baoLoi("Mã hóa đơn đã tồn tại!");
                return;
            }

            dao.lapHoaDonBanHang(
                maHD,
                kh.getMaKH(),
                nhanVienHienTai.getMaNV(),
                (String) cbHinhThuc.getSelectedItem(),
                giamGia,
                null,
                chiTiet
            );

            JOptionPane.showMessageDialog(this,
                "Lưu hóa đơn " + maHD + " thành công!\nTổng thanh toán: "
                + txtThanhToan.getText() + " VNĐ",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            resetForm();
            loadPhuTung(); // cập nhật lại tồn kho

        } catch (IllegalArgumentException | SQLException ex) {
            baoLoi(ex.getMessage());
        }
    }

    // ================= LÀM MỚI =================
    private void lamMoi() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn làm mới hóa đơn hiện tại?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            resetForm();
        }
    }

    private void resetForm() {
        model.setRowCount(0);
        txtGiamGia.setText("0");
        txtKhachDua.setText("0");
        txtMaHD.setText(taoMaHD());
        if (cbKhachHang.getItemCount() > 0) cbKhachHang.setSelectedIndex(0);
        cbHinhThuc.setSelectedIndex(0);
        spSoLuong.setValue(1);
        tinhTien();
    }

    // ================= HÀM TẠO GIAO DIỆN =================
    private JPanel createCard(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        panel.setBackground(WHITE);
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 230, 238)),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(NAVY);
        panel.add(lbl, BorderLayout.NORTH);

        return panel;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Arial", Font.PLAIN, 13));
        txt.setPreferredSize(new Dimension(150, 32));
        txt.setBorder(new CompoundBorder(
            new LineBorder(new Color(210, 218, 230)),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return txt;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 15, 9, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /*
     * Các dòng form được xếp dọc trong một panel "body" riêng,
     * tránh việc mỗi dòng ghi đè lên BorderLayout.CENTER.
     */
    private void addFormRow(JPanel card, String label, JComponent component) {
        JPanel body = (JPanel) card.getClientProperty("formBody");

        if (body == null) {
            body = new JPanel(new GridLayout(0, 1, 0, 8));
            body.setBackground(WHITE);
            card.putClientProperty("formBody", body);
            card.add(body, BorderLayout.CENTER);
        }

        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setBackground(WHITE);

        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(105, 30));
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));

        row.add(lbl, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);

        body.add(row);
    }

    // ================= MAIN (chạy thử) =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("GARA Ô TÔ - Lập hóa đơn bán hàng");
            f.setContentPane(new HoaDonBanHang_UI());
            f.setSize(1350, 820);
            f.setMinimumSize(new Dimension(1100, 700));
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
}