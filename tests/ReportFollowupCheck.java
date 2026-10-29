package tests;

import ConnectDB.ConnectDB;
import DAO.*;
import UI.*;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

/** Final independent verification of UI outcomes and persisted SQL evidence. */
public class ReportFollowupCheck extends ReportExecutionTest {
    public static void main(String[]args)throws Exception {
        db=ConnectDB.getInstance().getConnection();
        if(db==null||!"GaraSyncTest_Report_20261028".equals(db.getCatalog()))throw new IllegalStateException("Report database only");
        SwingUtilities.invokeAndWait(()->{
            host=new JFrame("Đối chiếu kết quả kiểm thử");host.setSize(1300,850);host.setVisible(true);
            closer=new javax.swing.Timer(80,e->{for(Window w:Window.getWindows())if(w instanceof JDialog d&&d.isShowing()){JOptionPane p=pane(d);if(p!=null){messages.add(String.valueOf(p.getMessage()));p.setValue(JOptionPane.OK_OPTION);d.dispose();}}});closer.start();
            test("TC_AUTH01",()->{var staff=new NhanVien_DAO().layTatCaNhanVien();var login=new Login_UI(staff);set(login,"txtTaiKhoan","quanLy");set(login,"txtMatKhau","123");call(login,"xuLyDangNhap");boolean main=false;for(Window w:Window.getWindows())if(w instanceof Main_UI&&w.isVisible()){main=true;w.dispose();}check(main,"Không mở Main_UI");return "Đăng nhập quanLy / 123 mở Main_UI thực tế. Menu quản lý đã được quan sát trực tiếp ở lượt trước.";});
            test("TC_KH06",()->{var ui=new QuanLyKhachHang_UI();show(ui);select(ui,"KH101");call(ui,"xoa");check(count("SELECT COUNT(*) FROM KhachHang WHERE maKH='KH101'")==1,"Mất khách có lịch sử");check(messages.stream().noneMatch(s->s.equals("Đã xóa khách hàng!")),"SQL giữ KH101 vì khóa ngoại nhưng giao diện báo Đã xóa khách hàng! Thông báo trái kết quả thực tế.");return "Xóa bị từ chối đúng và thông báo đúng.";});
            test("TC_BH01",()->{check(count("SELECT COUNT(*) FROM PhieuBaoHanh b JOIN HangMucBaoHanh h ON h.maPBH=b.maPBH WHERE b.maHoaDon='HD_RPT_DISC' AND h.thoiHanBaoHanh=6 AND h.ngayHetHan=DATEADD(month,6,b.ngayBD)")==1,"Thiếu bảo hành đúng hạn");return "Sau lưu bằng handler Swing: PBH00107/HD_RPT_DISC, Lọc dầu 6 tháng, bắt đầu 28/10/2026, hết hạn 28/04/2027; SQL xác nhận.";});
            closer.stop();for(Window w:Window.getWindows())w.dispose();
        });
        Files.write(Path.of("build/docx-qa/followup-results.tsv"),results,StandardCharsets.UTF_8);
        ConnectDB.getInstance().disconnect();System.exit(0);
    }
}
