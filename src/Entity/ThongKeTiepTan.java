package Entity;

import java.io.Serializable;

/**
 * Lớp chứa số liệu thống kê cho trang chủ bộ phận tiếp tân.
 */
public class ThongKeTiepTan implements Serializable {
    private static final long serialVersionUID = 1L;

    private int tongPhieuTiepNhan;
    private int phieuHomNay;
    private int xeChoTiepNhan;
    private int xeDangSuaChua;
    private int xeHoanThanh;
    private int tongKhachHang;
    private int khachHangMoiHomNay;
    private int tongPhuongTien;
    private int tongPhieuBaoHanh;
    private int baoHanhConHan;
    private int hoaDonHomNay;
    private long doanhThuHomNay;

    public ThongKeTiepTan() {
    }

    public ThongKeTiepTan(int tongPhieuTiepNhan, int phieuHomNay, int xeChoTiepNhan,
                         int xeDangSuaChua, int xeHoanThanh, int tongKhachHang,
                         int khachHangMoiHomNay, int tongPhuongTien, int tongPhieuBaoHanh,
                         int baoHanhConHan, int hoaDonHomNay, long doanhThuHomNay) {
        this.tongPhieuTiepNhan = tongPhieuTiepNhan;
        this.phieuHomNay = phieuHomNay;
        this.xeChoTiepNhan = xeChoTiepNhan;
        this.xeDangSuaChua = xeDangSuaChua;
        this.xeHoanThanh = xeHoanThanh;
        this.tongKhachHang = tongKhachHang;
        this.khachHangMoiHomNay = khachHangMoiHomNay;
        this.tongPhuongTien = tongPhuongTien;
        this.tongPhieuBaoHanh = tongPhieuBaoHanh;
        this.baoHanhConHan = baoHanhConHan;
        this.hoaDonHomNay = hoaDonHomNay;
        this.doanhThuHomNay = doanhThuHomNay;
    }

    public int getTongPhieuTiepNhan() { return tongPhieuTiepNhan; }
    public void setTongPhieuTiepNhan(int tongPhieuTiepNhan) { this.tongPhieuTiepNhan = tongPhieuTiepNhan; }

    public int getPhieuHomNay() { return phieuHomNay; }
    public void setPhieuHomNay(int phieuHomNay) { this.phieuHomNay = phieuHomNay; }

    public int getXeChoTiepNhan() { return xeChoTiepNhan; }
    public void setXeChoTiepNhan(int xeChoTiepNhan) { this.xeChoTiepNhan = xeChoTiepNhan; }

    public int getXeDangSuaChua() { return xeDangSuaChua; }
    public void setXeDangSuaChua(int xeDangSuaChua) { this.xeDangSuaChua = xeDangSuaChua; }

    public int getXeHoanThanh() { return xeHoanThanh; }
    public void setXeHoanThanh(int xeHoanThanh) { this.xeHoanThanh = xeHoanThanh; }

    public int getTongKhachHang() { return tongKhachHang; }
    public void setTongKhachHang(int tongKhachHang) { this.tongKhachHang = tongKhachHang; }

    public int getKhachHangMoiHomNay() { return khachHangMoiHomNay; }
    public void setKhachHangMoiHomNay(int khachHangMoiHomNay) { this.khachHangMoiHomNay = khachHangMoiHomNay; }

    public int getTongPhuongTien() { return tongPhuongTien; }
    public void setTongPhuongTien(int tongPhuongTien) { this.tongPhuongTien = tongPhuongTien; }

    public int getTongPhieuBaoHanh() { return tongPhieuBaoHanh; }
    public void setTongPhieuBaoHanh(int tongPhieuBaoHanh) { this.tongPhieuBaoHanh = tongPhieuBaoHanh; }

    public int getBaoHanhConHan() { return baoHanhConHan; }
    public void setBaoHanhConHan(int baoHanhConHan) { this.baoHanhConHan = baoHanhConHan; }

    public int getHoaDonHomNay() { return hoaDonHomNay; }
    public void setHoaDonHomNay(int hoaDonHomNay) { this.hoaDonHomNay = hoaDonHomNay; }

    public long getDoanhThuHomNay() { return doanhThuHomNay; }
    public void setDoanhThuHomNay(long doanhThuHomNay) { this.doanhThuHomNay = doanhThuHomNay; }
}
