package com.emman.tame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.Secure;

import com.emman.tame.services.CheckUpdateAtBoot;
import com.emman.tame.services.SetOnBoot;
import com.emman.tame.services.RunAtBoot;

import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class BootCompletedReceiver extends BroadcastReceiver implements Resources {

    @Override
    public void onReceive(Context context, Intent intent) {
	SharedPreferences mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
                    
        if(Utils.isStringEmpty(mPreferences.getString(TAME_UID, ""))) updateSharedPrefs(mPreferences, TAME_UID, Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
	else if(!mPreferences.getString(TAME_UID, "").equals(Secure.getString(context.getContentResolver(), Secure.ANDROID_ID))){
		Utils.notification(context, NotificationID.UID, null, "You previously used Tame from a different device. While you shouldn't run into any problems, you may consider resetting my data.");
		updateSharedPrefs(mPreferences, TAME_UID, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
	}

	if(Utils.stringToBool(mPreferences.getString(CHECK_UPDATE_AT_BOOT, "1"))) context.startService(new Intent(context, CheckUpdateAtBoot.class));
	
	if(Utils.fileExists(FILE_DISABLE_SET_ON_BOOT)){
		Utils.CMD("rm -rf " + FILE_DISABLE_SET_ON_BOOT, false);
		updateSharedPrefs(mPreferences, SET_ON_BOOT, "0");
		updateSharedPrefs(mPreferences, RUN_AT_BOOT, "0");
	} else {
		if((Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0")) || Utils.stringToBool(mPreferences.getString(RUN_AT_BOOT, "0"))) && !Utils.canSU()){
			Intent notifIntent = new Intent();
			//intent.setData(Uri.parse(LINK_PACKAGE_SUPERSU));
			Utils.notification(context, NotificationID.ROOTFAIL, notifIntent, context.getString(R.string.msg_lp_no_su));
			Utils.toast(context, context.getString(R.string.msg_fatal_error));
			//this.finish();
		}
		if(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0"))) context.startService(new Intent(context, SetOnBoot.class));
		if(Utils.stringToBool(mPreferences.getString(RUN_AT_BOOT, "0"))) context.startService(new Intent(context, RunAtBoot.class));
	}
    }
    
    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }
}
