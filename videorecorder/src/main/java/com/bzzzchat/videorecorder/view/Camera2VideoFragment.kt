package com.bzzzchat.videorecorder.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
import android.hardware.camera2.CameraCharacteristics.SENSOR_ORIENTATION
import android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
import android.hardware.camera2.CameraDevice.TEMPLATE_RECORD
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.bzzzchat.videorecorder.R
import com.bzzzchat.videorecorder.util.CompareSizesByArea
import com.bzzzchat.videorecorder.util.ImageSaver
import com.bzzzchat.videorecorder.view.custom.AutoFitTextureView
import com.bzzzchat.videorecorder.view.custom.CustomRecordButton
import com.bzzzchat.videorecorder.view.custom.RecordButtonListener
import kotlinx.android.synthetic.main.fragment_camera2_video.*
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

fun withDelay(delay : Long, block : () -> Unit) {
    Handler().postDelayed(Runnable(block), delay)
}

class Camera2VideoFragment : Fragment(),
        ActivityCompat.OnRequestPermissionsResultCallback {
    private val TAG = "Camera2VideoFragment"
    private val BIT_RATE_1080P = 16000000
    private val BIT_RATE_MIN = 64000
    private val BIT_RATE_MAX = 40000000
    private val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
    private val SENSOR_ORIENTATION_INVERSE_DEGREES = 270
    private val ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 0)
        append(Surface.ROTATION_90, 90)
        append(Surface.ROTATION_180, 180)
        append(Surface.ROTATION_270, 270)
    }
    private val DEFAULT_ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 90)
        append(Surface.ROTATION_90, 0)
        append(Surface.ROTATION_180, 270)
        append(Surface.ROTATION_270, 180)
    }
    private val INVERSE_ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 90)
        append(Surface.ROTATION_90, 180)
        append(Surface.ROTATION_180, 270)
        append(Surface.ROTATION_270, 0)
    }
    private val VIDEO_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val MAXIMUM_RECORD_TIME = 10 * 1000 // 10s
    // region picture

    /**
     * An [ImageReader] that handles still image capture.
     */
    private var imageReader: ImageReader? = null

    /**
     * This is the output file for our picture.
     */
    private lateinit var file: File

    /**
     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        backgroundHandler?.post(ImageSaver(it.acquireNextImage(), file))
    }

    /**
     * Whether the current camera device supports Flash or not.
     */
    private var flashSupported = false

    /**
     * Whether the current camera device supports Auto focus or not.
     */
    private var autoFocusSupported = false

    // endregion

    /**
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a
     * [TextureView].
     */
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(cameraDirection, width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit

    }

    /**
     * An [AutoFitTextureView] for camera preview.
     */
    private lateinit var textureView: AutoFitTextureView

    /**
     * Button to record video
     */
    private lateinit var videoButton: CustomRecordButton

    private lateinit var instructionView: View

    /**
     * A reference to the opened [android.hardware.camera2.CameraDevice].
     */
    private var cameraDevice: CameraDevice? = null

    /**
     * A reference to the current [android.hardware.camera2.CameraCaptureSession] for
     * preview.
     */
    private var captureSession: CameraCaptureSession? = null

    /**
     * The [android.util.Size] of camera preview.
     */
    private lateinit var previewSize: Size

    /**
     * The [android.util.Size] of video recording.
     */
    private lateinit var videoSize: Size

    /**
     * Whether the app is recording video now
     */
    private var isRecordingVideo = false

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var backgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var backgroundHandler: Handler? = null

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val cameraOpenCloseLock = Semaphore(1)

    /**
     * [CaptureRequest.Builder] for the camera preview
     */
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    /**
     * Orientation of the camera sensor
     */
    private var sensorOrientation = 0

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its status.
     */
    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@Camera2VideoFragment.cameraDevice = cameraDevice
            startPreview()
            configureTransform(textureView.width, textureView.height)
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@Camera2VideoFragment.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@Camera2VideoFragment.cameraDevice = null
            activity?.finish()
        }

    }

    /**
     * Output file for video
     */
    private var nextVideoAbsolutePath: String? = null

    private var mediaRecorder: MediaRecorder? = null

    private var currentRecordTime = 0

    private var recordTimer: Timer? = null

    private var cameraDirection: Int = CameraCharacteristics.LENS_FACING_BACK

    private var outputFolder: String = ""

    private lateinit var orientationEventListener: OrientationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.apply {
            outputFolder = getString(VideoRecorderActivity.OUTPUT_FOLDER_EXTRA_KEY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera2_video, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textureView = view.findViewById(R.id.texture)
        videoButton = view.findViewById(R.id.video)
        instructionView = view.findViewById(R.id.tutorial_message)
        videoButton.setOnTouchListener { v, event ->
            instructionView.visibility = View.GONE
            false
        }
        videoButton.setListener(object : RecordButtonListener {
            override fun onTakeImage() {
                captureStillPicture()
            }

            override fun onStartRecord() {
                startRecordingVideo()
            }

            override fun onStopRecord() {
                stopRecordingVideo()
            }
        })
        btnSwitchCamera.setOnClickListener {
            handleCameraSwitch()
        }
        file = File(activity?.getExternalFilesDir(null), "pic.jpeg")
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable) {
            openCamera(cameraDirection, textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        orientationEventListener = OrientationListener(context!!)
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun handleCameraSwitch() {
        closeCamera()
        cameraDirection = if (cameraDirection == CameraCharacteristics.LENS_FACING_BACK) {
            CameraCharacteristics.LENS_FACING_FRONT
        } else {
            CameraCharacteristics.LENS_FACING_BACK
        }
        if (textureView.isAvailable) {
            openCamera(cameraDirection, textureView.width, textureView.height)
        }
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }
    }

    /**
     * Requests permissions needed for recording video.
     */
    private fun hasPermissionsGranted(permissions: Array<String>) =
            permissions.none {
                checkSelfPermission(activity, it) != PERMISSION_GRANTED
            }

    /**
     * Tries to open a [CameraDevice]. The result is listened by [stateCallback].
     *
     * Lint suppression - permission is checked in [hasPermissionsGranted]
     */
    @SuppressLint("MissingPermission")
    private fun openCamera(direction: Int, width: Int, height: Int) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            return
        }

        val cameraActivity = activity
        if (cameraActivity == null || cameraActivity.isFinishing) return

        val manager = cameraActivity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            var cameraId = ""
            for (_cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(_cameraId)

                // We don't use a front facing camera in this sample.
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null && cameraDirection != direction) {
                    continue
                }
                cameraId = _cameraId
                val map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP)
                        ?: throw RuntimeException("Cannot get available preview/video sizes")
                sensorOrientation = characteristics.get(SENSOR_ORIENTATION)
                videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder::class.java))
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
                        width, height, videoSize)

                imageReader = ImageReader.newInstance(previewSize.width, previewSize.height,
                        ImageFormat.JPEG, /*maxImages*/ 2).apply {
                    setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                }

                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)
                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                }
                configureTransform(width, height)
                mediaRecorder = MediaRecorder()

                // Check if the flash is supported.
                flashSupported =
                        characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                val afAvailableModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)

                autoFocusSupported = !(afAvailableModes.isEmpty()
                        || afAvailableModes.size == 1
                        && afAvailableModes[0] == CameraMetadata.CONTROL_AF_MODE_OFF)


                manager.openCamera(cameraId, stateCallback, null)
                break
            }
        } catch (e: CameraAccessException) {
            showToast("Cannot access the camera.")
            cameraActivity.finish()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.")
        }
    }

    /**
     * Close the [CameraDevice].
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            closePreviewSession()
            cameraDevice?.close()
            cameraDevice = null

            mediaRecorder?.release()
            mediaRecorder = null

            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /**
     * Start the camera preview.
     */
    private fun startPreview() {
        if (cameraDevice == null || !textureView.isAvailable) return

        try {
            closePreviewSession()
            val texture = textureView.surfaceTexture
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(TEMPLATE_PREVIEW)

            val previewSurface = Surface(texture)
            previewRequestBuilder.addTarget(previewSurface)

            cameraDevice?.createCaptureSession(listOf(previewSurface, imageReader!!.surface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(session: CameraCaptureSession) {
                            captureSession = session
                            updatePreview()
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            if (activity != null) showToast("Failed")
                        }
                    }, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * Update the camera preview. [startPreview] needs to be called in advance.
     */
    private fun updatePreview() {
        if (cameraDevice == null) return

        try {
            setUpCaptureRequestBuilder(previewRequestBuilder)
            //HandlerThread("CameraPreview").start()
            captureSession?.setRepeatingRequest(previewRequestBuilder.build(),
                    null, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    private fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder?) {
        builder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
    }

    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `textureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `textureView` is fixed.
     *
     * @param viewWidth  The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        activity ?: return
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                    viewHeight.toFloat() / previewSize.height,
                    viewWidth.toFloat() / previewSize.width)
            with(matrix) {
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        }
        textureView.setTransform(matrix)
    }

    @Throws(IOException::class)
    private fun setUpMediaRecorder() {
        val cameraActivity = activity ?: return

        if (nextVideoAbsolutePath.isNullOrEmpty()) {
            nextVideoAbsolutePath = getVideoFilePath()
        }
        val deviceRotation = orientationEventListener.rotation
        val surfaceRotation = when (cameraDirection) {
            CameraCharacteristics.LENS_FACING_FRONT -> INVERSE_ORIENTATIONS.get(deviceRotation)
            else -> DEFAULT_ORIENTATIONS.get(deviceRotation)
        }
        val realRotation = (surfaceRotation + sensorOrientation + 270) % 360
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(nextVideoAbsolutePath)
            setOrientationHint(realRotation)
            setVideoEncodingBitRate(getVideoBitRate(videoSize))
            setVideoFrameRate(30)
            setVideoSize(videoSize.width, videoSize.height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }
    }

    /**
     * Calculate a video bit rate based on the size. The bit rate is scaled
     * based on ratio of video size to 1080p size.
     */
    private fun getVideoBitRate(sz: Size): Int {
        var rate = BIT_RATE_1080P
        val scaleFactor = sz.height * sz.width / (1920 * 1080).toFloat()
        rate = (rate * scaleFactor).toInt()
        // Clamp to the MIN, MAX range.
        return Math.max(BIT_RATE_MIN, Math.min(BIT_RATE_MAX, rate));
    }

    private fun getVideoFilePath(): String {
        val folder = File(outputFolder)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val filename = "${System.currentTimeMillis()}.mp4"
        return "${folder.absolutePath}/$filename"
    }

    private fun startRecordTimer() {
        currentRecordTime = 0
        recordTimer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                currentRecordTime += 1000
                Log.d("HEHEHE", "$currentRecordTime")
                if (currentRecordTime >= MAXIMUM_RECORD_TIME + 1000) {
                    videoButton.post { videoButton.resetRecordState() }
                    return
                }
                videoButton.post {
                    videoButton.setProgressWithAnimation(
                            currentRecordTime.toFloat() / MAXIMUM_RECORD_TIME * 100, 1000)
                }
            }
        }
        recordTimer?.scheduleAtFixedRate(task, 1000, 1000)
    }

    private fun stopRecordTimer() {
        recordTimer?.cancel()
    }

    private fun startRecordingVideo() {
        if (cameraDevice == null || !textureView.isAvailable) return

        try {
            closePreviewSession()
            setUpMediaRecorder()
            val texture = textureView.surfaceTexture.apply {
                setDefaultBufferSize(previewSize.width, previewSize.height)
            }

            // Set up Surface for camera preview and MediaRecorder
            val previewSurface = Surface(texture)
            val recorderSurface = mediaRecorder!!.surface
            val surfaces = ArrayList<Surface>().apply {
                add(previewSurface)
                add(recorderSurface)
            }
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(TEMPLATE_RECORD).apply {
                addTarget(previewSurface)
                addTarget(recorderSurface)
            }

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            cameraDevice?.createCaptureSession(surfaces,
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            captureSession = cameraCaptureSession
                            updatePreview()
                            activity?.runOnUiThread {
                                isRecordingVideo = true
                                mediaRecorder?.start()
                                startRecordTimer()
                            }
                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                            if (activity != null) showToast("Failed")
                        }
                    }, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        }

    }

    private fun closePreviewSession() {
        captureSession?.close()
        captureSession = null
    }

    private fun stopRecordingVideo() {
        isRecordingVideo = false
        stopRecordTimer()
        try {
            // Should stopRepeating before stop mediaRecorder
            // This fixed crash on some devices
            captureSession?.apply {
                stopRepeating()
                abortCaptures()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        // Need execute this block after delay time, cause stopRepeating take time to be affected
        withDelay(500) {
            mediaRecorder?.apply {
                stop()
                reset()
            }
            if (currentRecordTime >= 1000) {
                (activity as VideoRecorderActivity).openPreviewVideo(File(nextVideoAbsolutePath))
            }
            nextVideoAbsolutePath = null
            startPreview()
        }
    }

    private fun showToast(message: String) = Toast.makeText(activity, message, LENGTH_SHORT).show()

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private fun chooseVideoSize(choices: Array<Size>) = choices.firstOrNull {
        //it.width == it.height * 4 / 3 &&
                it.width <= 720
        //it.width <= 640
    } ?: choices[choices.size - 1]

    /**
     * Given [choices] of [Size]s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal [Size], or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(
            choices: Array<Size>,
            width: Int,
            height: Int,
            aspectRatio: Size
    ): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val w = aspectRatio.width
        val h = aspectRatio.height
        val bigEnough = choices.filter {
            //it.height == it.width * h / w &&
                    it.width >= width && it.height >= height
        }

        // Pick the smallest of those, assuming we found any
        return if (bigEnough.isNotEmpty()) {
            Collections.min(bigEnough, CompareSizesByArea())
        } else {
            choices[0]
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * [.captureCallback] from both [.lockFocus].
     */
    private fun captureStillPicture() {
        try {
            if (activity == null || cameraDevice == null) return
            val deviceRotation = orientationEventListener.rotation
            val surfaceRotation = when (cameraDirection) {
                CameraCharacteristics.LENS_FACING_FRONT -> INVERSE_ORIENTATIONS.get(deviceRotation)
                else -> DEFAULT_ORIENTATIONS.get(deviceRotation)
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = cameraDevice?.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
                addTarget(imageReader?.surface)

                // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
                // We have to take that into account and rotate JPEG properly.
                // For devices with orientation of 90, we return our mapping from ORIENTATIONS.
                // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
                set(CaptureRequest.JPEG_ORIENTATION,
                        (surfaceRotation + sensorOrientation + 270) % 360)

                // Use the same AE and AF modes as the preview.
//                set(CaptureRequest.CONTROL_AF_MODE,
//                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }?.also { setAutoFlash(it) }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult) {
                    unlockFocus()
                    onCapturedImage()
                }

                override fun onCaptureFailed(session: CameraCaptureSession?, request: CaptureRequest?, failure: CaptureFailure?) {
                    Log.e("Camera", failure.toString())
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder?.build(), captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private fun unlockFocus() {
        try {
            updatePreview()
            // Reset the auto-focus trigger
            /*previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            setAutoFlash(previewRequestBuilder)
            captureSession?.capture(previewRequestBuilder.build(), captureCallback,
                    backgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            //state = Camera2BasicFragment.STATE_PREVIEW
            captureSession?.setRepeatingRequest(previewRequest, captureCallback,
                    backgroundHandler)*/
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        }
    }

    private fun onCapturedImage() {
        (activity as VideoRecorderActivity).openPreviewPicture(file)
    }

    fun changeToPortrait(isInverted: Boolean = false) {
        Log.d("Camera", "Portrait")
        btnSwitchCamera.animate().rotation(if (isInverted) 180f else 0f).setDuration(500).start()
    }

    fun changeToLandscape(isInverted: Boolean = false) {
        Log.d("Camera", "Landscape")
        btnSwitchCamera.animate().rotation(if (isInverted) -90f else 90f).setDuration(500).start()
    }

    inner class OrientationListener(context: Context) : OrientationEventListener(context) {
        private val ROTATION_O = 0

        private val ROTATION_90 = 1
        private val ROTATION_180 = 2
        private val ROTATION_270 = 3
        var rotation = 0

        override fun onOrientationChanged(orientation: Int) {
            if ((orientation < 35 || orientation > 325) && rotation != ROTATION_O) { // PORTRAIT
                rotation = ROTATION_O
                changeToPortrait()
            } else if (orientation in 146..214 && rotation != ROTATION_180) { // REVERSE PORTRAIT
                rotation = ROTATION_180
                changeToPortrait(true)
            } else if (orientation in 56..124 && rotation != ROTATION_270) { // REVERSE LANDSCAPE
                rotation = ROTATION_270
                changeToLandscape(true)
            } else if (orientation in 236..304 && rotation != ROTATION_90) { //LANDSCAPE
                rotation = ROTATION_90
                changeToLandscape()
            }
        }
    }

    companion object {
        fun newInstance(extras: Bundle): Camera2VideoFragment = Camera2VideoFragment().apply {
            arguments = extras
        }
    }

}