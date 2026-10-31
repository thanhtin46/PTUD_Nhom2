package Entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Thực thể Phiếu nhập kho phụ tùng.
 */
public class PhieuNhapKho {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String maPhieuNhap;
    private NhaCungCap nhaCungCap;
    private NhanVien nhanVien;
    private LocalDate ngayNhap;
    private long tongTien;
    private String trangThai;
    private LocalDate ngayDuKienGiao;
    private String ghiChu;

    public PhieuNhapKho() {
    }

    public PhieuNhapKho(String maPhieuNhap) {
        this.maPhieuNhap = maPhieuNhap;
    }

    public PhieuNhapKho(String maPhieuNhap, NhaCungCap nhaCungCap, NhanVien nhanVien,
                        LocalDate ngayNhap, long tongTien, String trangThai,
                        LocalDate ngayDuKienGiao, String ghiChu) {
        this.maPhieuNhap = maPhieuNhap;
        this.nhaCungCap = nhaCungCap;
        this.nhanVien = nhanVien;
        this.ngayNhap = ngayNhap;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.ngayDuKienGiao = ngayDuKienGiao;
        this.ghiChu = ghiChu;
    }

    public PhieuNhapKho(String maPhieuNhap, String maNCC, String maNV, LocalDate ngayNhap,
                        long tongTien, String trangThai, LocalDate ngayDuKienGiao, String ghiChu) {
        this(maPhieuNhap,
             maNCC != null && !maNCC.isEmpty() ? new NhaCungCap(maNCC) : null,
             maNV != null && !maNV.isEmpty() ? new NhanVien(maNV) : null,
             ngayNhap, tongTien, trangThai, ngayDuKienGiao, ghiChu);
    }

    public String getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(String maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }

    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public String getMaNCC() { return nhaCungCap != null ? nhaCungCap.getMaNCC() : ""; }
    public void setMaNCC(String maNCC) {
        if (this.nhaCungCap == null) this.nhaCungCap = new NhaCungCap(maNCC);
        else this.nhaCungCap.setMaNCC(maNCC);
    }

    public String getTenNCC() { return nhaCungCap != null ? nhaCungCap.getTenNCC() : ""; }

    public String getMaNV() { return nhanVien != null ? nhanVien.getMaNV() : ""; }
    public void setMaNV(String maNV) {
        if (this.nhanVien == null) this.nhanVien = new NhanVien(maNV);
        else this.nhanVien.setMaNV(maNV);
    }

    public LocalDate getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(LocalDate ngayNhap) { this.ngayNhap = ngayNhap; }

    public long getTongTien() { return tongTien; }
    public void setTongTien(long tongTien) { this.tongTien = tongTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDate getNgayDuKienGiao() { return ngayDuKienGiao; }
    public void setNgayDuKienGiao(LocalDate ngayDuKienGiao) { this.ngayDuKienGiao = ngayDuKienGiao; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getNgayNhapHienThi() {
        return ngayNhap != null ? ngayNhap.format(DATE_FORMATTER) : "";
    }

    public String getNgayDuKienGiaoHienThi() {
        return ngayDuKienGiao != null ? ngayDuKienGiao.format(DATE_FORMATTER) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhieuNhapKho that = (PhieuNhapKho) o;
        return Objects.equals(maPhieuNhap, that.maPhieuNhap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPhieuNhap);
    }

    @Override
    public String toString() {
        return (maPhieuNhap != null ? maPhieuNhap : "") + (nhaCungCap != null ? (" - " + nhaCungCap.getTenNCC()) : "");
    }
}
