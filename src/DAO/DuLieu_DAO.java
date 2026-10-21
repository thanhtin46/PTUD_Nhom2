package DAO;

import ConnectDB.ConnectDB;
import Entity.*;

import java.sql.*;
import java.util.ArrayList;

/**
 * DuLieu_DAO - Lớp Data Access Object chịu trách nhiệm đọc/ghi dữ liệu
 * trực tiếp với Hệ quản trị CSDL Microsoft SQL Server (Database: QuanLySuaChuaXe).
 */
public class DuLieu_DAO {

    // ===============================================================
    // 1. KHÁCH HÀNG
    // ===============================================================
    public ArrayList<KhachHang> layTatCaKhachHang() {
        ArrayList<KhachHang> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT maKH, hoTen, soDT, chungMinh, " +
                     "ISNULL(diaChi, '') AS diaChi, " +
                     "ISNULL(email, '') AS email, " +
                     "ISNULL(ghiChu, '') AS ghiChu, " +
                     "ISNULL(ngayDangKy, '') AS ngayDangKy " +
                     "FROM KhachHang ORDER BY maKH ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maKH");
                String ten = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String cccd = rs.getString("chungMinh");
                String diaChi = rs.getString("diaChi");
                String email = rs.getString("email");
                String ghiChu = rs.getString("ghiChu");
                String ngayDangKy = rs.getString("ngayDangKy");
                list.add(new KhachHang(ma, ten, sdt, cccd != null ? cccd : "", diaChi, email, ghiChu, ngayDangKy));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCaKhachHang: " + e.getMessage());
        }
        return list;
    }

    public boolean themKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO KhachHang (maKH, hoTen, soDT, chungMinh, diaChi, email, ghiChu, ngayDangKy) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getMaKH());
            stmt.setString(2, kh.getHoTen());
            stmt.setString(3, kh.getSdt() != null && kh.getSdt().length() >= 9 ? kh.getSdt() : "0900000000");
            String cccd = kh.getCccd();
            if (cccd != null && !cccd.trim().isEmpty()) {
                stmt.setString(4, cccd.trim());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            stmt.setString(5, kh.getDiaChi() != null ? kh.getDiaChi() : "");
            stmt.setString(6, kh.getEmail() != null ? kh.getEmail() : "");
            stmt.setString(7, kh.getGhiChu() != null ? kh.getGhiChu() : "");
            stmt.setString(8, kh.getNgayDangKy() != null ? kh.getNgayDangKy() : "");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi themKhachHang: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatKhachHang(KhachHang kh) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "UPDATE KhachHang SET hoTen = ?, soDT = ?, chungMinh = ?, diaChi = ?, email = ?, ghiChu = ?, ngayDangKy = ? WHERE maKH = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getHoTen());
            stmt.setString(2, kh.getSdt() != null && kh.getSdt().length() >= 9 ? kh.getSdt() : "0900000000");
            String cccd = kh.getCccd();
            if (cccd != null && !cccd.trim().isEmpty()) {
                stmt.setString(3, cccd.trim());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }
            stmt.setString(4, kh.getDiaChi() != null ? kh.getDiaChi() : "");
            stmt.setString(5, kh.getEmail() != null ? kh.getEmail() : "");
            stmt.setString(6, kh.getGhiChu() != null ? kh.getGhiChu() : "");
            stmt.setString(7, kh.getNgayDangKy() != null ? kh.getNgayDangKy() : "");
            stmt.setString(8, kh.getMaKH());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatKhachHang: " + e.getMessage());
            return false;
        }
    }

    public boolean xoaKhachHang(String maKH) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // Xóa cascade an toàn dữ liệu liên quan
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHD IN (SELECT maHD FROM HoaDon WHERE maKH = ?)")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM HoaDon WHERE maKH = ?")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuBaoHanh WHERE maPBH IN (SELECT maPBH FROM PhieuBaoHanh WHERE maKH = ?)")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuBaoHanh WHERE maKH = ?")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietSuaChua WHERE maPSC IN (SELECT maPSC FROM PhieuSuaChua WHERE maKH = ?)")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuSuaChua WHERE maKH = ?")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuTiepNhan WHERE maKH = ?")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhuongTien WHERE maKH = ?")) {
                ps.setString(1, maKH);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM KhachHang WHERE maKH = ?")) {
                ps.setString(1, maKH);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xoaKhachHang: " + e.getMessage());
            return false;
        }
    }

    // ===============================================================
    // 2. PHƯƠNG TIỆN
    // ===============================================================
    public ArrayList<PhuongTien> layTatCaPhuongTien() {
        ArrayList<PhuongTien> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

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
                list.add(new PhuongTien(bienSo, hangXe, dongXe, vin, soMay, mauSon, String.valueOf(namSX), odo, maKH, ngayTN));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCaPhuongTien: " + e.getMessage());
        }
        return list;
    }

    public boolean themPhuongTien(PhuongTien pt) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "INSERT INTO PhuongTien (soVIN, bienSoXe, hangXe, dongXe, maSX, maKH, soMay, mauSon, odo, ngayTiepNhanGanNhat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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

            stmt.setString(1, vin);
            stmt.setString(2, pt.getBienSo());
            stmt.setString(3, pt.getHangXe() != null ? pt.getHangXe() : "Khác");
            stmt.setString(4, pt.getDongXe() != null ? pt.getDongXe() : "Khác");
            stmt.setInt(5, nam);
            stmt.setString(6, maKH);
            stmt.setString(7, pt.getSoMay() != null ? pt.getSoMay() : "");
            stmt.setString(8, pt.getMauSon() != null ? pt.getMauSon() : "");
            stmt.setString(9, pt.getOdo() != null ? pt.getOdo() : "0");
            stmt.setString(10, pt.getNgayTiepNhanGanNhat() != null ? pt.getNgayTiepNhanGanNhat() : "");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi themPhuongTien: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatPhuongTien(PhuongTien pt) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "UPDATE PhuongTien SET hangXe = ?, dongXe = ?, maSX = ?, maKH = ?, soMay = ?, mauSon = ?, odo = ?, ngayTiepNhanGanNhat = ? WHERE bienSoXe = ? OR soVIN = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int nam = 2022;
            try {
                if (pt.getNamSX() != null && !pt.getNamSX().trim().isEmpty()) {
                    nam = Integer.parseInt(pt.getNamSX().trim());
                    if (nam < 1900 || nam > 2100) nam = 2022;
                }
            } catch (Exception ignored) {}

            stmt.setString(1, pt.getHangXe());
            stmt.setString(2, pt.getDongXe());
            stmt.setInt(3, nam);
            stmt.setString(4, pt.getMaChuXe());
            stmt.setString(5, pt.getSoMay() != null ? pt.getSoMay() : "");
            stmt.setString(6, pt.getMauSon() != null ? pt.getMauSon() : "");
            stmt.setString(7, pt.getOdo() != null ? pt.getOdo() : "0");
            stmt.setString(8, pt.getNgayTiepNhanGanNhat() != null ? pt.getNgayTiepNhanGanNhat() : "");
            stmt.setString(9, pt.getBienSo());
            stmt.setString(10, pt.getSoKhung() != null ? pt.getSoKhung() : pt.getBienSo());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatPhuongTien: " + e.getMessage());
            return false;
        }
    }

    public boolean xoaPhuongTien(String bienSo) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // Lấy soVIN trước
            String soVIN = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT soVIN FROM PhuongTien WHERE bienSoXe = ?")) {
                ps.setString(1, bienSo);
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
                ps.setString(1, bienSo);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xoaPhuongTien: " + e.getMessage());
            return false;
        }
    }

    // ===============================================================
    // 3. PHỤ TÙNG & KHO
    // ===============================================================
    public ArrayList<PhuTung> layTatCaPhuTung() {
        ArrayList<PhuTung> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT pt.maPT, pt.tenPT, pt.donViTinh, pt.donGiaBan, " +
                     "ISNULL(k.soLuongTon, 0) AS soLuongTon, ISNULL(k.soLTT, 5) AS soLTT " +
                     "FROM PhuTung pt LEFT JOIN Kho k ON pt.maPT = k.maPT ORDER BY pt.maPT ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maPT");
                String ten = rs.getString("tenPT");
                String dvt = rs.getString("donViTinh");
                long donGia = (long) rs.getDouble("donGiaBan");
                int ton = rs.getInt("soLuongTon");
                int ltt = rs.getInt("soLTT");
                list.add(new PhuTung(ma, ten, dvt, ton, donGia, ltt));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCaPhuTung: " + e.getMessage());
        }
        return list;
    }

    public boolean themPhuTung(PhuTung pt) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sqlPT = "INSERT INTO PhuTung (maPT, tenPT, donViTinh, donGiaBan) VALUES (?, ?, ?, ?)";
        String sqlKho = "INSERT INTO Kho (maKho, maPT, soLuongTon, viTriKho, soLTT, soLTD) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            try (PreparedStatement stmt = con.prepareStatement(sqlPT)) {
                stmt.setString(1, pt.getMaPT());
                stmt.setString(2, pt.getTen());
                stmt.setString(3, pt.getDonVi());
                stmt.setDouble(4, (double) pt.getDonGia());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = con.prepareStatement(sqlKho)) {
                stmt.setString(1, "KHO_" + pt.getMaPT());
                stmt.setString(2, pt.getMaPT());
                stmt.setInt(3, pt.getSoLuongTon());
                stmt.setString(4, "Kệ A");
                stmt.setInt(5, pt.getSoLuongToiThieu() > 0 ? pt.getSoLuongToiThieu() : 5);
                stmt.setInt(6, 200);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi themPhuTung: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatPhuTung(PhuTung pt) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sqlPT = "UPDATE PhuTung SET tenPT = ?, donViTinh = ?, donGiaBan = ? WHERE maPT = ?";
        String sqlKho = "UPDATE Kho SET soLuongTon = ?, soLTT = ? WHERE maPT = ?";
        try {
            try (PreparedStatement stmt = con.prepareStatement(sqlPT)) {
                stmt.setString(1, pt.getTen());
                stmt.setString(2, pt.getDonVi());
                stmt.setDouble(3, (double) pt.getDonGia());
                stmt.setString(4, pt.getMaPT());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = con.prepareStatement(sqlKho)) {
                stmt.setInt(1, pt.getSoLuongTon());
                stmt.setInt(2, pt.getSoLuongToiThieu());
                stmt.setString(3, pt.getMaPT());
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    // Chưa có dòng kho thì tạo mới
                    try (PreparedStatement ins = con.prepareStatement(
                            "INSERT INTO Kho (maKho, maPT, soLuongTon, viTriKho, soLTT, soLTD) VALUES (?, ?, ?, ?, ?, ?)")) {
                        ins.setString(1, "KHO_" + pt.getMaPT());
                        ins.setString(2, pt.getMaPT());
                        ins.setInt(3, pt.getSoLuongTon());
                        ins.setString(4, "Kệ A");
                        ins.setInt(5, pt.getSoLuongToiThieu());
                        ins.setInt(6, 200);
                        ins.executeUpdate();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatPhuTung: " + e.getMessage());
            return false;
        }
    }

    public boolean xoaPhuTung(String maPT) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM Kho WHERE maPT = ?")) {
                ps.setString(1, maPT);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhuTung WHERE maPT = ?")) {
                ps.setString(1, maPT);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xoaPhuTung: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatTonKho(String maPT, int soLuongTon) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        String sql = "UPDATE Kho SET soLuongTon = ? WHERE maPT = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, soLuongTon);
            stmt.setString(2, maPT);
            int r = stmt.executeUpdate();
            if (r == 0) {
                try (PreparedStatement ins = con.prepareStatement(
                        "INSERT INTO Kho (maKho, maPT, soLuongTon, viTriKho, soLTT, soLTD) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ins.setString(1, "KHO_" + maPT);
                    ins.setString(2, maPT);
                    ins.setInt(3, soLuongTon);
                    ins.setString(4, "Kệ A");
                    ins.setInt(5, 5);
                    ins.setInt(6, 200);
                    ins.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTonKho: " + e.getMessage());
            return false;
        }
    }

    // ===============================================================
    // 4. PHIẾU TIẾP NHẬN & SỬA CHỮA
    // ===============================================================
    public ArrayList<PhieuTiepNhan> layTatCaPhieuTiepNhan() {
        ArrayList<PhieuTiepNhan> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT tn.maPTN, tn.maKH, kh.hoTen, kh.soDT, pt.bienSoXe, pt.hangXe, pt.dongXe, " +
                     "CONVERT(VARCHAR(10), tn.ngayTN, 103) AS ngayTNStr, tn.yeuCauSC, tn.trangThaiXe, nv.hoTen AS tenNV, " +
                     "sc.maNV AS maNVPhanCong, ktv.hoTen AS tenKtvPhanCong " +
                     "FROM PhieuTiepNhan tn " +
                     "JOIN KhachHang kh ON tn.maKH = kh.maKH " +
                     "JOIN PhuongTien pt ON tn.soVIN = pt.soVIN " +
                     "LEFT JOIN NhanVien nv ON tn.maNV = nv.maNV " +
                     "LEFT JOIN PhieuSuaChua sc ON ('SC' + SUBSTRING(tn.maPTN, 3, 10)) = sc.maPSC " +
                     "LEFT JOIN NhanVien ktv ON sc.maNV = ktv.maNV " +
                     "ORDER BY tn.maPTN ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String maTN = rs.getString("maPTN");
                String maKH = rs.getString("maKH");
                String hoTen = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String bienSo = rs.getString("bienSoXe");
                String hangXe = rs.getString("hangXe");
                String loaiXe = rs.getString("dongXe");
                String ngayTN = rs.getString("ngayTNStr");
                String yeuCau = rs.getString("yeuCauSC");
                String trangThai = rs.getString("trangThaiXe");
                String nguoiLap = rs.getString("tenNV");
                String maKtv = rs.getString("maNVPhanCong");

                PhieuTiepNhan ptn = new PhieuTiepNhan(maTN, maKH, hoTen, sdt, bienSo, hangXe, loaiXe,
                        ngayTN != null ? ngayTN : "", yeuCau, trangThai, nguoiLap != null ? nguoiLap : "", 0);
                if (maKtv != null && !maKtv.isEmpty()) {
                    ptn.setKyThuatVien(maKtv);
                }
                list.add(ptn);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCaPhieuTiepNhan: " + e.getMessage());
        }
        return list;
    }

    public boolean themPhieuTiepNhan(PhieuTiepNhan ptn, String maNV) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // 1. Xác định soVIN
            String soVIN = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT soVIN FROM PhuongTien WHERE bienSoXe = ?")) {
                ps.setString(1, ptn.getBienSo());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) soVIN = rs.getString("soVIN");
                }
            }
            if (soVIN.isEmpty()) {
                soVIN = "VIN_" + ptn.getBienSo().replaceAll("[^A-Za-z0-9]", "");
                // Nếu chưa có xe, thêm xe nhanh vào DB
                themPhuongTien(new PhuongTien(ptn.getBienSo(), ptn.getHangXe(), ptn.getLoaiXe(), soVIN, "", "", "2022", "0", ptn.getMaKH(), ptn.getNgayTiepNhan()));
            }

            // 2. Xác định maKH
            String maKH = ptn.getMaKH();
            if (maKH == null || maKH.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM PhuongTien WHERE soVIN = ?")) {
                    ps.setString(1, soVIN);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) maKH = rs.getString("maKH");
                    }
                }
            }
            if (maKH == null || maKH.isEmpty()) maKH = "KH001";

            // 3. Xác định maNV
            String nvLap = (maNV != null && !maNV.isEmpty()) ? maNV : "NV01";

            // 4. Insert PhieuTiepNhan
            String sqlTN = "INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) " +
                           "VALUES (?, ?, ?, ?, SYSDATETIME(), ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlTN)) {
                stmt.setString(1, ptn.getSoPhieu());
                stmt.setString(2, nvLap);
                stmt.setString(3, maKH);
                stmt.setString(4, soVIN);
                stmt.setString(5, ptn.getYeuCau() != null ? ptn.getYeuCau() : "Yêu cầu kiểm tra & sửa chữa");
                stmt.setString(6, ptn.getTienDo() != null ? ptn.getTienDo() : "Chờ tiếp nhận");
                stmt.executeUpdate();
            }

            // 5. Insert PhieuSuaChua tương ứng
            String maPSC = "SC" + ptn.getSoPhieu().replaceAll("[^0-9]", "");
            if (maPSC.length() <= 2) maPSC = "SC00" + ptn.getSoPhieu();
            String sqlSC = "INSERT INTO PhieuSuaChua (maPSC, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) " +
                           "VALUES (?, ?, ?, ?, SYSDATETIME(), DATEADD(day, 2, CAST(GETDATE() AS DATE)), ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlSC)) {
                stmt.setString(1, maPSC);
                stmt.setString(2, nvLap);
                stmt.setString(3, maKH);
                stmt.setString(4, soVIN);
                stmt.setString(5, "CHO_SUA");
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi themPhieuTiepNhan: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatTienDoPhieuTiepNhan(String soPhieu, String tienDo, String maKtv) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            String sqlTN = "UPDATE PhieuTiepNhan SET trangThaiXe = ? WHERE maPTN = ?";
            try (PreparedStatement stmt = con.prepareStatement(sqlTN)) {
                stmt.setString(1, tienDo);
                stmt.setString(2, soPhieu);
                stmt.executeUpdate();
            }

            String maPSC = "SC" + soPhieu.replaceAll("[^0-9]", "");
            String sqlSC = "UPDATE PhieuSuaChua SET trangThai = ?" +
                           (maKtv != null && !maKtv.isEmpty() ? ", maNV = ? " : " ") +
                           "WHERE maPSC = ?";
            try (PreparedStatement stmt = con.prepareStatement(sqlSC)) {
                stmt.setString(1, tienDo);
                if (maKtv != null && !maKtv.isEmpty()) {
                    stmt.setString(2, maKtv);
                    stmt.setString(3, maPSC);
                } else {
                    stmt.setString(2, maPSC);
                }
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatTienDoPhieuTiepNhan: " + e.getMessage());
            return false;
        }
    }

    // ===============================================================
    // 5. HÓA ĐƠN & CHI TIẾT HÓA ĐƠN
    // ===============================================================
    public ArrayList<HoaDon> layTatCaHoaDon() {
        ArrayList<HoaDon> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT hd.maHD, hd.maKH, kh.hoTen, kh.soDT, " +
                     "ISNULL(pt.bienSoXe, N'Chưa rõ') AS bienSoXe, " +
                     "CONVERT(VARCHAR(10), hd.ngayLap, 103) AS ngayLapStr, " +
                     "hd.hinhThucThanhToan, nv.hoTen AS tenNV, " +
                     "ISNULL(SUM(ct.soLuong * ct.donGia), 0) AS tongTien " +
                     "FROM HoaDon hd " +
                     "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                     "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                     "LEFT JOIN PhuongTien pt ON pt.maKH = kh.maKH " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.maHD = ct.maHD " +
                     "GROUP BY hd.maHD, hd.maKH, kh.hoTen, kh.soDT, pt.bienSoXe, hd.ngayLap, hd.hinhThucThanhToan, nv.hoTen " +
                     "ORDER BY hd.maHD ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String maHD = rs.getString("maHD");
                String maKH = rs.getString("maKH");
                String tenKH = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String bienSo = rs.getString("bienSoXe");
                String ngay = rs.getString("ngayLapStr");
                String httt = rs.getString("hinhThucThanhToan");
                String tenNV = rs.getString("tenNV");
                long tong = (long) rs.getDouble("tongTien");

                String ptTT = "TIEN_MAT".equalsIgnoreCase(httt) ? "Tiền mặt" :
                              ("CHUYEN_KHOAN".equalsIgnoreCase(httt) ? "Chuyển khoản QR" : "Quẹt thẻ POS");

                list.add(new HoaDon(maHD, "SC001", maKH, tenKH, sdt, bienSo, ngay,
                        tong / 2, tong / 2, 0, tong, ptTT, tenNV != null ? tenNV : "Thu ngân", "", HoaDon.DA_THANH_TOAN));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCaHoaDon: " + e.getMessage());
        }
        return list;
    }

    public boolean themHoaDon(HoaDon hd, String maNV) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // 1. Xác định maKH
            String maKH = hd.getMaKH();
            if (maKH == null || maKH.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT maKH FROM KhachHang WHERE soDT = ?")) {
                    ps.setString(1, hd.getSdtKH());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) maKH = rs.getString("maKH");
                    }
                }
            }
            if (maKH == null || maKH.isEmpty()) maKH = "KH001";

            // 2. Xác định maNV
            String nvLap = (maNV != null && !maNV.isEmpty()) ? maNV : "NV05";

            // 3. Phương thức thanh toán chuẩn hóa cho DB
            String httt = "TIEN_MAT";
            if (hd.getPhuongThuc() != null) {
                String pt = hd.getPhuongThuc().toLowerCase();
                if (pt.contains("chuyển") || pt.contains("qr")) httt = "CHUYEN_KHOAN";
                else if (pt.contains("pos") || pt.contains("thẻ")) httt = "THE_POS";
            }

            // 4. Insert HoaDon
            String sqlHD = "INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, soPhieuSC, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) " +
                           "VALUES (?, ?, ?, CAST(GETDATE() AS DATE), ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlHD)) {
                stmt.setString(1, hd.getMaHD());
                stmt.setString(2, maKH);
                stmt.setString(3, nvLap);
                stmt.setString(4, httt);
                stmt.setString(5, hd.getSoPhieuSC() != null ? hd.getSoPhieuSC() : "SC001");
                stmt.setDouble(6, (double) hd.getTienDichVu());
                stmt.setDouble(7, (double) hd.getTienPhuTung());
                stmt.setDouble(8, hd.getGiamGiaPhanTram());
                stmt.setDouble(9, (double) hd.getTongTien());
                stmt.setString(10, hd.getGhiChu() != null ? hd.getGhiChu() : "");
                stmt.setString(11, hd.getTrangThai() != null ? hd.getTrangThai() : "DA_THANH_TOAN");
                stmt.executeUpdate();
            }

            // 5. Insert ChiTietHoaDon
            String sqlCT = "INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, soLuong, donGia) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlCT)) {
                // Chi tiết phụ tùng
                stmt.setString(1, hd.getMaHD());
                stmt.setString(2, "PT001");
                stmt.setString(3, "Chi phí phụ tùng & vật tư sửa chữa");
                stmt.setInt(4, 1);
                stmt.setDouble(5, (double) hd.getTienPhuTung());
                stmt.executeUpdate();

                // Chi tiết tiền công dịch vụ
                if (hd.getTienDichVu() > 0) {
                    stmt.setString(1, hd.getMaHD());
                    stmt.setString(2, "PT004");
                    stmt.setString(3, "Chi phí tiền công bảo dưỡng / sửa chữa");
                    stmt.setInt(4, 1);
                    stmt.setDouble(5, (double) hd.getTienDichVu());
                    stmt.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi themHoaDon: " + e.getMessage());
            return false;
        }
    }

    public boolean huyHoaDon(String maHD) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHD = ?")) {
                ps.setString(1, maHD);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM HoaDon WHERE maHD = ?")) {
                ps.setString(1, maHD);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi huyHoaDon: " + e.getMessage());
            return false;
        }
    }

    // ===============================================================
    // 6. PHIẾU BẢO HÀNH
    // ===============================================================
    public ArrayList<PhieuBaoHanh> layTatCaPhieuBaoHanh() {
        ArrayList<PhieuBaoHanh> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT bh.maPBH, pt.bienSoXe, " +
                     "CONVERT(VARCHAR(10), bh.ngayBD, 103) AS bdStr, " +
                     "CONVERT(VARCHAR(10), bh.ngayKT, 103) AS ktStr, " +
                     "bh.trangThaiBH " +
                     "FROM PhieuBaoHanh bh " +
                     "JOIN PhuongTien pt ON bh.soVIN = pt.soVIN " +
                     "ORDER BY bh.maPBH ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maPBH");
                String bienSo = rs.getString("bienSoXe");
                String bd = rs.getString("bdStr");
                String kt = rs.getString("ktStr");
                String tt = rs.getString("trangThaiBH");
                String tinhTrang = "CON_HAN".equalsIgnoreCase(tt) ? "Còn hạn" : "Hết hạn";

                list.add(new PhieuBaoHanh(ma, bienSo, bd, kt, "Gara The T4", tinhTrang, "Bảo hành tiêu chuẩn"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCaPhieuBaoHanh: " + e.getMessage());
        }
        return list;
    }

    public boolean themPhieuBaoHanh(PhieuBaoHanh pbh, String maKH, String soVIN) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            // Xác định soVIN và maKH nếu chưa có
            if (soVIN == null || soVIN.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT soVIN, maKH FROM PhuongTien WHERE bienSoXe = ?")) {
                    ps.setString(1, pbh.getBienSo());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            soVIN = rs.getString("soVIN");
                            maKH = rs.getString("maKH");
                        }
                    }
                }
            }
            if (soVIN == null || soVIN.isEmpty()) soVIN = "VIN001";
            if (maKH == null || maKH.isEmpty()) maKH = "KH001";

            String sql = "INSERT INTO PhieuBaoHanh (maPBH, ngayBD, ngayKT, trangThaiBH, maKH, soVIN, maHoaDon) " +
                         "VALUES (?, CAST(GETDATE() AS DATE), DATEADD(year, 1, CAST(GETDATE() AS DATE)), N'CON_HAN', ?, ?, NULL)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, pbh.getSoPhieu());
                stmt.setString(2, maKH);
                stmt.setString(3, soVIN);
                stmt.executeUpdate();
            }

            // Thêm chi tiết bảo hành mẫu
            String sqlCT = "INSERT INTO ChiTietPhieuBaoHanh (maPBH, maPT, lyDo, huongXuLy) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlCT)) {
                stmt.setString(1, pbh.getSoPhieu());
                stmt.setString(2, "PT001");
                stmt.setString(3, pbh.getNoiDung() != null ? pbh.getNoiDung() : "Bảo hành linh kiện xe");
                stmt.setString(4, "Kiểm tra và thay thế miễn phí");
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi themPhieuBaoHanh: " + e.getMessage());
            return false;
        }
    }

    
}
