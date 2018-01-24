package com.ping.android.data.repository;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.domain.repository.SearchRepository;
import com.ping.android.model.User;
import com.ping.android.utils.Log;
import com.tl.rxfirebase.RxFirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
        Map<String, Object> query = new HashMap<>();
        query.put("index", "firebase");
        query.put("type", "user");
        query.put("q", text);
        DatabaseReference searchReference = database.getReference().child("search");
        DatabaseReference requestReference = searchReference.child("request").push();
        requestReference.setValue(query);
        return RxFirebaseDatabase.getInstance(searchReference.child("response").child(requestReference.getKey()))
                .onValueEvent()
                .map(dataSnapshot -> {
                   List<User> users = new ArrayList<>();
                   return users;
                });
    }
}
