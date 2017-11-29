package com.ping.android.service.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.model.Message;

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

    public String generateMessageKey() {
        return databaseReference.push().getKey();
    }

    public void updateMessage(String key, Message message) {
        databaseReference.child(key).updateChildren(message.toMap());
    }
}
