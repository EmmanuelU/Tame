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
import android.widget.ListView;
import android.widget.Toast;
import android.util.AttributeSet;
import android.util.Log;

import com.emman.tame.R;
import com.emman.tame.Utils;

public class BLNPreference extends DialogPreference {

    public static final String KEY_TOUCHKEY_BLN = "touchkey_bln";
    public static final String FILE_BLN_TOGGLE = "/sys/class/misc/backlightnotification/enabled";
    private CheckBox mTouchKeyBLN;

    public BLNPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.blndialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	updateprefs(view);

	mTouchKeyBLN.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Utils.writeValue(FILE_BLN_TOGGLE, mTouchKeyBLN.isChecked() ? "1" : "0");
			updateprefs(view);
		}
	});
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
    }

    private void updateprefs(final View view){
	if(!Utils.fileExists(FILE_BLN_TOGGLE)) return;
	mTouchKeyBLN = (CheckBox) view.findViewById(R.id.touchkey_bln);

	mTouchKeyBLN.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_BLN_TOGGLE)));
	
    }

} 
