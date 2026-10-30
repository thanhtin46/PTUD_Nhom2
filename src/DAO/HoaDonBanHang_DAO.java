
package DAO;

import ConnectDB.ConnectDB;
import DAO.PhieuBaoHanh_DAO.ChiTiet;
import Entity.KhachHang;
import Entity.PhuTung;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HoaDonBanHang_DAO {


    private Connection layKetNoi() throws SQLException {
        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null || con.isClosed()) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu!");
        }

        return con;
    }

    /*
     * 1. Lấy danh sách khách hàng.
     */
    public List<KhachHang> layDanhSachKhachHang()
            throws SQLException {

        List<KhachHang> danhSach = new ArrayList<>();

        synchronized (ConnectDB.getInstance()) {
            String sql = "SELECT maKH, hoTen, soDT, diaChi, email "
                    + "FROM KhachHang ORDER BY hoTen";

            Connection con = layKetNoi();

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    KhachHang kh = new KhachHang(
                            rs.getString("maKH"),
                            rs.getString("hoTen"),
                            rs.getString("soDT"),
                            rs.getString("email"),
                            rs.getString("diaChi")
                    );

                    danhSach.add(kh);
                }
            }
        }

        return danhSach;
    }

    /*
     * 2. Lấy danh sách phụ tùng còn tồn kho.
     */
    public List<PhuTung> layDanhSachPhuTung()
            throws SQLException {

        List<PhuTung> danhSach = new ArrayList<>();

        synchronized (ConnectDB.getInstance()) {
            String sql = "SELECT pt.maPT, pt.tenPT, pt.donViTinh, "
                    + "pt.donGiaBan, k.soLuongTon "
                    + "FROM PhuTung pt "
                    + "INNER JOIN Kho k ON pt.maPT = k.maPT "
                    + "WHERE k.soLuongTon > 0 "
                    + "ORDER BY pt.tenPT";

            Connection con = layKetNoi();

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    PhuTung pt = new PhuTung(
                            rs.getString("maPT"),
                            rs.getString("tenPT"),
                            rs.getString("donViTinh"),
                            rs.getInt("soLuongTon"),
                            rs.getLong("donGiaBan")

                    );

                    danhSach.add(pt);
                }
            }
        }

        return danhSach;
    }

    /*
     * 3. Tìm khách hàng theo mã.
     */
    public KhachHang timKhachHang(String maKH)
            throws SQLException {

        synchronized (ConnectDB.getInstance()) {
            String sql = "SELECT maKH, hoTen, soDT, diaChi, email "
                    + "FROM KhachHang WHERE maKH = ?";

            Connection con = layKetNoi();

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maKH);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new KhachHang(
                                rs.getString("maKH"),
                                rs.getString("hoTen"),
                                rs.getString("soDT"),
                                rs.getString("diaChi"),
                                rs.getString("email"), sql, sql, sql
                        );
                    }
                }
            }
        }

        return null;
    }

    /*
     * 4. Tìm phụ tùng theo mã.
     */
    public PhuTung timPhuTung(String maPT)
            throws SQLException {

        synchronized (ConnectDB.getInstance()) {
            String sql = "SELECT pt.maPT, pt.tenPT, pt.donViTinh, "
                    + "pt.donGiaBan, k.soLuongTon "
                    + "FROM PhuTung pt "
                    + "INNER JOIN Kho k ON pt.maPT = k.maPT "
                    + "WHERE pt.maPT = ?";

            Connection con = layKetNoi();

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maPT);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new PhuTung(
                                rs.getString("maPT"),
                                rs.getString("tenPT"),
                                rs.getString("donViTinh"),
                                rs.getInt("soLuongTon"),
                                rs.getLong("donGiaBan")

                        );
                    }
                }
            }
        }

        return null;
    }

    /*
     * 5. Kiểm tra mã hóa đơn đã tồn tại hay chưa.
     */
    public boolean kiemTraMaHD(String maHD)
            throws SQLException {

        synchronized (ConnectDB.getInstance()) {
            String sql = "SELECT COUNT(*) FROM HoaDon WHERE maHD = ?";

            Connection con = layKetNoi();

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHD);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        }

        return false;
    }

    /*
     * 6. Lập hóa đơn bán hàng.
     *
     * giamGia: phần trăm giảm giá, ví dụ 10 nghĩa là giảm 10%.
     * soPhieuSC: hóa đơn bán hàng thông thường không liên kết sửa chữa
     * nên lưu NULL.
     *
     * Trả về true nếu lưu thành công.
     */
    public boolean lapHoaDonBanHang(
            String maHD,
            String maKH,
            String maNV,
            String hinhThucThanhToan,
            double giamGia,              // phần trăm: 10 = giảm 10%
            String ghiChu,
            Map<String, Integer> danhSachChiTiet)  throws SQLException {

        if (maHD == null || maHD.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống!");
        }
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn khách hàng!");
        }
        if (maNV == null || maNV.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }
        if (hinhThucThanhToan == null || hinhThucThanhToan.trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn hình thức thanh toán!");
        }
        if (giamGia < 0 || giamGia > 100) {
            throw new IllegalArgumentException(
                    "Giảm giá phải nằm trong khoảng 0 đến 100%!");
        }
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            throw new IllegalArgumentException(
                    "Hóa đơn phải có ít nhất một phụ tùng!");
        }

        // Gộp các dòng trùng mã phụ tùng (khóa chính maHD + maPT)
        Map<String, Integer> chiTietGop = new LinkedHashMap<>();


        for (Map.Entry<String, Integer> e : danhSachChiTiet.entrySet()) {
            if (e.getKey() == null || e.getKey().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã phụ tùng không hợp lệ!");
            }
            if (e.getValue() == null || e.getValue() <= 0) {
                throw new IllegalArgumentException("Số lượng bán phải lớn hơn 0!");
            }
            chiTietGop.merge(e.getKey().trim(), e.getValue(), Math::addExact);
        }

        synchronized (ConnectDB.getInstance()) {

            Connection con = layKetNoi();
            boolean autoCommitBanDau = con.getAutoCommit();

            try {
                con.setAutoCommit(false);

                double tienPhuTung = 0;

                // Lưu tên và đơn giá để insert chi tiết
                Map<String, String> tenMap = new LinkedHashMap<>();
                Map<String, Double> donGiaMap = new LinkedHashMap<>();

                String sqlKiemTraKho =
                        "SELECT pt.maPT, pt.tenPT, pt.donGiaBan, k.soLuongTon "
                                + "FROM PhuTung pt "
                                + "INNER JOIN Kho k WITH (UPDLOCK, HOLDLOCK) "
                                + "ON pt.maPT = k.maPT "
                                + "WHERE pt.maPT = ?";

                try (PreparedStatement ps = con.prepareStatement(sqlKiemTraKho)) {

                    for (Map.Entry<String, Integer> entry : chiTietGop.entrySet()) {

                        String maPT = entry.getKey();
                        int soLuongBan = entry.getValue();

                        ps.setString(1, maPT);

                        try (ResultSet rs = ps.executeQuery()) {

                            if (!rs.next()) {
                                throw new SQLException(
                                        "Không tìm thấy phụ tùng hoặc "
                                                + "chưa được khai báo trong kho: " + maPT);
                            }

                            String tenPT = rs.getString("tenPT");
                            double donGia = rs.getDouble("donGiaBan");
                            int tonKho = rs.getInt("soLuongTon");

                            if (donGia < 0) {
                                throw new SQLException(
                                        "Đơn giá phụ tùng không hợp lệ: " + maPT);
                            }
                            if (tonKho < soLuongBan) {
                                throw new SQLException(
                                        "Phụ tùng " + tenPT
                                                + " không đủ số lượng. Tồn kho: "
                                                + tonKho + ", yêu cầu: " + soLuongBan);
                            }

                            tenMap.put(maPT, tenPT);
                            donGiaMap.put(maPT, donGia);

                            tienPhuTung += donGia * soLuongBan;
                        }
                    }
                }

                // Tổng tiền sau giảm giá, làm tròn 2 chữ số
                double tongTien = Math.round(
                        tienPhuTung * (1 - giamGia / 100.0) * 100.0) / 100.0;

                String sqlHoaDon =
                        "INSERT INTO HoaDon "
                                + "(maHD, maKH, maNV, ngayLap, "
                                + "hinhThucThanhToan, soPhieuSC, "
                                + "tienDichVu, tienPhuTung, giamGia, "
                                + "tongTien, ghiChu, trangThai) "
                                + "VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(sqlHoaDon)) {
                    ps.setString(1, maHD.trim());
                    ps.setString(2, maKH.trim());
                    ps.setString(3, maNV.trim());
                    ps.setDate(4, Date.valueOf(LocalDate.now()));
                    ps.setNString(5, hinhThucThanhToan.trim());
                    ps.setDouble(6, 0);              // tienDichVu
                    ps.setDouble(7, tienPhuTung);
                    ps.setDouble(8, giamGia);
                    ps.setDouble(9, tongTien);
                    ps.setString(10, ghiChu);
                    ps.setNString(11, "DA_THANH_TOAN");
                    ps.executeUpdate();
                }

                String sqlChiTiet =
                        "INSERT INTO ChiTietHoaDon "
                                + "(maHD, maPT, tenPhuTung, soLuong, donGia) "
                                + "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                    for (Map.Entry<String, Integer> entry : chiTietGop.entrySet()) {
                        String maPT = entry.getKey();

                        ps.setString(1, maHD.trim());
                        ps.setString(2, maPT);
                        ps.setNString(3, tenMap.get(maPT));
                        ps.setInt(4, entry.getValue());
                        ps.setDouble(5, donGiaMap.get(maPT));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                String sqlCapNhatKho =
                        "UPDATE Kho "
                                + "SET soLuongTon = soLuongTon - ? "
                                + "WHERE maPT = ? AND soLuongTon >= ?";

                try (PreparedStatement ps = con.prepareStatement(sqlCapNhatKho)) {
                    for (Map.Entry<String, Integer> entry : chiTietGop.entrySet()) {
                        int soLuong = entry.getValue();

                        ps.setInt(1, soLuong);
                        ps.setString(2, entry.getKey());
                        ps.setInt(3, soLuong);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Không thể cập nhật tồn kho phụ tùng: "
                                            + entry.getKey());
                        }
                    }
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                try {
                    con.rollback();
                } catch (SQLException exRollback) {
                    e.addSuppressed(exRollback);
                }
                throw e;

            } finally {
                try {
                    if (!con.isClosed()) {
                        con.setAutoCommit(autoCommitBanDau);
                    }
                } catch (SQLException ex) {
                    System.err.println("Lỗi khôi phục AutoCommit: " + ex.getMessage());
                }
            }
        }
    }
}
