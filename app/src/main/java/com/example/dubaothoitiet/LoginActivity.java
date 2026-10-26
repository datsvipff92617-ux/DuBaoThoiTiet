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
    private Button btnQuickAdmin, btnQuickStudent;
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
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin);
        btnQuickStudent = findViewById(R.id.btnQuickStudent);
        tvSkipLogin = findViewById(R.id.tvSkipLogin);

        // Điền sẵn tài khoản mẫu để người dùng/giảng viên mở app là vào được ngay
        if (etUsername.getText().toString().isEmpty()) {
            etUsername.setText("sinhvien");
        }
        if (etPassword.getText().toString().isEmpty()) {
            etPassword.setText("123456");
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String u = etUsername.getText().toString().trim();
            String p = etPassword.getText().toString().trim();
            loginWithCredentials(u, p);
        });

        // 1 Chạm vào thẳng Admin
        if (btnQuickAdmin != null) {
            btnQuickAdmin.setOnClickListener(v -> {
                etUsername.setText("admin");
                etPassword.setText("123456");
                loginWithCredentials("admin", "123456");
            });
        }

        // 1 Chạm vào thẳng Sinh viên
        if (btnQuickStudent != null) {
            btnQuickStudent.setOnClickListener(v -> {
                etUsername.setText("sinhvien");
                etPassword.setText("123456");
                loginWithCredentials("sinhvien", "123456");
            });
        }

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvSkipLogin.setOnClickListener(v -> {
            // Đăng nhập với tư cách Khách
            preferences.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_USERNAME, "Khách")
                    .putString(KEY_ROLE, "guest")
                    .apply();
            Toast.makeText(this, "Đang vào ứng dụng với tư cách Khách", Toast.LENGTH_SHORT).show();
            goToMainActivity();
        });
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