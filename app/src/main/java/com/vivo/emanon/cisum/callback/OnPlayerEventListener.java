package com.vivo.emanon.cisum.callback;

import com.vivo.emanon.cisum.model.Music;

/**
 * 播放音乐时间的回调方法接口，用于Service和Activity之间通信
 * Created by emanon on 2017/10/9.
 */

public interface OnPlayerEventListener {

    /**
     * 切换歌曲
     */
    void onChange(Music music);

    /**
     * 继续播放
     */
    void onPlayerStart();

    /**
     * 暂停播放
     */
    void onPlayerPause();

    /**
     * 更新进度
     */
    void onPublish(int progress);

    /**
     * 缓冲百分比
     */
    void onBufferingUpdate(int percent);

    /**
     * 更新定时停止播放时间
     */

    void onMusicListUpdate();
}
