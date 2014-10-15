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

import com.emman.tame.R;
import com.emman.tame.fragments.CPUSettings;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class CPUPolicyPreference extends DialogPreference
		implements Resources {

    private View mView;

    private Spinner mCpuMaxFreq;
    private Spinner mCpuMinFreq;
    private Spinner mCpuGovernor;

    private String[] mCpuFreqList;
    private String[] mCpuGovList;

    private List<String> list;

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
		android.R.layout.simple_spinner_item, list);
	mCpuGovernor.setAdapter(dataAdapter);
	
	updateData();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();
	else updateData();
    }

    private boolean initiateData(){

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mCpuGovernor = (Spinner) mView.findViewById(R.id.cpu_gov);
	mCpuMaxFreq = (Spinner) mView.findViewById(R.id.cpu_max_freq);
	mCpuMinFreq = (Spinner) mView.findViewById(R.id.cpu_min_freq);
	
	mCpuFreqList = Utils.readOneLine(FREQ_LIST_FILE).split("\\s+");
	mCpuGovList = Utils.readOneLine(GOV_LIST_FILE).split("\\s+");

	return true;
    }

    private void setData(){
	if(!initiateData()) return;

	updateSharedPrefs(mPreferences, SAVED_GOV, Utils.writeSYSValue(GOV_FILE, mCpuGovernor.getSelectedItem().toString()));
	updateSharedPrefs(mPreferences, SAVED_MAX_FREQ, Utils.writeSYSValue(FREQ_MAX_FILE, mCpuFreqList[(int) mCpuMaxFreq.getSelectedItemId()]));
	updateSharedPrefs(mPreferences, SAVED_MIN_FREQ, Utils.writeSYSValue(FREQ_MIN_FILE, mCpuFreqList[(int) mCpuMinFreq.getSelectedItemId()]));
	
    }

    private void updateData(){
	if(!initiateData()) return;

	updateDependencies();

	mCpuMaxFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, Utils.readOneLine(FREQ_MAX_FILE)));
	mCpuMinFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, Utils.readOneLine(FREQ_MIN_FILE)));
	mCpuGovernor.setSelection(Utils.getArrayIndex(mCpuGovList, Utils.readOneLine(GOV_FILE)));
    }

    private void updateDependencies(){
	if(!initiateData()) return;

    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(FREQ_MIN_FILE, preferences.getString(SAVED_MIN_FREQ, "384000"));
	Utils.SetSOBValue(FREQ_MAX_FILE, preferences.getString(SAVED_MAX_FREQ, "1512000"));
	Utils.SetSOBValue(GOV_FILE, preferences.getString(SAVED_GOV, "ondemand"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
