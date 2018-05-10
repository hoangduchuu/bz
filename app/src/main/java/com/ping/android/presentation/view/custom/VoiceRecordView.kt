package com.ping.android.presentation.view.custom

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewConfiguration
import android.widget.LinearLayout
import com.bzzzchat.extensions.inflate
import com.cleveroad.audiovisualization.AudioVisualization
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.ping.android.R
import com.ping.android.managers.FFmpegManager
import com.ping.android.model.enums.VoiceType
import com.ping.android.presentation.module.recorder.AudioRecorder
import com.ping.android.presentation.module.recorder.AudioRecordingHandler
import com.ping.android.utils.Log
import kotlinx.android.synthetic.main.view_voice_record.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class RecordViewState {
    DEFAULT, CANCEL, MASK
}

interface VoiceRecordViewListener {
    fun showInstruction(instruction: String)
    fun hideInstruction()
}

class VoiceRecordView : LinearLayout {
    private var listener: VoiceRecordViewListener? = null
    private lateinit var audioVisualization: AudioVisualization
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var handler: AudioRecordingHandler
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator

    private var mediaPlayer: MediaPlayer? = null

    private var lengthInMillis: Long = 0
    private var state: RecordViewState = RecordViewState.DEFAULT
    private var outputFile: String = ""

    constructor(context: Context) : super(context) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initView()
    }

    fun setListener(listener: VoiceRecordViewListener) {
        this.listener = listener
    }

    fun release() {
        handler.release()
        audioVisualization.release()
    }

    private fun initView() {
        inflate(R.layout.view_voice_record, true)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        handler = AudioRecordingHandler()
        audioRecorder = AudioRecorder()
                .recordingCallback(handler)
        audioVisualization = visualizer_view
        audioVisualization.linkTo(handler)

        recordView.addView(View(context))
        btnRecord.setOnTouchListener(TouchListener())

        maskChipMunk.setOnClickListener { this.handleMaskSelected(VoiceType.CHIPMUNK) }
        maskGirl.setOnClickListener { this.handleMaskSelected(VoiceType.GIRL) }
    }

    private fun handleMaskSelected(voiceType: VoiceType) {
        val input = File(outputFile)
        val output = File("${input.parent}/${voiceType}_${input.name}")
//        if (output.exists()) {
//            playAudio(output.absolutePath)
//        } else {
            val command = "-i ${input.absolutePath} -af atempo=${voiceType.atempo},asetrate=${voiceType.asetrate} -acodec aac ${output.absolutePath} -strict -2"
            val handler = object : FFmpegExecuteResponseHandler {
                override fun onFinish() {
                    // TODO hide loading
                    // Start playing output file
                    playAudio(output.absolutePath)
                }

                override fun onSuccess(message: String?) {
                    Log.d(message)
                }

                override fun onFailure(message: String?) {
                    Log.d(message)
                }

                override fun onProgress(message: String?) {
                    Log.d(message)
                }

                override fun onStart() {
                    // TODO show loading
                }

            }
            FFmpegManager.getInstance(context)
                    .execute(command.split(" ").toTypedArray(), handler)
//        }
    }

    private fun playAudio(filePath: String) {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(filePath)
        mediaPlayer!!.prepare()
        mediaPlayer!!.start()
    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
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
        btnCancel.animate().alpha(if (diff < -50) 1.0f else 0.0f).setDuration(0).start()
        btnTransform.animate().alpha(if (diff > 50) 1.0f else 0.0f).setDuration(0).start()
        val distanceToCancel = btnCancel.y - (btnRecord.y - btnRecord.translationY)
        val distanceToMask = btnTransform.y - (btnRecord.y - btnRecord.translationY)
        when {
            diff < distanceToCancel -> {
                diff = distanceToCancel
                updateState(RecordViewState.CANCEL)
            }
            diff > distanceToMask -> {
                diff = distanceToMask
                updateState(RecordViewState.MASK)
            }
            else -> updateState(RecordViewState.DEFAULT)
        }
        btnRecord.animate()
                .translationY(diff)
                .setDuration(0)
                .start()
    }

    private fun resetRecordTranslate(delay: Long = 0) {
        btnRecord.animate()
                .translationY(0.0f)
                .setDuration(0)
                .setStartDelay(delay)
                .start()
    }

    private fun updateState(state: RecordViewState) {
        when (state) {
            RecordViewState.CANCEL -> {
                if (this.state != RecordViewState.CANCEL) {
                    startVibrate()
                    listener?.showInstruction(context.getString(R.string.voice_record_instruction_release_to_cancel))
                }
            }
            RecordViewState.MASK -> {
                if (this.state != RecordViewState.MASK) {
                    startVibrate()
                    listener?.showInstruction(context.getString(R.string.voice_record_instruction_release_to_mask))
                }
            }
            else -> listener?.showInstruction(context.getString(R.string.voice_record_instruction_slide_up_down))
        }
        this.state = state
    }

    private fun startRecord() {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HH_mm_ss")
        val currentTimeStamp = dateFormat.format(Date())
        outputFile = context.getExternalFilesDir(null).absolutePath + "/recording_" + currentTimeStamp + ".3gp"
        audioRecorder.setOutputFile(outputFile)

        audioRecorder.startRecord()
        listener?.showInstruction(context.getString(R.string.voice_record_instruction_slide_up_down))

        btnRecord.elevation = 10.0f
        startVibrate()
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
        when (state) {
            RecordViewState.CANCEL -> {
                // reset timer
                tvTimer.visibility = View.GONE
                listener?.hideInstruction()
                val file = File(outputFile)
                if (file.exists()) {
                    file.delete()
                }
                resetRecordTranslate()
            }
            RecordViewState.MASK -> {
                listener?.showInstruction(context.getString(R.string.voice_record_instruction_mask_with))
                showReviewVoice()
                resetRecordTranslate(500)
            }
            else -> resetRecordTranslate()
        }
        btnCancel.animate().alpha(0.0f).start()
        btnTransform.animate().alpha(0.0f).start()
        // TODO stop & send record
        disableRecordMode()
        stopRecord()
    }

    private fun showReviewVoice() {
        val cx = btnTransform.x + btnTransform.width / 2
        val cy = btnTransform.y + btnTransform.height / 2
        val radius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val animator = ViewAnimationUtils.createCircularReveal(reviewView, cx.toInt(), cy.toInt(), 0.0f, radius)
        reviewView.visibility = View.VISIBLE
        animator.start()
    }

    private fun startVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(100)
        }
    }

    private fun stopVibrate() {
        if (vibrator.hasVibrator()) {
            vibrator.cancel()
        }
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
