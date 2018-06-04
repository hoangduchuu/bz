package com.ping.android.presentation.presenters.impl;

import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.AudioCallPresenter;
import com.ping.android.presentation.presenters.CallPresenter;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/20/18.
 */

public class AudioCallPresenterImpl implements AudioCallPresenter, CallActivity.CurrentCallStateCallback {
    @Inject
    AudioCallPresenter.View view;
    @Inject
    CallPresenter presenter;
    private QBRTCSession currentSession;

    @Inject
    public AudioCallPresenterImpl() {}

    @Override
    public void create() {
        QBRTCSession session = presenter.getCurrentSession();
        if (session != null) {
            currentSession = session;
        }
        User opponentUser = presenter.getOpponentUser();
        if (opponentUser != null) {
            view.updateOpponentInfo(opponentUser);
        }
        presenter.registerCallStateListener(this);
    }

    @Override
    public void destroy() {
        presenter.removeCallStateCallback(this);
    }

    @Override
    public void hangup(double duration) {
        presenter.hangup(duration);
    }

    @Override
    public void toggleAudio(boolean isEnable) {
        presenter.toggleAudio(isEnable);
    }

    @Override
    public void onCallStarted() {
        view.onCallStarted();
        view.initAudio(this.currentSession.getConferenceType() == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
    }

    @Override
    public void onCallStopped() {
        view.onCallStopped();
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        view.onOpponentsListUpdated(newUsers);
    }
}
