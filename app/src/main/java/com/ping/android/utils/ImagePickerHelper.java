package com.ping.android.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.BuildConfig;
import com.ping.android.presentation.view.cameraview.CameraActivity;
import com.ping.android.utils.configs.Constant;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

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
    private ImagePickerListener listener;

    private PublishSubject<File> imageSubject = PublishSubject.create();

    private Activity activity;
    private Fragment fragment;

    private boolean isOpeningCamera = false;
    private boolean isScale = true;
    private boolean isCrop = false;
    private boolean isGenerateThumbnail = false;

    private BaseView view;

    private ImagePickerHelper(Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    private ImagePickerHelper(Fragment fragment) {
        this.activity = null;
        this.fragment = fragment;
    }

    public static ImagePickerHelper from(Activity activity) {
        ImagePickerHelper imagePickerHelper = new ImagePickerHelper(activity);
        return imagePickerHelper;
    }

    public static ImagePickerHelper from(Fragment fragment) {
        ImagePickerHelper imagePickerHelper = new ImagePickerHelper(fragment);
        return imagePickerHelper;
    }

    public ImagePickerHelper setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public ImagePickerHelper setListener(ImagePickerListener listener) {
        this.listener = listener;
        return this;
    }

    public ImagePickerHelper setScale(boolean isScale) {
        this.isScale = isScale;
        return this;
    }

    public ImagePickerHelper setCrop(boolean isCrop) {
        this.isCrop = isCrop;
        return this;
    }

    public ImagePickerHelper setView(BaseView view) {
        this.view = view;
        return this;
    }

    public ImagePickerHelper setGenerateThumbnail(boolean isGenerateThumbnail) {
        this.isGenerateThumbnail = isGenerateThumbnail;
        return this;
    }

    private static Uri getUriFromFile(Context context, File file) {
        Uri photoUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            photoUri = FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            context.grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            photoUri = Uri.fromFile(file);
        }
        return photoUri;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.SELECT_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                File file = getMediaFileFromUri(getContext(), selectedImageUri);
                if (file == null) return;
                if (isCrop) {
                    Uri photoUri = getUriFromFile(getContext(), file);
                    performCrop(photoUri);
                } else {
                    if (listener != null) {
                        listener.onImageReceived(file);
                    }
                    tuningFinalImage(file, file.getName());
                }
            }
        } else if (requestCode == TAKE_PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //File file = new File(getFilePath());
                final File file = new File(data.getStringExtra("data"));
                if (file.exists()) {
                    if (listener != null) {
                        listener.onImageReceived(file);
                    }
                    if (isCrop) {
                        Uri photoUri = getUriFromFile(getContext(), file);
                        performCrop(photoUri);
                    } else {
                        tuningFinalImage(file, file.getName());
                    }
                }
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedUri = result.getUri();
                if (listener != null) {
                    Bitmap scaleBitmap = decodeSampledBitmap(getContext(), croppedUri, MAX_DIMENSION, MAX_DIMENSION);
                    saveImage(getFilePath(), scaleBitmap);
                    listener.onFinalImage(new File(getFilePath()));
                } else {
                    view.showLoading();
                    Disposable disposable = saveFile(croppedUri)
                            .subscribeOn(Schedulers.io())
                            .subscribe(file -> {
                                view.hideLoading();
                                imageSubject.onNext(file);
                            });
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                imageSubject.onError(error);
            }
        }
    }

    private Observable<File> saveFile(Uri croppedUri) {
        return Observable.create(e -> {
            Bitmap scaleBitmap = decodeSampledBitmap(getContext(), croppedUri, MAX_DIMENSION, MAX_DIMENSION);
            saveImage(getFilePath(), scaleBitmap);
            e.onNext(new File(getFilePath()));
        });
    }

    private static class TuningImage extends AsyncTask<Void, Void, List<File>> {

        private final File originalFile;
        private final String fileName;
        private final WeakReference<Context> context;
        private final boolean isGenerateThumbnail;
        private ImagePickerListener listener;

        public TuningImage(Context context, File originalFile, String fileName, boolean isGenerateThumbnail) {
            this.context = new WeakReference<>(context);
            this.originalFile = originalFile;
            this.fileName = fileName;
            this.isGenerateThumbnail = isGenerateThumbnail;
        }

        public void setListener(ImagePickerListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<File> doInBackground(Void... voids) {
            List<File> ret = new ArrayList<>();
            ret.add(originalFile);
//            if (isScale) {
//                String filePath = getCacheFolder() + File.separator + fileName;
//                Bitmap scaleBitmap = decodeSampledBitmap(getContext(), photoUri, MAX_DIMENSION, MAX_DIMENSION);
//                if (scaleBitmap != null) {
//                    saveImage(filePath, scaleBitmap);
//                    ret.add(new File(filePath));
//                }
//            }
            Context context = this.context.get();
            if (context == null) return new ArrayList<>();

            Uri photoUri = getUriFromFile(context, originalFile);
            if (isGenerateThumbnail) {
                String filePath = getCacheFolder(context) + File.separator + "thumbnail_" + fileName;
                Bitmap thumbnail = decodeSampledBitmap(context, photoUri, MAX_THUMB_DIMENSION, MAX_THUMB_DIMENSION);
                if (thumbnail != null) {
                    saveImage(filePath, thumbnail);
                    ret.add(new File(filePath));
                }
            }
            return ret;
        }

        @Override
        protected void onPostExecute(List<File> results) {
            super.onPostExecute(results);
            File selectedImage = results.size() > 0 ? results.get(0) : null;
            File thumbnailFile = results.size() > 1 ? results.get(1) : null;
            if (listener != null) {
                listener.onFinalImage(selectedImage, thumbnailFile);
            }
        }
    }

    private void tuningFinalImage(File file, String fileName) {
        TuningImage task = new TuningImage(getContext(), file, fileName, isGenerateThumbnail);
        task.setListener(listener);
        task.execute();
    }

    private void performCrop(Uri photoUri) {
        if (activity != null) {
            CropImage.activity(photoUri)
                    //.setAspectRatio(1, 1)
                    .start(activity);
        } else {
            CropImage.activity(photoUri)
                    //.setAspectRatio(1, 1)
                    .start(getContext(), fragment);
        }
    }

    public Observable<File> getFileObservable() {
        return imageSubject.share();
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
        Intent intent = new Intent(getContext(), CameraActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CameraActivity.EXTRA_OUTPUT, getFilePath());
        bundle.putInt(CameraActivity.EXTRA_MAX_HEIGHT, MAX_DIMENSION);
        bundle.putInt(CameraActivity.EXTRA_MAX_WIDTH, MAX_DIMENSION);
        intent.putExtras(bundle);
        if (activity != null) {
            activity.startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);
        } else if (fragment != null) {
            fragment.startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);
        }
    }

    private String getFilePath() {
        if (TextUtils.isEmpty(filePath)) {
            filePath = getCacheFolder(getContext()) +
                    File.separator + System.currentTimeMillis() + ".jpeg";
        }
        return filePath;
    }

    private String getThumbnailFilePath() {
        return getCacheFolder(getContext()) + File.separator +
                "cache" + File.separator +
                "thumbnail_" + System.currentTimeMillis() + ".jpeg";
    }

    private static String getCacheFolder(Context context) {
        File cache = context.getExternalCacheDir();
        if (cache == null) {
            cache = context.getCacheDir();
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
                    boolean isGrant = true;
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            isGrant = false;
                            break;
                        }
                    }
                    if (!isGrant) {
                        // TODO show error
                        return;
                    }
                    if (isOpeningCamera) {
                        openCamera();
                    } else {
                        openPicker();
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO show error
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // endregion

    // region utilities methods

    private File getMediaFileFromUri(Context context, Uri uri) {
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

    private static void saveImage(String filePath, Bitmap bitmap) {
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
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
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

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    // endregion

    public interface ImagePickerListener {
        void onImageReceived(File file);
        void onFinalImage(File... files);
    }
}
