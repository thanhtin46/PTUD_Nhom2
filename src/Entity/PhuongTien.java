package Entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Lớp thực thể Phương tiện (Xe ô tô).
 */
public class PhuongTien {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String bienSo;
    private String hangXe;
    private String dongXe;
    private String soKhung;  // tương ứng soVIN trong CSDL
    private String soMay;
    private String mauSon;
    private String namSX;
    private String odo;
    private KhachHang khachHang;
    private LocalDate ngayTiepNhanGanNhat;

    public PhuongTien() {
    }

    public PhuongTien(String bienSo) {
        this.bienSo = bienSo;
    }

    public PhuongTien(String bienSo, String hangXe, String dongXe, String soKhung,
                      String soMay, String mauSon, String namSX, String odo,
                      KhachHang khachHang, LocalDate ngayTiepNhanGanNhat) {
        this.bienSo = bienSo;
        this.hangXe = hangXe;
        this.dongXe = dongXe;
        this.soKhung = soKhung;
        this.soMay = soMay;
        this.mauSon = mauSon;
        this.namSX = namSX;
        this.odo = odo;
        this.khachHang = khachHang;
        this.ngayTiepNhanGanNhat = ngayTiepNhanGanNhat;
    }

    public PhuongTien(String bienSo, String hangXe, String dongXe, String soKhung,
                      String soMay, String mauSon, String namSX, String odo,
                      String maChuXe, LocalDate ngayTiepNhanGanNhat) {
        this(bienSo, hangXe, dongXe, soKhung, soMay, mauSon, namSX, odo,
                maChuXe != null && !maChuXe.isEmpty() ? new KhachHang(maChuXe) : null,
                ngayTiepNhanGanNhat);
    }

    public PhuongTien(String bienSo, String hangXe, String dongXe, String soKhung,
                      String soMay, String mauSon, String namSX, String odo,
                      String maChuXe, String ngayTiepNhanGanNhatStr) {
        this(bienSo, hangXe, dongXe, soKhung, soMay, mauSon, namSX, odo,
                maChuXe != null && !maChuXe.isEmpty() ? new KhachHang(maChuXe) : null,
                parseLocalDate(ngayTiepNhanGanNhatStr));
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

    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }

    public String getBienSoXe() { return bienSo; }
    public void setBienSoXe(String bienSoXe) { this.bienSo = bienSoXe; }

    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }

    public String getDongXe() { return dongXe; }
    public void setDongXe(String dongXe) { this.dongXe = dongXe; }

    public String getSoKhung() { return soKhung; }
    public void setSoKhung(String soKhung) { this.soKhung = soKhung; }

    public String getSoVIN() { return soKhung; }
    public void setSoVIN(String soVIN) { this.soKhung = soVIN; }

    public String getSoMay() { return soMay; }
    public void setSoMay(String soMay) { this.soMay = soMay; }

    public String getMauSon() { return mauSon; }
    public void setMauSon(String mauSon) { this.mauSon = mauSon; }

    public String getNamSX() { return namSX; }
    public void setNamSX(String namSX) { this.namSX = namSX; }

    public String getOdo() { return odo; }
    public void setOdo(String odo) { this.odo = odo; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public String getMaChuXe() {
        return khachHang != null ? khachHang.getMaKH() : "";
    }

    public void setMaChuXe(String maChuXe) {
        if (this.khachHang == null) {
            this.khachHang = new KhachHang(maChuXe);
        } else {
            this.khachHang.setMaKH(maChuXe);
        }
    }

    public String getTenChuXe() {
        return khachHang != null ? khachHang.getHoTen() : "";
    }

    public LocalDate getNgayTiepNhanGanNhat() { return ngayTiepNhanGanNhat; }
    public void setNgayTiepNhanGanNhat(LocalDate ngayTiepNhanGanNhat) { this.ngayTiepNhanGanNhat = ngayTiepNhanGanNhat; }

    public void setNgayTiepNhanGanNhat(String ngayTiepNhanGanNhatStr) {
        this.ngayTiepNhanGanNhat = parseLocalDate(ngayTiepNhanGanNhatStr);
    }

    public String getNgayTiepNhanGanNhatHienThi() {
        return ngayTiepNhanGanNhat != null ? ngayTiepNhanGanNhat.format(DATE_FORMATTER) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhuongTien that = (PhuongTien) o;
        if (bienSo != null && that.bienSo != null) {
            return bienSo.equalsIgnoreCase(that.bienSo);
        }
        return Objects.equals(soKhung, that.soKhung);
    }

    @Override
    public int hashCode() {
        return bienSo != null ? bienSo.toUpperCase().hashCode() : (soKhung != null ? soKhung.hashCode() : 0);
    }

    @Override
    public String toString() {
        if (bienSo != null && !bienSo.trim().isEmpty()) {
            return bienSo + (hangXe != null && !hangXe.trim().isEmpty() ? (" - " + hangXe + " " + (dongXe != null ? dongXe : "")) : "");
        }
        return soKhung != null ? soKhung : "";
    }
}
