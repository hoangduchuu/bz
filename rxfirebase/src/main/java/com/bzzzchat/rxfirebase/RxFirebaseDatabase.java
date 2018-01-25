package com.bzzzchat.rxfirebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.bzzzchat.rxfirebase.events.ChildEventFlowableOnSubcribe;
import com.bzzzchat.rxfirebase.events.SingleValueEventOnSubscribe;
import com.bzzzchat.rxfirebase.events.ValueEventFlowableOnSubcribe;

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
}
