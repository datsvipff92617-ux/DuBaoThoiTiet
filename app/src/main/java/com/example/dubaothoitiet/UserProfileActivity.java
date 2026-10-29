package com.example.dubaothoitiet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình Hồ Sơ Cá Nhân & Đổi Mật Khẩu (Member 1)
 */
public class UserProfileActivity extends BaseActivity {

    private ImageButton btnBackProfile;
    private TextView tvProfileUsername, tvProfileRole, tvProfileCreatedAt;
    private EditText etProfileFullName, etProfileEmail, etProfilePhone, etProfileLocation;
    private Button btnSaveProfile, btnChangePassword, btnLogoutProfile;
    private EditText etOldPassword, etNewPassword, etConfirmNewPassword;

    private DatabaseHelper databaseHelper;
    private SharedPreferences preferences;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        databaseHelper = new DatabaseHelper(this);
        preferences = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        currentUsername = preferences.getString(LoginActivity.KEY_USERNAME, "sinhvien");

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        btnBackProfile = findViewById(R.id.btnBackProfile);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvProfileCreatedAt = findViewById(R.id.tvProfileCreatedAt);

        etProfileFullName = findViewById(R.id.etProfileFullName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileLocation = findViewById(R.id.etProfileLocation);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);

        TextView tvProfileHeaderTitle = findViewById(R.id.tvProfileHeaderTitle);
        TextView tvProfileBasicInfoTitle = findViewById(R.id.tvProfileBasicInfoTitle);
        TextView tvLabelFullName = findViewById(R.id.tvLabelFullName);
        TextView tvLabelEmail = findViewById(R.id.tvLabelEmail);
        TextView tvLabelPhone = findViewById(R.id.tvLabelPhone);
        TextView tvLabelLocation = findViewById(R.id.tvLabelLocation);
        TextView tvProfileChangePassTitle = findViewById(R.id.tvProfileChangePassTitle);

        if (isEnglish()) {
            if (tvProfileHeaderTitle != null) tvProfileHeaderTitle.setText("👤 User Profile");
            if (tvProfileBasicInfoTitle != null) tvProfileBasicInfoTitle.setText("Basic Information");
            if (tvLabelFullName != null) tvLabelFullName.setText("Full Name");
            if (tvLabelEmail != null) tvLabelEmail.setText("Email");
            if (tvLabelPhone != null) tvLabelPhone.setText("Phone Number");
            if (tvLabelLocation != null) tvLabelLocation.setText("Living Area / City");
            if (tvProfileChangePassTitle != null) tvProfileChangePassTitle.setText("🔐 Change Password");

            if (etOldPassword != null) etOldPassword.setHint("Current Password");
            if (etNewPassword != null) etNewPassword.setHint("New Password (min 6 chars)");
            if (etConfirmNewPassword != null) etConfirmNewPassword.setHint("Confirm New Password");

            if (btnSaveProfile != null) btnSaveProfile.setText("💾 Save Changes");
            if (btnChangePassword != null) btnChangePassword.setText("🔑 Change Password");
            if (btnLogoutProfile != null) btnLogoutProfile.setText("🚪 Log Out");
        }
    }

    private void loadUserData() {
        User user = databaseHelper.getUserByUsername(currentUsername);
        boolean en = isEnglish();
        if (user != null) {
            tvProfileUsername.setText(user.getUsername());
            tvProfileRole.setText(user.isAdmin() ? (en ? "Role: ⭐ Administrator (Admin)" : "Vai trò: ⭐ Quản trị viên (Admin)") : (en ? "Role: 👤 Member" : "Vai trò: 👤 Thành viên"));
            tvProfileCreatedAt.setText((en ? "Joined: " : "Ngày tham gia: ") + (user.getCreatedAt() != null ? user.getCreatedAt() : (en ? "Recently" : "Mới đây")));

            etProfileFullName.setText(user.getFullName() != null ? user.getFullName() : "");
            etProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            etProfilePhone.setText(user.getPhone() != null ? user.getPhone() : "");
            etProfileLocation.setText(user.getLocation() != null ? user.getLocation() : "");
        } else {
            tvProfileUsername.setText(currentUsername);
            tvProfileRole.setText(en ? "Guest User" : "Khách vãng lai");
        }
    }

    private void setupListeners() {
        btnBackProfile.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnChangePassword.setOnClickListener(v -> processChangePassword());

        btnLogoutProfile.setOnClickListener(v -> confirmLogout());
    }

    private void saveProfile() {
        String fullName = etProfileFullName.getText().toString().trim();
        String email = etProfileEmail.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();
        String location = etProfileLocation.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, isEnglish() ? "Full name cannot be empty" : "Họ và tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.updateUserProfile(currentUsername, fullName, email, phone, location);
        if (success) {
            Toast.makeText(this, isEnglish() ? "Profile saved successfully!" : "Đã lưu cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, isEnglish() ? "Could not update profile right now" : "Không thể cập nhật thông tin lúc này", Toast.LENGTH_SHORT).show();
        }
    }

    private void processChangePassword() {
        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmNewPassword.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, isEnglish() ? "Please fill in all password fields" : "Vui lòng nhập đầy đủ các trường đổi mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, isEnglish() ? "New password must be at least 6 characters" : "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, isEnglish() ? "Confirmed password does not match" : "Mật khẩu xác nhận không khớp với mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.changePassword(currentUsername, oldPass, newPass);
        if (success) {
            Toast.makeText(this, isEnglish() ? "Password changed successfully!" : "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
            etOldPassword.setText("");
            etNewPassword.setText("");
            etConfirmNewPassword.setText("");
        } else {
            Toast.makeText(this, isEnglish() ? "Current password is incorrect!" : "Mật khẩu hiện tại không chính xác!", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmLogout() {
        boolean en = isEnglish();
        new AlertDialog.Builder(this)
                .setTitle(en ? "Logout" : "Đăng xuất")
                .setMessage(en ? "Are you sure you want to log out of this account?" : "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?")
                .setPositiveButton(en ? "Logout" : "Đăng xuất", (dialog, which) -> {
                    preferences.edit().clear().apply();
                    Toast.makeText(this, en ? "Logged out" : "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(en ? "Cancel" : "Hủy", null)
                .show();
    }
}
