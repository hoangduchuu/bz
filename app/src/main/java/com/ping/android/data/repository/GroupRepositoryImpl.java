package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.model.Group;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class GroupRepositoryImpl implements GroupRepository {
    FirebaseDatabase database;

    @Inject
    public GroupRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public Observable<Group> getGroup(String groupId) {
        DatabaseReference groupReference = database.getReference("groups").child(groupId);
        return RxFirebaseDatabase.getInstance(groupReference)
                .onSingleValueEvent()
                .map(Group::from)
                .toObservable();
    }

    @Override
    public Observable<ChildEvent> groupsChange(String userId) {
        DatabaseReference groupReference = database.getReference("groups").child(userId);
        return RxFirebaseDatabase.getInstance(groupReference)
                .onChildEvent();
    }
}
