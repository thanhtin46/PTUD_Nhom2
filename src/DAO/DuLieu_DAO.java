package DAO;

import ConnectDB.ConnectDB;
import Entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DuLieu_DAO {

    public ArrayList<KhachHang> layTatCaKhachHang() {
        ArrayList<KhachHang> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT maKH, hoTen, soDT, chungMinh FROM KhachHang";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maKH");
                String ten = rs.getString("hoTen");
                String sdt = rs.getString("soDT");
                String cccd = rs.getString("chungMinh");
                list.add(new KhachHang(ma, ten, sdt, cccd != null ? cccd : "", "", "", "", ""));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<PhuongTien> layTatCaPhuongTien() {
        ArrayList<PhuongTien> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT soVIN, bienSoXe, hangXe, dongXe, maSX, maKH FROM PhuongTien";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String vin = rs.getString("soVIN");
                String bienSo = rs.getString("bienSoXe");
                String hangXe = rs.getString("hangXe");
                String dongXe = rs.getString("dongXe");
                int namSX = rs.getInt("maSX");
                String maKH = rs.getString("maKH");
                list.add(new PhuongTien(bienSo, hangXe, dongXe, vin, "", "", String.valueOf(namSX), "0", maKH, ""));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<PhuTung> layTatCaPhuTung() {
        ArrayList<PhuTung> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT pt.maPT, pt.tenPT, pt.donViTinh, pt.donGiaBan, " +
                     "ISNULL(k.soLuongTon, 0) AS soLuongTon, ISNULL(k.soLTT, 5) AS soLTT " +
                     "FROM PhuTung pt LEFT JOIN Kho k ON pt.maPT = k.maPT";
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
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<PhieuTiepNhan> layTatCaPhieuTiepNhan() {
        ArrayList<PhieuTiepNhan> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT tn.maPTN, tn.maKH, kh.hoTen, kh.soDT, pt.bienSoXe, pt.hangXe, pt.dongXe, " +
                     "CONVERT(VARCHAR(10), tn.ngayTN, 103) AS ngayTNStr, tn.yeuCauSC, tn.trangThaiXe, nv.hoTen AS tenNV " +
                     "FROM PhieuTiepNhan tn " +
                     "JOIN KhachHang kh ON tn.maKH = kh.maKH " +
                     "JOIN PhuongTien pt ON tn.soVIN = pt.soVIN " +
                     "LEFT JOIN NhanVien nv ON tn.maNV = nv.maNV";
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

                list.add(new PhieuTiepNhan(maTN, maKH, hoTen, sdt, bienSo, hangXe, loaiXe,
                        ngayTN != null ? ngayTN : "", yeuCau, trangThai, nguoiLap != null ? nguoiLap : "", 0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<PhieuBaoHanh> layTatCaPhieuBaoHanh() {
        ArrayList<PhieuBaoHanh> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT bh.maPBH, pt.bienSoXe, " +
                     "CONVERT(VARCHAR(10), bh.ngayBD, 103) AS bdStr, " +
                     "CONVERT(VARCHAR(10), bh.ngayKT, 103) AS ktStr, " +
                     "bh.trangThaiBH " +
                     "FROM PhieuBaoHanh bh " +
                     "JOIN PhuongTien pt ON bh.soVIN = pt.soVIN";
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
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<HoaDon> layTatCaHoaDon() {
        ArrayList<HoaDon> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT hd.maHD, hd.maKH, kh.hoTen, kh.soDT, " +
                     "ISNULL(pt.bienSoXe, N'Chưa rõ') AS bienSoXe, " +
                     "CONVERT(VARCHAR(10), hd.ngayLap, 103) AS ngayLapStr, " +
                     "hd.hinhThucThanhToan, nv.hoTen AS tenNV, " +
                     "ISNULL(SUM(ct.thanhTien), 0) AS tongTien " +
                     "FROM HoaDon hd " +
                     "JOIN KhachHang kh ON hd.maKH = kh.maKH " +
                     "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                     "LEFT JOIN PhuongTien pt ON pt.maKH = kh.maKH " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.maHD = ct.maHD " +
                     "GROUP BY hd.maHD, hd.maKH, kh.hoTen, kh.soDT, pt.bienSoXe, hd.ngayLap, hd.hinhThucThanhToan, nv.hoTen";

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

                String ptTT = "TIEN_MAT".equalsIgnoreCase(httt) ? "Tiền mặt" : "Chuyển khoản QR";

                list.add(new HoaDon(maHD, "SC001", maKH, tenKH, sdt, bienSo, ngay,
                        tong / 2, tong / 2, 0, tong, ptTT, tenNV != null ? tenNV : "Thu ngân", "", HoaDon.DA_THANH_TOAN));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
