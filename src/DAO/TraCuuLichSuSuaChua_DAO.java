package DAO;

import ConnectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * TraCuuLichSuSuaChua_DAO — DAO chuyên trách tra cứu lịch sử sửa chữa của khách hàng / người dùng:
 * - Tra cứu đa tiêu chí kết hợp SQL Server: Tên khách hàng, SĐT, Biển số xe, Mã phiếu TN, Mã phiếu SC, CCCD/Mã KH
 * - Lọc theo trạng thái tiến độ: Tất cả, Hoàn thành, Đang sửa chữa, Đã phân công, Chờ phụ tùng, Chờ tiếp nhận
 * - Lọc theo trạng thái thanh toán: Tất cả, Đã thanh toán, Chưa thanh toán
 * - Lọc theo thời gian: Tất cả, Hôm nay, Tháng này, Năm nay
 * - Truy vấn chi tiết công việc sửa chữa / tiền công
 * - Truy vấn chi tiết phụ tùng thay thế / đơn giá / thành tiền
 * - Truy vấn thông tin hóa đơn & thời hạn bảo hành liên quan
 */
public class TraCuuLichSuSuaChua_DAO {

    /**
     * DTO đại diện cho một bản ghi trong danh sách lịch sử sửa chữa
     */
    public static class LichSuRecord {
        private String maPTN;
        private String maPSC;
        private String ngayTN;
        private String maKH;
        private String tenKH;
        private String soDT;
        private String chungMinh;
        private String soVIN;
        private String bienSoXe;
        private String tenXe;
        private String yeuCauSC;
        private String tenKTV;
        private String trangThaiSC;
        private String maHD;
        private long tienDichVu;
        private long tienPhuTung;
        private long tongTien;
        private String trangThaiHD;
        private String hinhThucTT;

        public LichSuRecord(String maPTN, String maPSC, String ngayTN, String maKH, String tenKH,
                            String soDT, String chungMinh, String soVIN, String bienSoXe, String tenXe,
                            String yeuCauSC, String tenKTV, String trangThaiSC, String maHD,
                            long tienDichVu, long tienPhuTung, long tongTien, String trangThaiHD, String hinhThucTT) {
            this.maPTN = maPTN;
            this.maPSC = maPSC;
            this.ngayTN = ngayTN;
            this.maKH = maKH;
            this.tenKH = tenKH;
            this.soDT = soDT;
            this.chungMinh = chungMinh;
            this.soVIN = soVIN;
            this.bienSoXe = bienSoXe;
            this.tenXe = tenXe;
            this.yeuCauSC = yeuCauSC;
            this.tenKTV = tenKTV;
            this.trangThaiSC = trangThaiSC;
            this.maHD = maHD;
            this.tienDichVu = tienDichVu;
            this.tienPhuTung = tienPhuTung;
            this.tongTien = tongTien;
            this.trangThaiHD = trangThaiHD;
            this.hinhThucTT = hinhThucTT;
        }

        public String getMaPTN() { return maPTN != null ? maPTN : "—"; }
        public String getMaPSC() { return maPSC != null ? maPSC : "—"; }
        public String getNgayTN() { return ngayTN != null ? ngayTN : ""; }
        public String getMaKH() { return maKH != null ? maKH : ""; }
        public String getTenKH() { return tenKH != null ? tenKH : ""; }
        public String getSoDT() { return soDT != null ? soDT : ""; }
        public String getChungMinh() { return chungMinh != null ? chungMinh : ""; }
        public String getSoVIN() { return soVIN != null ? soVIN : ""; }
        public String getBienSoXe() { return bienSoXe != null ? bienSoXe : ""; }
        public String getTenXe() { return tenXe != null ? tenXe : ""; }
        public String getYeuCauSC() { return yeuCauSC != null ? yeuCauSC : ""; }
        public String getTenKTV() { return tenKTV != null ? tenKTV : "Chưa giao"; }
        public String getTrangThaiSC() { return trangThaiSC != null ? trangThaiSC : "Chờ tiếp nhận"; }
        public String getMaHD() { return maHD != null ? maHD : "—"; }
        public long getTienDichVu() { return tienDichVu; }
        public long getTienPhuTung() { return tienPhuTung; }
        public long getTongTien() { return tongTien; }
        public String getTrangThaiHD() { return trangThaiHD != null ? trangThaiHD : "Chưa thanh toán"; }
        public String getHinhThucTT() { return hinhThucTT != null ? hinhThucTT : "—"; }
    }

    /**
     * DTO công việc sửa chữa / dịch vụ
     */
    public static class ChiTietCongViecDTO {
        private int stt;
        private String noiDung;
        private String tenKTV;
        private long tienCong;

        public ChiTietCongViecDTO(int stt, String noiDung, String tenKTV, long tienCong) {
            this.stt = stt;
            this.noiDung = noiDung;
            this.tenKTV = tenKTV;
            this.tienCong = tienCong;
        }

        public int getStt() { return stt; }
        public String getNoiDung() { return noiDung != null ? noiDung : ""; }
        public String getTenKTV() { return tenKTV != null ? tenKTV : "—"; }
        public long getTienCong() { return tienCong; }
    }

    /**
     * DTO phụ tùng linh kiện thay thế
     */
    public static class ChiTietPhuTungDTO {
        private int stt;
        private String maPT;
        private String tenPT;
        private String donViTinh;
        private int soLuong;
        private long donGia;
        private long thanhTien;
        private String trangThai;

        public ChiTietPhuTungDTO(int stt, String maPT, String tenPT, String donViTinh, int soLuong, long donGia, long thanhTien, String trangThai) {
            this.stt = stt;
            this.maPT = maPT;
            this.tenPT = tenPT;
            this.donViTinh = donViTinh;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.thanhTien = thanhTien;
            this.trangThai = trangThai;
        }

        public int getStt() { return stt; }
        public String getMaPT() { return maPT != null ? maPT : ""; }
        public String getTenPT() { return tenPT != null ? tenPT : ""; }
        public String getDonViTinh() { return donViTinh != null ? donViTinh : "Cái"; }
        public int getSoLuong() { return soLuong; }
        public long getDonGia() { return donGia; }
        public long getThanhTien() { return thanhTien; }
        public String getTrangThai() { return trangThai != null ? trangThai : "Đã hoàn thành"; }
    }

    /**
     * DTO bảo hành liên quan
     */
    public static class ChiTietBaoHanhDTO {
        private String maPBH;
        private String ngayBD;
        private String ngayKT;
        private String trangThai;
        private String tenHangMuc;
        private int thoiHan;
        private String dieuKien;

        public ChiTietBaoHanhDTO(String maPBH, String ngayBD, String ngayKT, String trangThai, String tenHangMuc, int thoiHan, String dieuKien) {
            this.maPBH = maPBH;
            this.ngayBD = ngayBD;
            this.ngayKT = ngayKT;
            this.trangThai = trangThai;
            this.tenHangMuc = tenHangMuc;
            this.thoiHan = thoiHan;
            this.dieuKien = dieuKien;
        }

        public String getMaPBH() { return maPBH != null ? maPBH : "—"; }
        public String getNgayBD() { return ngayBD != null ? ngayBD : ""; }
        public String getNgayKT() { return ngayKT != null ? ngayKT : ""; }
        public String getTrangThai() { return trangThai != null ? trangThai : ""; }
        public String getTenHangMuc() { return tenHangMuc != null ? tenHangMuc : ""; }
        public int getThoiHan() { return thoiHan; }
        public String getDieuKien() { return dieuKien != null ? dieuKien : ""; }
    }

    // =========================================================================
    //                        CHUẨN HÓA TRẠNG THÁI HIỂN THỊ
    // =========================================================================
    public static String chuanHoaTrangThaiSC(String tt) {
        if (tt == null || tt.trim().isEmpty()) return "Chờ tiếp nhận";
        String s = tt.trim().toUpperCase();
        if (s.contains("HOAN_THANH") || s.contains("HOÀN THÀNH")) return "Hoàn thành";
        if (s.contains("DANG_SUA") || s.contains("ĐANG SỬA")) return "Đang sửa chữa";
        if (s.contains("CHO_PHU_TUNG") || s.contains("CHỜ PHỤ TÙNG")) return "Chờ phụ tùng";
        if (s.contains("DA_PHAN_CONG") || s.contains("ĐÃ PHÂN CÔNG")) return "Đã phân công";
        if (s.contains("CHO_SUA") || s.contains("CHỜ SỬA")) return "Chờ sửa chữa";
        if (s.contains("CHO_TIEP_NHAN") || s.contains("CHỜ TIẾP NHẬN")) return "Chờ tiếp nhận";
        return tt;
    }

    public static String chuanHoaTrangThaiHD(String tt, String maHD) {
        if (maHD == null || maHD.trim().isEmpty() || "—".equals(maHD)) return "Chưa thanh toán";
        if (tt == null || tt.trim().isEmpty()) return "Chưa thanh toán";
        String s = tt.trim().toUpperCase();
        if (s.contains("DA_THANH_TOAN") || s.contains("ĐÃ THANH TOÁN")) return "Đã thanh toán";
        if (s.contains("DA_HUY") || s.contains("ĐÃ HỦY")) return "Đã hủy";
        return tt;
    }

    public static String chuanHoaHinhThucTT(String ht) {
        if (ht == null || ht.trim().isEmpty()) return "—";
        String s = ht.trim().toUpperCase();
        if (s.contains("TIEN_MAT") || s.contains("TIỀN MẶT")) return "Tiền mặt";
        if (s.contains("CHUYEN_KHOAN") || s.contains("CHUYỂN KHOẢN")) return "Chuyển khoản QR";
        if (s.contains("THE") || s.contains("THẺ") || s.contains("POS")) return "Quẹt thẻ POS";
        return ht;
    }

    public static String chuanHoaTrangThaiBH(String tt) {
        if (tt == null || tt.trim().isEmpty()) return "Còn hạn";
        String s = tt.trim().toUpperCase();
        if (s.contains("CON_HAN") || s.contains("CÒN HẠN")) return "Còn hạn";
        if (s.contains("HET_HAN") || s.contains("HẾT HẠN")) return "Hết hạn";
        if (s.contains("DANG_XU_LY") || s.contains("ĐANG XỬ LÝ")) return "Đang xử lý";
        return tt;
    }

    // =========================================================================
    //                        TRUY VẤN DANH SÁCH LỊCH SỬ
    // =========================================================================
    public ArrayList<LichSuRecord> traCuuLichSu(String tuKhoa, int tieuChi, String locTrangThai, String locThanhToan, String locThoiGian) {
        ArrayList<LichSuRecord> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
          .append("tn.maPTN, ")
          .append("sc.maPSC, ")
          .append("CONVERT(VARCHAR(10), tn.ngayTN, 103) + ' ' + CONVERT(VARCHAR(5), tn.ngayTN, 108) AS ngayTNStr, ")
          .append("kh.maKH, ")
          .append("kh.hoTen AS tenKH, ")
          .append("kh.soDT, ")
          .append("kh.chungMinh, ")
          .append("tn.soVIN, ")
          .append("pt.bienSoXe, ")
          .append("ISNULL(pt.hangXe, '') + ' ' + ISNULL(pt.dongXe, '') AS tenXe, ")
          .append("ISNULL(tn.yeuCauSC, '') AS yeuCauSC, ")
          .append("ISNULL(ktv.hoTen, N'Chưa giao') AS tenKTV, ")
          .append("ISNULL(sc.trangThai, ISNULL(tn.trangThaiXe, N'Chờ tiếp nhận')) AS trangThaiSC, ")
          .append("hd.maHD, ")
          .append("ISNULL(hd.tienDichVu, 0) AS tienDichVu, ")
          .append("ISNULL(hd.tienPhuTung, 0) AS tienPhuTung, ")
          .append("ISNULL(hd.tongTien, 0) AS tongTien, ")
          .append("ISNULL(hd.trangThai, N'Chưa thanh toán') AS trangThaiHD, ")
          .append("hd.hinhThucThanhToan ")
          .append("FROM PhieuTiepNhan tn ")
          .append("JOIN KhachHang kh ON tn.maKH = kh.maKH ")
          .append("JOIN PhuongTien pt ON tn.soVIN = pt.soVIN ")
          .append("LEFT JOIN PhieuSuaChua sc ON (sc.maPTN = tn.maPTN OR ('SC' + SUBSTRING(tn.maPTN, 3, 10)) = sc.maPSC) ")
          .append("LEFT JOIN NhanVien ktv ON sc.maNV = ktv.maNV ")
          .append("OUTER APPLY (SELECT TOP 1 maHD, tienDichVu, tienPhuTung, tongTien, trangThai, hinhThucThanhToan FROM HoaDon h WHERE h.maKH = tn.maKH AND h.ngayLap = CAST(tn.ngayTN AS DATE)) hd ")
          .append("WHERE 1=1 ");

        ArrayList<Object> params = new ArrayList<>();

        // 1. Lọc từ khóa theo tiêu chí
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            String kw = "%" + tuKhoa.trim() + "%";
            switch (tieuChi) {
                case 1 -> { // Tên khách hàng
                    sb.append("AND kh.hoTen LIKE ? ");
                    params.add(kw);
                }
                case 2 -> { // Số điện thoại
                    sb.append("AND kh.soDT LIKE ? ");
                    params.add(kw);
                }
                case 3 -> { // Biển số xe
                    sb.append("AND pt.bienSoXe LIKE ? ");
                    params.add(kw);
                }
                case 4 -> { // Mã phiếu tiếp nhận
                    sb.append("AND tn.maPTN LIKE ? ");
                    params.add(kw);
                }
                case 5 -> { // Mã phiếu sửa chữa
                    sb.append("AND sc.maPSC LIKE ? ");
                    params.add(kw);
                }
                case 6 -> { // Mã KH / CCCD
                    sb.append("AND (kh.maKH LIKE ? OR kh.chungMinh LIKE ?) ");
                    params.add(kw);
                    params.add(kw);
                }
                default -> { // Tất cả tiêu chí (Tự động nhận diện)
                    sb.append("AND (kh.hoTen LIKE ? OR kh.soDT LIKE ? OR pt.bienSoXe LIKE ? OR tn.maPTN LIKE ? OR sc.maPSC LIKE ? OR kh.maKH LIKE ? OR kh.chungMinh LIKE ?) ");
                    for (int i = 0; i < 7; i++) params.add(kw);
                }
            }
        }

        // 2. Lọc trạng thái tiến độ
        if (locTrangThai != null && !locTrangThai.isEmpty() && !"TAT_CA".equalsIgnoreCase(locTrangThai)) {
            switch (locTrangThai.toUpperCase()) {
                case "HOAN_THANH" -> sb.append("AND (sc.trangThai LIKE '%HOAN_THANH%' OR sc.trangThai LIKE N'%Hoàn thành%') ");
                case "DANG_SUA" -> sb.append("AND (sc.trangThai LIKE '%DANG_SUA%' OR sc.trangThai LIKE N'%Đang sửa%') ");
                case "CHO_SUA" -> sb.append("AND (sc.trangThai LIKE '%CHO_SUA%' OR sc.trangThai LIKE N'%Chờ sửa%' OR sc.trangThai LIKE '%DA_PHAN_CONG%') ");
                case "CHO_PHU_TUNG" -> sb.append("AND (sc.trangThai LIKE '%CHO_PHU_TUNG%' OR sc.trangThai LIKE N'%Chờ phụ tùng%') ");
                case "CHO_TIEP_NHAN" -> sb.append("AND (sc.maPSC IS NULL OR tn.trangThaiXe LIKE '%CHO_TIEP_NHAN%' OR tn.trangThaiXe LIKE N'%Chờ tiếp nhận%') ");
            }
        }

        // 3. Lọc trạng thái thanh toán
        if (locThanhToan != null && !locThanhToan.isEmpty() && !"TAT_CA".equalsIgnoreCase(locThanhToan)) {
            if ("DA_THANH_TOAN".equalsIgnoreCase(locThanhToan)) {
                sb.append("AND hd.maHD IS NOT NULL AND (hd.trangThai LIKE '%DA_THANH_TOAN%' OR hd.trangThai LIKE N'%Đã thanh toán%') ");
            } else if ("CHUA_THANH_TOAN".equalsIgnoreCase(locThanhToan)) {
                sb.append("AND (hd.maHD IS NULL OR hd.trangThai IS NULL OR hd.trangThai NOT LIKE '%DA_THANH_TOAN%') ");
            }
        }

        // 4. Lọc thời gian
        if (locThoiGian != null && !locThoiGian.isEmpty() && !"TAT_CA".equalsIgnoreCase(locThoiGian)) {
            switch (locThoiGian.toUpperCase()) {
                case "HOM_NAY" -> sb.append("AND CAST(tn.ngayTN AS DATE) = CAST(GETDATE() AS DATE) ");
                case "THANG_NAY" -> sb.append("AND MONTH(tn.ngayTN) = MONTH(GETDATE()) AND YEAR(tn.ngayTN) = YEAR(GETDATE()) ");
                case "NAM_NAY" -> sb.append("AND YEAR(tn.ngayTN) = YEAR(GETDATE()) ");
            }
        }

        sb.append("ORDER BY tn.ngayTN DESC");

        try (PreparedStatement stmt = con.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String maPTN = rs.getString("maPTN");
                    String maPSC = rs.getString("maPSC");
                    String ngayTNStr = rs.getString("ngayTNStr");
                    String maKH = rs.getString("maKH");
                    String tenKH = rs.getString("tenKH");
                    String soDT = rs.getString("soDT");
                    String chungMinh = rs.getString("chungMinh");
                    String soVIN = rs.getString("soVIN");
                    String bienSoXe = rs.getString("bienSoXe");
                    String tenXe = rs.getString("tenXe");
                    String yeuCauSC = rs.getString("yeuCauSC");
                    String tenKTV = rs.getString("tenKTV");
                    String trangThaiSC = chuanHoaTrangThaiSC(rs.getString("trangThaiSC"));
                    String maHD = rs.getString("maHD");
                    long tienDichVu = (long) rs.getDouble("tienDichVu");
                    long tienPhuTung = (long) rs.getDouble("tienPhuTung");
                    long tongTien = (long) rs.getDouble("tongTien");
                    String trangThaiHD = chuanHoaTrangThaiHD(rs.getString("trangThaiHD"), maHD);
                    String hinhThucTT = chuanHoaHinhThucTT(rs.getString("hinhThucThanhToan"));

                    list.add(new LichSuRecord(maPTN, maPSC, ngayTNStr, maKH, tenKH, soDT,
                            chungMinh, soVIN, bienSoXe, tenXe != null ? tenXe.trim() : "",
                            yeuCauSC, tenKTV, trangThaiSC, maHD, tienDichVu, tienPhuTung,
                            tongTien, trangThaiHD, hinhThucTT));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TraCuuLichSuSuaChua_DAO.traCuuLichSu: " + e.getMessage());
        }

        return list;
    }

    // =========================================================================
    //                        TRUY VẤN CHI TIẾT CÔNG VIỆC
    // =========================================================================
    public ArrayList<ChiTietCongViecDTO> layChiTietCongViec(String maPSC, String maHD) {
        ArrayList<ChiTietCongViecDTO> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        // Ưu tiên 1: ChiTietCongViecSuaChua theo maPSC
        if (maPSC != null && !maPSC.trim().isEmpty() && !"—".equals(maPSC)) {
            String sql = "SELECT stt, noiDungCongViec, ISNULL(tenKTV, N'—') AS tenKTV, tienCong " +
                         "FROM ChiTietCongViecSuaChua WHERE maPSC = ? ORDER BY stt";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, maPSC.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new ChiTietCongViecDTO(
                                rs.getInt("stt"),
                                rs.getString("noiDungCongViec"),
                                rs.getString("tenKTV"),
                                (long) rs.getDouble("tienCong")
                        ));
                    }
                }
            } catch (SQLException ignored) {}
        }

        // Ưu tiên 2: ChiTietDichVuHoaDon theo maHD nếu ChiTietCongViecSuaChua rỗng
        if (list.isEmpty() && maHD != null && !maHD.trim().isEmpty() && !"—".equals(maHD)) {
            String sql = "SELECT stt, tenDichVu, donGia FROM ChiTietDichVuHoaDon WHERE maHD = ? ORDER BY stt";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, maHD.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new ChiTietCongViecDTO(
                                rs.getInt("stt"),
                                rs.getString("tenDichVu"),
                                "KTV Gara",
                                (long) rs.getDouble("donGia")
                        ));
                    }
                }
            } catch (SQLException ignored) {}
        }

        return list;
    }

    // =========================================================================
    //                        TRUY VẤN CHI TIẾT PHỤ TÙNG
    // =========================================================================
    public ArrayList<ChiTietPhuTungDTO> layChiTietPhuTung(String maPSC, String maHD) {
        ArrayList<ChiTietPhuTungDTO> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        // Ưu tiên 1: ChiTietSuaChua theo maPSC
        if (maPSC != null && !maPSC.trim().isEmpty() && !"—".equals(maPSC)) {
            String sql = "SELECT ct.maPT, " +
                         "ISNULL(ct.tenPhuTung, pt.tenPT) AS tenPT, " +
                         "ISNULL(ct.donViTinh, ISNULL(pt.donViTinh, N'Cái')) AS donViTinh, " +
                         "ct.soLuong, ct.donGia, (ct.soLuong * ct.donGia) AS thanhTien, " +
                         "ISNULL(ct.trangThai, N'DA_HOAN_THANH') AS trangThai " +
                         "FROM ChiTietSuaChua ct " +
                         "LEFT JOIN PhuTung pt ON ct.maPT = pt.maPT " +
                         "WHERE ct.maPSC = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, maPSC.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    int stt = 1;
                    while (rs.next()) {
                        String tt = rs.getString("trangThai");
                        String ttStr = (tt != null && (tt.contains("HOAN_THANH") || tt.contains("Hoàn thành")))
                                ? "Đã lắp đặt" : "Đang xử lý";
                        list.add(new ChiTietPhuTungDTO(
                                stt++,
                                rs.getString("maPT"),
                                rs.getString("tenPT"),
                                rs.getString("donViTinh"),
                                rs.getInt("soLuong"),
                                (long) rs.getDouble("donGia"),
                                (long) rs.getDouble("thanhTien"),
                                ttStr
                        ));
                    }
                }
            } catch (SQLException ignored) {}
        }

        // Ưu tiên 2: ChiTietHoaDon theo maHD nếu ChiTietSuaChua rỗng
        if (list.isEmpty() && maHD != null && !maHD.trim().isEmpty() && !"—".equals(maHD)) {
            String sql = "SELECT ct.maPT, ISNULL(ct.tenPhuTung, pt.tenPT) AS tenPT, " +
                         "ISNULL(ct.donViTinh, ISNULL(pt.donViTinh, N'Cái')) AS donViTinh, " +
                         "ct.soLuong, ct.donGia, (ct.soLuong * ct.donGia) AS thanhTien " +
                         "FROM ChiTietHoaDon ct " +
                         "LEFT JOIN PhuTung pt ON ct.maPT = pt.maPT " +
                         "WHERE ct.maHD = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, maHD.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    int stt = 1;
                    while (rs.next()) {
                        list.add(new ChiTietPhuTungDTO(
                                stt++,
                                rs.getString("maPT"),
                                rs.getString("tenPT"),
                                rs.getString("donViTinh"),
                                rs.getInt("soLuong"),
                                (long) rs.getDouble("donGia"),
                                (long) rs.getDouble("thanhTien"),
                                "Đã thanh toán"
                        ));
                    }
                }
            } catch (SQLException ignored) {}
        }

        return list;
    }

    // =========================================================================
    //                        TRUY VẤN BẢO HÀNH LIÊN QUAN
    // =========================================================================
    public ArrayList<ChiTietBaoHanhDTO> layChiTietBaoHanh(String maHD, String maKH, String soVIN) {
        ArrayList<ChiTietBaoHanhDTO> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;

        String sql = "SELECT pbh.maPBH, " +
                     "CONVERT(VARCHAR(10), pbh.ngayBD, 103) AS ngayBDStr, " +
                     "CONVERT(VARCHAR(10), pbh.ngayKT, 103) AS ngayKTStr, " +
                     "pbh.trangThaiBH, " +
                     "ISNULL(hm.tenHangMuc, pbh.ghiChu) AS tenHangMuc, " +
                     "ISNULL(hm.thoiHanBaoHanh, 12) AS thoiHan, " +
                     "ISNULL(hm.dieuKienBaoHanh, N'Theo tiêu chuẩn nhà sản xuất') AS dieuKien " +
                     "FROM PhieuBaoHanh pbh " +
                     "LEFT JOIN ChiTietPhieuBaoHanh hm ON pbh.maPBH = hm.maPBH " +
                     "WHERE (pbh.maHoaDon = ? AND ? <> '') OR (pbh.soVIN = ? AND ? <> '') OR (pbh.maKH = ? AND ? <> '')";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHD != null ? maHD : "");
            stmt.setString(2, maHD != null ? maHD : "");
            stmt.setString(3, soVIN != null ? soVIN : "");
            stmt.setString(4, soVIN != null ? soVIN : "");
            stmt.setString(5, maKH != null ? maKH : "");
            stmt.setString(6, maKH != null ? maKH : "");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChiTietBaoHanhDTO(
                            rs.getString("maPBH"),
                            rs.getString("ngayBDStr"),
                            rs.getString("ngayKTStr"),
                            chuanHoaTrangThaiBH(rs.getString("trangThaiBH")),
                            rs.getString("tenHangMuc"),
                            rs.getInt("thoiHan"),
                            rs.getString("dieuKien")
                    ));
                }
            }
        } catch (SQLException ignored) {}

        return list;
    }
}
