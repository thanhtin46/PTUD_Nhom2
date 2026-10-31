package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Chi tiết phụ tùng xuất kho.
 */
public class ChiTietPhieuXuatKho {
    private PhieuXuatKho phieuXuatKho;
    private PhuTung phuTung;
    private int soLuongYeuCau;
    private int soLuongXuat;
    private long donGia;
    private String tinhTrangXuat;

    public ChiTietPhieuXuatKho() {
    }

    public ChiTietPhieuXuatKho(PhieuXuatKho phieuXuatKho, PhuTung phuTung, int soLuongYeuCau,
                               int soLuongXuat, long donGia, String tinhTrangXuat) {
        this.phieuXuatKho = phieuXuatKho;
        this.phuTung = phuTung;
        this.soLuongYeuCau = soLuongYeuCau;
        this.soLuongXuat = soLuongXuat;
        this.donGia = donGia;
        this.tinhTrangXuat = tinhTrangXuat;
    }

    public ChiTietPhieuXuatKho(String maPhieuXuat, String maPT, int soLuongYeuCau,
                               int soLuongXuat, long donGia, String tinhTrangXuat) {
        this(maPhieuXuat != null && !maPhieuXuat.isEmpty() ? new PhieuXuatKho(maPhieuXuat) : null,
             maPT != null && !maPT.isEmpty() ? new PhuTung(maPT) : null,
             soLuongYeuCau, soLuongXuat, donGia, tinhTrangXuat);
    }

    public PhieuXuatKho getPhieuXuatKho() {
        return phieuXuatKho;
    }

    public void setPhieuXuatKho(PhieuXuatKho phieuXuatKho) {
        this.phieuXuatKho = phieuXuatKho;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }

    public String getMaPhieuXuat() {
        return phieuXuatKho != null ? phieuXuatKho.getMaPhieuXuat() : "";
    }

    public void setMaPhieuXuat(String maPhieuXuat) {
        if (this.phieuXuatKho == null) this.phieuXuatKho = new PhieuXuatKho(maPhieuXuat);
        else this.phieuXuatKho.setMaPhieuXuat(maPhieuXuat);
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

    public int getSoLuongYeuCau() {
        return soLuongYeuCau;
    }

    public void setSoLuongYeuCau(int soLuongYeuCau) {
        this.soLuongYeuCau = soLuongYeuCau;
    }

    public int getSoLuongXuat() {
        return soLuongXuat;
    }

    public void setSoLuongXuat(int soLuongXuat) {
        this.soLuongXuat = soLuongXuat;
    }

    public long getDonGia() {
        return donGia;
    }

    public void setDonGia(long donGia) {
        this.donGia = donGia;
    }

    public String getTinhTrangXuat() {
        return tinhTrangXuat;
    }

    public void setTinhTrangXuat(String tinhTrangXuat) {
        this.tinhTrangXuat = tinhTrangXuat;
    }

    public long getThanhTien() {
        return (long) soLuongXuat * donGia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietPhieuXuatKho that = (ChiTietPhieuXuatKho) o;
        return Objects.equals(phieuXuatKho, that.phieuXuatKho) &&
               Objects.equals(phuTung, that.phuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phieuXuatKho, phuTung);
    }

    @Override
    public String toString() {
        return getTenPT() + " (Đã xuất: " + soLuongXuat + "/" + soLuongYeuCau + ")";
    }
}
