package com.example.dubaothoitiet;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Danh bạ toàn bộ 63 Tỉnh/Thành phố và địa danh quê hương tại Việt Nam
 * Cung cấp bộ lọc gợi ý tìm kiếm tức thì và ánh xạ tọa độ/tên truy vấn chuẩn xác cho WeatherAPI.
 */
public class VietnamLocations {

    public static class LocationItem {
        public final String name;           // Tên hiển thị tiếng Việt có dấu
        public final String province;       // Tỉnh/Thành phố
        public final String weatherQuery;   // Chuỗi truy vấn chuẩn xác gửi tới WeatherAPI
        public final double lat;            // Vĩ độ
        public final double lon;            // Kinh độ
        public final String normalizedKey;  // Chuỗi không dấu để tìm kiếm nhanh

        public LocationItem(String name, String province, String weatherQuery, double lat, double lon) {
            this.name = name;
            this.province = province;
            this.weatherQuery = weatherQuery;
            this.lat = lat;
            this.lon = lon;
            this.normalizedKey = stripAccents(name + " " + province + " " + weatherQuery).toLowerCase(Locale.ROOT);
        }

        public String getDisplayString() {
            if (province.isEmpty() || name.equalsIgnoreCase(province)) {
                return name + ", Việt Nam";
            }
            return name + " (" + province + "), Việt Nam";
        }
    }

    private static final List<LocationItem> ALL_LOCATIONS = new ArrayList<>();

    static {
        // --- KHU VỰC DÂN CƯ & ĐẠI HỌC TRỌNG ĐIỂM HÀ NỘI ---
        add("Triều Khúc", "Thanh Xuân, Hà Nội", "20.9856,105.7984", 20.9856, 105.7984);
        add("Phùng Khoang", "Nam Từ Liêm, Hà Nội", "20.9892,105.7892", 20.9892, 105.7892);
        add("Thanh Xuân", "Hà Nội", "20.9932,105.8078", 20.9932, 105.8078);
        add("Nam Từ Liêm", "Hà Nội", "21.0131,105.7656", 21.0131, 105.7656);
        add("Hà Đông", "Hà Nội", "20.9721,105.7772", 20.9721, 105.7772);
        add("Cầu Giấy", "Hà Nội", "21.0362,105.7906", 21.0362, 105.7906);
        add("Đống Đa", "Hà Nội", "21.0181,105.8294", 21.0181, 105.8294);
        add("Bách Khoa", "Hai Bà Trưng, Hà Nội", "21.0044,105.8444", 21.0044, 105.8444);

        // --- 5 THÀNH PHỐ TRỰC THUỘC TRUNG ƯƠNG ---
        add("Hà Nội", "Thủ đô", "Ha Noi, Vietnam", 21.0285, 105.8542);
        add("TP. Hồ Chí Minh", "Sài Gòn", "Ho Chi Minh City, Vietnam", 10.8231, 106.6297);
        add("Đà Nẵng", "Đà Nẵng", "Da Nang, Vietnam", 16.0678, 108.2208);
        add("Hải Phòng", "Hải Phòng", "Hai Phong, Vietnam", 20.8561, 106.6822);
        add("Cần Thơ", "Cần Thơ", "Can Tho, Vietnam", 10.0333, 105.7833);

        // --- BẮC TRUNG BỘ (HUẾ, NGHỆ AN, HÀ TĨNH, QUẢNG BÌNH, QUẢNG TRỊ, THANH HÓA) ---
        add("Huế", "Thừa Thiên Huế", "Hue, Vietnam", 16.4667, 107.6000);
        add("Nghệ An", "TP. Vinh", "Vinh, Vietnam", 18.6796, 105.6813);
        add("Vinh", "Nghệ An", "Vinh, Vietnam", 18.6796, 105.6813);
        add("Cửa Lò", "Nghệ An", "Vinh, Vietnam", 18.7989, 105.7144);
        add("Hà Tĩnh", "Hà Tĩnh", "Ha Tinh, Vietnam", 18.3427, 105.9056);
        add("Quảng Bình", "Đồng Hới", "Dong Hoi, Vietnam", 17.4667, 106.6000);
        add("Đồng Hới", "Quảng Bình", "Dong Hoi, Vietnam", 17.4667, 106.6000);
        add("Quảng Trị", "Đông Hà", "Quang Tri, Vietnam", 16.7500, 107.1833);
        add("Đông Hà", "Quảng Trị", "Quang Tri, Vietnam", 16.8167, 107.1000);
        add("Thanh Hóa", "Thanh Hóa", "Thanh Hoa, Vietnam", 19.8000, 105.7667);
        add("Sầm Sơn", "Thanh Hóa", "Thanh Hoa, Vietnam", 19.7444, 105.9056);

        // --- DUYÊN HẢI NAM TRUNG BỘ (QUẢNG NAM, QUẢNG NGÃI, BÌNH ĐỊNH, PHÚ YÊN, KHÁNH HÒA, NINH THUẬN, BÌNH THUẬN) ---
        add("Quảng Nam", "Tam Kỳ", "Tam Ky, Vietnam", 15.5667, 108.4833);
        add("Hội An", "Quảng Nam", "Hoi An, Vietnam", 15.8801, 108.3380);
        add("Tam Kỳ", "Quảng Nam", "Tam Ky, Vietnam", 15.5667, 108.4833);
        add("Quảng Ngãi", "Quảng Ngãi", "Quang Ngai, Vietnam", 15.1200, 108.8000);
        add("Bình Định", "Quy Nhơn", "Quy Nhon, Vietnam", 13.7820, 109.2194);
        add("Quy Nhơn", "Bình Định", "Quy Nhon, Vietnam", 13.7820, 109.2194);
        add("Phú Yên", "Tuy Hòa", "Tuy Hoa, Vietnam", 13.0883, 109.3106);
        add("Tuy Hòa", "Phú Yên", "Tuy Hoa, Vietnam", 13.0883, 109.3106);
        add("Khánh Hòa", "Nha Trang", "Nha Trang, Vietnam", 12.2388, 109.1967);
        add("Nha Trang", "Khánh Hòa", "Nha Trang, Vietnam", 12.2388, 109.1967);
        add("Cam Ranh", "Khánh Hòa", "Cam Ranh, Vietnam", 11.9214, 109.1591);
        add("Ninh Thuận", "Phan Rang", "Phan Rang, Vietnam", 11.5667, 108.9833);
        add("Phan Rang - Tháp Chàm", "Ninh Thuận", "Phan Rang, Vietnam", 11.5667, 108.9833);
        add("Bình Thuận", "Phan Thiết", "Phan Thiet, Vietnam", 10.9333, 108.1000);
        add("Phan Thiết", "Bình Thuận", "Phan Thiet, Vietnam", 10.9333, 108.1000);

        // --- ĐỒNG BẰNG SÔNG HỒNG & ĐÔNG BẮC BỘ ---
        add("Quảng Ninh", "Hạ Long", "Ha Long, Vietnam", 20.9500, 107.0833);
        add("Hạ Long", "Quảng Ninh", "Ha Long, Vietnam", 20.9500, 107.0833);
        add("Móng Cái", "Quảng Ninh", "Mong Cai, Vietnam", 21.5300, 107.9680);
        add("Cẩm Phả", "Quảng Ninh", "Cam Pha, Vietnam", 21.0167, 107.3000);
        add("Bắc Ninh", "Bắc Ninh", "Bac Ninh, Vietnam", 21.1833, 106.0500);
        add("Hà Nam", "Phủ Lý", "Phu Ly, Vietnam", 20.5411, 105.9139);
        add("Hải Dương", "Hải Dương", "Hai Duong, Vietnam", 20.9333, 106.3167);
        add("Hưng Yên", "Hưng Yên", "Hung Yen, Vietnam", 20.6500, 106.0500);
        add("Nam Định", "Nam Định", "Nam Dinh, Vietnam", 20.4167, 106.1667);
        add("Ninh Bình", "Ninh Bình", "Ninh Binh, Vietnam", 20.2500, 105.9750);
        add("Thái Bình", "Thái Bình", "Thai Binh, Vietnam", 20.4500, 106.3333);
        add("Vĩnh Phúc", "Vĩnh Yên", "Vinh Yen, Vietnam", 21.3090, 105.6049);
        add("Phú Thọ", "Việt Trì", "Viet Tri, Vietnam", 21.3228, 105.4019);
        add("Bắc Giang", "Bắc Giang", "Bac Giang, Vietnam", 21.2667, 106.2000);
        add("Bắc Kạn", "Bắc Kạn", "Bac Kan, Vietnam", 22.1469, 105.8347);
        add("Cao Bằng", "Cao Bằng", "Cao Bang, Vietnam", 22.6667, 106.2500);
        add("Hà Giang", "Hà Giang", "Ha Giang, Vietnam", 22.8233, 104.9839);
        add("Lạng Sơn", "Lạng Sơn", "Lang Son, Vietnam", 21.8536, 106.7628);
        add("Thái Nguyên", "Thái Nguyên", "Thai Nguyen, Vietnam", 21.5928, 105.8442);
        add("Tuyên Quang", "Tuyên Quang", "Tuyen Quang, Vietnam", 21.8236, 105.2155);

        // --- TÂY BẮC BỘ ---
        add("Lào Cai", "Lào Cai", "Lao Cai, Vietnam", 22.4856, 103.9707);
        add("Sa Pa", "Lào Cai", "Sa Pa, Vietnam", 22.3364, 103.8438);
        add("Yên Bái", "Yên Bái", "Yen Bai, Vietnam", 21.7167, 104.8667);
        add("Điện Biên", "Điện Biên Phủ", "Dien Bien Phu, Vietnam", 21.3833, 103.0167);
        add("Hòa Bình", "Hòa Bình", "Hoa Binh, Vietnam", 20.8167, 105.3333);
        add("Lai Châu", "Lai Châu", "Lai Chau, Vietnam", 22.3964, 103.4697);
        add("Sơn La", "Sơn La", "Son La, Vietnam", 21.3256, 103.9189);

        // --- TÂY NGUYÊN (LÂM ĐỒNG, ĐẮK LẮK, GIA LAI, KON TUM, ĐẮK NÔNG) ---
        add("Lâm Đồng", "Đà Lạt", "Da Lat, Vietnam", 11.9404, 108.4583);
        add("Đà Lạt", "Lâm Đồng", "Da Lat, Vietnam", 11.9404, 108.4583);
        add("Bảo Lộc", "Lâm Đồng", "Bao Loc, Vietnam", 11.5478, 107.8083);
        add("Đắk Lắk", "Buôn Ma Thuột", "Buon Ma Thuot, Vietnam", 12.6667, 108.0500);
        add("Buôn Ma Thuột", "Đắk Lắk", "Buon Ma Thuot, Vietnam", 12.6667, 108.0500);
        add("Gia Lai", "Pleiku", "Pleiku, Vietnam", 13.9833, 108.0000);
        add("Pleiku", "Gia Lai", "Pleiku, Vietnam", 13.9833, 108.0000);
        add("Kon Tum", "Kon Tum", "Kon Tum, Vietnam", 14.3500, 108.0000);
        add("Đắk Nông", "Gia Nghĩa", "Gia Nghia, Vietnam", 12.0000, 107.6833);

        // --- ĐÔNG NAM BỘ ---
        add("Bà Rịa - Vũng Tàu", "Vũng Tàu", "Vung Tau, Vietnam", 10.3460, 107.0843);
        add("Vũng Tàu", "Bà Rịa - Vũng Tàu", "Vung Tau, Vietnam", 10.3460, 107.0843);
        add("Bình Dương", "Thủ Dầu Một", "Thu Dau Mot, Vietnam", 10.9804, 106.6519);
        add("Thủ Dầu Một", "Bình Dương", "Thu Dau Mot, Vietnam", 10.9804, 106.6519);
        add("Bình Phước", "Đồng Xoài", "Dong Xoai, Vietnam", 11.5333, 106.8833);
        add("Đồng Nai", "Biên Hòa", "Bien Hoa, Vietnam", 10.9500, 106.8167);
        add("Biên Hòa", "Đồng Nai", "Bien Hoa, Vietnam", 10.9500, 106.8167);
        add("Tây Ninh", "Tây Ninh", "Tay Ninh, Vietnam", 11.3000, 106.1000);

        // --- TÂY NAM BỘ (ĐỒNG BẰNG SÔNG CỬU LONG) ---
        add("An Giang", "Long Xuyên", "Long Xuyen, Vietnam", 10.3833, 105.4167);
        add("Châu Đốc", "An Giang", "Chau Doc, Vietnam", 10.7000, 105.1167);
        add("Bạc Liêu", "Bạc Liêu", "Bac Lieu, Vietnam", 9.2833, 105.7167);
        add("Bến Tre", "Bến Tre", "Ben Tre, Vietnam", 10.2333, 106.3833);
        add("Cà Mau", "Cà Mau", "Ca Mau, Vietnam", 9.1833, 105.1500);
        add("Đồng Tháp", "Cao Lãnh", "Cao Lanh, Vietnam", 10.4500, 105.6333);
        add("Cao Lãnh", "Đồng Tháp", "Cao Lanh, Vietnam", 10.4500, 105.6333);
        add("Sa Đéc", "Đồng Tháp", "Sa Dec, Vietnam", 10.2975, 105.7578);
        add("Hậu Giang", "Vị Thanh", "Vi Thanh, Vietnam", 9.7833, 105.4667);
        add("Kiên Giang", "Rạch Giá", "Rach Gia, Vietnam", 10.0167, 105.0833);
        add("Phú Quốc", "Kiên Giang", "Phu Quoc, Vietnam", 10.2899, 103.9840);
        add("Rạch Giá", "Kiên Giang", "Rach Gia, Vietnam", 10.0167, 105.0833);
        add("Hà Tiên", "Kiên Giang", "Ha Tien, Vietnam", 10.3833, 104.4833);
        add("Long An", "Tân An", "Tan An, Vietnam", 10.5333, 106.4000);
        add("Sóc Trăng", "Sóc Trăng", "Soc Trang, Vietnam", 9.6000, 105.9667);
        add("Tiền Giang", "Mỹ Tho", "My Tho, Vietnam", 10.3500, 106.3667);
        add("Mỹ Tho", "Tiền Giang", "My Tho, Vietnam", 10.3500, 106.3667);
        add("Trà Vinh", "Trà Vinh", "Tra Vinh, Vietnam", 9.9333, 106.3333);
        add("Vĩnh Long", "Vĩnh Long", "Vinh Long, Vietnam", 10.2500, 105.9667);
    }

    private static void add(String name, String province, String weatherQuery, double lat, double lon) {
        ALL_LOCATIONS.add(new LocationItem(name, province, weatherQuery, lat, lon));
    }

    /**
     * Tìm kiếm gợi ý tỉnh thành Việt Nam theo từ khóa (có dấu hoặc không dấu)
     */
    public static List<LocationItem> search(String query) {
        List<LocationItem> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String rawClean = query.trim().toLowerCase(Locale.ROOT);
        String stripped = stripAccents(rawClean);

        // 1. Khớp từ bắt đầu trước (ưu tiên hàng đầu)
        for (LocationItem item : ALL_LOCATIONS) {
            String itemStripped = stripAccents(item.name.toLowerCase(Locale.ROOT));
            if (itemStripped.startsWith(stripped) || item.name.toLowerCase(Locale.ROOT).startsWith(rawClean)) {
                if (!results.contains(item)) {
                    results.add(item);
                }
            }
        }

        // 2. Khớp từ chứa trong tên tỉnh/thành phố
        for (LocationItem item : ALL_LOCATIONS) {
            if (item.normalizedKey.contains(stripped) || item.name.toLowerCase(Locale.ROOT).contains(rawClean)) {
                if (!results.contains(item)) {
                    results.add(item);
                }
            }
            if (results.size() >= 10) break;
        }

        return results;
    }

    /**
     * Phân giải chuỗi người dùng gõ thành chuỗi truy vấn chuẩn xác cho WeatherAPI
     */
    public static String resolveToWeatherQuery(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Ha Noi, Vietnam";
        }

        String raw = input.trim();
        String stripped = stripAccents(raw).toLowerCase(Locale.ROOT);

        // Kiểm tra khớp trực tiếp trong danh bạ Việt Nam
        for (LocationItem item : ALL_LOCATIONS) {
            String itemStripped = stripAccents(item.name).toLowerCase(Locale.ROOT);
            if (itemStripped.equals(stripped) || item.name.equalsIgnoreCase(raw)) {
                return item.weatherQuery;
            }
        }

        // Kiểm tra khớp từng phần (ví dụ người dùng gõ "tỉnh quảng bình" -> tìm "quang binh")
        for (LocationItem item : ALL_LOCATIONS) {
            String itemStripped = stripAccents(item.name).toLowerCase(Locale.ROOT);
            if (stripped.contains(itemStripped) || itemStripped.contains(stripped)) {
                return item.weatherQuery;
            }
        }

        // Nếu là tìm kiếm quốc tế hoặc chưa có trong danh bạ
        if (!stripped.contains("vietnam") && !stripped.contains("viet nam")) {
            // Nếu có dấu tiếng Việt, ưu tiên thêm ", Vietnam" để API không nhầm sang nước ngoài
            if (!raw.equals(stripped)) {
                return raw + ", Vietnam";
            }
        }

        return raw;
    }

    /**
     * Tìm địa danh gần nhất với tọa độ GPS (bảo đảm luôn tìm được tỉnh thành Việt Nam chính xác)
     */
    public static LocationItem findClosest(double lat, double lon) {
        LocationItem best = null;
        double minDistance = Double.MAX_VALUE;

        for (LocationItem item : ALL_LOCATIONS) {
            double dLat = Math.toRadians(item.lat - lat);
            double dLon = Math.toRadians(item.lon - lon);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                       Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(item.lat)) *
                       Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = 6371 * c; // Khoảng cách theo km

            if (distance < minDistance) {
                minDistance = distance;
                best = item;
            }
        }
        return best;
    }

    /**
     * Ánh xạ tên tiếng Anh hoặc tên quận huyện từ API thành tên Tỉnh/Thành phố đẹp
     */
    public static String getDisplayNameForApiResult(String apiName) {
        if (apiName == null) return "Hà Nội";
        String lower = stripAccents(apiName.trim().toLowerCase(Locale.ROOT));

        if (lower.contains("dong hoi")) return "Quảng Bình";
        if (lower.contains("vinh")) return "Nghệ An";
        if (lower.contains("hue")) return "Huế";
        if (lower.contains("tam ky")) return "Quảng Nam";
        if (lower.contains("quang tri")) return "Quảng Trị";
        if (lower.contains("ha long")) return "Quảng Ninh";
        if (lower.contains("da lat")) return "Đà Lạt";
        if (lower.contains("quy nhon")) return "Bình Định";
        if (lower.contains("nha trang")) return "Khánh Hòa";
        if (lower.contains("phan thiet")) return "Bình Thuận";
        if (lower.contains("phan rang")) return "Ninh Thuận";
        if (lower.contains("pleiku")) return "Gia Lai";
        if (lower.contains("buon ma thuot")) return "Đắk Lắk";
        if (lower.contains("viet tri")) return "Phú Thọ";
        if (lower.contains("vinh yen")) return "Vĩnh Phúc";
        if (lower.contains("phu ly")) return "Hà Nam";
        if (lower.contains("my tho")) return "Tiền Giang";
        if (lower.contains("tan an")) return "Long An";
        if (lower.contains("cao lanh")) return "Đồng Tháp";
        if (lower.contains("long xuyen")) return "An Giang";
        if (lower.contains("rach gia")) return "Kiên Giang";
        if (lower.contains("vi thanh")) return "Hậu Giang";

        for (LocationItem item : ALL_LOCATIONS) {
            String itemStripped = stripAccents(item.name.toLowerCase(Locale.ROOT));
            if (lower.equals(itemStripped) || lower.contains(itemStripped) || itemStripped.contains(lower)) {
                return item.name;
            }
        }
        return apiName;
    }

    /**
     * Khử dấu tiếng Việt thành chữ không dấu
     */
    public static String stripAccents(String input) {
        if (input == null) return "";
        String nfdNormalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalized).replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D")
                .replace("–", "-")
                .replace("—", "-");
    }
}

