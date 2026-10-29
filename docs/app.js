/**
 * DỰ BÁO THỜI TIẾT - WEB APPLICATION (PWA)
 * Đồng bộ tính năng, tra cứu viết tắt TP.HCM và giao diện Sáng/Tối với bản Android v2.2
 */

const API_KEY = "35d141f7fec24768b87114757252510";

// Danh bạ 63 Tỉnh/Thành & Khu Vực với Bí Danh Thông Minh (Khử lỗi viết tắt TP.HCM, HN...)
const VIETNAM_LOCATIONS = [
  { name: "TP. Hồ Chí Minh", province: "Sài Gòn", query: "Ho Chi Minh City, Vietnam", lat: 10.8231, lon: 106.6297, aliases: ["tphcm", "tp.hcm", "tp. hcm", "tp hcm", "hcm", "hcmc", "sg", "sai gon", "saigon", "sài gòn", "ho chi minh", "tp ho chi minh"] },
  { name: "Hà Nội", province: "Thủ đô", query: "Ha Noi, Vietnam", lat: 21.0285, lon: 105.8542, aliases: ["hn", "hanoi", "ha noi", "thu do"] },
  { name: "Đà Nẵng", province: "Đà Nẵng", query: "Da Nang, Vietnam", lat: 16.0678, lon: 108.2208, aliases: ["dn", "danang", "da nang"] },
  { name: "Hải Phòng", province: "Hải Phòng", query: "Hai Phong, Vietnam", lat: 20.8561, lon: 106.6822, aliases: ["hp", "haiphong", "hai phong"] },
  { name: "Cần Thơ", province: "Cần Thơ", query: "Can Tho, Vietnam", lat: 10.0333, lon: 105.7833, aliases: ["ct", "cantho", "can tho"] },
  { name: "Triều Khúc", province: "Thanh Xuân, Hà Nội", query: "20.9856,105.7984", lat: 20.9856, lon: 105.7984, aliases: ["trieu khuc", "trieukhuc"] },
  { name: "Phùng Khoang", province: "Nam Từ Liêm, Hà Nội", query: "20.9892,105.7892", lat: 20.9892, lon: 105.7892, aliases: ["phung khoang"] },
  { name: "Huế", province: "Thừa Thiên Huế", query: "Hue, Vietnam", lat: 16.4667, lon: 107.6000, aliases: ["hue", "tth"] },
  { name: "Nghệ An", province: "TP. Vinh", query: "Vinh, Vietnam", lat: 18.6796, lon: 105.6813, aliases: ["vinh", "nghe an"] },
  { name: "Hà Tĩnh", province: "Hà Tĩnh", query: "Ha Tinh, Vietnam", lat: 18.3427, lon: 105.9056, aliases: ["ha tinh"] },
  { name: "Quảng Bình", province: "Đồng Hới", query: "Dong Hoi, Vietnam", lat: 17.4667, lon: 106.6000, aliases: ["quang binh", "dong hoi"] },
  { name: "Quảng Trị", province: "Đông Hà", query: "Quang Tri, Vietnam", lat: 16.7500, lon: 107.1833, aliases: ["quang tri", "dong ha"] },
  { name: "Thanh Hóa", province: "Thanh Hóa", query: "Thanh Hoa, Vietnam", lat: 19.8000, lon: 105.7667, aliases: ["thanh hoa", "sam son"] },
  { name: "Quảng Nam", province: "Hội An / Tam Kỳ", query: "Hoi An, Vietnam", lat: 15.8801, lon: 108.3380, aliases: ["quang nam", "hoi an", "tam ky"] },
  { name: "Quảng Ngãi", province: "Quảng Ngãi", query: "Quang Ngai, Vietnam", lat: 15.1200, lon: 108.8000, aliases: ["quang ngai"] },
  { name: "Bình Định", province: "Quy Nhơn", query: "Quy Nhon, Vietnam", lat: 13.7820, lon: 109.2194, aliases: ["binh dinh", "quy nhon", "quynhon"] },
  { name: "Phú Yên", province: "Tuy Hòa", query: "Tuy Hoa, Vietnam", lat: 13.0883, lon: 109.3106, aliases: ["phu yen", "tuy hoa"] },
  { name: "Khánh Hòa", province: "Nha Trang", query: "Nha Trang, Vietnam", lat: 12.2388, lon: 109.1967, aliases: ["khanh hoa", "nha trang", "nhatrang"] },
  { name: "Ninh Thuận", province: "Phan Rang", query: "Phan Rang, Vietnam", lat: 11.5667, lon: 108.9833, aliases: ["ninh thuan", "phan rang"] },
  { name: "Bình Thuận", province: "Phan Thiết", query: "Phan Thiet, Vietnam", lat: 10.9333, lon: 108.1000, aliases: ["binh thuan", "phan thiet", "mui ne"] },
  { name: "Quảng Ninh", province: "Hạ Long", query: "Ha Long, Vietnam", lat: 20.9500, lon: 107.0833, aliases: ["quang ninh", "ha long", "halong"] },
  { name: "Bắc Ninh", province: "Bắc Ninh", query: "Bac Ninh, Vietnam", lat: 21.1833, lon: 106.0500, aliases: ["bac ninh"] },
  { name: "Hải Dương", province: "Hải Dương", query: "Hai Duong, Vietnam", lat: 20.9333, lon: 106.3167, aliases: ["hai duong"] },
  { name: "Hưng Yên", province: "Hưng Yên", query: "Hung Yen, Vietnam", lat: 20.6500, lon: 106.0500, aliases: ["hung yen"] },
  { name: "Nam Định", province: "Nam Định", query: "Nam Dinh, Vietnam", lat: 20.4167, lon: 106.1667, aliases: ["nam dinh"] },
  { name: "Ninh Bình", province: "Ninh Bình", query: "Ninh Binh, Vietnam", lat: 20.2500, lon: 105.9750, aliases: ["ninh binh"] },
  { name: "Thái Bình", province: "Thái Bình", query: "Thai Binh, Vietnam", lat: 20.4500, lon: 106.3333, aliases: ["thai binh"] },
  { name: "Vĩnh Phúc", province: "Vĩnh Yên", query: "Vinh Yen, Vietnam", lat: 21.3090, lon: 105.6049, aliases: ["vinh phuc", "tam dao"] },
  { name: "Phú Thọ", province: "Việt Trì", query: "Viet Tri, Vietnam", lat: 21.3228, lon: 105.4019, aliases: ["phu tho", "viet tri"] },
  { name: "Lào Cai", province: "Sa Pa", query: "Sa Pa, Vietnam", lat: 22.3364, lon: 103.8438, aliases: ["lao cai", "sa pa", "sapa"] },
  { name: "Lâm Đồng", province: "Đà Lạt", query: "Da Lat, Vietnam", lat: 11.9404, lon: 108.4583, aliases: ["lam dong", "da lat", "dalat", "dl"] },
  { name: "Đắk Lắk", province: "Buôn Ma Thuột", query: "Buon Ma Thuot, Vietnam", lat: 12.6667, lon: 108.0500, aliases: ["dak lak", "buon ma thuot", "bmt"] },
  { name: "Gia Lai", province: "Pleiku", query: "Pleiku, Vietnam", lat: 13.9833, lon: 108.0000, aliases: ["gia lai", "pleiku"] },
  { name: "Bà Rịa - Vũng Tàu", province: "Vũng Tàu", query: "Vung Tau, Vietnam", lat: 10.3460, lon: 107.0843, aliases: ["brvt", "vung tau", "vungtau"] },
  { name: "Bình Dương", province: "Thủ Dầu Một", query: "Thu Dau Mot, Vietnam", lat: 10.9804, lon: 106.6519, aliases: ["binh duong"] },
  { name: "Đồng Nai", province: "Biên Hòa", query: "Bien Hoa, Vietnam", lat: 10.9500, lon: 106.8167, aliases: ["dong nai", "bien hoa"] },
  { name: "Kiên Giang", province: "Phú Quốc", query: "Phu Quoc, Vietnam", lat: 10.2899, lon: 103.9840, aliases: ["phu quoc", "phuquoc", "kien giang", "rach gia"] }
];

// Trạng thái ứng dụng
let currentTempC = 28;
let isFahrenheit = false;
let isEnglish = false;
let currentCityName = "Hà Nội";
let currentQuery = "Ha Noi, Vietnam";
let lastWeatherData = null;

// Khử dấu tiếng Việt
function stripAccents(str) {
  if (!str) return "";
  return str.normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d").replace(/Đ/g, "D");
}

// Khử ký tự đặc biệt, dấu chấm, khoảng trắng thành chuỗi nén (tp.hcm -> tphcm)
function toCompactKey(str) {
  if (!str) return "";
  return stripAccents(str).toLowerCase().replace(/[^a-z0-9]/g, "");
}

// Khớp từ khóa người dùng gõ sang truy vấn chuẩn cho WeatherAPI
function resolveWeatherQuery(input) {
  if (!input || !input.trim()) return "Ha Noi, Vietnam";
  const raw = input.trim();
  const compact = toCompactKey(raw);

  // 1. Kiểm tra trực tiếp các từ viết tắt TP.HCM
  if (["tphcm", "hcm", "hcmc", "sg", "saigon", "tphochiminh", "hochiminh", "thanhphohochiminh"].includes(compact)) {
    return "Ho Chi Minh City, Vietnam";
  }
  if (["hn", "hanoi"].includes(compact)) return "Ha Noi, Vietnam";
  if (["dn", "danang"].includes(compact)) return "Da Nang, Vietnam";
  if (["hp", "haiphong"].includes(compact)) return "Hai Phong, Vietnam";
  if (["ct", "cantho"].includes(compact)) return "Can Tho, Vietnam";
  if (["brvt", "vungtau"].includes(compact)) return "Vung Tau, Vietnam";
  if (["bmt", "buonmathuot"].includes(compact)) return "Buon Ma Thuot, Vietnam";
  if (["dalat", "dl"].includes(compact)) return "Da Lat, Vietnam";
  if (["nhatrang"].includes(compact)) return "Nha Trang, Vietnam";
  if (["halong"].includes(compact)) return "Ha Long, Vietnam";
  if (["phuquoc", "pq"].includes(compact)) return "Phu Quoc, Vietnam";
  if (["hue", "tth"].includes(compact)) return "Hue, Vietnam";

  // 2. Tìm trong danh bạ qua alias
  for (const item of VIETNAM_LOCATIONS) {
    if (item.aliases.some(a => toCompactKey(a) === compact)) {
      return item.query;
    }
  }

  // 3. Khớp tên địa danh
  for (const item of VIETNAM_LOCATIONS) {
    if (toCompactKey(item.name) === compact || stripAccents(item.name).toLowerCase() === stripAccents(raw).toLowerCase()) {
      return item.query;
    }
  }

  // Nếu người dùng nhập tên tỉnh thành tiếng Việt khác
  if (!raw.toLowerCase().includes("vietnam") && !raw.toLowerCase().includes("viet nam")) {
    return raw + ", Vietnam";
  }
  return raw;
}

// Lấy tên hiển thị tiếng Việt chuẩn đẹp
function getCanonicalDisplayName(query) {
  if (!query) return "Hà Nội";
  const compact = toCompactKey(query);
  if (["tphcm", "hcm", "hcmc", "sg", "saigon", "tphochiminh", "hochiminh", "thanhphohochiminh"].includes(compact)) {
    return "TP. Hồ Chí Minh";
  }
  if (["hn", "hanoi"].includes(compact)) return "Hà Nội";
  if (["dn", "danang"].includes(compact)) return "Đà Nẵng";
  if (["hp", "haiphong"].includes(compact)) return "Hải Phòng";
  if (["ct", "cantho"].includes(compact)) return "Cần Thơ";
  if (["dalat", "dl"].includes(compact)) return "Đà Lạt";
  if (["nhatrang"].includes(compact)) return "Nha Trang";
  if (["halong"].includes(compact)) return "Hạ Long";
  if (["phuquoc", "pq"].includes(compact)) return "Phú Quốc";
  if (["bmt"].includes(compact)) return "Buôn Ma Thuột";
  if (["brvt"].includes(compact)) return "Vũng Tàu";
  if (["hue"].includes(compact)) return "Huế";

  for (const item of VIETNAM_LOCATIONS) {
    if (item.aliases.some(a => toCompactKey(a) === compact) || toCompactKey(item.name) === compact) {
      return item.name;
    }
  }
  return query;
}

// Format nhiệt độ theo đơn vị người dùng chọn
function formatTemp(tempC) {
  if (tempC === null || tempC === undefined) return "--";
  if (isFahrenheit) {
    const f = (tempC * 9/5) + 32;
    return `${Math.round(f)}°F`;
  }
  return `${Math.round(tempC)}°C`;
}

// Đánh giá mức độ UV
function getUVLevel(uv) {
  if (uv <= 2) return isEnglish ? "Low" : "Thấp (An toàn)";
  if (uv <= 5) return isEnglish ? "Moderate" : "Trung bình";
  if (uv <= 7) return isEnglish ? "High" : "Cao (Cần che chắn)";
  if (uv <= 10) return isEnglish ? "Very High" : "Rất cao (Nguy hiểm)";
  return isEnglish ? "Extreme" : "Cực độ (Tránh ra nắng)";
}

// Đánh giá chất lượng không khí US-EPA AQI
function getAqiDescription(aqi) {
  switch (aqi) {
    case 1: return isEnglish ? "Good (0-50)" : "Tốt (Không khí trong lành)";
    case 2: return isEnglish ? "Moderate (51-100)" : "Trung bình (Chấp nhận được)";
    case 3: return isEnglish ? "Unhealthy for sensitive groups" : "Kém (Nhạy cảm cần chú ý)";
    case 4: return isEnglish ? "Unhealthy (151-200)" : "Xấu (Có hại cho sức khỏe)";
    case 5: return isEnglish ? "Very Unhealthy (201-300)" : "Rất xấu (Rất có hại)";
    case 6: return isEnglish ? "Hazardous (300+)" : "Nguy hại (Cảnh báo khẩn)";
    default: return isEnglish ? "Moderate" : "Trung bình";
  }
}

// Dịch tuần trăng
function translateMoonPhase(phase) {
  if (!phase) return "Trăng tròn";
  const p = phase.toLowerCase();
  if (p.includes("new")) return "🌑 Trăng non (Đầu tháng âm)";
  if (p.includes("waxing crescent")) return "🌒 Trăng lưỡi liềm đầu tháng";
  if (p.includes("first quarter")) return "🌓 Bán nguyệt đầu tháng";
  if (p.includes("waxing gibbous")) return "🌔 Trăng khuyết đầu tháng";
  if (p.includes("full")) return "🌕 Trăng tròn (Rằm)";
  if (p.includes("waning gibbous")) return "🌖 Trăng khuyết cuối tháng";
  if (p.includes("last quarter")) return "🌗 Bán nguyệt cuối tháng";
  if (p.includes("waning crescent")) return "🌘 Trăng lưỡi liềm cuối tháng";
  return `🌙 ${phase}`;
}

// Tải dữ liệu thời tiết qua WeatherAPI
async function fetchWeather(query, exactDisplayName = null) {
  try {
    const resolved = resolveWeatherQuery(query);
    const displayName = exactDisplayName || getCanonicalDisplayName(query);
    currentCityName = displayName;
    currentQuery = resolved;

    document.getElementById("heroLocationName").textContent = `📍 ${displayName}`;
    document.getElementById("heroCondition").textContent = isEnglish ? "Loading weather data..." : "Đang tải dữ liệu thời tiết...";

    const url = `https://api.weatherapi.com/v1/forecast.json?key=${API_KEY}&q=${encodeURIComponent(resolved)}&days=7&aqi=yes&alerts=no&lang=${isEnglish ? "en" : "vi"}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error("API request failed");
    
    const data = await res.json();
    if (data.error) throw new Error(data.error.message);

    lastWeatherData = data;
    renderWeatherData(data, displayName);

  } catch (err) {
    console.error("Fetch weather error:", err);
    document.getElementById("heroCondition").textContent = isEnglish ? "Location not found" : "Không tìm thấy địa điểm";
  }
}

// Hiển thị dữ liệu lên giao diện
function renderWeatherData(data, displayName) {
  const current = data.current;
  const location = data.location;
  const forecastDays = data.forecast.forecastday;
  const today = forecastDays[0];

  currentTempC = current.temp_c;

  // Hero Card
  document.getElementById("heroLocationName").textContent = `📍 ${displayName || location.name}`;
  
  const now = new Date();
  const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  document.getElementById("heroDate").textContent = now.toLocaleDateString(isEnglish ? 'en-US' : 'vi-VN', options);

  const iconUrl = current.condition.icon.startsWith("http") ? current.condition.icon : `https:${current.condition.icon}`;
  document.getElementById("heroIcon").src = iconUrl.replace("64x64", "128x128");
  document.getElementById("heroTemp").textContent = formatTemp(currentTempC);
  document.getElementById("heroCondition").textContent = current.condition.text;

  document.getElementById("heroHighLow").textContent = `H: ${formatTemp(today.day.maxtemp_c)} • L: ${formatTemp(today.day.mintemp_c)}`;
  document.getElementById("heroFeelsLike").textContent = `${isEnglish ? "Feels like" : "Cảm giác như"} ${formatTemp(current.feelslike_c)}`;

  // Quick Stats
  document.getElementById("valHumidity").textContent = `${current.humidity}%`;
  document.getElementById("valWind").textContent = `${Math.round(current.wind_kph)} km/h`;
  document.getElementById("valUV").textContent = `${Math.round(current.uv)} (${getUVLevel(current.uv)})`;
  document.getElementById("valPressure").textContent = `${current.pressure_mb} mbar`;

  // Hourly Forecast (24 Giờ)
  const hourlyContainer = document.getElementById("hourlyScroll");
  hourlyContainer.innerHTML = "";

  const currentHour = now.getHours();
  // Lấy các giờ của ngày hôm nay và ngày mai
  const hoursToday = today.hour;
  const hoursTomorrow = forecastDays[1] ? forecastDays[1].hour : [];
  const combinedHours = [...hoursToday, ...hoursTomorrow];

  let addedCount = 0;
  for (const h of combinedHours) {
    const hTime = new Date(h.time);
    if (hTime >= now || addedCount === 0 && hTime.getHours() === currentHour) {
      if (addedCount >= 18) break;

      const hourCard = document.createElement("div");
      hourCard.className = `hourly-card ${addedCount === 0 ? "now" : ""}`;
      
      const timeLabel = addedCount === 0 ? (isEnglish ? "Now" : "Bây giờ") : `${hTime.getHours()}:00`;
      const hIcon = h.condition.icon.startsWith("http") ? h.condition.icon : `https:${h.condition.icon}`;

      hourCard.innerHTML = `
        <div class="hourly-time">${timeLabel}</div>
        <img class="hourly-icon" src="${hIcon}" alt="${h.condition.text}">
        <div class="hourly-temp">${formatTemp(h.temp_c)}</div>
      `;
      hourlyContainer.appendChild(hourCard);
      addedCount++;
    }
  }

  // 7-Day Forecast (Dự báo nhiều ngày)
  const dailyContainer = document.getElementById("dailyList");
  dailyContainer.innerHTML = "";

  forecastDays.forEach((day, index) => {
    const dDate = new Date(day.date);
    let dayName = dDate.toLocaleDateString(isEnglish ? 'en-US' : 'vi-VN', { weekday: 'short' });
    if (index === 0) dayName = isEnglish ? "Today" : "Hôm nay";

    const dIcon = day.day.condition.icon.startsWith("http") ? day.day.condition.icon : `https:${day.day.condition.icon}`;
    const dateFormatted = `${dDate.getDate()}/${dDate.getMonth() + 1}`;

    const row = document.createElement("div");
    row.className = "daily-row";
    row.innerHTML = `
      <div class="daily-day-info">
        <span class="daily-day-name">${dayName}</span>
        <span class="daily-day-date">${dateFormatted}</span>
      </div>
      <div class="daily-condition">
        <img class="daily-icon" src="${dIcon}" alt="${day.day.condition.text}">
        <span class="daily-text">${day.day.condition.text}</span>
      </div>
      <div class="daily-temps">
        <span class="daily-max">${formatTemp(day.day.maxtemp_c)}</span>
        <span class="daily-min">${formatTemp(day.day.mintemp_c)}</span>
      </div>
    `;
    dailyContainer.appendChild(row);
  });

  // AQI & Mặt Trời
  const aqiIndex = current.air_quality ? current.air_quality["us-epa-index"] : 2;
  document.getElementById("valAqi").textContent = `AQI: ${aqiIndex}`;
  document.getElementById("valAqiDesc").textContent = getAqiDescription(aqiIndex);

  const astro = today.astro;
  document.getElementById("valSunrise").textContent = astro ? astro.sunrise : "05:45 AM";
  document.getElementById("valSunset").textContent = astro ? `${astro.sunset} • ${translateMoonPhase(astro.moon_phase)}` : "17:50 PM";
}

// Tìm kiếm tự động gợi ý (Autocomplete)
function setupAutocomplete() {
  const searchInput = document.getElementById("searchInput");
  const suggestionsBox = document.getElementById("suggestionsDropdown");
  const clearBtn = document.getElementById("btnClearSearch");

  searchInput.addEventListener("input", () => {
    const val = searchInput.value.trim();
    if (val.length > 0) {
      clearBtn.style.display = "block";
      showSuggestions(val);
    } else {
      clearBtn.style.display = "none";
      suggestionsBox.style.display = "none";
    }
  });

  clearBtn.addEventListener("click", () => {
    searchInput.value = "";
    clearBtn.style.display = "none";
    suggestionsBox.style.display = "none";
    searchInput.focus();
  });

  document.addEventListener("click", (e) => {
    if (!document.getElementById("searchWrapper").contains(e.target)) {
      suggestionsBox.style.display = "none";
    }
  });

  searchInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      const val = searchInput.value.trim();
      if (val) {
        suggestionsBox.style.display = "none";
        fetchWeather(val);
      }
    }
  });

  document.getElementById("btnSearch").addEventListener("click", () => {
    const val = searchInput.value.trim();
    if (val) {
      suggestionsBox.style.display = "none";
      fetchWeather(val);
    }
  });
}

// Hiển thị danh sách gợi ý
function showSuggestions(query) {
  const suggestionsBox = document.getElementById("suggestionsDropdown");
  const compact = toCompactKey(query);
  const stripped = stripAccents(query).toLowerCase();

  const matches = [];

  // 1. Ưu tiên khớp từ viết tắt (TP.HCM, HN, ĐN...)
  for (const loc of VIETNAM_LOCATIONS) {
    if (loc.aliases.some(a => toCompactKey(a).startsWith(compact) || stripAccents(a).toLowerCase().startsWith(stripped))) {
      if (!matches.includes(loc)) matches.push(loc);
    }
  }

  // 2. Khớp tên địa danh bắt đầu hoặc chứa
  for (const loc of VIETNAM_LOCATIONS) {
    const locStripped = stripAccents(loc.name).toLowerCase();
    if (locStripped.startsWith(stripped) || loc.name.toLowerCase().startsWith(query.toLowerCase())) {
      if (!matches.includes(loc)) matches.push(loc);
    }
  }

  for (const loc of VIETNAM_LOCATIONS) {
    const locStripped = stripAccents(loc.name).toLowerCase();
    if (locStripped.includes(stripped) || loc.province.toLowerCase().includes(stripped)) {
      if (!matches.includes(loc)) matches.push(loc);
    }
    if (matches.length >= 8) break;
  }

  if (matches.length === 0) {
    suggestionsBox.style.display = "none";
    return;
  }

  suggestionsBox.innerHTML = `<div class="suggestion-header">${isEnglish ? "Suggestions" : "Địa danh phù hợp"}</div>`;
  matches.forEach(item => {
    const div = document.createElement("div");
    div.className = "suggestion-item";
    div.innerHTML = `
      <span class="suggestion-pin">📍</span>
      <div class="suggestion-info">
        <span class="suggestion-title">${item.name}</span>
        <span class="suggestion-sub">${item.province}, Việt Nam</span>
      </div>
    `;
    div.addEventListener("click", () => {
      document.getElementById("searchInput").value = item.name;
      suggestionsBox.style.display = "none";
      fetchWeather(item.query, item.name);
    });
    suggestionsBox.appendChild(div);
  });

  suggestionsBox.style.display = "block";
}

// Bắt vị trí GPS thực tế
function setupGeolocation() {
  const gpsBtn = document.getElementById("btnGps");
  gpsBtn.addEventListener("click", () => {
    if (!navigator.geolocation) {
      alert(isEnglish ? "Geolocation is not supported by your browser." : "Trình duyệt của bạn không hỗ trợ định vị GPS.");
      return;
    }

    gpsBtn.textContent = "⏳...";
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        gpsBtn.textContent = "📍";
        const lat = pos.coords.latitude;
        const lon = pos.coords.longitude;
        fetchWeather(`${lat},${lon}`, isEnglish ? "Current Location" : "Vị trí của bạn");
      },
      (err) => {
        gpsBtn.textContent = "📍";
        console.warn("Geolocation error:", err);
        // Fallback về TP.HCM hoặc Hà Nội
        fetchWeather("Ho Chi Minh City, Vietnam", "TP. Hồ Chí Minh");
      },
      { timeout: 10000, enableHighAccuracy: true }
    );
  });
}

// Đổi Dark / Light Theme
function setupThemeToggle() {
  const themeBtn = document.getElementById("btnTheme");
  const isDark = localStorage.getItem("theme_mode") === "dark";

  if (isDark) {
    document.body.classList.add("dark-mode");
    themeBtn.textContent = "☀️ Sáng";
  } else {
    document.body.classList.remove("dark-mode");
    themeBtn.textContent = "🌙 Tối";
  }

  themeBtn.addEventListener("click", () => {
    document.body.classList.toggle("dark-mode");
    const activeDark = document.body.classList.contains("dark-mode");
    localStorage.setItem("theme_mode", activeDark ? "dark" : "light");
    themeBtn.textContent = activeDark ? (isEnglish ? "☀️ Light" : "☀️ Sáng") : (isEnglish ? "🌙 Dark" : "🌙 Tối");
  });
}

// Đổi đơn vị độ C / độ F
function setupUnitToggle() {
  const unitBtn = document.getElementById("btnUnit");
  unitBtn.addEventListener("click", () => {
    isFahrenheit = !isFahrenheit;
    unitBtn.textContent = isFahrenheit ? "°F" : "°C";
    if (lastWeatherData) {
      renderWeatherData(lastWeatherData, currentCityName);
    }
  });
}

// Đổi ngôn ngữ
function setupLangToggle() {
  const langBtn = document.getElementById("btnLang");
  langBtn.addEventListener("click", () => {
    isEnglish = !isEnglish;
    langBtn.textContent = isEnglish ? "EN" : "VI";
    document.getElementById("searchInput").placeholder = isEnglish ? "Search city: TP.HCM, Hanoi, Danang..." : "Tìm kiếm: TP.HCM, Hà Nội, Đà Nẵng...";
    
    const themeBtn = document.getElementById("btnTheme");
    const isDark = document.body.classList.contains("dark-mode");
    themeBtn.textContent = isDark ? (isEnglish ? "☀️ Light" : "☀️ Sáng") : (isEnglish ? "🌙 Dark" : "🌙 Tối");

    fetchWeather(currentQuery, currentCityName);
  });
}

// Khởi chạy
document.addEventListener("DOMContentLoaded", () => {
  setupThemeToggle();
  setupUnitToggle();
  setupLangToggle();
  setupAutocomplete();
  setupGeolocation();

  // Khởi động mặc định với TP. Hồ Chí Minh
  fetchWeather("Ho Chi Minh City, Vietnam", "TP. Hồ Chí Minh");
});
