package com.ping.android.service.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.model.Message;
import com.ping.android.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void updateMessage(String key, Message message) {
        databaseReference.child(key).updateChildren(message.toMap());
    }

    public void updateMessageStatus(String key, List<User> members, long status) {
        if (members == null) return;
        for (User user : members) {
            databaseReference.child(key).child("status").child(user.key).setValue(status);
        }
    }

    public void updateThumbnailUrl(String messageKey, String thumbnailUrl) {
        databaseReference.child(messageKey).child("thumbUrl").setValue(thumbnailUrl);
    }

    public void updatePhotoUrl(String messageKey, String photoUrl) {
        databaseReference.child(messageKey).child("photoUrl").setValue(photoUrl);
    }

    public void updateMessageMask(List<Message> messages, String conversationId, String userId, boolean isLastMessage, boolean value) {
        Map<String, Object> updateValue = new HashMap<>();
        for (Message message : messages) {
            updateValue.put(String.format("messages/%s/%s/markStatuses/%s", conversationId, message.key, userId), value);
        }
        if (isLastMessage) {
            updateValue.put(String.format("conversations/%s/markStatuses/%s", conversationId, userId), value);
            updateValue.put(String.format("users/%s/conversations/%s/markStatuses/%s/", userId, conversationId, userId), value);
        }
        updateBatchData(updateValue, null);
    }
}
