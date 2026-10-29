package com.example.dubaothoitiet;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Màn hình Gợi Ý Đời Sống & Trang Phục (Member 5)
 * Cung cấp lời khuyên trang phục, giặt giũ, thể thao, rửa xe dựa trên dữ liệu thời tiết
 */
public class LifestyleAdviceActivity extends BaseActivity {

    private ImageButton btnBackLifestyle;
    private TextView tvLifestyleCity;
    private TextView tvAdviceClothing, tvAdviceUmbrella, tvAdviceSunscreen;
    private TextView tvAdviceSport, tvAdviceLaundry, tvAdviceCarWash;

    private String cityName = "Hanoi";
    private final String API_KEY = "35d141f7fec24768b87114757252510";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lifestyle_advice);

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
        loadWeatherAndComputeAdvice();
    }

    private void initViews(String displayCity) {
        btnBackLifestyle = findViewById(R.id.btnBackLifestyle);
        tvLifestyleCity = findViewById(R.id.tvLifestyleCity);
        tvLifestyleCity.setText("Gợi ý sinh hoạt cho khu vực: " + displayCity);

        tvAdviceClothing = findViewById(R.id.tvAdviceClothing);
        tvAdviceUmbrella = findViewById(R.id.tvAdviceUmbrella);
        tvAdviceSunscreen = findViewById(R.id.tvAdviceSunscreen);
        tvAdviceSport = findViewById(R.id.tvAdviceSport);
        tvAdviceLaundry = findViewById(R.id.tvAdviceLaundry);
        tvAdviceCarWash = findViewById(R.id.tvAdviceCarWash);
    }

    private void setupListeners() {
        btnBackLifestyle.setOnClickListener(v -> finish());
    }

    private void loadWeatherAndComputeAdvice() {
        String encodedCity = WeatherUtils.encodeQuery(cityName);
        String url = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + encodedCity + "&days=1&lang=vi";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LifestyleAdviceActivity.this, "Không thể tải gợi ý thời tiết", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    runOnUiThread(() -> parseAndComputeAdvice(json));
                }
            }
        });
    }

    private void parseAndComputeAdvice(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject current = root.getJSONObject("current");

            double temp = current.getDouble("temp_c");
            int humidity = current.getInt("humidity");
            double uv = current.getDouble("uv");
            String cond = current.getJSONObject("condition").getString("text").toLowerCase();

            // 1. Trang phục
            if (temp >= 32) {
                tvAdviceClothing.setText("Nhiệt độ cao (" + String.format("%.0f", temp) + "°C): Nên mặc áo thun cộc tay, quần áo chất liệu cotton hoặc lanh mỏng nhẹ, màu sáng để tránh hấp thụ nhiệt.");
            } else if (temp >= 24) {
                tvAdviceClothing.setText("Nhiệt độ mát mẻ dễ chịu (" + String.format("%.0f", temp) + "°C): Thích hợp mặc áo sơ mi, áo phông hoặc váy hè nhẹ nhàng.");
            } else if (temp >= 18) {
                tvAdviceClothing.setText("Trời hơi se lạnh (" + String.format("%.0f", temp) + "°C): Nên khoác thêm áo cardigan hoặc áo khoác gió mỏng khi đi xe máy.");
            } else {
                tvAdviceClothing.setText("Trời rét buốt (" + String.format("%.0f", temp) + "°C): Hãy mặc áo phao ấm, quàng khăn len và đeo găng tay khi ra ngoài.");
            }

            // 2. Mang ô / áo mưa
            if (cond.contains("mưa") || cond.contains("dông")) {
                tvAdviceUmbrella.setText("Trời đang có mưa hoặc dông! Bắt buộc phải mang theo áo mưa hoặc ô gấp trong cốp xe.");
            } else if (humidity >= 85) {
                tvAdviceUmbrella.setText("Độ ẩm rất cao (" + humidity + "%), trời nhiều mây có thể đổ mưa bất chợt, bạn nên chuẩn bị sẵn ô che mưa.");
            } else {
                tvAdviceUmbrella.setText("Thời tiết khô ráo, xác suất mưa thấp. Bạn có thể yên tâm di chuyển mà không cần mang áo mưa cồng kềnh.");
            }

            // 3. Chống nắng & UV
            if (uv >= 8) {
                tvAdviceSunscreen.setText("Chỉ số UV cực kỳ cao (" + String.format("%.0f", uv) + "): Cần bôi kem chống nắng SPF 50+, đeo kính râm chống tia cực tím và đội nón rộng vành.");
            } else if (uv >= 5) {
                tvAdviceSunscreen.setText("Chỉ số UV ở mức trung bình (" + String.format("%.0f", uv) + "): Nên thoa kem chống nắng nhẹ khi tiếp xúc trực tiếp với ánh nắng trên 30 phút.");
            } else {
                tvAdviceSunscreen.setText("Chỉ số UV thấp và an toàn (" + String.format("%.0f", uv) + "): Ánh nắng dịu nhẹ, cơ thể có thể hấp thụ vitamin D tự nhiên.");
            }

            // 4. Thể dục thể thao ngoài trời
            if (cond.contains("mưa") || cond.contains("bão")) {
                tvAdviceSport.setText("Thời tiết có mưa gió, mặt sân trơn trượt nguy hiểm. Nên chuyển sang tập luyện trong nhà hoặc phòng gym.");
            } else if (temp >= 35) {
                tvAdviceSport.setText("Nhiệt độ ngoài trời rất cao, chỉ nên chạy bộ hoặc tập thể thao vào sáng sớm (5h00 - 6h30) hoặc tối muộn.");
            } else {
                tvAdviceSport.setText("Thời tiết tuyệt đẹp để chạy bộ, đạp xe hoặc chơi thể thao ngoài trời cùng bạn bè vào buổi chiều (16h30 - 18h00).");
            }

            // 5. Giặt & Phơi quần áo
            if (humidity <= 65 && temp >= 26 && !cond.contains("mưa")) {
                tvAdviceLaundry.setText("Độ ẩm thấp, nắng ráo thông thoáng: Hôm nay là ngày cực kỳ lý tưởng để giặt chăn mền, quần áo sẽ khô thơm chỉ sau 3-4 tiếng!");
            } else if (cond.contains("mưa") || humidity >= 80) {
                tvAdviceLaundry.setText("Độ ẩm cao hoặc có mưa: Quần áo sẽ lâu khô và dễ bị ẩm mốc, nên vắt kỹ và phơi trong phòng có quạt gió hoặc máy sấy.");
            } else {
                tvAdviceLaundry.setText("Quần áo phơi ngoài ban công sẽ khô trong ngày, nhớ thu dọn trước khi trời tắt nắng.");
            }

            // 6. Rửa xe
            if (cond.contains("mưa")) {
                tvAdviceCarWash.setText("Đang có mưa, rửa xe lúc này sẽ nhanh chóng bị bẩn lại. Hãy đợi thời tiết khô ráo những ngày tới nhé!");
            } else {
                tvAdviceCarWash.setText("Dự báo trời khô ráo, không có mưa: Đây là thời điểm rất thích hợp để rửa xe máy, ô tô cho sạch sẽ.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
