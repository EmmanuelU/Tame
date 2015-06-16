package com.emman.tame.dialogs;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
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
import android.widget.Button;
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
import com.emman.tame.MainActivity;
import com.emman.tame.fragments.CPUSettings;
import com.emman.tame.Settings;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class SOBPreference extends DialogPreference
		implements Resources {

    private View mView;

    private Button mSOBRestore;
    private CheckBox mCPUSOBToggle;
    private Switch mSOBToggle;

    private SharedPreferences mPreferences;

    public SOBPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.sobdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;

	updateData();

	mSOBToggle.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateSharedPrefs(mPreferences, SET_ON_BOOT, Utils.boolToString(mSOBToggle.isChecked()));
			updateDependencies();
		}
	});

	mCPUSOBToggle.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateSharedPrefs(mPreferences, CPU_SET_ON_BOOT, Utils.boolToString(mCPUSOBToggle.isChecked()));
		}
	});

	mSOBRestore.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mSOBRestore.setText(getContext().getString(R.string.msg_loading));
			mSOBRestore.setEnabled(false);
			MainActivity.ExecuteBootData(mPreferences);
			mSOBRestore.setText(getContext().getString(R.string.msg_done));
			Utils.toast(getContext(), getContext().getString(R.string.msg_restored_settings));
		}
	});
	
    }

    private boolean initiateData(){

	mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

	mSOBRestore = (Button) mView.findViewById(R.id.restore_sob);
	mCPUSOBToggle = (CheckBox) mView.findViewById(R.id.sob_cpu);
	mSOBToggle = (Switch) mView.findViewById(R.id.sob);

	return true;
    }

    private void updateDependencies(){
	if(!initiateData()) return;

	mCPUSOBToggle.setEnabled(mSOBToggle.isChecked());
    }

    private void updateData(){
	if(!initiateData()) return;

	mSOBToggle.setChecked(Utils.stringToBool(mPreferences.getString(SET_ON_BOOT, "0")));
	mCPUSOBToggle.setChecked(Utils.stringToBool(mPreferences.getString(CPU_SET_ON_BOOT, "1")));

	updateDependencies();
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
	super.onPrepareDialogBuilder(builder);
	builder.setNegativeButton(null, null);
    }

} 
