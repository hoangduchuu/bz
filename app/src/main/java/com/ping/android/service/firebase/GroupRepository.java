package com.ping.android.service.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.ultility.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuanluong on 12/1/17.
 */

public class GroupRepository extends BaseFirebaseDatabase {
    @Override
    protected void initializeReference(FirebaseDatabase database) {
        databaseReference = database.getReference().child("groups");
    }

    public void createGroup(String key, Group group, Callback callback) {
        databaseReference.child(key).setValue(group.toMap());
        // Set groups reference to members
        Map<String, Object> newValue = new HashMap<>();
        for (String userId : group.memberIDs.keySet()) {
            newValue.put("/groups/" + userId + "/" + key, group.toMap());
        }
        updateBatchData(newValue, callback);
    }

    public void updateConversationId(Group group, String conversationId) {
        // Update conversation back to group
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("groups/%s/conversationID", group.key), conversationId);
        for (String userKey : group.memberIDs.keySet()) {
            updateValue.put(String.format("groups/%s/%s/conversationID", userKey, group.key), conversationId);
        }
        updateBatchData(updateValue, null);
    }

    public void loadGroup(String groupId, @NonNull Callback callback) {
        databaseReference.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group group = Group.from(dataSnapshot);
                callback.complete(null, group);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.complete("Get data error");
            }
        });
    }

    public void addNewMembersToGroup(Group group, Conversation conversation, ArrayList<String> newMembers, Callback callback) {
        Map<String, Object> updateValue = new HashMap<>();
        conversation.memberIDs.putAll(group.memberIDs);
        for (String userId : newMembers) {
            group.deleteStatuses.put(userId, null);
        }
        conversation.deleteStatuses = group.deleteStatuses;
        updateValue.put(String.format("groups/%s", group.key), group.toMap());
        updateValue.put(String.format("conversations/%s/memberIDs", conversation.key), conversation.memberIDs);
        updateValue.put(String.format("conversations/%s/deleteStatuses", conversation.key), conversation.deleteStatuses);
        conversation.message = "";
        for (String userId : newMembers) {
            updateValue.put(String.format("groups/%s/%s", userId, group.key), group.toMap());
            updateValue.put(String.format("conversations/%s/%s", userId, conversation.key), conversation.toMap());
        }
        for (String userId: group.memberIDs.keySet()) {
            if (!newMembers.contains(userId)) {
                updateValue.put(String.format("groups/%s/%s", userId, group.key), group.toMap());
                updateValue.put(String.format("conversations/%s/%s/memberIDs", userId, conversation.key), conversation.memberIDs);
                updateValue.put(String.format("conversations/%s/%s/deleteStatuses", userId, conversation.key), conversation.deleteStatuses);
            }
        }
        updateBatchData(updateValue, callback);
    }

    public void leaveGroup(Group group, Callback callback) {
        Map<String, Object> updateValue = new HashMap<>();
        //group.memberIDs.remove(currentUserId());
        group.deleteStatuses.put(currentUserId(), true);

        // 1. Remove group and conversation for current user
        updateValue.put(String.format("groups/%s/%s", currentUserId(), group.key), null);
        updateValue.put(String.format("conversations/%s/%s", currentUserId(), group.conversationID), null);

        // 2. Update members for group & conversation
        updateValue.put(String.format("groups/%s/deleteStatuses", group.key), group.deleteStatuses);
        updateValue.put(String.format("conversations/%s/deleteStatuses", group.conversationID), group.deleteStatuses);
        for (String userId : group.memberIDs.keySet()) {
            if (userId.equals(currentUserId())) continue;
            updateValue.put(String.format("groups/%s/%s/deleteStatuses", userId, group.key), group.deleteStatuses);
            updateValue.put(String.format("conversations/%s/%s/deleteStatuses", userId, group.conversationID), group.deleteStatuses);
        }
        updateBatchData(updateValue, callback);
    }
}
