package com.ping.android.device;

import android.widget.ImageView;

public interface ImageStorage {
    void loadImage(String path, ImageView view, boolean isMask);
    void loadThumb(String path, ImageView view);
}
