package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Chi tiết phụ tùng nhập kho.
 */
public class ChiTietPhieuNhapKho {
    private PhieuNhapKho phieuNhapKho;
    private PhuTung phuTung;
    private String tenPT;
    private int soLuong;
    private long donGia;

    public ChiTietPhieuNhapKho() {
    }

    public ChiTietPhieuNhapKho(PhieuNhapKho phieuNhapKho, PhuTung phuTung, String tenPT, int soLuong, long donGia) {
        this.phieuNhapKho = phieuNhapKho;
        this.phuTung = phuTung;
        this.tenPT = (tenPT != null && !tenPT.isEmpty()) ? tenPT : (phuTung != null ? phuTung.getTen() : "");
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public ChiTietPhieuNhapKho(String maPhieuNhap, String maPT, String tenPT, int soLuong, long donGia) {
        this(maPhieuNhap != null && !maPhieuNhap.isEmpty() ? new PhieuNhapKho(maPhieuNhap) : null,
             maPT != null && !maPT.isEmpty() ? new PhuTung(maPT) : null,
             tenPT, soLuong, donGia);
    }

    public PhieuNhapKho getPhieuNhapKho() {
        return phieuNhapKho;
    }

    public void setPhieuNhapKho(PhieuNhapKho phieuNhapKho) {
        this.phieuNhapKho = phieuNhapKho;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }

    public String getMaPhieuNhap() {
        return phieuNhapKho != null ? phieuNhapKho.getMaPhieuNhap() : "";
    }

    public void setMaPhieuNhap(String maPhieuNhap) {
        if (this.phieuNhapKho == null) this.phieuNhapKho = new PhieuNhapKho(maPhieuNhap);
        else this.phieuNhapKho.setMaPhieuNhap(maPhieuNhap);
    }

    public String getMaPT() {
        return phuTung != null ? phuTung.getMaPT() : "";
    }

    public void setMaPT(String maPT) {
        if (this.phuTung == null) this.phuTung = new PhuTung(maPT);
        else this.phuTung.setMaPT(maPT);
    }

    public String getTenPT() {
        return tenPT != null && !tenPT.isEmpty() ? tenPT : (phuTung != null ? phuTung.getTen() : "");
    }

    public void setTenPT(String tenPT) {
        this.tenPT = tenPT;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public long getDonGia() {
        return donGia;
    }

    public void setDonGia(long donGia) {
        this.donGia = donGia;
    }

    public long getThanhTien() {
        return (long) soLuong * donGia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietPhieuNhapKho that = (ChiTietPhieuNhapKho) o;
        return Objects.equals(phieuNhapKho, that.phieuNhapKho) &&
               Objects.equals(phuTung, that.phuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phieuNhapKho, phuTung);
    }

    @Override
    public String toString() {
        return getTenPT() + " (x" + soLuong + " = " + getThanhTien() + " đ)";
    }
}
