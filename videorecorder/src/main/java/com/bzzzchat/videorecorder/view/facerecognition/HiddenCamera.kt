package com.bzzzchat.videorecorder.view.facerecognition

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.Log
import android.util.SparseArray
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.bzzzchat.videorecorder.view.facerecognition.others.Camera2Source
import com.bzzzchat.videorecorder.view.facerecognition.others.CameraSourcePreview
import com.bzzzchat.videorecorder.view.facerecognition.others.GraphicOverlay
import com.bzzzchat.videorecorder.view.facerecognition.others.Utils
import com.bzzzchat.videorecorder.view.facerecognition.preprocessor.PreProcessorFactory
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

interface RecognitionCallback {
    fun onRecognitionSuccess()
}

class HiddenCamera(val context: Context, val callback: RecognitionCallback) {
    private val TAG = "HiddenCamera"
    private val confidenceThreshold = 30
    private val minConfidenceThreshold = 20
    private lateinit var cameraPreview: CameraSourcePreview
    private lateinit var previewFaceDetector: FaceDetector
    private lateinit var myDetector: MyFaceDetector
    private lateinit var mCamera2Source: Camera2Source
    private lateinit var visionDetector: FirebaseVisionFaceDetector
    private val preProcessorFactory = PreProcessorFactory(context)

    private var wasActivityResumed = false
    private var isProcessingImage = AtomicBoolean(false)

    private var width: Int = 200
    private var height: Int = 200

    private var confidenceCounter = AtomicInteger(0)
    private val counter = 3

    fun initWithActivity(activity: Activity) {
        addPreview(activity)
        setupFaceDetector()
    }

    private fun processFaceRecognition(path: String) {
        val result = FaceRecognition.getInstance(context).faceRecognition(path)
//        Toast.makeText(context, "Confidence: ${faceData.confidence}", Toast.LENGTH_SHORT).show()
        if (result == FaceRecognitionResult.SUCCESS){
            onRecognizedUser(path)
        }else{
            confidenceCounter.incrementAndGet()
        }
//        if (faceData.label > 0) {
//            if (faceData.confidence < minConfidenceThreshold) {
//                // Recognize user
//                onRecognizedUser(path)
//            }
//            if (faceData.confidence < confidenceThreshold) {
//                if (confidenceCounter.incrementAndGet() >= counter) {
//                    onRecognizedUser(path)
//                }
//            }
//        } else {
//            confidenceCounter.set(0)
//        }
    }

    private fun onRecognizedUser(path: String) {
        callback.onRecognitionSuccess()
        confidenceCounter.set(0)
        stopCameraSource()
        // Store this image for next recognition
        //Utils.moveFile(File(path), File(FaceRecognition.instance.getTrainingFolder()))
        //FaceRecognition.instance.trainModel()
    }

    internal val camera2SourceShutterCallback = Camera2Source.ShutterCallback { Log.d(TAG, "Shutter Callback for CAMERA2") }

    internal val camera2SourcePictureCallback = Camera2Source.PictureCallback { image ->
        Log.d(TAG, "Taken picture is here!")
        if (isProcessingImage.get()) {
            image.close()
            return@PictureCallback
        }
        isProcessingImage.set(true)
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        var picture = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        val ratio = picture.width.toDouble() / picture.height
        val newWidth = 1024
        val newHeight = (newWidth / ratio).toInt()
        picture = Bitmap.createScaledBitmap(picture, newWidth, newHeight, true)
        picture = rotateImage(picture, 270)

        val visionImage = FirebaseVisionImage.fromBitmap(picture)
        val finalPicture = picture
        image.close()
        visionDetector.detectInImage(visionImage)
                .addOnSuccessListener { faces ->
                    if (faces.size > 0) {
                        val face = faces[0]
                        val faceBitmap = Utils.getFaceFromBitmap(finalPicture, face)
                        val fileName = "user.png"
                        val file = File(Environment.getExternalStorageDirectory(), fileName)
                        //Utils.saveMatToImage(preProcessorFactory.processBitmap(faceBitmap), file.absolutePath)
                        Utils.saveBitmap(faceBitmap, file.absolutePath)
//                        Utils.brightnessAndContrastAuto(file.absolutePath)
//                        Utils.smooth(file.absolutePath)
                        processFaceRecognition(file.absolutePath)
                    }
                    isProcessingImage.set(false)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    isProcessingImage.set(false)
                }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    private inner class FaceTrackerFactory : MultiProcessor.Factory<Face> {
        override fun create(face: Face): Tracker<Face> {
            return FaceTracker()
        }
    }

    private inner class FaceTracker : Tracker<Face>()

    private inner class MyFaceDetector : Detector<Face>() {
        override fun detect(p0: Frame?): SparseArray<Face> {
            val faces = previewFaceDetector.detect(p0)
            if (faces.size() == 1) {
                p0?.let {
                    if (isProcessingImage.get()) {
                        return faces
                    }
                    mCamera2Source.takePicture(camera2SourceShutterCallback, camera2SourcePictureCallback)
                }
            }
            return faces
        }
    }

    private fun setupFaceDetector() {
        createCameraSourceFront()
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build()
        visionDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options)
    }

    private fun createCameraSourceFront() {
        // TODO need a plan to upgrade to mlkit

        previewFaceDetector = FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(true)
                //.setTrackingEnabled(true)
                .build()
        myDetector = MyFaceDetector()
        myDetector.setProcessor(MultiProcessor.Builder<Face>(FaceTrackerFactory()).build())
        if (previewFaceDetector.isOperational) {
            previewFaceDetector.setProcessor(MultiProcessor.Builder<Face>(FaceTrackerFactory()).build())
        } else {
            Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show()
        }

        mCamera2Source = Camera2Source.Builder(context, myDetector)
                .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                .setFacing(Camera2Source.CAMERA_FACING_FRONT)
                .build()
        //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
        //WE WILL USE CAMERA1.
        if (mCamera2Source.isCamera2Native) {
            startCameraSource()
        }
    }

    private fun startCameraSource() {
        try {
            cameraPreview.start(mCamera2Source, GraphicOverlay(context))
        } catch (e: IOException) {
            mCamera2Source.release()
        }
    }

    private fun addPreview(activity: Activity) {
        cameraPreview = CameraSourcePreview(activity)
        cameraPreview.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val view = (activity.window.decorView.rootView as ViewGroup).getChildAt(0)
        when (view) {
            is LinearLayout -> {
                val params = LinearLayout.LayoutParams(width, height)
                (view as LinearLayout).addView(cameraPreview, params!!)
            }
            is RelativeLayout -> {
                val params = RelativeLayout.LayoutParams(width, height)
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                view.addView(cameraPreview, params)
            }
            is FrameLayout -> {
                val params = FrameLayout.LayoutParams(width, height)
                view.addView(cameraPreview, params)
            }
            else -> throw RuntimeException("Root view of the activity/fragment cannot be other than Linear/Relative/Frame layout")
        }
    }

    fun onResume() {
        if (wasActivityResumed) {
            createCameraSourceFront()
        }
    }

    fun onPause() {
        wasActivityResumed = true
        stopCameraSource()
    }

    fun onDestroy() {
        stopCameraSource()
        if (previewFaceDetector != null) {
            previewFaceDetector.release()
        }
    }

    private fun stopCameraSource() {
        cameraPreview.stop()
    }
}