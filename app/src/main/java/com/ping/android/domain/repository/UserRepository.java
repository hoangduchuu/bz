package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.ping.android.model.Call;
import com.ping.android.model.ChildData;
import com.ping.android.model.User;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface UserRepository {
    Observable<User> getUser(String userId);
    Observable<List<User>> getUserList(Map<String, Boolean> userIds);
    Observable<ChildEvent> getCalls(String userId);
}
