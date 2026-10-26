package com.example.dubaothoitiet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Màn hình Cẩm Nang Sinh Tồn & Phòng Chống Thiên Tai (Member 5)
 * Cung cấp kỹ năng ứng phó bão, dông sét, ngập lụt, say nắng và gọi số khẩn cấp 112/114/115
 */
public class DisasterGuideActivity extends BaseActivity {

    private ImageButton btnBackDisaster;
    private Button btnCall112, btnCall114, btnCall115;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disaster_guide);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBackDisaster = findViewById(R.id.btnBackDisaster);
        btnCall112 = findViewById(R.id.btnCall112);
        btnCall114 = findViewById(R.id.btnCall114);
        btnCall115 = findViewById(R.id.btnCall115);
    }

    private void setupListeners() {
        btnBackDisaster.setOnClickListener(v -> finish());

        btnCall112.setOnClickListener(v -> dialPhoneNumber("112"));
        btnCall114.setOnClickListener(v -> dialPhoneNumber("114"));
        btnCall115.setOnClickListener(v -> dialPhoneNumber("115"));
    }

    private void dialPhoneNumber(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở trình quay số: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
