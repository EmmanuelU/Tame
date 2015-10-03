package com.emman.tame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.Secure;

import com.emman.tame.MainActivity;
import com.emman.tame.services.CheckUpdateAtBoot;
import com.emman.tame.services.SetOnBoot;
import com.emman.tame.services.RunAtBoot;
import com.emman.tame.services.PropAtBoot;

import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class BootCompletedReceiver extends WakefulBroadcastReceiver implements Resources {

    @Override
    public void onReceive(Context context, Intent intent) {
	SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(Utils.isStringEmpty(mPreferences.getString(TAME_UID, ""))) updateSharedPrefs(mPreferences, TAME_UID, Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
	else if(!mPreferences.getString(TAME_UID, "").equals(Secure.getString(context.getContentResolver(), Secure.ANDROID_ID))){
		Utils.notification(context, NotificationID.UID, null, context.getString(R.string.msg_uid_change));
		updateSharedPrefs(mPreferences, TAME_UID, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
	}

	if(Utils.fileExists(FILE_DISABLE_SET_ON_BOOT)){
		Utils.notification(context, NotificationID.DSOB, null, context.getString(R.string.msg_dsob));
		Utils.CMD(false, "rm -rf " + FILE_DISABLE_SET_ON_BOOT);
		updateSharedPrefs(mPreferences, SET_ON_BOOT, "0");
		updateSharedPrefs(mPreferences, RUN_AT_BOOT, "0");
		updateSharedPrefs(mPreferences, RETAIN_PROP_ENTRIES, "0");
	} else {
		if((Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0")) || Utils.stringToBool(mPreferences.getString(RUN_AT_BOOT, "0"))|| Utils.stringToBool(mPreferences.getString(RETAIN_PROP_ENTRIES, "0"))) && !Utils.canSU()){
			Intent notifIntent = new Intent();
			Utils.notification(context, NotificationID.ROOTFAIL, notifIntent, context.getString(R.string.msg_lp_no_su));
			Utils.toast(context, context.getString(R.string.msg_fatal_error));
		}
		if(Utils.stringToBool(mPreferences.getString(RUN_AT_BOOT, "0"))) startWakefulService(context, new Intent(context, RunAtBoot.class));
		if(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0"))) startWakefulService(context, new Intent(context, SetOnBoot.class));
		if(Utils.stringToBool(mPreferences.getString(RETAIN_PROP_ENTRIES, "0"))) startWakefulService(context, new Intent(context, PropAtBoot.class));
	}

	if(Utils.stringToBool(mPreferences.getString(CHECK_UPDATE_AT_BOOT, "1"))) context.startService(new Intent(context, CheckUpdateAtBoot.class));
    }
    
    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }
}
