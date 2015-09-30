package com.alperez.imageloader.helpers;

/**
 * Created by stanislav.perchenko on 29-Sep-15.
 */
public interface ImageExternalProvider {
    byte[] getImageDataSynchronously(String link);
}
