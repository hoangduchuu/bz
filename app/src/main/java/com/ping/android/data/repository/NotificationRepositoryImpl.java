package com.ping.android.data.repository;

import com.bzzz.rxquickblox.RxJava2PerformProcessor;
import com.google.gson.JsonObject;
import com.ping.android.domain.repository.NotificationRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
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
    @Inject
    public NotificationRepositoryImpl() {}

    @Override
    public Observable<Boolean> sendCallingNotificationToUser(int quickBloxId, String callType) {
        String messageData = String.format("%s is %s calling.", UserManager.getInstance().getUser().getDisplayName(), callType);
        JsonObject object = new JsonObject();
        object.addProperty("data", messageData);
        object.addProperty("message", messageData);
        //object.addProperty("ios_badge", "1");
        object.addProperty("ios_sound", "default");
        object.addProperty("notificationType", "incoming_call");
        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(quickBloxId);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        return createQBEvent(event);
    }

    @Override
    public Observable<Boolean> sendMissedCallNotificationToUser(int quickBloxId, String callType, int badgeNumber) {
        String messageData = String.format("You missed a %s call from %s.", callType, UserManager.getInstance().getUser().getDisplayName());
        JsonObject object = new JsonObject();
        object.addProperty("data", messageData);
        object.addProperty("message", messageData);
        object.addProperty("ios_badge", badgeNumber + 1);
        object.addProperty("ios_sound", "default");
        object.addProperty("ios_content_available", 1);
        object.addProperty("notificationType", "missed_call");
        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(quickBloxId);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        return createQBEvent(event);
    }

    @Override
    public Observable<Boolean> sendMessageNotification(String senderName, Conversation conversation, Message message,
                                                       User user, int badgeNumber) {
        //get incoming mask of target opponentUser
        boolean incomingMask = false;
        if (conversation.maskMessages != null
                && conversation.maskMessages.containsKey(user.key)){
            incomingMask = conversation.maskMessages.get(user.key);
        }

        String body = "";
        switch (message.messageType) {
            case Constant.MSG_TYPE_TEXT:
                String messageText = message.message;
                if (incomingMask && user.mappings != null && user.mappings.size() > 0) {
                    messageText = ServiceManager.getInstance().encodeMessage(user.mappings, message.message);
                }
                body = String.format("%s: %s", senderName, messageText);
                break;
            case Constant.MSG_TYPE_VOICE:
                body = senderName + ": sent a voice message.";
                break;
            case Constant.MSG_TYPE_IMAGE:
                body = senderName + ": sent a picture message.";
                break;
            case Constant.MSG_TYPE_GAME:
                body = senderName + ": sent a game.";
                break;
            default:
                break;
        }
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
        object.addProperty("conversationId", conversation.key);
        object.addProperty("photoUrl", message.photoUrl);
        object.addProperty("thumbUrl", message.thumbUrl);
        object.addProperty("audioUrl", message.audioUrl);
        object.addProperty("gameUrl", message.gameUrl);
        object.addProperty("senderId", UserManager.getInstance().getUser().key);
        object.addProperty("messageType", message.messageType);
        object.addProperty("ios_badge", badgeNumber + 1);

        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(user.quickBloxID);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
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
        object.addProperty("ios_category", "incoming_message");
        object.addProperty("notificationType", "incoming_message");
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
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        //this notification will send to both android and ios
        return createQBEvent(event);
    }

    private Observable<Boolean> createQBEvent(QBEvent event) {
        Performer<QBEvent> performer = QBPushNotifications.createEvent(event);
        return ((Observable<QBEvent>) performer.convertTo(RxJava2PerformProcessor.INSTANCE))
                .map(qbEvent -> true);
    }
}
