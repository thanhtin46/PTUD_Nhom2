package Entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Thực thể Hóa đơn thanh toán dịch vụ / sửa chữa gara.
 * Thiết kế chuẩn OOP theo quan hệ thực thể trong CSDL (Khách hàng, Nhân viên, Phiếu sửa chữa).
 */
public class HoaDon {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final String DA_THANH_TOAN = "Đã thanh toán";
    public static final String DA_HUY = "Đã hủy";

    private String maHD;
    private PhieuSuaChua phieuSuaChua;       // Liên kết phiếu sửa chữa
    private KhachHang khachHang;             // Khách hàng thanh toán
    private NhanVien nhanVien;               // Nhân viên thu ngân / lập hóa đơn
    private LocalDate ngayLap;
    private long tienDichVu;                 // Tiền công sửa chữa/dịch vụ
    private long tienPhuTung;                // Tiền linh kiện thay thế
    private double giamGiaPhanTram;          // Giảm giá %
    private long tongTien;                   // Tổng tiền sau giảm giá
    private String phuongThuc;               // Tiền mặt, Chuyển khoản, Thẻ
    private String ghiChu;
    private String trangThai;                // Đã thanh toán, Đã hủy

    public HoaDon() {
    }

    public HoaDon(String maHD) {
        this.maHD = maHD;
    }

    /** Constructor chuẩn OOP với các đối tượng tham chiếu */
    public HoaDon(String maHD, PhieuSuaChua phieuSuaChua, KhachHang khachHang, NhanVien nhanVien,
                  LocalDate ngayLap, long tienDichVu, long tienPhuTung,
                  double giamGiaPhanTram, long tongTien, String phuongThuc,
                  String ghiChu, String trangThai) {
        this.maHD = maHD;
        this.phieuSuaChua = phieuSuaChua;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.ngayLap = ngayLap;
        this.tienDichVu = tienDichVu;
        this.tienPhuTung = tienPhuTung;
        this.giamGiaPhanTram = giamGiaPhanTram;
        this.tongTien = tongTien;
        this.phuongThuc = phuongThuc;
        this.ghiChu = ghiChu;
        this.trangThai = "DA_THANH_TOAN".equalsIgnoreCase(trangThai) ? DA_THANH_TOAN
                : "DA_HUY".equalsIgnoreCase(trangThai) ? DA_HUY : trangThai;
    }

    /** Constructor tương thích ngược: tự động bọc chuỗi thành đối tượng liên kết */
    public HoaDon(String maHD, String soPhieuSC, String maKH, String tenKH, String sdtKH,
                  String bienSo, LocalDate ngayLap, long tienDichVu, long tienPhuTung,
                  double giamGiaPhanTram, long tongTien, String phuongThuc, String nguoiThu,
                  String ghiChu, String trangThai) {
        this.maHD = maHD;
        this.khachHang = new KhachHang(maKH, tenKH, sdtKH, "", "");

        PhuongTien pt = (bienSo != null && !bienSo.isEmpty() && !bienSo.equals("—"))
                ? new PhuongTien(bienSo, "", "", "VIN_" + bienSo.replaceAll("[^A-Za-z0-9]", ""), "", "", "2022", "0", this.khachHang, ngayLap)
                : null;

        if (soPhieuSC != null && !soPhieuSC.isEmpty() && !soPhieuSC.equals("—")) {
            PhieuSuaChua psc = new PhieuSuaChua(soPhieuSC);
            psc.setKhachHang(this.khachHang);
            psc.setPhuongTien(pt);
            this.phieuSuaChua = psc;
        }

        if (nguoiThu != null && !nguoiThu.isEmpty()) {
            NhanVien nv = new NhanVien();
            nv.setHoTen(nguoiThu);
            this.nhanVien = nv;
        }

        this.ngayLap = ngayLap;
        this.tienDichVu = tienDichVu;
        this.tienPhuTung = tienPhuTung;
        this.giamGiaPhanTram = giamGiaPhanTram;
        this.tongTien = tongTien;
        this.phuongThuc = phuongThuc;
        this.ghiChu = ghiChu;
        this.trangThai = "DA_THANH_TOAN".equalsIgnoreCase(trangThai) ? DA_THANH_TOAN
                : "DA_HUY".equalsIgnoreCase(trangThai) ? DA_HUY : trangThai;
    }

    public HoaDon(String maHD, String soPhieuSC, String maKH, String tenKH, String sdtKH,
                  String bienSo, String ngayLapStr, long tienDichVu, long tienPhuTung,
                  double giamGiaPhanTram, long tongTien, String phuongThuc, String nguoiThu,
                  String ghiChu, String trangThai) {
        this(maHD, soPhieuSC, maKH, tenKH, sdtKH, bienSo, parseLocalDate(ngayLapStr),
                tienDichVu, tienPhuTung, giamGiaPhanTram, tongTien, phuongThuc, nguoiThu, ghiChu, trangThai);
    }

    private static LocalDate parseLocalDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        s = s.trim();
        try {
            if (s.contains("/")) {
                String[] p = s.split("/");
                if (p.length == 3) {
                    return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                }
            } else if (s.contains("-")) {
                if (s.length() >= 10) {
                    return LocalDate.parse(s.substring(0, 10));
                }
            }
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public PhieuSuaChua getPhieuSuaChua() { return phieuSuaChua; }
    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) { this.phieuSuaChua = phieuSuaChua; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    // ==== CÁC HÀM TRUY XUẤT ĐỐI TƯỢNG (DELEGATE) AN TOÀN ====
    public String getMaKH() { return khachHang != null ? khachHang.getMaKH() : ""; }
    public String getTenKH() { return khachHang != null ? khachHang.getHoTen() : ""; }
    public String getSdtKH() { return khachHang != null ? khachHang.getSdt() : ""; }

    public String getBienSo() {
        if (phieuSuaChua != null && phieuSuaChua.getPhuongTien() != null) {
            return phieuSuaChua.getPhuongTien().getBienSo();
        }
        return "—";
    }

    public String getSoPhieuSC() { return phieuSuaChua != null ? phieuSuaChua.getMaPSC() : "—"; }
    public String getNguoiThu() { return nhanVien != null ? nhanVien.getHoTen() : ""; }
    public String getMaNV() { return nhanVien != null ? nhanVien.getMaNV() : ""; }

    public LocalDate getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDate ngayLap) { this.ngayLap = ngayLap; }

    public LocalDate getNgayThanhToan() { return ngayLap; }
    public void setNgayThanhToan(LocalDate ngayLap) { this.ngayLap = ngayLap; }
    public void setNgayThanhToan(String ngayThanhToanStr) { this.ngayLap = parseLocalDate(ngayThanhToanStr); }

    public String getNgayLapHienThi() {
        return ngayLap != null ? ngayLap.format(DATE_FORMATTER) : "";
    }

    public long getTienDichVu() { return tienDichVu; }
    public void setTienDichVu(long tienDichVu) { this.tienDichVu = tienDichVu; }

    public long getTienPhuTung() { return tienPhuTung; }
    public void setTienPhuTung(long tienPhuTung) { this.tienPhuTung = tienPhuTung; }

    public double getGiamGiaPhanTram() { return giamGiaPhanTram; }
    public void setGiamGiaPhanTram(double giamGiaPhanTram) { this.giamGiaPhanTram = giamGiaPhanTram; }

    public long getTongTien() { return tongTien; }
    public void setTongTien(long tongTien) { this.tongTien = tongTien; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) {
        this.trangThai = "DA_THANH_TOAN".equalsIgnoreCase(trangThai) ? DA_THANH_TOAN
                : "DA_HUY".equalsIgnoreCase(trangThai) ? DA_HUY : trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoaDon hoaDon = (HoaDon) o;
        return Objects.equals(maHD, hoaDon.maHD);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maHD);
    }

    @Override
    public String toString() {
        return (maHD != null ? maHD : "") + (khachHang != null ? (" - " + khachHang.getHoTen()) : "");
    }
}
