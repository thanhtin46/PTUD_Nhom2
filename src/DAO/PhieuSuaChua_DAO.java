package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietDichVu;
import Entity.DuLieuPhieuSuaChua;
import java.math.BigDecimal;
import java.sql.*;

/** Lưu đầu phiếu, phụ tùng và tiền công trong một giao dịch. */
public class PhieuSuaChua_DAO {
    private final Connection ketNoiKiemThu;

    public PhieuSuaChua_DAO() { this(null); }
    public PhieuSuaChua_DAO(Connection ketNoi) { this.ketNoiKiemThu = ketNoi; }

    /** maNhanVienGioiHan = null khi người thực hiện có quyền phân công. */
    public String luu(DuLieuPhieuSuaChua duLieu, String maNhanVienGioiHan) throws SQLException {
        synchronized (ConnectDB.getInstance()) {
            Connection con = ketNoiKiemThu != null ? ketNoiKiemThu : ConnectDB.getInstance().getConnection();
            if (con == null) throw new SQLException("Không kết nối được SQL Server.");
            boolean autoCu = con.getAutoCommit();
            if (!autoCu) throw new SQLException("Kết nối đang có giao dịch khác. Vui lòng thử lại.");
            con.setAutoCommit(false);
            try {
                String maPSC = LienKetPhieu.maSuaChua(con, duLieu.getMaTiepNhan());
                kiemTraPhieu(con, maPSC, maNhanVienGioiHan);
                luuDauPhieu(con, maPSC, duLieu);
                luuPhuTung(con, maPSC, duLieu);
                luuCongViec(con, maPSC, duLieu);
                con.commit();
                return maPSC;
            } catch (SQLException | RuntimeException ex) {
                try { con.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                throw ex;
            } finally {
                con.setAutoCommit(autoCu);
            }
        }
    }

    private void kiemTraPhieu(Connection con, String maPSC, String maNV) throws SQLException {
        // Giữ khóa trong lúc thay chi tiết, tránh đổi phân công hoặc xuất kho xen giữa.
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT maNV FROM PhieuSuaChua WITH (UPDLOCK, HOLDLOCK) WHERE maPSC=?")) {
            ps.setString(1, maPSC);
            try (ResultSet rs = ps.executeQuery()) {
                boolean tonTai = rs.next();
                if (maNV != null && (!tonTai || !maNV.equals(rs.getString("maNV"))))
                    throw new SQLException("Phiếu không thuộc phạm vi được giao.");
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM PhieuXuatKho WITH (UPDLOCK, HOLDLOCK) WHERE maPSC=?")) {
            ps.setString(1, maPSC);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0)
                    throw new SQLException("Phiếu đã xuất kho. Không thể ghi đè danh sách phụ tùng.");
            }
        }
    }

    private void luuDauPhieu(Connection con, String maPSC, DuLieuPhieuSuaChua d) throws SQLException {
        String maKH;
        String soVIN;
        try (PreparedStatement ps = con.prepareStatement("SELECT maKH, soVIN FROM PhieuTiepNhan WHERE maPTN=?")) {
            ps.setString(1, d.getMaTiepNhan());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy phiếu tiếp nhận " + d.getMaTiepNhan());
                maKH = rs.getString("maKH");
                soVIN = rs.getString("soVIN");
            }
        }
        int soDong;
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE PhieuSuaChua SET maNV=?, maKH=?, soVIN=?, ngayHoanThanhDuKien=?, maPTN=? WHERE maPSC=?")) {
            ps.setString(1, d.getMaKyThuatVien());
            ps.setString(2, maKH);
            ps.setString(3, soVIN);
            ps.setDate(4, Date.valueOf(d.getNgayHen()));
            ps.setString(5, d.getMaTiepNhan());
            ps.setString(6, maPSC);
            soDong = ps.executeUpdate();
        }
        if (soDong == 0) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO PhieuSuaChua (maPSC, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai, maPTN) "
                    + "VALUES (?, ?, ?, ?, SYSDATETIME(), ?, N'CHO_SUA', ?)")) {
                ps.setString(1, maPSC);
                ps.setString(2, d.getMaKyThuatVien());
                ps.setString(3, maKH);
                ps.setString(4, soVIN);
                ps.setDate(5, Date.valueOf(d.getNgayHen()));
                ps.setString(6, d.getMaTiepNhan());
                ps.executeUpdate();
            }
        }
    }

    private void luuPhuTung(Connection con, String maPSC, DuLieuPhieuSuaChua d) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietSuaChua WHERE maPSC=?")) {
            ps.setString(1, maPSC);
            ps.executeUpdate();
        }
        String sql = "INSERT INTO ChiTietSuaChua (maPSC, maPT, noiDungSC, tenPhuTung, soLuong, donGia, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, N'CHO_THUC_HIEN')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (ChiTietDichVu ct : d.getPhuTungs()) {
                ps.setString(1, maPSC);
                ps.setString(2, ct.getMaPT());
                ps.setString(3, "Thay thế theo phiếu sửa chữa");
                ps.setString(4, ct.getTenHangMuc());
                ps.setInt(5, ct.getSoLuong());
                ps.setBigDecimal(6, BigDecimal.valueOf(ct.getDonGia()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void luuCongViec(Connection con, String maPSC, DuLieuPhieuSuaChua d) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietCongViecSuaChua WHERE maPSC=?")) {
            ps.setString(1, maPSC);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ChiTietCongViecSuaChua (maPSC, stt, noiDungCongViec, tenKTV, tienCong) VALUES (?, ?, ?, ?, ?)")) {
            int stt = 1;
            for (DuLieuPhieuSuaChua.CongViec cv : d.getCongViecs()) {
                ps.setString(1, maPSC);
                ps.setInt(2, stt++);
                ps.setString(3, cv.getNoiDung());
                ps.setString(4, cv.getTenKyThuatVien());
                ps.setBigDecimal(5, BigDecimal.valueOf(cv.getTienCong()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public boolean duocPhanCong(String maNV, String maPhieu) throws SQLException {
        Connection con = ketNoiKiemThu != null ? ketNoiKiemThu : ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM PhieuSuaChua WHERE maNV=? AND (maPSC=? OR maPTN=?)")) {
            ps.setString(1, maNV);
            ps.setString(2, maPhieu);
            ps.setString(3, maPhieu);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
