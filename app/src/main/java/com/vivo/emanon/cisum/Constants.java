package com.vivo.emanon.cisum;

import android.os.Environment;

/**
 * Created by Administrator on 2017/9/25.
 */

public class Constants {
    public static final int MSG_FROM_CLIENT = 0;
    public static final int MSG_FROM_SERVER = 1;
    public static final int REQUEST_SERVER = 2;
    public static final String SERVER_HOST = "http://172.25.107.96:8080";
    public static final String DOWNLOAD_DESTINATION = "Cisum";
    public static final String MUSIC_PATH = Environment.getExternalStorageDirectory() + "/" +
            DOWNLOAD_DESTINATION;
    public static final String LOCAL = "local";
    public static final String ONLINE = "online";
}
