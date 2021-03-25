package com.codersdc.codersdclibvideoplayer.models;

import android.graphics.Bitmap;
import android.support.v4.media.session.MediaSessionCompat;

public class CustomMessageEvent {
    private String title;
    private String desc;
    private String filePath;
    private int drawable;
    private Bitmap bitmap;
    private MediaSessionCompat.Token token;
    private boolean isFirstItem;
    private boolean isLastItem;
    private boolean isNotificationDeleted;

    public boolean isNotificationDeleted() {
        return isNotificationDeleted;
    }

    public void setNotificationDeleted(boolean notificationDeleted) {
        isNotificationDeleted = notificationDeleted;
    }

    public boolean isFirstItem() {
        return isFirstItem;
    }

    public boolean isLastItem() {
        return isLastItem;
    }

    public void setFirstItem(boolean firstItem) {
        isFirstItem = firstItem;
    }

    public void setLastItem(boolean lastItem) {
        isLastItem = lastItem;
    }

    private boolean isNotificationClear;

    public boolean isNotificationClear() {
        return isNotificationClear;
    }

    public void setNotificationClear(boolean notificationClear) {
        isNotificationClear = notificationClear;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public MediaSessionCompat.Token getToken() {
        return token;
    }

    public void setToken(MediaSessionCompat.Token token) {
        this.token = token;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
