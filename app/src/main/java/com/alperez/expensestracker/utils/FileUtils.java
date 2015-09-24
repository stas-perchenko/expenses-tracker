package com.alperez.expensestracker.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public class FileUtils {

    public static final String IMAGE_CACHE_DIR = "images";


    public static File getFinalImageCacheDir(Context context) throws ExternalMediaException {
        File dir = FileUtils.getDiskCacheDir(context, IMAGE_CACHE_DIR);
        return FileUtils.createDirIfNeeded(dir);
    }


    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir
        // otherwise use internal cache dir
        boolean useExternalMemory = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !FileUtils.isExternalStorageRemovable();
        File cacheFolder;
        if (useExternalMemory) {
            cacheFolder = FileUtils.getExternalCacheDir(context);
        } else {
            try {
                cacheFolder = context.getCacheDir();
            } catch (Exception e) {
                cacheFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Lastly, try this as a last ditch effort
            }
        }

        return TextUtils.isEmpty(uniqueName) ? cacheFolder : new File(cacheFolder.getPath() + File.separator + uniqueName);
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context
     *            The context to use
     * @return The external cache dir
     */
    @TargetApi(8)
    public static File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }



    /**
     * Creates a directory and puts a .nomedia file in it
     *
     *
     * @param dir
     * @return new dir
     * @throws ExternalMediaException
     */
    private static File createDirIfNeeded(File dir) throws ExternalMediaException {
        if ((dir != null) && !dir.exists()) {
            if (!dir.mkdirs() && !dir.isDirectory()) {
                Log.d("CineLocations", "failed to create directory");
                throw new ExternalMediaException("error create directory");
            }
            File noMediaFile = new File(dir, ".nomedia");
            try {
                noMediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                throw new ExternalMediaException("error create .nomedia file");
            }
        }
        return dir;
    }
}
