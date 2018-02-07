package com.bzzzchat.rxfirebase.database;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by tuanluong on 2/6/18.
 */

public class SetValueSingleOnSubscribe implements SingleOnSubscribe<DatabaseReference> {
    private final DatabaseReference reference;
    private final Object value;

    public SetValueSingleOnSubscribe(DatabaseReference reference, Object value) {
        this.reference = reference;
        this.value = value;
    }

    @Override
    public void subscribe(SingleEmitter<DatabaseReference> emitter) throws Exception {
        this.reference.setValue(value, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                emitter.onError(databaseError.toException());
            } else {
                emitter.onSuccess(databaseReference);
            }
        });
    }
}
