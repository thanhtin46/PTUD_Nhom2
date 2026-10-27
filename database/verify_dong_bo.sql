-- Chạy sau upgrade_dong_bo.sql + seed_demo.sql trong database gara.
SET NOCOUNT ON;
IF COL_LENGTH('dbo.PhieuBaoHanh','ngayLap') IS NULL
 THROW 51000, N'Thiếu ngày lập bảo hành', 1;
IF OBJECT_ID('dbo.ChiTietDichVuHoaDon','U') IS NULL
 THROW 51000, N'Thiếu bảng tiền công hóa đơn', 1;
IF EXISTS (
 SELECT 1 FROM HoaDon h
 OUTER APPLY (SELECT SUM(soLuong*donGia) amount FROM ChiTietHoaDon WHERE maHD=h.maHD) p
 OUTER APPLY (SELECT SUM(soLuong*donGia) amount FROM ChiTietDichVuHoaDon WHERE maHD=h.maHD) d
 WHERE h.maHD IN ('HD101','HD102','HD103','HD104','HD105','HD106','HD107','HD108','HD109','HD110')
 AND (h.tienPhuTung<>COALESCE(p.amount,0) OR h.tienDichVu<>COALESCE(d.amount,0)
 OR h.tongTien<>h.tienPhuTung+h.tienDichVu-ROUND((h.tienPhuTung+h.tienDichVu)*h.giamGia/100,0)))
 THROW 51001, N'Tổng hóa đơn demo không khớp chi tiết',1;
IF EXISTS (
 SELECT 1 FROM HangMucBaoHanh m JOIN PhieuBaoHanh b ON m.maPBH=b.maPBH
 WHERE b.maPBH IN ('PBH101','PBH102','PBH103','PBH104','PBH105','PBH106')
 AND (m.ngayHetHan<>DATEADD(month,m.thoiHanBaoHanh,b.ngayBD) OR b.ngayKT<m.ngayHetHan))
 THROW 51002, N'Ngày bảo hành không khớp thời hạn',1;
IF EXISTS (SELECT 1 FROM Kho WHERE soLuongTon<0)
 THROW 51003, N'Tồn kho âm',1;
DBCC CHECKCONSTRAINTS WITH ALL_CONSTRAINTS;
SELECT N'Khách hàng' AS danhMuc,COUNT(*) AS soLuong FROM KhachHang
UNION ALL SELECT N'Phương tiện',COUNT(*) FROM PhuongTien
UNION ALL SELECT N'Nhân viên',COUNT(*) FROM NhanVien
UNION ALL SELECT N'Phụ tùng',COUNT(*) FROM PhuTung
UNION ALL SELECT N'Phiếu tiếp nhận',COUNT(*) FROM PhieuTiepNhan
UNION ALL SELECT N'Phiếu sửa chữa',COUNT(*) FROM PhieuSuaChua
UNION ALL SELECT N'Hóa đơn',COUNT(*) FROM HoaDon
UNION ALL SELECT N'Phiếu bảo hành',COUNT(*) FROM PhieuBaoHanh
UNION ALL SELECT N'Phiếu nhập kho',COUNT(*) FROM PhieuNhapKho
UNION ALL SELECT N'Phiếu xuất kho',COUNT(*) FROM PhieuXuatKho;
