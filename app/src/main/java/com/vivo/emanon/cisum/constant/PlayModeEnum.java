package com.vivo.emanon.cisum.constant;

/**
 * 播放模式枚举类
 * Created by emanon on 2017/10/10.
 */

public enum PlayModeEnum {
    LOOP(0),
    SHUFFLE(1),
    SINGLE(2);

    private final int value;

    PlayModeEnum(int value) {
        this.value = value;
    }

    public static PlayModeEnum valueOf(int value) {
        switch (value) {
            case 1:
                return SHUFFLE;
            case 2:
                return SINGLE;
            case 0:
            default:
                return LOOP;
        }
    }

    public int value() {
        return value;
    }
}
