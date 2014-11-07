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
import com.emman.tame.fragments.CPUSettings;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class IOPreference extends DialogPreference
		implements Resources {

    private View mView;

    private Spinner mIOSched;

    private SharedPreferences mPreferences;

    String[] availableIOSchedulers;
    String availableIOSchedulersLine;
    int bropen, brclose;
    String currentIOScheduler;

    public IOPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.iodialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	if(!initiateData()) return;

	List<String> list = new ArrayList<String>(Arrays.asList(availableIOSchedulers));
	
	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
		android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(R.layout.bigspinlayout);
	mIOSched.setAdapter(dataAdapter);

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

	mIOSched = (Spinner) mView.findViewById(R.id.iosched);

	availableIOSchedulersLine = Utils.readOneLine(IOSCHED_LIST_FILE);
	availableIOSchedulers = availableIOSchedulersLine.replace("[", "").replace("]", "").split(" ");
	bropen = availableIOSchedulersLine.indexOf("[");
	brclose = availableIOSchedulersLine.lastIndexOf("]");
	if (bropen >= 0 && brclose >= 0) currentIOScheduler = availableIOSchedulersLine.substring(bropen + 1, brclose);

	return true;
    }

    private void setData(){
	if(!initiateData()) return;
	
	updateSharedPrefs(mPreferences, SAVED_IOSCHED, Utils.writeSYSValue(IOSCHED_LIST_FILE, availableIOSchedulers[(int) mIOSched.getSelectedItemId()]));
	
    }

    private void updateData(){
	if(!initiateData()) return;
	mIOSched.setSelection(Utils.getArrayIndex(availableIOSchedulers, currentIOScheduler));
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(IOSCHED_LIST_FILE, preferences.getString(SAVED_IOSCHED, ""));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
