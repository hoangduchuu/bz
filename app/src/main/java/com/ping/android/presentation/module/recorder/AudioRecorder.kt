package com.ping.android.presentation.module.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import io.fabric.sdk.android.services.concurrency.PriorityRunnable
import java.io.FileNotFoundException


internal interface IAudioRecorder {
    val isRecording: Boolean
    fun startRecord()
    fun finishRecord()
}

/**
 * Helper class for audio recording and saving as .wav
 */
internal class AudioRecorder : IAudioRecorder {

    @Volatile
    private var recorderState: Int = 0

    private val recorderStateMonitor = Object()

    private var recordingCallback: RecordingCallback? = null


    override val isRecording: Boolean
        get() = recorderState != RECORDER_STATE_IDLE

    fun recordingCallback(recordingCallback: RecordingCallback): AudioRecorder {
        this.recordingCallback = recordingCallback
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

        try {
            recorderState = RECORDER_STATE_STARTING

            startRecordThread()
        } catch (e: FileNotFoundException) {
            onRecordFailure()
            e.printStackTrace()
        }

    }

    @Throws(FileNotFoundException::class)
    private fun startRecordThread() {

        Thread(object : PriorityRunnable() {

            private fun onExit() {
                synchronized(recorderStateMonitor) {
                    recorderState = RECORDER_STATE_IDLE
                    recorderStateMonitor.notifyAll()
                }
            }


            override fun run() {
                val bufferSize = Math.max(BUFFER_BYTES_ELEMENTS * BUFFER_BYTES_PER_ELEMENT,
                        AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING))

                val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING, bufferSize)

                try {
                    if (recorderState == RECORDER_STATE_STARTING) {
                        recorderState = RECORDER_STATE_BUSY
                    }
                    recorder.startRecording()

                    val recordBuffer = ByteArray(bufferSize)
                    do {
                        val bytesRead = recorder.read(recordBuffer, 0, bufferSize)

                        if (bytesRead > 0) {
                            recordingCallback!!.onDataReady(recordBuffer)
                        } else {
                            Log.e(AudioRecorder::class.java.simpleName, "error: $bytesRead")
                            onRecordFailure()
                        }
                    } while (recorderState == RECORDER_STATE_BUSY)
                } finally {
                    recorder.release()
                }
                onExit()
            }
        }).start()
    }

    override fun finishRecord() {
        var recorderStateLocal = recorderState
        if (recorderStateLocal != RECORDER_STATE_IDLE) {
            synchronized(recorderStateMonitor) {
                recorderStateLocal = recorderState
                if (recorderStateLocal == RECORDER_STATE_STARTING || recorderStateLocal == RECORDER_STATE_BUSY) {

                    recorderState = RECORDER_STATE_STOPPING
                    recorderStateLocal = recorderState
                }

                do {
                    try {
                        if (recorderStateLocal != RECORDER_STATE_IDLE) {
                            recorderStateMonitor.wait()
                        }
                    } catch (ignore: InterruptedException) {
                        /* Nothing to do */
                    }

                    recorderStateLocal = recorderState
                } while (recorderStateLocal == RECORDER_STATE_STOPPING)
            }
        }
    }

    internal interface RecordingCallback {
        fun onDataReady(data: ByteArray)
    }

    companion object {

        val RECORDER_SAMPLE_RATE = 8000
        val RECORDER_CHANNELS = AudioFormat.CHANNEL_OUT_MONO
        val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT


        private val BUFFER_BYTES_ELEMENTS = 1024
        private val BUFFER_BYTES_PER_ELEMENT = RECORDER_AUDIO_ENCODING
        private val RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO


        val RECORDER_STATE_FAILURE = -1
        val RECORDER_STATE_IDLE = 0
        val RECORDER_STATE_STARTING = 1
        val RECORDER_STATE_STOPPING = 2
        val RECORDER_STATE_BUSY = 3
    }
}