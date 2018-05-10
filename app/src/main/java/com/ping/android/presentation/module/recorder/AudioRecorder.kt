package com.ping.android.presentation.module.recorder

import android.media.MediaRecorder
import java.io.FileNotFoundException
import java.util.*


internal interface IAudioRecorder {
    val isRecording: Boolean
    fun startRecord()
    fun finishRecord()
}

/**
 * Helper class for audio recording and saving as .wav
 */
internal class AudioRecorder : IAudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var timer: Timer? = null
    private var callback: RecordingCallback? = null

    @Volatile
    private var recorderState: Int = 0

    private lateinit var outputFile: String

    override val isRecording: Boolean
        get() = recorderState != RECORDER_STATE_IDLE

    fun recordingCallback(callback: RecordingCallback): AudioRecorder {
        this.callback = callback
        return this
    }

    fun setOutputFile(outputFile: String): AudioRecorder {
        this.outputFile = outputFile
        return this
    }

    private fun onRecordFailure() {
        recorderState = RECORDER_STATE_FAILURE
        finishRecord()
    }

    override fun startRecord() {
        if (recorderState != RECORDER_STATE_IDLE) {
            return
        }

        recorderState = RECORDER_STATE_STARTING

        startRecordThread()

    }

    private fun startRecordThread() {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setAudioSamplingRate(44100)
        mediaRecorder?.setAudioEncodingBitRate(96000)
        mediaRecorder?.setOutputFile(outputFile)
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            if (recorderState == RECORDER_STATE_STARTING) {
                recorderState = RECORDER_STATE_BUSY
            }
            timer = Timer()
            val timerTask = object: TimerTask() {
                override fun run() {
                    val amplitude: Int = mediaRecorder!!.maxAmplitude
                    callback?.onDataReady(amplitude.toFloat() / 200)
                }
            }
            timer?.scheduleAtFixedRate(timerTask, 0, 40)
        } catch (exception: Exception) {
            recorderState = RECORDER_STATE_FAILURE
        }
    }

    override fun finishRecord() {
        recorderState = RECORDER_STATE_IDLE
        try {
            timer?.cancel()
            if (null != mediaRecorder) {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
            }
            //btSendRecord.setEnabled(true);
        } catch (e: Exception) {
            com.ping.android.utils.Log.e(e)
        }

    }

    internal interface RecordingCallback {
        fun onDataReady(data: Float)
    }

    companion object {
        const val RECORDER_STATE_FAILURE = -1
        const val RECORDER_STATE_IDLE = 0
        const val RECORDER_STATE_STARTING = 1
        const val RECORDER_STATE_STOPPING = 2
        const val RECORDER_STATE_BUSY = 3
    }
}