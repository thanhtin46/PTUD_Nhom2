-- ============================================================
-- HỆ THỐNG QUẢN LÝ GARA Ô TÔ
-- CƠ SỞ DỮ LIỆU: QuanLySuaChuaXe
-- Tạo bảng, ràng buộc khóa ngoại và dữ liệu mẫu ban đầu
-- ============================================================

USE master;
GO

-- 1. Nếu Database đã tồn tại, đóng kết nối và xóa để tạo lại sạch sẽ
IF DB_ID(N'QuanLySuaChuaXe') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLySuaChuaXe SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLySuaChuaXe;
END;
GO

-- 2. Tạo mới Database
CREATE DATABASE QuanLySuaChuaXe COLLATE Vietnamese_CI_AS;
GO

USE QuanLySuaChuaXe;
GO

-- Đảm bảo chuẩn định dạng ngày giờ và Unicode cho mọi máy tính
SET DATEFORMAT ymd;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- ============================================================
-- PHẦN I: TẠO CÁC BẢNG VÀ RÀNG BUỘC
-- Thứ tự tạo bảng theo quan hệ khóa ngoại
-- ============================================================

/* 0. CHUC VU (Danh mục chức vụ động của nhân sự) */
CREATE TABLE ChucVu (
    maCV VARCHAR(20) NOT NULL PRIMARY KEY,
    tenChucVu NVARCHAR(100) NOT NULL UNIQUE,
    moTa NVARCHAR(255) NULL,
    ngayTao VARCHAR(20) NULL DEFAULT CONVERT(VARCHAR(20), GETDATE(), 23)
);
GO

/* 1. NHAN VIEN */
CREATE TABLE NhanVien (
    maNV VARCHAR(20) NOT NULL PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(100) NOT NULL,
    soDT VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) NULL,
    bacNghe NVARCHAR(50) NULL,
    boPhan NVARCHAR(50) NULL,
    ngayVaoLam VARCHAR(20) NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang làm việc',
    CONSTRAINT CK_NhanVien_SDT CHECK (LEN(soDT) >= 9),
    CONSTRAINT FK_NhanVien_ChucVu FOREIGN KEY (chucVu) REFERENCES ChucVu(tenChucVu)
);
GO

/* 2. TAI KHOAN: Mỗi nhân viên có tối đa một tài khoản */
CREATE TABLE TaiKhoan (
    maNV VARCHAR(20) NOT NULL PRIMARY KEY,
    taiKhoan VARCHAR(50) NOT NULL UNIQUE,
    matKhau VARCHAR(255) NOT NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG',
    CONSTRAINT CK_TaiKhoan_TrangThai CHECK (trangThai IN ('HOAT_DONG', 'KHOA')),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

/* 3. KHACH HANG */
CREATE TABLE KhachHang (
    maKH VARCHAR(20) NOT NULL PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDT VARCHAR(15) NOT NULL,
    chungMinh VARCHAR(30) NULL UNIQUE,
    diaChi NVARCHAR(255) NULL,
    email VARCHAR(150) NULL,
    ghiChu NVARCHAR(255) NULL,
    ngayDangKy DATE NULL,
    CONSTRAINT CK_KhachHang_SDT CHECK (LEN(soDT) >= 9)
);
GO

/* 4. PHUONG TIEN */
CREATE TABLE PhuongTien (
    soVIN VARCHAR(30) NOT NULL PRIMARY KEY,
    bienSoXe VARCHAR(20) NOT NULL UNIQUE,
    hangXe NVARCHAR(50) NOT NULL,
    dongXe NVARCHAR(50) NOT NULL,
    maSX INT NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    soMay VARCHAR(50) NULL,
    mauSon NVARCHAR(50) NULL,
    odo VARCHAR(20) NULL,
    ngayTiepNhanGanNhat DATE NULL,
    CONSTRAINT CK_PhuongTien_Nam CHECK (maSX BETWEEN 1900 AND 2100),
    CONSTRAINT FK_PhuongTien_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);
GO

/* 5. NHA CUNG CAP */
CREATE TABLE NhaCungCap (
    maNCC VARCHAR(20) NOT NULL PRIMARY KEY,
    tenNCC NVARCHAR(150) NOT NULL,
    soDT VARCHAR(15) NOT NULL,
    diaChi NVARCHAR(255) NULL,
    email VARCHAR(150) NULL,
    CONSTRAINT CK_NhaCungCap_Email CHECK (email IS NULL OR email LIKE '%_@_%._%')
);
GO

/* 6. PHU TUNG */
CREATE TABLE PhuTung (
    maPT VARCHAR(20) NOT NULL PRIMARY KEY,
    tenPT NVARCHAR(150) NOT NULL,
    donViTinh NVARCHAR(30) NOT NULL,
    donGiaBan DECIMAL(15,2) NOT NULL,
    CONSTRAINT CK_PhuTung_Gia CHECK (donGiaBan >= 0)
);
GO

/* 7. KHO: Quản lý vị trí và tồn kho phụ tùng */
CREATE TABLE Kho (
    maKho VARCHAR(20) NOT NULL PRIMARY KEY,
    maPT VARCHAR(20) NOT NULL UNIQUE,
    soLuongTon INT NOT NULL DEFAULT 0,
    viTriKho NVARCHAR(100) NOT NULL,
    soLTT INT NOT NULL DEFAULT 0,
    soLTD INT NOT NULL DEFAULT 0,
    CONSTRAINT CK_Kho_SoLuong CHECK (soLuongTon >= 0),
    CONSTRAINT CK_Kho_ToiThieu CHECK (soLTT >= 0),
    CONSTRAINT CK_Kho_ToiDa CHECK (soLTD >= soLTT),
    CONSTRAINT FK_Kho_PhuTung FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 8. PHIEU TIEP NHAN */
CREATE TABLE PhieuTiepNhan (
    maPTN VARCHAR(20) NOT NULL PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    soVIN VARCHAR(30) NOT NULL,
    ngayTN DATETIME2 NOT NULL,
    yeuCauSC NVARCHAR(MAX) NOT NULL,
    trangThaiXe NVARCHAR(100) NOT NULL DEFAULT N'DA_TIEP_NHAN',
    CONSTRAINT FK_PTN_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PTN_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PTN_PhuongTien FOREIGN KEY (soVIN) REFERENCES PhuongTien(soVIN)
);
GO

/* 9. PHIEU SUA CHUA */
CREATE TABLE PhieuSuaChua (
    maPSC VARCHAR(20) NOT NULL PRIMARY KEY,
    maPTN VARCHAR(20) NULL,
    maPhieuXuat VARCHAR(20) NULL,
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    soVIN VARCHAR(30) NOT NULL,
    ngayBD DATETIME2 NOT NULL,
    ngayHoanThanhDuKien DATE NULL,
    trangThai NVARCHAR(50) NOT NULL DEFAULT N'CHO_SUA',
    CONSTRAINT CK_PSC_Ngay CHECK (
        ngayHoanThanhDuKien IS NULL
        OR TRY_CAST(ngayBD AS DATE) IS NULL
        OR ngayHoanThanhDuKien >= TRY_CAST(ngayBD AS DATE)
    ),
    CONSTRAINT FK_PSC_PTN FOREIGN KEY (maPTN) REFERENCES PhieuTiepNhan(maPTN),
    CONSTRAINT FK_PSC_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PSC_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PSC_PhuongTien FOREIGN KEY (soVIN) REFERENCES PhuongTien(soVIN)
);
GO

/* 10. CHI TIET SUA CHUA (Phụ tùng sử dụng) */
CREATE TABLE ChiTietSuaChua (
    maPSC VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    noiDungSC NVARCHAR(255) NOT NULL,
    tenPhuTung NVARCHAR(150) NOT NULL,
    soLuong INT NOT NULL DEFAULT 1,
    donGia DECIMAL(15,2) NOT NULL,
    trangThai NVARCHAR(50) NOT NULL DEFAULT N'CHO_THUC_HIEN',
    CONSTRAINT PK_ChiTietSuaChua PRIMARY KEY (maPSC, maPT),
    CONSTRAINT CK_CTSC_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_CTSC_DonGia CHECK (donGia >= 0),
    CONSTRAINT FK_CTSC_PhieuSuaChua FOREIGN KEY (maPSC) REFERENCES PhieuSuaChua(maPSC),
    CONSTRAINT FK_CTSC_PhuTung FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 11. CHI TIET CONG VIEC SUA CHUA (Tiền công & kỹ thuật viên) */
CREATE TABLE ChiTietCongViecSuaChua (
    maPSC VARCHAR(20) NOT NULL,
    stt INT NOT NULL,
    noiDungCongViec NVARCHAR(255) NOT NULL,
    tenKTV NVARCHAR(100) NULL,
    tienCong DECIMAL(15,2) NOT NULL DEFAULT 0,
    CONSTRAINT PK_ChiTietCongViecSuaChua PRIMARY KEY (maPSC, stt),
    CONSTRAINT CK_CTCV_TienCong CHECK (tienCong >= 0),
    CONSTRAINT FK_CTCV_PSC FOREIGN KEY (maPSC) REFERENCES PhieuSuaChua(maPSC)
);
GO

/* 12. HOA DON */
CREATE TABLE HoaDon (
    maHD VARCHAR(20) NOT NULL PRIMARY KEY,
    maKH VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayLap DATE NOT NULL,
    hinhThucThanhToan NVARCHAR(50) NOT NULL DEFAULT N'TIEN_MAT',
    tienDichVu DECIMAL(15,2) DEFAULT 0,
    tienPhuTung DECIMAL(15,2) DEFAULT 0,
    giamGia DECIMAL(5,2) DEFAULT 0,
    tongTien DECIMAL(15,2) DEFAULT 0,
    ghiChu NVARCHAR(255) NULL,
    trangThai NVARCHAR(50) DEFAULT N'DA_THANH_TOAN',
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

/* 13. CHI TIET HOA DON (Phụ tùng) */
CREATE TABLE ChiTietHoaDon (
    maHD VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    tenPhuTung NVARCHAR(150) NOT NULL,
    donViTinh NVARCHAR(50) NULL DEFAULT N'Cái',
    soLuong INT NOT NULL,
    donGia DECIMAL(15,2) NOT NULL,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHD, maPT),
    CONSTRAINT CK_CTHD_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_CTHD_DonGia CHECK (donGia >= 0),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_PhuTung FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 14. CHI TIET DICH VU HOA DON (Dịch vụ & Tiền công) */
CREATE TABLE ChiTietDichVuHoaDon (
    maHD VARCHAR(20) NOT NULL,
    stt INT NOT NULL,
    tenDichVu NVARCHAR(255) NOT NULL,
    donViTinh NVARCHAR(50) NULL DEFAULT N'Lần',
    soLuong INT NOT NULL CHECK (soLuong > 0),
    donGia DECIMAL(15,2) NOT NULL CHECK (donGia >= 0),
    CONSTRAINT PK_ChiTietDichVuHoaDon PRIMARY KEY (maHD, stt),
    CONSTRAINT FK_CTDVHD_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD)
);
GO

/* 15. PHIEU BAO HANH */
CREATE TABLE PhieuBaoHanh (
    maPBH VARCHAR(20) NOT NULL PRIMARY KEY,
    ngayLap DATE NOT NULL DEFAULT (CONVERT(date, GETDATE())),
    ngayBD DATE NOT NULL,
    ngayKT DATE NOT NULL,
    trangThaiBH NVARCHAR(50) NOT NULL DEFAULT N'CON_HAN',
    maKH VARCHAR(20) NOT NULL,
    soVIN VARCHAR(30) NOT NULL,
    maHoaDon VARCHAR(20) NULL,
    maNV VARCHAR(20) NULL,
    ghiChu NVARCHAR(MAX) NULL,
    CONSTRAINT CK_PBH_Ngay CHECK (ngayKT >= ngayBD),
    CONSTRAINT FK_PBH_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PBH_PhuongTien FOREIGN KEY (soVIN) REFERENCES PhuongTien(soVIN),
    CONSTRAINT FK_PBH_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_PBH_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

/* 16. CHI TIET PHIEU BAO HANH (Hạng mục bảo hành) */
CREATE TABLE ChiTietPhieuBaoHanh (
    maPBH VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    tenHangMuc NVARCHAR(255) NOT NULL,
    loaiHangMuc NVARCHAR(50) NULL DEFAULT N'Phụ tùng / Linh kiện',
    thoiHanBaoHanh INT NOT NULL DEFAULT 6 CHECK (thoiHanBaoHanh > 0),
    ngayHetHan DATE NOT NULL,
    dieuKienBaoHanh NVARCHAR(MAX) NULL,
    ghiChu NVARCHAR(MAX) NULL,
    lyDo NVARCHAR(255) NULL,
    huongXuLy NVARCHAR(255) NULL,
    CONSTRAINT PK_ChiTietPhieuBaoHanh PRIMARY KEY (maPBH, maPT),
    CONSTRAINT FK_CTPBH_PhieuBaoHanh FOREIGN KEY (maPBH) REFERENCES PhieuBaoHanh(maPBH) ON DELETE CASCADE,
    CONSTRAINT FK_CTPBH_PhuTung FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* View tương thích ngược cho HangMucBaoHanh */
IF OBJECT_ID(N'dbo.HangMucBaoHanh', N'V') IS NOT NULL
    DROP VIEW dbo.HangMucBaoHanh;
GO
CREATE VIEW dbo.HangMucBaoHanh AS
SELECT maPBH, maPT, tenHangMuc, thoiHanBaoHanh, ngayHetHan, dieuKienBaoHanh, ghiChu
FROM dbo.ChiTietPhieuBaoHanh;
GO

/* 18. PHIEU NHAP KHO */
CREATE TABLE PhieuNhapKho (
    maPhieuNhap VARCHAR(20) NOT NULL PRIMARY KEY,
    maNCC VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayNhap DATE NOT NULL,
    tongTien DECIMAL(15,2) NOT NULL DEFAULT 0,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'DA_NHAP',
    ngayDuKienGiao DATE NULL,
    ghiChu NVARCHAR(255) NULL,
    CONSTRAINT CK_PNK_TongTien CHECK (tongTien >= 0),
    CONSTRAINT CK_PNK_TrangThai CHECK (trangThai IN ('CHO_NHAP', 'DA_NHAP', 'DA_HUY')),
    CONSTRAINT FK_PNK_NCC FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC),
    CONSTRAINT FK_PNK_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

/* 19. CHI TIET PHIEU NHAP KHO */
CREATE TABLE ChiTietPhieuNhapKho (
    maPhieuNhap VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    tenPT NVARCHAR(150) NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(15,2) NOT NULL,
    CONSTRAINT PK_ChiTietPhieuNhapKho PRIMARY KEY (maPhieuNhap, maPT),
    CONSTRAINT CK_CTPNK_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_CTPNK_DonGia CHECK (donGia >= 0),
    CONSTRAINT FK_CTPNK_PhieuNhap FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhapKho(maPhieuNhap),
    CONSTRAINT FK_CTPNK_PhuTung FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 20. PHIEU XUAT KHO */
CREATE TABLE PhieuXuatKho (
    maPhieuXuat VARCHAR(20) NOT NULL PRIMARY KEY,
    maPSC VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayXuat DATETIME NOT NULL DEFAULT GETDATE(),
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xuất',
    lyDoXuat NVARCHAR(255) NOT NULL DEFAULT N'Xuất vật tư thay thế theo phiếu sửa chữa',
    CONSTRAINT UQ_PXK_maPSC UNIQUE (maPSC),
    CONSTRAINT FK_PXK_PhieuSuaChua FOREIGN KEY (maPSC) REFERENCES PhieuSuaChua(maPSC),
    CONSTRAINT FK_PXK_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT CK_PXK_TrangThai CHECK (trangThai IN (N'Chờ xuất', N'Đã xuất', N'Đã hủy', 'CHO_XUAT', 'DA_XUAT', 'DA_HUY'))
);
GO

/* Khóa ngoại PhieuSuaChua -> PhieuXuatKho (Tạo an toàn sau khi bảng PhieuXuatKho đã tồn tại) */
IF OBJECT_ID(N'dbo.FK_PSC_PhieuXuatKho', N'F') IS NULL
    ALTER TABLE PhieuSuaChua ADD CONSTRAINT FK_PSC_PhieuXuatKho FOREIGN KEY (maPhieuXuat) REFERENCES PhieuXuatKho(maPhieuXuat);
GO
CREATE TABLE ChiTietPhieuXuatKho (
    maPhieuXuat VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    soLuongYeuCau INT NOT NULL,
    soLuongXuat INT NOT NULL,
    donGia DECIMAL(15,2) NOT NULL,
    tinhTrangXuat NVARCHAR(50) NOT NULL DEFAULT N'Chờ xuất',
    CONSTRAINT PK_ChiTietPhieuXuatKho PRIMARY KEY (maPhieuXuat, maPT),
    CONSTRAINT CK_CTPXK_SoLuongYeuCau CHECK (soLuongYeuCau > 0),
    CONSTRAINT CK_CTPXK_SoLuongXuat CHECK (soLuongXuat >= 0 AND soLuongXuat <= soLuongYeuCau),
    CONSTRAINT CK_CTPXK_DonGia CHECK (donGia >= 0),
    CONSTRAINT CK_CTPXK_TinhTrang CHECK (tinhTrangXuat IN (N'Chờ xuất', N'Đã xuất', N'Xuất một phần', N'Không đủ tồn kho')),
    CONSTRAINT FK_CTPXK_PhieuXuat FOREIGN KEY (maPhieuXuat) REFERENCES PhieuXuatKho(maPhieuXuat),
    CONSTRAINT FK_CTPXK_PhuTung FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 21. QUYEN BO SUNG (Phân quyền bổ sung theo nhân viên) */
CREATE TABLE QuyenBoSung (
    maNV VARCHAR(20) NOT NULL,
    maQuyen VARCHAR(40) NOT NULL,
    CONSTRAINT PK_QuyenBoSung PRIMARY KEY (maNV, maQuyen),
    CONSTRAINT FK_QBS_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV) ON DELETE CASCADE
);
GO

/* 22. NHAT KY QUYEN (Audit log nhật ký thay đổi quyền & vai trò) */
CREATE TABLE NhatKyQuyen (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    thoiGian DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    nguoiThucHien VARCHAR(20) NOT NULL,
    doiTuong VARCHAR(20) NOT NULL,
    noiDung NVARCHAR(2000) NOT NULL
);
GO

-- ============================================================
-- PHẦN II: DỮ LIỆU MẪU CƠ BẢN (KHỞI TẠO HỆ THỐNG)
-- ============================================================

USE QuanLySuaChuaXe;
GO

/* 0. Chuc vu (Dữ liệu ban đầu) */
INSERT INTO ChucVu (maCV, tenChucVu, moTa, ngayTao) VALUES
('CV01', N'Quản lý', N'Ban Giám đốc, điều hành tổng thể gara, quản lý tài chính và nhân sự', '2026-01-01'),
('CV02', N'Cố vấn dịch vụ', N'Tiếp nhận xe, tư vấn khách hàng, chẩn đoán ban đầu và lập báo giá', '2026-01-01'),
('CV03', N'Nhân viên tiếp tân', N'Đón tiếp khách hàng, tạo phiếu tiếp nhận và hỗ trợ thủ tục', '2026-01-01'),
('CV04', N'Nhân viên sửa chữa', N'Kỹ thuật viên thực hiện sửa chữa, bảo dưỡng theo lệnh xưởng', '2026-01-01'),
('CV05', N'KTV Máy - Gầm', N'Kỹ thuật viên chuyên trách đại tu động cơ, hộp số và hệ thống gầm', '2026-01-01'),
('CV06', N'KTV Điện - Điện Lạnh Ô tô', N'Kỹ thuật viên chuyên điện tử, điều khiển ECU và điện lạnh', '2026-01-01'),
('CV07', N'Trưởng tổ Kỹ thuật', N'Quản lý tổ thợ, kiểm tra KCS chất lượng trước khi bàn giao xe', '2026-01-01'),
('CV08', N'Nhân viên kho', N'Thủ kho phụ tùng, kiểm tra nhập xuất vật tư và tồn kho', '2026-01-01'),
('CV09', N'Nhân viên thu ngân', N'Thu ngân quầy, lập và xuất hóa đơn dịch vụ, thanh toán VietQR', '2026-01-01'),
('CV10', N'KTV Học việc', N'Học việc xưởng, hỗ trợ kỹ thuật viên chính và vệ sinh khu vực', '2026-01-01');
GO

/* 1. Nhan vien */
INSERT INTO NhanVien (maNV, hoTen, chucVu, soDT, email, bacNghe, boPhan, ngayVaoLam, trangThai) VALUES
('NV01', N'Nguyễn Văn Tiếp Tân', N'Nhân viên tiếp tân', '0901234561', 'tieptan@gara.vn', N'Bậc 3/7', N'Tiếp tân', '2024-01-01', N'Đang làm việc'),
('NV02', N'Trần Văn Sửa Chữa', N'Nhân viên sửa chữa', '0901234562', 'suachua@gara.vn', N'Bậc 5/7', N'Xưởng sửa chữa', '2023-01-01', N'Đang làm việc'),
('NV03', N'Nguyễn Văn Kho', N'Nhân viên kho', '0901234563', 'kho@gara.vn', N'Bậc 4/7', N'Kho phụ tùng', '2024-03-15', N'Đang làm việc'),
('NV04', N'Quản Lý Tổng', N'Quản lý', '0901234564', 'quanly@gara.vn', N'Bậc 7/7', N'Ban Giám đốc', '2020-01-01', N'Đang làm việc'),
('NV05', N'Trần Thị Thu Ngân', N'Nhân viên thu ngân', '0901234565', 'thungan@gara.vn', N'Bậc 3/7', N'Kế toán thu ngân', '2024-05-10', N'Đang làm việc');
GO

/* 2. Tai khoan dang nhap he thong */
INSERT INTO TaiKhoan (maNV, taiKhoan, matKhau, trangThai) VALUES
('NV01', 'tiepTan', '123', 'HOAT_DONG'),
('NV02', 'suaChua', '123', 'HOAT_DONG'),
('NV03', 'kho', '123', 'HOAT_DONG'),
('NV04', 'quanLy', '123', 'HOAT_DONG'),
('NV05', 'thuNgan', '123', 'HOAT_DONG');
GO

/* 3. Khach hang */
INSERT INTO KhachHang (maKH, hoTen, soDT, chungMinh, diaChi, email, ghiChu, ngayDangKy) VALUES
('KH001', N'Nguyễn Minh Quân', '0912000001', '079000000001', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', 'quan.nm@gmail.com', N'Khách hàng thân thiết', '2026-01-01'),
('KH002', N'Trần Ngọc Mai', '0912000002', '079000000002', N'456 Lê Lợi, Q.1, TP.HCM', 'mai.tn@gmail.com', N'Khách hàng thân thiết', '2026-01-15'),
('KH003', N'Đỗ Hoàng Nam', '0912000003', '079000000003', N'789 Cách Mạng Tháng 8, Q.10, TP.HCM', 'nam.dh@gmail.com', N'Khách mới', '2026-02-20');
GO

/* 4. Phuong tien */
INSERT INTO PhuongTien (soVIN, bienSoXe, hangXe, dongXe, maSX, maKH, soMay, mauSon, odo, ngayTiepNhanGanNhat) VALUES
('VIN001', '51A-12345', N'Honda', N'City', 2022, 'KH001', N'MAY-HD01', N'Trắng', N'15000', '2026-03-10'),
('VIN002', '59A-67890', N'Toyota', N'Vios', 2021, 'KH002', N'MAY-TY02', N'Đen', N'28000', '2026-03-10'),
('VIN003', '60A-11223', N'Yamaha', N'Exciter', 2023, 'KH003', N'MAY-YM03', N'Xanh GP', N'8000', '2026-03-11');
GO

/* 5. Nha cung cap */
INSERT INTO NhaCungCap (maNCC, tenNCC, soDT, diaChi, email) VALUES
('NCC001', N'Công ty Phụ Tùng Sài Gòn', '02830000001', N'Quận 5, TP HCM', 'sales1@phutungsaigon.com'),
('NCC002', N'Phụ Tùng Miền Đông', '02830000002', N'TP Thủ Đức, TP HCM', 'sales2@phutungmiendong.com');
GO

/* 6. Phu tung */
INSERT INTO PhuTung (maPT, tenPT, donViTinh, donGiaBan) VALUES
('PT001', N'Lọc dầu động cơ', N'Chiếc', 150000),
('PT002', N'Lọc gió động cơ', N'Chiếc', 220000),
('PT003', N'Bugi đánh lửa', N'Chiếc', 85000),
('PT004', N'Dầu động cơ cao cấp', N'Lít', 120000),
('PT005', N'Bộ phanh trước', N'Bộ', 450000);
GO

/* 7. Kho */
INSERT INTO Kho (maKho, maPT, soLuongTon, viTriKho, soLTT, soLTD) VALUES
('KHO001', 'PT001', 50, N'Kệ A1', 5, 100),
('KHO002', 'PT002', 30, N'Kệ A2', 5, 80),
('KHO003', 'PT003', 100, N'Kệ B1', 10, 200),
('KHO004', 'PT004', 80, N'Kệ B2', 10, 150),
('KHO005', 'PT005', 20, N'Kệ C1', 3, 50);
GO

/* 8. Phieu tiep nhan */
INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES
('TN001', 'NV01', 'KH001', 'VIN001', '2026-03-10T08:30:00', N'Thay dầu và kiểm tra lọc gió', N'Đang sửa'),
('TN002', 'NV01', 'KH002', 'VIN002', '2026-03-10T09:00:00', N'Kiểm tra hệ thống phanh', N'Chờ sửa'),
('TN003', 'NV01', 'KH003', 'VIN003', '2026-03-11T10:00:00', N'Kiểm tra bugi', N'Chờ tiếp nhận');
GO

/* 9. Phieu sua chua */
INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES
('SC001', 'TN001', 'NV02', 'KH001', 'VIN001', '2026-03-10T09:00:00', '2026-03-10', N'DANG_SUA'),
('SC002', 'TN002', 'NV02', 'KH002', 'VIN002', '2026-03-10T10:00:00', '2026-03-11', N'CHO_SUA');
GO

/* 10. Chi tiet sua chua (Phụ tùng) */
INSERT INTO ChiTietSuaChua (maPSC, maPT, noiDungSC, tenPhuTung, soLuong, donGia, trangThai) VALUES
('SC001', 'PT001', N'Thay lọc dầu', N'Lọc dầu động cơ', 1, 150000, N'DA_HOAN_THANH'),
('SC001', 'PT004', N'Thay dầu động cơ', N'Dầu động cơ cao cấp', 4, 120000, N'DA_HOAN_THANH'),
('SC002', 'PT005', N'Kiểm tra và thay bộ phanh', N'Bộ phanh trước', 1, 450000, N'CHO_THUC_HIEN');
GO

/* 11. Chi tiet cong viec sua chua (Tiền công) */
INSERT INTO ChiTietCongViecSuaChua (maPSC, stt, noiDungCongViec, tenKTV, tienCong) VALUES
('SC001', 1, N'Thay dầu động cơ & lọc nhớt', N'Trần Văn Sửa Chữa', 150000),
('SC001', 2, N'Kiểm tra và vệ sinh hệ thống nạp khí', N'Trần Văn Sửa Chữa', 100000),
('SC002', 1, N'Bảo dưỡng và thay bộ phanh 4 bánh', N'Trần Văn Sửa Chữa', 250000);
GO

/* 12. Hoa don */
INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES
('HD001', 'KH001', 'NV05', '2026-03-01', N'CHUYEN_KHOAN', 250000, 780000, 0, 1030000, N'Hóa đơn sửa chữa SC001', N'DA_THANH_TOAN'),
('HD002', 'KH002', 'NV05', '2026-03-05', N'TIEN_MAT', 250000, 170000, 0, 420000, N'Hóa đơn bảo dưỡng Bugi SC002', N'DA_THANH_TOAN');
GO

/* 13. Chi tiet hoa don (Phụ tùng) */
INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES
('HD001', 'PT001', N'Lọc dầu động cơ', N'Chiếc', 2, 150000),
('HD001', 'PT004', N'Dầu động cơ cao cấp', N'Lít', 4, 120000),
('HD002', 'PT003', N'Bugi đánh lửa', N'Chiếc', 2, 85000);
GO

/* 14. Chi tiet dich vu hoa don (Dịch vụ & Tiền công) */
INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES
('HD001', 1, N'Thay dầu động cơ & lọc nhớt', N'Lần', 1, 150000),
('HD001', 2, N'Kiểm tra và vệ sinh hệ thống nạp khí', N'Lần', 1, 100000),
('HD002', 1, N'Bảo dưỡng và thay bộ phanh 4 bánh', N'Lần', 1, 250000);
GO

/* 15. Phieu bao hanh */
INSERT INTO PhieuBaoHanh (maPBH, ngayLap, ngayBD, ngayKT, trangThaiBH, maKH, soVIN, maHoaDon, maNV, ghiChu) VALUES
('BH001', '2026-03-01', '2026-03-01', '2027-03-01', N'CON_HAN', 'KH001', 'VIN001', 'HD001', 'NV05', N'Bảo hành sửa chữa SC001'),
('BH002', '2026-03-05', '2026-03-05', '2027-03-05', N'CON_HAN', 'KH002', 'VIN002', 'HD002', 'NV05', N'Bảo hành bảo dưỡng SC002');
GO

/* 16. Chi tiet phieu bao hanh */
INSERT INTO ChiTietPhieuBaoHanh (maPBH, maPT, tenHangMuc, thoiHanBaoHanh, ngayHetHan, dieuKienBaoHanh, ghiChu, lyDo, huongXuLy) VALUES
('BH001', 'PT001', N'Lọc dầu & gioăng làm kín', 12, '2027-03-01', N'Bảo hành lỗi rò rỉ do lắp ráp', N'Chính hãng', N'Bảo hành lọc dầu', N'Kiểm tra và thay mới nếu lỗi rò rỉ'),
('BH002', 'PT002', N'Bugi đánh lửa', 12, '2027-03-05', N'Bảo hành lỗi mất lửa điện tử', N'Chính hãng', N'Bảo hành lọc gió', N'Kiểm tra tình trạng lọc');
GO

/* 18. Phieu nhap kho */
INSERT INTO PhieuNhapKho (maPhieuNhap, maNCC, maNV, ngayNhap, tongTien, trangThai, ngayDuKienGiao, ghiChu) VALUES
('PN001', 'NCC001', 'NV04', '2026-03-01', 1800000, 'DA_NHAP', NULL, N'Nhập hàng lọc dầu và lọc gió định kỳ'),
('PN002', 'NCC002', 'NV04', '2026-03-06', 1000000, 'DA_NHAP', NULL, N'Nhập bugi đánh lửa');
GO

/* 19. Chi tiet nhap kho */
INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap, maPT, tenPT, soLuong, donGia) VALUES
('PN001', 'PT001', N'Lọc dầu động cơ', 10, 100000),
('PN001', 'PT002', N'Lọc gió động cơ', 5, 160000),
('PN002', 'PT003', N'Bugi đánh lửa', 20, 50000);
GO

/* 20. Phieu xuat kho */
INSERT INTO PhieuXuatKho (maPhieuXuat, maPSC, maNV, ngayXuat, trangThai, lyDoXuat) VALUES
('PX001', 'SC001', 'NV03', '2026-03-10T10:00:00', N'Đã xuất', N'Xuất vật tư thay thế theo phiếu sửa chữa'),
('PX002', 'SC002', 'NV03', '2026-03-10T10:00:00', N'Chờ xuất', N'Xuất vật tư thay thế theo phiếu sửa chữa');
GO

/* Cập nhật maPhieuXuat cho PhieuSuaChua */
UPDATE PhieuSuaChua SET maPhieuXuat = 'PX001' WHERE maPSC = 'SC001';
UPDATE PhieuSuaChua SET maPhieuXuat = 'PX002' WHERE maPSC = 'SC002';
GO

/* 21. Chi tiet xuat kho */
INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat, maPT, soLuongYeuCau, soLuongXuat, donGia, tinhTrangXuat) VALUES
('PX001', 'PT001', 1, 1, 150000, N'Đã xuất'),
('PX001', 'PT004', 4, 4, 120000, N'Đã xuất'),
('PX002', 'PT005', 1, 0, 450000, N'Chờ xuất');
GO

/* 22. Quyen bo sung (Dữ liệu mẫu) */
INSERT INTO QuyenBoSung (maNV, maQuyen) VALUES
('NV04', 'PHAN_QUYEN'),
('NV04', 'NHAN_VIEN'),
('NV04', 'CAU_HINH');
GO

/* 23. Nhat ky quyen (Dữ liệu mẫu) */
INSERT INTO NhatKyQuyen (nguoiThucHien, doiTuong, noiDung) VALUES
('SYSTEM', 'NV04', N'Khởi tạo quyền hệ thống mặc định cho Quản lý');
GO

-- ============================================================
-- PHẦN III: DỮ LIỆU MẪU BỔ SUNG
-- ============================================================
USE QuanLySuaChuaXe;
GO
SET DATEFORMAT ymd;
GO
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNV=N'NV101')
 INSERT INTO NhanVien (maNV,hoTen,chucVu,soDT,email,bacNghe,boPhan,ngayVaoLam,trangThai) VALUES (N'NV101',N'Lê Văn Cường',N'Nhân viên sửa chữa',N'0908100101',N'nv101@example.com',N'4/7',N'Xưởng dịch vụ',N'2025-01-01',N'Đang làm việc');
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maNV=N'NV101')
 INSERT INTO TaiKhoan (maNV,taiKhoan,matKhau,trangThai) VALUES (N'NV101',N'demo101',N'123',N'HOAT_DONG');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNV=N'NV102')
 INSERT INTO NhanVien (maNV,hoTen,chucVu,soDT,email,bacNghe,boPhan,ngayVaoLam,trangThai) VALUES (N'NV102',N'Hoàng Đình Kha',N'Nhân viên sửa chữa',N'0908100102',N'nv102@example.com',N'4/7',N'Xưởng dịch vụ',N'2025-01-01',N'Đang làm việc');
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maNV=N'NV102')
 INSERT INTO TaiKhoan (maNV,taiKhoan,matKhau,trangThai) VALUES (N'NV102',N'demo102',N'123',N'HOAT_DONG');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNV=N'NV103')
 INSERT INTO NhanVien (maNV,hoTen,chucVu,soDT,email,bacNghe,boPhan,ngayVaoLam,trangThai) VALUES (N'NV103',N'Nguyễn Thị Hạnh',N'Nhân viên tiếp tân',N'0908100103',N'nv103@example.com',N'4/7',N'Xưởng dịch vụ',N'2025-01-01',N'Đang làm việc');
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maNV=N'NV103')
 INSERT INTO TaiKhoan (maNV,taiKhoan,matKhau,trangThai) VALUES (N'NV103',N'demo103',N'123',N'HOAT_DONG');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNV=N'NV104')
 INSERT INTO NhanVien (maNV,hoTen,chucVu,soDT,email,bacNghe,boPhan,ngayVaoLam,trangThai) VALUES (N'NV104',N'Trần Quốc Phong',N'Nhân viên kho',N'0908100104',N'nv104@example.com',N'4/7',N'Xưởng dịch vụ',N'2025-01-01',N'Đang làm việc');
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maNV=N'NV104')
 INSERT INTO TaiKhoan (maNV,taiKhoan,matKhau,trangThai) VALUES (N'NV104',N'demo104',N'123',N'HOAT_DONG');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH101')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH101',N'Nguyễn Minh Anh',N'0918200101',N'079100000101',N'12 Nguyễn Văn Cừ, TP.HCM',N'kh101@example.com',N'Khách hàng thân thiết',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH102')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH102',N'Trần Ngọc Lan',N'0918200102',N'079100000102',N'13 Nguyễn Văn Cừ, TP.HCM',N'kh102@example.com',N'Khách hàng thân thiết',N'2026-10-23');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH103')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH103',N'Lê Quốc Bảo',N'0918200103',N'079100000103',N'14 Nguyễn Văn Cừ, TP.HCM',N'kh103@example.com',N'Khách hàng thân thiết',N'2026-10-19');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH104')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH104',N'Phạm Thu Hà',N'0918200104',N'079100000104',N'15 Nguyễn Văn Cừ, TP.HCM',N'kh104@example.com',N'Khách mới',N'2026-10-15');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH105')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH105',N'Hoàng Đức Huy',N'0918200105',N'079100000105',N'16 Nguyễn Văn Cừ, TP.HCM',N'kh105@example.com',N'Khách mới',N'2026-10-11');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH106')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH106',N'Đặng Thị Mai',N'0918200106',N'079100000106',N'17 Nguyễn Văn Cừ, TP.HCM',N'kh106@example.com',N'Khách mới',N'2026-10-07');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH107')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH107',N'Bùi Thanh Tùng',N'0918200107',N'079100000107',N'18 Nguyễn Văn Cừ, TP.HCM',N'kh107@example.com',N'Khách mới',N'2026-10-03');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH108')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH108',N'Đỗ Hải Yến',N'0918200108',N'079100000108',N'19 Nguyễn Văn Cừ, TP.HCM',N'kh108@example.com',N'Khách mới',N'2026-09-29');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH109')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH109',N'Vũ Gia Hưng',N'0918200109',N'079100000109',N'20 Nguyễn Văn Cừ, TP.HCM',N'kh109@example.com',N'Khách mới',N'2026-09-25');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH110')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH110',N'Ngô Bảo Châu',N'0918200110',N'079100000110',N'21 Nguyễn Văn Cừ, TP.HCM',N'kh110@example.com',N'Khách mới',N'2026-09-21');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH111')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH111',N'Dương Tuấn Kiệt',N'0918200111',N'079100000111',N'22 Nguyễn Văn Cừ, TP.HCM',N'kh111@example.com',N'Khách mới',N'2026-09-17');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH112')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH112',N'Hồ Mỹ Linh',N'0918200112',N'079100000112',N'23 Nguyễn Văn Cừ, TP.HCM',N'kh112@example.com',N'Khách mới',N'2026-09-13');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH113')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH113',N'Phan Quang Dũng',N'0918200113',N'079100000113',N'24 Nguyễn Văn Cừ, TP.HCM',N'kh113@example.com',N'Khách mới',N'2026-09-09');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH114')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH114',N'Võ Khánh Ngọc',N'0918200114',N'079100000114',N'25 Nguyễn Văn Cừ, TP.HCM',N'kh114@example.com',N'Khách mới',N'2026-09-05');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH115')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH115',N'Mai Anh Khoa',N'0918200115',N'079100000115',N'26 Nguyễn Văn Cừ, TP.HCM',N'kh115@example.com',N'Khách mới',N'2026-09-01');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH116')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH116',N'Đinh Phương Thảo',N'0918200116',N'079100000116',N'27 Nguyễn Văn Cừ, TP.HCM',N'kh116@example.com',N'Khách mới',N'2026-08-28');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH117')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH117',N'Cao Nhật Minh',N'0918200117',N'079100000117',N'28 Nguyễn Văn Cừ, TP.HCM',N'kh117@example.com',N'Khách mới',N'2026-08-24');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH118')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH118',N'Lý Thùy Dương',N'0918200118',N'079100000118',N'29 Nguyễn Văn Cừ, TP.HCM',N'kh118@example.com',N'Khách mới',N'2026-08-20');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH119')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH119',N'Tạ Hoàng Long',N'0918200119',N'079100000119',N'30 Nguyễn Văn Cừ, TP.HCM',N'kh119@example.com',N'Khách mới',N'2026-08-16');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH120')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH120',N'Trịnh Thanh Trúc',N'0918200120',N'079100000120',N'31 Nguyễn Văn Cừ, TP.HCM',N'kh120@example.com',N'Khách hàng thân thiết',N'2026-08-12');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO101')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO101',N'51A-20101',N'Toyota',N'Vios',2020,N'KH101',N'MAY101',N'Trắng',N'10000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO102')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO102',N'51A-20102',N'Honda',N'City',2021,N'KH102',N'MAY102',N'Đen',N'13000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO103')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO103',N'51A-20103',N'Mazda',N'CX-5',2022,N'KH103',N'MAY103',N'Đỏ',N'16000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO104')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO104',N'51A-20104',N'Hyundai',N'Accent',2023,N'KH104',N'MAY104',N'Bạc',N'19000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO105')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO105',N'51A-20105',N'Kia',N'Seltos',2024,N'KH105',N'MAY105',N'Trắng',N'22000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO106')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO106',N'51A-20106',N'Ford',N'Ranger',2025,N'KH106',N'MAY106',N'Đen',N'25000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO107')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO107',N'51A-20107',N'Mitsubishi',N'Xpander',2020,N'KH107',N'MAY107',N'Đỏ',N'28000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO108')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO108',N'51A-20108',N'Suzuki',N'XL7',2021,N'KH108',N'MAY108',N'Bạc',N'31000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO109')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO109',N'51A-20109',N'VinFast',N'VF 8',2022,N'KH109',N'MAY109',N'Trắng',N'34000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO110')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO110',N'51A-20110',N'Nissan',N'Navara',2023,N'KH110',N'MAY110',N'Đen',N'37000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO111')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO111',N'51A-20111',N'Toyota',N'Vios',2024,N'KH111',N'MAY111',N'Đỏ',N'40000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO112')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO112',N'51A-20112',N'Honda',N'City',2025,N'KH112',N'MAY112',N'Bạc',N'43000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO113')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO113',N'51A-20113',N'Mazda',N'CX-5',2020,N'KH113',N'MAY113',N'Trắng',N'46000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO114')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO114',N'51A-20114',N'Hyundai',N'Accent',2021,N'KH114',N'MAY114',N'Đen',N'49000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO115')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO115',N'51A-20115',N'Kia',N'Seltos',2022,N'KH115',N'MAY115',N'Đỏ',N'52000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO116')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO116',N'51A-20116',N'Ford',N'Ranger',2023,N'KH116',N'MAY116',N'Bạc',N'55000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO117')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO117',N'51A-20117',N'Mitsubishi',N'Xpander',2024,N'KH117',N'MAY117',N'Trắng',N'58000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO118')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO118',N'51A-20118',N'Suzuki',N'XL7',2025,N'KH118',N'MAY118',N'Đen',N'61000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO119')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO119',N'51A-20119',N'VinFast',N'VF 8',2020,N'KH119',N'MAY119',N'Đỏ',N'64000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO120')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO120',N'51A-20120',N'Nissan',N'Navara',2021,N'KH120',N'MAY120',N'Bạc',N'67000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO121')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO121',N'51A-20121',N'Toyota',N'Vios',2022,N'KH101',N'MAY121',N'Trắng',N'70000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO122')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO122',N'51A-20122',N'Honda',N'City',2023,N'KH102',N'MAY122',N'Đen',N'73000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO123')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO123',N'51A-20123',N'Mazda',N'CX-5',2024,N'KH103',N'MAY123',N'Đỏ',N'76000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO124')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO124',N'51A-20124',N'Hyundai',N'Accent',2025,N'KH104',N'MAY124',N'Bạc',N'79000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO125')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO125',N'51A-20125',N'Kia',N'Seltos',2020,N'KH105',N'MAY125',N'Trắng',N'82000',N'2026-10-27');
IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNCC=N'NCC101')
 INSERT INTO NhaCungCap (maNCC,tenNCC,soDT,diaChi,email) VALUES (N'NCC101',N'Phụ tùng ô tô An Phát',N'02838100101',N'TP.HCM',N'ncc101@example.com');
IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNCC=N'NCC102')
 INSERT INTO NhaCungCap (maNCC,tenNCC,soDT,diaChi,email) VALUES (N'NCC102',N'Vật tư gara Minh Long',N'02838100102',N'TP.HCM',N'ncc102@example.com');
IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNCC=N'NCC103')
 INSERT INTO NhaCungCap (maNCC,tenNCC,soDT,diaChi,email) VALUES (N'NCC103',N'Lốp xe Thành Công',N'02838100103',N'TP.HCM',N'ncc103@example.com');
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT101')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT101',N'Lọc dầu động cơ',N'Cái',150000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO101')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO101',N'PT101',0,N'Kệ 1 - ô 1',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT102')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT102',N'Lọc gió động cơ',N'Cái',220000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO102')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO102',N'PT102',0,N'Kệ 1 - ô 2',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT103')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT103',N'Bugi iridium',N'Cái',250000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO103')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO103',N'PT103',0,N'Kệ 1 - ô 3',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT104')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT104',N'Dầu động cơ 5W30',N'Lít',180000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO104')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO104',N'PT104',0,N'Kệ 1 - ô 4',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT105')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT105',N'Má phanh trước',N'Bộ',850000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO105')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO105',N'PT105',0,N'Kệ 1 - ô 5',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT106')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT106',N'Lọc gió điều hòa',N'Cái',200000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO106')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO106',N'PT106',0,N'Kệ 2 - ô 1',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT107')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT107',N'Ắc quy 12V 60Ah',N'Bình',1850000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO107')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO107',N'PT107',0,N'Kệ 2 - ô 2',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT108')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT108',N'Lốp 205/55R16',N'Cái',1600000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO108')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO108',N'PT108',0,N'Kệ 2 - ô 3',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT109')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT109',N'Gạt mưa trước',N'Bộ',350000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO109')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO109',N'PT109',0,N'Kệ 2 - ô 4',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT110')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT110',N'Nước làm mát',N'Lít',120000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO110')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO110',N'PT110',0,N'Kệ 2 - ô 5',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT111')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT111',N'Dầu phanh DOT4',N'Chai',180000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO111')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO111',N'PT111',0,N'Kệ 3 - ô 1',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT112')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT112',N'Dây curoa tổng',N'Sợi',450000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO112')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO112',N'PT112',0,N'Kệ 3 - ô 2',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT113')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT113',N'Bóng đèn H4',N'Cái',120000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO113')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO113',N'PT113',0,N'Kệ 3 - ô 3',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT114')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT114',N'Giảm xóc trước',N'Cái',1400000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO114')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO114',N'PT114',0,N'Kệ 3 - ô 4',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT115')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT115',N'Rotuyn lái',N'Cái',650000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO115')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO115',N'PT115',0,N'Kệ 3 - ô 5',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT116')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT116',N'Gioăng nắp máy',N'Bộ',450000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO116')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO116',N'PT116',0,N'Kệ 4 - ô 1',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT117')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT117',N'Cảm biến oxy',N'Cái',950000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO117')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO117',N'PT117',0,N'Kệ 4 - ô 2',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT118')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT118',N'Bơm nước',N'Cái',1250000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO118')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO118',N'PT118',0,N'Kệ 4 - ô 3',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT119')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT119',N'Lọc nhiên liệu',N'Cái',380000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO119')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO119',N'PT119',0,N'Kệ 4 - ô 4',5,200);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE maPT=N'PT120')
 INSERT INTO PhuTung (maPT,tenPT,donViTinh,donGiaBan) VALUES (N'PT120',N'Dung dịch vệ sinh kim phun',N'Chai',180000);
IF NOT EXISTS (SELECT 1 FROM Kho WHERE maKho=N'KHO120')
 INSERT INTO Kho (maKho,maPT,soLuongTon,viTriKho,soLTT,soLTD) VALUES (N'KHO120',N'PT120',0,N'Kệ 4 - ô 5',5,200);
IF NOT EXISTS (SELECT 1 FROM PhieuNhapKho WHERE maPhieuNhap='PN101') BEGIN
INSERT INTO PhieuNhapKho(maPhieuNhap,maNCC,maNV,ngayNhap,tongTien,trangThai) VALUES('PN101','NCC101','NV03','2026-09-01',115045000,'DA_NHAP');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT101')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT101',N'Lọc dầu động cơ',5,105000);
UPDATE Kho SET soLuongTon=soLuongTon+5 WHERE maPT='PT101';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT102')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT102',N'Lọc gió động cơ',30,154000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT102';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT103')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT103',N'Bugi iridium',30,175000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT103';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT104')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT104',N'Dầu động cơ 5W30',30,126000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT104';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT105')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT105',N'Má phanh trước',30,595000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT105';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT106')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT106',N'Lọc gió điều hòa',5,140000);
UPDATE Kho SET soLuongTon=soLuongTon+5 WHERE maPT='PT106';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT107')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT107',N'Ắc quy 12V 60Ah',30,1295000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT107';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT108')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT108',N'Lốp 205/55R16',30,1120000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT108';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT109')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT109',N'Gạt mưa trước',30,245000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT109';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN101' AND maPT=N'PT110')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN101',N'PT110',N'Nước làm mát',30,84000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT110';
END;
IF NOT EXISTS (SELECT 1 FROM PhieuNhapKho WHERE maPhieuNhap='PN102') BEGIN
INSERT INTO PhieuNhapKho(maPhieuNhap,maNCC,maNV,ngayNhap,tongTien,trangThai) VALUES('PN102','NCC101','NV03','2026-09-01',115185000,'DA_NHAP');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT111')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT111',N'Dầu phanh DOT4',5,126000);
UPDATE Kho SET soLuongTon=soLuongTon+5 WHERE maPT='PT111';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT112')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT112',N'Dây curoa tổng',30,315000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT112';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT113')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT113',N'Bóng đèn H4',30,84000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT113';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT114')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT114',N'Giảm xóc trước',30,980000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT114';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT115')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT115',N'Rotuyn lái',30,455000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT115';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT116')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT116',N'Gioăng nắp máy',5,315000);
UPDATE Kho SET soLuongTon=soLuongTon+5 WHERE maPT='PT116';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT117')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT117',N'Cảm biến oxy',30,665000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT117';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT118')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT118',N'Bơm nước',30,875000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT118';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT119')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT119',N'Lọc nhiên liệu',30,266000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT119';
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN102' AND maPT=N'PT120')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN102',N'PT120',N'Dung dịch vệ sinh kim phun',30,126000);
UPDATE Kho SET soLuongTon=soLuongTon+30 WHERE maPT='PT120';
END;
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN101')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN101',N'NV01',N'KH101',N'VINDEMO101',N'2026-10-15T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC101')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC101',N'TN101',N'NV101',N'KH101',N'VINDEMO101',N'2026-10-15T09:00:00',N'2026-10-16',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC101' AND maPT=N'PT101')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC101',N'PT101',N'Thay thế theo kết quả kiểm tra',N'Lọc dầu động cơ',1,150000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC101' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC101',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX101') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX101')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX101',N'SC101',N'NV03',N'2026-10-15T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX101' AND maPT=N'PT101')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX101',N'PT101',1,1,150000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT101';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD101')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD101',N'KH101',N'NV05',N'2026-10-15',N'TIEN_MAT',200000,150000,0,350000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD101' AND maPT=N'PT101')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD101',N'PT101',N'Lọc dầu động cơ',1,150000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD101' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD101',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH101')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH101',N'2026-10-15',N'2027-04-15',N'CON_HAN',N'KH101',N'VINDEMO101',N'HD101',N'NV01',N'2026-10-15',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuBaoHanh WHERE maPBH=N'PBH101' AND maPT=N'PT101')
 INSERT INTO ChiTietPhieuBaoHanh (maPBH,maPT,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH101',N'PT101',N'Lọc dầu động cơ',6,N'2027-04-15',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN102')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN102',N'NV01',N'KH102',N'VINDEMO102',N'2026-10-16T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC102')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC102',N'TN102',N'NV102',N'KH102',N'VINDEMO102',N'2026-10-16T09:00:00',N'2026-10-17',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC102' AND maPT=N'PT102')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC102',N'PT102',N'Thay thế theo kết quả kiểm tra',N'Lọc gió động cơ',1,220000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC102' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC102',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX102') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX102')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX102',N'SC102',N'NV03',N'2026-10-16T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX102' AND maPT=N'PT102')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX102',N'PT102',1,1,220000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT102';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD102')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD102',N'KH102',N'NV05',N'2026-10-16',N'CHUYEN_KHOAN',200000,220000,0,420000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD102' AND maPT=N'PT102')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD102',N'PT102',N'Lọc gió động cơ',1,220000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD102' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD102',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH102')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH102',N'2026-10-16',N'2027-04-16',N'CON_HAN',N'KH102',N'VINDEMO102',N'HD102',N'NV01',N'2026-10-16',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuBaoHanh WHERE maPBH=N'PBH102' AND maPT=N'PT102')
 INSERT INTO ChiTietPhieuBaoHanh (maPBH,maPT,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH102',N'PT102',N'Lọc gió động cơ',6,N'2027-04-16',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN103')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN103',N'NV01',N'KH103',N'VINDEMO103',N'2026-10-17T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC103')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC103',N'TN103',N'NV101',N'KH103',N'VINDEMO103',N'2026-10-17T09:00:00',N'2026-10-18',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC103' AND maPT=N'PT103')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC103',N'PT103',N'Thay thế theo kết quả kiểm tra',N'Bugi iridium',1,250000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC103' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC103',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX103') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX103')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX103',N'SC103',N'NV03',N'2026-10-17T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX103' AND maPT=N'PT103')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX103',N'PT103',1,1,250000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT103';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD103')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD103',N'KH103',N'NV05',N'2026-10-17',N'THE_POS',200000,250000,0,450000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD103' AND maPT=N'PT103')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD103',N'PT103',N'Bugi iridium',1,250000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD103' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD103',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH103')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH103',N'2026-10-17',N'2027-04-17',N'CON_HAN',N'KH103',N'VINDEMO103',N'HD103',N'NV01',N'2026-10-17',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuBaoHanh WHERE maPBH=N'PBH103' AND maPT=N'PT103')
 INSERT INTO ChiTietPhieuBaoHanh (maPBH,maPT,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH103',N'PT103',N'Bugi iridium',6,N'2027-04-17',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN104')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN104',N'NV01',N'KH104',N'VINDEMO104',N'2026-10-18T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC104')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC104',N'TN104',N'NV102',N'KH104',N'VINDEMO104',N'2026-10-18T09:00:00',N'2026-10-19',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC104' AND maPT=N'PT104')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC104',N'PT104',N'Thay thế theo kết quả kiểm tra',N'Dầu động cơ 5W30',1,180000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC104' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC104',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX104') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX104')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX104',N'SC104',N'NV03',N'2026-10-18T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX104' AND maPT=N'PT104')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX104',N'PT104',1,1,180000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT104';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD104')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD104',N'KH104',N'NV05',N'2026-10-18',N'TIEN_MAT',200000,180000,0,380000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD104' AND maPT=N'PT104')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD104',N'PT104',N'Dầu động cơ 5W30',1,180000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD104' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD104',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH104')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH104',N'2026-10-18',N'2027-04-18',N'CON_HAN',N'KH104',N'VINDEMO104',N'HD104',N'NV01',N'2026-10-18',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuBaoHanh WHERE maPBH=N'PBH104' AND maPT=N'PT104')
 INSERT INTO ChiTietPhieuBaoHanh (maPBH,maPT,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH104',N'PT104',N'Dầu động cơ 5W30',6,N'2027-04-18',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN105')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN105',N'NV01',N'KH105',N'VINDEMO105',N'2026-10-19T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC105')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC105',N'TN105',N'NV101',N'KH105',N'VINDEMO105',N'2026-10-19T09:00:00',N'2026-10-20',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC105' AND maPT=N'PT105')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC105',N'PT105',N'Thay thế theo kết quả kiểm tra',N'Má phanh trước',1,850000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC105' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC105',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX105') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX105')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX105',N'SC105',N'NV03',N'2026-10-19T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX105' AND maPT=N'PT105')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX105',N'PT105',1,1,850000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT105';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD105')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD105',N'KH105',N'NV05',N'2026-10-19',N'CHUYEN_KHOAN',200000,850000,0,1050000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD105' AND maPT=N'PT105')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD105',N'PT105',N'Má phanh trước',1,850000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD105' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD105',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH105')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH105',N'2026-10-19',N'2027-04-19',N'CON_HAN',N'KH105',N'VINDEMO105',N'HD105',N'NV01',N'2026-10-19',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuBaoHanh WHERE maPBH=N'PBH105' AND maPT=N'PT105')
 INSERT INTO ChiTietPhieuBaoHanh (maPBH,maPT,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH105',N'PT105',N'Má phanh trước',6,N'2027-04-19',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN106')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN106',N'NV01',N'KH106',N'VINDEMO106',N'2026-10-20T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC106')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC106',N'TN106',N'NV102',N'KH106',N'VINDEMO106',N'2026-10-20T09:00:00',N'2026-10-21',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC106' AND maPT=N'PT106')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC106',N'PT106',N'Thay thế theo kết quả kiểm tra',N'Lọc gió điều hòa',1,200000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC106' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC106',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX106') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX106')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX106',N'SC106',N'NV03',N'2026-10-20T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX106' AND maPT=N'PT106')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX106',N'PT106',1,1,200000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT106';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD106')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD106',N'KH106',N'NV05',N'2026-10-20',N'THE_POS',200000,200000,0,400000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD106' AND maPT=N'PT106')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD106',N'PT106',N'Lọc gió điều hòa',1,200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD106' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD106',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH106')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH106',N'2026-10-20',N'2027-04-20',N'CON_HAN',N'KH106',N'VINDEMO106',N'HD106',N'NV01',N'2026-10-20',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuBaoHanh WHERE maPBH=N'PBH106' AND maPT=N'PT106')
 INSERT INTO ChiTietPhieuBaoHanh (maPBH,maPT,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH106',N'PT106',N'Lọc gió điều hòa',6,N'2027-04-20',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN107')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN107',N'NV01',N'KH107',N'VINDEMO107',N'2026-10-21T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC107')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC107',N'TN107',N'NV101',N'KH107',N'VINDEMO107',N'2026-10-21T09:00:00',N'2026-10-22',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC107' AND maPT=N'PT107')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC107',N'PT107',N'Thay thế theo kết quả kiểm tra',N'Ắc quy 12V 60Ah',1,1850000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC107' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC107',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX107') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX107')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX107',N'SC107',N'NV03',N'2026-10-21T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX107' AND maPT=N'PT107')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX107',N'PT107',1,1,1850000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT107';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD107')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD107',N'KH107',N'NV05',N'2026-10-21',N'TIEN_MAT',200000,1850000,0,2050000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD107' AND maPT=N'PT107')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD107',N'PT107',N'Ắc quy 12V 60Ah',1,1850000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD107' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD107',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN108')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN108',N'NV01',N'KH108',N'VINDEMO108',N'2026-10-22T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC108')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC108',N'TN108',N'NV102',N'KH108',N'VINDEMO108',N'2026-10-22T09:00:00',N'2026-10-23',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC108' AND maPT=N'PT108')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC108',N'PT108',N'Thay thế theo kết quả kiểm tra',N'Lốp 205/55R16',1,1600000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC108' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC108',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX108') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX108')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX108',N'SC108',N'NV03',N'2026-10-22T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX108' AND maPT=N'PT108')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX108',N'PT108',1,1,1600000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT108';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD108')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD108',N'KH108',N'NV05',N'2026-10-22',N'CHUYEN_KHOAN',200000,1600000,0,1800000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD108' AND maPT=N'PT108')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD108',N'PT108',N'Lốp 205/55R16',1,1600000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD108' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD108',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN109')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN109',N'NV01',N'KH109',N'VINDEMO109',N'2026-10-23T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC109')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC109',N'TN109',N'NV101',N'KH109',N'VINDEMO109',N'2026-10-23T09:00:00',N'2026-10-24',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC109' AND maPT=N'PT109')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC109',N'PT109',N'Thay thế theo kết quả kiểm tra',N'Gạt mưa trước',1,350000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC109' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC109',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX109') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX109')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX109',N'SC109',N'NV03',N'2026-10-23T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX109' AND maPT=N'PT109')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX109',N'PT109',1,1,350000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT109';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD109')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD109',N'KH109',N'NV05',N'2026-10-23',N'THE_POS',200000,350000,0,550000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD109' AND maPT=N'PT109')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD109',N'PT109',N'Gạt mưa trước',1,350000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD109' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD109',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN110')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN110',N'NV01',N'KH110',N'VINDEMO110',N'2026-10-24T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC110')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC110',N'TN110',N'NV102',N'KH110',N'VINDEMO110',N'2026-10-24T09:00:00',N'2026-10-25',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC110' AND maPT=N'PT110')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC110',N'PT110',N'Thay thế theo kết quả kiểm tra',N'Nước làm mát',1,120000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC110' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC110',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX110') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX110')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX110',N'SC110',N'NV03',N'2026-10-24T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX110' AND maPT=N'PT110')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX110',N'PT110',1,1,120000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT110';
END;
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD=N'HD110')
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD110',N'KH110',N'NV05',N'2026-10-24',N'TIEN_MAT',200000,120000,0,320000,N'Bảo dưỡng định kỳ',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD110' AND maPT=N'PT110')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD110',N'PT110',N'Nước làm mát',1,120000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD110' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD110',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN111')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN111',N'NV01',N'KH111',N'VINDEMO111',N'2026-10-25T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC111')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC111',N'TN111',N'NV101',N'KH111',N'VINDEMO111',N'2026-10-25T09:00:00',N'2026-10-26',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC111' AND maPT=N'PT111')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC111',N'PT111',N'Thay thế theo kết quả kiểm tra',N'Dầu phanh DOT4',1,180000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC111' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC111',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX111') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX111')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX111',N'SC111',N'NV03',N'2026-10-25T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX111' AND maPT=N'PT111')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX111',N'PT111',1,1,180000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT111';
END;
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN112')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN112',N'NV01',N'KH112',N'VINDEMO112',N'2026-10-26T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC112')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC112',N'TN112',N'NV102',N'KH112',N'VINDEMO112',N'2026-10-26T09:00:00',N'2026-10-27',N'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC112' AND maPT=N'PT112')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC112',N'PT112',N'Thay thế theo kết quả kiểm tra',N'Dây curoa tổng',1,450000,N'DA_HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC112' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC112',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX112') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX112')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX112',N'SC112',N'NV03',N'2026-10-26T10:00:00',N'Đã xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX112' AND maPT=N'PT112')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX112',N'PT112',1,1,450000,N'Đã xuất');
UPDATE Kho SET soLuongTon=soLuongTon-1 WHERE maPT='PT112';
END;
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN113')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN113',N'NV01',N'KH113',N'VINDEMO113',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Chờ tiếp nhận');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC113')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC113',N'TN113',N'NV101',N'KH113',N'VINDEMO113',N'2026-10-27T09:00:00',N'2026-10-28',N'CHO_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC113' AND maPT=N'PT113')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC113',N'PT113',N'Thay thế theo kết quả kiểm tra',N'Bóng đèn H4',1,120000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC113' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC113',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX113') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX113')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX113',N'SC113',N'NV03',N'2026-10-27T10:00:00',N'Chờ xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX113' AND maPT=N'PT113')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX113',N'PT113',1,0,120000,N'Chờ xuất');
END;
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN114')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN114',N'NV01',N'KH114',N'VINDEMO114',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Đã phân công');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC114')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC114',N'TN114',N'NV102',N'KH114',N'VINDEMO114',N'2026-10-27T09:00:00',N'2026-10-28',N'CHO_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC114' AND maPT=N'PT114')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC114',N'PT114',N'Thay thế theo kết quả kiểm tra',N'Giảm xóc trước',1,1400000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC114' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC114',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX114') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX114')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX114',N'SC114',N'NV03',N'2026-10-27T10:00:00',N'Chờ xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX114' AND maPT=N'PT114')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX114',N'PT114',1,0,1400000,N'Chờ xuất');
END;
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN115')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN115',N'NV01',N'KH115',N'VINDEMO115',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Đang sửa');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC115')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC115',N'TN115',N'NV101',N'KH115',N'VINDEMO115',N'2026-10-27T09:00:00',N'2026-10-28',N'DANG_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC115' AND maPT=N'PT115')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC115',N'PT115',N'Thay thế theo kết quả kiểm tra',N'Rotuyn lái',1,650000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC115' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC115',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX115') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX115')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX115',N'SC115',N'NV03',N'2026-10-27T10:00:00',N'Chờ xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX115' AND maPT=N'PT115')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX115',N'PT115',1,0,650000,N'Chờ xuất');
END;
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN116')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN116',N'NV01',N'KH116',N'VINDEMO116',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Chờ phụ tùng');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC116')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC116',N'TN116',N'NV102',N'KH116',N'VINDEMO116',N'2026-10-27T09:00:00',N'2026-10-28',N'DANG_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC116' AND maPT=N'PT116')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC116',N'PT116',N'Thay thế theo kết quả kiểm tra',N'Gioăng nắp máy',1,450000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC116' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC116',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat='PX116') BEGIN
IF NOT EXISTS (SELECT 1 FROM PhieuXuatKho WHERE maPhieuXuat=N'PX116')
 INSERT INTO PhieuXuatKho (maPhieuXuat,maPSC,maNV,ngayXuat,trangThai,lyDoXuat) VALUES (N'PX116',N'SC116',N'NV03',N'2026-10-27T10:00:00',N'Chờ xuất',N'Xuất theo phiếu sửa chữa');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuXuatKho WHERE maPhieuXuat=N'PX116' AND maPT=N'PT116')
 INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat,maPT,soLuongYeuCau,soLuongXuat,donGia,tinhTrangXuat) VALUES (N'PX116',N'PT116',1,0,450000,N'Chờ xuất');
END;
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN117')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN117',N'NV01',N'KH117',N'VINDEMO117',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Chờ tiếp nhận');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC117')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC117',N'TN117',N'NV101',N'KH117',N'VINDEMO117',N'2026-10-27T09:00:00',N'2026-10-28',N'CHO_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC117' AND maPT=N'PT117')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC117',N'PT117',N'Thay thế theo kết quả kiểm tra',N'Cảm biến oxy',1,950000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC117' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC117',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN118')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN118',N'NV01',N'KH118',N'VINDEMO118',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Đã phân công');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC118')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC118',N'TN118',N'NV102',N'KH118',N'VINDEMO118',N'2026-10-27T09:00:00',N'2026-10-28',N'CHO_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC118' AND maPT=N'PT118')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC118',N'PT118',N'Thay thế theo kết quả kiểm tra',N'Bơm nước',1,1250000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC118' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC118',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN119')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN119',N'NV01',N'KH119',N'VINDEMO119',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Đang sửa');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC119')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC119',N'TN119',N'NV101',N'KH119',N'VINDEMO119',N'2026-10-27T09:00:00',N'2026-10-28',N'DANG_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC119' AND maPT=N'PT119')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC119',N'PT119',N'Thay thế theo kết quả kiểm tra',N'Lọc nhiên liệu',1,380000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC119' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC119',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN120')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN120',N'NV01',N'KH120',N'VINDEMO120',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Chờ phụ tùng');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC120')
 INSERT INTO PhieuSuaChua (maPSC,maPTN,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC120',N'TN120',N'NV102',N'KH120',N'VINDEMO120',N'2026-10-27T09:00:00',N'2026-10-28',N'DANG_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC120' AND maPT=N'PT120')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC120',N'PT120',N'Thay thế theo kết quả kiểm tra',N'Dung dịch vệ sinh kim phun',1,180000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC120' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC120',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuNhapKho WHERE maPhieuNhap=N'PN103')
 INSERT INTO PhieuNhapKho (maPhieuNhap,maNCC,maNV,ngayNhap,tongTien,trangThai,ngayDuKienGiao,ghiChu) VALUES (N'PN103',N'NCC102',N'NV03',N'2026-10-27',1500000,N'CHO_NHAP',N'2026-10-30',N'Đặt hàng, chưa cộng tồn kho');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN103' AND maPT=N'PT101')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN103',N'PT101',N'Lọc dầu động cơ',10,150000);

-- Đồng bộ liên kết maPTN nếu còn sót dòng cũ
UPDATE sc SET sc.maPTN = tn.maPTN
FROM PhieuSuaChua sc
JOIN PhieuTiepNhan tn ON sc.maPSC = 'SC' + SUBSTRING(tn.maPTN, 3, 18)
WHERE sc.maPTN IS NULL;

-- Đồng bộ liên kết maPhieuXuat từ PhieuXuatKho
UPDATE sc SET sc.maPhieuXuat = px.maPhieuXuat
FROM PhieuSuaChua sc
JOIN PhieuXuatKho px ON sc.maPSC = px.maPSC
WHERE sc.maPhieuXuat IS NULL;

-- Đồng bộ quyền bổ sung cho tài khoản Quản lý nếu chưa có
IF NOT EXISTS (SELECT 1 FROM QuyenBoSung WHERE maNV='NV04' AND maQuyen='PHAN_QUYEN')
    INSERT INTO QuyenBoSung (maNV, maQuyen) VALUES ('NV04', 'PHAN_QUYEN');
IF NOT EXISTS (SELECT 1 FROM QuyenBoSung WHERE maNV='NV04' AND maQuyen='NHAN_VIEN')
    INSERT INTO QuyenBoSung (maNV, maQuyen) VALUES ('NV04', 'NHAN_VIEN');
IF NOT EXISTS (SELECT 1 FROM QuyenBoSung WHERE maNV='NV04' AND maQuyen='CAU_HINH')
    INSERT INTO QuyenBoSung (maNV, maQuyen) VALUES ('NV04', 'CAU_HINH');
GO


-- Hóa đơn bán lẻ phụ tùng tại shop (khách hàng KH101.. và phụ tùng PT101.. đã tồn tại)
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD='HD201')
 INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES
 ('HD201', 'KH101', 'NV05', '2026-10-25', N'CHUYEN_KHOAN', 0, 420000, 0, 420000, N'Bán lẻ phụ tùng tại shop', N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD='HD201' AND maPT='PT101')
 INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD201', 'PT101', N'Lọc dầu động cơ', N'Chiếc', 1, 150000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD='HD201' AND maPT='PT103')
 INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD201', 'PT103', N'Bugi Denso Iridium', N'Cái', 2, 85000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD='HD201' AND maPT='PT110')
 INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD201', 'PT110', N'Nước làm mát cao cấp', N'Can', 1, 100000);

IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD='HD202')
 INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES
 ('HD202', 'KH102', 'NV05', '2026-10-26', N'TIEN_MAT', 0, 480000, 5, 456000, N'Bán lẻ dầu nhớt động cơ', N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD='HD202' AND maPT='PT104')
 INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD202', 'PT104', N'Dầu động cơ Castrol Magnatec', N'Lít', 4, 120000);

IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD='HD203')
 INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES
 ('HD203', 'KH103', 'NV05', '2026-10-27', N'TIEN_MAT', 0, 350000, 0, 350000, N'Khách đổi ý trả hàng hủy hóa đơn', N'DA_HUY');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD='HD203' AND maPT='PT109')
 INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD203', 'PT109', N'Cần gạt mưa Bosch AeroTwin', N'Bộ', 1, 350000);

GO

-- ============================================================
-- PHẦN IV: DỮ LIỆU HÓA ĐƠN THỰC TẾ & ĐỒNG BỘ DOANH THU HỆ THỐNG
-- Dữ liệu hóa đơn, sửa chữa và tiếp nhận cho 12 tháng năm 2026 và năm 2025
-- Giúp thống kê doanh thu gara dựa 100% trên dữ liệu thực tế hệ thống
-- ============================================================

USE QuanLySuaChuaXe;
GO

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN301')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN301', 'NV01', 'KH001', 'VIN001', '2026-01-10T08:00:00', N'Bảo dưỡng 40.000km và thay ắc quy', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC301')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC301', 'TN301', 'NV02', 'KH001', 'VIN001', '2026-01-10T09:00:00', '2026-01-10', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD301')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD301', 'KH001', 'NV05', '2026-01-10', 'CHUYEN_KHOAN', 1800000, 4650000, 0, 6450000, N'Bảo dưỡng 40.000km và thay ắc quy', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD301')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD301', 1, N'Bảo dưỡng 40.000km và thay ắc quy', N'Gói', 1, 1800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD301')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD301', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng 40.000km và thay ắc quy)', N'Bộ', 1, 4650000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN302')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN302', 'NV01', 'KH101', 'VINDEMO101', '2026-01-18T08:00:00', N'Đại tu hệ thống gầm và thay lốp', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC302')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC302', 'TN302', 'NV101', 'KH101', 'VINDEMO101', '2026-01-18T09:00:00', '2026-01-18', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD302')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD302', 'KH101', 'NV05', '2026-01-18', 'TIEN_MAT', 2500000, 7450000, 0, 9950000, N'Đại tu hệ thống gầm và thay lốp', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD302')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD302', 1, N'Đại tu hệ thống gầm và thay lốp', N'Gói', 1, 2500000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD302')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD302', 'PT101', N'Vật tư linh kiện thay thế (Đại tu hệ thống gầm và thay lốp)', N'Bộ', 1, 7450000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN303')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN303', 'NV01', 'KH102', 'VINDEMO102', '2026-01-25T08:00:00', N'Bảo dưỡng định kỳ cấp 2', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC303')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC303', 'TN303', 'NV102', 'KH102', 'VINDEMO102', '2026-01-25T09:00:00', '2026-01-25', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD303')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD303', 'KH102', 'NV05', '2026-01-25', 'THE_POS', 800000, 2300000, 0, 3100000, N'Bảo dưỡng định kỳ cấp 2', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD303')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD303', 1, N'Bảo dưỡng định kỳ cấp 2', N'Gói', 1, 800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD303')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD303', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ cấp 2)', N'Bộ', 1, 2300000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN304')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN304', 'NV01', 'KH002', 'VIN002', '2026-02-12T08:00:00', N'Bảo dưỡng phanh và thay má phanh', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC304')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC304', 'TN304', 'NV02', 'KH002', 'VIN002', '2026-02-12T09:00:00', '2026-02-12', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD304')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD304', 'KH002', 'NV05', '2026-02-12', 'TIEN_MAT', 1200000, 3850000, 0, 5050000, N'Bảo dưỡng phanh và thay má phanh', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD304')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD304', 1, N'Bảo dưỡng phanh và thay má phanh', N'Gói', 1, 1200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD304')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD304', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng phanh và thay má phanh)', N'Bộ', 1, 3850000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN305')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN305', 'NV01', 'KH107', 'VINDEMO107', '2026-02-20T08:00:00', N'Bảo dưỡng lớn hệ thống điều hòa và làm mát', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC305')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC305', 'TN305', 'NV101', 'KH107', 'VINDEMO107', '2026-02-20T09:00:00', '2026-02-20', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD305')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD305', 'KH107', 'NV05', '2026-02-20', 'CHUYEN_KHOAN', 2400000, 6250000, 0, 8650000, N'Bảo dưỡng lớn hệ thống điều hòa và làm mát', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD305')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD305', 1, N'Bảo dưỡng lớn hệ thống điều hòa và làm mát', N'Gói', 1, 2400000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD305')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD305', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng lớn hệ thống điều hòa và làm mát)', N'Bộ', 1, 6250000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN306')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN306', 'NV01', 'KH103', 'VINDEMO103', '2026-02-26T08:00:00', N'Thay lọc nhớt, vệ sinh buồng đốt', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC306')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC306', 'TN306', 'NV102', 'KH103', 'VINDEMO103', '2026-02-26T09:00:00', '2026-02-26', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD306')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD306', 'KH103', 'NV05', '2026-02-26', 'CHUYEN_KHOAN', 1500000, 2000000, 0, 3500000, N'Thay lọc nhớt, vệ sinh buồng đốt', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD306')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD306', 1, N'Thay lọc nhớt, vệ sinh buồng đốt', N'Gói', 1, 1500000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD306')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD306', 'PT101', N'Vật tư linh kiện thay thế (Thay lọc nhớt, vệ sinh buồng đốt)', N'Bộ', 1, 2000000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN307')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN307', 'NV01', 'KH112', 'VINDEMO112', '2026-03-15T08:00:00', N'Thay giảm xóc và cân chỉnh góc đặt bánh xe', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC307')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC307', 'TN307', 'NV101', 'KH112', 'VINDEMO112', '2026-03-15T09:00:00', '2026-03-15', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD307')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD307', 'KH112', 'NV05', '2026-03-15', 'THE_POS', 2800000, 8400000, 0, 11200000, N'Thay giảm xóc và cân chỉnh góc đặt bánh xe', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD307')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD307', 1, N'Thay giảm xóc và cân chỉnh góc đặt bánh xe', N'Gói', 1, 2800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD307')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD307', 'PT101', N'Vật tư linh kiện thay thế (Thay giảm xóc và cân chỉnh góc đặt bánh xe)', N'Bộ', 1, 8400000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN308')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN308', 'NV01', 'KH120', 'VINDEMO120', '2026-03-24T08:00:00', N'Bảo dưỡng cấp lớn 80.000km', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC308')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC308', 'TN308', 'NV102', 'KH120', 'VINDEMO120', '2026-03-24T09:00:00', '2026-03-24', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD308')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD308', 'KH120', 'NV05', '2026-03-24', 'CHUYEN_KHOAN', 3200000, 8150000, 0, 11350000, N'Bảo dưỡng cấp lớn 80.000km', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD308')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD308', 1, N'Bảo dưỡng cấp lớn 80.000km', N'Gói', 1, 3200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD308')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD308', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng cấp lớn 80.000km)', N'Bộ', 1, 8150000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN309')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN309', 'NV01', 'KH101', 'VINDEMO101', '2026-04-08T08:00:00', N'Bảo dưỡng tổng thể đón hè', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC309')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC309', 'TN309', 'NV02', 'KH101', 'VINDEMO101', '2026-04-08T09:00:00', '2026-04-08', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD309')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD309', 'KH101', 'NV05', '2026-04-08', 'TIEN_MAT', 1900000, 4800000, 0, 6700000, N'Bảo dưỡng tổng thể đón hè', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD309')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD309', 1, N'Bảo dưỡng tổng thể đón hè', N'Gói', 1, 1900000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD309')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD309', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng tổng thể đón hè)', N'Bộ', 1, 4800000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN310')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN310', 'NV01', 'KH104', 'VINDEMO104', '2026-04-14T08:00:00', N'Kiểm tra điện lạnh và thay lốc điều hòa', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC310')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC310', 'TN310', 'NV101', 'KH104', 'VINDEMO104', '2026-04-14T09:00:00', '2026-04-14', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD310')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD310', 'KH104', 'NV05', '2026-04-14', 'CHUYEN_KHOAN', 2100000, 5200000, 0, 7300000, N'Kiểm tra điện lạnh và thay lốc điều hòa', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD310')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD310', 1, N'Kiểm tra điện lạnh và thay lốc điều hòa', N'Gói', 1, 2100000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD310')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD310', 'PT101', N'Vật tư linh kiện thay thế (Kiểm tra điện lạnh và thay lốc điều hòa)', N'Bộ', 1, 5200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN311')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN311', 'NV01', 'KH105', 'VINDEMO105', '2026-04-20T08:00:00', N'Bảo dưỡng phanh và thay dầu phanh', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC311')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC311', 'TN311', 'NV102', 'KH105', 'VINDEMO105', '2026-04-20T09:00:00', '2026-04-20', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD311')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD311', 'KH105', 'NV05', '2026-04-20', 'THE_POS', 1600000, 3900000, 0, 5500000, N'Bảo dưỡng phanh và thay dầu phanh', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD311')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD311', 1, N'Bảo dưỡng phanh và thay dầu phanh', N'Gói', 1, 1600000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD311')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD311', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng phanh và thay dầu phanh)', N'Bộ', 1, 3900000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN312')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN312', 'NV01', 'KH106', 'VINDEMO106', '2026-04-28T08:00:00', N'Bảo dưỡng gầm và kiểm tra thước lái', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC312')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC312', 'TN312', 'NV02', 'KH106', 'VINDEMO106', '2026-04-28T09:00:00', '2026-04-28', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD312')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD312', 'KH106', 'NV05', '2026-04-28', 'TIEN_MAT', 2200000, 5100000, 0, 7300000, N'Bảo dưỡng gầm và kiểm tra thước lái', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD312')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD312', 1, N'Bảo dưỡng gầm và kiểm tra thước lái', N'Gói', 1, 2200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD312')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD312', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng gầm và kiểm tra thước lái)', N'Bộ', 1, 5100000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN313')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN313', 'NV01', 'KH107', 'VINDEMO107', '2026-05-06T08:00:00', N'Bảo dưỡng động cơ và vệ sinh kim phun', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC313')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC313', 'TN313', 'NV101', 'KH107', 'VINDEMO107', '2026-05-06T09:00:00', '2026-05-06', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD313')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD313', 'KH107', 'NV05', '2026-05-06', 'CHUYEN_KHOAN', 2500000, 6200000, 0, 8700000, N'Bảo dưỡng động cơ và vệ sinh kim phun', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD313')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD313', 1, N'Bảo dưỡng động cơ và vệ sinh kim phun', N'Gói', 1, 2500000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD313')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD313', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng động cơ và vệ sinh kim phun)', N'Bộ', 1, 6200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN314')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN314', 'NV01', 'KH108', 'VINDEMO108', '2026-05-12T08:00:00', N'Bảo dưỡng định kỳ 20.000km', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC314')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC314', 'TN314', 'NV102', 'KH108', 'VINDEMO108', '2026-05-12T09:00:00', '2026-05-12', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD314')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD314', 'KH108', 'NV05', '2026-05-12', 'TIEN_MAT', 1800000, 4200000, 0, 6000000, N'Bảo dưỡng định kỳ 20.000km', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD314')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD314', 1, N'Bảo dưỡng định kỳ 20.000km', N'Gói', 1, 1800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD314')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD314', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ 20.000km)', N'Bộ', 1, 4200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN315')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN315', 'NV01', 'KH109', 'VINDEMO109', '2026-05-19T08:00:00', N'Thay dầu máy và lọc gió', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC315')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC315', 'TN315', 'NV02', 'KH109', 'VINDEMO109', '2026-05-19T09:00:00', '2026-05-19', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD315')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD315', 'KH109', 'NV05', '2026-05-19', 'THE_POS', 1400000, 3600000, 0, 5000000, N'Thay dầu máy và lọc gió', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD315')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD315', 1, N'Thay dầu máy và lọc gió', N'Gói', 1, 1400000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD315')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD315', 'PT101', N'Vật tư linh kiện thay thế (Thay dầu máy và lọc gió)', N'Bộ', 1, 3600000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN316')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN316', 'NV01', 'KH110', 'VINDEMO110', '2026-05-27T08:00:00', N'Thay má phanh trước sau và láng đĩa phanh', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC316')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC316', 'TN316', 'NV101', 'KH110', 'VINDEMO110', '2026-05-27T09:00:00', '2026-05-27', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD316')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD316', 'KH110', 'NV05', '2026-05-27', 'CHUYEN_KHOAN', 2600000, 6200000, 0, 8800000, N'Thay má phanh trước sau và láng đĩa phanh', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD316')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD316', 1, N'Thay má phanh trước sau và láng đĩa phanh', N'Gói', 1, 2600000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD316')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD316', 'PT101', N'Vật tư linh kiện thay thế (Thay má phanh trước sau và láng đĩa phanh)', N'Bộ', 1, 6200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN317')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN317', 'NV01', 'KH111', 'VINDEMO111', '2026-06-05T08:00:00', N'Bảo dưỡng định kỳ và thay bugi', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC317')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC317', 'TN317', 'NV102', 'KH111', 'VINDEMO111', '2026-06-05T09:00:00', '2026-06-05', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD317')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD317', 'KH111', 'NV05', '2026-06-05', 'TIEN_MAT', 2100000, 4800000, 0, 6900000, N'Bảo dưỡng định kỳ và thay bugi', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD317')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD317', 1, N'Bảo dưỡng định kỳ và thay bugi', N'Gói', 1, 2100000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD317')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD317', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ và thay bugi)', N'Bộ', 1, 4800000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN318')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN318', 'NV01', 'KH112', 'VINDEMO112', '2026-06-11T08:00:00', N'Bảo dưỡng hệ thống làm mát động cơ', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC318')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC318', 'TN318', 'NV02', 'KH112', 'VINDEMO112', '2026-06-11T09:00:00', '2026-06-11', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD318')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD318', 'KH112', 'NV05', '2026-06-11', 'CHUYEN_KHOAN', 2700000, 6500000, 0, 9200000, N'Bảo dưỡng hệ thống làm mát động cơ', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD318')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD318', 1, N'Bảo dưỡng hệ thống làm mát động cơ', N'Gói', 1, 2700000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD318')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD318', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng hệ thống làm mát động cơ)', N'Bộ', 1, 6500000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN319')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN319', 'NV01', 'KH113', 'VINDEMO113', '2026-06-17T08:00:00', N'Bảo dưỡng hệ thống nạp khí và cảm biến', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC319')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC319', 'TN319', 'NV101', 'KH113', 'VINDEMO113', '2026-06-17T09:00:00', '2026-06-17', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD319')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD319', 'KH113', 'NV05', '2026-06-17', 'THE_POS', 1500000, 3800000, 0, 5300000, N'Bảo dưỡng hệ thống nạp khí và cảm biến', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD319')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD319', 1, N'Bảo dưỡng hệ thống nạp khí và cảm biến', N'Gói', 1, 1500000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD319')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD319', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng hệ thống nạp khí và cảm biến)', N'Bộ', 1, 3800000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN320')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN320', 'NV01', 'KH114', 'VINDEMO114', '2026-06-22T08:00:00', N'Bảo dưỡng cấp 3 định kỳ', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC320')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC320', 'TN320', 'NV102', 'KH114', 'VINDEMO114', '2026-06-22T09:00:00', '2026-06-22', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD320')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD320', 'KH114', 'NV05', '2026-06-22', 'TIEN_MAT', 1800000, 4200000, 0, 6000000, N'Bảo dưỡng cấp 3 định kỳ', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD320')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD320', 1, N'Bảo dưỡng cấp 3 định kỳ', N'Gói', 1, 1800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD320')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD320', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng cấp 3 định kỳ)', N'Bộ', 1, 4200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN321')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN321', 'NV01', 'KH115', 'VINDEMO115', '2026-06-28T08:00:00', N'Thay lọc nhiên liệu và vệ sinh họng hút', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC321')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC321', 'TN321', 'NV02', 'KH115', 'VINDEMO115', '2026-06-28T09:00:00', '2026-06-28', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD321')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD321', 'KH115', 'NV05', '2026-06-28', 'CHUYEN_KHOAN', 1400000, 3600000, 0, 5000000, N'Thay lọc nhiên liệu và vệ sinh họng hút', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD321')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD321', 1, N'Thay lọc nhiên liệu và vệ sinh họng hút', N'Gói', 1, 1400000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD321')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD321', 'PT101', N'Vật tư linh kiện thay thế (Thay lọc nhiên liệu và vệ sinh họng hút)', N'Bộ', 1, 3600000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN322')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN322', 'NV01', 'KH116', 'VINDEMO116', '2026-07-06T08:00:00', N'Bảo dưỡng điều hòa ô tô mùa nóng', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC322')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC322', 'TN322', 'NV101', 'KH116', 'VINDEMO116', '2026-07-06T09:00:00', '2026-07-06', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD322')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD322', 'KH116', 'NV05', '2026-07-06', 'TIEN_MAT', 2000000, 4900000, 0, 6900000, N'Bảo dưỡng điều hòa ô tô mùa nóng', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD322')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD322', 1, N'Bảo dưỡng điều hòa ô tô mùa nóng', N'Gói', 1, 2000000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD322')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD322', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng điều hòa ô tô mùa nóng)', N'Bộ', 1, 4900000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN323')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN323', 'NV01', 'KH117', 'VINDEMO117', '2026-07-14T08:00:00', N'Bảo dưỡng phanh và cân mâm bấm chì', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC323')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC323', 'TN323', 'NV102', 'KH117', 'VINDEMO117', '2026-07-14T09:00:00', '2026-07-14', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD323')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD323', 'KH117', 'NV05', '2026-07-14', 'CHUYEN_KHOAN', 1900000, 4800000, 0, 6700000, N'Bảo dưỡng phanh và cân mâm bấm chì', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD323')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD323', 1, N'Bảo dưỡng phanh và cân mâm bấm chì', N'Gói', 1, 1900000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD323')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD323', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng phanh và cân mâm bấm chì)', N'Bộ', 1, 4800000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN324')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN324', 'NV01', 'KH118', 'VINDEMO118', '2026-07-20T08:00:00', N'Bảo dưỡng gầm và thay rotuyn cân bằng', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC324')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC324', 'TN324', 'NV02', 'KH118', 'VINDEMO118', '2026-07-20T09:00:00', '2026-07-20', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD324')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD324', 'KH118', 'NV05', '2026-07-20', 'THE_POS', 2100000, 5300000, 0, 7400000, N'Bảo dưỡng gầm và thay rotuyn cân bằng', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD324')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD324', 1, N'Bảo dưỡng gầm và thay rotuyn cân bằng', N'Gói', 1, 2100000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD324')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD324', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng gầm và thay rotuyn cân bằng)', N'Bộ', 1, 5300000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN325')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN325', 'NV01', 'KH119', 'VINDEMO119', '2026-07-28T08:00:00', N'Bảo dưỡng định kỳ động cơ', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC325')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC325', 'TN325', 'NV101', 'KH119', 'VINDEMO119', '2026-07-28T09:00:00', '2026-07-28', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD325')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD325', 'KH119', 'NV05', '2026-07-28', 'TIEN_MAT', 1800000, 4800000, 0, 6600000, N'Bảo dưỡng định kỳ động cơ', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD325')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD325', 1, N'Bảo dưỡng định kỳ động cơ', N'Gói', 1, 1800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD325')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD325', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ động cơ)', N'Bộ', 1, 4800000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN326')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN326', 'NV01', 'KH120', 'VINDEMO120', '2026-08-04T08:00:00', N'Kiểm tra hệ thống treo và thước lái', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC326')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC326', 'TN326', 'NV102', 'KH120', 'VINDEMO120', '2026-08-04T09:00:00', '2026-08-04', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD326')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD326', 'KH120', 'NV05', '2026-08-04', 'CHUYEN_KHOAN', 2200000, 5100000, 0, 7300000, N'Kiểm tra hệ thống treo và thước lái', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD326')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD326', 1, N'Kiểm tra hệ thống treo và thước lái', N'Gói', 1, 2200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD326')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD326', 'PT101', N'Vật tư linh kiện thay thế (Kiểm tra hệ thống treo và thước lái)', N'Bộ', 1, 5100000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN327')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN327', 'NV01', 'KH101', 'VINDEMO101', '2026-08-11T08:00:00', N'Thay dầu tổng hợp và kiểm tra xe định kỳ', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC327')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC327', 'TN327', 'NV02', 'KH101', 'VINDEMO101', '2026-08-11T09:00:00', '2026-08-11', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD327')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD327', 'KH101', 'NV05', '2026-08-11', 'TIEN_MAT', 1800000, 4300000, 0, 6100000, N'Thay dầu tổng hợp và kiểm tra xe định kỳ', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD327')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD327', 1, N'Thay dầu tổng hợp và kiểm tra xe định kỳ', N'Gói', 1, 1800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD327')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD327', 'PT101', N'Vật tư linh kiện thay thế (Thay dầu tổng hợp và kiểm tra xe định kỳ)', N'Bộ', 1, 4300000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN328')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN328', 'NV01', 'KH102', 'VINDEMO102', '2026-08-17T08:00:00', N'Bảo dưỡng phanh ABS và thay má phanh', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC328')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC328', 'TN328', 'NV101', 'KH102', 'VINDEMO102', '2026-08-17T09:00:00', '2026-08-17', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD328')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD328', 'KH102', 'NV05', '2026-08-17', 'THE_POS', 1900000, 4500000, 0, 6400000, N'Bảo dưỡng phanh ABS và thay má phanh', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD328')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD328', 1, N'Bảo dưỡng phanh ABS và thay má phanh', N'Gói', 1, 1900000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD328')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD328', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng phanh ABS và thay má phanh)', N'Bộ', 1, 4500000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN329')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN329', 'NV01', 'KH103', 'VINDEMO103', '2026-08-23T08:00:00', N'Bảo dưỡng định kỳ 50.000km', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC329')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC329', 'TN329', 'NV102', 'KH103', 'VINDEMO103', '2026-08-23T09:00:00', '2026-08-23', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD329')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD329', 'KH103', 'NV05', '2026-08-23', 'CHUYEN_KHOAN', 2100000, 4900000, 0, 7000000, N'Bảo dưỡng định kỳ 50.000km', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD329')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD329', 1, N'Bảo dưỡng định kỳ 50.000km', N'Gói', 1, 2100000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD329')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD329', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ 50.000km)', N'Bộ', 1, 4900000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN330')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN330', 'NV01', 'KH001', 'VIN001', '2026-08-29T08:00:00', N'Vệ sinh buồng đốt và thay nước làm mát', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC330')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC330', 'TN330', 'NV02', 'KH001', 'VIN001', '2026-08-29T09:00:00', '2026-08-29', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD330')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD330', 'KH001', 'NV05', '2026-08-29', 'TIEN_MAT', 1400000, 3300000, 0, 4700000, N'Vệ sinh buồng đốt và thay nước làm mát', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD330')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD330', 1, N'Vệ sinh buồng đốt và thay nước làm mát', N'Gói', 1, 1400000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD330')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD330', 'PT101', N'Vật tư linh kiện thay thế (Vệ sinh buồng đốt và thay nước làm mát)', N'Bộ', 1, 3300000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN331')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN331', 'NV01', 'KH002', 'VIN002', '2026-09-03T08:00:00', N'Bảo dưỡng hộp số và gầm xe', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC331')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC331', 'TN331', 'NV101', 'KH002', 'VIN002', '2026-09-03T09:00:00', '2026-09-03', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD331')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD331', 'KH002', 'NV05', '2026-09-03', 'CHUYEN_KHOAN', 2400000, 5600000, 0, 8000000, N'Bảo dưỡng hộp số và gầm xe', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD331')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD331', 1, N'Bảo dưỡng hộp số và gầm xe', N'Gói', 1, 2400000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD331')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD331', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng hộp số và gầm xe)', N'Bộ', 1, 5600000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN332')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN332', 'NV01', 'KH104', 'VINDEMO104', '2026-09-10T08:00:00', N'Bảo dưỡng định kỳ cấp 2', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC332')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC332', 'TN332', 'NV102', 'KH104', 'VINDEMO104', '2026-09-10T09:00:00', '2026-09-10', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD332')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD332', 'KH104', 'NV05', '2026-09-10', 'TIEN_MAT', 2100000, 4900000, 0, 7000000, N'Bảo dưỡng định kỳ cấp 2', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD332')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD332', 1, N'Bảo dưỡng định kỳ cấp 2', N'Gói', 1, 2100000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD332')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD332', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ cấp 2)', N'Bộ', 1, 4900000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN333')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN333', 'NV01', 'KH105', 'VINDEMO105', '2026-09-16T08:00:00', N'Bảo dưỡng điều hòa và khử mùi ozon', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC333')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC333', 'TN333', 'NV02', 'KH105', 'VINDEMO105', '2026-09-16T09:00:00', '2026-09-16', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD333')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD333', 'KH105', 'NV05', '2026-09-16', 'THE_POS', 1800000, 4200000, 0, 6000000, N'Bảo dưỡng điều hòa và khử mùi ozon', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD333')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD333', 1, N'Bảo dưỡng điều hòa và khử mùi ozon', N'Gói', 1, 1800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD333')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD333', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng điều hòa và khử mùi ozon)', N'Bộ', 1, 4200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN334')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN334', 'NV01', 'KH106', 'VINDEMO106', '2026-09-22T08:00:00', N'Thay ắc quy và kiểm tra củ đề', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC334')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC334', 'TN334', 'NV101', 'KH106', 'VINDEMO106', '2026-09-22T09:00:00', '2026-09-22', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD334')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD334', 'KH106', 'NV05', '2026-09-22', 'TIEN_MAT', 2000000, 4700000, 0, 6700000, N'Thay ắc quy và kiểm tra củ đề', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD334')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD334', 1, N'Thay ắc quy và kiểm tra củ đề', N'Gói', 1, 2000000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD334')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD334', 'PT101', N'Vật tư linh kiện thay thế (Thay ắc quy và kiểm tra củ đề)', N'Bộ', 1, 4700000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN335')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN335', 'NV01', 'KH107', 'VINDEMO107', '2026-09-28T08:00:00', N'Bảo dưỡng phanh và gầm', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC335')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC335', 'TN335', 'NV102', 'KH107', 'VINDEMO107', '2026-09-28T09:00:00', '2026-09-28', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD335')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD335', 'KH107', 'NV05', '2026-09-28', 'CHUYEN_KHOAN', 1900000, 4600000, 0, 6500000, N'Bảo dưỡng phanh và gầm', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD335')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD335', 1, N'Bảo dưỡng phanh và gầm', N'Gói', 1, 1900000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD335')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD335', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng phanh và gầm)', N'Bộ', 1, 4600000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN336')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN336', 'NV01', 'KH112', 'VINDEMO112', '2026-10-05T08:00:00', N'Bảo dưỡng định kỳ 60.000km', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC336')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC336', 'TN336', 'NV02', 'KH112', 'VINDEMO112', '2026-10-05T09:00:00', '2026-10-05', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD336')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD336', 'KH112', 'NV05', '2026-10-05', 'THE_POS', 3000000, 7500000, 0, 10500000, N'Bảo dưỡng định kỳ 60.000km', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD336')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD336', 1, N'Bảo dưỡng định kỳ 60.000km', N'Gói', 1, 3000000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD336')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD336', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ 60.000km)', N'Bộ', 1, 7500000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN337')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN337', 'NV01', 'KH120', 'VINDEMO120', '2026-10-10T08:00:00', N'Thay lốp và cân mâm', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC337')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC337', 'TN337', 'NV101', 'KH120', 'VINDEMO120', '2026-10-10T09:00:00', '2026-10-10', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD337')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD337', 'KH120', 'NV05', '2026-10-10', 'CHUYEN_KHOAN', 2500000, 6000000, 0, 8500000, N'Thay lốp và cân mâm', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD337')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD337', 1, N'Thay lốp và cân mâm', N'Gói', 1, 2500000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD337')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD337', 'PT101', N'Vật tư linh kiện thay thế (Thay lốp và cân mâm)', N'Bộ', 1, 6000000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN338')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN338', 'NV01', 'KH101', 'VINDEMO101', '2026-11-02T08:00:00', N'Bảo dưỡng đầu đông', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC338')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC338', 'TN338', 'NV102', 'KH101', 'VINDEMO101', '2026-11-02T09:00:00', '2026-11-02', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD338')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD338', 'KH101', 'NV05', '2026-11-02', 'TIEN_MAT', 2200000, 5400000, 0, 7600000, N'Bảo dưỡng đầu đông', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD338')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD338', 1, N'Bảo dưỡng đầu đông', N'Gói', 1, 2200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD338')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD338', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng đầu đông)', N'Bộ', 1, 5400000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN339')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN339', 'NV01', 'KH102', 'VINDEMO102', '2026-11-05T08:00:00', N'Bảo dưỡng định kỳ và thay lọc nhớt', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC339')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC339', 'TN339', 'NV02', 'KH102', 'VINDEMO102', '2026-11-05T09:00:00', '2026-11-05', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD339')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD339', 'KH102', 'NV05', '2026-11-05', 'CHUYEN_KHOAN', 1800000, 4200000, 0, 6000000, N'Bảo dưỡng định kỳ và thay lọc nhớt', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD339')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD339', 1, N'Bảo dưỡng định kỳ và thay lọc nhớt', N'Gói', 1, 1800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD339')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD339', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ và thay lọc nhớt)', N'Bộ', 1, 4200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2501')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2501', 'NV01', 'KH001', 'VIN001', '2025-01-15T08:00:00', N'Bảo dưỡng định kỳ 2025', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2501')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2501', 'TN2501', 'NV02', 'KH001', 'VIN001', '2025-01-15T09:00:00', '2025-01-15', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2501')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2501', 'KH001', 'NV05', '2025-01-15', 'TIEN_MAT', 4500000, 12500000, 0, 17000000, N'Bảo dưỡng định kỳ 2025', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2501')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2501', 1, N'Bảo dưỡng định kỳ 2025', N'Gói', 1, 4500000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2501')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2501', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng định kỳ 2025)', N'Bộ', 1, 12500000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2502')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2502', 'NV01', 'KH002', 'VIN002', '2025-02-18T08:00:00', N'Bảo dưỡng hệ thống phanh', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2502')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2502', 'TN2502', 'NV02', 'KH002', 'VIN002', '2025-02-18T09:00:00', '2025-02-18', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2502')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2502', 'KH002', 'NV05', '2025-02-18', 'CHUYEN_KHOAN', 3800000, 11200000, 0, 15000000, N'Bảo dưỡng hệ thống phanh', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2502')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2502', 1, N'Bảo dưỡng hệ thống phanh', N'Gói', 1, 3800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2502')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2502', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng hệ thống phanh)', N'Bộ', 1, 11200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2503')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2503', 'NV01', 'KH101', 'VINDEMO101', '2025-03-20T08:00:00', N'Thay giảm xóc và lốp', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2503')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2503', 'TN2503', 'NV101', 'KH101', 'VINDEMO101', '2025-03-20T09:00:00', '2025-03-20', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2503')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2503', 'KH101', 'NV05', '2025-03-20', 'TIEN_MAT', 5200000, 14800000, 0, 20000000, N'Thay giảm xóc và lốp', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2503')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2503', 1, N'Thay giảm xóc và lốp', N'Gói', 1, 5200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2503')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2503', 'PT101', N'Vật tư linh kiện thay thế (Thay giảm xóc và lốp)', N'Bộ', 1, 14800000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2504')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2504', 'NV01', 'KH102', 'VINDEMO102', '2025-04-12T08:00:00', N'Bảo dưỡng tổng thể đón hè 2025', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2504')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2504', 'TN2504', 'NV102', 'KH102', 'VINDEMO102', '2025-04-12T09:00:00', '2025-04-12', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2504')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2504', 'KH102', 'NV05', '2025-04-12', 'THE_POS', 6100000, 16200000, 0, 22300000, N'Bảo dưỡng tổng thể đón hè 2025', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2504')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2504', 1, N'Bảo dưỡng tổng thể đón hè 2025', N'Gói', 1, 6100000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2504')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2504', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng tổng thể đón hè 2025)', N'Bộ', 1, 16200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2505')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2505', 'NV01', 'KH103', 'VINDEMO103', '2025-05-15T08:00:00', N'Đại tu điều hòa và làm mát', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2505')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2505', 'TN2505', 'NV02', 'KH103', 'VINDEMO103', '2025-05-15T09:00:00', '2025-05-15', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2505')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2505', 'KH103', 'NV05', '2025-05-15', 'CHUYEN_KHOAN', 7200000, 18500000, 0, 25700000, N'Đại tu điều hòa và làm mát', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2505')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2505', 1, N'Đại tu điều hòa và làm mát', N'Gói', 1, 7200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2505')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2505', 'PT101', N'Vật tư linh kiện thay thế (Đại tu điều hòa và làm mát)', N'Bộ', 1, 18500000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2506')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2506', 'NV01', 'KH104', 'VINDEMO104', '2025-06-18T08:00:00', N'Bảo dưỡng gầm xe', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2506')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2506', 'TN2506', 'NV101', 'KH104', 'VINDEMO104', '2025-06-18T09:00:00', '2025-06-18', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2506')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2506', 'KH104', 'NV05', '2025-06-18', 'TIEN_MAT', 7900000, 20100000, 0, 28000000, N'Bảo dưỡng gầm xe', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2506')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2506', 1, N'Bảo dưỡng gầm xe', N'Gói', 1, 7900000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2506')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2506', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng gầm xe)', N'Bộ', 1, 20100000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2507')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2507', 'NV01', 'KH105', 'VINDEMO105', '2025-07-22T08:00:00', N'Bảo dưỡng cấp 3', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2507')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2507', 'TN2507', 'NV102', 'KH105', 'VINDEMO105', '2025-07-22T09:00:00', '2025-07-22', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2507')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2507', 'KH105', 'NV05', '2025-07-22', 'THE_POS', 6800000, 17200000, 0, 24000000, N'Bảo dưỡng cấp 3', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2507')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2507', 1, N'Bảo dưỡng cấp 3', N'Gói', 1, 6800000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2507')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2507', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng cấp 3)', N'Bộ', 1, 17200000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2508')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2508', 'NV01', 'KH106', 'VINDEMO106', '2025-08-19T08:00:00', N'Thay ắc quy và bảo dưỡng điện', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2508')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2508', 'TN2508', 'NV02', 'KH106', 'VINDEMO106', '2025-08-19T09:00:00', '2025-08-19', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2508')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2508', 'KH106', 'NV05', '2025-08-19', 'CHUYEN_KHOAN', 7500000, 19300000, 0, 26800000, N'Thay ắc quy và bảo dưỡng điện', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2508')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2508', 1, N'Thay ắc quy và bảo dưỡng điện', N'Gói', 1, 7500000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2508')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2508', 'PT101', N'Vật tư linh kiện thay thế (Thay ắc quy và bảo dưỡng điện)', N'Bộ', 1, 19300000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2509')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2509', 'NV01', 'KH107', 'VINDEMO107', '2025-09-14T08:00:00', N'Bảo dưỡng lớn định kỳ', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2509')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2509', 'TN2509', 'NV101', 'KH107', 'VINDEMO107', '2025-09-14T09:00:00', '2025-09-14', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2509')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2509', 'KH107', 'NV05', '2025-09-14', 'TIEN_MAT', 8200000, 21500000, 0, 29700000, N'Bảo dưỡng lớn định kỳ', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2509')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2509', 1, N'Bảo dưỡng lớn định kỳ', N'Gói', 1, 8200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2509')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2509', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng lớn định kỳ)', N'Bộ', 1, 21500000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2510')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2510', 'NV01', 'KH108', 'VINDEMO108', '2025-10-16T08:00:00', N'Bảo dưỡng phanh và lái', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2510')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2510', 'TN2510', 'NV102', 'KH108', 'VINDEMO108', '2025-10-16T09:00:00', '2025-10-16', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2510')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2510', 'KH108', 'NV05', '2025-10-16', 'CHUYEN_KHOAN', 8900000, 23000000, 0, 31900000, N'Bảo dưỡng phanh và lái', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2510')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2510', 1, N'Bảo dưỡng phanh và lái', N'Gói', 1, 8900000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2510')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2510', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng phanh và lái)', N'Bộ', 1, 23000000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2511')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2511', 'NV01', 'KH109', 'VINDEMO109', '2025-11-20T08:00:00', N'Đại tu động cơ', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2511')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2511', 'TN2511', 'NV02', 'KH109', 'VINDEMO109', '2025-11-20T09:00:00', '2025-11-20', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2511')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2511', 'KH109', 'NV05', '2025-11-20', 'THE_POS', 9500000, 24500000, 0, 34000000, N'Đại tu động cơ', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2511')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2511', 1, N'Đại tu động cơ', N'Gói', 1, 9500000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2511')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2511', 'PT101', N'Vật tư linh kiện thay thế (Đại tu động cơ)', N'Bộ', 1, 24500000);

IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN = 'TN2512')
  INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES ('TN2512', 'NV01', 'KH110', 'VINDEMO110', '2025-12-22T08:00:00', N'Bảo dưỡng đón Tết 2025', N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC = 'SC2512')
  INSERT INTO PhieuSuaChua (maPSC, maPTN, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES ('SC2512', 'TN2512', 'NV101', 'KH110', 'VINDEMO110', '2025-12-22T09:00:00', '2025-12-22', 'HOAN_THANH');
IF NOT EXISTS (SELECT 1 FROM HoaDon WHERE maHD = 'HD2512')
  INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES ('HD2512', 'KH110', 'NV05', '2025-12-22', 'CHUYEN_KHOAN', 10200000, 26800000, 0, 37000000, N'Bảo dưỡng đón Tết 2025', 'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD = 'HD2512')
  INSERT INTO ChiTietDichVuHoaDon (maHD, stt, tenDichVu, donViTinh, soLuong, donGia) VALUES ('HD2512', 1, N'Bảo dưỡng đón Tết 2025', N'Gói', 1, 10200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD = 'HD2512')
  INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, donViTinh, soLuong, donGia) VALUES ('HD2512', 'PT101', N'Vật tư linh kiện thay thế (Bảo dưỡng đón Tết 2025)', N'Bộ', 1, 26800000);


-- Đồng bộ nhóm khách hàng theo quy tắc: >= 2 lần sửa chữa là Thân thiết, < 2 lần là Khách mới
UPDATE dbo.KhachHang SET ghiChu = N'Khách hàng thân thiết' 
WHERE maKH IN (
  SELECT maKH FROM (
    SELECT maKH, COUNT(DISTINCT maPTN) as cnt FROM dbo.PhieuTiepNhan GROUP BY maKH HAVING COUNT(DISTINCT maPTN) >= 2 
    UNION 
    SELECT maKH, COUNT(DISTINCT maPSC) as cnt FROM dbo.PhieuSuaChua GROUP BY maKH HAVING COUNT(DISTINCT maPSC) >= 2 
    UNION 
    SELECT maKH, COUNT(DISTINCT maHD) as cnt FROM dbo.HoaDon WHERE trangThai <> 'DA_HUY' GROUP BY maKH HAVING COUNT(DISTINCT maHD) >= 2 
  ) t 
);

UPDATE dbo.KhachHang SET ghiChu = N'Khách mới' 
WHERE maKH NOT IN (
  SELECT maKH FROM (
    SELECT maKH, COUNT(DISTINCT maPTN) as cnt FROM dbo.PhieuTiepNhan GROUP BY maKH HAVING COUNT(DISTINCT maPTN) >= 2 
    UNION 
    SELECT maKH, COUNT(DISTINCT maPSC) as cnt FROM dbo.PhieuSuaChua GROUP BY maKH HAVING COUNT(DISTINCT maPSC) >= 2 
    UNION 
    SELECT maKH, COUNT(DISTINCT maHD) as cnt FROM dbo.HoaDon WHERE trangThai <> 'DA_HUY' GROUP BY maKH HAVING COUNT(DISTINCT maHD) >= 2 
  ) t 
);
GO


PRINT N'Hoàn tất nạp CSDL QuanLySuaChuaXe thành công!';
GO
