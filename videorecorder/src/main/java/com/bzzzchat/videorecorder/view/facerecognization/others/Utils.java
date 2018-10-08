package com.bzzzchat.videorecorder.view.facerecognization.others;

/**
 * Created by Ezequiel Adrian on 24/02/2017.
 */

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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Utils {

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
}
