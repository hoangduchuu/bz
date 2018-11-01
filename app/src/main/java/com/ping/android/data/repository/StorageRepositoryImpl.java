package com.ping.android.data.repository;

import android.net.Uri;
import android.text.TextUtils;

import com.bzzzchat.rxfirebase.RxFirebaseStorage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.domain.repository.StorageRepository;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

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
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        File file = new File(filePath);
        String fileName = System.currentTimeMillis() + file.getName();
        String imageStoragePath = "groups" + File.separator + groupId + File.separator + fileName;
        StorageReference photoRef = storage.getReference().child(imageStoragePath);
        return RxFirebaseStorage.getInstance(photoRef)
                .putFile(Uri.fromFile(file))
                .map(taskSnapshot -> getStorageRoot() + "/" + taskSnapshot.getMetadata().getPath())
                .toObservable();
    }

    @Override
    public Observable<String> uploadFile(String key, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        File file = new File(filePath);
        String conversationImagePath = "conversations/" + key + "/" + file.getName();
        StorageReference photoRef = storage.getReference().child(conversationImagePath);
        return RxFirebaseStorage.getInstance(photoRef)
                .putFile(Uri.fromFile(file))
                .map(taskSnapshot -> getStorageRoot() + "/" + taskSnapshot.getMetadata().getPath())
                .toObservable();
    }

    @Override
    public Observable<String> uploadFile(String key, String fileName, byte[] bytes) {
        if (bytes == null) return Observable.just("");
        String conversationImagePath = "conversations/" + key + "/" + fileName;
        StorageReference photoRef = storage.getReference().child(conversationImagePath);
        return RxFirebaseStorage.getInstance(photoRef)
                .putByteArrays(bytes)
                .map(taskSnapshot -> getStorageRoot() + "/" + taskSnapshot.getMetadata().getPath())
                .toObservable();
    }

    @Override
    public Observable<String> uploadUserProfileImage(String userId, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        File file = new File(filePath);
        String fileName = System.currentTimeMillis() + file.getName();
        String imageStoragePath = "profiles" + File.separator + userId + File.separator + fileName;
        StorageReference photoRef = storage.getReference().child(imageStoragePath);
        return RxFirebaseStorage.getInstance(photoRef)
                .putFile(Uri.fromFile(file))
                .map(taskSnapshot -> getStorageRoot() + "/" + taskSnapshot.getMetadata().getPath())
                .toObservable();
    }

    @NotNull
    @Override
    public Observable<Boolean> downloadFile(@NotNull String url, @NotNull String saveFile) {
        return RxFirebaseStorage.downloadFile(url, saveFile)
                .toObservable();
    }

    @NotNull
    @Override
    public Observable<String> uploadStickerFile(@NotNull String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        String stickerStoragePath = "stickers" + File.separator + fileName;
        StorageReference photoRef = storage.getReference().child(stickerStoragePath);
        return exists(photoRef)
                .onErrorResumeNext(RxFirebaseStorage.getInstance(photoRef)
                        .putFile(Uri.fromFile(file))
                        .map(taskSnapshot -> getStorageRoot() + "/" + taskSnapshot.getMetadata().getPath()))
                .toObservable();
    }

    private Single<String> exists(StorageReference storageReference) {
        return Single.create(emitter -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                emitter.onSuccess(getStorageRoot() + "/" + uri.toString());
            }).addOnFailureListener(emitter::onError);
        });
    }

    private String getStorageRoot(){
        return String.format("gs://%s", storage.getReference().getBucket());
    }
}
