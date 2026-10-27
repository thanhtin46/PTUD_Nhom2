-- Nâng cấp schema từ main; không xóa dữ liệu hiện có. Chọn database trước khi chạy.
SET XACT_ABORT ON;
GO
IF COL_LENGTH('dbo.PhieuBaoHanh', 'ngayLap') IS NULL ALTER TABLE dbo.PhieuBaoHanh ADD ngayLap DATE NOT NULL CONSTRAINT DF_PBH_NgayLap DEFAULT (CONVERT(date,GETDATE())) WITH VALUES;
GO
IF COL_LENGTH('dbo.PhieuBaoHanh', 'maNV') IS NULL ALTER TABLE dbo.PhieuBaoHanh ADD maNV VARCHAR(20) NULL;
GO
IF COL_LENGTH('dbo.PhieuBaoHanh', 'ghiChu') IS NULL ALTER TABLE dbo.PhieuBaoHanh ADD ghiChu NVARCHAR(MAX) NULL;
GO
IF COL_LENGTH('dbo.NhanVien', 'email') IS NULL ALTER TABLE dbo.NhanVien ADD email VARCHAR(100) NULL;
GO
IF COL_LENGTH('dbo.NhanVien', 'bacNghe') IS NULL ALTER TABLE dbo.NhanVien ADD bacNghe NVARCHAR(50) NULL;
GO
IF COL_LENGTH('dbo.NhanVien', 'boPhan') IS NULL ALTER TABLE dbo.NhanVien ADD boPhan NVARCHAR(100) NULL;
GO
IF COL_LENGTH('dbo.NhanVien', 'ngayVaoLam') IS NULL ALTER TABLE dbo.NhanVien ADD ngayVaoLam VARCHAR(20) NULL;
GO
IF COL_LENGTH('dbo.NhanVien', 'trangThai') IS NULL ALTER TABLE dbo.NhanVien ADD trangThai NVARCHAR(50) NULL;
GO
IF COL_LENGTH('dbo.PhieuNhapKho', 'trangThai') IS NULL ALTER TABLE dbo.PhieuNhapKho ADD trangThai VARCHAR(20) NOT NULL CONSTRAINT DF_PNK_TrangThai DEFAULT 'DA_NHAP' WITH VALUES;
GO
IF COL_LENGTH('dbo.PhieuNhapKho', 'ngayDuKienGiao') IS NULL ALTER TABLE dbo.PhieuNhapKho ADD ngayDuKienGiao DATE NULL;
GO
IF COL_LENGTH('dbo.PhieuNhapKho', 'ghiChu') IS NULL ALTER TABLE dbo.PhieuNhapKho ADD ghiChu NVARCHAR(255) NULL;
GO
IF OBJECT_ID('dbo.HangMucBaoHanh','U') IS NULL
CREATE TABLE dbo.HangMucBaoHanh (
 maCTPBH INT IDENTITY PRIMARY KEY, maPBH VARCHAR(20) NOT NULL,
 tenHangMuc NVARCHAR(255) NOT NULL, thoiHanBaoHanh INT NOT NULL CHECK(thoiHanBaoHanh>0),
 ngayHetHan DATE NOT NULL, dieuKienBaoHanh NVARCHAR(MAX), ghiChu NVARCHAR(MAX),
 FOREIGN KEY(maPBH) REFERENCES dbo.PhieuBaoHanh(maPBH));
GO
IF OBJECT_ID('dbo.ChiTietCongViecSuaChua','U') IS NULL
CREATE TABLE dbo.ChiTietCongViecSuaChua (
 maPSC VARCHAR(20) NOT NULL, stt INT NOT NULL, noiDungCongViec NVARCHAR(255) NOT NULL,
 tenKTV NVARCHAR(100), tienCong DECIMAL(15,2) NOT NULL CHECK(tienCong>=0),
 PRIMARY KEY(maPSC,stt), FOREIGN KEY(maPSC) REFERENCES dbo.PhieuSuaChua(maPSC));
GO
IF OBJECT_ID('dbo.ChiTietDichVuHoaDon','U') IS NULL
CREATE TABLE dbo.ChiTietDichVuHoaDon (
 maHD VARCHAR(20) NOT NULL, stt INT NOT NULL, tenDichVu NVARCHAR(255) NOT NULL,
 soLuong INT NOT NULL CHECK(soLuong>0), donGia DECIMAL(15,2) NOT NULL CHECK(donGia>=0),
 PRIMARY KEY(maHD,stt), FOREIGN KEY(maHD) REFERENCES dbo.HoaDon(maHD));
GO

-- Liên kết tường minh, không suy đoán mã sửa chữa từ số phiếu tiếp nhận.
IF COL_LENGTH('dbo.PhieuSuaChua','maPTN') IS NULL
 ALTER TABLE dbo.PhieuSuaChua ADD maPTN VARCHAR(20) NULL REFERENCES dbo.PhieuTiepNhan(maPTN);
GO
UPDATE sc SET maPTN=tn.maPTN FROM PhieuSuaChua sc
JOIN PhieuTiepNhan tn ON sc.maPSC='SC'+SUBSTRING(tn.maPTN,3,18)
 AND sc.soVIN=tn.soVIN AND sc.maKH=tn.maKH WHERE sc.maPTN IS NULL;
GO
