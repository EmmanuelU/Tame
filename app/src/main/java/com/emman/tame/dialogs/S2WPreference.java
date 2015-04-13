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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import com.emman.tame.R;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class S2WPreference extends DialogPreference
		implements Resources {

    private View mView;

    private Switch mS2W;
    private Switch mS2S;
    private CheckBox mS2WSensitive;

    private SharedPreferences mPreferences;

    public S2WPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.s2wdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateData();

	mS2W.setOnClickListener(new View.OnClickListener() {
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
	if(!Utils.fileExists(FILE_S2W_TOGGLE)) return false;

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mS2W = (Switch) mView.findViewById(R.id.s2w);
	mS2S = (Switch) mView.findViewById(R.id.s2s);
	mS2WSensitive = (CheckBox) mView.findViewById(R.id.s2w_sensitive);

	return true;
    }

    private void setData(){
	if(!initiateData()) return;

	updateSharedPrefs(mPreferences, S2W, Utils.writeSYSValue(FILE_S2W_TOGGLE, mS2W.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, S2S, Utils.writeSYSValue(FILE_S2S_TOGGLE, mS2S.isChecked() ? "1" : "0"));
	updateSharedPrefs(mPreferences, S2W_SENSITIVE, Utils.writeSYSValue(FILE_S2W_SENSITIVE, mS2WSensitive.isChecked() ? "1" : "0"));

    }

    private void updateData(){
	if(!initiateData()) return;

	mS2W.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_S2W_TOGGLE)));
	mS2S.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_S2S_TOGGLE)));
	mS2WSensitive.setChecked(Utils.stringToBool(Utils.readOneLine(FILE_S2W_SENSITIVE)));

	updateDependencies();

    }

    private void updateDependencies(){
	if(!initiateData()) return;
	if(!Utils.fileExists(FILE_S2W_SENSITIVE)) mS2WSensitive.setEnabled(false);
	if(!Utils.fileExists(FILE_S2S_TOGGLE)) mS2S.setEnabled(false);
    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBValue(FILE_S2W_TOGGLE, preferences.getString(S2W, "1"));
	Utils.SetSOBValue(FILE_S2S_TOGGLE, preferences.getString(S2S, "0"));
	Utils.SetSOBValue(FILE_S2W_SENSITIVE, preferences.getString(S2W_SENSITIVE, "0"));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
