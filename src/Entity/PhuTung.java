package Entity;

/**
 * Phụ tùng trong kho.
 * UC009: thêm mức tồn tối thiểu để cảnh báo khi tồn thấp / hết hàng.
 */
public class PhuTung {
    private String maPT;
    private String ten;
    private String donVi;
    private int soLuongTon;
    private long donGia;
    private int soLuongToiThieu;     // mức cảnh báo real-time (UC009)

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

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) { this.donGia = donGia; }

    public int getSoLuongToiThieu() { return soLuongToiThieu; }
    public void setSoLuongToiThieu(int soLuongToiThieu) { this.soLuongToiThieu = soLuongToiThieu; }

    /** Cảnh báo theo 3 mức: HET, SAP_HET (<=min), sap_hoac_binh_thuong */
    public String getMucCanhBao() {
        if (soLuongTon == 0) return "HẾT HÀNG";
        if (soLuongTon <= soLuongToiThieu) return "SẮP HẾT";
        return "BÌNH THƯỜNG";
    }
    public boolean canhBao() { return soLuongTon <= soLuongToiThieu; }
}
