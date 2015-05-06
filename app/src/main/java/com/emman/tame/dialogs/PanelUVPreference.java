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

import com.emman.tame.R;
import com.emman.tame.fragments.KernelSettings;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class PanelUVPreference extends DialogPreference
		implements Resources {

    //support for multiple in future
    public static String mPanelUVFile = FILE_CELOX_DISPLAY_UV;

    private SeekBar mPanelUVSeekBar;
    private TextView mPanelUVSeekBarText;

    private SharedPreferences mPreferences;

    private View mView;

    public PanelUVPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.panel_uv_dialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateDependencies();

	mPanelUVSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

	    @Override
	    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			String prefix;
			float uvPrefixfloat = (float) mPanelUVSeekBar.getProgress() / mPanelUVSeekBar.getMax();
			if(mPanelUVSeekBar.getProgress() > 0){
				if(uvPrefixfloat < 0.2) prefix = getContext().getString(R.string.msg_miniscule);
				else if(uvPrefixfloat < 0.4) prefix = getContext().getString(R.string.msg_small);
				else if(uvPrefixfloat < 0.6) prefix = getContext().getString(R.string.msg_moderate);
				else if(uvPrefixfloat < 0.8) prefix = getContext().getString(R.string.msg_high);
				else prefix = getContext().getString(R.string.msg_excessive);
				mPanelUVSeekBarText.setText(prefix + LINE_SPACE+ getContext().getString(R.string.msg_undervolt) + " -" + String.valueOf(mPanelUVSeekBar.getProgress()) + "mV");
			} else mPanelUVSeekBarText.setText(getContext().getString(R.string.msg_default_volt));
	    }

	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) {

	    }

	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
		
	    }

	});
	
	updateData();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();

	KernelSettings.panelUpdate();
    }

    private boolean initiateData(){

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mPanelUVSeekBar = (SeekBar) mView.findViewById(R.id.panel_uv_slider);
	mPanelUVSeekBarText = (TextView) mView.findViewById(R.id.panel_uv_slider_text);
	

	return true;
    }

    private void setData(){
	if(!initiateData()) return;
	
	updateSharedPrefs(mPreferences, SAVED_DISPLAY_UV, Utils.writeSYSValue(mPanelUVFile, String.valueOf(mPanelUVSeekBar.getProgress())));
	
    }

    private void updateData(){
	if(!initiateData()) return;

	mPanelUVSeekBar.setProgress(Integer.parseInt(Utils.readOneLine(mPanelUVFile)));

    }

    private void updateDependencies(){
	if(!initiateData()) return;

	if(mPanelUVFile.equals(FILE_CELOX_DISPLAY_UV)) mPanelUVSeekBar.setMax(500);

    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(FILE_CELOX_DISPLAY_UV, preferences.getString(SAVED_DISPLAY_UV, ""));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
