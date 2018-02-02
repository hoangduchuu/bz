package com.ping.android.domain.repository;

import java.io.File;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public interface StorageRepository {
    Observable<String> uploadGroupProfileImage(String groupId, String path);
}
