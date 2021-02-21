package com.codersdc.codersdclibvideoplayer;

import com.google.android.exoplayer2.Format;

public class TrackInfo {
    public final int groupIndex;
    public final int trackIndex;
    public final Format format;
    public final String name;

    public TrackInfo(int groupIndex, int trackIndex, Format format, String name) {
        this.groupIndex = groupIndex;
        this.trackIndex = trackIndex;
        this.format = format;
        this.name = name;
    }
}