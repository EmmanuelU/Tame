package com.emman.tame.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.List;

import com.emman.tame.BootCompletedReceiver;
import com.emman.tame.fragments.AboutTame;
import com.emman.tame.R;
import com.emman.tame.utils.DownloadTask;
import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class CheckUpdateAtBoot extends IntentService implements Resources {

    OTA WildData;
    OTA TameData;

    public CheckUpdateAtBoot() {
	super(TAME_SERVICE);
	WildData = new OTA();
	TameData = new OTA();
    }

    private boolean WildInit(String script){
	if(Utils.isStringEmpty(script)) return false;

	Utils.writeFile(FILE_UPDATE_DATA, script);
	Utils.CMD(false, "chmod +x " + FILE_UPDATE_DATA);
	WildData.latestversion = Utils.CMD(false, "sh " + FILE_UPDATE_DATA + " latestversion");
	WildData.latestversionstamp = Integer.parseInt(Utils.CMD(false, "sh " + FILE_UPDATE_DATA + " latestdate"));
	WildData.latestversiondl = Utils.CMD(false, "sh " + FILE_UPDATE_DATA + " latestDL");
	WildData.latestversionreldate = Utils.CMD(false, "sh " + FILE_UPDATE_DATA + " latestdateliteral");
	Utils.CMD(false, "rm -rf " + FILE_UPDATE_DATA);
	return true;
    }

    private boolean TameInit(String script){
	if(Utils.isStringEmpty(script)) return false;

	Utils.writeFile(FILE_APP_UPDATE_DATA, script);
	Utils.CMD(false, "chmod +x " + FILE_APP_UPDATE_DATA);
	TameData.latestversion = Utils.CMD(false, "sh " + FILE_APP_UPDATE_DATA + " latestversion");
	TameData.latestversionstamp = Integer.parseInt(Utils.CMD(false, "sh " + FILE_APP_UPDATE_DATA + " latestversioncode"));
	TameData.latestversiondl = Utils.CMD(false, "sh " + FILE_APP_UPDATE_DATA + " latestDL");
	TameData.latestversionreldate = Utils.CMD(false, "sh " + FILE_APP_UPDATE_DATA + " latestdateliteral");
	Utils.CMD(false, "rm -rf " + FILE_APP_UPDATE_DATA);
	return true;
    }

    private void CheckUpdate(){
	//WildKernel Update
	try{
		if(AboutTame.isWild()){
			if(WildInit(Utils.fetchTextFile(AboutTame.propotalink))){
				WildData.versionstamp = Integer.parseInt(AboutTame.propversiondate);
				if(WildData.latestversionstamp > WildData.versionstamp && !WildData.latestversion.equals(AboutTame.propversion)){
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WildData.latestversiondl));
					Utils.notification(this, NotificationID.WKUPDATE, browserIntent, "WildKernel Build " + LINE_SPACE + getString(R.string.msg_app_update, WildData.latestversion, WildData.latestversionreldate));
				}
			}
		}
	} catch (Exception unhandled) {}	
    }

    private void CheckAppUpdate(){
	//Tame Update
	try{
		if(TameInit(Utils.fetchTextFile(LINK_APP_UPDATE))){
			TameData.versionstamp = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			if(TameData.latestversionstamp > TameData.versionstamp){
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TameData.latestversiondl));
				Utils.notification(this, NotificationID.APPUPDATE, browserIntent, "Tame v" + LINE_SPACE + getString(R.string.msg_app_update, TameData.latestversion, TameData.latestversionreldate));
			}
		}
	} catch (Exception unhandled) {}
    }

    @Override
    protected void onHandleIntent(Intent intent) {
	if(Utils.isNetworkOnline(this)){
		CheckUpdate();
		CheckAppUpdate();
	}
	BootCompletedReceiver.completeWakefulIntent(intent);
    }
}
