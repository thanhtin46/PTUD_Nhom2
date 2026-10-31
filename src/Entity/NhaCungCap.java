package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Nhà cung cấp phụ tùng và vật tư gara.
 */
public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String soDT;
    private String diaChi;
    private String email;

    public NhaCungCap() {
    }

    public NhaCungCap(String maNCC) {
        this.maNCC = maNCC;
    }

    public NhaCungCap(String maNCC, String tenNCC, String soDT, String diaChi, String email) {
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.soDT = soDT;
        this.diaChi = diaChi;
        this.email = email;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }

    public String getSoDT() {
        return soDT;
    }

    public void setSoDT(String soDT) {
        this.soDT = soDT;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhaCungCap that = (NhaCungCap) o;
        return Objects.equals(maNCC, that.maNCC);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maNCC);
    }

    @Override
    public String toString() {
        if (tenNCC != null && !tenNCC.trim().isEmpty()) {
            return (maNCC != null && !maNCC.trim().isEmpty()) ? (maNCC + " - " + tenNCC) : tenNCC;
        }
        return maNCC != null ? maNCC : "";
    }
}
