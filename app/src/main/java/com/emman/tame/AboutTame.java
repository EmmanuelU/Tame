package com.emman.tame;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class AboutTame extends Fragment {

    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
	mView = inflater.inflate(R.layout.about_tame, container, false);
	setversion();
        return mView;
    }

    private void setversion(){
	TextView version = (TextView) mView.findViewById(R.id.versionheader);
	String propversion = Utils.CMD("getprop ro.wild.version", false);
	if(propversion.isEmpty()) propversion = "N/A";
    propversion =  " " + propversion;
	version.setText(version.getText().toString() + propversion);
    }
}
