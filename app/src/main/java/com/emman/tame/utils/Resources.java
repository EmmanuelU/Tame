package com.emman.tame.utils;

import android.os.Environment;

public interface Resources {

	//Global Variables and stored data

	class OTA {
		public String device;
		public String latestversion;
		public String latestversiondl;
		public String latestversionreldate;
		public int versionstamp;
		public int latestversionstamp;
		public boolean islatestversion = false;
		public boolean fetchedlatestversion = false;
	}

	public static final String FILE_TEXT_FORMAT = "text/*";
	public static final String EXTRA_FORCE_SHOW_LIGHTS = "android.forceShowLights"; //supported by most custom AOSP ROMs
	public static final String PACKAGE_SUPERSU = "eu.chainfire.supersu";
	public static final String LINK_PACKAGE_SUPERSU = "market://details?id=eu.chainfire.supersu";

	public static final String NEW_LINE = "\n";
	public static final String LINE_SPACE = " ";

	public static final String TAG = "Tame";
	public static final String PATH_LOCAL = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	public static final String PATH_TAME = PATH_LOCAL + TAG + "/";
	public static final String LINK_APP_UPDATE = "https://raw.githubusercontent.com/EmmanuelU/Tame/master/releases/updatetame.sh";
	public static final String LINK_WK_CPU_PATCH = "https://github.com/EmmanuelU/wild_kernel_htc_msm8660/commit/f9d17e63e7f9055fde6febd0b709405b023bdb38";
	public static final String TAME_SERVICE = "TamerService";

	public static final String FILE_UPDATE_DATA = PATH_TAME + "updatewild.sh";
	public static final String FILE_APP_UPDATE_DATA = PATH_TAME + "updatetame.sh";
	public static final String FILE_SYS_QUEUE = PATH_TAME + "Tame-SYSQueue.sh";

	public static final String TAME_DEBUG = "tame_debug";
	public static final String FILE_TAME_LOG = PATH_TAME + "Tame-LOG.txt";

	public static final String TAME_LAST_KMSG = PATH_TAME + "last_kmsg.txt";
	public static final String TAME_LOGCAT = PATH_TAME + "logcat.txt";

	public static final String FILE_DISABLE_SET_ON_BOOT_ZIP = PATH_TAME + "DisableTame_S-O-B.zip";
	public static final String FILE_DISABLE_SET_ON_BOOT_ZIP_MD5 = FILE_DISABLE_SET_ON_BOOT_ZIP + ".md5";
	public static final String FILE_DISABLE_SET_ON_BOOT = PATH_LOCAL + "WildNFree.tame";

	public static final String FILE_TMP_BUILD_PROP = PATH_TAME + "tmp.prop";
	public static final String FILE_LOCAL_BUILD_PROP = PATH_TAME + "build.prop";
	public static final String FILE_BACKUP_BUILD_PROP = PATH_TAME + "build.prop.bak";
	
	public static final String TAME_UID = "tame_uid";
	public static final String SET_ON_BOOT = "set_on_boot";
	public static final String SET_ON_BOOT_TS = "set_on_boot_ts";
	public static final String RUN_AT_BOOT = "run_at_boot";
	public static final String CPU_SET_ON_BOOT = "cpu_set_on_boot";
	public static final String CHECK_UPDATE_AT_BOOT = "check_update_at_boot";
	public static final String RUN_AT_BOOT_COMMANDS = "run_at_boot_commands";

	public static final String S2W = "s2w";
	public static final String FILE_S2W_TOGGLE = "/sys/android_touch/sweep2wake";
	public static final String S2S = "s2s";
	public static final String FILE_S2S_TOGGLE = "/sys/android_touch/sweep2sleep";
	public static final String S2W_SENSITIVE = "s2w_sensitive";
	public static final String FILE_S2W_SENSITIVE = "/sys/android_touch/sweep2wake_sensitive";
	public static final String DT2W = "dt2w";
	public static final String FILE_DT2W_TOGGLE = "/sys/android_touch/doubletap2wake";

	public static final String SMP_CONTROL = "mpdec";
	public static final String SMP_SCROFF = "mpdec_scroff";

	public static final String FILE_MPDEC_TOGGLE = "/sys/kernel/msm_mpdecision/conf/enabled";
	public static final String FILE_MPDEC_SCROFF = "/sys/kernel/msm_mpdecision/conf/scroff_single_core";

	public static final String FILE_INTELLIP_TOGGLE = "/sys/module/intelli_plug/parameters/intelli_plug_active";
	public static final String FILE_INTELLIP_SCROFF_FREQ = "/sys/module/intelli_plug/parameters/screen_off_max";
	public static final String FILE_INTELLIP_BOOST = "/sys/module/intelli_plug/parameters/touch_boost_active";
	public static final String INTELLIP_BOOST = "intellip_boost";
	public static final String INTELLIP_LIMIT_SCROFF = "intellip_limit_scroff";
	public static final String INTELLIP_LIMIT_SCROFF_FREQ = "intellip_limit_scroff_freq";

	public static final String TOUCHKEY_BLN = "touchkey_bln";
	public static final String FILE_BLN_TOGGLE = "/sys/class/misc/backlightnotification/enabled";
	public static final String TOUCHKEY_BLN_MAX_BLINK = "touchkey_bln_max_blink";

	public static final String FILE_BLN_MAX_BLINK = "/sys/class/misc/backlightnotification/max_blink_count";
	public static final String TOUCHKEY_BLN_BLINK_OVERRIDE = "touchkey_bln_blink_override";
	public static final String FILE_BLN_BLINK_OVERRIDE = "/sys/class/misc/backlightnotification/override_blink_interval";
	public static final String FILE_EBLN = "/sys/class/misc/enhanced_bln/blink_control";
	public static final String TOUCHKEY_EBLN_BLINK_TIMEOUT = "touchkey_ebln_blink_timeout";
	public static final String FILE_EBLN_BLINK_TIMEOUT = "/sys/class/misc/enhanced_bln/blink_timeout_ms";
	public static final String FILE_EBLN_BLINK_OVERRIDE = "/sys/class/misc/enhanced_bln/blink_override_interval_ms";

	public static final String TOUCHKEY_BLN_BLINK_RGB = "touchkey_bln_blink_rgb";
	public static final String FILE_HBLN_BLINK_OVERRIDE = "/sys/class/leds/R/device/led_blink_interval";
	public static final String FILE_HBLN_BLINK_RGB = "/sys/class/leds/R/device/led_blink_rgb";
	
	public static final String SAVED_DISPLAY_UV = "saved_panel_uv";
	public static final String FILE_CELOX_DISPLAY_UV = "/sys/module/board_msm8x60_celox/parameters/panel_uv";

	public static final String SAVED_MIN_FREQ = "saved_min_freq";
	public static final String FREQ_MIN_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
	public static final String SAVED_MAX_FREQ = "saved_max_freq";
	public static final String FREQ_MAX_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	public static final String SAVED_GOV = "saved_gov";
	public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	public static final String GOV_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
	public static final String SAVED_VDD_LEVELS = "vdd_levels";
	public static final String VDD_LEVELS_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/vdd_levels";
	public static final String CPU_TOGGLE = "/sys/devices/system/cpu/cpu0/enabled";
	public static final String SAVED_CPU_TOGGLE = "saved_cpu_enabled";
	public static final String CPU_ONLINE = "/sys/devices/system/cpu/cpu0/online";
	public static final String FREQ_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
	public static final String FREQINFO_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";
	public static final String NUM_OF_CPUS_PATH = "/sys/devices/system/cpu/present";
	public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
	public static final String CPU_TIME_IN_STATE = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
	public static final String KERNEL_BUILD_VERSION = "/proc/version";
	public static final String KERNEL_INFO = "/proc/cpuinfo";

	public static final String SAVED_IOSCHED = "saved_iosched";
	public static final String IOSCHED_LIST_FILE = "/sys/block/mmcblk0/queue/scheduler";
	public static final String READAHEAD_FILE = "/sys/block/mmcblk0/queue/read_ahead_kb";
	public static final String SAVED_READAHEAD = "saved_readahead";
	public static final String SAVED_SCHED_MC = "saved_sched_mc";
	public static final String SCHED_MC_FILE = "/sys/devices/system/cpu/sched_mc_power_savings";
	public static final String SAVED_CPU_BOOST_INPUT_BOOST = "saved_cpu_boost_input_boost";
	public static final String SAVED_CPU_BOOST_INPUT_FREQ = "saved_cpu_boost_input_freq";
	public static final String SAVED_CPU_BOOST_INPUT_DUR = "saved_cpu_boost_input_dur";
	public static final String SAVED_CPU_BOOST_INPUT_UP_THRESHOLD = "saved_cpu_boost_input_up_threshold";
	public static final String CPU_BOOST_INPUT_FREQ_FILE = "/sys/module/cpu_input_boost/parameters/input_boost_freq";
	public static final String CPU_BOOST_INPUT_DUR_FILE = "/sys/module/cpu_input_boost/parameters/input_boost_ms";
	public static final String CPU_BOOST_INPUT_TOGGLE = "/sys/module/cpu_input_boost/parameters/enabled";
	public static final String CPU_BOOST_INPUT_UP_THRESHOLD_FILE = "/sys/module/cpu_input_boost/parameters/up_threshold";
	public static final String CPU_BOOST_INPUT2_TOGGLE = "/sys/kernel/cpu_input_boost/enabled";
	public static final String CPU_BOOST_INPUT2_FREQS = "/sys/kernel/cpu_input_boost/boost_freqs";
	public static final String SAVED_CPU_BOOST_INPUT_FREQS = "saved_cpu_boost_input_freqs";

	public static final String SAVED_CPU_GOV_SYNC = "force_cpu_gov_sync";
	public static final String RETAIN_CPU_GOV_SYNC_FILE = "/sys/kernel/retain_cpu_policy/policy_sync";
	public static final String CPU_GOV_SYNC_FILE = (Utils.fileExists(RETAIN_CPU_GOV_SYNC_FILE) ? RETAIN_CPU_GOV_SYNC_FILE : "/sys/kernel/cpu_gov_sync/force_cpu_gov_sync");

	public static final String SAVED_FORCE_FAST_CHARGE = "force_fast_charge";
	public static final String SAVED_FORCE_FAST_CHARGE_LEVEL = "fast_charge_level";
	public static final String FORCE_FAST_CHARGE_FILE = "/sys/kernel/fast_charge/force_fast_charge";
	public static final String FORCE_FAST_CHARGE_LEVEL_FILE = "/sys/kernel/fast_charge/fast_charge_level";
	public static final String AVAILABLE_FAST_CHARGE_LIST = "/sys/kernel/fast_charge/available_charge_levels";

	public static final String SAVED_GPU_MAX_FREQ = "saved_gpu_max_freq";
	public static final String GPU_FREQ_FILE = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies";
	public static final String GPU_MAX_FREQ_FILE = "/sys/class/kgsl/kgsl-3d0/max_gpuclk";
	public static final String GPU_CUR_FREQ_FILE = "/sys/class/kgsl/kgsl-3d0/gpuclk";

	public static final String LAST_KMSG = "/proc/last_kmsg";

	public static final String SAVED_TCP_PROTOCOL = "saved_tcp_protocol";
	public static final String TCP_PROTOCOL = "net.ipv4.tcp_congestion_control";
	public static final String TCP_AVAILABLE_PROTOCOLS = "net.ipv4.tcp_available_congestion_control";

	/* Deprecated variables */
	public static final String FILE_SET_ON_BOOT = PATH_TAME + "Tame-SOB.sh";
	public static final String FILE_RUN_AT_BOOT = PATH_TAME + "Tame-RAB.sh";

}
