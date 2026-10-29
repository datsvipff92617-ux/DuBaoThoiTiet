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
    private final String API_KEY = "35d141f7fec24768b87114757252510";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_alerts);

        if (getIntent().hasExtra("city")) {
            cityName = getIntent().getStringExtra("city");
        }

        String displayName;
        if (getIntent().hasExtra("displayName")) {
            displayName = WeatherUtils.cleanLocationName(getIntent().getStringExtra("displayName"));
        } else {
            displayName = WeatherUtils.cleanLocationName(cityName);
        }

        initViews(displayName);
        setupListeners();
        loadAlertsData();
    }

    private void initViews(String displayCity) {
        btnBackAlerts = findViewById(R.id.btnBackAlerts);
        tvAlertsCity = findViewById(R.id.tvAlertsCity);
        tvAlertsCity.setText("Đang kiểm tra cảnh báo tại " + displayCity);

        tvAlertLevelIcon = findViewById(R.id.tvAlertLevelIcon);
        tvAlertLevelTitle = findViewById(R.id.tvAlertLevelTitle);
        tvAlertLevelDesc = findViewById(R.id.tvAlertLevelDesc);

        tvRainAlertStatus = findViewById(R.id.tvRainAlertStatus);
        tvHeatAlertStatus = findViewById(R.id.tvHeatAlertStatus);
        tvUvAlertStatus = findViewById(R.id.tvUvAlertStatus);
        tvWindAlertStatus = findViewById(R.id.tvWindAlertStatus);
    }

    private void setupListeners() {
        btnBackAlerts.setOnClickListener(v -> finish());
    }

    private void loadAlertsData() {
        String encodedCity = WeatherUtils.encodeQuery(cityName);
        String url = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + encodedCity + "&days=1&alerts=yes&lang=vi";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(WeatherAlertsActivity.this, "Không thể kết nối máy chủ cảnh báo", Toast.LENGTH_SHORT).show());
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

            // 1. Phân tích mưa lớn & dông
            if (rainChance >= 70 || condition.contains("dông") || condition.contains("bão") || condition.contains("mưa to")) {
                tvRainAlertStatus.setText("🔴 CẢNH BÁO: Khả năng mưa lớn (" + rainChance + "%), có nguy cơ dông sét và ngập úng cục bộ!");
                tvRainAlertStatus.setTextColor(Color.parseColor("#FF5252"));
                riskCount += 2;
            } else if (rainChance >= 40 || condition.contains("mưa")) {
                tvRainAlertStatus.setText("🟡 Chú ý: Có thể xuất hiện mưa rào rải rác (" + rainChance + "%), cần chú ý đường trơn trượt.");
                tvRainAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else {
                tvRainAlertStatus.setText("🟢 Bình thường: Khả năng mưa thấp (" + rainChance + "%), không có nguy cơ ngập lụt.");
                tvRainAlertStatus.setTextColor(Color.parseColor("#81C784"));
            }

            // 2. Phân tích nhiệt độ & nắng nóng
            if (temp >= 37) {
                tvHeatAlertStatus.setText("🔴 NGUY CẤP: Nắng nóng đặc biệt gay gắt (" + String.format("%.0f", temp) + "°C), nguy cơ sốc nhiệt và cháy nổ rất cao!");
                tvHeatAlertStatus.setTextColor(Color.parseColor("#FF5252"));
                riskCount += 2;
            } else if (temp >= 35) {
                tvHeatAlertStatus.setText("🟡 Chú ý: Nắng nóng diện rộng (" + String.format("%.0f", temp) + "°C), hạn chế làm việc ngoài trời giữa trưa.");
                tvHeatAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else if (temp <= 12) {
                tvHeatAlertStatus.setText("🟡 Cảnh báo rét đậm: Nhiệt độ xuống thấp (" + String.format("%.0f", temp) + "°C), chú ý giữ ấm cho người già và trẻ nhỏ.");
                tvHeatAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else {
                tvHeatAlertStatus.setText("🟢 Bình thường: Nhiệt độ vừa phải (" + String.format("%.0f", temp) + "°C), cơ thể thích nghi tốt.");
                tvHeatAlertStatus.setTextColor(Color.parseColor("#81C784"));
            }

            // 3. Phân tích tia UV
            if (uv >= 8) {
                tvUvAlertStatus.setText("🔴 NGUY HIỂM: Chỉ số UV cực đại (" + String.format("%.0f", uv) + "), gây bỏng da và hại mắt nhanh chóng!");
                tvUvAlertStatus.setTextColor(Color.parseColor("#FF5252"));
                riskCount += 2;
            } else if (uv >= 6) {
                tvUvAlertStatus.setText("🟡 Chú ý: Chỉ số UV mức cao (" + String.format("%.0f", uv) + "), cần bôi kem chống nắng và đeo kính râm.");
                tvUvAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else {
                tvUvAlertStatus.setText("🟢 Bình thường: Chỉ số UV ở mức an toàn (" + String.format("%.0f", uv) + ").");
                tvUvAlertStatus.setTextColor(Color.parseColor("#81C784"));
            }

            // 4. Phân tích gió giật & bão
            if (wind >= 50) {
                tvWindAlertStatus.setText("🔴 BÁO ĐỘNG GIÓ BÃO: Tốc độ gió giật mạnh " + String.format("%.0f", wind) + " km/h, đề phòng cây đổ và tốc mái!");
                tvWindAlertStatus.setTextColor(Color.parseColor("#FF5252"));
                riskCount += 2;
            } else if (wind >= 30) {
                tvWindAlertStatus.setText("🟡 Chú ý: Gió cấp 4-5 (" + String.format("%.0f", wind) + " km/h), người đi xe máy qua cầu cần cẩn trọng.");
                tvWindAlertStatus.setTextColor(Color.parseColor("#FFD54F"));
                riskCount += 1;
            } else {
                tvWindAlertStatus.setText("🟢 Bình thường: Gió êm (" + String.format("%.0f", wind) + " km/h), các hoạt động ngoài trời diễn ra an toàn.");
                tvWindAlertStatus.setTextColor(Color.parseColor("#81C784"));
            }

            // Tổng hợp mức độ rủi ro
            if (riskCount >= 3) {
                tvAlertLevelIcon.setText("🚨");
                tvAlertLevelTitle.setText("Trạng Thái: NGUY CƠ CAO");
                tvAlertLevelTitle.setTextColor(Color.parseColor("#FF5252"));
                tvAlertLevelDesc.setText("Phát hiện hiện tượng thời tiết nguy hiểm. Khuyến cáo hạn chế di chuyển ngoài trời!");
            } else if (riskCount >= 1) {
                tvAlertLevelIcon.setText("⚠️");
                tvAlertLevelTitle.setText("Trạng Thái: CẦN CHÚ Ý");
                tvAlertLevelTitle.setTextColor(Color.parseColor("#FFD54F"));
                tvAlertLevelDesc.setText("Có yếu tố thời tiết bất lợi xuất hiện. Đề nghị người dân lưu ý chuẩn bị đồ bảo hộ.");
            } else {
                tvAlertLevelIcon.setText("🛡️");
                tvAlertLevelTitle.setText("Trạng Thái: AN TOÀN");
                tvAlertLevelTitle.setTextColor(Color.parseColor("#81C784"));
                tvAlertLevelDesc.setText("Thời tiết tại khu vực hiện tại hoàn toàn bình thường, thuận lợi cho mọi hoạt động.");
            }

            tvAlertsCity.setText("Báo cáo thời gian thực tại: " + cityName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
