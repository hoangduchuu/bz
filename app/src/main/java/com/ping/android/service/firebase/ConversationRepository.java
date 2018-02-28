package com.ping.android.service.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Nickname;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tuanluong on 12/1/17.
 */

public class ConversationRepository extends BaseFirebaseDatabase {
    @Override
    protected void initializeReference(FirebaseDatabase database) {
        databaseReference = database.getReference().child("conversations").child(UserManager.getInstance().getUser().key);
    }

    public void getMaskOutput(String conversationId, String userId, Callback callback) {
        databaseReference.child(conversationId).child("maskOutputs").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Boolean value = dataSnapshot.getValue(Boolean.class);
                    if (value != null) {
                        callback.complete(null, value);
                    } else {
                        callback.complete(null, false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.complete(databaseError);
            }
        });
    }

    public void getMaskMessageSetting(String conversationId, Callback callback) {
        databaseReference.child(conversationId).child("maskMessages")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void createConversation(String key, Conversation conversation, Callback callback) {
        Map<String, Object> updateValue = new HashMap<>();
        //updateValue.put(String.format("conversations/%s", key), conversation.toMap());
        for (String userKey : conversation.memberIDs.keySet()) {
            updateValue.put(String.format("conversations/%s/%s", userKey, key), conversation.toMap());
        }
        updateBatchData(updateValue, callback);
    }

    public void updateUserReadStatus(String conversationID, String userId, boolean value) {
        Map<String, Object> updateValue = new HashMap<>();
        //updateValue.put(String.format("conversations/%s/readStatuses/%s", conversationID, userId), value);
        updateValue.put(String.format("conversations/%s/%s/readStatuses/%s", userId, conversationID, userId), value);
        updateBatchData(updateValue, null);
    }

    public void updateNotificationSetting(String conversationId, String userId, boolean value, Callback callback) {
        Map<String, Object> updateValue = new HashMap<>();
        //updateValue.put(String.format("conversations/%s/notifications/%s", conversationId, userId), value);
        updateValue.put(String.format("conversations/%s/%s/notifications/%s", userId, conversationId, userId), value);
        updateBatchData(updateValue, callback);
    }

    public void changeMaskConversation(Conversation conversation, boolean data, Callback callback) {
        if(conversation.maskMessages == null) {
            conversation.maskMessages = new HashMap<>();
        }
        conversation.maskMessages.put(currentUserId(), data);
        Map<String, Object> updateValue = new HashMap<>();
        //updateValue.put(String.format("conversations/%s/maskMessages/%s", conversation.key, currentUserId()), data);
        for(String userID: conversation.memberIDs.keySet()) {
            updateValue.put(String.format("conversations/%s/%s/maskMessages/%s", userID, conversation.key, currentUserId()), data);
        }
        updateBatchData(updateValue, callback);
    }

    public void changePuzzleConversation(Conversation conversation, boolean data, Callback callback) {
        if(conversation.puzzleMessages == null) {
            conversation.puzzleMessages = new HashMap<>();
        }
        conversation.puzzleMessages.put(currentUserId(), data);
        Map<String, Object> updateValue = new HashMap<>();
        //updateValue.put(String.format("conversations/%s/puzzleMessages/%s", conversation.key, currentUserId()), data);
        for(String userID: conversation.memberIDs.keySet()) {
            updateValue.put(String.format("conversations/%s/%s/puzzleMessages/%s", userID, conversation.key, currentUserId()), data);
        }
        updateBatchData(updateValue, callback);
    }

    public void updateTypingIndicatorForUser(Conversation conversation, String userId, boolean typing) {
        Map<String, Object> updateData = new HashMap<>();
        for (User user: conversation.members) {
            updateData.put(String.format("conversations/%s/%s/typingIndicator/%s", user.key, conversation.key, userId), typing);
        }
        updateBatchData(updateData, null);
    }

    public void updateConversation(String conversationID, Conversation conversation,
                                   Map<String, Boolean> readAllowance) {
        Map<String, Object> updateData = new HashMap<>();
        //updateData.put(String.format("conversations/%s", conversationID), conversation.toMap());
        // Update message for conversation for each user
        for (String toUserId : conversation.memberIDs.keySet()) {
            if (!readAllowance.containsKey(toUserId)) continue;
            updateData.put(String.format("conversations/%s/%s", toUserId, conversationID), conversation.toMap());
        }
        updateBatchData(updateData, null);
    }

    public void updateUserNickname(String conversationId, Nickname nickname, Map<String, Boolean> userIds, Callback callback) {
        Map<String, Object> updateValue = new HashMap<>();
        //updateValue.put(String.format("conversations/%s/nickNames/%s", conversationId, nickname.userId), nickname.nickName);
        for (String userId : userIds.keySet()) {
            updateValue.put(String.format("conversations/%s/%s/nickNames/%s", userId, conversationId, nickname.userId), nickname.nickName);
        }
        updateBatchData(updateValue, callback);
    }

    public void deleteConversations(List<Conversation> conversations, Callback callback) {
        double timestamp = System.currentTimeMillis() / 1000d;
        Map<String, Object> updateValue = new HashMap<>();
        for (Conversation conversation : conversations) {
            //updateValue.put(String.format("conversations/%s/deleteStatuses/%s", conversation.key, currentUserId()), true);
            //updateValue.put(String.format("conversations/%s/deleteTimestamps/%s", conversation.key, currentUserId()), timestamp);
            for (String userId : conversation.memberIDs.keySet()) {
                updateValue.put(String.format("conversations/%s/%s/deleteStatuses/%s", userId, conversation.key, currentUserId()), true);
                updateValue.put(String.format("conversations/%s/%s/deleteTimestamps/%s", userId, conversation.key, currentUserId()), timestamp);
            }
        }
        updateBatchData(updateValue, callback);
    }
}
