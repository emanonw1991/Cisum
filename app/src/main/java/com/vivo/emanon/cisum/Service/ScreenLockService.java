package com.vivo.emanon.cisum.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.vivo.emanon.cisum.Activity.ScreenLockActivity;
import com.vivo.emanon.cisum.App.AppCache;

/**
 * 锁屏监听Service
 */
public class ScreenLockService extends Service {

    private ScreenLockBinder mScreenLockBinder = new ScreenLockBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mScreenLockBinder;
    }

    public class ScreenLockBinder extends Binder {

        public ScreenLockService getService() {
            return ScreenLockService.this;
        }
    }

    @Override
    public void onCreate() {
        ScreenLockReceiver mScreenLockReceiver = new ScreenLockReceiver();
        IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenLockReceiver, mIntentFilter);
    }

    /**
     * 锁屏时则发送广播拉起Activity
     */
    private class ScreenLockReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (AppCache.getAudioService().isPlaying()) {
                    Intent screenLock = new Intent(ScreenLockService.this,
                            ScreenLockActivity.class);
                    screenLock.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(screenLock);
                }
            }
        }
    }
}
