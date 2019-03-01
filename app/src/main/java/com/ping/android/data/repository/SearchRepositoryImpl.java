package com.ping.android.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.domain.repository.SearchRepository;
import com.ping.android.model.User;
import com.ping.android.utils.DataSnapshotWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/23/18.
 */

public class SearchRepositoryImpl implements SearchRepository {
    FirebaseDatabase database;

    @Inject
    public SearchRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public Observable<List<User>> searchUsers(String text) {
        if (text.isEmpty()) {
            return Observable.just(new ArrayList<>());
        }
        Map<String, Object> query = new HashMap<>();
        query.put("index", "firebase");
        query.put("type", "user");
        query.put("q", text);
        DatabaseReference searchReference = database.getReference().child("search");
        DatabaseReference requestReference = searchReference.child("request").push();
        requestReference.setValue(query);
        return RxFirebaseDatabase.observeValueEvent(searchReference.child("response").child(requestReference.getKey()))
                .toObservable()
                .flatMap(dataSnapshot -> {
                    List<User> users = new ArrayList<>();
                    if (dataSnapshot.exists()) {
                        DataSnapshot usersSnapshot = dataSnapshot.child("hits").child("hits");
                        for (DataSnapshot child : usersSnapshot.getChildren()) {
                            DataSnapshotWrapper wrapper = new DataSnapshotWrapper(child.child("_source"));
                            User user = new User();
                            user.key = child.child("_id").getValue(String.class);
                            user.email = wrapper.getStringValue("email", "");
                            user.pingID = wrapper.getStringValue("ping_id", "");
                            user.firstName = wrapper.getStringValue("first_name", "");
                            user.lastName = wrapper.getStringValue("last_name", "");
                            user.profile = wrapper.getStringValue("profile", "");
                            users.add(user);
                        }
                        return Observable.just(users);
                    }
                    return Observable.empty();
                });
    }
}
