package com.bzzzchat.videorecorder.util

class FFmpegInvoke(val path: String) {
    external fun run(param: String, params: Array<String>): Int

    fun run(params: Array<String>): Int {
        var res = -1
        try {
            res = run(path, params)
        } catch (exception: Exception) {
            exception.printStackTrace()
            return res
        }
        return res
    }

    companion object {
        init {
            System.loadLibrary("ffmpeginvoke")
        }
    }
}