package com.ping.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.ping.android.model.Message;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import java.security.MessageDigest;

/**
 * Created by bzzz on 12/5/17.
 */

public class BitmapEncode extends BitmapTransformation {

    private Context context;
    private boolean bitmapMark;

    public BitmapEncode(Context context, boolean bitmapMark){
        super();
        this.context = context;
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
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
