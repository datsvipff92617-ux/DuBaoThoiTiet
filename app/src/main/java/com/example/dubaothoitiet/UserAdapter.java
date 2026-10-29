package com.example.dubaothoitiet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Adapter hiển thị danh sách tài khoản người dùng cho màn hình Quản trị Admin
 */
public class UserAdapter extends ArrayAdapter<User> {

    public interface OnUserActionListener {
        void onDeleteUser(User user);
    }

    private final Context context;
    private final List<User> userList;
    private final OnUserActionListener listener;

    public UserAdapter(@NonNull Context context, @NonNull List<User> objects, OnUserActionListener listener) {
        super(context, 0, objects);
        this.context = context;
        this.userList = objects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        }

        User user = userList.get(position);
        if (user != null) {
            TextView tvAvatar = convertView.findViewById(R.id.tvUserItemAvatar);
            TextView tvUsername = convertView.findViewById(R.id.tvUserItemUsername);
            TextView tvRole = convertView.findViewById(R.id.tvUserItemRole);
            TextView tvFullName = convertView.findViewById(R.id.tvUserItemFullName);
            TextView tvContact = convertView.findViewById(R.id.tvUserItemContact);
            Button btnDelete = convertView.findViewById(R.id.btnDeleteUser);

            boolean en = SettingsActivity.isEnglish(context);
            tvUsername.setText(user.getUsername());
            tvFullName.setText(user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : (en ? "No full name set" : "Chưa đặt họ tên"));

            String contact = "";
            if (user.getEmail() != null && !user.getEmail().isEmpty()) contact += user.getEmail();
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                if (!contact.isEmpty()) contact += " | ";
                contact += user.getPhone();
            }
            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                if (!contact.isEmpty()) contact += " | ";
                contact += en ? VietnamLocations.toEnglishLocation(user.getLocation()) : user.getLocation();
            }
            tvContact.setText(contact.isEmpty() ? (en ? "No contact information" : "Chưa có thông tin liên hệ") : contact);

            if (user.isAdmin()) {
                tvAvatar.setText("👑");
                tvRole.setText("ADMIN");
                tvRole.setTextColor(Color.parseColor("#FFEB3B"));
                btnDelete.setVisibility(View.GONE); // Không cho xóa Admin
            } else {
                tvAvatar.setText("👤");
                tvRole.setText("USER");
                tvRole.setTextColor(Color.parseColor("#80D8FF"));
                btnDelete.setText(en ? "Delete" : "Xóa");
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteUser(user);
                    }
                });
            }
        }

        return convertView;
    }
}
