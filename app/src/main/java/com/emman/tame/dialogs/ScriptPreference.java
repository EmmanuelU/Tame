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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.emman.tame.R;
import com.emman.tame.fragments.CPUSettings;
import com.emman.tame.MainActivity;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class ScriptPreference extends DialogPreference
		implements Resources {

    private View mView;

    private Button mScriptRestore;
    private EditText mCommands;
    private Switch mScript;

    private SharedPreferences mPreferences;

    public ScriptPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.scriptdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateData();

	mCommands.setScroller(new Scroller(getContext()));
	mCommands.setMaxLines(5);
	mCommands.setVerticalScrollBarEnabled(true);
	mCommands.setMovementMethod(new ScrollingMovementMethod());

	mScript.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateDependencies();
		}
	});

	mScriptRestore.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mScriptRestore.setText(getContext().getString(R.string.msg_loading));
			mScriptRestore.setEnabled(false);
			ExecuteBootCommands(mPreferences);
			mScriptRestore.setText(getContext().getString(R.string.msg_done));
			Utils.toast(getContext(), getContext().getString(R.string.msg_restored_cmd));
		}
	});

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

	mCommands = (EditText) mView.findViewById(R.id.commands);
	mScript = (Switch) mView.findViewById(R.id.scripttoggle);
	mScriptRestore = (Button) mView.findViewById(R.id.restore_cmd);

	return true;
    }

    private void setData(){
	if(!initiateData()) return;
	
	if(mScript.isChecked() && Utils.isStringEmpty(mCommands.getText().toString())) mScript.setChecked(false);
	updateSharedPrefs(mPreferences, RUN_AT_BOOT_COMMANDS, mCommands.getText().toString());
	updateSharedPrefs(mPreferences, RUN_AT_BOOT, Utils.boolToString(mScript.isChecked()));

    }

    private void updateDependencies(){
	if(!initiateData()) return;

	mCommands.setEnabled(mScript.isChecked());
    }

    private void updateData(){
	if(!initiateData()) return;

	mScript.setChecked(Utils.stringToBool(mPreferences.getString(RUN_AT_BOOT, "0")));

	updateDependencies();

	mCommands.setText(mPreferences.getString(RUN_AT_BOOT_COMMANDS, ""));
	mCommands.setSelection(mCommands.getText().length());
    }

    public static void ExecuteBootCommands(SharedPreferences preferences){
	String[] commands = preferences.getString(RUN_AT_BOOT_COMMANDS, "").split(System.getProperty("line.separator"));
	Utils.log(null, preferences, "-RAB-", Utils.arrayToString(commands));
	Utils.CMD(true, commands);
	updateSharedPrefs(preferences, RUN_AT_BOOT_TS, new SimpleDateFormat("MMMM d, yyyy - h:mma").format(new Date()));
    }

    private static void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
