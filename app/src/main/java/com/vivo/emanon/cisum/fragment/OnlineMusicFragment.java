package com.vivo.emanon.cisum.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.vivo.emanon.cisum.adapter.OnlineMusicAdapter;
import com.vivo.emanon.cisum.app.AppCache;
import com.vivo.emanon.cisum.app.CisumApp;
import com.vivo.emanon.cisum.constant.Constants;
import com.vivo.emanon.cisum.constant.LoadStateEnum;
import com.vivo.emanon.cisum.model.Music;
import com.vivo.emanon.cisum.R;
import com.vivo.emanon.cisum.utils.Download;
import com.vivo.emanon.cisum.utils.NetworkUtil;
import com.vivo.emanon.cisum.utils.ViewUtil;

import java.io.File;

/**
 * 在线音乐Fragment
 * Created by emanon on 2017/10/10.
 */

public class OnlineMusicFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView lvOnlineMusic;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_online_music, container, false);
        lvOnlineMusic = (ListView) rootView.findViewById(R.id.lv_online_music);
        lvOnlineMusic.setOnItemClickListener(this);
        llLoading = (LinearLayout) rootView.findViewById(R.id.ll_loading);
        llLoadFail = (LinearLayout) rootView.findViewById(R.id.ll_load_fail);
        return rootView;
    }

    @Override
    protected void init() {
        if (!NetworkUtil.isNetworkAvailable(requireContext())) {
            ViewUtil.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            return;
        }
        OnlineMusicAdapter mAdapter = new OnlineMusicAdapter();
        lvOnlineMusic.setAdapter(mAdapter);
    }

    @Override
    protected void setListener() {
        lvOnlineMusic.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Music music = AppCache.getOnlineMusicList().get(position);
        String musicPath = music.getMusicPath();
        String path = Constants.MUSIC_PATH + "/" + music.getArtist() + " - " + music.getTitle() +
                musicPath.substring(musicPath.lastIndexOf("."));
        final File file = new File(path);
        Log.d("exist", "" + file.exists());
        if (!file.exists()) {
            Download.downloadMusic(music.getMusicPath(), music.getTitle(),
                    music.getArtist());
            Download.downloadLyric(music.getLyricPath(), music.getTitle(),
                    music.getArtist());
        } else {
            Toast.makeText(CisumApp.sContext, "已存在", Toast.LENGTH_SHORT).show();
        }
    }
}
