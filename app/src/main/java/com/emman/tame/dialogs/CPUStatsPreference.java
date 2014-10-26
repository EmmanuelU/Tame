package com.emman.tame.dialogs;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class CPUStatsPreference extends DialogPreference
		implements Resources {

    private View mView;

    private TextView mCpuFreqs;

    private SharedPreferences mPreferences;

    private class CurCPUThread extends Thread {

        @Override
        public void run() {
            try {
		while (!this.isInterrupted()) {
			sleep(500);
			String freqs = "";
			for(int i = 0; i < Utils.getNumOfCpus();){
				String freq;
				freq = Utils.readOneLine(Utils.toCPU(FREQ_CUR_FILE, i));
				if(freq == null || freq.equals("")) freq = "Offline";
				else freq = String.format("%s", Utils.toMHz(freq));
				freqs = freqs + "Core " + (i+1) + ": " + freq + "\n\n";
				i++;
			}
			if (freqs != null) mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, freqs));
		}
            } catch (InterruptedException e) {
            }
        }
    };

    private CurCPUThread mCurCPUThread = new CurCPUThread();

    private Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
		String freqs = ((String) msg.obj);
		mCpuFreqs.setText(freqs);
        }
    };

    public CPUStatsPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(false);
	setDialogLayoutResource(R.layout.cpufreqdialog);
    }

    @Override
    protected void onBindDialogView(final View view) {
	super.onBindDialogView(view);
	mView = view;
	mCpuFreqs = (TextView) mView.findViewById(R.id.cpufreqs);
	if(mCurCPUThread.isAlive()){
	    mCurCPUThread.interrupt(); 
	}
	mCurCPUThread = new CurCPUThread();
	mCurCPUThread.start();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
	super.onDialogClosed(positiveResult);

	mCurCPUThread.interrupt();
	try {
		mCurCPUThread.join();
	} catch (InterruptedException e) {
	}
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
	super.onPrepareDialogBuilder(builder);
	builder.setNegativeButton(null, null);
    }

} 
