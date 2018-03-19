package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.model.User;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface UserRepository {
    Observable<User> initializeUser();

    Observable<User> getCurrentUser();

    Observable<ChildEvent> observeFriendsChildEvent(String userId);

    Observable<Map<String, Boolean>> observeFriendsValue(String userId);

    Observable<User> observeCurrentUser();

    Observable<User> getUser(String userId);

    Observable<List<User>> getUserList(Map<String, Boolean> userIds);

    Observable<ChildEvent> getCalls(String userId);

    Observable<Boolean> updateQuickbloxId(int qbId);

    Observable<Boolean> updateDeviceId(Map<String, Double> devices);

    Observable<Boolean> logout(String deviceId);

    Observable<DataSnapshot> observeUserStatus(String userId);

    Observable<Boolean> deleteFriend(String userId, String friendId);

    Observable<Boolean> observeFriendStatus(String currentUserId, String friendId);

    Observable<Boolean> addContact(String currentUserId, String friendId);
}
