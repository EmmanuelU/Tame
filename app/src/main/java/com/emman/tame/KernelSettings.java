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

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class KernelSettings extends PreferenceFragment {

    private static final String TAG = "Tame";

    public static final String KEY_TOUCHKEY_BLN = "touchkey_bln";
    private static final String FILE_BLN_TOGGLE = "/sys/class/misc/backlightnotification/enabled";
    private CheckBoxPreference mTouchKeyBLN;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.kernel_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

	updateprefs();

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        String boxValue;
        String key = preference.getKey();

        Log.w(TAG, "key: " + key);

        if (preference == mTouchKeyBLN) {
            Utils.writeValue(FILE_BLN_TOGGLE, mTouchKeyBLN.isChecked() ? "1" : "0");
        }

	updateprefs();

        return true;
    }

    private void updateprefs(){
	mTouchKeyBLN = (CheckBoxPreference) findPreference(KEY_TOUCHKEY_BLN);

	if(Utils.fileExists(FILE_BLN_TOGGLE)){
		mTouchKeyBLN.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_BLN_TOGGLE)));
	}
	else mTouchKeyBLN.setEnabled(false);
    }

}
