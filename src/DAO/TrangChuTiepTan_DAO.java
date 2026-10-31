package DAO;

import ConnectDB.ConnectDB;
import Entity.KhoDuLieu;
import Entity.PhieuTiepNhan;
import Entity.ThongKeTiepTan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lớp DAO truy vấn dữ liệu thống kê cho trang chủ bộ phận tiếp tân.
 */
public class TrangChuTiepTan_DAO {

    /**
     * Lấy dữ liệu thống kê tổng quan dành riêng cho nghiệp vụ Tiếp tân
     */
    public ThongKeTiepTan layThongKeTongQuan() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            return fallbackThongKeCache();
        }

        ThongKeTiepTan tk = new ThongKeTiepTan();
        try {
            // 1. Nghiệp vụ tiếp nhận: Tổng số phiếu, phiếu tiếp nhận trong ngày, trạng thái xe
            String sqlTN = "SELECT " +
                    "COUNT(*) AS tongPTN, " +
                    "SUM(CASE WHEN CAST(ngayTN AS DATE) = CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) AS homNay, " +
                    "SUM(CASE WHEN trangThaiXe LIKE N'%chờ%' OR trangThaiXe IN ('DA_TIEP_NHAN','CHO_SUA','CHO_TIEP_NHAN') OR trangThaiXe IS NULL THEN 1 ELSE 0 END) AS choTN, " +
                    "SUM(CASE WHEN trangThaiXe LIKE N'%đang sửa%' OR trangThaiXe IN ('DANG_SUA','DANG_KIEM_TRA') OR trangThaiXe LIKE N'%sửa chữa%' OR trangThaiXe LIKE N'%xử lý%' OR trangThaiXe LIKE N'%bảo dưỡng%' THEN 1 ELSE 0 END) AS dangSC, " +
                    "SUM(CASE WHEN trangThaiXe IN ('HOAN_THANH','DA_HOAN_THANH') OR trangThaiXe LIKE N'%hoàn thành%' OR trangThaiXe LIKE N'%xong%' OR trangThaiXe LIKE N'%chờ giao%' THEN 1 ELSE 0 END) AS daHT " +
                    "FROM PhieuTiepNhan";
            try (PreparedStatement ps = con.prepareStatement(sqlTN); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tk.setTongPhieuTiepNhan(rs.getInt("tongPTN"));
                    tk.setPhieuHomNay(rs.getInt("homNay"));
                    tk.setXeChoTiepNhan(rs.getInt("choTN"));
                    tk.setXeDangSuaChua(rs.getInt("dangSC"));
                    tk.setXeHoanThanh(rs.getInt("daHT"));
                }
            }

            // 2. Nghiệp vụ khách hàng: Tổng số khách hàng và khách hàng mới đăng ký trong ngày
            String sqlKH = "SELECT COUNT(*) AS tongKH, " +
                    "SUM(CASE WHEN CAST(ngayDangKy AS DATE) = CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) AS khMoi " +
                    "FROM KhachHang";
            try (PreparedStatement ps = con.prepareStatement(sqlKH); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tk.setTongKhachHang(rs.getInt("tongKH"));
                    tk.setKhachHangMoiHomNay(rs.getInt("khMoi"));
                }
            }

            // 3. Nghiệp vụ phương tiện: Tổng số phương tiện đang quản lý
            String sqlPT = "SELECT COUNT(*) AS tongXe FROM PhuongTien";
            try (PreparedStatement ps = con.prepareStatement(sqlPT); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tk.setTongPhuongTien(rs.getInt("tongXe"));
                }
            }

            // 4. Nghiệp vụ bảo hành: Phiếu bảo hành còn hạn
            String sqlBH = "SELECT COUNT(*) AS tongBH, " +
                    "SUM(CASE WHEN trangThaiBH IN ('CON_HAN',N'Còn hạn') AND ngayKT >= CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) AS conHan " +
                    "FROM PhieuBaoHanh";
            try (PreparedStatement ps = con.prepareStatement(sqlBH); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tk.setTongPhieuBaoHanh(rs.getInt("tongBH"));
                    tk.setBaoHanhConHan(rs.getInt("conHan"));
                }
            }

            // 5. Nghiệp vụ hóa đơn dịch vụ: Hóa đơn lập trong ngày và doanh thu tiếp nhận hôm nay
            String sqlHD = "SELECT " +
                    "SUM(CASE WHEN CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) AS hdHomNay, " +
                    "ISNULL(SUM(CASE WHEN trangThai IN (N'Đã thanh toán','DA_THANH_TOAN') AND " +
                    "                    CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE) " +
                    "               THEN tongTien ELSE 0 END), 0) AS doanhThu " +
                    "FROM HoaDon";
            try (PreparedStatement ps = con.prepareStatement(sqlHD); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tk.setHoaDonHomNay(rs.getInt("hdHomNay"));
                    tk.setDoanhThuHomNay(rs.getLong("doanhThu"));
                }
            }

            return tk;
        } catch (SQLException e) {
            System.err.println("Lỗi TrangChuTiepTan_DAO.layThongKeTongQuan: " + e.getMessage());
            return fallbackThongKeCache();
        }
    }

    /**
     * Lấy danh sách các phiếu tiếp nhận mới nhất gần đây phục vụ theo dõi của tiếp tân
     */
    public ArrayList<PhieuTiepNhan> layTiepNhanGanDay(int limit) {
        ArrayList<PhieuTiepNhan> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            return fallbackDanhSachGanDay(limit);
        }

        String sql = "SELECT TOP (?) tn.maPTN, tn.maKH, kh.hoTen, kh.soDT, pt.bienSoXe, pt.hangXe, pt.dongXe, " +
                "tn.ngayTN, tn.yeuCauSC, tn.trangThaiXe, nv.hoTen AS tenNV " +
                "FROM PhieuTiepNhan tn " +
                "JOIN KhachHang kh ON tn.maKH = kh.maKH " +
                "JOIN PhuongTien pt ON tn.soVIN = pt.soVIN " +
                "LEFT JOIN NhanVien nv ON tn.maNV = nv.maNV " +
                "ORDER BY tn.maPTN DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 10);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maTN = rs.getString("maPTN");
                    String maKH = rs.getString("maKH");
                    String hoTen = rs.getString("hoTen");
                    String sdt = rs.getString("soDT");
                    String bienSo = rs.getString("bienSoXe");
                    String hangXe = rs.getString("hangXe");
                    String loaiXe = rs.getString("dongXe");
                    Timestamp tsTN = rs.getTimestamp("ngayTN");
                    LocalDateTime ngayTN = tsTN != null ? tsTN.toLocalDateTime() : null;
                    String yeuCau = rs.getString("yeuCauSC");
                    String trangThai = rs.getString("trangThaiXe");
                    String nguoiLap = rs.getString("tenNV");

                    PhieuTiepNhan p = new PhieuTiepNhan(
                            maTN, maKH, hoTen, sdt, bienSo, hangXe, loaiXe, ngayTN, yeuCau,
                            trangThai != null ? trangThai : "Chờ tiếp nhận",
                            nguoiLap != null ? nguoiLap : "Tiếp tân", 0, ""
                    );
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi TrangChuTiepTan_DAO.layTiepNhanGanDay: " + e.getMessage());
            return fallbackDanhSachGanDay(limit);
        }
        return list;
    }

    /**
     * Thống kê phân bổ theo tiến độ xe trong xưởng dịch vụ
     */
    public Map<String, Integer> layThongKeTheoTienDo() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Chờ tiếp nhận", 0);
        map.put("Đang sửa chữa", 0);
        map.put("Hoàn thành", 0);
        map.put("Đã giao xe", 0);

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) {
            for (PhieuTiepNhan p : KhoDuLieu.get().getPhieuTiepNhans()) {
                String td = p.getTienDo() == null ? "Chờ tiếp nhận" : p.getTienDo();
                if (td.toLowerCase().contains("chờ")) map.put("Chờ tiếp nhận", map.get("Chờ tiếp nhận") + 1);
                else if (td.toLowerCase().contains("sửa chữa") || td.toLowerCase().contains("xử lý")) map.put("Đang sửa chữa", map.get("Đang sửa chữa") + 1);
                else if (td.toLowerCase().contains("hoàn thành") || td.toLowerCase().contains("xong")) map.put("Hoàn thành", map.get("Hoàn thành") + 1);
                else map.put("Đã giao xe", map.get("Đã giao xe") + 1);
            }
            return map;
        }

        String sql = "SELECT trangThaiXe, COUNT(*) AS soLuong FROM PhieuTiepNhan GROUP BY trangThaiXe";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tt = rs.getString("trangThaiXe");
                int sl = rs.getInt("soLuong");
                if (tt == null || tt.toLowerCase().contains("chờ")) {
                    map.put("Chờ tiếp nhận", map.get("Chờ tiếp nhận") + sl);
                } else if (tt.toLowerCase().contains("sửa chữa") || tt.toLowerCase().contains("xử lý") || tt.toLowerCase().contains("bảo dưỡng")) {
                    map.put("Đang sửa chữa", map.get("Đang sửa chữa") + sl);
                } else if (tt.toLowerCase().contains("hoàn thành") || tt.toLowerCase().contains("xong") || tt.toLowerCase().contains("chờ giao")) {
                    map.put("Hoàn thành", map.get("Hoàn thành") + sl);
                } else {
                    map.put("Đã giao xe", map.get("Đã giao xe") + sl);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layThongKeTheoTienDo: " + e.getMessage());
        }
        return map;
    }

    // ===============================================================
    // Đọc dữ liệu từ bộ nhớ KhoDuLieu khi không có kết nối CSDL
    // ===============================================================
    private ThongKeTiepTan fallbackThongKeCache() {
        KhoDuLieu kho = KhoDuLieu.get();
        ThongKeTiepTan tk = new ThongKeTiepTan();

        LocalDate homNay = LocalDate.now();
        int tongTN = kho.getPhieuTiepNhans().size();
        int tnHomNay = 0;
        int choTN = 0;
        int dangSC = 0;
        int daHT = 0;

        for (PhieuTiepNhan p : kho.getPhieuTiepNhans()) {
            if (p.getNgayTN() != null && p.getNgayTN().toLocalDate().isEqual(homNay)) {
                tnHomNay++;
            }
            String td = p.getTienDo() == null ? "" : p.getTienDo().toLowerCase();
            if (td.contains("chờ") || td.isEmpty()) choTN++;
            else if (td.contains("sửa chữa") || td.contains("xử lý") || td.contains("tiếp nhận")) dangSC++;
            else if (td.contains("hoàn thành") || td.contains("xong")) daHT++;
        }

        tk.setTongPhieuTiepNhan(tongTN);
        tk.setPhieuHomNay(tnHomNay);
        tk.setXeChoTiepNhan(choTN);
        tk.setXeDangSuaChua(dangSC);
        tk.setXeHoanThanh(daHT);

        tk.setTongKhachHang(kho.getKhachHangs().size());
        int khMoi = 0;
        for (Entity.KhachHang kh : kho.getKhachHangs()) {
            if (kh.getNgayDangKy() != null && kh.getNgayDangKy().isEqual(homNay)) khMoi++;
        }
        tk.setKhachHangMoiHomNay(khMoi);

        tk.setTongPhuongTien(kho.getPhuongTiens().size());
        tk.setTongPhieuBaoHanh(kho.getPhieuBaoHanhs().size());

        int bhConHan = 0;
        for (Entity.PhieuBaoHanh bh : kho.getPhieuBaoHanhs()) {
            String tt = bh.getTinhTrang() == null ? "" : bh.getTinhTrang().toLowerCase();
            if (tt.contains("còn hạn") || tt.contains("con_han")) bhConHan++;
        }
        tk.setBaoHanhConHan(bhConHan);

        int hdHomNay = 0;
        long dtHomNay = 0;
        for (Entity.HoaDon hd : kho.getHoaDons()) {
            if (hd.getNgayLap() != null && hd.getNgayLap().isEqual(homNay)) {
                hdHomNay++;
                if (Entity.HoaDon.DA_THANH_TOAN.equals(hd.getTrangThai())) {
                    dtHomNay += hd.getTongTien();
                }
            }
        }
        tk.setHoaDonHomNay(hdHomNay);
        tk.setDoanhThuHomNay(dtHomNay);

        return tk;
    }

    private ArrayList<PhieuTiepNhan> fallbackDanhSachGanDay(int limit) {
        ArrayList<PhieuTiepNhan> list = new ArrayList<>();
        ArrayList<PhieuTiepNhan> all = KhoDuLieu.get().getPhieuTiepNhans();
        int max = limit > 0 ? limit : 10;
        int count = 0;
        for (int i = all.size() - 1; i >= 0 && count < max; i--) {
            list.add(all.get(i));
            count++;
        }
        return list;
    }
}
