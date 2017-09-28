package com.vivo.emanon.cisum;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2017/9/21.
 */

public class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

    private AudioService mAudioService;
    private AudioManager mAudioManager;
    private boolean isPausedByFocusLossTransient;
    private int mVolumeWhenFocusLossTransientCanDuck;

    public AudioFocusManager(@NonNull AudioService audioService) {
        mAudioService = audioService;
        mAudioManager = (AudioManager) audioService.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 播放音乐前请求音频焦点
     */
    public boolean requestAudioFocus() {
        return mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) ==  AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 退出播放器后不再占用音频焦点
     */
    public void abadonAudioFocus() {
        mAudioManager.abandonAudioFocus(this);
    }

    /**
     * 音频焦点监听
     * @param focusChange
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        int volume;
        switch (focusChange) {
            //重新获得焦点
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!willPlay() && isPausedByFocusLossTransient) {
                    //通话结束，恢复播放
                    mAudioService.playPause();
                }
                volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (mVolumeWhenFocusLossTransientCanDuck > 0 && volume ==
                        mVolumeWhenFocusLossTransientCanDuck / 2) {
                    //恢复音量
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            mVolumeWhenFocusLossTransientCanDuck, AudioManager.
                                    FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                isPausedByFocusLossTransient = false;
                mVolumeWhenFocusLossTransientCanDuck = 0;
                break;
            //永久丢失焦点，如被其他播放器抢占
            case AudioManager.AUDIOFOCUS_LOSS:
                if (willPlay()) {
                    forceStop();
                }
                break;
            //短暂丢失焦点，如来电
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (willPlay()) {
                    forceStop();
                    isPausedByFocusLossTransient = true;
                }
                break;
            //瞬间丢失焦点，如通知
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //音量减小一半
                volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (willPlay() && volume > 0) {
                    mVolumeWhenFocusLossTransientCanDuck = volume;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            mVolumeWhenFocusLossTransientCanDuck / 2,
                            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                break;
        }
    }

    private boolean willPlay() {
        return mAudioService.isPreparing() || mAudioService.isPlaying();
    }

    private void forceStop() {
        if (mAudioService.isPlaying()) {
            mAudioService.pause();
        } else {
            mAudioService.stop();
        }
    }
}
