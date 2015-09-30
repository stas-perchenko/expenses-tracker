package com.alperez.imageloader.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alperez.imageloader.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by stanislav.perchenko on 29-Sep-15.
 */
public class ImageROMCache {
    public static final String DEFAULT_CACHE_DIR_NAME = "image_cache";

    private static ImageROMCache instance;

    /**
     * This method must be called once before any using of the ImageROMCache
     * @param context
     */
    public static void instantiate(Context context, @Nullable File cacheDirectory, int maxCacheSizeMB, boolean debugMode) {
        if (instance == null) {
            synchronized (ImageROMCache.class) {
                if (instance == null) {
                    instance = new ImageROMCache(context, cacheDirectory, maxCacheSizeMB, debugMode);
                }
            }
        }
    }

    public static Bitmap getBitmapFromROMCache(String link, Size scaleToSize) {
        if (instance == null) throw new IllegalStateException("ImageROMCache not yet been initialized");
        Bitmap result = null;
        synchronized (instance) {
            result = instance.decodeBitmapFromCacheInternal(getCacheNameByLink(link), scaleToSize);
        }
        return result;
    }

    public static byte[] getDataFromCache(String link) {
        if (instance == null) throw new IllegalStateException("ImageROMCache not yet been initialized");
        byte[] result = null;
        synchronized (instance) {
            result = instance.getDataFromCacheInternal(getCacheNameByLink(link));
        }
        return result;
    }

    public static long cacheData(String link, byte[] data) {
        if (instance == null) throw new IllegalStateException("ImageROMCache not yet been initialized");
        long ret = -1;
        synchronized (instance) {
            ret = instance.cacheDataInternal(getCacheNameByLink(link), data);
        }
        return ret;
    }

    public static String getCacheNameByLink(String link) {
        return String.format(CACHE_NAME_TEMPLATE, UUID.nameUUIDFromBytes(link.getBytes()));
    }
    private static final String CACHE_NAME_TEMPLATE = "cache-%s";




    /**********************************************************************************************/
    private class CachedItemDescriptor {
        public long timeCreated;
        public int size;
        public String relativeName;
        public CachedItemDescriptor(String relativeName) {
            this.relativeName = relativeName;
        }
    }




    /**********************************************************************************************/
    private File mCacheFolder;
    private long maxCacheSize;
    private boolean debugMode;

    private long totalCacheSize;
    private Map<Long, CachedItemDescriptor> cachedLinksMap;


    private ImageROMCache(Context context, File cacheDir, int maxCacheSizeMB, boolean debugMode) {
        if (cacheDir != null && cacheDir.exists() && cacheDir.isDirectory()) {
            mCacheFolder = cacheDir;
        } else {
            try {
                mCacheFolder = ImageROMCache.getFinalCacheFolder(context, DEFAULT_CACHE_DIR_NAME);
            } catch(ExternalMediaException e){
                //TODO Log here
            }
        }
        maxCacheSize = 1024*1024*maxCacheSizeMB;
        this.debugMode = debugMode;
    }


    private Bitmap decodeBitmapFromCacheInternal(String fname, Size scaleToSize) {
        byte[] data = getDataFromCacheInternal(fname);
        if (data != null) {
            if (scaleToSize != null) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                return BitmapFactory.decodeByteArray(data, 0, data.length, Utils.getScaledOptions(opts, scaleToSize.width, scaleToSize.height));
            } else {
                return BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        }
        return null;
    }

    /**
     *
     * @param fname
     * @return byte array of data or null if there is no requested item in cache
     */
    private byte[] getDataFromCacheInternal(String fname) {
        if (mCacheFolder != null) {
            File f = new File(mCacheFolder, fname);
            if (f.exists()) {
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(f));
                    os = new BufferedOutputStream(new ByteArrayOutputStream((int)f.length()+512));

                    byte[] bb = new byte[512];
                    int bytesRead;
                    while ((bytesRead = is.read(bb)) > 0) {
                        os.write(bb, 0, bytesRead);
                    }
                    return ((ByteArrayOutputStream) os).toByteArray();
                } catch (IOException e) {
                    return null;
                } finally {
                    if (is != null) try { is.close(); } catch(IOException e){}
                    if (os != null) try { os.close(); } catch(IOException e){}
                }
            }
        }
        return null;
    }

    /**
     *
     * @param fname
     * @param data
     * @return number of free bytes left in cache. -1 means cache is in error state
     */
    private long cacheDataInternal(String fname, byte[] data) {
        if (mCacheFolder == null) {
            return -1;
        } else if (maxCacheSize == 0) {
            return 0;
        }

        File f = new File(mCacheFolder, fname);
        if (f.exists()) {
            if (!f.delete()) {
                return totalCacheSize;
            }
        }

        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(f));
            os.write(data);
            os.flush();
        } catch(IOException e) {
            return maxCacheSize - totalCacheSize;
        } finally {
            if (os != null) try { os.close(); } catch(IOException e){}
        }
        return maxCacheSize - trimCache(maxCacheSize);
    }


    /**
     * Trims cache to requested size by deleting the oldest files.
     * @param targetSize tre quested size the cache must not exceed after trim
     * @return actual size the cache occupies after trimming
     */
    private long trimCache(long targetSize) {
        updateCachedLinks();
        long currentSize = totalCacheSize;
        while(currentSize > targetSize) { // Delete the oldest files until requested size will be reached
            // Find link to the oldest file
            CachedItemDescriptor toDelete = null;
            for (Long key : cachedLinksMap.keySet()) {
                final CachedItemDescriptor opt = cachedLinksMap.get(key);
                if ((toDelete == null) || (toDelete.timeCreated > opt.timeCreated)) {
                    toDelete = opt;
                }
            }

            if (toDelete != null) {
                // Remove file from disk
                deleteFolderRecursively(new File(mCacheFolder, toDelete.relativeName));
                // Remove link from the Map
                cachedLinksMap.remove(toDelete);
                // update current size
                currentSize -= toDelete.size;
            }
        }
        totalCacheSize = currentSize;
        return currentSize;
    }


    /**
     *
     * @return size of free space in cache
     */
    private long updateCachedLinks() {

        if (cachedLinksMap == null) {
            cachedLinksMap = new HashMap<>();
        } else {
            cachedLinksMap.clear();
        }
        totalCacheSize = 0;

        File[] fItems = mCacheFolder.listFiles();
        if (fItems != null) {
            for (File item : fItems) {
                if (item.isDirectory()) {
                    deleteFolderRecursively(item);
                } else {
                    CachedItemDescriptor descr = new CachedItemDescriptor(item.getName());
                    descr.size = (int) item.length();
                    descr.timeCreated = item.lastModified();
                    cachedLinksMap.put(descr.timeCreated, descr);
                    totalCacheSize += descr.size;
                }
            }
        }
        return (totalCacheSize < maxCacheSize) ? (maxCacheSize - totalCacheSize) : 0;
    }


    private static void deleteFolderRecursively(File folder) {
        if (folder.isFile()) {
            folder.delete();
        } else if (folder.isDirectory()) {
            File[] children = folder.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFolderRecursively(child);
                }
            }
        }
    }

    private static File getFinalCacheFolder(Context context, String dirName) throws ExternalMediaException {
        File dir = ImageROMCache.getDiskCacheDir(context, dirName);
        return ImageROMCache.createDirIfNeeded(dir);
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
        boolean useExternalMemory = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !ImageROMCache.isExternalStorageRemovable();
        File cacheFolder;
        if (useExternalMemory) {
            cacheFolder = ImageROMCache.getExternalCacheDir(context);
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
