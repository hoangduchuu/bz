package com.ping.android.managers;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.model.User;
import com.ping.android.service.QuickBloxRepository;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.PresenceRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by tuanluong on 12/6/17.
 */

public class UserManager {
    private ArrayList<User> friendList;
    private ArrayList<User> blockList;
    private User user;
    private UserRepository userRepository;
    private PresenceRepository presenceRepository;
    private QuickBloxRepository quickBloxRepository;
    private ValueEventListener userUpdateListener;

    private static UserManager instance;

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() {
        userRepository = new UserRepository();
        quickBloxRepository = new QuickBloxRepository();
        presenceRepository = new PresenceRepository();
        friendList = new ArrayList<>();
        blockList = new ArrayList<>();
    }

    public void listenAuthChange() {

    }

    public void initialize(@NonNull Callback callback) {
        Callback qbCallback = (error, data) -> {
            if (error == null) {
                QBUser qbUser = (QBUser) data[0];
                user.quickBloxID = qbUser.getId();
                userRepository.updateQBId(user.key, qbUser.getId());
                setUser(user);
                initFriendList(user.friends);
            }
            callback.complete(error, data);
        };
        removeValueEventListener();
        userRepository.initializeUser((error, data) -> {
            if (error == null) {
                user = (User) data[0];
                if (user.quickBloxID <= 0) {
                    quickBloxRepository.signUpNewUserQB(user, qbCallback);
                } else {
                    quickBloxRepository.signInCreatedUser(user, qbCallback);
                }
            } else {
                callback.complete(error, data);
            }
        });
        presenceRepository.listenStatusChange((error, data) -> {
            if (error == null) {
                userRepository.registerUserPresence();
            }
        });
    }

    private void onBlocksUpdated(Map<String, Object> blocks) {
        blockList = new ArrayList<>();
    }

    private void onFriendsUpdated(Map<String, Boolean> friends) {
        friendList = new ArrayList<>();
        initFriendList(friends);
    }

    private void addValueEventListener() {
        if (user == null) return;

        userUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User updateUser = new User(dataSnapshot);
                if (user.friendList.size() != updateUser.friendList.size()) {
                    // Handle friends updated
                    onFriendsUpdated(updateUser.friends);
                }
                if (user.blocks.size() != updateUser.blocks.size()) {
                    // Handle blocks updated
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRepository.getDatabaseReference().child(user.key)
                .addValueEventListener(userUpdateListener);
    }

    private void removeValueEventListener() {
        if (userUpdateListener != null && user != null) {
            userRepository.getDatabaseReference().child(user.key).removeEventListener(userUpdateListener);
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

    private void setUser(User user) {
        this.user = user;
        this.addValueEventListener();
        // TODO Temporary set user for ServiceManager
        ServiceManager.getInstance().setCurrentUser(user);
    }

    public User getUser() {
        return user;
    }
}
