package com.emman.tame.fragments;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import java.lang.Runtime;

import com.emman.tame.R;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

//Major Thanks to CM Team for references

public class CPUSettings extends PreferenceFragment 
		implements Resources, Preference.OnPreferenceChangeListener {

    private static final String TAG = "Tame";

    private ListPreference mMinFreq;
    private ListPreference mMaxFreq;
    private Preference mCurFreq;
    private ListPreference mGovernor;
    private ListPreference mIOSched;
    private ListPreference mSchedMC;
    private ListPreference mCeloxUVPanel;
    private CheckBoxPreference mCpuBoost;
    private CheckBoxPreference mCpuGovSync;
    public static DialogPreference mGPUDialog;

    PreferenceScreen prefSet;

    private SharedPreferences mPreferences;

    String[] availableFrequencies;
    String[] availableGovernors;
    String[] frequencies;
    String availableGovernorsLine;
    String availableFrequenciesLine;
    String[] availableIOSchedulers;
    String availableIOSchedulersLine;
    int bropen, brclose;
    String currentIOScheduler;

    private class CurCPUThread extends Thread {
        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
		while (!mInterrupt) {
			sleep(500);
			String cpu0, cpu1;
			cpu0 = Utils.readOneLine(FREQ_CUR_FILE);
			cpu1 = Utils.readOneLine(FREQ_CUR_FILE.replace("cpu0", "cpu1"));
			cpu0 = String.format("%s", Utils.toMHz(cpu0));
			sleep(1);
			if(cpu1.equals("")) cpu1 = "Offline";
			else cpu1 = String.format("%s", Utils.toMHz(cpu1));
			final String curFreq = cpu0 + " " + cpu1;
			if (curFreq != null) mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, curFreq));
		}
            } catch (InterruptedException e) {
            }
        }
    };

    private CurCPUThread mCurCPUThread = new CurCPUThread();

    private Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
		String[] cpu = ((String) msg.obj).split("\\s+");
		mCurFreq.setSummary("Core 1: " + cpu[0] + "		Core 2: " + cpu[1]);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	FragmentManager fragmentManager = getFragmentManager();

		if(!Utils.checkSu()){
			Utils.toast(getActivity(), "Superuser permissions were denied, restarting.");
			Intent intent = getActivity().getIntent();
			getActivity().finish();
			startActivity(intent);
		}

		addPreferencesFromResource(R.xml.cpu_settings);
		prefSet = getPreferenceScreen();

		mPreferences = PreferenceManager
		    .getDefaultSharedPreferences(getActivity());
	
		CPUinit();

		mGovernor.setOnPreferenceChangeListener(this);
		mMinFreq.setOnPreferenceChangeListener(this);
		mMaxFreq.setOnPreferenceChangeListener(this);
		mIOSched.setOnPreferenceChangeListener(this);
		mSchedMC.setOnPreferenceChangeListener(this);
		mCpuBoost.setOnPreferenceChangeListener(this);
		mCeloxUVPanel.setOnPreferenceChangeListener(this);
		mCpuGovSync.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
	final String newValue;
	if(preference == mCpuBoost || preference == mCpuGovSync) newValue = Utils.boolToString((Boolean) value);
	else newValue = (String) value;
	String fname = "";

	if (newValue != null) {
		
		if (preference == mGovernor) fname = GOV_FILE;
		else if (preference == mMinFreq) fname = FREQ_MIN_FILE;
		else if (preference == mMaxFreq) fname = FREQ_MAX_FILE;
		else if (preference == mIOSched) fname = IOSCHED_LIST_FILE;
		else if (preference == mCpuBoost) fname = CPU_BOOST_FILE;
		else if (preference == mCeloxUVPanel) fname = FILE_CELOX_DISPLAY_UV;
		else if (preference == mCpuGovSync) fname = CPU_GOV_SYNC_FILE;
		else if (preference == mSchedMC){
			fname = SCHED_MC_FILE;
			Utils.toast(getActivity(), "Reboot is recommended.");
		}
	
		Utils.writeSYSValue(fname, newValue);
		setData();
		return true;

	}
	return false;
    }

    @Override
    public void onResume() {
	super.onResume();
	CPUupdate();
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	mCurCPUThread.interrupt();
	try {
		mCurCPUThread.join();
	} catch (InterruptedException e) {
	}
    }

    private void CPUinit(){

	availableFrequencies = new String[0];
	availableGovernors = new String[0];

	mGovernor = (ListPreference) prefSet.findPreference("governor");
	mCurFreq = (Preference) prefSet.findPreference("cur_freq");
	mMinFreq = (ListPreference) prefSet.findPreference("min_freq");
	mMaxFreq = (ListPreference) prefSet.findPreference("max_freq");
	mIOSched = (ListPreference) prefSet.findPreference("iosched");
	mSchedMC = (ListPreference) prefSet.findPreference("sched_mc");
	mCpuBoost = (CheckBoxPreference) prefSet.findPreference("cpu_boost");
	mCeloxUVPanel = (ListPreference) prefSet.findPreference("celox_uv_panel");
	mGPUDialog = (DialogPreference) prefSet.findPreference("gpu_dialog");
	mCpuGovSync = (CheckBoxPreference) prefSet.findPreference("cpu_gov_sync");

	if(!Utils.fileExists(SCHED_MC_FILE)) mSchedMC.setEnabled(false);
	if(!Utils.fileExists(CPU_BOOST_FILE)) mCpuBoost.setEnabled(false);
	if(!Utils.fileExists(FILE_CELOX_DISPLAY_UV)) mCeloxUVPanel.setEnabled(false);
	if(!Utils.fileExists(GPU_MAX_FREQ_FILE)) mGPUDialog.setEnabled(false);
	if(!Utils.fileExists(CPU_GOV_SYNC_FILE)) mCpuGovSync.setEnabled(false);
	if(!Utils.fileExists(FREQ_CUR_FILE)){
		mGovernor.setEnabled(false);
		mCurFreq.setEnabled(false);
		mMinFreq.setEnabled(false);
		mMaxFreq.setEnabled(false);
	} else {
		availableFrequenciesLine = Utils.readOneLineSU(FREQ_LIST_FILE);
		availableFrequencies = availableFrequenciesLine.split(" ");
		frequencies = new String[availableFrequencies.length];
		for (int i = 0; i < frequencies.length; i++) frequencies[i] = Utils.toMHz(availableFrequencies[i]);

		availableGovernorsLine = Utils.readOneLineSU(GOV_LIST_FILE);

		availableGovernors = availableGovernorsLine.split(" ");

		mGovernor.setEntryValues(availableGovernors);
		mGovernor.setEntries(availableGovernors);
		mGovernor.setValue(Utils.readOneLineSU(GOV_FILE));
		mGovernor.setSummary(String.format("%S", Utils.readOneLineSU(GOV_FILE)));

		mMaxFreq.setEntryValues(availableFrequencies);
		mMaxFreq.setEntries(frequencies);
		mMaxFreq.setValue(Utils.readOneLineSU(FREQ_MAX_FILE));
		mMaxFreq.setSummary(String.format("%s", Utils.toMHz(Utils.readOneLineSU(FREQ_MAX_FILE))));

		mMinFreq.setEntryValues(availableFrequencies);
		mMinFreq.setEntries(frequencies);
		mMinFreq.setValue(Utils.readOneLineSU(FREQ_MIN_FILE));
		mMinFreq.setSummary(String.format("%s", Utils.toMHz(Utils.readOneLineSU(FREQ_MIN_FILE))));

		availableIOSchedulersLine = Utils.readOneLineSU(IOSCHED_LIST_FILE);
		availableIOSchedulers = availableIOSchedulersLine.replace("[", "").replace("]", "").split(" ");
		bropen = availableIOSchedulersLine.indexOf("[");
		brclose = availableIOSchedulersLine.lastIndexOf("]");
		if (bropen >= 0 && brclose >= 0) currentIOScheduler = availableIOSchedulersLine.substring(bropen + 1, brclose);
		mCurFreq.setSummary("Core 1: " + Utils.toMHz(Utils.readOneLineSU(FREQ_CUR_FILE)));
		mCurCPUThread.start();

	}

	mIOSched.setEntryValues(availableIOSchedulers);
	mIOSched.setEntries(availableIOSchedulers);
	if (currentIOScheduler != null) mIOSched.setValue(currentIOScheduler);
	mIOSched.setSummary(String.format("%S", currentIOScheduler));

	mSchedMC.setValue(Utils.readOneLineSU(SCHED_MC_FILE));

	mCeloxUVPanel.setValue(Utils.readOneLine(FILE_CELOX_DISPLAY_UV));

	if(mGPUDialog.isEnabled()) mGPUDialog.setSummary(String.format("%s", Utils.toGPUMHz(Utils.readOneLine(GPU_MAX_FREQ_FILE))));

	mCpuBoost.setChecked(Utils.stringToBool(Utils.readOneLine(CPU_BOOST_FILE)));

	mCpuGovSync.setChecked(Utils.stringToBool(Utils.readOneLine(CPU_GOV_SYNC_FILE)));

    }

    private void CPUupdate(){
	if(Utils.fileExists(FREQ_CUR_FILE)){
		availableIOSchedulersLine = Utils.readOneLineSU(IOSCHED_LIST_FILE);
		availableIOSchedulers = availableIOSchedulersLine.replace("[", "").replace("]", "").split(" ");
		bropen = availableIOSchedulersLine.indexOf("[");
		brclose = availableIOSchedulersLine.lastIndexOf("]");
		if (bropen >= 0 && brclose >= 0) currentIOScheduler = availableIOSchedulersLine.substring(bropen + 1, brclose);

		mGovernor.setSummary(String.format("%S", Utils.readOneLineSU(GOV_FILE)));
		mMaxFreq.setSummary(String.format("%s", Utils.toMHz(Utils.readOneLineSU(FREQ_MAX_FILE))));
		mMinFreq.setSummary(String.format("%s", Utils.toMHz(Utils.readOneLineSU(FREQ_MIN_FILE))));
		mIOSched.setSummary(String.format("%S", currentIOScheduler));
	}
	GPUupdate();
    }

    public static void GPUupdate(){
	if(mGPUDialog.isEnabled()) mGPUDialog.setSummary(String.format("%s", Utils.toGPUMHz(Utils.readOneLine(GPU_MAX_FREQ_FILE))));
    }

    private void setData(){
	updateSharedPrefs(mPreferences, SAVED_MIN_FREQ, Utils.readOneLineSU(FREQ_MIN_FILE));
	updateSharedPrefs(mPreferences, SAVED_MAX_FREQ, Utils.readOneLineSU(FREQ_MAX_FILE));
	updateSharedPrefs(mPreferences, SAVED_GOV, Utils.readOneLineSU(GOV_FILE));
	updateSharedPrefs(mPreferences, SAVED_IOSCHED, Utils.readOneLineSU(IOSCHED_LIST_FILE));
	updateSharedPrefs(mPreferences, SAVED_SCHED_MC, Utils.readOneLineSU(SCHED_MC_FILE));
	updateSharedPrefs(mPreferences, SAVED_CPU_BOOST, Utils.readOneLine(CPU_BOOST_FILE));
	updateSharedPrefs(mPreferences, SAVED_CELOX_DISPLAY_UV, Utils.readOneLine(FILE_CELOX_DISPLAY_UV));
	CPUupdate();
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(FREQ_MIN_FILE, preferences.getString(SAVED_MIN_FREQ, "96000"));
	Utils.SetSOBValue(FREQ_MAX_FILE, preferences.getString(SAVED_MAX_FREQ, "1512000"));
	Utils.SetSOBValue(GOV_FILE, preferences.getString(SAVED_GOV, "ondemand"));
	Utils.SetSOBValue(IOSCHED_LIST_FILE, preferences.getString(SAVED_IOSCHED, "noop deadline row cfq bfq [sio] vr zen fifo"));
	Utils.SetSOBValue(SCHED_MC_FILE, preferences.getString(SAVED_SCHED_MC, "0"));
	Utils.SetSOBValue(CPU_BOOST_FILE, preferences.getString(SAVED_CPU_BOOST, "1"));
	Utils.SetSOBValue(FILE_CELOX_DISPLAY_UV, preferences.getString(SAVED_CELOX_DISPLAY_UV, "0"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

}