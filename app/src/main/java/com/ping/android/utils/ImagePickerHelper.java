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

import static android.app.Activity.RESULT_OK;

/**
 * Created by tuanluong on 11/27/17.
 */

public class ImagePickerHelper {
    private String profileFilePath;
    private Callback callback;

    private Activity activity;
    private Fragment fragment;

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
        this.profileFilePath = filePath;
        return imagePickerHelper;
    }

    public ImagePickerHelper setCallback(Callback callback) {
        this.callback = callback;
        return imagePickerHelper;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.SELECT_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                performCrop(selectedImageUri);
            }
        }

        if (requestCode == Constant.CROP_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (data.hasExtra("data")) {
                    Bitmap selectedBitmap = extras.getParcelable("data");
                    saveImage(selectedBitmap);
                    if (callback != null) {
                        callback.complete(null, new File(profileFilePath));
                    }
                }
            }
        }
    }

    private void saveImage(Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(profileFilePath);
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

    private void performCrop(Uri contentUri) {
        try {
            File file = getMediaFileFromUri(getContext(), contentUri);
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
        if ("content".equals(uri.getScheme())) {
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
        } else if ("file".equals(uri.getScheme())) {
            return new File(uri.getPath());
        }
        return null;
    }

    public void openPicker() {
        if (!isPermissionGrant()) {
            requestPermission();
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

    private Context getContext() {
        if (activity != null) {
            return activity;
        } else if (fragment != null) {
            return fragment.getContext();
        }
        throw new NullPointerException("Activity and Fragment is null");
    }

    // region Permissions

    private boolean isPermissionGrant() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        if (activity != null) {
            activity.requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 123);
        } else if (fragment != null) {
            fragment.requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 123);
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
                    openPicker();
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
}
