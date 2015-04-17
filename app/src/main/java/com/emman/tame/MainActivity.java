/*
 * Tame - An Android Kernel Control application Copyright (C)
 * Emmanuel Utomi <emmanuelutomi@gmail.com> Copyright (C) 2014
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */


package com.emman.tame;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.emman.tame.dialogs.BLNPreference;
import com.emman.tame.dialogs.FastChargePreference;
import com.emman.tame.dialogs.EBLNPreference;
import com.emman.tame.dialogs.HBLNPreference;
import com.emman.tame.dialogs.CPUInputBoostPreference;
import com.emman.tame.dialogs.CPUInputBoostV2Preference;
import com.emman.tame.dialogs.CPUPolicyPreference;
import com.emman.tame.dialogs.GPUPreference;
import com.emman.tame.dialogs.IOPreference;
import com.emman.tame.dialogs.PanelUVPreference;
import com.emman.tame.dialogs.SMPPreference;
import com.emman.tame.dialogs.S2WPreference;

import com.emman.tame.fragments.AboutTame;
import com.emman.tame.fragments.CPUSettings;
import com.emman.tame.fragments.GeneralSettings;
import com.emman.tame.fragments.KernelSettings;
import com.emman.tame.fragments.SysFSExplorer;

import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, Resources {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Menu mMenu;

    private CheckBox mSetOnBoot;

    private TextView mSetOnBootNote;

    private SharedPreferences mPreferences;

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	context = this;
	if(!Utils.checkSu()){
		Intent intent = getIntent();
		Utils.notification(this, NotificationID.ROOTFAIL, intent, "Please restart me with Superuser access.");
		Utils.toast(this, "Fatal Error, see notification drawer.");
		this.finish();
	}
	mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(!Utils.packageExists(this, PACKAGE_SUPERSU) && Utils.isLollipop()){
		Intent intent = new Intent();
		intent.setData(Uri.parse(LINK_PACKAGE_SUPERSU));
		Utils.notification(this, NotificationID.ROOTFAIL, intent, "As of now, Tame requires the SuperSU binary on Lollipop to avoid any potential issues. Click me to be directed to the Market. Blame CyanogenMod's Privacy Guard.");
		Utils.toast(this, "Fatal Error.");
		this.finish();
	}
	
	if(Utils.isStringEmpty(mPreferences.getString(TAME_UID, ""))) updateSharedPrefs(mPreferences, TAME_UID, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
	else if(!mPreferences.getString(TAME_UID, "").equals(Secure.getString(this.getContentResolver(), Secure.ANDROID_ID))){
		Utils.notification(this, NotificationID.UID, null, "You previously used Tame from a different device. While you shouldn't run into any problems, you may consider resetting my data.");
		updateSharedPrefs(mPreferences, TAME_UID, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
	}

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
	
	if(AboutTame.isWild()) Utils.toast(this, "WildKernel Detected");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
	Fragment fragment = null;
        switch (number) {
	    case 1:
		mTitle = getString(R.string.app_name);
		fragment = new AboutTame();
		 break;
	    case 2:
		mTitle = getString(R.string.title_generalsettings);
		fragment = new GeneralSettings();
		break;

	    case 3:
		mTitle = getString(R.string.title_kernelsettings);
		fragment = new KernelSettings();
		break;

	    case 4:
		mTitle = getString(R.string.title_cpusettings);
		fragment = new CPUSettings();
		break;

	    case 5:
		mTitle = getString(R.string.title_sysfs);
		fragment = new SysFSExplorer();
		break;
        }
	if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
		        .replace(R.id.container, fragment)
		        .commit();
	}
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();

	mSetOnBootNote = (TextView) findViewById(R.id.sobnote);

	actionBar.setDisplayShowCustomEnabled(true);
	actionBar.setCustomView(R.layout.action_bar);
	
	mSetOnBoot = (CheckBox) getActionBar().getCustomView().findViewById(R.id.set_on_boot);

	mSetOnBoot.setChecked(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0")));

	if(mSetOnBootNote != null){
		if(mSetOnBoot.isChecked()) mSetOnBootNote.setVisibility(View.VISIBLE);
		else mSetOnBootNote.setVisibility(View.GONE);
	}

	mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
	    @Override
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		updateSharedPrefs(mPreferences, SET_ON_BOOT, Utils.boolToString(isChecked));
		if(mSetOnBootNote != null){
			if(isChecked) mSetOnBootNote.setVisibility(View.VISIBLE);
			else mSetOnBootNote.setVisibility(View.GONE);
		}
	    }
	});

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
	    mMenu = menu;
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //if (id == R.id.set_on_boot) {
        //    return true;
       // }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

    public static void ExecuteBootData(SharedPreferences preferences){
	Utils.CMD("rm -rf " + FILE_SET_ON_BOOT, false);
	CPUPolicyPreference.SetOnBootData(preferences);
	CPUInputBoostPreference.SetOnBootData(preferences);
	CPUInputBoostV2Preference.SetOnBootData(preferences);
	SMPPreference.SetOnBootData(preferences);
	PanelUVPreference.SetOnBootData(preferences);
	CPUSettings.SetOnBootData(preferences);
	IOPreference.SetOnBootData(preferences);
	BLNPreference.SetOnBootData(preferences);
	EBLNPreference.SetOnBootData(preferences);
	HBLNPreference.SetOnBootData(preferences);
	S2WPreference.SetOnBootData(preferences);
	GPUPreference.SetOnBootData(preferences);
	FastChargePreference.SetOnBootData(preferences);
	Utils.CMD("sh " + FILE_SET_ON_BOOT, true);
    }

    public static Context getContext(){
	return context;
    }


}
