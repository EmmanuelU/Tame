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
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class FastChargePreference extends DialogPreference
		implements Resources {

    private View mView;

    private Spinner mFastCharge;
    private Switch mFastChargeToggle;
    private String[] mFastChargeMAList;

    private boolean mFastChargeOld;

    private SharedPreferences mPreferences;

    public FastChargePreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.fastchargedialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	initiateData();

	mFastChargeOld = !Utils.fileExists(AVAILABLE_FAST_CHARGE_LIST);

	List<String> list;
	if(mFastChargeOld)
		list = new ArrayList<String>(Arrays.asList(getContext().getString(R.string.item_max_current)));
	else
		list = new ArrayList<String>(Arrays.asList(Utils.getFilemA(AVAILABLE_FAST_CHARGE_LIST)));
	
	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
		android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	mFastCharge.setAdapter(dataAdapter);

	mFastChargeToggle.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});
	
	updateData();
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

	mFastCharge = (Spinner) mView.findViewById(R.id.fast_charge);
	mFastChargeToggle = (Switch) mView.findViewById(R.id.fast_charge_toggle);
	
	if(mFastChargeOld)
		mFastChargeMAList = getContext().getString(R.string.item_max_current).split("\\s+"); //will always be one line anyway >:)
	else
		mFastChargeMAList = Utils.readOneLine(AVAILABLE_FAST_CHARGE_LIST).split("\\s+");

	return true;
    }

    private void setData(){
	if(!initiateData()) return;
	
	if(mFastCharge.getSelectedItemId() > 0){
		updateSharedPrefs(mPreferences, SAVED_FORCE_FAST_CHARGE, Utils.writeSYSValue(FORCE_FAST_CHARGE_FILE, mFastChargeToggle.isChecked() ? "2" : "0"));
		updateSharedPrefs(mPreferences, SAVED_FORCE_FAST_CHARGE_LEVEL, Utils.writeSYSValue(FORCE_FAST_CHARGE_LEVEL_FILE, mFastChargeMAList[(int) mFastCharge.getSelectedItemId() - 1]));
	} else if(mFastChargeOld){
		updateSharedPrefs(mPreferences, SAVED_FORCE_FAST_CHARGE, Utils.writeSYSValue(FORCE_FAST_CHARGE_FILE, mFastChargeToggle.isChecked() ? "1" : "0"));
	} else updateSharedPrefs(mPreferences, SAVED_FORCE_FAST_CHARGE, Utils.writeSYSValue(FORCE_FAST_CHARGE_FILE, "0"));
	
    }

    private void updateData(){
	if(!initiateData()) return;

	if(mFastChargeOld)
		mFastChargeToggle.setChecked(Utils.stringToBool(Utils.readOneLine(FORCE_FAST_CHARGE_FILE)));
	else
		mFastChargeToggle.setChecked(Utils.readOneLine(FORCE_FAST_CHARGE_FILE).equals("2") ? true : false);

	mFastCharge.setSelection(Utils.getArrayIndex(mFastChargeMAList, Utils.readOneLine(FORCE_FAST_CHARGE_LEVEL_FILE)) + 1);

	updateDependencies();
    }

    private void updateDependencies(){
	if(!initiateData()) return;

	if(mFastChargeToggle.isChecked()){
		mFastCharge.setEnabled(true);
	} else {
		mFastCharge.setEnabled(false);
	}
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(FORCE_FAST_CHARGE_FILE, preferences.getString(SAVED_FORCE_FAST_CHARGE, "0"));
	Utils.SetSOBValue(FORCE_FAST_CHARGE_LEVEL_FILE, preferences.getString(SAVED_FORCE_FAST_CHARGE_LEVEL, ""));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
