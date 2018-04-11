package com.ping.android.device;

import com.ping.android.model.User;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

/**
 * Created by tuanluong on 3/27/18.
 */

public interface Notification {
    void showOngoingCallNotification(String tag);

    void cancelOngoingCall(String tag);

    void showMissedCallNotification(String opponentUserId, String opponentProfile, String message,
                                    boolean isVideo, String tag, boolean enableSound);

    void showMessageNotification(User user, String message, String conversationId, String senderProfile);
}
