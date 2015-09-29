package com.alperez.imageloader.helpers;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by stanislav.perchenko on 29-Sep-15.
 */
public class ImageROMCache {
    //TODO Implement this

    private static ImageROMCache instance;

    /**
     * This method must be called once before any using of the ImageROMCache
     * @param contex
     */
    public static void instantiate(Context contex) {
        if (instance == null) {
            synchronized (ImageROMCache.class) {
                if (instance == null) {
                    instance = new ImageROMCache(context);
                }
            }
        }
    }

    public static ImageROMCache getInstance() {
        if (instance == null) throw new IllegalStateException("ImageROMCache not yet been initialized");
        return instance;
    }

    public static Bitmap decodeBitmapFromROMCache(String link, Size scaleToSize) {
        if (instance == null) throw new IllegalStateException("ImageROMCache not yet been initialized");
        Bitmap result = null;
        synchronized (instance) {
            result = instance.decodeBitmapFromCacheInternal(link, scaleToSize);
        }
        return result;
    }


    private ImageROMCache(Context context) {
        //TODO Implement this
    }


    private Bitmap decodeBitmapFromCacheInternal(String link, Size scaleToSize) {
        //TODO Implement this;
    }
}
