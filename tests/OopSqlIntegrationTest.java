package tests;

import DAO.PhieuSuaChua_DAO;
import Entity.*;
import Service.SuaChuaService;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

/** Chỉ chạy trên DB kiểm thử; tự xóa đúng các bản ghi fixture vừa tạo. */
public class OopSqlIntegrationTest {
    private static final String TN = "TN_OOP_1031";
    private static final String SC = "SC_OOP_1031";
    private static int checks;

    private static void check(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }
    private static long number(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next(); return r.getLong(1);
        }
    }
    private static DuLieuPhieuSuaChua data(String nv, String pt, int quantity) {
        return new DuLieuPhieuSuaChua(TN, nv, LocalDate.now().plusDays(3),
                List.of(new ChiTietDichVu("Phụ tùng", pt, "Hạng mục kiểm thử OOP", "Cái", quantity, 100000)),
                List.of(new DuLieuPhieuSuaChua.CongViec("Công việc kiểm thử OOP", "KTV kiểm thử", 50000)));
    }

    public static void main(String[] args) throws Exception {
        String url = System.getProperty("gara.test.url");
        if (url == null) throw new IllegalArgumentException("Provide gara.test.url explicitly");
        try (Connection c = DriverManager.getConnection(url)) {
            if (!"GaraSyncTest_Report_20261028".equals(c.getCatalog()))
                throw new IllegalStateException("Refusing to write outside the dedicated test database");
            check(number(c, "SELECT COUNT(*) FROM PhieuTiepNhan WHERE maPTN='" + TN + "'") == 0, "Fixture receipt must be absent");
            check(number(c, "SELECT COUNT(*) FROM PhieuSuaChua WHERE maPSC='" + SC + "'") == 0, "Fixture repair must be absent");
            String nv, pt;
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT TOP 1 maNV FROM NhanVien ORDER BY maNV")) {
                if (!r.next()) throw new IllegalStateException("No employee fixture");
                nv = r.getString(1);
            }
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT TOP 1 maPT FROM PhuTung ORDER BY maPT")) {
                if (!r.next()) throw new IllegalStateException("No part fixture");
                pt = r.getString(1);
            }
            long stock = number(c, "SELECT SUM(CAST(soLuongTon AS BIGINT)) FROM Kho");
            boolean created = false;
            try {
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO PhieuTiepNhan(maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) "
                        + "SELECT TOP 1 ?,maNV,maKH,soVIN,SYSDATETIME(),N'Kiểm thử OOP',N'CHO_SUA' FROM PhieuTiepNhan ORDER BY maPTN")) {
                    s.setString(1, TN);
                    check(s.executeUpdate() == 1, "Create isolated receipt fixture");
                    created = true;
                }
                SuaChuaService service = new SuaChuaService(new PhieuSuaChua_DAO(c));
                NhanVien manager = new NhanVien(nv, "Test", "", "", "Quản lý");
                check(SC.equals(service.luu(manager, data(nv, pt, 2))), "Create repair through service/DAO");
                check(number(c, "SELECT COUNT(*) FROM PhieuSuaChua WHERE maPSC='" + SC + "' AND maPTN='" + TN + "'") == 1, "Correct receipt link");
                check(number(c, "SELECT COUNT(*) FROM ChiTietCongViecSuaChua WHERE maPSC='" + SC + "'") == 1, "Labour persisted");
                NhanVien technician = new NhanVien(nv, "Test", "", "", "Nhân viên sửa chữa");
                service.luu(technician, data(nv, pt, 3));
                check(number(c, "SELECT SUM(soLuong) FROM ChiTietSuaChua WHERE maPSC='" + SC + "'") == 3, "Assigned technician updates details");
                try {
                    service.luu(technician, data(nv, "PT_OOP_NOT_EXISTS", 4));
                    throw new AssertionError("Invalid foreign key should fail");
                } catch (SQLException expected) {
                    check(c.getAutoCommit(), "Auto commit restored after SQL failure");
                    check(number(c, "SELECT SUM(soLuong) FROM ChiTietSuaChua WHERE maPSC='" + SC + "'") == 3, "Rollback retains original parts");
                    check(number(c, "SELECT tienCong FROM ChiTietCongViecSuaChua WHERE maPSC='" + SC + "'") == 50000, "Rollback retains original labour");
                }
                check(number(c, "SELECT SUM(CAST(soLuongTon AS BIGINT)) FROM Kho") == stock, "Saving repair does not subtract warehouse stock");
            } finally {
                if (created) {
                    c.setAutoCommit(false);
                    try {
                        for (String sql : new String[]{
                                "DELETE FROM ChiTietCongViecSuaChua WHERE maPSC=?",
                                "DELETE FROM ChiTietSuaChua WHERE maPSC=?",
                                "DELETE FROM PhieuSuaChua WHERE maPSC=?",
                                "DELETE FROM PhieuTiepNhan WHERE maPTN=?"}) {
                            try (PreparedStatement s = c.prepareStatement(sql)) {
                                s.setString(1, sql.contains("PhieuTiepNhan") ? TN : SC);
                                s.executeUpdate();
                            }
                        }
                        c.commit();
                    } catch (SQLException ex) { c.rollback(); throw ex; }
                    finally { c.setAutoCommit(true); }
                }
            }
            check(number(c, "SELECT COUNT(*) FROM PhieuTiepNhan WHERE maPTN='" + TN + "'") == 0, "Receipt fixture cleaned up");
            check(number(c, "SELECT COUNT(*) FROM PhieuSuaChua WHERE maPSC='" + SC + "'") == 0, "Repair fixture cleaned up");
            System.out.println("PASS: " + checks + " live SQL checks on " + c.getCatalog() + "; test fixtures removed");
        }
    }
}
