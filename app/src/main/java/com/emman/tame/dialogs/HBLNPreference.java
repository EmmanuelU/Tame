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

public class HBLNPreference extends DialogPreference
		implements Resources {

    private LinearLayout mTouchKeyOverrideIntervalsGroup;
    private LinearLayout mTouchKeyOverrideRGBGroup;
        
    private CheckBox mTouchKeyOverrideRGB;
    private ColorPicker mTouchKeyRGB;
    private EditText mTouchKeyRGBText;
    private OpacityBar mTouchKeyRGBOpacity;

    private CheckBox mTouchKeyOverrideIntervals;
    private EditText mTouchKeyONInterval;
    private EditText mTouchKeyOFFInterval;

    private Button mBLNTest;

    private View mView;

    private SharedPreferences mPreferences;

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
		    final Bundle b = new Bundle();
		    b.putBoolean(EXTRA_FORCE_SHOW_LIGHTS, true);
		    String[] intervals = Utils.readOneLine(FILE_HBLN_BLINK_OVERRIDE).split("\\s+");
		    if (!intervals[0].equals("0"))
		        Utils.testNotification(getContext(), NotificationID.BLN_TEST, null, "BLN: Using custom light intervals (" + intervals[0] + "/" + intervals[1] + "msecs).", Integer.parseInt(intervals[0]), Integer.parseInt(intervals[1]), mTouchKeyRGB.getColor(), b);
		    else
		        Utils.notification(getContext(), NotificationID.BLN_TEST, null, "BLN: Using default light intervals (300/1500msecs).");
		}
	});
	
	mTouchKeyOverrideRGB.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    if(mTouchKeyOverrideRGB.isChecked()) mTouchKeyRGBText.setText(Integer.toHexString(mTouchKeyRGB.getColor()));
		    else mTouchKeyRGBText.setText("disabled");
		    updateDependencies();
		}
	});
	
	mTouchKeyRGB.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {

        @Override
        public void onColorChanged(int color) {
            if(mTouchKeyOverrideRGB.isChecked()) mTouchKeyRGBText.setText(Integer.toHexString(color));
        }
    });

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

	return true;
    }

    private void setData(){
	if(!initiateData()) return;
	
	if(mTouchKeyOverrideRGB.isChecked()) updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_RGB, Utils.writeSYSValue(FILE_HBLN_BLINK_RGB, Integer.toHexString(mTouchKeyRGB.getColor())));
	else updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_RGB, Utils.writeSYSValue(FILE_HBLN_BLINK_RGB, ""));
	
	if(mTouchKeyOverrideIntervals.isChecked()) updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_OVERRIDE, Utils.writeSYSValue(FILE_HBLN_BLINK_OVERRIDE, mTouchKeyONInterval.getText().toString() + " " + mTouchKeyOFFInterval.getText().toString()));
	else updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_OVERRIDE, Utils.writeSYSValue(FILE_HBLN_BLINK_OVERRIDE, ""));
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
	else mTouchKeyRGBText.setText("disabled");

	 updateDependencies();

    }

    private void updateDependencies(){
	if(!initiateData()) return;

	if(mTouchKeyOverrideIntervals.isChecked()){
		Utils.layoutEnable(mTouchKeyOverrideIntervalsGroup);
	} else {
		Utils.layoutDisable(mTouchKeyOverrideIntervalsGroup);
	}
	
	if(mTouchKeyOverrideRGB.isChecked()){
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

    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(FILE_HBLN_BLINK_RGB, preferences.getString(TOUCHKEY_BLN_BLINK_RGB, ""));
	Utils.SetSOBValue(FILE_HBLN_BLINK_OVERRIDE, preferences.getString(TOUCHKEY_BLN_BLINK_OVERRIDE, ""));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
