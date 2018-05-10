package com.ping.android.presentation.module.recorder

import com.cleveroad.audiovisualization.DbmHandler
import com.ping.android.utils.Log

class AudioRecordingHandler: DbmHandler<Float>(), AudioRecorder.RecordingCallback {
    override fun onDataReady(data: Float) {
        onDataReceived(data)
    }

    override fun onDataReceivedImpl(data: Float?, layersCount: Int, dBmArray: FloatArray?, ampsArray: FloatArray?) {
        var amplitude = data!!
        amplitude /= 100
        Log.e("Amplitude $amplitude")
        if (amplitude <= 0.5) {
            amplitude = 0.0f
        } else if (amplitude > 0.5 && amplitude <= 0.6) {
            amplitude = 0.2f
        } else if (amplitude > 0.6 && amplitude <= 0.7) {
            amplitude = 0.6f
        } else if (amplitude > 0.7) {
            amplitude = 1f
        }
        try {
            dBmArray!![0] = amplitude
            ampsArray!![0] = amplitude
        } catch (e: Exception) {
        }
    }

    fun stop() {
        try {
            calmDownAndStopRendering()
        } catch (e: Exception) {
        }
    }
}