# HỆ THỐNG QUẢN LÝ GARA Ô TÔ (THE T4 AUTO)
> **Đồ án học phần:** Phát triển ứng dụng (PTUD)  
> **Công nghệ sử dụng:** Java Core (JDK 17+), Java Swing Desktop, JDBC, Microsoft SQL Server.  
> **Kiến trúc:** Phân tầng Model / DAO / UI, tuân thủ Lập trình hướng đối tượng (OOP) và chuẩn thời gian `java.time` (`LocalDate`, `LocalDateTime`).

---

## 1. Yêu cầu môi trường cài đặt (Prerequisites)
1. **Java Development Kit (JDK):** JDK 17 hoặc JDK 21 trở lên.
2. **Hệ quản trị CSDL:** Microsoft SQL Server (2016, 2017, 2019, 2022 hoặc bản SQL Server Express).
3. **Môi trường phát triển (IDE):** IntelliJ IDEA, Eclipse hoặc Apache NetBeans.

---

## 2. Hướng dẫn thiết lập Cơ sở dữ liệu (Database Setup)
File script CSDL đã được tối ưu hóa toàn diện, hỗ trợ Collation `Vietnamese_CI_AS` và chuẩn mã hóa UTF-8: [`QuanLySuaChuaXe.sql`](QuanLySuaChuaXe.sql).

### Cách 1 (Khuyên dùng - Nhanh nhất 1 click): Sử dụng công cụ `NapDuLieu_UTF8.bat`
- Nhấp đúp chuột vào file **`NapDuLieu_UTF8.bat`** tại thư mục gốc của dự án.
- Công cụ sẽ **tự động dò tìm** SQL Server đang chạy trên máy (hỗ trợ `localhost\SQLEXPRESS`, `localhost`, `.\SQLEXPRESS`, `.` và Windows Authentication / tài khoản `sa`).
- Tự động nạp toàn bộ cấu trúc 22 bảng và bộ dữ liệu mẫu nghiệp vụ chuẩn 100% tiếng Việt mà không lo lỗi font chữ hay sai lệch phiên bản.

### Cách 2: Chạy trực tiếp từ SQL Server Management Studio (SSMS)
1. Mở **SQL Server Management Studio (SSMS)** và kết nối tới SQL Server của bạn.
2. Mở file **`QuanLySuaChuaXe.sql`** trong thư mục đồ án.
3. Nhấn **Execute** (hoặc phím `F5`) để thực thi toàn bộ script.
   - Script sẽ tự động tạo cơ sở dữ liệu `QuanLySuaChuaXe` với Collation `Vietnamese_CI_AS`.
   - Tạo toàn bộ cấu trúc 22 bảng, khóa chính, khóa ngoại, ràng buộc toàn vẹn dữ liệu.
   - Nạp đầy đủ dữ liệu mẫu chuẩn nghiệp vụ: hồ sơ khách hàng, danh mục phương tiện, kho vật tư phụ tùng, các phiếu tiếp nhận, lệnh sửa chữa, phiếu xuất kho, hóa đơn dịch vụ và phiếu bảo hành.

> **Lưu ý về cấu hình kết nối CSDL:**  
> Hệ thống [`ConnectDB`](src/ConnectDB/ConnectDB.java) đã tích hợp cơ chế tự động dò tìm cấu hình:
> - Hỗ trợ cả cổng mặc định `localhost:1433`, phiên bản `localhost\SQLEXPRESS` và IP `127.0.0.1:1433`.
> - Tự động thử các mật khẩu phổ biến của tài khoản `sa`: `sapassword123@`, `sapassword`, `123456`, `123`, `sa`, `12345678`, `admin`, `root`, mật khẩu rỗng và chế độ **Windows Authentication** (`integratedSecurity=true`).
> - Nếu SQL Server của bạn dùng tài khoản/mật khẩu riêng, có thể truyền tham số dòng lệnh khi chạy:  
>   `-Dgara.db.user=sa -Dgara.db.password=<mat_khau_cua_ban>`

---

## 3. Hướng dẫn khởi chạy ứng dụng (How to Run)

### Cách 1: Khởi chạy từ IntelliJ IDEA / Eclipse
1. Mở dự án trong IDE bằng cách chọn thư mục `HeThongQuanLyGaraOto (1)`.
2. Kiểm tra Project SDK đã chọn đúng JDK 17+.
3. Mở file **`src/UI/Login_UI.java`** và nhấn nút **Run** (phím tắt `Shift + F10` trên IntelliJ hoặc `Ctrl + F11` trên Eclipse).

### Cách 2: Khởi chạy bằng dòng lệnh (Terminal / Command Prompt)
Từ thư mục gốc của dự án:
```bash
# 1. Biên dịch toàn bộ mã nguồn
javac -encoding UTF-8 -cp "lib/*;bin" -d bin src/ConnectDB/*.java src/DAO/*.java src/Entity/*.java src/Service/*.java src/UI/*.java src/module-info.java

# 2. Khởi chạy ứng dụng
java -cp "bin;lib/*" UI.Login_UI
```

---

## 4. Danh sách tài khoản đăng nhập thử nghiệm (Test Accounts)

Tất cả tài khoản hệ thống đều dùng mật khẩu mặc định là: **`123`**

| Vai trò / Bộ phận | Tên đăng nhập | Mã nhân viên | Quyền hạn & Chức năng chính |
| :--- | :--- | :--- | :--- |
| **Quản lý tổng** | `quanLy` | `NV04` | Toàn quyền quản trị, điều hành gara, xem báo cáo doanh thu KPI, phân quyền nhân sự |
| **Nhân viên tiếp tân** | `tiepTan` | `NV01` | Tiếp nhận xe, lập phiếu hẹn, quản lý thông tin khách hàng và tra cứu lịch sử sửa chữa |
| **Kỹ thuật viên sửa chữa** | `suaChua` | `NV02` | Tiếp nhận lệnh sửa chữa, phân công kỹ thuật, kê khai tiền công, lập phiếu bảo hành |
| **Thủ kho phụ tùng** | `kho` | `NV03` | Quản lý danh mục vật tư, cấp phát phụ tùng xuất kho cho xưởng, lập phiếu nhập kho |
| **Thu ngân** | `thuNgan` | `NV05` | Thu tiền xe hoàn thành, bán lẻ phụ tùng tại quầy, xuất hóa đơn và in biên lai thanh toán |

*(Bạn có thể đăng nhập bằng **Tên đăng nhập** hoặc **Mã nhân viên** tương ứng).*

---

## 5. Cấu trúc thư mục dự án
```text
HeThongQuanLyGaraOto/
├── QuanLySuaChuaXe.sql       # Script CSDL đầy đủ (Schema & dữ liệu mẫu)
├── README.md                 # Hướng dẫn chi tiết dành cho Giảng viên
├── .classpath & .project     # File cấu hình chuẩn dành cho Eclipse
├── HeThongQuanLyGaraOto.iml  # File cấu hình chuẩn dành cho IntelliJ IDEA
├── lib/                      # Thư viện Driver JDBC kết nối SQL Server
│   └── mssql-jdbc-12.8.1.jre11.jar
└── src/                      # Mã nguồn ứng dụng
    ├── ConnectDB/            # Module kết nối CSDL SQL Server (Singleton)
    ├── Entity/               # Tầng thực thể (OOP Association, java.time)
    ├── DAO/                  # Tầng truy vấn dữ liệu (PreparedStatement, JOIN)
    └── UI/                   # Giao diện người dùng Java Swing hiện đại
        ├── Login_UI.java     # ĐIỂM BẮT ĐẦU CHƯƠNG TRÌNH & Màn hình đăng nhập hệ thống (Main entry point)
        ├── Main_UI.java      # Khung điều hướng trung tâm
        └── ...               # Giao diện các phân hệ chuyên trách
```
