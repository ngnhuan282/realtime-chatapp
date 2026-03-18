# 💬 Emerald Chat - Realtime Chat Application

Emerald Chat là một hệ thống nhắn tin đa nhiệm được xây dựng trên nền tảng Java, sử dụng kiến trúc Client-Server hiện đại. Dự án kết hợp sức mạnh của **Spring Boot** ở phía Backend và sự linh hoạt của **Android (Java)** ở phía Frontend để mang lại trải nghiệm nhắn tin ổn định và mượt mà.

---

## 🚀 I. Sơ lược chức năng

Hệ thống được thiết kế theo mô hình **MVC/MVVM** với các tính năng cốt lõi sau:

### 1. Xác thực người dùng (Authentication)

- **Đăng nhập:** Truy cập hệ thống an toàn bằng tài khoản cá nhân.
- **Quản lý phiên:** Duy trì trạng thái đăng nhập, tránh việc phải nhập lại mật khẩu nhiều lần.

### 2. Nhắn tin Realtime

- **Chat 1-1:** Trò chuyện riêng tư giữa hai người dùng dựa trên dữ liệu từ Database.
- **Chat nhóm:** Tạo và tham gia các nhóm thảo luận chung (Gia đình, Công ty, v.v.).
- **Trạng thái tin nhắn:** Hiển thị trạng thái tin nhắn trực quan (`Sent`, `Delivered`, `Read`).

### 3. Đính kèm đa phương tiện (Media Attachments)

- **Hình ảnh & Video:** Gửi và hiển thị hình ảnh/video trực tiếp trong cuộc hội thoại.
- **Vị trí (Location):** Chia sẻ tọa độ vị trí thực tế thông qua Google Maps tích hợp.
- **Emoji:** Tích hợp bộ biểu cảm phong phú qua menu đính kèm hiện đại.

### 4. Giao diện & Trải nghiệm (UI/UX)

- **Thiết kế hiện đại:** Sử dụng Material Design với tông màu xanh Emerald chủ đạo.
- **Bottom Sheet Menu:** Menu đính kèm tinh tế, tối ưu hóa cho thao tác một tay.
- **Dark Mode:** Hỗ trợ giao diện tối giúp bảo vệ mắt và tiết kiệm pin.

---

## 🛠 II. Hướng dẫn cài đặt

Dự án gồm hai phần: `chat-server` (Backend) và `android-client` (Frontend).

### 1. Cấu hình Backend (ChatServer)

**Yêu cầu:** JDK 17+, MySQL 8.0+.

#### Bước A: Thiết lập Database (MySQL)

Bạn có thể chọn một trong hai phương thức:

**Cách 1: Sử dụng Docker (Khuyên dùng)**

Mở Terminal tại thư mục gốc và chạy:

```bash
docker-compose up -d
```

> Dữ liệu mẫu từ `db.sql` sẽ tự động được khởi tạo vào container.

**Cách 2: Cài đặt MySQL thủ công**

1. Tạo database mới:
   ```sql
   CREATE DATABASE chat_app;
   ```
2. Thực thi script từ file `./database/db.sql` để tạo cấu trúc bảng và 40 dòng dữ liệu mẫu.

#### Bước B: Chạy Spring Boot

1. Mở thư mục `chat-server` bằng **IntelliJ IDEA**.
2. Cập nhật file `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chat_app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=root
spring.datasource.password=123456   # Thay bằng password của bạn
```

3. Chạy file `ChatServerApplication.java`. Backend sẽ sẵn sàng tại cổng **`8080`**.

---

### 2. Cấu hình Android Client (ChatApp)

**Yêu cầu:** Android Studio Jellyfish+, SDK 34.

#### Bước A: Cấu hình kết nối API

Mở file `com.example.chatapp.network.rest.ApiClient.java` và kiểm tra địa chỉ IP:

| Môi trường | Địa chỉ |
|---|---|
| Emulator | `http://10.0.2.2:8080/` |
| Điện thoại thật | `http://<IP-máy-tính>:8080/` (VD: `http://192.168.1.5:8080/`) |

#### Bước B: Cấu hình Manifest

Đảm bảo file `AndroidManifest.xml` đã có các khai báo sau:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:usesCleartextTraffic="true"
    ...>
```

#### Bước C: Đăng nhập thử nghiệm

Sử dụng các tài khoản mẫu đã được insert sẵn trong Database:

| Username | Password |
|---|---|
| `loopy` | `123456` |
| `alice` | `123456` |