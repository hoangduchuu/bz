package com.ping.android.service.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;

/**
 * Created by tuanluong on 11/29/17.
 */

public class UserRepository extends BaseFirebaseDatabase {

    @Override
    protected void initializeReference(FirebaseDatabase database) {
        databaseReference = database.getReference().child("users");
    }

    public void initializeUser(@NonNull Callback callback) {
        if (auth.getCurrentUser() != null) {
            getUser(auth.getUid(), callback);
        } else {
            callback.complete(new Error());
        }
    }

    public void getUser(String key, @NonNull Callback callback) {
        databaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = new User(dataSnapshot);
                    callback.complete(null, user);
                } else {
                    callback.complete(new Error());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.complete(new Error());
            }
        });
    }

    public void updateQBId(String userId, int id) {
        databaseReference.child(userId).child("quickBloxID").setValue(id);
    }
}
