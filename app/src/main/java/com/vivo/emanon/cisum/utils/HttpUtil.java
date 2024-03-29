package com.vivo.emanon.cisum.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Http相关类
 * Created by emanon on 2017/9/16.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
