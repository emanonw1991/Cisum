package com.vivo.emanon.cisum;

/**
 * Created by Administrator on 2017/10/9.
 */

public interface EventCallback<T> {
    void onEvent(T t);
}