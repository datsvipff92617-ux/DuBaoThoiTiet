package com.example.dubaothoitiet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lớp quản trị cơ sở dữ liệu SQLite cho ứng dụng Thời Tiết
 * Quản lý tài khoản người dùng, phân quyền Admin/User và lịch sử tìm kiếm
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WeatherApp.db";
    private static final int DATABASE_VERSION = 1;

    // Bảng Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_FULLNAME = "fullname";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Bảng Search History
    public static final String TABLE_HISTORY = "search_history";
    public static final String COLUMN_HISTORY_ID = "id";
    public static final String COLUMN_CITY_NAME = "city_name";
    public static final String COLUMN_SEARCHED_AT = "searched_at";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users
        String createTableUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_FULLNAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_ROLE + " TEXT DEFAULT 'user', " +
                COLUMN_CREATED_AT + " TEXT);";
        db.execSQL(createTableUsers);

        // Tạo bảng Search History
        String createTableHistory = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                COLUMN_SEARCHED_AT + " TEXT);";
        db.execSQL(createTableHistory);

        // Chèn tài khoản mẫu Admin và User
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USERNAME, "admin");
        adminValues.put(COLUMN_PASSWORD, "123456");
        adminValues.put(COLUMN_FULLNAME, "Quản Trị Viên");
        adminValues.put(COLUMN_EMAIL, "admin@dubaothoitiet.vn");
        adminValues.put(COLUMN_PHONE, "0988888888");
        adminValues.put(COLUMN_LOCATION, "Hà Nội");
        adminValues.put(COLUMN_ROLE, "admin");
        adminValues.put(COLUMN_CREATED_AT, currentDate);
        db.insert(TABLE_USERS, null, adminValues);

        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USERNAME, "sinhvien");
        userValues.put(COLUMN_PASSWORD, "123456");
        userValues.put(COLUMN_FULLNAME, "Nguyễn Văn Sinh Viên");
        userValues.put(COLUMN_EMAIL, "sinhvien@gmail.com");
        userValues.put(COLUMN_PHONE, "0912345678");
        userValues.put(COLUMN_LOCATION, "TP. Hồ Chí Minh");
        userValues.put(COLUMN_ROLE, "user");
        userValues.put(COLUMN_CREATED_AT, currentDate);
        db.insert(TABLE_USERS, null, userValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ TÀI KHOẢN NGƯỜI DÙNG (USERS)
    // ==========================================

    /**
     * Kiểm tra tên đăng nhập đã tồn tại chưa
     */
    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?", new String[]{username},
                null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    /**
     * Đăng ký người dùng mới
     */
    public boolean registerUser(String username, String password, String fullname, String email, String phone, String location) {
        if (checkUsernameExists(username)) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_FULLNAME, fullname);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_ROLE, "user");
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        values.put(COLUMN_CREATED_AT, currentDate);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /**
     * Kiểm tra đăng nhập (trả về User nếu đúng, null nếu sai)
     */
    public User checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    /**
     * Lấy thông tin User theo Username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    /**
     * Cập nhật thông tin cá nhân (Profile)
     */
    public boolean updateUserProfile(String username, String fullname, String email, String phone, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULLNAME, fullname);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_LOCATION, location);

        int rows = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
        return rows > 0;
    }

    /**
     * Đổi mật khẩu
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Kiểm tra mật khẩu cũ trước
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, oldPassword},
                null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, newPassword);
            int rows = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
            return rows > 0;
        }
        if (cursor != null) cursor.close();
        return false;
    }

    /**
     * Lấy danh sách toàn bộ người dùng (Dành cho Quản trị viên - Admin)
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " ORDER BY " + COLUMN_USER_ID + " DESC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * Xóa một tài khoản theo User ID (Admin)
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USERS, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ LỊCH SỬ TÌM KIẾM
    // ==========================================

    /**
     * Thêm thành phố vào lịch sử tìm kiếm
     */
    public void addSearchHistory(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) return;
        SQLiteDatabase db = this.getWritableDatabase();

        // Xóa nếu đã có trước đó để tránh trùng lặp
        db.delete(TABLE_HISTORY, COLUMN_CITY_NAME + "=?", new String[]{cityName.trim()});

        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_NAME, cityName.trim());
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        values.put(COLUMN_SEARCHED_AT, currentDate);
        db.insert(TABLE_HISTORY, null, values);
    }

    /**
     * Lấy danh sách các địa điểm đã tìm kiếm gần nhất
     */
    public List<String> getSearchHistory() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_CITY_NAME + " FROM " + TABLE_HISTORY + " ORDER BY " + COLUMN_HISTORY_ID + " DESC LIMIT 10", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * Xóa toàn bộ lịch sử tìm kiếm
     */
    public void clearSearchHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
    }

    /**
     * Helper chuyển Cursor thành đối tượng User
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
        user.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        return user;
    }
}
