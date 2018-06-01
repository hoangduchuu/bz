package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.data.entity.CallEntity;
import com.ping.android.model.Call;
import com.ping.android.data.entity.ChildData;
import com.ping.android.model.User;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface UserRepository {
    Observable<User> initializeUser();

    Observable<User> getCurrentUser();

    Observable<User> observeCurrentUser();

    Observable<User> getUser(String userId);

    Observable<Boolean> updateQuickbloxId(int qbId);

    Observable<Boolean> updateDeviceId(Map<String, Double> devices);

    Observable<Boolean> logout(String deviceId);

    Observable<DataSnapshot> observeUserStatus(String userId);

    Observable<Boolean> deleteFriend(String userId, String friendId);

    Observable<Boolean> observeFriendStatus(String currentUserId, String friendId);

    Observable<Boolean> addContact(String currentUserId, String friendId);

    Observable<User> getUserByQuickBloxId(Integer qbId);

    Observable<Boolean> removeUserBadge(String userId, String key);

    Observable<ChildEvent> observeBlockedContacts(String key);

    Observable<Boolean> updateUserNotificationSetting(String key, Boolean aBoolean);

    Observable<Boolean> updateUserPrivateProfileSetting(String key, Boolean aBoolean);

    Observable<Boolean> updateUserProfileImage(String userId, String s);

    Observable<Boolean> updateUserMapping(String key, String mapKey, String mapValue);

    Observable<Boolean> updateUserMappings(String key, Map<String, String> mappings);

    Observable<Integer> readBadgeNumbers(String userId);

    Observable<Boolean> turnOffMappingConfirmation(String key);

    Observable<Boolean> updatePhoneNumber(String userKey, String s);

    Observable<DataSnapshot> observeMappings(String key);

    Observable<List<User>> getUserList(Map<String, Boolean> userIds);

    Observable<ChildEvent> observeFriendsChildEvent(String userId);

    Observable<Map<String, Boolean>> observeFriendsValue(String userId);

    Observable<ChildData<CallEntity>> observeCalls(String userId);

    Observable<List<CallEntity>> getCalls(String userId);

    Observable<List<CallEntity>> loadMoreCalls(String key, Double params);

    Observable<Boolean> addCallHistory(CallEntity entity);
}
