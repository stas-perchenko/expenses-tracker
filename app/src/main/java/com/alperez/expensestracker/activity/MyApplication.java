package com.alperez.expensestracker.activity;

import android.app.Application;
import android.content.Context;

import com.alperez.expensestracker.utils.ExternalMediaException;
import com.alperez.expensestracker.utils.FileUtils;
import com.alperez.imageloader.ImageLoader;
import com.alperez.imageloader.helpers.ImageRAMCache;
import com.alperez.imageloader.helpers.ImageROMCache;
import com.alperez.imageloader.helpers.LoaderSettings;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public class MyApplication extends Application {
    public static int DISK_CACHE_SIZE_MB = 9;

    private static ImageLoader mImageLoader = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Context ctx = getApplicationContext();


        //----  Build ImageLoader instance per application  ----
        LoaderSettings.Builder sBuilder = new LoaderSettings.Builder()
                .setDefaultOverlay(null)
                .setDefaultPlaceholder(null)
                .setRamCacheEnabled(true)
                .setRamCacheSize(ImageRAMCache.getCacheSizeAsRAMPercent(ctx, 0.12f));

        mImageLoader = new ImageLoader(ctx, sBuilder.build());

        //----  Set ROM cache, which will be used by all ImageLoaders  ----
        try {
            ImageROMCache.instantiate(ctx, FileUtils.getFinalImageCacheDir(ctx), DISK_CACHE_SIZE_MB, false);
        } catch(ExternalMediaException e) {
            ImageROMCache.instantiate(ctx, null, DISK_CACHE_SIZE_MB, false);
        }
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
