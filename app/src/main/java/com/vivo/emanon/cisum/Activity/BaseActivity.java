package com.vivo.emanon.cisum.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.vivo.emanon.cisum.App.AppCache;

/**
 * 基础Activity
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSystemBarTransparent();
    }

    /**
     * 设置覆盖通知栏模式
     */
    private void setSystemBarTransparent() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    protected boolean checkServiceAlive() {
        if (AppCache.getAudioService() == null) {
            startActivity(new Intent(this, SplashActivity.class));
            AppCache.clearStack();
            return false;
        }
        return true;
    }
}
