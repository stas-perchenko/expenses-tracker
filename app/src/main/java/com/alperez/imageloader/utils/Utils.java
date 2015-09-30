package com.alperez.imageloader.utils;

import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.alperez.imageloader.helpers.Size;

/**
 * Created by stanislav.perchenko on 29-Sep-15.
 */
public class Utils {

    public static String getSizedImageLink(String link, @Nullable Size size) {
        return (size != null) ? (link + size.getSuffixForLink()) : link;
    }

    /**
     * Creates new options with downscale factor set.
     * @param inOpts Options with decoded bounds
     * @param targX requested width
     * @param targY requested height
     * @return new instance of Options with inJustDecodeBounds = false and properly set up inSampleSize (downscale factor)
     */
    public static BitmapFactory.Options getScaledOptions(BitmapFactory.Options inOpts, int targX, int targY) {
        float[] factors = new float[]{1f, 2f, 4f, 8f, 16f, 32f, 64f, 128f, 256f, 1024f, 2048f, 4096f};
        int index = 0;
        BitmapFactory.Options outOpts = new BitmapFactory.Options();
        outOpts.inJustDecodeBounds = false;
        outOpts.inSampleSize = (int)factors[index];
        try {
            while (true) {
                final float nextFactor = factors[index+1];
                if ((Math.round((float)inOpts.outWidth / nextFactor) < targX) || (Math.round((float)inOpts.outHeight / nextFactor) < targY)) {
                    break;
                }
                outOpts.inSampleSize = (int)nextFactor;
                index++;
            }
        } catch(IndexOutOfBoundsException e) {}
        return outOpts;
    }

}
