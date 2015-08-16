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
import com.emman.tame.fragments.KernelSettings;
import com.emman.tame.utils.Resources;
import com.emman.tame.utils.Utils;

public class TCPPreference extends DialogPreference
		implements Resources {

    private View mView;

    private Spinner mTCPProtocol;
    private String[] mProtocolList;

    private SharedPreferences mPreferences;

    public TCPPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.tcpdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	updateData();

	List<String> list = new ArrayList<String>(Arrays.asList(Utils.readSYSCTLValue(TCP_AVAILABLE_PROTOCOLS).split("\\s+")));
	
	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
		android.R.layout.simple_spinner_item, list);
	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	mTCPProtocol.setAdapter(dataAdapter);
	
	updateData();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);
		if (getOnPreferenceChangeListener() != null) getOnPreferenceChangeListener().onPreferenceChange(this, null);
	if(positiveResult) setData();

	KernelSettings.TCPupdate();
    }

    private boolean initiateData(){

	mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

	mTCPProtocol = (Spinner) mView.findViewById(R.id.tcp_protocol);
	
	mProtocolList = Utils.readSYSCTLValue(TCP_AVAILABLE_PROTOCOLS).split("\\s+");

	return true;
    }

    private void setData(){
	if(!initiateData()) return;
	
	updateSharedPrefs(mPreferences, SAVED_TCP_PROTOCOL, Utils.writeSYSCTLValue(TCP_PROTOCOL, mProtocolList[(int) mTCPProtocol.getSelectedItemId()]));
	
    }

    private void updateData(){
	if(!initiateData()) return;

	updateDependencies();

	mTCPProtocol.setSelection(Utils.getArrayIndex(mProtocolList, Utils.readSYSCTLValue(TCP_PROTOCOL)));
    }

    private void updateDependencies(){
	if(!initiateData()) return;

    }

    public static void SetOnBootData(SharedPreferences preferences){
	Utils.SetSOBCommand("sysctl -w " + TCP_PROTOCOL + "=" + preferences.getString(SAVED_TCP_PROTOCOL, ""));
    }

    private void updateSharedPrefs(SharedPreferences preferences, String var, String value) {
	final SharedPreferences.Editor editor = preferences.edit();
	editor.putString(var, value);
	editor.commit();
    }

} 
