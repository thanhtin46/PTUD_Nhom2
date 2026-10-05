package Entity;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Nhân viên của gara.
 * - Có vaiTro để phân quyền: "Nhân viên tiếp tân" / "Nhân viên sửa chữa"
 *   / "Nhân viên kho" / "Quản lý".
 * - UC013: thay vì xóa cứng, hỗ trợ "vô hiệu hóa" tài khoản qua
 *   trangThai = "NGUNG" (giữ lại lịch sử). "DANG_LAM" = đang hoạt động.
 * - UC014: cacQuyen = danh sách mã module được phép truy cập.
 */
public class NhanVien {
    public static final String DANG_LAM = "DANG_LAM";
    public static final String NGUNG    = "NGUNG";

    private String maNV;
    private String hoTen;
    private String taiKhoan;
    private String matKhau;
    private String vaiTro;
    private String trangThai;        // DANG_LAM | NGUNG
    private String[] cacQuyen;       // UC014: mã module, vd {"KH","PT","BH"}

    public NhanVien(String maNV, String hoTen,
                    String taiKhoan, String matKhau,
                    String vaiTro) {
        this(maNV, hoTen, taiKhoan, matKhau, vaiTro, DANG_LAM, null);
    }

    public NhanVien(String maNV, String hoTen,
                    String taiKhoan, String matKhau,
                    String vaiTro, String trangThai) {
        this(maNV, hoTen, taiKhoan, matKhau, vaiTro, trangThai, null);
    }

    public NhanVien(String maNV, String hoTen,
                    String taiKhoan, String matKhau,
                    String vaiTro, String trangThai, String[] cacQuyen) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.taiKhoan = taiKhoan;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai == null ? DANG_LAM : trangThai;
        this.cacQuyen = cacQuyen;
    }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getTaiKhoan() { return taiKhoan; }
    public void setTaiKhoan(String taiKhoan) { this.taiKhoan = taiKhoan; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean dangHoatDong() { return DANG_LAM.equals(trangThai); }

    public String[] getCacQuyen() { return cacQuyen; }
    public void setCacQuyen(String[] cacQuyen) { this.cacQuyen = cacQuyen; }

    /** Kiểm tra có quyền truy cập 1 module không. */
    public boolean coQuyen(String maModule) {
        if (cacQuyen == null) return false;
        for (String q : cacQuyen) if (q.equals(maModule)) return true;
        return false;
    }

    /** Khởi tạo quyền mặc định theo vai trò (gọi 1 lần khi cần). */
    public void khoiTaoQuyenMacDinh() {
        if ("Quản lý".equals(vaiTro)) {
            cacQuyen = new String[]{"KH","PT","BH","SC","TD","PHUTUNG","NHAPKHO","XUATKHO","TONKHO","NV","PQ","BCDT"};
        } else if ("Nhân viên tiếp tân".equals(vaiTro)) {
            cacQuyen = new String[]{"KH","PT","BH"};
        } else if ("Nhân viên sửa chữa".equals(vaiTro)) {
            cacQuyen = new String[]{"SC","TD","TONKHO"};
        } else if ("Nhân viên kho".equals(vaiTro)) {
            cacQuyen = new String[]{"PHUTUNG","NHAPKHO","XUATKHO","TONKHO"};
        }
    }
}
