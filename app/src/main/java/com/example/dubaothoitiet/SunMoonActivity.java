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

        if (getIntent() != null && getIntent().hasExtra("city") && getIntent().getStringExtra("city") != null && !getIntent().getStringExtra("city").trim().isEmpty()) {
            currentCity = getIntent().getStringExtra("city");
        } else {
            currentCity = LocationHelper.getSavedExactQuery(this);
            if (currentCity == null || currentCity.trim().isEmpty()) {
                currentCity = LocationHelper.getSavedLocation(this);
            }
        }

        if (getIntent() != null && getIntent().hasExtra("displayName") && getIntent().getStringExtra("displayName") != null && !getIntent().getStringExtra("displayName").trim().isEmpty()) {
            displayName = WeatherUtils.cleanLocationName(getIntent().getStringExtra("displayName"));
        } else {
            displayName = LocationHelper.getSavedExactLocation(this);
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = WeatherUtils.cleanLocationName(currentCity);
            }
        }
        displayName = WeatherUtils.cleanLocationName(displayName);

        initViews();
        setupListeners();
        loadAstronomyData(currentCity);
    }

    private void initViews() {
        btnBackSunMoon = findViewById(R.id.btnBackSunMoon);
        TextView tvSunMoonHeaderTitle = findViewById(R.id.tvSunMoonHeaderTitle);
        if (tvSunMoonHeaderTitle != null) {
            tvSunMoonHeaderTitle.setText(isEnglish() ? "🌅 Astronomy & Moon Phases" : "🌅 Thiên Văn & Tuần Trăng");
        }
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
                "&q=" + encodedCity + "&lang=" + getCurrentLang();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SunMoonActivity.this, isEnglish() ? "Astronomy data connection error" : "Lỗi kết nối dữ liệu thiên văn", Toast.LENGTH_SHORT).show());
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

            boolean en = isEnglish();
            tvMoonPhase.setText(en ? ("Moon Phase: " + moonPhase) : ("Tuần trăng: " + WeatherUtils.translateMoonPhase(moonPhase)));
            tvMoonIllumination.setText((en ? "💡 Moon Illumination: " : "💡 Độ sáng bề mặt trăng: ") + moonIllumination + "%");

            if (isSunUp == 1) {
                tvSunStatus.setText(en ? "☀️ Daytime: The sun is shining." : "☀️ Ban ngày: Mặt trời đang chiếu sáng.");
            } else {
                tvSunStatus.setText(en ? "🌙 Nighttime: The sun has set below the horizon." : "🌙 Ban đêm: Mặt trời đã lặn dưới đường chân trời.");
            }

            // Gợi ý ngắm thiên văn
            StringBuilder advice = new StringBuilder();
            if (en) {
                advice.append("• Golden hour for sunrise photos: 15-30 mins before ").append(sunrise).append(".\n");
                advice.append("• Golden hour for sunset photos: 20-30 mins around ").append(sunset).append(".\n");
                if (moonIllumination > 80) {
                    advice.append("• Very bright moon (").append(moonIllumination).append("%), wonderful for moon-gazing but harder to view dim constellations.");
                } else if (moonIllumination < 20) {
                    advice.append("• New or faint crescent moon (").append(moonIllumination).append("%), ideal night of the month to stargaze and view the Milky Way!");
                } else {
                    advice.append("• Moderate moonlight, comfortable weather for outdoor nighttime picnics.");
                }
            } else {
                advice.append("• Khung giờ vàng chụp ảnh bình minh: 15-30 phút trước ").append(sunrise).append(".\n");
                advice.append("• Khung giờ vàng chụp ảnh hoàng hôn: 20-30 phút quanh mốc ").append(sunset).append(".\n");
                if (moonIllumination > 80) {
                    advice.append("• Trăng rất sáng (").append(moonIllumination).append("%), rất đẹp để ngắm trăng nhưng khó quan sát các chòm sao mờ.");
                } else if (moonIllumination < 20) {
                    advice.append("• Trăng non hoặc khuyết mờ (").append(moonIllumination).append("%), điều kiện lý tưởng nhất trong tháng để ngắm dải ngân hà và sao băng!");
                } else {
                    advice.append("• Ánh trăng vừa phải, thời tiết thích hợp cho các hoạt động dã ngoại đêm ngoài trời.");
                }
            }
            tvAstroAdvice.setText(advice.toString());

        } catch (Exception e) {
            Log.e("SunMoonActivity", "Lỗi phân tích JSON thiên văn: " + e.getMessage());
        }
    }
}
