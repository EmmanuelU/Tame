package com.emman.tame.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.emman.tame.MainActivity;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class SetOnBoot extends Service implements Resources {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
        }
        new SetTameSettings(this).execute();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class SetTameSettings extends AsyncTask<Void, Void, Void> {

        Context context;

        public SetTameSettings(Context context) {
            this.context = context;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected Void doInBackground(Void... args) {
            SharedPreferences mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
	    if(Utils.fileExists(FILE_DISABLE_SET_ON_BOOT)){
		Utils.CMD("rm -rf " + FILE_DISABLE_SET_ON_BOOT, false);
		updateSharedPrefs(mPreferences, SET_ON_BOOT, "0");
	    } else if(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0"))) MainActivity.ExecuteBootData(mPreferences);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            stopSelf();
        }

	void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(var, value);
		editor.commit();
	}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
