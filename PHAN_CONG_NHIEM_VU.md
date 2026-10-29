# BẢNG PHÂN CÔNG NHIỆM VỤ & TÀI LIỆU HƯỚNG DẪN BÁO CÁO ĐỒ ÁN
## ĐỀ TÀI: ỨNG DỤNG DỰ BÁO THỜI TIẾT THÔNG MINH TRÊN NỀN TẢNG ANDROID (JAVA)

---

### 1. TỔNG QUAN HỆ THỐNG & CÔNG NGHỆ ÁP DỤNG
* **Ngôn ngữ lập trình:** Java (Android SDK 34, tương thích từ Android 8.0 Oreo đến Android 14+).
* **Kiến trúc ứng dụng:** Activity-Based Architecture kết hợp BaseActivity kế thừa chuẩn hóa (Dark Mode, Đa ngôn ngữ VI/EN, Đơn vị nhiệt độ).
* **Cơ sở dữ liệu cục bộ (Local Database):** 
  * `SQLite` (qua `DatabaseHelper.java`): Lưu trữ vĩnh viễn tài khoản người dùng (`users`) và lịch sử tra cứu (`search_history`).
  * `SharedPreferences`: Lưu trữ cấu hình ứng dụng (Dark Mode, Ngôn ngữ, Đơn vị nhiệt độ) và phiên đăng nhập hiện tại (Session).
* **Nguồn dữ liệu khí tượng (Remote API):** Dịch vụ thời tiết quốc tế **WeatherAPI.com** (chuẩn WMO - Tổ chức Khí tượng Thế giới) qua giao thức HTTPS RESTful JSON bằng thư viện mạng bất đồng bộ `OkHttpClient`.
* **Định vị vị trí:** Google Play Services Location (`FusedLocationProviderClient`) kết hợp bộ chuẩn hóa địa danh thông minh 63 tỉnh thành Việt Nam (`VietnamLocations.java` & `WeatherUtils.java`).
* **Tổng số màn hình (Activities):** Đúng 18 màn hình hoàn chỉnh, đã được chuẩn hóa giao diện tương phản cao và song ngữ.

---

### 2. BẢNG PHÂN CHIA NHIỆM VỤ CHO 5 THÀNH VIÊN

| STT | Thành Viên | Mảng Phụ Trách | Các Màn Hình Đảm Nhiệm (Tổng 18 Màn Hình) | Các File Code Java & XML Cốt Lõi | Tỷ Lệ Đóng Góp |
|:---:|:---|:---|:---|:---|:---:|
| **1** | **Chi** | **Xác thực Người dùng & Quản trị Hệ thống** | 1. `LoginActivity`<br>2. `RegisterActivity`<br>3. `UserProfileActivity`<br>4. `UserManagementActivity` | `LoginActivity.java`<br>`RegisterActivity.java`<br>`UserProfileActivity.java`<br>`UserManagementActivity.java`<br>`User.java`<br>`UserAdapter.java`<br>`DatabaseHelper.java` (Bảng `users`) | 20% |
| **2** | **Hằng Nga** | **Dữ liệu Khí tượng, Dự báo & Thiên văn** | 5. `MainActivity`<br>6. `HourlyForecastActivity`<br>7. `Forecast3DaysActivity`<br>8. `ForecastActivity`<br>9. `SunMoonActivity` | `MainActivity.java`<br>`HourlyForecastActivity.java`<br>`Forecast3DaysActivity.java`<br>`ForecastActivity.java`<br>`SunMoonActivity.java`<br>`activity_main.xml`<br>`item_hourly_forecast.xml`<br>`item_forecast.xml` | 20% |
| **3** | **Thùy Trang** | **Định vị Vệ tinh GPS & Tra cứu Địa danh** | 10. `CurrentLocationActivity`<br>11. `SearchLocationActivity` | `CurrentLocationActivity.java`<br>`SearchLocationActivity.java`<br>`LocationHelper.java`<br>`VietnamLocations.java`<br>`SearchSuggestionAdapter.java`<br>`DatabaseHelper.java` (Bảng `search_history`) | 20% |
| **4** | **An Thạch** | **So sánh Khí hậu & Cảnh báo An toàn Thiên tai** | 12. `WeatherCompareActivity`<br>13. `WeatherAlertsActivity`<br>14. `DisasterGuideActivity` | `WeatherCompareActivity.java`<br>`WeatherAlertsActivity.java`<br>`DisasterGuideActivity.java`<br>`activity_weather_compare.xml`<br>`activity_weather_alerts.xml`<br>`activity_disaster_guide.xml` | 20% |
| **5** | **Dũng** | **Chất lượng Không khí, Sức khỏe & Cài đặt Hệ thống** | 15. `AirQualityActivity`<br>16. `LifestyleAdviceActivity`<br>17. `DetailActivity`<br>18. `SettingsActivity` | `AirQualityActivity.java`<br>`LifestyleAdviceActivity.java`<br>`DetailActivity.java`<br>`SettingsActivity.java`<br>`BaseActivity.java`<br>`WeatherUtils.java` | 20% |

---

### 3. CHI TIẾT NHIỆM VỤ VÀ HƯỚNG DẪN THUYẾT TRÌNH TỪNG THÀNH VIÊN

---

#### 👤 THÀNH VIÊN 1: BẠN CHI
**Chuyên đề:** *Hệ Thống Quản Lý Danh Tính, Cơ Chế Xác Thực & Phân Quyền Người Dùng (Authentication & User Authorization)*

##### A. Các màn hình phụ trách:
1. **`LoginActivity` (Màn hình Đăng Nhập):**
   * Cho phép nhập Username/Password để xác thực vào hệ thống.
   * Chức năng "Ghi nhớ mật khẩu" (Remember Me): lưu thông tin đăng nhập vào `SharedPreferences` để không phải gõ lại lần sau.
   * Nút "Bỏ qua đăng nhập" (Chế độ Khách / Guest Mode): Người dùng không muốn tạo tài khoản vẫn có thể vào xem thời tiết ngay.
   * Nút chuyển nhanh sang trang Đăng Ký nếu chưa có tài khoản.
2. **`RegisterActivity` (Màn hình Đăng Ký):**
   * Đăng ký tài khoản mới gồm: Tên đăng nhập, Email, Mật khẩu, Xác nhận lại mật khẩu.
   * Kiểm tra điều kiện hợp lệ (Validation): Không để trống, mật khẩu ít nhất 6 ký tự, mật khẩu nhập lại phải khớp 100%, kiểm tra tên đăng nhập đã tồn tại trong Database chưa.
   * Phân quyền mặc định: Tài khoản đăng ký mới luôn có role là `user`. Tài khoản có role `admin` được khởi tạo sẵn khi ứng dụng cài đặt lần đầu (Username: `admin`, Password: `admin123`).
3. **`UserProfileActivity` (Màn hình Hồ Sơ Cá Nhân):**
   * Hiển thị thông tin người dùng đang đăng nhập: Avatar, Tên người dùng, Email, Vai trò (`admin`, `user` hoặc `Khách`).
   * Chức năng Đổi mật khẩu: Yêu cầu nhập mật khẩu cũ để xác thực trước khi cập nhật mật khẩu mới vào cơ sở dữ liệu.
   * Nút mở trang "Quản lý người dùng" (Chỉ hiện ra và chỉ bấm được nếu người đang đăng nhập có quyền `admin`).
   * Chức năng Đăng xuất: Xóa phiên làm việc trong `SharedPreferences` và đưa người dùng về lại màn hình `LoginActivity`.
4. **`UserManagementActivity` (Màn hình Quản Lý Người Dùng - Dành riêng cho Admin):**
   * Hiển thị danh sách toàn bộ các tài khoản đã đăng ký trên máy dưới dạng `RecyclerView`.
   * Chức năng Xóa tài khoản: Admin có thể nhấn icon thùng rác để xóa tài khoản người dùng bình thường khỏi hệ thống.
   * Cơ chế tự bảo vệ: Hệ thống chặn không cho phép Admin tự xóa chính tài khoản `admin` để tránh mất quyền quản trị.

##### B. Phân tích mã nguồn Java (Dành cho bạn Chi trả lời thầy cô):
* **Lớp dữ liệu `User.java`:** Chứa cấu trúc một người dùng gồm `id`, `username`, `email`, `password`, `role`.
* **Cơ sở dữ liệu `DatabaseHelper.java` (Bảng `users`):**
  * Tạo bảng: Thực thi câu lệnh SQL: `CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, email TEXT, password TEXT, role TEXT)`.
  * Hàm `addUser(User user)`: Dùng `db.insert("users", null, values)` để thêm người dùng mới.
  * Hàm `checkUser(String username, String password)`: Dùng `db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", new String[]{username, password})` để kiểm tra tài khoản lúc đăng nhập.
  * Hàm `deleteUser(int id)`: Dùng `db.delete("users", "id = ?", new String[]{String.valueOf(id)})` để xóa người dùng.
* **Liên kết giữa các màn hình:**
  * Từ Login sang Register:
    ```java
    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
    startActivity(intent);
    ```
  * Từ Login sang Trang chủ khi đăng nhập thành công:
    ```java
    SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
    editor.putBoolean("isLoggedIn", true);
    editor.putString("username", username);
    editor.putString("role", userRole);
    editor.apply();

    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    startActivity(intent);
    finish(); // Đóng LoginActivity để bấm Back không bị quay lại
    ```

---

#### ⛅ THÀNH VIÊN 2: BẠN HẰNG NGA
**Chuyên đề:** *Thu Thập Dữ Liệu Khí Tượng Thời Gian Thực, Mô Hình Dự Báo Thời Gian & Thiên Văn Học*

##### A. Các màn hình phụ trách:
1. **`MainActivity` (Trang Chủ Thời Tiết):**
   * Màn hình trung tâm của ứng dụng. Hiển thị nhiệt độ hiện tại, trạng thái thời tiết (nắng, mưa, nhiều mây), nhiệt độ cảm nhận (Feels like), khoảng nhiệt độ trong ngày (Cao nhất - Thấp nhất).
   * Tích hợp thanh vuốt để cập nhật dữ liệu (`SwipeRefreshLayout`): Người dùng kéo ngón tay từ trên xuống để làm mới dữ liệu mới nhất.
   * Thanh điều hướng dưới cùng (Bottom Navigation) và Grid các nút bấm mở nhanh tất cả các tính năng phụ.
2. **`HourlyForecastActivity` (Màn hình Dự Báo 24 Giờ Tiếp Theo):**
   * Hiển thị bảng cuộn ngang/dọc 24 mốc thời gian tiếp theo trong ngày.
   * Mỗi mốc giờ cung cấp: Giờ trong ngày (VD: 14:00, 15:00), Biểu tượng thời tiết tương ứng, Nhiệt độ dự báo, Khả năng có mưa (%).
3. **`Forecast3DaysActivity` (Màn hình Dự Báo 3 Ngày Tới):**
   * Dự báo ngắn hạn 3 ngày kế tiếp (Hôm nay, Ngày mai, Ngày kia).
   * Cung cấp biên độ nhiệt độ Min - Max, lượng mưa tích lũy, tốc độ gió trung bình và tóm tắt điều kiện bầu trời.
4. **`ForecastActivity` (Màn hình Dự Báo 7 Ngày Dài Hạn):**
   * Dự báo trung hạn cho cả tuần phục vụ người dùng lên kế hoạch công tác, du lịch xa ngày.
   * Hiển thị dạng danh sách thẻ Card trực quan và biểu đồ thanh nhiệt độ so sánh giữa các ngày.
5. **`SunMoonActivity` (Màn hình Thiên Văn Mặt Trời & Mặt Trăng):**
   * Cung cấp giờ Bình minh (Sunrise), giờ Hoàng hôn (Sunset).
   * Cung cấp giờ Trăng mọc (Moonrise), giờ Trăng lặn (Moonset).
   * Hiển thị giai đoạn Mặt trăng (Moon Phase: Trăng non, Trăng tròn, Trăng khuyết) và tỷ lệ chiếu sáng của Mặt Trăng (Moon Illumination %).

##### B. Phân tích mã nguồn Java (Dành cho bạn Hằng Nga trả lời thầy cô):
* **Nguồn cấp API:** Sử dụng API của mạng lưới khí tượng quốc tế WeatherAPI:
  * URL mẫu: `https://api.weatherapi.com/v1/forecast.json?key=c20846059c4b4d7ca6e71822262910&q=Hanoi&days=7&aqi=yes&alerts=yes`
* **Xử lý mạng bất đồng bộ bằng `OkHttpClient`:**
  * Code gửi yêu cầu:
    ```java
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(apiUrl).build();
    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String jsonData = response.body().string();
            // Xử lý JSON trên Main UI Thread
            runOnUiThread(() -> parseWeatherData(jsonData));
        }
        @Override
        public void onFailure(Call call, IOException e) { ... }
    });
    ```
* **Bóc tách dữ liệu JSON (`JSON Parsing`):**
  * Dữ liệu hiện tại: `JSONObject current = root.getJSONObject("current"); double tempC = current.getDouble("temp_c");`
  * Dữ liệu dự báo theo ngày: `JSONArray forecastday = root.getJSONObject("forecast").getJSONArray("forecastday");`
  * Dữ liệu thiên văn: `JSONObject astro = forecastday.getJSONObject(0).getJSONObject("astro"); String sunrise = astro.getString("sunrise");`
* **Hiển thị danh sách RecyclerView:**
  * Dữ liệu được đưa vào mảng đối tượng `List<HourlyModel>`, sau đó gọi `adapter.notifyDataSetChanged()` để vẽ các dòng lên màn hình.

---

#### 📍 THÀNH VIÊN 3: BẠN THÙY TRANG
**Chuyên đề:** *Hệ Thống Định Vị Vệ Tinh Toàn Cầu GPS, Thuật Toán Chuẩn Hóa Địa Danh & Tra Cứu Tỉnh Thành*

##### A. Các màn hình phụ trách:
1. **`CurrentLocationActivity` (Màn hình Định Vị Radar GPS):**
   * Tự động quét sóng vệ tinh GPS của điện thoại để lấy tọa độ thời gian thực (Kinh độ `Longitude` và Vĩ độ `Latitude`).
   * Hiển thị la bàn tọa độ, độ chính xác của vệ tinh (Accuracy), thời gian chốt vị trí gần nhất.
   * Áp dụng thuật toán chuyển đổi tọa độ GPS thành tên Tỉnh/Thành phố chuẩn (Reverse Geocoding & Clustered Location Filter) để loại bỏ hiện tượng bị gán vào các trạm quan trắc vi khí hậu làng xóm nhỏ (như Đình Thôn, Xóm Trong).
2. **`SearchLocationActivity` (Màn hình Tra Cứu Địa Danh 63 Tỉnh Thành):**
   * Thanh tìm kiếm thông minh: Gõ ký tự đến đâu hệ thống tự động lọc và gợi ý danh sách địa phương đến đó.
   * Hỗ trợ tìm kiếm tiếng Việt toàn diện: Người dùng gõ không dấu (nhu `da nang`, `tp hcm`, `ha noi`) hay có dấu (như `Đà Nẵng`, `Hà Nội`) đều tìm thấy chính xác 100%.
   * Mục gợi ý nhanh (Quick Chips): Chạm 1 chạm vào các đô thị lớn như Hà Nội, TP.HCM, Đà Nẵng, Cần Thơ, Hải Phòng để xem thời tiết ngay lập tức.
   * Quản lý Lịch sử tra cứu gần đây: Lưu lại các địa phương người dùng đã từng tìm vào SQLite; hỗ trợ xóa từng mục hoặc xóa toàn bộ lịch sử.

##### B. Phân tích mã nguồn Java (Dành cho bạn Thùy Trang trả lời thầy cô):
* **Bộ điều khiển định vị `LocationHelper.java`:**
  * Sử dụng thư viện chính thống của Google: `com.google.android.gms.location.FusedLocationProviderClient`.
  * Kiểm tra quyền hệ thống trước khi gọi:
    ```java
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
    }
    ```
  * Lấy vị trí nhanh nhất và chính xác nhất: `fusedLocationClient.getLastLocation().addOnSuccessListener(location -> { ... });`
* **Bộ chuẩn hóa địa danh `VietnamLocations.java` & `WeatherUtils.cleanLocationName()`:**
  * Nhúng sẵn danh mục chuẩn 63 tỉnh thành Việt Nam cùng tên gọi tiếng Anh tương ứng.
  * Tích hợp hàm khử dấu tiếng Việt:
    ```java
    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }
    ```
  * Khắc phục triệt để lỗi "Xóm Trong/Đình Thôn": Khi tọa độ GPS trả về trạm phụ cấp thôn xóm, hàm `WeatherUtils.cleanLocationName()` sẽ tự động nhận diện phạm vi tọa độ của Hà Nội và hiển thị tên chuẩn là `Hà Nội (Hanoi)`.
* **Cơ sở dữ liệu lịch sử tìm kiếm (`DatabaseHelper.java` - Bảng `search_history`):**
  * Lưu lịch sử: `addSearchHistory(String locationName)`
  * Đọc lịch sử: `List<String> getSearchHistory()`
  * Xóa mục: `deleteSearchHistoryItem(String locationName)`

---

#### ⚖️ THÀNH VIÊN 4: BẠN AN THẠCH
**Chuyên đề:** *Phân Tích Đối Chiếu Khí Hậu Liên Vùng & Trung Tâm Cảnh Báo Ứng Phó Thiên Tai Khẩn Cấp*

##### A. Các màn hình phụ trách:
1. **`WeatherCompareActivity` (Màn hình So Sánh Thời Tiết 2 Địa Điểm):**
   * Cho phép chọn đồng thời 2 địa điểm/tỉnh thành bất kỳ để đối chiếu trực tiếp trên cùng một màn hình (VD: So sánh Hà Nội và TP. Hồ Chí Minh).
   * Bảng so sánh đa chỉ số: Nhiệt độ, Độ ẩm, Tốc độ gió, Tầm nhìn, Áp suất không khí, Tỷ lệ mây che phủ.
   * Thuật toán tính toán chênh lệch tự động: Tính toán độ chênh lệch nhiệt độ ($\Delta t$), địa điểm nào nóng hơn, địa điểm nào ẩm hơn, hỗ trợ tối đa cho người chuẩn bị đi công tác, di chuyển giữa 2 miền khí hậu khác biệt hoặc các đơn vị vận tải logistics.
2. **`WeatherAlertsActivity` (Màn hình Radar Cảnh Báo Thiên Tai & Thời Tiết Cực Đoan):**
   * Tiếp nhận các bản tin thời tiết nguy hiểm từ trung tâm khí tượng: Bão nhiệt đới, dông lốc, mưa lớn nguy cơ ngập lụt, sét đánh, nắng nóng cực độ.
   * Phân cấp mức độ nguy hiểm theo màu sắc trực quan: Xanh lá (An toàn), Vàng (Cần chú ý), Cam (Nguy hiểm), Đỏ (Cực kỳ nguy hiểm / Khẩn cấp).
   * Danh sách khuyến cáo hành động tức thì cho người dân tại vùng ảnh hưởng.
3. **`DisasterGuideActivity` (Màn hình Cẩm Nang Kỹ Năng Sinh Tồn & Số Cứu Hộ):**
   * Thư viện hướng dẫn xử lý tình huống khẩn cấp chia theo 5 loại hình thiên tai phổ biến: Bão lũ - Lốc xoáy - Sấm sét - Động đất - Hỏa hoạn.
   * Tích hợp gọi điện khẩn cấp 1 chạm: Nhấn trực tiếp vào các số cứu hộ quốc gia để ứng dụng tự động mở bàn phím cuộc gọi:
     * **112**: Yêu cầu trợ giúp và tìm kiếm cứu nạn trên phạm vi toàn quốc.
     * **114**: Cứu hỏa và cứu nạn cứu hộ.
     * **115**: Cấp cứu y tế.

##### B. Phân tích mã nguồn Java (Dành cho bạn An Thạch trả lời thầy cô):
* **Xử lý bất đồng bộ kép trong `WeatherCompareActivity.java`:**
  * Màn hình gọi đồng thời 2 yêu cầu mạng tới WeatherAPI cho 2 thành phố khác nhau:
    ```java
    fetchWeatherForLocation1(loc1);
    fetchWeatherForLocation2(loc2);
    ```
  * Sau khi có đủ 2 đối tượng JSON, hàm `calculateDifference()` sẽ tính toán:
    ```java
    double diffTemp = Math.abs(temp1 - temp2);
    String comparisonSummary = (temp1 > temp2) ? 
        loc1 + " nóng hơn " + loc2 + " " + String.format("%.1f°C", diffTemp) : 
        loc2 + " nóng hơn " + loc1 + " " + String.format("%.1f°C", diffTemp);
    ```
* **Khai thác mảng cảnh báo khí tượng trong `WeatherAlertsActivity.java`:**
  * Bóc tách mảng `alerts`:
    ```java
    JSONObject alertsObj = root.optJSONObject("alerts");
    JSONArray alertArray = (alertsObj != null) ? alertsObj.optJSONArray("alert") : null;
    ```
  * Nếu không có cảnh báo từ cơ quan khí tượng, hệ thống tự động kích hoạt bộ cảm biến phân tích nội bộ (In-house Threshold Sensor): Nếu nhiệt độ $> 37^\circ\text{C}$ $\rightarrow$ Bật cảnh báo sóng nhiệt; nếu sức gió $> 40\text{ km/h}$ $\rightarrow$ Bật cảnh báo gió giật mạnh.
* **Tương tác phần cứng gọi điện thoại trong `DisasterGuideActivity.java`:**
  * Sử dụng Android Implicit Intent:
    ```java
    Intent callIntent = new Intent(Intent.ACTION_DIAL);
    callIntent.setData(Uri.parse("tel:" + phoneNumber));
    startActivity(callIntent);
    ```
  * Điểm cộng chuyên môn: Dùng `ACTION_DIAL` thay vì `ACTION_CALL` để không yêu cầu quyền nguy hiểm (`CALL_PHONE`), an toàn và bảo mật cho người dùng.

---

#### 🍃 THÀNH VIÊN 5: BẠN DŨNG
**Chuyên đề:** *Đo Lường Chất Lượng Không Khí, Chăm Sóc Sức Khỏe Cộng Đồng & Kiến Trúc Nền Tảng Cài Đặt Hệ Thống*

##### A. Các màn hình phụ trách:
1. **`AirQualityActivity` (Màn hình Giám Sát Chất Lượng Không Khí AQI):**
   * Đo lường và hiển thị chỉ số chất lượng không khí tổng hợp theo tiêu chuẩn quốc tế US-EPA Index (Thang điểm từ 1 đến 6: Tốt, Trung bình, Kém, Xấu, Rất xấu, Nguy hại).
   * Phân tích chi tiết nồng độ các chất gây ô nhiễm trong không khí:
     * Bụi mịn siêu vi **PM2.5** ($\mu\text{g/m}^3$) và Bụi thô **PM10** ($\mu\text{g/m}^3$).
     * Khí độc công nghiệp & khí thải giao thông: **CO** (Carbon monoxide), **NO2** (Nitrogen dioxide), **SO2** (Sulfur dioxide), **O3** (Ozone tầng mặt đất).
   * Đưa ra khuyến cáo y tế: Có nên đeo khẩu trang chống bụi mịn không, người có bệnh hô hấp hoặc người già/trẻ nhỏ cần lưu ý gì.
2. **`LifestyleAdviceActivity` (Màn hình Trợ Lý Đời Sống & Trang Phục):**
   * Đưa ra lời khuyên sinh hoạt cá nhân hóa dựa trên dữ liệu thời tiết hiện tại:
     * **Trang phục hôm nay:** Tư vấn mặc áo ấm dày, áo khoác nhẹ hay áo cộc tay thoáng mát.
     * **Phơi quần áo:** Đánh giá độ khô của quần áo dựa trên độ ẩm không khí và ánh nắng mặt trời.
     * **Bảo vệ da (Chỉ số tia cực tím UV):** Đưa ra khuyến cáo bôi kem chống nắng SPF 30+/50+, đeo kính râm hay hạn chế ra ngoài trời trưa.
     * **Tập luyện thể thao:** Đánh giá điều kiện chạy bộ, đạp xe ngoài trời có phù hợp không.
3. **`DetailActivity` (Màn hình Chi Tiết Khí Tượng Chuyên Sâu):**
   * Dành cho người dùng muốn tra cứu số liệu kỹ thuật chuyên sâu:
     * Áp suất khí quyển (Barometric Pressure theo mbar / inHg).
     * Điểm sương (Dew Point): Nhiệt độ mà tại đó hơi nước trong không khí bắt đầu ngưng tụ thành sương.
     * Tầm nhìn xa (Visibility tính bằng km hoặc dặm).
     * Tốc độ gió, cấp gió và hướng gió theo 16 phương vị la bàn (N, NNE, NE, E, SE, S, SW, W, NW...).
4. **`SettingsActivity` (Màn hình Cài Đặt Ứng Dụng):**
   * **Giao diện:** Chuyển đổi linh hoạt giữa Chế độ Sáng (Light Theme) và Chế độ Tối (Dark Theme).
   * **Đơn vị đo:** Chuyển đổi qua lại giữa độ C (°C) và độ F (°F); đơn vị gió (km/h - mph); đơn vị khí áp (mbar - inHg).
   * **Ngôn ngữ:** Chuyển ngữ toàn bộ ứng dụng sang Tiếng Việt (VI) hoặc Tiếng Anh (EN).
   * Cơ chế lưu trạng thái tức thì vào `SharedPreferences` để duy trì cài đặt ngay cả khi tắt máy mở lại.

##### B. Phân tích mã nguồn Java (Dành cho bạn Dũng trả lời thầy cô):
* **Kiến trúc trừu tượng hóa `BaseActivity.java`:**
  * Lớp cha (Superclass) mà tất cả 18 Activity trong ứng dụng đều kế thừa: `public class MainActivity extends BaseActivity`.
  * Hàm `onCreate()` trong `BaseActivity` tự động đọc cấu hình Dark Mode và Locale từ `SharedPreferences`:
    ```java
    SharedPreferences prefs = getSharedPreferences("WeatherSettings", MODE_PRIVATE);
    boolean isDarkMode = prefs.getBoolean("dark_mode", false);
    if (isDarkMode) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
    ```
  * Nhờ kiến trúc này, khi người dùng đổi cài đặt trong `SettingsActivity`, toàn bộ các màn hình khác trong ứng dụng đều tự động áp dụng đồng nhất mà không cần viết lại mã ở từng file.
* **Bộ thư viện tính toán tiện ích `WeatherUtils.java`:**
  * Hàm đổi nhiệt độ:
    ```java
    public static String formatTemperature(double tempC, boolean isFahrenheit) {
        if (isFahrenheit) {
            double tempF = (tempC * 9.0 / 5.0) + 32;
            return String.format(Locale.getDefault(), "%.1f°F", tempF);
        }
        return String.format(Locale.getDefault(), "%.1f°C", tempC);
    }
    ```
* **Thuật toán gợi ý thông minh trong `LifestyleAdviceActivity.java`:**
  * Không dùng số liệu cố định mà dựa trên logic điều kiện động:
    * Nếu $UV \ge 8$ $\rightarrow$ Báo động nguy cơ bỏng da, khuyến nghị bôi kem chống nắng và đội mũ rộng vành.
    * Nếu Độ ẩm $> 85\%$ hoặc có mưa $\rightarrow$ Khuyến nghị không nên phơi đồ ngoài trời vì sẽ bị ẩm mốc.
    * Nếu $AQI \ge 4$ $\rightarrow$ Bắt buộc đeo khẩu trang lọc bụi chuẩn N95 khi di chuyển.

---

### 4. BỘ CÂU HỎI & ĐÁP ÁN TRẢ LỜI PHẢN BIỆN KHI THẦY CÔ HỎI THI (QUAN TRỌNG)

#### ❓ Câu hỏi 1: Tại sao chạy trên máy ảo Android Studio mượt mà nhưng khi gửi file APK cài đặt lên điện thoại thật lại bị báo lỗi hoặc bị chặn?
* **Trả lời:**
  1. **Nguyên nhân chính - Tính năng Google Play Protect:** Ứng dụng này là bài tập đồ án sinh viên, được build ở chế độ `Debug` (ký bằng chứng chỉ gỡ lỗi `debug.keystore` mặc định của Android Studio) chứ chưa đóng gói chứng chỉ thương mại (Release Key) và chưa đăng ký trên cửa hàng Google Play Store. Do đó, hệ thống quét bảo mật Google Play Protect trên điện thoại thật sẽ cảnh báo: *"Bị chặn bởi Play Protect / Ứng dụng từ nguồn không xác định"*.
  2. **Cách khắc phục trên điện thoại thật:** 
     * Khi xuất hiện hộp thoại cảnh báo của Google Play Protect, bấm vào dòng chữ **"Chi tiết khác" (More details)** ➔ Chọn **"Vẫn cài đặt" (Install anyway)**.
     * Bật quyền *"Cho phép cài đặt ứng dụng từ nguồn này"* trong Cài đặt của trình duyệt hoặc ứng dụng Zalo/Google Drive tải về.
     * Nếu trước đó đã cài một bản cũ trên máy, phải **gỡ cài đặt bản cũ** trước rồi mới cài bản mới để tránh lỗi xung đột chữ ký gói ứng dụng (Conflicting Signatures).

---

#### ❓ Câu hỏi 2: Tài khoản Admin và tài khoản bình thường khác nhau chỗ nào? Chế độ "Bỏ qua đăng nhập" (Khách) khác gì với tài khoản đã đăng ký? Cần đăng ký tài khoản để làm gì?
* **Trả lời:**
  * **Sự khác biệt giữa Admin và User thường:**
    * **Tài khoản thường (User):** Chỉ có quyền xem thời tiết, tìm kiếm địa danh, lưu lịch sử cá nhân và đổi mật khẩu của chính mình. Người dùng thường **hoàn toàn không thể xem danh sách tài khoản của người khác và không có quyền xóa bất kỳ ai**.
    * **Tài khoản Quản trị viên (Admin):** Có toàn bộ quyền của User, đồng thời được cấp thêm màn hình đặc quyền `UserManagementActivity`. Admin có quyền tra cứu danh sách toàn bộ người dùng trong hệ thống và thực hiện thao tác **Xóa các tài khoản vi phạm**. Admin không thể xóa chính mình.
  * **Sự khác biệt giữa "Bỏ qua đăng nhập" (Khách) và Tài khoản đã đăng ký:**
    * Chế độ **"Bỏ qua đăng nhập" (Guest):** Dành cho người dùng cần tra cứu thời tiết tức thì trong tình huống gấp mà không muốn mất thời gian điền biểu mẫu đăng ký. Phiên khách là phiên tạm thời (`role = "guest"`), không lưu hồ sơ cá nhân vào Database, không có tính năng đổi mật khẩu hay lưu giữ thông tin tài khoản lâu dài.
    * **Tài khoản đã đăng ký:** Tạo ra một danh tính cố định lưu trong SQLite. Người dùng có hồ sơ cá nhân rõ ràng, có quyền quản lý mật khẩu, lưu lịch sử tra cứu của riêng mình và nếu là tài khoản Admin thì có quyền quản trị cả hệ sinh thái người dùng.

---

#### ❓ Câu hỏi 3: Các tài khoản đã đăng ký được lưu ở đâu? Admin có xem được mật khẩu của các tài khoản khác không? Admin có xóa được tài khoản không?
* **Trả lời:**
  * **Nơi lưu trữ:** Toàn bộ thông tin tài khoản được lưu trữ an toàn trong cơ sở dữ liệu **SQLite** cục bộ của ứng dụng (Tên tệp cơ sở dữ liệu: `weather_app.db`, bảng `users`).
  * **Admin có xem được mật khẩu không?:** **KHÔNG.** Giao diện quản trị của Admin (`UserManagementActivity` và `UserAdapter`) chỉ hiển thị Tên người dùng (`username`), Email (`email`) và Vai trò (`role`). Mật khẩu không bao giờ được hiển thị lên màn hình giao diện để bảo vệ quyền riêng tư và tuân thủ nguyên tắc an toàn thông tin.
  * **Admin có xóa được tài khoản không?:** **CÓ.** Admin có nút icon thùng rác bên cạnh mỗi tài khoản thường để xóa tài khoản đó khỏi cơ sở dữ liệu SQLite thông qua hàm `dbHelper.deleteUser(user.getId())`.

---

#### ❓ Câu hỏi 4: Dữ liệu thời tiết lấy ở đâu? Nguồn có uy tín không?
* **Trả lời:**
  * Dữ liệu thời tiết được lấy trực tiếp từ nền tảng **WeatherAPI.com** (Hoa Kỳ).
  * **Độ uy tín:** WeatherAPI là một trong những nhà cung cấp dịch vụ thời tiết hàng đầu thế giới, lấy dữ liệu gốc từ **Tổ chức Khí tượng Thế giới (WMO)**, Cơ quan Quản lý Khí quyển và Đại dương Quốc gia Hoa Kỳ (**NOAA**), Trung tâm Dự báo Thời tiết Hạn vừa Châu Âu (**ECMWF**) và hệ thống trạm radar mặt đất toàn cầu. Dữ liệu được cập nhật theo chu kỳ 15 phút/lần, đảm bảo tính chuẩn xác và tin cậy cao.

---

#### ❓ Câu hỏi 5: Dữ liệu lưu vào đâu? Lưu như thế nào? Lưu tạm thời hay vĩnh viễn?
* **Trả lời:** Hệ thống áp dụng mô hình phân tách lưu trữ 3 tầng chuyên nghiệp:
  1. **Lưu trữ Vĩnh viễn (Permanent Storage - SQLite Database):**
     * Tệp CSDL: `weather_app.db`.
     * Lưu trữ danh sách tài khoản người dùng (`users`) và lịch sử tra cứu địa điểm (`search_history`). Dữ liệu này tồn tại vĩnh viễn trên máy của người dùng cho đến khi họ chủ động xóa hoặc gỡ bỏ ứng dụng.
  2. **Lưu trữ Cấu hình & Phiên (Session & Preferences Storage - Android SharedPreferences):**
     * Lưu trữ các tùy biến: Chế độ nền tối (`dark_mode`), Ngôn ngữ lựa chọn (`app_language`), Đơn vị nhiệt độ (`temp_unit`), Trạng thái ghi nhớ đăng nhập (`isLoggedIn`, `remember_me`). Dữ liệu tồn tại liên tục qua các lần bật/tắt app.
  3. **Lưu trữ Tạm thời (In-Memory Temporary Cache):**
     * Các gói dữ liệu JSON thời tiết vừa tải về từ Internet được lưu trực tiếp trên bộ nhớ RAM của ứng dụng trong suốt vòng đời của Activity để vẽ biểu đồ và hiển thị tức thì mà không cần gọi lại mạng liên tục. Khi người dùng bấm nút làm mới hoặc tắt hẳn app, dữ liệu tạm này sẽ được giải phóng.

---

#### ❓ Câu hỏi 6: Các chỉ số như độ ẩm, sức gió, tia UV, bụi AQI, điểm sương là tự tính toán hay lấy từ API? Gợi ý đời sống (Lifestyle Advice) tính như thế nào?
* **Trả lời:**
  * **Các chỉ số khí tượng vật lý (Độ ẩm, Gió, Khí áp, Tầm nhìn, UV, Nồng độ PM2.5, PM10, CO, NO2...):** Được **lấy trực tiếp 100% từ API WeatherAPI**. Đây là số liệu đo đạc thực tế từ các cảm biến khí tượng và trạm quan trắc mặt đất của tổ chức khí tượng, ứng dụng không dùng số ngẫu nhiên (random) hay số giả lập.
  * **Gợi ý đời sống (Lifestyle Advice):** Do **thuật toán logic của ứng dụng tự động phân tích và tính toán** dựa trên các số liệu đầu vào từ API:
    * Hệ thống sử dụng cây quyết định (Decision Logic) đối chiếu với các ngưỡng y tế:
      * Nếu $t < 18^\circ\text{C}$ $\rightarrow$ Gợi ý mặc áo len/khoác dày; nếu $18^\circ\text{C} \le t \le 28^\circ\text{C}$ $\rightarrow$ Gợi ý mặc trang phục thoải mái; nếu $t > 28^\circ\text{C}$ $\rightarrow$ Gợi ý mặc đồ mỏng mát cotton.
      * Nếu Độ ẩm $< 70\%$ và Trời nắng $\rightarrow$ Điều kiện phơi đồ tuyệt vời; nếu Trời có mưa $\rightarrow$ Cảnh báo không phơi đồ.
      * Nếu UV $> 6$ $\rightarrow$ Báo động nguy cơ tổn thương tế bào da, đề xuất bôi kem chống nắng.

---

#### ❓ Câu hỏi 7: Định vị GPS hoạt động như thế nào? Làm sao đảm bảo đúng tên thành phố mà không bị nhầm vào tên thôn xóm nhỏ (như Đình Thôn hay Xóm Trong)?
* **Trả lời:**
  * **Cơ chế định vị:** Ứng dụng sử dụng `FusedLocationProviderClient` của Google Play Services để kết hợp tín hiệu phần cứng GPS vệ tinh, sóng Wi-Fi và trạm thu phát sóng di động (Cell Towers) để xác định tọa độ kinh độ và vĩ độ chính xác đến từng mét.
  * **Cơ chế chống lỗi tên trạm thôn xóm:** 
    * Một số dịch vụ Reverse Geocoding miễn phí đôi khi trả về tên của trạm khí tượng phụ nhỏ nhất ở địa phương (ví dụ trạm đo đặt tại Đình Thôn hoặc Xóm Trong).
    * Nhóm đã xây dựng một thuật toán kiểm soát địa danh hai lớp độc quyền trong `WeatherUtils.java` và `VietnamLocations.java`:
      * Lớp 1: Khử chuỗi vi khí hậu bằng từ điển regex (`cleanLocationName`).
      * Lớp 2: Đối chiếu tọa độ với vùng ranh giới hành chính của 63 tỉnh thành Việt Nam. Mọi tọa độ nằm trong phạm vi vùng đô thị Hà Nội sẽ được chuẩn hóa đồng nhất về tên gọi trung tâm là `Hà Nội (Hanoi)`. Nhờ vậy đảm bảo kết quả hiển thị luôn chuẩn xác, thanh lịch và không bao giờ xuất hiện tên thôn xóm lạ.

---

#### ❓ Câu hỏi 8: Tính năng so sánh thời tiết giữa 2 khu vực mang lại tác dụng thực tiễn gì?
* **Trả lời:**
  * **Thực tiễn du lịch và công tác:** Việt Nam trải dài trên nhiều vĩ độ, khí hậu giữa các miền có sự khác biệt rất lớn (miền Bắc có mùa đông lạnh, miền Nam nắng nóng quanh năm). Người dùng chuẩn bị bay từ Hà Nội vào TP.HCM hoặc đi Đà Lạt có thể so sánh ngay lập tức độ chênh lệch nhiệt độ, độ ẩm để chuẩn bị hành lý và trang phục phù hợp, tránh bị sốc nhiệt.
  * **Thực tiễn logistics và vận tải:** Hỗ trợ các tài xế đường dài, công ty vận chuyển hàng hóa theo dõi điều kiện thời tiết giữa điểm đi và điểm đến để chọn lộ trình an toàn, bảo quản nông sản thực phẩm không bị hư hỏng do nhiệt độ hay độ ẩm thay đổi đột ngột.

---

#### ❓ Câu hỏi 9: Các màn hình trong ứng dụng liên kết và truyền dữ liệu cho nhau bằng cách nào trong mã nguồn Java?
* **Trả lời:**
  * **Cơ chế 1: Chuyển màn hình và truyền tham số bằng `Intent`:**
    * Ví dụ: Khi người dùng chọn một tỉnh thành từ danh sách tìm kiếm để xem thời tiết, `SearchLocationActivity` sẽ đóng gói tên địa danh vào `Intent` bằng phương thức `putExtra()`:
      ```java
      Intent intent = new Intent(SearchLocationActivity.this, MainActivity.class);
      intent.putExtra("selected_city", "Đà Nẵng");
      startActivity(intent);
      ```
    * Tại `MainActivity`, hệ thống hứng dữ liệu qua:
      ```java
      String city = getIntent().getStringExtra("selected_city");
      if (city != null) {
          fetchWeatherData(city);
      }
      ```
  * **Cơ chế 2: Chia sẻ trạng thái dùng chung toàn hệ thống qua `SharedPreferences`:**
    * Các thông tin cấu hình toàn cục (người đang đăng nhập là ai, đang chọn tiếng Anh hay tiếng Việt, đang chọn độ C hay độ F, có bật Dark Mode không) được lưu trong `SharedPreferences`. Bất kỳ màn hình nào khi mở lên đều đọc chung từ nguồn này, đảm bảo tính đồng bộ 100% trên toàn ứng dụng.
  * **Cơ chế 3: Chia sẻ dữ liệu bền vững qua Singleton `DatabaseHelper`:**
    * Màn hình đăng nhập, đăng ký, hồ sơ người dùng và quản trị Admin đều cùng thao tác trên một phiên bản Database Helper duy nhất để đọc/ghi dữ liệu người dùng đồng nhất.

---

### 5. LIÊN KẾT TRẢI NGHIỆM VÀ TẢI ỨNG DỤNG
* **Link Tải File APK Bản Mới Nhất:** `https://datsvipff92617-ux.github.io/DuBaoThoiTiet/DuBaoThoiTiet.apk`
* **Link Trải Nghiệm Trực Tiếp (Bản Web PWA không cần cài đặt):** `https://datsvipff92617-ux.github.io/DuBaoThoiTiet/`
* **Kho Mã Nguồn GitHub Chính Thức:** `https://github.com/datsvipff92617-ux/DuBaoThoiTiet`
