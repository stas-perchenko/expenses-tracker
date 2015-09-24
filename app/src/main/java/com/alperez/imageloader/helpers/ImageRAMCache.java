package com.alperez.imageloader.helpers;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.alperez.expensestracker.BuildConfig;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public class ImageRAMCache {
    private static final String TAG = "ImageRAMCache";

    // Default memory cache size
    public static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 5; // 5MB

    private LruCache<String, Bitmap> mMemoryCache;


    public ImageRAMCache(int size) {
        mMemoryCache = new LruCache<String, Bitmap>(size > 0 ? size : DEFAULT_MEM_CACHE_SIZE){
            /**
             * Measure item size in bytes rather than units which is more
             * practical for a bitmap cache
             */
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount();
                }
                // Pre HC-MR1
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }


    /**
     * Adds a bitmap to memory cache only.
     *
     * @param key
     *            Unique identifier for the bitmap to store
     * @param bitmap
     *            The bitmap to store
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap, boolean forceUpdate) {
        if (key == null || bitmap == null) {
            return;
        }

        if (forceUpdate || mMemoryCache.get(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public boolean removeFromCache(String key){
        if(key == null){
            return false;
        }

        // Remove from memory cache
        return (mMemoryCache.remove(key) != null);
    }

    /**
     * Get from memory cache.
     *
     * @param data
     *            Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        final Bitmap memBitmap = mMemoryCache.get(data);
        if (memBitmap != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Memory cache hit - "+data);
            }
            return memBitmap;
        }
        return null;
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache
     * object. Note that this includes disk access so this should not be
     * executed on the main/UI thread.
     */
    public void clearCache() {
        mMemoryCache.evictAll();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Memory cache cleared");
        }
    }


    /**
     *
     * @param ctx
     * @param percent part of available memory [0.05 ... 0.3]
     * @return
     */
    public static int getCacheSizeAsRAMPercent(Context ctx, float percent) {
        if (percent < 0.05f || percent > 0.3f) {
            throw new IllegalArgumentException("must be between 0.05 and 0.8 (inclusive)");
        }
        return Math.round(percent * ((ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 1024 * 1024);
    }

}
