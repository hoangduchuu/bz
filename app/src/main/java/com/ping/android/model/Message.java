package com.ping.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.ping.android.model.enums.MessageCallType;
import com.ping.android.model.enums.MessageType;

import java.util.List;
import java.util.Map;

public class Message implements Parcelable {
    public String key;
    public String message;
    public String mediaUrl;
    public String thumbUrl;
    public String senderId;
    public String senderName;
    public double timestamp;
    public Map<String, Integer> status;
    public int callType;
    public int gameType;
    public int voiceType = 0;
    public double callDuration; // in seconds
    public List<Message> childMessages;
    public String parentKey;
    // Local variable, don't store on Firebase
    public String senderProfile;
    public String localFilePath;
    public boolean isCached;
    public String currentUserId;
    public String messageStatus;
    public int messageStatusCode;
    public long days;
    public boolean isMask;
    public MessageType type = MessageType.TEXT;
    public MessageCallType messageCallType = MessageCallType.VOICE_CALL;
    public boolean maskable;

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
        mediaUrl = in.readString();
        thumbUrl = in.readString();
        senderId = in.readString();
        senderName = in.readString();
        timestamp = in.readDouble();
        int messageType = in.readInt();
        type = MessageType.from(messageType);
        callType = in.readInt();
        gameType = in.readInt();
        voiceType = in.readInt();
        callDuration = in.readDouble();
        localFilePath = in.readString();
        isCached = in.readByte() != 0;
        currentUserId = in.readString();
        messageStatus = in.readString();
        messageStatusCode = in.readInt();
        days = in.readLong();
        isMask = in.readByte() != 0;
        showExtraInfo = in.readByte() != 0;
        opponentUser = in.readParcelable(User.class.getClassLoader());
        parentKey = in.readString();
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

    public boolean isFromMe() {
        return this.senderId.equals(currentUserId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message) {
            return key.equals(((Message) obj).key);
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
        dest.writeString(mediaUrl);
        dest.writeString(thumbUrl);
        dest.writeString(senderId);
        dest.writeString(senderName);
        dest.writeDouble(timestamp);
        dest.writeInt(type.ordinal());
        dest.writeInt(callType);
        dest.writeInt(gameType);
        dest.writeInt(voiceType);
        dest.writeDouble(callDuration);
        dest.writeString(localFilePath);
        dest.writeByte((byte) (isCached ? 1 : 0));
        dest.writeString(currentUserId);
        dest.writeString(messageStatus);
        dest.writeInt(messageStatusCode);
        dest.writeLong(days);
        dest.writeByte((byte) (isMask ? 1 : 0));
        dest.writeByte((byte) (showExtraInfo ? 1 : 0));
        dest.writeParcelable(opponentUser, flags);
        dest.writeString(parentKey);
    }
}
