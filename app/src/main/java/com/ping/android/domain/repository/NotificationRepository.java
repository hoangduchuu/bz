package com.ping.android.domain.repository;

import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

/**
 * Created by tuanluong on 3/22/18.
 */

public interface NotificationRepository {
    Observable<Boolean> sendCallingNotificationToUser(int quickBloxId, String callType);

    Observable<Boolean> sendMissedCallNotificationToUser(int quickBloxId, String callType, int badgeNumber);

    Observable<Boolean> sendMessageNotification(String senderName, Conversation conversation, Message message,
                                                User user, int badgeNumber);

    Observable<Boolean> sendGameStatusNotificationToSender(String senderId, String displayName,
                                                           int opponentQbId, String conversationId,
                                                           String body, int badgeNumber);
}
