package com.codersdc.codersdclibvideoplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.codersdc.codersdclibvideoplayer.R;
import com.codersdc.codersdclibvideoplayer.models.CustomMessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.codersdc.codersdclibvideoplayer.CodersDCApp.CHANNEL_ID_2;


public class MusicService extends Service {

    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREV = "PREVIOUS";
    public static final String ACTION_PLAY = "PLAY";
    private final IBinder mBinder = new MyBinder();
    private ActionPlaying actionPlaying;
    private PendingIntent prevPendingIntent, playPendingIntent, nextPendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
        prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent playIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        EventBus.getDefault().register(this);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String actionName = intent.getStringExtra("myActionName");
        if (actionName != null) {
            switch (actionName) {
                case ACTION_PREV:
                    actionPlaying.prevClicked();
                    break;
                case ACTION_NEXT:
                    actionPlaying.nextClicked();
                    break;
                case ACTION_PLAY:
                    actionPlaying.playClicked();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    public void showNotification(int playPauseBtn, String title, String desc, MediaSessionCompat.Token token, Bitmap bitmap, boolean isFirst, boolean isLast) {
        Notification notification;

        if (isFirst) {
            notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                    .setNotificationSilent()
                    .setSmallIcon(R.drawable.exo_icon_play)
                    .setLargeIcon(bitmap)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .addAction(playPauseBtn, "Play", playPendingIntent)
                    .addAction(R.drawable.exo_icon_next, "Next", nextPendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(token))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(null)
                    .setAutoCancel(true)
                    .build();
        } else if (isLast) {
            notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                    .setNotificationSilent()
                    .setSmallIcon(R.drawable.exo_icon_play)
                    .setLargeIcon(bitmap)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .addAction(R.drawable.exo_icon_previous, "Previous", prevPendingIntent)
                    .addAction(playPauseBtn, "Play", playPendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(token))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(null)
                    .setAutoCancel(true)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                    .setNotificationSilent()
                    .setSmallIcon(R.drawable.exo_icon_play)
                    .setLargeIcon(bitmap)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .addAction(R.drawable.exo_icon_previous, "Previous", prevPendingIntent)
                    .addAction(playPauseBtn, "Play", playPendingIntent)
                    .addAction(R.drawable.exo_icon_next, "Next", nextPendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(token))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(null)
                    .setAutoCancel(true)
                    .build();
        }
        startForeground(1, notification);
    }

    public void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }


    @Subscribe
    public void onEvent(CustomMessageEvent event) {
        showNotification(event.getDrawable(), event.getTitle(), event.getDesc(), event.getToken(), event.getBitmap(), event.isFirstItem(), event.isLastItem());
        if (event.isNotificationClear()) {
            stopForeground(false);
        }
        if (event.isNotificationDeleted()) {
            stopForeground(true);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
