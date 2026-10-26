package Entity;

import java.util.ArrayList;

/**
 * Phiếu kho (nhập hoặc xuất).
 * UC010: trangThai = CHO_NHAP (đã đặt NCC, chưa giao) | DA_NHAP (đã nhập kho thật) | DA_HUY.
 */
public class PhieuKho {
    public static final String NHAP = "NHAP";
    public static final String XUAT = "XUAT";

    public static final String CHO_NHAP = "CHO_NHAP";
    public static final String DA_NHAP  = "DA_NHAP";
    public static final String DA_HUY   = "DA_HUY";

    private String soPhieu;
    private String loai;
    private String ngay;
    private String nguoiLap;
    private String ghiChu;
    private String nhaCungCap;       // UC010: NCC cho phiếu nhập
    private String ngayDuKienGiao;   // UC010: ngày dự kiến NCC giao
    private String trangThai;        // UC010
    private final ArrayList<ChiTietKho> chiTiet = new ArrayList<>();

    public PhieuKho(String soPhieu, String loai, String ngay, String nguoiLap, String ghiChu) {
        this(soPhieu, loai, ngay, nguoiLap, ghiChu, "", "", CHO_NHAP);
    }

    public PhieuKho(String soPhieu, String loai, String ngay, String nguoiLap,
                    String ghiChu, String nhaCungCap, String ngayDuKienGiao, String trangThai) {
        this.soPhieu = soPhieu;
        this.loai = loai;
        this.ngay = ngay;
        this.nguoiLap = nguoiLap;
        this.ghiChu = ghiChu;
        this.nhaCungCap = nhaCungCap == null ? "" : nhaCungCap;
        this.ngayDuKienGiao = ngayDuKienGiao == null ? "" : ngayDuKienGiao;
        this.trangThai = trangThai == null ? CHO_NHAP : trangThai;
    }

    public String getSoPhieu() { return soPhieu; }
    public String getLoai() { return loai; }
    public String getNgay() { return ngay; }
    public String getNguoiLap() { return nguoiLap; }
    public String getGhiChu() { return ghiChu; }
    public ArrayList<ChiTietKho> getChiTiet() { return chiTiet; }
    public String getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public String getNgayDuKienGiao() { return ngayDuKienGiao; }
    public void setNgayDuKienGiao(String ngayDuKienGiao) { this.ngayDuKienGiao = ngayDuKienGiao; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public boolean isChoNhap() { return CHO_NHAP.equals(trangThai); }
    public boolean isDaNhap() { return DA_NHAP.equals(trangThai); }
    public boolean isDaHuy() { return DA_HUY.equals(trangThai); }

    public long getTongTien() {
        long tong = 0;
        for (ChiTietKho ct : chiTiet) tong += ct.getThanhTien();
        return tong;
    }

    public String getTrangThaiHienThi() {
        switch (trangThai) {
            case CHO_NHAP: return "Chờ nhập";
            case DA_NHAP:  return "Đã nhập";
            case DA_HUY:   return "Đã hủy";
            default:       return trangThai;
        }
    }
}
