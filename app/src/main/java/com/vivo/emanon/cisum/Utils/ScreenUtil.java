package com.vivo.emanon.cisum.Utils;

import android.content.Context;
import android.view.WindowManager;

import static com.vivo.emanon.cisum.App.CisumApp.sContext;

/**
 * 屏幕相关工具类
 * Created by Administrator on 2017/10/12.
 */

public class ScreenUtil {

    /**
     * 获取屏幕宽度
     * @return 屏幕宽度
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    /**
     * 获取状态栏高度
     */
    public static int getSystemBarHeight() {
        int result = 0;
        int resourceId = sContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = sContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * dp值转换为px值
     * @param dpValue dp值
     * @return px值
     */
    public static int dp2px(float dpValue) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /*
    public static int px2dp(float pxValue) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = sContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    */
}
