
USE master;
GO

IF DB_ID(N'QuanLySuaChuaXe') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLySuaChuaXe
    SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLySuaChuaXe;
END;
GO

CREATE DATABASE QuanLySuaChuaXe;
GO

USE QuanLySuaChuaXe;
GO

/*============================================================
  1. NHAN VIEN
  Luu thong tin nhan vien.
============================================================*/
CREATE TABLE NhanVien (
    maNV VARCHAR(20) NOT NULL PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50) NOT NULL,
    soDT VARCHAR(15) NOT NULL UNIQUE,
    CONSTRAINT CK_NhanVien_SDT CHECK (LEN(soDT) >= 9)
);
GO

/* 2. TAI KHOAN: moi nhan vien co toi da mot tai khoan */
CREATE TABLE TaiKhoan (
    maNV VARCHAR(20) NOT NULL PRIMARY KEY,
    taiKhoan VARCHAR(50) NULL,
    matKhau VARCHAR(255) NOT NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG',
    CONSTRAINT CK_TaiKhoan_TrangThai
        CHECK (trangThai IN ('HOAT_DONG','KHOA')),
    CONSTRAINT FK_TaiKhoan_NhanVien
        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
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
    ngayDangKy VARCHAR(20) NULL,
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
    ngayTiepNhanGanNhat VARCHAR(20) NULL,
    CONSTRAINT CK_PhuongTien_Nam
        CHECK (maSX BETWEEN 1900 AND 2100),
    CONSTRAINT FK_PhuongTien_KhachHang
        FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);
GO

/* 5. NHA CUNG CAP */
CREATE TABLE NhaCungCap (
    maNCC VARCHAR(20) NOT NULL PRIMARY KEY,
    tenNCC NVARCHAR(150) NOT NULL,
    soDT VARCHAR(15) NOT NULL,
    diaChi NVARCHAR(255) NULL,
    email VARCHAR(150) NULL,
    CONSTRAINT CK_NhaCungCap_Email
        CHECK (email IS NULL OR email LIKE '%_@_%._%')
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

/* 7. KHO: moi phu tung co mot dong ton kho */
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
    CONSTRAINT FK_Kho_PhuTung
        FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 8. HOA DON */
CREATE TABLE HoaDon (
    maHD VARCHAR(20) NOT NULL PRIMARY KEY,
    maKH VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayLap DATE NOT NULL,
    hinhThucThanhToan NVARCHAR(50) NOT NULL
        DEFAULT N'TIEN_MAT',
    soPhieuSC VARCHAR(20) NULL,
    tienDichVu DECIMAL(15,2) DEFAULT 0,
    tienPhuTung DECIMAL(15,2) DEFAULT 0,
    giamGia DECIMAL(5,2) DEFAULT 0,
    tongTien DECIMAL(15,2) DEFAULT 0,
    ghiChu NVARCHAR(255) NULL,
    trangThai NVARCHAR(50) DEFAULT N'DA_THANH_TOAN',
    CONSTRAINT FK_HoaDon_KhachHang
        FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HoaDon_NhanVien
        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

/* 9. CHI TIET HOA DON */
CREATE TABLE ChiTietHoaDon (
    maHD VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    tenPhuTung NVARCHAR(150) NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(15,2) NOT NULL,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHD, maPT),
    CONSTRAINT CK_CTHD_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_CTHD_DonGia CHECK (donGia >= 0),
    CONSTRAINT FK_CTHD_HoaDon
        FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_PhuTung
        FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 10. PHIEU BAO HANH */
CREATE TABLE PhieuBaoHanh (
    maPBH VARCHAR(20) NOT NULL PRIMARY KEY,
    ngayBD DATE NOT NULL,
    ngayKT DATE NOT NULL,
    trangThaiBH NVARCHAR(50) NOT NULL DEFAULT N'CON_HAN',
    maKH VARCHAR(20) NOT NULL,
    soVIN VARCHAR(30) NOT NULL,
    maHoaDon VARCHAR(20) NULL,
    CONSTRAINT CK_PBH_Ngay CHECK (ngayKT >= ngayBD),
    CONSTRAINT FK_PBH_KhachHang
        FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PBH_PhuongTien
        FOREIGN KEY (soVIN) REFERENCES PhuongTien(soVIN),
    CONSTRAINT FK_PBH_HoaDon
        FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHD)
);
GO

/* 11. CHI TIET PHIEU BAO HANH */
CREATE TABLE ChiTietPhieuBaoHanh (
    maPBH VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    lyDo NVARCHAR(255) NULL,
    huongXuLy NVARCHAR(255) NULL,
    CONSTRAINT PK_ChiTietPhieuBaoHanh PRIMARY KEY (maPBH, maPT),
    CONSTRAINT FK_CTPBH_PhieuBaoHanh
        FOREIGN KEY (maPBH) REFERENCES PhieuBaoHanh(maPBH),
    CONSTRAINT FK_CTPBH_PhuTung
        FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 12. PHIEU NHAP KHO */
CREATE TABLE PhieuNhapKho (
    maPhieuNhap VARCHAR(20) NOT NULL PRIMARY KEY,
    maNCC VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayNhap DATE NOT NULL,
    tongTien DECIMAL(15,2) NOT NULL DEFAULT 0,
    CONSTRAINT CK_PNK_TongTien CHECK (tongTien >= 0),
    CONSTRAINT FK_PNK_NCC
        FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC),
    CONSTRAINT FK_PNK_NhanVien
        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

/* 13. CHI TIET PHIEU NHAP KHO */
CREATE TABLE ChiTietPhieuNhapKho (
    maPhieuNhap VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    tenPT NVARCHAR(150) NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(15,2) NOT NULL,
    CONSTRAINT PK_ChiTietPhieuNhapKho
        PRIMARY KEY (maPhieuNhap, maPT),
    CONSTRAINT CK_CTPNK_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_CTPNK_DonGia CHECK (donGia >= 0),
    CONSTRAINT FK_CTPNK_PhieuNhap
        FOREIGN KEY (maPhieuNhap)
        REFERENCES PhieuNhapKho(maPhieuNhap),
    CONSTRAINT FK_CTPNK_PhuTung
        FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 14. PHIEU TIEP NHAN */
CREATE TABLE PhieuTiepNhan (
    maPTN VARCHAR(20) NOT NULL PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    soVIN VARCHAR(30) NOT NULL,
    ngayTN DATETIME2 NOT NULL,
    yeuCauSC NVARCHAR(MAX) NOT NULL,
    trangThaiXe NVARCHAR(100) NOT NULL DEFAULT N'DA_TIEP_NHAN',
    CONSTRAINT FK_PTN_NhanVien
        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PTN_KhachHang
        FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PTN_PhuongTien
        FOREIGN KEY (soVIN) REFERENCES PhuongTien(soVIN)
);
GO

/* 15. PHIEU SUA CHUA */
CREATE TABLE PhieuSuaChua (
    maPSC VARCHAR(20) NOT NULL PRIMARY KEY,
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    soVIN VARCHAR(30) NOT NULL,
    ngayBD DATETIME2 NOT NULL,
    ngayHoanThanhDuKien DATE NULL,
    trangThai NVARCHAR(50) NOT NULL DEFAULT N'CHO_SUA',
    CONSTRAINT CK_PSC_Ngay CHECK (
        ngayHoanThanhDuKien IS NULL
        OR ngayHoanThanhDuKien >= CAST(ngayBD AS DATE)
    ),
    CONSTRAINT FK_PSC_NhanVien
        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PSC_KhachHang
        FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_PSC_PhuongTien
        FOREIGN KEY (soVIN) REFERENCES PhuongTien(soVIN)
);
GO

/* 16. CHI TIET SUA CHUA */
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
    CONSTRAINT FK_CTSC_PhieuSuaChua
        FOREIGN KEY (maPSC) REFERENCES PhieuSuaChua(maPSC),
    CONSTRAINT FK_CTSC_PhuTung
        FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/* 17. PHIEU XUAT KHO
   Một phiếu xuất kho liên kết với một phiếu sửa chữa.
   Không lưu maPTN ở đây vì theo mô hình lớp, PXK liên kết với PhieuSuaChua.
*/
CREATE TABLE PhieuXuatKho (
    maPhieuXuat VARCHAR(20) NOT NULL PRIMARY KEY,
    maPSC VARCHAR(20) NOT NULL,
    maNV VARCHAR(20) NOT NULL,
    ngayXuat DATETIME NOT NULL DEFAULT GETDATE(),
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xuất',
    lyDoXuat NVARCHAR(255) NOT NULL
        DEFAULT N'Xuất vật tư thay thế theo phiếu sửa chữa',

    CONSTRAINT UQ_PXK_maPSC UNIQUE (maPSC),
    CONSTRAINT FK_PXK_PhieuSuaChua
        FOREIGN KEY (maPSC) REFERENCES PhieuSuaChua(maPSC),
    CONSTRAINT FK_PXK_NhanVien
        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT CK_PXK_TrangThai CHECK (
        trangThai IN (N'Chờ xuất', N'Đã xuất', N'Đã hủy')
    )
);
GO

/* 18. CHI TIET PHIEU XUAT KHO */
CREATE TABLE ChiTietPhieuXuatKho (
    maPhieuXuat VARCHAR(20) NOT NULL,
    maPT VARCHAR(20) NOT NULL,
    soLuongYeuCau INT NOT NULL,
    soLuongXuat INT NOT NULL DEFAULT 0,
    donGia DECIMAL(15,2) NOT NULL,
    tinhTrangXuat NVARCHAR(50) NOT NULL DEFAULT N'Chờ xuất',

    CONSTRAINT PK_ChiTietPhieuXuatKho
        PRIMARY KEY (maPhieuXuat, maPT),
    CONSTRAINT CK_CTPXK_SoLuongYeuCau CHECK (soLuongYeuCau > 0),
    CONSTRAINT CK_CTPXK_SoLuongXuat CHECK (
        soLuongXuat >= 0 AND soLuongXuat <= soLuongYeuCau
    ),
    CONSTRAINT CK_CTPXK_DonGia CHECK (donGia >= 0),
    CONSTRAINT CK_CTPXK_TinhTrang CHECK (
        tinhTrangXuat IN (N'Chờ xuất', N'Đã xuất', N'Xuất một phần', N'Không đủ tồn kho')
    ),
    CONSTRAINT FK_CTPXK_PhieuXuat
        FOREIGN KEY (maPhieuXuat) REFERENCES PhieuXuatKho(maPhieuXuat),
    CONSTRAINT FK_CTPXK_PhuTung
        FOREIGN KEY (maPT) REFERENCES PhuTung(maPT)
);
GO

/*============================================================
  DU LIEU MAU
============================================================*/

USE QuanLySuaChuaXe;
GO

/* 1. Nhan vien */
INSERT INTO NhanVien (maNV, hoTen, chucVu, soDT) VALUES
('NV01', N'Nguyen Van Tiep Tan', N'Nhân viên tiếp tân', '0901234561'),
('NV02', N'Tran Van Sua Chua', N'Nhân viên sửa chữa', '0901234562'),
('NV03', N'Nguyen Van Kho', N'Nhân viên kho', '0901234563'),
('NV04', N'Quan Ly Tong', N'Quản lý', '0901234564'),
('NV05', N'Tran Thi Thu Ngan', N'Nhân viên thu ngân', '0901234565');
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
('KH001', N'Nguyen Minh Quan', '0912000001', '079000000001', N'123 Nguyễn Văn Cừ, Q.5, TP.HCM', 'quan.nm@gmail.com', N'Khách VIP', '01/01/2026'),
('KH002', N'Tran Ngoc Mai', '0912000002', '079000000002', N'456 Lê Lợi, Q.1, TP.HCM', 'mai.tn@gmail.com', N'Khách thân thiết', '15/01/2026'),
('KH003', N'Do Hoang Nam', '0912000003', '079000000003', N'789 Cách Mạng Tháng 8, Q.10, TP.HCM', 'nam.dh@gmail.com', N'Khách mới', '20/02/2026');
GO

/* 4. Phuong tien */
INSERT INTO PhuongTien (soVIN, bienSoXe, hangXe, dongXe, maSX, maKH) VALUES
('VIN001', '51A-12345', N'Honda', N'City', 2022, 'KH001'),
('VIN002', '59A-67890', N'Toyota', N'Vios', 2021, 'KH002'),
('VIN003', '60A-11223', N'Yamaha', N'Exciter', 2023, 'KH003');
GO

/* 5. Nha cung cap */
INSERT INTO NhaCungCap (maNCC, tenNCC, soDT, diaChi, email) VALUES
('NCC001', N'Cong ty Phu Tung Sai Gon', '02830000001', N'Quan 5, TP HCM', 'sales1@example.com'),
('NCC002', N'Phu Tung Mien Dong', '02830000002', N'Thu Duc, TP HCM', 'sales2@example.com');
GO

/* 6. Phu tung */
INSERT INTO PhuTung (maPT, tenPT, donViTinh, donGiaBan) VALUES
('PT001', N'Loc dau', N'Chiec', 150000),
('PT002', N'Loc gio', N'Chiec', 220000),
('PT003', N'Bugi', N'Chiec', 85000),
('PT004', N'Dau dong co', N'Lit', 120000),
('PT005', N'Bo phanh', N'Bo', 450000);
GO

/* 7. Kho */
INSERT INTO Kho (maKho, maPT, soLuongTon, viTriKho, soLTT, soLTD) VALUES
('KHO001', 'PT001', 50, N'Ke A1', 5, 100),
('KHO002', 'PT002', 30, N'Ke A2', 5, 80),
('KHO003', 'PT003', 100, N'Ke B1', 10, 200),
('KHO004', 'PT004', 80, N'Ke B2', 10, 150),
('KHO005', 'PT005', 20, N'Ke C1', 3, 50);
GO

/* 8. Hoa don */
INSERT INTO HoaDon (maHD, maKH, maNV, ngayLap, hinhThucThanhToan, soPhieuSC, tienDichVu, tienPhuTung, giamGia, tongTien, ghiChu, trangThai) VALUES
('HD001', 'KH001', 'NV05', '2026-03-01', N'Chuyển Khoản', NULL, 0, 600000, 0, 600000, N'Hóa đơn mẫu', N'Đã Thanh Toán'),
('HD002', 'KH002', 'NV05', '2026-03-05', N'Tiền Mặt', NULL, 0, 170000, 0, 170000, N'Hóa đơn mẫu', N'Đã Thanh Toán');
GO

/* 9. Chi tiet hoa don */
INSERT INTO ChiTietHoaDon (maHD, maPT, tenPhuTung, soLuong, donGia) VALUES
('HD001', 'PT001', N'Loc dau', 2, 150000),
('HD001', 'PT004', N'Dau dong co', 4, 120000),
('HD002', 'PT003', N'Bugi', 2, 85000);
GO

/* 10. Phieu bao hanh */
INSERT INTO PhieuBaoHanh (maPBH, ngayBD, ngayKT, trangThaiBH, maKH, soVIN, maHoaDon) VALUES
('BH001', '2026-01-10', '2027-01-10', N'CON_HAN', 'KH001', 'VIN001', 'HD001'),
('BH002', '2026-02-15', '2027-02-15', N'CON_HAN', 'KH002', 'VIN002', 'HD002');
GO

/* 11. Chi tiet bao hanh */
INSERT INTO ChiTietPhieuBaoHanh (maPBH, maPT, lyDo, huongXuLy) VALUES
('BH001', 'PT001', N'Bao hanh loc dau', N'Kiem tra va thay neu loi'),
('BH002', 'PT002', N'Bao hanh loc gio', N'Kiem tra tinh trang loc');
GO

/* 12. Phieu nhap kho */
INSERT INTO PhieuNhapKho (maPhieuNhap, maNCC, maNV, ngayNhap, tongTien) VALUES
('PN001', 'NCC001', 'NV04', '2026-03-01', 1800000),
('PN002', 'NCC002', 'NV04', '2026-03-06', 1000000);
GO

/* 13. Chi tiet nhap kho */
INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap, maPT, tenPT, soLuong, donGia) VALUES
('PN001', 'PT001', N'Loc dau', 10, 100000),
('PN001', 'PT002', N'Loc gio', 5, 160000),
('PN002', 'PT003', N'Bugi', 20, 50000);
GO

/* 14. Phieu tiep nhan */
INSERT INTO PhieuTiepNhan (maPTN, maNV, maKH, soVIN, ngayTN, yeuCauSC, trangThaiXe) VALUES
('TN001', 'NV01', 'KH001', 'VIN001', '2026-03-10T08:30:00', N'Thay dau va kiem tra loc gio', N'DA_TIEP_NHAN'),
('TN002', 'NV01', 'KH002', 'VIN002', '2026-03-10T09:00:00', N'Kiem tra he thong phanh', N'DA_TIEP_NHAN'),
('TN003', 'NV01', 'KH003', 'VIN003', '2026-03-11T10:00:00', N'Kiem tra bugi', N'DANG_KIEM_TRA');
GO

/* 15. Phieu sua chua */
INSERT INTO PhieuSuaChua (maPSC, maNV, maKH, soVIN, ngayBD, ngayHoanThanhDuKien, trangThai) VALUES
('SC001', 'NV02', 'KH001', 'VIN001', '2026-03-10T09:00:00', '2026-03-10', N'DANG_SUA'),
('SC002', 'NV02', 'KH002', 'VIN002', '2026-03-10T10:00:00', '2026-03-11', N'CHO_SUA');
GO

/* 16. Chi tiet sua chua */
INSERT INTO ChiTietSuaChua (maPSC, maPT, noiDungSC, tenPhuTung, soLuong, donGia, trangThai) VALUES
('SC001', 'PT001', N'Thay loc dau', N'Loc dau', 1, 150000, N'DA_HOAN_THANH'),
('SC001', 'PT004', N'Thay dau dong co', N'Dau dong co', 4, 120000, N'DA_HOAN_THANH'),
('SC002', 'PT005', N'Kiem tra va thay bo phanh', N'Bo phanh', 1, 450000, N'CHO_THUC_HIEN');
GO

/* 17. Phieu xuat kho */
INSERT INTO PhieuXuatKho (maPhieuXuat, maPSC, maNV, ngayXuat, trangThai, lyDoXuat) VALUES
('PX001', 'SC001', 'NV03', '2026-03-10', N'Đã xuất', N'Xuất vật tư thay thế theo phiếu sửa chữa'),
('PX002', 'SC002', 'NV03', '2026-03-10', N'Chờ xuất', N'Xuất vật tư thay thế theo phiếu sửa chữa');
GO

/* 18. Chi tiet xuat kho */
INSERT INTO ChiTietPhieuXuatKho (maPhieuXuat, maPT, soLuongYeuCau, soLuongXuat, donGia, tinhTrangXuat) VALUES
('PX001', 'PT001', 1, 1, 150000, N'Đã xuất'),
('PX001', 'PT004', 4, 4, 120000, N'Đã xuất'),
('PX002', 'PT005', 1, 0, 450000, N'Chờ xuất');
GO
/*============================================================
  TRUY VAN THU NGHIEM
============================================================*/

-- 1. Danh sach khach hang va phuong tien
SELECT kh.maKH, kh.hoTen, kh.soDT,
       pt.soVIN, pt.bienSoXe, pt.hangXe, pt.dongXe
FROM KhachHang kh
LEFT JOIN PhuongTien pt ON pt.maKH = kh.maKH;
GO

-- 2. Danh sach phieu sua chua
SELECT sc.maPSC, kh.hoTen, pt.bienSoXe,
       nv.hoTen AS nhanVien,
       sc.ngayBD, sc.trangThai
FROM PhieuSuaChua sc
JOIN KhachHang kh ON sc.maKH = kh.maKH
JOIN PhuongTien pt ON sc.soVIN = pt.soVIN
JOIN NhanVien nv ON sc.maNV = nv.maNV;
GO

-- 3. Tong tien tung hoa don
SELECT hd.maHD, kh.hoTen,
       SUM(ct.soLuong * ct.donGia) AS tongTien
FROM HoaDon hd
JOIN KhachHang kh ON hd.maKH = kh.maKH
JOIN ChiTietHoaDon ct ON hd.maHD = ct.maHD
GROUP BY hd.maHD, kh.hoTen;
GO

-- 4. Kiem tra ton kho
SELECT k.maKho, pt.maPT, pt.tenPT,
       k.soLuongTon, k.soLTT,
       CASE WHEN k.soLuongTon <= k.soLTT
            THEN N'CAN NHAP HANG'
            ELSE N'DU HANG'
       END AS canhBao
FROM Kho k
JOIN PhuTung pt ON k.maPT = pt.maPT;
GO

-- 5. Danh sach phieu bao hanh con han
SELECT bh.maPBH, kh.hoTen, pt.bienSoXe,
       bh.ngayBD, bh.ngayKT, bh.trangThaiBH
FROM PhieuBaoHanh bh
JOIN KhachHang kh ON bh.maKH = kh.maKH
JOIN PhuongTien pt ON bh.soVIN = pt.soVIN
WHERE bh.ngayBD <= CAST(GETDATE() AS DATE)
  AND bh.ngayKT >= CAST(GETDATE() AS DATE);
GO

-- 6. Chi phi phu tung theo phieu sua chua
SELECT sc.maPSC,
       COALESCE(SUM(ct.soLuong * ct.donGia), 0) AS chiPhiPhuTung
FROM PhieuSuaChua sc
LEFT JOIN ChiTietSuaChua ct ON sc.maPSC = ct.maPSC
GROUP BY sc.maPSC;
GO

-- 7. Lich su nhap kho
SELECT pn.maPhieuNhap, pn.ngayNhap, ncc.tenNCC,
       ct.maPT, ct.tenPT, ct.soLuong, ct.donGia
FROM PhieuNhapKho pn
JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC
JOIN ChiTietPhieuNhapKho ct ON pn.maPhieuNhap = ct.maPhieuNhap;
GO

-- 8. Lich su xuat kho
SELECT px.maPhieuXuat, px.ngayXuat, px.maPSC,
       ct.maPT, pt.tenPT,
       ct.soLuongYeuCau, ct.soLuongXuat,
       ct.donGia, ct.tinhTrangXuat, px.lyDoXuat
FROM PhieuXuatKho px
JOIN ChiTietPhieuXuatKho ct ON px.maPhieuXuat = ct.maPhieuXuat
JOIN PhuTung pt ON ct.maPT = pt.maPT;
GO

PRINT N'Da tao CSDL QuanLySuaChuaXe va du lieu mau.';
GO

CREATE TABLE ChiTietCongViecSuaChua (
    maPSC VARCHAR(20) NOT NULL,
    stt INT NOT NULL,
    noiDungCongViec NVARCHAR(255) NOT NULL,
    tenKTV NVARCHAR(100),
    tienCong DECIMAL(15,2) NOT NULL DEFAULT 0,

    CONSTRAINT PK_ChiTietCongViecSuaChua
        PRIMARY KEY (maPSC, stt),

    CONSTRAINT FK_CTCV_PSC
        FOREIGN KEY (maPSC) REFERENCES PhieuSuaChua(maPSC),

    CONSTRAINT CK_CTCV_TienCong
        CHECK (tienCong >= 0)
);

USE QuanLySuaChuaXe;
GO

BEGIN TRANSACTION;
BEGIN TRY

    -- TẦNG 1: Xóa các bảng chi tiết / bảng phụ thuộc nhiều nhất
    DELETE FROM ChiTietPhieuXuatKho;
    DELETE FROM ChiTietPhieuNhapKho;
    DELETE FROM ChiTietCongViecSuaChua;
    DELETE FROM ChiTietSuaChua;
    DELETE FROM ChiTietPhieuBaoHanh;
    DELETE FROM ChiTietHoaDon;

    -- TẦNG 2: Xóa các bảng phiếu giao dịch
    DELETE FROM PhieuXuatKho;
    DELETE FROM PhieuNhapKho;
    DELETE FROM PhieuBaoHanh;
    DELETE FROM PhieuSuaChua;
    DELETE FROM PhieuTiepNhan;
    DELETE FROM HoaDon;

    -- TẦNG 3: Xóa các bảng chứa thông tin đối tượng & tài khoản
    DELETE FROM Kho;
    DELETE FROM PhuongTien;
    DELETE FROM TaiKhoan;

    -- TẦNG 4: Xóa các bảng danh mục gốc (Bảng cha không phụ thuộc bảng nào)
    DELETE FROM PhuTung;
    DELETE FROM NhaCungCap;
    DELETE FROM KhachHang;
    DELETE FROM NhanVien;

    -- Xác nhận xóa thành công nếu không có lỗi
    COMMIT TRANSACTION;
    PRINT N'Đã xóa sạch toàn bộ dữ liệu! Cấu trúc các bảng vẫn được giữ nguyên.';

END TRY
BEGIN CATCH
    -- Tự động khôi phục dữ liệu nếu có lỗi xảy ra
    ROLLBACK TRANSACTION;
    PRINT N'Có lỗi xảy ra, thao tác xóa đã được hủy!';
    PRINT ERROR_MESSAGE();
END CATCH;
GO