package DAO;

import ConnectDB.ConnectDB;
import Entity.KhoDuLieu;
import Entity.PhuongTien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Lớp DAO tra cứu thông tin và lịch sử phương tiện.
 */
public class TraCuuPhuongTien_DAO {

    /**
     * DTO chứa đầy đủ thông tin phương tiện kèm thông tin chủ sở hữu
     */
    public static class PhuongTienDTO {
        private String bienSo;
        private String hangXe;
        private String dongXe;
        private String soKhung;
        private String soMay;
        private String mauSon;
        private String namSX;
        private String odo;
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private String maChuXe;
        private LocalDate ngayTiepNhanGanNhat;
        private String tenChuXe;
        private String sdtChuXe;
        private String diaChiChuXe;
        private String emailChuXe;

        public PhuongTienDTO(String bienSo, String hangXe, String dongXe, String soKhung,
                             String soMay, String mauSon, String namSX, String odo,
                             String maChuXe, LocalDate ngayTiepNhanGanNhat,
                             String tenChuXe, String sdtChuXe, String diaChiChuXe, String emailChuXe) {
            this.bienSo = bienSo;
            this.hangXe = hangXe;
            this.dongXe = dongXe;
            this.soKhung = soKhung;
            this.soMay = soMay;
            this.mauSon = mauSon;
            this.namSX = namSX;
            this.odo = odo;
            this.maChuXe = maChuXe;
            this.ngayTiepNhanGanNhat = ngayTiepNhanGanNhat;
            this.tenChuXe = tenChuXe;
            this.sdtChuXe = sdtChuXe;
            this.diaChiChuXe = diaChiChuXe;
            this.emailChuXe = emailChuXe;
        }

        public String getBienSo() { return bienSo; }
        public String getHangXe() { return hangXe; }
        public String getDongXe() { return dongXe; }
        public String getSoKhung() { return soKhung; }
        public String getSoMay() { return soMay; }
        public String getMauSon() { return mauSon; }
        public String getNamSX() { return namSX; }
        public String getOdo() { return odo; }
        public String getMaChuXe() { return maChuXe; }
        public LocalDate getNgayTiepNhanGanNhat() { return ngayTiepNhanGanNhat; }
        public String getNgayTiepNhanGanNhatHienThi() { return ngayTiepNhanGanNhat != null ? ngayTiepNhanGanNhat.format(DATE_FORMATTER) : ""; }
        public String getTenChuXe() { return tenChuXe; }
        public String getSdtChuXe() { return sdtChuXe; }
        public String getDiaChiChuXe() { return diaChiChuXe; }
        public String getEmailChuXe() { return emailChuXe; }
    }

    /**
     * Tra cứu danh sách phương tiện theo từ khóa, tiêu chí và bộ lọc nhanh
     * @param tuKhoa Từ khóa tìm kiếm
     * @param tieuChi 0: Tất cả, 1: Biển số, 2: Hãng xe, 3: Dòng xe, 4: Chủ xe
     * @param boLocNhanh "TAT_CA", "BIEN_SO", "HANG_XE", "CHU_XE"
     */
    public ArrayList<PhuongTienDTO> traCuuPhuongTien(String tuKhoa, int tieuChi, String boLocNhanh) {
        ArrayList<PhuongTienDTO> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return fallbackCache(tuKhoa, tieuChi);

        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT pt.soVIN, pt.bienSoXe, pt.hangXe, pt.dongXe, pt.maSX, pt.maKH, ")
           .append("ISNULL(pt.soMay, '') AS soMay, ")
           .append("ISNULL(pt.mauSon, '') AS mauSon, ")
           .append("ISNULL(pt.odo, '0') AS odo, ")
           .append("ISNULL(pt.ngayTiepNhanGanNhat, '') AS ngayTiepNhanGanNhat, ")
           .append("ISNULL(kh.hoTen, N'Chưa có thông tin') AS tenChuXe, ")
           .append("ISNULL(kh.soDT, '') AS sdtChuXe, ")
           .append("ISNULL(kh.diaChi, '') AS diaChiChuXe, ")
           .append("ISNULL(kh.email, '') AS emailChuXe ")
           .append("FROM PhuongTien pt ")
           .append("LEFT JOIN KhachHang kh ON pt.maKH = kh.maKH ")
           .append("WHERE 1=1 ");

        // 1. Điều kiện từ khóa theo tiêu chí
        if (!q.isEmpty()) {
            switch (tieuChi) {
                case 1 -> sql.append("AND LOWER(pt.bienSoXe) LIKE ? ");
                case 2 -> sql.append("AND LOWER(pt.hangXe) LIKE ? ");
                case 3 -> sql.append("AND LOWER(pt.dongXe) LIKE ? ");
                case 4 -> sql.append("AND (LOWER(kh.hoTen) LIKE ? OR LOWER(kh.soDT) LIKE ? OR LOWER(pt.maKH) LIKE ?) ");
                default -> {
                    // Tất cả
                    sql.append("AND (LOWER(pt.bienSoXe) LIKE ? ")
                       .append("OR LOWER(pt.hangXe) LIKE ? ")
                       .append("OR LOWER(pt.dongXe) LIKE ? ")
                       .append("OR LOWER(kh.hoTen) LIKE ? ")
                       .append("OR LOWER(kh.soDT) LIKE ? ")
                       .append("OR LOWER(pt.maKH) LIKE ? ")
                       .append("OR LOWER(pt.soVIN) LIKE ?) ");
                }
            }
        }

        sql.append("ORDER BY pt.bienSoXe ASC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (!q.isEmpty()) {
                String pattern = "%" + q + "%";
                switch (tieuChi) {
                    case 1, 2, 3 -> stmt.setString(paramIdx++, pattern);
                    case 4 -> {
                        stmt.setString(paramIdx++, pattern);
                        stmt.setString(paramIdx++, pattern);
                        stmt.setString(paramIdx++, pattern);
                    }
                    default -> {
                        for (int i = 0; i < 7; i++) {
                            stmt.setString(paramIdx++, pattern);
                        }
                    }
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String vin = rs.getString("soVIN");
                    String bienSo = rs.getString("bienSoXe");
                    String hangXe = rs.getString("hangXe");
                    String dongXe = rs.getString("dongXe");
                    int namSX = rs.getInt("maSX");
                    String maChu = rs.getString("maKH");
                    String soMay = rs.getString("soMay");
                    String mauSon = rs.getString("mauSon");
                    String odo = rs.getString("odo");
                    Date dTN = rs.getDate("ngayTiepNhanGanNhat");
                    LocalDate ngayTN = dTN != null ? dTN.toLocalDate() : null;
                    String tenChu = rs.getString("tenChuXe");
                    String sdtChu = rs.getString("sdtChuXe");
                    String diaChiChu = rs.getString("diaChiChuXe");
                    String emailChu = rs.getString("emailChuXe");

                    list.add(new PhuongTienDTO(bienSo, hangXe, dongXe, vin, soMay, mauSon,
                            String.valueOf(namSX), odo, maChu, ngayTN, tenChu, sdtChu, diaChiChu, emailChu));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuPhuongTien_DAO.traCuuPhuongTien: " + e.getMessage());
            return fallbackCache(tuKhoa, tieuChi);
        }

        return list;
    }

    /**
     * Tìm kiếm dự phòng trên bộ nhớ cache nếu kết nối SQL Server gặp sự cố
     */
    private ArrayList<PhuongTienDTO> fallbackCache(String tuKhoa, int tieuChi) {
        ArrayList<PhuongTienDTO> list = new ArrayList<>();
        KhoDuLieu kho = KhoDuLieu.get();
        String q = (tuKhoa != null) ? tuKhoa.trim().toLowerCase() : "";

        for (PhuongTien xe : kho.getPhuongTiens()) {
            var chu = kho.timKhachHangTheoMa(xe.getMaChuXe());
            String tenChu = chu != null ? chu.getHoTen() : "";
            String sdtChu = chu != null ? chu.getSdt() : "";
            String diaChiChu = chu != null ? chu.getDiaChi() : "";
            String emailChu = chu != null ? chu.getEmail() : "";

            boolean match = q.isEmpty();
            if (!match) {
                switch (tieuChi) {
                    case 1 -> match = xe.getBienSo() != null && xe.getBienSo().toLowerCase().contains(q);
                    case 2 -> match = xe.getHangXe() != null && xe.getHangXe().toLowerCase().contains(q);
                    case 3 -> match = xe.getDongXe() != null && xe.getDongXe().toLowerCase().contains(q);
                    case 4 -> match = (tenChu.toLowerCase().contains(q) || sdtChu.contains(q) || (xe.getMaChuXe() != null && xe.getMaChuXe().toLowerCase().contains(q)));
                    default -> match = (xe.getBienSo() != null && xe.getBienSo().toLowerCase().contains(q))
                            || (xe.getHangXe() != null && xe.getHangXe().toLowerCase().contains(q))
                            || (xe.getDongXe() != null && xe.getDongXe().toLowerCase().contains(q))
                            || tenChu.toLowerCase().contains(q)
                            || sdtChu.contains(q)
                            || (xe.getMaChuXe() != null && xe.getMaChuXe().toLowerCase().contains(q));
                }
            }

            if (match) {
                list.add(new PhuongTienDTO(xe.getBienSo(), xe.getHangXe(), xe.getDongXe(),
                        xe.getSoKhung(), xe.getSoMay(), xe.getMauSon(), xe.getNamSX(),
                        xe.getOdo(), xe.getMaChuXe(), xe.getNgayTiepNhanGanNhat(),
                        tenChu, sdtChu, diaChiChu, emailChu));
            }
        }
        return list;
    }

    /**
     * Lấy lịch sử sửa chữa của chiếc xe theo biển số
     */
    public ArrayList<TraCuuKhachHang_DAO.LichSuSuaChuaDTO> layLichSuSuaChuaCuaXe(String bienSoXe) {
        ArrayList<TraCuuKhachHang_DAO.LichSuSuaChuaDTO> list = new ArrayList<>();
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) return list;

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT tn.maPTN, " +
                     "CONVERT(VARCHAR(10), tn.ngayTN, 103) AS ngayTNStr, " +
                     "pt.bienSoXe, " +
                     "ISNULL(pt.hangXe, '') + ' ' + ISNULL(pt.dongXe, '') AS xe, " +
                     "ISNULL(tn.yeuCauSC, '') AS yeuCauSC, " +
                     "ISNULL(ktv.hoTen, N'Chưa giao') AS tenKTV, " +
                     "ISNULL(tn.trangThaiXe, N'Chờ tiếp nhận') AS trangThaiXe, " +
                     "ISNULL(hd.tongTien, 0) AS chiPhi " +
                     "FROM PhieuTiepNhan tn " +
                     "JOIN PhuongTien pt ON tn.soVIN = pt.soVIN " +
                     "LEFT JOIN PhieuSuaChua sc ON (sc.maPTN = tn.maPTN OR (sc.maPTN IS NULL AND ('SC' + SUBSTRING(tn.maPTN, 3, 10)) = sc.maPSC)) " +
                     "LEFT JOIN NhanVien ktv ON sc.maNV = ktv.maNV " +
                     "OUTER APPLY (SELECT TOP 1 tongTien FROM HoaDon hd WHERE hd.maKH = pt.maKH AND hd.ngayLap = CAST(tn.ngayTN AS DATE)) hd " +
                     "WHERE LOWER(pt.bienSoXe) = ? " +
                     "ORDER BY tn.ngayTN DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bienSoXe.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String soPhieu = rs.getString("maPTN");
                    String ngayTN = rs.getString("ngayTNStr");
                    String bienSo = rs.getString("bienSoXe");
                    String xe = rs.getString("xe");
                    String yeuCau = rs.getString("yeuCauSC");
                    String tenKTV = rs.getString("tenKTV");
                    String trangThaiXe = rs.getString("trangThaiXe");
                    long chiPhi = rs.getLong("chiPhi");

                    list.add(new TraCuuKhachHang_DAO.LichSuSuaChuaDTO(
                            soPhieu, ngayTN, bienSo, xe, yeuCau, tenKTV, trangThaiXe, chiPhi
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuPhuongTien_DAO.layLichSuSuaChuaCuaXe: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy tổng số lượng phương tiện trong CSDL
     */
    public int layTongSoPhuongTien() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return KhoDuLieu.get().getPhuongTiens().size();

        String sql = "SELECT COUNT(*) FROM PhuongTien";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuPhuongTien_DAO.layTongSoPhuongTien: " + e.getMessage());
        }
        return KhoDuLieu.get().getPhuongTiens().size();
    }
}
