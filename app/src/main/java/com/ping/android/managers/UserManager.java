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
import com.quickblox.users.model.QBUser;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanluong on 12/6/17.
 */

public class UserManager {
    private User user;
    private QBUser qbUser;
    private List<Callback> userUpdated;

    private static UserManager instance;

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() {
        userUpdated = new ArrayList<>();
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

    public void startCallService(Activity activity) {
        Intent tempIntent = new Intent(activity.getApplicationContext(), CallService.class);
        PendingIntent pendingIntent = activity.createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(activity, qbUser, pendingIntent);
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

    public void logout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        user = null;
    }

    public void setQbUser(QBUser qbUser) {
        this.qbUser = qbUser;
    }
}
