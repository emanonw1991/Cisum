package com.vivo.emanon.cisum.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vivo.emanon.cisum.App.AppCache;
import com.vivo.emanon.cisum.App.CisumApp;
import com.vivo.emanon.cisum.Constant.Constants;
import com.vivo.emanon.cisum.R;
import com.vivo.emanon.cisum.Service.AudioService;
import com.vivo.emanon.cisum.Service.ScreenLockService;
import com.vivo.emanon.cisum.Service.StepCounterService;
import com.vivo.emanon.cisum.Utils.HttpUtil;
import com.vivo.emanon.cisum.Utils.LocalMusicUtil;
import com.vivo.emanon.cisum.Utils.OnlineMusicUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/*
**欢迎界面，每日更新界面图
 */
public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";

    private ImageView ivSplash;
    private TextView tvCopyright;

    //音乐播放服务，锁屏监听服务，记步服务
    ServiceConnection mPlayServiceConnection;
    ServiceConnection mScreenLockServiceConnection;
    ServiceConnection mStepCounterConnection;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CisumApp.sContext);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //初始化View
        initViews();
        //copyright
        int year = Calendar.getInstance().get(Calendar.YEAR);
        tvCopyright.setText(getString(R.string.copyright, year));
        //更新欢迎图
        updateSplash();
        //初始化播放模式
        initMode();
        //检查存放音乐的文件夹是否存在
        checkDirectory();
        //检查服务是否都已经启动，已启动则是通知栏控制引起，直接进入MainActivity，未启动则是启动App
        boolean isService = checkService();
        if (isService) {
            startMainActivity();
            finish();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean isLogin = prefs.getBoolean("login", false);
                    if (!isLogin) {
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                    finish();
                }
            }, 3000);
        }
    }

    /**
     * 初始化播放模式，首先从缓存中读取，若没有则使用顺序播放模式，并存入缓存
     */
    private void initMode() {
        int playMode = prefs.getInt("play_mode", -1);
        if (playMode == -1) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("play_mode", 0);
            editor.apply();
        }
    }

    /**
     * 初始化View
     */
    private void initViews() {
        tvCopyright = (TextView) findViewById(R.id.tv_copyright);
        ivSplash = (ImageView) findViewById(R.id.iv_splash);
    }

    /**
     *
     * @return 检查服务是否存在，不存在则启动
     */
    private boolean checkService() {
        boolean isService = true;
        if (AppCache.getAudioService() == null) {
            startAudioService();
            isService = false;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindAudioService();
                }
            }, 100);
        }
        if (AppCache.getScreenLockService() == null) {
            startScreenLockService();
            isService = false;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindScreenLockService();
                }
            }, 100);
        }
        if (AppCache.getStepCounterService() == null) {
            startStepCounterService();
            isService = false;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindStepCounterService();
                }
            }, 100);
        }
        return isService;
    }

    private void startAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        startService(intent);
    }

    private void startScreenLockService() {
        Intent intent = new Intent(this, ScreenLockService.class);
        startService(intent);
    }

    private void startStepCounterService() {
        Intent intent = new Intent(this, StepCounterService.class);
        startService(intent);
    }

    private void bindAudioService() {
        Intent intent = new Intent(this, AudioService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private void bindScreenLockService() {
        Intent intent = new Intent(this, ScreenLockService.class);
        mScreenLockServiceConnection = new ScreenLockServiceConnection();
        bindService(intent, mScreenLockServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private void bindStepCounterService() {
        Intent intent = new Intent(this, StepCounterService.class);
        mStepCounterConnection = new StepCounterServiceConnection();
        bindService(intent, mStepCounterConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * 启动音乐播放服务，将服务对象存入全局变量，同时扫描本地和在线音乐
     */
    private class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final AudioService audioService = ((AudioService.MyBinder) service).getService();
            AppCache.setAudioService(audioService);
            LocalMusicUtil.scanLocalMusic(CisumApp.sContext, AppCache.getLocalMusicList());
            OnlineMusicUtil.scanOnlineMusic();
            Log.d(TAG, "" + AppCache.getLocalMusicList().size());
            Log.d(TAG, "" + AppCache.getOnlineMusicList().size());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class ScreenLockServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final ScreenLockService screenLockService = ((ScreenLockService.ScreenLockBinder)
                    service).getService();
            AppCache.setScreenLockService(screenLockService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class StepCounterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final StepCounterService stepCounterService = ((StepCounterService.StepCounterBinder)
                    service).getService();
            AppCache.setStepCounterService(stepCounterService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    /**
     * 检查音乐文件夹是否存在，不存在则创建
     */
    private void checkDirectory() {
        try {
            File file = new File(Constants.MUSIC_PATH);
            if (!file.exists()) {
                file.mkdir();
            } else {
                if (!file.isDirectory()) {
                    file.mkdirs();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载必应每日一图，并将加载图和加载时间一起存入缓存
     */
    private void loadSplash() {
        String requestSplash = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestSplash, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String splash = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(SplashActivity.this).edit();
                editor.putString("splash", splash);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-mm-dd");
                String date = simpleDateFormat.format(new java.util.Date());
                editor.putString("date", date);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(SplashActivity.this).load(splash).into(ivSplash);
                    }
                });
            }
        });
    }

    /**
     * 每次启动都更新加载图，如果是同一天，则不更换加载图，直接从缓存中读取
     */
    private void updateSplash() {
        String bufferedDate = prefs.getString("date", null);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-mm-dd");
        String date = simpleDateFormat.format(new java.util.Date());
        if (bufferedDate == null) {
            loadSplash();
        } else {
            if (bufferedDate.equals(date)) {
                String splash = prefs.getString("splash", null);
                Glide.with(this).load(splash).into(ivSplash);
            } else {
                loadSplash();
            }
        }
    }

    /**
     * 由通知栏拉起App，则直接进入MainActivity，同时将通知栏传来的Intent交给MainActivity，
     * 并且设置其SingleInstance启动
     */
    private void startMainActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.putExtras(getIntent());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * 销毁时释放服务连接资源
     */
    @Override
    protected void onDestroy() {
        if (mPlayServiceConnection != null) {
            unbindService(mPlayServiceConnection);
        }
        if (mStepCounterConnection != null) {
            unbindService(mStepCounterConnection);
        }
        if (mScreenLockServiceConnection != null) {
            unbindService(mScreenLockServiceConnection);
        }
        super.onDestroy();
    }
}
