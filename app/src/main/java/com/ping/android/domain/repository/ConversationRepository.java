package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.model.Message;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface ConversationRepository {
    Observable<ChildEvent> registerConversationsUpdate(String userId);

    Observable<DataSnapshot> observeConversationValue(String conversationId);

    Observable<Message> sendMessage(Message message);
}
