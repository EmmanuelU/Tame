package com.emman.tame.utils;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.NetworkOnMainThreadException;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.lang.Comparable;
import java.lang.Process;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.emman.tame.MainActivity;

import com.emman.tame.R;
import com.emman.tame.utils.BackgroundTask;
import com.emman.tame.utils.NotificationID;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

public class Utils 
		implements Resources {


    public static Context getContext() {
	return MainActivity.getContext();
    }

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

    public static String appendFile(String filename, String value) {
	CMD(false, "echo '" + value + "' >> " + filename);
	return value;
    }

    public static String getSYSCommand(String fname, String value) {
	return "echo " + value + " > " + fname;
    }

    public static String writeSYSValue(final String fname, final String value) {
	Context context = getContext();
        if(!fileExists(fname)) return value;
	final BackgroundTask mCMDTask = new BackgroundTask(context);
	mCMDTask.queueTask(new BackgroundTask.task() {
		@Override
		public void doInBackground() {
			try {
				final FileOutputStream fos = new FileOutputStream(new File(fname));
				fos.write(value.getBytes());
				fos.flush();
				fos.close();
			} catch (Exception e) {
				logFile("Failed to normally write '" + value + "' to " + fname + ". Attempting SU...");
				String ret = CMD(true, "echo " + value + " > " + fname);
				if(!isStringEmpty(ret)) //outputed some error
					logFile("Failed to write '" + value + "' to " + fname + " using SU.", ret);
				else
					logFile("Successfully wrote '" + value + "' to " + fname + " using SU.");
			}
	
		}

		@Override
		public void onCompleted() {

		}
	});
	mCMDTask.execute();
	return value;
    }

    private static String cmdQueue = "";
    public static String queueSYSValue(String fname, String value) {
	if(!fileExists(fname)) return value;
	if(isStringEmpty(cmdQueue)) cmdQueue = "echo \"" + value + "\" > " + fname;
	else cmdQueue = cmdQueue + NEW_LINE + ("echo \"" + value + "\" > " + fname);
	
	return value;
    }

    public static void launchSYSQueue() {
	Context context = getContext();
	final BackgroundTask mCMDTask = new BackgroundTask(context);
	mCMDTask.queueTask(new BackgroundTask.task() {
		@Override
		public void doInBackground() {
			CMD(true, cmdQueue);
		}

		@Override
		public void onCompleted() {
			cmdQueue = "";
		}

	});
	mCMDTask.execute();
    }

    public static String SetSOBValue(String fname, String value) {
	if(!fileExists(fname)) return value;
	if(isStringEmpty(MainActivity.BootCommands)) MainActivity.BootCommands = "echo \"" + value + "\" > " + fname;
	else MainActivity.BootCommands = MainActivity.BootCommands + NEW_LINE + ("echo \"" + value + "\" > " + fname);
	logFile("New SOB Request: '" + value + "' to " + fname + ".");
	return value;
    }

    /**
    * @deprecated  No longer needed, just input multiple strings in {@link #CMD(boolean, String...)}
    */
    @Deprecated
    public static String SetRABCommand(String command) {
        if(!fileExists(FILE_RUN_AT_BOOT)){
		CMD(true, "touch " + FILE_RUN_AT_BOOT);
		appendFile(FILE_RUN_AT_BOOT, "#!/bin/sh");
	}
	appendFile(FILE_RUN_AT_BOOT, command);
	return command;
    }

    public static int getAndroidAPI() {
	return Build.VERSION.SDK_INT;
    }

    public static boolean isLollipop() {
	return getAndroidAPI() > 20;
    }

    public static void openFile(Context context, String filePath, String type) {
	Intent intent = new Intent();
        if(fileExists(filePath)){
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file = new File(filePath);
		intent.setDataAndType(Uri.fromFile(file), type);
		context.startActivity(intent); 
		logFile("Opening " + filePath + " (" + type + ") with external module.");
	}
    }
    
 public static int hexToInt(String str){
   int start = 6;
   int end = 10;
   String t = str.substring(start, end);

   char[] ch = t.toCharArray();
   String res = "";
   for(int i = 0; i < end-start; i += 2){
      res += Integer.parseInt(ch[i]+ch[i+1]+"", 16);
   }

   return Integer.parseInt(res);
   }

    public static boolean writeSystemProp(String propname, String propvalue) {
	boolean failed = false;
	Exception error = null;
	logFile("Attempting to set '" + propvalue + "' as " + propname + " in build.prop...");
	try {
		RootTools.remount("/system/", "RW");
		CMD(true, "mount -o remount rw /system/", "rm -rf " + FILE_LOCAL_BUILD_PROP, "mv -f /system/build.prop " + FILE_TMP_BUILD_PROP);
		RootTools.copyFile("/system/build.prop", FILE_TMP_BUILD_PROP, true, false);
		String previouspropvalue = readSystemProp(propname);
		//generate modified build.prop
		File newfile = new File(FILE_LOCAL_BUILD_PROP);
		FileWriter fw = new FileWriter(newfile);

		Reader fr = new FileReader(new File(FILE_TMP_BUILD_PROP));
		BufferedReader br = new BufferedReader(fr);
		while (br.ready()) {
			fw.write(br.readLine().replaceAll(propname + "=" + previouspropvalue, propname + "=" + propvalue) + "\n");
		}
		
		fw.close();
		br.close();
		fr.close();
		CMD(false, "rm -rf " + FILE_TMP_BUILD_PROP);

		//replace /system/build.prop
		if(!updateSystemProp(FILE_LOCAL_BUILD_PROP)){
			failed = true;
		}
	} catch (Exception e) {
		error = e;
		failed = true;
	} finally {
		if(failed){
			errorHandle(error, "Failed to write to build.prop, are you on Lollipop? Attempting to save ROM integrity");
			CMD(true, "mount -o remount rw /system/", "chmod 644  /system/build.prop");
		}
		return !failed;
	}
    }

    public static boolean updateSystemProp(String newpropfile) {
	RootTools.debugMode = true;
	if(!fileExists(newpropfile)) return false;

	RootTools.remount("/system/", "RW");
	RootTools.deleteFileOrDirectory("/system/build.prop", true);
	CMD(true, "mount -o remount rw /system/", "chmod 644 " + newpropfile);
	return RootTools.copyFile(newpropfile, "/system/build.prop", true, true);
    }

    public static String readSystemProp(String prop) {
	return CMD(false, "getprop " + prop);
    }

    public static void writeLocalFile(Context context, String filename){
	try {
		FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
		outputStream.write(filename.getBytes());
		outputStream.close();
	} catch (Exception e) {
		errorHandle(e);
	}
    }

    public static String toCPU(String value, int cpu) {
	return value.replace("cpu0", "cpu" + cpu);
    }

    public static String toSDCARD(String value, int sdcard) {
	return value.replace("mmcblk0", "mmcblk" + sdcard);
    }

    public static boolean isNumeric(String str){
	if(isStringEmpty(str)) return false;
	try{
		double d = Double.parseDouble(str);
	}
	catch(NumberFormatException nfe){
		return false;
	}
	return true;  
    }

   public static int getNumOfCpus() {
        int numOfCpu = 1;
        String numOfCpus = readOneLine(NUM_OF_CPUS_PATH);
        String[] cpuCount = numOfCpus.split("-");
        if (cpuCount.length > 1) {
            try {
                int cpuStart = Integer.parseInt(cpuCount[0]);
                int cpuEnd = Integer.parseInt(cpuCount[1]);

                numOfCpu = cpuEnd - cpuStart + 1;

                if (numOfCpu < 0)
                    numOfCpu = 1;
            } catch (NumberFormatException ex) {
                numOfCpu = 1;
            }
        }
	logFile("Number of Processors: " + numOfCpu);
        return numOfCpu;
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

    public static boolean canSU() {
        return RootTools.isAccessGiven();
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
        String line = "";
        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            return readFileViaShell(fname, true);
        }
        return line;
    }

    public static String readFile(String fname) {
	if(!fileExists(fname)) return "";
	File file = new File(fname);
	StringBuilder text = new StringBuilder();
	try {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;

		while((line = br.readLine()) != null) {
			text.append(line);
			text.append('\n');
		}
	}
	catch (Exception e) {
           	return readFileViaShell(fname, true);
	}
	return text.toString();
    }

    public static boolean isNetworkOnline(Context context) {
	ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo netInfo = cm.getActiveNetworkInfo();
	return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    private static boolean fetchComplete = false;
    public static String fetchTextFile(final String fileUrl) {
	if(!isNetworkOnline(getContext())) return "";
	final StringBuilder contents = new StringBuilder();
	final BackgroundTask mFetchTask = new BackgroundTask(getContext());
	fetchComplete = false;
	mFetchTask.queueTask(new BackgroundTask.task() {
		@Override
		public void doInBackground() {
			DefaultHttpClient  httpclient = new DefaultHttpClient();
			try {
				HttpGet httppost = new HttpGet(fileUrl);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity ht = response.getEntity();

				BufferedHttpEntity buf = new BufferedHttpEntity(ht);

				InputStream is = buf.getContent();

				BufferedReader r = new BufferedReader(new InputStreamReader(is));

				String line;
				while ((line = r.readLine()) != null) {
				    contents.append(line + "\n");
				}
			}
			catch (Exception e) {
				errorHandle(e);
			}
			fetchComplete = true;
		}

		@Override
		public void onCompleted() {
			fetchComplete = true;
		}

	});
	mFetchTask.execute();
	
	int timeoutms = 0;
        while (!fetchComplete && timeoutms < 10000){
		try{
			Thread.sleep(50);
			timeoutms += 50;
              	} catch (Exception e){
			errorHandle(e);
		}
		finally{
			if(timeoutms >= 10000) logFile("Fetch File '" + fileUrl + "' timed out.");
		}
        }

	return contents.toString();
    }

    public static boolean packageExists(Context context, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;
            pm = context.getPackageManager();        
            packages = pm.getInstalledApplications(0);
            for (ApplicationInfo packageInfo : packages) {
        if(packageInfo.packageName.equals(targetPackage)) return true;
        }        
        return false;
    }

    public static boolean fileIsReadable(String fname) {
        return new File(fname).canRead();
    }

    public static boolean fileIsWritable(String fname) {
        return new File(fname).canWrite(); //only returns true if world-writeable
    }

    public static boolean stringToBool(String s) {
	return (s.equals("1"));
    }
    public static boolean isStringEmpty(String s) {
	return (s == null || s.equals("") || !(s.trim().length() > 0));
    }

    public static String boolToString(boolean b) {
	if (b) return "1";
	else return "0"; 
    }

    public static boolean isInteger(String s) {
	if(isStringEmpty(s)) return false;
	try {
		Integer.parseInt(s); 
	} catch(NumberFormatException e) { 
		return false; 
	}
	return true;
    }

    public static boolean isSubstringInString(String substring, String string) {
	return string.toLowerCase().contains(substring.toLowerCase());
    }

    /**
     * Read file via shell
     *
     * @param filePath
     * @param useSu
     * @return file output
     */
    public static String readFileViaShell(String filePath, boolean useSu) {
	return CMD(useSu, "cat " + filePath);
    }

    public static String getSUVersion(){
	return CMD(false, "su -v");
    }

    public static String CMDScript(String... commands){
	String buildScript = "echo '#!/bin/sh' > " + FILE_SYS_QUEUE;
	for(String line : commands) {
		buildScript = buildScript + NEW_LINE + "echo '" + line + "' >> " + FILE_SYS_QUEUE;
	}
	return CMD(false, true, null, buildScript, "sh " + FILE_SYS_QUEUE);
    }

    public static String CMDSpecial(String... commands) {
	return CMD(true, true, Shell.ShellContext.SHELL, commands);
    }

    public static String CMDQuiet(boolean useSu, String... commands) {
	return CMD(useSu, true, Shell.ShellContext.SHELL, commands);
    }

    public static void CMDBackground(boolean useSu, String... commands) {
	CMD(useSu, false, null, commands);
    }

    public static String CMD(boolean useSu, String... commands) {
	return CMD(useSu, true, null, commands);
    }

    private static String cmdOutput = "";

    public static String CMD(final boolean useSu, final boolean waitFor, Shell.ShellContext context, String... commands){
	cmdOutput = "";

	Command cmd = new Command(0, false, commands){
	    	@Override
		public void commandOutput(int id, String line) {
			if(Utils.isStringEmpty(cmdOutput)) cmdOutput = line;
			else cmdOutput = cmdOutput + NEW_LINE + line;
		    	super.commandOutput(id, line);
		}

		@Override
		public void commandTerminated(int id, String reason) {
		    super.commandTerminated(id, reason);
		    if(!waitFor){
                    	try{
                        	RootTools.getShell(useSu).close();
			} catch (Exception unlikely){}
		    }
		}

		@Override
		public void commandCompleted(int id, int exitcode) {
		    super.commandCompleted(id, exitcode);    
		    if(!waitFor){
                    	try{
                        	RootTools.getShell(useSu).close();
			} catch (Exception unlikely){}
		    }
		}
	};

	try{	
		if(context == null)
			RootTools.getShell(useSu).add(cmd);
		else
			RootTools.getShell(useSu, 10000, context).add(cmd); //timeout of 10000 doesn't function for whatever reason, so I implemented my own below (timeoutms)
	} catch (Exception unlikely){}

	if(waitFor){  
		int timeoutms = 0;
		while (!cmd.isFinished() && timeoutms < 10000){
			try{
				Thread.sleep(50);
				timeoutms += 50;
		      	} catch (Exception unlikely){}
			finally{
				if(timeoutms >= 10000) logFile("Command timed out after 10 seconds.", "Output: " + cmdOutput);
			}
		}

		try{
			RootTools.getShell(useSu).close();
		} catch (Exception unlikely){}

		return cmdOutput;
	}

	return "";
    }

    public static void toast(Context context, String message) {
	Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	logFile("Toast: " + message);
    }

    public static void burntToast(Context context, String message) {
	Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	logFile("Burnt Toast: " + message);
    }

    public static void logFile(String... messages) {
	//messages don't need to be translated because I'm the only one reading them anyway
	if(MainActivity.isDebugging())
		for(String message : messages) {
			CMD(false, "echo '" + new SimpleDateFormat("[LL/d/yyyy @ kk:mm:ss] ").format(new Date()) + message + "' >> " + FILE_TAME_LOG);
		}
		CMD(false, "echo  >> " + FILE_TAME_LOG);
    }

    public static void errorHandle(Exception e) {
	errorHandle(e, e.toString());
    }

    public static void errorHandle(Exception e, String errordesc) {
	e.printStackTrace();
	logFile(errordesc);
    }

    public static int getNotificationID(NotificationID id) {
	return id.ordinal();
    }

    public static void notification(Context context, NotificationID id, Intent intent, String message) {
	Notification.Builder Notif;
	NotificationManager mNotifyMgr;
	PendingIntent pIntent;
	Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
	
	
	final Bundle extras = new Bundle();
	extras.putBoolean(EXTRA_FORCE_SHOW_LIGHTS, true);

	Notif = new Notification.Builder(context)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(TAG)
		.setLights(0xFF0000, 300, 1500)
		.setSmallIcon(R.drawable.ic_notification)
		.setLargeIcon(icon)
		.setStyle(new Notification.BigTextStyle()
		.bigText(message))
		.setContentText(message);
		
		
	Notif.setExtras(extras);

	//All these flags, and you still dont auto cancel.
	Notif.setAutoCancel(true);
	Notif.build().flags |= Notification.FLAG_SHOW_LIGHTS;

	if(intent != null){
		pIntent = PendingIntent.getActivity(context, 0, intent, 0);
	}
	else{
		//but this'll do it #logicispower
		pIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	Notif.setContentIntent(pIntent);
	mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
	mNotifyMgr.notify(Utils.getNotificationID(id), Notif.build());
	logFile("Notification: " + message);
    }
    
    public static void testNotification(Context context, NotificationID id, Intent intent, String message, int on, int off, int color) {
	Notification.Builder Notif;
	NotificationManager mNotifyMgr;
	PendingIntent pIntent;
	Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
	
	final Bundle extras = new Bundle();
	extras.putBoolean(EXTRA_FORCE_SHOW_LIGHTS, true);
	
	if(color == 0) color = 0xFF0000;

	Notif = new Notification.Builder(context)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(TAG)
		.setLights(color, on, off)
		.setSmallIcon(R.drawable.ic_notification)
		.setLargeIcon(icon)
		.setStyle(new Notification.BigTextStyle()
		.bigText(message))
		.setContentText(message);
		
	Notif.setExtras(extras);

	//All these flags, and you still dont auto cancel.
	Notif.setAutoCancel(true);
	Notif.build().flags |= Notification.FLAG_SHOW_LIGHTS;

	if(intent != null){
		pIntent = PendingIntent.getActivity(context, 0, intent, 0);
	}
	else{
		//but this'll do it #logicispower
		pIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	Notif.setContentIntent(pIntent);
	mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
	mNotifyMgr.notify(Utils.getNotificationID(id), Notif.build());
	logFile("Notification: " + message);
    }
    
    public static void clearNotification(Context context, NotificationID id){
	NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	notificationManager.cancel(Utils.getNotificationID(id));
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

    public static String[] getReadAhead(boolean realvalue) {
        ArrayList<String> values = new ArrayList<String>();
        int start = 128;
        for(int i = 1; i<=32; i++) {
		if(realvalue) values.add((start*i)+"");
		else values.add((start*i)+"KB");
        }
        return values.toArray(new String[values.size()]);
    }

    public static String toMHz(String mhzString) {
    	if(!isNumeric(mhzString)) return "";
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append("MHz")
                .toString();
    }

    public static String toGPUMHz(String mhzString) {
    	if(!isNumeric(mhzString)) return "";
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000000).append("MHz")
                .toString();
    }

    public static String[] getFileFreqToMhz(String file, int how) {
        if(fileExists(file)) {
            ArrayList<String> names = new ArrayList<String>();
            //setPermissions(file);
            File freqfile = new File(file);
            FileInputStream fin1 = null;
            byte fileContent[] = null;
            try {
                fin1 = new FileInputStream(freqfile);
                fileContent = new byte[(int)freqfile.length()];
                fin1.read(fileContent);
            }
            catch (FileNotFoundException e1) {
                //System.out.println("File not found" + e1);
            }
            catch (IOException ioe1) {
                //System.out.println("Exception while reading file " + ioe1);
            }
            finally {
                try {
                    if (fin1 != null) {
                        fin1.close();
                    }
                }
                catch (Exception ioe1) {
                    //System.out.println("Error while closing stream: " + ioe1);
                }
            }
            for(String s : new String(fileContent).trim().split(" ")) {
                names.add((Integer.parseInt(s) / how) + "MHz");
            }
            String[] toMhz = new String[names.size()];
            return names.toArray(toMhz);
        }
        return "".split("\\s+");
    }

    public static String[] getFilemA(String file) {
        if(fileExists(file)) {
            ArrayList<String> names = new ArrayList<String>();
	
            names.add(getContext().getString(R.string.item_default));
            //setPermissions(file);
            File freqfile = new File(file);
            FileInputStream fin1 = null;
            byte fileContent[] = null;
            try {
                fin1 = new FileInputStream(freqfile);
                fileContent = new byte[(int)freqfile.length()];
                fin1.read(fileContent);
            }
            catch (FileNotFoundException e1) {
                //System.out.println("File not found" + e1);
            }
            catch (IOException ioe1) {
                //System.out.println("Exception while reading file " + ioe1);
            }
            finally {
                try {
                    if (fin1 != null) {
                        fin1.close();
                    }
                }
                catch (IOException ioe1) {
                    //System.out.println("Error while closing stream: " + ioe1);
                }
            }
            for(String s : new String(fileContent).trim().split(" ")) {
                names.add(Integer.parseInt(s) + "mA");
            }
            String[] toMa = new String[names.size()];
            return names.toArray(toMa);
        }
        return "".split("\\s+");
    }

    public static void ExtractAssets(Context context){
	try {
            AssetManager assetFiles = context.getAssets();
 
            // MyHtmlFiles is the name of folder from inside our assets folder
            String[] files = assetFiles.list(TAG);
 
            // Initialize streams
            InputStream in = null;
            OutputStream out = null;
 
            for (int i = 0; i < files.length; i++) {
 
                     
                     // @Folder name is also case sensitive
                     // @MyHtmlFiles is the folder from our assets
                      
                    in = assetFiles.open(TAG + "/" + files[i]);
			File directory = new File(Environment.getExternalStorageDirectory()+File.separator+TAG);
			directory.mkdirs();

                    out = new FileOutputStream(PATH_TAME_LOCAL + files[i]);
                    copyAssets(in, out);
            }
 
        } catch (Exception e) {
	    logFile("Failed to extract zip to '" + FILE_DISABLE_SET_ON_BOOT_ZIP + "'. Do you have an sdcard?");
            Utils.notification(context, NotificationID.EXTRACT, null, "Failed to extract zip to '" + FILE_DISABLE_SET_ON_BOOT_ZIP + "'. Do you have an sdcard?");
            errorHandle(e);
	    return;
        }
	logFile("Extracted emergency zip to '" + FILE_DISABLE_SET_ON_BOOT_ZIP + "'. Flash in recovery when you wish to disable Tame's next Set on Boot.");
	Utils.notification(context, NotificationID.EXTRACT, null, "Extracted emergency zip to '" + FILE_DISABLE_SET_ON_BOOT_ZIP + "'. Flash in recovery when you wish to disable Tame's next Set on Boot.");
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
 
        } catch (Exception e) {
	    logFile("Failed to extract Application Assets, do you have an sdcard?");
            errorHandle(e);
        }
    }

    public static int getSpinnerIndex(Spinner spinner, String value){
	int index = 0;

	for(int i=0;i<spinner.getCount();i++){
		if(spinner.getItemAtPosition(i).equals(value)) index = i;
	}
	return index;
    }

    public static boolean spinnerValueChanged(Spinner spinner, String value, int highlighted){
	int selection = Utils.getSpinnerIndex(spinner, value);
	return (selection == highlighted);
    }

    public static int getArrayIndex(String[] arr, String targetValue) {
	int index = 0;
	for(String s: arr){
		if(s.equals(targetValue)) return index;
		index++;
	}
	return -1;
    }
}
