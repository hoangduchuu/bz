package com.ping.android.domain.repository;

import com.google.firebase.database.DataSnapshot;
import com.ping.android.model.Group;

import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface GroupRepository {
    Observable<String> getKey();
    Observable<Group> getGroup(String userId, String groupId);
    Observable<Group> observeGroupValue(String userId, String groupId);
    Observable<RxFirebaseChildEvent<DataSnapshot>> groupsChange(String userId);

    Observable<Boolean> createGroup(Group group);
    Observable<Boolean> updateGroupConversationId(String groupId, String conversationId);
}
