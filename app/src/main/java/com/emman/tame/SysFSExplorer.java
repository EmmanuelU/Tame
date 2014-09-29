package com.emman.tame;

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
import java.util.Collections;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.emman.tame.Resources;

public class SysFSExplorer extends ListFragment 
		implements Resources {

    private View mView;
    private File currentDir;
    private String currentPath;
    private FileArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	currentDir = new File("/sys/");
	fill(currentDir);
	Utils.toast(getActivity(), "Edit Values with Caution");
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		FileOption o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
			currentPath = o.getPath();
			currentDir = new File(currentPath);
			fill(currentDir);
		}
		else {
			final Dialog dialog = new Dialog(getActivity());
			dialog.setContentView(R.layout.syseditdialog);
			Button mSaveButton = (Button) dialog.findViewById(R.id.positive);
			final EditText mEditFile = (EditText) dialog.findViewById(R.id.editfile);
			final String mEditFilePath = o.getPath();
			String[] mTitle = mEditFilePath.split("/");
			dialog.setTitle("File: " + mTitle[mTitle.length - 1]);
			mEditFile.setText(Utils.readOneLine(o.getPath()));
			if(Utils.isNumeric(Utils.readOneLine(o.getPath()))) mEditFile.setSelection(mEditFile.getText().length());
			mEditFile.setRawInputType(Configuration.KEYBOARD_QWERTY);
			mSaveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Utils.writeSYSValue(mEditFilePath, mEditFile.getText().toString());
					Utils.toast(getActivity(), "Value Saved.");
					dialog.dismiss();
				}
			});
			dialog.show();
		}
	}


    private void fill(File f){
        File[]dirs = f.listFiles();
         List<FileOption>dir = new ArrayList<FileOption>();
         List<FileOption>fls = new ArrayList<FileOption>();
         try{
             for(File ff: dirs)
             {
                if(ff.isDirectory())
                    dir.add(new FileOption(ff.getName(),"Folder",ff.getAbsolutePath()));
                else
                {
                    fls.add(new FileOption(ff.getName(),"File",ff.getAbsolutePath()));
                }
             }
         }catch(Exception e)
         {
             
         }
         Collections.sort(dir);
         Collections.sort(fls);
         dir.addAll(fls);
         if(!f.getName().equalsIgnoreCase("sys"))
             dir.add(0,new FileOption("..","Parent Directory",f.getParent()));

	adapter = new FileArrayAdapter(getActivity(),R.layout.sysfs_explorer,dir);
	this.setListAdapter(adapter);
    }


}
