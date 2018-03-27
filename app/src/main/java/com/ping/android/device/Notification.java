package com.ping.android.device;

/**
 * Created by tuanluong on 3/27/18.
 */

public interface Notification {
    void showOngoingCallNotification(String tag);

    void cancelOngoingCall(String tag);
}
