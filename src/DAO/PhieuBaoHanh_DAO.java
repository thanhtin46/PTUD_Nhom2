package DAO;

import ConnectDB.ConnectDB;
import Entity.KhoDuLieu;
import Entity.PhieuBaoHanh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Truy vấn và xử lý dữ liệu phiếu bảo hành trong SQL Server. */
public class PhieuBaoHanh_DAO {

    public ArrayList<PhieuBaoHanh> layTatCa() {
        ArrayList<PhieuBaoHanh> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return ds;
        String sql = "SELECT bh.maPBH, pt.bienSoXe, CONVERT(VARCHAR(10), bh.ngayBD, 103) AS ngayBD, " +
                "CONVERT(VARCHAR(10), bh.ngayKT, 103) AS ngayKT, bh.trangThaiBH, ct.lyDo " +
                "FROM PhieuBaoHanh bh JOIN PhuongTien pt ON pt.soVIN = bh.soVIN " +
                "LEFT JOIN ChiTietPhieuBaoHanh ct ON ct.maPBH = bh.maPBH ORDER BY bh.maPBH DESC";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String status = rs.getString("trangThaiBH");
                String displayStatus = switch (status == null ? "" : status.toUpperCase()) {
                    case "CON_HAN" -> "Còn hạn";
                    case "DANG_XU_LY" -> "Đang xử lý";
                    case "HET_HAN" -> "Hết hạn";
                    default -> status == null ? "" : status;
                };
                ds.add(new PhieuBaoHanh(rs.getString("maPBH"), rs.getString("bienSoXe"),
                        rs.getString("ngayBD"), rs.getString("ngayKT"), "", displayStatus,
                        rs.getString("lyDo") == null ? "" : rs.getString("lyDo")));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi PhieuBaoHanh_DAO.layTatCa: " + e.getMessage());
        }
        return ds;
    }

    public ArrayList<PhieuBaoHanh> traCuu(String tuKhoa, int tieuChi, String tinhTrang) {
        String q = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        ArrayList<PhieuBaoHanh> result = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        ArrayList<PhieuBaoHanh> source = con == null
                ? new ArrayList<>(KhoDuLieu.get().getPhieuBaoHanhs()) : layTatCa();
        for (PhieuBaoHanh p : source) {
            String text = switch (tieuChi) {
                case 1 -> p.getSoPhieu();
                case 2 -> p.getBienSo();
                case 3 -> p.getTinhTrang();
                case 4 -> p.getNoiDung();
                default -> String.join(" ", safe(p.getSoPhieu()), safe(p.getBienSo()), safe(p.getNgayBatDau()),
                        safe(p.getNgayKetThuc()), safe(p.getTinhTrang()), safe(p.getNoiDung()));
            };
            boolean matchText = q.isEmpty() || safe(text).toLowerCase().contains(q);
            boolean matchStatus = tinhTrang == null || tinhTrang.isBlank() || "TAT_CA".equalsIgnoreCase(tinhTrang)
                    || tinhTrang.equalsIgnoreCase(p.getTinhTrang());
            if (matchText && matchStatus) result.add(p);
        }
        return result;
    }

    public boolean them(PhieuBaoHanh pbh) {
        if (pbh == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        String lookup = "SELECT soVIN, maKH FROM PhuongTien WHERE bienSoXe = ?";
        try {
            String vin, maKH;
            try (PreparedStatement ps = con.prepareStatement(lookup)) {
                ps.setString(1, pbh.getBienSo());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                    vin = rs.getString("soVIN"); maKH = rs.getString("maKH");
                }
            }
            con.setAutoCommit(false);
            String status = toDatabaseStatus(pbh.getTinhTrang());
            String insert = "INSERT INTO PhieuBaoHanh (maPBH, ngayBD, ngayKT, trangThaiBH, maKH, soVIN, maHoaDon) " +
                    "VALUES (?, TRY_CONVERT(date, ?, 103), TRY_CONVERT(date, ?, 103), ?, ?, ?, NULL)";
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setString(1, pbh.getSoPhieu()); ps.setString(2, pbh.getNgayBatDau()); ps.setString(3, pbh.getNgayKetThuc());
                ps.setString(4, status); ps.setString(5, maKH); ps.setString(6, vin); ps.executeUpdate();
            }
            themChiTietMacDinh(con, pbh);
            con.commit();
            return true;
        } catch (SQLException e) {
            rollback(con); System.err.println("Lỗi PhieuBaoHanh_DAO.them: " + e.getMessage()); return false;
        } finally { autoCommit(con); }
    }

    public boolean capNhat(PhieuBaoHanh pbh) {
        if (pbh == null) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        String sql = "UPDATE bh SET ngayBD = TRY_CONVERT(date, ?, 103), ngayKT = TRY_CONVERT(date, ?, 103), " +
                "trangThaiBH = ?, soVIN = pt.soVIN, maKH = pt.maKH FROM PhieuBaoHanh bh " +
                "JOIN PhuongTien pt ON pt.bienSoXe = ? WHERE bh.maPBH = ?";
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, pbh.getNgayBatDau()); ps.setString(2, pbh.getNgayKetThuc());
                ps.setString(3, toDatabaseStatus(pbh.getTinhTrang())); ps.setString(4, pbh.getBienSo()); ps.setString(5, pbh.getSoPhieu());
                if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE ChiTietPhieuBaoHanh SET lyDo = ? WHERE maPBH = ?")) {
                ps.setString(1, pbh.getNoiDung()); ps.setString(2, pbh.getSoPhieu());
                if (ps.executeUpdate() == 0) themChiTietMacDinh(con, pbh);
            }
            con.commit(); return true;
        } catch (SQLException e) {
            rollback(con); System.err.println("Lỗi PhieuBaoHanh_DAO.capNhat: " + e.getMessage()); return false;
        } finally { autoCommit(con); }
    }

    public boolean xoa(String soPhieu) {
        if (soPhieu == null || soPhieu.isBlank()) return false;
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietPhieuBaoHanh WHERE maPBH = ?")) {
                ps.setString(1, soPhieu); ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM PhieuBaoHanh WHERE maPBH = ?")) {
                ps.setString(1, soPhieu); if (ps.executeUpdate() == 0) { con.rollback(); return false; }
            }
            con.commit(); return true;
        } catch (SQLException e) {
            rollback(con); System.err.println("Lỗi PhieuBaoHanh_DAO.xoa: " + e.getMessage()); return false;
        } finally { autoCommit(con); }
    }

    private void themChiTietMacDinh(Connection con, PhieuBaoHanh pbh) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuBaoHanh (maPBH, maPT, lyDo, huongXuLy) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pbh.getSoPhieu()); ps.setString(2, "PT001");
            ps.setString(3, pbh.getNoiDung() == null ? "Bảo hành linh kiện xe" : pbh.getNoiDung());
            ps.setString(4, "Kiểm tra và thay thế miễn phí"); ps.executeUpdate();
        }
    }

    private String toDatabaseStatus(String status) {
        if ("Hết hạn".equalsIgnoreCase(status)) return "HET_HAN";
        if ("Đang xử lý".equalsIgnoreCase(status)) return "DANG_XU_LY";
        return "CON_HAN";
    }
    private String safe(String value) { return value == null ? "" : value; }
    private void rollback(Connection con) { if (con != null) try { con.rollback(); } catch (SQLException ignored) { } }
    private void autoCommit(Connection con) { if (con != null) try { con.setAutoCommit(true); } catch (SQLException ignored) { } }
}
