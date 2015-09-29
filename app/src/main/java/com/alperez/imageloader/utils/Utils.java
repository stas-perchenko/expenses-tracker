package com.alperez.imageloader.utils;

import android.support.annotation.Nullable;

import com.alperez.imageloader.helpers.Size;

/**
 * Created by stanislav.perchenko on 29-Sep-15.
 */
public class Utils {

    public static String getSizedImageLink(String link, @Nullable Size size) {
        return (size != null) ? (link + size.getSuffixForLink()) : link;
    }

}
