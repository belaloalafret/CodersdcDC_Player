package com.codersdc.codersdclibvideoplayer.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class HomeMusicItems implements Parcelable {

    public static final Creator<HomeMusicItems> CREATOR = new Creator<HomeMusicItems>() {
        @Override
        public HomeMusicItems createFromParcel(Parcel in) {
            return new HomeMusicItems(in);
        }

        @Override
        public HomeMusicItems[] newArray(int size) {
            return new HomeMusicItems[size];
        }
    };
    private int id;
    private String title;
    private String description;
    private String file_path;
    private String file_duration;
    private int orientation_id;
    private String orientation_title;
    private String mime_type;
    private int category_id;
    private String category_name;
    private int channel_id;
    private String channel_name;
    private int user_rate;
    private float global_rate;
    private int views;
    private int published;
    private String uploaded_by;
    private String uploaded_at;
    private boolean isSelected;
    private boolean isProgress;
    private String video_thumbnail;
    private String srt;
    private int drawable;
    private Bitmap bitmap;

    public HomeMusicItems() {
    }

    public HomeMusicItems(String title, String description, String file_path) {
        this.title = title;
        this.description = description;
        this.file_path = file_path;
    }

    public HomeMusicItems(String title, String description, String file_path, Bitmap bitmap) {
        this.title = title;
        this.description = description;
        this.file_path = file_path;
        this.bitmap = bitmap;
    }

    protected HomeMusicItems(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        file_path = in.readString();
        file_duration = in.readString();
        orientation_id = in.readInt();
        orientation_title = in.readString();
        mime_type = in.readString();
        category_id = in.readInt();
        category_name = in.readString();
        channel_id = in.readInt();
        channel_name = in.readString();
        user_rate = in.readInt();
        global_rate = in.readFloat();
        views = in.readInt();
        published = in.readInt();
        uploaded_by = in.readString();
        video_thumbnail = in.readString();
        isSelected = in.readByte() != 0;
        isProgress = in.readByte() != 0;
        uploaded_at = in.readString();
        srt = in.readString();
        drawable = in.readInt();
    }

    public HomeMusicItems(int id, String title, String description, String file_path, String file_duration, int orientation_id, String orientation_title, String mime_type, int category_id, String category_name, int channel_id, String channel_name, int user_rate, float global_rate, int views, int published, String uploaded_by, boolean isSelected, boolean isProgress, String video_thumbnail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.file_path = file_path;
        this.file_duration = file_duration;
        this.orientation_id = orientation_id;
        this.orientation_title = orientation_title;
        this.mime_type = mime_type;
        this.category_id = category_id;
        this.category_name = category_name;
        this.channel_id = channel_id;
        this.channel_name = channel_name;
        this.user_rate = user_rate;
        this.global_rate = global_rate;
        this.views = views;
        this.published = published;
        this.uploaded_by = uploaded_by;
        this.isSelected = isSelected;
        this.isProgress = isProgress;
        this.video_thumbnail = video_thumbnail;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isProgress() {
        return isProgress;
    }

    public void setProgress(boolean progress) {
        isProgress = progress;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getFile_duration() {
        return file_duration;
    }

    public void setFile_duration(String file_duration) {
        this.file_duration = file_duration;
    }

    public int getOrientation_id() {
        return orientation_id;
    }

    public void setOrientation_id(int orientation_id) {
        this.orientation_id = orientation_id;
    }

    public String getOrientation_title() {
        return orientation_title;
    }

    public void setOrientation_title(String orientation_title) {
        this.orientation_title = orientation_title;
    }

    public String getUploaded_at() {
        return uploaded_at;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(int channel_id) {
        this.channel_id = channel_id;
    }

    public String getChannel_name() {
        return channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public int getUser_rate() {
        return user_rate;
    }

    public void setUser_rate(int user_rate) {
        this.user_rate = user_rate;
    }

    public float getGlobal_rate() {
        return global_rate;
    }

    public void setGlobal_rate(float global_rate) {
        this.global_rate = global_rate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }

    public String getUploaded_by() {
        return uploaded_by;
    }

    public void setUploaded_by(String uploaded_by) {
        this.uploaded_by = uploaded_by;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getSrt() {
        return srt;
    }

    public void setSrt(String srt) {
        this.srt = srt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(file_path);
        parcel.writeString(file_duration);
        parcel.writeInt(orientation_id);
        parcel.writeString(orientation_title);
        parcel.writeString(mime_type);
        parcel.writeInt(category_id);
        parcel.writeString(category_name);
        parcel.writeInt(channel_id);
        parcel.writeString(channel_name);
        parcel.writeInt(user_rate);
        parcel.writeFloat(global_rate);
        parcel.writeInt(views);
        parcel.writeInt(published);
        parcel.writeString(uploaded_by);
        parcel.writeString(video_thumbnail);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
        parcel.writeByte((byte) (isProgress ? 1 : 0));
        parcel.writeString(uploaded_at);
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }
}
