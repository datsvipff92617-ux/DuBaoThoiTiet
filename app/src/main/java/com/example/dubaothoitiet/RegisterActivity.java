package com.example.dubaothoitiet;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Đăng Ký Tài Khoản (Member 1)
 * Lưu trữ thông tin người dùng mới vào SQLite, hỗ trợ phân quyền Admin/User theo ý muốn
 */
public class RegisterActivity extends BaseActivity {

    private EditText etRegUsername, etRegFullName, etRegEmail, etRegPhone, etRegLocation, etRegPassword, etRegConfirmPassword;
    private RadioGroup rgAccountRole;
    private RadioButton rbRoleUser, rbRoleAdmin;
    private Button btnSubmitRegister;
    private TextView tvBackToLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegFullName = findViewById(R.id.etRegFullName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegLocation = findViewById(R.id.etRegLocation);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        rgAccountRole = findViewById(R.id.rgAccountRole);
        rbRoleUser = findViewById(R.id.rbRoleUser);
        rbRoleAdmin = findViewById(R.id.rbRoleAdmin);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        if (isEnglish()) {
            TextView tvRegisterTitle = findViewById(R.id.tvRegisterTitle);
            if (tvRegisterTitle != null) tvRegisterTitle.setText("📝 Create New Account");
            if (etRegUsername != null) etRegUsername.setHint("Username (*)");
            if (etRegFullName != null) etRegFullName.setHint("Full Name (*)");
            if (etRegEmail != null) etRegEmail.setHint("Email address");
            if (etRegPhone != null) etRegPhone.setHint("Phone number");
            if (etRegLocation != null) etRegLocation.setHint("City / Living Area");
            if (etRegPassword != null) etRegPassword.setHint("Password (* min 6 chars)");
            if (etRegConfirmPassword != null) etRegConfirmPassword.setHint("Confirm password (*)");
            if (rbRoleUser != null) rbRoleUser.setText("Regular User (User)");
            if (rbRoleAdmin != null) rbRoleAdmin.setText("System Administrator (Admin)");
            if (btnSubmitRegister != null) btnSubmitRegister.setText("✅ Register Account");
            if (tvBackToLogin != null) tvBackToLogin.setText("⬅️ Already have an account? Back to Login");
        }
    }

    private void setupListeners() {
        btnSubmitRegister.setOnClickListener(v -> processRegister());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void processRegister() {
        String username = etRegUsername.getText().toString().trim();
        String fullName = etRegFullName.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String phone = etRegPhone.getText().toString().trim();
        String location = etRegLocation.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        // Xác định vai trò tài khoản được chọn
        String role = (rbRoleAdmin != null && rbRoleAdmin.isChecked()) ? "admin" : "user";

        // Validate dữ liệu
        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Tên đăng nhập phải có ít nhất 3 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có độ dài tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra tên đăng nhập đã có trong SQLite chưa
        if (databaseHelper.checkUsernameExists(username)) {
            Toast.makeText(this, "Tên đăng nhập này đã được sử dụng! Vui lòng chọn tên khác.", Toast.LENGTH_LONG).show();
            return;
        }

        // Lưu vào CSDL SQLite kèm vai trò (Admin hoặc User)
        boolean success = databaseHelper.registerUser(username, password, fullName, email, phone, location, role);
        if (success) {
            String roleText = role.equals("admin") ? "👑 Quản trị viên (Admin)" : "Người dùng";
            Toast.makeText(this, "Đăng ký thành công tài khoản " + roleText + "! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();

            // Truyền tên đăng nhập về LoginActivity để tiện đăng nhập
            Intent resultIntent = new Intent();
            resultIntent.putExtra("registered_username", username);
            setResult(RESULT_OK, resultIntent);
            finish(); // Trở về màn hình Login
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi lưu dữ liệu. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
}

