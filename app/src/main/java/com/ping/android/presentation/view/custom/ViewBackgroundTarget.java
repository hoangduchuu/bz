package com.ping.android.presentation.view.custom;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;

public abstract class ViewBackgroundTarget<Z> extends ViewTarget<View, Z> implements Transition.ViewAdapter {
    public ViewBackgroundTarget(View view) {
        super(view);
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {
        super.onLoadStarted(placeholder);
        setBackground(placeholder);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        setBackground(errorDrawable);
    }

    @Override
    public void onResourceReady(Z resource, Transition<? super Z> transition) {
        if (transition == null || !transition.transition(resource, this)) {
            setResource(resource);
        }
    }

    @Override
    public void setDrawable(Drawable drawable) {
        setBackground(drawable);
    }

    @Override
    public Drawable getCurrentDrawable() {
        return view.getBackground();
    }

    @SuppressWarnings("deprecation")
    protected void setBackground(Drawable drawable) {
        view.setBackground(drawable);
    }

    protected abstract void setResource(Z resource);
}
