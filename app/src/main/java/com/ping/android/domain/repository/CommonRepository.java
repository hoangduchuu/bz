package com.ping.android.domain.repository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public interface CommonRepository {
    Observable<Boolean> updateBatchData(Map<String, Object> updateValue);

    @NotNull
    Observable<Boolean> observeConnectionState();

    Observable<Boolean> getConnectionState();
}
