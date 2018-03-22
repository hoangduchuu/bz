package com.ping.android.data.repository;

import android.text.TextUtils;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.quickblox.users.model.QBUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */
public class UserRepositoryImpl implements UserRepository {
    FirebaseDatabase database;
    FirebaseAuth auth;
    private User user;
    private QBUser qbUser;
    private Map<String, Boolean> friends;

    @Inject
    public UserRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public Observable<User> initializeUser() {
        if (TextUtils.isEmpty(auth.getUid())) {
            return Observable.error(new NullPointerException());
        }
        return getUser(auth.getUid())
                .doOnNext(this::setUser);
    }

    private void setFriendsData(Map<String, Boolean> map) {
        this.friends = map;
        if (this.user != null) {
            this.user.friends = map;
        }
    }

    @Override
    public Observable<Map<String, Boolean>> observeFriendsValue(String userId) {
        Query query = database.getReference("friends")
                .child(userId);
        return RxFirebaseDatabase.getInstance(query).onValueEvent()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return (Map<String, Boolean>) dataSnapshot.getValue();
                    }
                    return new HashMap<String, Boolean>();
                })
                .doOnNext(this::setFriendsData);
    }

    @Override
    public Observable<ChildEvent> observeFriendsChildEvent(String userId) {
        Query query = database.getReference("friends")
                .child(userId);
        return RxFirebaseDatabase.getInstance(query).onChildEvent();
    }

    @Override
    public Observable<User> getCurrentUser() {
        if (user != null) {
            return Observable.just(user);
        } else {
            return initializeUser();
        }
    }

    @Override
    public Observable<User> observeCurrentUser() {
        return getCurrentUserId()
                .flatMap(userId -> {
                    DatabaseReference userReference = database.getReference("users").child(userId);
                    return RxFirebaseDatabase.getInstance(userReference)
                            .onValueEvent()
                            .map(User::new)
                            .doOnNext(user1 -> this.setUser(user1));
                });
    }

    @Override
    public Observable<User> getUser(String userId) {
        DatabaseReference userReference = database.getReference("users").child(userId);
        return RxFirebaseDatabase.getInstance(userReference)
                .onSingleValueEvent()
                .map(dataSnapshot -> {
                    User user = new User(dataSnapshot);
                    return user;
                })
                .toObservable();
    }

    @Override
    public Observable<List<User>> getUserList(Map<String, Boolean> userIds) {
        return Observable.fromArray(userIds.keySet().toArray())
                .flatMap(userId -> getUser((String) userId))
                .take(userIds.size())
                .toList()
                .toObservable();
    }

    @Override
    public Observable<ChildEvent> getCalls(String userId) {
        DatabaseReference callReference = database.getReference("calls").child(userId);
        return RxFirebaseDatabase.getInstance(callReference)
                .onChildEvent();
    }

    @Override
    public Observable<Boolean> updateQuickbloxId(int qbId) {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null)
            return Observable.error(new NullPointerException("Current uuid is null"));
        DatabaseReference reference = database.getReference().child("users").child(userId).child("quickBloxID");
        return RxFirebaseDatabase.setValue(reference, qbId)
                .map(reference1 -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> updateDeviceId(Map<String, Double> devices) {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null)
            return Observable.error(new NullPointerException("Current uuid is null"));
        DatabaseReference reference = database.getReference().child("users").child(userId).child("devices");
        return RxFirebaseDatabase.setValue(reference, devices)
                .map(reference1 -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> logout(String deviceId) {
        this.auth.signOut();
        user.devices.remove(deviceId);
        DatabaseReference reference = database.getReference("users").child(user.key).child("devices").child(deviceId);
        return RxFirebaseDatabase.setValue(reference, null)
                .map(databaseReference -> true)
                .toObservable()
                .doOnNext(aBoolean -> setUser(null));
    }

    @Override
    public Observable<DataSnapshot> observeUserStatus(String userId) {
        DatabaseReference reference = database.getReference("users").child(userId).child("devices");
        return RxFirebaseDatabase.getInstance(reference)
                .onValueEvent();
    }

    @Override
    public Observable<Boolean> deleteFriend(String userId, String friendId) {
        DatabaseReference reference = database.getReference("friends").child(userId).child(friendId);
        return RxFirebaseDatabase.setValue(reference, null)
                .map(reference1 -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> observeFriendStatus(String currentUserId, String friendId) {
        Query query = database.getReference("friends").child(currentUserId).child(friendId);
        return RxFirebaseDatabase.getInstance(query)
                .onValueEvent()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return dataSnapshot.getValue(Boolean.class);
                    }
                    return false;
                })
                .onErrorReturnItem(false);
    }

    @Override
    public Observable<Boolean> addContact(String currentUserId, String friendId) {
        DatabaseReference reference = database.getReference("friends").child(currentUserId).child(friendId);
        return RxFirebaseDatabase.setValue(reference, true)
                .map(databaseReference -> true)
                .toObservable();
    }

    @Override
    public Observable<User> getUserByQuickBloxId(Integer qbId) {
        Query query = database.getReference("users").orderByChild("quickBloxID").equalTo(qbId);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            return new User(snapshot);
                        }
                    }
                    throw new NullPointerException("");
                })
                .toObservable();
    }

    @Override
    public Observable<Boolean> addCallHistory(Call call) {
        String callId = database.getReference("calls").child(call.senderId).push().getKey();
        call.key = callId;
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("calls/%s/%s", call.senderId, callId), call.toMap());
        updateValue.put(String.format("calls/%s/%s", call.receiveId, callId), call.toMap());
        return RxFirebaseDatabase.updateBatchData(database.getReference(), updateValue)
                .toObservable();
    }

    @Override
    public Observable<Boolean> removeUserBadge(String userId, String key) {
        DatabaseReference databaseReference = database.getReference("users")
                .child(userId).child("badges").child(key);
        return RxFirebaseDatabase.setValue(databaseReference, null)
                .map(databaseReference1 -> true)
                .toObservable();
    }

    @Override
    public Observable<ChildEvent> observeBlockedContacts(String key) {
        Query query = database.getReference("users").child(key).child("blocks");
        return RxFirebaseDatabase.getInstance(query)
                .onChildEvent();
    }

    @Override
    public Observable<Boolean> updateUserNotificationSetting(String key, Boolean aBoolean) {
        DatabaseReference reference = database.getReference("users").child(key).child("settings").child("notification");
        return RxFirebaseDatabase.setValue(reference, aBoolean)
                .map(databaseReference -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> updateUserPrivateProfileSetting(String key, Boolean aBoolean) {
        DatabaseReference reference = database.getReference("users").child(key).child("settings").child("private_profile");
        return RxFirebaseDatabase.setValue(reference, aBoolean)
                .map(databaseReference -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> updateUserProfileImage(String userId, String s) {
        DatabaseReference reference = database.getReference("users").child(userId).child("profile");
        return RxFirebaseDatabase.setValue(reference, s)
                .map(databaseReference -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> updateUserMapping(String key, String mapKey, String mapValue) {
        DatabaseReference reference = database.getReference("users").child(key).child("mappings").child(mapKey);
        return RxFirebaseDatabase.setValue(reference, mapValue)
                .map(databaseReference -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> updateUserMappings(String key, Map<String, String> mappings) {
        DatabaseReference reference = database.getReference("users").child(key).child("mappings");
        return RxFirebaseDatabase.setValue(reference, mappings)
                .map(databaseReference -> true)
                .toObservable();
    }

    @Override
    public Observable<Integer> readBadgeNumbers(String userId) {
        final DatabaseReference userBadgesRef = database.getReference("users").child(userId).child("badges");
        userBadgesRef.keepSynced(true);
        DatabaseReference refreshMockReference = database.getReference("users").child(userId).child("badges")
                .child("refreshMock");
        return RxFirebaseDatabase.setValue(refreshMockReference, 0)
                .flatMap(databaseReference -> RxFirebaseDatabase.getInstance(userBadgesRef)
                        .onSingleValueEvent()
                        .map(dataSnapshot -> {
                            Map<String, Long> badges = (Map<String, Long>)dataSnapshot.getValue();
                            int result = 0;
                            for (Map.Entry<String, Long> entry: badges.entrySet()
                                    ) {
                                result += entry.getValue();
                            }
                            return result;
                        })
                ).toObservable();
    }

    private Observable<String> getCurrentUserId() {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null)
            return Observable.error(new NullPointerException("Current uuid is null"));
        return Observable.just(userId);
    }

    private void setUser(User user) {
        if (user != null) {
            user.friends = this.friends;
        }
        this.user = user;
    }
}
