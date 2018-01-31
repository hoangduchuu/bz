package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.User;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class UserRepositoryImpl implements UserRepository {
    FirebaseDatabase database;

    @Inject
    public UserRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
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
}
