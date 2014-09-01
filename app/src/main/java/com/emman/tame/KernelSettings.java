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

public class KernelSettings extends PreferenceFragment
		implements Resources {

    private static final String TAG = "Tame";

    private Preference mBlnDialog;

    private Preference mMPDecDialog;

    private Preference mS2WDialog;

    private SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.kernel_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

	updateDependencies();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        return true;
    }

    private boolean initiateData(){
	mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

	mBlnDialog = findPreference("blndialog");
	mMPDecDialog = findPreference("mpdecdialog");
	mS2WDialog = findPreference("s2wdialog");

	return true;
    }

    private void updateDependencies(){
	if(!initiateData()) return;

	if(!Utils.fileExists(FILE_BLN_TOGGLE)) mBlnDialog.setEnabled(false);
	if(!Utils.fileExists(FILE_MPDEC_TOGGLE)) mMPDecDialog.setEnabled(false);
	if(!Utils.fileExists(FILE_S2W_TOGGLE)) mS2WDialog.setEnabled(false);
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
