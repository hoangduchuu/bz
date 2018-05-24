package com.ping.android.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.Gson;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.configs.Constant;

import junit.framework.Assert;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Message implements Parcelable {
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
    public int gameType;
    public int voiceType = 0;
    public MessageType type;
    // Used for CALL messages
    public long duration;

    // Local variable, don't store on Firebase
    public User sender;
    public String localFilePath;
    public boolean isCached;
    public String currentUserId;
    public String messageStatus;
    public int messageStatusCode;
    public long days;
    public boolean isMask;

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

    protected Message(Parcel in) {
        key = in.readString();
        message = in.readString();
        photoUrl = in.readString();
        thumbUrl = in.readString();
        audioUrl = in.readString();
        gameUrl = in.readString();
        senderId = in.readString();
        senderName = in.readString();
        timestamp = in.readDouble();
        messageType = in.readInt();
        gameType = in.readInt();
        sender = in.readParcelable(User.class.getClassLoader());
        localFilePath = in.readString();
        isCached = in.readByte() != 0;
        currentUserId = in.readString();
        messageStatus = in.readString();
        messageStatusCode = in.readInt();
        days = in.readLong();
        isMask = in.readByte() != 0;
        showExtraInfo = in.readByte() != 0;

        Gson gson = new Gson();
        status = gson.fromJson(in.readString(), Map.class);
        markStatuses = gson.fromJson(in.readString(), Map.class);
        deleteStatuses = gson.fromJson(in.readString(), Map.class);
        readAllowed = gson.fromJson(in.readString(), Map.class);
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

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
        message.type = MessageType.Companion.from(message.messageType);
        message.timestamp = wrapper.getDoubleValue("timestamp", 0.0d);
        message.senderId = wrapper.getStringValue("senderId");
        message.senderName = wrapper.getStringValue("senderName");
        message.gameType = wrapper.getIntValue("gameType", 0);
        message.voiceType = wrapper.getIntValue("voiceType", 0);
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
        message.readAllowed = readAllowed;
        return message;
    }

    public static Message createCallMessage(String senderId, String senderName, MessageType type,
                                            double timestamp, Map<String, Integer> status,
                                            Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses,
                                            Map<String, Boolean> readAllowed) {
        Message message = new Message();
        message.messageType = type.ordinal();
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.readAllowed = readAllowed;
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
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message) {
            return timestamp == ((Message) obj).timestamp && key.equals(((Message) obj).key);
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(message);
        dest.writeString(photoUrl);
        dest.writeString(thumbUrl);
        dest.writeString(audioUrl);
        dest.writeString(gameUrl);
        dest.writeString(senderId);
        dest.writeString(senderName);
        dest.writeDouble(timestamp);
        dest.writeInt(messageType);
        dest.writeInt(gameType);
        dest.writeParcelable(sender, flags);
        dest.writeString(localFilePath);
        dest.writeByte((byte) (isCached ? 1 : 0));
        dest.writeString(currentUserId);
        dest.writeString(messageStatus);
        dest.writeInt(messageStatusCode);
        dest.writeLong(days);
        dest.writeByte((byte) (isMask ? 1 : 0));
        dest.writeByte((byte) (showExtraInfo ? 1 : 0));
        JSONObject jsonObject = new JSONObject(status);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(markStatuses);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(deleteStatuses);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(readAllowed);
        dest.writeString(jsonObject.toString());
    }

    public boolean isReadable(String key) {
        if (readAllowed != null
                && readAllowed.containsKey(key)) {
            return readAllowed.get(key);
        }
        return true;
    }
}
