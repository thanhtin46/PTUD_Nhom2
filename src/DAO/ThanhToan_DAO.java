package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietDichVu;
import Entity.HoaDon;
import java.sql.*;
import java.util.List;
import java.util.HashSet;

/** Lưu hóa đơn và hai loại chi tiết trong một giao dịch. Tồn kho do phiếu xuất quản lý. */
public class ThanhToan_DAO {
    public boolean luu(HoaDon hd, String maNV, List<ChiTietDichVu> details) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || hd == null || details == null || details.isEmpty()) return false;
        boolean auto = true;
        try {
            auto = con.getAutoCommit();
            con.setAutoCommit(false);
            String repair = hd.getSoPhieuSC();
            if (repair != null && !repair.isBlank()) {
                repair = LienKetPhieu.maSuaChua(con, repair);
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM HoaDon WHERE maHD=? AND trangThai NOT IN ('DA_HUY',N'Đã hủy')")) {
                ps.setString(1, hd.getMaHD());
                try (ResultSet rs = ps.executeQuery()) { rs.next(); if (rs.getInt(1)>0) throw new SQLException("Mã hóa đơn đã tồn tại"); }
            }
            long labour = 0, parts = 0;
            HashSet<String> partIds = new HashSet<>();
            for (ChiTietDichVu ct : details) {
                if (ct.getSoLuong() <= 0 || ct.getDonGia() < 0) throw new SQLException("Số lượng/đơn giá không hợp lệ");
                if ("Tiền công".equalsIgnoreCase(ct.getLoai())) labour += ct.getThanhTien();
                else {
                    if (ct.getMaPT() == null || ct.getMaPT().isBlank() || !partIds.add(ct.getMaPT())) throw new SQLException("Mã phụ tùng thiếu hoặc trùng");
                    parts += ct.getThanhTien();
                }
            }
            double discount = hd.getGiamGiaPhanTram();
            if (discount < 0 || discount > 100 || !Double.isFinite(discount)
                    || labour != hd.getTienDichVu() || parts != hd.getTienPhuTung()
                    || hd.getTongTien() != labour + parts - Math.round((labour + parts) * discount / 100))
                throw new SQLException("Tổng hóa đơn không khớp chi tiết");
            String payment = hd.getPhuongThuc() == null ? "" : hd.getPhuongThuc().toLowerCase();
            payment = payment.contains("chuyển") || payment.contains("qr") ? "CHUYEN_KHOAN"
                    : payment.contains("thẻ") || payment.contains("pos") ? "THE_POS" : "TIEN_MAT";
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO HoaDon(maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES(?,?,?,CAST(GETDATE() AS date),?,?,?,?,?,?,'DA_THANH_TOAN')")) {
                String ghiChu = hd.getGhiChu();
                if ((ghiChu == null || ghiChu.isBlank()) && repair != null && !repair.isBlank()) {
                    ghiChu = "Thanh toán " + repair;
                }
                ps.setString(1,hd.getMaHD()); ps.setString(2,hd.getMaKH()); ps.setString(3,maNV);
                ps.setString(4,payment); ps.setLong(5,labour); ps.setLong(6,parts);
                ps.setDouble(7,discount); ps.setLong(8,hd.getTongTien()); ps.setString(9,ghiChu); ps.executeUpdate();
                if (repair != null && !repair.isBlank()) {
                    try (PreparedStatement psUpdate = con.prepareStatement("UPDATE PhieuSuaChua SET trangThai='HOAN_THANH' WHERE maPSC=? OR maPTN=?")) {
                        psUpdate.setString(1, repair); psUpdate.setString(2, repair); psUpdate.executeUpdate();
                    }
                }
            }
            int index = 0;
            for (ChiTietDichVu ct : details) {
                boolean service = "Tiền công".equalsIgnoreCase(ct.getLoai());
                String sql = service ? "INSERT INTO ChiTietDichVuHoaDon(maHD,stt,tenDichVu,soLuong,donGia) VALUES(?,?,?,?,?)"
                        : "INSERT INTO ChiTietHoaDon(maHD,maPT,tenPhuTung,soLuong,donGia) VALUES(?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1,hd.getMaHD());
                    if (service) ps.setInt(2,++index); else ps.setString(2,ct.getMaPT());
                    ps.setString(3,ct.getTenHangMuc()); ps.setInt(4,ct.getSoLuong()); ps.setLong(5,ct.getDonGia()); ps.executeUpdate();
                }
            }
            con.commit();
            return true;
        } catch (SQLException | RuntimeException ex) {
            try { con.rollback(); } catch (SQLException ignored) {}
            System.err.println("Không lưu được hóa đơn: " + ex.getMessage());
            return false;
        } finally { try { con.setAutoCommit(auto); } catch (SQLException ignored) {} }
    }

    /** Hủy chứng từ thanh toán không đồng nghĩa đã nhận lại phụ tùng vào kho. */
    public boolean huy(String maHD) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement("UPDATE HoaDon SET trangThai='DA_HUY' WHERE maHD=? AND trangThai NOT IN ('DA_HUY',N'Đã hủy') AND NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maHoaDon=HoaDon.maHD AND trangThaiBH NOT IN ('DA_HUY',N'Đã hủy'))")) {
            ps.setString(1,maHD); return ps.executeUpdate()==1;
        } catch (SQLException ex) { System.err.println("Không hủy được hóa đơn: " + ex.getMessage()); return false; }
    }
}
