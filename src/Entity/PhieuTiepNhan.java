package Entity;

/**
 * Lớp thực thể Phiếu tiếp nhận xe.
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
    private String kyThuatVien;     // Mã nhân viên kỹ thuật viên phụ trách

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
        this.tienDo = chuanHoaTienDo(tienDo);
        this.nguoiLap = nguoiLap;
        this.thanhTien = thanhTien;
        this.kyThuatVien = kyThuatVien == null ? "" : kyThuatVien;
    }

    public static String chuanHoaTienDo(String value) {
        if (value == null) return "Chờ tiếp nhận";
        return switch (value.toUpperCase()) {
            case "DA_TIEP_NHAN", "CHO_TIEP_NHAN", "CHO_SUA" -> "Chờ tiếp nhận";
            case "DA_PHAN_CONG" -> "Đã phân công";
            case "DANG_SUA", "DANG_KIEM_TRA" -> "Đang sửa";
            case "CHO_PHU_TUNG" -> "Chờ phụ tùng";
            case "HOAN_THANH", "DA_HOAN_THANH" -> "Hoàn thành";
            default -> value;
        };
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
    public void setTienDo(String tienDo) { this.tienDo = chuanHoaTienDo(tienDo); }
    public String getNguoiLap() { return nguoiLap; }
    public long getThanhTien() { return thanhTien; }
    public void setThanhTien(long thanhTien) { this.thanhTien = thanhTien; }
    public String getKyThuatVien() { return kyThuatVien; }
    public void setKyThuatVien(String kyThuatVien) { this.kyThuatVien = kyThuatVien; }
    public boolean daPhanCong() { return kyThuatVien != null && !kyThuatVien.isEmpty(); }
}
