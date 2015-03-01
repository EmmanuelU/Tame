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

import com.emman.tame.dialogs.ScriptPreference;

public class RunAtBoot extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
        }
        new SetTameCommands(this).execute();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class SetTameCommands extends AsyncTask<Void, Void, Void> {

        Context context;
	SharedPreferences mPreferences;

        public SetTameCommands(Context context) {
            this.context = context;
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected Void doInBackground(Void... args) {
            ScriptPreference.ExecuteOnBootCommands(mPreferences);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
