package com.vivo.emanon.cisum.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vivo.emanon.cisum.R;
import com.vivo.emanon.cisum.utils.ScreenUtil;

/**
 * 播放页Indicator
 * Created by Administrator on 2017/10/10.
 */

public class IndicatorLayout extends LinearLayout {
    public IndicatorLayout(Context context) {
        this(context, null);
    }

    public IndicatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
    }

    public void create(int count) {
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            int padding = ScreenUtil.dp2px(3);
            imageView.setPadding(padding, 0, padding, 0);
            imageView.setImageResource(i == 0 ? R.drawable.ic_play_page_indicator_selected :
                    R.drawable.ic_play_page_indicator_unselected);
            addView(imageView);
        }
    }

    /**
     * 当前页面
     * @param position 页面位置
     */
    public void setCurrent(int position) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ImageView imageView = (ImageView) getChildAt(i);
            if (i == position) {
                imageView.setImageResource(R.drawable.ic_play_page_indicator_selected);
            } else {
                imageView.setImageResource(R.drawable.ic_play_page_indicator_unselected);
            }
        }
    }
}
