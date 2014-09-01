/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.emman.tame;

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

import com.emman.tame.Resources;

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
	    } else if(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0"))) MainActivity.SetOnBootData(mPreferences);

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
