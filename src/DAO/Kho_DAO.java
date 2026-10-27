package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietKho;
import Entity.PhieuKho;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Kho_DAO - Data Access Object phục vụ Use Case UC-KHO-03:
 * Truy vấn lịch sử nhập xuất kho và chi tiết phụ tùng từ SQL Server.
 */
public class Kho_DAO {

    private static final DateTimeFormatter FMT_DDMMYYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Lấy toàn bộ lịch sử phiếu kho (Cả phiếu nhập và phiếu xuất) từ CSDL SQL Server.
     * @throws SQLException nếu có lỗi kết nối hoặc truy vấn CSDL.
     */
    public ArrayList<PhieuKho> layTatCaLichSuKho() throws SQLException {
        ArrayList<PhieuKho> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Không thể kết nối đến cơ sở dữ liệu SQL Server!");
        }

        // 1. Truy vấn lịch sử Phiếu nhập kho
        String sqlPN = "SELECT pn.maPhieuNhap, pn.ngayNhap, pn.tongTien, pn.trangThai, pn.ngayDuKienGiao, pn.ghiChu, " +
                "ISNULL(nv.hoTen, pn.maNV) AS nguoiLap, " +
                "ISNULL(ncc.tenNCC, pn.maNCC) AS nhaCungCap " +
                "FROM PhieuNhapKho pn " +
                "LEFT JOIN NhanVien nv ON pn.maNV = nv.maNV " +
                "LEFT JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC " +
                "ORDER BY pn.ngayNhap DESC, pn.maPhieuNhap DESC";

        try (PreparedStatement stmt = con.prepareStatement(sqlPN);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String soPhieu = rs.getString("maPhieuNhap");
                Date ngayDate = rs.getDate("ngayNhap");
                String ngayStr = ngayDate != null ? ngayDate.toLocalDate().format(FMT_DDMMYYYY) : "";
                String nguoiLap = rs.getString("nguoiLap");
                String ncc = rs.getString("nhaCungCap");

                PhieuKho pk = new PhieuKho(soPhieu, PhieuKho.NHAP, ngayStr, nguoiLap,
                        rs.getString("ghiChu"), ncc, rs.getDate("ngayDuKienGiao") == null ? "" : rs.getDate("ngayDuKienGiao").toLocalDate().format(FMT_DDMMYYYY), rs.getString("trangThai"));

                // Nạp chi tiết phiếu nhập
                pk.getChiTiet().addAll(layChiTietPhieuNhap(con, soPhieu));
                ds.add(pk);
            }
        } catch (SQLException ex) {
            // Dự phòng nếu DB cũ chưa có các cột mới trangThai, ngayDuKienGiao, ghiChu
            String sqlPNCu = "SELECT pn.maPhieuNhap, pn.ngayNhap, pn.tongTien, " +
                    "ISNULL(nv.hoTen, pn.maNV) AS nguoiLap, " +
                    "ISNULL(ncc.tenNCC, pn.maNCC) AS nhaCungCap " +
                    "FROM PhieuNhapKho pn " +
                    "LEFT JOIN NhanVien nv ON pn.maNV = nv.maNV " +
                    "LEFT JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC " +
                    "ORDER BY pn.ngayNhap DESC, pn.maPhieuNhap DESC";
            try (PreparedStatement stmtCu = con.prepareStatement(sqlPNCu);
                 ResultSet rsCu = stmtCu.executeQuery()) {
                while (rsCu.next()) {
                    String soPhieu = rsCu.getString("maPhieuNhap");
                    Date ngayDate = rsCu.getDate("ngayNhap");
                    String ngayStr = ngayDate != null ? ngayDate.toLocalDate().format(FMT_DDMMYYYY) : "";
                    String nguoiLap = rsCu.getString("nguoiLap");
                    String ncc = rsCu.getString("nhaCungCap");
                    PhieuKho pk = new PhieuKho(soPhieu, PhieuKho.NHAP, ngayStr, nguoiLap, "", ncc, "", "DA_NHAP");
                    pk.getChiTiet().addAll(layChiTietPhieuNhap(con, soPhieu));
                    ds.add(pk);
                }
            } catch (SQLException ex2) {
                System.err.println("Lỗi nạp lịch sử phiếu nhập kho: " + ex2.getMessage());
            }
        }

        // 2. Truy vấn lịch sử Phiếu xuất kho
        String sqlPX = "SELECT px.maPhieuXuat, px.maPSC, px.ngayXuat, " +
                "ISNULL(px.trangThai, N'Đã xuất') AS trangThai, " +
                "ISNULL(px.lyDoXuat, N'Xuất sửa chữa') AS lyDoXuat, " +
                "ISNULL(nv.hoTen, px.maNV) AS nguoiLap " +
                "FROM PhieuXuatKho px " +
                "LEFT JOIN NhanVien nv ON px.maNV = nv.maNV " +
                "ORDER BY px.ngayXuat DESC, px.maPhieuXuat DESC";

        try (PreparedStatement stmt = con.prepareStatement(sqlPX);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String soPhieu = rs.getString("maPhieuXuat");
                Timestamp ts = rs.getTimestamp("ngayXuat");
                String ngayStr = ts != null ? ts.toLocalDateTime().toLocalDate().format(FMT_DDMMYYYY) : "";
                String nguoiLap = rs.getString("nguoiLap");
                String tt = rs.getString("trangThai");
                String lyDo = rs.getString("lyDoXuat");

                PhieuKho pk = new PhieuKho(soPhieu, PhieuKho.XUAT, ngayStr, nguoiLap, lyDo);
                pk.setTrangThai(tt);

                // Nạp chi tiết phiếu xuất
                pk.getChiTiet().addAll(layChiTietPhieuXuat(con, soPhieu));
                ds.add(pk);
            }
        }

        return ds;
    }

    /**
     * Lấy danh sách chi tiết phụ tùng của một phiếu nhập kho.
     */
    public ArrayList<ChiTietKho> layChiTietPhieuNhap(Connection con, String maPhieuNhap) {
        ArrayList<ChiTietKho> list = new ArrayList<>();
        String sql = "SELECT ct.maPT, ct.tenPT, ct.soLuong, ct.donGia " +
                "FROM ChiTietPhieuNhapKho ct WHERE ct.maPhieuNhap = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maPhieuNhap);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPT = rs.getString("maPT");
                    String tenPT = rs.getString("tenPT");
                    int sl = rs.getInt("soLuong");
                    long gia = (long) rs.getDouble("donGia");
                    list.add(new ChiTietKho(maPT, tenPT, sl, gia));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiTietPhieuNhap [" + maPhieuNhap + "]: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách chi tiết phụ tùng của một phiếu xuất kho.
     */
    public ArrayList<ChiTietKho> layChiTietPhieuXuat(Connection con, String maPhieuXuat) {
        ArrayList<ChiTietKho> list = new ArrayList<>();
        String sql = "SELECT ct.maPT, ISNULL(pt.tenPT, ct.maPT) AS tenPT, " +
                "ISNULL(ct.soLuongXuat, ct.soLuongYeuCau) AS sl, ct.donGia " +
                "FROM ChiTietPhieuXuatKho ct " +
                "LEFT JOIN PhuTung pt ON ct.maPT = pt.maPT " +
                "WHERE ct.maPhieuXuat = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maPhieuXuat);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPT = rs.getString("maPT");
                    String tenPT = rs.getString("tenPT");
                    int sl = rs.getInt("sl");
                    long gia = (long) rs.getDouble("donGia");
                    list.add(new ChiTietKho(maPT, tenPT, sl, gia));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiTietPhieuXuat [" + maPhieuXuat + "]: " + e.getMessage());
        }
        return list;
    }
}
