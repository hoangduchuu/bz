package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.ping.android.model.Group;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface GroupRepository {
    Observable<Group> getGroup(String groupId);
    Observable<ChildEvent> groupsChange(String userId);

    Observable<Boolean> addUsersToGroup(String groupId, List<String> userIds);
}
