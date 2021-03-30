package com.codersdc.codersdclibvideoplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;


public class CodersDCApp extends Application {

    public static final String CHANNEL_ID_2 = "CHANNEL_2";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    public void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_ID_2,
                    "CodersDc Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel2.setDescription("This Channel for CodersDc Player");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel2);
        }

    }
}
