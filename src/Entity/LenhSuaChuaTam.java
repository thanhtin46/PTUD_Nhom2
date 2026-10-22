package Entity;

import java.util.ArrayList;
import java.util.List;

public class LenhSuaChuaTam {

    private String maPSC;
    private String maPTN;
    private String maNV;
    private String kyThuatVien;
    private String bienSoXe;
    private String thongTinXe;
    private String khachHang;
    private String yeuCau;
    private String ngayLap;
    private String ngayHen;
    private String tienDo;
    private String ghiChu;

    // Danh sách phụ tùng được yêu cầu
    private List<Object[]> dsPhuTung;

    public LenhSuaChuaTam(
            String maPSC,
            String maPTN,
            String maNV,
            String kyThuatVien,
            String bienSoXe,
            String thongTinXe,
            String khachHang,
            String yeuCau,
            String ngayLap,
            String ngayHen,
            String tienDo,
            String ghiChu,
            List<Object[]> dsPhuTung) {

        this.maPSC = maPSC;
        this.maPTN = maPTN;
        this.maNV = maNV;
        this.kyThuatVien = kyThuatVien;
        this.bienSoXe = bienSoXe;
        this.thongTinXe = thongTinXe;
        this.khachHang = khachHang;
        this.yeuCau = yeuCau;
        this.ngayLap = ngayLap;
        this.ngayHen = ngayHen;
        this.tienDo = tienDo;
        this.ghiChu = ghiChu;

        this.dsPhuTung = new ArrayList<>();

        if (dsPhuTung != null) {
            for (Object[] row : dsPhuTung) {
                this.dsPhuTung.add(row.clone());
            }
        }
    }

    public String getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(String maPSC) {
        this.maPSC = maPSC;
    }

    public String getMaPTN() {
        return maPTN;
    }

    public void setMaPTN(String maPTN) {
        this.maPTN = maPTN;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getKyThuatVien() {
        return kyThuatVien;
    }

    public void setKyThuatVien(String kyThuatVien) {
        this.kyThuatVien = kyThuatVien;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public String getThongTinXe() {
        return thongTinXe;
    }

    public void setThongTinXe(String thongTinXe) {
        this.thongTinXe = thongTinXe;
    }

    public String getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(String khachHang) {
        this.khachHang = khachHang;
    }

    public String getYeuCau() {
        return yeuCau;
    }

    public void setYeuCau(String yeuCau) {
        this.yeuCau = yeuCau;
    }

    public String getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(String ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getNgayHen() {
        return ngayHen;
    }

    public void setNgayHen(String ngayHen) {
        this.ngayHen = ngayHen;
    }

    public String getTienDo() {
        return tienDo;
    }

    public void setTienDo(String tienDo) {
        this.tienDo = tienDo;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public List<Object[]> getDsPhuTung() {
        List<Object[]> copy = new ArrayList<>();

        for (Object[] row : dsPhuTung) {
            copy.add(row.clone());
        }

        return copy;
    }

    public void setDsPhuTung(List<Object[]> dsPhuTung) {
        this.dsPhuTung = new ArrayList<>();

        if (dsPhuTung != null) {
            for (Object[] row : dsPhuTung) {
                this.dsPhuTung.add(row.clone());
            }
        }
    }
}