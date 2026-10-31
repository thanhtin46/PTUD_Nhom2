package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Kho lưu trữ vị trí và tồn kho phụ tùng.
 */
public class Kho {
    private String maKho;
    private PhuTung phuTung;
    private int soLuongTon;
    private String viTriKho;
    private int soLTT;
    private int soLTD;

    public Kho() {
    }

    public Kho(String maKho) {
        this.maKho = maKho;
    }

    public Kho(String maKho, PhuTung phuTung, int soLuongTon, String viTriKho, int soLTT, int soLTD) {
        this.maKho = maKho;
        this.phuTung = phuTung;
        this.soLuongTon = soLuongTon;
        this.viTriKho = viTriKho;
        this.soLTT = soLTT;
        this.soLTD = soLTD;
    }

    public Kho(String maKho, String maPT, int soLuongTon, String viTriKho, int soLTT, int soLTD) {
        this(maKho, maPT != null && !maPT.isEmpty() ? new PhuTung(maPT) : null, soLuongTon, viTriKho, soLTT, soLTD);
    }

    public String getMaKho() {
        return maKho;
    }

    public void setMaKho(String maKho) {
        this.maKho = maKho;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public void setPhuTung(PhuTung phuTung) {
        this.phuTung = phuTung;
    }

    public String getMaPT() {
        return phuTung != null ? phuTung.getMaPT() : "";
    }

    public void setMaPT(String maPT) {
        if (this.phuTung == null) {
            this.phuTung = new PhuTung(maPT);
        } else {
            this.phuTung.setMaPT(maPT);
        }
    }

    public String getTenPT() {
        return phuTung != null ? phuTung.getTen() : "";
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public String getViTriKho() {
        return viTriKho;
    }

    public void setViTriKho(String viTriKho) {
        this.viTriKho = viTriKho;
    }

    public int getSoLTT() {
        return soLTT;
    }

    public void setSoLTT(int soLTT) {
        this.soLTT = soLTT;
    }

    public int getSoLTD() {
        return soLTD;
    }

    public void setSoLTD(int soLTD) {
        this.soLTD = soLTD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kho kho = (Kho) o;
        return Objects.equals(maKho, kho.maKho);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maKho);
    }

    @Override
    public String toString() {
        return (maKho != null ? maKho : "") + (phuTung != null ? (" - " + phuTung.getTen()) : "");
    }
}
