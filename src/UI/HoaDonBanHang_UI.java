package UI;

import Entity.NhanVien;

/**
 * Màn hình lập hóa đơn bán lẻ phụ tùng.
 */
public class HoaDonBanHang_UI extends ThanhToanHoaDon_UI {
    public HoaDonBanHang_UI(NhanVien nv) {
        super(nv, ThanhToanHoaDon_UI.CHE_DO_BAN_LE);
    }

    public HoaDonBanHang_UI() {
        this(null);
    }
}
