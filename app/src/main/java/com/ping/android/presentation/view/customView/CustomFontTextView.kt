package com.ping.android.presentation.view.customView;

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.ping.android.R
import com.ping.android.utils.font.FontManagerUtil
import com.vanniktech.emoji.EmojiTextView

/**
 * Created by Huu Hoang on 26/12/2018
 */
open class CustomFontTextView : EmojiTextView {
    lateinit var attrs: AttributeSet

    constructor(context: Context) : super(context) {
        setupView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupView(context, attrs)
        this.attrs = attrs
    }

    /**
     * Process properties (from android layout) and provide default property values
     */
    private fun setupView(context: Context, attrs: AttributeSet?) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView)
        this.attrs = attrs!!

        val fontId = typedArray.getInt(R.styleable.CustomFontTextView_fontName, 3)
        when (fontId) {
            2 -> this.typeface = FontManagerUtil.getFontSemiBold(context)
        }
        typedArray.recycle()
    }

    /**
     * make text-view to semi-bold
     */
    fun applySemiBold() {

        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView)

        this.typeface = FontManagerUtil.getFontSemiBold(context)

        typedArray.recycle()
    }

}