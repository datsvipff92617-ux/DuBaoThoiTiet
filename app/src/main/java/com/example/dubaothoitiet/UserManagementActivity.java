package com.example.dubaothoitiet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Quản Trị Người Dùng - Dành cho Quản trị viên (Admin) (Member 5)
 * Xem, tìm kiếm và xóa tài khoản người dùng trong hệ thống SQLite
 */
public class UserManagementActivity extends BaseActivity {

    private ImageButton btnBackUserMgmt;
    private TextView tvUserCountStats, tvEmptyUsers;
    private EditText etSearchUser;
    private ListView lvUsers;

    private DatabaseHelper databaseHelper;
    private UserAdapter userAdapter;
    private List<User> allUsersList;
    private List<User> displayList;
    private String currentLoggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        databaseHelper = new DatabaseHelper(this);
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String role = userPrefs.getString("role", "user");
        if (!"admin".equalsIgnoreCase(role)) {
            Toast.makeText(this, "⚠️ Quyền truy cập bị từ chối! Chức năng này chỉ dành riêng cho Quản trị viên (Admin).", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        currentLoggedInUsername = prefs.getString(LoginActivity.KEY_USERNAME, "admin");

        initViews();
        setupListeners();
        loadUsers();
    }

    private void initViews() {
        btnBackUserMgmt = findViewById(R.id.btnBackUserMgmt);
        tvUserCountStats = findViewById(R.id.tvUserCountStats);
        tvEmptyUsers = findViewById(R.id.tvEmptyUsers);
        etSearchUser = findViewById(R.id.etSearchUser);
        lvUsers = findViewById(R.id.lvUsers);

        allUsersList = new ArrayList<>();
        displayList = new ArrayList<>();

        userAdapter = new UserAdapter(this, displayList, user -> confirmDeleteUser(user));
        lvUsers.setAdapter(userAdapter);
    }

    private void setupListeners() {
        btnBackUserMgmt.setOnClickListener(v -> finish());

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        allUsersList.clear();
        allUsersList.addAll(databaseHelper.getAllUsers());

        tvUserCountStats.setText("📊 Tổng số tài khoản trong hệ thống: " + allUsersList.size());
        filterUsers(etSearchUser.getText().toString());
    }

    private void filterUsers(String query) {
        displayList.clear();
        String q = query.trim().toLowerCase();

        if (q.isEmpty()) {
            displayList.addAll(allUsersList);
        } else {
            for (User u : allUsersList) {
                boolean matchUsername = u.getUsername() != null && u.getUsername().toLowerCase().contains(q);
                boolean matchFullName = u.getFullName() != null && u.getFullName().toLowerCase().contains(q);
                boolean matchEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(q);
                if (matchUsername || matchFullName || matchEmail) {
                    displayList.add(u);
                }
            }
        }

        userAdapter.notifyDataSetChanged();
        tvEmptyUsers.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDeleteUser(User user) {
        if (user.getUsername().equalsIgnoreCase(currentLoggedInUsername)) {
            Toast.makeText(this, "Không thể xóa tài khoản bạn đang đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản @" + user.getUsername() + " (" + user.getFullName() + ")?")
                .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                    boolean success = databaseHelper.deleteUser(user.getId());
                    if (success) {
                        Toast.makeText(this, "Đã xóa tài khoản @" + user.getUsername(), Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Không thể xóa tài khoản này!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
