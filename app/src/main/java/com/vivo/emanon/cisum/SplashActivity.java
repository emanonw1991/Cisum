package com.vivo.emanon.cisum;

import android.content.Intent;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private TextView tvCopyright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initViews();

        int year = Calendar.getInstance().get(Calendar.YEAR);
        tvCopyright.setText(getString(R.string.copyright, year));

        initService();
    }

    private void initViews() {
        tvCopyright = (TextView) findViewById(R.id.tv_copyright);
    }

    private void initService() {
        Intent intent = new Intent(this, AudioService.class);
        startActivity(intent);
        intent = new Intent(this, StepCounterService.class);
        startActivity(intent);
    }
}
