package com.ping.android.domain.repository;

import com.google.firebase.database.DataSnapshot;
import com.ping.android.data.entity.CallEntity;
import com.ping.android.data.entity.ChildData;
import com.ping.android.model.Badge;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface UserRepository {
    Single<User> initializeUser();

    Single<User> getCurrentUser();

    Observable<User> observeCurrentUser();

    Observable<User> getUser(String userId);

    Observable<Boolean> updateQuickbloxId(int qbId);

    Observable<Boolean> updateDeviceIds(Map<String, Double> devices);

    Observable<Boolean> updateDeviceId(String device);


    Observable<Boolean> logout(String userId, String deviceId);

    Observable<DataSnapshot> observeUserStatus(String userId);

    Observable<Boolean> deleteFriend(String userId, String friendId);

    Observable<Boolean> observeFriendStatus(String currentUserId, String friendId);

    Observable<Boolean> addContact(String currentUserId, String friendId);

    Observable<User> getUserByQuickBloxId(Integer qbId);

    Observable<RxFirebaseChildEvent<DataSnapshot>> observeBlockedContacts(String key);

    Observable<Boolean> updateUserNotificationSetting(String key, Boolean aBoolean);

    Observable<Boolean> updateUserPrivateProfileSetting(String key, Boolean aBoolean);

    Observable<Boolean> updateUserProfileImage(String userId, String s);

    Observable<Boolean> updateUserMapping(String key, String mapKey, String mapValue);

    Observable<Boolean> updateUserMappings(String key, Map<String, String> mappings);

    Observable<Integer> readBadgeNumbers(String userId);

    Observable<Boolean> removeUserBadge(String userId, String key);

    Observable<Boolean> increaseBadgeNumber(String userId, String key);

    Observable<Map<String, Integer>> observeBadgeCount(@NotNull String userKey);

    Observable<ChildData<Badge>> observeBadgeCountChildEvent(@NotNull String userKey);

    Observable<Boolean> turnOffMappingConfirmation(String key);

    Observable<Boolean> updatePhoneNumber(String userKey, String s);

    Observable<Map<String, String>> observeMappings(String key);

    Observable<RxFirebaseChildEvent<DataSnapshot>> observeFriendsChildEvent(String userId);

    Observable<Map<String, Boolean>> observeFriendsValue(String userId);

    Observable<ChildData<CallEntity>> observeCalls(String userId);

    Observable<List<CallEntity>> getCalls(String userId);

    Observable<List<CallEntity>> loadMoreCalls(String key, Double params);

    Observable<Boolean> addCallHistory(CallEntity entity);

    Single<User> loginByEmail(String email, String password);

    Observable<User> checkValidUser(String userName);

    Observable<User> observeUsersChanged();

    Observable<Boolean> checkPassword(String password);

    Observable<Integer> getQuickBloxIdByUserUUidKey(String UserUUidKey);

    Observable<User> getUserInfoByUUidKey(String userUUidKey);


    Observable<List<User>> getUsersProfileInformationFromUserIds(ArrayList<String> users);
}
