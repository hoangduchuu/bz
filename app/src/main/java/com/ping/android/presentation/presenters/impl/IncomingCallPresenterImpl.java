package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.presenters.IncomingCallPresenter;
import com.quickblox.videochat.webrtc.QBRTCSession;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/20/18.
 */

public class IncomingCallPresenterImpl implements IncomingCallPresenter {
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
    }

    @Override
    public void reject() {
        callPresenter.reject();
    }

    @Override
    public void accept() {
        callPresenter.accept();
    }
}
