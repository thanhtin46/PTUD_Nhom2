package Entity;

import java.util.ArrayList;

/**
 * Thực thể Hóa đơn thanh toán dịch vụ / sửa chữa gara.
 */
public class HoaDon {
    private String maHD;
    private String soPhieuSC;       // Mã phiếu tiếp nhận/sửa chữa liên kết
    private String maKH;
    private String tenKH;
    private String sdtKH;
    private String bienSo;
    private String ngayThanhToan;
    private long tienDichVu;        // Tiền công sửa chữa/dịch vụ
    private long tienPhuTung;       // Tiền linh kiện thay thế
    private double giamGiaPhanTram; // Giảm giá % (nếu có VIP...)
    private long tongTien;          // Tổng tiền sau giảm giá
    private String phuongThuc;      // Tiền mặt, Chuyển khoản, Thẻ
    private String nguoiThu;        // Thu ngân / Nhân viên lập hóa đơn
    private String ghiChu;
    private String trangThai;       // Đã thanh toán, Đã hủy

    public static final String DA_THANH_TOAN = "Đã thanh toán";
    public static final String DA_HUY = "Đã hủy";

    public HoaDon(String maHD, String soPhieuSC, String maKH, String tenKH, String sdtKH,
                  String bienSo, String ngayThanhToan, long tienDichVu, long tienPhuTung,
                  double giamGiaPhanTram, long tongTien, String phuongThuc, String nguoiThu,
                  String ghiChu, String trangThai) {
        this.maHD = maHD;
        this.soPhieuSC = soPhieuSC;
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.sdtKH = sdtKH;
        this.bienSo = bienSo;
        this.ngayThanhToan = ngayThanhToan;
        this.tienDichVu = tienDichVu;
        this.tienPhuTung = tienPhuTung;
        this.giamGiaPhanTram = giamGiaPhanTram;
        this.tongTien = tongTien;
        this.phuongThuc = phuongThuc;
        this.nguoiThu = nguoiThu;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public String getSoPhieuSC() { return soPhieuSC; }
    public void setSoPhieuSC(String soPhieuSC) { this.soPhieuSC = soPhieuSC; }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }

    public String getSdtKH() { return sdtKH; }
    public void setSdtKH(String sdtKH) { this.sdtKH = sdtKH; }

    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }

    public String getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(String ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }

    public long getTienDichVu() { return tienDichVu; }
    public void setTienDichVu(long tienDichVu) { this.tienDichVu = tienDichVu; }

    public long getTienPhuTung() { return tienPhuTung; }
    public void setTienPhuTung(long tienPhuTung) { this.tienPhuTung = tienPhuTung; }

    public double getGiamGiaPhanTram() { return giamGiaPhanTram; }
    public void setGiamGiaPhanTram(double giamGiaPhanTram) { this.giamGiaPhanTram = giamGiaPhanTram; }

    public long getTongTien() { return tongTien; }
    public void setTongTien(long tongTien) { this.tongTien = tongTien; }

    public String getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }

    public String getNguoiThu() { return nguoiThu; }
    public void setNguoiThu(String nguoiThu) { this.nguoiThu = nguoiThu; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
