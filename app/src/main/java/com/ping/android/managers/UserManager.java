package com.ping.android.managers;

import android.app.Activity;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.ping.android.model.User;
import com.ping.android.service.CallService;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.SharedPrefsHelper;

/**
 * Created by tuanluong on 12/6/17.
 */

public class UserManager {
    private User user;

    private static UserManager instance;

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() {
    }

    public void startCallService(Context context) {
        Integer qbId = SharedPrefsHelper.getInstance().get("quickbloxId");
        String pingId = SharedPrefsHelper.getInstance().get("pingId");
        CallService.start(context, qbId, pingId);
    }

    public void setUser(User user) {
        this.user = user;
        SharedPrefsHelper.getInstance().save("quickbloxId", user.quickBloxID);
        SharedPrefsHelper.getInstance().save("pingId", user.pingID);
        // TODO Temporary set opponentUser for ServiceManager
        ServiceManager.getInstance().setCurrentUser(user);
    }

    public void logout() {
        SharedPrefsHelper.getInstance().save("quickbloxId", 0);
        SharedPrefsHelper.getInstance().save("pingId", "");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        user = null;
    }
}
