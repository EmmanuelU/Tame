package com.emman.tame;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.widget.TextView;
import android.widget.Button;

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

public class AboutTame extends Fragment {

    class Wild {
	String latestversion;
    }

    private View mView;
    static String propversion;
    static String propversiondate;
    private Button mUpdate;
    private TextView mTameLogo;
    private TextView mVersion;
    private TextView mLatVersion;
    Animation fadein = new AlphaAnimation(0.0f, 1.0f);
    Animation fadeout = new AlphaAnimation(1.0f, 0.0f);
    ProgressDialog mCheckUpdateDialog;
    private Wild WildData;

    private static final String FILE_UPDATE_DATA = "/sdcard/updatewild.sh";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	WildData = new Wild();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	mView = inflater.inflate(R.layout.about_tame, container, false);
	mUpdate = (Button) mView.findViewById(R.id.update_button);
	mTameLogo = (TextView) mView.findViewById(R.id.tame_logo);
	mVersion = (TextView) mView.findViewById(R.id.versionheader);
	mLatVersion = (TextView) mView.findViewById(R.id.latversionheader);
        mUpdate.setOnClickListener(new View.OnClickListener() {
		public void onClick(View view) {
			CheckUpdate();
		}
	});
	
	TameLogoAnim();
	mTameLogo.startAnimation(fadeout);
	setversiondata();
        return mView;
    }

    public static boolean isWild(){
	
	propversion = Utils.CMD("getprop ro.wild.version", false);
	propversiondate = Utils.CMD("getprop ro.wild.date", false);
	return (!propversion.isEmpty() || !propversiondate.isEmpty());
	
    }

    private void setversiondata(){
	mLatVersion.setVisibility(View.GONE);
	if(isWild()){
		mVersion.setText(mVersion.getText().toString() + " " + propversion);
	}
	else{
		mVersion.setVisibility(View.GONE);
		mUpdate.setVisibility(View.GONE);
	}
    }

    private boolean CheckUpdate(){
	mCheckUpdateDialog = new ProgressDialog(getActivity());
	mCheckUpdateDialog.setMessage("Checking for updates");
	mCheckUpdateDialog.setIndeterminate(true);
	mCheckUpdateDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	mCheckUpdateDialog.setCancelable(true);
	final DownloadTask downloadTask = new DownloadTask(getActivity(), FILE_UPDATE_DATA, mCheckUpdateDialog, true);
	downloadTask.execute("https://dl.dropboxusercontent.com/u/40873980/updatewild.sh");
	mCheckUpdateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
	    @Override
	    public void onCancel(DialogInterface dialog) {
		downloadTask.cancel(true);
	    }
	});

	mCheckUpdateDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
	    @Override
	    public void onDismiss(DialogInterface dialog) {
		if(Utils.fileExists(FILE_UPDATE_DATA)){
			Utils.CMD("chmod +x " + FILE_UPDATE_DATA, false);
			WildData.latestversion = Utils.CMD("bash " + FILE_UPDATE_DATA + " latestversion", false);
			//Utils.toast(getActivity(), );
			Utils.CMD("rm -rf " + FILE_UPDATE_DATA, false);
			mLatVersion.setText(mLatVersion.getText().toString() + " " + WildData.latestversion);
			mLatVersion.setVisibility(View.VISIBLE);
		}
	    }
	});
	
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
