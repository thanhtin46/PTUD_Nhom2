# Dữ liệu và đồng bộ SQL Server

Bản này đối chiếu với SQL trên origin/main và giữ các chỉnh sửa đang có trong dự án. Hai file QuanLySuaChuaXe.sql ở gốc và trong database/ có nội dung giống nhau.

## Chạy trên máy khác

- Database đã có dữ liệu: sao lưu, chọn đúng database trong SSMS, chạy upgrade_dong_bo.sql, rồi seed_demo.sql và verify_dong_bo.sql theo thứ tự.
- Chưa có database: chạy QuanLySuaChuaXe.sql. File này dừng nếu database đã tồn tại, không tự xóa database.
- Biên dịch lại dự án bằng Java 17 trở lên. Driver SQL Server nằm trong lib/.

upgrade_dong_bo.sql bổ sung cột và bảng, không xóa bản ghi. seed_demo.sql chỉ thêm dữ liệu chưa có, không ghi đè mã đã tồn tại. Chạy lại không cộng trùng tồn kho. Mã demo bổ sung bắt đầu từ 101; các bản ghi cũ được giữ nguyên.

## Dữ liệu trên máy đã cập nhật ngày 27/10/2026

| Danh mục | Tổng số |
|---|---:|
| Khách hàng | 23 |
| Phương tiện | 28 |
| Nhân viên | 9 |
| Phụ tùng | 25 |
| Phiếu tiếp nhận | 23 |
| Phiếu sửa chữa | 22 |
| Hóa đơn | 12 |
| Phiếu bảo hành | 8 |
| Phiếu nhập kho | 5 |
| Phiếu xuất kho | 18 |

Khách demo có thể có nhiều ô tô. Có phiếu hoàn thành, chờ tiếp nhận, đã phân công, đang sửa, chờ phụ tùng; phiếu nhập đã nhận và chờ nhận; phiếu xuất đã xuất và chờ xuất. Hóa đơn có tiền công riêng, phụ tùng riêng và ba phương thức thanh toán. Các dữ liệu bổ sung sử dụng ngày demo tháng 10/2026; khi thuyết trình ở thời điểm khác, chọn tháng/năm tương ứng trong báo cáo.

Tài khoản bài tập: tiepTan, suaChua, kho, quanLy, thuNgan; mật khẩu mẫu 123. Các tài khoản bổ sung demo101–demo104 cũng dùng 123. Đăng nhập yêu cầu đọc được dữ liệu SQL, không tự tạo tài khoản giả khi mất kết nối.

## Các thay đổi đồng bộ

- Hóa đơn và chi tiết được lưu trong một transaction. Tiền công nằm ở ChiTietDichVuHoaDon, không gán mã phụ tùng giả.
- Xuất kho trừ tồn. Thanh toán không trừ lần thứ hai; hủy hóa đơn không tự coi phụ tùng đã được trả về kho. Hóa đơn có bảo hành còn hiệu lực không được hủy.
- Phiếu chờ nhập lưu trong SQL và còn sau khi mở lại chương trình. Xác nhận chỉ cộng tồn một lần; hủy phiếu chờ nhập cũng lưu SQL.
- Bộ nhớ được nạp lại khi chuyển màn hình. Các thao tác thêm/xóa chính chỉ sửa bộ nhớ sau khi SQL thành công.
- Hóa đơn lấy đúng xe từ phiếu sửa chữa. PhieuSuaChua có maPTN để lưu liên kết thay vì chỉ suy từ tên mã.
- Trạng thái SQL và tiếng Việt được ánh xạ để màn hình nhận đúng. Thống kê nhận cả mã cũ và nhãn tiếng Việt.
- Khách hàng/xe có lịch sử không bị xóa từng phần. Xóa bảo hành xử lý cả bảng hạng mục trên schema không có cascade.

## Kiểm tra

Đã biên dịch toàn bộ nguồn Java với --release 17 và chạy:

- PhieuBaoHanhMergeTest: transaction giả lập bảo hành.
- SqlSyncIntegrationTest: database SQL Server riêng; đọc DAO, tổng tiền, commit/rollback, hóa đơn trùng, tồn kho, phiếu chờ nhập, xóa bị chặn bởi khóa ngoại, bảo hành lưu/đọc/xóa.
- verify_dong_bo.sql: tổng tiền hóa đơn demo, thời hạn bảo hành, tồn không âm, khóa ngoại và số lượng dữ liệu.
- SqlSyncReadCheck: đối chiếu số lượng SQL thật với cache của ứng dụng, đọc chi tiết hóa đơn và bảo hành.

SqlSyncIntegrationTest chỉ chấp nhận database có tên bắt đầu GaraSyncTest_. Không chạy test ghi trên database dùng thật. Kiểm thử tự động không thay thế việc thao tác mọi nút trên các màn hình Swing.

Có thể cấu hình máy khác qua GARA_DB_URL, GARA_DB_USER, GARA_DB_PASSWORD hoặc Java properties gara.db.url, gara.db.user, gara.db.password. Khi chỉ định URL riêng, ứng dụng không tự chuyển về database mặc định nếu kết nối đó thất bại.

## Sao lưu trước khi áp dụng

Database QuanLySuaChuaXe đã được sao lưu COPY_ONLY và RESTORE VERIFYONLY xác nhận hợp lệ:

C:\Program Files\Microsoft SQL Server\MSSQL17.SQLEXPRESS\MSSQL\Backup\QuanLySuaChuaXe_before_sync_20261027.bak

Không tự restore khi đang có người sử dụng. Bản sao các nguồn trước chỉnh sửa và log kiểm thử nằm trong build/sql-sync-before và build/ (không đưa vào Git).
