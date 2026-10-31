package Entity;

import java.text.Normalizer;
import java.util.*;

/** Định nghĩa vai trò và quyền truy cập các chức năng trong hệ thống. */
public final class QuyenTruyCap {
    private QuyenTruyCap() {}
    public enum VaiTro { QUAN_LY, TIEP_TAN, SUA_CHUA, KHO, THU_NGAN, KHONG_RO }
    public enum Quyen {
        KH_XEM("Xem khách hàng"),
        KH_SUA("Thêm sửa khách hàng"),
        KH_XOA("Xóa khách hàng"),
        XE_XEM("Xem phương tiện"),
        XE_SUA("Thêm sửa phương tiện"),
        XE_XOA("Xóa phương tiện"),
        TIEP_NHAN("Tiếp nhận xe"),
        LICH_SU("Xem lịch sử sửa chữa"),
        SC("Lập sửa chữa"),
        TIEN_DO("Cập nhật tiến độ"),
        PHAN_CONG("Phân công và điều hành"),
        KHO_XEM("Xem kho"),
        PHU_TUNG("Quản lý phụ tùng"),
        NHAP("Nhập kho"),
        XUAT("Xuất kho"),
        THANH_TOAN("Thanh toán"),
        HD_XEM("Xem và in hóa đơn"),
        HD_HUY("Hủy hóa đơn"),
        BAO_HANH("Bảo hành"),
        BAO_CAO("Báo cáo quản trị"),
        NHAN_VIEN("Quản lý tài khoản"),
        PHAN_QUYEN("Phân quyền"),
        CAU_HINH("Cấu hình hệ thống");

        private final String tenHienThi;

        Quyen(String tenHienThi) { this.tenHienThi = tenHienThi; }
        public String getTenHienThi() { return tenHienThi; }
    }
    public static VaiTro vaiTro(String s) {
        String v = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ','d').toLowerCase(Locale.ROOT).trim();
        return switch(v) {
            case "quan_ly", "quan ly", "admin", "giam doc", "chu xuong" -> VaiTro.QUAN_LY;
            case "tiep_tan", "nhan vien tiep tan", "tiep tan", "le tan", "co van dich vu" -> VaiTro.TIEP_TAN;
            case "sua_chua", "nhan vien sua chua", "ky thuat vien", "ktv", "nhan vien ky thuat" -> VaiTro.SUA_CHUA;
            case "kho", "nhan vien kho", "thu kho" -> VaiTro.KHO;
            case "thu_ngan", "nhan vien thu ngan", "thu ngan", "ke toan" -> VaiTro.THU_NGAN;
            default -> VaiTro.KHONG_RO;
        };
    }
    public static Set<Quyen> macDinh(String role) {
        return switch(vaiTro(role)) {
            case QUAN_LY -> EnumSet.allOf(Quyen.class);
            case TIEP_TAN -> EnumSet.of(Quyen.KH_XEM,Quyen.KH_SUA,Quyen.XE_XEM,Quyen.XE_SUA,Quyen.TIEP_NHAN,Quyen.LICH_SU,Quyen.BAO_HANH);
            case SUA_CHUA -> EnumSet.of(Quyen.SC,Quyen.TIEN_DO,Quyen.LICH_SU,Quyen.KHO_XEM);
            case KHO -> EnumSet.of(Quyen.KHO_XEM,Quyen.PHU_TUNG,Quyen.NHAP,Quyen.XUAT);
            case THU_NGAN -> EnumSet.of(Quyen.KH_XEM,Quyen.XE_XEM,Quyen.HD_XEM,Quyen.THANH_TOAN);
            default -> EnumSet.noneOf(Quyen.class);
        };
    }
    public static boolean co(NhanVien n, Quyen q) {
        if(n==null || !n.dangHoatDong() || vaiTro(n.getVaiTro())==VaiTro.KHONG_RO)return false;
        if(macDinh(n.getVaiTro()).contains(q))return true;
        // Quản trị tài khoản và cấp quyền chỉ dành cho vai trò quản lý.
        if(q==Quyen.PHAN_QUYEN || q==Quyen.NHAN_VIEN || q==Quyen.CAU_HINH)return false;
        return n.getCacQuyen()!=null && Arrays.asList(n.getCacQuyen()).contains(q.name());
    }
    public static String chuanTrang(String s) {
        if(s==null)return "";
        return switch(s.trim()) {
            case "Quản trị hệ thống", "Admin Hub" -> "Quản trị hệ thống (Admin)";
            case "Khách hàng", "Danh sách khách hàng" -> "Quản lý khách hàng";
            case "Phương tiện", "Danh sách phương tiện" -> "Quản lý phương tiện";
            case "Phiếu tiếp nhận" -> "Lập phiếu tiếp nhận";
            case "Phiếu sửa chữa", "Sửa chữa" -> "Lập phiếu sửa chữa";
            case "Tra cứu phiếu sửa chữa" -> "Cập nhật tiến độ";
            case "Lịch sử sửa chữa", "Lịch sử sửa chữa của khách hàng", "Lịch sử sửa chữa người dùng", "Tra cứu lịch sử sửa chữa người dùng" -> "Tra cứu lịch sử sửa chữa";
            case "Hóa đơn", "Thanh toán hóa đơn" -> "Lập hóa đơn";
            case "Hóa đơn bán hàng", "Bán lẻ phụ tùng" -> "Lập hóa đơn bán hàng";
            case "Kho phụ tùng", "Danh sách phụ tùng" -> "Quản lý phụ tùng";
            case "Theo dõi lịch sử nhập xuất", "Lịch sử nhập xuất" -> "Lịch sử nhập xuất kho";
            case "Bảo hành" -> "Lập phiếu bảo hành";
            case "Tra cứu bảo hành", "Quản lý bảo hành" -> "Tra cứu phiếu bảo hành";
            case "Nhân viên" -> "Quản lý nhân viên";
            case "Tra cứu nhân viên", "Phân quyền người dùng" -> "Phân quyền";
            case "Báo cáo – Thống kê", "Báo cáo thống kê", "Báo cáo doanh thu", "Theo tháng", "Theo năm" -> "Thống kê doanh thu";
            case "Biểu đồ đường xu hướng" -> "Xu hướng doanh thu (Đường)";
            default -> s.trim();
        };
    }
    public static final Map<String,Quyen> TRANG;
    static {
        Map<String,Quyen> m=new LinkedHashMap<>();
        m.put("Quản trị hệ thống (Admin)",Quyen.CAU_HINH);
        m.put("Quản lý khách hàng",Quyen.KH_SUA);m.put("Tra cứu khách hàng",Quyen.KH_XEM);
        m.put("Quản lý phương tiện",Quyen.XE_SUA);m.put("Tra cứu phương tiện",Quyen.XE_XEM);
        m.put("Lập phiếu tiếp nhận",Quyen.TIEP_NHAN);m.put("Tra cứu phiếu tiếp nhận",Quyen.TIEP_NHAN);
        m.put("Tra cứu lịch sử sửa chữa",Quyen.LICH_SU);m.put("Lập phiếu sửa chữa",Quyen.SC);
        m.put("Cập nhật tiến độ",Quyen.TIEN_DO);m.put("Phân công KTV",Quyen.PHAN_CONG);m.put("Điều hành sửa chữa",Quyen.PHAN_CONG);
        m.put("Lập hóa đơn",Quyen.THANH_TOAN);m.put("Lập hóa đơn bán hàng",Quyen.THANH_TOAN);m.put("Tra cứu hóa đơn",Quyen.HD_XEM);
        m.put("Quản lý phụ tùng",Quyen.PHU_TUNG);m.put("Nhập kho",Quyen.NHAP);m.put("Theo dõi chờ nhập",Quyen.NHAP);
        m.put("Xuất kho",Quyen.XUAT);m.put("Báo cáo tồn kho",Quyen.KHO_XEM);m.put("Lịch sử nhập xuất kho",Quyen.KHO_XEM);
        m.put("Lập phiếu bảo hành",Quyen.BAO_HANH);m.put("Tra cứu phiếu bảo hành",Quyen.BAO_HANH);
        m.put("Quản lý nhân viên",Quyen.NHAN_VIEN);m.put("Phân quyền",Quyen.PHAN_QUYEN);
        m.put("Thống kê doanh thu",Quyen.BAO_CAO);m.put("Xu hướng doanh thu (Đường)",Quyen.BAO_CAO);
        TRANG=Collections.unmodifiableMap(m);
    }
    public static boolean moTrang(NhanVien n,String name) {
        String route=chuanTrang(name);
        if("Trang chủ".equals(route))return n!=null && n.dangHoatDong();
        Quyen q=TRANG.get(route);return q!=null && co(n,q);
    }
}
