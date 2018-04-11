package com.ping.android.model.enums;

import android.support.annotation.ColorRes;

import com.ping.android.activity.R;

public enum Color {
    COLOR_1,
    COLOR_2,
    COLOR_3,
    COLOR_4,
    COLOR_5,
    COLOR_6,
    COLOR_7,
    COLOR_8,
    COLOR_9,
    DEFAULT;

    @ColorRes
    public int getColor() {
        switch (this) {
            case COLOR_1:
                return R.color.color_1;
            case COLOR_2:
                return R.color.color_2;
            case COLOR_3:
                return R.color.color_3;
            case COLOR_4:
                return R.color.color_4;
            case COLOR_5:
                return R.color.color_5;
            case COLOR_6:
                return R.color.color_6;
            case COLOR_7:
                return R.color.color_7;
            case COLOR_8:
                return R.color.color_8;
            case COLOR_9:
                return R.color.color_9;
            default:
                return R.color.orange;
        }
    }
}
