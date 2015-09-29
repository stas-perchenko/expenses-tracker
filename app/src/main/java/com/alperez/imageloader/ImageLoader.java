package com.alperez.imageloader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.alperez.expensestracker.BuildConfig;
import com.alperez.imageloader.helpers.ImagePresentationInterface;
import com.alperez.imageloader.helpers.ImageRAMCache;
import com.alperez.imageloader.helpers.LoaderSettings;
import com.alperez.imageloader.helpers.Size;
import com.alperez.imageloader.task.BitmapWorkerTask;
import com.alperez.imageloader.utils.Utils;

import java.lang.ref.WeakReference;

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

    public void loadImage(@NonNull ImageView v, @NonNull String link, @Nullable Size scaleTo, @Nullable Bitmap placeholder, @Nullable Drawable overlay) {
        loadImageInternal(v, link, scaleTo, placeholder, overlay);
    }

    public void loadImage(@NonNull ImagePresentationInterface v, @NonNull String link, @Nullable Size scaleTo, @Nullable Bitmap placeholder, @Nullable Drawable overlay) {
        loadImageInternal((View) v, link, scaleTo, placeholder, overlay);
    }

    /**
     *
     * @param v Instance of the View where image will be loaded. Must either be an ImageView or implements ImagePresentationInterface
     * @param link Link to a picture to be loaded
     * @param placeholder    placeholder bitmap
     * @param overlay
     */
    private void loadImageInternal(@NonNull View v, @NonNull String link, @Nullable Size scaleTo, @Nullable Bitmap placeholder, @Nullable Drawable overlay) {
        Bitmap finalPlaceholder = getActualPlaceholder(placeholder);
        Drawable finalOverlay = getActualOverlay(overlay);


        if (TextUtils.isEmpty(link)) {
            setBitmapWithOverlayImmediately(v, finalPlaceholder, finalOverlay);
            return;
        }

        Bitmap bitmap = null;

        if (mRamCache != null) {
            bitmap = mRamCache.getBitmapFromMemCache(Utils.getSizedImageLink(link, scaleTo));
        }

        if (bitmap != null) {
            setBitmapWithOverlayImmediately(v, bitmap, getActualOverlay(overlay));
        } else if(checkForCurrentWork(v, link)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(link, v, scaleTo, finalPlaceholder, finalOverlay);
            final AsyncDrawable adr = new AsyncDrawable(mContext.getResources(), finalPlaceholder, task);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            task.executeOnExecutor(BitmapWorkerTask.DUAL_THREAD_EXECUTOR);
        }
    }


    /**
     * Check if there is any work being performed in this View. As a result of the checking shows whether the requesting work
     * is allowed to be launched.
     *
     * If v is not the instance of ImageView of implements ImagePresentationInterface - false is returned
     * If no work is performed for this View - true is returned
     * If a work is performed for this view and that work is for different link - that work is cancelled and true is returned
     * If a work is performed for this View and that work is for the same link - false is returned
     *
     *
     * @param v
     * @param link
     * @return true if the requesting work is allowed to be launched.
     */
    private static boolean checkForCurrentWork(View v, String link) {
        if ((v instanceof  ImageView) || (v instanceof ImagePresentationInterface)) {
            final BitmapWorkerTask workerTask = getWorkerTaskForView(v);
            if (workerTask != null) {
                final String otherLink = workerTask.getLink();
                if (otherLink == null || !otherLink.equals(link)) {
                    workerTask.cancel(true);
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "A work was cancelled for "+otherLink+". New work - "+link);
                    }
                } else {
                    // The same work is already in progress
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    private static BitmapWorkerTask getWorkerTaskForView(View v) {
        if (v != null) {
            Drawable dr = null;
            if (v instanceof ImageView) {
                dr = ((ImageView) v).getDrawable();
            } else if (v instanceof ImagePresentationInterface) {
                dr = ((ImagePresentationInterface) v).getDrawable();
            }
            if ((dr != null) && (dr instanceof  AsyncDrawable)) {
                return ((AsyncDrawable) dr).getBitmapWorkerTask();
            }
        }
        return null;
    }



    private void setBitmapWithOverlayImmediately(View v, Bitmap bmp, Drawable overlay) {
        if (overlay == null) {
            if (v instanceof ImageView) {
                ((ImageView) v).setImageBitmap(bmp);
            } else if (v instanceof ImagePresentationInterface) {
                ((ImagePresentationInterface) v).setImageBitmap(bmp);
            }
        } else {
            Drawable bdr = new BitmapDrawable(mContext.getResources(), bmp);
            Drawable ldr = new LayerDrawable(new Drawable[]{bdr, overlay});
            if (v instanceof ImageView) {
                ((ImageView) v).setImageDrawable(ldr);
            } else if (v instanceof ImagePresentationInterface) {
                ((ImagePresentationInterface) v).setImageDrawable(ldr);
            }
        }
    }

    private Bitmap getActualPlaceholder(Bitmap overridingPlaceholder) {
        return (overridingPlaceholder != null) ? overridingPlaceholder : ((mSettings != null) ? mSettings.getDefaultPlaceholder() : null);
    }

    private Drawable getActualOverlay(Drawable overridingOverlay) {
        return (overridingOverlay != null) ? overridingOverlay : ((mSettings != null) ? mSettings.getDefaultOverlay() : null);
    }


    /***********************************************************************************************
     * A custom Drawable that will be attached to the imageView while the work
     * is in progress. Contains a reference to the actual worker task, so that
     * it can be stopped if a new binding is required, and makes sure that only
     * the last started worker process can bind its result, independently of the
     * finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> workerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask workerTask) {
            super(res, bitmap);
            workerTaskReference = new WeakReference<BitmapWorkerTask>(workerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return workerTaskReference.get();
        }
    }

}
