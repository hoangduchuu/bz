package com.bzzzchat.videorecorder.view.facerecognition

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraAccessException
import android.media.Image
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
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

interface RecognitionCallback {
    fun onRecognitionSuccess()
    fun onRecognizingError()
}

class HiddenCamera(val context: Context, val callback: RecognitionCallback) {
    private val TAG = "HiddenCamera"
    private lateinit var cameraPreview: CameraSourcePreview
    private lateinit var previewFaceDetector: FaceDetector
    private lateinit var myDetector: MyFaceDetector
    private lateinit var mCamera2Source: Camera2Source
    private lateinit var mFaceDetector: FaceDetector

    private var wasActivityResumed = false
    private var isProcessingImage = AtomicBoolean(false)

    private var width: Int = 200
    private var height: Int = 200

    private var confidenceCounter = AtomicInteger(0)
    private val counter = 3

    fun initWithActivity(activity: Activity) {
        addPreview(activity)
        val faceDetector = FaceDetector.Builder(context).setMode(FaceDetector.FAST_MODE)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setTrackingEnabled(false)
                .setProminentFaceOnly(true)
                .setMinFaceSize(0.25f)
                .build()
        mFaceDetector = faceDetector
    }

    private fun processFaceRecognition(path: String) {
        val result = FaceRecognition.getInstance(context).faceRecognition(path)
//        Toast.makeText(context, "Confidence: ${faceData.confidence}", Toast.LENGTH_SHORT).show()
        if (result == FaceRecognitionResult.SUCCESS){
            callback.onRecognitionSuccess()
            confidenceCounter.set(0)
            stopCameraSource()
        }else{
            callback.onRecognizingError()
            confidenceCounter.incrementAndGet()
        }
    }

    internal val camera2SourceShutterCallback = Camera2Source.ShutterCallback { Log.d(TAG, "Shutter Callback for CAMERA2") }

    val that = this
    internal val camera2SourcePictureCallback = Camera2Source.PictureCallback { image ->
        if (isProcessingImage.get()) {
            image.close()
            return@PictureCallback
        }
        isProcessingImage.set(true)

        var rotation = 0
        try {
            rotation = Utils.getRotationCompensation(mCamera2Source.cameraId, context as Activity)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        val picture = Utils.getBitmapFromImage(image)
        image.close()
        val finalPicture = Utils.rotateImage(picture, rotation, 384)



        val visionImage = FirebaseVisionImage.fromBitmap(finalPicture)
        visionDetector.detectInImage(visionImage)
                .addOnSuccessListener { faces ->
                    if (faces.size > 0) {
                        val face = faces[0]
                        val faceBitmap = Utils.getFaceFromBitmap(finalPicture, face)
                        val fileName = "user.png"
                        val file = File(Environment.getExternalStorageDirectory(), fileName)
                        Utils.saveBitmap(faceBitmap, file.absolutePath)
                        processFaceRecognition(file.absolutePath)
                    }
                    isProcessingImage.set(false)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    isProcessingImage.set(false)
                }
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
//                    camera2SourcePictureCallback.onPictureTaken()
                    mCamera2Source.takePicture(camera2SourceShutterCallback, camera2SourcePictureCallback)
                }
            }
            return faces
        }
    }

    private fun createCameraSourceFront() {
        // TODO need a plan to upgrade to mlkit

        previewFaceDetector = FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
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

        startCameraSource()
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

    fun stopCameraSource() {
        cameraPreview.stop()
    }
}