package com.example.dubaothoitiet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Đăng Nhập (Member 1)
 * Xác thực tài khoản người dùng qua SQLite Database
 */
public class LoginActivity extends BaseActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private TextView tvSkipLogin;
    private SharedPreferences preferences;
    private DatabaseHelper databaseHelper;

    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_ROLE = "role";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Nếu đã đăng nhập trước đó thì vào thẳng MainActivity
        if (isLoggedIn()) {
            goToMainActivity();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvSkipLogin = findViewById(R.id.tvSkipLogin);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        tvSkipLogin.setOnClickListener(v -> {
            // Đăng nhập với tư cách Khách
            preferences.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, false)
                    .putString(KEY_USERNAME, "Khách")
                    .putString(KEY_ROLE, "guest")
                    .apply();
            goToMainActivity();
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra tài khoản trong SQLite Database
        User user = databaseHelper.checkLogin(username, password);

        if (user != null) {
            preferences.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_USERNAME, user.getUsername())
                    .putString(KEY_ROLE, user.getRole())
                    .apply();

            String welcomeMsg = user.isAdmin() ? "Xin chào Quản trị viên: " + user.getFullName()
                    : "Đăng nhập thành công! Chào " + (user.getFullName() != null ? user.getFullName() : user.getUsername());
            Toast.makeText(this, welcomeMsg, Toast.LENGTH_SHORT).show();
            goToMainActivity();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}