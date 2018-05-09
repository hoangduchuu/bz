package com.bzzzchat.rxfirebase;

import android.net.Uri;

import com.bzzzchat.rxfirebase.storage.UploadTaskSingleOnSubscribe;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import io.reactivex.Single;

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
}
