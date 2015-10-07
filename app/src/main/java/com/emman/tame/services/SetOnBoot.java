package com.emman.tame.services;

import android.app.IntentService;
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

import com.emman.tame.BootCompletedReceiver;
import com.emman.tame.MainActivity;
import com.emman.tame.R;
import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class SetOnBoot extends IntentService implements Resources {

    public SetOnBoot() {
	super(TAME_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	MainActivity.ExecuteBootData(this, PreferenceManager.getDefaultSharedPreferences(this));
	if(!MainActivity.BootCommandsCompleted) Utils.notification(this, NotificationID.SOBFAIL, new Intent(this, SetOnBoot.class), true, getString(R.string.msg_sobfail));
	BootCompletedReceiver.completeWakefulIntent(intent);
    }

}
