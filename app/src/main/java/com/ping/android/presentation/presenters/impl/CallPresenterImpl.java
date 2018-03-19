package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.activity.CallActivity;
import com.ping.android.domain.usecase.AddCallHistoryUseCase;
import com.ping.android.domain.usecase.GetUserByQuickBloxIdUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.service.CallServiceHandler;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.WebRtcSessionManager;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.webrtc.CameraVideoCapturer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/16/18.
 */

public class CallPresenterImpl implements CallPresenter,
        QBRTCSessionStateCallback, QBRTCSignalingCallback, QBRTCClientSessionCallbacks {
    private ArrayList<CallActivity.CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();

    @Inject
    View view;
    @Inject
    CallServiceHandler callService;
    @Inject
    GetUserByQuickBloxIdUseCase getUserByQuickBloxIdUseCase;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    AddCallHistoryUseCase addCallHistoryUseCase;

    private User currentUser;
    private User opponentUser;
    private QBRTCSession currentSession;
    private boolean isIncomingCall;
    private double callTimestamp;
    private boolean callAccepted = false;

    @Inject
    public CallPresenterImpl() {}

    @Override
    public void initSession(String id, boolean isIncommingCall) {
        this.isIncomingCall = isIncommingCall;
        observeCurrentUser();
        QBRTCSession session = callService.getSession(id);
        if (session != null) {
            currentSession = session;
            callTimestamp = System.currentTimeMillis() / 1000;
            getOpponentInfo();
            WebRtcSessionManager.getInstance().setCurrentSession(session);
            view.configCallSettings(session.getOpponents());
            boolean isVideo = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(session.getConferenceType());
            view.initAudioSettings(isVideo);
            view.initUserData(currentSession.getCallerID(), currentSession.getOpponents());
            callService.registerSessionCallbacks(this);
            session.addSessionCallbacksListener(this);
            session.addSignalingCallback(this);
        } else {
            view.finishCall();
        }
    }

    @Override
    public void initCall(User opponentUser, boolean isVideoCall, boolean isIncommingCall) {
        this.isIncomingCall = isIncommingCall;
        this.opponentUser = opponentUser;
        ArrayList<Integer> opponentIds = new ArrayList<>();
        opponentIds.add(opponentUser.quickBloxID);
        callService.startNewSession(opponentIds, isVideoCall)
                .subscribe(s -> initSession(s, isIncommingCall));
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
        if (!isIncomingCall) {
            addCallHistory(callAccepted ? Constant.CALL_STATUS_SUCCESS : Constant.CALL_STATUS_MISS);
        }
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
    public void switchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        ((QBRTCCameraVideoCapturer) (currentSession.getMediaStreamManager().getVideoCapturer()))
                .switchCamera(cameraSwitchHandler);
    }

    private void observeCurrentUser() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
            }
        }, null);
    }

    private void getOpponentInfo() {
        if (this.currentSession == null) return;
        int qbId = isIncomingCall ? currentSession.getCallerID() : currentSession.getOpponents().get(0);
        getUserByQuickBloxIdUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                opponentUser = user;
                view.updateOpponentInfo(user);
            }
        }, qbId);
    }

    private void addCallHistory(int status) {
        Call call = new Call(currentUser.key, opponentUser.key, status, callTimestamp);
        addCallHistoryUseCase.execute(new DefaultObserver<>(), call);
    }

    @Override
    public void destroy() {
        callService.removeSessionCallbacks();
        observeCurrentUserUseCase.dispose();
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
        view.showErrorSendingPacket();
        e.printStackTrace();
    }

    // endregion

    // region QBRTCClientSessionCallbacks

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        if (!this.currentSession.equals(qbrtcSession)) {
            qbrtcSession.rejectCall(new HashMap<>());
        }
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
        view.stopRingtone();
        addCallHistory(Constant.CALL_STATUS_MISS);
        view.sendMissedCallNotification(opponentUser.key, opponentUser.quickBloxID);
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        view.stopRingtone();
        addCallHistory(Constant.CALL_STATUS_MISS);
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        view.stopRingtone();
        callAccepted = true;
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

        if (qbrtcSession.equals(this.currentSession)) {
            view.finishCall();
        }
    }

    // endregion
}
