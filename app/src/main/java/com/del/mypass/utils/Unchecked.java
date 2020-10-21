package com.del.mypass.utils;

/**
 * Created by dodolinel
 * date: 17.12.14
 */
public class Unchecked {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        if (o == null) {
            return null;
        }
        return (T) o;
    }

}
