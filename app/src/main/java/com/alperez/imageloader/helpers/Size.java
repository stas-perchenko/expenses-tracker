package com.alperez.imageloader.helpers;

/**
 * Created by stanislav.perchenko on 29-Sep-15.
 */
public class Size {
    public int width;
    public int height;

    public String getSuffixForLink() {
        return String.format("?sz=%dx%d", width, height);
    }

    public Size(int w, int h) {
        width = w;
        height = h;
    }

    public Size(int x) {
        width = x;
        height = x;
    }

    public Size(Size size) {
        if (size != null) {
            this.width = size.width;
            this.height = size.height;
        }
    }

    public void setDimentions(int w, int h) {
        width = w;
        height = h;
    }

    public void setSize(int size) {
        width = size;
        height = size;
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o instanceof Size)) {
            return ((this.width == ((Size) o).width) && (this.height == ((Size) o).height));
        }
        return false;
    }
}
