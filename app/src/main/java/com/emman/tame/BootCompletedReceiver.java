package com.emman.tame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.emman.tame.SetOnBoot;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SetOnBoot.class);
        context.startService(service);
    }
}
