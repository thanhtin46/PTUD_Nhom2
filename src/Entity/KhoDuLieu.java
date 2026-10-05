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

    private int demTiepNhan = 3;
    private int demBaoHanh = 2;
    private int demNhap = 1;
    private int demXuat = 1;

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

    private void seed() {
        khachHangs.add(new KhachHang("KH001", "Nguyễn Văn A", "0901234567", "079090012345",
                "123 Lê Lợi, Q.1, TP.HCM", "nguyenvana@example.com", "Khách VIP, Thân thiết", "01/01/2023"));
        khachHangs.add(new KhachHang("KH002", "Trần Thị B", "0909676543", "080090012456",
                "456 Nguyễn Trãi, Q.5, TP.HCM", "tranthib@example.com", "", "15/02/2023"));
        khachHangs.add(new KhachHang("KH003", "Lê Văn C", "0912345678", "070090234567",
                "789 Cách Mạng Tháng 8, Q.3, TP.HCM", "levanc@example.com", "", "20/03/2023"));

        phuongTiens.add(new PhuongTien("51A-123.45", "Toyota", "Vios 2021", "WBA3A5C5EF0MVIK91",
                "EN123456", "Trắng", "2021", "45000", "KH001", "22/01/2024"));
        phuongTiens.add(new PhuongTien("59C-678.90", "Honda", "City", "RLHRE1820LY000123",
                "L15Z1234", "Đen", "2020", "62000", "KH002", "23/01/2024"));
        phuongTiens.add(new PhuongTien("60A-111.22", "Hyundai", "Accent", "MALBB51BLLM123456",
                "G4LC7890", "Bạc", "2019", "80000", "KH003", "15/11/2023"));

        phuTungs.add(new PhuTung("PT001", "Dầu nhớt 4L", "Chai", 20, 350000, 5));
        phuTungs.add(new PhuTung("PT002", "Lọc gió động cơ", "Cái", 35, 180000, 10));
        phuTungs.add(new PhuTung("PT003", "Má phanh trước", "Bộ", 3, 850000, 5));    // SẮP HẾT
        phuTungs.add(new PhuTung("PT004", "Bugi iridium", "Cái", 40, 220000, 8));
        phuTungs.add(new PhuTung("PT005", "Bộ dây curoa", "Bộ", 0, 1200000, 2));   // HẾT HÀNG

        phieuTiepNhans.add(new PhieuTiepNhan("TN001", "KH001", "Nguyễn Văn A", "0901234567",
                "51A-123.45", "Toyota", "Vios 2021", "22/01/2024",
                "Bảo dưỡng định kỳ", "Hoàn thành", "Nguyễn Văn Tiếp Tân", 1500000));
        phieuTiepNhans.add(new PhieuTiepNhan("TN002", "KH002", "Trần Thị B", "0909676543",
                "59C-678.90", "Honda", "City", "23/01/2024",
                "Kiểm tra phanh", "Đang sửa", "Nguyễn Văn Tiếp Tân", 0));
        phieuTiepNhans.add(new PhieuTiepNhan("TN003", "KH003", "Lê Văn C", "0912345678",
                "60A-111.22", "Hyundai", "Accent", "15/11/2023",
                "Thay dầu nhớt", "Chờ tiếp nhận", "Nguyễn Văn Tiếp Tân", 0));

        phieuBaoHanhs.add(new PhieuBaoHanh("BH001", "51A-123.45", "22/01/2024", "22/01/2025",
                "Nguyễn Văn Tiếp Tân", "Còn hạn", "Bảo hành sau bảo dưỡng"));
        phieuBaoHanhs.add(new PhieuBaoHanh("BH002", "59C-678.90", "23/01/2024", "23/07/2024",
                "Nguyễn Văn Tiếp Tân", "Hết hạn", "Bảo hành má phanh"));
    }
}
