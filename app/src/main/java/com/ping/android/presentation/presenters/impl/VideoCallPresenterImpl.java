package com.ping.android.presentation.presenters.impl;

import com.ping.android.activity.CallActivity;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.AudioCallPresenter;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.presenters.VideoCallPresenter;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;

import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/20/18.
 */

public class VideoCallPresenterImpl implements VideoCallPresenter, CallActivity.CurrentCallStateCallback {
    @Inject
    View view;
    @Inject
    CallPresenter presenter;
    private QBRTCSession currentSession;

    @Inject
    public VideoCallPresenterImpl() {}

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
        if (!presenter.isIncomingCall()) {
            view.playRingtone();
        }
        presenter.registerCallStateListener(this);
    }

    @Override
    public void destroy() {
        presenter.removeCallStateCallback(this);
    }

    @Override
    public void hangup() {
        presenter.hangup();
    }

    @Override
    public void toggleAudio(boolean isEnable) {
        presenter.toggleAudio(isEnable);
    }

    @Override
    public void switchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        presenter.switchCamera(cameraSwitchHandler);
    }

    @Override
    public void toggleVideoLocal(boolean isNeedEnableCam) {
        currentSession.getMediaStreamManager().getLocalVideoTrack().setEnabled(isNeedEnableCam);
    }

    @Override
    public void onCallStarted() {
        view.onCallStarted();
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
