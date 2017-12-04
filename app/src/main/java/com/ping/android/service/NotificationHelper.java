package com.ping.android.service;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.gson.JsonObject;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Log;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBEventType;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBSubscription;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bzzz on 11/29/17.
 */

public class NotificationHelper {

    private static NotificationHelper instance = new NotificationHelper();
    private static Context context;

    private NotificationHelper() {

    }

    public static NotificationHelper getInstance() {
        return instance;
    }

    public void sendCallingNotificationToUser(int quickBloxId, String callType) {
        String messageData = String.format("%s is %s calling", ServiceManager.getInstance().getCurrentUser().getDisplayName(), callType);
        JsonObject object = new JsonObject();
        object.addProperty("message", messageData);
        object.addProperty("ios_badge", "1");
        object.addProperty("ios_sound", "default");
        object.addProperty("notificationType", "incoming_call");
        QBEvent event = new QBEvent();
        event.setNotificationType(QBNotificationType.PUSH);
        event.addUserIds(quickBloxId);
        event.setType(QBEventType.ONE_SHOT);
        event.setMessage(object.toString());
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        QBPushNotifications.createEvent(event).performAsync(new QBEntityCallback<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(e);
            }
        });
    }

    public static void setContext(Context context) {
        NotificationHelper.context = context;
    }

    public void sendNotificationForConversation(Conversation conversation, Message fmessage) {
        QBEntityCallback<QBEvent> eventCallback = new QBEntityCallback<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(e);
            }
        };
        for (User user : conversation.members) {
            if (user.quickBloxID > 0 && user.key != ServiceManager.getInstance().getCurrentUser().key) {
                if (ServiceManager.getInstance().isBlock(user.key) || ServiceManager.getInstance().isBlockBy(user)) {
                    continue;
                    //need to check notification enabled for this conversation also
                }
                //get incoming mask of target user
                boolean incomingMask = false;
                String body, senderName;
                if (conversation.group != null) {
                    senderName = String.format("%s to %s", user.getDisplayName(), conversation.group.groupName);
                } else {
                    senderName = user.getDisplayName();
                }

                switch (conversation.messageType) {
                    case Constant.MSG_TYPE_TEXT:
                        body = String.format("%s: %s", senderName, incomingMask && user.mappings != null && user.mappings.size() > 0 ?
                                ServiceManager.getInstance().encodeMessage(user.mappings, fmessage.message) : fmessage.message);
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
                        return;
                }

                JsonObject object = new JsonObject();
                JsonObject notifcation = new JsonObject();
                JsonObject data = new JsonObject();


                notifcation.addProperty("body", body);
                notifcation.addProperty("title", "test title");
                object.addProperty("notification", notifcation.toString());

                data.addProperty("senderId", fmessage.senderId);
                data.addProperty("senderName", fmessage.senderName);
                object.addProperty("data", data.toString());


                object.addProperty("ios_badge", "1");
                object.addProperty("message", body);
                object.addProperty("ios_sound", "default");
                object.addProperty("ios_content_available", 1);
                object.addProperty("notificationType", "incoming_message");
                object.addProperty("timestamp", fmessage.timestamp);
                object.addProperty("senderName", fmessage.senderName);
                object.addProperty("conversationId", conversation.key);
                object.addProperty("photoUrl", fmessage.photoUrl);
                object.addProperty("thumbUrl", fmessage.thumbUrl);
                object.addProperty("audioUrl", fmessage.audioUrl);
                object.addProperty("gameUrl", fmessage.gameUrl);
                object.addProperty("senderId", ServiceManager.getInstance().getCurrentUser().key);
                object.addProperty("messageType", fmessage.messageType);
                QBEvent event = new QBEvent();
                event.setNotificationType(QBNotificationType.PUSH);
                event.addUserIds(user.quickBloxID);
                event.setType(QBEventType.ONE_SHOT);
                event.setMessage(object.toString());
                event.setEnvironment(QBEnvironment.DEVELOPMENT);
                //this notification will send to both android and ios
                QBPushNotifications.createEvent(event).performAsync(eventCallback);

            }
        }
    }
}