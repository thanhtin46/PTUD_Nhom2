package Entity;

public class KhachHang {
    private String maKH;
    private String hoTen;
    private String sdt;
    private String cccd;
    private String diaChi;
    private String email;
    private String ghiChu;
    private String ngayDangKy;

    public KhachHang(String maKH, String hoTen, String sdt, String cccd,
                     String diaChi, String email, String ghiChu, String ngayDangKy) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.cccd = cccd;
        this.diaChi = diaChi;
        this.email = email;
        this.ghiChu = ghiChu;
        this.ngayDangKy = ngayDangKy;
    }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(String ngayDangKy) { this.ngayDangKy = ngayDangKy; }
}
