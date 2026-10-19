
package DAO;

import ConnectDB.ConnectDB;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PhieuSuaChua_DAO {

    private static final Map<String, Draft> DRAFTS =
            new ConcurrentHashMap<>();

    public boolean luuPhieu(
            String maPSC,
            String maNV,
            String maKH,
            String bienSo,
            String trangThai,
            String ngayLap,
            String ngayDuKien,
            String ghiChu,
            DefaultTableModel modelCongViec,
            DefaultTableModel modelPhuTung) {

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            System.err.println("Không có kết nối SQL Server.");
            return false;
        }

        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            // 1. Kiểm tra dữ liệu bắt buộc.
            if (maPSC == null || maPSC.trim().isEmpty()) {
                throw new SQLException("Mã phiếu sửa chữa không được để trống.");
            }

            if (maNV == null || maNV.trim().isEmpty()) {
                throw new SQLException("Vui lòng chọn nhân viên phụ trách.");
            }

            if (maKH == null || maKH.trim().isEmpty()) {
                throw new SQLException(
                        "Thiếu mã khách hàng. Hãy lấy mã khách hàng " +
                                "từ phiếu tiếp nhận đã chọn.");
            }

            if (bienSo == null || bienSo.trim().isEmpty()) {
                throw new SQLException("Chưa chọn phương tiện sửa chữa.");
            }


            // 2. Tìm phương tiện theo biển số trong SQL Server
            String soVIN = null;

            String sqlXe =
                    "SELECT TOP 1 soVIN " +
                            "FROM PhuongTien " +
                            "WHERE LTRIM(RTRIM(bienSoXe)) = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlXe)) {
                ps.setString(1, bienSo.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        soVIN = rs.getString("soVIN");
                    }
                }
            }

            if (soVIN == null || soVIN.trim().isEmpty()) {
                throw new SQLException(
                        "Không tìm thấy phương tiện hoặc phương tiện "
                                + "chưa có số VIN. Biển số: " + bienSo);
            }

            // 3. Kiểm tra mã khách hàng có tồn tại trong bảng KhachHang
            String sqlKH =
                    "SELECT COUNT(*) FROM KhachHang WHERE maKH = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlKH)) {
                ps.setString(1, maKH.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new SQLException(
                                "Mã khách hàng không tồn tại trong SQL Server: "
                                        + maKH);
                    }
                }
            }


            // 3. Chuyển ngày giờ giao diện sang kiểu DATE của SQL Server.
            Date ngayBatDau = parseDate(ngayLap);
            Date ngayHoanThanh = parseDate(ngayDuKien);

            // 4. Chuyển trạng thái giao diện.
            String dbStatus = mapTrangThai(trangThai);

            // 5. Kiểm tra phiếu sửa chữa đã tồn tại chưa.
            boolean existed;

            String sqlCheck =
                    "SELECT 1 FROM PhieuSuaChua WHERE maPSC = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlCheck)) {
                ps.setString(1, maPSC.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    existed = rs.next();
                }
            }

            // 6. Thêm mới hoặc cập nhật phiếu sửa chữa.
            if (existed) {

                String sqlUpdate =
                        "UPDATE PhieuSuaChua SET " +
                                "maNV = ?, maKH = ?, soVIN = ?, " +
                                "ngayBD = ?, ngayHoanThanhDuKien = ?, " +
                                "trangThai = ? " +
                                "WHERE maPSC = ?";

                try (PreparedStatement ps =
                             con.prepareStatement(sqlUpdate)) {

                    ps.setString(1, maNV.trim());
                    ps.setString(2, maKH.trim());
                    ps.setString(3, soVIN);
                    ps.setDate(4, ngayBatDau);
                    ps.setDate(5, ngayHoanThanh);
                    ps.setString(6, dbStatus);
                    ps.setString(7, maPSC.trim());

                    ps.executeUpdate();
                }

            } else {

                String sqlInsert =
                        "INSERT INTO PhieuSuaChua " +
                                "(maPSC, maNV, maPhieuXuat, ngayBD, " +
                                "ngayHoanThanhDuKien, trangThai, maKH, soVIN) " +
                                "VALUES (?, ?, NULL, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps =
                             con.prepareStatement(sqlInsert)) {

                    ps.setString(1, maPSC.trim());
                    ps.setString(2, maNV.trim());
                    ps.setDate(3, ngayBatDau);
                    ps.setDate(4, ngayHoanThanh);
                    ps.setString(5, dbStatus);
                    ps.setString(6, maKH.trim());
                    ps.setString(7, soVIN);

                    ps.executeUpdate();
                }
            }

            // 7. Xóa các dòng chi tiết cũ của phiếu.
            String sqlDelete =
                    "DELETE FROM ChiTietSuaChua WHERE maPSC = ?";

            try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                ps.setString(1, maPSC.trim());
                ps.executeUpdate();
            }

            // 8. Lưu danh sách phụ tùng.
            String sqlCT =
                    "INSERT INTO ChiTietSuaChua " +
                            "(maPSC, maPT, noiDungSC, tenPhuTung, " +
                            "soLuong, donGia, trangThai) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";

            if (modelPhuTung != null) {
                try (PreparedStatement ps =
                             con.prepareStatement(sqlCT)) {

                    for (int i = 0;
                         i < modelPhuTung.getRowCount();
                         i++) {

                        String maPT = str(
                                modelPhuTung.getValueAt(i, 1));

                        String tenPT = str(
                                modelPhuTung.getValueAt(i, 2));

                        if (maPT.isEmpty()) {
                            throw new SQLException(
                                    "Thiếu mã phụ tùng tại dòng "
                                            + (i + 1));
                        }

                        int soLuong = Integer.parseInt(
                                str(modelPhuTung.getValueAt(i, 4)));

                        long donGia = parseMoney(
                                modelPhuTung.getValueAt(i, 5));

                        if (soLuong <= 0 || donGia < 0) {
                            throw new SQLException(
                                    "Số lượng hoặc đơn giá không hợp lệ " +
                                            "tại dòng " + (i + 1));
                        }

                        ps.setString(1, maPSC.trim());
                        ps.setString(2, maPT);
                        ps.setString(3, "Thay thế linh kiện: " + tenPT);
                        ps.setString(4, tenPT);
                        ps.setInt(5, soLuong);
                        ps.setLong(6, donGia);
                        ps.setString(7, "CHO_THUC_HIEN");

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }
            }

            // 9. Hoàn tất giao dịch.
            con.commit();
            DRAFTS.remove(maPSC);

            System.out.println(
                    "Lưu phiếu sửa chữa thành công: " + maPSC);

            return true;

        } catch (Exception ex) {

            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }

            System.err.println(
                    "===== LỖI LƯU PHIẾU SỬA CHỮA =====");
            System.err.println("Mã phiếu: " + maPSC);
            System.err.println("Chi tiết: " + ex.getMessage());
            ex.printStackTrace();

            return false;

        } finally {
            try {
                con.setAutoCommit(oldAutoCommit);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Các cột ngày trong database có kiểu DATE.
    private static Date parseDate(String value)
            throws ParseException {

        if (value == null || value.trim().isEmpty()) {
            throw new ParseException(
                    "Ngày không được để trống.", 0);
        }

        // Ví dụ: "20/10/2026 02:02 (3 giờ)"
        String normalized = value.trim()
                .replaceFirst("\\s*\\(.*\\)$", "");

        SimpleDateFormat format =
                new SimpleDateFormat("dd/MM/yyyy HH:mm");

        format.setLenient(false);

        java.util.Date parsed = format.parse(normalized);

        return new Date(parsed.getTime());
    }

    private static String mapTrangThai(String trangThai) {
        if ("Hoàn thành".equalsIgnoreCase(trangThai)) {
            return "DA_HOAN_THANH";
        }

        if ("Chờ phụ tùng".equalsIgnoreCase(trangThai)) {
            return "CHO_SUA";
        }

        return "DANG_SUA";
    }

    private static String str(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static long parseMoney(Object value) {
        String s = str(value)
                .replace(",", "")
                .replace(".", "")
                .replace(" VNĐ", "")
                .trim();

        return Long.parseLong(s);
    }

    public void luuDraft(
            String maPSC,
            String ghiChu,
            String ngayLap,
            String ngayDuKien) {

        DRAFTS.put(
                maPSC,
                new Draft(ghiChu, ngayLap, ngayDuKien));
    }

    public boolean coDraft(String maPSC) {
        return DRAFTS.containsKey(maPSC);
    }

    private static final class Draft {
        final String ghiChu;
        final String ngayLap;
        final String ngayDuKien;

        Draft(
                String ghiChu,
                String ngayLap,
                String ngayDuKien) {

            this.ghiChu = ghiChu;
            this.ngayLap = ngayLap;
            this.ngayDuKien = ngayDuKien;
        }
    }
}
