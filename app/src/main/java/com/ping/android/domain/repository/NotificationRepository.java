package com.ping.android.domain.repository;

import com.ping.android.model.Message;
import com.ping.android.model.User;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/22/18.
 */

public interface NotificationRepository {
    Observable<Boolean> sendCallingNotificationToUser(int quickBloxId, String callType);

    Observable<Boolean> sendMissedCallNotificationToUser(String senderId, String profileImage, String body,
                                                         int quickBloxId, boolean isVideo, int badgeNumber);

    Observable<Boolean> sendMessageNotification(String senderId, String senderProfile,
                                                String body, String conversationId,
                                                User user, int badgeNumber);

    Observable<Boolean> sendGameStatusNotificationToSender(String senderId, String displayName,
                                                           int opponentQbId, String conversationId,
                                                           String body, int badgeNumber);
}
