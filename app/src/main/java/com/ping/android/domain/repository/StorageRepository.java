package com.ping.android.domain.repository;

import com.ping.android.model.Message;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public interface StorageRepository {
    Observable<String> uploadGroupProfileImage(String groupId, String path);

    Observable<String> uploadImageMessage(String key, String filePath);
}
