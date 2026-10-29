import ConnectDB.ConnectDB;
import DAO.*;
import Entity.*;
import UI.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Executes real Swing event handlers against the report-only database.
 * Dialogs are recorded and acknowledged within this test JVM, not another app.
 * Does not replace DAO logic or SQL with mocks. */
public class ReportExecutionTest {
    static Connection db;
    static JFrame host;
    static final List<String> messages=new ArrayList<>(), results=new ArrayList<>();
    static javax.swing.Timer closer;
    static Object field(Object x,String name)throws Exception {Field f=x.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(x);}
    static Object call(Object x,String name,Object...args)throws Exception {
        for(Method m:x.getClass().getDeclaredMethods())if(m.getName().equals(name)&&m.getParameterCount()==args.length){m.setAccessible(true);try{return m.invoke(x,args);}catch(InvocationTargetException e){throw new Exception(e.getCause());}}
        throw new NoSuchMethodException(name);
    }
    static void set(Object x,String name,String value)throws Exception {((JTextComponent)field(x,name)).setText(value);}
    static String get(Object x,String name)throws Exception{return ((JTextComponent)field(x,name)).getText();}
    static long count(String sql)throws Exception{try(var s=db.createStatement();var r=s.executeQuery(sql)){r.next();return r.getLong(1);}}
    static String text(String sql)throws Exception{try(var s=db.createStatement();var r=s.executeQuery(sql)){r.next();return r.getString(1);}}
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    static void show(JPanel p){host.setContentPane(p);host.validate();}
    static JOptionPane pane(Container c){for(Component x:c.getComponents()){if(x instanceof JOptionPane p)return p;if(x instanceof Container cc){JOptionPane p=pane(cc);if(p!=null)return p;}}return null;}
    interface Case {String run()throws Exception;}
    static void test(String id,Case c){messages.clear();String status="Pass",actual;
        try{actual=c.run();}catch(AssertionError e){status="Fail";actual=e.getMessage();}catch(Exception e){status="Error";actual=e.toString();e.printStackTrace();}
        String line=id+"\t"+status+"\t"+actual.replace('\t',' ').replace('\n',' ')+"\t"+String.join(" / ",messages).replace('\t',' ').replace('\n',' ');
        results.add(line);System.out.println(line);
    }
    static void select(Object ui,String id)throws Exception{JTable t=(JTable)field(ui,"table");for(int r=0;r<t.getRowCount();r++)if(id.equals(String.valueOf(t.getValueAt(r,0)))){t.setRowSelectionInterval(r,r);return;}throw new AssertionError("Không thấy dòng "+id);}
    static void kh(Object ui)throws Exception{set(ui,"txtTen","Nguyễn Minh An");set(ui,"txtSdt","0909123456");set(ui,"txtEmail","an@test.vn");}
    static void xe(Object ui)throws Exception{set(ui,"txtBienSo","51A-99001");set(ui,"txtSoKhung","VINTEST00001");set(ui,"txtHangXe","Toyota");set(ui,"txtDongXe","Vios");set(ui,"txtNamSX","2022");set(ui,"txtOdo","10000");}
    public static void main(String[]args)throws Exception{
        db=ConnectDB.getInstance().getConnection();
        if(db==null||!"GaraSyncTest_Report_20261028".equals(db.getCatalog()))throw new IllegalStateException("Report database only");
        if(count("SELECT COUNT(*) FROM KhachHang WHERE soDT='0909123456'")!=0)throw new IllegalStateException("Use fresh report fixtures");
        Thread watchdog=new Thread(()->{try{Thread.sleep(180000);System.err.println("Test watchdog timeout");System.exit(3);}catch(InterruptedException ignored){}});watchdog.setDaemon(true);watchdog.start();
        SwingUtilities.invokeAndWait(()->{
            host=new JFrame("Kiểm thử tự động Swing trên database riêng");host.setSize(1300,850);host.setVisible(true);
            closer=new javax.swing.Timer(80,e->{for(Window w:Window.getWindows())if(w instanceof JDialog d && d.isShowing()){JOptionPane p=pane(d);if(p!=null){messages.add(String.valueOf(p.getMessage()));p.setValue(JOptionPane.OK_OPTION);d.dispose();}}});closer.start();
            try{execute();}catch(Exception e){e.printStackTrace();results.add("HARNESS\tError\t"+e+"\t");}
            closer.stop();for(Window w:Window.getWindows())w.dispose();
        });
        Files.write(Path.of("build/docx-qa/swing-results.tsv"),results,StandardCharsets.UTF_8);
        ConnectDB.getInstance().disconnect();System.exit(0);
    }
    static void execute()throws Exception{
        var staff=new NhanVien_DAO().layTatCaNhanVien();
        NhanVien admin=staff.stream().filter(n->"quanLy".equals(n.getTaiKhoan())).findFirst().orElseThrow();
        test("TC_AUTH02",()->{Login_UI ui=new Login_UI(staff);for(String[] v:new String[][]{{"","123"},{"quanLy",""}}){set(ui,"txtTaiKhoan",v[0]);set(ui,"txtMatKhau",v[1]);call(ui,"xuLyDangNhap");}ui.dispose();check(messages.size()==2&&messages.stream().allMatch(s->s.contains("đầy đủ")),"Không cảnh báo đủ hai ô rỗng");return "Hai lượt bỏ trống đều báo nhập đầy đủ; không mở Main_UI.";});
        test("TC_AUTH03",()->{Login_UI ui=new Login_UI(staff);set(ui,"txtTaiKhoan","quanLy");set(ui,"txtMatKhau","sai123");call(ui,"xuLyDangNhap");ui.dispose();check(messages.stream().anyMatch(s->s.contains("không chính xác")),"Không báo sai mật khẩu");return "quanLy / sai123 bị từ chối, hiện thông báo tài khoản hoặc mật khẩu không chính xác.";});
        test("TC_AUTH04",()->{NhanVien n=new NhanVien("NV_RPT_LOCK","Nhân viên thử khóa","nv_rpt_lock","123","Nhân viên tiếp tân",NhanVien.NGUNG);check(new NhanVien_DAO().themNhanVien(n,"0909999971"),"Không tạo được tài khoản thử");var ds=new NhanVien_DAO().layTatCaNhanVien();Login_UI ui=new Login_UI(ds);set(ui,"txtTaiKhoan","nv_rpt_lock");set(ui,"txtMatKhau","123");call(ui,"xuLyDangNhap");ui.dispose();check(messages.stream().anyMatch(s->s.contains("vô hiệu hóa")),"Tài khoản khóa không bị chặn");return "Tài khoản SQL nv_rpt_lock trạng thái NGUNG bị chặn; báo vô hiệu hóa.";});
        final QuanLyKhachHang_UI k=new QuanLyKhachHang_UI();show(k);
        test("TC_KH02",()->{kh(k);long before=count("SELECT COUNT(*) FROM KhachHang");for(String s:new String[]{"123","1909123456","0909abc456"}){set(k,"txtSdt",s);call(k,"them");}check(count("SELECT COUNT(*) FROM KhachHang")==before&&messages.size()==3&&messages.stream().allMatch(s->s.contains("10 chữ số")),"SĐT sai không được chặn đủ");return "Cả 3 SĐT sai bị chặn; số khách SQL không đổi.";});
        test("TC_KH04",()->{kh(k);set(k,"txtCccd","12345");call(k,"them");set(k,"txtCccd","");set(k,"txtEmail","abc");call(k,"them");check(messages.size()==2&&messages.get(0).contains("9 hoặc 12")&&messages.get(1).contains("Email"),"Thiếu cảnh báo CCCD/email");return "CCCD 12345 và email abc đều bị chặn, đúng hai thông báo.";});
        final String[] khId={""};
        test("TC_KH01",()->{call(k,"lamMoi");kh(k);khId[0]=get(k,"txtMa");call(k,"them");call(k,"napBang");check(count("SELECT COUNT(*) FROM KhachHang WHERE soDT='0909123456'")==1,"Không lưu đúng một khách");select(k,khId[0]);return "Đã lưu "+khId[0]+" Nguyễn Minh An, 0909123456, an@test.vn; tải lại bảng thấy đúng hồ sơ.";});
        test("TC_KH03",()->{call(k,"lamMoi");kh(k);long before=count("SELECT COUNT(*) FROM KhachHang");call(k,"them");check(count("SELECT COUNT(*) FROM KhachHang")==before&&messages.stream().anyMatch(s->s.contains("đã được đăng ký")),"SĐT trùng không bị chặn");return "0909123456 bị báo đã đăng ký; không thêm khách thứ hai.";});
        test("TC_KH05",()->{select(k,khId[0]);kh(k);set(k,"txtDiaChi","12 Lê Lợi");call(k,"sua");call(k,"napBang");check("12 Lê Lợi".equals(text("SELECT diaChi FROM KhachHang WHERE soDT='0909123456'")),"Địa chỉ SQL không đúng");select(k,khId[0]);return "Sửa địa chỉ thành 12 Lê Lợi; truy vấn SQL và nạp bảng thành công.";});
        final QuanLyPhuongTien_UI v=new QuanLyPhuongTien_UI();show(v);
        test("TC_XE03",()->{xe(v);for(String year:new String[]{"1949",String.valueOf(java.time.LocalDate.now().getYear()+2)}){set(v,"txtNamSX",year);check(Boolean.FALSE.equals(call(v,"kiemTraForm",true)),"Chấp nhận năm sai "+year);}set(v,"txtNamSX","2022");for(String odo:new String[]{"-1","abc"}){set(v,"txtOdo",odo);check(Boolean.FALSE.equals(call(v,"kiemTraForm",true)),"Chấp nhận ODO sai");}check(messages.size()==4,"Thiếu cảnh báo năm/ODO");return "1949, năm hiện tại +2, ODO -1 và abc đều trả false kèm cảnh báo.";});
        test("TC_XE01",()->{xe(v);JComboBox<?> combo=(JComboBox<?>)field(v,"cboChuXe");for(int i=0;i<combo.getItemCount();i++)if(combo.getItemAt(i).toString().contains(khId[0]))combo.setSelectedIndex(i);call(v,"them");call(v,"napBang");check(count("SELECT COUNT(*) FROM PhuongTien WHERE bienSoXe='51A-99001' AND maKH='"+khId[0]+"'")==1,"Xe không lưu đúng chủ");select(v,"51A-99001");return "Xe 51A-99001/VINTEST00001 được lưu đúng chủ "+khId[0]+" và tải lại được.";});
        test("TC_XE02",()->{xe(v);check(Boolean.FALSE.equals(call(v,"kiemTraForm",true)),"Không chặn biển số trùng");set(v,"txtBienSo","51A-99003");check(Boolean.FALSE.equals(call(v,"kiemTraForm",true)),"Không chặn VIN trùng");check(messages.size()==2,"Thiếu cảnh báo trùng xe");return "Biển số 51A-99001 và VINTEST00001 đều bị chặn khi thử riêng.";});
        final LapPhieuTiepNhan_UI tn=new LapPhieuTiepNhan_UI(admin);show(tn);
        test("TC_TN02",()->{long n=count("SELECT COUNT(*) FROM PhieuTiepNhan");set(tn,"txtBienSo","");call(tn,"tao");set(tn,"txtBienSo","51A-99001");set(tn,"txtSdt","");call(tn,"tao");set(tn,"txtSdt","0909123456");set(tn,"txtYeuCau","");call(tn,"tao");check(messages.size()==3&&count("SELECT COUNT(*) FROM PhieuTiepNhan")==n,"Không chặn đủ 3 trường");return "Ba lượt thiếu biển số, SĐT, yêu cầu đều bị chặn; SQL không thêm phiếu.";});
        test("TC_TN01",()->{set(tn,"txtBienSo","51A-99001");set(tn,"txtSdt","0909123456");set(tn,"txtHoTen","Nguyễn Minh An");set(tn,"txtYeuCau","Bảo dưỡng định kỳ");call(tn,"tao");check(count("SELECT COUNT(*) FROM PhieuTiepNhan t JOIN PhuongTien v ON v.soVIN=t.soVIN WHERE v.bienSoXe='51A-99001' AND t.maKH='"+khId[0]+"'")==1,"Phiếu không gắn đúng xe/khách");return "Đã tạo phiếu tiếp nhận cho 51A-99001, đúng khách và yêu cầu trong SQL.";});
        test("TC_TN03",()->{call(tn,"lamMoi");set(tn,"txtBienSo","51A-99002");set(tn,"txtSdt","0909123457");set(tn,"txtHoTen","Khách thử mới");set(tn,"txtYeuCau","Thay dầu");call(tn,"tao");check(count("SELECT COUNT(*) FROM PhieuTiepNhan t JOIN KhachHang k ON k.maKH=t.maKH JOIN PhuongTien v ON v.soVIN=t.soVIN WHERE k.soDT='0909123457' AND v.bienSoXe='51A-99002'")==1,"Không tạo được liên kết khách/xe/phiếu");return "SQL có khách mới, xe 51A-99002 và một phiếu tiếp nhận liên kết đúng.";});
        final DanhSachPhuTung_UI pt=new DanhSachPhuTung_UI();show(pt);
        test("TC_PT01",()->{set(pt,"txtMa","PT_TEST01");set(pt,"txtTen","Lọc dầu thử");set(pt,"txtDonVi","Cái");set(pt,"txtTon","10");set(pt,"txtGia","100000");call(pt,"them");call(pt,"napBang");select(pt,"PT_TEST01");set(pt,"txtMa","PT_TEST01");set(pt,"txtTen","Lọc dầu thử");set(pt,"txtDonVi","Cái");set(pt,"txtTon","10");set(pt,"txtGia","120000");call(pt,"sua");KhoDuLieu.get().dongBoTuSQLServer();PhuTung p=KhoDuLieu.get().timPhuTung("PT_TEST01");check(p!=null&&p.getDonGia()==120000&&p.getSoLuongTon()==10,"Giá hoặc tồn không lưu");return "Thêm PT_TEST01 tồn 10; sửa giá 100000 thành 120000; nạp SQL đúng.";});
        test("TC_PT02",()->{set(pt,"txtMa","PT_TEST01");set(pt,"txtTen","Lọc dầu thử");set(pt,"txtTon","10");set(pt,"txtGia","120000");check(Boolean.FALSE.equals(call(pt,"kiemTra",true)),"Mã trùng không bị chặn");set(pt,"txtMa","PT_TEST02");set(pt,"txtTon","-1");check(Boolean.FALSE.equals(call(pt,"kiemTra",true)),"Tồn âm không bị chặn");set(pt,"txtTon","10");set(pt,"txtGia","abc");check(Boolean.FALSE.equals(call(pt,"kiemTra",true)),"Giá chữ không bị chặn");return "Mã trùng, tồn -1 và giá abc đều bị từ chối với cảnh báo.";});
        final NhapKho_UI nk=new NhapKho_UI(admin);show(nk);
        test("TC_NK05",()->{JComboBox<?> cb=(JComboBox<?>)field(nk,"cboPhuTung");cb.setSelectedIndex(0);int n=((DefaultTableModel)field(nk,"modelDong")).getRowCount();for(String q:new String[]{"0","-2","abc"}){set(nk,"txtSoLuong",q);call(nk,"themDong");}check(((DefaultTableModel)field(nk,"modelDong")).getRowCount()==n&&messages.size()==3,"Dòng nhập sai vẫn được thêm");return "0, -2 và abc đều không thêm dòng; bảng chi tiết không đổi.";});
        test("TC_PQ01",()->{KhoDuLieu.get().dongBoTuSQLServer();PhanQuyen_UI pq=new PhanQuyen_UI();show(pq);JTable table=(JTable)field(pq,"table");String id=null;for(int r=0;r<table.getRowCount();r++){String candidate=table.getValueAt(r,0).toString();NhanVien n=KhoDuLieu.get().timNhanVienTheoMa(candidate);if(n!=null&&"tiepTan".equals(n.getTaiKhoan())){id=candidate;table.setRowSelectionInterval(r,r);break;}}check(id!=null,"Không thấy tài khoản tiếp tân");for(JCheckBox c:(JCheckBox[])field(pq,"chkQuyen"))c.setSelected(true);call(pq,"luuQuyen");NhanVien after=new NhanVien_DAO().layTatCaNhanVien().stream().filter(n->"tiepTan".equals(n.getTaiKhoan())).findFirst().orElseThrow();check(after.coQuyen("PHUTUNG"),"Sau cấp thêm quyền PHUTUNG và báo lưu thành công, tải lại SQL mất quyền vừa cấp. Quyền không được lưu bền.");return "Quyền được lưu bền và đọc lại đúng.";});
        test("TC_NK04",()->{var p=new PhieuKho("PN_RPT_CANCEL",PhieuKho.NHAP,"27/10/2026",admin.getHoTen(),"Report","NCC101 - Phụ tùng ô tô An Phát","30/10/2026",PhieuKho.CHO_NHAP);p.getChiTiet().add(new ChiTietKho("PT112","Dây curoa tổng",1,300000));long n=count("SELECT soLuongTon FROM Kho WHERE maPT='PT112'");new NhapKho_DAO().luuChoNhap(p);check(new NhapKho_DAO().doiTrangThai(p.getSoPhieu(),false),"Hủy thất bại");check("DA_HUY".equals(text("SELECT trangThai FROM PhieuNhapKho WHERE maPhieuNhap='PN_RPT_CANCEL'"))&&count("SELECT soLuongTon FROM Kho WHERE maPT='PT112'")==n,"Tồn hoặc trạng thái sai");return "DAO: PN_RPT_CANCEL chuyển DA_HUY; tồn PT112 không đổi. Chưa kiểm tra nút giao diện.";});
    }
}

