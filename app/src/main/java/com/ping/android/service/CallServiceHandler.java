package com.ping.android.service;

import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;

/**
 * Created by tuanluong on 3/16/18.
 */

public interface CallServiceHandler {
    void loginUser(int qbId, String pingId);

    void destroy();

    void registerSessionCallbacks(QBRTCClientSessionCallbacks callbacks);

    void removeSessionCallbacks();

    QBRTCSession getSession(String id);
}
