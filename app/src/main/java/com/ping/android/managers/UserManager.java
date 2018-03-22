package com.ping.android.managers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.ping.android.model.User;
import com.ping.android.service.CallService;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Consts;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

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

    public void startCallService(Activity activity) {
        Integer qbId = SharedPrefsHelper.getInstance().get("quickbloxId");
        String pingId = SharedPrefsHelper.getInstance().get("pingId");
        CallService.start(activity, qbId, pingId);
    }

    public void setUser(User user) {
        this.user = user;
        SharedPrefsHelper.getInstance().save("quickbloxId", user.quickBloxID);
        SharedPrefsHelper.getInstance().save("pingId", user.pingID);
        // TODO Temporary set opponentUser for ServiceManager
        ServiceManager.getInstance().setCurrentUser(user);
    }

    public User getUser() {
        return user;
    }

    public void logout() {
        SharedPrefsHelper.getInstance().save("quickbloxId", 0);
        SharedPrefsHelper.getInstance().save("pingId", "");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        user = null;
    }
}
