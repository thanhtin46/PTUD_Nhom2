package UI;

import DAO.KhachHang_DAO;
import Entity.KhachHang;
import Entity.KhoDuLieu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Màn hình quản lý thông tin khách hàng (Thêm, Sửa, Xóa, Làm mới) dành cho Tiếp tân:
 * - Ràng buộc Họ và tên: Chữ cái đầu mỗi từ viết hoa, các chữ cái sau viết thường, cách nhau một khoảng trắng, không chứa số, không được để trống.
 * - Ràng buộc Số điện thoại: 0 ở đầu và 9 chữ số theo sau (10 số), không được nhập chữ, không được để trống.
 * - Ràng buộc CCCD: 0 ở đầu và 11 chữ số theo sau (12 số), không được nhập chữ.
 * - Ràng buộc Địa chỉ: Gồm 2 hoặc 3 chữ số ở đầu và phải có dấu phẩy.
 * - Ràng buộc Email: Ký tự đầu tiên phải là chữ hoặc số, sau đó tới @gmail.com.
 * - Nhóm KH: Chỉ gồm 2 nhóm "Khách mới" và "Khách hàng thân thiết". Tự động nâng hạng lên "Khách hàng thân thiết" khi khách có từ 2 lần sửa chữa tại gara.
 * - Nút Xóa hiển thị đầy đủ trực quan trên giao diện.
 * - Dòng áp cứng Ngày đăng ký (cố định ngày đăng ký, không cho sửa).
 */
public class QuanLyKhachHang_UI extends JPanel {
    private JTextField txtMa, txtTen, txtSdt, txtCccd, txtDiaChi, txtEmail, txtNgayDangKy;
    private JComboBox<String> cboNhomKH;
    private JLabel lblGiaiThichNhomKH;
    private JTable table;
    private DefaultTableModel model;
    private final KhoDuLieu kho = KhoDuLieu.get();
    private final KhachHang_DAO khachHangDAO = new KhachHang_DAO();

    public QuanLyKhachHang_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        root.add(UiHelper.tieuDeTrang("Quản lý khách hàng"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoForm(), taoBang());
        split.setResizeWeight(0.32);
        split.setDividerLocation(470);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);

        napBang();
        lamMoi(); // Tự động sinh mã khách hàng đầu tiên cho Tiếp tân
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin khách hàng", 470);

        txtMa = new JTextField();
        txtMa.setEditable(false); // Áp cứng mã khách hàng tự sinh để chuẩn hóa dữ liệu

        txtTen = new JTextField();
        txtSdt = new JTextField();
        txtCccd = new JTextField();

        // Cho phép nhập chữ bình thường, khi nhấn Thêm/Sửa mới kiểm tra và hiển thị thông báo lỗi
        txtDiaChi = new JTextField();
        txtEmail = new JTextField();

        // Chỉ cho phép 2 nhóm khách hàng: "Khách mới" và "Khách hàng thân thiết"
        cboNhomKH = new JComboBox<>(new String[]{
                KhachHang.NHOM_MOI,
                KhachHang.NHOM_THAN_THIET
        });
        cboNhomKH.setFont(UiHelper.FONT_BT);
        cboNhomKH.setBackground(Color.WHITE);
        cboNhomKH.setPreferredSize(new Dimension(190, 36));

        lblGiaiThichNhomKH = new JLabel("<html><i style='color:#64748B;'>Tự động lên 'Thân thiết' khi sửa từ 2 lần</i></html>");
        lblGiaiThichNhomKH.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JPanel pnlNhomKH = new JPanel(new BorderLayout(0, 3));
        pnlNhomKH.setOpaque(false);
        pnlNhomKH.add(cboNhomKH, BorderLayout.NORTH);
        pnlNhomKH.add(lblGiaiThichNhomKH, BorderLayout.CENTER);

        // Dòng áp cứng ngày đăng ký (cố định hiển thị ngày đăng ký, không cho sửa)
        txtNgayDangKy = new JTextField();
        txtNgayDangKy.setEditable(false);
        txtNgayDangKy.setBackground(new Color(241, 245, 249));
        txtNgayDangKy.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        UiHelper.themDong(form, 0, "Mã khách hàng *", txtMa);
        UiHelper.themDong(form, 1, "Họ và tên *", txtTen);
        UiHelper.themDong(form, 2, "Số điện thoại *", txtSdt);
        UiHelper.themDong(form, 3, "CCCD/CMND", txtCccd);
        UiHelper.themDong(form, 4, "Địa chỉ", txtDiaChi);
        UiHelper.themDong(form, 5, "Email", txtEmail);
        UiHelper.themDong(form, 6, "Nhóm KH", pnlNhomKH);
        UiHelper.themDong(form, 7, "Ngày đăng ký", txtNgayDangKy);

        // Hàng nút chức năng: Thêm, Sửa, Xóa, Làm mới
        JPanel nut = new JPanel(new GridLayout(1, 4, 8, 0));
        nut.setOpaque(false);
        nut.setBorder(new EmptyBorder(16, 0, 0, 0));
        nut.add(UiHelper.nut("Thêm", UiIcon.of(UiIcon.PLUS, 15), UiHelper.XANH_LA, true, e -> them()));
        nut.add(UiHelper.nut("Sửa", UiHelper.XANH_TRUNG, e -> sua()));
        nut.add(UiHelper.nut("Xóa", UiHelper.CHU_CHAY, e -> xoa()));
        nut.add(UiHelper.nut("Làm mới", UiHelper.XAM, e -> lamMoi()));

        UiHelper.ganNoiDungForm(formPanel, form, nut);
        return formPanel;
    }

    private JPanel taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách khách hàng");
        model = UiHelper.model("Mã KH", "Họ tên", "SĐT", "CCCD", "Địa chỉ", "Email", "Nhóm KH", "Ngày ĐK");
        table = UiHelper.bang(model);

        // Bật cuộn ngang tự nhiên và giữ đúng kích thước chuẩn của từng cột
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Đặt độ rộng tối thiểu và phù hợp cho từng cột để hiện đầy đủ thông tin
        table.getColumnModel().getColumn(0).setPreferredWidth(85);   // Mã KH
        table.getColumnModel().getColumn(1).setPreferredWidth(170);  // Họ tên
        table.getColumnModel().getColumn(2).setPreferredWidth(110);  // SĐT
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // CCCD
        table.getColumnModel().getColumn(4).setPreferredWidth(260);  // Địa chỉ
        table.getColumnModel().getColumn(5).setPreferredWidth(180);  // Email
        table.getColumnModel().getColumn(6).setPreferredWidth(160);  // Nhóm KH
        table.getColumnModel().getColumn(7).setPreferredWidth(110);  // Ngày ĐK

        UiHelper.canGiua(table, 0, 2, 3, 7);
        UiHelper.canTrai(table, 1, 4, 5, 6);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienLenForm();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        return bangPanel;
    }

    private void napBang() {
        model.setRowCount(0);
        // Tự động đồng bộ chuẩn hóa nhóm khách hàng trong SQL Server
        khachHangDAO.dongBoNhomTatCaKhachHang();

        // Đồng bộ danh sách từ CSDL SQL Server qua KhachHang_DAO
        java.util.ArrayList<KhachHang> ds = khachHangDAO.layTatCaKhachHang();
        if (ds.isEmpty()) {
            ds = kho.getKhachHangs();
        } else {
            kho.capNhatDanhSachKhachHang(ds);
        }

        for (KhachHang kh : ds) {
            String nhomKH = KhachHang.chuanHoaNhom(kh.getGhiChu());
            model.addRow(new Object[]{kh.getMaKH(), kh.getHoTen(), kh.getSdt(), kh.getCccd(),
                    kh.getDiaChi(), kh.getEmail(), nhomKH, kh.getNgayDangKyHienThi()});
        }
    }

    private void them() {
        if (!kiemTraForm(true)) return;

        LocalDate ngayDangKy = LocalDate.now();
        String nhomKH = cboNhomKH.getSelectedItem() != null ? cboNhomKH.getSelectedItem().toString() : KhachHang.NHOM_MOI;
        nhomKH = KhachHang.chuanHoaNhom(nhomKH);

        KhachHang kh = new KhachHang(
                txtMa.getText().trim(), txtTen.getText().trim(), txtSdt.getText().trim(),
                txtCccd.getText().trim(), txtDiaChi.getText().trim(), txtEmail.getText().trim(),
                nhomKH, ngayDangKy);

        boolean ok = kho.themKhachHang(kh);
        napBang();
        lamMoi();
        JOptionPane.showMessageDialog(this, ok ? "Thêm khách hàng và lưu CSDL SQL Server thành công!" : "Thêm khách hàng thành công (Lưu bộ nhớ)!");
    }

    private void sua() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!kiemTraForm(false)) return;

        String ma = model.getValueAt(dong, 0).toString();
        KhachHang kh = kho.timKhachHangTheoMa(ma);
        if (kh == null) {
            kh = khachHangDAO.timKhachHangTheoMa(ma);
        }
        if (kh == null) return;

        kh.setHoTen(txtTen.getText().trim());
        kh.setSdt(txtSdt.getText().trim());
        kh.setCccd(txtCccd.getText().trim());
        kh.setDiaChi(txtDiaChi.getText().trim());
        kh.setEmail(txtEmail.getText().trim());

        // Kiểm tra số lần sửa chữa để tự động áp dụng quy tắc nhóm KH
        int soLanSua = khachHangDAO.laySoLanSuaChua(ma);
        String nhomKH;
        if (soLanSua >= 2) {
            nhomKH = KhachHang.NHOM_THAN_THIET;
        } else {
            nhomKH = cboNhomKH.getSelectedItem() != null ? cboNhomKH.getSelectedItem().toString() : KhachHang.NHOM_MOI;
            nhomKH = KhachHang.chuanHoaNhom(nhomKH);
        }
        kh.setGhiChu(nhomKH);

        boolean ok = kho.capNhatKhachHang(kh);
        napBang();
        JOptionPane.showMessageDialog(this, ok ? "Cập nhật và đồng bộ CSDL SQL Server thành công!" : "Cập nhật thành công!");
    }

    private void xoa() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa trong bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ma = model.getValueAt(dong, 0).toString();
        String ten = model.getValueAt(dong, 1).toString();

        int hoi = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng [" + ma + "] " + ten + "?\nToàn bộ lịch sử liên quan cũng sẽ được xử lý an toàn.",
                "Xác nhận xóa khách hàng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (hoi != JOptionPane.YES_OPTION) return;

        boolean ok = kho.xoaKhachHang(ma);
        if (ok) {
            napBang();
            lamMoi();
            JOptionPane.showMessageDialog(this, "Đã xóa khách hàng [" + ma + "] thành công khỏi CSDL SQL Server!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể xóa khách hàng [" + ma + "]! Vui lòng kiểm tra lại kết nối CSDL hoặc dữ liệu ràng buộc.", "Lỗi xóa khách hàng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lamMoi() {
        // Tự động sinh mã mới tiếp theo từ KhachHang_DAO để Tiếp tân không phải gõ tay
        String maMoi = khachHangDAO.sinhMaKhachHangTuDong();
        txtMa.setText(maMoi);
        txtTen.setText("");
        txtSdt.setText("");
        txtCccd.setText("");
        txtDiaChi.setText("");
        txtEmail.setText("");
        cboNhomKH.setSelectedItem(KhachHang.NHOM_MOI); // Mặc định là "Khách mới"
        lblGiaiThichNhomKH.setText("<html><i style='color:#64748B;'>Tự động lên 'Thân thiết' khi sửa từ 2 lần</i></html>");
        txtNgayDangKy.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); // Áp cứng ngày hôm nay
        table.clearSelection();
        txtTen.requestFocus();
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        String ma = model.getValueAt(dong, 0) != null ? model.getValueAt(dong, 0).toString() : "";
        txtMa.setText(ma);
        txtTen.setText(model.getValueAt(dong, 1) != null ? model.getValueAt(dong, 1).toString() : "");
        txtSdt.setText(model.getValueAt(dong, 2) != null ? model.getValueAt(dong, 2).toString() : "");
        txtCccd.setText(model.getValueAt(dong, 3) != null ? model.getValueAt(dong, 3).toString() : "");
        txtDiaChi.setText(model.getValueAt(dong, 4) != null ? model.getValueAt(dong, 4).toString() : "");
        txtEmail.setText(model.getValueAt(dong, 5) != null ? model.getValueAt(dong, 5).toString() : "");

        String nhomKH = model.getValueAt(dong, 6) != null ? model.getValueAt(dong, 6).toString().trim() : "";
        String nhomChuan = KhachHang.chuanHoaNhom(nhomKH);

        // Kiểm tra số lần sửa chữa để hiển thị gợi ý và chọn nhóm tương ứng
        int soLanSua = khachHangDAO.laySoLanSuaChua(ma);
        if (soLanSua >= 2) {
            nhomChuan = KhachHang.NHOM_THAN_THIET;
            cboNhomKH.setSelectedItem(KhachHang.NHOM_THAN_THIET);
            lblGiaiThichNhomKH.setText("<html><b style='color:#16A34A;'>Đã sửa chữa " + soLanSua + " lần (Đạt chuẩn Thân thiết)</b></html>");
        } else {
            cboNhomKH.setSelectedItem(nhomChuan);
            lblGiaiThichNhomKH.setText("<html><i style='color:#64748B;'>Đã sửa chữa " + soLanSua + " lần (Đủ 2 lần sẽ tự động lên Thân thiết)</i></html>");
        }

        txtNgayDangKy.setText(model.getValueAt(dong, 7) != null ? model.getValueAt(dong, 7).toString() : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    /**
     * Kiểm tra các ràng buộc dữ liệu theo đúng yêu cầu:
     * 1. Họ và tên: Chữ cái đầu viết hoa, các chữ cái sau viết thường, phân cách bởi một dấu khoảng trắng,
     *    sau dấu khoảng trắng đó phải là chữ cái đầu viết hoa và các chữ cái thường, không được nhập số, không được để trống.
     * 2. Số điện thoại: 0 ở đầu và 9 chữ số phía sau, không được nhập chữ, không được để trống.
     * 3. CCCD: 0 ở đầu và 11 chữ số phía sau, không được nhập chữ.
     * 4. Địa chỉ: Phải có dấu phẩy, gồm 2 hoặc 3 chữ số ở đầu.
     * 5. Email: Đầu tiên phải là chữ hoặc số, sau đó tới @gmail.com.
     */
    private boolean kiemTraForm(boolean laThem) {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String cccd = txtCccd.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String email = txtEmail.getText().trim();

        // 1. Kiểm tra Mã khách hàng
        if (ma.isEmpty()) {
            UiHelper.loi(this, "Mã khách hàng không được để trống!", txtMa);
            return false;
        }

        // 2. Ràng buộc Họ và tên khách hàng
        if (ten.isEmpty()) {
            UiHelper.loi(this, "Họ và tên khách hàng không được để trống!", txtTen);
            return false;
        }
        if (ten.matches(".*\\d.*")) {
            UiHelper.loi(this, "Họ và tên không được chứa chữ số!", txtTen);
            return false;
        }
        // Chữ cái đầu viết hoa các chữ cái sau viết thường, phân cách bởi một dấu khoảng trắng,
        // sau dấu khoảng trắng đó phải là chữ cái đầu viết hoa và các chữ cái thường.
        if (!ten.matches("^[\\p{Lu}][\\p{Ll}]+( [\\p{Lu}][\\p{Ll}]+)+$")) {
            UiHelper.loi(this, "Họ và tên không hợp lệ! Chữ cái đầu mỗi từ phải viết hoa, các chữ cái sau viết thường, phân cách bởi một dấu khoảng trắng và không chứa số !", txtTen);
            return false;
        }

        // 3. Ràng buộc Số điện thoại: 0 ở đầu và 9 chữ số phía sau, không được nhập chữ, không được để trống
        if (sdt.isEmpty()) {
            UiHelper.loi(this, "Số điện thoại không được để trống!", txtSdt);
            return false;
        }
        if (!sdt.matches("^0\\d{9}$")) {
            UiHelper.loi(this, "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng số 0 và không được chứa chữ !", txtSdt);
            return false;
        }

        // 4. Ràng buộc CCCD: 0 ở đầu và 11 chữ số phía sau, không được nhập chữ
        if (!cccd.isEmpty() && !cccd.matches("^0\\d{11}$")) {
            UiHelper.loi(this, "Số CCCD phải gồm 12 chữ số, bắt đầu bằng số 0 và không được chứa chữ !", txtCccd);
            return false;
        }

        // 5. Ràng buộc Địa chỉ: 2 hoặc 3 chữ số đầu tiên, phía sau là ký tự bất kỳ và có ít nhất 1 dấu phẩy
        if (!diaChi.isEmpty() && !diaChi.matches("^\\d{2,3}(?!\\d).*,.*$")) {
            UiHelper.loi(this, "Địa chỉ phải gồm 2 hoặc 3 chữ số đầu tiên, phía sau là ký tự bất kỳ và có ít nhất 1 dấu phẩy !", txtDiaChi);
            return false;
        }

        // 6. Ràng buộc Email: Đầu tiên phải là chữ hoặc số, sau đó tới @gmail.com
        if (!email.isEmpty() && !email.matches("^[a-zA-Z0-9][a-zA-Z0-9._-]*@gmail\\.com$")) {
            UiHelper.loi(this, "Email không hợp lệ! Ký tự đầu tiên phải là chữ hoặc số và đuôi phải là @gmail.com !", txtEmail);
            return false;
        }

        // Kiểm tra trùng mã trong CSDL qua DAO
        if (laThem && khachHangDAO.kiemTraTrungMa(ma)) {
            UiHelper.loi(this, "Mã khách hàng \"" + ma + "\" đã tồn tại trong hệ thống!", txtMa);
            return false;
        }

        // Kiểm tra trùng số điện thoại qua DAO
        String maBoQua = laThem ? null : ma;
        if (khachHangDAO.kiemTraTrungSdt(sdt, maBoQua)) {
            UiHelper.loi(this, "Số điện thoại \"" + sdt + "\" đã được đăng ký bởi khách hàng khác!", txtSdt);
            return false;
        }

        return true;
    }
}
