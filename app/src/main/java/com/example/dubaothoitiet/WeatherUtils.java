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
     * Chuẩn hóa tên địa phương tìm kiếm:
     * - Tránh lỗi WeatherAPI nhận "HCM" thành sân bay ở Somalia
     * - Tránh lỗi WeatherAPI nhận "Đà Lạt" có dấu thành "Đà Nẵng"
     */
    public static String normalizeCityQuery(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Ha Noi";
        }

        String raw = input.trim();
        String lower = raw.toLowerCase();

        // 1. Xử lý trường hợp đặc biệt: TP. Hồ Chí Minh
        if (lower.equals("hcm") || lower.equals("tp hcm") || lower.equals("tp.hcm")
                || lower.equals("tphcm") || lower.contains("hồ chí minh") || lower.contains("ho chi minh")
                || lower.contains("sài gòn") || lower.contains("saigon")) {
            return "Ho Chi Minh City";
        }

        // 2. Xử lý Đà Lạt để không bị nhầm sang Đà Nẵng
        if (lower.contains("đà lạt") || lower.contains("da lat") || lower.contains("dalat")) {
            return "Da Lat";
        }

        // 3. Xử lý các tỉnh thành phổ biến khác
        if (lower.contains("đà nẵng") || lower.contains("da nang") || lower.contains("danang")) {
            return "Da Nang";
        }
        if (lower.contains("hà nội") || lower.contains("ha noi") || lower.contains("hanoi")) {
            return "Ha Noi";
        }
        if (lower.contains("hải phòng") || lower.contains("hai phong")) {
            return "Hai Phong";
        }
        if (lower.contains("cần thơ") || lower.contains("can tho")) {
            return "Can Tho";
        }
        if (lower.contains("nha trang")) {
            return "Nha Trang";
        }
        if (lower.contains("huế") || lower.contains("hue")) {
            return "Hue";
        }
        if (lower.contains("vũng tàu") || lower.contains("vung tau")) {
            return "Vung Tau";
        }
        if (lower.contains("phú quốc") || lower.contains("phu quoc")) {
            return "Phu Quoc";
        }
        if (lower.contains("quy nhơn") || lower.contains("quy nhon")) {
            return "Quy Nhon";
        }
        if (lower.contains("hạ long") || lower.contains("ha long") || lower.contains("quảng ninh") || lower.contains("quang ninh")) {
            return "Ha Long";
        }
        if (lower.contains("sa pa") || lower.contains("sapa")) {
            return "Sa Pa";
        }
        if (lower.contains("buôn ma thuột") || lower.contains("buon ma thuot") || lower.contains("đắk lắk")) {
            return "Buon Ma Thuot";
        }

        // 4. Khử dấu tiếng Việt chuẩn cho các địa phương còn lại
        String nfdNormalized = Normalizer.normalize(raw, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccents = pattern.matcher(nfdNormalized).replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D")
                .replace("–", "-")
                .replace("—", "-");

        return noAccents.trim();
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
