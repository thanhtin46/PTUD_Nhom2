package Entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Lớp chứa thông tin phiếu sửa chữa phục vụ theo dõi tiến độ và hạn hoàn thành.
 */
public class PhieuDieuHanhDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String maPSC;
    private String maPTN;
    private String bienSoXe;
    private String tenKhachHang;
    private String soDT;
    private String maKTV;
    private String tenKTV;
    private LocalDate ngayBD;
    private LocalDate ngayHoanThanhDuKien;
    private String trangThai;

    public PhieuDieuHanhDTO() {
    }

    public PhieuDieuHanhDTO(String maPSC, String maPTN, String bienSoXe, String tenKhachHang,
                           String soDT, String maKTV, String tenKTV, LocalDate ngayBD,
                           LocalDate ngayHoanThanhDuKien, String trangThai) {
        this.maPSC = maPSC;
        this.maPTN = maPTN;
        this.bienSoXe = bienSoXe != null && !bienSoXe.isBlank() ? bienSoXe : "—";
        this.tenKhachHang = tenKhachHang != null && !tenKhachHang.isBlank() ? tenKhachHang : "Khách vãng lai";
        this.soDT = soDT != null && !soDT.isBlank() ? soDT : "—";
        this.maKTV = maKTV;
        this.tenKTV = tenKTV != null && !tenKTV.isBlank() ? tenKTV : "(Chưa phân công)";
        this.ngayBD = ngayBD;
        this.ngayHoanThanhDuKien = ngayHoanThanhDuKien;
        this.trangThai = trangThai != null ? trangThai : "CHO_SUA";
    }

    // Kiểm tra phiếu đã hoàn thành
    public boolean isHoanThanh() {
        if (trangThai == null) return false;
        String s = trangThai.toUpperCase().trim();
        return s.equals("HOAN_THANH") || s.equals("DA_HOAN_THANH") || s.contains("HOÀN THÀNH");
    }

    // Kiểm tra phiếu đã hủy
    public boolean isDaHuy() {
        if (trangThai == null) return false;
        String s = trangThai.toUpperCase().trim();
        return s.equals("DA_HUY") || s.equals("HUY") || s.contains("HỦY");
    }

    // Kiểm tra phiếu chưa hoàn thành
    public boolean isChuaHoanThanh() {
        return !isHoanThanh() && !isDaHuy();
    }

    // Kiểm tra phiếu có hẹn hoàn thành hôm nay
    public boolean isHenHomNay() {
        if (ngayHoanThanhDuKien == null) return false;
        return ngayHoanThanhDuKien.isEqual(LocalDate.now());
    }

    // Kiểm tra phiếu bị quá hẹn
    public boolean isQuaHen() {
        if (!isChuaHoanThanh()) return false;
        if (ngayHoanThanhDuKien == null) return false;
        return ngayHoanThanhDuKien.isBefore(LocalDate.now());
    }

    // Lấy số ngày quá hẹn
    public int getSoNgayQuaHen() {
        if (!isQuaHen()) return 0;
        return (int) ChronoUnit.DAYS.between(ngayHoanThanhDuKien, LocalDate.now());
    }

    // Chuỗi hiển thị số ngày quá hẹn
    public String getSoNgayQuaHenHienThi() {
        if (isHoanThanh()) return "Đã hoàn thành";
        if (isDaHuy()) return "Đã hủy";
        if (ngayHoanThanhDuKien == null) return "—";

        LocalDate today = LocalDate.now();
        if (ngayHoanThanhDuKien.isEqual(today)) {
            return "Hôm nay";
        }
        if (ngayHoanThanhDuKien.isBefore(today)) {
            long days = ChronoUnit.DAYS.between(ngayHoanThanhDuKien, today);
            return "Quá " + days + " ngày";
        } else {
            long days = ChronoUnit.DAYS.between(today, ngayHoanThanhDuKien);
            return "Còn " + days + " ngày";
        }
    }

    // Chuỗi hiển thị ngày hẹn
    public String getNgayHenHienThi() {
        if (ngayHoanThanhDuKien == null) {
            return "Chưa có ngày hẹn";
        }
        return ngayHoanThanhDuKien.format(DATE_FORMATTER);
    }

    // Chuỗi hiển thị ngày bắt đầu
    public String getNgayBDHienThi() {
        if (ngayBD == null) return "—";
        return ngayBD.format(DATE_FORMATTER);
    }

    // Chuỗi hiển thị trạng thái sửa chữa
    public String getTrangThaiHienThi() {
        if (trangThai == null) return "Chờ sửa chữa";
        String s = trangThai.toUpperCase().trim();
        if (s.contains("HOAN_THANH") || s.contains("HOÀN THÀNH")) return "Hoàn thành";
        if (s.contains("DANG_SUA") || s.contains("ĐANG SỬA")) return "Đang sửa chữa";
        if (s.contains("CHO_SUA") || s.contains("CHỜ SỬA") || s.contains("DA_TIEP_NHAN")) return "Chờ sửa chữa";
        if (s.contains("DA_HUY") || s.contains("HỦY")) return "Đã hủy";
        return trangThai;
    }

    public String getMaPSC() { return maPSC; }
    public void setMaPSC(String maPSC) { this.maPSC = maPSC; }

    public String getMaPTN() { return maPTN; }
    public void setMaPTN(String maPTN) { this.maPTN = maPTN; }

    public String getBienSoXe() { return bienSoXe; }
    public void setBienSoXe(String bienSoXe) { this.bienSoXe = bienSoXe; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSoDT() { return soDT; }
    public void setSoDT(String soDT) { this.soDT = soDT; }

    public String getMaKTV() { return maKTV; }
    public void setMaKTV(String maKTV) { this.maKTV = maKTV; }

    public String getTenKTV() { return tenKTV; }
    public void setTenKTV(String tenKTV) { this.tenKTV = tenKTV; }

    public LocalDate getNgayBD() { return ngayBD; }
    public void setNgayBD(LocalDate ngayBD) { this.ngayBD = ngayBD; }

    public LocalDate getNgayHoanThanhDuKien() { return ngayHoanThanhDuKien; }
    public void setNgayHoanThanhDuKien(LocalDate ngayHoanThanhDuKien) { this.ngayHoanThanhDuKien = ngayHoanThanhDuKien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
