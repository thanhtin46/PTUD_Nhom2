package Entity;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Lớp thực thể Nhân viên.
 */
public class NhanVien {
    public static final String DANG_LAM  = "DANG_LAM";
    public static final String NGHI_PHEP = "NGHI_PHEP";
    public static final String THU_VIEC  = "THU_VIEC";
    public static final String NGUNG     = "NGUNG";

    private String maNV;
    private String hoTen;
    private String taiKhoan;
    private String matKhau;
    private String vaiTro;
    private String trangThai;        // DANG_LAM | NGHI_PHEP | THU_VIEC | NGUNG
    private String[] cacQuyen;       // Danh sách quyền bổ sung
    private String soDT;
    private String email;
    private String bacNghe;          // Bậc nghề
    private String boPhan;           // Ban Cố Vấn Dịch Vụ, Xưởng Sửa Chữa...
    private String ngayVaoLam;

    public NhanVien() {
    }

    public NhanVien(String maNV) {
        this.maNV = maNV;
    }

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
        this(maNV, hoTen, taiKhoan, matKhau, vaiTro, trangThai, cacQuyen, null, null, null, null, null);
    }

    public NhanVien(String maNV, String hoTen,
                    String taiKhoan, String matKhau,
                    String vaiTro, String trangThai, String[] cacQuyen,
                    String soDT, String email, String bacNghe, String boPhan, String ngayVaoLam) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.taiKhoan = taiKhoan;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai == null ? DANG_LAM : trangThai;
        this.cacQuyen = cacQuyen;
        this.soDT = soDT;
        this.email = email;
        this.bacNghe = bacNghe;
        this.boPhan = boPhan;
        this.ngayVaoLam = ngayVaoLam;
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

    public boolean dangHoatDong() {
        if (trangThai == null || trangThai.trim().isEmpty()) return true;
        String tt = java.text.Normalizer.normalize(trangThai, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace("đ", "d").trim().toUpperCase(java.util.Locale.ROOT);
        if (tt.contains("NGUNG") || tt.contains("KHOA") || tt.contains("THOI") || tt.contains("THÔI")) {
            return false;
        }
        return true;
    }

    public String[] getCacQuyen() { return cacQuyen; }
    public void setCacQuyen(String[] cacQuyen) { this.cacQuyen = cacQuyen; }

    public String getSoDT() {
        if (soDT == null || soDT.trim().isEmpty()) {
            return "09" + String.format("%08d", Math.abs((maNV != null ? maNV.hashCode() : 12345678) % 100000000));
        }
        return soDT;
    }
    public void setSoDT(String soDT) { this.soDT = soDT; }

    public String getEmail() {
        if (email == null || email.trim().isEmpty()) {
            String uname = taiKhoan != null && !taiKhoan.isEmpty() ? taiKhoan.toLowerCase() : (maNV != null ? maNV.toLowerCase() : "nv");
            return uname + "@garaauto.vn";
        }
        return email;
    }
    public void setEmail(String email) { this.email = email; }

    public String getBacNghe() {
        if (bacNghe == null || bacNghe.trim().isEmpty()) {
            if (vaiTro == null) return "Thợ bậc 3/7";
            String vt = vaiTro.toLowerCase();
            if (vt.contains("quản lý") || vt.contains("admin")) return "Kỹ sư Ô tô ĐHBK";
            if (vt.contains("tiếp tân") || vt.contains("cố vấn")) return "Cử nhân Quản trị";
            if (vt.contains("thu ngân") || vt.contains("kế toán")) return "Cử nhân Kế toán";
            if (vt.contains("kho")) return "Trung cấp Vật tư";
            if (vt.contains("học việc") || vt.contains("thử việc")) return "Trung cấp Nghề";
            if (vt.contains("trưởng tổ") || vt.contains("ktv trưởng")) return "Thợ bậc 5/7";
            return "Thợ bậc 4/7";
        }
        return bacNghe;
    }
    public void setBacNghe(String bacNghe) { this.bacNghe = bacNghe; }

    public String getBoPhan() {
        if (boPhan == null || boPhan.trim().isEmpty()) {
            if (vaiTro == null) return "Xưởng Sửa Chữa";
            String vt = vaiTro.toLowerCase();
            if (vt.contains("quản lý") || vt.contains("admin")) return "Ban Điều Hành Gara";
            if (vt.contains("tiếp tân") || vt.contains("cố vấn")) return "Ban Cố Vấn Dịch Vụ";
            if (vt.contains("thu ngân") || vt.contains("kế toán")) return "Phòng Tài Chính - Kế Toán";
            if (vt.contains("kho")) return "Kho Vật Tư & Phụ Tùng";
            if (vt.contains("học việc") || vt.contains("thử việc")) return "Tổ Học Việc Kỹ Thuật";
            if (vt.contains("điện") || vt.contains("lạnh")) return "Tổ Điện - Điện Tử";
            return "Xưởng Sửa Chữa Lớn";
        }
        return boPhan;
    }
    public void setBoPhan(String boPhan) { this.boPhan = boPhan; }

    public String getNgayVaoLam() {
        if (ngayVaoLam == null || ngayVaoLam.trim().isEmpty()) {
            return "15/06/2021";
        }
        return ngayVaoLam;
    }
    public void setNgayVaoLam(String ngayVaoLam) { this.ngayVaoLam = ngayVaoLam; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhanVien nhanVien = (NhanVien) o;
        return Objects.equals(maNV, nhanVien.maNV);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maNV);
    }

    @Override
    public String toString() {
        if (hoTen != null && !hoTen.trim().isEmpty()) {
            return (maNV != null && !maNV.trim().isEmpty()) ? (maNV + " - " + hoTen) : hoTen;
        }
        return maNV != null ? maNV : "";
    }

    /** Kiểm tra có quyền truy cập 1 module không. */
    public boolean coQuyen(String maModule) {
        if (cacQuyen == null) return false;
        for (String q : cacQuyen) if (q.equals(maModule)) return true;
        return false;
    }

    /** Khởi tạo quyền mặc định theo vai trò (gọi 1 lần khi cần). */
    public void khoiTaoQuyenMacDinh() {
        if ("Quản lý".equals(vaiTro)) {
            cacQuyen = new String[]{"KH","PT","BH","SC","TD","PHUTUNG","NHAPKHO","XUATKHO","TONKHO","NV","PQ","HD","BCDT"};
        } else if ("Nhân viên tiếp tân".equals(vaiTro)) {
            cacQuyen = new String[]{"KH","PT","BH","HD"};
        } else if ("Nhân viên thu ngân".equals(vaiTro)) {
            cacQuyen = new String[]{"HD","BCDT","KH","PT"};
        } else if ("Nhân viên sửa chữa".equals(vaiTro)) {
            cacQuyen = new String[]{"SC","TD","TONKHO"};
        } else if ("Nhân viên kho".equals(vaiTro)) {
            cacQuyen = new String[]{"PHUTUNG","NHAPKHO","XUATKHO","TONKHO"};
        }
    }
}
