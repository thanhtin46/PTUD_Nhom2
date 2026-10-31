package Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Lớp thực thể Phiếu tiếp nhận xe.
 */
public class PhieuTiepNhan {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String soPhieu;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private PhuongTien phuongTien;
    private LocalDateTime ngayTN;
    private String yeuCau;
    private String tienDo;
    private long thanhTien;
    private String kyThuatVien;     // Mã nhân viên kỹ thuật viên phụ trách

    public PhieuTiepNhan() {
    }

    public PhieuTiepNhan(String soPhieu) {
        this.soPhieu = soPhieu;
    }

    public PhieuTiepNhan(String soPhieu, NhanVien nhanVien, KhachHang khachHang,
                         PhuongTien phuongTien, LocalDateTime ngayTN, String yeuCau,
                         String tienDo, long thanhTien, String kyThuatVien) {
        this.soPhieu = soPhieu;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.phuongTien = phuongTien;
        this.ngayTN = ngayTN;
        this.yeuCau = yeuCau;
        this.tienDo = chuanHoaTienDo(tienDo);
        this.thanhTien = thanhTien;
        this.kyThuatVien = kyThuatVien == null ? "" : kyThuatVien;
    }

    public PhieuTiepNhan(String soPhieu, String maKH, String hoTen, String sdt,
                         String bienSo, String hangXe, String loaiXe,
                         LocalDateTime ngayTN, String yeuCau, String tienDo,
                         String nguoiLap, long thanhTien) {
        this(soPhieu, maKH, hoTen, sdt, bienSo, hangXe, loaiXe, ngayTN,
                yeuCau, tienDo, nguoiLap, thanhTien, "");
    }

    public PhieuTiepNhan(String soPhieu, String maKH, String hoTen, String sdt,
                         String bienSo, String hangXe, String loaiXe,
                         LocalDateTime ngayTN, String yeuCau, String tienDo,
                         String nguoiLap, long thanhTien, String kyThuatVien) {
        this.soPhieu = soPhieu;
        this.khachHang = new KhachHang(maKH, hoTen, sdt, "", "");
        this.phuongTien = new PhuongTien(bienSo, hangXe, loaiXe, "VIN_" + (bienSo != null ? bienSo.replaceAll("[^A-Za-z0-9]", "") : ""), "", "", "2022", "0", this.khachHang, ngayTN != null ? ngayTN.toLocalDate() : LocalDate.now());
        NhanVien nv = new NhanVien();
        nv.setHoTen(nguoiLap != null ? nguoiLap : "");
        this.nhanVien = nv;
        this.ngayTN = ngayTN;
        this.yeuCau = yeuCau;
        this.tienDo = chuanHoaTienDo(tienDo);
        this.thanhTien = thanhTien;
        this.kyThuatVien = kyThuatVien == null ? "" : kyThuatVien;
    }

    public PhieuTiepNhan(String soPhieu, String maKH, String hoTen, String sdt,
                         String bienSo, String hangXe, String loaiXe,
                         String ngayTNStr, String yeuCau, String tienDo,
                         String nguoiLap, long thanhTien, String kyThuatVien) {
        this(soPhieu, maKH, hoTen, sdt, bienSo, hangXe, loaiXe, parseLocalDateTime(ngayTNStr),
                yeuCau, tienDo, nguoiLap, thanhTien, kyThuatVien);
    }

    public PhieuTiepNhan(String soPhieu, String maKH, String hoTen, String sdt,
                         String bienSo, String hangXe, String loaiXe,
                         String ngayTNStr, String yeuCau, String tienDo,
                         String nguoiLap, long thanhTien) {
        this(soPhieu, maKH, hoTen, sdt, bienSo, hangXe, loaiXe, parseLocalDateTime(ngayTNStr),
                yeuCau, tienDo, nguoiLap, thanhTien, "");
    }

    private static LocalDateTime parseLocalDateTime(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        s = s.trim();
        try {
            if (s.contains("/")) {
                String[] parts = s.split(" ");
                String datePart = parts[0];
                String[] p = datePart.split("/");
                LocalDate ld = LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                if (parts.length > 1) {
                    String timePart = parts[1];
                    String[] tp = timePart.split(":");
                    int h = Integer.parseInt(tp[0]);
                    int m = tp.length > 1 ? Integer.parseInt(tp[1]) : 0;
                    int sec = tp.length > 2 ? Integer.parseInt(tp[2]) : 0;
                    return ld.atTime(h, m, sec);
                }
                return ld.atStartOfDay();
            } else if (s.contains("T")) {
                return LocalDateTime.parse(s);
            } else if (s.contains("-")) {
                if (s.length() == 10) return LocalDate.parse(s).atStartOfDay();
                return LocalDateTime.parse(s.replace(" ", "T"));
            }
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            return null;
        }
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
    public void setSoPhieu(String soPhieu) { this.soPhieu = soPhieu; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public PhuongTien getPhuongTien() { return phuongTien; }
    public void setPhuongTien(PhuongTien phuongTien) { this.phuongTien = phuongTien; }

    public String getMaKH() { return khachHang != null ? khachHang.getMaKH() : ""; }
    public String getHoTen() { return khachHang != null ? khachHang.getHoTen() : ""; }
    public String getSdt() { return khachHang != null ? khachHang.getSdt() : ""; }
    public String getBienSo() { return phuongTien != null ? phuongTien.getBienSo() : ""; }
    public String getHangXe() { return phuongTien != null ? phuongTien.getHangXe() : ""; }
    public String getLoaiXe() { return phuongTien != null ? phuongTien.getDongXe() : ""; }
    public String getSoVIN() { return phuongTien != null ? phuongTien.getSoKhung() : ""; }
    public String getNguoiLap() { return nhanVien != null ? nhanVien.getHoTen() : ""; }
    public String getMaNV() { return nhanVien != null ? nhanVien.getMaNV() : ""; }

    public LocalDateTime getNgayTN() { return ngayTN; }
    public void setNgayTN(LocalDateTime ngayTN) { this.ngayTN = ngayTN; }

    public LocalDateTime getNgayTiepNhan() { return ngayTN; }
    public void setNgayTiepNhan(LocalDateTime ngayTN) { this.ngayTN = ngayTN; }
    public void setNgayTiepNhan(String ngayTNStr) { this.ngayTN = parseLocalDateTime(ngayTNStr); }

    public String getNgayTNHienThi() {
        return ngayTN != null ? ngayTN.format(DATETIME_FORMATTER) : "";
    }

    public String getNgayTiepNhanHienThi() {
        return getNgayTNHienThi();
    }

    public String getYeuCau() { return yeuCau; }
    public void setYeuCau(String yeuCau) { this.yeuCau = yeuCau; }

    public String getTienDo() { return tienDo; }
    public void setTienDo(String tienDo) { this.tienDo = chuanHoaTienDo(tienDo); }

    public long getThanhTien() { return thanhTien; }
    public void setThanhTien(long thanhTien) { this.thanhTien = thanhTien; }

    public String getKyThuatVien() { return kyThuatVien; }
    public void setKyThuatVien(String kyThuatVien) { this.kyThuatVien = kyThuatVien; }
    public boolean daPhanCong() { return kyThuatVien != null && !kyThuatVien.isEmpty(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhieuTiepNhan that = (PhieuTiepNhan) o;
        return Objects.equals(soPhieu, that.soPhieu);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(soPhieu);
    }

    @Override
    public String toString() {
        return (soPhieu != null ? soPhieu : "") + (phuongTien != null ? (" - " + phuongTien.getBienSo()) : "");
    }
}
