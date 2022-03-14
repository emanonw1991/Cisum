package com.vivo.emanon.cisum.app;

import android.app.Application;
import android.content.Context;

/**
 * App类
 * Created by emanon on 2017/9/29.
 */

public class CisumApp extends Application{

    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        AppCache.init(this);
    }
}
