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
import com.emman.tame.Settings;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class SOBPreference extends DialogPreference
		implements Resources {

    private View mView;

    private CheckBox mCPUSOBToggle;
    private Switch mSOBToggle;

    private SharedPreferences mPreferences;

    public SOBPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.sobdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;

	updateData();

	mSOBToggle.setOnClickListener(new View.OnClickListener() {
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

	mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

	mCPUSOBToggle = (CheckBox) mView.findViewById(R.id.sob_cpu);
	mSOBToggle = (Switch) mView.findViewById(R.id.sob);

	return true;
    }

    private void updateDependencies(){
	if(!initiateData()) return;

	mCPUSOBToggle.setEnabled(mSOBToggle.isChecked());
    }

    private void setData(){
	if(!initiateData()) return;
	
	updateSharedPrefs(mPreferences, SET_ON_BOOT, Utils.boolToString(mSOBToggle.isChecked()));
	updateSharedPrefs(mPreferences, CPU_SET_ON_BOOT, Utils.boolToString(mCPUSOBToggle.isChecked()));

	Settings.settingsCallback();
    }

    private void updateData(){
	if(!initiateData()) return;

	mSOBToggle.setChecked(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0")));
	mCPUSOBToggle.setChecked(Utils.stringToBool(mPreferences.getString(CPU_SET_ON_BOOT, "1")));

	updateDependencies();
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
