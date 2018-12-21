package com.bzzzchat.videorecorder.view.facerecognition.others;

/**
 * Created by Ezequiel Adrian on 24/02/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.FaceDetector;
import android.media.Image;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.bzzzchat.videorecorder.view.facerecognition.Configs;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
//
//import org.bytedeco.javacpp.opencv_core;
//import org.bytedeco.javacpp.opencv_imgproc;
//import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Utils {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static float getScreenRatio(Context c) {
        DisplayMetrics metrics = c.getResources().getDisplayMetrics();
        return ((float) metrics.heightPixels / (float) metrics.widthPixels);
    }

    public static int getScreenRotation(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getRotation();
    }

    public static int distancePointsF(PointF p1, PointF p2) {
        return (int) Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public static PointF middlePoint(PointF p1, PointF p2) {
        if (p1 == null || p2 == null)
            return null;
        return new PointF((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    public static Size[] sizeToSize(android.util.Size[] sizes) {
        Size[] size = new Size[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            size[i] = new Size(sizes[i].getWidth(), sizes[i].getHeight());
        }
        return size;
    }

    public static Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width) ? height - (height - width) : height;
        int cropW = (width - height) / 2;
        cropW = (cropW < 0) ? 0 : cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0) ? 0 : cropH;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);

        return cropImg;
    }

    public static Bitmap getProcessedImage(Frame frame, Face face) {
        int rotationAngle = 0;
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

        switch (frame.getMetadata().getRotation()) {
            case 0:
                break;
            case 1:
                rotationAngle = 90;
                break;
            case 2:
                rotationAngle = 180;
                break;
            case 3:
                rotationAngle = 270;
                break;
            default:
                rotationAngle = 0;
        }


        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        return crop(rotatedBitmap, face);
    }

    public static Bitmap rotateImage(Bitmap img, int degree, int length) {
        Matrix matrix = new Matrix();
        float scale = img.getWidth() > img.getHeight()? (float)length/img.getWidth() : (float)length/img.getHeight();
//        int newWidth = img.getWidth() > img.getHeight() ? length : (int)(length* scale);
//        int newHeight = img.getWidth() > img.getHeight() ? (int)(length*scale) : length;
        matrix.postScale(scale, scale);
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(),img.getHeight(), matrix, true);
//        img.recycle();
        return rotatedImg;
    }

    public static int getRotationCompensation(String cameraId, Activity activity)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(activity.CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.

        return rotationCompensation;
    }

    public static Bitmap getProcessedImage(byte[] bytes, int width, int height, float rotationAngle) {
        YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    public static Bitmap crop(Bitmap bitmap, Face face) {
        try {
            float left = face.getPosition().x;
            float top = face.getPosition().y;
            float width = face.getWidth();
            float height = face.getHeight();

            if (left < 0) {
                left = 0;
            }
            if (left + width > bitmap.getWidth()) {
                width = bitmap.getWidth() - left - 1;
            }
            if (top < 0) {
                top = 0;
            }
            if (top + height > bitmap.getHeight()) {
                height = bitmap.getHeight() - top - 1;
            }

            return Bitmap.createBitmap(bitmap, (int) left, (int) top, (int) width, (int) height);
        } catch (Exception e) {
            return Bitmap.createBitmap(bitmap, 0, 0, 1, 1);
        }
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static void moveFile(File file, File dir) throws IOException {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }

    public static Bitmap convertTo565(final Bitmap origin) {

        if (origin == null) {

            return null;
        }

        Bitmap bitmap = origin;

        if (bitmap.getConfig() != Bitmap.Config.RGB_565) {

            bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        }

        if ((bitmap.getWidth() & 0x1) != 0) {

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() & ~0x1,
                    bitmap.getHeight());
        }

        return bitmap;
    }

    public static void detectFace(Bitmap bitmap) {
        FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 1);
        FaceDetector.Face[] facesAndroid = new FaceDetector.Face[1];
        if (faceDetector.findFaces(bitmap, facesAndroid) > 0){
            Rect[] faces = new Rect[facesAndroid.length];
            for (int i=0; i<facesAndroid.length; i++){
                PointF pointF = new PointF();
                facesAndroid[i].getMidPoint(pointF);
                int xWidth = (int) (1.34 * facesAndroid[i].eyesDistance());
                int yWidth = (int) (1.12 * facesAndroid[i].eyesDistance());
                int dist = (int) (2.77 * facesAndroid[i].eyesDistance());
                Rect face = new Rect((int) pointF.x - xWidth, (int) pointF.y - yWidth, dist, dist);
                faces[i] = face;
            }
        }
    }

    public static Bitmap getBitmapFromImage(Image image){
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        float scale = (float)image.getHeight()/image.getWidth();

        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    /**
     * The same with above but we scale 270 degrees
     * @param image
     * @return
     */
    public static Bitmap getBitmapFromImageAndScale(Image image){
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        matrix.postScale(-1,1);
        Bitmap scaledBitmap = Bitmap.createBitmap
                (temp,0,0,temp.getWidth(),temp.getHeight(), matrix,true);
        return getResizedBitmap(scaledBitmap,500);
    }


    /**
     * Scale the bitmap smaler, because ImageView if so slow if the bitmap size too large
     * @param bitmapInputSource
     * @return bitmap
     */
    public static Bitmap scaleBitMap(Bitmap bitmapInputSource){
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        matrix.postScale(-1,1);
        Bitmap bitmap = Bitmap.createBitmap
                (bitmapInputSource,0,0,bitmapInputSource.getWidth(),bitmapInputSource.getHeight(), matrix,true);
        return getResizedBitmap(bitmap, 500);
    }


    /**
     * helper for bitmap
     * @param
     * @param
     * @return
     */




    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (bitmapRatio * maxSize);
        } else {
            height = maxSize;
            width = (int) (maxSize * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap getFaceFromBitmap(Bitmap bitmap, FirebaseVisionFace face) {
        Rect rect = face.getBoundingBox();
        return getFaceFromBitmap(bitmap, rect);
    }

    public static Bitmap getFaceFromBitmap(Bitmap bitmap, Face face) {
        PointF position = face.getPosition();
        Rect rect = new Rect((int)position.x, (int)position.y, (int)(position.x + face.getWidth()),
                (int)(position.y + face.getHeight()));
        return getFaceFromBitmap(bitmap, rect);
    }

    public static Bitmap getFaceFromBitmap(Bitmap bitmap, Rect rect){
        int largestDimension = rect.width() > rect.height() ? rect.width() : rect.height();
        int diff = Math.abs(rect.width() - rect.height()) / 4;
        Rect sourceRect = new Rect(rect.centerX() - largestDimension / 2 - diff,
                rect.centerY() - largestDimension / 2 - diff,
                rect.centerX() + largestDimension / 2 + diff,
                rect.centerY() + largestDimension / 2 + diff);
        //Rect sourceRect = new Rect(rect.left, rect.bottom - smallestDimension, rect.right, rect.bottom);
        Rect destRect = new Rect(0, 0, largestDimension, largestDimension);
        //Bitmap faceBitmap = Bitmap.createBitmap(finalPicture, rect.left, rect.top, rect.width(), rect.height());
        Bitmap faceBitmap = Bitmap.createBitmap(largestDimension, largestDimension, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(faceBitmap);
        canvas.drawBitmap(bitmap, sourceRect, destRect, null);
        int finalWidth = Configs.modelDimension;
        faceBitmap = Bitmap.createScaledBitmap(faceBitmap, finalWidth, finalWidth, true);
        faceBitmap = Utils.convertTo565(faceBitmap);
        return faceBitmap;
    }

    public static void saveBitmap(Bitmap source, String filePath) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            source.compress(Bitmap.CompressFormat.PNG, 95, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
