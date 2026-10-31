package DAO;

import ConnectDB.ConnectDB;
import Entity.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Lớp DAO truy vấn dữ liệu từ CSDL SQL Server.
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
                Date dDangKy = rs.getDate("ngayDangKy");
                LocalDate ngayDangKy = dDangKy != null ? dDangKy.toLocalDate() : null;
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
            stmt.setDate(8, kh.getNgayDangKy() != null ? Date.valueOf(kh.getNgayDangKy()) : null);
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
            stmt.setDate(7, kh.getNgayDangKy() != null ? Date.valueOf(kh.getNgayDangKy()) : null);
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

        String sql = "SELECT pt.soVIN, pt.bienSoXe, pt.hangXe, pt.dongXe, pt.maSX, pt.maKH, " +
                     "kh.hoTen AS tenKH, kh.soDT AS sdtKH, " +
                     "ISNULL(pt.soMay, '') AS soMay, " +
                     "ISNULL(pt.mauSon, '') AS mauSon, " +
                     "ISNULL(pt.odo, '0') AS odo, " +
                     "pt.ngayTiepNhanGanNhat " +
                     "FROM PhuongTien pt " +
                     "LEFT JOIN KhachHang kh ON pt.maKH = kh.maKH " +
                     "ORDER BY pt.bienSoXe ASC";
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
                Date dTN = rs.getDate("ngayTiepNhanGanNhat");
                LocalDate ngayTN = dTN != null ? dTN.toLocalDate() : null;
                String tenKH = rs.getString("tenKH");
                String sdtKH = rs.getString("sdtKH");
                KhachHang kh = new KhachHang(maKH, tenKH != null ? tenKH : "", sdtKH != null ? sdtKH : "", "", "");
                list.add(new PhuongTien(bienSo, hangXe, dongXe, vin, soMay, mauSon, String.valueOf(namSX), odo, kh, ngayTN));
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
            stmt.setDate(10, pt.getNgayTiepNhanGanNhat() != null ? Date.valueOf(pt.getNgayTiepNhanGanNhat()) : null);
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
            stmt.setDate(8, pt.getNgayTiepNhanGanNhat() != null ? Date.valueOf(pt.getNgayTiepNhanGanNhat()) : null);
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
                     "tn.ngayTN, tn.yeuCauSC, tn.trangThaiXe, nv.hoTen AS tenNV, " +
                     "sc.maNV AS maNVPhanCong, ktv.hoTen AS tenKtvPhanCong " +
                     "FROM PhieuTiepNhan tn " +
                     "JOIN KhachHang kh ON tn.maKH = kh.maKH " +
                     "JOIN PhuongTien pt ON tn.soVIN = pt.soVIN " +
                     "LEFT JOIN NhanVien nv ON tn.maNV = nv.maNV " +
                     "LEFT JOIN PhieuSuaChua sc ON (sc.maPTN = tn.maPTN OR (sc.maPTN IS NULL AND ('SC' + SUBSTRING(tn.maPTN, 3, 10)) = sc.maPSC)) " +
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
                Timestamp tsTN = rs.getTimestamp("ngayTN");
                LocalDateTime ngayTN = tsTN != null ? tsTN.toLocalDateTime() : null;
                String yeuCau = rs.getString("yeuCauSC");
                String trangThai = rs.getString("trangThaiXe");
                String nguoiLap = rs.getString("tenNV");
                String maKtv = rs.getString("maNVPhanCong");

                KhachHang kh = new KhachHang(maKH, hoTen, sdt, "", "");
                PhuongTien pt = new PhuongTien(bienSo, hangXe, loaiXe, "VIN_" + (bienSo != null ? bienSo.replaceAll("[^A-Za-z0-9]", "") : ""), "", "", "2022", "0", kh, ngayTN != null ? ngayTN.toLocalDate() : null);
                NhanVien nv = new NhanVien();
                nv.setHoTen(nguoiLap != null ? nguoiLap : "");

                PhieuTiepNhan ptn = new PhieuTiepNhan(maTN, nv, kh, pt, ngayTN, yeuCau, trangThai, 0, maKtv != null ? maKtv : "");
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
                themPhuongTien(new PhuongTien(ptn.getBienSo(), ptn.getHangXe(), ptn.getLoaiXe(), soVIN, "", "", "2022", "0", ptn.getMaKH(), ptn.getNgayTN() != null ? ptn.getNgayTN().toLocalDate() : LocalDate.now()));
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
                     "CASE WHEN hd.tienDichVu = 0 THEN N'Bán lẻ (Shop)' ELSE ISNULL(pt.bienSoXe, N'—') END AS bienSoXe, " +
                     "hd.ngayLap, " +
                     "hd.hinhThucThanhToan, nv.hoTen AS tenNV, " +
                     "hd.tienDichVu, hd.tienPhuTung, hd.giamGia, hd.tongTien, " +
                     "ISNULL(hd.ghiChu, '') AS ghiChu, " +
                     "ISNULL(hd.trangThai, N'Đã thanh toán') AS trangThai " +
                     "FROM HoaDon hd " +
                     "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                     "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                     "LEFT JOIN (SELECT maKH, MIN(bienSoXe) AS bienSoXe FROM PhuongTien GROUP BY maKH) pt ON pt.maKH = hd.maKH " +
                     "ORDER BY hd.maHD ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            java.util.HashSet<String> daXem = new java.util.HashSet<>();
            while (rs.next()) {
                String maHD = rs.getString("maHD");
                if (daXem.contains(maHD)) continue;
                daXem.add(maHD);

                String maKH = rs.getString("maKH");
                String tenKH = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String bienSo = rs.getString("bienSoXe");
                Date dLap = rs.getDate("ngayLap");
                LocalDate ngay = dLap != null ? dLap.toLocalDate() : null;
                String httt = rs.getString("hinhThucThanhToan");
                String tenNV = rs.getString("tenNV");
                long tienDV = (long) rs.getDouble("tienDichVu");
                long tienPT = (long) rs.getDouble("tienPhuTung");
                double giamGia = rs.getDouble("giamGia");
                long tong = (long) rs.getDouble("tongTien");
                String ghiChu = rs.getString("ghiChu");
                String tt = rs.getString("trangThai");
                String soPhieuSC = tienDV == 0 ? "Bán lẻ" : (maKH != null ? maKH : "TN001");

                String ptTT = "TIEN_MAT".equalsIgnoreCase(httt) ? "Tiền mặt" :
                              ("CHUYEN_KHOAN".equalsIgnoreCase(httt) ? "Chuyển khoản QR" : "Quẹt thẻ POS");

                KhachHang kh = new KhachHang(maKH, tenKH, sdt, "", "");
                PhuongTien pt = (bienSo != null && !bienSo.isEmpty() && !bienSo.equals("—"))
                        ? new PhuongTien(bienSo, "", "", "VIN_" + bienSo.replaceAll("[^A-Za-z0-9]", ""), "", "", "2022", "0", kh, ngay)
                        : null;
                PhieuSuaChua psc = new PhieuSuaChua(soPhieuSC != null ? soPhieuSC : "TN001");
                psc.setKhachHang(kh);
                psc.setPhuongTien(pt);
                NhanVien nv = new NhanVien();
                nv.setHoTen(tenNV != null ? tenNV : "Thu ngân");

                HoaDon hd = new HoaDon(maHD, psc, kh, nv, ngay, tienDV, tienPT, giamGia, tong, ptTT, ghiChu, tt);
                list.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCaHoaDon: " + e.getMessage());
        }
        return list;
    }

    public boolean themHoaDon(HoaDon hd, String maNV) {
        return themHoaDon(hd, maNV, null);
    }

    public boolean themHoaDon(HoaDon hd, String maNV, java.util.List<ChiTietDichVu> dsChiTiet) {
        return new ThanhToan_DAO().luu(hd, maNV, dsChiTiet);
    }

    public ArrayList<ChiTietDichVu> layChiTietTheoPhieu(String soPhieu) {
        ArrayList<ChiTietDichVu> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || soPhieu == null) return list;

        String maPSC;
        try { maPSC = LienKetPhieu.maSuaChua(con, soPhieu); } catch (SQLException ex) { throw new IllegalStateException(ex); }
        String sql = "SELECT ct.maPT, ct.noiDungSC, ct.tenPhuTung, ct.soLuong, ct.donGia, ISNULL(pt.donViTinh, N'Cái') AS donViTinh " +
                     "FROM ChiTietSuaChua ct " +
                     "LEFT JOIN PhuTung pt ON ct.maPT = pt.maPT " +
                     "WHERE ct.maPSC = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maPSC);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPT = rs.getString("maPT");
                    String noiDung = rs.getString("noiDungSC");
                    String tenPT = rs.getString("tenPhuTung");
                    int sl = rs.getInt("soLuong");
                    long donGia = (long) rs.getDouble("donGia");
                    String dvt = rs.getString("donViTinh");

                    boolean laCong = (noiDung != null && noiDung.toLowerCase().contains("công")) ||
                                     (tenPT != null && tenPT.toLowerCase().contains("công"));
                    String loai = laCong ? "Tiền công" : "Phụ tùng";
                    String ten = (tenPT != null && !tenPT.isEmpty()) ? tenPT : noiDung;
                    list.add(new ChiTietDichVu(loai, maPT, ten, dvt, sl, donGia));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiTietTheoPhieu: " + e.getMessage());
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT noiDungCongViec, tienCong FROM ChiTietCongViecSuaChua WHERE maPSC = ? ORDER BY stt")) {
            ps.setString(1, maPSC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new ChiTietDichVu("Tiền công", "", rs.getString(1), "Công", 1, rs.getLong(2)));
            }
        } catch (SQLException ex) { throw new IllegalStateException("Không đọc được tiền công sửa chữa", ex); }
        return list;
    }

    public ArrayList<ChiTietDichVu> layChiTietTheoHoaDon(String maHD) {
        ArrayList<ChiTietDichVu> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || maHD == null) return list;

        String sql = "SELECT ct.maPT, ct.tenPhuTung, ct.soLuong, ct.donGia, ISNULL(ct.donViTinh, ISNULL(pt.donViTinh, N'Cái')) AS donViTinh " +
                     "FROM ChiTietHoaDon ct " +
                     "LEFT JOIN PhuTung pt ON ct.maPT = pt.maPT " +
                     "WHERE ct.maHD = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPT = rs.getString("maPT");
                    String tenPT = rs.getString("tenPhuTung");
                    int sl = rs.getInt("soLuong");
                    long donGia = (long) rs.getDouble("donGia");
                    String dvt = rs.getString("donViTinh");

                    boolean laCong = tenPT != null && tenPT.toLowerCase().contains("công");
                    String loai = laCong ? "Tiền công" : "Phụ tùng";
                    list.add(new ChiTietDichVu(loai, maPT, tenPT, dvt, sl, donGia));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layChiTietTheoHoaDon: " + e.getMessage());
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT tenDichVu, soLuong, donGia FROM ChiTietDichVuHoaDon WHERE maHD = ? ORDER BY stt")) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new ChiTietDichVu("Tiền công", "", rs.getString(1), "Công", rs.getInt(2), rs.getLong(3)));
            }
        } catch (SQLException ex) { throw new IllegalStateException("Không đọc được chi tiết dịch vụ hóa đơn", ex); }
        return list;
    }

    public boolean luuChiTietSuaChua(String maPSC, java.util.List<ChiTietDichVu> list) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || maPSC == null || list == null) return false;

        try {
            try (PreparedStatement del = con.prepareStatement("DELETE FROM ChiTietSuaChua WHERE maPSC = ?")) {
                del.setString(1, maPSC);
                del.executeUpdate();
            }

            String sql = "INSERT INTO ChiTietSuaChua (maPSC, maPT, noiDungSC, tenPhuTung, soLuong, donGia, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ins = con.prepareStatement(sql)) {
                java.util.HashSet<String> daCoPT = new java.util.HashSet<>();
                int ptCount = 1;
                for (ChiTietDichVu ct : list) {
                    String maPT = ct.getMaPT();
                    if (maPT == null || maPT.trim().isEmpty() || daCoPT.contains(maPT)) {
                        maPT = String.format("PT%03d", Math.min(20, ptCount++));
                    }
                    daCoPT.add(maPT);

                    ins.setString(1, maPSC);
                    ins.setString(2, maPT);
                    ins.setString(3, ct.getTenHangMuc());
                    ins.setString(4, ct.getTenHangMuc());
                    ins.setInt(5, ct.getSoLuong());
                    ins.setDouble(6, (double) ct.getDonGia());
                    ins.setString(7, "Hoàn thành");
                    ins.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi luuChiTietSuaChua: " + e.getMessage());
            return false;
        }
    }

    public boolean huyHoaDon(String maHD) {
        return new ThanhToan_DAO().huy(maHD);
    }

    // ===============================================================
    // 6. PHIẾU BẢO HÀNH
    // ===============================================================
    public ArrayList<PhieuBaoHanh> layTatCaPhieuBaoHanh() {
        ArrayList<PhieuBaoHanh> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT bh.maPBH, pt.bienSoXe, " +
                     "bh.ngayBD, " +
                     "bh.ngayKT, " +
                     "bh.trangThaiBH " +
                     "FROM PhieuBaoHanh bh " +
                     "JOIN PhuongTien pt ON bh.soVIN = pt.soVIN " +
                     "ORDER BY bh.maPBH ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maPBH");
                String bienSo = rs.getString("bienSoXe");
                Date dBD = rs.getDate("ngayBD");
                Date dKT = rs.getDate("ngayKT");
                LocalDate bd = dBD != null ? dBD.toLocalDate() : null;
                LocalDate kt = dKT != null ? dKT.toLocalDate() : null;
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
            String sqlCT = "INSERT INTO ChiTietPhieuBaoHanh (maPBH, maPT, tenHangMuc, thoiHanBaoHanh, ngayHetHan, dieuKienBaoHanh, ghiChu, lyDo, huongXuLy) VALUES (?, ?, ?, 12, DATEADD(year, 1, CAST(GETDATE() AS DATE)), N'Bảo hành chính hãng', N'Theo phiếu sửa chữa', ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sqlCT)) {
                stmt.setString(1, pbh.getSoPhieu());
                stmt.setString(2, "PT001");
                stmt.setString(3, pbh.getNoiDung() != null ? pbh.getNoiDung() : "Bảo hành linh kiện xe");
                stmt.setString(4, pbh.getNoiDung() != null ? pbh.getNoiDung() : "Bảo hành linh kiện xe");
                stmt.setString(5, "Kiểm tra và thay thế miễn phí");
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi themPhieuBaoHanh: " + e.getMessage());
            return false;
        }
    }

    // ===============================================================
    // 7. NHẬP KHO & XUẤT KHO
    // ===============================================================

    /**
     * Lấy danh sách Nhà cung cấp từ CSDL SQL Server để nhân viên kho chọn khi lập phiếu nhập.
     */
    public ArrayList<String> layDanhSachNhaCungCap() {
        ArrayList<String> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con != null) {
            String sql = "SELECT maNCC, tenNCC FROM NhaCungCap ORDER BY maNCC ASC";
            try (PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("maNCC") + " - " + rs.getString("tenNCC"));
                }
            } catch (SQLException e) {
                System.err.println("Lỗi layDanhSachNhaCungCap: " + e.getMessage());
            }
        }
        if (list.isEmpty()) {
            list.add("NCC001 - Công ty Phụ Tùng Sài Gòn");
            list.add("NCC002 - Phụ Tùng Miền Đông");
        }
        return list;
    }

    // Lưu phiếu nhập kho và cập nhật số lượng tồn vào CSDL
    public boolean themPhieuNhapKho(PhieuKho phieu, String maNV) throws SQLException {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Lỗi CSDL: Không thể kết nối tới SQL Server (localhost:1433)!");
        }

        boolean autoCommitGoc = con.getAutoCommit();
        try {
            // [Bắt đầu transaction SQL]
            con.setAutoCommit(false);

            // 1. Kiểm tra Nhà cung cấp hợp lệ
            String rawNcc = phieu.getNhaCungCap() != null ? phieu.getNhaCungCap().trim() : "";
            if (rawNcc.isEmpty()) {
                throw new SQLException("Nhà cung cấp không hợp lệ!");
            }
            String maNCC = rawNcc.contains(" - ") ? rawNcc.split(" - ")[0].trim() : rawNcc;

            boolean nccTonTai = false;
            try (PreparedStatement checkNCC = con.prepareStatement("SELECT maNCC FROM NhaCungCap WHERE maNCC = ?")) {
                checkNCC.setString(1, maNCC);
                try (ResultSet rs = checkNCC.executeQuery()) {
                    if (rs.next()) {
                        nccTonTai = true;
                    }
                }
            }
            if (!nccTonTai) {
                try (PreparedStatement checkTenNCC = con.prepareStatement("SELECT maNCC FROM NhaCungCap WHERE tenNCC LIKE ?")) {
                    checkTenNCC.setString(1, "%" + rawNcc + "%");
                    try (ResultSet rs = checkTenNCC.executeQuery()) {
                        if (rs.next()) {
                            maNCC = rs.getString("maNCC");
                            nccTonTai = true;
                        }
                    }
                }
            }
            if (!nccTonTai) {
                throw new SQLException("Nhà cung cấp không hợp lệ hoặc không tồn tại trong CSDL: " + rawNcc);
            }

            // 2. Xác định mã nhân viên lập phiếu
            String nvLap = (maNV != null && !maNV.isEmpty()) ? maNV : "NV04";
            try (PreparedStatement checkNV = con.prepareStatement("SELECT maNV FROM NhanVien WHERE maNV = ?")) {
                checkNV.setString(1, nvLap);
                try (ResultSet rs = checkNV.executeQuery()) {
                    if (!rs.next()) nvLap = "NV01";
                }
            }

            // 3. [Lưu phiếu nhập]
            String sqlPN = "INSERT INTO PhieuNhapKho (maPhieuNhap, maNCC, maNV, ngayNhap, tongTien) VALUES (?, ?, ?, CAST(GETDATE() AS DATE), ?)";
            try (PreparedStatement stmtPN = con.prepareStatement(sqlPN)) {
                stmtPN.setString(1, phieu.getSoPhieu());
                stmtPN.setString(2, maNCC);
                stmtPN.setString(3, nvLap);
                stmtPN.setDouble(4, (double) phieu.getTongTien());
                stmtPN.executeUpdate();
            }

            // 4. [Lưu chi tiết và Cộng tồn kho]
            if (phieu.getChiTiet() == null || phieu.getChiTiet().isEmpty()) {
                throw new SQLException("Phiếu rỗng, không có dòng chi tiết phụ tùng!");
            }

            String sqlCT = "INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap, maPT, tenPT, soLuong, donGia) VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateKho = "UPDATE Kho SET soLuongTon = soLuongTon + ? WHERE maPT = ?";

            for (ChiTietKho ct : phieu.getChiTiet()) {
                // Ngoại lệ: Mã phụ tùng không tồn tại
                try (PreparedStatement checkPT = con.prepareStatement("SELECT maPT FROM PhuTung WHERE maPT = ?")) {
                    checkPT.setString(1, ct.getMaPT());
                    try (ResultSet rsPT = checkPT.executeQuery()) {
                        if (!rsPT.next()) {
                            throw new SQLException("Mã phụ tùng không tồn tại trong CSDL: " + ct.getMaPT());
                        }
                    }
                }

                // Ngoại lệ: Số lượng không hợp lệ
                if (ct.getSoLuong() <= 0) {
                    throw new SQLException("Số lượng không hợp lệ cho phụ tùng " + ct.getMaPT() + ": " + ct.getSoLuong());
                }

                // Lưu chi tiết
                try (PreparedStatement stmtCT = con.prepareStatement(sqlCT)) {
                    stmtCT.setString(1, phieu.getSoPhieu());
                    stmtCT.setString(2, ct.getMaPT());
                    stmtCT.setString(3, ct.getTenPT());
                    stmtCT.setInt(4, ct.getSoLuong());
                    stmtCT.setDouble(5, (double) ct.getDonGia());
                    stmtCT.executeUpdate();
                }

                // [Cộng tồn kho]
                try (PreparedStatement stmtKho = con.prepareStatement(sqlUpdateKho)) {
                    stmtKho.setInt(1, ct.getSoLuong());
                    stmtKho.setString(2, ct.getMaPT());
                    int updated = stmtKho.executeUpdate();
                    if (updated == 0) {
                        String sqlInsertKho = "INSERT INTO Kho (maKho, maPT, soLuongTon, viTriKho, soLTT, soLTD) VALUES (?, ?, ?, N'KHO_CHINH', 5, 200)";
                        try (PreparedStatement stmtIns = con.prepareStatement(sqlInsertKho)) {
                            stmtIns.setString(1, "KHO_" + ct.getMaPT());
                            stmtIns.setString(2, ct.getMaPT());
                            stmtIns.setInt(3, ct.getSoLuong());
                            stmtIns.executeUpdate();
                        }
                    }
                }
            }

            // 5. [COMMIT] nếu tất cả thành công
            con.commit();
            return true;
        } catch (SQLException e) {
            // [ROLLBACK] nếu có bất kỳ lỗi nào xảy ra -> Thông báo lỗi, giữ tồn cũ
            try {
                con.rollback();
            } catch (SQLException exRoll) {
                System.err.println("Lỗi Rollback: " + exRoll.getMessage());
            }
            throw e;
        } finally {
            try {
                con.setAutoCommit(autoCommitGoc);
            } catch (SQLException ignored) {}
        }
    }

    public boolean themPhieuXuatKho(PhieuKho phieu, String maNV) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            String sqlPX = "INSERT INTO PhieuXuatKho (maPhieuXuat, maPSC, maNV, ngayXuat) VALUES (?, ?, ?, CAST(GETDATE() AS DATE))";
            try (PreparedStatement stmt = con.prepareStatement(sqlPX)) {
                stmt.setString(1, phieu.getSoPhieu());
                stmt.setString(2, "SC001");
                stmt.setString(3, maNV != null && !maNV.isEmpty() ? maNV : "NV03");
                stmt.executeUpdate();
            }

            String sqlCT = "INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat, maPT, soLuong, lyDoXuat) VALUES (?, ?, ?, ?)";
            for (ChiTietKho ct : phieu.getChiTiet()) {
                try (PreparedStatement stmt = con.prepareStatement(sqlCT)) {
                    stmt.setString(1, phieu.getSoPhieu());
                    stmt.setString(2, ct.getMaPT());
                    stmt.setInt(3, ct.getSoLuong());
                    stmt.setString(4, "Xuất vật tư sửa chữa: " + ct.getTenPT());
                    stmt.executeUpdate();
                }
                // Giảm tồn kho
                capNhatTonKhoTru(ct.getMaPT(), ct.getSoLuong());
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi themPhieuXuatKho: " + e.getMessage());
            return false;
        }
    }

    public void capNhatTonKhoCong(String maPT, int soLuong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        try (PreparedStatement ps = con.prepareStatement("UPDATE Kho SET soLuongTon = soLuongTon + ? WHERE maPT = ?")) {
            ps.setInt(1, soLuong);
            ps.setString(2, maPT);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void capNhatTonKhoTru(String maPT, int soLuong) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return;
        try (PreparedStatement ps = con.prepareStatement("UPDATE Kho SET soLuongTon = CASE WHEN soLuongTon >= ? THEN soLuongTon - ? ELSE 0 END WHERE maPT = ?")) {
            ps.setInt(1, soLuong);
            ps.setInt(2, soLuong);
            ps.setString(3, maPT);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }
}
