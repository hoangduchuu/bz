package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.model.Group;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface GroupRepository {
    Observable<String> getKey();
    Observable<Group> getGroup(String userId, String groupId);
    Observable<Group> observeGroupValue(String userId, String groupId);
    Observable<ChildEvent> groupsChange(String userId);

    Observable<Boolean> createGroup(Group group);
    Observable<Boolean> updateGroupConversationId(String groupId, String conversationId);
}
