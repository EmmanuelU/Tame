package com.emman.tame;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.emman.tame.Resources;
import com.emman.tame.dialogs.BLNPreference;
import com.emman.tame.dialogs.MPDecPreference;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
	
	if(AboutTame.isWild()) Utils.toast(this, "WildKernel Detected");

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(this);

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
		mTitle = getString(R.string.title_kernelsettings);
		fragment = new KernelSettings();
		break;

	    case 3:
		mTitle = getString(R.string.title_cpusettings);
		fragment = new CPUSettings();
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

    public static void SetOnBootData(SharedPreferences preferences){
	CPUSettings.SetOnBootData(preferences);
	BLNPreference.SetOnBootData(preferences);
	MPDecPreference.SetOnBootData(preferences);
    }

}
