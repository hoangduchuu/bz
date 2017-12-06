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
import com.quickblox.users.model.QBUser;

/**
 * Created by tuanluong on 12/6/17.
 */

public class QuickBloxRepository {
    private QBResRequestExecutor requestExecutor;

    public QuickBloxRepository() {
        requestExecutor = new QBResRequestExecutor();
    }

    public void getQBUser(User user, @NonNull Callback callback) {
        QBUser qbUser = new QBUser();
        StringifyArrayList<String> userTags = new StringifyArrayList<>();
        userTags.add(Constant.QB_PING_ROOM);
        qbUser.setId(user.quickBloxID);
//        qbUser.setFullName(currentUser.pingID);
//        qbUser.setEmail(currentUser.email);
        qbUser.setLogin(user.pingID);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
//        qbUser.setTags(userTags);

        callback.complete(null, qbUser);
    }

    public void signUpNewUserQB(@NonNull User user, @NonNull Callback callback) {
        QBUser qbUser = new QBUser();

        StringifyArrayList<String> userTags = new StringifyArrayList<>();
        userTags.add(Constant.QB_PING_ROOM);
//        qbUser.setFullName(currentUser.pingID);
//        qbUser.setEmail(currentUser.email);
        qbUser.setLogin(user.pingID);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
//        qbUser.setTags(userTags);

        requestExecutor.signUpNewUser(qbUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        //updateQuickBlox(result.getId());
                        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
                        signInCreatedUser(qbUser, callback);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(qbUser, callback);
                        } else {
                            Toaster.longToast(R.string.sign_up_error);
                            callback.complete(new Error());
                        }
                    }
                }
        );
    }

    private void signInCreatedUser(final QBUser user, Callback callback) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                //currentQBUser = result;
                //result.setPassword(Consts.DEFAULT_USER_PASSWORD);
                //saveQBUserData(result);
                result.setPassword(Consts.DEFAULT_USER_PASSWORD);
                //startCallService(result);
                //getAllQBUsers();
                callback.complete(null, user);
            }

            @Override
            public void onError(QBResponseException responseException) {
                Toaster.longToast(R.string.sign_up_error);
                callback.complete(new Error());
            }
        });
    }
}
