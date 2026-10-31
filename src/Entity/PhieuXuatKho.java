package Entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Thực thể Phiếu xuất kho phụ tùng.
 */
public class PhieuXuatKho {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String maPhieuXuat;
    private PhieuSuaChua phieuSuaChua;
    private NhanVien nhanVien;
    private LocalDateTime ngayXuat;
    private String trangThai;
    private String lyDoXuat;

    public PhieuXuatKho() {
    }

    public PhieuXuatKho(String maPhieuXuat) {
        this.maPhieuXuat = maPhieuXuat;
    }

    public PhieuXuatKho(String maPhieuXuat, PhieuSuaChua phieuSuaChua, NhanVien nhanVien,
                        LocalDateTime ngayXuat, String trangThai, String lyDoXuat) {
        this.maPhieuXuat = maPhieuXuat;
        this.phieuSuaChua = phieuSuaChua;
        this.nhanVien = nhanVien;
        this.ngayXuat = ngayXuat;
        this.trangThai = trangThai;
        this.lyDoXuat = lyDoXuat;
    }

    public PhieuXuatKho(String maPhieuXuat, String maPSC, String maNV,
                        LocalDateTime ngayXuat, String trangThai, String lyDoXuat) {
        this(maPhieuXuat,
             maPSC != null && !maPSC.isEmpty() ? new PhieuSuaChua(maPSC) : null,
             maNV != null && !maNV.isEmpty() ? new NhanVien(maNV) : null,
             ngayXuat, trangThai, lyDoXuat);
    }

    public String getMaPhieuXuat() { return maPhieuXuat; }
    public void setMaPhieuXuat(String maPhieuXuat) { this.maPhieuXuat = maPhieuXuat; }

    public PhieuSuaChua getPhieuSuaChua() { return phieuSuaChua; }
    public void setPhieuSuaChua(PhieuSuaChua phieuSuaChua) { this.phieuSuaChua = phieuSuaChua; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public String getMaPSC() { return phieuSuaChua != null ? phieuSuaChua.getMaPSC() : ""; }
    public void setMaPSC(String maPSC) {
        if (this.phieuSuaChua == null) this.phieuSuaChua = new PhieuSuaChua(maPSC);
        else this.phieuSuaChua.setMaPSC(maPSC);
    }

    public String getMaNV() { return nhanVien != null ? nhanVien.getMaNV() : ""; }
    public void setMaNV(String maNV) {
        if (this.nhanVien == null) this.nhanVien = new NhanVien(maNV);
        else this.nhanVien.setMaNV(maNV);
    }

    public LocalDateTime getNgayXuat() { return ngayXuat; }
    public void setNgayXuat(LocalDateTime ngayXuat) { this.ngayXuat = ngayXuat; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getLyDoXuat() { return lyDoXuat; }
    public void setLyDoXuat(String lyDoXuat) { this.lyDoXuat = lyDoXuat; }

    public String getGhiChu() { return lyDoXuat; }
    public void setGhiChu(String ghiChu) { this.lyDoXuat = ghiChu; }

    public String getNgayXuatHienThi() {
        return ngayXuat != null ? ngayXuat.format(DATETIME_FORMATTER) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhieuXuatKho that = (PhieuXuatKho) o;
        return Objects.equals(maPhieuXuat, that.maPhieuXuat);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPhieuXuat);
    }

    @Override
    public String toString() {
        return (maPhieuXuat != null ? maPhieuXuat : "") + (phieuSuaChua != null ? (" - " + phieuSuaChua.getMaPSC()) : "");
    }
}
