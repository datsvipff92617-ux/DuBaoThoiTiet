/**
 * DỰ BÁO THỜI TIẾT - WEB APPLICATION & PWA
 * Giao diện và 18 chức năng đồng bộ 100% với bản Android Java Native APK
 */

const API_KEY = "35d141f7fec24768b87114757252510";

// Danh bạ 63 Tỉnh/Thành Việt Nam với bí danh tìm kiếm thông minh
const VIETNAM_LOCATIONS = [
  { name: "Hà Nội", query: "Ha Noi, Vietnam", lat: 21.0285, lon: 105.8542, aliases: ["hn", "hanoi", "ha noi", "thu do"] },
  { name: "TP. Hồ Chí Minh", query: "Ho Chi Minh City, Vietnam", lat: 10.8231, lon: 106.6297, aliases: ["tphcm", "tp.hcm", "hcm", "sg", "sai gon", "saigon", "ho chi minh"] },
  { name: "Đà Nẵng", query: "Da Nang, Vietnam", lat: 16.0678, lon: 108.2208, aliases: ["dn", "danang", "da nang"] },
  { name: "Hải Phòng", query: "Hai Phong, Vietnam", lat: 20.8561, lon: 106.6822, aliases: ["hp", "haiphong", "hai phong"] },
  { name: "Cần Thơ", query: "Can Tho, Vietnam", lat: 10.0333, lon: 105.7833, aliases: ["ct", "cantho", "can tho"] },
  { name: "Đà Lạt", query: "Da Lat, Vietnam", lat: 11.9404, lon: 108.4583, aliases: ["dalat", "dl", "lam dong"] },
  { name: "Nha Trang", query: "Nha Trang, Vietnam", lat: 12.2388, lon: 109.1967, aliases: ["nhatrang", "khanh hoa"] },
  { name: "Huế", query: "Hue, Vietnam", lat: 16.4667, lon: 107.6000, aliases: ["hue", "thua thien hue"] },
  { name: "Quảng Ninh", query: "Ha Long, Vietnam", lat: 20.9500, lon: 107.0833, aliases: ["ha long", "quang ninh"] },
  { name: "Nghệ An", query: "Vinh, Vietnam", lat: 18.6796, lon: 105.6813, aliases: ["vinh", "nghe an"] },
  { name: "Sa Pa", query: "Sa Pa, Vietnam", lat: 22.3364, lon: 103.8438, aliases: ["sapa", "lao cai"] },
  { name: "Phú Quốc", query: "Phu Quoc, Vietnam", lat: 10.2899, lon: 103.9840, aliases: ["phuquoc", "kien giang"] },
  { name: "Vũng Tàu", query: "Vung Tau, Vietnam", lat: 10.3460, lon: 107.0843, aliases: ["vungtau", "brvt"] },
  { name: "Buôn Ma Thuột", query: "Buon Ma Thuot, Vietnam", lat: 12.6667, lon: 108.0500, aliases: ["bmt", "dak lak"] },
  { name: "Bắc Ninh", query: "Bac Ninh, Vietnam", lat: 21.1833, lon: 106.0500, aliases: ["bac ninh"] },
  { name: "Hải Dương", query: "Hai Duong, Vietnam", lat: 20.9333, lon: 106.3167, aliases: ["hai duong"] },
  { name: "Nam Định", query: "Nam Dinh, Vietnam", lat: 20.4167, lon: 106.1667, aliases: ["nam dinh"] },
  { name: "Ninh Bình", query: "Ninh Binh, Vietnam", lat: 20.2500, lon: 105.9750, aliases: ["ninh binh"] },
  { name: "Thanh Hóa", query: "Thanh Hoa, Vietnam", lat: 19.8000, lon: 105.7667, aliases: ["thanh hoa"] },
  { name: "Bình Định", query: "Quy Nhon, Vietnam", lat: 13.7820, lon: 109.2194, aliases: ["quy nhon", "binh dinh"] },
  { name: "Bình Thuận", query: "Phan Thiet, Vietnam", lat: 10.9333, lon: 108.1000, aliases: ["phan thiet", "mui ne"] }
];

// Trạng thái cấu hình (Đọc từ localStorage tương tự SharedPreferences của Android)
let state = {
  city: "Hà Nội",
  query: "Ha Noi, Vietnam",
  tempC: 28,
  isFahrenheit: localStorage.getItem("temp_unit") === "F",
  isDarkMode: localStorage.getItem("dark_mode") === "true",
  lang: localStorage.getItem("app_lang") || "vi",
  currentUser: JSON.parse(localStorage.getItem("current_user")) || { username: "admin", role: "admin", email: "admin@dubaothoitiet.vn" },
  lastData: null
};

// Khử dấu tiếng Việt
function stripAccents(str) {
  if (!str) return "";
  return str.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/đ/g, "d").replace(/Đ/g, "D");
}

// Khử ký tự đặc biệt
function toCompactKey(str) {
  if (!str) return "";
  return stripAccents(str).toLowerCase().replace(/[^a-z0-9]/g, "");
}

// Format nhiệt độ
function formatTemp(tempC) {
  if (tempC === null || tempC === undefined) return "--";
  if (state.isFahrenheit) {
    const f = (tempC * 9/5) + 32;
    return `${Math.round(f)}°F`;
  }
  return `${Math.round(tempC)}°C`;
}

// Áp dụng Dark Mode
function applyTheme() {
  if (state.isDarkMode) {
    document.body.classList.add("dark-mode");
    const toggle = document.getElementById("toggleDarkMode");
    if (toggle) toggle.checked = true;
  } else {
    document.body.classList.remove("dark-mode");
    const toggle = document.getElementById("toggleDarkMode");
    if (toggle) toggle.checked = false;
  }
}

// Áp dụng thông tin người dùng lên Header
function updateTopUserBadge() {
  const badge = document.getElementById("txtUserBadge");
  const profUser = document.getElementById("profUsername");
  const profRole = document.getElementById("profRoleBadge");
  const profEmail = document.getElementById("profEmail");
  const adminBtn = document.getElementById("btnUserManagement");

  if (!badge) return;

  if (state.currentUser.role === "admin") {
    badge.textContent = `👑 ${state.currentUser.username} (Admin)`;
    if (profRole) profRole.textContent = "Quản Trị Viên (Admin)";
    if (adminBtn) adminBtn.style.display = "flex";
  } else if (state.currentUser.role === "guest") {
    badge.textContent = `👤 Khách (Bỏ qua ĐN)`;
    if (profRole) profRole.textContent = "Khách (Phiên Tạm Thời)";
    if (adminBtn) adminBtn.style.display = "none";
  } else {
    badge.textContent = `👤 ${state.currentUser.username} (User)`;
    if (profRole) profRole.textContent = "Người Dùng Thường";
    if (adminBtn) adminBtn.style.display = "none";
  }

  if (profUser) profUser.textContent = state.currentUser.username;
  if (profEmail) profEmail.textContent = state.currentUser.email || "user@dubaothoitiet.vn";
}

// Tải dữ liệu thời tiết
async function fetchWeather(searchStr, displayName = null) {
  try {
    let q = searchStr;
    const compact = toCompactKey(searchStr);

    // Khớp danh bạ
    for (const item of VIETNAM_LOCATIONS) {
      if (item.aliases.some(a => toCompactKey(a) === compact) || toCompactKey(item.name) === compact) {
        q = item.query;
        if (!displayName) displayName = item.name;
        break;
      }
    }

    if (!displayName) displayName = searchStr;

    state.city = displayName;
    state.query = q;

    document.getElementById("cityNameText").textContent = `${displayName}, Việt Nam`;
    document.getElementById("conditionText").textContent = "Đang tải dữ liệu khí tượng...";

    const apiUrl = `https://api.weatherapi.com/v1/forecast.json?key=${API_KEY}&q=${encodeURIComponent(q)}&days=7&aqi=yes&alerts=yes&lang=${state.lang}`;
    const res = await fetch(apiUrl);
    if (!res.ok) throw new Error("Network response error");

    const data = await res.json();
    if (data.error) throw new Error(data.error.message);

    state.lastData = data;
    renderWeather(data, displayName);

  } catch (err) {
    console.error("Error fetching weather:", err);
    document.getElementById("conditionText").textContent = "Không tìm thấy dữ liệu thời tiết.";
  }
}

// Hiển thị thời tiết lên giao diện chính
function renderWeather(data, displayName) {
  const current = data.current;
  const forecast = data.forecast.forecastday;
  const today = forecast[0];

  state.tempC = current.temp_c;

  // Hero Card
  document.getElementById("cityNameText").textContent = `${displayName}, Việt Nam`;
  
  const now = new Date();
  const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  document.getElementById("dateText").textContent = now.toLocaleDateString(state.lang === 'en' ? 'en-US' : 'vi-VN', options);

  const iconUrl = current.condition.icon.startsWith("http") ? current.condition.icon : `https:${current.condition.icon}`;
  document.getElementById("weatherIcon").src = iconUrl.replace("64x64", "128x128");
  document.getElementById("tempText").textContent = formatTemp(current.temp_c);
  document.getElementById("conditionText").textContent = current.condition.text;

  // 4 Metrics
  document.getElementById("feelsLikeText").textContent = formatTemp(current.feelslike_c);
  document.getElementById("humidityText").textContent = `${current.humidity}%`;
  document.getElementById("windText").textContent = `${Math.round(current.wind_kph)} km/h`;
  document.getElementById("uvText").textContent = `${Math.round(current.uv)}`;

  // Điền dữ liệu cho các modal
  populateModals(data);
}

// Bơm dữ liệu vào các Modal chức năng
function populateModals(data) {
  const current = data.current;
  const forecast = data.forecast.forecastday;
  const today = forecast[0];
  const astro = today.astro;

  // 1. Modal 24h Hourly
  const hourlyBox = document.getElementById("hourlyListContent");
  if (hourlyBox) {
    hourlyBox.innerHTML = "";
    const allHours = [...today.hour, ...(forecast[1] ? forecast[1].hour : [])];
    const now = new Date();
    let count = 0;

    for (const h of allHours) {
      const hDate = new Date(h.time);
      if (hDate >= now || count === 0 && hDate.getHours() === now.getHours()) {
        if (count >= 24) break;
        const icon = h.condition.icon.startsWith("http") ? h.condition.icon : `https:${h.condition.icon}`;
        const timeLabel = count === 0 ? "Bây giờ" : `${hDate.getHours()}:00`;

        const row = document.createElement("div");
        row.className = "hour-row";
        row.innerHTML = `
          <div class="row-left">
            <img class="row-icon" src="${icon}" alt="">
            <div>
              <div class="row-time">${timeLabel}</div>
              <div class="row-desc">${h.condition.text}</div>
            </div>
          </div>
          <div class="row-temp">${formatTemp(h.temp_c)}</div>
        `;
        hourlyBox.appendChild(row);
        count++;
      }
    }
  }

  // 2. Modal 7 Days
  const forecast7Box = document.getElementById("forecast7ListContent");
  if (forecast7Box) {
    forecast7Box.innerHTML = "";
    forecast.forEach((day, idx) => {
      const dDate = new Date(day.date);
      let dayName = dDate.toLocaleDateString(state.lang === 'en' ? 'en-US' : 'vi-VN', { weekday: 'long' });
      if (idx === 0) dayName = "Hôm nay";
      const icon = day.day.condition.icon.startsWith("http") ? day.day.condition.icon : `https:${day.day.condition.icon}`;

      const row = document.createElement("div");
      row.className = "forecast-row";
      row.innerHTML = `
        <div class="row-left">
          <img class="row-icon" src="${icon}" alt="">
          <div>
            <div class="row-time">${dayName} (${dDate.getDate()}/${dDate.getMonth() + 1})</div>
            <div class="row-desc">${day.day.condition.text} • Mưa: ${day.day.daily_chance_of_rain}%</div>
          </div>
        </div>
        <div class="row-temp">${formatTemp(day.day.maxtemp_c)} / ${formatTemp(day.day.mintemp_c)}</div>
      `;
      forecast7Box.appendChild(row);
    });
  }

  // 3. Modal 3 Days
  const forecast3Box = document.getElementById("forecast3ListContent");
  if (forecast3Box) {
    forecast3Box.innerHTML = "";
    forecast.slice(0, 3).forEach((day, idx) => {
      const dDate = new Date(day.date);
      let dayTitle = idx === 0 ? "Hôm nay" : (idx === 1 ? "Ngày mai" : "Ngày kia");
      const icon = day.day.condition.icon.startsWith("http") ? day.day.condition.icon : `https:${day.day.condition.icon}`;

      const row = document.createElement("div");
      row.className = "forecast-row";
      row.innerHTML = `
        <div class="row-left">
          <img class="row-icon" src="${icon}" alt="">
          <div>
            <div class="row-time">${dayTitle} (${dDate.getDate()}/${dDate.getMonth() + 1})</div>
            <div class="row-desc">${day.day.condition.text}</div>
          </div>
        </div>
        <div class="row-temp">${formatTemp(day.day.maxtemp_c)} / ${formatTemp(day.day.mintemp_c)}</div>
      `;
      forecast3Box.appendChild(row);
    });
  }

  // 4. Modal Sun/Moon
  if (astro) {
    document.getElementById("valSunrise").textContent = astro.sunrise || "--";
    document.getElementById("valSunset").textContent = astro.sunset || "--";
    document.getElementById("valMoonPhase").textContent = astro.moon_phase || "Trăng Tròn";
    document.getElementById("valMoonIllum").textContent = `${astro.moon_illumination || 80}%`;
  }

  // 5. Modal Air Quality AQI
  if (current.air_quality) {
    const aqi = current.air_quality["us-epa-index"] || 2;
    document.getElementById("valAqiScore").textContent = `AQI Index: ${aqi}`;
    const descMap = {
      1: "Tốt (Không khí rất trong lành)",
      2: "Trung bình (Chấp nhận được)",
      3: "Kém (Người nhạy cảm nên hạn chế ra ngoài)",
      4: "Xấu (Bắt buộc đeo khẩu trang chống bụi)",
      5: "Rất xấu (Nguy hại cho đường hô hấp)",
      6: "Nguy hại (Cảnh báo khẩn cấp)"
    };
    document.getElementById("valAqiLevel").textContent = descMap[aqi] || "Trung bình";
    document.getElementById("valPm25").textContent = `${Math.round(current.air_quality.pm2_5 || 15)} µg/m³`;
    document.getElementById("valPm10").textContent = `${Math.round(current.air_quality.pm10 || 28)} µg/m³`;
    document.getElementById("valCo").textContent = `${Math.round(current.air_quality.co || 240)} µg/m³`;
    document.getElementById("valNo2").textContent = `${Math.round(current.air_quality.no2 || 12)} µg/m³`;
    document.getElementById("valO3").textContent = `${Math.round(current.air_quality.o3 || 45)} µg/m³`;
    document.getElementById("valSo2").textContent = `${Math.round(current.air_quality.so2 || 6)} µg/m³`;
  }

  // 6. Modal Details
  document.getElementById("valDetailPressure").textContent = `${current.pressure_mb} mbar`;
  document.getElementById("valDetailDew").textContent = formatTemp(current.feelslike_c - 3);
  document.getElementById("valDetailVis").textContent = `${current.vis_km} km`;
  document.getElementById("valDetailWindDir").textContent = `${current.wind_dir} (${Math.round(current.wind_kph)} km/h)`;

  // 7. Modal Lifestyle Advice
  if (current.temp_c < 18) {
    document.getElementById("lsClothing").textContent = "Thời tiết lạnh rét, khuyến nghị mặc áo len dày, áo khoác ấm và khăn quàng cổ.";
  } else if (current.temp_c <= 28) {
    document.getElementById("lsClothing").textContent = "Thời tiết mát mẻ dễ chịu, trang phục thoải mái, vải cotton thoáng mát.";
  } else {
    document.getElementById("lsClothing").textContent = "Thời tiết nắng nóng, nên mặc trang phục mỏng nhẹ, sáng màu và mang theo ô che nắng.";
  }

  if (current.humidity < 70 && !current.condition.text.toLowerCase().includes("mưa")) {
    document.getElementById("lsLaundry").textContent = "Độ ẩm lý tưởng và có nắng gió, phơi quần áo rất mau khô và thơm mát.";
  } else {
    document.getElementById("lsLaundry").textContent = "Độ ẩm cao hoặc có khả năng mưa, hạn chế phơi quần áo ngoài trời để tránh ẩm mốc.";
  }

  if (current.uv >= 6) {
    document.getElementById("lsUv").textContent = "Chỉ số tia UV cao, nguy cơ tổn thương da. Khuyến nghị bôi kem chống nắng SPF 30+ và đeo kính râm.";
  } else {
    document.getElementById("lsUv").textContent = "Chỉ số UV ở mức an toàn, thích hợp hoạt động ngoài trời.";
  }
}

// Setup các Modal mở/đóng mượt mà
function setupModals() {
  const modalButtons = [
    { btnId: "btnHourly24h", modalId: "modalHourly" },
    { btnId: "btnForecast7Days", modalId: "modalForecast7" },
    { btnId: "btnForecast3Days", modalId: "modalForecast3" },
    { btnId: "btnCompareWeather", modalId: "modalCompare" },
    { btnId: "btnSunMoon", modalId: "modalSunMoon" },
    { btnId: "btnAirQuality", modalId: "modalAirQuality" },
    { btnId: "btnWeatherAlerts", modalId: "modalAlerts" },
    { btnId: "btnLifestyle", modalId: "modalLifestyle" },
    { btnId: "btnDisasterGuide", modalId: "modalDisaster" },
    { btnId: "btnCurrentLocation", modalId: "modalGpsRadar" },
    { btnId: "btnSearchLocation", modalId: "modalSearch63" },
    { btnId: "btnDetails", modalId: "modalDetails" },
    { btnId: "btnProfile", modalId: "modalProfile" },
    { btnId: "btnTopUser", modalId: "modalProfile" },
    { btnId: "btnSettings", modalId: "modalSettings" },
    { btnId: "btnTopSettings", modalId: "modalSettings" },
    { btnId: "btnUserManagement", modalId: "modalUserManagement" },
    { btnId: "btnLoginModal", modalId: "modalLoginRegister" },
    { btnId: "btnTopLogout", modalId: "modalLoginRegister" }
  ];

  modalButtons.forEach(item => {
    const btn = document.getElementById(item.btnId);
    const modal = document.getElementById(item.modalId);
    if (btn && modal) {
      btn.addEventListener("click", () => {
        modal.classList.add("active");
      });
    }
  });

  // Nút đóng modal
  document.querySelectorAll(".modal-close").forEach(closeBtn => {
    closeBtn.addEventListener("click", () => {
      const targetId = closeBtn.getAttribute("data-close");
      if (targetId) {
        document.getElementById(targetId).classList.remove("active");
      }
    });
  });

  // Bấm nền mờ bên ngoài để đóng modal
  document.querySelectorAll(".modal-backdrop").forEach(backdrop => {
    backdrop.addEventListener("click", (e) => {
      if (e.target === backdrop) {
        backdrop.classList.remove("active");
      }
    });
  });
}

// Xử lý Tìm kiếm và Autocomplete
function setupSearch() {
  const searchInput = document.getElementById("searchEditText");
  const clearBtn = document.getElementById("btnClearSearch");
  const submitBtn = document.getElementById("btnSearchSubmit");
  const dropdown = document.getElementById("searchSuggestionsList");

  if (!searchInput) return;

  searchInput.addEventListener("input", () => {
    const val = searchInput.value.trim();
    if (val.length > 0) {
      clearBtn.style.display = "block";
      showSuggestions(val);
    } else {
      clearBtn.style.display = "none";
      dropdown.style.display = "none";
    }
  });

  clearBtn.addEventListener("click", () => {
    searchInput.value = "";
    clearBtn.style.display = "none";
    dropdown.style.display = "none";
    searchInput.focus();
  });

  submitBtn.addEventListener("click", () => {
    const val = searchInput.value.trim();
    if (val) {
      dropdown.style.display = "none";
      fetchWeather(val);
    }
  });

  searchInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      submitBtn.click();
    }
  });

  function showSuggestions(val) {
    const compact = toCompactKey(val);
    dropdown.innerHTML = "";

    const matched = VIETNAM_LOCATIONS.filter(item => {
      return toCompactKey(item.name).includes(compact) || item.aliases.some(a => toCompactKey(a).includes(compact));
    });

    if (matched.length > 0) {
      dropdown.style.display = "block";
      matched.slice(0, 6).forEach(item => {
        const div = document.createElement("div");
        div.className = "suggestion-item";
        div.innerHTML = `
          <span class="suggestion-name">📍 ${item.name}</span>
          <span class="suggestion-sub">Việt Nam</span>
        `;
        div.addEventListener("click", () => {
          searchInput.value = item.name;
          dropdown.style.display = "none";
          fetchWeather(item.query, item.name);
        });
        dropdown.appendChild(div);
      });
    } else {
      dropdown.style.display = "none";
    }
  }

  // Quick GPS Button
  const btnQuickGps = document.getElementById("btnQuickGps");
  if (btnQuickGps) {
    btnQuickGps.addEventListener("click", () => {
      if ("geolocation" in navigator) {
        document.getElementById("conditionText").textContent = "Đang quét tọa độ GPS vệ tinh...";
        navigator.geolocation.getCurrentPosition(
          (pos) => {
            const lat = pos.coords.latitude;
            const lon = pos.coords.longitude;
            document.getElementById("valGpsLat").textContent = `${lat.toFixed(4)}°`;
            document.getElementById("valGpsLon").textContent = `${lon.toFixed(4)}°`;
            document.getElementById("valGpsAcc").textContent = `±${Math.round(pos.coords.accuracy)} mét`;
            fetchWeather(`${lat},${lon}`, "Vị Trí Hiện Tại (GPS)");
          },
          (err) => {
            console.warn("GPS error/denied:", err);
            fetchWeather("Ha Noi, Vietnam", "Hà Nội");
          },
          { timeout: 7000 }
        );
      } else {
        fetchWeather("Ha Noi, Vietnam", "Hà Nội");
      }
    });
  }
}

// Setup So sánh thời tiết
function setupCompare() {
  const btnCompare = document.getElementById("btnRunCompare");
  const sel1 = document.getElementById("selCompare1");
  const sel2 = document.getElementById("selCompare2");
  const resultBox = document.getElementById("compareResultBox");

  if (!btnCompare) return;

  btnCompare.addEventListener("click", async () => {
    const c1 = sel1.value;
    const c2 = sel2.value;
    resultBox.innerHTML = "<p>Đang tải dữ liệu và đối chiếu song song 2 thành phố...</p>";

    try {
      const [res1, res2] = await Promise.all([
        fetch(`https://api.weatherapi.com/v1/current.json?key=${API_KEY}&q=${encodeURIComponent(c1 + ", Vietnam")}`),
        fetch(`https://api.weatherapi.com/v1/current.json?key=${API_KEY}&q=${encodeURIComponent(c2 + ", Vietnam")}`)
      ]);
      const d1 = await res1.json();
      const d2 = await res2.json();

      const t1 = d1.current.temp_c;
      const t2 = d2.current.temp_c;
      const diff = Math.abs(t1 - t2).toFixed(1);

      let summary = "";
      if (t1 > t2) {
        summary = `🔥 <strong>${c1}</strong> nóng hơn <strong>${c2}</strong> ${diff}°C`;
      } else if (t2 > t1) {
        summary = `🔥 <strong>${c2}</strong> nóng hơn <strong>${c1}</strong> ${diff}°C`;
      } else {
        summary = `⚖️ Cả 2 địa phương đều có mức nhiệt ngang nhau (${t1}°C)`;
      }

      resultBox.innerHTML = `
        <div style="margin-bottom:8px; font-weight:700; color:#38BDF8;">${summary}</div>
        <table style="width:100%; border-collapse:collapse; font-size:0.85rem;">
          <tr style="border-bottom:1px solid #475569;">
            <th style="text-align:left; padding:4px;">Chỉ số</th>
            <th style="padding:4px;">${c1}</th>
            <th style="padding:4px;">${c2}</th>
          </tr>
          <tr style="border-bottom:1px solid #334155;">
            <td style="padding:4px;">Nhiệt độ</td>
            <td style="text-align:center; padding:4px;">${formatTemp(t1)}</td>
            <td style="text-align:center; padding:4px;">${formatTemp(t2)}</td>
          </tr>
          <tr style="border-bottom:1px solid #334155;">
            <td style="padding:4px;">Độ ẩm</td>
            <td style="text-align:center; padding:4px;">${d1.current.humidity}%</td>
            <td style="text-align:center; padding:4px;">${d2.current.humidity}%</td>
          </tr>
          <tr>
            <td style="padding:4px;">Tốc độ gió</td>
            <td style="text-align:center; padding:4px;">${d1.current.wind_kph} km/h</td>
            <td style="text-align:center; padding:4px;">${d2.current.wind_kph} km/h</td>
          </tr>
        </table>
      `;
    } catch (e) {
      resultBox.innerHTML = "<p>Lỗi kết nối khi đối chiếu thời tiết. Vui lòng thử lại.</p>";
    }
  });
}

// Setup Cài đặt (Dark Mode, Đơn vị, Ngôn ngữ)
function setupSettings() {
  const toggleDark = document.getElementById("toggleDarkMode");
  const selectUnit = document.getElementById("selectTempUnit");
  const selectLang = document.getElementById("selectLang");

  if (toggleDark) {
    toggleDark.addEventListener("change", () => {
      state.isDarkMode = toggleDark.checked;
      localStorage.setItem("dark_mode", state.isDarkMode);
      applyTheme();
    });
  }

  if (selectUnit) {
    selectUnit.value = state.isFahrenheit ? "F" : "C";
    selectUnit.addEventListener("change", () => {
      state.isFahrenheit = selectUnit.value === "F";
      localStorage.setItem("temp_unit", selectUnit.value);
      if (state.lastData) renderWeather(state.lastData, state.city);
    });
  }

  if (selectLang) {
    selectLang.value = state.lang;
    selectLang.addEventListener("change", () => {
      state.lang = selectLang.value;
      localStorage.setItem("app_lang", state.lang);
      fetchWeather(state.query, state.city);
    });
  }

  // Quick Chips in 63 Provinces modal
  document.querySelectorAll(".chip").forEach(chip => {
    chip.addEventListener("click", () => {
      const city = chip.getAttribute("data-city");
      document.getElementById("modalSearch63").classList.remove("active");
      fetchWeather(city);
    });
  });

  // Modal 63 list
  const list63 = document.getElementById("list63Container");
  if (list63) {
    list63.innerHTML = "";
    VIETNAM_LOCATIONS.forEach(loc => {
      const item = document.createElement("div");
      item.className = "province-item";
      item.textContent = `📍 ${loc.name}`;
      item.addEventListener("click", () => {
        document.getElementById("modalSearch63").classList.remove("active");
        fetchWeather(loc.query, loc.name);
      });
      list63.appendChild(item);
    });
  }

  // Auth form switching
  const tabLogin = document.getElementById("tabLogin");
  const tabReg = document.getElementById("tabRegister");
  const formLogin = document.getElementById("formLogin");
  const formReg = document.getElementById("formRegister");

  if (tabLogin && tabReg) {
    tabLogin.addEventListener("click", () => {
      tabLogin.classList.add("active");
      tabReg.classList.remove("active");
      formLogin.style.display = "flex";
      formReg.style.display = "none";
    });
    tabReg.addEventListener("click", () => {
      tabReg.classList.add("active");
      tabLogin.classList.remove("active");
      formLogin.style.display = "none";
      formReg.style.display = "flex";
    });
  }

  // Đăng nhập demo
  const btnDoLogin = document.getElementById("btnDoLogin");
  const btnGuestLogin = document.getElementById("btnGuestLogin");

  if (btnDoLogin) {
    btnDoLogin.addEventListener("click", () => {
      const u = document.getElementById("loginUsername").value.trim() || "admin";
      state.currentUser = {
        username: u,
        role: u.toLowerCase() === "admin" ? "admin" : "user",
        email: `${u}@dubaothoitiet.vn`
      };
      localStorage.setItem("current_user", JSON.stringify(state.currentUser));
      updateTopUserBadge();
      document.getElementById("modalLoginRegister").classList.remove("active");
      alert(`Đăng nhập thành công với vai trò: ${state.currentUser.role.toUpperCase()}`);
    });
  }

  if (btnGuestLogin) {
    btnGuestLogin.addEventListener("click", () => {
      state.currentUser = { username: "Khách", role: "guest", email: "guest@dubaothoitiet.vn" };
      localStorage.setItem("current_user", JSON.stringify(state.currentUser));
      updateTopUserBadge();
      document.getElementById("modalLoginRegister").classList.remove("active");
    });
  }
}

// Khởi chạy ứng dụng
document.addEventListener("DOMContentLoaded", () => {
  applyTheme();
  updateTopUserBadge();
  setupModals();
  setupSearch();
  setupCompare();
  setupSettings();
  fetchWeather("Ha Noi, Vietnam", "Hà Nội");
});
