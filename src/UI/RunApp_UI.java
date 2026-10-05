package UI;

import Entity.KhoDuLieu;
import Entity.NhanVien;
import javax.swing.*;
import java.util.ArrayList;

public class RunApp_UI {
    public static void main(String[] args) {
        // Ép dùng LAF Windows để title bar + nút ✕ của JFrame hiển thị đúng kích thước
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        ArrayList<NhanVien> danhSach = new ArrayList<>();
        danhSach.add(new NhanVien("NV01", "Nguyễn Văn Tiếp Tân", "tiepTan", "123", "Nhân viên tiếp tân"));
        danhSach.add(new NhanVien("NV02", "Trần Văn Sửa Chữa", "suaChua", "123", "Nhân viên sửa chữa"));
        danhSach.add(new NhanVien("NV03", "Nguyễn Văn Kho", "kho", "123", "Nhân viên kho"));
        danhSach.add(new NhanVien("NV04", "Quản Lý Tổng", "quanLy", "123", "Quản lý"));
        KhoDuLieu.get().setNhanViens(danhSach);

        SwingUtilities.invokeLater(() -> new Login_UI(danhSach).setVisible(true));
    }
}
