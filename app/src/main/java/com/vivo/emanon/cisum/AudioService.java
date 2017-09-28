package com.vivo.emanon.cisum;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioService extends Service {

    private MyBinder mBinder = new MyBinder();
    private int audioIndex = 0;
    private List<File> audioList = new ArrayList<>();
    private MediaPlayer player = new MediaPlayer();
    private AudioFocusManager mAudioFocusManager;
    //设置标志位的原因是由于在焦点转移的情况下，播放器除了在start状态，还可能在prepare状态
    private boolean prepared = false;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化音乐文件
        initAudios();
        //初始化播放器
        initAudioService(audioIndex);
        //设置连续轮播（播放顺序）
        player.setOnCompletionListener(mOnCompletionListener);
        mAudioFocusManager = new AudioFocusManager(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {

        public AudioService getService() {
            return AudioService.this;
        }
    }

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (audioIndex == audioList.size() - 1) {
                audioIndex = 0;
            } else {
                audioIndex++;
            }
            player.reset();
            prepared = false;
            initAudioService(audioIndex);
            start();
        }
    };

    public void start() {
        if (mAudioFocusManager.requestAudioFocus()) {
            player.start();
        }
    }

    public void pause() {
        player.pause();
    }

    public void stop() {
        player.stop();
        prepared = false;
    }

    public void playPause() {
        if (isPreparing() && isPlaying()) {
            pause();
        } else if (isPreparing() && !isPlaying()) {
            start();
        } else if (!isPreparing()) {
            initAudioService(audioIndex);
        }
    }

    public void closePlayer() {
        if (player != null) {
            player.stop();
            mAudioFocusManager.abadonAudioFocus();
            prepared = false;
            player.release();
        }
    }

    public void nextAudio() {
        if (player != null && audioIndex < audioList.size() - 1) {
            audioIndex++;
            player.reset();
            prepared = false;
            initAudioService(audioIndex);
            start();
        }
    }

    public void lastAudio() {
        if (player != null && audioIndex > 0) {
            audioIndex--;
            player.reset();
            prepared = false;
            initAudioService(audioIndex);
            start();
        }
    }

    public int getProgress() {
        return player.getDuration();
    }

    public int getPosition() {
        return player.getCurrentPosition();
    }

    public void seekToPosition(int position) {
        player.seekTo(position);
    }

    public boolean isPreparing() {
        return prepared;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    private void initAudios() {
        File[] audios = new File(Environment.getExternalStorageDirectory() + "/Music").
                listFiles();
        for (File audio : audios) {
            audioList.add(audio);
        }
    }

    private void initAudioService(int index) {
        try {
            player.setDataSource(audioList.get(index).getPath());
            //player.setDataSource(this, uri);
            player.prepare();
            prepared = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
