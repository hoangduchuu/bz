package com.ping.android.model;

import static com.ping.android.utils.configs.Constant.CallStatus;

public class Call {
    public String key;
    public String senderId;
    public String receiveId;
    @CallStatus
    public int status;
    public double timestamp;
    public double callDuration; // in seconds

    // region local properties

    public User opponentUser;
    public String opponentName;
    public String conversationId;
    public CallType type;
    public boolean isVideo = false;

    // endregion

    public Call() {}

    public Call(String senderId, String receiveId, int status, double timestamp, double callDuration) {
        this.senderId = senderId;
        this.receiveId = receiveId;
        this.status = status;
        this.timestamp = timestamp;
        this.callDuration = callDuration;
    }

    public enum CallType {
        OUTGOING, INCOMING, MISSED
    }
}
