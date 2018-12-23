package com.importio.nitin.solarcalculator.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyNotificationReceiver extends BroadcastReceiver {
    public MyNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, MyNotificationIntentService.class);
        context.startService(intent1);
    }
}
