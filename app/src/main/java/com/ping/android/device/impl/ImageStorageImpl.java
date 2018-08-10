package com.ping.android.device.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bzzzchat.configuration.GlideApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.device.ImageStorage;
import com.ping.android.utils.BitmapEncode;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.Log;
import com.ping.android.utils.UiUtils;

import java.io.File;
import java.io.FileOutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ImageStorageImpl implements ImageStorage {
    private static String CONVERSATION_CACHE_FOLDER = "conversations";
    private static String CONVERSATION_THUMB_CACHE_FOLDER = "conversations";
    private File conversationFolder;
    private File thumbFolder;

    @Inject
    public ImageStorageImpl(Context context) {
        initFolders(context);
    }

    private void initFolders(Context context) {
        File cacheDir = context.getExternalCacheDir();
        conversationFolder = new File(cacheDir, CONVERSATION_CACHE_FOLDER);
        if (!conversationFolder.exists()) {
            if (!conversationFolder.mkdir()) {
                Log.e("Can not create cache folder for conversation");
            }
        }
        thumbFolder = new File(conversationFolder, CONVERSATION_THUMB_CACHE_FOLDER);
        if (!thumbFolder.exists()) {
            if (!thumbFolder.mkdirs()) {
                Log.e("Can not create cache folder for thumb");
            }
        }
    }

    @Override
    public void loadImage(String path, ImageView view, boolean isMask) {
        String imageName = UiUtils.getFileName(path);
        File cachedFile = new File(conversationFolder, imageName);
        if (cachedFile.exists()) {
            GlideApp.with(view.getContext())
                    .load(cachedFile)
                    .transform(new BitmapEncode(isMask))
                    .into(view);
        } else {
            SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap> () {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    //saveImage(cachedFile, resource);
                    if (isMask) {
                        Bitmap puzzle = CommonMethod.puzzleImage(resource, 3);
                        view.setImageBitmap(puzzle);
                    } else {
                        view.setImageBitmap(resource);
                    }
                }
            };
            if (path.startsWith("gs://")) {
                StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(path);
                GlideApp.with(view.getContext())
                        .asBitmap()
                        .load(gsReference)
                        .override(512)
                        .skipMemoryCache(false)
                        .into(target);
            }
        }
    }

    @Override
    public void loadThumb(String path, ImageView view) {

    }

    private static void saveImage(File filePath, Bitmap bitmap) {
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
}
