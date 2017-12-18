package com.ping.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Conversation implements Parcelable {
    public String key;
    public int messageType;
    public int conversationType;
    @PropertyName("lastMessage")
    public String message;
    public String groupID;
    public String senderId;
    public double timesstamps;
    public Map<String, Boolean> memberIDs;
    public Map<String, Boolean> markStatuses;
    public Map<String, Boolean> readStatuses;
    public Map<String, Boolean> deleteStatuses;

    //Conversation setting
    public Map<String, Boolean> notifications;
    public Map<String, Boolean> maskMessages;
    public Map<String, Boolean> puzzleMessages;
    public Map<String, Boolean> maskOutputs;

//    public boolean notificationSetting;
//    public boolean maskMessagesSetting;
//    public bool

    // Local variable, don't store on Firebase
    public List<User> members = new ArrayList<>();
    public Group group;
    public User opponentUser;

    protected Conversation(Parcel in) {
        key = in.readString();
        messageType = in.readInt();
        conversationType = in.readInt();
        message = in.readString();
        groupID = in.readString();
        senderId = in.readString();
        timesstamps = in.readDouble();
        members = in.createTypedArrayList(User.CREATOR);
        opponentUser = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeInt(messageType);
        dest.writeInt(conversationType);
        dest.writeString(message);
        dest.writeString(groupID);
        dest.writeString(senderId);
        dest.writeDouble(timesstamps);
        dest.writeTypedList(members);
        dest.writeParcelable(opponentUser, flags);
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

    public static Conversation from(DataSnapshot dataSnapshot) {
        Conversation conversation = dataSnapshot.getValue(Conversation.class);
        Assert.assertNotNull(conversation);
        conversation.key = dataSnapshot.getKey();
        return conversation;
    }

    public Conversation() {

    }

    public Conversation(int conversationType, int messageType, String message, String groupID, String senderId, Map<String, Boolean> memberIDs,
                        Map<String, Boolean> markStatuses, Map<String, Boolean> readStatuses, Map<String, Boolean> deleteStatuses, double timestamp,
                        Conversation originalConversation
    ) {
        this.conversationType = conversationType;
        this.messageType = messageType;
        this.message = message;
        this.senderId = senderId;
        this.groupID = groupID;
        this.memberIDs = memberIDs;
        this.markStatuses = markStatuses;
        this.readStatuses = readStatuses;
        this.deleteStatuses = deleteStatuses;
        this.timesstamps = timestamp;

        if (originalConversation != null) {
            this.key = originalConversation.key;
            this.notifications = originalConversation.notifications;
            this.maskMessages = originalConversation.maskMessages;
            this.puzzleMessages = originalConversation.puzzleMessages;
            this.maskOutputs = originalConversation.maskOutputs;
            this.members = originalConversation.members;
            this.group = originalConversation.group;
        }
    }

    public static Conversation createNewConversation(String fromUserId, String toUserId) {
        Conversation conversation = new Conversation();
        conversation.messageType = Constant.MSG_TYPE_TEXT;
        conversation.conversationType = Constant.CONVERSATION_TYPE_INDIVIDUAL;
        conversation.senderId = fromUserId;
        conversation.timesstamps = System.currentTimeMillis() / 1000L;

        Map<String, Boolean> defaultTrueValues = new HashMap<>();
        defaultTrueValues.put(fromUserId, true);
        defaultTrueValues.put(toUserId, true);

        Map<String, Boolean> defaultFalseValues = new HashMap<>();
        defaultFalseValues.put(fromUserId, true);
        defaultFalseValues.put(toUserId, true);

        conversation.memberIDs = defaultTrueValues;
        conversation.markStatuses = defaultTrueValues;
        conversation.readStatuses = defaultFalseValues;
        conversation.deleteStatuses = defaultFalseValues;
        conversation.notifications = defaultTrueValues;

        return conversation;
    }

    public static Conversation createNewGroupConversation(String fromUserId, Group group) {
        Conversation conversation = new Conversation();
        conversation.messageType = Constant.MSG_TYPE_TEXT;
        conversation.conversationType = Constant.CONVERSATION_TYPE_GROUP;
        conversation.groupID = group.key;
        conversation.senderId = fromUserId;
        conversation.timesstamps = System.currentTimeMillis() / 1000L;

        Map<String, Boolean> defaultTrueValues = new HashMap<>();
        Map<String, Boolean> defaultFalseValues = new HashMap<>();
        for (String userKey : group.memberIDs.keySet()) {
            defaultTrueValues.put(userKey, true);
            defaultFalseValues.put(userKey, false);
        }

        conversation.memberIDs = defaultTrueValues;
        conversation.markStatuses = defaultTrueValues;
        conversation.readStatuses = defaultFalseValues;
        conversation.deleteStatuses = defaultFalseValues;
        conversation.notifications = defaultTrueValues;

        return conversation;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        //result.put("key", key);
        result.put("conversationType", conversationType);
        result.put("messageType", messageType);
        result.put("lastMessage", message);
        result.put("senderId", senderId);
        result.put("groupID", groupID);
        result.put("timesstamps", timesstamps);
        result.put("memberIDs", memberIDs);
        result.put("markStatuses", markStatuses);
        result.put("readStatuses", readStatuses);
        result.put("deleteStatuses", deleteStatuses);
        result.put("notifications", notifications);
        result.put("maskMessages", maskMessages);
        result.put("puzzleMessages", puzzleMessages);
        result.put("maskOutputs", maskOutputs);

        return result;
    }
}
