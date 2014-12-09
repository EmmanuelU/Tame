package com.emman.tame.dialogs;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
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
import android.widget.AdapterView.OnItemSelectedListener;
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

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import com.emman.tame.R;
import com.emman.tame.fragments.CPUSettings;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class CPUPolicyPreference extends DialogPreference
		implements Resources, AdapterView.OnItemSelectedListener {

    private View mView;

    private CheckBox mCpuGovSync;

    private Spinner mCpuCore;
    private Spinner mCpuMaxFreq;
    private Spinner mCpuMinFreq;
    private Spinner mCpuGovernor;

    private LinearLayout mCpuPolicyGroup;

    private TextView IBDisclaimer;

    private String[] mCpuFreqList;
    private String[] mCpuGovList;

    private List<String> list;
    private int mCpu = 0;

    class CpuPolicy {
	String governor;
	String min;
	String max;
    }

    private CpuPolicy mCore[] = new CpuPolicy[Utils.getNumOfCpus()];

    private SharedPreferences mPreferences;

    public CPUPolicyPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.cpupolicydialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;

	if(!initiateData()) return;

	if(Utils.stringToBool(mPreferences.getString(SAVED_CPU_BOOST_INPUT_BOOST, "0")) && Utils.fileExists(CPU_BOOST_INPUT_FREQ_FILE) && Utils.fileExists(CPU_BOOST_INPUT_DUR_FILE)){
		IBDisclaimer.setVisibility(View.VISIBLE);
		Utils.layoutDisable(mCpuPolicyGroup);
	}
	else{
		IBDisclaimer.setVisibility(View.GONE);

		list = new ArrayList<String>(Arrays.asList(Utils.getFileFreqToMhz(FREQ_LIST_FILE, 1000)));
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
			android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	
		mCpuMinFreq.setAdapter(dataAdapter);
		mCpuMaxFreq.setAdapter(dataAdapter);

		list = new ArrayList<String>(Arrays.asList(mCpuGovList));
		dataAdapter = new ArrayAdapter<String>(getContext(),
			R.layout.bigspinlayout, list);
		mCpuGovernor.setAdapter(dataAdapter);

		List<String> CpuNames = new ArrayList<String>();
	
		for(int i = 0; i < Utils.getNumOfCpus();){
			if(i > 0){
				Utils.writeSYSValue(Utils.toCPU(CPU_ONLINE, i), "1");
				if(!Utils.stringToBool(Utils.readOneLine(Utils.toCPU(CPU_ONLINE, i)))) CpuNames.add("Core: " + (i+1) + " (offline)");
				else CpuNames.add("Core:    " + (i+1));
			}
			else CpuNames.add("Primary Core");
			i++;
		}
		dataAdapter = new ArrayAdapter<String>(getContext(),
			R.layout.biggerspinlayout, CpuNames);
		mCpuCore.setAdapter(dataAdapter);

		mCpuCore.setOnItemSelectedListener(this);

		mCpuGovSync.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateDependencies();
			}
		});

		for(int i = 0; i < Utils.getNumOfCpus();){
			if(i > 0) Utils.writeSYSValue(Utils.toCPU(CPU_ONLINE, i), "1");
			mCore[i] = new CpuPolicy();
			mCore[i].governor = Utils.readOneLine(Utils.toCPU(GOV_FILE, i));
			mCore[i].min = Utils.readOneLine(Utils.toCPU(FREQ_MIN_FILE, i));
			mCore[i].max = Utils.readOneLine(Utils.toCPU(FREQ_MAX_FILE, i));
			i++;
		}

		mCpuGovSync.setChecked(Utils.stringToBool(Utils.readOneLine(CPU_GOV_SYNC_FILE)));
	
		updateData();
	}
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
	super.onPrepareDialogBuilder(builder);
	if(Utils.stringToBool(mPreferences.getString(SAVED_CPU_BOOST_INPUT_BOOST, "0"))) builder.setPositiveButton(null, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	if(!initiateData()) return;

	mCpuMinFreq.setEnabled(false);
	mCpuMaxFreq.setEnabled(false);
	mCpuGovernor.setEnabled(false);

	mCore[mCpu].governor = mCpuGovernor.getSelectedItem().toString();
	mCore[mCpu].min = mCpuFreqList[(int) mCpuMinFreq.getSelectedItemId()];
	mCore[mCpu].max = mCpuFreqList[(int) mCpuMaxFreq.getSelectedItemId()];

	mCpu = pos;
	updateData();
    }

    @Override
    public void onNothingSelected(AdapterView parent) {
      // Do nothing.
    }

    private boolean initiateData(){

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mCpuCore = (Spinner) mView.findViewById(R.id.cpu_core);
	mCpuGovernor = (Spinner) mView.findViewById(R.id.cpu_gov);
	mCpuMaxFreq = (Spinner) mView.findViewById(R.id.cpu_max_freq);
	mCpuMinFreq = (Spinner) mView.findViewById(R.id.cpu_min_freq);
	mCpuGovSync = (CheckBox) mView.findViewById(R.id.cpu_gov_sync);
	IBDisclaimer = (TextView) mView.findViewById(R.id.IBdisclaimer);
	mCpuPolicyGroup = (LinearLayout) mView.findViewById(R.id.cpu_policy_group);
	
	mCpuFreqList = Utils.readOneLine(FREQ_LIST_FILE).split("\\s+");
	mCpuGovList = Utils.readOneLine(GOV_LIST_FILE).split("\\s+");

	return true;
    }

    private void setData(){
	if(!initiateData()) return;

	String governors, minfreqs, maxfreqs;
	mCore[mCpu].governor = mCpuGovernor.getSelectedItem().toString();
	mCore[mCpu].min = mCpuFreqList[(int) mCpuMinFreq.getSelectedItemId()];
	mCore[mCpu].max = mCpuFreqList[(int) mCpuMaxFreq.getSelectedItemId()];
	if(!Utils.stringToBool(Utils.readOneLine(CPU_GOV_SYNC_FILE))){
		for(int i = 0; i < Utils.getNumOfCpus();){
			if(i != mCpu){ //wait to apply new value last
				Utils.queueSYSValue(Utils.toCPU(GOV_FILE, i), mCore[i].governor);
				Utils.queueSYSValue(Utils.toCPU(FREQ_MIN_FILE, i), mCore[i].min);
				Utils.queueSYSValue(Utils.toCPU(FREQ_MAX_FILE, i), mCore[i].max);
			}
			i++;
		}
	}
	Utils.queueSYSValue(Utils.toCPU(GOV_FILE, mCpu), mCore[mCpu].governor);
	Utils.queueSYSValue(Utils.toCPU(FREQ_MIN_FILE, mCpu), mCore[mCpu].min);
	Utils.queueSYSValue(Utils.toCPU(FREQ_MAX_FILE, mCpu), mCore[mCpu].max);
	governors = mCore[0].governor;
	minfreqs = mCore[0].min;
	maxfreqs = mCore[0].max;
	for(int i = 1; i < Utils.getNumOfCpus();){
		governors = governors + " " + mCore[i].governor;
		minfreqs = minfreqs + " " + mCore[i].min;
		maxfreqs = maxfreqs + " " + mCore[i].max;
		i++;
	}
	updateSharedPrefs(mPreferences, SAVED_GOV, governors);
	updateSharedPrefs(mPreferences, SAVED_MIN_FREQ, minfreqs);
	updateSharedPrefs(mPreferences, SAVED_MAX_FREQ, maxfreqs);
	updateSharedPrefs(mPreferences, SAVED_CPU_GOV_SYNC, Utils.writeSYSValue(CPU_GOV_SYNC_FILE, mCpuGovSync.isChecked() ? "1" : "0"));
	Utils.launchSYSQueue();
    }

    private void updateDependencies(){
	if(!initiateData()) return;
	if(!Utils.fileExists(CPU_GOV_SYNC_FILE)) mCpuGovSync.setEnabled(false);
	if(mCpuGovSync.isChecked()){
		mCpu = 0;
		mCpuCore.setSelection(0);
		mCpuCore.setEnabled(false);
	}
	else mCpuCore.setEnabled(true);
    }

    private void updateData(){
	if(!initiateData()) return;
	updateDependencies();

	mCpuMinFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, mCore[mCpu].min));
	mCpuMaxFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, mCore[mCpu].max));
	mCpuGovernor.setSelection(Utils.getArrayIndex(mCpuGovList, mCore[mCpu].governor));

	mCpuMinFreq.setEnabled(true);
	mCpuMaxFreq.setEnabled(true);
	mCpuGovernor.setEnabled(true);
    }

    public static void SetOnBootData(SharedPreferences preferences){
	String[] governors = preferences.getString(SAVED_GOV, "0 0").split("\\s+");
	String[] minfreqs = preferences.getString(SAVED_MIN_FREQ, "0 0").split("\\s+");
	String[] maxfreqs = preferences.getString(SAVED_MAX_FREQ, "0 0").split("\\s+");
	List<String> lenght = Arrays.asList(governors);
	for(int i = 0; i < lenght.size();){
		if(governors[i] == null || minfreqs[i] == null || maxfreqs[i] == null){
			i++;
			continue;
		}
		else{
			Utils.SetSOBValue(Utils.toCPU(CPU_ONLINE, i), "1");
			Utils.SetSOBValue(Utils.toCPU(GOV_FILE, i), governors[i]);
			Utils.SetSOBValue(Utils.toCPU(FREQ_MIN_FILE, i), minfreqs[i]);
			Utils.SetSOBValue(Utils.toCPU(FREQ_MAX_FILE, i), maxfreqs[i]);
			i++;
		}
	}
	Utils.SetSOBValue(CPU_GOV_SYNC_FILE, preferences.getString(SAVED_CPU_GOV_SYNC, "1"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
