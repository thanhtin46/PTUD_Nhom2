package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuTiepNhan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import ConnectDB.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Màn hình tra cứu và cập nhật tiến độ phiếu sửa chữa.
 */
public class CapNhatTienDo_UI extends JPanel {
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final JTextField txtTuKhoa = new JTextField();
    private final JComboBox<String> cboLocTienDo = new JComboBox<>(new String[]{
            "Tất cả tiến độ", "Chờ tiếp nhận", "Đã phân công", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"
    });
    private final JComboBox<String> cboTienDoMoi = new JComboBox<>(new String[]{
            "Chờ tiếp nhận", "Đã phân công", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"
    });
    private final JLabel lblDem = new JLabel("Đang tải dữ liệu...");

    private JTable table;
    private DefaultTableModel model;

    public CapNhatTienDo_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        // Header
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("Tra cứu phiếu sửa chữa & Cập nhật tiến độ");
        JLabel phu = new JLabel("Theo dõi tình trạng sửa chữa phương tiện, xem phân công KTV và cập nhật tiến độ công việc");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(100, 116, 139));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(Box.createVerticalStrut(4));
        north.add(phu);
        north.add(Box.createVerticalStrut(10));
        root.add(north, BorderLayout.NORTH);

        // Bảng kết quả
        JPanel bangPanel = UiHelper.panelBang("Danh sách phiếu sửa chữa phương tiện");

        // Thanh công cụ tìm kiếm & lọc
        JPanel timPanel = taoThanhTimKiem();
        bangPanel.add(timPanel, BorderLayout.NORTH);

        // Bảng dữ liệu
        model = UiHelper.model(
                "Số phiếu", "Biển số xe", "Khách hàng", "Số ĐT", "Hãng / Dòng xe",
                "Yêu cầu dịch vụ", "KTV phụ trách", "Tiến độ", "Chi phí dự kiến", "Ngày tiếp nhận"
        );
        table = UiHelper.bang(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getColumnModel().getColumn(0).setPreferredWidth(85);   // Số phiếu
        table.getColumnModel().getColumn(1).setPreferredWidth(105);  // Biển số xe
        table.getColumnModel().getColumn(2).setPreferredWidth(160);  // Khách hàng
        table.getColumnModel().getColumn(3).setPreferredWidth(105);  // Số ĐT
        table.getColumnModel().getColumn(4).setPreferredWidth(130);  // Hãng/Dòng
        table.getColumnModel().getColumn(5).setPreferredWidth(210);  // Yêu cầu
        table.getColumnModel().getColumn(6).setPreferredWidth(150);  // KTV
        table.getColumnModel().getColumn(7).setPreferredWidth(125);  // Tiến độ
        table.getColumnModel().getColumn(8).setPreferredWidth(120);  // Chi phí
        table.getColumnModel().getColumn(9).setPreferredWidth(110);  // Ngày tiếp nhận

        // Renderer màu cho cột Tiến độ (Cột 7)
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSel) {
                    String td = val != null ? val.toString() : "";
                    if (td.contains("Hoàn thành")) {
                        l.setForeground(new Color(22, 163, 74));
                    } else if (td.contains("Đang sửa")) {
                        l.setForeground(new Color(37, 99, 235));
                    } else if (td.contains("Chờ phụ tùng")) {
                        l.setForeground(new Color(225, 29, 72));   // Rose 600
                    } else if (td.contains("phân công")) {
                        l.setForeground(new Color(217, 119, 6));
                    } else {
                        l.setForeground(new Color(100, 116, 139));
                    }
                }
                return l;
            }
        });

        UiHelper.canGiua(table, 0, 1, 3, 7, 9);
        UiHelper.canTrai(table, 2, 4, 5, 6);
        UiHelper.canPhai(table, 8);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                String td = String.valueOf(model.getValueAt(table.getSelectedRow(), 7));
                cboTienDoMoi.setSelectedItem(td);
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    moPhieuSuaChua();
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);

        // Thanh thao tác phía dưới
        JPanel bot = taoThanhThaoTac();
        bangPanel.add(bot, BorderLayout.SOUTH);

        root.add(bangPanel, BorderLayout.CENTER);
        napBang();
    }

    private JPanel taoThanhTimKiem() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lblTk = new JLabel("Từ khóa:");
        lblTk.setFont(UiHelper.FONT_NHAN);
        lblTk.setForeground(new Color(51, 65, 85));
        p.add(lblTk);

        txtTuKhoa.setPreferredSize(new Dimension(320, 36));
        txtTuKhoa.setToolTipText("Nhập số phiếu (TN001, SC001), biển số xe, tên khách hàng, SĐT hoặc KTV...");
        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { napBang(); }
            @Override public void removeUpdate(DocumentEvent e) { napBang(); }
            @Override public void changedUpdate(DocumentEvent e) { napBang(); }
        });
        p.add(txtTuKhoa);

        JLabel lblLd = new JLabel("Tiến độ:");
        lblLd.setFont(UiHelper.FONT_NHAN);
        lblLd.setForeground(new Color(51, 65, 85));
        p.add(lblLd);

        cboLocTienDo.setPreferredSize(new Dimension(170, 36));
        cboLocTienDo.setFont(UiHelper.FONT_BT);
        cboLocTienDo.setBackground(Color.WHITE);
        cboLocTienDo.addActionListener(e -> napBang());
        p.add(cboLocTienDo);

        JButton btnTim = UiHelper.nut("Tìm kiếm", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_DAM, true, e -> napBang());
        JButton btnLamMoi = UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, true, e -> {
            txtTuKhoa.setText("");
            cboLocTienDo.setSelectedIndex(0);
            napBang();
        });
        p.add(btnTim);
        p.add(btnLamMoi);

        return p;
    }

    private JPanel taoThanhThaoTac() {
        JPanel bot = new JPanel(new BorderLayout(10, 0));
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(10, 4, 4, 4));

        // Bên trái: Cập nhật tiến độ nhanh
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel lblDoi = new JLabel("Đổi tiến độ:");
        lblDoi.setFont(UiHelper.FONT_NHAN);
        lblDoi.setForeground(new Color(51, 65, 85));
        left.add(lblDoi);

        cboTienDoMoi.setPreferredSize(new Dimension(160, 36));
        cboTienDoMoi.setFont(UiHelper.FONT_BT);
        cboTienDoMoi.setBackground(Color.WHITE);
        left.add(cboTienDoMoi);

        JButton btnCapNhat = UiHelper.nut("Cập nhật tiến độ", UiIcon.of(UiIcon.CHECK, 15), UiHelper.XANH_TRUNG, true, e -> capNhatTienDo());
        left.add(btnCapNhat);

        bot.add(left, BorderLayout.WEST);

        // Bên phải: Nút liên kết tới Lập phiếu sửa chữa & Xem chi tiết
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton btnMoPSC = UiHelper.nut("Mở phiếu sửa chữa chi tiết", UiIcon.of(UiIcon.WRENCH, 15), UiHelper.XANH_DAM, true, e -> moPhieuSuaChua());
        JButton btnChiTiet = UiHelper.nut("Xem chi tiết", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XAM, true, e -> xemChiTiet());

        right.add(btnChiTiet);
        right.add(btnMoPSC);
        bot.add(right, BorderLayout.EAST);

        return bot;
    }

    private String chuyenTrangThai(String trangThai) {
        if (trangThai == null) return "Chờ tiếp nhận";

        switch (trangThai.trim().toUpperCase()) {
            case "HOAN_THANH":
            case "DA_HOAN_THANH":
                return "Hoàn thành";

            case "DANG_SUA":
                return "Đang sửa";

            case "CHO_SUA":
                return "Chờ phụ tùng";

            case "DA_PHAN_CONG":
                return "Đã phân công";

            case "CHO_TIEP_NHAN":
                return "Chờ tiếp nhận";

            default:
                return trangThai;
        }
    }

    private String chuyenTrangThaiSQL(String trangThai) {
        switch (trangThai) {
            case "Hoàn thành":
                return "HOAN_THANH";

            case "Đang sửa":
                return "DANG_SUA";

            case "Chờ phụ tùng":
                return "CHO_SUA";

            case "Đã phân công":
                return "DA_PHAN_CONG";

            case "Chờ tiếp nhận":
                return "CHO_TIEP_NHAN";

            default:
                return "DANG_SUA";
        }
    }

    public void napBang() {
        String q = txtTuKhoa.getText().trim();
        String locTd = String.valueOf(cboLocTienDo.getSelectedItem());

        model.setRowCount(0);

        String sql =
                "SELECT ps.maPSC, pt.bienSoXe, kh.hoTen, kh.soDT, " +
                        "       pt.hangXe, pt.dongXe, tn.yeuCauSC, " +
                        "       nv.hoTen AS tenKTV, ps.trangThai, " +
                        "       COALESCE(cv.tienCong, 0) AS tienCong, " +
                        "       COALESCE(ct.tienPhuTung, 0) AS tienPhuTung, " +
                        "       COALESCE(tn.ngayTN, ps.ngayBD) AS ngayTiepNhan " +
                        "FROM PhieuSuaChua ps " +
                        "LEFT JOIN PhieuTiepNhan tn ON ps.maPTN = tn.maPTN " +
                        "JOIN KhachHang kh ON ps.maKH = kh.maKH " +
                        "JOIN PhuongTien pt ON ps.soVIN = pt.soVIN " +
                        "LEFT JOIN NhanVien nv ON ps.maNV = nv.maNV " +

                        "OUTER APPLY ( " +
                        "    SELECT SUM(tienCong) AS tienCong " +
                        "    FROM ChiTietCongViecSuaChua " +
                        "    WHERE maPSC = ps.maPSC " +
                        ") cv " +

                        "OUTER APPLY ( " +
                        "    SELECT SUM(soLuong * donGia) AS tienPhuTung " +
                        "    FROM ChiTietSuaChua " +
                        "    WHERE maPSC = ps.maPSC " +
                        ") ct " +

                        "ORDER BY ps.ngayBD DESC";

        int dem = 0;

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maPSC = rs.getString("maPSC");

                if (!BaoVeQuyen.phieu(maPSC)) {
                    continue;
                }

                String bienSo = rs.getString("bienSoXe");
                String khach = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String hangXe = rs.getString("hangXe");
                String dongXe = rs.getString("dongXe");
                String yeuCau = rs.getString("yeuCauSC");
                String ktv = rs.getString("tenKTV");

                String trangThaiDB = rs.getString("trangThai");
                String td = chuyenTrangThai(trangThaiDB);

                if (!"Tất cả tiến độ".equals(locTd)
                        && !td.equalsIgnoreCase(locTd)) {
                    continue;
                }

                boolean khop = q.isEmpty()
                        || maPSC.toLowerCase().contains(q.toLowerCase())
                        || String.valueOf(bienSo).toLowerCase().contains(q.toLowerCase())
                        || String.valueOf(khach).toLowerCase().contains(q.toLowerCase())
                        || String.valueOf(sdt).contains(q)
                        || String.valueOf(ktv).toLowerCase().contains(q.toLowerCase());

                if (!khop) {
                    continue;
                }

                long tienCong = rs.getBigDecimal("tienCong").longValue();
                long tienPhuTung = rs.getBigDecimal("tienPhuTung").longValue();

                long tongTruocVAT = tienCong + tienPhuTung;
                long vat = (long) (tongTruocVAT * 0.08);
                long tongChiPhi = tongTruocVAT + vat;

                java.sql.Timestamp ngay =
                        rs.getTimestamp("ngayTiepNhan");

                String ngayHienThi = ngay == null
                        ? ""
                        : new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                        .format(ngay);

                model.addRow(new Object[]{
                        maPSC,
                        bienSo,
                        khach,
                        sdt,
                        hangXe + " " + dongXe,
                        yeuCau,
                        ktv == null ? "(Chưa phân công)" : ktv,
                        td,
                        UiHelper.tien(tongChiPhi) + " đ",
                        ngayHienThi
                });

                dem++;
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách phiếu sửa chữa:\n" + ex.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }

        lblDem.setText("Tìm thấy: " + dem + " phiếu sửa chữa");
    }

    private void capNhatTienDo() {
        if (!BaoVeQuyen.yeuCau(
                this, Entity.QuyenTruyCap.Quyen.TIEN_DO)) {
            return;
        }

        int dong = table.getSelectedRow();

        if (dong < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn phiếu cần cập nhật!"
            );
            return;
        }

        dong = table.convertRowIndexToModel(dong);

        String maPSC = String.valueOf(model.getValueAt(dong, 0));

        if (!BaoVeQuyen.phieu(maPSC)) {
            JOptionPane.showMessageDialog(
                    this, "Phiếu không thuộc phạm vi được giao."
            );
            return;
        }

        String tienDoMoi =
                String.valueOf(cboTienDoMoi.getSelectedItem());

        String trangThaiSQL = chuyenTrangThaiSQL(tienDoMoi);

        String sql =
                "UPDATE PhieuSuaChua SET trangThai = ? " +
                        "WHERE maPSC = ?";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, trangThaiSQL);
            ps.setString(2, maPSC);

            int soDong = ps.executeUpdate();

            if (soDong > 0) {
                kho.dongBoTuSQLServer();
                napBang();

                JOptionPane.showMessageDialog(
                        this,
                        "Đã cập nhật tiến độ phiếu " + maPSC
                                + " thành: " + tienDoMoi,
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this, "Không tìm thấy phiếu sửa chữa!"
                );
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi cập nhật tiến độ:\n" + ex.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /** Mở phiếu sửa chữa chi tiết và đồng bộ sang LapPhieuSuaChua_UI */

    /**
     * Mở đúng phiếu sửa chữa đang được chọn trên bảng.
     */
    private void moPhieuSuaChua() {
        int dong = table.getSelectedRow();

        if (dong < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một phiếu sửa chữa trong bảng!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Chuyển chỉ số dòng đang chọn về chỉ số trong model
        dong = table.convertRowIndexToModel(dong);

        // Cột 0 của bảng là mã phiếu sửa chữa (maPSC)
        Object giaTri = model.getValueAt(dong, 0);

        if (giaTri == null || giaTri.toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không lấy được mã phiếu sửa chữa!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String maPSC = giaTri.toString().trim();

        if (!maPSC.startsWith("SC")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mã phiếu sửa chữa không hợp lệ: " + maPSC,
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!BaoVeQuyen.phieu(maPSC)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Phiếu không thuộc phạm vi được giao.",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Main_UI mainUI = Main_UI.getInstance();

        if (mainUI == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy màn hình chính!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Truyền đúng mã phiếu sửa chữa đang chọn
        LapPhieuSuaChua_UI.datPSCCanMoLai(maPSC);

        // Chuyển sang màn hình lập phiếu sửa chữa
        mainUI.chuyenTrang("Lập phiếu sửa chữa");
    }


    private void xemChiTiet() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu trong bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maPSC = model.getValueAt(dong, 0).toString().trim();

        if (!BaoVeQuyen.phieu(maPSC)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Phiếu không thuộc phạm vi được giao."
            );
            return;
        }

// Lấy mã phiếu tiếp nhận tương ứng với phiếu sửa chữa
        String maPTN = null;

        String sql = """
        SELECT maPTN
        FROM PhieuSuaChua
        WHERE maPSC = ?
        """;

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maPSC);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maPTN = rs.getString("maPTN");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi lấy thông tin phiếu tiếp nhận: " + ex.getMessage()
            );
            return;
        }

        if (maPTN == null || maPTN.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy phiếu tiếp nhận của phiếu sửa chữa " + maPSC
            );
            return;
        }

        PhieuTiepNhan p = kho.timPhieuTiepNhan(maPTN);

        if (p == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy dữ liệu phiếu tiếp nhận " + maPTN
            );
            return;
        }
        if (p == null) return;

        String msg = "<html><div style='width:400px;font-family:Segoe UI;'>"
                + "<h3 style='color:#0F172A;margin-bottom:8px;'>CHI TIẾT PHIẾU TIẾP NHẬN & SỬA CHỮA</h3>"
                + "<table cellpadding='3' style='font-size:12px;'>"
                + "<tr><td width='130'><b>Số phiếu:</b></td><td><span style='color:#2563EB;font-weight:bold;'>" + p.getSoPhieu() + "</span></td></tr>"
                + "<tr><td><b>Biển số xe:</b></td><td><b>" + p.getBienSo() + "</b> (" + p.getHangXe() + " " + p.getLoaiXe() + ")</td></tr>"
                + "<tr><td><b>Khách hàng:</b></td><td>" + p.getHoTen() + " — " + p.getSdt() + "</td></tr>"
                + "<tr><td><b>Ngày tiếp nhận:</b></td><td>" + p.getNgayTiepNhanHienThi() + "</td></tr>"
                + "<tr><td><b>Yêu cầu ban đầu:</b></td><td>" + p.getYeuCau() + "</td></tr>"
                + "<tr><td><b>KTV phụ trách:</b></td><td><b>" + (p.getKyThuatVien() == null || p.getKyThuatVien().isEmpty() ? "Chưa giao" : p.getKyThuatVien()) + "</b></td></tr>"
                + "<tr><td><b>Tiến độ hiện tại:</b></td><td><span style='color:#D97706;font-weight:bold;'>" + p.getTienDo() + "</span></td></tr>"
                + "<tr><td><b>Chi phí ước tính:</b></td><td><span style='color:#DC2626;font-weight:bold;'>" + UiHelper.tien(p.getThanhTien()) + " VNĐ</span></td></tr>"
                + "</table>"
                + "</div></html>";

        JOptionPane.showMessageDialog(this, msg, "Thông tin phiếu sửa chữa", JOptionPane.INFORMATION_MESSAGE);
    }
}
