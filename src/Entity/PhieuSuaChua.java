package Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Thực thể Phiếu sửa chữa gara ô tô.
 */
public class PhieuSuaChua {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String maPSC;
    private PhieuTiepNhan phieuTiepNhan;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private PhuongTien phuongTien;
    private LocalDateTime ngayBD;
    private LocalDate ngayHoanThanhDuKien;
    private String trangThai;
    private String noiDungSuaChua;

    public PhieuSuaChua() {
    }

    public PhieuSuaChua(String maPSC) {
        this.maPSC = maPSC;
    }

    public PhieuSuaChua(String maPSC, PhieuTiepNhan phieuTiepNhan, NhanVien nhanVien,
                        KhachHang khachHang, PhuongTien phuongTien,
                        LocalDateTime ngayBD, LocalDate ngayHoanThanhDuKien,
                        String trangThai, String noiDungSuaChua) {
        this.maPSC = maPSC;
        this.phieuTiepNhan = phieuTiepNhan;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.phuongTien = phuongTien;
        this.ngayBD = ngayBD;
        this.ngayHoanThanhDuKien = ngayHoanThanhDuKien;
        this.trangThai = trangThai;
        this.noiDungSuaChua = noiDungSuaChua;
    }

    public PhieuSuaChua(String maPSC, String maPTN, String maNV, String maKH, String soVIN,
                        LocalDateTime ngayBD, LocalDate ngayHoanThanhDuKien,
                        String trangThai, String noiDungSuaChua) {
        this(maPSC,
                maPTN != null && !maPTN.isEmpty() ? new PhieuTiepNhan(maPTN) : null,
                maNV != null && !maNV.isEmpty() ? new NhanVien(maNV) : null,
                maKH != null && !maKH.isEmpty() ? new KhachHang(maKH) : null,
                soVIN != null && !soVIN.isEmpty() ? new PhuongTien(soVIN) : null,
                ngayBD, ngayHoanThanhDuKien, trangThai, noiDungSuaChua);
    }

    public String getMaPSC() { return maPSC; }
    public void setMaPSC(String maPSC) { this.maPSC = maPSC; }

    public PhieuTiepNhan getPhieuTiepNhan() { return phieuTiepNhan; }
    public void setPhieuTiepNhan(PhieuTiepNhan phieuTiepNhan) { this.phieuTiepNhan = phieuTiepNhan; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public PhuongTien getPhuongTien() { return phuongTien; }
    public void setPhuongTien(PhuongTien phuongTien) { this.phuongTien = phuongTien; }

    public String getMaPTN() { return phieuTiepNhan != null ? phieuTiepNhan.getSoPhieu() : ""; }
    public void setMaPTN(String maPTN) {
        if (this.phieuTiepNhan == null) this.phieuTiepNhan = new PhieuTiepNhan(maPTN);
        else this.phieuTiepNhan.setSoPhieu(maPTN);
    }

    public String getMaNV() { return nhanVien != null ? nhanVien.getMaNV() : ""; }
    public void setMaNV(String maNV) {
        if (this.nhanVien == null) this.nhanVien = new NhanVien(maNV);
        else this.nhanVien.setMaNV(maNV);
    }

    public String getMaKH() { return khachHang != null ? khachHang.getMaKH() : ""; }
    public void setMaKH(String maKH) {
        if (this.khachHang == null) this.khachHang = new KhachHang(maKH);
        else this.khachHang.setMaKH(maKH);
    }

    public String getSoVIN() { return phuongTien != null ? phuongTien.getSoKhung() : ""; }
    public void setSoVIN(String soVIN) {
        if (this.phuongTien == null) this.phuongTien = new PhuongTien(soVIN);
        else this.phuongTien.setSoKhung(soVIN);
    }

    public String getBienSo() {
        return phuongTien != null ? phuongTien.getBienSo() : "";
    }

    public LocalDateTime getNgayBD() { return ngayBD; }
    public void setNgayBD(LocalDateTime ngayBD) { this.ngayBD = ngayBD; }

    public LocalDate getNgayHoanThanhDuKien() { return ngayHoanThanhDuKien; }
    public void setNgayHoanThanhDuKien(LocalDate ngayHoanThanhDuKien) { this.ngayHoanThanhDuKien = ngayHoanThanhDuKien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getNoiDungSuaChua() { return noiDungSuaChua; }
    public void setNoiDungSuaChua(String noiDungSuaChua) { this.noiDungSuaChua = noiDungSuaChua; }

    public String getNgayBDHienThi() {
        return ngayBD != null ? ngayBD.format(DATETIME_FORMATTER) : "";
    }

    public String getNgayHoanThanhDuKienHienThi() {
        return ngayHoanThanhDuKien != null ? ngayHoanThanhDuKien.format(DATE_FORMATTER) : "Chưa có ngày hẹn";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhieuSuaChua that = (PhieuSuaChua) o;
        return Objects.equals(maPSC, that.maPSC);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maPSC);
    }

    @Override
    public String toString() {
        return (maPSC != null ? maPSC : "") + (phuongTien != null ? (" - " + phuongTien.getBienSo()) : "");
    }
}
