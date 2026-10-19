package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static ConnectDB instance = new ConnectDB();
    private Connection connection = null;

    // Chuỗi kết nối tới cơ sở dữ liệu QuanLySuaChuaXe trên SQL Server qua cổng 1433
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";

    static {
        try {
            String libPath = new java.io.File("lib").getAbsolutePath();
            String cur = System.getProperty("java.library.path");
            if (cur == null || !cur.contains(libPath)) {
                System.setProperty("java.library.path", libPath + java.io.File.pathSeparator + (cur != null ? cur : ""));
            }
        } catch (Exception ignored) {}
    }

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
