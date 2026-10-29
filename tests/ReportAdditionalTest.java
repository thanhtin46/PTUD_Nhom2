import ConnectDB.ConnectDB;
import DAO.*;
import Entity.*;
import UI.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class ReportAdditionalTest extends ReportExecutionTest {
    public static void main(String[]args)throws Exception {
        db=ConnectDB.getInstance().getConnection();
        if(db==null||!"GaraSyncTest_Report_20261028".equals(db.getCatalog()))throw new IllegalStateException("Report database only");
        SwingUtilities.invokeAndWait(()->{
            host=new JFrame("Kiểm thử bổ sung gara");host.setSize(1300,850);host.setVisible(true);
            closer=new javax.swing.Timer(80,e->{for(Window w:Window.getWindows())if(w instanceof JDialog d&&d.isShowing()){JOptionPane p=pane(d);if(p!=null){messages.add(String.valueOf(p.getMessage()));p.setValue(JOptionPane.OK_OPTION);d.dispose();}}});closer.start();
            try { runCases(); }catch(Exception e){e.printStackTrace();}
            closer.stop();for(Window w:Window.getWindows())w.dispose();
        });
        Files.write(Path.of("build/docx-qa/additional-results.tsv"),results,StandardCharsets.UTF_8);
        ConnectDB.getInstance().disconnect();System.exit(0);
    }
    static void runCases()throws Exception {
        var admin=new NhanVien_DAO().kiemTraDangNhap("quanLy","123");
        test("TC_SC02",()->{var ui=new LapPhieuSuaChua_UI(admin);show(ui);var jobs=(DefaultTableModel)field(ui,"modelCongViec");var parts=(DefaultTableModel)field(ui,"modelPhuTung");int j=jobs.getRowCount(),p=parts.getRowCount();set(ui,"txtTenCongViec","");call(ui,"themCongViec");((JComboBox<?>)field(ui,"cboPhuTung")).setSelectedIndex(1);set(ui,"txtSoLuongPT","0");call(ui,"themPhuTung");check(j==jobs.getRowCount()&&p==parts.getRowCount()&&messages.stream().anyMatch(x->x.contains("nội dung công việc"))&&messages.stream().anyMatch(x->x.contains("lớn hơn 0")),"Không chặn đủ hai dữ liệu sai");return "Handler Swing chặn tên công việc rỗng và số lượng phụ tùng 0; hai bảng không thêm dòng.";});
        test("TC_HD02",()->{var lines=new DuLieu_DAO().layChiTietTheoPhieu("TN112");HoaDon h=new HoaDon("HD_RPT_DISC","TN112","KH112","","","","",200000,450000,10,585000,"Tiền mặt","","Report",HoaDon.DA_THANH_TOAN);check(new HoaDon_DAO().themHoaDon(h,"NV05",lines),"Không lưu được hóa đơn giảm giá");check(count("SELECT tongTien FROM HoaDon WHERE maHD='HD_RPT_DISC'")==585000&&count("SELECT tienDichVu+tienPhuTung FROM HoaDon WHERE maHD='HD_RPT_DISC'")==650000,"Tính giảm giá sai");return "DAO SQL: HD_RPT_DISC lưu tổng 585000, công 200000, phụ tùng 450000, giảm 10%. Chưa thử nút thanh toán UI.";});
        final LapPhieuBaoHanh_UI bh=new LapPhieuBaoHanh_UI(admin);show(bh);
        JComboBox<?> invoices=(JComboBox<?>)field(bh,"cboHoaDon");
        for(int i=0;i<invoices.getItemCount();i++)if(invoices.getItemAt(i).toString().contains("HD_RPT_DISC"))invoices.setSelectedIndex(i);
        test("TC_BH02",()->{var m=(DefaultTableModel)field(bh,"model");m.setRowCount(0);long before=count("SELECT COUNT(*) FROM PhieuBaoHanh");call(bh,"luuPhieu");set(bh,"txtNgayBatDau",LocalDate.now().toString());m.addRow(new Object[]{1,"Lọc dầu",0,"","",""});call(bh,"luuPhieu");check(count("SELECT COUNT(*) FROM PhieuBaoHanh")==before&&messages.stream().anyMatch(s->s.contains("ít nhất"))&&messages.stream().anyMatch(s->s.contains("không hợp lệ")),"Không chặn đúng cả phiếu rỗng và 0 tháng");return "Form Swing từ chối phiếu không hạng mục và dòng thời hạn 0 tháng; SQL không thêm phiếu.";});
        test("TC_BH01",()->{var m=(DefaultTableModel)field(bh,"model");m.setRowCount(0);set(bh,"txtNgayBatDau",LocalDate.now().toString());m.addRow(new Object[]{1,"Lọc dầu",6,"","Lỗi sản phẩm","Report"});call(bh,"luuPhieu");check(count("SELECT COUNT(*) FROM PhieuBaoHanh b JOIN HangMucBaoHanh h ON h.maPBH=b.maPBH WHERE b.maHoaDon='HD_RPT_DISC' AND h.thoiHanBaoHanh=6 AND h.ngayHetHan=DATEADD(month,6,b.ngayBD)")==1,"Không có phiếu/hạng mục đúng hạn cho HD_RPT_DISC");return "Handler lưu bảo hành cho HD_RPT_DISC thành công; SQL có đầu phiếu, hạng mục Lọc dầu và hạn đúng +6 tháng.";});
        test("TC_BC02",()->{var stats=new TrangChuTiepTan_DAO().layThongKeTongQuan();check(stats.getTongKhachHang()==count("SELECT COUNT(*) FROM KhachHang"),"Tổng khách không khớp");check(stats.getTongPhuongTien()==count("SELECT COUNT(*) FROM PhuongTien"),"Tổng xe không khớp");check(stats.getTongPhieuTiepNhan()==count("SELECT COUNT(*) FROM PhieuTiepNhan"),"Tổng tiếp nhận không khớp");return "DAO thống kê khớp SQL về tổng khách, xe và tiếp nhận; chưa kiểm tra đầy đủ KPI ngày/bảo hành trên giao diện.";});
    }
}

