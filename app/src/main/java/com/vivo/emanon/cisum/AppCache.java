package com.vivo.emanon.cisum;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/9.
 */

public class AppCache {
    private final List<Music> localMusicList = new ArrayList<>();
    private final List<Music> onlineMusicList = new ArrayList<>();
    private final List<Activity> mActivityStack = new ArrayList<>();
    private AudioService mAudioService;
    private StepCounterService mStepCounterService;
    private ScreenLockService mScreenLockService;

    private AppCache() {
    }

    private static class SingletonHolder {
        private static AppCache sAppCache = new AppCache();
    }

    private static AppCache getInstance() {
        return SingletonHolder.sAppCache;
    }

    public static AudioService getAudioService() {
        return getInstance().mAudioService;
    }

    public static void setAudioService(AudioService service) {
        getInstance().mAudioService = service;
    }

    public static ScreenLockService getScreenLockService() {
        return getInstance().mScreenLockService;
    }

    public static void setScreenLockService(ScreenLockService service) {
        getInstance().mScreenLockService = service;
    }

    public static StepCounterService getStepCounterService() {
        return getInstance().mStepCounterService;
    }

    public static void setStepCounterService(StepCounterService stepCounterService) {
        getInstance().mStepCounterService = stepCounterService;
    }

    public static List<Music> getLocalMusicList() {
        return getInstance().localMusicList;
    }

    public static List<Music> getOnlineMusicList() {
        return getInstance().onlineMusicList;
    }

    public static void clearStack() {
        List<Activity> activityStack = getInstance().mActivityStack;
        for (int i = activityStack.size() - 1; i >= 0; i--) {
            Activity activity = activityStack.get(i);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activityStack.clear();
    }

    public static void init(Application application) {
        Preferences.init(application.getApplicationContext());
        getInstance().onInit(application);
    }

    private void onInit(Application application) {
        application.registerActivityLifecycleCallbacks(new ActivityLifecycle());
    }

    private static class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            getInstance().mActivityStack.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            getInstance().mActivityStack.remove(activity);
        }
    }
}
