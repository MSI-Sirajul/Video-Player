package com.videoplayer;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "VideoPlayerSettings";

    // --- KEYS ---
    private static final String KEY_SORT_TYPE = "sort_type";
    private static final String KEY_VIEW_MODE = "view_mode"; 
    private static final String KEY_DEFAULT_HOME = "default_home"; 
    private static final String KEY_THEME = "app_theme"; 
    
    // File Management Keys
    private static final String KEY_SHOW_HIDDEN = "show_hidden_nomedia";
    private static final String KEY_HIDE_SHORT = "hide_short_videos"; // < 30s or 1 min
    
    // Storage Keys
    private static final String KEY_STORAGE_INTERNAL = "storage_internal";
    private static final String KEY_STORAGE_SDCARD = "storage_sdcard";

    // Extension Filter Keys
    private static final String KEY_EXT_ALL = "ext_all";
    private static final String KEY_EXT_MP4 = "ext_mp4";
    private static final String KEY_EXT_MKV = "ext_mkv";
    private static final String KEY_EXT_3GP = "ext_3gp";
    private static final String KEY_EXT_AVI = "ext_avi";

    // --- CONSTANTS ---
    
    // View Mode
    public static final int VIEW_LIST = 0;
    public static final int VIEW_GRID = 1;

    // Theme
    public static final int THEME_AUTO = 0; // System Default
    public static final int THEME_DARK = 1;
    public static final int THEME_LIGHT = 2;

    // Sorting
    public static final int SORT_NAME_AZ = 0;
    public static final int SORT_NAME_ZA = 1;
    public static final int SORT_DATE_NEW = 2;
    public static final int SORT_DATE_OLD = 3;
    public static final int SORT_SIZE_LARGE = 4;
    public static final int SORT_SIZE_SMALL = 5;
    public static final int SORT_DURATION = 6;

    // Constructor
    public SettingsManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    // ===========================
    // 1. SORTING & VIEW
    // ===========================
    public void setSortType(int type) {
        editor.putInt(KEY_SORT_TYPE, type).apply();
    }
    public int getSortType() {
        return preferences.getInt(KEY_SORT_TYPE, SORT_DATE_NEW); // Default: Newest
    }

    public void setViewMode(int mode) {
        editor.putInt(KEY_VIEW_MODE, mode).apply();
    }
    public int getViewMode() {
        return preferences.getInt(KEY_VIEW_MODE, VIEW_LIST); // Default: List
    }

    // ===========================
    // 2. HOME PAGE
    // ===========================
    public void setDefaultHome(int tabIndex) { 
        editor.putInt(KEY_DEFAULT_HOME, tabIndex).apply();
    }
    public int getDefaultHome() {
        return preferences.getInt(KEY_DEFAULT_HOME, 0); // 0=All Videos, 1=Folder
    }

    // ===========================
    // 3. SHOW / HIDE FILES
    // ===========================
    public void setShowHidden(boolean show) {
        editor.putBoolean(KEY_SHOW_HIDDEN, show).apply();
    }
    public boolean getShowHidden() {
        return preferences.getBoolean(KEY_SHOW_HIDDEN, false); // Default: Hide hidden files
    }

    public void setHideShortVideos(boolean hide) {
        editor.putBoolean(KEY_HIDE_SHORT, hide).apply();
    }
    public boolean getHideShortVideos() {
        return preferences.getBoolean(KEY_HIDE_SHORT, false); // Default: Show all lengths
    }

    // ===========================
    // 4. EXTENSION FILTERS
    // ===========================
    public void setFilterAll(boolean enable) { editor.putBoolean(KEY_EXT_ALL, enable).apply(); }
    public boolean getFilterAll() { return preferences.getBoolean(KEY_EXT_ALL, true); } // Default: All ON

    public void setFilterMp4(boolean enable) { editor.putBoolean(KEY_EXT_MP4, enable).apply(); }
    public boolean getFilterMp4() { return preferences.getBoolean(KEY_EXT_MP4, false); }

    public void setFilterMkv(boolean enable) { editor.putBoolean(KEY_EXT_MKV, enable).apply(); }
    public boolean getFilterMkv() { return preferences.getBoolean(KEY_EXT_MKV, false); }

    public void setFilter3gp(boolean enable) { editor.putBoolean(KEY_EXT_3GP, enable).apply(); }
    public boolean getFilter3gp() { return preferences.getBoolean(KEY_EXT_3GP, false); }

    public void setFilterAvi(boolean enable) { editor.putBoolean(KEY_EXT_AVI, enable).apply(); }
    public boolean getFilterAvi() { return preferences.getBoolean(KEY_EXT_AVI, false); }

    // ===========================
    // 5. STORAGE TOGGLE
    // ===========================
    public void setEnableInternal(boolean enable) { editor.putBoolean(KEY_STORAGE_INTERNAL, enable).apply(); }
    public boolean getEnableInternal() { return preferences.getBoolean(KEY_STORAGE_INTERNAL, true); } // Default ON

    public void setEnableSdCard(boolean enable) { editor.putBoolean(KEY_STORAGE_SDCARD, enable).apply(); }
    public boolean getEnableSdCard() { return preferences.getBoolean(KEY_STORAGE_SDCARD, true); } // Default ON

    // ===========================
    // 6. THEME
    // ===========================
    public void setAppTheme(int themeMode) {
        editor.putInt(KEY_THEME, themeMode).apply();
    }
    public int getAppTheme() {
        return preferences.getInt(KEY_THEME, THEME_AUTO); // Default: Auto
    }
}