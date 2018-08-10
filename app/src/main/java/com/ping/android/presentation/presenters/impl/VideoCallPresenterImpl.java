package com.ping.android.presentation.presenters.impl;

import com.ping.android.model.User;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.presenters.VideoCallPresenter;
import com.ping.android.presentation.view.activity.CallActivity;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/20/18.
 */

public class VideoCallPresenterImpl implements VideoCallPresenter, CallActivity.CurrentCallStateCallback, QBRTCClientVideoTracksCallbacks {
    @Inject
    View view;
    @Inject
    CallPresenter presenter;
    private QBRTCSession currentSession;

    @Inject
    public VideoCallPresenterImpl() {
    }

    @Override
    public void create() {
        QBRTCSession session = presenter.getCurrentSession();
        if (session != null) {
            currentSession = session;
            currentSession.addVideoTrackCallbacksListener(this);
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
        if (currentSession != null) {
            currentSession.removeVideoTrackCallbacksListener(this);
        }
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

    @Override
    public void onLocalVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack) {
        view.onLocalVideoTrackReceive(baseSession, qbrtcVideoTrack);
    }

    @Override
    public void onRemoteVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        view.onRemoteVideoTrackReceive(baseSession, qbrtcVideoTrack, integer);
    }
}
