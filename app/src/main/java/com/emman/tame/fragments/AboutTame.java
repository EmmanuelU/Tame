package com.emman.tame.fragments;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.emman.tame.R;
import com.emman.tame.utils.DownloadTask;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class AboutTame extends Fragment 
		implements Resources {

    private View mView;
    public static String propversion, propversiondate, propotalink, propdevice;
    private String currentAppVersion;
    private Button mAppUpdate;
    private Button mTamePreferences;
    private Button mUpdate;
    private SharedPreferences mPreferences;
    private TextView mTameLogo;
    private TextView mVersion;
    private TextView mAppVersion;
    private TextView mLatVersion;
    private TextView mAppLatVersion;
    private TextView mSOBNote;


    Animation fadein = new AlphaAnimation(0.0f, 1.0f);
    Animation fadeout = new AlphaAnimation(1.0f, 0.0f);
    ProgressDialog mCheckUpdateDialog;
    ProgressDialog mAppCheckUpdateDialog;

    Context mContext;
    private OTA WildData;
    private OTA TameData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	WildData = new OTA();
	TameData = new OTA();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	mView = inflater.inflate(R.layout.about_tame, container, false);
	mAppUpdate = (Button) mView.findViewById(R.id.app_update_button);
	mContext = getActivity();
	mTamePreferences = (Button) mView.findViewById(R.id.preferences);
	mUpdate = (Button) mView.findViewById(R.id.update_button);
	mTameLogo = (TextView) mView.findViewById(R.id.tame_logo);
	mVersion = (TextView) mView.findViewById(R.id.versionheader);
	mAppVersion = (TextView) mView.findViewById(R.id.appversionheader);
	mLatVersion = (TextView) mView.findViewById(R.id.latversionheader);
	mAppLatVersion = (TextView) mView.findViewById(R.id.applatversionheader);
	mSOBNote = (TextView) mView.findViewById(R.id.sobnote);

        mAppUpdate.setOnClickListener(new View.OnClickListener() {
		public void onClick(View view) {
			CheckAppUpdate();
		}
	});

	
        mTamePreferences.setOnClickListener(new View.OnClickListener() {
		public void onClick(View view) {
			final Dialog mTamePreferenceDialog = new Dialog(mContext);

			mTamePreferenceDialog.setContentView(R.layout.tamedialog);
			mTamePreferenceDialog.setTitle("Preferences");

			final CheckBox mCheckUpdate = (CheckBox) mTamePreferenceDialog.findViewById(R.id.check_update);
			final Button mDismiss = (Button) mTamePreferenceDialog.findViewById(R.id.dismiss);
			final SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

			mCheckUpdate.setChecked(Utils.stringToBool(mPreferences.getString(CHECK_UPDATE_AT_BOOT, "1")));

			mDismiss.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mTamePreferenceDialog.dismiss();
				}
			});

			mTamePreferenceDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					final SharedPreferences.Editor editor = mPreferences.edit();
					editor.putString(CHECK_UPDATE_AT_BOOT, Utils.boolToString(mCheckUpdate.isChecked()));
					editor.commit();
				}
			});
			
			mTamePreferenceDialog.show();

		}
	});

        mUpdate.setOnClickListener(new View.OnClickListener() {
		public void onClick(View view) {
			CheckUpdate();
		}
	});

	mSOBNote.setText("Note: After a reboot, Tame may take a couple minutes to re-apply saved settings. Opening this app before those settings are applied may cause all your settings to be reset to default.\n\n In the event that you screwed up and you want to disable Tame Settings from recovery, there is a flash-able zip on your sdcard.\n\n" + FILE_DISABLE_SET_ON_BOOT_ZIP);
	
	TameLogoAnim();
	mTameLogo.startAnimation(fadeout);
	setversiondata();

	if(!Utils.fileExists(FILE_DISABLE_SET_ON_BOOT_ZIP)) Utils.ExtractAssets(getActivity());

	if(Utils.fileExists("/sdcard/DisableTame_S-O-B.zip")) Utils.CMD("rm -rf /sdcard/DisableTame_S-O-B.zip", false); //remove deprecated zip

        return mView;
    }

    public static String getWildOta(String device){
	if(device.equals("hercules")) return "https://raw.githubusercontent.com/EmmanuelU/wild_kernel_samsung_msm8660/android-msm-hercules-3.0/flashable/tools/updatewild.sh";
	if(device.equals("skyrocket")) return "https://raw.githubusercontent.com/EmmanuelU/wild_kernel_samsung_msm8660/android-msm-skyrocket-3.0/flashable/tools/updatewild.sh";
	if(device.equals("e98x")) return "https://raw.githubusercontent.com/EmmanuelU/wild_kernel_lge_gproj/android-msm-e98x-3.4/flashable/tools/updatewild.sh";
	return "";
    }

    public static boolean isWild(){
	propversion = Utils.CMD("getprop ro.wild.version", false);
	propversiondate = Utils.CMD("getprop ro.wild.date", false);
	propdevice = Utils.CMD("getprop ro.wild.device", false);
	propotalink = getWildOta(propdevice);
	return (!propversion.isEmpty() || !propversiondate.isEmpty() || !propdevice.isEmpty() || !propotalink.isEmpty());
    }

    private void setversiondata(){
	mLatVersion.setVisibility(View.GONE);
	mAppLatVersion.setVisibility(View.GONE);
	try{
		PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		mAppVersion.setText(mAppVersion.getText().toString() + " " + pInfo.versionName);
		TameData.versionstamp = pInfo.versionCode;
	} catch(Exception e){
		mAppVersion.setText(mAppVersion.getText().toString() + " N/A");
		TameData.versionstamp = 99999;
	}
	if(isWild()){
		mVersion.setText(mVersion.getText().toString() + " " + propversion);
		WildData.versionstamp = Integer.parseInt(propversiondate);
	}
	else{
		mVersion.setVisibility(View.GONE);
		mUpdate.setVisibility(View.GONE);
	}
    }

    private boolean CheckUpdate(){
	if(!DownloadTask.isNetworkOnline(getActivity())){
		Utils.toast(getActivity(), "No Internet Access Detected");
	} else if(WildData.fetchedlatestversion && !WildData.islatestversion){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WildData.latestversiondl));
		startActivity(browserIntent);
	}
	else if(WildData.fetchedlatestversion) return false;
	else {
		mCheckUpdateDialog = new ProgressDialog(getActivity());
		mCheckUpdateDialog.setMessage("Checking for updates ...");
		mCheckUpdateDialog.setIndeterminate(true);
		mCheckUpdateDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mCheckUpdateDialog.setCancelable(true);
		final DownloadTask downloadTask = new DownloadTask(getActivity(), FILE_UPDATE_DATA, mCheckUpdateDialog, true);
		downloadTask.execute(propotalink);
		mCheckUpdateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    @Override
		    public void onCancel(DialogInterface dialog) {
			downloadTask.cancel(true);
		    }
		});

		mCheckUpdateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
		    @Override
		    public void onDismiss(DialogInterface dialog) {
			if(WildInit()){
				if(WildData.latestversionstamp > WildData.versionstamp && !WildData.latestversion.equals(propversion)) WildData.islatestversion = false;
				else WildData.islatestversion = true;
				if(WildData.islatestversion){
					mUpdate.setText("Latest Version");
					mUpdate.setEnabled(false);
				} else {
					mUpdate.setText("Click to Update");
					mLatVersion.setText(WildData.latestversion + " - " + WildData.latestversionreldate);
					mLatVersion.setVisibility(View.VISIBLE);
				}
				WildData.fetchedlatestversion = true;
			}
		    }
		});
	}
	return true;
    }

    private boolean CheckAppUpdate(){
	if(!DownloadTask.isNetworkOnline(getActivity())){
		Utils.toast(getActivity(), "No Internet Access Detected");
	} else if(TameData.fetchedlatestversion && !TameData.islatestversion){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TameData.latestversiondl));
		startActivity(browserIntent);
	}
	else if(TameData.fetchedlatestversion) return false;
	else {
		mAppCheckUpdateDialog = new ProgressDialog(getActivity());
		mAppCheckUpdateDialog.setMessage("Checking for updates ...");
		mAppCheckUpdateDialog.setIndeterminate(true);
		mAppCheckUpdateDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mAppCheckUpdateDialog.setCancelable(true);
		final DownloadTask downloadTask = new DownloadTask(getActivity(), FILE_APP_UPDATE_DATA, mAppCheckUpdateDialog, true);
		downloadTask.execute(LINK_APP_UPDATE);
		mAppCheckUpdateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    @Override
		    public void onCancel(DialogInterface dialog) {
			downloadTask.cancel(true);
		    }
		});

		mAppCheckUpdateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
		    @Override
		    public void onDismiss(DialogInterface dialog) {
			if(TameInit()){
				if(TameData.latestversionstamp > TameData.versionstamp) TameData.islatestversion = false;
				else TameData.islatestversion = true;
				if(TameData.islatestversion){
					mAppUpdate.setText("Latest Version");
					mAppUpdate.setEnabled(false);
				} else {
					mAppUpdate.setText("Click to Update");
					mAppLatVersion.setText(TameData.latestversion + " - " + TameData.latestversionreldate);
					mAppLatVersion.setVisibility(View.VISIBLE);
				}
				TameData.fetchedlatestversion = true;
			}
			else Utils.toast(getActivity(), "test");
		    }
		});
	}
	return true;
    }

    private boolean WildInit(){
	if(Utils.fileExists(FILE_UPDATE_DATA)){
		Utils.CMD("chmod +x " + FILE_UPDATE_DATA, false);
		WildData.device = propdevice;
		WildData.latestversion = Utils.CMD("sh " + FILE_UPDATE_DATA + " latestversion", false);
		WildData.latestversionstamp = Integer.parseInt(Utils.CMD("sh " + FILE_UPDATE_DATA + " latestdate", false));
		WildData.latestversiondl = Utils.CMD("sh " + FILE_UPDATE_DATA + " latestDL", false);
		WildData.latestversionreldate = Utils.CMD("sh " + FILE_UPDATE_DATA + " latestdateliteral", false);
		Utils.CMD("rm -rf " + FILE_UPDATE_DATA, false);
		return true;
	}
	return false;
    }
    private boolean TameInit(){
	if(Utils.fileExists(FILE_APP_UPDATE_DATA)){
		Utils.CMD("chmod +x " + FILE_APP_UPDATE_DATA, false);
		TameData.latestversion = Utils.CMD("sh " + FILE_APP_UPDATE_DATA + " latestversion", false);
		TameData.latestversionstamp = Integer.parseInt(Utils.CMD("sh " + FILE_APP_UPDATE_DATA + " latestversioncode", false));
		TameData.latestversiondl = Utils.CMD("sh " + FILE_APP_UPDATE_DATA + " latestDL", false);
		TameData.latestversionreldate = Utils.CMD("sh " + FILE_APP_UPDATE_DATA + " latestdateliteral", false);
		Utils.CMD("rm -rf " + FILE_APP_UPDATE_DATA, false);
		return true;
	}
	return false;
    }

    private void TameLogoAnim(){
	fadein.setDuration(1000);
	fadeout.setDuration(1000);
	fadeout.setStartOffset(1500);
	fadein.setAnimationListener(new AnimationListener() {

	    @Override
	    public void onAnimationStart(Animation animation) {

	    }

	    @Override
	    public void onAnimationEnd(Animation animation) {
		mTameLogo.startAnimation(fadeout);

	    }

	    @Override
	    public void onAnimationRepeat(Animation animation) {
			
	    }
	});
	fadeout.setAnimationListener(new AnimationListener() {

	    @Override
	    public void onAnimationStart(Animation animation) {

	    }

	    @Override
	    public void onAnimationEnd(Animation animation) {
		mTameLogo.startAnimation(fadein);

	    }

	    @Override
	    public void onAnimationRepeat(Animation animation) {
			
	    }
	});
    }
}
