package com.bzzzchat.videorecorder.util

import android.util.Log
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern

class Processor(paramString: String) {
    private var mCommand: MutableList<String> = ArrayList()
    private var mFilters: MutableList<String> = ArrayList()
    private val mInvoker: FFmpegInvoke = FFmpegInvoke(paramString)
    private var mMetaData: HashMap<String, String> = HashMap()
    private var numCores = Processor.numCores

    fun addInputPath(paramString: String): Processor {
        this.mCommand.add("-i")
        this.mCommand.add(paramString)
        return this
    }

    fun addMetaData(paramString1: String, paramString2: String): Processor {
        this.mMetaData[paramString1] = paramString2
        return this
    }

    fun enableOverwrite(): Processor {
        this.mCommand.add("-y")
        return this
    }

    fun sameq(): Processor {
        this.mCommand.add("-sameq")
        return this
    }

    fun enableShortest(): Processor {
        this.mCommand.add("-shortest")
        return this
    }

    fun filterCrop(paramInt1: Int, paramInt2: Int): Processor {
        this.mFilters.add("crop=$paramInt1:$paramInt2")
        return this
    }

    fun newCommand(): Processor {
        this.mMetaData = HashMap()
        this.mFilters = ArrayList()
        this.mCommand = ArrayList()
        this.mCommand.add("ffmpeg")

        return this
    }

    fun process(paramArrayOfString: Array<String>): Int {
        return this.mInvoker.run(paramArrayOfString)
    }


    fun processToOutput(paramString: String): Int {
        if (this.mFilters.size > 0) {
            this.mCommand.add("-vf")
            val localStringBuilder = StringBuilder()
            val localIterator3 = this.mFilters.iterator()
            while (localIterator3.hasNext()) {
                localStringBuilder.append(localIterator3.next())
                localStringBuilder.append(",")
            }
            val str2 = localStringBuilder.toString()
            this.mCommand.add(str2.substring(0, -1 + str2.length))
        }
        val localIterator1 = this.mMetaData.keys.iterator()
        while (localIterator1.hasNext()) {
            val str1 = localIterator1.next()
            this.mCommand.add("-metadata")
            this.mCommand.add(str1 + "=" + "\"" + this.mMetaData[str1] as String + "\"")
        }
        if (numCores > 1)
            this.mCommand.add(paramString)
        val localIterator2 = this.mCommand.iterator()
        while (localIterator2.hasNext())
            Log.i("FFMPEG ARGUMENTS '{}'", localIterator2.next())
        return process(this.mCommand.toTypedArray())
    }

    fun setAudioCopy(): Processor {
        this.mCommand.add("-acodec")
        this.mCommand.add("copy")
        return this
    }

    fun setFilterComplex(): Processor {
        this.mCommand.add("-filter_complex")
        this.mCommand.add("\"[0:0] [0:1] [1:0] [1:1] [2:0] [2:1] concat=n=2:v=1:a=1 [v] [a]\"")
        return this
    }

    fun setBsfA(paramString: String): Processor {
        this.mCommand.add("-bsf:a")
        this.mCommand.add(paramString)
        return this
    }

    fun setBsfV(paramString: String): Processor {
        this.mCommand.add("-bsf:v")
        this.mCommand.add(paramString)
        return this
    }

    fun setCopy(): Processor {
        this.mCommand.add("-c")
        this.mCommand.add("copy")
        return this
    }

    fun setFormat(paramString: String): Processor {
        this.mCommand.add("-f")
        this.mCommand.add(paramString)
        return this
    }

    fun setFrames(paramLong: Long, paramInt: Int): Processor {
        this.mCommand.add("-vframes")
        this.mCommand.add((paramLong / 1000.0 * paramInt).toInt().toString())
        return this
    }

    fun setMap(paramString: String): Processor {
        this.mCommand.add("-map")
        this.mCommand.add(paramString)
        return this
    }

    fun setMetaData(paramHashMap: HashMap<String, String>): Processor {
        this.mMetaData = paramHashMap
        return this
    }

    fun setShortest(): Processor {
        this.mCommand.add("-shortest")
        return this
    }

    fun setStart(paramLong: Long): Processor {
        this.mCommand.add("-ss")
        this.mCommand.add((paramLong / 1000.0).toString())
        return this
    }

    fun setTotalDuration(paramLong: Long): Processor {
        this.mCommand.add("-t")
        this.mCommand.add((paramLong / 1000.0).toString())
        return this
    }

    fun setVideoCopy(): Processor {
        this.mCommand.add("-vcodec")
        this.mCommand.add("copy")
        return this
    }

    fun useX264(): Processor {
        this.mCommand.add("-vcodec")
        this.mCommand.add("libx264")
        return this
    }

    fun setStrict(): Processor {
        this.mCommand.add("-strict")
        this.mCommand.add("experimental")
        return this
    }

    fun setAudioCodec(): Processor {
        this.mCommand.add("-acodec")
        this.mCommand.add("aac")
        return this
    }

    fun setVideoCodec(): Processor {
        this.mCommand.add("-vcodec")
        this.mCommand.add("h264")
        return this
    }


    fun setThread(): Processor {
        this.mCommand.add("-threads")
        this.mCommand.add("1")
        return this
    }

    fun setPreset(): Processor {
        this.mCommand.add("-preset")
        this.mCommand.add("ultrafast")
        return this
    }

    fun setStrict2(): Processor {
        this.mCommand.add("-strict")
        this.mCommand.add("-2")
        return this
    }

    fun setWaterMark(imagePath: String): Processor {
        this.mCommand.add("-vf")
        this.mCommand.add("movie=$imagePath  [watermark]; [in][watermark] overlay=main_w-overlay_w-10:10 [out]")
        return this
    }

    fun setOverlayFilter(overlayImage: String): Processor {
        this.mCommand.add("-vf")
        this.mCommand.add("movie=$overlayImage [logo];[in][logo] overlay=0:0 [out]")

        this.mCommand.add("-acodec")
        this.mCommand.add("copy")

        return this
    }

    fun setOverlayFilter2(): Processor {
        this.mCommand.add("-vf")
        this.mCommand.add("overlay=0:0")

        return this
    }

    fun setConcatFilter(): Processor {
        this.mCommand.add("-f")
        this.mCommand.add("concat")

        return this
    }

    fun setTransposeFilter(frontCamera: Boolean, isRotateVideo: Boolean): Processor {
        this.mCommand.add("-vf")
        if (isRotateVideo) {
            if (frontCamera) {
                this.mCommand.add("transpose=2")
            } else {
                this.mCommand.add("transpose=1")
            }
        }
        return this
    }

    fun setRotateFilter(frontCamera: Boolean, isRotateVideo: Boolean): Processor {
        this.mCommand.add("-vf")
        if (isRotateVideo) {
            if (frontCamera) {
                this.mCommand.add("transpose=2")
            } else {
                this.mCommand.add("transpose=1")
            }
        }
        return this
    }

    companion object {
        private val numCores: Int
            get() {
                try {
                    return File("/sys/devices/system/cpu/").listFiles { paramAnonymousFile -> Pattern.matches("cpu[0-9]", paramAnonymousFile.name) }!!.size
                } catch (localException: Exception) {
                }

                return 1
            }
    }
}