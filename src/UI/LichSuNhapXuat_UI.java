package UI;

import Entity.ChiTietKho;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import Entity.PhieuKho;
import Entity.PhuTung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * UC-KHO-03: Theo dõi lịch sử nhập xuất kho.
 * Tác nhân: Nhân viên kho, Quản lý gara.
 * - Xem danh sách phiếu nhập và xuất kho.
 * - Lọc theo: Loại phiếu (Tất cả, Nhập kho, Xuất kho).
 * - Tìm kiếm theo mã phiếu hoặc mã phụ tùng / tên phụ tùng.
 * - Lọc theo khoảng thời gian: Từ ngày - Đến ngày.
 * - Xem chi tiết: danh sách phụ tùng, số lượng, đơn giá, thành tiền, đối chiếu tồn kho.
 * - Xử lý đầy đủ ngoại lệ E1, E2, E3, E4.
 */
public class LichSuNhapXuat_UI extends JPanel {

    private final KhoDuLieu kho = KhoDuLieu.get();
    private final NhanVien nvHienTai;

    // ==== THÀNH PHẦN TÌM KIẾM & BỘ LỌC ====
    private final JComboBox<String> cboLoaiPhieu = new JComboBox<>(new String[]{
            "Tất cả", "Nhập kho", "Xuất kho"
    });
    private final JTextField txtTuKhoa = new JTextField();
    private final JTextField txtTuNgay = new JTextField();
    private final JTextField txtDenNgay = new JTextField();

    // ==== THẺ THỐNG KÊ (KPI) ====
    private final JLabel lblTongPhieu = new JLabel("0");
    private final JLabel lblTongNhap = new JLabel("0 phiếu (0 PT)");
    private final JLabel lblTongXuat = new JLabel("0 phiếu (0 PT)");
    private final JLabel lblTongTien = new JLabel("0 đ");

    // ==== BẢNG DANH SÁCH LỊCH SỬ ====
    private DefaultTableModel modelPhieu;
    private JTable bangPhieu;
    private final ArrayList<PhieuKho> dsHienThi = new ArrayList<>();

    // ==== BẢNG CHI TIẾT PHỤ TÙNG ====
    private DefaultTableModel modelChiTiet;
    private JTable bangChiTiet;
    private final JLabel lblTieuDeChiTiet = new JLabel("Chi tiết phụ tùng: (Chưa chọn phiếu)");
    private final JLabel lblTongChiTiet = new JLabel("Tổng mặt hàng: 0 | Tổng SL: 0 | Tổng tiền: 0 đ");

    public LichSuNhapXuat_UI() {
        this(null);
    }

    public LichSuNhapXuat_UI(NhanVien nv) {
        this.nvHienTai = nv;
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);

        JPanel root = UiHelper.nen();
        add(root, BorderLayout.CENTER);

        // ==== NORTH PANEL: TIÊU ĐỀ + BỘ LỌC + THẺ THỐNG KÊ ====
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        // 1. Tiêu đề
        JPanel rowTitle = new JPanel(new BorderLayout(12, 0));
        rowTitle.setOpaque(false);
        JLabel tde = UiHelper.tieuDeTrang("📋 Lịch sử nhập xuất kho");
        JLabel sub = new JLabel("Tra cứu lịch sử nhập, xuất phụ tùng và kiểm tra biến động kho (UC-KHO-03)");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(100, 116, 139));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(tde);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(sub);
        rowTitle.add(titleBox, BorderLayout.WEST);

        JButton btnDongBo = UiHelper.nut("Đồng bộ CSDL (F5)", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XANH_TRUNG, true, e -> dongBoTuCSDL());
        rowTitle.add(btnDongBo, BorderLayout.EAST);
        north.add(rowTitle);
        north.add(Box.createVerticalStrut(14));

        // 2. Thẻ KPI thống kê đối chiếu
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setBorder(new EmptyBorder(0, 0, 14, 0));
        kpiGrid.add(theKPI("Tổng giao dịch", lblTongPhieu, "Tổng số phiếu đã lưu", new Color(15, 23, 42)));
        kpiGrid.add(theKPI("Phiếu nhập kho", lblTongNhap, "Nhập hàng từ NCC", new Color(22, 163, 74)));
        kpiGrid.add(theKPI("Phiếu xuất kho", lblTongXuat, "Xuất vật tư sửa chữa", new Color(37, 99, 235)));
        kpiGrid.add(theKPI("Tổng giá trị giao dịch", lblTongTien, "Giá trị luân chuyển", new Color(245, 158, 11)));
        north.add(kpiGrid);

        // 3. Khung tìm kiếm & bộ lọc (Thiết kế đúng theo ảnh giao diện mẫu)
        JPanel filterCard = UiHelper.panelBang("Bộ lọc tra cứu lịch sử");
        JPanel filterForm = new JPanel(new GridBagLayout());
        filterForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        // Cấu hình giao diện input
        cboLoaiPhieu.setFont(UiHelper.FONT_BT);
        cboLoaiPhieu.setPreferredSize(new Dimension(220, 36));

        UiHelper.trangTriInput(txtTuKhoa);
        txtTuKhoa.setPreferredSize(new Dimension(280, 36));
        txtTuKhoa.setToolTipText("Nhập mã phiếu (NK..., XK...) hoặc mã phụ tùng (PT...)");

        UiHelper.trangTriInput(txtTuNgay);
        txtTuNgay.setPreferredSize(new Dimension(180, 36));
        txtTuNgay.setToolTipText("Định dạng dd/MM/yyyy (ví dụ: 01/01/2026)");

        UiHelper.trangTriInput(txtDenNgay);
        txtDenNgay.setPreferredSize(new Dimension(180, 36));
        txtDenNgay.setToolTipText("Định dạng dd/MM/yyyy");

        // Hàng 1: Loại phiếu | Mã phiếu / Mã phụ tùng
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        JLabel lblLp = new JLabel("Loại phiếu");
        lblLp.setFont(UiHelper.FONT_NHAN);
        filterForm.add(lblLp, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35;
        filterForm.add(cboLoaiPhieu, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.15;
        JLabel lblTk = new JLabel("Mã phiếu / mã phụ tùng");
        lblTk.setFont(UiHelper.FONT_NHAN);
        filterForm.add(lblTk, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.35;
        filterForm.add(txtTuKhoa, gbc);

        // Hàng 2: Từ ngày | Đến ngày
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        JLabel lblTn = new JLabel("Từ ngày");
        lblTn.setFont(UiHelper.FONT_NHAN);
        filterForm.add(lblTn, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.35;
        filterForm.add(txtTuNgay, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.15;
        JLabel lblDn = new JLabel("Đến ngày");
        lblDn.setFont(UiHelper.FONT_NHAN);
        filterForm.add(lblDn, gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.35;
        filterForm.add(txtDenNgay, gbc);

        // Hàng 3: Nút Tìm kiếm & Đặt lại
        JPanel rowBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        rowBtn.setOpaque(false);
        JButton btnTimKiem = UiHelper.nut("Tìm kiếm", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, true, e -> thucHienTimKiem());
        JButton btnDatLai = UiHelper.nut("Đặt lại", UiIcon.of(UiIcon.REFRESH, 15), UiHelper.XAM, false, e -> datLaiBoLoc());
        rowBtn.add(btnDatLai);
        rowBtn.add(btnTimKiem);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.weightx = 1.0;
        filterForm.add(rowBtn, gbc);

        filterCard.add(filterForm, BorderLayout.CENTER);
        north.add(filterCard);
        north.add(Box.createVerticalStrut(14));
        root.add(north, BorderLayout.NORTH);

        // ==== CENTER: BẢNG DANH SÁCH LỊCH SỬ & CHI TIẾT (SPLIT MASTER-DETAIL) ====
        // 1. Bảng danh sách phiếu
        JPanel panelMaster = UiHelper.panelBang("Danh sách lịch sử phiếu nhập xuất");
        modelPhieu = UiHelper.model("Mã phiếu", "Loại phiếu", "Ngày lập", "Người lập", "Đối tác / Ghi chú", "Trạng thái", "Tổng tiền (VNĐ)");
        bangPhieu = UiHelper.bang(modelPhieu);
        bangPhieu.getColumnModel().getColumn(0).setPreferredWidth(100);
        bangPhieu.getColumnModel().getColumn(1).setPreferredWidth(100);
        bangPhieu.getColumnModel().getColumn(2).setPreferredWidth(100);
        bangPhieu.getColumnModel().getColumn(3).setPreferredWidth(150);
        bangPhieu.getColumnModel().getColumn(4).setPreferredWidth(220);
        bangPhieu.getColumnModel().getColumn(5).setPreferredWidth(110);
        bangPhieu.getColumnModel().getColumn(6).setPreferredWidth(140);
        UiHelper.canGiua(bangPhieu, 0, 1, 2, 5);
        UiHelper.canPhai(bangPhieu, 6);

        // Tô màu nổi bật cột Loại phiếu và Trạng thái
        bangPhieu.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                if (!isSelected) {
                    if ("Nhập kho".equals(value)) {
                        setForeground(new Color(22, 163, 74));
                    } else if ("Xuất kho".equals(value)) {
                        setForeground(new Color(37, 99, 235));
                    } else {
                        setForeground(Color.DARK_GRAY);
                    }
                }
                return c;
            }
        });

        bangPhieu.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                if (!isSelected) {
                    String str = value != null ? value.toString() : "";
                    if (str.contains("Đã nhập") || str.contains("Đã xuất")) {
                        setForeground(new Color(22, 163, 74));
                    } else if (str.contains("Chờ")) {
                        setForeground(new Color(245, 158, 11));
                    } else if (str.contains("Hủy") || str.contains("hủy")) {
                        setForeground(new Color(239, 68, 68));
                    } else {
                        setForeground(Color.DARK_GRAY);
                    }
                }
                return c;
            }
        });

        JScrollPane spMaster = new JScrollPane(bangPhieu);
        UiHelper.danhSachBong(spMaster);
        panelMaster.add(spMaster, BorderLayout.CENTER);

        // Thanh thao tác dưới bảng phiếu
        JPanel barMaster = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        barMaster.setOpaque(false);
        JButton btnXemChiTiet = UiHelper.nut("Xem chi tiết phiếu", UiIcon.of(UiIcon.SEARCH, 15), UiHelper.XANH_TRUNG, true, e -> hienThiChiTietPhieuDangChon());
        barMaster.add(btnXemChiTiet);
        panelMaster.add(barMaster, BorderLayout.SOUTH);

        // 2. Bảng chi tiết phụ tùng
        JPanel panelDetail = UiHelper.panelBang("Chi tiết phụ tùng giao dịch");
        lblTieuDeChiTiet.setFont(UiHelper.FONT_TIEU_DE_PHU);
        lblTieuDeChiTiet.setForeground(UiHelper.XANH_DAM);
        panelDetail.add(lblTieuDeChiTiet, BorderLayout.NORTH);

        modelChiTiet = UiHelper.model("Mã phụ tùng", "Tên phụ tùng", "Số lượng", "Đơn giá", "Thành tiền", "Tồn kho hiện tại");
        bangChiTiet = UiHelper.bang(modelChiTiet);
        bangChiTiet.getColumnModel().getColumn(0).setPreferredWidth(100);
        bangChiTiet.getColumnModel().getColumn(1).setPreferredWidth(230);
        bangChiTiet.getColumnModel().getColumn(2).setPreferredWidth(80);
        bangChiTiet.getColumnModel().getColumn(3).setPreferredWidth(110);
        bangChiTiet.getColumnModel().getColumn(4).setPreferredWidth(120);
        bangChiTiet.getColumnModel().getColumn(5).setPreferredWidth(120);
        UiHelper.canGiua(bangChiTiet, 0, 2, 5);
        UiHelper.canPhai(bangChiTiet, 3, 4);

        JScrollPane spDetail = new JScrollPane(bangChiTiet);
        UiHelper.danhSachBong(spDetail);
        panelDetail.add(spDetail, BorderLayout.CENTER);

        JPanel footerDetail = new JPanel(new BorderLayout());
        footerDetail.setOpaque(false);
        footerDetail.setBorder(new EmptyBorder(8, 4, 4, 4));
        lblTongChiTiet.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongChiTiet.setForeground(UiHelper.CHU_CHAY);
        footerDetail.add(lblTongChiTiet, BorderLayout.EAST);
        panelDetail.add(footerDetail, BorderLayout.SOUTH);

        // Chia giao diện thành Master-Detail SplitPane
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelMaster, panelDetail);
        split.setResizeWeight(0.58);
        split.setDividerLocation(280);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);

        // Lắng nghe sự kiện chọn dòng trên bảng master
        bangPhieu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                capNhatChiTietTuDong();
            }
        });

        // Hỗ trợ nhấn Enter trong ô tìm kiếm
        txtTuKhoa.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) thucHienTimKiem();
            }
        });

        // Nạp dữ liệu ban đầu
        taiDuLieuBanDau();
    }

    /**
     * Nạp dữ liệu ban đầu khi mở màn hình.
     */
    private void taiDuLieuBanDau() {
        thucHienLoc(false);
    }

    /**
     * Đồng bộ lại dữ liệu trực tiếp từ SQL Server (Phím F5).
     */
    private void dongBoTuCSDL() {
        try {
            kho.dongBoTuSQLServer();
            thucHienLoc(true);
            JOptionPane.showMessageDialog(this, "Đã đồng bộ lịch sử nhập xuất kho mới nhất từ CSDL!", "Đồng bộ thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            // E3: Lỗi kết nối CSDL
            JOptionPane.showMessageDialog(this, "Ngoại lệ E3: Lỗi kết nối CSDL!\nKhông thể tải dữ liệu từ SQL Server: " + ex.getMessage() + "\nĐang hiển thị dữ liệu lưu trong bộ nhớ.", "Lỗi CSDL", JOptionPane.WARNING_MESSAGE);
            thucHienLoc(false);
        }
    }

    /**
     * Thực hiện tìm kiếm theo các tiêu chí và xử lý ngoại lệ.
     */
    private void thucHienTimKiem() {
        thucHienLoc(true);
    }

    /**
     * Lọc và hiển thị danh sách phiếu.
     */
    private void thucHienLoc(boolean kiemTraNgoaiLeThoiGian) {
        String loai = (String) cboLoaiPhieu.getSelectedItem();
        String tuKhoa = txtTuKhoa.getText().trim();
        String strTuNgay = txtTuNgay.getText().trim();
        String strDenNgay = txtDenNgay.getText().trim();

        LocalDate tuNgay = null;
        LocalDate denNgay = null;

        // Xử lý ngoại lệ E2: Khoảng thời gian không hợp lệ
        if (!strTuNgay.isEmpty()) {
            tuNgay = KhoDuLieu.phanTichNgay(strTuNgay);
            if (tuNgay == null && kiemTraNgoaiLeThoiGian) {
                UiHelper.loi(this, "Ngoại lệ E2: Ngày bắt đầu không hợp lệ! Vui lòng nhập đúng định dạng dd/MM/yyyy.", txtTuNgay);
                return;
            }
        }

        if (!strDenNgay.isEmpty()) {
            denNgay = KhoDuLieu.phanTichNgay(strDenNgay);
            if (denNgay == null && kiemTraNgoaiLeThoiGian) {
                UiHelper.loi(this, "Ngoại lệ E2: Ngày kết thúc không hợp lệ! Vui lòng nhập đúng định dạng dd/MM/yyyy.", txtDenNgay);
                return;
            }
        }

        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            UiHelper.loi(this, "Ngoại lệ E2: Khoảng thời gian không hợp lệ!\nNgày bắt đầu (" + strTuNgay + ") không được lớn hơn ngày kết thúc (" + strDenNgay + ").", txtTuNgay);
            return;
        }

        // Lấy danh sách phiếu theo điều kiện lọc
        ArrayList<PhieuKho> ketQua;
        try {
            ketQua = kho.layLichSuNhapXuat(loai, tuKhoa, tuNgay, denNgay);
        } catch (Exception ex) {
            // E3: Lỗi kết nối CSDL
            JOptionPane.showMessageDialog(this, "Ngoại lệ E3: Lỗi kết nối CSDL khi tải lịch sử: " + ex.getMessage(), "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
            ketQua = new ArrayList<>();
        }

        dsHienThi.clear();
        dsHienThi.addAll(ketQua);
        napBangPhieu(dsHienThi);
        capNhatKPI(dsHienThi);

        // Xử lý ngoại lệ E1: Không có phiếu phù hợp
        if (dsHienThi.isEmpty() && kiemTraNgoaiLeThoiGian) {
            JOptionPane.showMessageDialog(this, "Ngoại lệ E1: Không tìm thấy lịch sử nhập xuất phù hợp với điều kiện tra cứu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }

        // Xóa bảng chi tiết
        modelChiTiet.setRowCount(0);
        lblTieuDeChiTiet.setText("Chi tiết phụ tùng: (Chưa chọn phiếu)");
        lblTongChiTiet.setText("Tổng mặt hàng: 0 | Tổng SL: 0 | Tổng tiền: 0 đ");
    }

    /**
     * Nạp dữ liệu vào bảng danh sách phiếu.
     */
    private void napBangPhieu(ArrayList<PhieuKho> ds) {
        modelPhieu.setRowCount(0);
        for (PhieuKho pk : ds) {
            String loaiHienThi = PhieuKho.NHAP.equalsIgnoreCase(pk.getLoai()) ? "Nhập kho" : "Xuất kho";
            String trangThaiHienThi = pk.getTrangThaiHienThi();

            modelPhieu.addRow(new Object[]{
                    pk.getSoPhieu(),
                    loaiHienThi,
                    pk.getNgay(),
                    pk.getNguoiLap() != null && !pk.getNguoiLap().isEmpty() ? pk.getNguoiLap() : "NV Kho",
                    pk.getGhiChu() != null && !pk.getGhiChu().isEmpty() ? pk.getGhiChu() : (pk.getNhaCungCap() != null ? pk.getNhaCungCap() : "-"),
                    trangThaiHienThi,
                    UiHelper.tien(pk.getTongTien())
            });
        }
        if (modelPhieu.getRowCount() > 0) {
            bangPhieu.setRowSelectionInterval(0, 0);
        }
    }

    /**
     * Cập nhật các thẻ KPI đối chiếu.
     */
    private void capNhatKPI(ArrayList<PhieuKho> ds) {
        int soNhap = 0;
        int soXuat = 0;
        int slPtNhap = 0;
        int slPtXuat = 0;
        long tongTienGiaoDich = 0;

        for (PhieuKho pk : ds) {
            tongTienGiaoDich += pk.getTongTien();
            int slTrongPhieu = 0;
            for (ChiTietKho ct : pk.getChiTiet()) {
                slTrongPhieu += ct.getSoLuong();
            }

            if (PhieuKho.NHAP.equalsIgnoreCase(pk.getLoai())) {
                soNhap++;
                slPtNhap += slTrongPhieu;
            } else {
                soXuat++;
                slPtXuat += slTrongPhieu;
            }
        }

        lblTongPhieu.setText(String.valueOf(ds.size()) + " phiếu");
        lblTongNhap.setText(soNhap + " phiếu (" + slPtNhap + " phụ tùng)");
        lblTongXuat.setText(soXuat + " phiếu (" + slPtXuat + " phụ tùng)");
        lblTongTien.setText(UiHelper.tien(tongTienGiaoDich) + " đ");
    }

    /**
     * Cập nhật bảng chi tiết khi chọn một dòng trên bảng phiếu.
     */
    private void capNhatChiTietTuDong() {
        int r = bangPhieu.getSelectedRow();
        if (r < 0 || r >= dsHienThi.size()) {
            modelChiTiet.setRowCount(0);
            lblTieuDeChiTiet.setText("Chi tiết phụ tùng: (Chưa chọn phiếu)");
            lblTongChiTiet.setText("Tổng mặt hàng: 0 | Tổng SL: 0 | Tổng tiền: 0 đ");
            return;
        }

        PhieuKho pk = dsHienThi.get(r);
        lblTieuDeChiTiet.setText("Chi tiết phụ tùng thuộc phiếu: " + pk.getSoPhieu() + " (" + (PhieuKho.NHAP.equalsIgnoreCase(pk.getLoai()) ? "Nhập kho" : "Xuất kho") + " - Ngày " + pk.getNgay() + ")");
        modelChiTiet.setRowCount(0);

        int tongSL = 0;
        long tongTien = 0;

        for (ChiTietKho ct : pk.getChiTiet()) {
            tongSL += ct.getSoLuong();
            tongTien += ct.getThanhTien();

            // Đối chiếu tồn kho hiện tại
            PhuTung pt = kho.timPhuTung(ct.getMaPT());
            String tonHienTai = pt != null ? String.valueOf(pt.getSoLuongTon()) : "Chưa có";

            modelChiTiet.addRow(new Object[]{
                    ct.getMaPT(),
                    ct.getTenPT(),
                    ct.getSoLuong(),
                    UiHelper.tien(ct.getDonGia()),
                    UiHelper.tien(ct.getThanhTien()),
                    tonHienTai
            });
        }

        lblTongChiTiet.setText("Tổng mặt hàng: " + pk.getChiTiet().size() +
                " | Tổng SL: " + tongSL +
                " | Tổng tiền: " + UiHelper.tien(tongTien) + " đ");
    }

    /**
     * Xem chi tiết phiếu đang chọn và xử lý ngoại lệ E4.
     */
    private void hienThiChiTietPhieuDangChon() {
        int r = bangPhieu.getSelectedRow();
        if (r < 0 || r >= dsHienThi.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu trong danh sách để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PhieuKho pk = dsHienThi.get(r);
        // E4: Phiếu không có chi tiết
        if (pk.getChiTiet() == null || pk.getChiTiet().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ngoại lệ E4: Phiếu [" + pk.getSoPhieu() + "] chưa có dòng chi tiết phụ tùng!", "Thông báo phiếu rỗng", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        capNhatChiTietTuDong();
    }

    /**
     * Đặt lại bộ lọc về mặc định.
     */
    private void datLaiBoLoc() {
        cboLoaiPhieu.setSelectedIndex(0);
        txtTuKhoa.setText("");
        txtTuNgay.setText("");
        txtDenNgay.setText("");
        thucHienLoc(false);
    }

    /**
     * Tạo thẻ chỉ số KPI giao diện hiện đại.
     */
    private JPanel theKPI(String tieuDe, JLabel lblGiaTri, String phuDe, Color mauAccent) {
        JPanel p = new JPanel(new BorderLayout(8, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, mauAccent),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel tde = new JLabel(tieuDe);
        tde.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tde.setForeground(new Color(100, 116, 139));

        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblGiaTri.setForeground(UiHelper.XANH_DAM);

        JLabel sub = new JLabel(phuDe);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(148, 163, 184));

        p.add(tde, BorderLayout.NORTH);
        p.add(lblGiaTri, BorderLayout.CENTER);
        p.add(sub, BorderLayout.SOUTH);
        return p;
    }
}
