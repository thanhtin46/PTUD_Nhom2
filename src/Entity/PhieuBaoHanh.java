package Entity;

public class PhieuBaoHanh {
    private String soPhieu;
    private String bienSo;
    private String ngayBatDau;
    private String ngayKetThuc;
    private String nguoiLap;
    private String tinhTrang;
    private String noiDung;

    public PhieuBaoHanh(String soPhieu, String bienSo, String ngayBatDau, String ngayKetThuc,
                        String nguoiLap, String tinhTrang, String noiDung) {
        this.soPhieu = soPhieu;
        this.bienSo = bienSo;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.nguoiLap = nguoiLap;
        this.tinhTrang = tinhTrang;
        this.noiDung = noiDung;
    }

    public String getSoPhieu() { return soPhieu; }
    public String getBienSo() { return bienSo; }
    public String getNgayBatDau() { return ngayBatDau; }
    public String getNgayKetThuc() { return ngayKetThuc; }
    public String getNguoiLap() { return nguoiLap; }
    public String getTinhTrang() { return tinhTrang; }
    public String getNoiDung() { return noiDung; }
}
