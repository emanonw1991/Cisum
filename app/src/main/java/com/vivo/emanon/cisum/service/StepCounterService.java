package com.vivo.emanon.cisum.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.vivo.emanon.cisum.callback.onStepEventListener;
import com.vivo.emanon.cisum.utils.Preferences;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 计步器服务
 * Created by emanon on 2017/9/22.
 */

public class StepCounterService extends Service implements SensorEventListener,
        onStepEventListener {

    private final Handler mHandler = new Handler();

    //回调方法，用于Service与Activity通信
    private onStepEventListener mStepEventListener;
    private final StepCounterBinder mStepCounterBinder = new StepCounterBinder();

    //步数
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

    /**
     * 初始化步数，如果是新一天则重新计起
     */
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

    /**
     * 获取当天日期
     * @return 当天日期
     */
    private String getThisDay() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM:dd");
        return simpleDateFormat.format(date);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        step_count++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setOnStepEventListener(onStepEventListener listener) {
        mStepEventListener = listener;
    }

    @Override
    public void onStepChange(long step) {

    }

    private final Runnable mStepCountRunnable = new Runnable() {
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
