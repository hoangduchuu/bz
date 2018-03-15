package com.ping.android.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;

import com.google.gson.JsonObject;
import com.ping.android.data.repository.ConversationRepositoryImpl;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.service.firebase.MessageRepository;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Log;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBEventType;
import com.quickblox.messages.model.QBNotificationType;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by bzzz on 11/29/17.
 */

public class NotificationHelper {
    public static String REPLY_ACTION = "com.ping.android.service.NotificationHelper.REPLY_ACTION";
    public static String KEY_REPLY = "key_reply_message";

    private static NotificationHelper instance = new NotificationHelper();

    MessageRepository messageRepository;
    ConversationRepository conversationRepository = new ConversationRepositoryImpl();
    private com.ping.android.service.firebase.ConversationRepository fbConversationRepository;
    private User fromUser;
    private Conversation originalConversation;
    private NotificationHelper() {

    }

    public static NotificationHelper getInstance() {
        return instance;
    }

    public void sendCallingNotificationToUser(int quickBloxId, String callType) {
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

    public void sendNotificationForMissedCall(String userKey, int quickBloxId, String callType){
        BadgesHelper.getInstance().readUserBadgesWithCompletion(userKey, (error, data) -> {

            if (error != null){
                return;
            }
            int badges = (int)data[0];
            String messageData = String.format("You missed a %s call from %s.", callType, UserManager.getInstance().getUser().getDisplayName());
            JsonObject object = new JsonObject();
            object.addProperty("data", messageData);
            object.addProperty("message", messageData);
            object.addProperty("ios_badge", badges + 1);
            object.addProperty("ios_sound", "default");
            object.addProperty("ios_content_available", 1);
            object.addProperty("notificationType", "missed_call");
            QBEvent event = new QBEvent();
            event.setNotificationType(QBNotificationType.PUSH);
            event.addUserIds(quickBloxId);
            event.setType(QBEventType.ONE_SHOT);
            event.setMessage(object.toString());
            event.setEnvironment(QBEnvironment.DEVELOPMENT);
            QBPushNotifications.createEvent(event).performAsync(new QBEntityCallback<QBEvent>() {
                @Override
                public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                    BadgesHelper.getInstance().increaseUserBadges(userKey, "missed_call");
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(e);
                }
            });
        });
    }

    public void sendNotificationForConversation(Conversation conversation, Message fmessage) {
        String body;
        User sender = UserManager.getInstance().getUser();
        String senderName = sender.getDisplayName();
        Map<String, String> nickNames = conversation.nickNames;
        if (nickNames.containsKey(sender.key)) {
            senderName = nickNames.get(sender.key);
        }
        if (conversation.group != null) {
            senderName = String.format("%s to %s", senderName, conversation.group.groupName);
        }

        for (User user : conversation.members) {
            if (user.quickBloxID > 0 && !user.key.equals(UserManager.getInstance().getUser().key)) {
                if (!needSendNotification(conversation, user)) continue;
                //get incoming mask of target user
                boolean incomingMask = false;
                if(conversation.maskMessages != null && conversation.maskMessages.containsKey(user.key)){
                    incomingMask = conversation.maskMessages.get(user.key);
                }

                switch (fmessage.messageType) {
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
                object.addProperty("data", body);
                object.addProperty("message", body);
                object.addProperty("ios_sound", "default");
                object.addProperty("ios_content_available", 1);
                object.addProperty("notificationType", "incoming_message");
                object.addProperty("timestamp", fmessage.timestamp);
                object.addProperty("originMessage", fmessage.message);
                object.addProperty("senderName", fmessage.senderName);
                object.addProperty("conversation", conversation.key);
                object.addProperty("photoUrl", fmessage.photoUrl);
                object.addProperty("thumbUrl", fmessage.thumbUrl);
                object.addProperty("audioUrl", fmessage.audioUrl);
                object.addProperty("gameUrl", fmessage.gameUrl);
                object.addProperty("senderId", UserManager.getInstance().getUser().key);
                object.addProperty("messageType", fmessage.messageType);

                BadgesHelper.getInstance().readUserBadgesWithCompletion(user.key, (error, data) -> {

                    if (error != null) { return;}

                    int badges = (int)data[0];
                    object.addProperty("ios_badge", badges + 1);

                    QBEvent event = new QBEvent();
                    event.setNotificationType(QBNotificationType.PUSH);
                    event.addUserIds(user.quickBloxID);
                    event.setType(QBEventType.ONE_SHOT);
                    event.setMessage(object.toString());
                    event.setEnvironment(QBEnvironment.DEVELOPMENT);
                    //this notification will send to both android and ios
                    QBPushNotifications.createEvent(event).performAsync(new QBEntityCallback<QBEvent>() {
                        @Override
                        public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                            BadgesHelper.getInstance().increaseUserBadges(user.key, conversation.key);
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });
                });
            }
        }
    }

    private boolean needSendNotification(Conversation conversation, User user) {
        if (user == null) return false;
        if (ServiceManager.getInstance().isBlock(user.key) || ServiceManager.getInstance().isBlockBy(user)) {
            return false;
        }
        //check if target user enable notification
        if (conversation.notifications != null && conversation.notifications.containsKey(user.key) && !conversation.notifications.get(user.key)){
            return false;
        }
        return true;
    }

    public void sendGameStatusNotificationToSender(User user, Conversation conversation, boolean passed){
        if(!needSendNotification(conversation, user)) return;

        User currentUser = UserManager.getInstance().getUser();
        String body = currentUser.getDisplayName() + (passed ? " passed a game you sent.": " failed to complete a game you sent.");
        JsonObject object = new JsonObject();
        object.addProperty("data", body);

        object.addProperty("message", body);
        object.addProperty("ios_sound", "default");
        object.addProperty("ios_content_available", 1);
        object.addProperty("notificationType", "incoming_message");
        object.addProperty("senderName", currentUser.getDisplayName());
        object.addProperty("conversation", conversation.key);
        object.addProperty("senderId", currentUser.key);
        object.addProperty("messageType", Constant.MSG_TYPE_GAME);

        BadgesHelper.getInstance().readUserBadgesWithCompletion(user.key, (error, data) -> {

            if (error != null) {
                return;
            }

            int badges = (int) data[0];
            object.addProperty("ios_badge", badges + 1);
            QBEvent event = new QBEvent();
            event.setNotificationType(QBNotificationType.PUSH);
            event.addUserIds(user.quickBloxID);
            event.setType(QBEventType.ONE_SHOT);
            event.setMessage(object.toString());
            event.setEnvironment(QBEnvironment.DEVELOPMENT);
            //this notification will send to both android and ios
            QBPushNotifications.createEvent(event).performAsync(new QBEntityCallback<QBEvent>() {
                @Override
                public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                    BadgesHelper.getInstance().increaseUserBadges(user.key, conversation.key);
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        });
    }

    public static CharSequence getReplyMessage(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(KEY_REPLY);
        }
        return null;
    }

    public void sendMessage(String text, String conversationId) {
        fromUser = UserManager.getInstance().getUser();
        if (fbConversationRepository == null){
            fbConversationRepository = new com.ping.android.service.firebase.ConversationRepository();
        }
        messageRepository = MessageRepository.from(conversationId);
        conversationRepository.getConversation(fromUser.key, conversationId).subscribe(conversation -> {
            originalConversation = conversation;
            double timestamp = System.currentTimeMillis() / 1000d;
            Map<String, Boolean> readAllowed = getReadAllowed();
            Message message = Message.createTextMessage(text, fromUser.key, fromUser.getDisplayName(),
                    timestamp, getStatuses(), getMessageMarkStatuses(), getMessageDeleteStatuses(), readAllowed);

            Conversation newConversation = new Conversation(originalConversation.conversationType, Constant.MSG_TYPE_TEXT,
                    text, originalConversation.groupID, fromUser.key, originalConversation.memberIDs, getMessageMarkStatuses(),
                    getMessageReadStatuses(), timestamp, originalConversation);
            conversation.members = originalConversation.members;
            String messageKey = messageRepository.generateKey();
            messageRepository.updateMessage(messageKey, message, (error, data) ->{
                if (error == null) {
                    messageRepository.updateMessageStatus(message.key, originalConversation.memberIDs.keySet(), Constant.MESSAGE_STATUS_DELIVERED);
                } else {
                    messageRepository.updateMessageStatus(message.key, originalConversation.memberIDs.keySet(), Constant.MESSAGE_STATUS_ERROR);
                }
            });
            message.key = messageKey;
            fbConversationRepository.updateConversation(conversationId, newConversation, readAllowed);
            sendNotificationForConversation(conversation, message);
        });

    }

    private Map<String, Boolean> getMessageMarkStatuses() {
        Map<String, Boolean> markStatuses = new HashMap<>();
        if (originalConversation.maskMessages != null) {
            markStatuses.putAll(originalConversation.maskMessages);
        }
        return markStatuses;
    }

    private Map<String, Boolean> getMessageReadStatuses() {
        Map<String, Boolean> markStatuses = new HashMap<>();
        for (String toUserId : originalConversation.memberIDs.keySet()) {
            markStatuses.put(toUserId, false);
        }
        markStatuses.put(fromUser.key, true);
        return markStatuses;
    }

    private Map<String, Boolean> getMessageDeleteStatuses() {
        Map<String, Boolean> deleteStatuses = new HashMap<>();
        for (String toUserId : originalConversation.memberIDs.keySet()) {
            deleteStatuses.put(toUserId, false);
        }
        deleteStatuses.put(fromUser.key, false);
        return deleteStatuses;
    }

    private Map<String, Integer> getStatuses() {
        Map<String, Integer> deleteStatuses = new HashMap<>();
        for (String toUserId : originalConversation.memberIDs.keySet()) {
            deleteStatuses.put(toUserId, Constant.MESSAGE_STATUS_SENT);
        }
        deleteStatuses.put(fromUser.key, Constant.MESSAGE_STATUS_SENT);
        return deleteStatuses;
    }

    private Map<String, Boolean> getReadAllowed() {
        Map<String, Boolean> ret = new HashMap<>();
        ret.put(fromUser.key, true);
        // Check whether sender is in block list of receiver

        for (String toUserId : originalConversation.memberIDs.keySet()) {
            if (toUserId.equals(fromUser.key)
                    || fromUser.blocks.containsKey(toUserId)
                    || fromUser.blockBys.containsKey(toUserId)) continue;
            ret.put(toUserId, true);
        }

        return ret;
    }
}