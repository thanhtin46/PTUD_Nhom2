
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

    // =========================================================
    // 1. KIỂM TRA PSC ĐÃ XUẤT KHO HAY CHƯA
    // =========================================================
    public boolean daXuatKhoChoPSC(String maPSC) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM PhieuXuatKho " +
                        "WHERE maPSC = ? AND trangThai = N'Đã xuất'";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, maPSC);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Tương thích với code cũ sử dụng maPTN.
    // Luồng nghiệp vụ mới nên gọi daXuatKhoChoPSC().
    @Deprecated
    public boolean daXuatKhoChoPTN(String maPTN) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM PhieuXuatKho px " +
                        "JOIN PhieuSuaChua psc ON px.maPSC = psc.maPSC " +
                        "JOIN PhieuTiepNhan ptn ON psc.soVIN = ptn.soVIN " +
                        "WHERE ptn.maPTN = ? AND px.trangThai = N'Đã xuất'";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, maPTN);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // =========================================================
    // 2. TẠO YÊU CẦU XUẤT KHO TỪ PHIẾU SỬA CHỮA
    //
    // Quy ước model giao diện:
    // Cột 0: Checkbox chọn
    // Cột 2: Mã phụ tùng
    // Cột 7: Số lượng yêu cầu
    // Cột 8: Số lượng thực xuất
    //
    // Chỉ lưu các dòng được chọn. Nếu giao diện của bạn có
    // cấu trúc cột khác, hãy chỉnh các chỉ số tại đây.
    // =========================================================
    public String taoPhieuXuat(
            String maPSC,
            String maNV,
            DefaultTableModel model) {

        if (maPSC == null || maPSC.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn phiếu sửa chữa!");
        }

        if (maNV == null || maNV.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Không xác định được nhân viên kho!");
        }

        if (model == null || model.getRowCount() == 0) {
            throw new IllegalArgumentException(
                    "Vui lòng thêm ít nhất một phụ tùng!");
        }

        String maPX = "PXK" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));

        String sqlPhieu =
                "INSERT INTO PhieuXuatKho " +
                        "(maPhieuXuat, maPSC, maNV, ngayXuat, trangThai) " +
                        "VALUES (?, ?, ?, GETDATE(), N'Chờ xuất')";

        String sqlChiTiet =
                "INSERT INTO ChiTietPhieuXuatKho " +
                        "(maPhieuXuat, maPT, soLuong, lyDoXuat) " +
                        "VALUES (?, ?, ?, ?)";

        Connection con = getConnection();

        if (con == null) {
            throw new RuntimeException("Không kết nối được database!");
        }

        boolean autoCommitCu;

        try {
            autoCommitCu = con.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException("Không đọc được trạng thái kết nối.", e);
        }

        try {
            con.setAutoCommit(false);

            // Kiểm tra PSC tồn tại trước khi tạo phiếu xuất.
            String sqlKiemTraPSC =
                    "SELECT COUNT(*) FROM PhieuSuaChua WHERE maPSC = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlKiemTraPSC)) {
                ps.setString(1, maPSC);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new SQLException(
                                "Không tìm thấy phiếu sửa chữa " + maPSC);
                    }
                }
            }

            // Tránh tạo nhiều yêu cầu đang chờ cho cùng một PSC.
            String sqlKiemTraPhieu =
                    "SELECT COUNT(*) FROM PhieuXuatKho " +
                            "WHERE maPSC = ? AND trangThai = N'Chờ xuất'";

            try (PreparedStatement ps = con.prepareStatement(sqlKiemTraPhieu)) {
                ps.setString(1, maPSC);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException(
                                "Phiếu sửa chữa này đã có yêu cầu xuất kho đang chờ.");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {
                ps.setString(1, maPX);
                ps.setString(2, maPSC);
                ps.setString(3, maNV);
                ps.executeUpdate();
            }

            int soDongDaThem = 0;

            try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                for (int i = 0; i < model.getRowCount(); i++) {

                    Object giaTriChon = model.getValueAt(i, 0);

                    // Nếu cột chọn là checkbox, chỉ lưu dòng được chọn.
                    if (giaTriChon instanceof Boolean
                            && !((Boolean) giaTriChon)) {
                        continue;
                    }

                    Object giaTriMaPT = model.getValueAt(i, 2);

                    if (giaTriMaPT == null
                            || giaTriMaPT.toString().trim().isEmpty()) {
                        throw new SQLException(
                                "Thiếu mã phụ tùng ở dòng " + (i + 1));
                    }

                    String maPT = giaTriMaPT.toString().trim();

                    Object giaTriSL = model.getValueAt(i, 7);

                    if (giaTriSL == null
                            || giaTriSL.toString().trim().isEmpty()) {
                        throw new SQLException(
                                "Thiếu số lượng yêu cầu của phụ tùng " + maPT);
                    }

                    int soLuong;
                    try {
                        soLuong = Integer.parseInt(giaTriSL.toString().trim());
                    } catch (NumberFormatException e) {
                        throw new SQLException(
                                "Số lượng không hợp lệ của phụ tùng " + maPT);
                    }

                    if (soLuong <= 0) {
                        throw new SQLException(
                                "Số lượng phải lớn hơn 0: " + maPT);
                    }

                    String lyDo = "Xuất phụ tùng theo phiếu sửa chữa " + maPSC;

                    ps.setString(1, maPX);
                    ps.setString(2, maPT);
                    ps.setInt(3, soLuong);
                    ps.setString(4, lyDo);
                    ps.addBatch();

                    soDongDaThem++;
                }

                if (soDongDaThem == 0) {
                    throw new SQLException(
                            "Vui lòng chọn ít nhất một phụ tùng!");
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
                    "Không thể tạo yêu cầu xuất kho: " + e.getMessage(), e);

        } finally {
            try {
                con.setAutoCommit(autoCommitCu);
            } catch (SQLException ignored) {
            }
        }
    }

    // =========================================================
    // 3. LẤY DANH SÁCH PHIẾU XUẤT ĐANG CHỜ
    //
    // model cần ít nhất 4 cột:
    // Mã phiếu xuất | Mã PSC | Ngày xuất | Trạng thái
    // =========================================================
    public void layDanhSachPhieuChoXuat(DefaultTableModel model)
            throws SQLException {

        model.setRowCount(0);

        String sql =
                "SELECT maPhieuXuat, maPSC, ngayXuat, trangThai " +
                        "FROM PhieuXuatKho " +
                        "WHERE trangThai = N'Chờ xuất' " +
                        "ORDER BY ngayXuat DESC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("maPhieuXuat"),
                        rs.getString("maPSC"),
                        rs.getTimestamp("ngayXuat"),
                        rs.getString("trangThai")
                });
            }
        }
    }

    // =========================================================
    // 4. LẤY CHI TIẾT PHỤ TÙNG CỦA PHIẾU XUẤT ĐÃ TẠO
    //
    // Dùng để tải lại phiếu khi nhân viên kho chọn yêu cầu.
    // =========================================================
    public void layChiTietPhieuXuat(
            String maPX,
            DefaultTableModel model) throws SQLException {

        model.setRowCount(0);

        String sql =
                "SELECT ct.maPT, pt.tenPT, ct.soLuong, " +
                        "ct.lyDoXuat, pt.soLuongTon " +
                        "FROM ChiTietPhieuXuatKho ct " +
                        "JOIN PhuTung pt ON pt.maPT = ct.maPT " +
                        "WHERE ct.maPhieuXuat = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, maPX);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("maPT"),
                            rs.getString("tenPT"),
                            rs.getInt("soLuong"),
                            rs.getString("lyDoXuat"),
                            rs.getInt("soLuongTon")
                    });
                }
            }
        }
    }

    // =========================================================
    // 5. XÁC NHẬN XUẤT KHO VÀ TRỪ TỒN KHO
    // =========================================================
    public boolean xacNhanXuat(String maPX) throws SQLException {

        if (maPX == null || maPX.trim().isEmpty()) {
            throw new SQLException("Vui lòng chọn phiếu xuất kho!");
        }

        Connection con = getConnection();

        if (con == null) {
            throw new SQLException("Không có kết nối database!");
        }

        boolean autoCommitCu = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            String trangThai;
            String sqlKiemTra =
                    "SELECT trangThai FROM PhieuXuatKho " +
                            "WITH (UPDLOCK, HOLDLOCK) " +
                            "WHERE maPhieuXuat = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlKiemTra)) {
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
                        "Phiếu không ở trạng thái Chờ xuất hoặc đã được xử lý.");
            }

            String sqlChiTiet =
                    "SELECT maPT, soLuong FROM ChiTietPhieuXuatKho " +
                            "WHERE maPhieuXuat = ?";

            boolean coChiTiet = false;

            try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                ps.setString(1, maPX);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        coChiTiet = true;

                        String maPT = rs.getString("maPT");
                        int soLuong = rs.getInt("soLuong");

                        if (soLuong <= 0) {
                            throw new SQLException(
                                    "Số lượng không hợp lệ của phụ tùng " + maPT);
                        }

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
                    "UPDATE PhieuXuatKho " +
                            "SET trangThai = N'Đã xuất' " +
                            "WHERE maPhieuXuat = ? AND trangThai = N'Chờ xuất'";

            try (PreparedStatement ps = con.prepareStatement(sqlCapNhat)) {
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
