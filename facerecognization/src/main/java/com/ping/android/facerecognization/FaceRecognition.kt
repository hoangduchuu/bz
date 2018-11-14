package com.ping.android.facerecognization

import android.util.Log
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.javacpp.opencv_core.*
import org.bytedeco.javacpp.opencv_face.*
import org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import java.io.File
import java.io.FilenameFilter
import java.nio.IntBuffer

enum class FaceRecognitionResult {
    SUCCESS, FAILED
}

class FaceRecognition {
    private val TAG = "FaceRecognition"

    private var faceRecognizer: FaceRecognizer? = null

    fun trainModel(trainingDir: String, threshold: Double = 50.0) {
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
            val img = imread(image.absolutePath, CV_LOAD_IMAGE_GRAYSCALE)

            val label = Integer.parseInt(image.name.split("\\-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
            Log.d(TAG, "name: " + image.name + ", label: " + label)
            images.put(counter.toLong(), img)

            labelsBuf.put(counter, label)

        }

//        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
//        faceRecognizer = EigenFaceRecognizer.create()
        faceRecognizer = LBPHFaceRecognizer.create(2, 8, 8, 8, threshold)
        faceRecognizer?.train(images, labels)
    }

    fun faceRecognition(testFile: String): FaceRecognitionResult {
        Log.d(TAG, "Start recognize ${File(testFile).name}")
        val testImage = imread(testFile, CV_LOAD_IMAGE_GRAYSCALE)
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
}