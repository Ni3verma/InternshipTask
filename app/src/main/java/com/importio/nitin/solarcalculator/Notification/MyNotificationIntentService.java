package com.importio.nitin.solarcalculator.Notification;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.importio.nitin.solarcalculator.R;

public class MyNotificationIntentService extends IntentService {
    public MyNotificationIntentService() {
        super("MyNotificationIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channelId", "channelName", NotificationManager.IMPORTANCE_HIGH);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channelId")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Time to shoot")
                .setContentText("Golden hour has started")
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setTicker("app");

        if (notificationManager != null) {
            notificationManager.notify(154, builder.build());
        }
    }
}
