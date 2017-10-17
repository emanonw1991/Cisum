package com.vivo.emanon.cisum.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.vivo.emanon.cisum.App.AppCache;
import com.vivo.emanon.cisum.App.CisumApp;
import com.vivo.emanon.cisum.Service.AudioService;

/**
 * 下载结束广播
 * Created by emanon on 2017/10/9.
 */

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(CisumApp.sContext, "下载成功", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scanMusic();
            }
        }, 1000);
    }

    private void scanMusic() {
        AudioService audioService = AppCache.getAudioService();
        if (audioService != null) {
            audioService.updateMusicList(null);
        }
    }
}
