package com.ping.android.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageCallType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.configs.Constant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class MessageEntity {
    public String key;
    public String message;
    public String photoUrl;
    public String thumbUrl;
    public String audioUrl;
    public String videoUrl;
    public String gameUrl;
    public String senderId;
    public String senderName;
    public double timestamp;
    public Map<String, Integer> status;
    public Map<String, Boolean> markStatuses;
    public Map<String, Boolean> deleteStatuses;
    public Map<String, Boolean> readAllowed;
    public int messageType;
    public int callType;
    public int gameType;
    public int voiceType = 0;
    public double callDuration; // in seconds
    public List<MessageEntity> childMessages;
    public int childCount;
    public String parentKey;
    public boolean isMask;
    public MessageType type = MessageType.TEXT;

    public MessageEntity() {
    }

    public static MessageEntity createTextMessage(String text, String senderId, String senderName,
                                                  double timestamp, Map<String, Integer> status,
                                                  Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses,
                                                  Map<String, Boolean> readAllowed) {
        MessageEntity message = new MessageEntity();
        message.message = text;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.messageType = Constant.MSG_TYPE_TEXT;
        message.readAllowed = readAllowed;
        return message;
    }

    public static MessageEntity createImageMessage(String photoUrl, String thumbUrl, String senderId,
                                                   String senderName, double timestamp, Map<String, Integer> status,
                                                   Map<String, Boolean> markStatuses,
                                                   Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed) {
        MessageEntity message = new MessageEntity();
        message.photoUrl = photoUrl;
        message.thumbUrl = thumbUrl;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.messageType = Constant.MSG_TYPE_IMAGE;
        message.type = MessageType.IMAGE;
        message.readAllowed = readAllowed;
        return message;
    }

    public static MessageEntity createAudioMessage(String audioUrl, String senderId, String senderName, double timestamp,
                                                   Map<String, Integer> status, Map<String, Boolean> markStatuses,
                                                   Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed, int voiceType) {
        MessageEntity message = new MessageEntity();
        message.audioUrl = audioUrl;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.messageType = Constant.MSG_TYPE_VOICE;
        message.type = MessageType.VOICE;
        message.readAllowed = readAllowed;
        message.voiceType = voiceType;
        return message;
    }

    public static MessageEntity createGameMessage(String gameUrl, String senderId, String senderName, double timestamp,
                                                  Map<String, Integer> status, Map<String, Boolean> markStatuses,
                                                  Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed, int gameType) {
        MessageEntity message = new MessageEntity();
        message.gameUrl = gameUrl;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.messageType = Constant.MSG_TYPE_GAME;
        message.type = MessageType.GAME;
        message.readAllowed = readAllowed;
        message.gameType = gameType;
        return message;
    }

    public static MessageEntity createVideoMessage(String fileUrl, String senderId, String senderName, double timestamp,
                                                   Map<String, Integer> status, Map<String, Boolean> markStatuses,
                                                   Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed) {
        MessageEntity message = new MessageEntity();
        message.videoUrl = fileUrl;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.messageType = Constant.MSG_TYPE_VIDEO;
        message.type = MessageType.VIDEO;
        message.readAllowed = readAllowed;
        return message;
    }

    public static MessageEntity createCallMessage(String senderId, String senderName, MessageType type,
                                                  double timestamp, Map<String, Integer> status,
                                                  Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses,
                                                  Map<String, Boolean> readAllowed, int callType, double callDuration) {
        MessageEntity message = new MessageEntity();
        message.messageType = type.ordinal();
        message.type = type;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.readAllowed = readAllowed;
        message.callType = callType;
        message.callDuration = callDuration;
        return message;
    }

    public static MessageEntity createGroupImageMessage(String senderId, String senderName, MessageType messageType,
                                                        double timestamp, Map<String, Integer> status,
                                                        Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses,
                                                        Map<String, Boolean> readAllowed, int childCount) {
        MessageEntity message = new MessageEntity();
        message.messageType = messageType.ordinal();
        message.type = messageType;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.readAllowed = readAllowed;
        message.childCount = childCount;
        return message;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("message", message);
        result.put("photoUrl", photoUrl);
        result.put("thumbUrl", thumbUrl);
        result.put("audioUrl", audioUrl);
        result.put("gameUrl", gameUrl);
        result.put("videoUrl", videoUrl);
        result.put("timestamp", timestamp);
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("status", status);
        result.put("markStatuses", markStatuses);
        result.put("deleteStatuses", deleteStatuses);
        result.put("messageType", messageType);
        result.put("readAllowed", readAllowed);
        result.put("gameType", gameType);
        result.put("voiceType", voiceType);
        result.put("callType", callType);
        result.put("callDuration", callDuration);
        result.put("childCount", childCount);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MessageEntity) {
            return key.equals(((MessageEntity) obj).key);
        }
        return false;
    }

    public boolean isReadable(String key) {
        if (readAllowed != null
                && readAllowed.containsKey(key)) {
            return readAllowed.get(key);
        }
        return true;
    }
}
