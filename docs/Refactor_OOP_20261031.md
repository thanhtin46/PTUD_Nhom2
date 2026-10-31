# Chỉnh sửa OOP phục vụ báo cáo — 31/10/2026

## Những phần đã chỉnh

| Phần | Thay đổi | Ý nghĩa khi vấn đáp |
|---|---|---|
| Lưu phiếu sửa chữa | Form lấy dữ liệu bảng, gọi `SuaChuaService`; `PhieuSuaChua_DAO` thực hiện SQL | Mỗi lớp có trách nhiệm rõ ràng; có thể kiểm thử nghiệp vụ mà không mở form |
| Dữ liệu phiếu | `DuLieuPhieuSuaChua` chứa mã tiếp nhận, KTV, ngày hẹn và các dòng có kiểu cụ thể | Không truyền JTable xuống DAO; bản sao bảo vệ dữ liệu trong lúc lưu |
| Giao dịch | Lưu đầu phiếu, phụ tùng và công việc cùng một transaction | Nếu một bước lỗi thì rollback, tránh phiếu chỉ lưu được một phần |
| Bảo hành | Dùng `List<ChiTietPhieuBaoHanh>` thay cho mảng chi tiết `Object[]` | Tái sử dụng entity sẵn có, dùng getter rõ tên, giảm ép kiểu và nhầm chỉ số |
| Chi tiết dịch vụ | Constructor và setter đều kiểm tra số lượng, đơn giá; tính thành tiền từ hai thuộc tính | Đóng gói bảo vệ dữ liệu, tránh lưu trạng thái có thể tự tính và bị lệch |
| Người thao tác | UI truyền `NhanVien` vào DAO nhân viên và truy vấn lịch sử | DAO không gọi `Main_UI` hay `BaoVeQuyen`; không phụ thuộc form đang mở |
| Bộ nhớ dùng chung | Getter danh sách trả bản sao; có phương thức cập nhật cache rõ ràng | Bên ngoài không thể xóa/thêm phần tử trực tiếp vào danh sách nội bộ |
| Sửa nhân viên | Sửa trên bản nháp, chỉ cập nhật cache sau khi SQL thành công | Lỗi lưu không làm thay đổi đối tượng đang hiển thị; không báo thành công giả |
| Phân quyền | Tên hiển thị nằm trong enum; nút được gắn quyền cụ thể | Đổi nhãn nút không làm thay đổi quyền; không phụ thuộc thứ tự enum |
| Sự kiện Swing | Mỗi nút lưu/khôi phục phân quyền đăng ký listener một lần | Tránh bấm một lần nhưng xử lý hai lần |

Không thêm framework hay thư viện mới. Dùng Java, Swing, JDBC và các lớp có sẵn.

## Luồng lưu phiếu để trình bày

```text
LapPhieuSuaChua_UI
  → tạo DuLieuPhieuSuaChua từ thông tin trên form
  → SuaChuaService.luu(nguoiThucHien, duLieu)
      → kiểm tra quyền lưu và quyền đổi KTV
      → PhieuSuaChua_DAO.luu(...)
          → bắt đầu transaction
          → kiểm tra phân công và phiếu xuất kho
          → lưu đầu phiếu + phụ tùng + tiền công
          → commit; nếu lỗi thì rollback
  → hiển thị kết quả, chuyển kho nếu người dùng có quyền
```

Lưu phiếu sửa chữa không trừ tồn kho. Nghiệp vụ xuất kho vẫn phụ trách việc đó.

## Năm câu hỏi có thể gặp

1. **Tại sao thêm Service?** Để chứa quy tắc như người nào được lưu hoặc đổi người phụ trách, không trộn quy tắc với mã dựng giao diện hay câu SQL.
2. **Tại sao không lưu thuộc tính thành tiền?** Thành tiền bằng số lượng nhân đơn giá. Tính trong getter giúp luôn nhất quán sau khi đổi số lượng hoặc giá.
3. **Trả bản sao danh sách có bảo vệ toàn bộ dữ liệu không?** Chỉ bảo vệ cấu trúc danh sách. Entity trong cache vẫn có thể thay đổi. Riêng dữ liệu gửi lưu phiếu được sao chép các dòng phụ tùng; form sửa nhân viên dùng bản nháp riêng.
4. **Ẩn nút có đủ để phân quyền không?** Không. Còn có kiểm tra tại handler, điều hướng và Service/DAO trong các luồng đã chỉnh. Thuộc tính quyền trên nút chỉ phục vụ hiển thị.
5. **Có cần thêm kế thừa để được xem là OOP không?** Không nên tạo kế thừa khi không có quan hệ “là một”. Đóng gói, phối hợp các đối tượng và phân chia trách nhiệm ở đây có lý do nghiệp vụ rõ ràng.

## Kết quả kiểm tra

- Biên dịch toàn bộ `src` và các lớp trong `tests` bằng JDK 27, chế độ Java 17: đạt.
- `OopRefactorTest`: 31 kiểm tra, đạt; kiểm tra dữ liệu không hợp lệ, bản sao, quyền lưu, rollback, bảo toàn giao dịch đang có và nút đổi nhãn.
- `RoleAccessTest` không bật SQL: 175 kiểm tra quyền, đạt.
- `PhieuBaoHanhMergeTest`: đạt các trường hợp lưu, hạn dài nhất, trạng thái, rollback chi tiết, trùng hóa đơn, sai xe, sai hạn.
- `OopSqlIntegrationTest`: 13 kiểm tra trên `GaraSyncTest_Report_20261028`, đạt. Đã tạo/cập nhật phiếu, gây lỗi khóa ngoại để kiểm tra rollback và xác minh không trừ tồn kho. Các bản ghi thử được xóa sau kiểm tra.
- `OopPermissionFormTest`: 159 kiểm tra Swing, đạt; kiểm tra quyền mặc định và listener. Đã mở form và kiểm tra ảnh hiển thị, không bấm lưu.
- Không chạy toàn bộ nghiệp vụ đầu cuối của ứng dụng trong đợt này. Không ghi dữ liệu vào database gara chính.

## Phạm vi còn có thể cải thiện sau

- Một số truy vấn đọc và thao tác hoàn tất phiếu vẫn nằm trong UI. Đợt này tách trọn nghiệp vụ **lưu phiếu sửa chữa**, không tuyên bố đã chuyển toàn bộ dự án sang ba tầng.
- `KhoDuLieu` vẫn là lớp tương thích cho các màn hình cũ. Chưa tách toàn bộ khách hàng, kho, hóa đơn thành các Service riêng.
- Các DAO cũ còn dùng cách bắt lỗi/trả `false` hoặc danh sách rỗng. Khi mở rộng nên chuẩn hóa cách báo lỗi, nhưng tránh đổi hàng loạt ngay trước buổi báo cáo.
- Kết nối JDBC dùng chung và nhiều thao tác SQL còn chạy trên Swing EDT là hạn chế có sẵn. Chưa thay cơ chế kết nối và xử lý nền của toàn ứng dụng.
- Không cần sửa cấu trúc SQL để sử dụng các chỉnh sửa OOP này; database cần có các bảng và cột nghiệp vụ vốn đã được dự án sử dụng.

## Chạy lại các kiểm tra độc lập

Sau khi biên dịch `src` và `tests` vào cùng thư mục lớp, có thể chạy ba lớp sau mà không cần database:

```powershell
java '-Djava.awt.headless=true' -cp 'build/oop/classes;lib/*' tests.OopRefactorTest
java -cp 'build/oop/classes;lib/*' tests.RoleAccessTest
java -cp 'build/oop/classes;lib/*' tests.PhieuBaoHanhMergeTest
```

Hai kiểm thử có SQL yêu cầu chỉ định URL database kiểm thử và kiểm tra tên database trước khi làm việc; không dùng URL database gara chính.
