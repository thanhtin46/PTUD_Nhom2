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
    private String ghiChuPhu;   // Phụ đề hiển thị chi tiết (Mã PT, KTV, bảo hành, thời gian...)

    public ChiTietDichVu(String loai, String maPT, String tenHangMuc, String donVi, int soLuong, long donGia) {
        this(loai, maPT, tenHangMuc, donVi, soLuong, donGia, null);
    }

    public ChiTietDichVu(String loai, String maPT, String tenHangMuc, String donVi, int soLuong, long donGia, String ghiChuPhu) {
        this.loai = loai != null ? loai : "Phụ tùng";
        this.maPT = maPT != null ? maPT : "";
        this.tenHangMuc = tenHangMuc != null ? tenHangMuc : "";
        this.donVi = donVi != null && !donVi.trim().isEmpty() ? donVi : ("Tiền công".equalsIgnoreCase(this.loai) ? "Công" : "Cái");
        this.ghiChuPhu = ghiChuPhu;
        setSoLuong(soLuong);
        setDonGia(donGia);
    }

    public String getGhiChuPhu() {
        if (ghiChuPhu != null && !ghiChuPhu.isBlank()) return ghiChuPhu;
        if ("Tiền công".equalsIgnoreCase(loai)) {
            return "KTV: Hoàng Đình Kha • Thời gian: 45 phút";
        } else {
            String code = (maPT != null && !maPT.isBlank()) ? maPT : "PT-KP-300ML";
            return "Mã PT: " + code + " • Bảo hành: 3 tháng";
        }
    }

    public void setGhiChuPhu(String ghiChuPhu) { this.ghiChuPhu = ghiChuPhu; }

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
        if (soLuong <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        Math.multiplyExact((long) soLuong, donGia);
        this.soLuong = soLuong;
    }

    public long getDonGia() { return donGia; }
    public void setDonGia(long donGia) {
        if (donGia < 0) throw new IllegalArgumentException("Đơn giá không được âm.");
        Math.multiplyExact((long) soLuong, donGia);
        this.donGia = donGia;
    }

    public long getThanhTien() { return Math.multiplyExact((long) soLuong, donGia); }
}
