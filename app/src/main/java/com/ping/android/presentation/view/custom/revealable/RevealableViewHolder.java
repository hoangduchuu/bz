package com.ping.android.presentation.view.custom.revealable;

import android.view.View;

public interface RevealableViewHolder {
    View getRevealView();

    View getSlideView();

    RevealStyle getRevealStyle();

    void transform(float xDiff);
}
