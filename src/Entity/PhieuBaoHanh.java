package Entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Thực thể Phiếu bảo hành phương tiện / linh kiện.
 */
public class PhieuBaoHanh {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String soPhieu;
    private KhachHang khachHang;
    private PhuongTien phuongTien;
    private HoaDon hoaDon;
    private NhanVien nhanVien;
    private LocalDate ngayLap;
    private LocalDate ngayBD;
    private LocalDate ngayKT;
    private String tinhTrang;
    private String noiDung;
    private String noiDungSuaChua = "";
    private String noiDungPhuTung = "";
    private String maHoaDonCache = "";
    private String tenKhachHangCache = "";
    private String soDienThoaiCache = "";

    public PhieuBaoHanh() {
    }

    public PhieuBaoHanh(String soPhieu) {
        this.soPhieu = soPhieu;
    }

    public PhieuBaoHanh(String soPhieu, String maHD, String bienSo, String tenKH, String soDT,
                        String nguoiLap, LocalDate ngayLap, LocalDate ngayBD, LocalDate ngayKT,
                        String tinhTrang, String dsSuaChua, String dsPhuTung, String noiDungTong) {
        this.soPhieu = soPhieu;
        this.maHoaDonCache = maHD != null ? maHD : "";
        this.hoaDon = new HoaDon(this.maHoaDonCache);
        this.phuongTien = new PhuongTien(bienSo != null ? bienSo : "");
        this.khachHang = new KhachHang();
        this.khachHang.setHoTen(tenKH != null ? tenKH : "");
        this.khachHang.setSdt(soDT != null ? soDT : "");
        this.tenKhachHangCache = tenKH != null ? tenKH : "";
        this.soDienThoaiCache = soDT != null ? soDT : "";
        this.nhanVien = new NhanVien();
        this.nhanVien.setHoTen(nguoiLap != null ? nguoiLap : "");
        this.ngayLap = ngayLap;
        this.ngayBD = ngayBD;
        this.ngayKT = ngayKT;
        this.tinhTrang = tinhTrang;
        this.noiDungSuaChua = dsSuaChua != null ? dsSuaChua : "";
        this.noiDungPhuTung = dsPhuTung != null ? dsPhuTung : "";
        this.noiDung = noiDungTong != null ? noiDungTong : "";
    }

    public PhieuBaoHanh(String soPhieu, KhachHang khachHang, PhuongTien phuongTien,
                        HoaDon hoaDon, NhanVien nhanVien, LocalDate ngayLap,
                        LocalDate ngayBD, LocalDate ngayKT, String tinhTrang, String noiDung) {
        this.soPhieu = soPhieu;
        this.khachHang = khachHang;
        this.phuongTien = phuongTien;
        this.hoaDon = hoaDon;
        this.nhanVien = nhanVien;
        this.ngayLap = ngayLap;
        this.ngayBD = ngayBD;
        this.ngayKT = ngayKT;
        this.tinhTrang = tinhTrang;
        this.noiDung = noiDung;
    }

    public PhieuBaoHanh(String soPhieu, String bienSo, LocalDate ngayBD, LocalDate ngayKT,
                        String nguoiLap, String tinhTrang, String noiDung) {
        this.soPhieu = soPhieu;
        this.phuongTien = new PhuongTien(bienSo);
        NhanVien nv = new NhanVien();
        nv.setHoTen(nguoiLap);
        this.nhanVien = nv;
        this.ngayBD = ngayBD;
        this.ngayKT = ngayKT;
        this.ngayLap = ngayBD;
        this.tinhTrang = tinhTrang;
        this.noiDung = noiDung;
    }

    public PhieuBaoHanh(String soPhieu, String bienSo, String ngayBDStr, String ngayKTStr,
                        String nguoiLap, String tinhTrang, String noiDung) {
        this(soPhieu, bienSo, parseLocalDate(ngayBDStr), parseLocalDate(ngayKTStr),
                nguoiLap, tinhTrang, noiDung);
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

    public String getSoPhieu() { return soPhieu; }
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public String getMaPBH() { return soPhieu; }
    public void setMaPBH(String maPBH) { this.soPhieu = maPBH; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public PhuongTien getPhuongTien() { return phuongTien; }
    public void setPhuongTien(PhuongTien phuongTien) { this.phuongTien = phuongTien; }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public String getBienSo() {
        return phuongTien != null ? phuongTien.getBienSo() : "";
    }

    public void setBienSo(String bienSo) {
        if (this.phuongTien == null) this.phuongTien = new PhuongTien(bienSo);
        else this.phuongTien.setBienSo(bienSo);
    }

    public String getNguoiLap() {
        return nhanVien != null ? nhanVien.getHoTen() : "";
    }

    public void setNguoiLap(String nguoiLap) {
        if (this.nhanVien == null) this.nhanVien = new NhanVien();
        this.nhanVien.setHoTen(nguoiLap);
    }

    public String getMaKH() { return khachHang != null ? khachHang.getMaKH() : ""; }
    public String getSoVIN() { return phuongTien != null ? phuongTien.getSoKhung() : ""; }
    public String getMaHoaDon() {
        if (maHoaDonCache != null && !maHoaDonCache.isEmpty()) return maHoaDonCache;
        return hoaDon != null ? hoaDon.getMaHD() : "";
    }
    public void setMaHoaDon(String maHD) {
        this.maHoaDonCache = maHD != null ? maHD : "";
        if (this.hoaDon == null) this.hoaDon = new HoaDon(this.maHoaDonCache);
        else this.hoaDon.setMaHD(this.maHoaDonCache);
    }

    public String getTenKhachHang() {
        if (tenKhachHangCache != null && !tenKhachHangCache.isEmpty()) return tenKhachHangCache;
        return khachHang != null ? khachHang.getHoTen() : "";
    }
    public void setTenKhachHang(String ten) {
        this.tenKhachHangCache = ten != null ? ten : "";
        if (this.khachHang == null) this.khachHang = new KhachHang();
        this.khachHang.setHoTen(this.tenKhachHangCache);
    }

    public String getSoDienThoai() {
        if (soDienThoaiCache != null && !soDienThoaiCache.isEmpty()) return soDienThoaiCache;
        return khachHang != null ? khachHang.getSdt() : "";
    }
    public void setSoDienThoai(String sdt) {
        this.soDienThoaiCache = sdt != null ? sdt : "";
        if (this.khachHang == null) this.khachHang = new KhachHang();
        this.khachHang.setSdt(this.soDienThoaiCache);
    }

    public String getNoiDungSuaChua() { return noiDungSuaChua != null ? noiDungSuaChua : ""; }
    public void setNoiDungSuaChua(String s) { this.noiDungSuaChua = s != null ? s : ""; }

    public String getNoiDungPhuTung() { return noiDungPhuTung != null ? noiDungPhuTung : ""; }
    public void setNoiDungPhuTung(String s) { this.noiDungPhuTung = s != null ? s : ""; }

    public boolean coSuaChua() {
        return noiDungSuaChua != null && !noiDungSuaChua.trim().isEmpty();
    }

    public boolean coPhuTung() {
        return noiDungPhuTung != null && !noiDungPhuTung.trim().isEmpty();
    }

    public LocalDate getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDate ngayLap) { this.ngayLap = ngayLap; }

    public LocalDate getNgayBD() { return ngayBD; }
    public void setNgayBD(LocalDate ngayBD) { this.ngayBD = ngayBD; }

    public LocalDate getNgayKT() { return ngayKT; }
    public void setNgayKT(LocalDate ngayKT) { this.ngayKT = ngayKT; }

    public LocalDate getNgayBatDau() { return ngayBD; }
    public void setNgayBatDau(LocalDate ngayBD) { this.ngayBD = ngayBD; }

    public LocalDate getNgayKetThuc() { return ngayKT; }
    public void setNgayKetThuc(LocalDate ngayKT) { this.ngayKT = ngayKT; }

    public String getNgayBDHienThi() {
        return ngayBD != null ? ngayBD.format(DATE_FORMATTER) : "";
    }

    public String getNgayKTHienThi() {
        return ngayKT != null ? ngayKT.format(DATE_FORMATTER) : "";
    }

    public String getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(String tinhTrang) { this.tinhTrang = tinhTrang; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhieuBaoHanh that = (PhieuBaoHanh) o;
        return Objects.equals(soPhieu, that.soPhieu);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(soPhieu);
    }

    @Override
    public String toString() {
        return (soPhieu != null ? soPhieu : "") + (phuongTien != null ? (" - " + phuongTien.getBienSo()) : "");
    }
}
