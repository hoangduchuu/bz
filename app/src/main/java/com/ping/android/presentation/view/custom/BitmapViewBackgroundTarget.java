package com.ping.android.presentation.view.custom;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

public class BitmapViewBackgroundTarget extends ViewBackgroundTarget<Bitmap> {
    public BitmapViewBackgroundTarget(View view) {
        super(view);
    }

    @Override
    protected void setResource(Bitmap resource) {
        setBackground(new BitmapDrawable(view.getResources(), resource));
    }
}
