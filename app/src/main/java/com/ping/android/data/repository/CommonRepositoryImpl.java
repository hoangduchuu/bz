package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.domain.repository.CommonRepository;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public Observable<Boolean> observeConnectionState() {
        Query query = database.getReference(".info/connected");
        return RxFirebaseDatabase.getInstance(query).onValueEvent()
                .map(dataSnapshot -> dataSnapshot.getValue(Boolean.class));
    }

    @Override
    public Observable<Boolean> getConnectionState() {
        Query query = database.getReference(".info/connected");
        return RxFirebaseDatabase.getInstance(query).onSingleValueEvent()
                .map(dataSnapshot -> dataSnapshot.getValue(Boolean.class))
                .toObservable();
    }
}
