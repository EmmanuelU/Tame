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

public class CPUBoostPreference extends DialogPreference
		implements Resources {

    private View mView;

    private CheckBox mCpuBoost;
    private CheckBox mCpuBoostTouch;
    private EditText mCpuBoostTouchDur;
    private LinearLayout mCpuBoostTouchGroup;
    private Spinner mCpuBoostTouchFreq;

    private String[] mCpuFreqList;

    private SharedPreferences mPreferences;

    public CPUBoostPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.cpuboostdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	initiateData();
	List<String> list = new ArrayList<String>(Arrays.asList(Utils.getFileFreqToMhz(FREQ_LIST_FILE, 1000)));
	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
		android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	
	mCpuBoostTouchFreq.setAdapter(dataAdapter);

	updateData();

	mCpuBoostTouch.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();
	if(!mCpuBoostTouch.isChecked()){
		updateSharedPrefs(mPreferences, SAVED_CPU_BOOST_INPUT_DUR, Utils.writeSYSValue(CPU_BOOST_INPUT_DUR_FILE, "0"));
		updateSharedPrefs(mPreferences, SAVED_CPU_BOOST_INPUT_FREQ, Utils.writeSYSValue(CPU_BOOST_INPUT_FREQ_FILE, "0"));
	}
    }

    private boolean initiateData(){

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mCpuBoost = (CheckBox) mView.findViewById(R.id.cpu_boost);
	mCpuBoostTouch = (CheckBox) mView.findViewById(R.id.boost_input_enable);
	mCpuBoostTouchFreq = (Spinner) mView.findViewById(R.id.boost_input_freq);
	mCpuBoostTouchDur = (EditText) mView.findViewById(R.id.boost_input_dur);
	mCpuBoostTouchGroup = (LinearLayout) mView.findViewById(R.id.boost_input_group);

	mCpuFreqList = Utils.readOneLine(FREQ_LIST_FILE).split("\\s+");

	return true;
    }

    private void setData(){
	if(!initiateData()) return;

	updateSharedPrefs(mPreferences, SAVED_CPU_BOOST, Utils.writeSYSValue(CPU_BOOST_FILE, mCpuBoost.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, SAVED_CPU_BOOST_INPUT_BOOST, mCpuBoostTouch.isChecked() ? "1" : "0");
	if(mCpuBoostTouch.isChecked()){
		updateSharedPrefs(mPreferences, SAVED_CPU_BOOST_INPUT_DUR, Utils.writeSYSValue(CPU_BOOST_INPUT_DUR_FILE, mCpuBoostTouchDur.getText().toString()));
		updateSharedPrefs(mPreferences, SAVED_CPU_BOOST_INPUT_FREQ, Utils.writeSYSValue(CPU_BOOST_INPUT_FREQ_FILE, mCpuFreqList[(int) mCpuBoostTouchFreq.getSelectedItemId()]));
		Utils.toast(getContext(), "You will not be able to change your Processor Policies while input boost is enabled.");
	}
    }

    private void updateData(){
	if(!initiateData()) return;
	mCpuBoostTouch.setChecked(Utils.stringToBool(mPreferences.getString(SAVED_CPU_BOOST_INPUT_BOOST, "0")));

	updateDependencies();
	
	mCpuBoost.setChecked(Utils.stringToBool(Utils.readOneLine(CPU_BOOST_FILE)));
	mCpuBoostTouchDur.setText(Utils.readOneLine(CPU_BOOST_INPUT_DUR_FILE));
	mCpuBoostTouchFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, Utils.readOneLine(CPU_BOOST_INPUT_FREQ_FILE)));

    }

    private void updateDependencies(){
	if(!initiateData()) return;
	if(!Utils.fileExists(CPU_BOOST_FILE)) mCpuBoost.setEnabled(false);
	if(!Utils.fileExists(CPU_BOOST_INPUT_FREQ_FILE) || !Utils.fileExists(CPU_BOOST_INPUT_DUR_FILE)){
		Utils.layoutDisable(mCpuBoostTouchGroup);
	}
	
	if(mCpuBoostTouch.isChecked()){
		if(mPreferences.getString(CPU_BOOST_INPUT_DUR_FILE, "0").equals("0")){
			mCpuBoostTouchDur.setText("4000");
			mCpuBoostTouchFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, "384000"));
		}
		mCpuBoostTouchDur.setEnabled(true);
		mCpuBoostTouchFreq.setEnabled(true);
	} else if(!mCpuBoostTouch.isChecked()){
		mCpuBoostTouchDur.setEnabled(false);
		mCpuBoostTouchFreq.setEnabled(false);
	}
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(CPU_BOOST_FILE, preferences.getString(SAVED_CPU_BOOST, "1"));
	Utils.SetSOBValue(CPU_BOOST_INPUT_DUR_FILE, preferences.getString(SAVED_CPU_BOOST_INPUT_DUR, "0"));
	Utils.SetSOBValue(CPU_BOOST_INPUT_FREQ_FILE, preferences.getString(SAVED_CPU_BOOST_INPUT_FREQ, "0"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
