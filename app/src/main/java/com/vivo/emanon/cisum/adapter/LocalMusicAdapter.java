package com.vivo.emanon.cisum.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vivo.emanon.cisum.app.AppCache;
import com.vivo.emanon.cisum.constant.Constants;
import com.vivo.emanon.cisum.model.Music;
import com.vivo.emanon.cisum.callback.OnMoreClickListener;
import com.vivo.emanon.cisum.R;
import com.vivo.emanon.cisum.service.AudioService;
import com.vivo.emanon.cisum.utils.CoverLoader;

/**
 * 本地音乐列表的Adapter
 * Created by emanon on 2017/10/8.
 */

public class LocalMusicAdapter extends BaseAdapter {

    //更多操作的回调方法
    private OnMoreClickListener mOnMoreClickListener;
    private int playingPosition;

    @Override
    public int getCount() {
        return AppCache.getLocalMusicList().size();
    }

    @Override
    public Object getItem(int position) {
        return AppCache.getLocalMusicList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.view_holder_music, parent, false);
            holder = new ViewHolder();
            holder.vPlaying = convertView.findViewById(R.id.v_playing);
            holder.ivCover = (ImageView) convertView.findViewById(R.id.iv_cover);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
            holder.vDivider = convertView.findViewById(R.id.v_divider);
            holder.ivMore = convertView.findViewById(R.id.iv_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == playingPosition) {
            holder.vPlaying.setVisibility(View.VISIBLE);
        } else {
            holder.vPlaying.setVisibility(View.INVISIBLE);
        }
        Music music = AppCache.getLocalMusicList().get(position);
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        holder.ivCover.setImageBitmap(cover);
        holder.tvTitle.setText(music.getTitle());
        holder.tvArtist.setText(music.getArtist());
        holder.ivMore.setOnClickListener(v -> {
            if (mOnMoreClickListener != null) {
                mOnMoreClickListener.onMoreClick(position);
            }
        });
        holder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        return convertView;
    }

    /**
     * 音乐之间的分割线是否显示
     * @param position 当前音乐的位置
     * @return 显示与否
     */
    private boolean isShowDivider(int position) {
        return position != AppCache.getLocalMusicList().size() - 1;
    }

    /**
     * 更新当前播放位置
     * @param audioService 播放服务
     */
    public void updatePlayingPosition(AudioService audioService) {
        if (audioService.getPlayingMusic() != null && audioService.getPlayingMusic().
                getType().equals(Constants.LOCAL)) {
            playingPosition = audioService.getPlayingPosition();
        } else {
            playingPosition = -1;
        }
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        mOnMoreClickListener = listener;
    }

    private static class ViewHolder {
        private View vPlaying;
        private ImageView ivCover;
        private TextView tvTitle;
        private TextView tvArtist;
        private View vDivider;
        private View ivMore;
    }
}
