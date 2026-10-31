package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Phụ tùng.
 */
public class PhuTung {
    private String maPT;
    private String ten;
    private String donVi;
    private int soLuongTon;
    private long donGia;
    private int soLuongToiThieu;     // Mức tồn tối thiểu để cảnh báo hết hàng

    public PhuTung() {
    }

    public PhuTung(String maPT) {
        this.maPT = maPT;
    }

    public PhuTung(String maPT, String ten, String donVi, int soLuongTon, long donGia) {
        this(maPT, ten, donVi, soLuongTon, donGia, 5);   // mặc định cảnh báo khi ≤5
    }

    public PhuTung(String maPT, String ten, String donVi, int soLuongTon,
                   long donGia, int soLuongToiThieu) {
        this.maPT = maPT;
        this.ten = ten;
        this.donVi = donVi;
        this.soLuongTon = soLuongTon;
        this.donGia = donGia;
        this.soLuongToiThieu = soLuongToiThieu;
    }

    public String getMaPT() { return maPT; }
    public void setMaPT(String maPT) { this.maPT = maPT; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getTenPT() { return ten; }
    public void setTenPT(String tenPT) { this.ten = tenPT; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public String getDonViTinh() { return donVi; }
    public void setDonViTinh(String donViTinh) { this.donVi = donViTinh; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) { this.donGia = donGia; }

    public long getDonGiaBan() { return donGia; }
    public void setDonGiaBan(long donGiaBan) { this.donGia = donGiaBan; }

    public int getSoLuongToiThieu() { return soLuongToiThieu; }
    public void setSoLuongToiThieu(int soLuongToiThieu) { this.soLuongToiThieu = soLuongToiThieu; }

    // Kiểm tra mức cảnh báo tồn kho
    public String getMucCanhBao() {
        if (soLuongTon == 0) return "HẾT HÀNG";
        if (soLuongTon <= soLuongToiThieu) return "SẮP HẾT";
        return "BÌNH THƯỜNG";
    }

    public boolean canhBao() { return soLuongTon <= soLuongToiThieu; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhuTung phuTung = (PhuTung) o;
        return Objects.equals(maPT, phuTung.maPT);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPT);
    }

    @Override
    public String toString() {
        if (ten != null && !ten.trim().isEmpty()) {
            return (maPT != null && !maPT.trim().isEmpty()) ? (maPT + " - " + ten) : ten;
        }
        return maPT != null ? maPT : "";
    }
}
