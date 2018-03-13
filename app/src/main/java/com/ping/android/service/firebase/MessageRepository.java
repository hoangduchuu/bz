package com.ping.android.service.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.ultility.Callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tuanluong on 11/29/17.
 */

public class MessageRepository extends BaseFirebaseDatabase {

    @Override
    protected void initializeReference(FirebaseDatabase database) {
        databaseReference = database.getReference().child("messages");
    }

    private void setConversationId(String conversationId) {
        databaseReference = databaseReference.child(conversationId);
    }

    public static MessageRepository from(String conversationId) {
        MessageRepository repository = new MessageRepository();
        repository.setConversationId(conversationId);
        return repository;
    }

    public void updateMessage(String key, Message message, Callback callback) {
        databaseReference.child(key).updateChildren(message.toMap(), callback::complete);
    }

    public void updateMessageStatus(String key, Set<String> memberIds, long status) {
        if (memberIds == null) return;
        for (String userId : memberIds) {
            databaseReference.child(key).child("status").child(userId).setValue(status);
        }
    }

    public void updateThumbnailUrl(String messageKey, String thumbnailUrl) {
        databaseReference.child(messageKey).child("thumbUrl").setValue(thumbnailUrl);
    }

    public void updatePhotoUrl(String messageKey, String photoUrl) {
        databaseReference.child(messageKey).child("photoUrl").setValue(photoUrl);
    }

    public void updateMessageMask(List<Message> messages, String conversationId, String userId, boolean isLastMessage, boolean value, Callback callback) {
        Map<String, Object> updateValue = new HashMap<>();
        for (Message message : messages) {
            updateValue.put(String.format("messages/%s/%s/markStatuses/%s", conversationId, message.key, userId), value);
        }
        if (isLastMessage) {
            //updateValue.put(String.format("conversations/%s/markStatuses/%s", conversationId, userId), value);
            updateValue.put(String.format("conversations/%s/%s/markStatuses/%s/", userId, conversationId, userId), value);
        }
        updateBatchData(updateValue, callback);
    }

    public void deleteMessage(String conversationID, List<Message> messages, Callback callback) {
        Map<String, Object> updateValue = new HashMap<>();
        for (Message message : messages) {
            updateValue.put(String.format("messages/%s/%s/deleteStatuses/%s", conversationID, message.key, currentUserId()), true);
        }
        updateBatchData(updateValue, callback);
    }
}
