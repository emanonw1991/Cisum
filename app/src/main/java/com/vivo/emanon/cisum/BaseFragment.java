package com.vivo.emanon.cisum;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by Administrator on 2017/10/9.
 */

public abstract class BaseFragment extends Fragment {
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init();
        setListener();
    }

    protected abstract void init();

    protected abstract void setListener();

    protected AudioService getAudioService() {
        AudioService audioService = AppCache.getAudioService();
        if (audioService == null) {
            throw new NullPointerException("play service is null");
        }
        return audioService;
    }
}
