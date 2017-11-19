package com.ping.android.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

import java.util.ArrayList;


public final class MultiDrawable extends Drawable {
    private final Paint paint;
    private final ArrayList<PhotoItem> items;
    private final ArrayList<Bitmap> bitmaps;

    public MultiDrawable(ArrayList bitmaps) {
        super();
        this.bitmaps = bitmaps;
        this.paint = new Paint(1);
        this.items = new ArrayList();
    }

    /**
     * Create PhotoItem with position and size depends of count of images
     */
    private void init() {
        items.clear();
        if (bitmaps.size() == 1) {
            Bitmap bitmap = scaleCenterCrop(bitmaps.get(0), this.getBounds().width(), this.getBounds().height());
            items.add(new PhotoItem(bitmap, new Rect(0, 0, this.getBounds().width(), this.getBounds().height())));
        } else if (bitmaps.size() == 2) {
            Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), this.getBounds().width(), this.getBounds().height() / 2);
            Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), this.getBounds().width(), this.getBounds().height() / 2);
            items.add(new PhotoItem(bitmap1, new Rect(0, 0, this.getBounds().width() / 2, this.getBounds().height())));
            items.add(new PhotoItem(bitmap2, new Rect(this.getBounds().width() / 2, 0, this.getBounds().width(), this.getBounds().height())));
        } else if (bitmaps.size() == 3) {
            Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), this.getBounds().width(), this.getBounds().height() / 2);
            Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), this.getBounds().width() / 2, this.getBounds().height() / 2);
            Bitmap bitmap3 = scaleCenterCrop(bitmaps.get(2), this.getBounds().width() / 2, this.getBounds().height() / 2);
            items.add(new PhotoItem(bitmap1, new Rect(0, 0, this.getBounds().width() / 2, this.getBounds().height())));
            items.add(new PhotoItem(bitmap2, new Rect(this.getBounds().width() / 2, 0, this.getBounds().width(), this.getBounds().height() / 2)));
            items.add(new PhotoItem(bitmap3, new Rect(this.getBounds().width() / 2, this.getBounds().height() / 2, this.getBounds().width(), this.getBounds().height())));
        }
        if (bitmaps.size() == 4) {
            Bitmap bitmap1 = scaleCenterCrop(bitmaps.get(0), this.getBounds().width() / 2, this.getBounds().height() / 2);
            Bitmap bitmap2 = scaleCenterCrop(bitmaps.get(1), this.getBounds().width() / 2, this.getBounds().height() / 2);
            Bitmap bitmap3 = scaleCenterCrop(bitmaps.get(2), this.getBounds().width() / 2, this.getBounds().height() / 2);
            Bitmap bitmap4 = scaleCenterCrop(bitmaps.get(3), this.getBounds().width() / 2, this.getBounds().height() / 2);
            items.add(new PhotoItem(bitmap1, new Rect(0, 0, this.getBounds().width() / 2, this.getBounds().height() / 2)));
            items.add(new PhotoItem(bitmap2, new Rect(0, this.getBounds().height() / 2, this.getBounds().width() / 2, this.getBounds().height())));
            items.add(new PhotoItem(bitmap3, new Rect(this.getBounds().width() / 2, 0, this.getBounds().width(), this.getBounds().height() / 2)));
            items.add(new PhotoItem(bitmap4, new Rect(this.getBounds().width() / 2, this.getBounds().height() / 2, this.getBounds().width(), this.getBounds().height())));
        }
    }

    private final Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        return ThumbnailUtils.extractThumbnail(source, newWidth, newHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas != null) {
            for (PhotoItem item : items) {
                canvas.drawBitmap(item.bitmap, this.getBounds(), item.position, paint);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.init();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    private class PhotoItem {
        private final Bitmap bitmap;
        private final Rect position;

        public PhotoItem(Bitmap bitmap, Rect position) {
            super();
            this.bitmap = bitmap;
            this.position = position;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public Rect getPosition() {
            return position;
        }
    }
}
