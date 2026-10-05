package Entity;

public class PhuongTien {
    private String bienSo;
    private String hangXe;
    private String dongXe;
    private String soKhung;
    private String soMay;
    private String mauSon;
    private String namSX;
    private String odo;
    private String maChuXe;
    private String ngayTiepNhanGanNhat;

    public PhuongTien(String bienSo, String hangXe, String dongXe, String soKhung,
                      String soMay, String mauSon, String namSX, String odo,
                      String maChuXe, String ngayTiepNhanGanNhat) {
        this.bienSo = bienSo;
        this.hangXe = hangXe;
        this.dongXe = dongXe;
        this.soKhung = soKhung;
        this.soMay = soMay;
        this.mauSon = mauSon;
        this.namSX = namSX;
        this.odo = odo;
        this.maChuXe = maChuXe;
        this.ngayTiepNhanGanNhat = ngayTiepNhanGanNhat;
    }

    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }

    public String getHangXe() { return hangXe; }
    public void setHangXe(String hangXe) { this.hangXe = hangXe; }

    public String getDongXe() { return dongXe; }
    public void setDongXe(String dongXe) { this.dongXe = dongXe; }

    public String getSoKhung() { return soKhung; }
    public void setSoKhung(String soKhung) { this.soKhung = soKhung; }

    public String getSoMay() { return soMay; }
    public void setSoMay(String soMay) { this.soMay = soMay; }

    public String getMauSon() { return mauSon; }
    public void setMauSon(String mauSon) { this.mauSon = mauSon; }

    public String getNamSX() { return namSX; }
    public void setNamSX(String namSX) { this.namSX = namSX; }

    public String getOdo() { return odo; }
    public void setOdo(String odo) { this.odo = odo; }

    public String getMaChuXe() { return maChuXe; }
    public void setMaChuXe(String maChuXe) { this.maChuXe = maChuXe; }

    public String getNgayTiepNhanGanNhat() { return ngayTiepNhanGanNhat; }
    public void setNgayTiepNhanGanNhat(String ngayTiepNhanGanNhat) { this.ngayTiepNhanGanNhat = ngayTiepNhanGanNhat; }
}
