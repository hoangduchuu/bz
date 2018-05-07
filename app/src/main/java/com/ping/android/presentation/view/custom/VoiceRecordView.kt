package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.bzzzchat.extensions.inflate
import com.ping.android.R

import kotlinx.android.synthetic.main.view_voice_record.view.*

class VoiceRecordView : LinearLayout {

    constructor(context: Context) : super(context) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initView()
    }

    private fun initView() {
        inflate(R.layout.view_voice_record, true)
        btnRecord.setOnTouchListener(TouchListener())
    }

    private fun hideTutorialMessage() {
        tutorial_message.visibility = View.GONE
    }

    private fun enableRecordMode() {
        btnRecord.setBackgroundResource(R.drawable.background_circle_gray_dark)
    }

    private fun disableRecordMode() {
        btnRecord.setBackgroundResource(R.drawable.background_circle_gray)
    }

    private fun startRecord() {

    }

    private fun stopRecord() {

    }

    inner class TouchListener: View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            val action = event?.action
            if (action == MotionEvent.ACTION_DOWN) {
                // Should hide instruction
                hideTutorialMessage()
                enableRecordMode();
                // TODO trigger record
            }
            if (action == MotionEvent.ACTION_UP) {
                // TODO stop & send record
                disableRecordMode()
            }
            return true
        }
    }
}
