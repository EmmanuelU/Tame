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
import android.widget.Switch;
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

public class CPUInputBoostV2Preference extends DialogPreference
		implements Resources {

    private View mView;

    private Switch mCpuBoostTouch;
    private TextView mCpuBoostThresholdText;
    private SeekBar mCpuBoostThreshold;
    private LinearLayout mCpuBoostGroup;

    private String[] mCpuFreqList;

    private SharedPreferences mPreferences;

    public CPUInputBoostV2Preference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.cpuinputboostv2dialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	initiateData();

	updateData();
	
	mCpuBoostTouch.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});
	
	mCpuBoostThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

	    @Override
	    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	    	updateText();
	    }

	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) {

	    }

	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
		
	    }

	});

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();
    }

    private boolean initiateData(){

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mCpuBoostTouch = (Switch) mView.findViewById(R.id.boost_input_enable);
	mCpuBoostThreshold = (SeekBar) mView.findViewById(R.id.boost_input_threshold);
	mCpuBoostThresholdText = (TextView) mView.findViewById(R.id.boost_input_threshold_text);
	mCpuBoostGroup = (LinearLayout) mView.findViewById(R.id.boost_input_group);

	return true;
    }

    private void setData(){
	if(!initiateData()) return;

	updateSharedPrefs(mPreferences, SAVED_CPU_BOOST_INPUT_BOOST, Utils.writeSYSValue(CPU_BOOST_INPUT_TOGGLE, Utils.boolToString(mCpuBoostTouch.isChecked())));
	updateSharedPrefs(mPreferences, SAVED_CPU_BOOST_INPUT_UP_THRESHOLD, Utils.writeSYSValue(CPU_BOOST_INPUT_UP_THRESHOLD_FILE, String.valueOf(mCpuBoostThreshold.getProgress())));
    }

    private void updateData(){
	if(!initiateData()) return;
	mCpuBoostTouch.setChecked(Utils.stringToBool(Utils.readOneLine(CPU_BOOST_INPUT_TOGGLE)));

	updateDependencies();
	
	mCpuBoostThreshold.setProgress(Integer.parseInt(Utils.readOneLine(CPU_BOOST_INPUT_UP_THRESHOLD_FILE)));
	mCpuBoostThreshold.setMax(100);
	updateText();

    }

    private void updateText(){
	mCpuBoostThresholdText.setText((getContext().getString(R.string.item_threshold) + LINE_SPACE) + String.valueOf(mCpuBoostThreshold.getProgress()) + "%");
    }

    private void updateDependencies(){
	if(!initiateData()) return;
	
	if(mCpuBoostTouch.isChecked()){
		Utils.layoutEnable(mCpuBoostGroup);
	} else {
		Utils.layoutDisable(mCpuBoostGroup);
	}
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(CPU_BOOST_INPUT_TOGGLE, preferences.getString(SAVED_CPU_BOOST_INPUT_BOOST, "0"));
	Utils.SetSOBValue(CPU_BOOST_INPUT_UP_THRESHOLD_FILE, preferences.getString(SAVED_CPU_BOOST_INPUT_UP_THRESHOLD, "30"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
