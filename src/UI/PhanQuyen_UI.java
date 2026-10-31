package UI;

import DAO.NhanVien_DAO;
import DAO.PhanQuyen_DAO;
import Entity.NhanVien;
import Entity.QuyenTruyCap;
import Entity.QuyenTruyCap.Quyen;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

/** Form chọn vai trò và các quyền bổ sung của nhân viên. */
public class PhanQuyen_UI extends JPanel {
    private final JComboBox<String> cboNhanVien = new JComboBox<>();
    private final JComboBox<String> cboVaiTro = new JComboBox<>(new String[]{
        "Quản lý", "Nhân viên tiếp tân", "Nhân viên sửa chữa", "Nhân viên kho", "Nhân viên thu ngân"
    });
    private final Map<Quyen, JCheckBox> oQuyen = new EnumMap<>(Quyen.class);
    private final List<NhanVien> nhanViens = new ArrayList<>();
    private final PhanQuyen_DAO phanQuyenDAO = new PhanQuyen_DAO();
    private final JButton btnLuu;

    public PhanQuyen_UI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel tren = new JPanel(new GridLayout(0, 1, 5, 5));
        tren.add(UiHelper.tieuDeTrang("Vai trò và quyền bổ sung"));
        tren.add(cboNhanVien);
        tren.add(cboVaiTro);
        tren.add(new JLabel("Quyền mặc định được giữ theo vai trò. Thay đổi có hiệu lực từ lần đăng nhập tiếp theo."));
        add(tren, BorderLayout.NORTH);
        JPanel luoi = new JPanel(new GridLayout(0, 3, 8, 8));
        for (Quyen quyen : Quyen.values()) {
            JCheckBox oChon = new JCheckBox(quyen.getTenHienThi());
            oQuyen.put(quyen, oChon);
            luoi.add(oChon);
        }
        add(new JScrollPane(luoi), BorderLayout.CENTER);
        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        duoi.setOpaque(false);
        // UiHelper.nut đã gắn listener; không đăng ký lần thứ hai.
        duoi.add(UiHelper.nut("Khôi phục theo vai trò", UiIcon.of(UiIcon.REFRESH, 14),
                UiHelper.XAM, e -> khoiPhucMacDinh()));
        btnLuu = UiHelper.nut("Lưu thay đổi", UiIcon.of(UiIcon.CHECK, 14),
                UiHelper.XANH_TRUNG, e -> luu());
        duoi.add(btnLuu);
        add(duoi, BorderLayout.SOUTH);
        nhanViens.addAll(new NhanVien_DAO().layTatCaNhanVien());
        for (NhanVien nv : nhanViens) cboNhanVien.addItem(nv.getMaNV() + " — " + nv.getHoTen());
        cboNhanVien.addActionListener(e -> chonNhanVien());
        cboVaiTro.addActionListener(e -> apDungMacDinh());
        chonNhanVien();
    }

    private void khoiPhucMacDinh() {
        oQuyen.values().forEach(o -> o.setSelected(false));
        apDungMacDinh();
    }

    private void apDungMacDinh() {
        Set<Quyen> macDinh = QuyenTruyCap.macDinh((String) cboVaiTro.getSelectedItem());
        oQuyen.forEach((quyen, oChon) -> {
            boolean batBuoc = macDinh.contains(quyen);
            if (!oChon.isEnabled()) oChon.setSelected(false);
            boolean chiQuanLy = quyen == Quyen.PHAN_QUYEN || quyen == Quyen.NHAN_VIEN || quyen == Quyen.CAU_HINH;
            oChon.setEnabled(!batBuoc && !chiQuanLy);
            if (batBuoc) oChon.setSelected(true);
        });
    }

    private void chonNhanVien() {
        int viTri = cboNhanVien.getSelectedIndex();
        btnLuu.setEnabled(false);
        if (viTri < 0) return;
        NhanVien nv = nhanViens.get(viTri);
        String vaiTro = switch (QuyenTruyCap.vaiTro(nv.getVaiTro())) {
            case QUAN_LY -> "Quản lý";
            case TIEP_TAN -> "Nhân viên tiếp tân";
            case SUA_CHUA -> "Nhân viên sửa chữa";
            case KHO -> "Nhân viên kho";
            case THU_NGAN -> "Nhân viên thu ngân";
            default -> null;
        };
        cboVaiTro.setSelectedItem(vaiTro);
        oQuyen.values().forEach(o -> { o.setEnabled(true); o.setSelected(false); });
        try {
            phanQuyenDAO.napQuyen(nv);
            if (nv.getCacQuyen() != null) {
                for (String tenQuyen : nv.getCacQuyen()) {
                    try {
                        oQuyen.get(Quyen.valueOf(tenQuyen)).setSelected(true);
                    } catch (IllegalArgumentException ignored) {
                        // Bỏ qua mã quyền cũ không có trong danh mục hiện tại.
                    }
                }
            }
            apDungMacDinh();
            btnLuu.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được quyền: " + ex.getMessage());
        }
    }

    private void luu() {
        if (!BaoVeQuyen.yeuCau(this, Quyen.PHAN_QUYEN)) return;
        int viTri = cboNhanVien.getSelectedIndex();
        if (viTri < 0) return;
        if (JOptionPane.showConfirmDialog(this, "Lưu vai trò và quyền mới?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Set<String> boSung = new HashSet<>();
        oQuyen.forEach((quyen, oChon) -> {
            if (oChon.isEnabled() && oChon.isSelected()) boSung.add(quyen.name());
        });
        try {
            NhanVien nv = nhanViens.get(viTri);
            String vaiTro = (String) cboVaiTro.getSelectedItem();
            phanQuyenDAO.luu(BaoVeQuyen.hienTai(), nv.getMaNV(), vaiTro, boSung);
            nv.setVaiTro(vaiTro);
            nv.setCacQuyen(boSung.toArray(new String[0]));
            JOptionPane.showMessageDialog(this, "Đã lưu. Có hiệu lực từ lần đăng nhập tiếp theo.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được quyền: " + ex.getMessage());
        }
    }
}
