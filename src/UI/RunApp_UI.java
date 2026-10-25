package UI;

import ConnectDB.ConnectDB;
import DAO.NhanVien_DAO;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ArrayList;

public class RunApp_UI {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 1. Kết nối tới SQL Server thông qua ConnectDB
        ConnectDB.getInstance().connect();

        // 2. Tải danh sách tài khoản nhân viên trực tiếp từ SQL Server
        NhanVien_DAO nvDao = new NhanVien_DAO();
        ArrayList<NhanVien> danhSach = nvDao.layTatCaNhanVien();

        // Nếu database chưa có dữ liệu thì dùng dữ liệu mặc định dự phòng
        if (danhSach.isEmpty()) {
            danhSach.add(new NhanVien("NV01", "Nguyễn Văn Tiếp Tân", "tiepTan", "123", "Nhân viên tiếp tân"));
            danhSach.add(new NhanVien("NV02", "Trần Văn Sửa Chữa", "suaChua", "123", "Nhân viên sửa chữa"));
            danhSach.add(new NhanVien("NV03", "Nguyễn Văn Kho", "kho", "123", "Nhân viên kho"));
            danhSach.add(new NhanVien("NV04", "Quản Lý Tổng", "quanLy", "123", "Quản lý"));
            danhSach.add(new NhanVien("NV05", "Trần Thị Thu Ngân", "thuNgan", "123", "Nhân viên thu ngân"));
        }
        KhoDuLieu.get().setNhanViens(danhSach);

        SwingUtilities.invokeLater(() -> new Login_UI(danhSach).setVisible(true));
    }
}
