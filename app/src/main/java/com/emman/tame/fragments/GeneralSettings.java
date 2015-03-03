package com.emman.tame.fragments;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import com.emman.tame.R;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class GeneralSettings extends PreferenceFragment
		implements Resources {

    private static Preference mDensDialog;

    private SharedPreferences mPreferences;

    private PreferenceScreen prefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_settings);

        prefSet = getPreferenceScreen();

	updateData();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        return true;
    }

    private boolean initiateData(){
	mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

	mDensDialog = findPreference("densdialog");

	return true;
    }

    public static void updateDensSummary(String summary){
	if(mDensDialog.isEnabled()) mDensDialog.setSummary(summary);
    }

    private void updateData(){
	if(!initiateData()) return;
	mDensDialog.setSummary(Utils.readProp("ro.sf.lcd_density") + "dpi");
    }

    private void setData(){
	if(!initiateData()) return;

    }

    public static void SetOnBootData(SharedPreferences preferences){
	
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

}
