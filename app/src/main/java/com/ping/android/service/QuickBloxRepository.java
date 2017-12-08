package com.ping.android.service;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ping.android.activity.R;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.ultility.Consts;
import com.ping.android.util.QBResRequestExecutor;
import com.ping.android.utils.Toaster;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

/**
 * Created by tuanluong on 12/6/17.
 */

public class QuickBloxRepository {

    public QBUser getQBUser(User user) {
        QBUser qbUser = new QBUser();
        StringifyArrayList<String> userTags = new StringifyArrayList<>();
        userTags.add(Constant.QB_PING_ROOM);
        qbUser.setId(user.quickBloxID);
        qbUser.setLogin(user.pingID);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
        return qbUser;
    }

    public void signUpNewUserQB(@NonNull User user, @NonNull Callback callback) {
        QBUser qbUser = new QBUser();

        StringifyArrayList<String> userTags = new StringifyArrayList<>();
        userTags.add(Constant.QB_PING_ROOM);
        qbUser.setLogin(user.pingID);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
        QBUsers.signUpSignInTask(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        result.setPassword(Consts.DEFAULT_USER_PASSWORD);
                        callback.complete(null, result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(user, callback);
                        } else {
                            Toaster.longToast(R.string.sign_up_error);
                            callback.complete(new Error());
                        }
                    }
                }
        );
    }

    public void signInCreatedUser(final User user, Callback callback) {
        QBUser qbUser = getQBUser(user);
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
                callback.complete(null, qbUser);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.complete(e);
            }
        });
    }
}
