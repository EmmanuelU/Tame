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
import android.widget.Switch;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.emman.tame.MainActivity;
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
    private Button mUpdate;
    private SharedPreferences mPreferences;
    private TextView mTameLogo;
    private TextView mVersion;
    private TextView mAppVersion;
    private TextView mLatVersion;
    private TextView mAppLatVersion;
    private TextView mLPSU;
    private TextView mSOBNote;
    private TextView mSOBStatus;


    Animation fadein = new AlphaAnimation(0.0f, 1.0f);
    Animation fadeout = new AlphaAnimation(1.0f, 0.0f);

    Context mContext;
    private OTA WildData;
    private OTA TameData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	MainActivity.setActionBarTitle(getActivity().getString(R.string.page_main), 1);
	MainActivity.setOnBackPressedListener(null); //normal operations

	WildData = new OTA();
	TameData = new OTA();

	
	mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	mView = inflater.inflate(R.layout.about_tame, container, false);
	mAppUpdate = (Button) mView.findViewById(R.id.app_update_button);
	mUpdate = (Button) mView.findViewById(R.id.update_button);
	mTameLogo = (TextView) mView.findViewById(R.id.tame_logo);
	mVersion = (TextView) mView.findViewById(R.id.versionheader);
	mAppVersion = (TextView) mView.findViewById(R.id.appversionheader);
	mLatVersion = (TextView) mView.findViewById(R.id.latversionheader);
	mAppLatVersion = (TextView) mView.findViewById(R.id.applatversionheader);
	mLPSU = (TextView) mView.findViewById(R.id.lp_no_su);
	mSOBNote = (TextView) mView.findViewById(R.id.sobnote);
	mSOBStatus = (TextView) mView.findViewById(R.id.sobstatus);

	mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mAppUpdate.setOnClickListener(new View.OnClickListener() {
		public void onClick(View view) {
			mAppUpdate.setText(getActivity().getString(R.string.msg_wait));
			CheckAppUpdate();
		}
	});

        mUpdate.setOnClickListener(new View.OnClickListener() {
		public void onClick(View view) {
			mUpdate.setText(getActivity().getString(R.string.msg_wait));
			CheckUpdate();
		}
	});
	
	mSOBNote.setText(getString(R.string.msg_sobnote) + FILE_DISABLE_SET_ON_BOOT_ZIP);
	mSOBNote.setEnabled(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0")));

	mSOBStatus.setText(getString(R.string.msg_sobstatus) + LINE_SPACE + mPreferences.getString(SET_ON_BOOT_TS, getActivity().getString(R.string.item_never)) + NEW_LINE + getString(R.string.msg_rabstatus) + LINE_SPACE + mPreferences.getString(RUN_AT_BOOT_TS, getActivity().getString(R.string.item_never)) + NEW_LINE + getString(R.string.msg_propstatus) + LINE_SPACE + mPreferences.getString(PROP_AT_BOOT_TS, getActivity().getString(R.string.item_never)));

        if(!Utils.isSubstringInString("SUPERSU", Utils.getSUVersion()) && Utils.isLollipop()){
		mLPSU.setVisibility(View.VISIBLE);
	}

	TameLogoAnim();
	mTameLogo.startAnimation(fadeout);
	setversiondata();

        return mView;
    }

    public static String getWildOta(String device){
	if(device.equals("hercules")) return "https://raw.githubusercontent.com/EmmanuelU/wild_kernel_samsung_msm8660/android-msm-hercules-3.0/flashable/tools/updatewild.sh";
	if(device.equals("skyrocket")) return "https://raw.githubusercontent.com/EmmanuelU/wild_kernel_samsung_msm8660/android-msm-skyrocket-3.0/flashable/tools/updatewild.sh";
	if(device.equals("e98x")) return "https://raw.githubusercontent.com/EmmanuelU/wild_kernel_lge_gproj/android-msm-e98x-3.4/flashable/tools/updatewild.sh";
	if(device.equals("f240x")) return "https://raw.githubusercontent.com/EmmanuelU/wild_kernel_lge_gproj/android-msm-f240x-3.4/flashable/tools/updatewild.sh";
	return "";
    }

    public static boolean isWild(){
	propversion = Utils.CMD(false, "getprop ro.wild.version");
	propversiondate = Utils.CMD(false, "getprop ro.wild.date");
	propdevice = Utils.CMD(false, "getprop ro.wild.device");
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

    private void CheckUpdate(){
	if(!Utils.isNetworkOnline(getActivity())){
		Utils.toast(getActivity(), getString(R.string.msg_no_net));
	} else if(WildData.fetchedlatestversion && !WildData.islatestversion){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WildData.latestversiondl));
		startActivity(browserIntent);
	}
	else if(!WildData.fetchedlatestversion){
		if(WildInit(Utils.fetchTextFile(propotalink))){
			if(WildData.latestversionstamp > WildData.versionstamp && !WildData.latestversion.equals(propversion)) WildData.islatestversion = false;
			else WildData.islatestversion = true;
			if(WildData.islatestversion){
				mUpdate.setText(getString(R.string.msg_latest_update));
				mUpdate.setEnabled(false);
			} else {
				mUpdate.setText(getString(R.string.msg_update_available));
				mLatVersion.setText(WildData.latestversion + " - " + WildData.latestversionreldate);
				mLatVersion.setVisibility(View.VISIBLE);
			}
			WildData.fetchedlatestversion = true;
		}
	}
    }

    private void CheckAppUpdate(){
	if(!Utils.isNetworkOnline(getActivity())){
		Utils.toast(getActivity(), getString(R.string.msg_no_net));
	} else if(TameData.fetchedlatestversion && !TameData.islatestversion){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TameData.latestversiondl));
		startActivity(browserIntent);
	}
	else if(!TameData.fetchedlatestversion) {
		mAppUpdate.setText(getActivity().getString(R.string.msg_wait));
		if(TameInit(Utils.fetchTextFile(LINK_APP_UPDATE))){
			if(TameData.latestversionstamp > TameData.versionstamp) TameData.islatestversion = false;
			else TameData.islatestversion = true;
			if(TameData.islatestversion){
				mAppUpdate.setText(getString(R.string.msg_latest_update));
				mAppUpdate.setEnabled(false);
			} else {
				mAppUpdate.setText(getString(R.string.msg_update_available));
				mAppLatVersion.setText(TameData.latestversion + " - " + TameData.latestversionreldate);
				mAppLatVersion.setVisibility(View.VISIBLE);
			}
			TameData.fetchedlatestversion = true;
		}
	}
    }

    private boolean WildInit(String script){
	if(Utils.isStringEmpty(script)) return false;

	Utils.writeFile(FILE_UPDATE_DATA, script);
	Utils.CMD(false, "chmod +x " + FILE_UPDATE_DATA);
	WildData.device = propdevice;
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
