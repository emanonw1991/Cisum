package com.vivo.emanon.cisum.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 缓存相关工具类
 * Created by emanon on 2017/10/9.
 */

public class Preferences {
    private static final String MUSIC_ID = "music_id";
    private static final String PLAY_MODE = "play_mode";
    private static final String STEP_COUNT = "step_count";
    private static final String STEP_DAY = "step_day";

    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    /**
     * 获取和储存当前播放音乐的ID
     * @return 当前播放音乐的ID
     */
    public static long getCurrentSongId() {
        return getLong(MUSIC_ID, -1);
    }

    public static void saveCurrentSongId(long id) {
        saveLong(MUSIC_ID, id);
    }

    /**
     * 获取和存储播放模式
     * @return 播放模式
     */
    public static int getPlayMode() {
        return getInt(PLAY_MODE, 0);
    }

    public static void savePlayMode(int mode) {
        saveInt(PLAY_MODE, mode);
    }

    /**
     * 获取和储存步数
     * @return 步数
     */
    public static long getStepCount() {
        return getLong(STEP_COUNT, 0);
    }

    public static void saveStepCount(long step_count) {
        saveLong(STEP_COUNT, step_count);
    }

    /**
     * 获取和储存日期
     * @return 日期
     */
    public static String getStepDay() {
        return getString(STEP_DAY, "-1");
    }

    public static void saveStepDay(String step_day) {
        saveString(STEP_DAY, step_day);
    }

    private static int getInt(String key, int defValue) {
        return getPreferences().getInt(key, defValue);
    }

    private static void saveInt(String key, int value) {
        getPreferences().edit().putInt(key, value).apply();
    }

    private static long getLong(String key, long defValue) {
        return getPreferences().getLong(key, defValue);
    }

    private static void saveLong(String key, long value) {
        getPreferences().edit().putLong(key, value).apply();
    }

    private static String getString(String key, String defValue) {
        return getPreferences().getString(key, defValue);
    }

    private static void saveString(String key, String defValue) {
        getPreferences().edit().putString(key, defValue).apply();
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sContext);
    }
}