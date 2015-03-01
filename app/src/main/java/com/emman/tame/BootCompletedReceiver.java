package com.emman.tame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.emman.tame.services.SetOnBoot;
import com.emman.tame.services.RunAtBoot;

import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class BootCompletedReceiver extends BroadcastReceiver implements Resources {

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
	SharedPreferences mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
	if(Utils.fileExists(FILE_DISABLE_SET_ON_BOOT)){
		Utils.CMD("rm -rf " + FILE_DISABLE_SET_ON_BOOT, false);
		updateSharedPrefs(mPreferences, SET_ON_BOOT, "0");
		updateSharedPrefs(mPreferences, RUN_AT_BOOT, "0");
	} else {
		if(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0"))) context.startService(new Intent(context, SetOnBoot.class));
		if(Utils.stringToBool(mPreferences.getString(RUN_AT_BOOT, "0"))) context.startService(new Intent(context, RunAtBoot.class));
	}
    }
}
