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
import com.emman.tame.fragments.BuildPropEditor;
import com.emman.tame.utils.Resources;

public class PropAtBoot extends IntentService implements Resources {

    public PropAtBoot() {
	super(TAME_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	BuildPropEditor.ExecuteBootProperties(this, PreferenceManager.getDefaultSharedPreferences(this));
	BootCompletedReceiver.completeWakefulIntent(intent);
    }

}
