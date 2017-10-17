package com.vivo.emanon.cisum.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vivo.emanon.cisum.Adapter.FragmentAdapter;
import com.vivo.emanon.cisum.Callback.OnPlayerEventListener;
import com.vivo.emanon.cisum.Constant.Extras;
import com.vivo.emanon.cisum.Fragment.LocalMusicFragment;
import com.vivo.emanon.cisum.Fragment.OnlineMusicFragment;
import com.vivo.emanon.cisum.Fragment.PlayFragment;
import com.vivo.emanon.cisum.Model.Music;
import com.vivo.emanon.cisum.R;
import com.vivo.emanon.cisum.Service.AudioService;
import com.vivo.emanon.cisum.Utils.CoverLoader;

import static com.vivo.emanon.cisum.App.AppCache.getAudioService;

/**
 * 主界面Activity
 */
public class MainActivity extends BaseActivity implements View.OnClickListener,
        OnPlayerEventListener, NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener{

    private DrawerLayout drawerLayout;
    private TextView tvLocalMusic;
    private TextView tvOnlineMusic;
    private ViewPager mViewPager;
    private ImageView ivPlayBarCover;
    private TextView tvPlayBarTitle;
    private TextView tvPlayBarArtist;
    private ImageView ivPlayBarPlay;
    private ProgressBar mProgressBar;
    private LocalMusicFragment mLocalMusicFragment;
    private PlayFragment mPlayFragment;
    private AudioService mAudioService;
    private boolean isPlayFragmentShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //启动主界面时总是检查服务是否开启
        if (!checkServiceAlive()) {
            return;
        }

        //设置回调函数以与Service进行通信
        mAudioService = getAudioService();
        mAudioService.setOnPlayEventListener(this);

        initViews();
        setupViews();
        //registerReceiver();
        onChangeImpl(getAudioService().getPlayingMusic());
        parseIntent();
    }

    /**
     * 通知栏控制处理
     * @param intent 通知栏控制Intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    private void initViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        ImageView ivMenu = (ImageView) findViewById(R.id.iv_menu);
        tvLocalMusic = (TextView) findViewById(R.id.tv_local_music);
        tvOnlineMusic = (TextView) findViewById(R.id.tv_online_music);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        FrameLayout flPlayBar = (FrameLayout) findViewById(R.id.fl_play_bar);
        ivPlayBarCover = (ImageView) findViewById(R.id.iv_play_bar_cover);
        tvPlayBarTitle = (TextView) findViewById(R.id.tv_play_bar_title);
        tvPlayBarArtist = (TextView) findViewById(R.id.tv_play_bar_artist);
        ivPlayBarPlay = (ImageView) findViewById(R.id.iv_play_bar_play);
        ImageView ivPlayBarNext = (ImageView) findViewById(R.id.iv_play_bar_next);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_play_bar);
        navigationView.setNavigationItemSelectedListener(this);
        ivMenu.setOnClickListener(this);
        //
        mViewPager.setOnPageChangeListener(this);
        tvLocalMusic.setOnClickListener(this);
        tvOnlineMusic.setOnClickListener(this);
        flPlayBar.setOnClickListener(this);
        ivPlayBarPlay.setOnClickListener(this);
        ivPlayBarNext.setOnClickListener(this);
    }

    private void setupViews() {
        mLocalMusicFragment = new LocalMusicFragment();
        OnlineMusicFragment mOnlineMusicFragment = new OnlineMusicFragment();
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(mLocalMusicFragment);
        adapter.addFragment(mOnlineMusicFragment);
        mViewPager.setAdapter(adapter);
        tvLocalMusic.setSelected(true);
    }

    /*
    private void registerReceiver() {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }
    */

    @Override
    public void onChange(Music music) {
        onChangeImpl(music);
        if (mPlayFragment != null) {
            mPlayFragment.onChange(music);
        }
    }

    @Override
    public void onPlayerStart() {
        ivPlayBarPlay.setSelected(true);
        if (mPlayFragment != null) {
            mPlayFragment.onPlayerStart();
        }
    }

    @Override
    public void onPlayerPause() {
        ivPlayBarPlay.setSelected(false);
        if (mPlayFragment != null) {
            mPlayFragment.onPlayerPause();
        }
    }

    @Override
    public void onPublish(int progress) {
        mProgressBar.setProgress(progress);
        if (mPlayFragment != null) {
            mPlayFragment.onPublish(progress);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        if (mPlayFragment != null) {
            mPlayFragment.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onMusicListUpdate() {
        if (mLocalMusicFragment != null) {
            mLocalMusicFragment.onMusicListUpdate();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.tv_local_music:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_online_music:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.fl_play_bar:
                showPlayingFragment();
                break;
            case R.id.iv_play_bar_play:
                play();
                break;
            case R.id.iv_play_bar_next:
                next();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        drawerLayout.closeDrawers();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);
        switch (item.getItemId()) {
            case R.id.nav_exit:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getAudioService().stop();
                        finish();
                    }
                }, 100);
                break;
            case R.id.nav_switch_user:
                getAudioService().stop();
                SharedPreferences.Editor mEditor = PreferenceManager.getDefaultSharedPreferences(this)
                        .edit();
                mEditor.putBoolean("login", false);
                mEditor.apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.nav_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            tvLocalMusic.setSelected(true);
            tvOnlineMusic.setSelected(false);
        } else {
            tvLocalMusic.setSelected(false);
            tvOnlineMusic.setSelected(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 通知栏控制音乐播放的Intent进行分析
     */
    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extras.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    /**
     * 更换播放音乐方法
     * @param music 当前播放的音乐
     */
    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        ivPlayBarCover.setImageBitmap(cover);
        tvPlayBarTitle.setText(music.getTitle());
        tvPlayBarArtist.setText(music.getArtist());
        ivPlayBarPlay.setSelected(getAudioService().isPlaying() || getAudioService().isPreparing());
        mProgressBar.setMax(music.getDuration());
        mProgressBar.setProgress((int) getAudioService().getCurrentPosition());

        if (mLocalMusicFragment != null) {
            mLocalMusicFragment.onItemPlay();
        }
    }

    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    private void play() {
        getAudioService().playPause();
    }

    private void next() {
        getAudioService().next();
    }

    @Override
    protected void onDestroy() {
        if (mAudioService != null) {
            mAudioService.setOnPlayEventListener(null);
        }
        super.onDestroy();
    }
}
