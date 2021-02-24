package com.codersdc.codersdclibvideoplayer.models;

import android.util.Pair;

import java.io.File;
import java.util.ArrayList;

public final class Sample {

    public final String uri;

    public final String name;

    public final String mimeType;

    public final File file;

    public final String srt;

    public final File srtFile;

    public final String note;

    public final ArrayList<Pair<Long, Long>> skippedPartition;

    public Sample(String uri, String name, String mimeType, File file, String srt, File srtFile, String note, String thumbURL, ArrayList<Pair<Long, Long>> skippedPartition) {
        this.uri = uri;
        this.name = name;
        this.mimeType = mimeType;
        this.file = file;
        this.srt = srt;
        this.srtFile = srtFile;
        this.note = note;
        this.skippedPartition = skippedPartition;
    }

    @Override
    public String toString() {
        return name;
    }

}