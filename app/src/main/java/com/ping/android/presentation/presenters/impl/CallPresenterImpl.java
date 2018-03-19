package com.ping.android.presentation.presenters.impl;

import com.ping.android.activity.CallActivity;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.service.CallServiceHandler;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.WebRtcSessionManager;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/16/18.
 */

public class CallPresenterImpl implements CallPresenter, QBRTCSessionStateCallback, QBRTCSignalingCallback,
        QBRTCClientSessionCallbacks {
    private ArrayList<CallActivity.CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();

    @Inject
    View view;
    @Inject
    CallServiceHandler callService;
    QBRTCSession currentSession;

    @Inject
    public CallPresenterImpl() {}

    @Override
    public void initSession(String id) {
        QBRTCSession session = callService.getSession(id);
        if (session != null) {
            currentSession = session;
            WebRtcSessionManager.getInstance().setCurrentSession(session);
            view.configCallSettings(session.getOpponents());
            boolean isVideo = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(session.getConferenceType());
            view.initAudioSettings(isVideo);
            callService.registerSessionCallbacks(this);
            session.addSessionCallbacksListener(this);
            session.addSignalingCallback(this);
        } else {
            view.finishCall();
        }
    }

    @Override
    public void reject() {
        if (this.currentSession != null) {
            this.currentSession.rejectCall(new HashMap<>());
        }
    }

    @Override
    public void accept() {
        boolean isVideo = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(this.currentSession.getConferenceType());;
        view.initCallViews(isVideo, true);
    }

    @Override
    public void hangup() {
        // TODO add history
//        if (!isInCommingCall) {
//            insertCallHistory(Constant.CALL_STATUS_MISS);
//        }
        this.currentSession.hangUp(new HashMap<>());
        view.finishCall();
    }

    @Override
    public void toggleAudio(boolean isAudioEnabled) {
        this.currentSession.getMediaStreamManager().getLocalAudioTrack().setEnabled(isAudioEnabled);
    }

    @Override
    public void registerCallStateListener(CallActivity.CurrentCallStateCallback callback) {
        currentCallStateCallbackList.add(callback);
    }

    @Override
    public void removeCallStateCallback(CallActivity.CurrentCallStateCallback callback) {
        currentCallStateCallbackList.remove(callback);
    }

    @Override
    public void destroy() {
        callService.removeSessionCallbacks();
    }

    // region QBRTCSessionStateCallback

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        for (CallActivity.CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    // endregion

    // region QBRTCSignalingCallback

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {

    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer, QBRTCSignalException e) {

    }

    // endregion

    // region QBRTCClientSessionCallbacks

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {

    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        for (CallActivity.CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        if (qbrtcSession.equals(this.currentSession)) {
            this.currentSession.hangUp(new HashMap<>());
            view.finishCall();
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        if (this.currentSession == null) return;

        if (qbrtcSession.getSessionID().equals(this.currentSession.getSessionID())) {
            view.finishCall();
        }
    }

    // endregion
}
