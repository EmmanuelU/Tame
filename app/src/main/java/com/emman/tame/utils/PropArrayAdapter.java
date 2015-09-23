package com.emman.tame.utils;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.res.AssetManager;
import android.content.DialogInterface;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.lang.Comparable;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.emman.tame.R;

public class PropArrayAdapter extends ArrayAdapter<String>{

	private Context c;
	private int id;
	private List<String>items;
	
	public PropArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}

	public String getItem(int i)
	 {
		 return items.get(i);
	 }

	@Override
       public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
                   LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                   v = vi.inflate(id, null);
		}
		final String o = items.get(position);

		if (o == null) return v;

		TextView header, subheader;
		header = (TextView) v.findViewById(R.id.header);
		subheader = (TextView) v.findViewById(R.id.subheader);

		if(o.split("=").length == 2) {
			header.setText(o.split("=")[0]);
			subheader.setText(o.split("=")[1]); 
		} else if(o.equals(c.getString(R.string.item_buildprop_settings)) || o.equals(c.getString(R.string.item_add_prop))){
			header.setText(o);
			subheader.setText("");
		}
               return v;
       }

}
