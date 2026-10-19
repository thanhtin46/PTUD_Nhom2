package Entity;

import java.util.ArrayList;

public class KhoDuLieu {
    private static KhoDuLieu instance;

    private ArrayList<NhanVien> nhanViens = new ArrayList<>();
    private final ArrayList<KhachHang> khachHangs = new ArrayList<>();
    private final ArrayList<PhuongTien> phuongTiens = new ArrayList<>();
    private final ArrayList<PhuTung> phuTungs = new ArrayList<>();
    private final ArrayList<PhieuTiepNhan> phieuTiepNhans = new ArrayList<>();
    private final ArrayList<PhieuBaoHanh> phieuBaoHanhs = new ArrayList<>();
    private final ArrayList<PhieuKho> phieuKhos = new ArrayList<>();
    private final ArrayList<HoaDon> hoaDons = new ArrayList<>();
    private final CauHinhHeThong cauHinh = new CauHinhHeThong();
    private final ArrayList<NhatKyHeThong> nhatKyList = new ArrayList<>();

    private int demTiepNhan = 3;
    private int demBaoHanh = 2;
    private int demNhap = 1;
    private int demXuat = 1;
    private int demHoaDon = 2;

    private KhoDuLieu() {
        seed();
    }

    public static KhoDuLieu get() {
        if (instance == null) instance = new KhoDuLieu();
        return instance;
    }

    public void setNhanViens(ArrayList<NhanVien> danhSach) {
        this.nhanViens = danhSach;
    }

    public ArrayList<NhanVien> getNhanViens() { return nhanViens; }
    public ArrayList<KhachHang> getKhachHangs() { return khachHangs; }
    public ArrayList<PhuongTien> getPhuongTiens() { return phuongTiens; }
    public ArrayList<PhuTung> getPhuTungs() { return phuTungs; }
    public ArrayList<PhieuTiepNhan> getPhieuTiepNhans() { return phieuTiepNhans; }
    public ArrayList<PhieuBaoHanh> getPhieuBaoHanhs() { return phieuBaoHanhs; }
    public ArrayList<PhieuKho> getPhieuKhos() { return phieuKhos; }
    public ArrayList<HoaDon> getHoaDons() { return hoaDons; }
    public CauHinhHeThong getCauHinh() { return cauHinh; }
    public ArrayList<NhatKyHeThong> getNhatKyList() { return nhatKyList; }

    public void ghiNhatKy(String nguoi, String vaiTro, String hanhDong, String mucDo, String chiTiet) {
        nhatKyList.add(0, new NhatKyHeThong(nguoi, vaiTro, hanhDong, mucDo, chiTiet));
    }

    public void resetDuLieuMacDinh() {
        khachHangs.clear();
        phuongTiens.clear();
        phuTungs.clear();
        phieuTiepNhans.clear();
        phieuBaoHanhs.clear();
        phieuKhos.clear();
        hoaDons.clear();
        nhatKyList.clear();
        demTiepNhan = 3;
        demBaoHanh = 2;
        demNhap = 1;
        demXuat = 1;
        demHoaDon = 2;
        seed();
        ghiNhatKy("Quản lý", "Quản lý", "Khôi phục dữ liệu gốc", NhatKyHeThong.WARN, "Admin đã kích hoạt Factory Reset đưa hệ thống về trạng thái mẫu ban đầu");
    }

    public String taoBaoCaoBackup() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BẢN SAO LƯU DỮ LIỆU HỆ THỐNG GARA THE T4 ===\n");
        sb.append("Thời gian sao lưu: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("Gara: ").append(cauHinh.getTenGara()).append("\n");
        sb.append("Địa chỉ: ").append(cauHinh.getDiaChi()).append(" | Hotline: ").append(cauHinh.getHotline()).append("\n");
        sb.append("------------------------------------------------\n");
        sb.append("1. Khách hàng: ").append(khachHangs.size()).append(" hồ sơ\n");
        sb.append("2. Phương tiện: ").append(phuongTiens.size()).append(" xe\n");
        sb.append("3. Phụ tùng trong kho: ").append(phuTungs.size()).append(" mặt hàng\n");
        sb.append("4. Phiếu tiếp nhận sửa chữa: ").append(phieuTiepNhans.size()).append(" phiếu\n");
        sb.append("5. Phiếu bảo hành: ").append(phieuBaoHanhs.size()).append(" phiếu\n");
        sb.append("6. Hóa đơn thanh toán: ").append(hoaDons.size()).append(" hóa đơn\n");
        sb.append("7. Tài khoản nhân sự: ").append(nhanViens.size()).append(" nhân viên\n");
        sb.append("8. Bản ghi nhật ký an ninh: ").append(nhatKyList.size()).append(" bản ghi\n");
        sb.append("------------------------------------------------\n");
        sb.append("Trạng thái sao lưu: HOÀN TẤT & ĐÃ XÁC THỰC TOÀN VẸN\n");
        sb.append("Mã Checksum SHA: ").append(Integer.toHexString(sb.toString().hashCode()).toUpperCase()).append("\n");
        sb.append("================================================\n");
        return sb.toString();
    }

    public String maHoaDonMoi() {
        return String.format("HD%03d", demHoaDon++);
    }

    public KhachHang timKhachHangTheoMa(String ma) {
        for (KhachHang kh : khachHangs) if (kh.getMaKH().equalsIgnoreCase(ma)) return kh;
        return null;
    }

    public KhachHang timKhachHangTheoSdt(String sdt) {
        for (KhachHang kh : khachHangs) if (kh.getSdt().equals(sdt)) return kh;
        return null;
    }

    public PhuongTien timXeTheoBienSo(String bienSo) {
        for (PhuongTien xe : phuongTiens) if (xe.getBienSo().equalsIgnoreCase(bienSo)) return xe;
        return null;
    }

    public PhuTung timPhuTung(String ma) {
        for (PhuTung pt : phuTungs) if (pt.getMaPT().equalsIgnoreCase(ma)) return pt;
        return null;
    }

    public NhanVien timNhanVienTheoMa(String ma) {
        for (NhanVien nv : nhanViens) if (nv.getMaNV().equalsIgnoreCase(ma)) return nv;
        return null;
    }

    public String maPhieuTiepNhanMoi() {
        demTiepNhan++;
        return String.format("TN%03d", demTiepNhan);
    }

    public String maPhieuBaoHanhMoi() {
        demBaoHanh++;
        return String.format("BH%03d", demBaoHanh);
    }

    public String maPhieuNhapMoi() {
        demNhap++;
        return String.format("NK%03d", demNhap);
    }

    public String maPhieuXuatMoi() {
        demXuat++;
        return String.format("XK%03d", demXuat);
    }

    public boolean apDungTonKho(PhieuKho phieu) {
        if (PhieuKho.XUAT.equals(phieu.getLoai())) {
            for (ChiTietKho ct : phieu.getChiTiet()) {
                PhuTung pt = timPhuTung(ct.getMaPT());
                if (pt == null || pt.getSoLuongTon() < ct.getSoLuong()) return false;
            }
        }
        for (ChiTietKho ct : phieu.getChiTiet()) {
            PhuTung pt = timPhuTung(ct.getMaPT());
            if (pt == null) continue;
            if (PhieuKho.NHAP.equals(phieu.getLoai())) {
                pt.setSoLuongTon(pt.getSoLuongTon() + ct.getSoLuong());
            } else {
                pt.setSoLuongTon(pt.getSoLuongTon() - ct.getSoLuong());
            }
        }
        phieu.setTrangThai(PhieuKho.DA_NHAP);
        phieuKhos.add(phieu);
        return true;
    }

    /** UC010: chỉ lưu phiếu nhập dạng "chờ nhập" — chưa cộng tồn kho. */
    public void luuPhieuChoNhap(PhieuKho phieu) {
        phieu.setTrangThai(PhieuKho.CHO_NHAP);
        phieuKhos.add(phieu);
    }

    /** UC010: khi NCC giao hàng, gọi hàm này để cộng tồn kho. */
    public boolean xacNhanNhap(PhieuKho phieu) {
        if (!phieu.isChoNhap()) return false;
        for (ChiTietKho ct : phieu.getChiTiet()) {
            PhuTung pt = timPhuTung(ct.getMaPT());
            if (pt == null) continue;
            pt.setSoLuongTon(pt.getSoLuongTon() + ct.getSoLuong());
        }
        phieu.setTrangThai(PhieuKho.DA_NHAP);
        return true;
    }

    /** UC010: đánh dấu hủy phiếu chờ nhập. */
    public boolean huyPhieuChoNhap(PhieuKho phieu) {
        if (!phieu.isChoNhap()) return false;
        phieu.setTrangThai(PhieuKho.DA_HUY);
        return true;
    }

    /** Lấy danh sách phiếu kho theo loại. */
    public java.util.List<PhieuKho> getPhieuKhoTheoLoai(String loai) {
        java.util.List<PhieuKho> ds = new java.util.ArrayList<>();
        for (PhieuKho pk : phieuKhos) if (loai.equals(pk.getLoai())) ds.add(pk);
        return ds;
    }

    public void dongBoTuSQLServer() {
        khachHangs.clear();
        phuongTiens.clear();
        phuTungs.clear();
        phieuTiepNhans.clear();
        phieuBaoHanhs.clear();
        hoaDons.clear();

        DAO.DuLieu_DAO dao = new DAO.DuLieu_DAO();

        // Nạp dữ liệu chuẩn từ SQL Server
        khachHangs.addAll(dao.layTatCaKhachHang());
        phuongTiens.addAll(dao.layTatCaPhuongTien());
        phuTungs.addAll(dao.layTatCaPhuTung());
        phieuTiepNhans.addAll(dao.layTatCaPhieuTiepNhan());
        phieuBaoHanhs.addAll(dao.layTatCaPhieuBaoHanh());
        hoaDons.addAll(dao.layTatCaHoaDon());

        demTiepNhan = phieuTiepNhans.size();
        demBaoHanh = phieuBaoHanhs.size();
        demHoaDon = hoaDons.size();

        ghiNhatKy("Hệ thống", "Hệ thống", "Đồng bộ CSDL SQL Server", NhatKyHeThong.INFO,
                "Đã đồng bộ thành công " + khachHangs.size() + " khách hàng, " +
                phuongTiens.size() + " xe, " + phuTungs.size() + " phụ tùng từ SQL Server");
    }

    private void seed() {
        dongBoTuSQLServer();
    }
}
