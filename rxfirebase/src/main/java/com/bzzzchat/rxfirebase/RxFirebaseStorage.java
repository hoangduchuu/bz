package com.bzzzchat.rxfirebase;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.bzzzchat.rxfirebase.storage.UploadTaskSingleOnSubscribe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by tuanluong on 2/1/18.
 */

public class RxFirebaseStorage {
    private StorageReference storageReference;

    public static RxFirebaseStorage getInstance(StorageReference storageReference) {
        RxFirebaseStorage instance = new RxFirebaseStorage();
        instance.storageReference = storageReference;
        return instance;
    }

    public Single<UploadTask.TaskSnapshot> putFile(Uri uri) {
        return Single.create(new UploadTaskSingleOnSubscribe(storageReference, uri));
    }

    public Single<UploadTask.TaskSnapshot> putByteArrays(byte[] data) {
        return Single.create(new UploadTaskSingleOnSubscribe(storageReference, data));
    }

    public static Single<Boolean> downloadFile(String url, String output) {
        return Single.create(emitter -> FirebaseStorage.getInstance().getReferenceFromUrl(url)
                .getFile(new File(output))
                .addOnSuccessListener(taskSnapshot -> emitter.onSuccess(true))
                .addOnFailureListener(emitter::onError));
    }
}
