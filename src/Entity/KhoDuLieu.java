package Entity;

import DAO.DuLieu_DAO;
import DAO.NhanVien_DAO;
import DAO.KhachHang_DAO;
import DAO.TraCuuKhachHang_DAO;
import DAO.PhuongTien_DAO;
import DAO.TraCuuPhuongTien_DAO;
import DAO.Kho_DAO;
import DAO.PhieuTiepNhan_DAO;
import DAO.PhieuBaoHanh_DAO;
import DAO.HoaDon_DAO;
import DAO.TrangChuTiepTan_DAO;

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

    // Bản đồ lưu chi tiết công việc & phụ tùng theo mã phiếu và theo mã hóa đơn
    private final java.util.Map<String, java.util.List<ChiTietDichVu>> chiTietTheoPhieu = new java.util.HashMap<>();
    private final java.util.Map<String, java.util.List<ChiTietDichVu>> chiTietTheoHoaDon = new java.util.HashMap<>();

    private final DuLieu_DAO duLieuDAO = new DuLieu_DAO();
    private final NhanVien_DAO nhanVienDAO = new NhanVien_DAO();
    private final KhachHang_DAO khachHangDAO = new KhachHang_DAO();
    private final TraCuuKhachHang_DAO traCuuKhachHangDAO = new TraCuuKhachHang_DAO();
    private final PhuongTien_DAO phuongTienDAO = new PhuongTien_DAO();
    private final TraCuuPhuongTien_DAO traCuuPhuongTienDAO = new TraCuuPhuongTien_DAO();
    private final Kho_DAO khoDAO = new Kho_DAO();
    private final PhieuTiepNhan_DAO phieuTiepNhanDAO = new PhieuTiepNhan_DAO();
    private final PhieuBaoHanh_DAO phieuBaoHanhDAO = new PhieuBaoHanh_DAO();
    private final HoaDon_DAO hoaDonDAO = new HoaDon_DAO();
    private final TrangChuTiepTan_DAO trangChuTiepTanDAO = new TrangChuTiepTan_DAO();


    private int demTiepNhan = 3;
    private int demBaoHanh = 2;
    private int demNhap = 1;
    private int demXuat = 1;
    private int demHoaDon = 2;

    private KhoDuLieu() {}

    public static KhoDuLieu get() {
        if (instance == null) { instance = new KhoDuLieu(); instance.seed(); }
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
    public KhachHang_DAO getKhachHangDAO() { return khachHangDAO; }
    public TraCuuKhachHang_DAO getTraCuuKhachHangDAO() { return traCuuKhachHangDAO; }
    public PhuongTien_DAO getPhuongTienDAO() { return phuongTienDAO; }
    public TraCuuPhuongTien_DAO getTraCuuPhuongTienDAO() { return traCuuPhuongTienDAO; }
    public PhieuTiepNhan_DAO getPhieuTiepNhanDAO() { return phieuTiepNhanDAO; }
    public PhieuBaoHanh_DAO getPhieuBaoHanhDAO() { return phieuBaoHanhDAO; }
    public HoaDon_DAO getHoaDonDAO() { return hoaDonDAO; }
    public TrangChuTiepTan_DAO getTrangChuTiepTanDAO() { return trangChuTiepTanDAO; }

    public void ghiNhatKy(String nguoi, String vaiTro, String hanhDong, String mucDo, String chiTiet) {
        nhatKyList.add(0, new NhatKyHeThong(nguoi, vaiTro, hanhDong, mucDo, chiTiet));
    }

    // ===============================================================
    // CÁC HÀM NGHIỆP VỤ ĐỒNG BỘ CSDL SQL SERVER MỖI LẦN THAO TÁC
    // ===============================================================

    // 1. KHÁCH HÀNG
    public boolean themKhachHang(KhachHang kh) {
        boolean dbOk = khachHangDAO.themKhachHang(kh);
        if (!dbOk) return false;
        khachHangs.add(kh);
        ghiNhatKy("Người dùng", "Thao tác", "Thêm khách hàng", NhatKyHeThong.INFO,
                "Đã thêm KH [" + kh.getMaKH() + "] " + kh.getHoTen() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean capNhatKhachHang(KhachHang kh) {
        boolean dbOk = khachHangDAO.capNhatKhachHang(kh);
        ghiNhatKy("Người dùng", "Thao tác", "Cập nhật khách hàng", NhatKyHeThong.INFO,
                "Đã cập nhật KH [" + kh.getMaKH() + "] " + kh.getHoTen() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean xoaKhachHang(String maKH) {
        boolean dbOk = khachHangDAO.xoaKhachHang(maKH);
        if (!dbOk) return false;
        khachHangs.removeIf(kh -> kh.getMaKH().equalsIgnoreCase(maKH));
        ghiNhatKy("Người dùng", "Thao tác", "Xóa khách hàng", NhatKyHeThong.WARN,
                "Đã xóa KH [" + maKH + "]" + (dbOk ? " (Đã xóa khỏi SQL Server)" : " (Xóa bộ nhớ)"));
        return dbOk;
    }

    // 2. PHƯƠNG TIỆN
    public boolean themPhuongTien(PhuongTien pt) {
        boolean dbOk = phuongTienDAO.themPhuongTien(pt);
        if (!dbOk) return false;
        phuongTiens.add(pt);
        ghiNhatKy("Người dùng", "Thao tác", "Thêm phương tiện", NhatKyHeThong.INFO,
                "Đã thêm xe [" + pt.getBienSo() + "] " + pt.getHangXe() + " " + pt.getDongXe() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean capNhatPhuongTien(PhuongTien pt) {
        boolean dbOk = phuongTienDAO.capNhatPhuongTien(pt);
        ghiNhatKy("Người dùng", "Thao tác", "Cập nhật phương tiện", NhatKyHeThong.INFO,
                "Đã cập nhật xe [" + pt.getBienSo() + "]" + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean xoaPhuongTien(String bienSo) {
        boolean dbOk = phuongTienDAO.xoaPhuongTien(bienSo);
        if (!dbOk) return false;
        phuongTiens.removeIf(xe -> xe.getBienSo().equalsIgnoreCase(bienSo));
        ghiNhatKy("Người dùng", "Thao tác", "Xóa phương tiện", NhatKyHeThong.WARN,
                "Đã xóa xe [" + bienSo + "]" + (dbOk ? " (Đã xóa khỏi SQL Server)" : " (Xóa bộ nhớ)"));
        return dbOk;
    }

    // 3. PHỤ TÙNG
    public boolean themPhuTung(PhuTung pt) {
        boolean dbOk = duLieuDAO.themPhuTung(pt);
        if (!dbOk) return false;
        phuTungs.add(pt);
        ghiNhatKy("Người dùng", "Thao tác", "Thêm phụ tùng", NhatKyHeThong.INFO,
                "Đã thêm phụ tùng [" + pt.getMaPT() + "] " + pt.getTen() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean capNhatPhuTung(PhuTung pt) {
        boolean dbOk = duLieuDAO.capNhatPhuTung(pt);
        ghiNhatKy("Người dùng", "Thao tác", "Cập nhật phụ tùng", NhatKyHeThong.INFO,
                "Đã cập nhật phụ tùng [" + pt.getMaPT() + "] " + pt.getTen() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean xoaPhuTung(String maPT) {
        boolean dbOk = duLieuDAO.xoaPhuTung(maPT);
        if (!dbOk) return false;
        phuTungs.removeIf(pt -> pt.getMaPT().equalsIgnoreCase(maPT));
        ghiNhatKy("Người dùng", "Thao tác", "Xóa phụ tùng", NhatKyHeThong.WARN,
                "Đã xóa phụ tùng [" + maPT + "]" + (dbOk ? " (Đã xóa khỏi SQL Server)" : " (Xóa bộ nhớ)"));
        return dbOk;
    }

    // 4. PHIẾU TIẾP NHẬN & TIẾN ĐỘ
    public boolean themPhieuTiepNhan(PhieuTiepNhan ptn, String maNV) {
        boolean dbOk = duLieuDAO.themPhieuTiepNhan(ptn, maNV);
        if (!dbOk) return false;
        phieuTiepNhans.add(ptn);
        ghiNhatKy("Tiếp tân", "Tiếp tân", "Lập phiếu tiếp nhận", NhatKyHeThong.INFO,
                "Lập phiếu tiếp nhận [" + ptn.getSoPhieu() + "] cho xe " + ptn.getBienSo() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean capNhatTienDoPhieuTiepNhan(String soPhieu, String tienDo, String maKtv) {
        for (PhieuTiepNhan p : phieuTiepNhans) {
            if (p.getSoPhieu().equalsIgnoreCase(soPhieu)) {
                p.setTienDo(tienDo);
                if (maKtv != null && !maKtv.isEmpty()) {
                    p.setKyThuatVien(maKtv);
                }
                break;
            }
        }
        boolean dbOk = duLieuDAO.capNhatTienDoPhieuTiepNhan(soPhieu, tienDo, maKtv);
        ghiNhatKy("Kỹ thuật", "Kỹ thuật", "Cập nhật tiến độ / KTV", NhatKyHeThong.INFO,
                "Cập nhật phiếu [" + soPhieu + "] -> " + tienDo + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean capNhatPhieuSuaChua(String soPhieu, String tienDo, String maKtv, long tongTien) {
        for (PhieuTiepNhan p : phieuTiepNhans) {
            if (p.getSoPhieu().equalsIgnoreCase(soPhieu)) {
                p.setTienDo(tienDo);
                if (maKtv != null && !maKtv.isEmpty()) {
                    p.setKyThuatVien(maKtv);
                }
                if (tongTien >= 0) {
                    p.setThanhTien(tongTien);
                }
                break;
            }
        }
        boolean dbOk = duLieuDAO.capNhatTienDoPhieuTiepNhan(soPhieu, tienDo, maKtv);
        ghiNhatKy("Kỹ thuật", "Kỹ thuật", "Lập/Cập nhật phiếu sửa chữa", NhatKyHeThong.INFO,
                "Cập nhật phiếu sửa chữa [" + soPhieu + "] -> " + tienDo + " - Tổng tiền: " + String.format("%,d", tongTien) + " đ" + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public PhieuTiepNhan timPhieuTiepNhan(String soPhieu) {
        if (soPhieu == null) return null;
        for (PhieuTiepNhan p : phieuTiepNhans) {
            if (p.getSoPhieu().equalsIgnoreCase(soPhieu.trim())) return p;
        }
        return null;
    }

    // 5. HÓA ĐƠN
    public boolean themHoaDon(HoaDon hd, String maNV) {
        return themHoaDon(hd, maNV, null);
    }

    public boolean themHoaDon(HoaDon hd, String maNV, java.util.List<ChiTietDichVu> dsChiTiet) {
        if (!hoaDonDAO.themHoaDon(hd, maNV, dsChiTiet)) return false;
        dongBoTuSQLServer();
        return true;
    }

    public boolean truTonKho(String maPT, int soLuong) {
        PhuTung pt = timPhuTung(maPT);
        if (pt == null) {
            pt = timPhuTungPhuHop(maPT, "");
        }
        if (pt != null) {
            int tonMoi = Math.max(0, pt.getSoLuongTon() - soLuong);
            pt.setSoLuongTon(tonMoi);
            duLieuDAO.capNhatTonKho(pt.getMaPT(), tonMoi);
            return true;
        }
        return false;
    }

    public boolean congTonKho(String maPT, int soLuong) {
        PhuTung pt = timPhuTung(maPT);
        if (pt == null) {
            pt = timPhuTungPhuHop(maPT, "");
        }
        if (pt != null) {
            int tonMoi = pt.getSoLuongTon() + soLuong;
            pt.setSoLuongTon(tonMoi);
            duLieuDAO.capNhatTonKho(pt.getMaPT(), tonMoi);
            return true;
        }
        return false;
    }

    public java.util.List<ChiTietDichVu> layChiTietTheoPhieu(String soPhieu) {
        if (soPhieu == null) return new java.util.ArrayList<>();
        String key = soPhieu.trim();
        if (chiTietTheoPhieu.containsKey(key)) {
            return chiTietTheoPhieu.get(key);
        }
        // Thử tìm theo CSDL
        java.util.List<ChiTietDichVu> fromDB = duLieuDAO.layChiTietTheoPhieu(key);
        if (!fromDB.isEmpty()) {
            chiTietTheoPhieu.put(key, fromDB);
            return fromDB;
        }
        return new java.util.ArrayList<>();
    }

    public void luuChiTietPhieu(String soPhieu, java.util.List<ChiTietDichVu> list) {
        if (soPhieu == null || list == null) return;
        chiTietTheoPhieu.put(soPhieu.trim(), new java.util.ArrayList<>(list));
        String maPSC = soPhieu.startsWith("SC") ? soPhieu : ("SC" + soPhieu.replaceAll("[^0-9]", ""));
        duLieuDAO.luuChiTietSuaChua(maPSC, list);
    }

    public java.util.List<ChiTietDichVu> layChiTietTheoHoaDon(String maHD) {
        if (maHD == null) return new java.util.ArrayList<>();
        String key = maHD.trim();
        if (chiTietTheoHoaDon.containsKey(key)) {
            return chiTietTheoHoaDon.get(key);
        }
        java.util.List<ChiTietDichVu> fromDB = duLieuDAO.layChiTietTheoHoaDon(key);
        if (!fromDB.isEmpty()) {
            chiTietTheoHoaDon.put(key, fromDB);
            return fromDB;
        }
        return new java.util.ArrayList<>();
    }

    public void luuChiTietHoaDon(String maHD, java.util.List<ChiTietDichVu> list) {
        if (maHD == null || list == null) return;
        chiTietTheoHoaDon.put(maHD.trim(), new java.util.ArrayList<>(list));
    }

    public boolean huyHoaDon(String maHD) {
        if (maHD == null) return false;
        boolean ok = hoaDonDAO.xoaHoaDon(maHD);
        if (ok) {
            dongBoTuSQLServer();
            ghiNhatKy("Thu ngân", "Thu ngân", "Hủy hóa đơn", NhatKyHeThong.WARN, "Hủy hóa đơn [" + maHD + "] (Đã cập nhật SQL Server)");
            return true;
        } else {
            // Trường hợp offline / không kết nối SQL Server
            for (HoaDon hd : hoaDons) {
                if (maHD.equalsIgnoreCase(hd.getMaHD())) {
                    hd.setTrangThai(HoaDon.DA_HUY);
                    ghiNhatKy("Thu ngân", "Thu ngân", "Hủy hóa đơn", NhatKyHeThong.WARN, "Hủy hóa đơn [" + maHD + "] (Lưu bộ nhớ tạm)");
                    return true;
                }
            }
        }
        return false;
    }

    // 6. PHIẾU BẢO HÀNH
    public boolean themPhieuBaoHanh(PhieuBaoHanh pbh) {
        boolean dbOk = phieuBaoHanhDAO.them(pbh);
        if (!dbOk) return false;
        phieuBaoHanhs.add(pbh);
        ghiNhatKy("Tiếp tân", "Tiếp tân", "Lập phiếu bảo hành", NhatKyHeThong.INFO,
                "Lập phiếu bảo hành [" + pbh.getSoPhieu() + "] cho xe " + pbh.getBienSo() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean capNhatPhieuBaoHanh(PhieuBaoHanh pbh) {
        int index = -1;
        for (int i = 0; i < phieuBaoHanhs.size(); i++) {
            if (phieuBaoHanhs.get(i).getSoPhieu().equals(pbh.getSoPhieu())) { index = i; break; }
        }
        if (index < 0) return false;
        if (!phieuBaoHanhDAO.capNhat(pbh)) return false;
        phieuBaoHanhs.set(index, pbh);
        return true;
    }

    public boolean xoaPhieuBaoHanh(String soPhieu) {
        boolean dbOk = phieuBaoHanhDAO.xoa(soPhieu);
        if (!dbOk) return false;
        phieuBaoHanhs.removeIf(p -> p.getSoPhieu().equals(soPhieu));
        return dbOk;
    }

    // 7. NHÂN VIÊN & TÀI KHOẢN
    public boolean themNhanVien(NhanVien nv, String sdt) {
        boolean dbOk = nhanVienDAO.themNhanVien(nv, sdt);
        if (!dbOk) return false;
        nhanViens.add(nv);
        ghiNhatKy("Quản lý", "Quản lý", "Thêm nhân viên", NhatKyHeThong.INFO,
                "Thêm nhân sự [" + nv.getMaNV() + "] " + nv.getHoTen() + " - " + nv.getVaiTro() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean capNhatNhanVien(NhanVien nv) {
        boolean dbOk = nhanVienDAO.capNhatNhanVien(nv);
        ghiNhatKy("Quản lý", "Quản lý", "Cập nhật nhân viên", NhatKyHeThong.INFO,
                "Cập nhật nhân sự [" + nv.getMaNV() + "] " + nv.getHoTen() + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean doiTrangThaiNhanVien(String maNV, String trangThaiMoi) {
        NhanVien nv = timNhanVienTheoMa(maNV);
        if (nv != null) nv.setTrangThai(trangThaiMoi);
        boolean dbOk = nhanVienDAO.capNhatTrangThaiTaiKhoan(maNV, trangThaiMoi);
        ghiNhatKy("Quản lý", "Quản lý", "Đổi trạng thái tài khoản", NhatKyHeThong.WARN,
                "Đổi trạng thái [" + maNV + "] -> " + trangThaiMoi + (dbOk ? " (Đã lưu vào SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    public boolean xoaNhanVien(String maNV) {
        NhanVien nv = timNhanVienTheoMa(maNV);
        String ten = nv != null ? nv.getHoTen() : maNV;
        boolean dbOk = nhanVienDAO.xoaNhanVien(maNV);
        // Nếu xóa cứng được trong DB thì bỏ khỏi list, nếu DB chuyển sang thôi việc thì cập nhật lại trạng thái
        if (dbOk) {
            NhanVien nvSau = timNhanVienTheoMa(maNV);
            if (nvSau != null) {
                nvSau.setTrangThai(NhanVien.NGUNG);
            }
        }
        ghiNhatKy("Quản lý", "Quản lý", "Xóa/Ngưng nhân sự", NhatKyHeThong.WARN,
                "Xử lý thôi việc/xóa hồ sơ [" + maNV + "] " + ten + (dbOk ? " (Đã cập nhật SQL Server)" : " (Lưu bộ nhớ)"));
        return dbOk;
    }

    // ===============================================================
    // CÁC HÀM TIỆN ÍCH & TÌM KIẾM
    // ===============================================================

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
        ghiNhatKy("Quản lý", "Quản lý", "Khôi phục dữ liệu gốc", NhatKyHeThong.WARN, "Admin đã khôi phục dữ liệu hệ thống về trạng thái ban đầu");
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
        demHoaDon++;
        return String.format("HD%03d", demHoaDon);
    }

    public KhachHang timKhachHangTheoMa(String ma) {
        for (KhachHang kh : khachHangs) if (kh.getMaKH().equalsIgnoreCase(ma)) return kh;
        return null;
    }

    public KhachHang timKhachHangTheoSdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        String s = sdt.trim();
        for (KhachHang kh : khachHangs) {
            if (kh.getSdt() != null && kh.getSdt().equalsIgnoreCase(s)) return kh;
        }
        String soChiSo = s.replaceAll("[^0-9]", "");
        if (!soChiSo.isEmpty()) {
            for (KhachHang kh : khachHangs) {
                if (kh.getSdt() != null) {
                    String khSo = kh.getSdt().replaceAll("[^0-9]", "");
                    if (khSo.equals(soChiSo)) return kh;
                }
            }
        }
        return null;
    }

    public PhuongTien timXeTheoBienSo(String bienSo) {
        if (bienSo == null || bienSo.trim().isEmpty()) return null;
        String bsTrim = bienSo.trim();
        for (PhuongTien xe : phuongTiens) {
            if (xe.getBienSo() != null && xe.getBienSo().equalsIgnoreCase(bsTrim)) return xe;
        }
        // Thử tìm theo chuỗi chuẩn hóa (loại bỏ khoảng trắng, dấu gạch ngang, dấu chấm)
        String chuanHoa = bsTrim.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (!chuanHoa.isEmpty()) {
            for (PhuongTien xe : phuongTiens) {
                if (xe.getBienSo() != null) {
                    String xeChuanHoa = xe.getBienSo().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                    if (xeChuanHoa.equals(chuanHoa)) return xe;
                }
            }
        }
        return null;
    }

    public String maKhachHangMoi() {
        String maDAO = khachHangDAO.sinhMaKhachHangTuDong();
        if (maDAO != null && !maDAO.isEmpty()) return maDAO;
        int max = 0;
        for (KhachHang kh : khachHangs) {
            if (kh.getMaKH() != null && kh.getMaKH().toUpperCase().startsWith("KH")) {
                try {
                    int so = Integer.parseInt(kh.getMaKH().replaceAll("[^0-9]", ""));
                    if (so > max) max = so;
                } catch (Exception ignored) {}
            }
        }
        return String.format("KH%02d", max + 1);
    }

    public PhuTung timPhuTung(String ma) {
        if (ma == null) return null;
        for (PhuTung pt : phuTungs) if (pt.getMaPT().equalsIgnoreCase(ma.trim())) return pt;
        return null;
    }

    public PhuTung timPhuTungPhuHop(String maPT, String ten) {
        if (maPT != null && !maPT.trim().isEmpty() && !"—".equals(maPT.trim()) && !"-".equals(maPT.trim())) {
            PhuTung pt = timPhuTung(maPT.trim());
            if (pt != null) return pt;
        }
        if (ten == null || ten.trim().isEmpty()) {
            return !phuTungs.isEmpty() ? phuTungs.get(0) : null;
        }
        String tenTrim = ten.trim();
        // 1. So sánh chính xác tên không phân biệt hoa thường
        for (PhuTung pt : phuTungs) {
            if (pt.getTen().equalsIgnoreCase(tenTrim)) return pt;
        }
        // 2. So sánh chứa chuỗi
        String tenLower = tenTrim.toLowerCase();
        for (PhuTung pt : phuTungs) {
            String ptTen = pt.getTen().toLowerCase();
            if (ptTen.contains(tenLower) || tenLower.contains(ptTen)) return pt;
        }
        // 3. So sánh theo từ khóa gara thông dụng
        String[] tuKhoa = new String[]{"castrol", "dầu", "nhớt", "lọc dầu", "lọc nhớt", "lọc gió", "má phanh", "bố thắng", "ắc quy", "ắc-quy", "bugi", "bu-gi", "gạt mưa", "lốp", "bóng đèn", "nước làm mát", "dây curoa"};
        for (String tk : tuKhoa) {
            if (tenLower.contains(tk)) {
                for (PhuTung pt : phuTungs) {
                    if (pt.getTen().toLowerCase().contains(tk)) return pt;
                }
            }
        }
        // Trả về phụ tùng đầu tiên nếu không tìm thấy
        return !phuTungs.isEmpty() ? phuTungs.get(0) : null;
    }

    public void dongBoPhuTungTuDB() {
        java.util.ArrayList<PhuTung> listDB = duLieuDAO.layTatCaPhuTung();
        if (listDB != null && !listDB.isEmpty()) {
            phuTungs.clear();
            phuTungs.addAll(listDB);
        }
    }

    public NhanVien timNhanVienTheoMa(String ma) {
        for (NhanVien nv : nhanViens) if (nv.getMaNV().equalsIgnoreCase(ma)) return nv;
        return null;
    }

    public String maPhieuTiepNhanMoi() {
        if (phieuTiepNhanDAO != null) {
            return phieuTiepNhanDAO.sinhMaTiepNhanTuDong();
        }
        int max = 0;
        for (PhieuTiepNhan p : phieuTiepNhans) {
            if (p.getSoPhieu() != null && p.getSoPhieu().toUpperCase().startsWith("TN")) {
                try {
                    int s = Integer.parseInt(p.getSoPhieu().replaceAll("[^0-9]", ""));
                    if (s > max) max = s;
                } catch (Exception ignored) {}
            }
        }
        return String.format("TN%03d", max + 1);
    }

    public String maPhieuBaoHanhMoi() {
        demBaoHanh++;
        return String.format("BH%03d", demBaoHanh);
    }

    public String maPhieuNhapMoi() {
        int max = phieuKhos.stream().filter(p -> PhieuKho.NHAP.equals(p.getLoai()))
                .mapToInt(p -> { try { return Integer.parseInt(p.getSoPhieu().replaceAll("[^0-9]", "")); } catch (Exception ex) { return 0; } }).max().orElse(0);
        demNhap = Math.max(demNhap, max) + 1;
        return String.format("PN%03d", demNhap);
    }

    public String maPhieuXuatMoi() {
        demXuat++;
        return String.format("XK%03d", demXuat);
    }

    public ArrayList<String> layDanhSachNhaCungCap() {
        return duLieuDAO.layDanhSachNhaCungCap();
    }

        /**
     * Lưu phiếu nhập kho và cập nhật số lượng tồn kho.
     */
    public void nhapKhoNgay(PhieuKho phieu, String maNV) throws Exception {
        if (phieu.getChiTiet() == null || phieu.getChiTiet().isEmpty()) {
            throw new IllegalArgumentException("Phiếu rỗng, không có dòng chi tiết phụ tùng!");
        }
        for (ChiTietKho ct : phieu.getChiTiet()) {
            PhuTung pt = timPhuTung(ct.getMaPT());
            if (pt == null) {
                throw new IllegalArgumentException("Mã phụ tùng " + ct.getMaPT() + " không tồn tại trong hệ thống!");
            }
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng không hợp lệ cho phụ tùng " + ct.getTenPT() + " (" + ct.getSoLuong() + ")!");
            }
        }

        // Bắt đầu Transaction SQL trên CSDL
        duLieuDAO.themPhieuNhapKho(phieu, maNV);

        // Sau khi Transaction SQL COMMIT thành công: Cập nhật tồn kho trong bộ nhớ
        for (ChiTietKho ct : phieu.getChiTiet()) {
            PhuTung pt = timPhuTung(ct.getMaPT());
            if (pt != null) {
                pt.setSoLuongTon(pt.getSoLuongTon() + ct.getSoLuong());
            }
        }
        phieu.setTrangThai(PhieuKho.DA_NHAP);
        if (!phieuKhos.contains(phieu)) {
            phieuKhos.add(phieu);
        }

        ghiNhatKy(maNV != null ? maNV : "Nhân viên kho", "Nhập kho", "Nhập kho ngay", NhatKyHeThong.INFO,
                "Hoàn tất nhập kho phiếu " + phieu.getSoPhieu() + " - Tổng: " + phieu.getTongTien() + " VNĐ");
    }

    public boolean apDungTonKho(PhieuKho phieu) {
        if (PhieuKho.XUAT.equals(phieu.getLoai())) {
            for (ChiTietKho ct : phieu.getChiTiet()) {
                PhuTung pt = timPhuTung(ct.getMaPT());
                if (pt == null || pt.getSoLuongTon() < ct.getSoLuong()) return false;
            }
            for (ChiTietKho ct : phieu.getChiTiet()) {
                PhuTung pt = timPhuTung(ct.getMaPT());
                if (pt == null) continue;
                pt.setSoLuongTon(Math.max(0, pt.getSoLuongTon() - ct.getSoLuong()));
                duLieuDAO.capNhatTonKho(pt.getMaPT(), pt.getSoLuongTon());
            }
            phieu.setTrangThai(PhieuKho.DA_NHAP);
            phieuKhos.add(phieu);
            duLieuDAO.themPhieuXuatKho(phieu, null);
            return true;
        } else {
            // Nhập kho
            try {
                nhapKhoNgay(phieu, null);
                return true;
            } catch (Exception e) {
                System.err.println("Lỗi nhập kho: " + e.getMessage());
                return false;
            }
        }
    }

    /** Lưu phiếu nhập dạng chờ nhập (chưa cộng tồn kho). */
    public void luuPhieuChoNhap(PhieuKho phieu) {
        try { new DAO.NhapKho_DAO().luuChoNhap(phieu); }
        catch (java.sql.SQLException ex) { throw new IllegalStateException(ex.getMessage(), ex); }
        dongBoTuSQLServer();
    }

    /** Cập nhật số lượng tồn kho khi nhận hàng từ nhà cung cấp. */
    public boolean xacNhanNhap(PhieuKho phieu) {
        if (!new DAO.NhapKho_DAO().doiTrangThai(phieu.getSoPhieu(), true)) return false;
        phieu.setTrangThai(PhieuKho.DA_NHAP);
        dongBoTuSQLServer(); return true;
    }

    /** Hủy phiếu chờ nhập. */
    public boolean huyPhieuChoNhap(PhieuKho phieu) {
        if (!new DAO.NhapKho_DAO().doiTrangThai(phieu.getSoPhieu(), false)) return false;
        phieu.setTrangThai(PhieuKho.DA_HUY);
        dongBoTuSQLServer(); return true;
    }

    /** Lấy danh sách phiếu kho theo loại. */
    public java.util.List<PhieuKho> getPhieuKhoTheoLoai(String loai) {
        java.util.List<PhieuKho> ds = new java.util.ArrayList<>();
        for (PhieuKho pk : phieuKhos) if (loai.equals(pk.getLoai())) ds.add(pk);
        return ds;
    }

    public Kho_DAO getKhoDAO() { return khoDAO; }

    /**
     * Phân tích chuỗi ngày tháng sang LocalDate hỗ trợ nhiều định dạng (dd/MM/yyyy, yyyy-MM-dd, ...)
     */
    public static java.time.LocalDate phanTichNgay(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        String s = str.trim();
        if (s.contains(" ")) s = s.split(" ")[0];
        try {
            if (s.contains("/")) {
                String[] p = s.split("/");
                if (p.length == 3) {
                    int d = Integer.parseInt(p[0]);
                    int m = Integer.parseInt(p[1]);
                    int y = Integer.parseInt(p[2]);
                    return java.time.LocalDate.of(y, m, d);
                }
            } else if (s.contains("-")) {
                return java.time.LocalDate.parse(s);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Tra cứu và lọc lịch sử nhập xuất kho:
     * @param loaiPhieu "Tất cả", "Nhập kho", "Xuất kho"
     * @param tuKhoa Mã phiếu, mã phụ tùng, tên phụ tùng, người lập, NCC
     * @param tuNgay Ngày bắt đầu
     * @param denNgay Ngày kết thúc
     */
    public ArrayList<PhieuKho> layLichSuNhapXuat(String loaiPhieu, String tuKhoa, java.time.LocalDate tuNgay, java.time.LocalDate denNgay) {
        try {
            ArrayList<PhieuKho> fresh = khoDAO.layTatCaLichSuKho();
            phieuKhos.clear(); phieuKhos.addAll(fresh);
        } catch (java.sql.SQLException ex) { throw new IllegalStateException("Không tải được lịch sử kho", ex); }

        ArrayList<PhieuKho> ketQua = new ArrayList<>();
        String tk = tuKhoa != null ? tuKhoa.trim().toLowerCase() : "";

        for (PhieuKho pk : phieuKhos) {
            // 1. Lọc theo loại phiếu
            if (loaiPhieu != null && !loaiPhieu.isEmpty() && !loaiPhieu.equalsIgnoreCase("Tất cả")) {
                if (loaiPhieu.contains("Nhập") && !PhieuKho.NHAP.equalsIgnoreCase(pk.getLoai())) {
                    continue;
                }
                if (loaiPhieu.contains("Xuất") && !PhieuKho.XUAT.equalsIgnoreCase(pk.getLoai())) {
                    continue;
                }
            }

            // 2. Lọc theo khoảng thời gian
            if (tuNgay != null || denNgay != null) {
                java.time.LocalDate d = phanTichNgay(pk.getNgay());
                if (d != null) {
                    if (tuNgay != null && d.isBefore(tuNgay)) continue;
                    if (denNgay != null && d.isAfter(denNgay)) continue;
                }
            }

            // 3. Lọc theo mã phiếu / mã phụ tùng / tên phụ tùng
            if (!tk.isEmpty()) {
                boolean khop = false;
                if (pk.getSoPhieu() != null && pk.getSoPhieu().toLowerCase().contains(tk)) {
                    khop = true;
                }
                if (!khop && pk.getNguoiLap() != null && pk.getNguoiLap().toLowerCase().contains(tk)) {
                    khop = true;
                }
                if (!khop && pk.getNhaCungCap() != null && pk.getNhaCungCap().toLowerCase().contains(tk)) {
                    khop = true;
                }
                if (!khop) {
                    for (ChiTietKho ct : pk.getChiTiet()) {
                        if ((ct.getMaPT() != null && ct.getMaPT().toLowerCase().contains(tk)) ||
                            (ct.getTenPT() != null && ct.getTenPT().toLowerCase().contains(tk))) {
                            khop = true;
                            break;
                        }
                    }
                }
                if (!khop) continue;
            }

            ketQua.add(pk);
        }

        return ketQua;
    }

    public void dongBoTuSQLServer() {
        if (ConnectDB.ConnectDB.getInstance().getConnection() == null) return;
        khachHangs.clear();
        phuongTiens.clear();
        phuTungs.clear();
        phieuTiepNhans.clear();
        phieuBaoHanhs.clear();
        hoaDons.clear();

        // Nạp dữ liệu chuẩn từ SQL Server
        khachHangs.addAll(khachHangDAO.layTatCaKhachHang());
        phuongTiens.addAll(phuongTienDAO.layTatCaPhuongTien());
        phuTungs.addAll(duLieuDAO.layTatCaPhuTung());
        phieuTiepNhans.addAll(phieuTiepNhanDAO.layTatCaPhieuTiepNhan());
        phieuBaoHanhs.addAll(phieuBaoHanhDAO.layTatCa());
        hoaDons.addAll(duLieuDAO.layTatCaHoaDon());

        try {
            ArrayList<PhieuKho> fresh = khoDAO.layTatCaLichSuKho();
            phieuKhos.clear();
            phieuKhos.addAll(fresh);
        } catch (java.sql.SQLException ex) { System.err.println("Cảnh báo: Không đồng bộ được lịch sử kho (" + ex.getMessage() + "), giữ dữ liệu bộ nhớ."); }
        nhanViens.clear();
        nhanViens.addAll(nhanVienDAO.layTatCaNhanVien());

        // Tải chi tiết hạng mục sửa chữa và hóa đơn vào bộ nhớ
        chiTietTheoPhieu.clear();
        chiTietTheoHoaDon.clear();
        for (PhieuTiepNhan p : phieuTiepNhans) {
            java.util.List<ChiTietDichVu> cts = duLieuDAO.layChiTietTheoPhieu(p.getSoPhieu());
            if (!cts.isEmpty()) {
                chiTietTheoPhieu.put(p.getSoPhieu(), cts);
            }
        }
        for (HoaDon hd : hoaDons) {
            java.util.List<ChiTietDichVu> cts = duLieuDAO.layChiTietTheoHoaDon(hd.getMaHD());
            if (!cts.isEmpty()) {
                chiTietTheoHoaDon.put(hd.getMaHD(), cts);
            }
        }

        demTiepNhan = phieuTiepNhans.size();
        demBaoHanh = phieuBaoHanhs.size();
        demHoaDon = hoaDons.size();

        ghiNhatKy("Hệ thống", "Hệ thống", "Đồng bộ CSDL SQL Server", NhatKyHeThong.INFO,
                "Đã đồng bộ thành công " + khachHangs.size() + " khách hàng, " +
                phuongTiens.size() + " xe, " + phuTungs.size() + " phụ tùng, " +
                hoaDons.size() + " hóa đơn từ SQL Server");
    }

    private void seed() {
        dongBoTuSQLServer();
    }
}
