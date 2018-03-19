package com.ping.android.service.firebase;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;

import java.util.ArrayList;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 11/29/17.
 */

public abstract class BaseFirebaseDatabase {
    protected FirebaseAuth auth;
    private FirebaseDatabase database;
    protected FirebaseUser firebaseUser;

    protected DatabaseReference databaseReference;

    public BaseFirebaseDatabase() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        initializeReference(database);
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    protected String currentUserId() {
        return auth.getCurrentUser().getUid();
    }

    protected abstract void initializeReference(FirebaseDatabase database);

    public String generateKey() {
        return databaseReference.push().getKey();
    }

    public void updateBatchData(@NonNull Map<String, Object> data, Callback callback) {
        database.getReference().updateChildren(data)
                .addOnSuccessListener(task -> {
                    if (callback != null) {
                        callback.complete(null, "Update successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.complete(e);
                    }
                });
    }
}
