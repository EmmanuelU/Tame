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
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class SMPPreference extends DialogPreference
		implements Resources {

    private View mView;

    private CheckBox mMPDec;
    private CheckBox mMPDecScroff;

    private LayoutInflater inflater;

    private LinearLayout mCpuToggleGroup;
    private LinearLayout mMPDecGroup;
    private LinearLayout mMPDecSubGroup;

    private SharedPreferences mPreferences;

    class CpuToggle {
	View view;
	Switch toggle;
    }

    private CpuToggle mCore[] = new CpuToggle[Utils.getNumOfCpus()];

    public SMPPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.smpdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	initiateData();

	for(int i = 1; i < Utils.getNumOfCpus();){
		mCore[i] = new CpuToggle();
		mCore[i].view = inflater.inflate(R.layout.cputoggle, null);
		mCore[i].toggle = (Switch) mCore[i].view.findViewById(R.id.core_toggle);
		mCore[i].toggle.setText("Core "+ (i+1));
		if(Utils.fileExists(CPU_TOGGLE)) mCore[i].toggle.setChecked(Utils.stringToBool(Utils.readOneLine(Utils.toCPU(CPU_TOGGLE, i))));
		else mCore[i].toggle.setChecked(true);
		mCpuToggleGroup.addView(mCore[i].view);
		mCore[i].toggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateDependencies();
			}
		});
		i++;
	}

	updateData();

	mMPDec.setOnClickListener(new View.OnClickListener() {
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

    }

    private boolean initiateData(){

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mMPDec = (CheckBox) mView.findViewById(R.id.mpdec);
	mMPDecScroff = (CheckBox) mView.findViewById(R.id.mpdec_scroff);
	mMPDecGroup = (LinearLayout) mView.findViewById(R.id.mpdec_group);
	mMPDecSubGroup = (LinearLayout) mView.findViewById(R.id.mpdec_subgroup);

	mCpuToggleGroup = (LinearLayout) mView.findViewById(R.id.core_toggle_group);
	inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	return true;
    }

    private void setData(){
	
	if(!initiateData()) return;

	updateSharedPrefs(mPreferences, MPDEC, Utils.writeSYSValue(FILE_MPDEC_TOGGLE, mMPDec.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, MPDEC_SCROFF, Utils.writeSYSValue(FILE_MPDEC_SCROFF, mMPDecScroff.isChecked() ? "1" : "0"));

	String enabled = "1";
	for(int i = 1; i < Utils.getNumOfCpus();){
		Utils.queueSYSValue(Utils.toCPU(CPU_TOGGLE, i), Utils.boolToString(mCore[i].toggle.isChecked()));
		enabled = enabled + LINE_SPACE + Utils.boolToString(mCore[i].toggle.isChecked());
		i++;
	}

	Utils.launchSYSQueue();
	updateSharedPrefs(mPreferences, SAVED_CPU_TOGGLE, enabled);

    }

    private void updateData(){
	if(!initiateData()) return;

	mMPDec.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_MPDEC_TOGGLE)));
	mMPDecScroff.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_MPDEC_SCROFF)));

	updateDependencies();

    }

    private void updateDependencies(){
	if(!initiateData()) return;
	
	if(!Utils.fileExists(FILE_MPDEC_TOGGLE)) Utils.layoutDisable(mMPDecGroup);
	else if(mMPDec.isChecked()){
		Utils.layoutEnable(mMPDecSubGroup);
	} else {
		Utils.layoutDisable(mMPDecSubGroup);
	}

	if(!Utils.fileExists(CPU_TOGGLE)) Utils.layoutDisable(mCpuToggleGroup);

    }

    public static void SetOnBootData(SharedPreferences preferences){
	String[] enabled = preferences.getString(SAVED_CPU_TOGGLE, "1 1").split("\\s+");

	for(int i = 0; i < Arrays.asList(enabled).size();){
		if(!Utils.isStringEmpty(enabled[i]))
			Utils.SetSOBValue(Utils.toCPU(CPU_TOGGLE, i), enabled[i]);
		i++;
	}

	Utils.SetSOBValue(FILE_MPDEC_TOGGLE, preferences.getString(MPDEC, "1"));
	Utils.SetSOBValue(FILE_MPDEC_SCROFF, preferences.getString(MPDEC_SCROFF, "1"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
