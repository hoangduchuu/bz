package com.bzzzchat.videorecorder.view.facerecognition;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bzzzchat.videorecorder.R;
import com.bzzzchat.videorecorder.view.ShowFaceFragment;
import com.bzzzchat.videorecorder.view.ShowFaceFragmentListener;
import com.bzzzchat.videorecorder.view.custom.ConfirmPictureButton;
import com.bzzzchat.videorecorder.view.custom.CustomRecordButton;
import com.bzzzchat.videorecorder.view.facerecognition.others.Camera2Source;
import com.bzzzchat.videorecorder.view.facerecognition.others.CameraSourcePreview;
import com.bzzzchat.videorecorder.view.facerecognition.others.FaceGraphic;
import com.bzzzchat.videorecorder.view.facerecognition.others.GraphicOverlay;
import com.bzzzchat.videorecorder.view.facerecognition.others.Utils;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class FaceTrainingActivity extends AppCompatActivity implements ShowFaceFragmentListener {

    private static final String TAG = "Ezequiel Adrian Camera";
    private Context context;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;
    private ImageView ivAutoFocus;

    // CAMERA VERSION TWO DECLARATIONS
    private Camera2Source mCamera2Source = null;

    // COMMON TO BOTH CAMERAS
    private CameraSourcePreview mPreview;
    private FaceDetector previewFaceDetector = null;
    private GraphicOverlay mGraphicOverlay;
    private FaceGraphic mFaceGraphic;
    private boolean wasActivityResumed = false;
    private CustomRecordButton takePictureButton;
    // DEFAULT CAMERA BEING OPENED
    private boolean usingFrontCamera = true;

    private ConfirmPictureButton confirmPictureButton;

    FaceDetector mFaceDetector;
    Bitmap mCapturedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_training);
        context = getApplicationContext();
        takePictureButton = findViewById(R.id.btn_takepicture);
        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.faceOverlay);
        ivAutoFocus = findViewById(R.id.ivAutoFocus);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.btnSwitchCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCameraSwitch();
            }
        });
        confirmPictureButton = findViewById(R.id.btnConfirm);

        File trainingFolder = new File(FaceRecognition.Companion.getInstance(context).getTrainingFolder());
        trainingFolder.delete();

        requestPermissionThenOpenCamera();

        FaceDetector faceDetector = new FaceDetector.Builder(context).setMode(FaceDetector.FAST_MODE)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setTrackingEnabled(false)
                .setProminentFaceOnly(true)
                .setMinFaceSize(0.25f)
                .build();
        mFaceDetector = faceDetector;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureButton.setEnabled(false);
                if (mCamera2Source != null)
                    mCamera2Source.takePicture(camera2SourceShutterCallback, camera2SourcePictureCallback);

            }
        });

        mPreview.setOnTouchListener(CameraPreviewTouchListener);
    }

    private void handleCameraSwitch() {
        stopCameraSource();
        usingFrontCamera = !usingFrontCamera;
        if (usingFrontCamera){
            createCameraSourceFront();
        }else{
            createCameraSourceBack();
        }
    }

    private void showErrorTraining(String title, String msg) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // handle on Ok CLick
                        showConfirmImage(null); // recapture trigger
                    }
                })
                .create();
        dialog.show();
    }

    private FaceTrainingActivity that = this;

    final Camera2Source.ShutterCallback camera2SourceShutterCallback = new Camera2Source.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.d(TAG, "Shutter Callback for CAMERA2");
        }
    };

    final Camera2Source.PictureCallback camera2SourcePictureCallback = new Camera2Source.PictureCallback() {
        @Override
        public void onPictureTaken(Image image) {
            int rotation = 0;
            try {
                rotation = Utils.getRotationCompensation(mCamera2Source.getCameraId(), that);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = Utils.getBitmapFromImage(image);
            final Bitmap finalPicture = Utils.rotateImage(bitmap, rotation, 768);
            mCapturedBitmap = Utils.getResizedBitmap(finalPicture, 384);
            // TODO confirm image
            showConfirmImage(finalPicture);
        }
    };

    /**
     * call this function to execute FaceRecognition processing
     *
     * @param faceDetector
     * @param capturedBitmap
     */
    private void onUserConfirmedPicture(FaceDetector faceDetector, Bitmap capturedBitmap) {

        SparseArray<Face> detectedFaces = faceDetector.detect(new Frame.Builder().setBitmap(capturedBitmap).build());

        if (detectedFaces.size() == 0 || detectedFaces.size() > 1) {
            //no FACE DETECTEd
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    takePictureButton.setEnabled(true);
                    showErrorTraining(context.getString(R.string.no_face_detect_title), context.getString(R.string.no_face_detect_msg));
                    takePictureButton.setVisibility(View.VISIBLE);
                    confirmPictureButton.setVisibility(View.GONE);

                }
            });
        } else {
            Face face = detectedFaces.valueAt(0);
            Bitmap bitmap = Utils.getFaceFromBitmap(capturedBitmap, face);
            String fileName = "1-user_1.png";
            File file = new File(FaceRecognition.Companion.getInstance(that).getTrainingFolder(), fileName);
            Utils.saveBitmap(bitmap, file.getAbsolutePath());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    that.setResult(Activity.RESULT_OK);
                    that.finish();
                }
            });

        }
    }

    /**
     * this function show the image, which user just capture to confirm before execute  onUserConfirmedPicture() function
     *
     * @param image
     */
    @SuppressLint("ResourceType")
    private void showConfirmImage(Bitmap image) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit,
                R.animator.fragment_slide_right_enter,
                R.animator.fragment_slide_right_exit);
        FragmentManager fm = getFragmentManager();

        ShowFaceFragment fragment = (ShowFaceFragment) fm.findFragmentByTag("tag");
        if (fragment == null) {
            fragment = new ShowFaceFragment().newInstance(image);
            ft.add(R.id.training_container, fragment, "tag");
        } else {
            ft.remove(fragment);
        }
        ft.commit();
    }

    private void requestPermissionThenOpenCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                createCameraSourceFront();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void createCameraSourceFront() {
        previewFaceDetector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();

        if (previewFaceDetector.isOperational()) {
            previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
        } else {
            Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }

        mCamera2Source = new Camera2Source.Builder(context, previewFaceDetector)
                .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                .setFacing(Camera2Source.CAMERA_FACING_FRONT)
                .build();

        startCameraSource();

    }

    private void createCameraSourceBack() {
        previewFaceDetector = new FaceDetector.Builder(context)
//                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
//                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();

        if (previewFaceDetector.isOperational()) {
            previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
        } else {
            Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }

        mCamera2Source = new Camera2Source.Builder(context, previewFaceDetector)
                .setFocusMode(Camera2Source.CAMERA_AF_AUTO).setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                .setFacing(Camera2Source.CAMERA_FACING_BACK)
                .build();
        startCameraSource();

    }

    private void startCameraSource() {
        if (mCamera2Source != null) {
            try {
                mPreview.start(mCamera2Source, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source 2.", e);
                mCamera2Source.release();
                mCamera2Source = null;
            }
        }
    }

    private void stopCameraSource() {
        mPreview.stop();
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, context);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mFaceGraphic.goneFace();
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mFaceGraphic.goneFace();
            mOverlay.remove(mFaceGraphic);
        }
    }

    private final CameraSourcePreview.OnTouchListener CameraPreviewTouchListener = new CameraSourcePreview.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent pEvent) {
            v.onTouchEvent(pEvent);
            if (pEvent.getAction() == MotionEvent.ACTION_DOWN) {
                int autoFocusX = (int) (pEvent.getX() - Utils.dpToPx(60) / 2);
                int autoFocusY = (int) (pEvent.getY() - Utils.dpToPx(60) / 2);
                ivAutoFocus.setTranslationX(autoFocusX);
                ivAutoFocus.setTranslationY(autoFocusY);
                ivAutoFocus.setVisibility(View.VISIBLE);
                ivAutoFocus.bringToFront();
                if (mCamera2Source != null) {
                    mCamera2Source.autoFocus(new Camera2Source.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ivAutoFocus.setVisibility(View.GONE);
                                }
                            });
                        }
                    }, pEvent, v.getWidth(), v.getHeight());
                } else {
                    ivAutoFocus.setVisibility(View.GONE);
                }
            }
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera();
            } else {
                Toast.makeText(FaceTrainingActivity.this, "CAMERA PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera();
            } else {
                Toast.makeText(FaceTrainingActivity.this, "STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasActivityResumed){
            if (usingFrontCamera) {
                createCameraSourceFront();
            } else {
                createCameraSourceBack();
            }
        } else {
            startCameraSource();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasActivityResumed = true;
        stopCameraSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCameraSource();
        if (previewFaceDetector != null) {
            previewFaceDetector.release();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ShowFaceFragment) {
            ((ShowFaceFragment) fragment).setListener(this);
        }
    }

    @Override
    public void onBackToReCaptureButtonClicked() {
        showConfirmImage(null);
        takePictureButton.setEnabled(true);
        confirmPictureButton.setVisibility(View.GONE);
        takePictureButton.setVisibility(View.VISIBLE);
    }

    /**
     * the callback from user profile fragment when picture is showing.
     * we show the confirmButton instead of record button
     */
    @Override
    public void onFragmentOpening() {
        confirmPictureButton.setVisibility(View.VISIBLE);
        takePictureButton.setVisibility(View.GONE);

    }

    /**
     * the click handler
     *
     * @param view
     */
    public void onConfirmClicked(View view) {
        onUserConfirmedPicture(mFaceDetector, mCapturedBitmap);
    }
}