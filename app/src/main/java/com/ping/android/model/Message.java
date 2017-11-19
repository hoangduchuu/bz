package com.ping.android.model;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.utils.Log;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Message {
    public String key;
    public String message;
    public String photoUrl;
    public String thumbUrl;
    public String audioUrl;
    public String gameUrl;
    public String senderId;
    public String senderName;
    public Double timestamp;
    public Map<String, Long> status;
    public Map<String, Boolean> markStatuses;
    public Map<String, Boolean> deleteStatuses;
    public Long messageType;

    // Local variable, don't store on Firebase
    public User sender;

    public Message() {
    }

    public Message(DataSnapshot dataSnapshot) {
        try {
            this.key = dataSnapshot.getKey();
            this.messageType = CommonMethod.getLongOf(dataSnapshot.child("messageType").getValue());
            this.message = CommonMethod.getStringOf(dataSnapshot.child("message").getValue());
            this.photoUrl = CommonMethod.getStringOf(dataSnapshot.child("photoUrl").getValue());
            this.thumbUrl = CommonMethod.getStringOf(dataSnapshot.child("thumbUrl").getValue());
            this.audioUrl = CommonMethod.getStringOf(dataSnapshot.child("audioUrl").getValue());
            this.gameUrl = CommonMethod.getStringOf(dataSnapshot.child("gameUrl").getValue());
            this.senderId = CommonMethod.getStringOf(dataSnapshot.child("senderId").getValue());
            this.senderName = CommonMethod.getStringOf(dataSnapshot.child("senderName").getValue());
            this.timestamp = CommonMethod.getDoubleOf(dataSnapshot.child("timestamp").getValue());

            this.markStatuses = (Map<String, Boolean>) dataSnapshot.child("markStatuses").getValue();
            this.deleteStatuses = (Map<String, Boolean>) dataSnapshot.child("deleteStatuses").getValue();
            this.status = (Map<String, Long>) dataSnapshot.child("status").getValue();
        } catch (Exception ex) {
            Log.e(ex);
        }
    }

    public static Message createTextMessage(String text, String senderId, String senderName,
                                            Double timestamp, Map<String, Long> status, Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses) {
        Message message = new Message();
        message.message = text;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        return message;
    }

    public static Message createImageMessage(String photoUrl, String thumbUrl, String senderId,
                                             String senderName, Double timestamp, Map<String, Long> status,
                                             Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses) {
        Message message = new Message();
        message.photoUrl = photoUrl;
        message.thumbUrl = thumbUrl;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        return message;
    }

    public static Message createAudioMessage(String audioUrl, String senderId, String senderName, Double timestamp,
                                             Map<String, Long> status, Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses) {
        Message message = new Message();
        message.audioUrl = audioUrl;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        return message;
    }

    public static Message createGameMessage(String gameUrl, String senderId, String senderName, Double timestamp,
                                            Map<String, Long> status, Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses) {
        Message message = new Message();
        message.gameUrl = gameUrl;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
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
        result.put("timestamp", timestamp);
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("status", status);
        result.put("markStatuses", markStatuses);
        result.put("deleteStatuses", deleteStatuses);
        return result;
    }
}
