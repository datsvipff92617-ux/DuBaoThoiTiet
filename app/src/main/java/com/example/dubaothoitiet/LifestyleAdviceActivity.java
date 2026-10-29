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
        loadWeatherAndComputeAdvice();
    }

    private void initViews(String displayCity) {
        btnBackLifestyle = findViewById(R.id.btnBackLifestyle);
        TextView tvLifestyleHeaderTitle = findViewById(R.id.tvLifestyleHeaderTitle);
        if (tvLifestyleHeaderTitle != null) {
            tvLifestyleHeaderTitle.setText(isEnglish() ? "👕 Lifestyle & Health Advice" : "👕 Gợi Ý Đời Sống");
        }
        tvLifestyleCity = findViewById(R.id.tvLifestyleCity);
        tvLifestyleCity.setText((isEnglish() ? "Lifestyle suggestions for: " : "Gợi ý sinh hoạt cho khu vực: ") + displayCity);

        tvAdviceClothing = findViewById(R.id.tvAdviceClothing);
        tvAdviceUmbrella = findViewById(R.id.tvAdviceUmbrella);
        tvAdviceSunscreen = findViewById(R.id.tvAdviceSunscreen);
        tvAdviceSport = findViewById(R.id.tvAdviceSport);
        tvAdviceLaundry = findViewById(R.id.tvAdviceLaundry);
        tvAdviceCarWash = findViewById(R.id.tvAdviceCarWash);

        TextView tvTitleClothing = findViewById(R.id.tvTitleClothing);
        TextView tvTitleUmbrella = findViewById(R.id.tvTitleUmbrella);
        TextView tvTitleSunscreen = findViewById(R.id.tvTitleSunscreen);
        TextView tvTitleSport = findViewById(R.id.tvTitleSport);
        TextView tvTitleLaundry = findViewById(R.id.tvTitleLaundry);
        TextView tvTitleCarWash = findViewById(R.id.tvTitleCarWash);

        if (isEnglish()) {
            if (tvTitleClothing != null) tvTitleClothing.setText("👕 Recommended Clothing");
            if (tvTitleUmbrella != null) tvTitleUmbrella.setText("☂️ Carry Umbrella / Raincoat");
            if (tvTitleSunscreen != null) tvTitleSunscreen.setText("🧴 Sunscreen & Skin Protection");
            if (tvTitleSport != null) tvTitleSport.setText("🏃 Outdoor Exercise & Sports");
            if (tvTitleLaundry != null) tvTitleLaundry.setText("🧺 Laundry & Clothes Drying");
            if (tvTitleCarWash != null) tvTitleCarWash.setText("🚗 Motorbike / Car Wash");
        }
    }

    private void setupListeners() {
        btnBackLifestyle.setOnClickListener(v -> finish());
    }

    private void loadWeatherAndComputeAdvice() {
        String encodedCity = WeatherUtils.encodeQuery(cityName);
        String url = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY +
                "&q=" + encodedCity + "&days=1&lang=" + getCurrentLang();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LifestyleAdviceActivity.this, isEnglish() ? "Unable to load lifestyle advice" : "Không thể tải gợi ý thời tiết", Toast.LENGTH_SHORT).show());
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
            boolean en = isEnglish();

            String tempFormatted = SettingsActivity.formatTemp(this, temp);

            // 1. Trang phục
            if (temp >= 32) {
                tvAdviceClothing.setText(en ? "High temperature (" + tempFormatted + "): Wear lightweight, breathable cotton or linen clothes, light colors to reflect heat."
                        : "Nhiệt độ cao (" + tempFormatted + "): Nên mặc áo thun cộc tay, quần áo chất liệu cotton hoặc lanh mỏng nhẹ, màu sáng để tránh hấp thụ nhiệt.");
            } else if (temp >= 24) {
                tvAdviceClothing.setText(en ? "Pleasant and cool (" + tempFormatted + "): Suitable for light shirts, t-shirts or casual summer wear."
                        : "Nhiệt độ mát mẻ dễ chịu (" + tempFormatted + "): Thích hợp mặc áo sơ mi, áo phông hoặc váy hè nhẹ nhàng.");
            } else if (temp >= 18) {
                tvAdviceClothing.setText(en ? "A bit chilly (" + tempFormatted + "): Carry a light cardigan or windbreaker jacket when going outdoors."
                        : "Trời hơi se lạnh (" + tempFormatted + "): Nên khoác thêm áo cardigan hoặc áo khoác gió mỏng khi đi xe máy.");
            } else {
                tvAdviceClothing.setText(en ? "Cold weather (" + tempFormatted + "): Wear a warm down jacket, scarf and gloves outdoors."
                        : "Trời rét buốt (" + tempFormatted + "): Hãy mặc áo phao ấm, quàng khăn len và đeo găng tay khi ra ngoài.");
            }

            // 2. Mang ô / áo mưa
            if (cond.contains("mưa") || cond.contains("dông") || cond.contains("rain") || cond.contains("thunder") || cond.contains("drizzle")) {
                tvAdviceUmbrella.setText(en ? "Rain or storm in progress! Be sure to carry an umbrella or raincoat."
                        : "Trời đang có mưa hoặc dông! Bắt buộc phải mang theo áo mưa hoặc ô gấp trong cốp xe.");
            } else if (humidity >= 85) {
                tvAdviceUmbrella.setText(en ? "High humidity (" + humidity + "%), overcast skies may bring sudden showers. Keep an umbrella handy."
                        : "Độ ẩm rất cao (" + humidity + "%), trời nhiều mây có thể đổ mưa bất chợt, bạn nên chuẩn bị sẵn ô che mưa.");
            } else {
                tvAdviceUmbrella.setText(en ? "Dry weather, low chance of rain. You can travel freely without bulky rain gear."
                        : "Thời tiết khô ráo, xác suất mưa thấp. Bạn có thể yên tâm di chuyển mà không cần mang áo mưa cồng kềnh.");
            }

            // 3. Chống nắng & UV
            if (uv >= 8) {
                tvAdviceSunscreen.setText(en ? "Very high UV (" + String.format("%.0f", uv) + "): Apply SPF 50+ sunscreen, wear UV-protection sunglasses and wide-brimmed hat."
                        : "Chỉ số UV cực kỳ cao (" + String.format("%.0f", uv) + "): Cần bôi kem chống nắng SPF 50+, đeo kính râm chống tia cực tím và đội nón rộng vành.");
            } else if (uv >= 5) {
                tvAdviceSunscreen.setText(en ? "Moderate UV (" + String.format("%.0f", uv) + "): Apply light sunscreen if exposed to direct sun for over 30 minutes."
                        : "Chỉ số UV ở mức trung bình (" + String.format("%.0f", uv) + "): Nên thoa kem chống nắng nhẹ khi tiếp xúc trực tiếp với ánh nắng trên 30 phút.");
            } else {
                tvAdviceSunscreen.setText(en ? "Safe low UV (" + String.format("%.0f", uv) + "): Gentle sunlight, great for natural vitamin D absorption."
                        : "Chỉ số UV thấp và an toàn (" + String.format("%.0f", uv) + "): Ánh nắng dịu nhẹ, cơ thể có thể hấp thụ vitamin D tự nhiên.");
            }

            // 4. Thể dục thể thao ngoài trời
            if (cond.contains("mưa") || cond.contains("bão") || cond.contains("rain") || cond.contains("storm")) {
                tvAdviceSport.setText(en ? "Wet and slippery conditions outside. Prefer indoor workouts or gym training."
                        : "Thời tiết có mưa gió, mặt sân trơn trượt nguy hiểm. Nên chuyển sang tập luyện trong nhà hoặc phòng gym.");
            } else if (temp >= 35) {
                tvAdviceSport.setText(en ? "Hot outdoor temperatures. Only jog or exercise early in the morning (5:00 - 6:30 AM) or late evening."
                        : "Nhiệt độ ngoài trời rất cao, chỉ nên chạy bộ hoặc tập thể thao vào sáng sớm (5h00 - 6h30) hoặc tối muộn.");
            } else {
                tvAdviceSport.setText(en ? "Splendid weather for jogging, cycling, or outdoor sports with friends in the late afternoon."
                        : "Thời tiết tuyệt đẹp để chạy bộ, đạp xe hoặc chơi thể thao ngoài trời cùng bạn bè vào buổi chiều (16h30 - 18h00).");
            }

            // 5. Giặt & Phơi quần áo
            if (humidity <= 65 && temp >= 26 && !cond.contains("mưa") && !cond.contains("rain")) {
                tvAdviceLaundry.setText(en ? "Low humidity and sunny breeze: Ideal day to do laundry and blankets, dries fresh in 3-4 hours!"
                        : "Độ ẩm thấp, nắng ráo thông thoáng: Hôm nay là ngày cực kỳ lý tưởng để giặt chăn mền, quần áo sẽ khô thơm chỉ sau 3-4 tiếng!");
            } else if (cond.contains("mưa") || humidity >= 80 || cond.contains("rain")) {
                tvAdviceLaundry.setText(en ? "High humidity or rain: Clothes will dry slowly. Dry indoors with a fan or tumble dryer."
                        : "Độ ẩm cao hoặc có mưa: Quần áo sẽ lâu khô và dễ bị ẩm mốc, nên vắt kỹ và phơi trong phòng có quạt gió hoặc máy sấy.");
            } else {
                tvAdviceLaundry.setText(en ? "Clothes hung on the balcony will dry during the day. Collect before sundown."
                        : "Quần áo phơi ngoài ban công sẽ khô trong ngày, nhớ thu dọn trước khi trời tắt nắng.");
            }

            // 6. Rửa xe
            if (cond.contains("mưa") || cond.contains("rain")) {
                tvAdviceCarWash.setText(en ? "It is raining or damp; washing cars now will quickly get dirty again. Wait for dry days ahead!"
                        : "Đang có mưa, rửa xe lúc này sẽ nhanh chóng bị bẩn lại. Hãy đợi thời tiết khô ráo những ngày tới nhé!");
            } else {
                tvAdviceCarWash.setText(en ? "Dry forecast with no rain expected: Great time to wash your motorbike or car!"
                        : "Dự báo trời khô ráo, không có mưa: Đây là thời điểm rất thích hợp để rửa xe máy, ô tô cho sạch sẽ.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
