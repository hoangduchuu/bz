package com.bzzzchat.rxfirebase.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.FlowableEmitter;

/**
 * Created by tuanluong on 1/22/18.
 */

public class TlValueEventListener implements ValueEventListener {
    private final FlowableEmitter<DataSnapshot> emitter;

    public TlValueEventListener(FlowableEmitter<DataSnapshot> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        emitter.onNext(dataSnapshot);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        emitter.onError(databaseError.toException());
    }
}
