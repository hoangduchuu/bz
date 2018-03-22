package com.ping.android.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.ultility.Callback;

import java.util.Map;

/**
 * Created by bzzz on 1/2/18.
 */

public class BadgesHelper {
    private static BadgesHelper instance = new BadgesHelper();
    private DatabaseReference dbReference = null;

    private BadgesHelper() {
        dbReference = FirebaseDatabase.getInstance().getReference();
    }

    public static BadgesHelper getInstance() {
        return instance;
    }

    public void readUserBadgesWithCompletion(String userId, Callback completion){
        final DatabaseReference userBadgesRef = dbReference.child("users").child(userId).child("badges");
        userBadgesRef.keepSynced(true);
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("badges")
                .child("refreshMock").setValue(0, (databaseError, databaseReference) ->
                userBadgesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Map<String, Long> badges = (Map<String, Long>)dataSnapshot.getValue();
                            int result = 0;
                            for (Map.Entry<String, Long> entry: badges.entrySet()
                                    ) {
                                result += entry.getValue();
                            }
                            completion.complete(null, result);
                        }else{
                            completion.complete(null, 0);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }));

    }

    public void updateUserBadges(String userId, String key, int value){
        dbReference.child("users").child(userId).child("badges").child(key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData.setValue(value);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    public void increaseUserBadges(String userId, String key){
        dbReference.child("users").child(userId).child("badges").child(key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    long value = (long)mutableData.getValue();
                    mutableData.setValue(value + 1);
                    return Transaction.success(mutableData);
                }
                mutableData.setValue(1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }
}
