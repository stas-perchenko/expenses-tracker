package com.alperez.imageloader.task;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.alperez.imageloader.helpers.ImageExternalProvider;
import com.alperez.imageloader.helpers.ImagePresentationInterface;
import com.alperez.imageloader.helpers.ImageRAMCache;
import com.alperez.imageloader.helpers.ImageROMCache;
import com.alperez.imageloader.helpers.Size;
import com.alperez.imageloader.utils.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by stanislav.perchenko on 29-Sep-15.
 */
public class BitmapWorkerTask extends AsyncTask<Void, Void, BitmapWorkerTask.Result> {
    private static final String TAG = "BitmapWorkerTask";

    private final String link;
    private final Size scaleToSize;
    private final boolean useImageROMCache;
    private WeakReference<ImageView> imageViewReference;
    private WeakReference<ImagePresentationInterface> imagePresenterReference;
    private final WeakReference<Drawable> overlayReference;
    private final WeakReference<Bitmap> loadingBitmapReference;
    private final WeakReference<ImageRAMCache> ramCacheReference;
    private final WeakReference<ImageExternalProvider> extImageProviderReference;

    /**
     * Single variable which is used for all instances of this task for managing sequential execution.
     */
    private static long timeLastStart = 0;
    private static final Object timeLocker = new Object(); // Locker object for above time holder

    public BitmapWorkerTask(@Nullable ImageExternalProvider extImageProvider, @NonNull String link, @NonNull View v, @Nullable Size scaleToSize, @Nullable ImageRAMCache ramCache, boolean useImageROMCache, @Nullable Bitmap placeholder, @Nullable Drawable overlay) {
        this.link = link;
        this.scaleToSize = scaleToSize;
        this.useImageROMCache = useImageROMCache;
        if (v instanceof ImageView) {
            imageViewReference = new WeakReference<ImageView>((ImageView) v);
        } else if (v instanceof ImagePresentationInterface) {
            imagePresenterReference = new WeakReference<ImagePresentationInterface>((ImagePresentationInterface) v);
        }
        overlayReference = new WeakReference<Drawable>(overlay);
        loadingBitmapReference = new WeakReference<Bitmap>(placeholder);
        ramCacheReference = new WeakReference<ImageRAMCache>(ramCache);
        extImageProviderReference = new WeakReference<ImageExternalProvider>(extImageProvider);
    }

    public String getLink() {
        return this.link;
    }

    @Override
    protected Result doInBackground(Void... params) {
        if ((imageViewReference == null) && imagePresenterReference == null) return null;

        // Check for task been canceled
        if (getParentViewforThisTask() == null || isCancelled()) {
            return null;
        }


        Result result = new Result();
        result.placeholder = this.loadingBitmapReference;
        result.overlay = this.overlayReference;

        //----  Try to get bitmap from ROM cache  ----
        Bitmap bitmap = ImageROMCache.getBitmapFromROMCache(link, scaleToSize);
        if (bitmap != null) {
            ImageRAMCache rCace = this.ramCacheReference.get();
            if (rCace != null) {
                rCace.addBitmapToMemoryCache(Utils.getSizedImageLink(link, scaleToSize), bitmap, true);
            }
            result.bitmap = bitmap;
            result.fromNetwork = false;

            synchronized (timeLocker) {
                long curr_time = System.currentTimeMillis();
                if ((curr_time - timeLastStart) < 80) {
                    try {
                        Thread.sleep(80 - (curr_time - timeLastStart));
                    } catch (InterruptedException e) {}
                }
                timeLastStart = System.currentTimeMillis();
            }
            Log.d(TAG, "doInBackground - complete with disc cache");
            return result;
        }

        // Wait till at least 300 ms from the last task start, while checking for task been canceled
        synchronized (timeLocker) {
            while ((System.currentTimeMillis() - timeLastStart) < 300) {
                if (getParentViewforThisTask() == null || isCancelled()) {
                    break;
                }
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {}
            }
            Log.d(TAG, "doInBackground - starting work. time = "+Long.toString(System.currentTimeMillis() - timeLastStart));
            timeLastStart = System.currentTimeMillis();
        }

        // Check for task been canceled
        if (getParentViewforThisTask() == null || isCancelled()) {
            Log.d(TAG, "doInBackground - break task 2");
            return null;
        }

        byte[] rawData = null;
        ImageExternalProvider extLoader = extImageProviderReference.get();
        if (extLoader != null) {
            //----  Use external loader, provided by client code  ----
            rawData = extLoader.getImageDataSynchronously(this.link);
        } else {
            //----  Use internal loader  ----
            Context ctx = tryGetContext();
            if ((ctx == null) || ((ctx != null) && Utils.isNetworkAvailable(ctx, true))) {
                rawData = Utils.loadDataFromNet(this.link);
            }
        }

        if (rawData != null && rawData.length > 0) {
            if (useImageROMCache) {
                ImageROMCache.cacheData(this.link, rawData);
                result.bitmap = ImageROMCache.getBitmapFromROMCache(this.link, this.scaleToSize);
            } else {
                if (this.scaleToSize != null) {
                    // Potentially need downscaling
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(rawData, 0, rawData.length, opts);
                    result.bitmap = BitmapFactory.decodeByteArray(rawData, 0, rawData.length, Utils.getScaledOptions(opts, scaleToSize.width, scaleToSize.height));
                } else { // No need downscaling
                    result.bitmap = BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
                }
            }
            result.fromNetwork = true;
        }

        return result;
    }


    private View getParentViewforThisTask() {
        View parentViewForThisTask = null;
        if (imageViewReference != null) {
            parentViewForThisTask = imageViewReference.get();
        } else if (imagePresenterReference != null) {
            parentViewForThisTask = (View) imagePresenterReference.get();
        }

        if (parentViewForThisTask != null) {
            BitmapWorkerTask validWorkerTask = getWorkerTaskForView(parentViewForThisTask);  // The task which is currently bound to a View
                                                                                            // Only one task can be bound
            if ((validWorkerTask != null) && (this == validWorkerTask)) {
                return parentViewForThisTask;
            }
        }
        return null;
    }

    private Context tryGetContext() {
        Object o = null;
        if (imageViewReference != null) {
            o = imageViewReference.get();
        } else if (imagePresenterReference != null) {
            o = imagePresenterReference.get();
        }
        return (o != null && (o instanceof View)) ? ((View) o).getContext() : null;
    }

    public static BitmapWorkerTask getWorkerTaskForView(View v) {
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

    public static Drawable buildDrawableWithAttachedWorkerTask(Resources res, Bitmap placeholder, BitmapWorkerTask task) {
        return new AsyncDrawable(res, placeholder, task);
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

    public class Result {
        public Bitmap bitmap;
        public boolean fromNetwork;
        public WeakReference<Bitmap> placeholder;
        public WeakReference<Drawable> overlay;
    }
}
