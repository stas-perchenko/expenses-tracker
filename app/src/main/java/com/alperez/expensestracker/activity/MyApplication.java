package com.alperez.expensestracker.activity;

import android.app.Application;
import android.content.Context;

import com.alperez.expensestracker.utils.ExternalMediaException;
import com.alperez.expensestracker.utils.FileUtils;
import com.alperez.imageloader.ImageLoader;
import com.alperez.imageloader.helpers.ImageRAMCache;
import com.alperez.imageloader.helpers.LoaderSettings;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public class MyApplication extends Application {
    public static int DISK_CACHE_SIZE = 9*1024*1024;

    private static ImageLoader mImageLoader = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Context ctx = getApplicationContext();



        LoaderSettings.Builder sBuilder = new LoaderSettings.Builder()
                .setDefaultOverlay(null)
                .setDefaultPlaceholder(null)
                .setRamCacheEnabled(true)
                .setRamCacheSize(ImageRAMCache.getCacheSizeAsRAMPercent(ctx, 0.12f))
                .setDiscCacheSize(DISK_CACHE_SIZE);
        try {
            sBuilder.setDiscCache(FileUtils.getFinalImageCacheDir(ctx));
        } catch(ExternalMediaException e){}

        mImageLoader = new ImageLoader(ctx, sBuilder.build());
    }

    /**
     * Check for null!
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            throw new IllegalStateException("onCreate() of the Application class has not yet been called");
        }
        return mImageLoader;
    }
}
