package com.example.dubaothoitiet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
 * Màn hình So Sánh Thời Tiết 2 Khu Vực (Member 3)
 */
public class WeatherCompareActivity extends BaseActivity {

    private ImageButton btnBackCompare;
    private EditText etCity1, etCity2;
    private Button btnStartCompare;

    private TextView tvCompareCity1Name, tvCompareCity2Name;
    private TextView tvCompareCity1Temp, tvCompareCity2Temp;
    private TextView tvCompareCity1Condition, tvCompareCity2Condition;
    private TextView tvCompareCity1Humidity, tvCompareCity2Humidity;
    private TextView tvCompareCity1Wind, tvCompareCity2Wind;
    private TextView tvCompareCity1UV, tvCompareCity2UV;
    private TextView tvCompareConclusion;

    private final String API_KEY = "35d141f7fec24768b87114757252510";

    // Lưu dữ liệu tạm để so sánh
    private Double temp1 = null, temp2 = null;
    private Integer hum1 = null, hum2 = null;
    private Double uv1 = null, uv2 = null;
    private String name1 = "Hà Nội", name2 = "TP. HCM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_compare);

        initViews();
        setupListeners();

        // Tự động so sánh mẫu ban đầu
        compareCities("Hanoi", "Ho Chi Minh");
    }

    private void initViews() {
        btnBackCompare = findViewById(R.id.btnBackCompare);
        etCity1 = findViewById(R.id.etCity1);
        etCity2 = findViewById(R.id.etCity2);
        btnStartCompare = findViewById(R.id.btnStartCompare);

        tvCompareCity1Name = findViewById(R.id.tvCompareCity1Name);
        tvCompareCity2Name = findViewById(R.id.tvCompareCity2Name);
        tvCompareCity1Temp = findViewById(R.id.tvCompareCity1Temp);
        tvCompareCity2Temp = findViewById(R.id.tvCompareCity2Temp);
        tvCompareCity1Condition = findViewById(R.id.tvCompareCity1Condition);
        tvCompareCity2Condition = findViewById(R.id.tvCompareCity2Condition);
        tvCompareCity1Humidity = findViewById(R.id.tvCompareCity1Humidity);
        tvCompareCity2Humidity = findViewById(R.id.tvCompareCity2Humidity);
        tvCompareCity1Wind = findViewById(R.id.tvCompareCity1Wind);
        tvCompareCity2Wind = findViewById(R.id.tvCompareCity2Wind);
        tvCompareCity1UV = findViewById(R.id.tvCompareCity1UV);
        tvCompareCity2UV = findViewById(R.id.tvCompareCity2UV);
        tvCompareConclusion = findViewById(R.id.tvCompareConclusion);

        if (isEnglish()) {
            TextView tvCompareHeaderTitle = findViewById(R.id.tvCompareHeaderTitle);
            TextView tvCompareInputLabel = findViewById(R.id.tvCompareInputLabel);
            if (tvCompareHeaderTitle != null) tvCompareHeaderTitle.setText("⚖️ Compare Weather");
            if (tvCompareInputLabel != null) tvCompareInputLabel.setText("Choose 2 Locations to Compare");
            if (etCity1 != null) etCity1.setHint("Location 1 (e.g. Hanoi)");
            if (etCity2 != null) etCity2.setHint("Location 2 (e.g. Da Nang)");
            if (btnStartCompare != null) btnStartCompare.setText("Compare Weather");
        }
    }

    private void setupListeners() {
        btnBackCompare.setOnClickListener(v -> finish());

        btnStartCompare.setOnClickListener(v -> {
            String c1 = etCity1.getText().toString().trim();
            String c2 = etCity2.getText().toString().trim();
            if (c1.isEmpty() || c2.isEmpty()) {
                Toast.makeText(this, isEnglish() ? "Please enter both location names" : "Vui lòng nhập đủ tên 2 khu vực", Toast.LENGTH_SHORT).show();
                return;
            }
            compareCities(c1, c2);
        });
    }

    private void compareCities(String city1, String city2) {
        temp1 = temp2 = null;
        hum1 = hum2 = null;
        uv1 = uv2 = null;

        tvCompareConclusion.setText(isEnglish() ? "Analyzing and contrasting weather data..." : "Đang phân tích và đối sánh dữ liệu...");

        fetchCityData(city1, 1);
        fetchCityData(city2, 2);
    }

    private void fetchCityData(String query, int targetSlot) {
        String encoded = WeatherUtils.encodeQuery(query);
        String url = "http://api.weatherapi.com/v1/current.json?key=" + API_KEY +
                "&q=" + encoded + "&aqi=no&lang=" + getCurrentLang();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(WeatherCompareActivity.this, (isEnglish() ? "Unable to load data: " : "Không thể tải dữ liệu: ") + query, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    runOnUiThread(() -> parseAndDisplaySlot(json, targetSlot));
                }
            }
        });
    }

    private void parseAndDisplaySlot(String json, int slot) {
        try {
            JSONObject obj = new JSONObject(json);
            String rawCityName = obj.getJSONObject("location").getString("name");
            String cityName = VietnamLocations.getDisplayNameForApiResult(rawCityName);
            JSONObject cur = obj.getJSONObject("current");

            double temp = cur.getDouble("temp_c");
            String cond = cur.getJSONObject("condition").getString("text");
            int hum = cur.getInt("humidity");
            double wind = cur.getDouble("wind_kph");
            double uv = cur.getDouble("uv");

            if (slot == 1) {
                name1 = cityName;
                temp1 = temp;
                hum1 = hum;
                uv1 = uv;
                tvCompareCity1Name.setText(cityName);
                tvCompareCity1Temp.setText(SettingsActivity.formatTemp(this, temp));
                tvCompareCity1Condition.setText(cond);
                tvCompareCity1Humidity.setText(hum + "%");
                tvCompareCity1Wind.setText(SettingsActivity.formatWindSpeed(this, wind));
                tvCompareCity1UV.setText(String.format(Locale.getDefault(), "%.0f", uv));
            } else {
                name2 = cityName;
                temp2 = temp;
                hum2 = hum;
                uv2 = uv;
                tvCompareCity2Name.setText(cityName);
                tvCompareCity2Temp.setText(SettingsActivity.formatTemp(this, temp));
                tvCompareCity2Condition.setText(cond);
                tvCompareCity2Humidity.setText(hum + "%");
                tvCompareCity2Wind.setText(SettingsActivity.formatWindSpeed(this, wind));
                tvCompareCity2UV.setText(String.format(Locale.getDefault(), "%.0f", uv));
            }

            updateConclusion();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateConclusion() {
        if (temp1 != null && temp2 != null) {
            double diff = Math.abs(temp1 - temp2);
            String hotterCity = (temp1 > temp2) ? name1 : name2;
            String coolerCity = (temp1 > temp2) ? name2 : name1;
            boolean en = isEnglish();

            String diffStr = "F".equalsIgnoreCase(SettingsActivity.getTempUnit(this))
                    ? String.format(Locale.getDefault(), "%.1f°F", diff * 9.0 / 5.0)
                    : String.format(Locale.getDefault(), "%.1f°C", diff);

            StringBuilder sb = new StringBuilder();
            if (en) {
                if (diff >= 1.0) {
                    sb.append(coolerCity).append(" is cooler than ").append(hotterCity)
                            .append(" by about ").append(diffStr).append(". ");
                } else {
                    sb.append("Both locations have comparable temperatures (difference under 1°). ");
                }

                if (uv1 != null && uv2 != null) {
                    if (uv1 > 7 || uv2 > 7) {
                        sb.append("Note: UV index is relatively high in ");
                        sb.append(uv1 > uv2 ? name1 : name2).append("; remember sunglasses and sunscreen when going outdoors.");
                    } else {
                        sb.append("UV index is within a safe range for normal outdoor travel.");
                    }
                }
            } else {
                if (diff >= 1.0) {
                    sb.append(coolerCity).append(" đang mát mẻ hơn ").append(hotterCity)
                            .append(" khoảng ").append(diffStr).append(". ");
                } else {
                    sb.append("Hai khu vực có mức nhiệt độ tương đương nhau (chênh lệch dưới 1°). ");
                }

                if (uv1 != null && uv2 != null) {
                    if (uv1 > 7 || uv2 > 7) {
                        sb.append("Lưu ý: Chỉ số tia UV khá cao tại ");
                        sb.append(uv1 > uv2 ? name1 : name2).append(", bạn nên chuẩn bị kính râm và kem chống nắng khi di chuyển ngoài trời.");
                    } else {
                        sb.append("Chỉ số tia UV ở mức an toàn cho các hoạt động di chuyển bình thường.");
                    }
                }
            }

            tvCompareConclusion.setText(sb.toString());
        }
    }
}
