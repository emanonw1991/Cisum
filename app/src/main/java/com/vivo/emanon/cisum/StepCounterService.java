package com.vivo.emanon.cisum;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by Administrator on 2017/9/22.
 */

public class StepCounterService extends Service implements SensorEventListener {

    public static final String TAG = "stepcounter";

    SensorManager mSensorManager;

    private Messenger cMessenger = null;
    private Messenger sMessenger = new Messenger(new StepCounterHandler());

    private float currentStep = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return sMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(StepCounterService.this, stepCounter,
                SensorManager.SENSOR_DELAY_GAME);
    }

    private class StepCounterHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.MSG_FROM_CLIENT:
                    cMessenger = msg.replyTo;
                    Message msgToClient = Message.obtain();
                    msgToClient.what = Constants.MSG_FROM_SERVER;
                    Bundle bundle = new Bundle();
                    bundle.putString("step", String.valueOf(currentStep));
                    msgToClient.setData(bundle);
                    try {
                        cMessenger.send(msgToClient);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentStep = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
