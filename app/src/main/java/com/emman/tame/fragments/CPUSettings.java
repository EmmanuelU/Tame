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
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.emman.tame.R;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

//Major Thanks to CM Team for references

public class CPUSettings extends PreferenceFragment 
		implements Resources, Preference.OnPreferenceChangeListener {

    private static final String TAG = "Tame";

    private Preference mCurFreq;
    private ListPreference mSchedMC;
    private ListPreference mCeloxUVPanel;
    private ListPreference mVDD;
    private CheckBoxPreference mCpuBoost;
    private CheckBoxPreference mCpuGovSync;
    public static DialogPreference mGPUDialog;

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
		String freq = ((String) msg.obj);
		mCurFreq.setSummary("Primary Core: " + freq);
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

	mSchedMC.setOnPreferenceChangeListener(this);
	mCpuBoost.setOnPreferenceChangeListener(this);
	mCeloxUVPanel.setOnPreferenceChangeListener(this);
	mCpuGovSync.setOnPreferenceChangeListener(this);
	mVDD.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
	final String newValue;
	if(preference == mCpuBoost || preference == mCpuGovSync) newValue = Utils.boolToString((Boolean) value);
	else newValue = (String) value;
	String fname = "";

	if (newValue != null) {
		
		if (preference == mCpuBoost) fname = CPU_BOOST_FILE;
		else if (preference == mCeloxUVPanel) fname = FILE_CELOX_DISPLAY_UV;
		else if (preference == mCpuGovSync) fname = CPU_GOV_SYNC_FILE;
		else if (preference == mSchedMC){
			fname = SCHED_MC_FILE;
			Utils.toast(getActivity(), "Reboot is recommended.");
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

    private void CPUinit(){

	mCurFreq = (Preference) prefSet.findPreference("cur_freq");
	mSchedMC = (ListPreference) prefSet.findPreference("sched_mc");
	mCpuBoost = (CheckBoxPreference) prefSet.findPreference("cpu_boost");
	mCeloxUVPanel = (ListPreference) prefSet.findPreference("celox_uv_panel");
	mGPUDialog = (DialogPreference) prefSet.findPreference("gpu_dialog");
	mCpuGovSync = (CheckBoxPreference) prefSet.findPreference("cpu_gov_sync");
	mVDD = (ListPreference) prefSet.findPreference("vdd");

	if(!Utils.fileExists(SCHED_MC_FILE)) mSchedMC.setEnabled(false);
	if(!Utils.fileExists(CPU_BOOST_FILE)) mCpuBoost.setEnabled(false);
	if(!Utils.fileExists(FILE_CELOX_DISPLAY_UV)) mCeloxUVPanel.setEnabled(false);
	if(!Utils.fileExists(GPU_MAX_FREQ_FILE)) mGPUDialog.setEnabled(false);
	if(!Utils.fileExists(CPU_GOV_SYNC_FILE)) mCpuGovSync.setEnabled(false);
	if(!Utils.fileExists(VDD_LEVELS_FILE)) mVDD.setEnabled(false);

	mCurFreq.setSummary("Core 1: " + Utils.toMHz(Utils.readOneLine(FREQ_CUR_FILE)));
	mCurCPUThread.start();

	mSchedMC.setValue(Utils.readOneLine(SCHED_MC_FILE));

	mCeloxUVPanel.setValue(Utils.readOneLine(FILE_CELOX_DISPLAY_UV));

	if(mGPUDialog.isEnabled()) mGPUDialog.setSummary(String.format("%s", Utils.toGPUMHz(Utils.readOneLine(GPU_MAX_FREQ_FILE))));

	mCpuBoost.setChecked(Utils.stringToBool(Utils.readOneLine(CPU_BOOST_FILE)));

	mCpuGovSync.setChecked(Utils.stringToBool(Utils.readOneLine(CPU_GOV_SYNC_FILE)));

	mVDDLevel = mPreferences.getString(SAVED_VDD_LEVELS, "0");

	mVDD.setValue(mVDDLevel);
	if(mVDDLevel.equals("0")) mVDD.setSummary("Default Voltage");
	else mVDD.setSummary("Voltage: " + (mVDDLevel.substring(0, 1)) + (Integer.parseInt(mVDDLevel.substring(1)) / 1000) + "mV");

    }

    private void CPUupdate(){
	if(mVDDLevel.equals("0")) mVDD.setSummary("Default Voltage");
	else mVDD.setSummary("Voltage: " + (mVDDLevel.substring(0, 1)) + (Integer.parseInt(mVDDLevel.substring(1)) / 1000) + "mV");
	GPUupdate();
    }

    public static void GPUupdate(){
	if(mGPUDialog.isEnabled()) mGPUDialog.setSummary(String.format("%s", Utils.toGPUMHz(Utils.readOneLine(GPU_MAX_FREQ_FILE))));
    }

    private void setData(){
	updateSharedPrefs(mPreferences, SAVED_SCHED_MC, Utils.readOneLine(SCHED_MC_FILE));
	updateSharedPrefs(mPreferences, SAVED_CPU_BOOST, Utils.readOneLine(CPU_BOOST_FILE));
	updateSharedPrefs(mPreferences, SAVED_CPU_GOV_SYNC, Utils.readOneLine(CPU_GOV_SYNC_FILE));
	updateSharedPrefs(mPreferences, SAVED_CELOX_DISPLAY_UV, Utils.readOneLine(FILE_CELOX_DISPLAY_UV));
	updateSharedPrefs(mPreferences, SAVED_VDD_LEVELS, mVDDLevel);
	
	CPUupdate();
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(CPU_GOV_SYNC_FILE, preferences.getString(SAVED_CPU_GOV_SYNC, "1"));
	Utils.SetSOBValue(SCHED_MC_FILE, preferences.getString(SAVED_SCHED_MC, "0"));
	Utils.SetSOBValue(CPU_BOOST_FILE, preferences.getString(SAVED_CPU_BOOST, "1"));
	Utils.SetSOBValue(FILE_CELOX_DISPLAY_UV, preferences.getString(SAVED_CELOX_DISPLAY_UV, "0"));
	Utils.SetSOBValue(VDD_LEVELS_FILE, preferences.getString(SAVED_VDD_LEVELS, "0"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

}
