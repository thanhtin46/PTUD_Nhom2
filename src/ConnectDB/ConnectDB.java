package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
<<<<<<< HEAD

    private static ConnectDB instance = new ConnectDB();
    private static Connection con = null;
    
=======
    private static ConnectDB instance = new ConnectDB();
    private Connection connection = null;

    // Danh sách cấu hình kết nối thông dụng giúp người khác tải về chạy được ngay
    private static final String[] DANH_SACH_URL = {
        "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;user=sa;password=sapassword123@;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;user=sa;password=123456;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;user=sa;password=123;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;user=sa;password=sa;encrypt=true;trustServerCertificate=true;loginTimeout=3;",
        "jdbc:sqlserver://localhost:1433;databaseName=QuanLySuaChuaXe;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=3;"
    };

>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
    public static ConnectDB getInstance() {
        return instance;
    }

<<<<<<< HEAD
    public void connect() throws SQLException{
        String url =  "jdbc:sqlserver://localhost:1433;"
                + "databaseName=QuanLySuaChuaXe;"
                + "encrypt=true;"
                + "trustServerCertificate=true;";
        String user = "sa";
        String pwd = "01082006";
        con = DriverManager.getConnection(url, user, pwd);
    }
    
    public void disconnect() {
    	if(con != null)
    		try {
    			con.close();
    		}catch (SQLException e) {
				e.printStackTrace();
			}
    }

    public static Connection getConnection() {
        return con;
    }
}
=======
    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;

            for (String url : DANH_SACH_URL) {
                try {
                    connection = DriverManager.getConnection(url);
                    if (connection != null && !connection.isClosed()) {
                        System.out.println("Kết nối CSDL SQL Server (QuanLySuaChuaXe) thành công!");
                        return;
                    }
                } catch (SQLException ignored) {
                    // Thử chuỗi kết nối tiếp theo trong danh sách
                }
            }
            System.err.println("Không thể kết nối SQL Server (localhost:1433, DB: QuanLySuaChuaXe). Hệ thống tự động chuyển sang chế độ Demo / Bộ nhớ đệm dự phòng.");
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo kết nối CSDL: " + e.getMessage());
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
>>>>>>> 32a3a048d53529208494b32d997c0ac097f98d22
