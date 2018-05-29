package com.ping.android.presentation.module.recorder

import android.media.MediaRecorder
import com.ping.android.utils.Log
import java.io.File
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
    private val lock = java.lang.Object()

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
        val file = File(outputFile)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
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
        synchronized(lock) {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(outputFile)
            }
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
                Log.e(exception)
                recorderState = RECORDER_STATE_FAILURE
            }
        }
    }

    override fun finishRecord() {
        synchronized(lock) {
            recorderState = RECORDER_STATE_IDLE
            try {
                timer?.cancel()
                mediaRecorder?.apply {
                    stop()
                    release()
                }
//                        //?.stop()
//                mediaRecorder?.release()
                mediaRecorder = null
                //btSendRecord.setEnabled(true);
            } catch (e: Exception) {
                val file = File(outputFile)
                if (file.exists()) {
                    file.delete()
                }
                com.ping.android.utils.Log.e(e)
            }
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