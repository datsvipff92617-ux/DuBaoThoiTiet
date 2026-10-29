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

        if (isEnglish()) {
            android.widget.TextView tvDisasterHeaderTitle = findViewById(R.id.tvDisasterHeaderTitle);
            android.widget.TextView tvEmergencyHotlineLabel = findViewById(R.id.tvEmergencyHotlineLabel);
            if (tvDisasterHeaderTitle != null) tvDisasterHeaderTitle.setText("🚨 Disaster Survival Guide");
            if (tvEmergencyHotlineLabel != null) tvEmergencyHotlineLabel.setText("📞 EMERGENCY RESCUE HOTLINES (Tap to dial):");

            if (btnCall112 != null) btnCall112.setText("🆘 112\nRescue");
            if (btnCall114 != null) btnCall114.setText("🚒 114\nFire");
            if (btnCall115 != null) btnCall115.setText("🚑 115\nAmbulance");

            android.widget.TextView s1Title = findViewById(R.id.tvDisasterSec1Title);
            android.widget.TextView s1Body = findViewById(R.id.tvDisasterSec1Body);
            if (s1Title != null) s1Title.setText("⚡ 1. THUNDERSTORMS & HAZARDOUS LIGHTNING SAFETY");
            if (s1Body != null) s1Body.setText("• 30-30 Rule: If time between lightning flash and thunder is under 30 seconds, seek indoor shelter immediately.\n• NEVER take shelter under tall trees, utility poles, or metal billboards.\n• In open fields: Crouch down into a ball with feet together; do NOT lie flat on the ground.\n• Indoors: Unplug TVs and computers; avoid using charging phones or corded equipment.");

            android.widget.TextView s2Title = findViewById(R.id.tvDisasterSec2Title);
            android.widget.TextView s2Body = findViewById(R.id.tvDisasterSec2Body);
            if (s2Title != null) s2Title.setText("🌀 2. TYPHOONS & TROPICAL STORM PREPAREDNESS");
            if (s2Body != null) s2Body.setText("• Secure roofs, tape X-patterns on glass windows to prevent shattering.\n• Prepare a 3-day survival kit: Flashlight, power bank, drinking water, and dry food rations.\n• Trim tree branches near your house that risk snapping in gale winds.\n• Constantly monitor the app's real-time forecast for storm track and severity.");

            android.widget.TextView s3Title = findViewById(R.id.tvDisasterSec3Title);
            android.widget.TextView s3Body = findViewById(R.id.tvDisasterSec3Body);
            if (s3Title != null) s3Title.setText("🌊 3. URBAN FLOOD & WATERLOGGING SAFETY");
            if (s3Body != null) s3Body.setText("• Watch out for displaced manhole covers forced open by rushing water pressure.\n• Switch off main circuit breakers if floodwaters rise above ground floor electrical sockets.\n• Do NOT drive motorbikes or cars through deep water exceeding half-wheel depth (hydro-lock risk).\n• Keep distance from street lighting poles and submerged wiring (electrocution hazard).");

            android.widget.TextView s4Title = findViewById(R.id.tvDisasterSec4Title);
            android.widget.TextView s4Body = findViewById(R.id.tvDisasterSec4Body);
            if (s4Title != null) s4Title.setText("☀️ 4. SEVERE HEATWAVE & HEATSTROKE PREVENTION");
            if (s4Body != null) s4Body.setText("• Minimize strenuous outdoor work or exertion between 11:00 AM and 3:00 PM.\n• Drink 2 - 2.5 liters of water daily; replenish with electrolyte fluids when sweating heavily.\n• Signs of heatstroke: Dizziness, nausea, hot red skin without sweating -> Move to cool shade and apply cold compresses to neck and underarms immediately.");
        }
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
            Toast.makeText(this, (isEnglish() ? "Cannot open dialer: " : "Không thể mở trình quay số: ") + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
