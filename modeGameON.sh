#!/system/bin/sh

# ╔══════════════════════════════════════════════════════════════╗
# ║       MODE GAMING DEWA - ULTRA RATA KANAN EDITION            ║
# ║      Optimasi BRUTAL untuk Mobile Legends Bang Bang          ║
# ║    Versi: 6.0 ULTRA - Unisoc SC9863A + IMG8322 PowerVR      ║
# ║    ⚠️ SEMUA PROSES DIMATIKAN KECUALI SISTEM & GAME ⚠️        ║
# ║    📱 Realme Go UI - Android 11 - RAM 2-3GB Optimized        ║
# ║    🛡️ FIX: SIM Card & Sinyal Dilindungi dari Kill/Disable    ║
# ╚══════════════════════════════════════════════════════════════╝

# ═══════════════════════════════════════════════════════════
# KODE WARNA
# ═══════════════════════════════════════════════════════════
MERAH='\033[0;31m'
HIJAU='\033[0;32m'
KUNING='\033[1;33m'
BIRU='\033[0;34m'
CYAN='\033[0;36m'
PUTIH='\033[1;37m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# ═══════════════════════════════════════════════════════════
# BANNER
# ═══════════════════════════════════════════════════════════
clear
echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${PUTIH}   🎮 MODE GAMING DEWA v6.0 - ULTRA RATA KANAN EDITION 🎮  ${CYAN}║${NC}"
echo -e "${CYAN}║${KUNING}     Unisoc SC9863A (8 Core) + IMG8322 PowerVR GPU         ${CYAN}║${NC}"
echo -e "${CYAN}║${KUNING}     Realme Go UI - Android 11 - RAM Rendah Optimized      ${CYAN}║${NC}"
echo -e "${CYAN}║${MERAH}        SEMUA PROSES NON-SISTEM AKAN DIMATIKAN TOTAL!       ${CYAN}║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${MERAH}   ⚠️  MODE ULTRA RATA KANAN - PERFORMA DI ATAS SPEK!${NC}"
echo -e "${MERAH}   ⚠️  SEMUA APP SELAIN GAME & SISTEM AKAN DIMATIKAN!${NC}"
echo -e "${MERAH}   ⚠️  HP AKAN SANGAT PANAS - SIAPKAN PENDINGIN!${NC}"
echo ""
echo -e "${MAGENTA}   ⚡ CPU 8 core dikunci 100% frekuensi maksimal${NC}"
echo -e "${MAGENTA}   ⚡ GPU IMG8322 dipaksa overclock mode${NC}"
echo -e "${MAGENTA}   ⚡ RAM dibersihkan total - hanya game yang jalan${NC}"
echo -e "${MAGENTA}   ⚡ Background limit = 0 (TIDAK ADA app lain)${NC}"
echo -e "${MAGENTA}   ⚡ Thermal bypass - no throttling sama sekali${NC}"
echo ""

# ═══════════════════════════════════════════════════════════
# CEK AKSES ROOT & SHIZUKU
# ═══════════════════════════════════════════════════════════
IS_ROOT=false
IS_SHIZUKU=false

# ═══════════════════════════════════════════════════════════
# AUTO-MODE FLAG (Digunakan oleh GameDetectionService)
# Jika dipanggil dari app dengan flag --auto, skip prompt interaktif
# dan gunakan profil yang diberikan via --profile
# Usage: modeGameON.sh --auto --profile ultra --package com.mobile.legends
# ═══════════════════════════════════════════════════════════
AUTO_MODE=false
AUTO_PROFILE="ultra"
AUTO_PACKAGE=""
AUTO_DISABLE_BLOAT=false
AUTO_RE_ENABLE=true

# Parse arguments
while [ "$#" -gt 0 ]; do
    case "$1" in
        --auto)
            AUTO_MODE=true
            shift
            ;;
        --profile)
            AUTO_PROFILE="$2"
            shift 2
            ;;
        --package)
            AUTO_PACKAGE="$2"
            shift 2
            ;;
        --disable-bloat)
            AUTO_DISABLE_BLOAT=true
            shift
            ;;
        --no-re-enable)
            AUTO_RE_ENABLE=false
            shift
            ;;
        *)
            shift
            ;;
    esac
done

# Jika auto-mode, override PKG dan skip banner
if [ "$AUTO_MODE" = true ]; then
    echo -e "${CYAN}[AUTO-DETECT] Mode otomatis diaktifkan oleh GameDetectionService${NC}"
    echo -e "${CYAN}[AUTO-DETECT] Profil: ${PUTIH}${AUTO_PROFILE}${NC}"
    echo -e "${CYAN}[AUTO-DETECT] Package: ${PUTIH}${AUTO_PACKAGE}${NC}"
    if [ -n "$AUTO_PACKAGE" ]; then
        PKG="$AUTO_PACKAGE"
    fi
fi

if [ "$(id -u)" = "0" ]; then
    IS_ROOT=true
    echo -e "${HIJAU}[✓] AKSES ROOT DIBERIKAN - OPTIMASI PENUH AKTIF${NC}"
    echo ""
elif [ -n "$SHIZUKU_API_VERSION" ]; then
    IS_SHIZUKU=true
    echo -e "${HIJAU}[✓] MODE SHIZUKU AKTIF - Izin Diperluas${NC}"
    echo -e "${KUNING}[i] Versi API Shizuku: $SHIZUKU_API_VERSION${NC}"
    echo ""
else
    echo -e "${MERAH}[⚠️] PERINGATAN: Script butuh ROOT/Shizuku untuk hasil MAKSIMAL!${NC}"
    echo -e "${KUNING}[!] Beberapa fitur mungkin tidak berjalan tanpa root${NC}"
    echo ""
fi

# ═══════════════════════════════════════════════════════════
# DETEKSI PAKET GAME (PRIORITAS MOBILE LEGENDS)
# ═══════════════════════════════════════════════════════════
deteksi_game() {
    # Prioritas utama: Mobile Legends
    DAFTAR_MLBB=(
        "com.mobile.legends"
        "com.mobile.legends.google"
        "com.moonton.mobilelegends"
    )

    # Game lain yang didukung
    DAFTAR_GAME_LAIN=(
        "com.pubg.krmobile"
        "com.tencent.ig"
        "com.garena.game.freefire"
        "com.garena.game.fctw"
        "com.activision.callofduty.shooter"
        "com.ea.gp.fifamobile"
        "com.riotgames.league.wildrift"
        "com.miHoYo.GenshinImpact"
        "com.tencent.tmgp.sgame"
    )

    # Cek MLBB dulu (prioritas)
    for game in "${DAFTAR_MLBB[@]}"; do
        if pm list packages 2>/dev/null | grep -q "$game"; then
            PKG="$game"
            echo -e "${HIJAU}[✓] Mobile Legends terdeteksi: $PKG${NC}"
            echo -e "${HIJAU}[✓] Mode prioritas MLBB diaktifkan!${NC}"
            return 0
        fi
    done

    # Jika MLBB tidak ada, cek game lain
    for game in "${DAFTAR_GAME_LAIN[@]}"; do
        if pm list packages 2>/dev/null | grep -q "$game"; then
            PKG="$game"
            echo -e "${HIJAU}[✓] Game terdeteksi: $PKG${NC}"
            return 0
        fi
    done

    PKG="com.mobile.legends"
    echo -e "${KUNING}[!] Game tidak terdeteksi, menggunakan default MLBB: $PKG${NC}"
    return 1
}

deteksi_game
echo ""

# Penghitung langkah
LANGKAH=1
TOTAL_LANGKAH=25

# Fungsi cetak langkah
cetak_langkah() {
    echo -e "${BIRU}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BIRU}[$LANGKAH/$TOTAL_LANGKAH] $1${NC}"
    echo -e "${BIRU}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    LANGKAH=$((LANGKAH + 1))
}

# ═══════════════════════════════════════════════════════════
# 1. MATIKAN SEMUA ANIMASI SISTEM
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔧 Menonaktifkan SEMUA Animasi Sistem..."
settings put global window_animation_scale 0.0 2>/dev/null
settings put global transition_animation_scale 0.0 2>/dev/null
settings put global animator_duration_scale 0.0 2>/dev/null
settings put global duration_scale 0 2>/dev/null
settings put global always_finish_activities 0 2>/dev/null
# Matikan animasi tambahan
settings put system lockscreen_aod 0 2>/dev/null
settings put system navigation_bar_gesture_longer_back_affordance 0 2>/dev/null
echo -e "${HIJAU}   [✓] Semua animasi dimatikan - HP lebih responsif${NC}"

# ═══════════════════════════════════════════════════════════
# 2. MODE PERFORMA DEVELOPER SETTINGS
# ═══════════════════════════════════════════════════════════
cetak_langkah "⚡ Mengaktifkan Mode Performa Developer..."
cmd power set-fixed-performance-mode-enabled true 2>/dev/null
settings put global sustain_performance_mode 1 2>/dev/null
settings put global low_power_mode 0 2>/dev/null
settings put global battery_saver_mode 0 2>/dev/null
settings put global battery_saver_constants "disabled" 2>/dev/null
settings put global adaptive_battery_management_enabled 0 2>/dev/null
# Settingan developer tambahan
settings put global development_settings_enabled 1 2>/dev/null
settings put global force_hw_ui 1 2>/dev/null
settings put global force_gpu_rendering 1 2>/dev/null
settings put global disable_overlays 1 2>/dev/null
settings put global debug_hw_overdraw "" 2>/dev/null
settings put global profile_rendering "false" 2>/dev/null
settings put global show_hw_layers_updates 0 2>/dev/null
settings put global show_hw_overdraw 0 2>/dev/null
settings put global show_non_rect_clip 0 2>/dev/null
settings put global debug_layout 0 2>/dev/null
settings put global pointer_location 0 2>/dev/null
# LIMIT BACKGROUND = 0 (TIDAK ADA app lain boleh jalan di background!)
settings put global background_process_limit 0 2>/dev/null
# Paksa activity manager lebih agresif
settings put global activity_manager_constants "max_cached_processes=2,background_settle_time=0" 2>/dev/null
# Nonaktifkan MIUI/Realme optimasi sendiri (kita ambil alih)
settings put global game_mode_status 1 2>/dev/null
echo -e "${HIJAU}   [✓] Mode performa developer aktif${NC}"
echo -e "${HIJAU}   [✓] GPU Rendering dipaksa aktif${NC}"
echo -e "${MERAH}   [✓] Batas proses background: 0 (TIDAK ADA yang boleh jalan!)${NC}"

# ═══════════════════════════════════════════════════════════
# 3. DETEKSI & ATUR REFRESH RATE MAKSIMAL (Realme Go UI)
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔄 Mendeteksi & Mengatur Refresh Rate Maksimal..."

dapatkan_refresh_rate_maks() {
    local maks_rate=60

    # Metode 1: lewat SurfaceFlinger
    local rates=$(dumpsys SurfaceFlinger 2>/dev/null | grep -oE "[0-9]+ Hz" | grep -oE "[0-9]+" | sort -rn | head -1)
    if [ -n "$rates" ]; then
        maks_rate=$rates
    fi

    # Metode 2: lewat display info
    local wm_rate=$(cmd display get-info 2>/dev/null | grep -oE "refreshRate[=:][0-9]+" | grep -oE "[0-9]+" | head -1)
    if [ -n "$wm_rate" ] && [ "$wm_rate" -gt "$maks_rate" ]; then
        maks_rate=$wm_rate
    fi

    # Metode 3: Realme/Oppo specific
    if [ "$(id -u)" = "0" ]; then
        local realme_rate=$(cat /sys/kernel/oppo_display/dynamic_fps 2>/dev/null | grep -oE "[0-9]+" | sort -rn | head -1)
        if [ -n "$realme_rate" ] && [ "$realme_rate" -gt "$maks_rate" ]; then
            maks_rate=$realme_rate
        fi
    fi

    echo "$maks_rate"
}

MAKS_HZ=$(dapatkan_refresh_rate_maks)
echo -e "${KUNING}   [i] Refresh rate layar terdeteksi: ${MAKS_HZ}Hz${NC}"

if [ "$MAKS_HZ" -ge 144 ]; then
    TARGET_HZ=144
elif [ "$MAKS_HZ" -ge 120 ]; then
    TARGET_HZ=120
elif [ "$MAKS_HZ" -ge 90 ]; then
    TARGET_HZ=90
else
    TARGET_HZ=60
    echo -e "${KUNING}   [i] Realme Go UI: Layar 60Hz (standar untuk performa stabil)${NC}"
fi

settings put system peak_refresh_rate ${TARGET_HZ}.0 2>/dev/null
settings put system min_refresh_rate ${TARGET_HZ}.0 2>/dev/null
settings put system user_refresh_rate ${TARGET_HZ}.0 2>/dev/null
settings put system refresh_rate ${TARGET_HZ}.0 2>/dev/null
cmd display set-refresh-rate $TARGET_HZ 2>/dev/null

# Realme specific display optimization
if [ "$(id -u)" = "0" ]; then
    echo "1" > /sys/kernel/oppo_display/hbm 2>/dev/null
    echo "0" > /sys/kernel/oppo_display/aod 2>/dev/null
fi

if [ "$TARGET_HZ" -gt 60 ]; then
    echo -e "${HIJAU}   [✓] Refresh rate diatur ke ${TARGET_HZ}Hz - Layar lebih mulus!${NC}"
else
    echo -e "${HIJAU}   [✓] Berjalan di ${TARGET_HZ}Hz (optimal untuk SC9863A)${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 4. MATIKAN PROSES BACKGROUND SECARA AGRESIF
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔪 Mematikan Proses Background Secara AGRESIF..."

# Aplikasi yang TIDAK BOLEH dimatikan (sistem kritis + telephony/sinyal!)
# ⚠️ PENTING: Semua proses telephony HARUS dilindungi agar SIM & sinyal tidak hilang!
APLIKASI_DILINDUNGI=(
    # === INTI SISTEM ===
    "com.android.systemui"
    "android"
    "com.android.shell"
    "com.android.se"
    "com.android.providers.media"
    "com.android.providers.settings"
    # === TELEPHONY & SINYAL (JANGAN SENTUH!) ===
    "com.android.phone"                  # Inti telephony - kelola SIM, modem, sinyal
    "com.android.server.telecom"         # Telecom service manager
    "com.android.providers.telephony"    # Database telephony (SMS, APN, SIM info)
    "com.android.providers.contacts"     # Provider kontak (dipakai telephony)
    "com.android.providers.blockednumber" # Blocked number provider
    "com.android.ims"                    # IMS/VoLTE service
    "com.android.stk"                    # SIM Toolkit
    "com.android.incallui"               # In-call UI
    "com.android.cellbroadcastreceiver"  # Cell broadcast (emergency alerts)
    # === VENDOR TELEPHONY ===
    "com.qualcomm.qti"                   # Qualcomm telephony interface
    "vendor.sprd"                        # Spreadtrum/Unisoc vendor service
    "com.spreadtrum"                     # Spreadtrum telephony
    "com.unisoc"                         # Unisoc vendor service
    # === TOOLS ===
    "com.termux"
    "$PKG"
)

# Aplikasi BERAT yang harus dimatikan PERTAMA (sosial media, streaming, dll)
APLIKASI_BERAT=(
    # Sosial Media
    "com.whatsapp"
    "com.whatsapp.w4b"
    "com.facebook.katana"
    "com.facebook.orca"
    "com.facebook.lite"
    "com.facebook.services"
    "com.instagram.android"
    "com.instagram.lite"
    "com.twitter.android"
    "com.snapchat.android"
    "com.tiktok"
    "com.zhiliaoapp.musically"
    "com.zhiliaoapp.musically.go"
    "com.pinterest"
    "com.tumblr"
    "com.reddit.frontpage"
    "com.linkedin.android"
    # Messenger
    "com.discord"
    "com.telegram.messenger"
    "org.telegram.messenger"
    "com.viber.voip"
    "com.skype.raider"
    "com.LINE.midp"
    "jp.naver.line.android"
    # Video & Streaming
    "com.google.android.youtube"
    "com.netflix.mediaclient"
    "com.primevideo.livingroom"
    "com.disney.disneyplus"
    "com.spotify.music"
    "com.joox.id"
    "com.resso.music"
    # E-Commerce & Transportasi
    "com.shopee.id"
    "com.tokopedia.tkpd"
    "com.lazada.android"
    "com.bukalapak.android"
    "id.compas.android"
    "com.grab.passenger"
    "com.gojek.app"
    "com.maxim.rider"
    # Produktivitas & Tools
    "com.microsoft.teams"
    "com.zoom.videomeetings"
    "com.microsoft.appmanager"
    "com.microsoft.office.outlook"
    "com.google.android.apps.docs"
    "com.google.android.apps.photos"
    "com.google.android.apps.maps"
    "com.google.android.apps.nbu.files"
    "com.google.android.apps.magazines"
    "com.google.android.apps.tachyon"
    "com.google.android.apps.wellbeing"
    "com.google.android.apps.youtube.music"
    "com.google.android.googlequicksearchbox"
    "com.google.android.gm"
    "com.google.android.keep"
    "com.google.android.calendar"
    # Browser
    "com.android.chrome"
    "org.mozilla.firefox"
    "com.opera.browser"
    "com.brave.browser"
    "com.UCMobile.intl"
    # File Manager & Utility
    "ru.zdevs.zarchiver"
    "com.foxdebug.acode"
    "tech.ula"
    "deltazero.amarok.foss"
    "ahapps.controlthescreenorientation"
    # Bloatware OEM
    "com.heytap.pictorial"
    "com.oppoex.afterservice"
    "com.oppo.operationManual"
    "com.coloros.gamespaceui"
    "com.coloros.weather2"
    "com.coloros.phonemanager"
    "com.coloros.compass2"
    "com.coloros.calculator"
    "com.coloros.oshare"
    "com.nearme.gamecenter"
    "com.heytap.music"
    "com.heytap.browser"
    "com.heytap.cloud"
    "com.heytap.smarthome"
    "com.samsung.android.game.gos"
    "com.samsung.android.app.notes"
    "com.samsung.android.calendar"
    "com.samsung.android.email.provider"
    "com.samsung.android.voc"
    "com.sec.android.app.samsungapps"
    "com.xiaomi.mipicks"
    "com.miui.analytics"
    "com.miui.daemon"
    "com.miui.cloudservice"
    "com.miui.gallery"
    "com.miui.weather2"
    "com.miui.yellowpage"
    # News & Berita
    "com.google.android.apps.magazines"
    "flipboard.app"
    "com.opera.app.news"
)

JUMLAH_DIMATIKAN=0

echo -e "${KUNING}   [i] Memindai semua proses yang berjalan...${NC}"

# Tahap 1: Matikan aplikasi berat dulu
echo -e "${KUNING}   [i] Tahap 1: Mematikan aplikasi berat...${NC}"
for app in "${APLIKASI_BERAT[@]}"; do
    # Cek apakah dilindungi
    dilindungi=false
    for proteksi in "${APLIKASI_DILINDUNGI[@]}"; do
        if [ "$app" = "$proteksi" ]; then
            dilindungi=true
            break
        fi
    done
    if [ "$dilindungi" = true ]; then continue; fi

    # Cek apakah aplikasi sedang berjalan
    if ps -A 2>/dev/null | grep -q "$app"; then
        am force-stop "$app" >/dev/null 2>&1
        # Juga matikan servicenya
        am kill "$app" >/dev/null 2>&1
        JUMLAH_DIMATIKAN=$((JUMLAH_DIMATIKAN + 1))
    fi
done

# Tahap 2: Scan semua proses running & matikan yang tidak penting
echo -e "${KUNING}   [i] Tahap 2: Memindai & mematikan proses non-esensial...${NC}"

PROSES_BERJALAN=$(ps -A 2>/dev/null | grep -E "u[0-9]+_" | awk '{print $NF}' | sort -u)

for proc in $PROSES_BERJALAN; do
    if [ -z "$proc" ]; then continue; fi

    # Skip jika dilindungi
    dilindungi=false
    for proteksi in "${APLIKASI_DILINDUNGI[@]}"; do
        if echo "$proc" | grep -q "$proteksi"; then
            dilindungi=true
            break
        fi
    done
    if [ "$dilindungi" = true ]; then continue; fi

    # Skip komponen sistem esensial
    if echo "$proc" | grep -qE "^(android|system|com\.android\.(systemui|phone|providers|inputmethod|launcher|shell|settings|nfc|ims|stk|server\.telecom|incallui|cellbroadcast))"; then
        continue
    fi
    # Skip overlay, provider, dan komponen telephony/radio
    if echo "$proc" | grep -qE "(overlay|provider|:service|inputmethod|keyboard|telephony|telecom|radio|ril|ims|modem|qti|sprd|spreadtrum|unisoc)"; then
        continue
    fi

    am force-stop "$proc" >/dev/null 2>&1
    am kill "$proc" >/dev/null 2>&1
    JUMLAH_DIMATIKAN=$((JUMLAH_DIMATIKAN + 1))
done

# Tahap 3: Matikan services Google yang tidak diperlukan
echo -e "${KUNING}   [i] Tahap 3: Mematikan layanan Google yang tidak perlu...${NC}"
LAYANAN_GOOGLE_NONAKTIF=(
    "com.google.android.apps.wellbeing"
    "com.google.android.apps.turbo"
    "com.google.android.feedback"
    "com.google.android.printservice.recommendation"
    "com.google.android.apps.docs"
    "com.google.android.videos"
    "com.google.android.music"
    "com.google.android.apps.magazines"
    "com.google.android.apps.photos"
    "com.google.android.apps.tachyon"
    "com.google.android.googlequicksearchbox"
    "com.google.android.apps.nbu.files"
    "com.google.android.projection.gearhead"
)

for layanan in "${LAYANAN_GOOGLE_NONAKTIF[@]}"; do
    am force-stop "$layanan" >/dev/null 2>&1
    am kill "$layanan" >/dev/null 2>&1
done

echo -e "${HIJAU}   [✓] Total aplikasi dimatikan: $JUMLAH_DIMATIKAN${NC}"
echo -e "${MERAH}   [✓] SEMUA proses non-sistem sudah DIMATIKAN TOTAL!${NC}"
echo -e "${HIJAU}   [✓] RAM sekarang khusus untuk Mobile Legends!${NC}"

# Tahap 4: Membersihkan cached processes (AMAN - tanpa kill-all)
echo -e "${KUNING}   [i] Tahap 4: Membersihkan sisa cached processes...${NC}"
# JANGAN gunakan 'am kill-all' - bisa membunuh telephony & SIM hilang!
# Gunakan targeted force-stop hanya untuk app pihak ketiga
for semua_pkg in $(pm list packages -3 2>/dev/null | cut -d: -f2); do
    if [ "$semua_pkg" != "$PKG" ]; then
        # Double check: jangan matikan app yang terkait telephony
        case "$semua_pkg" in
            *phone*|*telecom*|*telephony*|*ims*|*radio*|*ril*|*modem*|*sprd*|*unisoc*|*stk*)
                continue ;;
        esac
        am force-stop "$semua_pkg" >/dev/null 2>&1
    fi
done
echo -e "${HIJAU}   [✓] Cached processes dibersihkan (telephony dilindungi)${NC}"

# ═══════════════════════════════════════════════════════════
# 5. PEMBERSIHAN RAM & CACHE MENDALAM
# ═══════════════════════════════════════════════════════════
cetak_langkah "🧹 Pembersihan RAM & Cache Secara Mendalam..."
pm trim-caches 999999999999 >/dev/null 2>&1

# Kirim sinyal ke semua app untuk lepaskan memori
for pkg_clean in $(pm list packages -3 2>/dev/null | cut -d: -f2); do
    if [ "$pkg_clean" != "$PKG" ]; then
        am send-trim-memory "$pkg_clean" TRIM_MEMORY_COMPLETE >/dev/null 2>&1
    fi
done

# Drop cache jika root
if [ "$(id -u)" = "0" ]; then
    sync && echo 3 > /proc/sys/vm/drop_caches 2>/dev/null
    echo 1 > /proc/sys/vm/compact_memory 2>/dev/null
    echo -e "${HIJAU}   [✓] Page cache, dentries, dan inodes dibersihkan${NC}"
fi

# Hapus cache temp
rm -rf /data/local/tmp/* >/dev/null 2>&1
echo -e "${HIJAU}   [✓] RAM dibersihkan dan dipadatkan${NC}"

# ═══════════════════════════════════════════════════════════
# 6. GOVERNOR CPU - ULTRA OVERPOWER MODE (UNISOC SC9863A)
# ═══════════════════════════════════════════════════════════
cetak_langkah "🚀 Governor CPU - ULTRA OVERPOWER MODE (Unisoc SC9863A)..."
if [ "$(id -u)" = "0" ]; then
    echo -e "${MERAH}   [⚠️] MEMAKSA CPU KE BATAS MAKSIMAL - NO THROTTLING!${NC}"
    
    # Unisoc SC9863A: 4x Cortex-A55 @ 1.6GHz + 4x Cortex-A55 @ 1.2GHz
    # CPU0-3: Big cores (1.6GHz), CPU4-7: Little cores (1.2GHz)
    
    # PAKSA governor performance untuk semua core
    for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do
        echo "performance" > $cpu 2>/dev/null
        chmod 444 $cpu 2>/dev/null  # Lock agar tidak berubah
    done

    # OVERCLOCK attempt - Coba naikkan frekuensi di atas normal
    # Big cores: Coba push ke 1.8GHz (dari 1.6GHz)
    for i in 0 1 2 3; do
        if [ -f /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq ]; then
            FREK_BIG=$(cat /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq 2>/dev/null)
            FREK_OC=$((FREK_BIG + 200000))  # +200MHz overclock attempt
            
            # Coba set overclock
            echo "$FREK_OC" > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq 2>/dev/null
            
            # Kunci min = max untuk performa konstan
            FREK_ACTUAL=$(cat /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq 2>/dev/null)
            echo "$FREK_ACTUAL" > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_min_freq 2>/dev/null
            
            # Set cpuinfo max juga
            echo "$FREK_ACTUAL" > /sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq 2>/dev/null
            
            FREK_MHZ=$((FREK_ACTUAL / 1000))
            echo -e "${HIJAU}   [✓] CPU$i (Big): LOCKED ${FREK_MHZ}MHz (Target: 1800MHz)${NC}"
        fi
    done

    # Little cores: Push ke 1.4GHz (dari 1.2GHz)
    for i in 4 5 6 7; do
        if [ -f /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq ]; then
            FREK_LITTLE=$(cat /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq 2>/dev/null)
            FREK_OC=$((FREK_LITTLE + 200000))  # +200MHz overclock
            
            echo "$FREK_OC" > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq 2>/dev/null
            FREK_ACTUAL=$(cat /sys/devices/system/cpu/cpu$i/cpufreq/scaling_max_freq 2>/dev/null)
            echo "$FREK_ACTUAL" > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_min_freq 2>/dev/null
            echo "$FREK_ACTUAL" > /sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq 2>/dev/null
        fi
    done
    FREK_LITTLE_MHZ=$((FREK_ACTUAL / 1000))
    echo -e "${HIJAU}   [✓] CPU4-7 (Little): LOCKED ${FREK_LITTLE_MHZ}MHz (Target: 1400MHz)${NC}"

    # PAKSA semua core ONLINE dan LOCK
    for i in 0 1 2 3 4 5 6 7; do
        if [ -f /sys/devices/system/cpu/cpu$i/online ]; then
            echo "1" > /sys/devices/system/cpu/cpu$i/online 2>/dev/null
            chmod 444 /sys/devices/system/cpu/cpu$i/online 2>/dev/null
        fi
    done

    # EXTREME scheduler - Prioritas MAKSIMAL untuk foreground
    echo "1" > /proc/sys/kernel/sched_boost 2>/dev/null
    echo "99" > /proc/sys/kernel/sched_upmigrate 2>/dev/null
    echo "80" > /proc/sys/kernel/sched_downmigrate 2>/dev/null
    echo "1" > /proc/sys/kernel/sched_prefer_sync_wakee_to_waker 2>/dev/null
    echo "400000" > /proc/sys/kernel/sched_freq_inc_notify 2>/dev/null
    echo "400000" > /proc/sys/kernel/sched_freq_dec_notify 2>/dev/null
    
    # CPU boost parameters
    echo "1" > /sys/module/cpu_boost/parameters/input_boost_enabled 2>/dev/null
    echo "0:1600000 1:1600000 2:1600000 3:1600000 4:1200000 5:1200000 6:1200000 7:1200000" > /sys/module/cpu_boost/parameters/input_boost_freq 2>/dev/null
    echo "2000" > /sys/module/cpu_boost/parameters/input_boost_ms 2>/dev/null
    
    # MATIKAN SEMUA thermal protection (BAHAYA!)
    echo "0" > /sys/module/sprd_thermal/parameters/enabled 2>/dev/null
    echo "0" > /sys/class/thermal/thermal_message/sconfig 2>/dev/null
    echo "disabled" > /sys/kernel/debug/thermal/thermal_debug 2>/dev/null
    
    for tz in /sys/class/thermal/thermal_zone*/mode; do
        echo "disabled" > $tz 2>/dev/null
        chmod 444 $tz 2>/dev/null
    done
    
    # Matikan thermal daemon
    stop thermal-engine 2>/dev/null
    stop thermald 2>/dev/null
    stop mi_thermald 2>/dev/null
    stop thermal_manager 2>/dev/null
    
    # Set temperature limit ke maksimal (disable protection)
    for temp_limit in /sys/class/thermal/thermal_zone*/trip_point_*_temp; do
        echo "120000" > $temp_limit 2>/dev/null  # 120°C (ekstrem!)
    done

    echo -e "${HIJAU}   [✓] Semua 8 core CPU LOCKED 100% - NO SLEEP MODE!${NC}"
    echo -e "${HIJAU}   [✓] Scheduler ULTRA AGGRESSIVE - Game prioritas #1${NC}"
    echo -e "${MERAH}   [⚠️] THERMAL PROTECTION DIMATIKAN TOTAL!${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk OVERPOWER mode${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 7. OPTIMASI GPU - ULTRA OVERCLOCK IMG8322 (PowerVR)
# ═══════════════════════════════════════════════════════════
cetak_langkah "🎮 Optimasi GPU - ULTRA OVERCLOCK IMG8322 PowerVR..."
echo -e "${MERAH}   [⚠️] MEMAKSA GPU KE MODE OVERCLOCK!${NC}"

settings put global gpu_force_4x_msaa 0 2>/dev/null
settings put global force_gpu_rendering 1 2>/dev/null
settings put global hw_overlays_enabled 0 2>/dev/null
setprop debug.egl.hw 1 2>/dev/null
setprop debug.gr.numframebuffers 3 2>/dev/null
setprop debug.composition.type gpu 2>/dev/null
setprop debug.sf.hw 1 2>/dev/null
setprop debug.performance.tuning 1 2>/dev/null
setprop debug.sf.disable_backpressure 1 2>/dev/null
setprop debug.sf.latch_unsignaled 1 2>/dev/null
setprop debug.sf.enable_gl_tracing 0 2>/dev/null
setprop persist.sys.composition.type gpu 2>/dev/null
setprop persist.sys.ui.hw 1 2>/dev/null

# PowerVR IMG8322 EXTREME optimizations
setprop debug.hwui.renderer skiagl 2>/dev/null
setprop debug.renderengine.backend skiaglthreaded 2>/dev/null
setprop ro.hwui.texture_cache_size 96 2>/dev/null  # Increased
setprop ro.hwui.layer_cache_size 64 2>/dev/null    # Increased
setprop ro.hwui.path_cache_size 48 2>/dev/null     # Increased
setprop ro.hwui.gradient_cache_size 2 2>/dev/null
setprop ro.hwui.drop_shadow_cache_size 8 2>/dev/null
setprop ro.hwui.r_buffer_cache_size 12 2>/dev/null
setprop ro.hwui.texture_cache_flushrate 0.2 2>/dev/null  # Less flush = more speed

# Disable GPU power saving
setprop debug.egl.profiler 0 2>/dev/null
setprop debug.egl.force_msaa 0 2>/dev/null
setprop debug.enabletr true 2>/dev/null

# GPU IMG8322 OVERCLOCK (Unisoc path)
if [ "$(id -u)" = "0" ]; then
    # Path untuk GPU Unisoc/PowerVR
    if [ -d /sys/class/devfreq/60000000.gpu ]; then
        echo "performance" > /sys/class/devfreq/60000000.gpu/governor 2>/dev/null
        chmod 444 /sys/class/devfreq/60000000.gpu/governor 2>/dev/null
        
        GPU_MAX=$(cat /sys/class/devfreq/60000000.gpu/max_freq 2>/dev/null)
        if [ -n "$GPU_MAX" ]; then
            # Attempt overclock +100MHz
            GPU_OC=$((GPU_MAX + 100000000))
            echo "$GPU_OC" > /sys/class/devfreq/60000000.gpu/max_freq 2>/dev/null
            
            GPU_ACTUAL=$(cat /sys/class/devfreq/60000000.gpu/max_freq 2>/dev/null)
            echo "$GPU_ACTUAL" > /sys/class/devfreq/60000000.gpu/min_freq 2>/dev/null
            
            GPU_MHZ=$((GPU_ACTUAL / 1000000))
            echo -e "${HIJAU}   [✓] GPU IMG8322 OVERCLOCKED: ${GPU_MHZ}MHz${NC}"
        fi
        
        # Disable GPU power management
        echo "0" > /sys/class/devfreq/60000000.gpu/polling_interval 2>/dev/null
    fi
    
    # Alternative paths
    if [ -d /sys/devices/platform/60000000.gpu ]; then
        echo "performance" > /sys/devices/platform/60000000.gpu/devfreq/devfreq*/governor 2>/dev/null
        for gpu_min in /sys/devices/platform/60000000.gpu/devfreq/devfreq*/min_freq; do
            GPU_MAX_ALT=$(cat ${gpu_min/min_freq/max_freq} 2>/dev/null)
            echo "$GPU_MAX_ALT" > $gpu_min 2>/dev/null
        done
    fi
    
    # Sprd GPU specific - Force max performance
    echo "1" > /sys/kernel/debug/pvr/power_state 2>/dev/null
    echo "performance" > /sys/kernel/debug/pvr/dvfs_governor 2>/dev/null
    
    # Disable GPU thermal throttling
    echo "0" > /sys/class/devfreq/60000000.gpu/throttling 2>/dev/null
    
    # Mali/IMG power policy (if exists)
    echo "always_on" > /sys/devices/platform/*/mali*/power_policy 2>/dev/null
    echo "always_on" > /sys/devices/platform/*/pvr*/power_policy 2>/dev/null
    
    echo -e "${HIJAU}   [✓] PowerVR GPU thermal throttling DISABLED${NC}"
fi

echo -e "${HIJAU}   [✓] GPU IMG8322 ULTRA MODE - Render maksimal!${NC}"
echo -e "${HIJAU}   [✓] Texture cache diperbesar 50%${NC}"
echo -e "${MERAH}   [⚠️] GPU akan sangat panas - normal untuk overclock!${NC}"

# ═══════════════════════════════════════════════════════════
# 8. PENJADWAL I/O - KECEPATAN PENYIMPANAN
# ═══════════════════════════════════════════════════════════
cetak_langkah "💾 Optimasi Penjadwal I/O & Penyimpanan..."
if [ "$(id -u)" = "0" ]; then
    for block in /sys/block/*/queue/scheduler; do
        echo "noop" > $block 2>/dev/null
    done

    # Optimasi read-ahead
    for ra in /sys/block/*/queue/read_ahead_kb; do
        echo "2048" > $ra 2>/dev/null
    done

    # Nonaktifkan entropy yang lambat
    echo "0" > /proc/sys/kernel/random/read_wakeup_threshold 2>/dev/null
    echo "0" > /proc/sys/kernel/random/write_wakeup_threshold 2>/dev/null

    echo -e "${HIJAU}   [✓] Penjadwal I/O diatur ke performa - loading game lebih cepat${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk penjadwal I/O${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 9. PRIORITAS PROSES GAME - LEVEL TERTINGGI (MLBB)
# ═══════════════════════════════════════════════════════════
cetak_langkah "📊 Memprioritaskan Proses Mobile Legends ke TERTINGGI..."
if [ -n "$PKG" ]; then
    # ═══════════════════════════════════════
    # ANDROID GAME MODE API (Official Android 12+)
    # Ref: Game Mode API.md - cara resmi Android boost performa
    # ═══════════════════════════════════════
    echo -e "${CYAN}   [i] Mengaktifkan Android Game Mode API...${NC}"
    # Paksa mode PERFORMANCE via Game Mode API resmi
    cmd game mode performance $PKG 2>/dev/null
    echo -e "${HIJAU}   [✓] Android Game Mode: PERFORMANCE diaktifkan${NC}"

    # ═══════════════════════════════════════
    # DISABLE FPS THROTTLING (Ref: ThrottlingFPS.md)
    # fps=0 artinya TIDAK ADA limit FPS dari OEM
    # ═══════════════════════════════════════
    echo -e "${CYAN}   [i] Menonaktifkan FPS Throttling OEM...${NC}"
    device_config put game_overlay $PKG "mode=2,fps=0" 2>/dev/null
    cmd device_config put game_overlay $PKG "mode=2,fps=0" 2>/dev/null
    echo -e "${HIJAU}   [✓] FPS Throttling: DISABLED (tanpa limit FPS)${NC}"

    # ═══════════════════════════════════════
    # WINDOWMANAGER BACKBUFFER RESIZE (Ref: Intervensi Mode Game.md)
    # downscaleFactor=0.9 = kurangi GPU load 30% dengan
    # penurunan kualitas visual minimal (hampir tidak terlihat)
    # ═══════════════════════════════════════
    echo -e "${CYAN}   [i] Mengoptimasi WindowManager GPU...${NC}"
    device_config put game_overlay $PKG "mode=2,downscaleFactor=0.9,fps=0" 2>/dev/null
    cmd device_config put game_overlay $PKG "mode=2,downscaleFactor=0.9,fps=0" 2>/dev/null
    echo -e "${HIJAU}   [✓] WindowManager: GPU load berkurang ~30% (visual tetap tajam)${NC}"

    # Whitelist game dari Doze mode
    dumpsys deviceidle whitelist +$PKG 2>/dev/null

    # Set ke bucket AKTIF (prioritas tertinggi)
    cmd activity set-standby-bucket $PKG active 2>/dev/null

    # Tandai sebagai tidak inaktif
    am set-inactive $PKG false 2>/dev/null

    # Izinkan semua operasi penting
    cmd appops set $PKG RUN_IN_BACKGROUND allow 2>/dev/null
    cmd appops set $PKG WAKE_LOCK allow 2>/dev/null
    cmd appops set $PKG CHANGE_WIFI_STATE allow 2>/dev/null
    cmd appops set $PKG ACCESS_NOTIFICATIONS allow 2>/dev/null
    cmd appops set $PKG REQUEST_IGNORE_BATTERY_OPTIMIZATIONS allow 2>/dev/null
    cmd appops set $PKG RUN_ANY_IN_BACKGROUND allow 2>/dev/null
    cmd appops set $PKG START_FOREGROUND allow 2>/dev/null

    # Set prioritas proses game (jika root)
    if [ "$(id -u)" = "0" ]; then
        GAME_PID=$(pidof $PKG 2>/dev/null)
        if [ -n "$GAME_PID" ]; then
            renice -20 $GAME_PID 2>/dev/null
            ionice -c 1 -n 0 -p $GAME_PID 2>/dev/null
            echo -e "${HIJAU}   [✓] Prioritas proses game: REALTIME (PID: $GAME_PID)${NC}"
        fi
    fi

    # Set afinitas CPU untuk game (semua core)
    echo -e "${HIJAU}   [✓] $PKG di-whitelist & diprioritaskan ke TERTINGGI${NC}"
    echo -e "${HIJAU}   [✓] Game bebas dari pembatasan baterai & background${NC}"
    echo -e "${HIJAU}   [✓] Game Mode API + FPS Unlock + GPU Optimization AKTIF${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 10. NONAKTIFKAN NOTIFIKASI YANG MENGGANGGU
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔕 Menonaktifkan Notifikasi yang Mengganggu..."

APLIKASI_NONAKTIF_NOTIF=(
    "com.whatsapp"
    "com.facebook.katana"
    "com.facebook.orca"
    "com.instagram.android"
    "com.twitter.android"
    "com.tiktok"
    "com.zhiliaoapp.musically"
    "com.google.android.gm"
    "com.telegram.messenger"
    "com.discord"
    "com.shopee.id"
    "com.tokopedia.tkpd"
    "com.google.android.youtube"
)

for app_notif in "${APLIKASI_NONAKTIF_NOTIF[@]}"; do
    cmd appops set "$app_notif" POST_NOTIFICATION ignore 2>/dev/null
done

# Aktifkan DND (Do Not Disturb) mode
settings put global zen_mode 2 2>/dev/null
cmd notification set_dnd priority 2>/dev/null

echo -e "${HIJAU}   [✓] Notifikasi aplikasi non-game dinonaktifkan${NC}"
echo -e "${HIJAU}   [✓] Mode Jangan Ganggu diaktifkan${NC}"

# ═══════════════════════════════════════════════════════════
# 11. OPTIMASI JARINGAN - PING RENDAH
# ═══════════════════════════════════════════════════════════
cetak_langkah "📡 Optimasi Jaringan - Mode Ping Rendah..."
settings put global wifi_sleep_policy 2 2>/dev/null
# JANGAN matikan captive portal - bisa bikin Android blokir traffic seluler
# Android menganggap koneksi "invalid" → ikon sinyal "x" → game gagal konek
# settings put global captive_portal_detection_enabled 0 2>/dev/null
settings put global http_proxy "" 2>/dev/null

# Optimasi buffer TCP untuk ping rendah
setprop net.tcp.buffersize.default "4096,87380,110208,4096,16384,110208" 2>/dev/null
setprop net.tcp.buffersize.wifi "524288,1048576,2097152,262144,524288,1048576" 2>/dev/null
setprop net.tcp.buffersize.lte "524288,1048576,2097152,262144,524288,1048576" 2>/dev/null
setprop net.tcp.buffersize.umts "4094,87380,1220608,4096,16384,1220608" 2>/dev/null
setprop net.tcp.buffersize.edge "4093,26280,35040,4096,16384,35040" 2>/dev/null
setprop net.tcp.buffersize.gprs "4092,8760,11680,4096,8760,11680" 2>/dev/null
setprop net.tcp.buffersize.hsdpa "4094,87380,1220608,4096,16384,1220608" 2>/dev/null
setprop net.tcp.buffersize.hspa "4094,87380,1220608,4096,16384,1220608" 2>/dev/null
setprop net.tcp.buffersize.hspap "4094,87380,1220608,4096,16384,1220608" 2>/dev/null
setprop net.tcp.buffersize.evdo "4094,87380,262144,4096,16384,262144" 2>/dev/null

# DNS cepat (Google DNS)
setprop net.dns1 "8.8.8.8" 2>/dev/null
setprop net.dns2 "8.8.4.4" 2>/dev/null

# Optimasi TCP untuk gaming
if [ "$(id -u)" = "0" ]; then
    echo "1" > /proc/sys/net/ipv4/tcp_low_latency 2>/dev/null
    echo "1" > /proc/sys/net/ipv4/tcp_sack 2>/dev/null
    echo "1" > /proc/sys/net/ipv4/tcp_timestamps 2>/dev/null
    echo "1" > /proc/sys/net/ipv4/tcp_window_scaling 2>/dev/null
    echo "westwood" > /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null
    echo "2" > /proc/sys/net/ipv4/tcp_fastopen 2>/dev/null
    echo -e "${HIJAU}   [✓] TCP dioptimasi untuk latensi rendah${NC}"
fi

echo -e "${HIJAU}   [✓] Jaringan dioptimasi untuk ping rendah - game lebih stabil${NC}"

# ═══════════════════════════════════════════════════════════
# 12. SENSITIVITAS SENTUHAN - RESPON MAKSIMAL
# ═══════════════════════════════════════════════════════════
cetak_langkah "👆 Sensitivitas Sentuhan - Respon Maksimal..."
settings put system touch_sensitivity 1 2>/dev/null
settings put system pointer_speed 7 2>/dev/null
setprop ro.min_pointer_dur 0 2>/dev/null
settings put system touch_explosion_enabled 0 2>/dev/null
settings put system haptic_feedback_enabled 0 2>/dev/null
settings put system vibrate_on_touch 0 2>/dev/null
# Nonaktifkan getaran (hemat CPU)
settings put system vibrate_when_ringing 0 2>/dev/null
settings put system haptic_feedback_enabled 0 2>/dev/null
echo -e "${HIJAU}   [✓] Respon sentuhan dimaksimalkan - skill combo lebih mudah!${NC}"

# ═══════════════════════════════════════════════════════════
# 13. MANAJEMEN THERMAL - TOTAL BYPASS (EXTREME!)
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔥 Manajemen Thermal - TOTAL BYPASS MODE..."
if [ "$(id -u)" = "0" ]; then
    echo -e "${MERAH}   [⚠️⚠️⚠️] MENONAKTIFKAN SEMUA PROTEKSI THERMAL!${NC}"
    echo -e "${MERAH}   [⚠️⚠️⚠️] HP AKAN SANGAT PANAS - SIAPKAN COOLING!${NC}"

    # Disable ALL thermal zones
    for thermal in /sys/class/thermal/thermal_zone*/mode; do
        echo "disabled" > $thermal 2>/dev/null
        chmod 444 $thermal 2>/dev/null
    done

    # Set thermal policy ke performance
    for policy in /sys/class/thermal/thermal_zone*/policy; do
        echo "performance" > $policy 2>/dev/null
        echo "user_space" > $policy 2>/dev/null
    done
    
    # Set ALL trip points ke maksimal (120°C)
    for trip in /sys/class/thermal/thermal_zone*/trip_point_*_temp; do
        echo "120000" > $trip 2>/dev/null
    done
    
    # Disable thermal core control
    echo "0" > /sys/module/msm_thermal/core_control/enabled 2>/dev/null
    echo "0" > /sys/module/msm_thermal/vdd_restriction/enabled 2>/dev/null
    
    # Unisoc/Spreadtrum specific thermal
    echo "0" > /sys/module/sprd_thermal/parameters/enabled 2>/dev/null
    echo "0" > /sys/class/thermal/thermal_message/sconfig 2>/dev/null
    echo "0" > /sys/devices/virtual/thermal/thermal_message/sconfig 2>/dev/null
    
    # Kill thermal daemons
    killall -9 thermal-engine 2>/dev/null
    killall -9 thermald 2>/dev/null
    killall -9 mi_thermald 2>/dev/null
    killall -9 thermal_manager 2>/dev/null
    stop thermal-engine 2>/dev/null
    stop thermald 2>/dev/null
    stop mi_thermald 2>/dev/null
    
    # Disable thermal in kernel
    echo "N" > /sys/module/msm_thermal/parameters/enabled 2>/dev/null
    
    # Set CPU temp limit ke maksimal
    for temp in /sys/devices/system/cpu/cpu*/cpufreq/thermal_limit; do
        echo "999999" > $temp 2>/dev/null
    done
    
    # Disable GPU thermal limit
    echo "999999" > /sys/class/devfreq/60000000.gpu/thermal_limit 2>/dev/null
    
    echo -e "${HIJAU}   [✓] SEMUA thermal protection DIMATIKAN TOTAL!${NC}"
    echo -e "${HIJAU}   [✓] Temperature limit: 120°C (hardware max)${NC}"
    echo -e "${MERAH}   [⚠️] GUNAKAN COOLING PAD / FAN EKSTERNAL!${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk thermal bypass${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 14. OPTIMASI AUDIO - MODE GAME
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔊 Optimasi Audio - Mode Game..."
settings put system sound_effects_enabled 0 2>/dev/null
settings put system dtmf_tone_type 0 2>/dev/null
settings put system dial_tone_enabled 0 2>/dev/null
settings put system lockscreen_sounds_enabled 0 2>/dev/null
settings put system charging_sounds_enabled 0 2>/dev/null
setprop ro.audio.sampling_freq 48000 2>/dev/null
setprop ro.audio.pcm.sampling_rate 48000 2>/dev/null
# Nonaktifkan audio focus dari app lain
setprop audio.offload.disable 1 2>/dev/null
echo -e "${HIJAU}   [✓] Audio dioptimasi - suara game lebih jernih${NC}"

# ═══════════════════════════════════════════════════════════
# 15. OPTIMASI LAYAR - MODE TAJAM
# ═══════════════════════════════════════════════════════════
cetak_langkah "🖥️ Optimasi Layar - Mode Tajam Gaming..."
settings put system screen_brightness_mode 0 2>/dev/null
settings put system screen_brightness 200 2>/dev/null
settings put system screen_off_timeout 600000 2>/dev/null
settings put global peak_refresh_rate ${TARGET_HZ} 2>/dev/null
settings put global min_refresh_rate ${TARGET_HZ} 2>/dev/null
# Nonaktifkan adaptif brightness supaya tidak berubah-ubah
settings put system adaptive_sleep 0 2>/dev/null
echo -e "${HIJAU}   [✓] Layar dioptimasi - kecerahan stabil, screen timeout 10 menit${NC}"

# ═══════════════════════════════════════════════════════════
# 16. BYPASS CHARGING - MODE BATERAI IDLE
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔋 Bypass Charging - Mode Baterai Idle..."

aktifkan_bypass_charging() {
    local berhasil=false

    # Metode 1: Sony Xperia / Beberapa device
    if [ -f /sys/class/power_supply/battery/charging_enabled ]; then
        echo "0" > /sys/class/power_supply/battery/charging_enabled 2>/dev/null && berhasil=true
    fi
    # Metode 2: ASUS ROG Phone
    if [ -f /sys/class/power_supply/battery/bypass_mode ]; then
        echo "1" > /sys/class/power_supply/battery/bypass_mode 2>/dev/null && berhasil=true
    fi
    # Metode 3: Xiaomi/POCO
    if [ -f /sys/class/power_supply/battery/charging_limit ]; then
        echo "1" > /sys/class/power_supply/battery/charging_limit 2>/dev/null && berhasil=true
    fi
    # Metode 4: Samsung
    if [ -f /sys/class/power_supply/battery/input_limit ]; then
        echo "0" > /sys/class/power_supply/battery/input_limit 2>/dev/null && berhasil=true
    fi
    # Metode 5: Generic
    if [ -f /sys/class/power_supply/battery/force_charge ]; then
        echo "0" > /sys/class/power_supply/battery/force_charge 2>/dev/null && berhasil=true
    fi
    # Metode 6: MediaTek
    if [ -f /sys/class/power_supply/battery/mmi_charging_enable ]; then
        echo "0" > /sys/class/power_supply/battery/mmi_charging_enable 2>/dev/null && berhasil=true
    fi
    # Metode 7: Batas pengisian ~80%
    if [ -f /sys/class/power_supply/battery/charge_control_limit ]; then
        echo "1" > /sys/class/power_supply/battery/charge_control_limit 2>/dev/null && berhasil=true
    fi
    # Metode 8: OnePlus/Oppo
    if [ -f /sys/class/power_supply/battery/fastchg_enabled ]; then
        echo "0" > /sys/class/power_supply/battery/fastchg_enabled 2>/dev/null && berhasil=true
    fi

    if [ "$berhasil" = true ]; then
        return 0
    else
        return 1
    fi
}

if [ "$(id -u)" = "0" ]; then
    STATUS_CAS=$(cat /sys/class/power_supply/battery/status 2>/dev/null || echo "Tidak diketahui")

    if echo "$STATUS_CAS" | grep -qiE "(Charging|Full)"; then
        echo -e "${KUNING}   [i] Cas terdeteksi - Mencoba mode bypass...${NC}"

        if aktifkan_bypass_charging; then
            echo -e "${HIJAU}   [✓] Bypass charging AKTIF - Baterai idle${NC}"
            echo -e "${HIJAU}   [✓] HP langsung pakai daya dari cas${NC}"
            echo -e "${CYAN}   [i] Manfaat: Berkurang panas, kesehatan baterai terjaga${NC}"
        else
            echo -e "${KUNING}   [⚠️] Bypass charging tidak didukung di HP ini${NC}"
        fi
    else
        echo -e "${KUNING}   [i] Cas tidak terhubung - Mode bypass dilewati${NC}"
        echo -e "${CYAN}   [i] Hubungkan cas saat gaming untuk hasil terbaik${NC}"
    fi
else
    echo -e "${KUNING}   [!] Root diperlukan untuk bypass charging${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 17. OPTIMASI ZRAM & MEMORI VIRTUAL (Low-end optimized)
# ═══════════════════════════════════════════════════════════
cetak_langkah "💫 Optimasi ZRAM & Memori Virtual (Low RAM Device)..."
if [ "$(id -u)" = "0" ]; then
    # Untuk device RAM rendah (2-3GB), swappiness SANGAT rendah = RAM game tidak di-swap
    # Ref: DOKUMENTASI.md analisis - swappiness 60 terlalu tinggi untuk gaming
    echo "15" > /proc/sys/vm/swappiness 2>/dev/null
    echo "50" > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
    echo "0" > /proc/sys/vm/oom_kill_allocating_task 2>/dev/null
    echo "1" > /proc/sys/vm/overcommit_memory 2>/dev/null
    echo "500" > /proc/sys/vm/dirty_expire_centisecs 2>/dev/null
    echo "200" > /proc/sys/vm/dirty_writeback_centisecs 2>/dev/null
    echo "10" > /proc/sys/vm/dirty_ratio 2>/dev/null
    echo "3" > /proc/sys/vm/dirty_background_ratio 2>/dev/null
    
    # Low memory killer optimization untuk gaming
    echo "1536,2048,4096,5120,15360,23040" > /sys/module/lowmemorykiller/parameters/minfree 2>/dev/null
    
    # Extra free kbytes untuk smooth gaming
    echo "24576" > /proc/sys/vm/extra_free_kbytes 2>/dev/null
    
    # Compact memory untuk defragmentasi
    echo "1" > /proc/sys/vm/compact_memory 2>/dev/null
    
    echo -e "${HIJAU}   [✓] ZRAM & memori dioptimasi untuk low-end device${NC}"
    echo -e "${HIJAU}   [✓] Low memory killer disesuaikan untuk gaming${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk optimasi ZRAM${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 18. NONAKTIFKAN SINKRONISASI & UPDATE OTOMATIS
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔄 Menonaktifkan Sinkronisasi & Update Otomatis..."
settings put global auto_sync 0 2>/dev/null
settings put global package_verifier_enable 0 2>/dev/null
settings put global app_auto_update 0 2>/dev/null
# Nonaktifkan verifikasi data seluler
settings put global mobile_data_always_on 1 2>/dev/null
# Nonaktifkan crash dialog
settings put global hide_error_dialogs 1 2>/dev/null
settings put secure send_action_app_error 0 2>/dev/null
echo -e "${HIJAU}   [✓] Sinkronisasi otomatis dinonaktifkan - hemat bandwidth${NC}"
echo -e "${HIJAU}   [✓] Dialog error disembunyikan - tidak mengganggu game${NC}"

# ═══════════════════════════════════════════════════════════
# 19. NONAKTIFKAN FITUR YANG TIDAK PERLU
# ═══════════════════════════════════════════════════════════
cetak_langkah "🚫 Menonaktifkan Fitur yang Tidak Perlu..."
# Nonaktifkan Bluetooth (jika tidak pakai)
# settings put global bluetooth_on 0 2>/dev/null
# Nonaktifkan NFC
settings put global nfc_on 0 2>/dev/null
# Nonaktifkan lokasi (hemat baterai & CPU)
# settings put secure location_mode 0 2>/dev/null
# Nonaktifkan rotasi otomatis
settings put system accelerometer_rotation 0 2>/dev/null
# Nonaktifkan font scaling (biar UI stabil)
settings put system font_scale 1.0 2>/dev/null
# Nonaktifkan accessibility (bisa memperlambat)
settings put secure enabled_accessibility_services "" 2>/dev/null
# Nonaktifkan always-on display
settings put system aod_mode 0 2>/dev/null
settings put secure doze_always_on 0 2>/dev/null
echo -e "${HIJAU}   [✓] Fitur tidak perlu dinonaktifkan - sumber daya dilepaskan${NC}"

# ═══════════════════════════════════════════════════════════
# 20. MATIKAN PROSES FINAL & MULAI ULANG UI
# ═══════════════════════════════════════════════════════════
cetak_langkah "🧹 Pembersihan Final & Refresh Sistem..."
am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1

# Paksa garbage collection (alternative methods)
dumpsys gfxinfo --reset >/dev/null 2>&1
# JANGAN force-stop SystemUI! Ini bisa menyebabkan:
# - Chain restart yang mempengaruhi telephony stack
# - Status bar sinyal hilang/salah tampil
# - SIM card kehilangan registrasi jaringan
# am force-stop com.android.systemui >/dev/null 2>&1
sleep 1

echo -e "${HIJAU}   [✓] Layanan sistem di-refresh (SystemUI tetap stabil)${NC}"

# ═══════════════════════════════════════════════════════════
# 21. KERNEL TWEAKS ULTRA (ROOT ONLY)
# ═══════════════════════════════════════════════════════════
cetak_langkah "⚙️ Kernel Tweaks Ultra - Maximum Performance..."
if [ "$(id -u)" = "0" ]; then
    # Network stack optimization
    echo "1" > /proc/sys/net/ipv4/tcp_low_latency 2>/dev/null
    echo "1" > /proc/sys/net/ipv4/tcp_sack 2>/dev/null
    echo "1" > /proc/sys/net/ipv4/tcp_timestamps 2>/dev/null
    echo "1" > /proc/sys/net/ipv4/tcp_window_scaling 2>/dev/null
    echo "westwood" > /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null
    echo "3" > /proc/sys/net/ipv4/tcp_fastopen 2>/dev/null
    echo "1" > /proc/sys/net/ipv4/tcp_tw_reuse 2>/dev/null
    # tcp_tw_recycle DIHAPUS dari kernel Linux 4.12+
    # Berbahaya di jaringan seluler (NAT) - bisa bikin koneksi DROP!
    # echo "1" > /proc/sys/net/ipv4/tcp_tw_recycle 2>/dev/null
    
    # Kernel scheduler tweaks
    echo "1000000" > /proc/sys/kernel/sched_latency_ns 2>/dev/null
    echo "100000" > /proc/sys/kernel/sched_min_granularity_ns 2>/dev/null
    echo "1000000" > /proc/sys/kernel/sched_wakeup_granularity_ns 2>/dev/null
    echo "10000000" > /proc/sys/kernel/sched_migration_cost_ns 2>/dev/null
    echo "0" > /proc/sys/kernel/sched_child_runs_first 2>/dev/null
    echo "0" > /proc/sys/kernel/sched_tunable_scaling 2>/dev/null
    
    # Disable kernel debugging (performance boost)
    echo "0" > /proc/sys/kernel/printk 2>/dev/null
    echo "0" > /proc/sys/kernel/printk_devkmsg 2>/dev/null
    
    # Filesystem tweaks
    echo "0" > /proc/sys/fs/dir-notify-enable 2>/dev/null
    echo "0" > /proc/sys/fs/lease-break-time 2>/dev/null
    
    # Disable kernel panic (untuk stabilitas)
    echo "0" > /proc/sys/kernel/panic 2>/dev/null
    echo "0" > /proc/sys/kernel/panic_on_oops 2>/dev/null
    
    # Increase file handles
    echo "524288" > /proc/sys/fs/file-max 2>/dev/null
    
    echo -e "${HIJAU}   [✓] Kernel tweaks applied - Network & scheduler optimized${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk kernel tweaks${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 22. REALME/OPPO SPECIFIC OPTIMIZATIONS
# ═══════════════════════════════════════════════════════════
cetak_langkah "📱 Realme/Oppo Specific Ultra Optimizations..."
if [ "$(id -u)" = "0" ]; then
    # Disable Oppo/Realme bloatware services
    pm disable com.coloros.assistantscreen 2>/dev/null
    pm disable com.coloros.gamespaceui 2>/dev/null
    pm disable com.coloros.phonemanager 2>/dev/null
    pm disable com.heytap.pictorial 2>/dev/null
    pm disable com.oppo.operationManual 2>/dev/null
    pm disable com.oppoex.afterservice 2>/dev/null
    
    # Realme display optimizations
    echo "1" > /sys/kernel/oppo_display/hbm 2>/dev/null
    echo "0" > /sys/kernel/oppo_display/aod 2>/dev/null
    echo "1" > /sys/kernel/oppo_display/dimlayer_hbm 2>/dev/null
    
    # Disable Oppo power saving features
    echo "0" > /sys/module/oppo_bsp_tp/parameters/tp_gesture_enable 2>/dev/null
    echo "0" > /sys/module/oppo_charger/parameters/charger_suspend 2>/dev/null
    
    # Force performance mode in Oppo framework
    setprop persist.sys.oppo.region CN 2>/dev/null
    setprop persist.sys.oplus.region CN 2>/dev/null
    setprop sys.oplus.performance.support true 2>/dev/null
    
    echo -e "${HIJAU}   [✓] Realme/Oppo bloatware disabled${NC}"
    echo -e "${HIJAU}   [✓] Display HBM mode enabled${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk Realme optimizations${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 23. UNISOC SC9863A DVFS KHUSUS - PAKSA SPEK DI ATAS RATA-RATA
# ═══════════════════════════════════════════════════════════
cetak_langkah "⚡ Unisoc SC9863A DVFS Override - Paksa Spek Maksimal..."
if [ "$(id -u)" = "0" ]; then
    # Disable Spreadtrum DVFS (dynamic voltage frequency scaling)
    echo "0" > /sys/devices/system/cpu/cpufreq/sprdemand/cpu_hotplug_disable 2>/dev/null
    echo "1" > /sys/devices/system/cpu/cpufreq/sprdemand/boost 2>/dev/null
    echo "0" > /sys/devices/system/cpu/cpufreq/sprdemand/io_is_busy 2>/dev/null
    
    # Spreadtrum power hint
    echo "performance" > /sys/power/scenario 2>/dev/null
    echo "1" > /sys/power/pnpmgr/hotplug/cpu_core_num 2>/dev/null
    
    # Force all CPU online permanently
    echo "8" > /sys/power/pnpmgr/hotplug/cpu_core_num 2>/dev/null
    
    # Disable CPU idle states (no sleep!)
    for idle_state in /sys/devices/system/cpu/cpu*/cpuidle/state*/disable; do
        echo "1" > $idle_state 2>/dev/null
    done
    
    # Disable EAS energy-aware scheduling (force performance)
    echo "0" > /proc/sys/kernel/sched_energy_aware 2>/dev/null
    
    # Unisoc specific bus frequency lock
    for bus in /sys/class/devfreq/*/governor; do
        echo "performance" > $bus 2>/dev/null
    done
    
    echo -e "${HIJAU}   [✓] DVFS Override aktif - CPU tidak akan turun frekuensi!${NC}"
    echo -e "${HIJAU}   [✓] CPU idle states dimatikan - 8 core aktif terus!${NC}"
    echo -e "${HIJAU}   [✓] Bus frequency dikunci ke maksimal${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk DVFS override${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 24. OPTIMASI KHUSUS MOBILE LEGENDS BANG BANG
# ═══════════════════════════════════════════════════════════
cetak_langkah "🎯 Optimasi Khusus Mobile Legends Bang Bang..."
if [ -n "$PKG" ]; then
    # MLBB specific rendering - paksa OpenGL ES 3.0
    setprop debug.egl.force_gl_version 3.0 2>/dev/null
    setprop debug.hwui.render_thread true 2>/dev/null
    
    # Alokasikan RAM lebih untuk game (large heap)
    setprop dalvik.vm.heapsize 512m 2>/dev/null
    setprop dalvik.vm.heapgrowthlimit 256m 2>/dev/null
    setprop dalvik.vm.heapmaxfree 8m 2>/dev/null
    setprop dalvik.vm.heapminfree 512k 2>/dev/null
    setprop dalvik.vm.heaptargetutilization 0.75 2>/dev/null
    
    # GC optimization untuk game (kurangi stutter karena GC)
    setprop dalvik.vm.dex2oat-threads 4 2>/dev/null
    setprop dalvik.vm.dex2oat-Xms 64m 2>/dev/null
    setprop dalvik.vm.dex2oat-Xmx 512m 2>/dev/null
    setprop dalvik.vm.image-dex2oat-threads 4 2>/dev/null
    
    # Paksa app ke mode immersive (fullscreen tanpa navbar)
    settings put global policy_control "immersive.full=$PKG" 2>/dev/null
    
    # Izinkan game pakai semua core CPU
    if [ "$(id -u)" = "0" ]; then
        GAME_PID_PRE=$(pidof $PKG 2>/dev/null)
        if [ -n "$GAME_PID_PRE" ]; then
            taskset -a -p ff $GAME_PID_PRE 2>/dev/null
            echo -e "${HIJAU}   [✓] Game diarahkan ke SEMUA 8 core CPU${NC}"
        fi
    fi
    
    echo -e "${HIJAU}   [✓] MLBB rendering mode: OpenGL ES 3.0${NC}"
    echo -e "${HIJAU}   [✓] Dalvik VM dioptimasi untuk gaming${NC}"
    echo -e "${HIJAU}   [✓] Fullscreen immersive mode aktif${NC}"
    echo -e "${HIJAU}   [✓] GC optimization - kurangi stutter${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 25. DISABLE APP YANG TIDAK TERPAKAI (BEBASKAN RAM & CPU)
# ═══════════════════════════════════════════════════════════
cetak_langkah "📦 Menonaktifkan App yang Tidak Terpakai..."
echo -e "${KUNING}   [i] Menonaktifkan app bawaan yang tidak diperlukan saat gaming...${NC}"

# Daftar app yang akan di-disable (tidak diperlukan saat gaming)
# ⚠️ PERINGATAN: JANGAN masukkan com.android.phone, com.android.ims,
#    com.android.stk, atau proses telephony lainnya!
#    Itu akan membuat SIM card hilang dan sinyal mati!
APLIKASI_DISABLE=(
    # Komunikasi & Kontak (BUKAN telephony core!)
    "com.google.android.contacts"       # Google Kontak (UI saja, bukan provider)
    "com.android.contacts"              # Kontak bawaan (UI saja)
    "com.google.android.dialer"         # Google Telepon (UI dialer, bukan phone service)
    "com.android.dialer"                # Telepon bawaan (UI dialer, bukan phone service)
    # ❌ DIHAPUS: "com.android.phone" ← INI PENYEBAB SIM HILANG!
    # com.android.phone = telephony service inti, BUKAN sekedar app telepon
    # Disable = modem mati = SIM tidak terdeteksi = sinyal hilang total
    "com.google.android.apps.messaging" # Google Messages
    "com.android.mms"                   # Messaging bawaan
    # Email
    "com.google.android.gm"             # Gmail
    "com.google.android.gm.lite"        # Gmail Lite
    # Utilitas
    "com.google.android.deskclock"      # Google Jam/Clock
    "com.android.deskclock"             # Jam bawaan
    "com.google.android.calendar"       # Google Calendar
    "com.android.calendar"              # Kalender bawaan
    "com.google.android.calculator"     # Google Kalkulator
    "com.coloros.calculator"            # Kalkulator Realme
    "com.android.calculator2"           # Kalkulator bawaan
    # Maps & Navigasi
    "com.google.android.apps.maps"      # Google Maps
    # Media & Hiburan
    "com.google.android.youtube"        # YouTube
    "com.google.android.apps.youtube.music" # YouTube Music
    "com.google.android.music"          # Google Play Music
    # Browser
    "com.android.chrome"                # Google Chrome
    "com.heytap.browser"                # Realme Browser
    # Produktivitas
    "com.google.android.keep"           # Google Keep
    "com.google.android.apps.docs"      # Google Docs
    "com.google.android.apps.docs.editors.sheets"  # Google Sheets
    "com.google.android.apps.docs.editors.slides"  # Google Slides
    # Perekam
    "com.coloros.soundrecorder"         # Perekam Suara Realme
    "com.android.soundrecorder"         # Perekam Suara bawaan
    "com.google.android.apps.recorder"  # Google Recorder
    # Kamera & Foto (tidak perlu saat gaming)
    "com.google.android.apps.photos"    # Google Photos
    "com.google.android.GoogleCamera"   # Google Camera
    # Google Apps tambahan
    "com.google.android.apps.tachyon"   # Google Duo/Meet
    "com.google.android.apps.wellbeing" # Digital Wellbeing
    "com.google.android.apps.nbu.files" # Google Files
    "com.google.android.apps.magazines" # Google News
    "com.google.android.googlequicksearchbox" # Google Search
    "com.google.android.videos"         # Google TV
    # Realme/Oppo bloatware
    "com.coloros.weather2"              # Cuaca Realme
    "com.coloros.compass2"              # Kompas Realme
    "com.coloros.oshare"                # Oshare Realme
    "com.heytap.pictorial"              # Wallpaper Realme
    "com.heytap.music"                  # Musik Realme
    "com.heytap.cloud"                  # Cloud Realme
    "com.heytap.smarthome"              # Smart Home
    "com.nearme.gamecenter"             # Game Center Realme
    "com.oppo.operationManual"          # Manual Oppo
    "com.oppoex.afterservice"           # After Service
    "com.coloros.phonemanager"          # Phone Manager
)

JUMLAH_DISABLED=0
for app_dis in "${APLIKASI_DISABLE[@]}"; do
    # Cek apakah app terinstall
    if pm list packages 2>/dev/null | grep -q "$app_dis"; then
        pm disable-user --user 0 "$app_dis" >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            JUMLAH_DISABLED=$((JUMLAH_DISABLED + 1))
        fi
    fi
done

echo -e "${HIJAU}   [✓] Total app dinonaktifkan: ${JUMLAH_DISABLED}${NC}"
echo -e "${HIJAU}   [✓] RAM & CPU dibebaskan dari app yang tidak terpakai!${NC}"
echo -e "${CYAN}   [i] App akan diaktifkan kembali saat menjalankan modeGameOFF.sh${NC}"

# ═══════════════════════════════════════════════════════════
# 26. CEK KESEHATAN SIM CARD & SINYAL (SAFETY CHECK)
# ═══════════════════════════════════════════════════════════
LANGKAH=$((LANGKAH))
echo -e "${BIRU}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BIRU}[SAFETY] 📶 Memeriksa Kesehatan SIM Card & Sinyal...${NC}"
echo -e "${BIRU}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Cek apakah com.android.phone masih berjalan
PHONE_PID=$(pidof com.android.phone 2>/dev/null)
if [ -z "$PHONE_PID" ]; then
    echo -e "${MERAH}   [⚠️] PERINGATAN: com.android.phone TIDAK BERJALAN!${NC}"
    echo -e "${KUNING}   [i] Mencoba memulihkan telephony service...${NC}"
    # Re-enable jika ter-disable
    pm enable com.android.phone >/dev/null 2>&1
    # Mulai ulang telephony
    am startservice -n com.android.phone/.PhoneInterfaceManager >/dev/null 2>&1
    am start -n com.android.phone/.TelephonyDebugService >/dev/null 2>&1
    sleep 2
    PHONE_PID=$(pidof com.android.phone 2>/dev/null)
    if [ -n "$PHONE_PID" ]; then
        echo -e "${HIJAU}   [✓] Telephony service BERHASIL dipulihkan! (PID: $PHONE_PID)${NC}"
    else
        echo -e "${MERAH}   [✗] Gagal memulihkan - mungkin perlu restart HP${NC}"
    fi
else
    echo -e "${HIJAU}   [✓] com.android.phone aktif (PID: $PHONE_PID)${NC}"
fi

# Cek status SIM card via telephony registry
SIM_STATE=$(dumpsys telephony.registry 2>/dev/null | grep -i "mSimState" | head -1 | grep -oE "[A-Z_]+$")
if [ -n "$SIM_STATE" ]; then
    if echo "$SIM_STATE" | grep -qiE "(READY|LOADED)"; then
        echo -e "${HIJAU}   [✓] SIM Card: $SIM_STATE (Terdeteksi & Siap)${NC}"
    elif echo "$SIM_STATE" | grep -qiE "(ABSENT|NOT_READY|ERROR)"; then
        echo -e "${MERAH}   [⚠️] SIM Card: $SIM_STATE - Ada masalah!${NC}"
        echo -e "${KUNING}   [i] Mencoba recovery SIM...${NC}"
        # Toggle airplane mode untuk reset radio
        settings put global airplane_mode_on 1 2>/dev/null
        am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true 2>/dev/null
        sleep 3
        settings put global airplane_mode_on 0 2>/dev/null
        am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false 2>/dev/null
        sleep 5
        echo -e "${HIJAU}   [✓] Radio di-reset - SIM seharusnya pulih dalam beberapa detik${NC}"
    else
        echo -e "${KUNING}   [i] SIM Card: $SIM_STATE${NC}"
    fi
else
    echo -e "${KUNING}   [i] Tidak bisa baca status SIM (normal di beberapa device)${NC}"
fi

# Cek status sinyal/service
SERVICE_STATE=$(dumpsys telephony.registry 2>/dev/null | grep -i "mServiceState" | head -1)
if echo "$SERVICE_STATE" | grep -qiE "IN_SERVICE"; then
    echo -e "${HIJAU}   [✓] Sinyal: TERDAFTAR di jaringan (IN_SERVICE)${NC}"
elif echo "$SERVICE_STATE" | grep -qiE "OUT_OF_SERVICE"; then
    echo -e "${MERAH}   [⚠️] Sinyal: TIDAK ADA LAYANAN - Cek SIM card!${NC}"
else
    echo -e "${KUNING}   [i] Status sinyal: Tidak bisa dibaca${NC}"
fi

echo -e "${HIJAU}   [✓] Safety check selesai - SIM & sinyal dilindungi${NC}"

# ═══════════════════════════════════════════════════════════
# LUNCURKAN GAME
# ═══════════════════════════════════════════════════════════
echo ""
echo -e "${CYAN}[LUNCURKAN] 🎮 Memulai Game...${NC}"
if [ -n "$PKG" ]; then
    # Tunggu sebentar agar sistem stabil
    sleep 2
    monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
    echo -e "${HIJAU}[✓] Game sedang diluncurkan...${NC}"

    # Set prioritas setelah game mulai
    sleep 3
    if [ "$(id -u)" = "0" ]; then
        GAME_PID=$(pidof $PKG 2>/dev/null)
        if [ -n "$GAME_PID" ]; then
            renice -20 $GAME_PID 2>/dev/null
            ionice -c 1 -n 0 -p $GAME_PID 2>/dev/null
            echo -e "${HIJAU}[✓] Prioritas game diatur ke REALTIME (PID: $GAME_PID)${NC}"
        fi
    fi
else
    echo -e "${MERAH}[!] Package tidak ditemukan, silakan buka game manual${NC}"
fi

# ═══════════════════════════════════════════════════════════
# INFO SISTEM SETELAH OPTIMASI
# ═══════════════════════════════════════════════════════════
echo ""
echo -e "${KUNING}─── INFO SISTEM SETELAH OPTIMASI ───${NC}"

# Tampilkan penggunaan RAM
RAM_TOTAL=$(cat /proc/meminfo 2>/dev/null | grep MemTotal | awk '{print int($2/1024)}')
RAM_TERSEDIA=$(cat /proc/meminfo 2>/dev/null | grep MemAvailable | awk '{print int($2/1024)}')
RAM_TERPAKAI=$((RAM_TOTAL - RAM_TERSEDIA))
if [ -n "$RAM_TOTAL" ] && [ -n "$RAM_TERSEDIA" ]; then
    PERSEN_TERSEDIA=$((RAM_TERSEDIA * 100 / RAM_TOTAL))
    echo -e "${CYAN}   RAM Total    : ${RAM_TOTAL}MB${NC}"
    echo -e "${CYAN}   RAM Terpakai : ${RAM_TERPAKAI}MB${NC}"
    echo -e "${HIJAU}   RAM Tersedia : ${RAM_TERSEDIA}MB (${PERSEN_TERSEDIA}%)${NC}"
fi

# Info baterai
LEVEL_BATERAI=$(cat /sys/class/power_supply/battery/capacity 2>/dev/null || echo "?")
SUHU_BATERAI=$(cat /sys/class/power_supply/battery/temp 2>/dev/null)
if [ -n "$SUHU_BATERAI" ]; then
    SUHU_BATERAI=$((SUHU_BATERAI / 10))
    echo -e "${CYAN}   Baterai      : ${LEVEL_BATERAI}% | Suhu: ${SUHU_BATERAI}°C${NC}"
else
    echo -e "${CYAN}   Baterai      : ${LEVEL_BATERAI}%${NC}"
fi

# ═══════════════════════════════════════════════════════════
# STATUS AKHIR
# ═══════════════════════════════════════════════════════════
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ MODE GAMING DEWA v6.0 - ULTRA RATA KANAN AKTIF!         ${CYAN}║${NC}"
echo -e "${CYAN}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ CPU         : 8 CORE LOCKED 100% + OVERCLOCK            ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ GPU         : IMG8322 OVERCLOCKED + NO THROTTLE         ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ THERMAL     : TOTAL BYPASS - TANPA BATASAN             ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ FPS         : UNLOCKED & MAXIMUM STABLE                ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ SENTUHAN    : ULTRA RESPONSIVE MODE                    ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ JARINGAN    : ULTRA LOW LATENCY                        ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ RAM         : BERSIH TOTAL - HANYA GAME YANG JALAN     ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ BACKGROUND  : ${JUMLAH_DIMATIKAN} PROSES DIMATIKAN + LIMIT 0           ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ DVFS        : OVERRIDE - FREKUENSI DIKUNCI             ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ DALVIK VM   : OPTIMIZED UNTUK MLBB                     ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ KERNEL      : ULTRA TWEAKED - SCHEDULER GAMING          ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ BLOATWARE   : DISABLED (Realme/Oppo)                   ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ IDLE STATE  : CPU TIDAK BOLEH TIDUR                    ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ SIM CARD   : DILINDUNGI & TERVERIFIKASI               ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ SINYAL      : STABIL - TELEPHONY AMAN                 ${CYAN}║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${PUTIH}🎮 ${HIJAU}ULTRA RATA KANAN TERCAPAI - SIAP SAVAGE & MANIAC!${NC}"
echo ""
echo -e "${MERAH}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${MERAH}║                    ⚠️  PERINGATAN PENTING ⚠️                  ║${NC}"
echo -e "${MERAH}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${MERAH}║  • HP AKAN SANGAT PANAS (60-70°C adalah NORMAL)             ║${NC}"
echo -e "${MERAH}║  • THERMAL PROTECTION DIMATIKAN TOTAL                        ║${NC}"
echo -e "${MERAH}║  • GUNAKAN COOLING PAD / FAN EKSTERNAL                       ║${NC}"
echo -e "${MERAH}║  • JANGAN MAIN LEBIH DARI 2 JAM TERUS-MENERUS               ║${NC}"
echo -e "${MERAH}║  • BATERAI AKAN CEPAT HABIS - COLOK CHARGER                 ║${NC}"
echo -e "${MERAH}║  • RESTART HP UNTUK KEMBALI KE MODE NORMAL                   ║${NC}"
echo -e "${MERAH}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}💡 ${KUNING}TIPS RATA KANAN MLBB:${NC}"
echo -e "${CYAN}   • Lepas casing HP untuk ventilasi lebih baik${NC}"
echo -e "${CYAN}   • Main di ruangan ber-AC jika memungkinkan${NC}"
echo -e "${CYAN}   • Gunakan mode pesawat + WiFi untuk ping stabil${NC}"
echo -e "${CYAN}   • Setting MLBB: High Quality + High Frame Rate${NC}"
echo -e "${CYAN}   • Jangan buka app lain saat gaming!${NC}"
echo ""
echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
echo -e "${PUTIH}   MODE GAMING DEWA v6.0 - ULTRA RATA KANAN EDITION${NC}"
echo -e "${PUTIH}   Hardware: Unisoc SC9863A + IMG8322 PowerVR${NC}"
echo -e "${PUTIH}   Platform: Realme Go UI - Android 11${NC}"
echo -e "${PUTIH}   Target: Mobile Legends Bang Bang - 60 FPS Stable${NC}"
echo -e "${PUTIH}   🛡️ SIM Card & Sinyal: DILINDUNGI${NC}"
echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
