package com.example.dubaothoitiet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import java.util.List;
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

    private LocationManager locationManager;
    private final String API_KEY = "35d141f7fec24768b87114757252510";
    private String exactResolvedLocationName = "";
    private String exactResolvedFullAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);

        initViews();
        setupListeners();
        checkAndRequestLocation();
    }

    private void initViews() {
        btnBackCurrentLoc = findViewById(R.id.btnBackCurrentLoc);
        TextView tvCurrentLocHeaderTitle = findViewById(R.id.tvCurrentLocHeaderTitle);
        if (tvCurrentLocHeaderTitle != null) {
            tvCurrentLocHeaderTitle.setText(isEnglish() ? "📍 Your Location" : "📍 Vị Trí Của Bạn");
        }
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
        TextView tvCurrentLocEnvParamsTitle = findViewById(R.id.tvCurrentLocEnvParamsTitle);
        if (isEnglish()) {
            if (btnRefreshLocation != null) btnRefreshLocation.setText("🔄 Automatically Relocate with GPS");
            if (tvCurrentLocEnvParamsTitle != null) tvCurrentLocEnvParamsTitle.setText("Surrounding Environmental Metrics");
        }
    }

    private void setupListeners() {
        btnBackCurrentLoc.setOnClickListener(v -> finish());
        btnRefreshLocation.setOnClickListener(v -> checkAndRequestLocation());
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
                tvGpsStatus.setText(isEnglish() ? "⚠️ GPS permission denied. Using default Vietnam location." : "⚠️ Chưa cấp quyền GPS. Đang sử dụng khu vực sinh sống tại Việt Nam.");
                loadFallbackLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchDeviceLocation() {
        tvGpsStatus.setText(isEnglish() ? "🛰️ Scanning GPS satellite signal..." : "🛰️ Đang quét tín hiệu định vị vệ tinh GPS...");

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

        // Nếu là tọa độ mặc định của máy ảo Android Studio (Mountain View California)
        if (LocationHelper.isEmulatorDefaultCoordinates(lat, lon)) {
            tvGpsStatus.setText(isEnglish() ? "🌐 Emulator: Detecting your actual location..." : "🌐 Máy ảo: Đang tự động nhận diện vị trí thực tế của bạn...");
            LocationHelper.fetchLocationByIP(this, (shortName, fullAddress, weatherQuery) -> {
                exactResolvedLocationName = shortName;
                exactResolvedFullAddress = fullAddress;
                // Đồng bộ vị trí lưu trữ toàn ứng dụng
                LocationHelper.saveLocation(this, exactResolvedLocationName, exactResolvedFullAddress, weatherQuery);
                tvGpsStatus.setText(isEnglish() ? "🟢 Automatically located your actual position!" : "🟢 Đã tự động định vị vị trí thực tế của bạn!");
                tvCurrentLocCity.setText("📍 " + exactResolvedLocationName);
                tvCurrentLocCoords.setText(exactResolvedFullAddress);
                loadWeatherByCityName(weatherQuery);
            });
            return;
        }

        tvGpsStatus.setText(isEnglish() ? "🟢 GPS satellite located successfully!" : "🟢 Đã định vị thành công qua GPS vệ tinh!");
        tvCurrentLocCoords.setText(String.format(Locale.getDefault(), (isEnglish() ? "Coordinates: %.4f°N, %.4f°E" : "Tọa độ: %.4f°N, %.4f°E"), lat, lon));

        // 1. Phân giải địa chỉ tự động theo tọa độ GPS thực tế
        resolveAddressFromCoordinates(lat, lon);

        // 2. Tải dữ liệu thời tiết theo đúng tọa độ GPS
        loadWeatherByCoordinates(lat, lon);
    }

    private void resolveAddressFromCoordinates(double lat, double lon) {
        LocationHelper.resolvePreciseLocation(this, lat, lon, (shortName, fullAddress, weatherQuery) -> {
            exactResolvedLocationName = shortName;
            exactResolvedFullAddress = fullAddress;
            // Đồng bộ vị trí lưu trữ toàn ứng dụng
            LocationHelper.saveLocation(this, exactResolvedLocationName, exactResolvedFullAddress, weatherQuery);
            boolean en = isEnglish();
            tvGpsStatus.setText(en ? "🟢 Precise GPS location locked!" : "🟢 Định vị chuẩn xác theo GPS thực tế!");
            String dispCity = en ? VietnamLocations.toEnglishLocation(exactResolvedLocationName) : exactResolvedLocationName;
            String dispAddr = en ? VietnamLocations.toEnglishLocation(exactResolvedFullAddress) : exactResolvedFullAddress;
            tvCurrentLocCity.setText("📍 " + dispCity);
            tvCurrentLocCoords.setText(dispAddr + "\n(" + (en ? "Coordinates: " : "Tọa độ: ") + String.format(Locale.getDefault(), "%.4f°N, %.4f°E", lat, lon) + ")");
        });
    }

    private void loadFallbackLocation() {
        tvGpsStatus.setText(isEnglish() ? "🌐 Locating via Internet network..." : "🌐 Đang tự động định vị vị trí thực tế qua mạng Internet...");
        LocationHelper.fetchLocationByIP(this, (shortName, fullAddress, weatherQuery) -> {
            exactResolvedLocationName = shortName;
            exactResolvedFullAddress = fullAddress;
            // Đồng bộ vị trí lưu trữ toàn ứng dụng
            LocationHelper.saveLocation(this, exactResolvedLocationName, exactResolvedFullAddress, weatherQuery);
            boolean en = isEnglish();
            tvGpsStatus.setText(en ? "🟢 Located your actual location!" : "🟢 Đã tự động định vị theo vị trí thực tế!");
            String dispCity = en ? VietnamLocations.toEnglishLocation(exactResolvedLocationName) : exactResolvedLocationName;
            String dispAddr = en ? VietnamLocations.toEnglishLocation(exactResolvedFullAddress) : exactResolvedFullAddress;
            tvCurrentLocCity.setText("📍 " + dispCity);
            tvCurrentLocCoords.setText(dispAddr);
            loadWeatherByCityName(weatherQuery);
        });
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
                "&q=" + query + "&aqi=no&lang=" + getCurrentLang();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CurrentLocationActivity.this, isEnglish() ? "API connection error" : "Lỗi kết nối API", Toast.LENGTH_SHORT).show());
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

            // Ưu tiên sử dụng tên địa chỉ chuẩn Google Maps nếu Geocoder đã tìm thấy
            boolean en = isEnglish();
            if (exactResolvedLocationName != null && !exactResolvedLocationName.trim().isEmpty()) {
                String dispCity = en ? VietnamLocations.toEnglishLocation(exactResolvedLocationName) : exactResolvedLocationName;
                tvCurrentLocCity.setText("📍 " + dispCity);
            } else {
                // Sử dụng hàm khử lỗi Mojibake từ API nếu chưa có Geocoder
                String cleanName = WeatherUtils.cleanLocationName(name);
                String fullLocation = cleanName;
                if (!region.isEmpty() && !cleanName.contains(region)) fullLocation += ", " + region;
                fullLocation += " (" + (en ? "Vietnam" : country) + ")";
                if (en) fullLocation = VietnamLocations.toEnglishLocation(fullLocation);
                tvCurrentLocCity.setText("📍 " + fullLocation);
            }

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

            tvCurrentLocTemp.setText(SettingsActivity.formatTemp(this, tempC));
            tvCurrentLocCondition.setText("🌥️ " + conditionText);
            tvCurrentLocFeelsLike.setText((en ? "Feels like: " : "Cảm giác thực tế: ") + SettingsActivity.formatTemp(this, feelsLike));

            tvCurrentLocHumidity.setText((en ? "💧 Humidity: " : "💧 Độ ẩm: ") + humidity + "%");
            tvCurrentLocWind.setText((en ? "💨 Wind: " : "💨 Gió: ") + SettingsActivity.formatWindSpeed(this, windKph));
            tvCurrentLocUV.setText(String.format(Locale.getDefault(), (en ? "☀️ UV Index: %.0f" : "☀️ Chỉ số UV: %.0f"), uv));
            tvCurrentLocPressure.setText((en ? "⏱️ Pressure: " : "⏱️ Áp suất: ") + SettingsActivity.formatPressure(this, pressureMb));
            tvCurrentLocVisibility.setText(String.format(Locale.getDefault(), (en ? "👁️ Visibility: %.1f km" : "👁️ Tầm nhìn: %.1f km"), visKm));
            tvCurrentLocCloud.setText((en ? "☁️ Cloud Cover: " : "☁️ Độ che phủ: ") + cloud + "%");

            Toast.makeText(this, en ? "Location & weather updated successfully!" : "Đã cập nhật vị trí & thời tiết thành công!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("CurrentLocation", "Lỗi phân tích: " + e.getMessage());
        }
    }
}
