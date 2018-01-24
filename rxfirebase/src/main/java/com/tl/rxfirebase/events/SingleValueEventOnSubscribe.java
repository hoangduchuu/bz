package com.tl.rxfirebase.events;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by tuanluong on 1/22/18.
 */

public class SingleValueEventOnSubscribe implements SingleOnSubscribe<DataSnapshot> {
    private final Query query;

    public SingleValueEventOnSubscribe(Query query) {
        this.query = query;
    }

    @Override
    public void subscribe(SingleEmitter<DataSnapshot> e) throws Exception {
        ValueEventListener listener = new TlSingleValueEventListener(e);
        e.setCancellable(new ValueEventCancellable(query, listener));
        query.addListenerForSingleValueEvent(listener);
    }
}
