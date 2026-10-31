package Entity;

import java.io.Serializable;

/**
 * Lớp chứa thông tin thống kê số lượng phiếu sửa chữa của từng kỹ thuật viên.
 */
public class ThongKeKtvDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maKTV;
    private String tenKTV;
    private String chucVu;
    private int soPhieuDangPhuTrach; // Số phiếu chưa hoàn thành đang phụ trách
    private int soPhieuQuaHen;       // Số phiếu quá hẹn

    public ThongKeKtvDTO() {
    }

    public ThongKeKtvDTO(String maKTV, String tenKTV, String chucVu, int soPhieuDangPhuTrach, int soPhieuQuaHen) {
        this.maKTV = maKTV;
        this.tenKTV = tenKTV != null && !tenKTV.isBlank() ? tenKTV : "(Chưa có tên)";
        this.chucVu = chucVu != null && !chucVu.isBlank() ? chucVu : "Kỹ thuật viên";
        this.soPhieuDangPhuTrach = soPhieuDangPhuTrach;
        this.soPhieuQuaHen = soPhieuQuaHen;
    }

    public String getMaKTV() {
        return maKTV;
    }

    public void setMaKTV(String maKTV) {
        this.maKTV = maKTV;
    }

    public String getTenKTV() {
        return tenKTV;
    }

    public void setTenKTV(String tenKTV) {
        this.tenKTV = tenKTV;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public int getSoPhieuDangPhuTrach() {
        return soPhieuDangPhuTrach;
    }

    public void setSoPhieuDangPhuTrach(int soPhieuDangPhuTrach) {
        this.soPhieuDangPhuTrach = soPhieuDangPhuTrach;
    }

    public int getSoPhieuQuaHen() {
        return soPhieuQuaHen;
    }

    public void setSoPhieuQuaHen(int soPhieuQuaHen) {
        this.soPhieuQuaHen = soPhieuQuaHen;
    }

    @Override
    public String toString() {
        return maKTV + " - " + tenKTV;
    }
}
