package com.emman.tame.dialogs;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
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

    Spinner mCpuCore;
    Spinner mCpuMaxFreq;
    Spinner mCpuMinFreq;
    Spinner mCpuGovernor;

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
	setDialogLayoutResource(R.layout.cpupolicy);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;

	updateData();

	list = new ArrayList<String>(Arrays.asList(Utils.getFileFreqToMhz(FREQ_LIST_FILE, 1000)));
	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
		android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	
	mCpuMinFreq.setAdapter(dataAdapter);
	mCpuMaxFreq.setAdapter(dataAdapter);

	list = new ArrayList<String>(Arrays.asList(Utils.readOneLine(GOV_LIST_FILE).split("\\s+")));
	dataAdapter = new ArrayAdapter<String>(getContext(),
		R.layout.bigspinlayout, list);
	mCpuGovernor.setAdapter(dataAdapter);

	List<String> CpuNames = new ArrayList<String>();
	int number = 1;
	while(number <= Utils.getNumOfCpus()){
		if(!Utils.stringToBool(Utils.readOneLine(Utils.toCPU(CPU_ONLINE, number-1))) && number > 1) CpuNames.add("Core: " + number + " (offline)");
		else CpuNames.add("Core:    " + number);
		number = number + 1;
	}
	dataAdapter = new ArrayAdapter<String>(getContext(),
		R.layout.biggerspinlayout, CpuNames);
	mCpuCore.setAdapter(dataAdapter);

	mCpuCore.setOnItemSelectedListener(this);
	
	updateData();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();
	else updateData();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
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
	
	mCpuFreqList = Utils.readOneLine(FREQ_LIST_FILE).split("\\s+");
	mCpuGovList = Utils.readOneLine(GOV_LIST_FILE).split("\\s+");

	for(int i = 0; i < Utils.getNumOfCpus();){
		Utils.writeSYSValue(Utils.toCPU(CPU_ONLINE, i), "1");
		mCore[i] = new CpuPolicy();
		mCore[i].governor = Utils.readOneLine(Utils.toCPU(GOV_FILE, i));
		mCore[i].min = Utils.readOneLine(Utils.toCPU(FREQ_MIN_FILE, i));
		mCore[i].max = Utils.readOneLine(Utils.toCPU(FREQ_MAX_FILE, i));
		i++;
	}
	return true;
    }

    private void setData(){
	if(!initiateData()) return;
	String governors, minfreqs, maxfreqs;
	mCore[mCpu].governor = mCpuGovernor.getSelectedItem().toString();
	mCore[mCpu].min = mCpuFreqList[(int) mCpuMinFreq.getSelectedItemId()];
	mCore[mCpu].max = mCpuFreqList[(int) mCpuMaxFreq.getSelectedItemId()];
	Utils.writeSYSValue(Utils.toCPU(GOV_FILE, mCpu), mCore[mCpu].governor);
	Utils.writeSYSValue(Utils.toCPU(FREQ_MIN_FILE, mCpu), mCore[mCpu].min);
	Utils.writeSYSValue(Utils.toCPU(FREQ_MAX_FILE, mCpu), mCore[mCpu].max);

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
	
    }

    private void updateData(){
	if(!initiateData()) return;

	updateDependencies();

	mCpuMaxFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, Utils.readOneLine(Utils.toCPU(FREQ_MAX_FILE, mCpu))));
	mCpuMinFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, Utils.readOneLine(Utils.toCPU(FREQ_MIN_FILE, mCpu))));
	mCpuGovernor.setSelection(Utils.getArrayIndex(mCpuGovList, Utils.readOneLine(Utils.toCPU(GOV_FILE, mCpu))));
    }

    private void updateDependencies(){
	if(!initiateData()) return;
	if(!Utils.stringToBool(Utils.readOneLine(Utils.toCPU(CPU_ONLINE, mCpu))) && mCpu > 0) mCpuCore.setSelection(0);
	
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
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
