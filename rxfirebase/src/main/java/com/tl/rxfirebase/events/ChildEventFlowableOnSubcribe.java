package com.tl.rxfirebase.events;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;

import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by tuanluong on 1/22/18.
 */

public class ChildEventFlowableOnSubcribe implements FlowableOnSubscribe<ChildEvent> {
    private final Query query;

    public ChildEventFlowableOnSubcribe(Query query) {
        this.query = query;
    }

    @Override
    public void subscribe(FlowableEmitter<ChildEvent> e) throws Exception {
        ChildEventListener listener = new TlChildEventListener(e);
        e.setCancellable(new ChildEventCancellable(query, listener));
        query.addChildEventListener(listener);
    }
}
