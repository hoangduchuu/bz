package com.ping.android.service.firebase;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.ultility.Callback;

import java.util.Map;

/**
 * Created by tuanluong on 11/29/17.
 */

public abstract class BaseFirebaseDatabase {
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    protected DatabaseReference databaseReference;

    public BaseFirebaseDatabase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        initializeReference(database);
    }

    protected abstract void initializeReference(FirebaseDatabase database);

    public void updateBatchData(@NonNull Map<String, Object> data, Callback callback) {
        database.getReference().updateChildren(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (callback != null) {
                            callback.complete(null, "Update successfully");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) {
                            callback.complete(e);
                        }
                    }
                });
    }
}