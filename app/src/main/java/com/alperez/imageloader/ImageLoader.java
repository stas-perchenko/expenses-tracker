package com.alperez.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.alperez.imageloader.helpers.ImagePresentationInterface;
import com.alperez.imageloader.helpers.ImageRAMCache;
import com.alperez.imageloader.helpers.LoaderSettings;

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

    public void loadImage(@NonNull ImageView v, @NonNull String link, @Nullable Bitmap placeholder, @Nullable Drawable overlay) {
        loadImageInternal(v, link, placeholder, overlay);
    }

    public void loadImage(@NonNull ImagePresentationInterface v, @NonNull String link, @Nullable Bitmap placeholder, @Nullable Drawable overlay) {
        loadImageInternal((View) v, link, placeholder, overlay);
    }

    /**
     *
     * @param v Instance of the View where image will be loaded. Must either be an ImageView or implements ImagePresentationInterface
     * @param link Link to a picture to be loaded
     * @param placeholder    placeholder bitmap
     * @param overlay
     */
    private void loadImageInternal(@NonNull View v, @NonNull String link, @Nullable Bitmap placeholder, @Nullable Drawable overlay) {
        //TODO Implement this
    }
}
