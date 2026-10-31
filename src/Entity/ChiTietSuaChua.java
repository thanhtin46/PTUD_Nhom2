package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Chi tiết phụ tùng/vật tư trong Phiếu sửa chữa.
 */
public class ChiTietSuaChua {
    private PhieuSuaChua phieuSuaChua;
    private PhuTung phuTung;
    private String noiDungSC;
    private int soLuong;
    private long donGia;
    private String trangThai;

    public ChiTietSuaChua() {
    }

    public ChiTietSuaChua(PhieuSuaChua phieuSuaChua, PhuTung phuTung, String noiDungSC,
                          int soLuong, long donGia, String trangThai) {
        this.phieuSuaChua = phieuSuaChua;
        this.phuTung = phuTung;
        this.noiDungSC = noiDungSC;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }

    public ChiTietSuaChua(String maPSC, String maPT, String noiDungSC,
                          int soLuong, long donGia, String trangThai) {
        this(maPSC != null && !maPSC.isEmpty() ? new PhieuSuaChua(maPSC) : null,
             maPT != null && !maPT.isEmpty() ? new PhuTung(maPT) : null,
             noiDungSC, soLuong, donGia, trangThai);
    }

    public PhieuSuaChua getPhieuSuaChua() {
        return phieuSuaChua;
    }

    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) {
        this.phieuSuaChua = phieuSuaChua;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }

    public String getMaPSC() {
        return phieuSuaChua != null ? phieuSuaChua.getMaPSC() : "";
    }

    public void setMaPSC(String maPSC) {
        if (this.phieuSuaChua == null) this.phieuSuaChua = new PhieuSuaChua(maPSC);
        else this.phieuSuaChua.setMaPSC(maPSC);
    }

    public String getMaPT() {
        return phuTung != null ? phuTung.getMaPT() : "";
    }

    public void setMaPT(String maPT) {
        if (this.phuTung == null) this.phuTung = new PhuTung(maPT);
        else this.phuTung.setMaPT(maPT);
    }

    public String getTenPhuTung() {
        return phuTung != null ? phuTung.getTen() : "";
    }

    public String getNoiDungSC() {
        return noiDungSC;
    }

    public void setNoiDungSC(String noiDungSC) {
        this.noiDungSC = noiDungSC;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietSuaChua that = (ChiTietSuaChua) o;
        return Objects.equals(phieuSuaChua, that.phieuSuaChua) &&
               Objects.equals(phuTung, that.phuTung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phieuSuaChua, phuTung);
    }

    @Override
    public String toString() {
        return (phuTung != null ? phuTung.getTen() : "") + " (x" + soLuong + ")";
    }
}
