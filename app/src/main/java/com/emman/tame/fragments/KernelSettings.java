package com.emman.tame.fragments;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
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

import com.emman.tame.MainActivity;
import com.emman.tame.fragments.AboutTame;
import com.emman.tame.dialogs.BLNPreference;
import com.emman.tame.dialogs.PanelUVPreference;
import com.emman.tame.R;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class KernelSettings extends PreferenceFragment
		implements Resources {


    private Preference mBlnDialog;
    private Preference mEBlnDialog;
    private Preference mFastChargeDialog;
    public static Preference mGPUDialog;
    private Preference mHBlnDialog;
    public static Preference mIODialog;
    public static Preference mPanelUVDialog;
    private Preference mS2WDialog;
    public static Preference mTCPDialog;

    private SharedPreferences mPreferences;

    private PreferenceScreen prefSet;

   public static String[] availableIOSchedulers;
   public static String availableIOSchedulersLine;
   public static int bropen, brclose;
   public static String currentIOScheduler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	MainActivity.setActionBarTitle(getActivity().getString(R.string.page_kernelsettings), 2);
	MainActivity.setOnBackPressedListener(new MainActivity.overrideBackListener() {
		@Override
		public void onBackPressed() {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, new AboutTame()).commit();
			
		}
	});
	
	availableIOSchedulersLine = Utils.readOneLine(IOSCHED_LIST_FILE);
        addPreferencesFromResource(R.xml.kernel_settings);

        prefSet = getPreferenceScreen();

	updateData();
    }

    private boolean initiateData(){
	mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

	mBlnDialog = findPreference("blndialog");
	mEBlnDialog = findPreference("eblndialog");
	mFastChargeDialog = findPreference("fast_charge_dialog");
	mGPUDialog = findPreference("gpu_dialog");
	mHBlnDialog = findPreference("hblndialog");
	mIODialog = findPreference("iosched");
	mPanelUVDialog = findPreference("panel_uv_dialog");
	mS2WDialog = findPreference("s2wdialog");
	mTCPDialog = findPreference("tcp_dialog");

	return true;
    }

    private void updateDependencies(){
	if(!initiateData()) return;

	if(!Utils.fileExists(FILE_BLN_TOGGLE)){
		mBlnDialog.setEnabled(false);
	}
	if(!Utils.fileExists(FILE_HBLN_BLINK_OVERRIDE)){
		prefSet.removePreference(mHBlnDialog);
	} else prefSet.removePreference(mBlnDialog);
	if(!Utils.fileExists(FILE_EBLN)){
		prefSet.removePreference(mEBlnDialog);
	} else prefSet.removePreference(mBlnDialog);
	
	if(!Utils.fileExists(FILE_S2W_TOGGLE) && !Utils.fileExists(FILE_DT2W_TOGGLE)) mS2WDialog.setEnabled(false);

	if(!Utils.fileExists(GPU_MAX_FREQ_FILE)) mGPUDialog.setEnabled(false);

	if(!Utils.fileExists(FORCE_FAST_CHARGE_FILE)) mFastChargeDialog.setEnabled(false);

	if(!Utils.fileExists(FILE_CELOX_DISPLAY_UV)){
		mPanelUVDialog.setEnabled(false);
		mPanelUVDialog.setSummary(getString(R.string.msg_default_volt));
	}
    }

    private void updateData(){
	if(!initiateData()) return;
	updateDependencies();

	GPUupdate();
	IOupdate(availableIOSchedulersLine);
	panelUpdate();
	TCPupdate();
    }

    public static void panelUpdate(){
	if(Utils.fileExists(PanelUVPreference.mPanelUVFile)){
		if(Integer.parseInt(Utils.readOneLine(PanelUVPreference.mPanelUVFile)) > 0)
			mPanelUVDialog.setSummary(MainActivity.getContext().getString(R.string.msg_undervolt) + " -" + Utils.readOneLine(PanelUVPreference.mPanelUVFile) + "mV");
		else mPanelUVDialog.setSummary(MainActivity.getContext().getString(R.string.msg_default_volt));
	}
    }

    public static void GPUupdate(){
	if(Utils.fileExists(GPU_MAX_FREQ_FILE)) mGPUDialog.setSummary(String.format("%s", Utils.toGPUMHz(Utils.readOneLine(GPU_MAX_FREQ_FILE))));
    }

    public static void TCPupdate(){
	mTCPDialog.setSummary(Utils.readSYSCTLValue(TCP_PROTOCOL));
    }

    public static void IOupdate(String schedulers){
	availableIOSchedulers = schedulers.replace("[", "").replace("]", "").split(" ");
	bropen = availableIOSchedulersLine.indexOf("[");
	brclose = availableIOSchedulersLine.lastIndexOf("]");
	if (bropen >= 0 && brclose >= 0) currentIOScheduler = availableIOSchedulersLine.substring(bropen + 1, brclose);

	mIODialog.setSummary(currentIOScheduler);
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

}
