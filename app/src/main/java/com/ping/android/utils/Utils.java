package com.ping.android.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.ping.android.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.exifinterface.media.ExifInterface;

public class Utils {
    public static byte[] getImageData(String imagePath, int reqWidth, int reqHeight) {
        // Decode with inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        try {
            bitmap = rotateImageIfRequired(bitmap, imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        return byteArray;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static File getMediaFileFromUri(Context context, Uri uri) {
        if (uri.getScheme().equals("file")) {
            return new File(uri.getPath());
        }
        String selection = null;
        String[] selectionArgs = null;
        String[] projection;
        String column = null;
        Uri contentUri = uri;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                column = MediaStore.Images.Media.DATA;
            } else if (isMediaDocument(uri)) {
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    column = MediaStore.Images.Media.DATA;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    column = MediaStore.Video.Media.DATA;
                }
                selection = MediaStore.Images.Media._ID + "=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }

        } else {
            column = MediaStore.Images.Media.DATA;
        }
        projection = new String[]{column};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                String path = cursor.getString(columnIndex);
                if (!TextUtils.isEmpty(path)) {
                    return new File(path);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static void saveImage(String filePath, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e1) {
            }
        }
    }

    public static byte[] getImageData(Bitmap sourceBitmap, int reqWidth, int reqHeight) {
        //Bitmap bitmap = Bitmap.createScaledBitmap(sourceBitmap, reqWidth, reqHeight, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteArray = stream.toByteArray();
        sourceBitmap.recycle();
//        sourceBitmap.recycle();
        return byteArray;
    }

    public static Bitmap decodeSampledBitmap(Context context, Uri photoUri, int reqWidth, int reqHeight) {
        try {
            // Decode with inJustDecodeBounds = true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream imageStream = context.getContentResolver().openInputStream(photoUri);
            BitmapFactory.decodeStream(imageStream, null, options);
            imageStream.close();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize
            options.inJustDecodeBounds = false;
            imageStream = context.getContentResolver().openInputStream(photoUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);
            return rotateImageIfRequired(context, bitmap, photoUri);
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }


    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keep boths
            // height and width larger then the requested height and width
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, String selectedImage) throws IOException {
        ExifInterface ei = new ExifInterface(selectedImage);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static void saveFile(Context context, File file, Uri uri) {
        file.getParentFile().mkdirs();
        OutputStream os = null;
        InputStream is = null;
        try {
            file.createNewFile();
            os = new FileOutputStream(file);
            is = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                try {
                    os.write(buffer, 0, length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
