package com.ping.android.model;

import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.model.enums.MessageCallType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.DataSnapshotWrapper;
import com.ping.android.utils.configs.Constant;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Message {
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

    // Local variable, don't store on Firebase
    public User sender;
    public String localFilePath;
    public boolean isCached;
    public String currentUserId;
    public String messageStatus;
    public int messageStatusCode;
    public long days;
    public boolean isMask;
    public MessageType type = MessageType.TEXT;
    public MessageCallType messageCallType = MessageCallType.VOICE_CALL;

    /**
     * Indicates whether show user profile and date time or not
     */
    public boolean showExtraInfo = true;
    /**
     * Other user in PVP conversation
     */
    public User opponentUser;

    public Message() {
    }

    public static Message from(DataSnapshot dataSnapshot) {
        Message message = new Message();
        DataSnapshotWrapper wrapper = new DataSnapshotWrapper(dataSnapshot);
        message.message = wrapper.getStringValue("message");
        message.photoUrl = wrapper.getStringValue("photoUrl");
        message.thumbUrl = wrapper.getStringValue("thumbUrl");
        message.audioUrl = wrapper.getStringValue("audioUrl");
        message.gameUrl = wrapper.getStringValue("gameUrl");
        message.videoUrl = wrapper.getStringValue("videoUrl");
        message.messageType = wrapper.getIntValue("messageType", Constant.MSG_TYPE_TEXT);
        message.type = MessageType.from(message.messageType);
        message.timestamp = wrapper.getDoubleValue("timestamp", 0.0d);
        message.senderId = wrapper.getStringValue("senderId");
        message.senderName = wrapper.getStringValue("senderName");
        message.gameType = wrapper.getIntValue("gameType", 0);
        message.voiceType = wrapper.getIntValue("voiceType", 0);
        message.callType = wrapper.getIntValue("callType", 0);
        message.callDuration = wrapper.getIntValue("callDuration", 0);
        message.messageCallType = MessageCallType.from(message.callType);
        message.days = (long) (message.timestamp * 1000 / Constant.MILLISECOND_PER_DAY);
        message.status = new HashMap<>();
        Map<String, Object> status = (Map<String, Object>)dataSnapshot.child("status").getValue();
        if (status != null) {
            for (String k : status.keySet()) {
                Object value = status.get(k);
                int intValue = 0;
                if (value instanceof Long) {
                    intValue = ((Long) value).intValue();
                }
                message.status.put(k, intValue);
            }
        }

        message.markStatuses = (HashMap<String, Boolean>) dataSnapshot.child("markStatuses").getValue();
        message.deleteStatuses = (HashMap<String, Boolean>) dataSnapshot.child("deleteStatuses").getValue();
        message.readAllowed = (HashMap<String, Boolean>) dataSnapshot.child("readAllowed").getValue();

        Assert.assertNotNull(message);
        message.key = dataSnapshot.getKey();
        if (message.messageType == 0) {
            if (!TextUtils.isEmpty(message.message)) {
                message.messageType = Constant.MSG_TYPE_TEXT;
            } else if (!TextUtils.isEmpty(message.photoUrl)) {
                message.messageType = Constant.MSG_TYPE_IMAGE;
            } else if (!TextUtils.isEmpty(message.gameUrl)) {
                message.messageType = Constant.MSG_TYPE_GAME;
            } else if (!TextUtils.isEmpty(message.audioUrl)) {
                message.messageType = Constant.MSG_TYPE_VOICE;
            }
        }
        return message;
    }

    public static Message createTextMessage(String text, String senderId, String senderName,
                                            double timestamp, Map<String, Integer> status,
                                            Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses,
                                            Map<String, Boolean> readAllowed) {
        Message message = new Message();
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

    public static Message createImageMessage(String photoUrl, String thumbUrl, String senderId,
                                             String senderName, double timestamp, Map<String, Integer> status,
                                             Map<String, Boolean> markStatuses,
                                             Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed) {
        Message message = new Message();
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

    public static Message createAudioMessage(String audioUrl, String senderId, String senderName, double timestamp,
                                             Map<String, Integer> status, Map<String, Boolean> markStatuses,
                                             Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed, int voiceType) {
        Message message = new Message();
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

    public static Message createGameMessage(String gameUrl, String senderId, String senderName, double timestamp,
                                            Map<String, Integer> status, Map<String, Boolean> markStatuses,
                                            Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed, int gameType) {
        Message message = new Message();
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

    public static Message createVideoMessage(String fileUrl, String senderId, String senderName, double timestamp,
                                             Map<String, Integer> status, Map<String, Boolean> markStatuses,
                                             Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed) {
        Message message = new Message();
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

    public static Message createCallMessage(String senderId, String senderName, MessageType type,
                                            double timestamp, Map<String, Integer> status,
                                            Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses,
                                            Map<String, Boolean> readAllowed, int callType, double callDuration) {
        Message message = new Message();
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

    public boolean isFromMe() {
        return this.senderId.equals(currentUserId);
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
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message) {
            return timestamp == ((Message) obj).timestamp && key.equals(((Message) obj).key);
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
