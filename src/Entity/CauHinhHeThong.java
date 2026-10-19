package Entity;

import java.io.Serializable;

/**
 * Cấu hình & Tham số vận hành toàn hệ thống Gara (System Parameters).
 * Quản trị viên (Admin) có thể tùy chỉnh các quy định kinh doanh mà không phải là CRUD danh mục.
 */
public class CauHinhHeThong implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tenGara = "Gara Ô tô The T4 Auto";
    private String diaChi = "12 Nguyễn Văn Bảo, Phường 4, Quận Gò Vấp, TP.HCM";
    private String hotline = "0901.234.567";
    private String stkNganHang = "1028.6868.9999 - VietinBank Chi nhánh Gia Định";
    private int thueVAT = 10;                     // % Thuế VAT mặc định
    private int chietKhauToiDa = 15;              // % Chiết khấu tối đa thu ngân được giảm
    private int thoiHanBaoHanhThang = 6;          // Thời hạn bảo hành dịch vụ mặc định (tháng)
    private int tonKhoCanhBaoMin = 5;             // Mức tồn kho tối thiểu để kích hoạt cảnh báo
    private int gioiHanXeTrongNgay = 20;          // Số lượng xe tiếp nhận tối đa trong ngày
    private String loiCamOnHoaDon = "Cảm ơn Quý khách đã tin tưởng và sử dụng dịch vụ tại The T4 Auto!";
    private boolean batCanhBaoTuDong = true;      // Tự động quét và cảnh báo sự cố vận hành
    private boolean yeuCauXacNhanHuy = true;      // Bắt buộc xác nhận của Admin khi hủy hóa đơn

    public CauHinhHeThong() {}

    // Getters và Setters
    public String getTenGara() { return tenGara; }
    public void setTenGara(String tenGara) { this.tenGara = tenGara; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getHotline() { return hotline; }
    public void setHotline(String hotline) { this.hotline = hotline; }

    public String getStkNganHang() { return stkNganHang; }
    public void setStkNganHang(String stkNganHang) { this.stkNganHang = stkNganHang; }

    public int getThueVAT() { return thueVAT; }
    public void setThueVAT(int thueVAT) { this.thueVAT = thueVAT; }

    public int getChietKhauToiDa() { return chietKhauToiDa; }
    public void setChietKhauToiDa(int chietKhauToiDa) { this.chietKhauToiDa = chietKhauToiDa; }

    public int getThoiHanBaoHanhThang() { return thoiHanBaoHanhThang; }
    public void setThoiHanBaoHanhThang(int thoiHanBaoHanhThang) { this.thoiHanBaoHanhThang = thoiHanBaoHanhThang; }

    public int getTonKhoCanhBaoMin() { return tonKhoCanhBaoMin; }
    public void setTonKhoCanhBaoMin(int tonKhoCanhBaoMin) { this.tonKhoCanhBaoMin = tonKhoCanhBaoMin; }

    public int getGioiHanXeTrongNgay() { return gioiHanXeTrongNgay; }
    public void setGioiHanXeTrongNgay(int gioiHanXeTrongNgay) { this.gioiHanXeTrongNgay = gioiHanXeTrongNgay; }

    public String getLoiCamOnHoaDon() { return loiCamOnHoaDon; }
    public void setLoiCamOnHoaDon(String loiCamOnHoaDon) { this.loiCamOnHoaDon = loiCamOnHoaDon; }

    public boolean isBatCanhBaoTuDong() { return batCanhBaoTuDong; }
    public void setBatCanhBaoTuDong(boolean batCanhBaoTuDong) { this.batCanhBaoTuDong = batCanhBaoTuDong; }

    public boolean isYeuCauXacNhanHuy() { return yeuCauXacNhanHuy; }
    public void setYeuCauXacNhanHuy(boolean yeuCauXacNhanHuy) { this.yeuCauXacNhanHuy = yeuCauXacNhanHuy; }

    /** Khôi phục cấu hình về giá trị mặc định của gara */
    public void resetMacDinh() {
        this.tenGara = "Gara Ô tô The T4 Auto";
        this.diaChi = "12 Nguyễn Văn Bảo, Phường 4, Quận Gò Vấp, TP.HCM";
        this.hotline = "0901.234.567";
        this.stkNganHang = "1028.6868.9999 - VietinBank Chi nhánh Gia Định";
        this.thueVAT = 10;
        this.chietKhauToiDa = 15;
        this.thoiHanBaoHanhThang = 6;
        this.tonKhoCanhBaoMin = 5;
        this.gioiHanXeTrongNgay = 20;
        this.loiCamOnHoaDon = "Cảm ơn Quý khách đã tin tưởng và sử dụng dịch vụ tại The T4 Auto!";
        this.batCanhBaoTuDong = true;
        this.yeuCauXacNhanHuy = true;
    }
}
