package com.bzzzchat.videorecorder.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bzzzchat.videorecorder.R


/**
 * Created by Huu Hoang
 */
class ConfirmPictureButton @JvmOverloads
        constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        LinearLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context)
                .inflate(R.layout.view_confirm_button, this, true)
    }

}