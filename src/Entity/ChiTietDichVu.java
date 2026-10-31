package Entity;

/**
 * Lớp lưu thông tin chi tiết công việc hoặc phụ tùng trong phiếu sửa chữa.
 */
public class ChiTietDichVu {
    private String loai;        // "Tiền công" hoặc "Phụ tùng"
    private String maPT;        // Mã phụ tùng (nếu là phụ tùng), ví dụ: "PT001"
    private String tenHangMuc;  // Tên công việc hoặc tên phụ tùng
    private String donVi;       // "Lần", "Can", "Bộ", "Cái", "Lít", ...
    private int soLuong;
    private long donGia;
    private long thanhTien;

    public ChiTietDichVu(String loai, String maPT, String tenHangMuc, String donVi, int soLuong, long donGia) {
        this.loai = loai != null ? loai : "Phụ tùng";
        this.maPT = maPT != null ? maPT : "";
        this.tenHangMuc = tenHangMuc != null ? tenHangMuc : "";
        this.donVi = donVi != null && !donVi.trim().isEmpty() ? donVi : ("Tiền công".equalsIgnoreCase(this.loai) ? "Lần" : "Cái");
        this.soLuong = Math.max(1, soLuong);
        this.donGia = donGia;
        this.thanhTien = (long) this.soLuong * donGia;
    }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getMaPT() { return maPT; }
    public void setMaPT(String maPT) { this.maPT = maPT; }

    public String getTenHangMuc() { return tenHangMuc; }
    public void setTenHangMuc(String tenHangMuc) { this.tenHangMuc = tenHangMuc; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        this.thanhTien = (long) this.soLuong * this.donGia;
    }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) {
        this.donGia = donGia;
        this.thanhTien = (long) this.soLuong * this.donGia;
    }

    public long getThanhTien() { return thanhTien; }
}
