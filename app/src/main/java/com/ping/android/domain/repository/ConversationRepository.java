package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface ConversationRepository {
    Observable<String> getKey();

    Observable<Boolean> createConversation(Conversation conversation);

    Observable<ChildEvent> registerConversationsUpdate(String userId);

    Observable<DataSnapshot> observeConversationValue(String userId, String conversationId);

    Observable<String> getMessageKey(String conversationId);

    Observable<Message> sendMessage(String conversationId, Message message);

    Observable<Conversation> getConversation(String key, String conversationID);
}
