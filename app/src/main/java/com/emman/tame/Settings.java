package com.emman.tame;

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
import android.preference.SwitchPreference;
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

import com.emman.tame.dialogs.BLNPreference;
import com.emman.tame.dialogs.PanelUVPreference;
import com.emman.tame.R;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class Settings extends PreferenceActivity
		implements Resources {

    private PreferenceScreen mPref;
    private SharedPreferences mPreferences;

    private SwitchPreference mCheckUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.tame_settings);

	getActionBar().setDisplayHomeAsUpEnabled(true);

	updateData();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	if(!initiateData()) return false;

	if (preference == mCheckUpdate){
		updateSharedPrefs(mPreferences, CHECK_UPDATE_AT_BOOT, Utils.boolToString(mCheckUpdate.isChecked()));
	}

        return true;
    }

    private boolean initiateData(){
	mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	mPref = getPreferenceScreen();

	mCheckUpdate = (SwitchPreference) mPref.findPreference("check_updates");

	return true;
    }

    private void updateData(){
	if(!initiateData()) return;
	
	mCheckUpdate.setChecked(Utils.stringToBool(mPreferences.getString(CHECK_UPDATE_AT_BOOT, "1")));
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
