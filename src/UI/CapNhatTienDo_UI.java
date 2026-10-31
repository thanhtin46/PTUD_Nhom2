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

    public void napBang() {
        String q = txtTuKhoa.getText().trim().toLowerCase();
        String locTd = String.valueOf(cboLocTienDo.getSelectedItem());
        model.setRowCount(0);

        ArrayList<PhieuTiepNhan> ds = kho.getPhieuTiepNhans();
        int dem = 0;

        for (PhieuTiepNhan p : ds) {
            if (!BaoVeQuyen.phieu(p.getSoPhieu())) continue;
            String so = p.getSoPhieu().toLowerCase();
            String bienSo = p.getBienSo().toLowerCase();
            String khach = p.getHoTen().toLowerCase();
            String sdt = p.getSdt().toLowerCase();
            String ktv = (p.getKyThuatVien() != null ? p.getKyThuatVien() : "").toLowerCase();
            String td = p.getTienDo() != null ? p.getTienDo() : "Chờ tiếp nhận";

            // Kiểm tra bộ lọc tiến độ
            if (!"Tất cả tiến độ".equals(locTd) && !td.equalsIgnoreCase(locTd)) {
                continue;
            }

            // Kiểm tra từ khóa
            boolean khop = q.isEmpty() || so.contains(q) || bienSo.contains(q)
                    || khach.contains(q) || sdt.contains(q) || ktv.contains(q);

            if (khop) {
                dem++;
                model.addRow(new Object[]{
                        p.getSoPhieu(), p.getBienSo(), p.getHoTen(), p.getSdt(),
                        p.getHangXe() + " " + p.getLoaiXe(),
                        p.getYeuCau(),
                        p.getKyThuatVien() == null || p.getKyThuatVien().isEmpty() ? "(Chưa phân công)" : p.getKyThuatVien(),
                        td,
                        UiHelper.tien(p.getThanhTien()) + " đ",
                        p.getNgayTiepNhanHienThi()
                });
            }
        }

        lblDem.setText("Tìm thấy: " + dem + " / " + ds.size() + " phiếu sửa chữa");
    }

    private void capNhatTienDo() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.TIEN_DO)) return;
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu cần cập nhật tiến độ trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String soPhieu = model.getValueAt(dong, 0).toString();
        if (!BaoVeQuyen.phieu(soPhieu)) { JOptionPane.showMessageDialog(this,"Phiếu không thuộc phạm vi được giao."); return; }
        String tienDoMoi = String.valueOf(cboTienDoMoi.getSelectedItem());

        boolean ok = kho.capNhatTienDoPhieuTiepNhan(soPhieu, tienDoMoi, null);
        napBang();

        JOptionPane.showMessageDialog(this,
                "Đã cập nhật tiến độ phiếu [" + soPhieu + "] thành: " + tienDoMoi
                        + (ok ? "\n(Đã đồng bộ vào cơ sở dữ liệu SQL Server)" : ""),
                "Cập nhật thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Mở phiếu sửa chữa chi tiết và đồng bộ sang LapPhieuSuaChua_UI */

    private void moPhieuSuaChua() {
        int dong = table.getSelectedRow();

        if (dong < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn một phiếu trong bảng!"
            );
            return;
        }

        String maPTN = String.valueOf(model.getValueAt(dong, 0));
        String maPSC = null;

        String sql =
                "SELECT TOP 1 ps.maPSC " +
                        "FROM PhieuSuaChua ps " +
                        "JOIN PhieuTiepNhan tn ON tn.soVIN = ps.soVIN " +
                        "WHERE tn.maPTN = ? " +
                        "ORDER BY ps.ngayBD DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maPTN);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maPSC = rs.getString("maPSC");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm được phiếu sửa chữa:\n" + ex.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        Main_UI mainUI = Main_UI.getInstance();

        if (mainUI == null) return;

        if (maPSC != null) {
            // Đã có phiếu sửa chữa: mở đúng phiếu đã lưu
            LapPhieuSuaChua_UI.datPSCCanMoLai(maPSC);
        } else {
            // Chưa có phiếu sửa chữa: mở theo phiếu tiếp nhận
            LapPhieuSuaChua_UI.datPhieuCanChon(maPTN);
        }

        mainUI.chuyenTrang("Lập phiếu sửa chữa");
    }


    private void xemChiTiet() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu trong bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String soPhieu = model.getValueAt(dong, 0).toString();
        if (!BaoVeQuyen.phieu(soPhieu)) { JOptionPane.showMessageDialog(this,"Phiếu không thuộc phạm vi được giao."); return; }
        PhieuTiepNhan p = kho.timPhieuTiepNhan(soPhieu);
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
