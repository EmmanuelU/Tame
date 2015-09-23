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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.emman.tame.fragments.GeneralSettings;
import com.emman.tame.MainActivity;
import com.emman.tame.R;
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

	fill();
	Utils.toast(getActivity(), getActivity().getString(R.string.msg_edit_caution));
	mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

	MainActivity.setOnBackPressedListener(new MainActivity.overrideBackListener() {
		@Override
		public void onBackPressed() {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, new GeneralSettings()).commit();
			Utils.CMD(false, "rm -rf /sdcard/Tame/tmp.prop");
			MainActivity.setOnBackPressedListener(null); //normal operations
		}
	});

    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(adapter.getItem(position).equals(getActivity().getString(R.string.item_buildprop_settings))){

		} else if(adapter.getItem(position).split("=").length == 2){
			final String entry = adapter.getItem(position).split("=")[0];
			final String value = adapter.getItem(position).split("=")[1];

			final Dialog dialog = new Dialog(getActivity());
			dialog.setContentView(R.layout.propeditdialog);
			final Button mSaveButton = (Button) dialog.findViewById(R.id.positive);
			final EditText mEditValue = (EditText) dialog.findViewById(R.id.editvalue);

			dialog.setTitle(entry);
			mEditValue.setText(value);
			mEditValue.setSelection(mEditValue.getText().length());

			mSaveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(Utils.writeSystemProp(entry, mEditValue.getText().toString())) Utils.toast(getActivity(), getActivity().getString(R.string.msg_changes_reboot));
					else Utils.toast(getActivity(), getActivity().getString(R.string.item_error));
					dialog.dismiss();
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

	props.add(getActivity().getString(R.string.item_buildprop_settings));
	for (String entry : mPropEntries) {
		if(entry.contains("=") && !Utils.isStringEmpty(entry)) props.add(entry);
	}  

	adapter = new PropArrayAdapter(getActivity(), R.layout.build_prop_editor, props);
	this.setListAdapter(adapter);
    }

    private void appendSharedPrefs(SharedPreferences preferences, String var, String value) {
	updateSharedPrefs(mPreferences, var, preferences.getString(var, "") + "\n" + value);
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

}
