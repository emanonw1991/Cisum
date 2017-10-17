package com.vivo.emanon.cisum.Callback;

/**
 * 扫描音乐回调函数
 * Created by emanon on 2017/10/9.
 */

public interface EventCallback<T> {
    void onEvent(T t);
}