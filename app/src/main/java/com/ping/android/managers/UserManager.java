package com.ping.android.managers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.device.Device;
import com.ping.android.model.User;
import com.ping.android.service.CallService;
import com.ping.android.service.QuickBloxRepository;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.PresenceRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Consts;
import com.ping.android.utils.ActivityLifecycle;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.users.model.QBUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tuanluong on 12/6/17.
 */

public class UserManager {
    private ArrayList<User> friendList;
    private ArrayList<User> blockList;
    private User user;
    private QBUser qbUser;
    private DatabaseReference userDatabaseReference;
    private UserRepository userRepository;
    private ValueEventListener userUpdateListener;
    private List<Callback> userUpdated;

    private static UserManager instance;
    private ArrayList<User> allUsers = new ArrayList<>();

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() {
        userUpdated = new ArrayList<>();
        userRepository = new UserRepository();
        friendList = new ArrayList<>();
        blockList = new ArrayList<>();
    }

    public void addUserUpdated(Callback callback) {
        if (callback != null) {
            userUpdated.add(callback);
        }
    }

    public void removeUserUpdated(Callback callback) {
        if (callback != null) {
            userUpdated.remove(callback);
        }
    }

    private void onBlocksUpdated(Map<String, Object> blocks) {
        blockList = new ArrayList<>();
    }

    private void onFriendsUpdated(Map<String, Boolean> friends) {
        friendList = new ArrayList<>();
        initFriendList(friends);
    }

    public void startCallService() {
        Intent tempIntent = new Intent(ActivityLifecycle.getForegroundActivity(), CallService.class);
        PendingIntent pendingIntent = ActivityLifecycle.getForegroundActivity().createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(ActivityLifecycle.getForegroundActivity(), qbUser, pendingIntent);
    }

    public void addValueEventListener() {
        if (user == null) return;

        userUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (user == null) return;
                User updateUser = new User(dataSnapshot);
                boolean shouldUpdateFriends = false;
                if (user.friendList.size() != updateUser.friendList.size()) {
                    // Handle friends updated
                    shouldUpdateFriends = true;
                }
                if (user.blocks.size() != updateUser.blocks.size()) {
                    // Handle blocks updated
                }
                setUser(updateUser);
                if (shouldUpdateFriends) {
                    onFriendsUpdated(updateUser.friends);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userDatabaseReference = userRepository.getDatabaseReference().child(user.key);
        userDatabaseReference.addValueEventListener(userUpdateListener);
    }

    public void removeValueEventListener() {
        if (userUpdateListener != null && user != null) {
            userDatabaseReference.removeEventListener(userUpdateListener);
        }
    }

    private void initFriendList(Map<String, Boolean> keys) {
        userRepository.initMemberList(keys, (error, data) -> {
            if (error == null) {
                friendList = (ArrayList<User>) data[0];
                user.friendList = friendList;
            }
        });
    }

    private void initBlocksList(Map<String, Boolean> keys) {
        userRepository.initMemberList(keys, (error, data) -> {
            if (error == null) {
                blockList = (ArrayList<User>) data[0];;
            }
        });
    }

    public void setUser(User user) {
        this.user = user;
        this.notifyUserUpdated();
        // TODO Temporary set user for ServiceManager
        ServiceManager.getInstance().setCurrentUser(user);
    }

    private void notifyUserUpdated() {
        for (Callback callback : userUpdated) {
            callback.complete(null, user);
        }
    }

    public User getUser() {
        return user;
    }

    public void setAllUsers(ArrayList<User> allUsers) {
        this.allUsers = allUsers;
    }

    public ArrayList<User> getAllUsers() {
        return allUsers;
    }

    public void logout(Context context) {
        userRepository.deleteRefreshToken();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        SubscribeService.unSubscribeFromPushes(context);
        CallService.logout(context);
        removeValueEventListener();
        user = null;
    }

    public void setQbUser(QBUser qbUser) {
        this.qbUser = qbUser;
        startCallService();
    }
}
