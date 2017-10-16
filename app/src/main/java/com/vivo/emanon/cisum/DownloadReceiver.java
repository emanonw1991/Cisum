package com.vivo.emanon.cisum;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/10/9.
 */

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
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
