package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance = new ConnectDB();
    private Connection connection = null;

    // Chuỗi kết nối trực tiếp qua tài khoản sa chuẩn SQL Server (không phụ thuộc vào file DLL của Windows)
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;user=sa;password=sapassword123@;encrypt=true;trustServerCertificate=true;";

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void main(String[] args) {
        ConnectDB.getInstance().connect();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            try {
                System.out.println("Kết nối SQL Server thành công thông qua ConnectDB: " + con.getCatalog());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Không thể kết nối đến cơ sở dữ liệu.");
        }
    }
}
