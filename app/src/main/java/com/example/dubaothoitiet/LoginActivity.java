package com.example.dubaothoitiet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Màn hình Đăng Nhập (Member 1)
 * Xác thực tài khoản người dùng qua SQLite Database, hỗ trợ đăng nhập 1 chạm
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

    private static final int REQ_REGISTER = 101;

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvSkipLogin = findViewById(R.id.tvSkipLogin);

        if (isEnglish()) {
            TextView tvLoginAppTitle = findViewById(R.id.tvLoginAppTitle);
            TextView tvLoginTitle = findViewById(R.id.tvLoginTitle);
            TextView tvDemoHint = findViewById(R.id.tvDemoHint);

            if (tvLoginAppTitle != null) tvLoginAppTitle.setText("☀️ Weather Forecast");
            if (tvLoginTitle != null) tvLoginTitle.setText("Login");
            if (etUsername != null) etUsername.setHint("Username");
            if (etPassword != null) etPassword.setHint("Password");
            if (btnLogin != null) btnLogin.setText("Login");
            if (btnRegister != null) btnRegister.setText("📝 Create New Account (Register)");
            if (tvDemoHint != null) tvDemoHint.setText("💡 You can register an account (Admin or User), or use demo: admin / 123456");
            if (tvSkipLogin != null) tvSkipLogin.setText("🚀 Skip login (Explore as Guest) →");
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String u = etUsername.getText().toString().trim();
            String p = etPassword.getText().toString().trim();
            loginWithCredentials(u, p);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REQ_REGISTER);
        });

        tvSkipLogin.setOnClickListener(v -> {
            // Đăng nhập với tư cách Khách
            preferences.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_USERNAME, "Khách")
                    .putString(KEY_ROLE, "guest")
                    .apply();
            Toast.makeText(this, isEnglish() ? "Entering app as Guest" : "Đang vào ứng dụng với tư cách Khách", Toast.LENGTH_SHORT).show();
            goToMainActivity();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_REGISTER && resultCode == RESULT_OK && data != null) {
            String regUsername = data.getStringExtra("registered_username");
            if (regUsername != null && !regUsername.isEmpty()) {
                etUsername.setText(regUsername);
                etPassword.requestFocus();
            }
        }
    }


    private void loginWithCredentials(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra tài khoản trong SQLite Database
        User user = databaseHelper.checkLogin(username, password);

        // Nếu là tài khoản mẫu mặc định mà DB chưa kịp khởi tạo thì tự động cấp quyền
        if (user == null) {
            if ("admin".equalsIgnoreCase(username) && "123456".equals(password)) {
                user = new User(1, "admin", "123456", "Quản Trị Viên", "admin@dubaothoitiet.vn", "0988888888", "Hà Nội", "admin", "27/10/2026");
            } else if ("sinhvien".equalsIgnoreCase(username) && "123456".equals(password)) {
                user = new User(2, "sinhvien", "123456", "Nguyễn Văn Sinh Viên", "sinhvien@gmail.com", "0912345678", "TP. Hồ Chí Minh", "user", "27/10/2026");
            }
        }

        if (user != null) {
            preferences.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_USERNAME, user.getUsername())
                    .putString(KEY_ROLE, user.getRole())
                    .apply();

            String welcomeMsg = user.isAdmin() ? "👑 Xin chào Quản trị viên: " + user.getFullName()
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