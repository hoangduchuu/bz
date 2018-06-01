package com.ping.android.data.repository;

import android.text.TextUtils;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.data.entity.CallEntity;
import com.ping.android.data.mappers.CallEntityMapper;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.data.entity.ChildData;
import com.ping.android.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */
public class UserRepositoryImpl implements UserRepository {
    private static final String CHILD_CALLS = "calls";

    @Inject
    CallEntityMapper callEntityMapper;

    private FirebaseDatabase database;
    private FirebaseAuth auth;
//    private User user;
    // Currently, users will be cached for later use.
    // Should improve by invalidate user after a certain of time
    //private Map<String, User> cachedUsers = new HashMap<>();

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
        return getUser(auth.getUid());
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
                });
    }

    @Override
    public Observable<ChildEvent> observeFriendsChildEvent(String userId) {
        Query query = database.getReference("friends")
                .child(userId);
        return RxFirebaseDatabase.getInstance(query).onChildEvent();
    }

    @Override
    public Observable<User> getCurrentUser() {
//        if (user != null) {
//            return Observable.just(user);
//        } else {
            return initializeUser();
//        }
    }

    @Override
    public Observable<User> observeCurrentUser() {
        return getCurrentUserId()
                .flatMap(userId -> {
                    DatabaseReference userReference = database.getReference("users").child(userId);
                    return RxFirebaseDatabase.getInstance(userReference)
                            .onValueEvent()
                            .map(User::new);
                });
    }

    @Override
    public Observable<User> getUser(String userId) {
        DatabaseReference userReference = database.getReference("users").child(userId);
        return RxFirebaseDatabase.getInstance(userReference)
                .onSingleValueEvent()
                .map(User::new)
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
    public Observable<ChildData<CallEntity>> observeCalls(String userId) {
        Query query =
                database.getReference(CHILD_CALLS).child(userId)
                        .orderByChild("timestamp");
        query.keepSynced(true);
        return RxFirebaseDatabase.getInstance(query)
                .onChildEvent()
                .map(childEvent -> {
                    if (childEvent.dataSnapshot.exists()) {
                        CallEntity callEntity = callEntityMapper.transform(childEvent.dataSnapshot);
                        ChildData<CallEntity> childData = new ChildData<>(callEntity, ChildData.Type.from(childEvent.type));
                        return childData;
                    }
                    throw new NullPointerException();
                });
    }

    @Override
    public Observable<List<CallEntity>> getCalls(String userId) {
        Query query =
                database.getReference(CHILD_CALLS).child(userId)
                .orderByChild("timestamp");
                //.limitToLast(15);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .map(dataSnapshot -> {
                    List<CallEntity> callEntities = new ArrayList<>();
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot childDataSnapshot: dataSnapshot.getChildren()) {
                            CallEntity callEntity = callEntityMapper.transform(childDataSnapshot);
                            callEntities.add(callEntity);
                        }
                    }
                    return callEntities;
                })
                .toObservable();
    }

    @Override
    public Observable<List<CallEntity>> loadMoreCalls(String key, Double timestamp) {
        DatabaseReference callReference = database.getReference(CHILD_CALLS)
                .child(key);
        callReference.keepSynced(true);
        Query query = callReference
                .orderByChild("timesstamps")
                .endAt(timestamp)
                .limitToLast(15);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .map(dataSnapshot -> {
                    List<CallEntity> callEntities = new ArrayList<>();
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot childDataSnapshot: dataSnapshot.getChildren()) {
                            CallEntity callEntity = callEntityMapper.transform(childDataSnapshot);
                            callEntities.add(callEntity);
                        }
                    }
                    return callEntities;
                })
                .toObservable();
    }

    @Override
    public Observable<Boolean> addCallHistory(CallEntity entity) {
        String callId = database.getReference(CHILD_CALLS).child(entity.getSenderId()).push().getKey();
        entity.setKey(callId);
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("calls/%s/%s", entity.getSenderId(), callId), entity);
        updateValue.put(String.format("calls/%s/%s", entity.getReceiveId(), callId), entity);
        return RxFirebaseDatabase.updateBatchData(database.getReference(), updateValue)
                .toObservable();
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
        String userId = this.auth.getUid();
        this.auth.signOut();
        DatabaseReference reference = database.getReference("users").child(userId).child("devices").child(deviceId);
        return RxFirebaseDatabase.setValue(reference, null)
                .map(databaseReference -> true)
                .toObservable();
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

    @Override
    public Observable<Boolean> turnOffMappingConfirmation(String key) {
        DatabaseReference reference = database.getReference().child("users").child(key).child("show_mapping_confirm");
        return RxFirebaseDatabase.setValue(reference, true)
                .map(databaseReference -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> updatePhoneNumber(String userKey, String s) {
        DatabaseReference reference = database.getReference().child("users").child(userKey).child("phone");
        return RxFirebaseDatabase.setValue(reference, s)
                .map(databaseReference -> true)
                .toObservable();
    }

    @Override
    public Observable<DataSnapshot> observeMappings(String key) {
        DatabaseReference reference = database.getReference().child("users").child(key).child("mappings");
        return RxFirebaseDatabase.getInstance(reference)
                .onValueEvent();
    }

    private Observable<String> getCurrentUserId() {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null)
            return Observable.error(new NullPointerException("Current uuid is null"));
        return Observable.just(userId);
    }
}
