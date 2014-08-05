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
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Toast;
import android.util.AttributeSet;
import android.util.Log;

import com.emman.tame.R;
import com.emman.tame.Utils;

public class BLNPreference extends DialogPreference {

    private CheckBox mTouchKeyBLN;
    public static final String KEY_TOUCHKEY_BLN = "touchkey_bln";
    public static final String FILE_BLN_TOGGLE = "/sys/class/misc/backlightnotification/enabled";

    private SeekBar mTouchKeyBLNMaxBlink;
    private TextView mCurTouchKeyBLNBlink;
    public static final String FILE_BLN_MAX_BLINK = "/sys/class/misc/backlightnotification/max_blink_count";

    public static final String FILE_BLN_BLINK_OVERRIDE = "/sys/class/misc/backlightnotification/override_blink_interval";

    private CheckBox mTouchKeyOverrideIntervals;
    private EditText mTouchKeyONInterval;
    private EditText mTouchKeyOFFInterval;

    private View mView;

    public BLNPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.blndialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateprefs();

	mTouchKeyBLN.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updatedependencies();
		}
	});

	mTouchKeyOverrideIntervals.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updatedependencies();
		}
	});

	mTouchKeyBLNMaxBlink.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

	    @Override
	    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		mCurTouchKeyBLNBlink.setText(("Timeout After: " + String.valueOf(mTouchKeyBLNMaxBlink.getProgress()) + " Blinks").replace("After: ", "After:     "));
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
	if(positiveResult) setprefs();
	else updateprefs();

	
    }

    private boolean initiateprefs(){
	if(!Utils.fileExists(FILE_BLN_TOGGLE)) return false;

	mTouchKeyBLN = (CheckBox) mView.findViewById(R.id.touchkey_bln);
	mTouchKeyOverrideIntervals = (CheckBox) mView.findViewById(R.id.touchkey_bln_override_interval);

	mTouchKeyONInterval = (EditText) mView.findViewById(R.id.touchkey_bln_on_interval);
	mTouchKeyOFFInterval = (EditText) mView.findViewById(R.id.touchkey_bln_off_interval);

	mCurTouchKeyBLNBlink = (TextView) mView.findViewById(R.id.cur_touchkey_bln_blink);

	mTouchKeyBLNMaxBlink = (SeekBar) mView.findViewById(R.id.touchkey_bln_max_blink);

	return true;
    }

    private void setprefs(){
	if(!initiateprefs()) return;

	Utils.writeValue(FILE_BLN_TOGGLE, mTouchKeyBLN.isChecked() ? "1" : "0");
	Utils.writeValue(FILE_BLN_MAX_BLINK, String.valueOf(mTouchKeyBLNMaxBlink.getProgress()));

	if(!mTouchKeyOverrideIntervals.isChecked()) Utils.writeValue(FILE_BLN_BLINK_OVERRIDE, "0 0");
	else Utils.writeValue(FILE_BLN_BLINK_OVERRIDE, mTouchKeyONInterval.getText() + " " + mTouchKeyOFFInterval.getText());

	updateprefs();
    }

    private void updateprefs(){
	if(!initiateprefs()) return;

	mTouchKeyBLNMaxBlink.setMax(500);
	mTouchKeyBLNMaxBlink.setProgress(Integer.parseInt(Utils.readOneLine(FILE_BLN_MAX_BLINK)));

	mTouchKeyBLN.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_BLN_TOGGLE)));

	String[] intervals = Utils.readOneLine(FILE_BLN_BLINK_OVERRIDE).split("\\s+");
	if((Integer.parseInt(intervals[0]) <= 0) || (Integer.parseInt(intervals[1]) <= 0)) mTouchKeyOverrideIntervals.setChecked(false);
	else mTouchKeyOverrideIntervals.setChecked(true);

	mCurTouchKeyBLNBlink.setText(("Timeout After: " + String.valueOf(mTouchKeyBLNMaxBlink.getProgress()) + " Blinks").replace("After: ", "After:     "));

	 updatedependencies();

    }

    private void updatedependencies(){
	if(!initiateprefs()) return;

	if(mTouchKeyBLN.isChecked()){
		mTouchKeyBLNMaxBlink.setEnabled(true);
	} else {
		mTouchKeyBLNMaxBlink.setEnabled(false);
	}

	if(!Utils.fileExists(FILE_BLN_BLINK_OVERRIDE)) mTouchKeyOverrideIntervals.setEnabled(false);

	String[] intervals = Utils.readOneLine(FILE_BLN_BLINK_OVERRIDE).split("\\s+");

	mTouchKeyONInterval.setText(intervals[0]);
	mTouchKeyOFFInterval.setText(intervals[1]);

	if(!mTouchKeyOverrideIntervals.isChecked()) Utils.writeValue(FILE_BLN_BLINK_OVERRIDE, "0 0");

	if(mTouchKeyOverrideIntervals.isChecked()){
		mTouchKeyONInterval.setEnabled(true);
		mTouchKeyOFFInterval.setEnabled(true);
	} else {
		mTouchKeyONInterval.setEnabled(false);
		mTouchKeyOFFInterval.setEnabled(false);
	}
    }

} 
