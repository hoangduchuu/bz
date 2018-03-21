package com.ping.android.service;

import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/16/18.
 */

public interface CallServiceHandler {
    void create();

    void loginUser(int qbId, String pingId);

    void logout();

    void destroy();

    QBRTCSession getSession(String id);

    Observable<String> startNewSession(ArrayList<Integer> opponents, boolean isVideo);

    void registerSessionCallbacks(QBRTCClientSessionCallbacks callbacks);

    void removeSessionCallbacks();
}
