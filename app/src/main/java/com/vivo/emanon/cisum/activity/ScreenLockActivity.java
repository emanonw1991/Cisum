package com.vivo.emanon.cisum.activity;

import static com.vivo.emanon.cisum.app.AppCache.getAudioService;
import static com.vivo.emanon.cisum.app.AppCache.getStepCounterService;
import static com.vivo.emanon.cisum.app.CisumApp.sContext;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vivo.emanon.cisum.callback.OnPlayerEventListener;
import com.vivo.emanon.cisum.callback.onStepEventListener;
import com.vivo.emanon.cisum.constant.Constants;
import com.vivo.emanon.cisum.constant.PlayModeEnum;
import com.vivo.emanon.cisum.model.Music;
import com.vivo.emanon.cisum.R;
import com.vivo.emanon.cisum.service.AudioService;
import com.vivo.emanon.cisum.service.StepCounterService;
import com.vivo.emanon.cisum.utils.CoverLoader;
import com.vivo.emanon.cisum.utils.Download;
import com.vivo.emanon.cisum.utils.Preferences;
import com.vivo.emanon.cisum.utils.ScreenUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import me.wcy.lrcview.LrcView;

/**
 * 锁屏Activity
 */
public class ScreenLockActivity extends SwipeBackActivity implements Handler.Callback,
        View.OnClickListener, OnPlayerEventListener, onStepEventListener {
    private static final int TIME_MESSAGE = 0;

    private ImageView ivScreenCover;
    private TextView tvSystemTime;
    private TextView tvScreenTitleArtist;
    private TextView tvStepCounter;
    private LrcView mLrcViewScreen;
    private ImageView ivScreenMode;
    private ImageView ivScreenPrev;
    private ImageView ivScreenPlay;
    private ImageView ivScreenNext;
    private Handler mHandler;
    private AudioService mAudioService;
    private StepCounterService mStepCounterService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_screen_lock);

        initViews();
        setListeners();
        initPlayMode();
        initSwipeBackLayout();
        initService();
        refreshSystemTime();
        onChangeImpl(mAudioService.getPlayingMusic());
        onStepChange(mStepCounterService.getStepCount());
    }

    private void initViews() {
        ivScreenCover = (ImageView) findViewById(R.id.iv_screen_cover);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        tvScreenTitleArtist = (TextView) findViewById(R.id.tv_screen_title_artist);
        tvStepCounter = (TextView) findViewById(R.id.tv_stepcounter);
        mLrcViewScreen = (LrcView) findViewById(R.id.lrc_view_screen);
        ivScreenMode = (ImageView) findViewById(R.id.iv_screen_mode);
        ivScreenPrev = (ImageView) findViewById(R.id.iv_screen_prev);
        ivScreenPlay = (ImageView) findViewById(R.id.iv_screen_play);
        ivScreenNext = (ImageView) findViewById(R.id.iv_screen_next);
    }

    private void initService() {
        mAudioService = getAudioService();
        mAudioService.setOnScreenPlayEventListener(this);
        mStepCounterService = getStepCounterService();
        mStepCounterService.setOnStepEventListener(this);
    }

    private void setListeners() {
        ivScreenPrev.setOnClickListener(this);
        ivScreenPlay.setOnClickListener(this);
        ivScreenNext.setOnClickListener(this);
        ivScreenMode.setOnClickListener(this);
    }

    /**
     * 刷新系统时间
     */
    private void refreshSystemTime() {
        mHandler = new Handler(this);
        new TimeThread().start();
    }

    /**
     * 初始化SwipeBackLayout
     */
    private void initSwipeBackLayout() {
        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
        setSwipeBackEnable(true);
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_BOTTOM);
        swipeBackLayout.setEdgeSize(ScreenUtil.getScreenWidth());
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == TIME_MESSAGE) {
            long currentTime = System.currentTimeMillis();
            Date date = new Date(currentTime);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat systemTime = new SimpleDateFormat("HH:mm");
            tvSystemTime.setText(systemTime.format(date));
        }
        return true;
    }

    /**
     * 更新系统时间的线程
     */
    private class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = TIME_MESSAGE;
                    mHandler.sendMessageDelayed(msg, 100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_screen_prev:
                prev();
                break;
            case R.id.iv_screen_play:
                play();
                break;
            case R.id.iv_screen_next:
                next();
                break;
            case R.id.iv_screen_mode:
                switchPlayMode();
                break;
        }
    }

    /**
     * 更换音乐的方法
     * @param music 当前播放的音乐
     */
    @SuppressLint("SetTextI18n")
    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        ivScreenCover.setImageBitmap(CoverLoader.getInstance().loadThumbnail(music));
        tvScreenTitleArtist.setText(music.getTitle() + " - " +music.getArtist());

        setLrc(music);
        ivScreenPlay.setSelected(mAudioService.isPlaying() || mAudioService.isPreparing());
    }

    private void setLrc(final Music music) {
        if (music.getType().equals(Constants.LOCAL)) {
            String lrcPath = music.getLyricPath();
            if (!TextUtils.isEmpty(lrcPath)) {
                loadLrc(lrcPath);
            } else {
                mLrcViewScreen.setLabel("暂无歌词");
            }
        } else {
            String lrcPath = music.getLyricPath();
            Download.downloadLyric(lrcPath, music.getTitle(), music.getArtist());
            lrcPath = Constants.MUSIC_PATH + "/" + music.getArtist() + " - " + music.getTitle() +
                    ".lrc";
            loadLrc(lrcPath);
        }
    }

    private void loadLrc(String path) {
        File file = new File(path);
        mLrcViewScreen.loadLrc(file);
    }

    /**
     * 刷新界面的方法
     * @param progress 当前播放进度
     */
    @Override
    public void onPublish(int progress) {
        if (mLrcViewScreen.hasLrc()) {
            mLrcViewScreen.updateTime(progress);
        }
    }

    @Override
    public void onChange(Music music) {
        onChangeImpl(music);
    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onPlayerStart() {
        ivScreenPlay.setSelected(true);
    }

    @Override
    public void onPlayerPause() {
        ivScreenPlay.setSelected(false);
    }

    @Override
    public void onMusicListUpdate() {

    }

    private void play() {
        mAudioService.playPause();
    }

    private void next() {
        mAudioService.next();
    }

    private void prev() {
        mAudioService.prev();
    }

    private void initPlayMode() {
        int mode = Preferences.getPlayMode();
        ivScreenMode.setImageLevel(mode);
    }

    private void switchPlayMode() {
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case LOOP:
                mode = PlayModeEnum.SHUFFLE;
                Toast.makeText(sContext, R.string.mode_shuffle, Toast.LENGTH_SHORT).show();
                break;
            case SHUFFLE:
                mode = PlayModeEnum.SINGLE;
                Toast.makeText(sContext, R.string.mode_single, Toast.LENGTH_SHORT).show();
                break;
            case SINGLE:
                mode = PlayModeEnum.LOOP;
                Toast.makeText(sContext, R.string.mode_loop, Toast.LENGTH_SHORT).show();
                break;
        }
        Preferences.savePlayMode(mode.value());
        initPlayMode();
    }

    @Override
    protected void onDestroy() {
        mAudioService.setOnScreenPlayEventListener(null);
        mStepCounterService.setOnStepEventListener(null);
        super.onDestroy();
    }

    @Override
    public void onStepChange(long step) {
        String stepInfo = "今日步数：" + ((Long) step);
        tvStepCounter.setText(stepInfo);
    }
}
