package com.ping.android.service.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.ultility.Callback;

/**
 * Created by tuanluong on 11/29/17.
 */

public class UserRepository extends BaseFirebaseDatabase {

    @Override
    protected void initializeReference(FirebaseDatabase database) {
        databaseReference = database.getReference().child("users");
    }

    public void getUser(String key, @NonNull Callback callback) {
        databaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
