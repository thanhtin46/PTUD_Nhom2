package tests;

import ConnectDB.ConnectDB;
import Entity.KhoDuLieu;
import DAO.*;

/** Read-only verification of the application's configured database. */
public class SqlSyncReadCheck {
    public static void main(String[] args) throws Exception {
        var c=ConnectDB.getInstance().getConnection();
        if(c==null)throw new IllegalStateException("No SQL connection");
        var cache=KhoDuLieu.get();
        int[] loaded={cache.getKhachHangs().size(),cache.getPhuongTiens().size(),cache.getPhuTungs().size(),cache.getPhieuTiepNhans().size(),cache.getHoaDons().size(),cache.getPhieuBaoHanhs().size()};
        String[] tables={"KhachHang","PhuongTien","PhuTung","PhieuTiepNhan","HoaDon","PhieuBaoHanh"};
        for(int i=0;i<tables.length;i++)try(var s=c.createStatement();var rows=s.executeQuery("SELECT COUNT(*) FROM "+tables[i])) {
            rows.next();if(rows.getInt(1)!=loaded[i])throw new AssertionError(tables[i]+" cache mismatch");
            System.out.println(tables[i]+" = "+loaded[i]);
        }
        new TrangChuTiepTan_DAO().layThongKeTongQuan();
        new PhieuBaoHanh_DAO().layThongTinIn("PBH101");
        if(new HoaDon_DAO().layChiTietTheoHoaDon("HD101").size()!=2)throw new AssertionError("Invoice details");
        System.out.println("PASS: live SQL and application cache agree; invoice and warranty reads succeeded");
        ConnectDB.getInstance().disconnect();
    }
}
