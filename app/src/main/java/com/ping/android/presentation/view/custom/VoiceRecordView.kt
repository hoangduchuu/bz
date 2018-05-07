package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import com.bzzzchat.extensions.inflate
import com.cleveroad.audiovisualization.AudioVisualization
import com.ping.android.R
import com.ping.android.presentation.module.recorder.AudioRecorder
import com.ping.android.presentation.module.recorder.AudioRecordingDbmHandler
import com.ping.android.utils.Log
import kotlinx.android.synthetic.main.view_voice_record.view.*
import java.util.*

enum class RecordViewState {
    DEFAULT, CANCEL
}

class VoiceRecordView : LinearLayout {
    private lateinit var audioVisualization: AudioVisualization
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var handler: AudioRecordingDbmHandler
    private lateinit var timer: Timer
    private var lengthInMillis: Long = 0
    private var state: RecordViewState = RecordViewState.DEFAULT

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

    private fun updateTimer() {
        val minuteLeft = (lengthInMillis / 1000) / 60
        val secondsLeft = lengthInMillis / 1000
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

    private fun slideRecordButton(_diff: Float) {
        var diff = _diff
        if (diff < -50) {
            // Show cancel button
            btnCancel.animate().alpha(1.0f).setDuration(0).start()
        } else {
            btnCancel.animate().alpha(0.0f).setDuration(0).start()
        }
        val distanceToCancel = btnCancel.y - (btnRecord.y - btnRecord.translationY)
        Log.d("slideRecordButton $diff, $distanceToCancel, ${btnRecord.translationY}")
        if (diff < distanceToCancel) {
            diff = distanceToCancel
            state = RecordViewState.CANCEL
        } else {
            state = RecordViewState.DEFAULT
        }
        btnRecord.animate()
                .translationY(diff)
                .setDuration(0)
                .start()
    }

    private fun startRecord() {
        audioRecorder.startRecord()
        // Start timer
        lengthInMillis = 0
        updateTimer()
        tvTimer.visibility = View.VISIBLE
        timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                // update timer
                lengthInMillis += 1000
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

    private fun onReleaseView() {
        if (state == RecordViewState.CANCEL) {
            // reset timer
            tvTimer.visibility = View.GONE
        }
        slideRecordButton(0.0f)
        // TODO stop & send record
        disableRecordMode()
        stopRecord()
    }

    inner class TouchListener : OnTouchListener {
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
                onReleaseView()
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
