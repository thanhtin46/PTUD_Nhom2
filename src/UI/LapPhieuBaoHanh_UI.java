package UI;

import ConnectDB.ConnectDB;
import DAO.PhieuBaoHanh_DAO;
import DAO.PhieuBaoHanh_DAO.ChiTiet;
import DAO.PhieuBaoHanh_DAO.LuaChon;
import DAO.PhieuBaoHanh_DAO.ThongTinIn;
import Entity.ChiTietPhieuBaoHanh;
import Entity.KhoDuLieu;
import Entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Lập phiếu bảo hành xe & linh kiện chuyên nghiệp:
 * - Nêu rõ và phân định rành mạch giữa:
 *   1. Mục BẢO HÀNH DỊCH VỤ SỬA CHỮA (tay nghề, kỹ thuật, căn chỉnh, lắp ráp tại gara)
 *   2. Mục BẢO HÀNH PHỤ TÙNG & LINH KIỆN THAY THẾ (vật tư chính hãng, lỗi kỹ thuật nhà sản xuất)
 * - Tự động trích xuất các hạng mục sửa chữa và phụ tùng từ Hóa đơn đã chọn
 * - Hỗ trợ in mẫu phiếu bảo hành chính thức với đầy đủ 2 danh mục độc lập cho khách hàng.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class LapPhieuBaoHanh_UI extends JPanel {

    private final NhanVien nhanVien;
    private String maPhieuVuaLuu = "";
    private final PhieuBaoHanh_DAO dao = new PhieuBaoHanh_DAO();

    private final Color NAVY = new Color(15, 23, 42);
    private final Color BLUE = new Color(37, 99, 235);
    private final Color GREEN = new Color(22, 163, 74);
    private final Color BORDER = new Color(226, 232, 240);
    private final Color CARD_BG = Color.WHITE;

    // 1. Thông tin phiếu
    private final JTextField txtMaPBH = new JTextField();
    private final JTextField txtNgayLap = new JTextField();
    private final JTextField txtNgayBatDau = new JTextField();
    private final JComboBox<LuaChon> cboNhanVien = new JComboBox<>();
    private final JComboBox<String> cboTrangThai =
            new JComboBox<>(new String[]{"Còn hạn", "Hết hạn", "Tạm ngưng", "Đã hủy"});

    // 2. Thông tin hóa đơn & phương tiện
    private final JComboBox<LuaChon> cboHoaDon = new JComboBox<>();
    private final JTextField txtKhachHang = new JTextField();
    private final JTextField txtBienSoXe = new JTextField();
    private final JTextField txtTongTien = new JTextField();

    // 3. Nhập hạng mục bảo hành
    private final JComboBox<String> cboLoaiHangMuc = new JComboBox<>(new String[]{
            ChiTietPhieuBaoHanh.LOAI_SUA_CHUA,
            ChiTietPhieuBaoHanh.LOAI_PHU_TUNG
    });
    private final JTextField txtTenHangMuc = new JTextField();
    private final JSpinner spThoiHan = new JSpinner(new SpinnerNumberModel(3, 1, 120, 1));
    private final JTextField txtDieuKien = new JTextField();
    private final JTextField txtGhiChuCT = new JTextField();
    private final JTextArea txtGhiChu = new JTextArea(2, 20);

    // Bảng & Thống kê
    private DefaultTableModel model;
    private JTable tblChiTiet;
    private final JLabel lblThongKeHangMuc = new JLabel("Tổng số: 0 hạng mục (Sửa chữa: 0 | Phụ tùng: 0)");

    // Cờ kiểm soát nạp dữ liệu combobox
    private boolean dangNap = false;

    public LapPhieuBaoHanh_UI(NhanVien nhanVien) {
        this.nhanVien = nhanVien;

        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(12, 14, 12, 14));

        add(taoHeader(), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);

        moKhoaInput();

        if (spThoiHan.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.getTextField().setHorizontalAlignment(JTextField.LEFT);
            editor.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }

        // Sự kiện đổi hóa đơn -> tự động nạp thông tin và hạng mục đề xuất
        cboHoaDon.addActionListener(e -> {
            if (!dangNap) {
                napThongTinHoaDon();
            }
        });

        // Sự kiện đổi loại hạng mục -> gợi ý thời hạn & điều kiện mặc định
        cboLoaiHangMuc.addActionListener(e -> capNhatGoiYTheoLoaiHangMuc());

        napDuLieu();
    }

    // ===============================================================
    // 1. HEADER MÀN HÌNH
    // ===============================================================
    private JPanel taoHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("LẬP PHIẾU BẢO HÀNH DỊCH VỤ & PHỤ TÙNG", UiIcon.of(UiIcon.SHIELD, 20, BLUE), SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(NAVY);

        JLabel sub = new JLabel("Nêu rõ chi tiết các hạng mục bảo hành về công việc sửa chữa và linh kiện phụ tùng thay thế cho khách hàng");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));

        labels.add(title);
        labels.add(Box.createVerticalStrut(3));
        labels.add(sub);
        p.add(labels, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnMoi = UiHelper.nut(" Làm mới", UiIcon.of(UiIcon.REFRESH, 13), UiHelper.XAM, e -> lamMoi());
        JButton btnLuu = UiHelper.nut(" Lưu phiếu bảo hành", UiIcon.of(UiIcon.CHECK, 13), GREEN, true, e -> luuPhieu());
        JButton btnIn = UiHelper.nut(" In phiếu bảo hành", UiIcon.of(UiIcon.PRINT, 13), new Color(194, 65, 12), e -> inPhieu());

        actions.add(btnMoi);
        actions.add(btnLuu);
        actions.add(btnIn);

        p.add(actions, BorderLayout.EAST);
        return p;
    }

    // ===============================================================
    // 2. KHU VỰC NỘI DUNG CHÍNH (FORM + BẢNG)
    // ===============================================================
    private JPanel taoNoiDung() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 10, 0));
        top.setOpaque(false);
        top.add(taoThongTinPhieu());
        top.add(taoThongTinHoaDon());

        content.add(top, BorderLayout.NORTH);
        content.add(taoChiTietHangMuc(), BorderLayout.CENTER);

        return content;
    }

    // --- Khối 1: Thông tin phiếu bảo hành ---
    private JPanel taoThongTinPhieu() {
        JPanel card = card();
        card.add(tieuDe("1. THÔNG TIN PHIẾU BẢO HÀNH"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        themTruong(form, 0, "Mã phiếu bảo hành", txtMaPBH);
        themTruong(form, 1, "Ngày lập", txtNgayLap);
        themTruong(form, 2, "Ngày bắt đầu bảo hành", txtNgayBatDau);
        themTruong(form, 3, "Nhân viên phụ trách", cboNhanVien);
        themTruong(form, 4, "Tình trạng phiếu", cboTrangThai);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    // --- Khối 2: Thông tin hóa đơn & khách hàng ---
    private JPanel taoThongTinHoaDon() {
        JPanel card = card();
        card.add(tieuDe("2. THÔNG TIN HÓA ĐƠN & PHƯƠNG TIỆN"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JPanel pnlChonHD = new JPanel(new BorderLayout(6, 0));
        pnlChonHD.setOpaque(false);
        pnlChonHD.add(cboHoaDon, BorderLayout.CENTER);

        JButton btnTaiLaiHD = new JButton("Tải lại HĐ", UiIcon.of(UiIcon.REFRESH, 12, BLUE));
        btnTaiLaiHD.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTaiLaiHD.setForeground(BLUE);
        btnTaiLaiHD.setBackground(new Color(239, 246, 255));
        btnTaiLaiHD.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        btnTaiLaiHD.setFocusPainted(false);
        btnTaiLaiHD.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTaiLaiHD.setToolTipText("Trích xuất lại toàn bộ các mục sửa chữa và phụ tùng từ hóa đơn đã chọn");
        btnTaiLaiHD.addActionListener(e -> napThongTinHoaDon());
        pnlChonHD.add(btnTaiLaiHD, BorderLayout.EAST);

        themTruong(form, 0, "Chọn hóa đơn *", pnlChonHD);
        themTruong(form, 1, "Chủ xe / Khách hàng", txtKhachHang);
        themTruong(form, 2, "Biển số xe tiếp nhận", txtBienSoXe);
        themTruong(form, 3, "Tổng chi phí hóa đơn", txtTongTien);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    // --- Khối 3: Chi tiết các hạng mục bảo hành (Sửa chữa & Phụ tùng) ---
    private JPanel taoChiTietHangMuc() {
        JPanel card = card();

        JPanel topCard = new JPanel(new BorderLayout(8, 0));
        topCard.setOpaque(false);
        topCard.add(tieuDe("3. CÁC MỤC BẢO HÀNH CHO KHÁCH HÀNG (SỬA CHỮA & PHỤ TÙNG)"), BorderLayout.WEST);

        lblThongKeHangMuc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblThongKeHangMuc.setForeground(new Color(30, 64, 175));
        topCard.add(lblThongKeHangMuc, BorderLayout.EAST);
        card.add(topCard, BorderLayout.NORTH);

        // Model bảng 7 cột rõ ràng
        model = new DefaultTableModel(
                new Object[]{
                        "STT", "LOẠI BẢO HÀNH", "TÊN HẠNG MỤC BẢO HÀNH", "THỜI HẠN (THÁNG)",
                        "NGÀY HẾT HẠN", "ĐIỀU KIỆN BẢO HÀNH", "GHI CHÚ"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1 || col == 2 || col == 3 || col == 5 || col == 6;
            }
        };

        tblChiTiet = new JTable(model);
        tblChiTiet.setRowHeight(32);
        tblChiTiet.setGridColor(BORDER);
        tblChiTiet.setShowGrid(true);
        tblChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblChiTiet.getTableHeader().setBackground(new Color(241, 245, 249));
        tblChiTiet.getTableHeader().setForeground(NAVY);

        // Định dạng độ rộng cột
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(45);   // STT
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(145);  // Loại bảo hành
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(260);  // Tên hạng mục
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(110);  // Thời hạn (tháng)
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(110);  // Ngày hết hạn
        tblChiTiet.getColumnModel().getColumn(5).setPreferredWidth(270);  // Điều kiện bảo hành
        tblChiTiet.getColumnModel().getColumn(6).setPreferredWidth(140);  // Ghi chú

        // Căn giữa STT, Thời hạn, Ngày hết hạn
        DefaultTableCellRenderer rCenter = new DefaultTableCellRenderer();
        rCenter.setHorizontalAlignment(SwingConstants.CENTER);
        tblChiTiet.getColumnModel().getColumn(0).setCellRenderer(rCenter);
        tblChiTiet.getColumnModel().getColumn(3).setCellRenderer(rCenter);
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(rCenter);

        // Renderer Badge màu sắc cho cột LOẠI BẢO HÀNH (Sửa chữa vs Phụ tùng)
        tblChiTiet.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String loai = value != null ? value.toString() : "";
                boolean laSC = loai.toLowerCase().contains("sửa chữa") || loai.toLowerCase().contains("dịch vụ");

                if (!isSelected) {
                    if (laSC) {
                        l.setBackground(new Color(239, 246, 255)); // Blue nhạt
                        l.setForeground(new Color(29, 78, 216));    // Blue đậm
                    } else {
                        l.setBackground(new Color(240, 253, 244)); // Green nhạt
                        l.setForeground(new Color(21, 128, 61));    // Green đậm
                    }
                }
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setBorder(new EmptyBorder(4, 8, 4, 8));
                return l;
            }
        });

        // Editor ComboBox cho cột Loại bảo hành
        JComboBox<String> editorComboLoai = new JComboBox<>(new String[]{
                ChiTietPhieuBaoHanh.LOAI_SUA_CHUA,
                ChiTietPhieuBaoHanh.LOAI_PHU_TUNG
        });
        tblChiTiet.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(editorComboLoai));

        // Khi chỉnh sửa thời hạn trong bảng -> tự động tính lại ngày hết hạn
        model.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                if (e.getColumn() == 3 && e.getFirstRow() >= 0) {
                    tinhNgayHetHan(e.getFirstRow());
                }
                capNhatThongKeHangMuc();
            }
        });

        // Form thêm nhanh hạng mục
        JPanel formThem = new JPanel(new GridBagLayout());
        formThem.setOpaque(false);
        formThem.setBorder(new EmptyBorder(8, 0, 8, 0));

        cboLoaiHangMuc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cboLoaiHangMuc.setPreferredSize(new Dimension(190, 32));
        cboLoaiHangMuc.setBackground(Color.WHITE);

        // Khối thời hạn bảo hành + nút nhanh
        JPanel pnlThoiHanWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlThoiHanWrap.setOpaque(false);
        spThoiHan.setPreferredSize(new Dimension(80, 30));
        pnlThoiHanWrap.add(spThoiHan);
        pnlThoiHanWrap.add(new JLabel("tháng "));
        pnlThoiHanWrap.add(taoNutNhanhThang("3T", 3));
        pnlThoiHanWrap.add(taoNutNhanhThang("6T", 6));
        pnlThoiHanWrap.add(taoNutNhanhThang("12T", 12));
        pnlThoiHanWrap.add(taoNutNhanhThang("24T", 24));

        themTruong(formThem, 0, "Loại bảo hành *", cboLoaiHangMuc);
        themTruong(formThem, 1, "Tên hạng mục *", txtTenHangMuc);
        themTruong(formThem, 2, "Thời hạn bảo hành", pnlThoiHanWrap);
        themTruong(formThem, 3, "Điều kiện bảo hành", txtDieuKien);
        themTruong(formThem, 4, "Ghi chú hạng mục", txtGhiChuCT);

        capNhatGoiYTheoLoaiHangMuc();

        JButton btnThem = UiHelper.nut(" + Thêm hạng mục", UiIcon.of(UiIcon.PLUS, 12), BLUE, true, e -> themHangMuc());
        JButton btnXoa = UiHelper.nut(" Xóa dòng", UiIcon.of(UiIcon.TRASH, 12), new Color(220, 38, 38), e -> xoaHangMuc());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(btnThem);
        actions.add(btnXoa);

        JPanel pnlTren = new JPanel(new BorderLayout(0, 4));
        pnlTren.setOpaque(false);
        pnlTren.add(formThem, BorderLayout.CENTER);
        pnlTren.add(actions, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(pnlTren, BorderLayout.NORTH);

        JScrollPane spBang = new JScrollPane(tblChiTiet);
        spBang.setBorder(new LineBorder(BORDER, 1, true));
        center.add(spBang, BorderLayout.CENTER);

        // Ghi chú chung phiếu
        JPanel note = new JPanel(new BorderLayout(0, 4));
        note.setOpaque(false);
        JLabel lblGhiChuChung = new JLabel("Cam kết & Chính sách bảo hành chung tại xưởng");
        lblGhiChuChung.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGhiChuChung.setForeground(NAVY);
        note.add(lblGhiChuChung, BorderLayout.NORTH);

        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setText("Cam kết bảo hành chu đáo, khắc phục nhanh chóng và minh bạch theo quy chuẩn dịch vụ của Gara Ô tô The T4.");
        JScrollPane spGhiChu = new JScrollPane(txtGhiChu);
        spGhiChu.setBorder(new LineBorder(BORDER, 1, true));
        note.add(spGhiChu, BorderLayout.CENTER);

        center.add(note, BorderLayout.SOUTH);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    private JButton taoNutNhanhThang(String label, int soThang) {
        JButton b = new JButton(label);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setForeground(new Color(71, 85, 105));
        b.setBackground(new Color(241, 245, 249));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(3, 7, 3, 7)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            spThoiHan.setValue(soThang);
            tinhLaiNgayHetHanTruongForm();
        });
        return b;
    }

    private void capNhatGoiYTheoLoaiHangMuc() {
        String loai = (String) cboLoaiHangMuc.getSelectedItem();
        boolean laSC = loai != null && loai.contains("sửa chữa");
        if (laSC) {
            spThoiHan.setValue(3);
            txtDieuKien.setText("Bảo hành chất lượng tay nghề, kỹ thuật thi công & căn chỉnh tại gara The T4 (không do va chạm/ngoại lực)");
            txtTenHangMuc.putClientProperty("placeholder", "VD: Kiểm tra, thay thế và chạy thử / Cân chỉnh góc lái...");
        } else {
            spThoiHan.setValue(6);
            txtDieuKien.setText("Bảo hành lỗi kỹ thuật vật tư / phụ tùng chính hãng từ nhà sản xuất (còn nguyên tem niêm phong)");
            txtTenHangMuc.putClientProperty("placeholder", "VD: Lọc dầu động cơ / Má phanh trước / Bugi Iridium...");
        }
    }

    private void tinhLaiNgayHetHanTruongForm() {
        // Tùy chỉnh tự động
    }

    // ===============================================================
    // 3. KHÓA / MỞ KHÓA TRƯỜNG DỮ LIỆU
    // ===============================================================
    private void moKhoaInput() {
        txtMaPBH.setEditable(false);
        txtMaPBH.setBackground(new Color(241, 245, 249));

        txtNgayLap.setEditable(false);
        txtNgayLap.setBackground(new Color(241, 245, 249));

        txtKhachHang.setEditable(false);
        txtKhachHang.setBackground(new Color(241, 245, 249));

        txtBienSoXe.setEditable(false);
        txtBienSoXe.setBackground(new Color(241, 245, 249));

        txtTongTien.setEditable(false);
        txtTongTien.setBackground(new Color(241, 245, 249));

        txtNgayBatDau.setEditable(true);
        txtTenHangMuc.setEditable(true);
        txtDieuKien.setEditable(true);
        txtGhiChuCT.setEditable(true);
        txtGhiChu.setEditable(true);

        cboHoaDon.setEnabled(true);
        cboTrangThai.setEnabled(true);
        spThoiHan.setEnabled(true);
    }

    // ===============================================================
    // 4. NẠP DỮ LIỆU TỪ CSDL SQL SERVER
    // ===============================================================
    private void napDuLieu() {
        dangNap = true;
        try {
            txtMaPBH.setText(dao.taoMaPhieuMoi());
            txtNgayLap.setText(UiHelper.dinhDangNgay(LocalDate.now()));
            txtNgayBatDau.setText(UiHelper.dinhDangNgay(LocalDate.now()));
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

    /**
     * Nạp chi tiết hóa đơn được chọn và tự động trích xuất các mục sửa chữa & phụ tùng
     */
    private void napThongTinHoaDon() {
        LuaChon hoaDon = (LuaChon) cboHoaDon.getSelectedItem();

        txtKhachHang.setText("");
        txtBienSoXe.setText("");
        txtTongTien.setText("");
        model.setRowCount(0);

        if (hoaDon == null) {
            capNhatThongKeHangMuc();
            return;
        }

        String sql = "SELECT k.hoTen, k.soDT, h.tienDichVu, h.tienPhuTung, h.tongTien, pt.bienSoXe, pt.hangXe, pt.dongXe "
                + "FROM dbo.HoaDon h "
                + "LEFT JOIN dbo.KhachHang k ON k.maKH = h.maKH "
                + "LEFT JOIN dbo.PhuongTien pt ON pt.maKH = h.maKH "
                + "WHERE h.maHD = ? "
                + "ORDER BY pt.bienSoXe";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hoaDon.ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    thongBao("Không tìm thấy thông tin chi tiết cho hóa đơn: " + hoaDon.ma);
                    return;
                }

                String tenKH = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                long tienDV = rs.getLong("tienDichVu");
                long tienPT = rs.getLong("tienPhuTung");
                long tong = rs.getLong("tongTien");

                txtKhachHang.setText((tenKH != null ? tenKH : "Khách hàng") + (sdt != null && !sdt.isBlank() ? " (" + sdt + ")" : ""));
                txtTongTien.setText(String.format("%,d đ  (Công sửa chữa: %,d đ | Phụ tùng: %,d đ)", tong, tienDV, tienPT));

                List<String> danhSachXe = new ArrayList<>();
                do {
                    String bienSo = rs.getString("bienSoXe");
                    String hang = rs.getString("hangXe");
                    String dong = rs.getString("dongXe");
                    if (bienSo != null && !bienSo.trim().isEmpty() && !danhSachXe.contains(bienSo)) {
                        danhSachXe.add(bienSo + (hang != null && !hang.isBlank() ? " (" + hang + " " + dong + ")" : ""));
                    }
                } while (rs.next());

                if (!danhSachXe.isEmpty()) {
                    if (danhSachXe.size() == 1) {
                        String bsRaw = danhSachXe.get(0).split(" ")[0];
                        txtBienSoXe.setText(bsRaw);
                    } else {
                        Object xeDuocChon = JOptionPane.showInputDialog(
                                this,
                                "Khách hàng có nhiều phương tiện trong hệ thống.\nVui lòng chọn phương tiện được bảo hành:",
                                "Chọn phương tiện bảo hành",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                danhSachXe.toArray(),
                                danhSachXe.get(0)
                        );
                        if (xeDuocChon != null) {
                            String bsRaw = xeDuocChon.toString().split(" ")[0];
                            txtBienSoXe.setText(bsRaw);
                        }
                    }
                }
            }

            // TỰ ĐỘNG TRÍCH XUẤT CÁC HẠNG MỤC SỬA CHỮA & PHỤ TÙNG TỪ HÓA ĐƠN
            napHangMucTuHoaDon(hoaDon.ma);

        } catch (SQLException ex) {
            baoLoi("Lỗi khi tải thông tin hóa đơn.", ex);
        }
    }

    /**
     * Tự động quét và nạp các mục bảo hành từ hóa đơn (cả tiền công sửa chữa và phụ tùng)
     */
    private void napHangMucTuHoaDon(String maHD) {
        model.setRowCount(0);
        List<ChiTietPhieuBaoHanh> dsGoiY = dao.layGoiYHangMucTuHoaDon(maHD);

        LocalDate ngayBatDau = UiHelper.chuyenChuoiSangNgay(txtNgayBatDau.getText().trim(), null, "Ngày bắt đầu");
        if (ngayBatDau == null) ngayBatDau = LocalDate.now();

        int stt = 1;
        for (ChiTietPhieuBaoHanh ct : dsGoiY) {
            int thang = ct.getThoiHanBaoHanh();
            LocalDate hetHan = ngayBatDau.plusMonths(thang);

            model.addRow(new Object[]{
                    stt++,
                    ct.getLoaiHangMuc(),
                    ct.getTenHangMuc(),
                    thang,
                    UiHelper.dinhDangNgay(hetHan),
                    ct.getDieuKienBaoHanh(),
                    ct.getGhiChu()
            });
        }

        capNhatThongKeHangMuc();
    }

    private void napDanhSachNhanVien() throws SQLException {
        String sql = "SELECT maNV, hoTen, chucVu FROM dbo.NhanVien ORDER BY hoTen";
        cboNhanVien.removeAllItems();

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maNV = rs.getString("maNV");
                String hoTen = rs.getString("hoTen");
                String cv = rs.getString("chucVu");
                cboNhanVien.addItem(new LuaChon(maNV, hoTen + (cv != null ? " (" + cv + ")" : "")));
            }
        }
    }

    // ===============================================================
    // 5. THAO TÁC HẠNG MỤC (THÊM, XÓA, TÍNH TOÁN)
    // ===============================================================
    private void themHangMuc() {
        String loai = (String) cboLoaiHangMuc.getSelectedItem();
        String ten = txtTenHangMuc.getText().trim();

        if (ten.isEmpty()) {
            thongBao("Vui lòng nhập tên hạng mục bảo hành.");
            txtTenHangMuc.requestFocusInWindow();
            return;
        }

        LocalDate bd = UiHelper.chuyenChuoiSangNgay(txtNgayBatDau.getText().trim(), this, "Ngày bắt đầu");
        if (bd == null) {
            txtNgayBatDau.requestFocusInWindow();
            return;
        }

        try {
            int thang = (Integer) spThoiHan.getValue();
            LocalDate hetHan = bd.plusMonths(thang);

            model.addRow(new Object[]{
                    model.getRowCount() + 1,
                    loai,
                    ten,
                    thang,
                    UiHelper.dinhDangNgay(hetHan),
                    txtDieuKien.getText().trim(),
                    txtGhiChuCT.getText().trim()
            });

            txtTenHangMuc.setText("");
            txtGhiChuCT.setText("");
            txtTenHangMuc.requestFocusInWindow();
            capNhatGoiYTheoLoaiHangMuc();
            capNhatThongKeHangMuc();

        } catch (Exception ex) {
            thongBao("Lỗi khi thêm hạng mục: " + ex.getMessage());
        }
    }

    private void tinhNgayHetHan(int row) {
        if (row < 0 || row >= model.getRowCount()) return;
        try {
            LocalDate ngayBatDau = UiHelper.chuyenChuoiSangNgay(txtNgayBatDau.getText().trim(), null, "Ngày bắt đầu");
            if (ngayBatDau == null) ngayBatDau = LocalDate.now();

            int thoiHan = Integer.parseInt(model.getValueAt(row, 3).toString());
            if (thoiHan > 0) {
                LocalDate ngayHetHan = ngayBatDau.plusMonths(thoiHan);
                model.setValueAt(UiHelper.dinhDangNgay(ngayHetHan), row, 4);
            }
        } catch (Exception ignored) {}
    }

    private void xoaHangMuc() {
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            thongBao("Vui lòng chọn một dòng hạng mục trong bảng để xóa.");
            return;
        }

        model.removeRow(tblChiTiet.convertRowIndexToModel(row));
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }
        capNhatThongKeHangMuc();
    }

    private void capNhatThongKeHangMuc() {
        int sc = 0;
        int pt = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 1);
            if (v != null && (v.toString().contains("sửa chữa") || v.toString().contains("dịch vụ"))) {
                sc++;
            } else {
                pt++;
            }
        }
        lblThongKeHangMuc.setText(String.format("Tổng số: %d hạng mục  |  Dịch vụ sửa chữa: %d mục  |  Phụ tùng & Linh kiện: %d món", model.getRowCount(), sc, pt));
    }

    // ===============================================================
    // 6. LƯU PHIẾU BẢO HÀNH
    // ===============================================================
    private void luuPhieu() {
        if (!BaoVeQuyen.yeuCau(this, Entity.QuyenTruyCap.Quyen.BAO_HANH)) return;
        if (tblChiTiet.isEditing() && !tblChiTiet.getCellEditor().stopCellEditing()) {
            return;
        }

        LuaChon hd = (LuaChon) cboHoaDon.getSelectedItem();
        if (hd == null) {
            thongBao("Vui lòng chọn hóa đơn cần lập phiếu bảo hành.");
            return;
        }

        LuaChon nv = (LuaChon) cboNhanVien.getSelectedItem();
        if (nv == null) {
            thongBao("Vui lòng chọn nhân viên phụ trách lập phiếu.");
            return;
        }

        if (model.getRowCount() == 0) {
            thongBao("Phiếu bảo hành phải có ít nhất một hạng mục sửa chữa hoặc phụ tùng.");
            return;
        }

        try {
            LocalDate ngayLap = LocalDate.now();
            LocalDate ngayBD = UiHelper.chuyenChuoiSangNgay(txtNgayBatDau.getText().trim(), this, "Ngày bắt đầu");
            if (ngayBD == null) {
                txtNgayBatDau.requestFocusInWindow();
                return;
            }

            if (ngayBD.isBefore(ngayLap)) {
                thongBao("Ngày bắt đầu hiệu lực bảo hành không được trước ngày lập phiếu.");
                return;
            }

            List<ChiTietPhieuBaoHanh> ds = new ArrayList<>();
            int demSC = 0;
            int demPT = 0;

            for (int i = 0; i < model.getRowCount(); i++) {
                String loai = String.valueOf(model.getValueAt(i, 1)).trim();
                String ten = String.valueOf(model.getValueAt(i, 2)).trim();
                int thang = Integer.parseInt(model.getValueAt(i, 3).toString());

                if (ten.isEmpty() || thang <= 0) {
                    thongBao("Hạng mục ở dòng " + (i + 1) + " không hợp lệ.");
                    return;
                }

                Date hetHan = Date.valueOf(ngayBD.plusMonths(thang));
                String dieuKien = String.valueOf(model.getValueAt(i, 5) == null ? "" : model.getValueAt(i, 5));
                String ghiChuCT = String.valueOf(model.getValueAt(i, 6) == null ? "" : model.getValueAt(i, 6));

                ChiTietPhieuBaoHanh ct = new ChiTietPhieuBaoHanh("", "", loai, ten, thang,
                        hetHan.toLocalDate(), dieuKien, ghiChuCT, loai, "");
                ds.add(ct);

                if (ct.laSuaChua()) demSC++;
                else demPT++;
            }

            String maPBH = dao.taoMaPhieuMoi();
            int chon = JOptionPane.showConfirmDialog(
                    this,
                    "XÁC NHẬN LƯU PHIẾU BẢO HÀNH GARA THE T4:\n\n"
                            + "• Mã phiếu:           " + maPBH + "\n"
                            + "• Hóa đơn liên kết:   " + hd.ma + "\n"
                            + "• Khách hàng:         " + txtKhachHang.getText() + "\n"
                            + "• Biển số xe:         " + txtBienSoXe.getText() + "\n"
                            + "• Hạng mục sửa chữa:  " + demSC + " mục\n"
                            + "• Phụ tùng linh kiện: " + demPT + " món\n\n"
                            + "Bạn có chắc chắn muốn lưu phiếu bảo hành này vào hệ thống?",
                    "Xác nhận lưu phiếu bảo hành",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
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
            maPhieuVuaLuu = maPBH;
            KhoDuLieu.get().taiLaiBaoHanh();

            int optIn = JOptionPane.showConfirmDialog(
                    this,
                    "Lưu phiếu bảo hành thành công!\nMã phiếu: " + maPBH + "\n\nBạn có muốn mở bản in / xem trước phiếu bảo hành cho khách ngay không?",
                    "Thành công",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (optIn == JOptionPane.YES_OPTION) {
                inPhieuTheoMa(maPBH);
            }

            lamMoi();

        } catch (Exception ex) {
            baoLoi("Không thể lưu phiếu bảo hành.", ex);
        }
    }

    // ===============================================================
    // 7. IN PHIẾU BẢO HÀNH (CHUẨN GARA, NÊU RÕ SỬA CHỮA & PHỤ TÙNG)
    // ===============================================================
    private void inPhieu() {
        if (!maPhieuVuaLuu.isEmpty()) {
            inPhieuTheoMa(maPhieuVuaLuu);
            return;
        }

        // Cho phép chọn một mã phiếu bảo hành đã lưu để in
        String nhapMa = JOptionPane.showInputDialog(this, "Nhập mã phiếu bảo hành cần in (VD: PBH00001):", "In phiếu bảo hành", JOptionPane.QUESTION_MESSAGE);
        if (nhapMa != null && !nhapMa.trim().isEmpty()) {
            inPhieuTheoMa(nhapMa.trim());
        }
    }

    public void inPhieuTheoMa(String maPBH) {
        try {
            ThongTinIn t = dao.layThongTinIn(maPBH);
            List<ChiTiet> dsSuaChua = t.getChiTietSuaChua();
            List<ChiTiet> dsPhuTung = t.getChiTietPhuTung();

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            html.append("<style>");
            html.append("body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; font-size: 11pt; color: #1e293b; margin: 20px; line-height: 1.45; }");
            html.append(".header-box { border-bottom: 2px solid #2563eb; padding-bottom: 10px; margin-bottom: 12px; }");
            html.append(".brand { font-size: 16pt; font-weight: bold; color: #1e3a8a; text-transform: uppercase; }");
            html.append(".brand-sub { font-size: 9.5pt; color: #64748b; }");
            html.append(".title { text-align: center; font-size: 16pt; font-weight: bold; color: #0f172a; margin: 12px 0 2px 0; text-transform: uppercase; }");
            html.append(".meta { text-align: center; font-size: 10pt; color: #475569; margin-bottom: 14px; }");
            html.append(".info-table { width: 100%; border-collapse: collapse; margin-bottom: 14px; background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 6px; }");
            html.append(".info-table td { padding: 6px 10px; font-size: 10pt; border: none; }");
            html.append(".sec-header { font-size: 11pt; font-weight: bold; color: #1e3a8a; margin: 12px 0 5px 0; text-transform: uppercase; border-bottom: 1px solid #93c5fd; padding-bottom: 3px; }");
            html.append(".data-table { width: 100%; border-collapse: collapse; margin-bottom: 12px; }");
            html.append(".data-table th { background: #f1f5f9; color: #1e293b; font-weight: bold; border: 1px solid #94a3b8; padding: 6px 8px; font-size: 9.5pt; }");
            html.append(".data-table td { border: 1px solid #cbd5e1; padding: 6px 8px; font-size: 9.5pt; }");
            html.append(".center { text-align: center; }");
            html.append(".badge-sc { font-weight: bold; color: #1d4ed8; }");
            html.append(".badge-pt { font-weight: bold; color: #15803d; }");
            html.append(".policy-box { border: 1px dashed #94a3b8; background: #fafaf9; padding: 8px 12px; font-size: 9pt; color: #475569; margin-bottom: 16px; border-radius: 4px; }");
            html.append(".policy-box ul { margin: 3px 0 3px 18px; padding: 0; }");
            html.append(".sign-table { width: 100%; border-collapse: collapse; margin-top: 15px; border: none; }");
            html.append(".sign-table td { border: none; text-align: center; width: 33.3%; vertical-align: top; font-size: 9.5pt; }");
            html.append("</style></head><body>");

            // Header Gara
            html.append("<div class='header-box'>");
            html.append("<table style='width:100%; border:none;'><tr>");
            html.append("<td style='border:none;'><div class='brand'>GARA Ô TÔ THE T4</div>");
            html.append("<div class='brand-sub'>Địa chỉ: Khu Công Nghệ Cao, TP. Thủ Đức, TP. Hồ Chí Minh<br>Hotline dịch vụ: 1900 6868 — 0918 200 120 | Email: hotro@thet4auto.vn</div></td>");
            html.append("<td style='border:none; text-align:right;'><div style='font-size:10pt;'><b>Mã phiếu:</b> <span style='color:#2563eb; font-weight:bold;'>")
                    .append(escape(t.maPBH)).append("</span><br><b>Hóa đơn:</b> ")
                    .append(escape(t.maHD)).append("<br><b>Ngày lập:</b> ")
                    .append(t.ngayLap != null ? UiHelper.dinhDangNgay(t.ngayLap.toLocalDate()) : "").append("</div></td>");
            html.append("</tr></table></div>");

            // Tiêu đề
            html.append("<div class='title'>PHIẾU BẢO HÀNH DỊCH VỤ SỬA CHỮA & PHỤ TÙNG</div>");
            html.append("<div class='meta'>Thời hạn hiệu lực từ ngày: <b>")
                    .append(t.ngayBatDau != null ? UiHelper.dinhDangNgay(t.ngayBatDau.toLocalDate()) : "")
                    .append("</b> — Trạng thái: <b style='color:#16a34a;'>").append(escape(t.trangThai)).append("</b></div>");

            // Khối Thông tin khách hàng & Phương tiện
            html.append("<table class='info-table'><tr>");
            html.append("<td><b>Khách hàng:</b> ").append(escape(t.tenKhachHang)).append("</td>");
            html.append("<td><b>Biển số xe:</b> <b style='color:#1e3a8a;'>").append(escape(t.bienSoXe)).append("</b></td>");
            html.append("</tr><tr>");
            html.append("<td><b>Mã khách hàng:</b> ").append(escape(t.maKH)).append("</td>");
            html.append("<td><b>Cố vấn / NV lập:</b> ").append(escape(t.tenNhanVien)).append("</td>");
            html.append("</tr></table>");

            // -------------------------------------------------------------
            // PHẦN I: DANH MỤC DỊCH VỤ SỬA CHỮA ĐƯỢC BẢO HÀNH
            // -------------------------------------------------------------
            html.append("<div class='sec-header'>I. DANH MỤC CÔNG VIỆC DỊCH VỤ SỬA CHỮA ĐƯỢC BẢO HÀNH</div>");
            if (dsSuaChua.isEmpty()) {
                html.append("<p style='font-style:italic; color:#64748b; margin:4px 0 10px 0;'>Hóa đơn này không bao gồm hạng mục dịch vụ sửa chữa độc lập.</p>");
            } else {
                html.append("<table class='data-table'><tr>");
                html.append("<th style='width:35px;' class='center'>STT</th>");
                html.append("<th style='width:240px;'>Nội dung công việc sửa chữa</th>");
                html.append("<th style='width:80px;' class='center'>Thời hạn</th>");
                html.append("<th style='width:90px;' class='center'>Ngày hết hạn</th>");
                html.append("<th>Phạm vi & Điều kiện bảo hành tay nghề</th>");
                html.append("</tr>");

                int idx = 1;
                for (ChiTiet ct : dsSuaChua) {
                    html.append("<tr>");
                    html.append("<td class='center'>").append(idx++).append("</td>");
                    html.append("<td><b class='badge-sc'>[Sửa chữa]</b> ").append(escape(ct.tenHangMuc)).append("</td>");
                    html.append("<td class='center'>").append(ct.thoiHan).append(" tháng</td>");
                    html.append("<td class='center'>").append(ct.ngayHetHan != null ? UiHelper.dinhDangNgay(ct.ngayHetHan.toLocalDate()) : "").append("</td>");
                    html.append("<td>").append(escape(ct.dieuKien)).append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }

            // -------------------------------------------------------------
            // PHẦN II: DANH MỤC PHỤ TÙNG & LINH KIỆN THAY THẾ
            // -------------------------------------------------------------
            html.append("<div class='sec-header'>II. DANH MỤC PHỤ TÙNG & LINH KIỆN THAY THẾ ĐƯỢC BẢO HÀNH</div>");
            if (dsPhuTung.isEmpty()) {
                html.append("<p style='font-style:italic; color:#64748b; margin:4px 0 10px 0;'>Hóa đơn này không bao gồm phụ tùng thay thế được bảo hành.</p>");
            } else {
                html.append("<table class='data-table'><tr>");
                html.append("<th style='width:35px;' class='center'>STT</th>");
                html.append("<th style='width:240px;'>Tên phụ tùng / Vật tư thay thế</th>");
                html.append("<th style='width:80px;' class='center'>Thời hạn</th>");
                html.append("<th style='width:90px;' class='center'>Ngày hết hạn</th>");
                html.append("<th>Tiêu chuẩn & Điều kiện bảo hành vật tư</th>");
                html.append("</tr>");

                int idx = 1;
                for (ChiTiet ct : dsPhuTung) {
                    html.append("<tr>");
                    html.append("<td class='center'>").append(idx++).append("</td>");
                    html.append("<td><b class='badge-pt'>[Phụ tùng]</b> ").append(escape(ct.tenHangMuc))
                            .append(ct.maPT != null && !ct.maPT.isBlank() && !ct.maPT.startsWith("DV") ? " <span style='font-size:8.5pt;color:#64748b;'>(" + ct.maPT + ")</span>" : "")
                            .append("</td>");
                    html.append("<td class='center'>").append(ct.thoiHan).append(" tháng</td>");
                    html.append("<td class='center'>").append(ct.ngayHetHan != null ? UiHelper.dinhDangNgay(ct.ngayHetHan.toLocalDate()) : "").append("</td>");
                    html.append("<td>").append(escape(ct.dieuKien)).append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }

            // -------------------------------------------------------------
            // PHẦN III: CHÍNH SÁCH VÀ ĐIỀU KIỆN BẢO HÀNH CHUNG
            // -------------------------------------------------------------
            html.append("<div class='policy-box'>");
            html.append("<b>III. ĐIỀU KHOẢN VÀ CHÍNH SÁCH BẢO HÀNH TẠI GARA THE T4:</b>");
            html.append("<ul>");
            html.append("<li><b>Bảo hành dịch vụ sửa chữa:</b> Gara cam kết bảo hành miễn phí 100% công khắc phục nếu phát sinh lỗi kỹ thuật liên quan đến tay nghề lắp ráp, căn chỉnh của kỹ thuật viên trong thời hạn hiệu lực.</li>");
            html.append("<li><b>Bảo hành phụ tùng linh kiện:</b> Áp dụng đổi mới hoặc sửa chữa theo chính sách chính hãng đối với các lỗi vật liệu hoặc khuyết tật do nhà sản xuất (tem bảo hành còn nguyên vẹn, không bị biến dạng).</li>");
            html.append("<li><b>Các trường hợp từ chối bảo hành:</b> Xe bị tai nạn đâm đụng, ngập nước, sử dụng sai nhiên liệu/dầu nhờn khuyến cáo, tự ý tháo dỡ hoặc sửa chữa tại các cơ sở khác ngoài hệ thống Gara The T4.</li>");
            if (t.ghiChu != null && !t.ghiChu.isBlank()) {
                html.append("<li><b>Ghi chú đặc biệt:</b> ").append(escape(t.ghiChu)).append("</li>");
            }
            html.append("</ul></div>");

            // Phần ký tên xác nhận 3 bên
            html.append("<table class='sign-table'><tr>");
            html.append("<td><b>Khách hàng nhận xe</b><br><span style='font-size:8.5pt;color:#64748b;'>(Ký và ghi rõ họ tên)</span><br><br><br><br><b>")
                    .append(escape(t.tenKhachHang)).append("</b></td>");
            html.append("<td><b>Kỹ thuật viên phụ trách</b><br><span style='font-size:8.5pt;color:#64748b;'>(Ký và xác nhận kiểm thử)</span><br><br><br><br><b>KTV Xưởng Dịch Vụ</b></td>");
            html.append("<td><b>Đại diện Gara Ô tô The T4</b><br><span style='font-size:8.5pt;color:#64748b;'>(Cố vấn dịch vụ / Trưởng xưởng)</span><br><br><br><br><b>")
                    .append(escape(t.tenNhanVien)).append("</b></td>");
            html.append("</tr></table>");

            html.append("</body></html>");

            hienThiXemTruocVaIn(html.toString(), "Phiếu bảo hành — " + t.maPBH);

        } catch (SQLException ex) {
            baoLoi("Không tìm thấy dữ liệu phiếu bảo hành để in.", ex);
        }
    }

    private void hienThiXemTruocVaIn(String htmlContent, String tieuDeCuaSo) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWindow, tieuDeCuaSo, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(850, 720);
        dlg.setLocationRelativeTo(parentWindow);
        dlg.setLayout(new BorderLayout());

        JEditorPane editor = new JEditorPane("text/html", htmlContent);
        editor.setEditable(false);
        editor.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(editor);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBottom.setBackground(new Color(241, 245, 249));
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton btnDong = UiHelper.nut("Đóng", UiHelper.XAM, e -> dlg.dispose());
        JButton btnThucHienIn = UiHelper.nut(" In văn bản (Ctrl+P)", UiIcon.of(UiIcon.PRINT, 14), BLUE, true, e -> {
            try {
                editor.print(null, null, true, null, null, true);
            } catch (PrinterException ex) {
                baoLoi("Lỗi khi gửi lệnh in đến máy in.", ex);
            }
        });

        pnlBottom.add(btnDong);
        pnlBottom.add(btnThucHienIn);
        dlg.add(pnlBottom, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private String escape(Object value) {
        if (value == null) return "";
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // ===============================================================
    // 8. LÀM MỚI FORM
    // ===============================================================
    private void lamMoi() {
        txtGhiChuCT.setText("");
        txtTenHangMuc.setText("");
        txtBienSoXe.setText("");
        txtKhachHang.setText("");
        txtTongTien.setText("");
        model.setRowCount(0);
        cboTrangThai.setSelectedItem("Còn hạn");
        capNhatThongKeHangMuc();
        napDuLieu();
    }

    // ===============================================================
    // 9. CÁC HÀM TIỆN ÍCH GIAO DIỆN
    // ===============================================================
    private JPanel card() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        return p;
    }

    private JLabel tieuDe(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lb.setForeground(NAVY);
        return lb;
    }

    private void themTruong(JPanel panel, int row, String label, JComponent input) {
        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0;
        a.gridy = row;
        a.anchor = GridBagConstraints.WEST;
        a.insets = new Insets(5, 4, 5, 8);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(51, 65, 85));
        panel.add(lbl, a);

        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 1;
        b.gridy = row;
        b.weightx = 1;
        b.fill = GridBagConstraints.HORIZONTAL;
        b.insets = new Insets(5, 0, 5, 4);

        input.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        input.setPreferredSize(new Dimension(190, 32));

        panel.add(input, b);
    }

    private void thongBao(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void baoLoi(String msg, Exception ex) {
        JOptionPane.showMessageDialog(this, msg + "\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
