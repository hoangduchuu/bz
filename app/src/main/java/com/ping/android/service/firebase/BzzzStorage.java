package com.ping.android.service.firebase;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;

import java.io.File;

/**
 * Created by tuanluong on 11/29/17.
 */

public class BzzzStorage {
    FirebaseStorage storage;

    public BzzzStorage() {
        storage = FirebaseStorage.getInstance();
    }

    public void uploadImageForConversation(String conversationId, @NonNull File file, @NonNull Callback callback) {
        String conversationImagePath = "conversations/" + conversationId + "/" + System.currentTimeMillis() + file.getName();
        StorageReference photoRef = storage.getReferenceFromUrl(Constant.URL_STORAGE_REFERENCE).child(conversationImagePath);
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(file));
        uploadTask
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.complete(e);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    String downloadUrl = Constant.URL_STORAGE_REFERENCE + "/" + taskSnapshot.getMetadata().getPath();
                    callback.complete(null, downloadUrl);
                });
    }
}