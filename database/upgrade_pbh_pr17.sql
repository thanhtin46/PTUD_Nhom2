-- Run in the existing garage database before using PR #17. Safe to rerun.
-- Keeps the existing warranty and spare-part detail tables intact.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH('dbo.PhieuBaoHanh', 'maNV') IS NULL
    ALTER TABLE dbo.PhieuBaoHanh ADD maNV VARCHAR(20) NULL
        CONSTRAINT FK_PBH_NhanVien REFERENCES dbo.NhanVien(maNV);
IF COL_LENGTH('dbo.PhieuBaoHanh', 'ngayLap') IS NULL
    ALTER TABLE dbo.PhieuBaoHanh ADD ngayLap DATE NOT NULL
        CONSTRAINT DF_PBH_NgayLap DEFAULT (CONVERT(date, GETDATE())) WITH VALUES;
IF COL_LENGTH('dbo.PhieuBaoHanh', 'ghiChu') IS NULL
    ALTER TABLE dbo.PhieuBaoHanh ADD ghiChu NVARCHAR(MAX) NULL;
IF OBJECT_ID('dbo.HangMucBaoHanh', 'U') IS NULL
    CREATE TABLE dbo.HangMucBaoHanh (
        maCTPBH INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        maPBH VARCHAR(20) NOT NULL,
        tenHangMuc NVARCHAR(255) NOT NULL,
        thoiHanBaoHanh INT NOT NULL CHECK (thoiHanBaoHanh > 0),
        ngayHetHan DATE NOT NULL,
        dieuKienBaoHanh NVARCHAR(MAX) NULL,
        ghiChu NVARCHAR(MAX) NULL,
        CONSTRAINT FK_HangMucBaoHanh_Phieu FOREIGN KEY (maPBH)
            REFERENCES dbo.PhieuBaoHanh(maPBH) ON DELETE CASCADE
    );
COMMIT TRANSACTION;