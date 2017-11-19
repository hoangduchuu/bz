package com.ping.android.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Conversation {
    public String key;
    public Long messageType;
    public Long conversationType;
    public String message;
    public String groupID;
    public String senderId;
    public Double timesstamps;
    public Map<String, Boolean> memberIDs;
    public Map<String, Boolean> markStatuses;
    public Map<String, Boolean> readStatuses;
    public Map<String, Boolean> deleteStatuses;

    //Conversation setting
    public Map<String, Boolean> notifications;
    public Map<String, Boolean> maskMessages;
    public Map<String, Boolean> puzzleMessages;
    public Map<String, Boolean> maskOutputs;

    // Local variable, don't store on Firebase
    public List<User> members = new ArrayList<>();
    public Group group;
    public User opponentUser;

    public Conversation() {
    }

    public Conversation(DataSnapshot dataSnapshot) {
        this.key = dataSnapshot.getKey();
        this.messageType = CommonMethod.getLongOf(dataSnapshot.child("messageType").getValue());
        this.conversationType = CommonMethod.getLongOf(dataSnapshot.child("conversationType").getValue());
        this.message = CommonMethod.getStringOf(dataSnapshot.child("lastMessage").getValue());
        this.senderId = CommonMethod.getStringOf(dataSnapshot.child("senderId").getValue());
        this.groupID = CommonMethod.getStringOf(dataSnapshot.child("groupID").getValue());
        this.timesstamps = CommonMethod.getDoubleOf(dataSnapshot.child("timesstamps").getValue());
        this.memberIDs = (Map<String, Boolean>) dataSnapshot.child("memberIDs").getValue();
        this.markStatuses = (Map<String, Boolean>) dataSnapshot.child("markStatuses").getValue();
        this.readStatuses = (Map<String, Boolean>) dataSnapshot.child("readStatuses").getValue();
        this.deleteStatuses = (Map<String, Boolean>) dataSnapshot.child("deleteStatuses").getValue();
        this.notifications = (Map<String, Boolean>) dataSnapshot.child("notifications").getValue();
        this.maskMessages = (Map<String, Boolean>) dataSnapshot.child("maskMessages").getValue();
        this.puzzleMessages = (Map<String, Boolean>) dataSnapshot.child("puzzleMessages").getValue();
        this.maskOutputs = (Map<String, Boolean>) dataSnapshot.child("maskOutputs").getValue();
    }

    public Conversation(Long conversationType, Long messageType, String message, String groupID, String senderId, Map<String, Boolean> memberIDs,
                        Map<String, Boolean> markStatuses, Map<String, Boolean> readStatuses, Map<String, Boolean> deleteStatuses, Double timestamp,
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
            this.notifications = originalConversation.notifications;
            this.maskMessages = originalConversation.maskMessages;
            this.puzzleMessages = originalConversation.puzzleMessages;
            this.maskOutputs = originalConversation.maskOutputs;
        }
    }

    public static Conversation createNewConversation(String fromUserId, String toUserId) {
        Conversation conversation = new Conversation();
        conversation.messageType = Constant.MSG_TYPE_TEXT;
        conversation.conversationType = Constant.CONVERSATION_TYPE_INDIVIDUAL;
        conversation.senderId = fromUserId;
        conversation.timesstamps = System.currentTimeMillis() / 1000D;

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

        return conversation;
    }

    public static Conversation createNewGroupConversation(String fromUserId, Group group) {
        Conversation conversation = new Conversation();
        conversation.messageType = Constant.MSG_TYPE_TEXT;
        conversation.conversationType = Constant.CONVERSATION_TYPE_GROUP;
        conversation.groupID = group.key;
        conversation.senderId = fromUserId;
        conversation.timesstamps = System.currentTimeMillis() / 1000D;

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
