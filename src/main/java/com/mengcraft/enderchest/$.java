package com.mengcraft.enderchest;

/**
 * Created by on 2017/8/7.
 */
public class $ {

    public static void thr(boolean b, String message) {
        if (b) throw new IllegalStateException(message);
    }

    public static boolean nil(Object any) {
        return any == null;
    }
}
