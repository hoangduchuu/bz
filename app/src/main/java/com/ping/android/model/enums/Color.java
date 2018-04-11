package com.ping.android.model.enums;

import android.support.annotation.ColorRes;
import android.support.annotation.StyleRes;

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

    @ColorRes
    public int statusBarColor() {
        switch (this) {
            case COLOR_1:
                return R.color.color_accent_1;
            case COLOR_2:
                return R.color.color_accent_2;
            case COLOR_3:
                return R.color.color_accent_3;
            case COLOR_4:
                return R.color.color_accent_4;
            case COLOR_5:
                return R.color.color_accent_5;
            case COLOR_6:
                return R.color.color_accent_6;
            case COLOR_7:
                return R.color.color_accent_7;
            case COLOR_8:
                return R.color.color_accent_8;
            case COLOR_9:
                return R.color.color_accent_9;
            default:
                return R.color.orange;
        }
    }

    @StyleRes
    public int getTheme() {
        switch (this) {
            case COLOR_1:
                return R.style.AppTheme_Theme1;
            case COLOR_2:
                return R.style.AppTheme_Theme2;
            case COLOR_3:
                return R.style.AppTheme_Theme3;
            case COLOR_4:
                return R.style.AppTheme_Theme4;
            case COLOR_5:
                return R.style.AppTheme_Theme5;
            case COLOR_6:
                return R.style.AppTheme_Theme6;
            case COLOR_7:
                return R.style.AppTheme_Theme7;
            case COLOR_8:
                return R.style.AppTheme_Theme8;
            case COLOR_9:
                return R.style.AppTheme_Theme9;
            default:
                return R.style.AppTheme_Theme1;
        }
    }
}
