package com.ping.android.data.repository;

import android.net.Uri;

import com.bzzzchat.rxfirebase.RxFirebaseStorage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.domain.repository.StorageRepository;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class StorageRepositoryImpl implements StorageRepository {
    FirebaseStorage storage;

    @Inject
    public StorageRepositoryImpl() {
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public Observable<String> uploadGroupProfileImage(String groupId, String filePath) {
        File file = new File(filePath);
        String fileName = System.currentTimeMillis() + file.getName();
        String imageStoragePath = "groups" + File.separator + groupId + File.separator + fileName;
        StorageReference photoRef = storage.getReference().child(imageStoragePath);
        return RxFirebaseStorage.getInstance(photoRef)
                .putFile(Uri.fromFile(file))
                .map(taskSnapshot -> getStorageRoot() + "/" + taskSnapshot.getMetadata().getPath())
                .toObservable();
    }

    private String getStorageRoot(){
        return String.format("gs://%s", storage.getReference().getBucket());
    }
}
