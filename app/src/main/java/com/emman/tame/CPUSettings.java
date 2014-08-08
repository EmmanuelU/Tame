package com.emman.tame;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

//Major Thanks to CM Team for references

public class CPUSettings extends PreferenceFragment 
		implements Resources {

    private static final String TAG = "Tame";

    private ListPreference mMinFreq;
    private ListPreference mMaxFreq;
    private Preference mCurFreq;
    private ListPreference mGovernor;

    PreferenceScreen prefSet;

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
                    final String curFreq = Utils.readOneLine(FREQ_CUR_FILE);
                    if (curFreq != null)
                        mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, curFreq));
                }
            } catch (InterruptedException e) {
            }
        }
    };

    private CurCPUThread mCurCPUThread = new CurCPUThread();

    private Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
            mCurFreq.setSummary(Utils.toMHz((String) msg.obj));
            CPUupdate();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	addPreferencesFromResource(R.xml.cpu_settings);
	prefSet = getPreferenceScreen();
	
	CPUinit();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

	String key = preference.getKey();

	Log.w(TAG, "key: " + key);
	return true;
    }

    @Override
    public void onResume() {
	super.onResume();
	CPUupdate();
    }

    private boolean CPUinit(){
	if (!Utils.checkSu()) return Utils.checkSu();
	String[] availableFrequencies = new String[0];
	String[] availableGovernors = new String[0];
	String[] frequencies;
	String availableGovernorsLine;
	String availableFrequenciesLine;

	mGovernor = (ListPreference) prefSet.findPreference("governor");
	mCurFreq = (Preference) prefSet.findPreference("cur_freq");
	mMinFreq = (ListPreference) prefSet.findPreference("min_freq");
	mMaxFreq = (ListPreference) prefSet.findPreference("max_freq");

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

	mCurFreq.setSummary(Utils.toMHz(Utils.readOneLine(FREQ_CUR_FILE)));
	mCurCPUThread.start();

	return true;
    }

    private void CPUupdate(){

    }

}
