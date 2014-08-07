package com.emman.tame;

public interface Resources {

	//Global Vars
	public static final String FILE_UPDATE_DATA = "/sdcard/updatewild.sh";
	public static final String FILE_BLN_TOGGLE = "/sys/class/misc/backlightnotification/enabled";
	public static final String FILE_BLN_MAX_BLINK = "/sys/class/misc/backlightnotification/max_blink_count";
	public static final String FILE_BLN_BLINK_OVERRIDE = "/sys/class/misc/backlightnotification/override_blink_interval";

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
	//Stored Data
	public static String SET_ON_BOOT = "set_on_boot";

	public static String TOUCHKEY_BLN = "touchkey_bln";
	public static String TOUCHKEY_BLN_MAX_BLINK = "touchkey_bln_max_blink";
	public static String TOUCHKEY_BLN_BLINK_OVERRIDE = "touchkey_bln_blink_override";

}
