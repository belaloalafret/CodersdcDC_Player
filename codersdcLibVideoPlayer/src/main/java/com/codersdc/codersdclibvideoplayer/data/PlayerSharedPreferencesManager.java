package com.codersdc.codersdclibvideoplayer.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.codersdc.codersdclibvideoplayer.models.HomeMusicItems;
import com.codersdc.codersdclibvideoplayer.models.HomeVideosItems;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlayerSharedPreferencesManager {
    private static final String PREF_NAME = "CodersDC_Lib";
    private static final String PLAYER_PREV_AUTO_PLAY = "PREV_AUTO_PLAY";
    private static final String PLAYER_CONTINUE_WATCHING = "CONTINUE_WATCHING";
    private static final String PLAYER_SUBTITLE_FONT = "SUBTITLE_FONT";
    private static final String PLAYER_SUBTITLE = "SUBTITLE";
    private static final String LIST_MUSIC = "list_music";
    private static final String LIST_VIDEO = "list_video";

    Context _context;
    int PRIVATE_MODE = 0;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public PlayerSharedPreferencesManager(Context context) {
        this._context = context;
        mPreferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        mEditor = mPreferences.edit();
    }

    public static void writeListMusic(Context context, List<HomeMusicItems> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LIST_MUSIC, jsonString);
        editor.apply();
    }

    public static List<HomeMusicItems> readListFromMusic(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(LIST_MUSIC, "");

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<HomeMusicItems>>() {
        }.getType();
        List<HomeMusicItems> list = gson.fromJson(jsonString, type);
        return list;
    }

    public static void writeListVideo(Context context, List<HomeVideosItems> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LIST_VIDEO, jsonString);
        editor.apply();
    }

    public static List<HomeVideosItems> readListFromVideo(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(LIST_VIDEO, "");

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<HomeVideosItems>>() {
        }.getType();
        List<HomeVideosItems> list = gson.fromJson(jsonString, type);
        return list;
    }

    public String getPrevAutoPlay() {
        return mPreferences.getString(PLAYER_PREV_AUTO_PLAY, "off");
    }

    //--------------------------------------------------------------------------------------------//
    public void setPrevAutoPlay(String autoPlay) {
        mPreferences.edit().putString(PLAYER_PREV_AUTO_PLAY, autoPlay).apply();
    }

    public String getPlayerContinueWatching() {
        return mPreferences.getString(PLAYER_CONTINUE_WATCHING, "off");
    }

    //--------------------------------------------------------------------------------------------//
    public void setPlayerContinueWatching(String continueWatching) {
        mPreferences.edit().putString(PLAYER_CONTINUE_WATCHING, continueWatching).apply();
    }

    public String getPlayerSubtitleFont() {
        return mPreferences.getString(PLAYER_SUBTITLE_FONT, "30");
    }

    //--------------------------------------------------------------------------------------------//
    public void setPlayerSubtitleFont(String subtitleFont) {
        mPreferences.edit().putString(PLAYER_SUBTITLE_FONT, subtitleFont).apply();
    }

    public String getPlayerSubtitle() {
        return mPreferences.getString(PLAYER_SUBTITLE, "on");
    }

    //--------------------------------------------------------------------------------------------//
    public void setPlayerSubtitle(String subtitle) {
        mPreferences.edit().putString(PLAYER_SUBTITLE, subtitle).apply();
    }
    //--------------------------------------------------------------------------------------------//

}
