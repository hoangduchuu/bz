package com.bzzzchat.videorecorder.view.facerecognition

import android.os.Environment
import android.util.Log
import java.nio.ByteBuffer
import kotlin.concurrent.thread
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.TimeUnit
import org.tensorflow.lite.Interpreter
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.pow
import com.bzzzchat.videorecorder.R
import com.bzzzchat.videorecorder.view.facerecognition.others.Utils
import java.io.*
import java.util.*


enum class FaceRecognitionResult {
    SUCCESS, FAILED
}

class FaceRecognition private constructor(context: Context) {
    private val TAG = "FaceRecognition"
    private val MODEL_PATH = "optimized_facenet_quantized.tflite"
    // Specify the output size
    private val NUMBER_LENGTH = 128
    // Specify the input size
    private val DIM_BATCH_SIZE = 1
    private val DIM_IMG_SIZE_X = 160
    private val DIM_IMG_SIZE_Y = 160
    private val DIM_PIXEL_SIZE = 1
    // Number of bytes to hold a float (32 bits / float) / (8 bits / byte) = 4 bytes / float
    private val BYTE_SIZE_OF_FLOAT = 3
    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
    val IMAGE_MEAN = 128
    val IMAGE_STD = 128.0f
    private var referenceOutput: Array<ByteArray>? = null

    private var tflite: Interpreter? = null
    private var context: Context
    init {
        initInterpreter(context)
        this.context = context
    }
    fun getTrainingFolder(): String {
        val trainingFolder = File(Environment.getExternalStorageDirectory(), "training")
        if (!trainingFolder.exists()) {
            trainingFolder.mkdirs()
        }
        return trainingFolder.absolutePath
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    fun initInterpreter(context: Context) {
        try {
            tflite = Interpreter(loadModelFile(context))
            referenceOutput = Array(1) { ByteArray(128) }
        } catch (e: IOException) {
            Log.e(TAG, "IOException loading the tflite file")
        }
    }
    fun train() {
        thread {
            val trainingDir = getTrainingFolder()
            Log.d(TAG, "Start training model")
            val root = File(trainingDir)
            val imgFilter = FilenameFilter { dir, name ->
                var name = name
                name = name.toLowerCase()
                name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png")
            }

            val imageFiles = root.listFiles(imgFilter) ?: return@thread
            val ops = BitmapFactory.Options()
            ops.inPreferredConfig = Bitmap.Config.RGB_565
            val bitmap = BitmapFactory.decodeFile(imageFiles[0].absolutePath, ops)
            var inputBuffer = convertBitmapToByteBuffer(bitmap)

            tflite!!.run(inputBuffer!!, referenceOutput!!)
        }
    }

    fun isFastEnough(): Boolean{
        val start = Date()
        val bm = context.resources.openRawResource(R.raw.jason)
        val bufferedInputStream = BufferedInputStream(bm)
        val bitmap = BitmapFactory.decodeStream(bufferedInputStream)

        var bitmap96 = Bitmap.createScaledBitmap(bitmap, Configs.modelDimension, Configs.modelDimension, true)
        bitmap.recycle()
        bitmap96 = Utils.convertTo565(bitmap96)
        var inputBuffer = convertBitmapToByteBuffer(bitmap96)
        var output = Array(1) { ByteArray(128) }

        tflite!!.run(inputBuffer, output)
        val end = Date()
        val diff = end.time - start.time
        Log.e(TAG, "diff: $diff")
        return diff / 1000.0 < 0.5
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer{
        //Clear the Bytebuffer for a new image
        var imgData = ByteBuffer.allocateDirect(
                BYTE_SIZE_OF_FLOAT * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE)
        imgData.order(ByteOrder.nativeOrder())
        bitmap?.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        for (i: Int in 0 until intValues.size) {
            var pixel = intValues[i]
//            val channel = pixel and 0xff
//            imgData.putInt(0xff - channel)
            imgData.put((pixel shr 16 and 0xFF).toByte())
            imgData.put((pixel shr 8 and 0xFF).toByte())
            imgData.put((pixel and 0xFF).toByte())
        }

//        for (i in 0 until DIM_IMG_SIZE_X) {
//            for (j in 0 until DIM_IMG_SIZE_Y) {
//                val currPixel = intValues[pixel++]
//                imgData.putFloat(((currPixel shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
//                imgData.putFloat(((currPixel shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
//                imgData.putFloat(((currPixel and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
//            }
//        }
        bitmap?.recycle()
        return imgData
    }

    fun faceRecognition(fileName: String): FaceRecognitionResult {
        val ops = BitmapFactory.Options()
        ops.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeFile(fileName, ops)
        val inputBuffer = convertBitmapToByteBuffer(bitmap)
        val output = Array(1) { ByteArray(128) }
        tflite!!.run(inputBuffer!!, output)
        if (referenceOutput != null && output.size == referenceOutput!!.size){
            var result = 0.0
            var ref = referenceOutput!![0]
            var out = output!![0]
            for (index in 0..127) {
                var refi = 0.0078125 * (128 - ref!![index])
                if (ref!![index] < 0){
                    refi = -0.0078125 * (128 + ref!![index])
                }
                var outi = 0.0078125 * (128 - out!![index])
                if (out!![index] < 0){
                    outi = -0.0078125 * (128 + out!![index])
                }
                result += (refi-outi).pow(2)
            }
            result = Math.sqrt(result)
//            Toast.makeText(context, "Confidence: ${result}",
//                    Toast.LENGTH_LONG).show()

            Log.d(TAG, "Confidence: ${result}")
            if (result < 0.5) {
                return FaceRecognitionResult.SUCCESS
            }
        }
        return  FaceRecognitionResult.FAILED
    }

    fun removeTrainingData() {
        referenceOutput = null
        File(getTrainingFolder()).delete()
    }

    companion object : SingletonHolder<FaceRecognition, Context>(::FaceRecognition)

//    companion object Single {
//        private var instance : FaceRecognition? = null
//
//        fun  getInstance(activity: Activity): FaceRecognition {
//
//        }
//    }
}