package com.vivo.emanon.cisum.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.vivo.emanon.cisum.Constant.Actions;
import com.vivo.emanon.cisum.Service.AudioService;

/**
 * 通知栏控制接收广播
 * Created by Administrator on 2017/10/12.
 */

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_NOTIFICATION = "cisum.NOTIFICATION_ACTIONS";
    public static final String EXTRA = "extra";
    public static final String EXTRA_NEXT = "next";
    public static final String EXTRA_PLAY_PAUSE = "play_pause";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }

        String extra = intent.getStringExtra(EXTRA);
        if (TextUtils.equals(extra, EXTRA_NEXT)) {
            AudioService.startCommand(context, Actions.ACTION_MEDIA_NEXT);
        } else if (TextUtils.equals(extra, EXTRA_PLAY_PAUSE)) {
            AudioService.startCommand(context, Actions.ACTION_MEDIA_PLAY_PAUSE);
        }
    }
}
