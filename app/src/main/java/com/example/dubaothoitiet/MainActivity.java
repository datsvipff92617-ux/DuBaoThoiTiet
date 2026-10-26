package com.example.dubaothoitiet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends BaseActivity {

    private EditText searchEditText;
    private ImageView searchButton;
    private ListView searchSuggestionsListView;
    private TextView cityNameText, dateText, tempText, conditionText;
    private TextView feelsLikeText, humidityText, windText, uvText;
    private TextView tvLabelFeelsLike, tvLabelHumidity, tvLabelWind, tvLabelUv;
    private TextView tvSection1Title, tvSection2Title, tvSection3Title, tvSection4Title, tvSection4Subtitle;
    private Button btnHourly, btnForecast, btnDetails, btnFullForecast;
    private Button btnForecast3Days, btnCurrentLocation, btnSearchLocation, btnCompareWeather;
    private Button btnAirQuality, btnWeatherAlerts, btnLifestyle, btnProfile, btnUserManagement;
    private Button btnSunMoon, btnDisasterGuide;
    private View cardAdminSection;
    private TextView tvUserName;
    private ImageView settingsButton;
    private ImageView btnLogout;

    private String currentCity = "Hanoi";
    private String currentCountry = "VN";

    private ArrayAdapter<String> suggestionsAdapter;
    private List<String> suggestionsList;
    private Map<String, String> suggestionQueryMap = new HashMap<>();
    private SharedPreferences searchHistory;

    private double lastTempC = 0, lastFeelsLikeC = 0, lastWindKph = 0, lastUv = 0;
    private boolean hasParsedWeather = false;
    private String lastLanguage = "vi";

    private final String API_KEY = "35d141f7fec24768b87114757252510";
    private static final String PREF_NAME = "SearchHistory";
    private static final String HISTORY_KEY = "history";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent() != null && getIntent().hasExtra("city")) {
            currentCity = getIntent().getStringExtra("city");
        }

        initViews();
        setupListeners();
        setupSearchHistory();

        findViewById(R.id.main2).setOnClickListener(v -> {
            if (searchSuggestionsListView.getVisibility() == View.VISIBLE) {
                searchSuggestionsListView.setVisibility(View.GONE);
                hideKeyboard();
            }
        });

        loadWeatherData(currentCity);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.hasExtra("city")) {
            currentCity = intent.getStringExtra("city");
            loadWeatherData(currentCity);
        }
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        searchSuggestionsListView = findViewById(R.id.searchSuggestionsListView);
        cityNameText = findViewById(R.id.cityNameText);
        dateText = findViewById(R.id.dateText);
        tempText = findViewById(R.id.tempText);
        conditionText = findViewById(R.id.conditionText);
        feelsLikeText = findViewById(R.id.feelsLikeText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        uvText = findViewById(R.id.uvText);
        btnFullForecast = findViewById(R.id.btnFullForecast);
        btnHourly = findViewById(R.id.btnHourly);
        btnForecast = findViewById(R.id.btnForecast);
        btnDetails = findViewById(R.id.btnDetails);
        btnForecast3Days = findViewById(R.id.btnForecast3Days);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        btnSearchLocation = findViewById(R.id.btnSearchLocation);
        btnCompareWeather = findViewById(R.id.btnCompareWeather);
        btnAirQuality = findViewById(R.id.btnAirQuality);
        btnWeatherAlerts = findViewById(R.id.btnWeatherAlerts);
        btnLifestyle = findViewById(R.id.btnLifestyle);
        btnProfile = findViewById(R.id.btnProfile);
        btnUserManagement = findViewById(R.id.btnUserManagement);
        btnSunMoon = findViewById(R.id.btnSunMoon);
        btnDisasterGuide = findViewById(R.id.btnDisasterGuide);
        cardAdminSection = findViewById(R.id.cardAdminSection);
        settingsButton = findViewById(R.id.settingsButton);
        btnLogout = findViewById(R.id.btnLogout);
        tvUserName = findViewById(R.id.tvUserName);

        tvLabelFeelsLike = findViewById(R.id.tvLabelFeelsLike);
        tvLabelHumidity = findViewById(R.id.tvLabelHumidity);
        tvLabelWind = findViewById(R.id.tvLabelWind);
        tvLabelUv = findViewById(R.id.tvLabelUv);
        tvSection1Title = findViewById(R.id.tvSection1Title);
        tvSection2Title = findViewById(R.id.tvSection2Title);
        tvSection3Title = findViewById(R.id.tvSection3Title);
        tvSection4Title = findViewById(R.id.tvSection4Title);
        tvSection4Subtitle = findViewById(R.id.tvSection4Subtitle);

        updateLoginStatus();

        searchEditText.setHint("Tìm kiếm thành phố...");

        suggestionsList = new ArrayList<>();
        suggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, suggestionsList);
        searchSuggestionsListView.setAdapter(suggestionsAdapter);
        searchSuggestionsListView.setVisibility(View.GONE);
    }

    private void setupSearchHistory() {
        searchHistory = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private Set<String> getSearchHistory() {
        return searchHistory.getStringSet(HISTORY_KEY, new HashSet<>());
    }

    private void saveToHistory(String city) {
        Set<String> history = new HashSet<>(getSearchHistory());
        history.add(city);
        searchHistory.edit().putStringSet(HISTORY_KEY, history).apply();
    }

    private void setupListeners() {
        searchButton.setOnClickListener(v -> performSearch());
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && searchEditText.getText().toString().isEmpty()) {
                showSearchHistory();
            } else if (!hasFocus) {
                v.postDelayed(() -> {
                    searchSuggestionsListView.setVisibility(View.GONE);
                }, 200);
            }
        });

        // 1. Dự báo 3 ngày tới
        if (btnForecast3Days != null) {
            btnForecast3Days.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, Forecast3DaysActivity.class);
                intent.putExtra("city", currentCity);
                startActivity(intent);
            });
        }

        // 2. Dự báo 7 ngày
        btnForecast.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
            intent.putExtra("city", currentCity);
            startActivity(intent);
        });

        // 3. Dự báo theo giờ
        btnHourly.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HourlyForecastActivity.class);
            intent.putExtra("city", currentCity);
            startActivity(intent);
        });

        // 4. Vị trí người dùng đang sống (GPS)
        if (btnCurrentLocation != null) {
            btnCurrentLocation.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CurrentLocationActivity.class);
                startActivity(intent);
            });
        }

        // 5. Tìm kiếm khu vực mong muốn
        if (btnSearchLocation != null) {
            btnSearchLocation.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SearchLocationActivity.class);
                startActivity(intent);
            });
        }

        // 6. So sánh thời tiết 2 nơi
        if (btnCompareWeather != null) {
            btnCompareWeather.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WeatherCompareActivity.class);
                startActivity(intent);
            });
        }

        // 7. Chất lượng không khí AQI
        if (btnAirQuality != null) {
            btnAirQuality.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AirQualityActivity.class);
                intent.putExtra("city", currentCity);
                startActivity(intent);
            });
        }

        // 8. Cảnh báo thiên tai & thời tiết nguy hiểm
        if (btnWeatherAlerts != null) {
            btnWeatherAlerts.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WeatherAlertsActivity.class);
                intent.putExtra("city", currentCity);
                startActivity(intent);
            });
        }

        // 9. Gợi ý đời sống & trang phục
        if (btnLifestyle != null) {
            btnLifestyle.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LifestyleAdviceActivity.class);
                intent.putExtra("city", currentCity);
                startActivity(intent);
            });
        }

        // 10. Thông số khí tượng chi tiết
        btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("city", currentCity);
            startActivity(intent);
        });

        // 11. Hồ sơ cá nhân
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        }

        // 12. Quản trị người dùng (Admin)
        if (btnUserManagement != null) {
            btnUserManagement.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, UserManagementActivity.class);
                startActivity(intent);
            });
        }

        // 13. Thiên văn Mặt trời & Mặt trăng
        if (btnSunMoon != null) {
            btnSunMoon.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SunMoonActivity.class);
                intent.putExtra("city", currentCity);
                startActivity(intent);
            });
        }

        // 14. Cẩm nang sinh tồn thiên tai
        if (btnDisasterGuide != null) {
            btnDisasterGuide.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DisasterGuideActivity.class);
                startActivity(intent);
            });
        }

        // Cài đặt ứng dụng
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Nút Đăng xuất trên thanh tìm kiếm
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }

        // Xem web trực tuyến
        btnFullForecast.setOnClickListener(v -> openFullForecastWeb());

        if (tvUserName != null) {
            tvUserName.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchLocations(s.toString());
                } else {
                    showSearchHistory();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchSuggestionsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < suggestionsList.size()) {
                String selected = suggestionsList.get(position);
                String resolvedQuery = suggestionQueryMap.get(selected);
                if (resolvedQuery == null) {
                    resolvedQuery = selected.replace("📍 ", "").split(",")[0].trim();
                }

                searchEditText.setText("");
                searchSuggestionsListView.setVisibility(View.GONE);

                currentCity = resolvedQuery;
                loadWeatherData(resolvedQuery);
                saveToHistory(selected.replace("📍 ", ""));
                hideKeyboard();
            }
        });

    }

    private void updateLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String username = prefs.getString("username", "Khách");
        String role = prefs.getString("role", "user");

        boolean isAdmin = "admin".equalsIgnoreCase(role);
        boolean en = isEnglish();

        if (tvUserName != null) {
            if (isAdmin) {
                tvUserName.setText("👑 " + username + " (Admin)");
            } else if (isLoggedIn) {
                tvUserName.setText("👤 " + username);
            } else {
                tvUserName.setText(en ? "👤 Guest" : "👤 Khách");
            }
            tvUserName.setVisibility(View.VISIBLE);
        }

        // Quyền quản trị: CHỈ DÀNH RIÊNG CHO ADMIN - HOÀN TOÀN ẨN VỚI NGƯỜI DÙNG THƯỜNG
        if (cardAdminSection != null) {
            cardAdminSection.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
        if (btnUserManagement != null) {
            btnUserManagement.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(isEnglish() ? "Logout" : "Đăng xuất")
                .setMessage(isEnglish() ? "Are you sure you want to log out?" : "Bạn có chắc chắn muốn đăng xuất" + (username.isEmpty() ? "" : " khỏi tài khoản " + username) + "?")
                .setPositiveButton(isEnglish() ? "Logout" : "Đăng xuất", (dialog, which) -> {
                    prefs.edit().clear().apply();

                    Toast.makeText(this, isEnglish() ? "Logged out" : "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(isEnglish() ? "Cancel" : "Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginStatus();
        updateUnitsDisplay();
        checkAndApplyLanguage();
    }

    private void updateUnitsDisplay() {
        if (!hasParsedWeather) return;
        tempText.setText(SettingsActivity.formatTemp(this, lastTempC));
        feelsLikeText.setText(SettingsActivity.formatTemp(this, lastFeelsLikeC));
        windText.setText(SettingsActivity.formatWindSpeed(this, lastWindKph));
        uvText.setText(String.format(Locale.getDefault(), "%.0f - %s", lastUv, getUVLevel(lastUv)));
    }

    private boolean isEnglish() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        return "en".equalsIgnoreCase(prefs.getString("language", "vi"));
    }

    private void checkAndApplyLanguage() {
        boolean en = isEnglish();
        String currentLang = en ? "en" : "vi";

        applyLanguageToUI(en);

        if (!currentLang.equalsIgnoreCase(lastLanguage)) {
            lastLanguage = currentLang;
            loadWeatherData(currentCity);
        }
    }

    private void applyLanguageToUI(boolean en) {
        if (searchEditText != null) {
            searchEditText.setHint(en ? "Search 63 provinces/cities in Vietnam..." : "Tìm kiếm 63 tỉnh thành Việt Nam...");
        }
        if (btnForecast != null) btnForecast.setText(en ? "📅 7-Day Forecast" : "📅 Dự Báo 7 Ngày Tới");
        if (btnHourly != null) btnHourly.setText(en ? "⏱️ 24-Hour Forecast" : "⏱️ Dự Báo 24 Giờ Tiếp Theo");
        if (btnForecast3Days != null) btnForecast3Days.setText(en ? "📆 3-Day Forecast" : "📆 Dự Báo 3 Ngày Tới");
        if (btnCurrentLocation != null) btnCurrentLocation.setText(en ? "📍 Your Location" : "📍 Vị Trí Của Bạn");
        if (btnSearchLocation != null) btnSearchLocation.setText(en ? "🔍 Search City" : "🔍 Tìm Kiếm Tỉnh Thành");
        if (btnCompareWeather != null) btnCompareWeather.setText(en ? "📊 Compare Cities" : "📊 So Sánh Thời Tiết 2 Nơi");
        if (btnAirQuality != null) btnAirQuality.setText(en ? "🍃 Air Quality" : "🍃 Chất Lượng Không Khí");
        if (btnWeatherAlerts != null) btnWeatherAlerts.setText(en ? "⚠️ Weather Alerts" : "⚠️ Cảnh Báo Thời Tiết");
        if (btnLifestyle != null) btnLifestyle.setText(en ? "🏃 Health & Lifestyle" : "🏃 Chỉ Số Sức Khỏe & Đời Sống");
        if (btnDetails != null) btnDetails.setText(en ? "🔬 Atmospheric Details" : "🔬 Chi Tiết Khí Áp & Môi Trường");
        if (btnProfile != null) btnProfile.setText(en ? "👤 My Profile" : "👤 Hồ Sơ Cá Nhân");
        if (btnUserManagement != null) btnUserManagement.setText(en ? "👑 User Management" : "👑 Quản Trị Hệ Thống & CSDL");
        if (btnSunMoon != null) btnSunMoon.setText(en ? "🌓 Sun & Moon Astronomy" : "🌓 Thiên Văn Mặt Trời & Mặt Trăng");
        if (btnDisasterGuide != null) btnDisasterGuide.setText(en ? "🛡️ Disaster Guide" : "🛡️ Cẩm Nang Phòng Chống Thiên Tai");
        if (btnFullForecast != null) btnFullForecast.setText(en ? "🌐 View Live Online Forecast" : "🌐 Xem Dự Báo Trực Tuyến Chi Tiết");

        if (tvLabelFeelsLike != null) tvLabelFeelsLike.setText(en ? "Feels like" : "Cảm giác");
        if (tvLabelHumidity != null) tvLabelHumidity.setText(en ? "Humidity" : "Độ ẩm");
        if (tvLabelWind != null) tvLabelWind.setText(en ? "Wind Speed" : "Tốc độ gió");
        if (tvLabelUv != null) tvLabelUv.setText(en ? "UV Index" : "Chỉ số UV");

        if (tvSection1Title != null) tvSection1Title.setText(en ? "🌦️ WEATHER FORECAST & TRENDS" : "🌦️ DỰ BÁO & XU HƯỚNG THỜI TIẾT");
        if (tvSection2Title != null) tvSection2Title.setText(en ? "🌿 HEALTH, LIFESTYLE & SAFETY" : "🌿 SỨC KHỎE, ĐỜI SỐNG & AN TOÀN");
        if (tvSection3Title != null) tvSection3Title.setText(en ? "🧭 LOCATION & UTILITIES" : "🧭 VỊ TRÍ, TIỆN ÍCH & CÁ NHÂN");
        if (tvSection4Title != null) tvSection4Title.setText(en ? "👑 ADMIN DASHBOARD" : "👑 BẢNG ĐIỀU KHIỂN QUẢN TRỊ VIÊN (ADMIN ONLY)");
        if (tvSection4Subtitle != null) tvSection4Subtitle.setText(en ? "User management & SQLite database control." : "Khu vực kiểm soát tài khoản người dùng và cơ sở dữ liệu SQLite.");
    }

    private void openFullForecastWeb() {
        try {
            String webUrl = "https://www.weatherapi.com/weather/q/" + currentCity;

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            startActivity(browserIntent);

            Log.d("WeatherApp", "Opening browser for: " + currentCity);

        } catch (Exception e) {
            Toast.makeText(this, isEnglish() ? "Cannot open browser" : "Không thể mở trình duyệt", Toast.LENGTH_SHORT).show();
            Log.e("WeatherApp", "Open browser error: " + e.getMessage());
        }
    }

    private void showSearchHistory() {
        Set<String> history = getSearchHistory();
        suggestionsList.clear();
        suggestionQueryMap.clear();

        if (!history.isEmpty()) {
            suggestionsList.addAll(history);
            suggestionsAdapter.notifyDataSetChanged();
            searchSuggestionsListView.setVisibility(View.VISIBLE);
        } else {
            searchSuggestionsListView.setVisibility(View.GONE);
        }
    }

    private void searchLocations(String query) {
        if (query == null || query.trim().isEmpty()) {
            showSearchHistory();
            return;
        }

        suggestionsList.clear();
        suggestionQueryMap.clear();

        // 1. Tìm kiếm trong danh bạ 63 Tỉnh/Thành Việt Nam (hiển thị gợi ý ngay lập tức)
        List<VietnamLocations.LocationItem> vnMatches = VietnamLocations.search(query);
        for (VietnamLocations.LocationItem item : vnMatches) {
            String display = "📍 " + item.getDisplayString();
            suggestionsList.add(display);
            suggestionQueryMap.put(display, item.weatherQuery);
        }

        if (!suggestionsList.isEmpty()) {
            suggestionsAdapter.notifyDataSetChanged();
            searchSuggestionsListView.setVisibility(View.VISIBLE);
        }

        // 2. Tìm kiếm mở rộng qua WeatherAPI để hỗ trợ thêm các quận huyện và quốc tế
        String encoded = WeatherUtils.encodeQuery(query);
        String searchUrl = "http://api.weatherapi.com/v1/search.json?key=" + API_KEY +
                "&q=" + encoded + "&lang=" + (isEnglish() ? "en" : "vi");

        callAPI(searchUrl, response -> {
            if (response != null) {
                try {
                    JSONArray locations = new JSONArray(response);
                    boolean hasNew = false;
                    for (int i = 0; i < Math.min(locations.length(), 5); i++) {
                        JSONObject loc = locations.getJSONObject(i);
                        String name = loc.getString("name");
                        String country = loc.getString("country");
                        String region = loc.optString("region", "");

                        // Bỏ qua trạm rác Humera Ethiopia
                        if (name.toLowerCase().contains("humera") && country.equalsIgnoreCase("ethiopia")) {
                            continue;
                        }

                        String countryVN = translateCountry(country);
                        String display = name + ", " + (region.isEmpty() ? "" : region + ", ") + countryVN;
                        String queryVal = name + ", " + country;

                        if (!suggestionsList.contains("📍 " + display) && !suggestionsList.contains(display)) {
                            suggestionsList.add(display);
                            suggestionQueryMap.put(display, queryVal);
                            hasNew = true;
                        }
                    }

                    if (hasNew) {
                        runOnUiThread(() -> {
                            suggestionsAdapter.notifyDataSetChanged();
                            searchSuggestionsListView.setVisibility(View.VISIBLE);
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String translateCountry(String country) {
        if (isEnglish()) return country;
        switch (country.toLowerCase()) {
            case "vietnam": return "Việt Nam";
            case "united states": case "usa": return "Hoa Kỳ";
            case "united kingdom": case "uk": return "Anh";
            case "france": return "Pháp";
            case "germany": return "Đức";
            case "japan": return "Nhật Bản";
            case "south korea": case "korea": return "Hàn Quốc";
            case "china": return "Trung Quốc";
            case "thailand": return "Thái Lan";
            case "singapore": return "Singapore";
            case "malaysia": return "Malaysia";
            case "indonesia": return "Indonesia";
            case "philippines": return "Philippines";
            case "australia": return "Úc";
            case "new zealand": return "New Zealand";
            case "canada": return "Canada";
            case "italy": return "Ý";
            case "spain": return "Tây Ban Nha";
            case "russia": return "Nga";
            case "india": return "Ấn Độ";
            default: return country;
        }
    }

    private String translateCityName(String city) {
        if (city == null) return "Hà Nội";
        return VietnamLocations.getDisplayNameForApiResult(city);
    }

    private void performSearch() {
        String city = searchEditText.getText().toString().trim();
        if (!city.isEmpty()) {
            String resolved = VietnamLocations.resolveToWeatherQuery(city);
            currentCity = resolved;
            loadWeatherData(resolved);
            hideKeyboard();
            searchEditText.setText("");
            searchSuggestionsListView.setVisibility(View.GONE);
            saveToHistory(city);
        } else {
            Toast.makeText(this, isEnglish() ? "Please enter a location" : "Vui lòng nhập tên địa phương", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWeatherData(String city) {
        boolean en = isEnglish();
        runOnUiThread(() -> {
            cityNameText.setText(en ? "Loading..." : "Đang tải...");
            Toast.makeText(this, en ? "Loading weather..." : "Đang tải thời tiết...", Toast.LENGTH_SHORT).show();
        });

        String encodedCity = WeatherUtils.encodeQuery(city);
        String weatherUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + encodedCity + "&days=1&aqi=no&alerts=no&lang=" + (en ? "en" : "vi");

        Log.d("WeatherApp", "Weather URL: " + weatherUrl);

        callAPI(weatherUrl, weatherData -> {
            if (weatherData != null) {
                try {
                    JSONObject json = new JSONObject(weatherData);

                    if (json.has("error")) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, (en ? "Location not found: " : "Không tìm thấy địa điểm: ") + city, Toast.LENGTH_LONG).show();
                            cityNameText.setText(en ? "Not Found" : "Không tìm thấy");
                        });
                        return;
                    }

                    runOnUiThread(() -> {
                        parseWeatherData(weatherData);
                        String displayCity = translateCityName(currentCity);
                        Toast.makeText(this, (en ? "Loaded weather for " : "Đã tải thời tiết ") + displayCity, Toast.LENGTH_SHORT).show();
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(this, en ? "Data parse error" : "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        Log.e("WeatherApp", "Parse error: " + e.getMessage());
                    });
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, (en ? "Location not found: " : "Không tìm thấy địa điểm: ") + city, Toast.LENGTH_LONG).show();
                    cityNameText.setText(en ? "Not Found" : "Không tìm thấy");
                });
            }
        });
    }

    private void parseWeatherData(String jsonData) {
        try {
            JSONObject json = new JSONObject(jsonData);

            JSONObject location = json.getJSONObject("location");
            currentCity = location.getString("name");
            currentCountry = location.getString("country");

            JSONObject current = json.getJSONObject("current");
            JSONObject condition = current.getJSONObject("condition");

            lastTempC = current.getDouble("temp_c");
            lastFeelsLikeC = current.getDouble("feelslike_c");
            lastWindKph = current.getDouble("wind_kph");
            lastUv = current.getDouble("uv");
            hasParsedWeather = true;

            String displayCity = translateCityName(currentCity);
            String displayCountry = translateCountry(currentCountry);

            cityNameText.setText(displayCity + ", " + displayCountry);

            boolean en = isEnglish();
            SimpleDateFormat sdf = new SimpleDateFormat(
                    en ? "EEEE, MMMM dd, yyyy" : "EEEE, dd MMMM yyyy",
                    en ? Locale.ENGLISH : new Locale("vi", "VN")
            );
            dateText.setText(sdf.format(new Date()));

            tempText.setText(SettingsActivity.formatTemp(this, lastTempC));
            conditionText.setText(condition.getString("text"));

            feelsLikeText.setText(SettingsActivity.formatTemp(this, lastFeelsLikeC));
            humidityText.setText(current.getInt("humidity") + "%");
            windText.setText(SettingsActivity.formatWindSpeed(this, lastWindKph));
            uvText.setText(String.format(Locale.getDefault(), "%.0f - %s", lastUv, getUVLevel(lastUv)));

            Log.d("WeatherApp", "Weather data parsed successfully");

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, isEnglish() ? "Weather parse error" : "Lỗi xử lý dữ liệu thời tiết", Toast.LENGTH_SHORT).show();
            Log.e("WeatherApp", "Parse weather error: " + e.getMessage());
        }
    }

    private String getUVLevel(double uvi) {
        boolean en = isEnglish();
        if (uvi <= 2) return en ? "Low" : "Thấp";
        else if (uvi <= 5) return en ? "Moderate" : "Trung bình";
        else if (uvi <= 7) return en ? "High" : "Cao";
        else if (uvi <= 10) return en ? "Very High" : "Rất cao";
        else return en ? "Extreme" : "Nguy hiểm";
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void callAPI(String urlString, APICallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(urlString).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("WeatherApp", "API call failed: " + e.getMessage());
                callback.onResponse(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    callback.onResponse(responseData);
                } else {
                    Log.e("WeatherApp", "API response not successful: " + response.code());
                    callback.onResponse(null);
                }
            }
        });
    }
}