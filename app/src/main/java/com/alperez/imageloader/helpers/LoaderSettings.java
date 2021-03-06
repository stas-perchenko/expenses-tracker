package com.alperez.imageloader.helpers;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public final class LoaderSettings {

    private ImageExternalProvider imageExtProvider;
    private Bitmap defaultPlaceholder;
    private Drawable defaultOverlay;
    private boolean ramCacheEnabled;
    private int ramCacheSize;
    private boolean useImageROMCache;

    private LoaderSettings(){
        // Only factory-initialization allowed
    }

    public Bitmap getDefaultPlaceholder() {
        return defaultPlaceholder;
    }

    public Drawable getDefaultOverlay() {
        return defaultOverlay;
    }

    public boolean isRamCacheEnabled() {
        return ramCacheEnabled;
    }

    public int getRamCacheSize() {
        return ramCacheSize;
    }

    public boolean isUseImageROMCache() {
        return useImageROMCache;
    }

    public ImageExternalProvider getImageExtProvider() {
        return imageExtProvider;
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

        public Builder setRamCacheEnabled(boolean ramCacheEnabled) {
            instance.ramCacheEnabled = ramCacheEnabled;
            return this;
        }

        public Builder setRamCacheSize(int ramCacheSize) {
            instance.ramCacheSize = ramCacheSize;
            return this;
        }

        public Builder setUseImageROMCache(boolean use) {
            instance.useImageROMCache = use;
            return this;
        }

        public Builder setImageExtProvider(ImageExternalProvider imageExtProvider) {
            instance.imageExtProvider = imageExtProvider;
            return this;
        }

        public LoaderSettings build() {
            return instance;
        }
    }

}
