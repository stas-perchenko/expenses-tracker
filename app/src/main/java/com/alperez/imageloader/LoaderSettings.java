package com.alperez.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.File;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public final class LoaderSettings {

    private Bitmap defaultPlaceholder;
    private Drawable defaultOverlay;
    private File discCache;
    private int discCacheSize;
    private boolean ramCacheEnabled;
    private int ramCacheSize;

    private LoaderSettings(){
        // Only factory-initialization allowed
    }

    public Bitmap getDefaultPlaceholder() {
        return defaultPlaceholder;
    }

    public Drawable getDefaultOverlay() {
        return defaultOverlay;
    }

    public File getDiscCache() {
        return discCache;
    }

    public int getDiscCacheSize() {
        return discCacheSize;
    }

    public boolean isRamCacheEnabled() {
        return ramCacheEnabled;
    }

    public int getRamCacheSize() {
        return ramCacheSize;
    }

    /**
     * Factory builder class for settings
     */
    public static class Builder {
        private LoaderSettings instance;

        public Builder() {
            instance = new LoaderSettings();
        }

        public Builder setDefaultPlaceholder(Bitmap defaultPlaceholder) {
            instance.defaultPlaceholder = defaultPlaceholder;
            return this;
        }

        public Builder setDefaultOverlay(Drawable defaultOverlay) {
            instance.defaultOverlay = defaultOverlay;
            return this;
        }

        public Builder setDiscCache(File discCache) {
            instance.discCache = discCache;
            return this;
        }

        public Builder setDiscCacheSize(int discCacheSize) {
            instance.discCacheSize = discCacheSize;
            return this;
        }

        public Builder setRamCacheEnabled(boolean ramCacheEnabled) {
            instance.ramCacheEnabled = ramCacheEnabled;
            return this;
        }

        public Builder setRamCacheSize(int ramCacheSize) {
            instance.ramCacheSize = ramCacheSize;
            return this;
        }

        public LoaderSettings build() {
            return instance;
        }
    }

}
