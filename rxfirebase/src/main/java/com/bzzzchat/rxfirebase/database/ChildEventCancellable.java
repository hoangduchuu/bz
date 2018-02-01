package com.bzzzchat.rxfirebase.database;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;

import io.reactivex.functions.Cancellable;

/**
 * Created by tuanluong on 1/22/18.
 */

public class ChildEventCancellable implements Cancellable {
    private final Query query;
    private final ChildEventListener listener;

    public ChildEventCancellable(Query query, ChildEventListener listener) {
        this.query = query;
        this.listener = listener;
    }

    @Override
    public void cancel() throws Exception {
        this.query.removeEventListener(listener);
    }
}
