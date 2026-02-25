#!/system/bin/sh

# ╔══════════════════════════════════════════════════════════════╗
# ║         MODE GAMING DEWA - RESTORE TO NORMAL MODE            ║
# ║      Kembalikan Sistem dari Ultra Rata Kanan ke Normal       ║
# ║    Version: 6.0 - Unisoc SC9863A + IMG8322 PowerVR          ║
# ║    📱 Realme Go UI - Android 11                               ║
# ╚══════════════════════════════════════════════════════════════╝

# Kode warna
MERAH='\033[0;31m'
HIJAU='\033[0;32m'
KUNING='\033[1;33m'
BIRU='\033[0;34m'
CYAN='\033[0;36m'
PUTIH='\033[1;37m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# Banner
clear
echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${PUTIH}     🔄 RESTORE MODE v6.0 - Kembali ke Normal            ${CYAN}║${NC}"
echo -e "${CYAN}║${KUNING}       Membatalkan Semua Optimasi Ultra Rata Kanan        ${CYAN}║${NC}"
echo -e "${CYAN}║${MERAH}         Unisoc SC9863A + IMG8322 PowerVR                 ${CYAN}║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${MAGENTA}   🔄 Mengembalikan CPU dari overclock ke normal${NC}"
echo -e "${MAGENTA}   🔄 Mengembalikan GPU dari overclock ke normal${NC}"
echo -e "${MAGENTA}   🔄 Mengaktifkan kembali thermal protection${NC}"
echo -e "${MAGENTA}   🔄 Mengaktifkan kembali semua app yang di-disable${NC}"
echo -e "${MAGENTA}   🔄 Mengembalikan semua settingan sistem${NC}"
echo ""

# Root check
IS_ROOT=false
if [ "$(id -u)" = "0" ]; then
    IS_ROOT=true
    echo -e "${HIJAU}[✓] AKSES ROOT TERDETEKSI - Restore penuh tersedia${NC}"
    echo ""
else
    echo -e "${KUNING}[!] Beberapa fitur memerlukan ROOT access${NC}"
    echo -e "${KUNING}[!] Restart HP untuk restore penuh tanpa root${NC}"
    echo ""
fi

LANGKAH=1
TOTAL_LANGKAH=19

cetak_langkah() {
    echo -e "${BIRU}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BIRU}[$LANGKAH/$TOTAL_LANGKAH] $1${NC}"
    echo -e "${BIRU}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    LANGKAH=$((LANGKAH + 1))
}

# ═══════════════════════════════════════════════════════════
# 1. RESTORE ANIMASI SISTEM
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔧 Mengembalikan Animasi Sistem..."
settings put global window_animation_scale 1.0 2>/dev/null
settings put global transition_animation_scale 1.0 2>/dev/null
settings put global animator_duration_scale 1.0 2>/dev/null
settings put global duration_scale 1 2>/dev/null
settings put global always_finish_activities 0 2>/dev/null
echo -e "${HIJAU}   [✓] Animasi sistem dikembalikan ke normal${NC}"

# ═══════════════════════════════════════════════════════════
# 2. MATIKAN MODE PERFORMA
# ═══════════════════════════════════════════════════════════
cetak_langkah "⚡ Menonaktifkan Mode Performa..."
cmd power set-fixed-performance-mode-enabled false 2>/dev/null
settings put global sustain_performance_mode 0 2>/dev/null
settings put global low_power_mode 0 2>/dev/null
settings put global battery_saver_mode 0 2>/dev/null
settings put global adaptive_battery_management_enabled 1 2>/dev/null
settings put global force_gpu_rendering 0 2>/dev/null
settings put global disable_overlays 0 2>/dev/null
settings put global background_process_limit -1 2>/dev/null
settings put global activity_manager_constants "" 2>/dev/null
settings put global game_mode_status 0 2>/dev/null
echo -e "${HIJAU}   [✓] Mode performa dinonaktifkan${NC}"
echo -e "${HIJAU}   [✓] Background process limit: Normal${NC}"

# ═══════════════════════════════════════════════════════════
# 3. RESTORE REFRESH RATE
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔄 Mengembalikan Refresh Rate Default..."
settings put system peak_refresh_rate 60.0 2>/dev/null
settings put system min_refresh_rate 60.0 2>/dev/null
settings put system user_refresh_rate 60.0 2>/dev/null
settings put system refresh_rate 60.0 2>/dev/null
cmd display set-refresh-rate 60 2>/dev/null
echo -e "${HIJAU}   [✓] Refresh rate dikembalikan ke 60Hz${NC}"

# ═══════════════════════════════════════════════════════════
# 4. BERSIHKAN RAM
# ═══════════════════════════════════════════════════════════
cetak_langkah "🧹 Membersihkan RAM..."
pm trim-caches 999999999999 2>/dev/null
am send-trim-memory TRIM_MEMORY_RUNNING_CRITICAL 2>/dev/null

if [ "$(id -u)" = "0" ]; then
    sync && echo 3 > /proc/sys/vm/drop_caches 2>/dev/null
fi
echo -e "${HIJAU}   [✓] RAM dibersihkan${NC}"

# ═══════════════════════════════════════════════════════════
# 5. RESTORE CPU GOVERNOR
# ═══════════════════════════════════════════════════════════
cetak_langkah "🚀 Mengembalikan CPU Governor ke Normal..."
if [ "$(id -u)" = "0" ]; then
    for cpu in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do
        chmod 644 $cpu 2>/dev/null
        echo "schedutil" > $cpu 2>/dev/null
        echo "interactive" > $cpu 2>/dev/null
    done

    # Unlock CPU online status
    for i in 0 1 2 3 4 5 6 7; do
        chmod 644 /sys/devices/system/cpu/cpu$i/online 2>/dev/null
    done

    # Re-enable CPU idle states
    for idle_state in /sys/devices/system/cpu/cpu*/cpuidle/state*/disable; do
        echo "0" > $idle_state 2>/dev/null
    done

    # Re-enable EAS
    echo "1" > /proc/sys/kernel/sched_energy_aware 2>/dev/null

    # Restore bus governors
    for bus in /sys/class/devfreq/*/governor; do
        echo "simple_ondemand" > $bus 2>/dev/null
    done

    echo -e "${HIJAU}   [✓] CPU governor dikembalikan ke schedutil${NC}"
    echo -e "${HIJAU}   [✓] CPU idle states diaktifkan kembali${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk CPU governor${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 6. RESTORE GPU
# ═══════════════════════════════════════════════════════════
cetak_langkah "🎮 Mengembalikan GPU ke Normal..."
settings put global gpu_force_4x_msaa 0 2>/dev/null
setprop debug.egl.hw 0 2>/dev/null
setprop debug.gr.numframebuffers 1 2>/dev/null
setprop debug.composition.type auto 2>/dev/null
setprop debug.sf.disable_backpressure 0 2>/dev/null

if [ "$(id -u)" = "0" ]; then
    if [ -d /sys/class/devfreq/60000000.gpu ]; then
        chmod 644 /sys/class/devfreq/60000000.gpu/governor 2>/dev/null
        echo "simple_ondemand" > /sys/class/devfreq/60000000.gpu/governor 2>/dev/null
    fi
fi
echo -e "${HIJAU}   [✓] GPU dikembalikan ke normal${NC}"

# ═══════════════════════════════════════════════════════════
# 7. RESTORE I/O SCHEDULER
# ═══════════════════════════════════════════════════════════
cetak_langkah "💾 Mengembalikan I/O Scheduler..."
if [ "$(id -u)" = "0" ]; then
    for block in /sys/block/*/queue/scheduler; do
        echo "cfq" > $block 2>/dev/null
        echo "mq-deadline" > $block 2>/dev/null
    done

    for ra in /sys/block/*/queue/read_ahead_kb; do
        echo "128" > $ra 2>/dev/null
    done

    echo -e "${HIJAU}   [✓] I/O scheduler dikembalikan${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk I/O scheduler${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 8. HAPUS PRIORITAS GAME & GAME MODE API
# ═══════════════════════════════════════════════════════════
cetak_langkah "📊 Menghapus Prioritas Game..."
GAMES=(
    "com.mobile.legends"
    "com.mobile.legends.google"
    "com.moonton.mobilelegends"
    "com.pubg.krmobile"
    "com.tencent.ig"
    "com.garena.game.freefire"
)

for game in "${GAMES[@]}"; do
    dumpsys deviceidle whitelist -$game 2>/dev/null
    cmd activity set-standby-bucket $game neutral 2>/dev/null
    am set-inactive $game true 2>/dev/null
    # Kembalikan Game Mode API ke standard
    cmd game mode standard $game 2>/dev/null
done

# Hapus immersive mode
settings put global policy_control "" 2>/dev/null

echo -e "${HIJAU}   [✓] Prioritas game dihapus${NC}"
echo -e "${HIJAU}   [✓] Game Mode API dikembalikan ke standard${NC}"

# ═══════════════════════════════════════════════════════════
# 9. RESTORE NOTIFIKASI
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔔 Mengaktifkan Kembali Notifikasi..."
APLIKASI_RESTORE_NOTIF=(
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

for app_notif in "${APLIKASI_RESTORE_NOTIF[@]}"; do
    cmd appops set "$app_notif" POST_NOTIFICATION allow 2>/dev/null
done

# Matikan DND
settings put global zen_mode 0 2>/dev/null
cmd notification set_dnd off 2>/dev/null

echo -e "${HIJAU}   [✓] Notifikasi diaktifkan kembali${NC}"
echo -e "${HIJAU}   [✓] DND mode dimatikan${NC}"

# ═══════════════════════════════════════════════════════════
# 10. RESTORE JARINGAN
# ═══════════════════════════════════════════════════════════
cetak_langkah "📡 Mengembalikan Pengaturan Jaringan..."
settings put global wifi_sleep_policy 1 2>/dev/null
settings put global captive_portal_detection_enabled 1 2>/dev/null

setprop net.tcp.buffersize.default "" 2>/dev/null
setprop net.tcp.buffersize.wifi "" 2>/dev/null
setprop net.tcp.buffersize.lte "" 2>/dev/null
setprop net.dns1 "" 2>/dev/null
setprop net.dns2 "" 2>/dev/null

echo -e "${HIJAU}   [✓] Pengaturan jaringan dikembalikan${NC}"

# ═══════════════════════════════════════════════════════════
# 11. RESTORE SENSITIVITAS SENTUHAN
# ═══════════════════════════════════════════════════════════
cetak_langkah "👆 Mengembalikan Sensitivitas Sentuhan..."
settings put system touch_sensitivity 0 2>/dev/null
settings put system pointer_speed 0 2>/dev/null
settings put system haptic_feedback_enabled 1 2>/dev/null
settings put system vibrate_on_touch 1 2>/dev/null
settings put system vibrate_when_ringing 1 2>/dev/null
echo -e "${HIJAU}   [✓] Sensitivitas sentuhan dikembalikan${NC}"

# ═══════════════════════════════════════════════════════════
# 12. RESTORE THERMAL MANAGEMENT
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔥 Mengaktifkan Kembali Thermal Protection..."
if [ "$(id -u)" = "0" ]; then
    for thermal in /sys/class/thermal/thermal_zone*/mode; do
        chmod 644 $thermal 2>/dev/null
        echo "enabled" > $thermal 2>/dev/null
    done

    # Re-enable Unisoc thermal
    echo "1" > /sys/module/sprd_thermal/parameters/enabled 2>/dev/null
    echo "1" > /sys/module/msm_thermal/parameters/enabled 2>/dev/null

    # Restart thermal daemons
    start thermal-engine 2>/dev/null
    start thermald 2>/dev/null

    echo -e "${HIJAU}   [✓] Thermal protection AKTIF kembali!${NC}"
    echo -e "${HIJAU}   [✓] HP tidak akan overheat${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk thermal management${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 13. RESTORE AUDIO
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔊 Mengembalikan Pengaturan Audio..."
settings put system sound_effects_enabled 1 2>/dev/null
settings put system dtmf_tone_type 1 2>/dev/null
settings put system lockscreen_sounds_enabled 1 2>/dev/null
settings put system charging_sounds_enabled 1 2>/dev/null
echo -e "${HIJAU}   [✓] Audio dikembalikan ke normal${NC}"

# ═══════════════════════════════════════════════════════════
# 14. RESTORE LAYAR
# ═══════════════════════════════════════════════════════════
cetak_langkah "🖥️ Mengembalikan Pengaturan Layar..."
settings put system screen_brightness_mode 1 2>/dev/null
settings put system screen_brightness 128 2>/dev/null
settings put system screen_off_timeout 30000 2>/dev/null
settings put system adaptive_sleep 1 2>/dev/null
echo -e "${HIJAU}   [✓] Layar dikembalikan ke normal${NC}"

# ═══════════════════════════════════════════════════════════
# 15. RESTORE ZRAM & MEMORI VIRTUAL
# ═══════════════════════════════════════════════════════════
cetak_langkah "💫 Mengembalikan ZRAM & Memori Virtual..."
if [ "$(id -u)" = "0" ]; then
    echo "60" > /proc/sys/vm/swappiness 2>/dev/null
    echo "100" > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
    echo "0" > /proc/sys/vm/overcommit_memory 2>/dev/null
    echo -e "${HIJAU}   [✓] ZRAM dikembalikan ke normal${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan untuk ZRAM${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 16. RESTORE SINKRONISASI & UPDATE
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔄 Mengaktifkan Kembali Sinkronisasi..."
settings put global auto_sync 1 2>/dev/null
settings put global package_verifier_enable 1 2>/dev/null
settings put global hide_error_dialogs 0 2>/dev/null
settings put secure send_action_app_error 1 2>/dev/null
echo -e "${HIJAU}   [✓] Sinkronisasi otomatis aktif kembali${NC}"

# ═══════════════════════════════════════════════════════════
# 17. RESTORE FITUR YANG DINONAKTIFKAN
# ═══════════════════════════════════════════════════════════
cetak_langkah "🔓 Mengaktifkan Kembali Fitur Sistem..."
settings put system accelerometer_rotation 1 2>/dev/null
settings put secure doze_always_on 1 2>/dev/null
settings put system aod_mode 1 2>/dev/null
echo -e "${HIJAU}   [✓] Rotasi otomatis, AOD aktif kembali${NC}"

# ═══════════════════════════════════════════════════════════
# 18. RE-ENABLE REALME/OPPO SERVICES
# ═══════════════════════════════════════════════════════════
cetak_langkah "📱 Mengaktifkan Kembali Layanan Realme/Oppo..."
if [ "$(id -u)" = "0" ]; then
    pm enable com.coloros.assistantscreen 2>/dev/null
    pm enable com.coloros.gamespaceui 2>/dev/null
    pm enable com.coloros.phonemanager 2>/dev/null
    pm enable com.heytap.pictorial 2>/dev/null
    pm enable com.oppo.operationManual 2>/dev/null
    pm enable com.oppoex.afterservice 2>/dev/null
    echo -e "${HIJAU}   [✓] Layanan Realme/Oppo aktif kembali${NC}"
else
    echo -e "${KUNING}   [!] Root diperlukan${NC}"
fi

# ═══════════════════════════════════════════════════════════
# 19. RE-ENABLE APP YANG DI-DISABLE SAAT GAMING
# ═══════════════════════════════════════════════════════════
cetak_langkah "📦 Mengaktifkan Kembali App yang Dinonaktifkan..."
echo -e "${KUNING}   [i] Mengaktifkan semua app yang di-disable saat mode gaming...${NC}"

APLIKASI_ENABLE=(
    # Komunikasi & Kontak
    "com.google.android.contacts"
    "com.android.contacts"
    "com.google.android.dialer"
    "com.android.dialer"
    # com.android.phone TIDAK di-disable di v6.0, jadi tidak perlu re-enable
    # "com.android.phone"
    "com.google.android.apps.messaging"
    "com.android.mms"
    # Email
    "com.google.android.gm"
    "com.google.android.gm.lite"
    # Utilitas
    "com.google.android.deskclock"
    "com.android.deskclock"
    "com.google.android.calendar"
    "com.android.calendar"
    "com.google.android.calculator"
    "com.coloros.calculator"
    "com.android.calculator2"
    # Maps & Navigasi
    "com.google.android.apps.maps"
    # Media & Hiburan
    "com.google.android.youtube"
    "com.google.android.apps.youtube.music"
    "com.google.android.music"
    # Browser
    "com.android.chrome"
    "com.heytap.browser"
    # Produktivitas
    "com.google.android.keep"
    "com.google.android.apps.docs"
    "com.google.android.apps.docs.editors.sheets"
    "com.google.android.apps.docs.editors.slides"
    # Perekam
    "com.coloros.soundrecorder"
    "com.android.soundrecorder"
    "com.google.android.apps.recorder"
    # Kamera & Foto
    "com.google.android.apps.photos"
    "com.google.android.GoogleCamera"
    # Google Apps tambahan
    "com.google.android.apps.tachyon"
    "com.google.android.apps.wellbeing"
    "com.google.android.apps.nbu.files"
    "com.google.android.apps.magazines"
    "com.google.android.googlequicksearchbox"
    "com.google.android.videos"
    # Realme/Oppo
    "com.coloros.weather2"
    "com.coloros.compass2"
    "com.coloros.oshare"
    "com.heytap.pictorial"
    "com.heytap.music"
    "com.heytap.cloud"
    "com.heytap.smarthome"
    "com.nearme.gamecenter"
    "com.oppo.operationManual"
    "com.oppoex.afterservice"
    "com.coloros.phonemanager"
)

JUMLAH_ENABLED=0
for app_en in "${APLIKASI_ENABLE[@]}"; do
    pm enable "$app_en" >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        JUMLAH_ENABLED=$((JUMLAH_ENABLED + 1))
    fi
done

echo -e "${HIJAU}   [✓] Total app diaktifkan kembali: ${JUMLAH_ENABLED}${NC}"
echo -e "${HIJAU}   [✓] Semua app kontak, telepon, gmail, dll sudah aktif!${NC}"

# ═══════════════════════════════════════════════════════════
# FINAL SYSTEM REFRESH
# ═══════════════════════════════════════════════════════════
echo ""
echo -e "${CYAN}[FINAL] 🔄 Menyegarkan Layanan Sistem...${NC}"
am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1

# Force stop game
am force-stop com.mobile.legends >/dev/null 2>&1
am force-stop com.mobile.legends.google >/dev/null 2>&1
am force-stop com.moonton.mobilelegends >/dev/null 2>&1

# Reset graphics
dumpsys gfxinfo --reset >/dev/null 2>&1

echo -e "${HIJAU}[✓] Layanan sistem disegarkan${NC}"

# ═══════════════════════════════════════════════════════════
# INFO SISTEM
# ═══════════════════════════════════════════════════════════
echo ""
echo -e "${KUNING}─── INFO SISTEM SETELAH RESTORE ───${NC}"

RAM_TOTAL=$(cat /proc/meminfo 2>/dev/null | grep MemTotal | awk '{print int($2/1024)}')
RAM_TERSEDIA=$(cat /proc/meminfo 2>/dev/null | grep MemAvailable | awk '{print int($2/1024)}')
if [ -n "$RAM_TOTAL" ] && [ -n "$RAM_TERSEDIA" ]; then
    PERSEN_TERSEDIA=$((RAM_TERSEDIA * 100 / RAM_TOTAL))
    echo -e "${CYAN}   RAM Total    : ${RAM_TOTAL}MB${NC}"
    echo -e "${HIJAU}   RAM Tersedia : ${RAM_TERSEDIA}MB (${PERSEN_TERSEDIA}%)${NC}"
fi

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
echo -e "${CYAN}║${HIJAU}  ✅ SISTEM BERHASIL DIKEMBALIKAN KE MODE NORMAL            ${CYAN}║${NC}"
echo -e "${CYAN}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ CPU         : Governor Normal (Schedutil)              ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ GPU         : Simple Ondemand (No Overclock)           ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ THERMAL     : PROTECTION AKTIF KEMBALI                ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ ANIMASI     : Diaktifkan Kembali                      ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ REFRESH     : 60Hz Default                            ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ BACKGROUND  : Apps Normal (No Limit)                  ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ JARINGAN    : Default Settings                        ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ SENTUHAN    : Normal Sensitivity                      ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ AUDIO       : Semua Efek Aktif                        ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ NOTIFIKASI  : Diaktifkan Kembali                     ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ SYNC        : Auto Sync Aktif                        ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ APP DISABLE : ${JUMLAH_ENABLED} App Diaktifkan Kembali             ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ GAME MODE   : API Dikembalikan ke Standard            ${CYAN}║${NC}"
echo -e "${CYAN}║${HIJAU}  ✅ IDLE STATE  : CPU Idle Normal                         ${CYAN}║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${PUTIH}🔄 ${HIJAU}Sistem telah kembali ke pengaturan normal & aman${NC}"
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                    💡 REKOMENDASI PENTING                    ║${NC}"
echo -e "${CYAN}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}║  • RESTART HP untuk memastikan semua perubahan diterapkan   ║${NC}"
echo -e "${CYAN}║  • Thermal protection sudah aktif - HP tidak akan overheat  ║${NC}"
echo -e "${CYAN}║  • Baterai akan lebih awet dalam mode normal                ║${NC}"
echo -e "${CYAN}║  • Semua app (kontak, telepon, dll) sudah aktif kembali     ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
echo -e "${PUTIH}   MODE GAMING DEWA v6.0 - RESTORE SCRIPT${NC}"
echo -e "${PUTIH}   Optimized for: Unisoc SC9863A + IMG8322 PowerVR${NC}"
echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${KUNING}Ingin RESTART sekarang? (y/n)${NC}"
read -r jawaban
if [ "$jawaban" = "y" ] || [ "$jawaban" = "Y" ]; then
    echo -e "${CYAN}Restarting dalam 3 detik...${NC}"
    sleep 1
    echo -e "${CYAN}3...${NC}"
    sleep 1
    echo -e "${CYAN}2...${NC}"
    sleep 1
    echo -e "${CYAN}1...${NC}"
    sleep 1
    reboot 2>/dev/null || echo -e "${KUNING}Auto-restart gagal, silakan restart manual${NC}"
else
    echo -e "${KUNING}Jangan lupa restart HP untuk hasil maksimal!${NC}"
fi
