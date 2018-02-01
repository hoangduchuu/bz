package com.bzzzchat.rxfirebase;

import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.bzzzchat.rxfirebase.database.ChildEventFlowableOnSubcribe;
import com.bzzzchat.rxfirebase.database.SingleUpdateBatchDataOnSubscribe;
import com.bzzzchat.rxfirebase.database.SingleValueEventOnSubscribe;
import com.bzzzchat.rxfirebase.database.ValueEventFlowableOnSubcribe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by tuanluong on 1/22/18.
 */

public class RxFirebaseDatabase {
    private Query query;

    private RxFirebaseDatabase(Query query) {
        this.query = query;
    }

    public static RxFirebaseDatabase getInstance(Query query) {
        return new RxFirebaseDatabase(query);
    }

    public Single<DataSnapshot> onSingleValueEvent() {
        return Single.create(new SingleValueEventOnSubscribe(query));
    }

    public Observable<DataSnapshot> onValueEvent() {
        return Flowable.create(new ValueEventFlowableOnSubcribe(query), BackpressureStrategy.MISSING)
                .toObservable();
    }

    public Observable<ChildEvent> onChildEvent() {
        return Flowable.create(new ChildEventFlowableOnSubcribe(query), BackpressureStrategy.MISSING)
                .toObservable();
    }

    public static Single<Boolean> updateBatchData(DatabaseReference reference, Map<String, Object> data) {
        return Single.create(new SingleUpdateBatchDataOnSubscribe(reference, data));
    }
}
