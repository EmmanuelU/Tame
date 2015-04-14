package com.emman.tame.utils;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.content.DialogInterface;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.os.PowerManager;
import android.os.StrictMode;
import android.widget.Toast;
import android.util.Log;

import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

public class BackgroundTask extends AsyncTask<String, Integer, String> {

    public static interface tasks {
	void doInBackground();
    }

    public static interface onTaskCompleted {
	void onCompleted();
    }

    private Context context;
    private PowerManager.WakeLock mWakeLock;

    private ArrayList<onTaskCompleted> onTaskCompletedObservers = new ArrayList<onTaskCompleted>();
    private ArrayList<tasks> backgroundTaskObservers = new ArrayList<tasks>();
    private boolean taskCompleted = false;


    public BackgroundTask(Context context) {
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

	StrictMode.setThreadPolicy(policy);
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user 
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
             getClass().getName());
        mWakeLock.acquire();
    }

    public Context getContext() {
	return context;
    }

    public boolean isTaskCompleted() {
	return taskCompleted;
    }

    public void setBackgroundTask(tasks observer) {
	backgroundTaskObservers.add(observer);
    }

    public void setOnTaskCompletedListener(onTaskCompleted observer) {
	onTaskCompletedObservers.add(observer);
    }

    @Override
    protected void onPostExecute(String result) {
	taskCompleted = true;
	for(onTaskCompleted observer : onTaskCompletedObservers){
		observer.onCompleted();
	}
	mWakeLock.release();
    }

    @Override
    protected String doInBackground(String... stub) {
	for(tasks observer : backgroundTaskObservers){
		observer.doInBackground();
	}
        return null;
    }
}
