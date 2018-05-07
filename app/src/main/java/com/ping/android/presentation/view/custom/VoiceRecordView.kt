package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import com.bzzzchat.extensions.inflate
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.ping.android.R
import com.ping.android.presentation.module.recorder.AudioRecorder
import com.ping.android.presentation.module.recorder.AudioRecordingDbmHandler
import kotlinx.android.synthetic.main.activity_mapping.view.*

import kotlinx.android.synthetic.main.view_voice_record.view.*
import java.util.*

class VoiceRecordView : LinearLayout {
    private lateinit var audioVisualization: AudioVisualization
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var handler: AudioRecordingDbmHandler
    private lateinit var timer: Timer
    private var lengthInMilis: Long = 0

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
        audioRecorder = AudioRecorder()
        handler = AudioRecordingDbmHandler()
        audioRecorder.recordingCallback(handler)
        audioVisualization = visualizer_view
        audioVisualization.linkTo(handler)

        recordView.addView(View(context))
        btnRecord.setOnTouchListener(TouchListener())
    }

    private fun updateTimer(){
        val minuteLeft = (lengthInMilis / 1000) / 60
        val secondsLeft = lengthInMilis / 1000
        val secondsStr = secondsLeft.toString()
        val finalTime = "$minuteLeft:${if (secondsStr.length == 2) secondsStr else "0$secondsStr"}"
        tvTimer.text = finalTime
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

    private fun slideRecordButton(diff: Float) {
        btnRecord.animate()
                .translationY(diff)
                .setDuration(0)
                .start()
    }

    private fun startRecord() {
        audioRecorder.startRecord()
        // Start timer
        lengthInMilis = 0
        updateTimer()
        timer = Timer()
        val task = object: TimerTask() {
            override fun run() {
                // update timer
                lengthInMilis += 1000
                tvTimer.post { updateTimer() }
            }
        }
        timer.scheduleAtFixedRate(task, 1000, 1000)
    }

    private fun stopRecord() {
        audioRecorder.finishRecord()
        handler.stop()
        // Stop timer
        timer.cancel()
    }

    inner class TouchListener: OnTouchListener {
        var lastY: Float = 0.0f
        private val mTouchslop = ViewConfiguration.get(context).scaledTouchSlop
        private var mBeingDragged = false

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            val action = event?.action
            if (action == MotionEvent.ACTION_DOWN) {
                lastY = event.rawY
                mBeingDragged = true
                // Should hide instruction
                hideTutorialMessage()
                enableRecordMode()
                startRecord()
            }
            if (action == MotionEvent.ACTION_UP) {
                mBeingDragged = false
                slideRecordButton(0.0f)
                // TODO stop & send record
                disableRecordMode()
                stopRecord()
            }
            if (action == MotionEvent.ACTION_MOVE) {
                val diff = event.rawY - lastY
                if (mBeingDragged) {
                    slideRecordButton(diff)
                    return true
                }
                if (Math.abs(diff) > mTouchslop) {
                    mBeingDragged = true
                }
            }
            return true
        }
    }
}
