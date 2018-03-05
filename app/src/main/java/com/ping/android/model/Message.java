package com.ping.android.model;


import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.ultility.Constant;

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
    public String gameUrl;
    public String senderId;
    public String senderName;
    public double timestamp;
    public Map<String, Integer> status;
    public Map<String, Boolean> markStatuses;
    public Map<String, Boolean> deleteStatuses;
    public Map<String, Boolean> readAllowed;
    public int messageType;
    public int gameType;

    // Local variable, don't store on Firebase
    public User sender;
    public String localImage;
    public boolean isCached;
    public String currentUserId;

    public Message() {
    }

    public static Message from(DataSnapshot dataSnapshot) {
        Message message = new Message();
        message.message = dataSnapshot.child("message").getValue(String.class);
        message.photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);
        message.thumbUrl = dataSnapshot.child("thumbUrl").getValue(String.class);
        message.audioUrl = dataSnapshot.child("audioUrl").getValue(String.class);
        message.gameUrl = dataSnapshot.child("gameUrl").getValue(String.class);
        if(dataSnapshot.child("timestamp").exists()) {
            message.timestamp = dataSnapshot.child("timestamp").getValue(Double.class);
        }else{
            message.timestamp = 0.0d;
        }
        message.senderId = dataSnapshot.child("senderId").getValue(String.class);
        message.senderName = dataSnapshot.child("senderName").getValue(String.class);
        Integer type = dataSnapshot.child("gameType").getValue(Integer.class);
        message.gameType = type != null ? type : 0;

        message.status = new HashMap<>();
        Map<String, Object> status = (Map<String, Object>)dataSnapshot.child("status").getValue();
        for (String k:status.keySet()
             ) {
            Object value = status.get(k);
            int intValue = 0;
            if (value instanceof Long){
                intValue = ((Long)value).intValue();
            }
            message.status.put(k, intValue);
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
        message.readAllowed = readAllowed;
        return message;
    }

    public static Message createAudioMessage(String audioUrl, String senderId, String senderName, double timestamp,
                                             Map<String, Integer> status, Map<String, Boolean> markStatuses,
                                             Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed) {
        Message message = new Message();
        message.audioUrl = audioUrl;
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.messageType = Constant.MSG_TYPE_VOICE;
        message.readAllowed = readAllowed;
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
        message.readAllowed = readAllowed;
        message.gameType = gameType;
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
        result.put("messageType", messageType);
        result.put("readAllowed", readAllowed);
        result.put("gameType", gameType);
        return result;
    }
}
