package com.emman.tame.fragments;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.emman.tame.fragments.GeneralSettings;
import com.emman.tame.MainActivity;
import com.emman.tame.R;
import com.emman.tame.utils.NotificationID;
import com.emman.tame.utils.PropArrayAdapter;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;
import com.stericson.RootTools.RootTools;

public class BuildPropEditor extends ListFragment 
		implements Resources {

    private View mView;
    private PropArrayAdapter adapter;
    private SharedPreferences mPreferences;
    private String[] mPropEntries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	MainActivity.setActionBarTitle(getActivity().getString(R.string.item_buildprop));
	MainActivity.setOnBackPressedListener(new MainActivity.overrideBackListener() {
		@Override
		public void onBackPressed() {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, new GeneralSettings()).commit();
			Utils.CMD(false, "rm -rf /sdcard/Tame/tmp.prop");
		}
	});

	mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

	fill();
	Utils.toast(getActivity(), getActivity().getString(R.string.msg_edit_caution));

    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(adapter.getItem(position).equals(getActivity().getString(R.string.item_retain_prop))){
			//settings
			final Dialog dialog = new Dialog(getActivity());
			dialog.setContentView(R.layout.propsettingsdialog);
			final Switch mRetainToggle = (Switch) dialog.findViewById(R.id.retaintoggle);
			final EditText mSavedProps = (EditText) dialog.findViewById(R.id.properties);
			final Button mApplyProps = (Button) dialog.findViewById(R.id.apply_props);

			dialog.setTitle(adapter.getItem(position));

			mSavedProps.setText(mPreferences.getString(SAVED_PROP_ENTRIES, ""));
			mSavedProps.setScroller(new Scroller(getActivity()));
			mSavedProps.setMaxLines(5);
			mSavedProps.setVerticalScrollBarEnabled(true);
			mSavedProps.setMovementMethod(new ScrollingMovementMethod());
			mSavedProps.setSelection(mSavedProps.getText().length());

			mRetainToggle.setChecked(Utils.stringToBool(mPreferences.getString(RETAIN_PROP_ENTRIES, "0")));
			mSavedProps.setEnabled(mRetainToggle.isChecked());

			mRetainToggle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mSavedProps.setEnabled(mRetainToggle.isChecked());
				}
			});

			mApplyProps.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mRetainToggle.isChecked() && Utils.isStringEmpty(mSavedProps.getText().toString())) mRetainToggle.setChecked(false);
					updateSharedPrefs(mPreferences, SAVED_PROP_ENTRIES, mSavedProps.getText().toString());
					updateSharedPrefs(mPreferences, RETAIN_PROP_ENTRIES, Utils.boolToString(mRetainToggle.isChecked()));
					
					ExecuteBootProperties(getActivity(), mPreferences);

					dialog.dismiss();
					fill();
				}
			});

			dialog.show();

		} else if(adapter.getItem(position).equals(getActivity().getString(R.string.item_add_prop))){
			//add property

			final Dialog dialog = new Dialog(getActivity());
			dialog.setContentView(R.layout.propnewdialog);
			final Button mSaveButton = (Button) dialog.findViewById(R.id.positive);
			final CheckBox mForce = (CheckBox) dialog.findViewById(R.id.force);
			final EditText mEntry = (EditText) dialog.findViewById(R.id.entry);
			final EditText mValue = (EditText) dialog.findViewById(R.id.value);

			dialog.setTitle(adapter.getItem(position));

			mSaveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!Utils.isStringEmpty(mEntry.getText().toString()) && !Utils.isStringEmpty(mValue.getText().toString())){
						if(Utils.writeSystemProp(mEntry.getText().toString(), mValue.getText().toString()) && Utils.updateSystemProp()){
							Utils.toast(getActivity(), getActivity().getString(R.string.msg_changes_reboot));
							if(mForce.isChecked()){
								appendSharedPrefs(mPreferences, SAVED_PROP_ENTRIES, mEntry.getText().toString() + "=" + mValue.getText().toString());
								updateSharedPrefs(mPreferences, RETAIN_PROP_ENTRIES, "1");
								Utils.toast(getActivity(), getActivity().getString(R.string.msg_value_saved_prop));
							} else Utils.toast(getActivity(), getActivity().getString(R.string.msg_value_saved));
						} else Utils.toast(getActivity(), getActivity().getString(R.string.item_error));
						dialog.dismiss();
						fill();
					}
				}
			});

			dialog.show();
		} else if(adapter.getItem(position).split("=").length == 2){
			//editor
			final String entry = adapter.getItem(position).split("=")[0];
			final String value = adapter.getItem(position).split("=")[1];

			final Dialog dialog = new Dialog(getActivity());
			dialog.setContentView(R.layout.propeditdialog);
			final Button mDeleteButton = (Button) dialog.findViewById(R.id.delete);
			final Button mSaveButton = (Button) dialog.findViewById(R.id.positive);
			final CheckBox mForce = (CheckBox) dialog.findViewById(R.id.force);
			final EditText mEditValue = (EditText) dialog.findViewById(R.id.editvalue);

			dialog.setTitle(entry);
			mEditValue.setText(value);
			mEditValue.setSelection(mEditValue.getText().length());

			mDeleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utils.writeSystemProp(entry, "") && Utils.updateSystemProp()) Utils.toast(getActivity(), getActivity().getString(R.string.msg_changes_reboot));
					else Utils.toast(getActivity(), getActivity().getString(R.string.item_error));
					dialog.dismiss();
					fill();
				}
			});

			mSaveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utils.writeSystemProp(entry, mEditValue.getText().toString()) && Utils.updateSystemProp()){
						Utils.toast(getActivity(), getActivity().getString(R.string.msg_changes_reboot));
						if(mForce.isChecked()){
							appendSharedPrefs(mPreferences, SAVED_PROP_ENTRIES, entry + "=" + mEditValue.getText().toString());
							updateSharedPrefs(mPreferences, RETAIN_PROP_ENTRIES, "1");
							Utils.toast(getActivity(), getActivity().getString(R.string.msg_value_saved_prop));
						} else Utils.toast(getActivity(), getActivity().getString(R.string.msg_value_saved));
					} else Utils.toast(getActivity(), getActivity().getString(R.string.item_error));
					dialog.dismiss();
					fill();
				}
			});

			dialog.show();
		}
	}


    private void fill(){

	if(!RootTools.remount("/system/build.prop", "rw")) Utils.CMD(true, "mount -o remount rw /system/");
	Utils.CMD(true, "cp -f /system/build.prop " + "/sdcard/Tame/tmp.prop");
	mPropEntries = Utils.readFile("/sdcard/Tame/tmp.prop").split("\\s+");

	List<String> props = new ArrayList<String>();

	props.add(getActivity().getString(R.string.item_add_prop));
	props.add(getActivity().getString(R.string.item_retain_prop));
	for (String entry : mPropEntries) {
		if(entry.contains("=") && !Utils.isStringEmpty(entry)) props.add(entry);
	}  

	adapter = new PropArrayAdapter(getActivity(), R.layout.propeditlayout, props);
	this.setListAdapter(adapter);
    }

    public static void ExecuteBootProperties(Context context, SharedPreferences preferences){
	boolean needUpdate = false;
	try{
		if(Utils.stringToBool(preferences.getString(RETAIN_PROP_ENTRIES, "0"))){
			String[] properties = preferences.getString(SAVED_PROP_ENTRIES, "").split(System.getProperty("line.separator"));
			Utils.log(null, preferences, "-PROP-", Utils.arrayToString(properties));
			for (String property : properties) {
				if(property.contains("=")){
					String entry = property.split("=")[0];
					String value = property.split("=")[1];
					String valueCMD = Utils.readSystemProp(entry);
					if(!valueCMD.equals(value)){
						String valueCMDInvasive = Utils.readSystemProp(entry, true);
						if(!valueCMDInvasive.equals(value) && valueCMD.equals(valueCMDInvasive)){
							Utils.log(null, preferences, "-PROP-", entry, Utils.readSystemProp(entry, true), value);
							Utils.writeSystemProp(entry, value);
							needUpdate = true;
						}
					}
				}
			}
		}
	} catch(Exception e) {
		Utils.errorHandle(e);
	} finally {
		if(needUpdate){
			Utils.updateSystemProp();
			updateSharedPrefs(preferences, PROP_AT_BOOT_TS, new SimpleDateFormat("MMMM d, yyyy - h:mma").format(new Date()));
			Utils.notification(context, NotificationID.PROP, null, context.getString(R.string.msg_prop_update));
		}
	}
    }

    private static void appendSharedPrefs(SharedPreferences preferences, String var, String value) {
	updateSharedPrefs(preferences, var, preferences.getString(var, "") + "\n" + value);
    }

    private static void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

}
