package com.tl.rxfirebase.events;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.SingleEmitter;

/**
 * Created by tuanluong on 1/22/18.
 */

public class TlSingleValueEventListener implements ValueEventListener {
    private final SingleEmitter<DataSnapshot> emitter;

    public TlSingleValueEventListener(SingleEmitter<DataSnapshot> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        emitter.onSuccess(dataSnapshot);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        emitter.onError(databaseError.toException());
    }
}
