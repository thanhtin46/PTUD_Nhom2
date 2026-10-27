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
            JOptionPane.showMessageDialog(null, "Không tải được tài khoản SQL Server. Kiểm tra kết nối và chạy file SQL theo hướng dẫn database/README_DONG_BO.md.");
            return;
        }
        KhoDuLieu.get().setNhanViens(danhSach);

        SwingUtilities.invokeLater(() -> new Login_UI(danhSach).setVisible(true));
    }
}
