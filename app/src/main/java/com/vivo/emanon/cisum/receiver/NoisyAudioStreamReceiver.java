package com.vivo.emanon.cisum.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vivo.emanon.cisum.constant.Actions;
import com.vivo.emanon.cisum.service.AudioService;

/**
 *
 * Created by emanon on 2017/10/10.
 */

public class NoisyAudioStreamReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioService.startCommand(context, Actions.ACTION_MEDIA_PLAY_PAUSE);
    }
}
