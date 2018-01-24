package com.tl.rxfirebase.events;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.functions.Cancellable;

/**
 * Created by tuanluong on 1/22/18.
 */

public class ValueEventCancellable implements Cancellable {
    private final Query query;
    private final ValueEventListener listener;

    public ValueEventCancellable(Query query, ValueEventListener listener) {
        this.query = query;
        this.listener = listener;
    }

    @Override
    public void cancel() throws Exception {
        query.removeEventListener(listener);
    }
}
