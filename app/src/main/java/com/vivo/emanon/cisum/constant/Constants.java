package com.vivo.emanon.cisum.constant;

import android.os.Environment;

/**
 * 常数类
 * Created by emanon on 2017/9/25.
 */

public class Constants {
    public static final String SERVER_HOST = "http://172.25.107.96:8080";
    public static final String DOWNLOAD_DESTINATION = "Cisum";
    public static final String MUSIC_PATH = Environment.getExternalStorageDirectory() + "/" +
            DOWNLOAD_DESTINATION;
    public static final String LOCAL = "local";
    public static final String ONLINE = "online";
}
