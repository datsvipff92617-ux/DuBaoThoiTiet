package com.example.dubaothoitiet;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends BaseActivity {

    private ImageButton btnBack;
    private TextView titleText;
    private TextView detailHumidity, detailWind, detailUV, detailPressure, detailVisibility, detailFeelsLike;
    private TextView detailCloudCover, detailDewPoint, detailHeatIndex, detailWindChill;

    private String cityName;
    private final String API_KEY = "35d141f7fec24768b87114757252510";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        cityName = getIntent().getStringExtra("city");
        String displayName = getIntent().getStringExtra("displayName");
        if (cityName == null || cityName.trim().isEmpty()) {
            cityName = LocationHelper.getSavedExactQuery(this);
            if (cityName == null || cityName.trim().isEmpty()) {
                cityName = LocationHelper.getSavedLocation(this);
            }
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = LocationHelper.getSavedExactLocation(this);
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = WeatherUtils.cleanLocationName(cityName);
            }
        }
        displayName = WeatherUtils.cleanLocationName(displayName);

        initViews(displayName);
        setupListeners();
        loadWeatherDetails();
    }

    private void initViews(String displayCity) {
        btnBack = findViewById(R.id.btnBack);
        titleText = findViewById(R.id.titleText);
        detailHumidity = findViewById(R.id.detailHumidity);
        detailWind = findViewById(R.id.detailWind);
        detailUV = findViewById(R.id.detailUV);
        detailPressure = findViewById(R.id.detailPressure);
        detailVisibility = findViewById(R.id.detailVisibility);
        detailFeelsLike = findViewById(R.id.detailFeelsLike);
        detailCloudCover = findViewById(R.id.detailCloudCover);
        detailDewPoint = findViewById(R.id.detailDewPoint);
        detailHeatIndex = findViewById(R.id.detailHeatIndex);
        detailWindChill = findViewById(R.id.detailWindChill);

        boolean en = isEnglish();
        String disp = en ? VietnamLocations.toEnglishLocation(displayCity) : displayCity;
        titleText.setText((en ? "Atmospheric Details - " : "Chi tiết thời tiết - ") + disp);

        if (isEnglish()) {
            TextView lblHumidity = findViewById(R.id.lblHumidity);
            TextView lblWind = findViewById(R.id.lblWind);
            TextView lblUV = findViewById(R.id.lblUV);
            TextView lblPressure = findViewById(R.id.lblPressure);
            TextView lblVisibility = findViewById(R.id.lblVisibility);
            TextView lblFeelsLike = findViewById(R.id.lblFeelsLike);
            TextView lblCloudCover = findViewById(R.id.lblCloudCover);
            TextView lblDewPoint = findViewById(R.id.lblDewPoint);
            TextView lblHeatIndex = findViewById(R.id.lblHeatIndex);
            TextView lblWindChill = findViewById(R.id.lblWindChill);

            if (lblHumidity != null) lblHumidity.setText("💧 Humidity");
            if (lblWind != null) lblWind.setText("💨 Wind Speed");
            if (lblUV != null) lblUV.setText("☀️ UV Index");
            if (lblPressure != null) lblPressure.setText("🌡️ Pressure");
            if (lblVisibility != null) lblVisibility.setText("👁️ Visibility");
            if (lblFeelsLike != null) lblFeelsLike.setText("🌡️ Feels Like");
            if (lblCloudCover != null) lblCloudCover.setText("☁️ Cloud Cover");
            if (lblDewPoint != null) lblDewPoint.setText("💧 Dew Point");
            if (lblHeatIndex != null) lblHeatIndex.setText("🔥 Heat Index");
            if (lblWindChill != null) lblWindChill.setText("❄️ Wind Chill");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadWeatherDetails() {
        String encodedCity = WeatherUtils.encodeQuery(cityName);
        String weatherUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + encodedCity + "&days=1&aqi=no&alerts=no&lang=" + getCurrentLang();

        Log.d("WeatherApp", "Details URL: " + weatherUrl);

        callAPI(weatherUrl, response -> {
            if (response != null) {
                runOnUiThread(() -> parseWeatherDetails(response));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, isEnglish() ? "Unable to load details" : "Không thể tải chi tiết", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void parseWeatherDetails(String jsonData) {
        try {
            JSONObject json = new JSONObject(jsonData);
            JSONObject current = json.getJSONObject("current");

            detailHumidity.setText(current.getInt("humidity") + "%");
            detailWind.setText(SettingsActivity.formatWindSpeed(this, current.getDouble("wind_kph")));

            double uv = current.getDouble("uv");
            detailUV.setText(String.format("%.0f - %s", uv, getUVLevel(uv)));

            detailPressure.setText(SettingsActivity.formatPressure(this, current.getDouble("pressure_mb")));
            detailVisibility.setText(String.format("%.1f km", current.getDouble("vis_km")));
            detailFeelsLike.setText(SettingsActivity.formatTemp(this, current.getDouble("feelslike_c")));
            detailCloudCover.setText(current.getInt("cloud") + "%");

            if (current.has("dewpoint_c")) {
                detailDewPoint.setText(SettingsActivity.formatTemp(this, current.getDouble("dewpoint_c")));
            } else {
                detailDewPoint.setText("-");
            }

            if (current.has("heatindex_c")) {
                detailHeatIndex.setText(SettingsActivity.formatTemp(this, current.getDouble("heatindex_c")));
            } else {
                detailHeatIndex.setText("-");
            }

            if (current.has("windchill_c")) {
                detailWindChill.setText(SettingsActivity.formatTemp(this, current.getDouble("windchill_c")));
            } else {
                detailWindChill.setText("-");
            }

            Log.d("WeatherApp", "Weather details parsed successfully");

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, isEnglish() ? "Error processing detail data" : "Lỗi xử lý dữ liệu chi tiết", Toast.LENGTH_SHORT).show();
            Log.e("WeatherApp", "Parse details error: " + e.getMessage());
        }
    }

    private String getUVLevel(double uvi) {
        if (isEnglish()) {
            if (uvi <= 2) return "Low";
            else if (uvi <= 5) return "Moderate";
            else if (uvi <= 7) return "High";
            else if (uvi <= 10) return "Very High";
            else return "Extreme";
        } else {
            if (uvi <= 2) return "Thấp";
            else if (uvi <= 5) return "Trung bình";
            else if (uvi <= 7) return "Cao";
            else if (uvi <= 10) return "Rất cao";
            else return "Cực cao";
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