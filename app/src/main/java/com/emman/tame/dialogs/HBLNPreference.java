package com.emman.tame.dialogs;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
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
import android.widget.Button;
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

import com.emman.tame.R;
import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HBLNPreference extends DialogPreference
		implements Resources, AdapterView.OnItemSelectedListener {

    private LinearLayout mTouchKeyOverrideIntervalsGroup;
    private LinearLayout mTouchKeyOverrideRGBGroup;
        
    private CheckBox mTouchKeyOverrideRGB;
    private ColorPicker mTouchKeyRGB;
    private EditText mTouchKeyRGBText;
    private OpacityBar mTouchKeyRGBOpacity;

    private CheckBox mTouchKeyOverrideIntervals;
    private EditText mTouchKeyONInterval;
    private EditText mTouchKeyOFFInterval;


    private Spinner mTouchKeyOverridePattern;

    private Button mBLNTest;

    private View mView;

    private SharedPreferences mPreferences;

    private String previousPattern = Utils.readOneLine(FILE_HBLN_OVERRIDE_PATTERN);

    public HBLNPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.hblndialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateData();
	mTouchKeyOverrideIntervals.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});

	mBLNTest.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    try{
			    Utils.clearNotification(getContext(), NotificationID.BLN_TEST);
			    Utils.writeSYSValue(FILE_HBLN_OVERRIDE_PATTERN, getContext().getResources().getStringArray(R.array.hbln_pattern_values)[(int) mTouchKeyOverridePattern.getSelectedItemId()]);
			    try{
			    	Thread.sleep(300);
			    } catch (Exception e) {}
			    String[] intervals = Utils.readOneLine(FILE_HBLN_BLINK_OVERRIDE).split("\\s+");
			    if (!intervals[0].equals("0"))
				Utils.testNotification(getContext(), NotificationID.BLN_TEST, null, "BLN: " + getContext().getString(R.string.msg_bln_preset) + " (" + intervals[0] + "/" + intervals[1] + getContext().getString(R.string.item_msecs2) + ").", Integer.parseInt(intervals[0]), Integer.parseInt(intervals[1]), mTouchKeyRGB.getColor());
			    else if (Utils.isInteger(mTouchKeyONInterval.getText().toString()) && Utils.isInteger(mTouchKeyOFFInterval.getText().toString()) && Integer.parseInt(mTouchKeyONInterval.getText().toString()) > 0 && Integer.parseInt(mTouchKeyOFFInterval.getText().toString()) > 0)
				Utils.testNotification(getContext(), NotificationID.BLN_TEST, null, "BLN: " + getContext().getString(R.string.msg_bln_preset) + " (" + mTouchKeyONInterval.getText().toString() + "/" + mTouchKeyOFFInterval.getText().toString() + getContext().getString(R.string.item_msecs2) + ").", Integer.parseInt(mTouchKeyONInterval.getText().toString()), Integer.parseInt(mTouchKeyOFFInterval.getText().toString()), mTouchKeyRGB.getColor());
			    else
				Utils.notification(getContext(), NotificationID.BLN_TEST, null, getContext().getString(R.string.msg_bln_preset2));
			    try{
			    	Thread.sleep(300);
			    } catch (Exception e) {}
			    Utils.writeSYSValue(FILE_HBLN_OVERRIDE_PATTERN, previousPattern);
		    } catch (Exception e) {}
		}
	});
	
	mTouchKeyOverrideRGB.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    updateDependencies();
		}
	});
	
	mTouchKeyRGB.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {

		@Override
		public void onColorChanged(int color) {
			if (mTouchKeyOverrideRGB.isChecked())
				mTouchKeyRGBText.setText(Integer.toHexString(color));
		}
	});

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	if(!initiateData()) return;

	updateDependencies();
    }

    @Override
    public void onNothingSelected(AdapterView parent) {
      // Do nothing.
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();
	Utils.clearNotification(getContext(), NotificationID.BLN_TEST);
    }

    private boolean initiateData(){
	if(!Utils.fileExists(FILE_HBLN_BLINK_OVERRIDE)) return false;

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mBLNTest = (Button) mView.findViewById(R.id.blntest);
	
	mTouchKeyRGB = (ColorPicker) mView.findViewById(R.id.touchkey_bln_rgb);
	mTouchKeyRGBOpacity = (OpacityBar) mView.findViewById(R.id.touchkey_bln_rgb_alpha);
	
	mTouchKeyOverrideIntervals = (CheckBox) mView.findViewById(R.id.touchkey_bln_override_interval);
	mTouchKeyOverrideRGB = (CheckBox) mView.findViewById(R.id.touchkey_bln_override_rgb);

	mTouchKeyRGBText = (EditText) mView.findViewById(R.id.touchkey_bln_rgb_text);
	mTouchKeyONInterval = (EditText) mView.findViewById(R.id.touchkey_bln_on_interval);
	mTouchKeyOFFInterval = (EditText) mView.findViewById(R.id.touchkey_bln_off_interval);

	mTouchKeyOverrideIntervalsGroup = (LinearLayout) mView.findViewById(R.id.touchkey_bln_override_group);
	mTouchKeyOverrideRGBGroup = (LinearLayout) mView.findViewById(R.id.touchkey_bln_rgb_group);

	mTouchKeyOverridePattern = (Spinner) mView.findViewById(R.id.touchkey_bln_override_pattern);

	return true;
    }

    private void updateData(){
	if(!initiateData()) return;

	if(Utils.fileExists(FILE_HBLN_BLINK_OVERRIDE)){
		String[] intervals = Utils.readOneLine(FILE_HBLN_BLINK_OVERRIDE).split("\\s+");
		if(!(Utils.isInteger(intervals[0]) || Utils.isInteger(intervals[1]))){
			mTouchKeyOverrideIntervals.setChecked(false);
			mTouchKeyONInterval.setText("0");
			mTouchKeyOFFInterval.setText("0");
		}
		else{
			if(!(intervals[0].equals("0") || intervals[1].equals("0"))) mTouchKeyOverrideIntervals.setChecked(true);
			mTouchKeyONInterval.setText(intervals[0]);
			mTouchKeyOFFInterval.setText(intervals[1]);
		}
	}
	
	mTouchKeyOverrideRGB.setChecked(Utils.readOneLine(FILE_HBLN_BLINK_RGB).equals("0") ? false : true);
	BigInteger value = new BigInteger(Utils.readOneLine(FILE_HBLN_BLINK_RGB), 16);
	mTouchKeyRGB.setOldCenterColor(value.intValue());
	mTouchKeyRGB.setColor(value.intValue());
	mTouchKeyRGB.addOpacityBar(mTouchKeyRGBOpacity);
	
	if(mTouchKeyOverrideRGB.isChecked()) mTouchKeyRGBText.setText(Integer.toHexString(mTouchKeyRGB.getColor()));
	else mTouchKeyRGBText.setText(getContext().getString(R.string.item_disabled));

	List<String> list = new ArrayList<String>(Arrays.asList(getContext().getResources().getStringArray(R.array.hbln_pattern_entries)));
	
	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
		android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	mTouchKeyOverridePattern.setAdapter(dataAdapter);

	mTouchKeyOverridePattern.setSelection(Utils.getArrayIndex(getContext().getResources().getStringArray(R.array.hbln_pattern_values), Utils.readOneLine(FILE_HBLN_OVERRIDE_PATTERN)));

	mTouchKeyOverridePattern.setOnItemSelectedListener(this);

	updateDependencies();

    }

    private void updateDependencies(){
	if(!initiateData()) return;

	if(mTouchKeyOverrideIntervals.isChecked() && mTouchKeyOverridePattern.getSelectedItemId() == 0){
		Utils.layoutEnable(mTouchKeyOverrideIntervalsGroup);
	} else {
		Utils.layoutDisable(mTouchKeyOverrideIntervalsGroup);
	}
	
	if(mTouchKeyOverrideRGB.isChecked() && mTouchKeyOverridePattern.getSelectedItemId() == 0){
		Utils.layoutEnable(mTouchKeyOverrideRGBGroup);
	} else {
		Utils.layoutDisable(mTouchKeyOverrideRGBGroup);
	}

	if(!Utils.fileExists(FILE_HBLN_BLINK_OVERRIDE)) mTouchKeyOverrideIntervals.setEnabled(false);

	if(Utils.isInteger(mTouchKeyONInterval.getText().toString()) && Utils.isInteger(mTouchKeyOFFInterval.getText().toString())){
		if(mTouchKeyOverrideIntervals.isChecked() && (mTouchKeyONInterval.getText().toString()).equals("0") && (mTouchKeyOFFInterval.getText().toString()).equals("0")){
			mTouchKeyONInterval.setText("300");
			mTouchKeyOFFInterval.setText("1500");
		}
	}

	if(mTouchKeyOverrideRGB.isChecked() && mTouchKeyOverridePattern.getSelectedItemId() == 0) mTouchKeyRGBText.setText(Integer.toHexString(mTouchKeyRGB.getColor()));
	else mTouchKeyRGBText.setText(getContext().getString(R.string.item_disabled));

	if(!Utils.fileExists(FILE_HBLN_OVERRIDE_PATTERN)) mTouchKeyOverridePattern.setEnabled(false);

	mTouchKeyOverrideIntervals.setEnabled(mTouchKeyOverridePattern.getSelectedItemId() == 0);
	mTouchKeyOverrideRGB.setEnabled(mTouchKeyOverridePattern.getSelectedItemId() == 0);
    }

    private void setData(){
	if(!initiateData()) return;
	
	if(mTouchKeyOverrideRGB.isChecked()) updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_RGB, Utils.writeSYSValue(FILE_HBLN_BLINK_RGB, Integer.toHexString(mTouchKeyRGB.getColor())));
	else updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_RGB, Utils.writeSYSValue(FILE_HBLN_BLINK_RGB, ""));
	
	if(mTouchKeyOverrideIntervals.isChecked()) updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_OVERRIDE, Utils.writeSYSValue(FILE_HBLN_BLINK_OVERRIDE, mTouchKeyONInterval.getText().toString() + " " + mTouchKeyOFFInterval.getText().toString()));
	else updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_OVERRIDE, Utils.writeSYSValue(FILE_HBLN_BLINK_OVERRIDE, ""));

	updateSharedPrefs(mPreferences, HBLN_OVERRIDE_PATTERN, Utils.writeSYSValue(FILE_HBLN_OVERRIDE_PATTERN, getContext().getResources().getStringArray(R.array.hbln_pattern_values)[(int) mTouchKeyOverridePattern.getSelectedItemId()]));
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(FILE_HBLN_BLINK_RGB, preferences.getString(TOUCHKEY_BLN_BLINK_RGB, ""));
	Utils.SetSOBValue(FILE_HBLN_BLINK_OVERRIDE, preferences.getString(TOUCHKEY_BLN_BLINK_OVERRIDE, ""));
	Utils.SetSOBValue(FILE_HBLN_OVERRIDE_PATTERN, preferences.getString(HBLN_OVERRIDE_PATTERN, ""));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
