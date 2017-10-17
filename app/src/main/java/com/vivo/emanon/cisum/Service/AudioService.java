package com.vivo.emanon.cisum.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.vivo.emanon.cisum.App.AppCache;
import com.vivo.emanon.cisum.Callback.EventCallback;
import com.vivo.emanon.cisum.Callback.OnPlayerEventListener;
import com.vivo.emanon.cisum.Constant.Actions;
import com.vivo.emanon.cisum.Constant.PlayModeEnum;
import com.vivo.emanon.cisum.Model.Music;
import com.vivo.emanon.cisum.Receiver.NoisyAudioStreamReceiver;
import com.vivo.emanon.cisum.Utils.LocalMusicUtil;
import com.vivo.emanon.cisum.Utils.Preferences;
import com.vivo.emanon.cisum.Views.NotificationController;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * 音乐播放服务
 */
public class AudioService extends Service {

    private static final String TAG = "AudioService";

    //player状态：idle，preparing，playing，pause
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;

    private MyBinder mBinder = new MyBinder();
    private MediaPlayer player = new MediaPlayer();
    private AudioFocusManager mAudioFocusManager;
    private final Handler mHandler = new Handler();
    //实现与Activity通信的回调函数
    private OnPlayerEventListener mListener;
    private OnPlayerEventListener mScreenListener;
    private final NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private final IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    //播放列表
    private final List<Music> mMusicList = AppCache.getLocalMusicList();

    private Music playingMusic;
    private int playingPosition = -1;
    private int playState = STATE_IDLE;

    @Override
    public void onCreate() {
        super.onCreate();
        player.setOnCompletionListener(mOnCompletionListener);
        mAudioFocusManager = new AudioFocusManager(this);
        NotificationController.init(this);
    }

    /**
     * 通知栏控制
     * @param context 上下文
     * @param action 动作
     */
    public static void startCommand(Context context, String action) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Actions.ACTION_MEDIA_PLAY_PAUSE:
                    playPause();
                    break;
                case Actions.ACTION_MEDIA_NEXT:
                    next();
                    break;
                case Actions.ACTION_MEDIA_PREVIOUS:
                    prev();
                    break;
            }
        }
        return START_NOT_STICKY;
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
            next();
        }
    };

    /**
     * 设置与Activity通信的回调函数
     * @param listener Activity的回调函数
     */
    public void setOnPlayEventListener(OnPlayerEventListener listener) {
        mListener = listener;
    }

    public void setOnScreenPlayEventListener(OnPlayerEventListener listener) {
        mScreenListener = listener;
    }

    public void play(int position) {
        if (mMusicList.isEmpty()) {
            return;
        }

        //未选择歌曲时如何处理
        if (position < 0) {
            //position = mMusicList.size() - 1;
            position = 0;
        } else if (position >= mMusicList.size()) {
            position = 0;
        }

        playingPosition = position;
        Music music = mMusicList.get(playingPosition);
        //缓存当前正在播放的音乐的ID
        Preferences.saveCurrentSongId(music.getId());
        play(music);
    }

    public void play(Music music) {
        playingMusic = music;
        try {
            player.reset();
            player.setDataSource(music.getMusicPath());
            Log.d(TAG, music.getMusicPath());
            player.prepareAsync();
            playState = STATE_PREPARING;
            player.setOnPreparedListener(mPreparedListener);
            player.setOnBufferingUpdateListener(mBufferingUpdateListener);
            //通知Activity
            if (mListener != null) {
                mListener.onChange(music);
            }
            if (mScreenListener != null) {
                mScreenListener.onChange(music);
            }
            NotificationController.showPlay(music);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (isPreparing()) {
                start();
            }
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mListener != null) {
                mListener.onBufferingUpdate(percent);
            }
        }
    };

    void start() {
        if (!isPreparing() && !isPausing()) {
            return;
        }

        if (mAudioFocusManager.requestAudioFocus()) {
            player.start();
            playState = STATE_PLAYING;
            mHandler.post(mPublishRunnable);
            NotificationController.showPlay(playingMusic);
            registerReceiver(mNoisyReceiver, mNoisyFilter);
            if (mListener != null) {
                mListener.onPlayerStart();
            }
            if (mScreenListener != null) {
                mScreenListener.onPlayerStart();
            }
        }
    }

    void pause() {
        if (!isPlaying()) {
            return;
        }

        player.pause();
        playState = STATE_PAUSE;
        mHandler.removeCallbacks(mPublishRunnable);
        NotificationController.showPause(playingMusic);
        unregisterReceiver(mNoisyReceiver);

        if (mListener != null) {
            mListener.onPlayerPause();
        }
        if (mScreenListener != null) {
            mScreenListener.onPlayerPause();
        }
    }

    public void stop() {
        if (isIdle()) {
            return;
        }

        pause();
        player.reset();
        playState = STATE_IDLE;
    }

    public void playPause() {
        if (isPreparing()) {
            stop();
        } else if (isPlaying()) {
            pause();
        } else if (isPausing()) {
            start();
        } else {
            play(getPlayingPosition());
        }
    }

    public void next() {
        if (mMusicList.isEmpty()) {
            return;
        }

        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                playingPosition = new Random().nextInt(mMusicList.size());
                play(playingPosition);
                break;
            case SINGLE:
                play(playingPosition);
                break;
            case LOOP:
            default:
                play(playingPosition + 1);
                break;
        }
    }

    public void prev() {
        if (mMusicList.isEmpty()) {
            return;
        }

        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                playingPosition = new Random().nextInt(mMusicList.size());
                play(playingPosition);
                break;
            case SINGLE:
                play(playingPosition);
                break;
            case LOOP:
            default:
                play(playingPosition - 1);
                break;
        }
    }

    public void seekTo(int msec) {
        if (isPlaying() || isPausing()) {
            player.seekTo(msec);
            if (mListener != null) {
                mListener.onPublish(msec);
            }
            if (mScreenListener != null) {
                mScreenListener.onPublish(msec);
            }
        }
    }

    public boolean isPlaying() {
        return playState == STATE_PLAYING;
    }

    public boolean isPausing() {
        return playState == STATE_PAUSE;
    }

    public boolean isPreparing() {
        return playState == STATE_PREPARING;
    }

    public boolean isIdle() {
        return playState == STATE_IDLE;
    }

    public int getPlayingPosition() {
        return playingPosition;
    }

    public Music getPlayingMusic() {
        return playingMusic;
    }

    public long getCurrentPosition() {
        if (isPlaying() || isPausing()) {
            return player.getCurrentPosition();
        } else {
            return 0;
        }
    }

    private Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying() && mListener != null) {
                mListener.onPublish(player.getCurrentPosition());
            }
            if (isPlaying() && mScreenListener != null) {
                mScreenListener.onPublish(player.getCurrentPosition());
            }
            mHandler.postDelayed(this, 100);
        }
    };

    public void onDestroy() {
        player.reset();
        player.release();
        player = null;
        mAudioFocusManager.abandonAudioFocus();
        NotificationController.cancelAll();
        AppCache.setAudioService(null);
        super.onDestroy();
    }

    /**
     * 扫描音乐
     */
    public void updateMusicList(final EventCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                LocalMusicUtil.scanLocalMusic(AudioService.this, mMusicList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (!mMusicList.isEmpty()) {
                    updatePlayingPosition();
                    playingMusic = mMusicList.get(playingPosition);
                }

                if (mListener != null) {
                    mListener.onMusicListUpdate();
                }

                if (callback != null) {
                    callback.onEvent(null);
                }
            }
        }.execute();
    }

    /**
     * 删除或下载歌曲后刷新正在播放的本地歌曲的序号
     */
    public void updatePlayingPosition() {
        int position = 0;
        long id = Preferences.getCurrentSongId();
        for (int i = 0; i < mMusicList.size(); i++) {
            if (mMusicList.get(i).getId() == id) {
                position = i;
                break;
            }
        }
        playingPosition = position;
        Preferences.saveCurrentSongId(mMusicList.get(playingPosition).getId());
    }
}
