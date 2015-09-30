package com.alperez.imageloader.helpers;

import android.content.Context;
import android.graphics.Bitmap;

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
    //TODO Implement this

    private static ImageROMCache instance;

    /**
     * This method must be called once before any using of the ImageROMCache
     * @param context
     */
    public static void instantiate(Context context) {
        if (instance == null) {
            synchronized (ImageROMCache.class) {
                if (instance == null) {
                    instance = new ImageROMCache(context);
                }
            }
        }
    }

    public static Bitmap decodeBitmapFromROMCache(String link, Size scaleToSize) {
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








    private ImageROMCache(Context context) {
        //TODO Implement this
    }


    private Bitmap decodeBitmapFromCacheInternal(String link, Size scaleToSize) {
        //TODO Implement this;
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
                    int bytesRead = 0;
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
        if (mCacheFolder == null) return -1;

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




    private File mCacheFolder;
    private long maxCacheSize;
    private long maxNonExeedCachSize;

    private long totalCacheSize;
    private Map<Long, CachedItemDescriptor> cachedLinksMap;

    private class CachedItemDescriptor {
        public long timeCreated;
        public int size;
        public String relativeName;
        public CachedItemDescriptor(String relativeName) {
            this.relativeName = relativeName;
        }
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
}
