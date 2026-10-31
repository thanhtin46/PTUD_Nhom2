package UI;
import Entity.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
/** Trang làm việc theo vai trò; truy vấn thật trên tác vụ nền. */
public class TrangChuBoPhan_UI extends JPanel {
    private final NhanVien nv;
    private final JLabel status=new JLabel("Đang tải công việc...");
    private final JTabbedPane tabs=new JTabbedPane();
    private final JButton reload=UiHelper.nut("Làm mới", UiIcon.of(UiIcon.REFRESH, 14), UiHelper.XAM, null);
    public TrangChuBoPhan_UI(NhanVien nv,Main_UI main) {
        this.nv=nv;setLayout(new BorderLayout(12,12));setBackground(UiHelper.NEN);
        setBorder(BorderFactory.createEmptyBorder(22,22,22,22));
        JPanel header=new JPanel(new BorderLayout());header.setOpaque(false);
        header.add(UiHelper.tieuDeTrang("Bàn làm việc — "+nv.getVaiTro()),BorderLayout.NORTH);
        header.add(status,BorderLayout.CENTER);header.add(reload,BorderLayout.EAST);add(header,BorderLayout.NORTH);
        JPanel actions=new JPanel(new GridLayout(0,3,8,8));actions.setOpaque(false);
        QuyenTruyCap.TRANG.forEach((name,q)->{if(QuyenTruyCap.moTrang(nv,name)) {
            JButton b=UiHelper.nut(name, UiIcon.of(UiIcon.CHECK, 14), UiHelper.XANH_TRUNG, e->main.chuyenTrang(name));actions.add(b);
        }});
        JPanel body=new JPanel(new BorderLayout(10,15));body.setOpaque(false);body.add(actions,BorderLayout.NORTH);body.add(tabs,BorderLayout.CENTER);add(body);
        reload.addActionListener(e->load());load();
    }
    private record Bang(String ten,Vector<String> cols,Vector<Vector<Object>> rows) {}
    private Bang query(Connection c,String name,String sql, String staff)throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql)) {
            if(staff!=null)p.setString(1,staff);
            try(ResultSet r=p.executeQuery()) {
                Vector<String> cols=new Vector<>();Vector<Vector<Object>> rows=new Vector<>();int count=r.getMetaData().getColumnCount();
                for(int i=1;i<=count;i++)cols.add(r.getMetaData().getColumnLabel(i));
                while(r.next()){Vector<Object> row=new Vector<>();for(int i=1;i<=count;i++)row.add(r.getObject(i));rows.add(row);}
                return new Bang(name,cols,rows);
            }
        }
    }
    private void load() {
        reload.setEnabled(false);status.setText("Đang tải dữ liệu SQL...");
        new SwingWorker<java.util.List<Bang>,Void>() {
            protected java.util.List<Bang> doInBackground() throws Exception {
                java.util.List<Bang> result=new ArrayList<>();
                synchronized(ConnectDB.ConnectDB.getInstance()) {
                    Connection c=ConnectDB.ConnectDB.getInstance().getConnection();if(c==null)throw new SQLException("Không kết nối SQL Server");
                    switch(QuyenTruyCap.vaiTro(nv.getVaiTro())) {
                        case TIEP_TAN -> result.add(query(c,"Tiếp nhận và tiến độ","SELECT t.maPTN AS [Phiếu],v.bienSoXe AS [Biển số],k.hoTen AS [Khách],t.ngayTN AS [Ngày nhận],t.trangThaiXe AS [Trạng thái] FROM PhieuTiepNhan t JOIN PhuongTien v ON v.soVIN=t.soVIN JOIN KhachHang k ON k.maKH=t.maKH ORDER BY t.ngayTN DESC",null));
                        case SUA_CHUA -> result.add(query(c,"Phiếu được giao","SELECT s.maPSC AS [Phiếu],v.bienSoXe AS [Biển số],t.yeuCauSC AS [Yêu cầu],s.ngayHoanThanhDuKien AS [Ngày hẹn],s.trangThai AS [Tiến độ],x.trangThai AS [Xuất phụ tùng] FROM PhieuSuaChua s JOIN PhuongTien v ON v.soVIN=s.soVIN LEFT JOIN PhieuTiepNhan t ON t.maPTN=s.maPTN LEFT JOIN PhieuXuatKho x ON x.maPSC=s.maPSC WHERE s.maNV=? ORDER BY s.ngayBD DESC",nv.getMaNV()));
                        case KHO -> {
                            result.add(query(c,"Chờ xuất","SELECT maPhieuXuat,maPSC,ngayXuat,trangThai FROM PhieuXuatKho WHERE trangThai IN (N'Chờ xuất','CHO_XUAT')",null));
                            result.add(query(c,"Chờ nhập","SELECT maPhieuNhap,maNCC,ngayDuKienGiao,trangThai FROM PhieuNhapKho WHERE trangThai='CHO_NHAP'",null));
                            result.add(query(c,"Sắp hết hàng","SELECT p.maPT,p.tenPT,k.soLuongTon,k.soLTT FROM PhuTung p JOIN Kho k ON k.maPT=p.maPT WHERE k.soLuongTon<=k.soLTT",null));
                        }
                        case THU_NGAN -> {
                            result.add(query(c,"Chờ thanh toán","SELECT s.maPSC,v.bienSoXe,k.hoTen,s.trangThai FROM PhieuSuaChua s JOIN PhuongTien v ON v.soVIN=s.soVIN JOIN KhachHang k ON k.maKH=s.maKH WHERE s.trangThai IN ('HOAN_THANH','DA_HOAN_THANH',N'Hoàn thành') AND NOT EXISTS(SELECT 1 FROM HoaDon h WHERE h.soPhieuSC=s.maPSC AND h.trangThai NOT IN ('DA_HUY',N'Đã hủy'))",null));
                            result.add(query(c,"Hóa đơn hôm nay","SELECT maHD,maKH,tongTien,trangThai FROM HoaDon WHERE ngayLap=CAST(GETDATE() AS date)",null));
                            result.add(query(c,"Đã thu hôm nay","SELECT COALESCE(SUM(tongTien),0) AS [Tiền đã thu] FROM HoaDon WHERE ngayLap=CAST(GETDATE() AS date) AND trangThai IN ('DA_THANH_TOAN',N'Đã thanh toán')",null));
                        }
                        default -> { }
                    }
                }return result;
            }
            protected void done() {
                reload.setEnabled(true);tabs.removeAll();
                try {
                    int total=0;for(Bang b:get()) {total+=b.rows.size();JTable table=UiHelper.bang(new DefaultTableModel(b.rows,b.cols){public boolean isCellEditable(int r,int c){return false;}});tabs.addTab(b.ten,new JScrollPane(table));}
                    status.setText(total==0?"Không có công việc hoặc vai trò chưa được cấu hình.":"Dữ liệu SQL đã được cập nhật. Chọn chức năng phía trên để xử lý.");
                }catch(Exception ex){status.setText("Không tải được dữ liệu. Hãy kiểm tra kết nối rồi bấm Làm mới.");}
            }
        }.execute();
    }
}
