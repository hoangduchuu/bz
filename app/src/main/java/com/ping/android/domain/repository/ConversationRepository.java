package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.model.Conversation;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface ConversationRepository {
    Observable<ChildEvent> registerConversationsUpdate(String userId);

    Observable<DataSnapshot> observeConversationValue(String conversationId);
}
