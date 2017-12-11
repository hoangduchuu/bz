package com.ping.android.service.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                    user.typeFriend = Constant.TYPE_FRIEND.NON_FRIEND;
                    User currentUser = UserManager.getInstance().getUser();
                    if (currentUser != null && currentUser.friends.containsKey(user.key)) {
                        user.typeFriend = Constant.TYPE_FRIEND.IS_FRIEND;
                    }
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

    public void getUserByQbId(int qbId, @NonNull Callback callback) {
        databaseReference.orderByChild("quickBloxID").equalTo(qbId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = new User(snapshot);
                        callback.complete(null, user);
                        break;
                    }
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

    public void searchUsersWithText(String text, String child, Callback callback) {
        databaseReference.orderByChild(child)
                .startAt(text)
                .endAt(text + "\uf8ff")
                .limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (callback != null)
                            callback.complete(null, dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void matchUserWithText(String text, String child, Callback callback) {
        databaseReference.orderByChild(child)
                .equalTo(text)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (callback != null) {
                            if (dataSnapshot.exists()) {
                                callback.complete(null, dataSnapshot);
                            } else {
                                callback.complete(new Error());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void initMemberList(Map<String, Boolean> memberIds, Callback callback) {
        List<User> members = new ArrayList<>();
        List<String> finishIDs = new ArrayList<>();
        List<String> getIDs = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : memberIds.entrySet()) {
            String id = entry.getKey();
            Boolean exist = entry.getValue();
            if (exist)
                getIDs.add(id);
        }

        for (String id : getIDs) {
            getUser(id, (error, data) -> {
                User user = (User) data[0];
                members.add(user);
                finishIDs.add(user.key);
                if (finishIDs.size() == memberIds.size()) {
                    callback.complete(null, members);
                }
            });
        }
    }
}
