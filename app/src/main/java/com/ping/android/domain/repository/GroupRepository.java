package com.ping.android.domain.repository;

import com.ping.android.model.Group;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface GroupRepository {
    Observable<Group> getGroup(String groupId);
}
