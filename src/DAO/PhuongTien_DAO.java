package DAO;

import ConnectDB.ConnectDB;
import Entity.KhoDuLieu;
import Entity.PhuongTien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * PhuongTien_DAO — Lớp DAO chuyên trách phục vụ Quản lý phương tiện (CRUD):
 * - Đọc danh sách phương tiện trực tiếp từ bảng PhuongTien trong SQL Server
 * - Thêm mới xe, cập nhật thông tin xe, xóa xe an toàn với CSDL
 * - Kiểm tra trùng biển số, trùng số khung (VIN)
 * - Kiểm tra ràng buộc tiếp nhận / sửa chữa trước khi xóa
 * - Tự động đồng bộ và có cơ chế fallback mượt mà
 */
public class PhuongTien_DAO {

    /**
     * Lấy toàn bộ danh sách phương tiện từ SQL Server
     */
    public ArrayList<PhuongTien> layTatCaPhuongTien() {
        ArrayList<PhuongTien> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return fallbackCache();

        String sql = "SELECT soVIN, bienSoXe, hangXe, dongXe, maSX, maKH, " +
                     "ISNULL(soMay, '') AS soMay, " +
                     "ISNULL(mauSon, '') AS mauSon, " +
                     "ISNULL(odo, '0') AS odo, " +
                     "ISNULL(ngayTiepNhanGanNhat, '') AS ngayTiepNhanGanNhat " +
                     "FROM PhuongTien ORDER BY bienSoXe ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String vin = rs.getString("soVIN");
                String bienSo = rs.getString("bienSoXe");
                String hangXe = rs.getString("hangXe");
                String dongXe = rs.getString("dongXe");
                int namSX = rs.getInt("maSX");
                String maKH = rs.getString("maKH");
                String soMay = rs.getString("soMay");
                String mauSon = rs.getString("mauSon");
                String odo = rs.getString("odo");
                String ngayTN = rs.getString("ngayTiepNhanGanNhat");

                list.add(new PhuongTien(bienSo, hangXe, dongXe, vin, soMay, mauSon,
                        String.valueOf(namSX), odo, maKH, ngayTN));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.layTatCaPhuongTien: " + e.getMessage());
            return fallbackCache();
        }
        return list;
    }

    /**
     * Thêm mới một phương tiện vào CSDL SQL Server
     */
    public boolean themPhuongTien(PhuongTien pt) {
        if (pt == null || pt.getBienSo() == null || pt.getBienSo().trim().isEmpty()) {
            return false;
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO PhuongTien (soVIN, bienSoXe, hangXe, dongXe, maSX, maKH, soMay, mauSon, odo, ngayTiepNhanGanNhat) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            String vin = pt.getSoKhung();
            if (vin == null || vin.trim().isEmpty()) {
                vin = "VIN_" + pt.getBienSo().replaceAll("[^A-Za-z0-9]", "");
            }

            int nam = 2022;
            try {
                if (pt.getNamSX() != null && !pt.getNamSX().trim().isEmpty()) {
                    nam = Integer.parseInt(pt.getNamSX().trim());
                    if (nam < 1900 || nam > 2100) nam = 2022;
                }
            } catch (Exception ignored) {}

            String maKH = pt.getMaChuXe();
            if (maKH == null || maKH.trim().isEmpty()) {
                maKH = "KH001";
            }

            stmt.setString(1, vin.trim());
            stmt.setString(2, pt.getBienSo().trim());
            stmt.setString(3, pt.getHangXe() != null ? pt.getHangXe().trim() : "Khác");
            stmt.setString(4, pt.getDongXe() != null ? pt.getDongXe().trim() : "Khác");
            stmt.setInt(5, nam);
            stmt.setString(6, maKH.trim());
            stmt.setString(7, pt.getSoMay() != null ? pt.getSoMay().trim() : "");
            stmt.setString(8, pt.getMauSon() != null ? pt.getMauSon().trim() : "");
            stmt.setString(9, pt.getOdo() != null ? pt.getOdo().trim() : "0");
            stmt.setString(10, pt.getNgayTiepNhanGanNhat() != null ? pt.getNgayTiepNhanGanNhat().trim() : "");

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.themPhuongTien: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin phương tiện trong CSDL SQL Server
     */
    public boolean capNhatPhuongTien(PhuongTien pt) {
        if (pt == null || pt.getBienSo() == null || pt.getBienSo().trim().isEmpty()) {
            return false;
        }

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "UPDATE PhuongTien SET hangXe = ?, dongXe = ?, maSX = ?, maKH = ?, " +
                     "soMay = ?, mauSon = ?, odo = ?, ngayTiepNhanGanNhat = ?, soVIN = ? " +
                     "WHERE bienSoXe = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int nam = 2022;
            try {
                if (pt.getNamSX() != null && !pt.getNamSX().trim().isEmpty()) {
                    nam = Integer.parseInt(pt.getNamSX().trim());
                    if (nam < 1900 || nam > 2100) nam = 2022;
                }
            } catch (Exception ignored) {}

            String vin = pt.getSoKhung();
            if (vin == null || vin.trim().isEmpty()) {
                vin = "VIN_" + pt.getBienSo().replaceAll("[^A-Za-z0-9]", "");
            }

            stmt.setString(1, pt.getHangXe() != null ? pt.getHangXe().trim() : "Khác");
            stmt.setString(2, pt.getDongXe() != null ? pt.getDongXe().trim() : "Khác");
            stmt.setInt(3, nam);
            stmt.setString(4, pt.getMaChuXe());
            stmt.setString(5, pt.getSoMay() != null ? pt.getSoMay().trim() : "");
            stmt.setString(6, pt.getMauSon() != null ? pt.getMauSon().trim() : "");
            stmt.setString(7, pt.getOdo() != null ? pt.getOdo().trim() : "0");
            stmt.setString(8, pt.getNgayTiepNhanGanNhat() != null ? pt.getNgayTiepNhanGanNhat().trim() : "");
            stmt.setString(9, vin.trim());
            stmt.setString(10, pt.getBienSo().trim());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.capNhatPhuongTien: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa phương tiện và dọn dẹp các ràng buộc phụ thuộc an toàn
     */
    public boolean xoaPhuongTien(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) return false;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String bsTrim = bienSo.trim();
        try {
            // Lấy soVIN trước
            String soVIN = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT soVIN FROM PhuongTien WHERE bienSoXe = ?")) {
                ps.setString(1, bsTrim);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) soVIN = rs.getString("soVIN");
                }
            }

            if (!soVIN.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuBaoHanh WHERE maPBH IN (SELECT maPBH FROM PhieuBaoHanh WHERE soVIN = ?)")) {
                    ps.setString(1, soVIN);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuBaoHanh WHERE soVIN = ?")) {
                    ps.setString(1, soVIN);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietSuaChua WHERE maPSC IN (SELECT maPSC FROM PhieuSuaChua WHERE soVIN = ?)")) {
                    ps.setString(1, soVIN);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuSuaChua WHERE soVIN = ?")) {
                    ps.setString(1, soVIN);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuTiepNhan WHERE soVIN = ?")) {
                    ps.setString(1, soVIN);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhuongTien WHERE bienSoXe = ?")) {
                ps.setString(1, bsTrim);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.xoaPhuongTien: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tìm kiếm phương tiện theo biển số xe
     */
    public PhuongTien timPhuongTienTheoBienSo(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) return null;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            return KhoDuLieu.get().timXeTheoBienSo(bienSo);
        }

        String sql = "SELECT soVIN, bienSoXe, hangXe, dongXe, maSX, maKH, " +
                     "ISNULL(soMay, '') AS soMay, " +
                     "ISNULL(mauSon, '') AS mauSon, " +
                     "ISNULL(odo, '0') AS odo, " +
                     "ISNULL(ngayTiepNhanGanNhat, '') AS ngayTiepNhanGanNhat " +
                     "FROM PhuongTien WHERE LOWER(bienSoXe) = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bienSo.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String vin = rs.getString("soVIN");
                    String bs = rs.getString("bienSoXe");
                    String hang = rs.getString("hangXe");
                    String dong = rs.getString("dongXe");
                    int nam = rs.getInt("maSX");
                    String maKH = rs.getString("maKH");
                    String sm = rs.getString("soMay");
                    String mau = rs.getString("mauSon");
                    String odo = rs.getString("odo");
                    String tn = rs.getString("ngayTiepNhanGanNhat");

                    return new PhuongTien(bs, hang, dong, vin, sm, mau, String.valueOf(nam), odo, maKH, tn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.timPhuongTienTheoBienSo: " + e.getMessage());
        }
        return KhoDuLieu.get().timXeTheoBienSo(bienSo);
    }

    /**
     * Tìm phương tiện theo số khung (VIN)
     */
    public PhuongTien timPhuongTienTheoVIN(String soVIN) {
        if (soVIN == null || soVIN.trim().isEmpty()) return null;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return null;

        String sql = "SELECT soVIN, bienSoXe, hangXe, dongXe, maSX, maKH, " +
                     "ISNULL(soMay, '') AS soMay, " +
                     "ISNULL(mauSon, '') AS mauSon, " +
                     "ISNULL(odo, '0') AS odo, " +
                     "ISNULL(ngayTiepNhanGanNhat, '') AS ngayTiepNhanGanNhat " +
                     "FROM PhuongTien WHERE LOWER(soVIN) = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, soVIN.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PhuongTien(
                            rs.getString("bienSoXe"),
                            rs.getString("hangXe"),
                            rs.getString("dongXe"),
                            rs.getString("soVIN"),
                            rs.getString("soMay"),
                            rs.getString("mauSon"),
                            String.valueOf(rs.getInt("maSX")),
                            rs.getString("odo"),
                            rs.getString("maKH"),
                            rs.getString("ngayTiepNhanGanNhat")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.timPhuongTienTheoVIN: " + e.getMessage());
        }
        return null;
    }

    /**
     * Kiểm tra biển số xe đã tồn tại hay chưa
     */
    public boolean kiemTraTrungBienSo(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) return false;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            return KhoDuLieu.get().timXeTheoBienSo(bienSo) != null;
        }

        String sql = "SELECT COUNT(*) FROM PhuongTien WHERE LOWER(bienSoXe) = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bienSo.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.kiemTraTrungBienSo: " + e.getMessage());
        }
        return KhoDuLieu.get().timXeTheoBienSo(bienSo) != null;
    }

    /**
     * Kiểm tra số VIN đã tồn tại ở phương tiện khác chưa
     */
    public boolean kiemTraTrungVIN(String soVIN, String bienSoHienTai) {
        if (soVIN == null || soVIN.trim().isEmpty()) return false;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "SELECT COUNT(*) FROM PhuongTien WHERE LOWER(soVIN) = ? AND LOWER(bienSoXe) <> ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, soVIN.trim().toLowerCase());
            stmt.setString(2, (bienSoHienTai != null) ? bienSoHienTai.trim().toLowerCase() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.kiemTraTrungVIN: " + e.getMessage());
        }
        return false;
    }

    /**
     * Kiểm tra xe đã có phiếu tiếp nhận hay lịch sử sửa chữa chưa
     */
    public boolean xeCoLichSuHoatDong(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) return false;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "SELECT COUNT(*) FROM PhieuTiepNhan tn " +
                     "JOIN PhuongTien pt ON tn.soVIN = pt.soVIN " +
                     "WHERE LOWER(pt.bienSoXe) = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bienSo.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.xeCoLichSuHoatDong: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy tổng số lượng phương tiện
     */
    public int layTongSoPhuongTien() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return KhoDuLieu.get().getPhuongTiens().size();

        String sql = "SELECT COUNT(*) FROM PhuongTien";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Lỗi PhuongTien_DAO.layTongSoPhuongTien: " + e.getMessage());
        }
        return KhoDuLieu.get().getPhuongTiens().size();
    }

    private ArrayList<PhuongTien> fallbackCache() {
        return new ArrayList<>(KhoDuLieu.get().getPhuongTiens());
    }
}
