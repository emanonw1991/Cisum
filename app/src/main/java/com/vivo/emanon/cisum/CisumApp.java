package com.vivo.emanon.cisum;

import android.app.Application;
import android.content.Intent;

/**
 * Created by Administrator on 2017/9/29.
 */

public class CisumApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, AudioService.class));
        startService(new Intent(this, StepCounterService.class));
    }
}
