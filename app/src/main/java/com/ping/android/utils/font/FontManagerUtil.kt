package com.ping.android.utils.font;

import android.content.Context
import android.graphics.Typeface

/**
 * Created by Huu Hoang on 26/12/2018
 */
class FontManagerUtil {
    companion object {
        val SEMIBOLD_FONT_PATH = "fonts/Roboto-Semibold.ttf"
        var semiBoldFont: Typeface? = null

        fun getFontSemiBold(context: Context): Typeface? {
            if (semiBoldFont == null) {
                semiBoldFont = Typeface.createFromAsset(context.assets, SEMIBOLD_FONT_PATH)
            }
            return semiBoldFont
        }

    }
}