package com.ping.android.managers;

import com.google.firebase.auth.FirebaseAuth;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.SharedPrefsHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuanluong on 12/6/17.
 */

public class UserManager {
    private User user;
    private Map<String, Boolean> friends;
    // Currently, users will be cached for later use.
    // Should improve by invalidate user after a certain of time
    private Map<String, User> cachedUsers = new HashMap<>();

    public UserManager() { }

    public void setUser(User user) {
        this.user = user;
        SharedPrefsHelper.getInstance().save("isLoggedIn", true);
        SharedPrefsHelper.getInstance().save("quickbloxId", user.quickBloxID);
        SharedPrefsHelper.getInstance().save("pingId", user.pingID);
        // TODO Temporary set opponentUser for ServiceManager
        ServiceManager.getInstance().setCurrentUser(user);
    }

    public void logout() {
        SharedPrefsHelper.getInstance().save("isLoggedIn", false);
        SharedPrefsHelper.getInstance().save("quickbloxId", 0);
        SharedPrefsHelper.getInstance().save("pingId", "");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        user = null;
    }
}
