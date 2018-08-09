package com.bzzzchat.configuration;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;

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
            return puzzleImage(toTransform, 3);
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

    public static Bitmap puzzleImage(Bitmap bitmap, int items) {
        if (bitmap == null) {
            return null;
        }
        ArrayList<Bitmap> chunkedImages;
        int chunkNumbers = items * items;
        //For the number of rows and columns of the grid to be displayed
        int rows, cols;

        //For height and width of the small image chunks
        int chunkHeight, chunkWidth;

        //To store all the small image chunks in bitmap format in this list
        chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight() / rows;
        chunkWidth = bitmap.getWidth() / cols;

        int yCoord = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedImages.add(Bitmap.createBitmap(bitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }

        Bitmap puzzledBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Collections.shuffle(chunkedImages);
        Canvas canvas = new Canvas(puzzledBitmap);
        int count = 0;
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                canvas.drawBitmap(chunkedImages.get(count), chunkWidth * y, chunkHeight * x, null);
                count++;
            }
        }
        for (Bitmap bitmap1 : chunkedImages) {
            bitmap1.recycle();
        }
        return puzzledBitmap;
    }

}
