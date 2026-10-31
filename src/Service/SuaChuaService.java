package Service;

import DAO.PhieuSuaChua_DAO;
import Entity.DuLieuPhieuSuaChua;
import Entity.NhanVien;
import Entity.QuyenTruyCap;
import java.sql.SQLException;

/** Kiểm tra quyền nghiệp vụ trước khi giao DAO lưu dữ liệu. */
public class SuaChuaService {
    private final PhieuSuaChua_DAO phieuDAO;

    public SuaChuaService() { this(new PhieuSuaChua_DAO()); }

    public SuaChuaService(PhieuSuaChua_DAO phieuDAO) {
        this.phieuDAO = java.util.Objects.requireNonNull(phieuDAO);
    }

    public String luu(NhanVien nguoiThucHien, DuLieuPhieuSuaChua duLieu) throws SQLException {
        if (!QuyenTruyCap.co(nguoiThucHien, QuyenTruyCap.Quyen.SC))
            throw new IllegalArgumentException("Bạn không có quyền lưu phiếu sửa chữa.");
        if (duLieu == null) throw new IllegalArgumentException("Thiếu dữ liệu phiếu sửa chữa.");
        boolean duocPhanCong = QuyenTruyCap.co(nguoiThucHien, QuyenTruyCap.Quyen.PHAN_CONG);
        if (!duocPhanCong && !nguoiThucHien.getMaNV().equals(duLieu.getMaKyThuatVien()))
            throw new IllegalArgumentException("Không được đổi người phụ trách.");
        return phieuDAO.luu(duLieu, duocPhanCong ? null : nguoiThucHien.getMaNV());
    }
}
