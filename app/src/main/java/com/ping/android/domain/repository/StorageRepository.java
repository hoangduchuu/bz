package com.ping.android.domain.repository;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public interface StorageRepository {
    Observable<String> uploadGroupProfileImage(String groupId, String path);

    Observable<String> uploadFile(String key, String filePath);

    Observable<String> uploadFile(String key, String fileName, byte[] bytes);

    Observable<String> uploadUserProfileImage(String userId, String filePath);

    @NotNull
    Observable<Boolean> downloadFile(@NotNull String url, @NotNull String saveFile);
}
