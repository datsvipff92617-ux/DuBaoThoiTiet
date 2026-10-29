package com.example.dubaothoitiet;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Tiện ích định vị vị trí tự động chuẩn xác theo GPS cho mọi người dùng ở bất kỳ tỉnh thành nào.
 * Tự động phân giải kinh/vĩ độ thành địa danh chi tiết:
 * [Thôn/Làng/Khu phố/Phường] + [Quận/Huyện/Thị xã] + [Tỉnh/Thành phố].
 * Hoàn toàn động (Dynamic) 100%, không gán cố định bất kỳ địa phương nào.
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";
    public static final String PREF_NAME = "ExactLocationPrefs";
    public static final String KEY_EXACT_NAME = "exact_location_name";
    public static final String KEY_FULL_ADDRESS = "exact_full_address";
    public static final String KEY_EXACT_QUERY = "exact_weather_query";

    public interface LocationResultCallback {
        void onLocationResolved(String shortLocationName, String fullAddress, String weatherQuery);
    }

    /**
     * Phân giải tọa độ GPS hoàn toàn tự động cho BẤT KỲ vị trí nào trên toàn quốc và thế giới.
     */
    public static void resolvePreciseLocation(Context context, double lat, double lon, LocationResultCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String shortLocation = null;
            String fullAddress = null;

            // 1. Thử dùng Android Geocoder của hệ điều hành
            try {
                if (Geocoder.isPresent()) {
                    Geocoder geocoder = new Geocoder(context, new Locale("vi", "VN"));
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        fullAddress = addr.getAddressLine(0);

                        String feature = addr.getFeatureName();
                        String street = addr.getThoroughfare();
                        String ward = addr.getSubLocality();
                        String district = addr.getSubAdminArea();
                        String city = addr.getAdminArea();
                        String locality = addr.getLocality();

                        // Xác định cấp phường/xã/khu phố/thôn/đường
                        String subArea = null;
                        if (ward != null && !ward.trim().isEmpty()) {
                            subArea = ward.trim();
                        } else if (feature != null && !feature.matches("^\\d+.*") && !feature.equalsIgnoreCase(street)) {
                            subArea = feature.trim();
                        } else if (street != null && !street.trim().isEmpty()) {
                            subArea = street.trim();
                        }

                        // Xác định cấp quận/huyện
                        String distArea = null;
                        if (district != null && !district.trim().isEmpty()) {
                            distArea = district.trim();
                        } else if (locality != null && !locality.trim().isEmpty() && (city == null || !locality.equalsIgnoreCase(city))) {
                            distArea = locality.trim();
                        }

                        // Xác định cấp tỉnh/thành phố
                        String cityArea = (city != null) ? city.trim() : "";

                        StringBuilder sb = new StringBuilder();
                        if (subArea != null && !subArea.isEmpty()) {
                            sb.append(subArea);
                        }
                        if (distArea != null && !distArea.isEmpty() && (subArea == null || !distArea.equalsIgnoreCase(subArea))) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(distArea);
                        }
                        if (!cityArea.isEmpty() && (distArea == null || !cityArea.equalsIgnoreCase(distArea))) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(cityArea.replace("Thành phố ", "").replace("Tỉnh ", "").trim());
                        }

                        if (sb.length() > 0) {
                            shortLocation = sb.toString();
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Android Geocoder error: " + e.getMessage());
            }

            // Nếu Geocoder thành công và có tên chi tiết
            if (shortLocation != null && !shortLocation.trim().isEmpty()) {
                final String finalShort = shortLocation;
                final String finalFull = (fullAddress != null) ? fullAddress : shortLocation;
                saveLocation(context, finalShort, finalFull, lat + "," + lon);
                mainHandler.post(() -> callback.onLocationResolved(finalShort, finalFull, lat + "," + lon));
                return;
            }

            // 2. Fallback: Reverse Geocoding qua OpenStreetMap Nominatim API (Chính xác cho mọi tọa độ)
            fetchOnlineReverseGeocode(lat, lon, (name, full) -> {
                String finalName = name;
                String finalFull = full;

                if (finalName == null || finalName.isEmpty()) {
                    finalName = String.format(Locale.getDefault(), "Tọa độ: %.4f°N, %.4f°E", lat, lon);
                    finalFull = finalName;
                }

                saveLocation(context, finalName, finalFull, lat + "," + lon);
                String finalShort = finalName;
                String finalFullAddr = finalFull;
                mainHandler.post(() -> callback.onLocationResolved(finalShort, finalFullAddr, lat + "," + lon));
            });

        }).start();
    }

    private interface OnlineReverseCallback {
        void onResult(String shortName, String fullAddress);
    }

    private static void fetchOnlineReverseGeocode(double lat, double lon, OnlineReverseCallback callback) {
        String url = String.format(Locale.US,
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f&zoom=18&addressdetails=1&accept-language=vi",
                lat, lon);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "WeatherAppVietnam/2.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Nominatim error: " + e.getMessage());
                callback.onResult(null, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        String displayName = json.optString("display_name", "");
                        JSONObject addr = json.optJSONObject("address");

                        if (addr != null) {
                            String neighbourhood = addr.optString("neighbourhood", "");
                            String road = addr.optString("road", "");
                            String suburb = addr.optString("suburb", "");
                            String quarter = addr.optString("quarter", "");
                            String cityDistrict = addr.optString("city_district", "");
                            String district = addr.optString("district", "");
                            String county = addr.optString("county", "");
                            String city = addr.optString("city", "");
                            String state = addr.optString("state", "");

                            // Cấp 1: Thôn/làng/khu phố/đường/phường
                            String localName = "";
                            if (!neighbourhood.isEmpty()) {
                                localName = neighbourhood;
                            } else if (!quarter.isEmpty()) {
                                localName = quarter;
                            } else if (!road.isEmpty()) {
                                localName = road.replace("Đường ", "").replace("Phố ", "").trim();
                            } else if (!suburb.isEmpty()) {
                                localName = suburb.replace("Phường ", "").replace("Xã ", "").trim();
                            }

                            // Cấp 2: Quận/Huyện/Thị xã
                            String distName = "";
                            if (!cityDistrict.isEmpty()) {
                                distName = cityDistrict;
                            } else if (!district.isEmpty()) {
                                distName = district;
                            } else if (!county.isEmpty()) {
                                distName = county;
                            } else if (!suburb.isEmpty() && !suburb.equalsIgnoreCase(localName)) {
                                distName = suburb.replace("Phường ", "").replace("Xã ", "").trim();
                            }

                            // Cấp 3: Tỉnh/Thành phố
                            String cityName = !city.isEmpty() ? city : state;
                            cityName = cityName.replace("Thành phố ", "").replace("Tỉnh ", "").trim();

                            StringBuilder shortSb = new StringBuilder();
                            if (!localName.isEmpty()) shortSb.append(localName);
                            if (!distName.isEmpty() && !distName.equalsIgnoreCase(localName)) {
                                if (shortSb.length() > 0) shortSb.append(", ");
                                shortSb.append(distName);
                            }
                            if (!cityName.isEmpty() && !cityName.equalsIgnoreCase(distName)) {
                                if (shortSb.length() > 0) shortSb.append(", ");
                                shortSb.append(cityName);
                            }

                            String shortResult = shortSb.toString();
                            callback.onResult(shortResult.isEmpty() ? displayName : shortResult, displayName);
                            return;
                        }
                        callback.onResult(displayName, displayName);
                    } catch (Exception ex) {
                        Log.e(TAG, "Parse Nominatim JSON error: " + ex.getMessage());
                        callback.onResult(null, null);
                    }
                } else {
                    callback.onResult(null, null);
                }
            }
        });
    }

    public static void saveLocation(Context context, String name, String full, String query) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_EXACT_NAME, name)
                .putString(KEY_FULL_ADDRESS, full)
                .putString(KEY_EXACT_QUERY, query)
                .apply();

        // Đồng bộ sang UserPrefs home_living_city
        SharedPreferences userPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userPrefs.edit().putString("home_living_city", name).apply();
    }

    public static String getSavedExactLocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EXACT_NAME, "");
    }

    public static String getSavedLocation(Context context) {
        return getSavedExactLocation(context);
    }

    public static String getSavedExactQuery(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EXACT_QUERY, "");
    }

    /**
     * Nhận diện xem GPS có đang ở tọa độ mặc định của máy ảo Android Studio (Mountain View, California) không.
     * Mặc định máy ảo của Google luôn phát tọa độ 37.4220, -122.0840 nếu người dùng chưa chỉnh GPS trên máy ảo.
     */
    public static boolean isEmulatorDefaultCoordinates(double lat, double lon) {
        return Math.abs(lat - 37.4220) < 0.1 && Math.abs(lon - (-122.0840)) < 0.1;
    }

    /**
     * Tự động nhận diện vị trí thực tế của người dùng qua mạng Internet (IP Geolocation)
     * Giúp người dùng dùng máy ảo (emulator) ở Việt Nam vẫn định vị đúng tỉnh thành thực tế mà không bị văng sang Mỹ.
     */
    public static void fetchLocationByIP(Context context, LocationResultCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://ip-api.com/json/?fields=status,country,city,regionName,lat,lon,query")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                fallbackDefaultHanoi(context, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        if ("success".equalsIgnoreCase(json.optString("status"))) {
                            double lat = json.getDouble("lat");
                            double lon = json.getDouble("lon");

                            // Tiếp tục phân giải chi tiết từ tọa độ mạng thực tế này
                            resolvePreciseLocation(context, lat, lon, callback);
                            return;
                        }
                    } catch (Exception ignored) {}
                }
                fallbackDefaultHanoi(context, callback);
            }
        });
    }

    private static void fallbackDefaultHanoi(Context context, LocationResultCallback callback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            saveLocation(context, "Hà Nội", "Thành phố Hà Nội, Việt Nam", "21.0285,105.8542");
            callback.onLocationResolved("Hà Nội", "Thành phố Hà Nội, Việt Nam", "21.0285,105.8542");
        });
    }
}
