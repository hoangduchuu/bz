package com.ping.android.data.repository;

import android.text.TextUtils;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.data.entity.CallEntity;
import com.ping.android.data.entity.ChildData;
import com.ping.android.data.mappers.CallEntityMapper;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.exeption.BzzzExeption;
import com.ping.android.model.Badge;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import durdinapps.rxfirebase2.RxFirebaseAuth;
import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Observable;
import io.reactivex.Single;

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
    public Single<User> initializeUser() {
        if (TextUtils.isEmpty(auth.getUid())) {
            return Single.error(new NullPointerException());
        }
        return getUser(auth.getUid()).firstOrError();
    }

    @Override
    public Observable<Map<String, Boolean>> observeFriendsValue(String userId) {
        Query query = database.getReference("friends")
                .child(userId);
        return RxFirebaseDatabase.observeValueEvent(query).toObservable().map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return (Map<String, Boolean>) dataSnapshot.getValue();
                    }
                    return new HashMap<String, Boolean>();
                });
    }

    @Override
    public Observable<RxFirebaseChildEvent<DataSnapshot>> observeFriendsChildEvent(String userId) {
        Query query = database.getReference("friends")
                .child(userId);
        return RxFirebaseDatabase.observeChildEvent(query).toObservable();
    }

    @Override
    public Single<User> getCurrentUser() {
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
                    return RxFirebaseDatabase.observeValueEvent(userReference).toObservable()
                            .map(User::new);
                });
    }

    @Override
    public Observable<User> getUser(String userId) {
        DatabaseReference userReference = database.getReference("users").child(userId);
        userReference.keepSynced(true);
        return RxFirebaseDatabase.observeSingleValueEvent(userReference)
                .toSingle()
                .map(User::new).toObservable();
    }

    @Override
    public Observable<ChildData<CallEntity>> observeCalls(String userId) {
        Query query =
                database.getReference(CHILD_CALLS).child(userId)
                        .orderByChild("timestamp")
                        .limitToLast(10);
        return RxFirebaseDatabase.observeChildEvent(query)
                .toObservable()
                .map(childEvent -> {
                    if (childEvent.getValue().exists()) {
                        CallEntity callEntity = callEntityMapper.transform(childEvent.getValue());
                        ChildData<CallEntity> childData = new ChildData<>(callEntity, ChildData.Type.from(childEvent.getEventType()));
                        return childData;
                    }
                    throw new NullPointerException();
                });
    }

    @Override
    public Observable<List<CallEntity>> getCalls(String userId) {
        DatabaseReference call = database.getReference(CHILD_CALLS).child(userId);
        call.keepSynced(true);
        Query query = call.orderByChild("timestamp");
        return RxFirebaseDatabase.observeSingleValueEvent(query)
                .toSingle()
                .map(dataSnapshot -> {
                    List<CallEntity> callEntities = new ArrayList<>();
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                            CallEntity callEntity = callEntityMapper.transform(childDataSnapshot);
                            callEntities.add(callEntity);
                        }
                    }
                    return callEntities;
                }).toObservable();
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
        return RxFirebaseDatabase.observeSingleValueEvent(query)
                .toSingle()
                .map(dataSnapshot -> {
                    List<CallEntity> callEntities = new ArrayList<>();
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                            CallEntity callEntity = callEntityMapper.transform(childDataSnapshot);
                            callEntities.add(callEntity);
                        }
                    }
                    return callEntities;
                }).toObservable();
    }

    @Override
    public Observable<Boolean> addCallHistory(CallEntity entity) {
        String callId = database.getReference(CHILD_CALLS).child(entity.getSenderId()).push().getKey();
        entity.setKey(callId);
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("calls/%s/%s", entity.getSenderId(), callId), entity);
        updateValue.put(String.format("calls/%s/%s", entity.getReceiveId(), callId), entity);
        return RxFirebaseDatabase.updateChildren(database.getReference(), updateValue)
                .andThen(Observable.just(true));
    }

    @Override
    public Single<User> loginByEmail(String email, String password) {
        return RxFirebaseAuth.signInWithEmailAndPassword(auth, email, password)
                .flatMapSingle(authResult -> {
                    String userKey = authResult.getUser().getUid();
                    return getUser(userKey).firstOrError();
                });
    }

    @Override
    public Observable<User> checkValidUser(String userName) {
        DatabaseReference userRef = database.getReference("users");
        Query pingIdQuery = userRef.orderByChild("ping_id").equalTo(userName);
        Query emailQuery = userRef.orderByChild("email").equalTo(userName);
        Query phoneQuery = userRef.orderByChild("phone").equalTo(userName);
        Single<User> pingIdSingle = RxFirebaseDatabase.observeSingleValueEvent(pingIdQuery)
                .toSingle()
                .flatMap(dataSnapshot -> {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        User user = new User(dataSnapshot.getChildren().iterator().next());
                        return Single.just(user);
                    }
                    throw new NullPointerException();
                });
        Single<User> emailSingle = RxFirebaseDatabase.observeSingleValueEvent(emailQuery)
                .toSingle()
                .flatMap(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        User user = new User(dataSnapshot);
                        return Single.just(user);
                    }
                    throw new NullPointerException();
                });
        Single<User> phoneSingle = RxFirebaseDatabase.observeSingleValueEvent(phoneQuery)
                .toSingle()
                .flatMap(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        User user = new User(dataSnapshot);
                        return Single.just(user);
                    }
                    throw new NullPointerException();
                });
        return pingIdSingle
                .onErrorResumeNext(emailSingle)
                .onErrorResumeNext(phoneSingle)
                .toObservable();
    }

    @Override
    public Observable<Map<String, Integer>> observeBadgeCount(@NotNull String userKey) {
        DatabaseReference databaseReference = database.getReference("conversation_badge")
                .child(userKey);
        return RxFirebaseDatabase.observeValueEvent(databaseReference)
                .toObservable()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return (Map<String, Integer>) dataSnapshot.getValue();
                    }
                    return new HashMap<>();
                });
    }

    @Override
    public Observable<ChildData<Badge>> observeBadgeCountChildEvent(@NotNull String userKey) {
        DatabaseReference databaseReference = database.getReference("conversation_badge")
                .child(userKey);
        return RxFirebaseDatabase.observeChildEvent(databaseReference)
                .toObservable()
                .map(childEvent -> {
                    DataSnapshot dataSnapshot = childEvent.getValue();
                    Badge badge = new Badge(dataSnapshot.getKey(), dataSnapshot.getValue(Integer.class));
                    ChildData<Badge> childData = new ChildData(badge, childEvent.getEventType());
                    return childData;
                });
    }

    @Override
    public Observable<User> observeUsersChanged() {
        DatabaseReference databaseReference = database.getReference("users");
        return RxFirebaseDatabase.observeChildEvent(databaseReference)
                .toObservable()
                .flatMap(childEvent -> {
                    if (childEvent.getEventType() != RxFirebaseChildEvent.EventType.CHANGED) {
                        return Observable.empty();
                    }
                    User user = new User(childEvent.getValue());
                    return Observable.just(user);
                });
    }

    @Override
    public Observable<Boolean> updateQuickbloxId(int qbId) {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null)
            return Observable.error(new NullPointerException("Current uuid is null"));
        DatabaseReference reference = database.getReference().child("users").child(userId).child("quickBloxID");
        return RxFirebaseDatabase.setValue(reference, qbId)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> updateDeviceIds(Map<String, Double> devices) {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null)
            return Observable.error(new NullPointerException("Current uuid is null"));
        DatabaseReference reference = database.getReference().child("users").child(userId).child("devices");
        return RxFirebaseDatabase.setValue(reference, devices)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> updateDeviceId(String device) {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null)
            return Observable.error(new NullPointerException("Current uuid is null"));
        DatabaseReference reference = database.getReference().child("users").child(userId).child("devices").child(device);
        return RxFirebaseDatabase.setValue(reference, ((double) System.currentTimeMillis() / 1000))
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> logout(String userId, String deviceId) {
        DatabaseReference reference = database.getReference("users").child(userId).child("devices").child(deviceId);
        return RxFirebaseDatabase.setValue(reference, null).andThen(Observable.defer(() -> {
            auth.signOut();
            return Observable.just(true);
        }));
    }

    @Override
    public Observable<DataSnapshot> observeUserStatus(String userId) {
        DatabaseReference reference = database.getReference("users").child(userId).child("devices");
        return RxFirebaseDatabase.observeValueEvent(reference).toObservable();
    }

    @Override
    public Observable<Boolean> deleteFriend(String userId, String friendId) {
        DatabaseReference reference = database.getReference("friends").child(userId).child(friendId);
        return RxFirebaseDatabase.setValue(reference, null)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> observeFriendStatus(String currentUserId, String friendId) {
        Query query = database.getReference("friends").child(currentUserId).child(friendId);
        return RxFirebaseDatabase.observeValueEvent(query)
                .toObservable()
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
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<User> getUserByQuickBloxId(Integer qbId) {
        Query query = database.getReference("users").orderByChild("quickBloxID").equalTo(qbId);
        return RxFirebaseDatabase.observeSingleValueEvent(query)
                .toSingle()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            return new User(snapshot);
                        }
                    }
                    throw new NullPointerException("");
                }).toObservable();
    }

    @Override
    public Observable<Boolean> removeUserBadge(String userId, String key) {
        DatabaseReference databaseReference = database.getReference("conversation_badge")
                .child(userId).child(key);
        return RxFirebaseDatabase.setValue(databaseReference, null)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<RxFirebaseChildEvent<DataSnapshot>> observeBlockedContacts(String key) {
        Query query = database.getReference("users").child(key).child("blocks");
        return RxFirebaseDatabase.observeChildEvent(query).toObservable();
    }

    @Override
    public Observable<Boolean> updateUserNotificationSetting(String key, Boolean aBoolean) {
        DatabaseReference reference = database.getReference("users").child(key).child("settings").child("notification");
        return RxFirebaseDatabase.setValue(reference, aBoolean)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> updateUserPrivateProfileSetting(String key, Boolean aBoolean) {
        DatabaseReference reference = database.getReference("users").child(key).child("settings").child("profile_picture");
        return RxFirebaseDatabase.setValue(reference, aBoolean)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> updateUserProfileImage(String userId, String s) {
        DatabaseReference reference = database.getReference("users").child(userId).child("profile");
        return RxFirebaseDatabase.setValue(reference, s)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> updateUserMapping(String key, String mapKey, String mapValue) {
        DatabaseReference reference = database.getReference("users").child(key).child("mappings").child(mapKey);
        return RxFirebaseDatabase.setValue(reference, mapValue)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> updateUserMappings(String key, Map<String, String> mappings) {
        DatabaseReference reference = database.getReference("users").child(key).child("mappings");
        return RxFirebaseDatabase.setValue(reference, mappings)
                .andThen(Observable.just(true));
    }

    /**
     *
     * @param userId
     * @return number of current badges count
     *
     * NOTE: if badges-count-key is not available, we set badges-count-value to zero
     */
    @Override
    public Observable<Integer> readBadgeNumbers(String userId) {
        final DatabaseReference userBadgesRef = database.getReference("conversation_badge").child(userId);
        userBadgesRef.keepSynced(true);
        return Observable.create(emitter ->
                userBadgesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            emitter.onNext(0);
                            emitter.onComplete();
                            return;
                        }
                        Map<String, Long> badges = (Map<String, Long>) dataSnapshot.getValue();
                        int result = 0;
                        assert badges != null;
                        for (Map.Entry<String, Long> entry : badges.entrySet()) {
                            result += entry.getValue();
                        }
                        emitter.onNext(result);
                        emitter.onComplete();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        emitter.onNext(0);
                        emitter.onComplete();
                    }
                }));
    }

    @Override
    public Observable<Boolean> increaseBadgeNumber(String userId, String key) {
        return Observable.create(emitter -> {
            final DatabaseReference userBadgesRef = database.getReference("conversation_badge").child(userId).child(key);
            userBadgesRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Long badge = (Long) mutableData.getValue();
                    int result = 0;
                    if (badge != null) {
                        result = badge.intValue();
                    }
                    result++;
                    mutableData.setValue(result);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    if (databaseError != null) {
                        emitter.onError(databaseError.toException());
                    } else {
                        emitter.onNext(true);
                    }
                }
            });
        });
    }

    @Override
    public Observable<Boolean> turnOffMappingConfirmation(String key) {
        DatabaseReference reference = database.getReference().child("users").child(key).child("show_mapping_confirm");
        return RxFirebaseDatabase.setValue(reference, true)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> updatePhoneNumber(String userKey, String s) {
        DatabaseReference reference = database.getReference().child("users").child(userKey).child("phone");
        return RxFirebaseDatabase.setValue(reference, s)
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Map<String, String>> observeMappings(String key) {
        DatabaseReference reference = database.getReference().child("users").child(key).child("mappings");
        return RxFirebaseDatabase.observeValueEvent(reference)
                .toObservable()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return (Map<String, String>) dataSnapshot.getValue();
                    }
                    return new HashMap<String, String>();
                });
    }

    private Observable<String> getCurrentUserId() {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null)
            return Observable.error(new NullPointerException("Current uuid is null"));
        return Observable.just(userId);
    }

    @Override
    public Observable<Boolean> checkPassword(String password) {
     return   Observable.create(emitter -> {
            if (auth == null){
                emitter.onError(new BzzzExeption(BzzzExeption.Companion.getUnknown(), " Can not get Current User"));
            }else {
                auth.signInWithEmailAndPassword(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()),password)
                        .addOnSuccessListener(authResult -> {
                            emitter.onNext(true);
                            emitter.onComplete();
                        }).addOnFailureListener(e -> {
                            if (e instanceof FirebaseNetworkException){
                                emitter.onNext(false);
                                emitter.onError(new BzzzExeption(BzzzExeption.Companion.getFirebaseNetWorkExeption(), "Unable to turn off Face ID at this time due to no internet connection. \n Reconnect to internet and try again."));
                                emitter.onComplete();
                                return;
                            }
                            emitter.onNext(false);
                            emitter.onError(new BzzzExeption(BzzzExeption.Companion.getUnknown(), e.getLocalizedMessage()));
                            emitter.onComplete();
                        }).addOnCompleteListener(task -> {
                            emitter.onComplete();
                        });
            }
        });
    }


    @Override
    public Observable<Integer> getQuickBloxIdByUserUUidKey(String UserUUidKey) {
        DatabaseReference reference = database.getReference().child("users").child(UserUUidKey).child("quickBloxID");

        return RxFirebaseDatabase.observeSingleValueEvent(reference)
                .toSingle()
                .map(dataSnapshot -> dataSnapshot.getValue(Integer.class)).toObservable();
    }

    @Override
    public Observable<User> getUserInfoByUUidKey(String userUUidKey) {
        DatabaseReference reference = database.getReference().child("users").child(userUUidKey);
        return RxFirebaseDatabase.observeSingleValueEvent(reference)
                .toSingle()
                .map(User::new).toObservable();
    }



    @Override
    public Observable<List<User>> getUsersProfileInformationFromUserIds(ArrayList<String> userids) {
        List<Observable<User>> observables =
                new ArrayList<>();
        for (int i = 0; i < userids.size(); i++) {
            observables.add(getUserInfoByUUidKey(userids.get(i)));
        }

        return Observable.zip(observables, objects -> {
            List<User> userlist = new ArrayList<>();
            for (int i = 0; i < objects.length - 1; i++) {
                User u = (User) objects[i];
                userlist.add(u);
            }
            return userlist;
        });
    }
}
