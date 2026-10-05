package Entity;

/**
 * Phiếu tiếp nhận sửa chữa.
 * UC015: thêm kyThuatVien (mã NV) để giao việc cho KTV cụ thể.
 */
public class PhieuTiepNhan {
    private String soPhieu;
    private String maKH;
    private String hoTen;
    private String sdt;
    private String bienSo;
    private String hangXe;
    private String loaiXe;
    private String ngayTiepNhan;
    private String yeuCau;
    private String tienDo;
    private String nguoiLap;
    private long thanhTien;
    private String kyThuatVien;     // UC015: mã NV của KTV được phân công

    public PhieuTiepNhan(String soPhieu, String maKH, String hoTen, String sdt,
                         String bienSo, String hangXe, String loaiXe,
                         String ngayTiepNhan, String yeuCau, String tienDo,
                         String nguoiLap, long thanhTien) {
        this(soPhieu, maKH, hoTen, sdt, bienSo, hangXe, loaiXe, ngayTiepNhan,
                yeuCau, tienDo, nguoiLap, thanhTien, "");
    }

    public PhieuTiepNhan(String soPhieu, String maKH, String hoTen, String sdt,
                         String bienSo, String hangXe, String loaiXe,
                         String ngayTiepNhan, String yeuCau, String tienDo,
                         String nguoiLap, long thanhTien, String kyThuatVien) {
        this.soPhieu = soPhieu;
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.bienSo = bienSo;
        this.hangXe = hangXe;
        this.loaiXe = loaiXe;
        this.ngayTiepNhan = ngayTiepNhan;
        this.yeuCau = yeuCau;
        this.tienDo = tienDo;
        this.nguoiLap = nguoiLap;
        this.thanhTien = thanhTien;
        this.kyThuatVien = kyThuatVien == null ? "" : kyThuatVien;
    }

    public String getSoPhieu() { return soPhieu; }
    public String getMaKH() { return maKH; }
    public String getHoTen() { return hoTen; }
    public String getSdt() { return sdt; }
    public String getBienSo() { return bienSo; }
    public String getHangXe() { return hangXe; }
    public String getLoaiXe() { return loaiXe; }
    public String getNgayTiepNhan() { return ngayTiepNhan; }
    public String getYeuCau() { return yeuCau; }
    public String getTienDo() { return tienDo; }
    public void setTienDo(String tienDo) { this.tienDo = tienDo; }
    public String getNguoiLap() { return nguoiLap; }
    public long getThanhTien() { return thanhTien; }
    public void setThanhTien(long thanhTien) { this.thanhTien = thanhTien; }
    public String getKyThuatVien() { return kyThuatVien; }
    public void setKyThuatVien(String kyThuatVien) { this.kyThuatVien = kyThuatVien; }
    public boolean daPhanCong() { return kyThuatVien != null && !kyThuatVien.isEmpty(); }
}
