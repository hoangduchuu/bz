package com.ping.android.service;

import android.app.Application;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.activity.CallActivity;
import com.ping.android.domain.usecase.call.LoginChatServiceUseCase;
import com.ping.android.utils.Log;
import com.ping.android.utils.SettingsUtil;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/16/18.
 */

public class CallServiceHandlerImpl implements CallServiceHandler, QBRTCClientSessionCallbacks {
    @Inject
    Application context;
    @Inject
    LoginChatServiceUseCase loginChatServiceUseCase;
    Map<String, QBRTCSession> sessionMap;

    private QBRTCClientSessionCallbacks callbacks;

    @Inject
    public CallServiceHandlerImpl() {
        sessionMap = new HashMap<>();
    }

    @Override
    public void loginUser(int qbId, String pingId) {
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
                        QBRTCClient.getInstance(context).prepareToProcessCalls();
                        QBRTCConfig.setDebugEnabled(true);
                        SettingsUtil.configRTCTimers(context);
                        QBRTCClient.getInstance(context).addSessionCallbacksListener(this);
                    }
                });
    }

    @Override
    public void onReceiveNewSession(QBRTCSession session) {
        this.sessionMap.put(session.getSessionID(), session);
        CallActivity.start(context, session.getSessionID(), true);
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        Log.d("onUserNoActions");
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        Log.d("onSessionStartClose");
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        Log.d("onUserNotAnswer");
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d("onCallRejectByUser");
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d("onCallAcceptByUser");
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d("onReceiveHangUpFromUser");
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
