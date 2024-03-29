package com.ping.android.service;

import android.app.Application;
import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.call.LoginChatServiceUseCase;
import com.ping.android.domain.usecase.call.LogoutChatServiceUseCase;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.utils.Log;
import com.ping.android.utils.SettingsUtil;
import com.ping.android.utils.SharedPrefsHelper;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/16/18.
 */

public class CallServiceHandlerImpl implements CallServiceHandler, QBRTCClientSessionCallbacks {
    @Inject
    Application context;
    @Inject
    LoginChatServiceUseCase loginChatServiceUseCase;
    @Inject
    LogoutChatServiceUseCase logoutChatServiceUseCase;

    Map<String, QBRTCSession> sessionMap;

    private QBRTCClientSessionCallbacks callbacks;

    @Inject
    public CallServiceHandlerImpl() {
        sessionMap = new HashMap<>();
    }

    @Override
    public void create() {
        Integer qbId = SharedPrefsHelper.getInstance().get("quickbloxId");
        String pingId = SharedPrefsHelper.getInstance().get("pingId");
        if (qbId != null && qbId > 0 && !TextUtils.isEmpty(pingId)) {
            loginUser(qbId, pingId);
        }
    }

    @Override
    public void loginUser(int qbId, String pingId) {
        if (QBChatService.getInstance().isLoggedIn()) {
            QBUser qbUser = QBChatService.getInstance().getUser();
            if (qbUser.getId() == qbId) {
                return;
            }
        }
        loginChatServiceUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    init();
                }
            }
        }, new LoginChatServiceUseCase.Params(qbId, pingId));
    }

    @Override
    public void logout() {
        logoutChatServiceUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                QBChatService.getInstance().destroy();
                QBRTCClient.getInstance(context).destroy();
            }
        }, null);
    }

    @Override
    public Observable<String> startNewSession(ArrayList<Integer> opponents, boolean isVideo) {
        QBRTCTypes.QBConferenceType conferenceType = isVideo
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(context);

        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponents, conferenceType);
        sessionMap.put(newQbRtcSession.getSessionID(), newQbRtcSession);
        return Observable.just(newQbRtcSession.getSessionID());
    }

    @Override
    public void destroy() {
        QBRTCClient.getInstance(context).removeSessionsCallbacksListener(this);
    }

    @Override
    public void registerSessionCallbacks(QBRTCClientSessionCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void removeSessionCallbacks() {
        this.callbacks = null;
    }

    @Override
    public QBRTCSession getSession(String id) {
        return sessionMap.get(id);
    }

    private void init() {
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager()
                .addSignalingManagerListener((qbSignaling, createdLocally) -> {
                    if (!createdLocally) {
                        QBRTCClient.getInstance(context).addSignaling((QBWebRTCSignaling) qbSignaling);
                    }
                });
        QBRTCClient.getInstance(context).prepareToProcessCalls();
        QBRTCConfig.setDebugEnabled(true);
        SettingsUtil.configRTCTimers(context);
        QBRTCClient.getInstance(context).addSessionCallbacksListener(this);
    }

    @Override
    public void onReceiveNewSession(QBRTCSession session) {
        Log.d("onReceiveNewSession " + session.getState().toString());
        this.sessionMap.put(session.getSessionID(), session);
        if (callbacks != null) {
            callbacks.onReceiveNewSession(session);
        } else {
            boolean isVideoCall = session.getConferenceType() == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
            CallActivity.start(context, session.getSessionID(), true, isVideoCall);
        }
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        Log.d("onUserNoActions " + qbrtcSession.getState().toString());
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        Log.d("onSessionStartClose " + qbrtcSession.getState().toString());
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        Log.d("onUserNotAnswer " + qbrtcSession.getState().toString());
        if (callbacks != null) {
            callbacks.onUserNotAnswer(qbrtcSession, integer);
        }
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d("onCallRejectByUser " + qbrtcSession.getState().toString());
        if (callbacks != null) {
            callbacks.onCallRejectByUser(qbrtcSession, integer, map);
        }
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d("onCallAcceptByUser " + qbrtcSession.getState().toString());
        if (callbacks != null) {
            callbacks.onCallAcceptByUser(qbrtcSession, integer, map);
        }
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d("onReceiveHangUpFromUser " + qbrtcSession.getState().toString());
        if (callbacks != null) {
            callbacks.onReceiveHangUpFromUser(qbrtcSession, integer, map);
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        Log.d("onSessionClosed");
        this.sessionMap.remove(qbrtcSession.getSessionID());
        if (callbacks != null) {
            callbacks.onSessionClosed(qbrtcSession);
        }
    }
}
