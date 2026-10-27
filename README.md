# 🌦️ Ứng Dụng Dự Báo Thời Tiết (DuBaoThoiTiet - Android)

Ứng dụng dự báo thời tiết chuyên nghiệp trên hệ điều hành Android, cung cấp thông tin khí tượng thời gian thực, dự báo nhiều ngày, chỉ số môi trường, tự động định vị GPS thông minh và hỗ trợ giao diện Sáng / Tối (Dark / Light Theme).

---

## 📱 1. HƯỚNG DẪN TẢI APP VỀ ĐIỆN THOẠI ANDROID

Bạn có thể tải trực tiếp file cài đặt APK về điện thoại thông minh Android qua các đường dẫn bên dưới:

* **👉 Link Tải Bản Cập Nhật Mới Nhất (GitHub Releases v2.1):**  
  [https://github.com/datsvipff92617-ux/DuBaoThoiTiet/releases/download/v2.1/DuBaoThoiTiet.apk](https://github.com/datsvipff92617-ux/DuBaoThoiTiet/releases/download/v2.1/DuBaoThoiTiet.apk)
* **👉 Link Trang Releases GitHub:**  
  [https://github.com/datsvipff92617-ux/DuBaoThoiTiet/releases](https://github.com/datsvipff92617-ux/DuBaoThoiTiet/releases)
* **👉 Link Tải Từ GitHub Actions (Artifacts):**  
  [https://github.com/datsvipff92617-ux/DuBaoThoiTiet/actions](https://github.com/datsvipff92617-ux/DuBaoThoiTiet/actions)

> **Hướng dẫn cài đặt trên điện thoại:**
> 1. Dùng trình duyệt trên điện thoại (Chrome / Cốc Cốc / Samsung Internet) mở một trong các liên kết trên và tải file `DuBaoThoiTiet.apk`.
> 2. Mở file APK vừa tải về, chọn **Cài đặt**.
> 3. Nếu điện thoại thông báo *"Cho phép cài đặt ứng dụng từ nguồn này"*, hãy gạt bật nút Cho phép để hoàn tất cài đặt.

---

## 💻 2. HƯỚNG DẪN CHẠY TRÊN MÁY TÍNH (PC / LAPTOP)

Bạn có thể chạy và trải nghiệm ứng dụng trên máy tính bằng một trong các cách sau:

### Cách A: Chạy Trực Tiếp Trên Trình Duyệt Web (Không Cần Cài Đặt Gì)
1. Tải file `DuBaoThoiTiet.apk` về máy tính.
2. Truy cập trang web giả lập đám mây: [https://appetize.io/upload](https://appetize.io/upload)
3. Kéo thả file `DuBaoThoiTiet.apk` lên trang web.
4. Bấm **Play** để tương tác và sử dụng ứng dụng ngay trong cửa sổ trình duyệt (Chrome, Edge...).

### Cách B: Mở và Chạy Bằng Android Studio
1. Mở **Android Studio** trên máy tính.
2. Chọn **File** -> **New** -> **Project from Version Control...**
3. Dán đường dẫn Git Repo:
   ```
   https://github.com/datsvipff92617-ux/DuBaoThoiTiet.git
   ```
4. Đợi Android Studio đồng bộ Gradle xong, chọn thiết bị giả lập (Emulator) hoặc cắm điện thoại qua cáp USB và nhấn nút **Run 'app' (phím tắt Shift + F10)**.

### Cách C: Chạy Bằng Các Trình Giả Lập Android Trên PC
1. Cài đặt một trong các phần mềm giả lập phổ biến: BlueStacks, LDPlayer, NoxPlayer.
2. Kéo thả file `DuBaoThoiTiet.apk` vào cửa sổ giả lập để cài đặt và mở app.

---

## 🔑 3. TÀI KHOẢN ĐĂNG NHẬP NHANH (TESTING CREDENTIALS)

Tại màn hình Đăng Nhập, ứng dụng đã tích hợp sẵn 2 nút bấm **Đăng nhập 1 chạm** cực kỳ tiện lợi:

| Loại Tài Khoản | Nút Bấm Nhanh | Email | Mật Khẩu | Quyền Hạn |
| :--- | :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | 👑 Đăng nhập Admin | `admin@gmail.com` | `admin123` | Toàn quyền quản trị người dùng, thống kê hệ thống |
| **Học viên / Người dùng** | 🎓 Đăng nhập Student | `student@weather.vn`| `123456` | Người dùng trải nghiệm tất cả tính năng dự báo thời tiết |

*(Ngoài ra bạn có thể bấm **Đăng ký** để tạo tài khoản cá nhân mới với bất kỳ email nào).*

---

## ✨ 4. CÁC TÍNH NĂNG VÀ CẢI TIẾN MỚI NHẤT

1. **🛰️ Định vị GPS Tự Động Chuẩn Xác:**
   * Tự động nhận diện khu vực người dùng đang ở theo hệ phân cấp: `[Khu phố/Thôn/Xã/Phường] + [Quận/Huyện] + [Tỉnh/Thành phố]` (ví dụ: *Triều Khúc, Thanh Xuân, Hà Nội*).
   * **Smart Emulator Fallback:** Tự động phát hiện khi chạy trên máy ảo Android để chuyển sang vị trí mạng Internet thực tế tại Việt Nam, không bao giờ bị văng sang California.
2. **🌗 Giao Diện Sáng / Tối Đồng Bộ Toàn App (Dark & Light Theme):**
   * Cho phép chuyển đổi giao diện trong phần Cài đặt.
   * Toàn bộ 16 màn hình của ứng dụng lập tức chuyển đổi đồng bộ tone màu sang Midnight Slate hoặc Sky Blue sang trọng.
3. **🔍 Tìm Kiếm Khu Vực Trực Quan, Độ Tương Phản Cao:**
   * Khắc phục triệt để hiện tượng lóa trắng thanh tìm kiếm và khung gợi ý.
   * Chữ hiển thị đậm, rõ nét, hỗ trợ tìm kiếm nhanh 63 tỉnh thành và các quận huyện trên cả nước.
