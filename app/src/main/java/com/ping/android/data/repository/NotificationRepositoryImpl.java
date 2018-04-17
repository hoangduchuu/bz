package com.ping.android.data.repository;

import com.bzzz.rxquickblox.RxJava2PerformProcessor;
import com.google.gson.JsonObject;
import com.ping.android.BuildConfig;
import com.ping.android.domain.repository.NotificationRepository;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.ultility.Constant;
import com.quickblox.core.server.Performer;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBEventType;
import com.quickblox.messages.model.QBNotificationType;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/22/18.
 */

public class NotificationRepositoryImpl implements NotificationRepository {
    private QBEnvironment environment = BuildConfig.DEBUG ? QBEnvironment.DEVELOPMENT: QBEnvironment.PRODUCTION;

    @Inject
    public NotificationRepositoryImpl() {}

    @Override
    public Observable<Boolean> sendCallingNotificationToUser(int quickBloxId, String callType) {
        // FIXME: this type of notification just notify opponent user to start call service in case of turned off
        String messageData = String.format("%s is %s calling.", "", callType);
        JsonObject object = new JsonObject();
        object.addProperty("data", messageData);
        object.addProperty("message", messageData);
        //object.addProperty("ios_badge", "1");
        object.addProperty("ios_sound", "default");
        object.addProperty("ios_voip", 1);
        object.addProperty("VOIPCall", 1);
        object.addProperty("notificationType", "incoming_call");
        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(quickBloxId);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        event.setEnvironment(environment);
        return createQBEvent(event);
    }

    @Override
    public Observable<Boolean> sendMissedCallNotificationToUser(String senderId, String senderProfileImage, String body,
                                                                int quickBloxId, boolean isVideo, int badgeNumber) {
        JsonObject object = new JsonObject();
        object.addProperty("data", body);
        object.addProperty("message", body);
        object.addProperty("ios_badge", badgeNumber + 1);
        object.addProperty("ios_sound", "default");
        object.addProperty("ios_content_available", 1);
        object.addProperty("notificationType", "missed_call");
        object.addProperty("senderId", senderId);
        object.addProperty("senderProfile", senderProfileImage);
        object.addProperty("isVideo", isVideo ? 1 : 0);
        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(quickBloxId);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        event.setEnvironment(environment);
        return createQBEvent(event);
    }

    @Override
    public Observable<Boolean> sendMessageNotification(String senderId, String senderProfileImage, String body,
                                                       String conversationId, Message message,
                                                       User user, int badgeNumber) {
        JsonObject object = new JsonObject();
        object.addProperty("data", body);
        object.addProperty("message", body);
        object.addProperty("ios_sound", "default");
        object.addProperty("ios_content_available", 1);
        object.addProperty("ios_category", "incoming_message");
        object.addProperty("notificationType", "incoming_message");
        object.addProperty("timestamp", message.timestamp);
        object.addProperty("originMessage", message.message);
        object.addProperty("senderName", message.senderName);
        object.addProperty("conversationId", conversationId);
        object.addProperty("photoUrl", message.photoUrl);
        object.addProperty("thumbUrl", message.thumbUrl);
        object.addProperty("audioUrl", message.audioUrl);
        object.addProperty("gameUrl", message.gameUrl);
        object.addProperty("senderId", senderId);
        object.addProperty("senderProfile", senderProfileImage);
        object.addProperty("messageType", message.messageType);
        object.addProperty("ios_badge", badgeNumber + 1);

        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(user.quickBloxID);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        event.setEnvironment(environment);
        //this notification will send to both android and ios
        return createQBEvent(event);
    }

    @Override
    public Observable<Boolean> sendGameStatusNotificationToSender(String senderId, String displayName,
                                                                  int opponentQbId, String conversationId,
                                                                  String body, int badgeNumber) {
        JsonObject object = new JsonObject();
        object.addProperty("data", body);
        object.addProperty("message", body);
        object.addProperty("ios_sound", "default");
        object.addProperty("ios_content_available", 1);
        object.addProperty("ios_category", "game_status");
        object.addProperty("notificationType", "game_status");
        object.addProperty("senderName", displayName);
        object.addProperty("conversationId", conversationId);
        object.addProperty("senderId", senderId);
        object.addProperty("messageType", Constant.MSG_TYPE_GAME);
        object.addProperty("ios_badge", badgeNumber + 1);
        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(opponentQbId);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        event.setEnvironment(environment);
        //this notification will send to both android and ios
        return createQBEvent(event);
    }

    private Observable<Boolean> createQBEvent(QBEvent event) {
        Performer<QBEvent> performer = QBPushNotifications.createEvent(event);
        return ((Observable<QBEvent>) performer.convertTo(RxJava2PerformProcessor.INSTANCE))
                .map(qbEvent -> true);
    }
}
