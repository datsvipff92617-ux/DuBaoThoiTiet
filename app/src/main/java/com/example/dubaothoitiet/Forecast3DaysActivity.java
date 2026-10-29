package com.example.dubaothoitiet;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Màn hình Dự Báo Thời Tiết 3 Ngày Tiếp Theo (Member 2)
 * Đáp ứng trực tiếp yêu cầu của thầy giáo
 */
public class Forecast3DaysActivity extends BaseActivity {

    private ImageButton btnBack3Days;
    private TextView tvCityName3Days;

    private TextView tvDateDay1, tvTempDay1, tvConditionDay1, tvRainDay1, tvHumidityDay1, tvUVDay1, tvAdviceDay1;
    private TextView tvDateDay2, tvTempDay2, tvConditionDay2, tvRainDay2, tvHumidityDay2, tvUVDay2, tvAdviceDay2;
    private TextView tvDateDay3, tvTempDay3, tvConditionDay3, tvRainDay3, tvHumidityDay3, tvUVDay3, tvAdviceDay3;

    private String cityName = "Hanoi";
    private String displayName = "Hà Nội";
    private final String API_KEY = "35d141f7fec24768b87114757252510";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_3days);

        if (getIntent().hasExtra("city")) {
            cityName = getIntent().getStringExtra("city");
        }

        if (getIntent().hasExtra("displayName")) {
            displayName = WeatherUtils.cleanLocationName(getIntent().getStringExtra("displayName"));
        } else {
            displayName = WeatherUtils.cleanLocationName(cityName);
        }

        initViews();
        setupListeners();
        load3DaysForecast();
    }

    private void initViews() {
        btnBack3Days = findViewById(R.id.btnBack3Days);
        tvCityName3Days = findViewById(R.id.tvCityName3Days);
        tvCityName3Days.setText("Khu vực: " + displayName);

        // Ngày 1
        tvDateDay1 = findViewById(R.id.tvDateDay1);
        tvTempDay1 = findViewById(R.id.tvTempDay1);
        tvConditionDay1 = findViewById(R.id.tvConditionDay1);
        tvRainDay1 = findViewById(R.id.tvRainDay1);
        tvHumidityDay1 = findViewById(R.id.tvHumidityDay1);
        tvUVDay1 = findViewById(R.id.tvUVDay1);
        tvAdviceDay1 = findViewById(R.id.tvAdviceDay1);

        // Ngày 2
        tvDateDay2 = findViewById(R.id.tvDateDay2);
        tvTempDay2 = findViewById(R.id.tvTempDay2);
        tvConditionDay2 = findViewById(R.id.tvConditionDay2);
        tvRainDay2 = findViewById(R.id.tvRainDay2);
        tvHumidityDay2 = findViewById(R.id.tvHumidityDay2);
        tvUVDay2 = findViewById(R.id.tvUVDay2);
        tvAdviceDay2 = findViewById(R.id.tvAdviceDay2);

        // Ngày 3
        tvDateDay3 = findViewById(R.id.tvDateDay3);
        tvTempDay3 = findViewById(R.id.tvTempDay3);
        tvConditionDay3 = findViewById(R.id.tvConditionDay3);
        tvRainDay3 = findViewById(R.id.tvRainDay3);
        tvHumidityDay3 = findViewById(R.id.tvHumidityDay3);
        tvUVDay3 = findViewById(R.id.tvUVDay3);
        tvAdviceDay3 = findViewById(R.id.tvAdviceDay3);
    }

    private void setupListeners() {
        btnBack3Days.setOnClickListener(v -> finish());
    }

    private void load3DaysForecast() {
        String encodedCity = WeatherUtils.encodeQuery(cityName);
        String weatherUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + encodedCity + "&days=3&aqi=no&alerts=no&lang=vi";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(weatherUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Forecast3DaysActivity.this, "Không thể kết nối đến máy chủ thời tiết", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonString = response.body().string();
                    runOnUiThread(() -> parse3DaysData(jsonString));
                }
            }
        });
    }

    private void parse3DaysData(String jsonData) {
        try {
            JSONObject json = new JSONObject(jsonData);
            JSONObject forecast = json.getJSONObject("forecast");
            JSONArray forecastDays = forecast.getJSONArray("forecastday");

            // Format date helper
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

            for (int i = 0; i < Math.min(forecastDays.length(), 3); i++) {
                JSONObject dayObj = forecastDays.getJSONObject(i);
                String rawDate = dayObj.getString("date");
                String dateStr = rawDate;
                try {
                    Date d = inputFormat.parse(rawDate);
                    if (d != null) dateStr = outputFormat.format(d);
                } catch (Exception ignored) {}

                JSONObject dayData = dayObj.getJSONObject("day");
                double maxTemp = dayData.getDouble("maxtemp_c");
                double minTemp = dayData.getDouble("mintemp_c");
                String conditionText = dayData.getJSONObject("condition").getString("text");
                int dailyChanceOfRain = dayData.optInt("daily_chance_of_rain", 0);
                double maxWind = dayData.optDouble("maxwind_kph", 10.0);
                double uv = dayData.optDouble("uv", 5.0);

                String tempStr = String.format(Locale.getDefault(), "%.0f° / %.0f°C", maxTemp, minTemp);
                String rainStr = "💧 Mưa: " + dailyChanceOfRain + "%";
                String windStr = String.format(Locale.getDefault(), "💨 Gió: %.0f km/h", maxWind);
                String uvStr = String.format(Locale.getDefault(), "☀️ UV: %.0f (%s)", uv, getUVLevel(uv));
                String adviceStr = getAdviceForDay(dailyChanceOfRain, maxTemp, uv);

                if (i == 0) {
                    tvDateDay1.setText("Hôm Nay (" + dateStr + ")");
                    tvTempDay1.setText(tempStr);
                    tvConditionDay1.setText("🌤️ " + conditionText);
                    tvRainDay1.setText(rainStr);
                    tvHumidityDay1.setText(windStr);
                    tvUVDay1.setText(uvStr);
                    tvAdviceDay1.setText("💡 Lời khuyên: " + adviceStr);
                } else if (i == 1) {
                    tvDateDay2.setText("Ngày Mai (" + dateStr + ")");
                    tvTempDay2.setText(tempStr);
                    tvConditionDay2.setText("🌤️ " + conditionText);
                    tvRainDay2.setText(rainStr);
                    tvHumidityDay2.setText(windStr);
                    tvUVDay2.setText(uvStr);
                    tvAdviceDay2.setText("💡 Lời khuyên: " + adviceStr);
                } else if (i == 2) {
                    tvDateDay3.setText("Ngày Mốt (" + dateStr + ")");
                    tvTempDay3.setText(tempStr);
                    tvConditionDay3.setText("🌤️ " + conditionText);
                    tvRainDay3.setText(rainStr);
                    tvHumidityDay3.setText(windStr);
                    tvUVDay3.setText(uvStr);
                    tvAdviceDay3.setText("💡 Lời khuyên: " + adviceStr);
                }
            }

        } catch (Exception e) {
            Log.e("Forecast3Days", "Lỗi phân tích: " + e.getMessage());
        }
    }

    private String getUVLevel(double uv) {
        if (uv <= 2) return "Thấp";
        if (uv <= 5) return "Trung bình";
        if (uv <= 7) return "Cao";
        if (uv <= 10) return "Rất cao";
        return "Nguy hại";
    }

    private String getAdviceForDay(int rainChance, double maxTemp, double uv) {
        if (rainChance >= 60) {
            return "Khả năng có mưa rất cao, hãy nhớ mang theo ô hoặc áo mưa!";
        } else if (maxTemp >= 35) {
            return "Trời nắng nóng gay gắt, hạn chế ra ngoài vào buổi trưa và uống nhiều nước.";
        } else if (uv >= 8) {
            return "Tia cực tím cực mạnh, nên bôi kem chống nắng và đeo kính râm.";
        } else if (maxTemp <= 18) {
            return "Trời se lạnh, nhớ mặc áo ấm khi ra đường.";
        } else {
            return "Thời tiết thuận lợi cho các hoạt động làm việc và vui chơi ngoài trời.";
        }
    }
}
