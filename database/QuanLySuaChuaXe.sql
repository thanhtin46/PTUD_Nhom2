-- ===============================================================
-- HỆ THỐNG QUẢN LÝ GARA Ô TÔ THE T4
-- KỊCH BẢN KHỞI TẠO CƠ SỞ DỮ LIỆU: QuanLySuaChuaXe
-- ===============================================================

CREATE DATABASE QuanLySuaChuaXe;
GO

USE QuanLySuaChuaXe;
GO

-- 1. BẢNG NHÂN VIÊN
IF OBJECT_ID('NhanVien', 'U') IS NOT NULL DROP TABLE NhanVien;
CREATE TABLE NhanVien (
    maNV NVARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50),
    soDT VARCHAR(15),
    email VARCHAR(100),
    bacNghe NVARCHAR(50),
    boPhan NVARCHAR(50),
    ngayVaoLam VARCHAR(20),
    trangThai NVARCHAR(30) DEFAULT N'Đang làm việc'
);
GO

-- 2. BẢNG TÀI KHOẢN HỆ THỐNG
IF OBJECT_ID('TaiKhoan', 'U') IS NOT NULL DROP TABLE TaiKhoan;
CREATE TABLE TaiKhoan (
    taiKhoan VARCHAR(50) PRIMARY KEY,
    matKhau VARCHAR(100) NOT NULL DEFAULT '123',
    quyen NVARCHAR(50),
    maNV NVARCHAR(20) REFERENCES NhanVien(maNV) ON DELETE SET NULL,
    trangThai VARCHAR(20) DEFAULT 'HOAT_DONG'
);
GO

-- 3. BẢNG KHÁCH HÀNG
IF OBJECT_ID('KhachHang', 'U') IS NOT NULL DROP TABLE KhachHang;
CREATE TABLE KhachHang (
    maKH NVARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    soDT VARCHAR(15) NOT NULL,
    chungMinh VARCHAR(20),
    diaChi NVARCHAR(255),
    email VARCHAR(100),
    ghiChu NVARCHAR(255),
    ngayDangKy VARCHAR(20)
);
GO

-- 4. BẢNG XE / PHƯƠNG TIỆN
IF OBJECT_ID('Xe', 'U') IS NOT NULL DROP TABLE Xe;
CREATE TABLE Xe (
    bienSo NVARCHAR(20) PRIMARY KEY,
    hangXe NVARCHAR(50),
    dongXe NVARCHAR(50),
    soKhung VARCHAR(50),
    mauXe NVARCHAR(30),
    namSX INT,
    soKm INT,
    maKH NVARCHAR(20) REFERENCES KhachHang(maKH) ON DELETE SET NULL,
    ngayTiepNhanGanNhat VARCHAR(20)
);
GO

-- 5. BẢNG PHỤ TÙNG & VẬT TƯ
IF OBJECT_ID('PhuTung', 'U') IS NOT NULL DROP TABLE PhuTung;
CREATE TABLE PhuTung (
    maPT NVARCHAR(20) PRIMARY KEY,
    tenPT NVARCHAR(150) NOT NULL,
    loaiPT NVARCHAR(50),
    donGia DECIMAL(18, 2) DEFAULT 0,
    soLuongTon INT DEFAULT 0,
    donViTinh NVARCHAR(20),
    viTriKho NVARCHAR(50),
    trangThai NVARCHAR(30) DEFAULT N'Sẵn sàng'
);
GO

-- 6. BẢNG PHIẾU TIẾP NHẬN XE
IF OBJECT_ID('PhieuTiepNhan', 'U') IS NOT NULL DROP TABLE PhieuTiepNhan;
CREATE TABLE PhieuTiepNhan (
    maPTN NVARCHAR(20) PRIMARY KEY,
    bienSo NVARCHAR(20) REFERENCES Xe(bienSo),
    maNV NVARCHAR(20) REFERENCES NhanVien(maNV),
    ngayTiepNhan VARCHAR(20),
    tinhTrangXe NVARCHAR(255),
    yeuCauKhach NVARCHAR(255),
    trangThai NVARCHAR(30) DEFAULT N'Chờ sửa chữa'
);
GO

-- 7. BẢNG PHIẾU SỬA CHỮA
IF OBJECT_ID('PhieuSuaChua', 'U') IS NOT NULL DROP TABLE PhieuSuaChua;
CREATE TABLE PhieuSuaChua (
    maPSC NVARCHAR(20) PRIMARY KEY,
    maPTN NVARCHAR(20) REFERENCES PhieuTiepNhan(maPTN),
    maKTV NVARCHAR(20) REFERENCES NhanVien(maNV),
    ngayLap VARCHAR(20),
    duKienHoanThanh VARCHAR(20),
    trangThai NVARCHAR(30) DEFAULT N'Đang xử lý',
    khoangSuaChua NVARCHAR(50),
    mucUuTien NVARCHAR(30) DEFAULT N'Tiêu chuẩn',
    ghiChu NVARCHAR(500)
);
GO

-- 8. BẢNG HÓA ĐƠN THANH TOÁN
IF OBJECT_ID('HoaDon', 'U') IS NOT NULL DROP TABLE HoaDon;
CREATE TABLE HoaDon (
    maHD NVARCHAR(20) PRIMARY KEY,
    maPSC NVARCHAR(20),
    maKH NVARCHAR(20) REFERENCES KhachHang(maKH),
    ngayLap VARCHAR(20),
    tienCong DECIMAL(18, 2) DEFAULT 0,
    tienPhuTung DECIMAL(18, 2) DEFAULT 0,
    vat DECIMAL(18, 2) DEFAULT 0,
    tongTien DECIMAL(18, 2) DEFAULT 0,
    phuongThucTT NVARCHAR(50) DEFAULT N'Tiền mặt',
    trangThai NVARCHAR(30) DEFAULT N'Đã thanh toán',
    ghiChu NVARCHAR(255)
);
GO

-- ===============================================================
-- DỮ LIỆU MẪU ĐẦU TIÊN (SEED DATA)
-- ===============================================================

-- Thêm Nhân viên
INSERT INTO NhanVien (maNV, hoTen, chucVu, soDT, email, bacNghe, boPhan, ngayVaoLam, trangThai) VALUES
('NV01', N'Nguyễn Văn An', N'Cố vấn trưởng / Điều hành', '0903118892', 'an.nguyen@garaauto.vn', N'Kỹ sư Ô tô ĐHBK', N'Ban Cố Vấn Dịch Vụ', '12/03/2018', N'Đang làm việc'),
('NV02', N'Đỗ Hoàng Long', N'Cố vấn dịch vụ 2', '0934881094', 'long.do@garaauto.vn', N'Cử nhân Quản trị', N'Tổ Tiếp Nhận Xe', '10/04/2021', N'Đang làm việc'),
('NV03', N'Lê Văn Cường', N'Nhân viên thủ kho', '0901000003', 'cuong.le@garaauto.vn', N'Trung cấp Vật tư', N'Kho Phụ Tùng & Vật Tư', '18/08/2020', N'Đang làm việc'),
('NV04', N'Trần Văn Quản Lý', N'Quản lý điều hành', '0901234564', 'quanly@garaauto.vn', N'Thạc sĩ QTKD', N'Ban Giám Đốc Điều Hành', '01/01/2016', N'Đang làm việc'),
('NV05', N'Phạm Thị Dung', N'Thu ngân & Kế toán', '0901000004', 'dung.pham@garaauto.vn', N'Cử nhân Kế toán', N'Phòng Tài Chính - Kế Toán', '05/01/2022', N'Đang làm việc'),
('NV06', N'Võ Văn Nam', N'Trưởng tổ Máy - Gầm', '0982441229', 'nam.vo@garaauto.vn', N'Thợ bậc 5/7', N'Xưởng Sửa Chữa Lớn', '15/06/2019', N'Đang làm việc');
GO

-- Thêm Tài khoản đăng nhập (Mật khẩu mặc định: 123)
INSERT INTO TaiKhoan (taiKhoan, matKhau, quyen, maNV, trangThai) VALUES
('tiepTan', '123', N'Nhân viên tiếp tân', 'NV01', 'HOAT_DONG'),
('suaChua', '123', N'Nhân viên sửa chữa', 'NV06', 'HOAT_DONG'),
('kho', '123', N'Nhân viên kho', 'NV03', 'HOAT_DONG'),
('quanLy', '123', N'Quản lý', 'NV04', 'HOAT_DONG'),
('thuNgan', '123', N'Nhân viên thu ngân', 'NV05', 'HOAT_DONG');
GO

-- Thêm Khách hàng mẫu
INSERT INTO KhachHang (maKH, hoTen, soDT, chungMinh, diaChi, email, ghiChu, ngayDangKy) VALUES
('KH001', N'Trần Minh Tuấn', '0918234889', '079090001234', N'123 Nguyễn Huệ, Q.1, TP.HCM', 'tuan.tran@gmail.com', N'Khách VIP', '15/01/2024'),
('KH002', N'Lê Thị Hương', '0903888777', '079092005678', N'45 Lê Lợi, Q.1, TP.HCM', 'huong.le@gmail.com', N'Khách quen', '20/02/2024');
GO

-- Thêm Phương tiện mẫu
INSERT INTO Xe (bienSo, hangXe, dongXe, soKhung, mauXe, namSX, soKm, maKH, ngayTiepNhanGanNhat) VALUES
('60A-12345', 'Toyota', 'Camry 2.5Q', 'VNTOY1234567890', N'Đen', 2022, 45230, 'KH001', '19/10/2026'),
('51H-98765', 'Mazda', 'Mazda CX-5', 'VNMAZ9876543210', N'Đỏ Soul', 2023, 18500, 'KH002', '18/10/2026');
GO

-- Thêm Phụ tùng mẫu
INSERT INTO PhuTung (maPT, tenPT, loaiPT, donGia, soLuongTon, donViTinh, viTriKho, trangThai) VALUES
('PT-OIL01', N'Dầu nhớt Castrol Magnatec 5W-30', N'Dầu nhớt', 820000, 45, N'Can 4L', 'KHO-A1', N'Sẵn sàng'),
('PT-SPK04', N'Bugi Denso Iridium Power IK20', N'Đánh lửa', 280000, 120, N'Cái', 'KHO-B2', N'Sẵn sàng'),
('PT-BRK02', N'Má phanh trước Toyota Camry', N'Phanh', 1250000, 18, N'Bộ', 'KHO-C1', N'Sẵn sàng');
GO

PRINT N'✅ Khởi tạo CSDL QuanLySuaChuaXe thành công!';
GO
