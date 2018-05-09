package com.bzzzchat.rxfirebase.database;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import io.reactivex.SingleEmitter;

/**
 * Created by tuanluong on 2/1/18.
 */

public class CompletionListener implements DatabaseReference.CompletionListener {
    private final SingleEmitter<Boolean> emitter;

    public CompletionListener(SingleEmitter<Boolean> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError == null) {
            emitter.onSuccess(true);
        } else {
            emitter.onError(databaseError.toException());
        }
    }
}
