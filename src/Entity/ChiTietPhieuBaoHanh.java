package Entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Lớp thực thể Chi tiết các hạng mục bảo hành của Phiếu bảo hành.
 */
public class ChiTietPhieuBaoHanh {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PhieuBaoHanh phieuBaoHanh;
    private PhuTung phuTung;
    private String tenHangMuc;
    private int thoiHanBaoHanh;       // Số tháng
    private LocalDate ngayHetHan;
    private String dieuKienBaoHanh;
    private String ghiChu;
    private String lyDo;
    private String huongXuLy;

    public ChiTietPhieuBaoHanh() {
    }

    public ChiTietPhieuBaoHanh(PhieuBaoHanh phieuBaoHanh, PhuTung phuTung, String tenHangMuc,
                               int thoiHanBaoHanh, LocalDate ngayHetHan, String dieuKienBaoHanh,
                               String ghiChu, String lyDo, String huongXuLy) {
        this.phieuBaoHanh = phieuBaoHanh;
        this.phuTung = phuTung;
        this.tenHangMuc = tenHangMuc;
        this.thoiHanBaoHanh = thoiHanBaoHanh;
        this.ngayHetHan = ngayHetHan;
        this.dieuKienBaoHanh = dieuKienBaoHanh;
        this.ghiChu = ghiChu;
        this.lyDo = lyDo;
        this.huongXuLy = huongXuLy;
    }

    public ChiTietPhieuBaoHanh(String maPBH, String maPT, String tenHangMuc,
                               int thoiHanBaoHanh, LocalDate ngayHetHan, String dieuKienBaoHanh,
                               String ghiChu, String lyDo, String huongXuLy) {
        this(maPBH != null && !maPBH.isEmpty() ? new PhieuBaoHanh(maPBH) : null,
             maPT != null && !maPT.isEmpty() ? new PhuTung(maPT) : null,
             tenHangMuc, thoiHanBaoHanh, ngayHetHan, dieuKienBaoHanh, ghiChu, lyDo, huongXuLy);
    }

    public PhieuBaoHanh getPhieuBaoHanh() {
        return phieuBaoHanh;
    }

    public void setPhieuBaoHanh(PhieuBaoHanh phieuBaoHanh) {
        this.phieuBaoHanh = phieuBaoHanh;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }

    public String getMaPBH() {
        return phieuBaoHanh != null ? phieuBaoHanh.getSoPhieu() : "";
    }

    public void setMaPBH(String maPBH) {
        if (this.phieuBaoHanh == null) this.phieuBaoHanh = new PhieuBaoHanh(maPBH);
        else this.phieuBaoHanh.setSoPhieu(maPBH);
    }

    public String getMaPT() {
        return phuTung != null ? phuTung.getMaPT() : "";
    }

    public void setMaPT(String maPT) {
        if (this.phuTung == null) this.phuTung = new PhuTung(maPT);
        else this.phuTung.setMaPT(maPT);
    }

    public String getTenHangMuc() {
        return tenHangMuc != null && !tenHangMuc.isEmpty() ? tenHangMuc : (phuTung != null ? phuTung.getTen() : "");
    }

    public void setTenHangMuc(String tenHangMuc) {
        this.tenHangMuc = tenHangMuc;
    }

    public int getThoiHanBaoHanh() {
        return thoiHanBaoHanh;
    }

    public void setThoiHanBaoHanh(int thoiHanBaoHanh) {
        this.thoiHanBaoHanh = thoiHanBaoHanh;
    }

    public LocalDate getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDate ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public String getNgayHetHanHienThi() {
        return ngayHetHan != null ? ngayHetHan.format(DATE_FORMATTER) : "";
    }

    public String getDieuKienBaoHanh() {
        return dieuKienBaoHanh;
    }

    public void setDieuKienBaoHanh(String dieuKienBaoHanh) {
        this.dieuKienBaoHanh = dieuKienBaoHanh;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getHuongXuLy() {
        return huongXuLy;
    }

    public void setHuongXuLy(String huongXuLy) {
        this.huongXuLy = huongXuLy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietPhieuBaoHanh that = (ChiTietPhieuBaoHanh) o;
        return Objects.equals(phieuBaoHanh, that.phieuBaoHanh) &&
               Objects.equals(tenHangMuc, that.tenHangMuc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phieuBaoHanh, tenHangMuc);
    }

    @Override
    public String toString() {
        return (tenHangMuc != null ? tenHangMuc : "") + " (" + thoiHanBaoHanh + " tháng)";
    }
}
