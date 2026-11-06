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

    /**
     * Tự động nhận diện và khôi phục chuỗi tiếng Việt bị lỗi mã hóa UTF-8 sang Windows-1252 (Mojibake).
     * Ví dụ: "Quáº£n LÃ½ Tá»•ng" -> "Quản Lý Tổng", "Quáº£n lÃ½" -> "Quản lý"
     */
    public static String giaiMaMojibake(String input) {
        if (input == null || input.isEmpty()) return input;
        if (!input.contains("Ã") && !input.contains("áº") && !input.contains("á»") &&
            !input.contains("Æ") && !input.contains("Ä") && !input.contains("Å") &&
            !input.contains("Â")) {
            return input;
        }
        try {
            byte[] bytes = input.getBytes(java.nio.charset.Charset.forName("Windows-1252"));
            String decoded = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            if (!decoded.contains("\uFFFD")) {
                return decoded;
            }
        } catch (Exception ignored) {}
        return input;
    }

    public static VaiTro vaiTro(String s) {
        if (s == null) return VaiTro.KHONG_RO;
        String raw = giaiMaMojibake(s).trim();
        String v = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ','d').replace('Đ','D').toLowerCase(Locale.ROOT).trim();
        VaiTro vt = switch(v) {
            case "quan_ly", "quan ly", "admin", "giam doc", "chu xuong", "quan tri vien" -> VaiTro.QUAN_LY;
            case "tiep_tan", "nhan vien tiep tan", "tiep tan", "le tan", "co van dich vu", "nhan vien tiep nhan", "tiep nhan" -> VaiTro.TIEP_TAN;
            case "sua_chua", "nhan vien sua chua", "ky thuat vien", "ktv", "nhan vien ky thuat" -> VaiTro.SUA_CHUA;
            case "kho", "nhan vien kho", "thu kho" -> VaiTro.KHO;
            case "thu_ngan", "nhan vien thu ngan", "thu ngan", "ke toan" -> VaiTro.THU_NGAN;
            default -> VaiTro.KHONG_RO;
        };
        if (vt != VaiTro.KHONG_RO) return vt;

        // Dự phòng nếu chuỗi vai trò có từ khóa viết tắt hoặc biến thể
        if (v.contains("quan") || v.contains("giam doc") || v.contains("admin")) return VaiTro.QUAN_LY;
        if (v.contains("tiep") || v.contains("tan") || v.contains("le tan")) return VaiTro.TIEP_TAN;
        if (v.contains("sua") || v.contains("chua") || v.contains("ktv") || v.contains("thuat")) return VaiTro.SUA_CHUA;
        if (v.contains("kho")) return VaiTro.KHO;
        if (v.contains("ngan") || v.contains("thu") || v.contains("toan")) return VaiTro.THU_NGAN;

        // Dự phòng thô dựa trên chuỗi gốc s (nếu chuỗi s vẫn còn dính mojibake chưa khử hết)
        String sLower = s.toLowerCase(Locale.ROOT);
        if (sLower.contains("quan") || sLower.contains("quáº") || sLower.contains("lÃ½")) return VaiTro.QUAN_LY;
        if (sLower.contains("tiep") || sLower.contains("tiáº¿p") || sLower.contains("tÃ¢n")) return VaiTro.TIEP_TAN;
        if (sLower.contains("sua") || sLower.contains("sá»") || sLower.contains("chá»¯a")) return VaiTro.SUA_CHUA;
        if (sLower.contains("kho")) return VaiTro.KHO;
        if (sLower.contains("ngan") || sLower.contains("ngÃ¢n")) return VaiTro.THU_NGAN;

        return VaiTro.KHONG_RO;
    }

    public static Set<Quyen> macDinh(String role) {
        return switch(vaiTro(role)) {
            case QUAN_LY -> EnumSet.allOf(Quyen.class);
            case TIEP_TAN -> EnumSet.of(Quyen.KH_XEM,Quyen.KH_SUA,Quyen.XE_XEM,Quyen.XE_SUA,Quyen.TIEP_NHAN,Quyen.LICH_SU,Quyen.BAO_HANH,Quyen.THANH_TOAN,Quyen.HD_XEM);
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
        String raw = giaiMaMojibake(s).trim();
        String v = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ','d').replace('Đ','D').toLowerCase(Locale.ROOT).trim();
        return switch(v) {
            case "trang chu", "tong quan" -> "Trang chủ";
            case "admin hub", "quan tri he thong (admin)" -> "Admin Hub";
            case "quan ly khach hang", "khach hang" -> "Quản lý khách hàng";
            case "tra cuu khach hang" -> "Tra cứu khách hàng";
            case "lich su sua chua" -> "Lịch sử sửa chữa";
            case "quan ly phuong tien", "phuong tien" -> "Quản lý phương tiện";
            case "tra cuu phuong tien" -> "Tra cứu phương tiện";
            case "lap phieu tiep nhan", "phieu tiep nhan" -> "Lập phiếu tiếp nhận";
            case "tra cuu phieu tiep nhan" -> "Tra cứu phiếu tiếp nhận";
            case "lap phieu sua chua", "phieu sua chua", "phan cong ktv" -> "Lập phiếu sửa chữa";
            case "cap nhat tien do" -> "Cập nhật tiến độ";
            case "dieu hanh sua chua" -> "Điều hành sửa chữa";
            case "tra cuu lich su sua chua" -> "Tra cứu lịch sử sửa chữa";
            case "lap hoa don", "hoa don", "ban le phu tung", "lap hoa don ban hang" -> "Lập hóa đơn";
            case "tra cuu hoa don" -> "Tra cứu hóa đơn";
            case "quan ly phu tung", "kho phu tung" -> "Quản lý phụ tùng";
            case "nhap kho" -> "Nhập kho";
            case "theo doi cho nhap" -> "Theo dõi chờ nhập";
            case "xuat kho" -> "Xuất kho";
            case "lich su nhap xuat kho" -> "Lịch sử nhập xuất kho";
            case "bao cao ton kho" -> "Báo cáo tồn kho";
            case "lap phieu bao hanh", "bao hanh" -> "Lập phiếu bảo hành";
            case "tra cuu phieu bao hanh" -> "Tra cứu phiếu bảo hành";
            case "quan ly nhan vien", "nhan vien" -> "Quản lý nhân viên";
            case "phan quyen", "phan quyen nguoi dung" -> "Phân quyền";
            case "thong ke doanh thu", "bao cao - thong ke", "bao cao doanh thu" -> "Thống kê doanh thu";
            case "xu huong doanh thu (duong)" -> "Xu hướng doanh thu (Đường)";
            default -> s.trim();
        };
    }

    public static final Map<String, Quyen> TRANG = Map.ofEntries(
            Map.entry("Admin Hub", Quyen.CAU_HINH),
            Map.entry("Quản lý khách hàng", Quyen.KH_XEM),
            Map.entry("Tra cứu khách hàng", Quyen.KH_XEM),
            Map.entry("Lịch sử sửa chữa", Quyen.LICH_SU),
            Map.entry("Quản lý phương tiện", Quyen.XE_XEM),
            Map.entry("Tra cứu phương tiện", Quyen.XE_XEM),
            Map.entry("Lập phiếu tiếp nhận", Quyen.TIEP_NHAN),
            Map.entry("Tra cứu phiếu tiếp nhận", Quyen.TIEP_NHAN),
            Map.entry("Lập phiếu sửa chữa", Quyen.SC),
            Map.entry("Cập nhật tiến độ", Quyen.TIEN_DO),
            Map.entry("Điều hành sửa chữa", Quyen.PHAN_CONG),
            Map.entry("Tra cứu lịch sử sửa chữa", Quyen.LICH_SU),
            Map.entry("Lập hóa đơn", Quyen.THANH_TOAN),
            Map.entry("Lập hóa đơn bán hàng", Quyen.THANH_TOAN),
            Map.entry("Tra cứu hóa đơn", Quyen.HD_XEM),
            Map.entry("Quản lý phụ tùng", Quyen.PHU_TUNG),
            Map.entry("Nhập kho", Quyen.NHAP),
            Map.entry("Theo dõi chờ nhập", Quyen.NHAP),
            Map.entry("Xuất kho", Quyen.XUAT),
            Map.entry("Lịch sử nhập xuất kho", Quyen.KHO_XEM),
            Map.entry("Báo cáo tồn kho", Quyen.KHO_XEM),
            Map.entry("Lập phiếu bảo hành", Quyen.BAO_HANH),
            Map.entry("Tra cứu phiếu bảo hành", Quyen.BAO_HANH),
            Map.entry("Quản lý nhân viên", Quyen.NHAN_VIEN),
            Map.entry("Phân quyền", Quyen.PHAN_QUYEN),
            Map.entry("Thống kê doanh thu", Quyen.BAO_CAO),
            Map.entry("Xu hướng doanh thu (Đường)", Quyen.BAO_CAO)
    );

    public static boolean moTrang(NhanVien n, String tieuDe) {
        String c = chuanTrang(tieuDe);
        if("Trang chủ".equals(c)) return true;
        Quyen q = TRANG.get(c);
        return q != null && co(n, q);
    }
}
