package com.vivo.emanon.cisum.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.vivo.emanon.cisum.adapter.LocalMusicAdapter;
import com.vivo.emanon.cisum.app.AppCache;
import com.vivo.emanon.cisum.callback.OnMoreClickListener;
import com.vivo.emanon.cisum.constant.Constants;
import com.vivo.emanon.cisum.model.Music;
import com.vivo.emanon.cisum.R;

import java.io.File;

/**
 * 本地音乐播放Fragment
 */
public class LocalMusicFragment extends BaseFragment implements AdapterView.OnItemClickListener,
        OnMoreClickListener {

    private ListView lvLocalMusic;
    private TextView tvEmpty;
    private LocalMusicAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_local_music, container, false);
        lvLocalMusic = (ListView) rootView.findViewById(R.id.lv_local_music);
        lvLocalMusic.setOnItemClickListener(this);
        tvEmpty = (TextView) rootView.findViewById(R.id.tv_empty);
        return rootView;
    }

    protected void init() {
        mAdapter = new LocalMusicAdapter();
        mAdapter.setOnMoreClickListener(this);
        lvLocalMusic.setAdapter(mAdapter);
        if (getAudioService().getPlayingMusic() != null && getAudioService().getPlayingMusic().
                getType().equals(Constants.LOCAL)) {
            lvLocalMusic.setSelection(getAudioService().getPlayingPosition());
        }
        updateView();
    }

    @Override
    protected void setListener() {
        lvLocalMusic.setOnItemClickListener(this);
    }

    /**
     * 更新列表函数，删除或新歌曲出现时
     */
    private void updateView() {
        if (AppCache.getLocalMusicList().isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        mAdapter.updatePlayingPosition(getAudioService());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getAudioService().play(position);
    }

    public void onItemPlay() {
        if (isAdded()) {
            updateView();
            if (getAudioService().getPlayingMusic().getType().equals(Constants.LOCAL)) {
                lvLocalMusic.smoothScrollToPosition(getAudioService().getPlayingPosition());
            }
        }
    }

    public void onMusicListUpdate() {
        if (isAdded()) {
            updateView();
        }
    }

    @Override
    public void onMoreClick(int position) {
        final Music music = AppCache.getLocalMusicList().get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        dialog.setTitle(music.getTitle());
        dialog.setItems(R.array.local_music_dialog, (dialog1, which) -> {
            if (which == 0) {// 分享
                deleteMusic(music);
            }
        });
        dialog.show();
    }

    /**
     * 删除音乐
     *
     * @param music 要删除的音乐
     */
    private void deleteMusic(final Music music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext());
        String title = music.getTitle();
        String msg = getString(R.string.delete_music, title);
        dialog.setMessage(msg);
        dialog.setPositiveButton(R.string.delete, (dialog1, which) -> {
            File file = new File(music.getMusicPath());
            if (file.delete()) {
                boolean playing = (music == getAudioService().getPlayingMusic());
                AppCache.getLocalMusicList().remove(music);
                if (playing) {
                    getAudioService().stop();
                    getAudioService().playPause();
                } else {
                    getAudioService().updatePlayingPosition();
                }
                updateView();

                // 刷新媒体库
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://".concat(music.getMusicPath())));
                requireContext().sendBroadcast(intent);
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }
}
