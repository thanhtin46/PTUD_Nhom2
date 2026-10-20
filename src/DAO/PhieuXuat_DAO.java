
package DAO;

import ConnectDB.ConnectDB;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PhieuXuat_DAO {

    private Connection getConnection() {
        return ConnectDB.getInstance().getConnection();
    }

    public boolean daXuatKhoChoPTN(String maPTN) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM PhieuXuatKho " +
                        "WHERE maPTN = ? AND trangThai = N'Đã xuất'";

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql)) {

            ps.setString(1, maPTN);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }


    // Tao ma phieu xuat kho
    public String taoPhieuXuat(
            String maPTN, String maNV,
            DefaultTableModel model) {

        if (model.getRowCount() == 0) {
            throw new IllegalArgumentException(
                    "Vui lòng thêm ít nhất một phụ tùng!");
        }

        String maPX = "PXK" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyMMddHHmmss"));

        String sqlPhieu =
                "INSERT INTO PhieuXuatKho " +
                        "(maPhieuXuat, maPTN, maNV, ngayXuat, trangThai) " +
                        "VALUES (?, ?, ?, GETDATE(), N'Chờ xuất')";

        String sqlChiTiet =
                "INSERT INTO ChiTietPhieuXuatKho " +
                        "(maPhieuXuat, maPT, soLuong, lyDoXuat) " +
                        "VALUES (?, ?, ?, ?)";

        Connection con = getConnection();

        if (con == null) {
            throw new RuntimeException("Không kết nối được database!");
        }

        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                ps.setString(1, maPX);
                ps.setString(2, maPTN);
                ps.setString(3, maNV);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    String maPT = model.getValueAt(i, 1).toString();
                    int soLuong = Integer.parseInt(
                            model.getValueAt(i, 4).toString());
                    String lyDo = model.getValueAt(i, 7).toString();

                    if (soLuong <= 0) {
                        throw new IllegalArgumentException(
                                "Số lượng phải lớn hơn 0!");
                    }

                    ps.setString(1, maPX);
                    ps.setString(2, maPT);
                    ps.setInt(3, soLuong);
                    ps.setString(4, lyDo);
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            con.commit();
            return maPX;

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }
            throw new RuntimeException(
                    "Không thể tạo phiếu xuất kho: " + e.getMessage(), e);

        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    // Xac nhan xuat kho va tru ton kho
    public boolean xacNhanXuat(String maPX) throws SQLException {

        Connection con = getConnection();

        if (con == null) {
            throw new SQLException("Không có kết nối database!");
        }

        boolean autoCommitCu = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            String trangThai = null;

            String sqlKiemTra =
                    "SELECT trangThai FROM PhieuXuatKho " +
                            "WITH (UPDLOCK, HOLDLOCK) " +
                            "WHERE maPhieuXuat = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(sqlKiemTra)) {

                ps.setString(1, maPX);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy phiếu xuất!");
                    }
                    trangThai = rs.getString("trangThai");
                }
            }

            if (!"Chờ xuất".equals(trangThai)) {
                throw new SQLException(
                        "Phiếu đã được xuất hoặc đã bị hủy!");
            }

            String sqlChiTiet =
                    "SELECT maPT, soLuong FROM ChiTietPhieuXuatKho " +
                            "WHERE maPhieuXuat = ?";

            boolean coChiTiet = false;

            try (PreparedStatement ps =
                         con.prepareStatement(sqlChiTiet)) {

                ps.setString(1, maPX);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        coChiTiet = true;

                        String maPT = rs.getString("maPT");
                        int soLuong = rs.getInt("soLuong");

                        // Tru ton kho neu con du so luong.
                        String sqlTruTon =
                                "UPDATE PhuTung " +
                                        "SET soLuongTon = soLuongTon - ? " +
                                        "WHERE maPT = ? AND soLuongTon >= ?";

                        try (PreparedStatement psTon =
                                     con.prepareStatement(sqlTruTon)) {

                            psTon.setInt(1, soLuong);
                            psTon.setString(2, maPT);
                            psTon.setInt(3, soLuong);

                            if (psTon.executeUpdate() != 1) {
                                throw new SQLException(
                                        "Phụ tùng " + maPT +
                                                " không tồn tại hoặc không đủ tồn kho!");
                            }
                        }
                    }
                }
            }

            if (!coChiTiet) {
                throw new SQLException(
                        "Phiếu xuất chưa có chi tiết phụ tùng!");
            }

            String sqlCapNhat =
                    "UPDATE PhieuXuatKho SET trangThai = N'Đã xuất' " +
                            "WHERE maPhieuXuat = ? AND trangThai = N'Chờ xuất'";

            try (PreparedStatement ps =
                         con.prepareStatement(sqlCapNhat)) {

                ps.setString(1, maPX);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "Không thể cập nhật trạng thái phiếu xuất!");
                }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                e.addSuppressed(ex);
            }

            if (e instanceof SQLException) {
                throw (SQLException) e;
            }

            throw new SQLException("Lỗi xuất kho: " + e.getMessage(), e);

        } finally {
            try {
                con.setAutoCommit(autoCommitCu);
            } catch (SQLException ignored) {
            }
        }
    }
}
