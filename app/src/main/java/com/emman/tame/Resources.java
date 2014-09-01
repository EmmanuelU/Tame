package com.emman.tame;

public interface Resources {

	//Global Vars & Stored Data

	class Wild {
		String device;
		String latestversion;
		String latestversiondl;
		String latestversionreldate;
		int versionstamp;
		int latestversionstamp;
		boolean islatestversion = false;
		boolean fetchedlatestversion = false;
	}

	public static final String FILE_UPDATE_DATA = "/sdcard/updatewild.sh";

	public static final String FILE_DISABLE_SET_ON_BOOT_ZIP = "DisableTame_S-O-B.zip";
	public static final String PATH_DISABLE_SET_ON_BOOT_ZIP = "/sdcard/" + FILE_DISABLE_SET_ON_BOOT_ZIP;
	public static final String FILE_DISABLE_SET_ON_BOOT = "/sdcard/WildNFree.tame";
	public static final String SET_ON_BOOT = "set_on_boot";

	public static final String S2W = "s2w";
	public static final String FILE_S2W_TOGGLE = "/sys/android_touch/sweep2wake";

	public static final String MPDEC = "mpdec";
	public static final String MPDEC_SCROFF = "mpdec_scroff";
	public static final String FILE_MPDEC_TOGGLE = "/sys/kernel/msm_mpdecision/conf/enabled";
	public static final String FILE_MPDEC_SCROFF = "/sys/kernel/msm_mpdecision/conf/scroff_single_core";

	public static final String TOUCHKEY_BLN = "touchkey_bln";
	public static final String TOUCHKEY_BLN_MAX_BLINK = "touchkey_bln_max_blink";
	public static final String TOUCHKEY_BLN_BLINK_OVERRIDE = "touchkey_bln_blink_override";
	public static final String FILE_BLN_TOGGLE = "/sys/class/misc/backlightnotification/enabled";
	public static final String FILE_BLN_MAX_BLINK = "/sys/class/misc/backlightnotification/max_blink_count";
	public static final String FILE_BLN_BLINK_OVERRIDE = "/sys/class/misc/backlightnotification/override_blink_interval";

	public static final String SAVED_MIN_FREQ = "saved_min_freq";
	public static final String SAVED_MAX_FREQ = "saved_max_freq";
	public static final String SAVED_GOV = "saved_gov";
	public static final String SAVED_IOSCHED = "saved_iosched";
	public static final String SAVED_SCHED_MC = "saved_sched_mc";
	public static final String FREQ_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
	public static final String FREQINFO_CUR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq";
	public static final String GOV_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	public static final String GOV_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
	public static final String IOSCHED_LIST_FILE = "/sys/block/mmcblk0/queue/scheduler";
	public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
	public static final String FREQ_MIN_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
	public static final String FREQ_MAX_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	public static final String SCHED_MC_FILE = "/sys/devices/system/cpu/sched_mc_power_savings";
	

}
