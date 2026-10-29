package com.example.dubaothoitiet;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Màn hình Chỉ Số Chất Lượng Không Khí AQI (Member 4)
 * Đo lường bụi mịn PM2.5, PM10, khí độc hại và đưa ra lời khuyên y tế
 */
public class AirQualityActivity extends BaseActivity {

    private ImageButton btnBackAqi;
    private TextView tvAqiCity, tvAqiIndexNumber, tvAqiStatus, tvAqiSummary;
    private TextView tvPm25, tvPm10, tvCo, tvNo2, tvO3, tvSo2;
    private TextView tvAqiMaskAdvice, tvAqiWindowAdvice, tvAqiOutdoorAdvice;

    private String cityName = "Hanoi";
    private final String API_KEY = "35d141f7fec24768b87114757252510";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);

        if (!getIntent().hasExtra("city") || getIntent().getStringExtra("city") == null || getIntent().getStringExtra("city").trim().isEmpty()) {
            cityName = LocationHelper.getSavedExactQuery(this);
            if (cityName == null || cityName.trim().isEmpty()) {
                cityName = LocationHelper.getSavedLocation(this);
            }
        } else {
            cityName = getIntent().getStringExtra("city");
        }

        String displayName;
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
        loadAirQualityData();
    }

    private void initViews(String displayCity) {
        btnBackAqi = findViewById(R.id.btnBackAqi);
        tvAqiCity = findViewById(R.id.tvAqiCity);
        tvAqiCity.setText((isEnglish() ? "Location: " : "Khu vực: ") + displayCity);

        TextView tvAqiHeaderTitle = findViewById(R.id.tvAqiHeaderTitle);
        TextView tvAqiScoreLabel = findViewById(R.id.tvAqiScoreLabel);
        TextView tvAqiPollutantsTitle = findViewById(R.id.tvAqiPollutantsTitle);
        TextView tvAqiAdviceTitle = findViewById(R.id.tvAqiAdviceTitle);

        TextView tvLabelPm25 = findViewById(R.id.tvLabelPm25);
        TextView tvLabelPm10 = findViewById(R.id.tvLabelPm10);
        TextView tvLabelCo = findViewById(R.id.tvLabelCo);
        TextView tvLabelNo2 = findViewById(R.id.tvLabelNo2);
        TextView tvLabelO3 = findViewById(R.id.tvLabelO3);
        TextView tvLabelSo2 = findViewById(R.id.tvLabelSo2);

        if (isEnglish()) {
            if (tvAqiHeaderTitle != null) tvAqiHeaderTitle.setText("🍃 Air Quality Index");
            if (tvAqiScoreLabel != null) tvAqiScoreLabel.setText("Pollution Index (US EPA)");
            if (tvAqiPollutantsTitle != null) tvAqiPollutantsTitle.setText("Pollutant Concentrations");
            if (tvAqiAdviceTitle != null) tvAqiAdviceTitle.setText("🩺 Health Recommendations");

            if (tvLabelPm25 != null) tvLabelPm25.setText("Fine PM2.5");
            if (tvLabelPm10 != null) tvLabelPm10.setText("Coarse PM10");
            if (tvLabelCo != null) tvLabelCo.setText("Carbon Monoxide (CO)");
            if (tvLabelNo2 != null) tvLabelNo2.setText("Nitrogen Dioxide (NO2)");
            if (tvLabelO3 != null) tvLabelO3.setText("Ozone (O3)");
            if (tvLabelSo2 != null) tvLabelSo2.setText("Sulfur Dioxide (SO2)");
        }

        tvAqiIndexNumber = findViewById(R.id.tvAqiIndexNumber);
        tvAqiStatus = findViewById(R.id.tvAqiStatus);
        tvAqiSummary = findViewById(R.id.tvAqiSummary);

        tvPm25 = findViewById(R.id.tvPm25);
        tvPm10 = findViewById(R.id.tvPm10);
        tvCo = findViewById(R.id.tvCo);
        tvNo2 = findViewById(R.id.tvNo2);
        tvO3 = findViewById(R.id.tvO3);
        tvSo2 = findViewById(R.id.tvSo2);

        tvAqiMaskAdvice = findViewById(R.id.tvAqiMaskAdvice);
        tvAqiWindowAdvice = findViewById(R.id.tvAqiWindowAdvice);
        tvAqiOutdoorAdvice = findViewById(R.id.tvAqiOutdoorAdvice);
    }

    private void setupListeners() {
        btnBackAqi.setOnClickListener(v -> finish());
    }

    private void loadAirQualityData() {
        String encodedCity = WeatherUtils.encodeQuery(cityName);
        String url = "http://api.weatherapi.com/v1/current.json?key=" + API_KEY +
                "&q=" + encodedCity + "&aqi=yes&lang=" + getCurrentLang();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AirQualityActivity.this, isEnglish() ? "Unable to load air quality data" : "Không thể tải dữ liệu chất lượng không khí", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    runOnUiThread(() -> parseAqiData(json));
                }
            }
        });
    }

    private void parseAqiData(String jsonData) {
        try {
            JSONObject root = new JSONObject(jsonData);
            JSONObject current = root.getJSONObject("current");

            if (!current.has("air_quality")) {
                tvAqiSummary.setText(isEnglish() ? "No detailed air quality station available for this location." : "Chưa có trạm đo chất lượng không khí chi tiết tại địa bàn này.");
                return;
            }

            JSONObject aqi = current.getJSONObject("air_quality");
            int epaIndex = aqi.optInt("us-epa-index", 2);
            double pm25 = aqi.optDouble("pm2_5", 25.0);
            double pm10 = aqi.optDouble("pm10", 40.0);
            double co = aqi.optDouble("co", 300.0);
            double no2 = aqi.optDouble("no2", 15.0);
            double o3 = aqi.optDouble("o3", 50.0);
            double so2 = aqi.optDouble("so2", 5.0);

            tvPm25.setText(String.format(Locale.getDefault(), "%.1f µg/m³", pm25));
            tvPm10.setText(String.format(Locale.getDefault(), "%.1f µg/m³", pm10));
            tvCo.setText(String.format(Locale.getDefault(), "%.1f µg/m³", co));
            tvNo2.setText(String.format(Locale.getDefault(), "%.1f µg/m³", no2));
            tvO3.setText(String.format(Locale.getDefault(), "%.1f µg/m³", o3));
            tvSo2.setText(String.format(Locale.getDefault(), "%.1f µg/m³", so2));

            tvAqiIndexNumber.setText(String.valueOf(epaIndex));

            boolean en = isEnglish();

            // Đánh giá theo thang US EPA
            switch (epaIndex) {
                case 1:
                    tvAqiIndexNumber.setTextColor(Color.parseColor("#4CAF50"));
                    tvAqiStatus.setTextColor(Color.parseColor("#4CAF50"));
                    tvAqiStatus.setText(en ? "Good" : "Tốt (Good)");
                    tvAqiSummary.setText(en ? "Air quality is satisfactory, poses little or no risk to health." : "Không khí trong lành, không ảnh hưởng đến sức khỏe cộng đồng.");
                    tvAqiMaskAdvice.setText(en ? "😷 Face mask: Not required, enjoy fresh air." : "😷 Khẩu trang: Không bắt buộc, có thể hít thở tự nhiên.");
                    tvAqiWindowAdvice.setText(en ? "🪟 Windows: Great time to open windows for ventilation." : "🪟 Mở cửa sổ: Rất nên mở cửa để đón gió trời trong lành.");
                    tvAqiOutdoorAdvice.setText(en ? "🏃 Outdoor activity: Ideal for exercising and outdoor sports." : "🏃 Vận động ngoài trời: Rất tuyệt vời cho tập thể dục và thể thao.");
                    break;
                case 2:
                    tvAqiIndexNumber.setTextColor(Color.parseColor("#FFEB3B"));
                    tvAqiStatus.setTextColor(Color.parseColor("#FFEB3B"));
                    tvAqiStatus.setText(en ? "Moderate" : "Trung Bình (Moderate)");
                    tvAqiSummary.setText(en ? "Air quality is acceptable for most people." : "Chất lượng không khí ở mức chấp nhận được đối với đa số mọi người.");
                    tvAqiMaskAdvice.setText(en ? "😷 Face mask: Recommended when commuting in busy traffic." : "😷 Khẩu trang: Nên đeo khi đi lại trên các tuyến phố đông đúc.");
                    tvAqiWindowAdvice.setText(en ? "🪟 Windows: Can be opened in the early morning." : "🪟 Mở cửa sổ: Có thể mở cửa vào buổi sáng sớm.");
                    tvAqiOutdoorAdvice.setText(en ? "🏃 Outdoor activity: Normal exercise is acceptable." : "🏃 Vận động ngoài trời: Có thể tập thể dục bình thường.");
                    break;
                case 3:
                    tvAqiIndexNumber.setTextColor(Color.parseColor("#FF9800"));
                    tvAqiStatus.setTextColor(Color.parseColor("#FF9800"));
                    tvAqiStatus.setText(en ? "Unhealthy for Sensitive Groups" : "Kém (Kém cho nhóm nhạy cảm)");
                    tvAqiSummary.setText(en ? "Members of sensitive groups may experience health effects." : "Nhóm người già, trẻ nhỏ và người có bệnh hô hấp có thể bị ảnh hưởng.");
                    tvAqiMaskAdvice.setText(en ? "😷 Face mask: Sensitive groups should wear fine-dust filtering masks." : "😷 Khẩu trang: Nhóm nhạy cảm bắt buộc phải đeo khẩu trang chống bụi mịn.");
                    tvAqiWindowAdvice.setText(en ? "🪟 Windows: Close windows to avoid outside air pollution." : "🪟 Mở cửa sổ: Nên đóng cửa sổ để tránh bụi bay vào nhà.");
                    tvAqiOutdoorAdvice.setText(en ? "🏃 Outdoor activity: Reduce prolonged or heavy outdoor exertion." : "🏃 Vận động ngoài trời: Giảm cường độ tập thể dục ngoài trời.");
                    break;
                case 4:
                    tvAqiIndexNumber.setTextColor(Color.parseColor("#F44336"));
                    tvAqiStatus.setTextColor(Color.parseColor("#F44336"));
                    tvAqiStatus.setText(en ? "Unhealthy" : "Xấu (Unhealthy)");
                    tvAqiSummary.setText(en ? "Everyone may begin to experience adverse health effects." : "Bắt đầu ảnh hưởng xấu đến sức khỏe của tất cả mọi người.");
                    tvAqiMaskAdvice.setText(en ? "😷 Face mask: Wear N95/FFP2 protective mask when going outside." : "😷 Khẩu trang: Cần đeo khẩu trang lọc bụi mịn chuẩn N95 khi ra đường.");
                    tvAqiWindowAdvice.setText(en ? "🪟 Windows: Keep windows tightly closed; turn on air purifier." : "🪟 Mở cửa sổ: Đóng kín cửa sổ và bật máy lọc không khí nếu có.");
                    tvAqiOutdoorAdvice.setText(en ? "🏃 Outdoor activity: Avoid outdoor activities; exercise indoors." : "🏃 Vận động ngoài trời: Tránh hoàn toàn việc tập thể dục ngoài đường.");
                    break;
                default:
                    tvAqiIndexNumber.setTextColor(Color.parseColor("#9C27B0"));
                    tvAqiStatus.setTextColor(Color.parseColor("#9C27B0"));
                    tvAqiStatus.setText(en ? "Very Unhealthy / Hazardous" : "Rất Xấu / Nguy Hại (Hazardous)");
                    tvAqiSummary.setText(en ? "Health emergency warning: the entire population is more likely to be affected." : "Cảnh báo khẩn cấp: Không khí ô nhiễm nghiêm trọng, nguy hại cho sức khỏe!");
                    tvAqiMaskAdvice.setText(en ? "😷 Face mask: Mandatory specialized N95/N99 mask if outdoors." : "😷 Khẩu trang: Luôn đeo khẩu trang chuyên dụng N95/N99 khi bắt buộc ra ngoài.");
                    tvAqiWindowAdvice.setText(en ? "🪟 Windows: Never open windows; run air purifier continuously." : "🪟 Mở cửa sổ: Tuyệt đối không mở cửa, sử dụng máy lọc khí liên tục.");
                    tvAqiOutdoorAdvice.setText(en ? "🏃 Outdoor activity: Stay indoors and avoid physical activities outside." : "🏃 Vận động ngoài trời: Hạn chế tối đa việc ra khỏi nhà.");
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
