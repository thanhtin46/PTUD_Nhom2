package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {

    private static ConnectDB instance = new ConnectDB();
    private static Connection con = null;
    
    public static ConnectDB getInstance() {
        return instance;
    }

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