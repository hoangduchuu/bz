package com.bzzzchat.rxfirebase.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by tuanluong on 1/24/18.
 */

public class ValueEventFlowableOnSubcribe implements FlowableOnSubscribe<DataSnapshot> {
    private final Query query;

    public ValueEventFlowableOnSubcribe(Query query) {
        this.query = query;
    }

    @Override
    public void subscribe(FlowableEmitter<DataSnapshot> emitter) throws Exception {
        ValueEventListener listener = new TlValueEventListener(emitter);
        emitter.setCancellable(new ValueEventCancellable(query, listener));
        query.addValueEventListener(listener);
    }
}
