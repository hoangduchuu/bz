package com.ping.android.service.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.model.Conversation;
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

    public void createConversation(String key, Conversation conversation, Callback callback) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("conversations/%s", key), conversation.toMap());
        for (String userKey : conversation.memberIDs.keySet()) {
            updateValue.put(String.format("users/%s/conversations/%s", userKey, key), conversation.toMap());
        }
        updateBatchData(updateValue, callback);
    }
}
