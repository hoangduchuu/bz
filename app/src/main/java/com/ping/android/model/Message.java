package com.ping.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.model.enums.MessageCallType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.configs.Constant;

import java.util.HashMap;
import java.util.List;
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
    public int callType;
    public int gameType;
    public int voiceType = 0;
    public double callDuration; // in seconds
    public List<Message> childMessages;
    public String parentKey;
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

    protected Message(Parcel in) {
        key = in.readString();
        message = in.readString();
        photoUrl = in.readString();
        thumbUrl = in.readString();
        audioUrl = in.readString();
        videoUrl = in.readString();
        gameUrl = in.readString();
        senderId = in.readString();
        senderName = in.readString();
        timestamp = in.readDouble();
        messageType = in.readInt();
        callType = in.readInt();
        gameType = in.readInt();
        voiceType = in.readInt();
        callDuration = in.readDouble();
        sender = in.readParcelable(User.class.getClassLoader());
        localFilePath = in.readString();
        isCached = in.readByte() != 0;
        currentUserId = in.readString();
        messageStatus = in.readString();
        messageStatusCode = in.readInt();
        days = in.readLong();
        isMask = in.readByte() != 0;
        showExtraInfo = in.readByte() != 0;
        opponentUser = in.readParcelable(User.class.getClassLoader());
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

    public static Message createGroupImageMessage(String senderId, String senderName, MessageType messageType,
                                                  double timestamp, Map<String, Integer> status,
                                                  Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses,
                                                  Map<String, Boolean> readAllowed) {
        Message message = new Message();
        message.messageType = messageType.ordinal();
        message.type = messageType;
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
        dest.writeString(videoUrl);
        dest.writeString(gameUrl);
        dest.writeString(senderId);
        dest.writeString(senderName);
        dest.writeDouble(timestamp);
        dest.writeInt(messageType);
        dest.writeInt(callType);
        dest.writeInt(gameType);
        dest.writeInt(voiceType);
        dest.writeDouble(callDuration);
        dest.writeParcelable(sender, flags);
        dest.writeString(localFilePath);
        dest.writeByte((byte) (isCached ? 1 : 0));
        dest.writeString(currentUserId);
        dest.writeString(messageStatus);
        dest.writeInt(messageStatusCode);
        dest.writeLong(days);
        dest.writeByte((byte) (isMask ? 1 : 0));
        dest.writeByte((byte) (showExtraInfo ? 1 : 0));
        dest.writeParcelable(opponentUser, flags);
    }
}
