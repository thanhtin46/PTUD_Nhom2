package DAO;

import java.sql.*;

/** Tra mã sửa chữa thực tế, hỗ trợ dữ liệu TN/SC cũ và mã mới do giao diện sinh. */
public final class LienKetPhieu {
    private LienKetPhieu() {}
    public static String maSuaChua(Connection c, String ma) throws SQLException {
        if (ma == null || !ma.startsWith("TN")) return ma;
        try (PreparedStatement ps=c.prepareStatement("SELECT TOP 1 maPSC FROM PhieuSuaChua WHERE maPTN=? OR (maPTN IS NULL AND maPSC=?) ORDER BY CASE WHEN maPTN=? THEN 0 ELSE 1 END, ngayBD DESC")) {
            ps.setString(1,ma);ps.setString(2,"SC"+ma.substring(2));ps.setString(3,ma);
            try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getString(1):"SC"+ma.substring(2);}
        }
    }
}
