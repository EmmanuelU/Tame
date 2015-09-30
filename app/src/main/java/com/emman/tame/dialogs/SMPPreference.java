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
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;
import com.stericson.RootTools.RootTools;

public class SMPPreference extends DialogPreference
		implements Resources {

    private View mView;

    private Switch mMPDec;
    private static boolean mMPDecBinary = Utils.fileExists(FILE_MPDEC_BINARY) || Utils.fileExists(FILE_MPDEC_BINARY_DISABLED);
    private CheckBox mMPDecScroff;
    private LinearLayout mMPDecGroup;
    private LinearLayout mMPDecSubGroup;

    private Switch mIntelliP;
    private CheckBox mIntelliPBoost;
    private CheckBox mIntelliPScroff;
    private Spinner mIntelliPScroffFreq;
    private LinearLayout mIntelliPGroup;
    private LinearLayout mIntelliPSubGroup;
    private LinearLayout mIntelliPScroffSubGroup;

    private LayoutInflater inflater;

    private LinearLayout mCpuToggleGroup;

    private String[] mCpuFreqList;

    private SharedPreferences mPreferences;

    private List<String> list;
    private ArrayAdapter<String> dataAdapter;

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

	//remove deprecated file
	if(Utils.fileExists(FILE_MPDEC_BINARY_DISABLED)){
		if(!RootTools.remount(FILE_MPDEC_BINARY_DISABLED, "rw")) Utils.CMD(true, "mount -o remount rw /system/");
		Utils.CMD(true, "mv -f " + FILE_MPDEC_BINARY_DISABLED + LINE_SPACE + FILE_MPDEC_BINARY);
	}

	for(int i = 1; i < Utils.getNumOfCpus();){
		mCore[i] = new CpuToggle();
		mCore[i].view = inflater.inflate(R.layout.smp_cputoggle, null);
		mCore[i].toggle = (Switch) mCore[i].view.findViewById(R.id.core_toggle);
		mCore[i].toggle.setText(getContext().getString(R.string.page_cpusettings) + LINE_SPACE + "#" + (i+1) + "    ");
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

	mCpuToggleGroup.addView(inflater.inflate(R.layout.smp_note2, null));

	mCpuFreqList = Utils.readOneLine(FREQ_LIST_FILE).split("\\s+");

	list = new ArrayList<String>(Arrays.asList(Utils.getFileFreqToMhz(FREQ_LIST_FILE, 1000)));
	dataAdapter = new ArrayAdapter<String>(getContext(),
		android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	
	mIntelliPScroffFreq.setAdapter(dataAdapter);

	updateData();

	mMPDec.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});

	mIntelliP.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});

	mIntelliPScroff.setOnClickListener(new View.OnClickListener() {
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

	mMPDec = (Switch) mView.findViewById(R.id.mpdec);
	mMPDecScroff = (CheckBox) mView.findViewById(R.id.mpdec_scroff);
	mMPDecGroup = (LinearLayout) mView.findViewById(R.id.mpdec_group);
	mMPDecSubGroup = (LinearLayout) mView.findViewById(R.id.mpdec_subgroup);

	mIntelliP = (Switch) mView.findViewById(R.id.intellip);
	mIntelliPBoost = (CheckBox) mView.findViewById(R.id.intellip_boost);
	mIntelliPScroff = (CheckBox) mView.findViewById(R.id.intellip_scroff);
	mIntelliPScroffFreq = (Spinner) mView.findViewById(R.id.intellip_scroff_freq);
	mIntelliPGroup = (LinearLayout) mView.findViewById(R.id.intellip_group);
	mIntelliPSubGroup = (LinearLayout) mView.findViewById(R.id.intellip_subgroup);
	mIntelliPScroffSubGroup = (LinearLayout) mView.findViewById(R.id.intellip_scroff_subgroup);

	mCpuToggleGroup = (LinearLayout) mView.findViewById(R.id.core_toggle_group);
	inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	return true;
    }

    private void setData(){
	if(!initiateData()) return;

	if(mMPDecBinary){
		Utils.shellProcess(PROCESS_MPDEC, mMPDec.isChecked());
	}

	updateSharedPrefs(mPreferences, MPDEC_CONTROL, Utils.writeSYSValue(FILE_MPDEC_TOGGLE, mMPDec.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, MPDEC_SCROFF, Utils.writeSYSValue(FILE_MPDEC_SCROFF, mMPDecScroff.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, INTELLIP_CONTROL, Utils.writeSYSValue(FILE_INTELLIP_TOGGLE, mIntelliP.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, INTELLIP_BOOST, Utils.writeSYSValue(FILE_INTELLIP_BOOST, mIntelliPBoost.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, INTELLIP_LIMIT_SCROFF, Utils.boolToString(mIntelliPScroff.isChecked()));
	updateSharedPrefs(mPreferences, INTELLIP_LIMIT_SCROFF_FREQ, (mIntelliPScroff.isChecked() ? Utils.writeSYSValue(FILE_INTELLIP_SCROFF_FREQ, mCpuFreqList[(int) mIntelliPScroffFreq.getSelectedItemId()]) : Utils.writeSYSValue(FILE_INTELLIP_SCROFF_FREQ, "0")));


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

	if(mMPDecBinary) mMPDec.setChecked(Utils.isShellProcessRunning(PROCESS_MPDEC));

	else mMPDec.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_MPDEC_TOGGLE)));
	mMPDecScroff.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_MPDEC_SCROFF)));

	mIntelliP.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_INTELLIP_TOGGLE)));
	mIntelliPBoost.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_INTELLIP_BOOST)));
	mIntelliPScroff.setChecked(Utils.stringToBool(mPreferences.getString(INTELLIP_LIMIT_SCROFF, "0")));
	mIntelliPScroffFreq.setSelection(Utils.getArrayIndex(mCpuFreqList, Utils.readOneLine(FILE_INTELLIP_SCROFF_FREQ)));

	updateDependencies();

    }

    private void updateDependencies(){
	if(!initiateData()) return;

	if(mMPDec.isChecked() && mIntelliP.isChecked()){
		mMPDec.setChecked(false);
		mIntelliP.setChecked(false);
		Utils.burntToast(getContext(), getContext().getString(R.string.msg_multi_smp));
	}

	if(!Utils.fileExists(FILE_MPDEC_TOGGLE) && !mMPDecBinary) Utils.layoutDisable(mMPDecGroup);
	else if(mMPDec.isChecked() && !mMPDecBinary){
		Utils.layoutEnable(mMPDecSubGroup);
	} else {
		Utils.layoutDisable(mMPDecSubGroup);
	}

	if(!Utils.fileExists(FILE_INTELLIP_TOGGLE)) Utils.layoutDisable(mIntelliPGroup);	
	else if(mIntelliP.isChecked()){

		Utils.layoutEnable(mIntelliPSubGroup);
	} else {
		Utils.layoutDisable(mIntelliPSubGroup);
	}

	if(mIntelliP.isChecked() && mIntelliPScroff.isChecked())
		Utils.layoutEnable(mIntelliPScroffSubGroup);
	else
		Utils.layoutDisable(mIntelliPScroffSubGroup);

	if(!Utils.fileExists(CPU_TOGGLE)) Utils.layoutDisable(mCpuToggleGroup);

    }

    public static void SetOnBootData(SharedPreferences preferences){
	String[] enabled = preferences.getString(SAVED_CPU_TOGGLE, "1 1").split("\\s+");

	if(mMPDecBinary && !Utils.isStringEmpty(preferences.getString(MPDEC_CONTROL, ""))){
		Utils.shellProcess(PROCESS_MPDEC, Utils.stringToBool(preferences.getString(MPDEC_CONTROL, "")));
	}

	for(int i = 0; i < Arrays.asList(enabled).size();){
		if(!Utils.isStringEmpty(enabled[i]))
			Utils.SetSOBValue(Utils.toCPU(CPU_TOGGLE, i), enabled[i]);
		i++;
	}

	Utils.SetSOBValue(FILE_MPDEC_TOGGLE, preferences.getString(MPDEC_CONTROL, ""));
	Utils.SetSOBValue(FILE_MPDEC_SCROFF, preferences.getString(MPDEC_SCROFF, "1"));

	Utils.SetSOBValue(FILE_INTELLIP_TOGGLE, preferences.getString(INTELLIP_CONTROL, ""));
	Utils.SetSOBValue(FILE_INTELLIP_BOOST, preferences.getString(INTELLIP_BOOST, "1"));
	Utils.SetSOBValue(FILE_INTELLIP_SCROFF_FREQ, Utils.stringToBool(preferences.getString(INTELLIP_LIMIT_SCROFF, "0")) ? preferences.getString(INTELLIP_LIMIT_SCROFF_FREQ, "0") : "0");
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
