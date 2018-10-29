package com.ping.android.data.entity;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.data.db.AppDatabase;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.configs.Constant;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
@Table(database = AppDatabase.class)
public class MessageEntity extends BaseModel {
    @PrimaryKey
    public String key;
    @ForeignKey(tableClass = MessageEntity.class)
    public String parentKey;
    @Column
    public String message;
    @Column
    public String photoUrl;
    @Column
    public String thumbUrl;
    @Column
    public String audioUrl;
    @Column
    public String videoUrl;
    @Column
    public String gameUrl;
    @Column
    public String senderId;
    @Column
    public String senderName;
    @Column
    public double timestamp;
    @Column
    public String conversationId;
    public Map<String, Integer> status;
    public Map<String, Boolean> markStatuses;
    public Map<String, Boolean> deleteStatuses;
    public Map<String, Boolean> readAllowed;
    @Column
    public int messageType;
    @Column
    public int callType;
    @Column
    public int gameType;
    @Column
    public int voiceType = 0;
    @Column
    public double callDuration; // in seconds
    @Column
    public boolean isMask;
    @Column
    public int messageStatusCode;
    public List<MessageEntity> childMessages;
    public int childCount;
    public boolean isCached;
    public String fileUrl;

    public MessageEntity() {
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "childMessages")
    public List<MessageEntity> getChildMessages() {
        if (childMessages == null || childMessages.isEmpty()) {
            childMessages = SQLite.select()
                    .from(MessageEntity.class)
                    .where(MessageEntity_Table.parentKey_key.eq(key))
                    .queryList();
        }
        return childMessages;
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
        message.readAllowed = readAllowed;
        return message;
    }

    public static MessageEntity createStickerMessage(String photoUrl, String senderId,
                                                     String senderName, double timestamp, Map<String, Integer> status,
                                                     Map<String, Boolean> markStatuses,
                                                     Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed) {
        MessageEntity message = new MessageEntity();
        message.photoUrl = photoUrl;
        message.thumbUrl = "";
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.messageType = Constant.MSG_TYPE_STICKER;
        message.readAllowed = readAllowed;
        return message;
    }

    public static MessageEntity createGifMessage(String photoUrl, String senderId,
                                                   String senderName, double timestamp, Map<String, Integer> status,
                                                   Map<String, Boolean> markStatuses,
                                                   Map<String, Boolean> deleteStatuses, Map<String, Boolean> readAllowed) {
        MessageEntity message = new MessageEntity();
        message.photoUrl = photoUrl;
        message.thumbUrl = "";
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.messageType = Constant.MSG_TYPE_GIFS;
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
        message.readAllowed = readAllowed;
        return message;
    }

    public static MessageEntity createCallMessage(String senderId, String senderName, MessageType type,
                                                  double timestamp, Map<String, Integer> status,
                                                  Map<String, Boolean> markStatuses, Map<String, Boolean> deleteStatuses,
                                                  Map<String, Boolean> readAllowed, int callType, double callDuration) {
        MessageEntity message = new MessageEntity();
        message.messageType = type.ordinal();
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
                                                        Map<String, Boolean> readAllowed, List<MessageEntity> childMessages) {
        MessageEntity message = new MessageEntity();
        message.messageType = messageType.ordinal();
        message.senderId = senderId;
        message.senderName = senderName;
        message.timestamp = timestamp;
        message.status = status;
        message.markStatuses = markStatuses;
        message.deleteStatuses = deleteStatuses;
        message.readAllowed = readAllowed;
        message.childMessages = childMessages;
        message.childCount = childMessages.size();
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
        if (messageType == Constant.MSG_TYPE_IMAGE_GROUP && childMessages != null) {
            Map<String, Object> child = new HashMap<>();
            for (MessageEntity entity : childMessages) {
                child.put(entity.key, entity.toMap());
            }
            result.put("childMessages", child);
        }
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
