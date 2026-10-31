package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Chi tiết phụ tùng/dịch vụ trên Hóa đơn.
 */
public class ChiTietHoaDon {
    private HoaDon hoaDon;
    private PhuTung phuTung;
    private int soLuong;
    private long donGia;
    private long thanhTien;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(HoaDon hoaDon, PhuTung phuTung, int soLuong, long donGia) {
        this.hoaDon = hoaDon;
        this.phuTung = phuTung;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = (long) soLuong * donGia;
    }

    public ChiTietHoaDon(String maHD, String maPT, int soLuong, long donGia) {
        this(maHD != null && !maHD.isEmpty() ? new HoaDon(maHD) : null,
             maPT != null && !maPT.isEmpty() ? new PhuTung(maPT) : null,
             soLuong, donGia);
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }

    public String getMaHD() {
        return hoaDon != null ? hoaDon.getMaHD() : "";
    }

    public void setMaHD(String maHD) {
        if (this.hoaDon == null) this.hoaDon = new HoaDon(maHD);
        else this.hoaDon.setMaHD(maHD);
    }

    public String getMaPT() {
        return phuTung != null ? phuTung.getMaPT() : "";
    }

    public void setMaPT(String maPT) {
        if (this.phuTung == null) this.phuTung = new PhuTung(maPT);
        else this.phuTung.setMaPT(maPT);
    }

    public String getTenPT() {
        return phuTung != null ? phuTung.getTen() : "";
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        this.thanhTien = (long) this.soLuong * this.donGia;
    }

    public long getDonGia() {
        return donGia;
    }

    public void setDonGia(long donGia) {
        this.donGia = donGia;
        this.thanhTien = (long) this.soLuong * this.donGia;
    }

    public long getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(long thanhTien) {
        this.thanhTien = thanhTien;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietHoaDon that = (ChiTietHoaDon) o;
        return Objects.equals(hoaDon, that.hoaDon) &&
               Objects.equals(phuTung, that.phuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hoaDon, phuTung);
    }

    @Override
    public String toString() {
        return (phuTung != null ? phuTung.getTen() : "") + " (x" + soLuong + " = " + thanhTien + " đ)";
    }
}
