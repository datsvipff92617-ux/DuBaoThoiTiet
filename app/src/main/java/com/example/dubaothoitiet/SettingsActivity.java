package com.example.dubaothoitiet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class SettingsActivity extends BaseActivity {

    private RadioGroup tempUnitGroup, windSpeedGroup, pressureGroup, themeGroup, languageGroup;
    private RadioButton rbCelsius, rbFahrenheit;
    private RadioButton rbKmh, rbMph, rbMs;
    private RadioButton rbHpa, rbInHg, rbMbar;
    private RadioButton rbLight, rbDark, rbSystem;
    private RadioButton rbVietnamese, rbEnglish;
    private Button btnSave, btnBack;
    private ImageView btnBackArrow;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_TEMP_UNIT = "temp_unit";
    private static final String KEY_WIND_UNIT = "wind_unit";
    private static final String KEY_PRESSURE_UNIT = "pressure_unit";
    private static final String KEY_THEME = "theme";
    private static final String KEY_LANGUAGE = "language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSettings();
        setupListeners();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "vi");

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;
        Context context = newBase.createConfigurationContext(config);

        super.attachBaseContext(context);
    }

    private void initViews() {
        btnBackArrow = findViewById(R.id.btnBackArrow);

        tempUnitGroup = findViewById(R.id.tempUnitGroup);
        rbCelsius = findViewById(R.id.rbCelsius);
        rbFahrenheit = findViewById(R.id.rbFahrenheit);

        windSpeedGroup = findViewById(R.id.windSpeedGroup);
        rbKmh = findViewById(R.id.rbKmh);
        rbMph = findViewById(R.id.rbMph);
        rbMs = findViewById(R.id.rbMs);

        pressureGroup = findViewById(R.id.pressureGroup);
        rbHpa = findViewById(R.id.rbHpa);
        rbInHg = findViewById(R.id.rbInHg);
        rbMbar = findViewById(R.id.rbMbar);

        themeGroup = findViewById(R.id.themeGroup);
        rbLight = findViewById(R.id.rbLight);
        rbDark = findViewById(R.id.rbDark);
        rbSystem = findViewById(R.id.rbSystem);

        languageGroup = findViewById(R.id.languageGroup);
        rbVietnamese = findViewById(R.id.rbVietnamese);
        rbEnglish = findViewById(R.id.rbEnglish);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadSettings() {
        String tempUnit = preferences.getString(KEY_TEMP_UNIT, "C");
        if (tempUnit.equals("C")) {
            rbCelsius.setChecked(true);
        } else {
            rbFahrenheit.setChecked(true);
        }

        String windUnit = preferences.getString(KEY_WIND_UNIT, "kmh");
        switch (windUnit) {
            case "kmh":
                rbKmh.setChecked(true);
                break;
            case "mph":
                rbMph.setChecked(true);
                break;
            case "ms":
                rbMs.setChecked(true);
                break;
        }

        String pressureUnit = preferences.getString(KEY_PRESSURE_UNIT, "hPa");
        switch (pressureUnit) {
            case "hPa":
                rbHpa.setChecked(true);
                break;
            case "inHg":
                rbInHg.setChecked(true);
                break;
            case "mbar":
                rbMbar.setChecked(true);
                break;
        }

        int theme = preferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (theme == AppCompatDelegate.MODE_NIGHT_NO) {
            rbLight.setChecked(true);
        } else if (theme == AppCompatDelegate.MODE_NIGHT_YES) {
            rbDark.setChecked(true);
        } else {
            rbSystem.setChecked(true);
        }

        String language = preferences.getString(KEY_LANGUAGE, "vi");
        if (language.equals("vi")) {
            rbVietnamese.setChecked(true);
        } else {
            rbEnglish.setChecked(true);
        }
    }

    private void setupListeners() {
        btnBackArrow.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSettings());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();

        String tempUnit = rbCelsius.isChecked() ? "C" : "F";
        editor.putString(KEY_TEMP_UNIT, tempUnit);

        String windUnit;
        if (rbKmh.isChecked()) {
            windUnit = "kmh";
        } else if (rbMph.isChecked()) {
            windUnit = "mph";
        } else {
            windUnit = "ms";
        }
        editor.putString(KEY_WIND_UNIT, windUnit);

        String pressureUnit;
        if (rbHpa.isChecked()) {
            pressureUnit = "hPa";
        } else if (rbInHg.isChecked()) {
            pressureUnit = "inHg";
        } else {
            pressureUnit = "mbar";
        }
        editor.putString(KEY_PRESSURE_UNIT, pressureUnit);

        int oldTheme = preferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int theme;
        if (rbLight.isChecked()) {
            theme = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (rbDark.isChecked()) {
            theme = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        editor.putInt(KEY_THEME, theme);

        String language = rbVietnamese.isChecked() ? "vi" : "en";
        String oldLanguage = preferences.getString(KEY_LANGUAGE, "vi");
        editor.putString(KEY_LANGUAGE, language);

        editor.apply();

        AppCompatDelegate.setDefaultNightMode(theme);

        if (!language.equals(oldLanguage) || theme != oldTheme) {
            Toast.makeText(this, "Đã áp dụng giao diện mới trên tất cả các trang!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity();
        } else {
            Toast.makeText(this, "Đã lưu cài đặt", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void applyTheme() {
        int theme = preferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    public static boolean isEnglish(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return "en".equals(prefs.getString(KEY_LANGUAGE, "vi"));
    }

    public static String getTempUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TEMP_UNIT, "C");
    }

    public static String getWindUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_WIND_UNIT, "kmh");
    }

    public static String getPressureUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PRESSURE_UNIT, "hPa");
    }

    public static double convertTemp(double celsius, String unit) {
        if (unit.equals("F")) {
            return celsius * 9 / 5 + 32;
        }
        return celsius;
    }

    @SuppressLint("DefaultLocale")
    public static String formatTemp(Context context, double celsius) {
        String unit = getTempUnit(context);
        double temp = convertTemp(celsius, unit);
        return String.format("%.0f°%s", temp, unit);
    }

    public static double convertWindSpeed(double kmh, String unit) {
        switch (unit) {
            case "mph":
                return kmh * 0.621371;
            case "ms":
                return kmh * 0.277778;
            default:
                return kmh;
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatWindSpeed(Context context, double kmh) {
        String unit = getWindUnit(context);
        double speed = convertWindSpeed(kmh, unit);

        switch (unit) {
            case "mph":
                return String.format("%.1f mph", speed);
            case "ms":
                return String.format("%.1f m/s", speed);
            default:
                return String.format("%.1f km/h", speed);
        }
    }

    // Pressure conversion
    public static double convertPressure(double mb, String unit) {
        switch (unit) {
            case "inHg":
                return mb * 0.02953;
            case "mbar":
                return mb;
            default:
                return mb;
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatPressure(Context context, double mb) {
        String unit = getPressureUnit(context);
        double pressure = convertPressure(mb, unit);

        switch (unit) {
            case "inHg":
                return String.format("%.2f inHg", pressure);
            case "mbar":
                return String.format("%.0f mbar", pressure);
            default:
                return String.format("%.0f hPa", pressure);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}