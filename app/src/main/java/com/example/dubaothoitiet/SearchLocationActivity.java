package com.example.dubaothoitiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
 * Màn hình Tìm Kiếm Khu Vực Mong Muốn (Member 3)
 * Tìm kiếm địa phương, danh sách tỉnh thành gợi ý và quản lý lịch sử tìm kiếm SQLite
 */
public class SearchLocationActivity extends BaseActivity {

    private ImageButton btnBackSearchLoc;
    private EditText etSearchQuery;
    private Button btnDoSearch;

    private LinearLayout cardSearchResult;
    private TextView tvResultCityName, tvResultTemp, tvResultCondition, tvResultDetails;
    private Button btnSetAsMainCity;

    private LinearLayout layoutHistoryList;
    private TextView tvClearHistory, tvEmptyHistory;

    private LinearLayout layoutSearchSuggestions, containerSuggestions;

    private DatabaseHelper databaseHelper;
    private final String API_KEY = "35d141f7fec24768b87114757252510";
    private String currentSelectedCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        applyLanguage();
        setupListeners();
        setupPopularChips();
        loadHistoryList();
    }

    private void initViews() {
        btnBackSearchLoc = findViewById(R.id.btnBackSearchLoc);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnDoSearch = findViewById(R.id.btnDoSearch);

        cardSearchResult = findViewById(R.id.cardSearchResult);
        tvResultCityName = findViewById(R.id.tvResultCityName);
        tvResultTemp = findViewById(R.id.tvResultTemp);
        tvResultCondition = findViewById(R.id.tvResultCondition);
        tvResultDetails = findViewById(R.id.tvResultDetails);
        btnSetAsMainCity = findViewById(R.id.btnSetAsMainCity);

        layoutHistoryList = findViewById(R.id.layoutHistoryList);
        tvClearHistory = findViewById(R.id.tvClearHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        layoutSearchSuggestions = findViewById(R.id.layoutSearchSuggestions);
        containerSuggestions = findViewById(R.id.containerSuggestions);
    }

    private void applyLanguage() {
        boolean en = isEnglish();
        TextView tvSearchHeaderTitle = findViewById(R.id.tvSearchHeaderTitle);
        TextView tvSuggestionHeader = findViewById(R.id.tvSuggestionHeader);
        TextView tvPopularHeaderTitle = findViewById(R.id.tvPopularHeaderTitle);
        TextView tvHistoryHeaderTitle = findViewById(R.id.tvHistoryHeaderTitle);

        if (tvSearchHeaderTitle != null) tvSearchHeaderTitle.setText(en ? "🔍 Search Location" : "🔍 Tìm Kiếm Khu Vực");
        if (etSearchQuery != null) etSearchQuery.setHint(en ? "Enter city or province name..." : "Nhập tên tỉnh/thành phố mong muốn...");
        if (btnDoSearch != null) btnDoSearch.setText(en ? "Search" : "Tìm");
        if (tvSuggestionHeader != null) tvSuggestionHeader.setText(en ? "📍 MATCHING SUGGESTIONS:" : "📍 GỢI Ý ĐỊA PHƯƠNG PHÙ HỢP:");
        if (tvPopularHeaderTitle != null) tvPopularHeaderTitle.setText(en ? "🏙️ Popular Cities & Provinces" : "🏙️ Tỉnh & Thành Phố Phổ Biến");
        if (tvHistoryHeaderTitle != null) tvHistoryHeaderTitle.setText(en ? "🕒 Search History (SQLite)" : "🕒 Lịch Sử Tìm Kiếm (SQLite)");
        if (tvClearHistory != null) tvClearHistory.setText(en ? "Clear all" : "Xóa tất cả");
        if (tvEmptyHistory != null) tvEmptyHistory.setText(en ? "No search history yet" : "Chưa có lịch sử tìm kiếm nào");
        if (btnSetAsMainCity != null) btnSetAsMainCity.setText(en ? "View Full On Home Screen" : "Xem Đầy Đủ Trên Trang Chủ");
    }

    private void setupListeners() {
        btnBackSearchLoc.setOnClickListener(v -> finish());

        btnDoSearch.setOnClickListener(v -> {
            String q = etSearchQuery.getText().toString().trim();
            if (!q.isEmpty()) {
                if (layoutSearchSuggestions != null) layoutSearchSuggestions.setVisibility(View.GONE);
                performSearch(q);
            } else {
                Toast.makeText(this, isEnglish() ? "Please enter a location name" : "Vui lòng nhập tên địa phương cần tìm", Toast.LENGTH_SHORT).show();
            }
        });

        etSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                String query = s.toString().trim();
                showSuggestions(query);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        tvClearHistory.setOnClickListener(v -> {
            databaseHelper.clearSearchHistory();
            loadHistoryList();
            Toast.makeText(this, isEnglish() ? "Cleared search history" : "Đã xóa toàn bộ lịch sử", Toast.LENGTH_SHORT).show();
        });

        btnSetAsMainCity.setOnClickListener(v -> {
            if (!currentSelectedCity.isEmpty()) {
                String cleanSelected = WeatherUtils.cleanLocationName(currentSelectedCity);
                LocationHelper.saveLocation(this, cleanSelected, cleanSelected, cleanSelected);
                Intent intent = new Intent(SearchLocationActivity.this, MainActivity.class);
                intent.putExtra("city", cleanSelected);
                intent.putExtra("displayName", cleanSelected);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showSuggestions(String query) {
        if (query.isEmpty() || layoutSearchSuggestions == null || containerSuggestions == null) {
            if (layoutSearchSuggestions != null) layoutSearchSuggestions.setVisibility(View.GONE);
            return;
        }

        List<VietnamLocations.LocationItem> matches = VietnamLocations.search(query);
        containerSuggestions.removeAllViews();

        if (matches.isEmpty()) {
            layoutSearchSuggestions.setVisibility(View.GONE);
            return;
        }

        int itemTextColor = ContextCompat.getColor(this, R.color.suggestionItemText);
        int count = Math.min(matches.size(), 6);
        for (int i = 0; i < count; i++) {
            VietnamLocations.LocationItem item = matches.get(i);
            TextView tv = new TextView(this);
            tv.setText("📍 " + item.getDisplayString(isEnglish()));
            tv.setTextColor(itemTextColor);
            tv.setTextSize(14);
            tv.setPadding(24, 20, 24, 20);
            tv.setBackgroundResource(R.drawable.suggestion_item_row_bg);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 8);
            tv.setLayoutParams(lp);

            tv.setOnClickListener(v -> {
                etSearchQuery.setText(item.name);
                layoutSearchSuggestions.setVisibility(View.GONE);
                performSearch(item.name);
            });

            containerSuggestions.addView(tv);
        }

        layoutSearchSuggestions.setVisibility(View.VISIBLE);
    }

    private void setupPopularChips() {
        int[] chipIds = {
                R.id.chipHanoi, R.id.chipHcm, R.id.chipDanang,
                R.id.chipHaiphong, R.id.chipCantho, R.id.chipDalat,
                R.id.chipNhatrang, R.id.chipHue, R.id.chipSapa
        };

        boolean en = isEnglish();
        for (int id : chipIds) {
            Button b = findViewById(id);
            if (b != null) {
                if (en) {
                    b.setText(VietnamLocations.toEnglishLocation(b.getText().toString()));
                }
                b.setOnClickListener(v -> {
                    String cityName = b.getText().toString();
                    etSearchQuery.setText(cityName);
                    if (layoutSearchSuggestions != null) layoutSearchSuggestions.setVisibility(View.GONE);
                    performSearch(cityName);
                });
            }
        }
    }

    private void performSearch(String query) {
        String displayName = VietnamLocations.getCanonicalDisplayName(query);
        boolean en = isEnglish();
        Toast.makeText(this, (en ? "Searching: " : "Đang tra cứu: ") + displayName + "...", Toast.LENGTH_SHORT).show();

        String resolved = VietnamLocations.resolveToWeatherQuery(query);
        String encoded = WeatherUtils.encodeQuery(resolved);
        String url = "http://api.weatherapi.com/v1/current.json?key=" + API_KEY +
                "&q=" + encoded + "&aqi=no&lang=" + getCurrentLang();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SearchLocationActivity.this, en ? "Search connection failed" : "Lỗi kết nối tra cứu", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    runOnUiThread(() -> {
                        parseSearchResult(json, displayName);
                        // Lưu vào lịch sử SQLite
                        databaseHelper.addSearchHistory(displayName);
                        loadHistoryList();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(SearchLocationActivity.this, (en ? "Location not found: " : "Không tìm thấy khu vực: ") + displayName, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void parseSearchResult(String jsonData, String originalQuery) {
        try {
            JSONObject json = new JSONObject(jsonData);
            JSONObject loc = json.getJSONObject("location");
            String name = loc.getString("name");
            String country = loc.getString("country");
            currentSelectedCity = name;

            JSONObject current = json.getJSONObject("current");
            double temp = current.getDouble("temp_c");
            String condition = current.getJSONObject("condition").getString("text");
            int humidity = current.getInt("humidity");
            double wind = current.getDouble("wind_kph");
            double uv = current.getDouble("uv");

            boolean en = isEnglish();
            String cleanName = WeatherUtils.cleanLocationName(name);
            String displayCity = en ? VietnamLocations.toEnglishLocation(cleanName) : cleanName;
            String displayCountry = en ? "Vietnam" : ("Vietnam".equalsIgnoreCase(country) ? "Việt Nam" : country);
            tvResultCityName.setText(displayCity + ", " + displayCountry);
            tvResultTemp.setText(SettingsActivity.formatTemp(this, temp));
            tvResultCondition.setText("🌥️ " + condition);
            tvResultDetails.setText(String.format(Locale.getDefault(), (en ? "Humidity: %d%% | Wind: %s | UV: %.0f" : "Độ ẩm: %d%% | Gió: %s | UV: %.0f"),
                    humidity, SettingsActivity.formatWindSpeed(this, wind), uv));

            cardSearchResult.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, isEnglish() ? "Data parse error" : "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHistoryList() {
        layoutHistoryList.removeAllViews();
        List<String> history = databaseHelper.getSearchHistory();

        if (history.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);

            int itemTextColor = ContextCompat.getColor(this, R.color.suggestionItemText);
            boolean en = isEnglish();
            for (String city : history) {
                TextView tvItem = new TextView(this);
                tvItem.setText("🔍 " + (en ? VietnamLocations.toEnglishLocation(city) : city));
                tvItem.setTextColor(itemTextColor);
                tvItem.setTextSize(14);
                tvItem.setPadding(24, 20, 24, 20);
                tvItem.setBackgroundResource(R.drawable.suggestion_item_row_bg);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 8);
                tvItem.setLayoutParams(lp);

                tvItem.setOnClickListener(v -> {
                    etSearchQuery.setText(city);
                    performSearch(city);
                });

                layoutHistoryList.addView(tvItem);
            }
        }
    }
}
