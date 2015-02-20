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

import com.emman.tame.fragments.GeneralSettings;

import com.emman.tame.R;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class DensPreference extends DialogPreference
		implements Resources {

    private View mView;

    private EditText mScreenDensity;
    private TextView mScreenDensityBackup;

    private SharedPreferences mPreferences;

    public DensPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.densdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateData();
	
	mScreenDensityBackup.setText(FILE_BACKUP_BUILD_PROP + "\nRename and replace your /system/build.prop with this backup to restore, incase of problems.\n");
	mScreenDensity.setSelection(mScreenDensity.getText().length());
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

	mScreenDensity = (EditText) mView.findViewById(R.id.density);
	mScreenDensityBackup = (TextView) mView.findViewById(R.id.backup);
	return true;
    }

    private void updateData(){
	if(!initiateData()) return;
	mScreenDensity.setText(Utils.readProp("ro.sf.lcd_density"));
    }

    private void setData(){
	if(!initiateData()) return;
	if(Utils.writeProp("ro.sf.lcd_density", mScreenDensity.getText().toString())){
		Utils.toast(getContext(), "Density changed to " + mScreenDensity.getText().toString() + "dp.");
		GeneralSettings.updateDens(mScreenDensity.getText().toString());
	}
	else Utils.toast(getContext(), "Failed to edit Build.prop");
    }

} 