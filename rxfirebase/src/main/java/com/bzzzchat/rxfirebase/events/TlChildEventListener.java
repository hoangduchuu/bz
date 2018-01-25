package com.bzzzchat.rxfirebase.events;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import io.reactivex.FlowableEmitter;

/**
 * Created by tuanluong on 1/22/18.
 */

public class TlChildEventListener implements ChildEventListener {
    private final FlowableEmitter<ChildEvent> emitter;

    public TlChildEventListener(FlowableEmitter<ChildEvent> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        emitter.onNext(ChildEvent.from(dataSnapshot, ChildEvent.Type.CHILD_ADDED, s));
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        emitter.onNext(ChildEvent.from(dataSnapshot, ChildEvent.Type.CHILD_CHANGED, s));
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        emitter.onNext(ChildEvent.from(dataSnapshot, ChildEvent.Type.CHILD_REMOVED, null));
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        emitter.onNext(ChildEvent.from(dataSnapshot, ChildEvent.Type.CHILD_MOVED, s));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        emitter.onError(databaseError.toException());
    }
}
