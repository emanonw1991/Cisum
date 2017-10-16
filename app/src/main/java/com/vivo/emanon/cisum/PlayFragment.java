package com.vivo.emanon.cisum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.wcy.lrcview.LrcView;

import static com.vivo.emanon.cisum.CisumApp.sContext;

/**
 * Created by Administrator on 2017/10/10.
 */

public class PlayFragment extends BaseFragment implements View.OnClickListener,
        ViewPager.OnPageChangeListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "PlayFragment";

    private LinearLayout llContent;
    private ImageView ivPlayingBg;
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvArtist;
    private ViewPager vpPlay;
    private IndicatorLayout ilIndicator;
    private SeekBar sbProgress;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private ImageView ivMode;
    private ImageView ivPlay;
    private ImageView ivNext;
    private ImageView ivPrev;
    private AlbumCoverView mAlbumCoverView;
    private LrcView mLrcViewSingle;
    private LrcView mLrcViewFull;
    private SeekBar sbVolume;
    private AudioManager mAudioManager;
    private List<View> mViewPagerContent;
    private int mLastProgress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);
        llContent = (LinearLayout) rootView.findViewById(R.id.ll_content);
        ivPlayingBg = (ImageView) rootView.findViewById(R.id.iv_play_page_bg);
        ivBack = (ImageView) rootView.findViewById(R.id.iv_back);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        tvArtist = (TextView) rootView.findViewById(R.id.tv_artist);
        vpPlay = (ViewPager) rootView.findViewById(R.id.vp_play_page);
        ilIndicator = (IndicatorLayout) rootView.findViewById(R.id.il_indicator);
        sbProgress = (SeekBar) rootView.findViewById(R.id.sb_progress);
        tvCurrentTime = (TextView) rootView.findViewById(R.id.tv_current_time);
        tvTotalTime = (TextView) rootView.findViewById(R.id.tv_total_time);
        ivMode = (ImageView) rootView.findViewById(R.id.iv_mode);
        ivPlay = (ImageView) rootView.findViewById(R.id.iv_play);
        ivNext = (ImageView) rootView.findViewById(R.id.iv_next);
        ivPrev = (ImageView) rootView.findViewById(R.id.iv_prev);
        return rootView;
    }

    @Override
    protected void init() {
        initSystemBar();
        initViewPager();
        ilIndicator.create(mViewPagerContent.size());
        initPlayMode();
        onChangeImpl(getAudioService().getPlayingMusic());
    }

    private void initSystemBar() {
        int top = ScreenUtil.getSystemBarHeight();
        llContent.setPadding(0, top, 0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Actions.VOLUME_CHANGED_ACTION);
        getContext().registerReceiver(mVolumeReceiver, filter);
    }

    @Override
    protected void setListener() {
        ivBack.setOnClickListener(this);
        ivMode.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivPrev.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        sbProgress.setOnSeekBarChangeListener(this);
        sbVolume.setOnSeekBarChangeListener(this);
        vpPlay.setOnPageChangeListener(this);
    }

    private void initViewPager() {
        View coverView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_cover, null);
        View lrcView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_lrc, null);
        mAlbumCoverView = (AlbumCoverView) coverView.findViewById(R.id.album_cover_view);
        mLrcViewSingle = (LrcView) coverView.findViewById(R.id.lrc_view_single);
        mLrcViewFull = (LrcView) lrcView.findViewById(R.id.lrc_view_full);
        sbVolume = (SeekBar) lrcView.findViewById(R.id.sb_volume);
        mAlbumCoverView.initNeedle(getAudioService().isPlaying());
        initVolume();

        mViewPagerContent = new ArrayList<>(2);
        mViewPagerContent.add(coverView);
        mViewPagerContent.add(lrcView);
        vpPlay.setAdapter(new PlayPagerAdapter(mViewPagerContent));
    }

    private void initVolume() {
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        sbVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    private void initPlayMode() {
        int mode = Preferences.getPlayMode();
        ivMode.setImageLevel(mode);
    }

    public void onChange(Music music) {
        if (isAdded()) {
            onChangeImpl(music);
        }
    }

    public void onPlayerStart() {
        if (isAdded()) {
            ivPlay.setSelected(true);
            mAlbumCoverView.start();
        }
    }

    public void onPlayerPause() {
        if (isAdded()) {
            ivPlay.setSelected(false);
            mAlbumCoverView.pause();
        }
    }

    /**
     * 更新播放进度
     */
    public void onPublish(int progress) {
        if (isAdded()) {
            sbProgress.setProgress(progress);
            if (mLrcViewSingle.hasLrc()) {
                mLrcViewSingle.updateTime(progress);
                mLrcViewFull.updateTime(progress);
            }
            //更新当前播放时间
            if (progress - mLastProgress >= 1000) {
                tvCurrentTime.setText(formatTime(progress));
                mLastProgress = progress;
            }
        }
    }

    public void onBufferingUpdate(int percent) {
        if (isAdded()) {
            sbProgress.setSecondaryProgress(sbProgress.getMax() * 100 / percent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_mode:
                switchPlayMode();
                break;
            case R.id.iv_play:
                play();
                break;
            case R.id.iv_next:
                next();
                break;
            case R.id.iv_prev:
                prev();
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        ilIndicator.setCurrent(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == sbProgress) {
            if (getAudioService().isPlaying() || getAudioService().isPausing()) {
                int progress = seekBar.getProgress();
                getAudioService().seekTo(progress);
                mLrcViewSingle.onDrag(progress);
                mLrcViewFull.onDrag(progress);
                tvCurrentTime.setText(formatTime(progress));
                mLastProgress = progress;
            } else {
                seekBar.setProgress(0);
            }
        } else if (seekBar == sbVolume) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(),
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
    }

    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        tvTitle.setText(music.getTitle());
        tvArtist.setText(music.getArtist());
        sbProgress.setProgress((int) getAudioService().getCurrentPosition());
        sbProgress.setSecondaryProgress(0);
        sbProgress.setMax(music.getDuration());
        mLastProgress = 0;
        tvCurrentTime.setText(R.string.play_time_start);
        tvTotalTime.setText(formatTime(music.getDuration()));
        setCoverAndBg(music);
        Log.i(TAG, music.getType());
        setLrc(music);
        if (getAudioService().isPlaying() || getAudioService().isPreparing()) {
            ivPlay.setSelected(true);
            mAlbumCoverView.start();
        } else {
            ivPlay.setSelected(false);
            mAlbumCoverView.pause();
        }
    }

    private void play() {
        getAudioService().playPause();
    }

    private void next() {
        getAudioService().next();
    }

    private void prev() {
        getAudioService().prev();
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

    private void onBackPressed() {
        getActivity().onBackPressed();
        ivBack.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivBack.setEnabled(true);
            }
        }, 300);
    }

    private void setCoverAndBg(Music music) {
        mAlbumCoverView.setCoverBitmap(CoverLoader.getInstance().loadRound(music));
        ivPlayingBg.setImageBitmap(CoverLoader.getInstance().loadBlur(music));
    }

    private void setLrc(final Music music) {
        if (music.getType().equals(Constants.LOCAL)) {
            String lrcPath = music.getLyricPath();
            if (!TextUtils.isEmpty(lrcPath)) {
                loadLrc(lrcPath);
            } else {
                setLrcLabel("暂无歌词");
            }
        } else {
            String lrcPath = music.getLyricPath();
            Download.downloadLyric(lrcPath, music.getTitle(), music.getArtist());
            lrcPath = Constants.MUSIC_PATH + "/" + music.getArtist() + " - " + music.getTitle() +
                    ".lrc";
            loadLrc(lrcPath);
        }
    }

    private void setLrcLabel(String label) {
        mLrcViewFull.setLabel(label);
        mLrcViewSingle.setLabel(label);
    }

    private void loadLrc(String path) {
        File file = new File(path);
        mLrcViewSingle.loadLrc(file);
        mLrcViewFull.loadLrc(file);
    }

    private String formatTime(long time) {
        int m = (int) (time / DateUtils.MINUTE_IN_MILLIS);
        int s = (int) ((time / DateUtils.SECOND_IN_MILLIS) % 60);
        String mm = String.format(Locale.getDefault(), "%02d", m);
        String ss = String.format(Locale.getDefault(), "%02d", s);
        return mm + ":" + ss;
    }

    private BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    };

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mVolumeReceiver);
        super.onDestroy();
    }
}
