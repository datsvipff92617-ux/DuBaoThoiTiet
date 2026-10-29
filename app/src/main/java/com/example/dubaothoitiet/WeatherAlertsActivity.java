package com.example.dubaothoitiet;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Màn hình Cảnh Báo Khí Tượng & Thiên Tai (Member 5)
 * Cảnh báo mưa dông, bão lũ, nắng nóng và tia UV nguy hiểm
 */
public class WeatherAlertsActivity extends BaseActivity {

    private ImageButton btnBackAlerts;
    private TextView tvAlertsCity, tvAlertLevelIcon, tvAlertLevelTitle, tvAlertLevelDesc;
    private TextView tvRainAlertStatus, tvHeatAlertStatus, tvUvAlertStatus, tvWindAlertStatus;

    private String cityName = "Hanoi";
    private String displayName = "Hà Nội";
    private final String API_KEY = "35d141f7fec24768b87114757252510";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_alerts);

        if (!getIntent().hasExtra("city") || getIntent().getStringExtra("city") == null || getIntent().getStringExtra("city").trim().isEmpty()) {
            cityName = LocationHelper.getSavedExactQuery(this);
            if (cityName == null || cityName.trim().isEmpty()) {
                cityName = LocationHelper.getSavedLocation(this);
            }
        } else {
            cityName = getIntent().getStringExtra("city");
        }

        if (getIntent().hasExtra("displayName") && getIntent().getStringExtra("displayName") != null && !getIntent().getStringExtra("displayName").trim().isEmpty()) {
            displayName = WeatherUtils.cleanLocationName(getIntent().getStringExtra("displayName"));
        } else {
            displayName = LocationHelper.getSavedExactLocation(this);
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = WeatherUtils.cleanLocationName(cityName);
            }
        }
        displayName = WeatherUtils.cleanLocationName(displayName);

        initViews(displayName);
        setupListeners();
        loadAlertsData();
    }

    private TextView tvRainAlertTitle, tvHeatAlertTitle, tvUvAlertTitle, tvWindAlertTitle;
    private TextView tvSafetyAdviceTitle, tvSafetyAdviceBody;

    private void initViews(String displayCity) {
        btnBackAlerts = findViewById(R.id.btnBackAlerts);
        TextView tvAlertsHeaderTitle = findViewById(R.id.tvAlertsHeaderTitle);
        boolean en = isEnglish();
        if (tvAlertsHeaderTitle != null) {
            tvAlertsHeaderTitle.setText(en ? "⚠️ Weather Alerts" : "⚠️ Cảnh Báo Khí Tượng");
        }
        tvAlertsCity = findViewById(R.id.tvAlertsCity);
        String disp = en ? VietnamLocations.toEnglishLocation(displayCity) : displayCity;
        tvAlertsCity.setText((en ? "Scanning alerts for: " : "Đang kiểm tra cảnh báo tại ") + disp);

        tvAlertLevelIcon = findViewById(R.id.tvAlertLevelIcon);
        tvAlertLevelTitle = findViewById(R.id.tvAlertLevelTitle);
        tvAlertLevelDesc = findViewById(R.id.tvAlertLevelDesc);

        tvRainAlertStatus = findViewById(R.id.tvRainAlertStatus);
        tvHeatAlertStatus = findViewById(R.id.tvHeatAlertStatus);
        tvUvAlertStatus = findViewById(R.id.tvUvAlertStatus);
        tvWindAlertStatus = findViewById(R.id.tvWindAlertStatus);

        tvRainAlertTitle = findViewById(R.id.tvRainAlertTitle);
        tvHeatAlertTitle = findViewById(R.id.tvHeatAlertTitle);
        tvUvAlertTitle = findViewById(R.id.tvUvAlertTitle);
        tvWindAlertTitle = findViewById(R.id.tvWindAlertTitle);
        tvSafetyAdviceTitle = findViewById(R.id.tvSafetyAdviceTitle);
        tvSafetyAdviceBody = findViewById(R.id.tvSafetyAdviceBody);

        if (en) {
            if (tvRainAlertTitle != null) tvRainAlertTitle.setText("🌧️ Heavy Rain & Thunderstorms");
            if (tvHeatAlertTitle != null) tvHeatAlertTitle.setText("🔥 Extreme Heat & Heatstroke");
            if (tvUvAlertTitle != null) tvUvAlertTitle.setText("☀️ UV Radiation Index");
            if (tvWindAlertTitle != null) tvWindAlertTitle.setText("💨 Wind Gusts & Tropical Depressions");
            if (tvSafetyAdviceTitle != null) tvSafetyAdviceTitle.setText("🛡️ Safety & Disaster Mitigation Advice");
            if (tvSafetyAdviceBody != null) tvSafetyAdviceBody.setText("• Regularly check weather forecasts before heading outside.\n• During thunderstorms & lightning, never take shelter under large trees or power poles.\n• Prepare a portable power bank and flashlight when storm warnings are issued.\n• Dial emergency rescue hotline 114 during critical situations.");
        }
    }

    private void setupListeners() {
        btnBackAlerts.setOnClickListener(v -> finish());
    }

    private void loadAlertsData() {
        String encodedCity = WeatherUtils.encodeQuery(cityName);
        String url = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + encodedCity + "&days=1&alerts=yes&lang=" + getCurrentLang();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(WeatherAlertsActivity.this, isEnglish() ? "Unable to connect to alert server" : "Không thể kết nối máy chủ cảnh báo", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    runOnUiThread(() -> parseAlerts(json));
                }
            }
        });
    }

    private void parseAlerts(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject current = root.getJSONObject("current");

            double temp = current.getDouble("temp_c");
            double wind = current.getDouble("wind_kph");
            double uv = current.getDouble("uv");
            String condition = current.getJSONObject("condition").getString("text").toLowerCase();

            int rainChance = 10;
            if (root.has("forecast")) {
                JSONArray forecastDays = root.getJSONObject("forecast").getJSONArray("forecastday");
                if (forecastDays.length() > 0) {
                    JSONObject dayData = forecastDays.getJSONObject(0).getJSONObject("day");
                    rainChance = dayData.optInt("daily_chance_of_rain", 10);
                }
            }

            int riskCount = 0;
            boolean en = isEnglish();

            // 1. Phân tích mưa lớn & dông
            if (rainChance >= 70 || condition.contains("dông") || condition.contains("bão") || condition.contains("mưa to") || condition.contains("heavy rain") || condition.contains("thunder") || condition.contains("storm")) {
                tvRainAlertStatus.setText(en ? "🔴 WARNING: High probability of heavy rain (" + rainChance + "%), risk of lightning and localized flooding!" : "🔴 CẢNH BÁO: Khả năng mưa lớn (" + rainChance + "%), có nguy cơ dông sét và ngập úng cục bộ!");
                tvRainAlertStatus.setTextColor(Color.parseColor("#FF5252"));
                riskCount += 2;
            } else if (rainChance >= 40 || condition.contains("mưa") || condition.contains("rain")) {
                tvRainAlertStatus.setText(en ? "🟡 Caution: Scattered showers expected (" + rainChance + "%), watch out for slippery roads." : "🟡 Chú ý: Có thể xuất hiện mưa rào rải rác (" + rainChance + "%), cần chú ý đường trơn trượt.");
                tvRainAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else {
                tvRainAlertStatus.setText(en ? "🟢 Normal: Low probability of rain (" + rainChance + "%), no flood risk." : "🟢 Bình thường: Khả năng mưa thấp (" + rainChance + "%), không có nguy cơ ngập lụt.");
                tvRainAlertStatus.setTextColor(Color.parseColor("#81C784"));
            }

            // 2. Phân tích nhiệt độ & nắng nóng
            String formattedTemp = SettingsActivity.formatTemp(this, temp);
            if (temp >= 37) {
                tvHeatAlertStatus.setText(en ? "🔴 CRITICAL: Extreme heatwave (" + formattedTemp + "), severe risk of heatstroke and fire hazard!" : "🔴 NGUY CẤP: Nắng nóng đặc biệt gay gắt (" + formattedTemp + "), nguy cơ sốc nhiệt và cháy nổ rất cao!");
                tvHeatAlertStatus.setTextColor(Color.parseColor("#FF5252"));
                riskCount += 2;
            } else if (temp >= 35) {
                tvHeatAlertStatus.setText(en ? "🟡 Caution: Broad heatwave (" + formattedTemp + "), limit outdoor work around noon." : "🟡 Chú ý: Nắng nóng diện rộng (" + formattedTemp + "), hạn chế làm việc ngoài trời giữa trưa.");
                tvHeatAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else if (temp <= 12) {
                tvHeatAlertStatus.setText(en ? "🟡 Severe Cold Warning: Low temperatures (" + formattedTemp + "), keep the elderly and children warm." : "🟡 Cảnh báo rét đậm: Nhiệt độ xuống thấp (" + formattedTemp + "), chú ý giữ ấm cho người già và trẻ nhỏ.");
                tvHeatAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else {
                tvHeatAlertStatus.setText(en ? "🟢 Normal: Moderate temperature (" + formattedTemp + "), pleasant conditions." : "🟢 Bình thường: Nhiệt độ vừa phải (" + formattedTemp + "), cơ thể thích nghi tốt.");
                tvHeatAlertStatus.setTextColor(Color.parseColor("#81C784"));
            }

            // 3. Phân tích tia UV
            String uvFormatted = String.format(Locale.getDefault(), "%.0f", uv);
            if (uv >= 8) {
                String msg = en ? ("🔴 DANGER: Maximum UV index (" + uvFormatted + "), rapidly harms skin and eyes!") : ("🔴 NGUY HIỂM: Chỉ số UV cực đại (" + uvFormatted + "), gây bỏng da và hại mắt nhanh chóng!");
                tvUvAlertStatus.setText(msg);
                tvUvAlertStatus.setTextColor(Color.parseColor("#FF5252"));
                riskCount += 2;
            } else if (uv >= 6) {
                String msg = en ? ("🟡 Caution: High UV index (" + uvFormatted + "), apply sunscreen and wear sunglasses.") : ("🟡 Chú ý: Chỉ số UV mức cao (" + uvFormatted + "), cần bôi kem chống nắng và đeo kính râm.");
                tvUvAlertStatus.setText(msg);
                tvUvAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else {
                String msg = en ? ("🟢 Normal: Safe UV levels (" + uvFormatted + ").") : ("🟢 Bình thường: Chỉ số UV ở mức an toàn (" + uvFormatted + ").");
                tvUvAlertStatus.setText(msg);
                tvUvAlertStatus.setTextColor(Color.parseColor("#81C784"));
            }

            // 4. Phân tích gió giật & bão
            String formattedWind = SettingsActivity.formatWindSpeed(this, wind);
            if (wind >= 50) {
                tvWindAlertStatus.setText(en ? "🔴 GALE ALARM: Strong gale gusts " + formattedWind + ", beware of falling trees and roofs!" : "🔴 BÁO ĐỘNG GIÓ BÃO: Tốc độ gió giật mạnh " + formattedWind + ", đề phòng cây đổ và tốc mái!");
                tvWindAlertStatus.setTextColor(Color.parseColor("#FF5252"));
                riskCount += 2;
            } else if (wind >= 30) {
                tvWindAlertStatus.setText(en ? "🟡 Caution: Moderate breeze (" + formattedWind + "), drive carefully over bridges." : "🟡 Chú ý: Gió cấp 4-5 (" + formattedWind + "), người đi xe máy qua cầu cần cẩn trọng.");
                tvWindAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else {
                tvWindAlertStatus.setText(en ? "🟢 Normal: Gentle wind (" + formattedWind + "), outdoor activities are safe." : "🟢 Bình thường: Gió êm (" + formattedWind + "), các hoạt động ngoài trời diễn ra an toàn.");
                tvWindAlertStatus.setTextColor(Color.parseColor("#81C784"));
            }

            // Tổng hợp mức độ rủi ro
            if (riskCount >= 3) {
                tvAlertLevelIcon.setText("🚨");
                tvAlertLevelTitle.setText(en ? "Status: HIGH RISK" : "Trạng Thái: NGUY CƠ CAO");
                tvAlertLevelTitle.setTextColor(Color.parseColor("#FF5252"));
                tvAlertLevelDesc.setText(en ? "Dangerous weather phenomena detected. Minimize outdoor travel!" : "Phát hiện hiện tượng thời tiết nguy hiểm. Khuyến cáo hạn chế di chuyển ngoài trời!");
            } else if (riskCount >= 1) {
                tvAlertLevelIcon.setText("⚠️");
                tvAlertLevelTitle.setText(en ? "Status: CAUTION ADVISED" : "Trạng Thái: CẦN CHÚ Ý");
                tvAlertLevelTitle.setTextColor(Color.parseColor("#FFD54F"));
                tvAlertLevelDesc.setText(en ? "Adverse weather factors present. Prepare protective gear." : "Có yếu tố thời tiết bất lợi xuất hiện. Đề nghị người dân lưu ý chuẩn bị đồ bảo hộ.");
            } else {
                tvAlertLevelIcon.setText("🛡️");
                tvAlertLevelTitle.setText(en ? "Status: SAFE" : "Trạng Thái: AN TOÀN");
                tvAlertLevelTitle.setTextColor(Color.parseColor("#81C784"));
                tvAlertLevelDesc.setText(en ? "Weather in this area is normal and favorable for all activities." : "Thời tiết tại khu vực hiện tại hoàn toàn bình thường, thuận lợi cho mọi hoạt động.");
            }

            String displayLoc = WeatherUtils.cleanLocationName(displayName);
            tvAlertsCity.setText((en ? "Real-time report for: " : "Báo cáo thời gian thực tại: ") + displayLoc);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
