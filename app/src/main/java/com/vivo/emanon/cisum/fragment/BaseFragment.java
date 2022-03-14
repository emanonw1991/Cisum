package com.vivo.emanon.cisum.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vivo.emanon.cisum.app.AppCache;
import com.vivo.emanon.cisum.service.AudioService;

/**
 * 基础Fragment
 * Created by emanon on 2017/10/9.
 */

public abstract class BaseFragment extends Fragment {
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
