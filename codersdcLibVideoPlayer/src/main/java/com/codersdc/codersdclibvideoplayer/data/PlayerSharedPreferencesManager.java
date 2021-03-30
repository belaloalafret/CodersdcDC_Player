package com.codersdc.codersdclibvideoplayer.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PlayerSharedPreferencesManager {
    private static final String PREF_NAME = "CodersDC_Lib";
    private static final String PLAYER_PREV_AUTO_PLAY = "PREV_AUTO_PLAY";
    private static final String PLAYER_CONTINUE_WATCHING = "CONTINUE_WATCHING";
    private static final String PLAYER_SUBTITLE_FONT = "SUBTITLE_FONT";
    private static final String PLAYER_SUBTITLE = "SUBTITLE";
    private final SharedPreferences mPreferences;
    Context _context;
    int PRIVATE_MODE = 0;

    public PlayerSharedPreferencesManager(Context context) {
        this._context = context;
        mPreferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    public String getPrevAutoPlay() {
        return mPreferences.getString(PLAYER_PREV_AUTO_PLAY, "off");
    }

    public void setPrevAutoPlay(String autoPlay) {
        mPreferences.edit().putString(PLAYER_PREV_AUTO_PLAY, autoPlay).apply();
    }

    public String getPlayerContinueWatching() {
        return mPreferences.getString(PLAYER_CONTINUE_WATCHING, "off");
    }

    public void setPlayerContinueWatching(String continueWatching) {
        mPreferences.edit().putString(PLAYER_CONTINUE_WATCHING, continueWatching).apply();
    }

    public String getPlayerSubtitleFont() {
        return mPreferences.getString(PLAYER_SUBTITLE_FONT, "30");
    }

    public void setPlayerSubtitleFont(String subtitleFont) {
        mPreferences.edit().putString(PLAYER_SUBTITLE_FONT, subtitleFont).apply();
    }

    public String getPlayerSubtitle() {
        return mPreferences.getString(PLAYER_SUBTITLE, "on");
    }

    public void setPlayerSubtitle(String subtitle) {
        mPreferences.edit().putString(PLAYER_SUBTITLE, subtitle).apply();
    }

}
