package com.ping.android.view.viewholders;

import android.view.View;

public class BaseTextMessageViewHolder extends BaseMessageViewHolder {
    public BaseTextMessageViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected View getClickableView() {
        return null;
    }


    @Override
    public void onSingleTap() {

    }

    @Override
    public void onDoubleTap() {

    }

    @Override
    public void onLongPress() {

    }
}
