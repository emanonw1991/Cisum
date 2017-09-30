package com.vivo.emanon.cisum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SplashActivity extends BaseActivity {

    private ImageView ivSplash;
    private TextView tvCopyright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initViews();
        loadSplash();

        int year = Calendar.getInstance().get(Calendar.YEAR);
        tvCopyright.setText(getString(R.string.copyright, year));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!LOGIN) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                } else {

                }
                finish();
            }
        }, 1000);
    }

    private void initViews() {
        tvCopyright = (TextView) findViewById(R.id.tv_copyright);
        ivSplash = (ImageView) findViewById(R.id.iv_splash);
    }

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


}
