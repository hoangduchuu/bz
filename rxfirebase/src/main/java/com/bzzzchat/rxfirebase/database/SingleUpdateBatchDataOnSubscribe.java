package com.bzzzchat.rxfirebase.database;

import com.google.firebase.database.DatabaseReference;

import java.util.Map;

import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by tuanluong on 2/1/18.
 */

public class SingleUpdateBatchDataOnSubscribe implements SingleOnSubscribe<Boolean> {
    private DatabaseReference databaseReference;
    private final Map<String, Object> data;

    public SingleUpdateBatchDataOnSubscribe(DatabaseReference databaseReference, Map<String, Object> data) {
        this.databaseReference = databaseReference;
        this.data = data;
    }

    @Override
    public void subscribe(SingleEmitter<Boolean> emitter) throws Exception {
        databaseReference.updateChildren(data, new CompletionListener(emitter));
    }
}
