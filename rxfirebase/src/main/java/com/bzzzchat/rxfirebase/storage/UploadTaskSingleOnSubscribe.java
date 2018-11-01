package com.bzzzchat.rxfirebase.storage;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by tuanluong on 2/1/18.
 */

public class UploadTaskSingleOnSubscribe implements SingleOnSubscribe<UploadTask.TaskSnapshot> {
    private final StorageReference storageReference;
    private final Uri fileUri;
    private final byte[] bytes;

    public UploadTaskSingleOnSubscribe(StorageReference storageReference, Uri fileUri) {
        this.storageReference = storageReference;
        this.fileUri = fileUri;
        this.bytes = null;
    }

    public UploadTaskSingleOnSubscribe(StorageReference storageReference, byte[] bytes) {
        this.storageReference = storageReference;
        this.fileUri = null;
        this.bytes = bytes;
    }

    @Override
    public void subscribe(SingleEmitter<UploadTask.TaskSnapshot> emitter) throws Exception {
        StorageTask<UploadTask.TaskSnapshot> uploadTask;
        if (fileUri != null) {
            uploadTask = storageReference
                    .putFile(fileUri);
        } else {
            uploadTask = storageReference
                    .putBytes(bytes);
        }
        uploadTask.addOnSuccessListener(emitter::onSuccess)
                .addOnFailureListener(emitter::onError);
        emitter.setCancellable(uploadTask::cancel);
    }
}
