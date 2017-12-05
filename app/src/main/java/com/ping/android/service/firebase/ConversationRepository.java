package com.ping.android.service.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuanluong on 12/1/17.
 */

public class ConversationRepository extends BaseFirebaseDatabase {
    @Override
    protected void initializeReference(FirebaseDatabase database) {
        databaseReference = database.getReference().child("conversations");
    }

    public void getConversation(String key, Callback callback) {
        databaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Conversation from = Conversation.from(dataSnapshot);
                    callback.complete(null, from);
                } else {
                    callback.complete(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.complete(databaseError);
            }
        });
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
                        callback.complete(new Error());
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
        updateValue.put(String.format("conversations/%s", key), conversation.toMap());
        for (String userKey : conversation.memberIDs.keySet()) {
            updateValue.put(String.format("users/%s/conversations/%s", userKey, key), conversation.toMap());
        }
        updateBatchData(updateValue, callback);
    }

    public void updateUserReadStatus(String conversationID, String userId, boolean value) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("conversations/%s/readStatuses/%s", conversationID, userId), value);
        updateValue.put(String.format("users/%s/conversations/%s/readStatuses/%s", userId, conversationID, userId), value);
        updateBatchData(updateValue, null);
    }

    public void updateNotificationSetting(String conversationId, String userId, boolean value) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("conversations/%s/notifications/%s", conversationId, userId), value);
        updateValue.put(String.format("users/%s/conversations/%s/notifications/%s", userId, conversationId, userId), value);
        updateBatchData(updateValue, null);
    }

    public void updateTypingIndicatorForUser(String conversationId, String userId, boolean typing) {
        databaseReference.child(conversationId).child("typingIndicator").child(userId).setValue(typing);
    }

    public void updateConversation(String conversationID, Conversation conversation, String senderId) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(String.format("conversations/%s", conversationID), conversation.toMap());
        // Update message for conversation for each user
        for (User toUser : conversation.members) {
            if (checkMessageBlocked(toUser, senderId)) continue;
            updateData.put(String.format("users/%s/conversations/%s", toUser.key, conversationID), conversation.toMap());
        }
        updateBatchData(updateData, null);
    }

    private boolean checkMessageBlocked(User toUser, String senderId) {
        boolean isBlocked = false;

        if (!toUser.key.equals(senderId) && isBlockBy(toUser, senderId)) {
            isBlocked = true;
        }
        return isBlocked;
    }

    private boolean isBlockBy(User contact, String senderId) {
        boolean isBlocked = false;
        if (contact != null && contact.blocks != null && contact.blocks.containsKey(senderId)) {
            isBlocked = contact.blocks.get(senderId);
        }
        return isBlocked;
    }
}
