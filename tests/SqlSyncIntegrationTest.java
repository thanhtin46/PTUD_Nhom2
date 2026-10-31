package tests;

import ConnectDB.ConnectDB;
import DAO.*;
import Entity.*;
import java.sql.*;
import java.util.*;
import java.time.LocalDate;

/** Run only against the disposable GaraSyncTest_* database. */
public class SqlSyncIntegrationTest {
    static Connection c;
    static long scalar(String sql) throws SQLException {
        try (Statement s=c.createStatement();ResultSet rs=s.executeQuery(sql)) { rs.next();return rs.getLong(1); }
    }
    static void check(boolean ok,String msg) { if(!ok)throw new AssertionError(msg); }
    public static void main(String[] args) throws Exception {
        c=ConnectDB.getInstance().getConnection();
        check(c!=null && c.getCatalog().startsWith("GaraSyncTest_"),"Use isolated test database only");
        KhoDuLieu kho=KhoDuLieu.get();
        check(kho.getKhachHangs().size()==scalar("SELECT COUNT(*) FROM KhachHang"),"Customer cache");
        check(kho.getPhuongTiens().size()==scalar("SELECT COUNT(*) FROM PhuongTien"),"Vehicle cache");
        check(kho.getPhieuKhos().size()==scalar("SELECT (SELECT COUNT(*) FROM PhieuNhapKho)+(SELECT COUNT(*) FROM PhieuXuatKho)"),"Warehouse cache");
        check(kho.getNhanViens().size()==scalar("SELECT COUNT(*) FROM NhanVien"),"Staff cache");
        check(new HoaDon_DAO().layTatCaHoaDon().stream().anyMatch(h -> h.getMaHD().equals("HD101") && h.getBienSo().equals("51A-20101") && h.getTrangThai().equals(HoaDon.DA_THANH_TOAN)),"Invoice vehicle and status");
        check(new DuLieu_DAO().layChiTietTheoPhieu("TN112").size()==2,"Repair must include labour and parts");
        new TrangChuTiepTan_DAO().layThongKeTongQuan();
        new PhieuBaoHanh_DAO().layThongTinIn("PBH101");
        new PhieuXuat_DAO().layDanhSachPSCChoXuat();
        long stock=scalar("SELECT soLuongTon FROM Kho WHERE maPT='PT112'");
        List<ChiTietDichVu> lines=new DuLieu_DAO().layChiTietTheoPhieu("TN112");
        HoaDon invoice=new HoaDon("HD_TEST_SYNC","TN112","KH112","","","","",200000,450000,0,650000,"Tiền mặt","","Test",HoaDon.DA_THANH_TOAN);
        check(new HoaDon_DAO().themHoaDon(invoice,"NV05",lines),"Payment save");
        check(stock==scalar("SELECT soLuongTon FROM Kho WHERE maPT='PT112'"),"Payment must not decrement stock again");
        check(new HoaDon_DAO().layChiTietTheoHoaDon(invoice.getMaHD()).size()==2,"Invoice detail round trip");
        check(!new HoaDon_DAO().themHoaDon(invoice,"NV05",lines),"Duplicate payment rejected");
        check(new HoaDon_DAO().xoaHoaDon(invoice.getMaHD()),"Cancel payment");
        check(!new HoaDon_DAO().xoaHoaDon(invoice.getMaHD()),"Cancel twice rejected");
        check(stock==scalar("SELECT soLuongTon FROM Kho WHERE maPT='PT112'"),"Cancel must not invent returned inventory");
        HoaDon invalid=new HoaDon("HD_TEST_FAIL","TN111","KH111","","","","",200000,1,0,200001,"Tiền mặt","","Test",HoaDon.DA_THANH_TOAN);
        List<ChiTietDichVu> invalidLines=List.of(new ChiTietDichVu("Tiền công","","Công","Công",1,200000),new ChiTietDichVu("Phụ tùng","PT_NOT_EXISTS","Không có","Cái",1,1));
        check(!new HoaDon_DAO().themHoaDon(invalid,"NV05",invalidLines),"Invalid detail rejected");
        check(scalar("SELECT COUNT(*) FROM HoaDon WHERE maHD='HD_TEST_FAIL'")==0,"Rollback invoice header");
        check(scalar("SELECT COUNT(*) FROM ChiTietDichVuHoaDon WHERE maHD='HD_TEST_FAIL'")==0,"Rollback labour detail");
        PhieuBaoHanh_DAO warranty=new PhieuBaoHanh_DAO();
        java.sql.Date start=java.sql.Date.valueOf(LocalDate.now());
        List<Entity.ChiTietPhieuBaoHanh> warrantyLines=new ArrayList<>();
        warrantyLines.add(new Entity.ChiTietPhieuBaoHanh("", "", "Nước làm mát", 6, LocalDate.now().plusMonths(6), "Lỗi sản phẩm", "Test", "", ""));
        warranty.themPhieuBaoHanh("PBH_TEST_SYNC","HD110","NV01","51A-20110",start,"Còn hạn","Test",warrantyLines);
        check(warranty.layThongTinIn("PBH_TEST_SYNC").chiTiet.size()==1,"Warranty round trip");
        check(warranty.xoa("PBH_TEST_SYNC"),"Warranty deletes item details on non-cascade schema");
        String staff=kho.timNhanVienTheoMa("NV03").getHoTen();
        PhieuKho pending=new PhieuKho("PN_TEST_SYNC",PhieuKho.NHAP,"27/10/2026",staff,"Test","NCC101 - Phụ tùng ô tô An Phát","30/10/2026",PhieuKho.CHO_NHAP);
        pending.getChiTiet().add(new ChiTietKho("PT112","Dây curoa tổng",2,300000));
        kho.luuPhieuChoNhap(pending);
        kho.dongBoTuSQLServer();
        check(kho.getPhieuKhos().stream().anyMatch(p->p.getSoPhieu().equals(pending.getSoPhieu()) && p.isChoNhap()),"Pending receipt survives reload");
        check(stock==scalar("SELECT soLuongTon FROM Kho WHERE maPT='PT112'"),"Pending receipt must not add stock");
        check(kho.xacNhanNhap(pending),"Confirm receipt");
        check(!kho.xacNhanNhap(pending),"Receipt cannot be received twice");
        check(stock+2==scalar("SELECT soLuongTon FROM Kho WHERE maPT='PT112'"),"Receipt adds stock once");
        int before=kho.getKhachHangs().size();
        check(!kho.xoaKhachHang("KH101"),"Customer with history protected");
        check(kho.getKhachHangs().size()==before,"Failed deletion must not remove cache entry");
        check(c.getAutoCommit(),"Autocommit restored");
        System.out.println("PASS: SQL reads, cache, labour, vehicle joins, payment, duplicate/cancel, inventory, pending receipt, FK protection");
        ConnectDB.getInstance().disconnect();
    }
}
