package com.emman.tame.dialogs;

import android.app.Activity;
import android.app.ActionBar;
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

public class BLNPreference extends DialogPreference
		implements Resources {

    private CheckBox mTouchKeyBLN;

    private SeekBar mTouchKeyBLNMaxBlink;
    private TextView mCurTouchKeyBLNBlink;

    private LinearLayout mTouchKeyBLNBlinkGroup;
    private LinearLayout mTouchKeyOverrideIntervalsGroup;

    private CheckBox mTouchKeyOverrideIntervals;
    private EditText mTouchKeyONInterval;
    private EditText mTouchKeyOFFInterval;

    private Button mBLNTest;

    private View mView;

    private SharedPreferences mPreferences;

    public BLNPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.blndialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateData();

	mTouchKeyBLN.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});

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
		    String[] intervals = Utils.readOneLine(FILE_BLN_BLINK_OVERRIDE).split("\\s+");
		    if (!intervals[0].equals("0"))
		        Utils.testNotification(getContext(), NotificationID.BLN_TEST, null, "BLN: " + getContext().getString(R.string.msg_bln_preset) + " (" + intervals[0] + "/" + intervals[1] + getContext().getString(R.string.item_msecs2) + ").", Integer.parseInt(intervals[0]), Integer.parseInt(intervals[1]), 0);
		    else if (Utils.isInteger(mTouchKeyONInterval.getText().toString()) && Utils.isInteger(mTouchKeyOFFInterval.getText().toString()) && Integer.parseInt(mTouchKeyONInterval.getText().toString()) > 0 && Integer.parseInt(mTouchKeyOFFInterval.getText().toString()) > 0)
		        Utils.testNotification(getContext(), NotificationID.BLN_TEST, null, "BLN: " + getContext().getString(R.string.msg_bln_preset) + " (" + mTouchKeyONInterval.getText().toString() + "/" + mTouchKeyOFFInterval.getText().toString() + getContext().getString(R.string.item_msecs2) + ").", Integer.parseInt(mTouchKeyONInterval.getText().toString()), Integer.parseInt(mTouchKeyOFFInterval.getText().toString()), 0);
		    else
		        Utils.notification(getContext(), NotificationID.BLN_TEST, null, getContext().getString(R.string.msg_bln_preset2));
		    } catch (Exception e) {}
		}
	});

	mTouchKeyONInterval.addTextChangedListener(new TextWatcher() {

		public void afterTextChanged(Editable s) {
			updateText();
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		public void onTextChanged(CharSequence s, int start, int before, int count) {}
	});

	mTouchKeyOFFInterval.addTextChangedListener(new TextWatcher() {

		public void afterTextChanged(Editable s) {
			updateText();
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		public void onTextChanged(CharSequence s, int start, int before, int count) {}
	});

	mTouchKeyBLNMaxBlink.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
	Utils.clearNotification(getContext(), NotificationID.BLN_TEST);
    }

    private boolean initiateData(){
	if(!Utils.fileExists(FILE_BLN_TOGGLE)) return false;

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mBLNTest = (Button) mView.findViewById(R.id.blntest);

	mTouchKeyBLN = (CheckBox) mView.findViewById(R.id.touchkey_bln);
	mTouchKeyOverrideIntervals = (CheckBox) mView.findViewById(R.id.touchkey_bln_override_interval);

	mTouchKeyONInterval = (EditText) mView.findViewById(R.id.touchkey_bln_on_interval);
	mTouchKeyOFFInterval = (EditText) mView.findViewById(R.id.touchkey_bln_off_interval);

	mCurTouchKeyBLNBlink = (TextView) mView.findViewById(R.id.cur_touchkey_bln_blink);

	mTouchKeyBLNBlinkGroup = (LinearLayout) mView.findViewById(R.id.touchkey_bln_blink_group);
	mTouchKeyOverrideIntervalsGroup = (LinearLayout) mView.findViewById(R.id.touchkey_bln_override_group);

	mTouchKeyBLNMaxBlink = (SeekBar) mView.findViewById(R.id.touchkey_bln_max_blink);

	return true;
    }

    private void setData(){
	if(!initiateData()) return;

	updateSharedPrefs(mPreferences, TOUCHKEY_BLN, Utils.writeSYSValue(FILE_BLN_TOGGLE, mTouchKeyBLN.isChecked() ? "1" : "0"));

	updateSharedPrefs(mPreferences, TOUCHKEY_BLN_MAX_BLINK, Utils.writeSYSValue(FILE_BLN_MAX_BLINK, String.valueOf(mTouchKeyBLNMaxBlink.getProgress())));

	if(!mTouchKeyOverrideIntervals.isChecked()) updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_OVERRIDE, Utils.writeSYSValue(FILE_BLN_BLINK_OVERRIDE, " "));
	else updateSharedPrefs(mPreferences, TOUCHKEY_BLN_BLINK_OVERRIDE, Utils.writeSYSValue(FILE_BLN_BLINK_OVERRIDE, mTouchKeyONInterval.getText().toString() + LINE_SPACE + mTouchKeyOFFInterval.getText().toString()));
    }

    private void updateData(){
	if(!initiateData()) return;

	mTouchKeyBLNMaxBlink.setMax(1000);
	mTouchKeyBLNMaxBlink.setProgress(Integer.parseInt(Utils.readOneLine(FILE_BLN_MAX_BLINK)));

	mTouchKeyBLN.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_BLN_TOGGLE)));

	if(Utils.fileExists(FILE_BLN_BLINK_OVERRIDE)){
		String[] intervals = Utils.readOneLine(FILE_BLN_BLINK_OVERRIDE).split("\\s+");
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

	 updateDependencies();

    }

    private void updateDependencies(){
	if(!initiateData()) return;

	if(mTouchKeyOverrideIntervals.isChecked()){
		Utils.layoutEnable(mTouchKeyOverrideIntervalsGroup);
	} else {
		Utils.layoutDisable(mTouchKeyOverrideIntervalsGroup);
	}

	if(mTouchKeyBLN.isChecked()){
		Utils.layoutEnable(mTouchKeyBLNBlinkGroup);

	} else {
		Utils.layoutDisable(mTouchKeyBLNBlinkGroup);
		Utils.layoutDisable(mTouchKeyOverrideIntervalsGroup);

	}
	if(!Utils.fileExists(FILE_BLN_BLINK_OVERRIDE)) mTouchKeyOverrideIntervals.setEnabled(false);

	if(Utils.isInteger(mTouchKeyONInterval.getText().toString()) && Utils.isInteger(mTouchKeyOFFInterval.getText().toString())){
		if(mTouchKeyOverrideIntervals.isChecked() && (mTouchKeyONInterval.getText().toString()).equals("0") && (mTouchKeyOFFInterval.getText().toString()).equals("0")){
			mTouchKeyONInterval.setText("300");
			mTouchKeyOFFInterval.setText("1500");
		}
		updateText();
	}

    }

    private void updateText(){
	if(Utils.isInteger(mTouchKeyONInterval.getText().toString()) && Utils.isInteger(mTouchKeyOFFInterval.getText().toString())){
		String[] intervals = (mTouchKeyONInterval.getText().toString() + " " + mTouchKeyOFFInterval.getText().toString()).split("\\s+");
		mCurTouchKeyBLNBlink.setText(((mTouchKeyOverrideIntervals.isChecked() ? (String.valueOf((((Integer.parseInt(intervals[0]) + Integer.parseInt(intervals[1])) * mTouchKeyBLNMaxBlink.getProgress()) / 1000) / 60) + LINE_SPACE + getContext().getString(R.string.msg_minutes) + " - ") : getContext().getString(R.string.item_na) + " - ") + String.valueOf(mTouchKeyBLNMaxBlink.getProgress()) + LINE_SPACE + getContext().getString(R.string.msg_blinks)));
	}
	else mCurTouchKeyBLNBlink.setText((getContext().getString(R.string.item_na) + " - ") + String.valueOf(mTouchKeyBLNMaxBlink.getProgress()) + LINE_SPACE + getContext().getString(R.string.msg_blinks));
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(FILE_BLN_TOGGLE, preferences.getString(TOUCHKEY_BLN, "0"));
	Utils.SetSOBValue(FILE_BLN_MAX_BLINK, preferences.getString(TOUCHKEY_BLN_MAX_BLINK, "0"));
	Utils.SetSOBValue(FILE_BLN_BLINK_OVERRIDE, preferences.getString(TOUCHKEY_BLN_BLINK_OVERRIDE, ""));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
