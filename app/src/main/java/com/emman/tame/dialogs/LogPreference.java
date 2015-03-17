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

public class LogPreference extends DialogPreference
		implements Resources {

    private View mView;

    private Button mLogcat;
    private Button mLastKmsg;

    private Boolean mLogcatDumped = false;
    private Boolean mLastKmsgDumped = false;

    public LogPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.logdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	initiateData();

	mLogcat.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!mLogcatDumped){
				Utils.CMD("logcat -d  > "+ TAME_LOGCAT, true);
				Utils.toast(getContext(), "Logcat saved to " + TAME_LOGCAT);
				mLogcat.setText("View " + TAME_LOGCAT);
				mLogcatDumped = true;
			} else {
				Utils.openFile(getContext(), TAME_LOGCAT, FILE_TEXT_FORMAT);
			}
		}
	});


	mLastKmsg.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(!mLastKmsgDumped){
				Utils.CMD("cp -f " + LAST_KMSG + LINE_SPACE + TAME_LAST_KMSG, true);
				Utils.toast(getContext(), "KMSG saved to " + TAME_LAST_KMSG);
				mLastKmsg.setText("View " + TAME_LAST_KMSG);
				mLastKmsgDumped = true;
			} else {
				Utils.openFile(getContext(), TAME_LAST_KMSG, FILE_TEXT_FORMAT);
			}
		}
	});

    }

    private boolean initiateData(){

	mLogcat = (Button) mView.findViewById(R.id.logcat);
	mLastKmsg = (Button) mView.findViewById(R.id.last_kmsg);

	return true;
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
	super.onPrepareDialogBuilder(builder);
	builder.setNegativeButton(null, null);
    }

} 
