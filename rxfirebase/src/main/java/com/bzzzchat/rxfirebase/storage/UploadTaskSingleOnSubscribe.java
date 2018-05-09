package com.bzzzchat.rxfirebase.storage;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Cancellable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class UploadTaskSingleOnSubscribe implements SingleOnSubscribe<UploadTask.TaskSnapshot> {
    private final StorageReference storageReference;
    private final Uri fileUri;

    public UploadTaskSingleOnSubscribe(StorageReference storageReference, Uri fileUri) {
        this.storageReference = storageReference;
        this.fileUri = fileUri;
    }

    @Override
    public void subscribe(SingleEmitter<UploadTask.TaskSnapshot> emitter) throws Exception {
        StorageTask<UploadTask.TaskSnapshot> uploadTask = storageReference.putFile(fileUri)
                .addOnSuccessListener(emitter::onSuccess)
                .addOnFailureListener(emitter::onError);
        emitter.setCancellable(uploadTask::cancel);
    }
}
