package UI;

import ConnectDB.ConnectDB;
import DAO.NhanVien_DAO;
import Entity.KhoDuLieu;
import Entity.NhanVien;
import javax.swing.*;
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

        if (danhSach.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Không thể kết nối hoặc chưa có dữ liệu tài khoản trong CSDL SQL Server (QuanLySuaChuaXe)!\n\n"
                    + "Hướng dẫn dành cho Giảng viên / Người chấm đồ án:\n"
                    + "1. Mở SQL Server Management Studio (SSMS).\n"
                    + "2. Mở file 'QuanLySuaChuaXe.sql' (ở thư mục gốc của đồ án) và nhấn Execute để khởi tạo database và dữ liệu mẫu.\n"
                    + "3. Đảm bảo dịch vụ SQL Server đang chạy và mật khẩu tài khoản sa là một trong các mật khẩu thông dụng (sapassword123@, 123456, 123, sa...) hoặc sử dụng Windows Authentication.\n"
                    + "4. Sau đó khởi động lại ứng dụng.",
                    "Thông báo kết nối CSDL",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        KhoDuLieu.get().setNhanViens(danhSach);

        SwingUtilities.invokeLater(() -> new Login_UI(danhSach).setVisible(true));
    }
}
