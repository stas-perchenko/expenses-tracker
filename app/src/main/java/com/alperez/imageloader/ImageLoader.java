package com.alperez.imageloader;

import android.content.Context;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public final class ImageLoader {

    private Context mContext;
    private LoaderSettings mSettings;
    private ImageRAMCache mRamCache;


    public ImageLoader(Context context, LoaderSettings settings) {
        if (context == null) {
            throw new IllegalArgumentException("A valid instance of Context must be provided");
        } else if (settings == null) {
            throw new IllegalArgumentException("Not-null instance of settings must be provided");
        }

        mContext = context;
        mSettings = settings;

        //---- Create instance of RAM cache ---
        if (settings.isRamCacheEnabled()) {
            mRamCache = new ImageRAMCache(settings.getRamCacheSize());
        }
    }

    public LoaderSettings getSettings() {
        return mSettings;
    }

}
