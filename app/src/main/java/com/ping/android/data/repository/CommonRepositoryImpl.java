package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.domain.repository.CommonRepository;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class CommonRepositoryImpl implements CommonRepository {
    FirebaseDatabase database;

    @Inject
    public CommonRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public Observable<Boolean> updateBatchData(Map<String, Object> updateValue) {
        return RxFirebaseDatabase.updateBatchData(database.getReference(), updateValue)
                .toObservable();
    }
}
