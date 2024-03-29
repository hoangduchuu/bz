package com.ping.android.model.enums;

import androidx.annotation.ColorRes;
import androidx.annotation.StyleRes;

import com.ping.android.R;

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
                return R.color.colorPrimary;
        }
    }

    @ColorRes
    public int statusBarColor() {
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
                return R.color.colorPrimary;
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
                return R.style.AppTheme_NoActionBar;
        }
    }

    public static Color from(int mainColor) {
        switch (mainColor) {
            case 0:
                return COLOR_1;
            case 1:
                return COLOR_2;
            case 2:
                return COLOR_3;
            case 3:
                return COLOR_4;
            case 4:
                return COLOR_5;
            case 5:
                return COLOR_6;
            case 6:
                return COLOR_7;
            case 7:
                return COLOR_8;
            case 8:
                return COLOR_9;
            default:
                return DEFAULT;
        }
    }

    public int getCode() {
        switch (this) {
            case COLOR_1:
                return 0;
            case COLOR_2:
                return 1;
            case COLOR_3:
                return 2;
            case COLOR_4:
                return 3;
            case COLOR_5:
                return 4;
            case COLOR_6:
                return 5;
            case COLOR_7:
                return 6;
            case COLOR_8:
                return 7;
            case COLOR_9:
                return 8;
            default:
                return 9;
        }
    }
}
