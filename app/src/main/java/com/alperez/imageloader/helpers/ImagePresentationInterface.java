package com.alperez.imageloader.helpers;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public interface ImagePresentationInterface {
    void setImageBitmap(Bitmap bmp);
    void setImageDrawable(Drawable drawable);
    Drawable getDrawable();
}
