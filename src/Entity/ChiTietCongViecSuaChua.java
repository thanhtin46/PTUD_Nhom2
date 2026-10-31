package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Chi tiết công việc / tiền công trong Phiếu sửa chữa.
 */
public class ChiTietCongViecSuaChua {
    private PhieuSuaChua phieuSuaChua;
    private String tenCongViec;
    private long tienCong;
    private String trangThai;

    public ChiTietCongViecSuaChua() {
    }

    public ChiTietCongViecSuaChua(PhieuSuaChua phieuSuaChua, String tenCongViec,
                                  long tienCong, String trangThai) {
        this.phieuSuaChua = phieuSuaChua;
        this.tenCongViec = tenCongViec;
        this.tienCong = tienCong;
        this.trangThai = trangThai;
    }

    public ChiTietCongViecSuaChua(String maPSC, String tenCongViec,
                                  long tienCong, String trangThai) {
        this(maPSC != null && !maPSC.isEmpty() ? new PhieuSuaChua(maPSC) : null,
             tenCongViec, tienCong, trangThai);
    }

    public PhieuSuaChua getPhieuSuaChua() {
        return phieuSuaChua;
    }

    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) {
        this.phieuSuaChua = phieuSuaChua;
    }

    public String getMaPSC() {
        return phieuSuaChua != null ? phieuSuaChua.getMaPSC() : "";
    }

    public void setMaPSC(String maPSC) {
        if (this.phieuSuaChua == null) this.phieuSuaChua = new PhieuSuaChua(maPSC);
        else this.phieuSuaChua.setMaPSC(maPSC);
    }

    public String getTenCongViec() {
        return tenCongViec;
    }

    public void setTenCongViec(String tenCongViec) {
        this.tenCongViec = tenCongViec;
    }

    public long getTienCong() {
        return tienCong;
    }

    public void setTienCong(long tienCong) {
        this.tienCong = tienCong;
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
        ChiTietCongViecSuaChua that = (ChiTietCongViecSuaChua) o;
        return Objects.equals(phieuSuaChua, that.phieuSuaChua) &&
               Objects.equals(tenCongViec, that.tenCongViec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phieuSuaChua, tenCongViec);
    }

    @Override
    public String toString() {
        return (tenCongViec != null ? tenCongViec : "") + " (" + tienCong + " đ)";
    }
}
