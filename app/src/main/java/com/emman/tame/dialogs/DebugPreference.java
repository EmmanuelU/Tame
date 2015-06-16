package com.emman.tame.dialogs;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
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

import java.io.File;

public class DebugPreference extends DialogPreference implements Resources {

    private View mView;

    private Button mClearLog;
    private Button mViewLog;
    private SharedPreferences mPreferences;
    private Switch mLog;
    private TextView mLogInfo;

    public DebugPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.debugdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateData();

	mClearLog.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Utils.toast(getContext(), getContext().getString(R.string.msg_cleared_tamelog));
			Utils.CMD(false, "echo > " + FILE_TAME_LOG);
		}
	});

	mViewLog.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Utils.openFile(getContext(), FILE_TAME_LOG, FILE_TEXT_FORMAT);
		}
	});

	mLog.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			updateSharedPrefs(mPreferences, TAME_DEBUG, Utils.boolToString(mLog.isChecked()));
			updateText();
		}
	});


    }

    private boolean initiateData(){
	mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

	mLog = (Switch) mView.findViewById(R.id.log);
	mLogInfo = (TextView) mView.findViewById(R.id.log_info);

	mClearLog = (Button) mView.findViewById(R.id.clearlog);
	mViewLog = (Button) mView.findViewById(R.id.viewlog);

	return true;
    }

    private void updateData(){
	if(!initiateData()) return;
	
	mLog.setChecked(Utils.stringToBool(mPreferences.getString(TAME_DEBUG, "0")));
	mLogInfo.setText(getContext().getString(R.string.item_sum_tamelog, FILE_TAME_LOG, getContext().getString(R.string.item_msg_tamelog)));

	updateText();
    }

    private void updateText(){
	if(!initiateData()) return;
	mLogInfo.setEnabled(mLog.isChecked());
    }

    private static void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
	super.onPrepareDialogBuilder(builder);
	builder.setNegativeButton(null, null);
    }

} 
