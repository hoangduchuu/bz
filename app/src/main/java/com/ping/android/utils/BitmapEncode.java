package com.ping.android.utils;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * Created by bzzz on 12/5/17.
 */

public class BitmapEncode extends BitmapTransformation {
    private static final String ID = "com.ping.android.utils.BitmapEncode";

    private boolean bitmapMark;

    public BitmapEncode(boolean bitmapMark){
        super();
        this.bitmapMark = bitmapMark;

    }
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        if(bitmapMark){
            return CommonMethod.puzzleImage(toTransform, 3);
        }
        return toTransform;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BitmapEncode;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        try {
            messageDigest.update(ID.getBytes(STRING_CHARSET_NAME));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
