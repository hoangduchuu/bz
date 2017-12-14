package com.ping.android.service.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
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
            newValue.put("users/" + userId + "/groups/" + key, group.toMap());
        }
        updateBatchData(newValue, callback);
    }

    public void updateConversationId(Group group, String conversationId) {
        // Update conversation back to group
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("groups/%s/conversationID", group.key), conversationId);
        for (String userKey : group.memberIDs.keySet()) {
            updateValue.put(String.format("users/%s/groups/%s/conversationID", userKey, group.key), conversationId);
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

    public void addNewMembersToGroup(Group group, Conversation conversation, ArrayList<String> newMembers) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("groups/%s", group.key), group.toMap());
        updateValue.put(String.format("conversations/%s/memberIDs", conversation.key), conversation.memberIDs);
        conversation.message = "";
        for (String userId : newMembers) {
            updateValue.put(String.format("users/%s/groups/%s", userId, group.key), group.toMap());
            updateValue.put(String.format("users/%s/conversations/%s", userId, conversation.key), conversation.toMap());
        }
        for (String userId: group.memberIDs.keySet()) {
            if (!newMembers.contains(userId)) {
                updateValue.put(String.format("users/%s/groups/%s", userId, group.key), group.toMap());
                updateValue.put(String.format("users/%s/conversations/%s/memberIDs", userId, conversation.key), group.memberIDs);
            }
        }
        updateBatchData(updateValue, null);
    }
}
