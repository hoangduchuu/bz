package com.ping.android.data.repository;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.domain.repository.CommonRepository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
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
        return RxFirebaseDatabase.updateChildren(database.getReference(), updateValue).andThen(Observable.just(true));
    }

    @NotNull
    @Override
    public Observable<Boolean> observeConnectionState() {
        Query query = database.getReference(".info/connected");
        return RxFirebaseDatabase.observeValueEvent(query).toObservable()
                .map(dataSnapshot -> dataSnapshot.getValue(Boolean.class));
    }

    @Override
    public Observable<Boolean> getConnectionState() {
        Query query = database.getReference(".info/connected");
        return RxFirebaseDatabase.observeSingleValueEvent(query).toSingle()
                .map(dataSnapshot -> dataSnapshot.getValue(Boolean.class)).toObservable();
    }
}
