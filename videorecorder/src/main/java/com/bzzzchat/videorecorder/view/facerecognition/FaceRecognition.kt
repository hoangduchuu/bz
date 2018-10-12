package com.bzzzchat.videorecorder.view.facerecognition

import android.os.Environment
import android.util.Log
import com.bzzzchat.videorecorder.view.facerecognition.others.Utils
import com.bzzzchat.videorecorder.view.model.FaceData
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.javacpp.opencv_core.*
import org.bytedeco.javacpp.opencv_face
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer
import org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import java.io.File
import java.io.FilenameFilter
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.concurrent.thread

enum class FaceRecognitionResult {
    SUCCESS, FAILED
}

class FaceRecognition {
    private val TAG = "FaceRecognition"

    private var faceRecognizer: opencv_face.FaceRecognizer? = null

    fun getTrainingFolder(): String {
        val trainingFolder = File(Environment.getExternalStorageDirectory(), "training")
        if (!trainingFolder.exists()) {
            trainingFolder.mkdirs()
        }
        return trainingFolder.absolutePath
    }

    fun trainModel() {
        thread {
            val trainingDir = getTrainingFolder()
            Log.d(TAG, "Start training model")
            val root = File(trainingDir)
            val imgFilter = FilenameFilter { dir, name ->
                var name = name
                name = name.toLowerCase()
                name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png")
            }

            val imageFiles = root.listFiles(imgFilter)

            val images = MatVector(imageFiles.size.toLong())

            val labels = Mat(imageFiles.size, 1, CV_32SC1)
            val labelsBuf: IntBuffer = labels.createBuffer()

            for ((counter, image) in imageFiles.withIndex()) {
                //val img = imread(image.absolutePath, CV_LOAD_IMAGE_GRAYSCALE)
                val img = Utils.brightnessAndContrastAuto(image.absolutePath)
                //val processImage: org.opencv.core.Mat = org.opencv.core.Mat()

                val label = Integer.parseInt(image.name.split("\\-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
                Log.d(TAG, "name: " + image.name + ", label: " + label)
                images.put(counter.toLong(), img)

                labelsBuf.put(counter, label)

            }
//            faceRecognizer = FisherFaceRecognizer.create()
//        faceRecognizer = EigenFaceRecognizer.create(0, threshold)
            val threshold: Double = 70.0
//            faceRecognizer = LBPHFaceRecognizer.create(1, 8, 8, 8, threshold)
            faceRecognizer = LBPHFaceRecognizer.create()
            faceRecognizer?.train(images, labels)
        }
    }

    fun faceRecognition(testFile: String): FaceData {
        Log.d(TAG, "Start recognize ${File(testFile).name}")
        val testImage = imread(testFile, CV_LOAD_IMAGE_GRAYSCALE)
        val label = IntPointer(1)
        val confidence = DoublePointer(1)
        faceRecognizer?.predict(testImage, label, confidence)
        val predictedLabel = label.get(0)

        Log.d(TAG, "Predicted label $predictedLabel")
        Log.d(TAG, "Confidence: ${confidence.get()}")
        return FaceData(predictedLabel, confidence.get())
    }

    fun faceRecognition(bytes: ByteArray): FaceRecognitionResult {
        val testImage = imread(BytePointer(ByteBuffer.wrap(bytes)))
        val label = IntPointer(1)
        val confidence = DoublePointer(1)
        faceRecognizer?.predict(testImage, label, confidence)
        val predictedLabel = label.get(0)

        Log.d(TAG, "Predicted label $predictedLabel")
        Log.d(TAG, "Confidence: ${confidence.get()}")
        if (predictedLabel > 0 && confidence.get() < 50) {
            return FaceRecognitionResult.SUCCESS
        }
        return FaceRecognitionResult.FAILED
    }

    fun removeTrainingData() {
        File(getTrainingFolder()).delete()
    }

    fun releaseResource() {
        faceRecognizer = null
        removeTrainingData()
    }

    companion object {
        @JvmStatic
        val instance: FaceRecognition by lazy { FaceRecognition() }
    }
}