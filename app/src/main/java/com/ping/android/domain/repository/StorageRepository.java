package com.ping.android.domain.repository;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public interface StorageRepository {
    Observable<String> uploadGroupProfileImage(String groupId, String path);

    Observable<String> uploadFile(String key, String filePath);

    Observable<String> uploadUserProfileImage(String userId, String filePath);
}
