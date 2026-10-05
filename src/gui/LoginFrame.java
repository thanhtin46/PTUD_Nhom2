package gui;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {

        setTitle("Gara Oto - Đăng nhập");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initUI();
    }

    private void initUI() {

        JPanel mainPanel = new JPanel(new BorderLayout());

        // ==============================
        // PANEL BÊN TRÁI
        // ==============================

        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(420, 550));
        leftPanel.setBackground(new Color(30, 41, 59));
        leftPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 20, 10, 20);

        JLabel lblLogo = new JLabel("GARA OTO");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Arial", Font.BOLD, 36));

        gbc.gridy = 0;
        leftPanel.add(lblLogo, gbc);

        JLabel lblDescription = new JLabel(
                "<html><center>HỆ THỐNG QUẢN LÝ<br>GARA Ô TÔ</center></html>"
        );

        lblDescription.setForeground(new Color(203, 213, 225));
        lblDescription.setFont(new Font("Arial", Font.PLAIN, 20));
        lblDescription.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 1;
        leftPanel.add(lblDescription, gbc);

        JLabel lblVersion = new JLabel("GARAGE MANAGEMENT SYSTEM");

        lblVersion.setForeground(new Color(148, 163, 184));
        lblVersion.setFont(new Font("Arial", Font.PLAIN, 12));

        gbc.gridy = 2;
        leftPanel.add(lblVersion, gbc);


        // ==============================
        // PANEL ĐĂNG NHẬP
        // ==============================

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints form = new GridBagConstraints();

        form.gridx = 0;
        form.fill = GridBagConstraints.HORIZONTAL;
        form.insets = new Insets(8, 30, 8, 30);


        // Tiêu đề

        JLabel lblTitle = new JLabel("Đăng nhập");

        lblTitle.setFont(
                new Font("Arial", Font.BOLD, 30)
        );

        lblTitle.setForeground(
                new Color(15, 23, 42)
        );

        form.gridy = 0;
        rightPanel.add(lblTitle, form);


        // Chào mừng

        JLabel lblWelcome = new JLabel(
                "Chào mừng bạn quay trở lại"
        );

        lblWelcome.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        lblWelcome.setForeground(
                new Color(100, 116, 139)
        );

        form.gridy = 1;
        rightPanel.add(lblWelcome, form);


        // ==============================
        // USERNAME
        // ==============================

        JLabel lblUsername = new JLabel("Tài khoản");

        lblUsername.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        form.gridy = 2;
        rightPanel.add(lblUsername, form);


        txtUsername = new JTextField();

        txtUsername.setPreferredSize(
                new Dimension(300, 42)
        );

        txtUsername.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );

        form.gridy = 3;
        rightPanel.add(txtUsername, form);


        // ==============================
        // PASSWORD
        // ==============================

        JLabel lblPassword = new JLabel("Mật khẩu");

        lblPassword.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        form.gridy = 4;
        rightPanel.add(lblPassword, form);


        txtPassword = new JPasswordField();

        txtPassword.setPreferredSize(
                new Dimension(300, 42)
        );

        txtPassword.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );

        form.gridy = 5;
        rightPanel.add(txtPassword, form);


        // ==============================
        // BUTTON ĐĂNG NHẬP
        // ==============================

        JButton btnLogin = new JButton("ĐĂNG NHẬP");

        btnLogin.setPreferredSize(
                new Dimension(300, 45)
        );

        btnLogin.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        btnLogin.setBackground(
                new Color(37, 99, 235)
        );

        btnLogin.setForeground(Color.WHITE);

        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);

        btnLogin.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        form.gridy = 6;
        form.insets = new Insets(20, 30, 8, 30);

        rightPanel.add(btnLogin, form);


        // ==============================
        // TÀI KHOẢN DEMO
        // ==============================

        JLabel lblDemo = new JLabel(
                "<html><center>" +
                        "Tài khoản demo: admin<br>" +
                        "Mật khẩu: 123456" +
                        "</center></html>"
        );

        lblDemo.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        lblDemo.setForeground(
                new Color(100, 116, 139)
        );

        lblDemo.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        form.gridy = 7;
        form.insets = new Insets(10, 30, 10, 30);

        rightPanel.add(lblDemo, form);


        // ==============================
        // SỰ KIỆN
        // ==============================

        btnLogin.addActionListener(e -> login());

        txtPassword.addActionListener(e -> login());


        // ==============================
        // ADD PANEL
        // ==============================

        mainPanel.add(
                leftPanel,
                BorderLayout.WEST
        );

        mainPanel.add(
                rightPanel,
                BorderLayout.CENTER
        );

        add(mainPanel);
    }


    // ==============================
    // XỬ LÝ ĐĂNG NHẬP
    // ==============================

    private void login() {

        String username =
                txtUsername.getText().trim();

        String password =
                new String(
                        txtPassword.getPassword()
                );


        if (username.isEmpty() ||
                password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ tài khoản và mật khẩu!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }


        // Tạm thời dùng tài khoản giả
        if (username.equals("admin") &&
                password.equals("123456")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Đăng nhập thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );


            // Đóng Login
            dispose();


            // Mở MainFrame
            MainFrame mainFrame =
                    new MainFrame(username);

            mainFrame.setVisible(true);

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Tài khoản hoặc mật khẩu không chính xác!",
                    "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}