package com.codersdc.codersdclibvideoplayer.models;

import com.google.android.exoplayer2.Format;

public class TrackInfo {
    public final int groupIndex;
    public final int trackIndex;
    public final Format format;
    public final String name;
    public boolean isSelected;
    public int height;
    public int width;

    public TrackInfo(int groupIndex, int trackIndex, Format format, String name,boolean isSelected,int height,int width) {
        this.groupIndex = groupIndex;
        this.trackIndex = trackIndex;
        this.format = format;
        this.name = name;
        this.isSelected = isSelected;
        this.height = height;
        this.width = width;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public Format getFormat() {
        return format;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}

