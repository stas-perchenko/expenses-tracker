package com.alperez.imageloader.task;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
    private WeakReference<ImageView> imageViewReference;
    private WeakReference<ImagePresentationInterface> imagePresenterReference;
    private final WeakReference<Drawable> overlayReference;
    private final WeakReference<Bitmap> loadingBitmapReference;
    private final WeakReference<ImageRAMCache> ramCache;

    /**
     * Single variable which is used for all instances of this task for managing sequential execution.
     */
    private static long timeLastStart = 0;
    private static final Object timeLocker = new Object(); // Locker object for above time holder

    public BitmapWorkerTask(@NonNull String link, @NonNull View v, @Nullable Size scaleToSize, @Nullable ImageRAMCache ramCache, @Nullable Bitmap placeholder, @Nullable Drawable overlay) {
        this.link = link;
        this.scaleToSize = scaleToSize;
        if (v instanceof ImageView) {
            imageViewReference = new WeakReference<ImageView>((ImageView) v);
        } else if (v instanceof ImagePresentationInterface) {
            imagePresenterReference = new WeakReference<ImagePresentationInterface>((ImagePresentationInterface) v);
        }
        overlayReference = new WeakReference<Drawable>(overlay);
        loadingBitmapReference = new WeakReference<Bitmap>(placeholder);
        this.ramCache = new WeakReference<ImageRAMCache>(ramCache);
    }

    public String getLink() {
        return this.link;
    }

    @Override
    protected Result doInBackground(Void... params) {
        if ((imageViewReference == null) && imagePresenterReference == null) return null;

        // Check for task been canceled
        if (getAttachedImageView() == null || isCancelled()) {
            return null;
        }


        Result result = new Result();
        Bitmap bitmap = ImageROMCache.decodeBitmapFromROMCache(link, scaleToSize);

        if (bitmap != null) {
            ImageRAMCache rCace = this.ramCache.get();
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
                if (getAttachedImageView() == null || isCancelled()) {
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
        if (getAttachedImageView() == null || isCancelled()) {
            Log.d(TAG, "doInBackground - break task 2");
            return null;
        }

        // Load content from the network
        //TODO call to ROM cache for caching after this there.
        //TODO use external processor if available
        //byte[] rawData = processBitmap(data);


        return null;
    }


    private View getAttachedImageView() {
        //TODO implement this
        return null;
    }

    public class Result {
        public Bitmap bitmap;
        public boolean fromNetwork;

        //TODO Implement result class;
    }
}
