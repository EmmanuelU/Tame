package com.emman.tame;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;


public class AboutTame extends Fragment {

    private View mView;
    String propversion;
    String propversiondate;
    private Button mUpdate;
    private TextView mTameLogo;
    Animation fadein = new AlphaAnimation(0.0f, 1.0f);
    Animation fadeout = new AlphaAnimation(1.0f, 0.0f);

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	mView = inflater.inflate(R.layout.about_tame, container, false);
	mUpdate = (Button) mView.findViewById(R.id.update_button);
	mTameLogo = (TextView) mView.findViewById(R.id.tame_logo);

        mUpdate.setOnClickListener(new View.OnClickListener() {
		public void onClick(View view) {
			CheckUpdate();
		}
	});
	
	TameLogoAnim();
	mTameLogo.startAnimation(fadeout);
	setversiondata();
        return mView;
    }

    private boolean isWild(){
	
	propversion = Utils.CMD("getprop ro.wild.version", false);
	propversiondate = Utils.CMD("getprop ro.wild.date", false);
	return (!propversion.isEmpty() || !propversiondate.isEmpty());
	
    }

    private void setversiondata(){
	TextView version = (TextView) mView.findViewById(R.id.versionheader);
	if(isWild()){
		version.setText(version.getText().toString() + " " + propversion);
		Utils.toast(getActivity(), "WildKernel Detected");
	}
	else{
		version.setVisibility(View.GONE);
		mUpdate.setVisibility(View.GONE);
	}
    }

    private void CheckUpdate(){
	Utils.toast(getActivity(), "Test lol");
    }

    private void TameLogoAnim(){
	fadein.setDuration(1000);
	fadeout.setDuration(2000);
	fadein.setAnimationListener(new AnimationListener() {

	    @Override
	    public void onAnimationStart(Animation animation) {

	    }

	    @Override
	    public void onAnimationEnd(Animation animation) {
		mTameLogo.startAnimation(fadeout);

	    }

	    @Override
	    public void onAnimationRepeat(Animation animation) {

	    }
	});
	fadeout.setAnimationListener(new AnimationListener() {

	    @Override
	    public void onAnimationStart(Animation animation) {

	    }

	    @Override
	    public void onAnimationEnd(Animation animation) {
		mTameLogo.startAnimation(fadein);

	    }

	    @Override
	    public void onAnimationRepeat(Animation animation) {

	    }
	});
    }
}
