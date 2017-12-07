package com.ping.android.managers;

import android.support.annotation.NonNull;

import com.ping.android.model.User;
import com.ping.android.service.QuickBloxRepository;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.quickblox.users.model.QBUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by tuanluong on 12/6/17.
 */

public class UserManager {
    private ArrayList<User> friendList;
    private User user;
    private UserRepository userRepository;
    private QuickBloxRepository quickBloxRepository;

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
    }

    public void initialize(@NonNull Callback callback) {
        Callback qbCallback = (error, data) -> {
            if (error == null) {
                QBUser qbUser = (QBUser) data[0];
                user.quickBloxID = qbUser.getId();
                userRepository.updateQBId(user.key, qbUser.getId());
                setUser(user);
                initFriendList(user.friends.keySet());
            }
            callback.complete(error, data);
        };
        userRepository.initializeUser((error, data) -> {
            if (error == null) {
                user = (User) data[0];
                if (user.quickBloxID <= 0) {
                    quickBloxRepository.signUpNewUserQB(user, qbCallback);
                } else {
                    quickBloxRepository.getQBUser(user, qbCallback);
                }
            } else {
                callback.complete(error, data);
            }
        });
    }

    public void initFriendList(Set<String> keys) {
        friendList = new ArrayList<>();
        for (String userID : keys) {
            userRepository.getUser(userID, (error, data) -> {
                if (error == null) {
                    User user = (User) data[0];
                    friendList.add(user);
                    user.friendList = friendList;
                }
            });
        }
    }

    private void setUser(User user) {
        this.user = user;
        // TODO Temporary set user for ServiceManager
        ServiceManager.getInstance().setCurrentUser(user);
    }

    public User getUser() {
        return user;
    }
}
