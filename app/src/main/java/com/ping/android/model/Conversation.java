package com.ping.android.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.gson.Gson;
import com.ping.android.model.enums.Color;
import com.ping.android.model.enums.MessageCallType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.configs.Constant;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Conversation implements Parcelable {
    public String key;
    public int conversationType;
    public String conversationName;
    public String conversationAvatarUrl;
    public int messageType;
    public int callType;
    @PropertyName("lastMessage")
    public String message;
    public String groupID;
    public String senderId;
    public double timesstamps;
    public Map<String, Boolean> memberIDs = new HashMap<>();
    public Map<String, Boolean> markStatuses = new HashMap<>();
    public Map<String, Boolean> readStatuses = new HashMap<>();
    public Map<String, Boolean> deleteStatuses = new HashMap<>();
    public Map<String, Double> deleteTimestamps = new HashMap<>();

    //Conversation setting
    public HashMap<String, Boolean> notifications = new HashMap<>();
    public Map<String, Boolean> maskMessages = new HashMap<>();
    public Map<String, Boolean> puzzleMessages = new HashMap<>();
    public Map<String, Boolean> maskOutputs = new HashMap<>();
    public Map<String, String> nickNames = new HashMap<>();
    public Map<String, Theme> themes = new HashMap<>();

    // Local variable, don't store on Firebase
    public boolean isRead = false;
    public boolean isMask = false;
    public double deleteTimestamp = 0.0;
    public String currentUserId;
    public String filterText;
    public Color currentColor = Color.DEFAULT;
    public MessageType type;
    public MessageCallType messageCallType;
    public List<User> members = new ArrayList<>();
    public Group group;
    public User opponentUser;

    protected Conversation(Parcel in) {
        key = in.readString();
        messageType = in.readInt();
        callType = in.readInt();
        conversationType = in.readInt();
        conversationName = in.readString();
        conversationAvatarUrl = in.readString();
        message = in.readString();
        groupID = in.readString();
        senderId = in.readString();
        timesstamps = in.readDouble();
        members = in.createTypedArrayList(User.CREATOR);
        opponentUser = in.readParcelable(User.class.getClassLoader());
        Gson gson = new Gson();
        memberIDs = gson.fromJson(in.readString(), Map.class);
        markStatuses = gson.fromJson(in.readString(), Map.class);
        readStatuses = gson.fromJson(in.readString(), Map.class);
        deleteStatuses = gson.fromJson(in.readString(), Map.class);
        deleteTimestamps = gson.fromJson(in.readString(), Map.class);

        notifications = (HashMap<String, Boolean>) in.readSerializable();
        maskMessages = gson.fromJson(in.readString(), Map.class);
        puzzleMessages = gson.fromJson(in.readString(), Map.class);
        maskOutputs = gson.fromJson(in.readString(), Map.class);
        nickNames = gson.fromJson(in.readString(), Map.class);

        deleteTimestamp = in.readDouble();
        currentColor = Color.valueOf(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeInt(messageType);
        dest.writeInt(callType);
        dest.writeInt(conversationType);
        dest.writeString(conversationName);
        dest.writeString(conversationAvatarUrl);
        dest.writeString(message);
        dest.writeString(groupID);
        dest.writeString(senderId);
        dest.writeDouble(timesstamps);
        dest.writeTypedList(members);
        dest.writeParcelable(opponentUser, flags);
        JSONObject jsonObject = new JSONObject(memberIDs);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(markStatuses);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(readStatuses);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(deleteStatuses);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(deleteTimestamps);
        dest.writeString(jsonObject.toString());


        //Conversation setting
        dest.writeSerializable(notifications);
        jsonObject = new JSONObject(maskMessages);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(puzzleMessages);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(maskOutputs);
        dest.writeString(jsonObject.toString());
        jsonObject = new JSONObject(nickNames);
        dest.writeString(jsonObject.toString());

        dest.writeDouble(deleteTimestamp);
        dest.writeString(currentColor.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    public Conversation() {

    }

    public Conversation(int conversationType, int messageType, int callType, String message,
                        String groupID, String senderId, Map<String, Boolean> memberIDs,
                        Map<String, Boolean> markStatuses, Map<String, Boolean> readStatuses, double timestamp,
                        Conversation originalConversation
    ) {
        this.conversationType = conversationType;
        this.messageType = messageType;
        this.callType = callType;
        this.message = message;
        this.senderId = senderId;
        this.groupID = groupID;
        this.memberIDs = memberIDs;
        this.markStatuses = markStatuses;
        this.readStatuses = readStatuses;
        this.timesstamps = timestamp;

        if (originalConversation != null) {
            this.key = originalConversation.key;
            this.notifications = originalConversation.notifications;
            this.maskMessages = originalConversation.maskMessages;
            this.puzzleMessages = originalConversation.puzzleMessages;
            this.maskOutputs = originalConversation.maskOutputs;
            this.deleteStatuses = originalConversation.deleteStatuses;
            this.deleteTimestamps = originalConversation.deleteTimestamps;
            this.members = originalConversation.members;
            this.group = originalConversation.group;
            this.nickNames = originalConversation.nickNames;
            this.themes = originalConversation.themes;
            if (!TextUtils.isEmpty(groupID)) {
                this.conversationName = originalConversation.conversationName;
                this.conversationAvatarUrl = originalConversation.conversationAvatarUrl;
            }
        }
    }

    public static Conversation createNewConversation(String fromUserId, String toUserId) {
        Conversation conversation = new Conversation();
        conversation.messageType = Constant.MSG_TYPE_TEXT;
        conversation.conversationType = Constant.CONVERSATION_TYPE_INDIVIDUAL;
        conversation.senderId = fromUserId;
        conversation.timesstamps = System.currentTimeMillis() / 1000d;

        HashMap<String, Boolean> defaultTrueValues = new HashMap<>();
        defaultTrueValues.put(fromUserId, true);
        defaultTrueValues.put(toUserId, true);

        HashMap<String, Boolean> defaultFalseValues = new HashMap<>();
        defaultFalseValues.put(fromUserId, false);
        defaultFalseValues.put(toUserId, false);

        conversation.memberIDs = defaultTrueValues;
        conversation.markStatuses = defaultTrueValues;
        conversation.readStatuses = defaultFalseValues;
        conversation.deleteStatuses = defaultFalseValues;
        conversation.notifications = defaultTrueValues;
        conversation.deleteTimestamps = new HashMap<>();

        return conversation;
    }

    public static Conversation createNewGroupConversation(String fromUserId, Group group) {
        Conversation conversation = new Conversation();
        conversation.messageType = Constant.MSG_TYPE_TEXT;
        conversation.conversationName = group.groupName;
        conversation.conversationAvatarUrl = group.groupAvatar;
        conversation.conversationType = Constant.CONVERSATION_TYPE_GROUP;
        conversation.groupID = group.key;
        conversation.senderId = fromUserId;
        conversation.timesstamps = System.currentTimeMillis() / 1000d;

        HashMap<String, Boolean> defaultTrueValues = new HashMap<>();
        HashMap<String, Boolean> defaultFalseValues = new HashMap<>();
        for (String userKey : group.memberIDs.keySet()) {
            defaultTrueValues.put(userKey, true);
            defaultFalseValues.put(userKey, false);
        }

        conversation.memberIDs = defaultTrueValues;
        conversation.markStatuses = defaultTrueValues;
        conversation.readStatuses = defaultFalseValues;
        conversation.deleteStatuses = defaultFalseValues;
        conversation.deleteTimestamps = new HashMap<>();
        conversation.notifications = defaultTrueValues;

        return conversation;
    }

    public Color getColor(String key) {
        if (themes != null && themes.containsKey(key)) {
            Theme theme = themes.get(key);
            return Color.from(theme.mainColor);
        }
        return Color.DEFAULT;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        //result.put("key", key);
        result.put("conversationType", conversationType);
        result.put("messageType", messageType);
        result.put("callType", callType);
        result.put("lastMessage", message);
        result.put("conversationName", conversationName);
        result.put("conversationAvatarUrl", conversationAvatarUrl);
        result.put("senderId", senderId);
        result.put("groupID", groupID);
        result.put("timesstamps", timesstamps);
        result.put("memberIDs", memberIDs);
        result.put("markStatuses", markStatuses);
        result.put("readStatuses", readStatuses);
        result.put("deleteStatuses", deleteStatuses);
        result.put("deleteTimestamps", deleteTimestamps);
        result.put("notifications", notifications);
        result.put("maskMessages", maskMessages);
        result.put("puzzleMessages", puzzleMessages);
        result.put("maskOutputs", maskOutputs);
        result.put("nickNames", nickNames);
        result.put("themes", themes);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Conversation) {
            return key.equals(((Conversation) obj).key);
        }
        return false;
    }

    public boolean isValid() {
        return timesstamps > deleteTimestamp;
    }

    public boolean isFromMe() {
        return senderId.equals(currentUserId);
    }

    public User getUser(String userId) {
        for (User user : members) {
            if (userId.equals(user.key)) {
                return user;
            }
        }
        return null;
    }
}
