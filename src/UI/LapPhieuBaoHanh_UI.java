
package UI;

import DAO.PhieuBaoHanh_DAO;
import DAO.PhieuBaoHanh_DAO.LuaChon;
import DAO.PhieuBaoHanh_DAO.ThongTinIn;
import Entity.NhanVien;
import ConnectDB.ConnectDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LapPhieuBaoHanh_UI extends JPanel {

    private final NhanVien nhanVien;
    private final PhieuBaoHanh_DAO dao = new PhieuBaoHanh_DAO();

    private final Color NAVY = new Color(23, 42, 70);
    private final Color BLUE = new Color(53, 91, 170);
    private final Color GREEN = new Color(34, 139, 91);
    private final Color BG = new Color(245, 247, 252);
    private final Color BORDER = new Color(215, 222, 234);

    private final JTextField txtMaPBH = new JTextField();
    private final JTextField txtNgayLap = new JTextField();
    private final JTextField txtNgayBatDau = new JTextField();

    private final JComboBox<LuaChon> cboNhanVien = new JComboBox<>();
    private final JTextField txtKhachHang = new JTextField();
    private final JTextField txtBienSoXe = new JTextField();
    private final JTextField txtTongTien = new JTextField();

    private final JComboBox<LuaChon> cboHoaDon = new JComboBox<>();

    private final JComboBox<String> cboTrangThai =
            new JComboBox<>(new String[]{
                    "Còn hạn", "Hết hạn", "Tạm ngưng", "Đã hủy"
            });

    private final JTextArea txtGhiChu = new JTextArea(2, 20);

    private final JTextField txtTenHangMuc = new JTextField();
    private final JSpinner spThoiHan =
            new JSpinner(new SpinnerNumberModel(6, 1, 120, 1));
    private final JTextField txtDieuKien = new JTextField();
    private final JTextField txtGhiChuCT = new JTextField();

    private DefaultTableModel model;
    private JTable tblChiTiet;

    private boolean dangNap = false;

    public LapPhieuBaoHanh_UI(NhanVien nhanVien) {
        this.nhanVien = nhanVien;

        setLayout(new BorderLayout(0, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(taoHeader(), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);

        moKhoaInput();

        cboHoaDon.addActionListener(e -> {
            if (!dangNap) {
                napThongTinHoaDon();
            }
        });

        napDuLieu();
    }

    // ================= HEADER =================

    private JPanel taoHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 4));
        labels.setOpaque(false);

        JLabel title = new JLabel("LẬP PHIẾU BẢO HÀNH");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(NAVY);

        JLabel sub = new JLabel(
                "Lập phiếu bảo hành theo hóa đơn sửa chữa"
        );
        sub.setForeground(Color.GRAY);

        labels.add(title);
        labels.add(sub);
        p.add(labels, BorderLayout.CENTER);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 6, 0)
        );
        actions.setOpaque(false);

        JButton btnMoi = nut("Làm mới", BLUE);
        JButton btnLuu = nut("Lưu phiếu", GREEN);
        JButton btnIn = nut("In phiếu", new Color(174, 105, 26));

        btnMoi.addActionListener(e -> lamMoi());
        btnLuu.addActionListener(e -> luuPhieu());
        btnIn.addActionListener(e -> inPhieu());

        actions.add(btnMoi);
        actions.add(btnLuu);
        actions.add(btnIn);

        p.add(actions, BorderLayout.EAST);
        return p;
    }

    // ================= FORM =================

    private JPanel taoNoiDung() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 10, 0));
        top.setOpaque(false);
        top.add(taoThongTin());
        top.add(taoThongTinHoaDon());

        content.add(top, BorderLayout.NORTH);
        content.add(taoChiTiet(), BorderLayout.CENTER);

        return content;
    }

    private JPanel taoThongTin() {
        JPanel card = card();
        card.add(tieuDe("1. THÔNG TIN PHIẾU BẢO HÀNH"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        themTruong(form, 0, "Mã phiếu bảo hành", txtMaPBH);
        themTruong(form, 1, "Ngày lập", txtNgayLap);
        themTruong(form, 2, "Ngày bắt đầu", txtNgayBatDau);
        themTruong(form, 3, "Nhân viên lập", cboNhanVien);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel taoThongTinHoaDon() {
        JPanel card = card();
        card.add(tieuDe("2. THÔNG TIN HÓA ĐƠN"),
                BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        themTruong(form, 0, "Chọn hóa đơn", cboHoaDon);
        themTruong(form, 1, "Khách hàng", txtKhachHang);
        themTruong(form, 2, "Biển số xe", txtBienSoXe);
        themTruong(form, 3, "Tổng tiền hóa đơn", txtTongTien);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel taoChiTiet() {
        JPanel card = card();
        card.add(tieuDe("3. CÁC HẠNG MỤC BẢO HÀNH"),
                BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{
                        "STT", "Tên hạng mục", "Thời hạn (tháng)",
                        "Ngày hết hạn", "Điều kiện bảo hành", "Ghi chú"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1 || col == 2
                        || col == 4 || col == 5;
            }
        };

        tblChiTiet = new JTable(model);
        tblChiTiet.setRowHeight(29);
        tblChiTiet.setGridColor(BORDER);
        tblChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblChiTiet.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        model.addTableModelListener(e -> {
            if (e.getType()
                    == javax.swing.event.TableModelEvent.UPDATE
                    && e.getColumn() == 2
                    && e.getFirstRow() >= 0) {
                tinhNgayHetHan(e.getFirstRow());
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        themTruong(form, 0, "Tên hạng mục", txtTenHangMuc);
        themTruong(form, 1, "Thời hạn (tháng)", spThoiHan);
        themTruong(form, 2, "Điều kiện", txtDieuKien);
        themTruong(form, 3, "Ghi chú hạng mục", txtGhiChuCT);

        JButton btnThem = nut("+ Thêm hạng mục", BLUE);
        JButton btnXoa = nut("Xóa dòng", new Color(180, 65, 65));

        btnThem.addActionListener(e -> themHangMuc());
        btnXoa.addActionListener(e -> xoaHangMuc());

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 6, 0)
        );
        actions.setOpaque(false);
        actions.add(btnThem);
        actions.add(btnXoa);

        JPanel tren = new JPanel(new BorderLayout(0, 6));
        tren.setOpaque(false);
        tren.add(form, BorderLayout.CENTER);
        tren.add(actions, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(tren, BorderLayout.NORTH);
        center.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);

        JPanel note = new JPanel(new BorderLayout(0, 4));
        note.setOpaque(false);
        note.add(new JLabel("Ghi chú chung"), BorderLayout.NORTH);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        note.add(new JScrollPane(txtGhiChu), BorderLayout.CENTER);

        center.add(note, BorderLayout.SOUTH);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    // ================= INPUT STATE =================

    private void moKhoaInput() {
        txtMaPBH.setEditable(false);
        txtNgayLap.setEditable(false);
        txtKhachHang.setEditable(false);
        txtBienSoXe.setEditable(false);
        txtTongTien.setEditable(false);

        txtNgayBatDau.setEditable(true);
        txtTenHangMuc.setEditable(true);
        txtDieuKien.setEditable(true);
        txtGhiChuCT.setEditable(true);
        txtGhiChu.setEditable(true);

        cboHoaDon.setEnabled(true);
        cboTrangThai.setEnabled(true);
        spThoiHan.setEnabled(true);
    }

    // ================= LOAD SQL DATA =================

    private void napDuLieu() {
        dangNap = true;

        try {
            txtMaPBH.setText(dao.taoMaPhieuMoi());
            txtNgayLap.setText(LocalDate.now().toString());
            txtNgayBatDau.setText(LocalDate.now().toString());
            napDanhSachNhanVien();

            if (nhanVien != null) {
                for (int i = 0; i < cboNhanVien.getItemCount(); i++) {
                    LuaChon nv = cboNhanVien.getItemAt(i);

                    if (nv.ma.equals(nhanVien.getMaNV())) {
                        cboNhanVien.setSelectedIndex(i);
                        break;
                    }
                }
            }

            cboHoaDon.removeAllItems();

            for (LuaChon x : dao.layDanhSachHoaDon()) {
                // Mỗi hóa đơn đã được cấp phiếu sẽ không xuất hiện
                // trong danh sách tạo mới.
                if (!dao.hoaDonDaCoPhieu(x.ma)) {
                    cboHoaDon.addItem(x);
                }
            }

        } catch (SQLException ex) {
            baoLoi("Không tải được dữ liệu từ SQL Server.", ex);
        } finally {
            dangNap = false;
        }

        napThongTinHoaDon();
    }



    private void napThongTinHoaDon() {
        LuaChon hd = (LuaChon) cboHoaDon.getSelectedItem();

        txtKhachHang.setText("");
        txtBienSoXe.setText("");
        txtTongTien.setText("");

        if (hd == null) {
            return;
        }

        String sql = """
        SELECT
            k.hoTen,
            h.tongTien,
            pt.bienSoXe
        FROM dbo.HoaDon h
        LEFT JOIN dbo.KhachHang k
            ON k.maKH = h.maKH
        LEFT JOIN dbo.PhuongTien pt
            ON pt.maKH = h.maKH
        WHERE h.maHD = ?
        ORDER BY pt.bienSoXe
        """;

        try (Connection c = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, hd.ma);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    thongBao("Không tìm thấy hóa đơn: " + hd.ma);
                    return;
                }

                String tenKH = rs.getString("hoTen");
                String tongTien = rs.getString("tongTien");

                txtKhachHang.setText(tenKH == null ? "" : tenKH);
                txtTongTien.setText(tongTien == null ? "" : tongTien);

                List<String> bienSoList = new ArrayList<>();

                do {
                    String bienSo = rs.getString("bienSoXe");

                    if (bienSo != null && !bienSo.isBlank()
                            && !bienSoList.contains(bienSo)) {
                        bienSoList.add(bienSo);
                    }
                } while (rs.next());

                if (bienSoList.isEmpty()) {
                    txtBienSoXe.setText("");
                    System.out.println(
                            "Chưa tìm thấy biển số xe cho hóa đơn: " + hd.ma
                    );
                    return;
                }

                if (bienSoList.size() == 1) {
                    txtBienSoXe.setText(bienSoList.get(0));
                    return;
                }

                Object luaChon = JOptionPane.showInputDialog(
                        this,
                        "Khách hàng có nhiều xe. Hãy chọn biển số xe:",
                        "Chọn phương tiện",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        bienSoList.toArray(),
                        bienSoList.get(0)
                );

                if (luaChon != null) {
                    txtBienSoXe.setText(luaChon.toString());
                }

            }

        } catch (SQLException ex) {
            baoLoi("Không lấy được thông tin hóa đơn.", ex);
        }
    }



    private void napDanhSachNhanVien() throws SQLException {
        String sql = """
        SELECT maNV, hoTen
        FROM dbo.NhanVien
        ORDER BY hoTen
        """;

        cboNhanVien.removeAllItems();

        try (Connection c = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cboNhanVien.addItem(
                        new LuaChon(
                                rs.getString("maNV"),
                                rs.getString("hoTen")
                        )
                );
            }
        }
    }
    // ================= DETAIL INPUT =================

    private void themHangMuc() {
        String ten = txtTenHangMuc.getText().trim();

        if (ten.isEmpty()) {
            thongBao("Vui lòng nhập tên hạng mục.");
            txtTenHangMuc.requestFocusInWindow();
            return;
        }

        try {
            LocalDate bd = LocalDate.parse(
                    txtNgayBatDau.getText().trim()
            );

            int thang = (Integer) spThoiHan.getValue();
            LocalDate hetHan = bd.plusMonths(thang);

            model.addRow(new Object[]{
                    model.getRowCount() + 1,
                    ten,
                    thang,
                    Date.valueOf(hetHan),
                    txtDieuKien.getText().trim(),
                    txtGhiChuCT.getText().trim()
            });

            txtTenHangMuc.setText("");
            txtDieuKien.setText("");
            txtGhiChuCT.setText("");
            txtTenHangMuc.requestFocusInWindow();

        } catch (Exception ex) {
            thongBao("Ngày bắt đầu phải có định dạng yyyy-MM-dd.");
        }
    }

    private void tinhNgayHetHan(int row) {
        if (row < 0 || row >= model.getRowCount()) return;

        try {
            LocalDate bd = LocalDate.parse(
                    txtNgayBatDau.getText().trim()
            );

            int thang = Integer.parseInt(
                    model.getValueAt(row, 2).toString()
            );

            if (thang <= 0) return;

            model.setValueAt(
                    Date.valueOf(bd.plusMonths(thang)), row, 3
            );

        } catch (Exception ignored) {
            // Kiểm tra lại dữ liệu khi lưu.
        }
    }

    private void xoaHangMuc() {
        int row = tblChiTiet.getSelectedRow();

        if (row < 0) {
            thongBao("Vui lòng chọn dòng cần xóa.");
            return;
        }

        model.removeRow(tblChiTiet.convertRowIndexToModel(row));

        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }
    }

    // ================= SAVE =================

    private void luuPhieu() {
        if (tblChiTiet.isEditing()
                && !tblChiTiet.getCellEditor().stopCellEditing()) {
            return;
        }

        LuaChon hd = (LuaChon) cboHoaDon.getSelectedItem();

        if (hd == null) {
            thongBao("Vui lòng chọn hóa đơn chưa có phiếu bảo hành.");
            return;
        }

        LuaChon nv = (LuaChon) cboNhanVien.getSelectedItem();

        if (nv == null) {
            thongBao("Vui lòng chọn nhân viên lập phiếu.");
            return;
        }

        if (model.getRowCount() == 0) {
            thongBao("Vui lòng thêm ít nhất một hạng mục bảo hành.");
            return;
        }

        try {
            LocalDate ngayLap = LocalDate.now();
            LocalDate ngayBD = LocalDate.parse(
                    txtNgayBatDau.getText().trim()
            );

            if (ngayBD.isBefore(ngayLap)) {
                thongBao("Ngày bắt đầu không được trước ngày lập.");
                return;
            }

            List<Object[]> ds = new ArrayList<>();

            for (int i = 0; i < model.getRowCount(); i++) {
                String ten = String.valueOf(
                        model.getValueAt(i, 1)
                ).trim();

                int thang = Integer.parseInt(
                        model.getValueAt(i, 2).toString()
                );

                if (ten.isEmpty() || thang <= 0) {
                    thongBao("Hạng mục ở dòng " + (i + 1)
                            + " không hợp lệ.");
                    return;
                }

                Date hetHan = Date.valueOf(ngayBD.plusMonths(thang));

                String dieuKien = String.valueOf(
                        model.getValueAt(i, 4) == null
                                ? "" : model.getValueAt(i, 4)
                );

                String ghiChuCT = String.valueOf(
                        model.getValueAt(i, 5) == null
                                ? "" : model.getValueAt(i, 5)
                );

                ds.add(new Object[]{
                        ten, thang, hetHan, dieuKien, ghiChuCT
                });
            }

            String maPBH = dao.taoMaPhieuMoi();

            int chon = JOptionPane.showConfirmDialog(
                    this,
                    "Mã phiếu: " + maPBH
                            + "\nHóa đơn: " + hd.ma
                            + "\nSố hạng mục: " + ds.size()
                            + "\nXác nhận lưu phiếu bảo hành?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (chon != JOptionPane.YES_OPTION) return;

            String bienSoXe = txtBienSoXe.getText().trim();

            if (bienSoXe.isEmpty()) {
                thongBao("Không lấy được biển số xe từ hóa đơn đã chọn.");
                return;
            }

            dao.themPhieuBaoHanh(
                    maPBH,
                    hd.ma,
                    nv.ma,
                    bienSoXe,
                    Date.valueOf(ngayBD),
                    (String) cboTrangThai.getSelectedItem(),
                    txtGhiChu.getText().trim(),
                    ds
            );

            txtMaPBH.setText(maPBH);

            JOptionPane.showMessageDialog(
                    this,
                    "Lưu phiếu bảo hành thành công!\nMã phiếu: " + maPBH
            );

            int in = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có muốn in phiếu vừa lưu không?",
                    "In phiếu",
                    JOptionPane.YES_NO_OPTION
            );

            if (in == JOptionPane.YES_OPTION) {
                inPhieuTheoMa(maPBH);
            }

            lamMoi();

        } catch (Exception ex) {
            baoLoi("Không thể lưu phiếu bảo hành.", ex);
        }
    }

    // ================= PRINT =================

    private void inPhieu() {
        String maPBH = txtMaPBH.getText().trim();

        if (maPBH.isEmpty()) {
            thongBao("Không có mã phiếu để in.");
            return;
        }

        inPhieuTheoMa(maPBH);
    }

    private void inPhieuTheoMa(String maPBH) {
        try {
            ThongTinIn t = dao.layThongTinIn(maPBH);

            StringBuilder html = new StringBuilder();

            html.append("""
                <html><head><meta charset='UTF-8'>
                <style>
                body { font-family: sans-serif; font-size: 11pt; }
                h2 { text-align: center; }
                table { width: 100%; border-collapse: collapse; }
                th, td { border: 1px solid #555; padding: 6px; }
                th { background: #eee; }
                </style></head><body>
                <b>GARA Ô TÔ</b>
                <h2>PHIẾU BẢO HÀNH</h2>
                <p style='text-align:center'>Mã phiếu: 
                """).append(escape(t.maPBH)).append("</p>");

            html.append("<p><b>Mã hóa đơn:</b> ")
                    .append(escape(t.maHD)).append("</p>");

            html.append("<p><b>Ngày lập:</b> ")
                    .append(t.ngayLap).append("</p>");

            html.append("<p><b>Ngày bắt đầu:</b> ")
                    .append(t.ngayBatDau).append("</p>");

            html.append("<p><b>Khách hàng:</b> ")
                    .append(escape(t.tenKhachHang)).append("</p>");

            html.append("<p><b>Biển số xe:</b> ")
                    .append(escape(t.bienSoXe)).append("</p>");

            html.append("<p><b>Mã khách hàng:</b> ")
                    .append(escape(t.maKH)).append("</p>");

            html.append("<p><b>Trạng thái:</b> ")
                    .append(escape(t.trangThai)).append("</p>");

            html.append("""
                <table><tr>
                <th>STT</th><th>Hạng mục</th>
                <th>Thời hạn</th><th>Ngày hết hạn</th>
                <th>Điều kiện</th><th>Ghi chú</th>
                </tr>
                """);

            int stt = 1;

            for (PhieuBaoHanh_DAO.ChiTiet ct : t.chiTiet) {
                html.append("<tr><td>").append(stt++)
                        .append("</td><td>").append(escape(ct.tenHangMuc))
                        .append("</td><td>").append(ct.thoiHan)
                        .append(" tháng</td><td>").append(ct.ngayHetHan)
                        .append("</td><td>").append(escape(ct.dieuKien))
                        .append("</td><td>").append(escape(ct.ghiChu))
                        .append("</td></tr>");
            }

            html.append("</table>");

            html.append("<p><b>Ghi chú:</b> ")
                    .append(escape(t.ghiChu)).append("</p>");

            html.append("""
                <br><br>
                <table style='border:none'>
                <tr>
                <td style='border:none;text-align:center'>
                Khách hàng<br>(Ký và ghi rõ họ tên)
                <br><br><br><br>
                </td>
                <td style='border:none;text-align:center'>
                Đại diện gara<br>
                """)
                    .append(escape(t.tenNhanVien))
                    .append("""
                <br><br><br>
                (Ký và ghi rõ họ tên)
                </td></tr></table></body></html>
                """);

            JEditorPane editor = new JEditorPane(
                    "text/html", html.toString()
            );
            editor.setEditable(false);

            editor.print(
                    null, null, true, null, null, true
            );

        } catch (SQLException ex) {
            baoLoi("Không tìm thấy dữ liệu phiếu để in.", ex);
        } catch (PrinterException ex) {
            baoLoi("Không thể in phiếu.", ex);
        }
    }

    private String escape(Object value) {
        if (value == null) return "";

        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // ================= RESET =================

    private void lamMoi() {
        txtGhiChu.setText("");
        txtTenHangMuc.setText("");
        txtDieuKien.setText("");
        txtGhiChuCT.setText("");
        txtBienSoXe.setText("");

        model.setRowCount(0);

        cboTrangThai.setSelectedItem("Còn hạn");
        napDuLieu();
    }

    // ================= STYLE =================

    private JPanel card() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return p;
    }

    private JLabel tieuDe(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lb.setForeground(NAVY);
        return lb;
    }

    private void themTruong(
            JPanel panel, int row, String label, JComponent input) {

        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0;
        a.gridy = row;
        a.anchor = GridBagConstraints.WEST;
        a.insets = new Insets(5, 4, 5, 8);

        panel.add(new JLabel(label), a);

        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 1;
        b.gridy = row;
        b.weightx = 1;
        b.fill = GridBagConstraints.HORIZONTAL;
        b.insets = new Insets(5, 0, 5, 4);

        input.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        input.setPreferredSize(new Dimension(190, 30));

        panel.add(input, b);
    }

    private JButton nut(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color),
                new EmptyBorder(7, 10, 7, 10)
        ));
        return b;
    }

    private void thongBao(String msg) {
        JOptionPane.showMessageDialog(
                this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE
        );
    }

    private void baoLoi(String msg, Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                msg + "\n" + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
