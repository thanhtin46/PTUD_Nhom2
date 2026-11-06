package Entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity đại diện cho chức vụ / chức danh công việc trong hệ thống.
 */
public class ChucVu implements Serializable {
    private String maCV;
    private String tenChucVu;
    private String moTa;
    private String ngayTao;

    public ChucVu() {
    }

    public ChucVu(String maCV, String tenChucVu) {
        this(maCV, tenChucVu, "", LocalDate.now().toString());
    }

    public ChucVu(String maCV, String tenChucVu, String moTa) {
        this(maCV, tenChucVu, moTa, LocalDate.now().toString());
    }

    public ChucVu(String maCV, String tenChucVu, String moTa, String ngayTao) {
        this.maCV = maCV != null ? maCV.trim() : "";
        this.tenChucVu = tenChucVu != null ? tenChucVu.trim() : "";
        this.moTa = moTa != null ? moTa.trim() : "";
        this.ngayTao = (ngayTao != null && !ngayTao.isBlank()) ? ngayTao.trim() : LocalDate.now().toString();
    }

    public String getMaCV() {
        return maCV;
    }

    public void setMaCV(String maCV) {
        this.maCV = maCV != null ? maCV.trim() : "";
    }

    public String getTenChucVu() {
        return tenChucVu;
    }

    public void setTenChucVu(String tenChucVu) {
        this.tenChucVu = tenChucVu != null ? tenChucVu.trim() : "";
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa != null ? moTa.trim() : "";
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    @Override
    public String toString() {
        return tenChucVu != null ? tenChucVu : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChucVu chucVu = (ChucVu) o;
        return Objects.equals(maCV, chucVu.maCV) || Objects.equals(tenChucVu, chucVu.tenChucVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maCV, tenChucVu);
    }
}
