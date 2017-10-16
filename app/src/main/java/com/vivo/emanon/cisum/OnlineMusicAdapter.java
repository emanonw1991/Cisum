package com.vivo.emanon.cisum;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/10/10.
 */

public class OnlineMusicAdapter extends BaseAdapter {

    private static final String TAG = "OnlineMusicAdapter";

    private OnMoreClickListener mOnMoreClickListener;

    @Override
    public int getCount() {
        return AppCache.getOnlineMusicList().size();
    }

    @Override
    public Object getItem(int position) {
        return AppCache.getOnlineMusicList().get(position);
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
            holder.ivCover = (ImageView) convertView.findViewById(R.id.iv_cover);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
            holder.vDivider = convertView.findViewById(R.id.v_divider);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Music music = AppCache.getOnlineMusicList().get(position);
        Log.d(TAG, "" + AppCache.getOnlineMusicList().size());
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        holder.ivCover.setImageBitmap(cover);
        holder.tvTitle.setText(music.getTitle());
        holder.tvArtist.setText(music.getArtist());
        holder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        return convertView;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        mOnMoreClickListener = listener;
    }

    private boolean isShowDivider(int position) {
        return position != AppCache.getOnlineMusicList().size() - 1;
    }

    private static class ViewHolder {
        private ImageView ivCover;
        private TextView tvTitle;
        private TextView tvArtist;
        private View vDivider;
    }
}
