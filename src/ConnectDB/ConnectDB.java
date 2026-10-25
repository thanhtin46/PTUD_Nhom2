package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {

    private static final ConnectDB instance = new ConnectDB();

    private Connection connection;

    // Danh sách mật khẩu phổ biến của các thành viên trong nhóm để máy nào cũng chạy được
    private static final String[] DANH_SACH_MAT_KHAU = {
            "sapassword123@",
            "sapassword",
            "123456",
            "123",
            "sa"
    };

    private static final String BASE_URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=QuanLySuaChuaXe;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;"
                    + "loginTimeout=3;";

    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword123@";

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    public synchronized void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            // 1. Thử mật khẩu chính của máy hiện tại
            try {
                connection = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
                if (connection != null && !connection.isClosed()) {
                    System.out.println("Kết nối CSDL SQL Server (QuanLySuaChuaXe) thành công!");
                    return;
                }
            } catch (SQLException ignored) {}

            // 2. Thử các mật khẩu dự phòng khác của thành viên trong nhóm
            for (String mk : DANH_SACH_MAT_KHAU) {
                if (mk.equals(PASSWORD)) continue;
                try {
                    connection = DriverManager.getConnection(BASE_URL, USER, mk);
                    if (connection != null && !connection.isClosed()) {
                        System.out.println("Kết nối CSDL SQL Server (QuanLySuaChuaXe) thành công với mật khẩu dự phòng!");
                        return;
                    }
                } catch (SQLException ignored) {}
            }

            // 3. Thử kết nối qua Windows Authentication (nếu cấu hình)
            try {
                String winUrl = "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=3;";
                connection = DriverManager.getConnection(winUrl);
                if (connection != null && !connection.isClosed()) {
                    System.out.println("Kết nối CSDL SQL Server qua Windows Authentication thành công!");
                    return;
                }
            } catch (SQLException ignored) {}

            System.err.println("Không thể kết nối SQL Server (localhost:1433, DB: QuanLySuaChuaXe). Hệ thống tự động chuyển sang chế độ Demo / Bộ nhớ đệm dự phòng.");

        } catch (Exception e) {
            connection = null;
            System.err.println("Lỗi khởi tạo kết nối CSDL: " + e.getMessage());
        }
    }

    public synchronized void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = null;

        } catch (SQLException e) {
            System.err.println("Lỗi đóng kết nối: " + e.getMessage());
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra kết nối: " + e.getMessage());
        }

        return connection;
    }

    public static void main(String[] args) {
        Connection con = getInstance().getConnection();

        if (con != null) {
            System.out.println("Cơ sở dữ liệu: " + getCatalog(con));
        } else {
            System.out.println("Kết nối thất bại. Xem thông báo lỗi phía trên.");
        }
    }

    private static String getCatalog(Connection con) {
        try {
            return con.getCatalog();
        } catch (SQLException e) {
            return "Không xác định";
        }
    }
}
