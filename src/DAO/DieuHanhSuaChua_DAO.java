package DAO;

import ConnectDB.ConnectDB;
import Entity.PhieuDieuHanhDTO;
import Entity.ThongKeKtvDTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Lớp truy vấn dữ liệu theo dõi tiến độ sửa chữa và khối lượng công việc của kỹ thuật viên.
 */
public class DieuHanhSuaChua_DAO {

    private Connection getValidConnection() throws SQLException {
        ConnectDB cdb = ConnectDB.getInstance();
        Connection con = cdb.getConnection();
        if (con == null || con.isClosed()) {
            cdb.connect();
            con = cdb.getConnection();
        }
        if (con == null || con.isClosed()) {
            throw new SQLException("Không thể kết nối đến cơ sở dữ liệu SQL Server!");
        }
        return con;
    }

    // Lấy danh sách phiếu sửa chữa kèm thông tin khách hàng và hạn hoàn thành
    public ArrayList<PhieuDieuHanhDTO> layDanhSachTheoDoi() throws SQLException {
        ArrayList<PhieuDieuHanhDTO> list = new ArrayList<>();

        String sql = "SELECT sc.maPSC, sc.maPTN, sc.maNV, "
                + "       ISNULL(nv.hoTen, N'(Chưa phân công)') AS tenKTV, "
                + "       sc.maKH, "
                + "       ISNULL(kh.hoTen, N'Khách vãng lai') AS tenKH, "
                + "       ISNULL(kh.soDT, N'—') AS soDT, "
                + "       ISNULL(pt.bienSoXe, N'—') AS bienSoXe, "
                + "       sc.ngayBD, "
                + "       sc.ngayHoanThanhDuKien, "
                + "       ISNULL(sc.trangThai, 'CHO_SUA') AS trangThai "
                + "FROM PhieuSuaChua sc "
                + "LEFT JOIN NhanVien nv ON sc.maNV = nv.maNV "
                + "LEFT JOIN KhachHang kh ON sc.maKH = kh.maKH "
                + "LEFT JOIN PhuongTien pt ON sc.soVIN = pt.soVIN "
                + "ORDER BY "
                + "    CASE "
                + "        WHEN sc.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY') "
                + "             AND sc.trangThai NOT LIKE N'%hoàn thành%' AND sc.trangThai NOT LIKE N'%hủy%' "
                + "             AND sc.ngayHoanThanhDuKien IS NOT NULL "
                + "             AND sc.ngayHoanThanhDuKien < CAST(GETDATE() AS DATE) THEN 1 "
                + "        WHEN sc.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY') "
                + "             AND sc.trangThai NOT LIKE N'%hoàn thành%' AND sc.trangThai NOT LIKE N'%hủy%' "
                + "             AND sc.ngayHoanThanhDuKien IS NOT NULL "
                + "             AND sc.ngayHoanThanhDuKien = CAST(GETDATE() AS DATE) THEN 2 "
                + "        WHEN sc.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY') "
                + "             AND sc.trangThai NOT LIKE N'%hoàn thành%' AND sc.trangThai NOT LIKE N'%hủy%' THEN 3 "
                + "        ELSE 4 "
                + "    END ASC, "
                + "    sc.ngayHoanThanhDuKien ASC, "
                + "    sc.maPSC DESC";

        Connection con = getValidConnection();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maPSC = rs.getString("maPSC");
                String maPTN = rs.getString("maPTN");
                String maNV = rs.getString("maNV");
                String tenKTV = rs.getString("tenKTV");
                String tenKH = rs.getString("tenKH");
                String soDT = rs.getString("soDT");
                String bienSoXe = rs.getString("bienSoXe");
                String trangThai = rs.getString("trangThai");

                Timestamp tsBD = rs.getTimestamp("ngayBD");
                LocalDate ngayBD = (tsBD != null) ? tsBD.toLocalDateTime().toLocalDate() : null;

                Date dHen = rs.getDate("ngayHoanThanhDuKien");
                LocalDate ngayHen = (dHen != null) ? dHen.toLocalDate() : null;

                PhieuDieuHanhDTO dto = new PhieuDieuHanhDTO(
                        maPSC, maPTN, bienSoXe, tenKH, soDT, maNV, tenKTV, ngayBD, ngayHen, trangThai
                );
                list.add(dto);
            }
        }

        return list;
    }

    // Thống kê số lượng phiếu đang làm và quá hẹn theo từng kỹ thuật viên
    public ArrayList<ThongKeKtvDTO> layThongKeKyThuatVien() throws SQLException {
        ArrayList<ThongKeKtvDTO> list = new ArrayList<>();

        String sql = "SELECT nv.maNV, nv.hoTen, ISNULL(nv.chucVu, N'Kỹ thuật viên') AS chucVu, "
                + "       COUNT(DISTINCT CASE "
                + "           WHEN sc.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY') "
                + "                AND sc.trangThai NOT LIKE N'%hoàn thành%' AND sc.trangThai NOT LIKE N'%hủy%' "
                + "           THEN sc.maPSC "
                + "       END) AS soPhieuDangLam, "
                + "       COUNT(DISTINCT CASE "
                + "           WHEN sc.trangThai NOT IN ('HOAN_THANH', 'DA_HOAN_THANH', 'DA_HUY', 'HUY') "
                + "                AND sc.trangThai NOT LIKE N'%hoàn thành%' AND sc.trangThai NOT LIKE N'%hủy%' "
                + "                AND sc.ngayHoanThanhDuKien IS NOT NULL "
                + "                AND sc.ngayHoanThanhDuKien < CAST(GETDATE() AS DATE) "
                + "           THEN sc.maPSC "
                + "       END) AS soPhieuQuaHen "
                + "FROM NhanVien nv "
                + "LEFT JOIN PhieuSuaChua sc ON sc.maNV = nv.maNV "
                + "WHERE (nv.trangThai = N'Đang làm việc' OR nv.trangThai IS NULL) "
                + "  AND (LOWER(nv.chucVu) LIKE N'%sửa chữa%' OR LOWER(nv.chucVu) LIKE '%ktv%' "
                + "       OR LOWER(nv.chucVu) LIKE N'%kỹ thuật%' OR LOWER(nv.chucVu) LIKE N'%máy%' "
                + "       OR LOWER(nv.chucVu) LIKE N'%gầm%' OR LOWER(nv.chucVu) LIKE N'%điện%' "
                + "       OR LOWER(nv.chucVu) LIKE N'%sơn%' OR LOWER(nv.chucVu) LIKE N'%bảo dưỡng%' "
                + "       OR nv.maNV IN (SELECT DISTINCT maNV FROM PhieuSuaChua WHERE maNV IS NOT NULL)) "
                + "GROUP BY nv.maNV, nv.hoTen, nv.chucVu "
                + "ORDER BY soPhieuDangLam DESC, soPhieuQuaHen DESC, nv.maNV ASC";

        Connection con = getValidConnection();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maNV = rs.getString("maNV");
                String hoTen = rs.getString("hoTen");
                String chucVu = rs.getString("chucVu");
                int soDangLam = rs.getInt("soPhieuDangLam");
                int soQuaHen = rs.getInt("soPhieuQuaHen");

                list.add(new ThongKeKtvDTO(maNV, hoTen, chucVu, soDangLam, soQuaHen));
            }
        }

        return list;
    }
}
