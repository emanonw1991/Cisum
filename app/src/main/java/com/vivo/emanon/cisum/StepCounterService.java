package com.vivo.emanon.cisum;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/9/22.
 */

public class StepCounterService extends Service implements SensorEventListener,
        onStepEventListener {

    private final Handler mHandler = new Handler();

    private onStepEventListener mStepEventListener;
    private StepCounterBinder mStepCounterBinder = new StepCounterBinder();

    private long step_count;

    @Override
    public IBinder onBind(Intent intent) {
        return mStepCounterBinder;
    }

    public class StepCounterBinder extends Binder {

        public StepCounterService getService() {
            return StepCounterService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initStepCounter();
        initStep();
    }

    private void initStep() {
        long lastSavedStep = Preferences.getStepCount();
        String stepDay = Preferences.getStepDay();
        String thisDay = getThisDay();
        if (stepDay.equals(thisDay)) {
            step_count = lastSavedStep;
        } else {
            step_count = 0;
        }
    }

    private void initStepCounter() {
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(StepCounterService.this, stepCounter,
                SensorManager.SENSOR_DELAY_GAME);
        mHandler.postDelayed(mStepCountRunnable, 100);
    }

    private String getThisDay() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:dd");
        return simpleDateFormat.format(date);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        step_count++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public onStepEventListener getOnStepEventListener() {
        return mStepEventListener;
    }

    public void setOnStepEventListener(onStepEventListener listener) {
        mStepEventListener = listener;
    }

    @Override
    public void onStepChange(long step) {

    }

    private Runnable mStepCountRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStepEventListener != null) {
                mStepEventListener.onStepChange(step_count);
            }
            mHandler.postDelayed(this, 100);
        }
    };

    public long getStepCount() {
        return step_count;
    }

    @Override
    public void onDestroy() {
        Preferences.saveStepDay(getThisDay());
        Preferences.saveStepCount(step_count);
        mHandler.removeCallbacks(mStepCountRunnable);
        super.onDestroy();
    }
}
