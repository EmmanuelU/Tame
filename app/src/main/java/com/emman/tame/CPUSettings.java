package com.emman.tame;

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

//Major Thanks to CM Team for references

public class CPUSettings extends PreferenceFragment 
		implements Resources, Preference.OnPreferenceChangeListener {

    private static final String TAG = "Tame";

    private ListPreference mMinFreq;
    private ListPreference mMaxFreq;
    private Preference mCurFreq;
    private ListPreference mGovernor;
    private ListPreference mIOSched;

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
		CPUupdate();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	FragmentManager fragmentManager = getFragmentManager();

		if(!Utils.checkSu()){
			Utils.toast(getActivity(), "Superuser permissions were denied, retarting.");
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
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {

        final String newValue = (String) value;
        String fname = "";

	if (newValue != null) {
		
		if (preference == mGovernor) fname = GOV_FILE;
		else if (preference == mMinFreq) fname = FREQ_MIN_FILE;
		else if (preference == mMaxFreq) fname = FREQ_MAX_FILE;
		else if (preference == mIOSched) fname = IOSCHED_LIST_FILE;

		if (Utils.fileExists(fname)) Utils.writeValueSU(fname, newValue);
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

	availableFrequenciesLine = Utils.readOneLine(FREQ_LIST_FILE);
	availableFrequencies = availableFrequenciesLine.split(" ");
	frequencies = new String[availableFrequencies.length];
	for (int i = 0; i < frequencies.length; i++) frequencies[i] = Utils.toMHz(availableFrequencies[i]);

	availableGovernorsLine = Utils.readOneLine(GOV_LIST_FILE);

	availableGovernors = availableGovernorsLine.split(" ");

	mGovernor.setEntryValues(availableGovernors);
	mGovernor.setEntries(availableGovernors);
	mGovernor.setValue(Utils.readOneLine(GOV_FILE));
	mGovernor.setSummary(String.format("%S", Utils.readOneLine(GOV_FILE)));

	mMaxFreq.setEntryValues(availableFrequencies);
	mMaxFreq.setEntries(frequencies);
	mMaxFreq.setValue(Utils.readOneLine(FREQ_MAX_FILE));
	mMaxFreq.setSummary(String.format("%s", Utils.toMHz(Utils.readOneLine(FREQ_MAX_FILE))));

	mMinFreq.setEntryValues(availableFrequencies);
	mMinFreq.setEntries(frequencies);
	mMinFreq.setValue(Utils.readOneLine(FREQ_MIN_FILE));
	mMinFreq.setSummary(String.format("%s", Utils.toMHz(Utils.readOneLine(FREQ_MIN_FILE))));

	availableIOSchedulersLine = Utils.readOneLine(IOSCHED_LIST_FILE);
	availableIOSchedulers = availableIOSchedulersLine.replace("[", "").replace("]", "").split(" ");
	bropen = availableIOSchedulersLine.indexOf("[");
	brclose = availableIOSchedulersLine.lastIndexOf("]");
	if (bropen >= 0 && brclose >= 0) currentIOScheduler = availableIOSchedulersLine.substring(bropen + 1, brclose);

	mIOSched.setEntryValues(availableIOSchedulers);
	mIOSched.setEntries(availableIOSchedulers);
	if (currentIOScheduler != null) mIOSched.setValue(currentIOScheduler);
	mIOSched.setSummary(String.format("%S", currentIOScheduler));
	mIOSched.setOnPreferenceChangeListener(this);

	mCurFreq.setSummary("Core 1: " + Utils.toMHz(Utils.readOneLine(FREQ_CUR_FILE)));
	mCurCPUThread.start();

    }

    private void CPUupdate(){

	availableIOSchedulersLine = Utils.readOneLine(IOSCHED_LIST_FILE);
	availableIOSchedulers = availableIOSchedulersLine.replace("[", "").replace("]", "").split(" ");
	bropen = availableIOSchedulersLine.indexOf("[");
	brclose = availableIOSchedulersLine.lastIndexOf("]");
	if (bropen >= 0 && brclose >= 0) currentIOScheduler = availableIOSchedulersLine.substring(bropen + 1, brclose);

	mGovernor.setSummary(String.format("%S", Utils.readOneLine(GOV_FILE)));
	mMaxFreq.setSummary(String.format("%s", Utils.toMHz(Utils.readOneLine(FREQ_MAX_FILE))));
	mMinFreq.setSummary(String.format("%s", Utils.toMHz(Utils.readOneLine(FREQ_MIN_FILE))));
	mIOSched.setSummary(String.format("%S", currentIOScheduler));

    }

    private void setData(){
	updateSharedPrefs(mPreferences, SAVED_MIN_FREQ, Utils.readOneLine(FREQ_MIN_FILE));
	updateSharedPrefs(mPreferences, SAVED_MAX_FREQ, Utils.readOneLine(FREQ_MAX_FILE));
	updateSharedPrefs(mPreferences, SAVED_GOV, Utils.readOneLine(GOV_FILE));
	updateSharedPrefs(mPreferences, SAVED_IOSCHED, Utils.readOneLine(IOSCHED_LIST_FILE));

	CPUupdate();
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.writeValueSU(FREQ_MIN_FILE, preferences.getString(SAVED_MIN_FREQ, "96000"));
	Utils.writeValueSU(FREQ_MAX_FILE, preferences.getString(SAVED_MAX_FREQ, "1512000"));
	Utils.writeValueSU(GOV_FILE, preferences.getString(SAVED_GOV, "ondemand"));
	Utils.writeValueSU(IOSCHED_LIST_FILE, preferences.getString(SAVED_IOSCHED, "noop deadline row cfq bfq [sio] vr zen fifo"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

}
