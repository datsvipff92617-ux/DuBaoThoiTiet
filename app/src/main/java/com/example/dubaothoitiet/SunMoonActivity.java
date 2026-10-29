package com.example.dubaothoitiet;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Màn hình Thiên Văn & Chu Kỳ Mặt Trời / Mặt Trăng (Member 2/4)
 * Tra cứu giờ bình minh, hoàng hôn, trăng mọc, trăng lặn, tuần trăng và độ chiếu sáng
 */
public class SunMoonActivity extends BaseActivity {

    private ImageButton btnBackSunMoon;
    private TextView tvSunMoonCity, tvSunMoonDate;
    private TextView tvSunrise, tvSunset, tvSunStatus;
    private TextView tvMoonrise, tvMoonset, tvMoonPhase, tvMoonIllumination, tvAstroAdvice;

    private final String API_KEY = "35d141f7fec24768b87114757252510";
    private String currentCity = "Hà Nội";
    private String displayName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sun_moon);

        if (getIntent() != null && getIntent().hasExtra("city")) {
            currentCity = getIntent().getStringExtra("city");
        }
        if (getIntent() != null && getIntent().hasExtra("displayName")) {
            displayName = WeatherUtils.cleanLocationName(getIntent().getStringExtra("displayName"));
        } else {
            displayName = WeatherUtils.cleanLocationName(currentCity);
        }

        initViews();
        setupListeners();
        loadAstronomyData(currentCity);
    }

    private void initViews() {
        btnBackSunMoon = findViewById(R.id.btnBackSunMoon);
        tvSunMoonCity = findViewById(R.id.tvSunMoonCity);
        tvSunMoonDate = findViewById(R.id.tvSunMoonDate);

        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvSunStatus = findViewById(R.id.tvSunStatus);

        tvMoonrise = findViewById(R.id.tvMoonrise);
        tvMoonset = findViewById(R.id.tvMoonset);
        tvMoonPhase = findViewById(R.id.tvMoonPhase);
        tvMoonIllumination = findViewById(R.id.tvMoonIllumination);
        tvAstroAdvice = findViewById(R.id.tvAstroAdvice);
    }

    private void setupListeners() {
        btnBackSunMoon.setOnClickListener(v -> finish());
    }

    private void loadAstronomyData(String city) {
        String encodedCity = WeatherUtils.encodeQuery(city);
        String url = "http://api.weatherapi.com/v1/astronomy.json?key=" + API_KEY +
                "&q=" + encodedCity + "&lang=vi";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SunMoonActivity.this, "Lỗi kết nối dữ liệu thiên văn", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    runOnUiThread(() -> parseAstronomyJson(json));
                }
            }
        });
    }

    private void parseAstronomyJson(String jsonData) {
        try {
            JSONObject root = new JSONObject(jsonData);
            JSONObject loc = root.getJSONObject("location");
            String cityName = loc.optString("name", "");
            String country = loc.optString("country", "Vietnam");

            String displayLoc = (!displayName.isEmpty() && !displayName.equals("Hà Nội"))
                    ? displayName
                    : WeatherUtils.cleanLocationName(cityName);

            tvSunMoonCity.setText(displayLoc + " (" + country + ")");

            JSONObject astro = root.getJSONObject("astronomy").getJSONObject("astro");
            String sunrise = astro.getString("sunrise");
            String sunset = astro.getString("sunset");
            String moonrise = astro.getString("moonrise");
            String moonset = astro.getString("moonset");
            String moonPhase = astro.getString("moon_phase");
            int moonIllumination = astro.getInt("moon_illumination");
            int isSunUp = astro.optInt("is_sun_up", 1);

            tvSunrise.setText(sunrise);
            tvSunset.setText(sunset);
            tvMoonrise.setText(moonrise);
            tvMoonset.setText(moonset);

            tvMoonPhase.setText("Tuần trăng: " + WeatherUtils.translateMoonPhase(moonPhase));
            tvMoonIllumination.setText("💡 Độ sáng bề mặt trăng: " + moonIllumination + "%");

            if (isSunUp == 1) {
                tvSunStatus.setText("☀️ Ban ngày: Mặt trời đang chiếu sáng.");
            } else {
                tvSunStatus.setText("🌙 Ban đêm: Mặt trời đã lặn dưới đường chân trời.");
            }

            // Gợi ý ngắm thiên văn
            StringBuilder advice = new StringBuilder();
            advice.append("• Khung giờ vàng chụp ảnh bình minh: 15-30 phút trước ").append(sunrise).append(".\n");
            advice.append("• Khung giờ vàng chụp ảnh hoàng hôn: 20-30 phút quanh mốc ").append(sunset).append(".\n");
            if (moonIllumination > 80) {
                advice.append("• Trăng rất sáng (").append(moonIllumination).append("%), rất đẹp để ngắm trăng nhưng khó quan sát các chòm sao mờ.");
            } else if (moonIllumination < 20) {
                advice.append("• Trăng non hoặc khuyết mờ (").append(moonIllumination).append("%), điều kiện lý tưởng nhất trong tháng để ngắm dải ngân hà và sao băng!");
            } else {
                advice.append("• Ánh trăng vừa phải, thời tiết thích hợp cho các hoạt động dã ngoại đêm ngoài trời.");
            }
            tvAstroAdvice.setText(advice.toString());

        } catch (Exception e) {
            Log.e("SunMoonActivity", "Lỗi phân tích JSON thiên văn: " + e.getMessage());
        }
    }
}
