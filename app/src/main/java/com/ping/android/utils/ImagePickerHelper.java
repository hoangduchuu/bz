package com.ping.android.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.ping.android.activity.BuildConfig;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by tuanluong on 11/27/17.
 */

public class ImagePickerHelper {
    private static final int TAKE_PICTURE_REQUEST_CODE = 123;
    private static final int MAX_DIMENSION = 1200;
    private static final int MAX_THUMB_DIMENSION = 250;

    private String filePath;
    private String thumbnailFilePath;
    private Callback callback;

    private Activity activity;
    private Fragment fragment;

    private boolean isOpeningCamera = false;
    private boolean isScale = true;
    private boolean isCrop = false;
    private boolean isGenerateThumbnail = false;

    private static ImagePickerHelper imagePickerHelper;

    private ImagePickerHelper(Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    private ImagePickerHelper(Fragment fragment) {
        this.activity = null;
        this.fragment = fragment;
    }

    public static ImagePickerHelper from(Activity activity) {
        imagePickerHelper = new ImagePickerHelper(activity);
        return imagePickerHelper;
    }

    public static ImagePickerHelper from(Fragment fragment) {
        imagePickerHelper = new ImagePickerHelper(fragment);
        return imagePickerHelper;
    }

    public ImagePickerHelper setFilePath(String filePath) {
        this.filePath = filePath;
        return imagePickerHelper;
    }

    public ImagePickerHelper setCallback(Callback callback) {
        this.callback = callback;
        return imagePickerHelper;
    }

    public ImagePickerHelper setScale(boolean isScale) {
        this.isScale = isScale;
        return imagePickerHelper;
    }

    public ImagePickerHelper setCrop(boolean isCrop) {
        this.isCrop = isCrop;
        return imagePickerHelper;
    }

    public ImagePickerHelper setGenerateThumbnail(boolean isGenerateThumbnail) {
        this.isGenerateThumbnail = isGenerateThumbnail;
        return imagePickerHelper;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.SELECT_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                File file = getMediaFileFromUri(getContext(), selectedImageUri);
                if (isCrop) {
                    performCrop(file);
                } else {
                    tuningFinalImage(file);
                }
            }
        } else if (requestCode == TAKE_PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File file = new File(getFilePath());
                if (file.exists()) {
                    tuningFinalImage(file);
                }
            }
        } else if (requestCode == Constant.CROP_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (data.hasExtra("data")) {
                    Bitmap selectedBitmap = extras.getParcelable("data");
                    saveImage(getFilePath(), selectedBitmap);
                    if (callback != null) {
                        callback.complete(null, new File(filePath));
                    }
                }
            }
        }
    }

    private void tuningFinalImage(File file) {
        File selectedImage = null;
        File thumbnailFile = null;
        String originFileName = file.getName();
        if (isScale) {
            String filePath = getCacheFolder() + File.separator + originFileName;
            Bitmap scaleBitmap = decodeSampledBitmap(file, MAX_DIMENSION, MAX_DIMENSION);
            saveImage(filePath, scaleBitmap);
            selectedImage = new File(filePath);
        }
        if (isGenerateThumbnail) {
            String filePath = getCacheFolder() + File.separator + "thumbnail_" + originFileName;
            Bitmap thumbnail = decodeSampledBitmap(file, MAX_THUMB_DIMENSION, MAX_THUMB_DIMENSION);
            saveImage(filePath, thumbnail);
            thumbnailFile = new File(filePath);
        }
        if (callback != null) {
            callback.complete(null, selectedImage, thumbnailFile);
        }
    }

    private void performCrop(File file) {
        try {
            if (file == null) {
                return;
            }
            Uri photoURI;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        file);
                getContext().grantUriPermission("com.android.camera", photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                photoURI = Uri.fromFile(file);
            }
            //Start Crop Activity
            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            cropIntent.setDataAndType(photoURI, "image/*");
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);

            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            if (activity != null) {
                activity.startActivityForResult(cropIntent, Constant.CROP_IMAGE_REQUEST);
            } else if (fragment != null) {
                fragment.startActivityForResult(cropIntent, Constant.CROP_IMAGE_REQUEST);
            }
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException exception) {
        }
    }

    public void openPicker() {
        isOpeningCamera = false;
        if (!isPermissionGrant(false)) {
            requestPermissions(false);
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (activity != null) {
            activity.startActivityForResult(Intent.createChooser(intent, "Get photo"), Constant.SELECT_IMAGE_REQUEST);
        } else if (fragment != null) {
            fragment.startActivityForResult(Intent.createChooser(intent, "Get photo"), Constant.SELECT_IMAGE_REQUEST);
        }
    }

    public void openCamera() {
        isOpeningCamera = true;
        if (!isPermissionGrant(true)) {
            requestPermissions(true);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getFilePath())));
        if (activity != null) {
            activity.startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);
        } else if (fragment != null) {
            fragment.startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);
        }
    }

    private String getFilePath() {
        if (TextUtils.isEmpty(filePath)) {
            filePath = getCacheFolder() +
                    File.separator + System.currentTimeMillis() + ".png";
        }
        return filePath;
    }

    private String getThumbnailFilePath() {
        return getCacheFolder() + File.separator +
                "cache" + File.separator +
                "thumbnail_" + System.currentTimeMillis() + ".png";
    }

    private String getCacheFolder() {
        File cache = getContext().getExternalCacheDir();
        if (cache == null) {
            cache = getContext().getCacheDir();
        }
        return cache.getAbsolutePath();
    }

    private Context getContext() {
        if (activity != null) {
            return activity;
        } else if (fragment != null) {
            return fragment.getContext();
        }
        throw new NullPointerException("Activity and Fragment is null");
    }

    // region Permissions

    private boolean isPermissionGrant(boolean isCamera) {
        boolean isPermissionGrant = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (isCamera && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGrant = false;
        }
        return isPermissionGrant;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(boolean isCamera) {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (isCamera) {
            permissions.add(Manifest.permission.CAMERA);
        }
        String[] array = permissions.toArray(new String[permissions.size()]);
        if (activity != null) {
            activity.requestPermissions(array, 123);
        } else if (fragment != null) {
            fragment.requestPermissions(array, 123);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (isOpeningCamera) {
                        openCamera();
                    } else {
                        openPicker();
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    if (callback != null) {
                        callback.complete(new IllegalStateException("Permissions is not granted"));
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // endregion

    // region utilities methods

    private File getMediaFileFromUri(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        String[] projection;
        String column = null;
        Uri contentUri = uri;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
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

    private void saveImage(String filePath, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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

    public static Bitmap decodeSampledBitmap(File file, int reqWidth, int reqHeight) {
        // Decode with inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    // endregion
}
