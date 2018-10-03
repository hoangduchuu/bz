package com.bzzzchat.videorecorder.view.facerecognization

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.Log
import android.util.SparseArray
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.bzzzchat.videorecorder.view.facerecognization.others.Camera2Source
import com.bzzzchat.videorecorder.view.facerecognization.others.CameraSourcePreview
import com.bzzzchat.videorecorder.view.facerecognization.others.GraphicOverlay
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException


class HiddenCamera(val context: Context) {
    private lateinit var cameraPreview: CameraSourcePreview
    private lateinit var previewFaceDetector: FaceDetector
    private lateinit var myDetector: MyFaceDetector
    private lateinit var mCamera2Source: Camera2Source

    private var wasActivityResumed = false

    private var width: Int = 200
    private var height: Int = 200

    fun initWithActivity(activity: Activity) {
        addPreview(activity)
        setupFaceDetector()
    }

    private fun setupFaceDetector() {
        createCameraSourceFront()
    }

    private fun createCameraSourceFront() {
        previewFaceDetector = FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build()
        myDetector = MyFaceDetector()
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
//        width = view.measuredWidth
//        height = view.measuredHeight
        when (view) {
            is LinearLayout -> {
                val params = LinearLayout.LayoutParams(width, height)
                view.addView(cameraPreview, params)
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

    private inner class FaceTrackerFactory : MultiProcessor.Factory<Face> {
        override fun create(face: Face): Tracker<Face> {
            return FaceTracker()
        }
    }

    private inner class FaceTracker : Tracker<Face>() {
        override fun onNewItem(p0: Int, p1: Face?) {
            super.onNewItem(p0, p1)
        }

        override fun onUpdate(p0: Detector.Detections<Face>?, p1: Face?) {
            super.onUpdate(p0, p1)
            p1?.let {
                val x = it.position.x
                val y = it.position.y
                val x2 = x / 4 + it.width
                val y2 = y / 4 + it.height
                //results = Bitmap.createBitmap(bitmap, x.toInt(), y.toInt(), x2.toInt(), y2.toInt())
            }

        }
    }

    private inner class MyFaceDetector: Detector<Face>() {

        override fun detect(p0: Frame?): SparseArray<Face> {
            val faces = previewFaceDetector.detect(p0)
            if (faces.size() == 1) {
                p0?.let {
                    val buf = p0.grayscaleImageData
                    val imageBytes = ByteArray(buf.remaining())
                    buf.get(imageBytes)
                    val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    val size = mCamera2Source.previewSize
                    val width = size.width
                    val height = size.height

                    val yuvImage = YuvImage(it.grayscaleImageData.array(), ImageFormat.NV21, size.width, size.height, null)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, byteArrayOutputStream)
                    val jpegArray = byteArrayOutputStream.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.size)
                    val thisFace = faces.valueAt(0)
                    val x = thisFace.position.x
                    val y = thisFace.position.y
                    val x2 = x / 4 + thisFace.width
                    val y2 = y / 4 + thisFace.height
                    val faceBitmap = Bitmap.createBitmap(bitmap, x.toInt(), y.toInt(), x2.toInt(), y2.toInt())
                    print("detected")
                }
            }
            return faces
        }


        fun getFace(context: android.content.Context, data: ByteArray): Bitmap? {
            try {
                val imageStrem = ByteArrayInputStream(data)
                var bitmap = BitmapFactory.decodeStream(imageStrem)
                if (bitmap.width > bitmap.height) {
                    val matrix = Matrix()
                    matrix.postRotate(270f)
                    if (bitmap.width > 1500) matrix.postScale(0.5f, 0.5f)
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
                val faceDetector = FaceDetector.Builder(context).setProminentFaceOnly(true).setTrackingEnabled(false).build()
                val frame = Frame.Builder().setBitmap(bitmap).build()
                val faces = faceDetector.detect(frame)
                var results: Bitmap? = null
                for (i in 0 until faces.size()) {
                    val thisFace = faces.valueAt(i)
                    val x = thisFace.position.x
                    val y = thisFace.position.y
                    val x2 = x / 4 + thisFace.width
                    val y2 = y / 4 + thisFace.height
                    results = Bitmap.createBitmap(bitmap, x.toInt(), y.toInt(), x2.toInt(), y2.toInt())
                }
                return results
            } catch (e: Exception) {
                Log.e("GET_FACE", e.message)
            }
            return null
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