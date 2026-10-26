package com.example.dubaothoitiet;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class HourlyForecastActivity extends BaseActivity {

    private ImageButton btnBack;
    private TextView titleText;
    private LinearLayout hourlyContainer;

    private String cityName;
    private final String API_KEY = "35d141f7fec24768b87114757252510";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);

        cityName = getIntent().getStringExtra("city");
        if (cityName == null) cityName = "Hanoi";

        initViews();
        setupListeners();
        loadHourlyForecast();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        titleText = findViewById(R.id.titleText);
        hourlyContainer = findViewById(R.id.hourlyContainer);

        String displayCity = translateCityName(cityName);
        titleText.setText("Dự báo theo giờ - " + displayCity);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadHourlyForecast() {
        String weatherUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + cityName + "&days=2&aqi=no&alerts=no&lang=vi";

        Log.d("WeatherApp", "Hourly Forecast URL: " + weatherUrl);

        callAPI(weatherUrl, response -> {
            if (response != null) {
                runOnUiThread(() -> parseHourlyForecast(response));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không thể tải dự báo theo giờ", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void parseHourlyForecast(String jsonData) {
        hourlyContainer.removeAllViews();

        try {
            JSONObject json = new JSONObject(jsonData);
            JSONObject forecast = json.getJSONObject("forecast");
            JSONArray forecastDays = forecast.getJSONArray("forecastday");

            int hourCount = 0;
            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

            // Lấy thời gian hiện tại
            long currentTimeMillis = System.currentTimeMillis();

            // Duyệt qua các ngày
            for (int dayIndex = 0; dayIndex < forecastDays.length() && hourCount < 24; dayIndex++) {
                JSONObject day = forecastDays.getJSONObject(dayIndex);
                String date = day.getString("date");
                JSONArray hours = day.getJSONArray("hour");

                // Duyệt qua các giờ trong ngày
                for (int hourIndex = 0; hourIndex < hours.length() && hourCount < 24; hourIndex++) {
                    JSONObject hour = hours.getJSONObject(hourIndex);

                    // Lấy thời gian của giờ này
                    long hourTime = hour.getLong("time_epoch") * 1000L;

                    // Chỉ hiển thị các giờ trong tương lai
                    if (hourTime > currentTimeMillis) {
                        addHourlyItem(hour, date, hourCount == 0);
                        hourCount++;
                    }
                }
            }

            if (hourCount == 0) {
                Toast.makeText(this, "Không có dữ liệu dự báo", Toast.LENGTH_SHORT).show();
            }

            Log.d("WeatherApp", "Displayed " + hourCount + " hours forecast");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hiển thị dự báo", Toast.LENGTH_SHORT).show();
            Log.e("WeatherApp", "Parse hourly forecast error: " + e.getMessage());
        }
    }

    private void addHourlyItem(JSONObject hour, String date, boolean isFirst) {
        try {
            android.view.View hourlyItem = getLayoutInflater().inflate(
                    R.layout.item_hourly_forecast, hourlyContainer, false);

            TextView timeTextView = hourlyItem.findViewById(R.id.hourlyTime);
            TextView tempTextView = hourlyItem.findViewById(R.id.hourlyTemp);
            TextView conditionTextView = hourlyItem.findViewById(R.id.hourlyCondition);
            TextView rainTextView = hourlyItem.findViewById(R.id.hourlyRain);
            TextView windTextView = hourlyItem.findViewById(R.id.hourlyWind);

            // Thời gian
            String timeStr = hour.getString("time"); // "2025-10-26 15:00"
            String[] parts = timeStr.split(" ");
            String hourTime = parts[1].substring(0, 5); // "15:00"

            if (isFirst) {
                timeTextView.setText("Bây giờ");
            } else {
                timeTextView.setText(hourTime);
            }

            // Nhiệt độ
            double temp = hour.getDouble("temp_c");
            tempTextView.setText(SettingsActivity.formatTemp(this, temp));

            // Điều kiện thời tiết
            JSONObject condition = hour.getJSONObject("condition");
            String conditionText = condition.getString("text");
            conditionTextView.setText(getWeatherEmoji(conditionText) + " " + conditionText);

            // Tỷ lệ mưa
            int rainChance = hour.getInt("chance_of_rain");
            rainTextView.setText("☔ " + rainChance + "%");

            // Tốc độ gió
            double windSpeed = hour.getDouble("wind_kph");
            windTextView.setText("💨 " + SettingsActivity.formatWindSpeed(this, windSpeed));

            hourlyContainer.addView(hourlyItem);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("WeatherApp", "Parse hour item error: " + e.getMessage());
        }
    }

    private String getWeatherEmoji(String condition) {
        String lower = condition.toLowerCase();
        if (lower.contains("nắng") || lower.contains("sunny") || lower.contains("clear")) {
            return "☀️";
        } else if (lower.contains("mây") || lower.contains("cloud") || lower.contains("overcast")) {
            return "☁️";
        } else if (lower.contains("mưa") || lower.contains("rain") || lower.contains("drizzle")) {
            return "🌧️";
        } else if (lower.contains("giông") || lower.contains("thunder") || lower.contains("storm")) {
            return "⛈️";
        } else if (lower.contains("tuyết") || lower.contains("snow")) {
            return "❄️";
        } else if (lower.contains("sương mù") || lower.contains("fog") || lower.contains("mist")) {
            return "🌫️";
        } else {
            return "🌤️";
        }
    }

    private String translateCityName(String city) {
        switch (city.toLowerCase()) {
            case "hanoi": return "Hà Nội";
            case "ho chi minh city": case "saigon": return "Hồ Chí Minh";
            case "da nang": return "Đà Nẵng";
            case "hai phong": return "Hải Phòng";
            case "can tho": return "Cần Thơ";
            case "bien hoa": return "Biên Hòa";
            case "nha trang": return "Nha Trang";
            case "hue": return "Huế";
            case "buon ma thuot": return "Buôn Ma Thuột";
            case "vung tau": return "Vũng Tàu";
            default: return city;
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