package Entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu form gửi đến nghiệp vụ lưu phiếu,
 * không phụ thuộc JTable hay SQL.
 */
public final class DuLieuPhieuSuaChua {

    private final String maTiepNhan;
    private final String maKyThuatVien;
    private final LocalDate ngayHen;
    private final String ghiChu;
    private final String trangThai;
    private final List<ChiTietDichVu> phuTungs;
    private final List<CongViec> congViecs;

    public DuLieuPhieuSuaChua(
            String maTiepNhan,
            String maKyThuatVien,
            LocalDate ngayHen,
            String ghiChu,
            List<ChiTietDichVu> phuTungs,
            List<CongViec> congViecs) {
        this(maTiepNhan, maKyThuatVien, ngayHen, ghiChu, "CHO_SUA", phuTungs, congViecs);
    }

    public DuLieuPhieuSuaChua(
            String maTiepNhan,
            String maKyThuatVien,
            LocalDate ngayHen,
            String ghiChu,
            String trangThai,
            List<ChiTietDichVu> phuTungs,
            List<CongViec> congViecs) {

        if (maTiepNhan == null || maTiepNhan.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn phiếu tiếp nhận.");
        }

        if (ngayHen == null) {
            throw new IllegalArgumentException("Thiếu ngày hẹn giao xe.");
        }

        if (phuTungs == null || congViecs == null
                || (phuTungs.isEmpty() && congViecs.isEmpty())) {
            throw new IllegalArgumentException(
                    "Phiếu cần ít nhất một công việc hoặc phụ tùng.");
        }

        this.maTiepNhan = maTiepNhan.trim();
        this.maKyThuatVien = (maKyThuatVien == null || maKyThuatVien.isBlank()) ? null : maKyThuatVien.trim();
        this.ngayHen = ngayHen;
        this.ghiChu = ghiChu == null ? "" : ghiChu.trim();
        this.trangThai = (trangThai == null || trangThai.isBlank()) ? "CHO_SUA" : trangThai.trim();

        this.phuTungs = saoChep(phuTungs);
        this.congViecs = List.copyOf(congViecs);
    }

    private static List<ChiTietDichVu> saoChep(
            List<ChiTietDichVu> nguon) {

        List<ChiTietDichVu> ketQua = new ArrayList<>();

        for (ChiTietDichVu ct : nguon) {
            if (ct == null || ct.getMaPT() == null
                    || ct.getMaPT().isBlank()) {
                throw new IllegalArgumentException("Thiếu mã phụ tùng.");
            }

            ketQua.add(new ChiTietDichVu(
                    ct.getLoai(),
                    ct.getMaPT(),
                    ct.getTenHangMuc(),
                    ct.getDonVi(),
                    ct.getSoLuong(),
                    ct.getDonGia()
            ));
        }

        return ketQua;
    }

    public String getMaTiepNhan() {
        return maTiepNhan;
    }

    public String getMaKyThuatVien() {
        return maKyThuatVien;
    }

    public LocalDate getNgayHen() {
        return ngayHen;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public List<ChiTietDichVu> getPhuTungs() {
        return saoChep(phuTungs);
    }

    public List<CongViec> getCongViecs() {
        return congViecs;
    }

    /**
     * Một dòng tiền công; bất biến sau khi lấy từ form.
     */
    public static final class CongViec {

        private final String noiDung;
        private final String tenKyThuatVien;
        private final long tienCong;

        public CongViec(
                String noiDung,
                String tenKyThuatVien,
                long tienCong) {

            if (noiDung == null || noiDung.isBlank()) {
                throw new IllegalArgumentException(
                        "Nội dung công việc không được trống.");
            }

            if (tienCong < 0) {
                throw new IllegalArgumentException(
                        "Tiền công không được âm.");
            }

            this.noiDung = QuyenTruyCap.giaiMaMojibake(noiDung).trim();

            this.tenKyThuatVien = QuyenTruyCap.giaiMaMojibake(
                    tenKyThuatVien == null ? "" : tenKyThuatVien
            ).trim();

            this.tienCong = tienCong;
        }

        public String getNoiDung() {
            return noiDung;
        }

        public String getTenKyThuatVien() {
            return tenKyThuatVien;
        }

        public long getTienCong() {
            return tienCong;
        }
    }
}
