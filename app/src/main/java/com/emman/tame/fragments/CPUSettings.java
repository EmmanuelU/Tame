package com.emman.tame.fragments;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
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
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.emman.tame.MainActivity;
import com.emman.tame.R;
import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

//Major Thanks to CM Team for references

public class CPUSettings extends PreferenceFragment 
		implements Resources, Preference.OnPreferenceChangeListener {

    private Preference mCpuBoost;
    private Preference mCpuBoostV2;
    private Preference mCurFreq;
    private ListPreference mSchedMC;
    private ListPreference mVDD;
    private Preference mSMPDialog;

    String mVDDLevel;

    PreferenceScreen prefSet;

    private SharedPreferences mPreferences;

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
			String cpu0;
			cpu0 = Utils.readOneLine(FREQ_CUR_FILE);
			cpu0 = String.format("%s", Utils.toMHz(cpu0));
			if (cpu0 != null) mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, cpu0));
		}
            } catch (InterruptedException e) {
            }
        }
    };

    private CurCPUThread mCurCPUThread = new CurCPUThread();

    private Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
		try {
			String freq = ((String) msg.obj);
			mCurFreq.setSummary(getString(R.string.msg_primary) + LINE_SPACE + getString(R.string.msg_core) + ": " + freq);
            } catch (Exception e) {}
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	if(!Utils.fileIsReadable(FREQ_MIN_FILE) || !Utils.fileIsReadable(FREQ_MAX_FILE)){
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(LINK_WK_CPU_PATCH));
		Utils.notification(getActivity(), NotificationID.CPUPERM, intent, getString(R.string.msg_kernelbug1));
	}

	addPreferencesFromResource(R.xml.cpu_settings);
	prefSet = getPreferenceScreen();

	mPreferences = PreferenceManager
	    .getDefaultSharedPreferences(getActivity());
	
	updateDependencies();

	mSchedMC.setOnPreferenceChangeListener(this);
	mVDD.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
	final String newValue;
	newValue = (String) value;
	String fname = "";

	if (newValue != null) {
		
		if (preference == mSchedMC){
			fname = SCHED_MC_FILE;
			Utils.toast(getActivity(), getString(R.string.msg_reboot));
		}
		else if (preference == mVDD){
			String undovdd = mVDDLevel;
			fname = VDD_LEVELS_FILE;

			if((undovdd.substring(0, 1)).equals("+")) undovdd = "-" + undovdd.substring(1);
			else if((undovdd.substring(0, 1)).equals("-")) undovdd = "+" + undovdd.substring(1);
			else undovdd = "";
			mVDDLevel = newValue;
			Utils.writeSYSValue(fname, undovdd);
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

    private void updateDependencies(){
	if(!initiateData()) return;

	if(!Utils.fileExists(CPU_BOOST_INPUT_FREQ_FILE)){
		mCpuBoost.setEnabled(false);
	}
	if(!Utils.fileExists(CPU_BOOST_INPUT_TOGGLE)){
		prefSet.removePreference(mCpuBoostV2);
	} else prefSet.removePreference(mCpuBoost);
	
	if(!Utils.fileExists(SCHED_MC_FILE)) mSchedMC.setEnabled(false);
	if(!Utils.fileExists(VDD_LEVELS_FILE)) mVDD.setEnabled(false);
	if(!Utils.fileExists(FILE_MPDEC_TOGGLE) && !Utils.fileExists(CPU_TOGGLE)){
		mSMPDialog.setEnabled(false);
		mSMPDialog.setSummary(getString(R.string.msg_no_smp));
	}
	
    }

    private boolean initiateData(){

	mCpuBoost = (DialogPreference) prefSet.findPreference("cpu_boost");
	mCpuBoostV2 = (DialogPreference) prefSet.findPreference("cpu_boostv2");
	mCurFreq = (Preference) prefSet.findPreference("cur_freq");
	mSchedMC = (ListPreference) prefSet.findPreference("sched_mc");
	mVDD = (ListPreference) prefSet.findPreference("vdd");
	mSMPDialog = findPreference("smpdialog");

	mCurCPUThread.start();

	mSchedMC.setValue(Utils.readOneLine(SCHED_MC_FILE));

	mVDDLevel = mPreferences.getString(SAVED_VDD_LEVELS, "0");

	mVDD.setValue(mVDDLevel);
	if(mVDDLevel.equals("0")) mVDD.setSummary(getString(R.string.msg_default_volt));
	else mVDD.setSummary(getString(R.string.msg_volt) + ": " + (mVDDLevel.substring(0, 1)) + (Integer.parseInt(mVDDLevel.substring(1)) / 1000) + "mV");

	return true;

    }

    private void CPUupdate(){
	if(mVDDLevel.equals("0")) mVDD.setSummary(getString(R.string.msg_default_volt));
	else mVDD.setSummary(getString(R.string.msg_volt) + ": " + (mVDDLevel.substring(0, 1)) + (Integer.parseInt(mVDDLevel.substring(1)) / 1000) + "mV");
    }

    private void setData(){
	updateSharedPrefs(mPreferences, SAVED_SCHED_MC, Utils.readOneLine(SCHED_MC_FILE));
	updateSharedPrefs(mPreferences, SAVED_VDD_LEVELS, mVDDLevel);
	
	CPUupdate();
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(SCHED_MC_FILE, preferences.getString(SAVED_SCHED_MC, "0"));
	Utils.SetSOBValue(VDD_LEVELS_FILE, preferences.getString(SAVED_VDD_LEVELS, "0"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

}
