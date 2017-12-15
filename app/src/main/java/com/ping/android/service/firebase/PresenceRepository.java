package com.ping.android.service.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.ultility.Callback;
import com.ping.android.utils.Log;

/**
 * Created by tuanluong on 12/11/17.
 */

public class PresenceRepository extends BaseFirebaseDatabase {
    private boolean isOnline = false;

    @Override
    protected void initializeReference(FirebaseDatabase database) {
        databaseReference = database.getReference(".info/connected");
    }

    public void listenStatusChange(Callback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isOnline = dataSnapshot.getValue(Boolean.class);
                if (isOnline) {
                    Log.d("connected");
                    if (callback != null) {
                        callback.complete(null);
                    }
                } else {
                    Log.d("disconnected");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean isOnline() {
        return isOnline;
    }
}
