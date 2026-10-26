# Tích hợp PR #17: phiếu bảo hành

Trước khi dùng code mới, chọn đúng database gara trong SQL Server và chạy
database/upgrade_pbh_pr17.sql. Áp dụng cho schema của main trong
QuanLySuaChuaXe.sql (hoặc database/QuanLySuaChuaXe.sql).
Không chạy lại script tạo database trên database đang có dữ liệu.

Migration chạy lại được, giữ bảng/khóa cũ, thêm maNV, ngayLap, ghiChu vào
PhieuBaoHanh và tạo bảng HangMucBaoHanh riêng cho hạng mục tự do.
Các phiếu cũ được gán ngayLap là ngày chạy migration vì schema cũ không lưu ngày lập.
Không đổi bảng ChiTietPhieuBaoHanh đang liên kết phụ tùng.
Xóa phiếu sẽ tự xóa hạng mục mới qua ON DELETE CASCADE.

DAO giữ API của main, ánh xạ ngày/trạng thái/hóa đơn về ngayBD, ngayKT,
trangThaiBH, maHoaDon. Ngày kết thúc phiếu là ngày hết hạn lớn nhất của các hạng mục.
Xe phải thuộc khách hàng trên hóa đơn; nếu hóa đơn có phiếu sửa chữa thì phải đúng xe đó.
Nút in dùng phiếu vừa lưu trong phiên, thay vì mã phiếu mới sau khi làm mới.

Kiểm tra tự động (Java 17, từ thư mục dự án):
- Biên dịch src với classpath lib/* vào build.
- javac --release 17 -encoding UTF-8 -cp "build;lib/*" -d build tests/PhieuBaoHanhMergeTest.java
- java -cp "build;lib/*" PhieuBaoHanhMergeTest

Test dùng JDBC giả lập để kiểm tra commit/rollback, khôi phục autocommit,
ngày hết hạn, trạng thái, hóa đơn trùng và xe không khớp.
Chưa thay thế kiểm thử SQL Server thật. Sau migration, kiểm tra:
1. Tra cứu phiếu cũ và lập phiếu mới có nhiều hạng mục.
2. Kiểm tra phiếu mới xuất hiện ở tra cứu, đọc/in đúng thông tin và trạng thái.
3. Kiểm tra không lập trùng hóa đơn; chọn đúng xe khi khách có nhiều xe.
4. Kiểm tra xóa phiếu xóa các hạng mục tương ứng.