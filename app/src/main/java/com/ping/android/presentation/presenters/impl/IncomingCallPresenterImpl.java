package com.ping.android.presentation.presenters.impl;

import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.presenters.IncomingCallPresenter;
import com.ping.android.presentation.view.activity.CallActivity;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/20/18.
 */

public class IncomingCallPresenterImpl implements IncomingCallPresenter, CallActivity.CurrentCallStateCallback {
    @Inject
    IncomingCallPresenter.View view;
    @Inject
    CallPresenter callPresenter;
    private QBRTCSession currentSession;

    @Inject
    public IncomingCallPresenterImpl() {}

    @Override
    public void create() {
        QBRTCSession session = callPresenter.getCurrentSession();
        if (session != null) {
            currentSession = session;
            Map<String, String> userInfo = session.getUserInfo();
            String displayName = userInfo.get("display_name");
            String avatar = userInfo.get("avatar_url");
            view.showOpponentInfo(displayName, avatar);
            view.showConferenceType(session.getConferenceType());
        }
        callPresenter.registerCallStateListener(this);
    }

    @Override
    public void destroy() {
        callPresenter.removeCallStateCallback(this);
    }

    @Override
    public void reject() {
        callPresenter.reject();
        view.stopCallNotification();
    }

    @Override
    public void accept() {
        callPresenter.accept();
        view.stopCallNotification();
    }

    @Override
    public void onCallStarted() {

    }

    @Override
    public void onCallStopped() {
        view.stopCallNotification();
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {

    }
}
