package Entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Lớp thực thể Phiếu nhập xuất kho.
 */
public class PhieuKho {
    public static final String NHAP = "NHAP";
    public static final String XUAT = "XUAT";

    public static final String CHO_NHAP = "CHO_NHAP";
    public static final String DA_NHAP  = "DA_NHAP";
    public static final String DA_HUY   = "DA_HUY";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String soPhieu;
    private String loai;
    private LocalDate ngay;
    private String nguoiLap;
    private String ghiChu;
    private String nhaCungCap;       // Nhà cung cấp
    private LocalDate ngayDuKienGiao;   // Ngày dự kiến giao
    private String trangThai;        // Trạng thái phiếu kho
    private final ArrayList<ChiTietKho> chiTiet = new ArrayList<>();

    public PhieuKho(String soPhieu, String loai, String ngay, String nguoiLap, String ghiChu) {
        this(soPhieu, loai, parseLocalDate(ngay), nguoiLap, ghiChu, "", null, CHO_NHAP);
    }

    public PhieuKho(String soPhieu, String loai, String ngay, String nguoiLap,
                    String ghiChu, String nhaCungCap, String ngayDuKienGiao, String trangThai) {
        this(soPhieu, loai, parseLocalDate(ngay), nguoiLap, ghiChu, nhaCungCap, parseLocalDate(ngayDuKienGiao), trangThai);
    }

    public PhieuKho(String soPhieu, String loai, LocalDate ngay, String nguoiLap,
                    String ghiChu, String nhaCungCap, LocalDate ngayDuKienGiao, String trangThai) {
        this.soPhieu = soPhieu;
        this.loai = loai;
        this.ngay = ngay != null ? ngay : LocalDate.now();
        this.nguoiLap = nguoiLap;
        this.ghiChu = ghiChu;
        this.nhaCungCap = nhaCungCap == null ? "" : nhaCungCap;
        this.ngayDuKienGiao = ngayDuKienGiao;
        this.trangThai = trangThai == null ? CHO_NHAP : trangThai;
    }

    private static LocalDate parseLocalDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        s = s.trim();
        try {
            if (s.contains("/")) {
                String[] p = s.split("/");
                if (p.length == 3) {
                    return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                }
            } else if (s.contains("-")) {
                if (s.length() >= 10) {
                    return LocalDate.parse(s.substring(0, 10));
                }
            }
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public String getSoPhieu() { return soPhieu; }
    public String getLoai() { return loai; }

    public String getNgay() {
        return ngay != null ? ngay.format(DATE_FORMATTER) : "";
    }

    public LocalDate getNgayDate() {
        return ngay;
    }

    public void setNgay(LocalDate ngay) {
        this.ngay = ngay;
    }

    public void setNgay(String ngayStr) {
        this.ngay = parseLocalDate(ngayStr);
    }

    public String getNguoiLap() { return nguoiLap; }
    public String getGhiChu() { return ghiChu; }
    public ArrayList<ChiTietKho> getChiTiet() { return chiTiet; }
    public String getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }

    public String getNgayDuKienGiao() {
        return ngayDuKienGiao != null ? ngayDuKienGiao.format(DATE_FORMATTER) : "";
    }

    public LocalDate getNgayDuKienGiaoDate() {
        return ngayDuKienGiao;
    }

    public void setNgayDuKienGiao(LocalDate ngayDuKienGiao) {
        this.ngayDuKienGiao = ngayDuKienGiao;
    }

    public void setNgayDuKienGiao(String ngayDuKienGiaoStr) {
        this.ngayDuKienGiao = parseLocalDate(ngayDuKienGiaoStr);
    }

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
