package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * UC014: Phân quyền người dùng.
 * Quyền ứng với từng module. Khi chọn vai trò, mặc định tick theo vai trò.
 * Có thể tick thêm/bớt quyền chi tiết.
 */
public class PhanQuyen_UI extends JPanel {
    private static final String[][] MODULES = {
            {"KH",       "Quản lý khách hàng"},
            {"PT",       "Quản lý phương tiện"},
            {"BH",       "Quản lý bảo hành"},
            {"SC",       "Lập phiếu sửa chữa"},
            {"TD",       "Cập nhật tiến độ"},
            {"PHUTUNG",  "Quản lý phụ tùng"},
            {"NHAPKHO",  "Nhập kho"},
            {"XUATKHO",  "Xuất kho"},
            {"TONKHO",   "Báo cáo tồn kho"},
            {"NV",       "Quản lý nhân viên"},
            {"PQ",       "Phân quyền"},
            {"BCDT",     "Báo cáo doanh thu"},
    };

    /** Quyền mặc định theo vai trò */
    private static final String[][] QUYEN_MAC_DINH = {
            {"Quản lý",            "KH,PT,BH,SC,TD,PHUTUNG,NHAPKHO,XUATKHO,TONKHO,NV,PQ,BCDT"},
            {"Nhân viên tiếp tân", "KH,PT,BH"},
            {"Nhân viên sửa chữa", "SC,TD,TONKHO"},
            {"Nhân viên kho",      "PHUTUNG,NHAPKHO,XUATKHO,TONKHO"},
    };

    private final KhoDuLieu kho = KhoDuLieu.get();
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtTk = new JTextField();
    private final JLabel lblVaiTro = new JLabel("—");
    private final JCheckBox[] chkQuyen = new JCheckBox[MODULES.length];
    private JTable table;
    private DefaultTableModel model;

    public PhanQuyen_UI() {
        JPanel root = UiHelper.nen();
        setLayout(new BorderLayout());
        setBackground(UiHelper.NEN);
        add(root, BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JLabel tde = UiHelper.tieuDeTrang("🔐 Phân quyền người dùng");
        JLabel phu = new JLabel("Gán / thu hồi quyền truy cập cho từng tài khoản");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        phu.setForeground(new Color(110, 120, 135));
        tde.setAlignmentX(Component.LEFT_ALIGNMENT);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(tde);
        north.add(phu);
        root.add(north, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoBang(), taoForm());
        split.setResizeWeight(0.45);
        split.setDividerLocation(540);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        napBang();
    }

    private JScrollPane taoBang() {
        JPanel bangPanel = UiHelper.panelBang("Danh sách tài khoản");
        model = UiHelper.model("Mã NV", "Họ tên", "Tài khoản", "Vai trò", "Trạng thái", "Số quyền");
        table = UiHelper.bang(model);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) hienLenForm();
        });
        JScrollPane sp = new JScrollPane(table);
        UiHelper.danhSachBong(sp);
        bangPanel.add(sp, BorderLayout.CENTER);
        JScrollPane wrap = new JScrollPane(bangPanel);
        wrap.setBorder(null);
        wrap.setOpaque(false);
        wrap.getViewport().setOpaque(false);
        return wrap;
    }

    private JPanel taoForm() {
        JPanel formPanel = UiHelper.panelForm("Thông tin & quyền chi tiết", 480);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        txtMa.setEditable(false);
        txtTen.setEditable(false);
        txtTk.setEditable(false);
        UiHelper.themDong(form, 0, "Mã NV", txtMa);
        UiHelper.themDong(form, 1, "Họ tên", txtTen);
        UiHelper.themDong(form, 2, "Tài khoản", txtTk);
        lblVaiTro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblVaiTro.setForeground(new Color(37, 99, 168));
        UiHelper.themDong(form, 3, "Vai trò", lblVaiTro);

        // Bảng checkbox quyền
        JPanel quyenPanel = new JPanel(new GridLayout(0, 2, 10, 6));
        quyenPanel.setOpaque(false);
        quyenPanel.setBorder(new EmptyBorder(10, 4, 10, 4));
        for (int i = 0; i < MODULES.length; i++) {
            JCheckBox cb = new JCheckBox(MODULES[i][1]);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cb.setOpaque(false);
            cb.setFocusPainted(false);
            chkQuyen[i] = cb;
            quyenPanel.add(cb);
        }
        JScrollPane spQuyen = new JScrollPane(quyenPanel);
        spQuyen.setBorder(BorderFactory.createTitledBorder(
                new javax.swing.border.LineBorder(new Color(220, 228, 240)),
                "Quyền truy cập các module",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(37, 99, 168)));
        spQuyen.setPreferredSize(new Dimension(0, 280));

        // Nút hành động
        JPanel nutPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        nutPanel.setOpaque(false);
        nutPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        nutPanel.add(UiHelper.nut("Mặc định theo vai trò", new Color(37, 99, 168), e -> apDungMacDinh()));
        nutPanel.add(UiHelper.nut("Chọn tất cả", new Color(46, 160, 92), e -> chonTatCa()));
        nutPanel.add(UiHelper.nut("Lưu thay đổi", new Color(225, 150, 45), e -> luuQuyen()));

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(spQuyen, BorderLayout.CENTER);
        body.add(nutPanel, BorderLayout.SOUTH);
        formPanel.add(body, BorderLayout.CENTER);
        return formPanel;
    }

    private void napBang() {
        model.setRowCount(0);
        for (NhanVien nv : kho.getNhanViens()) {
            int soQuyen = nv.getCacQuyen() == null ? 0 : nv.getCacQuyen().length;
            String trangThai = nv.dangHoatDong() ? "Đang làm" : "Ngưng";
            model.addRow(new Object[]{nv.getMaNV(), nv.getHoTen(), nv.getTaiKhoan(),
                    nv.getVaiTro(), trangThai, soQuyen + " module"});
        }
    }

    private void hienLenForm() {
        int dong = table.getSelectedRow();
        if (dong < 0) return;
        NhanVien nv = kho.timNhanVienTheoMa(model.getValueAt(dong, 0).toString());
        if (nv == null) return;
        txtMa.setText(nv.getMaNV());
        txtTen.setText(nv.getHoTen());
        txtTk.setText(nv.getTaiKhoan());
        lblVaiTro.setText(nv.getVaiTro());
        // Set quyền
        Set<String> dsQuyen = new LinkedHashSet<>();
        if (nv.getCacQuyen() != null) dsQuyen.addAll(Arrays.asList(nv.getCacQuyen()));
        for (int i = 0; i < MODULES.length; i++) {
            chkQuyen[i].setSelected(dsQuyen.contains(MODULES[i][0]));
        }
    }

    private void apDungMacDinh() {
        for (String[] vt : QUYEN_MAC_DINH) {
            if (vt[0].equals(lblVaiTro.getText())) {
                Set<String> ds = new LinkedHashSet<>(Arrays.asList(vt[1].split(",")));
                for (int i = 0; i < MODULES.length; i++) {
                    chkQuyen[i].setSelected(ds.contains(MODULES[i][0]));
                }
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Không tìm thấy cấu hình mặc định cho vai trò này!");
    }

    private void chonTatCa() {
        for (JCheckBox cb : chkQuyen) cb.setSelected(true);
    }

    private void luuQuyen() {
        int dong = table.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Chọn tài khoản cần phân quyền!");
            return;
        }
        NhanVien nv = kho.timNhanVienTheoMa(model.getValueAt(dong, 0).toString());
        if (nv == null) return;
        java.util.List<String> ds = new java.util.ArrayList<>();
        for (int i = 0; i < MODULES.length; i++) {
            if (chkQuyen[i].isSelected()) ds.add(MODULES[i][0]);
        }
        nv.setCacQuyen(ds.toArray(new String[0]));
        napBang();
        JOptionPane.showMessageDialog(this, "Đã lưu quyền cho " + nv.getHoTen() + "!\n" +
                "Có " + ds.size() + " module được phép truy cập.");
    }
}
