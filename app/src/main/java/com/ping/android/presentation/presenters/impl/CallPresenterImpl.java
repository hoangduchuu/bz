package com.ping.android.presentation.presenters.impl;

import android.content.Intent;
import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.device.Notification;
import com.ping.android.domain.usecase.AddCallHistoryUseCase;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.GetUserByKeyUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.call.InitCallInfoUseCase;
import com.ping.android.domain.usecase.notification.SendMissedCallNotificationUseCase;
import com.ping.android.domain.usecase.notification.SendStartCallNotificationUseCase;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.service.CallServiceHandler;
import com.ping.android.utils.Log;
import com.ping.android.utils.configs.Constant;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCCameraVideoCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.jetbrains.annotations.NotNull;
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
    GetUserByKeyUseCase getUserByKeyUseCase;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    AddCallHistoryUseCase addCallHistoryUseCase;
    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;
    @Inject
    InitCallInfoUseCase initCallInfoUseCase;
    @Inject
    SendStartCallNotificationUseCase sendStartCallNotificationUseCase;
    @Inject
    SendMissedCallNotificationUseCase sendMissedCallNotificationUseCase;
    @Inject
    Notification notification;

    private User currentUser;
    private User opponentUser;
    private QBRTCSession currentSession;
    private String currentUserNickname;
    private String opponentNickname;
    private boolean isIncomingCall;
    private double callTimestamp;
    private boolean callAccepted = false;
    private boolean callStarted = false;

    @Inject
    public CallPresenterImpl() {
    }

    @Override
    public void init(Intent intent, boolean isInComingCall, boolean isVideoCall) {
        this.isIncomingCall = isInComingCall;
        if (isInComingCall) {
            getCurrentUserUseCase.execute(new DefaultObserver<User>() {
                @Override
                public void onNext(User user) {
                    String sessionId = intent.getStringExtra(CallActivity.EXTRA_SESSION_ID);
                    initSession(sessionId, isInComingCall);
                    view.startInComingCall(isVideoCall);
                }

                @Override
                public void onError(@NotNull Throwable exception) {
                    view.finishCall();
                }
            }, null);
        } else {
            opponentUser = intent.getParcelableExtra(CallActivity.EXTRA_OPPONENT_USER);
            if (opponentUser == null) {
                view.finishCall();
                return;
            }
            initCallInfoUseCase.execute(new DefaultObserver<InitCallInfoUseCase.Output>() {
                @Override
                public void onNext(InitCallInfoUseCase.Output output) {
                    currentUser = output.currentUser;
                    currentUserNickname = output.currentUserNickname;
                    opponentNickname = output.opponentNickname;
                    initCall(opponentUser, isVideoCall, isInComingCall);
                    view.startOutgoingCall(opponentUser, isVideoCall);
                    sendStartCallNotification(isVideoCall);
                }

                @Override
                public void onError(@NotNull Throwable exception) {
                    view.finishCall();
                }
            }, opponentUser.key);
        }
    }

    @Override
    public void initSession(String id, boolean isIncomingCall) {
        this.isIncomingCall = isIncomingCall;
        QBRTCSession session = callService.getSession(id);
        if (session != null) {
            if (!isIncomingCall) {
                String avatar = currentUser.profile;
                if (currentUser.settings != null && currentUser.settings.private_profile) {
                    avatar = "";
                }
                String name = TextUtils.isEmpty(currentUserNickname) ? currentUser.getDisplayName() : currentUserNickname;
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("ping_id", currentUser.pingID);
                userInfo.put("user_id", currentUser.key);
                userInfo.put("display_name", name);
                userInfo.put("avatar_url", avatar);
                session.startCall(userInfo);
            } else {
                String opponentId = session.getUserInfo().get("user_id");
                opponentNickname = session.getUserInfo().get("display_name");
                getOpponentInfo(opponentId);
            }
            currentSession = session;
            callTimestamp = System.currentTimeMillis() / 1000;
            view.configCallSettings(session.getOpponents());
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
            Log.d("reject call " + this.currentSession.getState());
            this.currentSession.rejectCall(new HashMap<>());
            view.stopRingtone();
        }
    }

    @Override
    public void accept() {
//        Map<String, String> userInfo = new HashMap<>();
//        userInfo.put("ping_id", currentUser.pingID);
//        userInfo.put("user_id", currentUser.key);
//        userInfo.put("display_name", currentUser.getDisplayName());
//        userInfo.put("avatar_url", avatar);
        this.currentSession.acceptCall(new HashMap<>());
        boolean isVideo = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(this.currentSession.getConferenceType());
        view.initCallViews(isVideo, true);
        view.stopRingtone();
        view.updateAudioSetting(isIncomingCall, isVideo);
        notification.showOngoingCallNotification(this.currentSession.getSessionID());
    }

    @Override
    public void hangup(double duration) {
        if (this.currentSession == null) return;
        this.currentSession.hangUp(new HashMap<>());
        notification.cancelOngoingCall(this.currentSession.getSessionID());
        if (isOugoingCall()) {
            addCallHistory(duration, callAccepted ? Constant.CALL_STATUS_SUCCESS : Constant.CALL_STATUS_MISS);
            if (!callAccepted) {
                sendMissedCallNotification(this.currentSession.getConferenceType()
                        == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
            }
        }
        this.currentSession = null;
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

    @Override
    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    @Override
    public User getOpponentUser() {
        opponentUser.nickName = TextUtils.isEmpty(opponentNickname) ? opponentUser.getDisplayName() : opponentNickname;
        return opponentUser;
    }

    @Override
    public boolean isIncomingCall() {
        return isIncomingCall;
    }

    private void getOpponentInfo(String opponentId) {
        getUserByKeyUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                opponentUser = user;
                view.updateOpponentInfo(user);
            }
        }, opponentId);
    }

    private void addCallHistory(double duration, int status) {
        Call call = new Call(currentUser.key, opponentUser.key, status, callTimestamp, duration);
        call.opponentUser = opponentUser;
        call.isVideo = isVideoCall();
        addCallHistoryUseCase.execute(new DefaultObserver<>(), call);
    }

    @Override
    public void destroy() {
        view = null;
        callService.removeSessionCallbacks();
        //observeCurrentUserUseCase.dispose();
        if (this.currentSession != null) {
            if (callStarted) {
                this.currentSession.hangUp(new HashMap<>());
                notification.cancelOngoingCall(this.currentSession.getSessionID());
                if (isOugoingCall()) {
                    addCallHistory(0, Constant.CALL_STATUS_SUCCESS);
                }
            } else {
                if (isOugoingCall()) {
                    addCallHistory(0, Constant.CALL_STATUS_MISS);
                    // FIXME Currently, it can not add history record if I send miss call notification here
                    //sendMissedCallNotification(isVideoCall());
                    this.currentSession.hangUp(new HashMap<>());
                } else {
                    this.currentSession.rejectCall(new HashMap<>());
                    for (CallActivity.CurrentCallStateCallback callback : currentCallStateCallbackList) {
                        callback.onCallStopped();
                    }
                }
            }
        }
    }

    private boolean isVideoCall() {
        return this.currentSession.getConferenceType() == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
    }

    private boolean isOugoingCall() {
        return !isIncomingCall;
    }

    private void sendStartCallNotification(boolean isVideoCall) {
        sendStartCallNotificationUseCase.execute(new DefaultObserver<Boolean>() {

        }, new SendStartCallNotificationUseCase.Params(opponentUser.quickBloxID, isVideoCall ? "video" : "voice"));
    }

    private void sendMissedCallNotification(boolean isVideoCall) {
        sendMissedCallNotificationUseCase.execute(
                new DefaultObserver<>(),
                new SendMissedCallNotificationUseCase.Params(opponentUser.key,
                        opponentUser.quickBloxID, isVideoCall)
        );
    }

    // region QBRTCSessionStateCallback

    @Override
    public void onStateChanged(BaseSession baseSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }

    @Override
    public void onConnectedToUser(BaseSession baseSession, Integer integer) {
        Log.d("onConnectedToUser " + baseSession.getState().toString());
        callStarted = true;
        view.onCallStarted();
        notification.showOngoingCallNotification(baseSession.getSessionID());
        for (CallActivity.CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    @Override
    public void onDisconnectedFromUser(BaseSession baseSession, Integer integer) {
        Log.d("onDisconnectedFromUser");
        for (CallActivity.CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    @Override
    public void onConnectionClosedForUser(BaseSession baseSession, Integer integer) {
        Log.d("onConnectionClosedForUser");
    }

    // endregion

    // region QBRTCSignalingCallback

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
        Log.d("onSuccessSendingPacket");
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
        //addCallHistory(0, Constant.CALL_STATUS_MISS);
        sendMissedCallNotification(qbrtcSession.getConferenceType()
                == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
        for (CallActivity.CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        //addCallHistory(0, Constant.CALL_STATUS_MISS);
        for (CallActivity.CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        if (this.currentSession != null && this.currentSession.equals(qbrtcSession)) {
            callAccepted = true;
            view.updateAudioSetting(isIncomingCall, isVideoCall());
        }
        //callStarted = true;
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        if (qbrtcSession.equals(this.currentSession)) {
            for (CallActivity.CurrentCallStateCallback callback : currentCallStateCallbackList) {
                callback.onCallStopped();
            }
//            if (!isIncomingCall) {
//                addCallHistory(0, Constant.CALL_STATUS_SUCCESS);
//            }
            notification.cancelOngoingCall(qbrtcSession.getSessionID());
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        if (this.currentSession == null) return;

        if (qbrtcSession.equals(this.currentSession)) {
            view.finishCall();
        }
        this.currentSession = null;
    }
    // endregion
}
