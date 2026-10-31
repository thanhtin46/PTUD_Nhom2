package Entity;

import java.util.Objects;

/**
 * Lớp thực thể Tài khoản đăng nhập hệ thống gara ô tô.
 */
public class TaiKhoan {
    private NhanVien nhanVien;
    private String taiKhoan;
    private String matKhau;
    private String trangThai;

    public TaiKhoan() {
    }

    public TaiKhoan(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public TaiKhoan(String maNV) {
        this.nhanVien = new NhanVien(maNV);
    }

    public TaiKhoan(NhanVien nhanVien, String taiKhoan, String matKhau, String trangThai) {
        this.nhanVien = nhanVien;
        this.taiKhoan = taiKhoan;
        this.matKhau = matKhau;
        this.trangThai = trangThai;
    }

    public TaiKhoan(String maNV, String taiKhoan, String matKhau, String trangThai) {
        this(new NhanVien(maNV), taiKhoan, matKhau, trangThai);
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getMaNV() {
        return nhanVien != null ? nhanVien.getMaNV() : "";
    }

    public void setMaNV(String maNV) {
        if (this.nhanVien == null) {
            this.nhanVien = new NhanVien(maNV);
        } else {
            this.nhanVien.setMaNV(maNV);
        }
    }

    public String getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(String taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaiKhoan taiKhoan1 = (TaiKhoan) o;
        return Objects.equals(nhanVien, taiKhoan1.nhanVien);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nhanVien);
    }

    @Override
    public String toString() {
        String nvName = (nhanVien != null && nhanVien.getHoTen() != null) ? nhanVien.getHoTen() : "";
        return (taiKhoan != null ? taiKhoan : "") + (!nvName.isEmpty() ? (" (" + nvName + ")") : "");
    }
}
