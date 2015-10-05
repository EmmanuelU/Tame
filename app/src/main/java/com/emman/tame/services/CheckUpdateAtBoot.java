package com.emman.tame.services;

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

import com.emman.tame.fragments.AboutTame;
import com.emman.tame.R;
import com.emman.tame.utils.DownloadTask;
import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class CheckUpdateAtBoot extends Service implements Resources {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
        }
        new CheckUpdates(this).execute();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static interface onUpdateTaskFinished {
	void onFinish();
    }

    class UpdateTask extends DownloadTask {

	private ArrayList<onUpdateTaskFinished> observers = new ArrayList<onUpdateTaskFinished>();

	public UpdateTask(Context context, String file) {
	    super(context, file, null, false);
	}

        public void setOnFinishListener(onUpdateTaskFinished observer) {
            observers.add(observer);
        }

	@Override
    	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		for(onUpdateTaskFinished observer : observers) {
                	observer.onFinish();
          	}
   	}

    }


    class CheckUpdates extends AsyncTask<Void, Void, Void> {

        Context context;
	SharedPreferences mPreferences;
	OTA WildData;
	OTA TameData;

        public CheckUpdates(Context context) {
            this.context = context;
            WildData = new OTA();
            TameData = new OTA();
        }

    private boolean WildInit(){
	if(Utils.fileExists(FILE_UPDATE_DATA)){
		Utils.CMD(false, "chmod +x " + FILE_UPDATE_DATA);
		WildData.device = AboutTame.propdevice;
		WildData.versionstamp = Integer.parseInt(Utils.CMD(false, "getprop ro.wild.date"));
		WildData.latestversion = Utils.CMD(false, "sh " + FILE_UPDATE_DATA + " latestversion");
		WildData.latestversionstamp = Integer.parseInt(Utils.CMD(false, "sh " + FILE_UPDATE_DATA + " latestdate"));
		WildData.latestversiondl = Utils.CMD(false, "sh " + FILE_UPDATE_DATA + " latestDL");
		WildData.latestversionreldate = Utils.CMD(false, "sh " + FILE_UPDATE_DATA + " latestdateliteral");
		Utils.CMD(false, "rm -rf " + FILE_UPDATE_DATA);
		return true;
	}
	return false;
    }
    private boolean TameInit(){
	if(Utils.fileExists(FILE_APP_UPDATE_DATA)){
		Utils.CMD(false, "chmod +x " + FILE_APP_UPDATE_DATA);
		try{
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			TameData.versionstamp = pInfo.versionCode;
		} catch(Exception e){
			TameData.versionstamp = 99999;
		}
		TameData.latestversion = Utils.CMD(false, "sh " + FILE_APP_UPDATE_DATA + " latestversion");
		TameData.latestversionstamp = Integer.parseInt(Utils.CMD(false, "sh " + FILE_APP_UPDATE_DATA + " latestversioncode"));
		TameData.latestversiondl = Utils.CMD(false, "sh " + FILE_APP_UPDATE_DATA + " latestDL");
		TameData.latestversionreldate = Utils.CMD(false, "sh " + FILE_APP_UPDATE_DATA + " latestdateliteral");
		Utils.CMD(false, "rm -rf " + FILE_APP_UPDATE_DATA);
		return true;
	}
	return false;
    }

        @SuppressWarnings("deprecation")
        @Override
        protected Void doInBackground(Void... args) {
		try{
			//WildKernel Update
			if(AboutTame.isWild()){
				if(Utils.isNetworkOnline(context)){
					final UpdateTask wildDownloadTask = new UpdateTask(context, FILE_UPDATE_DATA);
					wildDownloadTask.setOnFinishListener(new onUpdateTaskFinished() {
						@Override
						public void onFinish() {
							if(WildInit()){
								if(WildData.latestversionstamp > WildData.versionstamp && !WildData.latestversion.equals(AboutTame.propversion)){
									Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WildData.latestversiondl));
									Utils.notification(context, NotificationID.WKUPDATE, browserIntent, "WildKernel Build " + LINE_SPACE + context.getString(R.string.msg_app_update, WildData.latestversion, WildData.latestversionreldate));
								}
							}
						}

					});
					wildDownloadTask.execute(AboutTame.propotalink);
				}
			}

			//Tame Update
			if(Utils.isNetworkOnline(context)){
				final UpdateTask tameDownloadTask = new UpdateTask(context, FILE_APP_UPDATE_DATA);
				tameDownloadTask.setOnFinishListener(new onUpdateTaskFinished() {
					@Override
					public void onFinish() {
						if(TameInit()){
							if(TameData.latestversionstamp > TameData.versionstamp){
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TameData.latestversiondl));
								Utils.notification(context, NotificationID.APPUPDATE, browserIntent, "Tame v" + LINE_SPACE + context.getString(R.string.msg_app_update, TameData.latestversion, TameData.latestversionreldate));
							}
						}
					}

				});
				tameDownloadTask.execute(LINK_APP_UPDATE);
			}
		} catch (Exception unhandled) {}

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
