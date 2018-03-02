package com.ping.android.data.repository;

import android.text.TextUtils;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;
import com.quickblox.users.model.QBUser;

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
                .doOnNext(user1 -> this.user = user1);
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
                            .doOnNext(user1 -> user = user1);
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
                .flatMap(userId -> getUser((String)userId))
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
        if (userId == null) return Observable.error(new NullPointerException("Current uuid is null"));
        DatabaseReference reference = database.getReference().child("users").child(userId).child("quickBloxID");
        return RxFirebaseDatabase.setValue(reference, qbId)
                .map(reference1 -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> updateDeviceId(Map<String, Double> devices) {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null) return Observable.error(new NullPointerException("Current uuid is null"));
        DatabaseReference reference = database.getReference().child("users").child(userId).child("devices");
        return RxFirebaseDatabase.setValue(reference, devices)
                .map(reference1 -> true)
                .toObservable();
    }

    @Override
    public Observable<Boolean> logout() {
        this.user = null;
        this.auth.signOut();
        return Observable.just(true);
    }

    @Override
    public Observable<DataSnapshot> observeUserStatus(String userId) {
        DatabaseReference reference = database.getReference("users").child(userId).child("devices");
        return RxFirebaseDatabase.getInstance(reference)
                .onValueEvent();
    }

    private Observable<String> getCurrentUserId() {
        if (auth == null) return Observable.error(new NullPointerException("FirebaseAuth is null"));
        String userId = auth.getUid();
        if (userId == null) return Observable.error(new NullPointerException("Current uuid is null"));
        return Observable.just(userId);
    }
}
