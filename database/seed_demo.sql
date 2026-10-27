-- Dữ liệu demo bổ sung, mã từ 101; chạy lại không nhân đôi hay ghi đè dữ liệu.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNV=N'NV101')
 INSERT INTO NhanVien (maNV,hoTen,chucVu,soDT,email,bacNghe,boPhan,ngayVaoLam,trangThai) VALUES (N'NV101',N'Lê Văn Cường',N'Nhân viên sửa chữa',N'0908100101',N'nv101@example.com',N'4/7',N'Xưởng dịch vụ',N'01/01/2025',N'Đang làm việc');
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maNV=N'NV101')
 INSERT INTO TaiKhoan (maNV,taiKhoan,matKhau,trangThai) VALUES (N'NV101',N'demo101',N'123',N'HOAT_DONG');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNV=N'NV102')
 INSERT INTO NhanVien (maNV,hoTen,chucVu,soDT,email,bacNghe,boPhan,ngayVaoLam,trangThai) VALUES (N'NV102',N'Hoàng Đình Kha',N'Nhân viên sửa chữa',N'0908100102',N'nv102@example.com',N'4/7',N'Xưởng dịch vụ',N'01/01/2025',N'Đang làm việc');
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maNV=N'NV102')
 INSERT INTO TaiKhoan (maNV,taiKhoan,matKhau,trangThai) VALUES (N'NV102',N'demo102',N'123',N'HOAT_DONG');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNV=N'NV103')
 INSERT INTO NhanVien (maNV,hoTen,chucVu,soDT,email,bacNghe,boPhan,ngayVaoLam,trangThai) VALUES (N'NV103',N'Nguyễn Thị Hạnh',N'Nhân viên tiếp tân',N'0908100103',N'nv103@example.com',N'4/7',N'Xưởng dịch vụ',N'01/01/2025',N'Đang làm việc');
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maNV=N'NV103')
 INSERT INTO TaiKhoan (maNV,taiKhoan,matKhau,trangThai) VALUES (N'NV103',N'demo103',N'123',N'HOAT_DONG');
IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNV=N'NV104')
 INSERT INTO NhanVien (maNV,hoTen,chucVu,soDT,email,bacNghe,boPhan,ngayVaoLam,trangThai) VALUES (N'NV104',N'Trần Quốc Phong',N'Nhân viên kho',N'0908100104',N'nv104@example.com',N'4/7',N'Xưởng dịch vụ',N'01/01/2025',N'Đang làm việc');
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE maNV=N'NV104')
 INSERT INTO TaiKhoan (maNV,taiKhoan,matKhau,trangThai) VALUES (N'NV104',N'demo104',N'123',N'HOAT_DONG');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH101')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH101',N'Nguyễn Minh Anh',N'0918200101',N'079100000101',N'12 Nguyễn Văn Cừ, TP.HCM',N'kh101@example.com',N'Dữ liệu học phần',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH102')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH102',N'Trần Ngọc Lan',N'0918200102',N'079100000102',N'13 Nguyễn Văn Cừ, TP.HCM',N'kh102@example.com',N'Dữ liệu học phần',N'23/10/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH103')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH103',N'Lê Quốc Bảo',N'0918200103',N'079100000103',N'14 Nguyễn Văn Cừ, TP.HCM',N'kh103@example.com',N'Dữ liệu học phần',N'19/10/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH104')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH104',N'Phạm Thu Hà',N'0918200104',N'079100000104',N'15 Nguyễn Văn Cừ, TP.HCM',N'kh104@example.com',N'Dữ liệu học phần',N'15/10/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH105')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH105',N'Hoàng Đức Huy',N'0918200105',N'079100000105',N'16 Nguyễn Văn Cừ, TP.HCM',N'kh105@example.com',N'Dữ liệu học phần',N'11/10/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH106')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH106',N'Đặng Thị Mai',N'0918200106',N'079100000106',N'17 Nguyễn Văn Cừ, TP.HCM',N'kh106@example.com',N'Dữ liệu học phần',N'07/10/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH107')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH107',N'Bùi Thanh Tùng',N'0918200107',N'079100000107',N'18 Nguyễn Văn Cừ, TP.HCM',N'kh107@example.com',N'Dữ liệu học phần',N'03/10/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH108')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH108',N'Đỗ Hải Yến',N'0918200108',N'079100000108',N'19 Nguyễn Văn Cừ, TP.HCM',N'kh108@example.com',N'Dữ liệu học phần',N'29/09/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH109')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH109',N'Vũ Gia Hưng',N'0918200109',N'079100000109',N'20 Nguyễn Văn Cừ, TP.HCM',N'kh109@example.com',N'Dữ liệu học phần',N'25/09/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH110')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH110',N'Ngô Bảo Châu',N'0918200110',N'079100000110',N'21 Nguyễn Văn Cừ, TP.HCM',N'kh110@example.com',N'Dữ liệu học phần',N'21/09/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH111')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH111',N'Dương Tuấn Kiệt',N'0918200111',N'079100000111',N'22 Nguyễn Văn Cừ, TP.HCM',N'kh111@example.com',N'Dữ liệu học phần',N'17/09/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH112')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH112',N'Hồ Mỹ Linh',N'0918200112',N'079100000112',N'23 Nguyễn Văn Cừ, TP.HCM',N'kh112@example.com',N'Dữ liệu học phần',N'13/09/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH113')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH113',N'Phan Quang Dũng',N'0918200113',N'079100000113',N'24 Nguyễn Văn Cừ, TP.HCM',N'kh113@example.com',N'Dữ liệu học phần',N'09/09/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH114')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH114',N'Võ Khánh Ngọc',N'0918200114',N'079100000114',N'25 Nguyễn Văn Cừ, TP.HCM',N'kh114@example.com',N'Dữ liệu học phần',N'05/09/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH115')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH115',N'Mai Anh Khoa',N'0918200115',N'079100000115',N'26 Nguyễn Văn Cừ, TP.HCM',N'kh115@example.com',N'Dữ liệu học phần',N'01/09/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH116')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH116',N'Đinh Phương Thảo',N'0918200116',N'079100000116',N'27 Nguyễn Văn Cừ, TP.HCM',N'kh116@example.com',N'Dữ liệu học phần',N'28/08/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH117')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH117',N'Cao Nhật Minh',N'0918200117',N'079100000117',N'28 Nguyễn Văn Cừ, TP.HCM',N'kh117@example.com',N'Dữ liệu học phần',N'24/08/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH118')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH118',N'Lý Thùy Dương',N'0918200118',N'079100000118',N'29 Nguyễn Văn Cừ, TP.HCM',N'kh118@example.com',N'Dữ liệu học phần',N'20/08/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH119')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH119',N'Tạ Hoàng Long',N'0918200119',N'079100000119',N'30 Nguyễn Văn Cừ, TP.HCM',N'kh119@example.com',N'Dữ liệu học phần',N'16/08/2026');
IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH=N'KH120')
 INSERT INTO KhachHang (maKH,hoTen,soDT,chungMinh,diaChi,email,ghiChu,ngayDangKy) VALUES (N'KH120',N'Trịnh Thanh Trúc',N'0918200120',N'079100000120',N'31 Nguyễn Văn Cừ, TP.HCM',N'kh120@example.com',N'Dữ liệu học phần',N'12/08/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO101')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO101',N'51A-20101',N'Toyota',N'Vios',2020,N'KH101',N'MAY101',N'Trắng',N'10000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO102')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO102',N'51A-20102',N'Honda',N'City',2021,N'KH102',N'MAY102',N'Đen',N'13000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO103')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO103',N'51A-20103',N'Mazda',N'CX-5',2022,N'KH103',N'MAY103',N'Đỏ',N'16000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO104')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO104',N'51A-20104',N'Hyundai',N'Accent',2023,N'KH104',N'MAY104',N'Bạc',N'19000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO105')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO105',N'51A-20105',N'Kia',N'Seltos',2024,N'KH105',N'MAY105',N'Trắng',N'22000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO106')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO106',N'51A-20106',N'Ford',N'Ranger',2025,N'KH106',N'MAY106',N'Đen',N'25000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO107')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO107',N'51A-20107',N'Mitsubishi',N'Xpander',2020,N'KH107',N'MAY107',N'Đỏ',N'28000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO108')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO108',N'51A-20108',N'Suzuki',N'XL7',2021,N'KH108',N'MAY108',N'Bạc',N'31000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO109')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO109',N'51A-20109',N'VinFast',N'VF 8',2022,N'KH109',N'MAY109',N'Trắng',N'34000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO110')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO110',N'51A-20110',N'Nissan',N'Navara',2023,N'KH110',N'MAY110',N'Đen',N'37000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO111')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO111',N'51A-20111',N'Toyota',N'Vios',2024,N'KH111',N'MAY111',N'Đỏ',N'40000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO112')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO112',N'51A-20112',N'Honda',N'City',2025,N'KH112',N'MAY112',N'Bạc',N'43000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO113')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO113',N'51A-20113',N'Mazda',N'CX-5',2020,N'KH113',N'MAY113',N'Trắng',N'46000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO114')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO114',N'51A-20114',N'Hyundai',N'Accent',2021,N'KH114',N'MAY114',N'Đen',N'49000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO115')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO115',N'51A-20115',N'Kia',N'Seltos',2022,N'KH115',N'MAY115',N'Đỏ',N'52000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO116')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO116',N'51A-20116',N'Ford',N'Ranger',2023,N'KH116',N'MAY116',N'Bạc',N'55000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO117')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO117',N'51A-20117',N'Mitsubishi',N'Xpander',2024,N'KH117',N'MAY117',N'Trắng',N'58000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO118')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO118',N'51A-20118',N'Suzuki',N'XL7',2025,N'KH118',N'MAY118',N'Đen',N'61000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO119')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO119',N'51A-20119',N'VinFast',N'VF 8',2020,N'KH119',N'MAY119',N'Đỏ',N'64000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO120')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO120',N'51A-20120',N'Nissan',N'Navara',2021,N'KH120',N'MAY120',N'Bạc',N'67000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO121')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO121',N'51A-20121',N'Toyota',N'Vios',2022,N'KH101',N'MAY121',N'Trắng',N'70000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO122')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO122',N'51A-20122',N'Honda',N'City',2023,N'KH102',N'MAY122',N'Đen',N'73000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO123')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO123',N'51A-20123',N'Mazda',N'CX-5',2024,N'KH103',N'MAY123',N'Đỏ',N'76000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO124')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO124',N'51A-20124',N'Hyundai',N'Accent',2025,N'KH104',N'MAY124',N'Bạc',N'79000',N'27/10/2026');
IF NOT EXISTS (SELECT 1 FROM PhuongTien WHERE soVIN=N'VINDEMO125')
 INSERT INTO PhuongTien (soVIN,bienSoXe,hangXe,dongXe,maSX,maKH,soMay,mauSon,odo,ngayTiepNhanGanNhat) VALUES (N'VINDEMO125',N'51A-20125',N'Kia',N'Seltos',2020,N'KH105',N'MAY125',N'Trắng',N'82000',N'27/10/2026');
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
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC101',N'NV101',N'KH101',N'VINDEMO101',N'2026-10-15T09:00:00',N'2026-10-16',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD101',N'KH101',N'NV05',N'2026-10-15',N'TIEN_MAT',N'SC101',200000,150000,0,350000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD101' AND maPT=N'PT101')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD101',N'PT101',N'Lọc dầu động cơ',1,150000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD101' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD101',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH101')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH101',N'2026-10-15',N'2027-04-15',N'CON_HAN',N'KH101',N'VINDEMO101',N'HD101',N'NV01',N'2026-10-15',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM HangMucBaoHanh WHERE maPBH=N'PBH101' AND tenHangMuc=N'Lọc dầu động cơ')
 INSERT INTO HangMucBaoHanh (maPBH,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH101',N'Lọc dầu động cơ',6,N'2027-04-15',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN102')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN102',N'NV01',N'KH102',N'VINDEMO102',N'2026-10-16T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC102')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC102',N'NV102',N'KH102',N'VINDEMO102',N'2026-10-16T09:00:00',N'2026-10-17',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD102',N'KH102',N'NV05',N'2026-10-16',N'CHUYEN_KHOAN',N'SC102',200000,220000,0,420000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD102' AND maPT=N'PT102')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD102',N'PT102',N'Lọc gió động cơ',1,220000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD102' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD102',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH102')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH102',N'2026-10-16',N'2027-04-16',N'CON_HAN',N'KH102',N'VINDEMO102',N'HD102',N'NV01',N'2026-10-16',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM HangMucBaoHanh WHERE maPBH=N'PBH102' AND tenHangMuc=N'Lọc gió động cơ')
 INSERT INTO HangMucBaoHanh (maPBH,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH102',N'Lọc gió động cơ',6,N'2027-04-16',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN103')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN103',N'NV01',N'KH103',N'VINDEMO103',N'2026-10-17T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC103')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC103',N'NV101',N'KH103',N'VINDEMO103',N'2026-10-17T09:00:00',N'2026-10-18',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD103',N'KH103',N'NV05',N'2026-10-17',N'THE_POS',N'SC103',200000,250000,0,450000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD103' AND maPT=N'PT103')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD103',N'PT103',N'Bugi iridium',1,250000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD103' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD103',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH103')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH103',N'2026-10-17',N'2027-04-17',N'CON_HAN',N'KH103',N'VINDEMO103',N'HD103',N'NV01',N'2026-10-17',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM HangMucBaoHanh WHERE maPBH=N'PBH103' AND tenHangMuc=N'Bugi iridium')
 INSERT INTO HangMucBaoHanh (maPBH,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH103',N'Bugi iridium',6,N'2027-04-17',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN104')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN104',N'NV01',N'KH104',N'VINDEMO104',N'2026-10-18T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC104')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC104',N'NV102',N'KH104',N'VINDEMO104',N'2026-10-18T09:00:00',N'2026-10-19',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD104',N'KH104',N'NV05',N'2026-10-18',N'TIEN_MAT',N'SC104',200000,180000,0,380000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD104' AND maPT=N'PT104')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD104',N'PT104',N'Dầu động cơ 5W30',1,180000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD104' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD104',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH104')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH104',N'2026-10-18',N'2027-04-18',N'CON_HAN',N'KH104',N'VINDEMO104',N'HD104',N'NV01',N'2026-10-18',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM HangMucBaoHanh WHERE maPBH=N'PBH104' AND tenHangMuc=N'Dầu động cơ 5W30')
 INSERT INTO HangMucBaoHanh (maPBH,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH104',N'Dầu động cơ 5W30',6,N'2027-04-18',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN105')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN105',N'NV01',N'KH105',N'VINDEMO105',N'2026-10-19T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC105')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC105',N'NV101',N'KH105',N'VINDEMO105',N'2026-10-19T09:00:00',N'2026-10-20',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD105',N'KH105',N'NV05',N'2026-10-19',N'CHUYEN_KHOAN',N'SC105',200000,850000,0,1050000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD105' AND maPT=N'PT105')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD105',N'PT105',N'Má phanh trước',1,850000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD105' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD105',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH105')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH105',N'2026-10-19',N'2027-04-19',N'CON_HAN',N'KH105',N'VINDEMO105',N'HD105',N'NV01',N'2026-10-19',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM HangMucBaoHanh WHERE maPBH=N'PBH105' AND tenHangMuc=N'Má phanh trước')
 INSERT INTO HangMucBaoHanh (maPBH,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH105',N'Má phanh trước',6,N'2027-04-19',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN106')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN106',N'NV01',N'KH106',N'VINDEMO106',N'2026-10-20T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC106')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC106',N'NV102',N'KH106',N'VINDEMO106',N'2026-10-20T09:00:00',N'2026-10-21',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD106',N'KH106',N'NV05',N'2026-10-20',N'THE_POS',N'SC106',200000,200000,0,400000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD106' AND maPT=N'PT106')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD106',N'PT106',N'Lọc gió điều hòa',1,200000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD106' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD106',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuBaoHanh WHERE maPBH=N'PBH106')
 INSERT INTO PhieuBaoHanh (maPBH,ngayBD,ngayKT,trangThaiBH,maKH,soVIN,maHoaDon,maNV,ngayLap,ghiChu) VALUES (N'PBH106',N'2026-10-20',N'2027-04-20',N'CON_HAN',N'KH106',N'VINDEMO106',N'HD106',N'NV01',N'2026-10-20',N'Bảo hành vật tư thay thế');
IF NOT EXISTS (SELECT 1 FROM HangMucBaoHanh WHERE maPBH=N'PBH106' AND tenHangMuc=N'Lọc gió điều hòa')
 INSERT INTO HangMucBaoHanh (maPBH,tenHangMuc,thoiHanBaoHanh,ngayHetHan,dieuKienBaoHanh,ghiChu) VALUES (N'PBH106',N'Lọc gió điều hòa',6,N'2027-04-20',N'Lỗi nhà sản xuất',N'Giữ hóa đơn để đối chiếu');
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN107')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN107',N'NV01',N'KH107',N'VINDEMO107',N'2026-10-21T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC107')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC107',N'NV101',N'KH107',N'VINDEMO107',N'2026-10-21T09:00:00',N'2026-10-22',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD107',N'KH107',N'NV05',N'2026-10-21',N'TIEN_MAT',N'SC107',200000,1850000,0,2050000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD107' AND maPT=N'PT107')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD107',N'PT107',N'Ắc quy 12V 60Ah',1,1850000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD107' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD107',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN108')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN108',N'NV01',N'KH108',N'VINDEMO108',N'2026-10-22T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC108')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC108',N'NV102',N'KH108',N'VINDEMO108',N'2026-10-22T09:00:00',N'2026-10-23',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD108',N'KH108',N'NV05',N'2026-10-22',N'CHUYEN_KHOAN',N'SC108',200000,1600000,0,1800000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD108' AND maPT=N'PT108')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD108',N'PT108',N'Lốp 205/55R16',1,1600000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD108' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD108',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN109')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN109',N'NV01',N'KH109',N'VINDEMO109',N'2026-10-23T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC109')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC109',N'NV101',N'KH109',N'VINDEMO109',N'2026-10-23T09:00:00',N'2026-10-24',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD109',N'KH109',N'NV05',N'2026-10-23',N'THE_POS',N'SC109',200000,350000,0,550000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD109' AND maPT=N'PT109')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD109',N'PT109',N'Gạt mưa trước',1,350000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD109' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD109',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN110')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN110',N'NV01',N'KH110',N'VINDEMO110',N'2026-10-24T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC110')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC110',N'NV102',N'KH110',N'VINDEMO110',N'2026-10-24T09:00:00',N'2026-10-25',N'HOAN_THANH');
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
 INSERT INTO HoaDon (maHD,maKH,maNV,ngayLap,hinhThucThanhToan,soPhieuSC,tienDichVu,tienPhuTung,giamGia,tongTien,ghiChu,trangThai) VALUES (N'HD110',N'KH110',N'NV05',N'2026-10-24',N'TIEN_MAT',N'SC110',200000,120000,0,320000,N'Dữ liệu học phần',N'DA_THANH_TOAN');
IF NOT EXISTS (SELECT 1 FROM ChiTietHoaDon WHERE maHD=N'HD110' AND maPT=N'PT110')
 INSERT INTO ChiTietHoaDon (maHD,maPT,tenPhuTung,soLuong,donGia) VALUES (N'HD110',N'PT110',N'Nước làm mát',1,120000);
IF NOT EXISTS (SELECT 1 FROM ChiTietDichVuHoaDon WHERE maHD=N'HD110' AND stt=1)
 INSERT INTO ChiTietDichVuHoaDon (maHD,stt,tenDichVu,soLuong,donGia) VALUES (N'HD110',1,N'Kiểm tra, thay thế và chạy thử',1,200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN111')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN111',N'NV01',N'KH111',N'VINDEMO111',N'2026-10-25T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Hoàn thành');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC111')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC111',N'NV101',N'KH111',N'VINDEMO111',N'2026-10-25T09:00:00',N'2026-10-26',N'HOAN_THANH');
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
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC112',N'NV102',N'KH112',N'VINDEMO112',N'2026-10-26T09:00:00',N'2026-10-27',N'HOAN_THANH');
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
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC113',N'NV101',N'KH113',N'VINDEMO113',N'2026-10-27T09:00:00',N'2026-10-28',N'CHO_SUA');
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
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC114',N'NV102',N'KH114',N'VINDEMO114',N'2026-10-27T09:00:00',N'2026-10-28',N'CHO_SUA');
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
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC115',N'NV101',N'KH115',N'VINDEMO115',N'2026-10-27T09:00:00',N'2026-10-28',N'DANG_SUA');
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
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC116',N'NV102',N'KH116',N'VINDEMO116',N'2026-10-27T09:00:00',N'2026-10-28',N'DANG_SUA');
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
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC117',N'NV101',N'KH117',N'VINDEMO117',N'2026-10-27T09:00:00',N'2026-10-28',N'CHO_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC117' AND maPT=N'PT117')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC117',N'PT117',N'Thay thế theo kết quả kiểm tra',N'Cảm biến oxy',1,950000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC117' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC117',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN118')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN118',N'NV01',N'KH118',N'VINDEMO118',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Đã phân công');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC118')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC118',N'NV102',N'KH118',N'VINDEMO118',N'2026-10-27T09:00:00',N'2026-10-28',N'CHO_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC118' AND maPT=N'PT118')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC118',N'PT118',N'Thay thế theo kết quả kiểm tra',N'Bơm nước',1,1250000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC118' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC118',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN119')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN119',N'NV01',N'KH119',N'VINDEMO119',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Đang sửa');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC119')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC119',N'NV101',N'KH119',N'VINDEMO119',N'2026-10-27T09:00:00',N'2026-10-28',N'DANG_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC119' AND maPT=N'PT119')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC119',N'PT119',N'Thay thế theo kết quả kiểm tra',N'Lọc nhiên liệu',1,380000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC119' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC119',1,N'Kiểm tra, thay thế và chạy thử',N'Lê Văn Cường',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuTiepNhan WHERE maPTN=N'TN120')
 INSERT INTO PhieuTiepNhan (maPTN,maNV,maKH,soVIN,ngayTN,yeuCauSC,trangThaiXe) VALUES (N'TN120',N'NV01',N'KH120',N'VINDEMO120',N'2026-10-27T08:00:00',N'Bảo dưỡng định kỳ và kiểm tra an toàn',N'Chờ phụ tùng');
IF NOT EXISTS (SELECT 1 FROM PhieuSuaChua WHERE maPSC=N'SC120')
 INSERT INTO PhieuSuaChua (maPSC,maNV,maKH,soVIN,ngayBD,ngayHoanThanhDuKien,trangThai) VALUES (N'SC120',N'NV102',N'KH120',N'VINDEMO120',N'2026-10-27T09:00:00',N'2026-10-28',N'DANG_SUA');
IF NOT EXISTS (SELECT 1 FROM ChiTietSuaChua WHERE maPSC=N'SC120' AND maPT=N'PT120')
 INSERT INTO ChiTietSuaChua (maPSC,maPT,noiDungSC,tenPhuTung,soLuong,donGia,trangThai) VALUES (N'SC120',N'PT120',N'Thay thế theo kết quả kiểm tra',N'Dung dịch vệ sinh kim phun',1,180000,N'CHO_THUC_HIEN');
IF NOT EXISTS (SELECT 1 FROM ChiTietCongViecSuaChua WHERE maPSC=N'SC120' AND stt=1)
 INSERT INTO ChiTietCongViecSuaChua (maPSC,stt,noiDungCongViec,tenKTV,tienCong) VALUES (N'SC120',1,N'Kiểm tra, thay thế và chạy thử',N'Hoàng Đình Kha',200000);
IF NOT EXISTS (SELECT 1 FROM PhieuNhapKho WHERE maPhieuNhap=N'PN103')
 INSERT INTO PhieuNhapKho (maPhieuNhap,maNCC,maNV,ngayNhap,tongTien,trangThai,ngayDuKienGiao,ghiChu) VALUES (N'PN103',N'NCC102',N'NV03',N'2026-10-27',1500000,N'CHO_NHAP',N'2026-10-30',N'Đặt hàng, chưa cộng tồn kho');
IF NOT EXISTS (SELECT 1 FROM ChiTietPhieuNhapKho WHERE maPhieuNhap=N'PN103' AND maPT=N'PT101')
 INSERT INTO ChiTietPhieuNhapKho (maPhieuNhap,maPT,tenPT,soLuong,donGia) VALUES (N'PN103',N'PT101',N'Lọc dầu động cơ',10,150000);
COMMIT TRANSACTION;
GO
