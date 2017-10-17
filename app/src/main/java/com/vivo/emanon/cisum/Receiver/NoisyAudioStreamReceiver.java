package com.vivo.emanon.cisum.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vivo.emanon.cisum.Constant.Actions;
import com.vivo.emanon.cisum.Service.AudioService;

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
