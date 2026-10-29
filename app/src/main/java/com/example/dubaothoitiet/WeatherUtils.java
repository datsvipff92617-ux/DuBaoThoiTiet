package com.example.dubaothoitiet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Lớp tiện ích xử lý chuẩn hóa địa danh Việt Nam, loại bỏ lỗi tìm kiếm
 * và hỗ trợ dịch thuật ngữ khí tượng/thiên văn.
 */
public class WeatherUtils {

    /**
     * Chuẩn hóa tên địa phương tìm kiếm qua kho dữ liệu 63 Tỉnh/Thành Việt Nam
     * Tránh tuyệt đối việc WeatherAPI map "Huế" sang Ethiopia hay "Nghệ An" sang Da Nghe.
     */
    public static String normalizeCityQuery(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Ha Noi, Vietnam";
        }
        return VietnamLocations.resolveToWeatherQuery(input);
    }


    /**
     * Mã hóa URL UTF-8 an toàn
     */
    public static String encodeQuery(String query) {
        try {
            return URLEncoder.encode(normalizeCityQuery(query), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return query.replace(" ", "%20");
        }
    }

    /**
     * Kiểm tra tọa độ có nằm trong lãnh thổ Việt Nam hay không
     */
    public static boolean isCoordinatesInVietnam(double lat, double lon) {
        return (lat >= 8.0 && lat <= 24.5) && (lon >= 102.0 && lon <= 110.5);
    }

    /**
     * Kiểm tra chuỗi có phải là cặp tọa độ vĩ/kinh (ví dụ "21.0182,105.7765") hay không
     */
    public static boolean isCoordinateQuery(String query) {
        if (query == null) return false;
        return query.trim().matches("^-?\\d+(\\.\\d+)?,\\s*-?\\d+(\\.\\d+)?$");
    }

    /**
     * Làm sạch tên địa phương, khử lỗi Mojibake từ API (ví dụ: Ã Inh Thon / Ãlnh Thon -> Hà Nội)
     */
    public static String cleanLocationName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Hà Nội";
        }

        String trimmed = name.trim();

        // Xử lý lỗi mã hóa ký tự Latin-1 / UTF-8 đặc trưng từ WeatherAPI tại Việt Nam
        if (trimmed.contains("Ã") || trimmed.contains("\u0081") || trimmed.contains("\u0090")
                || trimmed.contains("lnh Thon") || trimmed.contains("Inh Thon")
                || trimmed.toLowerCase().contains("inh thon") || trimmed.toLowerCase().contains("dinh thon")) {
            return "Hà Nội";
        }

        if (isCoordinateQuery(trimmed)) {
            return "Vị trí hiện tại";
        }

        return VietnamLocations.getDisplayNameForApiResult(trimmed);
    }


    /**
     * Dịch tuần trăng tiếng Anh sang tiếng Việt
     */
    public static String translateMoonPhase(String phase) {
        if (phase == null) return "Trăng tròn";
        switch (phase.trim().toLowerCase()) {
            case "new moon":
                return "🌑 Trăng non (Đầu tháng âm lịch)";
            case "waxing crescent":
                return "🌒 Trăng lưỡi liềm đầu tháng";
            case "first quarter":
                return "🌓 Bán nguyệt đầu tháng";
            case "waxing gibbous":
                return "🌔 Trăng khuyết đầu tháng";
            case "full moon":
                return "🌕 Trăng tròn (Rằm)";
            case "waning gibbous":
                return "🌖 Trăng khuyết cuối tháng";
            case "last quarter":
                return "🌗 Bán nguyệt cuối tháng";
            case "waning crescent":
                return "🌘 Trăng lưỡi liềm cuối tháng";
            default:
                return "🌙 " + phase;
        }
    }
}
