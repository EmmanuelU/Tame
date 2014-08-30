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
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import com.emman.tame.R;
import com.emman.tame.Utils;
import com.emman.tame.Resources;

public class MPDecPreference extends DialogPreference
		implements Resources {

    private View mView;

    private CheckBox mMPDec;
    private CheckBox mMPDecScroff;

    private LinearLayout mMPDecGroup;

    private SharedPreferences mPreferences;

    public MPDecPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.mpdecdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateData();

	mMPDec.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();
	else updateData();

    }

    private boolean initiateData(){
	if(!Utils.fileExists(FILE_BLN_TOGGLE)) return false;

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mMPDec = (CheckBox) mView.findViewById(R.id.mpdec);
	mMPDecScroff = (CheckBox) mView.findViewById(R.id.mpdec_scroff);

	mMPDecGroup = (LinearLayout) mView.findViewById(R.id.mpdec_group);

	return true;
    }

    private void setData(){
	if(!initiateData()) return;

	updateSharedPrefs(mPreferences, MPDEC, Utils.writeValueSU(FILE_MPDEC_TOGGLE, mMPDec.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, MPDEC_SCROFF, Utils.writeValueSU(FILE_MPDEC_SCROFF, mMPDecScroff.isChecked() ? "1" : "0"));

    }

    private void updateData(){
	if(!initiateData()) return;

	mMPDec.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_MPDEC_TOGGLE)));
	mMPDecScroff.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_MPDEC_SCROFF)));

	updateDependencies();

    }

    private void updateDependencies(){
	if(!initiateData()) return;
	
	if(mMPDec.isChecked()){
		Utils.layoutEnable(mMPDecGroup);
	} else {
		Utils.layoutDisable(mMPDecGroup);
	}

    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.writeValue(FILE_MPDEC_TOGGLE, preferences.getString(MPDEC, "1"));
	Utils.writeValue(FILE_MPDEC_SCROFF, preferences.getString(MPDEC_SCROFF, "1"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
