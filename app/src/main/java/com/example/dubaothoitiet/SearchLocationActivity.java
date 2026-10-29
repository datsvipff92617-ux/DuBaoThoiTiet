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

import androidx.appcompat.app.AppCompatActivity;

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

    private void setupListeners() {
        btnBackSearchLoc.setOnClickListener(v -> finish());

        btnDoSearch.setOnClickListener(v -> {
            String q = etSearchQuery.getText().toString().trim();
            if (!q.isEmpty()) {
                if (layoutSearchSuggestions != null) layoutSearchSuggestions.setVisibility(View.GONE);
                performSearch(q);
            } else {
                Toast.makeText(this, "Vui lòng nhập tên địa phương cần tìm", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Đã xóa toàn bộ lịch sử", Toast.LENGTH_SHORT).show();
        });

        btnSetAsMainCity.setOnClickListener(v -> {
            if (!currentSelectedCity.isEmpty()) {
                Intent intent = new Intent(SearchLocationActivity.this, MainActivity.class);
                intent.putExtra("city", currentSelectedCity);
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

        int count = Math.min(matches.size(), 6);
        for (int i = 0; i < count; i++) {
            VietnamLocations.LocationItem item = matches.get(i);
            TextView tv = new TextView(this);
            tv.setText("📍 " + item.getDisplayString());
            tv.setTextColor(0xFFFFFFFF);
            tv.setTextSize(14);
            tv.setPadding(16, 12, 16, 12);
            tv.setBackgroundResource(R.drawable.button_normal);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 6);
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

        for (int id : chipIds) {
            Button btn = findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    String cityName = btn.getText().toString();
                    etSearchQuery.setText(cityName);
                    if (layoutSearchSuggestions != null) layoutSearchSuggestions.setVisibility(View.GONE);
                    performSearch(cityName);
                });
            }
        }
    }

    private void performSearch(String query) {
        String displayName = VietnamLocations.getCanonicalDisplayName(query);
        Toast.makeText(this, "Đang tra cứu: " + displayName + "...", Toast.LENGTH_SHORT).show();

        String resolved = VietnamLocations.resolveToWeatherQuery(query);
        String encoded = WeatherUtils.encodeQuery(resolved);
        String url = "http://api.weatherapi.com/v1/current.json?key=" + API_KEY +
                "&q=" + encoded + "&aqi=no&lang=vi";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SearchLocationActivity.this, "Lỗi kết nối tra cứu", Toast.LENGTH_SHORT).show());
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
                    runOnUiThread(() -> Toast.makeText(SearchLocationActivity.this, "Không tìm thấy khu vực: " + displayName, Toast.LENGTH_LONG).show());
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

            String cleanName = WeatherUtils.cleanLocationName(name);
            tvResultCityName.setText(cleanName + ", " + country);
            tvResultTemp.setText(SettingsActivity.formatTemp(this, temp));
            tvResultCondition.setText("🌥️ " + condition);
            tvResultDetails.setText(String.format(Locale.getDefault(), "Độ ẩm: %d%% | Gió: %s | UV: %.0f",
                    humidity, SettingsActivity.formatWindSpeed(this, wind), uv));

            cardSearchResult.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHistoryList() {
        layoutHistoryList.removeAllViews();
        List<String> history = databaseHelper.getSearchHistory();

        if (history.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);

            for (String city : history) {
                TextView tvItem = new TextView(this);
                tvItem.setText("🔍 " + city);
                tvItem.setTextColor(0xFFFFFFFF);
                tvItem.setTextSize(14);
                tvItem.setPadding(16, 12, 16, 12);
                tvItem.setBackgroundResource(R.drawable.button_normal);

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
