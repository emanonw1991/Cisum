package com.vivo.emanon.cisum.Utils;

import android.view.View;

import com.vivo.emanon.cisum.Constant.LoadStateEnum;

/**
 * View工具类
 * Created by emanon on 2017/10/10.
 */

public class ViewUtil {
    /**
     * View显示状态
     * @param loadSuccess 加载成功
     * @param loading 加载中
     * @param loadFail 加载失败
     * @param state 加载状态
     */
    public static void changeViewState(View loadSuccess, View loading, View loadFail, LoadStateEnum state) {
        switch (state) {
            case LOADING:
                loadSuccess.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                loadFail.setVisibility(View.GONE);
                break;
            case LOAD_SUCCESS:
                loadSuccess.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                loadFail.setVisibility(View.GONE);
                break;
            case LOAD_FAIL:
                loadSuccess.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                loadFail.setVisibility(View.VISIBLE);
                break;
        }
    }
}
