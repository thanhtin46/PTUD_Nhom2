package gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private Sidebar sidebar;

    private String username;

    public MainFrame(String username) {

        this.username = username;

        setTitle("Gara Oto - Hệ thống quản lý");

        setSize(1300, 750);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        initUI();
    }


    private void initUI() {

        JPanel mainPanel =
                new JPanel(new BorderLayout());


        // ==============================
        // SIDEBAR
        // ==============================

        sidebar = new Sidebar(this);


        // ==============================
        // CONTENT
        // ==============================

        contentPanel =
                new JPanel(new BorderLayout());

        contentPanel.setBackground(
                new Color(241, 245, 249)
        );


        // Hiển thị Dashboard lúc mở
        showDashboard();


        mainPanel.add(
                sidebar,
                BorderLayout.WEST
        );

        mainPanel.add(
                contentPanel,
                BorderLayout.CENTER
        );


        add(mainPanel);
    }


    // ==============================
    // DASHBOARD
    // ==============================

    public void showDashboard() {

        contentPanel.removeAll();


        DashboardPanel dashboardPanel =
                new DashboardPanel(username);


        contentPanel.add(
                dashboardPanel,
                BorderLayout.CENTER
        );


        refreshContent();
    }


    // ==============================
    // HIỂN THỊ PANEL
    // ==============================

    public void showPanel(JPanel panel) {

        contentPanel.removeAll();

        contentPanel.add(
                panel,
                BorderLayout.CENTER
        );

        refreshContent();
    }


    private void refreshContent() {

        contentPanel.revalidate();

        contentPanel.repaint();
    }


    // ==============================
    // ĐĂNG XUẤT
    // ==============================

    public void logout() {

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        "Bạn có chắc muốn đăng xuất?",
                        "Xác nhận đăng xuất",
                        JOptionPane.YES_NO_OPTION
                );


        if (result ==
                JOptionPane.YES_OPTION) {

            dispose();

            LoginFrame loginFrame =
                    new LoginFrame();

            loginFrame.setVisible(true);
        }
    }
}