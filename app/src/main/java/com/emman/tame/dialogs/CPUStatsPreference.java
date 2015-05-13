package com.emman.tame.dialogs;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.emman.tame.R;
import com.emman.tame.fragments.CPUSettings;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class CPUStatsPreference extends DialogPreference
		implements Resources {

    private LinearLayout mCpuStatsGroup;

    private LayoutInflater mInflater;

    private View mView;


    private TextView mCpuFreqs;
    private TextView mGpuFreq;
    private TextView mKernInfo;

    private String mKernelVersion;

    private View mSleepView;
    private TextView mSleepFreq;
    private TextView mSleepFreqInfo;
    private SeekBar mSleepmBar;
    private TextView mSleepBarText;

    private View mRareView;
    private TextView mRareFreq;
    private TextView mRareFreqInfo;
    private SeekBar mRaremBar;
    private TextView mRareBarText;
    private TextView mRarelyUsed;

    private TextView mTotalTimeText;
    private View mOtherStatsView;


    private SharedPreferences mPreferences;

    class FrequencyStat {
	View mView;
	SeekBar mBar;
	TextView mBarText;
	TextView mFreq;
	TextView mFreqInfo;
	boolean enabled;
    }

    private final String[] mCpuFreqList = Utils.readOneLine(FREQ_LIST_FILE).split("\\s+");
    private FrequencyStat mFreqs[] = new FrequencyStat[mCpuFreqList.length];
    private String mFreqTimeList[] = new String[mCpuFreqList.length];

    public CPUStatsPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.cpustatsdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);

	mView = view;
	mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	mCpuFreqs = (TextView) mView.findViewById(R.id.cpufreqs);
	mGpuFreq = (TextView) mView.findViewById(R.id.gpufreq);
	mKernInfo = (TextView) mView.findViewById(R.id.kerninfo);
	mCpuStatsGroup = (LinearLayout) mView.findViewById(R.id.cpu_stats_group);

	mKernInfo.setText("\n" + Utils.readOneLine(KERNEL_BUILD_VERSION) + "\n\n" + Utils.readFile(KERNEL_INFO));

	mSleepView = mInflater.inflate(R.layout.freqstat, null);
	mSleepFreq = (TextView) mSleepView.findViewById(R.id.freq);
	mSleepFreqInfo = (TextView) mSleepView.findViewById(R.id.freqinfo);
	mSleepmBar = (SeekBar) mSleepView.findViewById(R.id.bar);
	mSleepBarText = (TextView) mSleepView.findViewById(R.id.bartext);

	mCpuStatsGroup.addView(mSleepView);


	mOtherStatsView = mInflater.inflate(R.layout.freqstatother, null);

	mRareView = mInflater.inflate(R.layout.freqstat, null);
	mRareFreq = (TextView) mRareView.findViewById(R.id.freq);
	mRareFreqInfo = (TextView) mRareView.findViewById(R.id.freqinfo);
	mRaremBar = (SeekBar) mRareView.findViewById(R.id.bar);
	mRareBarText = (TextView) mRareView.findViewById(R.id.bartext);
	mRarelyUsed = (TextView) mOtherStatsView.findViewById(R.id.notused);

	mTotalTimeText = (TextView) mOtherStatsView.findViewById(R.id.totaltime);

	mCpuStatsGroup.addView(mRareView);

	for(int i = 0; i < mCpuFreqList.length;){
		mFreqs[i] = new FrequencyStat();
		mFreqs[i].mView = mInflater.inflate(R.layout.freqstat, null);
		mFreqs[i].mFreq = (TextView) mFreqs[i].mView.findViewById(R.id.freq);
		mFreqs[i].mFreqInfo = (TextView) mFreqs[i].mView.findViewById(R.id.freqinfo);
		mFreqs[i].mBar = (SeekBar) mFreqs[i].mView.findViewById(R.id.bar);
		mFreqs[i].mBarText = (TextView) mFreqs[i].mView.findViewById(R.id.bartext);
		mFreqs[i].enabled = true;

		mCpuStatsGroup.addView(mFreqs[i].mView);
		i++;
	}

	mCpuStatsGroup.addView(mOtherStatsView);

	updateTimesInState();

	if(mCurCPUThread.isAlive()){
	    mCurCPUThread.interrupt(); 
	}
	mCurCPUThread = new CurCPUThread();
	mCurCPUThread.start();
    }

    private void updateTimesInState() {
	String rarelyUsedFreqs = "";
	String neverUsedFreqs = "";

	String mTimeList[] = Utils.readFile(CPU_TIME_IN_STATE).split(System.getProperty("line.separator"));

	int mRareTime = 0; 
	int mTotalTime = (int) SystemClock.elapsedRealtime();
	long mSleepTime = mTotalTime - SystemClock.uptimeMillis();

	for(int i = 0; i < mCpuFreqList.length;){
		mFreqTimeList[i] = mTimeList[i].split(" ", 2)[1];
		i++;
	}

	String time;
	mSleepmBar.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    });
	mSleepmBar.setMax(mTotalTime);
	mSleepmBar.setProgress((int) mSleepTime);

	time = String.format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(mSleepTime), TimeUnit.MILLISECONDS.toMinutes(mSleepTime) -  TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mSleepTime)), TimeUnit.MILLISECONDS.toSeconds(mSleepTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mSleepTime)));

	mSleepFreq.setText(getContext().getString(R.string.item_deep_sleep));
	mSleepFreqInfo.setText(time);
	mSleepBarText.setText(Integer.toString(((int) mSleepTime * 100) / mTotalTime) + "%" + NEW_LINE);

	for(int i = 0; i < mCpuFreqList.length;){

			/* 
				insane mathz that took me a while, just to get simpliefied
				long hour = (((Integer.parseInt(mFreqTimeList[i]) / 100) / 60) / 60);
				long minute = (((Integer.parseInt(mFreqTimeList[i]) / 100) / 60) - (hour * 60));
				long second = (Integer.parseInt(mFreqTimeList[i]) / 100) - (minute * 60);
				String time = String.format("%02d:%02d:%02d", hour, minute, second);
			*/
			long timeMS = Integer.parseInt(mFreqTimeList[i]) * 10;

			mFreqs[i].mBar.setOnTouchListener(new View.OnTouchListener() {
			    @Override
			    public boolean onTouch(View v, MotionEvent event) {
				return true;
			    }
			});
			mFreqs[i].mBar.setMax(mTotalTime);
			mFreqs[i].mBar.setProgress((int) timeMS);

			if(timeMS == 0) {
				neverUsedFreqs = (Utils.isStringEmpty(neverUsedFreqs) ? Utils.toMHz(mCpuFreqList[i]) + LINE_SPACE : neverUsedFreqs + "," + LINE_SPACE + Utils.toMHz(mCpuFreqList[i]));
				if(mFreqs[i].enabled) mCpuStatsGroup.removeView(mFreqs[i].mView);
				mFreqs[i].enabled = false;
			} else if(((timeMS * 100) / mTotalTime) < 1) {
				rarelyUsedFreqs = (Utils.isStringEmpty(rarelyUsedFreqs) ? Utils.toMHz(mCpuFreqList[i]) + LINE_SPACE : rarelyUsedFreqs + "," + LINE_SPACE + Utils.toMHz(mCpuFreqList[i]));
				mRareTime = mRareTime + Integer.parseInt(mFreqTimeList[i]);
				if(mFreqs[i].enabled) mCpuStatsGroup.removeView(mFreqs[i].mView);
				mFreqs[i].enabled = false;
			} else {
				time = String.format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeMS), TimeUnit.MILLISECONDS.toMinutes(timeMS) -  TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeMS)), TimeUnit.MILLISECONDS.toSeconds(timeMS) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMS)));

				mFreqs[i].mFreq.setText(Utils.toMHz(mCpuFreqList[i]));
				mFreqs[i].mFreqInfo.setText(time);
				mFreqs[i].mBarText.setText(Integer.toString(((int) timeMS * 100) / mTotalTime) + "%" + NEW_LINE);
			}
		i++;
	}

	mRaremBar.setOnTouchListener(new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
		    return true;
		}
        });

	mRaremBar.setMax(mTotalTime);
	mRaremBar.setProgress(mRareTime);
	time = String.format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(mRareTime), TimeUnit.MILLISECONDS.toMinutes(mRareTime) -  TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mRareTime)), TimeUnit.MILLISECONDS.toSeconds(mRareTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mRareTime)));
	mRareFreq.setText(getContext().getString(R.string.item_freq_trans));
	mRareFreqInfo.setText(time);
	mRareBarText.setText((mRareTime * 100 / mTotalTime) + "%" + NEW_LINE);

	mTotalTimeText.setText(String.format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(mTotalTime), TimeUnit.MILLISECONDS.toMinutes(mTotalTime) -  TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mTotalTime)), TimeUnit.MILLISECONDS.toSeconds(mTotalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mTotalTime))));

	mRarelyUsed.setText(getContext().getString(R.string.item_transy_freq) + NEW_LINE + rarelyUsedFreqs + NEW_LINE + NEW_LINE + getContext().getString(R.string.item_unused) + NEW_LINE + neverUsedFreqs + NEW_LINE);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);

	mCurCPUThread.interrupt();
	try {
		mCurCPUThread.join();
	} catch (InterruptedException e) {
	}
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
	super.onPrepareDialogBuilder(builder);
	builder.setNegativeButton(null, null);
    }

    private class CurCPUThread extends Thread {

        @Override
        public void run() {
            try {
		while (!this.isInterrupted()) {
			sleep(500);
			String stats = "";
			for(int i = 0; i < Utils.getNumOfCpus();){
				String minfreq, maxfreq, curfreq, governor;
				curfreq = Utils.readOneLine(Utils.toCPU(FREQ_CUR_FILE, i));
				if(Utils.isStringEmpty(curfreq)){
					if(Utils.fileExists(Utils.toCPU(CPU_TOGGLE, i)) && !Utils.stringToBool(Utils.readOneLine(Utils.toCPU(CPU_TOGGLE, i))))
						curfreq = getContext().getString(R.string.item_disabled);
					else
						curfreq = getContext().getString(R.string.item_offline);

					minfreq = getContext().getString(R.string.item_na);
					maxfreq = "";
					governor = "N/A";
				}
				else {
					curfreq = String.format("%s", Utils.toMHz(curfreq));
					minfreq = Utils.toMHz(Utils.readOneLine(Utils.toCPU(FREQ_MIN_FILE, i))).replace("MHz", "-");
					maxfreq = Utils.toMHz(Utils.readOneLine(Utils.toCPU(FREQ_MAX_FILE, i)));
					governor = Utils.readOneLine(Utils.toCPU(GOV_FILE, i));
				}
				stats = stats + getContext().getString(R.string.msg_core) + LINE_SPACE + (i+1) + " (" + minfreq + maxfreq + "): " + curfreq + NEW_LINE + governor + NEW_LINE;
				i++;
			}
			try{
				if (stats != null) mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, stats));
				stats = Utils.toGPUMHz(Utils.readOneLine(GPU_CUR_FREQ_FILE));
				if (stats != null) mCurGPUHandler.sendMessage(mCurGPUHandler.obtainMessage(0, stats));
			} catch (Exception e) {}
		}
            } catch (Exception e) {}
        }
    };

    private CurCPUThread mCurCPUThread = new CurCPUThread();

    private Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
		String stats = ((String) msg.obj);
		mCpuFreqs.setText(stats);
		updateTimesInState();
        }
    };

    private Handler mCurGPUHandler = new Handler() {
        public void handleMessage(Message msg) {
		String freq = ((String) msg.obj);
		mGpuFreq.setText("3D GPU: " + freq);
        }
    };

} 
