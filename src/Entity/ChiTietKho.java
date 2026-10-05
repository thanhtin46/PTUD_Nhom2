package Entity;

public class ChiTietKho {
    private String maPT;
    private String tenPT;
    private int soLuong;
    private long donGia;

    public ChiTietKho(String maPT, String tenPT, int soLuong, long donGia) {
        this.maPT = maPT;
        this.tenPT = tenPT;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String getMaPT() { return maPT; }
    public String getTenPT() { return tenPT; }
    public int getSoLuong() { return soLuong; }
    public long getDonGia() { return donGia; }
    public long getThanhTien() { return soLuong * donGia; }
}
