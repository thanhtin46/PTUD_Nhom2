package Entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Lớp thực thể Khách hàng.
 */
public class KhachHang {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String maKH;
    private String hoTen;
    private String sdt;
    private String cccd;
    private String diaChi;
    private String email;
    private String ghiChu;
    private LocalDate ngayDangKy;

    public KhachHang() {
    }

    public KhachHang(String maKH) {
        this.maKH = maKH;
    }

    public KhachHang(String maKH, String hoTen, String sdt, String diaChi, String email) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.email = email;
    }

    public KhachHang(String maKH, String hoTen, String sdt, String cccd,
                     String diaChi, String email, String ghiChu, LocalDate ngayDangKy) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.cccd = cccd;
        this.diaChi = diaChi;
        this.email = email;
        this.ghiChu = ghiChu;
        this.ngayDangKy = ngayDangKy;
    }

    public KhachHang(String maKH, String hoTen, String sdt, String cccd,
                     String diaChi, String email, String ghiChu, String ngayDangKyStr) {
        this(maKH, hoTen, sdt, cccd, diaChi, email, ghiChu, parseLocalDate(ngayDangKyStr));
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

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDate getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(LocalDate ngayDangKy) { this.ngayDangKy = ngayDangKy; }

    public void setNgayDangKy(String ngayDangKyStr) {
        this.ngayDangKy = parseLocalDate(ngayDangKyStr);
    }

    public String getNgayDangKyHienThi() {
        return ngayDangKy != null ? ngayDangKy.format(DATE_FORMATTER) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhachHang khachHang = (KhachHang) o;
        return Objects.equals(maKH, khachHang.maKH);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maKH);
    }

    @Override
    public String toString() {
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            return (maKH != null && !maKH.trim().isEmpty()) ? (maKH + " - " + hoTen) : hoTen;
        }
        return maKH != null ? maKH : "";
    }
}
