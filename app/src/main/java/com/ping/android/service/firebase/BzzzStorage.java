package com.ping.android.service.firebase;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
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
        StorageReference photoRef = storage.getReference().child(conversationImagePath);
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(file));
        uploadTask
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.complete(e);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    String downloadUrl = getStorageRoot() + "/" + taskSnapshot.getMetadata().getPath();
                    callback.complete(null, downloadUrl);
                });
    }

    public void uploadGroupAvatar(String groupId, File file, Callback callback) {
        String fileName = System.currentTimeMillis() + file.getName();
        String imageStoragePath = "groups" + File.separator + groupId + File.separator + fileName;
        StorageReference photoRef = storage.getReference().child(imageStoragePath);
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(file));
        uploadTask.addOnFailureListener(e -> {
            e.printStackTrace();
            callback.complete(e);
        }).addOnSuccessListener(taskSnapshot -> {
            String downloadUrl = getStorageRoot() + "/" + taskSnapshot.getMetadata().getPath();
            callback.complete(null, downloadUrl);
        });
    }

    public void uploadFile(String storagePath, File file, @NonNull Callback callback) {
        StorageReference reference = storage.getReference().child(storagePath);
        UploadTask uploadTask = reference.putFile(Uri.fromFile(file));
        uploadTask
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.complete(e);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    StorageMetadata metadata = taskSnapshot.getMetadata();
                    if (metadata != null) {
                        String downloadUrl = getStorageRoot() + "/" + metadata.getPath();
                        callback.complete(null, downloadUrl);
                    } else {
                        callback.complete(new Error());
                    }
                });
    }

    public String getStorageRoot(){
        return String.format("gs://%s", storage.getReference().getBucket());
    }
}
