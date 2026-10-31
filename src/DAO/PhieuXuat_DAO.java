
package DAO;

import ConnectDB.ConnectDB;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PhieuXuat_DAO {

    private Connection getConnection() {
        return ConnectDB.getInstance().getConnection();
    }

    // =========================================================
    // 1. KIỂM TRA PSC ĐÃ XUẤT KHO HAY CHƯA
    // =========================================================
    public boolean daXuatKhoChoPSC(String maPSC) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM PhieuXuatKho " +
                        "WHERE maPSC = ? AND trangThai = N'Đã xuất'";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, maPSC);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Tương thích với code cũ sử dụng maPTN.
    // Luồng nghiệp vụ mới nên gọi daXuatKhoChoPSC().
    @Deprecated
    public boolean daXuatKhoChoPTN(String maPTN) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM PhieuXuatKho px " +
                        "JOIN PhieuSuaChua psc ON px.maPSC = psc.maPSC " +
                        "JOIN PhieuTiepNhan ptn ON psc.soVIN = ptn.soVIN " +
                        "WHERE ptn.maPTN = ? AND px.trangThai = N'Đã xuất'";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, maPTN);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // =========================================================
    // 2. TẠO YÊU CẦU XUẤT KHO TỪ PHIẾU SỬA CHỮA
    //
    // Quy ước model giao diện:
    // Cột 0: Checkbox chọn
    // Cột 2: Mã phụ tùng
    // Cột 7: Số lượng yêu cầu
    // Cột 8: Số lượng thực xuất
    //
    // Chỉ lưu các dòng được chọn. Nếu giao diện của bạn có
    // cấu trúc cột khác, hãy chỉnh các chỉ số tại đây.
    // =========================================================

    public String taoPhieuXuat(
            String maPSC,
            String maNV,
            DefaultTableModel model,
            String lyDoXuat) {

        String sqlKiemTra =
                "SELECT COUNT(*) FROM PhieuSuaChua WHERE maPSC = ?";

        String sqlTaoPhieu =
                "INSERT INTO PhieuXuatKho " +
                        "(maPhieuXuat, maPSC, maNV, ngayXuat, trangThai, lyDoXuat) " +
                        "VALUES (?, ?, ?, GETDATE(), N'Chờ xuất', ?)";

        String sqlChiTiet =
                "INSERT INTO ChiTietPhieuXuatKho " +
                        "(maPhieuXuat, maPT, soLuongYeuCau, soLuongXuat, donGia, tinhTrangXuat) " +
                        "VALUES (?, ?, ?, 0, ?, N'Chờ xuất')";

        String maPX = "PXK" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));

        Connection conn = null;

        try {
            conn = getConnection();

            if (conn == null) {
                return null;
            }

            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlKiemTra)) {
                ps.setString(1, maPSC);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new SQLException(
                                "Không tìm thấy phiếu sửa chữa " + maPSC
                        );
                    }
                }
            }

            // Mỗi phiếu sửa chữa chỉ có một phiếu xuất kho theo UNIQUE(maPSC).
            String sqlDaTonTai =
                    "SELECT COUNT(*) FROM PhieuXuatKho WHERE maPSC = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlDaTonTai)) {
                ps.setString(1, maPSC);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException(
                                "Phiếu sửa chữa này đã có phiếu xuất kho."
                        );
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlTaoPhieu)) {
                ps.setString(1, maPX);
                ps.setString(2, maPSC);
                ps.setString(3, maNV);
                ps.setString(4, lyDoXuat == null || lyDoXuat.isBlank()
                        ? "Xuất vật tư theo phiếu sửa chữa"
                        : lyDoXuat);
                ps.executeUpdate();
            }

            int soDongDaThem = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlChiTiet)) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    Object chon = model.getValueAt(i, 0);

                    if (!Boolean.TRUE.equals(chon)) {
                        continue;
                    }

                    String maPT = String.valueOf(model.getValueAt(i, 2));
                    int soLuongYeuCau = Integer.parseInt(
                            String.valueOf(model.getValueAt(i, 6))
                    );
                    long donGia = Long.parseLong(
                            String.valueOf(model.getValueAt(i, 8))
                    );

                    if (soLuongYeuCau <= 0 || donGia < 0) {
                        throw new SQLException(
                                "Số lượng yêu cầu hoặc đơn giá không hợp lệ: " + maPT
                        );
                    }

                    ps.setString(1, maPX);
                    ps.setString(2, maPT);
                    ps.setInt(3, soLuongYeuCau);
                    ps.setLong(4, donGia);
                    ps.addBatch();
                    soDongDaThem++;
                }

                if (soDongDaThem == 0) {
                    throw new SQLException("Bạn chưa chọn phụ tùng nào.");
                }

                ps.executeBatch();
            }

            try (PreparedStatement psUpdatePSC = conn.prepareStatement(
                    "UPDATE PhieuSuaChua SET maPhieuXuat = ? WHERE maPSC = ?")) {
                psUpdatePSC.setString(1, maPX);
                psUpdatePSC.setString(2, maPSC);
                psUpdatePSC.executeUpdate();
            }

            conn.commit();
            return maPX;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }

            e.printStackTrace();
            return null;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }


    // =========================================================
    // 3. LẤY DANH SÁCH PHIẾU XUẤT ĐANG CHỜ
    //
    // model cần ít nhất 4 cột:
    // Mã phiếu xuất | Mã PSC | Ngày xuất | Trạng thái
    // =========================================================
    public void layDanhSachPhieuChoXuat(DefaultTableModel model)
            throws SQLException {

        model.setRowCount(0);

        String sql =
                "SELECT maPhieuXuat, maPSC, ngayXuat, trangThai " +
                        "FROM PhieuXuatKho " +
                        "WHERE trangThai = N'Chờ xuất' " +
                        "ORDER BY ngayXuat DESC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Timestamp tsXuat = rs.getTimestamp("ngayXuat");
                LocalDateTime ngayXuat = tsXuat != null ? tsXuat.toLocalDateTime() : null;
                model.addRow(new Object[]{
                        rs.getString("maPhieuXuat"),
                        rs.getString("maPSC"),
                        ngayXuat != null ? ngayXuat.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—",
                        rs.getString("trangThai")
                });
            }
        }
    }

    // =========================================================
    // 4. LẤY CHI TIẾT PHỤ TÙNG CỦA PHIẾU XUẤT ĐÃ TẠO
    //
    // Dùng để tải lại phiếu khi nhân viên kho chọn yêu cầu.
    // =========================================================
    public void layChiTietPhieuXuat(
            String maPX,
            DefaultTableModel model) throws SQLException {

        model.setRowCount(0);

        String sql =
                "SELECT ct.maPT, pt.tenPT, " +
                        "ct.soLuongYeuCau, ct.soLuongXuat, " +
                        "ct.donGia, ct.tinhTrangXuat, COALESCE(k.soLuongTon, 0) AS soLuongTon " +
                        "FROM ChiTietPhieuXuatKho ct " +
                        "JOIN PhuTung pt ON pt.maPT = ct.maPT " +
                        "LEFT JOIN Kho k ON k.maPT = pt.maPT " +
                        "WHERE ct.maPhieuXuat = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, maPX);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("maPT"),
                            rs.getString("tenPT"),
                            rs.getInt("soLuongYeuCau"),
                            rs.getInt("soLuongXuat"),
                            rs.getBigDecimal("donGia"),
                            rs.getString("tinhTrangXuat"),
                            rs.getInt("soLuongTon")
                    });
                }
            }
        }
    }



    public void layPhuTungTheoPSC(
            String maPSC,
            DefaultTableModel model
    ) throws SQLException {

        model.setRowCount(0);

        String sql =
                "SELECT ct.maPT, pt.tenPT, pt.donViTinh, " +
                        "COALESCE(k.soLuongTon, 0) AS soLuongTon, " +
                        "ct.soLuong, ct.donGia " +
                        "FROM ChiTietSuaChua ct " +
                        "JOIN PhuTung pt ON pt.maPT = ct.maPT " +
                        "LEFT JOIN Kho k ON k.maPT = pt.maPT " +
                        "WHERE ct.maPSC = ? " +
                        "ORDER BY ct.maPT";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maPSC);

            try (ResultSet rs = ps.executeQuery()) {
                int stt = 1;

                while (rs.next()) {
                    String maPT = rs.getString("maPT");
                    String tenPT = rs.getString("tenPT");
                    String donVi = rs.getString("donViTinh");

                    int tonKho = rs.getInt("soLuongTon");
                    int soLuongYeuCau = rs.getInt("soLuong");
                    long donGia = rs.getBigDecimal("donGia").longValue();

                    int soLuongXuat = Math.min(soLuongYeuCau, tonKho);

                    String tinhTrang;
                    if (tonKho == 0) {
                        tinhTrang = "Không đủ tồn kho";
                    } else if (tonKho < soLuongYeuCau) {
                        tinhTrang = "Xuất một phần";
                    } else {
                        tinhTrang = "Chờ xuất";
                    }

                    model.addRow(new Object[]{
                            true,
                            stt++,
                            maPT,
                            tenPT,
                            donVi,
                            tonKho,
                            soLuongYeuCau,
                            soLuongXuat,
                            donGia,
                            (long) soLuongXuat * donGia,
                            tinhTrang
                    });
                }
            }
        }
    }



    // =========================================================
    // 5. XÁC NHẬN XUẤT KHO VÀ TRỪ TỒN KHO
    // =========================================================

    public boolean xacNhanXuat(String maPX) {
        String sqlKhoaPhieu =
                "SELECT trangThai FROM PhieuXuatKho WITH (UPDLOCK, HOLDLOCK) " +
                        "WHERE maPhieuXuat = ?";

        String sqlChiTiet =
                "SELECT maPT, soLuongYeuCau, soLuongXuat " +
                        "FROM ChiTietPhieuXuatKho WHERE maPhieuXuat = ?";

        String sqlTruTon =
                "UPDATE Kho " +
                        "SET soLuongTon = soLuongTon - ? " +
                        "WHERE maPT = ? AND soLuongTon >= ?";

        String sqlCapNhatChiTiet =
                "UPDATE ChiTietPhieuXuatKho " +
                        "SET tinhTrangXuat = ? " +
                        "WHERE maPhieuXuat = ? AND maPT = ?";

        String sqlCapNhatPhieu =
                "UPDATE PhieuXuatKho SET trangThai = N'Đã xuất' " +
                        "WHERE maPhieuXuat = ? AND trangThai = N'Chờ xuất'";

        Connection conn = null;

        try {
            conn = getConnection();

            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);

            // 1. Khóa phiếu và kiểm tra trạng thái.
            try (PreparedStatement ps = conn.prepareStatement(sqlKhoaPhieu)) {
                ps.setString(1, maPX);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()
                            || !"Chờ xuất".equals(rs.getString("trangThai"))) {
                        throw new SQLException(
                                "Phiếu không tồn tại hoặc không ở trạng thái Chờ xuất."
                        );
                    }
                }
            }

            // 2. Đọc các dòng chi tiết.
            class ChiTiet {
                String maPT;
                int soLuongYeuCau;
                int soLuongXuat;

                ChiTiet(String maPT, int yeuCau, int xuat) {
                    this.maPT = maPT;
                    this.soLuongYeuCau = yeuCau;
                    this.soLuongXuat = xuat;
                }
            }

            java.util.List<ChiTiet> ds = new java.util.ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(sqlChiTiet)) {
                ps.setString(1, maPX);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ds.add(new ChiTiet(
                                rs.getString("maPT"),
                                rs.getInt("soLuongYeuCau"),
                                rs.getInt("soLuongXuat")
                        ));
                    }
                }
            }

            if (ds.isEmpty()) {
                throw new SQLException("Phiếu xuất kho chưa có phụ tùng.");
            }

            boolean coHangDuocXuat = false;

            for (ChiTiet ct : ds) {
                if (ct.soLuongXuat < 0
                        || ct.soLuongXuat > ct.soLuongYeuCau) {
                    throw new SQLException(
                            "Số lượng xuất không hợp lệ: " + ct.maPT
                    );
                }

                if (ct.soLuongXuat > 0) {
                    coHangDuocXuat = true;

                    // 3. Trừ tồn kho có kiểm tra tồn thực tế.
                    try (PreparedStatement ps = conn.prepareStatement(sqlTruTon)) {
                        ps.setInt(1, ct.soLuongXuat);
                        ps.setString(2, ct.maPT);
                        ps.setInt(3, ct.soLuongXuat);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Phụ tùng " + ct.maPT
                                            + " không đủ tồn kho hoặc không tồn tại."
                            );
                        }
                    }
                }

                String tinhTrang;

                if (ct.soLuongXuat == 0) {
                    tinhTrang = "Không đủ tồn kho";
                } else if (ct.soLuongXuat < ct.soLuongYeuCau) {
                    tinhTrang = "Xuất một phần";
                } else {
                    tinhTrang = "Đã xuất";
                }

                // 4. Cập nhật trạng thái chi tiết.
                try (PreparedStatement ps =
                             conn.prepareStatement(sqlCapNhatChiTiet)) {
                    ps.setString(1, tinhTrang);
                    ps.setString(2, maPX);
                    ps.setString(3, ct.maPT);
                    ps.executeUpdate();
                }
            }

            if (!coHangDuocXuat) {
                throw new SQLException(
                        "Chưa có phụ tùng nào được xuất. Vui lòng kiểm tra số lượng."
                );
            }

            // 5. Cập nhật trạng thái phiếu.
            try (PreparedStatement ps = conn.prepareStatement(sqlCapNhatPhieu)) {
                ps.setString(1, maPX);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Không cập nhật được trạng thái phiếu.");
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }

            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }


    // Lấy các phiếu sửa chữa đang chờ phụ tùng
    public DefaultTableModel layDanhSachPSCChoXuat()
            throws SQLException {

        String[] columns = {
                "Mã phiếu SC",
                "Mã khách hàng",
                "Số VIN",
                "Biển số xe",
                "Mã nhân viên",
                "Ngày bắt đầu",
                "Ngày dự kiến hoàn thành",
                "Trạng thái"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql =
                "SELECT p.maPSC, p.maKH, p.soVIN, " +
                        "       pt.bienSoXe, p.maNV, p.ngayBD, " +
                        "       p.ngayHoanThanhDuKien, p.trangThai " +
                        "FROM PhieuSuaChua p " +
                        "LEFT JOIN PhuongTien pt ON p.soVIN = pt.soVIN " +
                        "WHERE p.trangThai = ? " +
                        "AND EXISTS ( " +
                        "    SELECT 1 FROM ChiTietSuaChua ct " +
                        "    WHERE ct.maPSC = p.maPSC " +
                        "      AND ct.trangThai = ? " +
                        ") " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 FROM PhieuXuatKho px " +
                        "    WHERE px.maPSC = p.maPSC " +
                        "      AND px.trangThai = N'Đã xuất' " +
                        ") " +
                        "ORDER BY p.ngayBD DESC";

        try (PreparedStatement ps =
                     getConnection().prepareStatement(sql)) {

            ps.setString(1, "CHO_SUA");
            ps.setString(2, "CHO_THUC_HIEN");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp tsBD = rs.getTimestamp("ngayBD");
                    Date dHen = rs.getDate("ngayHoanThanhDuKien");
                    LocalDateTime ngayBD = tsBD != null ? tsBD.toLocalDateTime() : null;
                    LocalDate ngayHen = dHen != null ? dHen.toLocalDate() : null;
                    model.addRow(new Object[]{
                            rs.getString("maPSC"),
                            rs.getString("maKH"),
                            rs.getString("soVIN"),
                            rs.getString("bienSoXe"),
                            rs.getString("maNV"),
                            ngayBD != null ? ngayBD.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—",
                            ngayHen != null ? ngayHen.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—",
                            rs.getString("trangThai")
                    });
                }
            }
        }

        return model;
    }


    public String taoVaXacNhanXuat(
            String maPSC,
            String maNV,
            DefaultTableModel model,
            String lyDoXuat) throws SQLException {

        if (maPSC == null || maPSC.isBlank()) {
            throw new SQLException("Vui lòng chọn phiếu sửa chữa.");
        }
        if (maNV == null || maNV.isBlank()) {
            throw new SQLException("Không xác định được nhân viên xuất kho.");
        }

        String maPX = "PXK" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));

        Connection conn = getConnection();
        if (conn == null) {
            throw new SQLException("Không kết nối được SQL Server.");
        }

        boolean autoCommitCu = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            // Không tạo phiếu xuất trùng cho cùng một phiếu sửa chữa.
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT maPhieuXuat, trangThai "
                            + "FROM PhieuXuatKho WITH (UPDLOCK, HOLDLOCK) "
                            + "WHERE maPSC = ?")) {

                ps.setString(1, maPSC);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException(
                                "Phiếu sửa chữa đã có phiếu xuất kho "
                                        + rs.getString("maPhieuXuat")
                                        + " với trạng thái " + rs.getString("trangThai")
                                        + ". Không tạo phiếu trùng."
                        );
                    }
                }
            }

            // Tạo phiếu xuất.
            String sqlHeader =
                    "INSERT INTO PhieuXuatKho "
                            + "(maPhieuXuat, maPSC, maNV, ngayXuat, trangThai, lyDoXuat) "
                            + "VALUES (?, ?, ?, GETDATE(), N'Chờ xuất', ?)";

            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setString(1, maPX);
                ps.setString(2, maPSC);
                ps.setString(3, maNV);
                ps.setString(
                        4,
                        lyDoXuat == null || lyDoXuat.isBlank()
                                ? "Xuất vật tư theo phiếu sửa chữa"
                                : lyDoXuat
                );
                ps.executeUpdate();
            }

            String sqlChiTiet =
                    "INSERT INTO ChiTietPhieuXuatKho "
                            + "(maPhieuXuat, maPT, soLuongYeuCau, soLuongXuat, "
                            + "donGia, tinhTrangXuat) VALUES (?, ?, ?, ?, ?, ?)";

            String sqlTruTon =
                    "UPDATE Kho "
                            + "SET soLuongTon = soLuongTon - ? "
                            + "WHERE maPT = ? AND soLuongTon >= ?";

            int soDongDaXuat = 0;

            try (PreparedStatement psCT = conn.prepareStatement(sqlChiTiet);
                 PreparedStatement psTon = conn.prepareStatement(sqlTruTon)) {

                for (int i = 0; i < model.getRowCount(); i++) {
                    if (!Boolean.TRUE.equals(model.getValueAt(i, 0))) {
                        continue;
                    }

                    String maPT = String.valueOf(model.getValueAt(i, 2)).trim();
                    int slYeuCau = Integer.parseInt(
                            String.valueOf(model.getValueAt(i, 6)).trim()
                    );
                    int slXuat = Integer.parseInt(
                            String.valueOf(model.getValueAt(i, 7)).trim()
                    );
                    String giaText = String.valueOf(model.getValueAt(i, 8))
                            .replaceAll("[^0-9]", "");
                    java.math.BigDecimal donGia =
                            new java.math.BigDecimal(giaText.isEmpty() ? "0" : giaText);

                    if (maPT.isBlank() || slYeuCau <= 0
                            || slXuat < 0 || slXuat > slYeuCau
                            || donGia.signum() < 0) {
                        throw new SQLException(
                                "Dữ liệu không hợp lệ tại phụ tùng " + maPT
                        );
                    }

                    if (slXuat == 0) {
                        throw new SQLException(
                                "Số lượng thực xuất phải lớn hơn 0 cho phụ tùng " + maPT
                        );
                    }

                    // Trừ tồn kho có điều kiện. Nếu không đủ thì rollback toàn bộ.
                    psTon.setInt(1, slXuat);
                    psTon.setString(2, maPT);
                    psTon.setInt(3, slXuat);

                    if (psTon.executeUpdate() != 1) {
                        throw new SQLException(
                                "Phụ tùng " + maPT
                                        + " không tồn tại hoặc không đủ tồn kho."
                        );
                    }

                    String tinhTrang = slXuat < slYeuCau
                            ? "Xuất một phần"
                            : "Đã xuất";

                    psCT.setString(1, maPX);
                    psCT.setString(2, maPT);
                    psCT.setInt(3, slYeuCau);
                    psCT.setInt(4, slXuat);
                    psCT.setBigDecimal(5, donGia);
                    psCT.setString(6, tinhTrang);
                    psCT.addBatch();

                    soDongDaXuat++;
                }

                if (soDongDaXuat == 0) {
                    throw new SQLException("Bạn chưa chọn phụ tùng nào để xuất.");
                }

                psCT.executeBatch();
            }

            // Chỉ đổi trạng thái sau khi lưu chi tiết và trừ kho thành công.
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE PhieuXuatKho SET trangThai = N'Đã xuất' "
                            + "WHERE maPhieuXuat = ? AND trangThai = N'Chờ xuất'")) {
                ps.setString(1, maPX);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Không cập nhật được trạng thái phiếu xuất.");
                }
            }

            try (PreparedStatement psUpdatePSC = conn.prepareStatement(
                    "UPDATE PhieuSuaChua SET maPhieuXuat = ? WHERE maPSC = ?")) {
                psUpdatePSC.setString(1, maPX);
                psUpdatePSC.setString(2, maPSC);
                psUpdatePSC.executeUpdate();
            }

            conn.commit();
            return maPX;

        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }

            if (ex instanceof SQLException) {
                throw (SQLException) ex;
            }
            throw new SQLException("Lỗi khi xác nhận xuất kho: " + ex.getMessage(), ex);

        } finally {
            try {
                conn.setAutoCommit(autoCommitCu);
            } catch (SQLException ignored) {
            }
        }
    }

}
