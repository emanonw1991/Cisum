package com.vivo.emanon.cisum;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

public class ScreenLockService extends Service {

    private ScreenLockBinder mScreenLockBinder = new ScreenLockBinder();

    private ScreenLockReceiver mScreenLockReceiver;
    private IntentFilter mIntentFilter;

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
        mScreenLockReceiver = new ScreenLockReceiver();
        mIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenLockReceiver, mIntentFilter);
    }

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
