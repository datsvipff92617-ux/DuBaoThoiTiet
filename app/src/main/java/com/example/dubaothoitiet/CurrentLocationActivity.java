package com.example.dubaothoitiet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Màn hình Thời Tiết Khu Vực Người Dùng Đang Sống (Member 3)
 * Tự động định vị vị trí qua GPS/Network và hỗ trợ chọn nhanh địa phương thực tế tại Việt Nam
 */
public class CurrentLocationActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String PREF_HOME_CITY = "home_living_city";

    private ImageButton btnBackCurrentLoc;
    private TextView tvGpsStatus;
    private TextView tvCurrentLocCity, tvCurrentLocCoords, tvCurrentLocTemp, tvCurrentLocCondition, tvCurrentLocFeelsLike;
    private TextView tvCurrentLocHumidity, tvCurrentLocWind, tvCurrentLocUV, tvCurrentLocPressure, tvCurrentLocVisibility, tvCurrentLocCloud;
    private Button btnRefreshLocation;

    private Button chipLocHanoi, chipLocHcm, chipLocDanang, chipLocDalat, chipLocHaiphong, chipLocCantho;

    private LocationManager locationManager;
    private final String API_KEY = "35d141f7fec24768b87114757252510";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);

        initViews();
        setupListeners();
        setupCityChips();
        checkAndRequestLocation();
    }

    private void initViews() {
        btnBackCurrentLoc = findViewById(R.id.btnBackCurrentLoc);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        tvCurrentLocCity = findViewById(R.id.tvCurrentLocCity);
        tvCurrentLocCoords = findViewById(R.id.tvCurrentLocCoords);
        tvCurrentLocTemp = findViewById(R.id.tvCurrentLocTemp);
        tvCurrentLocCondition = findViewById(R.id.tvCurrentLocCondition);
        tvCurrentLocFeelsLike = findViewById(R.id.tvCurrentLocFeelsLike);

        tvCurrentLocHumidity = findViewById(R.id.tvCurrentLocHumidity);
        tvCurrentLocWind = findViewById(R.id.tvCurrentLocWind);
        tvCurrentLocUV = findViewById(R.id.tvCurrentLocUV);
        tvCurrentLocPressure = findViewById(R.id.tvCurrentLocPressure);
        tvCurrentLocVisibility = findViewById(R.id.tvCurrentLocVisibility);
        tvCurrentLocCloud = findViewById(R.id.tvCurrentLocCloud);

        btnRefreshLocation = findViewById(R.id.btnRefreshLocation);

        chipLocHanoi = findViewById(R.id.chipLocHanoi);
        chipLocHcm = findViewById(R.id.chipLocHcm);
        chipLocDanang = findViewById(R.id.chipLocDanang);
        chipLocDalat = findViewById(R.id.chipLocDalat);
        chipLocHaiphong = findViewById(R.id.chipLocHaiphong);
        chipLocCantho = findViewById(R.id.chipLocCantho);
    }

    private void setupListeners() {
        btnBackCurrentLoc.setOnClickListener(v -> finish());
        btnRefreshLocation.setOnClickListener(v -> checkAndRequestLocation());
    }

    private void setupCityChips() {
        if (chipLocHanoi != null) chipLocHanoi.setOnClickListener(v -> selectLivingCity("Hà Nội"));
        if (chipLocHcm != null) chipLocHcm.setOnClickListener(v -> selectLivingCity("TP. Hồ Chí Minh"));
        if (chipLocDanang != null) chipLocDanang.setOnClickListener(v -> selectLivingCity("Đà Nẵng"));
        if (chipLocDalat != null) chipLocDalat.setOnClickListener(v -> selectLivingCity("Đà Lạt"));
        if (chipLocHaiphong != null) chipLocHaiphong.setOnClickListener(v -> selectLivingCity("Hải Phòng"));
        if (chipLocCantho != null) chipLocCantho.setOnClickListener(v -> selectLivingCity("Cần Thơ"));
    }

    private void selectLivingCity(String cityName) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_HOME_CITY, cityName).apply();

        tvGpsStatus.setText("📍 Vị trí bạn chọn: " + cityName);
        tvCurrentLocCoords.setText("Đang tải dữ liệu thời tiết thực tế...");
        loadWeatherByCityName(cityName);
    }

    private void checkAndRequestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchDeviceLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchDeviceLocation();
            } else {
                tvGpsStatus.setText("⚠️ Chưa cấp quyền GPS. Đang sử dụng khu vực sinh sống tại Việt Nam.");
                loadFallbackLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchDeviceLocation() {
        tvGpsStatus.setText("🛰️ Đang quét tín hiệu định vị vệ tinh GPS...");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            loadFallbackLocation();
            return;
        }

        Location bestLocation = null;
        try {
            Location gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location passiveLoc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (gpsLoc != null) bestLocation = gpsLoc;
            else if (netLoc != null) bestLocation = netLoc;
            else bestLocation = passiveLoc;
        } catch (Exception e) {
            Log.e("CurrentLocation", "Error getting location: " + e.getMessage());
        }

        if (bestLocation != null) {
            handleLocationResult(bestLocation);
        } else {
            try {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        handleLocationResult(location);
                        locationManager.removeUpdates(this);
                    }
                };

                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                } else {
                    loadFallbackLocation();
                }
            } catch (Exception e) {
                loadFallbackLocation();
            }
        }
    }

    private void handleLocationResult(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        // Kiểm tra xem tọa độ có thuộc lãnh thổ Việt Nam không
        if (WeatherUtils.isCoordinatesInVietnam(lat, lon)) {
            tvGpsStatus.setText("🟢 Đã định vị thành công qua GPS vệ tinh!");
            tvCurrentLocCoords.setText(String.format(Locale.getDefault(), "Tọa độ: %.4f°N, %.4f°E (Việt Nam)", lat, lon));
            loadWeatherByCoordinates(lat, lon);
        } else {
            // Máy ảo / Emulator (Appetize / Android Studio) thường có tọa độ US
            tvGpsStatus.setText("💡 Máy ảo/Giả lập (Tọa độ Quốc tế). Tự động dùng vị trí tại Việt Nam.");
            loadFallbackLocation();
        }
    }

    private void loadFallbackLocation() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String savedHomeCity = prefs.getString(PREF_HOME_CITY, "");

        if (!savedHomeCity.isEmpty()) {
            tvCurrentLocCoords.setText("Khu vực sinh sống: " + savedHomeCity);
            loadWeatherByCityName(savedHomeCity);
            return;
        }

        String username = prefs.getString("username", "sinhvien");
        DatabaseHelper db = new DatabaseHelper(this);
        User user = db.getUserByUsername(username);

        String targetLocation = (user != null && user.getLocation() != null && !user.getLocation().trim().isEmpty())
                ? user.getLocation() : "Hà Nội";

        tvCurrentLocCoords.setText("Khu vực mặc định: " + targetLocation);
        loadWeatherByCityName(targetLocation);
    }

    private void loadWeatherByCoordinates(double lat, double lon) {
        String query = lat + "," + lon;
        callWeatherApi(query);
    }

    private void loadWeatherByCityName(String cityName) {
        String encoded = WeatherUtils.encodeQuery(cityName);
        callWeatherApi(encoded);
    }

    private void callWeatherApi(String query) {
        String url = "http://api.weatherapi.com/v1/current.json?key=" + API_KEY +
                "&q=" + query + "&aqi=no&lang=vi";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CurrentLocationActivity.this, "Lỗi kết nối API", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    runOnUiThread(() -> parseCurrentWeatherData(json));
                }
            }
        });
    }

    private void parseCurrentWeatherData(String jsonData) {
        try {
            JSONObject json = new JSONObject(jsonData);
            JSONObject loc = json.getJSONObject("location");
            String name = loc.getString("name");
            String region = loc.optString("region", "");
            String country = loc.getString("country");

            // Hiển thị tên đẹp tiếng Việt nếu là Ho Chi Minh City
            if (name.equalsIgnoreCase("Ho Chi Minh City")) {
                name = "TP. Hồ Chí Minh";
            } else if (name.equalsIgnoreCase("Ha Noi")) {
                name = "Hà Nội";
            } else if (name.equalsIgnoreCase("Da Nang")) {
                name = "Đà Nẵng";
            } else if (name.equalsIgnoreCase("Da Lat")) {
                name = "Đà Lạt";
            }

            String fullLocation = name;
            if (!region.isEmpty()) fullLocation += ", " + region;
            fullLocation += " (" + country + ")";
            tvCurrentLocCity.setText(fullLocation);

            JSONObject current = json.getJSONObject("current");
            double tempC = current.getDouble("temp_c");
            double feelsLike = current.getDouble("feelslike_c");
            int humidity = current.getInt("humidity");
            double windKph = current.getDouble("wind_kph");
            double pressureMb = current.getDouble("pressure_mb");
            double uv = current.getDouble("uv");
            double visKm = current.getDouble("vis_km");
            int cloud = current.getInt("cloud");

            String conditionText = current.getJSONObject("condition").getString("text");

            tvCurrentLocTemp.setText(String.format(Locale.getDefault(), "%.0f°C", tempC));
            tvCurrentLocCondition.setText("🌥️ " + conditionText);
            tvCurrentLocFeelsLike.setText(String.format(Locale.getDefault(), "Cảm giác thực tế: %.0f°C", feelsLike));

            tvCurrentLocHumidity.setText("💧 Độ ẩm: " + humidity + "%");
            tvCurrentLocWind.setText(String.format(Locale.getDefault(), "💨 Gió: %.1f km/h", windKph));
            tvCurrentLocUV.setText(String.format(Locale.getDefault(), "☀️ Chỉ số UV: %.0f", uv));
            tvCurrentLocPressure.setText(String.format(Locale.getDefault(), "⏱️ Áp suất: %.0f mb", pressureMb));
            tvCurrentLocVisibility.setText(String.format(Locale.getDefault(), "👁️ Tầm nhìn: %.1f km", visKm));
            tvCurrentLocCloud.setText("☁️ Độ che phủ: " + cloud + "%");

            Toast.makeText(this, "Đã cập nhật vị trí & thời tiết thành công!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("CurrentLocation", "Lỗi phân tích: " + e.getMessage());
        }
    }
}
