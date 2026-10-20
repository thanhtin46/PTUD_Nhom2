
package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {

    private static final ConnectDB instance = new ConnectDB();

    private Connection connection;

    // Thay đổi thông tin cho đúng với SQL Server trên máy bạn
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
                    + "databaseName=QuanLySuaChuaXe;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;"
                    + "loginTimeout=5;";

    private static final String USER = "sa";

    // Thay bằng mật khẩu thực tế của tài khoản SQL Server
    private static final String PASSWORD = "sapassword";

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

            connection = DriverManager.getConnection(
                    URL, USER, PASSWORD
            );

            System.out.println(
                    "Kết nối SQL Server thành công: "
                            + connection.getCatalog()
            );

        } catch (SQLException e) {
            connection = null;

            System.err.println("Không thể kết nối SQL Server.");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Mã lỗi: " + e.getErrorCode());
            System.err.println("Chi tiết: " + e.getMessage());
        }
    }

    public synchronized void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = null;

        } catch (SQLException e) {
            System.err.println(
                    "Lỗi đóng kết nối: " + e.getMessage()
            );
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println(
                    "Lỗi kiểm tra kết nối: " + e.getMessage()
            );
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
