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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends BaseActivity {

    private EditText searchEditText;
    private ImageView searchButton;
    private ListView searchSuggestionsListView;
    private TextView cityNameText, dateText, tempText, conditionText;
    private TextView feelsLikeText, humidityText, windText, uvText;
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
    private SharedPreferences searchHistory;

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
            String selected = suggestionsList.get(position);
            searchEditText.setText("");
            searchSuggestionsListView.setVisibility(View.GONE);

            String cityName = selected.split(",")[0].trim();
            currentCity = cityName;
            loadWeatherData(cityName);
            saveToHistory(selected);
            hideKeyboard();
        });

    }

    private void updateLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String username = prefs.getString("username", "Khách");
        String role = prefs.getString("role", "user");

        boolean isAdmin = "admin".equalsIgnoreCase(role);

        if (tvUserName != null) {
            if (isAdmin) {
                tvUserName.setText("👑 " + username + " (Admin)");
            } else if (isLoggedIn) {
                tvUserName.setText("👤 " + username);
            } else {
                tvUserName.setText("👤 Khách");
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
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất" + (username.isEmpty() ? "" : " khỏi tài khoản " + username) + "?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    prefs.edit().clear().apply();

                    Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginStatus();
    }

    private void openFullForecastWeb() {
        try {
            String webUrl = "https://www.weatherapi.com/weather/q/" + currentCity;

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            startActivity(browserIntent);

            Log.d("WeatherApp", "Opening browser for: " + currentCity);

        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở trình duyệt", Toast.LENGTH_SHORT).show();
            Log.e("WeatherApp", "Open browser error: " + e.getMessage());
        }
    }
    private void showSearchHistory() {
        Set<String> history = getSearchHistory();
        suggestionsList.clear();

        if (!history.isEmpty()) {
            suggestionsList.addAll(history);
            suggestionsAdapter.notifyDataSetChanged();
            searchSuggestionsListView.setVisibility(View.VISIBLE);
        } else {
            searchSuggestionsListView.setVisibility(View.GONE);
        }
    }

    private void searchLocations(String query) {
        String encoded = WeatherUtils.encodeQuery(query);
        String searchUrl = "http://api.weatherapi.com/v1/search.json?key=" + API_KEY +
                "&q=" + encoded + "&lang=vi";

        callAPI(searchUrl, response -> {
            if (response != null) {
                try {
                    JSONArray locations = new JSONArray(response);
                    suggestionsList.clear();

                    for (int i = 0; i < Math.min(locations.length(), 5); i++) {
                        JSONObject loc = locations.getJSONObject(i);
                        String name = loc.getString("name");
                        String country = loc.getString("country");
                        String region = loc.has("region") ? loc.getString("region") : "";

                        String countryVN = translateCountry(country);

                        String display = name + ", " + (region.isEmpty() ? "" : region + ", ") + countryVN;
                        suggestionsList.add(display);
                    }

                    runOnUiThread(() -> {
                        if (!suggestionsList.isEmpty()) {
                            suggestionsAdapter.notifyDataSetChanged();
                            searchSuggestionsListView.setVisibility(View.VISIBLE);
                        } else {
                            searchSuggestionsListView.setVisibility(View.GONE);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String translateCountry(String country) {
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
        switch (city.toLowerCase()) {
            case "hanoi": case "ha noi": return "Hà Nội";
            case "ho chi minh city": case "saigon": case "hcm": return "Hồ Chí Minh";
            case "da nang": return "Đà Nẵng";
            case "da lat": return "Đà Lạt";
            case "hai phong": return "Hải Phòng";
            case "can tho": return "Cần Thơ";
            case "bien hoa": return "Biên Hòa";
            case "nha trang": return "Nha Trang";
            case "hue": return "Huế";
            case "buon ma thuot": return "Buôn Ma Thuột";
            case "vung tau": return "Vũng Tàu";
            case "phu quoc": return "Phú Quốc";
            case "ha long": return "Hạ Long";
            case "sa pa": return "Sa Pa";
            default: return city;
        }
    }

    private void performSearch() {
        String city = searchEditText.getText().toString().trim();
        if (!city.isEmpty()) {
            currentCity = city;
            loadWeatherData(city);
            hideKeyboard();
            searchEditText.setText("");
            searchSuggestionsListView.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Vui lòng nhập tên thành phố", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWeatherData(String city) {
        runOnUiThread(() -> {
            cityNameText.setText("Đang tải...");
            Toast.makeText(this, "Đang tải thời tiết...", Toast.LENGTH_SHORT).show();
        });

        String encodedCity = WeatherUtils.encodeQuery(city);
        String weatherUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + encodedCity + "&days=1&aqi=no&alerts=no&lang=vi";

        Log.d("WeatherApp", "Weather URL: " + weatherUrl);

        callAPI(weatherUrl, weatherData -> {
            if (weatherData != null) {
                try {
                    JSONObject json = new JSONObject(weatherData);

                    if (json.has("error")) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Không tìm thấy thành phố: " + city, Toast.LENGTH_LONG).show();
                            cityNameText.setText("Không tìm thấy");
                        });
                        return;
                    }

                    runOnUiThread(() -> {
                        parseWeatherData(weatherData);
                        String displayCity = translateCityName(currentCity);
                        Toast.makeText(this, "Đã tải thời tiết " + displayCity, Toast.LENGTH_SHORT).show();
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        Log.e("WeatherApp", "Parse error: " + e.getMessage());
                    });
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không tìm thấy thành phố: " + city, Toast.LENGTH_LONG).show();
                    cityNameText.setText("Không tìm thấy");
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

            String displayCity = translateCityName(currentCity);
            String displayCountry = translateCountry(currentCountry);

            cityNameText.setText(displayCity + ", " + displayCountry);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("vi", "VN"));
            dateText.setText(sdf.format(new Date()));

            tempText.setText(SettingsActivity.formatTemp(this, current.getDouble("temp_c")));
            conditionText.setText(condition.getString("text"));

            feelsLikeText.setText(SettingsActivity.formatTemp(this, current.getDouble("feelslike_c")));
            humidityText.setText(current.getInt("humidity") + "%");
            windText.setText(SettingsActivity.formatWindSpeed(this, current.getDouble("wind_kph")));
            uvText.setText(String.format("%.0f - %s", current.getDouble("uv"), getUVLevel(current.getDouble("uv"))));

            Log.d("WeatherApp", "Weather data parsed successfully");

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý dữ liệu thời tiết", Toast.LENGTH_SHORT).show();
            Log.e("WeatherApp", "Parse weather error: " + e.getMessage());
        }
    }

    private String getUVLevel(double uvi) {
        if (uvi <= 2) return "Thấp";
        else if (uvi <= 5) return "Trung bình";
        else if (uvi <= 7) return "Cao";
        else if (uvi <= 10) return "Rất cao";
        else return "Cực cao";
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