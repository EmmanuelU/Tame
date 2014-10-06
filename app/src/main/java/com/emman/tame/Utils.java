package com.emman.tame;

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
import java.io.BufferedWriter;
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

public class Utils 
		implements Resources {

    private static final String TAG = "Tame";

    /**
     * Write a string value to the specified file.
     * @param filename      The filename
     * @param value         The value
     */
    public static String writeValue(String filename, String value) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	return value;
    }

    public static String appendValue(String filename, String value) {
        new CMDProcessor().sh.runWaitFor("busybox echo '" + value + "' >> " + filename);
	return value;
    }

    public static String writeSYSValue(String fname, String value) {
        if(fileExists(fname)) new CMDProcessor().sh.runWaitFor("busybox echo " + value + " > " + fname);
	return value;
    }

    public static String SetSOBValue(String fname, String value) {
        if(!fileExists(FILE_SET_ON_BOOT)){ 
		new CMDProcessor().su.runWaitFor("busybox touch " + FILE_SET_ON_BOOT);
		appendValue(FILE_SET_ON_BOOT, "#!/bin/sh");
	}
	appendValue(FILE_SET_ON_BOOT, "echo \"" + value + "\" > " + fname);
	return value;
    }

public static boolean isNumeric(String str)  
{  
  try  
  {  
    double d = Double.parseDouble(str);  
  }  
  catch(NumberFormatException nfe)  
  {  
    return false;  
  }  
  return true;  
}

    /**
     * Write the "color value" to the specified file. The value is scaled from
     * an integer to an unsigned integer by multiplying by 2.
     * @param filename      The filename
     * @param value         The value of max value Integer.MAX
     */
    public static void writeColor(String filename, int value) {
        writeValue(filename, String.valueOf((long) value * 2));
    }

    /**
     * Check if the specified file exists.
     * @param filename      The filename
     * @return              Whether the file exists or not
     */
    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }


    public static void showDialog(Context ctx, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
              alertDialog.dismiss();
           }
        });
        alertDialog.show();
    }

    public static boolean checkSu() {
        if (!new File("/system/bin/su").exists()
                && !new File("/system/xbin/su").exists()) {
            Log.e(TAG, "su does not exist!!!");
            return false; // tell caller to bail...
        }

        try {
            if ((new CMDProcessor().su.runWaitFor("ls /data/app-private"))
                    .success()) {
                Log.i(TAG, " SU exists and we have permission");
                return true;
            } else {
                Log.i(TAG, " SU exists but we dont have permission");
                return false;
            }
        } catch (final NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage().toString());
            return false;
        }
    }

 /**
     * Read one line from file
     *
     * @param fname
     * @return line
     */
    public static String readOneLine(String fname) {
	if(!fileExists(fname)) return "";
        BufferedReader br;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "IO Exception when reading sys file", e);
            // attempt to do magic!
            return readFileViaShell(fname, true);
        }
        return line;
    }

    public static boolean fileIsReadable(String fname) {
        return new File(fname).canRead();
    }

    public static boolean fileIsWritable(String fname) {
        return new File(fname).canWrite();
    }

    public static boolean stringToBool(String s) {
	if (s.equals("")) return false;
	else if (s.equals("1")) return true;
	else if (s.equals("0")) return false;
	throw new IllegalArgumentException(s+" is not a bool. Only 1 and 0 are.");
    }

    public static String boolToString(boolean b) {
	if (b) return "1";
	else return "0"; 
    }


    /**
     * Read file via shell
     *
     * @param filePath
     * @param useSu
     * @return file output
     */
    public static String readFileViaShell(String filePath, boolean useSu) {
        CMDProcessor.CommandResult cr = null;
        if (useSu) {
            cr = new CMDProcessor().su.runWaitFor("cat " + filePath);
        } else {
            cr = new CMDProcessor().sh.runWaitFor("cat " + filePath);
        }
        if (cr.success())
            return cr.stdout;
        return null;
    }

    public static String CMD(String command, boolean useSu) {
        CMDProcessor.CommandResult cr = null;
        if (useSu) {
            cr = new CMDProcessor().su.runWaitFor(command);
        } else {
            cr = new CMDProcessor().sh.runWaitFor(command);
        }
        if (cr.success())
            return cr.stdout;
        return null;
    }

    public static void toast(Context context, String message) {
	Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void layoutDisable(ViewGroup layout) {
	layout.setEnabled(false);
	for (int i = 0; i < layout.getChildCount(); i++) {
		View child = layout.getChildAt(i);
		if (child instanceof ViewGroup) {
			layoutDisable((ViewGroup) child);
		} else {
			child.setEnabled(false);
		}
	}
    }

    public static void layoutEnable(ViewGroup layout) {
	layout.setEnabled(true);
	for (int i = 0; i < layout.getChildCount(); i++) {
		View child = layout.getChildAt(i);
		if (child instanceof ViewGroup) {
			layoutEnable((ViewGroup) child);
		} else {
			child.setEnabled(true);
		}
	}
    }

    public static String toMHz(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append("MHz")
                .toString();
    }

    public static void ExtractAssets(Context context){
	try {
            AssetManager assetFiles = context.getAssets();
 
            // MyHtmlFiles is the name of folder from inside our assets folder
            String[] files = assetFiles.list("Tame");
 
            // Initialize streams
            InputStream in = null;
            OutputStream out = null;
 
            for (int i = 0; i < files.length; i++) {
 
                     
                     // @Folder name is also case sensitive
                     // @MyHtmlFiles is the folder from our assets
                      
                    in = assetFiles.open("Tame/" + files[i]);
 
                     
                     // Currently we will copy the files to the root directory
                     // but you should create specific directory for your app
                    Utils.CMD("mkdir /sdcard/Tame", false);

                    out = new FileOutputStream(
                            Environment.getExternalStorageDirectory() + "/Tame/"
                                    + files[i]);
                    copyAssets(in, out);
            }
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
 
    private static void copyAssets(InputStream in, OutputStream out) {
        try {
 
            byte[] buffer = new byte[1024];
            int read;
 
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
 
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
 
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
